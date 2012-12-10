/*
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
//For Operator Custom
//MTK_OP03_PROTECT_START
package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.ServiceState; 
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;

public class TetherApnSettings extends ApnSettings implements
        Preference.OnPreferenceChangeListener {
    static final String TAG = "TetherApnSettings";

	private static final Uri PREFER_APN_TETHER_URI = Uri.parse("content://telephony/carriers/prefertetheringapn");

    private boolean mIsSwitching = false;
    private boolean mIsSIMReady = true;
    private boolean mIsTetehred = false;
    private ConnectivityManager mConnManager;
    private String[] mUsbRegexs;

    private final BroadcastReceiver mTetheringStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        
        	String action = intent.getAction(); 
            if (action.equals(ConnectivityManager.TETHER_CHANGED_DONE_ACTION)) {
                Xlog.d(TAG, "onReceive:ConnectivityManager.TETHER_CHANGED_DONE_ACTION");
            	mIsSwitching = false;
               	getPreferenceScreen().setEnabled(getScreenEnableState());                
            } else if(action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	Xlog.d(TAG, "onReceive:AIRPLANE_MODE state changed: " + mAirplaneModeEnabled);            	
            	mAirplaneModeEnabled = intent.getBooleanExtra("state", false);
            	getPreferenceScreen().setEnabled(getScreenEnableState());
            } else if(action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {            	
            	Xlog.d(TAG, "onReceive: ConnectivityManager.ACTION_TETHER_STATE_CHANGED");
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
		if (active != null) {
                updateTetheredState(active.toArray());  
		} else {
		   Xlog.d(TAG, "active tether is null , not update tether state.");
		} 
            }
        }
    };
    

    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mConnManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 
        if(mConnManager != null) {
        	mUsbRegexs = mConnManager.getTetherableUsbRegexs();  	
        }
        TelephonyManager telManager = TelephonyManager.getDefault();
        
        if(telManager != null) {
            mIsSIMReady = TelephonyManager.SIM_STATE_READY == telManager.getSimState();
        }

		mIsTetherApn = true;
		mRestoreCarrierUri = PREFER_APN_TETHER_URI;
    }   
    
    @Override
    protected void onResume() {
        super.onResume();
        Xlog.d(TAG, "onResume , mIsSwitching = "+mIsSwitching);
        if(mConnManager != null) {
            mIsSwitching = !mConnManager.isTetheringChangeDone();
            String[] tethered = mConnManager.getTetheredIfaces(); 
            updateTetheredState(tethered);     	
        }  	
    }

    @Override
	protected IntentFilter getIntentFilter(){
	    IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);       
        filter.addAction(ConnectivityManager.TETHER_CHANGED_DONE_ACTION); 
        filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED); 
		return filter;
	}
	
    @Override
	protected BroadcastReceiver  getBroadcastReceiver(){
		return mTetheringStateReceiver;
	}    

    @Override
	protected String getFillListQuery(){
		return "numeric=\"" + mNumeric + "\" AND type=\""+ApnSettings.TETHER_TYPE+"\"";
	}

    @Override
	protected boolean getScreenEnableState(){		
		mIsCallStateIdle = mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;  
		Xlog.w(TAG,"mIsCallStateIdle : "+ mIsCallStateIdle +" mAirplaneModeEnabled : " + mAirplaneModeEnabled +
			" mIsSIMReady :" +mIsSIMReady + " mIsSwitching: " +mIsSwitching + " mIsTetehred: " + mIsTetehred);
		return !mIsTetehred && mIsCallStateIdle && !mAirplaneModeEnabled && mIsSIMReady &&!mIsSwitching;
	}	
	
	@Override
    protected void addMenu(Menu menu) {
        /*the 20801 23430 23431 23432 is sim card for orange support*/
        if(Utils.opType == Utils.OpIndex.OP_ORANGE){
            if("20801".equals(mNumeric)
                || "23430".equals(mNumeric)
                || "23431".equals(mNumeric)
                || "23432".equals(mNumeric)){
                return;	
            }
        }

        menu.add(0, MENU_NEW, 0,
            getResources().getString(R.string.menu_new))
            .setIcon(android.R.drawable.ic_menu_add);
    }
	
    @Override
    protected void setSelectedApnKey(String key) {
        mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        resolver.delete(PREFER_APN_TETHER_URI, null, null);
        ContentValues values = new ContentValues();
        values.put(APN_ID, mSelectedKey);
        resolver.insert(PREFER_APN_TETHER_URI, values);
        
    }

	@Override
    protected void addNewApn() {
		Intent it = new Intent(Intent.ACTION_INSERT, mUri);
		it.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
		it.putExtra(ApnSettings.APN_TYPE, ApnSettings.TETHER_TYPE);
		startActivity(it);
    }  
   
   private void updateTetheredState(Object[] tethered) {
	   
		mIsTetehred = false;
		for (Object o : tethered) {
			   String s = (String)o;
			   for (String regex : mUsbRegexs) {
				   if (s.matches(regex)) mIsTetehred = true;
			   }
		}		 

        getPreferenceScreen().setEnabled(getScreenEnableState());
   } 
}
//MTK_OP03_PROTECT_END

