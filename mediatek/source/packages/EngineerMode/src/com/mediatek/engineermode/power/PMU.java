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

package com.mediatek.engineermode.power;

import java.io.IOException;

import android.app.TabActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;

public class PMU extends TabActivity implements OnClickListener {

    private TextView mInfo = null;
    private Button mBtnGetRegister = null;
    private Button mBtnSetRegister = null;
    private EditText mEditAddr = null;
    private EditText mEditVal = null;

    private static final int TAB_REG = 1;
    private static final int TAB_INFO = 2;
    private int whichTab = TAB_INFO;
    private static final int EVENT_UPDATE = 1;

    private void setLayout() {

        mInfo = (TextView) findViewById(R.id.pmu_info_text);
        mBtnGetRegister = (Button) findViewById(R.id.pmu_btn_get);
        mBtnSetRegister = (Button) findViewById(R.id.pmu_btn_set);
        mEditAddr = (EditText) findViewById(R.id.pmu_edit_addr);
        mEditVal = (EditText) findViewById(R.id.pmu_edit_val);

        if (mInfo == null || mBtnGetRegister == null || mBtnSetRegister == null
                || mEditAddr == null || mEditVal == null) {
            Elog.e("PMU", "clocwork worked...");
            // not return and let exception happened.
        }

        mBtnGetRegister.setOnClickListener(this);
        mBtnSetRegister.setOnClickListener(this);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost tabHost = getTabHost();

        LayoutInflater.from(this).inflate(R.layout.power_pmu_tabs,
                tabHost.getTabContentView(), true);

        // tab1
        tabHost.addTab(tabHost.newTabSpec(
                this.getString(R.string.pmu_info_text)).setIndicator(
                this.getString(R.string.pmu_info_text)).setContent(
                R.id.LinerLayout_pmu_info_text));

        // tab2
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.pmu_reg))
                .setIndicator(this.getString(R.string.pmu_reg)).setContent(
                        R.id.LinerLayout_pmu_reg));

        setLayout();
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                String pmu_info_text = PMU.this.getString(R.string.pmu_info_text);
                String pmu_reg = PMU.this.getString(R.string.pmu_reg);
                if (tabId.equals(pmu_info_text)) {
                    onTabInfo();
                } else if (tabId.equals(pmu_reg)) {
                    onTabReg();
                }
            }

        });
        // init

    }

    private void onTabReg() {
        whichTab = TAB_REG;
    }

    private void onTabInfo() {
        whichTab = TAB_INFO;
    }

    public void onClick(View arg0) {
        // TODO Auto-generated method stub

        if (arg0.getId() == mBtnGetRegister.getId()) {
            String addr = mEditAddr.getText().toString();
            if (CheckAddr(addr)) {
                String cmd = "echo " + addr
                        + " > /sys/devices/platform/mt6573-pmu/PMU_Register";
                getInfo(cmd);
                cmd = "cat /sys/devices/platform/mt6573-pmu/PMU_Register";
                mEditVal.setText(getInfo(cmd));
            } else {
                Toast.makeText(this, "Please check address.",Toast.LENGTH_LONG).show();
            }
        } else if (arg0.getId() == mBtnSetRegister.getId()) {
            String addr = mEditAddr.getText().toString();
            String val = mEditVal.getText().toString();
            if (CheckAddr(addr) && CheckVal(val)) {
                String cmd = "echo " + addr + " " + val
                        + " > /sys/devices/platform/mt6573-pmu/PMU_Register";
                String out = getInfo(cmd);

                Toast.makeText(this, out, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please check address or value.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean CheckAddr(String s) {
        if (s == null || s.length() < 8)
            return false;

        if ((s.charAt(0) != 'F' && s.charAt(0) != 'f') || s.charAt(2) != '0') {
            return false;
        }

        try {
            Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    private boolean CheckVal(String s) {
        if (s == null || s.length() < 1)
            return false;

        try {
            Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;

    }

    private String getInfo(String cmd) {
        String result = null;
        try {
            String[] cmdx = { "/system/bin/sh", "-c", cmd }; // file must
            // exist// or
            // wait()
            // return2
            int ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                result = ShellExe.getOutput();
            } else {
                // result = "ERROR";
                result = ShellExe.getOutput();
            }

        } catch (IOException e) {
            Elog.i("EM-PMU", e.toString());
            result = "ERR.JE";
        }
        return result;
    }

    private int mUpdateInterval = 1500; // 1.5 sec
    public Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_UPDATE:
                Bundle b = msg.getData();
                mInfo.setText(b.getString("INFO"));
                break;
            }
        }
    };

    private String promptSw = "0/1=off/on";
    private String promptUnit = "mV";
    private String[][] files = { { "BUCK_VCORE_VOLTAGE", promptUnit },
            { "BUCK_VIO1V8_VOLTAGE", promptUnit },
            { "BUCK_VAPROC_VOLTAGE", promptUnit },
            { "BUCK_VRF1V8_VOLTAGE", promptUnit }, { "SEP", "" },
            { "BUCK_VCORE_STATUS", promptSw },
            { "BUCK_VIO1V8_STATUS", promptSw },
            { "BUCK_VAPROC_STATUS", promptSw },
            { "BUCK_VRF1V8_STATUS", promptSw }, { "SEP", "" },
            { "LDO_VIO_STATUS", promptSw }, { "LDO_VUSB_STATUS", promptSw },
            { "LDO_VRF_STATUS", promptSw }, { "LDO_VCAMA_STATUS", promptSw },
            { "LDO_VCAMA2_STATUS", promptSw },
            { "LDO_VCAMD_STATUS", promptSw },
            { "LDO_VCAMD2_STATUS", promptSw }, { "LDO_VSIM_STATUS", promptSw },
            { "LDO_VSIM2_STATUS", promptSw }, { "LDO_VM12_STATUS", promptSw },
            { "LDO_VM12_INT_STATUS", promptSw },
            { "LDO_VIBR_STATUS", promptSw }, { "LDO_VMC_STATUS", promptSw },
            { "SEP", "" }, { "LDO_VCAMA_VOLTAGE", promptUnit },
            { "LDO_VCAMD_VOLTAGE", promptUnit },
            { "LDO_VSIM_VOLTAGE", promptUnit },
            { "LDO_VSIM2_VOLTAGE", promptUnit },
            { "LDO_VIBR_VOLTAGE", promptUnit },
            { "LDO_VMC_VOLTAGE", promptUnit },
            { "LDO_VCAMA2_VOLTAGE", promptUnit },
            { "LDO_VCAMD2_VOLTAGE", promptUnit }, { "SEP", "" },
            { "BUCK_BOOST_EN", promptSw },
            { "BUCK_BOOST_VOLTAGE_0", promptUnit },
            { "BUCK_BOOST_VOLTAGE_1", promptUnit },
            { "BUCK_BOOST_VOLTAGE_2", promptUnit },
            { "BUCK_BOOST_VOLTAGE_3", promptUnit },
            { "BUCK_BOOST_VOLTAGE_4", promptUnit },
            { "BUCK_BOOST_VOLTAGE_5", promptUnit },
            { "BUCK_BOOST_VOLTAGE_6", promptUnit },
            { "BUCK_BOOST_VOLTAGE_7", promptUnit } };

    private boolean mRun = false;

    class runThread extends Thread {

        public void run() {
            while (mRun) {
                StringBuilder text = new StringBuilder("");
                String cmd = "";
                for (int i = 0; i < files.length; i++) {
                    if (files[i][0].equalsIgnoreCase("SEP")) {
                        text.append("- - - - - - - -\n");
                        continue;
                    }
                    cmd = "cat /sys/devices/platform/mt6573-pmu/" + files[i][0];

                    if (files[i][1].equalsIgnoreCase("mA")) {
                        text.append(String.format("%1$-28s:[%2$-6s]%3$s\n", files[i][0],
                                        Float.valueOf(getInfo(cmd)) / 10.0,
                                        files[i][1]));
                    } else {
                        text.append(String.format("%1$-28s:[%2$-6s]%3$s\n",
                                files[i][0], getInfo(cmd), files[i][1]));
                    }
                }

                Bundle b = new Bundle();
                b.putString("INFO", text.toString());

                Message msg = new Message();
                msg.what = EVENT_UPDATE;
                msg.setData(b);

                mUpdateHandler.sendMessage(msg);
                try {
                    sleep(mUpdateInterval);
                } catch (InterruptedException e) {

                }
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        mRun = false;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mRun = true;
        new runThread().start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
