/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package  com.android.pqtuningtool.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.util.MtkLog;

public class BookmarkEnhance {
    private static final String TAG = "BookmarkEnhance";
    private static final boolean LOG = true;
    
    private static final Uri BOOKMARK_URI = Uri.parse("content://media/internal/bookmark");
    
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATA = "_data";
    public static final String COLUMN_TITLE = "_display_name";
    public static final String COLUMN_ADD_DATE = "date_added";
    public static final String COLUMN_MEDIA_TYPE = "mime_type";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_MIME_TYPE = "media_type";
    
    private static final String NULL_HOCK = COLUMN_POSITION;
    public static final String ORDER_COLUMN = COLUMN_ADD_DATE + " ASC ";
    private static final String VIDEO_STREAMING_MEDIA_TYPE = "streaming";
    
    public static final int INDEX_ID = 0;
    public static final int INDEX_DATA = 1;
    public static final int INDEX_TITLE = 2;
    public static final int INDEX_ADD_DATE = 3;
    public static final int INDEX_MIME_TYPE = 4;
    private static final int INDEX_POSITION = 5;
    private static final int INDEX_MEDIA_TYPE = 6;
    
    public static final String[] PROJECTION = new String[]{
        COLUMN_ID,
        COLUMN_DATA,
        COLUMN_TITLE,
        COLUMN_ADD_DATE,
        COLUMN_MIME_TYPE,
    };
    
    private Context mContext;
    private ContentResolver mCr;
    
    public BookmarkEnhance(Context context) {
        mContext = context;
        mCr = context.getContentResolver();
    }
    
    public Uri insert(String title, String uri, String mimeType,long position) {
        ContentValues values = new ContentValues();
        String mytitle = (title == null ? mContext.getString(R.string.default_title) : title);
        values.put(COLUMN_TITLE, mytitle);
        values.put(COLUMN_DATA, uri);
        values.put(COLUMN_POSITION, position);
        values.put(COLUMN_ADD_DATE, System.currentTimeMillis());
        values.put(COLUMN_MEDIA_TYPE, VIDEO_STREAMING_MEDIA_TYPE);
        values.put(COLUMN_MIME_TYPE, mimeType);
        Uri insertUri = mCr.insert(BOOKMARK_URI, values);
        if (LOG) MtkLog.v(TAG, "insert(" + title + "," + uri + ", " + position + ") return " + insertUri);
        return insertUri;
    }
    
    public int delete(long _id) {
        Uri uri = ContentUris.withAppendedId(BOOKMARK_URI, _id);
        int count = mCr.delete(uri, null, null);
        if (LOG) MtkLog.v(TAG, "delete(" + _id + ") return " + count);
        return count;
    }
    
    public int deleteAll() {
        int count = mCr.delete(BOOKMARK_URI, COLUMN_MEDIA_TYPE + "=? ", new String[]{VIDEO_STREAMING_MEDIA_TYPE});
        if (LOG) MtkLog.v(TAG, "deleteAll() return " + count);
        return count;
    }
    
    public boolean exists(String uri) {
        Cursor cursor = mCr.query(BOOKMARK_URI,
                PROJECTION,
                COLUMN_DATA + "=? and " + COLUMN_MEDIA_TYPE + "=? ",
                new String[]{uri, VIDEO_STREAMING_MEDIA_TYPE},
                null
                );
        boolean exist = false;
        if (cursor != null) {
            exist = cursor.moveToFirst();
            cursor.close();
        }
        if (LOG) MtkLog.v(TAG, "exists(" + uri + ") return " + exist);
        return exist;
    }
    
    public Cursor query() {
        Cursor cursor = mCr.query(BOOKMARK_URI,
                PROJECTION,
                COLUMN_MEDIA_TYPE + "='" + VIDEO_STREAMING_MEDIA_TYPE + "' ",
                null,
                ORDER_COLUMN
                );
        if (LOG) MtkLog.v(TAG, "query() return cursor=" + (cursor == null ? -1 : cursor.getCount()));
        return cursor;
    }
    
    public int update(long _id, String title, String uri, int position) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATA, uri);
        values.put(COLUMN_POSITION, position);
        Uri updateUri = ContentUris.withAppendedId(BOOKMARK_URI, _id);
        int count = mCr.update(updateUri, values, null, null);
        if (LOG) MtkLog.v(TAG, "update(" + _id + ", " + title + ", " + uri + ", " + position + ")" +
                " return " + count);
        return count;
    }
}
