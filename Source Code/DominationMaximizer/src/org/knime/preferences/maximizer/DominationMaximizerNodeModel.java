package org.knime.preferences.maximizer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.DatabaseUtility;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of DominationMaximizer.
 * 
 *
 * @author Stefan Wohlfart
 */
public class DominationMaximizerNodeModel extends NodeModel {

	public static final int IN_PORT_DATABASE_CONNECTION = 0;

	private SettingsModelIntegerBounded outputSize = new SettingsModelIntegerBounded(
			DominationMaximizerNodeDialog.CFG_KEY_OUTPUT_SIZE, 3, 1, Integer.MAX_VALUE);

	/**
	 * Constructor for the node model.
	 */
	protected DominationMaximizerNodeModel() {

		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		// get the scoreQuery which was created from the preferencecreator
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		// check if score and preference query are available
		if (flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY) == null
				&& flowVars.get(ConfigKeys.CFG_KEY_PREFERENCE_QUERY) == null)
			throw new InvalidSettingsException("Input needs to be from the Preference Creator node.");

		String scoreQuery = flowVars.get(ConfigKeys.CFG_KEY_SCORE_QUERY).getStringValue();

		// create original table
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inData[IN_PORT_DATABASE_CONNECTION].getSpec();
		BufferedDataTable originalData = createTable(spec, exec);

		// create table with SQL statement
		BufferedDataTable scoreTable = createTable(spec, exec, scoreQuery);

		// creates priorities and map for colindexes
		DataTableSpec scoreSpec = scoreTable.getDataTableSpec();

		// create dominationmaximizer which computes skyline points which
		// dominate the most points
		DominationChecker domChecker = new DominationChecker(flowVars, scoreSpec);
		DominationMaximizer maximizer = new DominationMaximizer(outputSize.getIntValue(), domChecker, scoreTable);

		// output skyline with all dimensions
		int numColumn = originalData.getDataTableSpec().getNumColumns();
		DataColumnSpec[] newColumns = new DataColumnSpec[numColumn];

		for (int i = 0; i < numColumn; i++) {
			newColumns[i] = originalData.getDataTableSpec().getColumnSpec(i);
		}

		DataTableSpec newSpec = new DataTableSpec(newColumns);

		BufferedDataContainer repSkyContainer = exec.createDataContainer(newSpec);

		List<RowKey> repSkyKeys = maximizer.getRepSkylineKeys();

		for (DataRow row : originalData) {

			if (repSkyKeys.contains(row.getKey())) {
				repSkyContainer.addRowToTable(row);
			}

		}

		// finally close the container and get the result table.
		repSkyContainer.close();
		BufferedDataTable repSkyline = repSkyContainer.getTable();

		// repskyline output
		BufferedDataContainer skylineContainer = exec.createDataContainer(newSpec);

		List<RowKey> skyKeys = maximizer.getSkylineKeys();

		for (DataRow row : originalData) {

			if (skyKeys.contains(row.getKey())) {
				skylineContainer.addRowToTable(row);
			}

		}

		// finally close the container and get the result table.
		skylineContainer.close();
		BufferedDataTable skyline = skylineContainer.getTable();

		return new PortObject[] { repSkyline, skyline };
	}

	private BufferedDataTable createTable(DatabasePortObjectSpec spec, ExecutionContext exec)
			throws CanceledExecutionException, SQLException, InvalidSettingsException {
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		DBReader reader = databaseUtil.getReader(dbSettings);
		BufferedDataTable table = reader.createTable(exec, credProvider);

		return table;
	}

	private BufferedDataTable createTable(DatabasePortObjectSpec spec, ExecutionContext exec, String query)
			throws CanceledExecutionException, SQLException, InvalidSettingsException {
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		dbSettings.setQuery(query);
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

		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[IN_PORT_DATABASE_CONNECTION];

		return new PortObjectSpec[] { spec.getDataTableSpec(), spec.getDataTableSpec() };
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
