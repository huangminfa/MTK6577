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

package com.mediatek.engineermode.cfu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import android.os.AsyncResult;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.fastdormancy.FastDormancy;

public class CfuActivity extends Activity {

    private static final String TAG = "CFU";
    
    private RadioButton mRadioButton_de;
    private RadioButton mRadioButton_on;
    private RadioButton mRadioButton_off;
    private Button mButton_ok;
    
    private Phone mPhone;
    private static final int QUERY = 1;
    private static final int SET_DEFAULT = 2;
    private static final int SET_ON = 3;
    private static final int SET_OFF = 4;
    
    private static final int CHECKBUTTONERROR = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.cfu_activity);
        init();
        
        mButton_ok.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mRadioButton_de.isChecked()) {
                    mPhone.invokeOemRilRequestStrings(createCmd(SET_DEFAULT),
                            mResponseHander.obtainMessage(SET_DEFAULT));
                    Elog.i(TAG, "Set Query CFU Status : default");

                } else if (mRadioButton_on.isChecked()) {
                    mPhone.invokeOemRilRequestStrings(createCmd(SET_ON),
                            mResponseHander.obtainMessage(SET_ON));
                    Elog.i(TAG, "Set Query CFU Status : on");

                } else if (mRadioButton_off.isChecked()) {
                    mPhone.invokeOemRilRequestStrings(createCmd(SET_OFF),
                            mResponseHander.obtainMessage(SET_OFF));
                    Elog.i(TAG, "Set Query CFU Status : off");

                } else {
                    createDialog(CHECKBUTTONERROR);
                    Elog.e(TAG, "Set Query CFU Status : off");
                }
            }
        });

    }
    
    private void init() {
        mRadioButton_de = (RadioButton) findViewById(R.id.cfu_default_radio);
        mRadioButton_on = (RadioButton) findViewById(R.id.cfu_on_radio);
        mRadioButton_off = (RadioButton) findViewById(R.id.cfu_off_radio);
        mButton_ok = (Button) findViewById(R.id.cfu_set_button);

        mPhone = PhoneFactory.getDefaultPhone();
        mPhone.invokeOemRilRequestStrings(createCmd(QUERY), mResponseHander
                .obtainMessage(QUERY));

    }
    
    private String[] createCmd(int type) {
        String cmd[] = new String[2];
        switch (type) {
        case QUERY:
            cmd[0] = "AT+ESSP?";
            cmd[1] = "+ESSP";
            break;
        case SET_DEFAULT:
            cmd[0] = "AT+ESSP=0";
            cmd[1] = "";
            break;
        case SET_ON:
            cmd[0] = "AT+ESSP=2";
            cmd[1] = "";
            break;
        case SET_OFF:
            cmd[0] = "AT+ESSP=1";
            cmd[1] = "";
            break;
        default:
            cmd[0] = "AT+ESSP?";
            cmd[1] = "+ESSP";
            break;
        }
        Elog.d(TAG, "Send msg:" + cmd[0]);
        return cmd;
    }

    private Handler mResponseHander = new Handler() {
        public void handleMessage(Message msg) {
            Elog.i(TAG, "Receive msg from modem");
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
            case QUERY:
                if (null == ar.exception) {
                    String[] receiveDate = (String[]) ar.result;
                    
                    // For log
                    if(null != receiveDate[0]) {
                        Elog.i(TAG, "receiveDate[0] is :" + receiveDate[0]);
                    }
//                    if(null != receiveDate[1]) {
//                        Elog.i(TAG, "receiveDate[1] is :" + receiveDate[1]);
//                    }
                    
                    if (null != receiveDate) {
                        if (null != receiveDate[0] && receiveDate[0].startsWith("+ESSP:")) {
//                            receiveDate[0].substring(6).split(",");
                            receiveDate[0] = receiveDate[0].substring(6,receiveDate[0].length());
                            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                            Matcher m0 = p.matcher(receiveDate[0]);
//                            Matcher m1 = p.matcher(receiveDate[1]);
                            receiveDate[0] = m0.replaceAll("");
//                            receiveDate[1] = m1.replaceAll("");
                            if (receiveDate[0].equals("0")) {
                                mRadioButton_de.setChecked(true);
                            } else if (receiveDate[0].equals("1")) {
                                mRadioButton_off.setChecked(true);
                            } else if (receiveDate[0].equals("2")) {
                                mRadioButton_on.setChecked(true);
                            } else {
                                Toast.makeText(CfuActivity.this,
                                        "Invalid status : " + receiveDate[0],
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CfuActivity.this,
                                    "Warning: Invalid return",
                                    Toast.LENGTH_SHORT).show();                           
                        }
                    } else {
                        Toast.makeText(CfuActivity.this,
                                "Warning: Received data is null!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CfuActivity.this, "Query Failed!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case SET_DEFAULT:
                if (null == ar.exception) {
                    Toast.makeText(CfuActivity.this, "Success!",
                            Toast.LENGTH_SHORT).show();
                } else {
//                    mRadioButton_de.setChecked(false);
                    createDialog(SET_DEFAULT);
                }
                break;
            case SET_ON:
                if (null == ar.exception) {
                    Toast.makeText(CfuActivity.this, "Success!",
                            Toast.LENGTH_SHORT).show();
                } else {
//                    mRadioButton_on.setChecked(false);
                    createDialog(SET_ON);
                }
                break;
            case SET_OFF:
                if (null == ar.exception) {
                    Toast.makeText(CfuActivity.this, "Success!",
                            Toast.LENGTH_SHORT).show();
                } else {
//                    mRadioButton_off.setChecked(false);
                    createDialog(SET_OFF);
                }
                break;
            default:
                break;
            }
        }
    };
    
    private void createDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(false);
        switch (id) {
        case QUERY:
            break;
        case SET_DEFAULT:
            builder.setTitle("Set Failed!")
            .setMessage("Set Default Status Failed!")
            .setPositiveButton("OK", null)
            .create().show();
            break;
        case SET_ON:
            builder.setTitle("Set Failed!")
            .setMessage("Open Query Status Failed!")
            .setPositiveButton("OK", null)
            .create().show();
            break;
        case SET_OFF:
            builder.setTitle("Set Failed!")
            .setMessage("Close Query Status Failed!")
            .setPositiveButton("OK", null)
            .create().show();
            break;
        case CHECKBUTTONERROR:
            builder.setTitle("Warning!")
            .setMessage("Please chose a item!")
            .setPositiveButton("OK", null)
            .create().show();
            break;
        default:
            break;
        }  
    }
}
