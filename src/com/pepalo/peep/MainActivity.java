package com.pepalo.peep;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pepalo.peep.gcm.GcmListener;
import com.pepalo.peep.gcm.GcmUtil;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, GcmListener {
	
	private SimpleCursorAdapter adapter;
	public static Context c;    
	private GcmUtil gcmUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		c = this.getApplicationContext();
		Util.clearNotif();
		
		adapter = new SimpleCursorAdapter(this, 
				R.layout.main_list_item, 
				null, 
				new String[]{DataProvider.COL_NAME, DataProvider.COL_COUNT,DataProvider.COL_CHATID}, 
				new int[]{R.id.text1, R.id.text2 ,  R.id.avatar},
				0);
	
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch(view.getId()) {
				case R.id.text2:
					int count = cursor.getInt(columnIndex);
					if (count > 0) {
						((TextView)view).setText(String.format("%d new message%s", count, count==1 ? "" : "s"));
					}
					else
						((TextView)view).setText(String.format("no new messages"));
					return true;	
				    case R.id.avatar:
				     ImageView avatar = (ImageView)view;
				     String chatid = cursor.getString(columnIndex);
				     
				     if(chatid.length()>3){
				     String pic= Util.getdpbynum(chatid);
				    				     
					 if (pic !="") {
						avatar.setImageURI(Uri.parse(pic));
					 }
					 else{
						 avatar.setImageResource(R.drawable.avatar_contact);
					 }
				     }
				     else
				    	 avatar.setImageResource(R.drawable.avatar_group);
				     
				     
					return true;
				}
				return false;
			}
		});
		
		setListAdapter(adapter);
		
		if(Common.getChatId()=="")
		{
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			new AlertDialog.Builder(MainActivity.this)
		    .setTitle("Login to Peep")
		    .setMessage("enter your mobile number")
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		           
		            Common.setChatId(input.getText().toString());
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	Intent i = getBaseContext().getPackageManager()
		                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
		             i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(i);
		        }
		    }).show();
			
			
		}
		
		gcmUtil = new GcmUtil(getApplicationContext());
		connect();
		
		getLoaderManager().initLoader(0, null, this);
		
	}
	
	private void connect() {
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(Common.getChatId());
		actionBar.setSubtitle("connecting...");
		
		if (!TextUtils.isEmpty(Common.getServerUrl()) && !TextUtils.isEmpty(Common.getSenderId()) && gcmUtil.register(this)) {
			onRegister(true);
		} else {
			onRegister(false);
		}		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onRegister(boolean status) {
		if (status) {
			getActionBar().setTitle(Common.getChatId());
			getActionBar().setSubtitle("online");
		} else {
			getActionBar().setSubtitle("offline");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			Util.share(this, Common.getChatId(), false);
			return true;		
		
		case R.id.action_add:
			AddContactDialog dialog = new AddContactDialog();
			dialog.show(getFragmentManager(), "AddContactDialog");
			return true;
			
		case R.id.action_create:
			new CreateGroupTask(this).execute();
			return true;			
			
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Common.PROFILE_ID, String.valueOf(id));
		startActivity(intent);
	}	

	@Override
	protected void onDestroy() {
		gcmUtil.cleanup();
		super.onDestroy();
	}	
	
	//----------------------------------------------------------------------------

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = new CursorLoader(this, 
				DataProvider.CONTENT_URI_PROFILE, 
				new String[]{DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_COUNT,DataProvider.COL_CHATID}, 
				null, 
				null, 
				DataProvider.COL_COUNT + " DESC, " + DataProvider.COL_ID + " DESC"); 
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}	
	
	public Context getContext(){
		return this.getApplicationContext();
   }
	
	


}
