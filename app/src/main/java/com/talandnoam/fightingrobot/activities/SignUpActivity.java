package com.talandnoam.fightingrobot.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.TextValidator;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity
{
	private static final String TAG = "SignUpActivity", KEY_VIBRATION = "vibration";
	private TextInputLayout emailInputLayout, passwordInputLayout;
	private static final String KEY_BACKGROUND = "background";
	private LinearProgressIndicator progressIndicator;
	private boolean isEmailValid, isPasswordValid;
	private Button emailPassSignUp, helpPassword;
	private SharedPreferences sharedPreferences;
	private FirebaseDatabase firebaseDatabase;
	private EditText userEmail, userPassword;
	private int numberOfIncorrectAttempts;
	private DatabaseReference myRef1;
	private TextView toLoginActivity;
	private Intent toMainActivity;
	private CheckBox showPassword;
	private FirebaseAuth mAuth;
	private Vibrator vibe;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		Objects.requireNonNull(getSupportActionBar()).hide();

		getComponents();
		initializeFirebase();
		handleSharedPreferences();
		setListeners();
	}

	private void getComponents ()
	{
		sharedPreferences = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		toMainActivity = new Intent(this, MainActivity.class);
		progressIndicator = findViewById(R.id.linearProgressIndicator);
		emailPassSignUp = findViewById(R.id.user_and_password_signup);
		passwordInputLayout = findViewById(R.id.textInputLayout1);
		emailInputLayout = findViewById(R.id.textInputLayout);
		showPassword = findViewById(R.id.show_password);
		helpPassword = findViewById(R.id.help_password);
		toLoginActivity = findViewById(R.id.sign_up);
		userPassword = findViewById(R.id.password);
		userEmail = findViewById(R.id.username);
	}

	private void initializeFirebase ()
	{
		mAuth = FirebaseAuth.getInstance();
		firebaseDatabase = FirebaseDatabase.getInstance();
		myRef1 = firebaseDatabase.getReference("users");
	}

	private void handleSharedPreferences ()
	{
		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.white);
		findViewById(R.id.activity_sign_up).setBackgroundColor(getResources().getColor(backgroundColor,  getResources().newTheme()));
	}

	private void setListeners ()
	{
		userEmail.addTextChangedListener(new TextValidator(userEmail)
		{
			@Override
			public void validate (TextView textView, String text)
			{ validateEmailOrPassword(emailInputLayout, text, R.string.invalid_email, true); }
		});

		userPassword.addTextChangedListener(new TextValidator(userPassword)
		{
			@Override
			public void validate (TextView textView, String text)
			{ validateEmailOrPassword(passwordInputLayout, text, R.string.invalid_password, false); }
		});

		helpPassword.setOnClickListener(this::showPasswordRules);
		showPassword.setOnCheckedChangeListener((compoundButton, isChecked) -> changePasswordState(isChecked));
		emailPassSignUp.setOnClickListener(view -> login());
		toLoginActivity.setOnClickListener(view -> activityLauncher(new Intent(this, LoginActivity.class)));
	}

	private void validateEmailOrPassword (TextInputLayout inputLayout, String text, int resourceID, boolean isEmail)
	{
		vibrate();
		if (isEmail)
			isEmailValid = isTextValidUsingRegex(text, true);
		else
			isPasswordValid = isTextValidUsingRegex(text, false);
		if (isTextValidUsingRegex(text, isEmail))
		{
			inputLayout.setError(null);
			numberOfIncorrectAttempts = 0;
		}
		else
		{
			inputLayout.setError(getString(resourceID));
			numberOfIncorrectAttempts++;
		}
		emailPassSignUp.setEnabled(isEmailValid && isPasswordValid);
		if(numberOfIncorrectAttempts > 10)
			helpPassword.setVisibility(View.VISIBLE);
	}

	private boolean isTextValidUsingRegex (String text, boolean isEmail)
	{
		String regex;
		if (isEmail)
			regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		else
			/*
            ^                                   # start of line
            (?=.*[0-9])                         # positive lookahead, digit [0-9]
            (?=.*[a-z])                         # positive lookahead, one lowercase character [a-z]
            (?=.*[A-Z])                         # positive lookahead, one uppercase character [A-Z]
            (?=.*[!@#&()–[{}]:;',?/*~$^+=<>])   # positive lookahead, one of the special character in this [..]
            .                                   # matches anything
            {8,20}                              # length at least 8 characters and maximum of 20 characters
            $                                   # end of line
            */
			regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		return matcher.matches();
	}

	private void showPasswordRules (View view)
	{
		vibrate();
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext());
		builder.setTitle(R.string.password_rules)
				.setMessage(getString(R.string.password_rule) + "!@#&()–[{}]:;',?/*~$^+=<>")
				.setNegativeButton("ok", (dialogInterface, i) -> {})
				.setIcon(R.drawable.ic_password)
				.setCancelable(true)
				.show();
	}

	private void changePasswordState (boolean isChecked)
	{
		vibrate();
		userPassword.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
		userPassword.setSelection(userPassword.getText().length());
	}

	private void vibrate ()
	{
		if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
	}

	private void login ()
	{
		vibrate();
		emailPassSignUp.setEnabled(false);
		String emailAddress = userEmail.getText().toString().trim();
		String pass = userPassword.getText().toString().trim();
		progressIndicator.setVisibility(View.VISIBLE);
		createAccount(emailAddress, pass);
	}

	private void createAccount (String email, String password)
	{
		// [START create_user_with_email]
		mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
		{
			if (task.isSuccessful())
			{
				// Sign in success, update UI with the signed-in user's information
				Log.d(TAG, "createUserWithEmail:success");
				FirebaseUser user = mAuth.getCurrentUser();
				updateUI(user);
			}
			else
			{
				// If sign in fails, display a message to the user.
				Log.w(TAG, "createUserWithEmail:failure\n\n" + Objects.requireNonNull(task.getException()).getMessage() + "\n\n", task.getException());
				if (Objects.requireNonNull(task.getException().getMessage()).equals("The email address is already in use by another account."))
					Snackbar.make(emailPassSignUp, "The email address is already in use by another account.", Snackbar.LENGTH_LONG)
							.setAction("clear",view -> userEmail.setText(""))
							.show();
				Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
				progressIndicator.setVisibility(View.GONE);
				emailPassSignUp.setEnabled(true);
				// signIn(email, password);
			}
		});
		// [END create_user_with_email]
	}

	private void updateUI (FirebaseUser user)
	{
		sendUserData(user);
		progressIndicator.setVisibility(View.GONE);
		activityLauncher(toMainActivity);
	}

	private void activityLauncher (Intent intent)
	{
		vibrate();
		finish();
		startActivity(intent);
	}

	private void sendUserData (FirebaseUser user)
	{
		DatabaseReference myRef2 = myRef1.child(Objects.requireNonNull(user.getUid()));
		DatabaseReference myRef3 = firebaseDatabase.getReference("processor/currentUser");
		DatabaseReference myRef4 = myRef2.child("email");
		myRef4.setValue(user.getEmail());
		myRef3.setValue(user.getUid());
	}
}
