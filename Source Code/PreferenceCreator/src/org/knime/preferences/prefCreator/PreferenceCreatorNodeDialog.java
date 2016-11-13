package org.knime.preferences.prefCreator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.DefaultTreeModel;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.preferences.prefCreator.gui.SQLPreferenceEditor;


/**
 * Adds the SQLPreferenceEditor to a tab of the NodeDialog. A previous state of
 * the editor will be loaded if it was saved once time. (Saving = User applies
 * settings of the NodeDialog)</br>
 * Creates a map which stores which dimension only allows numeric values. </br>
 * Creates a map with all values for every dimension and the dimension as
 * key.</br>
 * Saves treeModel, preferenceQuery, scoreQuery and dimensions which have
 * preferences.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class PreferenceCreatorNodeDialog extends DataAwareDefaultNodeSettingsPane {

	private SQLPreferenceEditor sqlPrefEditor;

	public static final String CFG_KEY_DIMENSIONS = "dimensions";
	public static final String CFG_KEY_TREEMODEL = "treeModel";
	public static final String CFG_KEY_PREFERENCE_KEYS = "preferenceKeys";

	public static final String CFG_KEY_ROW_KEYS = "rowKeys";
	public static final String CFG_KEY_COLUMN_NAMES = "columnNames";
	

	// checks if the preference editor was already created so that if it gets
	// reopened,
	// it doesn't get created again
	private boolean isCreated = false;
	
	private RowKey[] rowKeys;
	private String[] columnNames;
	
	private final String tabName = "Preferences";
	

	protected PreferenceCreatorNodeDialog() {

		super();

	}
	
	

	@Override
	public void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {

		// load treeModel if it is was saved in the settings
		// load previous state if KNIME gets restarted
		DefaultTreeModel treeModel = null;
		
		try {
			rowKeys = settings.getRowKeyArray(CFG_KEY_ROW_KEYS);
			columnNames = settings.getStringArray(CFG_KEY_COLUMN_NAMES);
			
			treeModel = (DefaultTreeModel) convertFromBytes(settings.getByteArray(CFG_KEY_TREEMODEL));
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidSettingsException e1) {
			e1.printStackTrace();
		}
		
		BufferedDataTable inputData = (BufferedDataTable) input[PreferenceCreatorNodeModel.TABLE_PORT];
		String[] tmpColumnNames = inputData.getDataTableSpec().getColumnNames();
		List<RowKey> keys = new ArrayList<>();
		for(DataRow row: inputData){
			keys.add(row.getKey());
		}
		RowKey[] tmpRowKeys = new RowKey[keys.size()];
		tmpRowKeys = keys.toArray(tmpRowKeys);
		
		boolean isSameInput = false;
		if(rowKeys==null && columnNames == null)
			isSameInput = true;
		else if(rowKeys != null && columnNames != null && isEqualColumns(columnNames,tmpColumnNames) && isEqualKeys(rowKeys,tmpRowKeys))
			isSameInput = true;
		else{
			isSameInput = false;
			isCreated = false;
		}
		
		rowKeys = tmpRowKeys;
		columnNames = tmpColumnNames;
		
		if (!isCreated) {
			
			if(!isSameInput)
				removeTab(tabName);

			// get the database settings which stores the query
			DatabasePortObjectSpec spec = (DatabasePortObjectSpec) input[PreferenceCreatorNodeModel.DATABASE_CONNECTION_PORT]
					.getSpec();
			CredentialsProvider credProvider = getCredentialsProvider();
			DatabaseQueryConnectionSettings dbSettings = null;
			try {
				dbSettings = spec.getConnectionSettings(credProvider);
			} catch (InvalidSettingsException e) {
				e.printStackTrace();
			}
			// get the query to pass it to the PreferenceEditor
			String query = "";
			if (dbSettings != null) {
				query = dbSettings.getQuery();
			} else {
				throw new IllegalArgumentException("No database connection found.");
			}

			// check which dimension only allows numeric values and stores this
			// in a map
			HashMap<String, Boolean> isDimensionNumeric = new HashMap<>();
			DataTableSpec tableSpec = inputData.getDataTableSpec();
			String[] dimensions = new String[tableSpec.getNumColumns()];

			for (int i = 0; i < tableSpec.getNumColumns(); i++) {
				DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
				dimensions[i] = columnSpec.getName();
				if (!columnSpec.getType().isCompatible(DoubleValue.class))
					isDimensionNumeric.put(columnSpec.getName(), false);
				else
					isDimensionNumeric.put(columnSpec.getName(), true);
			}

			// initialize valueMap to store all values for every dimension
			Map<String, List<String>> valueMap = new HashMap<>();
			for (int i = 0; i < dimensions.length; i++) {
				List<String> tmpList = new LinkedList<>();
				valueMap.put(dimensions[i], tmpList);
			}

			// stores all values (data cell values) in a map with dimension of
			// the values as key
			for (DataRow row : inputData) {
				for (int i = 0; i < row.getNumCells(); i++) {
					List<String> tmpList = valueMap.get(dimensions[i]);
					if (!row.getCell(i).isMissing()) {
						String value = row.getCell(i).toString();
						if (!tmpList.contains(value))
							tmpList.add(value);
					}
				}
			}
			// initialize Preference Editor and add it to one of the tabs from
			// the NodeDialog
			sqlPrefEditor = new SQLPreferenceEditor(dimensions, valueMap, isDimensionNumeric, query);
			addTabAt(0, tabName, sqlPrefEditor);
			selectTab(tabName);
			// set isCreated to true so the editor won't be created all the time
			// the
			// node dialog gets opened
			isCreated = true;

			/*
			 * if a treemodel could be loaded, the treeModel of the
			 * PreferenceEditor will change to the loaded one. This allows for
			 * loading states when KNIME gets restarted.
			 */
			if (treeModel != null && isSameInput) {
				sqlPrefEditor.loadPreviousState(treeModel);
			}

		}

		super.loadSettingsFrom(settings, input);

	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {

		/*
		 * get preferenceQuery, scoreQuery, dimensions and priorities All these
		 * variables should be saved into the settings so they can be loaded in
		 * the NodeModel.
		 */
		String scoreQuery = sqlPrefEditor.getScoreQuery();
		String preferenceQuery = sqlPrefEditor.getPreferenceQuery();
		// gets only the dimensions which have preferences
		String[] dimensions = sqlPrefEditor.getPreferenceDimensions();

		settings.addString(ConfigKeys.CFG_KEY_SCORE_QUERY, scoreQuery);
		settings.addString(ConfigKeys.CFG_KEY_PREFERENCE_QUERY, preferenceQuery);
		settings.addStringArray(CFG_KEY_DIMENSIONS, dimensions);
		
		settings.addRowKeyArray(CFG_KEY_ROW_KEYS, rowKeys);
		settings.addStringArray(CFG_KEY_COLUMN_NAMES, columnNames);
		

		TreeMap<String, String> preferences = sqlPrefEditor.getPreferences();
		Set<String> keySet = preferences.keySet();
		String[] keyArray = new String[keySet.size()];
		keyArray = keySet.toArray(keyArray);
		settings.addStringArray(CFG_KEY_PREFERENCE_KEYS, keyArray);

		for (String key : keyArray)
			settings.addString(key, preferences.get(key));

		// Converts treeModel into bytes and saves this array to the settings.
		byte[] treeBytes = null;
		try {
			treeBytes = convertToBytes(sqlPrefEditor.getTreeModel());
		} catch (IOException e) {
			e.printStackTrace();
		}
		settings.addByteArray(CFG_KEY_TREEMODEL, treeBytes);

		super.saveSettingsTo(settings);
	}
	
	/**
	 * 
	 * @param columns1 - a string array
	 * @param columns2 - a string array
	 * @return true - if both arrays have the same length and the same values </br>
	 * false - otherwise
	 */
	private boolean isEqualColumns(String[] columns1, String[] columns2){
		
		boolean isEqual = false;
		
		if(columns1.length==columns2.length){
			isEqual = true;
			for(int i=0; i < columns1.length; i++){
				if(!columns1[i].equals(columns2[i])){
					isEqual = false;
					break;
				}
			}
		}
		
		return isEqual;
		
	}
	
	/**
	 * 
	 * @param rowKeys1 - a RowKey array
	 * @param rowKeys2 - a RowKey array
	 * @return true - if both arrays have the same length and the same RowKeys </br>
	 * false - otherwise
	 */
	private boolean isEqualKeys(RowKey[] rowKeys1, RowKey[] rowKeys2){
		
		boolean isEqual = false;
		
		if(rowKeys1.length==rowKeys2.length){
			isEqual = true;
			for(int i=0; i < rowKeys1.length; i++){
				if(!rowKeys1[i].equals(rowKeys2[i])){
					isEqual = false;
					break;
				}
			}
		}
		
		return isEqual;
		
	}
	

	/**
	 * Converts an Object to bytes.
	 * 
	 * @param object
	 *            - an object
	 * @return Returns an byte array which represents the object
	 * @throws IOException
	 */
	public static byte[] convertToBytes(Object object) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return bos.toByteArray();
		}
	}

	/**
	 * Converts a byte array to an object.
	 * 
	 * @param bytes
	 *            - array of bytes
	 * @return Returns an object which was converted to a byte array.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
			return in.readObject();
		}
	}

}