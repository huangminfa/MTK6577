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

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.engineermode.wifi.EMWifi;
import android.text.TextUtils;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import com.mediatek.xlog.Xlog;

public class WiFi_EEPROM extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_EEPROM";

	private EditText mWordAddrEdit;
	private EditText mWordValueEdit;
	private Button mWordReadBtn;
	private Button mWordWriteBtn;
	private EditText mStringAddrEdit;
	private EditText mStringLengthEdit;
	private EditText mStringValueEdit;
	private Button mStringReadBtn;
	private Button mStringWriteBtn;
	//private Button mSaveBtn;
	//private Button mBurnBtn;
	//private Button mReadAllBtn;
	//private Button mClearBtn;
	private EditText mShowWindowText;

	private String mSaveFileName;
	private long chipID = 0x00;
	private boolean normalBackgroundFlag = false;
	private WiFiStateManager mWiFiStateManager = null;
	private final String FILE_PATH = "/data/data/com.mediatek.engineermode/EEPROM";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_eeprom);

		// mDialog = new myDialog(this);

		mWordAddrEdit = (EditText) findViewById(R.id.WiFi_addr_Content);
		mWordValueEdit = (EditText) findViewById(R.id.WiFi_value_Content);
		mWordReadBtn = (Button) findViewById(R.id.WiFi_Read_Word);
		mWordWriteBtn = (Button) findViewById(R.id.WiFi_Write_Word);

		mStringAddrEdit = (EditText) findViewById(R.id.WiFi_addr_Content_String);
		mStringLengthEdit = (EditText) findViewById(R.id.WiFi_length_Content_String);
		mStringValueEdit = (EditText) findViewById(R.id.WiFi_value_Content_String);
		mStringReadBtn = (Button) findViewById(R.id.WiFi_Read_String);
		mStringWriteBtn = (Button) findViewById(R.id.WiFi_Write_String);

		//mSaveBtn = (Button) findViewById(R.id.WiFi_SaveBtn);
		//mBurnBtn = (Button) findViewById(R.id.WiFi_BurnBtn);
		//mReadAllBtn = (Button) findViewById(R.id.WiFi_ReadAllBtn);
		//mClearBtn = (Button) findViewById(R.id.WiFi_ClearBtn);
		mShowWindowText = (EditText) findViewById(R.id.WiFi_ShowWindow);

		if(null != mWordReadBtn)
		{
			mWordReadBtn.setOnClickListener(this);
		}
		if(null != mWordWriteBtn)
		{
			mWordWriteBtn.setOnClickListener(this);
		}
		if(null != mStringReadBtn)
		{
			mStringReadBtn.setOnClickListener(this);
		}
		if(null != mStringWriteBtn)
		{
			mStringWriteBtn.setOnClickListener(this);
		}
		/*
		if(null != mSaveBtn)
		{
			mSaveBtn.setOnClickListener(this);
		}
		if(null != mBurnBtn)
		{
			mBurnBtn.setOnClickListener(this);
		}
		if(null != mReadAllBtn)
		{
			mReadAllBtn.setOnClickListener(this);
		}
		if(null != mClearBtn)
		{
			mClearBtn.setOnClickListener(this);
		}
		*/
		// String logPath = this.getFilesDir().getParentFile().toString() +
		// "/myData";
		// File myFile = new File(logPath);
		File myFile = new File(FILE_PATH);
		if (!myFile.exists()) {

			if (myFile.mkdirs()) {
				Xlog.v(TAG, "create dir succeed" + myFile.toString());
			} else {
				Xlog.d(TAG, "create dir failed" + myFile.toString());
			}
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

	protected void onResume() {
		super.onResume();
		if (EMWifi.setEEPRomSize(512) != 0) {
			Toast.makeText(WiFi_EEPROM.this,
					"initial setEEPRomSize to 512 failed", Toast.LENGTH_LONG)
					.show();
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
		long u4Addr = 0;
		long u4Value = 0;
		long u4Length = 0;
		CharSequence inputVal;
		String text;

		switch (arg0.getId()) {

		case R.id.WiFi_Read_Word:
			long[] u4Val = new long[1];
			try {
				u4Addr = Long.parseLong(mWordAddrEdit.getText().toString(), 16);
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_EEPROM.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}
			/*
			 * if (u4Addr < 0){ return; }
			 */
			EMWifi.readEEPRom16(u4Addr, u4Val);
			mWordValueEdit.setText(Long.toHexString(u4Val[0]));
			break;

		case R.id.WiFi_Write_Word:

			try {
				u4Addr = Long.parseLong(mWordAddrEdit.getText().toString(), 16);
				u4Value = Long.parseLong(mWordValueEdit.getText().toString(),
						16);
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_EEPROM.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}

			EMWifi.writeEEPRom16(u4Addr, u4Value);
			EMWifi.setEEPromCKSUpdated();
			break;

		case R.id.WiFi_Read_String:
			
			byte[] acSzTmp = new byte[512];

			try {
				u4Addr = Long.parseLong(mStringAddrEdit.getText().toString(),
						16);
				u4Length = Long.parseLong(mStringLengthEdit.getText()
						.toString());
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_EEPROM.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (u4Length == 0) {
				return;
			}

			EMWifi.eepromReadByteStr(u4Addr, u4Length, acSzTmp);
			text = new String(acSzTmp, 0, (int) u4Length * 2);
			mStringValueEdit.setText(text);
			break;

		case R.id.WiFi_Write_String:
			String szTmp;

			inputVal = mStringAddrEdit.getText();
			if (TextUtils.isEmpty(inputVal)) {
				Toast.makeText(WiFi_EEPROM.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				break;
			}
			
			try {
				u4Addr = Long.parseLong(inputVal.toString(),
						16);
				u4Length = Long.parseLong(mStringLengthEdit.getText()
						.toString());
			} catch (NumberFormatException e) {
				Toast.makeText(WiFi_EEPROM.this, "invalid input value",
						Toast.LENGTH_SHORT).show();
				return;
			}

			szTmp = mStringValueEdit.getText().toString();
//			szTmp = inputVal.toString();
			int len = szTmp.length();
			if ((len == 0) || (len % 2 == 1)) {
				PrintText("Byte string length error:" + len + "bytes\n");
				return;
			}

			EMWifi.eepromWriteByteStr(u4Addr, (len / 2), szTmp);
			EMWifi.setEEPromCKSUpdated();
			break;

			/*
		case R.id.WiFi_SaveBtn:
			
			//make sure that EEProm does exist
			
			if (!isEepromExist())
			{
				return;
			}
			
			onClickSaveBtnEepEe2File();
			break;

		case R.id.WiFi_BurnBtn:
			
			onClickBurnBtnEepFile2ee();
			break;

		case R.id.WiFi_ReadAllBtn:
			
			onClickReadAllBtn();
			break;

		case R.id.WiFi_ClearBtn:
			mShowWindowText.setText("");
			break;
		*/
		default:
			break;
		}
	}

	private boolean isEepromExist()
	{
		long[] i4Tmp = new long[1];
		long[] u4Tmp = new long[1];
		long u2Tmp;
		int u2EepromSz = 0;
		int u2EepromValue;
		if (EMWifi.getSpecEEPRomSize(i4Tmp) != 0) {
			PrintText("query EEPROM size failed\n");	
			return false;
		}
		u2EepromSz = (int) i4Tmp[0];
		Xlog.d(TAG, "u2EepromSz = " + u2EepromSz);
		if(i4Tmp[0] == 0)
		{
			PrintText("EEPROM does not exist\n");
			return false;
		}
		return true;
	}

	private boolean createFile(String filename) {

		File mFile = new File(FILE_PATH + "/" + filename);
		try {
			mFile.createNewFile();
			Xlog.d(TAG, "create file succeed");
		} catch (IOException e) {
			PrintText("Create file failed\n");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean SaveAsFile(String filename, String str) {
		// String path =
		// WiFi_EEPROM.this.getFilesDir().getParentFile().toString()+ "/myData";
		File mfile = new File(FILE_PATH, filename);
		FileOutputStream out;

		try {
			out = new FileOutputStream(mfile, true);
			try {
				out.write(str.getBytes());
				out.close();
			} catch (IOException e) {
				PrintText("No file is specified");
				e.printStackTrace();
				return false;
			}
		} catch (FileNotFoundException E) {
			PrintText("Write fail with string" + str + "\n");
			E.printStackTrace();
			return false;
		}

		return true;
	}
/*
	private void onClickSaveBtnEepEe2File() {
		LayoutInflater inflater = (LayoutInflater) WiFi_EEPROM.this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.mydialog, null);
		final EditText text = (EditText) layout.findViewById(R.id.save_file_name);
		AlertDialog.Builder builder = new AlertDialog.Builder(WiFi_EEPROM.this);
		AlertDialog alertDialog;
		
				

		builder.setTitle("Please input a file name");
		builder.setView(layout);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				CharSequence name = text.getText();
				if (TextUtils.isEmpty(name)) {
					return;
				}
				long[] i4Tmp = new long[1];
				long[] u4Tmp = new long[1];
				long u2Tmp;
				int u2EepromSz = 0;
				int u2EepromValue;

				if (EMWifi.getEEPRomSize(i4Tmp) != 0) {
					i4Tmp[0] = 0;
				}
				u2EepromSz = (int) i4Tmp[0];
				u2EepromSz = u2EepromSz > 512 ? 512 : u2EepromSz;
				Log.d(TAG, "u2EepromSz = " + u2EepromSz);
				if(u2EepromSz == 0)
				{
					PrintText("EEPROM does not exist\n");
					return;
				}
				
				mSaveFileName = name.toString();
				PrintText("SaveFileName =" + mSaveFileName + "\n");
				if (!createFile(mSaveFileName)) {
					return;
				}
				/*
				File mfile = new File(FILE_PATH, mSaveFileName);
				FileOutputStream out;
				byte[] buffer = new byte[2];
				buffer[0] = (byte)(u4Tmp[0] & 0x00ff);
					buffer[1] = (byte)((u4Tmp[0] & 0xff00) >> 8);
					try {
						out = new FileOutputStream(mfile, true);
						try {
							out.write(buffer);
							out.close();
						} catch (IOException e) {
							PrintText("No file is specified");
							e.printStackTrace();
							return;
						}
					} catch (FileNotFoundException E) {
						E.printStackTrace();
						return ;
					}
					*\\\\/
				for (u2Tmp = 0; u2Tmp < (u2EepromSz / 2); u2Tmp++) {
					//if (EMWifi.readEEPRom16(u2Tmp, u4Tmp) != 0) {
					if (EMWifi.readSpecEEPRom16(u2Tmp, u4Tmp) != 0) {
						Log.e(TAG, "read offset:" + u2Tmp + " failed");
						return;
					}
					u2EepromValue = (int) u4Tmp[0];
					Log.d(TAG, "offset = " + u2Tmp + ", value = "  + u4Tmp[0]);
					// String str = Long.toHexString(u2EepromValue);
					String result = String.format("%1$04x", u2EepromValue)/*+ "\r\n"*\\/;
							
					// Log.d(TAG, result);
					if (!SaveAsFile(mSaveFileName, result)) {
						return;
					}
					/*
					if ((u2Tmp % 16) == 0xF) {
						result = "\r\n";
						if (!SaveAsFile(mSaveFileName, result)) {
							return;
						}
					}
					*\\\/
					
					
				}
			}
		});

		alertDialog = builder.create();
		alertDialog.show();
	}

	private void onClickBurnBtnEepFile2ee() {
		long[] i4Tmp = new long[1];
				long[] u4Tmp = new long[1];
				long u2Tmp;
				int u2EepromSz = 0;
				int u2EepromValue;

				if (EMWifi.getEEPRomSize(i4Tmp) != 0) {
					i4Tmp[0] = 0;
				}
				u2EepromSz = (int) i4Tmp[0];
				u2EepromSz = u2EepromSz > 512 ? 512 : u2EepromSz;
				Log.d(TAG, "u2EepromSz = " + u2EepromSz);
				if(i4Tmp[0] == 0)
				{
					PrintText("EEPROM does not exist\n");
					return;
				}
		Intent intent = new Intent(WiFi_EEPROM.this, FileList.class);
		intent.putExtra("identifier", "EEPROM");
		normalBackgroundFlag = true;
		startActivityForResult(intent, 1);
	}

	private void onClickReadAllBtn() {
		long[] i4Tmp = new long[1];
		int u2EepromSzByte;
		int u2Tmp;
		int[] pau2EepromValue;
		int u2Tmp2;
		String key, value0, value1, value2, value3;

		if (EMWifi.getEEPRomSize(i4Tmp) < 0) {
			PrintText("Get EEPROM size failed\n");
			return;
		}
		if(i4Tmp[0] == 0)
		{
			PrintText("EEPROM does not exist\n");
			return;
		}
		u2EepromSzByte = (int) (i4Tmp[0]);
		if (u2EepromSzByte > 512) {
			PrintText("Get EEPROM size " + u2EepromSzByte + " too big\n");
			//return;
			u2EepromSzByte = 512;
		}
		
		pau2EepromValue = new int[u2EepromSzByte / 2];

		if (pau2EepromValue == null) {
			PrintText("Memory allocate fail, size " + u2EepromSzByte + "\n");
			return;
		}

		if (eepReadWordBySize(pau2EepromValue, u2EepromSzByte / 2) == false) {
			PrintText("Get EEPROM content failed\n");
			return;
		}

		for (u2Tmp = 0; u2Tmp < (u2EepromSzByte / 2); u2Tmp += 4) {
			if ((u2Tmp + 4) < (u2EepromSzByte / 2)) {
				u2Tmp2 = 4;
			} else {
				u2Tmp2 = (u2EepromSzByte / 2) - u2Tmp;
			}

			switch (u2Tmp2) {
			case 0:
				// assert(0);
				break;

			case 1:
				key = String.format("%1$04x", u2Tmp);
				value0 = String.format("%1$04x", pau2EepromValue[u2Tmp]);
				PrintText("\n" + key + ": " + value0 + "	");
				break;

			case 2:
				key = String.format("%1$04x", u2Tmp);
				value0 = String.format("%1$04x", pau2EepromValue[u2Tmp]);
				value1 = String.format("%1$04x", pau2EepromValue[u2Tmp+1]);
				PrintText("\n" + key + ": " + value0 + "	" + value1 + "	");
				break;

			case 3:
				key = String.format("%1$04x", u2Tmp);
				value0 = String.format("%1$04x", pau2EepromValue[u2Tmp]);
				value1 = String.format("%1$04x", pau2EepromValue[u2Tmp+1]);
				value2 = String.format("%1$04x", pau2EepromValue[u2Tmp+2]);
				PrintText("\n" + key + ": " + value0 + "	" + value1 + "	" + value2 + "	");
				break;

			case 4:
				key = String.format("%1$04x", u2Tmp);
				value0 = String.format("%1$04x", pau2EepromValue[u2Tmp]);
				value1 = String.format("%1$04x", pau2EepromValue[u2Tmp+1]);
				value2 = String.format("%1$04x", pau2EepromValue[u2Tmp+2]);
				value3 = String.format("%1$04x", pau2EepromValue[u2Tmp+3]);
				PrintText("\n" + key + ": " + value0 + "	" + value1 + "	" + value2 + "	" + value3 + "	");

			default:
				break;
			}
		}
	}
*/
	private boolean eepReadWordBySize(int[] pau2EepValue, int u2EepSizeW) {
		long[] u4Tmp = new long[1];
		int u2Tmp = 0;

		for (u2Tmp = 0; u2Tmp < u2EepSizeW; u2Tmp++) {
			if (EMWifi.readEEPRom16(u2Tmp, u4Tmp) != 0) {
				String str = String.format("%1$04x", u2Tmp);
				PrintText("EEPROM read fial at word offset "
						+ str + "\n");
				return false;
			} else {
				pau2EepValue[u2Tmp] = (int) (u4Tmp[0]);
			}
		}

		return true;
	}

	private void PrintText(String text) {
		mShowWindowText.append(text);
		return;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		normalBackgroundFlag = false;
		switch (resultCode) {

		case RESULT_OK:
			Bundle b = data.getExtras();
			if (null == b)
			{
				return;
			}
			String filename = b.getString("FILENAME");
			PrintText("GetOpenFileNameEx = " + filename + "\n");
			long size = 512;

			if (EMWifi.setEEPRomSize(512) != 0) {
				// PrintText("GetOpenFileNameEx = " + filename);
				return;
			}

			int i4RetVal = EMWifi.setEEPRomFromFile(FILE_PATH + "/" + filename);
			if (i4RetVal != 0) {
				PrintText("setEEPRomFromFile failed, error = " + i4RetVal);
				Xlog.d(TAG, "write from file failed");
			} else {
				Xlog.d(TAG, "write from file succeed");
			}

			break;

		default:
			PrintText("GetOpenFileNameEx return null");
			break;
		}
	}

    protected void onStop() {
            super.onStop();
            if(!normalBackgroundFlag)
            {
            	finish(); //this should not be called because we need to get result from FileList activity, if this causes other side effect later, we can add a flag to judge whether this function call is caused by user's click on "burn from file" button
            }
            else
            {
            	Xlog.d(TAG, "onStop - normal call");
            }
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
