
package com.mediatek.nfc.tag.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mediatek.nfc.tag.utils.Utils;

public class TagDBHelper extends SQLiteOpenHelper {
    private static final String TAG = Utils.TAG + "/TagDBHelper";

    public static final String DATABASE_NAME = "nfc_tags.db";

    private static final int DATABASE_VERSION = 1;

    public static final String TAG_TABLE_NAME = "ndef_tags";

    private static final String CREATE_TAG_TABLE_SQL = "CREATE TABLE " + TAG_TABLE_NAME + " ("
            + TagContract.COLUMN_ID + " INTEGER PRIMARY KEY, " + TagContract.COLUMN_DATE
            + " INTEGER NOT NULL, " + TagContract.COLUMN_TYPE + " INTEGER NOT NULL, "
            + TagContract.COLUMN_BYTES + " BLOB NOT NULL, " + TagContract.COLUMN_IS_CREATED_BY_ME
            + " INTEGER NOT NULL DEFAULT 0, " + TagContract.COLUMN_HISTORY_TITLE + " TEXT, "
            + TagContract.COLUMN_01 + " TEXT, " + TagContract.COLUMN_02 + " TEXT, "
            + TagContract.COLUMN_03 + " TEXT, " + TagContract.COLUMN_04 + " TEXT, "
            + TagContract.COLUMN_05 + " TEXT, " + TagContract.COLUMN_06 + " TEXT, "
            + TagContract.COLUMN_07 + " TEXT, " + TagContract.COLUMN_08 + " TEXT" + ")";

    public TagDBHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    public TagDBHelper(Context context, String dbFile) {
        super(context, dbFile, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Utils.logi(TAG, "-->onCreate()");
        db.execSQL(CREATE_TAG_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Utils
                .logi(TAG, "-->onUpgrade(), oldVersion = " + oldVersion + ", newVersion="
                        + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
        onCreate(db);
    }

}
