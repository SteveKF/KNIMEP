package org.knime.preferences.distance.algorithm;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;

/**
 * Class for saving values of a DataRow which grants easy access to these values
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataPoint {

	private double[] coords;
	private RowKey rowKey;

	/**
	 * Constructor for the DataPoint class.
	 * Initializes the coords and row variables.
	 * @param coords - the values of the data row which will be considered
	 * @param row - a DataRow
	 */
	private DataPoint(double[] coords, RowKey rowKey) {
		this.coords = coords;
		this.rowKey = rowKey;
	}

	/**
	 * 
	 * @return Returns all values of this DataPoint
	 */
	public double[] getCoordinates() {
		return coords;
	}
	
	public RowKey getRowKey(){
		return rowKey;
	}

	/**
	 * 
	 * @param index - an index
	 * @return Returns the value of this DataPoint at the specific index
	 */
	public double getCoordinateAt(int index) {
		assert(coords.length > index);
		return coords[index];
	}

	/**
	 * Creates a DataPoint which contains the values of the data row. The DataPoint only holds the values of the columns which 
	 * indexes are contained in the colIndexes. 
	 * @param row - a DataRow
	 * @param colIndexes - integer array with only indexes which should be considered
	 * @return Returns a DataPoint holding the column values of the DataRow which indexes are contained in the integer array 
	 */
	public static DataPoint createDataPoint(DataRow row) {
		
		double[] values = new double[row.getNumCells()];
		for(int i=0; i < row.getNumCells(); i++){
			DataCell currCell = row.getCell(i);
			if(currCell.isMissing())
				values[i] = Double.MAX_VALUE;
			else
				values[i] = ((DoubleValue) currCell).getDoubleValue();
		}
		
		DataPoint p = new DataPoint(values, row.getKey());

		return p;
	}
	
}