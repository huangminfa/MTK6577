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

package com.mediatek.backuprestore;


import android.content.Context;
import android.os.Looper;
import android.os.PowerManager.WakeLock;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;


public class BackupRestoreUtils {

    public static String getStoragePath()
    {
        String storagePath = null;
        StorageManager storageManager = null;
        try {
            storageManager = new StorageManager(Looper.getMainLooper());
        } catch(RemoteException e) {
            return null;
        }

        StorageVolume[] volumes = storageManager.getVolumeList();
        if (volumes != null) {
            for(StorageVolume volume : volumes) {
                if(volume.isRemovable()) {
                    String path = volume.getPath();
                    if(path != null && !path.matches("/mnt/usbotg")) {
                        storagePath = path + File.separator + ".backup";
                        break;
                    }
                }
            }
        }

        if(storagePath == null) {
            return null;
        }

        File file = new File(storagePath);
        if (file != null) {
            if (file.exists() && file.isDirectory()) {
                File temp = new File(storagePath + File.separator + ".BackupRestoretemp");
                boolean ret;
                if (temp.exists()){
                    ret = temp.delete();
                }else{
                    try{
                        ret = temp.createNewFile();
                    }catch(IOException e){
                        e.printStackTrace();
                        Log.e(LogTag.FILE, e.getMessage());
                        ret = false;
                    } finally {
                        temp.delete();
                    }
                }
                if (ret){
                    return storagePath;
                }else{
                    return null;
                }
                
            } else if (file.mkdir()) {
                return storagePath;
            }
        }
        return null;
    }

    static public boolean isSdCardAvailable(){
        return (getStoragePath()!= null);
    }
    
    static public int getAvailableSize(String file){
      android.os.StatFs stat = new android.os.StatFs(file);
      int count = stat.getAvailableBlocks();
      int size = stat.getBlockSize();
      return count * size;
  }
    
    static String getModuleStringFromType(Context context, int type){
        int resId = 0;
        switch(type){
        case ModuleType.TYPE_CONTACT:
            resId = R.string.contact_module;
            break;
        
        case ModuleType.TYPE_MESSAGE:
            resId = R.string.message_module;
            break;
        
        case ModuleType.TYPE_CALENDAR:
            resId = R.string.calendar_module;
            break;
            
        case ModuleType.TYPE_PICTURE:
            resId = R.string.picture_module;
            break;
            
        case ModuleType.TYPE_APP:
            resId = R.string.app_module;
            break;

        case ModuleType.TYPE_MUSIC:
            resId = R.string.music_module;
            break;

        case ModuleType.TYPE_NOTEBOOK:
            resId = R.string.notebook_module;
            break;
        }
        Log.v(LogTag.COMMON, "resId = " + resId);
        return context.getResources().getString(resId);            
    }

    public static class ModuleType {
        public static final int TYPE_CONTACT = 0x1;

        public static final int TYPE_SMS = 0x2;

        public static final int TYPE_MMS = 0x4;

        public static final int TYPE_CALENDAR = 0x8;

        public static final int TYPE_APP = 0x10;

        public static final int TYPE_PICTURE = 0x20;

        public static final int TYPE_MESSAGE = 0x40;

        public static final int TYPE_MUSIC = 0x80;

        public static final int TYPE_NOTEBOOK = 0x100;
    }


    public static double round(double value, int scale, int roundingMode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        double d = bd.doubleValue();
        bd = null;
        return d;
    }
    
    public static class Consts{
        public static final String BackupFileExt = "zip";
        public static final String BACKUP_FOLDER_NAME = ".backup";
    }
    
   
    public static class LogTag {
        public static String UTILS = "utils";

        public static String FILE = "FILE";

        public static String BACKUP = "BACKUP";

        public static String RESTORE = "RESTORE";

        public static String COMMON = "COMMON";

    }
    
    
    
    public static class ScreenLock {

        WakeLock mWakeLock;
        private ScreenLock() {

        }
        private static ScreenLock mIncetance = null;
        public static ScreenLock instance() {
            if (mIncetance == null) {
                mIncetance = new ScreenLock();
            }
            return mIncetance;
        }

        /**
         * keep screen always on
         * @param context
         */
        public void acquireWakeLock(Context context) {

            if (mWakeLock == null) {
                Log.d(LogTag.COMMON, "Acquiring wake lock");
                PowerManager pm = (PowerManager) context
                        .getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
                        .getClass().getCanonicalName());
            }
            mWakeLock.acquire();
        }

        /**
         * release screen always on
         */
        public void releaseWakeLock() {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
            }

        }
    }
    
    public static class FileUtils {

        

        public static boolean isSdCardAvailable() {
//          String str = Environment.getExternalStorageState();
//            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
//                    && checkFsWritable();
//          return false;
//            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            return (getStoragePath()!= null);
        }

        
        public static String getDisplaySize(long bytes, Context context) {
            String displaySize = context.getString(R.string.unknow);
            long iKb = bytes2KB(bytes);
            if (iKb == 0 && bytes > 0) {
                // display "less than 1KB"
                displaySize = context.getString(R.string.less_1K);
            } else if (iKb >= 1024) {
                // diplay MB
                double iMb = ((double) iKb) / 1024;
                iMb = round(iMb, 2, BigDecimal.ROUND_UP);
                StringBuilder builder = new StringBuilder(new Double(iMb).toString());
                builder.append("MB");
                displaySize = builder.toString();
            } else {
                // display KB
                StringBuilder builder = new StringBuilder(new Long(iKb).toString());
                builder.append("KB");
                displaySize = builder.toString();
            }
            return displaySize;
        }

       
        /**
         * create files
         * 
         * @param filePath
         * @return
         */
//        public static File createFile(String filePath) {
//            File file = null;
//            File tmpFile = new File(filePath);
//            if (createFile(tmpFile)) {
//                file = tmpFile;
//            }
//            return file;
//        }

        /**
         * create the file
         * 
         * @param file
         * @return
         */
//        public static boolean createFile(File file) {
//            boolean bSuccess = true;
//            if (file != null) {
//
//                File dir = file.getParentFile();
//                if (dir != null && !dir.exists()) {
//                    dir.mkdirs();
//                }
//
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    bSuccess = false;
//                    Log.d(LogTag.FILE, "createFile() failed !cause:" + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//            return bSuccess;
//        }

        /**
         * see if the file exsit
         * 
         * @param filePath
         * @return
         */
//        public static boolean isFileExist(String filePath) {
//            File file = new File(filePath);
//            return file.exists();
//        }


        public static String getNameWithoutExt(String fileName) {
            String nameWithoutExt = fileName;
            int iExtPoint = fileName.lastIndexOf(".");
            if (iExtPoint != -1) {
                nameWithoutExt = fileName.substring(0, iExtPoint);
            }
            return nameWithoutExt;
        }

   
        public static long bytes2MB(long bytes) {
            return bytes2KB(bytes) / 1024;
        }

        public static long bytes2KB(long bytes) {
            return bytes / 1024;
        }

        /**
         * return the filename's ext
         * 
         * @param file
         * @return
         */
        public static String getExt(File file) {
            if (file == null) {
                return null;
            }
            return getExt(file.getName());
        }

        /**
         * return the filename's ext
         * 
         * @param fileName
         * @return
         */
        public static String getExt(String fileName) {
            if (fileName == null) {
                return null;
            }
            String ext = null;

            int iLastOfPoint = fileName.lastIndexOf(".");
            if (iLastOfPoint != -1) {
                ext = fileName.substring(iLastOfPoint + 1, fileName.length());
            }
            return ext;
        }
    }
}
