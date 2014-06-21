package com.pepalo.peep;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pepalo.peep.gcm.ServerUtilities;

public class CreateGroupTask extends AsyncTask<Void, Void, String> {
	
	private Context mContext;
	private ProgressDialog pd;
	
	public CreateGroupTask(Context mContext) {
		super();
		this.mContext = mContext; 
	}
	
	@Override
	protected void onPreExecute() {
		pd = ProgressDialog.show(mContext, null, "Creating group...");
	}

	@Override
	protected String doInBackground(Void... params) {
		String chatId = ServerUtilities.create();
		if (chatId == null) return null;
		
		try {
			ContentValues values = new ContentValues(2);
			values.put(DataProvider.COL_NAME, "Group_"+chatId);
			values.put(DataProvider.COL_CHATID, chatId);
			values.put(DataProvider.COL_ISGROUP, 1);
			mContext.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
		} catch (SQLException sqle) {}
		
		return chatId;
	}	

	@Override
	protected void onCancelled() {
		pd.dismiss();
	}

	@Override
	protected void onPostExecute(String result) {
		pd.dismiss();
		
		if (result != null) {
			Toast.makeText(mContext, "Group created " + result, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "Group creation failed. Please retry later.", Toast.LENGTH_LONG).show();
		}
	}

}
