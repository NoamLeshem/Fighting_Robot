package com.talandnoam.fightingrobot.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager
{
	public static final String PREF_NAME = "fighting_robot_pref",
			KEY_BACKGROUND = "background", KEY_ITEM_BACKGROUND = "background item",
			KEY_VIBRATION = "vibration", KEY_THEME = "theme",
			KEY_FRAGMENT = "fragment", KEY_ITEM = "item",
			KEY_LANGUAGE = "language", KEY_LANGUAGE_ITEM = "language code";
	private final SharedPreferences pref;
	private final SharedPreferences.Editor editor;

	public PrefsManager (Context context)
	{
		pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		editor = pref.edit();
	}

	public SharedPreferences getPref ()
	{
		return this.pref;
	}

	public SharedPreferences.Editor getEditor ()
	{
		return this.editor;
	}

	public void setPref (String key, String value)
	{
		this.getEditor().putString(key, value).apply();
	}

	public void setPref (String key, int value)
	{
		this.getEditor().putInt(key, value).apply();
	}

	public void setPref (String key, boolean value)
	{
		this.getEditor().putBoolean(key, value).apply();
	}

	public String getPrefString (String key, String defValue)
	{
		return this.getPref().getString(key, defValue);
	}

	public int getPrefInt (String key, int defValue)
	{
		return this.getPref().getInt(key, defValue);
	}

	public boolean getPrefBoolean (String key)
	{
		return this.pref.getBoolean(key, false);
	}

	public void clearPref ()
	{
		this.getEditor().clear().apply();
	}
}
