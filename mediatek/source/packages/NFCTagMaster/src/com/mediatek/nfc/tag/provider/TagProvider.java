
package com.mediatek.nfc.tag.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.mediatek.nfc.tag.utils.Utils;

public class TagProvider extends ContentProvider {
    private static final String TAG = Utils.TAG + "/TagProvider";

    private TagDBHelper mDbHelper = null;

    private static final int NDEF_TAGS = 1000;

    private static final int NDEF_TAG_ID = 1001;

    private static final UriMatcher MATCHER;
    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(TagContract.AUTHORITY, "tags", NDEF_TAGS);
        MATCHER.addURI(TagContract.AUTHORITY, "tags/#", NDEF_TAG_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new TagDBHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Utils.logd(TAG, "-->delete(), uri=" + uri + ", selection=" + selection);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case NDEF_TAGS:
                if ((selection != null) && selection.startsWith(Utils.HISTORY_LIMITATION_FLAG)
                        && selectionArgs != null && selectionArgs.length > 0) {
                    // delete over number-limitation records by date column
                    String dateLimit = "(select min(" + TagContract.COLUMN_DATE + ") from "
                            + "(select * from " + TagDBHelper.TAG_TABLE_NAME + " order by "
                            + TagContract.COLUMN_DATE + " desc " + "limit 0," + selectionArgs[0]
                            + "))";
                    Utils.logv(TAG, "dateLimit = " + dateLimit);
                    return db.delete(TagDBHelper.TAG_TABLE_NAME, TagContract.COLUMN_DATE + "<"
                            + dateLimit, null);
                }
                break;
            case NDEF_TAG_ID:
                String uriId = uri.getPathSegments().get(1);
                Utils.logd(TAG, " tag id = " + uriId);
                if (TextUtils.isEmpty(selection)) {
                    selection = TagContract.COLUMN_ID + " = \'" + uriId + "\'";
                } else {
                    selection = selection + " AND (" + TagContract.COLUMN_ID + " = \'" + uriId
                            + "\')";
                }
                // qb.appendWhere(TagContract.COLUMN_ID+" = \'"+uriId+"\'");
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return db.delete(TagDBHelper.TAG_TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        Utils.logd(TAG, "-->getType(), uri=" + uri);
        switch (MATCHER.match(uri)) {
            case NDEF_TAGS:
                return TagContract.TAGS_CONTENT_TYPE;
            case NDEF_TAG_ID:
                return TagContract.TAGS_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Utils.logd(TAG, "-->insert(), uri=" + uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = -1;
        switch (MATCHER.match(uri)) {
            case NDEF_TAGS:
                id = db.insert(TagDBHelper.TAG_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri [" + uri + "]  for insert");
        }

        if (id >= 0) {
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Utils.logd(TAG, "-->query(), uri=" + uri + ", selection=" + selection);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TagDBHelper.TAG_TABLE_NAME);

        switch (MATCHER.match(uri)) {
            case NDEF_TAGS:
                break;
            case NDEF_TAG_ID:
                String id = uri.getPathSegments().get(1);
                Utils.logd(TAG, " tag id = " + id);
                qb.appendWhere(TagContract.COLUMN_ID + " = \'" + id + "\'");
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri);
        }
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Utils.logd(TAG, "-->update(), uri=" + uri + ", selection=" + selection);
        return 0;
    }
}
