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

package com.android.camera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import com.mediatek.mpo.MpoDecoder;

import java.io.File;
import java.io.FileOutputStream;

public class Storage {
    private static final String TAG = "CameraStorage";
    private static StorageManager sStorageManager;

    public static String MOUNT_POINT = "/mnt/sdcard";

    public static String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

    public static String DIRECTORY = DCIM + "/Camera";

    // Match the code in MediaProvider.computeBucketValues().
    public static String BUCKET_ID =
            String.valueOf(DIRECTORY.toLowerCase().hashCode());

    public static final long UNAVAILABLE = -1L;
    public static final long PREPARING = -2L;
    public static final long UNKNOWN_SIZE = -3L;
    public static final long FULL_SDCARD = -4L;
    public static final long LOW_STORAGE_THRESHOLD= 50000000;
    public static final long RECORD_LOW_STORAGE_THRESHOLD = 48000000;
    public static final long PICTURE_SIZE = 1500000;

    private static final int BUFSIZE = 4096;

	//mtk migration start
    public static final int CANNOT_STAT_ERROR = -2;
    public static final int PICTURE_TYPE_JPG = 0;
    public static final int PICTURE_TYPE_MPO = 1;
    public static final int PICTURE_TYPE_JPS = 2;
    public static final int PICTURE_TYPE_MPO_3D = 3;
    public static String DIRECTORY_3D = DCIM + "/Camera3D";
    public static String BUCKET_ID_3D =
        String.valueOf(DIRECTORY_3D.toLowerCase().hashCode());
    private static final String JPEG_NAME_EXT = ".jpg";
    private static final String JPS_NAME_EXT = ".jps";
    public static final String MPO_NAME_EXT = ".mpo";

	public static int getSize(String key) {
		return PICTURE_SIZE_TABLE.get(key);
	}

    /* use estimated values for picture size (in Bytes)*/
    static final DefaultHashMap<String, Integer>
            PICTURE_SIZE_TABLE = new DefaultHashMap<String, Integer>();

    static {
    	PICTURE_SIZE_TABLE.put("3264x2448-normal", 696320);
    	PICTURE_SIZE_TABLE.put("3264x2448-fine", 696320);
    	PICTURE_SIZE_TABLE.put("3264x2448-superfine", 870400);
    	
        PICTURE_SIZE_TABLE.put("2592x1944-normal", 327680);
        PICTURE_SIZE_TABLE.put("2592x1944-fine", 491520);
        PICTURE_SIZE_TABLE.put("2592x1944-superfine", 614400);
        
        PICTURE_SIZE_TABLE.put("2560x1920-normal", 327680);
        PICTURE_SIZE_TABLE.put("2560x1920-fine", 491520);
        PICTURE_SIZE_TABLE.put("2560x1920-superfine", 614400);
        
        PICTURE_SIZE_TABLE.put("2048x1536-normal", 262144);
        PICTURE_SIZE_TABLE.put("2048x1536-fine", 327680);
        PICTURE_SIZE_TABLE.put("2048x1536-superfine", 491520);
        
        PICTURE_SIZE_TABLE.put("1600x1200-normal", 204800);
        PICTURE_SIZE_TABLE.put("1600x1200-fine", 245760);
        PICTURE_SIZE_TABLE.put("1600x1200-superfine", 368640);
        
        PICTURE_SIZE_TABLE.put("1280x960-normal", 163840);
        PICTURE_SIZE_TABLE.put("1280x960-fine", 196608);
        PICTURE_SIZE_TABLE.put("1280x960-superfine", 262144);
        
        PICTURE_SIZE_TABLE.put("1024x768-normal", 102400);
        PICTURE_SIZE_TABLE.put("1024x768-fine", 122880);
        PICTURE_SIZE_TABLE.put("1024x768-superfine", 163840);
        
        PICTURE_SIZE_TABLE.put("640x480-normal", 30720);
        PICTURE_SIZE_TABLE.put("640x480-fine", 30720);
        PICTURE_SIZE_TABLE.put("640x480-superfine", 30720);
        
        PICTURE_SIZE_TABLE.put("320x240-normal", 13312);
        PICTURE_SIZE_TABLE.put("320x240-fine", 13312);
        PICTURE_SIZE_TABLE.put("320x240-superfine", 13312);
        
        PICTURE_SIZE_TABLE.put("mav", 1036288);
        PICTURE_SIZE_TABLE.put("autorama", 163840);
        
        PICTURE_SIZE_TABLE.putDefault(1500000);
    }

    public static void updateDefaultDirectory(Activity activity, boolean stillCapture) {
    	String defaultPath = StorageManager.getDefaultPath();
    	MOUNT_POINT = defaultPath;
    	Log.i(TAG, "Write default path =" + defaultPath);

    	if (Util.CU) {
    		DCIM = stillCapture ? (MOUNT_POINT + "/Photo") : (MOUNT_POINT + "/Video");
    		DIRECTORY = DCIM;
    		DIRECTORY_3D = DCIM;
    	} else {
    		DCIM = MOUNT_POINT + "/DCIM";
    		DIRECTORY = DCIM + "/Camera";
    		DIRECTORY_3D = DCIM + "/Camera3D";
    	}
    	BUCKET_ID = String.valueOf(DIRECTORY.toLowerCase().hashCode());
    }
    
    public static String getImageBuketNameId(Activity activity, boolean stillCapture) {
    	String path = null;
        String[] projection = new String[] {"id", "category", "path"};
        Uri allCategory = Uri.parse("content://com.mediatek.filemanager.provider");
        
        if (stillCapture) {
        	Cursor cursor = activity.getContentResolver().query(allCategory, projection, "photo", null, null);
        	if (cursor != null && cursor.moveToFirst()) {
                do {
                	path = cursor.getString(cursor.getColumnIndex("path"));
                } while (cursor.moveToNext());
            }
        	if (cursor != null) {
        		cursor.close();
        	}
        } else {
	        Cursor cursor = activity.getContentResolver().query(allCategory, projection, "video", null, null);
	        if (cursor != null && cursor.moveToFirst()) {
	            do {
	            	path = cursor.getString(cursor.getColumnIndex("path"));
	            } while (cursor.moveToNext());
	        }
	    	if (cursor != null) {
	    		cursor.close();
	    	}
        }
        return path;
    }

    public static Uri addImage(ContentResolver resolver, String title, long date,
                Location location, int orientation, int width, int height, int type) {
        String path = generateFilepath(title, type);
        File file = new File(path);

        int stereoType = (Storage.PICTURE_TYPE_MPO_3D == type)
                ? MediaStore.Images.Media.STEREO_TYPE_SIDE_BY_SIDE
                : MediaStore.Images.Media.STEREO_TYPE_2D;
        return insertToMeidaProvider(resolver, title, type, date, orientation, path, file.length(),
                width, height, stereoType, location);
    }

    public static String generateMpoFilepath(String title) {
        return DIRECTORY + '/' + title + MPO_NAME_EXT;
    }

    public static boolean checkMountPoint(StorageVolume volume) {
    	if (volume == null) return true;
    	return MOUNT_POINT.equals(volume.getPath());
    }

    private static Uri insertToMeidaProvider(ContentResolver resolver, String title, int type, long date,
            int orientation, String path, long size, int width, int height,
            int stereoType, Location location) {

        // Insert into MediaStore.
        ContentValues values = new ContentValues(11);
        values.put(ImageColumns.TITLE, title);
        values.put(ImageColumns.DISPLAY_NAME, generateFileName(title, type));
        values.put(ImageColumns.DATE_TAKEN, date);
        values.put(ImageColumns.MIME_TYPE, generateMimetype(title, type));
        values.put(ImageColumns.ORIENTATION, orientation);
        values.put(ImageColumns.DATA, path);
        values.put(ImageColumns.SIZE, size);
        values.put(ImageColumns.WIDTH, width);
        values.put(ImageColumns.HEIGHT, height);
        values.put(MediaStore.Images.Media.MPO_TYPE, generateMpoType(type));
        values.put(MediaStore.Images.Media.STEREO_TYPE, stereoType);

        if (location != null) {
            values.put(ImageColumns.LATITUDE, location.getLatitude());
            values.put(ImageColumns.LONGITUDE, location.getLongitude());
        }

        Uri uri = null;
        try {
            uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }

    public static String getVideoBucketID() {
    	if (Util.CU) {
    		String defaultPath = StorageManager.getDefaultPath();
    		String path = defaultPath + "/Video";
    		return String.valueOf(path.toLowerCase().hashCode());
    	}
    	return BUCKET_ID;
    }

    public static String getImageBucketID() {
    	if (Util.CU) {
    		String defaultPath = StorageManager.getDefaultPath();
    		String path = defaultPath + "/Photo";
    		return String.valueOf(path.toLowerCase().hashCode());
    	}
    	return BUCKET_ID;
    }
	//mtk migration end

    public static Uri addImage(ContentResolver resolver, String title, long date,
                Location location, int orientation, byte[] jpeg, int width, int height, int type, int stereoType) {
        // Save the image.
        String path = generateFilepath(title, type);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(jpeg);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write image", e);
            return null;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }

        return insertToMeidaProvider(resolver, title, type, date, orientation, path,
                jpeg.length, width, height, stereoType, location);
    }

    private static int generateMpoType(int type) {
        if (type == PICTURE_TYPE_MPO) {
            return MpoDecoder.MTK_TYPE_MAV;
        } else if (type == PICTURE_TYPE_MPO_3D) {
            return MpoDecoder.MTK_TYPE_Stereo;
        } else {
            return MpoDecoder.MTK_TYPE_NONE;
        }
    }

    public static int generateStereoType(String stereoType) {
        if (Parameters.STEREO3D_TYPE_SIDEBYSIDE.equals(stereoType)) {
            return MediaStore.ThreeDimensionColumns.STEREO_TYPE_SIDE_BY_SIDE;
        } else if (Parameters.STEREO3D_TYPE_TOPBOTTOM.equals(stereoType)) {
            return MediaStore.ThreeDimensionColumns.STEREO_TYPE_TOP_BOTTOM;
        } else if (Parameters.STEREO3D_TYPE_FRAMESEQ.equals(stereoType)) {
            return MediaStore.ThreeDimensionColumns.STEREO_TYPE_FRAME_SEQUENCE;
        } else {
            return MediaStore.ThreeDimensionColumns.STEREO_TYPE_2D;
        }
    }

    public static String generateFileName(String title, int type) {
        if (type == PICTURE_TYPE_MPO || type == PICTURE_TYPE_MPO_3D) {
            return title + MPO_NAME_EXT;
        } else if (type == PICTURE_TYPE_JPS) {
            return title + JPS_NAME_EXT;
        } else {
            return title + JPEG_NAME_EXT;
        }
    }

    public static String generateMimetype(String title, int type) {
        if (type == PICTURE_TYPE_MPO || type == PICTURE_TYPE_MPO_3D) {
            return "image/mpo";
        } else if (type == PICTURE_TYPE_JPS) {
            return "image/x-jps";
        } else {
            return "image/jpeg";
        }
    }

    public static String generateFilepath(String title) {
        return DIRECTORY + '/' + title + JPEG_NAME_EXT;
    }

    public static String generateFilepath(String title, int type) {
        if (type == PICTURE_TYPE_MPO) {
            return DIRECTORY + '/' + title + MPO_NAME_EXT;
        } else if (type == PICTURE_TYPE_JPS) {
            return DIRECTORY_3D + '/' + title + JPS_NAME_EXT;
        } else if (type == PICTURE_TYPE_MPO_3D) {
            return DIRECTORY_3D + '/' + title + MPO_NAME_EXT;
        } else {
            return DIRECTORY + '/' + title + JPEG_NAME_EXT;
        }
    }

    public static long getAvailableSpace() {
    	String state;
    	if (sStorageManager == null) {
    		HandlerThread handler = new HandlerThread("StorageManager");
    		try {
				sStorageManager = new StorageManager(handler.getLooper());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	}
    	state = sStorageManager.getVolumeState(MOUNT_POINT);
        Log.d(TAG, "External storage state=" + state + ", mount point = " + MOUNT_POINT);
        if (Environment.MEDIA_CHECKING.equals(state)) {
            return PREPARING;
        }
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return UNAVAILABLE;
        }

        File dir = new File(DIRECTORY);
        dir.mkdirs();
        if (!dir.isDirectory() || !dir.canWrite()) {
            return FULL_SDCARD;
        }
        dir = new File(DIRECTORY_3D);
        dir.mkdirs();
        if (!dir.isDirectory() || !dir.canWrite()) {
            return FULL_SDCARD;
        }

        try {
            StatFs stat = new StatFs(DIRECTORY);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage", e);
        }
        return UNKNOWN_SIZE;
    }

    /**
     * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
     * imported. This is a temporary fix for bug#1655552.
     */
    public static void ensureOSXCompatible() {
        File nnnAAAAA = new File(DCIM, "100ANDRO");
        if (!(nnnAAAAA.exists() || nnnAAAAA.mkdirs())) {
            Log.e(TAG, "Failed to create " + nnnAAAAA.getPath());
        }
    }

    public static boolean isSDCard(){
        if (sStorageManager == null) {
            HandlerThread handler = new HandlerThread("StorageManager");
            try {
                sStorageManager = new StorageManager(handler.getLooper());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        String storagePath = sStorageManager.getDefaultPath();
        StorageVolume[] volumes = sStorageManager.getVolumeList();
        int nVolume = -1;
        for(int i = 0; i < volumes.length; i++){
            if(volumes[i].getPath().equals(storagePath)) {
                nVolume = i;
                break;
            }
        }
        return volumes[nVolume].isRemovable();
    }

    public static boolean isMultiStorage(){
        if (sStorageManager == null) {
            HandlerThread handler = new HandlerThread("StorageManager");
            try {
                sStorageManager = new StorageManager(handler.getLooper());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        StorageVolume[] volumes = sStorageManager.getVolumeList();
        return volumes.length > 1;
    }

    public static boolean isHaveExternalSDCard(){
        if (sStorageManager == null) {
            HandlerThread handler = new HandlerThread("StorageManager");
            try {
                sStorageManager = new StorageManager(handler.getLooper());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        StorageVolume[] volumes = sStorageManager.getVolumeList();
        for(int i = 0; i < volumes.length; i++){
            if(volumes[i].isRemovable() && sStorageManager.getVolumeState(volumes[i].getPath()).equals(Environment.MEDIA_MOUNTED)) {
                return true;
            }
        }
        return false;
    }   
}
