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

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.MediaStore;

import  com.android.pqtuningtool.app.Gallery;
import  com.android.pqtuningtool.app.GalleryApp;
import  com.android.pqtuningtool.data.MediaSet.ItemConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import  com.android.pqtuningtool.util.MediatekFeature;

class LocalSource extends MediaSource {

    public static final String KEY_BUCKET_ID = "bucketId";

    private GalleryApp mApplication;
    private PathMatcher mMatcher;
    private static final int NO_MATCH = -1;
    private final UriMatcher mUriMatcher = new UriMatcher(NO_MATCH);
    public static final Comparator<PathId> sIdComparator = new IdComparator();

    private static final int LOCAL_IMAGE_ALBUMSET = 0;
    private static final int LOCAL_VIDEO_ALBUMSET = 1;
    private static final int LOCAL_IMAGE_ALBUM = 2;
    private static final int LOCAL_VIDEO_ALBUM = 3;
    private static final int LOCAL_IMAGE_ITEM = 4;
    private static final int LOCAL_VIDEO_ITEM = 5;
    private static final int LOCAL_ALL_ALBUMSET = 6;
    private static final int LOCAL_ALL_ALBUM = 7;

    private static final String TAG = "LocalSource";

    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private ContentProviderClient mClient;

    public LocalSource(GalleryApp context) {
        super("local");
        mApplication = context;
        mMatcher = new PathMatcher();
        mMatcher.add("/local/image", LOCAL_IMAGE_ALBUMSET);
        mMatcher.add("/local/video", LOCAL_VIDEO_ALBUMSET);
        mMatcher.add("/local/all", LOCAL_ALL_ALBUMSET);

        mMatcher.add("/local/image/*", LOCAL_IMAGE_ALBUM);
        mMatcher.add("/local/video/*", LOCAL_VIDEO_ALBUM);
        mMatcher.add("/local/all/*", LOCAL_ALL_ALBUM);
        mMatcher.add("/local/image/item/*", LOCAL_IMAGE_ITEM);
        mMatcher.add("/local/video/item/*", LOCAL_VIDEO_ITEM);

        mUriMatcher.addURI(MediaStore.AUTHORITY,
                "external/images/media/#", LOCAL_IMAGE_ITEM);
        mUriMatcher.addURI(MediaStore.AUTHORITY,
                "external/video/media/#", LOCAL_VIDEO_ITEM);
        mUriMatcher.addURI(MediaStore.AUTHORITY,
                "external/images/media", LOCAL_IMAGE_ALBUM);
        mUriMatcher.addURI(MediaStore.AUTHORITY,
                "external/video/media", LOCAL_VIDEO_ALBUM);
    }

    @Override
    public MediaObject createMediaObject(Path path) {
        GalleryApp app = mApplication;
        switch (mMatcher.match(path)) {
            case LOCAL_ALL_ALBUMSET:
            case LOCAL_IMAGE_ALBUMSET:
            case LOCAL_VIDEO_ALBUMSET:
                return new LocalAlbumSet(path, mApplication);
            case LOCAL_IMAGE_ALBUM:
                return new LocalAlbum(path, app, mMatcher.getIntVar(0), true);
            case LOCAL_VIDEO_ALBUM:
                return new LocalAlbum(path, app, mMatcher.getIntVar(0), false);
            case LOCAL_ALL_ALBUM: {
                int bucketId = mMatcher.getIntVar(0);

                //added for DRM feature
                //new path that include drm information
                Path imagePath = LocalAlbumSet.getPathImage().getChild(bucketId);
                Path videoPath = LocalAlbumSet.getPathVideo().getChild(bucketId);
                if (mIsDrmSupported) {
                    imagePath.setDrmInclusion(path.getDrmInclusion());
                    videoPath.setDrmInclusion(path.getDrmInclusion());
                }

                DataManager dataManager = app.getDataManager();
                MediaSet imageSet = (MediaSet) dataManager.getMediaObject(
                        //LocalAlbumSet.PATH_IMAGE.getChild(bucketId));
                        imagePath);
                MediaSet videoSet = (MediaSet) dataManager.getMediaObject(
                        //LocalAlbumSet.PATH_VIDEO.getChild(bucketId));
                        videoPath);
                Comparator<MediaItem> comp = DataManager.sDateTakenComparator;
                return new LocalMergeAlbum(
                        path, comp, new MediaSet[] {imageSet, videoSet});
            }
            case LOCAL_IMAGE_ITEM:
                return new LocalImage(path, mApplication, mMatcher.getIntVar(0));
            case LOCAL_VIDEO_ITEM:
                return new LocalVideo(path, mApplication, mMatcher.getIntVar(0));
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    private static int getMediaType(String type, int defaultType) {
        if (type == null) return defaultType;
        try {
            int value = Integer.parseInt(type);
            if ((value & (MEDIA_TYPE_IMAGE
                    | MEDIA_TYPE_VIDEO)) != 0) return value;
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid type: " + type, e);
        }
        return defaultType;
    }

    // The media type bit passed by the intent
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 4;

    private Path getAlbumPath(Uri uri, int defaultType) {
        int mediaType = getMediaType(
                uri.getQueryParameter(Gallery.KEY_MEDIA_TYPES),
                defaultType);
        String bucketId = uri.getQueryParameter(KEY_BUCKET_ID);
        int id = 0;
        try {
            id = Integer.parseInt(bucketId);
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid bucket id: " + bucketId, e);
            return null;
        }
        switch (mediaType) {
            case MEDIA_TYPE_IMAGE:
                return Path.fromString("/local/image").getChild(id);
            case MEDIA_TYPE_VIDEO:
                return Path.fromString("/local/video").getChild(id);
            default:
                return Path.fromString("/local/all").getChild(id);
        }
    }

    @Override
    public Path findPathByUri(Uri uri) {
        try {
            switch (mUriMatcher.match(uri)) {
                case LOCAL_IMAGE_ITEM: {
                    long id = ContentUris.parseId(uri);
                    return id >= 0 ? LocalImage.ITEM_PATH.getChild(id) : null;
                }
                case LOCAL_VIDEO_ITEM: {
                    long id = ContentUris.parseId(uri);
                    return id >= 0 ? LocalVideo.ITEM_PATH.getChild(id) : null;
                }
                case LOCAL_IMAGE_ALBUM: {
                    return getAlbumPath(uri, MEDIA_TYPE_IMAGE);
                }
                case LOCAL_VIDEO_ALBUM: {
                    return getAlbumPath(uri, MEDIA_TYPE_VIDEO);
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "uri: " + uri.toString(), e);
        }
        return null;
    }

    @Override
    public Path getDefaultSetOf(Path item) {
        MediaObject object = mApplication.getDataManager().getMediaObject(item);
        if (object instanceof LocalMediaItem) {
            return Path.fromString("/local/all").getChild(
                    String.valueOf(((LocalMediaItem) object).getBucketId()));
        }
        return null;
    }

    @Override
    public void mapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        ArrayList<PathId> imageList = new ArrayList<PathId>();
        ArrayList<PathId> videoList = new ArrayList<PathId>();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = list.get(i);
            // We assume the form is: "/local/{image,video}/item/#"
            // We don't use mMatcher for efficiency's reason.
            Path parent = pid.path.getParent();
            if (parent == LocalImage.ITEM_PATH) {
                imageList.add(pid);
            } else if (parent == LocalVideo.ITEM_PATH) {
                videoList.add(pid);
            }
        }
        // TODO: use "files" table so we can merge the two cases.
        processMapMediaItems(imageList, consumer, true);
        processMapMediaItems(videoList, consumer, false);
    }

    private void processMapMediaItems(ArrayList<PathId> list,
            ItemConsumer consumer, boolean isImage) {
        // Sort path by path id
        Collections.sort(list, sIdComparator);
        int n = list.size();
        for (int i = 0; i < n; ) {
            PathId pid = list.get(i);

            // Find a range of items.
            ArrayList<Integer> ids = new ArrayList<Integer>();
            int startId = Integer.parseInt(pid.path.getSuffix());
            ids.add(startId);

            int j;
            for (j = i + 1; j < n; j++) {
                PathId pid2 = list.get(j);
                int curId = Integer.parseInt(pid2.path.getSuffix());
                if (curId - startId >= MediaSet.MEDIAITEM_BATCH_FETCH_COUNT) {
                    break;
                }
                ids.add(curId);
            }

            MediaItem[] items = LocalAlbum.getMediaItemById(
                    mApplication, isImage, ids);
            for(int k = i ; k < j; k++) {
                PathId pid2 = list.get(k);
                consumer.consume(pid2.id, items[k - i]);
            }

            i = j;
        }
    }

    // This is a comparator which compares the suffix number in two Paths.
    private static class IdComparator implements Comparator<PathId> {
        public int compare(PathId p1, PathId p2) {
            String s1 = p1.path.getSuffix();
            String s2 = p2.path.getSuffix();
            int len1 = s1.length();
            int len2 = s2.length();
            if (len1 < len2) {
                return -1;
            } else if (len1 > len2) {
                return 1;
            } else {
                return s1.compareTo(s2);
            }
        }
    }

    @Override
    public void resume() {
        mClient = mApplication.getContentResolver()
                .acquireContentProviderClient(MediaStore.AUTHORITY);
    }

    @Override
    public void pause() {
        mClient.release();
        mClient = null;
    }
}
