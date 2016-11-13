package org.knime.preferences.prefCreator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A JPanel which holds a JTree, Buttons to add/remove/clear Priority/Pareto/Preference nodes to the JTree. 
 * @author Stefan Wohlfart
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PriorityPanel extends JPanel {

	// GUI COMPONENTS
	//Buttons for adding/removing/clearing priority or pareto nods
	private JButton addNode;
	private JButton removeNode;
	private JButton clearNodes;
	//tree adn the model which store all nodes
	private JTree jTree;
	private DefaultTreeModel treeModel;
	//rootnode which is not visible to the user
	private PreferenceNode rootNode;
	//options for creating either priority or pareto nodes
	private JRadioButton priority_jrb;
	private JRadioButton pareto_jrb;

	/**
	 * List which stores all Preference nodes.
	 */
	private List<PreferenceNode> preferenceNodes;

	// PREFERENCE PANEL
	private PreferencePanel preferencePanel;

	/**
	 * String constant for naming Priority nodes.
	 */
	public static final String PRIORITY = "Priority ";
	/**
	 * String constant for naming Pareto nodes.
	 */
	public static final String PARETO = "Pareto";

	/**
	 * Return value for the addPriorityNode method if the node was successfully added.
	 */
	public static final int PRIORITY_ADDED = 0;
	/**
	 * Return value for the addPriorityNode method if no Pareto node was selected.
	 */
	public static final int PRIORITY_NO_PARETO = 1;
	/**
	 * Return value for the addPriorityNode method if the JRadioButton for Priority was not selected.
	 */
	public static final int PRIORITY_NOT_SELECTED = 2;

	/**
	 * Return value for the addParetoNode method if the node was successfully added.
	 */
	public static final int PARETO_ADDED = 0;
	/**
	 * Return value for the addParetoNode method if no Priority node was selected.
	 */
	public static final int PARETO_NO_PRIORITY = 1;
	/**
	 * Return value for the addParetoNode method if the JRadioButton for Pareto was not selected.
	 */
	public static final int PARETO_NOT_SELECTED = 2;

	
	public static final int PREFERENCE_ADDED = 0;
	public static final int PREFENCE_NOT_SELECTED = 1;
	public static final int PREFERENCE_LAYER_NULL = 2;

	/**
	 * Constructor which initializes GUI components (JTree, Add, Remove, Clear, Pareto and Priority JRadioButtons) and adds them to this JPanel.
	 * @param query - a SQL-Query from a previous KNIME node which is used to initialize Preference nodes
	 * @param preferencePanel - a PreferencePanel instance whose variables are used for creating preference nodes
	 */
	protected PriorityPanel(String query, PreferencePanel preferencePanel) {

		super();

		preferenceNodes = new LinkedList<>();

		this.preferencePanel = preferencePanel;

		//set layout for this JPanel
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridwidth = GridBagConstraints.RELATIVE;
		gc.anchor = GridBagConstraints.NORTHWEST;
		gc.fill = GridBagConstraints.HORIZONTAL;

		// JTREE TO STORE PRIORITIES AND PREFERENCES
		jTree = new JTree();
		jTree.setRootVisible(false);
		rootNode = new PreferenceNode("root node, should be invisible");
		treeModel = new DefaultTreeModel(rootNode);
		jTree.setModel(treeModel);
		JScrollPane listScrollPane = new JScrollPane(jTree);
		listScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 2;
		gc.fill = GridBagConstraints.BOTH;
		add(listScrollPane, gc);
		// reset gridwith
		gc.gridwidth = 1;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weighty = 0;

		// jradiobuttonPanel to choose which node you want to create
		priority_jrb = new JRadioButton("Priority");
		priority_jrb.setSelected(true);
		pareto_jrb = new JRadioButton("Pareto");
		gc.gridx = 0;
		gc.gridy = 1;
		add(priority_jrb, gc);
		gc.gridx = 1;
		gc.gridy = 1;
		add(pareto_jrb, gc);

		//add/remove button for adding/removing priority or pareto nodes
		//clear button to clear all nodes
		addNode = new JButton("Add");
		removeNode = new JButton("Remove");
		clearNodes = new JButton("Clear");
		gc.gridwidth = 2;
		gc.gridx = 0;
		gc.gridy = 2;
		add(addNode, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		add(removeNode, gc);
		gc.gridx = 0;
		gc.gridy = 4;
		add(clearNodes, gc);
	}

	/**
	 * Method for adding an ActionListener to every GUI component (add/remove/clear JButtons and priority and pareto JRadioButtons)
	 * which triggers an event.
	 * @param listener - a PriorityListener which will be created in the SQLPreferenceEditor class
	 */
	protected void addActionListener(PriorityListener listener) {
		addNode.addActionListener(listener);
		removeNode.addActionListener(listener);
		clearNodes.addActionListener(listener);
		priority_jrb.addActionListener(listener);
		pareto_jrb.addActionListener(listener);
	}

	/**
	 * Adds a node to the parentNode from the JTree.
	 * @param parentNode - a Priority or Pareto node which will be the parent of the new added node
	 * @param node - a Priority/Pareto/Preference node which will be added to the JTree
	 */
	private void addNode(PreferenceNode parentNode, PreferenceNode node) {

		treeModel.insertNodeInto(node, parentNode, parentNode.getChildCount());
		if (parentNode == treeModel.getRoot()) {
			treeModel.nodeStructureChanged((TreeNode) treeModel.getRoot());
		}
		jTree.scrollPathToVisible(new TreePath(node.getPath()));
	}
	
	/**
	 * Adds a Priority node to either a Pareto node or if there aren't any nodes yet to the invisible root node.
	 * If there already exists a node in the JTree besides the root node then the user always needs to select a node 
	 * which will be the parent node of this new added node.
	 * @return PRIORITY_ADDED - if the node was successfully added. </br>
	 * PRIORITY_NO_PARETO - if the selected node is no Pareto node </br>
	 * PRIORITY_NOT_SELECTED - if the JRadioButton is not selected as 'Priority'.
	 * 
	 */
	public int addPriorityNode() {

		int isAdded = PRIORITY_NO_PARETO;
		
		if(!priority_jrb.isSelected())
			return PRIORITY_NOT_SELECTED;

		// check what node the user selected
		TreePath parentPath = jTree.getSelectionPath();

		if (parentPath != null) {
			PreferenceNode parentNode = (PreferenceNode) (parentPath.getLastPathComponent());

			// a priority node can only be created if the parent is a pareto
			// node (or if there are no nodes)
			if (parentNode.isParetoNode()) {
				PreferenceNode node = new PreferenceNode(PRIORITY);
				addNode(parentNode, node);
				isAdded = PRIORITY_ADDED;
			}

		} else {
			// if no nodes exist it can be added
			if (rootNode.getChildCount() == 0) {

				PreferenceNode node = new PreferenceNode(PRIORITY);
				addNode(rootNode, node);
				isAdded = PRIORITY_ADDED;

			}

		}

		return isAdded;

	}

	/**
	 * Adds a Pareto node to either a priority node or if there aren't any nodes yet to the invisible root node.
	 * If there already exists a node in the JTree besides the root node then the user always needs to select a node 
	 * which will be the parent node of this new added node.
	 * @return PARETO_ADDED - if the node was successfully added </br> 
	 * PARETO_NO_PRIORITY - if the selected node is no Priority node </br>
	 * PARETO_NOT_SELECTED - if the JRadioButton is not selected as 'Pareto'.
	 * 
	 */
	public int addParetoNode() {

		int isAdded = PARETO_NO_PRIORITY;
		
		if(!pareto_jrb.isSelected())
			return PARETO_NOT_SELECTED;

		// check what node the user selected
		TreePath parentPath = jTree.getSelectionPath();

		if (parentPath != null) {
			PreferenceNode parentNode = (PreferenceNode) (parentPath.getLastPathComponent());

			// a pareto node can only be created if the parent is a priority
			// node (or if there are no nodes)
			if (parentNode.isPriorityNode()) {
				PreferenceNode node = new PreferenceNode(PARETO);
				addNode(parentNode, node);
				isAdded = PARETO_ADDED;
			}

		} else {
			// if no nodes exist it can be added
			if (rootNode.getChildCount() == 0) {

				PreferenceNode node = new PreferenceNode(PARETO);
				addNode(rootNode, node);
				isAdded = PARETO_ADDED;

			}

		}

		return isAdded;

	}
	
	/**
	 * Removes a from the user selected Priority or Pareto node from the JTree.
	 * @return true - if the node was successfully added </br>
	 * false - if it wasn't removed from the JTree because no node was selected
	 */
	public boolean removeNode(){
		
		boolean isRemoved = false;
		
		TreePath currentSelection = jTree.getSelectionPath();
		if (currentSelection != null) {
			PreferenceNode node = (PreferenceNode) (currentSelection.getLastPathComponent());
			PreferenceNode parent = (PreferenceNode) (node.getParent());

			if (parent != null && (node.isParetoNode() || node.isPriorityNode())){
				removePreferencesFromNode(node);
				treeModel.removeNodeFromParent(node);
				isRemoved = true;
			}		
		}
		
		return isRemoved;
		
	}
	
	/**
	 * This method will be called if an old or saved state of the JTree was loaded from the KNIME settings.
	 * It will traverse through all children of this node (and the children of those children, etc.) until a leaf was found.
	 * It also checks if on of those nodes is a Preference node. The method will add all found Preference nodes
	 * to the preferenceNodes list. 
	 * @param node - a node which will be added 
	 */
	private void addPreferencesForNode(PreferenceNode node){
		  // traverse children
	    int childCount = node.getChildCount();
	    if (childCount == 0) {
	    	//check if leaf is a Preference node an remove it from the list
	    	//recursion ends here
	    	if(!node.isParetoNode() && !node.isPriorityNode())
	    		preferenceNodes.add(node);
	    
	    } else {
	        for (int i = 0; i < childCount; i++) {
	        	PreferenceNode child = (PreferenceNode) node.getChildAt(i);
	        	addPreferencesForNode(child);
	        }
	    }
	}

	/**
	 * This method will be called if a Priority or Pareto node will be removed from the JTree.
	 * It will traverse through all children of this node (and the children of those children, etc.) until a leaf was found.
	 * It also checks if on of those nodes is a Preference node. The method will remove all found Preference nodes
	 * from the preferenceNodes list. 
	 * @param node - a node which will be deleted
	 */
	private void removePreferencesFromNode(PreferenceNode node) {

	    // traverse children
	    int childCount = node.getChildCount();
	    if (childCount == 0) {
	    	//check if leaf is a Preference node an remove it from the list
	    	//recursion ends here
	    	if(!node.isParetoNode() && !node.isPriorityNode() && preferenceNodes.contains(node))
	    		preferenceNodes.remove(node);
	    
	    } else {
	        for (int i = 0; i < childCount; i++) {
	        	PreferenceNode child = (PreferenceNode) node.getChildAt(i);
	        	removePreferencesFromNode(child);
	        }
	    }
	}

	/**
	 * Removes every node from the root node and removes every node from the preferenceNodes list which holds every
	 * Preference node.
	 */
	public void clear() {
		
		preferenceNodes.clear();
		rootNode.removeAllChildren();
		treeModel.reload();
		
	}

	/**
	 * Adds a Preference node to the JTree and the preferenceNodes list which holds every Preference node. 
	 * This is only possible if a Priority or Pareto node was selected.
	 * If a Preference node with a 'LAYERED' preference should be added, 
	 * it will be first checked if there was a LayeredDialog instantiated. 
	 * @return PREFENCE_NOT_SELECTED - if there was no Pareto or Priority node selected </br>
	 * PREFERENCE_LAYER_NULL - if the LayeredDialog was not instantiated yet </br>
	 * PREFERENCE_ADDED - if the node was successfully added.
	 */
	public int addPreferenceNode() {

		int resultValue = PREFENCE_NOT_SELECTED;

		PreferenceNode parentNode = null;
		TreePath parentPath = jTree.getSelectionPath();
		
		//if tree is empty a simple prefence can be added. After that no other nodes are allowed in the tree.
		if(rootNode.getChildCount()==0)
			parentPath = new TreePath(rootNode.getPath());
		
		if (parentPath != null) {

			parentNode = (PreferenceNode) (parentPath.getLastPathComponent());

			if ((parentNode.isPriorityNode() || parentNode.isParetoNode()) || parentNode==rootNode) {
				
				LayeredDialog layeredDialog = preferencePanel.getLayeredDialogSaver()
						.getLayeredDialog(preferencePanel.getDimension());

				PreferenceNode node = new PreferenceNode("", preferencePanel, preferenceNodes.size());

				if (Preference.isLayered(preferencePanel.getPreference())) {
					if (layeredDialog == null) {
						return PREFERENCE_LAYER_NULL;
					} else {
						node.setRootNode(layeredDialog);
					}
				}

				node.changeNodeName();
				addNode(parentNode, node);
				preferenceNodes.add(node);

				resultValue = PREFERENCE_ADDED;
			}
		}

		return resultValue;

	}

	/**
	 * Removes a selected Preference node from the JTree and from the preferenceNodes list which holds every Preference node.
	 * @return true - if node was successfully removed </br>
	 * false - if no Preference node was selected by the user
	 */
	public boolean removePreferenceNode() {

		boolean isRemoved = false;

		TreePath path = jTree.getSelectionPath();
		if (path != null) {

			PreferenceNode node = (PreferenceNode) (path.getLastPathComponent());

			if (preferenceNodes.contains(node) && !node.isPriorityNode() && !node.isParetoNode()) {
				preferenceNodes.remove(node);
				for(int i=0; i < preferenceNodes.size(); i++)
					preferenceNodes.get(i).decreaseColumnNumber(node.getColumnNumber());
				treeModel.removeNodeFromParent(node);
				isRemoved = true;

			}
		}

		return isRemoved;
	}
	
	/**
	 * This method is accessed if an old or saved state from the KNIME settings is loaded.
	 * It initializes the treeModel, jTree and the rootNode with the old or loaded state. 
	 * @param treeModel - the old or saved treeModel
	 */
	public void loadPreviousState(DefaultTreeModel treeModel) {
		
		this.treeModel = treeModel;
		this.jTree.setModel(treeModel);
		this.rootNode = (PreferenceNode) treeModel.getRoot();
		this.preferenceNodes.clear();

		addPreferencesForNode(rootNode);

		for (int i = 0; i < jTree.getRowCount(); i++) {
			jTree.expandRow(i);
		}
	}

	/**
	 * 
	 * @return Returns the add button which adds Priority or Pareto nodes to the JTree.
	 */
	public JButton getAddNodeButton() {
		return addNode;
	}

	/**
	 * 
	 * @return Returns the remove button which removes Priority or Pareto nodes from the JTree.
	 */
	public JButton getRemoveNodeButton() {
		return removeNode;
	}

	/**
	 * 
	 * @return Returns the clear button which removes every node in the JTree.
	 */
	public JButton getClearNodesButton() {
		return clearNodes;
	}

	/**
	 * 
	 * @return Returns the tree model of this class.
	 */
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * 
	 * @return Returns the JTree of this class.
	 */
	public JTree getTree() {
		return jTree;
	}

	/**
	 * 
	 * @return Returns the root Node of this class.
	 */
	public PreferenceNode getRootNode() {
		return rootNode;
	}

	/**
	 * 
	 * @return Returns the Priority Radio Button which forces that only Priority nodes
	 * can be added to the JTree.
	 */
	public JRadioButton getPriorityRadioButton() {
		return priority_jrb;
	}

	/**
	 * 
	 * @return Returns the Pareto Radio Button which forces that only Pareto nodes
	 * can be added to the JTree.
	 */
	public JRadioButton getParetoRadioButton() {
		return pareto_jrb;
	}

	/**
	 * 
	 * @return Returns the preferenceNodes list which contains all Preference nodes in the JTree.
	 */
	public List<PreferenceNode> getPreferenceNodes() {
		return preferenceNodes;
	}
}
