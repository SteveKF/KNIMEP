package org.knime.preferences.prefCreator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This is the LayeredDialog which allows for every dimension to prioritize the values of this dimension in layers.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class LayeredDialog extends JDialog {

	// variables for tree
	private JTree jTree;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode layer0;

	//checkbox to force that only positive or negative layers can be added
	private JCheckBox checkBox;
	// buttons for adding/removing layers
	private JButton addLayer;
	private JButton removeLayer;
	private JButton clearLayer;

	// Box with all values for the dimension selected in the preference pane.
	private JComboBox<String> valueBox;
	//buttons to add and remove values from layers
	private JButton addValue;
	private JButton removeValue;

	// label for tips
	private CustomWrappedLabel tooltipLabel;

	//button to save dialog
	private JButton saveButton;
	
	//list which stores all value nodes
	private List<DefaultMutableTreeNode> layerList;
	
	// number of layers
	private int NUM_POSITIVE_LAYERS = 0;
	private int NUM_NEGATIVE_LAYERS = 0;

	// integer for checking if a value node was added or not
	public static final int VALUE_NODE_ADDED = 0;
	public static final int VALUE_NO_LAYER_SELECTED = 1;
	public static final int VALUE_SINGLE_ELEMENT = 2;
	
	public static final int VALUE_NODE_REMOVED = 0;
	public static final int VALUE_LAYER0 = 1;
	//VALUE_SINGLE_ELEMENT = 2

	public static final String LAYER = "Layer ";

	/**
	 * Constructor of the LayeredDialog which sets up a JTree and Buttons and Boxes to move the values to between Layers.
	 * @param values - the values for the dimension
	 */
	public LayeredDialog(List<String> values) {

		super();
		
		layerList = new ArrayList<>();

		//LEFTSIDEPANEL

		// jtree to store priorities and nonNumeric values
		jTree = new JTree();
		jTree.setRootVisible(false);
		rootNode = new DefaultMutableTreeNode("root node, should be invisible");
		treeModel = new DefaultTreeModel(rootNode);
		jTree.setModel(treeModel);

		// add a positive layer (Layer 1)
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeModel.getRoot();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(LAYER + ++NUM_POSITIVE_LAYERS);
		addNode(parentNode, node);
		layerList.add(node);

		//add layer 0 which contains every value at the first start of the JPanel
		parentNode = (DefaultMutableTreeNode) treeModel.getRoot();
		layer0 = new DefaultMutableTreeNode(LAYER +0);
		addNode(parentNode, layer0);

		// add every value to layer 0
		for (int i = 0; i < values.size(); i++) {
			node = new DefaultMutableTreeNode(values.get(i));
			addNode(layer0, node);
		}

		//add layer a negative layer (Layer -1)
		parentNode = (DefaultMutableTreeNode) treeModel.getRoot();
		node = new DefaultMutableTreeNode(LAYER + --NUM_NEGATIVE_LAYERS);
		addNode(parentNode, node);
		layerList.add(node);
		
		JScrollPane listScrollPane = new JScrollPane(jTree);

		// buttons to add and remove layers => buttonPrioPanel
		JPanel buttonPrioPanel = new JPanel(new GridLayout(4, 0));
		addLayer = new JButton("Add");
		checkBox = new JCheckBox("Negative Layers");
		removeLayer = new JButton("Remove");
		clearLayer = new JButton("Clear");
		buttonPrioPanel.add(checkBox);
		buttonPrioPanel.add(addLayer);
		buttonPrioPanel.add(removeLayer);
		buttonPrioPanel.add(clearLayer);

		JPanel leftSidePanel = new JPanel(new BorderLayout());
		leftSidePanel.add(listScrollPane, BorderLayout.CENTER);
		leftSidePanel.add(buttonPrioPanel, BorderLayout.SOUTH);

		//RIGHTSIDEPANEL

		// NORTH
		String[] vals = new String[values.size()];
		valueBox = new JComboBox<>(values.toArray(vals));

		// CENTER
		JPanel buttonLayeredPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 2);
		gridLayout.setHgap(10);
		buttonLayeredPanel.setLayout(gridLayout);
		addValue = new JButton("Add");
		removeValue = new JButton("Remove");
		buttonLayeredPanel.add(addValue);
		buttonLayeredPanel.add(removeValue);

		// SOUTH
		tooltipLabel = new CustomWrappedLabel("Put the values in the order you want them to get prioritized.");
		tooltipLabel.setColumns(15);
		tooltipLabel.setRows(3);

		
		JPanel rightSidePanel = new JPanel(new GridLayout(2, 0, 5, 15));
		rightSidePanel.add(valueBox);
		rightSidePanel.add(buttonLayeredPanel);

		
		saveButton = new JButton("Save");
		
		JPanel test = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridy = 0;
		gc.insets = new Insets(5, 0, 5, 0);
		test.add(rightSidePanel, gc);
		gc.gridy = 1;
		test.add(tooltipLabel, gc);
		gc.gridy = 2;
		test.add(saveButton,gc);

		
		// add both sides to one split pane and add it to this class
		JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSidePanel, test);
		horizontalSplitPane.setPreferredSize(new Dimension(800, horizontalSplitPane.getPreferredSize().height));
		horizontalSplitPane.setResizeWeight(0.5);

		LayeredListener listener = new LayeredListener(this);
		addActionListener(listener);

		add(horizontalSplitPane);
		
		setAlwaysOnTop(true);
	}

	/**
	 * Adds an ActionListener to every GUI component which triggers an event.
	 * @param listener
	 */
	private void addActionListener(ActionListener listener) {
		addLayer.addActionListener(listener);
		removeLayer.addActionListener(listener);
		clearLayer.addActionListener(listener);
		addValue.addActionListener(listener);
		removeValue.addActionListener(listener);
		saveButton.addActionListener(listener);
	}

	/**
	 * Adds the node to the parentNode.
	 * @param parentNode - a DefaultMutableTreeNode which serves as the parent for the node
	 * @param node - DefaultMutableTreeNode which will be added to the JTree as a child of the parentNode
	 */
	private void addNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node) {

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
	 * Adds a positive Layer if the checkbox for negative layers isn't selected and the positive ancestor layer is not empty.</br>
	 * Adds a negative Layer if the checkbox for negative layers is selected and the negative descendant layer  is not empty.</br>
	 * Sorts the layers in the JTree to following scheme: Layer 1, Layer 2, ..., Layer 0, Layer -1, Layer -2 ,...</br>
	 * @return true - if a layer was added </br>
	 * false - if no layer was added
	 */
	public boolean addLayerNode() {

		boolean isAdded = false;

		DefaultMutableTreeNode node = null;

		/* checkbox for negative layers should not be selected
		and the ancestor should have nodes in it
		 => new positive layer node*/
		if (!checkBox.isSelected() && (!isEmpty(searchNode(LAYER + NUM_POSITIVE_LAYERS))|| NUM_POSITIVE_LAYERS==0)) {

			node = new DefaultMutableTreeNode(LAYER + ++NUM_POSITIVE_LAYERS);

			// checkbox for negative layers should be selected
			// and the descendant should have no nodes in it
			// = > new negative layer node
		} else if (checkBox.isSelected()
				&& (!isEmpty(searchNode(LAYER + NUM_NEGATIVE_LAYERS))|| NUM_NEGATIVE_LAYERS==0)) {

			node = new DefaultMutableTreeNode(LAYER + --NUM_NEGATIVE_LAYERS);

		}

		// add the layer node to the tree and to the layerlist
		if (node != null) {
			addNode(rootNode, node);
			layerList.add(node);
			isAdded = true;
		}

		// sorts the tree
		sortTree();

		return isAdded;
	}

	/**
	 * Adds a value node to the selected Layer node if it isn't Layer 0;
	 * @return VALUE_NO_LAYER_SELECTED - if no Layer was selected </br>
	 * VALUE_SINGLE_ELEMENT - if the value node exists in another Layer and can't be deleted because this will cause a empty Layer before another one</br>
	 * VALUE_NODE_ADDED - if the node was successfully added
	 */
	public int addValueNode() {

		int isNodeAdded = VALUE_NO_LAYER_SELECTED;

		DefaultMutableTreeNode parentNode = null;
		DefaultMutableTreeNode childNode = null;

		TreePath parentPath = jTree.getSelectionPath();
		if (parentPath != null) {
			
			parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());

			// add valueNode only if the selected node is a layer node
			if (layerList.contains(parentNode) || parentNode == layer0) {

				// searches for the value node with the current selected value
				childNode = searchNode((String) valueBox.getSelectedItem());
				if (childNode != null) {
					
					int result = removeValueNode();
					
					if(result == VALUE_LAYER0 || result == VALUE_NODE_REMOVED){
						removeNodeFromLayer0(childNode);
						addNode(parentNode, childNode);	
						isNodeAdded = VALUE_NODE_ADDED;
					}else
						isNodeAdded = VALUE_SINGLE_ELEMENT;
				}
			}
		}

		return isNodeAdded;
	}

	/**
	 * Removes the node from Layer 0.
	 * @param node - a node which should be removed from node
	 * @return true - if the Layer 0 contains the node and can be removed </br>
	 * false - otherwise
	 */
	private boolean removeNodeFromLayer0(DefaultMutableTreeNode node) {

		boolean isRemoved = false;

		DefaultMutableTreeNode childNode = null;

		for (int i = 0; i < layer0.getChildCount(); i++) {
			if (layer0.getChildAt(i)==node) 
				childNode = node;
		}

		if (childNode != null) {
			layer0.remove(childNode);
			treeModel.reload();
			isRemoved = true;
		}

		return isRemoved;

	}

	/**
	 * Removes the last positive Layer node if the checkBox for negative Layers is unselected. </br>
	 * Otherwise the last negative Layer node is removed from the JTree.
	 * @return true - if the layer node was removed </br>
	 * false - if there isn't any layer node which can be removed
	 */
	public boolean removeLayerNode() {

		boolean isRemoved = false;
		boolean isPositiveLayer = false;

		if (rootNode.getChildCount() > 0) {
			DefaultMutableTreeNode node = null;

			if (!checkBox.isSelected() && NUM_POSITIVE_LAYERS > 0) {
				node = searchNode(LAYER + NUM_POSITIVE_LAYERS);
				isPositiveLayer = true;

			} else if (checkBox.isSelected() && NUM_NEGATIVE_LAYERS < 0) {
				node = searchNode(LAYER + NUM_NEGATIVE_LAYERS);
				isPositiveLayer = false;
			}

			if (node != null) {
				
				if(isPositiveLayer)
					NUM_POSITIVE_LAYERS--;
				else
					NUM_NEGATIVE_LAYERS++;

				List<DefaultMutableTreeNode> removedValueNodes = new LinkedList<>();
				for (int i = 0; i < node.getChildCount(); i++) {
					removedValueNodes.add((DefaultMutableTreeNode) node.getChildAt(i));
				}

				treeModel.removeNodeFromParent(node);
				
				if(layerList.contains(node))
					layerList.remove(node);
				
				isRemoved = true;

				//add every value nodes to layer 0
				for (int i = 0; i < removedValueNodes.size(); i++) {
					addNode(layer0,removedValueNodes.get(i));
				}
			}
		}
		// else isRemoved stays false

		return isRemoved;
	}

	/**
	 * Removes the value node with the value which is currently selected in the valueBox.
	 * @return VALUE_LAYER0 - if the value node's parent is Layer 0 and therefore can't be removed </br>
	 * VALUE_SINGLE_ELEMENT - if the value node can't be deleted because these will cause empty Layers with existing descendant layers</br>
	 * VALUE_NODE_REMOVED - if the value node was successfully removed
	 */
	public int removeValueNode() {

		int result = VALUE_LAYER0;

		DefaultMutableTreeNode deletableNode = searchNode((String) valueBox.getSelectedItem());
		
		if (deletableNode != null && layerList.contains(deletableNode.getParent())) {
			if (isRemovable(deletableNode)) {
				treeModel.removeNodeFromParent(deletableNode);
				//add removed node to layer 0 
				addNode(layer0,deletableNode);
				result = VALUE_NODE_REMOVED;
			} else {
				result = VALUE_SINGLE_ELEMENT;
			}
		}

		return result;
	}

	/**
	 * Removes every Layer node with their respective value nodes. </br>
	 * Layer 0 will not be removed and all value nodes will be added to Layer 0.
	 */
	public void clear() {

		List<DefaultMutableTreeNode> valueNodes = new LinkedList<>();

		for (int i = 0; i < rootNode.getChildCount(); i++) {
			DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
			for (int j = 0; j < layerNode.getChildCount(); j++) {
				if (layerNode != layer0) {
					valueNodes.add((DefaultMutableTreeNode) layerNode.getChildAt(j));
				}
			}
		}

		rootNode.removeAllChildren();
		treeModel.reload();
		// add layer 0 to tree
		addNode(rootNode, layer0);

		for (int i = 0; i < valueNodes.size(); i++) {
			addNode(layer0, valueNodes.get(i));
		}
		// reset counters
		NUM_POSITIVE_LAYERS = 0;
		NUM_NEGATIVE_LAYERS = 0;
	}

	
	/**
	 * Sorts the Layer nodes from the JTree in following order: </br>
	 * Layer 1, Layer 2, ..., Layer 0, Layer -1, Layer -2, ...
	 */
	private void sortTree() {

		LinkedList<Integer> positive = new LinkedList<>();
		LinkedList<Integer> negative = new LinkedList<>();

		for (int i = 0; i < rootNode.getChildCount(); i++) {

			String nodeName = rootNode.getChildAt(i).toString();
			nodeName = nodeName.replace(LAYER, "");
			int number = Integer.parseInt(nodeName);
			// if the number of the layer is greater then 0
			if (number > 0) {
				positive.add(number);
			} else if (number <= 0) {
				negative.add(number);
			}
		}

		// order ascending
		Collections.sort(positive, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1 < o2) {
					return -1;
				} else if (o1 > o2) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		// order descending
		Collections.sort(negative, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (o1 < o2) {
					return 1;
				} else if (o1 > o2) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		List<DefaultMutableTreeNode> nodeList = new LinkedList<>();
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			nodeList.add((DefaultMutableTreeNode) rootNode.getChildAt(i));
		}

		rootNode.removeAllChildren();

		DefaultMutableTreeNode node = null;

		for (int i = 0; i < positive.size(); i++) {
			node = searchNode(LAYER + positive.get(i), nodeList);
			addNode(rootNode, node);
		}

		for (int i = 0; i < negative.size(); i++) {
			node = searchNode(LAYER + negative.get(i), nodeList);
			addNode(rootNode, node);
		}
	}

	/**
	 * Searches the JTree for a node with the name of the value of nodeStr.
	 * @param nodeStr - a string value which represents a name of a node
	 * @return a DefaultMutableTreeNode with the name of the value of nodeStr - if it exists in the JTree </br>
	 * null - if a node with this name doesn't exist
	 */
	private DefaultMutableTreeNode searchNode(String nodeStr) {
		DefaultMutableTreeNode node = null;
		@SuppressWarnings("rawtypes")
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			node = (DefaultMutableTreeNode) e.nextElement();
			if (nodeStr.equals(node.getUserObject().toString())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Checks if a node exists in the list nodeList with a name of the value from nodeStr
	 * @param nodeStr - a string value which represents a node name
	 * @param nodeList - a list of nodes
	 * @return a DefaultMutableTreeNode with the name of the value nodeStr - if it exists in the nodeList </br>
	 * null - if a node with this name doesn't exist
	 */
	private DefaultMutableTreeNode searchNode(String nodeStr, List<DefaultMutableTreeNode> nodeList) {
		DefaultMutableTreeNode node = null;
		for (int i = 0; i < nodeList.size(); i++) {
			node = nodeList.get(i);
			if (nodeStr.equals(node.getUserObject().toString())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Checks if this node is empty. 
	 * @param node - a layer node
	 * @return true - if this node is empty, if the node is null, the input node is no Layer node or the node is Layer 0</br>
	 * false - if this node has value nodes
	 */
	private boolean isEmpty(DefaultMutableTreeNode node) {
		
		assert(node != null);
		
		//return true if the node isn't a layer node or is Layer 0
		if(!layerList.contains(node) || node == layer0)
			return true;

		boolean isEmpty = node.getChildCount() <= 0 ? true : false;

		return isEmpty;
	}


	/**
	 * Checks if the parent of the inputed node is empty, if the node will be deleted.</br>
	 * If this is true it also will be checked if a Descendant Layer exists.
	 * @param node - a value node
	 * @return true - if the parent node has still some value nodes after deleting the node or if it is empty no Descendant Layer exists</br>
	 * false - if the parent node would be empty and a Descendant Layer exists </br>
	 * 
	 */
	private boolean isRemovable(DefaultMutableTreeNode node) {

		boolean isRemovable = true;

		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

		if (parent != null) {
			if (parent.getChildCount() - 1 == 0) {
				String nodeName = parent.toString();
				nodeName = nodeName.replace(LAYER, "");
				int layerNumber = Integer.parseInt(nodeName);

				if (layerNumber > 0) 
					layerNumber++;
				else if(layerNumber < 0)
					layerNumber--;
				

				DefaultMutableTreeNode followingNode = searchNode(LAYER + layerNumber);

				if (followingNode != null) 
					isRemovable = false;
				

			}

		}else
			isRemovable = false;

		return isRemovable;

	}

	/**
	 * 
	 * @return Returns button to add Layers
	 */
	public JButton getAddLayerButton() {
		return addLayer;
	}
	
	/**
	 * 
	 * @return Returns button to remove Layers
	 */
	public JButton getRemoveLayerButton() {
		return removeLayer;
	}

	/**
	 * 
	 * @return Returns button to clear Layers
	 */
	public JButton getClearLayerButton() {
		return clearLayer;
	}

	/**
	 * 
	 * @return Returns button to add value nodes
	 */
	public JButton getAddValueButton() {
		return addValue;
	}

	/**
	 * 
	 * @return Returns button to remove value nodes
	 */
	public JButton getRemoveValueButton() {
		return removeValue;
	}

	/**
	 * 
	 * @return Returns the CustomWrappedLabel which sets tooltips for the user
	 */
	public CustomWrappedLabel getToolTipLabel() {
		return tooltipLabel;
	}
	
	/**
	 * 
	 * @return Returns the save button which saves the Layered Dialog
	 */
	public JButton getSaveButton(){
		return saveButton;
	}

	/**
	 * 
	 * @return Returns the check box which tells this class to add/remove only positive or negative layers 
	 */
	public JCheckBox getCheckBox() {
		return checkBox;
	}

	/**
	 * 
	 * @return Returns the JComboBox for all values of the dimension.
	 */
	public JComboBox<String> getValueBox() {
		return valueBox;
	}
	
	/**
	 * 
	 * @return Returns the JTree with all values and 
	 */
	public JTree getJTree() {
		return jTree;
	}

	/**
	 * 
	 * @return Returns the Tree model
	 */
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * 
	 * @return Returns the root node
	 */
	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	/**
	 * 
	 * @return Returns the positive layer size
	 */
	public int getPositiveLayerSize() {
		return NUM_POSITIVE_LAYERS;
	}

	/**
	 * 
	 * @return Returns the negative layer size
	 */
	public int getNegativeLayerSize() {
		return NUM_NEGATIVE_LAYERS;
	}
	
	/**
	 * 
	 * @return Returns Layer0
	 */
	public DefaultMutableTreeNode getLayer0(){
		return layer0;
	}
}
