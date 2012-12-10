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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;

import  com.android.pqtuningtool.common.Utils;

import  com.android.pqtuningtool.util.MediatekFeature;

public class TileImageViewAdapter implements TileImageView.Model {
    private static final String TAG = "TileImageViewAdapter";
    protected BitmapRegionDecoder mRegionDecoder;
    protected int mImageWidth;
    protected int mImageHeight;
    protected Bitmap mBackupImage;
    protected int mLevelCount;
    protected boolean mFailedToLoad;

    //added for DRM
    protected boolean mShowDrmMicroThumb;

    private final Rect mIntersectRect = new Rect();
    private final Rect mRegionRect = new Rect();

    public TileImageViewAdapter() {
    }

    public TileImageViewAdapter(Bitmap backup, BitmapRegionDecoder regionDecoder) {
        mBackupImage = Utils.checkNotNull(backup);
        mRegionDecoder = regionDecoder;
        mImageWidth = regionDecoder.getWidth();
        mImageHeight = regionDecoder.getHeight();
        mLevelCount = calculateLevelCount();
    }

    public synchronized void clear() {
        mBackupImage = null;
        mImageWidth = 0;
        mImageHeight = 0;
        mLevelCount = 0;
        mRegionDecoder = null;
        mFailedToLoad = false;
    }

    public synchronized void setBackupImage(Bitmap backup, int width, int height) {
        mBackupImage = Utils.checkNotNull(backup);
        mImageWidth = width;
        mImageHeight = height;
        mRegionDecoder = null;
        mLevelCount = 0;
        mFailedToLoad = false;
    }

    public synchronized void setRegionDecoder(BitmapRegionDecoder decoder) {
        mRegionDecoder = Utils.checkNotNull(decoder);
        mImageWidth = decoder.getWidth();
        mImageHeight = decoder.getHeight();
        mLevelCount = calculateLevelCount();
        mFailedToLoad = false;
    }

    private int calculateLevelCount() {
        return Math.max(0, Utils.ceilLog2(
                (float) mImageWidth / mBackupImage.getWidth()));
    }

    @Override
    public synchronized Bitmap getTile(int level, int x, int y, int length) {
        if (mRegionDecoder == null) return null;

        Rect region = mRegionRect;
        Rect intersectRect = mIntersectRect;
        region.set(x, y, x + (length << level), y + (length << level));
        intersectRect.set(0, 0, mImageWidth, mImageHeight);

        // Get the intersected rect of the requested region and the image.
        Utils.assertTrue(intersectRect.intersect(region));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inPreferQualityOverSpeed = true;
        options.inSampleSize =  (1 << level);

        //for picture quality enhancement
        if (MediatekFeature.isPictureQualityEnhanceSupported()) {
            options.inPostProc = true;
        }

        Bitmap bitmap;

        // In CropImage, we may call the decodeRegion() concurrently.
        synchronized (mRegionDecoder) {
            bitmap = mRegionDecoder.decodeRegion(intersectRect, options);
        }

        // The returned region may not match with the targetLength.
        // If so, we fill black pixels on it.
        if (intersectRect.equals(region)) return bitmap;

        if (bitmap == null) {
            Log.w(TAG, "fail in decoding region");
            return null;
        }

        Bitmap tile = Bitmap.createBitmap(length, length, Config.ARGB_8888);
        Canvas canvas = new Canvas(tile);
        canvas.drawBitmap(bitmap,
                (intersectRect.left - region.left) >> level,
                (intersectRect.top - region.top) >> level, null);
        bitmap.recycle();
        return tile;
    }

    @Override
    public Bitmap getBackupImage() {
        return mBackupImage;
    }

    @Override
    public int getImageHeight() {
        return mImageHeight;
    }

    @Override
    public int getImageWidth() {
        return mImageWidth;
    }

    @Override
    public int getLevelCount() {
        return mLevelCount;
    }

    public void setFailedToLoad() {
        mFailedToLoad = true;
    }

    @Override
    public boolean isFailedToLoad() {
        return mFailedToLoad;
    }

    public void setShowDrmMicroThumb(boolean showDrmMicroThumb) {
        mShowDrmMicroThumb = showDrmMicroThumb;
    }

    public boolean getShowDrmMicroThumb() {
        return mShowDrmMicroThumb;
    }
}
