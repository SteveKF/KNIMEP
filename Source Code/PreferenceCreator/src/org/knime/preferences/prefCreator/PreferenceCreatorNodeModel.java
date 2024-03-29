package org.knime.preferences.prefCreator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.tree.DefaultTreeModel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
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
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.DatabaseUtility;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.workflow.CredentialsProvider;

/**
 * This is the model implementation of PreferenceCreator.
 * 
 *
 * @author Stefan Wohlfart
 */
public class PreferenceCreatorNodeModel extends NodeModel {

	// INPUT PORTS
	public final static int DATABASE_CONNECTION_PORT = 0;
	public final static int TABLE_PORT = 1;

	private String scoreQuery;
	private String preferenceQuery;
	private String[] dimensions;
	private String[] keyArray;
	private TreeMap<String, String> preferences;

	private DefaultTreeModel treeModel;
	
	private String[] columnNames;
	private RowKey[] rowKeys;

	protected PreferenceCreatorNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE, BufferedDataTable.TYPE }, new PortType[] {
				DatabasePortObject.TYPE, BufferedDataTable.TYPE_OPTIONAL });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		// get database connection settings
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inData[DATABASE_CONNECTION_PORT].getSpec();
		CredentialsProvider credProvider = getCredentialsProvider();
		DatabaseQueryConnectionSettings dbSettings = spec.getConnectionSettings(credProvider);

		// create original table with the database connection and the score query
		DatabaseUtility databaseUtil = DatabaseUtility.getUtility(dbSettings.getDatabaseIdentifier());
		DBReader reader = databaseUtil.getReader(dbSettings);
		BufferedDataTable originalTable = reader.createTable(exec, credProvider);
		
		if(originalTable.size() != ((BufferedDataTable) inData[TABLE_PORT]).size())
			throw new IllegalArgumentException(
					"The query of the database connection needs to return the same amount of rows as the data table.");

		if (scoreQuery == null || preferenceQuery == null || dimensions == null)
			throw new IllegalArgumentException(
					"Can't create queries with current settings. Open the dialog to create preferences.");

		// push/create flow variables
		pushFlowVariableString(ConfigKeys.CFG_KEY_SCORE_QUERY, scoreQuery);
		pushFlowVariableString(ConfigKeys.CFG_KEY_PREFERENCE_QUERY, preferenceQuery);

		// create score table with the database connection and the score query
		dbSettings.setQuery(scoreQuery);
		reader = databaseUtil.getReader(dbSettings);
		BufferedDataTable scoreTable = reader.createTable(exec, credProvider);

		// push the columns with the respective priorities
		// priorities forces comparision order

		for (String key : keyArray)
			pushFlowVariableString(key, preferences.get(key));

		// push for every dimension if it has a preference (true) or not (false)
		DataTableSpec originalSpec = spec.getDataTableSpec();
		String[] columnNames = originalSpec.getColumnNames();
		List<String> dims = Arrays.asList(dimensions);
		for (int i = 0; i < columnNames.length; i++) {
			if (dims.contains(columnNames[i]))
				pushFlowVariableString(columnNames[i], ConfigKeys.CFG_KEY_EXISTS_PREFERENCE);
			else
				pushFlowVariableString(columnNames[i], ConfigKeys.CFG_KEY_NOT_EXISTS_PREFERENCE);
		}

		// return original database connection and the score table as optional
		// output
		return new PortObject[] { inData[DATABASE_CONNECTION_PORT], scoreTable };
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

		DataTableSpec dbSpec = ((DatabasePortObjectSpec) inSpecs[DATABASE_CONNECTION_PORT]).getDataTableSpec();
		DataTableSpec spec = (DataTableSpec) inSpecs[TABLE_PORT];

		if (!dbSpec.equalStructure(spec))
			throw new InvalidSettingsException(
					"The database connection needs to return the same data table as the data table at inport 1.");

		return new PortObjectSpec[] { inSpecs[DATABASE_CONNECTION_PORT], null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		// saves important variables so they can be loaded when KNIME is
		// restarted
		settings.addString(ConfigKeys.CFG_KEY_SCORE_QUERY, scoreQuery);
		settings.addString(ConfigKeys.CFG_KEY_PREFERENCE_QUERY, preferenceQuery);
		settings.addStringArray(PreferenceCreatorNodeDialog.CFG_KEY_DIMENSIONS, dimensions);

		if (preferences != null) {
			Set<String> keySet = preferences.keySet();
			String[] keyArray = new String[keySet.size()];
			keyArray = keySet.toArray(keyArray);
			settings.addStringArray(PreferenceCreatorNodeDialog.CFG_KEY_PREFERENCE_KEYS, keyArray);

			for (String key : keyArray)
				settings.addString(key, preferences.get(key));
		}

		byte[] treeBytes = null;
		try {
			treeBytes = PreferenceCreatorNodeDialog.convertToBytes(treeModel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		settings.addByteArray(PreferenceCreatorNodeDialog.CFG_KEY_TREEMODEL, treeBytes);
		
		settings.addStringArray(PreferenceCreatorNodeDialog.CFG_KEY_COLUMN_NAMES, columnNames);
		settings.addRowKeyArray(PreferenceCreatorNodeDialog.CFG_KEY_ROW_KEYS, rowKeys);


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		// loads important values
		scoreQuery = settings.getString(ConfigKeys.CFG_KEY_SCORE_QUERY);
		preferenceQuery = settings.getString(ConfigKeys.CFG_KEY_PREFERENCE_QUERY);
		dimensions = settings.getStringArray(PreferenceCreatorNodeDialog.CFG_KEY_DIMENSIONS);
		keyArray = settings.getStringArray(PreferenceCreatorNodeDialog.CFG_KEY_PREFERENCE_KEYS);
		
		columnNames = settings.getStringArray(PreferenceCreatorNodeDialog.CFG_KEY_COLUMN_NAMES);
		rowKeys = settings.getRowKeyArray(PreferenceCreatorNodeDialog.CFG_KEY_ROW_KEYS);

		preferences = new TreeMap<>();
		for (String key : keyArray)
			preferences.put(key, settings.getString(key));

		try {
			treeModel = (DefaultTreeModel) PreferenceCreatorNodeDialog
					.convertFromBytes(settings.getByteArray(PreferenceCreatorNodeDialog.CFG_KEY_TREEMODEL));
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidSettingsException e1) {
			e1.printStackTrace();
		}
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
