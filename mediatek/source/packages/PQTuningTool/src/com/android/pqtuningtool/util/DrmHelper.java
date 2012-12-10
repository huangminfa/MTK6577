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
package  com.android.pqtuningtool.util;

import android.os.Bundle;
import android.drm.DrmStore;
import android.drm.DrmManagerClient;
import android.provider.MediaStore.Files.FileColumns;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.content.ContentResolver;
import android.content.Context;
import com.mediatek.dcfdecoder.DcfDecoder;
import android.view.Display;
import android.app.Activity;
import android.util.DisplayMetrics;
import  com.android.pqtuningtool.data.MediaItem;
import  com.android.pqtuningtool.data.LocalMediaItem;
import  com.android.pqtuningtool.data.LocalImage;
import  com.android.pqtuningtool.data.LocalVideo;

public class DrmHelper {

    private static final String TAG = "DrmHelper";

    public static final float DRM_MICRO_THUMB_IN_DIP = 200f/1.5f;
    public static final int DRM_MICRO_THUMB_DEFAULT_BG = 0x66666666;

    public static final int INVALID_DRM_LEVEL = -1;
    public static final int NO_DRM_INCLUSION = 0;
    //include fl type drm media
    public static final int INCLUDE_FL_DRM_MEDIA = (1 << 1);
    //include cd type drm media
    public static final int INCLUDE_CD_DRM_MEDIA = (1 << 2);
    //include sd type drm media
    public static final int INCLUDE_SD_DRM_MEDIA = (1 << 3);
    //include fldcf type drm media
    public static final int INCLUDE_FLDCF_DRM_MEDIA = (1 << 4);
    //include all types of drm media
    public static final int ALL_DRM_MEDIA = INCLUDE_FL_DRM_MEDIA |
                                            INCLUDE_CD_DRM_MEDIA |
                                            INCLUDE_SD_DRM_MEDIA |
                                            INCLUDE_FLDCF_DRM_MEDIA;

    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();

    private static DrmManagerClient mDrmManagerClient = null;
    //default display
    private static Display mDefaultDisplay = null;

    public static int getDrmMicroThumbDim(Activity activity) {
        if (null == mDefaultDisplay) {
            mDefaultDisplay = activity.getWindowManager().getDefaultDisplay(); 
        }
        DisplayMetrics metrics = new DisplayMetrics();
        mDefaultDisplay.getMetrics(metrics);
        return (int)(DRM_MICRO_THUMB_IN_DIP * metrics.density);
    }

    public static int getDrmInclusionFromData(Bundle data) {
        int drmInclusion = NO_DRM_INCLUSION;
        if (null == data) {
            return drmInclusion;
        }
        int drmLevel = data.getInt(DrmStore.DrmExtra.EXTRA_DRM_LEVEL, INVALID_DRM_LEVEL);
        if (drmLevel != INVALID_DRM_LEVEL) {
            if (DrmStore.DrmExtra.DRM_LEVEL_FL == drmLevel) {
                drmInclusion |= INCLUDE_FL_DRM_MEDIA;
            } else if (DrmStore.DrmExtra.DRM_LEVEL_SD == drmLevel) {
                drmInclusion |= INCLUDE_SD_DRM_MEDIA;
            } else if (DrmStore.DrmExtra.DRM_LEVEL_ALL == drmLevel) {
                drmInclusion |= ALL_DRM_MEDIA;
            }
        }
        return drmInclusion;
    }

    public static String getDrmWhereClause(int drmInclusion) {
        if (ALL_DRM_MEDIA == drmInclusion) {
            return null;
        }
        
        String noDrmClause = FileColumns.IS_DRM + "=0 OR " + FileColumns.IS_DRM + " IS NULL";
        if (NO_DRM_INCLUSION == drmInclusion) {
            return noDrmClause;
        }

        String whereClause = null;
        if ((drmInclusion & INCLUDE_FL_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_FL :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_FL;
        }
        if ((drmInclusion & INCLUDE_CD_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_CD :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_CD;
        }
        if ((drmInclusion & INCLUDE_SD_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_SD :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_SD;
        }
        if ((drmInclusion & INCLUDE_FLDCF_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_FLDCF :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_FLDCF;
        }
        whereClause = (null != whereClause) ?
                      "(" + noDrmClause + ") OR (" + FileColumns.IS_DRM + "=1 AND (" + 
                      whereClause + "))" : noDrmClause;
        return whereClause;
    }

    public static Bitmap forceDecodeDrmUri(ContentResolver cr, Uri drmUri, 
                               BitmapFactory.Options options, boolean consume) {
        if (! mIsDrmSupported) {
            Log.w(TAG,"Decode Drm image when Drm is not supported.");
            return null;
        }

        if (null == options) {
            options = new BitmapFactory.Options();
        }

        if (options.mCancel) {
            return null;
        }

        return DcfDecoder.forceDecodeUri(cr, drmUri, options, consume);
    }

    public static int checkRightsStatus(Context context, String path, int action) {
        if (null == mDrmManagerClient) {
            mDrmManagerClient = new DrmManagerClient(context);
        }
        if (null == path) {
            Log.e(TAG,"checkRightsStatus:got null file path");
        }
        return mDrmManagerClient.checkRightsStatus(path, action);
    }

    public static DrmManagerClient getDrmManagerClient(Context context) {
        if (null == mDrmManagerClient) {
            mDrmManagerClient = new DrmManagerClient(context);
        }
        return mDrmManagerClient;
    }

    private static Bitmap createBitmap(int width, int height, Bitmap.Config config,
                                       int bgColor) {
        if (width <= 0 || height <= 0 || null == config) {
            Log.e(TAG,"createBitmap:invalid Bitmap argumentation");
            return null;
        }

        //create Bitmap to hold thumbnail
        Bitmap canvasBitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(canvasBitmap);
        //draw Background color to avoid merging in to black background
        canvas.drawColor(bgColor);

        return canvasBitmap;
    }

    public static Bitmap createDefaultDrmMicroThumb(Activity activity) {
        int thumbDim = getDrmMicroThumbDim(activity);//MICROTHUMBNAIL_TARGET_SIZE
        return createBitmap(thumbDim, thumbDim, Bitmap.Config.ARGB_8888,
                            DRM_MICRO_THUMB_DEFAULT_BG);
    }

    public static boolean drawOverlayToBottomRight(Context context,Bitmap target, 
                                                   boolean hasDrmRights) {
        if (null == context || null == target || !target.isMutable()) {
            return false;
        }
        //init drm lock icons if needed
        initDrmLockIcons(context);
        Bitmap overlay = hasDrmRights ? mDrmGreenLockOverlay : mDrmRedLockOverlay;
        return drawOverlayToBottomRight(overlay, target);
    }

    // We put mTempSrcRect and mTempDstRect as instance variables to
    // reduce the memory allocation overhead because drawOverlayToBottomRight()
    // may be called a lot.
    private static final Rect mTempSrcRect = new Rect();
    private static final Rect mTempDstRect = new Rect();
    private static boolean drawOverlayToBottomRight(Bitmap overlay, Bitmap b) {
        if (null == overlay || null == b) {
            Log.e(TAG,"drawOverlayToBottomRight:overlay or Bitmap null!");
            return false;
        }
        if (! b.isMutable()) {
            Log.e(TAG,"drawOverlayToBottomRight:Bitmap is NOT mutable!");
            return false;
        }

        mTempSrcRect.set(0, 0, overlay.getWidth(), overlay.getHeight());
        mTempDstRect.set(b.getWidth() - overlay.getWidth(), 
                         b.getHeight() - overlay.getHeight(),
                         b.getWidth(), b.getHeight());

        Canvas tempCanvas = new Canvas(b);
        tempCanvas.drawBitmap(overlay, mTempSrcRect, mTempDstRect, null);

        return true;
    }

    //this function guanrentee the returned bitmap is mutable.
    //Note: it may recycle the in passed Bitmap.
    public static Bitmap ensureBitmapMutable(Bitmap b) {
        if (null == b) return null;
        if (b.isMutable()) {
            return b;
        }
        //now we recreate a Bitmap with same content
        Bitmap temp = Bitmap.createBitmap(b.getHeight(), b.getWidth(), 
                                              Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(temp);
        tempCanvas.drawColor(DRM_MICRO_THUMB_DEFAULT_BG);
        tempCanvas.drawBitmap(b,new Matrix(),null);
        b.recycle();
        return temp;
    }

    private static boolean isDrmLockIconInited = false;
    private static Bitmap mDrmRedLockOverlay = null;
    private static Bitmap mDrmGreenLockOverlay = null;

    private static void initDrmLockIcons(Context context) {
        if (!isDrmLockIconInited) {
            mDrmRedLockOverlay = getResBitmap(context, 
                                     com.mediatek.internal.R.drawable.drm_red_lock);
            mDrmGreenLockOverlay = getResBitmap(context, 
                                     com.mediatek.internal.R.drawable.drm_green_lock);
            isDrmLockIconInited = true;
        }
    }

    private static Bitmap getResBitmap(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(
                context.getResources(), resId, options);
    }

}
