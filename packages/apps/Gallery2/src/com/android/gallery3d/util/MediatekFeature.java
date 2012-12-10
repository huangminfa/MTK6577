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

import android.os.SystemProperties;
import android.os.Bundle;
import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.mpo.MpoDecoder;

import com.android.gallery3d.data.Path;

//this class is used to identify what features are added to Gallery3D
//by MediaTek Corporation
public class MediatekFeature {

    private static final String TAG = "MediatekFeature";

    //GIF animation is a feature developed by MediaTek. It avails Skia
    //to play GIF animation
    private static final boolean supportGifAnimation = true;

    //DRM (Digital Rights management) is developed by MediaTek.
    //Gallery3d avails MtkPlugin via android DRM framework to manage
    //digital rights of videos and images
    private static final boolean supportDrm = true;//to be replaced by FeatureOptions("DRM")

    //MPO (Multi-Picture Object) is series of 3D features developed by
    //MediaTek. Camera can shot MAV file or stereo image. Gallery is
    //responsible to list all mpo files add call corresponding module
    //to playback them.
    private static final boolean supportMpo = true;

    //Stereo Display
    private static final boolean supportStereoDisplay = MtkUtils.isSupport3d();//true;

    //This feature can stereoly display normal image. It takes advantage
    //of a special lib that can generate the right eye image from origin
    //(Origin image can be perceived as the left eye image)
    //The actual result depends on the outcome of the lib. So this feature
    //may be turned off if customer thinks this feature is not useful.
    private static final boolean supportDisplay2dAs3d = 
                                     supportStereoDisplay && true;

    //This feature can take advantage of algorithm that calculates which
    //parts of two stereo image matches, and provides info about how user
    //adjusting the two parts results in good stereo user experience
    private static final boolean supportStereoConvergence = //false;
                                     supportStereoDisplay && true;

    //MyFavorite is a short cut type application developed by MediaTek
    //It provides entries like Camera Photo, My Music, My Videos
    private static final boolean customizedForMyFavorite = true;

    //VLW stands for Video Live Wallpaper developed by MediaTek. It is 
    //like Live Wallpaper which can display dynamic wallpapers, by unlike
    //Live wallpaper, VLW's source is common video/videos, not OpenGL 
    //program
    private static final boolean customizedForVLW = true;

    // Media3D app requires to pick an image folder, so we've added corresponding
    // logic in AlbumPicker/AlbumSetPage to support this.
    // This feature shares the common flow with VLW.
    private static final boolean customizedForMedia3D = true;

    //Bluetooth Print feature avails Bluetooth capacity on MediaTek
    //platform to print specified kind of image to a printer.
    private static final boolean supportBluetoothPrint = 
                                        FeatureOption.MTK_BT_PROFILE_BPP;

    //Picture quality enhancement feature avails Camera ISP hardware
    //to improve image quality displayed on the screen.
    private static final boolean supportPictureQualityEnhance = true;

    // mobile phone operator 
    private static final String optr = SystemProperties.get("ro.operator.optr");
    // CMCC
    private static final String OPTR_CMCC = "OP01";

    public static boolean isCMCC() {
        return OPTR_CMCC.equals(optr);
    }

    public static boolean isGifAnimationSupported() {
        return supportGifAnimation;
    }

    public static boolean isDrmSupported() {
        return supportDrm;
    }

    public static boolean isMpoSupported() {
        return supportMpo;
    }

    public static boolean isStereoDisplaySupported() {
        return supportStereoDisplay;
    }

    public static boolean isDisplay2dAs3dSupported() {
        return supportDisplay2dAs3d;
    }

    public static boolean isStereoConvergenceSupported() {
        return supportStereoConvergence;
    }

    public static boolean hasCustomizedForMyFavorite() {
        return customizedForMyFavorite;
    }

    public static boolean hasCustomizedForVLW() {
        return customizedForVLW;
    }
    
    public static boolean hasCustomizedForMedia3D() {
        return customizedForMedia3D;
    }

    public static boolean isBluetoothPrintSupported() {
        return supportBluetoothPrint;
    }

    public static boolean isPictureQualityEnhanceSupported() {
        return supportPictureQualityEnhance;
    }

    //the following are variables or settings for Mediatek feature

    //gif background color
    private static final int gifBackGroundColor = 0xFFFFFFFF;

    public static int getGifBackGroundColor() {
        return gifBackGroundColor;
    }

    //OpenGL max texture Width and Height is 2048 Because of OpenGL limit, so we should restrict the bitmap range
    private static final int maxTextureSize = 2048;
    
    public static int getMaxTextureSize() {
        return maxTextureSize;
    }

    //this variable indicates how many CPU cores are there in the device.
    //It is used for fine tune ThreadPool performance.
    private static final int cpuCoreNum = 2;

    public static int getCpuCoreNum() {
        return cpuCoreNum;
    }

    public static class Params {
        public Params() {
            inPQEnhance = false;
            inOriginalFrame = true;
            inFirstFrame = false;
            inSecondFrame = false;
            inFirstFullFrame = false;
            inSecondFullFrame = false;
            inSampleDown = true;
            inMpoFrames = false;
            inTargetDisplayWidth = 0;
            inTargetDisplayHeight = 0;
            inRotation = 0;
        }
        //added for picture quality enhancement, indicating whether image
        //is enhanced
        public boolean inPQEnhance;
        //added for stereo display feature, indicating whether we want the
        //original frame. For 2D to 3D function, original frame is different
        //from first frame. For Jps & Mpo image, original frame equals
        //first frame.
        public boolean inOriginalFrame;
        //added for stereo display feature, indicating whether we want the
        //logical first frame
        public boolean inFirstFrame;
        //added for stereo display feature, indicating whether we want the
        //logical second frame. 
        public boolean inSecondFrame;
        //added for stereo display feature, indicating whether we want the
        //logical first full frame
        public boolean inFirstFullFrame;
        //added for stereo display feature, indicating whether we want the
        //logical second full frame
        public boolean inSecondFullFrame;
//        //added for stereo display feature, indicating whether we want retrieve
//        //the auto/manual convergence data according to stereo pair
//        public boolean inConvergenceData;
        //tell if the decoded Bitmap is to be sampled down the same as the
        //cache
        public boolean inSampleDown;

        //tell if MPO frames should be retrieved
        public boolean inMpoFrames;

        //the two variables tells how large the decoded image will be
        //displayed on the screen. Currently, this variable is used only
        //for mpo.
        public int inTargetDisplayWidth;
        public int inTargetDisplayHeight;
        //image rotation, used for 2d to 3d algorithm
        public int inRotation;

        public void showInfo() {
            android.util.Log.i(TAG,"Params:inOriginalFrame="+inOriginalFrame);
            android.util.Log.d(TAG,"Params:inFirstFrame="+inFirstFrame);
            android.util.Log.v(TAG,"Params:inSecondFrame="+inSecondFrame);
            android.util.Log.d(TAG,"Params:inFirstFullFrame="+inFirstFullFrame);
            android.util.Log.v(TAG,"Params:inSecondFullFrame="+inSecondFullFrame);
            android.util.Log.d(TAG,"Params:inSampleDown="+inSampleDown);
        }
    }

    public static class RegionDecoder {
        public RegionDecoder() {}

        public void release() {
            jpegBuffer = null;
            regionDecoder = null;
        }

        public void showInfo() {
            android.util.Log.i(TAG,"RegionDecoder:jpegBuffer="+jpegBuffer);
            android.util.Log.d(TAG,"RegionDecoder:regionDecoder="+regionDecoder);
        }

        public byte[] jpegBuffer;
        public BitmapRegionDecoder regionDecoder;
    }

    public static class DataBundle {
        public DataBundle() {
            originalFrame = null;
            firstFrame = null;
            secondFrame = null;
            firstFullFrame = null;
            secondFullFrame = null;
            mpoFrames = null;
        }
        public void recycle() {
            if (null != originalFrame) originalFrame.recycle();
            if (null != firstFrame) firstFrame.recycle();
            if (null != secondFrame) secondFrame.recycle();
            if (null != firstFullFrame) firstFullFrame.release();
            if (null != secondFullFrame) secondFullFrame.release();
            if (null != mpoFrames) {
                for (int i = 0; i < mpoFrames.length; i++) {
                    if (null != mpoFrames[i]) {
                        mpoFrames[i].recycle();
                    }
                }
            }
            originalFrame = null;
            firstFrame = null;
            secondFrame = null;
            firstFullFrame = null;
            secondFullFrame = null;
            mpoFrames = null;
        }
        public void showInfo() {
            android.util.Log.i(TAG,"DataBundle:originalFrame="+originalFrame);
            android.util.Log.d(TAG,"DataBundle:firstFrame="+firstFrame);
            if (null != firstFrame) {
                android.util.Log.v(TAG,"DataBundle:firstFrame["+firstFrame.getWidth()+"x"+firstFrame.getHeight()+"]");
            }
            android.util.Log.d(TAG,"DataBundle:secondFrame="+secondFrame);
            if (null != firstFrame) {
                android.util.Log.v(TAG,"DataBundle:secondFrame["+secondFrame.getWidth()+"x"+secondFrame.getHeight()+"]");
            }
            android.util.Log.d(TAG,"DataBundle:firstFullFrame="+firstFullFrame);
            if (null != firstFullFrame) {
                firstFullFrame.showInfo();
            }
            android.util.Log.d(TAG,"DataBundle:secondFullFrame="+secondFullFrame);
            if (null != secondFullFrame) {
                secondFullFrame.showInfo();
            }
        }
        public Bitmap originalFrame;
        public Bitmap firstFrame;
        public Bitmap secondFrame;
        public RegionDecoder firstFullFrame;
        public RegionDecoder secondFullFrame;
        public Bitmap[] mpoFrames;
    }

    //Stere Photo Display
    public static final int STEREO_DISPLAY_INVALID_PASS = -1;
    public static final int STEREO_DISPLAY_LEFT_PASS = 1;
    public static final int STEREO_DISPLAY_RIGHT_PASS = 2;

    //Mediatek inclusion used in querying data base
    //So far, we have add two kinds of inclusion
    //inc1: add new mime types, such as mpo, jps
    //inc2: add new way to operate the files: drm protection
    //influnce can be express as follow:
    //The media sets that are supported by Google default Gallery
    //are named as set A
    //when we add inc1, we are adding an extra media sets to A,
    //let's name it as B
    //before we add inc2, all we have is A+B, an enlarge media set
    //when we add inc2 (the drm protection), we have a converted
    //media set A' and B'. From the name, we can tell A' represents
    //set A that protected by drm, and B' represents set B that
    //protected by drm.
    //Note:as OMA drm v1.0 that we support in Gallery emerged very
    //early, mpo and jps kind media may not be protected by drm
    //in practise. Therefore, media set B' is empty at present, and
    //may be not empty in the future.

    //now we have A,A',B,B' four kinds of set, each launching is
    //actually set operation. for example
    //case 1, when all features are support and Gallery launches
    //    normally, drm inclusions, mpo inclusions and stereo
    //    are all setted, then we query all medias.
    //case 2, when drm are not supported, and Gallery launches to
    //    query stereo, mpo and stereo inclusion should be set, then
    //    A and B will be queried out.
    //case 3, when drm are supported and MAV (subset of MPO) is not
    //    supported, and stereo are supported, drm inclusion is set,
    //    mav is not set, stereo are set. When quering, mav media
    //    with drm protection should not be queried out.
    //case 4, when user only want view stereo image and video, 
    //    (default pictures such as jpg, gif should not be exhibited
    //    then, exclude default media bit is setted to 1, include 3d
    //    pan and 3d bit is setted to 1.

    public static final int EXCLUDE_DEFAULT_MEDIA = (1 << 0);
    //below is intended for DRM kind media
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

    //below is intended for Stereo & MPO feature
    public static final int INCLUDE_MPO_MAV = (1 << 6);
    public static final int INCLUDE_MPO_3D  = (1 << 7);
    public static final int INCLUDE_MPO_3D_PAN = (1 << 8);
    public static final int INCLUDE_MPO_UNKNOWN = (1 << 9);
    public static final int ALL_MPO_MEDIA = INCLUDE_MPO_UNKNOWN |
                                            INCLUDE_MPO_3D_PAN |
                                            INCLUDE_MPO_3D |
                                            INCLUDE_MPO_MAV;

    public static final int INCLUDE_STEREO_JPS = (1 << 12);
    public static final int INCLUDE_STEREO_PNS = (1 << 13);

    public static final int INCLUDE_STEREO_VIDEO = (1 << 14);

    //This field represents a virtual folder, rather than media types.
    //If this bit is set, we will create a virtual folder (3D Media)
    //for user to use. Or, the virtual folder will be created
    public static final int INCLUDE_STEREO_FOLDER = (1 << 15);

    public static int getInclusionFromData(Bundle data) {
        return DrmHelper.getDrmInclusionFromData(data) |
               StereoHelper.getInclusionFromData(data);
    }

    public static String getWhereClause(int mtkInclusion) {
        //Log.i(TAG,"getWhereClause(mtkInclusion="+mtkInclusion+")");
        //added support for Streao display
        String whereClauseStereo = StereoHelper.getWhereClause(mtkInclusion);

        //added support for MPO and DRM

        String whereClauseDrm = null;
        if (supportDrm) {
            whereClauseDrm = DrmHelper.getDrmWhereClause(mtkInclusion);
        } else {
            whereClauseDrm = DrmHelper.getDrmWhereClause(DrmHelper.NO_DRM_INCLUSION);
        }

        String whereGroup = null;
        //if (null != whereClauseMpo) {
        //    whereGroup = whereClauseMpo;
        //}
        if (null != whereClauseDrm) {
            whereGroup = (null == whereGroup) ? whereClauseDrm :
                         "(" + whereGroup + ") AND (" + whereClauseDrm +")";
        }
        //added to erase folder that Gallery secretly created ".ConvertedTo2D"
        if (null != whereClauseStereo) {
            whereGroup = (null == whereGroup) ? whereClauseStereo :
                         "(" + whereGroup + ") AND (" + whereClauseStereo +")";
        }
        return whereGroup;
    }

    public static String getWhereClause(int mtkInclusion, boolean queryVideo) {
        //Log.i(TAG,"getWhereClause(mtkInclusion="+mtkInclusion+")");
        //added support for Streao display
        String whereClauseStereo = 
                     StereoHelper.getWhereClause(mtkInclusion, queryVideo);

        //added support for MPO and DRM

        String whereClauseDrm = null;
        if (supportDrm) {
            whereClauseDrm = DrmHelper.getDrmWhereClause(mtkInclusion);
        } else {
            whereClauseDrm = DrmHelper.getDrmWhereClause(DrmHelper.NO_DRM_INCLUSION);
        }

        String whereGroup = null;
        //if (null != whereClauseMpo) {
        //    whereGroup = whereClauseMpo;
        //}
        if (null != whereClauseDrm) {
            whereGroup = (null == whereGroup) ? whereClauseDrm :
                         "(" + whereGroup + ") AND (" + whereClauseDrm +")";
        }
        //added to erase folder that Gallery secretly created ".ConvertedTo2D"
        if (null != whereClauseStereo) {
            whereGroup = (null == whereGroup) ? whereClauseStereo :
                         "(" + whereGroup + ") AND (" + whereClauseStereo +")";
        }
        return whereGroup;
    }

    public static String getOnlyStereoWhereClause(int drmInclusion, boolean queryVideo) {
        int stereoInclusion = 0;
        if (queryVideo) {
            stereoInclusion |= INCLUDE_STEREO_VIDEO;
        } else {
            stereoInclusion |= INCLUDE_MPO_3D;
            stereoInclusion |= INCLUDE_MPO_3D_PAN;
            stereoInclusion |= INCLUDE_STEREO_JPS;
        }
        //exclude normal images/videos
        stereoInclusion |= EXCLUDE_DEFAULT_MEDIA;
        return getWhereClause(stereoInclusion | drmInclusion, queryVideo);
    }
    public static String getOnlyStereoWhereClause(int drmInclusion) {
        int stereoInclusion = 0;
        stereoInclusion |= INCLUDE_STEREO_VIDEO;
        stereoInclusion |= INCLUDE_MPO_3D;
        stereoInclusion |= INCLUDE_MPO_3D_PAN;
        stereoInclusion |= INCLUDE_STEREO_JPS;
        //exclude normal images/videos
        stereoInclusion |= EXCLUDE_DEFAULT_MEDIA;
        return getWhereClause(stereoInclusion | drmInclusion);
    }

    public static String getAddedMimetype(String extension) {
        if (null == extension) return null;
        if (MpoHelper.MPO_EXTENSION.equalsIgnoreCase(extension)) {
            return MpoHelper.MPO_MIME_TYPE;
        } else if (StereoHelper.JPS_EXTENSION.equalsIgnoreCase(extension)) {
            return StereoHelper.JPS_MIME_TYPE;
        }
        //for default format, we return null
        return null;
    }

    public static int getPhotoWidgetInclusion() {
        int mtkInclusion = 0;
        //if (MediatekFeature.isDrmSupported()) {
        //    //we only add fl kind drm image
        //    mtkInclusion |= MediatekFeature.INCLUDE_FL_DRM_MEDIA;
        //}
        if (MediatekFeature.isMpoSupported()) {
            mtkInclusion |= MediatekFeature.INCLUDE_MPO_MAV;
            if (MediatekFeature.isStereoDisplaySupported()) {
                mtkInclusion |= MediatekFeature.INCLUDE_MPO_3D;
                mtkInclusion |= MediatekFeature.INCLUDE_MPO_3D_PAN;
                mtkInclusion |= MediatekFeature.INCLUDE_STEREO_JPS;
                mtkInclusion |= MediatekFeature.INCLUDE_STEREO_PNS;
                //this is nasty!!! but as StereoHelper is not well
                //developped, we add the inclusion here
                mtkInclusion |= MediatekFeature.INCLUDE_STEREO_VIDEO;
            }
        } else {
            if (MediatekFeature.isStereoDisplaySupported()) {
                mtkInclusion |= MediatekFeature.INCLUDE_STEREO_PNS;
                //this is nasty!!! but as StereoHelper is not well
                //developped, we add the inclusion here
                mtkInclusion |= MediatekFeature.INCLUDE_STEREO_VIDEO;
            }
        }
        return mtkInclusion;
    }

    public static void drawImageTypeOverlay(Context context, Uri imgUri, 
                                            String mimeType, Bitmap bitmap) {
        if (null == context || null == imgUri || 
            null == mimeType || null == bitmap) {
            Log.e(TAG,"drawImageTypeOverlay:invalid params");
            return;
        }
        
        boolean isMAV = false;
        if (MediatekFeature.isMpoSupported() && 
            MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            MpoDecoder mpoDecoder = MpoDecoder.decodeUri(
                                        context.getContentResolver(), imgUri);
            if (mpoDecoder != null) {
                int mpoSubType = mpoDecoder.suggestMtkMpoType();
                if (mpoSubType == MpoDecoder.MTK_TYPE_MAV) {
                    isMAV = true;
                }
                mpoDecoder.close();
            }
        }

        boolean isStereoImage = false;
        if (MediatekFeature.isStereoDisplaySupported() && (
             StereoHelper.PNS_MIME_TYPE.equalsIgnoreCase(mimeType) ||
             StereoHelper.JPS_MIME_TYPE.equalsIgnoreCase(mimeType) ||
             (MpoHelper.MPO_MIME_TYPE.equalsIgnoreCase(mimeType) && !isMAV))) {
            isStereoImage = true;
        }

        if (isMAV) {
            // for MAV, draw MAV overlay over the original bitmap
            MpoHelper.drawImageTypeOverlay(context, bitmap);
        }
        
        if (isStereoImage) {
            // draw stereo3D overlay at the bottom left corner of the image
            StereoHelper.drawImageTypeOverlay(context, bitmap);
        }

        //if (isDrm) {..
    }

    public static Uri addMtkInclusion(Uri uri, Path path) {
        if (!supportDrm) return uri;
        if (null == uri || null == path) {
            Log.e(TAG, "addMtkInclusion:invalid parameter");
            return uri;
        }
        if (path.getMtkInclusion() != 0) {
            uri = uri.buildUpon().appendQueryParameter("mtkInclusion", 
                 String.valueOf(path.getMtkInclusion()))
                 .build();
            Log.i(TAG,"addMtkInclusion:uri="+uri);
        }
        return uri;
    }

    public static void initialize(Context context) {
        //DrmHelper.initialize(context);
        //MpoHelper.initialize(context);
        //StereoHelper.initialize(context);
        StereoConvertor.initialize(context);
    }

    public static void enablePictureQualityEnhance(Options options,
                           boolean suggestEnhance) {
        if (null == options) {
            return;
        }
        //we should consider both feature option and caller's suggestion
        if (supportPictureQualityEnhance && suggestEnhance) {
            options.inPostProc = true;
        } else {
            options.inPostProc = false;
        }
    }

    public static void enablePictureQualityEnhance(Params params,
                           boolean suggestEnhance) {
        if (null == params) {
            return;
        }
        //we should consider both feature option and caller's suggestion
        if (supportPictureQualityEnhance && suggestEnhance) {
            params.inPQEnhance = true;
        } else {
            params.inPQEnhance = false;
        }
    }
}
