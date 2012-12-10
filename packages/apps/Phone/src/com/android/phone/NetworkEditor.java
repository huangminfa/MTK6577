package com.android.phone;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class NetworkEditor extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_DISCARD = Menu.FIRST + 2;

    private static final String BUTTON_PRIORITY_KEY = "priority_key";
    private static final String BUTTON_NEWWORK_MODE_KEY = "network_mode_key";
    private static final String BUTTON_NETWORK_ID_KEY = "network_id_key";
    
    public static final String PLMN_NAME = "plmn_name";
    public static final String PLMN_CODE = "plmn_code";
    public static final String PLMN_PRIORITY = "plmn_priority";
    public static final String PLMN_SERVICE = "plmn_service";
    public static final String PLMN_SIZE = "plmn_size";
    public static final String PLMN_ADD = "plmn_add";
    
    private EditTextPreference mNetworkId = null;
    private EditTextPreference mPriority = null;
    private ListPreference mNetworkMode = null;

    private String mPLMNName;

    public static final int RESULT_MODIFY = 100;
    public static final int RESULT_DELETE = 200;


    private String mNotSet = null;

    private TelephonyManager mTelephonyManager;
    private boolean mAirplaneModeEnabled = false;
    private int mDualSimMode = -1;
    private IntentFilter mIntentFilter;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
            case TelephonyManager.CALL_STATE_IDLE:
                setScreenEnabled();
                break;
            default:
                break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); 
            if(action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mAirplaneModeEnabled = intent.getBooleanExtra("state", false);
                setScreenEnabled();
            } else if(action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)){
                mDualSimMode = intent.getIntExtra(Intent.EXTRA_DUAL_SIM_MODE, -1);
                setScreenEnabled();
            }
        }
    };

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.plmn_editor);
        mNotSet = getResources().getString(R.string.voicemail_number_not_set);

        mNetworkId = (EditTextPreference)findPreference(BUTTON_NETWORK_ID_KEY);
        mPriority = (EditTextPreference)findPreference(BUTTON_PRIORITY_KEY);
        mNetworkMode = (ListPreference)findPreference(BUTTON_NEWWORK_MODE_KEY);
        mNetworkId.setOnPreferenceChangeListener(this);
        mPriority.setOnPreferenceChangeListener(this);
        mNetworkMode.setOnPreferenceChangeListener(this);


        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED); 
        if(CallSettings.isMultipleSim()){
            mIntentFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        }
        registerReceiver(mReceiver, mIntentFilter);

    }
    
    protected void onResume() {
        super.onResume();
        createNetworkInfo(getIntent());
        mAirplaneModeEnabled = android.provider.Settings.System.getInt(getContentResolver(),
                android.provider.Settings.System.AIRPLANE_MODE_ON, -1)==1;
        if(CallSettings.isMultipleSim()) {
            mDualSimMode = android.provider.Settings.System.getInt(getContentResolver(), 
                    android.provider.Settings.System.DUAL_SIM_MODE_SETTING, -1);
        }
        setScreenEnabled();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String value = objValue.toString();
        if (preference == mNetworkId) {
            mNetworkId.setSummary(checkNull(value));
        } else if (preference == mPriority) {
            mPriority.setSummary(checkNull(value));
        } else if (preference == mNetworkMode) {
            mNetworkMode.setValue(value);
            mNetworkMode.setSummary(getResources().getStringArray(R.array.plmn_prefer_network_mode_choices)[Integer.parseInt(value)]);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(!getIntent().getBooleanExtra(PLMN_ADD, false)){
            menu.add(0, MENU_DELETE, 0, com.android.internal.R.string.delete);
        }
        menu.add(0, MENU_SAVE, 0, R.string.save);
        menu.add(0, MENU_DISCARD, 0, com.android.internal.R.string.cancel);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        super.onMenuOpened(featureId, menu);
        boolean isShouldEnabled = false;
        boolean isIdle = (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE);
        isShouldEnabled = isIdle && (!mAirplaneModeEnabled) && (mDualSimMode != 0);
        boolean isEmpty = mNotSet.equals(mNetworkId.getSummary()) || mNotSet.equals(mPriority.getSummary());
        if(menu != null){
            menu.setGroupEnabled(0, isShouldEnabled);
            if(getIntent().getBooleanExtra(PLMN_ADD, true)){
                menu.getItem(0).setEnabled(isShouldEnabled && !isEmpty);
            } else {
                menu.getItem(1).setEnabled(isShouldEnabled && !isEmpty);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_DELETE:
            setRemovedNetwork();
            break;
        case MENU_SAVE:
            validateAndSetResult();
            break;
        case MENU_DISCARD:
            break;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void validateAndSetResult() {
        Intent intent = new Intent(this, PLMNListPreference.class);
        setResult(RESULT_MODIFY, intent);
        genNetworkInfo(intent);
    }
    
    private void genNetworkInfo(Intent intent) {
        intent.putExtra(NetworkEditor.PLMN_NAME, checkNotSet(mPLMNName));
        intent.putExtra(NetworkEditor.PLMN_CODE, mNetworkId.getSummary());
        int priority = 0;
        int size = getIntent().getIntExtra(PLMN_SIZE, 0);
        try{
            priority = Integer.parseInt(String.valueOf(mPriority.getSummary()));
        } catch (NumberFormatException e){
        }
        if(getIntent().getBooleanExtra(PLMN_ADD, false)){
            if( priority > size ){
                priority = size;
            }
        } else {
            if( priority >= size ){
                priority = size - 1;
            }
        }
        intent.putExtra(NetworkEditor.PLMN_PRIORITY, priority);
        try{
            intent.putExtra(NetworkEditor.PLMN_SERVICE, 
                    covertApNW2Ril(Integer.parseInt(String.valueOf(mNetworkMode.getValue()))));
        } catch (NumberFormatException e){
            intent.putExtra(NetworkEditor.PLMN_SERVICE, covertApNW2Ril(0));
        }
    }
    
    private void setRemovedNetwork() {
        Intent intent = new Intent(this, PLMNListPreference.class);
        setResult(RESULT_DELETE, intent);
        genNetworkInfo(intent);
    }

    public static int covertRilNW2Ap(int mode) {
        int result = 0;
        if (mode >= 5) {
            result = 2;
        } else if ((mode & 0x04) != 0) {
            result = 1;
        } else {
            result = 0;
        }
        return result;
    }
    
    public static int covertApNW2Ril(int mode) {
        int result = 0;
        if (mode == 2) {
            result = 0x5;
        } else if (mode == 1) {
            result = 0x4;
        } else {
            result = 0x1;
        }
        return result;
    }

    private void createNetworkInfo(Intent intent) {
        mPLMNName = intent.getStringExtra(PLMN_NAME);
        String number = intent.getStringExtra(PLMN_CODE);
        mNetworkId.setSummary(checkNull(number));
        mNetworkId.setText(number);
        int priority = intent.getIntExtra(PLMN_PRIORITY, 0);
        mPriority.setSummary(String.valueOf(priority));
        mPriority.setText(String.valueOf(priority));
        int act = intent.getIntExtra(PLMN_SERVICE, 0);
        act = covertRilNW2Ap(act);
        if(act < 0 || act > 2){
            act = 0;
        }
        mNetworkMode.setSummary(getResources().getStringArray(R.array.plmn_prefer_network_mode_choices)[act]);
        mNetworkMode.setValue(String.valueOf(act));
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals(mNotSet)) {
            return "";
        } else {
            return value;
        }
    }

    private String checkNull(String value) {
        if (value == null || value.length() == 0) {
            return mNotSet;
        } else {
            return value;
        }
    }

    private void setScreenEnabled(){
        boolean isShouldEnabled = false;
        boolean isIdle = (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE);
        isShouldEnabled = isIdle && (!mAirplaneModeEnabled) && (mDualSimMode != 0);
        getPreferenceScreen().setEnabled(isShouldEnabled);
        invalidateOptionsMenu();
    }
}
