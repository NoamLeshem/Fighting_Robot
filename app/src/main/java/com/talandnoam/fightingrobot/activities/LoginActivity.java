package com.talandnoam.fightingrobot.activities;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.core.splashscreen.SplashScreen;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.LanguageManager;
import com.talandnoam.fightingrobot.classes.PrefsManager;
import com.talandnoam.fightingrobot.classes.TextValidator;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity
{
	private static final String TAG = "LoginActivity";
	private Button emailPassLogin, loginWithFacebook, helpPassword, googleButton;
	private TextInputLayout emailInputLayout, passwordInputLayout;
	boolean isEmailValid = false, isPasswordValid = false;
	private LinearProgressIndicator progressIndicator;
	private static final int RC_SIGN_IN = 9001;
	private EditText userEmail, userPassword;
	private CallbackManager callbackManager;
	private int numberOfIncorrectAttempts;
	private TextView toSignUpActivity;
	private CheckBox showPassword;
	private Intent toMainActivity;

	@Override
	public void onStart ()
	{
		super.onStart();

		// Check if user is signed in (non-null) and update UI accordingly.
		if (FirebaseManager.isSignedIn())
			Commons.activityLauncher(this , toMainActivity);
	}

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		startSplashScreen();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		getViews();
		initVariables();
		handleSharedPreferences();
		setListeners();
	}

	private void startSplashScreen ()
	{
		// Handle the splash screen transition.
		SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
		splashScreen.setKeepOnScreenCondition(() ->
		{
			// Keep the splash screen on as long as
			// the user is not interacting with the app.
			return false;
		});
	}

	private void getViews ()
	{
		progressIndicator = findViewById(R.id.linearProgressIndicator);
		emailPassLogin = findViewById(R.id.user_and_password_login);
		passwordInputLayout = findViewById(R.id.textInputLayout1);
		emailInputLayout = findViewById(R.id.textInputLayout);
		loginWithFacebook = findViewById(R.id.login_button);
		toSignUpActivity = findViewById(R.id.new_account);
		helpPassword = findViewById(R.id.help_password);
		showPassword = findViewById(R.id.show_password);
		googleButton = findViewById(R.id.google_button);
		userPassword = findViewById(R.id.password);
		userEmail = findViewById(R.id.username);
	}

	private void initVariables ()
	{
		toMainActivity = new Intent(this, MainActivity.class);
		callbackManager = CallbackManager.Factory.create();
	}

	private void handleSharedPreferences ()
	{
		PrefsManager prefsManager = new PrefsManager(this);
		int backgroundColor = prefsManager.getPrefInt(PrefsManager.KEY_BACKGROUND, R.color.black);
		findViewById(R.id.activity_login).setBackgroundColor(getColor(backgroundColor));
		LanguageManager languageManager = new LanguageManager(this);
		languageManager.setLanguage(prefsManager.getPrefString(PrefsManager.KEY_LANGUAGE, "en"));
	}

	private void setListeners ()
	{
		userEmail.addTextChangedListener(new TextValidator(userEmail)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(
						emailInputLayout,
						text,
						R.string.invalid_email,
						true);
			}
		});

		userPassword.addTextChangedListener(new TextValidator(userPassword)
		{
			@Override
			public void validate (TextView textView, String text)
			{
				validateEmailOrPassword(
						passwordInputLayout,
						text,
						R.string.invalid_password,
						false);
			}
		});
		Intent toSignUp = new Intent(this, SignUpActivity.class);
		toSignUpActivity.setOnClickListener(view ->
				Commons.activityLauncher(this, toSignUp));
		showPassword.setOnCheckedChangeListener((compoundButton, isChecked) ->
				changePasswordState(isChecked));
		loginWithFacebook.setOnClickListener(view -> facebookLogin());
		helpPassword.setOnClickListener(this::showPasswordRules);
		googleButton.setOnClickListener(view -> googleLogin());
		emailPassLogin.setOnClickListener(view -> login());
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
		emailPassLogin.setEnabled(isEmailValid && isPasswordValid);
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
		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(view.getContext());
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
		emailPassLogin.setEnabled(false);
		String emailAddress = userEmail.getText().toString().trim();
		String pass = userPassword.getText().toString().trim();
		progressIndicator.setVisibility(View.VISIBLE);
		loginWithEmailAndPass(emailAddress, pass);
	}

	private void loginWithEmailAndPass (String email, String password)
	{
		// [START sign_in_with_email]
		FirebaseManager.getAuth()
				.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, this::handleTaskResult);
		// [END sign_in_with_email]
	}

	private void facebookLogin ()
	{
		Commons.vibrate();
		progressIndicator.setVisibility(View.VISIBLE);
		LoginManager.getInstance()
				.logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

		LoginManager.getInstance()
				.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
		{
			@Override
			public void onSuccess (LoginResult loginResult)
			{ handleFacebookAccessToken(loginResult.getAccessToken()); }

			@Override
			public void onCancel () { Log.d(TAG, "facebook:onCancel"); }

			@Override
			public void onError (@NonNull FacebookException exception)
			{ Log.w(TAG, "facebook:onError", exception); }
		});
	}

	private void handleFacebookAccessToken (AccessToken token)
	{
		Log.d(TAG, "handleFacebookAccessToken:" + token.getUserId());
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		FirebaseManager.getAuth()
				.signInWithCredential(credential)
				.addOnCompleteListener(this, this::handleTaskResult);
	}

	private void googleLogin ()
	{
		Commons.vibrate();
		progressIndicator.setVisibility(View.VISIBLE);
		// [START config_sign_in]
		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions
				.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.idToken))
				.requestEmail()
				.build();
		GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
		// [END config_sign_in]

		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent
		// from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK)
		{
			Task<GoogleSignInAccount> task =
					GoogleSignIn.getSignedInAccountFromIntent(data);
			try
			{
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				firebaseAuthWithGoogle(account.getIdToken());
			}
			catch (ApiException e)
			{
				// Google Sign In failed, update UI appropriately
				progressIndicator.setVisibility(View.GONE);
				Log.w(TAG, "Google sign in failed", e);
			}
		}
		callbackManager.onActivityResult(requestCode, resultCode, data);
		progressIndicator.setVisibility(View.GONE);
		Log.d(TAG, "onActivityResult: data = " + data.getExtras().toString());
	}

	private void firebaseAuthWithGoogle (String idToken)
	{
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		FirebaseManager.getAuth()
				.signInWithCredential(credential)
				.addOnCompleteListener(this, this::handleTaskResult);
	}

	private void handleTaskResult (Task<AuthResult> task)
	{
		if (task.isSuccessful()) // Sign in success, update UI with the signed-in user's information
			updateUI(FirebaseManager.getAuth().getCurrentUser());
		else
		{
			// If sign in fails, display a message to the user.
			Log.w(TAG, "handleTaskResult:failure", task.getException());
			Commons.showToast("Authentication failed.");
			progressIndicator.setVisibility(View.GONE);
			if (Objects.requireNonNull(Objects.requireNonNull(task.getException())
					.getMessage())
					.equals("The password is invalid or the user does not have a password."))
				Snackbar.make(emailPassLogin, R.string.wrong_password, Snackbar.LENGTH_LONG)
						.setAction(R.string.clear, view -> userPassword.setText(""))
						.show();
			emailPassLogin.setEnabled(true);
		}
	}

	private void updateUI (FirebaseUser user)
	{
		progressIndicator.setVisibility(View.GONE);
		if (user != null) sendUserData(user);
	}

	private void sendUserData (FirebaseUser user)
	{
		DatabaseReference myRef2 = FirebaseManager.getDataRef("users/" + user.getUid() + "/email");
		DatabaseReference myRef3 = FirebaseManager.getDataRef("processor/currentUser");
		myRef2.setValue(user.getEmail());
		myRef3.setValue(user.getUid());
		Commons.activityLauncher(this, toMainActivity);
	}
}
