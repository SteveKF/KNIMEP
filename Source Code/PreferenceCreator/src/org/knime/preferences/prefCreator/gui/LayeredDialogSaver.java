package org.knime.preferences.prefCreator.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * This class saves the values of all the input fields of the PreferencePanel.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class LayeredDialogSaver {

	private Map<String, LayeredDialog> layeredDialogs;


	/**
	 * Constructor which initializes a map and puts the dimension as key with default values in it.
	 * @param dimensions
	 * @param isDimensionNumeric
	 */
	public LayeredDialogSaver() {

		layeredDialogs = new HashMap<>();

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