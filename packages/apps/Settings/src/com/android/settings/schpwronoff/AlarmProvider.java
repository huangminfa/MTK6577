package com.android.settings.schpwronoff;

import com.android.settings.R;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.mediatek.xlog.Xlog;

public class AlarmProvider extends ContentProvider {
	private static final String TAG = "AlarmProvider";
    private SQLiteOpenHelper mOpenHelper;

    private static final int SCHPWRS = 1;
    private static final int SCHPWRS_ID = 2;
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("com.android.settings", "schpwr", SCHPWRS);
        sURLMatcher.addURI("com.android.settings", "schpwr/#", SCHPWRS_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "schpwrs.db";
        private static final int DATABASE_VERSION = 5;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE schpwrs (" +
                       "_id INTEGER PRIMARY KEY," +
                       "hour INTEGER, " +
                       "minutes INTEGER, " +
                       "daysofweek INTEGER, " +
                       "alarmtime INTEGER, " +
                       "enabled INTEGER, " +
                       "vibrate INTEGER, " +
                       "message TEXT, " +
                       "alert TEXT);");

            // insert default alarms
            String insertMe = "INSERT INTO schpwrs " +
                    "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, message, alert) " +
                    "VALUES ";
            db.execSQL(insertMe + "(7, 0, 127, 0, 0, 1, '', '');");
            db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
             Xlog.d(TAG,
                    "Upgrading schpwrs database from version " +
                    oldVersion + " to " + currentVersion +
                    ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS schpwrs");
            onCreate(db);
        }
    }

    public AlarmProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case SCHPWRS:
                qb.setTables("schpwrs");
                break;
            case SCHPWRS_ID:
                qb.setTables("schpwrs");
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);

        if (ret == null) {
            Xlog.e(TAG,"Alarms.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case SCHPWRS:
                return "vnd.android.cursor.dir/schpwrs";
            case SCHPWRS_ID:
                return "vnd.android.cursor.item/schpwrs";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case SCHPWRS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("schpwrs", values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url);
            }
        }
        Xlog.d(TAG,"*** notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != SCHPWRS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values;
        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();

        if (!values.containsKey(Alarm.Columns.HOUR))
            values.put(Alarm.Columns.HOUR, 0);

        if (!values.containsKey(Alarm.Columns.MINUTES))
            values.put(Alarm.Columns.MINUTES, 0);

        if (!values.containsKey(Alarm.Columns.DAYS_OF_WEEK))
            values.put(Alarm.Columns.DAYS_OF_WEEK, 0);

        if (!values.containsKey(Alarm.Columns.ALARM_TIME))
            values.put(Alarm.Columns.ALARM_TIME, 0);

        if (!values.containsKey(Alarm.Columns.ENABLED))
            values.put(Alarm.Columns.ENABLED, 0);

        if (!values.containsKey(Alarm.Columns.VIBRATE))
            values.put(Alarm.Columns.VIBRATE, 1);

        if (!values.containsKey(Alarm.Columns.MESSAGE))
            values.put(Alarm.Columns.MESSAGE, "");

        if (!values.containsKey(Alarm.Columns.ALERT))
            values.put(Alarm.Columns.ALERT, "");

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert("schpwrs", Alarm.Columns.MESSAGE, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }
        Xlog.d(TAG,"Added alarm rowId = " + rowId);

        Uri newUrl = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case SCHPWRS:
                count = db.delete("schpwrs", where, whereArgs);
                break;
            case SCHPWRS_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }
                count = db.delete("schpwrs", where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
}
