package com.mediatek.nfc.tag.utils;

import android.net.Uri;

public class Bookmark {
    private static final String TAG = Utils.TAG + "/Bookmark";

    public static final Uri BROWSER_URI_GB = Uri.parse("content://browser");

    public static final Uri BROWSER_URI_ICS = Uri.parse("content://com.android.browser");

    public static final String BROWSER_URI_TABLE = "bookmarks";

    public static final Uri BOOKMARK_URI;
    static {
        String version = android.os.Build.VERSION.RELEASE;
        Utils.logi(TAG, "Andriod version = " + version);
        if (version.startsWith("4.")) {
            BOOKMARK_URI = Uri.withAppendedPath(BROWSER_URI_ICS, "bookmarks");
        } else {
            BOOKMARK_URI = Uri.withAppendedPath(BROWSER_URI_GB, "bookmarks");
        }
    }

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_TITLE = "title";

    public static final String COLUMN_URL = "url";

    // Flag indicating if an item is a folder or bookmark. zero stand for
    // bookmark, non-zero value stand for folder
    // This column only exist in ICS
    public static final String COLUMN_IS_FOLDER = "folder";

    public static final String[] PROJECTION = new String[] {
            COLUMN_ID, COLUMN_TITLE, COLUMN_URL
    };

    public static final String EXTRA_SELECTED_BOOKMARK_ID = "selected_bookmark_id";
}
