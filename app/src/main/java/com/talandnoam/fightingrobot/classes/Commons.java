package com.talandnoam.fightingrobot.classes;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public final class Commons
{
	private static final PrefsManager prefsManager = new PrefsManager(getApplicationContext());
	private static final Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

	public static void vibrate ()
	{
		if (prefsManager.getPrefBoolean(PrefsManager.KEY_VIBRATION))
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	public static void activityLauncher (Activity activity, Intent intent)
	{
		vibrate();
		activity.finish();
		activity.startActivity(intent);
	}

	public static void activityLauncher (Context context, Intent intent)
	{
		vibrate();
		context.startActivity(intent);
	}

	public static void showToast(String message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(int message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static Snackbar makeSnackbar(View view, String message)
	{
		return Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
	}

	public static Snackbar makeSnackbar(View view, int message)
	{
		return Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
	}
}
