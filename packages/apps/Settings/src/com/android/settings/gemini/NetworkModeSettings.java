//For Operator Custom
//MTK_OP02_PROTECT_START
package com.android.settings.gemini;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import android.util.AttributeSet;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.xlog.Xlog;



public class NetworkModeSettings extends SettingsPreferenceFragment 
				implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "NetworkModeSettings";  
    private NetworkModePreference mNetworkMode;
    private static final String KEY_NETWORK_MODE_SETTING = "gsm_umts_preferred_network_mode_key";
    private IntentFilter mIntentFilter;
    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    private ITelephony iTelephony;
    
    public static final String OLD_NETWORK_MODE = "com.android.phone.OLD_NETWORK_MODE";
    public static final String NETWORK_MODE_CHANGE_BROADCAST = "com.android.phone.NETWORK_MODE_CHANGE";
    public static final String NETWORK_MODE_CHANGE_RESPONSE = "com.android.phone.NETWORK_MODE_CHANGE_RESPONSE";

    private static final int DIALOG_NETWORK_MODE_CHANGE = 1008; 
    
	private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            String action = intent.getAction();
            
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
            	
                int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                int simStatus = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
            	Xlog.i(TAG, "receive notification of  sim slot = "+slotId+" status = "+simStatus);
                
                if ((slotId == Phone.GEMINI_SIM_1)&&(simStatus>=0)) {
            		mNetworkMode.setStatus(simStatus);      

                }
            	
			} else if (action.equals(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE)) {

				if (!intent.getBooleanExtra(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE, true)) {
					Xlog.i(TAG,"BroadcastReceiver: network mode change failed! restore the old value.");
					android.provider.Settings.Secure.putInt(
									getContentResolver(),android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(OLD_NETWORK_MODE, 0));
				} else {
					Xlog.i(TAG,"BroadcastReceiver: network mode change succeed! set the new value.");
					android.provider.Settings.Secure.putInt(
									getContentResolver(),android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(GeminiUtils.NEW_NETWORK_MODE, 0));
				}
				
				if(isDialogShowing(DIALOG_NETWORK_MODE_CHANGE)){
                    removeDialog(DIALOG_NETWORK_MODE_CHANGE);
				}
			}
            	
        }
	};
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_mode_settings);

        mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);    
        mIntentFilter.addAction(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE);
        mNetworkMode = (NetworkModePreference) findPreference(KEY_NETWORK_MODE_SETTING); 
        mNetworkMode.setMultiple(true);
        
        getActivity().registerReceiver(mSimReceiver, mIntentFilter);
	    mNetworkMode.setOnPreferenceChangeListener(this);  
	    
        //get iTelephony
		iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));



    }
 
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    getActivity().unregisterReceiver(mSimReceiver);

	}



	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        int settingsNetworkMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE,
                preferredNetworkMode);
        
        if((settingsNetworkMode>=0)&&(settingsNetworkMode<=2)) {
            mNetworkMode.setValue(Integer.toString(settingsNetworkMode));
        }
        
		if(iTelephony == null){
			return;
		}
		
		try{
	    	boolean Sim1Insert = iTelephony.isSimInsert(Phone.GEMINI_SIM_1);
	        boolean Sim1Ready = false;
	        
	        if(Sim1Insert == true) {
	           	Sim1Ready = iTelephony.isRadioOnGemini(Phone.GEMINI_SIM_1);
	        }
	        
	        mNetworkMode.setEnabled(Sim1Ready);
	        
		}catch(RemoteException e){
			Xlog.e(TAG, "RemoteException happens......");
		}

	}
	

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
    	
        final String key = arg0.getKey();
        
        if (KEY_NETWORK_MODE_SETTING.equals(key)) {
        	
            int oldNetworkMode = android.provider.Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            
    		int newNetworkMode = Integer.valueOf((String) arg1).intValue();
    		
    		newNetworkMode = GeminiUtils.getNetworkMode(newNetworkMode);
    		
    		Settings.Secure.putInt(getContentResolver(),Settings.Secure.PREFERRED_NETWORK_MODE,
    				newNetworkMode);	
    		
    		if(newNetworkMode != oldNetworkMode) {
    			
                Intent intent = new Intent(GeminiUtils.NETWORK_MODE_CHANGE_BROADCAST,
                        null);
                intent.putExtra(GeminiUtils.OLD_NETWORK_MODE, oldNetworkMode);
                intent.putExtra(GeminiUtils.NETWORK_MODE_CHANGE_BROADCAST, newNetworkMode);
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
       			showDialog(DIALOG_NETWORK_MODE_CHANGE);
       			setCancelable(false);
       			getActivity().sendBroadcast(intent);
                Xlog.i(TAG, "Send broadcast of "+GeminiUtils.NETWORK_MODE_CHANGE_BROADCAST);
    			
    		}
        }
        return true;
    }
    

    @Override
	public Dialog onCreateDialog(int id) {
    	  ProgressDialog dialog = new ProgressDialog(getActivity());
        
    	  switch (id) {
            case DIALOG_NETWORK_MODE_CHANGE:                
	        	 dialog.setMessage(getResources().getString(R.string.gemini_data_connection_progress_message));
	        	 dialog.setIndeterminate(true);
	             return dialog; 
           default:
               return null;
        }
    }
    
     
}
//MTK_OP02_PROTECT_END
