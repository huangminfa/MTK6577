/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.android.gallery3d.common.Utils;

import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.MediatekFeature.RegionDecoder;
import com.android.gallery3d.util.StereoHelper;
import com.mediatek.stereo3d.Stereo3DConvergence;

// Comments added by Mediatek Inc.
// TileImageViewAdapter connects PhotoDataAdapter (or SinglePhotoDataAdapter)
// and TileImageView. It stores the data that PhotoDataAdapter retrieved for it
// and provides them to TileImageView.
// As Mediate adds new features to Google default feature, such as Drm support
// and Stereo Display support, modifications are inevitable.

// For Drm feature, we add variable mShowDrmMicroThumb to let the
// PositionController to know when to display Drm micro thumbnail in the full
// screen interface.

// For Stereo Display feature, we made a lot modification. We've add Thumbanils
// for left eye and right eye to view. Also, full images for left eye and right
// eye will be added to enhance the clarity of stereo image displayed on the
// screen.

public class TileImageViewAdapter implements TileImageView.Model {
    private static final String TAG = "TileImageViewAdapter";
    protected BitmapRegionDecoder mRegionDecoder;
    protected int mImageWidth;
    protected int mImageHeight;
    protected Bitmap mBackupImage;
    protected int mLevelCount;
    protected boolean mFailedToLoad;

    private static final boolean mIsStereoDisplaySupported = 
                        MediatekFeature.isStereoDisplaySupported();
    //added for DRM
    protected boolean mShowDrmMicroThumb;
    //added for stereo display
    protected Bitmap mFirstImage;
    protected Bitmap mSecondImage;
    protected BitmapRegionDecoder mFirstRegionDecoder;
    protected int mFirstLevelCount;
    protected int mFirstImageWidth;
    protected int mFirstImageHeight;
    protected BitmapRegionDecoder mSecondRegionDecoder;
    protected int mSecondLevelCount;
    protected int mSecondImageWidth;
    protected int mSecondImageHeight;
    protected Stereo3DConvergence mStereoConvergence;
    

    private final Rect mIntersectRect = new Rect();
    private final Rect mRegionRect = new Rect();

    public TileImageViewAdapter() {
    }

    public TileImageViewAdapter(Bitmap backup, BitmapRegionDecoder regionDecoder) {
        mBackupImage = Utils.checkNotNull(backup);
        mRegionDecoder = regionDecoder;
        mImageWidth = regionDecoder.getWidth();
        mImageHeight = regionDecoder.getHeight();
        mLevelCount = calculateLevelCount();
    }

    public synchronized void clear() {
        mBackupImage = null;
        mImageWidth = 0;
        mImageHeight = 0;
        mLevelCount = 0;
        mRegionDecoder = null;
        mFailedToLoad = false;
        //added for stereo display
        mFirstImage = null;
        mSecondImage = null;
        mFirstRegionDecoder = null;
        mFirstLevelCount = 0;
        mFirstImageWidth = 0;
        mFirstImageHeight = 0;
        mSecondRegionDecoder = null;
        mSecondLevelCount = 0;
        mSecondImageWidth = 0;
        mSecondImageHeight = 0;
        mStereoConvergence = null;
    }

    public synchronized void setBackupImage(Bitmap backup, int width, int height) {
        mBackupImage = Utils.checkNotNull(backup);
        mImageWidth = width;
        mImageHeight = height;
        mRegionDecoder = null;
        mLevelCount = 0;
        mFailedToLoad = false;
        //added for stereo display
        mFirstImage = null;
        mSecondImage = null;
    }

    public synchronized void setRegionDecoder(BitmapRegionDecoder decoder) {
        mRegionDecoder = Utils.checkNotNull(decoder);
        mImageWidth = decoder.getWidth();
        mImageHeight = decoder.getHeight();
        mLevelCount = calculateLevelCount();
        mFailedToLoad = false;
    }

    public synchronized void setStereo(BitmapRegionDecoder decoder,
                                 Bitmap backup, int width, int height) {
        mRegionDecoder = decoder;//the decoder may be null for bmp format
        mBackupImage = Utils.checkNotNull(backup);
        mImageWidth = width;
        mImageHeight = height;
        mLevelCount = calculateLevelCount();
        mFailedToLoad = false;
    }

    private int calculateLevelCount() {
        return Math.max(0, Utils.ceilLog2(
                (float) mImageWidth / mBackupImage.getWidth()));
    }

    private int calculateLevelCount(int fullWidth, int backupWidth) {
        return Math.max(0, Utils.ceilLog2((float) fullWidth / backupWidth));
    }

    @Override
    public Bitmap getTile(int level, int x, int y, int length) {
         return getTile(level, x, y, length, 0);
    }

    //@Override
    public Bitmap getTile(int level, int x, int y, int length, 
                          int stereoIndex) {
        BitmapRegionDecoder tempRegionDecoder = 
                                getBitmapRegionDecoder(stereoIndex);

        if (tempRegionDecoder == null) return null;
        int imageWidth = getImageWidth(stereoIndex);
        int imageHeight = getImageHeight(stereoIndex);

        // Note: we changed getTile() function type from synchronized
        // to non-synchroinzed because massive getTile() call from
        // threads may block setBackupImage() is called from UI thread,
        // which caused frequent ANR issue. 
        // As setBackupImage() is a synchronized method with sound
        // reason and getTile() seams without very clear reason to be 
        // synchronized, we change the function type.
        // This modification is WRONG if there is a chance that getTile()
        // and setRegionDecoder() runs concurrently. Wait to check.

        // Instead of using common variable, create a Rect object to
        // decouple from different threads.
        Rect region = new Rect();//mRegionRect;
        Rect intersectRect = new Rect();//mIntersectRect;
        region.set(x, y, x + (length << level), y + (length << level));
        intersectRect.set(0, 0, imageWidth, imageHeight);

        // Get the intersected rect of the requested region and the image.
        Utils.assertTrue(intersectRect.intersect(region));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inPreferQualityOverSpeed = true;
        options.inSampleSize =  (1 << level);

        //for picture quality enhancement
        if (MediatekFeature.isPictureQualityEnhanceSupported()) {
            options.inPostProc = mEnablePQ;
        }

        Bitmap bitmap;

        // In CropImage, we may call the decodeRegion() concurrently.
        synchronized (tempRegionDecoder) {
            bitmap = tempRegionDecoder.decodeRegion(intersectRect, options);
        }

        // The returned region may not match with the targetLength.
        // If so, we fill black pixels on it.
        if (intersectRect.equals(region)) return bitmap;

        if (bitmap == null) {
            Log.w(TAG, "fail in decoding region");
            return null;
        }

        Bitmap tile = Bitmap.createBitmap(length, length, Config.ARGB_8888);
        Canvas canvas = new Canvas(tile);
        canvas.drawBitmap(bitmap,
                (intersectRect.left - region.left) >> level,
                (intersectRect.top - region.top) >> level, null);
        bitmap.recycle();
        return tile;
    }

    @Override
    public Bitmap getBackupImage() {
        return mBackupImage;
    }

    @Override
    public int getImageHeight() {
        return mImageHeight;
    }

    @Override
    public int getImageWidth() {
        return mImageWidth;
    }

    @Override
    public int getLevelCount() {
        return mLevelCount;
    }

    public void setFailedToLoad() {
        mFailedToLoad = true;
    }

    @Override
    public boolean isFailedToLoad() {
        return mFailedToLoad;
    }

    public void setShowDrmMicroThumb(boolean showDrmMicroThumb) {
        mShowDrmMicroThumb = showDrmMicroThumb;
    }

    public boolean getShowDrmMicroThumb() {
        return mShowDrmMicroThumb;
    }

    private boolean mEnablePQ = true;
    public void setEnablePQ(boolean enablePQ) {
        mEnablePQ = enablePQ;
    }

    public synchronized void setFirstImage(Bitmap first) {
        mFirstImage = first;
        if (null == mFirstImage) {
            mFirstImageWidth = 0;
            mFirstImageHeight = 0;
            mFirstLevelCount = 0;
        } else {
            mFirstImageWidth = mFirstImage.getWidth();
            mFirstImageHeight = mFirstImage.getHeight();
            mFirstLevelCount = 0;
        }
    }

    public synchronized void setSecondImage(Bitmap second) {
        mSecondImage = second;
        if (null == mSecondImage) {
            mSecondImageWidth = 0;
            mSecondImageHeight = 0;
            mSecondLevelCount = 0;
        } else {
            mSecondImageWidth = mSecondImage.getWidth();
            mSecondImageHeight = mSecondImage.getHeight();
            mSecondLevelCount = 0;
        }
    }

    public synchronized void setFirstFullImage(BitmapRegionDecoder first) {
        mFirstRegionDecoder = first;
        if (mFirstRegionDecoder != null ) {
            mFirstImageWidth = mFirstRegionDecoder.getWidth();
            mFirstImageHeight = mFirstRegionDecoder.getHeight();
            mFirstLevelCount = calculateLevelCount(mFirstImageWidth,
                                                   mFirstImage.getWidth());
        }
    }

    public synchronized void setSecondFullImage(BitmapRegionDecoder second) {
        mSecondRegionDecoder = second;
        if (mSecondRegionDecoder != null ) {
            mSecondImageWidth = mSecondRegionDecoder.getWidth();
            mSecondImageHeight = mSecondRegionDecoder.getHeight();
            mSecondLevelCount = calculateLevelCount(mSecondImageWidth,
                                                    mSecondImage.getWidth());
        }
    }

    @Override
    public int getImageHeight(int stereoIndex) {
        if (StereoHelper.STEREO_INDEX_NONE == stereoIndex) {
            return mImageHeight;
        } else if (StereoHelper.STEREO_INDEX_FIRST == stereoIndex) {
            return mFirstImageHeight;
        } else {
            return mSecondImageHeight;
        }
    }

    @Override
    public int getImageWidth(int stereoIndex) {
        if (StereoHelper.STEREO_INDEX_NONE == stereoIndex) {
            return mImageWidth;
        } else if (StereoHelper.STEREO_INDEX_FIRST == stereoIndex) {
            return mFirstImageWidth;
        } else {
            return mSecondImageWidth;
        }
    }

    @Override
    public int getLevelCount(int stereoIndex) {
        if (StereoHelper.STEREO_INDEX_NONE == stereoIndex) {
            return mLevelCount;
        } else if (StereoHelper.STEREO_INDEX_FIRST == stereoIndex) {
            return mFirstLevelCount;
        } else {
            return mSecondLevelCount;
        }
    }

    @Override
    public Bitmap getFirstImage() {
        return mFirstImage;
    }

    @Override
    public Bitmap getSecondImage() {
        return mSecondImage;
    }

    private BitmapRegionDecoder getBitmapRegionDecoder(int stereoIndex) {
        if (mIsStereoDisplaySupported && 
            StereoHelper.STEREO_INDEX_NONE != stereoIndex) {
            if (StereoHelper.STEREO_INDEX_FIRST == stereoIndex) {
                return mFirstRegionDecoder;
            } else if (StereoHelper.STEREO_INDEX_SECOND == stereoIndex){
                return mSecondRegionDecoder;
            } else {
                Log.e(TAG, "getBitmapRegionDecoder: invalid stereo index " + 
                          stereoIndex + ", should be in {0, 1, 2}");
                return null;
            }
        } else {
            return mRegionDecoder;
        }
    }

    @Override
    public Stereo3DConvergence getStereoConvergence() {
        return mStereoConvergence;
    }

    public synchronized void setStereoConvergence(
                                    Stereo3DConvergence stereoConvergence) {
        Log.i(TAG,"setConvergenceData(stereoConvergence="+stereoConvergence+")");
        mStereoConvergence = stereoConvergence;
    }
}
