package com.talandnoam.fightingrobot.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.login.LoginManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.LanguageManager;
import com.talandnoam.fightingrobot.classes.PrefsManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment
{
	private static final String TAG = "SettingsFragment";
	private Button signOutButton, primaryColorButton, bgChooseButton, languageButton, clearButton;
	private SwipeRefreshLayout mySwipeRefreshLayout;
	private LanguageManager languageManager;
	private SwitchMaterial vibrationSwitch;
	private PrefsManager prefsManager;
	private String[] languages;
	private View rootView;

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

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

	private void getViews ()
	{
		signOutButton = rootView.findViewById(R.id.sign_out);
		primaryColorButton = rootView.findViewById(R.id.color_chooser);
		bgChooseButton = rootView.findViewById(R.id.bg_chooser);
		languageButton = rootView.findViewById(R.id.language_chooser);
		clearButton = rootView.findViewById(R.id.clear_data_button);
		vibrationSwitch = rootView.findViewById(R.id.vibe_chooser);
		mySwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
	}

	private void handleSharedPreferences()
	{
		prefsManager = new PrefsManager(requireContext());
		languageManager = new LanguageManager(requireActivity());
		int backgroundColor = prefsManager.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		rootView.setBackgroundColor(requireActivity().getColor(backgroundColor));
		languageManager.setLanguage(prefsManager.getPrefString(PrefsManager.KEY_LANGUAGE, "English"));
	}

	private void setListeners ()
	{
		vibrationSwitch.setChecked(prefsManager.getPrefBoolean(PrefsManager.KEY_VIBRATION));
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
		languageButton.setOnClickListener(this::chooseLanguage);
	}

	private void chooseLanguage (View view)
	{
		Commons.vibrate();
		languages = new String[]{"English", "Hebrew"};
		int checkedItem = prefsManager.getPrefInt(PrefsManager.KEY_LANGUAGE_ITEM, 0);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.change_theme_color)
				.setSingleChoiceItems(languages, checkedItem, (dialogInterface, selectedItem) ->
						changeLanguage(selectedItem))
				.setPositiveButton(R.string.ok, (dialogInterface, i) ->
				{
					Commons.vibrate();
					refreshActivity();
				})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void refreshActivity ()
	{
		requireActivity().startActivity(requireActivity().getIntent());
	}

	private void changeLanguage (int selectedItem)
	{
		String languageCode = languages[selectedItem];
		languageManager.setLanguage(languageCode);
		prefsManager.setPref(PrefsManager.KEY_LANGUAGE_ITEM, selectedItem);
		prefsManager.setPref(PrefsManager.KEY_LANGUAGE, languageCode);
	}

	private void switchVibrationMode (View rootView, SwitchMaterial vibrationSwitch)
	{
		Commons.showToast(getString(R.string.vibe_is) + " " + (vibrationSwitch.isChecked() ? getString(R.string.on) : getString(R.string.off)));
		prefsManager.setPref(PrefsManager.KEY_VIBRATION, vibrationSwitch.isChecked());
		requireActivity().recreate();
	}

	private void myUpdateOperation ()
	{
		Log.d(TAG, "myUpdateOperation: ");
		mySwipeRefreshLayout.setRefreshing(false);
	}

	private void clearData (View view)
	{
		Commons.vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.clear_data)
				.setMessage(R.string.are_you_sure)
				.setPositiveButton(R.string.ok, (dialogInterface, i) ->
				{
					Commons.vibrate();
					prefsManager.clearPref();
					DatabaseReference myRef1 = FirebaseManager.getDataRef("users/" + FirebaseManager.getUid() + "/match_history");
					myRef1.removeValue();
					refreshActivity();
				})
				.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void chooseBackgroundColor (View view)
	{
		Commons.vibrate();
		String[] colors = getResources().getStringArray(R.array.colors);
		int checkedItem = prefsManager.getPrefInt(PrefsManager.KEY_ITEM_BACKGROUND, 1);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.change_bg_color)
				.setSingleChoiceItems(colors, checkedItem, (dialogInterface, selectedItem) ->
						changeBackgroundColor(selectedItem))
				.setPositiveButton(R.string.ok, (dialogInterface, i) ->
				{
					Commons.vibrate();
					requireActivity().recreate();
				})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void changeBackgroundColor (int selectedColor)
	{
		Commons.vibrate();
		int backgroundId = getBackgroundColor(selectedColor);
		prefsManager.setPref(PrefsManager.KEY_ITEM_BACKGROUND, selectedColor);
		prefsManager.setPref(PrefsManager.KEY_BACKGROUND, backgroundId);
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
		Commons.vibrate();
		Resources.Theme theme = requireContext().getTheme();
		int themeId = getThemeId(selectedColor);
		theme.applyStyle(themeId, true);
		prefsManager.setPref(PrefsManager.KEY_ITEM, selectedColor);
		prefsManager.setPref(PrefsManager.KEY_THEME, themeId); // save the selected color
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
		Commons.vibrate();
		String[] colors = {"blue", "red", "green", "yellow", "purple"};
		int checkedItem = prefsManager.getPrefInt(PrefsManager.KEY_ITEM, 4);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.change_theme_color)
				.setSingleChoiceItems(colors, checkedItem, (dialogInterface, selectedItem) ->
						changePrimaryColor(selectedItem))
				.setPositiveButton("ok", (dialogInterface, i) ->
				{
					Commons.vibrate();
					requireActivity().recreate();
				})
				.setIcon(R.drawable.ic_palette)
				.setCancelable(true)
				.show();
	}

	private void logoutVerify (View view)
	{
		Commons.vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.confirm_logout)
				.setMessage(R.string.logout_confirm)
				.setPositiveButton(R.string.yes, (dialogInterface, i) -> logout())
				.setNegativeButton(R.string.cancel,(dialogInterface, i) -> Commons.vibrate())
				.setIcon(R.drawable.ic_logout)
				.setCancelable(true)
				.show();
	}

	private void logout ()
	{
		Commons.vibrate();
		LoginManager.getInstance().logOut();
		FirebaseManager.signOut();

		Bundle result = new Bundle();
		result.putString("bundleKey", "result");
		// The child fragment needs to still set the result on its parent fragment manager
		getParentFragmentManager().setFragmentResult("requestKey", result);
		onDestroy();
	}
}