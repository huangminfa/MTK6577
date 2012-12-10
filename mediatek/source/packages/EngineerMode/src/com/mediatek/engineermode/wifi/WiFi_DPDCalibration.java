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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

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
import android.content.Intent;
import android.view.LayoutInflater;
public class WiFi_DPDCalibration extends Activity implements OnClickListener {

	private final String TAG = "EM/WiFi_DPD";
	private final String FILE_PATH = "/data/data/com.mediatek.engineermode/DPD";//"/data/data/com.mediatek.engineermode/DPD";
	
	private Button mCalibrationBtn = null;;
	private Button mReadBtn = null;;
	private Button mImportBtn = null;;
	private Button mSaveBtn = null;;
	private EditText mDPDText = null;
	private WiFiStateManager mWiFiStateManager = null;
	private String mStrDPDParameters = null;
	private long mlDPDLength = 0;
	private long chipID = 0x00;
	private boolean normalBackgroundFlag = false;
	private void Loge(String errInfo)
	{
		Xlog.w(TAG, errInfo);
	}
	
	private void Logd(String errInfo)
	{
		Xlog.d(TAG, errInfo);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logd("-->onCreate: ");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_dpd_calibration);

		
		mCalibrationBtn = (Button) findViewById(R.id.WiFi_DPD_Calibration);
		mReadBtn = (Button) findViewById(R.id.WiFi_DPD_Read);
		mImportBtn = (Button) findViewById(R.id.WiFi_DPD_Import);
		mSaveBtn = (Button) findViewById(R.id.WiFi_DPD_Save);
		mDPDText = (EditText) findViewById(R.id.WiFi_DPD_Info);
		if(null != mCalibrationBtn)
		{
			mCalibrationBtn.setOnClickListener(this);
		}
		if(null != mReadBtn)
		{
			mReadBtn.setOnClickListener(this);
		}
		if(null != mImportBtn)
		{
			mImportBtn.setOnClickListener(this);
		}
		if(null != mSaveBtn)
		{
			mSaveBtn.setOnClickListener(this);
		}
		
		File myFile = new File(FILE_PATH);
		if (!myFile.exists()) {

			if (myFile.mkdirs()) {
				Xlog.d(TAG, "create dir succeed" + myFile.toString());
			} else {
				Xlog.w(TAG, "create dir failed" + myFile.toString());
			}
		}
	}

	public void onClick(View v) {
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
		Logd("-->onClick: " + v);
		if(v.equals(mCalibrationBtn))
		{
			//do DPD calibration
			doCalibration();
		}
		else if(v.equals(mReadBtn))
		{
			//read DPD parameters
			readDPDParameters();
		}
		else if(v.equals(mImportBtn))
		{
			//import DPD from file
			importDPDParameters();
		}
		else if(v.equals(mSaveBtn))
		{
			//save DPD to file
			saveDPDParameters();
		}
		else
		{
			//error happens
			Loge("onClick: error");
		}
		
	}
	
	protected void onStart() {
		Logd("-->onStart");
		super.onStart();
		checkWiFiChipState();
		/*
                if (!EMWifi.isIntialed) {
                	//WiFi is for MT6620 only
                        chipID = EMWifi.initial();
		                if (chipID != 0x6620) {
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
		Logd("-->onStop");
		super.onStop();
		if(!normalBackgroundFlag)
		{
			finish(); //this should not be called because we need to get result from FileList activity, if this causes other side effect later, we can add a flag to judge whether this function call is caused by user's click on "import DPD from file" button
		}
		 else
            {
            	Xlog.d(TAG, "onStop - normal call");
            }
	}
	
	protected void onDestroy() {
		Logd("-->onDestroy");
		super.onDestroy();
	}
	
	//handle the DPD calibration button event
	private void doCalibration()
	{
		Logd("-->doCalibration");
		//enable DPD function first
		EMWifi.setATParam(28, 1);//do i need to do this operation before start DPD calibration ?	
		//set Tx gain for DPD calibration procedure
		EMWifi.setATParam(27, 0);
		//start DPD calibration command
		//evaluate how much time this operation will take, then decide whether a ProgressDialog need to show or not
		EMWifi.setATParam(1, 9); 
	}
	
	//handle the read DPD parameters button event
	private void readDPDParameters()
	{
		long[] i4Tmp = new long[1];
		Logd("-->readDPDParameters");	
		ClearText();
		if( 0 != EMWifi.getDPDLength(i4Tmp))
		{
			PrintText("read DPD parameters length failed");
			return;
		}
		Logd("read DPD parameters length result:" + i4Tmp[0]);
		int[] parameter = new int[(int)i4Tmp[0]];
		dpdReadIntBySize(parameter, (int)i4Tmp[0]);
		
	}
	
	//handle the import DPD parameters button event
	private void importDPDParameters()
	{
		Logd("-->importDPDParameters");	
		ClearText();
		Intent intent = new Intent(WiFi_DPDCalibration.this, FileList.class);
		intent.putExtra("identifier", "DPD");
		normalBackgroundFlag = true;
		startActivityForResult(intent, 1);
		
		
	}
	
	//handle the save DPD parameters button event
	private void saveDPDParameters()
	{
		Logd("-->saveDPDParameters");
		ClearText();
		saveDPDParametersToFile();	
	}
	
	private void saveDPDParametersToFile() {
		Logd("-->saveDPDParametersToFile");
		LayoutInflater inflater = (LayoutInflater) WiFi_DPDCalibration.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.mydialog, null);
		final EditText text = (EditText) layout.findViewById(R.id.save_file_name);
		AlertDialog.Builder builder = new AlertDialog.Builder(WiFi_DPDCalibration.this);
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

				String mSaveFileName = name.toString();
				PrintText("SaveFileName = " + mSaveFileName + "\n");
				if (!createFile(mSaveFileName)) {
					//toast is not needed, result will be shown in the EditText
					//Toast.makeText(WiFi_DPDCalibration.this,"create file " + mSaveFileName +" failed", Toast.LENGTH_LONG).show();
					return;
				}
				//read DPD calibration length
				long[] i4DPDLength = new long[1];
				long[] i4DPDdata = new long[1];
				int iDPDParameter = 0;
				long i = 0;
				if( 0 != EMWifi.getDPDLength(i4DPDLength))
				{
					PrintText("read DPD parameters length failed");
					Xlog.w(TAG, "read DPD parameters length failed");
					return;
				}
				Logd("i4DPDLength = " + i4DPDLength[0]);
				
				
				for (i = 0; (i << 2) < i4DPDLength[0]; i++) {
					if (EMWifi.readDPD32((i << 2), i4DPDdata) != 0) {
						Xlog.w(TAG, "read iDPDParameter[" + i + "]" + " failed");
						return;
					}
					/*
					File mfile = new File(FILE_PATH, mSaveFileName);
					FileOutputStream out;
					
					byte[] buffer = new byte[4];
					
					buffer[0] = (byte)(iDPDParameter& 0x000000ff);
					buffer[1] = (byte)((iDPDParameter & 0x0000ff00) >> 8);
					buffer[2] = (byte)((iDPDParameter & 0x00ff0000) >> 16);
					buffer[3] = (byte)((iDPDParameter & 0xff000000) >> 24);
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
					*/
					iDPDParameter = (int) i4DPDdata[0];
					Xlog.d(TAG, String.format("iDPDParameter[0x%04x] = 0x%04x",  i << 2, iDPDParameter));
					String result = String.format("%1$04x", iDPDParameter)+ "\r\n";
					// Log.d(TAG, result);
					if (!SaveAsFile(mSaveFileName, result)) {
						return;
					}
					
				}
			}
		});

		alertDialog = builder.create();
		
		alertDialog.show();
	}
	
	private void PrintText(String text) {
		mDPDText.append(text);
		return;
	}

	private void ClearText(){
		mDPDText.setText("");
		return;
	}
	
	private boolean createFile(String filename) {

		File mFile = new File(FILE_PATH + "/" + filename);
		try {
			mFile.createNewFile();
			Xlog.d(TAG, "create file succeed");
		} catch (IOException e) {
			PrintText("Create file "+ filename + " failed.\n");
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
			long[] size = new long[1];
			if (EMWifi.getDPDLength(size) != 0) {
				// PrintText("GetOpenFileNameEx = " + filename);
				return;
			}
			int i4RetVal = EMWifi.setDPDFromFile(FILE_PATH + "/" + filename);
			if (i4RetVal != 0) {
				PrintText("setDPDFromFile failed, error = " + i4RetVal);
				Loge("write from file failed");
			} else {
				Logd("write from file succeed");
			}
			break;

		default:
			PrintText("GetOpenFileNameEx return null");
			break;
		}
	}
	
	private boolean dpdReadIntBySize(int[] pau2EepValue, int u2EepSizeW) {
			long[] u4Tmp = new long[1];
			int u2Tmp = 0;
			String str = null;
			ClearText();
			for (u2Tmp = 0; (u2Tmp  << 2)< u2EepSizeW; u2Tmp++) {
				if (EMWifi.readDPD32((u2Tmp << 2), u4Tmp) != 0) {
					str = String.format("0x%04x", u2Tmp << 2);
					PrintText("DPD read failed at word offset "	+ str + "\n");
					return false;
				} else {
					str = String.format("%04x: 0x%08x", u2Tmp << 2, u4Tmp[0]);
					Xlog.d(TAG, str);
					PrintText(str + "\n");
					pau2EepValue[u2Tmp] = (int) (u4Tmp[0]);
				}
			}
	
			return true;
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
