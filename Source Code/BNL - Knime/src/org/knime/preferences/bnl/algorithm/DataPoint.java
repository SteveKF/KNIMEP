package org.knime.preferences.bnl.algorithm;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;

/**
 * Class to store cell values and the key of a BufferedDataTable DataRow.
 * Stores also a timestamp for the BlockNestedLoop class.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataPoint {

	private final double[] coords;
	private long timestamp;
	private RowKey key;

	/*Constructor which needs the cell values and the key of a DataRow.
	 * Plus a timestamps which is used for the BlockNestedLoop algorithm
	 */
	private DataPoint(double[] coords, int timestamp, RowKey key) {

		this.coords = coords;
		this.timestamp = timestamp;
		this.key = key;
		
	}

	/**
	 * Returns the RowKey of the DataRow which this class should represent 
	 * @return RowKey of the DataRow this class is based on
	 */
	public RowKey getRowKey() {
		return key;
	}

	/**
	 * Returns the cell values of the original DataRow
	 * @return a double array which represents the cell values of the original DataRow
	 */
	public double[] getCoordinates() {
		return coords;
	}
	
	/**
	 * 
	 * @param index - an integer value
	 * @return Returns the double value from the coordinates at the index 
	 */
	public double getCoordinate(int index) {
		assert(index < coords.length);
		return coords[index];
	}
 
	/**
	 * Returns the timestamp value of this class. This timestamp value represents the time when a DataPoint 
	 * was inserted into the window or temporary file of the BlockNestedLoop class.
	 * @return the value of the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp for this class. This timestamp value represents the time when a DataPoint was inserted 
	 * into the window or temporary file of the BlockNestedLoop class.
	 * @param timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Creates an instance of a DataPoint with the values of a DataRow
	 * @param row - a DataRow 
	 * @return a DataPoint instance which will be created with the cell values and the key of the DataRow.
	 * 			The timestamp will be set to 0 at creation of this instance.
	 */
	public static DataPoint createDataPoint(DataRow row) {
		
		double[] values = new double[row.getNumCells()];
		for (int i = 0; i < values.length; i++) {
			DataCell currCell = row.getCell(i);
			if(currCell.isMissing())
				values[i] = 0;
			else
				values[i] = ((DoubleValue) currCell).getDoubleValue();
		}

		DataPoint p = new DataPoint(values, 0 ,row.getKey());

		return p;
	}

}



