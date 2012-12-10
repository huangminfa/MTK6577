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
import com.android.gallery3d.util.MpoHelper;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.UpdateHelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import com.mediatek.gifDecoder.GifDecoder;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.DrmHelper;
import android.drm.DrmStore;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import com.mediatek.mpo.MpoDecoder;
import com.android.gallery3d.util.StereoHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;

// LocalImage represents an image in the local storage.
public class LocalImage extends LocalMediaItem {
    private static final String TAG = "LocalImage";

    static final Path ITEM_PATH = Path.fromString("/local/image/item");

    static final Path ITEM_PATH_DRM_FL = Path.fromString("/local/image/item", 
                                         MediatekFeature.INCLUDE_FL_DRM_MEDIA);
    static final Path ITEM_PATH_DRM_SD = Path.fromString("/local/image/item", 
                                         MediatekFeature.INCLUDE_SD_DRM_MEDIA);
    static final Path ITEM_PATH_DRM_ALL = Path.fromString("/local/image/item",
                                         MediatekFeature.ALL_DRM_MEDIA);

    private static HashMap<String, Path> mPathMap =
            new LinkedHashMap<String, Path>();

    public static Path getItemPath(int mtkInclusion) {
        if (mIsDrmSupported) {
            switch (mtkInclusion) {
                case DrmHelper.NO_DRM_INCLUSION: 
                    return ITEM_PATH;
                case MediatekFeature.INCLUDE_FL_DRM_MEDIA: 
                    return ITEM_PATH_DRM_FL;
                case MediatekFeature.INCLUDE_SD_DRM_MEDIA:
                    return ITEM_PATH_DRM_SD;
                case MediatekFeature.ALL_DRM_MEDIA:
                    return ITEM_PATH_DRM_ALL;
                default:
                    Path path = mPathMap.get(String.valueOf(mtkInclusion));
                    if (null != path) {
                        return path;
                    }
                    //create new path with respect to mtk inclusion
                    path = Path.fromString("/local/image/item", mtkInclusion);
                    //put it into path map
                    mPathMap.put(String.valueOf(mtkInclusion), path);
                    return path;
            }
        } else {
            return ITEM_PATH;
        }
    }

    //added to support Mediatek features
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();
    private static final boolean mIsPrintSupported = 
                                          MediatekFeature.isBluetoothPrintSupported();
    private static final boolean mIsGifAnimationSupported = 
        MediatekFeature.isGifAnimationSupported(); 
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();
    private static final boolean mIsDisplay2dAs3dSupported = 
            MediatekFeature.isDisplay2dAs3dSupported();

    //added to avoid decode bitmap many times
    private String mSniffedMimetype;
    public int mMpoSubType = -1;
    private boolean mStereoDimAdjusted = false;
    //added for manual convergence
    public int mConvergence = -1;

    // Must preserve order between these indices and the order of the terms in
    // the following PROJECTION array.
    private static final int INDEX_ID = 0;
    private static final int INDEX_CAPTION = 1;
    private static final int INDEX_MIME_TYPE = 2;
    private static final int INDEX_LATITUDE = 3;
    private static final int INDEX_LONGITUDE = 4;
    private static final int INDEX_DATE_TAKEN = 5;
    private static final int INDEX_DATE_ADDED = 6;
    private static final int INDEX_DATE_MODIFIED = 7;
    private static final int INDEX_DATA = 8;
    private static final int INDEX_ORIENTATION = 9;
    private static final int INDEX_BUCKET_ID = 10;
    private static final int INDEX_SIZE_ID = 11;
    private static final int INDEX_WIDTH = 12;
    private static final int INDEX_HEIGHT = 13;
    //added to support DRM
    private static final int INDEX_IS_DRM = 14;
    private static final int INDEX_DRM_METHOD = 15;
    //added to support Stereo display
    private static final int INDEX_MPO_SUB_TYPE = 16;
    //added to support manual convergence
    private static final int INDEX_CONVERGENCE = 17;

    static final String[] PROJECTION =  {
            ImageColumns._ID,           // 0
            ImageColumns.TITLE,         // 1
            ImageColumns.MIME_TYPE,     // 2
            ImageColumns.LATITUDE,      // 3
            ImageColumns.LONGITUDE,     // 4
            ImageColumns.DATE_TAKEN,    // 5
            ImageColumns.DATE_ADDED,    // 6
            ImageColumns.DATE_MODIFIED, // 7
            ImageColumns.DATA,          // 8
            ImageColumns.ORIENTATION,   // 9
            ImageColumns.BUCKET_ID,     // 10
            ImageColumns.SIZE,          // 11
            // These should be changed to proper names after they are made public.
            "width", // ImageColumns.WIDTH,         // 12
            "height", // ImageColumns.HEIGHT         // 13
            //added to support DRM
            ImageColumns.IS_DRM,
            ImageColumns.DRM_METHOD,
            //added to support Stereo display
            Images.Media.MPO_TYPE,
            Images.Media.CONVERGENCE,
    };

    private final GalleryApp mApplication;

    public int rotation;
    public int width;
    public int height;

    public LocalImage(Path path, GalleryApp application, Cursor cursor) {
        super(path, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor);
    }

    public LocalImage(Path path, GalleryApp application, int id) {
        super(path, nextVersionNumber());
        mApplication = application;
        ContentResolver resolver = mApplication.getContentResolver();
        Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = LocalAlbum.getItemCursor(resolver, uri, PROJECTION, id);
        if (cursor == null) {
            throw new RuntimeException("cannot get cursor for: " + path);
        }
        try {
            if (cursor.moveToNext()) {
                loadFromCursor(cursor);
            } else {
                throw new RuntimeException("cannot find data for: " + path);
            }
        } finally {
            cursor.close();
        }
    }

    private void loadFromCursor(Cursor cursor) {
        id = cursor.getInt(INDEX_ID);
        caption = cursor.getString(INDEX_CAPTION);
        mimeType = cursor.getString(INDEX_MIME_TYPE);
        latitude = cursor.getDouble(INDEX_LATITUDE);
        longitude = cursor.getDouble(INDEX_LONGITUDE);
        dateTakenInMs = cursor.getLong(INDEX_DATE_TAKEN);
        //added to avoid fake data changed judgement in
        //updateFromCursor function-begin
        dateAddedInSec = cursor.getLong(INDEX_DATE_ADDED);
        dateModifiedInSec = cursor.getLong(INDEX_DATE_MODIFIED);
        //end
        filePath = cursor.getString(INDEX_DATA);
        rotation = cursor.getInt(INDEX_ORIENTATION);
        bucketId = cursor.getInt(INDEX_BUCKET_ID);
        fileSize = cursor.getLong(INDEX_SIZE_ID);
        width = cursor.getInt(INDEX_WIDTH);
        height = cursor.getInt(INDEX_HEIGHT);
        //added to support drm feature
        if (mIsDrmSupported) {
            is_drm = cursor.getInt(INDEX_IS_DRM);
            drm_method = cursor.getInt(INDEX_DRM_METHOD);
        }
        //added to support stereo display feature
        mMpoSubType = cursor.getInt(INDEX_MPO_SUB_TYPE);
        if (mIsStereoDisplaySupported) {
            mConvergence = cursor.getInt(INDEX_CONVERGENCE);
            mStereoDimAdjusted = false;
        }
    }

    @Override
    protected boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        id = uh.update(id, cursor.getInt(INDEX_ID));
        caption = uh.update(caption, cursor.getString(INDEX_CAPTION));
        mimeType = uh.update(mimeType, cursor.getString(INDEX_MIME_TYPE));
        latitude = uh.update(latitude, cursor.getDouble(INDEX_LATITUDE));
        longitude = uh.update(longitude, cursor.getDouble(INDEX_LONGITUDE));
        dateTakenInMs = uh.update(
                dateTakenInMs, cursor.getLong(INDEX_DATE_TAKEN));
        dateAddedInSec = uh.update(
                dateAddedInSec, cursor.getLong(INDEX_DATE_ADDED));
        dateModifiedInSec = uh.update(
                dateModifiedInSec, cursor.getLong(INDEX_DATE_MODIFIED));
        filePath = uh.update(filePath, cursor.getString(INDEX_DATA));
        rotation = uh.update(rotation, cursor.getInt(INDEX_ORIENTATION));
        bucketId = uh.update(bucketId, cursor.getInt(INDEX_BUCKET_ID));
        fileSize = uh.update(fileSize, cursor.getLong(INDEX_SIZE_ID));

        if (!mIsStereoDisplaySupported ||
            !StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            width = uh.update(width, cursor.getInt(INDEX_WIDTH));
            height = uh.update(height, cursor.getInt(INDEX_HEIGHT));
        } else {
            //for jps file, we do not need to update, cause we ourselves will change
            //it privately
            width = cursor.getInt(INDEX_WIDTH);
            height = cursor.getInt(INDEX_HEIGHT);
        }

        //added to support drm feature
        if (mIsDrmSupported) {
            is_drm = uh.update(is_drm,cursor.getInt(INDEX_IS_DRM));
            drm_method = uh.update(drm_method,cursor.getInt(INDEX_DRM_METHOD));
        }
        //added to support stereo display feature
        if (mIsStereoDisplaySupported) {
            mMpoSubType = cursor.getInt(INDEX_MPO_SUB_TYPE);
            mConvergence = cursor.getInt(INDEX_CONVERGENCE);
            mStereoDimAdjusted = false;
        }
        return uh.isUpdated();
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new LocalImageRequest(mApplication, mPath, type, filePath, dateModifiedInSec);
    }

    public static class LocalImageRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        LocalImageRequest(GalleryApp application, Path path, int type,
                String localFilePath, long dateModifiedInSec) {
            super(application, path, type, getTargetSize(type), dateModifiedInSec);
            mLocalFilePath = localFilePath;
        }

        @Override
        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            if (null == mLocalFilePath) {
                Log.w(TAG,"onDecodeOriginal:got null mLocalFilePath");
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = true;
            }

            // try to decode from JPEG EXIF
            if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
                ExifInterface exif = null;
                byte [] thumbData = null;
                try {
                    exif = new ExifInterface(mLocalFilePath);
                    if (exif != null) {
                        thumbData = exif.getThumbnail();
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "fail to get exif thumb", t);
                }
                if (thumbData != null) {
                    Bitmap bitmap = DecodeUtils.requestDecodeIfBigEnough(
                            jc, thumbData, options, getTargetSize(type));
                    if (bitmap != null) return bitmap;
                }
            }

            Bitmap bitmap = null;
            if (mIsStereoDisplaySupported && 
                mLocalFilePath.toLowerCase().endsWith(".jps")) {
                //decode first frame of jps file
                bitmap = StereoHelper.getStereoImage(jc, mLocalFilePath, 
                             null, true, options, getTargetSize(type));
            } else if (mIsMpoSupported && 
                mLocalFilePath.toLowerCase().endsWith(".mpo")) {
                //decode first frame of mpo file
                bitmap = StereoHelper.getStereoImage(jc, mLocalFilePath, 
                             null, true, options, getTargetSize(type));
            } else if (mIsDrmSupported && mLocalFilePath.toLowerCase().endsWith(".dcf")) {
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mLocalFilePath);
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, options, false);
            } else {
                bitmap = DecodeUtils.requestDecode(
                       jc, mLocalFilePath, options, getTargetSize(type));
            }

            if (MediatekFeature.isGifAnimationSupported() &&
                mLocalFilePath.toLowerCase().endsWith(".gif")) {
                //if needed, replace gif background
                bitmap = BitmapUtils.replaceBitmapBgColor(bitmap,
                          MediatekFeature.getGifBackGroundColor(),true);
            }

            return DecodeUtils.ensureGLCompatibleBitmap(bitmap);
        }
    }

    static int getTargetSize(int type) {
        switch (type) {
            case TYPE_THUMBNAIL:
                return THUMBNAIL_TARGET_SIZE;
            case TYPE_MICROTHUMBNAIL:
                return MICROTHUMBNAIL_TARGET_SIZE;
            default:
                throw new RuntimeException(
                    "should only request thumb/microthumb from cache");
        }
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new LocalLargeImageRequest(filePath);
    }

    public static class LocalLargeImageRequest
            implements Job<BitmapRegionDecoder> {
        String mLocalFilePath;

        public LocalLargeImageRequest(String localFilePath) {
            mLocalFilePath = localFilePath;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            return DecodeUtils.requestCreateBitmapRegionDecoder(
                    jc, mLocalFilePath, false);
        }
    }

    @Override
    public Job<GifDecoder> requestGifDecoder() {
        if (!MediatekFeature.isGifAnimationSupported()) {
            Log.e("LocalImage","requestGifDecoder() call when feature off");
            return null;
        }
        return new LocalGifDecoderRequest(filePath);
    }

    public static class LocalGifDecoderRequest
            implements Job<GifDecoder> {
        String mLocalFilePath;

        public LocalGifDecoderRequest(String localFilePath) {
            mLocalFilePath = localFilePath;
        }

        public GifDecoder run(JobContext jc) {
            return DecodeUtils.requestGifDecoder(jc, mLocalFilePath);
        }
    }

    @Override
    public Job<Bitmap> requestImageWithPostProc(boolean with, int type) {
        return new BitmapJob(type,filePath, with);
    }

    private class BitmapJob implements Job<Bitmap> {
        private String mLocalFilePath;
        private int mType;
        private boolean mPostProc;

        protected BitmapJob(int type,String localFilePath, boolean postProc) {
            mType = type;
            mLocalFilePath = localFilePath;
            mPostProc = postProc;
        }

        public Bitmap run(JobContext jc) {
            if (null == mLocalFilePath) {
                Log.w(TAG,"requestImageWithoutPostProc:run:got null mLocalFilePath");
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = mPostProc;
            }

            // try to decode from JPEG EXIF
            if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                ExifInterface exif = null;
                byte [] thumbData = null;
                try {
                    exif = new ExifInterface(mLocalFilePath);
                    if (exif != null) {
                        thumbData = exif.getThumbnail();
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "fail to get exif thumb", t);
                }
                if (thumbData != null) {
                    Bitmap bitmap = DecodeUtils.requestDecodeIfBigEnough(
                            jc, thumbData, options, getTargetSize(mType));
                    if (bitmap != null) return bitmap;
                }
            }
            Bitmap bitmap = null;
            if (mIsStereoDisplaySupported && 
                mLocalFilePath.toLowerCase().endsWith(".jps")) {
                //decode first frame of jps file
                bitmap = StereoHelper.getStereoImage(jc, mLocalFilePath, 
                             null, true, options, getTargetSize(mType));
            } else if (mIsMpoSupported && 
                mLocalFilePath.toLowerCase().endsWith(".mpo")) {
                //decode first frame of mpo file
                bitmap = StereoHelper.getStereoImage(jc, mLocalFilePath, 
                             null, true, options, getTargetSize(mType));
            } else if (mIsDrmSupported && mLocalFilePath.toLowerCase().endsWith(".dcf")) {
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mLocalFilePath);
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, options, false);
            } else {
                bitmap = DecodeUtils.requestDecode(
                       jc, mLocalFilePath, options, getTargetSize(mType));
            }

            if (MediatekFeature.isGifAnimationSupported() &&
                mLocalFilePath.toLowerCase().endsWith(".gif")) {
                //if needed, replace gif background
                bitmap = BitmapUtils.replaceBitmapBgColor(bitmap,
                          MediatekFeature.getGifBackGroundColor(),true);
            }

            //the max Open GL texture size is 2048, so we need to make sure
            //the returned bitmap can be bind as texture.
            bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
                       2048, true);

            return DecodeUtils.ensureGLCompatibleBitmap(bitmap);
        }
    }

    @Override
    public Job<MediatekFeature.DataBundle> 
            requestImage(int type, MediatekFeature.Params params) {
        return new LocalImageRequestEx(mApplication, mPath, type, filePath, params);
    }

    public static class LocalImageRequestEx implements 
                                         Job<MediatekFeature.DataBundle> {
        private GalleryApp mApplication;
        private Path mPath;
        private int mType;
        private int mTargetSize;
        private String mLocalFilePath;
        private MediatekFeature.Params mParams;

        LocalImageRequestEx(GalleryApp application, Path path, int type,
                String localFilePath, MediatekFeature.Params params) {
            mApplication = application;
            mPath = path;
            mType = type;
            mTargetSize = getTargetSize(type);
            mLocalFilePath = localFilePath;
            mParams = params;
        }

        public MediatekFeature.DataBundle run(JobContext jc) {

            if (!mIsStereoDisplaySupported) {
                Log.e(TAG,"LocalSecondImageRequest:Stereo is not supported!");
                return null;
            }

            if (null == mLocalFilePath) {
                Log.w(TAG,"LocalSecondImageRequest:got null mLocalFilePath");
                return null;
            }

            if (null == mParams) {
                mParams = new MediatekFeature.Params();
                mParams.inOriginalFrame = false;
                mParams.inFirstFrame = false;
                mParams.inSecondFrame = true;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = true;
            }

            MediatekFeature.DataBundle dataBundle = null;
            Bitmap bitmap = null;

            if (mIsMpoSupported && mLocalFilePath.toLowerCase().endsWith(".mpo")
                || mLocalFilePath.toLowerCase().endsWith(".jps")) {
                dataBundle = StereoHelper.getStereoImage(jc, mLocalFilePath,
                                        null, mParams, options, mTargetSize);
            } else if (mIsDrmSupported && mLocalFilePath.toLowerCase().endsWith(".dcf")) {
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mLocalFilePath);
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, options, false);
                //generate the second image
                dataBundle = StereoHelper.generateSecondImage(jc, bitmap, mParams,
                                                                true);
            } else {
                if (mParams.inSecondFullFrame || mParams.inFirstFullFrame) {
                    dataBundle = StereoHelper.getStereoImage(jc, mLocalFilePath,
                                        null, mParams, options, mTargetSize);
                } else {
                    //create stereo thumb pair
                    bitmap = DecodeUtils.requestDecode(
                           jc, mLocalFilePath, options, mTargetSize);
                    //generate the second image
                    dataBundle = StereoHelper.generateSecondImage(jc, bitmap, mParams,
                                                                    true);
                }
            }

            if (null == dataBundle) {
                if (bitmap.isRecycled()) {
                    Log.w(TAG, "ImageRequestEx:run:failed to get databundle");
                    return null;
                }
                // We need to resize down if the decoder does not support inSampleSize.
                // (For example, GIF images.)
                bitmap = postScaleDown(bitmap, mTargetSize);
                dataBundle = new MediatekFeature.DataBundle();
                dataBundle.secondFrame = bitmap;
            } else {
                if (null != dataBundle.firstFrame) {
                    dataBundle.firstFrame = postScaleDown(
                                         dataBundle.firstFrame, mTargetSize);
                }
                if (null != dataBundle.secondFrame) {
                    dataBundle.secondFrame = postScaleDown(
                                         dataBundle.secondFrame, mTargetSize);
                }
            }
            //dataBundle.showInfo();
            return dataBundle;
        }
    }

    private static Bitmap postScaleDown(Bitmap bitmap, int targetSize) {
        if (null == bitmap) return null;
        bitmap = BitmapUtils.resizeDownIfTooBig(bitmap, targetSize, true);
        //scale down according to type
        bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
        bitmap = DecodeUtils.ensureGLCompatibleBitmap(bitmap);
        return bitmap;
    }

    //added for Stereo Display
    public int getStereoLayout() {
        if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            return StereoHelper.STEREO_LAYOUT_FULL_FRAME;
        } else if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            //now we ignore the possibility that the image is top and bottom layout
            return StereoHelper.STEREO_LAYOUT_LEFT_AND_RIGHT;
        } else {
            return StereoHelper.STEREO_LAYOUT_NONE;
        }
    }

    @Override
    public int getSupportedOperations() {
        //added for DRM feature
        if (MediatekFeature.isDrmSupported() && isDrm()) {
            return getDrmSupportedOperations();
        }

    	// MAV image has the same supported operations as video
        if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            initMpoSubType();
            if (MpoDecoder.MTK_TYPE_MAV == mMpoSubType) {
                return (SUPPORT_DELETE | SUPPORT_SHARE |
                        SUPPORT_PLAY | SUPPORT_INFO );
            } else {
                int jpsOperation =
                       (SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_CROP |
                        SUPPORT_SETAS | SUPPORT_EDIT | SUPPORT_INFO | 
                        SUPPORT_FULL_IMAGE | SUPPORT_STEREO_DISPLAY |
                        SUPPORT_CONV_TUNING);
                if (mIsPrintSupported) {
                    jpsOperation |= SUPPORT_PRINT;
                }
                return jpsOperation;
            }
        }

    	// JPS stereo image has to be treated specially
        if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            if (mIsStereoDisplaySupported) {
                int jpsOperation =
                       (SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_CROP |
                        SUPPORT_SETAS | SUPPORT_EDIT | SUPPORT_INFO |
                        SUPPORT_FULL_IMAGE | SUPPORT_STEREO_DISPLAY |
                        SUPPORT_CONV_TUNING);
                if (mIsPrintSupported) {
                    jpsOperation |= SUPPORT_PRINT;
                }
                return jpsOperation;
            }
        }

        int operation = SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_CROP
                | SUPPORT_SETAS | SUPPORT_EDIT | SUPPORT_INFO;
        if (BitmapUtils.isSupportedByRegionDecoder(mimeType)) {
            operation |= SUPPORT_FULL_IMAGE;
        }

        //added for GIF animation
        if (MediatekFeature.isGifAnimationSupported() &&
            BitmapUtils.isSupportedByGifDecoder(mimeType)) {
            operation |= SUPPORT_GIF_ANIMATION;
        }

        if (BitmapUtils.isRotationSupported(mimeType)) {
            operation |= SUPPORT_ROTATE;
        }

        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            operation |= SUPPORT_SHOW_ON_MAP;
        }

        if (mIsPrintSupported) {
            operation |= SUPPORT_PRINT;
        }

        //for normal image, support stereo display if possible
        //GIF animation is not supposed to be displayed as stereo
        if (mIsDisplay2dAs3dSupported &&
            0 == (operation & SUPPORT_GIF_ANIMATION)) {
            operation |= SUPPORT_STEREO_DISPLAY;
            operation |= SUPPORT_CONVERT_TO_3D;
        }

        return operation;
    }

    public int getDrmSupportedOperations() {
        if (!MediatekFeature.isDrmSupported() || !isDrm()) {
            return ~SUPPORT_ALL;
        }
        int operation = SUPPORT_DELETE | SUPPORT_INFO | SUPPORT_DRM_INFO;

        //temporarily no drm media supports region decoder, gif 
        //animation, edit, crop, rotate

        if (!isDrmMethod(DrmStore.DrmMethod.METHOD_FL)) {
            //for local image that is drm and is not fl type, add
            //consume operation.
            //Noto: consume_drm operation has nothing to do
            //with current drm rights status
            operation |= SUPPORT_CONSUME_DRM;
        }

        if (DrmStore.RightsStatus.RIGHTS_VALID ==
            drmRights(DrmStore.Action.WALLPAPER)) {
            operation |= SUPPORT_SETAS;
        }

        if (DrmStore.RightsStatus.RIGHTS_VALID ==
            drmRights(DrmStore.Action.TRANSFER)) {
            operation |= SUPPORT_SHARE;
        }
        
        if(mIsGifAnimationSupported && isGifImage()) {
            operation |= SUPPORT_GIF_ANIMATION;
        }
        
        if (mIsPrintSupported &&
            DrmStore.RightsStatus.RIGHTS_VALID ==
            drmRights(DrmStore.Action.PRINT)) {
            operation |= SUPPORT_PRINT;
        }

        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            operation |= SUPPORT_SHOW_ON_MAP;
        }
        return operation;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        mApplication.getContentResolver().delete(baseUri, "_id=?",
                new String[]{String.valueOf(id)});
    }

    private static String getExifOrientation(int orientation) {
        switch (orientation) {
            case 0:
                return String.valueOf(ExifInterface.ORIENTATION_NORMAL);
            case 90:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_90);
            case 180:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_180);
            case 270:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_270);
            default:
                throw new AssertionError("invalid: " + orientation);
        }
    }

    @Override
    public void rotate(int degrees) {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        int rotation = (this.rotation + degrees) % 360;
        if (rotation < 0) rotation += 360;

        if (mimeType.equalsIgnoreCase("image/jpeg")) {
            try {
                ExifInterface exif = new ExifInterface(filePath);
                exif.setAttribute(ExifInterface.TAG_ORIENTATION,
                        getExifOrientation(rotation));
                exif.saveAttributes();
            } catch (IOException e) {
                Log.w(TAG, "cannot set exif data: " + filePath);
            }

            // We need to update the filesize as well
            fileSize = new File(filePath).length();
            values.put(Images.Media.SIZE, fileSize);
        }

        values.put(Images.Media.ORIENTATION, rotation);
        mApplication.getContentResolver().update(baseUri, values, "_id=?",
                new String[]{String.valueOf(id)});
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    @Override
    public int getMediaType() {
        return MEDIA_TYPE_IMAGE;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();

        if (!mimeType.equalsIgnoreCase("image/jpeg") &&
            !MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            //if image is not exif or mpo, no width or height values can 
            //be extracted exif data, then set these values with the ones
            //queried from database

            //this is added to support stereo display, because the logical dimension of
            //jps/pns image is not what it actually is!
            adjustDimIfNeeded();

            details.addDetail(MediaDetails.INDEX_WIDTH,String.valueOf(width));
            details.addDetail(MediaDetails.INDEX_HEIGHT,String.valueOf(height));
            return details;
        }

        details.addDetail(MediaDetails.INDEX_ORIENTATION, Integer.valueOf(rotation));
        MediaDetails.extractExifInfo(details, filePath);
        return details;
    }

    @Override
    public int getRotation() {
        return rotation;
    }

    @Override
    public int getWidth() {
        //this is added to support stereo display, because the logical dimension of
        //jps/pns image is not what it actually is!
        adjustDimIfNeeded();

        return width;
    }

    @Override
    public int getHeight() {
        //this is added to support stereo display, because the logical dimension of
        //jps/pns image is not what it actually is!
        adjustDimIfNeeded();

        return height;
    }

    @Override
    public boolean hasDrmRights() {
         return DrmStore.RightsStatus.RIGHTS_VALID == 
                DrmHelper.checkRightsStatus(mApplication.getAndroidContext(), 
                                            filePath, DrmStore.Action.DISPLAY);
    }

    @Override
    public int drmRights(int action) {
         return DrmHelper.checkRightsStatus(mApplication.getAndroidContext(),
                                            filePath, action);
    }

    @Override
    public boolean isTimeInterval() {
         return DrmHelper.isTimeIntervalMedia(mApplication.getAndroidContext(), 
                                            filePath, DrmStore.Action.DISPLAY);
    }

    private void initMpoSubType() {
        if (-1 == mMpoSubType) {
            ContentResolver resolver = mApplication.getContentResolver();
            MpoDecoder mpoDecoder = MpoDecoder.decodeFile(filePath);
            if (null != mpoDecoder) {
                mMpoSubType = mpoDecoder.suggestMtkMpoType();
                mpoDecoder.close();
            }
        }
    }

    private void adjustDimIfNeeded() {
        if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType) &&
            mIsStereoDisplaySupported && !mStereoDimAdjusted) {
            int layout = getStereoLayout();
            width = StereoHelper.adjustDim(true, layout, width);
            height = StereoHelper.adjustDim(false, layout, height);
            mStereoDimAdjusted = true;
        }
    }

    public int getSubType() {
        int subType = 0;
    	// MPO image has the same supported operations as video
        if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            initMpoSubType();
            if (MpoDecoder.MTK_TYPE_MAV == mMpoSubType) {
                subType |= MediaObject.SUBTYPE_MPO_MAV;
            } else if (MpoDecoder.MTK_TYPE_Stereo == mMpoSubType){
                subType |= MediaObject.SUBTYPE_MPO_3D;
            }else if (MpoDecoder.MTK_TYPE_3DPan == mMpoSubType){
                //subType |= MediaObject.SUBTYPE_MPO_3D_PAN;
                //as 3D panorama is currently not supported,
                //perceive all 3D panorama as 3D stereo photo
                subType |= MediaObject.SUBTYPE_MPO_3D;
            }
        }

    	// JPS stereo image has to be treated specially
        if (StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            if (mIsStereoDisplaySupported) {
                subType |= MediaObject.SUBTYPE_STEREO_JPS;
            }
        }

        //when show drm media whose type is not FL, show extra lock
        if (mIsDrmSupported && isDrm() &&
            DrmStore.DrmMethod.METHOD_FL != drm_method) {
            if (!hasDrmRights()) {
                subType |= SUBTYPE_DRM_NO_RIGHT;
            } else {
                subType |= SUBTYPE_DRM_HAS_RIGHT;
            }
        }
        return subType;
    }

    public int getConvergence() {
        return mConvergence;
    }
}
