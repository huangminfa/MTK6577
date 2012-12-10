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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mediatek.backuprestore.BackupRestoreUtils.Consts;
import com.mediatek.backuprestore.BackupRestoreUtils.FileUtils;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;


public class BackupFileScanner {

    private Handler mHandler;
    private Object object = new Object();

    public BackupFileScanner(Context context, Handler handler) {
        mHandler = handler;
        if (mHandler == null) {
            Log.e(LogTag.RESTORE,"constuctor BackupFileScanner maybe failed!cause mHandler is null");
        }
        }
    
    public void setHandler(Handler handler){
    	synchronized(object){
        mHandler = handler;
    }
    }

    ScanThread mScanThread;
    public void startScan() {
        mScanThread = new ScanThread();
        mScanThread.start();
    }
    
    public void quitScan(){
    	synchronized (object) {
        if (mScanThread != null){
            mScanThread.cancel();
            mScanThread = null;
            Log.v(LogTag.RESTORE, "BackupFileScanner-> quitScan");
        }
    }
    }

    private class ScanThread extends Thread {

        boolean isCanceled = false;
        
        public void cancel(){
            isCanceled = true;
        }

        private File[] filterFile(File[] fileList) {
            if (fileList == null) {
                return null;
            }
            List<File> list = new ArrayList<File>();
            for (File file : fileList) {
                if (isCanceled){
                    break;
                }
                String ext = FileUtils.getExt(file);
				
				if(!isRightFileName(file)){
					continue;
				}					
                if (ext != null && ext.equalsIgnoreCase(Consts.BackupFileExt)) {
                    list.add(file);
                }
            }
            if (isCanceled){
                return null;
            }else{
            return (File[]) list.toArray(new File[0]);
        }
        }
        
        private boolean isRightFileName(File file){
            boolean ret = false;
            String fileName = FileUtils.getNameWithoutExt(file.getName());
            if (fileName.length() == 14){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                try {
                    Date date = dateFormat.parse(fileName);
                    String fileName2 = dateFormat.format(date);
                    if (fileName.equals(fileName2)){
                        ret = true;
                    }
                } catch (ParseException e){
                    // if not formate to date, it is not a needed file
                }
            }
            return ret;
        }
        
        private File[] scanBackupFiles() {
            String path = BackupRestoreUtils.getStoragePath();
            if (path!= null && !isCanceled){
                return filterFile(new File(path).listFiles());
            }else{
                return null;
            }
        }

        @Override
        public void run() {
            File[] files = scanBackupFiles();
            List<BackupFilePreview> backupItems = generateBackupFIleItems(files);
            synchronized (object){
            if (!isCanceled && mHandler != null){
            Message msg = mHandler.obtainMessage(RestoreActivity.SCANNER_FINISH, backupItems);
            mHandler.sendMessage(msg);
        }
                mScanThread = null;
            }
            
        }

        private List<BackupFilePreview> generateBackupFIleItems(File[] files) {
            if (files == null || isCanceled) {
                return null;
            }
            List<BackupFilePreview> list = new ArrayList<BackupFilePreview>();
            for (File file : files) {
                if (isCanceled){
                    break;
                }
                BackupFilePreview backupFile = new BackupFilePreview(file);
                if (backupFile != null){
                    if (backupFile.getBackupTime() != null) {
                        // only add files that can read backup date
                        list.add(backupFile);
                    }else{
                        file.delete();
                    }
                }      
            }
            if (!isCanceled){
                sort(list);
                return list;
            }else{
                return null;
            }
		}

        private void sort(List<BackupFilePreview> list) {
            Collections.sort(list, new Comparator<BackupFilePreview>() {
                public int compare(BackupFilePreview object1, BackupFilePreview object2) {
                    String dateLeft = object1.getBackupTime();
                    String dateRight = object2.getBackupTime();
                    if (dateLeft != null && dateRight != null){
                        return dateRight.compareTo(dateLeft);
                    }else{
                    	return 0;
                    }
                }
            });
        }
    }

    
}
