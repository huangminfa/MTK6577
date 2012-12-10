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

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.common.Utils;
import  com.android.pqtuningtool.data.Path;

import android.content.Context;

public class ManageCacheDrawer extends IconDrawer {
    private final ResourceTexture mCheckedItem;
    private final ResourceTexture mUnCheckedItem;
    private final SelectionManager mSelectionManager;

    private final ResourceTexture mLocalAlbumIcon;
    private final StringTexture mCachingText;

    private final int mCachePinSize;
    private final int mCachePinMargin;

    public ManageCacheDrawer(Context context, SelectionManager selectionManager,
            int cachePinSize, int cachePinMargin) {
        super(context);
        mCheckedItem = new ResourceTexture(context, R.drawable.btn_make_offline_normal_on_holo_dark);
        mUnCheckedItem = new ResourceTexture(context, R.drawable.btn_make_offline_normal_off_holo_dark);
        mLocalAlbumIcon = new ResourceTexture(context, R.drawable.btn_make_offline_disabled_on_holo_dark);
        String cachingLabel = context.getString(R.string.caching_label);
        mCachingText = StringTexture.newInstance(cachingLabel, 12, 0xffffffff);
        mSelectionManager = selectionManager;
        mCachePinSize = cachePinSize;
        mCachePinMargin = cachePinMargin;
    }

    @Override
    public void prepareDrawing() {
    }

    private static boolean isLocal(int dataSourceType) {
        return dataSourceType != DATASOURCE_TYPE_PICASA;
    }

    @Override
    public void draw(GLCanvas canvas, Texture content, int width,
            int height, int rotation, Path path,
            int dataSourceType, int mediaType, /* boolean isPanorama */ int subType,
            int labelBackgroundHeight, boolean wantCache, boolean isCaching) {

        boolean selected = mSelectionManager.isItemSelected(path);
        boolean chooseToCache = wantCache ^ selected;
        boolean available = isLocal(dataSourceType) || chooseToCache;

        int x = -width / 2;
        int y = -height / 2;

        if (!available) {
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
            canvas.multiplyAlpha(0.6f);
        }

        drawWithRotation(canvas, content, x, y, width, height, rotation);

        if (!available) {
            canvas.restore();
        }

        if (((rotation / 90) & 0x01) == 1) {
            int temp = width;
            width = height;
            height = temp;
            x = -width / 2;
            y = -height / 2;
        }

        drawMediaTypeOverlay(canvas, mediaType, /* isPanorama */ subType, x, y, width, height);
        drawLabelBackground(canvas, width, height, labelBackgroundHeight);
        drawIcon(canvas, width, height, dataSourceType);
        drawCachingPin(canvas, path, dataSourceType, isCaching, chooseToCache,
                width, height);

        if (mSelectionManager.isPressedPath(path)) {
            drawPressedFrame(canvas, x, y, width, height);
        }
    }

    private void drawCachingPin(GLCanvas canvas, Path path, int dataSourceType,
            boolean isCaching, boolean chooseToCache, int width, int height) {

        ResourceTexture icon = null;
        if (isLocal(dataSourceType)) {
            icon = mLocalAlbumIcon;
        } else if (chooseToCache) {
            icon = mCheckedItem;
        } else {
            icon = mUnCheckedItem;
        }

        int w = mCachePinSize;
        int h = mCachePinSize;
        int right = (width + 1) / 2;
        int bottom = (height + 1) / 2;
        int x = right - w - mCachePinMargin;
        int y = bottom - h - mCachePinMargin;

        icon.draw(canvas, x, y, w, h);

        if (isCaching) {
            int textWidth = mCachingText.getWidth();
            int textHeight = mCachingText.getHeight();
            // Align the center of the text to the center of the pin icon
            x = right - mCachePinMargin - (textWidth + mCachePinSize) / 2;
            y = bottom - textHeight;
            mCachingText.draw(canvas, x, y);
        }
    }

    @Override
    public void drawFocus(GLCanvas canvas, int width, int height) {
    }
}
