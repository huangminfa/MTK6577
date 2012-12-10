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
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.PhotoView.ImageData;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TileImageViewAdapter;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.StereoHelper;
import com.mediatek.gifDecoder.GifDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;

public class SinglePhotoDataAdapter extends TileImageViewAdapter
        implements PhotoPage.Model {

    private static final String TAG = "SinglePhotoDataAdapter";
    private static final int SIZE_BACKUP = 1024;
    private static final int MSG_UPDATE_IMAGE = 1;

    //added for Mediatek feature
    private static final int MSG_RUN_OBJECT = 2;
    private static final int MSG_UPDATE_SECOND_IMAGE = 3;
    private static final int MSG_UPDATE_LARGE_IMAGE = 4;

    private static final boolean mIsGifAnimationSupported = 
                            MediatekFeature.isGifAnimationSupported();
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

    private boolean mAnimateGif;
    private Future<?> mGifTask;
    private GifDecoder mGifDecoder;
    private Bitmap mCurrentGifFrame;
    private int mCurrentFrameNum;
    private int mTotalFrameCount;
    private boolean mIsActive = false;

    // Mediatek patch to improve Display image performance:
    // As there is a chance that the UriImage is indeed a very large JPEG
    // image capture by digital camera, for example, 3500x2333, view this
    // kind image will cost a lot of time. 
    // This process is time consuming is caused by the following two reason:
    // 1, creating a RegionDecoder will cause about 300ms on a 1G Hz single
    //    core device.
    // 2, region decoding by RegionDecoder is via SW, no hardware is used.
    //
    // The solution is firstly decode a small Bitmap by Bitmap factory to
    // make UI displays fast, then follow orginal routin to decode a large
    // bitmap by region decoder.
    // BitmapFactory will returns a bitmap sooner because:
    // 1, there is no need to construct a decoder object.
    // 2, there is a chance to use HW acceleration
    // 
    // This solution will shorten black screen duration, but complext program
    // structure.

    private static final boolean mShowThumbFirst = true;
    private boolean mShowedThumb;

    //Stereo display feature
    //Stereo display requires that first and second frame 
    private boolean mShowStereoImage;
    //added for Mediatek feature end

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
        //added for stereo display
        mShowStereoImage = mIsStereoDisplaySupported &&
                ((mItem.getSupportedOperations() &
                 MediaItem.SUPPORT_STEREO_DISPLAY) != 0);
        Log.i(TAG,TAG+":mShowStereoImage="+mShowStereoImage);
        mPhotoView = Utils.checkNotNull(view);
        mHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @Override
            @SuppressWarnings("unchecked")
            public void handleMessage(Message message) {
                Utils.assertTrue(message.what == MSG_UPDATE_IMAGE ||
                                 message.what == MSG_RUN_OBJECT ||
                                 message.what == MSG_UPDATE_SECOND_IMAGE ||
                                 message.what == MSG_UPDATE_LARGE_IMAGE);
                switch (message.what) {
                    case MSG_UPDATE_IMAGE:
                        onDecodeThumbComplete((Future<Bitmap>) message.obj);
                        return;
                    case MSG_RUN_OBJECT: {
                        ((Runnable) message.obj).run();
                        return;
                    }
                    case MSG_UPDATE_SECOND_IMAGE: {
                        onDecodeSecondThumbComplete(
                             (Future<MediatekFeature.DataBundle>) message.obj);
                        return;
                    }
                    case MSG_UPDATE_LARGE_IMAGE: {
                        onDecodeLargeComplete((ImageBundle) message.obj);
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

            if (mIsStereoDisplaySupported && mShowStereoImage) {
                width = StereoHelper.adjustDim(true, mItem.getStereoLayout(),
                                                   width);
                height = StereoHelper.adjustDim(false, mItem.getStereoLayout(),
                                                    height);
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = BitmapUtils.computeSampleSize(
                    (float) SIZE_BACKUP / Math.max(width, height));
            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = true;
            }

            Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
            mHandler.sendMessage(mHandler.obtainMessage(
                    MSG_UPDATE_LARGE_IMAGE, new ImageBundle(decoder, bitmap)));
        }
    };

    private FutureListener<Bitmap> mThumbListener =
            new FutureListener<Bitmap>() {
        public void onFutureDone(Future<Bitmap> future) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_UPDATE_IMAGE, future));
        }
    };

    private FutureListener<MediatekFeature.DataBundle> mSecondThumbListener =
            new FutureListener<MediatekFeature.DataBundle>() {
        public void onFutureDone(Future<MediatekFeature.DataBundle> future) {
            mHandler.sendMessage(
                    mHandler.obtainMessage(MSG_UPDATE_SECOND_IMAGE, future));
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
            //adjust full image dimesion if needed
            if (mIsStereoDisplaySupported && mShowStereoImage) {
                int fullWidth = bundle.decoder.getWidth();
                int fullHeight = bundle.decoder.getHeight();
                fullWidth = StereoHelper.adjustDim(true, mItem.getStereoLayout(),
                                                   fullWidth);
                fullHeight = StereoHelper.adjustDim(false, mItem.getStereoLayout(),
                                                    fullHeight);
                setStereo(bundle.decoder, bundle.backupImage, fullWidth, fullHeight);
            } else {
                setBackupImage(bundle.backupImage,
                        bundle.decoder.getWidth(), bundle.decoder.getHeight());
                setRegionDecoder(bundle.decoder);
            }
            mPhotoView.notifyImageInvalidated(0);
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode large", t);
        }
    }

    private void onDecodeThumbComplete(Future<Bitmap> future) {
        try {
            Bitmap backup = future.get();
            if (backup == null) return;

            int width = backup.getWidth();
            int height = backup.getHeight();
            if (MediatekFeature.isCMCC() && 
                0 != mItem.getWidth() && 0 != mItem.getHeight()) {
                width = mItem.getWidth();
                height = mItem.getHeight();
            }
            setBackupImage(backup, width, height);

            mPhotoView.notifyOnNewImage();
            mPhotoView.notifyImageInvalidated(0); // the current image

            //decode second image for stereo display
            if (mShowStereoImage) {
                //create mediatek parameters
                MediatekFeature.Params params = new MediatekFeature.Params();
                params.inOriginalFrame = false;
                params.inFirstFrame = true;//we decode the first frame if possible
                params.inSecondFrame = true;
                Log.i(TAG,"onDecodeThumbComplete:start second image task");
                mTask = mThreadPool.submit(
                        mItem.requestImage(MediaItem.TYPE_THUMBNAIL, params),
                        mSecondThumbListener);
                return;
            }

            if (mShowThumbFirst && mHasFullImage) {
                //after showed thumbnail, change status
                mShowedThumb = true;
                mTask = mThreadPool.submit(
                        mItem.requestLargeImage(), mLargeListener);
            }
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode thumb", t);
        }
    }

    private void onDecodeSecondThumbComplete(
                     Future<MediatekFeature.DataBundle> future) {
        try {
            Bitmap second = future.get().secondFrame;
            Log.i(TAG,"onDecodeSecondThumbComplete:second="+second);
            if (second == null) return;

            //as we have already made sure that second screen
            //nail has the same dimension as screen nail, only
            //bitmap is needed to set
            setFirstImage(future.get().firstFrame);
            setSecondImage(second);
            mPhotoView.notifyImageInvalidated(0);

            //the 2d to 3d picture should not enter stereo mode.
            //Enter stereo mode only when stereo photo is encountered
            if ((mItem.getSupportedOperations() & 
                MediaItem.SUPPORT_CONVERT_TO_3D) == 0) {
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

            //mPhotoView.notifyOnNewImage();

            if (mShowThumbFirst && mHasFullImage) {
                //after showed thumbnail, change status
                mShowedThumb = true;
                mTask = mThreadPool.submit(
                        mItem.requestLargeImage(), mLargeListener);
            }
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode thumb", t);
        }
    }

    public void resume() {
        mIsActive = true;

        if (mTask == null) {
            //when resume, first show thumbnail
            mShowedThumb = false;

            if (!mShowThumbFirst && mHasFullImage) {
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
            mCurrentGifFrame = BitmapUtils.resizeDownBySideLength(mCurrentGifFrame, MediatekFeature.getMaxTextureSize(), true);
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
            curBitmap = BitmapUtils.resizeDownBySideLength(curBitmap, MediatekFeature.getMaxTextureSize(), true);

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

    public boolean suggestFullScreen() {
        return !MediatekFeature.isCMCC();
    }

    public void enterConsumeMode() {
        return;//temporarily do nothing
    }

    public void triggerStereoFullImage() {
        return;
    }

    public boolean enteredConsumeMode() {
        return false;
    }

}
