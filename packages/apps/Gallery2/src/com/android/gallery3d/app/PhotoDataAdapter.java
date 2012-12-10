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

package com.android.gallery3d.app;

import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.PhotoView.ImageData;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.DrmHelper;
import com.android.gallery3d.util.StereoHelper;
import com.mediatek.gifDecoder.GifDecoder;
import com.mediatek.stereo3d.Stereo3DConvergence;

import android.os.SystemClock;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.drm.DrmStore;
import android.content.Context;
import android.app.Activity;

public class PhotoDataAdapter implements PhotoPage.Model {
    @SuppressWarnings("unused")
    private static final String TAG = "PhotoDataAdapter";

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    private static final int MIN_LOAD_COUNT = 8;
    private static final int DATA_CACHE_SIZE = 32;
    private static final int IMAGE_CACHE_SIZE = 7;  // enlarged for performance

    private static final int BIT_SCREEN_NAIL = 1;
    private static final int BIT_FULL_IMAGE = 2;
    //added to support GIF animation
    private static final int BIT_GIF_ANIMATION = 4;
    //added to support DRM micro-thumb
    private static final int BIT_DRM_SCREEN_NAIL = 8;
    //added to support Stereo Display
    private static final int BIT_SECOND_SCREEN_NAIL = 16;
    private static final int BIT_STEREO_FULL_IMAGE = 32;

    private static final long VERSION_OUT_OF_RANGE = MediaObject.nextVersionNumber();

    private static final int STEREO_THUMB_SHIFT = 1;

    private static final boolean mIsGifAnimationSupported = 
                               MediatekFeature.isGifAnimationSupported();
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsStereoDisplaySupported = 
                        MediatekFeature.isStereoDisplaySupported();
    private static final boolean mIsStereoConvergenceSupported =
                        MediatekFeature.isStereoConvergenceSupported();

    private final GalleryActivity mActivity;
    private Path mConsumedItemPath;
    private boolean mTimeIntervalDRM;
    //because Gallery cached thumbnail as JPEG, and JPEG usually loses image
    //quality. For those image format whose does not has BitmapRegionDecoder
    //this will results in poor image quality, expecially for those man-made
    //image which is used to test image quality.
    //So we will decode from original image to improve image quality if there
    //is no regiondecoder for that image
    private final boolean mReDecodeToImproveImageQuality = true;


    // sImageFetchSeq is the fetching sequence for images.
    // We want to fetch the current screennail first (offset = 0), the next
    // screennail (offset = +1), then the previous screennail (offset = -1) etc.
    // After all the screennail are fetched, we fetch the full images (only some
    // of them because of we don't want to use too much memory).
    private static ImageFetch[] sImageFetchSeq;

    private static class ImageFetch {
        int indexOffset;
        int imageBit;
        public ImageFetch(int offset, int bit) {
            indexOffset = offset;
            imageBit = bit;
        }
    }

    static {
        int gifRequestCount = mIsGifAnimationSupported ? 1 : 0;
        int drmRequestCount = mIsDrmSupported ? 1 : 0;
        int stereoRequestCount = mIsStereoDisplaySupported ?
                (1 + 1 + STEREO_THUMB_SHIFT * 2) : 0;

        int k = 0;
        sImageFetchSeq = new ImageFetch[1 + drmRequestCount +
                                (IMAGE_CACHE_SIZE - 1) * (2 + 2 * drmRequestCount) 
                                        + 3 + gifRequestCount - 2 /*remove 2 full image requests to improve pan performance*/
                                        + stereoRequestCount];
        sImageFetchSeq[k++] = new ImageFetch(0, BIT_SCREEN_NAIL);

        //add to retrieve the second frame of stereo photo
        if (mIsStereoDisplaySupported) {
            sImageFetchSeq[k++] = new ImageFetch(0, BIT_SECOND_SCREEN_NAIL);
        }

        //add for drm to get drm micro-thumb
        if (mIsDrmSupported) {
            sImageFetchSeq[k++] = new ImageFetch(0, BIT_DRM_SCREEN_NAIL);
        }

        for (int i = 1; i < IMAGE_CACHE_SIZE; ++i) {
            sImageFetchSeq[k++] = new ImageFetch(i, BIT_SCREEN_NAIL);
            sImageFetchSeq[k++] = new ImageFetch(-i, BIT_SCREEN_NAIL);
        }

        //add for drm to get drm micro-thumb
        if (mIsDrmSupported) {
            for (int i = 1; i < IMAGE_CACHE_SIZE; ++i) {
                sImageFetchSeq[k++] = new ImageFetch(i, BIT_DRM_SCREEN_NAIL);
                sImageFetchSeq[k++] = new ImageFetch(-i, BIT_DRM_SCREEN_NAIL);
            }
        }

        //add to retrieve the second frame of stereo photo
        if (mIsStereoDisplaySupported && 1 == STEREO_THUMB_SHIFT) {
            sImageFetchSeq[k++] = new ImageFetch(1, BIT_SECOND_SCREEN_NAIL);
            sImageFetchSeq[k++] = new ImageFetch(-1, BIT_SECOND_SCREEN_NAIL);
        }

        sImageFetchSeq[k++] = new ImageFetch(0, BIT_FULL_IMAGE);
//        sImageFetchSeq[k++] = new ImageFetch(1, BIT_FULL_IMAGE);
//        sImageFetchSeq[k++] = new ImageFetch(-1, BIT_FULL_IMAGE);

        //add to retrieve the second full frame of stereo photo
        if (mIsStereoDisplaySupported) {
            sImageFetchSeq[k++] = new ImageFetch(0, BIT_STEREO_FULL_IMAGE);
        }

        if (mIsGifAnimationSupported) {
            sImageFetchSeq[k++] = new ImageFetch(0, BIT_GIF_ANIMATION);
        }
    }

    private final TileImageViewAdapter mTileProvider = new TileImageViewAdapter();

    // PhotoDataAdapter caches MediaItems (data) and ImageEntries (image).
    //
    // The MediaItems are stored in the mData array, which has DATA_CACHE_SIZE
    // entries. The valid index range are [mContentStart, mContentEnd). We keep
    // mContentEnd - mContentStart <= DATA_CACHE_SIZE, so we can use
    // (i % DATA_CACHE_SIZE) as index to the array.
    //
    // The valid MediaItem window size (mContentEnd - mContentStart) may be
    // smaller than DATA_CACHE_SIZE because we only update the window and reload
    // the MediaItems when there are significant changes to the window position
    // (>= MIN_LOAD_COUNT).
    private final MediaItem mData[] = new MediaItem[DATA_CACHE_SIZE];
    private int mContentStart = 0;
    private int mContentEnd = 0;

    /*
     * The ImageCache is a version-to-ImageEntry map. It only holds
     * the ImageEntries in the range of [mActiveStart, mActiveEnd).
     * We also keep mActiveEnd - mActiveStart <= IMAGE_CACHE_SIZE.
     * Besides, the [mActiveStart, mActiveEnd) range must be contained
     * within the[mContentStart, mContentEnd) range.
     */
    private HashMap<Long, ImageEntry> mImageCache = new HashMap<Long, ImageEntry>();
    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    // mCurrentIndex is the "center" image the user is viewing. The change of
    // mCurrentIndex triggers the data loading and image loading.
    private int mCurrentIndex;

    // mChanges keeps the version number (of MediaItem) about the previous,
    // current, and next image. If the version number changes, we invalidate
    // the model. This is used after a database reload or mCurrentIndex changes.
    private final long mChanges[] = new long[3];

    private final Handler mMainHandler;
    private final ThreadPool mThreadPool;

    private final PhotoView mPhotoView;
    private final MediaSet mSource;
    private ReloadTask mReloadTask;

    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;
    private int mSize = 0;
    private Path mItemPath;
    private boolean mIsActive;

    public interface DataListener extends LoadingListener {
        public void onPhotoAvailable(long version, boolean fullImage);
        public void onPhotoChanged(int index, Path item);
    }

    private DataListener mDataListener;

    private final SourceListener mSourceListener = new SourceListener();

    // The path of the current viewing item will be stored in mItemPath.
    // If mItemPath is not null, mCurrentIndex is only a hint for where we
    // can find the item. If mItemPath is null, then we use the mCurrentIndex to
    // find the image being viewed.
    public PhotoDataAdapter(GalleryActivity activity,
            PhotoView view, MediaSet mediaSet, Path itemPath, int indexHint) {
        mSource = Utils.checkNotNull(mediaSet);
        mPhotoView = Utils.checkNotNull(view);
        mItemPath = Utils.checkNotNull(itemPath);
        mCurrentIndex = indexHint;
        mThreadPool = activity.getThreadPool();
        //hold activity istance for DRM feature
        mActivity = activity;

        Arrays.fill(mChanges, MediaObject.INVALID_DATA_VERSION);

        mMainHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
                    case MSG_LOAD_START: {
                        if (mDataListener != null) mDataListener.onLoadingStarted();
                        return;
                    }
                    case MSG_LOAD_FINISH: {
                        if (mDataListener != null) mDataListener.onLoadingFinished();
                        return;
                    }
                    default: throw new AssertionError();
                }
            }
        };

        updateSlidingWindow();
    }

    private long getVersion(int index) {
        if (index < 0 || index >= mSize) return VERSION_OUT_OF_RANGE;
        if (index >= mContentStart && index < mContentEnd) {
            MediaItem item = mData[index % DATA_CACHE_SIZE];
            if (item != null) return item.getDataVersion();
        }
        return MediaObject.INVALID_DATA_VERSION;
    }

    private void fireModelInvalidated() {
        for (int i = -1; i <= 1; ++i) {
            long current = getVersion(mCurrentIndex + i);
            long change = mChanges[i + 1];
            if (current != change) {
                mPhotoView.notifyImageInvalidated(i);
                mChanges[i + 1] = current;
            }
        }
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    private void updateScreenNail(long version, Future<Bitmap> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.screenNailTask != future) {
            Bitmap screenNail = future.get();
            if (screenNail != null) screenNail.recycle();
            return;
        }

        entry.screenNailTask = null;
        entry.screenNail = future.get();

        if (entry.screenNail == null) {
            entry.failToLoad = true;
            //update tile provider
            if (version == getVersion(mCurrentIndex)) {
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
            }
        } else {
            if (mDataListener != null) {
                mDataListener.onPhotoAvailable(version, false);
            }
            for (int i = -1; i <=1; ++i) {
                if (version == getVersion(mCurrentIndex + i)) {
                    if (i == 0) updateTileProvider(entry);
                    mPhotoView.notifyImageInvalidated(i);
                }
            }
        }
        updateImageRequests();
    }

    private void updateFullImage(long version, Future<BitmapRegionDecoder> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.fullImageTask != future) {
            BitmapRegionDecoder fullImage = future.get();
            if (fullImage != null) fullImage.recycle();
            return;
        }

        entry.fullImageTask = null;
        entry.fullImage = future.get();
        if (entry.fullImage != null) {
            if (mDataListener != null) {
                mDataListener.onPhotoAvailable(version, true);
            }
            if (version == getVersion(mCurrentIndex)) {
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
            }
        }
        updateImageRequests();
    }

    public void resume() {
        mIsActive = true;
        mSource.addContentListener(mSourceListener);
        updateImageCache();
        updateImageRequests();

        mReloadTask = new ReloadTask();
        mReloadTask.start();

        mPhotoView.notifyModelInvalidated();
    }

    public void pause() {
        mIsActive = false;

        mReloadTask.terminate();
        mReloadTask = null;

        mSource.removeContentListener(mSourceListener);

        //added for drm consume behavior
        if (mIsDrmSupported) {
            saveDrmConsumeStatus();
        }

        for (ImageEntry entry : mImageCache.values()) {
            if (entry.fullImageTask != null) entry.fullImageTask.cancel();
            if (entry.screenNailTask != null) entry.screenNailTask.cancel();
            //added to cancel Gif decoder task
            if (mIsGifAnimationSupported &&
                entry.gifDecoderTask != null) {
                entry.gifDecoderTask.cancel();
            }
            //added to cancel second screen nail for stereo diaplay
            if (mIsStereoDisplaySupported &&
                entry.secondScreenNailTask != null) {
                entry.secondScreenNailTask.cancel();
            }
            
            if (entry.stereoConvergence != null) {
                entry.stereoConvergence = null;
            }
            if (entry.screenNail != null) {
                entry.screenNail.recycle();
                entry.screenNail= null;
            }
            if (entry.secondScreenNail != null) {
                entry.secondScreenNail.recycle();
                entry.secondScreenNail = null;
            }
        }
        mImageCache.clear();
        mTileProvider.clear();
    }

    private ImageData getImage(int index) {
        if (index < 0 || index >= mSize || !mIsActive) return null;
        Utils.assertTrue(index >= mActiveStart && index < mActiveEnd);

        ImageEntry entry = mImageCache.get(getVersion(index));
        Bitmap screennail = null;

        if (mIsDrmSupported) {
            if (null != entry) {
                if (entry.isDrm) {
                    screennail = entry.drmScreenNail;
                } else {
                    screennail = entry.screenNail;
                }
            } else {
                screennail = null;
            }
        } else {
            screennail = entry == null ? null : entry.screenNail;
        }

        if (screennail != null) {
            if (mIsDrmSupported) {
                int subType = 0;
                MediaItem item = mData[index % DATA_CACHE_SIZE];
                if (item instanceof LocalMediaItem) {
                    subType = ((LocalMediaItem)item).getSubType();
                }
                ImageData imageData =
                    new ImageData(screennail, entry.rotation, 
                             (entry.isDrm && entry.showDrmMicroThumb),subType);
                //update image data for MediatekFeature
                updateImageData(index, entry, imageData);
                return imageData;
            } else {
                ImageData imageData = new ImageData(screennail, entry.rotation);
                //update image data for MediatekFeature
                updateImageData(index, entry, imageData);
                return imageData;
            }
        } else {
            return new ImageData(null, 0);
        }
    }

    public ImageData getPreviousImage() {
        return getImage(mCurrentIndex - 1);
    }

    public ImageData getNextImage() {
        return getImage(mCurrentIndex + 1);
    }

    private void updateCurrentIndex(int index) {
        // add protection for possible index out of bounds exception.
        if (index < 0) {
            index = 0;
        } else if (index >= mSize && mSize > 0) {
            index = mSize - 1;
        }
        mCurrentIndex = index;
        updateSlidingWindow();

        MediaItem item = mData[index % DATA_CACHE_SIZE];
        mItemPath = item == null ? null : item.getPath();

        updateImageCache();
        updateImageRequests();
        updateTileProvider();
        mPhotoView.notifyOnNewImage();

        if (mIsStereoDisplaySupported) {
            mPhotoView.setStereoMode(false);
            mPhotoView.setStoredProgress(-1);
            mPhotoView.notifyImageInvalidated(0);
        }

        if (mDataListener != null) {
            mDataListener.onPhotoChanged(index, mItemPath);
        }
        fireModelInvalidated();
    }

    public void next() {
        updateCurrentIndex(mCurrentIndex + 1);
    }

    public void previous() {
        updateCurrentIndex(mCurrentIndex - 1);
    }

    public void jumpTo(int index) {
        if (mCurrentIndex == index) return;
        updateCurrentIndex(index);
    }

    public Bitmap getBackupImage() {
        return mTileProvider.getBackupImage();
    }

    public int getImageHeight() {
        return mTileProvider.getImageHeight();
    }

    public int getImageWidth() {
        return mTileProvider.getImageWidth();
    }

    public int getImageRotation() {
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));
        return entry == null ? 0 : entry.rotation;
    }

    public int getLevelCount() {
        return mTileProvider.getLevelCount();
    }

    public Bitmap getTile(int level, int x, int y, int tileSize) {
        return mTileProvider.getTile(level, x, y, tileSize, 0);
    }

    public int getLevelCount(int stereoIndex) {
        return mTileProvider.getLevelCount(stereoIndex);
    }

    public int getImageHeight(int stereoIndex) {
        return mTileProvider.getImageHeight(stereoIndex);
    }

    public int getImageWidth(int stereoIndex) {
        return mTileProvider.getImageWidth(stereoIndex);
    }

    public Bitmap getFirstImage() {
        return mTileProvider.getFirstImage();
    }

    public Bitmap getSecondImage() {
        return mTileProvider.getSecondImage();
    }

    public Bitmap getTile(int level, int x, int y, int tileSize,
                          int stereoIndex) {
        return mTileProvider.getTile(level, x, y, tileSize, stereoIndex);
    }

    public Stereo3DConvergence getStereoConvergence() {
        return mTileProvider.getStereoConvergence();
    }

    public boolean isFailedToLoad() {
        return mTileProvider.isFailedToLoad();
    }

    public boolean isEmpty() {
        return mSize == 0;
    }

    public boolean showDrmMicroThumb() {
        return mTileProvider.getShowDrmMicroThumb();
    }

    public boolean suggestFullScreen() {
        return suggestFullScreen(getCurrentMediaItem());
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public MediaItem getCurrentMediaItem() {
        return mData[mCurrentIndex % DATA_CACHE_SIZE];
    }

    public void setCurrentPhoto(Path path, int indexHint) {
        if (mItemPath == path) return;
        mItemPath = path;
        mCurrentIndex = indexHint;
        updateSlidingWindow();
        updateImageCache();
        fireModelInvalidated();

        // We need to reload content if the path doesn't match.
        MediaItem item = getCurrentMediaItem();
        if (item != null && item.getPath() != path) {
            if (mReloadTask != null) mReloadTask.notifyDirty();
        }
    }

    private void updateTileProvider() {
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));
        if (entry == null) { // in loading
            mTileProvider.clear();
        } else {
            updateTileProvider(entry);
        }
    }

    private void updateTileProvider(ImageEntry entry) {
        if (null == entry) {
            return;
        }
        if (mIsDrmSupported
                && ((entry.isDrm && !entry.isDrmGif)
                        || (entry.isDrmGif &&  entry.isDrmConsume && !entry.enteredConsumeMode)
                        || (entry.isDrmGif && !entry.isDrmConsume && entry.gifDecoder == null))) {
            if (null != entry.drmScreenNail) {
                int width = entry.drmScreenNail.getWidth();
                int height = entry.drmScreenNail.getHeight();
                mTileProvider.setBackupImage(entry.drmScreenNail, width, height);
                mTileProvider.setShowDrmMicroThumb(entry.showDrmMicroThumb);
            } else {
                mTileProvider.clear();
                if (entry.failToLoad) mTileProvider.setFailedToLoad();
            }
            return;
        }
        mTileProvider.setShowDrmMicroThumb(false);

        Bitmap screenNail = null;
        // for Drm image only has drmScreenNail, haven't screenNail and fullImage.
        if (entry.isDrm) {
            screenNail = entry.drmScreenNail;
        } else {
            screenNail = entry.screenNail;
        }
        BitmapRegionDecoder fullImage = entry.fullImage;
        if (screenNail != null) {
            if (mIsGifAnimationSupported && entry.currentGifFrame != null) {
                int width = entry.currentGifFrame.getWidth();
                int height = entry.currentGifFrame.getHeight();
                mTileProvider.setBackupImage(entry.currentGifFrame, width, height);
            } else if (fullImage != null) {
                int fullWidth = fullImage.getWidth();
                int fullHeight = fullImage.getHeight();
                //adjust full image dimesion if needed
                if (mIsStereoDisplaySupported) {
                    fullWidth = StereoHelper.adjustDim(true, entry.stereoLayout,
                                                       fullWidth);
                    fullHeight = StereoHelper.adjustDim(false, entry.stereoLayout,
                                                       fullHeight);
                    mTileProvider.setStereo(fullImage, screenNail, fullWidth,
                                                                   fullHeight);
                    //update stereo relate texture
                    mTileProvider.setFirstImage(entry.firstScreenNail);
                    mTileProvider.setSecondImage(entry.secondScreenNail);
                    if (null != entry.firstFullImage) {
                        mTileProvider.setFirstFullImage(entry.firstFullImage.regionDecoder);
                    } else {
                        mTileProvider.setFirstFullImage(null);
                    }
                    if (null != entry.secondFullImage) {
                        mTileProvider.setSecondFullImage(entry.secondFullImage.regionDecoder);
                    } else {
                        mTileProvider.setSecondFullImage(null);
                    }
                    //update stereo convergence data
                    mTileProvider.setStereoConvergence(entry.stereoConvergence);
                } else {
                    mTileProvider.setBackupImage(screenNail, fullWidth, fullHeight);
                    mTileProvider.setRegionDecoder(fullImage);
                }
            } else if (mIsStereoDisplaySupported && null != entry.secondScreenNail) {
                int width2 = screenNail.getWidth();
                int height2 = screenNail.getHeight();
                if (MediatekFeature.isCMCC() && 
                    0 != entry.originWidth && 0 != entry.originHeight) {
                    width2 = entry.originWidth;
                    height2 = entry.originHeight;
                }
                mTileProvider.setBackupImage(screenNail, width2, height2);
                //update stereo relate texture
                mTileProvider.setFirstImage(entry.firstScreenNail);
                mTileProvider.setSecondImage(entry.secondScreenNail);
                if (null != entry.firstFullImage) {
                    mTileProvider.setFirstFullImage(entry.firstFullImage.regionDecoder);
                } else {
                    mTileProvider.setFirstFullImage(null);
                }
                if (null != entry.secondFullImage) {
                    mTileProvider.setSecondFullImage(entry.secondFullImage.regionDecoder);
                } else {
                    mTileProvider.setSecondFullImage(null);
                }
                //update stereo convergence data
                mTileProvider.setStereoConvergence(entry.stereoConvergence);
                if (mReDecodeToImproveImageQuality && entry.originScreenNail != null) {
                    int width = entry.originScreenNail.getWidth();
                    int height = entry.originScreenNail.getHeight();
                    mTileProvider.setStereo(null, entry.originScreenNail, width, height);
                }
            } else if (mReDecodeToImproveImageQuality &&
                       entry.originScreenNail != null) {
                int width = entry.originScreenNail.getWidth();
                int height = entry.originScreenNail.getHeight();
                mTileProvider.setBackupImage(entry.originScreenNail, width, height);
                //update stereo relate texture
                mTileProvider.setFirstImage(null);
                mTileProvider.setSecondImage(null);
                mTileProvider.setFirstFullImage(null);
                mTileProvider.setSecondFullImage(null);
                //update stereo convergence data
                mTileProvider.setStereoConvergence(entry.stereoConvergence);
            } else {
                int width = screenNail.getWidth();
                int height = screenNail.getHeight();
                if (MediatekFeature.isCMCC() && 
                    0 != entry.originWidth && 0 != entry.originHeight) {
                    width = entry.originWidth;
                    height = entry.originHeight;
                }
                mTileProvider.setBackupImage(screenNail, width, height);
                //update stereo relate texture
                mTileProvider.setFirstImage(null);
                mTileProvider.setSecondImage(null);
                mTileProvider.setFirstFullImage(null);
                mTileProvider.setSecondFullImage(null);
                //update stereo convergence data
                mTileProvider.setStereoConvergence(entry.stereoConvergence);
            }
        } else {
            mTileProvider.clear();
            if (entry.failToLoad) mTileProvider.setFailedToLoad();
        }
    }

    private void updateSlidingWindow() {
        // 1. Update the image window
        int start = Utils.clamp(mCurrentIndex - IMAGE_CACHE_SIZE / 2,
                0, Math.max(0, mSize - IMAGE_CACHE_SIZE));
        int end = Math.min(mSize, start + IMAGE_CACHE_SIZE);

        if (mActiveStart == start && mActiveEnd == end) return;

        mActiveStart = start;
        mActiveEnd = end;

        // 2. Update the data window
        start = Utils.clamp(mCurrentIndex - DATA_CACHE_SIZE / 2,
                0, Math.max(0, mSize - DATA_CACHE_SIZE));
        end = Math.min(mSize, start + DATA_CACHE_SIZE);
        if (mContentStart > mActiveStart || mContentEnd < mActiveEnd
                || Math.abs(start - mContentStart) > MIN_LOAD_COUNT) {
            for (int i = mContentStart; i < mContentEnd; ++i) {
                if (i < start || i >= end) {
                    mData[i % DATA_CACHE_SIZE] = null;
                }
            }
            mContentStart = start;
            mContentEnd = end;
            if (mReloadTask != null) mReloadTask.notifyDirty();
        }
    }

    private void updateImageRequests() {
        if (!mIsActive) return;

        int currentIndex = mCurrentIndex;
        MediaItem item = mData[currentIndex % DATA_CACHE_SIZE];
        if (item == null || item.getPath() != mItemPath) {
            // current item mismatch - don't request image
            return;
        }

        // 1. Find the most wanted request and start it (if not already started).
        Future<?> task = null;
        for (int i = 0; i < sImageFetchSeq.length; i++) {
            int offset = sImageFetchSeq[i].indexOffset;
            int bit = sImageFetchSeq[i].imageBit;
            task = startTaskIfNeeded(currentIndex + offset, bit);
            if (task != null) break;
        }

        // 2. Cancel everything else.
        for (ImageEntry entry : mImageCache.values()) {
            if (entry.screenNailTask != null && entry.screenNailTask != task) {
                entry.screenNailTask.cancel();
                entry.screenNailTask = null;
                entry.requestedBits &= ~BIT_SCREEN_NAIL;
            }
            if (entry.fullImageTask != null && entry.fullImageTask != task) {
                entry.fullImageTask.cancel();
                entry.fullImageTask = null;
                entry.requestedBits &= ~BIT_FULL_IMAGE;
            }
            //cancel gif animation task if needed
            if (mIsGifAnimationSupported &&
                entry.gifDecoderTask != null && entry.gifDecoderTask != task) {
                entry.gifDecoderTask.cancel();
                entry.gifDecoderTask = null;
                entry.requestedBits &= ~BIT_GIF_ANIMATION;
            }
            //cancel drm task if needed
            if (mIsDrmSupported &&
                entry.drmScreenNailTask != null && 
                entry.drmScreenNailTask != task) {
                entry.drmScreenNailTask.cancel();
                entry.drmScreenNailTask = null;
                entry.requestedBits &= ~BIT_DRM_SCREEN_NAIL;
            }
            //cancel stereo task if needed
            if (mIsStereoDisplaySupported &&
                entry.secondScreenNailTask != null && 
                entry.secondScreenNailTask != task) {
                entry.secondScreenNailTask.cancel();
                entry.secondScreenNailTask = null;
                entry.requestedBits &= ~BIT_SECOND_SCREEN_NAIL;
            }
            //cancel stereo full image
            if (mIsStereoDisplaySupported &&
                entry.stereoFullImageTask != null &&
                entry.stereoFullImageTask != task) {
                entry.stereoFullImageTask.cancel();
                entry.stereoFullImageTask = null;
                entry.requestedBits &= ~BIT_STEREO_FULL_IMAGE;
            }
            //cancel decode original bitmap task
            if (mReDecodeToImproveImageQuality &&
                entry.originScreenNailTask != null &&
                entry.originScreenNailTask != task) {
                entry.originScreenNailTask.cancel();
                entry.originScreenNailTask = null;
                entry.requestedBits &= ~BIT_FULL_IMAGE;
            }
        }
    }

    private static class ScreenNailJob implements Job<Bitmap> {
        private MediaItem mItem;

        public ScreenNailJob(MediaItem item) {
            mItem = item;
        }

        @Override
        public Bitmap run(JobContext jc) {
            Bitmap bitmap = mItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
            if (jc.isCancelled()) return null;
            if (bitmap != null) {
                bitmap = BitmapUtils.rotateBitmap(bitmap,
                    mItem.getRotation() - mItem.getFullImageRotation(), true);
            }
            return bitmap;
        }
    }

    // Returns the task if we started the task or the task is already started.
    private Future<?> startTaskIfNeeded(int index, int which) {
        if (index < mActiveStart || index >= mActiveEnd) return null;

        ImageEntry entry = mImageCache.get(getVersion(index));
        if (entry == null) return null;

        if (which == BIT_SCREEN_NAIL && entry.screenNailTask != null) {
            return entry.screenNailTask;
        } else if (which == BIT_FULL_IMAGE && entry.fullImageTask != null) {
            return entry.fullImageTask;
        } else if (which == BIT_GIF_ANIMATION && entry.gifDecoderTask != null
                   && mIsGifAnimationSupported) {
            return entry.gifDecoderTask;
        } else if (which == BIT_DRM_SCREEN_NAIL && entry.drmScreenNailTask != null
                   && mIsDrmSupported) {
            return entry.drmScreenNailTask;
        } else if (which == BIT_SECOND_SCREEN_NAIL && entry.secondScreenNailTask != null
                   && mIsStereoDisplaySupported) {
            return entry.secondScreenNailTask;
        } else if (which == BIT_STEREO_FULL_IMAGE && entry.stereoFullImageTask != null
                   && mIsStereoDisplaySupported) {
            return entry.stereoFullImageTask;
        } else if (which == BIT_FULL_IMAGE && entry.originScreenNailTask != null
                   && mReDecodeToImproveImageQuality) {
            return entry.originScreenNailTask;
        }

        MediaItem item = mData[index % DATA_CACHE_SIZE];
        Utils.assertTrue(item != null);
        //update image entry infos
        updateImageEntry(entry, item, which);
        //remember that for current drm, it may decode large thumbnail
        if (mIsDrmSupported && (item instanceof LocalMediaItem) && 
            ((LocalMediaItem)item).isDrm()) {
            entry.isDrm = true;
	    // is Drm format Gif image 
            entry.isDrmGif = ((LocalMediaItem)item).isGifImage();
            entry.isDrmConsume = (((LocalMediaItem)item).getSupportedOperations() & MediaItem.SUPPORT_CONSUME_DRM) != 0;
            if (which == BIT_DRM_SCREEN_NAIL &&
                (entry.requestedBits & BIT_DRM_SCREEN_NAIL) == 0) {
                entry.requestedBits |= BIT_DRM_SCREEN_NAIL;
                entry.drmScreenNailTask = mThreadPool.submit(
                        new DrmScreenNailJob(item),
                        new DrmScreenNailListener(item.getDataVersion()));
                // request drm micro thumb
                return entry.drmScreenNailTask;
            }
            // for drm, only DRM_SCREEN_NAIL can be retrieved,
            // so skip all the other tasks
            //return null;
        }

        if (which == BIT_SCREEN_NAIL
                && (entry.requestedBits & BIT_SCREEN_NAIL) == 0 && !entry.isDrmGif) {
            entry.requestedBits |= BIT_SCREEN_NAIL;
            entry.screenNailTask = mThreadPool.submit(
                    new ScreenNailJob(item),
                    new ScreenNailListener(item.getDataVersion()));
            // request screen nail
            return entry.screenNailTask;
        }
        if (which == BIT_FULL_IMAGE
                && (entry.requestedBits & BIT_FULL_IMAGE) == 0
                && (item.getSupportedOperations()
                & MediaItem.SUPPORT_FULL_IMAGE) != 0 && !entry.isDrmGif) {
            entry.requestedBits |= BIT_FULL_IMAGE;
            entry.fullImageTask = mThreadPool.submit(
                    item.requestLargeImage(),
                    new FullImageListener(item.getDataVersion()));
            // request full image
            return entry.fullImageTask;
        }
        if (mReDecodeToImproveImageQuality && which == BIT_FULL_IMAGE
                && (entry.requestedBits & BIT_FULL_IMAGE) == 0
                && (item.getSupportedOperations()
                & MediaItem.SUPPORT_FULL_IMAGE) == 0 && !entry.isDrmGif) {
            entry.requestedBits |= BIT_FULL_IMAGE;
            entry.originScreenNailTask = mThreadPool.submit(
                    new OriginScreenNailJob(item),
                    new OriginScreenNailListener(item.getDataVersion()));
            // request original screen nail
            return entry.originScreenNailTask;
        }
        if (mIsGifAnimationSupported) {
            if (which == BIT_GIF_ANIMATION
                    && (entry.requestedBits & BIT_GIF_ANIMATION) == 0
                    && (item.getSupportedOperations() & 
                        MediaItem.SUPPORT_GIF_ANIMATION) != 0
                    ) {
                entry.requestedBits |= BIT_GIF_ANIMATION;
                entry.gifDecoderTask = mThreadPool.submit(
                        item.requestGifDecoder(),
                        new GifDecoderListener(item.getDataVersion()));
                // request gif decoder
                return entry.gifDecoderTask;
            }
        }
        if (mIsStereoDisplaySupported) {
            if (which == BIT_SECOND_SCREEN_NAIL
                    && (entry.requestedBits & BIT_SECOND_SCREEN_NAIL) == 0
                    && (item.getSupportedOperations() & 
                        MediaItem.SUPPORT_STEREO_DISPLAY) != 0 && !entry.isDrmGif
                    ) {
                //we should get the layout of stereo image first
                entry.stereoLayout = item.getStereoLayout();
                //we create a decode job
                entry.requestedBits |= BIT_SECOND_SCREEN_NAIL;
                //create mediatek parameters
                MediatekFeature.Params params = new MediatekFeature.Params();
                params.inOriginalFrame = false;
                params.inFirstFrame = true;//we decode the first frame if possible
                params.inSecondFrame = true;
                params.inRotation = item.getRotation();
                entry.secondScreenNailTask = mThreadPool.submit(
                    new SecondScreenNailJob(item, params),
                    new SecondScreenNailListener(item.getDataVersion()));
                // request second screen nail for stereo display
                return entry.secondScreenNailTask;
            }
            //create stereo full image task
            if (which == BIT_STEREO_FULL_IMAGE
                    && (entry.requestedBits & BIT_STEREO_FULL_IMAGE) == 0
                    && (item.getSupportedOperations() & 
                        MediaItem.SUPPORT_STEREO_DISPLAY) != 0
                    && (item.getSupportedOperations() & 
                        MediaObject.SUPPORT_CONVERT_TO_3D) == 0
                        && !entry.isDrmGif) {
                Log.d(TAG,"create stereo full image task...");
                //we should get the layout of stereo image first
                entry.stereoLayout = item.getStereoLayout();
                //we create a decode job
                entry.requestedBits |= BIT_STEREO_FULL_IMAGE;
                //create mediatek parameters
                MediatekFeature.Params params = new MediatekFeature.Params();
                params.inOriginalFrame = false;
                params.inFirstFrame = false;
                params.inSecondFrame = false;
                params.inFirstFullFrame = true;
                params.inSecondFullFrame = true;
                params.inRotation = item.getRotation();
                entry.stereoFullImageTask = mThreadPool.submit(
                    new StereoFullImageJob(item, params),
                    new StereoFullImageListener(item.getDataVersion()));
                // request second screen nail for stereo display
                return entry.stereoFullImageTask;
            }

        }
        return null;
    }

    private void updateImageCache() {
        HashSet<Long> toBeRemoved = new HashSet<Long>(mImageCache.keySet());
        for (int i = mActiveStart; i < mActiveEnd; ++i) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            long version = item == null
                    ? MediaObject.INVALID_DATA_VERSION
                    : item.getDataVersion();
            if (version == MediaObject.INVALID_DATA_VERSION) continue;
            ImageEntry entry = mImageCache.get(version);
            toBeRemoved.remove(version);
            if (entry != null) {
                if (Math.abs(i - mCurrentIndex) > 1) {
                    if (entry.fullImageTask != null) {
                        entry.fullImageTask.cancel();
                        entry.fullImageTask = null;
                    }
                    entry.fullImage = null;
                    entry.requestedBits &= ~BIT_FULL_IMAGE;
                }
                //added for cancel Gif animation tasks
                if (mIsGifAnimationSupported &&
                    Math.abs(i - mCurrentIndex) > 0) {
                    if (entry.gifDecoderTask != null) {
                        entry.gifDecoderTask.cancel();
                        entry.gifDecoderTask = null;
                    }
                    entry.gifDecoder = null;
                    entry.requestedBits &= ~BIT_GIF_ANIMATION;
                    if (null != entry.currentGifFrame && 
                        !entry.currentGifFrame.isRecycled()) {
                        //recycle cached gif frame
                        entry.currentGifFrame.recycle();
                        entry.currentGifFrame = null;
                    }
                }
                //added for DRM screen nail tasks
                //Note: we should reset those who entered consume mode
                //to micro thumb mode
                if (mIsDrmSupported && Math.abs(i - mCurrentIndex) > 0 &&
                    entry.enteredConsumeMode) {                    
                    if (entry.drmScreenNailTask != null) {
                        entry.drmScreenNailTask.cancel();
                        entry.drmScreenNailTask = null;
                    }
                    entry.requestedBits &= ~BIT_DRM_SCREEN_NAIL;
                    entry.enteredConsumeMode = false;
                    entry.drmScreenNail = null;
                }
                //added to decode original bitmap (not from cache, to 
                //improve image quality
                if (mReDecodeToImproveImageQuality && 
                    Math.abs(i - mCurrentIndex) > 0) {
                    if (entry.originScreenNailTask != null) {
                        entry.originScreenNailTask.cancel();
                        entry.originScreenNailTask = null;
                    }
                    entry.originScreenNail = null;
                    entry.requestedBits &= ~BIT_FULL_IMAGE;
                }
                //added for cancel second frame screen nail task
                if (mIsStereoDisplaySupported &&
                    Math.abs(i - mCurrentIndex) > STEREO_THUMB_SHIFT) {
                    if (entry.secondScreenNailTask != null) {
                        entry.secondScreenNailTask.cancel();
                        entry.secondScreenNailTask = null;
                    }
                    entry.requestedBits &= ~BIT_SECOND_SCREEN_NAIL;
                    if (null != entry.secondScreenNail && 
                        !entry.secondScreenNail.isRecycled()) {
                        //recycle second screen nail
                        entry.secondScreenNail.recycle();
                        entry.secondScreenNail = null;
                    }
                    if (null != entry.firstScreenNail && 
                        !entry.firstScreenNail.isRecycled()) {
                        //recycle first screen nail
                        entry.firstScreenNail.recycle();
                        entry.firstScreenNail = null;
                    }
                    //cancel stereo full image task
                    if (entry.stereoFullImageTask != null) {
                        entry.stereoFullImageTask.cancel();
                        entry.stereoFullImageTask = null;
                    }
                    entry.requestedBits &= ~BIT_STEREO_FULL_IMAGE;
                    if (null != entry.firstFullImage) {
                        entry.firstFullImage.release();
                        entry.firstFullImage = null;
                    }
                    if (null != entry.secondFullImage) {
                        entry.secondFullImage.release();
                        entry.secondFullImage = null;
                    }
                    if (null != entry.stereoConvergence) {
                        entry.stereoConvergence = null;
                    }
                }
            } else {
                entry = new ImageEntry();
                entry.rotation = item.getFullImageRotation();
                mImageCache.put(version, entry);
            }
        }

        // Clear the data and requests for ImageEntries outside the new window.
        for (Long version : toBeRemoved) {
            ImageEntry entry = mImageCache.remove(version);
            if (entry.fullImageTask != null) entry.fullImageTask.cancel();
            if (entry.screenNailTask != null) entry.screenNailTask.cancel();
            //added for gif animation: cancel gifDecoder task and recycle frame
            if (mIsGifAnimationSupported) {
                if (entry.gifDecoderTask != null) entry.gifDecoderTask.cancel();
                if (null != entry.currentGifFrame && 
                    !entry.currentGifFrame.isRecycled()) {
                    //recycle cached gif frame
                    entry.currentGifFrame.recycle();
                    entry.currentGifFrame = null;
                }
            }
            //added for drm feature: cancel drm decode task
            if (mIsDrmSupported && entry.drmScreenNailTask != null) {
                entry.drmScreenNailTask.cancel();
                entry.drmScreenNail = null;
            }
            //added to decode original bitmap (not from cache, to 
            //improve image quality
            if (mReDecodeToImproveImageQuality &&
                entry.originScreenNailTask != null) {
                entry.originScreenNailTask.cancel();
                entry.originScreenNail = null;
            }
            if (mIsStereoDisplaySupported) {
                if (entry.secondScreenNailTask != null) {
                    entry.secondScreenNailTask.cancel();
                }
                if (entry.secondScreenNail != null &&
                    !entry.secondScreenNail.isRecycled()) {
                    //recycle second screen nail for stereo photo
                    entry.secondScreenNail.recycle();
                    entry.secondScreenNail = null;
                }
                if (entry.firstScreenNail != null &&
                    !entry.firstScreenNail.isRecycled()) {
                    //recycle first screen nail for stereo photo
                    entry.firstScreenNail.recycle();
                    entry.firstScreenNail = null;
                }
            }
        }
    }

    private void updateDrmCache() {
        if (!mIsDrmSupported) return;
        for (int i = mActiveStart; i < mActiveEnd; ++i) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            long version = item == null
                    ? MediaObject.INVALID_DATA_VERSION
                    : item.getDataVersion();
            if (version == MediaObject.INVALID_DATA_VERSION) continue;
            ImageEntry entry = mImageCache.get(version);
            if (entry != null) {
                if (entry.drmScreenNailTask != null) {
                    entry.drmScreenNailTask.cancel();
                    entry.drmScreenNailTask = null;
                }
                entry.requestedBits &= ~BIT_DRM_SCREEN_NAIL;
                if (i != mCurrentIndex) {
                    //reset all except current image
                    entry.enteredConsumeMode = false;
                }
                entry.drmScreenNail = null;
            }
        }
    }

    private class FullImageListener
            implements Runnable, FutureListener<BitmapRegionDecoder> {
        private final long mVersion;
        private Future<BitmapRegionDecoder> mFuture;

        public FullImageListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateFullImage(mVersion, mFuture);
        }
    }

    private class ScreenNailListener
            implements Runnable, FutureListener<Bitmap> {
        private final long mVersion;
        private Future<Bitmap> mFuture;

        public ScreenNailListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<Bitmap> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateScreenNail(mVersion, mFuture);
        }
    }

    private class GifDecoderListener
            implements Runnable, FutureListener<GifDecoder> {
        private final long mVersion;
        private Future<GifDecoder> mFuture;

        public GifDecoderListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<GifDecoder> future) {
            mFuture = future;
            if (mIsGifAnimationSupported && null != mFuture.get()) {
                mMainHandler.sendMessage(
                        mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
            }
        }

        @Override
        public void run() {

            ImageEntry entry = mImageCache.get(mVersion);
            if (entry == null || entry.gifDecoderTask != mFuture) {
                GifDecoder gifDecoder = mFuture.get();
                gifDecoder = null;
                return;
            }
            entry.gifDecoderTask = null;
            entry.gifDecoder = mFuture.get();
            if (entry.showDrmMicroThumb && entry.enteredConsumeMode == false) {
                Log.d(TAG, "updateGifDecoder:no animation for non-consume drm");
            } else {
                startGifAnimation(mVersion);
            }
            updateImageRequests();
        }
    }

    private void startGifAnimation(long version) {
        ImageEntry entry = mImageCache.get(version);
        if (entry.gifDecoder != null && 
            entry.gifDecoder.getTotalFrameCount() != GifDecoder.INVALID_VALUE) {
            //we also have to check if width & height got from GifDecoder
            //is valid.
            //Note: in Bitmap object, the max Bitmap buffer size max be
            //within a 32bit integer. w * h * 4 < 2^32. 4 for AGGB.
            if (entry.gifDecoder.getWidth() <= 0 ||
                entry.gifDecoder.getHeight() <= 0 ||
                (long)entry.gifDecoder.getWidth() * 
                (long)entry.gifDecoder.getHeight() * 4
                >= (long)65536*(long)65536) {
                Log.e(TAG,"startGifAnimation:illegal gif frame dimension");
                return;
            }
            //create gif frame bitmap
            entry.currentGifFrame = 
                        Bitmap.createBitmap(entry.gifDecoder.getWidth(), 
                                            entry.gifDecoder.getHeight(),
                                            Bitmap.Config.ARGB_8888);
            Utils.assertTrue(null != entry.currentGifFrame);
            entry.currentGifFrame = BitmapUtils.resizeDownBySideLength(entry.currentGifFrame, MediatekFeature.getMaxTextureSize(), true);
            //update UI
            if (mDataListener != null) {
                mDataListener.onPhotoAvailable(version, true);
            }
            int currentIndex = mCurrentIndex;
            if (version == getVersion(currentIndex)) {
                //MediaItem item = mData[currentIndex % DATA_CACHE_SIZE];

                GifAnimation gifAnimation = new GifAnimation();
                gifAnimation.gifDecoder = entry.gifDecoder;
                //gifAnimation.mediaItem = item;
                gifAnimation.animatedIndex = currentIndex;
                gifAnimation.entry = entry;

                mMainHandler.sendMessage(
                        mMainHandler.obtainMessage(MSG_RUN_OBJECT, 
                                  new GifAnimationRunnable(gifAnimation)));
            }
        }
    }

    private class GifAnimationRunnable implements Runnable {

        private GifAnimation mGifAnimation;

        private void releaseGifResource() {
            if (null != mGifAnimation) {
                mGifAnimation.gifDecoder = null;
                //mGifAnimation.mediaItem = null;
                mGifAnimation = null;
            }
        }

        public GifAnimationRunnable(GifAnimation gifAnimation) {
            mGifAnimation = gifAnimation;
            if (null == mGifAnimation || null == mGifAnimation.gifDecoder) {
                Log.e("PhotoDataAdapter","GifAnimationRunnable:invalid GifDecoder");
                releaseGifResource();
                return;
            }
            if (mGifAnimation.animatedIndex != mCurrentIndex) {
                Log.i("PhotoDataAdapter","GifAnimationRunnable:image changed");
                releaseGifResource();
                return;
            }
            //prepare Gif animation state
            mGifAnimation.currentFrame = 0;
            mGifAnimation.totalFrameCount =
                                   mGifAnimation.gifDecoder.getTotalFrameCount();
            if (mGifAnimation.totalFrameCount <= 1) {
                Log.w("PhotoDataAdapter",
                       "GifAnimationRunnable:invalid frame count, NO animation!");
                releaseGifResource();
                return;
            }
        }

        @Override
        public void run() {
            if (!mIsActive) {
                Log.i("PhotoDataAdapter","GifAnimationRunnable:run:already paused");
                releaseGifResource();
                return;
            }

            if (null == mGifAnimation || null == mGifAnimation.gifDecoder) {
                Log.e("PhotoDataAdapter","GifAnimationRunnable:run:invalid GifDecoder");
                releaseGifResource();
                return;
            }

            if (mGifAnimation.animatedIndex != mCurrentIndex) {
                Log.i("PhotoDataAdapter","GifAnimationRunnable:run:image changed");
                releaseGifResource();
                return;
            }

            long preTime = SystemClock.uptimeMillis();

            //assign decoded bitmap to CurrentGifFrame
            Bitmap curBitmap = mGifAnimation.gifDecoder.getFrameBitmap(mGifAnimation.currentFrame);
            if (null == curBitmap) {
                Log.e("PhotoDataAdapter","GifAnimationRunnable:onFutureDone:got null frame!");
                releaseGifResource();
                return;
            }
            curBitmap = BitmapUtils.resizeDownBySideLength(curBitmap, MediatekFeature.getMaxTextureSize(), true);
Log.i("PhotoDataAdapter","GifAnimationRunnable:run:update frame["+(mGifAnimation.currentFrame)+"]");

            //get curent frame duration
            long curDuration = mGifAnimation.gifDecoder.getFrameDuration(mGifAnimation.currentFrame);
            //calculate next frame index
            mGifAnimation.currentFrame = (mGifAnimation.currentFrame + 1) % mGifAnimation.totalFrameCount;

            //check if animation is cancelled
            if (null == mGifAnimation.entry.currentGifFrame) {
                Log.w("PhotoDataAdapter","GifAnimationRunnable:onFutureDone:animation cancelled");
                releaseGifResource();
                curBitmap.recycle();
                return;
            }

            //update Current Gif Frame
            Canvas canvas = new Canvas(mGifAnimation.entry.currentGifFrame);
            canvas.drawColor(0xFFFFFFFF);
            Matrix m = new Matrix();
            canvas.drawBitmap(curBitmap,m,null);
            
            curBitmap.recycle();
            updateTileProvider(mGifAnimation.entry);
            mPhotoView.notifyImageInvalidated(0);

            mMainHandler.sendMessageAtTime(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this), (curDuration+preTime));
        }
    }

    private static class GifAnimation {
        public ImageEntry entry;
        public GifDecoder gifDecoder;
        public int animatedIndex;
        public int currentFrame;
        public int totalFrameCount;
    }

    private class DrmScreenNailJob implements Job<Bitmap> {
        private MediaItem mItem;

        public DrmScreenNailJob(MediaItem item) {
            mItem = item;
        }

        @Override
        public Bitmap run(JobContext jc) {
            // when user shift to a drm file, thumbnail should be displayed first,
            // when user tap center of the thumbnail, a dialog prompts, indicating
            // if the user still have rights to view/play this drm media, and show
            // options for user to consume. If user finally click consume, we have
            // to re-decode a TYPE_THUMBNAIL kind bitmap, and display it on the
            // the screen.
            // Note: only TYPE_MICROTHUMBNAIL kind thumbnail has to be processed
            // before displaying on the screen.
            ImageEntry entry = mImageCache.get(mItem.getDataVersion());
            if (entry == null) {
                Log.e(TAG,"DrmScreenNailJob:run:got null ImageEntry from mItem:"+mItem);
                return null;
            }
            LocalMediaItem localMediaItem = (LocalMediaItem)mItem;
            entry.isDrm = localMediaItem.isDrm();
            if (!entry.isDrm) {
                Log.e(TAG,"DrmScreenNailJob:run:try to decode non-drm media");
                return null;
            }
            boolean hasDrmRights = localMediaItem.hasDrmRights();
            boolean isFlDrm = localMediaItem.isDrmMethod(DrmStore.DrmMethod.METHOD_FL);
            if (isFlDrm || entry.enteredConsumeMode) {
                entry.showDrmMicroThumb = false;
                Bitmap bitmap = mItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
                if (jc.isCancelled()) return null;
                if (bitmap != null) {
                    bitmap = BitmapUtils.rotateBitmap(bitmap,
                        mItem.getRotation() - mItem.getFullImageRotation(), true);
                }
                return bitmap;
            } else {
                entry.showDrmMicroThumb = true;
                Bitmap bitmap = null;
                if (hasDrmRights) {
                    //we only decode drm micro thumbnail when the media has drm rights
                    //Note: for those media without drm rights, show a default thumbnail
                    bitmap = mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL).run(jc);
                    if (jc.isCancelled()) return null;
                    if (bitmap != null) {
                        bitmap = BitmapUtils.rotateBitmap(bitmap,
                            mItem.getRotation() - mItem.getFullImageRotation(), true);
                        //we should resize decode thumbnail to 200x200, that it will
                        //not scale in display, resulting clear and beautifull lock icon
                        bitmap = DrmHelper.resizeThumbToDefaultSize((Activity)mActivity,
                                                                    bitmap);
                    }
                }
                //if returned bitmap is null, possiblely the media has no drm rights,
                //or decode thumbnail failed even if it has drm rights.
                //In this circumstance, we create a default icon
                if (null == bitmap) {
                    bitmap = DrmHelper.createDefaultDrmMicroThumb((Activity)mActivity);
                }
                bitmap = DrmHelper.ensureBitmapMutable(bitmap);
                //draw drm icons onto the thumbnail
                DrmHelper.drawOverlayToBottomRight((Context)mActivity, bitmap, 
                                                   hasDrmRights);
                return bitmap;
            }
        }
    }

    private class DrmScreenNailListener
            implements Runnable, FutureListener<Bitmap> {
        private final long mVersion;
        private Future<Bitmap> mFuture;

        public DrmScreenNailListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<Bitmap> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateDrmScreenNail(mVersion, mFuture);
        }
    }

    private void updateDrmScreenNail(long version, Future<Bitmap> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.drmScreenNailTask != future) {
            Bitmap screenNail = future.get();
            if (screenNail != null) screenNail.recycle();
            return;
        }

        entry.drmScreenNailTask = null;
        entry.drmScreenNail = future.get();

        if (entry.drmScreenNail == null) {
            entry.failToLoad = true;
            //update tile provider
            if (version == getVersion(mCurrentIndex)) {
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
            }
        } else {
            if (mDataListener != null) {
                mDataListener.onPhotoAvailable(version, false);
            }
            for (int i = -1; i <=1; ++i) {
                if (version == getVersion(mCurrentIndex + i)) {
                    if (i == 0) updateTileProvider(entry);
                    mPhotoView.notifyImageInvalidated(i);
                }
            }
        }
        updateImageRequests();
    }

    public void enterConsumeMode() {
        if (!mIsDrmSupported) return;
        // update mImagecache to avoid entry is null
        updateImageCache();
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));

        if (entry == null) {
            Log.i(TAG, "enter is null");
            return;
        }
        if (entry.enteredConsumeMode) {
            //we should ignore it?
            return;
        }
        //cancel previous drm bitmap loading task if any
        if (entry.drmScreenNailTask != null) {
            entry.drmScreenNailTask.cancel();
            entry.drmScreenNailTask = null;
        }
        entry.requestedBits &= ~BIT_DRM_SCREEN_NAIL;

        //enter consumed mode
        entry.enteredConsumeMode = true;

        //record drm time interval type
        MediaItem item = mData[mCurrentIndex % DATA_CACHE_SIZE];
        if (item instanceof LocalMediaItem &&
            ((LocalMediaItem)item).isTimeInterval()) {
            mTimeIntervalDRM = true;
        }

        //clears back up image in tile image view adapter
        mTileProvider.clear();
        mPhotoView.notifyImageInvalidated(0);

        // If current media item is consume, DRM rights status other
        // of media item may be affected, so we have to update cache
        updateDrmCache();
        updateImageRequests();
        updateTileProvider();
        mPhotoView.notifyOnNewImage();
        fireModelInvalidated();
        // play gif animation if possible
        startGifAnimation(getVersion(mCurrentIndex));
    }

    public boolean enteredConsumeMode() {
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));
        return entry == null ? false : entry.enteredConsumeMode;
    }

    private void saveDrmConsumeStatus() {
        resetDrmConsumeStatus();
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));
        if (null != entry && entry.enteredConsumeMode) {
            MediaItem current = mData[mCurrentIndex % DATA_CACHE_SIZE];
            mConsumedItemPath = current == null ? null : current.getPath();
        }
    }

    private void restoreDrmConsumeStatus() {
        if (null == mConsumedItemPath) return;
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));

        //for time interval kind drm media, check if we still have
        //rights to display full image
        if (mTimeIntervalDRM) {
            Log.d(TAG,"restoreDrmConsumeStatus:for time interval media...");
            if (mCurrentIndex < 0){
                return;
            }
            MediaItem item = mData[mCurrentIndex % DATA_CACHE_SIZE];
            if (item == null){
                return;
            }
            if (!((LocalMediaItem)item).hasDrmRights()) {
                Log.d(TAG,"restoreDrmConsumeStatus:we have no rights ");
                resetDrmConsumeStatus();
                return;
            }
        }

        if (null != entry) {
            if (mItemPath == mConsumedItemPath) {
                if (entry.enteredConsumeMode) {
                    return;
                }
                //cancel previous drm bitmap loading task if any
                if (entry.drmScreenNailTask != null) {
                    entry.drmScreenNailTask.cancel();
                    entry.drmScreenNailTask = null;
                }
                entry.requestedBits &= ~BIT_DRM_SCREEN_NAIL;
                //restore consumed mode
                entry.enteredConsumeMode = true;
            }
            resetDrmConsumeStatus();
        }
    }

    private void resetDrmConsumeStatus() {
        mConsumedItemPath = null;
    }

    private static class OriginScreenNailJob implements Job<Bitmap> {
        private MediaItem mItem;

        public OriginScreenNailJob(MediaItem item) {
            mItem = item;
        }

        @Override
        public Bitmap run(JobContext jc) {
            Bitmap bitmap = mItem.requestImageWithPostProc(
                        MediatekFeature.isPictureQualityEnhanceSupported(),
                        MediaItem.TYPE_THUMBNAIL).run(jc);
            if (jc.isCancelled()) return null;
            if (bitmap != null) {
                bitmap = BitmapUtils.rotateBitmap(bitmap,
                    mItem.getRotation() - mItem.getFullImageRotation(), true);
            }
            return bitmap;
        }
    }

    private class OriginScreenNailListener
            implements Runnable, FutureListener<Bitmap> {
        private final long mVersion;
        private Future<Bitmap> mFuture;

        public OriginScreenNailListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<Bitmap> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateOriginScreenNail(mVersion, mFuture);
        }
    }

    private void updateOriginScreenNail(long version, Future<Bitmap> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.originScreenNailTask != future) {
            Bitmap screenNail = future.get();
            if (screenNail != null) screenNail.recycle();
            return;
        }

        entry.originScreenNailTask = null;
        entry.originScreenNail = future.get();

        if (entry.originScreenNail != null) {
            if (mDataListener != null) {
                mDataListener.onPhotoAvailable(version, false);
            }
            if (version == getVersion(mCurrentIndex)) {
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
            }
        }
        updateImageRequests();
    }

    private class SecondScreenNailJob implements Job<MediatekFeature.DataBundle> {
        private MediaItem mItem;
        private MediatekFeature.Params mParams;

        public SecondScreenNailJob(MediaItem item,
                                   MediatekFeature.Params params) {
            mItem = item;
            mParams = params;
        }

        @Override
        public MediatekFeature.DataBundle run(JobContext jc) {
            // got the second frame of target stereo photo or video
            MediatekFeature.DataBundle dataBundle
                    = mItem.requestImage(MediaItem.TYPE_THUMBNAIL,
                                                     mParams).run(jc);
            if (jc.isCancelled()) return null;
            //if (bitmap != null) {
            //    bitmap = BitmapUtils.rotateBitmap(bitmap,
            //        mItem.getRotation() - mItem.getFullImageRotation(), true);
            //}
            return dataBundle;
        }
    }

    private class SecondScreenNailListener
            implements Runnable, FutureListener<MediatekFeature.DataBundle> {
        private final long mVersion;
        private Future<MediatekFeature.DataBundle> mFuture;

        public SecondScreenNailListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<MediatekFeature.DataBundle> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateSecondScreenNail(mVersion, mFuture);
        }
    }

    private void updateSecondScreenNail(long version, 
                           Future<MediatekFeature.DataBundle> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.secondScreenNailTask != future) {
            MediatekFeature.DataBundle dataBundle = future.get();
            if (dataBundle != null) dataBundle.recycle();
            return;
        }

        entry.secondScreenNailTask = null;
        MediatekFeature.DataBundle dataBundle = future.get();
        if (null == dataBundle) {
            Log.w(TAG, "updateSecondScreenNail:got null bundle()");
            updateImageRequests();
            return;
        }
        entry.firstScreenNail = dataBundle.firstFrame;
        entry.secondScreenNail = dataBundle.secondFrame;

        if (entry.secondScreenNail != null) {
            Log.i(TAG,"udateSecondScreenNail:got second screen nail");
            if (null == entry.screenNail) {
                Log.w(TAG, "updateSecondScreenNail:got null entry.screenNail");
                updateImageRequests();
                return;
            }
            //make sure second screen nail has the same dimension with the
            //screen nail
            if (entry.screenNail.getWidth() != entry.secondScreenNail.getWidth() ||
                entry.screenNail.getHeight() != entry.secondScreenNail.getHeight()) {
                Log.w(TAG,"updateSecondScreenNail:resize second to screen nail");
                try {
                    //create a new bitmap that has the same dimension as screen nail
                    Bitmap temp = Bitmap.createBitmap(entry.screenNail.getWidth(), 
                                                      entry.screenNail.getHeight(),
                                                      Bitmap.Config.ARGB_8888);
                    //draw second screen nail onto it
                    Canvas canvas = new Canvas(temp);
                    Rect src = new Rect(0, 0, entry.secondScreenNail.getWidth(),
                                              entry.secondScreenNail.getHeight());
                    RectF dst = new RectF(0, 0, entry.screenNail.getWidth(),
                                                entry.screenNail.getHeight());
                    canvas.drawBitmap(entry.secondScreenNail, src, dst, null);
                    //recycle original and replace
                    entry.secondScreenNail.recycle();
                    entry.secondScreenNail = temp;
                } catch(OutOfMemoryError e) {
                    Log.w(TAG,"updateSecondScreenNail:out memory when resize 2nd");
                    e.printStackTrace();
                }
            }

            MediaItem item = mData[mCurrentIndex % DATA_CACHE_SIZE];
            //gernerate auto convergence data
            if (mIsStereoConvergenceSupported &&
                (item.getSupportedOperations() & 
                    MediaObject.SUPPORT_CONVERT_TO_3D) == 0 &&
                entry.screenNail.getWidth() == entry.secondScreenNail.getWidth() &&
                entry.screenNail.getHeight() == entry.secondScreenNail.getHeight()) {
                Log.i(TAG,"updateSecondScreenNail:call stereo convergence");
                //for truely stereo image, we calculates the convergence data
                //2d-to-3d image is not designed to avail the convergence data
                entry.stereoConvergence = Stereo3DConvergence.execute(
                                          entry.screenNail, entry.secondScreenNail);
            }

            if (version == getVersion(mCurrentIndex)) {
                Log.v(TAG,"updateSecondScreenNail:update tileProvider");
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
                //MediaItem item = mData[mCurrentIndex % DATA_CACHE_SIZE];
                //the 2d to 3d picture should not enter stereo mode.
                //Enter stereo mode only when stereo photo is encountered
                if ((item.getSupportedOperations() & 
                     MediaObject.SUPPORT_CONVERT_TO_3D) == 0) {
                    //stereo photo
                    //after loaded second image, all enter stereo mode
                    mPhotoView.allowStereoMode(true);
                    mPhotoView.setStereoMode(true);
                } else {
                    //2d to 3d kind
                    //after loaded second image, all exit stereo mode
                    mPhotoView.allowStereoMode(false);
                    mPhotoView.setStereoMode(false);
                }
            }
        }
        updateImageRequests();
    }

    //we want to add zoom capacity when user is viewing 3D stereo image.
    //To save memory usage, we should use region decoder to decode tiles
    //on the fly when user is zooming and panning image.
    //Temporarily, there are three kinds of Stereo Image:
    //1, MPO stereo image
    //2, JPS stereo image
    //3, 3D image that converted from 2D by algorithm
    //
    //The MPO stereo image has its second image appended to the first one,
    //making creating region decoder a bit complex.
    //Perhaps the best way is to retrieve the MPO info, locate the position
    //of second image, read the second image into memory buffer, and create
    //region decoder from buffer. This way is most straight forward and
    //computation saving. But temporarily, the API of MPO Decoder does not
    //provide these infos, and adding api takes several efforts, so we temporarily
    //go second way: decode the second image by MPO Decoder (resizing if possible),
    //compress it into memory buffer and create Region Decoder from buffer.
    //This way is not very efficient, because it involes decoding and compressing
    //before we get the JPEG buffer in memory. The good aspect of this way is
    //that before compress, we can change the resolution of second image before
    //or after decoding, making the buffer size small and easy to decode.
    //When we get a JPEG buffer in memory, we can create Rgion Decoder for second
    //frame of MPO file. (MPO file will have two region decoder, one from origion
    //file, the other from decode-compress JPEG buffer)
    //
    //The JPS stereo image is a bit complex. The left and right frame reside
    //in the same frame, and we actually can use only one region decoder to
    //retrieve all tiles for both frame.
    //Can we crop the second frame, compress it to buffer, and create a new
    //Region Decoder for second frame? this is very good aspect when we control
    //decoding processes.
    //Conclusion:for JPS, we crop the right frame (resizing if need) from original
    //file, resize it into a reasonal size and compress it into a buffer.
    //
    //The 2D-to-3D image is most complext. As there is no corresponding 3D image
    //in the sdcard, we should create a 2D-to-3D image when needed.
    //Creating 2D-to-3D image is straight forward, but there are two risk
    //1, Out of memory accur when converting
    //2, Source 2D image is not qualified to undertake conversion.
    //then the converted 3D image should be compressed to buffer, to create
    //Region Decoder.
    //Wait! we can splite the converted Bitmap and compressed it into two buffer,
    //and creating two region decoder! (Is there any risk when spliting Bitmap?
    //Can two bitmap created on top of origional converted bitmap, consuming no
    //extra memory? If so, we create two Region Decoders for converted 3D pairs)
    //Here is how it goes: 
    //1, We have original Bitmap [A], then we use 2d-to-3 dconversion API to generate
    // a combined Bitmap pair [A1][A2], notice that [A] and [A1][A2] exist together.
    //2, Recycle [A], and create a Bitmap to store [A1], notice that [A1]
    // and [A1][A2] exit together
    //3, Compress [A1] to buffer and recycle [A1] (only [A1][A2] exits)
    //4, Create a Bitmap to store [A2], notice that [A2] and [A1][A2] exist together
    //5, Compress [A2] to buffer, recycel [A2] and [A1][A2]
    //6, We get only two buffer for [A1] and [A2] separately.
    //the above procedure can ganrentee that at most three times buffer exist at the
    //same time, and generate to JPEG buffer for region decoder. This may encounter
    //OutOfMemory error, so we have to budget and estimate total size of [A].
    //Another issue is that the api of 2d-to-3d only accept even resolution, so we
    //have to modify the procedure accordingly:Create a larger Bitmap [B] to contain
    //content of [A], then create [B1][B2], then create [A1] from [B1][B2] and
    //compress, recycle [A1] and create [A2], compress [A2]. Then we got two buffer.



    private class StereoFullImageJob implements Job<MediatekFeature.DataBundle> {
        private MediaItem mItem;
        private MediatekFeature.Params mParams;

        public StereoFullImageJob(MediaItem item,
                                   MediatekFeature.Params params) {
            mItem = item;
            mParams = params;
        }

        @Override
        public MediatekFeature.DataBundle run(JobContext jc) {
            MediatekFeature.DataBundle dataBundle
                    = mItem.requestImage(MediaItem.TYPE_THUMBNAIL,
                                                     mParams).run(jc);
            if (jc.isCancelled()) return null;
            return dataBundle;
        }
    }

    private class StereoFullImageListener
            implements Runnable, FutureListener<MediatekFeature.DataBundle> {
        private final long mVersion;
        private Future<MediatekFeature.DataBundle> mFuture;

        public StereoFullImageListener(long version) {
            mVersion = version;
        }

        @Override
        public void onFutureDone(Future<MediatekFeature.DataBundle> future) {
            mFuture = future;
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
        }

        @Override
        public void run() {
            updateStereoFullImage(mVersion, mFuture);
        }
    }

    private void updateStereoFullImage(long version, 
                           Future<MediatekFeature.DataBundle> future) {
        Log.v(TAG,"updateStereoFullImage()");
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.stereoFullImageTask != future) {
            Log.w(TAG,"updateStereoFullImage:wrong task:"+future);
            MediatekFeature.DataBundle dataBundle = future.get();
            if (dataBundle != null) dataBundle.recycle();
            return;
        }

        entry.stereoFullImageTask = null;
        MediatekFeature.DataBundle dataBundle = future.get();
        if (null == dataBundle) {
            Log.w(TAG, "updateStereoFullImage:got null bundle()");
            updateImageRequests();
            return;
        }

        if (null != entry.firstFullImage) {
            entry.firstFullImage.release();
        }
        if (null != entry.secondFullImage) {
            entry.secondFullImage.release();
        }
        entry.firstFullImage = dataBundle.firstFullFrame;
        entry.secondFullImage = dataBundle.secondFullFrame;
        Log.i(TAG,"updateStereoFullImage:entry.secondFullImage="+entry.secondFullImage);

        if (entry.secondFullImage != null) {
            Log.i(TAG,"updateStereoFullImage:got second full image");

            if (version == getVersion(mCurrentIndex)) {
                Log.v(TAG,"updateStereoFullImage:update tileProvider");
                updateTileProvider(entry);
                mPhotoView.notifyImageInvalidated(0);
            }
        }
        updateImageRequests();
    }

    public void triggerStereoFullImage() {
        Log.i(TAG,"trigger stereo full image()");
        if (!mIsStereoDisplaySupported) return;

        updateImageCache();
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));

        if (entry == null) {
            Log.i(TAG, "entry is null");
            return;
        }

        if (null != entry.stereoFullImageTask) {
            Log.d(TAG,"trigger stereo full image :already started");
            return;
        }

        if (null != entry.firstFullImage &&
            null != entry.secondFullImage) {
            Log.d(TAG,"trigger stereo full image :already done");
            return;
        }

        MediaItem item = mData[mCurrentIndex % DATA_CACHE_SIZE];
        //create stereo full image task for nomal image
        if ((entry.requestedBits & BIT_STEREO_FULL_IMAGE) == 0 && 
            (item.getSupportedOperations() & 
                        MediaObject.SUPPORT_CONVERT_TO_3D) != 0 &&
            (item.getSupportedOperations() & 
                        MediaItem.SUPPORT_STEREO_DISPLAY) != 0) {
            Log.v(TAG,"create stereo full image task for 2d image...");
            //we should get the layout of stereo image first
            entry.stereoLayout = item.getStereoLayout();
            //we create a decode job
            entry.requestedBits |= BIT_STEREO_FULL_IMAGE;
            //create mediatek parameters
            MediatekFeature.Params params = new MediatekFeature.Params();
            params.inOriginalFrame = false;
            params.inFirstFrame = false;
            params.inSecondFrame = false;
            params.inFirstFullFrame = true;
            params.inSecondFullFrame = true;
            params.inRotation = item.getRotation();
            entry.stereoFullImageTask = mThreadPool.submit(
                new StereoFullImageJob(item, params),
                new StereoFullImageListener(item.getDataVersion()));
            // request second screen nail for stereo display
            return;
        }
    }


    private void updateImageData(int index, ImageEntry entry, ImageData imageData) {
        if (null == entry || null == imageData) return;
        //update drm info and screen nail
        //if (mIsDrmSupported) {
        //    int subType = 0;
        //    MediaItem item = mData[index % DATA_CACHE_SIZE];
        //    if (item instanceof LocalMediaItem) {
        //        subType = ((LocalMediaItem)item).getSubType();
        //    }
        //    imageData.setSubType(subType);
        //    if (entry.isDrm) {
        //        imageData.setShowDrmMicroThumb(entry.showDrmMicroThumb);
        //        //update drm micro thumb if needed
        //        imageData.setScreenNail(entry.drmScreenNail);
        //    }
        //}
        //update original image dimension
        if (MediatekFeature.isCMCC()) {
            imageData.originWidth = entry.originWidth;
            imageData.originHeight = entry.originHeight;
            imageData.suggestFullScreen = entry.suggestFullScreen;
        }
    }

    private void updateImageEntry(ImageEntry entry, MediaItem item, int which) {
        if (null == entry || null == item) return;


        if (mIsDrmSupported && (item instanceof LocalMediaItem) && 
            ((LocalMediaItem)item).isDrm()) {
            entry.originWidth = item.getWidth();
            entry.originHeight = item.getHeight();
            //update video info
            entry.suggestFullScreen = suggestFullScreen(item);
            //for Stereo display feature, we have to adjust dimensions
            return;
        }

        if ((item instanceof LocalMediaItem) && which == BIT_SCREEN_NAIL
                && (entry.requestedBits & BIT_SCREEN_NAIL) == 0) {
            //we update original dimension only the first time 
            //we decode screen nail.
            entry.originWidth = item.getWidth();
            entry.originHeight = item.getHeight();
            //update video info
            entry.suggestFullScreen = suggestFullScreen(item);
            //for Stereo display feature, we have to adjust dimensions
        }
    }

    private boolean suggestFullScreen(MediaItem item) {
        if (!MediatekFeature.isCMCC()) return true;
        if (null == item) {
            return false;
        } else {
            return (item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO)
                || ((item.getSubType() & MediaObject.SUBTYPE_MPO_MAV) != 0);
        }
    }


    private static class ImageEntry {
        public int requestedBits = 0;
        public int rotation;
        public BitmapRegionDecoder fullImage;
        public Bitmap screenNail;
        public Future<Bitmap> screenNailTask;
        public Future<BitmapRegionDecoder> fullImageTask;
        public boolean failToLoad = false;
        //the below members are added for Gif animation
        public GifDecoder gifDecoder;
        public Future<GifDecoder> gifDecoderTask;
        public Bitmap currentGifFrame;
        //added to support DRM ;for Drm image no need to load screen nail and full image
        public boolean isDrm;
        //added to support Drm Gif
        public boolean isDrmGif ;
        //add consume mode Drm gif image
        public boolean isDrmConsume;
        public boolean showDrmMicroThumb;
        public boolean enteredConsumeMode;
        public Future<Bitmap> drmScreenNailTask;
        public Bitmap drmScreenNail;
        //added to gain better image quality
        public Future<Bitmap> originScreenNailTask;
        public Bitmap originScreenNail;
        //added to support stereo display
        public Future<MediatekFeature.DataBundle> secondScreenNailTask;
        public Bitmap firstScreenNail;
        public Bitmap secondScreenNail;
        public Future<MediatekFeature.DataBundle> stereoFullImageTask;
        public MediatekFeature.RegionDecoder firstFullImage;
        public MediatekFeature.RegionDecoder secondFullImage;
        public Stereo3DConvergence stereoConvergence;
        public int stereoLayout;
        //added for cmcc feature
        public int originWidth;
        public int originHeight;
        public boolean suggestFullScreen;
    }

    private class SourceListener implements ContentListener {
        public void onContentDirty() {
            if (mReloadTask != null) mReloadTask.notifyDirty();
        }
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(
                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UpdateInfo {
        public long version;
        public boolean reloadContent;
        public Path target;
        public int indexHint;
        public int contentStart;
        public int contentEnd;

        public int size;
        public ArrayList<MediaItem> items;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {

        private boolean needContentReload() {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                if (mData[i % DATA_CACHE_SIZE] == null) return true;
            }
            MediaItem current = mData[mCurrentIndex % DATA_CACHE_SIZE];
            return current == null || current.getPath() != mItemPath;
        }

        @Override
        public UpdateInfo call() throws Exception {
            // TODO: Try to load some data in first update
            UpdateInfo info = new UpdateInfo();
            info.version = mSourceVersion;
            info.reloadContent = needContentReload();
            info.target = mItemPath;
            info.indexHint = mCurrentIndex;
            info.contentStart = mContentStart;
            info.contentEnd = mContentEnd;
            info.size = mSize;
            return info;
        }
    }

    private class UpdateContent implements Callable<Void> {
        UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo updateInfo) {
            mUpdateInfo = updateInfo;
        }

        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;

            if (info.size != mSize) {
                mSize = info.size;
                if (mContentEnd > mSize) mContentEnd = mSize;
                if (mActiveEnd > mSize) mActiveEnd = mSize;
            }

            if (info.indexHint == MediaSet.INDEX_NOT_FOUND) {
                // The image has been deleted, clear mItemPath, the
                // mCurrentIndex will be updated in the updateCurrentItem().
                mItemPath = null;
                updateCurrentItem();
            } else {
                mCurrentIndex = info.indexHint;
            }

            updateSlidingWindow();

            if (info.items != null) {
                int start = Math.max(info.contentStart, mContentStart);
                int end = Math.min(info.contentStart + info.items.size(), mContentEnd);
                int dataIndex = start % DATA_CACHE_SIZE;
                for (int i = start; i < end; ++i) {
                    mData[dataIndex] = info.items.get(i - info.contentStart);
                    if (++dataIndex == DATA_CACHE_SIZE) dataIndex = 0;
                }
            }
            if (mItemPath == null) {
                MediaItem current = mData[mCurrentIndex % DATA_CACHE_SIZE];
                mItemPath = current == null ? null : current.getPath();
            }

            //added for drm consume behavior
            if (mIsDrmSupported) {
                restoreDrmConsumeStatus();
            }

            updateImageCache();
            updateTileProvider();
            updateImageRequests();
            fireModelInvalidated();
            return null;
        }

        private void updateCurrentItem() {
            if (mSize == 0) return;
            if (mCurrentIndex >= mSize) {
                mCurrentIndex = mSize - 1;
                mPhotoView.notifyOnNewImage();
                mPhotoView.startSlideInAnimation(PhotoView.TRANS_SLIDE_IN_LEFT);
            } else {
                mPhotoView.notifyOnNewImage();
                mPhotoView.startSlideInAnimation(PhotoView.TRANS_SLIDE_IN_RIGHT);
            }
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;

        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading) return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            while (mActive) {
                synchronized (this) {
                    if (!mDirty && mActive) {
                        updateLoading(false);
                        Utils.waitWithoutInterrupt(this);
                        continue;
                    }
                }
                mDirty = false;
                UpdateInfo info = executeAndWait(new GetUpdateInfo());
                synchronized (DataManager.LOCK) {
                    updateLoading(true);
                    long version = mSource.reload();
                    if (info.version != version) {
                        info.reloadContent = true;
                        info.size = mSource.getMediaItemCount();
                    }
                    if (!info.reloadContent) continue;
                    info.items =  mSource.getMediaItem(info.contentStart, info.contentEnd);
                    MediaItem item = findCurrentMediaItem(info);
                    if (item == null || item.getPath() != info.target) {
                        info.indexHint = findIndexOfTarget(info);
                    }
                }
                executeAndWait(new UpdateContent(info));
            }
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }

        private MediaItem findCurrentMediaItem(UpdateInfo info) {
            ArrayList<MediaItem> items = info.items;
            int index = info.indexHint - info.contentStart;
            return index < 0 || index >= items.size() ? null : items.get(index);
        }

        private int findIndexOfTarget(UpdateInfo info) {
            if (info.target == null) return info.indexHint;
            ArrayList<MediaItem> items = info.items;

            // First, try to find the item in the data just loaded
            if (items != null) {
                for (int i = 0, n = items.size(); i < n; ++i) {
                    if (items.get(i) == null) continue;
                    if (items.get(i).getPath() == info.target) return i + info.contentStart;
                }
            }

            // Not found, find it in mSource.
            return mSource.getIndexOfItem(info.target, info.indexHint);
        }
    }
}
