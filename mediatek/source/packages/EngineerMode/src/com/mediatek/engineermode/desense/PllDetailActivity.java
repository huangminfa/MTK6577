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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

public class PllDetailActivity extends Activity {

    private Button mSetBtn;
    private TextView mTitleView;
    private EditText mEditText;

    private String mName;
    private int mId;
    private String mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desense_pll_detail_activity);

        mSetBtn = (Button) findViewById(R.id.desense_pll_detail_set_btn);
        mTitleView = (TextView) findViewById(R.id.desense_pll_detail_title_textview);
        mEditText = (EditText) findViewById(R.id.desense_pll_detail_edit);

        Intent intent = this.getIntent();
        mName = intent.getStringExtra("name");
        mId = intent.getIntExtra("id", -1);
        mValue = intent.getStringExtra("value");

        mTitleView.setText(mName);
        mEditText.setText(mValue);

        mSetBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String editValue = mEditText.getText().toString().trim();
                Elog.v(DesenseActivity.TAG, "editValue = " + editValue);
                try {
                    @SuppressWarnings("unused")
                    long value = Long.parseLong(editValue, 16);
                } catch (Exception e) {
                    Toast.makeText(PllDetailActivity.this,
                            "The input number is wrong!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (editValue.length() > 8) {
                    Toast.makeText(PllDetailActivity.this,
                            "The input number is wrong!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                EMDsense.PLLSetClock(mId, checkValue(editValue));
                Elog.v(DesenseActivity.TAG, "hexVal = " + editValue);
            }
        });

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Toast.makeText(PllDetailActivity.this,
                "Wrong set may cause system error!", Toast.LENGTH_SHORT).show();
    }

    private String checkValue(String str) {
        String editValue = str;
        if (editValue.length() > 4 && editValue.length() <= 8) {
            String temp1 = editValue.substring(editValue.length() - 4, editValue.length());
            String temp2 = editValue.substring(0, editValue.length() - 4);
            Elog.i(DesenseActivity.TAG, "temp1 = " + temp1);
            Elog.i(DesenseActivity.TAG, "temp2 = " + temp2);
            if (null != temp2 && temp2.length() < 4) {
                Elog.i(DesenseActivity.TAG, "4- temp2.length() = "
                        + (4 - temp2.length()));
                int len = 4 - temp2.length();
                for (int i = 0; i < len; i++) {
                    temp2 = "0" + temp2;
                    Elog.i(DesenseActivity.TAG, "temp2 = " + temp2);
                }
            }
            Elog.i(DesenseActivity.TAG, "temp1+temp2 = " + temp1 + temp2);
            return temp1 + temp2;
        }
        return editValue;
    }
}
