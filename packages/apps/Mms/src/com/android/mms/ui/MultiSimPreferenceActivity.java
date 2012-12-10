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

/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mms.ui;

import java.util.List;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.ui.AdvancedCheckBoxPreference.GetSimInfo;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony;
import com.mediatek.telephony.TelephonyManagerEx;

import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import com.mediatek.featureoption.FeatureOption;

public class MultiSimPreferenceActivity extends PreferenceActivity implements GetSimInfo{
	private static final String TAG = "MultiSimPreferenceActivity";
	
    private AdvancedCheckBoxPreference mSim1;
    private AdvancedCheckBoxPreference mSim2;
    private AdvancedCheckBoxPreference mSim3;
    private AdvancedCheckBoxPreference mSim4;
    
    private int simCount;
	private List<SIMInfo> listSimInfo;
	
	private int mSim1CurrentId;
	private int mSim2CurrentId;
	
	private TelephonyManagerEx mTelephonyManager;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	listSimInfo = SIMInfo.getInsertedSIMList(this);
        simCount = listSimInfo.size();

        addPreferencesFromResource(R.xml.multicardselection);    
        Intent intent = getIntent();
        String preference = intent.getStringExtra("preference");
        //translate key to SIM-related key;
        Log.i("MultiSimPreferenceActivity, getIntent:", intent.toString());
        Log.i("MultiSimPreferenceActivity, getpreference:", preference);
        String title = intent.getStringExtra("preferenceTitle");
        if (title != null){
            setTitle(title);
        }
        changeMultiCardKeyToSimRelated(preference);
        
    }
    
    protected void onResume(){ 
        super.onResume();
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        if (mSim1 != null){
            mSim1.setNotifyChange(this);
        }
        if (mSim2 != null){
            mSim2.setNotifyChange(this);
        }
    }
    
    private void changeMultiCardKeyToSimRelated(String preference) {
        mSim1 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim1");
        mSim2 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim2");
        if(listSimInfo != null && listSimInfo.get(0).mSlot == 0){
            mSim1CurrentId = 0;
            mSim2CurrentId = 1;
        } else {
            mSim1CurrentId = 1;
            mSim2CurrentId = 0;
        }
        Log.d(TAG, "changeMultiCardKeyToSimRelated mSim1CurrentId: " + mSim1CurrentId);
        mSim1.init(this, mSim1CurrentId);
        Log.d(TAG, "changeMultiCardKeyToSimRelated mSim2CurrentId: " + mSim2CurrentId);
        mSim2.init(this, mSim2CurrentId);
        mSim3 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim3");
        mSim3.init(this, 2);
        mSim4 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim4");
        mSim4.init(this, 3);
        //get the stored value
    	SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
    	
    	if (simCount == 1) {
    		getPreferenceScreen().removePreference(mSim2);
    		getPreferenceScreen().removePreference(mSim3);
    		getPreferenceScreen().removePreference(mSim4);
    		mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)	
                   	+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
    	} else if (simCount == 2) {
    		getPreferenceScreen().removePreference(mSim3);
    		getPreferenceScreen().removePreference(mSim4);
    		
            mSim1.setKey(Long.toString(listSimInfo.get(mSim1CurrentId).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(mSim2CurrentId).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)	
                   	+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
    	} else if (simCount == 3) {
    	    getPreferenceScreen().removePreference(mSim4);
    	
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
            mSim3.setKey(Long.toString(listSimInfo.get(2).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)	
                   	+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(2).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            }
    	} else{
            
            mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
            mSim3.setKey(Long.toString(listSimInfo.get(2).mSimId) + "_" + preference);
            mSim4.setKey(Long.toString(listSimInfo.get(3).mSimId) + "_" + preference);
            if (preference.equals(MessagingPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(0).mSimId)	
                   	+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(1).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(2).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim4.setEnabled(sp.getBoolean(Long.toString(listSimInfo.get(3).mSimId)	
               		+ "_" + MessagingPreferenceActivity.AUTO_RETRIEVAL, true));
            } 
    	}
    	
        if (mSim1 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL)) {
    	        mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
            } else {
            	mSim1.setChecked(sp.getBoolean(mSim1.getKey(), false));
            }
        }
        if (mSim2 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL)) {
    	        mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            }  else {
            	mSim2.setChecked(sp.getBoolean(mSim2.getKey(), false));
            }
        }
        if (mSim3 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL)) {
    	        mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            } else {
            	mSim3.setChecked(sp.getBoolean(mSim3.getKey(), false));
            }
        }
        if (mSim4 != null) {
            if (preference.equals(MessagingPreferenceActivity.AUTO_RETRIEVAL)) {
    	        mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            } else {
            	mSim4.setChecked(sp.getBoolean(mSim4.getKey(), false));
            }
        }
        
        if (mSim1 != null) {
            if (preference.equals(MessagingPreferenceActivity.READ_REPORT_MODE) 
                    || preference.equals(MessagingPreferenceActivity.READ_REPORT_AUTO_REPLY)) {
                if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(mSim1CurrentId).mSlot)) {
                    mSim1.setEnabled(false);
                }
            }
        }
        if (mSim2 != null) {
            if (preference.equals(MessagingPreferenceActivity.READ_REPORT_MODE) 
                    || preference.equals(MessagingPreferenceActivity.READ_REPORT_AUTO_REPLY)) {
                if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(mSim2CurrentId).mSlot)) {
                    mSim2.setEnabled(false);
    }
            }
        }
    }
    
    public String getSimName(int id) {
    	return listSimInfo.get(id).mDisplayName;
    }

    
    public String getSimNumber(int id) {
    	return listSimInfo.get(id).mNumber;
    }
    
    public int getSimColor(int id) {
    	return listSimInfo.get(id).mSimBackgroundRes;
    }
    
    public int getNumberFormat(int id) {
    	return listSimInfo.get(id).mDispalyNumberFormat;
    }
    
    public int getSimStatus(int id) {
    	mTelephonyManager = TelephonyManagerEx.getDefault();
    	//int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
    	int slotId = listSimInfo.get(id).mSlot;
    	if (slotId != -1) {
    		return mTelephonyManager.getSimIndicatorStateGemini(slotId);
    	}
    	return -1;
    }
    
    public boolean is3G(int id)	{
    	//int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
    	int slotId = listSimInfo.get(id).mSlot;
    	Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
    	if (slotId == MessageUtils.get3GCapabilitySIM()) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }
        
        try {
            return iTel.getIccCardTypeGemini(slot).equals("UIM");
        } catch (Exception e) {
            Log.e(TAG, "[isUIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }
}