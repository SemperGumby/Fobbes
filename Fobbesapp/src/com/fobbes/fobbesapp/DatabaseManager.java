package com.fobbes.fobbesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager {
	/*
	 * General class for all database access
	 */

	private static final String DATABASE_NAME = "FobbesApp";
	private static final int DATABASE_VERSION = 1;

	//Pre-built strings for table creation
	private static final String pollsCreate = "create table Polls (_id integer primary key autoincrement, "
			+ "name text not null, " + "schedule integer not null);";
	//TODO: See if color is used anywhere in the program; delete it and update the database if it's not
	private static final String entriesCreate = "create table Entries (_id integer primary key autoincrement, "
			+ "name text not null, "
			+ "type integer not null, "
			+ "unit text, "
			+ "min integer, "
			+ "max integer, "
			+ "color integer not null, "
			+ "schedule integer not null, "
			+ "poll_id integer not null references Polls(_id) on update cascade on delete cascade);";
	private static final String inputsCreate = "create table Inputs (_id integer primary key autoincrement, "
			+ "value real not null, "
			+ "timestamp text not null, "
			+ "note text, "
			+ "entry_id integer not null references Entries(_id) on update cascade on delete cascade);";

	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DatabaseManager(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);

	}
	public long addPoll(String pollName, int schedule) {
		// Insert into Polls table
		// Returns -1 if there is an sqlite error
		if (db.query("Polls", new String[]{"name"}, "name='" + pollName + "'", null, null, null,
				null).getCount() != 0) {
			return -1;
		}
		ContentValues initialValues = new ContentValues();
		initialValues.put("name", pollName);
		initialValues.put("schedule", schedule);
		return db.insert("Polls", null, initialValues);
	}

	public long addItem(String name, int type, String unit, int min, int max, int schedule,
			long poll_id, int color) {
		// poll_id is the parent poll's id
		// Insert into Entries table
		// Returns -1 if there is an sqlite error
		ContentValues initialValues = new ContentValues();
		initialValues.put("name", name);
		initialValues.put("type", type);
		initialValues.put("unit", unit);
		initialValues.put("min", min);
		initialValues.put("max", max);
		initialValues.put("schedule", schedule);
		initialValues.put("poll_id", poll_id);
		initialValues.put("color", color);
		return db.insert("Entries", null, initialValues);

	}

	public long addInput(double value, String timestamp, String note, long entry_id) {
		// entry_id is the parent entry's id
		// Insert into Inputs table
		// Returns -1 if there is an sqlite error
		ContentValues initialValues = new ContentValues();
		initialValues.put("value", value);
		initialValues.put("timestamp", timestamp);
		initialValues.put("note", note);
		initialValues.put("entry_id", entry_id);
		return db.insert("Inputs", null, initialValues);
	}

	public long addInput(int value, String timestamp, String note, long entry_id) {
		// entry_id is the parent entry's id
		// Insert into Inputs table, converting int value to real for storage
		// Returns -1 if there is an sqlite error
		ContentValues initialValues = new ContentValues();
		initialValues.put("value", (double) value);
		initialValues.put("timestamp", timestamp);
		initialValues.put("note", note);
		initialValues.put("entry_id", entry_id);
		return db.insert("Inputs", null, initialValues);
	}

	public Cursor getPoll(long rowID) {
		// Return a single row of the Polls table
		Cursor mCursor = db.query(true, "Polls", new String[]{"_id", "name", "schedule"}, "_id"
				+ "=" + rowID, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getAllPolls() {
		// Return all rows of the Polls table
		return db.query("Polls", new String[]{"_id", "name", "schedule"}, null, null, null, null,
				null);
	}

	public Cursor getItem(int rowID) {
		// Return a single row of the Entries table
		Cursor mCursor = db.query(true, "Entries", new String[]{"_id", "name", "type", "unit",
				"min", "max", "color", "schedule", "poll_id"}, "_id" + "=" + rowID, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getAllItems() {
		// Return all rows of the Entries table
		return db.query("Entries", new String[]{"_id", "name", "type", "unit", "min", "max",
				"color", "schedule", "poll_id"}, null, null, null, null, null);
	}

	public Cursor getAllPollsItems(long pollID) {
		// Get all entries that belong to the poll with the given ID
		return db.query("Entries", new String[]{"_id", "name", "type", "unit", "min", "max",
				"color", "schedule", "poll_id"}, "poll_id=" + pollID, null, null, null, null);
	}

	public Cursor getInput(long rowID) {
		// Return one row of Inputs table with given ID
		Cursor mCursor = db.query(true, "Inputs", new String[]{"_id", "value", "timestamp", "note",
				"entry_id"}, "_id" + "=" + rowID, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getAllInputs() {
		// Return all rows of the Inputs table
		return db.query("Inputs", new String[]{"_id", "value", "timestamp", "note", "entry_id"},
				null, null, null, null, null);
	}

	public Cursor getAllItemsInputs(long entryID) {
		// Get all input that belongs to the item with the given ID
		return db.query("Inputs", new String[]{"_id", "value", "timestamp", "note", "entry_id"},
				"entry_id=" + entryID, null, null, null, null);
	}

	public long updatePoll(long pollID, String name, int sched) {
		// Update the row of Polls with the given ID
		// name and sched are used as new values
		// Returns 0 if no rows are updated
		ContentValues newValues = new ContentValues();
		newValues.put("name", name);
		newValues.put("schedule", sched);
		return db.update("Polls", newValues, "_id=" + pollID, null);
	}

	public long updateItem(long entryID, String name, String unit, int min, int max, int sched) {
		// Update the row of Entries with the given ID
		// Returns 0 if no rows are updated
		ContentValues newValues = new ContentValues();
		newValues.put("name", name);
		newValues.put("unit", unit);
		newValues.put("min", min);
		newValues.put("max", max);
		newValues.put("schedule", sched);
		//newValues.put("type", datatype);
		return db.update("Entries", newValues, "_id=" + entryID, null);
	}

	public long updateInput(long inputID, double value, String note) {
		// Update the row of Inputs with the given ID
		// Returns 0 if no rows are updated
		ContentValues newValues = new ContentValues();
		newValues.put("value", value);
		newValues.put("note", note);
		return db.update("Inputs", newValues, "_id=" + inputID, null);
	}

	public long updateInput(long inputID, int value, String note) {
		// Update the row of Inputs with the given ID
		// Returns 0 if no rows are updated
		ContentValues newValues = new ContentValues();
		newValues.put("value", (double) value);
		newValues.put("note", note);
		return db.update("Inputs", newValues, "_id=" + inputID, null);
	}

	public long deletePoll(long pollID) {
		// Deletes given row
		// Returns 0 if nothing is deleted
		Cursor itemCursor = getAllPollsItems(pollID);
		itemCursor.moveToFirst();
		if (itemCursor.getCount() > 0){
			do{
				Cursor entriesCursor = getAllItemsInputs(Long.parseLong(itemCursor.getString(0)));
				entriesCursor.moveToFirst();
				if(entriesCursor.getCount() > 0){
					do{
						deleteInput(Long.parseLong(entriesCursor.getString(0)));
					}while(entriesCursor.moveToNext());
				}
				deleteItem(Long.parseLong(itemCursor.getString(0)));
			}while(itemCursor.moveToNext());
		}
		return db.delete("Polls", "_id=" + pollID, null);
	}

	public long deleteItem(long entryID) {
		// Deletes given row
		// Returns 0 if nothing is deleted
		return db.delete("Entries", "_id=" + entryID, null);
	}

	public long deleteInput(long inputID) {
		// Deletes given row
		// Returns 0 if nothing is deleted
		return db.delete("Inputs", "_id=" + inputID, null);
	}
	// ---opens the database---
	public DatabaseManager open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}
	// ---closes the database---
	public void close() {
		DBHelper.close();
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Try creating initial tables
			Log.e("DB","Trying to create tables...");
			db.execSQL(pollsCreate);

			db.execSQL(entriesCreate);

			db.execSQL(inputsCreate);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("DB", "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS titles");
			onCreate(db);
		}
	}

	// Exports string used to recreate a poll and its items
	// TODO: Fold these methods into a db importer/exporter (2 classes mehbeh?)
	public String getPollCreateString(long pollid) {
		String query = "";
		Cursor pollCursor = getPoll(pollid);
		pollCursor.moveToFirst();
		if (pollCursor.getCount() == 0)
			return null;
		query = query + pollCursor.getString(1) + ",";
		Cursor itemsCursor = getAllPollsItems(pollid);
		itemsCursor.moveToFirst();
		if (itemsCursor.getCount() == 0)
			return null;

		for (int p = 0; p < itemsCursor.getCount(); p++) {
			for (int i = 1; i < 9; i++) {
				if (i != 8)
					query = query + itemsCursor.getString(i) + "|";
			}
			query = query + ",";
			itemsCursor.moveToNext();
			Log.i(null, query);
		}
		return query;
	}

	// Imports a string, created by getPollCreateString, to recreate a poll
	// Returns true if insert succeeds
	public boolean importPoll(String importString) {
		String[] splitPoll = importString.split(",");
		Log.i(null, splitPoll[0]);

		long pollid = addPoll(splitPoll[0], 0);

		for (int p = 1; p < splitPoll.length; p++) {

			String[] splitString = splitPoll[p].split("\\|");
			Log.i(null, splitString[0]
					+ splitString[1]
					+ splitString[2]
					+ splitString[3]
					+ splitString[4]
					+ splitString[6]
					+ splitString[5]);
			addItem(splitString[0],
					Integer.parseInt(splitString[1]),
					splitString[2],
					Integer.parseInt(splitString[3]),
					Integer.parseInt(splitString[4]),
					Integer.parseInt(splitString[6]),
					pollid,
					Integer.parseInt(splitString[5]));
			
			//RAW  0:id 1:String name 2:int type 3:String unit 4:int min 5:int max  6:int schedule 7:long poll_id 8:int color
		}

		return true;
	}
}