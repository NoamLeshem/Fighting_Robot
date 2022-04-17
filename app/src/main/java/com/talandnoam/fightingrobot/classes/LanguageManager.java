package com.talandnoam.fightingrobot.classes;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager
{
	private Context context;
	private final String[] languages = {"English", "Hebrew"};
	private final String[] languageCodes = {"en", "iw"};

	public LanguageManager(Context context)
	{
		this.setContext(context);
	}

	public void setLanguage(String language)
	{
		String languageCode = this.getLanguageCode(language);
		Locale locale = new Locale(languageCode);
		Locale.setDefault(locale);
		Resources res = context.getResources();
		Configuration config = res.getConfiguration();
		config.setLocale(locale);
		this.getContext().createConfigurationContext(config);
		res.updateConfiguration(config, res.getDisplayMetrics());

	}

	private String getLanguageCode (String language)
	{
		for (int i = 0; i < languages.length; i++)
			if (languages[i].equals(language))
				return languageCodes[i];
		return "en";
	}

	public Context getContext ()
	{
		return this.context;
	}

	public void setContext (Context context)
	{
		this.context = context;
	}
}
