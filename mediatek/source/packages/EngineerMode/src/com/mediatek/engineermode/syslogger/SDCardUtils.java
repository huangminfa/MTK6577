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

package com.mediatek.engineermode.syslogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;

import android.util.Log;

public class SDCardUtils {
    
    private static final String TAG = "Syslog_taglog";
    
    private static final String LOCALPATH = "/mnt/sdcard/mtklog/taglog";
    
//    public static final String PER_LOG2SD = "persist.sys.log2sd.defaultpath";
    public static final String PER_LOG2SD = "persist.radio.log2sd.path";
    public static final String EXTERNALSDPATH = "/mnt/sdcard2";
    
    public static void writeTagLog(String path, String newPath) {
        File file_in = new File(path);
        if (!file_in.exists()) {
            Log.e(TAG, "Log isn't exist!");
            return;
        }
        String fileName = file_in.getName();
        File file_out = new File(newPath + "/" + fileName);
        if (!file_out.exists()) {
            try{
            file_out.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
        Log.i(TAG, "create tag log file :" + file_out.getAbsolutePath());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file_in));
            BufferedWriter bw = new BufferedWriter(new FileWriter(file_out));
            // System.out.println(file.getAbsolutePath());
            boolean havePerpory = false;
            String result = br.readLine();
            while (null != result) {
                bw.write(result, 0, result.length());
                result = br.readLine();
                bw.flush();
            }
            br.close();
            bw.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    
//    public void getFilesUnder(String path,String tag) {
//        File dir = new File(path);
//
//        if (null == dir || !dir.exists()) {
//            return;
//        }
//
//        // Get all files and sub directories under the current directory
//        File[] files = dir.listFiles();
//        if (null == files) {
//            // Current file is not a directory
//            writeTagLog(dir.getAbsolutePath());
//            return;
//        } else {
//            // Current file is a directory
//            for (int i = 0; i < files.length; i++) {
//                getFilesUnder(files[i].getAbsolutePath(),tag);
//            }
//        }
//    }
    
    
    public static void writeTagLogFolder(String folderPath, String newPath) {
        File dir = new File(folderPath);

        if (null == dir || !dir.exists()) {
            return;
        }
        Log.i(TAG, "write Log to tag folder" + folderPath + "--> " + newPath);
        // Get all files and sub directories under the current directory
        File[] files = dir.listFiles();
        if (null == files) {
            // Current file is not a directory
            String tagLogPath = dir.getAbsolutePath();
            Log.i(TAG, "Log path: " + tagLogPath);
            writeTagLog(tagLogPath,newPath);
            return;
        } else {
            // Current file is a directory
            for (int i = 0; i < files.length; i++) {
                writeTagLogFolder(files[i].getAbsolutePath(),newPath);
            }
        }
    }
    
    /**
     * 
     * @param dbFolderPath ex:/mnt/sdcard/mtklog/aee_exp/db.00
     * @param tagLogFolderPath ex:/mnt/sdcard/mtklog/taglog/2012_5_31_1200_taglog
     */
    
    public static void writeFolderToTagFolder(String dbFolderPath,
            String tagLogFolderPath) {
        File dir = new File(dbFolderPath);

        if (null == dir || !dir.exists()) {
            return;
        }
        Log.i(TAG, "write Log to tag folder" + dbFolderPath + "--> "
                + tagLogFolderPath);
        // Get all files and sub directories under the current directory
        File[] files = dir.listFiles();
        if (null == files) {
            // Current file is not a directory
            String tagLogPath = dir.getAbsolutePath();
            Log.i(TAG, "Log path: " + tagLogPath);
            writeDBFile(tagLogPath, tagLogFolderPath);
            return;
        } else {
            // Current file is a directory
            String dbSubFolderPath = dir.getName();
            File dbSubFolderNew = new File(tagLogFolderPath + "/"
                    + dbSubFolderPath);
            Log.i(TAG, "SubFolderNew: " + dbSubFolderNew);
            if (!dbSubFolderNew.exists()) {
                dbSubFolderNew.mkdirs();
            }
            for (int i = 0; i < files.length; i++) {
                writeFolderToTagFolder(files[i].getAbsolutePath(), dbSubFolderNew.getAbsolutePath());
            }
        }
    }
    
    public static boolean writeDBFile(String path, String newPath) {
        File file_in = new File(path);
        if (!file_in.exists()) {
            Log.e(TAG, "Log isn't exist!");
            return false;
        }
        String fileName = file_in.getName();
        File file_out = new File(newPath + "/" + fileName);
        if (!file_out.exists()) {
            try{
            file_out.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }  
        Log.i(TAG, "create new log file :" + file_out.getAbsolutePath());
        try {
            FileInputStream fis = new FileInputStream(file_in);
            FileOutputStream fos = new FileOutputStream(file_out);
            byte[] temp = new byte[1024];
            int len;
            while ((len = fis.read(temp)) != -1) {
                fos.write(temp, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static long getFolderOrFileSize(String FolderPath) {
        long size = 0;
        File folder = new File(FolderPath);
        if (!folder.exists()) {
            return -1;
        }
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                size = size + getFolderOrFileSize(file.getAbsolutePath());
            } else {
                size = file.length();
            }
        }
        return size;
    }
    
    public static int checkSdCardSpace(String SDPath, String[] logToolPath) {
        Log.i(TAG, "checkSdCardSpace SDPath: " + SDPath);
        long logSize = 0;   
//        if (SDPath.endsWith(SysUtils.EXTERNALSDPATH)) {

        File sdroot = new File(SDPath);
        if (!sdroot.exists()) {
            Log.i(TAG, "The SD Card doesn't exist");
            return SysUtils.SD_NOT_EXIST;
        }
        if (!sdroot.canWrite()) {
            Log.i(TAG, "The SD Card is not writtable");
            return SysUtils.SD_NOT_WRITABLE;
        }
        StatFs stat = new StatFs(sdroot.getPath());
        int availableBlocks = stat.getAvailableBlocks();
        int blockSize = stat.getBlockSize();
        long sdAvailableSpace = (long)availableBlocks * (long)blockSize;
        Log.i(TAG, "availableBlocks: " + availableBlocks);
        Log.i(TAG, "blockSize: " + blockSize);
        for (int i = 0; i < logToolPath.length; i++) {
            long tempSize = getFolderOrFileSize(logToolPath[i]);
            
              // for change 1
//            if (tempSize > logSize)
//                logSize = tempSize;
            logSize += tempSize;
        }
        Log.d(TAG, "sdAvailableSpace is: " + sdAvailableSpace + ", logSize: " + logSize);
        return (sdAvailableSpace > logSize ? SysUtils.SD_NORMAL : SysUtils.SD_LOCK_OF_SPACE);
//        } else {
//            long sdAvailableSpace = getSdAvailableSpace();
//            for (int i = 0; i < logToolPath.length; i++) {
//                long tempSize = getFolderOrFileSize(logToolPath[i]);
//                if (tempSize > logSize)
//                    logSize = tempSize;
//            }
//            Log.d(TAG, "sdAvailableSpace is: " + sdAvailableSpace
//                    + ", logSize: " + logSize);
//            return (sdAvailableSpace > logSize ? 1 : 0);
//        }
//        return 1;
    }  
    
    public static long getSdAvailableSpace() {
        if (isSdMounted()){
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            StatFs statFs = new StatFs(sdcard);
            long availableSpace = (long)statFs.getBlockSize() * 
            statFs.getAvailableBlocks(); 
            
            return availableSpace;
        } else {
            return -1;
        }
    }
    
    public static boolean isSdMounted() {
        if (Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState()) || 
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                Environment.getExternalStorageState())){
            return true;
        } else {
            return false;
        }
    }
    
    public static void deleteFolder(File file) {
//        File file = new File(FolderPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files) {
                // Current file is not a directory
                file.delete();
                Log.i(TAG,"Delete file :" + file.getPath());
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteFolder(files[i]);
                }
            }
            file.delete();
        }        
    }
    
    public static String createTagLogFolder(String logToolPath,String tag) {
//        String folderName = SystemProperties.get(PER_LOG2SD);
//        if (null == folderName) {
//            return null;
//        }
//        if (folderName.equals(EXTERNALSDPATH)) {
//            logPath = "mnt/sdcard2/mtklog/taglog/"
//                    + ZipManager.translateTime2(System.currentTimeMillis())
//                    + "_" + tag;
//        } else {
//            logPath = "mnt/sdcard/mtklog/taglog/"
//                    + ZipManager.translateTime2(System.currentTimeMillis())
//                    + "_" + tag;
//        }
        String logPath = logToolPath + "/" + SysUtils.ZIP_TAG_LOG_FOLDER + "/"
                + ZipManager.translateTime2(System.currentTimeMillis()) + "_"
                + tag;
        Log.i(TAG, "createTagLogFolder : " + logPath);
        File tagLogFolder = new File(logPath);
        if (!tagLogFolder.exists()) {
            tagLogFolder.mkdirs();
        }
        return tagLogFolder.getAbsolutePath();
    }

}
