package org.knime.preferences.distance;

import java.io.File;
import java.io.IOException;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.distance.algorithm.DataPoint;
import org.knime.preferences.distance.algorithm.DataPointComparator;
import org.knime.preferences.distance.algorithm.DistanceRepSky;
import org.knime.preferences.prefCreator.ConfigKeys;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of DistanceBasedResolver.
 * 
 *
 * @author Stefan Wohlfart
 */
public class DistanceBasedResolverNodeModel extends NodeModel {

	// Index for Inport
	public static final int IN_PORT_SKYLINE = 0;
	// Output size k
	public static final String CFGKEY_OUTPUT_SIZE = "outputSize";

	private SettingsModelIntegerBounded outputSize = new SettingsModelIntegerBounded(CFGKEY_OUTPUT_SIZE, 1, 1,
			Integer.MAX_VALUE);


	/**
	 * Constructor for the node model.
	 */
	protected DistanceBasedResolverNodeModel() {

		super(1, 2);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		List<DataPoint> points = new LinkedList<>();
		BufferedDataTable skyline = inData[IN_PORT_SKYLINE];

		// output Size
		int k = outputSize.getIntValue();

		// create colIndexes (columns which are important
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		DataTableSpec skySpec = inData[IN_PORT_SKYLINE].getSpec();

		int[] colIndexes = getColumnIndexes(skySpec, flowVars);

		
		//transform buffereddatatable to datapoint list
		for (DataRow row : skyline) {
			points.add(DataPoint.createDataPoint(row, colIndexes));
		}

		points.sort(new DataPointComparator());
		
		//no more than the size of the original bufferedatatable can be outputed
		if (k > points.size())
			k = points.size();

		//run algorithm and return result
		DistanceRepSky distBasedResolver = new DistanceRepSky(points, k);
		List<DataPoint> repSkyline = distBasedResolver.getRepSkyline();

		// create output for the representative skyline
		int numColumn = inData[IN_PORT_SKYLINE].getDataTableSpec().getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = inData[IN_PORT_SKYLINE].getDataTableSpec().getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer container = exec.createDataContainer(newSpec);

		for (DataPoint p : repSkyline) {
			container.addRowToTable(p.getDataRow());
		}

		// finally close the container and get the result table.
		container.close();
		BufferedDataTable result = container.getTable();

		return new BufferedDataTable[] { result, inData[IN_PORT_SKYLINE] };
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
				 if (spec.getColumnSpec(i).getType().isCompatible(DoubleValue.class)) 
						tmpColIndexes.add(i);            
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		int columnSize = 0;
		int dims = 0;
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		String[] names = inSpecs[IN_PORT_SKYLINE].getColumnNames();

		for (int i = 0; i < names.length; i++) {
			if (flowVars.containsKey(names[i])){
				columnSize++;
				if(flowVars.get(names[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
					dims++;
					
			}
		}

		if (columnSize != names.length)
			throw new InvalidSettingsException(
					"No preferences were found. One of the parent nodes needs to be a PreferenceCreator Node.");
		
		if(dims <= 1)
			throw new InvalidSettingsException("Two dimensions need to have preferences.");
		
		if(dims != 2)
			setWarningMessage("More than two dimensions have preferences. Only the first two dimensions will be considered.");
			

		return new DataTableSpec[] { inSpecs[IN_PORT_SKYLINE], inSpecs[IN_PORT_SKYLINE] };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		outputSize.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		outputSize.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		outputSize.validateSettings(settings);
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
