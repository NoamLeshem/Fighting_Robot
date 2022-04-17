package com.talandnoam.fightingrobot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.classes.TextValidator;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity
{
	private static final String TAG = "SignUpActivity";
	private TextInputLayout emailInputLayout, passwordInputLayout;
	private boolean isEmailValid, isPasswordValid, vibrationState;
	private LinearProgressIndicator progressIndicator;
	private Button emailPassSignUp, helpPassword;
	private EditText userEmail, userPassword;
	private int numberOfIncorrectAttempts;
	private TextView toLoginActivity;
	private Intent toMainActivity;
	private CheckBox showPassword;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		Objects.requireNonNull(getSupportActionBar()).hide();

		getViews();
		initVariables();
		handleSharedPreferences();
		setListeners();
	}

	private void getViews ()
	{
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

	private void initVariables ()
	{
		toMainActivity = new Intent(this, MainActivity.class);
	}

	private void handleSharedPreferences ()
	{
		PrefsManager prefsManager = new PrefsManager(this);
		int backgroundColor = prefsManager.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		findViewById(R.id.activity_sign_up).setBackgroundColor(getColor(backgroundColor));
	}

	private void setListeners ()
	{
		userEmail.addTextChangedListener(new TextValidator(userEmail)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(emailInputLayout, text,
					R.string.invalid_email, true);
			}
		});

		userPassword.addTextChangedListener(new TextValidator(userPassword)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(passwordInputLayout, text,
						R.string.invalid_password, false);
			}
		});

		helpPassword.setOnClickListener(this::showPasswordRules);
		showPassword.setOnCheckedChangeListener((compoundButton, isChecked) ->
				changePasswordState(isChecked));
		emailPassSignUp.setOnClickListener(view -> login());
		Intent toLogin = new Intent(this, LoginActivity.class);
		toLoginActivity.setOnClickListener(view -> Commons.activityLauncher(this, toLogin));
	}

	private void validateEmailOrPassword (TextInputLayout inputLayout, String text, int resourceID, boolean isEmail)
	{
		Commons.vibrate();
		if (isEmail) isEmailValid = isTextValidUsingRegex(text, true);
		else isPasswordValid = isTextValidUsingRegex(text, false);
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
		Commons.vibrate();
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
		Commons.vibrate();
		userPassword.setTransformationMethod(
				isChecked ?
						HideReturnsTransformationMethod.getInstance() :
						PasswordTransformationMethod.getInstance());
		userPassword.setSelection(userPassword.getText().length());
	}

	private void login ()
	{
		Commons.vibrate();
		emailPassSignUp.setEnabled(false);
		String emailAddress = userEmail.getText().toString().trim();
		String pass = userPassword.getText().toString().trim();
		progressIndicator.setVisibility(View.VISIBLE);
		createAccount(emailAddress, pass);
	}

	private void createAccount (String email, String password)
	{
		// [START create_user_with_email]
		FirebaseManager.getAuth().createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, task ->
		{
			if (task.isSuccessful())
			{
				// Sign in success, update UI with the signed-in user's information
				Log.d(TAG, "createUserWithEmail:success");
				FirebaseUser user = FirebaseManager.getAuth().getCurrentUser();
				updateUI(user);
			}
			else
			{
				// If sign in fails, display a message to the user.
				if (task.getException().getMessage()
						.equals("The email address is already in use by another account."))
					Snackbar.make(emailPassSignUp, R.string.email_occupied, Snackbar.LENGTH_LONG)
							.setAction("clear",view -> userEmail.setText(""))
							.show();
				Commons.showToast(R.string.auth_failed);
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
		Commons.activityLauncher(this, toMainActivity);
	}

	private void sendUserData (FirebaseUser user)
	{
		DatabaseReference myRef2 = FirebaseManager.getDataRef("users/" + user.getUid() + "/email");
		DatabaseReference myRef3 = FirebaseManager.getDataRef("processor/currentUser");
		myRef2.setValue(user.getEmail());
		myRef3.setValue(user.getUid());
	}
}
