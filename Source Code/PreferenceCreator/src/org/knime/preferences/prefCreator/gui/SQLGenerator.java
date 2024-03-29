package org.knime.preferences.prefCreator.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knime.core.node.InvalidSettingsException;

/**
 * Class which creates the preferenceQuery, scoreQuery and priorities for every
 * column of the score table (created by the scoreQuery).
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class SQLGenerator {

	// INPUT VARIABLES
	// priority panel object which holds the root node of the JTree
	private PriorityPanel priorityPanel;
	// original query from a previous KNIME node
	private String query;

	// original dimensions
	private String[] dimensions;

	// columnNames of the scoreTable
	private List<String> columnNames;
	// a list of priorities/indexes for every columnName
	private Map<String, List<Integer>> indexMap;

	// OUTPUT VARIABLES
	private StringBuffer scoreQuery;
	private StringBuffer preferenceQuery;

	/**
	 * Constructor which initializes some variables.
	 * 
	 * @param priorityPanel
	 *            - a priority panel
	 * @param query
	 *            - the original query which is part of a output from previous
	 *            nodes
	 */
	public SQLGenerator(PriorityPanel priorityPanel, String query, String[] dimensions) {

		this.priorityPanel = priorityPanel;
		this.query = query;
		this.dimensions = dimensions;

		columnNames = new ArrayList<>();

	}

	/**
	 * Computes the scoreQuery and preferenceQuery. If shouldAddPriority = true,
	 * it also computes the priorities for every column of the scoreTable.
	 * 
	 * @param shouldAddPriority
	 *            - a boolean to check if priorities for every column name of
	 *            the scoretable should be computed
	 */
	private void computeValues(boolean shouldAddPriority) {

		if (priorityPanel == null || priorityPanel.getRootNode().getChildCount() == 0)
			return;

		/*
		 * this list adds columnNames in the order of how the scoreStatements of
		 * the nodes with respective columnNames are added to the scoreQuery
		 */
		columnNames = new ArrayList<>();
		scoreQuery = new StringBuffer("SELECT ");
		preferenceQuery = new StringBuffer("SELECT * FROM (" + query + ") AS T PREFERRING ");

		traverseTree((PreferenceNode) priorityPanel.getRootNode().getFirstChild(), shouldAddPriority);

		// deletes the last empty space and ',' character from the scoreQuery
		if (priorityPanel.getPreferenceNodes().size() > 0) {
			scoreQuery.deleteCharAt(scoreQuery.length() - 1);
			scoreQuery.deleteCharAt(scoreQuery.length() - 1);
			scoreQuery.append(" FROM (" + query + ") as T");
		} else {
			scoreQuery = new StringBuffer(query);
		}

	}

	/**
	 * Traverses tree recursively and if a preference node is found, it will add
	 * its score/preference statement to the score/preference query.
	 * 
	 * @param node
	 *            - node which will be checked if it is a preference node
	 * @param shouldAddPriority
	 *            - boolean to check if priorities should be computed or not
	 *            (Only needed to reduce computing time)
	 */
	private void traverseTree(PreferenceNode node, boolean shouldAddPriority) {

		// traverse children
		int childCount = node.getChildCount();
		if (childCount == 0) {
			// check if leaf is a Preference node an remove it from the list
			// recursion ends here
			if (!node.isParetoNode() && !node.isPriorityNode()) {

				if (shouldAddPriority)
					addPreferenceStatements(node);
				else {
					scoreQuery.append("" + node.createScoreStatement() + ", ");
					preferenceQuery.append(node.createPreferenceStatement());
				}

			}
		} else {

			preferenceQuery.append("(");
			for (int i = 0; i < childCount; i++) {
				PreferenceNode child = (PreferenceNode) node.getChildAt(i);
				traverseTree(child, shouldAddPriority);

				if (childCount - 1 != i) {
					if (node.isParetoNode())
						preferenceQuery.append(" AND ");
					else if (node.isPriorityNode())
						preferenceQuery.append(" PRIOR TO ");
				}
			}
			preferenceQuery.append(")");

		}
	}

	/**
	 * Adds priorities, which where computed by the computeIndexes method of the
	 * node, to a map. This map uses the column name of the node as key. Adds
	 * column name to a list.
	 * 
	 * @param node
	 *            - a preference node which column name for the score table will
	 *            be used as a key for the prrioritiesMap
	 */
	private void addPreferenceStatements(PreferenceNode node) {
		// add indexes to a map with the key as the score statement/ column name
		// of score table
		String columnName = node.getAlias();
		List<Integer> indexList = indexMap.get(columnName);

		if (indexList == null)
			return;

		StringBuffer indexes = new StringBuffer("");
		for (int i = 0; i < indexList.size(); i++) {
			indexes.append(indexList.get(i));
			if (indexList.size() - 1 != i)
				indexes.append(",");
		}
		columnNames.add(columnName);
	}

	/**
	 * 
	 * @return Returns all dimensions of the original data which have
	 *         preferences
	 */
	public String[] getPreferenceDimensions() {

		List<String> originalDimensions = new ArrayList<>(Arrays.asList(dimensions));
		List<String> dims = new ArrayList<String>();

		List<PreferenceNode> prefNodes = priorityPanel.getPreferenceNodes();

		for (int i = 0; i < prefNodes.size(); i++) {
			String dimension = prefNodes.get(i).getDimension();
			if (!dims.contains(dimension) && originalDimensions.contains(dimension))
				dims.add(dimension);
		}

		String[] dimensions = new String[dims.size()];

		dimensions = dims.toArray(dimensions);

		return dimensions;

	}

	private TreeMap<String, String> createFlowVariableOutput() throws InvalidSettingsException {

		int conjunctionNumber = 0;
		TreeMap<String, String> prefConjunctions = new TreeMap<>();

		if (priorityPanel == null)
			return prefConjunctions;
		PreferenceNode root = priorityPanel.getRootNode();
		if (root == null)
			return prefConjunctions;
		PreferenceNode firstChild = (PreferenceNode) root.getFirstChild();
		if (firstChild != null)
			traverseTree(firstChild, conjunctionNumber, prefConjunctions, "P"+conjunctionNumber,firstChild.getChildCount());

		return prefConjunctions;

	}

	private int traverseTree(PreferenceNode node, int conjunctionNumber,
			TreeMap<String, String> prefConjunctions, String prefName, int counter) throws InvalidSettingsException {

		StringBuffer conjunction = new StringBuffer("");
		if (node.isPriorityNode())
			conjunction.append("Priority");
		else if (node.isParetoNode())
			conjunction.append("Pareto");
		else
			return conjunctionNumber;

		int childCount = node.getChildCount();
		if (childCount <= 1)
				throw new InvalidSettingsException("A Priority or Pareto node needs to have at least two children!");

		if (counter < 3) {
			for (int i = 0; i < counter; i++) {
				PreferenceNode child = (PreferenceNode) node.getChildAt(i);

				if (child.isPriorityNode() || child.isParetoNode()) {
					String name = "P" + ++conjunctionNumber;
					conjunction.append(","+name);
					conjunctionNumber = traverseTree(child, conjunctionNumber,prefConjunctions,
							 name, child.getChildCount());
				} else
					conjunction.append("," + child.getAlias());
			}
		} else {
			
				String name = "P"+ ++conjunctionNumber;
				conjunction.append(","+name);
				PreferenceNode child = (PreferenceNode) node.getChildAt(counter-1);
				if (child.isPriorityNode() || child.isParetoNode()) {
					String name2 = "P"+ ++conjunctionNumber;
					conjunction.append(","+name2);
					conjunctionNumber = traverseTree(child, conjunctionNumber, prefConjunctions, name2, child.getChildCount());
				} else
					conjunction.append("," + child.getAlias());
				
				conjunctionNumber = traverseTree(node, conjunctionNumber, prefConjunctions, name, counter-1);
		}

		prefConjunctions.put(prefName, conjunction.toString());

		return conjunctionNumber;
	}
	
	

	public TreeMap<String, String> getPreferences() throws InvalidSettingsException {
		return createFlowVariableOutput();
	}

	/**
	 * Computes preference and scoreQuery and returns the preferenceQuery.
	 * 
	 * @return preferenceQuery - if the computation of the queries was
	 *         successful </br>
	 *         original query - if the computation of the queries failed
	 */
	public String getPreferenceQuery() {

		computeValues(false);

		if (preferenceQuery != null)
			return preferenceQuery.toString();
		else
			return query;

	}

	/**
	 * Computes preference and scoreQuery and returns the scoreQuery.
	 * 
	 * @return scoreQuery - if the computation of the queries was successful
	 *         </br>
	 *         original query - if the computation of the queries failed
	 * @throws InvalidSettingsException if the root node of the tree has no children
	 */
	public String getScoreQuery() throws InvalidSettingsException {
		
		if(priorityPanel.getRootNode().getChildCount()==0)
			throw new InvalidSettingsException("Tree has no children!");

		computeValues(false);

		if (scoreQuery != null)
			return scoreQuery.toString();
		else
			return query;

	}

	/**
	 * Just a method to test print things. Can be removed.
	 */
	public void print() {

		try {
			System.out.println(getScoreQuery());
			System.out.println(getPreferenceQuery());
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
