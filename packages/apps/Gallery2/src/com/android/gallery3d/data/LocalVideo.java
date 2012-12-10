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

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.UpdateHelper;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;

import java.io.File;

import com.mediatek.gifDecoder.GifDecoder;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.DrmHelper;
import com.android.gallery3d.util.StereoHelper;
import android.drm.DrmStore;

import java.util.HashMap;
import java.util.LinkedHashMap;

// LocalVideo represents a video in the local storage.
public class LocalVideo extends LocalMediaItem {

    static final Path ITEM_PATH = Path.fromString("/local/video/item");

    private static final String TAG = "LocalVideo";

    static final Path ITEM_PATH_DRM_FL = Path.fromString("/local/video/item", 
                                         MediatekFeature.INCLUDE_FL_DRM_MEDIA);
    static final Path ITEM_PATH_DRM_SD = Path.fromString("/local/video/item", 
                                         MediatekFeature.INCLUDE_SD_DRM_MEDIA);
    static final Path ITEM_PATH_DRM_ALL = Path.fromString("/local/video/item",
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
                    path = Path.fromString("/local/video/item", mtkInclusion);
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
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

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
    private static final int INDEX_DURATION = 9;
    private static final int INDEX_BUCKET_ID = 10;
    private static final int INDEX_SIZE_ID = 11;
    //added to support DRM
    private static final int INDEX_IS_DRM = 12;
    private static final int INDEX_DRM_METHOD = 13;
    //added to support Stereo display
    private static final int INDEX_STEREO_TYPE = 14;

    static final String[] PROJECTION = new String[] {
            VideoColumns._ID,
            VideoColumns.TITLE,
            VideoColumns.MIME_TYPE,
            VideoColumns.LATITUDE,
            VideoColumns.LONGITUDE,
            VideoColumns.DATE_TAKEN,
            VideoColumns.DATE_ADDED,
            VideoColumns.DATE_MODIFIED,
            VideoColumns.DATA,
            VideoColumns.DURATION,
            VideoColumns.BUCKET_ID,
            VideoColumns.SIZE,
            //added to support DRM
            VideoColumns.IS_DRM,
            VideoColumns.DRM_METHOD,
            //added to support stereo display
            Video.Media.STEREO_TYPE,
    };

    private final GalleryApp mApplication;
    private static Bitmap sOverlay;

    public int durationInSec;

    public LocalVideo(Path path, GalleryApp application, Cursor cursor) {
        super(path, nextVersionNumber());
        mApplication = application;
        loadFromCursor(cursor);
    }

    public LocalVideo(Path path, GalleryApp context, int id) {
        super(path, nextVersionNumber());
        mApplication = context;
        ContentResolver resolver = mApplication.getContentResolver();
        Uri uri = Video.Media.EXTERNAL_CONTENT_URI;
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
        durationInSec = cursor.getInt(INDEX_DURATION) / 1000;
        bucketId = cursor.getInt(INDEX_BUCKET_ID);
        fileSize = cursor.getLong(INDEX_SIZE_ID);
        //added to support drm feature
        if (mIsDrmSupported) {
            is_drm = cursor.getInt(INDEX_IS_DRM);
            drm_method = cursor.getInt(INDEX_DRM_METHOD);
        }
        if (mIsStereoDisplaySupported) {
            stereoType = cursor.getInt(INDEX_STEREO_TYPE);
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
        durationInSec = uh.update(
                durationInSec, cursor.getInt(INDEX_DURATION) / 1000);
        bucketId = uh.update(bucketId, cursor.getInt(INDEX_BUCKET_ID));
        fileSize = uh.update(fileSize, cursor.getLong(INDEX_SIZE_ID));
        //added to support drm feature
        if (mIsDrmSupported) {
            is_drm = uh.update(is_drm,cursor.getInt(INDEX_IS_DRM));
            drm_method = uh.update(drm_method,cursor.getInt(INDEX_DRM_METHOD));
        }
        if (mIsStereoDisplaySupported) {
            stereoType = uh.update(stereoType,cursor.getInt(INDEX_STEREO_TYPE));
        }
        return uh.isUpdated();
    }

    @Override
    public Job<Bitmap> requestImage(int type) {
        return new LocalVideoRequest(mApplication, getPath(), type, filePath, dateModifiedInSec);
    }

    public static class LocalVideoRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        LocalVideoRequest(GalleryApp application, Path path, int type,
                String localFilePath, long dateModifiedInSec) {
            super(application, path, type, LocalImage.getTargetSize(type), dateModifiedInSec);
            mLocalFilePath = localFilePath;
        }

        @Override
        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Bitmap bitmap = BitmapUtils.createVideoThumbnail(mLocalFilePath);
            if (bitmap == null || jc.isCancelled()) return null;
            if (mIsStereoDisplaySupported) {
                DataManager manager = mApplication.getDataManager();
                LocalMediaItem item = (LocalMediaItem) manager.getMediaObject(mPath);
                int stereoType = item.stereoType;
                if (StereoHelper.isStereo(stereoType)) {
                    Bitmap temp = StereoHelper.getStereoVideoImage(jc, bitmap, 
                                                             true, stereoType);
                    bitmap.recycle();
                    bitmap = temp;
                }
            }

            return bitmap;
        }
    }

    @Override
    public Job<BitmapRegionDecoder> requestLargeImage() {
        throw new UnsupportedOperationException("Cannot regquest a large image"
                + " to a local video!");
    }

    @Override
    public Job<GifDecoder> requestGifDecoder() {
        throw new UnsupportedOperationException("Cannot regquest a GifDecoder"
                + " to a local video!");
    }

    @Override
    public Job<MediatekFeature.DataBundle> 
        requestImage(int type, MediatekFeature.Params params) {
        return new LocalImageRequest(mApplication, getPath(), type, filePath);
    }

    public static class LocalImageRequest implements 
                                       Job<MediatekFeature.DataBundle> {
        private GalleryApp mApplication;
        private Path mPath;
        private int mType;
        private int mTargetSize;
        private String mLocalFilePath;

        LocalImageRequest(GalleryApp application, Path path, int type,
                String localFilePath) {
            mApplication = application;
            mPath = path;
            mType = type;
            //mTargetSize = getTargetSize(type);
            mLocalFilePath = localFilePath;
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

            Bitmap bitmap = BitmapUtils.createVideoThumbnail(mLocalFilePath);
            if (bitmap == null || jc.isCancelled()) return null;
            Log.i(TAG,"LocalSecondImageRequest:bitmap.getWidth()="+bitmap.getWidth());
            Log.d(TAG,"LocalSecondImageRequest:bitmap.getHeight()="+bitmap.getHeight());
            DataManager manager = mApplication.getDataManager();
            LocalMediaItem item = (LocalMediaItem) manager.getMediaObject(mPath);
            int stereoType = item.stereoType;
            if (StereoHelper.isStereo(stereoType)) {
                Bitmap temp = StereoHelper.getStereoVideoImage(jc, bitmap, false,
                                                               stereoType);
                bitmap.recycle();
                bitmap = temp;
            }

            MediatekFeature.DataBundle dataBundle = 
                                     new MediatekFeature.DataBundle();
            dataBundle.secondFrame = bitmap;
            return dataBundle;
        }
    }



    @Override
    public int getSupportedOperations() {
        int operation = SUPPORT_DELETE | SUPPORT_SHARE | 
                        SUPPORT_PLAY | SUPPORT_INFO;
        if (isDrm()) {
            //add drm protection info
            operation |= SUPPORT_DRM_INFO;
            if (DrmStore.RightsStatus.RIGHTS_VALID !=
                drmRights(DrmStore.Action.TRANSFER)) {
                    //remove share operation if forbids.
                    operation &= ~SUPPORT_SHARE;
            }
        }
    	// thumbnail of stereo video can be displayed as a stereo photo
        if (mIsStereoDisplaySupported && StereoHelper.isStereo(stereoType)) {
            operation |= SUPPORT_STEREO_DISPLAY;
        }

        return operation;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
        mApplication.getContentResolver().delete(baseUri, "_id=?",
                new String[]{String.valueOf(id)});
    }

    @Override
    public void rotate(int degrees) {
        // TODO
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    @Override
    public Uri getPlayUri() {
        return getContentUri();
    }

    @Override
    public int getMediaType() {
        return MEDIA_TYPE_VIDEO;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        int s = durationInSec;
        if (s > 0) {
            details.addDetail(MediaDetails.INDEX_DURATION, GalleryUtils.formatDuration(
                    mApplication.getAndroidContext(), durationInSec));
        }
        return details;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean hasDrmRights() {
         return DrmStore.RightsStatus.RIGHTS_VALID == 
                DrmHelper.checkRightsStatus(mApplication.getAndroidContext(), 
                                            filePath, DrmStore.Action.PLAY);
    }

    @Override
    public int drmRights(int action) {
         return DrmHelper.checkRightsStatus(mApplication.getAndroidContext(), 
                                            filePath, action);
    }

    @Override
    public boolean isTimeInterval() {
         return DrmHelper.isTimeIntervalMedia(mApplication.getAndroidContext(), 
                                            filePath, DrmStore.Action.PLAY);
    }

    public int getSubType() {
        int subType = 0;
        
    	// VIDEO STEREO
        if (mIsStereoDisplaySupported && StereoHelper.isStereo(stereoType)) {
            subType |= MediaObject.SUBTYPE_STEREO_VIDEO;
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
}
