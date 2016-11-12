package org.knime.preferences.visualizer;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.preferences.prefCreator.ConfigKeys;

/**
 * <code>NodeDialog</code> for the "(Representative) Skyline Visualizer" Node. A
 * node to visualize skylines with their dominated points or representative
 * skylines with the corresponding skyline.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class SkylineVisualizerNodeDialog extends DataAwareDefaultNodeSettingsPane {

	public static final String[] options = new String[] { "Skyline Graph", "Representative Skyline Graph", "Custom" };

	private SettingsModelString optionSelector = new SettingsModelString(
			SkylineVisualizerNodeModel.CFGKEY_GRAPH_OPTIONS, options[1]);
	private SettingsModelString chartName;
	private SettingsModelString subTitle;
	private SettingsModelString dominatedPointsName;
	private SettingsModelString undominatedPointsName;
	private SettingsModelStringArray dimensionsModel = new SettingsModelStringArray(SkylineVisualizerNodeModel.CFGKEY_DIMENSIONS, new String[0]); 

	private ChangeListener listener;
	private DialogComponentStringListSelection listSelection;

	private boolean isCreated = false;

	protected SkylineVisualizerNodeDialog() {
		super();
	}
	
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		DataTableSpec spec = ((BufferedDataTable) input[SkylineVisualizerNodeModel.UNDOMINATED_PORT]).getDataTableSpec();
		
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
		if(!isEqualDimensions(dimensions,columnList) && listSelection != null){
			listSelection.replaceListItems(columnList);
		}


		if (!isCreated) {
			
			createNewGroup("Dimension Chooser");
			listSelection = new DialogComponentStringListSelection(dimensionsModel,
					"Select two or three dimensions :", columnList, true, columnList.size());
			addDialogComponent(listSelection);
			closeCurrentGroup();

			// options which changes the chartname, subtitle and the legend
			// names accordingly
			createNewGroup("Graph Labels");
			addDialogComponent(
					new DialogComponentButtonGroup(optionSelector, false, "Choose which Graph you want:", options));
			closeCurrentGroup();

			/*
			 * custom gui components which allow the user to input customized
			 * chartname, subtitles and legend name for dominated and
			 * undominated points
			 */
			createNewGroup("Custom Graph Options");
			chartName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_CHART_NAME, "");
			DialogComponentString dialog1 = new DialogComponentString(chartName, "Chartname:");
			dialog1.getComponentPanel().setLayout(new FlowLayout(FlowLayout.RIGHT));
			addDialogComponent(dialog1);
			chartName.setEnabled(false);

			subTitle = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_SUBTITLE, "");
			DialogComponentString dialog2 = new DialogComponentString(subTitle, "Subtitle:");
			dialog2.getComponentPanel().setLayout(new FlowLayout(FlowLayout.RIGHT));
			addDialogComponent(dialog2);

			dominatedPointsName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_DOMINATED_POINTS_NAME, "");
			DialogComponentString dialog3 = new DialogComponentString(dominatedPointsName,
					"Name for dominated points: ");
			dialog3.getComponentPanel().setLayout(new FlowLayout(FlowLayout.RIGHT));
			addDialogComponent(dialog3);

			undominatedPointsName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_UNDOMINATED_POINTS_NAME,
					"");
			DialogComponentString dialog4 = new DialogComponentString(undominatedPointsName,
					"Name for undominated points: ");
			dialog4.getComponentPanel().setLayout(new FlowLayout(FlowLayout.RIGHT));
			addDialogComponent(dialog4);

			closeCurrentGroup();

			listener = new ChangeListener() {

				// listener so if the custom option isn't selected all custom
				// GUI components are disabled
				@Override
				public void stateChanged(ChangeEvent e) {
					if (optionSelector.getStringValue().equals(options[options.length - 1])) {
						chartName.setEnabled(true);
						subTitle.setEnabled(true);
						dominatedPointsName.setEnabled(true);
						undominatedPointsName.setEnabled(true);
					} else {
						chartName.setEnabled(false);
						subTitle.setEnabled(false);
						dominatedPointsName.setEnabled(false);
						undominatedPointsName.setEnabled(false);
					}
				}
			};

			optionSelector.addChangeListener(listener);

			/*
			 * trigger an event manually so the GUI components get enabled or
			 * disabled according to the default value of options
			 */
			listener.stateChanged(new ChangeEvent(optionSelector));
			
			isCreated = true;
		}
		
		super.loadAdditionalSettingsFrom(settings, input);
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

	@Override
	public void onOpen() {
		// trigger event manually
		if (listener != null)
			listener.stateChanged(new ChangeEvent(optionSelector));
		super.onOpen();
	}

}
