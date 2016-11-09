package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * This JPanel allows the user to change the diversity and significance weight and saves those values.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class RepresentativeSkylineWeight extends JPanel implements PropertyChangeListener {

	private double diversityWeight;
	private double significanceWeight;
	
	private JFormattedTextField diversityField;
	private JFormattedTextField significanceField;

	/**
	 * Constructor of the RepresentativeSkylineWeight which adds two JSpinner 
	 * for the diversity and the significance weight to this JPanel
	 */
	protected RepresentativeSkylineWeight() {

		setLayout(new GridBagLayout());

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.anchor = GridBagConstraints.LINE_START;
		
		// diversity label
		JLabel diversityLabel = new JLabel("Diversity:");
		add(diversityLabel, gc);

		// diversity field
		SpinnerModel diversityModel = new SpinnerNumberModel(0.5, 0, 1.0, 0.1);
		JSpinner diversitySpinner = new JSpinner(diversityModel);
		diversityWeight = (double) diversitySpinner.getValue();
		resizeSpinner(diversitySpinner);
		diversitySpinner.addPropertyChangeListener(this);
		gc.anchor = GridBagConstraints.LINE_END;
		add(diversitySpinner, gc);
		
		//empty space for right alignment
		gc.gridx = 1;
		add(Box.createHorizontalStrut(1),gc);
		gc.gridx = 2;
		add(Box.createHorizontalStrut(1),gc);

		// significance label
		gc.anchor = GridBagConstraints.LINE_START;
		gc.gridx = 0;
		gc.gridy = 1;
		
		JLabel significanceLabel = new JLabel("Significance:");
		add(significanceLabel, gc);

		// significance field
		SpinnerModel significanceModel = new SpinnerNumberModel(0.5, 0, 1.0, 0.1);
		JSpinner significanceSpinner = new JSpinner(significanceModel);
		significanceWeight = (double) significanceSpinner.getValue();
		resizeSpinner(significanceSpinner);
		significanceSpinner.addPropertyChangeListener(this);
		gc.anchor = GridBagConstraints.LINE_END;
		add(significanceSpinner, gc);
		
		//empty space for right alignment
		gc.gridx = 1;
		add(Box.createHorizontalStrut(1),gc);
		gc.gridx = 2;
		add(Box.createHorizontalStrut(1),gc);
		
		setBorder(BorderFactory.createTitledBorder("Weights"));

		addListeners(diversitySpinner,significanceSpinner);
	}

	/**
	 * Resizes the entered JSpinner
	 * @param spinner - The JSpinner which should be resized
	 */
	private void resizeSpinner(JSpinner spinner) {

		JComponent editor = spinner.getEditor();
		JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
		tf.setColumns(3);

	}

	/**
	 * Adds a PropertyChangeListener to both spinners
	 * @param diversitySpinner - The Spinner for the diversity weight
	 * @param significanceSpinner - The Spinner for the significance weight
	 */
	private void addListeners(JSpinner diversitySpinner, JSpinner significanceSpinner) {

		JSpinner.DefaultEditor diversityEditor = (JSpinner.DefaultEditor) diversitySpinner.getEditor();
		diversityField = diversityEditor.getTextField();
		diversityField.addPropertyChangeListener(this);
		
		JSpinner.DefaultEditor significanceEditor = (JSpinner.DefaultEditor) significanceSpinner.getEditor();
		significanceField = significanceEditor.getTextField();
		significanceField.addPropertyChangeListener(this);

	}

	/**
	 * If the diversity weight value gets changed the method changes the significance weight 
	 * so that both value sum up to 1 and vice versa
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == diversityField) {
			//changes both weights to the current entered values
			diversityWeight = (double) diversityField.getValue();
			significanceWeight = 1 - diversityWeight;
			significanceField.setValue(significanceWeight);

		} else if (evt.getSource() == significanceField) {
			//changes both weights to the current entered values
			significanceWeight = (double) significanceField.getValue();
			diversityWeight = 1 - significanceWeight;
			diversityField.setValue(diversityWeight);

		}
	}
	
	/**
	 * 
	 * @return Returns the diversity weight
	 */
	public double getDiversityWeight(){
		return diversityWeight;
	}
	
	/**
	 * 
	 * @return Returns the significance weight
	 */
	public double getSignificanceWeight(){
		return significanceWeight;
	}
	
	/**
	 * Restores the diversity weight from a loaded state
	 * @param weight - a double which will be the new diversity weight
	 */
	public void restoreDiversityWeight(double weight){
		
		diversityWeight = weight;
		diversityField.setValue(diversityWeight);
		
	}
	
	/**
	 * Restores the significance weight from a loaded state
	 * @param weight - a double which will be the new diversity weight
	 */
	public void restoreSignificanceWeight(double weight){
		
		significanceWeight = weight;
		significanceField.setValue(significanceWeight);
		
	}

}
