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

package com.mediatek.engineermode.digitalstandard;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.AsyncResult;
import android.net.sip.SipManager;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony.SIMInfo;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.IccCard;
import com.mediatek.featureoption.FeatureOption;


import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;




public class DigitalStandard extends PreferenceActivity 
			implements Preference.OnPreferenceChangeListener {

	private static final String TAG = "DigitalStandard";

	private boolean hasNetworkMode = false;
	
	private NetworkModePreference mNetworkMode;
	private PreferenceScreen mNetworkModeGemini;
	private boolean mIsSlot1Insert = false;
	private boolean mIsSlot2Insert = false;
	
	private static final String KEY_GENERAL_SETTINGS_CATEGORY = "general_settings";
	private static final String KEY_NETWORK_MODE_SETTING = "gsm_umts_preferred_network_mode_key";
	private static final String KEY_NETWORK_MODE_SETTING_GEMINI = "gsm_umts_preferred_network_mode_gemini_key";
	
	private static final String OLD_NETWORK_MODE = "com.android.phone.OLD_NETWORK_MODE";
	private static final String NEW_NETWORK_MODE = "NEW_NETWORK_MODE";
	private static final String NETWORK_MODE_CHANGE_BROADCAST = "com.android.phone.NETWORK_MODE_CHANGE";
	private static final String NETWORK_MODE_CHANGE_RESPONSE = "com.android.phone.NETWORK_MODE_CHANGE_RESPONSE";
	   
	private static final int DIALOG_NETWORK_MODE_CHANGE = 1008; 
	
	
	static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
	
	private String[] mNetworkModeSummary;
	
	private TelephonyManager mTelephonyManager;
	private IntentFilter mIntentFilter;
	private static boolean mAirplaneMode = false;
	private Phone geminiPhone;
	
	private boolean isRegister;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.sim_management);
		geminiPhone = (Phone)PhoneFactory.getDefaultPhone();  
		
		//hasNetworkMode = true;
		//only support single card version.
		hasNetworkMode = !FeatureOption.MTK_GEMINI_SUPPORT && (TelephonyManager.getDefault().getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA);
		if(!hasNetworkMode)
		{
			Elog.e(TAG, "PHONE_TYPE_CDMA is needed.");
			DigitalStandard.this.finish();
			return;
		}
		
		PreferenceGroup GeneralSettingsCategory = (PreferenceGroup) findPreference(
        		KEY_GENERAL_SETTINGS_CATEGORY);
		
		 mNetworkMode = (NetworkModePreference) findPreference(
	        		KEY_NETWORK_MODE_SETTING);
	        
	    mNetworkModeGemini = (PreferenceScreen) findPreference(
	        		KEY_NETWORK_MODE_SETTING_GEMINI);
	    
	    mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        mIntentFilter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);        
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);     
        mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        
        
        mIsSlot1Insert = geminiPhone.isSimInsert();
	    //mIsSlot1Insert = geminiPhone.isSimInsert(Phone.GEMINI_SIM_1);
    	//mIsSlot2Insert = geminiPhone.isSimInsert(Phone.GEMINI_SIM_2);
    	
    	
    	if(!mIsSlot1Insert) {
    		mNetworkMode.setEnabled(false);
        	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
    	} 
    	
    	else {
            
        	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
        	mNetworkMode.setOnPreferenceChangeListener(this); 
            mIntentFilter.addAction(NETWORK_MODE_CHANGE_RESPONSE);

    	} 
    	if(false) {    		

    		GeneralSettingsCategory.removePreference(mNetworkMode);
    		String[] mTempCopy = getResources().getStringArray(R.array.gsm_umts_network_preferences_choices);

    		mNetworkModeSummary = new String[3];
    		mNetworkModeSummary[0] = mTempCopy[0];
    		mNetworkModeSummary[1] = mTempCopy[2];
    		mNetworkModeSummary[2] = mTempCopy[1];
    		
    	}
    	
    	mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);		

        
        
        registerReceiver(mSimReceiver, mIntentFilter);
        isRegister = true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(hasNetworkMode) {
			boolean isCallIdle = isCallStateIdle();
	        if(!isCallIdle){
	        	mNetworkMode.setEnabled(false);
	        	mNetworkModeGemini.setEnabled(false);
	        }else{
	        	
	            boolean Sim1Ready = false;
	            
	            if(mIsSlot1Insert) {  
			            	//Sim1Ready = geminiPhone.isRadioOn();
			            	if(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState())
			            	{
			            		Sim1Ready = true;
			            	}
			            	 
			         }


	        	mNetworkMode.setEnabled(Sim1Ready);
	        	mNetworkModeGemini.setEnabled(Sim1Ready);

	        }	
			
			if((mIsSlot1Insert)&&(mIsSlot2Insert)) {

				if(mNetworkModeSummary != null) {
			        int settingsNetworkMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE,
			                preferredNetworkMode);
			        if((settingsNetworkMode>=0)&&(settingsNetworkMode<=2)){
				        mNetworkModeGemini.setSummary(mNetworkModeSummary[settingsNetworkMode]);      	
			        }

					
				}
			
			} else {
		        int settingsNetworkMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE,
		                preferredNetworkMode);
		        
		        if((settingsNetworkMode>=0)&&(settingsNetworkMode<=2)) {
			        mNetworkMode.setValue(Integer.toString(settingsNetworkMode));
			        mNetworkMode.updateSummary();
		        }

			}
			
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegister) {
            unregisterReceiver(mSimReceiver);
        }
    }
	
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
        Elog.i(TAG, "Enter onPreferenceChange function.");
        
        final String key = arg0.getKey();
        // TODO Auto-generated method stub
        if(KEY_NETWORK_MODE_SETTING.equals(key)) {
        	
            int oldNetworkMode = Settings.Secure.getInt(
                    getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            
    		int newNetworkMode = Integer.valueOf((String) arg1).intValue();
    		
    		newNetworkMode = Utils.getNetworkMode(newNetworkMode);
    		
    		Settings.Secure.putInt(getContentResolver(),
    				Settings.Secure.PREFERRED_NETWORK_MODE,
    				newNetworkMode);	
    		
    		if(newNetworkMode != oldNetworkMode) {
    			
                Intent intent = new Intent(NETWORK_MODE_CHANGE_BROADCAST,
                        null);
                intent.putExtra(OLD_NETWORK_MODE, oldNetworkMode);
                intent.putExtra(NETWORK_MODE_CHANGE_BROADCAST, newNetworkMode);
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
       			showDialog(DIALOG_NETWORK_MODE_CHANGE);
                sendBroadcast(intent);
    			
    		}
        
        	
        }
        return true;
    }
	
	@Override
    protected Dialog onCreateDialog(int id) {
    	  ProgressDialog dialog = new ProgressDialog(this);
    	  Builder builder = new AlertDialog.Builder(this);
		    AlertDialog alertDlg;
    	  switch (id) {
            	             
		        case DIALOG_NETWORK_MODE_CHANGE:                
			         dialog.setMessage(getResources().getString(R.string.gemini_data_connection_progress_message));
			         dialog.setIndeterminate(true);
			         dialog.setCancelable(false);
			         return dialog; 
           default:
               return null;
        }
    }
		
	
	
	private boolean isCallStateIdle(){
    	
    	int stateSim1 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_1);
    	int stateSim2 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_2);
    	Elog.i(TAG,"stateSim1 is "+stateSim1+" stateSim2 is "+stateSim2);
    	
    	if((stateSim1 == TelephonyManager.CALL_STATE_IDLE)&&(stateSim2 == TelephonyManager.CALL_STATE_IDLE)) {
    		return true;
    	} else {
    		return false;
    	}
    }	
	
	
	private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            String action = intent.getAction();
            
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
            		mAirplaneMode = intent.getBooleanExtra("state", false);
                	Elog.d(TAG, "airplane mode changed to "+mAirplaneMode);            		
            		
		            if(hasNetworkMode == true){
			            boolean Sim1Ready = false;
			            
			            if(mIsSlot1Insert) {  
			            	//Sim1Ready = geminiPhone.isRadioOn();
			            	if(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState())
			            	{
			            		Sim1Ready = true;
			            	}
			            	 
			            }


			        	mNetworkMode.setEnabled(Sim1Ready);
			        	mNetworkModeGemini.setEnabled(Sim1Ready);
		            }

            } else if (action.equals(NETWORK_MODE_CHANGE_RESPONSE)){

            	
				if (!intent.getBooleanExtra(NETWORK_MODE_CHANGE_RESPONSE, true)) {
					Elog.d(TAG,"BroadcastReceiver: network mode change failed! restore the old value.");
					Settings.Secure.putInt(
									getContentResolver(),Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(OLD_NETWORK_MODE, 0));
				} else {
					Elog.d(TAG,"BroadcastReceiver: network mode change succeed! set the new value.");
					Settings.Secure.putInt(
									getContentResolver(),Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(NEW_NETWORK_MODE, 0));
				}
		        mNetworkMode.updateSummary();
                removeDialog(DIALOG_NETWORK_MODE_CHANGE);
                

			}
        }
	};
	
	

}