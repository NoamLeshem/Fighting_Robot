package com.talandnoam.fightingrobot.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.fragments.FightFragment;
import com.talandnoam.fightingrobot.fragments.HistoryFragment;
import com.talandnoam.fightingrobot.fragments.SettingsFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity", KEY_THEME = "theme",
			KEY_FRAGMENT = "fragment", KEY_VIBRATION = "vibration";
	private boolean doubleBackToExitPressedOnce = false;
	private SharedPreferences sharedPreferences;
	private BottomNavigationView navigationView;
	private FragmentManager fragmentManager;
	private boolean vibrationState;
	private ActionBar actionBar;
	private Vibrator vibe;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeVariables();
		handleSharedPreferences();
		setListeners();
	}

	private void initializeVariables ()
	{
		vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		navigationView = findViewById(R.id.bottom_navigation);
		fragmentManager = getSupportFragmentManager();
		actionBar = getSupportActionBar();
	}

	private void handleSharedPreferences ()
	{
		sharedPreferences = this.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		vibrationState = sharedPreferences.getBoolean(KEY_VIBRATION, false);
		sharedPreferences = this.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		vibrationState = sharedPreferences.getBoolean(KEY_VIBRATION, false);
		int fragmentId = sharedPreferences.getInt(KEY_FRAGMENT, R.id.fight);
		handleItemSelected(fragmentId);
		navigationView.setSelectedItemId(fragmentId);
		int themeId = sharedPreferences.getInt(KEY_THEME, R.style.ThemeFightingRobot);
		this.setTheme(themeId);
	}

	private void setListeners ()
	{
		navigationView.setOnItemSelectedListener(item -> handleItemSelected(item.getItemId()));
		fragmentManager.setFragmentResultListener(
				"requestKey", this, (requestKey, result) ->
				{
					finish();
					startActivity(new Intent(getApplicationContext(), LoginActivity.class));
				});
	}

	private boolean handleItemSelected (int itemId)
	{
		int titleId = R.string.fight;
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		vibrate();
		Fragment selectedFragment = new Fragment();
		if (itemId == R.id.history) {
			selectedFragment = new HistoryFragment();
			titleId = R.string.history;
		} else if (itemId == R.id.fight) {
			selectedFragment = new FightFragment();
			titleId = R.string.fight;
		} else if (itemId == R.id.settings) {
			selectedFragment = new SettingsFragment();
			titleId = R.string.settings;
		}
		editor.putInt(KEY_FRAGMENT, itemId);
		editor.apply();
		Objects.requireNonNull(actionBar).setTitle(titleId);
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragment_container, selectedFragment)
				.commit();
		return true;
	}

	private void vibrate ()
	{
		if (vibrationState)
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	@Override
	public void onBackPressed()
	{
		//Checking for fragment count on backstack
		if (fragmentManager.getBackStackEntryCount() > 0)
			fragmentManager.popBackStack();
		else if (!doubleBackToExitPressedOnce) {
			this.doubleBackToExitPressedOnce = true;
			Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
		} else
			super.onBackPressed();
	}
}