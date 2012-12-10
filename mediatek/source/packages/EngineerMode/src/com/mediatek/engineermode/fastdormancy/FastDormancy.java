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

package com.mediatek.engineermode.fastdormancy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncResult;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.featureoption.FeatureOption;

public class FastDormancy extends Activity {

    private static final String TAG = "EM_FD";
    private RadioGroup radioGroup;
    private Button okButton;
    private String[] returnData = new String[2];
    private static final String queryCmd[] = { "AT+EPCT?", "+EPCT" };
    private Phone phone = null;

    private static final int EVENT_FD_QUERY = 0;
    private static final int EVENT_FD_SET = 1;

    private static final int FD_ON = 0x7FFFFF;
    private static final int FD_OFF = 0x800000;

    private int value = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fastdormancy);
        radioGroup = (RadioGroup) findViewById(R.id.fd_radio_group);
        okButton = (Button) findViewById(R.id.fd_set_button);
        okButton.setOnClickListener(new ButtonClickListener());
        init();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (null != returnData && null != returnData[1]
                && returnData[1].equals("0")) {
            radioGroup.check(R.id.fd_off_radio);
        } else if (null != returnData && null != returnData[1]
                && returnData[1].equals("1")) {
            radioGroup.check(R.id.fd_on_radio);
        } else {
            Elog.w(TAG, "returnData is null ");
        }
    }

    private void init() {

        phone = PhoneFactory.getDefaultPhone();
        phone.invokeOemRilRequestStrings(queryCmd, mResponseHander
                .obtainMessage(EVENT_FD_QUERY));
    }

    private String[] parseData(String[] content) {
        Elog.i(TAG, "parseData() content[0]: " + content[0]);
        if (content[0].startsWith("+EPCT:")) {
            content = content[0].substring(6).split(",");
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m0 = p.matcher(content[0]);
            Matcher m1 = p.matcher(content[1]);
            content[0] = m0.replaceAll("");
            content[1] = m1.replaceAll("");
            return content;
        }
        return content;
    }

    public class ButtonClickListener implements View.OnClickListener {

        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v.getId() == R.id.fd_set_button) {
                int checkedId = radioGroup.getCheckedRadioButtonId();
                String cmdStr[] = new String[2];
                if (checkedId == R.id.fd_on_radio) {
                    if (null != returnData && null != returnData[0] && value != -1) {
                        int mToModem = value & FD_ON;
                        Elog.i(TAG, "To Modem :" + mToModem);
                        cmdStr[0] = "AT+EPCT=" + returnData[0] + "," + mToModem;
                        cmdStr[1] = "";
                        phone.invokeOemRilRequestStrings(cmdStr,
                                mResponseHander.obtainMessage(EVENT_FD_SET));
                        Elog.i(TAG, "invoke cmdStr :" + cmdStr[0]);
                    } else {
                        Toast.makeText(FastDormancy.this, "Get FD data fail!",
                                Toast.LENGTH_SHORT).show();
                        Elog.w(TAG, "returnData is null");
                        finish();
                    }
                } else if (checkedId == R.id.fd_off_radio) {
                    if (null != returnData && null != returnData[0] && value != -1) {
                        int mToModem = value | FD_OFF;
                        Elog.i(TAG, "To Modem :" + mToModem);
                        cmdStr[0] = "AT+EPCT=" + returnData[0] + "," + mToModem;
                        cmdStr[1] = "";
                        phone.invokeOemRilRequestStrings(cmdStr,
                                mResponseHander.obtainMessage(EVENT_FD_SET));
                        Elog.i(TAG, "invoke cmdStr :" + cmdStr[0]);
                    } else {
                        Toast.makeText(FastDormancy.this, "Get FD data fail!",
                                Toast.LENGTH_SHORT).show();
                        Elog.w(TAG, "returnData is null");
                        finish();
                    }
                }
            }
        }
    }

    private Handler mResponseHander = new Handler() {
        public void handleMessage(Message msg) {
            Elog.i(TAG, "Receive msg from modem");
            AsyncResult ar;
            switch (msg.what) {
            case EVENT_FD_QUERY:
                Elog.i(TAG, "Receive EVENT_FD_QUERY_SIM1:");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    returnData = (String[]) ar.result;
                    returnData = parseData(returnData);
                    if (null != returnData && returnData.length > 0) {
                        for (int i = 0; i < returnData.length; i++) {
                            Elog.i(TAG, "returnData[" + i + "]: "
                                    + returnData[i] + "\n");
                        }
                        if (returnData.length != 0 && null != returnData[0]) {
                            if(returnData.length == 1) {
                                value = 0;
                            } else {
                                value = Integer.parseInt(returnData[1]); 
                            }                            
                            boolean isOff = ((value & 0x800000) == 0x800000) ? true : false;
                            Elog.i(TAG, "value = " + value);
                            if (isOff) {                               
                                radioGroup.check(R.id.fd_off_radio);
                                Elog.i(TAG, "check off");
                            } else {                                
                                radioGroup.check(R.id.fd_on_radio);
                                Elog.i(TAG, "check on");
                            }
                        }
                    } else {
                        Elog.i(TAG, "Received data is null");
                    }
                } else {
                    Elog.i(TAG, "Receive EVENT_FD_QUERY: exception" + ar.exception);
                }
                break;
            case EVENT_FD_SET:
                Elog.i(TAG, "Receive EVENT_FD_SET:");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
//                    new AlertDialog.Builder(FastDormancy.this).setTitle(
//                            "SCRI/FD Set")
//                            .setMessage("please reboot the phone")
//                            .setPositiveButton("OK", null).create().show();
                    Toast.makeText(FastDormancy.this,
                            "success!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            FastDormancy.this);
                    builder.setTitle("SCRI/FD Set");
                    builder.setMessage("SCRI/FD Set failed.");
                    builder.setPositiveButton("OK", null);
                    builder.create().show();
                }
                break;
            default:
                break;
            }
        }
    };
}
