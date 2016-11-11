package org.knime.preferences.bnl.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.knime.core.data.DataCell;
import org.knime.core.data.DoubleValue;

/**
 * Class which creates the JPanel with the Graphs.
 * 
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class BlockNestedLoopViewPanel extends JPanel {


	private List<BlockNestedLoopStructure> dominatedPoints;
	private List<BlockNestedLoopStructure> undominatedPoints;

	private boolean plot = false;

	private final int[] INDEXES_XY = new int[] { 0, 1 };
	private final int[] INDEXES_XZ = new int[] { 0, 2 };
	private final int[] INDEXES_YZ = new int[] { 1, 2 };

	private String[] dimensions;

	private String chartname;
	private String subtitleText;
	private String dominatedName;
	private String undominatedName;

	public enum DataType {
		ALL, SKYLINE
	};

	/**
	 * Constructor which chooses chart name, subtitle, etc. according to the
	 * option
	 * 
	 * @param dominatedPoints-
	 *            list of dominated points (can be also dominated + undominated
	 *            points)
	 * @param undominatedPoints
	 *            - list of undomianted points
	 * @param dimensions
	 *            - string array with dimensions for labeling the axes
	 * @param option
	 *            - option to choose which chart name, subtitle, etc. to use
	 * @param chartName
	 *            - custom chart name if "Custom" is the option
	 * @param subtitleText
	 *            - custom subtitle text if "Custom" is the option
	 * @param dominatedName
	 *            - custom name for domianted points if "Custom" is the option
	 * @param undominatedName
	 *            - custom name for undominated points if "Custom" is the option
	 */
	public BlockNestedLoopViewPanel(List<BlockNestedLoopStructure> dominatedPoints,
			List<BlockNestedLoopStructure> undominatedPoints, String[] dimensions) {

		super();

		this.dimensions = dimensions;
		this.dominatedPoints = dominatedPoints;
		this.undominatedPoints = undominatedPoints;

		this.chartname = "Skyline Graph";
		this.subtitleText = "This chart shows the dominated and the skyline points in every dimension combination which was selected.";
		this.dominatedName = "Dominated Points";
		this.undominatedName = "Skyline";

		if ((dominatedPoints.size() > 0 && !dominatedPoints.get(0).isLoadedData())
				|| (undominatedPoints.size() > 0 && !undominatedPoints.get(0).isLoadedData())) {
			List<BlockNestedLoopStructure> removableStructures = new ArrayList<>();
			for (int i = 0; i < dominatedPoints.size(); i++) {
				for (int j = 0; j < undominatedPoints.size(); j++) {
					if (dominatedPoints.get(i).getRowKey().equals(undominatedPoints.get(j).getRowKey())) {
						removableStructures.add(dominatedPoints.get(i));
						break;
					}
				}
			}
			for (BlockNestedLoopStructure struct : removableStructures) {
				if (dominatedPoints.contains(struct))
					dominatedPoints.remove(struct);
			}
		}

		if (dimensions.length == 3) {
			plot = true;
			setLayout(new GridLayout(2, 2));
		}

		JFreeChart chart = createChart(INDEXES_XY);
		ChartPanel panel = new ChartPanel(chart);

		add(panel);

		if (plot) {
			JFreeChart chart2 = createChart(INDEXES_XZ);
			ChartPanel panel2 = new ChartPanel(chart2);

			JFreeChart chart3 = createChart(INDEXES_YZ);
			ChartPanel panel3 = new ChartPanel(chart3);

			add(panel2);
			add(panel3);
		}

		setSize(panel.getPreferredSize());
	}

	/**
	 * Creates a chart with settings and the chart name, subtitles, etc.
	 *
	 * @return A chart
	 */
	private JFreeChart createChart(int[] indexes) {

		XYDataset dataset = createDataset(dominatedName, DataType.ALL, indexes);

		JFreeChart chart = ChartFactory.createXYLineChart(chartname, dimensions[indexes[0]], dimensions[indexes[1]],
				dataset, PlotOrientation.VERTICAL, true, true, false);

		if (indexes == INDEXES_XY) {

			chart = ChartFactory.createXYLineChart(chartname, dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);

		} else if (indexes == INDEXES_XZ) {

			chart = ChartFactory.createXYLineChart(chartname, dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);

		} else if (indexes == INDEXES_YZ) {

			chart = ChartFactory.createXYLineChart(chartname, dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);
		}

		TextTitle subtitle = new TextTitle(subtitleText);
		subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));
		chart.addSubtitle(subtitle);

		ChartUtilities.applyCurrentTheme(chart);

		XYPlot plot = (XYPlot) chart.getPlot();

		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setAutoRangeIncludesZero(false);

		Shape shape = new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);

		// renderer of non-skyline points
		XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer1.setBaseShapesVisible(true);
		renderer1.setSeriesShape(0, shape);
		renderer1.setSeriesFillPaint(0, Color.white);
		renderer1.setSeriesOutlinePaint(0, Color.black);
		renderer1.setUseFillPaint(true);
		renderer1.setUseOutlinePaint(true);
		renderer1.setSeriesLinesVisible(0, false);

		XYDataset dataset2 = createDataset(undominatedName, DataType.SKYLINE, indexes);
		// renderer of skyline points
		XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, renderer2);
		renderer2.setSeriesShape(0, shape);
		renderer2.setSeriesPaint(0, Color.black);
		renderer2.setSeriesLinesVisible(0, false);

		return chart;
	}

	/**
	 * Creates a data set.
	 * 
	 * @param name
	 *            - name of the series
	 * @param type
	 *            - data type (DOMINATEDPOINT or UNDOMINATEDPOINT)
	 * @param indexes
	 *            - column indexes which will be considered
	 * @return
	 */
	private XYDataset createDataset(String name, DataType type, int[] indexes) {

		XYSeries series = new XYSeries(name);

		switch (type) {
		case ALL:
			addSeriesPoint(series, dominatedPoints, indexes);
			break;

		case SKYLINE:
			addSeriesPoint(series, undominatedPoints, indexes);
			break;

		default:
			System.err.println("Wrong DataType was used for adding point to the DataSet");
			break;
		}

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);

		return dataset;
	}

	/**
	 * Adds points to the series.
	 * 
	 * @param series
	 *            - the series
	 * @param list
	 *            - the data which will be added to the series
	 * @param indexes
	 *            - the column indexes which are being considered
	 */
	private void addSeriesPoint(XYSeries series, List<BlockNestedLoopStructure> list, int[] indexes) {
		for (int i = 0; i < list.size(); i++) {
			double[] values = new double[3];
			for (int j = 0; j < list.get(i).getRow().size(); j++) {
				DataCell currCell = list.get(i).getRow().get(j);
				values[j] = ((DoubleValue) currCell).getDoubleValue();
			}
			series.add(values[indexes[0]], values[indexes[1]]);
		}
	}
}