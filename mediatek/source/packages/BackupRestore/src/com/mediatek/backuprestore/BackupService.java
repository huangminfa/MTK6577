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

import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mediatek.backuprestore.BackupEngine.BackupResult;
import com.mediatek.backuprestore.BackupEngine.OnBackupEndListner;
import com.mediatek.backuprestore.ResultDialog.ResultEntity;

public class BackupService extends Service implements ProgressReporter, OnBackupEndListner{
    
    private final static String LOGTAG = "BackupService";
    final static int INIT = 0X00;
    final static int RUNNING = 0X01;
    final static int PAUSE = 0X02;
    final static int CANCELCONFORM = 0X03;
    final static int CANCELLING = 0X04;
    final static int FINISH = 0X05;
    final static int ERROHAPPEN = 0X06;
    
    
    BackupBinder mBinder = new BackupBinder();
    int mState;
    
    BackupEngine mBackupEngine;
    ArrayList<ResultEntity> mResult;
    BackupProgress mCurProgress = new BackupProgress();
    OnBackupChanged mListener;
    BackupResult mResultType;
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }
    
    
    public boolean  onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.v(LOGTAG, "BackupService onUnbind");
        return true;
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        mState = INIT;
        Log.v(LOGTAG, "BackupService onCreate");
    }
    
    public int  onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Log.v(LOGTAG, "BackupService onStartCommand");
        return 0;
    }
    
    public void  onRebind  (Intent intent){
        super.onRebind(intent);
        Log.v(LOGTAG, "BackupService onRebind");
    }
    
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOGTAG, "BackupService onDestroy");
        if (mBackupEngine != null && mBackupEngine.isRunning()){
            mBackupEngine.setOnBackupEndListner(null);
            mBackupEngine.cancel();
        }
    }
    
    public static class BackupProgress{
        int mType;
        int mMax;
        int mCurNum;
    }
    
    public class BackupBinder extends Binder{
        public int getState(){
            return mState;
        }
        
        public void startBackup(ArrayList<Integer> list){
            if(mBackupEngine == null){
                mBackupEngine = new BackupEngine(BackupService.this, BackupService.this);
            }
            reset();
            mBackupEngine.setOnBackupEndListner(BackupService.this);
            mBackupEngine.startBackup(list);
            mState = RUNNING;
            Log.v(LOGTAG, "BackupService startBackup");
        }
        
        public void pauseBackup(){
            mState = PAUSE;
            if (mBackupEngine != null){
                mBackupEngine.pause();
            }
            Log.v(LOGTAG, "BackupService pauseBackup");
        }
        
        public void cancelBackup(){
            mState = CANCELLING;
            if (mBackupEngine != null){
                mBackupEngine.cancel();
            }
            Log.v(LOGTAG, "BackupService cancelBackup");
        }
        
        public void continueBackup(){
            mState = RUNNING;
            if (mBackupEngine != null){
                mBackupEngine.continueBackup();
            }
            Log.v(LOGTAG, "BackupService continueBackup");
        }
        
        public void reset(){
            mState = INIT;
            if( mResult != null){
                mResult.clear();
            }
        }
        
        public BackupProgress getCurBackupProgress(){
            return mCurProgress;
        }
        
        public void setOnBackupChangedListner(OnBackupChanged listener){
            mListener = listener;
        }
        
        public ArrayList<ResultEntity> getBackupResult(){
            return mResult;
        }
        
        public BackupResult getBackupResultType(){
            return mResultType;
        }
    }
    
    
    public void onStart(Composer iComposer) {
        // TODO Auto-generated method stub
        mCurProgress.mType = iComposer.getModuleType();
        mCurProgress.mMax = iComposer.getCount();
        mCurProgress.mCurNum = 0;
        if (mListener != null){
            mListener.onComposerChanged(mCurProgress.mType, mCurProgress.mMax);
        }
    }

    public void onOneFinished(Composer composer) {
        // TODO Auto-generated method stub
        mCurProgress.mCurNum ++;
        if (mListener != null){
            mListener.onProgressChange(mCurProgress.mCurNum);
        }
    }
    public void onEnd(Composer composer, boolean result) {
        // TODO Auto-generated method stub
        if (mResult == null){
            mResult = new ArrayList<ResultEntity>();
        }
        ResultEntity item = new ResultEntity(composer.getModuleType(), result);
        mResult.add(item);
    }

    public void onErr(IOException e) {
        // TODO Auto-generated method stub
        Log.d(LOGTAG, "BackupService -> onErr" + e.getMessage());
        if (mListener != null){
            mListener.onBackupErr(e);
        }
    }


    public void onEnd(BackupResult result) {
        // TODO Auto-generated method stub
        Log.d(LOGTAG, "BackupService end; result = " + result);       
        mResultType = result;
        if (mListener != null){
            if(mState == CANCELLING){
                result = BackupResult.Cancel;
            }
            
            if(result != BackupResult.Success && result != BackupResult.Cancel){
                for(ResultEntity item : mResult){
                    item.mResult = false;
                }
            }
            mState = FINISH;
            mListener.onBackupEnd(result, mResult);
        }else{
        mState = FINISH;
        }
    }
    
    public interface OnBackupChanged{
        public void onComposerChanged(int type, int num);
        public void onProgressChange(int progress);
        public void onBackupEnd(BackupResult resultCode, ArrayList<ResultEntity> resultRecord);
        public void onBackupErr(IOException e);
    }
    
}
