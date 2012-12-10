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

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;

public class SizeClustering extends Clustering {
    private static final String TAG = "SizeClustering";

    private Context mContext;
    private ArrayList<Path>[] mClusters;
    private String[] mNames;
    private long mMinSizes[];

    private static final long MEGA_BYTES = 1024L*1024;
    private static final long GIGA_BYTES = 1024L*1024*1024;

    private static final long[] SIZE_LEVELS = {
        0,
        1 * MEGA_BYTES,
        10 * MEGA_BYTES,
        100 * MEGA_BYTES,
        1 * GIGA_BYTES,
        2 * GIGA_BYTES,
        4 * GIGA_BYTES,
    };

    public SizeClustering(Context context) {
        mContext = context;
    }

    @Override
    public void run(MediaSet baseSet) {
        final ArrayList<Path>[] group =
                (ArrayList<Path>[]) new ArrayList[SIZE_LEVELS.length];
        baseSet.enumerateTotalMediaItems(new MediaSet.ItemConsumer() {
            public void consume(int index, MediaItem item) {
                // Find the cluster this item belongs to.
                long size = item.getSize();
                int i;
                for (i = 0; i < SIZE_LEVELS.length - 1; i++) {
                    if (size < SIZE_LEVELS[i + 1]) {
                        break;
                    }
                }

                ArrayList<Path> list = group[i];
                if (list == null) {
                    list = new ArrayList<Path>();
                    group[i] = list;
                }
                list.add(item.getPath());
            }
        });

        int count = 0;
        for (int i = 0; i < group.length; i++) {
            if (group[i] != null) {
                count++;
            }
        }

        mClusters = (ArrayList<Path>[]) new ArrayList[count];
        mNames = new String[count];
        mMinSizes = new long[count];

        Resources res = mContext.getResources();
        int k = 0;
        // Go through group in the reverse order, so the group with the largest
        // size will show first.
        for (int i = group.length - 1; i >= 0; i--) {
            if (group[i] == null) continue;

            mClusters[k] = group[i];
            if (i == 0) {
                mNames[k] = String.format(
                        res.getString(R.string.size_below), getSizeString(i + 1));
            } else if (i == group.length - 1) {
                mNames[k] = String.format(
                        res.getString(R.string.size_above), getSizeString(i));
            } else {
                String minSize = getSizeString(i);
                String maxSize = getSizeString(i + 1);
                mNames[k] = String.format(
                        res.getString(R.string.size_between), minSize, maxSize);
            }
            mMinSizes[k] = SIZE_LEVELS[i];
            k++;
        }
    }

    private String getSizeString(int index) {
        long bytes = SIZE_LEVELS[index];
        if (bytes >= GIGA_BYTES) {
            return (bytes / GIGA_BYTES) + "GB";
        } else {
            return (bytes / MEGA_BYTES) + "MB";
        }
    }

    @Override
    public int getNumberOfClusters() {
        return mClusters.length;
    }

    @Override
    public ArrayList<Path> getCluster(int index) {
        return mClusters[index];
    }

    @Override
    public String getClusterName(int index) {
        return mNames[index];
    }

    public long getMinSize(int index) {
        return mMinSizes[index];
    }
}
