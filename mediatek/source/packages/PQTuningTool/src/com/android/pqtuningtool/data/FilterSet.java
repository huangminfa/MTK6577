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

import java.util.ArrayList;

// FilterSet filters a base MediaSet according to a condition. Currently the
// condition is a matching media type. It can be extended to other conditions
// if needed.
public class FilterSet extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "FilterSet";

    private final DataManager mDataManager;
    private final MediaSet mBaseSet;
    private final int mMediaType;
    private final ArrayList<Path> mPaths = new ArrayList<Path>();
    private final ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();

    public FilterSet(Path path, DataManager dataManager, MediaSet baseSet,
            int mediaType) {
        super(path, INVALID_DATA_VERSION);
        mDataManager = dataManager;
        mBaseSet = baseSet;
        mMediaType = mediaType;
        mBaseSet.addContentListener(this);
    }

    @Override
    public String getName() {
        return mBaseSet.getName();
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
    public int getMediaItemCount() {
        return mPaths.size();
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return ClusterAlbum.getMediaItemFromPath(
                mPaths, start, count, mDataManager);
    }

    @Override
    public long reload() {
        if (mBaseSet.reload() > mDataVersion) {
            updateData();
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    private void updateData() {
        // Albums
        mAlbums.clear();
        String basePath = "/filter/mediatype/" + mMediaType;

        for (int i = 0, n = mBaseSet.getSubMediaSetCount(); i < n; i++) {
            MediaSet set = mBaseSet.getSubMediaSet(i);
            String filteredPath = basePath + "/{" + set.getPath().toString() + "}";
            MediaSet filteredSet = mDataManager.getMediaSet(filteredPath);
            filteredSet.reload();
            if (filteredSet.getMediaItemCount() > 0
                    || filteredSet.getSubMediaSetCount() > 0) {
                mAlbums.add(filteredSet);
            }
        }

        // Items
        mPaths.clear();
        final int total = mBaseSet.getMediaItemCount();
        final Path[] buf = new Path[total];

        mBaseSet.enumerateMediaItems(new MediaSet.ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if (item.getMediaType() == mMediaType) {
                    if (index < 0 || index >= total) return;
                    Path path = item.getPath();
                    buf[index] = path;
                }
            }
        });

        for (int i = 0; i < total; i++) {
            if (buf[i] != null) {
                mPaths.add(buf[i]);
            }
        }
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_SHARE | SUPPORT_DELETE;
    }

    @Override
    public void delete() {
        ItemConsumer consumer = new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if ((item.getSupportedOperations() & SUPPORT_DELETE) != 0) {
                    item.delete();
                }
            }
        };
        mDataManager.mapMediaItems(mPaths, consumer, 0);
    }
}
