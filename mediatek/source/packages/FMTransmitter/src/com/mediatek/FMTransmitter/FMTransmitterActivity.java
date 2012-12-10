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

package com.mediatek.FMTransmitter;


import com.mediatek.FMTransmitter.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.WindowManager;
import android.content.res.Configuration;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.mediatek.featureoption.FeatureOption;

public class FMTransmitterActivity extends Activity {
    public static final String TAG = "FMTxAPK";
    public static FMTransmitterActivity refThis = null;

    private boolean mIsPlaying = false;
    private boolean mIsDoingPlayStop = false;
    private boolean mIsSeeking = false;
    private boolean mIsServiceStarted = false;
    private boolean mIsServiceBinded = false;
    private boolean mIsDestroying = false;
    private ServiceConnection mServiceConnection = null;
    private IFMTransmitterService mService = null;
    private Handler mHandler = null;

    // The toast and its timer
    public static final long TOAST_TIMER_DELAY = 2000; // Timer delay 2 seconds.
    private Toast mToast = null;
    private Timer mTimer = null;

    private static final String TYPE_MSGID = "MSGID";
    private static final int MSGID_PLAYSTOP_FINISH = 1;
    private static final int MSGID_SEEK_FINISH = 2;

    // Seek parameters.
    private static final int SEEK_CHANNEL_COUNT = 1;
    private static final int SEEK_CHANNEL_DIRECTION = 0;
    private static final int SEEK_CHANNEL_GAP = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 5 : 1;
    private static final int BASE_NUMBER = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 100 : 10;

    public int mCurrentStation = FMTransmitterStation.FIXED_STATION_FREQ; // 100.0 MHz
    private final int REQUEST_CODE_ADVANCED = 1;

    //private RelativeLayout mMainView = null;
    private ImageButton mButtonPlayStop = null;
    private ImageButton mButtonSeek = null;
    private ImageButton mButtonAdvanced = null;
    private TextView mTextStationValue = null;
    private TextView mTextFM = null;
    private TextView mTextMHz = null;
    private ProgressDialog mDialogSearchProgress = null;
    private Context mContext;

     // add for CMCC project
    protected static final String OP = android.os.SystemProperties.get("ro.operator.optr");
    protected static final boolean IS_CMCC = ("OP01").equals(OP);
    private enum TxDeviceStateEnum {
        TXOPENED, 
        TXPOWERUP,
        TXCLOSED
    };
    private class FMTxAppBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onReceive");
            String action = intent.getAction();
            FMTxLogUtils.v(TAG, "Context: " + context);
            FMTxLogUtils.v(TAG, "Action: " + action);
            if (action.equals(FMTransmitterService.ACTION_STATE_CHANGED)) {
                mIsPlaying = intent.getBooleanExtra(FMTransmitterService.EXTRA_FMTX_ISPOWERUP, false);
                enableAllButtons(true);
                refreshAllButtonsImages();
                refreshAllButtonsStatus();
            }
            else {
                FMTxLogUtils.e(TAG, "Error: undefined Tx action.");
            }
            FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onReceive");
        }
    }

    private FMTxAppBroadcastReceiver mBroadcastReceiver = null;

    public void onCreate(Bundle savedInstanceState) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onCreate");
        
        refThis = this;
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            //    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
     //mark for ICS phase 1 not support theme change
        mContext = getApplicationContext();
     /*   mMainView = (RelativeLayout)findViewById(R.id.main_view);
        if (FeatureOption.MTK_THEMEMANAGER_APP) {
            mMainView.setThemeContentBgColor(0xff000000);
          }*/
        mCurrentStation = FMTransmitterStation.getCurrentStation(mContext);
        
        mTextStationValue = (TextView)findViewById(R.id.station_value);
        if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
            mTextStationValue.setTextSize(50);
        }
        mTextStationValue.setText(formatStation(mCurrentStation));

        mTextFM = (TextView)findViewById(R.id.text_fm);
        mTextFM.setText("FM");

        mTextMHz = (TextView)findViewById(R.id.text_mhz);
        mTextMHz.setText("MHz");
        
        mButtonPlayStop = (ImageButton)findViewById(R.id.button_play_stop);
        mButtonPlayStop.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    FMTxLogUtils.d(TAG, ">>> onClick PlayStop");
                    handlePowerClick();
                    FMTxLogUtils.d(TAG, "<<< onClick PlayStop");
                }
            }
        );

        mButtonAdvanced = (ImageButton)findViewById(R.id.button_advanced);
        mButtonAdvanced.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    FMTxLogUtils.d(TAG, ">>> onClick Advanced");
                    // Show advanced activity.
                    Intent intent = new Intent();
                    intent.setClass(FMTransmitterActivity.this, FMTransmitterAdvanced.class);
                    startActivityForResult(intent, REQUEST_CODE_ADVANCED);
                    FMTxLogUtils.d(TAG, "<<< onClick Advanced");
                }
            }
        );

        mButtonSeek = (ImageButton)findViewById(R.id.button_seek);
        mButtonSeek.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    FMTxLogUtils.d(TAG, ">>> onClick Seek");
                    if (mIsSeeking) {
                        FMTxLogUtils.e(TAG, "Error: already seeking.");
                    }
                    else {
                        mIsSeeking = true;
                        
                        // Start to search an unoccupied channel. Use a progress dialog to show the progress.
                        mDialogSearchProgress = new ProgressDialog(FMTransmitterActivity.this);
                        mDialogSearchProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mDialogSearchProgress.setMessage(getString(R.string.dlg_search_text));
                        mDialogSearchProgress.setTitle(R.string.dlg_seek_title);
                        mDialogSearchProgress.setCancelable(false);
                        mDialogSearchProgress.show();
                        
                        new Thread(){
                            public void run(){
                                FMTxLogUtils.d(TAG, ">>> SeekThread.run");
                                // Seek an unoccupied channel which is bigger than current.
                                int iStation = mCurrentStation + SEEK_CHANNEL_GAP;
                                if (iStation > FMTransmitterStation.HIGHEST_STATION) {
                                    iStation = FMTransmitterStation.LOWEST_STATION;
                                }
                                float[] channels = searchChannelsForTx((float)iStation / BASE_NUMBER, SEEK_CHANNEL_DIRECTION, SEEK_CHANNEL_COUNT);
                                if (null == channels) {
                                    FMTxLogUtils.v(TAG, "Seek again from the lowest frequency");
                                    channels = searchChannelsForTx((float)FMTransmitterStation.LOWEST_STATION / BASE_NUMBER, SEEK_CHANNEL_DIRECTION, SEEK_CHANNEL_COUNT);
                                }

                                if (null != channels) {
                                    FMTxLogUtils.v(TAG, "Seek out channel number: " + channels.length);
                                    int iFrq = (int)(channels[0] * BASE_NUMBER);
                                    FMTxLogUtils.v(TAG, "Seek out channel: " + iFrq);
                                    
                                    if (turnToFrequency((float)iFrq / BASE_NUMBER)) {
                                        mCurrentStation = iFrq;
                                        
                                        // Save the current station frequency into data base.
                                        FMTransmitterStation.setCurrentStation(mContext, mCurrentStation);
                                    }
                                    else {
                                        FMTxLogUtils.e(TAG, "Error: tune to channel failed.");
                                    }
                                }
                                else {
                                    FMTxLogUtils.e(TAG, "Error: seek channel failed.");
                                }

                                // Close progress dialog.
                                if (null != mDialogSearchProgress) {
                                    mDialogSearchProgress.dismiss();
                                    mDialogSearchProgress = null;
                                }

                                mIsSeeking = false;

                                // Send message to update UI.
                                Message msg = new Message();
                                msg.setTarget(mHandler);
                                Bundle bundle = new Bundle();
                                bundle.putInt(TYPE_MSGID, MSGID_SEEK_FINISH);
                                msg.setData(bundle);
                                msg.sendToTarget();
                                
                                FMTxLogUtils.d(TAG, "<<< SeekThread.run");
                            }
                        }.start();
                    }
                    
                    FMTxLogUtils.d(TAG, "<<< onClick Seek");
                }
            }
        );

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.handleMessage ID: " + msg.getData().getInt(TYPE_MSGID));
                if (mIsDestroying) {
                    FMTxLogUtils.w(TAG, "Warning: app is being destroyed.");
                    FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.handleMessage");
                    return;
                }
                
                if (MSGID_PLAYSTOP_FINISH == msg.getData().getInt(TYPE_MSGID)) {
                   long endPowerTime = 0;
                    enableAllButtons(true);
                    refreshAllButtonsImages();
                    refreshAllButtonsStatus();
                    endPowerTime = System.currentTimeMillis();
                    FMTxLogUtils.i(TAG, "[Performance test][FMTransmitter] Test FM Tx Power on end ["+ endPowerTime +"]");
                    FMTxLogUtils.i(TAG, "[Performance test][FMTransmitter] Test FM Tx Power down end ["+ endPowerTime +"]");
                }
                else if (MSGID_SEEK_FINISH == msg.getData().getInt(TYPE_MSGID)) {
                    long endSeekTime = 0;
                    mTextStationValue.setText(formatStation(mCurrentStation));
                    endSeekTime = System.currentTimeMillis();
                    FMTxLogUtils.i(TAG, "[Performance test][FMTransmitter] Test FM Tx total seek time end ["+ endSeekTime +"]");
                }
                else {
                    FMTxLogUtils.e(TAG, "Error: undefined message ID.");
                }
                FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.handleMessage");
            }
        };

        // Register broadcast receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FMTransmitterService.ACTION_STATE_CHANGED);
        mBroadcastReceiver = new FMTxAppBroadcastReceiver();
        FMTxLogUtils.i(TAG, "Register Tx broadcast receiver.");
        registerReceiver(mBroadcastReceiver, filter);
        
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onCreate");
    }

    public void handlePowerClick() {
        if (mIsDoingPlayStop) {
            FMTxLogUtils.e(TAG, "Error: already doing play/stop.");
        } else if (isEarphonePlugged()) {
            // When earphone is plugged, should not power up FM Tx.
            FMTxLogUtils.w(TAG, "Warning: do not power up Tx when earphone is plugged.");

            // Show toast if there is no toast being shown at the moment.
           // if (!isToasting()) {
                showToast(getString(R.string.toast_plugout_earphone));
           // }
        } else {
            mIsDoingPlayStop = true;
            enableAllButtons(false);
            new Thread() {
                public void run() {
                    FMTxLogUtils.d(TAG, ">>> PlayStopThread.run");
                    switch (getTxStatus()) {
                    case TXOPENED:
                        FMTxLogUtils.v(TAG,"Device is open,then power up tx");
                        // power up Tx
                        if (powerUpTx((float) mCurrentStation / BASE_NUMBER)) {
                            mIsPlaying = true;
                        } else {
                            FMTxLogUtils.e(TAG, "Error: Cannot power up.");
                        }
                        break;
                    case TXPOWERUP:
                        FMTxLogUtils.v(TAG,"Device is power up,then power down tx and close device");
                        // power down tx.
                        if (powerDownTx() && closeTxDevice()) {
                            mIsPlaying = false;
                        } else {
                            FMTxLogUtils.e(TAG, "Error: Cannot power down.");
                        }
                        break;
                    case TXCLOSED:
                        FMTxLogUtils.v(TAG,"Device is closed,then open device and power up tx");
                        // first open device, if succeed, then power up Tx
                        if (openTxDevice()) {
                            if (powerUpTx((float) mCurrentStation / BASE_NUMBER)) {
                                mIsPlaying = true;
                            } else {
                                FMTxLogUtils.e(TAG, "Error: Cannot power up.");
                            }
                        } else {
                            FMTxLogUtils.e(TAG, "Error: FM Tx device is not open");
                        }
                        break;
                    default:
                        break;
                    }
                    mIsDoingPlayStop = false;
                    // Send message to update UI.
                    Message msg = new Message();
                    msg.setTarget(mHandler);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TYPE_MSGID, MSGID_PLAYSTOP_FINISH);
                    msg.setData(bundle);
                    msg.sendToTarget();
                    FMTxLogUtils.d(TAG, "<<< PlayStopThread.run");
                }
            }.start();
        }

    }
    public void onStart() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onStart");
        // Should start FM Tx service first.
        ComponentName cn = startService(new Intent(FMTransmitterActivity.this, FMTransmitterService.class));
        if (null == cn) {
            FMTxLogUtils.e(TAG, "Error: Cannot start FM Tx service");
        }
        else {
            FMTxLogUtils.d(TAG, "Start FM Tx service successfully.");
            mIsServiceStarted = true;

            mServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {
                    FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onServiceConnected");
                    mService = IFMTransmitterService.Stub.asInterface(service);
                    if (null == mService) {
                        FMTxLogUtils.e(TAG, "Error: null interface");
                        finish();
                    }
                    else {
                        if (!isServiceInit()) {
                            FMTxLogUtils.i(TAG, "FM Tx service is not init.");
                            initService((float)mCurrentStation / BASE_NUMBER);
                            mIsPlaying = isTxPowerUp();

                            refreshAllButtonsImages();
                            refreshAllButtonsStatus();
                        }
                        else {
                            FMTxLogUtils.i(TAG, "FM Tx service is already init.");
                                // Get the current frequency in service and save it into database.
                                int iFreq = (int)(getCurFrequency() * BASE_NUMBER);
                                if (iFreq > FMTransmitterStation.HIGHEST_STATION
                                    || iFreq < FMTransmitterStation.LOWEST_STATION) {
                                    FMTxLogUtils.e(TAG, "Error: invalid frequency in service.");
                                }
                                else {
                                    if (mCurrentStation != iFreq) {
                                        FMTxLogUtils.i(TAG, "The frequency in FM Tx service is not same as in database.");
                                       // mCurrentStation = iFreq;

                                        // Save the current station frequency into data base.
                                        FMTransmitterStation.setCurrentStation(mContext, mCurrentStation);
                                        // Change the station frequency displayed.
                                        mTextStationValue.setText(formatStation(mCurrentStation));
                                    }
                                    else {
                                        FMTxLogUtils.i(TAG, "The frequency in FM Tx service is same as in database.");
                                    }
                                }
                                
                                if (!isSearching()) {
                                    mIsPlaying = isTxPowerUp();
                                    
                                    refreshAllButtonsImages();
                                    refreshAllButtonsStatus();
                                }
                            }
                    }
                    FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onServiceConnected");
                }

                public void onServiceDisconnected(ComponentName className) {
                    FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onServiceDisconnected");
                    mService = null;
                    FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onServiceDisconnected");
                }
            };
            mIsServiceBinded = bindService(
                new Intent(FMTransmitterActivity.this, FMTransmitterService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
        }
        if (!mIsServiceBinded) {
            FMTxLogUtils.e(TAG, "Error: Cannot bind FM Tx service");
            finish();
            FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onCreat");
            return;
        }
        else {
            FMTxLogUtils.d(TAG, "Bind FM Tx service successfully.");
        }
        super.onStart();
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onStart");
    }
    
    public void onResume() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onResume");
        super.onResume();
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onResume");
    }

    public void onPause() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onPause");
        super.onPause();
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onPause");
    }
    
    public void onStop() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onStop");
          // Unbind the FM service.
        if (mIsServiceBinded) {
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }
        super.onStop();
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onStop");
    }

    public void onDestroy() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onDestroy");
        mIsDestroying = true;
    
        // Unregister the broadcast receiver.
        if (null != mBroadcastReceiver) {
            FMTxLogUtils.v(TAG, "Unregister Tx broadcast receiver.");
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        switch (getTxStatus()) {
            case TXOPENED:
                if (!closeTxDevice()) {
                    FMTxLogUtils.e(TAG, "Error: FM Tx device is not closed");
                }
                break;
            case TXPOWERUP:
                FMTxLogUtils.v(TAG, "FM Tx device also can work");
                break;
            case TXCLOSED:
                if (mIsServiceStarted) {
                    boolean bRes = stopService(new Intent(FMTransmitterActivity.this,FMTransmitterService.class));
                    if (!bRes) {
                        FMTxLogUtils.e(TAG, "Error: Cannot stop the FM service.");
                    }
                    mIsServiceStarted = false;
                }
                FMTxLogUtils.v(TAG, "FM Tx device is closed");
                break;
            default:
                break;
            }
        super.onDestroy();

        refThis = null;
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onDestroy");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.onConfigurationChanged");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            if (REQUEST_CODE_ADVANCED == requestCode) {
                int iStation = data.getIntExtra(FMTransmitterAdvanced.ACTIVITY_RESULT, mCurrentStation);
                mCurrentStation = iStation;
                // Save the current station frequency into data base.
                FMTransmitterStation.setCurrentStation(FMTransmitterActivity.this, mCurrentStation);
                // Update UI.
                mTextStationValue.setText(formatStation(mCurrentStation));
                // Tune to this channel.
                if (!isTxPowerUp() && !isEarphonePlugged()) {
                    powerUpTx((float)mCurrentStation / BASE_NUMBER);
                }
                
                if (isTxPowerUp()) {
                    if (turnToFrequency((float)iStation / BASE_NUMBER)) {
                    }
                    else {
                        FMTxLogUtils.e(TAG, "Error: tune to channel failed.");
                    }
                }
                else {
                    FMTxLogUtils.i(TAG, "Error: cannot power up Tx");
                }
                

                
            }
            else {
                FMTxLogUtils.e(TAG, "Error: Invalid requestcode.");
            }
        }
        else {
            // Do not handle other result.
            FMTxLogUtils.v(TAG, "The activity for requestcode " + requestCode + " does not return any data.");
        }
		long endTuneTime = 0;
		endTuneTime = System.currentTimeMillis();
        FMTxLogUtils.i(TAG, "[Performance test][FMTransmitter] Test FM Tx Tune end ["+ endTuneTime +"]");
    }

    private void refreshAllButtonsImages() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.refreshAllButtonsImages");
        //... Refresh button images.
        if (mIsPlaying) {
            mButtonPlayStop.setImageResource(R.drawable.fmtx_started);
            mButtonSeek.setImageResource(R.drawable.fmtx_seek);
            mButtonAdvanced.setImageResource(R.drawable.fmtx_setting);
        }
        else {
            mButtonPlayStop.setImageResource(R.drawable.fmtx_stop);
            mButtonSeek.setImageResource(R.drawable.fmtx_seek_dis);
            mButtonAdvanced.setImageResource(R.drawable.fmtx_setting_dis);
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.refreshAllButtonsImages");
    }

    private void refreshAllButtonsStatus() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.refreshAllButtonsStatus");
        //... Refresh button enable/disable status.
        if (mIsPlaying) {
            mButtonSeek.setEnabled(true);
            mButtonAdvanced.setEnabled(true);
        }
        else {
            mButtonSeek.setEnabled(false);
            mButtonAdvanced.setEnabled(false);
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.refreshAllButtonsStatus");
    }

    private void enableAllButtons(boolean enable) {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.enableAllButtons: " + enable);
        mButtonPlayStop.setEnabled(enable);
        mButtonSeek.setEnabled(enable);
        mButtonAdvanced.setEnabled(enable);
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.enableAllButtons");
    }
    
    private boolean isEarphonePlugged() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.isEarphonePlugged");
        boolean bRet = true;
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (null != am) {
            bRet = am.isWiredHeadsetOn();
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.isEarphonePlugged: " + bRet);
        return bRet;
    }

    private void showToast(CharSequence text) {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.showToast: " + text);
        // Schedule a timer to clear the toast.
      /*  mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Clear the timer and toast.
                cancelToast();
            }
        }, TOAST_TIMER_DELAY, TOAST_TIMER_DELAY);
        */
        // Toast it.
        if (null == mToast) {
            mToast = Toast.makeText(FMTransmitterActivity.this, text, Toast.LENGTH_SHORT);
        }
        mToast.show();
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.showToast");
    }

/*    private void cancelToast() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.cancelToast");
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        else {
            FMTxLogUtils.w(TAG, "Warning: The timer is null.");
        }
        if (null != mToast) {
            mToast.cancel();
            mToast = null;
        }
        else {
            FMTxLogUtils.w(TAG, "Warning: The toast is null.");
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.cancelToast");
    }

    private boolean isToasting() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.isToasting");
        boolean bRet = true;
        if (null == mToast && null == mTimer) {
            bRet = false;
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.isToasting: " + bRet);
        return bRet;
    }
*/
    private void enableRDS() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.enableRDS");
        if (isRDSTxSupport()) {
            if (isRDSOn() || setRDSTxEnabled(true)) {
                 if (IS_CMCC) {
                     char[] ps = {'A', 't', 'h', 'e', 'n', 's', '1', '5'};
                     setRDSText(mCurrentStation, ps, null, 0);
                 } else {
                     char[] ps = {'M', 'e', 'd', 'i', 'a', 't', 'e', 'k'};
                     setRDSText(mCurrentStation, ps, null, 0);
                 }
            }
        }
        else {
            FMTxLogUtils.d(TAG, "RDS Tx is not supported.");
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.enableRDS");
    }

    private void disableRDS() {
        FMTxLogUtils.v(TAG, ">>> FMTransmitterActivity.disableRDS");
        if (isRDSOn()) {
            setRDSTxEnabled(false);
        }
        FMTxLogUtils.v(TAG, "<<< FMTransmitterActivity.disableRDS");
    }

    private String formatStation(int station) {
        String result = null;
        if (FeatureOption.MTK_FM_50KHZ_SUPPORT) {
            result = String.format(Locale.ENGLISH, "%.2f",  (float)station/BASE_NUMBER);
        } else {
            result = String.format(Locale.ENGLISH, "%.1f",  (float)station/BASE_NUMBER);
        }
        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////
    // Wrap service interfaces.
    public boolean openTxDevice() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.openTxDevice");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.openTxDevice();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.openTxDevice: " + bRet);
        return bRet;
    }

    public boolean closeTxDevice() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.closeTxDevice");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.closeTxDevice();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.closeTxDevice: " + bRet);
        return bRet;
    }

    public boolean isTxDeviceOpen() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isTxDeviceOpen");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isTxDeviceOpen();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isTxDeviceOpen: " + bRet);
        return bRet;
    }

    public boolean powerUpTx(float frequency) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.powerUpTx: " + frequency);
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.powerUpTx(frequency);
                if (bRet) {
                    enableRDS();
                }
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.powerUpTx: " + bRet);
        return bRet;
    }

    public boolean powerDownTx() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.powerDownTx");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                disableRDS();
                bRet = mService.powerDownTx();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.powerDownTx: " + bRet);
        return bRet;
    }

    public boolean isTxPowerUp() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isTxPowerUp");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isTxPowerUp();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isTxPowerUp: " + bRet);
        return bRet;
    }

    public boolean isSearching() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isSearching");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isSearching();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isSearching: " + bRet);
        return bRet;
    }
    
    public boolean turnToFrequency(float frequency) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.turnToFrequency: " + frequency);
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.turnToFrequency(frequency);
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.turnToFrequency: " + bRet);
        return bRet;
    }

    public boolean initService(float frequency) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.initService: " + frequency);
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.initService(frequency);
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.initService: " + bRet);
        return bRet;
    }
    
    public boolean isServiceInit() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isServiceInit");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isServiceInit();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isServiceInit: " + bRet);
        return bRet;
    }

    public float getCurFrequency() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.getCurFrequency");
        //...
        float ret = (float)FMTransmitterStation.FIXED_STATION_FREQ / BASE_NUMBER;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                ret = mService.getCurFrequency();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.getCurFrequency: " + ret);
        return ret;
    }
    
    public float[] searchChannelsForTx(float frequency, int direction, int number) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.searchChannelsForTx: " + frequency + direction + number);
        float[] ret = null;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                ret = mService.searchChannelsForTx(frequency, direction, number);
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.searchChannelsForTx: " + Arrays.toString(ret));
        return ret;
    }

    public boolean isRDSTxSupport() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isRDSTxSupport");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isRDSTxSupport();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isRDSTxSupport: " + bRet);
        return bRet;
    }

    public boolean isRDSOn() {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.isRDSOn");
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.isRDSOn();
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.isRDSOn: " + bRet);
        return bRet;
    }

    public boolean setRDSTxEnabled(boolean state) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.setRDSTxEnabled: " + state);
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.setRDSTxEnabled(state);
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.setRDSTxEnabled: " + bRet);
        return bRet;
    }

    public boolean setRDSText(int pi, char[] ps, int[] rdsText, int rdsCnt) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterActivity.setRDSText: " + pi + " " + Arrays.toString(ps) + " " + rdsText + " " + rdsCnt);
        boolean bRet = false;
        if (null == mService) {
            FMTxLogUtils.e(TAG, "Error: No service interface.");
        }
        else {
            try {
                bRet = mService.setRDSText(pi, ps, rdsText, rdsCnt);
            }
            catch (Exception e) {
                FMTxLogUtils.e(TAG, "Exception: Cannot call service function.");
            }
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterActivity.setRDSText: " + bRet);
        return bRet;
    }

    /**
     * Get FMTx device status
     * 
     * @return FMTx device status which can be open,powerup,closed
     */
    public TxDeviceStateEnum getTxStatus() {
        FMTxLogUtils.d(TAG,"getTxStatus");
        if (isTxPowerUp()) {
            FMTxLogUtils.v(TAG, "tx state -> power up.");
            return TxDeviceStateEnum.TXPOWERUP;

        } else if (isTxDeviceOpen()) {
            FMTxLogUtils.v(TAG, "tx state -> open.");
            return TxDeviceStateEnum.TXOPENED;

        }
        return TxDeviceStateEnum.TXCLOSED;
    }
}
