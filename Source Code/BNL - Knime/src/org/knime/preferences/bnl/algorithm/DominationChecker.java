package org.knime.preferences.bnl.algorithm;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Class to check if a Data Point dominates another Data Point
 * @author Stefan Wohlfart 1.0
 * @version 1.0
 *
 */
public class DominationChecker {
	
	/**
	 * Boolean which tells if there is only one preference which needs to be considered
	 */
	private boolean isSinglePreference = false;

	// all complex preferences (priority or pareto)
	private Map<String, String> complexPref;
	//all basic preferences
	private Map<String, String[]> underlyingPrefs;
	//column Index for every complex preference
	private Map<String, Integer> colIndexes;


	/**
	 * Constructor for the Domination Checker. </br>
	 * Checks if a Data Points dominates another Data Point
	 * @param flowVars - flow variables
	 * @param scoreSpec - data table which contains the scores of the data records
	 */
	public DominationChecker(Map<String, FlowVariable> flowVars, DataTableSpec scoreSpec) {

		complexPref = new TreeMap<>();
		underlyingPrefs = new TreeMap<>();
		colIndexes = new TreeMap<>();

		Map<String, String> preferences = new TreeMap<>();
		int count=0;
		String prefName = "P" + count++;
		
		if(!flowVars.containsKey(prefName))
			isSinglePreference = true;
		
		
		while(flowVars.containsKey(prefName)){
			preferences.put(prefName, flowVars.get(prefName).getStringValue());
			prefName = "P" + count++;
		}

		Set<String> keySet = preferences.keySet();
		for (String key : keySet) {
			String string = preferences.get(key);
			String[] parts = string.split(",");
			assert(parts.length == 3);
			complexPref.put(key, parts[0]);
			underlyingPrefs.put(key, new String[]{parts[1],parts[2]});
		}

		// set up map with name of the columns and the indexes of these columns
		// as values
		for (int i = 0; i < scoreSpec.getNumColumns(); i++)
			colIndexes.put(scoreSpec.getColumnSpec(i).getName(), i);
	}
	
	/**
	 * 
	 * @param p - a Data Point
	 * @param q - a Data Point
	 * @return true - if p dominates q </br>
	 * false - if p doesn't dominate q
	 * @throws InvalidSettingsException
	 */
	public boolean isDominated(DataPoint p, DataPoint q) throws InvalidSettingsException {
		
		//if only a single score exists => compare only this score
		if(isSinglePreference && p.getCoordinates().length == 1 && q.getCoordinates().length==1){
			int compareVal = Double.compare(p.getCoordinate(0), q.getCoordinate(0));
			if(compareVal < 0)
				return true;
			else
				return false;
		}else if(isSinglePreference && p.getCoordinates().length != 1 && q.getCoordinates().length != 1){
			throw new InvalidSettingsException("Error occured while checking domination of two data points.");
		}
		
		boolean isDominated = false;
		
		/*
		 * To check domination the node structure of the Preference Create Node needs to be looped through
		 * The node structure is saved as flowvariables in the following format:
		 * P0 - ComplexPreference,Preference1,Preference2 (Preference1 and 2 can be additionally complex preferences)
		 * If Preference1 is a complex preference, too: P1 - ComplexPreference,Preference1,Preference2
		 * The loop terminates when a complex preference is found with only basic preferences and then all other complex preferences can be computed 
		 */
		String prefName = "P"+0;
		String comPref = complexPref.get(prefName);
		String[] prefs = underlyingPrefs.get(prefName);
		
		//depending on the complex preference the priority function of this type will be called
		if (comPref.equals("Priority")) {
			isDominated = priorCmp(p, q, prefs[0],prefs[1]);
		} else if (comPref.equals("Pareto")) {
			isDominated = paretoCmp(p, q, prefs[0],prefs[1]);
		}

		return isDominated;
	}

	/**
	 * 
	 * @param p - a Data Point
	 * @param q - a Data Point
	 * @param pref1 - preference 1
	 * @param pref2 - preference 2
	 * @return true - if p is better than q in preference 1 - or if p and q are equal in preference 1 and p is better than q in preference 2 </br> 
	 * false - otherwise
	 */
	private boolean priorCmp(DataPoint p, DataPoint q, String pref1, String pref2) {
		return cmp(p, q, pref1) || (eq(p, q, pref1) && cmp(p, q, pref2));
	}
	
	/**
	 * 
	 * @param p - a Data Point
	 * @param q - a Data Point
	 * @param pref1 - preference 1
	 * @param pref2 - preference 2
	 * @return true - if p has equal values for preference 1 and 2 </br>
	 * 
	 */
	private boolean priorParetoEq(DataPoint p, DataPoint q, String pref1, String pref2) {
		return eq(p, q, pref1) && eq(p, q, pref2);
	}

	/**
	 * 
	 * @param p - a Data Point
	 * @param q - a Data Point
	 * @param pref1 - preference 1
	 * @param pref2 - preference 2
	 * @return true - if p is better than q in preference 1 and is equal or better in preference 2 - or
	 * if p is better than q in preference 2 and is equal or better in preference 1 </br>
	 * false - otherwise
	 */
	private boolean paretoCmp(DataPoint p, DataPoint q, String pref1, String pref2) {
		return (cmp(p, q, pref1) && (cmp(p, q, pref2) || eq(p, q, pref2)))
				|| (cmp(p, q, pref2) && (cmp(p, q, pref1) || eq(p, q, pref1)));
	}

	/**
	 * 
	 * @param p - a Data Point 
	 * @param q - a Data Point 
	 * @param pref - a complex preference
	 * @return true - if p is better than q for the preferences of the complex preference </br>
	 * false - otherwise
	 */
	private boolean cmp(DataPoint p, DataPoint q, String pref) {
		int colIndex = 0;
		if (colIndexes.containsKey(pref)) {
			colIndex = colIndexes.get(pref);
		} else {
			String comPref = complexPref.get(pref);
			String[] prefs = underlyingPrefs.get(pref);
			if (comPref.equals("Priority"))
				return priorCmp(p, q, prefs[0], prefs[1]);
			else if (comPref.equals("Pareto"))
				return paretoCmp(p, q, prefs[0], prefs[1]);

		}
		int compareVal = Double.compare(p.getCoordinate(colIndex), q.getCoordinate(colIndex));
		if (compareVal < 0)
			return true;
		else
			return false;
	}


	/**
	 * 
	 * @param p - a Data Point 
	 * @param q - a Data Point 
	 * @param pref - a complex preference
	 * @return true - if p is equal than q for the preferences of the complex preference </br>
	 * false - otherwise
	 */
	private boolean eq(DataPoint p, DataPoint q, String pref) {
		int colIndex = 0;
		if (colIndexes.containsKey(pref)) {
			colIndex = colIndexes.get(pref);
		} else {
			String comPref = complexPref.get(pref);
			String[] prefs = underlyingPrefs.get(pref);
			if (comPref.equals("Priority"))
				return priorParetoEq(p, q, prefs[0], prefs[1]);
			else if (comPref.equals("Pareto"))
				return priorParetoEq(p, q, prefs[0], prefs[1]);

		}
		int compareVal = Double.compare(p.getCoordinate(colIndex), q.getCoordinate(colIndex));
		if (compareVal == 0)
			return true;
		else
			return false;
	}
}
