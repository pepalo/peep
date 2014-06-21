package com.pepalo.peep;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://com.pepalo.peep.provider/messages");
	public static final Uri CONTENT_URI_MESSAGES_CHAT = Uri.parse("content://com.pepalo.peep.provider/messages/chat");
	public static final Uri CONTENT_URI_PROFILE = Uri.parse("content://com.pepalo.peep.provider/profile");
	public static final Uri CONTENT_URI_PROFILE_FROM_CHATID = Uri.parse("content://com.pepalo.peep.provider/profile/chatid");

	public static final String COL_ID = "_id";
	
	public static final String TABLE_MESSAGES = "messages";
	public static final String COL_MSG = "msg";
	public static final String COL_FROM = "from_cid";
	public static final String COL_TO = "to_cid";
	public static final String COL_STAT = "status";
	public static final String COL_AT = "at";
	
	public static final String TABLE_PROFILE = "profile";
	public static final String COL_NAME = "name";
	public static final String COL_CHATID = "chat_id";
	public static final String COL_COUNT = "count";
	public static final String COL_ISGROUP = "is_group";
	
	private DbHelper dbHelper;
	
	private static final int MESSAGES_ALLROWS = 1;
	private static final int MESSAGES_SINGLE_ROW = 2;
	private static final int PROFILE_ALLROWS = 3;
	private static final int PROFILE_SINGLE_ROW = 4;
	private static final int PROFILE_SINGLE_ROW_FROM_CHATID = 5;
	private static final int MESSAGES_CHAT_ROW = 6;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.pepalo.peep.provider", "messages", MESSAGES_ALLROWS);
		uriMatcher.addURI("com.pepalo.peep.provider", "messages/#", MESSAGES_SINGLE_ROW);
		uriMatcher.addURI("com.pepalo.peep.provider", "messages/chat/#", MESSAGES_CHAT_ROW);
		uriMatcher.addURI("com.pepalo.peep.provider", "profile", PROFILE_ALLROWS);
		uriMatcher.addURI("com.pepalo.peep.provider", "profile/#", PROFILE_SINGLE_ROW);
		uriMatcher.addURI("com.pepalo.peep.provider", "profile/chatid/#", PROFILE_SINGLE_ROW_FROM_CHATID);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			qb.setTables(TABLE_MESSAGES);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			qb.setTables(TABLE_MESSAGES);
			qb.appendWhere("_id = " + uri.getLastPathSegment());
			break;

		case PROFILE_ALLROWS:
			qb.setTables(TABLE_PROFILE);
			break;			
			
		case PROFILE_SINGLE_ROW:
			qb.setTables(TABLE_PROFILE);
			qb.appendWhere("_id = " + uri.getLastPathSegment());
			break;
			
		case PROFILE_SINGLE_ROW_FROM_CHATID:             
			qb.setTables(TABLE_PROFILE);
			qb.appendWhere(COL_CHATID + " = " + uri.getLastPathSegment());
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);			
		}
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		long id;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			id = db.insertOrThrow(TABLE_MESSAGES, null, values);
			if (values.get(COL_TO) == null) {
				db.execSQL("update profile set count = count+1 where chat_id = ?", new Object[]{values.get(COL_FROM)});
				getContext().getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
			}
			break;
			
		case PROFILE_ALLROWS:
			id = db.insertOrThrow(TABLE_PROFILE, null, values);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		Uri insertUri = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(insertUri, null);
		return insertUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		int count;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			count = db.update(TABLE_MESSAGES, values, selection, selectionArgs);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			count = db.update(TABLE_MESSAGES, values, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;

		case PROFILE_ALLROWS:
			count = db.update(TABLE_PROFILE, values, selection, selectionArgs);
			break;			
			
		case PROFILE_SINGLE_ROW:
			count = db.update(TABLE_PROFILE, values, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);			
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		int count;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			count = db.delete(TABLE_MESSAGES, selection, selectionArgs);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			count = db.delete(TABLE_MESSAGES, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;

		case PROFILE_ALLROWS:
			count = db.delete(TABLE_PROFILE, selection, selectionArgs);
			break;			
			
		case PROFILE_SINGLE_ROW:
			count = db.delete(TABLE_PROFILE, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;
			
		case MESSAGES_CHAT_ROW:
			count = db.delete(TABLE_MESSAGES, "to_cid = ? or ( from_cid = ? and to_cid = ?)", new String[]{uri.getLastPathSegment(),uri.getLastPathSegment(), Common.getChatId()});
			//Log.d("count del", ""+count);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);			
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}	
	
	//--------------------------------------------------------------------------
	
	private static class DbHelper extends SQLiteOpenHelper {
		
		private static final String DATABASE_NAME = "peep.db";
		private static final int DATABASE_VERSION = 4;

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table messages (_id integer primary key autoincrement, msg text, from_cid text, to_cid text, status integer default 0, at datetime default current_timestamp);");
			db.execSQL("create table profile (_id integer primary key autoincrement, name text, chat_id text unique, count integer default 0, is_group integer default 0);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists messages");
			db.execSQL("drop table if exists profile");
			onCreate(db);
		}
	}
}
