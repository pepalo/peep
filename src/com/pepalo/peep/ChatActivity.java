package com.pepalo.peep;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.ActionBar;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pepalo.peep.gcm.ServerUtilities;




public class ChatActivity extends FragmentActivity implements MessagesFragment.OnFragmentInteractionListener, EditContactDialog.OnFragmentInteractionListener {

	private EditText msgEdit;
	private String profileId;
	private String profileName;
	private String profileChatId;
	private boolean isGroup;
	EditText mEditEmojicon;
	LinearLayout emojicontainer;
	ImageButton emojibut;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		profileId = getIntent().getStringExtra(Common.PROFILE_ID);
		msgEdit = (EditText) findViewById(R.id.msg_edit);
		mEditEmojicon = (EditText) findViewById(R.id.msg_edit);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		
		
		Util.clearNotif();
		
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background3));
		
		Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
		if (c.moveToFirst()) {
			isGroup = c.getInt(c.getColumnIndex(DataProvider.COL_ISGROUP)) != 0;
			profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
			profileChatId = c.getString(c.getColumnIndex(DataProvider.COL_CHATID));
			actionBar.setTitle(profileName);
			actionBar.setSubtitle(profileChatId);
			if(isGroup)
				actionBar.setIcon(R.drawable.avatar_group);
			else
			{
				String pic= Util.getdpbynum(profileChatId);
			     
				 if (pic !="") {
					 try{
					 InputStream is = getContentResolver().openInputStream(Uri.parse(pic));
					 Drawable snap = Drawable.createFromStream(is, pic);
					 actionBar.setIcon(snap);}
					 catch(FileNotFoundException e){
						 actionBar.setIcon(R.drawable.avatar_contact);
					 }
				 }
				 else{
					 actionBar.setIcon(R.drawable.avatar_contact);
				 }
				
			}
		}
		
		
		
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		Common.setCurrentChat(profileChatId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		
		if (!isGroup) menu.findItem(R.id.action_share).setVisible(false);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			Util.share(this, profileChatId, isGroup);
			return true;		
		
		case R.id.action_edit:
			EditContactDialog dialog = new EditContactDialog();
			Bundle args = new Bundle();
			args.putString(Common.PROFILE_ID, profileId);
			args.putString(DataProvider.COL_NAME, profileName);
			dialog.setArguments(args);
			dialog.show(getFragmentManager(), "EditContactDialog");
			return true;
			
		case R.id.action_clear:
			getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES_CHAT, profileChatId), null, null);
			//finish();
			return true;	
			
		case R.id.action_delete:
			getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES_CHAT, profileChatId), null, null);
			getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null);
			finish();
			return true;			
			
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}
	

	   
	    
	    
	   

//	public void onClick(View v) {
//		switch(v.getId()) {
//		case R.id.send_btn:
//			String msg = msgEdit.getText().toString();
//			if (!TextUtils.isEmpty(msg)) {
//				send(msg);
//				msgEdit.setText(null);
//			}
//			break;
//		}
//	}
	
	
	public void sendMsg(View view){
		String msg = msgEdit.getText().toString();
		if (!TextUtils.isEmpty(msg)) {
			send(msg);
			msgEdit.setText(null);
		}
	}
	
	@Override
	public void onEditContact(String name) {
		getActionBar().setTitle(name);
	}	
	
	@Override
	public String getProfileChatId() {
		return profileChatId;
	}	
	
	private void send(final String txt) {
		
		ContentValues values = new ContentValues(2);
		values.put(DataProvider.COL_MSG, txt);
		values.put(DataProvider.COL_TO, profileChatId);
		Uri uri = getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
		final long id = ContentUris.parseId(uri);
		//Log.d("id",""+id);
		
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                	msg = ServerUtilities.send(txt, profileChatId);
                    
        			ContentValues values = new ContentValues(1);
        			values.put(DataProvider.COL_STAT, 1);
        			getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, String.valueOf(id)), values, null, null);
        			
                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	if (!TextUtils.isEmpty(msg)) {
            		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            	}
            }
        }.execute(null, null, null);		
	}	

	@Override
	protected void onPause() {
		ContentValues values = new ContentValues(1);
		values.put(DataProvider.COL_COUNT, 0);
		getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
		Common.setCurrentChat(null);
		super.onPause();
	}	

}
