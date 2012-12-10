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

package com.mediatek.cmmb.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.cmmb.app.UpgradeHelper.UpgradeCallback;
import com.mediatek.cmmb.app.Utils.SavedLocation;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.mbbms.MBBMSStore;
import com.mediatek.mbbms.ServerStatus;
import com.mediatek.mbbms.MBBMSStore.DB;
import com.mediatek.mbbms.service.MBBMSService;
import com.mediatek.mbbms.service.SettingsUtils;

import android.app.NotificationManagerPlus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemProperties;

public class SettingActivity extends PreferenceActivity {
    private static final String TAG = "SettingActivity";
    private static final boolean LOG = true;
    
    // private static int mSelectSim = -1;
    private static final String KEY_SG_HOST = "key_sg_server_host";
    private static final String KEY_SG_PORT = "key_sg_server_port";
    private static final String KEY_WAP_HOST = "key_wap_host";
    private static final String KEY_WAP_PORT = "key_wap_port";
    private static final String KEY_APN = "key_apn";
    private static final String KEY_RESTORE = "key_restore";
    private static final String KEY_INTERACTIVITY = "key_interactivity";
    private static final String KEY_UPDATE = "key_update";
    private static final String KEY_LOCATION_CHECK_AUTO = "key_location_check_auto";
    private static final String KEY_LOCATION_CHECK_MANUAL = "key_location_check_manual";
    
    private ModeSwitchManager mModeSwitchManager;
    private NotificationManagerPlus mNMP;
    private Preference mSgHost;
    private Preference mSgPort;
    private Preference mWapHost;
    private Preference mWapPort;
    private Preference mRestore;
    private Preference mApn;
    private Preference mUpdate;
    private CheckBoxPreference mCheckInteract;
    private CheckBoxPreference mLocationAuto;
    private CheckBoxPreference mLocationManual;
    
    private HashMap<String, String> mValues;
    private boolean mIsPaused;
    private boolean mNeedShowUpdate;
    private ServerStatus mUpdateResult;
    
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (MBBMSService.isV3()) {
            addPreferencesFromResource(R.xml.mbbms_setting);
        } else {
            addPreferencesFromResource(R.xml.mbbms_setting_v2);
        }
        mModeSwitchManager = new ModeSwitchManager(this, null, savedInstance);
        mNMP = new NotificationManagerPlus.ManagerBuilder(this).create();
        mSgHost = findPreference(KEY_SG_HOST);
        mSgPort = findPreference(KEY_SG_PORT);
        mWapHost = findPreference(KEY_WAP_HOST);
        mWapPort = findPreference(KEY_WAP_PORT);
        mApn = findPreference(KEY_APN);
        mUpdate = findPreference(KEY_UPDATE);
        mCheckInteract = (CheckBoxPreference)findPreference(KEY_INTERACTIVITY);
        mLocationAuto = (CheckBoxPreference)findPreference(KEY_LOCATION_CHECK_AUTO);
        mLocationManual = (CheckBoxPreference)findPreference(KEY_LOCATION_CHECK_MANUAL);
    }

    private void refresh() {
        mValues = DB.Setting.getSettings(getContentResolver());
        mSgHost.setSummary(mValues.get(DB.Setting.NAME_ENUM_SG_SERVER));
        mSgPort.setSummary(mValues.get(DB.Setting.NAME_ENUM_SG_PORT));
        mWapHost.setSummary(mValues.get(DB.Setting.NAME_ENUM_WAP_GATEWAY));
        mWapPort.setSummary(mValues.get(DB.Setting.NAME_ENUM_WAP_PORT));
        String pop = mValues.get(DB.Setting.NAME_ENUM_AUTO_POP_INTERACTIVITY); 
        if (MBBMSService.isV3()) {
            mCheckInteract.setChecked("1".equals(pop));
            String mode = mValues.get(DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE);
            if (DB.Setting.LOCATION_CHECK_MANUAL.equalsIgnoreCase(mode)) {
                mLocationManual.setChecked(true);
            } else if (DB.Setting.LOCATION_CHECK_AUTO.equalsIgnoreCase(mode)) {
                mLocationAuto.setChecked(true);
            }
            String savedLocation = mValues.get(DB.Setting.NAME_ENUM_LOCATION_SAVED);
            if (savedLocation == null || savedLocation.trim().equals("")) {
                mLocationManual.setTitle(getString(R.string.check_location_manually, getString(R.string.unknown)));
            } else {
                SavedLocation saved = Utils.parseSavedLocation(savedLocation);
                if (SavedLocation.SUGGESTION_HOLDER.equals(saved.province)) {
                    mLocationManual.setTitle(getString(R.string.check_location_manually, saved.city));    
                } else {
                    mLocationManual.setTitle(getString(R.string.check_location_manually, savedLocation));
                }
            }
            if (LOG) Log.v(TAG, "refresh() savedLocation=" + savedLocation + ", pop=" + pop + ", mode=" + mode);
        }
        if (LOG) Log.v(TAG, "refresh()");
    }
    
    private void refreshApn() {
        String apnName = getApnName();
        if (apnName == null) {
            mApn.setSummary(R.string.invalid_apn);
        } else {
            mApn.setSummary(apnName);
        }
        mApn.setEnabled(mApnNameList.size() > 0);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mModeSwitchManager.onActivityStart();
        if (LOG) Log.v(TAG, "onStart()");
    }
    @Override
    protected void onResume() {
        super.onResume();
        mNMP.startListening();
        if (LOG) Log.v(TAG, "onResume() mNeedShowUpdate=" + mNeedShowUpdate + ", mUpdateResult=" + mUpdateResult);
        mIsPaused = false;
        if (mNeedShowUpdate) {
            showUpdateResult(mUpdateResult);
            mNeedShowUpdate = false;
        }
        fillApnName();
        refresh();
        refreshApn();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mNMP.stopListening();
        mIsPaused = true;
        if (LOG) Log.v(TAG, "onPause()");
    }
    @Override
    protected void onStop() {
        super.onStop();
        mModeSwitchManager.onActivityStop();
        if (LOG) Log.v(TAG, "onStop()");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);    
        mModeSwitchManager.onSaveInstanceState(state);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        String key = preference.getKey();
        if (LOG) Log.v(TAG, "onPreferenceTreeClick(" + key + ")");
        if (KEY_APN.equals(key)) {
            selectApn();
        } else if (KEY_RESTORE.equals(key)) {
            showRestoreDialog();
        } else if (KEY_SG_HOST.equals(key)) {
            showEditDialog(DB.Setting.NAME_ENUM_SG_SERVER,
                    InputType.TYPE_TEXT_VARIATION_URI,
                    R.string.sg_server_host);
        } else if (KEY_SG_PORT.equals(key)) {
            showEditDialog(DB.Setting.NAME_ENUM_SG_PORT,
                    InputType.TYPE_CLASS_NUMBER,
                    R.string.sg_server_port);
        } else if (KEY_WAP_HOST.equals(key)) {
            showEditDialog(DB.Setting.NAME_ENUM_WAP_GATEWAY,
                    InputType.TYPE_TEXT_VARIATION_URI,
                    R.string.wap_host);
        } else if (KEY_WAP_PORT.equals(key)) {
            showEditDialog(DB.Setting.NAME_ENUM_WAP_PORT,
                    InputType.TYPE_CLASS_NUMBER,
                    R.string.wap_port);
        } else if (KEY_INTERACTIVITY.equals(key)) {
            updatePopMode(mCheckInteract.isChecked());
        } else if (KEY_UPDATE.equals(key)) {
            showUpdateDialog();
        } else if (KEY_LOCATION_CHECK_AUTO.equals(key)) {
            if (LOG) Log.v(TAG, "mLocationAuto.isChecked()=" + mLocationAuto.isChecked());
            String mode = mValues.get(DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE);
            if (!DB.Setting.LOCATION_CHECK_AUTO.equalsIgnoreCase(mode)) {
                DB.Setting.UpdateSettings(getContentResolver(),
                     DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE,
                     DB.Setting.LOCATION_CHECK_AUTO);
                mValues.put(DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE,
                     DB.Setting.LOCATION_CHECK_AUTO);
            }
            mLocationAuto.setChecked(true);
            mLocationManual.setChecked(false);
        } else if (KEY_LOCATION_CHECK_MANUAL.equals(key)) {
            if (LOG) Log.v(TAG, "mLocationManual.isChecked()=" + mLocationManual.isChecked());
            String mode = mValues.get(DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE);
            if (!DB.Setting.LOCATION_CHECK_MANUAL.equalsIgnoreCase(mode)) {
                DB.Setting.UpdateSettings(getContentResolver(),
                     DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE,
                     DB.Setting.LOCATION_CHECK_MANUAL);
                mValues.put(DB.Setting.NAME_ENUM_LOCATION_CHECK_MODE,
                        DB.Setting.LOCATION_CHECK_MANUAL);
            }
            mLocationAuto.setChecked(false);
            mLocationManual.setChecked(true);
            Intent intent = new Intent(this, LocationSelector.class);
            intent.putExtra(Utils.EXTRA_LOCATION_MODE, Utils.LOCATION_MODE_NORMAL);
            startActivity(intent);
        }
        return true;
    }
    
    private void showUpdateDialog() {
        new UpgradeTask().execute();
    }
    
    private class UpgradeTask extends AsyncTask<Void, Void, ServerStatus> implements OnCancelListener {
        private ProgressDialog mProgressDialog;
        
        private void showProgress() {
            if (LOG) Log.v(TAG, "showProgress() this=" + this);
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setOnCancelListener(this);
            }
            mProgressDialog.setMessage(getString(R.string.update_new_version));
            mProgressDialog.show();
        }
        
        private void hideProgress() {
            if (LOG) Log.v(TAG, "hideProgress()");
            if (mProgressDialog != null) {
                try {
                    mProgressDialog.dismiss();
                } catch (Exception e) {//to be fixed
                    e.printStackTrace();
                }
            }
        }
        
        public void onCancel(DialogInterface dialog) {
            if (LOG) Log.v(TAG, "onCancel() this=" + this);
            cancel(true);
            hideProgress();
        }
        
        @Override
        protected void onPreExecute() {
            showProgress();
        }
        
        @Override
        protected ServerStatus doInBackground(Void... params) {
            if (LOG) Log.v(TAG, "doInBackground() begin");
            ServerStatus result = ServiceManager.getServiceManager(SettingActivity.this).doUpgradeRequest();
            if (LOG) Log.v(TAG, "doInBackground() end result=" + result);
            return result;
        }
        
        @Override
        protected void onPostExecute(ServerStatus result) {
            if (LOG) Log.v(TAG, "onPostExecute(" + result + ") isCancelled()=" + isCancelled()
                    + ", mIsPaused=" + mIsPaused + ", mNeedShowUpdate=" + mNeedShowUpdate);
            if (isCancelled()) return;
            hideProgress();
            if (mIsPaused) {
                mNeedShowUpdate = true;
                mUpdateResult = result;
            } else {
                showUpdateResult(result);
            }
            if (LOG) Log.v(TAG, "onPostExecute(" + result + ") mIsPaused=" + mIsPaused
                    + ", mNeedShowUpdate=" + mNeedShowUpdate);
        }
        
    }

    private void showUpdateResult(ServerStatus result) {
        if (LOG) Log.v(TAG, "showUpdateResult(" + result + ")");
        boolean success = Utils.isSuccess(result);
        String message = null;
        if (success) {//sync success
            if (result.data instanceof Integer) {//get data
                int response = (Integer)result.data;
                if (MBBMSService.UPGRADE_TYPE_OPTIONAL == response) {
                    new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.check_new_version)
                        .setMessage(R.string.has_new_version)
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
    
                            public void onClick(DialogInterface dialog, int which) {
                                UpgradeHelper.getInstance().doUpgrade(SettingActivity.this, null);
                            }
                            
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                } else if (MBBMSService.UPGRADE_TYPE_MANDATORY == response) {
                    new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.check_new_version)
                        .setMessage(R.string.force_upgrade)
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
    
                            public void onClick(DialogInterface dialog, int which) {
                                UpgradeHelper.getInstance().doUpgrade(SettingActivity.this, new UpgradeCallback() {
                                    
                                    public void onUpgradeFinish(int result) {
                                        if (result == UpgradeHelper.RESULT_FAILED) {
                                            SettingActivity.this.finishActivity(Utils.REQUEST_CODE_FOR_FINISHED);
                                            SettingActivity.this.finish();
                                        }
                                    }
                                });
                            }
                            
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                } else if (MBBMSService.UPGRADE_TYPE_NONE == response) {
                    new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.check_new_version)
                        .setMessage(R.string.no_new_version)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
                } else {
                    new AlertDialog.Builder(SettingActivity.this)
                    .setTitle(R.string.check_new_version)
                    .setMessage(R.string.unknown_version)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
                }
            } else {//no data
                new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.check_new_version)
                .setMessage(R.string.unknown_version)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
            }
            message = Utils.getErrorDescription(getResources(), result, null);
        } else {
            message = Utils.getErrorDescription(getResources(), result, getString(R.string.error_check_upgrade));
        }
        if (message != null) {
            Toast.makeText(SettingActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updatePopMode(boolean pop) {
        int count = DB.Setting.UpdateSettings(getContentResolver(),
                DB.Setting.NAME_ENUM_AUTO_POP_INTERACTIVITY,
                String.valueOf(pop ? 1 : 0));
        if (LOG) Log.v(TAG, "updatePopMode(" + pop + ") update " + count);
    }
    
    private void showEditDialog(String key, int inputType, int titleRes) {
        EditDialog dialog = new EditDialog(this, key, mValues.get(key), inputType, titleRes);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.show();
    }
    
    private void showRestoreDialog() {
        new AlertDialog.Builder(this)
        .setTitle(R.string.restore)
        .setMessage(R.string.restore_warning)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                SettingsUtils.restore(getContentResolver());
                finish();
            }
            
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .show();
    }
    
    private void notifyChanged(String key, String value) {
        mValues.put(key, value);
        refresh();
        refreshApn();
    }
    
    public class EditDialog extends AlertDialog implements DialogInterface.OnClickListener {
        private static final int BTN_OK = DialogInterface.BUTTON_POSITIVE;
        private static final int BTN_CANCEL = DialogInterface.BUTTON_NEGATIVE;
        private static final int UNKNOWN_PORT = -1;
        
        private EditText mEditField;
        private String mKey;
        private String mValue;
        private int mInputType;
        private int mTitleRes;
        private boolean mDismiss;
        
        public EditDialog(Context context, String key, String value, int inputType, int titleRes) {
            super(context);
            mKey = key;
            mValue = value;
            mInputType = inputType;
            mTitleRes = titleRes;
            mDismiss = true;
        }
        
        @Override
        public void dismiss() {
            if (mDismiss == true) {
                super.dismiss();
            } else {
                mDismiss = true;
            }
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            View view = getLayoutInflater().inflate(R.layout.edit_text, null);
            setView(view);
            setTitle(mTitleRes);
            mEditField = (EditText) view.findViewById(R.id.edit_field);
            if (mEditField != null) {
                mEditField.setInputType(mInputType);
                mEditField.setText(mValue != null ? mValue : "");
                mEditField.setHint(R.string.invalid_value);
            }
            setButton(BTN_OK, getString(android.R.string.ok), this);
            setButton(BTN_CANCEL, getString(android.R.string.cancel), this);
            super.onCreate(savedInstanceState);
        }
        
        public void onClick(DialogInterface dialog, int which) {
            if (which == BTN_OK) {
                if (validate() == true) {
                    save(mKey, mEditField.getText().toString(), mValue);
                } else {
                    mDismiss = false;
                }
            } else if (which == BTN_CANCEL) {
                // do nothing
            }
        }
        
        private boolean validate() {
            if (mInputType == InputType.TYPE_CLASS_NUMBER) {
                return validatePort();
            } else if (mInputType == InputType.TYPE_TEXT_VARIATION_URI) {
                return validateUri();
            }
            return true;
        }
        
        private boolean validateUri() {
            String text = mEditField.getText().toString().trim();
            String host = null;
            mEditField.setHint(R.string.invalid_value);

            if (text.length() > 0) {
                String urlPattern = "^http(s{0,1})://.+";
                if (text.matches(urlPattern) == false) {
                    text = "http://" + text;
                }
                java.net.URI uri;
                try {
                    uri = new java.net.URI(text);
                    host = uri.getHost();
                } catch (java.net.URISyntaxException e) {
                    Log.v(TAG, "validateUri: new URI exception(" + e.toString() + ").");
                }
            }
            Log.v(TAG, "validateUri: text=" + text + "; host=" + (host == null ? "null" : host));

            if (host == null) {
                // invalid host
                Toast.makeText(SettingActivity.this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        private boolean validatePort() {
            String text = mEditField.getText().toString().trim();
            int port = UNKNOWN_PORT;

            if (text.length() > 0) {
                try {
                    port = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    Log.v(TAG, "validatePort: parseInt exception(" + e.toString() + ")");
                }
            }
            Log.v(TAG, "validatePort: text=" + text + "; port=" + String.valueOf(port));

            if (port <= 0 || port > 0xFFFF) {
                // invalid port
                Toast.makeText(SettingActivity.this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
    }
    
    private void save(String key, String newValue, String old) {
        if (LOG) Log.v(TAG, "save() key=" + key + ", new=" + newValue + ", old=" + old);
        if (key == null || newValue.equalsIgnoreCase(old)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MBBMSStore.DB.Setting.NAME, key);
        values.put(MBBMSStore.DB.Setting.VALUE, newValue);
        int count = getContentResolver().update(DB.Setting.CONTENT_URI,
                values, MBBMSStore.DB.Setting.NAME + "=?", new String[]{key});
        if (count == 0) {
            Log.w(TAG, "save() default setting losed, key=" + key);
            getContentResolver().insert(DB.Setting.CONTENT_URI, values);
        }
        notifyChanged(key, newValue);
        if (LOG) Log.v(TAG, "save() update count=" + count);
    }
    
    ////APN info
    private ArrayList<String> mApnNameList = new ArrayList<String>();
    private void fillApnName() {
        int simId = mModeSwitchManager.getCurSimID();
        TelephonyManagerEx tm = new TelephonyManagerEx(this);
        String simOperator = tm.getSimOperator(simId);
        String where = "numeric=\"" + simOperator + "\"";
        Cursor cursor = null;
        String name = null;
        try {
        	Uri mUri = Telephony.Carriers.CONTENT_URI;
        	if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
        		if(simId == Phone.GEMINI_SIM_2){
        			mUri = Telephony.Carriers.GeminiCarriers.CONTENT_URI;
        		}
        	}

            cursor = getContentResolver().query(
                    mUri,
                new String[] { "_id", "name" },
                where,
                null, Telephony.Carriers.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                mApnNameList.clear();
                while(cursor.moveToNext()) {
                    mApnNameList.add(cursor.getString(1));
                    if (LOG) Log.v(TAG, "Apn " + cursor.getString(0) + ", " + cursor.getString(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) Log.v(TAG, "fillApnName() simId=" + String.valueOf(simId) + ", numeric=" + simOperator + ", size=" + mApnNameList.size());
    }
    
    private String getApnName() {
        int index = getSelectedIndex();
        if (index >= 0 && index < mApnNameList.size()) {
            return mValues.get(DB.Setting.NAME_ENUM_APN);
        }
        return null;
    }

    private int getSelectedIndex() {
        int index = -1;
        String myApn = null;
        try {
            myApn = mValues.get(DB.Setting.NAME_ENUM_APN);
            int size = mApnNameList.size();
            if (myApn != null && size > 0) {
                for(int i = 0; i < size; i++) {
                    if (myApn.equalsIgnoreCase(mApnNameList.get(i))) {
                        index = i;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (LOG) Log.v(TAG, "getSelectedIndex() return " + index + ", apn=" + myApn);
        return index;
    }
    
    private void selectApn() {
        if (mApnNameList.size() > 0) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.apn)
            .setNegativeButton(android.R.string.cancel, null)
            .setSingleChoiceItems(mApnNameList.toArray(new String[]{}), getSelectedIndex(), new OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    save(DB.Setting.NAME_ENUM_APN, mApnNameList.get(which), mValues.get(DB.Setting.NAME_ENUM_APN));
                    dialog.dismiss();
                }
                
            })
            .create()
            .show();
        }
    }
}

class RightCheckBoxPreference extends CheckBoxPreference {
    public RightCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        CheckedTextView check = (CheckedTextView) view.findViewById(android.R.id.checkbox);
        check.setText(getTitle());
    }
    
}
