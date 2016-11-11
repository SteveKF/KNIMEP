package org.knime.repskyline;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;

/**
 * Class for saving values of a DataRow which grants easy access to these values
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataPoint {

	private DataRow row;
	private double[] coords;
	private double[] scoreCoords;

	/**
	 * Constructor for the DataPoint class.
	 * Initializes the coords and row variables.
	 * @param coords - the values of the data row which will be considered
	 * @param row - a DataRow
	 */
	private DataPoint(double[] scoreCoods, double[] coords, DataRow row) {
		this.row = row;
		this.coords = coords;
		this.scoreCoords = scoreCoods;
	}

	/**
	 * 
	 * @return Returns all values of this DataPoint
	 */
	public double[] getCoordinates() {
		return coords;
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
	 * 
	 * @return Returns all score values of this DataPoint
	 */
	public double[] getScoreCoordinates() {
		return scoreCoords;
	}

	/**
	 * 
	 * @param index - an index
	 * @return Returns the score value of this DataPoint at the specific index
	 */
	public double getScoreCoordinateAt(int index) {
		assert(scoreCoords.length > index);
		return scoreCoords[index];
	}

	/**
	 * 
	 * @return Returns the DataRow 
	 */
	public DataRow getDataRow() {
		return row;
	}
	
	/**
	 * Creates a DataPoint which contains the values of the data row. The DataPoint only holds the values of the columns which 
	 * indexes are contained in the colIndexes. 
	 * @param scoreRow - a DataRow which contains the scores of the data record
	 * @param row - a DataRow
	 * @param colIndexes - integer array with only indexes which should be considered
	 * @return Returns a DataPoint holding the column values of the DataRow which indexes are contained in the integer array 
	 */
	public static DataPoint createDataPoint(DataRow scoreRow, DataRow row, int[] colIndexes) {
		
		double[] values = new double[colIndexes.length];
		for (int i = 0; i < colIndexes.length; i++) {
			DataCell currCell = row.getCell(colIndexes[i]);
			values[i] = ((DoubleValue) currCell).getDoubleValue();
		}
		
		double[] scoreValues = new double[scoreRow.getNumCells()];
		for(int i = 0; i < colIndexes.length; i++){
			DataCell currCell = row.getCell(colIndexes[i]);
			scoreValues[i] = ((DoubleValue) currCell).getDoubleValue();
		}
		

		DataPoint p = new DataPoint(scoreValues, values, row);

		return p;
	}
	
}