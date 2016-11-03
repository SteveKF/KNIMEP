package org.knime.preferences.bnl.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
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


public class BlockNestedLoopViewPanel extends JPanel {

	private static final long serialVersionUID = -3807907228824920209L;
	
	private List<BlockNestedLoopStructure> dominatedPoints;
	private List<BlockNestedLoopStructure> skyline;
	
	private boolean plot = false;
	
	private final int[] INDEXES_XY = new int[]{0,1};
	private final int[] INDEXES_XZ = new int[]{0,2};
	private final int[] INDEXES_YZ = new int[]{1,2};
	
	String[] dimensions;

	public enum DataType {
		DOMINATEDPOINT, SKYLINEPOINT
	};
	
	public BlockNestedLoopViewPanel(List<BlockNestedLoopStructure> dominatedPoints, List<BlockNestedLoopStructure> skyline, String[] dimensions) {
		
		super();
		
		this.dimensions = dimensions;
		int numColumns = dimensions.length;
		
		
		for(int i=0; i < dominatedPoints.size(); i++){
			List<DataCell> cellList = dominatedPoints.get(i).getRow();
			
			for(int j=0; j < cellList.size(); j++){
				
			}
		}
		
		if(numColumns==3){
			plot = true;
			setLayout(new GridLayout(2,2));
		}
		
		this.dominatedPoints = dominatedPoints;
		this.skyline = skyline;
		
		
		JFreeChart chart = createChart(INDEXES_XY);
		ChartPanel panel = new ChartPanel(chart);
		
		add(panel);
		
		if(plot){
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
	 * Creates a sample chart.
	 *
	 * @return A sample chart.
	 */
	private JFreeChart createChart(int[] indexes) {

		XYDataset dataset = createDataset("Dominated Points", DataType.DOMINATEDPOINT, indexes);
		
		JFreeChart chart = ChartFactory.createXYLineChart("BlockNestedLoop Graph", dimensions[indexes[0]], dimensions[indexes[1]], dataset,
				PlotOrientation.VERTICAL, true, true, false);;
		
		if(indexes == INDEXES_XY){
			
			chart = ChartFactory.createXYLineChart("BlockNestedLoop Graph", dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);
			
		}else if(indexes == INDEXES_XZ){

			chart = ChartFactory.createXYLineChart("BlockNestedLoop Graph", dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);
			
		}else if(indexes == INDEXES_YZ){
			

			chart = ChartFactory.createXYLineChart("BlockNestedLoop Graph", dimensions[indexes[0]], dimensions[indexes[1]], dataset,
					PlotOrientation.VERTICAL, true, true, false);
		}
		
		
		TextTitle subtitle = new TextTitle(
				"This chart shows the dominated and the skyline points in every dimension combination which was selected.");
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

		XYDataset dataset2 = createDataset("Skyline", DataType.SKYLINEPOINT, indexes);
		// renderer of skyline points
		XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, renderer2);
		renderer2.setSeriesShape(0, shape);
		renderer2.setSeriesPaint(0, Color.black);
		renderer2.setSeriesLinesVisible(0, false);

		return chart;
	}


	private XYDataset createDataset(String name, DataType type, int[] indexes) {

		XYSeries series = new XYSeries(name);
	
		
		switch (type) {
		case DOMINATEDPOINT:
			addSeriesPoint(series,dominatedPoints, indexes);
			break;

		case SKYLINEPOINT:
			addSeriesPoint(series,skyline, indexes);
			break;

		default:
			System.err.println("Wrong DataType was used for adding point to the DataSet");
			break;
		}

	XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);

	return dataset;
	}
	
	private void addSeriesPoint(XYSeries series, List<BlockNestedLoopStructure> list, int[] indexes){
		for(int i=0; i < list.size(); i++){
			double[] values = new double[3];
			for(int j=0; j < list.get(i).getRow().size(); j++){
				DataCell currCell = list.get(i).getRow().get(j);
				values[j] = ((DoubleValue) currCell).getDoubleValue();
			}
			series.add(values[indexes[0]], values[indexes[1]]);
		}
	}
}