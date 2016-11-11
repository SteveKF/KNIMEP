package org.knime.preference.sql;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
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
 * This is the model implementation of PreferenceSQL. Output for Preference SQL
 *
 * @author Stefan Wohlfart
 */
public class PreferenceSQLNodeModel extends NodeModel {
	
	public static final int IN_PORT_CONNECTION = 0;

	/**
	 * Constructor for the node model.
	 */
	protected PreferenceSQLNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE }, new PortType[] { BufferedDataTable.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
		
		//throw exception if input doesn't come from a Preference Creator node
		Map<String,FlowVariable> flowVars = getAvailableFlowVariables();
		if(!flowVars.containsKey(ConfigKeys.CFG_KEY_SCORE_QUERY) && !flowVars.containsKey(ConfigKeys.CFG_KEY_PREFERENCE_QUERY))
			throw new InvalidSettingsException("Input needs to be from a Preference Creator node.");
		
	
		String preferenceQuery = flowVars.get(ConfigKeys.CFG_KEY_PREFERENCE_QUERY).getStringValue();

		//create skyline table with Preference SQL query
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inData[0].getSpec();
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		DBReader reader = databaseUtil.getReader(dbSettings);
		//throw exception if the JDBC driver isn't the PSQLDriver
		if(!dbSettings.getDriver().equals("psql.connector.client.PSQLDriver"))
			throw new IllegalArgumentException("PSQLDriver needed!");
		
		System.out.println(preferenceQuery);
		dbSettings.setQuery(preferenceQuery);
		BufferedDataTable preferenceTable = reader.createTable(exec, credProvider);
		

		return new PortObject[] { preferenceTable };
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

		return new PortObjectSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
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
