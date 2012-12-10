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
package com.android.gallery3d.ui;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.util.MediatekFeature;

import android.content.Context;

public abstract class IconDrawer extends SelectionDrawer {
    private static final String TAG = "IconDrawer";
    private static final int LABEL_BACKGROUND_COLOR = 0x99000000;  // 60% black

    //added to support Mediatek features
    private static final boolean mIsDrmSupported = 
                                          MediatekFeature.isDrmSupported();
    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

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
    // add for stereo3D display
    private ResourceTexture mStereoFolderIcon;
    private Texture mStereoOverlay;
    private Texture mStereoPanOverlay;

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
        if (mIsStereoDisplaySupported) {
            mStereoOverlay = new ResourceTexture(context, R.drawable.ic_stereo_overlay);
            mStereoPanOverlay = new ResourceTexture(context, R.drawable.ic_stereo_pan_overlay);
            mStereoFolderIcon = new ResourceTexture(context, R.drawable.frame_overlay_gallery_stereo);
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
            //added to draw stereo folder icon
            //actually the it should be a virtual folder that does 
            //not exist in the sdcard
            case DATASOURCE_VIRTUAL_STEREO:
                icon = mStereoFolderIcon;
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
        } else if (mIsMpoSupported && (subType & MediaObject.SUBTYPE_MPO_MAV) != 0) {
            drawMpoOverlay(canvas, x, y, width, height);
        }
        //draw panorama overlay
        if (mIsStereoDisplaySupported && (subType & MediaObject.SUBTYPE_MPO_3D_PAN) != 0) {
            drawStereoPanOverlay(canvas, x, y, width, height);
        }
        //draw drm icon if needed
        if (mIsDrmSupported) {
            if ((subType & MediaObject.SUBTYPE_DRM_NO_RIGHT) != 0) {
                drawDrmRedLock(canvas, x, y, width, height);
            } else if ((subType & MediaObject.SUBTYPE_DRM_HAS_RIGHT) != 0) {
                drawDrmGreenLock(canvas, x, y, width, height);
            }
        }
        // draw stereo3D overlay icon if needed
        if (mIsStereoDisplaySupported) {
            drawStereoDisplayOverlay(canvas, x, y, width, height, subType);
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

    protected void drawStereoPanOverlay(GLCanvas canvas, int x, int y,
            int width, int height) {
        int side = Math.min(width, height) / 5;
        // TODO retain aspect ratio of the MPO overlay icon
        mStereoPanOverlay.draw(canvas, -side / 2, -side / 2, side, side);
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

    protected void drawStereoDisplayOverlay(GLCanvas canvas, int x, int y,
            int width, int height, int subType) {
        int side = Math.min(width, height) / 5;
        int widthPadding = 2;
        int iconWidth;
        int iconHeight;
        if ((subType & MediaObject.SUBTYPE_MPO_3D) != 0 ||
            (subType & MediaObject.SUBTYPE_MPO_3D_PAN) != 0 ||
            (subType & MediaObject.SUBTYPE_STEREO_JPS) != 0 ||
            (subType & MediaObject.SUBTYPE_STEREO_VIDEO) != 0) {
            //if stereo media, show 3D icon
            iconWidth = mStereoOverlay.getWidth();
            iconHeight = mStereoOverlay.getHeight();
            mStereoOverlay.draw(canvas, -width / 2 + widthPadding, 
                                height / 2 - iconHeight, iconWidth, iconHeight);
        }
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
