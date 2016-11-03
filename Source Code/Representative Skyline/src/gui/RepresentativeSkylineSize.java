package gui;

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

@SuppressWarnings("serial")
public class RepresentativeSkylineSize extends JPanel implements PropertyChangeListener{
	
	private final int MAXIMUM = 10000;
	private int k;

	private JFormattedTextField sizeField;


	public RepresentativeSkylineSize() {

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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if(evt.getSource() == sizeField){
			k = (int) sizeField.getValue();
		}
	}
	
	public int getSizeOfRepresentativeSkyline(){
		return k;
	}
	
	public void setSizeOfRepresentativeSkyline(int k){
		this.k = k;
		sizeField.setValue(k);
	}
	
	

}
