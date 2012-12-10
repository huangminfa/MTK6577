/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.quicksearchbox.preferences;

import java.util.ArrayList;
import java.util.List;

import com.android.common.SharedPreferencesCompat;
import com.android.quicksearchbox.Corpora;
import com.android.quicksearchbox.Corpus;
import com.android.quicksearchbox.QsbApplication;
import com.android.quicksearchbox.R;
import com.android.quicksearchbox.SearchSettings;
import com.android.quicksearchbox.SearchSettingsImpl;
import com.android.quicksearchbox.SearchWidgetProvider;

import android.app.SearchEngineInfo;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.util.Log;

/**
 * Logic backing the searchable items activity or fragment.
 */
public class SearchEngineItemsController implements PreferenceController, OnPreferenceClickListener {

    private static final boolean DBG = true;
    private static final String TAG = "QSB.SearchEngineItemsController";
    public static final String DEFAULT_SEARCH_ENGINE = "google";
    /// M: default cmcc search engine. @{
    public static final String DEFAULT_CMCC_SEARCH_ENGINE = "baidu";
    /// @}
    public static final String PREF_SYNC_SEARCH_ENGINE = "syc_search_engine";

    public static final String SEARCH_ENGINE_PREF = "search_engine";
    // intent action used to notify Browser that user has has changed search engine setting
    private static final String ACTION_BROWSER_SEARCH_ENGINE_CHANGED = 
    		"com.android.browser.SEARCH_ENGINE_CHANGED";

    private final SearchSettings mSearchSettings;
    private final Context mContext;
    
    private SharedPreferences mPrefs;
    private List<SearchEngineItemPreference> mRadioPrefs;
    private SearchEngineUnifyPreference mCheckBoxPref;
    private String[] mEntryValues;
    private String[] mEntries;

    // References to the top-level preference objects
    private PreferenceGroup mSearchEnginePreferences;
 

    public SearchEngineItemsController(SearchSettings searchSettings, Context context) {
        mSearchSettings = searchSettings;
        mContext = context;
        mPrefs = context.getSharedPreferences(SearchSettingsImpl.PREFERENCES_NAME, Context.MODE_PRIVATE);
        mRadioPrefs = new ArrayList<SearchEngineItemPreference>();
    }

    public void handlePreference(Preference searchEnginePreferences) {
    	mSearchEnginePreferences = (PreferenceGroup) searchEnginePreferences;
        populateSourcePreference();
    }

    public String getCorporaPreferenceKey() {
        return SEARCH_ENGINE_PREF;
    }

    private SearchSettings getSettings() {
        return mSearchSettings;
    }

    private Context getContext() {
        return mContext;
    }

    private Resources getResources() {
        return getContext().getResources();
    }
    
    private static List<SearchEngineInfo> getSearchEngineInfos(Context context) {
        SearchManager searchManager = (SearchManager)context.getSystemService(Context.SEARCH_SERVICE);
        return searchManager.getSearchEngineInfos();
    }


    /**
     * Fills the suggestion source list.
     */
    private void populateSourcePreference() {
    	List<SearchEngineInfo> searchEngines = getSearchEngineInfos(mContext);
    	int len = searchEngines.size();
    	if (len <= 0) {
    		Log.w(TAG, "SearchManager.getSearchEngineInfos return 0 search engine");
    		return;
    	}
    	mEntryValues = null;
    	mEntries = null;
    	mEntryValues = new String[len];
    	mEntries = new String[len];
    	int selectedItem = -1;
        String defaultSearchEngine = mSearchSettings.getDefaultSearchEngineName();
        /// M: engine name can't be null here.. @{
        String[] vendorName = defaultSearchEngine.split("_");
        for (int i = 0; i < len; i++) {
    		mEntryValues[i] = searchEngines.get(i).getName();
    		mEntries[i] = searchEngines.get(i).getLabel();
            String[] candidateVendorName = mEntryValues[i].split("_");
            // if the engine come from the same vendor, use it.
            if (candidateVendorName[0].equals(vendorName[0])) {
                selectedItem = i;
			}
		}
    	/// @}
    	PreferenceCategory consistencyPref = new PreferenceCategory(getContext());
        consistencyPref.setTitle(R.string.pref_content_search_engine_consistency);
        mSearchEnginePreferences.addPreference(consistencyPref);
        
    	mCheckBoxPref = createUnifySearchEnginePreference();
        mCheckBoxPref.setKey("toggle_consistency");
        mCheckBoxPref.setTitle(R.string.pref_search_engine_unify);
        mCheckBoxPref.setSummaryOn(R.string.pref_search_engine_unify_summary);
        mCheckBoxPref.setSummaryOff(R.string.pref_search_engine_unify_summary);
        consistencyPref.addPreference(mCheckBoxPref);
        boolean syncSearchEngine = mSearchSettings.shouldSyncSearchEngineWithBrowser();
        mCheckBoxPref.setChecked(syncSearchEngine);
        
        PreferenceCategory searchEnginesPref = new PreferenceCategory(getContext());
        searchEnginesPref.setTitle(R.string.pref_content_search_engine);
        mSearchEnginePreferences.addPreference(searchEnginesPref);
        for (String entry : mEntries) {
        	Preference pref = createSearchEnginePreference(entry);
        	searchEnginesPref.addPreference(pref);
        }
        
        if (selectedItem!= -1) {
    		mRadioPrefs.get(selectedItem).setChecked(true);
        } 
        /// M: if there is no engine selected, using the first one as default engine. @{
        else if (mRadioPrefs.size() >= 1) {
            mRadioPrefs.get(0).setChecked(true);
        }
        // should never reach here.
        Log.w(TAG, "Unknown error, unreachable code executed.");
        /// @}
    }
    
    /**
     * Adds a unify search engine to the list of suggestion source checkbox preferences.
     */
    private SearchEngineUnifyPreference createUnifySearchEnginePreference() {
    	SearchEngineUnifyPreference sourcePref = new SearchEngineUnifyPreference(getContext());
        return sourcePref;
    }

    /**
     * Adds a suggestion source to the list of suggestion source checkbox preferences.
     */
    private SearchEngineItemPreference createSearchEnginePreference(String entry) {
    	SearchEngineItemPreference sourcePref = new SearchEngineItemPreference(getContext());
    	sourcePref.setTitle(entry);
    	sourcePref.setOnPreferenceClickListener(this);
    	mRadioPrefs.add(sourcePref);
        //sourcePref.setIcon(corpus.getCorpusIcon());
        return sourcePref;
    }

    public boolean onPreferenceClick(Preference preference) {
    	for(SearchEngineItemPreference radioPref: mRadioPrefs) {
			radioPref.setChecked(false);
		}
		((SearchEngineItemPreference)preference).setChecked(true);
		SharedPreferences.Editor editor = mPrefs.edit();
		SharedPreferencesCompat.apply(editor.putString(SEARCH_ENGINE_PREF, mEntryValues[preference.getOrder()]));
		return true;
    }

    public void onCreateComplete() {
    }

    public void onStop() {
    	SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(PREF_SYNC_SEARCH_ENGINE, mCheckBoxPref.isChecked());
		editor.commit();
		broadcastSearchEngineChangedInternal(mContext);
		if (mCheckBoxPref.isChecked()) {
			broadcastSearchEngineChangedExternal(mContext);
		}
    }

    public void onDestroy() {
    }

    public void onResume() {
    }
    
    private void broadcastSearchEngineChangedInternal(Context context) {
    	Intent intent = new Intent(SearchWidgetProvider.ACTION_SEARCH_ENGINE_CHANGED);
    	intent.setPackage(mContext.getPackageName());
    	/// M: Change default search engine to baidu. @{
    	intent.putExtra(SEARCH_ENGINE_PREF, mPrefs.getString(SEARCH_ENGINE_PREF, getSettings().getDefaultSearchEngineName()));
    	/// @}
    	
    	if (DBG) {
    		Log.i(TAG, "Broadcasting: " + intent);
    	}
        context.sendBroadcast(intent);
    }
    
    private void broadcastSearchEngineChangedExternal(Context context) {
    	Intent intent = new Intent(ACTION_BROWSER_SEARCH_ENGINE_CHANGED);
    	intent.setPackage("com.android.browser");
    	/// M: Change default search engine to baidu. @{
    	intent.putExtra(SEARCH_ENGINE_PREF, mPrefs.getString(SEARCH_ENGINE_PREF, getSettings().getDefaultSearchEngineName()));
    	/// @}
    	
    	if (DBG) {
    		Log.i(TAG, "Broadcasting: " + intent);
    	}
        context.sendBroadcast(intent);
    }
}
