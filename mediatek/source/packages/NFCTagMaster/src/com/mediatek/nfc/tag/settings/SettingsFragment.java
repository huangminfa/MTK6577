package com.mediatek.nfc.tag.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.utils.Utils;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = Utils.TAG + "/SettingsFragment";

    private ListPreference mHistorySizePref;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.logv(TAG, "-->onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Utils.logv(TAG, "-->onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_fragment);

        mSharedPreferences = getActivity().getSharedPreferences(Utils.CONFIG_FILE_NAME,
                Context.MODE_PRIVATE);
        mHistorySizePref = (ListPreference) findPreference(Utils.KEY_HISTORY_SIZE);
        mHistorySizePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        int historySize = mSharedPreferences.getInt(Utils.KEY_HISTORY_SIZE,
                Utils.DEFAULT_VALUE_HISTORY_SIZE);

        mHistorySizePref.setValue(String.valueOf(historySize));
        mHistorySizePref.setSummary(String.valueOf(historySize));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHistorySizePref) {
            Utils.logd(TAG, "History size new value = " + newValue);
            int newSize = Integer.parseInt((String) newValue);
            mSharedPreferences.edit().putInt(Utils.KEY_HISTORY_SIZE, newSize).commit();
            mHistorySizePref.setSummary(String.valueOf(newSize));

            int deletedRecordNum = Utils.limitHistorySize(getActivity(), newSize);
            Utils
                    .logi(TAG, "After adjust history size, deleted history size = "
                            + deletedRecordNum);
            return true;
        }
        return false;
    }
}
