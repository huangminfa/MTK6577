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

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.app.GalleryApp;
import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.util.GalleryUtils;
import  com.android.pqtuningtool.util.MediaSetUtils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import  com.android.pqtuningtool.util.MediatekFeature;
import  com.android.pqtuningtool.util.MpoHelper;
import  com.android.pqtuningtool.util.DrmHelper;

// LocalAlbumSet lists all image or video albums in the local storage.
// The path should be "/local/image", "local/video" or "/local/all"
public class LocalAlbumSet extends MediaSet {
    public static final Path PATH_ALL = Path.fromString("/local/all");
    public static final Path PATH_IMAGE = Path.fromString("/local/image");
    public static final Path PATH_VIDEO = Path.fromString("/local/video");

    private static final String TAG = "LocalAlbumSet";
    private static final String EXTERNAL_MEDIA = "external";

    // The indices should match the following projections.
    private static final int INDEX_BUCKET_ID = 0;
    private static final int INDEX_MEDIA_TYPE = 1;
    private static final int INDEX_BUCKET_NAME = 2;

    private static final Uri mBaseUri = Files.getContentUri(EXTERNAL_MEDIA);
    private static final Uri mWatchUriImage = Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri mWatchUriVideo = Video.Media.EXTERNAL_CONTENT_URI;

    // BUCKET_DISPLAY_NAME is a string like "Camera" which is the directory
    // name of where an image or video is in. BUCKET_ID is a hash of the path
    // name of that directory (see computeBucketValues() in MediaProvider for
    // details). MEDIA_TYPE is video, image, audio, etc.
    //
    // The "albums" are not explicitly recorded in the database, but each image
    // or video has the two columns (BUCKET_ID, MEDIA_TYPE). We define an
    // "album" to be the collection of images/videos which have the same value
    // for the two columns.
    //
    // The goal of the query (used in loadSubMediaSets()) is to find all albums,
    // that is, all unique values for (BUCKET_ID, MEDIA_TYPE). In the meantime
    // sort them by the timestamp of the latest image/video in each of the album.
    //
    // The order of columns below is important: it must match to the index in
    // MediaStore.
    private static final String[] PROJECTION_BUCKET = {
            ImageColumns.BUCKET_ID,
            FileColumns.MEDIA_TYPE,
            ImageColumns.BUCKET_DISPLAY_NAME };

    // We want to order the albums by reverse chronological order. We abuse the
    // "WHERE" parameter to insert a "GROUP BY" clause into the SQL statement.
    // The template for "WHERE" parameter is like:
    //    SELECT ... FROM ... WHERE (%s)
    // and we make it look like:
    //    SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
    // The "(1)" means true. The "1,(2)" means the first two columns specified
    // after SELECT. Note that because there is a ")" in the template, we use
    // "(2" to match it.
    private static final String BUCKET_GROUP_BY =
            "1) GROUP BY 1,(2";
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

    private final GalleryApp mApplication;
    private final int mType;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private final ChangeNotifier mNotifierImage;
    private final ChangeNotifier mNotifierVideo;
    private final String mName;

    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();

    // as MediaTek DRM feature should be added to Google's solution, we have to
    // modify BUCKET_GROUP_BY for complicated queries. For example: FL (forward
    // lock) kind DRM media should not be forworded by e-mail or MMS, but SD 
    // (separate delivery) kind can. So our in-house app should pass other
    // variable to indicate whether some kind DRM media should be or should not 
    // be displayed

    // this variable is used to work with where clause (BUCKET_GROUP_BY has
    // a always true where clause which is no longer suitable for our feature.
    private static final String PURE_BUCKET_GROUP_BY = ") GROUP BY 1,(2";
    // as we only care about videos and images, add condition that only queries
    // videos and images from files table
    private static final String VIDEO_IMAGE_CLAUSE = 
                FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_IMAGE + 
                " OR " +
                FileColumns.MEDIA_TYPE + "=" + FileColumns.MEDIA_TYPE_VIDEO;

    public static Path getPathAll() {
        if (mIsDrmSupported) {
            return Path.fromString("/local/all");
        } else {
            return PATH_ALL;
        }
    }
    public static Path getPathImage() {
        if (mIsDrmSupported) {
            return Path.fromString("/local/image");
        } else {
            return PATH_IMAGE;
        }
    }
    public static Path getPathVideo() {
        if (mIsDrmSupported) {
            return Path.fromString("/local/video");
        } else {
            return PATH_VIDEO;
        }
    }

    public LocalAlbumSet(Path path, GalleryApp application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mType = getTypeFromPath(path);
        mNotifierImage = new ChangeNotifier(this, mWatchUriImage, application);
        mNotifierVideo = new ChangeNotifier(this, mWatchUriVideo, application);
        mName = application.getResources().getString(
                R.string.set_label_local_albums);
    }

    private static int getTypeFromPath(Path path) {
        String name[] = path.split();
        if (name.length < 2) {
            throw new IllegalArgumentException(path.toString());
        }
        if ("all".equals(name[1])) return MEDIA_TYPE_ALL;
        if ("image".equals(name[1])) return MEDIA_TYPE_IMAGE;
        if ("video".equals(name[1])) return MEDIA_TYPE_VIDEO;
        throw new IllegalArgumentException(path.toString());
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mName;
    }

    private BucketEntry[] loadBucketEntries(Cursor cursor) {
        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
        int typeBits = 0;
        if ((mType & MEDIA_TYPE_IMAGE) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_IMAGE);
        }
        if ((mType & MEDIA_TYPE_VIDEO) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_VIDEO);
        }
        try {
            while (cursor.moveToNext()) {
                if ((typeBits & (1 << cursor.getInt(INDEX_MEDIA_TYPE))) != 0) {
                    BucketEntry entry = new BucketEntry(
                            cursor.getInt(INDEX_BUCKET_ID),
                            cursor.getString(INDEX_BUCKET_NAME));
                    if (!buffer.contains(entry)) {
                        buffer.add(entry);
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return buffer.toArray(new BucketEntry[buffer.size()]);
    }


    private static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n ; ++i) {
            if (entries[i].bucketId == bucketId) return i;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<MediaSet> loadSubMediaSets() {
        // Note: it will be faster if we only select media_type and bucket_id.
        //       need to test the performance if that is worth

        Uri uri = mBaseUri;
        GalleryUtils.assertNotInRenderThread();

        //added support for MPO and DRM
        String whereClauseMpo = MpoHelper.getMpoWhereClause(mIsMpoSupported);

        String whereClauseDrm = null;
        if (mIsDrmSupported) {
            whereClauseDrm = DrmHelper.getDrmWhereClause(mPath.getDrmInclusion());
        }
        String whereGroup = null;
        if (null != whereClauseMpo) {
            whereGroup = whereClauseMpo;
        }
        if (null != whereClauseDrm) {
            whereGroup = (null == whereGroup) ? whereClauseDrm :
                         "(" + whereGroup + ") AND (" + whereClauseDrm +")";
        }
        if (null == whereGroup) {
            whereGroup = VIDEO_IMAGE_CLAUSE + PURE_BUCKET_GROUP_BY;
        } else {
            whereGroup = "(" + VIDEO_IMAGE_CLAUSE + ") AND (" + whereGroup +")" + 
                         PURE_BUCKET_GROUP_BY;
        }

        Cursor cursor = mApplication.getContentResolver().query(
                uri, PROJECTION_BUCKET, whereGroup, null, BUCKET_ORDER_BY);
        if (cursor == null) {
            Log.w(TAG, "cannot open local database: " + uri);
            return new ArrayList<MediaSet>();
        }
        BucketEntry[] entries = loadBucketEntries(cursor);
        int offset = 0;

        // Move camera and download bucket to the front, while keeping the
        // order of others.
        int index = findBucket(entries, MediaSetUtils.CAMERA_BUCKET_ID);
        if (index != -1) {
            circularShiftRight(entries, offset++, index);
        }
        index = findBucket(entries, MediaSetUtils.DOWNLOAD_BUCKET_ID);
        if (index != -1) {
            circularShiftRight(entries, offset++, index);
        }

        ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
        DataManager dataManager = mApplication.getDataManager();
        for (BucketEntry entry : entries) {
            albums.add(getLocalAlbum(dataManager,
                    mType, mPath, entry.bucketId, entry.bucketName));
        }
        for (int i = 0, n = albums.size(); i < n; ++i) {
            albums.get(i).reload();
        }
        return albums;
    }

    private MediaSet getLocalAlbum(
            DataManager manager, int type, Path parent, int id, String name) {
        Path path = parent.getChild(id);

        if (mIsDrmSupported) {
            path.setDrmInclusion(parent.getDrmInclusion());
        }

        MediaObject object = manager.peekMediaObject(path);
        if (object != null) return (MediaSet) object;
        switch (type) {
            case MEDIA_TYPE_IMAGE:
                return new LocalAlbum(path, mApplication, id, true, name);
            case MEDIA_TYPE_VIDEO:
                return new LocalAlbum(path, mApplication, id, false, name);
            case MEDIA_TYPE_ALL:
                Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                Path imagePath = getPathImage();
                Path videoPath = getPathVideo();
                if (mIsDrmSupported) {
                    imagePath.setDrmInclusion(parent.getDrmInclusion());
                    videoPath.setDrmInclusion(parent.getDrmInclusion());
                }
                return new LocalMergeAlbum(path, comp, new MediaSet[] {
                        getLocalAlbum(manager, MEDIA_TYPE_IMAGE, imagePath, id, name),
                        getLocalAlbum(manager, MEDIA_TYPE_VIDEO, videoPath, id, name)});
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }

    public static String getBucketName(ContentResolver resolver, int bucketId) {
        Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", "1")
                .build();

        Cursor cursor = resolver.query(
                uri, PROJECTION_BUCKET, "bucket_id = ?",
                new String[]{String.valueOf(bucketId)}, null);

        if (cursor == null) {
            Log.w(TAG, "query fail: " + uri);
            return "";
        }
        try {
            return cursor.moveToNext()
                    ? cursor.getString(INDEX_BUCKET_NAME)
                    : "";
        } finally {
            cursor.close();
        }
    }

    @Override
    public long reload() {
        // "|" is used instead of "||" because we want to clear both flags.
        if (mNotifierImage.isDirty() | mNotifierVideo.isDirty()) {
            mDataVersion = nextVersionNumber();
            mAlbums = loadSubMediaSets();
        }
        return mDataVersion;
    }

    // For debug only. Fake there is a ContentObserver.onChange() event.
    void fakeChange() {
        mNotifierImage.fakeChange();
        mNotifierVideo.fakeChange();
    }

    private static class BucketEntry {
        public String bucketName;
        public int bucketId;

        public BucketEntry(int id, String name) {
            bucketId = id;
            bucketName = Utils.ensureNotNull(name);
        }

        @Override
        public int hashCode() {
            return bucketId;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BucketEntry)) return false;
            BucketEntry entry = (BucketEntry) object;
            return bucketId == entry.bucketId;
        }
    }

    // Circular shift the array range from a[i] to a[j] (inclusive). That is,
    // a[i] -> a[i+1] -> a[i+2] -> ... -> a[j], and a[j] -> a[i]
    private static <T> void circularShiftRight(T[] array, int i, int j) {
        T temp = array[j];
        for (int k = j; k > i; k--) {
            array[k] = array[k - 1];
        }
        array[i] = temp;
    }
}
