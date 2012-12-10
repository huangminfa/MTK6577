package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import com.mediatek.xlog.Xlog;

import com.mediatek.tvOut.*;

public class TVOutSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    
    private static final String TAG = "TV_OUT";
    private static final String PROFILE_TV_OUT_SETTINGS = "tv_out";

    private static final String KEY_TVOUT_ENABLE = "tvout_enable";
    private static final String KEY_TV_SYSTEM = "tv_system";

    private static boolean mIsFirst = true;
    private static boolean mIsFirstFire = true;
    private CheckBoxPreference mEnablePreference;
    private ListPreference mTVSystem;
    private TvOut mTvOut;
    private static boolean mNeedUserEnable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tvout_settings);
        mTvOut = new TvOut();
        mEnablePreference = (CheckBoxPreference) findPreference(KEY_TVOUT_ENABLE);
        mTVSystem = (ListPreference) findPreference(KEY_TV_SYSTEM);

        mNeedUserEnable = mTvOut.isShowButton();
        if(mNeedUserEnable == true){
            mEnablePreference.setOnPreferenceChangeListener(this);
            Xlog.i(TAG,"[TVOUT] enable button ");
        }
        mTVSystem.setOnPreferenceChangeListener(this);


        if(mIsFirst){
            Xlog.i(TAG,"First launch");
            mEnablePreference.setChecked(false);
            mIsFirst = false;
        }

        PreferenceScreen root = this.getPreferenceScreen();

        if(mNeedUserEnable == false){
            root.removePreference(mEnablePreference);
            Xlog.i(TAG,"[TVOUT] no enable button ,remove");
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if(mNeedUserEnable == true){
            if (KEY_TVOUT_ENABLE.equals(key)) {
                if (objValue.equals(false)) {
                    mTvOut.enableTvOut(false);
                } else{
                    if(mIsFirstFire){
                        String sys = mTVSystem.getValue();
                        Xlog.i(TAG,"First enable, system type is:" + sys);
                        if (sys.equals("NTSC")){
                            mTvOut.setTvSystem(TvOut.NTSC);
                        }
                        else{
                            mTvOut.setTvSystem(TvOut.PAL);
                        }
                        mIsFirstFire = false;
                    }
                    mTvOut.enableTvOut(true);
                }
            } 
        }
        if (KEY_TV_SYSTEM.equals(key)) {
            SharedPreferences setting = getActivity().getSharedPreferences(
                    PROFILE_TV_OUT_SETTINGS, Context.MODE_PRIVATE);
            setting.edit().putString(KEY_TV_SYSTEM, objValue.toString())
                    .commit();
            if (objValue.equals("NTSC")){
                mTvOut.setTvSystem(TvOut.NTSC);
            }
            else{
                mTvOut.setTvSystem(TvOut.PAL);
            }   
            mTVSystem.setSummary(objValue.toString());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        SharedPreferences setting = getActivity().getSharedPreferences(
                PROFILE_TV_OUT_SETTINGS, Context.MODE_PRIVATE);
        String tv_system = setting.getString(KEY_TV_SYSTEM, "NTSC");
        Xlog.i(TAG,"onResume, tv system is:" + tv_system);
        mTVSystem.setValue(tv_system);
        mTVSystem.setSummary(tv_system);
        super.onResume();
    }

}

