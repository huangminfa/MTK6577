/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms;

import java.io.File;
import java.util.Locale;

import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.layout.LayoutManager;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.DraftCache;
import com.android.mms.drm.DrmUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.RateController;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.mms.MmsException;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

public class MmsApp extends Application {
    public static final String LOG_TAG = "Mms";
    public static final String TXN_TAG = "Mms/Txn";

    private SearchRecentSuggestions mRecentSuggestions;
    private TelephonyManager mTelephonyManager;
    private CountryDetector mCountryDetector;
    private CountryListener mCountryListener;
    private String mCountryIso;
    private static MmsApp sMmsApp = null;

    // for toast thread
    public static final int MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL = 2;
    public static final int MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION = 4;
    public static final int MSG_MMS_TOO_BIG_TO_DOWNLOAD = 6;
    public static final int MSG_MMS_CAN_NOT_SAVE = 8;
    public static final int MSG_MMS_CAN_NOT_OPEN = 10;
    public static final int MSG_DONE = 12;
    public static final int EVENT_QUIT = 100;
    private static HandlerThread mToastthread = null;
    private static Looper mToastLooper = null;
    private static ToastHandler mToastHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();

        sMmsApp = this;

        // Load the default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Figure out the country *before* loading contacts and formatting numbers
        MmsConfig.init(this);
        Contact.init(this);
        DraftCache.init(this);
        //Conversation.init(this);
        DownloadManager.init(this);
        RateController.init(this);
        //DrmUtils.cleanupStorage(this);
        LayoutManager.init(this);
        SmileyParser.init(this);
        MessagingNotification.init(this);
        mCountryDetector = (CountryDetector) getSystemService(Context.COUNTRY_DETECTOR);
        mCountryListener = new CountryListener() {
            @Override
            public synchronized void onCountryDetected(Country country) {
                mCountryIso = country.getCountryIso();
            }
        };
        mCountryDetector.addCountryListener(mCountryListener, getMainLooper());
        mCountryIso = mCountryDetector.detectCountry().getCountryIso();

        InitToastThread();
    }

    synchronized public static MmsApp getApplication() {
        return sMmsApp;
    }

    @Override
    public void onTerminate() {
        DrmUtils.cleanupStorage(this);
        mCountryDetector.removeCountryListener(mCountryListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LayoutManager.getInstance().onConfigurationChanged(newConfig);
    }

    /**
     * @return Returns the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }

    /**
     * Returns the content provider wrapper that allows access to recent searches.
     * @return Returns the content provider wrapper that allows access to recent searches.
     */
    public SearchRecentSuggestions getRecentSuggestions() {
        /*
        if (mRecentSuggestions == null) {
            mRecentSuggestions = new SearchRecentSuggestions(this,
                    SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
        }
        */
        return mRecentSuggestions;
    }

    public String getCurrentCountryIso() {
        return mCountryIso;
    }

    private void InitToastThread() {
        if (null == mToastHandler) {
            HandlerThread thread = new HandlerThread("MMSAppToast");
            thread.start();
            mToastLooper = thread.getLooper();
            if (null != mToastLooper) {
                mToastHandler = new ToastHandler(mToastLooper);
            }
        }
    }

    public synchronized static ToastHandler getToastHandler() {
        /*if (null == mToastHandler) {
            HandlerThread thread = new HandlerThread("MMSAppToast");
            thread.start();
            mToastLooper = thread.getLooper();
            mToastHandler = new ToastHandler(mToastLooper);
        }*/
        return mToastHandler;
    }

    public final class ToastHandler extends Handler {
        public ToastHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Xlog.d(MmsApp.TXN_TAG, "Toast Handler handleMessage :" + msg);
            
            switch (msg.what) {
                case EVENT_QUIT: {
                    Xlog.d(MmsApp.TXN_TAG, "EVENT_QUIT");
                    getLooper().quit();
                    return;
                }

                case MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL: {
                    Toast.makeText(sMmsApp, R.string.download_failed_due_to_full_memory, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION: {
                    Toast.makeText(sMmsApp, R.string.transmission_transiently_failed, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_TOO_BIG_TO_DOWNLOAD: {
                    Toast.makeText(sMmsApp, R.string.mms_too_big_to_download, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_CAN_NOT_SAVE: {
                    Toast.makeText(sMmsApp, R.string.cannot_save_message, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_MMS_CAN_NOT_OPEN: {
                    String str = sMmsApp.getResources().getString(R.string.unsupported_media_format, (String)msg.obj);
                    Toast.makeText(sMmsApp, str, Toast.LENGTH_LONG).show();
                    break;
                }

                case MSG_DONE: {
                    Toast.makeText(sMmsApp, R.string.done, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

}
