/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.server.search;

import com.android.internal.content.PackageMonitor;

import android.app.ISearchManager;
import android.app.SearchEngineInfo;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * The search manager service handles the search UI, and maintains a registry of searchable
 * activities.
 */
public class SearchManagerService extends ISearchManager.Stub {

    // broadcast for search engine change.
    public static final String ACTION_SEARCH_ENGINE_CHANGED = "com.android.quicksearchbox.SEARCH_ENGINE_CHANGED";
    // general debugging support
    private static final String TAG = "SearchManagerService";

    // Context that the service is running in.
    private final Context mContext;

    // This field is initialized lazily in getSearchables(), and then never modified.
    private Searchables mSearchables;

    private ContentObserver mGlobalSearchObserver;

    // This list saved all the search engines supported by search framework.
    private List<SearchEngineInfo> mSearchEngineInfos;

    /**
     * Initializes the Search Manager service in the provided system context.
     * Only one instance of this object should be created!
     *
     * @param context to use for accessing DB, window manager, etc.
     */
    public SearchManagerService(Context context)  {
        mContext = context;
        mContext.registerReceiver(new BootCompletedReceiver(),
                new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
        mGlobalSearchObserver = new GlobalSearchProviderObserver(
                mContext.getContentResolver());
    }

    private synchronized Searchables getSearchables() {
        if (mSearchables == null) {
            Log.i(TAG, "Building list of searchable activities");
            new MyPackageMonitor().register(mContext, true);
            mSearchables = new Searchables(mContext);
            mSearchables.buildSearchableList();
        }
        return mSearchables;
    }

    /**
     * Creates the initial searchables list after boot.
     */
    private final class BootCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    mContext.unregisterReceiver(BootCompletedReceiver.this);
                    getSearchables();

                    /// M: fix widget searchengine not change after language
                    /// changed at first boot. @{
                    mContext.registerReceiver(new LocaleChangeReceiver(),
                            new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
                    /// @}
                }
            }.start();
        }
    }

    /**
     * Refreshes the "searchables" list when packages are added/removed.
     */
    class MyPackageMonitor extends PackageMonitor {

        @Override
        public void onSomePackagesChanged() {
            updateSearchables();
        }

        @Override
        public void onPackageModified(String pkg) {
            updateSearchables();
        }

        private void updateSearchables() {
            // Update list of searchable activities
            getSearchables().buildSearchableList();
            // Inform all listeners that the list of searchables has been updated.
            Intent intent = new Intent(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            mContext.sendBroadcast(intent);
        }
    }

    class GlobalSearchProviderObserver extends ContentObserver {
        private final ContentResolver mResolver;

        public GlobalSearchProviderObserver(ContentResolver resolver) {
            super(null);
            mResolver = resolver;
            mResolver.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.SEARCH_GLOBAL_SEARCH_ACTIVITY),
                    false /* notifyDescendants */,
                    this);
        }

        @Override
        public void onChange(boolean selfChange) {
            getSearchables().buildSearchableList();
            Intent intent = new Intent(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            mContext.sendBroadcast(intent);
        }

    }

    //
    // Searchable activities API
    //

    /**
     * Returns the SearchableInfo for a given activity.
     *
     * @param launchActivity The activity from which we're launching this search.
     * @return Returns a SearchableInfo record describing the parameters of the search,
     * or null if no searchable metadata was available.
     */
    public SearchableInfo getSearchableInfo(final ComponentName launchActivity) {
        if (launchActivity == null) {
            Log.e(TAG, "getSearchableInfo(), activity == null");
            return null;
        }
        return getSearchables().getSearchableInfo(launchActivity);
    }

    /**
     * Returns a list of the searchable activities that can be included in global search.
     */
    public List<SearchableInfo> getSearchablesInGlobalSearch() {
        return getSearchables().getSearchablesInGlobalSearchList();
    }

    public List<ResolveInfo> getGlobalSearchActivities() {
        return getSearchables().getGlobalSearchActivities();
    }

    /**
     * Gets the name of the global search activity.
     */
    public ComponentName getGlobalSearchActivity() {
        return getSearchables().getGlobalSearchActivity();
    }

    /**
     * Gets the name of the web search activity.
     */
    public ComponentName getWebSearchActivity() {
        return getSearchables().getWebSearchActivity();
    }
    
    /**
     * Gets the search engines for web search 
     * @hide
     */
    public synchronized List<SearchEngineInfo> getSearchEngineInfos() {
    	if (mSearchEngineInfos == null) {
    		initSearchEngineInfos();
    	}
    	return mSearchEngineInfos;
    }
    
    private void initSearchEngineInfos() {
    	mSearchEngineInfos = new ArrayList<SearchEngineInfo>();
        Resources res = mContext.getResources();
        String[] searchEngines = res.getStringArray(com.mediatek.internal.R.array.search_engines);
        for (int i = 0; i < searchEngines.length; i++) {
            String name = searchEngines[i];
            SearchEngineInfo info = SearchEngineInfo.getSpecifiedSearchEngineInfo(mContext, name);
            mSearchEngineInfos.add(info);
        }
        // tell search widget that search engine changed.
        broadcastSearchEngineChangedInternal(mContext);
    }
    
    /**
     * Creates the initial searchables list after boot.
     */
    private final class LocaleChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	initSearchEngineInfos();
        }
    }

    /**
     * Sent a broadcast without extral data.
     */
    private void broadcastSearchEngineChangedInternal(Context context) {
        Intent intent = new Intent(ACTION_SEARCH_ENGINE_CHANGED);
        intent.setPackage("com.android.quicksearchbox");
        context.sendBroadcast(intent);
    }
}
