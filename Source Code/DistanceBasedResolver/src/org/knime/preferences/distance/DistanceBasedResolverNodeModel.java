package org.knime.preferences.distance;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
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
 * This is the model implementation of DistanceBasedResolver which computes the k-representative skyline based on distance.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class DistanceBasedResolverNodeModel extends NodeModel {

	// Index for Inport Skyline
	public static final int IN_PORT_SKYLINE = 0;
	// Index for Inport Score Skyline
	public static final int IN_PORT_SCORE_SKYLINE = 1;
	
	public static final String standardSkyline = "STANDARD";
	public static final String scoreSkyline= "SCORE";
	// Output size k
	public static final String CFGKEY_OUTPUT_SIZE = "outputSize";

	private SettingsModelIntegerBounded outputSize = new SettingsModelIntegerBounded(CFGKEY_OUTPUT_SIZE, 1, 1,
			Integer.MAX_VALUE);
	


	/**
	 * Constructor for the node model.
	 */
	protected DistanceBasedResolverNodeModel() {


		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws Exception {
		
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		if(!flowVars.containsKey(ConfigKeys.CFG_KEY_SCORE_QUERY) && !flowVars.containsKey(ConfigKeys.CFG_KEY_PREFERENCE_QUERY))
			throw new InvalidSettingsException("The data flow needs to contain a Preference Creator node");

		List<DataPoint> points = new LinkedList<>();
		BufferedDataTable skyline = (BufferedDataTable) inData[IN_PORT_SKYLINE];
		DataTableSpec skySpec = skyline.getDataTableSpec();
		
		BufferedDataTable scoreSkyline = (BufferedDataTable) inData[IN_PORT_SCORE_SKYLINE];
		
		// output Size
		int k = outputSize.getIntValue();
		
		//transform buffereddatatable to datapoint list
		
		for (DataRow row : scoreSkyline) {
			points.add(DataPoint.createDataPoint(row));
		}
		//sort datapoint list by first dimension
		points.sort(new DataPointComparator());
		
		//no more than the size of the original bufferedatatable can be outputed
		if (k > points.size())
			k = points.size();

		//run algorithm and return result
		DistanceRepSky distBasedResolver = new DistanceRepSky(points, k);
		List<RowKey> repSkyline = distBasedResolver.getRepSkyline();

		// create output for the representative skyline
		int numColumn = skySpec.getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = skySpec.getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer container = exec.createDataContainer(newSpec);

		for (DataRow row : skyline) {
			if(repSkyline.contains(row.getKey()))
				container.addRowToTable(row);
		}

		// finally close the container and get the result table.
		container.close();
		BufferedDataTable result = container.getTable();

		return new PortObject[] { result, inData[IN_PORT_SKYLINE] };
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

		DataTableSpec scoreSpec = ((DataTableSpec) inSpecs[IN_PORT_SCORE_SKYLINE]);

		if(scoreSpec.getNumColumns() <= 1)
			throw new InvalidSettingsException("At least two dimensions need to have preferences.");
		
		if(scoreSpec.getNumColumns() != 2)
			setWarningMessage("More than two dimensions have preferences. Only the first two dimensions will be considered.");
			

		return new PortObjectSpec[] { inSpecs[IN_PORT_SKYLINE], inSpecs[IN_PORT_SKYLINE] };
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
