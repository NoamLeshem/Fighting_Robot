package com.talandnoam.fightingrobot.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.activities.FightActivity;
import com.talandnoam.fightingrobot.classes.BetterActivityResult;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.Match;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.databinding.FightChooserBinding;
import com.talandnoam.fightingrobot.databinding.FragmentFightBinding;
import com.talandnoam.fightingrobot.utilities.services.BackgroundService;

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
	private static final String TAG = "FightFragment";
	protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	private FragmentFightBinding binding;
	private FightChooserBinding chooserBinding;
	private StorageReference imageReference;
	private boolean isFromCamera;
	private Bitmap matchBitmap;
	private Uri imagePath;

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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = FragmentFightBinding.inflate(inflater, container, false);

		handleSharedPreferences(binding.getRoot());
		setListeners(binding.getRoot());

		return binding.getRoot();
	}

	private void handleSharedPreferences (View rootView)
	{
		PrefsManager prefsManager = new PrefsManager(requireContext());
		int backgroundColor = prefsManager.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		rootView.setBackgroundColor(requireActivity().getColor(backgroundColor));
	}

	private void setListeners (View rootView)
	{
		binding.autoModeButton.setOnClickListener(view ->
		{
			Commons.vibrate();
			Commons.makeSnackbar(rootView, "autoModeActivityLauncher")
					.setAnchorView(R.id.bottom_navigation)
					.show();
		});
		binding.firebaseButton.setOnClickListener(view -> chooseFightingRules());
	}

	/**
	 * setting the adapters for each of the spinners.
	 */
	private void getSpinnerAdapter (Spinner matchType, Spinner matchLength, Spinner matchFormat)
	{
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> matchTypeAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.match_type, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> matchLengthAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.match_length, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> matchFormatAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.match_format, android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		matchLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		matchFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		matchType.setAdapter(matchTypeAdapter);
		matchLength.setAdapter(matchLengthAdapter);
		matchFormat.setAdapter(matchFormatAdapter);
	}

	private void chooseFightingRules ()
	{
		chooserBinding = FightChooserBinding.inflate(getLayoutInflater());
		getSpinnerAdapter(chooserBinding.matchTypeSpinner, chooserBinding.matchLengthSpinner, chooserBinding.matchFormatSpinner);
		Commons.vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
		AlertDialog myDialog = builder
				.setTitle(R.string.prep_the_fight)
				.setView(chooserBinding.getRoot())
				.setCancelable(true)
				.setNeutralButton(R.string.cancel, (dialog, which) -> Commons.vibrate())
				.show();
		chooserBinding.startButton.setOnClickListener(view ->
		{
			String type = chooserBinding.matchTypeSpinner.getSelectedItem().toString().trim();
			String length = chooserBinding.matchLengthSpinner.getSelectedItem().toString().trim();
			String format = chooserBinding.matchFormatSpinner.getSelectedItem().toString().trim();
			if (type.equals("match type") || length.equals("match length") || format.equals("match format") || matchBitmap == null)
				Commons.makeSnackbar(view, R.string.fill_all).show();
			else
			{
				startFight(type, length, format);
				myDialog.dismiss();
			}
		});
		chooserBinding.matchImageView.setOnClickListener(this::choosePhotoFromPhone);
	}

	private void choosePhotoFromPhone (View view)
	{
		MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(view.getContext());
		mBuilder.setTitle("Choose Robot Image")
				.setMessage("you can select from galley or camera")
				.setCancelable(true)
				.setPositiveButton("camera", (dialog, which) ->
				{
					isFromCamera = true;
					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					activityLauncher.launch(cameraIntent, this::myOnActivityResult);
				})
				.setNegativeButton("gallery", (dialog, which) ->
				{
					isFromCamera = false;
					Intent galleryIntent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
					Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image From");
					activityLauncher.launch(chooserIntent, this::myOnActivityResult);
				})
				.setNeutralButton("Cancel", (dialog, which) -> Log.d(TAG, "simpleAlert: canceled"))
				.create()
				.show();
	}

	private void myOnActivityResult (ActivityResult result)
	{
		Intent data = result.getData();
		int resultCode = result.getResultCode();
		Log.d(TAG, "myOnActivityResult: resultCode" + resultCode + "data" + data + "result ok" + Activity.RESULT_OK);
		if (isFromCamera && resultCode == Activity.RESULT_OK)
			matchBitmap = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
		else if (!isFromCamera && resultCode == Activity.RESULT_OK && Objects.requireNonNull(data).getData() != null)
			try
			{
				imagePath = data.getData();
				matchBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imagePath);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		chooserBinding.matchImageView.setImageBitmap(matchBitmap);
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
			uploadTask
					.addOnFailureListener(e ->
							Commons.showToast("Image upload NOT successfully"))
					.addOnSuccessListener(taskSnapshot ->
							Commons.showToast("Image upload successfully"));
		else
			Commons.showToast("Please upload a photo!");
	}

	private void startFight (String type, String length, String format)
	{
		DatabaseReference myRef1 = FirebaseManager.getDataRef("users/" + FirebaseManager.getUid() + "/match_history");
		DatabaseReference myRef2 = myRef1.push();
		DatabaseReference myRef3 =  FirebaseManager.getDataRef("processor/currentMatch");
		String matchId = myRef2.getKey();
		myRef3.setValue(matchId);
		imageReference = FirebaseManager.getStorageRef("users/" + FirebaseManager.getUid() + "/matches/" + matchId);
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

		Intent serviceIntent = new Intent(requireContext(), BackgroundService.class);
		//Start Service
		serviceIntent.setAction("ACTION_START_FOREGROUND_SERVICE");
		ContextCompat.startForegroundService(requireContext(), serviceIntent);

		Intent intent = new Intent(getContext(), FightActivity.class);
		Gson gson = new Gson();
		String json = gson.toJson(match);
		intent.putExtra("match", json);
		startActivity(intent);
	}
}
