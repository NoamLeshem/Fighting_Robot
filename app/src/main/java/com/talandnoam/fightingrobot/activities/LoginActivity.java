package com.talandnoam.fightingrobot.activities;

import android.app.Activity;
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

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.TextValidator;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity
{
	private static final String TAG = "LoginActivity", KEY_BACKGROUND = "background", KEY_VIBRATION = "vibration";
	private Button emailPassLogin, loginWithFacebook, helpPassword, googleButton;
	private TextInputLayout emailInputLayout, passwordInputLayout;
	boolean isEmailValid = false, isPasswordValid = false;
	private LinearProgressIndicator progressIndicator;
	private SharedPreferences sharedPreferences;
	private static final int RC_SIGN_IN = 9001;
	private FirebaseDatabase firebaseDatabase;
	private EditText userEmail, userPassword;
	private CallbackManager callbackManager;
	private int numberOfIncorrectAttempts;
	private TextView toSignUpActivity;
	private DatabaseReference myRef1;
	private boolean vibrationState;
	private CheckBox showPassword;
	private Intent toMainActivity;
	private FirebaseAuth mAuth;
	private Vibrator vibe;

	@Override
	public void onStart ()
	{
		super.onStart();

		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null)
		{
			activityLauncher(toMainActivity);
		}
	}

	private void activityLauncher (Intent intent)
	{
		vibrate();
		finish();
		startActivity(intent);
	}

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		getComponents();
		initializeFirebase();

		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.black);
		findViewById(R.id.activity_login).setBackgroundColor(getResources().getColor(backgroundColor, null));

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
		emailPassLogin.setOnClickListener(view -> login());
		googleButton.setOnClickListener(view -> googleLogin());
		loginWithFacebook.setOnClickListener(view -> facebookLogin());
		toSignUpActivity.setOnClickListener(view -> activityLauncher(new Intent(this, SignUpActivity.class)));
	}

	private void vibrate ()
	{
		if (vibrationState)
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
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
		emailPassLogin.setEnabled(isEmailValid && isPasswordValid);
		if(numberOfIncorrectAttempts > 10)
			helpPassword.setVisibility(View.VISIBLE);
	}

	private void initializeFirebase ()
	{
		mAuth = FirebaseAuth.getInstance();
		firebaseDatabase = FirebaseDatabase.getInstance();
		myRef1 = firebaseDatabase.getReference("users");
	}

	private void getComponents ()
	{
		sharedPreferences = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		vibrationState = sharedPreferences.getBoolean(KEY_VIBRATION, false);
		toMainActivity = new Intent(this, MainActivity.class);
		vibe = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		progressIndicator = findViewById(R.id.linearProgressIndicator);
		emailPassLogin = findViewById(R.id.user_and_password_login);
		passwordInputLayout = findViewById(R.id.textInputLayout1);
		emailInputLayout = findViewById(R.id.textInputLayout);
		loginWithFacebook = findViewById(R.id.login_button);
		callbackManager = CallbackManager.Factory.create();
		toSignUpActivity = findViewById(R.id.new_account);
		helpPassword = findViewById(R.id.help_password);
		showPassword = findViewById(R.id.show_password);
		googleButton = findViewById(R.id.google_button);
		userPassword = findViewById(R.id.password);
		userEmail = findViewById(R.id.username);
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
				.create()
				.show();
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

	private void changePasswordState (boolean isChecked)
	{
		vibrate();
		userPassword.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
		userPassword.setSelection(userPassword.getText().length());
	}

	private void login ()
	{
		vibrate();
		emailPassLogin.setEnabled(false);
		String emailAddress = userEmail.getText().toString().trim();
		String pass = userPassword.getText().toString().trim();
		progressIndicator.setVisibility(View.VISIBLE);
		loginWithEmailAndPass(emailAddress, pass);
	}

	private void loginWithEmailAndPass (String email, String password)
	{
		// [START sign_in_with_email]
		mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, this::handleTaskResult);
		// [END sign_in_with_email]
	}

	private void facebookLogin ()
	{
		vibrate();
		progressIndicator.setVisibility(View.VISIBLE);
		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>()
		{
			@Override
			public void onSuccess (LoginResult loginResult)
			{
				GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (object, response) ->
				{
					// Application code
					Log.d(TAG, "onSuccess: token" + Objects.requireNonNull(AccessToken.getCurrentAccessToken()));
				});
				Bundle parameters = new Bundle();
				parameters.putString("fields", "id,name,email");
				request.setParameters(parameters);
				request.executeAsync();
				handleFacebookAccessToken(loginResult.getAccessToken());
			}

			@Override
			public void onCancel () { Log.d(TAG, "facebook:onCancel"); }

			@Override
			public void onError (@NonNull FacebookException exception) { Log.w(TAG, "facebook:onError", exception); }
		});
	}

	private void googleLogin ()
	{
		vibrate();
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

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK)
		{
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try
			{
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
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

	private void handleFacebookAccessToken (AccessToken token)
	{
		Log.d(TAG, "handleFacebookAccessToken:" + token.getUserId());
		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		mAuth.signInWithCredential(credential).addOnCompleteListener(this, this::handleTaskResult);
	}

	private void firebaseAuthWithGoogle (String idToken)
	{
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		mAuth.signInWithCredential(credential).addOnCompleteListener(this, this::handleTaskResult);
	}

	private void handleTaskResult (Task<AuthResult> task)
	{
		if (task.isSuccessful()) // Sign in success, update UI with the signed-in user's information
			updateUI();
		else
		{
			// If sign in fails, display a message to the user.
			Log.w(TAG, "handleTaskResult:failure", task.getException());
			Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
			progressIndicator.setVisibility(View.GONE);
			if (Objects.requireNonNull(Objects.requireNonNull(task.getException())
					.getMessage())
					.equals("The password is invalid or the user does not have a password."))
				Snackbar.make(emailPassLogin, "Wrong password", Snackbar.LENGTH_LONG)
						.setAction("clear",view -> userPassword.setText(""))
						.show();
			emailPassLogin.setEnabled(true);
		}
	}

	private void updateUI ()
	{
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) sendUserData(user);
		progressIndicator.setVisibility(View.GONE);
		activityLauncher(toMainActivity);
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
