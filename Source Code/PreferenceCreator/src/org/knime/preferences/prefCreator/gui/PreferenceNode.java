package org.knime.preferences.prefCreator.gui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Preference node which implements methods to create the score and preference
 * query.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class PreferenceNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 6529685098267757690L;

	private String dimension;
	private String preference;
	private double numericValue1;
	private double numericValue2;
	private String booleanValue;

	private DefaultMutableTreeNode rootNode;
	private DefaultMutableTreeNode layer0;
	private int positiveLayerSize;

	private boolean isParetoNode = false;
	private boolean isPriorityNode = false;

	/**
	 * String constant for alias in the preference node
	 */
	private final String COLUMN_NAME = "column";
	private int columnNumber;

	/**
	 * Constructor for preference nodes. All input fields from the preference
	 * panel will be saved as member variables. The nodeName represents the name
	 * in the JTree.
	 * 
	 * @param nodeName
	 *            - the name the node will have
	 * @param preferencePanel
	 *            - a preference panel instance which input fields will be
	 *            accessed and their values will be saved in member variables
	 */
	public PreferenceNode(String nodeName, PreferencePanel preferencePanel, int columnNumber) {

		super(nodeName);
		
		this.columnNumber = columnNumber;
		
		dimension = preferencePanel.getDimension();
		preference = preferencePanel.getPreference();
		numericValue1 = preferencePanel.getNumericValue1();
		numericValue2 = preferencePanel.getNumericValue2();
		booleanValue = preferencePanel.getBooleanValue();
		
		if(dimension == SQLPreferenceEditor.CUSTOM_DIMENSION)
			dimension = booleanValue;
		
	}

	/**
	 * Constructor for Pareto and Priority nodes. Sets the name for the node and
	 * a boolean if it is a Priority or Pareto node.
	 * 
	 * @param nodeName
	 *            - the name the node will have
	 */
	public PreferenceNode(String nodeName) {

		super(nodeName);

		if (nodeName.contains(PriorityPanel.PARETO)) {
			isParetoNode = true;
		} else if (nodeName.contains(PriorityPanel.PRIORITY)) {
			isPriorityNode = true;
		}
	}

	/**
	 * Creates Preference Statements (preference query parts) which will be used
	 * to create the preferenceQuery. (According to Preference-SQL Syntax)
	 * 
	 * @return Returns the preference statement for this node
	 */
	public String createPreferenceStatement() {
	
		String statement = "";

		if (Preference.isLowest(preference)) {

			statement = "" + dimension + " LOWEST";

		} else if (Preference.isHighest(preference)) {

			statement = "" + dimension + " HIGHEST";

		} else if (Preference.isAround(preference)) {

			statement = "" + dimension + " AROUND " + numericValue1 + "";

		} else if (Preference.isBetween(preference)) {

			statement = "" + dimension + " BETWEEN " + numericValue1 + ", " + numericValue2 + "";

		} else if (Preference.isBoolean(preference)) {

			statement = "" + dimension + " BOOLEAN";

		} else if (Preference.isLayered(preference)) {

			statement = createPreferenceLayeredStatement();

		}

		return statement;

	}

	/**
	 * Create the Preference Statement for a node with a Layered preference. This will be used for the preference query.
	 * 
	 * @return Returns the preference statement for the layered preference
	 */
	private String createPreferenceLayeredStatement() {

		StringBuffer statement = new StringBuffer("");

		statement.append("" + dimension + " LAYERED (");

		for (int i = 0; i < rootNode.getChildCount(); i++) {

			DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);

			if (layerNode != layer0 && layerNode.getChildCount() > 0) {
				statement.append("(");
			}

			if (layerNode == layer0 && layerNode.getChildCount() > 0) {
				statement.append("OTHERS");
			} else {

				for (int j = 0; j < layerNode.getChildCount(); j++) {

					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) layerNode.getChildAt(j);

					statement.append("'" + childNode.toString() + "'");

					if (j != layerNode.getChildCount() - 1)
						statement.append(", ");

				}
			}

			if (layerNode != layer0 && layerNode.getChildCount() > 0) {
				statement.append(")");
			}

			if (i < rootNode.getChildCount() - 1 && layerNode.getChildCount() > 0) {

				String str = "";

				if (i + 2 == rootNode.getChildCount()) {
					if (rootNode.getChildAt(i + 1).getChildCount() > 0) {
						str = ", ";
					}
				} else {
					str = ", ";
				}

				statement.append(str);

			}

		}

		statement.append(")");

		return statement.toString();
	}

	/**
	 * Changes the preferences names.
	 * Can be used from other classes to create names for this node with current settings.
	 * 
	 * @param preferencePanel
	 *            - a preference panel object
	 * @return Returns the name for the node according to the member variables values of this node
	 */
	public void changeNodeName() {

		String statement = "";
		

		if (Preference.isLowest(preference)) {

			statement = "" + dimension + " LOWEST";

		} else if (Preference.isHighest(preference)) {

			statement = "" + dimension + " HIGHEST";

		} else if (Preference.isAround(preference)) {

			statement = "" + dimension + " AROUND " + numericValue1 + "";

		} else if (Preference.isBetween(preference)) {

			statement = "" + dimension + " BETWEEN " + numericValue1 + ", " + numericValue2 + "";


		} else if (Preference.isBoolean(preference)) {

			statement = "" + dimension + " BOOLEAN";

		} else if (Preference.isLayered(preference)) {

			statement = createPreferenceLayeredStatement();

		}

		//change name of this node
		setUserObject(statement);
		
	}
	

	/**
	 * Creates clone of the rootNode and the Layer 0 from the LayeredDialog and saves these clones as member variables.
	 * 
	 * @param layeredDialog
	 *            - a layered dialog object
	 */
	public void setRootNode(LayeredDialog layeredDialog) {
		
		rootNode = new DefaultMutableTreeNode("rootNode");
		layer0 = new DefaultMutableTreeNode(LayeredDialog.LAYER + 0);
		//positive layer size used for creating the score statement
		positiveLayerSize = layeredDialog.getPositiveLayerSize();
		
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		JTree jTree = new JTree();
		jTree.setModel(treeModel);
		
		DefaultMutableTreeNode layeredRoot = layeredDialog.getRootNode();
		
		for(int i=0; i < layeredRoot.getChildCount(); i++){
			
			//add layers to rootNode
			DefaultMutableTreeNode layeredLayer = (DefaultMutableTreeNode) layeredRoot.getChildAt(i);
			
			if(layeredLayer == layeredDialog.getLayer0()){
				layer0 = new DefaultMutableTreeNode(layeredLayer.getUserObject());
				layer0.removeAllChildren();
				rootNode.add(layer0);
			}
			else{
				DefaultMutableTreeNode layer = new DefaultMutableTreeNode(layeredLayer.getUserObject());
				layer.removeAllChildren();
				rootNode.add(layer);
			}
			
			for(int j=0; j < layeredLayer.getChildCount(); j++){
				
				DefaultMutableTreeNode layeredValue = (DefaultMutableTreeNode) layeredLayer.getChildAt(j);
				
				DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(layeredValue.getUserObject());
					
				addNode((DefaultMutableTreeNode) rootNode.getChildAt(i),valueNode,treeModel,jTree);
				
			}
		
		}
	}

	/**
	 * Creates Score statement (part of the score query).
	 * 
	 * @return Returns score statement
	 */
	public String createScoreStatement() {
		
		String alias = COLUMN_NAME + columnNumber;

		String statement = "";

		if (Preference.isLowest(preference)) {

			statement = "(" + dimension + ") AS " + alias + "";

		} else if (Preference.isHighest(preference)) {

			statement = "(-(" + dimension + ")) AS " + alias + "";

		} else if (Preference.isAround(preference)) {

			statement = "(abs(" + numericValue1 + "-" + dimension + ")) AS " + alias + "";

		} else if (Preference.isBetween(preference)) {

			statement = "(CASE WHEN " + dimension + " >= " + numericValue1 + " AND " + dimension + " <= "
					+ numericValue2 + " THEN 0 " + "WHEN " + dimension + " < " + numericValue1 + " THEN ("
					+ numericValue1 + " - " + dimension + ") " + "WHEN " + dimension + " > "
					+ numericValue2 + " THEN (" + dimension + "-" + numericValue2 + ") END) AS "
					+ alias + "";

		} else if (Preference.isBoolean(preference)) {

			statement = "(-(" + booleanValue + ")) AS " + alias + "";

		} else if (Preference.isLayered(preference)) {

			statement = "(" + createScoreLayeredStatement() + ") AS " + alias + "";

		}

		return statement;

	}

	/**
	 * Creates the score statement for the layered preference.
	 * 
	 * @param dimension
	 *            - the dimension this node is assigned with
	 * @return Returns the score statement for the layered preference
	 */
	private String createScoreLayeredStatement() {

		StringBuffer layered = new StringBuffer("-(CASE ");

		int num_positive_layers = positiveLayerSize;
		int num_negative_layers = -1;

		boolean isNegative = false;

		for (int i = 0; i < rootNode.getChildCount(); i++) {

			DefaultMutableTreeNode layer = (DefaultMutableTreeNode) rootNode.getChildAt(i);

			if (layer.getChildCount() > 0) {

				layered.append("WHEN " + dimension + " IN (");

				for (int j = 0; j < layer.getChildCount(); j++) {
					layered.append("'" + layer.getChildAt(j) + "'");

					if (j < layer.getChildCount() - 1) {
						layered.append(", ");
					}
				}

				if (layer == layer0) {

					layered.append(") THEN 0 ");
					isNegative = true;

				} else {

					if (!isNegative) {
						layered.append(") THEN " + num_positive_layers-- + " ");
					} else {
						layered.append(") THEN " + num_negative_layers-- + " ");
					}

				}
			} else if (layer == layer0) {
				isNegative = true;
			}
		}

		layered.append("END)");

		return layered.toString();

	}

	private void addNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node, DefaultTreeModel treeModel, JTree jTree) {

		// adds the node to the parentNode
		treeModel.insertNodeInto(node, parentNode, parentNode.getChildCount());
		if (parentNode == treeModel.getRoot()) {
			treeModel.nodeStructureChanged((TreeNode) treeModel.getRoot());
		}
		jTree.scrollPathToVisible(new TreePath(node.getPath()));
		
		for (int i = 0; i < jTree.getRowCount(); i++) {
		    jTree.expandRow(i);
		}
	}


	/**
	 * 
	 * @return Returns the dimension of this node
	 */
	public String getDimension() {
		return dimension;
	}

	/**
	 * 
	 * @return Returns the preference of this node
	 */
	public String getPreference() {
		return preference;
	}

	/**
	 * 
	 * @return Returns the first input value of this node (around value or the
	 *         lower bound for the between preference)
	 */
	public double getNumericValue1() {
		return numericValue1;
	}

	/**
	 * 
	 * @return Returns the second input value of this node (upper bound for the
	 *         between preference)
	 */
	public double getNumericValue2() {
		return numericValue2;
	}

	/**
	 * 
	 * @return Returns the custom dimension of this node
	 */
	public String getBooleanValue() {
		return booleanValue;
	}

	/**
	 * 
	 * @return Returns the root node of the layered dialog
	 */
	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	/**
	 * 
	 * @return Returns the positive layer size of the layered dialog
	 */
	public int getPositiveLayerSize() {
		return positiveLayerSize;
	}

	/**
	 * 
	 * @return true - if this node is a Priority node </br>
	 *         false - otherwise
	 */
	public boolean isPriorityNode() {
		return isPriorityNode;
	}

	/**
	 * 
	 * @return true - if this node is a Pareto node </br>
	 *         false - otherwise
	 */
	public boolean isParetoNode() {
		return isParetoNode;
	}

	/**
	 * 
	 * @return Returns the name of the column which this nodes represents for
	 *         the score table
	 */
	public String getAlias(){
		return COLUMN_NAME+columnNumber;
	}
	
	/**
	 * Decreases the value from the columnNumber of the object by 1 if the
	 * inputed columnNumber is less than the columnNumber of the object.
	 * @param columnNumber - a integer value
	 */
	public void decreaseColumnNumber(int columnNumber){
		if(columnNumber < this.columnNumber)
			this.columnNumber--;
	}
	
	
	public int getColumnNumber(){
		return columnNumber;
	}
}
