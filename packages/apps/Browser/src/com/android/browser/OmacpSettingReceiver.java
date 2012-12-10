package com.android.browser;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ParseException;
import android.net.WebAddress;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import com.mediatek.xlog.Xlog;
import com.android.browser.provider.BrowserProvider2;

public class OmacpSettingReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = true;
    private static final String XLOG = "browser/OmacpSettingReceiver";
    
    private static final String BROWSER_APP_ID = "w2";
    private static final String APP_ID_KEY = "appId";
    private static final String APP_RESULT = "result";
    private static final String APP_SETTING_ACTION = "com.mediatek.omacp.settings";
    private static final String APP_SETTING_RESULT_ACTION = "com.mediatek.omacp.settings.result";
    private static final String APP_CAPABILITY_ACTION = "com.mediatek.omacp.capability";
    private static final String APP_CAPABILITY_RESULT_ACTION = "com.mediatek.omacp.capability.result";
    
    //Omacp key of setting app
    private static final String FOLDER_NAME = "NAME"; //application/NAME
    private static final String RESOURCE = "RESOURCE";//resource
    private static final String BOOKMARK_URI = "URI"; //resource/URI
    private static final String BOOKMARK_NAME = "NAME"; //resource/NAME
    private static final String STARTPAGE = "STARTPAGE"; //resource/STARTPAGE
    private static final String STARTPAGE_TRUE = "1"; //STARTPAGE = true
    
    //Omacp key of capability item
    private static final String BROWSER = "browser";
    private static final String BROWSER_BOOKMARK_FOLDER = "browser_bookmark_folder";
    private static final String BROWSER_TO_PROXY = "browser_to_proxy";
    private static final String BROWSER_TO_NAPID = "browser_to_napid";
    private static final String BROWSER_BOOKMARK_NAME = "browser_bookmark_name";
    private static final String BROWSER_BOOKMARK = "browser_bookmark";
    private static final String BROWSER_USERNAME = "browser_username";
    private static final String BROWSER_PASSWORD = "browser_password";
    private static final String BROWSER_HOMEPAGE = "browser_homepage";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Xlog.d(XLOG,"OmacpSettingReceiver action:" + intent.getAction());
        }
        final ContentResolver cr = context.getContentResolver();
        if (APP_SETTING_ACTION.equals(intent.getAction())) {
            boolean result = false;
            String folderName = intent.getStringExtra(FOLDER_NAME);
            if (null == folderName) {
                result = setBookmarkAndHomePage(context, intent, BrowserProvider2.FIXED_ID_ROOT);
            } else {
                Xlog.i(XLOG, "folderName isn't null");
                long folderId = AddBookmarkPage.addFolderToRoot(context, folderName);
                result = setBookmarkAndHomePage(context, intent, folderId);
            }
            //send setting result to Omacp
            sendSettingResult(context, result);
        }
        if (APP_CAPABILITY_ACTION.equals(intent.getAction())) {
            //send capability result to Omacp
            sendCapabilityResult(context);
        }
    }
    private boolean setBookmarkAndHomePage(Context context, Intent intent, long folderId) {
        boolean result = false;
        if (-1 == folderId) {
            return result;
        }
        
        final ContentResolver cr = context.getContentResolver();
        ArrayList<HashMap<String, String>> resourceMapList = 
            (ArrayList<HashMap<String, String>>) intent.getSerializableExtra(RESOURCE);
        if (null == resourceMapList) {
            Xlog.i(XLOG, "resourceMapList is null");
        } else {
            if (DEBUG) {
                 Xlog.i(XLOG, "resourceMapList size:" + resourceMapList.size());
            }
            boolean hasSetStartPage = false;
            for (HashMap<String, String> item : resourceMapList) {
                String url = item.get(BOOKMARK_URI);
                String name = item.get(BOOKMARK_NAME);
                String startPage = item.get(STARTPAGE);
                if (null == url) {
                    continue;
                }
//                String formattedUrl = UrlUtils.fixUrl(url);
                String formattedUrl = url.trim();
                formattedUrl = UrlUtils.smartUrlFilter(formattedUrl);

                if (null == formattedUrl) {
                    continue;
                }
                if (null == name) {
                    name = formattedUrl;
                }

                Bookmarks.addBookmark(context, false, formattedUrl, name, null, folderId);

                if (!hasSetStartPage && null != startPage && startPage.equals(STARTPAGE_TRUE)) {
                    setHomePage(context, formattedUrl);
                    hasSetStartPage = true;
                }
                if (DEBUG) {
                    Xlog.i(XLOG, "BOOKMARK_URI: " + formattedUrl);
                    Xlog.i(XLOG, "BOOKMARK_NAME: " + name);
                    Xlog.i(XLOG, "STARTPAGE: " + startPage);
                }
            }
            result = true;
        }
        return result;
    }
    private boolean setHomePage(Context context, String url) {
        if (null == url || url.length() <= 0) {
            return false;
        }
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = p.edit();
        editor.putString(BrowserSettings.PREF_HOMEPAGE, url);
        editor.commit();
        return true;
    }
    private void sendSettingResult(Context context, boolean result) {
        Intent intent = new Intent(APP_SETTING_RESULT_ACTION);
        intent.putExtra(APP_ID_KEY, BROWSER_APP_ID);
        intent.putExtra(APP_RESULT, result);
        
        if (DEBUG) {
            Xlog.i(XLOG, "Setting Broadcasting: " + intent);
        }
        context.sendBroadcast(intent);
    }
    private void sendCapabilityResult(Context context) {
        Intent intent = new Intent(APP_CAPABILITY_RESULT_ACTION);
        intent.putExtra(APP_ID_KEY, BROWSER_APP_ID);
        intent.putExtra(BROWSER, true);
        intent.putExtra(BROWSER_BOOKMARK_FOLDER, true);
        intent.putExtra(BROWSER_TO_PROXY, false);
        intent.putExtra(BROWSER_TO_NAPID, false);
        intent.putExtra(BROWSER_BOOKMARK_NAME, true);
        intent.putExtra(BROWSER_BOOKMARK, true);
        intent.putExtra(BROWSER_USERNAME, false);
        intent.putExtra(BROWSER_PASSWORD, false);
        intent.putExtra(BROWSER_HOMEPAGE, true);
        
        if (DEBUG) {
            Xlog.i(XLOG, "Capability Broadcasting: " + intent);
        }
        context.sendBroadcast(intent);
    }
}
