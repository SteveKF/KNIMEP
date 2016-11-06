package org.knime.preferences.prefCreator.gui;

import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**

 * Utility class that can be used to retrieve and/or restore the expansion state of a JTree. 

 * @author G. Cope

 *

 */

public class TreeUtil {



	private final JTree tree;
	private List<DefaultMutableTreeNode> layerList; 

	

	/**

	 * Constructs a new utility object based upon the parameter JTree

	 * @param tree

	 */

	public TreeUtil(JTree tree, List<DefaultMutableTreeNode> layerList){

		this.tree = tree;
		this.layerList = layerList;

	}

	

	/**

	 * Retrieves the expansion state as a String, defined by a comma delimited list of 

	 * each row node that is expanded.

	 * @return

	 */

	public String getExpansionState(){

		StringBuilder sb = new StringBuilder();

		for ( int i = 0; i < layerList.size(); i++ ){
			
			TreePath path = new TreePath(layerList.get(i).getPath());
			
			if ( tree.isExpanded(path) )				
				sb.append(layerList.get(i).toString()).append(",");
			
		}

		return sb.toString();

	}

	

	/**

	 * Sets the expansion state based upon a comma delimited list of row indexes that 

	 * are expanded. 

	 * @param s

	 */

	public void setExpansionState(String s){
		
		if(s==null || s.length() < 2)
			return;

		String[] layers = s.split(",");

		for ( String st : layers ){
			
			for(int j=0; j < layerList.size(); j++){
				if(layerList.get(j).toString().equals(st)){
					TreePath path = new TreePath(layerList.get(j).getPath());
					tree.expandPath(path);
				}
			}
			


		}

	}

}
