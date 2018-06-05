package com.example.recordvoice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by vieta on 28/6/2016.
 */
public class DatabaseHandle extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "record_call";

    // Contacts table name
    private static final String TABLE_RECORD = "record1";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_DATE = "date";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_TYPE_CALL = "type_call";

    //Whitelist table
    public static String CREATE_WHITELIST_TABLE = "CREATE TABLE whitelist( _id INTEGER PRIMARY KEY, contact_id TEXT, record INTEGER )";
    public static String WHITELIST_TABLE = "whitelist";
    public static String WHITELIST_TABLE_ID = "_id"; // only because of https://developer.android.com/reference/android/widget/CursorAdapter.html
    public static String WHITELIST_TABLE_CONTACT_ID = "contact_id";
    public static String WHITELIST_TABLE_RECORD = "record";

    //Call Record Table
    String CREATE_CALL_RECORDS_TABLE = "CREATE TABLE records(_id INTEGER PRIMARY KEY, phone_number TEXT, outgoing INTEGER, start_date_time INTEGER, end_date_time INTEGER, path_to_recording TEXT, keep INTEGER DEFAULT 0, backup_state INTEGER DEFAULT 0 )";
    public static String CALL_RECORDS_TABLE = "records";
    public static String CALL_RECORDS_TABLE_ID = "_id"; // only because of https://developer.android.com/reference/android/widget/CursorAdapter.html
    public static String CALL_RECORDS_TABLE_PHONE_NUMBER = "phone_number";
    public static String CALL_RECORDS_TABLE_OUTGOING = "outgoing";
    public static String CALL_RECORDS_TABLE_START_DATE = "start_date_time";
    public static String CALL_RECORDS_TABLE_END_DATE = "end_date_time";
    public static String CALL_RECORDS_TABLE_RECORDING_PATH = "path_to_recording";
    public static String CALL_RECORDS_TABLE_KEEP = "keep";
    public static String CALL_RECORDS_BACKUP_STATE = "backup_state";

    String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_RECORD + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_PHONE_NUMBER + " TEXT, "
            + KEY_DATE + " TEXT, " + KEY_FILE_NAME + " TEXT, "+ KEY_TYPE_CALL + " INTEGER )";

    private static DatabaseHandle instance;

    public static synchronized DatabaseHandle getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandle(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHandle(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_WHITELIST_TABLE);
        db.execSQL(CREATE_CALL_RECORDS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if(oldVersion==2 && newVersion==3){
            db.execSQL(CREATE_WHITELIST_TABLE);
            db.execSQL(CREATE_CALL_RECORDS_TABLE);
        }*/
    }

    /**
     * Lop Database dung de khoi tao, add them, load cac record
     */

    // Adding new contact
    public void addHistory(RecordCall recordCall) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, recordCall.getPhoneNumber());
        values.put(KEY_DATE, recordCall.getDate());
        values.put(KEY_FILE_NAME, recordCall.getFileName());
        values.put(KEY_TYPE_CALL, recordCall.getTypeCall());
        // Inserting Row
        db.insert(TABLE_RECORD, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Record
    public List<RecordCall> getAllRecord() {
        List<RecordCall> recordCalls = new ArrayList<RecordCall>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECORD + " ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RecordCall recordCall = new RecordCall(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
                recordCall.setId(cursor.getInt(0));
                // Adding contact to list
                recordCalls.add(recordCall);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return record list
        return recordCalls;
    }

    // Getting All Contacts incall or outcall
    public List<RecordCall> getAllRecordCondition(int type) {
        List<RecordCall> recordCalls = new ArrayList<RecordCall>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECORD + " WHERE "+KEY_TYPE_CALL +" = "+ type +" ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RecordCall recordCall = new RecordCall(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
                recordCall.setId(cursor.getInt(0));
                // Adding contact to list
                recordCalls.add(recordCall);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return recordCalls;
    }

    // Getting contacts Count
    public int getListCount() {
        String countQuery = "SELECT  * FROM " + TABLE_RECORD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public void delete(RecordCall recordCall) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("LOgMain",recordCall.getId()+"/m");
        db.delete(TABLE_RECORD, KEY_ID + " = ?",
                new String[]{String.valueOf(recordCall.getId())});
        db.close();
    }


    private synchronized Whitelist getWhitelistFrom(Cursor cursor) {
        Whitelist whitelist = new Whitelist();

        int index = cursor.getColumnIndex(DatabaseHandle.WHITELIST_TABLE_ID);
        whitelist.getContent().put(DatabaseHandle.WHITELIST_TABLE_ID, cursor.getInt(index));

        index = cursor.getColumnIndex(DatabaseHandle.WHITELIST_TABLE_CONTACT_ID);
        whitelist.getContent().put(DatabaseHandle.WHITELIST_TABLE_CONTACT_ID, cursor.getString(index));

        index = cursor.getColumnIndex(DatabaseHandle.WHITELIST_TABLE_RECORD);
        whitelist.getContent().put(DatabaseHandle.WHITELIST_TABLE_RECORD, cursor.getString(index));

        return whitelist;
    }


    public synchronized ArrayList<Whitelist> getAllWhitelist() {
        ArrayList<Whitelist> array_list = new ArrayList<Whitelist>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.WHITELIST_TABLE, null);
            String[] columns = cursor.getColumnNames();

            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Whitelist contact = getWhitelistFrom(cursor);
                array_list.add(contact);
                cursor.moveToNext();
            }
            return array_list;
        } finally {
            db.close();
        }
    }

    public synchronized Whitelist getContact(String contactId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.WHITELIST_TABLE + " where " + DatabaseHandle.WHITELIST_TABLE_CONTACT_ID + " = " + contactId + "", null);
            if (!cursor.moveToFirst()) return null; // does not exist
            return getWhitelistFrom(cursor);
        } finally {
            db.close();
        }
    }


    public synchronized boolean addWhitelist(Whitelist contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.insert(DatabaseHandle.WHITELIST_TABLE, null, contact.getContent());
            return true;
        } finally {
            db.close();
        }
    }


    public synchronized void removeWhiteList(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            db.execSQL("Delete from " + DatabaseHandle.WHITELIST_TABLE + " where " + DatabaseHandle.WHITELIST_TABLE_ID + "=" + id + "");
        } finally {
            db.close();
        }
    }

    public synchronized static boolean isWhitelisted(Context context, String phoneNumber) {
        // define the columns the query should return
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID};
        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
            // Found the callers... now check the whitelist
            Whitelist whitelist = getInstance(context).getContact(contactId);
            return (null != whitelist);
        }
        return false;
    }

    private CallLog getCallLogFrom(Cursor cursor) {
        CallLog phoneCall = new CallLog();
        phoneCall.isNew = false;

        // String[] columnNames = cursor.getColumnNames();

        int index = cursor.getColumnIndex(CALL_RECORDS_TABLE_ID);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_ID, cursor.getInt(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_PHONE_NUMBER);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_PHONE_NUMBER, cursor.getString(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_OUTGOING);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_OUTGOING, cursor.getInt(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_START_DATE);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_START_DATE, cursor.getLong(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_END_DATE);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_END_DATE, cursor.getLong(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_RECORDING_PATH);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_RECORDING_PATH, cursor.getString(index));

        index = cursor.getColumnIndex(CALL_RECORDS_TABLE_KEEP);
        phoneCall.getContent().put(CALL_RECORDS_TABLE_KEEP, cursor.getInt(index));

        index = cursor.getColumnIndex(CALL_RECORDS_BACKUP_STATE);
        phoneCall.getContent().put(CALL_RECORDS_BACKUP_STATE, cursor.getInt(index));

        return phoneCall;
    }


    public synchronized ArrayList<CallLog> getAllCalls() {
        ArrayList<CallLog> array_list = new ArrayList<CallLog>();

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.CALL_RECORDS_TABLE, null);
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                CallLog phoneCall = getCallLogFrom(cursor);
                array_list.add(phoneCall);
                cursor.moveToNext();
            }
            return array_list;
        } finally {
            db.close();
        }
    }


    public synchronized ArrayList<CallLog> getAllCalls(boolean outgoing) {
        ArrayList<CallLog> array_list = new ArrayList<CallLog>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.CALL_RECORDS_TABLE + " where " + DatabaseHandle.CALL_RECORDS_TABLE_OUTGOING + "=" + (outgoing ? "1" : "0"), null);
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                CallLog phoneCall = getCallLogFrom(cursor);
                array_list.add(phoneCall);
                cursor.moveToNext();
            }
            return array_list;
        } finally {
            db.close();
        }
    }

    public synchronized boolean addCall(CallLog phoneCall) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            if (phoneCall.isNew) {
                long rowId = db.insert(DatabaseHandle.CALL_RECORDS_TABLE, null, phoneCall.getContent());
                // rowID is and Alias for _ID  see: http://www.sqlite.org/autoinc.html
                phoneCall.getContent().put(DatabaseHandle.CALL_RECORDS_TABLE_ID, rowId);
            } else {
                db.update(DatabaseHandle.CALL_RECORDS_TABLE, phoneCall.getContent(), CALL_RECORDS_TABLE_ID + "=" + phoneCall.getId(), null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public synchronized boolean updateCall(CallLog phoneCall) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.update(DatabaseHandle.CALL_RECORDS_TABLE, phoneCall.getContent(), "id = ?", new String[]{Integer.toString(phoneCall.getId())});
            return true;
        } finally {
            db.close();
        }
    }

    public synchronized int count() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            int numRows = (int) DatabaseUtils.queryNumEntries(db, DatabaseHandle.CALL_RECORDS_TABLE);
            return numRows;
        } finally {
            db.close();
        }
    }

    public synchronized CallLog getCall(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.CALL_RECORDS_TABLE + " where " + DatabaseHandle.CALL_RECORDS_TABLE_ID + "=" + id, null);
            if (!cursor.moveToFirst()) return null; // does not exist
            return getCallLogFrom(cursor);
        } finally {
            db.close();
        }
    }

    public synchronized void removeCall(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from " + DatabaseHandle.CALL_RECORDS_TABLE + " where " + DatabaseHandle.CALL_RECORDS_TABLE_ID + "=" + id, null);
            if (!cursor.moveToFirst()) return; // doesn't exist
            CallLog call = getCallLogFrom(cursor);
            String path = call.getPathToRecording();
            try {
                if (null != path)
                    new File(path).delete();
            } catch (Exception e) {

            }
            db.execSQL("Delete from " + DatabaseHandle.CALL_RECORDS_TABLE + " where " + DatabaseHandle.CALL_RECORDS_TABLE_ID + "=" + id);
        } finally {
            db.close();
        }
    }


    public synchronized void removeAllCalls(boolean includeKept) {
        final ArrayList<CallLog> allCalls = getAllCalls();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (CallLog call : allCalls) {
                if (includeKept || !call.isKept()) {
                    try {
                        new File(call.getPathToRecording()).delete();
                    } catch (Exception e) {
                    }
                    try {
                        db.execSQL("Delete from " + DatabaseHandle.CALL_RECORDS_TABLE + " where " + DatabaseHandle.CALL_RECORDS_TABLE_ID + "=" + call.getId());
                    } catch (Exception e) {
                    }
                }
            }
            // db.delete(Database.CALL_RECORDS_TABLE, null, null);
        } finally {
            db.close();
        }
    }

}
