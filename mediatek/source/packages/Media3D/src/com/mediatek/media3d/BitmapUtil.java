package com.mediatek.media3d;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public final class BitmapUtil {

    private static final String TAG = "BMPUTIL";

    private BitmapUtil() {} // make it singleton

    /**
     *  create a bit map with assigned width / height.
     *  the bitmap will be up/down sized and cropped if the sourcebmp is larger/smaller
     *  than target image
     *
     *  @param  sourceBmp   the original bitmap
     *  @param  dstWidth    desired width of bitmap
     *  @param  dstHeight   desired height of bitmap
     *  @return cropped bitmap
     *
     **/
    public static Bitmap autoCropBitmapBySize(Bitmap sourceBmp, int dstWidth, int dstHeight) {
        Rect dst = new Rect(0, 0, dstWidth, dstHeight);

        Rect src = measureRect(sourceBmp.getWidth(), sourceBmp.getHeight(), dstWidth, dstHeight);

        Log.v(TAG, "dstRect: " + dst + ", srcRect: " + src);
        Bitmap targetBmp = Bitmap.createBitmap(dstWidth, dstHeight, sourceBmp.getConfig());
        if (targetBmp == null) {
            Log.v(TAG, "bitmap create failed!!");
            return null;
        }
        Canvas c = new Canvas(targetBmp);
        c.drawBitmap(sourceBmp, src, dst, null);

        return targetBmp;
    }

    /**
     *  create a bit map with assigned width / height, and blur it
     *  the bitmap will be up/down sized and cropped if the sourcebmp is larger/smaller
     *  than target image
     *
     *  @param  sourceBmp   the original bitmap
     *  @param  dstWidth    desired width of bitmap
     *  @param  dstHeight   desired height of bitmap
     *  @return cropped bitmap
     *
     **/
    public static Bitmap autoCropBitmapBySizeBlur(Bitmap sourceBmp, int dstWidth, int dstHeight) {

        // get a smaller one, then enlarge.
        Bitmap thumbBitmap = autoCropBitmapBySize(sourceBmp, dstWidth / 4, dstHeight / 4);
        Bitmap ret = Bitmap.createScaledBitmap(thumbBitmap, dstWidth, dstHeight, true);
        thumbBitmap.recycle();
        return ret;
    }


    private static Rect measureRect(int srcWidth, int srcHeight, int destWidth, int destHeight) {
        // got the src width / height
        float dstRatio = (float)destWidth / destHeight;

        Log.v(TAG, String.format("src: %dx%d", srcWidth, srcHeight));
        // use the dest ratio to get the biggest rect in src.
        // try width;
        Log.v(TAG, "dstRatio = " + dstRatio);
        int tempWidth = srcWidth;
        int tempHeight = (int)((float)srcWidth / dstRatio);
        Log.v(TAG, "tempHeight: " + tempHeight);
        if (tempHeight <= srcHeight) {
            // deal.
        } else {
            // use height
            tempHeight = srcHeight;
            tempWidth = (int)((float)srcHeight * dstRatio);
        }

        int x0 = (srcWidth - tempWidth) / 2;
        int y0 = (srcHeight - tempHeight) / 2;

        return new Rect(x0, y0, x0 + tempWidth, y0 + tempHeight);
    }

}
