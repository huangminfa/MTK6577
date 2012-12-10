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

package com.mediatek.engineermode.audio;

import com.mediatek.engineermode.Elog;

import com.mediatek.engineermode.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.media.AudioSystem;
import com.mediatek.featureoption.FeatureOption;

/**
 * 
 * @author mtk54045 To make user mode can dump audio data Time: 2012.05.22
 */
public class Audio_AudioLogger extends Activity {

	private static final String TAG = "EM/Audio_AudioLogger";

	private CheckBox mAudioStrmOtptDump;
	private CheckBox mAudioMixerBufDump;
	private CheckBox mAudioTrackBufDump;
	private CheckBox mAudioA2DPStrmDump;
	private CheckBox mAudioStrmInptDump;
//	private CheckBox mAudioIdleRecdVMDump;
	private Button mDumpAudioDbgInfo;

	// used for Audio Logger
	private final int SET_DUMP_AUDIO_DEBUG_INFO = 0x62;
	private final int SET_DUMP_AUDIO_STREAM_OUT = 0x63;
	private final int GET_DUMP_AUDIO_STREAM_OUT = 0x64;
	private final int SET_DUMP_AUDIO_MIXER_BUF = 0x65;
	private final int GET_DUMP_AUDIO_MIXER_BUF = 0x66;
	private final int SET_DUMP_AUDIO_TRACK_BUF = 0x67;
	private final int GET_DUMP_AUDIO_TRACK_BUF = 0x68;
	private final int SET_DUMP_A2DP_STREAM_OUT = 0x69;
	private final int GET_DUMP_A2DP_STREAM_OUT = 0x6A;
	private final int SET_DUMP_AUDIO_STREAM_IN = 0x6B;
	private final int GET_DUMP_AUDIO_STREAM_IN = 0x6C;
	private final int SET_DUMP_IDLE_VM_RECORD = 0x6D;
	private final int GET_DUMP_IDLE_VM_RECORD = 0x6E;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_audiologger);

		mAudioStrmOtptDump = (CheckBox) findViewById(R.id.Audio_StrmOtpt_Dump);
		mAudioMixerBufDump = (CheckBox) findViewById(R.id.Audio_MixerBuf_Dump);
		mAudioTrackBufDump = (CheckBox) findViewById(R.id.Audio_TrackBuf_Dump);
		mAudioA2DPStrmDump = (CheckBox) findViewById(R.id.Audio_A2DPOtpt_Dump);
		mAudioStrmInptDump = (CheckBox) findViewById(R.id.Audio_StrmInpt_Dump);
//		mAudioIdleRecdVMDump = (CheckBox) findViewById(R.id.Audio_IDModeVM_Dump);
		mDumpAudioDbgInfo = (Button) findViewById(R.id.Dump_Audio_DebgInfo);
		
//		if (!FeatureOption.MTK_AUDIO_HD_REC_SUPPORT) {
//			mAudioIdleRecdVMDump.setVisibility(View.GONE);
//		}

		if (AudioSystem.GetAudioCommand(GET_DUMP_AUDIO_STREAM_OUT) == 1) {
			mAudioStrmOtptDump.setChecked(true);
		} else {
			mAudioStrmOtptDump.setChecked(false);
		}
		if (AudioSystem.GetAudioCommand(GET_DUMP_AUDIO_MIXER_BUF) == 1) {
			mAudioMixerBufDump.setChecked(true);
		} else {
			mAudioMixerBufDump.setChecked(false);
		}
		if (AudioSystem.GetAudioCommand(GET_DUMP_AUDIO_TRACK_BUF) == 1) {
			mAudioTrackBufDump.setChecked(true);
		} else {
			mAudioTrackBufDump.setChecked(false);
		}
		if (AudioSystem.GetAudioCommand(GET_DUMP_A2DP_STREAM_OUT) == 1) {
			mAudioA2DPStrmDump.setChecked(true);
		} else {
			mAudioA2DPStrmDump.setChecked(false);
		}
		if (AudioSystem.GetAudioCommand(GET_DUMP_AUDIO_STREAM_IN) == 1) {
			mAudioStrmInptDump.setChecked(true);
		} else {
			mAudioStrmInptDump.setChecked(false);
		}
//		if (AudioSystem.GetAudioCommand(GET_DUMP_IDLE_VM_RECORD) == 1) {
//			mAudioIdleRecdVMDump.setChecked(true);
//		} else {
//			mAudioIdleRecdVMDump.setChecked(false);
//		}

		mAudioStrmOtptDump.setOnCheckedChangeListener(mCheckedListener);
		mAudioMixerBufDump.setOnCheckedChangeListener(mCheckedListener);
		mAudioTrackBufDump.setOnCheckedChangeListener(mCheckedListener);
		mAudioA2DPStrmDump.setOnCheckedChangeListener(mCheckedListener);
		mAudioStrmInptDump.setOnCheckedChangeListener(mCheckedListener);
//		mAudioIdleRecdVMDump.setOnCheckedChangeListener(mCheckedListener);
		mDumpAudioDbgInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int ret = AudioSystem.SetAudioCommand(
						SET_DUMP_AUDIO_DEBUG_INFO, 1);
				if (ret == -1) {
					Elog.i(TAG, "set mDumpAudioDbgInfo parameter failed");
				}
			}
		});
	}

	private CheckBox.OnCheckedChangeListener mCheckedListener = new CheckBox.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int ret;
			if (buttonView == mAudioStrmOtptDump) {
				if (isChecked) {
					Elog.d(TAG, "Checked mAudioStrmOtptDump");
					if (!CheckSDCardIsAvaliable()) {
						mAudioStrmOtptDump.setChecked(false);
						return;
					}
					ret = AudioSystem.SetAudioCommand(
							SET_DUMP_AUDIO_STREAM_OUT, 1);
				} else {
					Elog.d(TAG, "UnChecked mAudioStrmOtptDump");
					ret = AudioSystem.SetAudioCommand(
							SET_DUMP_AUDIO_STREAM_OUT, 0);
				}
				if (ret == -1) {
					Elog.i(TAG, "set mAudioStrmOtptDump parameter failed");
				}
			} else if (buttonView == mAudioMixerBufDump) {
				if (isChecked) {
					Elog.d(TAG, "Checked mAudioMixerBufDump");
					if (!CheckSDCardIsAvaliable()) {
						mAudioMixerBufDump.setChecked(false);
						return;
					}
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_MIXER_BUF,
							1);
				} else {
					Elog.d(TAG, "UnChecked mAudioMixerBufDump");
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_MIXER_BUF,
							0);
				}
				if (ret == -1) {
					Elog.i(TAG, "set mAudioMixerBufDump parameter failed");
				}
			} else if (buttonView == mAudioTrackBufDump) {
				if (isChecked) {
					Elog.d(TAG, "Checked mAudioTrackBufDump");
					if (!CheckSDCardIsAvaliable()) {
						mAudioTrackBufDump.setChecked(false);
						return;
					}
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_TRACK_BUF,
							1);
				} else {
					Elog.d(TAG, "UnChecked mAudioTrackBufDump");
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_TRACK_BUF,
							0);
				}
				if (ret == -1) {
					Elog.i(TAG, "set mAudioTrackBufDump parameter failed");
				}
			} else if (buttonView == mAudioA2DPStrmDump) {
				if (isChecked) {
					Elog.d(TAG, "Checked mAudioA2DPStrmDump");
					if (!CheckSDCardIsAvaliable()) {
						mAudioA2DPStrmDump.setChecked(false);
						return;
					}
					ret = AudioSystem.SetAudioCommand(SET_DUMP_A2DP_STREAM_OUT,
							1);
				} else {
					Elog.d(TAG, "UnChecked mAudioA2DPStrmDump");
					ret = AudioSystem.SetAudioCommand(SET_DUMP_A2DP_STREAM_OUT,
							0);
				}
				if (ret == -1) {
					Elog.i(TAG, "set mAudioA2DPStrmDump parameter failed");
				}
			} else if (buttonView == mAudioStrmInptDump) {
				if (isChecked) {
					Elog.d(TAG, "Checked mAudioStrmInptDump");
					if (!CheckSDCardIsAvaliable()) {
						mAudioStrmInptDump.setChecked(false);
						return;
					}
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_STREAM_IN,
							1);
				} else {
					Elog.d(TAG, "UnChecked mAudioStrmInptDump");
					ret = AudioSystem.SetAudioCommand(SET_DUMP_AUDIO_STREAM_IN,
							0);
				}
				if (ret == -1) {
					Elog.i(TAG, "set mAudioStrmInptDump parameter failed");
				}
			} 
//			else if (buttonView == mAudioIdleRecdVMDump) {
//				if (isChecked) {
//					Elog.d(TAG, "Checked mAudioIdleRecdVMDump");
//					if (!CheckSDCardIsAvaliable()) {
//						mAudioIdleRecdVMDump.setChecked(false);
//						return;
//					}
//					ret = AudioSystem.SetAudioCommand(SET_DUMP_IDLE_VM_RECORD,
//							1);
//				} else {
//					Elog.d(TAG, "UnChecked mAudioIdleRecdVMDump");
//					ret = AudioSystem.SetAudioCommand(SET_DUMP_IDLE_VM_RECORD,
//							0);
//				}
//				if (ret == -1) {
//					Elog.i(TAG, "set mAudioIdleRecdVMDump parameter failed");
//				}
//			}
		}

	};

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
}