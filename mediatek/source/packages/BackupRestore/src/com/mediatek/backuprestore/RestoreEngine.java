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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.ProgressReporter;
import com.mediatek.backuprestore.CalendarRestoreComposer;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.Composer;
import com.mediatek.backuprestore.ProgressReporter;
import com.mediatek.backuprestore.MmsRestoreComposer;
import com.mediatek.backuprestore.AppRestoreComposer;


public class RestoreEngine {
    private Context mContext;
    private String mZipFileName;
    OnRestoreEndListner mRestoreEndListner;
    
    private boolean mIsRunning = false;
    private boolean mPause = false;
    
    private Object mLock = new Object();

    private ProgressReporter mReporter;
    List<Composer> mComposers;
    
    static private RestoreEngine mSelf;
    
    public static RestoreEngine getInstance(Context context, ProgressReporter reporter){
        if (mSelf == null) {
            new RestoreEngine(context, reporter); 
        }

        return mSelf;
    }
    
    public RestoreEngine(Context context, ProgressReporter reporter){
        mContext = context;
        mReporter = reporter;
        mComposers = new ArrayList<Composer>();
        mSelf = this;
    }
    
    public boolean isRunning() {
        return mIsRunning;
    }
    
    public void pause() {
        mPause = true;
    }

    public boolean isPaused() {
        return mPause;
    }
    
    public void continueRestore() {
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

            continueRestore();
        }
    }

     public void setOnRestoreEndListner(OnRestoreEndListner restoreEndListner) {
         mRestoreEndListner = restoreEndListner;
     }

    
    public void startRestore(String fileName, List<Integer> list) {
        reset();
        if (fileName != null && list.size() > 0) {
            mZipFileName = fileName;
            setupComposer(list);
            mIsRunning = true;
            new RestoreThread().start();
        }
    }
    
    private void addComposer(Composer composer){
        if (composer != null ) {
            composer.setReporter(mReporter);
            composer.setZipFileName(mZipFileName);
            mComposers.add(composer);
        }
    }
    private void reset() {
        if (mComposers!= null) {
            mComposers.clear();
        }

        mPause = false;
    }
    
    private boolean setupComposer(List<Integer> list) {
        boolean bSuccess = true;
        Composer composer;
        for (int type : list) {
            switch(type) {
            case ModuleType.TYPE_CONTACT:
                addComposer(new ContactRestoreComposer(mContext));
                break;
                
            case ModuleType.TYPE_MESSAGE:    
                addComposer(new MessageRestoreComposer(mContext));
                break;
                
            case ModuleType.TYPE_SMS:
                addComposer(new SmsRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_MMS:
                addComposer(new MmsRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_PICTURE:
                addComposer(new PictureRestoreComposer(mContext));
                break;
                
            case ModuleType.TYPE_CALENDAR:
                addComposer(new CalendarRestoreComposer(mContext));
                break;   

            case ModuleType.TYPE_APP:
                addComposer(new AppRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_MUSIC:
                addComposer(new MusicRestoreComposer(mContext));
                break;
                
            case ModuleType.TYPE_NOTEBOOK:
                addComposer(new NoteBookRestoreComposer(mContext));
                break;

            default:
                bSuccess = false;
            }
        }
        
        return bSuccess;
    }
   

    public class RestoreThread extends Thread {
        public RestoreThread() {
        }
        
        @Override
        public void run() {
            Log.d(LogTag.RESTORE, "RestoreThread begin...");

            for (Composer composer : mComposers) {
                Log.d(LogTag.RESTORE, "RestoreThread composer:" + composer.getModuleType() + " start..");
                Log.d(LogTag.RESTORE, "begin restore:" + System.currentTimeMillis());
                if (!composer.isCancel()) {
                    composer.init();
                    Log.d(LogTag.RESTORE, "RestoreThread composer:" + composer.getModuleType() + " init finish");
                    composer.onStart();
                    while (!composer.isAfterLast() && !composer.isCancel()) {
                        if (mPause) {
                            synchronized (mLock) {
                                try {
                                    Log.d(LogTag.RESTORE, "RestoreThread wait...");
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        composer.composeOneEntity();
                        Log.d(LogTag.RESTORE, "RestoreThread composer:" + composer.getModuleType() + "composer one entiry");
                    }
                }

                try {
                    sleep(200);
                } catch(InterruptedException e) {
                }

                composer.onEnd();
                Log.d(LogTag.RESTORE, "end restore:" + System.currentTimeMillis());
                Log.d(LogTag.RESTORE, "RestoreThread composer:" + composer.getModuleType() + " composer finish");
            }

            Log.d(LogTag.RESTORE, "RestoreThread run finish");
            mIsRunning = false;

            if (mRestoreEndListner != null) {
                mRestoreEndListner.onEnd(true);
            }
        }
    }


     public interface OnRestoreEndListner {
         public void onEnd(boolean bSuccess);
     }
}
