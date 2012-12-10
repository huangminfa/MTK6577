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

package com.mediatek.engineermode.touchscreen;

import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public class TouchScreen_Settings extends Activity implements OnClickListener {

	private class spinnerData {
		public String name;
		public String fullPath;
	}

	// private ToggleButton mBtnSDcard;
	// private ToggleButton mBtnUart;
	private Button mBtnSet;
	private EditText mEdit;
	private Spinner mModeSpinner;
	private ArrayAdapter<String> ModeAdatper;
	private int mModeIndex;
	private ArrayList<spinnerData> category;

	private String[] firstCommand = { "/system/bin/sh", "-c",
	// "echo 1 > /sys/module/tpd_debug/parameters/tpd_em_log" };
			"echo 2 > /sys/module/tpd_setting/parameters/tpd_mode" };

	private String paramFilePath = "/sys/module/tpd_debug/parameters";
	private String paramFilePath1 = "/sys/module/tpd_setting/parameters";
	private String paramTag = "tpd_em_";
	private int err_no = 0;
	private final int ERR_OK = 0;
	private final int ERR_ERR = 1;

	private static String TAG = "EM/TouchScreen/set";

	private boolean isSdcardExist = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.touch_settings);

		// mBtnSDcard = (ToggleButton)
		// findViewById(R.id.TouchScreen_Settings_sdcard);
		// mBtnUart = (ToggleButton)
		// findViewById(R.id.TouchScreen_Settings_uart);

		mBtnSet = (Button) findViewById(R.id.TouchScreen_Settings_TextSet);
		mEdit = (EditText) findViewById(R.id.TouchScreen_Settings_Value);
		mModeSpinner = (Spinner) findViewById(R.id.TouchScreen_Settings_Spinner);
		if (mBtnSet == null || mEdit == null || mModeSpinner == null) {
			Xlog.w(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		ModeAdatper = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		ModeAdatper
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category = getCategory();
		if (null == category) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Warning");
			builder.setMessage("No setting file exist.");
			builder.setCancelable(false);
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							TouchScreen_Settings.this.finish();
						}
					});
			builder.create().show();
			return;
		}

		for (int i = 0; i < category.size(); i++) {
			ModeAdatper.add(category.get(i).name);
		}
		mModeSpinner.setAdapter(ModeAdatper);
		mModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mModeIndex = arg2;
				mEdit.setText(GetFileValue(category.get(mModeIndex).fullPath));
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		mBtnSet.setOnClickListener(this);
		// mBtnSDcard.setOnClickListener(this);
		// mBtnUart.setOnClickListener(this);

	}

	private String GetFileValue(String path) {
		String[] cmd = { "/system/bin/sh", "-c", "cat " + path }; // file must
		// exist or
		// wait()
		// return2
		Xlog.v(TAG, "-->GetFileValue:"+path);
		int ret;
		try {
			ret = TouchScreen_ShellExe.execCommand(cmd);

			if (0 == ret) {
				return TouchScreen_ShellExe.getOutput();
			} else {
				return "N/A";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "N/A";
		}
	}

	@Override
	public void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();
		// final SharedPreferences preferences =
		// this.getSharedPreferences("touch_screen_settings",
		// android.content.Context.MODE_PRIVATE);
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_REMOVED)) {
			// if(preferences.getBoolean("en_sdcard_output", false))
			// {
			// mBtnSDcard.setChecked(true);
			// }
			isSdcardExist = true;
		} else {
			isSdcardExist = false;
			// preferences.edit().putBoolean("en_sdcard_output",
			// false).commit();
		}

		// if(preferences.getString("tpd_em_log", "0").equalsIgnoreCase("1"))
		// {
		// mBtnUart.setChecked(true);
		// }
	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");
		super.onPause();

	}

	private ArrayList<spinnerData> getCategory() {
		File dir = new File(paramFilePath);
		File[] files = dir.listFiles();
		if (files == null) {
			return null;
		}
		ArrayList<spinnerData> result = new ArrayList<spinnerData>();
		for (File f : files) {
			if (f.getName().indexOf(paramTag) == 0) {
				spinnerData data = new spinnerData();
				data.name = f.getName();
				data.fullPath = f.getAbsolutePath();
				result.add(data);
			}

		}
		dir = new File(paramFilePath1);
		files = dir.listFiles();
		if (files == null) {
			return result;
		}
		for (File f : files) {
			if (f.getName().indexOf(paramTag) == 0) {
				spinnerData data = new spinnerData();
				data.name = f.getName();
				data.fullPath = f.getAbsolutePath();
				result.add(data);
			}
		}

		if (result.size() == 0) {
			return null;
		}
		return result;
	}

	/*
	 * private int getLastError() { return err_no; }
	 */
	private void setLastError(int err) {
		err_no = err;
	}

	private volatile static boolean mRun = false;
	private static String currentFileName = null;
	private static final int EVENT_UPDATE = 1;

	class runThread extends Thread {

		public void run() {
			while (mRun) {
				// Log.i("MTHR", "LOOP mRun = "+mRun);
				if (isSdcardExist) {
//					String shell = "cat /proc/tpd_em_log >> " + currentFileName;
					String shell = "cat /sys/module/tpd_debug/parameters/tpd_em_log  >> " + currentFileName;
					Xlog.v(TAG, "run file shell--" + shell);
					String[] cmd2 = { "/system/bin/sh", "-c", shell };
					int ret = 0;
					try {
						ret = TouchScreen_ShellExe.execCommand(cmd2);
						if (0 != ret) {
							Xlog.i(TAG, "cat >> failed!! ");
							// return;
						}
					} catch (IOException e) {
						Xlog.w(TAG, "cat >> failed!!  io exception");
					}
				}

				try {
					sleep(10);
					// Log.i("MTHR", "After sleep");
				} catch (InterruptedException e) {
					Xlog.w(TAG, "sleep(10) >> exception!!!");

				}
			}
			// Toast.makeText(TouchScreen_Settings.this, "Finish file logging.",
			// Toast.LENGTH_LONG).show();
			Message msg = new Message();
			msg.what = EVENT_UPDATE;

			mUpdateHandler.sendMessage(msg);

			Xlog.i(TAG, "Copy /proc/tpd_em_log success");
		}
	}

	public Handler mUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_UPDATE:
				Toast.makeText(TouchScreen_Settings.this,
						"Finish file logging.", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == mBtnSet.getId()) {
			String editString = mEdit.getText().toString();
			if (null == editString || editString.length() == 0) {
				Toast.makeText(this, "Please input the value.",
						Toast.LENGTH_LONG).show();
				setLastError(ERR_OK);
				return;
			}

			// boolean resultOK = false;
			try {
				if (category.get(mModeIndex).name.equals("tpd_em_log_to_fs")) { // if
					// no
					// sdcard
					// exists.
					if (!isSdcardExist) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								this);
						builder.setTitle("Error");
						builder.setMessage("No SD card exists.");
						builder.setPositiveButton("OK", null);
						builder.create().show();
						return;
					}
					final SharedPreferences preferences = this
							.getSharedPreferences("touch_screen_settings",
									android.content.Context.MODE_PRIVATE);
					if (!editString.equals("0"))// open file log
					{
						String curVal = GetFileValue(category.get(mModeIndex).fullPath);
						if (!curVal.equals("0") && !curVal.equals("N/A"))// already
						// open
						// file
						// log.
						{
							Toast.makeText(this, "File Log Already Opened.",
									Toast.LENGTH_LONG).show();
							return;
						}

						runFirstCommand();

						String[] cmd = {
								"/system/bin/sh",
								"-c",
								"echo " + editString + " > " + paramFilePath
										+ "/tpd_em_log" }; // file

						int ret = TouchScreen_ShellExe.execCommand(cmd);
						if (0 == ret) {
							// Toast.makeText(this, "Set tpd_em_log success.",
							// Toast.LENGTH_LONG).show();
							setLastError(ERR_OK);
						} else {
							Toast
									.makeText(
											this,
											"Set tpd_em_log failed. open file log failed.",
											Toast.LENGTH_LONG).show();
							setLastError(ERR_ERR);
							return;
						}

						String[] cmdx = {
								"/system/bin/sh",
								"-c",
								"echo " + editString + " > "
										+ category.get(mModeIndex).fullPath }; // file

						ret = TouchScreen_ShellExe.execCommand(cmdx);
						if (0 == ret) {
							Toast.makeText(this, "open file log success.",
									Toast.LENGTH_LONG).show();
							setLastError(ERR_OK);
						} else {
							Toast.makeText(this, "open file log failed.",
									Toast.LENGTH_LONG).show();
							setLastError(ERR_ERR);
							return;
						}

						File sdcard = Environment.getExternalStorageDirectory();
						File touchLog = new File(sdcard.getParent() + "/"
								+ sdcard.getName() + "/TouchLog/");

						if (!touchLog.isDirectory()) {
							touchLog.mkdirs();
							Xlog.i(TAG, "mkdir " + touchLog.getPath()
									+ " success");
						}
						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd_HH-mm-ss");
						currentFileName = touchLog.getPath() + "/L"
								+ df.format(new Date().getTime());
						String shell = "echo START > " + currentFileName;
						Xlog.i(TAG, "file shell " + shell);
						String[] cmd2 = { "/system/bin/sh", "-c", shell };
						ret = TouchScreen_ShellExe.execCommand(cmd2);
						if (0 != ret) {
							// Log.i("MTH",
							// "Create file failed.(echo ###> failed!! )");
							Toast.makeText(this,
									"Error: Create file in sdcard failed!!",
									Toast.LENGTH_LONG).show();
							return;
						}

						mRun = true;
						new runThread().start();

						Xlog.v(TAG, "thread start mRun = " + mRun);
						Toast.makeText(this, "Start log file to sdcard.",
								Toast.LENGTH_LONG).show();
						Xlog.v(TAG, "Start log file to sdcard.");
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("filename", currentFileName);
						editor.commit();
					} else// close file log
					{
						mRun = false;
						Xlog.i(TAG, "close file log mRun = " + mRun);
						SetCategory("0");
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("filename", "N");
						editor.commit();

					}
				} else if (category.get(mModeIndex).name.equals("tpd_em_log"))// close
																				// uart
																				// log
				{
					if (editString.equals("0")) {
						mRun = false;
						Xlog.i(TAG, "uart close mRun = " + mRun);
						final SharedPreferences preferences = this
								.getSharedPreferences("touch_screen_settings",
										android.content.Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("filename", "N");
						editor.commit();

						runFirstCommand();

						String[] cmd = {
								"/system/bin/sh",
								"-c",
								"echo 0 > " + paramFilePath
										+ "/tpd_em_log_to_fs" };
						int ret = TouchScreen_ShellExe.execCommand(cmd);
						if (0 == ret) {
							// Toast.makeText(this,
							// "Set tpd_em_log_to_fs success.",
							// Toast.LENGTH_LONG).show();
							setLastError(ERR_OK);

						} else {
							Toast
									.makeText(
											this,
											"Set tpd_em_log_to_fs failed. close file log failed.",
											Toast.LENGTH_LONG).show();
							setLastError(ERR_ERR);
							return;
						}

						String[] cmdx = {
								"/system/bin/sh",
								"-c",
								"echo " + editString + " > "
										+ category.get(mModeIndex).fullPath }; // file
						// must
						// exist
						// or
						// wait()
						// return2
						ret = TouchScreen_ShellExe.execCommand(cmdx);
						if (0 == ret) {
							Toast.makeText(this, "Close uart log success.",
									Toast.LENGTH_LONG).show();
							setLastError(ERR_OK);
						} else {
							Toast.makeText(this, "Close uart log failed.",
									Toast.LENGTH_LONG).show();
							setLastError(ERR_ERR);
							return;
						}
					} else {
						SetCategory(editString);
					}
				}

				else {
					SetCategory(editString);
				}

			} catch (IOException e) {
				Xlog.i(TAG, e.toString());
				Toast
						.makeText(
								this,
								"Set ." + category.get(mModeIndex).name
										+ " exception.", Toast.LENGTH_LONG)
						.show();
				setLastError(ERR_ERR);
			}
		}

	}

	private void SetCategory(String editString) throws IOException {
		runFirstCommand();

		String[] cmd = {
				"/system/bin/sh",
				"-c",
				"echo " + editString + " > "
						+ category.get(mModeIndex).fullPath };
		int ret = TouchScreen_ShellExe.execCommand(cmd);
		if (0 == ret) {
			Toast.makeText(this,
					"Set ." + category.get(mModeIndex).name + " success.",
					Toast.LENGTH_LONG).show();
			setLastError(ERR_OK);
		} else {
			Toast.makeText(this,
					"Set ." + category.get(mModeIndex).name + " failed.",
					Toast.LENGTH_LONG).show();
			setLastError(ERR_ERR);
		}
	}

	// private String firstCommand =
	// "adb shell echo 2 > /sys/module/tpd_setting/parameters/tpd_mode";

	// private String[] firstCommand = { "/system/bin/sh",
	// "-c",
	// "echo 2 > /sys/module/tpd_setting/parameters/tpd_mode" };
	public void runFirstCommand() {
		try {

			int ret = TouchScreen_ShellExe.execCommand(firstCommand);

			Xlog.v(TAG, "write tpd_mode result:"+TouchScreen_ShellExe.getOutput());
			if (0 == ret) {
				Toast.makeText(this, "write tpd_mode 2 success.",
						Toast.LENGTH_LONG).show();
				setLastError(ERR_OK);
			} else {
				Toast.makeText(this, "write tpd_mode 2 failed.",
						Toast.LENGTH_LONG).show();
				setLastError(ERR_ERR);
				return;
			}
		} catch (IOException e) {
			Xlog.i(TAG, e.toString());
			Toast.makeText(this, "write tpd_mode 2  exception.",
					Toast.LENGTH_LONG).show();
			setLastError(ERR_ERR);
		}
	}

}
