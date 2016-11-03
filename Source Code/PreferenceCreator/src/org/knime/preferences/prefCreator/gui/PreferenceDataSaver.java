package org.knime.preferences.prefCreator.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * This class saves the values of all the input fields of the PreferencePanel.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class PreferenceDataSaver {

	private Map<String, NumericPreference> numericPreferences;
	private Map<String, LayeredDialog> layeredDialogs;


	/**
	 * Constructor which initializes a map and puts the dimension as key with default values in it.
	 * @param dimensions
	 * @param isDimensionNumeric
	 */
	public PreferenceDataSaver(String[] dimensions,
			Map<String, Boolean> isDimensionNumeric) {

		numericPreferences = new HashMap<>();
		layeredDialogs = new HashMap<>();

		for (int i = 0; i < dimensions.length; i++) {

			String preference = Preference.Lowest.toString();

			if (!isDimensionNumeric.get(dimensions[i]))
				preference = Preference.Layered.toString();

			// initiate numeric preferences
			NumericPreference savedPref = new NumericPreference(dimensions[i], preference);
			numericPreferences.put(dimensions[i], savedPref);
		}
	}

	/**
	 * Sets the prompted parameters in a map with the key as the value of the dimension.
	 * @param dimension - a string value which represents a dimension
	 * @param preference - a string value which represents a preference
	 * @param numericInput1 - the value of the first input field from the PreferencePanel
	 * @param numericInput2 - the value of the second input field from the PreferencePanel
	 * @param booleanInput - the value of the custom dimension
	 */
	public void setNumericPreferences(String dimension, String preference, double numericInput1, double numericInput2,
			String booleanInput) {

		NumericPreference savedPref = numericPreferences.get(dimension);

		savedPref.setPreference(preference);

		double[] numericInput = new double[] { numericInput1, numericInput2 };
		savedPref.setNumericInput(numericInput);

		savedPref.setBooleanInput(booleanInput);

	}

	
	/**
	 * 
	 * @param dimension - a string value which represents a dimension
	 * @return Returns the NumericPreference for this dimension
	 */
	public NumericPreference getNumericPreferences(String dimension) {

		return numericPreferences.get(dimension);

	}

	/**
	 * 
	 * @param dimension - a string value which represents a dimension
	 * @return a LayeredDialog - if the layeredDialogs map contains one LayeredDialog for this dimension </br>
	 * null - if the map doesn't contain a LayeredDialog for this dimension
	 */
	public LayeredDialog getLayeredDialog(String dimension) {

		if (layeredDialogs.containsKey(dimension)) {

			return layeredDialogs.get(dimension);

		} else {

			return null;

		}

	}

	/**
	 * Puts the LayeredDialog into the layeredDialogs map if there exists no LayeredDialog for this dimension. 
	 * Otherwise the existing LayeredDialog will be replaced.
	 * @param dimension - a string value which holds a dimension
	 * @param layeredDialog - a LayeredDialog
	 */
	public void setLayeredDialog(String dimension, LayeredDialog layeredDialog) {

		if (layeredDialogs.containsKey(dimension)) {

			layeredDialogs.replace(dimension, layeredDialog);

		} else {

			layeredDialogs.put(dimension, layeredDialog);

		}

	}
}
