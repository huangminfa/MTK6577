package com.mediatek.media3d.video;

import android.graphics.Bitmap;
import android.util.Log;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;



/**
 *  utility functions
 */
public final class Util {
    private static String TAG = "UTIL";

    private Util() {} // make it singleton

    private static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    public static String dumpBitmap(Bitmap b, String fileName, Uri uri) {
        FileOutputStream f = null;
        BufferedOutputStream bos = null;
        try {
            Log.v(TAG, "save to " + fileName);
            f = new FileOutputStream(fileName);
            bos = new BufferedOutputStream(f);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        } catch (IOException e) {
            Log.v(TAG, "IOException while saving image");
        } finally {
            closeSilently(bos);
            closeSilently(f);
        }
        return fileName;
    }

}
