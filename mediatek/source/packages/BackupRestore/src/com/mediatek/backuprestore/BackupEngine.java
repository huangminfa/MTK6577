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
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.Composer;
import com.mediatek.backuprestore.ProgressReporter;
import com.mediatek.backuprestore.SmsBackupComposer;
import com.mediatek.backuprestore.MmsBackupComposer;
import com.mediatek.backuprestore.MessageBackupComposer;
import com.mediatek.backuprestore.AppBackupComposer;
import com.mediatek.backuprestore.ContactBackupComposer;
import com.mediatek.backuprestore.CalendarBackupComposer;
import com.mediatek.backuprestore.BackupXmlInfo;
import com.mediatek.backuprestore.BackupXmlComposer;
import com.mediatek.backuprestore.PictureBackupComposer;
import java.io.File;
//import android.provider.Settings.System;
//import java.lang.System;

public class BackupEngine {

    private Context mContext;
    private ProgressReporter mReporter;
    private BackupXmlInfo mXmlInfo;

    List<Composer> mComposers;

    private BackupZip mZipFileHandler;
    private OnBackupEndListner mBackupEndListner;

    private boolean mIsRunning = false;

    private boolean mPause = false;
    private boolean mIsCancel = false;
    private Object mLock = new Object();

    private static BackupEngine mSelf;
    
    public static BackupEngine getInstance(Context context, ProgressReporter reporter) {
        if (mSelf == null){
            new BackupEngine(context, reporter);
        } else {
            mSelf.updateInfo(context, reporter);
        }

        return mSelf;
    }
    
    public BackupEngine(Context context, ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
        mComposers = new ArrayList<Composer>();
        mSelf = this;
    }

    public boolean isRunning() {
        return mIsRunning;
    }


    private void updateInfo(Context context, ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
    }

    public void pause() {
        mPause = true;
    }

    public boolean isPaused() {
        return mPause;
    }

    public void continueBackup() {
        if (mPause) {
            synchronized (mLock) {
                mPause = false;
                mLock.notify();
            }
        }
    }

    public void cancel() {
        if (mComposers != null && mComposers.size() > 0) {
            for (Composer composer : mComposers) {
                composer.setCancel(true);
            }
            mIsCancel = true;
            continueBackup();
        }
    }

    public void setOnBackupEndListner(OnBackupEndListner listner){
        mBackupEndListner = listner;
    }

    public boolean startBackup(ArrayList<Integer> moduleList) {
        reset();
        boolean startSuccess = true;
        mXmlInfo = new BackupXmlInfo();
        if (setupComposer(moduleList)) {
            mIsRunning = true;
            new BackupThread().start();
        } else {
            startSuccess = false;
        }

        return startSuccess;
    }

    private void addComposer(Composer composer) {
        if (composer != null) {
            composer.setReporter(mReporter);
            composer.setZipHandler(mZipFileHandler);
            mComposers.add(composer);
        }
    }

    private void reset() {
        if (mComposers != null) {
            mComposers.clear();
        }

        mPause = false;
        mIsCancel = false;
    }

    private boolean setupComposer(ArrayList<Integer> list) {
        Log.d(LogTag.BACKUP, "setupComposer begin...");

        boolean result = true;
        String zipFileName = generateFileName();
        if (zipFileName != null) {
            try {
                mZipFileHandler = new BackupZip(zipFileName);
                Log.d(LogTag.BACKUP, "zipFile:" + zipFileName + " success");
            } catch (Exception e) {
                Log.d(LogTag.BACKUP, "generate zipFile:" + zipFileName + " failed");
                result = false;
            }

            for (int type : list) {
                switch (type) {
                    case ModuleType.TYPE_CONTACT:
                        addComposer(new ContactBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_CALENDAR:
                        addComposer(new CalendarBackupComposer(mContext));
                        break;
                    
                    case ModuleType.TYPE_SMS:
                        addComposer(new SmsBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_MMS:
                        addComposer(new MmsBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_MESSAGE:
                        addComposer(new MessageBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_APP:
                        addComposer(new AppBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_PICTURE:
                        addComposer(new PictureBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_MUSIC:
                        addComposer(new MusicBackupComposer(mContext));
                        break;

                    case ModuleType.TYPE_NOTEBOOK:
                        addComposer(new NoteBookBackupComposer(mContext));
                        break;

                    default:
                        result = false;
                        break;
                }
            }

            Log.d(LogTag.BACKUP, "setupComposer finish");
        } else {
            Log.e(LogTag.BACKUP, "setupComposer failed");
            result = false;
        }

        return result;
    }

    private class BackupThread extends Thread {
        @Override
        public void run() {
            BackupResult result = BackupResult.Fail;

            Log.d(LogTag.BACKUP, "BackupThread begin...");
            for (Composer composer : mComposers) {
                Log.d(LogTag.BACKUP, "BackupThread->composer:" + composer.getModuleType() + " start...");
                if (!composer.isCancel()) {
                    composer.init();
                    composer.onStart();
                    Log.d(LogTag.BACKUP, "BackupThread-> composer:" + composer.getModuleType() + " init finish");
                    while (!composer.isAfterLast() && !composer.isCancel()) {
                        if (mPause) {
                            synchronized (mLock) {
                                try {
                                    Log.d(LogTag.BACKUP, "BackupThread wait...");
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        composer.composeOneEntity();
                        Log.d(LogTag.BACKUP, "BackupThread->composer:" + composer.getModuleType() + " compose one entiry");
                    }
                }

                try {
                    sleep(200);
                } catch(InterruptedException e) {
                }

                composer.onEnd();

                generateModleXmlInfo(composer);

                Log.d(LogTag.BACKUP, "BackupThread-> composer:" + composer.getModuleType() + " finish");
            }

            if (mZipFileHandler != null) {
                try {
                    if (!mIsCancel && mXmlInfo.getTotalNum() > 0) {
                        String xmlFileContent = new BackupXmlComposer().composeXml(mXmlInfo);
                        mZipFileHandler.addFile("backup.xml", xmlFileContent);
                        mZipFileHandler.finish();
                        result = BackupResult.Success;
                        //Log.d(LogTag.BACKUP, "Xml:" + xmlFileContent);
                    } else {
                        deleteFile();
                    }
                    Log.d(LogTag.BACKUP, "mZipFileHandler finish");
                } catch (IOException e) {
                    deleteFile();
                } finally {
                    mZipFileHandler = null;
                }
            }

            Log.d(LogTag.BACKUP, "BackupThread run finish, result:" + result);
            mIsRunning = false;

            if (mBackupEndListner != null) {
                if (mPause) {
                    synchronized (mLock) {
                        try {
                            Log.d(LogTag.BACKUP, "BackupThread wait before end...");
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (mIsCancel) {
                    result = BackupResult.Cancel;
                    deleteFile();
                }

                mBackupEndListner.onEnd(result);
            }
        }
    }


    private boolean generateModleXmlInfo(Composer composer) {

        if (mXmlInfo != null && composer != null) {
            int count = composer.getComposed();
            switch (composer.getModuleType()) {
                case ModuleType.TYPE_CONTACT:
                    mXmlInfo.setContactNum(count);
                    break;

                case ModuleType.TYPE_MESSAGE:
                    mXmlInfo.setSmsNum(((MessageBackupComposer)composer).getComposed(ModuleType.TYPE_SMS));
                    mXmlInfo.setMmsNum(((MessageBackupComposer)composer).getComposed(ModuleType.TYPE_MMS));
                    break;

                case ModuleType.TYPE_CALENDAR:
                    mXmlInfo.setCalendarNum(count);
                    break;

                case ModuleType.TYPE_APP:
                    mXmlInfo.setAppNum(count);
                    break;

                case ModuleType.TYPE_PICTURE:
                    mXmlInfo.setPictureNum(count);
                    break;

                case ModuleType.TYPE_MUSIC:
                    mXmlInfo.setMusicNum(count);
                    break;

                case ModuleType.TYPE_NOTEBOOK:
                    mXmlInfo.setNoteBookNum(count);
                    break;
            }

            return true;
        }

        return false;
    }


    private String generateFileName() {
        String path = BackupRestoreUtils.getStoragePath();
        if (path != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = dateFormat.format(new Date(System.currentTimeMillis()));
            String fileName = (path + "/" +  dateString + ".zip");
            mXmlInfo.setBackupDate(dateString);
            mXmlInfo.setSystem("Android " + android.os.Build.VERSION.RELEASE);
            mXmlInfo.setDevicetype(android.os.Build.MODEL);

            return fileName;
        }

        return null;
    }

    public interface OnBackupEndListner {
        public void onEnd(BackupResult result);
    }

    public enum BackupResult {
        Success, Fail, Error, Cancel
    }

    public void deleteFile() {
        File file = new File(BackupRestoreUtils.getStoragePath() + "/" + mXmlInfo.getBackupDateString() + ".zip");
        if (file != null && file.exists()) {
            file.delete();
            Log.d(LogTag.BACKUP, "deleteFile");
        }
    }
}
