package org.knime.repskyline;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;

import gui.RepresentativeSkylineThreshold;

/**
 * This is the model implementation of RepresentativeSkyline. An algorithm which
 * computes a k-representative skyline based on significance and diversity.
 *
 * @author Stefan Wohlfart
 */
public class RepresentativeSkylineNodeModel extends NodeModel {

	// ************ fields for the settings ***************

	// PORTS FOR INPUTS
	public static final int IN_PORT_SKYLINE = 0;

	private String[] dimensions;
	// size of representative skyline output
	private int k;
	// weight for diversity (1-this = significanceWeight)
	private double diversityWeight;
	// check if the single threshold should used a upper bound or lower bound
	private Map<String, Boolean> isUpperBound;
	// single thresholds for every dimension
	private Map<String, Double> singleValues;
	// range thresholds for every dimension
	private Map<String, double[]> rangeValues;
	// options which thresholds to use: single, range or none thresholds
	private Map<String, String> options;
	
	private DataTableSpec originalSpec;

	/**
	 * Constructor for the node model.
	 */
	protected RepresentativeSkylineNodeModel() {

		super(new PortType[] { BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		BufferedDataTable skyline = (BufferedDataTable) inData[IN_PORT_SKYLINE];
		DataTableSpec skylineSpec = skyline.getDataTableSpec();
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();

		// throw an exception if there are no flowvariables
		if (flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY) == null
				&& flowVars.get(ConfigKeys.CFG_KEY_PREFERENCE_QUERY) == null)
			throw new InvalidSettingsException("A Preference Creator node needs to be in the data flow.");

		// set default values if the node dialog never was opened
		setDefaultValues(skylineSpec);

		// compute the representative skyline
		RepresentativeSkyline repSky = new RepresentativeSkyline(skyline, dimensions, singleValues, rangeValues,
				options, isUpperBound, k, diversityWeight);

		// create spec for the representative skyline
		int numColumn = skyline.getDataTableSpec().getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = skyline.getDataTableSpec().getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer container = exec.createDataContainer(newSpec);

		// put all rows into the BufferedDataContainer which were outputed by
		// the
		// representative skyline algorithm
		for (DataRow row : repSky.getRepresentativeSkyline()) {
			container.addRowToTable(row);

		}

		// finally close the container and get the result table.
		container.close();
		BufferedDataTable result = container.getTable();

		return new PortObject[] { result, inData[IN_PORT_SKYLINE] };
	}

	/**
	 * Gets all dimensions which only allow numeric values and have preferences
	 * and returns their names.
	 * 
	 * @param spec
	 *            - a DataTableSpec
	 * @param flowVars
	 *            - flow variables which stores which columns has preferences
	 * @return Returns the name of the columns which only allow numeric values
	 */
	public static String[] getDimensions(DataTableSpec spec, Map<String, FlowVariable> flowVars) {

		String[] columnNames = spec.getColumnNames();
		List<Integer> tmpColIndexes = new LinkedList<Integer>();

		for (int i = 0; i < columnNames.length; i++) {
			if (flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
				if (spec.getColumnSpec(i).getType().isCompatible(DoubleValue.class))
					tmpColIndexes.add(i);
		}

		int[] colIndexes = tmpColIndexes.stream().mapToInt(i -> i).toArray();

		String[] dims = new String[colIndexes.length];
		for (int i = 0; i < colIndexes.length; i++) {
			dims[i] = columnNames[colIndexes[i]];
		}

		return dims;
	}

	/**
	 * Sets default values if the node dialog has never been opened </br>
	 * Otherwise nothing happens
	 * 
	 * @param spec - the data table spec of the input data table of this node
	 */
	private void setDefaultValues(DataTableSpec spec) {
		// set default values if the node dialog never was opened
		if (dimensions == null || singleValues == null || rangeValues == null || options == null
				|| isUpperBound == null) {
			
			dimensions = getDimensions(spec, getAvailableFlowVariables());
			singleValues = new TreeMap<>();
			rangeValues = new TreeMap<>();
			options = new TreeMap<>();
			isUpperBound = new TreeMap<>();
			for (String dimension : dimensions) {
				double[] values = new double[] { 0.0, 0.0 };
				singleValues.put(dimension, values[0]);
				rangeValues.put(dimension, values);
				options.put(dimension, RepresentativeSkylineThreshold.NONE);
				isUpperBound.put(dimension, false);

				k = 1;
				diversityWeight = 0.5;
			}
		}
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
		
		DataTableSpec spec = (DataTableSpec) inSpecs[IN_PORT_SKYLINE];
		
		if(originalSpec==null)
			originalSpec = spec;
		
		if(!originalSpec.equalStructure(spec)){
			dimensions = null;
			singleValues = null;
			rangeValues = null;
			options = null;
			isUpperBound = null;
			originalSpec = spec;
		}

		return new PortObjectSpec[] { spec, spec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		// save settings by adding them to the node settings object
		settings.addStringArray(RepresentativeSkylineNodeDialog.CFG_KEY_DIMENSIONS, dimensions);
		byte[] singleBytes = null;
		byte[] rangeBytes = null;
		byte[] optionBytes = null;
		byte[] upperBoundBytes = null;
		try {
			singleBytes = RepresentativeSkylineNodeDialog.convertToBytes(singleValues);
			rangeBytes = RepresentativeSkylineNodeDialog.convertToBytes(rangeValues);
			optionBytes = RepresentativeSkylineNodeDialog.convertToBytes(options);
			upperBoundBytes = RepresentativeSkylineNodeDialog.convertToBytes(isUpperBound);
		} catch (IOException e) {
			e.printStackTrace();
		}
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_SINGLEVALUE, singleBytes);
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_RANGEVALUE, rangeBytes);
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_OPTIONVALUE, optionBytes);
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_UPPER_BOUND, upperBoundBytes);

		settings.addInt(RepresentativeSkylineNodeDialog.CFG_KEY_SIZE, k);
		settings.addDouble(RepresentativeSkylineNodeDialog.CFG_KEY_WEIGHT, diversityWeight);

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		// try to load the saved variables
		dimensions = settings.getStringArray(RepresentativeSkylineNodeDialog.CFG_KEY_DIMENSIONS);
		try {
			singleValues = (Map<String, Double>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_SINGLEVALUE));
			rangeValues = (Map<String, double[]>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_RANGEVALUE));
			options = (Map<String, String>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_OPTIONVALUE));
			isUpperBound = (Map<String, Boolean>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_UPPER_BOUND));
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}

		k = settings.getInt(RepresentativeSkylineNodeDialog.CFG_KEY_SIZE);
		diversityWeight = settings.getDouble(RepresentativeSkylineNodeDialog.CFG_KEY_WEIGHT);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
