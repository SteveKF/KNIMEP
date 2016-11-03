package org.knime.preferences.prefCreator.gui;

/**
 * This class saves every input of every input field from the PreferencePanel for every dimension.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class NumericPreference {

		private String dimension;
		private String preference;
		private double[] numericInput;
		private String booleanInput;

		/**
		 * Constructor which instantiates the dimension and preference member variables with the input.
		 * It initializes the other member variables with default values.
		 * @param dimension - a String value which represents a dimension
		 * @param preference - a String value which represents a preference
		 */
		public NumericPreference(String dimension, String preference) {
			
			this.dimension = dimension;
			this.preference = preference;

			numericInput = new double[2];
			numericInput[0] = 0.0;
			numericInput[1] = 0.0;

			booleanInput = "";
			
		}
		
		/**
		 * Constructor which sets all member variables to the prompted NumericPreference object
		 * @param savedPreference
		 */
		public NumericPreference(NumericPreference savedPreference){
			
			dimension = savedPreference.getDimension();
			preference = savedPreference.getPreference();
			numericInput = savedPreference.getNumericInput();
			booleanInput = savedPreference.getBooleanInput();
			
		}

		/**
		 * 
		 * @return Returns the dimension which this class was instantiated with
		 */
		public String getDimension() {
			return dimension;
		}

		/**
		 * 
		 * @return Returns the saved preference value 
		 */
		public String getPreference() {
			return preference;
		}

		/**
		 * 
		 * @return Returns the saved numeric input field values of the PreferencePanel as a double array
		 */
		public double[] getNumericInput() {
			return numericInput;
		}

		/**
		 * 
		 * @return Returns the saved custom dimension value 
		 */
		public String getBooleanInput() {
			return booleanInput;
		}
		
		/**
		 * 
		 * @param preference - a preference value
		 */
		public void setPreference(String preference){
			this.preference = preference;
		}
		
		/**
		 * 
		 * @param numericInput - a value from the numeric input fields
		 */
		public void setNumericInput(double[] numericInput){
			this.numericInput = numericInput;
		}
		
		/**
		 * 
		 * @param booleanInput - a custom dimension value
		 */
		public void setBooleanInput(String booleanInput){
			this.booleanInput = booleanInput;
		}
	}