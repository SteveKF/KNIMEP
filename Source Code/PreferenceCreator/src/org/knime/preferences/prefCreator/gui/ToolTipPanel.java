package org.knime.preferences.prefCreator.gui;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * JPanel with a JLabel which displays a tooltip text for the user.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class ToolTipPanel extends JPanel{
	
	//JLabel which displays some tooltip
	private JLabel tooltipLabel;
	
	/**
	 * Constructor which will add a JLabel with a tooltip text for the user to this JPanel.
	 */
	protected ToolTipPanel(){
		tooltipLabel = new JLabel("<html>Start with creating a Preference or Pareto node.<br></html>");
		add(tooltipLabel);
	}
	
	/**
	 * Sets a text for the JLabel of this class.
	 * @param text - a string which replaces the text of the JLabel of this class
	 */
	public void setText(String text){
		tooltipLabel.setText("<html>"+text+"<br></html>");
	}
	
}
