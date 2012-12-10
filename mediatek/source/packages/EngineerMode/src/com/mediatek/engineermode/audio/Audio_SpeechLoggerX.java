/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.engineermode.audio;

import java.io.IOException;

import com.mediatek.engineermode.*;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioSystem;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import com.mediatek.engineermode.Elog;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.TextView;
import android.os.Environment;
import com.mediatek.featureoption.FeatureOption;

public class Audio_SpeechLoggerX extends Activity {
	private final String TAG = "EM/Audio_SpeechLogger";
	public static final String ENGINEER_MODE_PREFERENCE = "engineermode_audiolog_preferences";
	public static final String EPL_STATUS = "epl_status";

	private CheckBox mCKSpeechLogger;
	private CheckBox mCKVOIPLogger;
	// private CheckBox mCKSpeechPlay;
	private CheckBox mCKCTM4WAY;
	private TextView mCKCTM4WAYText;

	// Added by mtk54045 2012.05.22, To simplify the way customers set the
	// Speech logger
	private RadioButton mCKBEPL;
	private RadioButton mCKBNormalVm;

	// Added by mtk54045 2012.05.22, To trigger audio driver to dump speech
	// debug info into ap side log
	private Button mDumpSpeechInfo;
	private View mSpliteView;

	// private Boolean mIsEnable = false;
	// private Boolean mIsVOIPEnable = false;
	byte data[];
	private final int DATA_SIZE = 1444;
	private final int VM_LOG_POS = 1440;
	private int VM_LOG_STATE = 0;
	// used for Speech Logger
	private final int SET_SPEECH_VM_ENABLE = 0x60;
	private final int SET_DUMP_SPEECH_DEBUG_INFO = 0x61;

	private boolean isForRefresh = false; // Sloved radiobutton can not checked

	// problem

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_speechloggerx);

		mCKSpeechLogger = (CheckBox) findViewById(R.id.Audio_SpeechLogger_Enable);
		mCKVOIPLogger = (CheckBox) findViewById(R.id.Audio_VOIPLogger_Enable);
		// mCKSpeechPlay = (CheckBox)
		// findViewById(R.id.Audio_SpeechLogger_Play);
		mCKCTM4WAY = (CheckBox) findViewById(R.id.Audio_CTM4WAYLogger_Enable);
		mCKCTM4WAYText = (TextView) findViewById(R.id.Audio_CTM4WAYLogger_EnableText);

		mCKBEPL = (RadioButton) findViewById(R.id.Audio_SpeechLogger_EPL);
		mCKBNormalVm = (RadioButton) findViewById(R.id.Audio_SpeechLogger_Normalvm);
		mDumpSpeechInfo = (Button) findViewById(R.id.Dump_Speech_DbgInfo);
		mSpliteView = (View) this.findViewById(R.id.Audio_View1);

		if (mCKSpeechLogger == null || mCKBEPL == null || mCKBNormalVm == null
				|| mCKVOIPLogger == null || mCKCTM4WAY == null) {
			Elog.e(TAG, "clocwork worked...");
			// not return and let exception happened.
		}

		if (!ChipSupport.IsFeatureSupported(ChipSupport.MTK_TTY_SUPPORT)) {
			mCKCTM4WAY.setVisibility(View.GONE);
			mCKCTM4WAYText.setVisibility(View.GONE);
			mSpliteView.setVisibility(View.GONE);
		}
		SharedPreferences preferences = getSharedPreferences(
				ENGINEER_MODE_PREFERENCE, Audio_SpeechLoggerX.MODE_WORLD_READABLE);
		int eplStatus = preferences.getInt(EPL_STATUS, 1);
		int result = 0;
		if (eplStatus == 1) {
			result = AudioSystem.SetAudioCommand(SET_SPEECH_VM_ENABLE, 1);
		} else {
			result = AudioSystem.SetAudioCommand(SET_SPEECH_VM_ENABLE, 0);
		}
		if (result == -1) {
			Elog.i(TAG, "init mCKBEPL parameter failed");
		}
		data = new byte[DATA_SIZE];
		int ret = AudioSystem.GetEMParameter(data, DATA_SIZE);
		if (ret != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Get data error");
			builder.setMessage("Get audio data error.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			Elog.i(TAG, "Audio_SpeechLogger GetEMParameter return value is : "
					+ ret);
		}

		VM_LOG_STATE = ShortToInt(data[VM_LOG_POS], data[VM_LOG_POS + 1]);
		Elog.i(TAG, "Audio_SpeechLogger GetEMParameter return value is : "
				+ VM_LOG_STATE);

		if ((VM_LOG_STATE & 0x01) != 0) {
			mCKSpeechLogger.setChecked(true);
			mCKBEPL.setEnabled(true);
			mCKBNormalVm.setEnabled(true);
			if (eplStatus == 1) {
				mCKBEPL.setChecked(true);
			} else {
				mCKBNormalVm.setChecked(true);
			}
		} else {
			mCKSpeechLogger.setChecked(false);
			mCKBEPL.setEnabled(false);
			mCKBNormalVm.setEnabled(false);
			mCKBEPL.setChecked(false);
			mCKBNormalVm.setChecked(false);
		}

		if ((VM_LOG_STATE & 0x02) != 0) {
			mCKCTM4WAY.setChecked(true);
		} else {
			mCKCTM4WAY.setChecked(false);
		}

		if (getVOIP() == 0) {
			mCKVOIPLogger.setChecked(false);
			// mIsVOIPEnable = false;
		} else {
			mCKVOIPLogger.setChecked(true);
			// mIsVOIPEnable = true;
		}

		mCKSpeechLogger.setOnCheckedChangeListener(mCheckedListener);
		mCKVOIPLogger.setOnCheckedChangeListener(mCheckedListener);
		// mCKSpeechPlay.setOnCheckedChangeListener(mCheckedListener);
		mCKCTM4WAY.setOnCheckedChangeListener(mCheckedListener);

		mCKBEPL.setOnCheckedChangeListener(mCheckedListener);
		mCKBNormalVm.setOnCheckedChangeListener(mCheckedListener);
		mDumpSpeechInfo.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Elog.d(TAG, "On Click  mDumpSpeechInfo button.");
				int ret = AudioSystem.SetAudioCommand(
						SET_DUMP_SPEECH_DEBUG_INFO, 1);
				if (ret == -1) {
					Elog.i(TAG, "set mDumpSpeechInfo parameter failed");
				}
			}

		});
	}

	private int getVOIP() {
		String[] cmdx = { "/system/bin/sh", "-c",
				"cat /data/data/com.mediatek.engineermode/sharefile/audio_voip" }; // file
		int ret = 0;
		try {
			ret = ShellExe.execCommand(cmdx);
			if (0 == ret) {
				// Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
			} else {
				// Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
				return 0;
			}
		} catch (IOException e) {
			Elog.e(TAG, e.toString());
			return 0;
		}
		return Integer.valueOf(ShellExe.getOutput());
	}

	private boolean setVOIP(int n) {
		String[] cmd = { "/system/bin/sh", "-c",
				"mkdir /data/data/com.mediatek.engineermode/sharefile" }; // file

		int ret;
		try {
			ret = ShellExe.execCommand(cmd);
			if (0 == ret) {
				// Toast.makeText(this, "mkdir ok", Toast.LENGTH_LONG).show();
			} else {
				// Toast.makeText(this, "file exists",
				// Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			Elog.e(TAG, e.toString());
			return false;
		}

		String[] cmdx = {
				"/system/bin/sh",
				"-c",
				"echo "
						+ n
						+ " > /data/data/com.mediatek.engineermode/sharefile/audio_voip " }; // file

		try {
			ret = ShellExe.execCommand(cmdx);
			if (0 == ret) {
				// Toast.makeText(this, "Success.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			Elog.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	private Boolean CheckSDCardIsAvaliable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_REMOVED)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("SD Card not available");
			builder.setMessage("Please insert SD Card.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			return false;
		}

		String state = Environment.getExternalStorageState();
		Elog.i(TAG, "Environment.getExternalStorageState() is : " + state);

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_SHARED)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("SD card is busy");
			builder.setMessage("Sorry, your SD card is busy.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			return false;
		}
		return true;
	}

	private CheckBox.OnCheckedChangeListener mCheckedListener = new CheckBox.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean Checked) {

			SharedPreferences preferences = getSharedPreferences(
					ENGINEER_MODE_PREFERENCE, Audio_SpeechLoggerX.MODE_WORLD_READABLE);
			Editor edit = preferences.edit();
			if (buttonView == mCKSpeechLogger) {
				if (Checked) {
					Elog.d(TAG, "mCKSpeechLogger checked");
					if (!CheckSDCardIsAvaliable()) {
						Elog.d(TAG, "mCKSpeechLogger checked 111");
						mCKSpeechLogger.setChecked(false);
						mCKBEPL.setEnabled(false);
						mCKBNormalVm.setEnabled(false);
						return;
					}

					mCKBEPL.setEnabled(true);
					mCKBNormalVm.setEnabled(true);
					isForRefresh = true;
					mCKBNormalVm.setChecked(true);
					mCKBEPL.setChecked(true);
					data[VM_LOG_POS] |= 0x01;
					int index = AudioSystem.SetEMParameter(data, DATA_SIZE);
					if (index != 0) {
						Elog.i(TAG, "set mAutoVM parameter failed");
					}
				} else {
					Elog.d(TAG, "mCKSpeechLogger unchecked");
					if (mCKBEPL.isChecked()) {
						mCKBEPL.setChecked(false);
					}
					if (mCKBNormalVm.isChecked()) {
						mCKBNormalVm.setChecked(false);
					}

					mCKBEPL.setEnabled(false);
					mCKBNormalVm.setEnabled(false);
					int ret = AudioSystem.SetAudioCommand(SET_SPEECH_VM_ENABLE,
							0);
					if (ret == -1) {
						Elog.i(TAG, "set mCKBEPL parameter failed 1");
					}
					edit.putInt(EPL_STATUS, 0);
					edit.commit();
					data[VM_LOG_POS] &= (~0x01);
					int index = AudioSystem.SetEMParameter(data, DATA_SIZE);
					if (index != 0) {
						Elog.i(TAG, "set mAutoVM parameter failed");
					}
				}
			} else if (buttonView == mCKCTM4WAY) {
				if (Checked) {
					data[VM_LOG_POS] |= 0x02;
					VM_LOG_STATE |= 0x02;
					Elog.d(TAG, "E VM_LOG_STATE " + VM_LOG_STATE);
				} else {
					data[VM_LOG_POS] &= (~0x02);
					VM_LOG_STATE &= (~0x02);

					Elog.d(TAG, "D VM_LOG_STATE " + VM_LOG_STATE);
				}
				int index = AudioSystem.SetEMParameter(data, DATA_SIZE);
				if (index != 0) {
					Elog.i(TAG, "set CTM4WAY parameter failed");
				}
			}
			// else if (buttonView == mCKSpeechPlay) {
			// if (Checked) {
			// if (!CheckSDCardIsAvaliable()) {
			// mCKSpeechPlay.setChecked(false);
			// return;
			// }
			// Intent intent = new Intent(Audio_SpeechLoggerX.this,
			// Audio_LogPlayer.class);
			// Audio_SpeechLoggerX.this.startActivity(intent);
			// }
			// }
			else if (buttonView == mCKVOIPLogger) {
				if (Checked) {
					Elog.d(TAG, "mCKVOIPLogger checked");
					setVOIP(1);
				} else {
					Elog.d(TAG, "mCKVOIPLogger Unchecked");
					setVOIP(0);
				}
			} else if (buttonView == mCKBEPL) {
				if (Checked) {
					Elog.d(TAG, "mCKBEPL checked");
					int ret = AudioSystem.SetAudioCommand(SET_SPEECH_VM_ENABLE,
							1);
					if (ret == -1) {
						Elog.i(TAG, "set mCKBEPL parameter failed");
					}
					edit.putInt(EPL_STATUS, 1);
					edit.commit();
				} else {
					Elog.d(TAG, "mCKBEPL unchecked");
				}
			} else if (buttonView == mCKBNormalVm) {
				if (Checked) {
					Elog.d(TAG, "mCKBNormalVm checked");
					if (!isForRefresh) {
						Elog.d(TAG, "mCKBNormalVm checked ok");
						int ret = AudioSystem.SetAudioCommand(
								SET_SPEECH_VM_ENABLE, 0);
						if (ret == -1) {
							Elog.i(TAG, "set mCKBNormalVm parameter failed");
						}
						edit.putInt(EPL_STATUS, 0);
						edit.commit();
					} else {
						isForRefresh = false;
					}
				} else {
					Elog.d(TAG, "mCKBNormalVm unchecked");
				}
			}
		}
	};

	private int ShortToInt(byte low, byte high) {
		int pad = 256; // turn byte to unsigned byte.
		int ii = 0xFF & (high + pad);
		int highByte = 256 * ii;

		int lowByte = 0xFF & (low + pad);

		return highByte + lowByte;
	}

}
