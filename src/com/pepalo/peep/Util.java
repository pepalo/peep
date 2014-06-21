package com.pepalo.peep;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;



public class Util  {
	
	

	public static boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
	
	public static void share(Context context, String chatId, boolean isGroup) {
		Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with it.
		if (isGroup) {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my group");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "My group ID is " + chatId);

		} else {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Invitation to chat");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "My chat ID is " + chatId);
		}
		
		context.startActivity(Intent.createChooser(sendIntent, "Invite via"));
	}
	
	
	public static String getContactDisplayNameByNumber(String number) {
		
		if(number.length()<4)
	    {
	    	
	    	return "Group_"+number;
	    }
		
		Context context = MainActivity.c;
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    //String name = "Contact_"+Long.toHexString(Double.doubleToLongBits(Math.random()));
	    String name = "Contact_"+number;

	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
	
	
	
	//function to get name from chat db
	public static String getchatname(String number) {
	
	    String name = ""+number;
	    
	    
	    Context context = MainActivity.c;
	   
	    Cursor c = context.getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE_FROM_CHATID,number), null, null, null, null);
	   
	    if (c.moveToFirst()) {
		
		name = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
		
		
	    }
	    
	    
	    return name;
	}
	
	
	public  static void clearNotif()
	{
		Context context = MainActivity.c;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}
	
	
     public static String getdpbynum(String number) {
		
		Context context = MainActivity.c;
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	   
	    String puri ="";

	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.PHOTO_URI }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToFirst();
	            if(contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_URI))!=null)
	            puri = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_URI));
	            
	            
	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return puri;
	}
}
