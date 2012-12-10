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

package  com.android.pqtuningtool.ui;

import android.graphics.Bitmap;
import android.os.Message;

import  com.android.pqtuningtool.app.GalleryActivity;
import  com.android.pqtuningtool.common.BitmapUtils;
import  com.android.pqtuningtool.common.LruCache;
import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.MediaObject;
import  com.android.pqtuningtool.data.Path;
import  com.android.pqtuningtool.util.Future;
import  com.android.pqtuningtool.util.FutureListener;
import  com.android.pqtuningtool.util.GalleryUtils;
import  com.android.pqtuningtool.util.JobLimiter;
import  com.android.pqtuningtool.util.ThreadPool.Job;
import  com.android.pqtuningtool.util.ThreadPool.JobContext;

public class AlbumSlidingWindow implements AlbumView.ModelListener {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumSlidingWindow";

    private static final int MSG_LOAD_BITMAP_DONE = 0;
    private static final int MSG_UPDATE_SLOT = 1;
    private static final int JOB_LIMIT = 2;
    private static final int PLACEHOLDER_COLOR = 0xFF222222;

    public static interface Listener {
        public void onSizeChanged(int size);
        public void onContentInvalidated();
        public void onWindowContentChanged(
                int slot, DisplayItem old, DisplayItem update);
    }

    private final AlbumView.Model mSource;
    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private Listener mListener;
    private int mFocusIndex = -1;

    private final AlbumDisplayItem mData[];
    private final ColorTexture mWaitLoadingTexture;
    private SelectionDrawer mSelectionDrawer;

    private SynchronizedHandler mHandler;
    private JobLimiter mThreadPool;

    private int mActiveRequestCount = 0;
    private boolean mIsActive = false;

    private int mCacheThumbSize;  // 0: Don't cache the thumbnails
    private LruCache<Path, Bitmap> mImageCache = new LruCache<Path, Bitmap>(1000);

    public AlbumSlidingWindow(GalleryActivity activity,
            AlbumView.Model source, int cacheSize,
            int cacheThumbSize) {
        source.setModelListener(this);
        mSource = source;
        mData = new AlbumDisplayItem[cacheSize];
        mSize = source.size();

        mWaitLoadingTexture = new ColorTexture(PLACEHOLDER_COLOR);
        mWaitLoadingTexture.setSize(1, 1);

        mHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LOAD_BITMAP_DONE: {
                        ((AlbumDisplayItem) message.obj).onLoadBitmapDone();
                        break;
                    }
                    case MSG_UPDATE_SLOT: {
                        updateSlotContent(message.arg1);
                        break;
                    }
                }
            }
        };

        mThreadPool = new JobLimiter(activity.getThreadPool(), JOB_LIMIT);
    }

    public void setSelectionDrawer(SelectionDrawer drawer) {
        mSelectionDrawer = drawer;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setFocusIndex(int slotIndex) {
        mFocusIndex = slotIndex;
    }

    public DisplayItem get(int slotIndex) {
        Utils.assertTrue(isActiveSlot(slotIndex),
                "invalid slot: %s outsides (%s, %s)",
                slotIndex, mActiveStart, mActiveEnd);
        return mData[slotIndex % mData.length];
    }

    public int size() {
        return mSize;
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= mActiveStart && slotIndex < mActiveEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;

        if (!mIsActive) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
            mSource.setActiveWindow(contentStart, contentEnd);
            return;
        }

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareSlotContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
    }

    public void setActiveWindow(int start, int end) {
        Utils.assertTrue(start <= end
                && end - start <= mData.length && end <= mSize,
                "%s, %s, %s, %s", start, end, mData.length, mSize);
        DisplayItem data[] = mData;

        mActiveStart = start;
        mActiveEnd = end;

        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2,
                0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);
        if (mIsActive) updateAllImageRequests();
    }

    // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            requestSlotImage(mActiveEnd + i, false);
            requestSlotImage(mActiveStart - 1 - i, false);
        }
    }

    private void requestSlotImage(int slotIndex, boolean isActive) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return;
        AlbumDisplayItem item = mData[slotIndex % mData.length];
        item.requestImage();
    }

    private void cancelNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            cancelSlotImage(mActiveEnd + i, false);
            cancelSlotImage(mActiveStart - 1 - i, false);
        }
    }

    private void cancelSlotImage(int slotIndex, boolean isActive) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return;
        AlbumDisplayItem item = mData[slotIndex % mData.length];
        item.cancelImageRequest();
    }

    private void freeSlotContent(int slotIndex) {
        AlbumDisplayItem data[] = mData;
        int index = slotIndex % data.length;
        AlbumDisplayItem original = data[index];
        if (original != null) {
            original.recycle();
            data[index] = null;
        }
    }

    private void prepareSlotContent(final int slotIndex) {
        mData[slotIndex % mData.length] = new AlbumDisplayItem(
                slotIndex, mSource.get(slotIndex));
    }

    private void updateSlotContent(final int slotIndex) {
        MediaItem item = mSource.get(slotIndex);
        AlbumDisplayItem data[] = mData;
        int index = slotIndex % data.length;
        AlbumDisplayItem original = data[index];
        AlbumDisplayItem update = new AlbumDisplayItem(slotIndex, item);
        data[index] = update;
        boolean isActive = isActiveSlot(slotIndex);
        if (mListener != null && isActive) {
            mListener.onWindowContentChanged(slotIndex, original, update);
        }
        if (original != null) {
            if (isActive && original.isRequestInProgress()) {
                --mActiveRequestCount;
            }
            original.recycle();
        }
        if (isActive) {
            if (mActiveRequestCount == 0) cancelNonactiveImages();
            ++mActiveRequestCount;
            update.requestImage();
        } else {
            if (mActiveRequestCount == 0) update.requestImage();
        }
    }

    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        AlbumDisplayItem data[] = mData;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            AlbumDisplayItem item = data[i % data.length];
            item.requestImage();
            if (item.isRequestInProgress()) ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    private class AlbumDisplayItem extends AbstractDisplayItem
            implements FutureListener<Bitmap>, Job<Bitmap> {
        private Future<Bitmap> mFuture;
        private final int mSlotIndex;
        private final int mMediaType;
        private Texture mContent;
        private boolean mIsPanorama;
        private boolean mWaitLoadingDisplayed;
        private int mSubType;

        public AlbumDisplayItem(int slotIndex, MediaItem item) {
            super(item);
            mMediaType = (item == null)
                    ? MediaItem.MEDIA_TYPE_UNKNOWN
                    : item.getMediaType();
            mSlotIndex = slotIndex;
            mIsPanorama = GalleryUtils.isPanorama(item);
            mSubType |= (mIsPanorama ? MediaObject.SUBTYPE_PANORAMA : 0);
            if (item != null) {
                mSubType |= item.getSubType();
            }
            updateContent(mWaitLoadingTexture);
        }

        @Override
        protected void onBitmapAvailable(Bitmap bitmap) {
            boolean isActiveSlot = isActiveSlot(mSlotIndex);
            if (isActiveSlot) {
                --mActiveRequestCount;
                if (mActiveRequestCount == 0) requestNonactiveImages();
            }
            if (bitmap != null) {
                BitmapTexture texture = new BitmapTexture(bitmap, true);
                texture.setThrottled(true);
                if (mWaitLoadingDisplayed) {
                    updateContent(new FadeInTexture(PLACEHOLDER_COLOR, texture));
                } else {
                    updateContent(texture);
                }
                if (mListener != null && isActiveSlot) {
                    mListener.onContentInvalidated();
                }
            }
        }

        private void updateContent(Texture content) {
            mContent = content;
        }

        @Override
        public int render(GLCanvas canvas, int pass) {
            // Fit the content into the box
            int width = mContent.getWidth();
            int height = mContent.getHeight();

            float scalex = mBoxWidth / (float) width;
            float scaley = mBoxHeight / (float) height;
            float scale = Math.min(scalex, scaley);

            width = (int) Math.floor(width * scale);
            height = (int) Math.floor(height * scale);

            // Now draw it
            if (pass == 0) {
                Path path = null;
                if (mMediaItem != null) path = mMediaItem.getPath();
                mSelectionDrawer.draw(canvas, mContent, width, height,
                        getRotation(), path, mMediaType, /* mIsPanorama */ mSubType);
                if (mContent == mWaitLoadingTexture) {
                       mWaitLoadingDisplayed = true;
                }
                int result = 0;
                if (mFocusIndex == mSlotIndex) {
                    result |= RENDER_MORE_PASS;
                }
                if ((mContent instanceof FadeInTexture) &&
                        ((FadeInTexture) mContent).isAnimating()) {
                    result |= RENDER_MORE_FRAME;
                }
                return result;
            } else if (pass == 1) {
                mSelectionDrawer.drawFocus(canvas, width, height);
            }
            return 0;
        }

        @Override
        public void startLoadBitmap() {
            if (mCacheThumbSize > 0) {
                Path path = mMediaItem.getPath();
                if (mImageCache.containsKey(path)) {
                    Bitmap bitmap = mImageCache.get(path);
                    updateImage(bitmap, false);
                    return;
                }
                mFuture = mThreadPool.submit(this, this);
            } else {
                mFuture = mThreadPool.submit(mMediaItem.requestImage(
                        MediaItem.TYPE_MICROTHUMBNAIL), this);
            }
        }

        // This gets the bitmap and scale it down.
        public Bitmap run(JobContext jc) {
            Job<Bitmap> job = mMediaItem.requestImage(
                    MediaItem.TYPE_MICROTHUMBNAIL);
            Bitmap bitmap = job.run(jc);
            if (bitmap != null) {
                bitmap = BitmapUtils.resizeDownBySideLength(
                        bitmap, mCacheThumbSize, true);
            }
            return bitmap;
        }

        @Override
        public void cancelLoadBitmap() {
            if (mFuture != null) {
                mFuture.cancel();
            }
        }

        @Override
        public void onFutureDone(Future<Bitmap> bitmap) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_BITMAP_DONE, this));
        }

        private void onLoadBitmapDone() {
            Future<Bitmap> future = mFuture;
            mFuture = null;
            Bitmap bitmap = future.get();
            boolean isCancelled = future.isCancelled();
            if (mCacheThumbSize > 0 && (bitmap != null || !isCancelled)) {
                Path path = mMediaItem.getPath();
                mImageCache.put(path, bitmap);
            }
            updateImage(bitmap, isCancelled);
        }

        @Override
        public String toString() {
            return String.format("AlbumDisplayItem[%s]", mSlotIndex);
        }
    }

    public void onSizeChanged(int size) {
        if (mSize != size) {
            mSize = size;
            if (mListener != null) mListener.onSizeChanged(mSize);
        }
    }

    public void onWindowContentChanged(int index) {
        if (index >= mContentStart && index < mContentEnd && mIsActive) {
            updateSlotContent(index);
        }
    }

    public void resume() {
        mIsActive = true;
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    public void pause() {
        mIsActive = false;
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeSlotContent(i);
        }
        mImageCache.clear();
    }
}
