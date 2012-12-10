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

package com.mediatek.engineermode.cpustress;

import java.io.IOException;

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;
import com.mediatek.engineermode.cpustress.CpuStressTestService.ICpuStressTestComplete;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mediatek.xlog.Xlog;

public class ClockSwitch extends Activity implements OnCheckedChangeListener,
        OnClickListener, ServiceConnection, ICpuStressTestComplete {

    private static final String TAG = "EM/CpuStress_ClockSwitch";
    private CheckBox mCbDebugMsgEnable = null;
    private TextView mTvDebugMsgEnable = null;
    private Button mBtnStart = null;
    private Button mBtnSwitchM = null;
    private Button mBtnSwitchG = null;
    private EditText mEtSecond = null;
    private EditText mEtNSecond = null;
    private static final String CPU_SS_MODE = "/proc/cpu_ss/cpu_ss_mode";
    private static final String CPU_SS_PERIOD = "/proc/cpu_ss/cpu_ss_period";
    private static final String CPU_SS_PERIOD_MODE = "/proc/cpu_ss/cpu_ss_period_mode";
    private static final String CPU_SS_DEBUG_MODE = "/proc/cpu_ss/cpu_ss_debug_mode";
    private static final String[] FILES = new String[] { CPU_SS_MODE,
            CPU_SS_PERIOD, CPU_SS_PERIOD_MODE, CPU_SS_DEBUG_MODE };
    private static final int INDEX_SET_MODE = 0;
    private static final int INDEX_SET_PERIOD = 1;
    private static final int INDEX_SET_PERIOD_MODE = 2;
    private static final int INDEX_SET_DEBUG_MODE = 3;
    private static final int INDEX_QUERY_MODE = 10;
    private static final int INDEX_QUERY_PERIOD = 11;
    private static final int INDEX_QUERY_PERIOD_MODE = 12;
    private static final int INDEX_QUERY_DEBUG_MODE = 13;
    private static final String INDEX_SET_MODE_VALUE_0 = "0";
    private static final String INDEX_SET_MODE_VALUE_1 = "1";
    private static final String INDEX_SET_PERIOD_MODE_VALUE_E = "enable";
    private static final String INDEX_SET_PERIOD_MODE_VALUE_D = "disable";
    private static final String INDEX_SET_DEBUG_MODE_VALUE_E = "enable";
    private static final String INDEX_SET_DEBUG_MODE_VALUE_D = "disable";
    private static final int DIALOG_WAIT = 1;
    private int recordMask = 0x0;
    private CpuStressTestService mBoundService = null;
    private boolean bBounded = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Xlog.v(TAG, "mHandler receive message: " + msg.what);
            Xlog.v(TAG, "msg.what: " + msg.what + " msg.obj: "
                    + msg.obj.toString());
            switch (msg.what) {
                case INDEX_SET_MODE:
                case INDEX_SET_PERIOD:
                case INDEX_SET_PERIOD_MODE:
                case INDEX_SET_DEBUG_MODE:
                    Toast.makeText(ClockSwitch.this,
                            FILES[msg.what % 10] + ":" + msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case INDEX_QUERY_MODE:
                    recordMask |= 1 << INDEX_QUERY_MODE % 10;
                    // updateSwitchView(msg.obj.toString().trim().equals(
                    // INDEX_SET_MODE_VALUE_0));
                    if (0xF == recordMask) {
                        removeDialog(DIALOG_WAIT);
                    }
                    break;
                case INDEX_QUERY_PERIOD:
                    recordMask |= 1 << INDEX_QUERY_PERIOD % 10;
                    updatePeriodView(msg.obj.toString().trim());
                    if (0xF == recordMask) {
                        removeDialog(DIALOG_WAIT);
                    }
                    break;
                case INDEX_QUERY_PERIOD_MODE:
                    recordMask |= 1 << INDEX_QUERY_PERIOD_MODE % 10;
                    updateAutoTestView(msg.obj.toString().trim().equals(
                            INDEX_SET_PERIOD_MODE_VALUE_E));
                    if (0xF == recordMask) {
                        removeDialog(DIALOG_WAIT);
                    }
                    break;
                case INDEX_QUERY_DEBUG_MODE:
                    recordMask |= 1 << INDEX_QUERY_DEBUG_MODE % 10;
                    mCbDebugMsgEnable.setEnabled(false);
                    mCbDebugMsgEnable.setChecked(msg.obj.toString().trim()
                            .equals(INDEX_SET_DEBUG_MODE_VALUE_E));
                    mCbDebugMsgEnable.setEnabled(true);
                    if (0xF == recordMask) {
                        removeDialog(DIALOG_WAIT);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Xlog.v(TAG, "Enter onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hqa_cpustress_clockswitch);
        mCbDebugMsgEnable = (CheckBox) findViewById(R.id.clockswitch_debug_enable);
        mTvDebugMsgEnable = (TextView) findViewById(R.id.clockswitch_debug_view);
        mBtnStart = (Button) findViewById(R.id.clockswitch_btn_start);
        mBtnSwitchM = (Button) findViewById(R.id.clockswitch_btn_switch26);
        mBtnSwitchG = (Button) findViewById(R.id.clockswitch_btn_switch1g);
        mEtSecond = (EditText) findViewById(R.id.clockswitch_timeout_s);
        mEtNSecond = (EditText) findViewById(R.id.clockswitch_timeout_ns);
        if (null != mCbDebugMsgEnable && null != mTvDebugMsgEnable
                && null != mBtnStart && null != mBtnSwitchM
                && null != mBtnSwitchG && null != mEtSecond
                && null != mEtNSecond) {
            mCbDebugMsgEnable.setOnCheckedChangeListener(this);
            mTvDebugMsgEnable.setOnClickListener(this);
            mBtnStart.setOnClickListener(this);
            mBtnSwitchM.setOnClickListener(this);
            mBtnSwitchG.setOnClickListener(this);
        } else {
            Toast
                    .makeText(this, "Get view is null, return",
                            Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    public void onStart() {
        Xlog.v(TAG, "Enter onStart");
        showDialog(DIALOG_WAIT);
        recordMask = 0;
        new Thread(new Runnable() {

            public void run() {
                getCurrentStatus(INDEX_QUERY_MODE);
                getCurrentStatus(INDEX_QUERY_PERIOD);
                getCurrentStatus(INDEX_QUERY_PERIOD_MODE);
                getCurrentStatus(INDEX_QUERY_DEBUG_MODE);
            }
        }).start();
        this.bindService(new Intent(ClockSwitch.this,
                CpuStressTestService.class), this, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        Xlog.v(TAG, "Enter onStop");
        // mBoundService.testObject = null;
        this.unbindService(this);
        super.onStop();
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_WAIT) {
            ProgressDialog dialog = new ProgressDialog(ClockSwitch.this);
            if (dialog != null) {
                dialog.setTitle("Waiting");
                dialog.setMessage("Please wait for getting status...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
                Xlog.v(TAG, "create ProgressDialog");
            }
            return dialog;
        }
        return null;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void updatePeriodView(String period) {
        Xlog.v(TAG, "Enter updatePeriodView: " + period);
        int begin;
        int end;
        begin = period.indexOf('(');
        if (-1 != begin) {
            mEtSecond.setText(period.substring(0, begin).trim());
        }
        begin = period.indexOf(')');
        end = period.lastIndexOf('(');
        if (-1 != begin && -1 != end && begin < end) {
            mEtNSecond.setText(period.substring(begin + 1, end).trim());
        }
        if (null != mBoundService) {
            if (!mBoundService.isClockSwitchRun()) {
                mEtSecond.setText("1");
                mEtNSecond.setText("0");
            }
        }
        mEtSecond.setSelection(mEtSecond.getText().length());
        mEtNSecond.setSelection(mEtNSecond.getText().length());
    }

    private void updateAutoTestView(boolean bRun) {
        if (bRun) {
            mBtnStart.setText(R.string.hqa_cpustress_clockswitch_stop);
            mEtSecond.setEnabled(false);
            mEtNSecond.setEnabled(false);
            mBtnSwitchM.setEnabled(false);
            mBtnSwitchG.setEnabled(false);
        } else {
            mBtnStart.setText(R.string.hqa_cpustress_clockswitch_start);
            mEtSecond.setEnabled(true);
            mEtNSecond.setEnabled(true);
            mBtnSwitchM.setEnabled(true);
            mBtnSwitchG.setEnabled(true);
        }
    }

    private void updateSwitchView(boolean bM) {
        if (bM) {
            mBtnSwitchM.setEnabled(false);
            mBtnSwitchG.setEnabled(true);
        } else {
            mBtnSwitchM.setEnabled(true);
            mBtnSwitchG.setEnabled(false);
        }
    }

    private void handleEvent(final String value, final int index) {
        Xlog.v(TAG, "handleEvent: " + value + " " + index);
        new Thread(new Runnable() {

            public void run() {
                setCommand(value, index);
                getResponse(index);
            }
        }).start();
    }

    private synchronized void getResponse(int index) {
        switch (index) {
            case INDEX_SET_MODE:
            case INDEX_SET_PERIOD:
            case INDEX_SET_PERIOD_MODE:
            case INDEX_SET_DEBUG_MODE:
                getStatus(index);
                break;
            default:
                Xlog.d(TAG, "getResponse: index is error, " + index);
                break;
        }
    }

    private synchronized void setCommand(String value, int index) {
        String command = "echo " + value + " > " + FILES[index % 10];
        Xlog.v(TAG, "setCommand: " + command);
        try {
            ShellExe.execCommand(new String[] { "sh", "-c", command });
        } catch (IOException e) {
            Xlog.d(TAG, e.getMessage());
        }
    }

    private synchronized void getCurrentStatus(int index) {
        switch (index) {
            case INDEX_QUERY_MODE:
            case INDEX_QUERY_PERIOD:
            case INDEX_QUERY_PERIOD_MODE:
            case INDEX_QUERY_DEBUG_MODE:
                getStatus(index);
                break;
            default:
                Xlog.d(TAG, "getCurrentStatus: index is error, " + index);
                break;
        }
    }

    private synchronized void getStatus(int index) {
        Xlog.v(TAG, "Enter getStatus: " + index);
        try {
            ShellExe.execCommand(new String[] { "sh", "-c",
                    "cat " + FILES[index % 10] });
            Message m = new Message();
            m.what = index;
            m.obj = ShellExe.getOutput();
            mHandler.sendMessage(m);
        } catch (IOException e) {
            Xlog.d(TAG, e.getMessage());
        }
    }

    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if (arg0.isEnabled()) {
            if (mCbDebugMsgEnable == arg0) {
                Xlog.v(TAG, "CheckBox is checked: " + arg1);
                handleEvent(arg1 ? INDEX_SET_DEBUG_MODE_VALUE_E
                        : INDEX_SET_DEBUG_MODE_VALUE_D, INDEX_SET_DEBUG_MODE);
            } else {
                Xlog.v(TAG, "Unknown event");
            }
        }
    }

    public void onClick(View arg0) {
        if (mTvDebugMsgEnable == arg0) {
            Xlog.v(TAG, "TextView is clicked");
            mCbDebugMsgEnable.performClick();
        } else if (mBtnStart == arg0) {
            Xlog.v(TAG, mBtnStart.getText() + " is clicked");
            if (mBtnStart.getText().toString().equals(
                    getResources().getString(
                            R.string.hqa_cpustress_clockswitch_start))) {
                int second = 0;
                long nsecond = 0;
                try {
                    second = Integer.valueOf(mEtSecond.getText().toString());
                    nsecond = Long.valueOf(mEtNSecond.getText().toString());
                } catch (NumberFormatException nfe) {
                    Xlog.d(TAG, nfe.getMessage());
                    Toast.makeText(ClockSwitch.this, "Time period value error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                handleEvent(second + " " + nsecond, INDEX_SET_PERIOD);
                handleEvent(INDEX_SET_PERIOD_MODE_VALUE_E,
                        INDEX_SET_PERIOD_MODE);
                updateAutoTestView(true);
                if (null != mBoundService) {
                    mBoundService.startTest(null);
                }
            } else {
                handleEvent(INDEX_SET_PERIOD_MODE_VALUE_D,
                        INDEX_SET_PERIOD_MODE);
                updateAutoTestView(false);
                if (null != mBoundService) {
                    mBoundService.stopTest();
                    mBoundService.updateWakeLock();
                }
            }
        } else if (mBtnSwitchM == arg0) {
            Xlog.v(TAG, mBtnSwitchM.getText() + " is clicked");
            handleEvent(INDEX_SET_MODE_VALUE_0, INDEX_SET_MODE);
        } else if (mBtnSwitchG == arg0) {
            Xlog.v(TAG, mBtnSwitchG.getText() + " is clicked");
            handleEvent(INDEX_SET_MODE_VALUE_1, INDEX_SET_MODE);
        } else {
            Xlog.v(TAG, "Unknown event");
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Xlog.v(TAG, "Enter onServiceConnected");
        bBounded = true;
        mBoundService = ((CpuStressTestService.StressTestBinder) service)
                .getService();
        mBoundService.testObject = ClockSwitch.this;
        //this.removeDialog(DIALOG_WAIT);
    }

    public void onServiceDisconnected(ComponentName name) {
        Xlog.v(TAG, "Enter onServiceDisconnected");
        bBounded = false;
        // mBoundService.testObject = null;
        mBoundService = null;
    }

    public void onGetTestResult() {
    }

}
