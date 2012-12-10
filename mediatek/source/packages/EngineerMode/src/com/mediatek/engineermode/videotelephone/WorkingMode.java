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

package com.mediatek.engineermode.videotelephone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import com.mediatek.xlog.Xlog;
import android.view.View;
//import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mediatek.engineermode.R;

public class WorkingMode extends Activity {

    private static final String TAG = "EM/WorkingMode";

    private RadioButton mWorkingModeNormalRadio;
    private RadioButton mMediaLoopRadio;
    private RadioButton mMediaLoopStackRadio;
    private RadioButton mMediaLoopTransceiverRadio;
    private RadioButton mWorkingModeNetworkLoopRadio;
    private RadioButton mNetworkLoopStackRadio;
    private RadioButton mNetworkLoopServiceRadio;
    private RadioButton mWorkingModeTestFileRadio;

    private RadioGroup mMediaLoopRadiogroup;
    private RadioGroup mNetworkLoopRadiogroup;

//    private Button mMediaLoopButton;

    private boolean mSDCardFlag = false;

    private View.OnClickListener mListener = new View.OnClickListener() {

        public void onClick(View v) {
            
            SharedPreferences preferences = getSharedPreferences(VideoTelephony.ENGINEER_MODE_PREFERENCE,
                    WorkingMode.MODE_WORLD_READABLE);
            Editor edit = preferences.edit();
            switch (v.getId()) {
            case R.id.working_mode_normal:
                edit.putString(VideoTelephony.WORKING_MODE, "0");
                edit.putString(VideoTelephony.WORKING_MODE_DETAIL, "2");
                edit.commit();
                Xlog.v(TAG, "Working mode normal radio is clicked!");
                break;
            case R.id.media_loopback_stack:
                edit.putString(VideoTelephony.WORKING_MODE, "1");
                edit.putString(VideoTelephony.WORKING_MODE_DETAIL, "0");
                edit.commit();
                Xlog.v(TAG, "Media loopback stack radio is clicked!");
                break;
            case R.id.media_loopback_transceiver:
                edit.putString(VideoTelephony.WORKING_MODE, "1");
                edit.putString(VideoTelephony.WORKING_MODE_DETAIL, "1");
                edit.commit();
                Xlog.v(TAG, "Media loopback transceiver radio is clicked!");
                break;
            case R.id.network_loopback_stack:
                edit.putString(VideoTelephony.WORKING_MODE, "2");
                edit.putString(VideoTelephony.WORKING_MODE_DETAIL, "0");
                edit.commit();
                Xlog.v(TAG, "Network loopback stack radio is clicked!");
                break;
            case R.id.network_loopback_service:
                edit.putString(VideoTelephony.WORKING_MODE, "2");
                edit.putString(VideoTelephony.WORKING_MODE_DETAIL, "1");
                edit.commit();
                Xlog.v(TAG, "Network loopback service radio is clicked!");
                break;
            case R.id.working_mode_test_file:
                edit.putString(VideoTelephony.WORKING_MODE, "3");
                edit.commit();
                Xlog.v(TAG, "Working mode test file radio is clicked!");
                break;
            case R.id.working_mode_media_loopback:
                mMediaLoopRadiogroup.setVisibility(View.VISIBLE);
//                mMediaLoopButton.setVisibility(View.VISIBLE);
                mNetworkLoopRadiogroup.setVisibility(View.GONE);
                return;
            case R.id.working_mode_network_loopback:
                mMediaLoopRadiogroup.setVisibility(View.GONE);
//                mMediaLoopButton.setVisibility(View.GONE);
                mNetworkLoopRadiogroup.setVisibility(View.VISIBLE);
                return;
//            case R.id.media_loopback_button:
//		try {
//			Intent it = new Intent(Intent.ACTION_MAIN);
//			it.setClassName("com.android.phone", "com.android.phone.VTEMMediaLoopBack");
//			WorkingMode.this.startActivity(it);	
//		} catch (Exception e) {
//			Log.v(TAG, "Can't find VTEMMediaLoopBack activity!");
//		}
//                
//                return;
            }
            onClicked();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
        setContentView(R.layout.working_mode);

        mWorkingModeNormalRadio = (RadioButton) findViewById(R.id.working_mode_normal);
        mMediaLoopRadio = (RadioButton) findViewById(R.id.working_mode_media_loopback);
        mMediaLoopStackRadio = (RadioButton) findViewById(R.id.media_loopback_stack);
        mMediaLoopTransceiverRadio = (RadioButton) findViewById(R.id.media_loopback_transceiver);
        mWorkingModeNetworkLoopRadio = (RadioButton) findViewById(R.id.working_mode_network_loopback);
        mNetworkLoopStackRadio = (RadioButton) findViewById(R.id.network_loopback_stack);
        mNetworkLoopServiceRadio = (RadioButton) findViewById(R.id.network_loopback_service);
        mWorkingModeTestFileRadio = (RadioButton) findViewById(R.id.working_mode_test_file);
        mMediaLoopRadiogroup = (RadioGroup) findViewById(R.id.media_loopback_radiogroup);
        mNetworkLoopRadiogroup = (RadioGroup) findViewById(R.id.network_loopback_radiogroup);
//        mMediaLoopButton = (Button) findViewById(R.id.media_loopback_button);
        
        if(mWorkingModeNormalRadio == null
        		|| mMediaLoopRadio == null
        		|| mMediaLoopStackRadio == null
        		|| mMediaLoopTransceiverRadio == null
        		|| mWorkingModeNetworkLoopRadio == null
        		|| mNetworkLoopStackRadio == null
        		|| mNetworkLoopServiceRadio == null
        		|| mWorkingModeTestFileRadio == null
        		|| mMediaLoopRadiogroup == null
        		|| mNetworkLoopRadiogroup == null
//        		|| mMediaLoopButton == null
        		)
        {
        	Xlog.e(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        
        mWorkingModeNormalRadio.setOnClickListener(mListener);
        mMediaLoopStackRadio.setOnClickListener(mListener);
        mMediaLoopTransceiverRadio.setOnClickListener(mListener);
        mNetworkLoopStackRadio.setOnClickListener(mListener);
        mNetworkLoopServiceRadio.setOnClickListener(mListener);
        mMediaLoopRadio.setOnClickListener(mListener);
        mWorkingModeNetworkLoopRadio.setOnClickListener(mListener);
//        mMediaLoopButton.setOnClickListener(mListener);

        try {
            SharedPreferences preferences = getSharedPreferences(VideoTelephony.ENGINEER_MODE_PREFERENCE,
                    WorkingMode.MODE_WORLD_READABLE);
            mSDCardFlag = preferences.getBoolean(VideoTelephony.SDCARD_FLAG, false);

            if (!mSDCardFlag) {
                mWorkingModeTestFileRadio.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Xlog.v(TAG, "preferences Exception");
        }

        mWorkingModeTestFileRadio.setOnClickListener(mListener);

    }

    private void onClicked() {
        this.finish();
    }

}
