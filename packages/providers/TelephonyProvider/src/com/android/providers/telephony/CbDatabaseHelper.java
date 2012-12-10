package com.android.providers.telephony;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony;

class CbDatabaseHelper extends SQLiteOpenHelper {

    // Context to access resources with
    private Context mContext;
    static final String DATABASE_NAME = "cb.db";
    static final String CHANNEL_TABLE = "channel";
    static final String CHANNEL_TABLE1 = "channel1";
    static final String CBMESSAGE_TABLE = "messages";
    static final String CONVERSATION_TABLE = "threads";
    // TODO need remove same as channel table
    static final String ADDRESS_TABLE = "address"; // need remove

    private static final String UPDATE_THREAD_COUNT_ON_NEW = "  UPDATE threads SET msg_count = "
            + "     (SELECT COUNT(messages._id) FROM messages LEFT JOIN threads "
            + "      ON threads._id = "
            + Telephony.CbSms.THREAD_ID
            + "      WHERE "
            + Telephony.CbSms.THREAD_ID
            + " = new.thread_id )" + "  WHERE threads._id = new.thread_id; ";

    private static final String SMS_UPDATE_THREAD_READ_BODY = "  UPDATE threads SET read = "
            + "    CASE (SELECT COUNT(*)"
            + "          FROM messages"
            + "          WHERE "
            + Telephony.CbSms.READ
            + " = 0"
            + "            AND "
            + Telephony.CbSms.THREAD_ID
            + " = threads._id)"
            + "      WHEN 0 THEN 1"
            + "      ELSE 0"
            + "    END"
            + "  WHERE threads._id = new."
            + Telephony.CbSms.THREAD_ID + "; ";

    private static final String CB_UPDATE_THREAD_READ_BODY = "  UPDATE threads SET read = "
            + "    CASE (SELECT COUNT(*)"
            + "          FROM messages"
            + "          WHERE "
            + Telephony.CbSms.READ
            + " = 0"
            + "            AND "
            + Telephony.CbSms.THREAD_ID
            + " = threads._id)"
            + "      WHEN 0 THEN 1"
            + "      ELSE 0"
            + "    END"
            + "  WHERE threads._id = new."
            + Telephony.CbSms.THREAD_ID + "; ";

    // update date && snippet && count of threads table.
    private static final String CB_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE = "BEGIN"
            + "  UPDATE threads SET"
            + "    date = (strftime('%s','now') * 1000), "
            + "    snippet = new."
            + Telephony.CbSms.BODY
            + " "
            + "  WHERE threads._id = new."
            + Telephony.CbSms.THREAD_ID
            + "; "
            + UPDATE_THREAD_COUNT_ON_NEW + CB_UPDATE_THREAD_READ_BODY + "END;";

    private static final String UPDATE_THREAD_COUNT_ON_OLD =
        "  UPDATE threads SET msg_count = " +
        "     (SELECT COUNT(messages._id) FROM messages LEFT JOIN threads " +
        "      ON threads._id = " + Telephony.CbSms.THREAD_ID +
        "      WHERE " + Telephony.CbSms.THREAD_ID + " = old.thread_id)" +
        "  WHERE threads._id = old.thread_id; ";
    
    private static final String UPDATE_THREAD_SNIPPET_ON_DELETE =
        "  UPDATE threads SET snippet = " +
        "   (SELECT body FROM" +
        "     (SELECT date, body, thread_id FROM messages)" +
        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
        "  WHERE threads._id = OLD.thread_id; ";
    
    private static final String UPDATE_THREAD_DATE_ON_DELETE =
        "  UPDATE threads SET date = " +
        "   (SELECT date FROM" +
        "     (SELECT date, body, thread_id FROM messages)" +
        "    WHERE thread_id = OLD.thread_id ORDER BY date DESC LIMIT 1) " +
        "  WHERE threads._id = OLD.thread_id; ";

    public CbDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    public CbDatabaseHelper(Context context, String name,
            CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CHANNEL_TABLE
                + "(_id INTEGER PRIMARY KEY," + "name TEXT," + "number TEXT,"
                + "enable BOOLEAN);");
        
        db.execSQL("CREATE TABLE " + CHANNEL_TABLE1
                + "(_id INTEGER PRIMARY KEY," + "name TEXT," + "number TEXT,"
                + "enable BOOLEAN);");
        
        db.execSQL("CREATE TABLE " + CBMESSAGE_TABLE
                + "(_id INTEGER PRIMARY KEY," + "sim_id INTEGER,"
                + "body TEXT," + "channel_id INTEGER," + "thread_id INTEGER,"
                + "read INTEGER DEFAULT 0," + "date_sent INTEGER DEFAULT 0," + "date INTEGER);");

        db.execSQL("CREATE TABLE " + CONVERSATION_TABLE
                + "(_id INTEGER PRIMARY KEY," + "date INTEGER,"
                + "msg_count INTEGER," + "address_id INTEGER,"
                + "read INTEGER DEFAULT 0," + "snippet TEXT);");
        db.execSQL("CREATE TABLE " + ADDRESS_TABLE
                + "(_id INTEGER PRIMARY KEY," + "address TEXT);");

         /**
         * This table is used by the CBSMS dispatcher to hold
         * incomplete partial messages until all the parts arrive.
         */
        db.execSQL("CREATE TABLE cbraw (" +
                   "_id INTEGER PRIMARY KEY," +
                   "msgID INTEGER," +
                   "serialNum INTEGER," + // one per full message
                   "sequence INTEGER," + // the part number of this message
                   "count INTEGER," + // the number of parts
                   "pdu TEXT," +  // the raw PDU for this part   
                   "sim_id INTEGER DEFAULT 0);"); // sim_id
                   
    }

    private void createCommonTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER cb_update_thread_on_insert AFTER INSERT ON messages "
                        + CB_UPDATE_THREAD_DATE_SNIPPET_COUNT_ON_UPDATE);
        db.execSQL("CREATE TRIGGER cb_update_thread_read_on_update AFTER"
                + "  UPDATE OF " + Telephony.CbSms.READ + "  ON messages "
                + "BEGIN " + CB_UPDATE_THREAD_READ_BODY + "END;");
        // Update threads table whenever a message in messages is deleted
        db.execSQL("CREATE TRIGGER cb_update_thread_on_delete " +
                   "AFTER DELETE ON messages " +
                   "BEGIN " +
                   "  UPDATE threads SET " +
                   "     date = (strftime('%s','now') * 1000)" +
                   "  WHERE threads._id = old." + Telephony.CbSms.THREAD_ID + "; " +
                   UPDATE_THREAD_COUNT_ON_OLD +
                   UPDATE_THREAD_SNIPPET_ON_DELETE +
                   UPDATE_THREAD_DATE_ON_DELETE +
                   "END;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createCommonTriggers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 
    }

    public static void updateThread(SQLiteDatabase db, long thread_id) {
        if (thread_id < 0) {
            updateAllThreads(db, null, null);
            return;
        }

        int rows = db.delete(CONVERSATION_TABLE, "_id = ? AND _id NOT IN"
                + "          (SELECT thread_id FROM messages)",
                new String[] { String.valueOf(thread_id) });
    }

    public static void updateAllThreads(SQLiteDatabase db, String where,
            String[] whereArgs) {
        if (where == null) {
            where = "";
        } else {
            where = "WHERE (" + where + ")";
        }

        String query = "SELECT _id FROM threads WHERE _id NOT IN "
                + "(SELECT DISTINCT thread_id FROM messages " + where + ")";
        Cursor c = db.rawQuery(query, whereArgs);
        if (c != null) {
            while (c.moveToNext()) {
                updateThread(db, c.getInt(0));
            }
            c.close();
        }
    }
}
