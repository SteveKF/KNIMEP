package org.knime.skyvisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;
import org.knime.skyvisualizer.SkylineStructure.SaveOption;

/**
 * This is the model implementation of RepresentativeSkylineGraph. A node to
 * visualize Skylines or Representative Skylines.
 *
 * @author Stefan Wohlfart
 */
public class SkylineVisualizerNodeModel extends NodeModel {

	
	public static final int UNDOMINATED_PORT = 0;
	public static final int DOMINATED_PORT = 1;
	
	private final String DIMENSIONS = "dimensionssaving";

	static final String CFGKEY_GRAPH_OPTIONS = "graphOptions";
	static final String CFGKEY_DIMENSIONS = "dimensions";
	static final String CFGKEY_CHART_NAME = "chartName";
	static final String CFGKEY_SUBTITLE = "subTitlte";
	static final String CFGKEY_DOMINATED_POINTS_NAME = "dominatedPointsName";
	static final String CFGKEY_UNDOMINATED_POINTS_NAME = "undominatedPointsName";

	private static final String FILE_NAME_DOMINATED = "dominated.xml";
	private static final String FILE_NAME_UNDOMINATED = "undominated.xml";
	private static final String FILE_NAME_DIMENSIONS = "dimensions.xml";

	private static final String INTERNAL_MODEL_DOMINATED = "internalModelDominated";
	private static final String INTERNAL_MODEL_UNDOMINATED = "internalModelUndominated";
	private static final String INTERNAL_MODEL_DIMENSIONS = "internalModelDimensions";

	private final SettingsModelString graphOptions = new SettingsModelString(CFGKEY_GRAPH_OPTIONS, SkylineVisualizerNodeDialog.options[1]);
	private SettingsModelString chartName = new SettingsModelString(CFGKEY_CHART_NAME, "");
	private SettingsModelString subTitle = new SettingsModelString(CFGKEY_SUBTITLE, "");
	private SettingsModelString dominatedPointsName = new SettingsModelString(CFGKEY_DOMINATED_POINTS_NAME, "");
	private SettingsModelString undominatedPointsName = new SettingsModelString(CFGKEY_UNDOMINATED_POINTS_NAME, "");

	private List<SkylineStructure> dominatedStruct;
	private List<SkylineStructure> undominatedStruct;
	private List<String> columnList;

	protected SkylineVisualizerNodeModel() {

		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE },
				new PortType[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws Exception {
		
		dominatedStruct = new LinkedList<>();
		undominatedStruct = new LinkedList<>();
		columnList = new LinkedList<String>();
		
		List<Integer> tmpColIndexes = new LinkedList<>();

		DataTableSpec spec = ((BufferedDataTable) inData[UNDOMINATED_PORT]).getDataTableSpec();
		Map<String,FlowVariable> flowVars = getAvailableInputFlowVariables();
		String[] columnNames = spec.getColumnNames();
		for(int i=0; i < columnNames.length; i++){
			//only columns which have preferences and are numeric will be in the view
			if(flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
				if(spec.getColumnSpec(columnNames[i]).getType().
						isCompatible(DoubleValue.class)){
					columnList.add(columnNames[i]);
					tmpColIndexes.add(i);
				}
		}
		
		int[] colIndexes = tmpColIndexes.stream().mapToInt(i->i).toArray();

		
		// SAVE DOMINATED POINTS IN A SKYLINESTRUCTURE
		for (DataRow row : (BufferedDataTable) inData[DOMINATED_PORT]) {
			SkylineStructure struct = new SkylineStructure(row, SaveOption.DOMINATED, colIndexes);
			dominatedStruct.add(struct);
		}

		// SAVE UNDOMINATED POINTS IN A SKYLINESTRUCTURE
		for (DataRow row : (BufferedDataTable) inData[UNDOMINATED_PORT]) {
			SkylineStructure struct = new SkylineStructure(row, SaveOption.UNDOMINATED, colIndexes);
			undominatedStruct.add(struct);
		}
		

		return new PortObject[] {};
	}

	public List<SkylineStructure> getUndominatedPoints() {
		return undominatedStruct;
	}

	public List<SkylineStructure> getDominatedPoints() {
		return dominatedStruct;
	}

	public String[] getDimensions() {
		String[] dims = new String[columnList.size()];
		dims = columnList.toArray(dims);
		return dims;
	}
	
	public String getGraphOption(){
		return graphOptions.getStringValue();
	}
	
	public String getCharName(){
		return chartName.getStringValue();
	}
	
	public String getSubTitle(){
		return subTitle.getStringValue();
	}
	
	public String getDominatedPointsName(){
		return dominatedPointsName.getStringValue();
	}
	
	public String getUndominatedPointsName(){
		return undominatedPointsName.getStringValue();
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
		
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		//get all columnNames of the original DataTable
		String[] columnNames = ((DataTableSpec) inSpecs[UNDOMINATED_PORT]).getColumnNames();
		
		//all these columns need to be a key of a flow variables. 
		//Otherwise not one of the parents of these node is a PreferenceCreator
		int counter = 0;
		for(int i=0; i < columnNames.length; i++){
			if(flowVars.containsKey(columnNames[i]))
				counter++;
		}
			
		if(columnNames.length != counter && counter > 0)
			throw new InvalidSettingsException("No preferences found. The data flow needs a PreferenceCreator node.");
		
		
		return new PortObjectSpec[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		graphOptions.saveSettingsTo(settings);
		chartName.saveSettingsTo(settings);
		subTitle.saveSettingsTo(settings);
		dominatedPointsName.saveSettingsTo(settings);
		undominatedPointsName.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		graphOptions.loadSettingsFrom(settings);
		chartName.loadSettingsFrom(settings);
		subTitle.loadSettingsFrom(settings);
		dominatedPointsName.loadSettingsFrom(settings);
		undominatedPointsName.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		graphOptions.validateSettings(settings);
		chartName.validateSettings(settings);
		subTitle.validateSettings(settings);
		dominatedPointsName.validateSettings(settings);
		undominatedPointsName.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		dominatedStruct = new LinkedList<>();
		undominatedStruct = new LinkedList<>();
		columnList = new LinkedList<>();

		// ALL
		File all_file = new File(internDir, FILE_NAME_DOMINATED);
		FileInputStream all_fis = new FileInputStream(all_file);
		ModelContentRO all_modelContent = ModelContent.loadFromXML(all_fis);
		try {
			for (int i = 0; i < all_modelContent.getChildCount(); i++) {
				SkylineStructure struct = new SkylineStructure(SaveOption.DOMINATED);
				ModelContentRO subModelContent = all_modelContent.getModelContent(SaveOption.DOMINATED.toString() + i);
				struct.loadFrom(subModelContent);
				dominatedStruct.add(struct);
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}

		
		// SKY
		File sky_file = new File(internDir, FILE_NAME_UNDOMINATED);
		FileInputStream sky_fis = new FileInputStream(sky_file);
		ModelContentRO sky_modelContent = ModelContent.loadFromXML(sky_fis);
		try {
			for (int i = 0; i < sky_modelContent.getChildCount(); i++) {
				SkylineStructure struct = new SkylineStructure(SaveOption.UNDOMINATED);
				ModelContentRO subModelContent = sky_modelContent
						.getModelContent(SaveOption.UNDOMINATED.toString() + i);
				struct.loadFrom(subModelContent);
				undominatedStruct.add(struct);
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}

		// DIMENSIONS
		File dimensions_file = new File(internDir, FILE_NAME_DIMENSIONS);
		FileInputStream dimensions_fis = new FileInputStream(dimensions_file);
		ModelContentRO dimensions_modelContent = ModelContent.loadFromXML(dimensions_fis);
		try {
				String[] dims = dimensions_modelContent.getStringArray(DIMENSIONS);
				columnList = Arrays.asList(dims);
	
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		boolean dominated_saving = false;
		boolean undominated_saving = false;
		boolean dimensions_saving = false;

		// create the main model content
		ModelContent all_modelContent = new ModelContent(INTERNAL_MODEL_DOMINATED);

		// create the main model content
		ModelContent sky_modelContent = new ModelContent(INTERNAL_MODEL_UNDOMINATED);

		// create the main model content
		ModelContent dimensions_modelContent = new ModelContent(INTERNAL_MODEL_DIMENSIONS);

		if (dominatedStruct.size() > 0) {

			for (int i = 0; i < dominatedStruct.size(); i++) {
				// for each bin create a sub model content
				ModelContentWO subContent = all_modelContent.addModelContent(SaveOption.DOMINATED.toString() + i);
				// save the bin to the sub model content
				dominatedStruct.get(i).saveTo(subContent);
			}
			dominated_saving = true;
		}

		if (undominatedStruct.size() > 0) {
			for (int i = 0; i < undominatedStruct.size(); i++) {
				// for each bin create a sub model content
				ModelContentWO subContent = sky_modelContent.addModelContent(SaveOption.UNDOMINATED.toString() + i);
				// save the bin to the sub model content
				undominatedStruct.get(i).saveTo(subContent);
			}
			undominated_saving = true;
		}

		if (columnList.size() > 0) {
			String[] dims = new String[columnList.size()];
			dims = columnList.toArray(dims);
			dimensions_modelContent.addStringArray(DIMENSIONS, dims);
			dimensions_saving = true;
		}

		if (dominated_saving) {
			// now all bins are stored to the model content
			// but the model content must be written to XML
			// internDir is the directory for this node
			File file = new File(internDir, FILE_NAME_DOMINATED);
			FileOutputStream fos = new FileOutputStream(file);
			all_modelContent.saveToXML(fos);
		}
		if (undominated_saving) {
			// now all bins are stored to the model content
			// but the model content must be written to XML
			// internDir is the directory for this node
			File file = new File(internDir, FILE_NAME_UNDOMINATED);
			FileOutputStream fos = new FileOutputStream(file);
			sky_modelContent.saveToXML(fos);
		}

		if (dimensions_saving) {
			File file = new File(internDir, FILE_NAME_DIMENSIONS);
			FileOutputStream fos = new FileOutputStream(file);
			dimensions_modelContent.saveToXML(fos);
		}

	}
}