package org.knime.preferences.bnl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
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
 * This is the model implementation of the "Block Nested Loop" Node. It uses the
 * Block Nested Loop algorithm to get the skyline points of a specific database
 * table
 *
 * @author Stefan Wohlfart version 1.0
 */
public class BlockNestedLoopNodeModel extends NodeModel {

	// *********** Ports:*************
	public static final int PORT_DATABASE_CONNECTION = 0;

	// *********** Config Keys:*************
	public static final String CFGKEY_DIMENSIONS = "dimensions";
	public static final String CFGKEY_WINDOW_SIZE = "windowSize";

	private static final String FILE_NAME_ALL = "dominatedPoints.xml";
	private static final String INTERNAL_MODEL_ALL = "internalModelAll";

	private static final String FILE_NAME_SKY = "skylinePoints.xml";
	private static final String INTERNAL_MODEL_SKY = "internalModelSky";

	// *********** Settings Models:*************
	private SettingsModelIntegerBounded windowSize = new SettingsModelIntegerBounded(
			CFGKEY_WINDOW_SIZE, 2, 1, Integer.MAX_VALUE);
	private SettingsModelStringArray dimensionsModel = new SettingsModelStringArray(CFGKEY_DIMENSIONS, new String[0]);

	/**
	 * Save all or only the dominated DataRows
	 */
	private List<BlockNestedLoopStructure> all_structure;
	/**
	 * Save all skyline DataRows
	 */
	private List<BlockNestedLoopStructure> sky_structure;

	/**
	 * Constructor for the node model.
	 */
	protected BlockNestedLoopNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE }, new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE  });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		// check if score and preference query are available
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		if (flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY) == null
				&& flowVars.get(ConfigKeys.CFG_KEY_PREFERENCE_QUERY) == null)
			throw new InvalidSettingsException("Input needs to be from the Preference Creator node.");

		all_structure = new LinkedList<>();
		sky_structure = new LinkedList<>();

		// get the scoreQuery which was created from the preference creator node
		String scoreQuery = flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY).getStringValue();

		// create original table
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inData[PORT_DATABASE_CONNECTION].getSpec();
		BufferedDataTable originalData = createTable(spec, exec);

		// create table with score query
		BufferedDataTable scoreTable = createTable(spec, exec, scoreQuery);

		// create the domination checker which checks if a data points dominates
		// another one
		DominationChecker domChecker = new DominationChecker(flowVars, scoreTable.getDataTableSpec());

		// run Block Nested Loop algorithm
		BlockNestedLoop bnl = new BlockNestedLoop(scoreTable, windowSize.getIntValue(), domChecker);
		
		DataTableSpec originalSpec = originalData.getDataTableSpec();
		//get the indexes of the columns which should be displayed in the view
		int[] colIndexes = createColumnIndexes(originalSpec, flowVars);

		// create BufferedDataTable which contains only skyline records
		int numColumn = originalSpec.getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];
		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = originalSpec.getColumnSpec(i);
		}
		DataTableSpec newSpec = new DataTableSpec(newColumns);
		BufferedDataContainer container = exec.createDataContainer(newSpec);
		BufferedDataTable skyline = createSkylineTable(originalData, bnl.getSkylineKeys(), container, colIndexes);

		// create BufferedDataTable which contains only score skyline records
		BufferedDataTable scoreSkyline = createScoreSkylineTable(scoreTable, bnl.getSkylineKeys(), exec);


		saveDominatedTable(originalData, bnl.getDominatedKeys(), colIndexes);

		return new PortObject[] { skyline, scoreSkyline };
	}
	
	/**
	 * 
	 * @param spec - the DataTableSpec of the DataBasePortObject which entered this node
	 * @param flowVars - FlowVariables of this node
	 * @return Returns the indexes for the columns which should be displayed in the view
	 */
	private int[] createColumnIndexes(DataTableSpec spec, Map<String, FlowVariable> flowVars) {

		List<Integer> tmpColIndexes = new ArrayList<>();
		List<String> tmpDimension = new ArrayList<>(Arrays.asList(dimensionsModel.getStringArrayValue()));
		String[] columnNames = spec.getColumnNames();
		//get the dimensions which were selected in the dialog and get the indexes of them
		boolean newComputation = false;
		if (tmpDimension.size() > 0) {
			for (int i = 0; i < columnNames.length; i++) {
				if (tmpDimension.contains(columnNames[i]) && flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
					tmpColIndexes.add(i);
				else if(tmpDimension.contains(columnNames[i]) && !flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
					newComputation = true;
			}
		}else{
			newComputation = true;
		}
		//if no settings were entered by the user get the indexes of all columns which have preferences and are numeric
		if(newComputation){
			List<String> columnList = new ArrayList<>();
			tmpColIndexes = new ArrayList<>();
			for (int i = 0; i < columnNames.length; i++) {
				// only columns which have preferences and are numeric will be in the view
				if (flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
					if (spec.getColumnSpec(columnNames[i]).getType().isCompatible(DoubleValue.class)) {
						columnList.add(columnNames[i]);
						tmpColIndexes.add(i);
					}
			}
			
			String[] dims = new String[columnList.size()];
			dims = columnList.toArray(dims);
			dimensionsModel.setStringArrayValue(dims);
		}

		return tmpColIndexes.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Puts the skyline records into a data table and saves the skyline data
	 * rows into a BlockNestedLoopStructure
	 * 
	 * @param originalData
	 *            - the original data
	 * @param skylineKeys
	 *            - the skyline keys which are the output of a BlockNestedLoop
	 *            object
	 * @param container
	 *            - the container in which the skyline points will be added
	 * @param colIndexes
	 *            - column indexes which tell which columns should be saved in a
	 *            file
	 * @return Returns the created data table
	 */
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
	
	private BufferedDataTable createScoreSkylineTable(BufferedDataTable scoreTable, LinkedList<RowKey> skylineKeys,ExecutionContext exec){
		
		DataTableSpec scoreSpec = scoreTable.getDataTableSpec();
		int numColumn = scoreSpec.getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];
		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = scoreSpec.getColumnSpec(i);
		}
		DataTableSpec newSpec = new DataTableSpec(newColumns);
		BufferedDataContainer container = exec.createDataContainer(newSpec);
		
		List<DataRow> skyline = new LinkedList<DataRow>();
		for (DataRow row : scoreTable) {
			if (skylineKeys.contains(row.getKey())) {
				skyline.add(row);
			}

		}

		for (int i = 0; i < skyline.size(); i++) {

			container.addRowToTable(skyline.get(i));

		}

		// finally close the container and get the result table.
		container.close();

		return container.getTable();
	}

	/**
	 * Saves the data rows of a dominated data record in a
	 * BlockNestedLoopStructure
	 * 
	 * @param originalData
	 *            - original data
	 * @param dominatedKeys
	 *            - RowKeys of dominated DataRows
	 * @param colIndexes
	 *            - column indexes which tell which columns should be saved in a
	 *            file
	 */
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
	 * Creates a BufferedDataTable with the according DatabasePortObjectSpec
	 * spec and the ExecutionContext exec. The scoreQuery replaces the original
	 * query in the DatabasePortObjectSpec.
	 * 
	 * @param spec
	 *            - the DatabasePortObjectSpec of the input of this node
	 * @param exec
	 *            - the ExecutionContext of this node
	 * @param scoreQuery
	 *            - the scoreQuery which was a input for this node
	 * @return Returns the BufferedDataTable which was created with the input
	 *         variables
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
	 * Creates a BufferedDataTable with the according DatabasePortObjectSpec
	 * spec and the ExecutionContext exec.
	 * 
	 * @param spec
	 *            - the DatabasePortObjectSpec of the input of this node
	 * @param exec
	 *            - the ExecutionContext of this node
	 * @return Returns the BufferedDataTable which was created with the input
	 *         variables
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

		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[PORT_DATABASE_CONNECTION];

		return new PortObjectSpec[] { spec.getDataTableSpec(), null };
	}

	/**
	 * 
	 * @return Returns the structure of all or only dominated points
	 */
	public List<BlockNestedLoopStructure> getDominatedPoints() {
		return all_structure;
	}

	/**
	 * 
	 * @return Returns the structure of all skyline points
	 */
	public List<BlockNestedLoopStructure> getSkylinePoints() {
		return sky_structure;
	}

	/**
	 * 
	 * @return Returns the dimensions which have preferences on them
	 */
	public String[] getDimensions() {
		return dimensionsModel.getStringArrayValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		windowSize.saveSettingsTo(settings);
		dimensionsModel.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		windowSize.loadSettingsFrom(settings);
		dimensionsModel.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		windowSize.validateSettings(settings);
		dimensionsModel.validateSettings(settings);

	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// loads the skyline and dominated data rows structures to create a view

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
	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// saves the skyline and dominated data rows structures to create a view
		// if KNIME is restarted without executing the node again

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
		}

		if (sky_structure.size() > 0) {
			for (int i = 0; i < sky_structure.size(); i++) {
				// for each bin create a sub model content
				ModelContentWO subContent = sky_modelContent.addModelContent(SaveOption.SKYLINE.toString() + i);
				// save the bin to the sub model content
				sky_structure.get(i).saveTo(subContent);
			}
		}

			File dominatedFile = new File(internDir, FILE_NAME_ALL);
			FileOutputStream dominatedFos = new FileOutputStream(dominatedFile);
			all_modelContent.saveToXML(dominatedFos);

		
			File skylineFile = new File(internDir, FILE_NAME_SKY);
			FileOutputStream skylineFos = new FileOutputStream(skylineFile);
			sky_modelContent.saveToXML(skylineFos);
	}

}
