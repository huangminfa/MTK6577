
package com.mediatek.nfc.tag.provider;

import android.net.Uri;

public class TagContract {
    public static final String AUTHORITY = "com.mediatek.nfc.tag";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * URL to access NFC tags table
     */
    public static final Uri TAGS_CONTENT_URI = AUTHORITY_URI.buildUpon().appendPath("tags").build();

    public static final String TAGS_CONTENT_TYPE = "vnd.android.cursor.dir/tags";

    public static final String TAGS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/tags";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_TYPE = "type"; /* which kind of tag type */

    public static final String COLUMN_DATE = "date"; /*
                                                      * record added date
                                                      * unit:ms
                                                      */

    public static final String COLUMN_BYTES = "bytes"; /*
                                                        * bytes format of the
                                                        * whole NDEF message
                                                        */

    /*
     * Whether the tag is new created or scanned from tag
     */
    public static final String COLUMN_IS_CREATED_BY_ME = "is_created_by_me";

    /*
     * Record title that will be shown in history list
     */
    public static final String COLUMN_HISTORY_TITLE = "history_title";

    /*
     * user defined column 01
     */
    public static final String COLUMN_01 = "column_01";

    /*
     * user defined column 02
     */
    public static final String COLUMN_02 = "column_02";

    /*
     * user defined column 03
     */
    public static final String COLUMN_03 = "column_03";

    /*
     * user defined column 04
     */
    public static final String COLUMN_04 = "column_04";

    /*
     * user defined column 05
     */
    public static final String COLUMN_05 = "column_05";

    /*
     * user defined column 06
     */
    public static final String COLUMN_06 = "column_06";

    /*
     * user defined column 07
     */
    public static final String COLUMN_07 = "column_07";

    /*
     * user defined column 08
     */
    public static final String COLUMN_08 = "column_08";

}
