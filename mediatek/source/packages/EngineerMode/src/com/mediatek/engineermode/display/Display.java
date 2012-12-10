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

package com.mediatek.engineermode.display;

import com.mediatek.engineermode.R;

import com.mediatek.engineermode.ShellExe;
import com.mediatek.engineermode.emsvr.AFMFunctionCallEx;
import com.mediatek.engineermode.emsvr.FunctionReturn;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

public class Display extends Activity implements OnClickListener {

	private Button mBtnLcdON;
	private Button mBtnLcdOFF;
	private Button mBtnLcmON;
	private Button mBtnLcmOFF;
	private Button mBtnSet;
	private EditText mEdit;

	// private String mount = "mount -t debugfs none /sys/kernel/debug";
	// private String umount = "umount /sys/kernel/debug";
	private String lcdCmdON = "echo 255 > /sys/class/leds/lcd-backlight/brightness";
	private String lcdCmdOFF = "echo 0 > /sys/class/leds/lcd-backlight/brightness";
	// private String lcmCmdON =
	// "echo lcd:on > /sys/kernel/debug/mtkfb && echo lcm:on > /sys/kernel/debug/mtkfb";

	// private String lcmCmdOFF =
	// "echo lcd:off > /sys/kernel/debug/mtkfb && echo lcm:off > /sys/kernel/debug/mtkfb";
	private String dutyFile = "/sys/class/leds/lcd-backlight/duty";

	private static String TAG = "EM-Display";
	private int err_no = 0;
	private final int ERR_OK = 0;
	private final int ERR_ERR = 1;

	public static int LCMPowerON() {
		return FB0_Fucntion(FB0_LCMPowerON);
	}

	public static int LCMPowerOFF() {
		return FB0_Fucntion(FB0_LCMPowerOFF);
	}

	private final static int FB0_LCMPowerON = 4;
	private final static int FB0_LCMPowerOFF = 5;

	private static int FB0_Fucntion(int... param) {
		AFMFunctionCallEx A = new AFMFunctionCallEx();
		boolean result = A
				.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_FB0_IOCTL);
		A.WriteParamNo(param.length);
		for (int i : param) {
			A.WriteParamInt(i);
		}

		if (!result) {
			return -1;
		}

		int valueRet = -1;
		FunctionReturn r;
		do {
			r = A.GetNextResult();
			if (r.returnString == "") {
				break;
			} else {
				if (r.returnString.equalsIgnoreCase("FFFFFFFF")) {
					valueRet = -1;
					break;
				}
				try {
					valueRet = Integer.valueOf(r.returnString);
				} catch (NumberFormatException e) {
					Xlog.e(TAG, r.returnString);
					valueRet = -1;
				}
			}

		} while (r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);

		if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
			// error
			return -1;
		} else {
			return valueRet;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.v(TAG, "-->onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display);

		mBtnLcdON = (Button) findViewById(R.id.Display_lcd_on);
		mBtnLcdOFF = (Button) findViewById(R.id.Display_lcd_off);
		mBtnLcmON = (Button) findViewById(R.id.Display_lcm_on);
		mBtnLcmOFF = (Button) findViewById(R.id.Display_lcm_off);

		mBtnSet = (Button) findViewById(R.id.Display_set);
		mEdit = (EditText) findViewById(R.id.Display_Edit_Value);

		mBtnSet.setOnClickListener(this);
		mBtnLcdON.setOnClickListener(this);
		mBtnLcmON.setOnClickListener(this);
		mBtnLcdOFF.setOnClickListener(this);
		mBtnLcmOFF.setOnClickListener(this);

	}

	@Override
	public void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();

	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");
		super.onPause();

	}

	private int getLastError() {
		return err_no;
	}

	private void setLastError(int err) {
		err_no = err;
	}

	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}

	public void onClick(View arg0) {
		Xlog.v(TAG, "-->onClick");
		// TODO Auto-generated method stub
		try {
			if (arg0.getId() == mBtnSet.getId()) {
				String editString = mEdit.getText().toString();
				if (null == editString || editString.equals("")) {
					Toast.makeText(this, "Please input the value.",
							Toast.LENGTH_LONG).show();
					setLastError(ERR_OK);
					return;
				}

				if (editString.length() > 3
						|| (Integer.valueOf(editString) > 63)) {
					Toast
							.makeText(
									this,
									"The input is not correct. Please input the number between 0 and 63.",
									Toast.LENGTH_LONG).show();
					setLastError(ERR_OK);
					return;
				}

				String[] cmd = { "/system/bin/sh", "-c",
						"echo " + editString + " > " + dutyFile }; // file must
																	// exist//
																	// or wait()
																	// return2
				int ret = ShellExe.execCommand(cmd);
				if (0 == ret) {
					ShowDialog("Set Success", "Set duty " + editString
							+ " succeeded.");
					setLastError(ERR_OK);
				} else {
					ShowDialog("Set Failed", "Set duty " + editString
							+ " Failed.");
					setLastError(ERR_ERR);
				}

			} else if (arg0.getId() == mBtnLcdON.getId()) {
				String[] cmd = { "/system/bin/sh", "-c", lcdCmdON }; // file
																		// must
																		// exist//
																		// or
																		// wait()
																		// return2
				int ret = ShellExe.execCommand(cmd);
				if (0 == ret) {
					ShowDialog("Set Success", "Set LCD backlight ON success");
					setLastError(ERR_OK);
				} else {
					ShowDialog("Set Failed", "Set LCD backlight ON Failed.");
					setLastError(ERR_ERR);
				}

			} else if (arg0.getId() == mBtnLcdOFF.getId()) {
				String[] cmd = { "/system/bin/sh", "-c", lcdCmdOFF }; // file
																		// must
																		// exist//
																		// or
																		// wait()
																		// return2
				int ret = ShellExe.execCommand(cmd);
				if (0 == ret) {
					ShowDialog("Set Success", "Set LCD backlight OFF success");
					setLastError(ERR_OK);
				} else {
					ShowDialog("Set Failed", "Set LCD backlight OFF Failed.");
					setLastError(ERR_ERR);
				}

			} else if (arg0.getId() == mBtnLcmON.getId()) {
				if (-1 == LCMPowerON()) {
					ShowDialog("Set Failed", "Set LCM controller ON Failed.");
					setLastError(ERR_ERR);
				} else {
					ShowDialog("Set Success", "Set LCM controller ON success");
					setLastError(ERR_OK);
				}

			} else if (arg0.getId() == mBtnLcmOFF.getId()) {
				if (-1 == LCMPowerOFF()) {
					ShowDialog("Set Failed", "Set LCM controller OFF Failed.");
					setLastError(ERR_ERR);
				} else {
					ShowDialog("Set Success", "Set LCM controller OFF success");
					setLastError(ERR_OK);
				}
			}

		} catch (IOException e) {
			Xlog.e("MTK", e.toString());
			setLastError(ERR_ERR);
		}
	}

}
