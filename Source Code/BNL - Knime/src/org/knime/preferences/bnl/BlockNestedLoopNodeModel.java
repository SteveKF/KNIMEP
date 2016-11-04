package org.knime.preferences.bnl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.data.RowKey;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.DatabaseUtility;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.bnl.algorithm.BlockNestedLoop;
import org.knime.preferences.bnl.algorithm.DominationChecker;
import org.knime.preferences.bnl.view.BlockNestedLoopStructure;
import org.knime.preferences.bnl.view.BlockNestedLoopStructure.SaveOption;
import org.knime.preferences.prefCreator.ConfigKeys;

/**
 * This is the model implementation of BlockNestedLoop. Uses a Block Nested Loop
 * to get the skyline points of a specific database table
 *
 * @author Stefan Wohlfart
 */
public class BlockNestedLoopNodeModel extends NodeModel {

	// *********** Ports:*************
	public static final int PORT_DATABASE_CONNECTION = 0;

	// *********** Config Keys:*************
	public static final String CFGKEY_DIMENSIONS = "dimensions";
	public static final String CFGKEY_WINDOW_SIZE = "windowSize";

	private static final String FILE_NAME_ALL = "dominatedPoints.xml";
	private static final String INTERNAL_MODEL_ALL = "internalModelAll";
	private static final String FILE_NAME_DIMENSIONS = "dimensions.xml";

	private static final String FILE_NAME_SKY = "skylinePoints.xml";
	private static final String INTERNAL_MODEL_SKY = "internalModelSky";
	private static final String INTERNAL_MODEL_DIMENSIONS = "internalModelDimensions";
	
	// *********** Settings Models:*************
	private final SettingsModelInteger windowSize = new SettingsModelInteger(CFGKEY_WINDOW_SIZE, 2);

	/**
	 * Save all DataCells
	 */
	private List<BlockNestedLoopStructure> all_structure;
	/**
	 * Save all skyline DataCells
	 */
	private List<BlockNestedLoopStructure> sky_structure;
	private String[] dimensions;

	/**
	 * Constructor for the node model.
	 */
	protected BlockNestedLoopNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		all_structure = new LinkedList<>();
		sky_structure = new LinkedList<>();

		// get the scoreQuery which was created from the preference creator node
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		if (flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY) == null)
			throw new IllegalArgumentException("Input needs to be from the PreferenceCreator node.");
		String scoreQuery = flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY).getStringValue();

		// create original table
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inData[PORT_DATABASE_CONNECTION].getSpec();
		BufferedDataTable originalData = createTable(spec, exec);

		// create table with Score-Query
		BufferedDataTable scoreTable = createTable(spec, exec, scoreQuery);
		
		DominationChecker domChecker = new DominationChecker(flowVars,scoreTable.getDataTableSpec());
		
		//run Block Nested Loop algorithm
		BlockNestedLoop bnl = new BlockNestedLoop(scoreTable, windowSize.getIntValue(),domChecker);
		bnl.computeSkyline();

		// create colIndexes to save data
		List<Integer> tmpColIndexes = new LinkedList<>();
		String[] names = originalData.getSpec().getColumnNames();

		for (int i = 0; i < names.length; i++) {
			String bool = flowVars.get(names[i]).getStringValue();

			if (bool.equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
				tmpColIndexes.add(i);

		}
		int[] colIndexes = tmpColIndexes.stream().mapToInt(i -> i).toArray();

		// set dimension which are considered for creating the view
		dimensions = new String[colIndexes.length];
		for (int i = 0; i < colIndexes.length; i++) {
			dimensions[i] = originalData.getSpec().getColumnSpec(colIndexes[i]).getName();
		}
		
		//set dimension to only 0 value (no view) if there exist a column with string values
		DataTableSpec originalSpec = originalData.getDataTableSpec();
		for(int i=0; i < colIndexes.length; i++){
			 if (!(originalSpec.getColumnSpec(colIndexes[i]).getType().isCompatible(DoubleValue.class))) {
				 	dimensions = new String[0];
	            }
		}
		


		// create BufferedDataTable which contains only skyline records
		int numColumn = originalData.getDataTableSpec().getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = originalData.getDataTableSpec().getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer container = exec.createDataContainer(newSpec);

		BufferedDataTable skyline = createSkylineTable(originalData, bnl.getSkylineKeys(), container, colIndexes);
		saveDominatedTable(originalData, bnl.getDominatedKeys(), colIndexes);

		return new PortObject[] { skyline, originalData };
	}

	private BufferedDataTable createSkylineTable(BufferedDataTable originalData, LinkedList<RowKey> skylineKeys,
			BufferedDataContainer container, int[] colIndexes) {

		List<DataRow> skyline = new LinkedList<DataRow>();
		for (DataRow row : originalData) {
			if (skylineKeys.contains(row.getKey())) {
				skyline.add(row);
			}

		}

		for (int i = 0; i < skyline.size(); i++) {

			container.addRowToTable(skyline.get(i));

			BlockNestedLoopStructure struct = new BlockNestedLoopStructure(skyline.get(i), SaveOption.SKYLINE,
					colIndexes);
			sky_structure.add(struct);
		}

		// finally close the container and get the result table.
		container.close();

		return container.getTable();
	}

	private void saveDominatedTable(BufferedDataTable originalData, List<RowKey> dominatedKeys, int[] colIndexes) {

		List<DataRow> dominatedPoints = new LinkedList<DataRow>();
		for (DataRow row : originalData) {
			if (dominatedKeys.contains(row.getKey())) {
				dominatedPoints.add(row);
			}
		}

		for (int i = 0; i < dominatedPoints.size(); i++) {
			BlockNestedLoopStructure struct = new BlockNestedLoopStructure(dominatedPoints.get(i), SaveOption.ALL,
					colIndexes);
			all_structure.add(struct);
		}
	}

	
	/**
	 * Creates a BufferedDataTable with the according DatabasePortObjectSpec spec and the ExecutionContext exec.
	 * The scoreQuery replaces the original query in the DatabasePortObjectSpec.
	 * @param spec - the DatabasePortObjectSpec of the input of this node
	 * @param exec - the ExecutionContext of this node
	 * @param scoreQuery - the scoreQuery which was a input for this node
	 * @return Returns the BufferedDataTable which was created with the input variables
	 * @throws CanceledExecutionException
	 * @throws SQLException
	 * @throws InvalidSettingsException
	 */
	private BufferedDataTable createTable(DatabasePortObjectSpec spec, ExecutionContext exec, String scoreQuery)
			throws CanceledExecutionException, SQLException, InvalidSettingsException {
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		dbSettings.setQuery(scoreQuery);
		DBReader reader = databaseUtil.getReader(dbSettings);
		BufferedDataTable table = reader.createTable(exec, credProvider);

		return table;
	}

	/**
	 * Creates a BufferedDataTable with the according DatabasePortObjectSpec spec and the ExecutionContext exec.
	 * @param spec - the DatabasePortObjectSpec of the input of this node
	 * @param exec - the ExecutionContext of this node
	 * @return Returns the BufferedDataTable which was created with the input variables
	 * @throws CanceledExecutionException
	 * @throws SQLException
	 * @throws InvalidSettingsException
	 */
	private BufferedDataTable createTable(DatabasePortObjectSpec spec, ExecutionContext exec)
			throws CanceledExecutionException, SQLException, InvalidSettingsException {
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		DBReader reader = databaseUtil.getReader(dbSettings);
		BufferedDataTable table = reader.createTable(exec, credProvider);

		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		//check if scoreQuery is available
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		if (flowVars.get("scoreQuery") == null)
			throw new IllegalArgumentException("Input needs to be from the PreferenceCreator node.");
	
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[PORT_DATABASE_CONNECTION];
		
		
		return new PortObjectSpec[] { spec.getDataTableSpec(), spec.getDataTableSpec() };
	}

	public List<BlockNestedLoopStructure> getDominatedPoints() {
		return all_structure;
	}

	public List<BlockNestedLoopStructure> getSkylinePoints() {
		return sky_structure;
	}

	public String[] getDimensions() {

		return dimensions;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		windowSize.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		windowSize.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		windowSize.validateSettings(settings);

	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		all_structure = new LinkedList<>();
		sky_structure = new LinkedList<>();

		// ALL
		File all_file = new File(internDir, FILE_NAME_ALL);
		FileInputStream all_fis = new FileInputStream(all_file);
		ModelContentRO all_modelContent = ModelContent.loadFromXML(all_fis);
		try {
			for (int i = 0; i < all_modelContent.getChildCount(); i++) {
				BlockNestedLoopStructure struct = new BlockNestedLoopStructure(SaveOption.ALL);
				ModelContentRO subModelContent = all_modelContent.getModelContent(SaveOption.ALL.toString() + i);
				struct.loadFrom(subModelContent);
				all_structure.add(struct);
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}

		// SKY
		File sky_file = new File(internDir, FILE_NAME_SKY);
		FileInputStream sky_fis = new FileInputStream(sky_file);
		ModelContentRO sky_modelContent = ModelContent.loadFromXML(sky_fis);
		try {
			for (int i = 0; i < sky_modelContent.getChildCount(); i++) {
				BlockNestedLoopStructure struct = new BlockNestedLoopStructure(SaveOption.SKYLINE);
				ModelContentRO subModelContent = sky_modelContent.getModelContent(SaveOption.SKYLINE.toString() + i);
				struct.loadFrom(subModelContent);
				sky_structure.add(struct);
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}

		// DIMENSIONS
		File dimensions_file = new File(internDir, FILE_NAME_DIMENSIONS);
		FileInputStream dimensions_fis = new FileInputStream(dimensions_file);
		ModelContentRO dimensions_modelContent = ModelContent.loadFromXML(dimensions_fis);
		try {
			dimensions = dimensions_modelContent.getStringArray(CFGKEY_DIMENSIONS);
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		boolean all_saving = false;
		boolean sky_saving = false;

		// create the main model content
		ModelContent all_modelContent = new ModelContent(INTERNAL_MODEL_ALL);

		// create the main model content
		ModelContent sky_modelContent = new ModelContent(INTERNAL_MODEL_SKY);

		if (all_structure.size() > 0) {

			for (int i = 0; i < all_structure.size(); i++) {
				// for each bin create a sub model content
				ModelContentWO subContent = all_modelContent.addModelContent(SaveOption.ALL.toString() + i);
				// save the bin to the sub model content
				all_structure.get(i).saveTo(subContent);
			}
			all_saving = true;
		}

		if (sky_structure.size() > 0) {
			for (int i = 0; i < sky_structure.size(); i++) {
				// for each bin create a sub model content
				ModelContentWO subContent = sky_modelContent.addModelContent(SaveOption.SKYLINE.toString() + i);
				// save the bin to the sub model content
				sky_structure.get(i).saveTo(subContent);
			}
			sky_saving = true;
		}

		if (all_saving) {
			// now all bins are stored to the model content
			// but the model content must be written to XML
			// internDir is the directory for this node
			File file = new File(internDir, FILE_NAME_ALL);
			FileOutputStream fos = new FileOutputStream(file);
			all_modelContent.saveToXML(fos);
		}
		if (sky_saving) {
			// now all bins are stored to the model content
			// but the model content must be written to XML
			// internDir is the directory for this node
			File file = new File(internDir, FILE_NAME_SKY);
			FileOutputStream fos = new FileOutputStream(file);
			sky_modelContent.saveToXML(fos);
		}

		// save dimensions to xml
		ModelContent dimension_modelContent = new ModelContent(INTERNAL_MODEL_DIMENSIONS);
		dimension_modelContent.addStringArray(CFGKEY_DIMENSIONS, dimensions);
		File file = new File(internDir, FILE_NAME_DIMENSIONS);
		FileOutputStream fos = new FileOutputStream(file);
		dimension_modelContent.saveToXML(fos);

	}

}