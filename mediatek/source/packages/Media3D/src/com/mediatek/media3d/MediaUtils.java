package com.mediatek.media3d;

import android.content.res.Resources;
import android.graphics.*;
import android.os.Environment;
import android.util.Log;

import java.util.Locale;

public final class MediaUtils {
    public static final String TAG = "MediaUtils";
    public static final String CAMERA_IMAGE_BUCKET_NAME  = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    private MediaUtils() {}

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase(Locale.ENGLISH).hashCode());
    }

    public static Rect getTargetRect(int srcWidth, int srcHeight, int maxWidth, int maxHeight) {
        float r = (float)srcHeight / srcWidth;

        int height = (int)(maxWidth * r);
        int margin = (maxHeight - height) / 2;
        return new Rect(0 , margin, maxWidth, margin + height);
    }

    public static Bitmap resizeBitmap(int maxWidth, int maxHeight, Bitmap bitmap) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        Rect srcRect = new Rect(0, 0, srcWidth, srcHeight);
        Rect dstRect = getTargetRect(srcWidth, srcHeight, maxWidth, maxHeight);
        Bitmap b = Bitmap.createBitmap(maxWidth, maxHeight, bitmap.getConfig());
        if (b != null) {
            Canvas canvas = new Canvas(b);
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC);
            canvas.drawBitmap(bitmap, srcRect, dstRect, null);
        }

        return b;
    }

    public static Rect getSourceRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        float dstRatio = (float) dstWidth / dstHeight;
        float srcRatio = (float) srcWidth / srcHeight;

        if (srcRatio >= dstRatio) {
            int width = (int)(dstRatio * srcHeight);
            int margin = (srcWidth - width) / 2;
            return new Rect(margin, 0, margin + width, srcHeight);
        } else {
            int height = (int)(srcWidth / dstRatio);
            int margin = (srcHeight - height) / 2;
            return new Rect(0, margin, srcWidth, margin + height);
        }
    }

    public static Bitmap resizeBitmapFitTarget(int maxWidth, int maxHeight, Bitmap bitmap) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        Rect srcRect = getSourceRect(srcWidth, srcHeight, maxWidth, maxHeight);
        Rect dstRect = new Rect(0, 0, maxWidth, maxHeight);
        Bitmap b = Bitmap.createBitmap(maxWidth, maxHeight, bitmap.getConfig());
        if (b != null) {
            Canvas canvas = new Canvas(b);
            canvas.drawBitmap(bitmap, srcRect, dstRect, null);
        }

        return b;
    }

    public static Bitmap decodeResourceBitmap(Resources resources, int resId, int width, int height) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, opts);
        if ((width == 0 && height == 0) || (width == bitmap.getWidth() && height == bitmap.getHeight())) {
            return bitmap;
        } else {
            Bitmap resizedBitmap = resizeBitmap(width, height, bitmap);
            bitmap.recycle();
            return resizedBitmap;
        }
    }

    public static int calculateSubSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "calculateSubSampleSize - src: " + srcWidth + "x" + srcHeight + ", dst: " + dstWidth + "x" + dstHeight);
        }
        int subSampleSize;
        int width = srcWidth;
        int height = srcHeight;
        // any side is smaller than target size is ok.
        for (subSampleSize = 0; width > 0 && height > 0; subSampleSize++) {
            if (dstWidth > width || dstHeight > height) {
                if (dstWidth > width && dstHeight > height) {
                    if (subSampleSize > 0) {
                        subSampleSize--;
                    }
                }
                break;
            }
            width >>= 1;
            height >>= 1;
        }

        return subSampleSize;
    }

    public static Bitmap cropBitmapFromResource(Resources resources, int resId, int dstWidth, int dstHeight) {
        BitmapFactory.Options defaultOptions = new BitmapFactory.Options();
        defaultOptions.inJustDecodeBounds = true;
        defaultOptions.inSampleSize = 1;
        defaultOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeResource(resources, resId, defaultOptions);
        int srcWidth = defaultOptions.outWidth;
        int srcHeight = defaultOptions.outHeight;

        if (Media3D.DEBUG) {
            Log.v(TAG, "cropBitmapFromResource - src: " + srcWidth + "x" + srcHeight + ", dst: " + dstWidth + "x" + dstHeight);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        if ((dstWidth == 0 && dstHeight == 0) || (dstWidth == srcWidth && dstHeight == srcHeight)) {
            options.inScaled = false;
            return BitmapFactory.decodeResource(resources, resId, options);
        } else {
            options.inSampleSize = calculateSubSampleSize(srcWidth, srcHeight, dstWidth, dstHeight);
            Bitmap bmp = BitmapFactory.decodeResource(resources, resId, options);
            Bitmap b = MediaUtils.resizeBitmapFitTarget(dstWidth, dstHeight, bmp);
            bmp.recycle();
            return b;
        }
    }

    public static Bitmap cropBitmapFromFile(String filePath, int dstWidth, int dstHeight) {
        BitmapFactory.Options defaultOptions = new BitmapFactory.Options();
        defaultOptions.inJustDecodeBounds = true;
        defaultOptions.inSampleSize = 1;
        defaultOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(filePath, defaultOptions);
        int srcWidth = defaultOptions.outWidth;
        int srcHeight = defaultOptions.outHeight;

        if (Media3D.DEBUG) {
            Log.v(TAG, "cropBitmapFromFile - src: " + srcWidth + "x" + srcHeight + ", dst: " + dstWidth + "x" + dstHeight);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        if ((dstWidth == 0 && dstHeight == 0) || (dstWidth == srcWidth && dstHeight == srcHeight)) {
            options.inScaled = false;
            return BitmapFactory.decodeFile(filePath, options);
        } else {
            options.inSampleSize = calculateSubSampleSize(srcWidth, srcHeight, dstWidth, dstHeight);
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
            Bitmap b = MediaUtils.resizeBitmapFitTarget(dstWidth, dstHeight, bmp);
            bmp.recycle();
            return b;
        }
    }

    public static Bitmap cropHalfAndFit(int maxWidth, int maxHeight, Bitmap bitmap, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        Rect srcRect = new Rect(0, 0, srcWidth/2, srcHeight);
        Rect dstRect = new Rect(0, 0, maxWidth, maxHeight);
        Bitmap b = Bitmap.createBitmap(maxWidth, maxHeight, getConfig(bitmap));
        if (b != null) {
            Canvas canvas = new Canvas(b);
            canvas.drawBitmap(bitmap, srcRect, dstRect, null);
        }
        if (recycle) {
            bitmap.recycle();
        }
        return b;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }
}
