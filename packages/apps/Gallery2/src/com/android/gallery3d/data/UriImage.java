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

package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.mediatek.gifDecoder.GifDecoder;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.DrmHelper;
import com.android.gallery3d.util.MpoHelper;
import com.android.gallery3d.util.StereoHelper;

import com.mediatek.mpo.MpoDecoder;

public class UriImage extends MediaItem {
    private static final String TAG = "UriImage";

    private static final int STATE_INIT = 0;
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_DOWNLOADED = 2;
    private static final int STATE_ERROR = -1;

    //added to support Mediatek features
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();
    private static final boolean mIsDisplay2dAs3dSupported = 
            MediatekFeature.isDisplay2dAs3dSupported();

    private int mMpoSubType = -1;

    private final Uri mUri;
    private final String mContentType;

    private DownloadCache.Entry mCacheEntry;
    private ParcelFileDescriptor mFileDescriptor;
    private int mState = STATE_INIT;
    private int mWidth;
    private int mHeight;
    private int mRotation;

    private GalleryApp mApplication;

    public UriImage(GalleryApp application, Path path, Uri uri) {
        super(path, nextVersionNumber());
        mUri = uri;
        mApplication = Utils.checkNotNull(application);
        mContentType = getMimeType(uri);
    }

    private String getMimeType(Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String extension =
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString());

            //added for mediatek feature.
            if (mIsMpoSupported || mIsStereoDisplaySupported) {
                String mtkAddedType = MediatekFeature.getAddedMimetype(extension);
                if (null != mtkAddedType) return mtkAddedType;
            }

            String type = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase());
            if (type != null) return type;
        }
        return mApplication.getContentResolver().getType(uri);
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        //return new BitmapJob(type);
        boolean postProc = false;
        //for picture quality enhancement
        if (MediatekFeature.isPictureQualityEnhanceSupported()) {
            postProc = true;
        }
        return new BitmapJob(type, postProc);
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new RegionDecoderJob();
    }

    @Override
    public Job<GifDecoder> requestGifDecoder() {
        if (!MediatekFeature.isGifAnimationSupported()) {
            Log.e("LocalImage","requestGifDecoder() call when feature off");
            return null;
        }
        return new GifDecoderRequest();
    }

    @Override
    public Job<Bitmap> requestImageWithPostProc(boolean with, int type) {
        return new BitmapJob(type, with);
    }

    private void openFileOrDownloadTempFile(JobContext jc) {
        int state = openOrDownloadInner(jc);
        synchronized (this) {
            mState = state;
            if (mState != STATE_DOWNLOADED) {
                if (mFileDescriptor != null) {
                    Utils.closeSilently(mFileDescriptor);
                    mFileDescriptor = null;
                }
            }
            notifyAll();
        }
    }

    private int openOrDownloadInner(JobContext jc) {
        String scheme = mUri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
                    InputStream is = mApplication.getContentResolver()
                            .openInputStream(mUri);
                    mRotation = Exif.getOrientation(is);
                    Utils.closeSilently(is);
                }
                mFileDescriptor = mApplication.getContentResolver()
                        .openFileDescriptor(mUri, "r");
                if (jc.isCancelled()) return STATE_INIT;
                return STATE_DOWNLOADED;
            } catch (FileNotFoundException e) {
                Log.w(TAG, "fail to open: " + mUri, e);
                return STATE_ERROR;
            }
        } else {
            try {
                URL url = new URI(mUri.toString()).toURL();
                mCacheEntry = mApplication.getDownloadCache().download(jc, url);
                if (jc.isCancelled()) return STATE_INIT;
                if (mCacheEntry == null) {
                    Log.w(TAG, "download failed " + url);
                    return STATE_ERROR;
                }
                if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
                    InputStream is = new FileInputStream(mCacheEntry.cacheFile);
                    mRotation = Exif.getOrientation(is);
                    Utils.closeSilently(is);
                }
                mFileDescriptor = ParcelFileDescriptor.open(
                        mCacheEntry.cacheFile, ParcelFileDescriptor.MODE_READ_ONLY);
                return STATE_DOWNLOADED;
            } catch (Throwable t) {
                Log.w(TAG, "download error", t);
                return STATE_ERROR;
            }
        }
    }

    private boolean prepareInputFile(JobContext jc) {
        jc.setCancelListener(new CancelListener() {
            public void onCancel() {
                synchronized (this) {
                    notifyAll();
                }
            }
        });

        while (true) {
            synchronized (this) {
                if (jc.isCancelled()) return false;
                if (mState == STATE_INIT) {
                    mState = STATE_DOWNLOADING;
                    // Then leave the synchronized block and continue.
                } else if (mState == STATE_ERROR) {
                    return false;
                } else if (mState == STATE_DOWNLOADED) {
                    return true;
                } else /* if (mState == STATE_DOWNLOADING) */ {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // ignored.
                    }
                    continue;
                }
            }
            // This is only reached for STATE_INIT->STATE_DOWNLOADING
            openFileOrDownloadTempFile(jc);
        }
    }

    private class RegionDecoderJob implements Job<BitmapRegionDecoder> {
        public BitmapRegionDecoder run(JobContext jc) {
            if (!prepareInputFile(jc)) return null;
            BitmapRegionDecoder decoder = DecodeUtils.requestCreateBitmapRegionDecoder(
                    jc, mFileDescriptor.getFileDescriptor(), false);
            mWidth = decoder.getWidth();
            mHeight = decoder.getHeight();
            return decoder;
        }
    }

    private class BitmapJob implements Job<Bitmap> {
        private int mType;
        private boolean mPostProc;

        protected BitmapJob(int type, boolean postProc) {
            mType = type;
            mPostProc = postProc;
        }

        public Bitmap run(JobContext jc) {
            //reset mState each time we want to prepare
            //Is is a very bad solution for image not updated
            //issue ?
            mState = STATE_INIT;

            if (!prepareInputFile(jc)) return null;
            int targetSize = LocalImage.getTargetSize(mType);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = mPostProc;
            }

            //check if we need to extract Origin image dimensions
            extractImageInfo(jc);

            Bitmap bitmap = null;

            if (mIsStereoDisplaySupported && 
                StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mContentType)) {
                //decode first frame of jps file
                ContentResolver resolver = mApplication.getContentResolver();
                bitmap = StereoHelper.getStereoImage(jc, resolver, mUri,
                                        mContentType, true, null, targetSize);
            } else if (mIsStereoDisplaySupported && mIsMpoSupported && 
                MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mContentType)) {
                //decode first frame of mpo file
                ContentResolver resolver = mApplication.getContentResolver();
                bitmap = StereoHelper.getStereoImage(jc, resolver, mUri,
                                        mContentType, true, null, targetSize);
            } else if (mIsDrmSupported && null != mUri && null != mUri.getPath() && 
                ContentResolver.SCHEME_FILE.equals(mUri.getScheme()) &&
                mUri.getPath().toLowerCase().endsWith(".dcf")) {
                //when drm file, decode it.
                //Note: currently, only DRM files on sdcard can be decoded
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mUri.getPath());
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, null, false);
            } else {
                bitmap = DecodeUtils.requestDecode(jc,
                        mFileDescriptor.getFileDescriptor(), options, targetSize);
            }
            if (jc.isCancelled() || bitmap == null) {
                return null;
            }

            if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                bitmap = BitmapUtils.resizeDownAndCropCenter(bitmap,
                        targetSize, true);
            } else {
                bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
                        targetSize, true);
            }

            if (MediatekFeature.isGifAnimationSupported() &&
                0 != (getSupportedOperations() & SUPPORT_GIF_ANIMATION)) {
                //if needed, replace gif background
                bitmap = BitmapUtils.replaceBitmapBgColor(bitmap,
                            MediatekFeature.getGifBackGroundColor(),true);
            }

            //return bitmap;
            return DecodeUtils.ensureGLCompatibleBitmap(bitmap);
        }
    }

    private InputStream openUriInputStream(Uri uri) {
        if (null == uri) return null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) || 
            ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || 
            ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                return mApplication.getContentResolver()
                            .openInputStream(uri);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "openUriInputStream:fail to open: " + uri, e);
                return null;
            }
        }
        Log.w(TAG,"openUriInputStream:encountered unknow scheme!");
        return null;
    }

    private void extractImageInfo(JobContext jc) {
        //temporarily, we only decode width & height for CMCC feature
        if (!MediatekFeature.isCMCC()) return;

        Options options = new Options();
        options.inJustDecodeBounds = true;

        if (mIsDrmSupported && null != mUri && null != mUri.getPath() && 
            ContentResolver.SCHEME_FILE.equals(mUri.getScheme()) &&
            mUri.getPath().toLowerCase().endsWith(".dcf")) {
            //when drm file, decode it.
            //Note: currently, only DRM files on sdcard can be decoded
            ContentResolver resolver = mApplication.getContentResolver();
            Uri drmUri = Uri.parse("file:///" + mUri.getPath());
            DrmHelper.forceDecodeDrmUri(resolver, drmUri, null, false);
        } else {
            DecodeUtils.requestDecode(jc,
                    mFileDescriptor.getFileDescriptor(), options);
        }
        if (0 != options.outWidth && 0 != options.outHeight) {
            mWidth = options.outWidth;
            mHeight = options.outHeight;
        }
        //for stereo feature, adjust dimension
        if (mIsStereoDisplaySupported && 
            StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            //we assume left and right stereo layout here.
            Log.w(TAG, "extractImageInfo:for JPS, assume left/right layout");
            mWidth = mWidth / 2;
        }
    }

    private class GifDecoderRequest implements Job<GifDecoder> {
        public GifDecoder run(JobContext jc) {
            //I don't know why, but if I open an InputStream from
            //FileDescriptor, the stream may be illegal, causing
            //invalid GifDecoder!
            //So I open InputStream directly from Uri in the first
            //priority. If failed, I'll open an InputStream from 
            //FileDescriptor, and GIF animation may failed to play
            if (null != mUri) {
                GifDecoder gifDecoder = null;
                try {
                    InputStream is = openUriInputStream(mUri);
                    if (null != is) {
                        gifDecoder = DecodeUtils.requestGifDecoder(jc,is);
                        is.close();
                        return gifDecoder;
                    }
                    Log.w(TAG,"GifDecoderRequest:run:get InputStream from Uri failed!");
                } catch (java.io.IOException ex) {
                    Log.i(TAG,"GifDecoderRequest:run:got IOException!");
                    return null;
                }

            }
            Log.w(TAG,"GifDecoderRequest:run:get GifDecoder from FileDescriptor");
            if (!prepareInputFile(jc)) return null;
            return DecodeUtils.requestGifDecoder(jc, 
                                   mFileDescriptor.getFileDescriptor());
        }
    }

    @Override
    public Job<MediatekFeature.DataBundle> requestImage(int type,
                                                  MediatekFeature.Params params) {
        return new UriImageRequest(type, 
                     MediatekFeature.isPictureQualityEnhanceSupported(), params);
    }

    public class UriImageRequest implements Job<MediatekFeature.DataBundle> {
        private int mType;
        private boolean mPostProc;
        private MediatekFeature.Params mParams;

        UriImageRequest(int type, boolean postProc, MediatekFeature.Params params) {
            mType = type;
            mPostProc = postProc;
            mParams = params;
        }

        public MediatekFeature.DataBundle run(JobContext jc) {

            //if (!mIsStereoDisplaySupported) {
            //    Log.e(TAG,"UriImageRequest:Stereo is not supported!");
            //    return null;
            //}

            if (null == mUri) {
                Log.w(TAG,"UriImageRequest:got null mUri");
                return null;
            }

            if (null == mParams) {
                mParams = new MediatekFeature.Params();
                mParams.inOriginalFrame = false;
                mParams.inFirstFrame = false;
                mParams.inSecondFrame = true;
            }

            int targetSize = LocalImage.getTargetSize(mType);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = mPostProc;
            }
            Bitmap bitmap = null;
            MediatekFeature.DataBundle dataBundle = null;

            if (mIsStereoDisplaySupported && 
                StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mContentType)) {
                //decode first frame of jps file
                ContentResolver resolver = mApplication.getContentResolver();
                bitmap = StereoHelper.getStereoImage(jc, resolver, mUri,
                                        mContentType, false, null, targetSize);
            } else if (mIsStereoDisplaySupported && mIsMpoSupported && 
                MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mContentType)) {
                //decode first frame of mpo file
                ContentResolver resolver = mApplication.getContentResolver();
                bitmap = StereoHelper.getStereoImage(jc, resolver, mUri,
                                        mContentType, false, null, targetSize);
            } else if (mIsDrmSupported && null != mUri && null != mUri.getPath() && 
                ContentResolver.SCHEME_FILE.equals(mUri.getScheme()) &&
                mUri.getPath().toLowerCase().endsWith(".dcf")) {
                //when drm file, decode it.
                //Note: currently, only DRM files on sdcard can be decoded
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mUri.getPath());
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, null, false);
                //generate the second image
                dataBundle = StereoHelper.generateSecondImage(jc, bitmap, mParams,
                                                                true);
            } else {
                bitmap = DecodeUtils.requestDecode(jc,
                        mFileDescriptor.getFileDescriptor(), options, targetSize);
                //generate the second image
                dataBundle = StereoHelper.generateSecondImage(jc, bitmap, mParams,
                                                                true);
            }

            if (null == dataBundle) {
                bitmap = postScaleDown(bitmap, mType, targetSize);
                dataBundle = new MediatekFeature.DataBundle();
                dataBundle.secondFrame = bitmap;
            } else {
                if (null != dataBundle.firstFrame) {
                    dataBundle.firstFrame = postScaleDown(
                                         dataBundle.firstFrame, mType, targetSize);
                }
                if (null != dataBundle.secondFrame) {
                    dataBundle.secondFrame = postScaleDown(
                                         dataBundle.secondFrame, mType, targetSize);
                }
            }

            return dataBundle;
        }
    }

    private Bitmap postScaleDown(Bitmap bitmap, int type, int targetSize) {
        if (null == bitmap) return null;
        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            bitmap = BitmapUtils.resizeDownAndCropCenter(bitmap,
                    targetSize, true);
        } else {
            bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
                    targetSize, true);
        }
        //ensue OpenGL compatible;
        bitmap = DecodeUtils.ensureGLCompatibleBitmap(bitmap);
        return bitmap;
    }

    //added for Stereo Display
    public int getStereoLayout() {
        if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            return StereoHelper.STEREO_LAYOUT_FULL_FRAME;
        } else if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            //now we ignore the possibility that the image is top and bottom layout
            return StereoHelper.STEREO_LAYOUT_LEFT_AND_RIGHT;
        } else {
            return StereoHelper.STEREO_LAYOUT_NONE;
        }
    }

    @Override
    public int getSupportedOperations() {
        int supported = SUPPORT_EDIT | SUPPORT_SETAS;
        if (isSharable()) supported |= SUPPORT_SHARE;
        if (BitmapUtils.isSupportedByRegionDecoder(mContentType)) {
            supported |= SUPPORT_FULL_IMAGE;
        }
        //added for GIF animation
        if (MediatekFeature.isGifAnimationSupported() &&
            BitmapUtils.isSupportedByGifDecoder(mContentType)) {
            supported |= SUPPORT_GIF_ANIMATION;
        }
        //added for stereo display
    	// JPS stereo image has to be treated specially
        if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            if (mIsStereoDisplaySupported) {
                supported |= SUPPORT_STEREO_DISPLAY;
            }
        }
        // Whether a mpo file can be stereoly display depends on its
        // subtype. This will introduce IO operation and decoding, which
        // is very risky for ANR
        if (mIsMpoSupported &&
            MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            initMpoSubType();
            if (mIsStereoDisplaySupported &&
                (MpoDecoder.MTK_TYPE_Stereo == mMpoSubType ||
                 MpoDecoder.MTK_TYPE_3DPan == mMpoSubType)) {
                supported |= SUPPORT_STEREO_DISPLAY;
            } else {
                supported |= SUPPORT_PLAY;
            }
        }

        //for normal image, support stereo display if possible
        //GIF animation is not supposed to be displayed as stereo
        if (mIsDisplay2dAs3dSupported &&
            0 == (supported & SUPPORT_PLAY) &&
            0 == (supported & SUPPORT_STEREO_DISPLAY) &&
            0 == (supported & SUPPORT_GIF_ANIMATION)) {
            supported |= SUPPORT_STEREO_DISPLAY;
            supported |= SUPPORT_CONVERT_TO_3D;
        }

        return supported;
    }

    private boolean isSharable() {
        // We cannot grant read permission to the receiver since we put
        // the data URI in EXTRA_STREAM instead of the data part of an intent
        // And there are issues in MediaUploader and Bluetooth file sender to
        // share a general image data. So, we only share for local file.
        return ContentResolver.SCHEME_FILE.equals(mUri.getScheme());
    }

    @Override
    public int getMediaType() {
        return MEDIA_TYPE_IMAGE;
    }

    @Override
    public Uri getContentUri() {
        return mUri;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (mWidth != 0 && mHeight != 0) {
            details.addDetail(MediaDetails.INDEX_WIDTH, mWidth);
            details.addDetail(MediaDetails.INDEX_HEIGHT, mHeight);
        }
        if (mContentType != null) {
            details.addDetail(MediaDetails.INDEX_MIMETYPE, mContentType);
        }
        if (ContentResolver.SCHEME_FILE.equals(mUri.getScheme())) {
            String filePath = mUri.getPath();
            details.addDetail(MediaDetails.INDEX_PATH, filePath);
            MediaDetails.extractExifInfo(details, filePath);
        }
        return details;
    }

    @Override
    public String getMimeType() {
        return mContentType;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mFileDescriptor != null) {
                Utils.closeSilently(mFileDescriptor);
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public int getWidth() {
        return mWidth;//0;
    }

    @Override
    public int getHeight() {
        return mHeight;//0;
    }

    @Override
    public int getRotation() {
        return mRotation;
    }

    public int getSubType() {
        int subType = 0;
        if (mIsMpoSupported && 
            MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mContentType)) {
            initMpoSubType();
            if (MpoDecoder.MTK_TYPE_MAV == mMpoSubType) {
                subType |= MediaObject.SUBTYPE_MPO_MAV;
            } //else if
        }
        return subType;
    }

    private void initMpoSubType() {
        if (-1 == mMpoSubType) {
            ContentResolver resolver = mApplication.getContentResolver();
            MpoDecoder mpoDecoder = MpoDecoder.decodeUri(resolver, mUri);
            if (null != mpoDecoder) {
                mMpoSubType = mpoDecoder.suggestMtkMpoType();
                Log.d(TAG, "initMpoSubType:mMpoSubType="+mMpoSubType);
                mpoDecoder.close();
            }
        }
    }
}
