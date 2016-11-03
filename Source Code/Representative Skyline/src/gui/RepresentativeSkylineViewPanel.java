package gui;

import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RepresentativeSkylineViewPanel extends JPanel {

	private String[] dimensions;

	private RepresentativeSkylineSize repSkySize;
	private RepresentativeSkylineWeight repSkyWeight;
	private RepresentativeSkylineThreshold repSkyThresh;

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

	public void restoreState(Map<String, Double> singleValues, Map<String, double[]> rangeValues,
			Map<String, String> options, int k, double diversityWeight, boolean useUpperBound) {

		repSkySize.setSizeOfRepresentativeSkyline(k);
		repSkyThresh.setSingleValues(singleValues);
		repSkyThresh.setRangeValues(rangeValues);
		repSkyThresh.setOptions(options);
		repSkyThresh.setUseUpperBound(useUpperBound);
		repSkyWeight.setDiversityWeight(diversityWeight);
		repSkyWeight.setSignificanceWeight(1-diversityWeight);

	}
	
	public static void main(String[] args) {

		RepresentativeSkylineViewPanel viewPanel = new RepresentativeSkylineViewPanel(
				new String[] { "Price", "Distance" });

		JFrame frame = new JFrame();
		frame.setContentPane(viewPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public String[] getDimensions() {

		return dimensions;

	}

	public int getSizeOfRepresentativeSkyline() {
		return repSkySize.getSizeOfRepresentativeSkyline();
	}

	public Map<String, Double> getSingleValues() {
		return repSkyThresh.getSingleValues();
	}

	public Map<String, double[]> getRangeValues() {
		return repSkyThresh.getRangeValues();
	}

	public Map<String, String> getOptions() {
		return repSkyThresh.getOptions();
	}

	public double getDiversityWeight() {
		return repSkyWeight.getDiversityWeight();
	}

	public double getSignificanceWeight() {
		return repSkyWeight.getSignificanceWeight();
	}
	
	public boolean isUsingUpperBound(){
		return repSkyThresh.isUsingUpperBound();
	}
}
