package org.knime.repskyline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import gui.RepresentativeSkylineThreshold;

/**
 * Class which computes the representative Skyline according to the algorithm in:
 * Magnani, Matteo, Ira Assent, and Michael L. Mortensen. "Taking the Big Picture: representative skylines based on significance and diversity." The VLDB Journal 23.5 (2014): 795-815.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class RepresentativeSkyline {

	/**
	 * Skyline as DataPoints. 
	 */
	private List<DataPoint> skyline;
	private List<DataRow> repSkyline;

	private Map<DataPoint, Map<DataPoint, Double>> objFunction;
	private Map<DataPoint, Map<DataPoint, Double>> diversity;
	private Map<DataPoint, Double> significance;

	private int[] colIndexes;
	private DataPoint maxSignificanceRecord;
	
	
	private int k;
	private String[] dimensions;
	private boolean useUpperBound;
	private Map<String, Double> singleValues;
	private Map<String, double[]> rangeValues;
	private Map<String, String> options;
	
	/**
	 * Constrcutor for this class which computes all representative skylines.
	 * @param skyData - the BufferedDataTable which stores all skyline records as DataRows
	 * @param dimensions - all the dimensions which only allow numeric values and have preferences 
	 * @param singleValues - single thresholds for every dimension
	 * @param rangeValues - range thresholds for every dimension
	 * @param options - option which thresholds was chosen by the user for every dimension
	 * @param k - size of the representative skyline 
	 * @param lambda - diversity weight
	 * @param useUpperBound - boolean to check if single threshold is used as a upper or lower bound
	 */
	protected RepresentativeSkyline(BufferedDataTable skyData, 
			String[] dimensions, Map<String,Double> singleValues, 
			Map<String, double[]> rangeValues,Map<String,String> options,
			int k,double lambda, boolean useUpperBound) {
		
		this.k = k;
		this.singleValues = singleValues;
		this.rangeValues = rangeValues;
		this.options = options;
		this.dimensions = dimensions;
		this.useUpperBound = useUpperBound;
		
		//list initialization
		repSkyline = new LinkedList<>();
		skyline = new LinkedList<>();
		diversity = new HashMap<>();
		significance = new HashMap<>();
		objFunction = new HashMap<>();
		
		//compute the indexes which the prompted dimensions have
		colIndexes = new int[dimensions.length];
		int n=0;
		List<String> dims = new LinkedList<>(Arrays.asList(dimensions));
		String[]columnNames = skyData.getSpec().getColumnNames();
		
		for(int i=0; i < columnNames.length; i++){

			if(dims.contains(columnNames[i]))
				colIndexes[n++] = i;

		}
		
		//create Data Points for every DataRow of the skyline
		for (DataRow row : skyData) {
			skyline.add(DataPoint.createDataPoint(row, colIndexes));
		}

		computeValues();

		//compute objFunction
		for (int i = 0; i < skyline.size(); i++) {
			DataPoint p = skyline.get(i);
			HashMap<DataPoint, Double> tmp = new HashMap<>();
			for (int j = 0; j < skyline.size(); j++) {
				DataPoint q = skyline.get(j);

				double value = diversity.get(p).get(q) * lambda + significance.get(q) * (1-lambda);
				tmp.put(q, value);
			}
			objFunction.put(p, tmp);
		}
		
		List<DataPoint> result = EGreedy();

		//only the data rows are important 
		for(int i=0; i < result.size(); i++){
			repSkyline.add(result.get(i).getDataRow());
		}
	}

	/**
	 * Computes the representative skyline. The first representative skyine record is the one 
	 * with the highest significance. After that every record which maximizes with the already computed 
	 * representative skyline the objective function will be put into the representative skyline.
	 * This will be done until k records were found.
	 * Objective function: lambda * diversity + (1-lambda) * significance
	 * @return Returns the representative Skyline as a list of DataPoints
	 */
	private List<DataPoint> EGreedy() {
		
		List<DataPoint> result = new LinkedList<DataPoint>();

		result.add(maxSignificanceRecord);

		for (int i = 0; i < k - 1; i++) {
			DataPoint next = getNextPoint(result);
			if(next!=null){
				result.add(next);
			}
		}

		return result;
	}

	/**
	 *
	 * @param result - current representative skyline
	 * @return Returns the DataPoint which maximizes the object function with the current representative skyline
	 */
	private DataPoint getNextPoint(List<DataPoint> result) {

		DataPoint next = null;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < result.size(); i++) {
			for (int j = 0; j < skyline.size(); j++) {

				double value = objFunction.get(skyline.get(j)).get(result.get(i));
				int compareVal = Double.compare(value, max);
				if (compareVal > 0 && !result.contains(skyline.get(j))) {
					max = value;
					next = skyline.get(j);
				}
			}
		}

		return next;
	}

	/**
	 * Computes the diversity and the significance by looping through all DataPoints and 
	 * call the according methods to compute those values.
	 */
	private void computeValues() {

		double max = 0.0;
		/*the significance only needs to iterate over every DataPoint once. 
		 If this is done it doesn't need to be computed anymore*/
		boolean isComputed = false;

		for (int i = 0; i < skyline.size(); i++) {
			DataPoint p = skyline.get(i);
			HashMap<DataPoint, Double> tmp = new HashMap<>();

			for (int j = 0; j < skyline.size(); j++) {
				DataPoint q = skyline.get(j);
				tmp.put(q, computeDiversity(p, q));

				if (!isComputed) {
					max = computeSignificance(q, max);
				}
			}
			//after the first iteration every logit value for every DataPoint was computed
			isComputed = true;

			
			diversity.put(p, tmp);
			//to get the significance every logit values needs to get divided by
			//the logit value of the DataPoint with the highest logit value
			significance.replace(p, significance.get(p) / max);
		}
	}

	/**
	 * Calls the logit function and checks if the logit value for DataPoint r is greater than maxSignificance.</br>
	 * This method should be looped over all DataPoints and the return value should be used a parameter for the next iteration.
	 * @param r - a DataPoint
	 * @param maxSignificance - the maximal significance which was computed by now
	 * @return Returns the logit value of DataPoint - if it is greater than maxSignificance </br>
	 * maxSignificance - otherwise
	 */
	private double computeSignificance(DataPoint r, double maxSignificance) {

		//computes logit for DataPoint r
		double logit = computeLogit(r);
		//puts the logit into a hashmap with the DataPoint as key
		significance.put(r, logit);

		int compareVal = Double.compare(logit, maxSignificance);
		if (compareVal > 0) {
			maxSignificance = logit;
			//save the DataPoint if it has a higher significance
			maxSignificanceRecord = r;
		}

		return maxSignificance;

	}

	/**
	 * Computes the Sigmoid logit value for DataPoin r.
	 * @param r - a DataPoint which Sigmoid Logit value should be computed
	 * @return Returns the Logit value of the DataPoint r
	 */
	private double computeLogit(DataPoint r) {

		double result = 0.0;

		for (int i = 0; i < dimensions.length; i++) {

			// SINGLE THRESHOLD VALUE WAS GIVEN FOR DIMENSION i
			if (options.get(dimensions[i]).equals(RepresentativeSkylineThreshold.SINGLE)) {
				
				double threshold = singleValues.get(dimensions[i]);
				
				//threshold should be used as a upper bound
				if(useUpperBound)
					result += 1 / (1 + Math.exp(r.getCoordinateAt(i) - threshold));
				else
					result += 1 / (1 + Math.exp(-r.getCoordinateAt(i) + threshold));

			// RANGE THRESHOLD VALUE WAS GIVEN FOR DIMENSION i
			} else if(options.get(dimensions[i]).equals(RepresentativeSkylineThreshold.RANGE)){

				double threshold[] = rangeValues.get(dimensions[i]);
				//check if there are only two thresholds
				assert(threshold.length == 2);
				
				result += ((-Math.log(1 + Math.exp(r.getCoordinateAt(i) - threshold[1])))
						+ (Math.log(1 + Math.exp(r.getCoordinateAt(i) - threshold[0]))))
						/ (threshold[1] - threshold[0]);
			// NO THRESHOLD VALUE WAS GIVEN FOR DIMENSION i
			}else{
				//if no thresholds are given
				result += 0.5;
			}
		}
		return (result / dimensions.length);
	}

	/**
	 * Calls for every coordinate of p and q the computeRecordsBetween method to count all
	 * record between those DataPoints. Divides through the number of skyline points -1 and 
	 * sums up all these values. By dividing through the number of skyline points, you get the
	 * diversity.  
	 * @param p - a DataPoint
	 * @param q - a DataPoint
	 * @return Returns the diversity between DataPoint p and q
	 */
	private double computeDiversity(DataPoint p, DataPoint q) {

		// sum of all the fraction of the skyline in each dimension
		double[] sumFracSky = new double[dimensions.length];

		for (int i = 0; i < p.getCoordinates().length ; i++) {

			int diversity = 0;
			
			int compareVal = Double.compare(p.getCoordinateAt(i), q.getCoordinateAt(i));
			
			if (compareVal > 0) {
				diversity = countRecordsBetween(p, q, i);
			} else if (compareVal < 0) {
				diversity = countRecordsBetween(q, p, i);
			} else if (!p.equals(q)) {
				diversity = countRecordsBetween(p, q, i);
			}
			// else it stays 0

			if (diversity != 0) {
				sumFracSky[i] = (diversity - 1.0) / (skyline.size() - 1.0);
			} else {
				sumFracSky[i] = 0.0;
			}
		}

		double result = 0.0;
		for (int i = 0; i < sumFracSky.length; i++) {
			result += sumFracSky[i];
		}

		result /= dimensions.length;

		return result;
	}

	/**
	 * Counts the number of records between DataPoint r and s for the coordinate with the prompted index.
	 * @param r - a DataPoint
	 * @param s - a DataPoint
	 * @param index - a index which says which coordinate should be considered for the counting
	 * @return Returns the number of records between DataPoint r and s
	 */
	private int countRecordsBetween(DataPoint r, DataPoint s, int index) {

		int diversity = 0;
		
		for (int i = 0; i < skyline.size(); i++) {
			DataPoint o = skyline.get(i);
			if (r.getCoordinateAt(index) >= o.getCoordinateAt(index)
					&& o.getCoordinateAt(index) >= s.getCoordinateAt(index)) {
				diversity++;
			}
		}

		return diversity;
	}
	
	/**
	 * 
	 * @return Returns all DataRows which were computed by the class's algorithm.
	 * These represent the representative skyline.
	 */
	public List<DataRow> getRepresentativeSkyline(){
		return repSkyline;
	}
}
