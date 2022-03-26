package com.talandnoam.fightingrobot.fragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.activities.FightActivity;
import com.talandnoam.fightingrobot.classes.Match;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FightFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FightFragment extends Fragment // implements IOnBackPressed
{
	private static final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
	private static final String KEY_BACKGROUND = "background", KEY_VIBRATION = "vibration";

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	private FirebaseAuth mAuth;
	private Vibrator vibe;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	public FightFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * // @param param1 Parameter 1.
	 * // @param param2 Parameter 2.
	 * @return A new instance of fragment FightFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static FightFragment newInstance ()
	{
		return new FightFragment();

//		FightFragment fragment = new FightFragment();
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
		final View rootView = inflater.inflate(R.layout.fragment_fight, container, false);

		mAuth = FirebaseAuth.getInstance();
		vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.black);
		rootView.setBackgroundColor(getResources().getColor(backgroundColor, null));
		final Button autoButton = rootView.findViewById(R.id.auto_mode_button);
		final Button firebaseButton = rootView.findViewById(R.id.firebaseButton);


		autoButton.setOnClickListener(view ->
				{
					boolean vibration = sharedPreferences.getBoolean(KEY_VIBRATION, false);
					if (vibration)
						vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
					Snackbar.make(rootView,"autoModeActivityLauncher", BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(R.id.bottom_navigation).show();
				});
		firebaseButton.setOnClickListener(this::materialAlertDialogBuilder);
		return rootView;
	}

	private void materialAlertDialogBuilder (View view)
	{

		if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle("Tilt your phone")
				.setMessage("The next screen is sideways (horizontal), please turn your phone for proper use.")
				.setPositiveButton("ok", (dialogInterface, i) -> startFight())
				.setNegativeButton("cancel",(dialogInterface, i) -> vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)))
				.setIcon(R.drawable.ic_screen_rotation)
				.setCancelable(true)
				.create()
				.show();
	}

	private void startFight ()
	{
		DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getUid() + "/match_history");
		DatabaseReference myRef2 = myRef1.push();
		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter myFormatTimeObj = DateTimeFormatter.ofPattern("HH:mm:ss");
		String formattedDate = myDateObj.format(myFormatDateObj);
		String formattedTime = myDateObj.format(myFormatTimeObj);
		myRef2.setValue(new Match(myRef2.getKey(),"we", formattedDate, formattedTime,"friendly","to 10 points","10-4"));
		startActivity(new Intent(getApplicationContext(), FightActivity.class));
	}
}
