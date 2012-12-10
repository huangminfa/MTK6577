package com.mediatek.ngin3d.presentation;

import android.graphics.Bitmap;

/**
 * To generate a bitmap if requested.
 */
public abstract class BitmapGenerator {

    private Bitmap mCachedBitmap;
    private Bitmap mDefaultBitmap;

    public void cacheBitmap() {
        mCachedBitmap = generate();
    }

    public Bitmap getCachedBitmap() {
        return mCachedBitmap;
    }

    public Bitmap getBitmap() {
        if (mCachedBitmap == null) {
            return mDefaultBitmap;
        }
        return mCachedBitmap;
    }

    public void free() {
        if (mCachedBitmap != null) {
            mCachedBitmap.recycle();
            mCachedBitmap = null;
        }
    }

    /**
     * Set default bitmap in generator and generator returns it when null bitmap be generated.
     * Notice that default bitmap won't be recycled by generator.
     */
    public void setDefaultBitmap(Bitmap bitmap) {
        mDefaultBitmap = bitmap;
    }

    public abstract Bitmap generate();
}
