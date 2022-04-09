package com.talandnoam.fightingrobot.fragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.classes.Match;
import com.talandnoam.fightingrobot.utilities.adapters.MatchListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment
{
	private static final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
	private static final String KEY_BACKGROUND = "background";
	private SwipeRefreshLayout mySwipeRefreshLayout;
	private static final String TAG = "HistoryFragment";

	private ListView listView;
	boolean isListFull = false;
	MatchListAdapter adapter;

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;


	public HistoryFragment ()
	{
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment HistoryFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static HistoryFragment newInstance (String param1, String param2)
	{
		HistoryFragment fragment = new HistoryFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
		{
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		final View rootView = inflater.inflate(R.layout.fragment_history, container, false);
		mySwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);

		/*
		 * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
		 * performs a swipe-to-refresh gesture.
		 */
		mySwipeRefreshLayout.setOnRefreshListener(this::myUpdateOperation);

		int backgroundColor = sharedPreferences.getInt(KEY_BACKGROUND, R.color.black);
		rootView.setBackgroundColor(getResources().getColor(backgroundColor, null));

		listView = rootView.findViewById(R.id.matches_list);
		List<Match> allMatchesList = new ArrayList<>();
		adapter = new MatchListAdapter(this.getContext(), allMatchesList);
		inflateListView(listView);
		extractAllMatches(allMatchesList);
		return rootView;
	}
	private void extractAllMatches (List<Match> allMatchesList)
	{
		isListFull = false;
		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/" + mAuth.getUid() + "/match_history");
		databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{
				for (DataSnapshot matchSnapshot : snapshot.getChildren())
					allMatchesList.add(matchSnapshot.getValue(Match.class));
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled (@NonNull DatabaseError error) {}
		});
	}

	private void myUpdateOperation ()
	{
		// This method performs the actual data-refresh operation.
		// The method calls setRefreshing(false) when it's finished.
		// Do an update
		// extractAllMatches(allMatchesList);
		Log.i(TAG, "Performing update");
		mySwipeRefreshLayout.setRefreshing(false);
	}

	private void inflateListView (ListView listView)
	{
		listView.setAdapter(adapter);
		listView.setDivider(null);
		listView.setDividerHeight(0);
	}
}