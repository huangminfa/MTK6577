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
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.ui.PositionRepository.Position;

public class AlbumView extends SlotView {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumView";
    private static final int CACHE_SIZE = 64;

    private int mVisibleStart = 0;
    private int mVisibleEnd = 0;

    private AlbumSlidingWindow mDataWindow;
    private final GalleryActivity mActivity;
    private SelectionDrawer mSelectionDrawer;
    private int mCacheThumbSize;

    private boolean mIsActive = false;

    public static interface Model {
        public int size();
        public MediaItem get(int index);
        public void setActiveWindow(int start, int end);
        public void setModelListener(ModelListener listener);
    }

    public static interface ModelListener {
        public void onWindowContentChanged(int index);
        public void onSizeChanged(int size);
    }

    public AlbumView(GalleryActivity activity, SlotView.Spec spec,
            int cacheThumbSize) {
        super(activity.getAndroidContext());
        mCacheThumbSize = cacheThumbSize;
        setSlotSpec(spec);
        mActivity = activity;
    }

    public void setSelectionDrawer(SelectionDrawer drawer) {
        mSelectionDrawer = drawer;
        if (mDataWindow != null) mDataWindow.setSelectionDrawer(drawer);
    }

    public void setModel(Model model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            setSlotCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new AlbumSlidingWindow(
                    mActivity, model, CACHE_SIZE,
                    mCacheThumbSize);
            mDataWindow.setSelectionDrawer(mSelectionDrawer);
            mDataWindow.setListener(new MyDataModelListener());
            setSlotCount(model.size());
            updateVisibleRange(getVisibleStart(), getVisibleEnd());
        }
    }

    public void setFocusIndex(int slotIndex) {
        if (mDataWindow != null) {
            mDataWindow.setFocusIndex(slotIndex);
        }
    }

    private void putSlotContent(int slotIndex, DisplayItem item) {
        Rect rect = getSlotRect(slotIndex);
        Position position = new Position(
                (rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2, 0);
        putDisplayItem(position, position, item);
    }

    private void updateVisibleRange(int start, int end) {
        if (start == mVisibleStart && end == mVisibleEnd) {
            // we need to set the mDataWindow active range in any case.
            mDataWindow.setActiveWindow(start, end);
            return;
        }

        if (!mIsActive) {
            mVisibleStart = start;
            mVisibleEnd = end;
            mDataWindow.setActiveWindow(start, end);
            return;
        }

        if (start >= mVisibleEnd || mVisibleStart >= end) {
            for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
                DisplayItem item = mDataWindow.get(i);
                if (item != null) removeDisplayItem(item);
            }
            mDataWindow.setActiveWindow(start, end);
            for (int i = start; i < end; ++i) {
                putSlotContent(i, mDataWindow.get(i));
            }
        } else {
            for (int i = mVisibleStart; i < start; ++i) {
                DisplayItem item = mDataWindow.get(i);
                if (item != null) removeDisplayItem(item);
            }
            for (int i = end, n = mVisibleEnd; i < n; ++i) {
                DisplayItem item = mDataWindow.get(i);
                if (item != null) removeDisplayItem(item);
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
    }

    @Override
    protected void onLayoutChanged(int width, int height) {
        // Reput all the items
        updateVisibleRange(0, 0);
        updateVisibleRange(getVisibleStart(), getVisibleEnd());
    }

    @Override
    protected void onScrollPositionChanged(int position) {
        super.onScrollPositionChanged(position);
        updateVisibleRange(getVisibleStart(), getVisibleEnd());
    }

    @Override
    protected void render(GLCanvas canvas) {
        mSelectionDrawer.prepareDrawing();
        super.render(canvas);
    }

    private class MyDataModelListener implements AlbumSlidingWindow.Listener {

        public void onContentInvalidated() {
            invalidate();
        }

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

        public void onWindowContentChanged(
                int slotIndex, DisplayItem old, DisplayItem update) {
            removeDisplayItem(old);
            putSlotContent(slotIndex, update);
        }
    }

    public void resume() {
        mIsActive = true;
        mDataWindow.resume();
        for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
            putSlotContent(i, mDataWindow.get(i));
        }
    }

    public void pause() {
        mIsActive = false;
        for (int i = mVisibleStart, n = mVisibleEnd; i < n; ++i) {
            removeDisplayItem(mDataWindow.get(i));
        }
        mDataWindow.pause();
    }
}
