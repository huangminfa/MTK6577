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

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;

import java.util.List;

import com.android.mms.R;
import com.android.mms.ui.AdvancedEditorPreference.GetSimInfo;

import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.mediatek.xlog.Xlog;
import android.telephony.SmsManager;

import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import com.mediatek.featureoption.FeatureOption;

public class SelectCardPreferenceActivity extends PreferenceActivity implements GetSimInfo{
    private static final String TAG = "Mms/SelectCardPreferenceActivity";
    
    private AdvancedEditorPreference mSim1;
    private AdvancedEditorPreference mSim2;
    private AdvancedEditorPreference mSim3;
    private AdvancedEditorPreference mSim4;
    
    private String mSim1Number;
    private String mSim2Number;
    private String mSim3Number;
    private String mSim4Number;
    
    private int simCount;
    
    private int currentSim = -1;
    private TelephonyManagerEx mTelephonyManager;
    private EditText mNumberText;
    private AlertDialog.Builder mNumberTextDialog;
    private List<SIMInfo> listSimInfo;
    String intentPreference;
    private static Handler mSMSHandler = new Handler();
    private final int MAX_EDITABLE_LENGTH = 20;
    public String SUB_TITLE_NAME = "sub_title_name";
    private AlertDialog mSaveLocDialog;//for sms save location
    private SharedPreferences spref;
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        simCount = listSimInfo.size();
        spref = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.multicardeditorpreference);    
        Intent intent = getIntent();
        intentPreference = intent.getStringExtra("preference");
        String title = intent.getStringExtra("preferenceTitle");
        if (title != null){
            setTitle(title);
        }
        changeMultiCardKeyToSimRelated(intentPreference);
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

        mSim1 = (AdvancedEditorPreference) findPreference("pref_key_sim1");
        if(listSimInfo != null && listSimInfo.get(0).mSlot == 0){
             mSim1.init(this, 0, preference);
        } else {
             mSim1.init(this, 1, preference);
        }
        mSim2 = (AdvancedEditorPreference) findPreference("pref_key_sim2");
        if(listSimInfo != null && listSimInfo.get(0).mSlot == 1){
              mSim2.init(this, 0, preference);
        } else {
              mSim2.init(this, 1, preference);
        }
        mSim3 = (AdvancedEditorPreference) findPreference("pref_key_sim3");
        mSim3.init(this, 2, preference);
        mSim4 = (AdvancedEditorPreference) findPreference("pref_key_sim4");
        mSim4.init(this, 3, preference);
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (simCount == 1) {
            getPreferenceScreen().removePreference(mSim2);
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);           
        } else if (simCount == 2) {
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);   
        } else if (simCount == 3) {
            getPreferenceScreen().removePreference(mSim4);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        int currentId = 0;
        if (preference == mSim1) {
            if(listSimInfo != null && listSimInfo.get(0).mSlot == 0){
                  currentId = 0;
            } else {
                  currentId = 1;
            }
        } else if (preference == mSim2) {
              if(listSimInfo != null && listSimInfo.get(0).mSlot == 1){
                  currentId = 0;
              } else {
                  currentId = 1;
              }
        } else if (preference == mSim3) {
            currentId = 2;
        } else if (preference == mSim4) {
            currentId = 3;
        }
        if (intentPreference.equals(MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES)) {
            setTitle(R.string.pref_title_manage_sim_messages);
            startManageSimMessages(currentId);
        } else if (intentPreference.equals(MessagingPreferenceActivity.CELL_BROADCAST)) {
            startCellBroadcast(currentId);
        }  else if (intentPreference.equals(MessagingPreferenceActivity.SMS_SERVICE_CENTER)) {
            //mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            currentSim = listSimInfo.get(currentId).mSlot;
            setServiceCenter(currentSim);
        } else if(intentPreference.equals(MessagingPreferenceActivity.SMS_SAVE_LOCATION)){
            setSaveLocation(currentId, (AdvancedEditorPreference)preference, this);
        }else if(intentPreference.equals(MessagingPreferenceActivity.SMS_VALIDITY_PERIOD)){
        	final Preference pref = preference;
        	int slotId = listSimInfo.get(currentId).mSlot;

			final int [] peroids = {
				SmsManager.VALIDITY_PERIOD_NO_DURATION,
				SmsManager.VALIDITY_PERIOD_ONE_HOUR,
				SmsManager.VALIDITY_PERIOD_SIX_HOURS,
				SmsManager.VALIDITY_PERIOD_TWELVE_HOURS,
				SmsManager.VALIDITY_PERIOD_ONE_DAY,
				SmsManager.VALIDITY_PERIOD_MAX_DURATION,
			};
			final CharSequence[] items = {
				getResources().getText(R.string.sms_validity_period_nosetting), 
				getResources().getText(R.string.sms_validity_period_1hour), 
				getResources().getText(R.string.sms_validity_period_6hours), 
				getResources().getText(R.string.sms_validity_period_12hours), 
				getResources().getText(R.string.sms_validity_period_1day), 
				getResources().getText(R.string.sms_validity_period_max)};

            /* check validity index*/
        	final String validityKey = Long.toString(slotId) + "_" + MessagingPreferenceActivity.SMS_VALIDITY_PERIOD;
            int vailidity = spref.getInt(validityKey, SmsManager.VALIDITY_PERIOD_NO_DURATION);
            int currentPosition = 0;
            Xlog.d(TAG, "validity found the res = " + vailidity);
            for(int i = 0; i < peroids.length; i++){
            	if(vailidity == peroids[i]){
            		Xlog.d(TAG, "validity found the position = "+i);
            		currentPosition = i;
            		break;
            	}
            }
            
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(getResources().getText(R.string.sms_validity_period));
        	builder.setSingleChoiceItems(items, currentPosition, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
                    SharedPreferences.Editor editor = spref.edit();
                    editor.putInt(validityKey, peroids[item]);
                    editor.commit();
                    dialog.dismiss();
        	    }
        	});
        	builder.show();
        	
        	
        	AlertDialog alert = builder.create();
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void setSaveLocation(long id, final AdvancedEditorPreference mSim, final Context context){
        Xlog.d(TAG, "currentSlot is: " + id);
        //the key value for each saveLocation
        final String [] saveLocation; 
        //the diplayname for each saveLocation
        final String [] saveLocationDisp;
        
        if(!getResources().getBoolean(R.bool.isTablet))
        {
            saveLocation = getResources().getStringArray(R.array.pref_sms_save_location_values);
            saveLocationDisp = getResources().getStringArray(R.array.pref_sms_save_location_choices);
        }
        else
        {
            saveLocation = getResources().getStringArray(R.array.pref_tablet_sms_save_location_values);
            saveLocationDisp = getResources().getStringArray(R.array.pref_tablet_sms_save_location_choices);
        }
        
       	if(saveLocation == null || saveLocationDisp == null){
       		Xlog.d(TAG, "setSaveLocation is null");
       		return;
       	}
       	final String saveLocationKey = Long.toString(id) + "_" + MessagingPreferenceActivity.SMS_SAVE_LOCATION;
       	int pos = getSelectedPosition(saveLocationKey, saveLocation);
       	mSaveLocDialog = new AlertDialog.Builder(this)
    	.setTitle(R.string.sms_save_location)
        .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
        .setSingleChoiceItems(saveLocationDisp, pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences.Editor editor = spref.edit();
                editor.putString(saveLocationKey, saveLocation[whichButton]);
                editor.commit();
                mSaveLocDialog.dismiss();
                mSaveLocDialog = null;
                mSim.setNotifyChange(context);
            }
        })
        .show();
    }
    
    //get the position which is selected before
    private int getSelectedPosition(String inputmodeKey, String [] modes){
        String res = spref.getString(inputmodeKey, "Phone");
        Xlog.d(TAG, "getSelectedPosition found the res = "+res);
        for(int i = 0; i < modes.length; i++){
        	if(res.equals(modes[i])){
        		Xlog.d(TAG, "getSelectedPosition found the position = "+i);
        		return i;
        	}
        }
        Xlog.d(TAG, "getSelectedPosition not found the position");

        return 0; 
    }
    
    public void setServiceCenter(int id){

        if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(id)) {
            showToast(R.string.cdma_not_support);
        } else {

			mNumberTextDialog = new AlertDialog.Builder(this);
		    mNumberText = new EditText(mNumberTextDialog.getContext());
		    mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
		    mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
		    //mNumberText.setKeyListener(new DigitsKeyListener(false, true));
		    mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
		    mNumberText.computeScroll();
		    Xlog.d(TAG, "currentSlot is: " + id);
		    String scNumber = getServiceCenter(id);
		    Xlog.d(TAG, "getScNumber is: " + scNumber);
		    mNumberText.setText(scNumber);
		    mNumberTextDialog.setIcon(R.drawable.ic_dialog_info_holo_light)
		    .setTitle(R.string.sms_service_center)
		    .setView(mNumberText)
		    .setPositiveButton(R.string.OK, new PositiveButtonListener())
		    .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
		    .show();

        }

    }

    public void startManageSimMessages(int id){
        if(listSimInfo == null || id >= listSimInfo.size()){
            Xlog.e(TAG, "startManageSimMessages listSimInfo is null ");
            return;
        }
        Intent it = new Intent();
         int slotId = listSimInfo.get(id).mSlot;
         Xlog.d(TAG, "currentSlot is: " + slotId);
         Xlog.d(TAG, "currentSlot name is: " + listSimInfo.get(0).mDisplayName);
         it.setClass(this, ManageSimMessages.class);
         it.putExtra("SlotId", slotId);
         startActivity(it);
    }
    private void startCellBroadcast(int num){
        if (listSimInfo == null || num >= listSimInfo.size()){
            Xlog.e(TAG, "startCellBroadcast listSimInfo is null ");
            return;
        }
        int slotId = listSimInfo.get(num).mSlot;

        if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(slotId)) {
            showToast(R.string.cdma_not_support);
        } else {

		     Intent it = new Intent();
		     Xlog.i(TAG, "currentSlot is: " + slotId);
		     Xlog.i(TAG, "currentSlot name is: " + listSimInfo.get(num).mDisplayName);
		     it.setClassName("com.android.phone", "com.android.phone.CellBroadcastActivity");
		     it.setAction(Intent.ACTION_MAIN);
		     it.putExtra(Phone.GEMINI_SIM_ID_KEY, slotId);
		     it.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, slotId).mDisplayName);
		     startActivity(it);

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
    
    public boolean is3G(int id)    {
        mTelephonyManager = TelephonyManagerEx.getDefault();
        //int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = listSimInfo.get(id).mSlot;
        Xlog.d(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == MessageUtils.get3GCapabilitySIM()) {
            return true;
        }
        return false;
    }
    
    private String getServiceCenter(int id) {
        mTelephonyManager = TelephonyManagerEx.getDefault();
        return mTelephonyManager.getScAddress(id);    
    }
    
    private boolean setServiceCenter(String SCnumber, int id) {
        mTelephonyManager = TelephonyManagerEx.getDefault();
        Xlog.d(TAG, "setScAddress is: " + SCnumber);
        return mTelephonyManager.setScAddress(SCnumber, id);
    }
    
    private void tostScOK() {
        Toast.makeText(this, R.string.set_service_center_OK, 0);
    }
    
    private void tostScFail() {
        Toast.makeText(this, R.string.set_service_center_fail, 0);
    }
    
    private class PositiveButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            mTelephonyManager = TelephonyManagerEx.getDefault();
            String scNumber = mNumberText.getText().toString();
            Xlog.d(TAG, "setScNumber is: " + scNumber);
            Xlog.d(TAG, "currentSim is: " + currentSim);
            //setServiceCenter(scNumber, currentSim);
            new Thread(new Runnable() {
                public void run() {
                    mTelephonyManager.setScAddress(mNumberText.getText().toString(), currentSim);
                }
            }).start();
        }
    }
    
    private class NegativeButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // cancel
            dialog.dismiss();
        }
    }
    
    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }
        
        try {
            String type = iTel.getIccCardTypeGemini(slot);
            return iTel.getIccCardTypeGemini(slot).equals("UIM");
        } catch (Exception e) {
            Log.e(TAG, "[isUIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }
    
    private void showToast(int id) { 
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    } 
}
