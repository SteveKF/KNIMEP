package org.knime.preferences.maximizer;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;

public class DataPoint {
	
	private double[] coords;
	private RowKey key;
	
	private boolean isDominated = false;
	private int numDominations = 0;
	
	private DataPoint(double[] coords,RowKey key){
		
		this.coords = coords;
		this.key = key;
		
	}
	
	public static DataPoint createDataPoint(DataRow row) {
		
		double[] values = new double[row.getNumCells()];
		for (int i = 0; i < values.length; i++) {
			DataCell currCell = row.getCell(i);
			values[i] = ((DoubleValue) currCell).getDoubleValue();
		}

		DataPoint p = new DataPoint(values,row.getKey());

		return p;
	}
		
	public double[] getCoordinates(){
		return coords;
	}
	
	public double getCoordinate(int index){
		return coords[index];
	}
				
	public boolean isDominated(){
		
		return isDominated;
		
	}
	
	public void setDominated(boolean isDominated){
		this.isDominated = isDominated;
	}
	
	public int getNumDominations(){
		
		return numDominations;
		
	}
	
	public void increaseNumDominations(){
		numDominations++;
	}
	
	public RowKey getRowKey(){
		return key;
	}
}
