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

package com.mediatek.engineermode.devicemgr;

import java.io.IOException;

import com.mediatek.engineermode.R;
import com.mediatek.engineermode.ShellExe;

import com.mediatek.xlog.Xlog;

import android.widget.Toast;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.app.AlertDialog;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class DeviceMgr extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static final String KEY_SMS_AUTO_REG = "sms_auto_reg";

	private ListPreference LPsmsAutoReg;
    private DMAgent mAgent;
	private String TAG = "EM/devmgr";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.devicemgr);

        IBinder binder = ServiceManager.getService("DMAgent");
        mAgent = DMAgent.Stub.asInterface(binder);

		LPsmsAutoReg = (ListPreference) findPreference(KEY_SMS_AUTO_REG);

		if (LPsmsAutoReg == null) {
			Xlog.e(TAG, "clocwork worked...");
			// not return and let exception happened.
		} else {
			// do nothing.
		}

		LPsmsAutoReg.setOnPreferenceChangeListener(this);

		int savedCTA = getSavedCTA();
		String summary = savedCTA == 1? "Enabled" : "Disabled";
		LPsmsAutoReg.setSummary(summary);
		LPsmsAutoReg.setValue(String.valueOf(savedCTA));

	}

	@Override
	public void onPause() {
		// EMbaseband.End();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}

	public boolean onPreferenceChange(Preference preference, Object objValue) {

		final String key = preference.getKey();

		if (KEY_SMS_AUTO_REG.equals(key)) {
			try {
				setSavedCTA((String) objValue);
			} catch (NumberFormatException e) {
				Xlog.e(TAG, "set exception. ", e);
			}
	        boolean isEnabled = getSavedCTA()==1;
	        LPsmsAutoReg.setValue(isEnabled?"1":"0");
	        String summary = isEnabled ? "Enabled" : "Disabled";
	        LPsmsAutoReg.setSummary(summary);
		} else {
			ShowDialog("N", "N");
		}

		return false;
	}

//	private int getSmsAutoReg() {
//		String[] cmdx = { "/system/bin/sh", "-c",
//				"cat /data/data/com.mediatek.engineermode/sharefile/cta_cmcc" }; // file
//		int ret = 0;
//		try {
//			ret = ShellExe.execCommand(cmdx);
//			if (0 == ret) {
//				// Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
//			} else {
//				// Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
//				return 0;
//			}
//		} catch (IOException e) {
//			Xlog.e(TAG, e.toString());
//			return 0;
//		}
//		return Integer.valueOf(ShellExe.getOutput());
//	}

//	private boolean setSmsAutoReg(int n) {
//		String[] cmd = { "/system/bin/sh", "-c",
//				"mkdir /data/data/com.mediatek.engineermode/sharefile" }; // file
//
//		int ret;
//		try {
//			ret = ShellExe.execCommand(cmd);
//			if (0 == ret) {
//				// Toast.makeText(this, "mkdir ok", Toast.LENGTH_LONG).show();
//			} else {
//				// Toast.makeText(this, "file exists",
//				// Toast.LENGTH_LONG).show();
//			}
//		} catch (IOException e) {
//			Xlog.e(TAG, e.toString());
//			return false;
//		}
//
//		String[] cmdx = {
//				"/system/bin/sh",
//				"-c",
//				"echo "
//						+ n
//						+ " > /data/data/com.mediatek.engineermode/sharefile/cta_cmcc " }; // file
//
//		try {
//			ret = ShellExe.execCommand(cmdx);
//			if (0 == ret) {
//				Toast.makeText(this, "Success.", Toast.LENGTH_LONG).show();
//			} else {
//				Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
//			}
//		} catch (IOException e) {
//			Xlog.e(TAG, e.toString());
//			return false;
//		}
//		return true;
//	}

    private int getSavedCTA() {
        if(mAgent == null){
            Xlog.e(TAG, "get CTA failed, agent is null!");
            return 0;
        }
        int savedCTA=0;
        try{
            savedCTA = Integer.parseInt(mAgent.readCTA());
        }catch (RemoteException e) {
            Xlog.e(TAG, "get cta cmcc switch failed, readCTA failed!");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Xlog.e(TAG, "number format exception. ", e);
        }
        Xlog.i(TAG, "Get savedCTA = [" + savedCTA + "]");
        return savedCTA;
    }

    private void setSavedCTA(String cta) {
        if(mAgent == null){
            Xlog.e(TAG, "save CTA switch value failed, agent is null!");
            return;
        }
        try{
            mAgent.writeCTA(cta);
        }catch (RemoteException e) {
            Xlog.e(TAG, "save CTA switch failed, writeCTA failed!");
            e.printStackTrace();
        }
        
        Xlog.i(TAG, "save CTA [" + cta + "]");

    }
}
