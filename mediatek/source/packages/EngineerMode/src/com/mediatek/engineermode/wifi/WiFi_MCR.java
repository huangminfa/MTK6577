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

package com.mediatek.engineermode.wifi;

import com.mediatek.engineermode.R;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.mediatek.engineermode.wifi.EMWifi;
import com.mediatek.xlog.Xlog;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;

public class WiFi_MCR extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_MCR";

	private EditText mAddrEdit;
	private EditText mValueEdit;
	private Button mReadBtn;
	private Button mWriteBtn;
	private long chipID = 0x00;
	private WiFiStateManager mWiFiStateManager = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_mcr);

		mAddrEdit = (EditText) findViewById(R.id.WiFi_MCR_Addr_Content);
		mValueEdit = (EditText) findViewById(R.id.WiFi_MCR_Value_Content);
		mReadBtn = (Button) findViewById(R.id.WiFi_MCR_ReadBtn);
		mWriteBtn = (Button) findViewById(R.id.WiFi_MCR_WriteBtn);
		if(null != mReadBtn)
		{
			mReadBtn.setOnClickListener(this);
		}
		if(null != mWriteBtn)
		{
			mWriteBtn.setOnClickListener(this);
		}
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (!EMWifi.isIntialed) {
			Xlog.w(TAG, "Wifi is not initialized");
			new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
         		   "Wifi is not initialized").setPositiveButton("OK",
        		    new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog,int which) {
                            // TODO Auto-generated method stub  
                    		dialog.dismiss();
                	        finish();
     		           }
		        }).show();
			return;
		}
		if (arg0.getId() == mReadBtn.getId()) {
			// mAddrEdit.setText("mAddrEdit settext");
			// mValueEdit.setText("mValueEdit settext");
			long u4Addr = -1;
			long[] u4Value = new long[1];

			try {
				u4Addr = Long.parseLong(mAddrEdit.getText().toString(), 16);
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_MCR.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}
			EMWifi.readMCR32(u4Addr, u4Value);
			String result = String.format("%1$08x", u4Value[0]);
			mValueEdit.setText(result);
//			mValueEdit.setText(Long.toHexString(u4Value[0]));
		}
		if (arg0.getId() == mWriteBtn.getId()) {

			long u4Addr = -1;
			long u4Value = -1;

			try {
				u4Addr = Long.parseLong(mAddrEdit.getText().toString(), 16);
				u4Value = Long.parseLong(mValueEdit.getText().toString(), 16);
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_MCR.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}

			EMWifi.writeMCR32(u4Addr, u4Value);
		}
	}

	protected void onStart() {
		super.onStart();
		checkWiFiChipState();
		/*
                if (!EMWifi.isIntialed) {
                	//WiFi is for both MT6620 and MT5921
						chipID = EMWifi.initial();
		           		if (chipID != 0x6620 && chipID != 0x5921) {
                                // Toast.makeText(this, "Wrong to configuration",
                                // Toast.LENGTH_LONG);
                                new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
                                                "Please check your wifi state").setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
        
                                                        public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                                // TODO Auto-generated method stub
                                                                finish();
                                                        }
                                                }).show();
                        } else {
                                Log.d(TAG, "Initialize succeed");
										int result = -1;
		                                result = EMWifi.setTestMode();
		                                if(result == 0)
		                                {
		                                	EMWifi.isIntialed = true;
		                                	Log.e(TAG, "setTestMode succeed");
		                                }
		                                else
		                                {
		                                	Log.e(TAG, "setTestMode failed, ERROR_CODE = " + result);
		                                }
                        }
                }
                */
        }


	protected void onStop() {
		super.onStop();
		finish();
	}
	private void checkWiFiChipState()
	{
		int result = 0x0;
				if(mWiFiStateManager == null)
				{
					mWiFiStateManager = new WiFiStateManager(this);
				}
				result = mWiFiStateManager.checkState(this);
				switch(result)
				{
					case WiFiStateManager.ENABLEWIFIFAIL:
						new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
		                                                "enable Wi-Fi failed").setPositiveButton("OK",
		                                                new DialogInterface.OnClickListener() {
		                                                        public void onClick(DialogInterface dialog,int which) {
		                                                                // TODO Auto-generated method stub  
										dialog.dismiss();
		                                                                finish();
		                                                        }
		                                                }).show();
					break;
					
					case WiFiStateManager.INVALIDCHIPID:
					case WiFiStateManager.SETTESTMODEFAIL:
						new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setMessage(
		                                                "Please check your Wi-Fi state").setPositiveButton("OK",
		                                                new DialogInterface.OnClickListener() {
		                                                        public void onClick(DialogInterface dialog,int which) {
		                                                                // TODO Auto-generated method stub 
		                                                               	mWiFiStateManager.disableWiFi();
										dialog.dismiss();	 
		                                                                finish();
		                                                        }
		                                                }).show();
					break;
					case WiFiStateManager.CHIPREADY:	
					case 0x6620:
					case 0x5921:
					break;
					default:
					
					break;
				}
	}

}
