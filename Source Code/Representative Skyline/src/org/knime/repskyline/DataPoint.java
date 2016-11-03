package org.knime.repskyline;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;

public class DataPoint {

	private DataRow row;
	private double[] coords;

	private DataPoint(double[] coords, DataRow row) {
		this.row = row;
		this.coords = coords;
	}

	public double[] getCoordinates() {
		return coords;
	}

	public double getCoordinateAt(int index) {
		return coords[index];
	}

	public DataRow getDataRow() {
		return row;
	}
	
	public static DataPoint createDataPoint(DataRow row, int[] colIndexes) {
		
		double[] values = new double[colIndexes.length];
		
		for (int i = 0; i < colIndexes.length; i++) {
			DataCell currCell = row.getCell(colIndexes[i]);
			values[i] = ((DoubleValue) currCell).getDoubleValue();
		}

		DataPoint p = new DataPoint(values, row);

		return p;
	}
	
}