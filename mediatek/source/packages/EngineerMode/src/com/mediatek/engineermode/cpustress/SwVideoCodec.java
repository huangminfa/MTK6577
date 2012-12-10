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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SwVideoCodec extends Activity implements OnClickListener,
        ServiceConnection, ICpuStressTestComplete {

    private static final String TAG = "EM/CpuStress_SwVideoCodec";
    private static final int INDEX_UPDATE_RESULT = 11;
    private static final int DIALOG_WAIT = 1011;
    public static final int SWCODEC_TEST_SINGLE = 0;
    public static final int SWCODEC_TEST_FORCE_SINGLE = 1;
    public static final int SWCODEC_TEST_FORCE_DUAL = 2;
    public static final int SWCODEC_MASK_SINGLE = 0;
    public static final int SWCODEC_MASK_FORCE_SINGLE = 1;
    public static final int SWCODEC_MASK_FORCE_DUAL_0 = 2;
    public static final int SWCODEC_MASK_FORCE_DUAL_1 = 3;
    private Button mBtnStart = null;
    private EditText mEtLoopCount = null;
    private EditText mEtIteration = null;
    private TextView mTvStatus = null;
    private TextView mTvStatus1 = null;
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
        setContentView(R.layout.hqa_cpustress_swvideo);
        mBtnStart = (Button) findViewById(R.id.swvideo_btn);
        mEtLoopCount = (EditText) findViewById(R.id.swvideo_loopcount);
        mEtIteration = (EditText) findViewById(R.id.swvideo_iteration);
        mTvStatus = (TextView) findViewById(R.id.swvideo_iteration_result);
        mTvStatus1 = (TextView) findViewById(R.id.swvideo_iteration_result_1);
        mTvResult = (TextView) findViewById(R.id.swvideo_result);
        if (null != mBtnStart && null != mEtLoopCount && null != mEtIteration
                && null != mTvStatus && null != mTvStatus1 && null != mTvResult) {
            mBtnStart.setOnClickListener(this);
            mEtIteration.setText("1");
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
        this.bindService(new Intent(SwVideoCodec.this,
                CpuStressTestService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Xlog.v(TAG, "Enter onStop");
        // mBoundService.testObject = null;
        this.unbindService(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Xlog.v(TAG, "Enter onDestroy");
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_WAIT) {
            ProgressDialog dialog = new ProgressDialog(SwVideoCodec.this);
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

    private void updateTestUI() {
        Xlog.v(TAG, "Enter updateTestUI");
        Bundle data = mBoundService.getData();
        if (null == data) {
            return;
        }
        int times = data.getInt(CpuStressTestService.RESULT_VIDEOCODEC);
        updateTestUI(data.getBoolean(CpuStressTestService.VALUE_RUN), data
                .getLong(CpuStressTestService.VALUE_LOOPCOUNT), data
                .getInt(CpuStressTestService.VALUE_ITERATION), data
                .getInt(CpuStressTestService.VALUE_RESULT), times);
        updateTestResult(times, data
                .getInt(CpuStressTestService.RESULT_PASS_VIDEOCODEC));
    }

    private void updateTestUI(boolean bRun, long iCount, int iItera,
            int result, int times) {
        Xlog.v(TAG, "updateTestUI: " + bRun + " " + iCount + " 0x"
                + Integer.toHexString(result));
        mEtLoopCount.setEnabled(!bRun);
        mEtLoopCount.setText(iCount + "");
        mEtIteration.setEnabled(!bRun);
        mEtIteration.setText(iItera + "");
        mEtLoopCount.setSelection(mEtLoopCount.getText().length());
        if (times > 0) {
            if ((result & 1 << 31) == 1 << 31) {
                switch (CpuStressTestService.sIndexMode) {
                    case CpuStressTest.INDEX_SINGLE:
                        mTvStatus
                                .setText((result & (1 << SWCODEC_MASK_FORCE_SINGLE)) == 0 ? R.string.hqa_cpustress_result_fail
                                        : R.string.hqa_cpustress_result_pass);
                        mTvStatus1.setText(R.string.hqa_cpustress_result);
                        break;
                    case CpuStressTest.INDEX_TEST:
                    case CpuStressTest.INDEX_DUAL:
                        mTvStatus
                                .setText((result & (1 << SWCODEC_MASK_FORCE_DUAL_0)) == 0 ? R.string.hqa_cpustress_result_fail
                                        : R.string.hqa_cpustress_result_pass);
                        mTvStatus1
                                .setText((result & (1 << SWCODEC_MASK_FORCE_DUAL_1)) == 0 ? R.string.hqa_cpustress_result_fail
                                        : R.string.hqa_cpustress_result_pass);
                        break;
                    default:
                        break;
                }
            } else {
                mTvStatus
                        .setText((result & (1 << SWCODEC_MASK_SINGLE)) == 0 ? R.string.hqa_cpustress_result_fail
                                : R.string.hqa_cpustress_result_pass);
                mTvStatus1.setText(R.string.hqa_cpustress_result);
            }
        }
        mBtnStart.setText(bRun ? R.string.hqa_cpustress_swvideo_stop
                : R.string.hqa_cpustress_swvideo_start);
        if (!bRun) {
            mTvStatus.setText(R.string.hqa_cpustress_result);
            mTvStatus1.setText(R.string.hqa_cpustress_result);
        }
        if (!CpuStressTestService.bWantStopSwCodec) {
            removeDialog(DIALOG_WAIT);
        }
    }

    private void updateTestResult(int int1, int int2) {
        Xlog.v(TAG, "Enter updateTestResult: " + int1 + " " + int2);
        StringBuffer sb = new StringBuffer();
        if (0 != int1) {
            sb.append(" Test Pass: ");
            sb.append(int2);
            sb.append("/");
            sb.append(int1);
            sb.append(String.format(" %3.2f%%", int2 * 100.0 / int1));
        }
        mTvResult.setText(sb.toString());
    }

    public void onClick(View arg0) {
        if (mBtnStart == arg0) {
            Xlog.v(TAG, mBtnStart.getText() + " is clicked");
            if (getResources().getString(R.string.hqa_cpustress_swvideo_start) == mBtnStart
                    .getText()) {
                Intent intent = new Intent();
                long count = 0;
                int iteration = 0;
                try {
                    count = Long.valueOf(mEtLoopCount.getText().toString());
                    iteration = Integer.valueOf(mEtIteration.getText()
                            .toString());
                } catch (NumberFormatException nfe) {
                    Xlog.d(TAG, nfe.getMessage());
                    Toast.makeText(SwVideoCodec.this,
                            "loop count or iteration value error",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle data = new Bundle();
                data.putLong(CpuStressTestService.VALUE_LOOPCOUNT, count);
                data.putInt(CpuStressTestService.VALUE_ITERATION, iteration);
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
        mTvStatus.setText(R.string.hqa_cpustress_result);
        mEtLoopCount.setEnabled(false);
        mEtIteration.setEnabled(false);
        mTvStatus.setText(R.string.hqa_cpustress_result);
        mBtnStart.setText(R.string.hqa_cpustress_swvideo_stop);
        mTvResult.setText(R.string.hqa_cpustress_result);
    }

    public void onGetTestResult() {
        mHandler.sendEmptyMessage(INDEX_UPDATE_RESULT);
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Xlog.v(TAG, "Enter onServiceConnected");
        mBoundService = ((CpuStressTestService.StressTestBinder) service)
                .getService();
        mBoundService.testObject = this;
        // updateTestUI();
        mHandler.sendEmptyMessage(INDEX_UPDATE_RESULT);
        //this.removeDialog(DIALOG_WAIT);
    }

    public void onServiceDisconnected(ComponentName name) {
        Xlog.v(TAG, "Enter onServiceDisconnected");
        // mBoundService.testObject = null;
        mBoundService = null;
    }
}
