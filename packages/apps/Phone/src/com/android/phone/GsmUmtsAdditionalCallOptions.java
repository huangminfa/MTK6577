package com.android.phone;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.mediatek.xlog.Xlog;

/* Fion add start */
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
/* Fion add end */

public class GsmUmtsAdditionalCallOptions extends
        TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "Settings/Callsettings";
    private final boolean DBG = true; // (PhoneApp.DBG_LEVEL >= 2);

    private static final String BUTTON_CLIR_KEY  = "button_clir_key";
    private static final String BUTTON_CW_KEY    = "button_cw_key";

    private CLIRListPreference mCLIRButton;
    private CallWaitingCheckBoxPreference mCWButton;

    private ArrayList<Preference> mPreferences = new ArrayList<Preference> ();
    private int mInitIndex= 0;
    Bundle mIcicle = null;
    boolean mFirstResume = false;

/* Fion add start */
    public static final int DEFAULT_SIM = 2; /* 0: SIM1, 1: SIM2 */
    private int mSimId = DEFAULT_SIM;
/* Fion add end */
    
    private boolean isVtSetting = false;

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
        Xlog.d(LOG_TAG, "[GsmUmtsAdditionalCallOptions]Sim Id : " + mSimId + " ISVT = " + isVtSetting);		
/* Fion add end */

        addPreferencesFromResource(R.xml.gsm_umts_additional_options);

        PreferenceScreen prefSet = getPreferenceScreen();
        mCLIRButton = (CLIRListPreference) prefSet.findPreference(BUTTON_CLIR_KEY);
        mCWButton = (CallWaitingCheckBoxPreference) prefSet.findPreference(BUTTON_CW_KEY);

        mPreferences.add(mCLIRButton);
        mPreferences.add(mCWButton);
        mIcicle = icicle;
        mFirstResume = true;
        
        if (null != getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME))
        {
            setTitle(getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME));
        }
        
        //mCLIRButton.setVtSetting(isVtSetting);
        if (isVtSetting)
        {
            mCWButton.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
            //Our VT don't support Call Waiting, so
            /*mCWButton.setChecked(false);
			mCWButton.setEnabled(false);*/
        }
        
        PhoneUtils.setMmiFinished(false);

/*        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
    }

    public void onResume()
    {
        super.onResume();
        mInitIndex = 0;
        if (mFirstResume == true)
        {
            mCLIRButton.init(this, false, mSimId);
            mFirstResume = false;
        }
        else
        {
        	if (PhoneUtils.getMmiFinished() == true)
        	{
                     mCLIRButton.init(this, false, mSimId);
                     PhoneUtils.setMmiFinished(false);
        	}
        	else
        	{
        		mInitIndex = mPreferences.size() - 1;
            }
        }
        
		/*if (isVtSetting) {
			//Our VT don't support Call Waiting, so
			mCWButton.setChecked(false);
			mCWButton.setEnabled(false);
		}*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCLIRButton.clirArray != null) {
            outState.putIntArray(mCLIRButton.getKey(), mCLIRButton.clirArray);
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            Preference pref = mPreferences.get(mInitIndex);
            if (pref instanceof CallWaitingCheckBoxPreference) {
/* Fion add start */
                ((CallWaitingCheckBoxPreference) pref).init(this, false, mSimId);
/* Fion add end */
            }
        }
        super.onFinished(preference, reading);
        
        /*if (isVtSetting) {
			//Our VT don't support Call Waiting, so
			mCWButton.setChecked(false);
			mCWButton.setEnabled(false);
		}*/
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
