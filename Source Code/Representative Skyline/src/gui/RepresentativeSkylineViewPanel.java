package gui;

import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Class in which the user can enter size of the representative skyline, diversity/significance weights and thresholds for every dimension.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class RepresentativeSkylineViewPanel extends JPanel {

	private String[] dimensions;

	private RepresentativeSkylineSize repSkySize;
	private RepresentativeSkylineWeight repSkyWeight;
	private RepresentativeSkylineThreshold repSkyThresh;

	/**
	 * Constructor of the RepresentativeSkylineViewPanel class.
	 * Creates a RepresentativeSkylineSize, RepresentativeSkylineWeight and a RepresentativeSkylineThreshold and adds them to this JPanel.
	 * @param dimensions
	 */
	public RepresentativeSkylineViewPanel(String[] dimensions) {

		this.dimensions = dimensions;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		repSkySize = new RepresentativeSkylineSize();
		repSkyWeight = new RepresentativeSkylineWeight();
		repSkyThresh = new RepresentativeSkylineThreshold(dimensions);

		add(repSkySize);
		add(Box.createVerticalStrut(10));
		add(repSkyWeight);
		add(Box.createVerticalStrut(10));
		add(repSkyThresh);

	}

	/**
	 * Restores the values and the saved maps for this JPanel
	 * @param singleValues - a map with single threshold as values
	 * @param rangeValues - a map with range threshold as values
	 * @param options - a map with options which threshold was selected as values
	 * @param isUpperBound - a map which say if the threshold was used as upper bound or not
	 * @param k - size of the representative skyline
	 * @param diversityWeight - weight of the diversity 
	 */
	public void restoreState(Map<String, Double> singleValues, Map<String, double[]> rangeValues,
			Map<String, String> options, Map<String,Boolean> isUpperBound,
			int k, double diversityWeight) {

		repSkySize.restoreSizeOfRepresentativeSkyline(k);
		repSkyThresh.restoreSingleValues(singleValues);
		repSkyThresh.restoreRangeValues(rangeValues);
		repSkyThresh.restoreOptions(options);
		repSkyThresh.restoreUpperBounds(isUpperBound);
		repSkyWeight.restoreDiversityWeight(diversityWeight);
		repSkyWeight.restoreSignificanceWeight(1-diversityWeight);

	}

	/**
	 * 
	 * @return Returns the dimensions
	 */
	public String[] getDimensions() {

		return dimensions;

	}

	/**
	 * 
	 * @return Returns the size of the representative skyline
	 */
	public int getSizeOfRepresentativeSkyline() {
		return repSkySize.getSizeOfRepresentativeSkyline();
	}

	/**
	 * 
	 * @return Returns the single thresholds as a map
	 */
	public Map<String, Double> getSingleValues() {
		return repSkyThresh.getSingleValues();
	}

	/**
	 * 
	 * @return Returns the range thresholds as a map
	 */
	public Map<String, double[]> getRangeValues() {
		return repSkyThresh.getRangeValues();
	}

	/**
	 * 
	 * @return Returns the threshold options (SINGLE, RANGE, NONE) as a map
	 */
	public Map<String, String> getOptions() {
		return repSkyThresh.getOptions();
	}
	
	/**
	 * 
	 * @return Returns the upper bounds (if a single threshold is used as an upper bound) as a map
	 */
	public Map<String,Boolean> getUpperBounds(){
		return repSkyThresh.getUpperBounds();
	}

	/**
	 * 
	 * @return Returns the diversity weight (lambda)
	 */
	public double getDiversityWeight() {
		return repSkyWeight.getDiversityWeight();
	}

	/**
	 * 
	 * @return Returns the significance weight (1-lambda)
	 */
	public double getSignificanceWeight() {
		return repSkyWeight.getSignificanceWeight();
	}
	
}
