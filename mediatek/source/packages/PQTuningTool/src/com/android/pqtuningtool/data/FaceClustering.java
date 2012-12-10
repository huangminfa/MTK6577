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
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Rect;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.picasasource.PicasaSource;

import java.util.ArrayList;
import java.util.TreeMap;

public class FaceClustering extends Clustering {
    @SuppressWarnings("unused")
    private static final String TAG = "FaceClustering";

    private FaceCluster[] mClusters;
    private String mUntaggedString;
    private Context mContext;

    private class FaceCluster {
        ArrayList<Path> mPaths = new ArrayList<Path>();
        String mName;
        MediaItem mCoverItem;
        Rect mCoverRegion;
        int mCoverFaceIndex;

        public FaceCluster(String name) {
            mName = name;
        }

        public void add(MediaItem item, int faceIndex) {
            Path path = item.getPath();
            mPaths.add(path);
            Face[] faces = item.getFaces();
            if (faces != null) {
                Face face = faces[faceIndex];
                if (mCoverItem == null) {
                    mCoverItem = item;
                    mCoverRegion = face.getPosition();
                    mCoverFaceIndex = faceIndex;
                } else {
                    Rect region = face.getPosition();
                    if (mCoverRegion.width() < region.width() &&
                            mCoverRegion.height() < region.height()) {
                        mCoverItem = item;
                        mCoverRegion = face.getPosition();
                        mCoverFaceIndex = faceIndex;
                    }
                }
            }
        }

        public int size() {
            return mPaths.size();
        }

        public MediaItem getCover() {
            if (mCoverItem != null) {
                if (PicasaSource.isPicasaImage(mCoverItem)) {
                    return PicasaSource.getFaceItem(mContext, mCoverItem, mCoverFaceIndex);
                } else {
                    return mCoverItem;
                }
            }
            return null;
        }
    }

    public FaceClustering(Context context) {
        mUntaggedString = context.getResources().getString(R.string.untagged);
        mContext = context;
    }

    @Override
    public void run(MediaSet baseSet) {
        final TreeMap<Face, FaceCluster> map =
                new TreeMap<Face, FaceCluster>();
        final FaceCluster untagged = new FaceCluster(mUntaggedString);

        baseSet.enumerateTotalMediaItems(new MediaSet.ItemConsumer() {
            public void consume(int index, MediaItem item) {
                Face[] faces = item.getFaces();
                if (faces == null || faces.length == 0) {
                    untagged.add(item, -1);
                    return;
                }
                for (int j = 0; j < faces.length; j++) {
                    Face face = faces[j];
                    FaceCluster cluster = map.get(face);
                    if (cluster == null) {
                        cluster = new FaceCluster(face.getName());
                        map.put(face, cluster);
                    }
                    cluster.add(item, j);
                }
            }
        });

        int m = map.size();
        mClusters = map.values().toArray(new FaceCluster[m + ((untagged.size() > 0) ? 1 : 0)]);
        if (untagged.size() > 0) {
            mClusters[m] = untagged;
        }
    }

    @Override
    public int getNumberOfClusters() {
        return mClusters.length;
    }

    @Override
    public ArrayList<Path> getCluster(int index) {
        return mClusters[index].mPaths;
    }

    @Override
    public String getClusterName(int index) {
        return mClusters[index].mName;
    }

    @Override
    public MediaItem getClusterCover(int index) {
        return mClusters[index].getCover();
    }
}
