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

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.engineermode.google_phone;


import com.mediatek.engineermode.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;


import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.content.SharedPreferences;
import com.android.internal.telephony.gemini.GeminiPhone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import android.os.SystemProperties;

import com.mediatek.featureoption.FeatureOption;
import android.widget.Toast;

import android.util.Log;

public class RadioInfo extends Activity {
    private final String TAG = "phone";

    private static final int EVENT_QUERY_PREFERRED_TYPE_DONE = 1000;
    private static final int EVENT_SET_PREFERRED_TYPE_DONE = 1001;
  
    private Spinner preferredNetworkType;

   
    private Phone phone = null;
    private GeminiPhone geminiPhone = null;
   
    private int modetype ;
    public static final int MODEM_MASK_WCDMA = 0x04;
	public static final int MODEM_MASK_TDSCDMA = 0x08;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_QUERY_PREFERRED_TYPE_DONE:
                    ar= (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        int type = ((int[])ar.result)[0];
                        Log.d(TAG, "Get Preferred Type " + type);
                        switch(type){
                        case 0:
                        	preferredNetworkType.setSelection(1, true);
                        	break;
                        case 1:
                        	preferredNetworkType.setSelection(3, true);
                        	break;
                        case 2:
                        	preferredNetworkType.setSelection(2, true);
                        	break;
                        case 3:
                        	preferredNetworkType.setSelection(0, true);
                        	default:
                        		break;
                        }
                    } else {
                        //preferredNetworkType.setSelection(0, true);
                    }
                    break;
                case EVENT_SET_PREFERRED_TYPE_DONE:
                    ar= (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        phone.getPreferredNetworkType(
                                obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE));
                    }
                    break;
                default:
                    break;

            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.radio_info);
        isFTTOrTDDType();
        phone = PhoneFactory.getDefaultPhone();
        if(FeatureOption.MTK_GEMINI_SUPPORT){
        geminiPhone =(GeminiPhone) PhoneFactory.getDefaultPhone();
        }
        	 
        

        preferredNetworkType = (Spinner) findViewById(R.id.preferredNetworkType);
        if(!isTDDType){
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
                android.R.layout.simple_spinner_item, mPreferredNetworkLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preferredNetworkType.setAdapter(adapter);
        }else{
        	 ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
                     android.R.layout.simple_spinner_item, tddPreferredNetworkLabels);
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             preferredNetworkType.setAdapter(adapter);
        }
        preferredNetworkType.setOnItemSelectedListener(mPreferredNetworkHandler);
       
        phone.getPreferredNetworkType(
                mHandler.obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeatureOption.MTK_GEMINI_SUPPORT){
        modetype = geminiPhone.get3GCapabilitySIM();
        }else{
        	modetype = phone.get3GCapabilitySIM();
        }


       

        Log.i(TAG, "[RadioInfo] onResume: register phone & data intents");

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "[RadioInfo] onPause: unregister phone & data intents");
    }

    AdapterView.OnItemSelectedListener
            mPreferredNetworkHandler = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            Message msg = mHandler.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE);
            if (pos>=0 && pos<=4) { //IS THIS NEEDED to extend to the entire range of values
            	switch(pos){
            	case 0:
                        if(modetype != -1){
            		 Log.d(TAG, "GSM/WCDMA(auto) " + 3);
                            writePreferred(3);
            		 phone.setPreferredNetworkType(3, msg);
                        }else{
                           preferredNetworkType.setSelection(3, true);
                         Toast.makeText(RadioInfo.this, "3G Off, Cannot Set", Toast.LENGTH_LONG).show();
                        }
            		 break;
            	case 1:
            		 Log.d(TAG, "WCDMA Preferred " + 0);
            		 if(FeatureOption.MTK_RAT_WCDMA_PREFERRED){
                         if(modetype != -1){
            			   writePreferred(0);
            			 phone.setPreferredNetworkType(0, msg);
                          }else{
                           preferredNetworkType.setSelection(3, true);
                         Toast.makeText(RadioInfo.this, "3G Off, Cannot Set", Toast.LENGTH_LONG).show();
                        }
            		 
            		 }else{
            			 if(!isTDDType){
            			 Toast.makeText(RadioInfo.this, "Not Support WCDMA Preferred", Toast.LENGTH_LONG).show();
            			 }else{
            				 Toast.makeText(RadioInfo.this, "Not Support TD-SCDMA Preferred", Toast.LENGTH_LONG).show(); 
            			 }
            	      }
            		 break;
            	case 2:
                         if(modetype != -1){
            		 Log.d(TAG, "WCDMA only " + 2);
                            writePreferred(2);
            		 phone.setPreferredNetworkType(2, msg);
                         }else{
                           preferredNetworkType.setSelection(3, true);
                         Toast.makeText(RadioInfo.this, "3G Off, Cannot Set", Toast.LENGTH_LONG).show();
                        }
            		 break;
            	case 3:
                         if(modetype != -1){
            		 Log.d(TAG, "GSM Only" + 1);
                           writePreferred(1);
            		 phone.setPreferredNetworkType(1, msg);
                         }else{
                         Toast.makeText(RadioInfo.this, "3G Off, Cannot Set", Toast.LENGTH_LONG).show();
                        }
            		 break;
            	case 4:
                    writePreferred(4);
                    break;
            	default:
            		break;            		
            	}
            
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };
   
  public void writePreferred(int type){
    	SharedPreferences sh = this.getSharedPreferences("RATMode", MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = sh.edit();		
        if (type == 4) {
            Log.d(TAG, "type = 4 , clear value");
            editor.clear();
        } else {
        editor.putInt("ModeType", type);
        }		
		editor.commit();
    }
  private boolean isTDDType = false;
  private boolean isFTTOrTDDType(){
     String mt = SystemProperties.get("gsm.baseband.capability");
     if (mt != null) {
			try {
				int mask = Integer.valueOf(mt);
				if ((mask & MODEM_MASK_TDSCDMA) != 0) {
					Log.i(TAG, "MODEM_MASK_TDSCDMA : " + isTDDType);
					isTDDType = true;
				} else if ((mask & MODEM_MASK_WCDMA) != 0) {
					Log.i(TAG, "MODEM_MASK_WCDMA : " + isTDDType);
					isTDDType = false;
				} 
			} catch (NumberFormatException e) {
				Log.i(TAG, "isFDDType : " + isTDDType);
			}
		}
     return isTDDType;
  }
    private String[] mPreferredNetworkLabels = {
    		"GSM/WCDMA (auto)",
    		"GSM/WCDMA (WCDMA preferred)",
            "WCDMA only",
            "GSM only",
            "Not Specified"};
    private String[] tddPreferredNetworkLabels={
    		"GSM/TD-SCDMA(auto)",
    		"TD-SCDMA preferred",
    		"TD-SCDMA only",
    		"GSM only",
    		"Not Specified"};
}
