package com.android.settings.audioprofile;

import android.R.bool;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.audioprofile.AudioProfileListener;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AudioProfileSettings extends SettingsPreferenceFragment implements DialogInterface.OnClickListener{
    private static final String XLogTAG = "Settings/AudioP";
    private static final String TAG = "AudioProfileSettings:";
    
    private static final int MENUID_ADD = Menu.FIRST;
    private static final int MENUID_RESET= Menu.FIRST + 1;
    
    private static final int MENUID_ENABLE= 2;
    private static final int MENUID_RENAME = 3;
    private static final int MENUID_DELETE = 4;
    
    private static final int DIALOG_NAME = 0;
    private static final int DIALOG_ERROR = 1;
    private static final int DIALOG_RESET = 2;
    private static final int DIALOG_DELETE = 3;
    
    private static final int ERROR_NAME_EXIST = 0;
    private static final int ERROR_NAME_LENGTH = 1;
    private static final int ERROR_COUNT_OVERFLOW = 2;
    
    private static final int H_RESET_SUCCESS = 11;
    
    private static final String CUSTOMCATEGORY = "custom";
    private static final String PREDEFINEDCATEGORY = "predefine";
    
    private int mMenuId;
    public int mCurrentDialogId = -1;
    private int mErrorType;
    private Handler mHandler = null;
    
    private AudioProfileManager mProfileManager;
    private PreferenceCategory mCustomParent;
    private boolean mCustomerExist = true;
    private PreferenceCategory mPredefineParent;
    private AudioProfilePreference mPref;
    private IntentFilter mFilter;
    private String[] mProfileTitle;
    private String mDefaultKey;
    private EditText editText = null;
    private String mRenameDialogtext;
    
    //the customer preference list that should update the summary
    private List<AudioProfilePreference> mCustomerProfilePrefList = 
    	new ArrayList<AudioProfilePreference>();
    private AudioProfilePreference mGeneralPref;
    
    private AudioProfileListener mListener = new AudioProfileListener(){
        @Override
        public void onAudioProfileChanged(String profileKey) {
            Xlog.d(XLogTAG, TAG + "onAudioPerfileChanged:key "+ profileKey);
            AudioProfilePreference activePreference = (AudioProfilePreference)findPreference(profileKey != null ? profileKey : mDefaultKey);
            if(activePreference != null){
                activePreference.setChecked();
            }
        }
    };
    
    public void onCreate (Bundle icicle){
        Xlog.d(XLogTAG, "onCreate");
    	super.onCreate(icicle);
        
        mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIOPROFILE_SERVICE);
        
        createPreferenceHierarchy();
        mDefaultKey = AudioProfileManager.PROFILE_PREFIX + Scenario.GENERAL.toString().toLowerCase();
        mGeneralPref = (AudioProfilePreference)findPreference(mDefaultKey);
        
        mFilter = new IntentFilter(AudioProfileManager.ACTION_PROFILE_CHANGED);
        mHandler =  new Handler(){
            public void handleMessage(Message msg){
        	    if(msg.what == H_RESET_SUCCESS) {
                    findPreference(PREDEFINEDCATEGORY).setEnabled(true);
                	mGeneralPref.dynamicShowSummary();
        	    }
            }
        };
    }

    private void createPreferenceHierarchy(){
    	
    	PreferenceScreen root = getPreferenceScreen();
        if(root != null){
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.audioprofile_settings);
        
        root = getPreferenceScreen();
        mCustomParent = (PreferenceCategory)findPreference(CUSTOMCATEGORY);
        
        List<String> profileKeys = mProfileManager.getAllProfileKeys();
        if(profileKeys == null){
        	Xlog.d(XLogTAG, TAG + "profileKey size is 0");
        	return;
        }
        Xlog.d(XLogTAG, TAG + "profileKey size" + profileKeys.size());
        if(profileKeys.size() == AudioProfileManager.PREDEFINED_PROFILES_COUNT){
            root.removePreference(mCustomParent);
            mCustomerExist = false;
        }
        
        for(String profileKey:profileKeys){
            addPreference(root, profileKey);
        }
        setHasOptionsMenu(true);
    }

    private AudioProfilePreference addPreference(PreferenceScreen root, String key) {
    	Scenario scenario = mProfileManager.getScenario(key);
    	AudioProfilePreference preference = null;
        if(Scenario.CUSTOM.equals(scenario)){
        	preference = new AudioProfilePreference(getActivity());
        	preference.setProfileKey(key);
        	mCustomerProfilePrefList.add(preference);
            Xlog.d(XLogTAG, TAG + "Add into profile list" + preference.getKey());
        	
        	if(!mCustomerExist) {        		
        		root.addPreference(mCustomParent);
        		mCustomerExist = true;
        	} 
    		mCustomParent.addPreference(preference);        		

            //String name = Settings.System.getString(getContentResolver(), mProfileManager.getProfileNameKey(key));
            String name = mProfileManager.getProfileName(key);

            if(name != null){
                preference.setTitle(name, false);
                Xlog.d(XLogTAG, TAG + String.valueOf(preference.getTitle()));
            }
        }
        return preference;
    }
    
    private void updateActivePreference(){
        String key = mProfileManager.getActiveProfileKey();
        Xlog.d(XLogTAG, TAG + "key "+key);
        AudioProfilePreference activePreference = (AudioProfilePreference)findPreference(key != null ? key : mDefaultKey);
        if(activePreference != null){
            activePreference.setChecked();
        }
    }
    
    private void dynamicshowSummary() {
    	
    	mGeneralPref.dynamicShowSummary();
    	for(AudioProfilePreference pref : mCustomerProfilePrefList) {
    		pref.dynamicShowSummary();
    	}
    }
    
    public void onResume(){
        Xlog.d(XLogTAG, TAG + "onResume");
    	super.onResume();
        
        dynamicshowSummary();
        
        updateActivePreference();
        registerForContextMenu(getListView());
        mProfileManager.listenAudioProfie(mListener.getCallback(), AudioProfileListener.LISTEN_AUDIOPROFILE_CHANGEG);
    }
    
    public void onPause(){
        super.onPause();
        mProfileManager.listenAudioProfie(mListener.getCallback(), AudioProfileListener.LISTEN_NONE);
    }

    /**
     * Once clicked, start Activity to edit relative profile.
     */
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
    	String key = preference.getKey();
    	Scenario scenario = mProfileManager.getScenario(key);
    	
    	boolean isCmccLoad = Utils.isCmccLoad();
    	Xlog.d(XLogTAG, TAG + "IsCmccLoad" + isCmccLoad);
    	if(isCmccLoad || (!isCmccLoad && 
    	    (Scenario.GENERAL).equals(scenario) || (Scenario.CUSTOM).equals(scenario))) {
    	    
    		Bundle args = new Bundle();
    	    args.putString("profileKey", key);
            ((PreferenceActivity) getActivity()).startPreferencePanel(
    		    Editprofile.class.getName(), args, 0, null, null, 0);
    	}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENUID_ADD, 0, R.string.audio_profile_add)
            .setIcon(android.R.drawable.ic_menu_add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENUID_RESET, 0, R.string.audio_profile_reset)
            .setIcon(android.R.drawable.ic_menu_set_as)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
    
    /**
     * If selected, set all profiles to defaults.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case MENUID_ADD:
            if(mProfileManager.getProfileCount() >= AudioProfileManager.MAX_PROFILES_COUNT){
                mErrorType = ERROR_COUNT_OVERFLOW;
                showDialog(DIALOG_ERROR);
                return true;
            }
            mMenuId = MENUID_ADD;
            showDialog(DIALOG_NAME);
            return true;
        case MENUID_RESET:
            showDialog(DIALOG_RESET);
            return true;
        }
        return false;
    }
    
    /**
     * Create context menu, set title icon and menu message.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Preference pref = (Preference) getPreferenceScreen().getRootAdapter().getItem(info.position);
        if (pref == null || !(pref instanceof AudioProfilePreference)) {
            return;
        }
        String key = pref.getKey();
        mRenameDialogtext = ((AudioProfilePreference)pref).getTitle();
        menu.setHeaderTitle(mRenameDialogtext);
        menu.setHeaderIcon(R.drawable.ic_settings_profiles);
        menu.add(Menu.NONE,MENUID_ENABLE,0,R.string.audio_profile_enable);
        Scenario senario = mProfileManager.getScenario(key);
        if(Scenario.CUSTOM.equals(senario)){
            menu.add(Menu.NONE,MENUID_RENAME,0,R.string.audio_profile_rename);
            menu.add(Menu.NONE,MENUID_DELETE,0,R.string.audio_profile_delete);
        }
    }
    
    /**
     * Long press items to enable this profile and change RINGER_MODE if needed.
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Preference pref = (Preference) getPreferenceScreen().getRootAdapter().getItem(info.position);
        if (pref == null || !(pref instanceof AudioProfilePreference)) {
            return false;
        }
        mPref = (AudioProfilePreference)pref;
        switch(item.getItemId()){
        case MENUID_DELETE:
            showDialog(DIALOG_DELETE);
            return true;
        case MENUID_RENAME:
            mMenuId = MENUID_RENAME;
            showDialog(DIALOG_NAME);
            return true;
        case MENUID_ENABLE:
            mProfileManager.setActiveProfile(mPref.getKey());
        	mPref.setChecked();
            return true;
        }
        return false;
    }
    
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        mCurrentDialogId = id;
        if (id == DIALOG_NAME) {
            View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_edittext, null);
            editText = (EditText) content.findViewById(R.id.edittext);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            if(editText != null){
                editText.setText(mMenuId==MENUID_ADD ? "" : mRenameDialogtext);
            }
            dialog = new AlertDialog.Builder(getActivity())
                .setTitle(mMenuId==MENUID_ADD ? R.string.audio_profile_add : R.string.audio_profile_rename)
                .setMessage(R.string.audio_profile_message_rename)
                .setView(content)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }else if (id == DIALOG_ERROR) {
            int stringId = 0;
            switch(mErrorType){
            case ERROR_COUNT_OVERFLOW:
                stringId = R.string.audio_profile_message_overflow;
                break;
            case ERROR_NAME_EXIST:
                stringId = R.string.audio_profile_message_name_error;
                break;
            case ERROR_NAME_LENGTH:
                stringId = R.string.audio_profile_message_name_length_wrong;
                break;
            }
            dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.audio_profile_error)
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(stringId)
                .setPositiveButton(android.R.string.ok, this)
                .create();
        }else if (id == DIALOG_RESET) {
              dialog = new AlertDialog.Builder(getActivity())
                  .setTitle(R.string.audio_profile_reset)
                  .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                  .setMessage(R.string.audio_profile_message_reset)
                  .setPositiveButton(android.R.string.ok, this)
                  .setNegativeButton(android.R.string.cancel, null)
                  .create();
        }else if (id == DIALOG_DELETE) {
              dialog = new AlertDialog.Builder(getActivity())
                  .setTitle(R.string.audio_profile_delete)
                  .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                  .setMessage(getString(R.string.audio_profile_message_delete,mPref.getTitle()))
                  .setPositiveButton(android.R.string.ok, this)
                  .setNegativeButton(android.R.string.cancel, null)
                  .create();
        }
        return dialog;
    }
    
    public void onClick(DialogInterface dialogInterface, int button) {
        Xlog.d(XLogTAG, "onClick");
		Xlog.d(XLogTAG, "" + button);
    	if (button != DialogInterface.BUTTON_POSITIVE){
    		Xlog.d(XLogTAG, "return");
    		return;
        }
        switch(mCurrentDialogId){
        case DIALOG_NAME:
              String title = editText==null ? "" : String.valueOf(editText.getText());
            if(title.length()==0){
                mErrorType = ERROR_NAME_LENGTH;
                showDialog(DIALOG_ERROR);
            }else if(mProfileManager.isNameExist(title)){
                mErrorType = ERROR_NAME_EXIST;
                showDialog(DIALOG_ERROR);
            }else{
                if(mMenuId == MENUID_ADD){
                    String profileKey = mProfileManager.addProfile();
                    Xlog.d(XLogTAG, TAG + "add profile Key" + profileKey);
                    mProfileManager.setProfileName(profileKey, title);    
                    AudioProfilePreference activePreference = addPreference(getPreferenceScreen(), profileKey);
                    
                    if(activePreference != null){
                        mProfileManager.setActiveProfile(profileKey);
                        activePreference.setChecked();
                        activePreference.dynamicShowSummary();
                    }else{
                        mProfileManager.setActiveProfile(mDefaultKey);
                    	mGeneralPref.setChecked();
                    }
                }else{
                    mPref.setTitle(title,true);
                }
            }
            return;
        case DIALOG_ERROR:
            if(mErrorType != ERROR_COUNT_OVERFLOW){
                showDialog(DIALOG_NAME);
            }
            return;
        case DIALOG_DELETE:
            if(mPref.isChecked()){
                mProfileManager.setActiveProfile(mDefaultKey);
            	mGeneralPref.setChecked();
            };
            mProfileManager.deleteProfile(mPref.getKey());
            mCustomParent.removePreference(mPref);
            mCustomerProfilePrefList.remove(mPref);
            if(mCustomParent.getPreferenceCount()==0){
                 getPreferenceScreen().removePreference(mCustomParent);
                 mCustomerExist = false;
            }
            return;
        case DIALOG_RESET:
            if(mCustomParent != null){
                mCustomParent.removeAll();
            	getPreferenceScreen().removePreference(mCustomParent);
                mCustomerProfilePrefList.clear();
                mCustomerExist = false;
            }
            findPreference(PREDEFINEDCATEGORY).setEnabled(false);
            new ResetTask().execute();            
        }
    }
    
    private class ResetTask extends AsyncTask<String, Void, Integer> {
        private final static int RESET_SUCCESS = 0;
        private final static int RESET_ONGOING = 1;
        
    	@Override
        protected Integer doInBackground(String... arg) {
    		int result = RESET_ONGOING;
            mProfileManager.reset();
            result = RESET_SUCCESS;
            return result;
        }
        
        @Override
        protected void onPostExecute(Integer result) {
        	 int getResult = result.intValue();
        	 if(result == RESET_SUCCESS) {
        		 mHandler.sendEmptyMessage(H_RESET_SUCCESS);
        	 }
        }
    } 
}
