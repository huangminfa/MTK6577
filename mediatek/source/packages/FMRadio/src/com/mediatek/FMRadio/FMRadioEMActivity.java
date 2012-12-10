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

package com.mediatek.FMRadio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.mediatek.FMRadio.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Configuration;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import java.lang.Exception;
import java.lang.reflect.Array;

import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.featureoption.FeatureOption;


public class FMRadioEMActivity extends Activity {
    public static final String TAG = "FMRadioEM";
    
    public static final String TYPE_MSGID = "MSGID";
    public static final String TYPE_TOAST_STRING = "TYPE_TOAST_STRING";
    public static final int MSGID_RETRY = 1;
    public static final int MSGID_OK = 2;
    public static final int MSGID_SEARCH_FINISH = 3;
    public static final int MSGID_UPDATE_RDS = 4;
    public static final int MSGID_UPDATE_CURRENT_STATION = 5;
    public static final int MSGID_SEEK_FINISH = 6;
    public static final int MSGID_SEEK_FAIL = 7;
    public static final int MSGID_PLAY_FINISH = 8;
    public static final int MSGID_PLAY_FAIL = 9;
    public static final int MSGID_ANTENNA_UNAVAILABE = 10;
    public static final int MSGID_SHOW_TOAST = 11;
    public static final int MSGID_TICK_EVENT = 12;
    public static final int MSGID_INIT_OK = 13;
    
    private static final String FM_SAVE_INSTANCE_STATE_INITED = "FM_SAVE_INSTANCE_STATE_INITED";
    private static final String FM_SAVE_INSTANCE_STATE_PLAYING = "FM_SAVE_INSTANCE_STATE_PLAYING";
    private static final String FM_SAVE_INSTANCE_STATE_EARPHONEUSED = "FM_SAVE_INSTANCE_STATE_EARPHONEUSED";
    
    private boolean mIsServiceStarted = false;
    private boolean mIsServiceBinded = false;
    private IFMRadioService mService = null;
    private ServiceConnection mServiceConnection = null;

    private Handler mHandler = null;
    //private boolean mIsInited = false;
    
    private boolean mIsPlaying = false; // When start, the radio is not playing.
    private boolean mIsExitPressed = false;
    

    // Record whether we are destroying.
    private boolean mIsDestroying = false;

    // RDS settings
    private boolean mIsPSRTEnabled = false;
    private boolean mIsAFEnabled = false;
   // private boolean mIsTAEnabled = false;
    
    // Record whether RDS is supported.
    private boolean mIsRDSSupported = false;
    // Record whether RDS is enabled.
    private boolean mIsRDSEnabled = false;
    
    // Strings shown in RDS text view.
    private String mPSString = "";
    private String mLRTextString = "";
    
    // The toast and its timer
    public static final long TOAST_TIMER_DELAY = 2000; // Timer delay 2 seconds.
    private Toast mToast = null;
    private Timer mTimer = null;
    
    private TextView mTextStereoMono = null;
    private TextView mTextRssi = null;
    private TextView mTextCapArray = null;
    private TextView mTextRdsBler = null;
    private TextView mTextRdsPS = null;
    private TextView mTextRdsRT = null;
    private TextView mTextChipID = null;
    private TextView mTextECOVersion = null;
    private TextView mTextPatchVersion = null;
    private TextView mTextDSPVersion = null;
    private EditText mEditFreq = null;    
    private Button mButtonTune = null;
    private RadioButton mRdAntennaS = null;
    private RadioButton mRdAntennaL = null;
    private RadioGroup mRgAntenna = null;
    private RadioButton mRdStereo = null;
    private RadioButton mRdMono = null;
    private RadioGroup mRgSM = null;

    // Can not use float to record the station. Because there will be inaccuracy when increase/decrease 0.1
    private int mCurrentStation = FMRadioStation.FIXED_STATION_FREQ;
    private AudioManager mAudioManager = null;
    
    private static final int CONVERT_RATE = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 100 : 10;
   // private Bundle mSavedInstanceState = null;

    private class HeadsetConnectionReceiver extends BroadcastReceiver {
         public void onReceive(Context context, Intent intent) {
             if (intent.hasExtra("state")){    
                 if (intent.getIntExtra("state", 0) == 0){//unpluged
                     if(mIsPlaying)
                     {
                         mRgAntenna.check(R.id.FMR_Antenna_short);
                     }
                 } else if (intent.getIntExtra("state", 0) == 1){ //pluged        
                     if(mIsPlaying)
                     {
                         mRgAntenna.check(R.id.FMR_Antenna_long);
                     }
                 }
            }
                 
        }  
    };     
     
         
    private class FMBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onReceive");
            String action = intent.getAction();
            FMRadioLogUtils.d(TAG, "Context: " + context);
            FMRadioLogUtils.d(TAG, "Action: " + action);
            if (action.equals(FMRadioService.ACTION_STATE_CHANGED)) {
                boolean bIsPowerUp = intent.getBooleanExtra(FMRadioService.EXTRA_FMRADIO_ISPOWERUP, false);
                if (bIsPowerUp) {
                    FMRadioLogUtils.d(TAG, "FM Radio is power up.");                    
                    mIsPlaying = true;
                    refreshTextStatus(true);
                } else {
                    FMRadioLogUtils.d(TAG, "FM Radio is power down.");                    
                    mIsPlaying = false;
                    refreshTextStatus(false);
                }
            } else if (action.equals(FMRadioService.ACTION_RDS_PS_CHANGED)){
                mPSString = intent.getStringExtra(FMRadioService.EXTRA_RDS_PS);
                FMRadioLogUtils.d(TAG, "getPS: " + mPSString);
                
                // Update the RDS text view.
                Message msg = new Message();
                msg.setTarget(mHandler);
                Bundle bundle = new Bundle();
                bundle.putInt(TYPE_MSGID, MSGID_UPDATE_RDS);
                msg.setData(bundle);
                msg.sendToTarget();
            } else if (action.equals(FMRadioService.ACTION_RDS_RT_CHANGED)){
                mLRTextString = intent.getStringExtra(FMRadioService.EXTRA_RDS_RT);
                FMRadioLogUtils.d(TAG, "getLRText: " + mLRTextString);
                
                // Update the RDS text view.
                Message msg = new Message();
                msg.setTarget(mHandler);
                Bundle bundle = new Bundle();
                bundle.putInt(TYPE_MSGID, MSGID_UPDATE_RDS);
                msg.setData(bundle);
                msg.sendToTarget();
            } else if (action.equals(FMRadioService.ACTION_RDS_AF_ACTIVED)){
                int iFreq = intent.getIntExtra(FMRadioService.EXTRA_RDS_AF_ACTIVED, 0);
                if (iFreq >= FMRadioStation.LOWEST_STATION
                    && iFreq <= FMRadioStation.HIGHEST_STATION) {
                    // Valid alternative frequency. Should update the current station.
                    mCurrentStation = iFreq;
                    Message msg = new Message();
                    msg.setTarget(mHandler);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TYPE_MSGID, MSGID_UPDATE_CURRENT_STATION);
                    msg.setData(bundle);
                    msg.sendToTarget();
                } else {
                    FMRadioLogUtils.e(TAG, "Error: invalid alternative frequency");
                }
            } else if (action.equals(FMRadioService.ACTION_RDS_TA_ACTIVED)){
                int iFreq = intent.getIntExtra(FMRadioService.EXTRA_RDS_TA_ACTIVED, 0);
                if (iFreq >= FMRadioStation.LOWEST_STATION
                    && iFreq <= FMRadioStation.HIGHEST_STATION) {
                    // Valid alternative frequency. Should update the current station.
                    mCurrentStation = iFreq;
                    Message msg = new Message();
                    msg.setTarget(mHandler);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TYPE_MSGID, MSGID_UPDATE_CURRENT_STATION);
                    msg.setData(bundle);
                    msg.sendToTarget();
                } else {
                    FMRadioLogUtils.e(TAG, "Error: invalid activeTA frequency");
                }
            } else if (action.equals(FMRadioService.ACTION_RDS_TA_DEACTIVED)){
                int iFreq = intent.getIntExtra(FMRadioService.EXTRA_RDS_TA_DEACTIVED, 0);
                if (iFreq >= FMRadioStation.LOWEST_STATION
                    && iFreq <= FMRadioStation.HIGHEST_STATION) {
                    // Valid alternative frequency. Should update the current station.
                    mCurrentStation = iFreq;
                    Message msg = new Message();
                    msg.setTarget(mHandler);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TYPE_MSGID, MSGID_UPDATE_CURRENT_STATION);
                    msg.setData(bundle);
                    msg.sendToTarget();
                } else {
                    FMRadioLogUtils.e(TAG, "Error: invalid activeTA frequency");
                }
            } else {
                FMRadioLogUtils.e(TAG, "Error: undefined action.");
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onReceive");
        }
    }

    private FMBroadcastReceiver mBroadcastReceiver = null;
    private HeadsetConnectionReceiver mHeadsetConnectionReceiver = null;
    
    private View.OnClickListener btnClickListener = new View.OnClickListener() {        
        public void onClick(View v) {
            if (v.getId() == R.id.FMR_Antenna_short) {
                switchAntenna(1);
                
            } else if (v.getId() == R.id.FMR_Antenna_long) {
                switchAntenna(0);
            }
            if (v.getId() == R.id.FMR_Stereomono_stereo) {
                if (!setStereoMono(false)) {
                    Toast.makeText(FMRadioEMActivity.this, "Set Stereo Mono failed.", Toast.LENGTH_SHORT).show();
                }
                
            } else if (v.getId() == R.id.FMR_Stereomono_mono) {
                if (!setStereoMono(true)) {
                    Toast.makeText(FMRadioEMActivity.this, "Set Stereo Mono failed.", Toast.LENGTH_SHORT).show();
                }
            } else if (v.getId() == R.id.FMR_Freq_tune) {
                String s = mEditFreq.getText().toString();
                float freq = 0;
                try {
                    freq = Float.valueOf(s);
                } catch (NumberFormatException e) {
                    Toast.makeText(FMRadioEMActivity.this, "bad float format.", Toast.LENGTH_SHORT).show();
                    mEditFreq.setText(formatStation(mCurrentStation));
                    return;
                }
                tuneToStation((int)(freq * CONVERT_RATE));
            }
//            else
//            {
//                //do nothing.
//            }
        }
    };
    
    // Called when the activity is first created. 
    public void onCreate(Bundle savedInstanceState) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onCreate");
       // mSavedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        // Bind the activity to FM audio stream.
        //setVolumeControlStream(AudioManager.STREAM_FM);
        setContentView(R.layout.fm_rx_em);
        
        mTextStereoMono = (TextView) findViewById(R.id.FMR_Status_Stereomono);
        mTextRssi = (TextView) findViewById(R.id.FMR_Status_RSSI);
        mTextCapArray = (TextView) findViewById(R.id.FMR_Status_Caparray);
        mTextRdsBler = (TextView) findViewById(R.id.FMR_RDS_Ratio);
        mTextRdsPS = (TextView) findViewById(R.id.FMR_RDS_PS);
        mTextRdsRT = (TextView) findViewById(R.id.FMR_RDS_RT);
        mTextChipID = (TextView) findViewById(R.id.FMR_Chip_ID);
        mTextECOVersion = (TextView) findViewById(R.id.FMR_ECO_Version);
        mTextPatchVersion = (TextView) findViewById(R.id.FMR_Patch_Version);
        mTextDSPVersion = (TextView) findViewById(R.id.FMR_DSP_Version);
        mEditFreq = (EditText) findViewById(R.id.FMR_Freq_edit);    
        mButtonTune = (Button) findViewById(R.id.FMR_Freq_tune);
        mRdAntennaS = (RadioButton) findViewById(R.id.FMR_Antenna_short);
        mRdAntennaL = (RadioButton) findViewById(R.id.FMR_Antenna_long);
        mRgAntenna = (RadioGroup) findViewById(R.id.FMR_Antenna_type);        
        mRdStereo = (RadioButton) findViewById(R.id.FMR_Stereomono_stereo);
        mRdMono = (RadioButton) findViewById(R.id.FMR_Stereomono_mono);
        mRgSM = (RadioGroup) findViewById(R.id.FMR_Stereomono_type); 
        
        if(mTextStereoMono == null
                || mTextRssi == null
                || mTextCapArray == null
                || mTextRdsBler == null
                || mTextRdsPS == null
                || mTextRdsRT == null
                || mEditFreq == null
                || mButtonTune == null
                || mRdAntennaS == null
                || mRdAntennaL == null
                || mRgAntenna == null
                || mRdStereo == null
                || mRdMono == null
                || mRgSM == null ) {
            FMRadioLogUtils.e(TAG, "clocwork worked...");    
            //not return and let exception happened.
        }    
        
        mRdAntennaS.setOnClickListener(btnClickListener);
        mRdAntennaL.setOnClickListener(btnClickListener);
        mRdStereo.setOnClickListener(btnClickListener);
        mRdMono.setOnClickListener(btnClickListener);
        mButtonTune.setOnClickListener(btnClickListener);
        

        // Should start FM service first.
        ComponentName cn = startService(new Intent(FMRadioEMActivity.this, FMRadioService.class));
        if (null == cn) {
            FMRadioLogUtils.e(TAG, "Error: Cannot start FM service");
        } else {
            FMRadioLogUtils.d(TAG, "Start FM service successfully.");
            mIsServiceStarted = true;

            mServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {
                    FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onServiceConnected");
                    mService = IFMRadioService.Stub.asInterface(service);
                    if (null == mService) {
                        FMRadioLogUtils.e(TAG, "Error: null interface");
                        finish();
                    } else {
                        if (!isServiceInit()) {
                            FMRadioLogUtils.d(TAG, "FM service is not init.");
                            initService(mCurrentStation);
                            refreshTextStatus(false);
                            InitialThread thread = new InitialThread();
                           // mSavedInstanceState = null;
                            thread.start();
                            if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    FMRadioLogUtils.e(TAG, "Exception: Thread.sleep.");
                                }
                            }
                        } else {
                            FMRadioLogUtils.d(TAG, "FM service is already init.");
                            if (isDeviceOpen()) {
                                // Get the current frequency in service and save it into database.
                                int iFreq = getFrequency();
                                if (iFreq > FMRadioStation.HIGHEST_STATION
                                    || iFreq < FMRadioStation.LOWEST_STATION) {
                                    FMRadioLogUtils.e(TAG, "Error: invalid frequency in service.");
                                } else {
                                    if (mCurrentStation != iFreq) {
                                        FMRadioLogUtils.d(TAG, "The frequency in FM service is not same as in database.");
                                        mCurrentStation = iFreq;
                                        // Save the current station frequency into data base.
                                        FMRadioStation.setCurrentStation(FMRadioEMActivity.this, mCurrentStation);
                                    } else {
                                        FMRadioLogUtils.d(TAG, "The frequency in FM service is same as in database.");
                                    }
                                }
                                
                                mIsPlaying = isPowerUp();
                                mIsRDSSupported = (1 == isRDSSupported()? true : false);
                                //... Check if RDS is enabled.
                                if (mIsRDSSupported) {
                                    mIsRDSEnabled = true;
                                } else {
                                    mIsRDSEnabled = false;
                                }

                                if (mIsPlaying) {
                                    FMRadioLogUtils.d(TAG, "FM is already power up.");
                                    refreshTextStatus(true);
                                }

                                if (mIsRDSSupported) {
                                    FMRadioLogUtils.d(TAG, "RDS is supported.");
                                    
                                    // Get RDS settings from FM server.
                                    mIsPSRTEnabled = isPSRTEnabled();
                                    mIsAFEnabled = isAFEnabled();

                                    if (mIsPlaying) {
                                        // Update the RDS text view.
                                        mPSString = getPS();
                                        mLRTextString = getLRText();
                                        Message msg = new Message();
                                        msg.setTarget(mHandler);
                                        Bundle bundle = new Bundle();
                                        bundle.putInt(TYPE_MSGID, MSGID_UPDATE_RDS);
                                        msg.setData(bundle);
                                        msg.sendToTarget();
                                    }
                                } else {
                                    FMRadioLogUtils.d(TAG, "RDS is not supported.");
                                }

                                // Now the activity is inited.
                                //mIsInited = true;
                            } else {
                                // This is theoretically never happen.
                                FMRadioLogUtils.e(TAG, "Error: FM device is not open");
                            }
                            Message msg = new Message();
                            msg.setTarget(mHandler);
                            Bundle bundle = new Bundle();
                            bundle.putInt(TYPE_MSGID, MSGID_INIT_OK);
                            msg.setData(bundle);
                            msg.sendToTarget();
                            
                            
                        }
                    }
                    FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onServiceConnected");
                }

                public void onServiceDisconnected(ComponentName className) {
                    FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onServiceDisconnected");
                    mService = null;
                    FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onServiceDisconnected");
                }
            };
            mIsServiceBinded = bindService(
                new Intent(FMRadioEMActivity.this, FMRadioService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
        }
        if (!mIsServiceBinded) {
            FMRadioLogUtils.e(TAG, "Error: Cannot bind FM service");
            finish();
            FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.onCreate");
            return;
        } else {
            FMRadioLogUtils.d(TAG, "Bind FM service successfully.");
        }

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FMRadioService.ACTION_STATE_CHANGED);
        filter.addAction(FMRadioService.ACTION_RDS_PS_CHANGED);
        filter.addAction(FMRadioService.ACTION_RDS_RT_CHANGED);
        filter.addAction(FMRadioService.ACTION_RDS_AF_ACTIVED);
        filter.addAction(FMRadioService.ACTION_RDS_TA_ACTIVED);
        filter.addAction(FMRadioService.ACTION_RDS_TA_DEACTIVED);
        mBroadcastReceiver = new FMBroadcastReceiver();
        FMRadioLogUtils.d(TAG, "Register broadcast receiver.");
        registerReceiver(mBroadcastReceiver, filter);
        
        
        IntentFilter filterHeadset = new IntentFilter();
        filterHeadset.addAction(Intent.ACTION_HEADSET_PLUG);
        mHeadsetConnectionReceiver = new HeadsetConnectionReceiver();
        FMRadioLogUtils.d(TAG, "Register HeadsetConnectionReceiver");
        registerReceiver(mHeadsetConnectionReceiver, filterHeadset);

        // Get all the views and set their actions.
        mCurrentStation = FMRadioStation.getCurrentStation(this);
        
        // When created, detect whether the antenna exists.
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                FMRadioLogUtils.d(TAG, ">>> handleMessage ID: " + msg.getData().getInt(TYPE_MSGID));
                if (mIsDestroying) {
                    FMRadioLogUtils.d(TAG, "Warning: app is being destroyed.");
                    FMRadioLogUtils.d(TAG, "<<< handleMessage");
                    return;
                }
                
                if (MSGID_RETRY == msg.getData().getInt(TYPE_MSGID)) {
                    if (!isAntennaAvailable()) {
                        FMRadioLogUtils.i(TAG, "The antenna is still not pluged in.");
                    }
//                    else {
//                    }
                } else if (MSGID_OK == msg.getData().getInt(TYPE_MSGID)) {
                    FMRadioLogUtils.i(TAG, "should search channel");
                } else if (MSGID_UPDATE_RDS == msg.getData().getInt(TYPE_MSGID)) {
                    showRDS();
                } else if (MSGID_UPDATE_CURRENT_STATION == msg.getData().getInt(TYPE_MSGID)) {
                    // Save the current station frequency into data base.
                    FMRadioStation.setCurrentStation(FMRadioEMActivity.this, mCurrentStation);
                                    
                } else if (MSGID_PLAY_FINISH == msg.getData().getInt(TYPE_MSGID)) {
                    refreshTextStatus(true);
                } else if (MSGID_PLAY_FAIL == msg.getData().getInt(TYPE_MSGID)) {
                    refreshTextStatus(false);
                } else if (MSGID_SHOW_TOAST== msg.getData().getInt(TYPE_MSGID)) {
                    String text = msg.getData().getString(TYPE_TOAST_STRING);
                    showToast(text);
                } else if(MSGID_INIT_OK== msg.getData().getInt(TYPE_MSGID)) {
                    FMRadioLogUtils.d(TAG, "ENTER MSGID_INIT_OK");
                    // Get RDS settings from database and set them into FM service.
                    mIsPSRTEnabled = FMRadioStation.getEnablePSRT(FMRadioEMActivity.this);
                    mIsPSRTEnabled = true;//always true in EM.
                    mIsAFEnabled= FMRadioStation.getEnableAF(FMRadioEMActivity.this);
                    FMRadioStation.setEnablePSRT(FMRadioEMActivity.this, mIsPSRTEnabled);
                    enablePSRT(mIsPSRTEnabled);
                    enableAF(mIsAFEnabled);
                    
                    // Update the earphone button.
                    if (isEarphoneUsed()) {
                        switchAntenna(0);
                        mRgAntenna.check(R.id.FMR_Antenna_long);
                    } else {
                        switchAntenna(1);
                         mRgAntenna.check(R.id.FMR_Antenna_short);
                    }
                    mTextChipID.setText(getChipId());
                    mTextECOVersion.setText(getECOVersion());
                    mTextPatchVersion.setText(getPatchVersion());
                    mTextDSPVersion.setText(getDSPVersion());
                    FMRadioLogUtils.i(TAG, "Leave MSGID_INIT_OK");
                } else {
                    // update timer.
                    if (MSGID_TICK_EVENT == msg.what) {
                        FMRadioLogUtils.d(TAG, "MSGID_TICK_EVENT msg arrived.");
                        if (isDeviceOpen()) {
                            mTextRdsBler.setText(String.format("%d%%", readRdsBler()));
                            mTextRssi.setText(String.format("%d", readRssi()));
                            boolean stereo = getStereoMono();
                            mTextStereoMono.setText(stereo? "Stereo":"Mono");
                            
                            if (mRdAntennaL.isChecked()) {
                                mTextCapArray.setText("N/A");
                            } else {
                                mTextCapArray.setText(formatCapArray(readCapArray()));
                            }
                            
                        }
                        sendEmptyMessageDelayed(MSGID_TICK_EVENT, 300); 
                    } else {
                        FMRadioLogUtils.e(TAG, "Error: undefined message ID.");
                    }                    
                }
                
                
                FMRadioLogUtils.d(TAG, "<<< handleMessage");
            }
        };
        
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        refreshTextStatus(mIsPlaying);
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onCreate");
    }
    private String formatCapArray(int raw) {
        final float[] pF= {0.166f, 0.332f, 0.664f, 1.33f
                , 2.66f, 5.31f, 10.6f, 18.6f};
        
        float sum = 0.0f;
        for (int i=0; i<8; i++) {
            sum += (((raw >> (6+i)) & 0x1) == 1 ? pF[i] : 0.0);
        }
        return String.format("%.2f", sum);
    }
    
    public void onStart() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onStart");
        super.onStart();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onStart");
    }
    
    public void onResume() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onResume");
        mHandler.sendEmptyMessageDelayed(MSGID_TICK_EVENT, 1000);
        super.onResume();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onResume");
    }
    
    public void onPause() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onPause");
        mHandler.removeMessages(MSGID_TICK_EVENT);
        super.onPause();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onPause");
    }
    
    public void onStop() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onStop");
        super.onStop();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onStop");
    }
    
    public void onDestroy() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onDestroy");
        mIsDestroying = true;
        // If searching, we should stop it and dismiss the progress dialog.
            
        // Unregister the broadcast receiver.
        if (null != mBroadcastReceiver) {
            FMRadioLogUtils.d(TAG, "Unregister broadcast receiver.");
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        
        if (null != mHeadsetConnectionReceiver) {
            FMRadioLogUtils.d(TAG, "Unregister headset broadcast receiver.");
            unregisterReceiver(mHeadsetConnectionReceiver);
            mHeadsetConnectionReceiver = null;
        }

        //LXO modify always true;
        mIsExitPressed = true;
        if (mIsExitPressed) {
            // Should powerdown FM.
            if (mIsPlaying) {
                FMRadioLogUtils.d(TAG, "FM is Playing. So stop it.");
                setMute(true);
                rdsset(false);
                powerDown();
                mIsPlaying = false;
            }
        }
        
        // Unbind the FM service.
        if (mIsServiceBinded) {
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }
        
        if (mIsExitPressed) {
            if (mIsServiceStarted) {
                boolean bRes = stopService(new Intent(FMRadioEMActivity.this,FMRadioService.class));
                if (!bRes) {
                    FMRadioLogUtils.d(TAG, "Error: Cannot stop the FM service.");
                }
                mIsServiceStarted = false;
            }
        }
        
        super.onDestroy();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onDestroy");
    }
    
    class InitialThread extends Thread{
        //private Bundle savedInstanceState;

        public InitialThread(){
            //savedInstanceState = params;
        }
        
        public void run(){            
            if (!openDevice()) {
                //If failed, exit?
                FMRadioLogUtils.e(TAG, "Error: opendev failed.");
            } else {
                FMRadioLogUtils.d(TAG, "opendev succeed.");
            }
            Message msgi = new Message();
            msgi.setTarget(mHandler);
            Bundle bundlei = new Bundle();
            bundlei.putInt(TYPE_MSGID, MSGID_INIT_OK);
            msgi.setData(bundlei);
            msgi.sendToTarget();

            // Check if RDS is supported.
            mIsRDSSupported = (1 == isRDSSupported()? true : false);

            //... Check if RDS is enabled.
            if (mIsRDSSupported) {
                mIsRDSEnabled = true;
            } else {
                mIsRDSEnabled = false;
            }
            
            // The app maybe killed at the previous time. So after opendev, get the power state.
            mIsPlaying = isPowerUp();
            if (mIsPlaying) {
                Message msg = new Message();
                msg.setTarget(mHandler);
                Bundle bundle = new Bundle();
                bundle.putInt(TYPE_MSGID, MSGID_PLAY_FINISH);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                playFM();
            }
            
            // Now the activity is inited.
           // mIsInited = true;
            FMRadioLogUtils.e(TAG, "InitialThread terminated.");
        }
    }
    
        
    private boolean isAntennaAvailable() {
        if (!FeatureOption.MTK_MT519X_FM_SUPPORT) {
            return mAudioManager.isWiredHeadsetOn();
        } else {
            return true;
        }
    }    

    
    private void onPlayFM() {
        FMRadioLogUtils.d(TAG, ">>> onPlayFM");
        
        refreshTextStatus(false);
        new Thread() {
            public void run(){
                playFM();
            }
        }.start();
        
        if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                FMRadioLogUtils.e(TAG, "Exception: Thread.sleep.");
            }
        }
        //startAnimation();
        
        FMRadioLogUtils.d(TAG, "<<< onPlayFM");
    }
    
    private void playFM() {
        FMRadioLogUtils.d(TAG, ">>> PlayFM");
        
        setMute(true);
        rdsset(false);
        boolean bRes = powerUp((float)mCurrentStation / CONVERT_RATE);
        if (bRes) {
            rdsset(true);
        
            mIsPlaying = true;
            setMute(false);
            
            if (!isAntennaAvailable()) {
                    switchAntenna(1); //short antenna.
            }    
                
            Message msg = new Message();
            msg.setTarget(mHandler);
            Bundle bundle = new Bundle();
            bundle.putInt(TYPE_MSGID, MSGID_PLAY_FINISH);
            msg.setData(bundle);
            msg.sendToTarget();
            
            // Hold the wake lock.
            //mWakeLock.acquire();
        } else {
            setMute(true);            
            powerDown();
            mIsPlaying = false;
            
            Message msg = new Message();
            msg.setTarget(mHandler);
            Bundle bundle = new Bundle();
            bundle.putInt(TYPE_MSGID, MSGID_PLAY_FAIL);
            msg.setData(bundle);
            msg.sendToTarget();
            
            FMRadioLogUtils.e(TAG, "Error: Can not power up.");
        }
        FMRadioLogUtils.d(TAG, "<<< PlayFM");
    }
    
    private void onPauseFM() {
        FMRadioLogUtils.d(TAG, ">>> onPauseFM");
        setMute(true);
            
        rdsset(false);
        if (powerDown()) {
            mIsPlaying = false;
            refreshTextStatus(false);
        } else {
            FMRadioLogUtils.e(TAG, "Error: Can not power down.");
        }
        
        FMRadioLogUtils.d(TAG, "<<< onPauseFM");
    }
    
    private void onUseEarphone() {
        FMRadioLogUtils.d(TAG, ">>> onUseEarphone");
        useEarphone(true);
        FMRadioLogUtils.d(TAG, "<<< onUseEarphone");
    }
    
    private void onUseLoudspeaker() {
        FMRadioLogUtils.d(TAG, ">>> onUseLoudspeaker");
        useEarphone(false);
        FMRadioLogUtils.d(TAG, "<<< onUseLoudspeaker");
    }
    
    private void tuneToStation(int iStation) {
        FMRadioLogUtils.v(TAG, ">>> tuneToStation: " + (float)iStation / CONVERT_RATE);
        if (mIsPlaying) {
            FMRadioLogUtils.d(TAG, "FM is playing.");
            rdsset(false);
            boolean bRes = tune((float)iStation / CONVERT_RATE);
            if (bRes) {
                FMRadioLogUtils.i(TAG, "Tune to the station succeeded.");
                rdsset(true);
                mCurrentStation = iStation;
                // Save the current station frequency into data base.
                FMRadioStation.setCurrentStation(this, mCurrentStation);
            } else {
                FMRadioLogUtils.e(TAG, "Error: Can not tune to the station.");
            }
        } else {
            FMRadioLogUtils.d(TAG, "FM is paused.");
            mCurrentStation = iStation;
            // Save the current station frequency into data base.
            FMRadioStation.setCurrentStation(this, mCurrentStation);
            
            onPlayFM();
        }
        
        FMRadioLogUtils.v(TAG, "<<< tuneToStation");
    }
    
    
    private void refreshTextStatus(boolean on){        
        if (!on) {
            mTextStereoMono.setText("X");
            mTextRssi.setText("X");
            mTextCapArray.setText("X");
            mTextRdsBler.setText("X");
            mTextRdsPS.setText("X");
            mTextRdsRT.setText("X");    
            mEditFreq.setText(formatStation(mCurrentStation));                
            mRgAntenna.clearCheck();
            mButtonTune.setEnabled(false);            
        } else {
            mButtonTune.setEnabled(true);
        }
    }
    
    protected void onSaveInstanceState(Bundle outState) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(FM_SAVE_INSTANCE_STATE_INITED, true);
        outState.putBoolean(FM_SAVE_INSTANCE_STATE_PLAYING, mIsPlaying);
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onSaveInstanceState");
    }
    
    public void onBackPressed() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onBackPressed");
        super.onBackPressed();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onBackPressed");
    }
    
    private int rdsset(boolean rdson) {
        int iRet = -1;
        if (mIsRDSEnabled) {
            iRet = setRDS(rdson);
        }
//        else {
//            // Do nothing.
//        }
        return iRet;
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onConfigurationChanged");
    }

    private void showToast(CharSequence text) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.showToast: " + text);
        // Schedule a timer to clear the toast.
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Clear the timer and toast.
                cancelToast();
            }
        }, TOAST_TIMER_DELAY, TOAST_TIMER_DELAY);
        
        // Toast it.
        mToast = Toast.makeText(FMRadioEMActivity.this, text, Toast.LENGTH_SHORT);
        mToast.show();
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.showToast");
    }

    private void cancelToast() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.cancelToast");
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        } else {
            FMRadioLogUtils.d(TAG, "Warning: The timer is null.");
        }
        if (null != mToast) {
            mToast.cancel();
            mToast = null;
        } else {
            FMRadioLogUtils.d(TAG, "Warning: The toast is null.");
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.cancelToast");
    }

    private boolean isToasting() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isToasting");
        boolean bRet = true;
        if (null == mToast && null == mTimer) {
            bRet = false;
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isToasting: " + bRet);
        return bRet;
    }

    private void showRDS() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.showRDS");        
        mTextRdsPS.setText(mPSString);
        mTextRdsRT.setText(mLRTextString);
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.showRDS");
    }

    public void onLowMemory() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioEMActivity.onLowMemory");
        super.onLowMemory();
        FMRadioLogUtils.d(TAG, "<<< FMRadioEMActivity.onLowMemory");
    }
    public String getChipId() {
        int[] hardwareVersion = getHardwareVersion();
        int chipId = 0;
        if (null != hardwareVersion && hardwareVersion.length >= 1) {
            chipId = hardwareVersion[0];
        }
        return getStringValue(chipId);
    }
    
    public String getECOVersion() {
        int[] hardwareVersion = getHardwareVersion();
        int ecoVersion = 0;
        if (null != hardwareVersion && hardwareVersion.length >= 2) {
            ecoVersion = hardwareVersion[1];
        }
        String ecoVersionStr = "E" + getStringValue(ecoVersion);
        return ecoVersionStr;
    }
    
    public String getPatchVersion() {
        int[] hardwareVersion = getHardwareVersion();
        int patchVersion = 0;
        if (null != hardwareVersion && hardwareVersion.length >= 4) {
            patchVersion = hardwareVersion[3];
        }
        String patchStr = getStringValue(patchVersion);
        float patch = 0;
        try {
            patch = Float.parseFloat(patchStr)/100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Float.toString(patch);
    }
    
    public String getDSPVersion() {
        int[] hardwareVersion = getHardwareVersion();
        int dspVersion = 0;
        if (null != hardwareVersion && hardwareVersion.length >= 3) {
            dspVersion = hardwareVersion[2];
        }
        String dspVersionStr = "V" + getStringValue(dspVersion);
        return dspVersionStr;
    }
    
    // Wrap service functions.
    private boolean openDevice() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.openDevice");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.openDevice();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.openDevice: " + bRet);
        return bRet;
    }

    private boolean closeDevice() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.closeDevice");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.closeDevice();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.closeDevice: " + bRet);
        return bRet;
    }
    
    private boolean isDeviceOpen() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isDeviceOpen");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isDeviceOpen();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isDeviceOpen: " + bRet);
        return bRet;
    }
    
    private boolean powerUp(float frequency) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.powerUp");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.powerUp(frequency);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.powerUp: " + bRet);
        return bRet;
    }
    
    private boolean powerDown() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.powerDown");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.powerDown();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.powerDown: " + bRet);
        return bRet;
    }
    
    private boolean isPowerUp() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isPowerUp");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isPowerUp();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isPowerUp: " + bRet);
        return bRet;
    }
    
    private boolean tune(float frequency) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.tune");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.tune(frequency);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.tune: " + bRet);
        return bRet;
    }
    
    private float seek(float frequency, boolean isUp) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.seek");
        float fRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                fRet = mService.seek(frequency, isUp);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.seek: " + fRet);
        return fRet;
    }
    
    private int[] startScan() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.startScan");
        int[] iChannels = null;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iChannels = mService.startScan();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.startScan: " + Arrays.toString(iChannels));
        return iChannels;
    }
    
    private boolean stopScan() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.stopScan");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.stopScan();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.stopScan: " + bRet);
        return bRet;
    }
    
    private int setRDS(boolean on) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.setRDS");
        int iRet = -1;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.setRDS(on);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.setRDS: " + iRet);
        return iRet;
    }
    
    private int readRDS() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.readRDS");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.readRDS();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.readRDS: " + iRet);
        return iRet;
    }
    
    private String getPS() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.getPS");
        String sPS = null;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                sPS = mService.getPS();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.getPS: " + sPS);
        return sPS;
    }
    
    private String getLRText() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.getLRText");
        String sRT = null;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                sRT = mService.getLRText();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.getLRText: " + sRT);
        return sRT;
    }
    
    private int activeAF() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.activeAF");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.activeAF();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.activeAF: " + iRet);
        return iRet;
    }
    
    private int activeTA() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.activeTA");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.activeTA();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.activeTA: " + iRet);
        return iRet;
    }
    
    private int deactiveTA() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.deactiveTA");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.deactiveTA();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.deactiveTA: " + iRet);
        return iRet;
    }
    
    private int setMute(boolean mute) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.setMute");
        int iRet = -1;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.setMute(mute);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.setMute: " + iRet);
        return iRet;
    }
    
    private int isRDSSupported() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isRDSSupported");
        int iRet = -1;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.isRDSSupported();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isRDSSupported: " + iRet);
        return iRet;
    }

    private void useEarphone(boolean use) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.useEarphone: " + use);
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                mService.useEarphone(use);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.useEarphone");
    }
    
    private boolean isEarphoneUsed() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isEarphoneUsed");
        boolean bRet = true;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isEarphoneUsed();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isEarphoneUsed: " + bRet);
        return bRet;
    }
    
    private void initService(int iCurrentStation) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.initService: " + iCurrentStation);
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                mService.initService(iCurrentStation);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.initService");
    }
    
    private boolean isServiceInit() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isServiceInit");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isServiceInit();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isServiceInit: " + bRet);
        return bRet;
    }

    private void enablePSRT(boolean enable) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.enablePSRT: " + enable);
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                mService.enablePSRT(enable);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.enablePSRT");
    }
        
    private void enableAF(boolean enable) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.enableAF: " + enable);
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                mService.enableAF(enable);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.enableAF");
    }
    
    private void enableTA(boolean enable) {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.enableTA: " + enable);
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                mService.enableTA(enable);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.enableTA");
    }
    
    private boolean isPSRTEnabled() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isPSRTEnabled");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isPSRTEnabled();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isPSRTEnabled: " + bRet);
        return bRet;
    }
    
    private boolean isAFEnabled() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isAFEnabled");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isAFEnabled();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isAFEnabled: " + bRet);
        return bRet;
    }
    
    private boolean isTAEnabled() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.isTAEnabled");
        boolean bRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                bRet = mService.isTAEnabled();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.isTAEnabled: " + bRet);
        return bRet;
    }

    private int getFrequency() {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.getFrequency");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.getFrequency();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.getFrequency: " + iRet);
        return iRet;
    }
    

    //LXO add.
    public int readCapArray()
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.readCapArray");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.readCapArray();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.readCapArray: " + iRet);
        return iRet;
    }
    public int readRssi()
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.readRssi");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.readRssi();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.readRssi: " + iRet);
        return iRet;
    }
    public boolean getStereoMono()
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.getStereoMono");
        boolean iRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.getStereoMono();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.getStereoMono: " + iRet);
        return iRet;
    }
    public boolean setStereoMono(boolean isMono)
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.setStereoMono");
        boolean iRet = false;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.setStereoMono(isMono);
            }
            catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.setStereoMono: " + iRet);
        return iRet;
    }
    public int switchAntenna(int type)  //0 success, 1 fail, 2 not support.
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.switchAntenna");
        int iRet = 2; //not supported short antenna.
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                iRet = mService.switchAntenna(type);
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.switchAntenna: " + iRet);
        return iRet;
    }
    public int readRdsBler()
    {
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.readRdsBler");
        int iRet = 0;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                iRet = mService.readRdsBler();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.readRdsBler: " + iRet);
        return iRet;
    }
    
    private String formatStation(int station) {
        float frequency = (float)station / CONVERT_RATE;
        String result = null;
        if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
            result = String.format(Locale.ENGLISH, "%.2f", Float.valueOf(frequency));
        } else {
            result = String.format(Locale.ENGLISH, "%.1f", Float.valueOf(frequency));
        }
        return result;
    }
    
    public int[] getHardwareVersion(){
        FMRadioLogUtils.v(TAG, ">>> FMRadioEMActivity.getHardwareversion");
        int[] hardwareVersion = null;
        if (null == mService) {
            FMRadioLogUtils.e(TAG, "Error: No service interface.");
        } else {
            try {
                hardwareVersion = mService.getHardwareVersion();
            } catch (Exception e) {
                FMRadioLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMRadioLogUtils.v(TAG, "<<< FMRadioEMActivity.getHardwareversion: " + Arrays.toString(hardwareVersion));
        return hardwareVersion;
    }

    public String getStringValue(int convertData) {
        StringBuilder temp = new StringBuilder();
        int quotient = convertData;
        int remainder = 0; 
        while(quotient > 0) {
            remainder = quotient % 16;
            quotient = quotient / 16;
            temp = temp.append(remainder); 
        }
        return temp.reverse().toString();
    }
         
}
