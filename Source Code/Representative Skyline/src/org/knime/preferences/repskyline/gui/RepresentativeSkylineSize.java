package org.knime.preferences.repskyline.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * This class allows the user to change the output size of the representative skyline
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class RepresentativeSkylineSize extends JPanel implements PropertyChangeListener{
	
	private final int MAXIMUM = 10000;
	private int k;

	private JFormattedTextField sizeField;


	/**
	 * Constructor for the RepresentativeSkylineSize which adds a JSpinner for output size of the representative skyline to this JPanel
	 */
	protected RepresentativeSkylineSize() {

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.anchor = GridBagConstraints.LINE_START;
		
		// size label
		JLabel sizeLabel = new JLabel("k:");
		add(sizeLabel,gc);

		//field for k
		SpinnerModel sizeModel = new SpinnerNumberModel(1, 1, MAXIMUM, 1);
		JSpinner sizeSpinner = new JSpinner(sizeModel);
		
		JSpinner.DefaultEditor sizeEditor = (JSpinner.DefaultEditor) sizeSpinner.getEditor();
		sizeField = sizeEditor.getTextField();
		sizeField.addPropertyChangeListener(this);
		k = (int) sizeField.getValue();
		gc.anchor = GridBagConstraints.CENTER;
		add(sizeSpinner,gc);
		setBorder(BorderFactory.createTitledBorder("Size of representative Skyline"));
	
		gc.gridx = 1;
		add(Box.createHorizontalStrut(1),gc);
		gc.gridx = 2;
		add(Box.createHorizontalStrut(1),gc);
		
	}

	/**
	 * If the value of the JSpinner for the size of the representative skyline is changed change the according variable 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if(evt.getSource() == sizeField){
			k = (int) sizeField.getValue();
		}
	}
	
	/**
	 * 
	 * @return Returns the size of the representative skyline
	 */
	public int getSizeOfRepresentativeSkyline(){
		return k;
	}
	
	/**
	 * Restores the size of the representative skyline from a old loaded state
	 * @param k - output size of the representative skyline
	 */
	public void restoreSizeOfRepresentativeSkyline(int k){
		this.k = k;
		sizeField.setValue(k);
	}
	
	

}
