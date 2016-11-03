package org.knime.repskyline;

import java.io.File;
import java.io.IOException;
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
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;

/**
 * This is the model implementation of RepresentativeSkyline. An algorithm which
 * computes a k-representative skyline based on significance and diversity. * n
 *
 * @author Stefan Wohlfart
 */
public class RepresentativeSkylineNodeModel extends NodeModel {

	// ************ fields for the settings ***************

	//PORTS FOR INPUTS
	public static final int IN_PORT_SKYLINE = 0;
	public static final int IN_PORT_ALL_DATA = 1;

	private String[] dimensions;
	//size of representative skyline output
	private int k;
	//weight for diversity (1-this = significanceWeight)
	private double diversityWeight;
	//check if the single threshold should used a upper bound or lower bound
	private boolean useUpperBound;
	//single thresholds for every dimension
	private Map<String, Double> singleValues;
	//range thresholds for every dimension
	private Map<String, double[]> rangeValues;
	//options which thresholds to use: single, range or none thresholds
	private Map<String, String> options;

	/**
	 * Constructor for the node model.
	 */
	protected RepresentativeSkylineNodeModel() {

		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		
		//loads variables in 
		if (dimensions == null || singleValues == null 
				|| rangeValues == null || options == null) {

			throw new IllegalArgumentException("Failed to load saved variables");

		}
		
		String[] columnNames = ((DataTableSpec) inData[IN_PORT_SKYLINE].getSpec()).getColumnNames();
		int[] colIndexes = new int[dimensions.length];
		
		int j=0; 
		List<String> dims = new LinkedList<>(Arrays.asList(dimensions));
		for(int i=0; i < columnNames.length; i++){
			
			if(dims.contains(columnNames[i])){
				colIndexes[j++] = i;
			}
		}

		BufferedDataTable skyline = (BufferedDataTable) inData[IN_PORT_SKYLINE];


		RepresentativeSkyline repSky = new RepresentativeSkyline(skyline,
				dimensions,singleValues,rangeValues,options,k,diversityWeight, useUpperBound);

		// create spec for the representative skyline
		int numColumn = skyline.getDataTableSpec().getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = skyline.getDataTableSpec().getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer container = exec.createDataContainer(newSpec);

		//put all rows into the BufferedDataContainer which were ouputed by the representative skyline algorithm
		for (DataRow row : repSky.getRepresentativeSkyline()) {
			container.addRowToTable(row);

		}

		// finally close the container and get the result table.
		container.close();
		BufferedDataTable result = container.getTable();

		return new PortObject[] { result, inData[IN_PORT_SKYLINE] };
	}

	/**
	 * Gets all columns which only allow numeric values and have preferences and returns their indexes.
	 * @param spec - a DataTableSpec
	 * @param flowVars - flow variables which stores which columns has preferences
	 * @return Returns indexes from columns which only allow numeric values 
	 */
	public static int[] getColumnIndexes(DataTableSpec spec, Map<String, FlowVariable> flowVars) {

		String[] columnNames = spec.getColumnNames();
		List<Integer> tmpColIndexes = new LinkedList<Integer>();
		for (int i = 0; i < columnNames.length; i++) {

			if (flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE)) {
				 
				 if (spec.getColumnSpec(i).getType().isCompatible(DoubleValue.class)) {
		                
						tmpColIndexes.add(i);
					 
		            }		            
			}
		}
		
		int[] colIndexes = tmpColIndexes.stream().mapToInt(i -> i).toArray();

		return colIndexes;
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

		//counts the number of columns which are used as keys for the Flow variables
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		String[] columnNames = ((DataTableSpec) inSpecs[IN_PORT_SKYLINE]).getColumnNames();
		int counter = 0;
		for(int i=0; i < columnNames.length; i++){

			if(flowVars.containsKey(columnNames[i]))
				counter++;
		}
		
		//if it doesn't match with the number of columns it throws an error
		if(columnNames.length != counter)
			throw new InvalidSettingsException("No preferences found.");
		
		
		return new PortObjectSpec[] { inSpecs[IN_PORT_SKYLINE], inSpecs[IN_PORT_ALL_DATA] };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		//save settings by adding them to the node settings object
		settings.addStringArray(RepresentativeSkylineNodeDialog.CFG_KEY_DIMENSIONS, dimensions);
		byte[] singleBytes = null;
		byte[] rangeBytes = null;
		byte[] optionBytes = null;
		try {
			singleBytes = RepresentativeSkylineNodeDialog.convertToBytes(singleValues);
			rangeBytes = RepresentativeSkylineNodeDialog.convertToBytes(rangeValues);
			optionBytes = RepresentativeSkylineNodeDialog.convertToBytes(options);
		} catch (IOException e) {
			e.printStackTrace();
		}
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_SINGLEVALUE, singleBytes);
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_RANGEVALUE, rangeBytes);
		settings.addByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_OPTIONVALUE, optionBytes);

		settings.addInt(RepresentativeSkylineNodeDialog.CFG_KEY_SIZE, k);
		settings.addDouble(RepresentativeSkylineNodeDialog.CFG_KEY_WEIGHT, diversityWeight);
		settings.addBoolean(RepresentativeSkylineNodeDialog.CFG_KEY_UPPER_BOUND, useUpperBound);

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		
		//try to load the saved variables
		dimensions = settings.getStringArray(RepresentativeSkylineNodeDialog.CFG_KEY_DIMENSIONS);
		try {
			singleValues = (Map<String, Double>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_SINGLEVALUE));
			rangeValues = (Map<String, double[]>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_RANGEVALUE));
			options = (Map<String, String>) RepresentativeSkylineNodeDialog
					.convertFromBytes(settings.getByteArray(RepresentativeSkylineNodeDialog.CFG_KEY_OPTIONVALUE));
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
		
		k = settings.getInt(RepresentativeSkylineNodeDialog.CFG_KEY_SIZE);
		diversityWeight = settings.getDouble(RepresentativeSkylineNodeDialog.CFG_KEY_WEIGHT);
		useUpperBound = settings.getBoolean(RepresentativeSkylineNodeDialog.CFG_KEY_UPPER_BOUND);

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
