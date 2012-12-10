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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.mediatek.mpo.MpoDecoder;

import com.android.gallery3d.R;

import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.MediatekFeature;
import com.android.gallery3d.util.MediatekFeature.Params;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class MpoHelper {
	
    private static final String TAG = "MpoHelper";

    public static final String MPO_EXTENSION = "mpo";

    public static final String MPO_MIME_TYPE = "image/mpo";
    public static final String MPO_VIEW_ACTION = "com.mediatek.action.VIEW_MPO";

    private static final int TARGET_DISPLAY_WIDTH [] = {/*1920,*/ 1280, 1280, 960, 800, 640, 480};
    private static final int TARGET_DISPLAY_HEIGHT [] = {/*1080,*/ 800, 720, 540, 480, 480, 320};
    private static final int MAX_BITMAP_DIM = 8000;

    private static Drawable sMavOverlay = null;

    public static String getMpoWhereClause(boolean showAllMpo) {
        String mpoFilter = null;
        if (!showAllMpo) {
            mpoFilter = FileColumns.MIME_TYPE + "!='" + MPO_MIME_TYPE + "'";
        }
        return mpoFilter;
    }

    public static String getWhereClause(int mtkInclusion) {
        if ((MediatekFeature.ALL_MPO_MEDIA & mtkInclusion) == 0) {
            return null;
        }
        String whereClause = null;
        String whereClauseEx = FileColumns.MIME_TYPE + "='" + 
                               MpoHelper.MPO_MIME_TYPE + "'";
        String whereClauseIn = FileColumns.MIME_TYPE + "='" + 
                               MpoHelper.MPO_MIME_TYPE + "'";
        String subWhereClause = null;
        if ((mtkInclusion & MediatekFeature.EXCLUDE_DEFAULT_MEDIA) != 0) {
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_MAV) != 0) {
                //Log.v(TAG,"getWhereClause:add where clause add mav");
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_MAV:
                          subWhereClause + " OR " + 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_MAV;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_3D) != 0) {
                //Log.v(TAG,"getWhereClause:add where clause add mpo 3d");
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_Stereo:
                          subWhereClause + " OR " + 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_Stereo;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_3D_PAN) != 0) {
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_3DPan:
                          subWhereClause + " OR " + 
                          Images.Media.MPO_TYPE + "=" + MpoDecoder.MTK_TYPE_3DPan;
            }

            if (null != subWhereClause) {
                whereClause = whereClauseEx + " AND ( " + subWhereClause + " )";
            } //else {
                //whereClause = whereClauseEx + " AND ( " + subWhereClause + " )";
            //}
        } else {
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_MAV) == 0) {
                //Log.v(TAG,"getWhereClause2:add where clause remove mav");
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_MAV:
                          subWhereClause + " AND " + 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_MAV;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_3D) == 0) {
                //Log.v(TAG,"getWhereClause2:add where clause remove mpo 3d");
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_Stereo:
                          subWhereClause + " AND " + 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_Stereo;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_MPO_3D_PAN) == 0) {
                subWhereClause = (null == subWhereClause) ? 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_3DPan:
                          subWhereClause + " AND " + 
                          Images.Media.MPO_TYPE + "!=" + MpoDecoder.MTK_TYPE_3DPan;
            }

            if (null != subWhereClause) {
                whereClause = whereClauseEx + " AND ( " + subWhereClause + " )";
            } else {
                whereClause = null;
            }

        }
        //if (null == subWhereClause) {
        //    Log.e(TAG,"getWhereClause:why got null subWhereClause?");
        //} else {
        //    whereClause = whereClause + " AND (" + subWhereClause + ")";
        //}
        //Log.i(TAG,"getWhereClause:whereClause="+whereClause);
        return whereClause;
    }
    
    public static void playMpo(Activity activity, Uri uri) {
        try {
            Intent i = new Intent(MPO_VIEW_ACTION);
            i.setDataAndType(uri, MPO_MIME_TYPE);
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Unable to open mpo file: ", e);
        }
    }

    public static void drawImageTypeOverlay(Context context, Bitmap bitmap) {
        if (null == sMavOverlay) {
            sMavOverlay = context.getResources().getDrawable(R.drawable.icon_mav_overlay);
        }
        int width = sMavOverlay.getIntrinsicWidth();
        int height = sMavOverlay.getIntrinsicHeight();
        float aspectRatio = (float) width / (float) height;
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        boolean heightSmaller = (bmpHeight < bmpWidth);
        int scaleResult = (heightSmaller ? bmpHeight : bmpWidth) / 5;
        if (heightSmaller) {
            height = scaleResult;
            width = (int)((float) scaleResult * aspectRatio);
        } else {
            width = scaleResult;
            height = (int)((float) width / aspectRatio);
        }
        int left = (bmpWidth - width) / 2;
        int top = (bmpHeight - height) / 2;
        sMavOverlay.setBounds(left, top, left + width, top + height);
        Canvas tmpCanvas = new Canvas(bitmap);
        sMavOverlay.draw(tmpCanvas);
    }

    public static Bitmap [] decodeMpoFrames(JobContext jc, Params params, 
                                            MpoDecoder mpoDecoder) {
        if (null == params || null == mpoDecoder) {
            Log.e(TAG, "decodeMpoFrames:got null decoder or params!");
            return null;
        }
        int targetDisplayWidth = params.inTargetDisplayWidth;
        int targetDisplayHeight = params.inTargetDisplayHeight;
        int frameCount = mpoDecoder.frameCount();
        int frameWidth = mpoDecoder.width();
        int frameHeight = mpoDecoder.height();
        if (targetDisplayWidth <= 0 || targetDisplayHeight <= 0 ||
            0 == frameCount || 0 == frameWidth || 0 == frameHeight) {
            Log.e(TAG, "decodeMpoFrames:got invalid parameters");
            return null;
        }

        // now as paramters are all valid, we start to decode mpo frames
        Bitmap [] mpoFrames = null;
        try {
            mpoFrames = tryDecodeMpoFrames(jc, mpoDecoder, params,
                            targetDisplayWidth, targetDisplayHeight);
        } catch(OutOfMemoryError e) {
            Log.w(TAG,"decodeMpoFrames:out memory when decode mpo frames");
            e.printStackTrace();
            // when out of memory happend, we decode smaller mpo frames
            // we try smaller display size
            int targetDisplayPixelCount = targetDisplayWidth * targetDisplayHeight;
            for (int i = 0; i < TARGET_DISPLAY_WIDTH.length; i++) {
                int pixelCount = TARGET_DISPLAY_WIDTH[i] * TARGET_DISPLAY_HEIGHT[i];
                if (pixelCount >= targetDisplayPixelCount) {
                    continue;
                } else {
                    Log.i(TAG, "decodeMpoFrames:try display (" +
                               TARGET_DISPLAY_WIDTH[i] + " x " +
                               TARGET_DISPLAY_HEIGHT[i] + ")");
                    try {
                        mpoFrames = tryDecodeMpoFrames(jc, mpoDecoder, params,
                            TARGET_DISPLAY_WIDTH[i], TARGET_DISPLAY_HEIGHT[i]);
                    } catch (OutOfMemoryError oom) {
                        Log.w(TAG, "decodeMpoFrames:out of memory again:" + oom);
                        continue;
                    }
                    Log.d(TAG, "decodeMpoFrame: we got all mpo frames");
                    break;
                }
            }
        }
        return mpoFrames;
    }

    public static Bitmap [] tryDecodeMpoFrames(JobContext jc,
            MpoDecoder mpoDecoder, Params params,
            int targetDisplayWidth, int targetDisplayHeight) {
        //we believe all the parameters are valid
        int frameCount = mpoDecoder.frameCount();
        int frameWidth = mpoDecoder.width();
        int frameHeight = mpoDecoder.height();

        Options options = new Options();
        int initTargetSize = targetDisplayWidth > targetDisplayHeight ?
                             targetDisplayWidth : targetDisplayHeight;
        float scale = (float) initTargetSize / Math.max(frameWidth, frameHeight);
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        MediatekFeature.enablePictureQualityEnhance(options, params.inPQEnhance);

        Bitmap [] mpoFrames = new Bitmap[frameCount];
        boolean decodeFailed = false;
        try {
            for (int i = 0; i < frameCount; i++) {
                Bitmap bitmap = decodeFrame(jc, mpoDecoder, i, options);
                if (null == bitmap) {
                    Log.e(TAG, "tryDecodeMpoFrames:got null frame");
                    decodeFailed = true;
                    break;
                }
                float scaleDown =
                    largerDisplayScale(bitmap.getWidth(), bitmap.getHeight(),
                                       targetDisplayWidth, targetDisplayHeight);
                if (scaleDown < 1.0f) {
                    mpoFrames[i] = resizeBitmap(bitmap, scaleDown, true);
                } else {
                    mpoFrames[i] = bitmap;
                }
                if (null != mpoFrames[i]) {
                    Log.v(TAG, "tryDecodeMpoFrames:got mpoFrames[" + i + "]:[" +
                               mpoFrames[i].getWidth() + "x" +
                               mpoFrames[i].getHeight() + "]");
                }
            }
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "tryDecodeMpoFrames:out of memory");
            recycleBitmapArray(mpoFrames);
            throw e;
        }
        if (decodeFailed) {
            recycleBitmapArray(mpoFrames);
            return null;
        }
        return mpoFrames;
    }

    public static void recycleBitmapArray(Bitmap[] bitmapArray) {
        if (null == bitmapArray) {
            return;
        }
        for (int i = 0; i < bitmapArray.length; i++) {
            if (null == bitmapArray[i]) {
                continue;
            }
            //Log.v(TAG, "recycleBitmapArray:recycle bitmapArray[" + i + "]");
            bitmapArray[i].recycle();
        }
    }

    public static Bitmap resizeBitmap(Bitmap source, float scale, boolean recycleInput) {
        if (null == source || scale <= 0.0f) {
            Log.e(TAG, "resizeBitmap:invalid parameters");
            return source;
        }
        if (scale == 1.0f) {
            // no bother to scale down
            return source;
        }

        int newWidth = Math.round((float)source.getWidth() * scale);
        int newHeight = Math.round((float)source.getHeight() * scale);
        if (newWidth > MAX_BITMAP_DIM || newHeight > MAX_BITMAP_DIM) {
            Log.w(TAG, "resizeBitmap:too large new Bitmap for scale:" + scale);
            return source;
        }

        Bitmap target = Bitmap.createBitmap(newWidth, newHeight,
                                            Bitmap.Config.ARGB_8888);
        //draw source bitmap onto it
        Canvas canvas = new Canvas(target);
        Rect src = new Rect(0, 0, source.getWidth(), source.getHeight());
        RectF dst = new RectF(0, 0, (float)newWidth, (float)newHeight);
        canvas.drawBitmap(source, src, dst, null);
        if (recycleInput) {
            source.recycle();
        }
        return target;
    }

    public static float largerDisplayScale(int frameWidth, int frameHeight,
                            int targetDisplayWidth, int targetDisplayHeight) {
        if (targetDisplayWidth <= 0 ||
            targetDisplayHeight <= 0 ||
            frameWidth <= 0 || frameHeight <= 0) {
            Log.w(TAG, "largerDisplayScale:invalid parameters");
            return 1.0f;
        }
        float initRate = 1.0f;
        initRate = Math.min((float) targetDisplayWidth / frameWidth, 
                            (float) targetDisplayHeight / frameHeight);
        initRate = Math.max(initRate,
                      Math.min((float) targetDisplayWidth / frameHeight, 
                               (float) targetDisplayHeight / frameWidth));
        initRate = Math.min(initRate, 1.0f);
        //Log.v(TAG, "largerDisplayScale:initRate=" + initRate);
        return initRate;
    }

    public static Bitmap decodeFrame(JobContext jc, MpoDecoder mpoDecoder,
                               int frameIndex, Options options) {
        if (null == mpoDecoder || frameIndex < 0 || null == options) {
            Log.w(TAG, "decodeFrame:invalid paramters");
            return null;
        }
        Bitmap bitmap = mpoDecoder.frameBitmap(frameIndex, options);
        if (null != jc && jc.isCancelled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bitmap;
    }

}
