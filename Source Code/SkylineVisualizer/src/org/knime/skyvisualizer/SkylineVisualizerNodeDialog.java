package org.knime.skyvisualizer;

import java.awt.FlowLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeDialog</code> for the "(Representative) Skyline Visualizer" Node. A node to
 * visualize skylines with their dominated points or representative skylines with the corresponding skyline.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 */
public class SkylineVisualizerNodeDialog extends DefaultNodeSettingsPane {

	public static final String[] options = new String[] { "Skyline Graph", "Representative Skyline Graph", "Custom" };
	
	private SettingsModelString optionSelector = new SettingsModelString(
			SkylineVisualizerNodeModel.CFGKEY_GRAPH_OPTIONS, options[1]);
	private SettingsModelString chartName;
	private SettingsModelString subTitle;
	private SettingsModelString dominatedPointsName;
	private SettingsModelString undominatedPointsName;
	
	private ChangeListener listener;


	protected SkylineVisualizerNodeDialog() {
		
		//options which changes the chartname, subtitle and the legend names accordingly 
		addDialogComponent(
				new DialogComponentButtonGroup(optionSelector, false, "Choose which Graph you want:", options));

		/*custom gui components which allow the user to input 
		customized chartname, subtitles and legend name for dominated and undominated points*/
		createNewGroup("Custom Graph Options");
		chartName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_CHART_NAME, "");
		DialogComponentString dialog1 = new DialogComponentString(chartName, "Chartname:");
		dialog1.getComponentPanel().setLayout( new FlowLayout(FlowLayout.RIGHT));
		addDialogComponent(dialog1);
		chartName.setEnabled(false);

		subTitle = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_SUBTITLE, "");
		DialogComponentString dialog2 = new DialogComponentString(subTitle, "Subtitle:");
		dialog2.getComponentPanel().setLayout( new FlowLayout(FlowLayout.RIGHT));
		addDialogComponent(dialog2);

		dominatedPointsName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_DOMINATED_POINTS_NAME, "");
		DialogComponentString dialog3 = new DialogComponentString(dominatedPointsName, "Name for dominated points: ");
		dialog3.getComponentPanel().setLayout( new FlowLayout(FlowLayout.RIGHT));
		addDialogComponent(dialog3);

		undominatedPointsName = new SettingsModelString(SkylineVisualizerNodeModel.CFGKEY_UNDOMINATED_POINTS_NAME, "");
		DialogComponentString dialog4 = new DialogComponentString(undominatedPointsName, "Name for undominated points: ");
		dialog4.getComponentPanel().setLayout( new FlowLayout(FlowLayout.RIGHT));
		addDialogComponent(dialog4);
		
		closeCurrentGroup();
		
		listener = new ChangeListener() {
			
			//listener so if the custom option isn't selected all custom GUI components are disabled 
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
		
		/*trigger an event manually so the GUI components get enabled or disabled according 
		to the default value of options*/
		listener.stateChanged(new ChangeEvent(optionSelector));
	}
	
	@Override
	public void onOpen() {
		//trigger event manually
		if(listener!=null)
			listener.stateChanged(new ChangeEvent(optionSelector));
		super.onOpen();
	}
}
