package org.knime.preferences.maximizer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;

/**
 * Class which represents a DataRow and has collections and methods for computing the (representative) skyline
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataPoint {
	
	private double[] coords;
	private RowKey key;
	
	private List<DataPoint> dominatedPoints;
	
	/**
	 * Constructor which saves the values of a data row and its key
	 * @param coords - double array which represents the values of a data row
	 * @param key - RowKey of a data row
	 */
	private DataPoint(double[] coords,RowKey key){
		
		this.coords = coords;
		this.key = key;
		
		dominatedPoints = new ArrayList<>();
		
	}
	
	/**
	 * Creates a Data Point based on the inputed data row.
	 * @param row - a DataRow
	 * @return - a DataPoint based on the DataRow 
	 */
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
	
	/**
	 * 
	 * @return Returns all points which are dominated by this point as a list
	 */
	public List<DataPoint> getDominatedPoints(){
		return dominatedPoints;
	}
	
	/**
	 * Adds p to a list of dominated points
	 * @param p - a DataPoint
	 */
	public void addDominatedPoint(DataPoint p){
		if(!dominatedPoints.contains(p))
			dominatedPoints.add(p);
	}
		
	/**
	 * 
	 * @return Returns all values of the DataPoint
	 */
	public double[] getCoordinates(){
		return coords;
	}
	
	/**
	 * 
	 * @param index - a index of the values
	 * @return Returns the value of the DataPoint at the specific index
	 */
	public double getCoordinate(int index){
		assert(coords.length > index);
		return coords[index];
	}
				
	/**
	 * 
	 * @return Returns the RowKey of this DataPoint
	 */
	public RowKey getRowKey(){
		return key;
	}
}
