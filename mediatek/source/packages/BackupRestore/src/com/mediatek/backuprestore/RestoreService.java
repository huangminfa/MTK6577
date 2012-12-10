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

import com.mediatek.backuprestore.RestoreEngine.OnRestoreEndListner;
import com.mediatek.backuprestore.ResultDialog.ResultEntity;

public class RestoreService extends Service implements ProgressReporter, OnRestoreEndListner{
    
    private final static String LOGTAG = "RestoreService";
    final static int INIT = 0X00;
    final static int RUNNING = 0X01;
    final static int PAUSE = 0X02;
    final static int CANCELLING = 0X03;  
//    final static int END = 0X04;
    final static int FINISH = 0X05;
    final static int ERROHAPPEN = 0X06;
    
    RestoreBinder mBinder = new RestoreBinder();
    int mState;
    
    RestoreEngine mRestoreEngine;
    ArrayList<ResultEntity> mResult;
    RestoreProgress mCurProgress = new RestoreProgress();
    OnRestoreChanged mListener;
    
    
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOGTAG, "RestoreService onbind");
        return mBinder;
    }
    
    
    public boolean  onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.v(LOGTAG, "RestoreService onUnbind");
        return true;
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        moveToState(INIT);
        
        Log.v(LOGTAG, "RestoreService onCreate");
    }
    
    public int  onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Log.v(LOGTAG, "RestoreService onStartCommand");
        return 0;
    }
    
    public void  onRebind  (Intent intent){
        super.onRebind(intent);
        Log.v(LOGTAG, "RestoreService onRebind");
    }
    
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOGTAG, "RestoreService onDestroy");
        if (mRestoreEngine != null && mRestoreEngine.isRunning()){
            mRestoreEngine.setOnRestoreEndListner(null);
            mRestoreEngine.cancel();
        }
    }
    
    public void moveToState(int state){
        synchronized(this){
            mState = state;
        }
    }
    
    public static class RestoreProgress{
        int mType;
        int mMax;
        int mCurNum;
    }
    
    private int getRestoreState(){
        synchronized(this){
            return mState;
        }
    }
    
    
    public class RestoreBinder extends Binder{
        public int getState(){
            return getRestoreState();
        }
        
        public void startRestore(String fileName, ArrayList<Integer> list){
            if(mRestoreEngine == null){
                mRestoreEngine = new RestoreEngine(RestoreService.this, RestoreService.this);
            }
            reset();
            mRestoreEngine.setOnRestoreEndListner(RestoreService.this);
            mRestoreEngine.startRestore(fileName, list);
            moveToState(RUNNING);
            Log.v(LOGTAG, "RestoreService startRestore");
        }
        
        public void pauseRestore(){
            moveToState(PAUSE);
            if (mRestoreEngine != null){
                mRestoreEngine.pause();
            }
            Log.v(LOGTAG, "RestoreService pauseRestore");
        }
        
        public void continueRestore(){
            moveToState(RUNNING);
            if (mRestoreEngine != null){
                mRestoreEngine.continueRestore();
            }
            Log.v(LOGTAG, "RestoreService continueRestore");
        }
        
        public void cancelRestore(){
            moveToState(CANCELLING);
            if (mRestoreEngine != null){
                mRestoreEngine.cancel();
            }
            Log.v(LOGTAG, "RestoreService cancelRestore");
        }
        
        public void reset(){
            moveToState(INIT);
            if( mResult != null){
                mResult.clear();
            }
        }
        
        public RestoreProgress getCurRestoreProgress(){
            return mCurProgress;
        }
        
        public void setOnRestoreChangedListner(OnRestoreChanged listener){
            mListener = listener;
        }
        
        public ArrayList<ResultEntity> getRestoreResult(){
            return mResult;
        }
    }
    
    
    public void onStart(Composer iComposer) {
        mCurProgress.mType = iComposer.getModuleType();
        mCurProgress.mMax = iComposer.getCount();
        mCurProgress.mCurNum = 0;
        if (mListener != null){
            mListener.onComposerChanged(mCurProgress.mType, mCurProgress.mMax);
        }
    }

    public void onOneFinished(Composer composer) {

        mCurProgress.mCurNum ++;
        if (mListener != null){
            mListener.onProgressChange(mCurProgress.mCurNum);
        }
    }
    public void onEnd(Composer composer, boolean result) {

        if (mResult == null){
            mResult = new ArrayList<ResultEntity>();
        }
        ResultEntity item = new ResultEntity(composer.getModuleType(), result);
        mResult.add(item);
    }

    public void onErr(IOException e) {

        if (mListener != null){
            mListener.onRestoreErr(e);
        }
    }
    
    public interface OnRestoreChanged{
        public void onComposerChanged(int type, int num);
        public void onProgressChange(int progress);
        public void onRestoreEnd(boolean bSuccess, ArrayList<ResultEntity> resultRecord);
        public void onRestoreErr(IOException e);
    }

    public void onEnd(boolean bSuccess) {

        moveToState(FINISH);
        if (mListener != null){
            mListener.onRestoreEnd(bSuccess, mResult);
        }
    }
}
