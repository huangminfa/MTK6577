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

package  com.android.pqtuningtool.picasasource;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.ExifInterface;
import android.os.ParcelFileDescriptor;

import  com.android.pqtuningtool.app.GalleryApp;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.MediaObject;
import  com.android.pqtuningtool.data.MediaSet;
import  com.android.pqtuningtool.data.MediaSource;
import  com.android.pqtuningtool.data.Path;
import  com.android.pqtuningtool.data.PathMatcher;

import java.io.FileNotFoundException;

public class PicasaSource extends MediaSource {
    private static final String TAG = "PicasaSource";

    private static final int NO_MATCH = -1;
    private static final int IMAGE_MEDIA_ID = 1;

    private static final int PICASA_ALBUMSET = 0;
    private static final int MAP_BATCH_COUNT = 100;

    private GalleryApp mApplication;
    private PathMatcher mMatcher;

    public static final Path ALBUM_PATH = Path.fromString("/picasa/all");

    public PicasaSource(GalleryApp application) {
        super("picasa");
        mApplication = application;
        mMatcher = new PathMatcher();
        mMatcher.add("/picasa/all", PICASA_ALBUMSET);
        mMatcher.add("/picasa/image", PICASA_ALBUMSET);
        mMatcher.add("/picasa/video", PICASA_ALBUMSET);
    }

    private static class EmptyAlbumSet extends MediaSet {

        public EmptyAlbumSet(Path path, long version) {
            super(path, version);
        }

        @Override
        public String getName() {
            return "picasa";
        }

        @Override
        public long reload() {
            return mDataVersion;
        }
    }

    @Override
    public MediaObject createMediaObject(Path path) {
        switch (mMatcher.match(path)) {
            case PICASA_ALBUMSET:
                return new EmptyAlbumSet(path, MediaObject.nextVersionNumber());
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    public static MediaItem getFaceItem(Context context, MediaItem item, int faceIndex) {
        throw new UnsupportedOperationException();
    }

    public static boolean isPicasaImage(MediaObject object) {
        return false;
    }

    public static String getImageTitle(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static int getImageSize(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static String getContentType(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static long getDateTaken(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static double getLatitude(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static double getLongitude(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static int getRotation(MediaObject image) {
        throw new UnsupportedOperationException();
    }

    public static ParcelFileDescriptor openFile(Context context, MediaObject image, String mode)
            throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    public static void initialize(Context context) {/*do nothing*/}

    public static void requestSync(Context context) {/*do nothing*/}

    public static void showSignInReminder(Activity context) {/*do nothing*/}

    public static void onPackageAdded(Context context, String packageName) {/*do nothing*/}

    public static void onPackageRemoved(Context context, String packageName) {/*do nothing*/}

    public static void onPackageChanged(Context context, String packageName) {/*do nothing*/}

    public static void extractExifValues(MediaObject item, ExifInterface exif) {/*do nothing*/}

    public static Dialog getVersionCheckDialog(Activity activity){
        return null;
    }
}
