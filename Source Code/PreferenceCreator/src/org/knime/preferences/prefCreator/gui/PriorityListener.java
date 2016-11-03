package org.knime.preferences.prefCreator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles event which will trigger for various GUI components of the PriorityPanel class.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class PriorityListener implements ActionListener {

	// PRIORITY PANEL
	private PriorityPanel priorityPanel;

	// TOOLTIPPANEL PANEL
	private ToolTipPanel tooltipPanel;

	/**
	 * Constructor which initializes a PriorityPanel and ToolTipPanel member variable.
	 * @param priorityPanel - a PriorityPanel object
	 * @param tooltipPanel - a ToolTipPanel object
	 */
	public PriorityListener(PriorityPanel priorityPanel, ToolTipPanel tooltipPanel) {

		// initialize panels
		this.priorityPanel = priorityPanel;
		this.tooltipPanel = tooltipPanel;
	}

	/**
	 * This method handles events for the GUI elements of the PriorityPanel. </br> </br>
	 * Add Button - Calls the methods addPriorityNode and addParetoNode and adds the node which JRadiotButtion was selected. 
	 * Replaces the tooltip text accordingly if the node was added or not. </br>
	 * Remove Button - Calls the method removeNode. Replaces the tooltip text accordingly to if the node was removed. </br>
	 * Clear Button - Calls the method clear and replaces the tooltip text with a blank text. </br>
	 * Priority JRadioButton - sets the Priority JRadioButton to selected and unselects the Pareto JRadioButton 
	 * Pareto JRadioButton - sets the Pareto JRadioButton to selected and unselects the Priority JRadioButton 
	 * @param e - an event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == priorityPanel.getAddNodeButton()) {

			// add priority node
			int isPriorityAdded = priorityPanel.addPriorityNode();

			if (isPriorityAdded == PriorityPanel.PRIORITY_ADDED) 
				tooltipPanel.setText("");
			else if(isPriorityAdded == PriorityPanel.PRIORITY_NO_PARETO)
				tooltipPanel.setText("No Pareto node was selected.");
			

			// add pareto node
			int isParetoAdded = priorityPanel.addParetoNode();

			if (isParetoAdded == PriorityPanel.PARETO_ADDED)
				tooltipPanel.setText("");
			else if(isParetoAdded == PriorityPanel.PARETO_NO_PRIORITY)
				tooltipPanel.setText("No Priority node was selected.");

		} else if (e.getSource() == priorityPanel.getRemoveNodeButton()) {

			// remove priority or pareto node
			boolean isRemoved = priorityPanel.removeNode();

			if (isRemoved)
				tooltipPanel.setText("");
			else
				tooltipPanel.setText("No Pareto or Priority Node selected!");

			// remove every node in the tree
		} else if (e.getSource() == priorityPanel.getClearNodesButton()) {

			priorityPanel.clear();
			tooltipPanel.setText("");

			// depending on which radiobutton was selected
			// the other one gets deselected and the bool for
			// creating nodes get changed so that only the correct node gets
			// created
		} else if (e.getSource() == priorityPanel.getPriorityRadioButton()) {
			priorityPanel.getPriorityRadioButton().setSelected(true);
			priorityPanel.getParetoRadioButton().setSelected(false);
		} else if (e.getSource() == priorityPanel.getParetoRadioButton()) {
			priorityPanel.getPriorityRadioButton().setSelected(false);
			priorityPanel.getParetoRadioButton().setSelected(true);
		}
	}
}
