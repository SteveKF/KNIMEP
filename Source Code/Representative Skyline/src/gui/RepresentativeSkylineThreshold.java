package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class RepresentativeSkylineThreshold extends JPanel implements ActionListener, PropertyChangeListener {

	// GUI components
	private JComboBox<String> dimensionBox;
	private JRadioButton single_jrb;
	private JRadioButton range_jrb;
	private JRadioButton none_jrb;
	private JFormattedTextField single;
	private JFormattedTextField minRange;
	private JFormattedTextField maxRange;
	private JCheckBox upperBoundCheckBox;

	// String constants for threshold options
	public static final String SINGLE = "Single";
	public static final String RANGE = "Range";
	public static final String NONE = "None";

	private Map<String, Double> singleValues;
	private Map<String, double[]> rangeValues;
	private Map<String, String> options;

	private String[] dimensions;
	private String prevDimension;

	public RepresentativeSkylineThreshold(String[] dimensions) {

		this.dimensions = dimensions;

		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		
		gc.gridwidth = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.insets = new Insets(5, 5, 5, 5);

		// DIMENSION LABEL + BOX
		// dimension label
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		JLabel boxLabel = new JLabel("Dimensions:");
		add(boxLabel, gc);

		// dimension box
		gc.gridx = 1;
		gc.gridy = 0;
		dimensionBox = new JComboBox<>(dimensions);
		add(dimensionBox, gc);

		// THRESHOLD LABEL + OPTIONS
		// threshold label
		gc.gridx = 0;
		gc.gridy = 1;
		JLabel optionLabel = new JLabel("Threshold:");
		add(optionLabel, gc);

		// threshold options -> JRadioButtons
		gc.gridx = 1;
		gc.gridy = 1;
		single_jrb = new JRadioButton(SINGLE);
		range_jrb = new JRadioButton(RANGE);
		none_jrb = new JRadioButton(NONE);
		Box optionBox = Box.createHorizontalBox();
		optionBox.add(single_jrb);
		optionBox.add(Box.createHorizontalStrut(15));
		optionBox.add(range_jrb);
		optionBox.add(Box.createHorizontalStrut(15));
		optionBox.add(none_jrb);
		add(optionBox, gc);


		// SINGLE LABEL + FIELD
		// single label
		gc.gridx = 0;
		gc.gridy = 2;
		JLabel singleLabel = new JLabel("Single:");
		add(singleLabel, gc);

		// single text field
		gc.gridx = 1;
		gc.gridy = 2;
		single = new JFormattedTextField(new Double(0.0));
		single.setColumns(7);
		add(single, gc);
		
		gc.gridx = 1;
		gc.gridy = 2;
		gc.anchor = GridBagConstraints.CENTER;
		upperBoundCheckBox = new JCheckBox("Upper Bound");
		upperBoundCheckBox.setToolTipText("Use the Single threshold as upper bound instead of lower bound.");
		add(upperBoundCheckBox,gc);
		gc.anchor = GridBagConstraints.LINE_START;
		
		

		// RANGE LABEL + FIELDS
		// range label
		gc.gridx = 0;
		gc.gridy = 3;
		JLabel rangeLabel = new JLabel("Range:");
		add(rangeLabel, gc);

		// range text field
		gc.gridx = 1;
		gc.gridy = 3;
		gc.anchor = GridBagConstraints.LINE_START;
		JLabel minLabel = new JLabel("Min: ");
		JLabel maxLabel = new JLabel(" Max: ");
		minRange = new JFormattedTextField(new Double(0.0));
		minRange.setColumns(10);
		add(minRange, gc);
		maxRange = new JFormattedTextField(new Double(0.0));
		maxRange.setColumns(10);
		add(maxRange, gc);

		// add labels + fields to box
		Box rangeBox = Box.createHorizontalBox();
		rangeBox.add(minLabel);
		rangeBox.add(minRange);
		rangeBox.add(maxLabel);
		rangeBox.add(maxRange);
		add(rangeBox, gc);

		setBorder(BorderFactory.createTitledBorder("Threshold"));

		initialize();
	}

	private void initialize() {

		options = new HashMap<>();
		singleValues = new HashMap<>();
		rangeValues = new HashMap<>();

		// adds listeners to gui components who use them
		addListeners();

		// set previous dimension
		prevDimension = (String) dimensionBox.getSelectedItem();

		double[] values = new double[] { 0.0, 0.0 };
		for (int i = 0; i < dimensions.length; i++) {
			singleValues.put(dimensions[i], values[0]);
			rangeValues.put(dimensions[i], values);
			options.put(dimensions[i], NONE);
		}

		setOption(NONE);

	}

	private void addListeners() {
		dimensionBox.addActionListener(this);
		single_jrb.addActionListener(this);
		range_jrb.addActionListener(this);
		none_jrb.addActionListener(this);
		single.addPropertyChangeListener(this);
		minRange.addPropertyChangeListener(this);
		maxRange.addPropertyChangeListener(this);
	}

	private void removeListeners() {
		dimensionBox.removeActionListener(this);
		single_jrb.removeActionListener(this);
		range_jrb.removeActionListener(this);
		none_jrb.removeActionListener(this);
		single.removePropertyChangeListener(this);
		minRange.removePropertyChangeListener(this);
		maxRange.removePropertyChangeListener(this);
	}

	private void saveThreshold(String dimension) {
		
		String option = options.get(dimension);

		saveOption(dimension, option);
		saveValues(dimension);

	}

	private void loadThreshold(String dimension) {
		
		String option = options.get(dimension);
		setOption(option);

		setValues(dimension);

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == single || evt.getSource() == minRange || evt.getSource() == maxRange) {

			String dimension = (String) dimensionBox.getSelectedItem();
			saveValues(dimension);

		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String dimension = (String) dimensionBox.getSelectedItem();

		if (e.getSource() == dimensionBox) {

			removeListeners();

			saveThreshold(prevDimension);
			loadThreshold(dimension);

			prevDimension = dimension;

			addListeners();

		} else if (e.getSource() == single_jrb) {

			setOption(SINGLE);
			saveOption(dimension, SINGLE);

		} else if (e.getSource() == range_jrb) {

			setOption(RANGE);
			saveOption(dimension, RANGE);

		} else if (e.getSource() == none_jrb) {

			setOption(NONE);
			saveOption(dimension, NONE);
		}
	}

	private void setValues(String dimension) {

		double singleVal = singleValues.get(dimension);
		double[] rangeVals = rangeValues.get(dimension);

		single.setValue(singleVal);
		minRange.setValue(rangeVals[0]);
		maxRange.setValue(rangeVals[1]);

	}

	private void saveValues(String dimension) {

		if (!singleValues.containsKey(dimension) || !rangeValues.containsKey(dimension)) {
			return;
		}

		double singleVal = 0.0;
		singleVal = (double) single.getValue();
		singleValues.replace(dimension, singleVal);

		double[] rangeVals = new double[2];
		rangeVals[0] = (double) minRange.getValue();
		rangeVals[1] = (double) maxRange.getValue();
		rangeValues.replace(dimension, rangeVals);

	}

	private void setOption(String option) {

		if (option.equals(SINGLE)) {

			single_jrb.setSelected(true);
			range_jrb.setSelected(false);
			none_jrb.setSelected(false);

			single.setEditable(true);
			minRange.setEditable(false);
			maxRange.setEditable(false);

		} else if (option.equals(RANGE)) {

			single_jrb.setSelected(false);
			range_jrb.setSelected(true);
			none_jrb.setSelected(false);

			single.setEditable(false);
			minRange.setEditable(true);
			maxRange.setEditable(true);

		} else if (option.equals(NONE)) {

			single_jrb.setSelected(false);
			range_jrb.setSelected(false);
			none_jrb.setSelected(true);

			single.setEditable(false);
			minRange.setEditable(false);
			maxRange.setEditable(false);

		}
	}

	private void saveOption(String dimension, String option) {

		if (options.containsKey(dimension)) {
			options.replace(dimension, option);
		}
	}

	public Map<String, Double> getSingleValues() {
		return singleValues;
	}

	public Map<String, double[]> getRangeValues() {
		return rangeValues;
	}
	
	public boolean isUsingUpperBound(){
		if(upperBoundCheckBox.isSelected())
			return true;
		else 
			return false;
	}
	
	public void setUseUpperBound(boolean useUpperBound){
		if(useUpperBound)
			upperBoundCheckBox.setSelected(true);
		else
			upperBoundCheckBox.setSelected(false);
			
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setSingleValues(Map<String, Double> singleValues) {
		String dimension = (String) dimensionBox.getSelectedItem();
		this.singleValues = singleValues;
		setValues(dimension);
	}

	public void setRangeValues(Map<String, double[]> rangeValues) {
		String dimension = (String) dimensionBox.getSelectedItem();
		this.rangeValues = rangeValues;
		setValues(dimension);
	}

	public void setOptions(Map<String, String> options) {

		String dimension = (String) dimensionBox.getSelectedItem();
		this.options = options;
		String option = options.get(dimension);
		setOption(option);

	}
}
