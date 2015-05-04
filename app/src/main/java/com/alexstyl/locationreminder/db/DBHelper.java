package com.alexstyl.locationreminder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;

import com.alexstyl.locationreminder.DeLog;
import com.alexstyl.locationreminder.entity.StoredReminder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by alexstyl on 23/02/15.</p>
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reminders.db";


    private Context mContext;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context.getApplicationContext();
    }

    private static DBHelper sInstance;

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
        }
        return sInstance;
    }

    private static final String NULLABLE = null;
    // --- SQL Queries ---

    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_INT = " INTEGER";
    private static final String COMMA_SEP = ", ";

    private static final String SQL_CREATE_REMINDERS =
            "CREATE TABLE " + RemindersContract.TABLE_NAME + " (" +
                    RemindersContract._ID + TEXT_INT + COMMA_SEP
                    + RemindersContract.NOTE + TEXT_TYPE + COMMA_SEP
                    + RemindersContract.LONGTITUDE + TEXT_TYPE + COMMA_SEP
                    + RemindersContract.LATITUDE + TEXT_TYPE + COMMA_SEP
                    + RemindersContract.DATE_MOTIFIED + TEXT_INT + COMMA_SEP
                    + RemindersContract.ADDRESS + TEXT_TYPE + COMMA_SEP

                    + "PRIMARY KEY (" + RemindersContract._ID + ")"
                    + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing for now
    }

    public SQLiteDatabase getDatabase() {
        synchronized (sInstance) {
            return this.getWritableDatabase();
        }
    }

    /**
     * Returns a list of all stored reminders
     *
     * @return
     */
    public ArrayList<StoredReminder> getAllReminders() {
        ArrayList<StoredReminder> mReminders = new ArrayList<>();
        synchronized (sInstance) {
            Cursor cur = null;
            try {
                cur = getDatabase().query(RemindersContract.TABLE_NAME, null, null, null, null, null, RemindersContract._ID);
                int col_id = cur.getColumnIndex(RemindersContract._ID);
                int col_note = cur.getColumnIndex(RemindersContract.NOTE);
                int col_lat = cur.getColumnIndex(RemindersContract.LATITUDE);
                int col_log = cur.getColumnIndex(RemindersContract.LONGTITUDE);
                int col_date = cur.getColumnIndex(RemindersContract.DATE_MOTIFIED);
                int col_address = cur.getColumnIndex(RemindersContract.ADDRESS);

                while (cur.moveToNext()) {
                    long id = cur.getLong(col_id);
                    String note = cur.getString(col_note);
                    double lat = cur.getDouble(col_lat);
                    double log = cur.getDouble(col_log);
                    long date = cur.getLong(col_date);
                    String address = cur.getString(col_address);
                    mReminders.add(new StoredReminder(id, note, lat, log, date, address));
                }
            } catch (SQLiteException ex) {
                DeLog.log(ex);
            } finally {
                if (cur != null && !cur.isClosed()) {
                    cur.close();
                }
            }

        }
        return mReminders;

    }

    //note, name, lat, log, address
    public long saveReminder(String note, String name, double lat, double lng, String address) {
        return saveReminder(new StoredReminder(-1, note, name, lat, lng, SystemClock.elapsedRealtime(), address)
        );
    }

    public long saveReminder(StoredReminder reminder) {
        synchronized (sInstance) {
            SQLiteDatabase db = getDatabase();
            try {
                ContentValues values = new ContentValues(5);
                values.put(RemindersContract.LATITUDE, reminder.getLatitude());
                values.put(RemindersContract.LONGTITUDE, reminder.getLongtitude());
                values.put(RemindersContract.NOTE, reminder.getNote());
                values.put(RemindersContract.DATE_MOTIFIED, reminder.getDateModified());
                values.put(RemindersContract.ADDRESS, reminder.getAddress());

                return db.insert(RemindersContract.TABLE_NAME, NULLABLE, values);

            } catch (SQLiteException ex) {
                DeLog.log(ex);
            }
        }
        return -1;
    }

    public boolean saveReminders(List<StoredReminder> reminders) {

        int count = 0;
        long timestamp = SystemClock.elapsedRealtime();
        synchronized (sInstance) {
            SQLiteDatabase db = getDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues(5);
                for (StoredReminder reminder : reminders) {
                    values.put(RemindersContract.LATITUDE, reminder.getLatitude());
                    values.put(RemindersContract.LONGTITUDE, reminder.getLongtitude());
                    values.put(RemindersContract.NOTE, reminder.getNote());
                    values.put(RemindersContract.DATE_MOTIFIED, timestamp);
                    values.put(RemindersContract.ADDRESS, reminder.getAddress());

                    if (db.insert(RemindersContract.TABLE_NAME, NULLABLE, values) != -1) {
                        count++;
                    }
                }
                db.setTransactionSuccessful();
            } catch (SQLiteException ex) {
                DeLog.log(ex);
            } finally {
                db.endTransaction();
            }
        }
        return count > 0;
    }

    /**
     * Removes a reminder from the database
     * </br> <i>WARNING:</i> This doesn't cancel the reminder from the server.
     *
     * @param id The id to delete
     * @return
     */
    public boolean deleteReminder(long id) {
        SQLiteDatabase db = getDatabase();
        synchronized (sInstance) {
            return 1 == db.delete(RemindersContract.TABLE_NAME, RemindersContract._ID + " = " + id, null);
        }

    }

    /**
     * Retrieves the reminder from the database with the specified latitude and longtitude
     *
     * @param latitude  The latitude of the reminder to retrieve
     * @param longitude The longtitude of the reminder to retrieve
     * @return The stored reminder, or null if nothing was found
     */
    public StoredReminder getReminder(double latitude, double longitude) {
        String selection = RemindersContract.LATITUDE + "= ? AND " +
                RemindersContract.LONGTITUDE + " = ?";
        String[] selectArgs = {String.valueOf(latitude),
                String.valueOf(longitude)};
        StoredReminder reminder = null;
        synchronized (sInstance) {
            Cursor cur = null;
            try {
                cur = getDatabase().query(RemindersContract.TABLE_NAME, null, selection, selectArgs, null, null, RemindersContract._ID);
                int col_id = cur.getColumnIndex(RemindersContract._ID);
                int col_note = cur.getColumnIndex(RemindersContract.NOTE);
                int col_lat = cur.getColumnIndex(RemindersContract.LATITUDE);
                int col_log = cur.getColumnIndex(RemindersContract.LONGTITUDE);
                int col_date = cur.getColumnIndex(RemindersContract.DATE_MOTIFIED);
                int col_address = cur.getColumnIndex(RemindersContract.ADDRESS);

                while (cur.moveToNext()) {
                    long id = cur.getLong(col_id);
                    String note = cur.getString(col_note);
                    double lat = cur.getDouble(col_lat);
                    double log = cur.getDouble(col_log);
                    long date = cur.getLong(col_date);
                    String address = cur.getString(col_address);
                    reminder = new StoredReminder(id, note, lat, log, date, address);
                }
            } catch (SQLiteException ex) {
                DeLog.log(ex);
            } finally {
                if (cur != null && !cur.isClosed()) {
                    cur.close();
                }
            }

        }
        return reminder;
    }

    public StoredReminder getReminder(long requestId) {
        String selection = RemindersContract._ID + "= ?";
        String[] selectArgs = {String.valueOf(requestId)};
        StoredReminder reminder = null;
        synchronized (sInstance) {
            Cursor cur = null;
            try {
                cur = getDatabase().query(RemindersContract.TABLE_NAME, null, selection, selectArgs, null, null, RemindersContract._ID);
                int col_id = cur.getColumnIndex(RemindersContract._ID);
                int col_note = cur.getColumnIndex(RemindersContract.NOTE);
                int col_lat = cur.getColumnIndex(RemindersContract.LATITUDE);
                int col_log = cur.getColumnIndex(RemindersContract.LONGTITUDE);
                int col_date = cur.getColumnIndex(RemindersContract.DATE_MOTIFIED);
                int col_address = cur.getColumnIndex(RemindersContract.ADDRESS);

                while (cur.moveToNext()) {
                    long id = cur.getLong(col_id);
                    String note = cur.getString(col_note);
                    double lat = cur.getDouble(col_lat);
                    double log = cur.getDouble(col_log);
                    long date = cur.getLong(col_date);
                    String address = cur.getString(col_address);
                    reminder = new StoredReminder(id, note, lat, log, date, address);
                }
            } catch (SQLiteException ex) {
                DeLog.log(ex);
            } finally {
                if (cur != null && !cur.isClosed()) {
                    cur.close();
                }
            }

        }
        return reminder;
    }

    public boolean updateReminder(long id, String note, String name, double lat, double log, String address) {
        SQLiteDatabase db = getDatabase();
        synchronized (sInstance) {
            ContentValues values = new ContentValues();
            values.put(RemindersContract.LATITUDE, lat);
            values.put(RemindersContract.LONGTITUDE, log);
            values.put(RemindersContract.NOTE, note);
            values.put(RemindersContract.DATE_MOTIFIED, SystemClock.elapsedRealtime());
            values.put(RemindersContract.ADDRESS, address);

            return 0 < db.update(RemindersContract.TABLE_NAME, values, RemindersContract._ID + " = " + id, null);
        }
    }
}
