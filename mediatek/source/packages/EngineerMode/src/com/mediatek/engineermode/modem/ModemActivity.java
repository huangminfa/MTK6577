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

package com.mediatek.engineermode.modem;

import com.mediatek.engineermode.R;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.view.View;
import android.widget.RadioButton;

public class ModemActivity extends Activity {

	public final static String TAG = "EM/MODEM";
	private RadioGroup mGpMemoryDump;
	private RadioButton mRMemoryDumpON;
	private RadioButton mRMemoryDumpOFF;

	private RadioButton mSleepModeON;
	private RadioButton mSleepModeOFF;

	private RadioButton mDcmON;
	private RadioButton mDcmOFF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modem_activity);

		mGpMemoryDump = (RadioGroup) findViewById(R.id.Modem_memorydump);

		mRMemoryDumpON = (RadioButton) findViewById(R.id.memory_dump_on_radio);
		mRMemoryDumpOFF = (RadioButton) findViewById(R.id.memory_dump_off_radio);

		mSleepModeON = (RadioButton) findViewById(R.id.sleep_mode_on_radio);
		mSleepModeOFF = (RadioButton) findViewById(R.id.sleep_mode_off_radio);
		mDcmON = (RadioButton) findViewById(R.id.dcm_on_radio);
		mDcmOFF = (RadioButton) findViewById(R.id.dcm_off_radio);

		View.OnClickListener L = new View.OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.memory_dump_on_radio) {
					try {
						if (0 == MemoryDump.SwitchOnOff(1)) {
							Toast.makeText(ModemActivity.this, "Success.",
									Toast.LENGTH_LONG).show();
						} else {
							Xlog.v(TAG, "Memory Dump Set Error!");
							Toast
									.makeText(ModemActivity.this,
											"Memory Dump Set Error!",
											Toast.LENGTH_LONG).show();
							mGpMemoryDump.clearCheck();
						}
					} catch (Exception e) {
						Xlog.e(TAG, "Memory Dump Set Exception!");
						Toast
								.makeText(ModemActivity.this,
										"Memory Dump Set Exception!",
										Toast.LENGTH_LONG).show();
						mGpMemoryDump.clearCheck();
					}

				} else if (v.getId() == R.id.memory_dump_off_radio) {
					try {
						if (0 == MemoryDump.SwitchOnOff(0)) {
							Toast.makeText(ModemActivity.this, "Success.",
									Toast.LENGTH_LONG).show();
						} else {
							Xlog.v(TAG, "Memory Dump Set Error!");
							Toast
									.makeText(ModemActivity.this,
											"Memory Dump Set Error!",
											Toast.LENGTH_LONG).show();
							mGpMemoryDump.clearCheck();
						}
					} catch (Exception e) {
						Xlog.e(TAG, "Memory Dump Set Exception!");
						Toast
								.makeText(ModemActivity.this,
										"Memory Dump Set Exception!",
										Toast.LENGTH_LONG).show();
						mGpMemoryDump.clearCheck();
					}
				} else {
					// do nothing.
				}
			}
		};
		mRMemoryDumpON.setOnClickListener(L);
		mRMemoryDumpOFF.setOnClickListener(L);

		Xlog.v("SleepMode", "before Memory dump");

		int state = MemoryDump.GetState();
		if (1 == state) {
			mGpMemoryDump.check(R.id.memory_dump_on_radio);
		} else if (0 == state) {
			mGpMemoryDump.check(R.id.memory_dump_off_radio);
		} else {
			Xlog.v(TAG, "MemoryDump.GetState() error");
			Toast.makeText(this, "Memory Dump State Unknown.",
					Toast.LENGTH_LONG).show();
		}

		View.OnClickListener listenner = new View.OnClickListener() {

			public void onClick(View v) {

				switch (v.getId()) {
				case R.id.sleep_mode_on_radio:
					boolean result1 = SleepMode.setSleepMode(1);
					if (result1) {
						Toast
								.makeText(ModemActivity.this,
										"set sleep mode on success",
										Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ModemActivity.this,
								"set sleep mode on failed", Toast.LENGTH_SHORT)
								.show();
						return;
					}
					break;
				case R.id.sleep_mode_off_radio:
					boolean result2 = SleepMode.setSleepMode(0);
					if (result2) {
						Toast.makeText(ModemActivity.this,
								"set sleep mode off success",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast
								.makeText(ModemActivity.this,
										"set sleep mode off failed",
										Toast.LENGTH_SHORT).show();
						return;
					}
					break;
				case R.id.dcm_on_radio:
					boolean result3 = SleepMode.setDCM(1);
					if (result3) {
						Toast.makeText(ModemActivity.this,
								"set DCM on success", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(ModemActivity.this, "set DCM on failed",
								Toast.LENGTH_SHORT).show();
						return;
					}
					break;
				case R.id.dcm_off_radio:
					boolean result4 = SleepMode.setDCM(0);
					if (result4) {
						Toast.makeText(ModemActivity.this,
								"set DCM off success", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(ModemActivity.this,
								"set DCM off failed", Toast.LENGTH_SHORT)
								.show();
						return;
					}
					break;

				}
			}
		};

		int sleepMode = SleepMode.getSleepMode();
		Xlog.v("SleepMode", "SleepMode.getSleepMode() = " + sleepMode);
		if (1 == sleepMode) {
			mSleepModeON.setChecked(true);
		} else {
			mSleepModeOFF.setChecked(true);
		}
		int dcm = SleepMode.getDCM();
		Xlog.v("SleepMode", "SleepMode.getDCM() = " + dcm);
		if (1 == dcm) {
			mDcmON.setChecked(true);
		} else {
			mDcmOFF.setChecked(true);
		}
		mSleepModeON.setOnClickListener(listenner);
		mSleepModeOFF.setOnClickListener(listenner);
		mDcmON.setOnClickListener(listenner);
		mDcmOFF.setOnClickListener(listenner);
	}

}
