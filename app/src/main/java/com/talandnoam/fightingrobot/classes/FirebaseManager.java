package com.talandnoam.fightingrobot.classes;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class FirebaseManager
{
	private static FirebaseAuth mAuth;
	private static FirebaseDatabase mDatabase;
	private static  FirebaseStorage mStorage;

	// Private constructor to prevent instantiation
	private FirebaseManager() {
		throw new UnsupportedOperationException();
	}

	public static FirebaseAuth getAuth ()
	{
		if (mAuth == null)
			mAuth = FirebaseAuth.getInstance();
		return mAuth;
	}

	public static FirebaseDatabase getDatabase ()
	{
		if (mDatabase == null)
			mDatabase = FirebaseDatabase.getInstance();
		return mDatabase;
	}

	public static DatabaseReference getDataRef (String path)
	{
		return getDatabase().getReference(path);
	}

	public static FirebaseStorage getStorage()
	{
		if (mStorage == null)
			mStorage = FirebaseStorage.getInstance();
		return mStorage;
	}

	public static StorageReference getStorageRef (String path)
	{
		return getStorage().getReference(path);
	}

	public static void signOut ()
	{
		getAuth().signOut();
	}

	public static boolean isSignedIn ()
	{
		return getAuth().getCurrentUser() != null;
	}

	public static String getUid ()
	{
		return getAuth().getUid();
	}
}
