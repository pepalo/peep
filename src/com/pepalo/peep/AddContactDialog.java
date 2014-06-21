package com.pepalo.peep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddContactDialog extends DialogFragment {	

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Context ctx = getActivity();
		
		final EditText et = new EditText(ctx);
		et.setHint(Common.getChatId());
		
		final AlertDialog alert = new AlertDialog.Builder(ctx)
			.setTitle("Enter Phone Number or Group ID")
			.setView(et)
			.setPositiveButton(android.R.string.ok, null)
			.setNegativeButton(android.R.string.cancel, null)
			.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				Button okBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String chatId = et.getText().toString().trim();
						if (TextUtils.isEmpty(chatId)) {
							et.setError("Phone Number or Group ID is required");
							return;
						} else if (chatId.length()<2) {
							et.setError("Invalid Phone Number or Group ID");
							return;							
						}
						
						try {
							ContentValues values = new ContentValues(2);
							values.put(DataProvider.COL_NAME, Util.getContactDisplayNameByNumber(chatId));
							values.put(DataProvider.COL_CHATID, chatId);
							values.put(DataProvider.COL_ISGROUP, chatId.length()==2);
							ctx.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
						} catch (SQLException sqle) {}
						
						alert.dismiss();
					}
				});
			}
		});
		
		return alert;
	}	
	
	
	
}
