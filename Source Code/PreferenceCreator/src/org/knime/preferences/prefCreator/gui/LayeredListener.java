package org.knime.preferences.prefCreator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class which sets the tooltip text accordingly to the output of the method the events trigger.
 * @author stevekanonfreak
 *
 */
public class LayeredListener implements ActionListener {

	private LayeredDialog layeredDialog;

	/**
	 * Constructor which stores the LayeredDialog
	 * @param layeredDialog - a Layered Dialog object
	 */
	protected LayeredListener(LayeredDialog layeredDialog) {

		this.layeredDialog = layeredDialog;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == layeredDialog.getAddLayerButton()) {
			//add new layer
			if(layeredDialog.addLayerNode()){
			layeredDialog.getToolTipLabel().setText("");
			}else{
				layeredDialog.getToolTipLabel().setText("The positive or negative Layer before has no value nodes.");
			}

		} else if (e.getSource() == layeredDialog.getRemoveLayerButton()) {
			//remove layer
			if(layeredDialog.removeLayerNode()){
				layeredDialog.getToolTipLabel().setText("");
			}else{
				layeredDialog.getToolTipLabel().setText("No more Layers to delete.");
			}

		} else if (e.getSource() == layeredDialog.getClearLayerButton()) {
			//clear all layers
			layeredDialog.clear();
			layeredDialog.getToolTipLabel().setText("");

		} else if (e.getSource() == layeredDialog.getAddValueButton()) {
			//add value node
			int result = layeredDialog.addValueNode();
			
			if (result == LayeredDialog.VALUE_NODE_ADDED) {
				layeredDialog.getToolTipLabel().setText("");
			} else if(result == LayeredDialog.VALUE_SINGLE_ELEMENT){
				layeredDialog.getToolTipLabel().setText("Adding this node will empty a node which has a descendant.");
			} else if(result == LayeredDialog.VALUE_NO_LAYER_SELECTED){
				layeredDialog.getToolTipLabel().setText("No Layer selected.");
			}

		} else if (e.getSource() == layeredDialog.getRemoveValueButton()) {
			//remove value node
			int result = layeredDialog.removeValueNode();
			
			if (result == LayeredDialog.VALUE_NODE_REMOVED) {
				layeredDialog.getToolTipLabel().setText("");
			} else if(result == LayeredDialog.VALUE_LAYER0){
				layeredDialog.getToolTipLabel().setText("Selected value can't be removed from Layer 0.");
			} else{
				layeredDialog.getToolTipLabel().setText("Removing this node will empty a node which has a descendant.");
			}
		} else if(e.getSource() == layeredDialog.getSaveButton()){
			//if save button was clicked set dialog invisible
			layeredDialog.setVisible(false);
		}

	}
}
