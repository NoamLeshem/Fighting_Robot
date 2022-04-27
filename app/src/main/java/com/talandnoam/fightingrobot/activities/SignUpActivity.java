package com.talandnoam.fightingrobot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.classes.TextValidator;
import com.talandnoam.fightingrobot.databinding.ActivitySignUpBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity
{
	private static final String TAG = "SignUpActivity";
	private ActivitySignUpBinding binding;
	private boolean isEmailValid, isPasswordValid;
	private int numberOfIncorrectAttempts;
	private Intent toMainActivity;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		binding = ActivitySignUpBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		getSupportActionBar().hide();

		initVariables();
		handleSharedPreferences();
		setListeners();
	}

	private void initVariables ()
	{
		toMainActivity = new Intent(this, MainActivity.class);
	}

	private void handleSharedPreferences ()
	{
		PrefsManager prefsManager = new PrefsManager(this);
		int backgroundColor = prefsManager
				.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		binding.activitySignUp
				.setBackgroundColor(getColor(backgroundColor));
	}

	private void setListeners ()
	{
		binding.username
				.addTextChangedListener(new TextValidator(binding.username)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(binding.textInputLayout, text,
					R.string.invalid_email, true);
			}
		});

		binding.password
				.addTextChangedListener(new TextValidator(binding.password)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(binding.textInputLayout1, text,
						R.string.invalid_password, false);
			}
		});

		binding.helpPassword
				.setOnClickListener(this::showPasswordRules);
		binding.showPassword
				.setOnCheckedChangeListener((compoundButton, isChecked) ->
						changePasswordState(isChecked));
		binding.userAndPasswordSignup
				.setOnClickListener(view -> login());
		Intent toLogin = new Intent(this, LoginActivity.class);
		binding.signUp
				.setOnClickListener(view ->
						Commons.activityLauncher(this, toLogin));
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
		binding.userAndPasswordSignup.setEnabled(isEmailValid && isPasswordValid);
		if(numberOfIncorrectAttempts > 10)
			binding.helpPassword.setVisibility(View.VISIBLE);
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
		binding.password.setTransformationMethod(
				isChecked ?
						HideReturnsTransformationMethod.getInstance() :
						PasswordTransformationMethod.getInstance());
		binding.password.setSelection(binding.password.getText().length());
	}

	private void login ()
	{
		Commons.vibrate();
		binding.userAndPasswordSignup.setEnabled(false);
		String emailAddress = binding.username.getText().toString().trim();
		String pass = binding.password.getText().toString().trim();
		binding.linearProgressIndicator.setVisibility(View.VISIBLE);
		createAccount(emailAddress, pass);
	}

	private void createAccount (String email, String password)
	{
		// [START create_user_with_email]
		FirebaseManager
				.getAuth()
				.createUserWithEmailAndPassword(email, password)
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
					Commons.makeSnackbar(binding.userAndPasswordSignup, R.string.email_occupied)
							.setAction("clear",view -> binding.username.setText(""))
							.show();
				Commons.showToast(R.string.auth_failed);
				binding.linearProgressIndicator.setVisibility(View.GONE);
				binding.userAndPasswordSignup.setEnabled(true);
				// signIn(email, password);
			}
		});
		// [END create_user_with_email]
	}

	private void updateUI (FirebaseUser user)
	{
		sendUserData(user);
		binding.linearProgressIndicator.setVisibility(View.GONE);
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
