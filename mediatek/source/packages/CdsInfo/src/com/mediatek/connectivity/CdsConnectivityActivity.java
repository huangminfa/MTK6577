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

package com.mediatek.connectivity;

import android.app.Activity;
import com.mediatek.connectivity.R;
import android.view.View;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.mediatek.xlog.Xlog;
import android.net.ConnectivityManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

public class CdsConnectivityActivity extends Activity implements View.OnClickListener {
    private final String TAG = "CdsConnectivityActivity";
    private Context mContext;
    private Toast mToast;
    String[] defaultConnList = new String[]{"Wi-Fi", "Mobile"};
    Spinner mConnSpinner = null;
    private int selectConnType = ConnectivityManager.TYPE_MOBILE;
    private EditText mReportPercent = null;
    private ConnectivityManager mConnMgr = null;
    private Button mReportBtnCmd = null;
        
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.cds_connectivity);

        mContext = this.getBaseContext();
        
        mConnSpinner = (Spinner) findViewById(R.id.connTypeSpinnner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, defaultConnList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mConnSpinner.setAdapter(adapter);
        mConnSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View arg1,
            int position, long arg3) {
                // TODO Auto-generated method stub
                try{
                    switch(position){
                            case 0:
                                selectConnType = ConnectivityManager.TYPE_WIFI;
                            break;                                
                            case 1:
                                selectConnType = ConnectivityManager.TYPE_MOBILE;
                            break;
                        }                    
                    mConnSpinner.requestFocus();
                    
                }catch(Exception e){
                    selectConnType = ConnectivityManager.TYPE_MOBILE;
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });        
        
        mReportPercent = (EditText)  findViewById(R.id.ReportPercent);
        mReportPercent.setText("55");
        mConnMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
        mReportBtnCmd = (Button) findViewById(R.id.Report);
        mReportBtnCmd.setOnClickListener(this);
        
        Xlog.i(TAG, "CdsConnectivityActivity is started");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void onClick(View v) {
        int buttonId = v.getId();
        
        switch(buttonId) {
            case R.id.Report:                
                Xlog.i(TAG, "Report Inet action");
                reportInetAcction();
                break;
        }
    }
    
    private void reportInetAcction(){
        
        String percentText = mReportPercent.getText().toString();
        
        try{
            if(percentText.length() == 0){
               mToast.setText("The percent value is empty. This is not allowed");
               mToast.show();
               return;
             }             
            int percentValue = Integer.parseInt(percentText);
            if(percentValue > 100 || percentValue < 0){
               mToast.setText("The range fo report percent should be 1 ~ 100");
               mToast.show();
               return;
            }
            Xlog.i(TAG, "Report nw:" + selectConnType + "-" + percentValue);
            mConnMgr.reportInetCondition(selectConnType, percentValue);                        
        }catch(Exception e){
            mToast.setText("ERROR:" + e.toString());
            e.printStackTrace();
        }
        
    }
    
}