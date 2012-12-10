/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.settings.nfc;

import android.app.ActionBar;
import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.mediatek.xlog.Xlog;

public class NfcSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "NfcSettings";

    private static final String KEY_NFC_SOUND = "nfc_sound";
    private static final String KEY_NFC_VIBRATION = "nfc_vibrate";
    private static final String KEY_NFC_TAG_RW = "nfc_rw_tag";
    private static final String KEY_NFC_PROMPT = "nfc_prompt";
    private static final String KEY_NFC_EMULATION = "nfc_card_emulation";
    private static final String KEY_RECEIVE_BEAM = "nfc_receive_android_beam";

    private MtkNfcEnabler mNfcEnabler;
    private NfcAdapter mNfcAdapter;
    private CheckBoxPreference mNfcSoundPref;
    private CheckBoxPreference mNfcVibrationPref;
    private CheckBoxPreference mNfcRwTagPref;
    private CheckBoxPreference mNfcPromptPref;
    private ListPreference mNfcEmulationPref;
    private CheckBoxPreference mNfcReceiveBeamPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.nfc_settings);

        Activity activity = getActivity();

        Switch mActionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders()
                    || !preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources()
                        .getDimensionPixelSize(
                                R.dimen.action_bar_switch_padding);
                mActionBarSwitch.setPadding(0, 0, padding, 0);
                activity.getActionBar().setDisplayOptions(
                        ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(
                        mActionBarSwitch,
                        new ActionBar.LayoutParams(
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                activity.getActionBar().setTitle(
                        R.string.nfc_quick_toggle_title);
            }
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        mNfcEnabler = new MtkNfcEnabler(activity, null, null, mActionBarSwitch, mNfcAdapter);

        initPreferences();

    }

    /**
     * According to the key find the corresponding preference
     */
    private void initPreferences() {
        mNfcSoundPref = (CheckBoxPreference) findPreference(KEY_NFC_SOUND);
        mNfcVibrationPref = (CheckBoxPreference) findPreference(KEY_NFC_VIBRATION);
        mNfcRwTagPref = (CheckBoxPreference) findPreference(KEY_NFC_TAG_RW);
        mNfcPromptPref = (CheckBoxPreference) findPreference(KEY_NFC_PROMPT);
        mNfcEmulationPref = (ListPreference) findPreference(KEY_NFC_EMULATION);
        mNfcEmulationPref.setOnPreferenceChangeListener(this);
        mNfcReceiveBeamPref = (CheckBoxPreference) findPreference(KEY_RECEIVE_BEAM);
    }

    /**
     * update the preference according to the status of NfcAdapter settings
     */
    private void updatePreferences() {
        mNfcSoundPref.setChecked(mNfcAdapter.isSoundEnabled());
        mNfcVibrationPref.setChecked(mNfcAdapter.isVibrationEnabled());
        mNfcRwTagPref.setChecked(mNfcAdapter.isTagRwEnabled());
        mNfcPromptPref.setChecked(mNfcAdapter.isPromptEnabled());
        mNfcEmulationPref.setValue(String.valueOf(mNfcAdapter
                .getCardEmulationConfig()));
        mNfcReceiveBeamPref.setChecked(mNfcAdapter.isP2pRecvEnabled());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Xlog.d(TAG, "onPreferenceChange");
        if (preference == mNfcEmulationPref) {
            Xlog.d(TAG, "card emulation : " + mNfcEmulationPref.getValue()
                    + "newValue: " + newValue);
            mNfcAdapter.setCardEmulationConfig(Integer.parseInt(newValue
                    .toString()));
        }
        return true;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mNfcSoundPref) {
            Xlog.d(TAG, "click sound");
            if (mNfcSoundPref.isChecked()) {
                mNfcAdapter.enableSound();
            } else {
                mNfcAdapter.disableSound();
            }
        } else if (preference == mNfcVibrationPref) {
            Xlog.d(TAG, "click vibrate");
            if (mNfcVibrationPref.isChecked()) {
                mNfcAdapter.enableVibration();
            } else {
                mNfcAdapter.disableVibration();
            }
        } else if (preference == mNfcRwTagPref) {
            Xlog.d(TAG, "click RW tag");
            if (mNfcRwTagPref.isChecked()) {
                mNfcAdapter.enableTagRw();
            } else {
                mNfcAdapter.disableTagRw();
            }
        } else if (preference == mNfcPromptPref) {
            Xlog.d(TAG, "click prompt");
            if (mNfcPromptPref.isChecked()) {
                mNfcAdapter.enablePrompt();
            } else {
                mNfcAdapter.disablePrompt();
            }
        } else if (preference == mNfcReceiveBeamPref) {
            Xlog.d(TAG, "click receive beam");
            if (mNfcReceiveBeamPref.isChecked()) {
                mNfcAdapter.enableP2pRecv();
            } else {
                mNfcAdapter.disableP2pRecv();
            }
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        if (mNfcEnabler != null) {
            mNfcEnabler.resume();
        }
        updatePreferences();
    }

    public void onPause() {
        super.onPause();

        if (mNfcEnabler != null) {
            mNfcEnabler.pause();
        }
    }
}
