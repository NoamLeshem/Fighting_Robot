package com.talandnoam.fightingrobot.classes;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Noam on 25/04/2022.
 * <br>
 * This class is used to save and load data from the SharedPreferences.
 * <br>
 * The data is saved in the following format:
 * <p>
 *     key: value
 *     <br>
 *     key: value
 *     <br>
 *     key: value
 *     <br>
 *     ...
 *     <br>
 *     key: value
 * </p>
 * The keys are the names of the preferences and the values are the values of the preferences
 *
 */
public class PrefsManager
{
	public static final String PREF_NAME = "fighting_robot_pref",
			KEY_BACKGROUND = "background", KEY_ITEM_BACKGROUND = "background item",
			KEY_VIBRATION = "vibration", KEY_THEME = "theme",
			KEY_FRAGMENT = "fragment", KEY_ITEM = "item",
			KEY_LANGUAGE = "language", KEY_LANGUAGE_ITEM = "language code";
	private final SharedPreferences pref;
	private final SharedPreferences.Editor editor;

	/**
	 * Constructor of the class PrefsManager that gets the context of the application
	 * @param context the context of the application
	 * @see Context
	 * @see SharedPreferences
	 * @see SharedPreferences.Editor
	 */
	public PrefsManager (Context context)
	{
		pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		editor = pref.edit();
	}

	/**
	 * Getter of the SharedPreferences
	 * @return the SharedPreferences of the application
	 */
	public SharedPreferences getPref ()
	{
		return this.pref;
	}

	/**
	 * Getter of the SharedPreferences.Editor
	 * @return the SharedPreferences.Editor of the application
	 */
	public SharedPreferences.Editor getEditor ()
	{
		return this.editor;
	}

	/**
	 * Setter for a String preference value
	 * @param key the key of the preference
	 * @param value the value of the preference
	 */
	public void setPref (String key, String value)
	{
		this.getEditor().putString(key, value).apply();
	}

	/**
	 * Setter for a int preference value
	 * @param key the key of the preference
	 * @param value the value of the preference
	 */
	public void setPref (String key, int value)
	{
		this.getEditor().putInt(key, value).apply();
	}

	/**
	 * Setter for a boolean preference value
	 * @param key the key of the preference
	 * @param value the value of the preference
	 */
	public void setPref (String key, boolean value)
	{
		this.getEditor().putBoolean(key, value).apply();
	}

	/**
	 * Getter for a String preference value
	 * @param key the key of the preference
	 * @param defValue the default value of the preference
	 * @return the value of the preference
	 */
	public String getPrefString (String key, String defValue)
	{
		return this.getPref().getString(key, defValue);
	}

	/**
	 * Getter for a int preference value
	 * @param key the key of the preference
	 * @param defValue the default value of the preference
	 * @return the value of the preference
	 */
	public int getPrefInt (String key, int defValue)
	{
		return this.getPref().getInt(key, defValue);
	}

	/**
	 * Getter for a boolean preference value
	 * @param key the key of the preference
	 * @return the value of the preference
	 */
	public boolean getPrefBoolean (String key)
	{
		return this.pref.getBoolean(key, false);
	}

	/**
	 * Clear all the preferences
	 */
	public void clearPref ()
	{
		this.getEditor().clear().apply();
	}
}
