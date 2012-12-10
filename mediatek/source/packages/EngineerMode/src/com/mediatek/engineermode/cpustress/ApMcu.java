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

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.cpustress.CpuStressTestService.ICpuStressTestComplete;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class ApMcu extends Activity implements ServiceConnection,
        OnClickListener, OnCheckedChangeListener, ICpuStressTestComplete {

    private static final String TAG = "EM/CpuStress_ApMcu";
    private static final int INDEX_UPDATE_RESULT = 1;
    private static final int DIALOG_WAIT = 1001;
    public static final int MASK_L2C = 0;
    public static final int MASK_NEON = 1;
    public static final int MASK_NEON_0 = 2;
    public static final int MASK_NEON_1 = 3;
    public static final int MASK_CA9 = 4;
    public static final int MASK_CA9_0 = 5;
    public static final int MASK_CA9_1 = 6;
    private EditText mEtLoopCount = null;
    private CheckBox mCbL2C = null;
    private CheckBox mCbNeon = null;
    private CheckBox mCbCA9 = null;
    private TextView mTvResultL2C = null;
    private TextView mTvResultNeon = null;
    private TextView mTvResultCA9 = null;
    private TextView mTvResultNeon1 = null;
    private TextView mTvResultCA91 = null;
    private Button mBtnStart = null;
    private TextView mTvResult = null;
    private CpuStressTestService mBoundService = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Xlog.v(TAG, "mHandler receive message: " + msg.what);
            switch (msg.what) {
                case INDEX_UPDATE_RESULT:
                    updateTestUI();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Xlog.v(TAG, "Enter onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hqa_cpustress_apmcu);
        mEtLoopCount = (EditText) findViewById(R.id.apmcu_loopcount);
        mCbL2C = (CheckBox) findViewById(R.id.apmcu_l2c_test);
        mCbNeon = (CheckBox) findViewById(R.id.apmcu_neon_test);
        mCbCA9 = (CheckBox) findViewById(R.id.apmcu_ca9_test);
        mTvResultL2C = (TextView) findViewById(R.id.apmcu_l2c_result);
        mTvResultNeon = (TextView) findViewById(R.id.apmcu_neon_result);
        mTvResultCA9 = (TextView) findViewById(R.id.apmcu_ca9_result);
        mTvResultNeon1 = (TextView) findViewById(R.id.apmcu_neon_result_1);
        mTvResultCA91 = (TextView) findViewById(R.id.apmcu_ca9_result_1);
        mBtnStart = (Button) findViewById(R.id.apmcu_btn);
        mTvResult = (TextView) findViewById(R.id.apmcu_result);
        if (null != mEtLoopCount && null != mCbL2C && null != mCbNeon
                && null != mCbCA9 && null != mTvResultL2C
                && null != mTvResultNeon && null != mTvResultCA9
                && null != mBtnStart && null != mTvResultNeon1
                && null != mTvResultCA91 && null != mTvResult) {
            mCbL2C.setOnCheckedChangeListener(this);
            mCbNeon.setOnCheckedChangeListener(this);
            mCbCA9.setOnCheckedChangeListener(this);
            mBtnStart.setOnClickListener(this);
            // mCbL2C.setVisibility(View.GONE);
            // mTvResultL2C.setVisibility(View.GONE);
        } else {
            Toast
                    .makeText(this, "Get view is null, return",
                            Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        Xlog.v(TAG, "Enter onStart");
        super.onStart();
        showDialog(DIALOG_WAIT);
        this.bindService(new Intent(ApMcu.this, CpuStressTestService.class),
                this, Context.BIND_AUTO_CREATE);
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
            ProgressDialog dialog = new ProgressDialog(ApMcu.this);
            if (dialog != null) {
                dialog.setTitle("Waiting");
                dialog.setMessage("Please wait ...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
                Xlog.v(TAG, "create ProgressDialog");
            }
            return dialog;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        Xlog.v(TAG, "Enter onDestroy");
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void updateTestUI() {
        Xlog.v(TAG, "Enter updateTestUI()");
        try {
            Bundle data = mBoundService.getData();
            if (null == data) {
                return;
            }
            int times_L2C = data.getInt(CpuStressTestService.RESULT_L2C);
            int times_NEON = data.getInt(CpuStressTestService.RESULT_NEON);
            int times_CA9 = data.getInt(CpuStressTestService.RESULT_CA9);
            updateTestUI(data.getBoolean(CpuStressTestService.VALUE_RUN), data
                    .getLong(CpuStressTestService.VALUE_LOOPCOUNT), data
                    .getInt(CpuStressTestService.VALUE_MASK), data
                    .getInt(CpuStressTestService.VALUE_RESULT), times_L2C,
                    times_NEON, times_CA9);
            updateTestResult(times_L2C, data
                    .getInt(CpuStressTestService.RESULT_PASS_L2C), times_NEON,
                    data.getInt(CpuStressTestService.RESULT_PASS_NEON),
                    times_CA9, data
                            .getInt(CpuStressTestService.RESULT_PASS_CA9));
        } catch (NullPointerException e) {
            Xlog.w(TAG, "updateTestUI NullPointerException: " + e.getMessage());
        }
    }

    private void updateTestUI(boolean bRun, long iCount, int mask, int result,
            int timesL, int timesN, int timesC) {
        Xlog.v(TAG, "updateTestUI: " + bRun + " " + iCount + " " + mask + " 0x"
                + Integer.toHexString(result));
        mEtLoopCount.setEnabled(!bRun);
        mEtLoopCount.setText(iCount + "");
        mEtLoopCount.setSelection(mEtLoopCount.getText().length());
        mBtnStart.setText(bRun ? R.string.hqa_cpustress_apmcu_stop
                : R.string.hqa_cpustress_apmcu_start);
        mCbL2C.setEnabled(false);
        mCbNeon.setEnabled(false);
        mCbCA9.setEnabled(false);
        mCbL2C.setChecked(0 != (mask & (1 << MASK_L2C)));
        mCbNeon.setChecked(0 != (mask & (0x7 << MASK_NEON)));
        mCbCA9.setChecked(0 != (mask & (0x7 << MASK_CA9)));
        mCbL2C.setEnabled(true);
        mCbNeon.setEnabled(true);
        mCbCA9.setEnabled(true);
        if (timesL > 0 || timesN > 0 || timesC > 0) {
            mTvResultL2C
                    .setText(0 != (result & 1 << MASK_L2C) ? R.string.hqa_cpustress_result_pass
                            : R.string.hqa_cpustress_result_fail);
            if ((result & 1 << 31) == 1 << 31) {
                switch (CpuStressTestService.sIndexMode) {
                    case CpuStressTest.INDEX_SINGLE:
                        mTvResultNeon
                                .setText(0 != (result & 1 << MASK_NEON) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        mTvResultCA9
                                .setText(0 != (result & 1 << MASK_CA9) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        break;
                    case CpuStressTest.INDEX_TEST:
                    case CpuStressTest.INDEX_DUAL:
                        mTvResultNeon
                                .setText(0 != (result & 1 << MASK_NEON_0) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        mTvResultCA9
                                .setText(0 != (result & 1 << MASK_CA9_0) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        mTvResultNeon1
                                .setText(0 != (result & 1 << MASK_NEON_1) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        mTvResultCA91
                                .setText(0 != (result & 1 << MASK_CA9_1) ? R.string.hqa_cpustress_result_pass
                                        : R.string.hqa_cpustress_result_fail);
                        break;
                    default:
                        break;
                }
            } else {
                mTvResultNeon
                        .setText(0 != (result & 1 << MASK_NEON) ? R.string.hqa_cpustress_result_pass
                                : R.string.hqa_cpustress_result_fail);
                mTvResultCA9
                        .setText(0 != (result & 1 << MASK_CA9) ? R.string.hqa_cpustress_result_pass
                                : R.string.hqa_cpustress_result_fail);
            }
        }
        if (!bRun || !mCbL2C.isChecked()) {
            mTvResultL2C.setText(R.string.hqa_cpustress_result);
        }
        if (!bRun || !mCbNeon.isChecked()) {
            mTvResultNeon.setText(R.string.hqa_cpustress_result);
            mTvResultNeon1.setText(R.string.hqa_cpustress_result);
        }
        if (!bRun || !mCbCA9.isChecked()) {
            mTvResultCA9.setText(R.string.hqa_cpustress_result);
            mTvResultCA91.setText(R.string.hqa_cpustress_result);
        }
        if (!CpuStressTestService.bWantStopApmcu) {
            removeDialog(DIALOG_WAIT);
        }
    }

    private void updateTestResult(int int1, int int2, int int3, int int4,
            int int5, int int6) {
        Xlog.v(TAG, String.format(
                "Enter updateTestResult: %1$d, %1$d, %1$d, %1$d, %1$d, %1$d",
                int1, int2, int3, int4, int5, int6));
        StringBuffer sb = new StringBuffer();
        // if (int1 + int3 + int5 != 0) {
        // sb.append("Pass:");
        // }
        if (0 != int1) {
            sb.append(" L2C ");
            sb.append(int2);
            sb.append("/");
            sb.append(int1);
            sb.append(String.format(" %3.2f%%", int2 * 100.0 / int1));
        }
        if (0 != int3) {
            sb.append(" NEON ");
            sb.append(int4);
            sb.append("/");
            sb.append(int3);
            sb.append(String.format(" %3.2f%%", int4 * 100.0 / int3));
        }
        if (0 != int5) {
            sb.append(" MaxPower ");
            sb.append(int6);
            sb.append("/");
            sb.append(int5);
            sb.append(String.format(" %3.2f%%", int6 * 100.0 / int5));
        }
        mTvResult.setText(sb.toString());
    }

    public void onClick(View arg0) {
        if (mBtnStart == arg0) {
            Xlog.v(TAG, mBtnStart.getText() + " is clicked");
            if (getResources().getString(R.string.hqa_cpustress_apmcu_start) == mBtnStart
                    .getText()) {
                long count = 0;
                try {
                    count = Long.valueOf(mEtLoopCount.getText().toString());
                } catch (NumberFormatException nfe) {
                    Xlog.d(TAG, nfe.getMessage());
                    Toast.makeText(ApMcu.this, "loop count error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle data = new Bundle();
                data.putLong(CpuStressTestService.VALUE_LOOPCOUNT, count);
                data.putInt(CpuStressTestService.VALUE_MASK, (mCbL2C
                        .isChecked() ? 1 << MASK_L2C : 0)
                        | (mCbNeon.isChecked() ? 7 << MASK_NEON : 0)
                        | (mCbCA9.isChecked() ? 7 << MASK_CA9 : 0));
                mBoundService.startTest(data);
                updateStartUI();
            } else {
                showDialog(DIALOG_WAIT);
                mBoundService.stopTest();
            }
        } else {
            Xlog.v(TAG, "Unknown event");
        }
    }

    private void updateStartUI() {
        mEtLoopCount.setEnabled(false);
        mBtnStart.setText(R.string.hqa_cpustress_apmcu_stop);
        mTvResultL2C.setText(R.string.hqa_cpustress_result);
        mTvResultNeon.setText(R.string.hqa_cpustress_result);
        mTvResultNeon1.setText(R.string.hqa_cpustress_result);
        mTvResultCA9.setText(R.string.hqa_cpustress_result);
        mTvResultCA91.setText(R.string.hqa_cpustress_result);
        mTvResult.setText(R.string.hqa_cpustress_result);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isEnabled()) {
            int mask = (mCbL2C.isChecked() ? 1 << MASK_L2C : 0)
                    | (mCbNeon.isChecked() ? 7 << MASK_NEON : 0)
                    | (mCbCA9.isChecked() ? 7 << MASK_CA9 : 0);
            Xlog.v(TAG, "onCheckChanged, mask: 0x" + Integer.toHexString(mask));
            Bundle data = new Bundle();
            data.putInt(CpuStressTestService.VALUE_MASK, mask);
            mBoundService.updateData(data);
        }
    }

    public void onGetTestResult() {
        mHandler.sendEmptyMessage(INDEX_UPDATE_RESULT);
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Xlog.v(TAG, "onServiceConnected");
        mBoundService = ((CpuStressTestService.StressTestBinder) service)
                .getService();
        mBoundService.testObject = ApMcu.this;
        // updateTestUI();
        mHandler.sendEmptyMessage(INDEX_UPDATE_RESULT);
        //this.removeDialog(DIALOG_WAIT);
    }

    public void onServiceDisconnected(ComponentName name) {
        Xlog.v(TAG, "onServiceDisconnected");
        // mBoundService.testObject = null;
        mBoundService = null;
    }
}
