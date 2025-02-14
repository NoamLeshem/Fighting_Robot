package com.talandnoam.fightingrobot.activities;

import static com.google.android.gms.common.util.DeviceProperties.isTablet;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.Match;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.databinding.ActivityFightBinding;

import java.util.Objects;

/**
 * FightActivity - The activity that shows the fight between the two robots.
 * <p>
 *     This activity is responsible for the fight between the two robots.
 *     It is responsible for the communication between the robot and the server.
 * </p>
 * <p>
 *     The activity is created by the {@link com.talandnoam.fightingrobot.activities.MainActivity}
 *     and is destroyed when the user returns to the {@link com.talandnoam.fightingrobot.activities.MainActivity}.
 *     The activity is also destroyed when the user closes the app.
 * </p>
 * @author TalandNoam
 * @version 1.0
 * @since 2020-02-05
 */
public class FightActivity extends AppCompatActivity
{
	private DatabaseReference myRef2, myRef3, myRef4, myRef5, myRef6, myRef7, myRef8;
	private static final String TAG = "FightActivity";
	public static ActivityFightBinding binding;
	public static Match match;
	private Commons commons;
	public static int score;

	/**
	 * {@inheritDoc}
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = ActivityFightBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		Objects.requireNonNull(getSupportActionBar()).hide();

		extractDataFromIntent();
		handleSharedPreferences();
		setWebViewSettings();
		initializeFirebaseDirectory();
		setMatchScore();
		setListeners();
	}

	private void setWebViewSettings ()
	{
		final WebSettings webSettings = binding.webView.getSettings();
		webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
		webSettings.setAppCacheEnabled(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // there are transfer problems when using cached resources
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setSupportMultipleWindows(true);
		webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; Build/HUAWEI" +  getString(R.string.app_name) + ") Version/1.10" + (isTablet(this) ? " Tablet " : " Mobile ") + "Safari/537.36");
		binding.webView.setWebChromeClient(new WebChromeClient());
		binding.webView.setWebViewClient(new WebViewClient());

		// Allow webContentsDebugging if APK was build as debuggable
		if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
			WebView.setWebContentsDebuggingEnabled(true);
	}

	/**
	 * This method is responsible for extracting the data from the intent.
	 * <p>
	 *     This method is called in {@link #onCreate(Bundle)}
	 *     The data is extracted from the intent and saved in the match variable.
	 * </p>
	 */
	private void extractDataFromIntent ()
	{
		Gson gson = new Gson();
		String json = getIntent().getStringExtra("match");
		match = gson.fromJson(json, Match.class);
		binding.foramt.setText(match.getFormat());
	}

	/**
	 * This method is responsible for handling the shared preferences.
	 * It is used to get the background color of the screen.
	 * <p>
	 *     This method is called in {@link #onCreate(Bundle)}
	 *     The background color is saved in the shared preferences.
	 *     If the background color is not saved in the shared preferences,
	 *     the default background color is set to be black.
	 * </p>
	 */
	private void handleSharedPreferences ()
	{
		PrefsManager prefsManager = new PrefsManager(this);
		int backgroundColor = prefsManager
				.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		binding.activityFight
				.setBackgroundColor(getColor(backgroundColor));
		commons = new Commons(this);
	}

	/**
	 * Initialize Firebase database directory for this activity
	 * <p>
	 *     This method is called in {@link #onCreate(Bundle)}
	 *     and it is used to initialize the Firebase database directory
	 *     for this activity.
	 *     The directory is initialized by calling {@link FirebaseManager#getDataRef(String)}
	 *     with the directory name "processor".
	 *     <br>
	 *     and the directory is stored in {@link #myRef2} - the directory for left-joystick/angle.
	 *     <br>
	 *     and {@link #myRef3} - the directory for left-joystick/strength.
	 *     <br>
	 *     and {@link #myRef4} - the directory for right-joystick/x.
	 *     <br>
	 *     and {@link #myRef5} - the directory for right-joystick/y.
	 *     <br>
	 *     and {@link #myRef6} - the directory for processor/laser-emitter.
	 * </p>
	 */
	private void initializeFirebaseDirectory ()
	{
		// hitRef = FirebaseManager.getDataRef("processor/isHit"); // hitRef is the firebase directory for knowing if the robot got hit
		DatabaseReference myRef1 = FirebaseManager.getDataRef("processor/"); // myRef1 is the firebase directory for the controller
		myRef2 = myRef1.child("controller/leftStick/angle"); // myRef2 is the firebase directory for the left stick angle
		myRef3 = myRef1.child("controller/leftStick/strength"); // myRef3 is the firebase directory for the left stick strength
		myRef4 = myRef1.child("controller/rightStick/x"); // myRef4 is the firebase directory for the right stick x
		myRef5 = myRef1.child("controller/rightStick/y"); // myRef5 is the firebase directory for the right stick y
		myRef6 = myRef1.child("laserEmitter"); // myRef6 is the firebase directory for the laser emitter
		myRef7 = myRef1.child("cam/ip"); // myRef7 is the firebase directory for the camera ip
		myRef8 = myRef1.child("cam/trackMode"); // myRef8 is the firebase directory for the camera track mode
	}

	/**
	 * This method is responsible for listening to the firebase directory,
	 * and updating the score of the match accordingly.
	 * <p>
	 *     This method is called in {@link #onCreate(Bundle)}
	 *     and it is used to listen to the firebase directory
	 *     and update the score of the match accordingly.
	 * </p>
	 */
	private void setMatchScore ()
	{
		DatabaseReference myRef = FirebaseManager.getDataRef("users/" + FirebaseManager.getUid() + "/match_history/" + match.getId() + "/matchResult");
		myRef.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{ binding.score.setText(snapshot.getValue(String.class)); }

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled: " + error.getMessage()); }
		});
	}

	/**
	 * This method is responsible for setting the listeners for the joystick and the shoot button.
	 * <br>
	 * The listeners are responsible for sending the data to the firebase directory.
	 * <br>
	 * The listeners are also responsible for updating the score and the match result.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void setListeners ()
	{
		myRef7.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{
				String camIp = snapshot.getValue(String.class);
				binding.webView.loadUrl("http://" + camIp + "/index.html");
			}

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled: " + error.getMessage()); }
		});
		binding.shootButton.setOnTouchListener((v, event) ->
				sendToFirebaseWhilePressed(event));
		binding.joystickLeft.setOnMoveListener((angle, strength) ->
		{
			myRef2.setValue(angle);
			myRef3.setValue(strength);
		});
		binding.joystickRight.setOnMoveListener((angle, strength) ->
		{
			double x = (Math.cos(Math.toRadians(angle)) * (strength * 0.9)) / 2 + 90;
			double y = (Math.sin(Math.toRadians(angle)) * (strength * 0.9)) / 2 + 90;
			myRef4.setValue((int) x);
			myRef5.setValue((int) y);
		});
		binding.switchFightMode.setOnClickListener(v ->
		{
			hadleModeSwitching();
		});

	}

	private void hadleModeSwitching ()
	{
		if (binding.switchFightMode.isChecked())
		{
			myRef8.setValue(1); // start tracking
			binding.joystickLeft.setVisibility(View.GONE);
			binding.joystickRight.setVisibility(View.GONE);
			binding.shootButton.setVisibility(View.GONE);
			commons.makeSnackbar(binding.getRoot(), "Tracking mode activated").show();
		}
		else
		{
			myRef8.setValue(0); // stop tracking
			myRef2.setValue(0); // set left stick to 0
			myRef3.setValue(0); // set left stick strength to 0
			myRef4.setValue(90);
			myRef5.setValue(90);
			binding.joystickLeft.setVisibility(View.VISIBLE);
			binding.joystickRight.setVisibility(View.VISIBLE);
			binding.shootButton.setVisibility(View.VISIBLE);
			commons.makeSnackbar(binding.getRoot(), "Tracking mode deactivated").show();
		}
	}

	/**
	 * Sends the fire-signal to the firebase database.
	 *
	 * @param event The event that triggered the fire-signal.
	 * @return true if the event was a down event, false otherwise.
	 */
	private boolean sendToFirebaseWhilePressed (MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				myRef6.setValue(1);
				break;
			case MotionEvent.ACTION_UP:
				myRef6.setValue(0);
				break;
		}
		return false;
	}
}