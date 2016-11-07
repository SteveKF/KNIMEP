package org.knime.preferences.prefCreator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * This class handles event which will trigger for various GUI components of the PreferencePanel class.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class PreferenceListener implements ActionListener {

	// OWN VARIABLES
	private String prevDimension;

	// TOOLTIPPANEL VARIABLES
	private ToolTipPanel toolTipPanel;

	// PRIORITYPANEL
	private PriorityPanel priorityPanel;

	// PREFERENCEPANEL
	private PreferencePanel preferencePanel;
	
	//SQLGENERATOR
	private SQLGenerator sqlGenerator;

	/**
	 * Constructor which initializes all member variables with the input variables and sets the previous dimension to the current selected dimension. 
	 * @param priorityPanel - a PriorityPanel object
	 * @param preferencePanel - a PreferencePanel object
	 * @param toolTipPanel - a ToolTipPanel object
	 * @param sqlGenerator - a SQLGenerator object
	 */
	public PreferenceListener(PriorityPanel priorityPanel, PreferencePanel preferencePanel, ToolTipPanel toolTipPanel,
			SQLGenerator sqlGenerator) {

		this.priorityPanel = priorityPanel;
		this.preferencePanel = preferencePanel;
		this.toolTipPanel = toolTipPanel;
		this.sqlGenerator = sqlGenerator;

		// set previous dimensions to default dimension
		prevDimension = (String) preferencePanel.getDimensionBox().getSelectedItem();
	}

	/**
	 * This method handles events for the GUI elements of the ParetoPanel. </br> </br>
	 * Add Preference Button - calls the addPreferenceNode from the PriorityPanel and replaces the tooltip text accordingly to the return value of the method.</br>
	 * Remove Preference Button - calls the removePreferenceNode from the PriorityPanel and repalces the tooltip text accordingly to the return value of the method.</br>
	 * DimensionBox - Calls the saveValues with the previous dimension and loadValues method with the current selected dimension of the PreferencePanel and the setPreferencesFor method from the PriorityPanel. 
	 * Sets the previous dimension as the current dimension </br>
	 * Preference SelectionBox - Calls the disableUnusedComponents method from the PreferencePanel
	 * LayeredDialog Button - Calls the openLayeredDialog method of the PreferencePanel
	 * Query Button - Opens a JOption Dialog which displays the current generated Preference-SQL Query
	 * @param e - an event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == preferencePanel.getAddPreferenceButton()) {
			// Add preference node to tree

			int resultValue = priorityPanel.addPreferenceNode();
			
			if (resultValue == PriorityPanel.PREFERENCE_ADDED) {
				toolTipPanel.setText("");
			} else if(resultValue == PriorityPanel.PREFENCE_NOT_SELECTED){
				toolTipPanel.setText("Select a priority/pareto node for your preference node.");
			}else{
				toolTipPanel.setText("No Layer order was set up yet.");
			}

		} else if (e.getSource() == preferencePanel.getDimensionBox()) {

			preferencePanel.saveValues(prevDimension);

			preferencePanel.loadValues();

			prevDimension = (String) preferencePanel.getDimension();

			preferencePanel.setPreferencesFor(preferencePanel.getDimension());
			
			if(prevDimension==SQLPreferenceEditor.CUSTOM_DIMENSION)
				toolTipPanel.setText("This dimension allows you to create your own dimension with SQL Syntax. (e.g. price/power)");

			// disable/enable GUI components depending on selected Preference
		} else if (e.getSource() == preferencePanel.getPreferenceSelectionBox()) {

			preferencePanel.disableUnusedComponents();

			// just a test for printing the saved preferences
		} else if (e.getSource() == preferencePanel.getRemovePreferenceButton()) {

			if (priorityPanel.removePreferenceNode()) {
				toolTipPanel.setText(" ");
			} else {
				toolTipPanel.setText("Select a preference node you want to delete.");
			}
		} else if (e.getSource() == preferencePanel.getOpenLayeredDialogButton()
				&& preferencePanel.getPreferenceSelectionBox().getSelectedItem().toString().equals(Preference.Layered.toString())) {
		
			preferencePanel.openLayeredDialog();
			toolTipPanel.setText("Saved Layer preference.");
			
		}else if(e.getSource() == preferencePanel.getQueryButton()){
			//TODO: REMOVE
			sqlGenerator.print();
			JOptionPane optionPane = new JOptionPane();
			optionPane.setMessage(breakLongString(sqlGenerator.getPreferenceQuery(), 100));
			JDialog dialog = optionPane.createDialog("Preference Query");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		}
	}
	
	private String breakLongString( String input, int charLimit )
	{
	    String output = "", rest = input;
	    int i = 0;

	     // validate.
	    if ( rest.length() < charLimit ) {
	        output = rest;
	    }
	    else if (  !rest.equals("")  &&  (rest != null)  )  // safety precaution
	    {
	        do
	        {    // search the next index of interest.
	            i = rest.lastIndexOf(" ", charLimit) +1;
	            if ( i == -1 )
	                i = charLimit;
	            if ( i > rest.length() )
	                i = rest.length();

	             // break!
	            output += rest.substring(0,i) +"\n";
	            rest = rest.substring(i);
	        }
	        while (  (rest.length() > charLimit)  );
	        output += rest;
	    }

	    return output;
	}

}
