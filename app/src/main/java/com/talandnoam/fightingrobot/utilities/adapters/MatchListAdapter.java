package com.talandnoam.fightingrobot.utilities.adapters;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Match;

import java.util.List;

public class MatchListAdapter extends ArrayAdapter<Match>
{
	private static final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
	private static final Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
	private TextView headerTextView, matchId, matchWinner, matchDate, matchTime, matchType, matchFormat, matchResult;
	private static final String TAG = "MatchListAdapter" ,KEY_VIBRATION = "vibration";
	private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
	private Context context;

	public MatchListAdapter(Context context, List<Match> objects)
	{
		super(context, 0, objects);
		this.setContext(context);
	}

	private void setContext (Context context)
	{
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.expandable_card_view, parent, false);

		Match match = getItem(position);

		findViews(convertView);
		CardView cardView = convertView.findViewById(R.id.base_card_view);
		ImageView arrow = convertView.findViewById(R.id.arrow_button);
		LinearLayout hiddenView = convertView.findViewById(R.id.hidden_view);
		ImageView matchImage = convertView.findViewById(R.id.icon);
		cardView.setOnClickListener(view -> handleExpandAndCollapse(cardView, arrow, hiddenView));
		arrow.setOnClickListener(view -> handleExpandAndCollapse(cardView, arrow, hiddenView));
		Log.d(TAG, "getView: " + match.getMatchId());
		StorageReference imageReference = FirebaseStorage.getInstance().getReference("users/" + mAuth.getUid() + "/matches/" + match.getMatchId());
		View finalConvertView = convertView;
		imageReference.getDownloadUrl()
				.addOnSuccessListener(uri ->
						Glide
								.with(finalConvertView)
								.load(uri)
								.centerCrop()
								.placeholder(R.drawable.ic_launcher_foreground)
								.into(matchImage));
		setValues(position, match);
		return convertView;
	}

	private void handleExpandAndCollapse (CardView cardView, ImageView arrow, LinearLayout hiddenView)
	{
		if (sharedPreferences.getBoolean(KEY_VIBRATION, false))
			vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));

		// If the CardView is already expanded, set its visibility
		// to gone and change the expand less icon to expand more.
		if (hiddenView.getVisibility() == View.VISIBLE)
			handleAnimation(cardView, arrow, hiddenView, View.GONE, R.drawable.ic_arrow_down);
		// If the CardView is not expanded, set its visibility
		// to visible and change the expand more icon to expand less.
		else
			handleAnimation(cardView, arrow, hiddenView, View.VISIBLE, R.drawable.ic_arrow_up);
	}

	private void handleAnimation (CardView cardView, ImageView arrow, LinearLayout hiddenView, int visibility, int direction)
	{
		// The transition of the hiddenView is carried out by the TransitionManager class.
		// Here we use an object of the AutoTransition Class to create a default transition.
		TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
		hiddenView.setVisibility(visibility);
		arrow.setImageResource(direction);
	}

	private void findViews (View convertView)
	{
		headerTextView = convertView.findViewById(R.id.heading);
		matchId = convertView.findViewById(R.id.match_id);
		matchWinner = convertView.findViewById(R.id.match_winner);
		matchDate = convertView.findViewById(R.id.match_date);
		matchTime = convertView.findViewById(R.id.match_time);
		matchType = convertView.findViewById(R.id.match_type);
		matchFormat = convertView.findViewById(R.id.match_format);
		matchResult = convertView.findViewById(R.id.match_result);
	}

	private void setValues (int position, Match match)
	{
		headerTextView.setText("Match #" + (position + 1));
		matchId.setText("Match ID: " + match.getMatchId());
		matchWinner.setText("Match Winner: " + match.getMatchWinner());
		matchDate.setText("Match Date: " + match.getMatchDate());
		matchTime.setText("Match Time: " + match.getMatchTime());
		matchType.setText("Match Type: " + match.getMatchType());
		matchFormat.setText("Match Format: " + match.getMatchFormat());
		matchResult.setText("Match Result: " + match.getMatchResult());
	}
}
