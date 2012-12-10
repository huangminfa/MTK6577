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

package com.mediatek.engineermode.modemtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.AsyncResult;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class ModemTestActivity extends Activity {

    public static final String TAG = "ModemTest";

    private Phone phone = null;
    private static final int EVENT_MODEM_NONE = 0;
    private static final int EVENT_MODEM_CTA = 1;
    private static final int EVENT_MODEM_FTA = 2;
    private static final int EVENT_MODEM_IOT = 3;
    private static final int EVENT_MODEM_QUERY = 4;
    private static final int EVENT_MODEM_OPERATOR = 5;
    private static final int EVENT_QUERY_PREFERRED_TYPE_DONE = 1000;
    private static final int EVENT_SET_PREFERRED_TYPE_DONE = 1001;
    private static final int CTA_DIALOG = 1;
    private static final int REBOOT_DIALOG = 2;
    private static final int IOT_DIALOG = 3;
    private static final int OPERATOR_DIALOG = 5;

    private int mCtaOption = 0;
    private int mIotOption = 0;
    private int mOperatorOption = 0;

    private Button mNoneBtn;
    private Button mCtaBtn;
    private Button mFtaBtn;
    private Button mIotBtn;
    private Button mOperatorBtn;
    private TextView textView;
    private boolean modemFlag = false;
    private String[] mCtaOptionsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.modem_test_activity);

        View.OnClickListener listener = new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (v.getId()) {
                case R.id.modem_test_none_btn:
                    sendATCommad("0", EVENT_MODEM_NONE);
                    break;
                case R.id.modem_test_cta_btn:
                	if(modemFlag){
                		 writePreferred(3);
                		phone.setPreferredNetworkType(3, mATCmdHander.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                	}
                    ModemTestActivity.this.showDialog(CTA_DIALOG);
                    break;
                case R.id.modem_test_fta_btn:
                	if(modemFlag){
                		 writePreferred(3);
                		phone.setPreferredNetworkType(3, mATCmdHander.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                	}
                    sendATCommad("2", EVENT_MODEM_FTA);
                    break;
                case R.id.modem_test_iot_btn:
                	if(modemFlag){
                		 writePreferred(3);
                		phone.setPreferredNetworkType(3, mATCmdHander.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                	}
                    sendATCommad("3", EVENT_MODEM_IOT);
                    // ModemTestActivity.this.showDialog(IOT_DIALOG);
                    break;
                case R.id.modem_test_operator_btn:
                	if(modemFlag){
                		 writePreferred(3);
                		phone.setPreferredNetworkType(3, mATCmdHander.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE));
                	}
                    ModemTestActivity.this.showDialog(OPERATOR_DIALOG);
                    break;                    
                }
            }
        };
        textView = (TextView) findViewById(R.id.modem_test_textview);
        mNoneBtn = (Button) findViewById(R.id.modem_test_none_btn);
        mCtaBtn = (Button) findViewById(R.id.modem_test_cta_btn);
        mFtaBtn = (Button) findViewById(R.id.modem_test_fta_btn);
        mIotBtn = (Button) findViewById(R.id.modem_test_iot_btn);
        mOperatorBtn = (Button) findViewById(R.id.modem_test_operator_btn);

        mNoneBtn.setOnClickListener(listener);
        mCtaBtn.setOnClickListener(listener);
        mFtaBtn.setOnClickListener(listener);
        mIotBtn.setOnClickListener(listener);
        mOperatorBtn.setOnClickListener(listener);

        textView.setText("The current mode is unknown");

        mCtaOptionsArray = this.getResources().getStringArray(R.array.modem_test_cta_options);
        // send AT Cmd and register the event
        phone = PhoneFactory.getDefaultPhone();
        // phone.registerForNetworkInfo(mResponseHander, EVENT_NW_INFO, null);

        String ATCmd[] = new String[2];
        ATCmd[0] = "AT+EPCT?";
        ATCmd[1] = "+EPCT:";
        phone.invokeOemRilRequestStrings(ATCmd, mATCmdHander
                .obtainMessage(EVENT_MODEM_QUERY));
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mCtaOption = 0;
        checkNetworkType();
//        mOperatorOption = 0;

    }
    
    private void checkNetworkType(){
    	 Elog.i(TAG, "TcheckNetworkType");
    	
    	phone.getPreferredNetworkType(
    			 mATCmdHander.obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE));
    }

    private void sendATCommad(String str, int message) {

        String ATCmd[] = new String[2];
        ATCmd[0] = "AT+EPCT=" + str;
        ATCmd[1] = "";
        phone.invokeOemRilRequestStrings(ATCmd, mATCmdHander
                .obtainMessage(message));
    }

    private Handler mATCmdHander = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            boolean rebootFlag = false;
            switch (msg.what) {
            case EVENT_MODEM_NONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_NONE AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_NONE AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case EVENT_MODEM_CTA:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_CTA AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_CTA AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case EVENT_MODEM_FTA:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_FTA AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_FTA AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case EVENT_MODEM_IOT:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_IOT AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_IOT AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case EVENT_MODEM_QUERY:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Elog.i(TAG, "Query success.");
                    // Log.i(TAG, (String)ar.result);
                    String data[] = (String[]) ar.result;
                    if (null != data) {
                        Elog.i(TAG, "data length is " + data.length);
                    }
                    int i = 0;
                    for (String str : data) {
                        Elog.i(TAG, "data[" + i + "] is : " + str);
                        i++;
                    }
                    if (data[0].length() > 6) {
                        String mode = data[0].substring(7, data[0].length());
                        Elog.i(TAG, "mode is " + mode);
                        if (mode.length() >= 3) {
                            String subMode = mode.substring(0, 1);
                            String subCtaMode = mode.substring(2, mode.length());
                            Elog.i(TAG, "subMode is " + subMode);
                            Elog.i(TAG, "subCtaMode is " + subCtaMode);
                            if ("0".equals(subMode)) {
                                textView.setText("The current mode is none");
                            } else if ("1".equals(subMode)) {
                                textView.setText("The current mode is CTA");
                                try {
                                    int ctaLength = mCtaOptionsArray.length;
                                    Elog.i(TAG, "ctaLength is " + ctaLength);
                                    int val = Integer.valueOf(subCtaMode).intValue();
                                    Elog.i(TAG, "val is " + val);
                                    String text = "The current mode is CTA: ";
                                    for (int j = 0; j < ctaLength; j++) {
                                        Elog.i(TAG, "j ==== " + j);
                                        Elog.i(TAG, "(val & (1 << j)) is " + (val & (1 << j)));
                                        if ((val & (1 << j)) != 0) {
                                            text = text + mCtaOptionsArray[j] + ",";
                                        }
                                    }
                                    // Drop the last ","
                                    text = text.substring(0, text.length() - 1);
                                    textView.setText(text);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Elog.i(TAG,"Exception when transfer subCtaMode");
                                }
                            } else if ("2".equals(subMode)) {
                                textView.setText("The current mode is FTA.");
                            } else if ("3".equals(subMode)) {
                                textView.setText("The current mode is IOT.");
                            } else if ("4".equals(subMode)) {
                                textView.setText("The current mode is OPERATOR.");
                            }                            
                        } else {
                            Elog.i(TAG, "mode len is " + mode.length());
                        }
                    } else {
                        Elog.i(TAG, "The data returned is not right.");
                    }
                } else {
                    Toast.makeText(ModemTestActivity.this, "Query failed.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case EVENT_MODEM_OPERATOR:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_OPERATOR AT cmd success.", Toast.LENGTH_LONG)
                            .show();
                    rebootFlag = true;
                } else {
                    Toast.makeText(ModemTestActivity.this,
                            "MODEM_OPERATOR AT cmd failed.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case EVENT_QUERY_PREFERRED_TYPE_DONE:
            	ar= (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int type = ((int[])ar.result)[0];
                    Elog.i(TAG, "Get Preferred Type " + type);
                    if(type == 0){
                    	modemFlag = true;
                    }else {
                    	modemFlag = false;
                    }
                 }
                 break;
            case  EVENT_SET_PREFERRED_TYPE_DONE :
            	 ar= (AsyncResult) msg.obj;
                 if (ar.exception != null) {
                	 Toast.makeText(ModemTestActivity.this, "Turn off WCDMA Preferred Fail", Toast.LENGTH_LONG).show();
                 }
                 break;
            	
           
        } 
            if (rebootFlag) {
                Elog.i(TAG, "disableAllButton.");
                disableAllButton();
            }
        }
    };

    private void disableAllButton() {
        mNoneBtn.setEnabled(false);
        mCtaBtn.setEnabled(false);
        mFtaBtn.setEnabled(false);
        mIotBtn.setEnabled(false);
        mOperatorBtn.setEnabled(false);
        showDialog(REBOOT_DIALOG);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        switch (id) {
        case CTA_DIALOG:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_cta_options,
                    new boolean[] { false, false, false, false, false, false,
                            false, false, false },
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (isChecked) {
                                mCtaOption = mCtaOption + (1 << whichButton);
                            } else {
                                mCtaOption = mCtaOption - (1 << whichButton);
                            }
                            Elog.v(TAG, "mCtaOption = " + mCtaOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            sendATCommad("1," + String.valueOf(mCtaOption),
                                    EVENT_MODEM_CTA);
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mCtaOption = 0;
                        }
                    }).create();
        case REBOOT_DIALOG:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMessage("Please reboot the phone!")
                    .setPositiveButton("OK", null).create();
        case IOT_DIALOG:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_iot_options,
                    new boolean[] { false, false, false, false, false, false,
                            false, false, false },
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (isChecked) {
                                mIotOption = mIotOption + (1 << whichButton);
                            } else {
                                mIotOption = mIotOption - (1 << whichButton);
                            }
                            Elog.v(TAG, "mIotOption = " + mIotOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            sendATCommad("3," + String.valueOf(mIotOption),
                                    EVENT_MODEM_IOT);
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mIotOption = 0;
                        }
                    }).create();
        case OPERATOR_DIALOG:
            return new AlertDialog.Builder(ModemTestActivity.this).setTitle(
                    "MODEM TEST").setMultiChoiceItems(
                    R.array.modem_test_operator_options,
                    new boolean[] { false, false, false, false, false, false,
                            false, false, false },
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton, boolean isChecked) {

                            /* User clicked on a check box do some stuff */
                            Elog.v(TAG, "whichButton = " + whichButton);
                            Elog.v(TAG, "isChecked = " + isChecked);
                            if (isChecked) {
                                mOperatorOption = mOperatorOption + (1 << whichButton);
                            } else {
                                mOperatorOption = mOperatorOption - (1 << whichButton);
                            }
                            Elog.v(TAG, "mOperatorOption = " + mOperatorOption);
                        }
                    }).setPositiveButton("Send",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked Yes so do some stuff */
                            sendATCommad("4," + String.valueOf(mOperatorOption),
                                    EVENT_MODEM_OPERATOR);
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {

                            /* User clicked No so do some stuff */
                            mOperatorOption = 0;
                        }
                    }).create();
        }
        return null;
    }

    public void writePreferred(int type){
    	SharedPreferences sh = this.getSharedPreferences("RATMode", MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = sh.edit();
		editor.putInt("ModeType", type);
		
		editor.commit();
    }
}
