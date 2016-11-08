package org.knime.preferences.maximizer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;

public class DataPoint {
	
	private double[] coords;
	private RowKey key;
	
	private List<DataPoint> dominatedPoints;
	
	private DataPoint(double[] coords,RowKey key){
		
		this.coords = coords;
		this.key = key;
		
		dominatedPoints = new ArrayList<>();
		
	}
	
	public static DataPoint createDataPoint(DataRow row) {
				
		double[] values = new double[row.getNumCells()];
		for (int i = 0; i < values.length; i++) {
			DataCell currCell = row.getCell(i);
			if(currCell.isMissing())
				values[i] = Double.MAX_VALUE;
			else
				values[i] = ((DoubleValue) currCell).getDoubleValue();
		}

		DataPoint p = new DataPoint(values,row.getKey());

		return p;
	}
	
	public List<DataPoint> getDominatedPoints(){
		return dominatedPoints;
	}
	
	public void addDominatedPoint(DataPoint p){
		if(!dominatedPoints.contains(p))
			dominatedPoints.add(p);
	}
		
	public double[] getCoordinates(){
		return coords;
	}
	
	public double getCoordinate(int index){
		return coords[index];
	}
				
	public RowKey getRowKey(){
		return key;
	}
}
