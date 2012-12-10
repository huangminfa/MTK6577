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

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.content.res.Configuration;
import android.media.AudioManager;
import java.lang.Exception;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Locale;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.Phone;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
//import android.os.Process;
import android.os.ServiceManager;
import android.os.Binder;
import android.os.IBinder;
import android.bluetooth.BluetoothA2dp;
import android.server.BluetoothA2dpService;
import android.bluetooth.IBluetoothA2dp;
import java.io.IOException;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnErrorListener;
import com.mediatek.featureoption.FeatureOption;

public class FMRadioService extends Service implements FMRecorder.onRecorderStateChangedListener {
    public static final String TAG = "FMRadioService";

    // Broadcast messages from FM service to clients.
    public static final String ACTION_STATE_CHANGED = "com.mediatek.FMRadio.FMRadioService.ACTION_STATE_CHANGED";
    public static final String EXTRA_FMRADIO_ISPOWERUP = "EXTRA_FMRADIO_ISPOWERUP"; // boolean
    public static final String ACTION_RDS_PS_CHANGED = "com.mediatek.FMRadio.FMRadioService.ACTION_RDS_PS_CHANGED";
    public static final String EXTRA_RDS_PS = "EXTRA_RDS_PS"; // String
    public static final String ACTION_RDS_RT_CHANGED = "com.mediatek.FMRadio.FMRadioService.ACTION_RDS_RT_CHANGED";
    public static final String EXTRA_RDS_RT = "EXTRA_RDS_RT"; // String
    public static final String ACTION_RDS_AF_ACTIVED = "com.mediatek.FMRadio.FMRadioService.ACTION_RDS_AF_ACTIVED";
    public static final String EXTRA_RDS_AF_ACTIVED = "EXTRA_RDS_AF_ACTIVED"; // int
    public static final String ACTION_RDS_TA_ACTIVED = "com.mediatek.FMRadio.FMRadioService.ACTION_RDS_TA_ACTIVED";
    public static final String EXTRA_RDS_TA_ACTIVED = "EXTRA_RDS_TA_ACTIVED"; // int
    public static final String ACTION_RDS_TA_DEACTIVED = "com.mediatek.FMRadio.FMRadioService.ACTION_RDS_TA_DEACTIVED";
    public static final String EXTRA_RDS_TA_DEACTIVED = "EXTRA_RDS_TA_DEACTIVED"; // int

    public static final String ACTION_RECORDING_STATE_CHANGED = "com.mediatek.FMRadio.FMRadioService.ACTION_RECORDING_STATE_CHANGED";
    public static final String EXTRA_RECORDING_STATE = "EXTRA_RECORDING_STATE";
    public static final String ACTION_RECORDER_ERROR = "com.mediatek.FMRadio.FMRadioService.ACTION_RECORDER_ERROR";
    public static final String EXTRA_RECORDER_ERROR_STATE = "com.mediatek.FMRadio.FMRadioService.EXTRA_RECORDER_ERROR_STATE";
    // Broadcast message for recorder mode change due to other event's interruption, e.g. FM over BT
    // is on
    public static final String ACTION_RECORDING_MODE_CHANGED = "com.mediatek.FMRadio.FMRadioService.ACTION_RECORDING_MODE_CHANGED";
    public static final String EXTRA_RECORDING_MODE = "com.mediatek.FMRadio.FMRadioService.EXTRA_RECORDING_MODE";

    public static final String ACTION_EXIT_FMSERVICE = "com.mediatek.FMRadio.FMRadioService.ACTION_EXIT_FMSERVICE";
    
    // Broadcast messages from clients to FM service.
    public static final String ACTION_TOFMSERVICE_POWERDOWN = "com.mediatek.FMRadio.FMRadioService.ACTION_TOFMSERVICE_POWERDOWN";
    // Broadcast messages to FM Tx service.
    public static final String ACTION_TOFMTXSERVICE_POWERDOWN = "com.mediatek.FMTransmitter.FMTransmitterService.ACTION_TOFMTXSERVICE_POWERDOWN";
    // Broadcast messages to mATV service.
    public static final String ACTION_TOATVSERVICE_POWERDOWN = "com.mediatek.app.mtv.ACTION_REQUEST_SHUTDOWN";
    // Broadcast messages to music service.
    public static final String ACTION_TOMUSICSERVICE_POWERDOWN = "com.android.music.musicservicecommand.pause";
    // Broadcast messages from mATV service.
    public static final String ACTION_FROMATVSERVICE_POWERUP = "com.mediatek.app.mtv.POWER_ON";
    // Broadcast to tell A2DP that FM has powered up / powered down
    public static final String MSG_FM_POWER_UP = "com.mediatek.FMRadio.FMRadioService.ACTION_TOA2DP_FM_POWERUP";
    public static final String MSG_FM_POWER_DOWN = "com.mediatek.FMRadio.FMRadioService.ACTION_TOA2DP_FM_POWERDOWN";
    // Broadcast messages from other sounder APP to FM service
    public static final String MESSAGE_FROMSOUNDER_TOFM_POWERDOWN = "com.android.music.musicservicecommand";
    public static final String CMDPAUSE = "pause";
    public static final int CONVERT_RATE = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 100 : 10;

    private IBluetoothA2dp mA2dpService = null;
    private boolean mIsStopPressed = false;

    private boolean mUsingFMViaBTController = false;
    private IBinder mICallBack = new Binder();
    // FM recorder
    FMRecorder mFMRecorder = null;
    private BroadcastReceiver mSDListener = null;

    // RDS events
    public static final int RDS_EVENT_FLAGS = 0x0001;
    public static final int RDS_EVENT_PI_CODE = 0x0002;
    public static final int RDS_EVENT_PTY_CODE = 0x0004;
    public static final int RDS_EVENT_PROGRAMNAME = 0x0008;
    public static final int RDS_EVENT_UTCDATETIME = 0x0010;
    public static final int RDS_EVENT_LOCDATETIME = 0x0020;
    public static final int RDS_EVENT_LAST_RADIOTEXT = 0x0040;
    public static final int RDS_EVENT_AF = 0x0080;
    public static final int RDS_EVENT_AF_LIST = 0x0100;
    public static final int RDS_EVENT_AFON_LIST = 0x0200;
    public static final int RDS_EVENT_TAON = 0x0400;
    public static final int RDS_EVENT_TAON_OFF = 0x0800;
    public static final int RDS_EVENT_RDS = 0x2000;
    public static final int RDS_EVENT_NO_RDS = 0x4000;
    public static final int RDS_EVENT_RDS_TIMER = 0x8000;

    private static final int NOTIFICATION_ID = 1;
    

    private FMServiceBroadcastReceiver mBroadcastReceiver = null;

    // RDS Strings.
    private String mPSString = "";
    private String mLRTextString = "";

    // RDS settings
    private boolean mIsPSRTEnabled = false;
    private boolean mIsAFEnabled = false;
    private boolean mIsTAEnabled = false;

    private boolean mIsSearching = false;
    private boolean mIsStopScanCalled = false;
    private Thread mRDSThread = null;
    private boolean mIsExit = false;
    private boolean mIsRDSThreadNeedDie = false;
    private boolean mIsEarphoneUsed = true;
    private boolean mIsHeadSetPlugInOut = false;
    private boolean mIsDeviceOpen = false;
    private boolean mIsPowerUp = false;
    private boolean mIsServiceInit = false;
    private int mCurrentStation = FMRadioStation.FIXED_STATION_FREQ;
    private int mValueHeadSetPlug = 1;
    private AudioManager mAudioManager = null;
    private MediaPlayer mFMPlayer = null;
    private WakeLock mWakeLock = null;

    // Audio Manager parameters
    private static final String AUDIO_PATH_LOUDSPEAKER = "AudioSetForceToSpeaker=1";
    private static final String AUDIO_PATH_EARPHONE = "AudioSetForceToSpeaker=0";
    private static final String FM_AUDIO_ENABLE = "AudioSetFmEnable=1";
    private static final String FM_AUDIO_DISABLE = "AudioSetFmEnable=0";

    private static final int HEADSET_PLUG_IN = 1;
    
    // Interact with phone call.
    private boolean mIsSIM1Idle = true;
    private boolean mIsSIM2Idle = true;

    private boolean mIsRecording = false;
    private boolean mIsConnectBluetooth = false;
    private String mDefaultSDCardPath = null;
    
    private boolean mIsResumeAfterCall = false;
    private FMPhoneStateListener mPhoneStateListener1 = null;
    private FMPhoneStateListener mPhoneStateListener2 = null;

    private static WeakReference<FMRadioService> sFMService = null;

    private class FMServiceBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.onReceive");
            String action = intent.getAction();
            String command = intent.getStringExtra("command");
            FMRadioLogUtils.d(TAG, "Action/Command: " + action + " / " + command);
            if (action.equals(ACTION_TOFMSERVICE_POWERDOWN)
                    || ACTION_FROMATVSERVICE_POWERUP.equals(action)
                    || (action.equals(MESSAGE_FROMSOUNDER_TOFM_POWERDOWN) 
                            && CMDPAUSE.equals(command))) {
                mIsResumeAfterCall = false;

                if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
                    int fmState = mFMRecorder.getState();
                    if (fmState == FMRecorder.STATE_PLAYBACK) {
                        mFMRecorder.stopPlayback();
                    } else if (fmState == FMRecorder.STATE_RECORDING) {
                        mFMRecorder.stopRecording();
                    }
                }

                if (mIsSearching) {
                    try {
                        mIsStopScanCalled = true;
                        mBinder.stopScan();
                    } catch (Exception e) {
                        FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                    }
                }
                if (mIsPowerUp) {
                    try {
                        mBinder.powerDown();
                    } catch (Exception e) {
                        FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                    }
                } else {
                    FMRadioLogUtils.v(TAG, "FM is not playing, so do nothing.");
                }
            } else if (action.equals(Intent.ACTION_SHUTDOWN)
                    || action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                try {
                    if (!mIsEarphoneUsed) {
                        mBinder.useEarphone(true);
                    }
                    if (mIsPowerUp) {
                        mBinder.powerDown();
                    }
                    mBinder.closeDevice();
                } catch (Exception e) {
                    FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                }

                if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
                    int fmState = mFMRecorder.getState();
                    if (fmState == FMRecorder.STATE_PLAYBACK) {
                        mFMRecorder.stopPlayback();
                    } else if (fmState == FMRecorder.STATE_RECORDING) {
                        mFMRecorder.stopRecording();
                    }
                }
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                new Thread() {
                    public void run() {
                        try {
                            if (mIsPowerUp) {
                                mBinder.setRDS(true);
                            }
                        } catch (Exception e) {
                            FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                        }
                    }
                }.start();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                new Thread() {
                    public void run() {
                        try {
                            if (mIsPowerUp) {
                                mBinder.setRDS(false);
                            }
                        } catch (Exception e) {
                            FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                        }
                    }
                }.start();
            } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                FMRadioLogUtils.d(TAG, "ACTION_HEADSET_PLUG");
                if (!mIsPowerUp) {
                    // If not even powered up, just return and do nothing
                    FMRadioLogUtils.w(TAG, "ACTION_HEADSET_PLUG: FM not powered up yet!!");
                    return;
                }
//                int state = intent.getIntExtra("state", -1);
                mValueHeadSetPlug = (intent.getIntExtra("state", -1) == HEADSET_PLUG_IN) ? 0 : 1;
                if (mIsSearching) {
                    mIsHeadSetPlugInOut = true;
                } else {
                    int ret = -1;
                    try {
                        ret = mBinder.switchAntenna(mValueHeadSetPlug);
                        FMRadioLogUtils.d(TAG, "ACTION_HEADSET_PLUG:switch antenna finished");
                    } catch (Exception ex) {
                        FMRadioLogUtils.e(TAG, "Exception: switchAntenna()");
                    }
                    if (ret != 0) {
                        FMRadioLogUtils.e(TAG, "ACTION_HEADSET_PLUG: Cannot switch to short antenna: " + ret);
                    }
                } 
            } else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                // Change FM chip status according to A2dp sink state and current FM chip
                // state
                int sinkState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, 0);
                FMRadioLogUtils.d(TAG, "ACTION_SINK_STATE_CHANGED: sinkState=" + sinkState + ", ispowerup="
                        + mIsPowerUp);
                if (mIsPowerUp) {
                    switch (sinkState) {
                    case BluetoothA2dp.STATE_CONNECTED:
                    case BluetoothA2dp.STATE_PLAYING:
                        mIsConnectBluetooth = true;
                        if (!mUsingFMViaBTController) {
                            FMRadioLogUtils.d(TAG,
                                    "ACTION_SINK_STATE_CHANGED: disable FM audio first to avoid I2S noise!!");
                            enableFMAudio(false);
                            Intent i = new Intent(MSG_FM_POWER_UP);
                            FMRadioLogUtils.d(TAG, MSG_FM_POWER_UP + " sent to A2dp service!!");
                            sendBroadcast(i);
                        } else {
                            FMRadioLogUtils.d(TAG,
                                    "SINK_STATE_CHANGED: FM over BT already enabled, ignore this message");
                        }
                        break;
                    case BluetoothA2dp.STATE_DISCONNECTED:
                    case BluetoothA2dp.STATE_DISCONNECTING:
                        mIsConnectBluetooth = false;
                        if (!FMRadioNative.setFMViaBTController(false)) {
                            FMRadioLogUtils.e(TAG, "failed to set FM over BT via Host!!");
                        } else {
                            FMRadioLogUtils.d(TAG, "setFMViaBTController(false) succeeded!!");
                            mUsingFMViaBTController = false;
                            Intent intent_recorder = new Intent(ACTION_RECORDING_MODE_CHANGED);
                            intent_recorder.putExtra(EXTRA_RECORDING_MODE, true);
                            sendBroadcast(intent_recorder);
                            enableFMAudio(true);
                        }
                        break;
                    default:
                        break;
                    }
                }
            } else if (action.equals(BluetoothA2dpService.ACTION_FM_OVER_BT_CONTROLLER)) {
                // change to controller if necessary
                // stop FM player btw.
                int fmOverBTState = intent.getIntExtra(BluetoothA2dpService.EXTRA_RESULT_STATE,
                        BluetoothA2dpService.FMSTART_SUCCESS);
                FMRadioLogUtils.d(TAG, "handling ACTION_FM_OVER_BT_CONTROLLER: " + fmOverBTState);
                switch (fmOverBTState) {
                case BluetoothA2dpService.FMSTART_SUCCESS:
                    if (!FMRadioNative.setFMViaBTController(true)) {
                        FMRadioLogUtils.e(TAG, "failed to set FM over BT via Controller!!");
                    } else {
                        FMRadioLogUtils.d(TAG, "setFMViaBTController(true) succeeded!!");
                        mUsingFMViaBTController = true;
                        // quit recording mode
                        if (FeatureOption.MTK_FM_RECORDING_SUPPORT) {
                            //                            if (mIsRecording) {
//                            try {
//                                mBinder.setRecordingMode(false);
//                            } catch (RemoteException re) {
//                                FMRadioLogUtils.e(TAG, "RemoteException in setRecordingMode(false):", re);
//                            }
                            Intent intent_recorder = new Intent(ACTION_RECORDING_MODE_CHANGED);
                            intent_recorder.putExtra(EXTRA_RECORDING_MODE, false);
                            sendBroadcast(intent_recorder);
                            //                            }
                        }
                        enableFMAudio(false);
                        // FM over bt but also have audio focus, if FMRadio doesn't have audio focus, other app can't power down FMRadio  
                        int audioFocus = mAudioManager.requestAudioFocus(mFMAudioFocusChangeListener, AudioManager.STREAM_FM, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
                        FMRadioLogUtils.d(TAG, "FM request audioFocus =" + audioFocus);
                    }
                    break;
                case BluetoothA2dpService.FMSTART_FAILED:
                    //if FMRadio connect bt failed, FMRadio will abandon audio focus and request audio focus again
                    int audioFocus = mAudioManager.abandonAudioFocus(mFMAudioFocusChangeListener);
                    FMRadioLogUtils.d(TAG, "FM abandon audioFocus =" + audioFocus);
                    enableFMAudio(true);
                    break;
                case BluetoothA2dpService.FMSTART_ALREADY:
                    FMRadioLogUtils.d(TAG, "ACTION_FM_OVER_BT_CONTROLLER: FM over BT already on-going!");
                    break;
                }
            } else if (action.equals(BluetoothA2dpService.ACTION_FM_OVER_BT_HOST)) {
                // change back to host if necessary
                // re-start FM player btw.
                FMRadioLogUtils.d(TAG, "ACTION_FM_OVER_BT_HOST");
                if (!FMRadioNative.setFMViaBTController(false)) {
                    FMRadioLogUtils.e(TAG, "failed to set FM over BT via Host!!");
                } else {
                    FMRadioLogUtils.d(TAG, "setFMViaBTController(false) succeeded!!");
                    mUsingFMViaBTController = false;
                    enableFMAudio(true);
                }

            } else {
                FMRadioLogUtils.w(TAG, "Error: undefined action.");
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.onReceive");
        }
    }

    private class FMPhoneStateListener extends PhoneStateListener {
        private int simId = Phone.GEMINI_SIM_1;

        public void setSIMID(int iSIMID) {
            simId = iSIMID;
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            FMRadioLogUtils.d(TAG, ">>> onCallStateChanged SIM" + simId);
            if (state == TelephonyManager.CALL_STATE_RINGING
            // Device call state: Ringing. A new call arrived and is ringing or waiting.
            // In the latter case, another call is already active.
                    || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                // Device call state: Off-hook. At least one call exists that is dialing,
                // active, or on hold, and no calls are ringing or waiting.
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    FMRadioLogUtils.d(TAG, "CALL_STATE_RINGING");
                } else {
                    FMRadioLogUtils.d(TAG, "CALL_STATE_OFFHOOK");
                }

                // Should record this state.
                if (Phone.GEMINI_SIM_1 == simId) {
                    mIsSIM1Idle = false;
                } else {
                    mIsSIM2Idle = false;
                }

                if (mIsResumeAfterCall) {
                    FMRadioLogUtils.d(TAG, "Already know current is in a call.");
                } else {
                    if (mIsSearching) {
                        try {
                            mIsStopScanCalled = true;
                            mBinder.stopScan();
                        } catch (Exception e) {
                            FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                        }
                    }

                    if (mIsPowerUp) {
                        mIsResumeAfterCall = true;
                        try {
                            mBinder.setRDS(false);
                            mBinder.powerDown();
                        } catch (Exception e) {
                            FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                        }
                    } else {
                        FMRadioLogUtils.d(TAG, "FM is not playing, so do nothing.");
                    }
                    if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
                        int fmState = mFMRecorder.getState();
                        if (fmState == FMRecorder.STATE_PLAYBACK) {
                            mFMRecorder.stopPlayback();
                        } else if (fmState == FMRecorder.STATE_RECORDING) {
                            mFMRecorder.stopRecording();
                        }
                    }

                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                // Should record this state.
                if (Phone.GEMINI_SIM_1 == simId) {
                    mIsSIM1Idle = true;
                } else {
                    mIsSIM2Idle = true;
                }

                // Device call state: No activity.
                FMRadioLogUtils.i(TAG, "CALL_STATE_IDLE");
                // Only if SIM1 and SIM2 are all idle, resume FM.
                if (mIsSIM1Idle && mIsSIM2Idle) {
                    if (mIsResumeAfterCall) {
                        mIsResumeAfterCall = false;
                        if (mIsPowerUp) {
                            FMRadioLogUtils.d(TAG, "FM is already playing, no need to resume.");
                        } else {
                            FMRadioLogUtils.d(TAG, "Need to resume FM.");
                            try {
                                Thread.sleep(1000);
                                if (mBinder.powerUp((float) mCurrentStation / CONVERT_RATE)) {
                                    mBinder.setRDS(true);
                                    if (!FeatureOption.MTK_MT519X_FM_SUPPORT
                                            && !mAudioManager.isWiredHeadsetOn()) {
                                        int ret = -1;
                                        try {
                                            ret = mBinder.switchAntenna(1);
                                        } catch (Exception ex) {
                                            FMRadioLogUtils.e(TAG, "Exception: switchAntenna");
                                        }
                                        FMRadioLogUtils.d(TAG, "switchAntenna for resume FM after call: " + ret);
                                    }
                                }
                            } catch (Exception e) {
                                FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                            }
                        }
                    } else {
                        FMRadioLogUtils.d(TAG, "Do not need to resume, so do nothing.");
                    }
                } else {
                    FMRadioLogUtils.d(TAG, "The other SIM is not idle. So do nothing.");
                }
            } else {
                FMRadioLogUtils.w(TAG, "Error: Invalid call status.");
            }
            FMRadioLogUtils.d(TAG, "<<< onCallStateChanged SIM" + simId);
        }
    }

    private IFMRadioService.Stub mBinder = new IFMRadioService.Stub() {
        public boolean openDevice() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.openDevice");
            boolean bRet = false;
            if (mIsDeviceOpen) {
                FMRadioLogUtils.w(TAG, "Error: device is already open.");
                bRet = true;
            } else {
                bRet = FMRadioNative.opendev();
                if (bRet) {
                    mIsDeviceOpen = true;
                } else {
                    FMRadioLogUtils.e(TAG, "Error: opendev failed.");
                }
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.openDevice: " + bRet);
            return bRet;
        }

        public boolean closeDevice() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.closeDevice");
            boolean bRet = false;
            if (mIsDeviceOpen) {
                // Stop the RDS thread if RDS is supported.
                if (1 == isRDSSupported()) {
                    FMRadioLogUtils.d(TAG, "RDS is supported. Stop the RDS thread.");
                    stopRDSThread();
                } else {
                    FMRadioLogUtils.d(TAG, "RDS is not supported.");
                }
                bRet = FMRadioNative.closedev();
                if (bRet) {
                    mIsDeviceOpen = false;
                } else {
                    FMRadioLogUtils.e(TAG, "Error: closedev failed.");
                }
            } else {
                FMRadioLogUtils.w(TAG, "Error: device is already closed.");
                bRet = true;
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.closeDevice: " + bRet);
            return bRet;
        }

        public boolean isDeviceOpen() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isDeviceOpen");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isDeviceOpen: " + mIsDeviceOpen);
            return mIsDeviceOpen;
        }

        public boolean powerUp(float frequency) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.powerUp: " + frequency);
            Intent it_music = new Intent(ACTION_TOMUSICSERVICE_POWERDOWN);
            sendBroadcast(it_music);
            boolean bRet = false;
            mCurrentStation = (int) (frequency * CONVERT_RATE);
            if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
                Intent intentToAtv = new Intent(ACTION_TOATVSERVICE_POWERDOWN);
                sendBroadcast(intentToAtv);
            }
            if (!FeatureOption.MTK_MT519X_FM_SUPPORT) {
                Intent intentToFMTx = new Intent(ACTION_TOFMTXSERVICE_POWERDOWN);
                sendBroadcast(intentToFMTx);
            }

            if (mIsSIM1Idle && mIsSIM2Idle) {
                if (mIsPowerUp) {
                    FMRadioLogUtils.w(TAG, "Error: device is already power up.");
                    bRet = true;
                } else {
                    if (!FeatureOption.MTK_MT519X_FM_SUPPORT) {
                        // Sleep to wait for FM Tx power down.
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            FMRadioLogUtils.e(TAG, "Exception: Thread.sleep.");
                        }
                    }
                    bRet = FMRadioNative.powerup(frequency);
                    if (FeatureOption.MTK_MT519X_FM_SUPPORT && FMRadioNative.isFMPoweredUp() == 0) {
                        // FM is actually NOT powered up
                        // due to mATV power down FM before FM.powerup returns
                        FMRadioLogUtils.w(TAG, "powerup: NOT powered up after calling powerup()!!");
                        return false;
                    }
                    if (bRet) {
                        // Add notification to the title bar.
                        showNotification();

                        if (!FeatureOption.MTK_BT_FM_OVER_BT_VIA_CONTROLLER) {
                            enableFMAudio(true);
                        }

                        mIsPowerUp = true;
                        setPS("");
                        setLRText("");

                        // Start the RDS thread if RDS is supported.
                        if (1 == isRDSSupported()) {
                            FMRadioLogUtils.d(TAG, "RDS is supported. Start the RDS thread.");
                            startRDSThread();
                        } else {
                            FMRadioLogUtils.d(TAG, "RDS is not supported.");
                        }

                        // Broadcast message to applications.
                        Intent intent = new Intent(ACTION_STATE_CHANGED);
                        intent.putExtra(EXTRA_FMRADIO_ISPOWERUP, mIsPowerUp);
                        sendBroadcast(intent);

                        mWakeLock.acquire();
                        FMRadioLogUtils.d(TAG, "acquire wake lock");
                        
                        if (FeatureOption.MTK_BT_FM_OVER_BT_VIA_CONTROLLER) {
                            // Notify A2dp about power up event
                            IBinder b = ServiceManager
                                    .getService(BluetoothA2dpService.BLUETOOTH_A2DP_SERVICE);
                            FMRadioLogUtils.d(TAG, "powerup: A2dp service IBinder=" + b);
                            if (b != null) {
                                mA2dpService = IBluetoothA2dp.Stub.asInterface(b);
                                try {
                                    mA2dpService.setAudioPathToAudioTrack(mICallBack);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                FMRadioLogUtils.d(TAG, "powerup: mA2dpService=" + mA2dpService);
                                try {
                                    int a2dpState = mA2dpService != null ? mA2dpService.getState()
                                            : -1;
                                    FMRadioLogUtils.d(TAG, "powerup: mA2dpService.getState()=" + a2dpState);
                                    if (a2dpState == BluetoothA2dp.STATE_CONNECTED
                                            || a2dpState == BluetoothA2dp.STATE_PLAYING || a2dpState == BluetoothA2dp.STATE_NOT_PLAYING) {
                                        // A2dp connected, so send out MSG_FM_POWER_UP
                                        Intent i = new Intent(MSG_FM_POWER_UP);
                                        FMRadioLogUtils.d(TAG, MSG_FM_POWER_UP + " sent to A2dp service!!");
                                        sendBroadcast(i);
                                    } else {
                                        enableFMAudio(true);
                                    }
                                } catch (RemoteException re) {
                                    FMRadioLogUtils.e(TAG, "binder error!!");
                                }
                            } else {
                                FMRadioLogUtils.w(TAG, "Failed to bind to BluetoothA2dpService!!!");
                                enableFMAudio(true);
                            }
                        }
                    } else {
                        FMRadioLogUtils.e(TAG, "Error: powerup failed.");
                        // powerup failed, update UI as stop state
                        Intent intent = new Intent(ACTION_STATE_CHANGED);
                        intent.putExtra(EXTRA_FMRADIO_ISPOWERUP, mIsPowerUp);
                        sendBroadcast(intent);
                    }
                }
            } else {
                FMRadioLogUtils.d(TAG, "Phone call is ongoing.");
            }

            FMRadioLogUtils.d(TAG, "<<< FMRadioService.powerUp: " + bRet);
            return bRet;
        }

        public boolean powerDown() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.powerDown");
            boolean bRet = false;
            if (mIsPowerUp) {
                enableFMAudio(false);
                bRet = FMRadioNative.powerdown(0); // 0, FM_RX; 1, FM_TX
                if (bRet) {
                    mUsingFMViaBTController = false;
                    if (1 == isRDSSupported()) {
                        FMRadioLogUtils.d(TAG, "RDS is supported. Stop the RDS thread.");
                        stopRDSThread();
                    } else {
                        FMRadioLogUtils.d(TAG, "RDS is not supported.");
                    }
                    // Remove the notification in the title bar.
                    removeNotification();

                    mIsPowerUp = false;
                    setPS("");
                    setLRText("");

                    // Broadcast message to applications.
                    Intent intent = new Intent(ACTION_STATE_CHANGED);
                    intent.putExtra(EXTRA_FMRADIO_ISPOWERUP, mIsPowerUp);
                    sendBroadcast(intent);
                    
                    if (null != mWakeLock) {
                        mWakeLock.release();
                        FMRadioLogUtils.d(TAG, "release wake lock");
                    }
                    //mark for ICS
                    if (FeatureOption.MTK_BT_FM_OVER_BT_VIA_CONTROLLER) {
                        // Notify A2dp about power down event
                        if (mA2dpService == null) {
                            IBinder b = ServiceManager
                                    .getService(BluetoothA2dpService.BLUETOOTH_A2DP_SERVICE);
                            FMRadioLogUtils.d(TAG, "powerdown: IBinder=" + b);
                            if (b != null) {
                                mA2dpService = IBluetoothA2dp.Stub.asInterface(b);
                                FMRadioLogUtils.d(TAG, "powerdown: mA2dpService=" + mA2dpService);
                            }
                        }
                        if (mA2dpService != null) {
                            FMRadioLogUtils.d(TAG, "powerdown: mA2dpService != null");
                            try {
                                int a2dpState = mA2dpService.getState();
                                FMRadioLogUtils.d(TAG, "powerdown: mA2dpService.getState()=" + a2dpState);
                                if (a2dpState == BluetoothA2dp.STATE_CONNECTED
                                        || a2dpState == BluetoothA2dp.STATE_PLAYING) {
                                    // A2dp connected, so send out MSG_FM_POWER_DOWN
                                    Intent i = new Intent(MSG_FM_POWER_DOWN);
                                    sendBroadcast(i);
                                    FMRadioLogUtils.d(TAG, MSG_FM_POWER_DOWN + " sent to A2dp service!!");
                                }
                            } catch (RemoteException re) {
                                FMRadioLogUtils.e(TAG, "binder error!!");
                            }
                        }
                    }
                } else {
                    FMRadioLogUtils.e(TAG, "Error: powerdown failed.");
                    // powerdown failed, update UI as play state
                    Intent intent = new Intent(ACTION_STATE_CHANGED);
                    intent.putExtra(EXTRA_FMRADIO_ISPOWERUP, mIsPowerUp);
                    sendBroadcast(intent);
                }
            } else {
                FMRadioLogUtils.w(TAG, "Error: device is already power down.");
                bRet = true;
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.powerDown: " + bRet);
            return bRet;
        }

        public boolean isPowerUp() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isPowerUp");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isPowerUp: " + mIsPowerUp);
            return mIsPowerUp;
        }

        public boolean tune(float frequency) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.tune: " + frequency);
            boolean bRet = FMRadioNative.tune(frequency);
            if (bRet) {
                mCurrentStation = (int) (frequency * CONVERT_RATE);
                updateNotification();
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.tune: " + bRet);
            return bRet;
        }

        public float seek(float frequency, boolean isUp) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.seek: " + frequency + " " + isUp);
            long startSeekTime = System.currentTimeMillis();
            FMRadioLogUtils.i(TAG, "[Performance test][FMRadio] Test FMRadio Native seek time start ["+ startSeekTime +"]");
            float fRet = FMRadioNative.seek(frequency, isUp);
            long endSeekTime = System.currentTimeMillis();
            FMRadioLogUtils.i(TAG, "[Performance test][FMRadio] Test FMRadio Native seek time end ["+ endSeekTime +"]");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.seek: " + fRet);
            return fRet;
        }

        public int[] startScan() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.startScan");
            int[] iChannels = null;

            if (mIsPowerUp) {
                setRDS(false);
                enableFMAudio(false);
            } else {
                FMRadioNative.powerup((float) mCurrentStation / CONVERT_RATE);
                // switch to short antenna if possible,
                // so that the scan might be success even without a long antenna
                if (!FeatureOption.MTK_MT519X_FM_SUPPORT && !mAudioManager.isWiredHeadsetOn()) {
                    int ret = -1;
                    try {
                        ret = switchAntenna(1);
                    } catch (Exception ex) {
                        FMRadioLogUtils.e(TAG, "Exception: switchAntenna(1)");
                    }
                    FMRadioLogUtils.d(TAG, "startScan: switchAntenna(1): " + ret);
                }
                // Acquire a wakelock so that search will continue
                // for 5193 only...
                if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
                    FMRadioLogUtils.d(TAG, "startScan: acquire a wakelock for MT5193 to search complete!");
                    mWakeLock.acquire();
                }
            }
            short[] shortChannels = null;
            if (!mIsStopScanCalled) {
                mIsSearching = true;
                FMRadioLogUtils.d(TAG, "startScan native method:start");
                shortChannels = FMRadioNative.autoscan();
                FMRadioLogUtils.d(TAG, "startScan native method:end " + shortChannels);
                mIsSearching = false;
                // call switch antenna should after scan finish.
                if (mIsHeadSetPlugInOut) {
                    mIsHeadSetPlugInOut = false;
                    int ret = -1;
                    try {
                        ret = mBinder.switchAntenna(mValueHeadSetPlug);
                    } catch (Exception ex) {
                        FMRadioLogUtils.e(TAG, "Exception: switchAntenna()");
                    }
                    if (ret != 0) {
                        FMRadioLogUtils.e(TAG, "startScan: Cannot switch to short antenna: " + ret);
                    }
                }
            }

            if (mIsPowerUp) {
                if (mIsSIM1Idle && mIsSIM2Idle) {
                    if (!mIsStopScanCalled) {
                        setRDS(true);
                        // We do not enable audio after activity has tuneToStation
                        FMRadioLogUtils.d(TAG, "startScan: scan complete, but don't enable audio yet!");
                    }
                } else {
                    FMRadioLogUtils.d(TAG, "A phone call is ongoing.");
                }
            } else {
                // No need to switch off short antenna since we'll powerdown from here
                FMRadioNative.powerdown(0); // 0, FM_RX; 1, FM_TX
            }
            FMRadioLogUtils.d(TAG, "startscan service update mIsStopScan:" + mIsStopScanCalled);
            if (mIsStopScanCalled) {
                // Received a message to power down FM, or interrupted by a phone call.
                // Do not return any stations.
                shortChannels = null;
                mIsStopScanCalled = false;
            }
            if (null != shortChannels) {
                iChannels = new int[shortChannels.length];
                for (int i = 0; i < shortChannels.length; i++) {
                    iChannels[i] = shortChannels[i];
                }
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.startScan: " + Arrays.toString(iChannels));
            return iChannels;
        }

        public boolean stopScan() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.stopScan");
            boolean bRet = false;
            mIsStopScanCalled = true;
            if (mIsSearching) {
                FMRadioLogUtils.d(TAG, "native stop scan:start");
                bRet = FMRadioNative.stopscan();
                FMRadioLogUtils.d(TAG, "native stop scan:end --" + bRet);
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.stopScan: " + bRet);
            return bRet;
        }

        public int setRDS(boolean on) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.setRDS: " + on);
            int iRet = FMRadioNative.rdsset(on);
            setPS("");
            setLRText("");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.setRDS: " + iRet);
            return iRet;
        }

        public int readRDS() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.readRDS");
            int iEvents = FMRadioNative.readrds();
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.readRDS: " + iEvents);
            return iEvents;
        }

        public String getPS() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.getPS");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.getPS: " + mPSString);
            return mPSString;
        }

        public String getLRText() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.getLRText");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.getLRText: " + mLRTextString);
            return mLRTextString;
        }

        public int activeAF() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.activeAF");
            int frequency = FMRadioNative.activeAF();
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.activeAF: " + frequency);
            return frequency;
        }

        public int activeTA() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.activeTA");
            int frequency = FMRadioNative.activeTA();
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.activeTA: " + frequency);
            return frequency;
        }

        public int deactiveTA() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.deactiveTA");
            int frequency = FMRadioNative.deactiveTA();
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.deactiveTA: " + frequency);
            return frequency;
        }

        public int setMute(boolean mute) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.setMute: " + mute);
            int iRet = FMRadioNative.setmute(mute);
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.setMute: " + iRet);
            return iRet;
        }

        public int isRDSSupported() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isRDSSupported");
            int iRet = FMRadioNative.isRDSsupport();
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isRDSSupported: " + iRet);
            return iRet;
        }

        public void useEarphone(boolean use) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.useEarphone: " + use);
            if (use) {
                mAudioManager.setParameters(AUDIO_PATH_EARPHONE);
                mIsEarphoneUsed = true;
            } else {
                mAudioManager.setParameters(AUDIO_PATH_LOUDSPEAKER);
                mIsEarphoneUsed = false;
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.useEarphone");
        }

        public boolean isEarphoneUsed() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isEarphoneUsed");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isEarphoneUsed: " + mIsEarphoneUsed);
            return mIsEarphoneUsed;
        }

        public void initService(int iCurrentStation) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.initService: " + iCurrentStation);
            mIsServiceInit = true;
            mCurrentStation = iCurrentStation;
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.initService");
        }

        public boolean isServiceInit() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isServiceInit");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isServiceInit: " + mIsServiceInit);
            return mIsServiceInit;
        }

        public void enablePSRT(boolean enable) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.enablePSRT: " + enable);
            mIsPSRTEnabled = enable;
            if (!mIsPSRTEnabled) {
                // Clear PS and RT strings.
                setPS("");
                setLRText("");
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.enablePSRT");
        }

        public void enableAF(boolean enable) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.enableAF: " + enable);
            mIsAFEnabled = enable;
            // ... Need to disable RDS if all settings are off?
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.enableAF");
        }

        public void enableTA(boolean enable) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.enableTA: " + enable);
            mIsTAEnabled = enable;
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.enableTA");
        }

        public boolean isPSRTEnabled() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isPSRTEnabled");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isPSRTEnabled: " + mIsPSRTEnabled);
            return mIsPSRTEnabled;
        }

        public boolean isAFEnabled() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isAFEnabled");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isAFEnabled: " + mIsAFEnabled);
            return mIsAFEnabled;
        }

        public boolean isTAEnabled() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.isTAEnabled");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.isTAEnabled: " + mIsTAEnabled);
            return mIsTAEnabled;
        }

        public int getFrequency() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.getFrequency");
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.getFrequency: " + mCurrentStation);
            return mCurrentStation;
        }
        
        public void setFrequency(int station) {
            mCurrentStation = station;
        }

        public void resumeFMAudio() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.resumeFMAudio");
            if (!mUsingFMViaBTController) {
                enableFMAudio(true);
            } else {
                FMRadioLogUtils.d(TAG,
                        "resumeFMAudio: FM over BT via controller ongoing, so do NOT enable FMPlayer!!");
            }
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.resumeFMAudio");
        }

        public int switchAntenna(int antenna) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.switchAntenna");
            int ret = FMRadioNative.switchAntenna(antenna);
            FMRadioLogUtils.d(TAG, "<<< FMRadioService.switchAntenna: " + ret);
            return ret;
        }

        // LXO add.
        public int readCapArray() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.readCapArray");
            return FMRadioNative.readCapArray();
        }

        public int readRssi() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.readRssi");
            return FMRadioNative.readRssi();
        }

        public boolean getStereoMono() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.getStereoMono");
            return FMRadioNative.stereoMono();
        }

        public boolean setStereoMono(boolean isMono) {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.setStereoMono: isMono=" + isMono);
            return FMRadioNative.setStereoMono(isMono);
        }

        public int readRdsBler() {
            FMRadioLogUtils.d(TAG, ">>> FMRadioService.readRdsBler");
            return FMRadioNative.readRdsBler();
        }

        public void startRecording() {
            FMRadioLogUtils.d(TAG, ">>> startRecording");
            mDefaultSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            FMRadioLogUtils.d(TAG, "default sd card file path: " + mDefaultSDCardPath);
            if (mFMRecorder == null) {
                mFMRecorder = new FMRecorder();
                mFMRecorder.registerRecorderStateListener(FMRadioService.this);
            }
            mFMRecorder.startRecording();
            FMRadioLogUtils.d(TAG, "<<< startRecording");
        }

        public void stopRecording() {
            FMRadioLogUtils.d(TAG, ">>> stopRecording");
            if (mFMRecorder == null) {
                FMRadioLogUtils.e(TAG, "stopRecording called without a valid recorder!!");
                return;
            }
            mFMRecorder.stopRecording();
            FMRadioLogUtils.d(TAG, "<<< stopRecording");
        }

        public void startPlayback() {
            FMRadioLogUtils.d(TAG, ">>> startPlayback");
            if (mFMRecorder != null) {
                if (mIsPowerUp) {
                    enableFMAudio(false);
                }
                int audioFocus = mAudioManager.requestAudioFocus(mRecordingAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocus) {
                    FMRadioLogUtils.i(TAG, "Record player get audio focus");
                    mFMRecorder.startPlayback();
                } else {
                    FMRadioLogUtils.w(TAG, "Record player request audio focus failed");
                }
                
            } else {
                FMRadioLogUtils.e(TAG, "FMRecorder is null !!");
            }
            FMRadioLogUtils.d(TAG, "<<< startPlayback");
        }

        public void stopPlayback() {
            FMRadioLogUtils.d(TAG, ">>> stopPlayback");
            if (mFMRecorder != null) {
                mAudioManager.abandonAudioFocus(mRecordingAudioFocusChangeListener);
                mFMRecorder.stopPlayback();
                if (mIsPowerUp && mIsStopPressed) {
                    enableFMAudio(true);
                }
            } else {
                FMRadioLogUtils.e(TAG, "FMRecorder is null !!");
            }
            mIsStopPressed = false;
            FMRadioLogUtils.d(TAG, "<<< stopPlayback");
        }

        public void saveRecording(String newName) {
            FMRadioLogUtils.d(TAG, ">>> saveRecording");
            if (mFMRecorder != null) {
                if (newName != null) {
                    mFMRecorder.saveRecording(FMRadioService.this, newName);
                } else {
                    mFMRecorder.discardRecording();
                }
            } else {
                FMRadioLogUtils.e(TAG, "FMRecorder is null !!");
            }
            FMRadioLogUtils.d(TAG, "<<< saveRecording");
        }

        public long getRecordTime() {
            if (mFMRecorder != null) {
                return mFMRecorder.recordTime();
            } else {
                FMRadioLogUtils.e(TAG, "FMRecorder is null !!");
                return 0;
            }
        }

        public void setRecordingMode(boolean isRecording) {
            FMRadioLogUtils.d(TAG, ">>> setRecordingMode: isRecording=" + isRecording);
            mIsRecording = isRecording;
            if (!isRecording && mFMRecorder != null) {
                if (mFMRecorder.getState() != FMRecorder.STATE_IDLE) {
                    mFMRecorder.stopRecording();
                    mAudioManager.abandonAudioFocus(mRecordingAudioFocusChangeListener);
                    mFMRecorder.stopPlayback();
                }
            } else if (isRecording && mFMRecorder != null) {
                // reset recorder to unused status
                mFMRecorder.resetRecorder();
            }
            FMRadioLogUtils.d(TAG, "<<< setRecordingMode");
        }

        public boolean getRecordingMode() {
            return mIsRecording;
        }

        public int getRecorderState() {
            if (mFMRecorder != null) {
                return mFMRecorder.getState();
            } else {
                return -1;
            }
        }

        public int getPlaybackPosition() {
            if (mFMRecorder != null) {
                return mFMRecorder.getPosition();
            } else {
                return 0;
            }
        }

        public String getRecordingName() {
            if (mFMRecorder != null) {
                return mFMRecorder.getRecordingName();
            } else {
                return null;
            }
        }

        public boolean isFMOverBTActive() {
            return mUsingFMViaBTController;
        }
        
        public boolean getResumeAfterCall(){
            return mIsResumeAfterCall;
        }
        
        public boolean isSIMCardIdle() {
            return (mIsSIM1Idle && mIsSIM2Idle);
        }
        public int[] getHardwareVersion(){
            return FMRadioNative.getHardwareVersion();
        }
        
        public void setStopPressed(boolean isStopPressed) {
            mIsStopPressed = isStopPressed;
        }

    };

    public void onCreate() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.onCreate");
        super.onCreate();
        sFMService = new WeakReference<FMRadioService>(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(false);
        mFMPlayer = new MediaPlayer();
        mFMPlayer.setWakeMode(FMRadioService.this, PowerManager.PARTIAL_WAKE_LOCK);
        mFMPlayer.setOnErrorListener(playerErrorListener);
        try {
            mFMPlayer.setDataSource("MEDIATEK://MEDIAPLAYER_PLAYERTYPE_FM");
        } catch (IOException ex) {
            // notify the user why the file couldn't be opened
            FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
            return;
        } catch (IllegalArgumentException ex) {
            // notify the user why the file couldn't be opened
            FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
            return;
        } catch (IllegalStateException ex) {
            FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
            return;
        }
        mFMPlayer.setAudioStreamType(AudioManager.STREAM_FM);

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOFMSERVICE_POWERDOWN);
        filter.addAction(MESSAGE_FROMSOUNDER_TOFM_POWERDOWN);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
            filter.addAction(ACTION_FROMATVSERVICE_POWERUP);
        }
        
        if (FeatureOption.MTK_BT_FM_OVER_BT_VIA_CONTROLLER) {
            filter.addAction(BluetoothA2dpService.ACTION_FM_OVER_BT_CONTROLLER);
            filter.addAction(BluetoothA2dpService.ACTION_FM_OVER_BT_HOST);
            filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        }
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mBroadcastReceiver = new FMServiceBroadcastReceiver();
        FMRadioLogUtils.i(TAG, "Register broadcast receiver.");
        registerReceiver(mBroadcastReceiver, filter);

        // Listen to the phone call status.
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            FMRadioLogUtils.i(TAG, "Dual SIM phone");
            mPhoneStateListener1 = new FMPhoneStateListener();
            mPhoneStateListener1.setSIMID(Phone.GEMINI_SIM_1);
            tm.listenGemini(mPhoneStateListener1, PhoneStateListener.LISTEN_CALL_STATE,
                    Phone.GEMINI_SIM_1);
            mPhoneStateListener2 = new FMPhoneStateListener();
            mPhoneStateListener2.setSIMID(Phone.GEMINI_SIM_2);
            tm.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_CALL_STATE,
                    Phone.GEMINI_SIM_2);
        } else {
            FMRadioLogUtils.i(TAG, "Single SIM phone");
            mPhoneStateListener1 = new FMPhoneStateListener();
            tm.listen(mPhoneStateListener1, PhoneStateListener.LISTEN_CALL_STATE);
        }

        // register FM recorder related listener/broadcast receiver
        if (FeatureOption.MTK_FM_RECORDING_SUPPORT) {
            registerSDListener();
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.onCreate");
    }

    public void onDestroy() {
        sFMService = null;
        mIsRDSThreadNeedDie = true;
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.onDestroy");
        try {
            if (1 == mBinder.isRDSSupported()) {
                FMRadioLogUtils.d(TAG, "RDS is supported. Stop the RDS thread.");
                stopRDSThread();
            } else {
                FMRadioLogUtils.d(TAG, "RDS is not supported.");
            }
        } catch (Exception ex) {
            FMRadioLogUtils.e(TAG, "Exception in isRDSSupported: " + ex);
        }
        // Unregister the broadcast receiver.
        if (null != mBroadcastReceiver) {
            FMRadioLogUtils.i(TAG, "Unregister broadcast receiver.");
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }

        // Stop FM recorder if it is working
        if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
            int fmState = mFMRecorder.getState();
            if (fmState == FMRecorder.STATE_PLAYBACK) {
                mFMRecorder.stopPlayback();
                FMRadioLogUtils.d(TAG, "Stop playback FMRecorder.");
            } else if (fmState == FMRecorder.STATE_RECORDING) {
                mFMRecorder.discardRecording();
                FMRadioLogUtils.d(TAG, "Discard Recording.");
            }
        }

        // When exit, we set the audio path back to earphone.
        try {
            if (!mIsEarphoneUsed) {
                mBinder.useEarphone(true);
            }
            if (mIsPowerUp) {
                mBinder.powerDown();
            }
            if (mIsDeviceOpen) {
                mBinder.closeDevice();
            }
        } catch (Exception e) {
            FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
        }

        // Release FM player upon exit
        if (mFMPlayer != null) {
            mFMPlayer.release();
        }

        // Stop listening to the phone call status.
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            FMRadioLogUtils.i(TAG, "Dual SIM phone");
            tm.listenGemini(mPhoneStateListener1, PhoneStateListener.LISTEN_NONE,
                    Phone.GEMINI_SIM_1);
            tm.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_NONE,
                    Phone.GEMINI_SIM_2);
        } else {
            FMRadioLogUtils.i(TAG, "Single SIM phone");
            tm.listen(mPhoneStateListener1, PhoneStateListener.LISTEN_NONE);
        }
        // release FMRecorder & unregister SD event receiver
        if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
            mFMRecorder = null;
        }
        if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mSDListener != null) {
            unregisterReceiver(mSDListener);
        }
        Intent intent = new Intent(ACTION_EXIT_FMSERVICE);
        sendBroadcast(intent);
        super.onDestroy();
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.onDestroy");
    }

    public IBinder onBind(Intent intent) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.onBind");
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.onBind: " + mBinder);
        return mBinder;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        // Change the notification string.
        if (mIsPowerUp) {
            showNotification();
        }

        FMRadioLogUtils.d(TAG, "<<< FMRadioService.onConfigurationChanged");
    }

    public void onLowMemory() {
        FMRadioLogUtils.w(TAG, ">>> FMRadioService.onLowMemory");
        super.onLowMemory();
        FMRadioLogUtils.w(TAG, "<<< FMRadioService.onLowMemory");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.onStartCommand intent: " + intent + " startId: " + startId);
        int iRet = super.onStartCommand(intent, flags, startId);
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.onStartCommand: " + iRet);
        return iRet;
    }

    private void startRDSThread() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.startRDSThread");
        mIsExit = false;
        if (mRDSThread == null ||
                mRDSThread.getState() == Thread.State.TERMINATED) {
            mRDSThread = new Thread() {
                public void run() {
                    while (!mIsRDSThreadNeedDie) {
                        while (!mIsExit) {
                            FMRadioLogUtils.d(TAG, ">>> RDS Thread run()");
                            int iRDSEvents = FMRadioNative.readrds();
                            if (iRDSEvents != 0) {
                                FMRadioLogUtils.d(TAG, "<<< FMRadioNative.readrds events: " + iRDSEvents);
                            }
                            if (mIsPSRTEnabled
                                    && RDS_EVENT_PROGRAMNAME == (RDS_EVENT_PROGRAMNAME & iRDSEvents)) {
                                FMRadioLogUtils.d(TAG, "RDS_EVENT_PROGRAMNAME");
                                byte[] bytePS = FMRadioNative.getPS();
                                if (null != bytePS) {
                                    setPS(new String(bytePS));
                                } else {
                                    FMRadioLogUtils.w(TAG, "Error: No program name.");
                                }
                            }
                            if (mIsPSRTEnabled
                                    && RDS_EVENT_LAST_RADIOTEXT == (RDS_EVENT_LAST_RADIOTEXT & iRDSEvents)) {
                                FMRadioLogUtils.d(TAG, "RDS_EVENT_LAST_RADIOTEXT");
                                byte[] byteLRText = FMRadioNative.getLRText();
                                if (null != byteLRText) {
                                    setLRText(new String(byteLRText));
                                } else {
                                    FMRadioLogUtils.w(TAG, "Error: No LRText.");
                                }
                            }
                            if (mIsAFEnabled && RDS_EVENT_AF == (RDS_EVENT_AF & iRDSEvents)) {
                                FMRadioLogUtils.d(TAG, "RDS_EVENT_AF");
                                int iFreq = FMRadioNative.activeAF();
                                FMRadioLogUtils.d(TAG, "Frequency: " + iFreq);
                                if (iFreq >= FMRadioStation.LOWEST_STATION
                                        && iFreq <= FMRadioStation.HIGHEST_STATION) {
                                    // Valid alternative frequency. Should update the current station if
                                    // the new frequency is not equal to current frequency.
                                    if (mCurrentStation != iFreq) {
                                        setPS("");
                                        setLRText("");

                                        mCurrentStation = iFreq;
                                        updateNotification();

                                        // Broadcast message to applications.
                                        Intent intent = new Intent(ACTION_RDS_AF_ACTIVED);
                                        intent.putExtra(EXTRA_RDS_AF_ACTIVED, mCurrentStation);
                                        sendBroadcast(intent);
                                    } else {
                                        FMRadioLogUtils.w(TAG, "Error: the new frequency is the same as current.");
                                    }
                                } else {
                                    FMRadioLogUtils.e(TAG, "Error: invalid alternative frequency");
                                }
                            }
                            if (mIsTAEnabled && RDS_EVENT_TAON == (RDS_EVENT_TAON & iRDSEvents)) {
                                FMRadioLogUtils.d(TAG, "RDS_EVENT_TAON");
                                int iFreq = FMRadioNative.activeTA();
                                FMRadioLogUtils.d(TAG, "Frequency: " + iFreq);
                                if (iFreq >= FMRadioStation.LOWEST_STATION
                                        && iFreq <= FMRadioStation.HIGHEST_STATION) {
                                    // Valid alternative frequency. Should update the current station if
                                    // the new frequency is not equal to current frequency.
                                    if (mCurrentStation != iFreq) {
                                        setPS("");
                                        setLRText("");

                                        mCurrentStation = iFreq;
                                        updateNotification();

                                        // Broadcast message to applications.
                                        Intent intent = new Intent(ACTION_RDS_TA_ACTIVED);
                                        intent.putExtra(EXTRA_RDS_TA_ACTIVED, mCurrentStation);
                                        sendBroadcast(intent);
                                    } else {
                                        FMRadioLogUtils.w(TAG, "Error: the new frequency is the same as current.");
                                    }
                                } else {
                                    FMRadioLogUtils.e(TAG, "Error: invalid activeTA frequency");
                                }
                            }
                            if (mIsTAEnabled && RDS_EVENT_TAON_OFF == (RDS_EVENT_TAON_OFF & iRDSEvents)) {
                                FMRadioLogUtils.d(TAG, "RDS_EVENT_TAON_OFF");
                                int iFreq = FMRadioNative.deactiveTA();
                                FMRadioLogUtils.v(TAG, "Frequency: " + iFreq);
                                if (iFreq >= FMRadioStation.LOWEST_STATION
                                        && iFreq <= FMRadioStation.HIGHEST_STATION) {
                                    // Valid alternative frequency. Should update the current station if
                                    // the new frequency is not equal to current frequency.
                                    if (mCurrentStation != iFreq) {
                                        setPS("");
                                        setLRText("");

                                        mCurrentStation = iFreq;
                                        updateNotification();

                                        // Broadcast message to applications.
                                        Intent intent = new Intent(ACTION_RDS_TA_DEACTIVED);
                                        intent.putExtra(EXTRA_RDS_TA_DEACTIVED, mCurrentStation);
                                        sendBroadcast(intent);
                                    } else {
                                        FMRadioLogUtils.w(TAG, "Error: the new frequency is the same as current.");
                                    }
                                } else {
                                    FMRadioLogUtils.e(TAG, "Error: invalid deactiveTA frequency");
                                }
                            }

                            // Do not handle other events.
                            // Sleep 100ms to reduce inquiry frequency
                            try {
                                Thread.sleep(100);
                            } catch (Exception ex) {
                            }
                            FMRadioLogUtils.d(TAG, "<<< RDS Thread run()");
                        }
                        // keep thread run background, and check 100ms interval. 
                        try {
                            Thread.sleep(100);
                        } catch (Exception ex) {
                        }
//                        FMRadioLogUtils.d(TAG, "-0--RDS Thread");
                    }
                }
            };

            FMRadioLogUtils.d(TAG, "Start RDS Thread.");
            mRDSThread.start();
        } else {
            FMRadioLogUtils.w(TAG, "Error: RDS thread is already started.");
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.startRDSThread");
    }

    private void stopRDSThread() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.stopRDSThread");
        if (null == mRDSThread) {
            FMRadioLogUtils.w(TAG, "Error: RDS thread is not started.");
        } else {
            // Must call closedev after stopRDSThread.
            mIsExit = true;
//            mRDSThread = null;
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.stopRDSThread");
    }

    private void setPS(String sPS) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.setPS: " + sPS + " ,current: " + mPSString);
        if (0 == mPSString.compareTo(sPS)) {
            FMRadioLogUtils.d(TAG, "New PS is the same as current.");
        } else {
            mPSString = sPS;

            // Broadcast message to applications.
            Intent intent = new Intent(ACTION_RDS_PS_CHANGED);
            intent.putExtra(EXTRA_RDS_PS, mPSString);
            sendBroadcast(intent);
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.setPS");
    }

    private void setLRText(String sLRText) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.setLRText: " + sLRText + " ,current: " + mLRTextString);
        if (0 == mLRTextString.compareTo(sLRText)) {
            FMRadioLogUtils.d(TAG, "New RT is the same as current.");
        } else {
            mLRTextString = sLRText;

            // Broadcast message to applications.
            Intent intent = new Intent(ACTION_RDS_RT_CHANGED);
            intent.putExtra(EXTRA_RDS_RT, mLRTextString);
            sendBroadcast(intent);
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.setLRText");
    }

    private void enableFMAudio(boolean bEnable) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.enableFMAudio: " + bEnable);
        if (bEnable) {
            if (!isGetAudioFocus()) {
                FMRadioLogUtils.w(TAG, "powerup: Can not get audio focus.");
                return;
            }

            FMRadioLogUtils.i(TAG, "FM get audio Focus");
            if (mFMPlayer.isPlaying()) {
                FMRadioLogUtils.d(TAG, "warning: FM audio is already enabled.");
            } else {
                try {
                    mFMPlayer.prepare();
                } catch (IOException e) {
                    FMRadioLogUtils.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
                } catch (IllegalStateException e) {
                    FMRadioLogUtils.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
                }

                mFMPlayer.start();
                FMRadioLogUtils.d(TAG, "Start FM audio.");
            }
        } else {
            mAudioManager.abandonAudioFocus(mFMAudioFocusChangeListener);
            if (mFMPlayer.isPlaying()) {
                mFMPlayer.stop();
            } else {
                FMRadioLogUtils.d(TAG, "warning: FM audio is already disabled.");
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.enableFMAudio");
    }

    private void showNotification() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.showNotification");
        Intent notificationIntent = new Intent();
        notificationIntent.setClassName(getPackageName(), FMRadioActivity.class.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);
        Notification notification = new Notification(R.drawable.fm_title_icon, null,
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        String text = formatStation(mCurrentStation) + " MHz";
        notification.setLatestEventInfo(getApplicationContext(),
                getResources().getString(R.string.app_name), text, pendingIntent);
        FMRadioLogUtils.d(TAG, "Add notification to the title bar.");
        startForeground(NOTIFICATION_ID, notification);
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.showNotification");
    }

    private void removeNotification() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.removeNotification");
        stopForeground(true);
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.removeNotification");
    }

    private void updateNotification() {
        FMRadioLogUtils.d(TAG, ">>> FMRadioService.updateNotification");
        if (mIsPowerUp) {
            showNotification();
        } else {
            FMRadioLogUtils.w(TAG, "FM is not power up.");
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioService.updateNotification");
    }

    public static void onStateChanged(int state) {
        FMRadioLogUtils.d(TAG, ">>> onStateChanged");
        FMRadioService sService = (FMRadioService) sFMService.get();
        if (sService == null) {
            FMRadioLogUtils.d(TAG, "onStateChanged: service ref is null!!");
            return;
        }
        if (state == 0) {
            // FM has powered down from lower layers
            FMRadioLogUtils.d(TAG, "onStateChanged: FM has been powered down");
            if (sService.mIsPowerUp) {
                // DO WE STILL NEED TO SETMUTE/RDSSET?????
                sService.enableFMAudio(false);
                sService.removeNotification();
                sService.setPS("");
                sService.setLRText("");
                sService.mIsPowerUp = false;

                // Broadcast to FM activity
                Intent intent = new Intent(ACTION_STATE_CHANGED);
                intent.putExtra(EXTRA_FMRADIO_ISPOWERUP, sService.mIsPowerUp);
                sService.sendBroadcast(intent);
            }
        }
        FMRadioLogUtils.d(TAG, "<<< onStateChanged: " + state);
    }

    private void registerSDListener() {
        FMRadioLogUtils.v(TAG, "registerSDListener >>> ");
        if (mSDListener == null) {
            mSDListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mFMRecorder == null) {
                        FMRadioLogUtils.w(TAG, "SD receiver: FMRecorder is not present!!");
                        return;
                    }
                    String action = intent.getAction();
                    if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                        FMRadioLogUtils.v(TAG, "MEDIA_MOUNTED");
                        mFMRecorder.onSDInserted();
                    } else if (Intent.ACTION_MEDIA_EJECT.equals(action) || 
                            Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                        // If not unmount recording sd card, do nothing;
                        if (!isRecordingCardUnmount(intent)) {
                            return;
                        } else {
                            int oldState = mFMRecorder.getState();
                            FMRadioLogUtils.v(TAG, "MEDIA_EJECT");
                            mFMRecorder.onSDRemoved();

                            if (oldState == FMRecorder.STATE_RECORDING) {
                                onRecorderError(FMRecorder.ERROR_SDCARD_NOT_PRESENT);
                                mFMRecorder.discardRecording();
                            }
                            Intent i = new Intent(ACTION_RECORDING_STATE_CHANGED);
                            i.putExtra(EXTRA_RECORDING_STATE, FMRecorder.STATE_IDLE);
                            sendBroadcast(i);
                        }
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        registerReceiver(mSDListener, filter);
        FMRadioLogUtils.v(TAG, "registerSDListener <<< ");
    }

    public void onRecorderStateChanged(int state) {
        FMRadioLogUtils.d(TAG, ">>> onRecorderStateChanged: " + state);
        if (state == FMRecorder.STATE_IDLE && mIsPowerUp && (!mIsConnectBluetooth)) {
            if ((null != mFMRecorder && mFMRecorder.getPlayCompleted()) || !mIsRecording) {
                enableFMAudio(true);
                mFMRecorder.setPlayCompleted(false);
            }
            
        }
        Intent i = new Intent();
        i.setAction(ACTION_RECORDING_STATE_CHANGED);
        i.putExtra(EXTRA_RECORDING_STATE, state);
        sendBroadcast(i);
        FMRadioLogUtils.d(TAG, "<<< onRecorderStateChanged");
    }

    public void onRecorderError(int error) {
        FMRadioLogUtils.d(TAG, "onRecorderError: " + error);
        Intent i = new Intent();
        i.setAction(ACTION_RECORDER_ERROR);
        i.putExtra(EXTRA_RECORDER_ERROR_STATE, error);
        sendBroadcast(i);
        switch (error) {
        case FMRecorder.ERROR_SDCARD_NOT_PRESENT:
            // Toast.makeText(this, "SD card is not ready!", Toast.LENGTH_SHORT).show();
            break;
        case FMRecorder.ERROR_SDCARD_INSUFFICIENT_SPACE:
            break;
        case FMRecorder.ERROR_RECORDER_INTERNAL:
            break;
        case FMRecorder.ERROR_PLAYER_INTERNAL:
            if (mIsPowerUp && !mUsingFMViaBTController)
                enableFMAudio(true);
            break;
        default:
            FMRadioLogUtils.e(TAG, "onRecorderError: unknown error!!");
            break;
        }
    }
    
    public void onPlayRecordFileComplete() {
        mAudioManager.abandonAudioFocus(mRecordingAudioFocusChangeListener);
    }
    
    private OnAudioFocusChangeListener mFMAudioFocusChangeListener = new OnAudioFocusChangeListener(){
        public void onAudioFocusChange(int focusChange) {
            FMRadioLogUtils.d(TAG, "onAudioFocusChange >>>:focusChange=" + focusChange);
            if ((focusChange == AudioManager.AUDIOFOCUS_LOSS) || (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)) {
                if (mIsSearching) {
                    try {
                        mIsStopScanCalled = true;
                        mBinder.stopScan();
                        FMRadioLogUtils.d(TAG, "FM loss focus,so stop scan channel.");
                    } catch (Exception e) {
                        FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                    }
                }
                if (mIsPowerUp) {
                    try {
                        mBinder.powerDown();
                        FMRadioLogUtils.d(TAG, "FM loss focus,so powerdown FM.");
                    } catch (Exception e) {
                        FMRadioLogUtils.e(TAG, "Exception: Cannot call binder function.");
                    }
                } else {
                    FMRadioLogUtils.d(TAG, "FM is not playing, so do nothing.");
                }
                
                if (FeatureOption.MTK_FM_RECORDING_SUPPORT && mFMRecorder != null) {
                    int fmState = mFMRecorder.getState();
                    if (fmState == FMRecorder.STATE_RECORDING) {
                        mFMRecorder.stopRecording();
                    }
                    FMRadioLogUtils.d(TAG, "FM loss focus,so stop recording or playback.");
                }
            }
        }
    };
    private OnAudioFocusChangeListener mRecordingAudioFocusChangeListener = new OnAudioFocusChangeListener(){
        public void onAudioFocusChange(int focusChange) {
            if ((focusChange == AudioManager.AUDIOFOCUS_LOSS) || (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)) {
                try {
                    mBinder.stopPlayback();
                    FMRadioLogUtils.d(TAG, "FM Recorder loss focus,so stop playback.");
                    if (mIsPowerUp) {
                        mBinder.powerDown();
                        FMRadioLogUtils.d(TAG, "FM loss focus,so powerdown FM.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private MediaPlayer.OnErrorListener playerErrorListener = new MediaPlayer.OnErrorListener(){
        public boolean onError(MediaPlayer mp, int what, int extra) {
            
            if(MediaPlayer.MEDIA_ERROR_SERVER_DIED == what){
                FMRadioLogUtils.e(TAG, "onError: MEDIA_SERVER_DIED");
                mFMPlayer.release();
                mFMPlayer = new MediaPlayer();
                mFMPlayer.setWakeMode(FMRadioService.this, PowerManager.PARTIAL_WAKE_LOCK);
                mFMPlayer.setOnErrorListener(playerErrorListener);
                try {
                    mFMPlayer.setDataSource("MEDIATEK://MEDIAPLAYER_PLAYERTYPE_FM");
                    mFMPlayer.setAudioStreamType(AudioManager.STREAM_FM);
                    mFMPlayer.prepare();
                    mFMPlayer.start();
                } catch (IOException ex) {
                    FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
                    return false;
                } catch (IllegalArgumentException ex) {
                    FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
                    return false;
                } catch (IllegalStateException ex) {
                    FMRadioLogUtils.e(TAG, "setDataSource: " + ex);
                    return false;
                }
            }
            if (FeatureOption.MTK_MT519X_FM_SUPPORT) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                mWakeLock.setReferenceCounted(false);
            }
            return true;
        }
    };
    
    // check recording sd card is unmount
    public boolean isRecordingCardUnmount(Intent intent) {
        String unmountSDCard = intent.getData().toString();
        boolean result = unmountSDCard.equalsIgnoreCase("file://" + mDefaultSDCardPath) ? true : false;
        FMRadioLogUtils.d(TAG, "unmount sd card is recording sd card: " + result);
        return result;
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

    /*
     * ALPS00284472 FM could not continue to play after end a call in suspend mode
     * retry request audio focus
     */
    public boolean isGetAudioFocus() {
        final int REQUEST_AUDIO_FOCUS_TIMES = 10;
        int count = 0;
        do {
            int audioFocus = mAudioManager.requestAudioFocus(mFMAudioFocusChangeListener, AudioManager.STREAM_FM, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocus) {
                return true;
            } else {
                try {
                    // retry after 100ms
                    Thread.sleep(100);
                } catch (Exception e) {
                    FMRadioLogUtils.e(TAG, "FMRadioService.isGetAudioFocus sleep error.", e);
                }
            }
            count ++;
        } while (count < REQUEST_AUDIO_FOCUS_TIMES);
        return false;
    }
}
