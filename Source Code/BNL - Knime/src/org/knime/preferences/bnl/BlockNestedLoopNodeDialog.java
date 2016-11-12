package org.knime.preferences.bnl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;

/**
 * The NodeDialog of the "Block Nested Loop" Node has a dialog component for the
 * window size and stores this value in a SettingsModel
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */

public class BlockNestedLoopNodeDialog extends DataAwareDefaultNodeSettingsPane {

	private SettingsModelStringArray dimensionsModel = new SettingsModelStringArray(
			BlockNestedLoopNodeModel.CFGKEY_DIMENSIONS, new String[0]);
	private DialogComponentStringListSelection listSelection;
	private boolean isCreated = false;

	protected BlockNestedLoopNodeDialog() {
		super();

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {

		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		DataTableSpec spec = ((DatabasePortObject) input[BlockNestedLoopNodeModel.PORT_DATABASE_CONNECTION]).getSpec().getDataTableSpec();

		try {
			dimensionsModel.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e1) {
			e1.printStackTrace();
		}

		List<String> columnList = new ArrayList<>();
		String[] columnNames = spec.getColumnNames();
		for (int i = 0; i < columnNames.length; i++) {
			// only columns which have preferences and are numeric will be
			// in
			// the view
			if (flowVars.get(columnNames[i]).getStringValue().equals(ConfigKeys.CFG_KEY_EXISTS_PREFERENCE))
				if (spec.getColumnSpec(columnNames[i]).getType().isCompatible(DoubleValue.class)) {
					columnList.add(columnNames[i]);
				}
		}

		String[] dimensions = dimensionsModel.getStringArrayValue();
		if (!isEqualDimensions(dimensions, columnList) && listSelection != null) {
			listSelection.replaceListItems(columnList);
		}

		if (!isCreated) {
			
			createNewGroup("Window size");
	    	 //adds a component where the user can input his window size     
	        addDialogComponent(new DialogComponentNumber(new SettingsModelIntegerBounded(BlockNestedLoopNodeModel.CFGKEY_WINDOW_SIZE,
	        		3, 1, Integer.MAX_VALUE), "Window size: ", 1));
			closeCurrentGroup();

			createNewGroup("Dimension Chooser");
			listSelection = new DialogComponentStringListSelection(dimensionsModel, "Select two or three dimensions:",
					columnList, true, columnList.size());
			addDialogComponent(listSelection);
			closeCurrentGroup();
			
			isCreated = true;

			super.loadAdditionalSettingsFrom(settings, input);
		}
	}
	
	/**
	 * Checks if the list has the same size and the same values as the array
	 * @param dimensions - string array with dimensions as values
	 * @param columnList - list with dimensions as values
	 * @return true - if the list has the same size and the same values as the array </br>
	 * false - otherwise
	 */
	private boolean isEqualDimensions(String[] dimensions, List<String> columnList){
		
		boolean isEqual = false;
		
		if(dimensions.length==columnList.size()){
			int counter = 0;
			for(int i=0; i < dimensions.length; i++){
				if(columnList.contains(dimensions[i])){
					counter++;
				}
			}
			if(counter==dimensions.length)
				isEqual = true;
		}
		
		return isEqual;
		
	}
	
	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		dimensionsModel.saveSettingsTo(settings);
		super.saveAdditionalSettingsTo(settings);
	}

}
