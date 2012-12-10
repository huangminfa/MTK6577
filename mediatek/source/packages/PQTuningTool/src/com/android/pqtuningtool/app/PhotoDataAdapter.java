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

package  com.android.pqtuningtool.app;

import  com.android.pqtuningtool.common.BitmapUtils;
import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.data.ContentListener;
import  com.android.pqtuningtool.data.DataManager;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.MediaObject;
import  com.android.pqtuningtool.data.MediaSet;
import  com.android.pqtuningtool.data.Path;
import  com.android.pqtuningtool.ui.PhotoView;
import  com.android.pqtuningtool.ui.PhotoView.ImageData;
import  com.android.pqtuningtool.ui.SynchronizedHandler;
import  com.android.pqtuningtool.ui.TileImageViewAdapter;
import  com.android.pqtuningtool.util.Future;
import  com.android.pqtuningtool.util.FutureListener;
import  com.android.pqtuningtool.util.MtkLog;
import  com.android.pqtuningtool.util.ThreadPool;
import  com.android.pqtuningtool.util.ThreadPool.Job;
import  com.android.pqtuningtool.util.ThreadPool.JobContext;

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

import  com.android.pqtuningtool.data.LocalMediaItem;
import  com.android.pqtuningtool.data.LocalVideo;
import  com.android.pqtuningtool.util.MediatekFeature;
import  com.android.pqtuningtool.util.DrmHelper;
import com.mediatek.gifDecoder.GifDecoder;
import android.os.SystemClock;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    private static final int IMAGE_CACHE_SIZE = 5;

    private static final int BIT_SCREEN_NAIL = 1;
    private static final int BIT_FULL_IMAGE = 2;
    //added to support GIF animation
    private static final int BIT_GIF_ANIMATION = 4;
    //added to support DRM micro-thumb
    private static final int BIT_DRM_SCREEN_NAIL = 8;

    private static final long VERSION_OUT_OF_RANGE = MediaObject.nextVersionNumber();

    private static final boolean mIsGifAnimationSupported = 
                               MediatekFeature.isGifAnimationSupported();
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private final GalleryActivity mActivity;
    private Path mConsumedItemPath;


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

        int k = 0;
        sImageFetchSeq = new ImageFetch[1 + drmRequestCount +
                                (IMAGE_CACHE_SIZE - 1) * (2 + 2 * drmRequestCount) 
                                        + 3 + gifRequestCount];
        sImageFetchSeq[k++] = new ImageFetch(0, BIT_SCREEN_NAIL);

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

        sImageFetchSeq[k++] = new ImageFetch(0, BIT_FULL_IMAGE);
        sImageFetchSeq[k++] = new ImageFetch(1, BIT_FULL_IMAGE);
        sImageFetchSeq[k++] = new ImageFetch(-1, BIT_FULL_IMAGE);

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
                return new ImageData(screennail, entry.rotation, 
                             (entry.isDrm && entry.showDrmMicroThumb),subType);
            } else {
                return new ImageData(screennail, entry.rotation);
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
        mCurrentIndex = index;
        updateSlidingWindow();

        MediaItem item = mData[index % DATA_CACHE_SIZE];
        mItemPath = item == null ? null : item.getPath();

        updateImageCache();
        updateImageRequests();
        updateTileProvider();
        mPhotoView.notifyOnNewImage();

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
        return mTileProvider.getTile(level, x, y, tileSize);
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
        if (mIsDrmSupported && entry.isDrm) {
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

        Bitmap screenNail = entry.screenNail;
        BitmapRegionDecoder fullImage = entry.fullImage;
        if (screenNail != null) {
            if (mIsGifAnimationSupported && entry.currentGifFrame != null) {
                int width = entry.currentGifFrame.getWidth();
                int height = entry.currentGifFrame.getHeight();
                mTileProvider.setBackupImage(entry.currentGifFrame, width, height);
            } else if (fullImage != null) {
                mTileProvider.setBackupImage(screenNail,
                        fullImage.getWidth(), fullImage.getHeight());
                mTileProvider.setRegionDecoder(fullImage);
            } else {
                int width = screenNail.getWidth();
                int height = screenNail.getHeight();
                mTileProvider.setBackupImage(screenNail, width, height);
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
        }

        MediaItem item = mData[index % DATA_CACHE_SIZE];
        Utils.assertTrue(item != null);
        //remember that for current drm, it may decode large thumbnail
        if (mIsDrmSupported && (item instanceof LocalMediaItem) && 
            ((LocalMediaItem)item).isDrm()) {
            entry.isDrm = true;
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
            return null;
        }

        if (which == BIT_SCREEN_NAIL
                && (entry.requestedBits & BIT_SCREEN_NAIL) == 0) {
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
                & MediaItem.SUPPORT_FULL_IMAGE) != 0) {
            entry.requestedBits |= BIT_FULL_IMAGE;
            entry.fullImageTask = mThreadPool.submit(
                    item.requestLargeImage(),
                    new FullImageListener(item.getDataVersion()));
            // request full image
            return entry.fullImageTask;
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
                if(entry.gifDecoderTask != null) entry.gifDecoderTask.cancel();
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
            startGifAnimation(mVersion, mFuture);
        }
    }

    private void startGifAnimation(long version, Future<GifDecoder> future) {
        ImageEntry entry = mImageCache.get(version);
        if (entry == null || entry.gifDecoderTask != future) {
            GifDecoder gifDecoder = future.get();
            gifDecoder = null;
            return;
        }

        entry.gifDecoderTask = null;
        entry.gifDecoder = future.get();
        if (entry.gifDecoder != null && 
            entry.gifDecoder.getTotalFrameCount() != GifDecoder.INVALID_VALUE) {
            //create gif frame bitmap
            entry.currentGifFrame = 
                        Bitmap.createBitmap(entry.gifDecoder.getWidth(), 
                                            entry.gifDecoder.getHeight(),
                                            Bitmap.Config.ARGB_8888);
            Utils.assertTrue(null != entry.currentGifFrame);
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
        updateImageRequests();
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
            boolean isLocalVideo = localMediaItem instanceof LocalVideo;
            boolean hasDrmRights = localMediaItem.hasDrmRights();
            boolean isFlDrm = localMediaItem.isDrmMethod(DrmStore.DrmMethod.METHOD_FL);
            if (isLocalVideo || isFlDrm || entry.enteredConsumeMode) {
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
        ImageEntry entry = mImageCache.get(getVersion(mCurrentIndex));

        if (entry == null) {
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
        //added to support DRM
        public boolean isDrm;
        public boolean showDrmMicroThumb;
        public boolean enteredConsumeMode;
        public Future<Bitmap> drmScreenNailTask;
        public Bitmap drmScreenNail;
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
            MtkLog.i(TAG, "mIsLoading  is  " + mIsLoading);
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
            if (loading) {
                MtkLog.i(TAG, "MSG_LOAD_START ");
            } else {
                MtkLog.i(TAG, "MSG_LOAD_FINISH ");
            }
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
                    if (items.get(i).getPath() == info.target) return i + info.contentStart;
                }
            }

            // Not found, find it in mSource.
            return mSource.getIndexOfItem(info.target, info.indexHint);
        }
    }
}
