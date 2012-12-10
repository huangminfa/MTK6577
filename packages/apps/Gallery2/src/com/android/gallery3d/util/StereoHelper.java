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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.WindowManager;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore;
import android.database.Cursor;
import android.media.MediaFile;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.util.MediatekFeature.DataBundle;
import com.android.gallery3d.util.MediatekFeature.RegionDecoder;
import com.android.gallery3d.util.MediatekFeature.Params;

import com.mediatek.mpo.MpoDecoder;

//this class is add purely to support Mediatek Stereo Photo display feature.
//At present, there may be three kinds of stereo photo: MPO, JPS and PNS.
//For mpo image, different frames are stored in a series of JPEG sequence.
//For jps & pns, right and left eye frames are stored as right part and left
//part of a single frame.
//MPO images are roughly handled in MpoHelper.java and MpoDecoder.java.
//This class is mainly aimed for JPS & PNS: decode left and right eye frame,
//data base query clause, and so on

public class StereoHelper {
	
    private static final String TAG = "StereoHelper";

    public static final String JPS_EXTENSION = "jps";

    public static final String PNS_MIME_TYPE = "image/pns";
    public static final String JPS_MIME_TYPE = "image/x-jps";
    //this mime type is mainly to support un-common mime-types imposed by
    //other source
    public static final String JPS_MIME_TYPE2 = "image/jps";

    public static final int STEREO_INDEX_NONE = 0;
    public static final int STEREO_INDEX_FIRST = 1;
    public static final int STEREO_INDEX_SECOND = 2;

    public static final int MIN_STEREO_INPUT_WIDTH = 40;
    public static final int MIN_STEREO_INPUT_HEIGHT = 60;

    public static final int STEREO_LAYOUT_NONE = 0;
    public static final int STEREO_LAYOUT_FULL_FRAME = 1 << 0; 
    public static final int STEREO_LAYOUT_LEFT_AND_RIGHT = 1 << 1;
    public static final int STEREO_LAYOUT_TOP_AND_BOTTOM = 1 << 2;
    public static final int STEREO_LAYOUT_SWAP_LEFT_AND_RIGHT = 1 << 3;
    public static final int STEREO_LAYOUT_SWAP_TOP_AND_BOTTOM = 1 << 4;

    //3d layout in MediaStore or Video Codec
    public static final int STEREO_TYPE_2D =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_2D;
    public static final int STEREO_TYPE_FRAME_SEQUENCE =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_FRAME_SEQUENCE;
    public static final int STEREO_TYPE_SIDE_BY_SIDE =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_SIDE_BY_SIDE;
    public static final int STEREO_TYPE_TOP_BOTTOM =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_TOP_BOTTOM;
    public static final int STEREO_TYPE_SWAP_LEFT_RIGHT =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_SWAP_LEFT_RIGHT;
    public static final int STEREO_TYPE_SWAP_TOP_BOTTOM =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_SWAP_TOP_BOTTOM;
    public static final int STEREO_TYPE_UNKNOWN =
        MediaStore.ThreeDimensionColumns.STEREO_TYPE_UNKNOWN;

    public static final int FLAG_EX_S3D_2D             = WindowManager.LayoutParams.FLAG_EX_S3D_2D;
    public static final int FLAG_EX_S3D_3D             = WindowManager.LayoutParams.FLAG_EX_S3D_3D;
    public static final int FLAG_EX_S3D_UNKNOWN        = WindowManager.LayoutParams.FLAG_EX_S3D_UNKNOWN;
    public static final int FLAG_EX_S3D_SIDE_BY_SIDE   = WindowManager.LayoutParams.FLAG_EX_S3D_SIDE_BY_SIDE;
    public static final int FLAG_EX_S3D_TOP_AND_BOTTOM = WindowManager.LayoutParams.FLAG_EX_S3D_TOP_AND_BOTTOM;
    public static final int FLAG_EX_S3D_LR_SWAPPED     = WindowManager.LayoutParams.FLAG_EX_S3D_LR_SWAPPED;
    public static final int FLAG_EX_S3D_MASK           = WindowManager.LayoutParams.FLAG_EX_S3D_MASK;

    private static final String SHARED_PREF_AC_SWITCH = "ac_switch";
    private static final String PREF_TAG_IMAGE_AC = "image_ac_switch";
    private static final String PREF_TAG_VIDEO_AC = "video_ac_switch";
    private static final String SHARED_PREF_AUTO_TO_2D_TOAST = "auto_to_2d";
    private static final String PREF_TAG_AUTO_TO_2D = "auto_to_2d_tag";

    //aligned with alps/frameworks/base/include/media/mediaplayer.h
    public static final int MEDIA_INFO_3D = 863;
    public static final int KEY_PARAMETER_3D_INFO = 1501;
    public static final int KEY_PARAMETER_3D_OFFSET = 1502;
    public static final int VALUE_PARAMETER_3D_AC_OFF = 0x7FFFFFFF;

    static final String[] PROJECTION_DATA =  {ImageColumns.DATA};

    //invalid bucket is regarded as that bucket not within images
    //table. There are three candidate:
    //1, special value of String.hashCode(), we choose 0
    //2, hashCode of DCIM/.thumbnails 
    //3, hashCode of sdcard/.ConvertedTo2D
    //for simplicity, we choose 0 as invalide bucket id
    public static final int INVALID_BUCKET_ID = 
                                 StereoConvertor.INVALID_BUCKET_ID;
    public static final String INVALID_LOCAL_PATH_END =
                                 StereoConvertor.INVALID_LOCAL_PATH_END;

    //stereo signature in Bundle
    public static final String STEREO_EXTRA = "onlyStereoMedia";
    public static final String INCLUDED_STEREO_IMAGE = "includedSteroImage";
    public static final String KEY_GET_NO_STEREO_IMAGE = "get_no_stereo_image";
    public static final String ATTATCH_WITHOUT_CONVERSION = "attachWithoutConversion";

    public static final int HW_LIMITATION_2D_TO_3D = 2048;
    public static final int MAX_BITMAP_BYTE_COUNT = 10 * 1024 * 1024;



    private static final boolean mIsMpoSupported = 
                                          MediatekFeature.isMpoSupported();
    private static final boolean mIsStereoDisplaySupported = 
                                          MediatekFeature.isStereoDisplaySupported();

    private static Drawable sStereoOverlay = null;

    public static void drawImageTypeOverlay(Context context, Bitmap bitmap) {
        if (null == sStereoOverlay) {
            sStereoOverlay = context.getResources().getDrawable(R.drawable.ic_stereo_overlay);
        }
        int width = sStereoOverlay.getIntrinsicWidth();
        int height = sStereoOverlay.getIntrinsicHeight();
        Log.d(TAG, "original stereo overlay w=" + width + ", h=" + height);
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
        Log.d(TAG, "scaled stereo overlay w=" + width + ", h=" + height);
        // 2 pixels' padding for both left and bottom
        int left = 2;   
        int bottom = bmpHeight - 2;
        int top = bottom - height;
        int right = width + left;
        Log.d(TAG, "stereo overlay drawing dimension=(" + left + ", " + top + ", " + right + ", " + bottom + ")");
        sStereoOverlay.setBounds(left, top, right, bottom);
        Canvas tmpCanvas = new Canvas(bitmap);
        sStereoOverlay.draw(tmpCanvas);
    }

    public static boolean isStereoImage(MediaItem item) {
        if (null == item) return false;
        int support = item.getSupportedOperations();
        if ((support & MediaObject.SUPPORT_STEREO_DISPLAY) != 0 &&
            (support & MediaObject.SUPPORT_CONVERT_TO_3D) == 0 &&
            MediaObject.MEDIA_TYPE_IMAGE == item.getMediaType()) {
            return true;
        } else {
            return false;
        }
    }

    public static void updateConvergence(Context context, 
                           int convergence, MediaItem item) {
        if (null == item || !(item instanceof LocalImage)) return;
        LocalImage image = (LocalImage) item;
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Images.Media.CONVERGENCE, convergence);
        String where = ImageColumns._ID + "=" + String.valueOf(image.id);
        cr.update(Images.Media.EXTERNAL_CONTENT_URI, values, where, null);
    }

    private static Uri getExternalUri(boolean isImage) {
        return isImage? Images.Media.EXTERNAL_CONTENT_URI :
                        Video.Media.EXTERNAL_CONTENT_URI;
    }

    public static void updateConvergence(Context context, 
                           boolean isImage, int id, int convergence) {
        if (null == context || id < 0) return;
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.ThreeDimensionColumns.CONVERGENCE, convergence);
        String where = ImageColumns._ID + "=" + id;
        cr.update(getExternalUri(isImage), values, where, null);
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        if (null == context || null == uri) return null;

        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            Log.v(TAG, "getFilePathFromUri:got file path:"+uri.getPath());
            return uri.getPath();
        }
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Log.w(TAG, "getFilePathFromUri:got unknown uri scheme");
            return null;
        }
        return queryFilePathFromDB(context, uri);
    }

    private static String queryFilePathFromDB(Context context, Uri uri) {
        if (null == context || null == uri) return null;
        String filePath = null;
        Cursor cursor = null;
        try { 
            ContentResolver cr = context.getContentResolver();
            cursor = cr.query(uri, PROJECTION_DATA, null, null ,null);
            if (null != cursor && cursor.moveToNext()) {
                filePath = cursor.getString(0);
                Log.d(TAG, "queryFilePathFromDB:got file path " +
                           filePath + " for " + uri);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }
        return filePath;
    }

    private static void updateDatabaseForFilePath(Context context,
                            ContentValues values, String filePath) {
        if (null == context || null == values || null == filePath) return;
        ContentResolver cr = context.getContentResolver();
        Uri targetUri = null;

        int fileType = MediaFile.getFileTypeBySuffix(filePath);
        if (MediaFile.isVideoFileType(fileType)) {
            targetUri = Video.Media.EXTERNAL_CONTENT_URI;
        } else if (MediaFile.isImageFileType(fileType)) {
            targetUri = Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            Log.e(TAG, "updateDatabaseForFilePath:un-intended file type");
            return;
        }
        Log.d(TAG, "updateDatabaseForFilePath:got target Uri:" + targetUri);

        int updated = cr.update(targetUri, values, Images.Media.DATA + "= ?",
                                new String[]{ filePath });
        if (1 != updated) {
            Log.w(TAG, "updateDatabaseForFilePath: why we updated " + updated + " row(s)");
        }
    }

    public static void updateStereoLayout(Context context, Uri uri, int stereoLayout) {
        updateStereoLayout(context, uri, stereoLayout, System.currentTimeMillis());
    }

    public static void updateStereoLayout(Context context, Uri uri, int stereoLayout,
                                         long lastModifiedTime) {
        if (null == context || null == uri) return;
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) &&
            !ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            Log.w(TAG, "updateStereoLayout:got a Uri that is not file or from media DB");
            return;
        }

        //query database to get _data
        String filePath = getFilePathFromUri(context, uri);
        if (null == filePath) {
            Log.e(TAG, "updateStereoLayout:got null file path for " + uri);
            return;
        }

        //modify file last modified.
        File file = new File(filePath);
        if (null == file || !file.exists()) {
            Log.w(TAG, "updateStereoLayout: why no file exist!");
            return;
        }
        if (!file.setLastModified(lastModifiedTime)) {
            Log.w(TAG, "updateStereoLayout: why setLastModified returns false!");
        }

        //update database!
        ContentValues values = new ContentValues();
        values.put(MediaStore.ThreeDimensionColumns.STEREO_TYPE, stereoLayout);
        values.put(Images.Media.DATE_MODIFIED, (int)(lastModifiedTime / 1000));
        updateDatabaseForFilePath(context, values, filePath);
    }

    public static void updateStereoLayout(Context context, 
                           boolean isImage, int id, int stereoLayout) {
        if (null == context || id < 0) return;
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.ThreeDimensionColumns.STEREO_TYPE, stereoLayout);
        String where = ImageColumns._ID + "=" + id;
        cr.update(getExternalUri(isImage), values, where, null);
    }

    public static int getSurfaceLayout(int stereoLayout) {
        switch (stereoLayout) {
        case STEREO_TYPE_2D:
            return FLAG_EX_S3D_2D;
        case STEREO_TYPE_SIDE_BY_SIDE:
            return FLAG_EX_S3D_SIDE_BY_SIDE;
        case STEREO_TYPE_TOP_BOTTOM:
            return FLAG_EX_S3D_TOP_AND_BOTTOM;
        case STEREO_TYPE_SWAP_LEFT_RIGHT:
            return FLAG_EX_S3D_SIDE_BY_SIDE | FLAG_EX_S3D_LR_SWAPPED;
        case STEREO_TYPE_SWAP_TOP_BOTTOM:
            return FLAG_EX_S3D_TOP_AND_BOTTOM | FLAG_EX_S3D_LR_SWAPPED;
        default:
            return FLAG_EX_S3D_UNKNOWN;
        }
    }

    public static int getSurfaceStereoMode(boolean stereo) {
        if (stereo) {
            return FLAG_EX_S3D_3D;
        } else {
            return FLAG_EX_S3D_2D;
        }
    }

    public static boolean isStereo(int stereoLayout) {
        if (stereoLayout == STEREO_TYPE_2D || stereoLayout == STEREO_TYPE_UNKNOWN) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean needAutoFormatDection(int stereoLayout) {
        return STEREO_TYPE_UNKNOWN == stereoLayout;
    }

    public static int getManualConvergencePointNum() {
        return 9;
    }

    public static void tryToShowConvertTo2DMode(Context context) {
        if (null == context) return;
        //judge if we have shown this toast before
        SharedPreferences pref = 
            context.getSharedPreferences(SHARED_PREF_AUTO_TO_2D_TOAST,
                                         Context.MODE_PRIVATE);
        boolean shown = pref.getBoolean(PREF_TAG_AUTO_TO_2D, false);

        if (shown) {
            return;
        }

        //show toast
        Toast.makeText(context, R.string.stereo3d_auto_switch_to_2d_mode,
                       Toast.LENGTH_SHORT).show();

        //record that we have shown the toast
        Editor ed = pref.edit();
        ed.putBoolean(PREF_TAG_AUTO_TO_2D, true);
        ed.commit();
    }

    private static String getAcPrefTAG(boolean isImage) {
        return isImage ? PREF_TAG_IMAGE_AC :
                         PREF_TAG_VIDEO_AC;
    }

    public static boolean setACEnabled(Context context, 
                                       boolean isImage, boolean enabled) {
        if (null == context) {
            Log.e(TAG, "setACEnabled:why we got a null context");
            return false;
        }
        SharedPreferences pref = 
            context.getSharedPreferences(SHARED_PREF_AC_SWITCH,
                                         Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putBoolean(getAcPrefTAG(isImage), enabled);
        ed.commit();
        return true;
    }

    public static boolean getACEnabled(Context context, boolean isImage) {
        if (null == context) {
            Log.e(TAG, "getACEnabled:why we got a null context");
            return false;
        }
        SharedPreferences pref = 
            context.getSharedPreferences(SHARED_PREF_AC_SWITCH,
                                         Context.MODE_PRIVATE);
        return pref.getBoolean(getAcPrefTAG(isImage), true);
    }

    public static void dumpBitmap(Bitmap b, String filename) {
        Log.d(TAG,"dumpBitmap("+b+")");
        if (b != null) {
            Log.v(TAG,"dumpBitmap:["+b.getWidth()+"x"+b.getHeight()+"]");
            Log.v(TAG,"dumpBitmap:config:"+b.getConfig());
            java.io.FileOutputStream fos = null;
            try {
                if (null == filename) {
                    filename = android.os.Environment.getExternalStorageDirectory().toString()
                            +"/DCIM/Bitmap["+ android.os.SystemClock.uptimeMillis() + "].png";
                }
                fos = new java.io.FileOutputStream(filename);
                b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (java.io.IOException ex) {
                // MINI_THUMBNAIL not exists, ignore the exception and generate one.
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (java.io.IOException ex) {
                    }
                }
            }
        }
    }

    public static int convertToLocalLayout(int MediaStoreLayout) {
        switch (MediaStoreLayout) {
            case STEREO_TYPE_2D: 
                return STEREO_LAYOUT_NONE;
            case STEREO_TYPE_FRAME_SEQUENCE: 
                return STEREO_LAYOUT_FULL_FRAME; 
            case STEREO_TYPE_SIDE_BY_SIDE: 
                return STEREO_LAYOUT_LEFT_AND_RIGHT;
            case STEREO_TYPE_TOP_BOTTOM: 
                return STEREO_LAYOUT_TOP_AND_BOTTOM;
            case STEREO_TYPE_SWAP_LEFT_RIGHT: 
                return STEREO_LAYOUT_SWAP_LEFT_AND_RIGHT;
            case STEREO_TYPE_SWAP_TOP_BOTTOM: 
                return STEREO_LAYOUT_SWAP_TOP_AND_BOTTOM;
        }
        return STEREO_LAYOUT_NONE;
    }

    public static int getInclusionFromData(Bundle data) {
        //Log.v(TAG,"getInclusionFromData(data="+data+")");
        if (!mIsStereoDisplaySupported || null == data) {
            if (mIsMpoSupported) {
                return MediatekFeature.INCLUDE_MPO_MAV;
            }
            return 0;
        }
        int stereoInclusion = MediatekFeature.INCLUDE_STEREO_JPS;
        // stereoInclusion |= MediatekFeature.INCLUDE_STEREO_PNS;
        stereoInclusion |= MediatekFeature.INCLUDE_STEREO_VIDEO;
        boolean onlyStereoMedia = data.getBoolean(STEREO_EXTRA, false);
        boolean getAlbum = data.getBoolean(Gallery.KEY_GET_ALBUM, false);
        if (mIsMpoSupported) {
            if (onlyStereoMedia) {
                stereoInclusion |= MediatekFeature.INCLUDE_MPO_3D;
                stereoInclusion |= MediatekFeature.INCLUDE_MPO_3D_PAN;
                //exclude normal images/videos
                stereoInclusion |= MediatekFeature.EXCLUDE_DEFAULT_MEDIA;
            } else {
                stereoInclusion |= MediatekFeature.ALL_MPO_MEDIA;
            }
        }
        //if (!getAlbum && !onlyStereoMedia) {
        if (!onlyStereoMedia) {
            //Log.e(TAG,"getInclusionFromData:create virtual folder...");
            //create a virtual folder for stereo feature
            stereoInclusion |= MediatekFeature.INCLUDE_STEREO_FOLDER;
        }
        return stereoInclusion;
    }

    public static String getWhereClause(int mtkInclusion) {
        //Log.i(TAG,"getWhereClause(mtkInclusion="+mtkInclusion+")");
        String mpoWhereClause = null;//MpoHelper.getWhereClause(mtkInclusion);
        String whereClause = null;
        if ((mtkInclusion & MediatekFeature.EXCLUDE_DEFAULT_MEDIA) != 0) {
            //add mpo inclusion
            if (null != mpoWhereClause) {
                //Log.v(TAG,"getWhereClause:add where clause add mpo");
                whereClause = (null == whereClause) ? 
                          mpoWhereClause : whereClause + " OR " + mpoWhereClause;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_VIDEO) != 0) {
                //Log.v(TAG,"getWhereClause:add where clause add stereo video");
                whereClause = (null == whereClause) ? 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "!=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + "!=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NOT NULL)" :
                          whereClause + " OR " + 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "!=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + "!=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NOT NULL)";
            }
        } else {
            if (null != mpoWhereClause) {
                //Log.v(TAG,"getWhereClaus2:add where clause remove mpo");
                whereClause = (null == whereClause) ? 
                          mpoWhereClause : whereClause + " AND " + mpoWhereClause;
            }
            if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_VIDEO) == 0) {
                //Log.v(TAG,"getWhereClause:add where clause add stereo video");
                whereClause = (null == whereClause) ? 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + "=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NULL)":
                          whereClause + " AND " + 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + "=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NULL)";
            }
        }

        //String directPath = StorageManager.getDefaultPath() +
        //                    StereoConvertor.STEREO_CONVERTED_TO_2D_FOLDER2;
        //String dirWhereClause = ImageColumns.BUCKET_ID + " != " + 
        //                        directPath.toLowerCase().hashCode();
        whereClause = (null == whereClause) ? 
                      StereoConvertor.HIDE_FOLDER_WHERE_CLAUSE :
                      "(" + whereClause +  ") AND " + 
                      StereoConvertor.HIDE_FOLDER_WHERE_CLAUSE;
        //Log.v(TAG,"getWhereClause:whereClause = "+whereClause);
        return whereClause;
    }

    public static String getWhereClause(int mtkInclusion, boolean queryVideo) {
        //Log.i(TAG,"getWhereClause(mtkInclusion="+mtkInclusion+",queryVideo="+queryVideo+")");
        String mpoWhereClause = MpoHelper.getWhereClause(mtkInclusion);
        String whereClause = null;
        if ((mtkInclusion & MediatekFeature.EXCLUDE_DEFAULT_MEDIA) != 0) {
            if (!queryVideo) {
                //add mpo inclusion
                if (null != mpoWhereClause) {
                    //Log.v(TAG,"getWhereClause:add where clause add mpo");
                    whereClause = (null == whereClause) ? 
                          mpoWhereClause : whereClause + " OR " + mpoWhereClause;
                }
                if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_JPS) != 0) {
                    //Log.v(TAG,"getWhereClause:add where clause add jps");
                    whereClause = (null == whereClause) ? 
                          FileColumns.MIME_TYPE + "='" + JPS_MIME_TYPE + "'" :
                          whereClause + " OR " + 
                          FileColumns.MIME_TYPE + "='" + JPS_MIME_TYPE + "'";
                }
            } else {
                if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_VIDEO) != 0) {
                    //Log.v(TAG,"getWhereClause:add where clause add stereo video");
                    whereClause = (null == whereClause) ? 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "!=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + "!=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NOT NULL)" :
                          whereClause + " OR " + 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "!=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + "!=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " AND " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NOT NULL)";
                }
            }
        } else {
            if (!queryVideo) {
                if (null != mpoWhereClause) {
                    //Log.v(TAG,"getWhereClaus2:add where clause remove mpo");
                    whereClause = (null == whereClause) ? 
                          mpoWhereClause : whereClause + " AND " + mpoWhereClause;
                }
                if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_JPS) == 0) {
                    //Log.v(TAG,"getWhereClause2:add where clause remove jps");
                    whereClause = (null == whereClause) ? 
                          FileColumns.MIME_TYPE + "!='" + JPS_MIME_TYPE + "'" :
                          whereClause + " AND " + 
                          FileColumns.MIME_TYPE + "!='" + JPS_MIME_TYPE + "'";
                }
            } else {
                if ((mtkInclusion & MediatekFeature.INCLUDE_STEREO_VIDEO) != 0) {
                    //Log.v(TAG,"getWhereClause:add where clause add stereo video");
                    whereClause = (null == whereClause) ? 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + "=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NULL)":
                          whereClause + " AND " + 
                          "("+MediaStore.Video.Media.STEREO_TYPE + "=" + 
                          MediaStore.Video.Media.STEREO_TYPE_2D + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + "=" +
                          MediaStore.Video.Media.STEREO_TYPE_UNKNOWN + " OR " +
                          MediaStore.Video.Media.STEREO_TYPE + " IS NULL)";
                }
            }
        }

        //String directPath = StorageManager.getDefaultPath() +
        //                    StereoConvertor.STEREO_CONVERTED_TO_2D_FOLDER2;
        //String dirWhereClause = ImageColumns.BUCKET_ID + " != " + 
        //                        directPath.toLowerCase().hashCode();
        whereClause = (null == whereClause) ? 
                      StereoConvertor.HIDE_FOLDER_WHERE_CLAUSE :
                      "(" + whereClause +  ") AND " + 
                      StereoConvertor.HIDE_FOLDER_WHERE_CLAUSE;
        //Log.v(TAG,"getWhereClause:whereClause = "+whereClause);
        return whereClause;
    }

    public static DataBundle
        generateSecondImage(JobContext jc, Bitmap bitmap, Params params,
                            boolean recyleBitmap) {
        if (null == bitmap || null == params) return null;

        if (MIN_STEREO_INPUT_WIDTH > bitmap.getWidth() ||
            MIN_STEREO_INPUT_HEIGHT > bitmap.getHeight()) {
            Log.i(TAG,"generateSecondImage:image dimension too small");
            return null;
        }

        bitmap = BitmapUtils.resizeDownBySideLength(bitmap, 
                     HW_LIMITATION_2D_TO_3D / 2, true);

        int originBitmapW = bitmap.getWidth();
        int originBitmapH = bitmap.getHeight();

        //be careful if one dimension is not even.
        int increaseX = originBitmapW % 2;
        int increaseY = originBitmapH % 2;

        Log.v(TAG, "generateSecondImage:params.inRotation="+params.inRotation);

        Bitmap input = null;
        if (increaseX + increaseY > 0 || params.inRotation != 0) {
            Log.d(TAG, "generateSecondImage:resize or rotate before convert");
            boolean switchWH = (params.inRotation % 180) == 0;
            int newWidth = switchWH ? originBitmapW + increaseX :
                                      originBitmapH + increaseY;
            int newHeight = switchWH ? originBitmapH + increaseY :
                                       originBitmapW + increaseX;
            input = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            //draw original bitmap to input Bitmap
            Canvas tempCanvas = new Canvas(input);
            Matrix matrix = getDrawBitmapMatrix(params.inRotation,
                                                originBitmapW, originBitmapH);
            tempCanvas.drawBitmap(bitmap, matrix, null);
        } else {
            input = bitmap;
        }

        //To save memory usage,recycel bitmap if a new bitmap is created
        if (recyleBitmap && bitmap != input) {
            bitmap.recycle();
        }

        //dumpBitmap(input,"mnt/sdcard2/DCIM/["
        //        + android.os.SystemClock.uptimeMillis() + "]Input.png");
        Bitmap stereo = StereoConvertor.convert2Dto3D(input);
        //dumpBitmap(stereo,"mnt/sdcard2/DCIM/["
        //        + android.os.SystemClock.uptimeMillis() + "]Stereo.png");

        if (recyleBitmap) {
            input.recycle();
        }

        if (jc.isCancelled()) return null;

        if (null == stereo) {
            Log.w(TAG, "generateSecondImage:failed to create stereo pair");
            return null;
        }

        DataBundle dataBundle =retrieveDataBundle(jc, stereo, params,
                         originBitmapW, originBitmapH, increaseX, increaseY);
        stereo.recycle();
        return dataBundle;
    }

    private static DataBundle
        retrieveDataBundle(JobContext jc, Bitmap stereo, Params params,
            int originBitmapW, int originBitmapH, int increaseX, int increaseY) {
        if (null == stereo || null == params) {
            Log.w(TAG, "retrieveDataBundle: got null stereo or params");
            return null;
        }

        DataBundle dataBundle = new DataBundle();

        //crop left part of the image to create a new Bitmap
        Bitmap firstFrame = retrieveStereoImage(stereo, true, originBitmapW, 
                        originBitmapH, increaseX, increaseY, params.inRotation);
        //dumpBitmap(firstFrame,"mnt/sdcard2/DCIM/["
        //        + android.os.SystemClock.uptimeMillis() + "]First.png");

        if (jc.isCancelled()) return null;

        if (params.inFirstFrame) {
            dataBundle.firstFrame = firstFrame;
        } else if (params.inFirstFullFrame) {
            //compress the first frame to buffer
            dataBundle.firstFullFrame = getRegionDecoder(jc, firstFrame, true);
        }

        if (jc.isCancelled()) {
            dataBundle.recycle();
            return null;
        }

        //crop left part of the image to create a new Bitmap
        Bitmap secondFrame = retrieveStereoImage(stereo, false, originBitmapW,
                        originBitmapH, increaseX, increaseY, params.inRotation);
        //dumpBitmap(firstFrame,"mnt/sdcard2/DCIM/["
        //        + android.os.SystemClock.uptimeMillis() + "]Second.png");

        if (params.inSecondFrame) {
            dataBundle.secondFrame = secondFrame;
        } else if (params.inSecondFullFrame) {
            //compress the first frame to buffer
            dataBundle.secondFullFrame = getRegionDecoder(jc, secondFrame, true);
        }

        if (jc.isCancelled()) {
            dataBundle.recycle();
            return null;
        }

        return dataBundle;
    }

    private static Bitmap retrieveStereoImage(Bitmap stereo, boolean first,
                      int originBitmapW, int originBitmapH,
                      int increaseX, int increaseY, int rotation) {
        Bitmap temp = Bitmap.createBitmap(originBitmapW, 
                                          originBitmapH,
                                          Bitmap.Config.ARGB_8888);

        //draw first/second screen nail onto it
        Canvas canvas = new Canvas(temp);
        Matrix matrix = getSplitBitmapBackpMatrix(rotation, stereo.getWidth(),
            stereo.getHeight(), increaseX, increaseY, first);
        canvas.drawBitmap(stereo, matrix, null);

        return temp;
    }

    private static Matrix getDrawBitmapMatrix(int rotation, int width, int height) {
        Matrix matrix = new Matrix();
        switch (rotation) {
        case 0:
            break;
        case 90:
            matrix.postTranslate(0, - height);
            break;
        case 180:
            matrix.postTranslate(- width, - height);
            break;
        case 270:
            matrix.postTranslate(- width, 0);
            break;
        }
        matrix.postRotate(rotation);
        return matrix;
    }

    private static Matrix getSplitBitmapBackpMatrix(int rotation, int width,
                       int height, int increaseX, int increaseY, boolean first) {
        Matrix matrix = new Matrix();
        int pivotX = 0;
        int pivotY = 0;
        switch (rotation) {
        case 0:
            if (first) {
                pivotX = 0;
            } else {
                pivotX = width / 2;
            }
            pivotY = 0;
            matrix.postTranslate(- pivotX, - pivotY);
            matrix.postRotate(360 - rotation);
            break;
        case 90:
            if (first) {
                pivotX = width / 2;
            } else {
                pivotX = width;
            }
            pivotY = 0;
            matrix.postTranslate(- pivotX, - pivotY);
            matrix.postRotate(360 - rotation);
            matrix.postTranslate(0, - increaseY);
            break;
        case 180:
            if (first) {
                pivotX = width / 2;
            } else {
                pivotX = width;
            }
            pivotY = height;
            matrix.postTranslate(- pivotX, - pivotY);
            matrix.postRotate(360 - rotation);
            matrix.postTranslate(- increaseX, - increaseY);
            break;
        case 270:
            if (first) {
                pivotX = 0;
            } else {
                pivotX = width / 2;
            }
            pivotY = height;
            matrix.postTranslate(- pivotX, - pivotY);
            matrix.postRotate(360 - rotation);
            matrix.postTranslate(- increaseX, 0);
            break;
        }
        return matrix;
    }

    public static DataBundle getStereoImage(JobContext jc, ContentResolver cr, Uri uri,
                                        String mimeType, Params params,
                                        Options options, int targetSize) {
        if (null == params) return null;
        params.showInfo();
        DataBundle dataBundle = new DataBundle();
        Bitmap bitmap = null;

        if (params.inSecondFrame || params.inFirstFrame) {
            bitmap = getStereoImage(jc, cr, uri, mimeType, false,
                                        options, targetSize);
            dataBundle.secondFrame = bitmap;
        } else if (params.inSecondFullFrame || params.inFirstFullFrame) {
            //for JPS or MPO file, only second full image is needed, as
            //the first image is the one that is displayed as 2D

            if (null == options) options = new Options();

            //This is tricky! we pass the special target size (-1) as to tell
            //getStereoImage() to recalculate the sample size 
            bitmap = getStereoImage(jc, cr, uri, mimeType, false,
                                        options, -1);
            if (bitmap == null) {
                Log.w(TAG, "decode orig failed ");
                return null;
            }
            if (jc.isCancelled()) return null;

            //resize the bitmap if it is too large
            //if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
            //    bitmap = BitmapUtils.resizeDownAndCropCenter(bitmap,
            //            mTargetSize, true);
            //} else {
            //    bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
            //            mTargetSize, true);
            //}

            //compress the bitmap to JPEG buffer
            dataBundle.secondFullFrame = getRegionDecoder(jc, bitmap, true);
        }
        return dataBundle;
    }

    private static Bitmap getMpoFrame(JobContext jc, MpoDecoder mpoDecoder, 
                boolean firstFrame, Options options, int targetSize) {
        if (null == mpoDecoder) {
            Log.w(TAG,"getMpoFrame:null MpoDecoder");
            return null;
        }
        if (mpoDecoder.frameCount() < 1) {
            Log.w(TAG,"getMpoFrame:invalid frame count:"+
                      mpoDecoder.frameCount());
            return null;
        }
        if (null != jc && jc.isCancelled()) return null;

        int frameIndex = firstFrame ? 0 : 1;
        if (!firstFrame && 4 == mpoDecoder.frameCount()) {
            //sepecial workaround for sony 3d panorama, because it
            //contains 4 image, first two are left eyed, the last
            //two are right eyed.
            frameIndex = 2;
        }
        if (options == null) options = new Options();
        if (targetSize > 0) {
            if (null != jc && jc.isCancelled()) return null;
            options.inSampleSize = computeSampleSizeLarger(
                   mpoDecoder.width(), mpoDecoder.height(), targetSize, 0);
        } else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;
        return mpoDecoder.frameBitmap(frameIndex, options);
    }

    public static Bitmap getStereoImage(JobContext jc, ContentResolver cr, Uri uri,
                                        String mimeType, boolean firstFrame,
                                        Options options, int targetSize) {
        Bitmap bitmap = null;
        if (uri != null && null != mimeType) {
            if (options == null) options = new Options();

            if ("image/mpo".equals(mimeType)) {
                MpoDecoder mpoDecoder = MpoDecoder.decodeUri(cr, uri);
                if (null == mpoDecoder) {
                    Log.e(TAG,"getStereoImage:failed to decode mpo uri:"+ uri);
                    return null;
                }
                try {
                    bitmap = getMpoFrame(jc, mpoDecoder, firstFrame,
                                         options, targetSize);
                } finally {
                    mpoDecoder.close();
                }
            } else if (JPS_MIME_TYPE.equals(mimeType)) {
                ParcelFileDescriptor pfd = null;
                FileDescriptor fd = null;
                try {
                    pfd = cr.openFileDescriptor(uri, "r");
                    fd = pfd.getFileDescriptor();
                    Rect imageRect = generateRect(fd, mimeType, firstFrame, options);
                    Log.i(TAG,"getStereoImage:imageRect="+imageRect);
                    if (null != jc && jc.isCancelled()) return null;

                    if (targetSize != 0) {
                        options.inSampleSize = computeSampleSizeLarger(
                                    imageRect.right - imageRect.left, 
                                    imageRect.bottom - imageRect.top, targetSize, 0);
                    } else {
                        options.inSampleSize = 1;
                    }
                    //Log.d(TAG,"getStereoImage:options.inSampleSize="+options.inSampleSize);

                    bitmap = getFlatternedImage(jc, fd, mimeType, firstFrame,
                                                options, imageRect);
                } catch (Exception ex) {
                    Log.w(TAG, ex);
                } finally {
                    Utils.closeSilently(pfd);
                }
            }
        } else {
            Log.e(TAG,"getStereoImage:get null local path");
        }
        if (null != bitmap) {
            Log.i(TAG,"getStereoImage:["+bitmap.getWidth()+"x"+bitmap.getHeight()+"]");
        }
        return bitmap;
    }

    public static DataBundle getStereoImage(JobContext jc, String localFilePath,
                                        String mimeType, Params params,
                                        Options options, int targetSize) {
        if (null == params) return null;
        params.showInfo();
        DataBundle dataBundle = new DataBundle();
        Bitmap bitmap = null;

        if (params.inSecondFrame || params.inFirstFrame) {
            bitmap = getStereoImage(jc, localFilePath, mimeType, false,
                                        options, targetSize);
            dataBundle.secondFrame = bitmap;
        } else if (params.inSecondFullFrame || params.inFirstFullFrame) {
            if (null == options) options = new Options();
            boolean tempPostProc;
            tempPostProc = options.inPostProc;
            options.inPostProc = false;

            if (mIsMpoSupported && localFilePath.toLowerCase().endsWith(".mpo")
                || localFilePath.toLowerCase().endsWith(".jps")) {
                //for JPS or MPO file, only second full image is needed, as
                //the first image is the one that is displayed as 2D

                //This is tricky! we pass the special target size (-1) as to tell
                //getStereoImage() to recalculate the sample size 
                bitmap = getStereoImage(jc, localFilePath, mimeType, false,
                                        options, -1);
                if (bitmap == null) {
                    Log.w(TAG, "decode orig failed ");
                    return null;
                }
                if (jc.isCancelled()) return null;

                //resize the bitmap if it is too large
                //if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                //    bitmap = BitmapUtils.resizeDownAndCropCenter(bitmap,
                //            mTargetSize, true);
                //} else {
                //    bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
                //            mTargetSize, true);
                //}

                //compress the bitmap to JPEG buffer
                dataBundle.secondFullFrame = getRegionDecoder(jc, bitmap, true);
           } else {
                bitmap = decodeLargeBitmap(jc, localFilePath,
                                        options, -1);
                if (null == bitmap) {
                    Log.e(TAG,"getStereoImage:failed to decode large bitmap!");
                    return null;
                }
                if (jc.isCancelled()) {
                    bitmap.recycle();
                    return null;
                }
                dataBundle = generateSecondImage(jc, bitmap, params, true);
           }
           options.inPostProc = tempPostProc;
        }
        return dataBundle;
    }

    private static int computeSampleSizeLarger(int w, int h, int targetSize,
                                               int maxDim) {
        //Log.d(TAG, "computeSampleSizeLarger(w=" + w + ",h=" + h + ",targetSize=" +
        //           targetSize + ",maxDim=" + maxDim + ")");
        int sampleSize = 1;
        if (targetSize != -1) {
            if (targetSize <= MediaItem.MICROTHUMBNAIL_TARGET_SIZE) {
                // We center-crop the original image as it's micro thumbnail. In this case,
                // we want to make sure the shorter side >= "targetSize".
                float scale = (float) targetSize / Math.min(w, h);
                sampleSize = BitmapUtils.computeSampleSizeLarger(scale);

                // For an extremely wide image, e.g. 300x30000, we may got OOM when decoding
                // it for TYPE_MICROTHUMBNAIL. So we add a max number of pixels limit here.
                final int MAX_PIXEL_COUNT = 640000; // 400 x 1600
                if ((w / sampleSize) * (h / sampleSize) > MAX_PIXEL_COUNT) {
                    sampleSize = BitmapUtils.computeSampleSize(
                            android.util.FloatMath.sqrt((float) MAX_PIXEL_COUNT / (w * h)));
                }
            } else {
                sampleSize = BitmapUtils.computeSampleSizeLarger(w, h, targetSize);
            }
        } else {
            sampleSize = calculateSampleSize(MAX_BITMAP_BYTE_COUNT, maxDim, w, h);
        }
        //Log.i(TAG,"computeSampleSizeLarger:sampleSize="+sampleSize);
        return sampleSize;
    }

    private static int calculateSampleSize(int maxBytes, int maxDim,
                                           int width, int height) {
        //we need to recalculate the target Size to decode large bitmap
        //we believe (partially base on survey) that height/width ration of
        //normal panorama is more or less 7. We hope that the decode image
        //is at least 540 (1080/2) pixels in width or height. So the budget 
        //is about 540*7*540*4=8MB memory. So totally we hope the decoded
        //Bitmap is 540 pixels or within 10MB memory.
        //targetSize = xxxxx

        int sampleSize = 1;
        //first, calculate the smallest sample size that makes the decoded
        //Bitmap's memory size is less than the budget
        while (width * height * 4 / sampleSize / sampleSize > maxBytes) {
            sampleSize *= 2;
        }
        //second, check if the dimension of decode bitmap exceed max dimension
        //If exceed, continue enlarge sample size
        while (maxDim > 0 &&
            (width / sampleSize > maxDim || height / sampleSize > maxDim)) {
            sampleSize *= 2;
        }
        //then, we check if we increase the sample size that still makes
        //short dimension is larger than 540
        while (width / sampleSize / 2 > 540 && 
               height / sampleSize / 2 > 540 &&
               width / sampleSize / 2 <= HW_LIMITATION_2D_TO_3D &&
               height / sampleSize / 2 <= HW_LIMITATION_2D_TO_3D) {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    public static RegionDecoder getRegionDecoder(JobContext jc, Bitmap bitmap,
                                    boolean recycleInput) {
        if (null == bitmap) return null;
        byte[] array = BitmapUtils.compressBitmap(bitmap);
        if (null == array) return null;
        if (null != array)Log.v(TAG,"getStereoImage:array.length="+array.length);

        if (jc.isCancelled()) return null;
        if (recycleInput) bitmap.recycle();

        RegionDecoder regionDecoder = new RegionDecoder();
        regionDecoder.jpegBuffer = array;
        regionDecoder.regionDecoder = 
                     DecodeUtils.requestCreateBitmapRegionDecoder(
                                       jc, array, 0, array.length, true);
        return regionDecoder;
    }

    public static Bitmap decodeLargeBitmap(JobContext jc, String localFilePath,
                                        Options options, int targetSize) {
        Bitmap bitmap = null;
        if (localFilePath == null) {
            Log.e(TAG,"decodeLargeBitmap:get null local path");
            return null;
        }

        if (options == null) options = new Options();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(localFilePath);
            FileDescriptor fd = fis.getFD();

            Rect imageRect = decodeImageRect(fd, options);

            Log.i(TAG,"decodeLargeBitmap:imageRect="+imageRect);
            if (null != jc && jc.isCancelled()) return null;

            options.inSampleSize = computeSampleSizeLarger(
                        imageRect.right - imageRect.left, 
                        imageRect.bottom - imageRect.top, 
                        targetSize, HW_LIMITATION_2D_TO_3D / 2);
            Log.w(TAG,"decodeLargeBitmap:options.inSampleSize="+options.inSampleSize);

            //save value that we will modified
            boolean temp = options.inJustDecodeBounds;

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);

            //restore the value we changed
            options.inJustDecodeBounds = temp;
        } catch (Exception ex) {
            Log.w(TAG, ex);
        } finally {
            Utils.closeSilently(fis);
        }

        if (null != bitmap) {
            Log.i(TAG,"decodeLargeBitmap:["+bitmap.getWidth()+"x"+bitmap.getHeight()+"]");
        }
        return bitmap;
    }

    public static Bitmap getStereoImage(JobContext jc, String localFilePath,
                                        String mimeType, boolean firstFrame,
                                        Options options, int targetSize) {
        Bitmap bitmap = null;
        if (localFilePath != null) {
            if (options == null) options = new Options();

            if (localFilePath.toLowerCase().endsWith(".mpo")) {
                MpoDecoder mpoDecoder = MpoDecoder.decodeFile(localFilePath);
                if (null == mpoDecoder) {
                    Log.e(TAG,"getStereoImage:failed to decode mpo file:"+
                              localFilePath);
                    return null;
                }
                try {
                    bitmap = getMpoFrame(jc, mpoDecoder, firstFrame,
                                         options, targetSize);
                } finally {
                    mpoDecoder.close();
                }
            } else if (localFilePath.toLowerCase().endsWith(".jps")) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(localFilePath);
                    FileDescriptor fd = fis.getFD();

                    Rect imageRect = generateRect(fd, mimeType, firstFrame, options);
                    Log.i(TAG,"getStereoImage:imageRect="+imageRect);
                    if (null != jc && jc.isCancelled()) return null;

                    options.inSampleSize = computeSampleSizeLarger(
                                imageRect.right - imageRect.left, 
                                imageRect.bottom - imageRect.top, targetSize, 0);
                    //Log.w(TAG,"getStereoImage:options.inSampleSize="+options.inSampleSize);

                    bitmap = getFlatternedImage(jc, fd, mimeType, firstFrame,
                                                options, imageRect);
                } catch (Exception ex) {
                    Log.w(TAG, ex);
                } finally {
                    Utils.closeSilently(fis);
                }
            }
        } else {
            Log.e(TAG,"getStereoImage:get null local path");
        }
        if (null != bitmap) {
            Log.i(TAG,"getStereoImage:["+bitmap.getWidth()+"x"+bitmap.getHeight()+"]");
        }
        return bitmap;
    }

    //Stretch half of original video frame as a whole video frame
    public static Bitmap getStereoVideoImage(JobContext jc, Bitmap originFrame, 
                                        boolean firstFrame, int mediaStoreLayout) {
        if (null == originFrame || originFrame.getWidth() <= 0 ||
            originFrame.getHeight() <= 0) {
            Log.e(TAG, "getStereoVideoImage:got invalid original frame");
            return null;
        }
        int localLayout = convertToLocalLayout(mediaStoreLayout);
        //Log.i(TAG,"getStereoVideoImage:localLayout="+localLayout);

        if (STEREO_LAYOUT_NONE == localLayout ||
            STEREO_LAYOUT_FULL_FRAME == localLayout && !firstFrame) {
            Log.e(TAG, "getStereoVideoImage:can not retrieve second image!");
            return null;
        }

        boolean isLeftRight = (STEREO_LAYOUT_LEFT_AND_RIGHT == localLayout) ||
                              (STEREO_LAYOUT_SWAP_LEFT_AND_RIGHT == localLayout);
        //change index for swapped type
        if (STEREO_LAYOUT_SWAP_LEFT_AND_RIGHT == localLayout ||
            STEREO_LAYOUT_SWAP_TOP_AND_BOTTOM == localLayout) {
            firstFrame = !firstFrame;
        }

        Rect src = new Rect(0, 0, originFrame.getWidth(), originFrame.getHeight());
        //Log.v(TAG,"getStereoVideoImage:src="+src);
        //create a new bitmap that has the same dimension as the original video
        Bitmap bitmap = Bitmap.createBitmap(src.right - src.left, 
                                           src.bottom - src.top,
                                           Bitmap.Config.ARGB_8888);
        adjustRect(isLeftRight, firstFrame, src);
        //Log.d(TAG,"getStereoVideoImage:src="+src);
        Canvas canvas = new Canvas(bitmap);
        RectF dst = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(originFrame, src, dst, null);

        if (null != bitmap) {
            Log.i(TAG,"getStereoVideoImage:["+bitmap.getWidth()+"x"+bitmap.getHeight()+"]");
        }
        return bitmap;
    }

    public static Bitmap getFlatternedImage(JobContext jc, FileDescriptor fd, 
                             String mimeType, boolean firstFrame, Options options,
                             Rect imageRect) {
        //Log.i(TAG,"getFlatternedImage(fd="+fd+",imageRect="+imageRect+
        //                              ",firstFrame="+firstFrame+"..)");
        if (null == fd) {// && null == mimeType) {
            Log.e(TAG,"getFlatternedImage:invalid parameters");
        }

        if (options == null) options = new Options();

        Bitmap flatternedImage = null;

        BitmapRegionDecoder mRegionDecoder =
                                 DecodeUtils.requestCreateBitmapRegionDecoder(
                                                  null, fd, false);
        //Log.v(TAG, "getFlatternedImage:got mRegionDecoder="+mRegionDecoder);
        if (null != jc && jc.isCancelled()) return null;
        if (null != mRegionDecoder) {
            if (null == imageRect) {
                imageRect = generateRect(fd, mimeType, firstFrame, options);
                if (jc.isCancelled()) return null;
            }
            flatternedImage = safeDecodeImageRegion(jc, mRegionDecoder,
                                                    imageRect, options);
        } else {
            Log.w(TAG,"getFlatternedImage:we have to decode using BitmapFactory..");
        }

        return flatternedImage;        
    }

    public static Bitmap getFlatternedImage(JobContext jc, FileDescriptor fd,
                             String mimeType, boolean firstFrame, Options options) {
        //Log.i(TAG,"getFlatternedImage(fd="+fd+",mimeType="+mimeType+
        //                              ",firstFrame="+firstFrame+"..)");
        if (null == fd) {// && null == mimeType) {
            Log.e(TAG,"getFlatternedImage:invalid parameters");
        }

        if (options == null) options = new Options();

        Bitmap flatternedImage = null;

        BitmapRegionDecoder mRegionDecoder =
                                 DecodeUtils.requestCreateBitmapRegionDecoder(
                                                  null, fd, false);
        //Log.v(TAG, "getFlatternedImage:got mRegionDecoder="+mRegionDecoder);
        if (null != jc && jc.isCancelled()) return null;
        if (null != mRegionDecoder) {
            Rect imageRect = generateRect(fd, mimeType, firstFrame, options);
            if (null != jc && jc.isCancelled()) return null;
            flatternedImage = safeDecodeImageRegion(jc, mRegionDecoder,
                                                    imageRect, options);
        } else {
            Log.w(TAG,"getFlatternedImage:we have to decode using BitmapFactory..");
        }

        return flatternedImage;        
    }

    public static Bitmap getFlatternedImage(String filePath, String mimeType,
                                    boolean firstFrame, Options options) {
        //Log.i(TAG,"getFlatternedImage(filePath="+filePath+",mimeType="+mimeType+
        //                              ",firstFrame="+firstFrame+"..)");
        if (null == filePath && null == mimeType) {
            Log.e(TAG,"getFlatternedImage:invalid parameters");
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return getFlatternedImage(null, fd, mimeType, firstFrame, options);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    //this function return part of origin image as a Bitmap. It may shrink the
    //Bitmap size in case that out of memory happens
    public static Bitmap safeDecodeImageRegion(JobContext jc, BitmapRegionDecoder
                              regionDecoder, Rect imageRect, Options options) {
        if (null == regionDecoder || null == imageRect) {
            Log.e(TAG,"safeDecodeImageRegion:invalid region decoder or rect");
            return null;
        }
        if (options == null) options = new Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;

        Bitmap bmp = null;

        if (null != jc && jc.isCancelled()) return null;

        //add protection in case rect is not valid
        try {
            try {
                Log.i(TAG,"safeDecodeImageRegion:decodeRegion(rect="+imageRect+"..)");
                bmp = regionDecoder.decodeRegion(imageRect, options);
                //Log.v(TAG,"safeDecodeImageRegion:decodeRegion() returns "+bmp);
            } catch (OutOfMemoryError e) {
                Log.w(TAG,"safeDecodeImageRegion:out of memory when decoding:"+e);
                bmp = null;
            }
            //As there is a chance no enough dvm memory for decoded Bitmap,
            //Skia will return a null Bitmap. In this case, we have to
            //downscale the decoded Bitmap by increase the options.inSampleSize
            if (null == bmp) {
                final int maxTryNum = 8;
                for (int i = 0; i < maxTryNum; i++) {
                    if (null != jc && jc.isCancelled()) return null;
                    //we increase inSampleSize to expect a smaller Bitamp
                    options.inSampleSize *= 2;
                    Log.w(TAG,"safeDecodeImageRegion:try for sample size " +
                              options.inSampleSize);
                    try {
                        //Log.i(TAG,"safeDecodeImageRegion:decodeRegion(rect="+imageRect+"..)");
                        bmp = regionDecoder.decodeRegion(imageRect, options);
                        //Log.v(TAG,"safeDecodeImageRegion:decodeRegion() returns "+bmp);
                    } catch (OutOfMemoryError e) {
                        Log.w(TAG,"safeDecodeImageRegion:out of memory when decoding:"+e);
                        bmp = null;
                    }
                    if (null != bmp) break;
                }
                if (null == bmp) {
                    Log.e(TAG,"safeDecodeImageRegion:failed to get a Bitmap");
                }
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG,"safeDecodeImageRegion:got exception:"+e);
        }
        if (null != bmp) {
            Log.i(TAG,"safeDecodeImageRegion:["+bmp.getWidth()+"x"+bmp.getHeight()+"]");
        }
        return bmp;
    }

    public static Rect generateRect(FileDescriptor fd, String mimeType,
                                    boolean firstFrame, Options options) {
        if (null == fd && null == mimeType) {
            Log.e(TAG,"generateRect:invalid parameters");
        }

        //actually, there is a "layout" tag in the specified JPS specification,
        //and we should parse the origion file to get this layout, and decide
        //which part of this image is dedicated firstFrame, and return its Rect.
        boolean isLeftRight = true;

        Rect imageRect = decodeImageRect(fd, options);

        adjustRect(isLeftRight, firstFrame, imageRect);

        return imageRect;
    }

    public static Rect generateRect(String filePath, String mimeType,
                                    boolean firstFrame, Options options) {
        //Log.v(TAG,"generateRect(filePath="+filePath+",mimeType="+mimeType+")");
        if (null == filePath && null == mimeType) {
            Log.e(TAG,"generateRect:invalid parameters");
        }

        //actually, there is a "layout" tag in the specified JPS specification,
        //and we should parse the origion file to get this layout, and decide
        //which part of this image is dedicated firstFrame, and return its Rect.
        boolean isLeftRight = true;

        Rect imageRect = decodeImageRect(filePath, options);

        adjustRect(isLeftRight, firstFrame, imageRect);

        return imageRect;
    }

    private static void adjustRect(boolean isLeftRight,boolean firstFrame, 
                                  Rect imageRect) {
        //Log.i(TAG,"adjustRect:got imageRect: "+imageRect);
        if (null == imageRect) {
            Log.e(TAG,"adjustRect:got null image rect");
            return;
        }
        if (isLeftRight) {
            if (firstFrame) {
                imageRect.set(imageRect.left, imageRect.top,
                              (imageRect.left + imageRect.right) / 2, 
                              imageRect.bottom);
            } else {
                imageRect.set((imageRect.left + imageRect.right) / 2, 
                              imageRect.top, imageRect.right, imageRect.bottom);
            }
        } else {
            if (firstFrame) {
                imageRect.set(imageRect.left, imageRect.top,
                              imageRect.right, 
                              (imageRect.top + imageRect.bottom) / 2);
            } else {
                imageRect.set(imageRect.left, 
                              (imageRect.top + imageRect.bottom) / 2, 
                              imageRect.right, imageRect.bottom);
            }
        }
        //Log.d(TAG,"adjustRect:adjusted imageRect: "+imageRect);
    }

    public static int adjustDim(boolean dimX, int layout, int length) {
        if (dimX) {
            //for dimension x, we have to check left-and-right layout
            if (STEREO_LAYOUT_LEFT_AND_RIGHT == layout) {
                return length / 2;
            } else {
                return length;
            }
        } else {
            if (STEREO_LAYOUT_TOP_AND_BOTTOM == layout) {
                return length / 2;
            } else {
                return length;
            }
        }
    }

    public static Rect decodeImageRect(FileDescriptor fd, Options options) {
        if (null == fd) {
            Log.e(TAG,"decodeImageRect:got null FileDescriptor");
            return null;
        }

        if (options == null) options = new Options();

        //save value that we will modified
        boolean temp = options.inJustDecodeBounds;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        //restore the value we changed
        options.inJustDecodeBounds = temp;

        return getRect(options);
    }

    public static Rect decodeImageRect(String filePath, Options options) {
        if (null == filePath) {
            Log.e(TAG,"decodeImageRect:got null filePath");
            return null;
        }

        if (options == null) options = new Options();

        //save value that we will modified
        boolean temp = options.inJustDecodeBounds;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        //restore the value we changed
        options.inJustDecodeBounds = temp;

        return getRect(options);
    }

    private static Rect getRect(Options options) {
        if (options == null) {
            Log.e(TAG,"getRect:got null options");
            return null;
        }

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Log.e(TAG,"getRect:got invalid bitmap dimension!");
            return null;
        }
        //Log.i(TAG,"getRect:["+options.outWidth+" x "+options.outHeight+"]");

        return new Rect(0, 0, options.outWidth, options.outHeight);
    }

}
