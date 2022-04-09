package com.talandnoam.fightingrobot.fragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.login.LoginManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.talandnoam.fightingrobot.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment
{
	private static final String TAG = "SettingsFragment";
	private static final Vibrator vibe  = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
	private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
	private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
	private static final String KEY_PRIMARY = "theme", KEY_ITEM = "item",
			KEY_BACKGROUND = "background", KEY_ITEM_BACKGROUND = "background item",
			KEY_VIBRATION = "vibration", KEY_SOUND = "sound", KEY_LANGUAGE = "language";
	private View rootView;
	private Button signOutButton, primaryColorButton, bgChooseButton, languageButton, clearButton;
	private SwitchMaterial vibrationSwitch;
	private SwipeRefreshLayout mySwipeRefreshLayout;
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private boolean vibrationState;

	public SettingsFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * // @param param1 Parameter 1.
	 * // @param param2 Parameter 2.
	 * @return A new instance of fragment SettingsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static SettingsFragment newInstance ()
	{
		return new SettingsFragment();

//		SettingsFragment fragment = new SettingsFragment();
//		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
//		fragment.setArguments(args);
//		return fragment;
	}

	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
		{
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.fragment_settings, container, false);

		getViews();
		handleSharedPreferences();
		setListeners();

		return rootView;
	}

	private void handleSharedPreferences()
	{
		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.white);
		rootView.setBackgroundColor(getResources().getColor(backgroundColor,  getResources().newTheme()));

		vibrationState = sharedPreferences.getBoolean(KEY_VIBRATION, false);
	}

	private void setListeners ()
	{
		vibrationSwitch.setChecked(vibrationState);
		vibrationSwitch.setOnClickListener(view -> switchVibrationMode(rootView, vibrationSwitch));
		clearButton.setOnClickListener(this::clearData);
		primaryColorButton.setOnClickListener(this::choosePrimaryColor);
		bgChooseButton.setOnClickListener(this::chooseBackgroundColor);
		signOutButton.setOnClickListener(this::logoutVerify);
		/*
		 * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
		 * performs a swipe-to-refresh gesture.
		 */
		mySwipeRefreshLayout.setOnRefreshListener(this::myUpdateOperation);
	}

	private void getViews ()
	{
		signOutButton = rootView.findViewById(R.id.sign_out);
		primaryColorButton = rootView.findViewById(R.id.color_chooser);
		bgChooseButton = rootView.findViewById(R.id.bg_chooser);
		languageButton = rootView.findViewById(R.id.language_chooser); // TODO: Add language chooser
		clearButton = rootView.findViewById(R.id.clear_data_button);
		vibrationSwitch = rootView.findViewById(R.id.vibe_chooser);
		mySwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
	}

	private void switchVibrationMode (View rootView, SwitchMaterial vibrationSwitch)
	{
		Snackbar.make(rootView, "Vibration is " + (vibrationSwitch.isChecked() ? "On" : "Off"), Snackbar.LENGTH_SHORT)
				.setAnchorView(R.id.bottom_navigation)
				.show();
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(KEY_VIBRATION, vibrationSwitch.isChecked());
		editor.apply();
		requireActivity().recreate();
	}

	private void myUpdateOperation ()
	{
		Log.d(TAG, "myUpdateOperation: ");
		requireActivity().recreate();
		mySwipeRefreshLayout.setRefreshing(false);
	}

	private void clearData (View view)
	{
		vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle("Clear Data")
				.setMessage("Are you sure you want to clear all data?\nThis action cannot be undone.\n\nThis will also clear your saved matches history.")
				.setPositiveButton("ok", (dialogInterface, i) ->
				{
					vibrate();
					sharedPreferences.edit().clear().apply();
					DatabaseReference myRef1 = firebaseDatabase.getReference("users/" + mAuth.getUid() + "/match_history");
					myRef1.removeValue();
					requireActivity().recreate();
				})
				.setNegativeButton("cancel", (dialogInterface, i) ->{})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void vibrate ()
	{
		if (vibrationState)
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	private void chooseBackgroundColor (View view)
	{
		vibrate();
		String[] colors = {"white", "black", "gray",
				"light blue", "light red", "light green", "light yellow", "light purple",
				"dark blue", "dark red", "dark green", "dark yellow", "dark purple"};
		int checkedItem = sharedPreferences.getInt(KEY_ITEM_BACKGROUND, 1);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle("Change Background Color")
				.setSingleChoiceItems(colors, checkedItem, (dialogInterface, selectedItem) ->
						changeBackgroundColor(selectedItem))
				.setPositiveButton("ok", (dialogInterface, i) ->
				{
					vibrate();
					requireActivity().recreate();
				})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void changeBackgroundColor (int selectedColor)
	{
		vibrate();
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		int backgroundId = getBackgroundColor(selectedColor);
		editor.putInt(KEY_ITEM_BACKGROUND, selectedColor);
		editor.putInt(KEY_BACKGROUND, backgroundId);
		editor.apply();
	}

	private int getBackgroundColor (int selectedColor)
	{
		switch (selectedColor)
		{
			case 0:
				return R.color.white; // white
			case 1:
				return R.color.black; // black
			case 2:
				return R.color.light_gray; // gray
			case 3:
				return R.color.light_blue; // light blue
			case 4:
				return R.color.light_red; // light red
			case 5:
				return R.color.light_green; // light green
			case 6:
				return R.color.light_yellow; // light yellow
			case 7:
				return R.color.light_purple; // light purple
			case 8:
				return R.color.dark_blue; // dark blue
			case 9:
				return R.color.dark_red; // dark red
			case 10:
				return R.color.dark_green; // dark green
			case 11:
				return R.color.dark_yellow; // dark yellow
			case 12:
				return R.color.dark_purple; // dark purple
		}
		return -1;
	}

	private void changePrimaryColor (int selectedColor)
	{
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		vibrate();
		Resources.Theme theme = requireContext().getTheme();
		int themeId = getThemeId(selectedColor);
		theme.applyStyle(themeId, true);
		editor.putInt(KEY_ITEM, selectedColor);
		editor.putInt(KEY_PRIMARY, themeId); // save the selected color
		editor.apply();
	}

	private int getThemeId (int selectedColor)
	{
		switch (selectedColor)
		{
			case 0:
				return R.style.OverlayPrimaryColorBlue;
			case 1:
				return R.style.OverlayPrimaryColorRed;
			case 2:
				return R.style.OverlayPrimaryColorGreen;
			case 3:
				return R.style.OverlayPrimaryColorYellow;
			case 4:
				return R.style.ThemeFightingRobot; // R.style.OverlayPrimaryColorPurple;
		}
		return -1;
	}

	private void choosePrimaryColor (View view)
	{
		vibrate();
		String[] colors = {"blue", "red", "green", "yellow", "purple"};
		int checkedItem = sharedPreferences.getInt(KEY_ITEM, 4);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle("Change Theme Color")
				.setSingleChoiceItems(colors, checkedItem, (dialogInterface, selectedItem) ->
						changePrimaryColor(selectedItem))
				.setPositiveButton("ok", (dialogInterface, i) ->
				{
					if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
						vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
					requireActivity().recreate();
				})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void logoutVerify (View view)
	{
		vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle("confirm logout")
				.setMessage("are you sure you want to logout?")
				.setPositiveButton("yes", (dialogInterface, i) -> logout())
				.setNegativeButton("cancel",(dialogInterface, i) -> {
					if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
						vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
				})
				.setIcon(R.drawable.ic_logout)
				.setCancelable(true)
				.show();
	}

	private void logout ()
	{
		vibrate();
		LoginManager.getInstance().logOut();
		FirebaseAuth.getInstance().signOut();

		Bundle result = new Bundle();
		result.putString("bundleKey", "result");
		// The child fragment needs to still set the result on its parent fragment manager
		getParentFragmentManager().setFragmentResult("requestKey", result);
		onDestroy();
	}
}