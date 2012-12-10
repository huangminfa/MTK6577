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

package com.mediatek.engineermode.desense;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class DesenseLcdActivity extends Activity {

    private Button mDownBtn;
    private Button mUpBtn;

    private Button mSetBtn;

    private EditText mEditText;
    private TextView mDescView;

    private int mMinValue;
    private int mCurrentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.desense_lcd_activity);

        View.OnClickListener listener = new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String inputStr = mEditText.getText().toString().trim();
                if (null == inputStr) {
                    Toast.makeText(DesenseLcdActivity.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (0 == inputStr.length()) {
                    Toast.makeText(DesenseLcdActivity.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int value = Integer.valueOf(inputStr);
                    switch (v.getId()) {
                    case R.id.desense_lcd_down_btn:
                        if (value <= mMinValue) {
                            Toast.makeText(DesenseLcdActivity.this, "The min value is " + mMinValue, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        value--;
                        mEditText.setText(String.valueOf(value));
                        break;
                    case R.id.desense_lcd_up_btn:
                        int maxValue = mMinValue + 50;
                        if (value >= maxValue) {
                            Toast.makeText(DesenseLcdActivity.this, "The max value is " + maxValue, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        value++;
                        mEditText.setText(String.valueOf(value));
                        break;
                    case R.id.desense_lcd_set_btn:

                        if (value < mMinValue) {
                            value = mMinValue;
                            Toast.makeText(DesenseLcdActivity.this, "The value your set is " + mMinValue,
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (value > mMinValue + 50) {
                            value = mMinValue + 50;
                            Toast.makeText(DesenseLcdActivity.this, "The value your set is " + value,
                                    Toast.LENGTH_SHORT).show();
                        }

                        try {
                            EMDsense.LCDWriteCycleSetVal(value);
                            mEditText.setText(String.valueOf(value));
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Elog.v(DesenseActivity.TAG, "LCDWriteCycleSetVal() Exception");
                        }

                        break;
                    }
                } catch (Exception e) {
                    Toast.makeText(DesenseLcdActivity.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        };

        mDownBtn = (Button) findViewById(R.id.desense_lcd_down_btn);
        mUpBtn = (Button) findViewById(R.id.desense_lcd_up_btn);

        mDownBtn.setText("<");
        mDownBtn.setOnClickListener(listener);
        mUpBtn.setText(">");
        mUpBtn.setOnClickListener(listener);

        mSetBtn = (Button) findViewById(R.id.desense_lcd_set_btn);
        mSetBtn.setOnClickListener(listener);

        try {
            mMinValue = EMDsense.LCDWriteCycleGetMinVal();
            mCurrentValue = EMDsense.LCDWriteCycleGetCurrentVal();
        } catch (Exception e) {
            e.printStackTrace();
            mMinValue = 0;
            mCurrentValue = 0;
            Elog.v(DesenseActivity.TAG, "LCDWriteCycleGetMinVal() or LCDWriteCycleGetCurrentVal() Exception");
        }

        mEditText = (EditText) findViewById(R.id.desense_lcd_value_edit);
        mEditText.setText(String.valueOf(mCurrentValue));
        
        mDescView = (TextView) findViewById(R.id.desense_lcd_desc_textview);
        mDescView.setText("The input number must be between " + mMinValue +" and " + String.valueOf(mMinValue + 50));
    }

}
