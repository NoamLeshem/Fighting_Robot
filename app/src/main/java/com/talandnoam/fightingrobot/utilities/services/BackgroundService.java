package com.talandnoam.fightingrobot.utilities.services;

import static android.app.PendingIntent.getActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.talandnoam.fightingrobot.R;
import com.talandnoam.fightingrobot.activities.FightActivity;
import com.talandnoam.fightingrobot.activities.MainActivity;
import com.talandnoam.fightingrobot.classes.Commons;
import com.talandnoam.fightingrobot.classes.FirebaseManager;
import com.talandnoam.fightingrobot.classes.NotificationHelper;

public class BackgroundService extends Service
{
	private static final String TAG = "BackgroundService";

	@Nullable
	@Override
	public IBinder onBind (Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId)
	{
		Log.d("FOREGROUND_SERVICE", "Start foreground service.");
		if (intent == null)
			return super.onStartCommand(null, flags, startId);
		switch (intent.getAction())
		{
			case "ACTION_START_FOREGROUND_SERVICE":
				startForegroundService();
				Commons.showToast("Foreground service is started.");
				break;
			case "ACTION_STOP_FOREGROUND_SERVICE":
				stopForegroundService();
				Commons.showToast("Foreground service is stopped.");
				break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void startForegroundService ()
	{
		Log.d("FOREGROUND_SERVICE", "Start foreground service.");
		DatabaseReference hitRef = FirebaseManager.getDataRef("processor/isHit");
		hitRef.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot)
			{ handleHitEvent(snapshot); }

			@Override
			public void onCancelled (@NonNull DatabaseError error)
			{ Log.d(TAG, "onCancelled:     " + error.getMessage()); }
		});

		String name = "Fighting Robot";
		String CHANNEL_ID = "fighting_robot_channel_id";
		int importance = NotificationManager.IMPORTANCE_HIGH;
		Intent dismissIntent = new Intent();
		dismissIntent.setAction("action_view");

		PendingIntent pendingIntentDismiss;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
			pendingIntentDismiss = getActivity(this, 3, dismissIntent, PendingIntent.FLAG_MUTABLE);
		else
			pendingIntentDismiss = getActivity(this, 3, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent intent = new Intent(getApplicationContext(), FightActivity.class);
		PendingIntent pendingIntent;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
			pendingIntent = getActivity(this, 3, intent, PendingIntent.FLAG_MUTABLE);
		else
			pendingIntent = getActivity(this, 3, intent, PendingIntent.FLAG_ONE_SHOT);
		NotificationCompat.Builder notification = new NotificationCompat
				.Builder(this, CHANNEL_ID)
				.setContentTitle("Shake Detector Running")
				.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", pendingIntentDismiss)
				.setContentIntent(pendingIntent)
				.setContentText("Click Dismiss to stop the App");

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
		notificationManager.createNotificationChannel(mChannel);
		startForeground(1, notification.build());
	}

	private void handleHitEvent (@NonNull DataSnapshot snapshot)
	{
		FightActivity.score += snapshot.getValue(Boolean.class) ? 1 : 0;
		Log.d(TAG, "handleHitEvent:     " + snapshot.getValue(Boolean.class));
		if (FightActivity.score == Integer.parseInt(FightActivity.match.getRoundsCap()))
		{
			notifyLostMessage();
		}
		NotificationHelper.createNotification("you got hit",this);
		FightActivity.matchScore.setText(FightActivity.score + " / " + FightActivity.match.getRoundsCap());
	}

	private void notifyLostMessage ()
	{
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(R.string.you_lost)
				.setMessage(R.string.lose_messege)
				.setNegativeButton(R.string.ok, (dialogInterface, i) ->
						Commons.activityLauncher(getApplicationContext(), new Intent(this, MainActivity.class)))
				.setCancelable(false)
				.show();
		NotificationHelper.createNotification( "You Lost!\nYou lost the match.\n", this);
	}

	private void stopForegroundService ()
	{
		Log.d("FOREGROUND_SERVICE", "Stop foreground service.");
		// Stop foreground service and remove the notification.
		stopForeground(true);
		// Stop the foreground service.
		stopSelf();
	}
}
