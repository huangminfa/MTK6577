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

package com.mediatek.engineermode.swla;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;
import com.mediatek.engineermode.modemtest.ModemTestActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.AsyncResult;

public class SwlaActivity extends Activity {

    private Phone phone = null;

    private static final int MSG_ASSERT = 1;
    private static final int MSG_SWLA_ENABLE = 2;
    
    private static final String TAG = "SWLA" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.swla_activity);
        Button mAssert = (Button) findViewById(R.id.swla_assert_btn);
        Button mSwla = (Button) findViewById(R.id.swla_swla_btn);
        mAssert.setOnClickListener(new ButtonListener());
        mSwla.setOnClickListener(new ButtonListener());

        phone = PhoneFactory.getDefaultPhone();
    }

    class ButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
            case R.id.swla_assert_btn:
                sendATCommad("0", MSG_ASSERT);
                break;
            case R.id.swla_swla_btn:
                sendATCommad("1", MSG_SWLA_ENABLE);
                break;
            }
        }
    };

    private void sendATCommad(String str, int message) {

        String ATCmd[] = new String[2];
        ATCmd[0] = "AT+ESWLA=" + str;
        ATCmd[1] = "";
        phone.invokeOemRilRequestStrings(ATCmd, mATCmdHander
                .obtainMessage(message));
        Elog.i(TAG, "Send ATCmd : " + ATCmd[0]);
    }

    private Handler mATCmdHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            AsyncResult ar;
            switch (msg.what) {
            case MSG_ASSERT:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    Toast.makeText(SwlaActivity.this, "Assert Modem Success.",
                            Toast.LENGTH_LONG).show();
                } else {
                    new AlertDialog.Builder(SwlaActivity.this).setTitle(
                            "Failed").setMessage("Assert Modem Failed.")
                            .setPositiveButton("OK", null).show();
                    Toast.makeText(SwlaActivity.this, "Failed.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case MSG_SWLA_ENABLE:
                ar = (AsyncResult) msg.obj;
//                String data[] = (String[]) ar.result;
//                if(null != data && null != data[0]){
//                    Elog.i(TAG, "data[0] is : " + data[0]);
//                } else {
//                    Elog.w(TAG, "data[0] is null");
//                }
                if (ar.exception == null) {
                    Toast.makeText(SwlaActivity.this,
                            "Success", Toast.LENGTH_LONG)
                            .show();
                } else {
                    new AlertDialog.Builder(SwlaActivity.this).setTitle(
                            "Failed").setMessage("Enable Softwore LA Failed.")
                            .setPositiveButton("OK", null).show();
                    Toast.makeText(SwlaActivity.this, "Failed.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    };
}
