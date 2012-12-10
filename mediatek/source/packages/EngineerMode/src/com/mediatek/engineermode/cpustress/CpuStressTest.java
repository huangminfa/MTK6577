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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;
import com.mediatek.engineermode.cpustress.CpuStressTestService.ICpuStressTestComplete;
import com.mediatek.engineermode.emsvr.AFMFunctionCallEx;
import com.mediatek.engineermode.emsvr.FunctionReturn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.mediatek.xlog.Xlog;

public class CpuStressTest extends Activity implements ServiceConnection,
        OnItemClickListener, RadioGroup.OnCheckedChangeListener,
        CheckBox.OnCheckedChangeListener, ICpuStressTestComplete {

    private static final String TAG = "EM/CpuStress";
    private static final int INDEX_UPDATE_RADIOBTN = 1;
    private static final int INDEX_UPDATE_RADIOGROUP = 2;
    private static final int DIALOG_WAIT = 1001;
    private static final String[] sHQACpuItems = { "AP MCU", "SW Video Codec",
            "Clock Switch" };
    private List<String> mListCpuData;
    private RadioGroup radioGroup = null;
    private RadioButton radioBtnDefault = null;
    private RadioButton radioBtnTest = null;
    private RadioButton radioBtnSingle = null;
    private RadioButton radioBtnDual = null;
    private CheckBox checkBoxThermal = null;
    private CpuStressTestService mBoundService = null;

    public static final int INDEX_DEFAULT = 0;
    public static final int INDEX_TEST = 1;
    public static final int INDEX_SINGLE = 2;
    public static final int INDEX_DUAL = 3;
    public static final int TEST_BACKUP = 20;
    public static final int TEST_BACKUP_TEST = 21;
    public static final int TEST_BACKUP_SINGLE = 22;
    public static final int TEST_BACKUP_DUAL = 23;
    public static final int TEST_RESTORE = 24;
    public static final int TEST_RESTORE_TEST = 25;
    public static final int TEST_RESTORE_SINGLE = 26;
    public static final int TEST_RESTORE_DUAL = 27;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Xlog.v(TAG, "mHandler receive message: " + msg.what);
            CpuStressTestService.bThermal = new File("/etc/.tp/.ht120.mtc")
                    .exists();
            switch (msg.what) {
                case INDEX_UPDATE_RADIOBTN:
                    if (!CpuStressTestService.bDualCore.booleanValue()) {
                        radioBtnDual.setVisibility(View.GONE);
                        radioBtnSingle.setVisibility(View.GONE);
                        checkBoxThermal.setVisibility(View.GONE);
                        switch (CpuStressTestService.sIndexMode) {
                            case INDEX_DEFAULT:
                                radioBtnDefault.setEnabled(false);
                                radioBtnDefault.setChecked(true);
                                radioBtnDefault.setEnabled(true);
                                break;
                            case INDEX_TEST:
                                radioBtnTest.setEnabled(false);
                                radioBtnTest.setChecked(true);
                                radioBtnTest.setEnabled(true);
                                break;
                            default:
                                break;
                        }
                    } else {
                        checkBoxThermal.setEnabled(false);
                        checkBoxThermal
                                .setChecked(CpuStressTestService.bThermalDisable);
                        checkBoxThermal
                                .setEnabled(CpuStressTestService.bThermal);
                        switch (CpuStressTestService.sIndexMode) {
                            case INDEX_DEFAULT:
                                radioBtnDefault.setEnabled(false);
                                radioBtnDefault.setChecked(true);
                                radioBtnDefault.setEnabled(true);
                                break;
                            case INDEX_TEST:
                                radioBtnTest.setEnabled(false);
                                radioBtnTest.setChecked(true);
                                radioBtnTest.setEnabled(true);
                                break;
                            case INDEX_SINGLE:
                                radioBtnSingle.setEnabled(false);
                                radioBtnSingle.setChecked(true);
                                radioBtnSingle.setEnabled(true);
                                break;
                            case INDEX_DUAL:
                                radioBtnDual.setEnabled(false);
                                radioBtnDual.setChecked(true);
                                radioBtnDual.setEnabled(true);
                                break;
                            default:
                                break;
                        }
                    }
                    updateRadioGroup(!mBoundService.isTestRun());
                    break;
                case INDEX_UPDATE_RADIOGROUP:
                    updateRadioGroup(!mBoundService.isTestRun());
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
        setContentView(R.layout.hqa_cpustress);

        ListView cpuMainListView = (ListView) this
                .findViewById(R.id.listview_hqa_cpu_main);
        radioGroup = (RadioGroup) this
                .findViewById(R.id.hqa_cpu_main_radiogroup);
        radioBtnDefault = (RadioButton) this
                .findViewById(R.id.hqa_cpu_main_raidobutton_default);
        radioBtnTest = (RadioButton) this
                .findViewById(R.id.hqa_cpu_main_raidobutton_test);
        radioBtnSingle = (RadioButton) this
                .findViewById(R.id.hqa_cpu_main_raidobutton_single);
        radioBtnDual = (RadioButton) this
                .findViewById(R.id.hqa_cpu_main_raidobutton_dual);
        checkBoxThermal = (CheckBox) this
                .findViewById(R.id.hqa_cpu_main_checkbox);
        if (cpuMainListView == null || radioGroup == null
                || radioBtnDefault == null || radioBtnTest == null
                || radioBtnSingle == null || radioBtnDual == null
                || checkBoxThermal == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Internal Error");
            builder.setMessage("Can not find \"cpuMainListView\" in XML.");
            builder.setPositiveButton("OK", null);
            builder.create().show();
            finish();
            return;
        }
        mListCpuData = filterData();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListCpuData);
        cpuMainListView.setAdapter(adapter);
        if (!Build.TYPE.equals("eng")) {
            Toast.makeText(this, "Load not support", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // set listener
        cpuMainListView.setOnItemClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        checkBoxThermal.setOnCheckedChangeListener(this);
        updateRadioGroup(false);
        this.startService(new Intent(this, CpuStressTestService.class));
        Xlog.i(TAG, "In internal CpuStressTest");
    }

    protected void updateRadioGroup(boolean b) {
        radioBtnDefault.setEnabled(b);
        radioBtnTest.setEnabled(b);
        radioBtnSingle.setEnabled(b);
        radioBtnDual.setEnabled(b);
        checkBoxThermal.setEnabled(b);
        if (b && (!CpuStressTestService.bThermal)) {
            checkBoxThermal.setEnabled(false);
        }
        this.removeDialog(DIALOG_WAIT);
    }

    @Override
    protected void onStart() {
        Xlog.v(TAG, "Enter onStart");
        super.onStart();
        showDialog(DIALOG_WAIT);
        this.bindService(new Intent(CpuStressTest.this,
                CpuStressTestService.class), this, Context.BIND_AUTO_CREATE);
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
            ProgressDialog dialog = new ProgressDialog(CpuStressTest.this);
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

    @Override
    protected void onDestroy() {
        Xlog.v(TAG, "Enter onDestroy");
        super.onDestroy();
    }

    private List<String> filterData() {
        mListCpuData = new ArrayList<String>();
        for (String eachItem : sHQACpuItems) {
            mListCpuData.add(eachItem);
        }
        return mListCpuData;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent();
        if (0 == CpuStressTestService.sIndexMode) {
            Toast.makeText(this, "Please select mode", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            Xlog.i(TAG, "In CpuStressTest view, user pressed : "
                    + mListCpuData.get(arg2));
            if (mListCpuData.get(arg2).equals("AP MCU")) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cpustress.ApMcu");
            } else if (mListCpuData.get(arg2).equals("SW Video Codec")) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cpustress.SwVideoCodec");
            } else if (mListCpuData.get(arg2).equals("Clock Switch")) {
                intent.setClassName(this,
                        "com.mediatek.engineermode.cpustress.ClockSwitch");
                if (2 > CpuStressTestService.sIndexMode
                        && CpuStressTestService.bDualCore.booleanValue()) {
                    Toast.makeText(this, "Not force test mode",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            this.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorStr = "Can not find packages about \""
                    + mListCpuData.get(arg2)
                    + "\". Please report to RDs, thanks.";
            Xlog.e(TAG, errorStr);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage(errorStr);
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Xlog.v(TAG, "Enter onCheckedChanged: " + checkedId);
        if (checkedId == radioBtnDefault.getId()) {
            mBoundService.setIndexMode(INDEX_DEFAULT);
        } else if (checkedId == radioBtnTest.getId()) {
            mBoundService.setIndexMode(INDEX_TEST);
        } else if (checkedId == radioBtnSingle.getId()) {
            mBoundService.setIndexMode(INDEX_SINGLE);
        } else if (checkedId == radioBtnDual.getId()) {
            mBoundService.setIndexMode(INDEX_DUAL);
        } else {
            Xlog.d(TAG, "Unknown checked Id");
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isEnabled()) {
            if (buttonView == checkBoxThermal) {
                Xlog.v(TAG, "check box checked: " + isChecked);
                doThermalDisable(isChecked);
            } else {
                Xlog.v(TAG, "Unknown checkbox");
            }
        }
    }

    private void doThermalDisable(boolean isChecked) {
        Xlog.v(TAG, "Enter doThermalDisable: " + isChecked);
        CpuStressTestService.bThermalDisable = isChecked;
        String response = "";
        AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A
                .StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_CPU_STRESS_TEST_THERMAL);
        A.WriteParamNo(1);
        A.WriteParamInt(isChecked ? 0 : 1);
        if (!result) {
            Xlog.d(TAG, "AFMFunctionCallEx return false");
            response = "ERROR";
        } else {
            FunctionReturn r;
            do {
                r = A.GetNextResult();
                if (r.returnString == "") {
                    break;
                }
                response += r.returnString;
            } while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                Xlog.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                response = "ERROR";
            }
        }
        Xlog.v(TAG, "doThermalDisable response: " + response);
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Xlog.v(TAG, "Enter onServiceConnected");
        mBoundService = ((CpuStressTestService.StressTestBinder) service)
                .getService();
        mBoundService.testObject = CpuStressTest.this;
        mHandler.sendEmptyMessage(INDEX_UPDATE_RADIOBTN);
        //this.removeDialog(DIALOG_WAIT);
    }

    public void onServiceDisconnected(ComponentName name) {
        Xlog.v(TAG, "Enter onServiceDisconnected");
        // mBoundService.testObject = null;
        mBoundService = null;
    }

    public void onGetTestResult() {
        mHandler.sendEmptyMessage(INDEX_UPDATE_RADIOGROUP);
    }

}
