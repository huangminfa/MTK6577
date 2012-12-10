package com.android.settings.gemini;


import java.util.ArrayList;
import java.util.List;
import android.util.Log;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.preference.Preference.OnPreferenceClickListener;

import android.view.View;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ITelephony;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.featureoption.FeatureOption;
import com.android.settings.R;
import com.mediatek.xlog.Xlog;






public class SimDataRoamingSettings extends SimCheckboxEntrance {

    private static final String TAG = "SimDataRoamingSettings";

    private ITelephony iTelephony;
    private int mSimSum = 0;
//    private GeminiPhone mGeminiPhone;

    private int mCurrentSimSlot;
    private long mCurrentSimID;

    private TelephonyManagerEx mTelephonyManager;

    
 	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		iTelephony = ITelephony.Stub.asInterface(ServiceManager
				.getService("phone"));


    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)  {

        // TODO Auto-generated method stub


        long simID = Long.parseLong(preference.getKey());
        
        SIMInfo simInfo = SIMInfo.getSIMInfoById(getActivity(), simID);
        
        
        if (simInfo != null) {
        	int DataRoaming = simInfo.mDataRoaming;
        	mCurrentSimSlot = simInfo.mSlot;
        	mCurrentSimID = simInfo.mSimId;
  
            final SimInfoPreference simInfoPref = (SimInfoPreference)preference;
        	if (DataRoaming==SimInfo.DATA_ROAMING_DISABLE) {
                new AlertDialog.Builder(getActivity()).setMessage(
                        getResources().getString(R.string.roaming_warning))
                        .setTitle(android.R.string.dialog_alert_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {				
				            public void onClick(DialogInterface dialog, int whichButton) {
					              // TODO Auto-generated method stub
					              //use to judge whether the click is correctly done!

				            	try {
				        			if(iTelephony != null) {
				        				iTelephony.setDataRoamingEnabledGemini(true, mCurrentSimSlot);

				        			}
				            	} catch (RemoteException e){
				    				Xlog.e(TAG, "iTelephony exception");
				    				return;
				    			}
			        			SIMInfo.setDataRoaming(SimDataRoamingSettings.this.getActivity(),SimInfo.DATA_ROAMING_ENABLE, mCurrentSimID);
			                    
			                    if (simInfoPref != null) {
			                    	simInfoPref.setCheck(true);
			                    };
				            }
			          })
			          .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {				
				            public void onClick(DialogInterface dialog, int whichButton) {
					              // TODO Auto-generated method stub
					              //use to judge whether the click is correctly done!
			                    
/*			                    if (simInfoPref != null) {
			                    	simInfoPref.setCheck(false);
			                    };*/
				            }
			          })
			          .show();

        	} else {



            	try {
        			if(iTelephony != null) {
        				iTelephony.setDataRoamingEnabledGemini(false, mCurrentSimSlot);

        			}
            	} catch (RemoteException e){
    				Xlog.e(TAG, "iTelephony exception");
    				return false;
    			}
    			SIMInfo.setDataRoaming(getActivity(),SimInfo.DATA_ROAMING_DISABLE, mCurrentSimID);
                if (simInfoPref != null) {
                	simInfoPref.setCheck(false);
                };
                    
        	}
        	return true;
        }
         return false;
    }
    
    
	protected boolean shouldDisableWhenRadioOff() {
		return true;
	}

    
    protected void updateCheckState(SimInfoPreference pref, SIMInfo siminfo) {
    	
    	pref.setCheck(siminfo.mDataRoaming == SimInfo.DATA_ROAMING_ENABLE);

    	return;
    }
	
}
