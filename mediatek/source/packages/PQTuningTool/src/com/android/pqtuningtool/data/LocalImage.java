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

package  com.android.pqtuningtool.data;

import  com.android.pqtuningtool.app.GalleryApp;
import  com.android.pqtuningtool.app.PictureQualityTool;
import  com.android.pqtuningtool.common.BitmapUtils;
import com.android.pqtuningtool.pqjni.PictureQualityJni;
import  com.android.pqtuningtool.util.MpoHelper;
import  com.android.pqtuningtool.util.GalleryUtils;
import  com.android.pqtuningtool.util.MtkLog;
import com.android.pqtuningtool.util.PictureQualityOptions;
import  com.android.pqtuningtool.util.ThreadPool.Job;
import  com.android.pqtuningtool.util.ThreadPool.JobContext;
import  com.android.pqtuningtool.util.UpdateHelper;

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
import  com.android.pqtuningtool.util.MediatekFeature;
import  com.android.pqtuningtool.util.DrmHelper;
import android.drm.DrmStore;

// LocalImage represents an image in the local storage.
public class LocalImage extends LocalMediaItem {
    private static final String TAG = "LocalImage";

    static final Path ITEM_PATH = Path.fromString("/local/image/item");

    //added to support Mediatek features
    private static final boolean mIsDrmSupported =
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported =
                                          MediatekFeature.isMpoSupported();
    private static final boolean mIsPrintSupported =
                                          MediatekFeature.isBluetoothPrintSupported();

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
        width = uh.update(width, cursor.getInt(INDEX_WIDTH));
        height = uh.update(height, cursor.getInt(INDEX_HEIGHT));
        //added to support drm feature
        if (mIsDrmSupported) {
            is_drm = uh.update(is_drm,cursor.getInt(INDEX_IS_DRM));
            drm_method = uh.update(drm_method,cursor.getInt(INDEX_DRM_METHOD));
        }
        return uh.isUpdated();
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new LocalImageRequest(mApplication, mPath, type, filePath);
    }

    public static class LocalImageRequest extends ImageCacheRequest {
        private final String mLocalFilePath;

        LocalImageRequest(GalleryApp application, Path path, int type,
                String localFilePath) {
            super(application, path, type, getTargetSize(type));
            mLocalFilePath = localFilePath;
        }

        @Override
        public Bitmap onDecodeOriginal(JobContext jc, int type, boolean isPictureQualistyEnhance) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //for picture quality enhancement
            if (MediatekFeature.isPictureQualityEnhanceSupported()) {
                options.inPostProc = true;
            }
            PictureQualityJni.nativeSetSharpnessIndex(PictureQualityOptions.getSharpnessOptin());
            MtkLog.i(TAG, "Sharpness Index is " + PictureQualityJni.nativeGetSharpnessIndex());

            PictureQualityJni.nativeSetColorIndex(PictureQualityOptions.getColorOptin());
            MtkLog.i(TAG, "Color Index is " + PictureQualityJni.nativeGetColorIndex());

            PictureQualityJni.nativeSetSkinToneIndex(PictureQualityOptions.getSkinToneOptin());
            MtkLog.i(TAG, "SkinTone Index is " + PictureQualityJni.nativeGetSkinToneIndex());

            PictureQualityJni.nativeSetGrassToneIndex(PictureQualityOptions.getGrassToneOptin());
            MtkLog.i(TAG, "GrassTone Index is " + PictureQualityJni.nativeGetGrassToneIndex());

            PictureQualityJni.nativeSetSkyToneIndex(PictureQualityOptions.getSkyToneOption());
            MtkLog.i(TAG, "SkyTone Index is " + PictureQualityJni.nativeGetSkyToneIndex());

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
                    MtkLog.i(TAG, "onDecodeOriginal : bitmap_jpg is  " + bitmap);
                    if (bitmap != null) return bitmap;
                }
            }

            Bitmap bitmap = null;
            if (mIsDrmSupported && mLocalFilePath.toLowerCase().endsWith(".dcf")) {
                ContentResolver resolver = mApplication.getContentResolver();
                Uri drmUri = Uri.parse("file:///" + mLocalFilePath);
                bitmap = DrmHelper.forceDecodeDrmUri(resolver, drmUri, null, false);
            } else {
                bitmap = DecodeUtils.requestDecode(
                       jc, mLocalFilePath, options, getTargetSize(type));
                MtkLog.i(TAG, "onDecodeOriginal : bitmap is  " + bitmap);
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
    public int getSupportedOperations() {
        //added for DRM feature
        if (MediatekFeature.isDrmSupported() && isDrm()) {
            return getDrmSupportedOperations();
        }

        // MPO image has the same supported operations as video
        if (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            return (SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_PLAY | SUPPORT_INFO);
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
        return width;
    }

    @Override
    public int getHeight() {
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
}
