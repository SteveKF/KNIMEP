package org.knime.preferences.distance.algorithm;

import java.util.Comparator;

/**
 * Class for comparing two DataPoints so you can sort them by the first dimension
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataPointComparator implements Comparator<DataPoint>{
	
	private final int X = 0;
	private final int Y = 1;

	/**
	 * Sorts two DataPoints by the x value (first dimension)
	 */
	@Override
	public int compare(DataPoint o1, DataPoint o2) {
		
		int compareVal = Double.compare(o1.getCoordinateAt(X), o2.getCoordinateAt(X));
		
		if(compareVal == 0)
			return Double.compare(o1.getCoordinateAt(Y), o2.getCoordinateAt(Y));
		
		return compareVal;
		
	}

}
