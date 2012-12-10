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
package com.android.gallery3d.util;

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
import android.content.ContentValues;
import com.mediatek.dcfdecoder.DcfDecoder;
import android.view.Display;
import android.app.Activity;
import android.util.DisplayMetrics;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalVideo;

import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class DrmHelper {

    private static final String TAG = "DrmHelper";

    public static final float DRM_MICRO_THUMB_IN_DIP = 200f/1.5f;
    // this value should be equal to the default bg color in AlbumSetSlidingWindow & AlbumSlidingWindow
    public static final int DRM_MICRO_THUMB_DEFAULT_BG = 0xFF444444;
    public static final int DRM_MICRO_THUMB_BLACK_BG = 0x00000000;

    public static final int INVALID_DRM_LEVEL = -1;
    public static final int NO_DRM_INCLUSION = 0;

    //DRM inclusion definitions are shifted to MediatekFeature class

    //drm inclusion extra signature in Bundle
    public static final String DRM_INCLUSION = "GalleryDrmInclusion";

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
                drmInclusion |= MediatekFeature.INCLUDE_FL_DRM_MEDIA;
            } else if (DrmStore.DrmExtra.DRM_LEVEL_SD == drmLevel) {
                drmInclusion |= MediatekFeature.INCLUDE_SD_DRM_MEDIA;
            } else if (DrmStore.DrmExtra.DRM_LEVEL_ALL == drmLevel) {
                drmInclusion |= MediatekFeature.ALL_DRM_MEDIA;
            }
        }
        return drmInclusion;
    }

    public static String getDrmWhereClause(int drmInclusion) {
        drmInclusion = drmInclusion & MediatekFeature.ALL_DRM_MEDIA;
        if (MediatekFeature.ALL_DRM_MEDIA == drmInclusion) {
            return null;
        }
        
        String noDrmClause = FileColumns.IS_DRM + "=0 OR " + FileColumns.IS_DRM + " IS NULL";
        if (NO_DRM_INCLUSION == drmInclusion) {
            return noDrmClause;
        }

        String whereClause = null;
        if ((drmInclusion & MediatekFeature.INCLUDE_FL_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_FL :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_FL;
        }
        if ((drmInclusion & MediatekFeature.INCLUDE_CD_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_CD :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_CD;
        }
        if ((drmInclusion & MediatekFeature.INCLUDE_SD_DRM_MEDIA) != 0) {
            whereClause = (null == whereClause) ? 
                          FileColumns.DRM_METHOD + "=" + DrmStore.DrmMethod.METHOD_SD :
                          whereClause + " OR " + FileColumns.DRM_METHOD + "=" + 
                          DrmStore.DrmMethod.METHOD_SD;
        }
        if ((drmInclusion & MediatekFeature.INCLUDE_FLDCF_DRM_MEDIA) != 0) {
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

    public static Bitmap createDrmThumb(Activity activity,JobContext jc,
                                        LocalMediaItem item, int kind) {
        if (!item.isDrm()) {
            return item.requestImage(kind).run(jc);
        }

        boolean hasDrmRights = item.hasDrmRights();
        if (MediaItem.TYPE_MICROTHUMBNAIL == kind) {
            Bitmap bitmap = null;
            if (hasDrmRights) {
                //we only decode drm micro thumbnail when the media has drm rights
                //Note: for those media without drm rights, show a default thumbnail
                bitmap = item.requestImage(MediaItem.TYPE_MICROTHUMBNAIL).run(jc);
                if (jc.isCancelled()) return null;
                if (bitmap != null) {
                    bitmap = BitmapUtils.rotateBitmap(bitmap,
                        item.getRotation() - item.getFullImageRotation(), true);
                    //we should resize decode thumbnail to 200x200, that it will
                    //not scale in display, resulting clear and beautifull lock icon
                    bitmap = resizeThumbToDefaultSize(activity, bitmap);
                }
            }
            //if returned bitmap is null, possiblely the media has no drm rights,
            //or decode thumbnail failed even if it has drm rights.
            //In this circumstance, we create a default icon
            if (null == bitmap) {
                bitmap = createDefaultDrmMicroThumb(activity);
            }
            bitmap = ensureBitmapMutable(bitmap);
            //draw drm icons onto the thumbnail
            drawOverlayToBottomRight((Context)activity, bitmap, hasDrmRights);
            return bitmap;
        } else if (MediaItem.TYPE_THUMBNAIL == kind) {
            Bitmap bitmap = item.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
            if (jc.isCancelled()) return null;
            if (bitmap != null) {
                bitmap = BitmapUtils.rotateBitmap(bitmap,
                    item.getRotation() - item.getFullImageRotation(), true);
            }
            return bitmap;
        }
        return null;
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

    public static Bitmap resizeThumbToDefaultSize(Activity activity,
                                                  Bitmap bitmap) {
        if (null == activity || null == bitmap) return null;
        if (bitmap.getWidth() != bitmap.getHeight()) {
            Log.w(TAG,"resizeThumbToDefaultSize:we expect width==height!");
            return bitmap;
        }
        int thumbDim = getDrmMicroThumbDim(activity);//MICROTHUMBNAIL_TARGET_SIZE
        if (thumbDim == bitmap.getWidth()) {
            return bitmap;
        }
        Bitmap b = createBitmap(thumbDim, thumbDim, Bitmap.Config.ARGB_8888,
                            DRM_MICRO_THUMB_BLACK_BG);
        //draw orginal bitmap onto new bitmap
        Canvas canvas = new Canvas(b);
        Rect srcRect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Rect dstRect = new Rect(0,0,b.getWidth(),b.getHeight());
        canvas.drawBitmap(bitmap, srcRect, dstRect, null);
        bitmap.recycle();
        return b;
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

    public static boolean isTimeIntervalMedia(Context context, String path, int action) {
        if (null == mDrmManagerClient) {
            mDrmManagerClient = new DrmManagerClient(context);
        }
        ContentValues values = mDrmManagerClient.getConstraints(path, action);
        if (null != values && (
            -1 != values.getAsInteger(DrmStore.ConstraintsColumns.LICENSE_START_TIME) ||
            -1 != values.getAsInteger(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME))) {
            return true;
        } else {
            return false;
        }
    }
}
