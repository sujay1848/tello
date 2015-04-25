package com.nielsen.cloudapi.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.adapter.VideoListAdapter;

/**
 * Helper class to store and retrieve stream data into/from SQLite database for Android.
 * @author sanjankar
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// Shared preferences to be used across the app.
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;
	private SQLiteDatabase db;
	public final String TABLE_NAME_STREAMS_AND_HISTORY = "STREAMS_AND_HISTORY";
	private final String CREATE_DB_TABLES = "CREATE TABLE STREAMS_AND_HISTORY (ID INTEGER PRIMARY KEY, NAME TEXT,URL TEXT,RATING INT, LAST_PLAYED DATE)";
	private final String DROP_DB_TABLES = "DROP TABLE IF EXISTS STREAMS_AND_HISTORY";
	//private final String LOAD_HISTORY = "SELECT  * FROM " + TABLE_NAME_STREAMS_AND_HISTORY + " WHERE LAST_PLAYED IS NOT NULL";
	private final String LOAD_HISTORY = "SELECT NAME FROM " + TABLE_NAME_STREAMS_AND_HISTORY + " WHERE LAST_PLAYED IS NOT NULL ORDER BY LAST_PLAYED DESC";

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * Initializes the database the first time the app is installed. The values
	 * of stream URLs are loaded from property files to the DB.
	 * 
	 * @param ctx
	 *            Context of calling activity
	 */
	public void init(Context ctx) {
		Resources resources = ctx.getResources();
		prefs = ctx.getSharedPreferences(
				resources.getString(R.string.SHARED_PREFERENCES_KEY),
				Context.MODE_APPEND);
		if (!prefs.contains(resources.getString(R.string.DB_INITIALIZED_FLAG))) {
			loadStreamsToDatabase(ctx);
			edit = prefs.edit();
			edit.putBoolean(resources.getString(R.string.DB_INITIALIZED_FLAG), true);
			edit.commit();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DB_TABLES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_DB_TABLES);
		onCreate(db);
	}
	
	/**
	 * Loads the streams and stream data into the <code>STREAMS_AND_HISTORY</code> table for the first time.
	 * @param ctx 
	 */
	private void loadStreamsToDatabase(Context ctx) {
		try {
			MovieList movieList = new MovieList(ctx);
			int idx = 0;
			ContentValues cValues = new ContentValues(Global.SIZE_STREAMHISTORY);
			for (MovieItem movieItem : movieList.mList) {
				cValues.put(Global.COL_STREAMHISTORY_NAME, movieItem.getName());
				cValues.put(Global.COL_STREAMHISTORY_URL, movieItem.getUrl());
				cValues.put(Global.COL_STREAMHISTORY_ID, Integer.toString(idx++));
				cValues.put(Global.COL_STREAMHISTORY_RATING, Integer.toString(0));
				cValues.putNull(Global.COL_STREAMHISTORY_LAST_PLAYED);
				db = getWritableDatabase();
				db.insert(TABLE_NAME_STREAMS_AND_HISTORY, null, cValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts params into passed table. Params should be sent in pairs of Strings as Key and Value.
	 * @param tableName
	 * 	Name of the table to perform the insert on.
	 * @param values
	 * 	Key value pairs in form of {@link ContentValues}
	 */
	public void insertIntoTable(String tableName, ContentValues values) {
		db = getWritableDatabase();
		db.insert(tableName, null, values);
	}
	
	public void getDbStatus() {
		String selectQuery = "SELECT  * FROM " + TABLE_NAME_STREAMS_AND_HISTORY;
		Log.e("DBHelper", selectQuery);
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		if (c.moveToFirst()) {
			do {
				Log.d("Names: ", ((c.getString(c
						.getColumnIndex(Global.COL_STREAMHISTORY_NAME)))));
			} while (c.moveToNext());
		}
	}
	
	/**
	 * Returns name of the stream depending on the ID.</br>
	 * See {@link VideoListAdapter}
	 * @param id
	 * @return
	 */
	public String getNameById(int id) {
		String selectQuery = "SELECT  * FROM " + TABLE_NAME_STREAMS_AND_HISTORY + " WHERE ID = " + Integer.toString(id);
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		if (c.moveToFirst()) {
			do {
				return c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_NAME));
			} while (c.moveToNext());
		}
		return CREATE_DB_TABLES;
	}
	
	/**
	 * Update the LAST_PLAYED date for a stream.
	 * @param name
	 * 	Name of the stream that was played
	 */
	public void updateLastPlayed(String name) {
		String updateQuery = "UPDATE " + TABLE_NAME_STREAMS_AND_HISTORY + " SET LAST_PLAYED = datetime() WHERE " + Global.COL_STREAMHISTORY_NAME + " = \"" + name + "\"";
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(updateQuery);
	}
	
	public List<VideoStream> loadHistoryList() {
		List<VideoStream> historyList = new ArrayList<VideoStream>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(LOAD_HISTORY, null);
		if (c.moveToFirst()) {
			do {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				VideoStream stream;
				try {
					stream = new VideoStream(c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_NAME)),
							c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_URL)),
							Integer.parseInt(c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_RATING))),
							df.parse(c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_LAST_PLAYED))));
					historyList.add(stream);
				} catch (NumberFormatException e){
					e.printStackTrace();
					return null;
				}catch ( ParseException e ) {
					e.printStackTrace();
					return null;
				}
			} while (c.moveToNext());
		}
		return historyList;
	}
	
	public String[] getHistoryList() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(LOAD_HISTORY, null);
		String[] historyList = new String[c.getCount()];
		if (c.moveToFirst()) {
			int i = 0;
			do {
				historyList[i] = c.getString(c.getColumnIndex((Global.COL_STREAMHISTORY_NAME)));
				i++;
			} while (c.moveToNext());
		}
		return historyList;
	}
	
	/**
	 * Returns name of the stream depending on the ID.</br>
	 * See {@link VideoListAdapter}
	 * @param id
	 * @return
	 */
	public String[] getMovieNames() {
		String selectQuery = "SELECT NAME FROM " + TABLE_NAME_STREAMS_AND_HISTORY;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		String[] nameArray = new String[c.getCount()];
		if (c.moveToFirst()) {
			int i = 0;
			do {
				nameArray[i] = c.getString(c.getColumnIndex(Global.COL_STREAMHISTORY_NAME));
				i++;
			} while (c.moveToNext());
			return nameArray;
		}
		return null;
	}
}
