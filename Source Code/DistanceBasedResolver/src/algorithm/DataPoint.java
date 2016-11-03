package algorithm;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;

public class DataPoint{

	private double[] coords;
	private DataRow row;

	public DataPoint(double[] coords, DataRow row) {

		this.coords = coords;
		this.row = row;

	}

	public double getCoordinate(int index) {

		if (coords.length > index)
			return coords[index];
		else
			return 0.0;

	}
	
	public DataRow getDataRow(){
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
