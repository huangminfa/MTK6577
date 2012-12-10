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

package com.mediatek.GoogleOta;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class GoogleOtaService extends Service {
    private static final String TAG = "GoogleOtaService:";
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_QUERY = 1;
    private static GoogleOtaService mServiceInstance = null;

    private int mStatus = STATUS_IDLE;
    private Handler mHandler;
    private HttpManager httpManager;


    public static GoogleOtaService getInstance() {
        return mServiceInstance;
    }

    public void onCreate() {
        Util.logInfo(TAG,"onCreate, thread name = " + Thread.currentThread().getName());
        super.onCreate();
        httpManager = HttpManager.getInstance(this);
        mServiceInstance = this;
        Util.logInfo(TAG,"On create service done");	
    }

    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        Util.logInfo(TAG,"onStartCommand, mStatus = "+mStatus);
        if (!isQuerying()) {
	        new Thread(new Runnable() {
	                public void run() {
	                    Util.logInfo(TAG,"onStartCommand, new thread to query form receiver");
	                    GoogleOtaService.this.queryNewVersion();
	                    GoogleOtaService.this.onStop();
	                    Util.logInfo(TAG,"onStartCommand, query done");
	                }
	            }
	        ).start();
        }
        Util.logInfo(TAG,"onStartCommand, exit");
        return START_STICKY;  
    }

    @Override
    public IBinder onBind(Intent intent) {
        Util.logInfo(TAG,"onBind");
        mHandler = GoogleOtaClient.getInstance().mHandler;
        httpManager.onSetMessageHandler(mHandler);
        return mBinder;
    }
    
    public void onRebind(Intent intent) {
        Util.logInfo(TAG,"onRebind");          
        super.onRebind(intent);      
    }   

    @Override      
    public boolean onUnbind(Intent intent) {
        Util.logInfo(TAG,"onUnbind");  
        return super.onUnbind(intent);      
    }

    public void onDestroy() {
        Util.logInfo(TAG,"onDestroy");
        mStatus = STATUS_IDLE;
        super.onDestroy();	
    }

    public void onStop() {
        Util.logInfo(TAG,"onStop");
        stopSelf();	
	}

    public long onParseTime(String string) {
        Util.logInfo(TAG,"onParseTime string = "+string);
        String times[] = string.split("/", 3);
        return Integer.parseInt(times[0])*Integer.parseInt(times[1])*Integer.parseInt(times[2])*60*1000;
	}

    public void queryNewVersion() {

        mStatus = STATUS_QUERY;
        Util.logInfo(TAG,"queryNewVersion, mQueryAbort = "+false);
        httpManager.onSetAbort(false);
        httpManager.onQueryNewVersion();
        Util.logInfo(TAG,"queryNewVersion, done");
        mStatus = STATUS_IDLE;
    }

    public boolean isQuerying() {
        if (mStatus == STATUS_QUERY) {
        	return true;
        } else {
        	return false;
        }
    }
    
    private IGoogleOtaService.Stub mBinder = new IGoogleOtaService.Stub() {
        public void queryNewVersion() {
        	if (GoogleOtaService.this.isQuerying()) {
        		Util.logInfo(TAG,"queryNewVersion, background running");
        	} else {
        		Util.logInfo(TAG,"queryNewVersion, start");
        		GoogleOtaService.this.queryNewVersion();
        	}
        }
        public void queryNewVersionAbort() {

            GoogleOtaService.this.onStop();
            Util.logInfo(TAG,"queryNewVersionAbort, mQueryAbort = "+true);
            httpManager.onSetAbort(true);
        }
        public void startDlPkg() {
        	Util.logInfo(TAG,"startDlPkg" ); 
            httpManager.onDownloadImage();
        }
        public void cancelDlPkg() {
            Util.logInfo(TAG,"cancelDlPkg" ); 
            GoogleOtaService.this.onStop();
        }
        public void pauseDlPkg() {
            Util.logInfo(TAG, "pauseDlPkg");
            httpManager.onDownloadPause();
        }
        public void resetDescriptionInfo() {
            Util.logInfo(TAG, "resetDescriptionInfo");
            httpManager.resetDescriptionInfo();
        }
        public void runningBg() {
            httpManager.onSetMessageHandler(null);
        }
        public void setUpdateType(int type) {
            Util.logInfo(TAG, "setUpdateTypetype type = "+type);
            if(type == 0) {
                boolean state = httpManager.isDeltaPackageOk();
                if (!state) {
                    return;
                }
                boolean flag = httpManager.onSetRebootRecoveryFlag();
                if (!flag) {
                    return;
                }
                Intent intent = new Intent("android.intent.action.GoogleOta.RebootService");
                GoogleOtaService.this.startService(intent);
                //httpManager.onSendCloseClientMessage();
            } else {
                String[] textArray = GoogleOtaService.this.getResources().getStringArray(R.array.enquire_values);
                if (type == textArray.length - 1)
                {
                    GoogleOtaService.this.onStop();
                    httpManager.onSendCloseClientMessage();
                    return;
                }
                long interval = onParseTime(textArray[type]);
                Calendar calendar = Calendar.getInstance();
                long currentTime = calendar.getTimeInMillis();
                Util.serAlarm(GoogleOtaService.this, AlarmManager.RTC, currentTime + interval, GoogleOtaReceiver.ACTION_UPDATE_REMIND);
                GoogleOtaService.this.onStop();
                httpManager.onSendCloseClientMessage();
            }
        }
        
        
        public void setStartFlag(){
            httpManager.onSetAbort(false);
        }
    };
}
