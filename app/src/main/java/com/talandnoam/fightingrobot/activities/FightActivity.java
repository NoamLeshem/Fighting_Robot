package com.talandnoam.fightingrobot.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.talandnoam.fightingrobot.R;

import java.util.Objects;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class FightActivity extends AppCompatActivity
{
	private DatabaseReference myRef2, myRef3, myRef4, myRef5, myRef6;
	private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
	private static final String KEY_BACKGROUND = "background", TAG = "FightActivity";
	private JoystickView joystickLeft, joystickRight;
	private String matchIDString, userIDString;
	private TextView matchFormat, matchScore;
	private DatabaseReference hitRef;
	private Button shootButton;
	private int score;

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

	private void handleSharedPreferences ()
	{
		SharedPreferences sharedPreferences = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.black);
		findViewById(R.id.activity_fight).setBackgroundColor(getResources().getColor(backgroundColor, null));
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setListeners ()
	{
		hitRef.addValueEventListener(new ValueEventListener(){
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{
				score += snapshot.getValue(Boolean.class) ? 1 : 0;
				if (score == 10)
				{
					MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(FightActivity.this);
					builder.setTitle("You lost!")
							.setMessage("You lost the match! hahaha Loser cry about it:D")
							.setNegativeButton("ok", (dialogInterface, i) ->
							{
								startActivity(new Intent(FightActivity.this, MainActivity.class));
								finish();
							})
							.setCancelable(true)
							.show();
				}
				matchScore.setText(score + " / 10");
			}

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled:     " + error.getMessage()); }
		});
		shootButton.setOnTouchListener((v, event) -> FightActivity.this.sendToFirebaseWhilePressed(event));

		joystickLeft.setOnMoveListener((angle, strength) ->
		{
			myRef2.setValue(angle);
			myRef3.setValue(strength);
		});

		joystickRight.setOnMoveListener((angle, strength) ->
		{
			double x = Math.cos(Math.toRadians(angle)) * (strength * 0.9);
			double y = Math.sin(Math.toRadians(angle)) * (strength * 0.9);
			myRef4.setValue((int) x);
			myRef5.setValue((int) y);
		});
	}

	private void setMatchScore ()
	{
		DatabaseReference myRef = firebaseDatabase.getReference("users/" + userIDString + "/match_history/" + matchIDString + "/matchResult");
		myRef.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{ matchScore.setText(snapshot.getValue(String.class)); }

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled:     " + error.getMessage()); }
		});
	}

	private void extractDataFromIntent ()
	{
		Intent intent = getIntent();
		String matchFormatString = intent.getStringExtra("matchFormat");
		matchIDString = intent.getStringExtra("matchID");
		userIDString = intent.getStringExtra("userID");
		matchFormat.setText(matchFormatString);
	}

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

	private void getViews ()
	{
		joystickLeft = findViewById(R.id.joystick_left);
		joystickRight = findViewById(R.id.joystick_right);
		shootButton = findViewById(R.id.shoot_button);
		matchFormat = findViewById(R.id.foramt);
		matchScore = findViewById(R.id.score);
	}

	private void initializeFirebaseDirectory ()
	{
		hitRef = firebaseDatabase.getReference("processor/isHit");
		DatabaseReference myRef1 = firebaseDatabase.getReference("processor/controller");
		myRef2 = myRef1.child("leftStick/angle");
		myRef3 = myRef1.child("leftStick/strength");
		myRef4 = myRef1.child("rightStick/x");
		myRef5 = myRef1.child("rightStick/y");
		myRef6 = firebaseDatabase.getReference("processor/laserEmitter");
	}


}