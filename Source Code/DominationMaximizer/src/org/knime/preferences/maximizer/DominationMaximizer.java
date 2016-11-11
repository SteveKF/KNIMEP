package org.knime.preferences.maximizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;

/**
 * This class computes the skyline and a representative skyline.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DominationMaximizer {

	private DominationChecker domChecker;
	private List<DataPoint> repSkyline;
	private List<DataPoint> skyline;

	private int k;


	/**
	 * Constructor of the DominationMaximizer algorithm.
	 * This algorithm computes the k representative skyline points 
	 * @param k - integer which tells how many points are in the representative skyline
	 * @param domChecker - DominationChecker object which checks the domination of two DataPoints
	 * @param dataTable - data table which should contain scores
	 * @throws InvalidSettingsException
	 */
	protected DominationMaximizer(int k, DominationChecker domChecker,
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

	/**
	 * Returns the representative skyline and computes the standard skyline
	 * @param dataPoints - List of data points which contains all data of the original data table
	 * @param tmpSkyline - List of data points which contains all data of the original data table. 
	 * Data Points which are dominated will be removed from this list
	 * @return Returns the representative skyline
	 * @throws InvalidSettingsException
	 */
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

	/**
	 * Checks which DataPoints in the skyline maximize the dominated points
	 * @param tmpSkyline - skyline (list of data points)
	 * @return Returns the k points which maximize the dominated points
	 */
	private List<DataPoint> getKPoints(List<DataPoint> tmpSkyline) {
		
		int skylineSize = tmpSkyline.size();
		List<DataPoint> result = new LinkedList<>();
		List<DataPoint> dominatedPoints = new LinkedList<>();

		while(result.size() < k && result.size() < skylineSize){
			
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

	/**
	 * 
	 * @return Returns the list of RowKeys of the representative skyline 
	 */
	public List<RowKey> getRepSkylineKeys(){
		
		List<RowKey> rowKeys = new LinkedList<>();
		
		for(DataPoint p: repSkyline)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}

	/**
	 * 
	 * @return Returns the list of RowKeys of the skyline 
	 */
	public List<RowKey> getSkylineKeys(){
		
		List<RowKey> rowKeys = new ArrayList<>();
		
		for(DataPoint p: skyline)
			rowKeys.add(p.getRowKey());
		
		return rowKeys;
		
	}
}
