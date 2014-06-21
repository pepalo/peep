package com.pepalo.peep;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;

import com.pepalo.peep.gcm.Constants;

public class Common extends Application {
	
	public static final String PROFILE_ID = "profile_id";
	
	//parameters recognized by demo server
	public static final String FROM = "chatId";
	public static final String REG_ID = "regId";
	public static final String MSG = "msg";
	public static final String TO = "chatId2";	
	
	private static SharedPreferences prefs;
	public static String[] email_arr;

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		 List<String> emailList = getEmailList();
	        email_arr = emailList.toArray(new String[emailList.size()]);
	    
	}

    private List<String> getEmailList() {
    List<String> lst = new ArrayList<String>();
    Account[] accounts = AccountManager.get(this).getAccounts();
    for (Account account : accounts) {
        if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
            lst.add(account.name);
        }
    }
    return lst;
    }   
	
    
    public static String getPreferredEmail() {
        return prefs.getString("chat_email_id", email_arr.length==0 ? "" : email_arr[0]);
    }
     
    
    
	public static String getDisplayName() {
		return prefs.getString("display_name", "");
	}
	
	public static String getChatId() {
		return prefs.getString("chat_id", "");
	}
	public static void setChatId(String chatId) {
		prefs.edit().putString("chat_id", chatId).commit();
	}
	
	public static String getCurrentChat() {
		return prefs.getString("current_chat", null);
	}
	public static void setCurrentChat(String chatId) {
		prefs.edit().putString("current_chat", chatId).commit();
	}	
	
	public static boolean isNotify() {
		return prefs.getBoolean("notifications_new_message", true);
	}	
	
	public static String getRingtone() {
		return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
	}
	
	public static String getServerUrl() {
		return prefs.getString("server_url_pref", Constants.SERVER_URL);
		
	}
	
	public static String getSenderId() {
		return prefs.getString("sender_id_pref", Constants.SENDER_ID);
		
	}	
    	
}
