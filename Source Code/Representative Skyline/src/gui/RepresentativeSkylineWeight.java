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

@SuppressWarnings("serial")
public class RepresentativeSkylineWeight extends JPanel implements PropertyChangeListener {

	private double diversityWeight;
	private double significanceWeight;
	
	private JFormattedTextField diversityField;
	private JFormattedTextField significanceField;

	public RepresentativeSkylineWeight() {

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

	private void resizeSpinner(JSpinner spinner) {

		JComponent editor = spinner.getEditor();
		JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
		tf.setColumns(3);

	}

	private void addListeners(JSpinner diversitySpinner, JSpinner significanceSpinner) {

		JSpinner.DefaultEditor diversityEditor = (JSpinner.DefaultEditor) diversitySpinner.getEditor();
		diversityField = diversityEditor.getTextField();
		diversityField.addPropertyChangeListener(this);
		
		JSpinner.DefaultEditor significanceEditor = (JSpinner.DefaultEditor) significanceSpinner.getEditor();
		significanceField = significanceEditor.getTextField();
		significanceField.addPropertyChangeListener(this);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == diversityField) {

			diversityWeight = (double) diversityField.getValue();
			significanceWeight = 1 - diversityWeight;
			significanceField.setValue(significanceWeight);

		} else if (evt.getSource() == significanceField) {

			significanceWeight = (double) significanceField.getValue();
			diversityWeight = 1 - significanceWeight;
			diversityField.setValue(diversityWeight);

		}
	}
	
	public double getDiversityWeight(){
		return diversityWeight;
	}
	
	public double getSignificanceWeight(){
		return significanceWeight;
	}
	
	public void setDiversityWeight(double weight){
		
		diversityWeight = weight;
		diversityField.setValue(diversityWeight);
		
	}
	
	public void setSignificanceWeight(double weight){
		
		significanceWeight = weight;
		significanceField.setValue(significanceWeight);
		
	}

}
