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
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.Path;
import  com.android.pqtuningtool.ui.PhotoView;
import  com.android.pqtuningtool.ui.PhotoView.ImageData;
import  com.android.pqtuningtool.ui.SynchronizedHandler;
import  com.android.pqtuningtool.ui.TileImageViewAdapter;
import  com.android.pqtuningtool.util.Future;
import  com.android.pqtuningtool.util.FutureListener;
import  com.android.pqtuningtool.util.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import  com.android.pqtuningtool.util.MediatekFeature;
import com.mediatek.gifDecoder.GifDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;

public class SinglePhotoDataAdapter extends TileImageViewAdapter
        implements PhotoPage.Model {

    private static final String TAG = "SinglePhotoDataAdapter";
    private static final int SIZE_BACKUP = 1024;
    private static final int MSG_UPDATE_IMAGE = 1;
    //added for GifAnimation
    private static final int MSG_RUN_OBJECT = 2;

    private static final boolean mIsGifAnimationSupported = 
                            MediatekFeature.isGifAnimationSupported();
    private boolean mAnimateGif;
    private Future<?> mGifTask;
    private GifDecoder mGifDecoder;
    private Bitmap mCurrentGifFrame;
    private int mCurrentFrameNum;
    private int mTotalFrameCount;
    private boolean mIsActive = false;
    //added for GifAnimation end

    private MediaItem mItem;
    private boolean mHasFullImage;
    private Future<?> mTask;
    private Handler mHandler;

    private PhotoView mPhotoView;
    private ThreadPool mThreadPool;

    public SinglePhotoDataAdapter(
            GalleryActivity activity, PhotoView view, MediaItem item) {
        mItem = Utils.checkNotNull(item);
        mHasFullImage = (item.getSupportedOperations() &
                MediaItem.SUPPORT_FULL_IMAGE) != 0;
        //added for gif animation
        if (mIsGifAnimationSupported && 
            (item.getSupportedOperations() & 
             MediaItem.SUPPORT_GIF_ANIMATION) != 0) {
            mAnimateGif = true;
        } else {
            mAnimateGif = false;
        }
        mPhotoView = Utils.checkNotNull(view);
        mHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @Override
            @SuppressWarnings("unchecked")
            public void handleMessage(Message message) {
                Utils.assertTrue(message.what == MSG_UPDATE_IMAGE ||
                                 message.what == MSG_RUN_OBJECT);
                switch (message.what) {
                    case MSG_UPDATE_IMAGE:
                        if (mHasFullImage) {
                            onDecodeLargeComplete((ImageBundle) message.obj);
                        } else {
                            onDecodeThumbComplete((Future<Bitmap>) message.obj);
                        }
                        return;
                    case MSG_RUN_OBJECT: {
                        ((Runnable) message.obj).run();
                        return;
                    }
                    default: throw new AssertionError();
                }
            }
        };
        mThreadPool = activity.getThreadPool();
    }

    private static class ImageBundle {
        public final BitmapRegionDecoder decoder;
        public final Bitmap backupImage;

        public ImageBundle(BitmapRegionDecoder decoder, Bitmap backupImage) {
            this.decoder = decoder;
            this.backupImage = backupImage;
        }
    }

    private FutureListener<BitmapRegionDecoder> mLargeListener =
            new FutureListener<BitmapRegionDecoder>() {
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            BitmapRegionDecoder decoder = future.get();
            if (decoder == null) return;
            int width = decoder.getWidth();
            int height = decoder.getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = BitmapUtils.computeSampleSize(
                    (float) SIZE_BACKUP / Math.max(width, height));
            Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
            mHandler.sendMessage(mHandler.obtainMessage(
                    MSG_UPDATE_IMAGE, new ImageBundle(decoder, bitmap)));
        }
    };

    private FutureListener<Bitmap> mThumbListener =
            new FutureListener<Bitmap>() {
        public void onFutureDone(Future<Bitmap> future) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_UPDATE_IMAGE, future));
        }
    };

    public boolean isEmpty() {
        return false;
    }

    public int getImageRotation() {
        return mItem.getRotation();
    }

    private void onDecodeLargeComplete(ImageBundle bundle) {
        try {
            setBackupImage(bundle.backupImage,
                    bundle.decoder.getWidth(), bundle.decoder.getHeight());
            setRegionDecoder(bundle.decoder);
            mPhotoView.notifyImageInvalidated(0);
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode large", t);
        }
    }

    private void onDecodeThumbComplete(Future<Bitmap> future) {
        try {
            Bitmap backup = future.get();
            if (backup == null) return;
            setBackupImage(backup, backup.getWidth(), backup.getHeight());
            mPhotoView.notifyOnNewImage();
            mPhotoView.notifyImageInvalidated(0); // the current image
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode thumb", t);
        }
    }

    public void resume() {
        mIsActive = true;

        if (mTask == null) {
            if (mHasFullImage) {
                mTask = mThreadPool.submit(
                        mItem.requestLargeImage(), mLargeListener);
            } else {
                mTask = mThreadPool.submit(
                        mItem.requestImage(MediaItem.TYPE_THUMBNAIL),
                        mThumbListener);
            }
        }

        //added for gif animation
        if (mIsGifAnimationSupported && null == mGifTask) {
            if (mAnimateGif) {
                mGifTask = mThreadPool.submit(
                        mItem.requestGifDecoder(), new GifDecoderListener());
            }
        }
    }

    public void pause() {
        mIsActive = false;

        Future<?> task = mTask;
        task.cancel();
        task.waitDone();
        if (task.get() == null) {
            mTask = null;
        }

        //cancel GIF task
        task = mGifTask;
        if (null != task) {
            task.cancel();
            task.waitDone();
        }
        mGifTask = null;
    }

    private class GifDecoderListener
            implements Runnable, FutureListener<GifDecoder> {
        private Future<GifDecoder> mFuture;

        public GifDecoderListener() {}

        @Override
        public void onFutureDone(Future<GifDecoder> future) {
            mFuture = future;
            if (mIsGifAnimationSupported && null != mFuture.get()) {
                mHandler.sendMessage(
                        mHandler.obtainMessage(MSG_RUN_OBJECT, this));
            }
        }

        @Override
        public void run() {
            startGifAnimation(mFuture);
        }
    }

    private void startGifAnimation(Future<GifDecoder> future) {
        mGifTask = null;
        mGifDecoder = future.get();
        if (mGifDecoder != null) {
            //prepare Gif animation state
            mCurrentFrameNum = 0;
            mTotalFrameCount = mGifDecoder.getTotalFrameCount();
            if (mTotalFrameCount <= 1) {
                Log.w("SinglePhotoDataAdapter","invalid frame count, NO animation!");
                return;
            }
            //create gif frame bitmap
            mCurrentGifFrame = Bitmap.createBitmap(mGifDecoder.getWidth(), 
                                                   mGifDecoder.getHeight(),
                                                   Bitmap.Config.ARGB_8888);
            Utils.assertTrue(null != mCurrentGifFrame);
            //start GIF animation
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_RUN_OBJECT, 
                                     new GifAnimationRunnable()));
        }
    }

    private class GifAnimationRunnable implements Runnable {
        public GifAnimationRunnable() {
        }

        @Override
        public void run() {
            if (!mIsActive) {
                Log.i("SinglePhotoDataAdapter","GifAnimationRunnable:run:already paused");
                //releaseGifResource();
                return;
            }

            if (null == mGifDecoder) {
                Log.e("SinglePhotoDataAdapter","GifAnimationRunnable:run:invalid GifDecoder");
                //releaseGifResource();
                return;
            }

            long preTime = SystemClock.uptimeMillis();

            //assign decoded bitmap to CurrentGifFrame
            Bitmap curBitmap = mGifDecoder.getFrameBitmap(mCurrentFrameNum);
            if (null == curBitmap) {
                Log.e("SinglePhotoDataAdapter","GifAnimationRunnable:onFutureDone:got null frame!");
                //releaseGifResource();
                return;
            }

            //get curent frame duration
            long curDuration = mGifDecoder.getFrameDuration(mCurrentFrameNum);
            //calculate next frame index
            mCurrentFrameNum = (mCurrentFrameNum + 1) % mTotalFrameCount;

            //update Current Gif Frame
            Canvas canvas = new Canvas(mCurrentGifFrame);
            canvas.drawColor(MediatekFeature.getGifBackGroundColor());
            Matrix m = new Matrix();
            canvas.drawBitmap(curBitmap,m,null);
            
            curBitmap.recycle();

            updateGifFrame(mCurrentGifFrame);

            mHandler.sendMessageAtTime(
                    mHandler.obtainMessage(MSG_RUN_OBJECT, this), (curDuration+preTime));
        }
    }

    private void updateGifFrame(Bitmap gifFrame) {
        if (gifFrame == null) return;
        setBackupImage(gifFrame, gifFrame.getWidth(), gifFrame.getHeight());
        mPhotoView.notifyImageInvalidated(0); // the current image
    }

    private void releaseGifResource() {
        mGifDecoder = null;
        if (null != mCurrentGifFrame && !mCurrentGifFrame.isRecycled()) {
            mCurrentGifFrame.recycle();
            mCurrentGifFrame = null;
        }
    }

    public ImageData getNextImage() {
        return null;
    }

    public ImageData getPreviousImage() {
        return null;
    }

    public void next() {
        throw new UnsupportedOperationException();
    }

    public void previous() {
        throw new UnsupportedOperationException();
    }

    public void jumpTo(int index) {
        throw new UnsupportedOperationException();
    }

    public MediaItem getCurrentMediaItem() {
        return mItem;
    }

    public int getCurrentIndex() {
        return 0;
    }

    public void setCurrentPhoto(Path path, int indexHint) {
        // ignore
    }

    public boolean showDrmMicroThumb() {
        return false;
    }

    public void enterConsumeMode() {
        return;//temporarily do nothing
    }

    public boolean enteredConsumeMode() {
        return false;
    }

}
