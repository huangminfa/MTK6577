/**
 * 
 */
package com.android.mms.ui;

import java.util.HashMap;

import android.graphics.Bitmap;


public class VideoThumbnailCache {

    private static HashMap<String, Bitmap> mBitmapCache = new HashMap<String, Bitmap>();

    public static boolean cacheBitmap(String src, String uri, Bitmap mBitmap) {
        if (mBitmap == null) {
            return false;
        }
        mBitmapCache.clear();
        String key = getKey(src, uri);
        mBitmapCache.put(key, mBitmap);
        return true;
    }

    private static String getKey(String src, String uri) {
        String keyStr = ((src == null ? "" : src) + (uri == null ? "" : uri));
        return keyStr.hashCode() + "";
    }

    public static Bitmap getBitmap(String src, String uri) {
        String keyStr = getKey(src, uri);
        return mBitmapCache.get(keyStr);
    }
    
    public static void clear()
    {
        mBitmapCache.clear();
    }
}
