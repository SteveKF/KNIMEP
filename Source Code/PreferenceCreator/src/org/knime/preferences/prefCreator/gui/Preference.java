package org.knime.preferences.prefCreator.gui;

/**
 * Enum which contains all preferences which are available 
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public enum Preference {
	
	Lowest, Highest, Around, Between, Boolean, Layered;
	
	/**
	 * 
	 * @return Returns all enum values/preferences as a string array
	 */
	public static String[] getPreferences(){
		Preference[] tmpPref = Preference.values();
		String[] preferences = new String[tmpPref.length];
		for (int i = 0; i < tmpPref.length; i++) {
			preferences[i] = tmpPref[i].toString();
		}
		
		return preferences;
	}
	
	/**
	 * Checks if a preference is a Lowest preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Lowest preference
	 */
	public static boolean isLowest(String preference){
		if(Lowest.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a preference is a Highest preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Highest preference
	 */
	public static boolean isHighest(String preference){
		if(Highest.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a preference is a Around preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Around preference
	 */
	public static boolean isAround(String preference){
		if(Around.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a preference is a Between preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Between preference
	 */
	public static boolean isBetween(String preference){
		if(Between.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a preference is a Boolean preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Boolean preference
	 */
	public static boolean isBoolean(String preference){
		if(Boolean.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if a preference is a Layered preference.
	 * @param preference - a string which should represent a preference
	 * @return true - if the preference parameter is a Layered preference
	 */
	public static boolean isLayered(String preference){
		if(Layered.toString().equals(preference)){
			return true;
		}else{
			return false;
		}
	}
}
