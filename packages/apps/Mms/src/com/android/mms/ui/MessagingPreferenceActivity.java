/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.ui;

import java.util.List;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.mms.data.WorkingMessage;
import com.android.mms.util.Recycler;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.Phone;
import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import android.os.Handler;
import android.view.inputmethod.EditorInfo;
import android.preference.Preference.OnPreferenceChangeListener;
import android.os.SystemProperties;

import android.database.sqlite.SqliteWrapper;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Telephony.Sms;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.telephony.SmsManager;
import com.mediatek.xlog.Xlog;

import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MessagingPreferenceActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener{
    
    private static final String TAG = "MessagingPreferenceActivity";
    private static final boolean DEBUG = false;
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    // M: add this for read report
    public static final String READ_REPORT_AUTO_REPLY   = "pref_key_mms_auto_reply_read_reports";    
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String CREATION_MODE            = "pref_key_mms_creation_mode";
    public static final String MMS_SIZE_LIMIT           = "pref_key_mms_size_limit";
    public static final String SMS_QUICK_TEXT_EDITOR    = "pref_key_quick_text_editor";
    public static final String SMS_SERVICE_CENTER       = "pref_key_sms_service_center";
    public static final String SMS_VALIDITY_PERIOD      = "pref_key_sms_validity_period";
    public static final String SMS_MANAGE_SIM_MESSAGES  = "pref_key_manage_sim_messages";
    public static final String SMS_SAVE_LOCATION        = "pref_key_sms_save_location";
    public static final String MMS_ENABLE_TO_SEND_DELIVERY_REPORT = "pref_key_mms_enable_to_send_delivery_reports";
    public static final String MSG_IMPORT               = "pref_key_import_msg";
    public static final String MSG_EXPORT               = "pref_key_export_msg";
    public static final String SMS_INPUT_MODE           = "pref_key_sms_input_mode";
    public static final String CELL_BROADCAST           = "pref_key_cell_broadcast";
    public static final String SMS_FORWARD_WITH_SENDER  = "pref_key_forward_with_sender";
    
    public static final String WATCH_ANIMATION          = "pref_key_watch_animation";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;
    private final int MAX_EDITABLE_LENGTH = 20;
    private Preference mStorageStatusPref;
    private Preference mSmsLimitPref;
    private Preference mSmsQuickTextEditorPref;
    private Preference mMmsLimitPref;
    private Preference mManageSimPref;
    private Preference mClearHistoryPref;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    private Preference mSmsServiceCenterPref;
    private Preference mSmsValidityPeriodPref;
    private Preference mImportMessages;
    private Preference mExportMessages;
    private Preference mCBsettingPref;

    private Preference mWatchAnimation;

    //MTK_OP01_PROTECT_START
    private Preference mFontSize;
    private AlertDialog mFontSizeDialog;
    private String[] mFontSizeChoices;
    private String[] mFontSizeValues;
    private static final int FONT_SIZE_DIALOG = 10;
    public static final String FONT_SIZE_SETTING = "pref_key_message_font_size";
    public static final String TEXT_SIZE = "message_font_size";
    //MTK_OP01_PROTECT_END

    // all preferences need change key for single sim card
    private CheckBoxPreference mSmsDeliveryReport;
    private CheckBoxPreference mMmsDeliveryReport;
    private CheckBoxPreference mMmsEnableToSendDeliveryReport;
    private CheckBoxPreference mMmsReadReport;
    // M: add this for read report
    private CheckBoxPreference mMmsAutoReplyReadReport;    
    private CheckBoxPreference mMmsAutoRetrieval;
    private CheckBoxPreference mMmsRetrievalDuringRoaming;
    private CheckBoxPreference mEnableNotificationsPref;
    private CheckBoxPreference mSmsForwardWithSender;

    // all preferences need change key for multiple sim card
    private Preference mSmsDeliveryReportMultiSim;
    private Preference mMmsDeliveryReportMultiSim;
    private Preference mMmsEnableToSendDeliveryReportMultiSim;
    private Preference mMmsReadReportMultiSim;
    // M: add this for read report
    private Preference mMmsAutoReplyReadReportMultiSim;
    private Preference mMmsAutoRetrievalMultiSim;
    private Preference mMmsRetrievalDuringRoamingMultiSim;
    private Preference mSmsServiceCenterPrefMultiSim;
    private Preference mSmsValidityPeriodPrefMultiSim;
    private Preference mManageSimPrefMultiSim;
    private Preference mCellBroadcastMultiSim;
    private Preference mSmsSaveLoactionMultiSim;

    private ListPreference mMmsPriority;
    private ListPreference mSmsLocation;
    private ListPreference mMmsCreationMode;
    private ListPreference mMmsSizeLimit;
    private ListPreference mSmsInputMode;
    
    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;
    private static final String PRIORITY_HIGH= "High";
    private static final String PRIORITY_LOW= "Low";
    private static final String PRIORITY_NORMAL= "Normal";
    
    private static final String LOCATION_PHONE = "Phone";
    private static final String LOCATION_SIM = "Sim";
    
    private static final String CREATION_MODE_RESTRICTED = "RESTRICTED";
    private static final String CREATION_MODE_WARNING = "WARNING";
    private static final String CREATION_MODE_FREE = "FREE";
    
    private static final String SIZE_LIMIT_100 = "100";
    private static final String SIZE_LIMIT_200 = "200";
    private static final String SIZE_LIMIT_300 = "300";
     

    private Handler mSMSHandler = new Handler();
    private Handler mMMSHandler = new Handler();
    private EditText mNumberText;
    private AlertDialog mNumberTextDialog;
    private List<SIMInfo> listSimInfo;
    private TelephonyManagerEx mTelephonyManager;
    int slotId;
    private NumberPickerDialog mSmsDisplayLimitDialog;
    private NumberPickerDialog mMmsDisplayLimitDialog;
    private EditText inputNumber;
    /*import or export SD card*/
    private ProgressDialog progressdialog = null;
    private static final String TABLE_SMS = "sms";
    private String mFileNamePrefix = "sms";
    private String mFileNameSuffix = "";
    private String mFileNameExtension = "db";
    private static final Uri SMS_URI = Uri.parse("content://sms");
    private static final Uri CANADDRESS_URI = Uri.parse("content://mms-sms/canonical-addresses");
    public static final String SDCARD_DIR_PATH = "//sdcard//message//";
    public static final String MEM_DIR_PATH = "//data//data//com.android.mms//message//sms001.db";
    private static final String[] SMS_COLUMNS =
    { "thread_id", "address","m_size", "person", "date", "protocol", "read", "status", "type", "reply_path_present",
      "subject", "body", "service_center", "locked", "sim_id", "error_code", "seen"};
    public Handler mMainHandler; 
    private static final String[] ADDRESS_COLUMNS = {"address"};
    private static final int EXPORT_SMS    = 2;    
    private static final int EXPORT_SUCCES = 3;
    private static final int EXPORT_FAILED = 4;
    private static final int IMPORT_SUCCES = 5;
    private static final int IMPORT_FAILED = 6;
    private static final int EXPORT_EMPTY_SMS = 7;
    private static final int DISK_IO_FAILED = 8;
    private static final int MIN_FILE_NUMBER = 1;
    private static final int MAX_FILE_NUMBER = 999;
    public String SUB_TITLE_NAME = "sub_title_name";
    private int currentSimCount = 0;
    @Override
    protected void onPause(){
        super.onPause();
        if (mSmsDisplayLimitDialog != null ) {
            mSmsDisplayLimitDialog.dismiss();
        }
        if (mMmsDisplayLimitDialog != null ) {
            mMmsDisplayLimitDialog.dismiss();
        }
    }
  
    @Override
    protected void onResume() {
        super.onResume();
        setListPrefSummary();
        // Since the enabled notifications pref can be changed outside of this activity,
        // we have to reload it whenever we resume.
        setEnabledNotificationsPref();
    }
        
    private void setListPrefSummary(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //For mMmsPriority;
        String stored = sp.getString(PRIORITY, getString(R.string.priority_normal));
        mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
                R.array.pref_key_mms_priority_values));
        String optr = SystemProperties.get("ro.operator.optr");
        //For mSmsLocation;
        String saveLocation = null;
        //MTK_OP01_PROTECT_START
        if ("OP01".equals(optr)) {
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int currentSimCount = SIMInfo.getInsertedSIMCount(this);
                int slotId = 0;
                if (currentSimCount == 1) {
                    slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
                    saveLocation = sp.getString((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION), "Phone");
                }
                Xlog.d(TAG, "setListPrefSummary op01 mSmsLocation stored slotId = "+ slotId + " stored =" + stored);
            } else {
            	saveLocation = sp.getString(SMS_SAVE_LOCATION, "Phone");
                Xlog.d(TAG, "setListPrefSummary op01 mSmsLocation stored 2 =" + stored);
            }
        }
        //MTK_OP01_PROTECT_END
        //MTK_OP02_PROTECT_START
        if ("OP02".equals(optr)) {
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int currentSimCount = SIMInfo.getInsertedSIMCount(this);
                int slotId = 0;
                if (currentSimCount == 1) {
                    slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
                    saveLocation = sp.getString((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION), "Phone");
                }
                Xlog.d(TAG, "setListPrefSummary op02 mSmsLocation stored slotId = "+ slotId + " stored =" + stored);
            } else {
            	saveLocation = sp.getString(SMS_SAVE_LOCATION, "Phone");
                Xlog.d(TAG, "setListPrefSummary op02 mSmsLocation stored 2 =" + stored);
            }
        }
        //MTK_OP02_PROTECT_END
        
        if (saveLocation == null){
            saveLocation = sp.getString(SMS_SAVE_LOCATION, "Phone");
        }
        if(!getResources().getBoolean(R.bool.isTablet))
        {
            mSmsLocation.setSummary(getVisualTextName(saveLocation, R.array.pref_sms_save_location_choices,
                R.array.pref_sms_save_location_values));
        } else {
            mSmsLocation.setSummary(getVisualTextName(saveLocation, R.array.pref_tablet_sms_save_location_choices,
                R.array.pref_tablet_sms_save_location_values));
        }
        
        //For mMmsCreationMode
        stored = sp.getString(CREATION_MODE, "FREE");
        mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
                R.array.pref_mms_creation_mode_values));
        
        //For mMmsSizeLimit
        stored = sp.getString(MMS_SIZE_LIMIT, "300");
        mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
                R.array.pref_mms_size_limit_values));
    }

    private void newMainHandler(){
        mMainHandler=new Handler() {
            @Override
            public void handleMessage(Message msg) { 
                int output = R.string.export_message_empty;  
                switch(msg.what){ 
                case EXPORT_SUCCES: 
                    output = R.string.export_message_success;
                    break;
                case EXPORT_FAILED: 
                    output = R.string.export_message_fail;
                    break;
                case IMPORT_SUCCES: 
                    output = R.string.import_message_success;
                    break;
                case IMPORT_FAILED: 
                    output = R.string.import_message_fail;
                    break;
                case EXPORT_EMPTY_SMS: 
                    output = R.string.export_message_empty;
                    break;    
                case DISK_IO_FAILED: 
                    output = R.string.export_disk_problem;
                    break;
                default: 
                    break;
                }
                showToast(output);
                
            }
        };
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Xlog.d(TAG, "onCreate");
        newMainHandler();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setMessagePreferences();
    }

    private void setMessagePreferences() {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            Xlog.d(TAG, "MTK_GEMINI_SUPPORT is true");
            currentSimCount = SIMInfo.getInsertedSIMCount(this);
            Xlog.d(TAG, "currentSimCount is :" + currentSimCount);
            if (currentSimCount <= 1) {
                addPreferencesFromResource(R.xml.preferences);
                // MTK_OP01_PROTECT_START
                String optr1 = SystemProperties.get("ro.operator.optr");
                if ("OP01".equals(optr1)) {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                } else
                // MTK_OP01_PROTECT_END
                {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                PreferenceCategory mmsCategory = (PreferenceCategory) findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
                } 
                
            } else {
                addPreferencesFromResource(R.xml.multicardpreferences);
            }
        } else {
            addPreferencesFromResource(R.xml.preferences);
             // MTK_OP01_PROTECT_START
            String optr1 = SystemProperties.get("ro.operator.optr");
            if ("OP01".equals(optr1)) {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            } else
               // MTK_OP01_PROTECT_END
               {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                PreferenceCategory mmsCategory = (PreferenceCategory) findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
               } 
        }
        // M: add for read report
        if (FeatureOption.MTK_SEND_RR_SUPPORT == false) {
            // remove read report entry
            Xlog.d(MmsApp.TXN_TAG, "remove the read report entry, it should be hidden.");
            PreferenceCategory mmOptions = (PreferenceCategory)findPreference("pref_key_mms_settings");
            mmOptions.removePreference(findPreference(READ_REPORT_AUTO_REPLY));          
        }

        // MTK_OP01_PROTECT_START
        String optr1 = SystemProperties.get("ro.operator.optr");
        if ("OP01".equals(optr1)) { 
            mStorageStatusPref = findPreference("pref_key_storage_status");
        } else
        // MTK_OP01_PROTECT_END
        {  
            mStorageStatusPref = findPreference("pref_key_storage_status");
            PreferenceCategory storageCategory = (PreferenceCategory) findPreference("pref_key_storage_settings");
            storageCategory.removePreference(mStorageStatusPref);
        }
        
        mWatchAnimation = findPreference(WATCH_ANIMATION);

        // MTK_OP01_PROTECT_START
        if ("OP01".equals(optr1)) {
            mFontSizeChoices = getFontSizeArray(R.array.pref_message_font_size_choices);
            mFontSizeValues = getFontSizeArray(R.array.pref_message_font_size_values);
            mFontSize = (Preference)findPreference(FONT_SIZE_SETTING);
            mFontSize.setSummary(mFontSizeChoices[getFontSizeCurrentPosition()]);
        }
        else {
        // MTK_OP01_PROTECT_END
            PreferenceCategory fontSizeOptions =
                (PreferenceCategory)findPreference("pref_key_font_size_setting");
            getPreferenceScreen().removePreference(fontSizeOptions);
        // MTK_OP01_PROTECT_START
        }
        // MTK_OP01_PROTECT_END
        
        mCBsettingPref = findPreference(CELL_BROADCAST); 
        mSmsLimitPref = findPreference("pref_key_sms_delete_limit"); 
        mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mSmsQuickTextEditorPref = findPreference("pref_key_quick_text_editor");

        mMmsPriority = (ListPreference) findPreference("pref_key_mms_priority");
        mMmsPriority.setOnPreferenceChangeListener(this);
        mSmsLocation = (ListPreference) findPreference(SMS_SAVE_LOCATION);
        mSmsLocation.setOnPreferenceChangeListener(this);
        mMmsCreationMode = (ListPreference) findPreference("pref_key_mms_creation_mode");
        mMmsCreationMode.setOnPreferenceChangeListener(this);
        mMmsSizeLimit = (ListPreference) findPreference("pref_key_mms_size_limit");
        mMmsSizeLimit.setOnPreferenceChangeListener(this);
        mEnableNotificationsPref = (CheckBoxPreference) findPreference(NOTIFICATION_ENABLED);
        PreferenceCategory smsCategory =
            (PreferenceCategory)findPreference("pref_key_sms_settings");
        if(FeatureOption.MTK_GEMINI_SUPPORT == true){ 
       	    //remove SMS validity period feature for non-OP01 project
        	{
	            String optr = SystemProperties.get("ro.operator.optr");
	            if (!"OP01".equals(optr)) {
	                mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
	                smsCategory.removePreference(mSmsValidityPeriodPref);
	            }
        	}
            if (currentSimCount == 0){
                
                // No SIM card, remove the SIM-related prefs
                //smsCategory.removePreference(mManageSimPref);
                //If there is no SIM, this item will be disabled and can not be accessed.
                mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                mManageSimPref.setEnabled(false);
                //MTK_OP02_PROTECT_START
                String optr = SystemProperties.get("ro.operator.optr");
                if ("OP02".equals(optr)) {
                    smsCategory.removePreference(mManageSimPref);
                }
                //MTK_OP02_PROTECT_END
                mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");
                mSmsServiceCenterPref.setEnabled(false);
                //MTK_OP01_PROTECT_START
                if ("OP01".equals(optr)) {
	                mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
	                mSmsValidityPeriodPref.setEnabled(false);
	            }
                //MTK_OP01_PROTECT_END
            }
        } else {
        	 //remove SMS validity period feature for non-Gemini project
        	 {
                 mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
                 smsCategory.removePreference(mSmsValidityPeriodPref);
        	 }
             if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
                 //smsCategory.removePreference(mManageSimPref);
                 //If there is no SIM, this item will be disabled and can not be accessed.
                 mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                 mManageSimPref.setEnabled(false);
                 //MTK_OP02_PROTECT_START
                 String optr = SystemProperties.get("ro.operator.optr");
                 if ("OP02".equals(optr)) {
                    smsCategory.removePreference(mManageSimPref);
                 }
                 //MTK_OP02_PROTECT_END
                 mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");
                 mSmsServiceCenterPref.setEnabled(false);
             } else {
                 mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                 //MTK_OP02_PROTECT_START
                 String optr = SystemProperties.get("ro.operator.optr");
                 if ("OP02".equals(optr)) {
                     smsCategory.removePreference(mManageSimPref);
                 }
                 //MTK_OP02_PROTECT_END
                 mSmsServiceCenterPref = findPreference("pref_key_sms_service_center");

                 listSimInfo = SIMInfo.getInsertedSIMList(this);
                 mMmsReadReport = (CheckBoxPreference) findPreference(READ_REPORT_MODE);
                 mMmsAutoReplyReadReport = (CheckBoxPreference) findPreference(READ_REPORT_AUTO_REPLY);
                 if (FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(0).mSlot)) {
                     mMmsAutoReplyReadReport.setEnabled(false);
                     mMmsReadReport.setEnabled(false);
             }

             }
        }
        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

            PreferenceCategory storageOptions =
                (PreferenceCategory)findPreference("pref_key_storage_settings");
            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        }
        
        setEnabledNotificationsPref();

        enablePushSetting();
        
        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        setSmsDisplayLimit();
        setMmsDisplayLimit();
        addSmsInputModePreference();
        // Change the key to the SIM-related key, if has one SIM card, else set default value.
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            Xlog.d(TAG, "MTK_GEMINI_SUPPORT is true");
            if (currentSimCount == 1 ) {
                Xlog.d(TAG, "single sim");
                changeSingleCardKeyToSimRelated();
            } else if (currentSimCount > 1) {
                setMultiCardPreference();
            }
        }
        //MTK_OP01_PROTECT_START
        String optr = SystemProperties.get("ro.operator.optr");
        if (null != optr && optr.equals("OP01")) {
            mSmsForwardWithSender = (CheckBoxPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            if (mSmsForwardWithSender != null) {
                mSmsForwardWithSender.setChecked(sp.getBoolean(mSmsForwardWithSender.getKey(), true));
            }
            //mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
            if (MmsConfig.getMmsDirMode() && mManageSimPref != null){
                ((PreferenceCategory)findPreference("pref_key_sms_settings")).removePreference(mManageSimPref);
            }
        } else {
        //MTK_OP01_PROTECT_END	 
            mSmsForwardWithSender = (CheckBoxPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            smsCategory.removePreference(mSmsForwardWithSender);
       //MTK_OP01_PROTECT_START   
        }
        //MTK_OP01_PROTECT_END
    }
    
    private void removeBackupMessage(){
    	PreferenceCategory portPref = (PreferenceCategory)findPreference("pref_title_io_settings");
    	getPreferenceScreen().removePreference(portPref);
    }
    
    //add import/export Message 
    private void addBankupMessages(){
        mImportMessages = findPreference(MSG_IMPORT);
        mExportMessages = findPreference(MSG_EXPORT); 
    }

    //add input mode setting for op03 request, if not remove it.
    private void addSmsInputModePreference(){
        //MTK_OP03_PROTECT_START 
        String optr3 = SystemProperties.get("ro.operator.optr");
        Xlog.i(TAG, "addSmsInputModePreference optr3 = "+optr3);
        if ("OP03".equals(optr3)) {
             mSmsInputMode = (ListPreference) findPreference(SMS_INPUT_MODE);
        } else
        //MTK_OP03_PROTECT_END
        {
             PreferenceCategory smsCategory = (PreferenceCategory)findPreference("pref_key_sms_settings");
             mSmsInputMode = (ListPreference) findPreference(SMS_INPUT_MODE);
             if(mSmsInputMode != null ){
                smsCategory.removePreference(mSmsInputMode);
             }
        }
    }

    private void changeSingleCardKeyToSimRelated() {
        // get to know which one
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        SIMInfo singleCardInfo = null;
        if (listSimInfo.size() != 0) {
            singleCardInfo = listSimInfo.get(0);
        }
        if (singleCardInfo == null) {
            return;
        }
        Long simId = listSimInfo.get(0).mSimId;
        Xlog.d(TAG,"changeSingleCardKeyToSimRelated Got simId = " + simId);
        //translate all key to SIM-related key;
        mSmsDeliveryReport = (CheckBoxPreference) findPreference(SMS_DELIVERY_REPORT_MODE);
        mMmsDeliveryReport = (CheckBoxPreference) findPreference(MMS_DELIVERY_REPORT_MODE);
        mMmsReadReport = (CheckBoxPreference) findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReport = (CheckBoxPreference) findPreference(READ_REPORT_AUTO_REPLY);

        if (FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(0).mSlot)) {
            mMmsAutoReplyReadReport.setEnabled(false);
            mMmsReadReport.setEnabled(false);
        }

        mMmsAutoRetrieval = (CheckBoxPreference) findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming = (CheckBoxPreference) findPreference(RETRIEVAL_DURING_ROAMING);
        mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
        //MTK_OP01_PROTECT_START
        mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
        //MTK_OP01_PROTECT_END
        mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPrefMultiSim = null;
        PreferenceCategory smsCategory =
            (PreferenceCategory)findPreference("pref_key_sms_settings");
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP01_PROTECT_START
        if ("OP01".equals(optr)) {
            int slotid = listSimInfo.get(0).mSlot;
            mSmsLocation = (ListPreference) findPreference(SMS_SAVE_LOCATION);
            mSmsLocation.setKey(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION);
            SharedPreferences spr = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            mSmsLocation.setValue(spr.getString((Long.toString(slotid) + "_" + SMS_SAVE_LOCATION), "Phone"));
        }
        //MTK_OP01_PROTECT_END
        //MTK_OP02_PROTECT_START
        if ("OP02".equals(optr)) {
            if (mManageSimPref != null) {
                smsCategory.removePreference(mManageSimPref);
            }
            int slotid = listSimInfo.get(0).mSlot;
            mSmsLocation = (ListPreference) findPreference(SMS_SAVE_LOCATION);
            mSmsLocation.setKey(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION);
            SharedPreferences spr = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            mSmsLocation.setValue(spr.getString((Long.toString(slotid) + "_" + SMS_SAVE_LOCATION), "Phone"));
        }
        //MTK_OP02_PROTECT_END
        mSmsDeliveryReport.setKey(Long.toString(simId) + "_" + SMS_DELIVERY_REPORT_MODE);
        mMmsDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_DELIVERY_REPORT_MODE);  
        mMmsReadReport.setKey(Long.toString(simId) + "_" + READ_REPORT_MODE);
        // M: add this for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setKey(Long.toString(simId) + "_" +READ_REPORT_AUTO_REPLY); 
        }
        mMmsAutoRetrieval.setKey(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setDependency(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setKey(Long.toString(simId) + "_" + RETRIEVAL_DURING_ROAMING);
        
        //MTK_OP01_PROTECT_START
        if (optr.equals("OP01")) {
            mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else
        //MTK_OP01_PROTECT_END
        {
            mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            if(mMmsEnableToSendDeliveryReport != null){
                mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                PreferenceCategory mmsCategory = (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
            } 
        }
        
        
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (mSmsDeliveryReport != null) {
            mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), false));
        }
        if (mMmsDeliveryReport != null) {
            mMmsDeliveryReport.setChecked(sp.getBoolean(mMmsDeliveryReport.getKey(), false));
        }
        if (mMmsEnableToSendDeliveryReport != null) {
            mMmsEnableToSendDeliveryReport.setChecked(sp.getBoolean(mMmsEnableToSendDeliveryReport.getKey(), false));
        }
        if (mMmsReadReport != null) {
            mMmsReadReport.setChecked(sp.getBoolean(mMmsReadReport.getKey(), false));
        }
        // M: add for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setChecked(sp.getBoolean(mMmsAutoReplyReadReport.getKey(), false));
        }        
        if (mMmsAutoRetrieval != null) {
            mMmsAutoRetrieval.setChecked(sp.getBoolean(mMmsAutoRetrieval.getKey(), true));
        }
        if (mMmsRetrievalDuringRoaming != null) {
            mMmsRetrievalDuringRoaming.setChecked(sp.getBoolean(mMmsRetrievalDuringRoaming.getKey(), false));
        }
    }
    
    private void setMultiCardPreference() {    
        mSmsDeliveryReportMultiSim = findPreference(SMS_DELIVERY_REPORT_MODE);
        mMmsDeliveryReportMultiSim = findPreference(MMS_DELIVERY_REPORT_MODE);
        //MTK_OP01_PROTECT_START
        String optr = SystemProperties.get("ro.operator.optr");
        if ("OP01".equals(optr)) {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else
        //MTK_OP01_PROTECT_END
        {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            PreferenceCategory mmsCategory =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            mmsCategory.removePreference(mMmsEnableToSendDeliveryReportMultiSim);
        }
        
        
        mMmsReadReportMultiSim = findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReportMultiSim = findPreference(READ_REPORT_AUTO_REPLY);
        mMmsAutoRetrievalMultiSim = findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoamingMultiSim = findPreference(RETRIEVAL_DURING_ROAMING);
        mSmsServiceCenterPrefMultiSim = findPreference(SMS_SERVICE_CENTER);
        mSmsValidityPeriodPrefMultiSim = findPreference(SMS_VALIDITY_PERIOD);        
        mManageSimPrefMultiSim = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPref = null;
        PreferenceCategory smsCategory =
            (PreferenceCategory)findPreference("pref_key_sms_settings");
        //MTK_OP01_PROTECT_START
        if ("OP01".equals(optr)) {
            if (mSmsLocation != null) {
                smsCategory.removePreference(mSmsLocation);
                Preference saveLocationMultiSim = new Preference(this);
                saveLocationMultiSim.setKey(SMS_SAVE_LOCATION);
                saveLocationMultiSim.setTitle(R.string.sms_save_location);
                saveLocationMultiSim.setSummary(R.string.sms_save_location);
                smsCategory.addPreference(saveLocationMultiSim);
                mSmsSaveLoactionMultiSim = findPreference(SMS_SAVE_LOCATION);
           }
        }
        //MTK_OP01_PROTECT_END
        //MTK_OP02_PROTECT_START
        if ("OP02".equals(optr)) {
            if (mManageSimPrefMultiSim != null) {
                smsCategory.removePreference(mManageSimPrefMultiSim);
            }
            if (mSmsLocation != null) {
                smsCategory.removePreference(mSmsLocation);
                Preference saveLocationMultiSim = new Preference(this);
                saveLocationMultiSim.setKey(SMS_SAVE_LOCATION);
                saveLocationMultiSim.setTitle(R.string.sms_save_location);
                saveLocationMultiSim.setSummary(R.string.sms_save_location);
                smsCategory.addPreference(saveLocationMultiSim);
                mSmsSaveLoactionMultiSim = findPreference(SMS_SAVE_LOCATION);
           }
        }
        //MTK_OP02_PROTECT_END
        mCellBroadcastMultiSim = findPreference(CELL_BROADCAST);
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(getNotificationEnabled(this));
    }

    private void setSmsDisplayLimit() {
        mSmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mSmsRecycler.getMessageLimit(this)));
    }

    private void setMmsDisplayLimit() {
        mMmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mMmsRecycler.getMessageLimit(this)));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;

            case android.R.id.home:
                // The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mStorageStatusPref) {
            final String memoryStatus = MessageUtils.getStorageStatus(getApplicationContext());
            new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.pref_title_storage_status)
                    .setIcon(R.drawable.ic_dialog_info_holo_light)
                    .setMessage(memoryStatus)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(true)
                    .show();
        } else if (preference == mSmsLimitPref) {
            mSmsDisplayLimitDialog = 
            new NumberPickerDialog(this,
                    mSmsLimitListener,
                    mSmsRecycler.getMessageLimit(this),
                    mSmsRecycler.getMessageMinLimit(),
                    mSmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_sms_delete);
            mSmsDisplayLimitDialog.show();
        } else if (preference == mMmsLimitPref) {
            mMmsDisplayLimitDialog = 
            new NumberPickerDialog(this,
                    mMmsLimitListener,
                    mMmsRecycler.getMessageLimit(this),
                    mMmsRecycler.getMessageMinLimit(),
                    mMmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_mms_delete);
            mMmsDisplayLimitDialog.show();
        } else if (preference == mManageSimPref) {
            if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                listSimInfo = SIMInfo.getInsertedSIMList(this);
                int slotId = listSimInfo.get(0).mSlot;
                Xlog.d(TAG, "slotId is : " + slotId);
                if (slotId != -1) {
                    Intent it = new Intent();
                    it.setClass(this, ManageSimMessages.class);
                    it.putExtra("SlotId", slotId);
                    startActivity(it);
                }
            } else {
                startActivity(new Intent(this, ManageSimMessages.class));
            }
        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;
        } else if (preference == mSmsQuickTextEditorPref) {
            Intent intent = new Intent();
            intent.setClass(this, SmsTemplateEditActivity.class);
            startActivity(intent);
        } else if (preference == mSmsDeliveryReportMultiSim 
                || preference == mMmsDeliveryReportMultiSim
                || preference == mMmsEnableToSendDeliveryReportMultiSim
                || preference == mMmsReadReportMultiSim 
                // M: add this for read report
                || preference == mMmsAutoReplyReadReportMultiSim
                || preference == mMmsAutoRetrievalMultiSim 
                || preference == mMmsRetrievalDuringRoamingMultiSim) {
            
            Intent it = new Intent();
            it.setClass(this, MultiSimPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            it.putExtra("preferenceTitle", preference.getTitle());
            startActivity(it);
        } else if (preference == mSmsServiceCenterPref) {

            listSimInfo = SIMInfo.getInsertedSIMList(this);
            if(listSimInfo != null && listSimInfo.isEmpty()){
                Xlog.d(TAG, "there is no sim card");
                return true;
            }
            int id = listSimInfo.get(0).mSlot;
            if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(id)) {
                showToast(R.string.cdma_not_support);
            } else {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            mNumberText = new EditText(dialog.getContext());
            mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
            mNumberText.computeScroll();
            mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
            //mNumberText.setKeyListener(new DigitsKeyListener(false, true));
            mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
            mTelephonyManager = TelephonyManagerEx.getDefault();
            String gotScNumber;
            if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                int slotId = listSimInfo.get(0).mSlot;
                gotScNumber = mTelephonyManager.getScAddress(slotId);
            } else {
                gotScNumber = mTelephonyManager.getScAddress(0);
            }
            Xlog.d(TAG, "gotScNumber is: " + gotScNumber);
            mNumberText.setText(gotScNumber);
            mNumberTextDialog = dialog
            .setIcon(R.drawable.ic_dialog_info_holo_light)
            .setTitle(R.string.sms_service_center)
            .setView(mNumberText)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();

            }

        }else if(preference == mSmsValidityPeriodPref){
	        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
	        	int slotId = listSimInfo.get(0).mSlot;
				final int [] validity_peroids = {
					SmsManager.VALIDITY_PERIOD_NO_DURATION,
					SmsManager.VALIDITY_PERIOD_ONE_HOUR,
					SmsManager.VALIDITY_PERIOD_SIX_HOURS,
					SmsManager.VALIDITY_PERIOD_TWELVE_HOURS,
					SmsManager.VALIDITY_PERIOD_ONE_DAY,
					SmsManager.VALIDITY_PERIOD_MAX_DURATION,
				};
				final CharSequence[] validity_items = {
					getResources().getText(R.string.sms_validity_period_nosetting), 
					getResources().getText(R.string.sms_validity_period_1hour), 
					getResources().getText(R.string.sms_validity_period_6hours), 
					getResources().getText(R.string.sms_validity_period_12hours), 
					getResources().getText(R.string.sms_validity_period_1day), 
					getResources().getText(R.string.sms_validity_period_max)};
                /* check validity index*/
	        	final String validityKey = Long.toString(slotId) + "_" + MessagingPreferenceActivity.SMS_VALIDITY_PERIOD;
	            int vailidity = PreferenceManager.getDefaultSharedPreferences(this).getInt(validityKey, SmsManager.VALIDITY_PERIOD_NO_DURATION);
	            int currentPosition = 0;
	            Xlog.d(TAG, "validity found the res = " + vailidity);
	            for(int i = 0; i < validity_peroids.length; i++){
	            	if(vailidity == (validity_peroids[i])){
	            		Xlog.d(TAG, "validity found the position = "+i);
	            		currentPosition = i;
	            	}
	            }
	            
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(getResources().getText(R.string.sms_validity_period));	        	
	        	builder.setSingleChoiceItems(validity_items, currentPosition, new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int item) {
	                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MessagingPreferenceActivity.this).edit();
	                    editor.putInt(validityKey, validity_peroids[item]);
	                    editor.commit();
	                    dialog.dismiss();
	        	    }
	        	});
	        	builder.show();
        	} 
        } else if (preference == mSmsServiceCenterPrefMultiSim
        		|| preference == mSmsValidityPeriodPrefMultiSim
                || preference == mManageSimPrefMultiSim
                || preference == mCellBroadcastMultiSim
                ||(preference == mSmsSaveLoactionMultiSim && currentSimCount > 1)) {
            Intent it = new Intent();
            it.setClass(this, SelectCardPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            it.putExtra("preferenceTitle", preference.getTitle());
            startActivity(it);
        } else if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
            enableNotifications(mEnableNotificationsPref.isChecked(), this);
        } else if(preference == mImportMessages){
            //importMessages
            Intent it = new Intent();
            it.setClass(this, ImportSmsActivity.class);
            startActivity(it);
        } else if(preference == mExportMessages){
            showDialog(EXPORT_SMS);
        } else if(preference == mCBsettingPref){
             listSimInfo = SIMInfo.getInsertedSIMList(this);
             if(listSimInfo != null && listSimInfo.isEmpty()){
                 Xlog.d(TAG, "there is no sim card");
                 return true;
             }
             int slotId = listSimInfo.get(0).mSlot;
             Xlog.d(TAG, "mCBsettingPref slotId is : " + slotId);

            if(FeatureOption.EVDO_DT_SUPPORT == true && isUSimType(slotId)) {
                showToast(R.string.cdma_not_support);
            } else {
             	Intent it = new Intent();
             	it.setClassName("com.android.phone", "com.android.phone.CellBroadcastActivity");
             	it.setAction(Intent.ACTION_MAIN);
             	it.putExtra(Phone.GEMINI_SIM_ID_KEY, slotId);
             	it.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, slotId).mDisplayName);
             	startActivity(it);
             }
        } else if(preference == mWatchAnimation) {
            Intent it = new Intent();
            it.setClass(this, LongPressGuideLineActivity.class);
            startActivity(it);
        //MTK_OP01_PROTECT_START
        } else if(preference == mFontSize) {
            showDialog(FONT_SIZE_DIALOG);
        //MTK_OP01_PROTECT_END
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
	/// M: added for bug ALPS00314789 begin
	private boolean isValidAddr(String address) {
		boolean ret = true;
		if (address.isEmpty()) {
			return ret;
		}
		if (address.charAt(0) == '+') {
			for (int i = 1, count = address.length(); i < count; i++) {
				if (address.charAt(i) < '0' || address.charAt(i) > '9') {
					ret = false;
					break;
				}
			}
		} else {
			for (int i = 0, count = address.length(); i < count; i++) {
				if (address.charAt(i) < '0' || address.charAt(i) > '9') {
					ret = false;
					break;
				}
			}
		}
		return ret;
	}

	/// M: added for bug ALPS00314789 end

	private class PositiveButtonListener implements OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			// write to the SIM Card.
			/// M: added for bug ALPS00314789 begin
			if (!isValidAddr(mNumberText.getText().toString())) {
				String num = mNumberText.getText().toString();
				String strUnSpFormat = getResources().getString(
						R.string.unsupported_media_format, "");
				Toast.makeText(getApplicationContext(), strUnSpFormat,
						Toast.LENGTH_SHORT).show();
				return;
			}
			/// M: added for bug ALPS00314789 end
            mTelephonyManager = TelephonyManagerEx.getDefault();
            if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                slotId = listSimInfo.get(0).mSlot;
            } else {
                slotId = 0;
            }
            new Thread(new Runnable() {
                public void run() {
                    mTelephonyManager.setScAddress(mNumberText.getText().toString(), slotId);
                }
            }).start();
        }
    }

    private class NegativeButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // cancel
            dialog.dismiss();
        }
    }
    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().apply();
        setPreferenceScreen(null);
        setMessagePreferences();
        setListPrefSummary();
        
        //watch guide line only once.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(LongPressGuideLineActivity.GUIDE_LINE_DISMISS, true);
        editor.commit();
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                if (limit <= mSmsRecycler.getMessageMinLimit()){
                    limit = mSmsRecycler.getMessageMinLimit();
                }else if( limit >= mSmsRecycler.getMessageMaxLimit()) {
                    limit = mSmsRecycler.getMessageMaxLimit();
                }
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setSmsDisplayLimit();
                if (progressdialog == null || !progressdialog.isShowing()){
                    progressdialog = ProgressDialog.show(MessagingPreferenceActivity.this, "", getString(R.string.deleting), true);
                }
                mSMSHandler.post(new Runnable() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                               Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
                               if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                                   Recycler.getWapPushRecycler().deleteOldMessages(getApplicationContext());
                               }
                               if (null != progressdialog) {
                                   progressdialog.dismiss();
                               }
                             }
                        }, "DeleteSMSOldMsgAfterSetNum").start();
                    }
                });
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                if (limit <= mMmsRecycler.getMessageMinLimit()){
                    limit = mMmsRecycler.getMessageMinLimit();
                }else if( limit >= mMmsRecycler.getMessageMaxLimit()) {
                    limit = mMmsRecycler.getMessageMaxLimit();
                } 
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setMmsDisplayLimit();
                if (progressdialog == null || !progressdialog.isShowing()){
                    progressdialog = ProgressDialog.show(MessagingPreferenceActivity.this, "", getString(R.string.deleting), true);
                }
                mMMSHandler.post(new Runnable() {
                    public void run() {
                        new Thread(new Runnable() {
                            public void run() {
                                Xlog.d("Recycler", "mMmsLimitListener");
                                Recycler.getMmsRecycler().deleteOldMessages(getApplicationContext());                            
                                if (null != progressdialog) {
                                    progressdialog.dismiss();
                                }
                            } 
                        }, "DeleteMMSOldMsgAfterSetNum").start();
                    }
                });
            }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((MmsApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .create();
            case EXPORT_SMS:  
                return new AlertDialog.Builder(this)
                .setMessage(getString(R.string.whether_export_item))
                .setTitle(R.string.pref_summary_export_msg).setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                exportMessages();
                                return;
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }
                        }).create();
            //MTK_OP01_PROTECT_START
            case FONT_SIZE_DIALOG:
                FontSizeDialogAdapter adapter = new FontSizeDialogAdapter(MessagingPreferenceActivity.this, 
                        mFontSizeChoices, mFontSizeValues);
                mFontSizeDialog = new AlertDialog.Builder(MessagingPreferenceActivity.this)
                .setTitle(R.string.message_font_size_dialog_title)
                .setNegativeButton(R.string.message_font_size_dialog_cancel, new DialogInterface.OnClickListener() {
                                
                    public void onClick(DialogInterface dialog, int which) {
                        mFontSizeDialog.dismiss();
                    }
                })
                .setSingleChoiceItems(adapter, getFontSizeCurrentPosition(), new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(MessagingPreferenceActivity.this).edit();
                        editor.putInt(FONT_SIZE_SETTING, which);
                        editor.putFloat(TEXT_SIZE, Float.parseFloat(mFontSizeValues[which]));
                        editor.apply();
                        mFontSizeDialog.dismiss();
                        mFontSize.setSummary(mFontSizeChoices[which]);
                    }
                }).create();
                mFontSizeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					public void onDismiss(DialogInterface dialog) {
						MessagingPreferenceActivity.this.removeDialog(FONT_SIZE_DIALOG);
					}
				});
                return mFontSizeDialog;
            //MTK_OP01_PROTECT_END
        }
        return super.onCreateDialog(id);
    }

    public static boolean getNotificationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        return notificationsEnabled;
    }

    public static void enableNotifications(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, enabled);

        editor.apply();
    }
    /*
     * Notes: if wap push is not support, wap push setting should be removed
     * 
     */
    private void enablePushSetting(){
        
        PreferenceCategory wapPushOptions =
            (PreferenceCategory)findPreference("pref_key_wappush_settings");
        
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){  
            if(!MmsConfig.getSlAutoLanuchEnabled()){
                wapPushOptions.removePreference(findPreference("pref_key_wappush_sl_autoloading"));
            }
        }else{
            if(getPreferenceScreen() != null){
                getPreferenceScreen().removePreference(wapPushOptions);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        final String key = arg0.getKey();
        int slotId = 0;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true &&
                "OP02".equals(SystemProperties.get("ro.operator.optr"))) {
            int currentSimCount = SIMInfo.getInsertedSIMCount(this);
            if (currentSimCount == 1){
                slotId = SIMInfo.getInsertedSIMList(this).get(0).mSlot;
            }
        }
        String stored = (String)arg1;
        if (PRIORITY.equals(key)) {
            mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
                    R.array.pref_key_mms_priority_values));
        } else if (CREATION_MODE.equals(key)) {
            mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
                    R.array.pref_mms_creation_mode_values));
            mMmsCreationMode.setValue(stored);
            WorkingMessage.updateCreationMode(this);
        } else if (MMS_SIZE_LIMIT.equals(key)) {
            mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
                    R.array.pref_mms_size_limit_values));
            MmsConfig.setUserSetMmsSizeLimit(Integer.valueOf(stored));
            
        } else if (SMS_SAVE_LOCATION.equals(key) && !(currentSimCount > 1 
        		&& ("OP02".equals(SystemProperties.get("ro.operator.optr"))
        		|| "OP01".equals(SystemProperties.get("ro.operator.optr"))))) {
            
            if(!getResources().getBoolean(R.bool.isTablet))
            {      	
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
            }
            else
            {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_tablet_sms_save_location_choices,
                    R.array.pref_tablet_sms_save_location_values));
            }
            
        } else if((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION).equals(key)){
        	  
            if(!getResources().getBoolean(R.bool.isTablet))
            {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
            }
            else
            {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_tablet_sms_save_location_choices,
                    R.array.pref_tablet_sms_save_location_values));
            }
        }
        return true;
    }
    private CharSequence getVisualTextName(String enumName, int choiceNameResId, int choiceValueResId) {
        CharSequence[] visualNames = getResources().getTextArray(
                choiceNameResId);
        CharSequence[] enumNames = getResources().getTextArray(
                choiceValueResId);

        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }

        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }

    private boolean exportMessages(){
        Xlog.d(TAG,"exportMessages");
        if(!isSDcardReady()){
            return false;
        }
        progressdialog = ProgressDialog.show(this, "", getString(R.string.export_message_ongoing), true); 
        new Thread() {
            public void run() {
                Cursor cursor = null;
                int quiteCode = 0;
                String storeFileName = "";
                try { 
                    File dir = new File(SDCARD_DIR_PATH);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    storeFileName = getAppropriateFileName(SDCARD_DIR_PATH);
                    if (null == storeFileName) {
                        Xlog.w(TAG, "exportMessages sms file name is null");
                        return;
                    }
                    cursor = getContentResolver().query(SMS_URI, SMS_COLUMNS, null, null, null);
                    if (cursor == null || cursor.getCount() == 0) {
                        Xlog.w(TAG, "exportMessages query sms cursor is null");
                        quiteCode = EXPORT_EMPTY_SMS;
                        return;
                    }
                    Xlog.d(TAG, "exportMessages query sms cursor count is "+cursor.getCount());
                    int exportCount = copyToPhoneMemory(cursor, MEM_DIR_PATH);
                    if (exportCount >0){
                    copyToSDMemory(MEM_DIR_PATH, storeFileName);
                    mMainHandler.sendEmptyMessage(EXPORT_SUCCES);
                    Xlog.d(TAG, "ExportDict success");
                    } else {
                    	  Xlog.d(TAG, "ExportDict failure there is no message to export");
                    	  quiteCode = EXPORT_EMPTY_SMS;
                    }
                } catch (SQLiteDiskIOException e) {
                    mMainHandler.sendEmptyMessage(DISK_IO_FAILED);
                    //if the file is created, erase it
                    File file = new File(storeFileName);
                    if (file.exists()) {
                        file.delete();
                    } 
                    e.printStackTrace();
                } catch (Exception e) {
                    Xlog.e(TAG, "exportMessages can't create the database file");
                    //if the file is created, erase it
                    File file = new File(storeFileName);
                    if (file.exists()) {
                        file.delete();
                    } 
                    mMainHandler.sendEmptyMessage(EXPORT_FAILED);
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    File file = new File(MEM_DIR_PATH);
                    if (file.exists()) {
                      file.delete();
                    } 
                    if (null != progressdialog) {
                        progressdialog.dismiss();
                    } 
                    if(quiteCode == EXPORT_EMPTY_SMS){
                        mMainHandler.sendEmptyMessage(EXPORT_EMPTY_SMS);
                    }
                } 
            }
        }.start();
        return true;
    } 
    
    /**
     * Tries to get an appropriate filename. Returns null if it fails.
     */
    private String getAppropriateFileName(final String destDirectory) {
        //get max number of  digital
        int fileNumberStringLength = 0;
        {
            int tmp;
            for (fileNumberStringLength = 0, tmp = MAX_FILE_NUMBER; tmp > 0;
                fileNumberStringLength++, tmp /= 10) {
            }
        }
        String bodyFormat = "%s%0" + fileNumberStringLength + "d%s";
        for (int i = MIN_FILE_NUMBER; i <= MAX_FILE_NUMBER; i++) {
            boolean isExitFile = false;
            String body = String.format(bodyFormat, mFileNamePrefix, i, mFileNameSuffix);
            String fileName = String.format("%s%s.%s", destDirectory, body, mFileNameExtension);
            File file = new File(fileName);
            if (file.exists()) {
                isExitFile = true;
            } 
            if (!isExitFile){
                Xlog.w(TAG, "exportMessages getAppropriateFileName fileName =" + fileName);
                return fileName;
            }
        }
        return null;
    }
    
    private int copyToPhoneMemory(Cursor cursor, String dest){
        SQLiteDatabase db =  openOrCreateDatabase(dest, 1, null);
        db.execSQL("CREATE TABLE sms ("
                + "_id INTEGER PRIMARY KEY,"
                + "thread_id INTEGER,"
                + "address TEXT,"
                + "m_size INTEGER,"
                + "person INTEGER,"
                + "date INTEGER,"
                + "date_sent INTEGER DEFAULT 0,"
                + "protocol INTEGER,"
                + "read INTEGER DEFAULT 0,"
                + "status INTEGER DEFAULT -1,"
                + "type INTEGER," + "reply_path_present INTEGER,"
                + "subject TEXT," + "body TEXT," + "service_center TEXT,"
                + "locked INTEGER DEFAULT 0," + "sim_id INTEGER DEFAULT -1,"
                + "error_code INTEGER DEFAULT 0," + "seen INTEGER DEFAULT 0"
                + ");");
        db.beginTransaction();
        Xlog.d(TAG, "export mem begin");
        int count = 0;
        while (cursor.moveToNext()) {
            int messageType = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
            if (messageType == 3) {
                continue;
            }
            ContentValues smsValue = new ContentValues();
            int threadId = cursor.getInt(cursor.getColumnIndexOrThrow("thread_id"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            int simId = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
            int read = cursor.getInt(cursor.getColumnIndexOrThrow("read")); 
            int seen = cursor.getInt(cursor.getColumnIndexOrThrow("seen")); 
            String serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow("service_center"));
            smsValue.put(Sms.READ, read);
            smsValue.put(Sms.SEEN, seen);
            smsValue.put(Sms.BODY, body);
            smsValue.put(Sms.DATE, date);
            smsValue.put(Sms.SIM_ID, simId);
            smsValue.put(Sms.SERVICE_CENTER, serviceCenter);
            smsValue.put(Sms.TYPE, messageType);
            smsValue.put(Sms.ADDRESS, address);
            db.insert(TABLE_SMS, null, smsValue);
            count++;
        } 
        Xlog.d(TAG, "export mem end count = " + count);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();   
        return count;
   }

    private void copyToSDMemory(String src, String dst) throws Exception{
        Xlog.d(TAG, "export sdcard begin dst = "  +dst);
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            myInput = new FileInputStream(src);
            File dir = new File(SDCARD_DIR_PATH);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File dstFile = new File(dst);
            if (!dstFile.exists()){
                dstFile.createNewFile();
            }
            myOutput = new FileOutputStream(dstFile);
             //transfer bytes from the inputfile to the outputfile
             byte[] buffer = new byte[1024];
             int length;
             while ((length = myInput.read(buffer))>0){
                 myOutput.write(buffer, 0, length);
             }
             myOutput.flush();
             myOutput.close();
             myInput.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Xlog.e(TAG, "export sdcard FileNotFoundException");
        } catch (IOException e){
            Xlog.e(TAG, "export sdcard IOException");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Xlog.d(TAG, "export sdcard end");
    }
    
    private boolean isSDcardReady(){
        boolean isSDcard = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); 
        if (!isSDcard) {
            showToast(R.string.no_sd_card);
            Xlog.d(TAG, "there is no SD card");
            return false;
        }
        return true;
    }
    
    private void showToast(int id) { 
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    } 
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged: newConfig = " + newConfig + ",this = " + this);
        super.onConfigurationChanged(newConfig);
        this.getListView().clearScrapViewsIfNeeded();
    }

    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }
        
        try {
            if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                return iTel.getIccCardTypeGemini(slot).equals("UIM");
            } else {
                return iTel.getIccCardType().equals("UIM");
            }
        } catch (Exception e) {
            Log.e(TAG, "[isUSIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }
    
    //MTK_OP01_PROTECT_START
    private String[] getFontSizeArray(int resId) {
        return getResources().getStringArray(resId);
    }
    
    private int getFontSizeCurrentPosition() {
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        return sp.getInt(FONT_SIZE_SETTING, 0);
    }
    //MTK_OP01_PROTECT_END
}
