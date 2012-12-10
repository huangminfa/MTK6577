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

package com.mediatek.engineermode.phone;

import com.mediatek.engineermode.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;

import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;
import android.os.ServiceManager;

public class PhoneAutoTestTool extends Activity implements OnClickListener {

	private static final String MAIN_INTENT_ACTION = "android.phone.extra.VT_AUTO_TEST_TOOL";
	private static final String TAG = "PhoneAutoTestTool";

	private static final int START_REPEAT_MO_TEST = 9999;
	private static final int STOP_REPEAT_MO_TEST = 9998;

	private Button mButton01 = null;
	private Button mButton02 = null;

	private EditText mNumber = null;
	private String mNumberValue = null;

	private EditText mRepeatTime = null;
	private int mRepeatTimeValue;
	private int mRepeatTimeNow;

	private EditText mDuration = null;
	private int mDurationValue;

	private EditText mWait = null;
	private int mWaitValue;

	private EditText mType = null;
	private int mTypeValue;// 1-voice call;2-video call;3-SIP call;
	
	private EditText mSim = null;
	private int mSimValue;

	private boolean mInWorking;

	private void resetAutoTest() {
		mNumberValue = null;
		mRepeatTimeNow = 0;
		mRepeatTimeValue = 0;
		mDurationValue = 10;
		mWaitValue = 10;
		mTypeValue = 1;
		mSimValue = 0;
		mInWorking = false;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case START_REPEAT_MO_TEST:
				testRepeatCall();
				break;
			case STOP_REPEAT_MO_TEST:
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_auto_test_tool);

		resetAutoTest();

		mButton01 = (Button) findViewById(R.id.Button01);
		mButton01.setOnClickListener(this);
		mButton02 = (Button) findViewById(R.id.Button02);
		mButton02.setOnClickListener(this);

		mNumber = (EditText) this.findViewById(R.id.EditText01);
		mRepeatTime = (EditText) this.findViewById(R.id.EditText02);
		mDuration = (EditText) this.findViewById(R.id.EditText03);
		mWait = (EditText) this.findViewById(R.id.EditText04);
		mType = (EditText) this.findViewById(R.id.EditText05);
		mSim = (EditText) this.findViewById(R.id.EditText06);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onClick : " + v.getId());

		if (R.id.Button01 == v.getId()) {
			mNumberValue = mNumber.getText().toString();
			try {
				mRepeatTimeValue = new Integer(mRepeatTime.getText().toString());
			} catch (Exception e) {
				mRepeatTimeValue = 0;
			}

			try {
				mDurationValue = new Integer(mDuration.getText().toString());
			} catch (Exception e) {
				mDurationValue = 10;
			}
			try {
				mWaitValue = new Integer(mWait.getText().toString());
			} catch (Exception e) {
				mWaitValue = 10;
			}
			try {
				mTypeValue = new Integer(mType.getText().toString());
			} catch (Exception e) {
				mTypeValue = 1;
			}
			try {
				mSimValue = new Integer(mSim.getText().toString());
			} catch (Exception e) {
				mSimValue = 0;
			}

			mInWorking = true;
			logValues();

			if (mHandler != null) {
				mHandler.sendMessage(mHandler
						.obtainMessage(START_REPEAT_MO_TEST));
			}

		} else if (R.id.Button02 == v.getId()) {

			mInWorking = false;
			runEndCall();
			resetAutoTest();
		}
	}

	private void testRepeatCall() {

		// logValues();
		Log.d(TAG, "testRepeatCall() ! ");

		mRepeatTimeNow++;
		if (mRepeatTimeNow > mRepeatTimeValue || !mInWorking) {
			resetAutoTest();
			return;
		}

		Log.d(TAG, "testRepeatCall() : total - " + mRepeatTimeValue);
		Log.d(TAG, "testRepeatCall() : now - " + mRepeatTimeNow);

		runDialCall();

		(new Thread() {
			public void run() {
				mySleep(mDurationValue);
				runEndCall();
				mySleep(mWaitValue);
				if (mHandler != null) {
					mHandler.sendMessage(mHandler
							.obtainMessage(START_REPEAT_MO_TEST));
				}
			}
		}).start();

	}

	public void mySleep(int m) {
		try {
			Thread.sleep(1000 * m);
		} catch (Exception e) {
			Log.d(TAG, "can't mySleep(m) ! ");
		}
	}

	private void runDialCall() {
		Log.d(TAG, "runDialCall() ! ");

		Intent intent = new Intent("com.android.phone.InCallScreen");
		intent.setAction(Intent.ACTION_CALL);
		
		if (3 != mTypeValue)
			intent.setData(Uri.fromParts("tel", mNumberValue, null));
		else
			intent.setData(Uri.fromParts("sip", mNumberValue, null));
		
		intent.putExtra("simId", mSimValue);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (2 == mTypeValue)
			intent.putExtra("is_vt_call", true);
		startActivity(intent);
	}

	private void runEndCall() {
		Log.d(TAG, "runEndCall() ! ");

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));

		if (iTel != null) {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				try {
					iTel.endCallGemini(mSimValue);
				} catch (Exception e) {

				}
			} else {
				try {
					iTel.endCall();
				} catch (Exception e) {

				}
			}
		}

	}

	private void logValues() {
		Log.d(TAG, "mNumberValue : " + mNumberValue);
		Log.d(TAG, "mRepeatTimeNow : " + mRepeatTimeNow);
		Log.d(TAG, "mRepeatTimeValue : " + mRepeatTimeValue);
		Log.d(TAG, "mDurationValue : " + mDurationValue);
		Log.d(TAG, "mWaitValue : " + mWaitValue);
		Log.d(TAG, "mTypeValue : " + mTypeValue);
		Log.d(TAG, "mSimValue : " + mSimValue);
		Log.d(TAG, "mInWorking : " + mInWorking);
	}
}