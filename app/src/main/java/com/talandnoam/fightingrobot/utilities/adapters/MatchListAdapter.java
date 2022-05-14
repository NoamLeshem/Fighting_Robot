package com.talandnoam.fightingrobot.utilities.adapters;

import android.content.Context;
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
import com.google.firebase.storage.StorageReference;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.Match;

import java.util.List;


public class MatchListAdapter extends ArrayAdapter<Match>
{
	private TextView headerTextView, matchWinner, matchDate, matchTime, matchType, matchFormat, matchResult;
	private Commons commons;
	private static final String TAG = "MatchListAdapter";

	public MatchListAdapter(Context context, List<Match> objects)
	{
		super(context, 0, objects);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		commons = new Commons(getContext());
//		if (convertView == null)
//			binding = ExpandableCardViewBinding.inflate(LayoutInflater.from(getContext()), parent, false);
//		convertView = binding.getRoot();
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
		Log.d(TAG, "getView: " + match.getId());
		StorageReference imageReference = FirebaseManager.getStorageRef("users/" + FirebaseManager.getUid() + "/matches/" + match.getId());
		imageReference.getDownloadUrl()
				.addOnSuccessListener(uri ->
						Glide
								.with(getContext())
								.load(uri)
								.centerCrop()
								.placeholder(R.drawable.ic_launcher_foreground)
								.into(matchImage));
		setValues(position, match);
		return convertView;
	}

	/**
	 * This method handles the expand and collapse of the hiddenView.
	 * <p>
	 *     If the CardView is already expanded, set its visibility
	 *     to gone and change the expand less icon to expand more.
	 *     <br>
	 *     If the CardView is not expanded, set its visibility
	 *     to visible and change the expand more icon to expand less.
	 * </p>
	 * @param cardView - the CardView that is clicked.
	 * @param arrow - the arrow button that is clicked.
	 * @param hiddenView - the hidden view that will be shown or hidden.
	 */
	private void handleExpandAndCollapse (CardView cardView, ImageView arrow, LinearLayout hiddenView)
	{
		commons.vibrate();
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
		matchWinner.setText(match.getWinner());
		matchDate.setText(match.getDate());
		matchTime.setText(match.getTime());
		matchType.setText(match.getType());
		matchFormat.setText(match.getFormat());
		matchResult.setText(match.getResult());
	}
}
