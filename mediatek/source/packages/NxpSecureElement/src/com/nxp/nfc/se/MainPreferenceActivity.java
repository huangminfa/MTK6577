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

/*
 * Copyright (C) 2010 NXP Semiconductors
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

package com.nxp.nfc.se;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author nxp38233
 *
 */
public class MainPreferenceActivity extends PreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener, OnSharedPreferenceChangeListener,
        DialogInterface.OnClickListener {

    /*  */
    private static final String TAG = "NxpSecureElement";
    /*  */
    private static final int DIALOG_NFC_OFF = 1;
    /*  */
    private ListPreference listPreferenceSeId;
    /*  */
    private MyPreference mSetGetSE;
    /*  */
    private CheckBoxPreference mCheckBoxCePreference;
    /*  */
    private MyPreference mActiveSwp;
    /*  */
    private MyPreference mExit;
    /*  */
    private NfcAdapter mNfc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Starting...");

        addPreferencesFromResource(R.layout.main_activity);

        listPreferenceSeId = (ListPreference) getPreferenceScreen().findPreference(
                Constants.KEY_SE_ID_LIST);
        listPreferenceSeId.setSummary(listPreferenceSeId.getEntry());
        listPreferenceSeId.setOnPreferenceChangeListener(this);
        listPreferenceSeId.setSummary("Select SE to enable");

        mSetGetSE = (MyPreference) getPreferenceScreen().findPreference(
                Constants.KEY_SET_GET_SE);
        mSetGetSE.setOnPreferenceClickListener(this);

        mCheckBoxCePreference = (CheckBoxPreference) getPreferenceScreen().findPreference(
                Constants.KEY_CE_CHECKBOX);

        mActiveSwp = (MyPreference) getPreferenceScreen().findPreference(
                Constants.KEY_ACTIVE_SWP);
        mActiveSwp.setOnPreferenceClickListener(this);

        mExit = (MyPreference) getPreferenceScreen().findPreference(Constants.KEY_EXIT);
        mExit.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Check again nfc state when application is pushed on foreground after to be put in background
        mNfc = NfcAdapter.getDefaultAdapter(this);
        if (mNfc == null || !mNfc.isEnabled()) {
            listPreferenceSeId.setEnabled(false);
            mSetGetSE.setEnabled(false);
            mCheckBoxCePreference.setEnabled(false);
            mCheckBoxCePreference.setChecked(false);
            mActiveSwp.setEnabled(false);
            showDialog(DIALOG_NFC_OFF);
        } else {
            listPreferenceSeId.setEnabled(true);
            mCheckBoxCePreference.setEnabled(false);
            mActiveSwp.setEnabled(false);            
            setSecureElementUI(null);   
        }        
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(int dialogId, Bundle args) {
        if (dialogId == DIALOG_NFC_OFF) {
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_nfc_off)
                    .setMessage(R.string.dialog_text_nfc_off)
                    .setPositiveButton(R.string.button_settings, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setCancelable(true)
                    .create();
        }

        throw new IllegalArgumentException("Unknown dialog id " + dialogId);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Thake the user to the wireless settings panel, where they can
            // enable NFC
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        }
        dialog.dismiss();
    }

    private void setSecureElementUI(Object newValue) {
        int index = 0;
        if (listPreferenceSeId != null) {
            if (newValue == null) {
                try {
                    index = Byte.valueOf(listPreferenceSeId.getValue());                    
                } catch (Exception e) {
                    // Expected Exception for the first time the app is started
                    index = 2; // None
                }
                Log.d(TAG, "index = " + index);
            } else {
                index = listPreferenceSeId.findIndexOfValue((String) newValue);
            }
            Log.i(TAG, "### listPreferenceLayerId index =  " + index + " ###");
            
            if (index == 2)
                mSetGetSE.setEnabled(false);
            else
                mSetGetSE.setEnabled(true);
            
            listPreferenceSeId.setValueIndex(index);

            switch (index) {
                case 0:
                    /* Select SMX */
                    try {
                        mNfc.selectDefaultSecureElement(NfcAdapter.SMART_MX_ID);
                        Toast.makeText(this, "Smart MX selected", Toast.LENGTH_SHORT).show();
                        mSetGetSE.setSummary("");
                        listPreferenceSeId.setSummary(listPreferenceSeId.getEntry() + " selected");
                        mCheckBoxCePreference.setEnabled(true);
                        if (newValue != null) {
                            mCheckBoxCePreference.setChecked(true);
                        }    
                    } catch (IOException e) {
                        Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                        listPreferenceSeId.setSummary("select Smart MX failed");
                        mSetGetSE.setSummary("No Secure Element selected!");
                        mCheckBoxCePreference.setEnabled(false);
                        mCheckBoxCePreference.setChecked(false);
                    }
                    mActiveSwp.setEnabled(false);
                    break;
                case 1:
                    /* Select UICC */
                    try {
                        mNfc.selectDefaultSecureElement(NfcAdapter.UICC_ID);
                        Toast.makeText(this, "UICC selected", Toast.LENGTH_SHORT).show();
                        mSetGetSE.setSummary("");
                        listPreferenceSeId.setSummary(listPreferenceSeId.getEntry() + " selected");
                        mCheckBoxCePreference.setEnabled(true);
                        if (newValue != null) {
                            mCheckBoxCePreference.setChecked(true);
                        }
                        mActiveSwp.setEnabled(true);
                    } catch (IOException e) {
                        Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                        listPreferenceSeId.setSummary("select UICC failed");
                        mSetGetSE.setSummary("No Secure Element selected!");
                        mCheckBoxCePreference.setEnabled(false);
                        mCheckBoxCePreference.setChecked(false);
                        mActiveSwp.setEnabled(false);
                    }
                    break;
                case 2:
                    /* DeSelect SE */
                    try {
                        mNfc.deSelectedSecureElement();
                        Toast.makeText(this, "SE deselected", Toast.LENGTH_SHORT).show();
                        mSetGetSE.setSummary("");
                        listPreferenceSeId.setSummary("None selected");
                        mCheckBoxCePreference.setEnabled(false);
                        mCheckBoxCePreference.setChecked(false);
                    } catch (IOException e) {
                        Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                        listPreferenceSeId.setSummary("select UICC failed");
                        mSetGetSE.setSummary("No Secure Element selected!");
                        mCheckBoxCePreference.setEnabled(false);
                        mCheckBoxCePreference.setChecked(false);
                    }
                    mActiveSwp.setEnabled(false);
                    break;
                default:
                    listPreferenceSeId.setSummary("unknown selected SE");
                    mSetGetSE.setSummary("No Secure Element selected!");
                    mCheckBoxCePreference.setEnabled(false);
                    mCheckBoxCePreference.setChecked(false);
                    mActiveSwp.setEnabled(false);
                    break;
            }

        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i(TAG, "### onPreferenceChange ###");

        if (preference.equals(listPreferenceSeId)) {
            setSecureElementUI(newValue);
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.i(TAG, "### onPreferenceClick ###");

        if (preference.equals(mSetGetSE)) {
            Log.i(TAG, "### mSetGetSE ###");
            try {
                String seID = mNfc.getDefaultSelectedSecureElement();
                Toast.makeText(this, seID, Toast.LENGTH_SHORT).show();
                mSetGetSE.setSummary(seID);
            } catch (IOException e) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                mSetGetSE.setSummary(e.getMessage());
            }

        } else if (preference.equals(mActiveSwp)) {
            Log.i(TAG, "### mActiveSwp ###");
            try {
                mNfc.activeSwp();
                Toast.makeText(this, "SWP activated", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }

        } else if (preference.equals(mExit)) {
            Log.i(TAG, "### mExit ###");
            finish();
        }

        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(Constants.KEY_CE_CHECKBOX)) {
            if (mCheckBoxCePreference != null) {
                try {
                    mNfc.setDefaultSecureElementState(mCheckBoxCePreference.isChecked());
                    if (mCheckBoxCePreference.isChecked())
                        Toast.makeText(this, "Secure Element Card Emulation enable",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Secure Element Card Emulation disable",
                                Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
