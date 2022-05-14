package com.talandnoam.fightingrobot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.databinding.ActivityMainBinding;
import com.talandnoam.fightingrobot.fragments.FightFragment;
import com.talandnoam.fightingrobot.fragments.HistoryFragment;
import com.talandnoam.fightingrobot.fragments.SettingsFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
	private final Commons commons = new Commons(getApplicationContext());
	private boolean doubleBackToExitPressedOnce = false;
	private FragmentManager fragmentManager;
	private ActivityMainBinding binding;
	private PrefsManager prefsManager;
	private ActionBar actionBar;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initializeVariables();
		handleSharedPreferences();
		setListeners();
	}

	private void initializeVariables ()
	{
		fragmentManager = getSupportFragmentManager();
		actionBar = getSupportActionBar();
	}

	private void handleSharedPreferences ()
	{
		prefsManager = new PrefsManager(this);
		int fragmentId = prefsManager.getPrefInt(PrefsManager.KEY_FRAGMENT, R.id.fight);
		handleItemSelected(fragmentId);
		binding.bottomNavigation.setSelectedItemId(fragmentId);
		int themeId = prefsManager.getPrefInt(PrefsManager.KEY_THEME, R.style.ThemeFightingRobot);
		this.setTheme(themeId);
	}

	private void setListeners ()
	{
		binding.bottomNavigation.setOnItemSelectedListener(item -> handleItemSelected(item.getItemId()));
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

		commons.vibrate();
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
		prefsManager.setPref(PrefsManager.KEY_FRAGMENT, itemId);
		Objects.requireNonNull(actionBar).setTitle(titleId);
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragment_container, selectedFragment)
				.commit();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed()
	{
		//Checking for fragment count on backstack
		if (fragmentManager.getBackStackEntryCount() > 0)
			fragmentManager.popBackStack();
		else if (!doubleBackToExitPressedOnce) {
			this.doubleBackToExitPressedOnce = true;
			commons.showToast(R.string.press_back_again);
			new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
		} else
			super.onBackPressed();
	}
}