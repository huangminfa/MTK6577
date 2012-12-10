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

import com.mediatek.engineermode.ChipSupport;
import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class DesenseBackLight extends Activity {

    private EditText mFrequencyEdit;
    private EditText mDutyEdit;

    private Button mFrequencyBtn;
    private Button mDutyBtn;
    
    private TextView mFreqDescView;
    private TextView mDutyDescView;
    
    private TextView mPWMFreqView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.desense_back_light);

        View.OnClickListener listener = new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (v.getId()) {
                case R.id.desense_back_light_frequency_btn:
                    String frequnecy = mFrequencyEdit.getText().toString().trim();
                    if (null == frequnecy) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (0 == frequnecy.length()) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int valueFreq = Integer.valueOf(frequnecy);
                        if (valueFreq > 7) {
                            valueFreq = 7;
                            Toast.makeText(DesenseBackLight.this, "The max frequency value you can set is 7", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (valueFreq < 0) {
                            valueFreq = 0;
                            Toast.makeText(DesenseBackLight.this, "The min frequency value you can set is 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int result = EMDsense.BacklightSetPwmFreq(valueFreq);
                        Elog.v(DesenseActivity.TAG, "BacklightSetPwmFreq result = " + result);
                        mFrequencyEdit.setText(String.valueOf(valueFreq));
                        
                        String freqHz = EMDsense.BacklightGetCurrentPwmFreqHZ();
                        if (null != freqHz) {
                            Toast.makeText(DesenseBackLight.this, "The frequency value you set is " + freqHz + " HZ", Toast.LENGTH_SHORT).show();
                        }
                        
                    } catch (Exception e) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    break;
                case R.id.desense_back_light_duty_btn:
                    String duty = mDutyEdit.getText().toString().trim();
                    if (null == duty) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (0 == duty.length()) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int dutyValue = Integer.valueOf(duty);
                        if (dutyValue < 0) {
                            dutyValue = 0;
                            Toast.makeText(DesenseBackLight.this, "The min duty value you can set is 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (dutyValue > 255) {
                            dutyValue = 255;
                            Toast.makeText(DesenseBackLight.this, "The max duty value you can set is 255", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        EMDsense.BacklightSetPwmDuty(dutyValue);
                        mDutyEdit.setText(String.valueOf(dutyValue));
                        
                    } catch (Exception e) {
                        Toast.makeText(DesenseBackLight.this, "Please input right value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    break;
                }
            }
        };

        mFrequencyEdit = (EditText) findViewById(R.id.desense_back_light_frequency_edit);
        mDutyEdit = (EditText) findViewById(R.id.desense_back_light_duty_edit);
        
        mFrequencyEdit.setText(String.valueOf(EMDsense.BacklightGetCurrentPwmFreq()));
        mDutyEdit.setText(String.valueOf(EMDsense.BacklightGetCurrentPwmDuty()));

        mFrequencyBtn = (Button) findViewById(R.id.desense_back_light_frequency_btn);
        mDutyBtn = (Button) findViewById(R.id.desense_back_light_duty_btn);
        mFrequencyBtn.setOnClickListener(listener);
        mDutyBtn.setOnClickListener(listener);
        
        mFreqDescView = (TextView) findViewById(R.id.desense_back_light_freq_desc_textview);
        mDutyDescView = (TextView) findViewById(R.id.desense_back_light_duty_desc_textview);
        
        mFreqDescView.setText("The input freq must be between 0 and 7");
        mDutyDescView.setText("The input duty must be between 0 and 255");
        
        mPWMFreqView = (TextView) findViewById(R.id.desense_back_light_frequency);
        
        setDisable75();
    }

    /**
     * PWM Freq in 75 have no sense
     */
    private void setDisable75() {
        if (ChipSupport.GetChip() == ChipSupport.MTK_6575_SUPPORT) {
            mFrequencyEdit.setVisibility(View.INVISIBLE);
            mFrequencyBtn.setVisibility(View.INVISIBLE);
            mFreqDescView.setVisibility(View.INVISIBLE);
            mPWMFreqView.setVisibility(View.INVISIBLE);
        }
    }
}
