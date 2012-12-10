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

package com.mediatek.engineermode.syslogger;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.SystemProperties;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.featureoption.FeatureOption;
import android.os.AsyncResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class SysCommon extends Activity implements OnClickListener {

    private final String TAG = "Syslog";

//    private final String PROP_MD = "com.mediatek.mdlogger.Running"; 
    /* mdlog property change */
    private final String PROP_MD = "debug.mdlogger.Running";

//    private final String PROP_MOBILE = "com.mediatek.mobilelog.Running";
    /* mobilelog property change  --> zhengchao xu*/
    private final String PROP_MOBILE = "debug.MB.running";
//    private final String PROP_NETWORK = "com.mediatek.network.Running";
    /* netlog property change */
    private final String PROP_NETWORK = "persist.radio.netlog.Running";
    private final String PROP_EXTMD = "com.mtk.extmdlogger.Running";
    
//    private final String PER_LOG2SD = "persist.sys.log2sd.defaultpath";
    private final String PER_LOG2SD = "persist.radio.log2sd.path";
    private final String INTERNALSDTEXT = "Path: /mnt/sdcard";
    private final String EXTERNALSDTEXT = "Path: /mnt/sdcard2";
    private final String EXTERNALSDPATH = "/mnt/sdcard2";
    private static final String LOG_CONFIG_FILE_PATH = "/system/etc/mtklog-config.prop";
    
    private final String PROP_ON = "1";
    private final String PROP_OFF = "0";

    private final String BROADCAST_ACTION = "com.mediatek.syslogger.action";
    private final String BROADCAST_KEY_SRC_FROM = "From";
    private final String BROADCAST_KEY_SRC_TO = "To";
    private final String BROADCAST_VAL_SRC_MD = "ModemLog";
    private final String BROADCAST_VAL_SRC_MOBILE = "MobileLog";
    private final String BROADCAST_VAL_SRC_NETWORK = "ActivityNetworkLog";
    private final String BROADCAST_VAL_SRC_EXTMD = "ExtModemLog";
    private final String BROADCAST_VAL_SRC_HQ = "CommonUI";
    private final String BROADCAST_VAL_SRC_UNK = "Unknown";
    private final String BROADCAST_KEY_COMMAND = "Command";
    private final String BROADCAST_VAL_COMMAND_START = "Start";
    private final String BROADCAST_VAL_COMMAND_STOP = "Stop";
    private final String BROADCAST_VAL_COMMAND_UNK = "Unknown";
    private final String BROADCAST_KEY_RETURN = "Return";
    private final String BROADCAST_VAL_RETURN_OK = "Normal";
    private final String BROADCAST_VAL_RETURN_ERR = "Error";
    private final String BROADCAST_KEY_SELFTEST = "Satan";
    private final String BROADCAST_VAL_SELFTEST = "Angel";

    private static final int EVENT_OP_SEARCH_START = 101;
    private static final int EVENT_OP_SEARCH_FIN = 103;
    private static final int EVENT_OP_ERR = 104;
    private static final int EVENT_OP_EXCEPTION = 105;
    private static final int EVENT_OP_TIMEOUT = 106;
    private static final int EVENT_OP_MSG = 107;
    private static final int EVENT_OP_UPDATE_CK = 108;
    private static final int EVENT_TICK = 109;

    private static final int EVENT_OP_EXTMODEM_START = 110;
    private static final int EVENT_OP_EXTMODEM_STOP = 111;
    private GeminiPhone phone = null;
    private boolean mIsExtModem;
    private boolean mExtModemReply;
    private static final String[] STARTEXTMODEM = {"AT+ETSTLP=4,4", ""};
    private static final String[] STOPEXTMODEM = {"AT+ETSTLP=0,0", ""};

    private CheckBox mCKModem;
    private CheckBox mCKMobile;
    private CheckBox mCKNetwork;
    private CheckBox mCKAll;
    private CheckBox mCKExtModem;
    private TextView mTInfo; 
    private TextView mSDcardPath;

    private SYSLogBroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private StringBuilder mMsg = new StringBuilder();

    private boolean mInClickProcess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.systemloggerhq);

        mCKModem = (CheckBox) findViewById(R.id.syslogger_hq_md);
        mCKMobile = (CheckBox) findViewById(R.id.syslogger_hq_mobile);
        mCKNetwork = (CheckBox) findViewById(R.id.syslogger_hq_network);
        mCKAll = (CheckBox) findViewById(R.id.syslogger_hq_all);
        mCKExtModem = (CheckBox) findViewById(R.id.syslogger_hq_extmd);
        mTInfo = (TextView) findViewById(R.id.syslogger_hq_info);
        mSDcardPath = (TextView) findViewById(R.id.syslogger_hq_sd);

        if (mCKModem == null || mCKMobile == null || mCKNetwork == null
                || mCKAll == null || mTInfo == null || mCKExtModem == null) {
            Elog.e(TAG, "clocwork worked...");
            // not return and let exception happened.
        }

        mCKModem.setOnClickListener(this);
        mCKMobile.setOnClickListener(this);
        mCKNetwork.setOnClickListener(this);
        mCKExtModem.setOnClickListener(this);

        // "all" is ambiguous.
        mCKAll.setVisibility(View.GONE);

        // network is not ready.
//        mCKNetwork.setVisibility(View.GONE);  Yu

        if (!FeatureOption.MTK_DT_SUPPORT) {
            mCKExtModem.setVisibility(View.GONE);
        }

        mBroadcastReceiver = new SYSLogBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BROADCAST_ACTION);

        mMsg.append("Standby...\n");

        if (FeatureOption.MTK_DT_SUPPORT) {
            phone = (GeminiPhone) PhoneFactory.getDefaultPhone();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mInClickProcess = false;        
        setCheckBoxStatus();
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
        mTInfo.setText(mMsg.toString());

        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }


    private void setCheckBoxStatus() {
        if (mInClickProcess) {
            return;
        }
        String usermode = SystemProperties.get(PROP_MD);
        // Log.i(TAG, "MODEM "+ usermode);
        if (usermode.equalsIgnoreCase(PROP_ON)) {
            mCKModem.setChecked(true);
        } else {
            mCKModem.setChecked(false);
        }

        usermode = SystemProperties.get(PROP_MOBILE);
        // Log.i(TAG, "MOBILE "+ usermode);
        if (usermode.equalsIgnoreCase(PROP_ON)) {
            mCKMobile.setChecked(true);
            Elog.d(TAG, "Mobile CHECKED  setCheckBoxStatus");
        } else {
            mCKMobile.setChecked(false);
            Elog.d(TAG, "Mobile UN-CHECKED  setCheckBoxStatus");
        }

        usermode = SystemProperties.get(PROP_NETWORK);
         
        if (usermode.equalsIgnoreCase(PROP_ON)) {
            mCKNetwork.setChecked(true);
            Elog.i(TAG, "NETWORK status: "+ usermode);
        } else {
            mCKNetwork.setChecked(false);
            Elog.i(TAG, "NETWORK status: "+ usermode);
        }
        
        if (FeatureOption.MTK_DT_SUPPORT) {
            usermode = SystemProperties.get(PROP_EXTMD);
            Elog.i(TAG, "EXT MODEM " + usermode);
            if (usermode.equalsIgnoreCase(PROP_ON)) {
                mCKExtModem.setChecked(true);
            } else {
                mCKExtModem.setChecked(false);
            }
        }
        
        String sdPath = SystemProperties.get(PER_LOG2SD);
        if(null == sdPath || "".equals(sdPath)){
            Elog.i(TAG, "SystemProperties have not been initialized, try to get path from file.");
            //SystemProperties have not this value, try to get it from system/etc
            File confFile = new File(LOG_CONFIG_FILE_PATH);
            if(confFile.exists()){
                Properties configProperties = new Properties();
                try
                {
                    FileInputStream mfileInputStream = new FileInputStream(confFile);
                    configProperties.load(mfileInputStream);
                    mfileInputStream.close();
                }
                catch(Exception e)
                {
                    Elog.e(TAG , " Exception happen when load mtklog-config.prop" );
                }
                sdPath = configProperties.getProperty(PER_LOG2SD,""); 
                Elog.i(TAG, "SD path from system/etc: " + sdPath);
            }
        }
        
        if (null != sdPath && sdPath.equals(EXTERNALSDPATH)) {
            Elog.i(TAG, "SD path: " + sdPath);
            mSDcardPath.setText(EXTERNALSDTEXT);
        } else {
            mSDcardPath.setText(INTERNALSDTEXT);
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v instanceof CheckBox) {
            mIsExtModem = false;
            CheckBox chk = (CheckBox) v;
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(BROADCAST_KEY_SELFTEST, BROADCAST_VAL_SELFTEST);
            intent.putExtra(BROADCAST_KEY_SRC_FROM, BROADCAST_VAL_SRC_HQ);

            if (chk == mCKModem) {
                intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_MD);
                Elog.i(TAG, "BROADCAST_KEY_SRC_TO :" + BROADCAST_VAL_SRC_MD);
            } else if (chk == mCKMobile) {
                intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_MOBILE);
                Elog.i(TAG, "BROADCAST_KEY_SRC_TO :" + BROADCAST_VAL_SRC_MOBILE);
            } else if (chk == mCKNetwork) {
                intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_NETWORK);
                Elog.i(TAG, "BROADCAST_KEY_SRC_TO :" + BROADCAST_VAL_SRC_NETWORK);
            } else if (chk == mCKExtModem) {
                mIsExtModem = true;
                intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_EXTMD);
                Elog.i(TAG, "BROADCAST_KEY_SRC_TO :" + BROADCAST_VAL_SRC_EXTMD);
            } else {
                Elog.e(TAG, "$90000");
            }

            if (chk.isChecked()) {
                intent.putExtra(BROADCAST_KEY_COMMAND,
                        BROADCAST_VAL_COMMAND_START);
                Elog.i(TAG, "BROADCAST_KEY_COMMAND :" + BROADCAST_VAL_COMMAND_START);
            } else {
                intent.putExtra(BROADCAST_KEY_COMMAND,
                        BROADCAST_VAL_COMMAND_STOP);
                Elog.i(TAG, "BROADCAST_KEY_COMMAND :" + BROADCAST_VAL_COMMAND_STOP);
            }
            if (!mIsExtModem) {
            mHandler.sendEmptyMessage(EVENT_OP_SEARCH_START);
            mHandler.sendEmptyMessageDelayed(EVENT_OP_SEARCH_FIN, 5 * 1000);// 5s
                                                                            // to
                                                                            // close
                                                                            // the
                                                                            // dialog.
            sendBroadcast(intent);
                Elog.i(TAG, "sendBroadcast over!");
            } else {
                if (mCKExtModem.isChecked()) {
                    phone.invokeOemRilRequestStringsGemini(STARTEXTMODEM, mHandler
                            .obtainMessage(EVENT_OP_EXTMODEM_START),Phone.GEMINI_SIM_2);
                } else {
                    phone.invokeOemRilRequestStringsGemini(STOPEXTMODEM, mHandler
                            .obtainMessage(EVENT_OP_EXTMODEM_STOP),Phone.GEMINI_SIM_2);
                } 
                mHandler.sendEmptyMessage(EVENT_OP_SEARCH_START);
                mHandler.sendEmptyMessageDelayed(EVENT_OP_SEARCH_FIN, 5 * 1000);
            }
        }

    }

    private class SYSLogBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Elog.i(TAG, "--> SYSLogBroadcastReceiver.onReceive " + action);
            if (action.equals(BROADCAST_ACTION)) {
                String self = intent.getStringExtra(BROADCAST_KEY_SELFTEST);
                if (self != null && self.equalsIgnoreCase(BROADCAST_VAL_SELFTEST)) {
                    Elog.i(TAG, "Receive loop message.");
                    return;
                }
                String src = intent.getStringExtra(BROADCAST_KEY_SRC_FROM);
                String result = intent.getStringExtra(BROADCAST_KEY_RETURN);
                checkResult(src, result);

                mHandler.sendEmptyMessage(EVENT_OP_SEARCH_FIN);
                SendMsghToHandler(src + " : " + result);
                Elog.i(TAG, "src: " + src + "result: " + result);
            }

            Elog.i(TAG, "<-- SYSLogBroadcastReceiver.onReceive");
        }

        private void checkResult(String which, String res) {
            if (null != res && res.equalsIgnoreCase(BROADCAST_VAL_RETURN_OK)) {
                Elog.i(TAG, "Receive Nomal");
                Message msg = new Message();
                Bundle bun = new Bundle();
                msg.what = EVENT_OP_UPDATE_CK;
                bun.putString("SRC", which);
                bun.putBoolean("BOOL", true);
                msg.setData(bun);
                mHandler.sendMessage(msg);
            } else {
                Message msg = new Message();
                Bundle bun = new Bundle();
                msg.what = EVENT_OP_UPDATE_CK;
                bun.putString("SRC", which);
                bun.putBoolean("BOOL", false);
                msg.setData(bun);
                mHandler.sendMessage(msg);
            }
        }
    }

    private ProgressDialog mDialogSearchProgress = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AlertDialog.Builder builder = null;
            switch (msg.what) {
            case EVENT_OP_SEARCH_START: {
                if (null == mDialogSearchProgress) {
                    mDialogSearchProgress = new ProgressDialog(SysCommon.this);
                    if (null != mDialogSearchProgress) {
                        mDialogSearchProgress
                                .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        String text = "In Progress...";
                        mDialogSearchProgress.setMessage(text);
                        mDialogSearchProgress.setTitle("Start");
                        mDialogSearchProgress.setCancelable(false);
                        mDialogSearchProgress.show();
                    } else {
                        Elog.e(TAG, "new mDialogSearchProgress failed");
                    }
                }
                mInClickProcess = true;
            }
                break;
            case EVENT_OP_SEARCH_FIN:
                if (null != mDialogSearchProgress) {
                    mDialogSearchProgress.dismiss();
                    mDialogSearchProgress = null;
                }
                mInClickProcess = false;

                break;
            case EVENT_OP_ERR:
                Toast.makeText(getApplicationContext(), "Error.",
                        Toast.LENGTH_LONG).show();
                break;

            case EVENT_OP_TIMEOUT:
                if (null != mDialogSearchProgress) {
                    mDialogSearchProgress.dismiss();
                    mDialogSearchProgress = null;

                    builder = new AlertDialog.Builder(SysCommon.this);
                    builder.setTitle("Timeout");
                    builder.setMessage("Operation Timeout.");
                    builder.setPositiveButton("OK", null);
                    builder.create().show();
                }

                break;
            case EVENT_OP_EXCEPTION:
                builder = new AlertDialog.Builder(SysCommon.this);
                builder.setTitle("Internal Error[Code error]");
                builder.setMessage("Exception.");
                builder.setPositiveButton("OK", null);
                builder.create().show();
                break;
            case EVENT_OP_MSG:
                String result = msg.getData().getString("MSG");
                Elog.i(TAG, "handleMessage updateUI" + result);
                mMsg.append(result + "\n");
                mTInfo.setText(mMsg.toString());
                break;
            case EVENT_OP_UPDATE_CK:
                String src = msg.getData().getString("SRC");
                Boolean status = msg.getData().getBoolean("BOOL");
                Elog.i(TAG, "handleMessage update CheckBOx" + src + "status: " + status.toString());
                if (src == null || status == null) {
                    sendEmptyMessage(EVENT_OP_EXCEPTION);
                    return;
                }

                CheckBox ckb = null;
                if (src.equalsIgnoreCase(BROADCAST_VAL_SRC_MD)) {
                    ckb = mCKModem;
                } else if (src.equalsIgnoreCase(BROADCAST_VAL_SRC_MOBILE)) {
                    ckb = mCKMobile;
                } else if (src.equalsIgnoreCase(BROADCAST_VAL_SRC_NETWORK)) {
                    ckb = mCKNetwork;
                } else if (src.equalsIgnoreCase(BROADCAST_VAL_SRC_EXTMD)) {
                    ckb = mCKExtModem;
                } else {
                    Elog.e(TAG, "unknow source: " + src);
                    sendEmptyMessage(EVENT_OP_EXCEPTION);
                    return;
                }

                if (!status) {
                    // restore origin status.
                    if (ckb.isChecked()) {
                        Elog.d(TAG, "update CHECKED->UN  ");
                        ckb.setChecked(false);
                    } else {
                        Elog.d(TAG, "update UN-CHECKED->CH");
                        ckb.setChecked(true);
                    }
                }
                Elog.d(TAG, "update UNCHANGED.");
                break;
            case EVENT_TICK:
                setCheckBoxStatus();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            case EVENT_OP_EXTMODEM_START:
                AsyncResult ar_start = (AsyncResult) msg.obj;
                if (ar_start.exception == null) {
                    Toast.makeText(SysCommon.this, "Change port success!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BROADCAST_ACTION);
                    intent.putExtra(BROADCAST_KEY_SELFTEST, BROADCAST_VAL_SELFTEST);
                    intent.putExtra(BROADCAST_KEY_SRC_FROM, BROADCAST_VAL_SRC_HQ);
                    intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_EXTMD);
                    intent.putExtra(BROADCAST_KEY_COMMAND, BROADCAST_VAL_COMMAND_START);

                    mHandler.sendEmptyMessage(EVENT_OP_SEARCH_START);
                    mHandler.sendEmptyMessageDelayed(EVENT_OP_SEARCH_FIN, 5 * 1000);
                    sendBroadcast(intent);
                    Elog.i(TAG, "sendBroadcast to extModem!");
                    Elog.i(TAG, BROADCAST_KEY_SRC_TO + ":" +BROADCAST_VAL_SRC_EXTMD);
                    Elog.i(TAG, BROADCAST_KEY_COMMAND + ":" +BROADCAST_VAL_COMMAND_START);
                } else {
                    Toast.makeText(SysCommon.this, "Change port failed!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case EVENT_OP_EXTMODEM_STOP:
                AsyncResult ar_stop = (AsyncResult) msg.obj;
                if (ar_stop.exception == null) {
                    Toast.makeText(SysCommon.this, "Change port success!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BROADCAST_ACTION);
                    intent.putExtra(BROADCAST_KEY_SELFTEST, BROADCAST_VAL_SELFTEST);
                    intent.putExtra(BROADCAST_KEY_SRC_FROM, BROADCAST_VAL_SRC_HQ);
                    intent.putExtra(BROADCAST_KEY_SRC_TO, BROADCAST_VAL_SRC_EXTMD);
                    intent.putExtra(BROADCAST_KEY_COMMAND, BROADCAST_VAL_COMMAND_STOP);
                    sendBroadcast(intent);
                    Elog.i(TAG, "sendBroadcast to extModem!");
                    Elog.i(TAG, BROADCAST_KEY_SRC_TO + ":" +BROADCAST_VAL_SRC_EXTMD);
                    Elog.i(TAG, BROADCAST_KEY_COMMAND + ":" +BROADCAST_VAL_COMMAND_STOP);
                } else {
                    Toast.makeText(SysCommon.this, "Change port failed!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
            }
        }
    };

    private void SendMsghToHandler(String s) {
        Message msg = new Message();

        Bundle bun = new Bundle();
        msg.what = EVENT_OP_MSG;
        bun.putString("MSG", s);
        msg.setData(bun);
        mHandler.sendMessage(msg);
    }
}
