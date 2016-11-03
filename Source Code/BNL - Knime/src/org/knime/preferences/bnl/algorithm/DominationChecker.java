package org.knime.preferences.bnl.algorithm;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.workflow.FlowVariable;

public class DominationChecker {

	private Map<String, String> complexPref;
	private Map<String, String[]> underlyingPrefs;
	private Map<String, Integer> colIndexes;


	public DominationChecker(Map<String, FlowVariable> flowVars, DataTableSpec scoreSpec) {

		complexPref = new TreeMap<>();
		underlyingPrefs = new TreeMap<>();
		colIndexes = new TreeMap<>();

		Map<String, String> preferences = new TreeMap<>();
		int count=0;
		String prefName = "P" + count++;
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
	
	public boolean isDominated(DataPoint p, DataPoint q) {
		
		boolean isDominated = false;
		
		String prefName = "P"+0;
		String comPref = complexPref.get(prefName);
		String[] prefs = underlyingPrefs.get(prefName);
		
		if (comPref.equals("Priority")) {
			isDominated = priorCmp(p, q, prefs[0],prefs[1]);
		} else if (comPref.equals("Pareto")) {
			isDominated = paretoCmp(p, q, prefs[0],prefs[1]);
		}

		return isDominated;
	}

	private boolean priorCmp(DataPoint p, DataPoint q, String pref1, String pref2) {
		return cmp(p, q, pref1) || (eq(p, q, pref1) && cmp(p, q, pref2));
	}
	
	private boolean priorEq(DataPoint p, DataPoint q, String pref1, String pref2) {
		return eq(p, q, pref1) && eq(p, q, pref2);
	}

	private boolean paretoCmp(DataPoint p, DataPoint q, String pref1, String pref2) {
		return (cmp(p, q, pref1) && (cmp(p, q, pref2) || eq(p, q, pref2)))
				|| (cmp(p, q, pref2) && (cmp(p, q, pref1) || eq(p, q, pref1)));
	}

	private boolean paretoEq(DataPoint p, DataPoint q, String pref1, String pref2) {
		return eq(p, q, pref1) && eq(p, q, pref2);
	}
	
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

	private boolean eq(DataPoint p, DataPoint q, String pref) {
		int colIndex = 0;
		if (colIndexes.containsKey(pref)) {
			colIndex = colIndexes.get(pref);
		} else {
			String comPref = complexPref.get(pref);
			String[] prefs = underlyingPrefs.get(pref);
			if (comPref.equals("Priority"))
				return priorEq(p, q, prefs[0], prefs[1]);
			else if (comPref.equals("Pareto"))
				return paretoEq(p, q, prefs[0], prefs[1]);

		}
		int compareVal = Double.compare(p.getCoordinate(colIndex), q.getCoordinate(colIndex));
		if (compareVal == 0)
			return true;
		else
			return false;
	}
}
