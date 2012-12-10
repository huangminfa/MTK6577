package com.android.phone;

import java.util.ArrayList;

import com.android.internal.telephony.CommandsInterface;
import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CallForwardInfo;
import android.widget.Toast;
import com.mediatek.xlog.Xlog;
import android.view.MenuItem;

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandsInterface;

/* Fion add start */
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
/* Fion add end */

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;


public class GsmUmtsCallForwardOptions extends TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "Settings/GsmUmtsCallForwardOptions";
    private final boolean DBG = true;//(PhoneApp.DBG_LEVEL >= 2);

    private static final String NUM_PROJECTION[] = {Phone.NUMBER};

    private static final String BUTTON_CFU_KEY   = "button_cfu_key";
    private static final String BUTTON_CFB_KEY   = "button_cfb_key";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key";

    private static final String KEY_TOGGLE = "toggle";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_ITEM_STATUS = "item_status";

    private CallForwardEditPreference mButtonCFU;
    private CallForwardEditPreference mButtonCFB;
    private CallForwardEditPreference mButtonCFNRy;
    private CallForwardEditPreference mButtonCFNRc;

    private TelephonyManager mTelephonyManager;

    private boolean isFinished = false;
    
    private boolean isVtSetting = false;

/* Fion add start */
    public static final int DEFAULT_SIM = 2; /* 0: SIM1, 1: SIM2 */

    private int mSimId = DEFAULT_SIM;
/* Fion add end */

    private final ArrayList<CallForwardEditPreference> mPreferences =
            new ArrayList<CallForwardEditPreference> ();
    private int mInitIndex= 0;

    private boolean mFirstResume = false;
    private Bundle mIcicle;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
	            String action = intent.getAction();
	            
		        if ((action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        					&&intent.getBooleanExtra("state", false))||
	            		(action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)
	            				&&(intent.getIntExtra(Intent.EXTRA_DUAL_SIM_MODE, -1) == 0))) {
		            //Xlog.d("GsmUmtsCallForwardoptions", "Received airplane changed");
                            finish();
		        }
		    }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

/* Fion add start */
        if (CallSettings.isMultipleSim())
        {
            PhoneApp app = PhoneApp.getInstance();
            mSimId = getIntent().getIntExtra(app.phone.GEMINI_SIM_ID_KEY, -1);
        }
        
        isVtSetting = getIntent().getBooleanExtra("ISVT", false);
        Xlog.d("GsmUmtsCallForwardoptions", "Sim Id : " + mSimId + "  for VT setting = " + isVtSetting);		
/* Fion add end */
        
        isReady();
        addPreferencesFromResource(R.xml.callforward_options);

        PreferenceScreen prefSet = getPreferenceScreen();
        mButtonCFU   = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFU_KEY);
        mButtonCFB   = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFB_KEY);
        mButtonCFNRy = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRY_KEY);


        ///M: [ALPS00357990] set Call forward statement in EVDO mode {
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	if (FeatureOption.EVDO_DT_SUPPORT
                && mSimId == com.android.internal.telephony.Phone.GEMINI_SIM_1
                && mTelephonyManager.getPhoneTypeGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) 
                    == com.android.internal.telephony.Phone.PHONE_TYPE_GSM) {
            prefSet.removePreference(findPreference(BUTTON_CFNRC_KEY));
            mButtonCFNRy.setTitle(R.string.cb_unreachable_unanswered);
        } else {
	    mButtonCFNRc = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRC_KEY);
	}
        ///@}

        if (mButtonCFU != null) {
            mButtonCFU.setParentActivity(this, mButtonCFU.reason);
        }
        if (mButtonCFB != null) {
            mButtonCFB.setParentActivity(this, mButtonCFB.reason);
        }
        if (mButtonCFNRy != null) {
            mButtonCFNRy.setParentActivity(this, mButtonCFNRy.reason);
        }
        if (mButtonCFNRc != null) {
            mButtonCFNRc.setParentActivity(this, mButtonCFNRc.reason);
        }
        /*mButtonCFU.setVtCFoward(isVtSetting);
        mButtonCFB.setVtCFoward(isVtSetting);
        mButtonCFNRy.setVtCFoward(isVtSetting);
        mButtonCFNRc.setVtCFoward(isVtSetting);*/
        
        if (isVtSetting)
        {
                if (mButtonCFU != null) {
        	    mButtonCFU.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
                }
                if (mButtonCFB != null) {
        	    mButtonCFB.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
                }
                if (mButtonCFNRy != null) {
        	    mButtonCFNRy.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
                }
                if (mButtonCFNRc != null) {
        	    mButtonCFNRc.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
                }
        }
        if (mButtonCFU != null) {
            mPreferences.add(mButtonCFU);
        }
        if (mButtonCFB != null) {
            mPreferences.add(mButtonCFB);
        }
        if (mButtonCFNRy != null) {
            mPreferences.add(mButtonCFNRy);
        }
        if (mButtonCFNRc != null) {
            mPreferences.add(mButtonCFNRc);
        }

        //Set the toggle in order to meet the recover dialog in correct status
        if (null != icicle)
        {
            for (CallForwardEditPreference pref : mPreferences) {
            	if (null != pref) {
                    Bundle bundle = icicle.getParcelable(pref.getKey());
                    if (null != bundle) {
                        pref.setToggled(bundle.getBoolean(KEY_TOGGLE));
                    }
            	}
            }
        }

        // we wait to do the initialization until onResume so that the
        // TimeConsumingPreferenceActivity dialog can display as it
        // relies on onResume / onPause to maintain its foreground state.

        mFirstResume = true;
        PhoneUtils.setMmiFinished(false);
        mIcicle = icicle;
        if (null != getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME))
        {
            setTitle(getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME));
        }
        
        IntentFilter intentFilter =
            new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            intentFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        }

        registerReceiver(mIntentReceiver, intentFilter);

/*        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFirstResume == true){
            mInitIndex = 0;
            Xlog.d(LOG_TAG, "START INIT(onResume1): mInitIndex is  " + mInitIndex);	
            mPreferences.get(mInitIndex).init(this, false, mSimId);	  	
            mFirstResume = false;
	} else if (PhoneUtils.getMmiFinished() == true){
            mInitIndex = 0;
	    Xlog.d(LOG_TAG, "START INIT(onResume2): mInitIndex is  " + mInitIndex);	
	    mPreferences.get(mInitIndex).init(this, false, mSimId);
	    PhoneUtils.setMmiFinished(false);
	} else {
	    Xlog.d(LOG_TAG, "No change, so don't query!");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        removeDialog();
        super.onSaveInstanceState(outState);

        for (CallForwardEditPreference pref : mPreferences) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_TOGGLE, pref.isToggled());
            bundle.putBoolean(KEY_ITEM_STATUS, pref.isEnabled());
            if (pref.callForwardInfo != null) {
                bundle.putString(KEY_NUMBER, pref.callForwardInfo.number);
                bundle.putInt(KEY_STATUS, pref.callForwardInfo.status);
            }
            outState.putParcelable(pref.getKey(), bundle);
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            //mInitIndex++;
            if (mPreferences.get(mInitIndex++).isSuccess())
            {
/* Fion add start */
            Xlog.d(LOG_TAG, "START INIT(onFinished): mInitIndex is  " + mInitIndex);	
            mPreferences.get(mInitIndex).init(this, false, mSimId);
/* Fion add end */
        }
            else
            {
                for (int i = mInitIndex; i < mPreferences.size(); ++i)
                {
                    mPreferences.get(i).setEnabled(false);
                }
                mInitIndex = mPreferences.size();
            }
        }

        super.onFinished(preference, reading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Xlog.d(LOG_TAG, "onActivityResult: done");
        if (resultCode != RESULT_OK) {
            if (DBG) Xlog.d(LOG_TAG, "onActivityResult: contact picker result not OK.");
            return;
        }
        Cursor cursor = getContentResolver().query(data.getData(),
                NUM_PROJECTION, null, null, null);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (DBG) Xlog.d(LOG_TAG, "onActivityResult: bad contact data, no results found.");
            return;
        }

        switch (requestCode) {
            case CommandsInterface.CF_REASON_UNCONDITIONAL:
                if (mButtonCFU != null) {
                    mButtonCFU.onPickActivityResult(cursor.getString(0));
                }
                break;
            case CommandsInterface.CF_REASON_BUSY:
                if (mButtonCFB != null) {
                    mButtonCFB.onPickActivityResult(cursor.getString(0));
                }
                break;
            case CommandsInterface.CF_REASON_NO_REPLY:
                if (mButtonCFNRy != null) {
                    mButtonCFNRy.onPickActivityResult(cursor.getString(0));
                }
                break;
            case CommandsInterface.CF_REASON_NOT_REACHABLE:
                if (mButtonCFNRc != null) {
                    mButtonCFNRc.onPickActivityResult(cursor.getString(0));
                }
                break;
            default:
                // TODO: may need exception here.
        }
    }
    public void onDestroy(){
        if (mButtonCFU != null) {
            mButtonCFU.setStatus(true);
        }
        if (mButtonCFB != null) {
            mButtonCFB.setStatus(true);
        }
        if (mButtonCFNRy != null) {
            mButtonCFNRy.setStatus(true);
        }
        if (mButtonCFNRc != null) {
            mButtonCFNRc.setStatus(true);
        }
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }
    
    private void isReady() {
        com.android.internal.telephony.Phone  phone = PhoneApp.getPhone();
/* Fion add start */
        int state;
        if (CallSettings.isMultipleSim())
        {
            state=((GeminiPhone)phone).getServiceStateGemini(mSimId).getState();
        }
        else
        {
            state=phone.getServiceState().getState();
        }
/* Fion add end */

        if(state!=android.telephony.ServiceState.STATE_IN_SERVICE) {
        	finish();
        	Toast.makeText(this,getString(R.string.net_or_simcard_busy),Toast.LENGTH_SHORT).show();
        }
    }
    
    //Refresh the settings when disable CFU
    public void refreshSettings(boolean bNeed)
    {
        if (bNeed)
        {
            mInitIndex = 1;
            Xlog.d(LOG_TAG, "START INIT(refreshSettings): mInitIndex is  " + mInitIndex);	
            mPreferences.get(mInitIndex).init(this, false, mSimId);
        }
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            CallFeaturesSetting.goUpToTopLevelSetting(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
