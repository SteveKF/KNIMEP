package org.knime.preferences.prefCreator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class which is a JPanel and allows the user to add preferences to the JTree from a PriorityPanel.
 * This JPanel also displays the currently generated Preference-SQL Query.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class PreferencePanel extends JPanel{

	// GUI ELEMENTS
	//JComboBox which holds every dimension of the KNIME Input and a Custom Dimension
	private JComboBox<String> dimensionBox;
	//JComboBox which holds every preference from the Preference class
	private JComboBox<String> prefSelectionBox;
	//fields for user input which will be used for around and between preferences 
	private JFormattedTextField numericField1;
	private JFormattedTextField numericField2;
	/**
	* Field for the Custom Dimension. In this field the user can type his own Preference.
	* For example 'price/power'. He can still use the original preferences except the Layered one.
	*/
	private JFormattedTextField booleanField;
	//Button to open the Layered Dialog
	private JButton layeredButton;
	//Button to add Preference nodes to the JTree of the PriorityPanel
	private JButton addPreference;
	//Button to removes Preference nodes from the JTree of the PriorityPanel
	private JButton removePreference;
	//Button to open a Dialog which shows the current generted Preference-SQl Query
	private JButton queryButton;

	// object to store data for every dimension
	// if another will be chosen in the JComboBox for the dimensions it will load the input of this dimension and
	// change the values of all other fields accordingly
	/**
	 * Object to store data for every dimension.
	 * If another will be chosen in the JComboBox for the dimensions it will load the input of this dimension and
	 * change the values of all other fields accordingly.
	 * 
	 */
	private PreferenceDataSaver preferenceSaver;

	//map which stores for every dimension if it has only numeric values or not
	private Map<String, Boolean> isDimensionNumeric;
	/**
	 * Values for every dimension.
	 */
	private Map<String, List<String>> values;

	/**
	 * Constructor which adds all GUI components to this JPanel and set the preferences for the current dimension and
	 * disables all GUI components which are not used for the current preference. 
	 * @param dimensions - the dimension
	 * @param values - a map which stores all values for every dimension 
	 * @param isDimensionNumeric - map which stores if a dimension only contains numeric values or not
	 */
	protected PreferencePanel(String[] dimensions, Map<String, List<String>> values,
			Map<String, Boolean> isDimensionNumeric) {
		
		this.isDimensionNumeric = isDimensionNumeric;
		this.values = values;
		preferenceSaver = new PreferenceDataSaver(dimensions, isDimensionNumeric);
		
		//set layout for this panel
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(10, 10, 10, 10);
		gc.weightx = 1;
		gc.gridwidth = GridBagConstraints.RELATIVE;
		gc.fill = GridBagConstraints.HORIZONTAL;
		
		//set layout for the preference editor panel
		JPanel preferenceEditor = new JPanel();
		preferenceEditor.setLayout(new GridBagLayout());
		
		// dimension label + dimension box
		JLabel dimensionLabel = new JLabel("Dimension:");
		dimensionBox = new JComboBox<>(dimensions);
		gc.gridx = 0;
		gc.gridy = 0;
		preferenceEditor.add(dimensionLabel,gc);
		gc.gridx = 1;
		gc.gridy = 0;
		preferenceEditor.add(dimensionBox,gc);

		// preference label + preference selection box
		JLabel prefSelectionLabel = new JLabel("Preference:");
		// get preferences from the enum class Preference
		prefSelectionBox = new JComboBox<>(Preference.getPreferences());
		gc.gridx = 0;
		gc.gridy = 1;
		preferenceEditor.add(prefSelectionLabel,gc);
		gc.gridx = 1;
		gc.gridy = 1;
		preferenceEditor.add(prefSelectionBox,gc);

		// input fields for numeric preferences
		numericField1 = new JFormattedTextField(new Double(0.0));
		numericField1.setColumns(7);
		numericField2 = new JFormattedTextField(new Double(0.0));
		numericField2.setColumns(7);
		gc.gridx = 0;
		gc.gridy = 2;
		preferenceEditor.add(numericField1,gc);
		gc.gridx = 1;
		gc.gridy = 2;
		preferenceEditor.add(numericField2,gc);

		// nonNumericBox + inputField
		booleanField = new JFormattedTextField(new String(""));
		booleanField.setColumns(14);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 2;
		preferenceEditor.add(booleanField,gc);

		//layered button to open layered dialog
		layeredButton = new JButton("Open Layered Dialog");
		gc.gridx = 0;
		gc.gridy = 4;
		gc.gridwidth = 2;
		preferenceEditor.add(layeredButton,gc);
		//reset gridwidth
		gc.gridwidth = 1;
		//add border to the preference editor panel
		preferenceEditor.setBorder(BorderFactory.createTitledBorder("Preference Editor"));

		
		// buttons to add and remove preferences
		JPanel prefButtonPanel = new JPanel();
		prefButtonPanel.setLayout(new GridBagLayout());
		addPreference = new JButton("Add");
		removePreference = new JButton("Remove");
		queryButton = new JButton("Show Preference-SQL Query");
		gc.gridx = 0;
		gc.gridy = 0;
		prefButtonPanel.add(addPreference,gc);
		gc.gridx = 1;
		gc.gridy = 0;
		prefButtonPanel.add(removePreference,gc);
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 2;
		prefButtonPanel.add(queryButton,gc);

		// add preference panel + buttonPanel to this class
		gc.gridx = 0;
		gc.gridy = 0;
		add(preferenceEditor,gc);
		gc.gridx = 0;
		gc.gridy = 1;
		add(prefButtonPanel,gc);		
		
		//sets the preferences for the prefSelectionBox accordingly to the curenntly selected dimension
		setPreferencesFor(getDimension());
		// initiate the disabled components
		disableUnusedComponents();
		
	}

	//disable components which will not be used
	/**
	 * If the JComboBox for the preferences changes and another preference will be chosen, all components
	 * which will not be used for this preference, will be disabled.
	 * If the Custom dimension in the JComboBox for the dimensions is selected, the booleanField is always enabled.
	 */
	public void disableUnusedComponents() {

		String preference = getPreference();

		if (preference == Preference.Lowest.toString()) {
			numericField1.setEnabled(false);
			numericField2.setEnabled(false);
			booleanField.setEnabled(false);
			layeredButton.setEnabled(false);
		} else if (preference == Preference.Highest.toString()) {
			numericField1.setEnabled(false);
			numericField2.setEnabled(false);
			booleanField.setEnabled(false);
			layeredButton.setEnabled(false);
		} else if (preference == Preference.Around.toString()) {
			numericField1.setEnabled(true);
			numericField2.setEnabled(false);
			booleanField.setEnabled(false);
			layeredButton.setEnabled(false);
		} else if (preference == Preference.Between.toString()) {
			numericField1.setEnabled(true);
			numericField2.setEnabled(true);
			booleanField.setEnabled(false);
			layeredButton.setEnabled(false);
		} else if (preference == Preference.Boolean.toString()) {
			numericField1.setEnabled(false);
			numericField2.setEnabled(false);
			booleanField.setEnabled(true);
			layeredButton.setEnabled(false);
		} else if (preference == Preference.Layered.toString()) {
			numericField1.setEnabled(false);
			numericField2.setEnabled(false);
			booleanField.setEnabled(false);
			layeredButton.setEnabled(true);
		}
		
		if(getDimension()==SQLPreferenceEditor.CUSTOM_DIMENSION)
			booleanField.setEnabled(true);
	}


	/**
	 * Adds a listener to every GUI component (Add/remove Preference Button, JComboBox for Dimensions and Preferences,
	 * queryButton and the button for Opening the LayeredDialog) which should trigger an event.
	 * @param listener - a PreferenceListener
	 */
	public void addActionListener(PreferenceListener listener) {
		addPreference.addActionListener(listener);
		removePreference.addActionListener(listener);
		dimensionBox.addActionListener(listener);
		prefSelectionBox.addActionListener(listener);
		layeredButton.addActionListener(listener);
		queryButton.addActionListener(listener);
	}

	/**
	 * Accesses every input of the fields of this class and saves them in the preferenceSaver object. 
	 * @param dimension - the dimension for which all the values should be saved
	 */
	public void saveValues(String dimension) {

		preferenceSaver.setNumericPreferences(dimension, getPreference(),
				getNumericValue1(), getNumericValue2(),
				getBooleanValue());

	}

	/**
	 * Loads values for the currently selected dimension in the Dimension JComboBox and sets all the fields accordinly to the loaded values.
	 */
	public void loadValues() {

		NumericPreference savedPref = preferenceSaver.getNumericPreferences(getDimension());

		setPreference(savedPref.getPreference());
		setNumericValue1(savedPref.getNumericInput()[0]);
		setNumericValue2(savedPref.getNumericInput()[1]);
		setBooleanValue(savedPref.getBooleanInput());

	}

	/**
	 * Resets all the preferences the user can select in the JComboBox for the preferences accordingly to the dimension.</br> </br>
	 * Numeric preferences - all preferences except the 'BOOLEAN' preference will be added </br>
	 * Non numeric preferences - only the 'LAYERED' preference will be added </br>
	 * Custom Dimension - all preferences except the 'LAYERED' preference will be added
	 * @param dimension - the dimension for which the preferences should change
	 */
	public void setPreferencesFor(String dimension) {

		if (isDimensionNumeric.get(dimension)) {
			prefSelectionBox.removeAllItems();
			for (String str : Preference.getPreferences()) {
				if(!Preference.isBoolean(str))
					prefSelectionBox.addItem(str);
			}
		}else if(dimension==SQLPreferenceEditor.CUSTOM_DIMENSION){
			prefSelectionBox.removeAllItems();
			for (String str : Preference.getPreferences()) {
				if(!Preference.isLayered(str))
					prefSelectionBox.addItem(str);
			}
		}else {
			prefSelectionBox.removeAllItems();
			prefSelectionBox.addItem(Preference.Layered.toString());
		}

	}
	
	/**
	 * Opens the LayeredDialog for the currently selected dimension if it wasn't opened before.
	 * Otherwise the old LayereDialog will be restored and will be setVisible.
	 */
	public void openLayeredDialog() {
		
		String dimension = getDimension();
		
		LayeredDialog layeredDialog = preferenceSaver.getLayeredDialog(dimension);
		
		if(layeredDialog == null){
		
			layeredDialog = new LayeredDialog(values.get(dimension));
			layeredDialog.pack();
			layeredDialog.setModal(true);
			layeredDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			preferenceSaver.setLayeredDialog(dimension,layeredDialog);
		
		}
		
		layeredDialog.setVisible(true);
		
	}

	/**
	 * 
	 * @return Returns the JComboBox for the dimensions.
	 */
	public JComboBox<String> getDimensionBox() {
		return dimensionBox;
	}

	/**
	 * 
	 * @return Returns the JComboBox for the preferences.
	 */
	public JComboBox<String> getPreferenceSelectionBox() {
		return prefSelectionBox;
	}

	/**
	 * 
	 * @return Returns the JFormattedTextField for the first input (AROUND or lower bound for BETWEEN).
	 */
	public JFormattedTextField getNumericField1() {
		return numericField1;
	}

	/**
	 * 
	 * @return Returns the JFormattedTextField for the second input (upper bound for BETWEEN).
	 */
	public JFormattedTextField getNumericField2() {
		return numericField2;
	}

	/**
	 * 
	 * @return Returns the JFormattedTextField for the input of the Custom Dimension. 
	 * In this field the user can create his own dimension. (e.g. 'price/power' where price and power are single dimensions).
	 */
	public JFormattedTextField getBooleanField() {
		return booleanField;
	}
	
	/**
	 * 
	 * @return Returns the JButton which opens the LayeredDialog.
	 */
	public JButton getOpenLayeredDialogButton(){
		return layeredButton;
	}
	
	/**
	 * 
	 * @return Returns the JButton which adds a preference to the JTree from the PriorityPanel.
	 */
	public JButton getAddPreferenceButton() {
		return addPreference;
	}

	/**
	 * 
	 * @return Returns the JButton which removes a preference from the JTree from the PriorityPanel.
	 */
	public JButton getRemovePreferenceButton() {
		return removePreference;
	}
	
	/**
	 * 
	 * @return Returns the JButton whichs opens a dialog. This dialog displays accordingly to the preferences in the JTree
	 * the currently generated Preference-SQL Query.
	 */
	public JButton getQueryButton(){
		return queryButton;
	}

	/**
	 * 
	 * @return Returns the PreferenceDataSaver which saved all the input of all input fields for every dimension.
	 */
	public PreferenceDataSaver getPreferenceDataSaver() {
		return preferenceSaver;
	}

	/**
	 * 
	 * @return Returns the currently selected dimension.
	 */
	public String getDimension() {
		return (String) dimensionBox.getSelectedItem();
	}
	
	/**
	 * 
	 * @return Returns the currently selected preference.
	 */
	public String getPreference() {
		return (String) prefSelectionBox.getSelectedItem();
	}

	/**
	 * 
	 * @return Returns the current input of the first input field for the currently selected dimension.
	 */
	public double getNumericValue1() {
		return (double) numericField1.getValue();
	}

	/**
	 * 
	 * @return Returns the current input of the second input field for the currently selected dimension.
	 */
	public double getNumericValue2() {
		return (double) numericField2.getValue();
	}

	/**
	 * 
	 * @return Returns the input of the field which stores the values of a custom created dimension.
	 */
	public String getBooleanValue() {
		return (String) booleanField.getValue();
	}
	
	public void setDimension(String dimension) {
		dimensionBox.setSelectedItem(dimension);
	}


	/**
	 * 
	 * @param preference - a preference 
	 */
	public void setPreference(String preference) {

		prefSelectionBox.setSelectedItem(preference);

	}

	/**
	 * 
	 * @param value - input for inputfield1
	 */
	public void setNumericValue1(double value) {

		numericField1.setValue(value);

	}

	/**
	 * 
	 * @param value - input for inputfield2
	 */
	public void setNumericValue2(double value) {

		numericField2.setValue(value);

	}

	/**
	 * 
	 * @param booleanInput - custom dimension 
	 */
	public void setBooleanValue(String booleanInput) {

		booleanField.setValue(booleanInput);

	}


}
