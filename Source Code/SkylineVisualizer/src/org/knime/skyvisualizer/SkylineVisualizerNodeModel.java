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
 * <code>NodeModel</code> for the "(Representative) Skyline Visualizer" Node. A
 * node to visualize skylines with their dominated points or representative
 * skylines with the corresponding skyline.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
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

	private final SettingsModelString graphOptions = new SettingsModelString(CFGKEY_GRAPH_OPTIONS,
			SkylineVisualizerNodeDialog.options[1]);
	private SettingsModelString chartName = new SettingsModelString(CFGKEY_CHART_NAME, "");
	private SettingsModelString subTitle = new SettingsModelString(CFGKEY_SUBTITLE, "");
	private SettingsModelString dominatedPointsName = new SettingsModelString(CFGKEY_DOMINATED_POINTS_NAME, "");
	private SettingsModelString undominatedPointsName = new SettingsModelString(CFGKEY_UNDOMINATED_POINTS_NAME, "");

	private List<SkylineStructure> dominatedStruct;
	private List<SkylineStructure> undominatedStruct;
	private List<String> columnList;

	protected SkylineVisualizerNodeModel() {

		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE }, new PortType[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		if (!flowVars.containsKey(ConfigKeys.CFG_KEY_PREFERENCE_QUERY)
				&& !flowVars.containsKey(ConfigKeys.CFG_KEY_SCORE_QUERY))
			throw new InvalidSettingsException("No preferences found. The data flow needs a PreferenceCreator node.");

		dominatedStruct = new LinkedList<>();
		undominatedStruct = new LinkedList<>();
		columnList = new LinkedList<String>();

		List<Integer> tmpColIndexes = new LinkedList<>();

		DataTableSpec spec = ((BufferedDataTable) inData[UNDOMINATED_PORT]).getDataTableSpec();
		String[] columnNames = spec.getColumnNames();
		for (int i = 0; i < columnNames.length; i++) {
			// only columns which have preferences and are numeric will be in
			// the view
			if (flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
				if (spec.getColumnSpec(columnNames[i]).getType().isCompatible(DoubleValue.class)) {
					columnList.add(columnNames[i]);
					tmpColIndexes.add(i);
				}
		}

		int[] colIndexes = tmpColIndexes.stream().mapToInt(i -> i).toArray();

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

	/**
	 * 
	 * @return Returns the undominated points in a SkylineStructure
	 */
	public List<SkylineStructure> getUndominatedPoints() {
		return undominatedStruct;
	}

	/**
	 * 
	 * @return Returns the dominated points in a SkylineStructure
	 */
	public List<SkylineStructure> getDominatedPoints() {
		return dominatedStruct;
	}

	/**
	 * 
	 * @return Returns the dimensions which have preferences on it and are numeric
	 */
	public String[] getDimensions() {
		String[] dims = new String[columnList.size()];
		dims = columnList.toArray(dims);
		return dims;
	}

	/**
	 * 
	 * @return Returns the graph option
	 */
	public String getGraphOption() {
		return graphOptions.getStringValue();
	}

	/**
	 * 
	 * @return Returns the chart name
	 */
	public String getChartName() {
		return chartName.getStringValue();
	}

	/**
	 * 
	 * @return Returns the sub title
	 */
	public String getSubTitle() {
		return subTitle.getStringValue();
	}

	/**
	 * 
	 * @return Returns the legend name for the dominated points
	 */
	public String getDominatedPointsName() {
		return dominatedPointsName.getStringValue();
	}

	/**
	 * 
	 * @return Returns the legend name for the undominated points
	 */
	public String getUndominatedPointsName() {
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

		//method which loads data from a intern file
		
		dominatedStruct = new LinkedList<>();
		undominatedStruct = new LinkedList<>();
		columnList = new LinkedList<>();

		// create new file for dominated points
		File all_file = new File(internDir, FILE_NAME_DOMINATED);
		FileInputStream all_fis = new FileInputStream(all_file);
		ModelContentRO all_modelContent = ModelContent.loadFromXML(all_fis);
		try {
			//try to get every child of the main model and create a skyline structure
			for (int i = 0; i < all_modelContent.getChildCount(); i++) {
				SkylineStructure struct = new SkylineStructure(SaveOption.DOMINATED);
				ModelContentRO subModelContent = all_modelContent.getModelContent(SaveOption.DOMINATED.toString() + i);
				struct.loadFrom(subModelContent);
				dominatedStruct.add(struct);
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e.getMessage());
		}

		// create new file for undominated points
		File sky_file = new File(internDir, FILE_NAME_UNDOMINATED);
		FileInputStream sky_fis = new FileInputStream(sky_file);
		ModelContentRO sky_modelContent = ModelContent.loadFromXML(sky_fis);
		try {
			//try to get every child of the main model and create a skyline structure
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

		//load all dimensions from an intern file
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

		// create the main model content
		ModelContent all_modelContent = new ModelContent(INTERNAL_MODEL_DOMINATED);

		// create the main model content
		ModelContent sky_modelContent = new ModelContent(INTERNAL_MODEL_UNDOMINATED);

		// create the main model content
		ModelContent dimensions_modelContent = new ModelContent(INTERNAL_MODEL_DIMENSIONS);

		if (dominatedStruct.size() > 0) {

			for (int i = 0; i < dominatedStruct.size(); i++) {
				//for each dominated point create a sub model and add it to the main model
				ModelContentWO subContent = all_modelContent.addModelContent(SaveOption.DOMINATED.toString() + i);
				// save the dominated point to the sub model content
				dominatedStruct.get(i).saveTo(subContent);
			}
		}

		if (undominatedStruct.size() > 0) {
			for (int i = 0; i < undominatedStruct.size(); i++) {
				//for each dominated point create a sub model and add it to the main model
				ModelContentWO subContent = sky_modelContent.addModelContent(SaveOption.UNDOMINATED.toString() + i);
				// save the dominated point to the sub model content
				undominatedStruct.get(i).saveTo(subContent);
			}
		}

		//add dimensions to a model content
		if (columnList.size() > 0) {
			String[] dims = new String[columnList.size()];
			dims = columnList.toArray(dims);
			dimensions_modelContent.addStringArray(DIMENSIONS, dims);
		}

		// model content must be written to XML
		// internDir is the directory for this node
		File dominatedFile = new File(internDir, FILE_NAME_DOMINATED);
		FileOutputStream dominatedFos = new FileOutputStream(dominatedFile);
		all_modelContent.saveToXML(dominatedFos);

		// model content must be written to XML
		// internDir is the directory for this node
		File undominatedFile = new File(internDir, FILE_NAME_UNDOMINATED);
		FileOutputStream undominatedFos = new FileOutputStream(undominatedFile);
		sky_modelContent.saveToXML(undominatedFos);

		// model content must be written to XML
		// internDir is the directory for this node
		File dimensionsFile = new File(internDir, FILE_NAME_DIMENSIONS);
		FileOutputStream dimensionsFos = new FileOutputStream(dimensionsFile);
		dimensions_modelContent.saveToXML(dimensionsFos);

	}
}