package algorithm;

import java.util.Comparator;

public class DataPointComparator implements Comparator<DataPoint>{
	
	private final int X = 0;
	private final int Y = 0;

	@Override
	public int compare(DataPoint o1, DataPoint o2) {
		
		int compareVal = Double.compare(o1.getCoordinate(X), o2.getCoordinate(X));
		
		if(compareVal == 0)
			return Double.compare(o1.getCoordinate(Y), o2.getCoordinate(Y));
		
		return compareVal;
		
	}

}
