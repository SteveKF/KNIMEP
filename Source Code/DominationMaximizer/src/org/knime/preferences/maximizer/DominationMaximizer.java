package org.knime.preferences.maximizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

public class DominationMaximizer {

	private DominationChecker domChecker;
	private List<DataPoint> repSkyline;
	private List<DataPoint> skyline;

	private int k;


	public DominationMaximizer(int k, DominationChecker domChecker,
			BufferedDataTable dataTable) throws InvalidSettingsException {
		
		skyline = new ArrayList<>();
		repSkyline = new ArrayList<>();
		
		this.k = k;
		this.domChecker = domChecker;
		
		List<DataPoint> dataPoints = new ArrayList<>();
		List<DataPoint> tmpSkyline = new ArrayList<>();

		for(DataRow row: dataTable){
			DataPoint p = DataPoint.createDataPoint(row); 
			dataPoints.add(p);
		}
		tmpSkyline = new ArrayList<>(dataPoints);

		repSkyline = computeRepSkyline(dataPoints,tmpSkyline);

	}

	private List<DataPoint> computeRepSkyline(List<DataPoint> dataPoints, List<DataPoint> tmpSkyline) throws InvalidSettingsException {

		for(DataPoint p : dataPoints){
			for(DataPoint q : dataPoints){
				if(p!=q){
					if(domChecker.isDominated(p, q)){
						tmpSkyline.remove(q);
						p.addDominatedPoint(q);
					}	
				}
			}
		}
		
		skyline = new ArrayList<>(tmpSkyline);
		
		return getKPoints(tmpSkyline);

	}

	private List<DataPoint> getKPoints(List<DataPoint> tmpSkyline) {
		
		List<DataPoint> result = new LinkedList<>();
		List<DataPoint> dominatedPoints = new LinkedList<>();

		while(result.size() < k && result.size() < tmpSkyline.size()){
			
			int numDominated = 0;
			int newEntry = 0;
			for(int i=0; i < tmpSkyline.size(); i++){
				List<DataPoint> skyDominatedPoints = tmpSkyline.get(i).getDominatedPoints();
				List<DataPoint> tmpDominatedList = new ArrayList<>(dominatedPoints);
				tmpDominatedList.removeAll(skyDominatedPoints);
				tmpDominatedList.addAll(skyDominatedPoints);
				
				if(tmpDominatedList.size() > numDominated)
					numDominated = tmpDominatedList.size();
					newEntry = i;
			}
			
			DataPoint repSkyPoint = tmpSkyline.get(newEntry);
			result.add(repSkyPoint);
			tmpSkyline.remove(repSkyPoint);
			dominatedPoints.removeAll(repSkyPoint.getDominatedPoints());
			dominatedPoints.addAll(repSkyPoint.getDominatedPoints());
			
		}
		
		return result;
	}

	
	public List<RowKey> getRepSkylineKeys(){
		
		List<RowKey> rowKeys = new LinkedList<>();
		
		for(DataPoint p: repSkyline)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}
	
	public List<RowKey> getSkylineKeys(){
		
		List<RowKey> rowKeys = new ArrayList<>();
		
		for(DataPoint p: skyline)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}
}
