package com.talandnoam.fightingrobot.fragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.activities.FightActivity;
import com.talandnoam.fightingrobot.classes.BetterActivityResult;
import com.talandnoam.fightingrobot.classes.Match;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FightFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FightFragment extends Fragment // implements IOnBackPressed
{
	private static final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
	private static final String TAG = "FightFragment";
	protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
	private static final String KEY_BACKGROUND = "background", KEY_VIBRATION = "vibration";
	private static final int CAMERA_REQUEST = 500, PICK_IMAGE = 123;
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	private FirebaseAuth mAuth;
	private Vibrator vibe;
	private Button autoButton;
	private Button firebaseButton;
	private ImageView matchImageView;
	private String matchId;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private boolean isFromCamera;
	private Bitmap matchBitmap;
	private Uri imagePath;
	private StorageReference imageReference;

	public FightFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 * <p>
	 * // @param param1 Parameter 1.
	 * // @param param2 Parameter 2.
	 *
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

		handleSharedPreferences(rootView);
		initComponents(rootView);
		setListeners(container, rootView);

		return rootView;
	}

	private void handleSharedPreferences (View rootView)
	{
		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.black);
		rootView.setBackgroundColor(requireActivity().getColor(backgroundColor));
	}

	private void initComponents (View rootView)
	{
		mAuth = FirebaseAuth.getInstance();
		vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		autoButton = rootView.findViewById(R.id.auto_mode_button);
		firebaseButton = rootView.findViewById(R.id.firebaseButton);
	}

	private void setListeners (ViewGroup container, View rootView)
	{
		autoButton.setOnClickListener(view ->
		{
			vibrate();
			Snackbar.make(rootView, "autoModeActivityLauncher", Snackbar.LENGTH_SHORT)
					.setAnchorView(R.id.bottom_navigation)
					.show();
		});
		firebaseButton.setOnClickListener(view -> chooseFightingRules(view, container));
	}

	/**
	 * setting the adapters for each of the spinners.
	 */
	private void getSpinnerAdapter (Spinner matchType, Spinner matchLength, Spinner matchFormat)
	{
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> matchTypeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.match_type, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> matchLengthAdapter = ArrayAdapter.createFromResource(getContext(), R.array.match_length, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> matchFormatAdapter = ArrayAdapter.createFromResource(getContext(), R.array.match_format, android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		matchLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		matchFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		matchType.setAdapter(matchTypeAdapter);
		matchLength.setAdapter(matchLengthAdapter);
		matchFormat.setAdapter(matchFormatAdapter);
	}


	private void vibrate ()
	{
		if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	private void chooseFightingRules (View view, ViewGroup container)
	{
		final View rootView = getLayoutInflater().inflate(R.layout.fight_chooser, container, false);
		matchImageView = rootView.findViewById(R.id.match_image_view);
		final Spinner matchType = rootView.findViewById(R.id.match_type_spinner);
		final Spinner matchLength = rootView.findViewById(R.id.match_length_spinner);
		final Spinner matchFormat = rootView.findViewById(R.id.match_format_spinner);
		final Button fightButton = rootView.findViewById(R.id.start_button);
		getSpinnerAdapter(matchType, matchLength, matchFormat);
		vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		AlertDialog myDialog = builder.setTitle(R.string.prep_the_fight)
				.setView(rootView)
				.setCancelable(true)
				.setNeutralButton(R.string.cancel, (dialog, which) -> vibrate())
				.show();
		fightButton.setOnClickListener(view1 ->
		{
			String type = matchType.getSelectedItem().toString().trim();
			String length = matchLength.getSelectedItem().toString().trim();
			String format = matchFormat.getSelectedItem().toString().trim();
			if (type.equals("match type") || length.equals("match length") || format.equals("match format") || matchBitmap == null)
				Snackbar.make(view1, R.string.fill_all, Snackbar.LENGTH_SHORT).show();
			else
			{
				startFight(type, length, format);
				myDialog.dismiss();
			}
		});
		matchImageView.setOnClickListener(this::choosePhotoFromPhone);
	}

	private void choosePhotoFromPhone (View view)
	{
		MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(view.getContext());
		mBuilder.setTitle("Choose Cocktail Image").setMessage("you can select from galley or camera").setCancelable(true).setPositiveButton("camera", (dialog, which) ->
		{
			isFromCamera = true;
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			activityLauncher.launch(cameraIntent, this::myOnActivityResult);
		}).setNegativeButton("gallery", (dialog, which) ->
		{
			isFromCamera = false;
			Intent galleryIntent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
			Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image From");
			activityLauncher.launch(chooserIntent, this::myOnActivityResult);
		}).setNeutralButton("Cancel", (dialog, which) -> Log.d(TAG, "simpleAlert: canceled")).create().show();
	}

	private void myOnActivityResult (ActivityResult result)
	{
		Intent data = result.getData();
		int resultCode = result.getResultCode();
		Log.d(TAG, "myOnActivityResult: resultCode" + resultCode + "data" + data + "result ok" + Activity.RESULT_OK);
		if (isFromCamera && resultCode == Activity.RESULT_OK)
			matchBitmap = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
		else if (!isFromCamera && resultCode == Activity.RESULT_OK && Objects.requireNonNull(data).getData() != null)
		{
			imagePath = data.getData();
			try
			{
				matchBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imagePath);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		matchImageView.setImageBitmap(matchBitmap);
	}

	private void uploadMatchImage ()
	{
		UploadTask uploadTask = null;
		if (isFromCamera)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			matchBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] data = baos.toByteArray();
			uploadTask = imageReference.putBytes(data);
		}
		else if (imagePath == null)
			Log.e("BAD", "addDogToDB: null imagePath");
		else
			uploadTask = imageReference.putFile(imagePath);
		if (uploadTask != null)
			uploadTask.addOnFailureListener(e -> Toast.makeText(requireActivity(), "Image upload NOT successfully", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> Toast.makeText(requireActivity(), "Image upload successfully", Toast.LENGTH_SHORT).show());
		else
		{
			Toast.makeText(requireActivity(), "Please upload a photo!", Toast.LENGTH_SHORT).show();
		}
	}

	private void startFight (String type, String length, String format)
	{
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef1 = database.getReference("users/" + mAuth.getUid() + "/match_history");
		DatabaseReference myRef2 = myRef1.push();
		DatabaseReference myRef3 = database.getReference("processor/currentMatch");
		String matchId = myRef2.getKey();
		myRef3.setValue(matchId);
		imageReference = FirebaseStorage.getInstance().getReference("users/" + mAuth.getUid() + "/matches/" + matchId);
		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		DateTimeFormatter myFormatTimeObj = DateTimeFormatter.ofPattern("HH:mm:ss");
		String formattedDate = myDateObj.format(myFormatDateObj);
		String formattedTime = myDateObj.format(myFormatTimeObj);
		uploadMatchImage();
		Match match = new Match(
				matchId,
				"we",
				formattedDate,
				formattedTime,
				type,
				format + " " + length,
				"0-0",
				length);
		myRef2.setValue(match);
		Intent intent = new Intent(getContext(), FightActivity.class);
		Gson gson = new Gson();
		String json = gson.toJson(match);
		intent.putExtra("match", json);
		startActivity(intent);
	}
}
