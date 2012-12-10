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

package  com.android.pqtuningtool.ui;

import android.graphics.Rect;

import  com.android.pqtuningtool.app.GalleryActivity;
import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.MediaSet;
import  com.android.pqtuningtool.ui.PositionRepository.Position;

import java.util.Random;

public class AlbumSetView extends SlotView {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumSetView";
    private static final int CACHE_SIZE = 32;
    private static final float PHOTO_DISTANCE = 35f;

    private int mVisibleStart;
    private int mVisibleEnd;

    private final Random mRandom = new Random();
    private final long mSeed = mRandom.nextLong();

    private AlbumSetSlidingWindow mDataWindow;
    private final GalleryActivity mActivity;
    private final LabelSpec mLabelSpec;

    private SelectionDrawer mSelectionDrawer;

    public static interface Model {
        public MediaItem[] getCoverItems(int index);
        public MediaSet getMediaSet(int index);
        public int size();
        public void setActiveWindow(int start, int end);
        public void setModelListener(ModelListener listener);
    }

    public static interface ModelListener {
        public void onWindowContentChanged(int index);
        public void onSizeChanged(int size);
    }

    public static class AlbumSetItem {
        public DisplayItem[] covers;
        public DisplayItem labelItem;
        public long setDataVersion;
    }

    public static class LabelSpec {
        public int labelBackgroundHeight;
        public int titleOffset;
        public int countOffset;
        public int titleFontSize;
        public int countFontSize;
        public int leftMargin;
        public int iconSize;
    }

    public AlbumSetView(GalleryActivity activity, SelectionDrawer drawer,
            SlotView.Spec slotViewSpec, LabelSpec labelSpec) {
        super(activity.getAndroidContext());
        mActivity = activity;
        setSelectionDrawer(drawer);
        setSlotSpec(slotViewSpec);
        mLabelSpec = labelSpec;
    }

    public void setSelectionDrawer(SelectionDrawer drawer) {
        mSelectionDrawer = drawer;
        if (mDataWindow != null) {
            mDataWindow.setSelectionDrawer(drawer);
        }
    }

    public void setModel(AlbumSetView.Model model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            setSlotCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new AlbumSetSlidingWindow(mActivity, mLabelSpec,
                    mSelectionDrawer, model, CACHE_SIZE);
            mDataWindow.setListener(new MyCacheListener());
            setSlotCount(mDataWindow.size());
            updateVisibleRange(getVisibleStart(), getVisibleEnd());
        }
    }

    private void putSlotContent(int slotIndex, AlbumSetItem entry) {
        // Get displayItems from mItemsetMap or create them from MediaSet.
        Utils.assertTrue(entry != null);
        Rect rect = getSlotRect(slotIndex);

        DisplayItem[] items = entry.covers;
        mRandom.setSeed(slotIndex ^ mSeed);

        int x = (rect.left + rect.right) / 2;
        int y = (rect.top + rect.bottom) / 2;

        Position basePosition = new Position(x, y, 0);

        // Put the cover items in reverse order, so that the first item is on
        // top of the rest.
        Position position = new Position(x, y, 0f);
        putDisplayItem(position, position, entry.labelItem);

        for (int i = 0, n = items.length; i < n; ++i) {
            DisplayItem item = items[i];
            float dx = 0;
            float dy = 0;
            float dz = 0f;
            float theta = 0;
            if (i != 0) {
                dz = i * PHOTO_DISTANCE;
            }
            position = new Position(x + dx, y + dy, dz);
            position.theta = theta;
            putDisplayItem(position, basePosition, item);
        }

    }

    private void freeSlotContent(int index, AlbumSetItem entry) {
        if (entry == null) return;
        for (DisplayItem item : entry.covers) {
            removeDisplayItem(item);
        }
        removeDisplayItem(entry.labelItem);
    }

    public int size() {
        return mDataWindow.size();
    }

    @Override
    public void onLayoutChanged(int width, int height) {
        updateVisibleRange(0, 0);
        updateVisibleRange(getVisibleStart(), getVisibleEnd());
    }

    @Override
    public void onScrollPositionChanged(int position) {
        super.onScrollPositionChanged(position);
        updateVisibleRange(getVisibleStart(), getVisibleEnd());
    }

    private void updateVisibleRange(int start, int end) {
        if (start == mVisibleStart && end == mVisibleEnd) {
            // we need to set the mDataWindow active range in any case.
            mDataWindow.setActiveWindow(start, end);
            return;
        }
        if (start >= mVisibleEnd || mVisibleStart >= end) {
            for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
                freeSlotContent(i, mDataWindow.get(i));
            }
            mDataWindow.setActiveWindow(start, end);
            for (int i = start; i < end; ++i) {
                putSlotContent(i, mDataWindow.get(i));
            }
        } else {
            for (int i = mVisibleStart; i < start; ++i) {
                freeSlotContent(i, mDataWindow.get(i));
            }
            for (int i = end, n = mVisibleEnd; i < n; ++i) {
                freeSlotContent(i, mDataWindow.get(i));
            }
            mDataWindow.setActiveWindow(start, end);
            for (int i = start, n = mVisibleStart; i < n; ++i) {
                putSlotContent(i, mDataWindow.get(i));
            }
            for (int i = mVisibleEnd; i < end; ++i) {
                putSlotContent(i, mDataWindow.get(i));
            }
        }
        mVisibleStart = start;
        mVisibleEnd = end;

        invalidate();
    }

    @Override
    protected void render(GLCanvas canvas) {
        mSelectionDrawer.prepareDrawing();
        super.render(canvas);
    }

    private class MyCacheListener implements AlbumSetSlidingWindow.Listener {

        public void onSizeChanged(int size) {
            if (setSlotCount(size)) {
                // If the layout parameters are changed, we need reput all items.
                // We keep the visible range at the same center but with size 0.
                // So that we can:
                //     1.) flush all visible items
                //     2.) keep the cached data
                int center = (getVisibleStart() + getVisibleEnd()) / 2;
                updateVisibleRange(center, center);
            }
            updateVisibleRange(getVisibleStart(), getVisibleEnd());
            invalidate();
        }

        public void onWindowContentChanged(int slot, AlbumSetItem old, AlbumSetItem update) {
            freeSlotContent(slot, old);
            putSlotContent(slot, update);
            invalidate();
        }

        public void onContentInvalidated() {
            invalidate();
        }
    }

    public void pause() {
        for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
            freeSlotContent(i, mDataWindow.get(i));
        }
        mDataWindow.pause();
    }

    public void resume() {
        mDataWindow.resume();
        for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
            putSlotContent(i, mDataWindow.get(i));
        }
    }
}
