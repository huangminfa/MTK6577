package com.android.browser.preferences;

import android.app.SearchManager;
import android.app.SearchEngineInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import com.android.browser.search.SearchEngines;
import com.android.browser.BrowserSettings;
import com.android.browser.R;
import com.mediatek.xlog.Xlog;

import android.os.SystemProperties;

public class SearchEngineSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {

	private static final boolean DBG = true;
	private static final String XLOG = "browser/SearchEngineSettings";
    private static final String GOOGLE = "google";
    private static final String PREF_SYNC_SEARCH_ENGINE = "syc_search_engine";
    
    private static final String BAIDU = "baidu";
    
    // intent action used to notify QuickSearchBox that user has has changed search engine setting
    private static final String ACTION_QUICKSEARCHBOX_SEARCH_ENGINE_CHANGED = 
            "com.android.quicksearchbox.SEARCH_ENGINE_CHANGED";

	private List<RadioPreference> mRadioPrefs;
	private CheckBoxPreference mCheckBoxPref;
	private SharedPreferences mPrefs;
	private PreferenceActivity mActivity;
    
    private String[] mEntryValues;
    private String[] mEntries;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRadioPrefs = new ArrayList<RadioPreference>();
        mActivity = (PreferenceActivity) getActivity();
        mPrefs =  PreferenceManager.getDefaultSharedPreferences(mActivity);
        
        int selectedItem = -1;
        String searchEngineName = null;
        // MTK_OP01_PROTECT_START
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && optr.equals("OP01")) {
            if(!mPrefs.contains(BrowserSettings.PREF_SEARCH_ENGINE))
            {
                broadcastSearchEngineChangedExternal(mActivity);
            }
            searchEngineName= mPrefs.getString(BrowserSettings.PREF_SEARCH_ENGINE, BAIDU);
        }else
        // MTK_OP01_PROTECT_END
        {
            searchEngineName= mPrefs.getString(BrowserSettings.PREF_SEARCH_ENGINE, GOOGLE);
        }
        
        List<SearchEngineInfo> searchEngines = getSearchEngineInfos(mActivity);
        int len = 0;
        if (null != searchEngines) {
            len = searchEngines.size();
        }
        mEntryValues = new String[len];
        mEntries = new String[len];
        
        for (int i = 0; i < len; i++) {
            mEntryValues[i] = searchEngines.get(i).getName();
            mEntries[i] = searchEngines.get(i).getLabel();
            if (mEntryValues[i].equals(searchEngineName) || mEntryValues[i].equals("baidu_English") ) {
               selectedItem = i;
            }
        }
        setPreferenceScreen(createPreferenceHierarchy());
        if (selectedItem!= -1) {
            mRadioPrefs.get(selectedItem).setChecked(true);
        }
    }
    
    public static List<SearchEngineInfo> getSearchEngineInfos(Context context) {
        SearchManager searchManager = (SearchManager)context.getSystemService(Context.SEARCH_SERVICE);
        return searchManager.getSearchEngineInfos();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_SYNC_SEARCH_ENGINE, mCheckBoxPref.isChecked());
        editor.commit();
        if (mCheckBoxPref.isChecked()) {
            broadcastSearchEngineChangedExternal(mActivity);
        }
    }
    /*
     * Notify Quick search box that the search engine in browser has changed.
     * Quick search box have to keep consistent with browser.
     */
    private void broadcastSearchEngineChangedExternal(Context context) {
        Intent intent = new Intent(ACTION_QUICKSEARCHBOX_SEARCH_ENGINE_CHANGED);
        intent.setPackage("com.android.quicksearchbox");
        // MTK_OP01_PROTECT_START
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && optr.equals("OP01")) {
            intent.putExtra(BrowserSettings.PREF_SEARCH_ENGINE, mPrefs.getString(BrowserSettings.PREF_SEARCH_ENGINE, BAIDU));
        }else
        // MTK_OP01_PROTECT_END
        {
            intent.putExtra(BrowserSettings.PREF_SEARCH_ENGINE, mPrefs.getString(BrowserSettings.PREF_SEARCH_ENGINE, GOOGLE));
        }
        
        if (DBG) {
            Xlog.i(XLOG, "Broadcasting: " + intent);
        }
        context.sendBroadcast(intent);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mActivity);
        
        PreferenceCategory consistencyPref = new PreferenceCategory(mActivity);
        consistencyPref.setTitle(R.string.pref_content_search_engine_consistency);
        root.addPreference(consistencyPref);
        
        // Toggle preference
        mCheckBoxPref = new CheckBoxPreference(mActivity);
        mCheckBoxPref.setKey("toggle_consistency");
        mCheckBoxPref.setTitle(R.string.pref_search_engine_unify);
        mCheckBoxPref.setSummaryOn(R.string.pref_search_engine_unify_summary);
        mCheckBoxPref.setSummaryOff(R.string.pref_search_engine_unify_summary);
        consistencyPref.addPreference(mCheckBoxPref);
        boolean syncSearchEngine = mPrefs.getBoolean(PREF_SYNC_SEARCH_ENGINE, true);
        mCheckBoxPref.setChecked(syncSearchEngine);
        
        PreferenceCategory searchEnginesPref = new PreferenceCategory(mActivity);
        searchEnginesPref.setTitle(R.string.pref_content_search_engine);
        root.addPreference(searchEnginesPref);
        
        for(int i = 0; i < mEntries.length; i++) {
            RadioPreference radioPref = new RadioPreference(mActivity);
            radioPref.setWidgetLayoutResource(R.layout.radio_preference);
            radioPref.setTitle(mEntries[i]);
            radioPref.setOrder(i);
            radioPref.setOnPreferenceClickListener(this);
            searchEnginesPref.addPreference(radioPref);
            mRadioPrefs.add(radioPref);
        }
        
        return root;
    }

    public boolean onPreferenceClick(Preference preference) {
        for(RadioPreference radioPref: mRadioPrefs) {
            radioPref.setChecked(false);
        }
        ((RadioPreference)preference).setChecked(true);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(BrowserSettings.PREF_SEARCH_ENGINE, mEntryValues[preference.getOrder()]);
        editor.commit();
        // sync the shared preferences back to BrowserSettings
        // BrowserSettings.getInstance().syncSharedPreferences(this, mPrefs);
        return true;
    }
}
