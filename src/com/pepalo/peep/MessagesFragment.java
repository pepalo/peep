package com.pepalo.peep;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat dformat = new SimpleDateFormat("dd MMM yyyy");
	private static final SimpleDateFormat tformat = new SimpleDateFormat("h:mm a");
	
	private OnFragmentInteractionListener mListener;
	private SimpleCursorAdapter adapter;
	private Date now;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		now = new Date();
		
		//this.getView().setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
		
		
		adapter = new SimpleCursorAdapter(getActivity(), 
				R.layout.chat_list_item, 
				null, 
				new String[]{DataProvider.COL_FROM,DataProvider.COL_MSG, DataProvider.COL_AT,DataProvider.COL_STAT }, 
				new int[]{R.id.text1, R.id.text2, R.id.text3,R.id.imgstat},
				0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				String from = cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM));
				String to = cursor.getString(cursor.getColumnIndex(DataProvider.COL_TO));
				String stat = cursor.getString(cursor.getColumnIndex(DataProvider.COL_STAT));
				
				switch(view.getId()) {
				case R.id.text1:
					TextView fromText = (TextView) view;
					fromText.setText(cursor.getString(columnIndex)+":");
					if (from == null || Common.getCurrentChat().equals(from) ) //myself or contact 
						fromText.setVisibility(View.GONE);
					else
					{
						
						fromText.setText(Util.getchatname(cursor.getString(columnIndex))+":");
						fromText.setVisibility(View.VISIBLE);
					}
					return true;	
					
				case R.id.text2:
					LinearLayout parent = (LinearLayout) view.getParent();
					LinearLayout root = (LinearLayout) parent.getParent();
					if (from == null) {//myself
						root.setGravity(Gravity.RIGHT);
						root.setPadding(50, 10, 10, 10);
						
						//GradientDrawable background = (GradientDrawable) root.getBackground();
						//background.setColor(Color.parseColor("#334342"));
						//parent.setBackgroundResource(R.drawable.right);
					} else {
						root.setGravity(Gravity.LEFT);
						root.setPadding(10, 10, 50, 10);
						//parent.setBackgroundResource(R.drawable.left);
					}
					break;
					
				case R.id.text3:
					TextView timeText = (TextView) view;
					timeText.setText(getDisplayTime(cursor.getString(columnIndex)));
					return true;
					
				case R.id.imgstat:
					//Log.d("stat value",stat);
					ImageView img = (ImageView) view;
					if (stat.equals("1"))
						img.setImageResource(R.drawable.message_got_receipt_from_server);
					if (stat.equals("0"))
						img.setImageResource(R.drawable.message_unsent);
					else if(stat.equals("3"))
					  img.setVisibility(View.GONE);
						
					
					return true;
					
								
				}
				return false;
			}
		});		
		
		setListAdapter(adapter);
		
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setDivider(null);
		
		Bundle args = new Bundle();
		args.putString(DataProvider.COL_CHATID, mListener.getProfileChatId());
		getLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		public String getProfileChatId();
	}
	
	@SuppressWarnings("deprecation")
	private String getDisplayTime(String datetime) {
		
		try{
		SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date parsed = sourceFormat.parse(datetime); // => Date is in UTC now

		TimeZone tz = TimeZone.getDefault();
		SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		destFormat.setTimeZone(tz);

		String result = destFormat.format(parsed);
		
		
		
		Date dt = sdf.parse(result);
		if (now.getYear()==dt.getYear() && now.getMonth()==dt.getMonth() && now.getDate()==dt.getDate()) {
			return tformat.format(dt);
		}
		return dformat.format(dt);
		}catch (Exception e) {
	     }
	        return "";
		    
		    
	}
	
	//----------------------------------------------------------------------------

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String profileChatId = args.getString(DataProvider.COL_CHATID);
		CursorLoader loader = new CursorLoader(getActivity(), 
				DataProvider.CONTENT_URI_MESSAGES, 
				null, 
				DataProvider.COL_TO + " = ? or (" + DataProvider.COL_FROM + " = ? and " + DataProvider.COL_TO + " = ?)",
				new String[]{profileChatId, profileChatId, Common.getChatId()}, 
				DataProvider.COL_AT + " ASC"); 
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		this.setSelection(adapter.getCount());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
