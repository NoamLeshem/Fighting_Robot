package com.talandnoam.fightingrobot.classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class Commons
{
	private final Context context;
	private final PrefsManager prefsManager;
	private final Vibrator vibe;

	public Commons(Context context)
	{
		this.context = context;
		prefsManager = new PrefsManager(context);
		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void vibrate ()
	{
		if (this.prefsManager.getPrefBoolean(PrefsManager.KEY_VIBRATION))
			this.vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	public void activityLauncher (Activity activity, Intent intent)
	{
		vibrate();
		activity.finish();
		activity.startActivity(intent);
	}

	public void activityLauncher (Context context, Intent intent)
	{
		vibrate();
		context.startActivity(intent);
	}

	public void showToast(String message)
	{
		Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
	}

	public void showToast(int message)
	{
		Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
	}

	public Snackbar makeSnackbar(View view, String message)
	{
		return Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
	}

	public Snackbar makeSnackbar(View view, int message)
	{
		return Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
	}
}
