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
import  com.android.pqtuningtool.data.MediaObject;
import  com.android.pqtuningtool.util.MediatekFeature;

import android.content.Context;

public abstract class IconDrawer extends SelectionDrawer {
    private static final String TAG = "IconDrawer";
    private static final int LABEL_BACKGROUND_COLOR = 0x99000000;  // 60% black

    //added to support Mediatek features
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();

    private final ResourceTexture mLocalSetIcon;
    private final ResourceTexture mCameraIcon;
    private final ResourceTexture mPicasaIcon;
    private final ResourceTexture mMtpIcon;
    private final NinePatchTexture mFramePressed;
    private final NinePatchTexture mFrameSelected;
    private final NinePatchTexture mDarkStrip;
    private final NinePatchTexture mPanoramaBorder;
    private final Texture mVideoOverlay;
    private final Texture mVideoPlayIcon;
    //added for mpo feature
    private Texture mMpoOverlay;
    //added for drm feature
    private Texture mDrmRedLockOverlay;
    private Texture mDrmGreenLockOverlay;
    //add mpo-drm feature end
    private final int mIconSize;

    public static class IconDimension {
        int x;
        int y;
        int width;
        int height;
    }

    public IconDrawer(Context context) {
        mLocalSetIcon = new ResourceTexture(context, R.drawable.frame_overlay_gallery_folder);
        mCameraIcon = new ResourceTexture(context, R.drawable.frame_overlay_gallery_camera);
        mPicasaIcon = new ResourceTexture(context, R.drawable.frame_overlay_gallery_picasa);
        mMtpIcon = new ResourceTexture(context, R.drawable.frame_overlay_gallery_ptp);
        mVideoOverlay = new ResourceTexture(context, R.drawable.ic_video_thumb);
        mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_play);
        mPanoramaBorder = new NinePatchTexture(context, R.drawable.ic_pan_thumb);
        mFramePressed = new NinePatchTexture(context, R.drawable.grid_pressed);
        mFrameSelected = new NinePatchTexture(context, R.drawable.grid_selected);
        mDarkStrip = new NinePatchTexture(context, R.drawable.dark_strip);
        mIconSize = context.getResources().getDimensionPixelSize(
                R.dimen.albumset_icon_size);
        //icons for Mediatek add-on features
        if (mIsMpoSupported) {
            mMpoOverlay = new ResourceTexture(context, R.drawable.icon_mav_overlay);
        }
        if (mIsDrmSupported) {
            mDrmRedLockOverlay = new ResourceTexture(context, 
                                     com.mediatek.internal.R.drawable.drm_red_lock);
            mDrmGreenLockOverlay = new ResourceTexture(context, 
                                     com.mediatek.internal.R.drawable.drm_green_lock);
        }
    }

    @Override
    public void prepareDrawing() {
    }

    protected IconDimension drawIcon(GLCanvas canvas, int width, int height,
            int dataSourceType) {
        ResourceTexture icon = getIcon(dataSourceType);

        if (icon != null) {
            IconDimension id = getIconDimension(icon, width, height);
            icon.draw(canvas, id.x, id.y, id.width, id.height);
            return id;
        }
        return null;
    }

    protected ResourceTexture getIcon(int dataSourceType) {
        ResourceTexture icon = null;
        switch (dataSourceType) {
            case DATASOURCE_TYPE_LOCAL:
                icon = mLocalSetIcon;
                break;
            case DATASOURCE_TYPE_PICASA:
                icon = mPicasaIcon;
                break;
            case DATASOURCE_TYPE_CAMERA:
                icon = mCameraIcon;
                break;
            case DATASOURCE_TYPE_MTP:
                icon = mMtpIcon;
                break;
            default:
                break;
        }

        return icon;
    }

    protected IconDimension getIconDimension(ResourceTexture icon, int width,
            int height) {
        IconDimension id = new IconDimension();
        float scale = (float) mIconSize / icon.getWidth();
        id.width = Math.round(scale * icon.getWidth());
        id.height = Math.round(scale * icon.getHeight());
        id.x = -width / 2;
        id.y = (height + 1) / 2 - id.height;
        return id;
    }

    protected void drawMediaTypeOverlay(GLCanvas canvas, int mediaType,
            /* boolean isPanorama */ int subType, int x, int y, int width, int height) {
        if (mediaType == MediaObject.MEDIA_TYPE_VIDEO) {
            drawVideoOverlay(canvas, x, y, width, height);
        }
        //if (isPanorama) {
        if ((subType & MediaObject.SUBTYPE_PANORAMA) != 0) {
            drawPanoramaBorder(canvas, x, y, width, height);
        } else if (mIsMpoSupported && (subType & MediaObject.SUBTYPE_MAV) != 0) {
            drawMpoOverlay(canvas, x, y, width, height);
        }
        //draw drm icon if needed
        if (mIsDrmSupported) {
            if ((subType & MediaObject.SUBTYPE_DRM_NO_RIGHT) != 0) {
                drawDrmRedLock(canvas, x, y, width, height);
            } else if ((subType & MediaObject.SUBTYPE_DRM_HAS_RIGHT) != 0) {
                drawDrmGreenLock(canvas, x, y, width, height);
            }
        }
    }

    protected void drawVideoOverlay(GLCanvas canvas, int x, int y,
            int width, int height) {
        // Scale the video overlay to the height of the thumbnail and put it
        // on the left side.
        float scale = (float) height / mVideoOverlay.getHeight();
        int w = Math.round(scale * mVideoOverlay.getWidth());
        int h = Math.round(scale * mVideoOverlay.getHeight());
        mVideoOverlay.draw(canvas, x, y, w, h);

        int side = Math.min(width, height) / 6;
        mVideoPlayIcon.draw(canvas, -side / 2, -side / 2, side, side);
    }
    
    protected void drawMpoOverlay(GLCanvas canvas, int x, int y,
            int width, int height) {
        int side = Math.min(width, height) / 5;
        // TODO retain aspect ratio of the MPO overlay icon
        mMpoOverlay.draw(canvas, -side / 2, -side / 2, side, side);
    }

    protected void drawDrmRedLock(GLCanvas canvas, int x, int y,
            int width, int height) {
        int widthPadding = 2;
        int iconWidth = mDrmRedLockOverlay.getWidth();
        int iconHeight = mDrmRedLockOverlay.getHeight();
        mDrmRedLockOverlay.draw(canvas, width / 2 - iconWidth - widthPadding, 
              height / 2 - iconHeight, iconWidth, iconHeight);
    }

    protected void drawDrmGreenLock(GLCanvas canvas, int x, int y,
            int width, int height) {
        int widthPadding = 2;
        int iconWidth = mDrmGreenLockOverlay.getWidth();
        int iconHeight = mDrmGreenLockOverlay.getHeight();
        mDrmGreenLockOverlay.draw(canvas, width / 2 - iconWidth - widthPadding, 
              height / 2 - iconHeight, iconWidth, iconHeight);
    }

    protected void drawPanoramaBorder(GLCanvas canvas, int x, int y,
            int width, int height) {
        float scale = (float) width / mPanoramaBorder.getWidth();
        int w = Math.round(scale * mPanoramaBorder.getWidth());
        int h = Math.round(scale * mPanoramaBorder.getHeight());
        // draw at the top
        mPanoramaBorder.draw(canvas, x, y, w, h);
        // draw at the bottom
        mPanoramaBorder.draw(canvas, x, y + width - h, w, h);
    }

    protected void drawLabelBackground(GLCanvas canvas, int width, int height,
            int drawLabelBackground) {
        int x = -width / 2;
        int y = (height + 1) / 2 - drawLabelBackground;
        drawFrame(canvas, mDarkStrip, x, y, width, drawLabelBackground);
    }

    protected void drawPressedFrame(GLCanvas canvas, int x, int y, int width,
            int height) {
        drawFrame(canvas, mFramePressed, x, y, width, height);
    }

    protected void drawSelectedFrame(GLCanvas canvas, int x, int y, int width,
            int height) {
        drawFrame(canvas, mFrameSelected, x, y, width, height);
    }

    @Override
    public void drawFocus(GLCanvas canvas, int width, int height) {
    }
}
