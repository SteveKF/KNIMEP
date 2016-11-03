package org.knime.preferences.maximizer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;

public class DominationMaximizer {

	private DominationChecker domChecker;
	private List<DataPoint> repSkyline;
	private List<DataPoint> sky;

	private int k;


	public DominationMaximizer(int k, DominationChecker domChecker,
			BufferedDataTable dataTable) {
		
		List<DataPoint> dataPoints = new LinkedList<>();
		List<DataPoint> skyline = new LinkedList<>();

		for(DataRow row: dataTable){
			DataPoint p = DataPoint.createDataPoint(row); 
			dataPoints.add(p);
			skyline.add(p);
		}
			
		
		repSkyline = new LinkedList<>();

		this.k = k;
		this.domChecker = domChecker;

		repSkyline = computeRepSkyline(dataPoints,skyline);

	}

	private List<DataPoint> computeRepSkyline(List<DataPoint> dataPoints, List<DataPoint> skyline) {

		for (int i = 0; i < dataPoints.size(); i++) {

			DataPoint p = dataPoints.get(i);

			for (int j = 0; j < dataPoints.size(); j++) {

				DataPoint q = dataPoints.get(j);

				boolean isDominated = domChecker.isDominated(p, q);

				if (isDominated) {
				
					q.setDominated(true);
					p.increaseNumDominations();
					updateSkyline(p, q, skyline);

				}

			}

		}

		return getKPoints(skyline);

	}

	private List<DataPoint> getKPoints(List<DataPoint> skyline) {

		List<DataPoint> result = new LinkedList<>();

		skyline.sort(new Comparator<DataPoint>() {

			@Override
			public int compare(DataPoint p, DataPoint q) {

				return p.getNumDominations() < q.getNumDominations() ? 1
						: p.getNumDominations() == q.getNumDominations() ? 0 : -1;

			}
		});

		for (int i = 0; i < k; i++) {
			if (i == skyline.size())
				break;
			result.add(skyline.get(i));
		}

		sky = skyline;
		return result;
	}

	private void updateSkyline(DataPoint p, DataPoint q, List<DataPoint> skyline) {
	
		if (skyline.contains(q)) {
			skyline.remove(q);
		}
	}

	
	public List<RowKey> getRepSkylineKeys(){
		
		List<RowKey> rowKeys = new LinkedList<>();
		
		for(DataPoint p: repSkyline)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}
	
	public List<RowKey> getSkylineKeys(){
		
		List<RowKey> rowKeys = new LinkedList<>();
		
		for(DataPoint p: sky)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}
}
