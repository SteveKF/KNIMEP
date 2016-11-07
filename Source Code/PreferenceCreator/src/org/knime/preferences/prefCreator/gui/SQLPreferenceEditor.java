package org.knime.preferences.prefCreator.gui;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import org.knime.core.node.InvalidSettingsException;

@SuppressWarnings("serial")
public class SQLPreferenceEditor extends JPanel {
	
	

	private ToolTipPanel toolTipPanel;
	private PriorityPanel priorityPanel;
	private PreferencePanel preferencePanel;
	private SQLGenerator sqlGenerator;

	// input variables
	private String[] dimensions;
	private Map<String, List<String>> values;
	private Map<String, Boolean> isDimensionNumeric;
	
	/**
	 * String which holds the value "Custom Dimension" which will be used for the dimension JComboBox
	 */
	public static final String CUSTOM_DIMENSION = "Custom Dimension";
	
	public SQLPreferenceEditor(String[] dimensions, Map<String, List<String>> values,
			Map<String, Boolean> isDimensionNumeric, String query) {

		this.values = values;
		this.isDimensionNumeric = isDimensionNumeric;
		this.dimensions = dimensions;
		//add custom dimension to dimensions arrays
		List<String> tmp = new ArrayList<>(Arrays.asList(dimensions));
		tmp.add(CUSTOM_DIMENSION);
		dimensions = new String[tmp.size()];
		tmp.toArray(dimensions);
		isDimensionNumeric.put(CUSTOM_DIMENSION, false);

		toolTipPanel = new ToolTipPanel();

		preferencePanel = new PreferencePanel(dimensions, values, isDimensionNumeric);
	
		setLayout(new BorderLayout());
		priorityPanel = new PriorityPanel(query, preferencePanel);

		// add priority and preference panel to one split pane
		JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, priorityPanel, preferencePanel);
		horizontalSplitPane.setPreferredSize(new Dimension(700, 530));
		horizontalSplitPane.setResizeWeight(0.5);
		horizontalSplitPane.setDividerLocation(350);
		
		// add the horizontal split pane and the tool tip panel to one
		// split pane
		JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplitPane, toolTipPanel);
		verticalSplitPane.setResizeWeight(0.9);
		add(verticalSplitPane);

		// adds actionlistener to every object which needs to have it
		PriorityListener priorityHandler = new PriorityListener(priorityPanel, toolTipPanel);
		sqlGenerator = new SQLGenerator(priorityPanel,query,dimensions);
		PreferenceListener preferenceHandler = new PreferenceListener(priorityPanel, preferencePanel, toolTipPanel,sqlGenerator);

		priorityPanel.addActionListener(priorityHandler);
		preferencePanel.addActionListener(preferenceHandler);
	
	}

	public void printTest() {
		sqlGenerator.print();
	}

	public String getScoreQuery() throws InvalidSettingsException {
		return sqlGenerator.getScoreQuery();
	}

	public String getPreferenceQuery() {
		return sqlGenerator.getPreferenceQuery();
	}

	public String[] getDimensions() {
		return dimensions;
	}

	public Map<String, List<String>> getValues() {
		return values;
	}

	public Map<String, Boolean> getIsDimensionNumeric() {
		return isDimensionNumeric;
	}

	public TreeMap<String,String> getPreferences() throws InvalidSettingsException {
		return sqlGenerator.getPreferences();
	}

	public String[] getPreferenceDimensions() {
		String[] prefDimensions = sqlGenerator.getPreferenceDimensions();
		if(prefDimensions==null)
			return new String[0];
		else
			return prefDimensions;
	}
	
	public DefaultTreeModel getTreeModel(){
		return priorityPanel.getTreeModel();
	}
	
	public void loadPreviousState(DefaultTreeModel treeModel){
		priorityPanel.loadPreviousState(treeModel);
	}
}
