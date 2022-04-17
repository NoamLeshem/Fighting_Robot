package com.talandnoam.fightingrobot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.Match;
import com.talandnoam.fightingrobot.classes.PrefsManager;

import java.util.Objects;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class FightActivity extends AppCompatActivity
{
	private DatabaseReference myRef2, myRef3, myRef4, myRef5, myRef6, hitRef;
	private static final String TAG = "FightActivity";
	private JoystickView joystickLeft, joystickRight;
	public static TextView matchScore;
	public TextView matchFormat;
	private Button shootButton;
	public static Match match;
	private WebView webView;
	public static int score;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fight);
		Objects.requireNonNull(getSupportActionBar()).hide();

		getViews();
		extractDataFromIntent();
		handleSharedPreferences();
		initializeFirebaseDirectory();
		setMatchScore();
		setListeners();
	}

	private void getViews ()
	{
		webView = findViewById(R.id.web_view);
		joystickLeft = findViewById(R.id.joystick_left);
		joystickRight = findViewById(R.id.joystick_right);
		shootButton = findViewById(R.id.shoot_button);
		matchFormat = findViewById(R.id.foramt);
		matchScore = findViewById(R.id.score);
	}

	private void extractDataFromIntent ()
	{
		Intent intent = getIntent();
		Gson gson = new Gson();
		String json = intent.getStringExtra("match");
		match = gson.fromJson(json, Match.class);
		matchFormat.setText(match.getFormat());
	}

	private void handleSharedPreferences ()
	{
		PrefsManager prefsManager = new PrefsManager(this);
		int backgroundColor = prefsManager.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		findViewById(R.id.activity_fight).setBackgroundColor(getColor(backgroundColor));
	}

	private void initializeFirebaseDirectory ()
	{
		hitRef = FirebaseManager.getDataRef("processor/isHit");
		DatabaseReference myRef1 = FirebaseManager.getDataRef("processor/controller");
		myRef2 = myRef1.child("leftStick/angle");
		myRef3 = myRef1.child("leftStick/strength");
		myRef4 = myRef1.child("rightStick/x");
		myRef5 = myRef1.child("rightStick/y");
		myRef6 = FirebaseManager.getDataRef("processor/laserEmitter");
	}

	private void setMatchScore ()
	{
		DatabaseReference myRef = FirebaseManager.getDataRef("users/" + FirebaseManager.getUid() + "/match_history/" + match.getId() + "/matchResult");
		myRef.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{ matchScore.setText(snapshot.getValue(String.class)); }

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled: " + error.getMessage()); }
		});
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setListeners ()
	{
//		hitRef.addValueEventListener(new ValueEventListener()
//		{
//			@Override
//			public void onDataChange (@NonNull DataSnapshot snapshot)
//			{ handleHitEvent(snapshot); }
//
//			@Override
//			public void onCancelled (@NonNull DatabaseError error)
//			{ Log.d(TAG, "onCancelled:     " + error.getMessage()); }
//		});
		shootButton.setOnTouchListener((v, event) ->
				FightActivity.this.sendToFirebaseWhilePressed(event));
		joystickLeft.setOnMoveListener((angle, strength) ->
		{
			myRef2.setValue(angle);
			myRef3.setValue(strength);
		});
		joystickRight.setOnMoveListener((angle, strength) ->
		{
			double x = Math.cos(Math.toRadians(angle)) * (strength * 0.9) + 90;
			double y = Math.sin(Math.toRadians(angle)) * (strength * 0.9) + 90;
			myRef4.setValue((int) x);
			myRef5.setValue((int) y);
		});
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl("http://192.168.1.27:8000/index.html"); // TODO: make it work correctly
	}

//	public void handleHitEvent (Context context, @NonNull DataSnapshot snapshot)
//	{
//		score += snapshot.getValue(Boolean.class) ? 1 : 0;
//		if (score == Integer.parseInt(match.getRoundsCap()))
//		{
//			notifyLostMessage(context);
//		}
//		matchScore.setText(score + " / " + match.getRoundsCap());
//	}

//	private void notifyLostMessage (Context context)
//	{
//		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
//		builder.setTitle(R.string.you_lost)
//				.setMessage(R.string.lose_messege)
//				.setNegativeButton(R.string.ok, (dialogInterface, i) ->
//						Commons.activityLauncher(this, new Intent(context, MainActivity.class)))
//				.setCancelable(false)
//				.show();
//		NotificationHelper.createNotification("You lost\nYou lost the match", context);
//	}

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