package com.android.settings.lbs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.EpoProgressCategory;
import com.mediatek.epo.MtkEpoClientManager;
import com.mediatek.epo.MtkEpoFileInfo;
import com.mediatek.epo.MtkEpoStatusListener;

import com.mediatek.xlog.Xlog;

public class EPOSettings extends SettingsPreferenceFragment {
    private static final String TAG = "Settings/EPO";
    private static final String KEY_AUTO_DOWNLOAD = "auto_download";
    private static final String KEY_EPO_INFO_CAT = "epo_info_cat";
    private static final String KEY_DOWNLOAD_TIME = "download_time";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_ABOUT_EPO = "epo_about";
    
    protected static final int MENU_EPO_DOWNLOAD = Menu.FIRST;
    
    private static final int ErrorDialogId = 0;
    private static final int AlertDialogId = 1;
    private static final int AboutDialogId = 2;

    private static String mErrorMessage;
    
    private EpoProgressCategory mEPOInfoCategory;
    private CheckBoxPreference mAutoDownloadPreference;
    private Preference mDownloadTimePreference;
    private Preference mStartTimePreference;
    private Preference mExpireTimePreference;
    
    private MtkEpoClientManager mEpoMgr;
    private MenuItem            mDownloadMenuItem;
    private boolean             mEpoStatus = false; //false: idle, true: running
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.epo_settings);
        log("onCreate");

        mEpoMgr = (MtkEpoClientManager)getSystemService(Context.MTK_EPO_CLIENT_SERVICE);
        if(mEpoMgr == null) {
            log("ERR: cannot get EPO client service");
        }
        
        initPage();
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onResume(){
        super.onResume();
        log("onResume");
        updatePage();
        mEpoMgr.addStatusListener(mEpoStatusListener);
        updateEpoProgressToMmi(mEpoMgr.getProgress());
    }
    
    @Override
    public void onPause(){
        super.onPause();
        log("onPause");
        mEpoMgr.removeStatusListener(mEpoStatusListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    private void handleStopDownload() {
        mEpoStatus = false;
        if(mDownloadMenuItem != null)
            mDownloadMenuItem.setTitle(R.string.epo_download);
    }

    private void handleStatusChanged() {

        if(isNetworkAvailable() == false) {
            mErrorMessage = this.getResources().getString(R.string.epo_error_no_activite_network);
            showDialog(ErrorDialogId);
            return;
        }
        
        mEpoStatus = !mEpoStatus;
        if(mEpoStatus) {
            if(mDownloadMenuItem != null)    
                mDownloadMenuItem.setTitle(R.string.epo_cancel_download);
            int ret = mEpoMgr.startDownload();
            log("startDownload ret=" + ret);
        } else {
            String stoppingMsg = getResources().getString(R.string.epo_status_stopping);
            mEPOInfoCategory.setText(stoppingMsg);
            mEPOInfoCategory.setProgress(true);
            if(mDownloadMenuItem != null)
                mDownloadMenuItem.setTitle(R.string.epo_download);
            mEpoMgr.stopDownload();
        }
    }

    private MtkEpoStatusListener mEpoStatusListener = new MtkEpoStatusListener() {
        public void onStatusChanged(int status) {
            updateEpoProgressToMmi(status);
        }
    };

    private void updateEpoProgressToMmi(int status) {
        if(status >= 0 && status <= 100) {
            log("progress=" + status);
            String startingMsg = getResources().getString(R.string.epo_status_starting);
            mEPOInfoCategory.setText(startingMsg + " (" + status + "%)");
            mEPOInfoCategory.setProgress(true);
        } else if(status == MtkEpoClientManager.EPO_STATUS_STARTING) {
            log("epo started");
        } else if(status == MtkEpoClientManager.EPO_STATUS_UPDATE_SUCCESS) {
            log("epo update success");
            mEPOInfoCategory.setProgress(false);
            handleStopDownload();
            updatePage();
        } else if(status == MtkEpoClientManager.EPO_STATUS_UPDATE_FAILURE) {
            log("epo update failure");
            mEPOInfoCategory.setProgress(false);
            handleStopDownload();

            mErrorMessage = getResources().getString(R.string.epo_err_update_failed);
            showDialog(ErrorDialogId);
        } else if(status == MtkEpoClientManager.EPO_STATUS_CANCELED) {
            log("epo update canceled");
            mEPOInfoCategory.setProgress(false);
            handleStopDownload();
        } else if(status == MtkEpoClientManager.EPO_STATUS_IDLE) {
            log("epo is idle");
            mEPOInfoCategory.setProgress(false);
            handleStopDownload();
        } else if(status == MtkEpoClientManager.EPO_STATUS_CANCELING) {
            log("epo is canceling");
            String stoppingMsg = getResources().getString(R.string.epo_status_stopping);
            mEPOInfoCategory.setText(stoppingMsg);
            mEPOInfoCategory.setProgress(true);
        } else {
            log("WARNING: unknown status recv");
        }
    }

    private void epoEnableAuto(boolean enable) {
        if(enable) {
            mEpoMgr.enableAutoDownload(true);
        } else {
            mEpoMgr.enableAutoDownload(false);
        }
    }
    
    private boolean isGpsAvailable() {
        return Settings.Secure.isLocationProviderEnabled(getContentResolver(), "gps");
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);     
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {         
            return true;
        }
        return false;
    }
    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        log("onCreateOptionsMenu");
        int StringId;
        StringId = R.string.epo_download;
        menu.add(Menu.NONE, MENU_EPO_DOWNLOAD, 0, StringId)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mDownloadMenuItem = menu.findItem(MENU_EPO_DOWNLOAD);
        
        int progress = mEpoMgr.getProgress();

        if(progress != MtkEpoClientManager.EPO_STATUS_IDLE || 
            progress != MtkEpoClientManager.EPO_STATUS_CANCELING) {
            mDownloadMenuItem.setTitle(R.string.epo_cancel_download);
            mEpoStatus = true;
        }
        
        updateEpoProgressToMmi(progress);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == MENU_EPO_DOWNLOAD) {
            handleStatusChanged();
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * init UI page, get preference and button from XML file
     */
    public void initPage() {
        mAutoDownloadPreference = (CheckBoxPreference)findPreference(KEY_AUTO_DOWNLOAD);
        
        mEPOInfoCategory = (EpoProgressCategory)findPreference(KEY_EPO_INFO_CAT);
        mDownloadTimePreference = (Preference)findPreference(KEY_DOWNLOAD_TIME);
        mStartTimePreference = (Preference)findPreference(KEY_START_TIME);
        mExpireTimePreference = (Preference)findPreference(KEY_EXPIRE_TIME);

        mDownloadTimePreference.setEnabled(false);
        mStartTimePreference.setEnabled(false);
        mExpireTimePreference.setEnabled(false);
    }
    
    /**
     * update UI, including refresh download/start/expire time, enable/disable preference and download button etc 
     */
    public void updatePage() {
        MtkEpoFileInfo epoFileInfo = mEpoMgr.getEpoFileInfo();
        if(epoFileInfo != null) {
            mStartTimePreference.setSummary(epoFileInfo.getStartTimeString());
            mDownloadTimePreference.setSummary(epoFileInfo.getDownloadTimeString());
            mExpireTimePreference.setSummary(epoFileInfo.getExpireTimeString());
        }

        mAutoDownloadPreference.setChecked(mEpoMgr.getAutoDownloadStatus());
    }
    
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        String key = preference.getKey();
        
        if(KEY_AUTO_DOWNLOAD.equals(key)) { 
            epoEnableAuto(((CheckBoxPreference)preference).isChecked());
        } else if(KEY_ABOUT_EPO.equals(key)) {
            showDialog(AboutDialogId);
        }
        return false;
    }
        
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if(id == ErrorDialogId) {
            dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.epo_error_dialog_title)
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(mErrorMessage)
                .setPositiveButton(R.string.epo_ok_btn, null)
                .create();
        } else if (id == AlertDialogId){
             dialog = new AlertDialog.Builder(getActivity())
                 .setTitle(R.string.epo_warning_dialog_title)
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(R.string.epo_warning_dialog_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         //confirmToDownload();
                          }
                  }).setNegativeButton(android.R.string.no, null)
                  .create();
        } else if (id == AboutDialogId){
            dialog = new AlertDialog.Builder(getActivity()).
                setTitle(R.string.epo_about_title)
                .setIcon(com.android.internal.R.drawable.ic_dialog_info)
                .setMessage(R.string.epo_about_message)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        } else {
            log("There is no such Dialog id");
        }
        return dialog;
    }

    private void log(String msg) {
        Log.d("[MtkEpoClientManagerService][Settings]", msg);
    }
}
