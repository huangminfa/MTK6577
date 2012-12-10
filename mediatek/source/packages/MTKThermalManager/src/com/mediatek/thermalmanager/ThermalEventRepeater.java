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

package com.mediatek.thermalmanager;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.UEventObserver;
import android.util.Log;
import android.view.WindowManager;
import android.os.RemoteException;
import android.os.IHardwareService;
import android.provider.Settings;
import android.os.ServiceManager;
import android.os.IPowerManager;

public class ThermalEventRepeater extends Service {
    
    static final String LOG_TAG = "thermalmanager.ThermalEventRepeater";
    private Context mContext;
    private boolean mShutdown = false;
    private int mBrightnessLimit = -1;
    
    @Override    
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate() start observing.");
        // Start observing thermal uevents
        mThermalEventObserver.startObserving("SUBSYSTEM=thermal");
        mContext = getApplicationContext();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "onBind() do nothing.");
        return null;
    }
    
    private UEventObserver mThermalEventObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            Log.d(LOG_TAG, "mThermalEventObserver.onUEvent()\n");
            
            if (event.get("SHUTDOWN") != null)
            {
              int shutdown = "1".equals(event.get("SHUTDOWN")) ? 1 : 0;
              Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() shutdown="+shutdown+"\n");
              
              // call a system dialog
              if (1 == shutdown && mShutdown == false)
              {
                  // start Activity and set mShutdown to true
                  mShutdown = true;
                  Intent i = new Intent();
                  i.setClass(mContext, ShutDownAlertDialogActivity.class);
                  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  startActivity(i);
              }
            }
            
            if (event.get("SPKRVOL") != null)
            {
              int spkrVolumeLimit = Integer.parseInt(event.get("SPKRVOL"));
              Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() SPKRVOL="+event.get("SPKRVOL")+"\n");
              Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() spkrVolumeLimit="+spkrVolumeLimit+"\n");
              
              if (70 == spkrVolumeLimit)
              {
                AudioManager audiomgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int maxVol = audiomgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() MaxVol="+maxVol+"\n");
                int curVol = audiomgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() curVol="+curVol+"\n");
                int limitVol = maxVol*7/10;
                Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() limitVol="+limitVol+"\n");
                if (curVol > limitVol)
                {
                  audiomgr.setStreamVolume(AudioManager.STREAM_MUSIC, limitVol, 0);
                  Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() limit to "+limitVol+"\n");
                }
              }
            }
            
            if (event.get("BACKLIGHT") != null)
            {
                int nBrightnessLimit=0;                 
                int nBrightnessLevel = Integer.parseInt(event.get("BACKLIGHT")); 
                int nCurrentBrightness=0;
                
                //Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() BACKLIGHT="+event.get("BACKLIGHT")+"\n");
                //Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() nBrightnessLevel="+nBrightnessLevel+"\n");
                 
                /* get Current Brightness */ 
                try {
                    nCurrentBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS); 
                    Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() nCurrentBrightness="+nCurrentBrightness+"\n"); 
                }catch (Exception e) {
                    Log.d(LOG_TAG, "ERROR Read Current Brightness Error:"+nCurrentBrightness+"\n");
                    nCurrentBrightness = 0;
                }
                 
                switch(nBrightnessLevel)
                {
                    case 0:
                        /* Recovery */
                        nBrightnessLimit = -1;
                        break;
                    case 1:
                        nBrightnessLimit = (255*7)/10;  /* 70% of 255 */
                        break;
                    case 2:
                        nBrightnessLimit = (255*4)/10;  /* 40% of 255 */
                        break;
                    case 3:
                        nBrightnessLimit = (255*1)/10;  /* 10% of 255 */
                        break;
                    default:
                        Log.d(LOG_TAG, "ERROR BACKLIGHT Level:"+nBrightnessLevel+"\n");
                        nBrightnessLimit = nCurrentBrightness;
                        break;
                }//switch 
                    
                
                //if(nBrightnessLimit < nCurrentBrightness) {
                if (mBrightnessLimit != nBrightnessLimit) {
                    //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, nBrightnessLimit);
                    try {
                        IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                        if (power != null) {
							Log.d(LOG_TAG, "mThermalEventObserver.onUEvent() Set Brightness nBrightnessLimit="+nBrightnessLimit+"\n"); 
                            power.setMaxBrightness(nBrightnessLimit);
                        }
                    } catch (RemoteException doe) {
                    }//try-catch     
                    mBrightnessLimit = nBrightnessLimit;
                }else {
                    //Log.d(LOG_TAG, "Warning  nBrightnessLimit < nCurrentBrightness: ("+nBrightnessLimit+", "+nCurrentBrightness+") \n");
                }//if-else
            }//if                   
        }
    };

}
