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

package com.android.systemui.statusbar.policy;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wimax.WimaxManagerConstants;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.cdma.EriInfo;
import com.android.internal.util.AsyncChannel;
import com.android.server.am.BatteryStatsService;
import com.android.systemui.R;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

// [SystemUI] Support "Dual SIM".
public class NetworkControllerGemini extends BroadcastReceiver {
    // debug
    static final String TAG = "NetworkControllerGemini";
    static final boolean DEBUG = false;
    static final boolean CHATTY = false; // additional diagnostics, but not logspew

    private boolean mIsRoaming = false;
    private boolean mIsRoamingGemini = false;
    
    // telephony
    boolean mHspaDataDistinguishable;  
    final TelephonyManager mPhone;
    boolean mDataConnected;
    IccCard.State mSimState = IccCard.State.READY;
    int mPhoneState = TelephonyManager.CALL_STATE_IDLE;
    int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    int mDataState = TelephonyManager.DATA_DISCONNECTED;
    int mDataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
    ServiceState mServiceState;
    SignalStrength mSignalStrength;
    int[] mDataIconList;
    String mNetworkName;
    String mNetworkNameDefault;
    String mNetworkNameSeparator;
    int mPhoneSignalIconId[] = {0,0};
    int mDataDirectionIconId; // data + data direction on phones
    int mDataSignalIconId;
    int mDataTypeIconId;
    boolean mDataActive;
    int mMobileActivityIconId; // overlay arrows for data direction
    int mLastSignalLevel[] = {0,0};
    boolean mShowPhoneRSSIForData = false;
    boolean mShowAtLeastThreeGees = false;
    boolean mAlwaysShowCdmaRssi = false;

    String mContentDescriptionPhoneSignal;
    String mContentDescriptionWifi;
    String mContentDescriptionWimax;
    String mContentDescriptionCombinedSignal;
    String mContentDescriptionDataType;

    // wifi
    final WifiManager mWifiManager;
    AsyncChannel mWifiChannel;
    boolean mWifiEnabled, mWifiConnected;
    int mWifiRssi, mWifiLevel;
    String mWifiSsid;
    int mWifiIconId = 0;
    int mWifiActivityIconId = 0; // overlay arrows for wifi direction
    int mWifiActivity = WifiManager.DATA_ACTIVITY_NONE;

    // bluetooth
    private boolean mBluetoothTethered = false;
    private int mBluetoothTetherIconId =
        com.android.internal.R.drawable.stat_sys_tether_bluetooth;

    private static final boolean IS_CMCC = SystemProperties.get("ro.operator.optr").equals("OP01");

    //wimax
    private boolean mWimaxSupported = false;
    private boolean mIsWimaxEnabled = false;
    private boolean mWimaxConnected = false;
    private boolean mWimaxIdle = false;
    private int mWimaxIconId[] = {0};
    private int mWimaxSignal = 0;
    private int mWimaxState = 0;
    private int mWimaxExtraState = 0;
    // data connectivity (regardless of state, can we access the internet?)
    // state of inet connection - 0 not connected, 100 connected
    private int mInetCondition = 0;
    private static final int INET_CONDITION_THRESHOLD = 50;

    private boolean mAirplaneMode = false;

    // our ui
    Context mContext;
    ArrayList<ImageView> mPhoneSignalIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mDataDirectionIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mDataDirectionOverlayIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mWifiIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mWimaxIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mCombinedSignalIconViews = new ArrayList<ImageView>();
    ArrayList<ImageView> mDataTypeIconViews = new ArrayList<ImageView>();
    ArrayList<TextView> mCombinedLabelViews = new ArrayList<TextView>();
    ArrayList<TextView> mMobileLabelViews = new ArrayList<TextView>();
    ArrayList<TextView> mWifiLabelViews = new ArrayList<TextView>();

    ArrayList<SignalCluster> mSignalClusters = new ArrayList<SignalCluster>();
    int mLastPhoneSignalIconId[] = {-1,-1};
    int mLastDataDirectionIconId = -1;
    int mLastDataDirectionOverlayIconId = -1;
    int mLastWifiIconId = -1;
    int mLastWimaxIconId = -1;
    int mLastCombinedSignalIconId = -1;
    int mLastDataTypeIconId = -1;
    String mLastCombinedLabel = "";

    private boolean mHasMobileDataFeature;

    boolean mDataAndWifiStacked = false;
    
    boolean sIsScreenLarge = false;

    // yuck -- stop doing this here and put it in the framework
    IBatteryStats mBatteryStats;

    public interface SignalCluster {
        void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon, 
                String contentDescription);
        // [SystemUI] Support "Dual SIM". {
        void setMobileDataIndicators(int slotId, boolean visible, int []strengthIcon, int activityIcon,
                int typeIcon, String contentDescription, String typeContentDescription);
        void setIsAirplaneMode(boolean is);
        void setSIMBackground(int slotId, int resId);
	void setSIMState(int slotId, boolean isSIMCUSignVisible);
        void setDataConnected(int slotId, boolean dataConnected);
        void setDataNetType3G(int slotId, DataNetType dataNetType3G);
        void setRoamingFlag(boolean roaming,boolean roamingGemini);
        void apply();
        // [SystemUI] Support "Dual SIM". }
    }

    /**
     * Construct this controller object and register for updates.
     */
    public NetworkControllerGemini(Context context) {
        mContext = context;
        final Resources res = context.getResources();

        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        mHasMobileDataFeature = cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);

        mShowPhoneRSSIForData = res.getBoolean(R.bool.config_showPhoneRSSIForData);
        mShowAtLeastThreeGees = res.getBoolean(R.bool.config_showMin3G);
        Xlog.d(TAG, "NetworkControllerGemini, mShowAtLeastThreeGees=" + mShowAtLeastThreeGees);

        int screenSize = (res.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        sIsScreenLarge = ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE));
        mAlwaysShowCdmaRssi = res.getBoolean(
                com.android.internal.R.bool.config_alwaysUseCdmaRssi);

        // set up the default wifi icon, used when no radios have ever appeared
        updateWifiIcons();
        updateWimaxIcons();

        // telephony
        mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        // [SystemUI] Support "Dual SIM". {
        if (IS_CU) {
            mDataIconList = TelephonyIconsGeminiCU.DATA_G[0];
            mDataIconListGemini = TelephonyIconsGeminiCU.DATA_G[0];
            mMobileVisible = true;
            mMobileVisibleGemini = true;
            mLastMobileVisible = false;
            mLastMobileVisibleGemini = false;
        } 
        mDataIconList = TelephonyIconsGemini.DATA_G;
        mDataIconListGemini = TelephonyIconsGemini.DATA_G;
		if (IS_CT) {
        	mDataIconList = TelephonyIconsGemini.DATA_G_CT;
            mDataIconListGemini = TelephonyIconsGemini.DATA_G_CT;
        }
        
        mSimCardReady = SystemProperties.getBoolean(TelephonyProperties.PROPERTY_SIM_INFO_READY, false);
        if (mSimCardReady) {
            if (IS_CU) {
                if (IS_CT) {// support CT
                	mPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim_ct;
                	mPhoneSignalIconIdGemini[0] = R.drawable.zzz_stat_sys_signal_null_sim_ct;
				} else {
	                mPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim1;
	                mPhoneSignalIconIdGemini[0] = R.drawable.zzz_stat_sys_signal_null_sim2;
				}
            } else {
                mPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_0;
                mPhoneSignalIconIdGemini[0] = R.drawable.zzz_stat_sys_signal_0;
            }
        }
        mPhone.listenGemini(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
              | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
              | PhoneStateListener.LISTEN_CALL_STATE
              | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
              | PhoneStateListener.LISTEN_DATA_ACTIVITY,
              Phone.GEMINI_SIM_1);
        mPhone.listenGemini(mPhoneStateListenerGemini,
                PhoneStateListener.LISTEN_SERVICE_STATE
              | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
              | PhoneStateListener.LISTEN_CALL_STATE
              | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
              | PhoneStateListener.LISTEN_DATA_ACTIVITY,
              Phone.GEMINI_SIM_2);
        // [SystemUI] Support "Dual SIM". }
        mHspaDataDistinguishable = mContext.getResources().getBoolean(
                R.bool.config_hspa_data_distinguishable);
        mNetworkNameSeparator = mContext.getString(R.string.status_bar_network_name_separator);
        mNetworkNameDefault = mContext.getString(
                com.android.internal.R.string.lockscreen_carrier_default);
        mNetworkName = mNetworkNameDefault;
        // [SystemUI] Support "Dual SIM". {
        mNetworkNameGemini = mNetworkNameDefault;
        // [SystemUI] Support "Dual SIM". }

        // wifi
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Handler handler = new WifiHandler();
        mWifiChannel = new AsyncChannel();
        Messenger wifiMessenger = mWifiManager.getMessenger();
        if (wifiMessenger != null) {
            mWifiChannel.connect(mContext, handler, wifiMessenger);
        }

        // broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mWimaxSupported = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_wimaxEnabled);
        if(mWimaxSupported) {
            filter.addAction(WimaxManagerConstants.WIMAX_NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION);
            filter.addAction(WimaxManagerConstants.NET_4G_STATE_CHANGED_ACTION);
        }
        // [SystemUI] Support "Dual SIM". {
        filter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_INSERTED_STATUS);
        filter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        // [SystemUI] Support "Dual SIM". }
        context.registerReceiver(this, filter);

        // AIRPLANE_MODE_CHANGED is sent at boot; we've probably already missed it
        updateAirplaneMode();

        // yuck
        mBatteryStats = BatteryStatsService.getService();
    }

    public void addPhoneSignalIconView(ImageView v) {
        mPhoneSignalIconViews.add(v);
    }

    public void addDataDirectionIconView(ImageView v) {
        mDataDirectionIconViews.add(v);
    }

    public void addDataDirectionOverlayIconView(ImageView v) {
        mDataDirectionOverlayIconViews.add(v);
    }

    public void addWifiIconView(ImageView v) {
        mWifiIconViews.add(v);
    }

    public void addWimaxIconView(ImageView v) {
        mWimaxIconViews.add(v);
    }

    public void addCombinedSignalIconView(ImageView v) {
        mCombinedSignalIconViews.add(v);
    }

    public void addDataTypeIconView(ImageView v) {
        mDataTypeIconViews.add(v);
    }

    public void addCombinedLabelView(TextView v) {
        mCombinedLabelViews.add(v);
    }

    public void addMobileLabelView(TextView v) {
        mMobileLabelViews.add(v);
    }

    public void addWifiLabelView(TextView v) {
        mWifiLabelViews.add(v);
    }

    public void addSignalCluster(SignalCluster cluster) {
        mSignalClusters.add(cluster);
        refreshSignalCluster(cluster);
    }

    public void refreshSignalCluster(SignalCluster cluster) {
    	cluster.setRoamingFlag(mIsRoaming, mIsRoamingGemini);
        cluster.setWifiIndicators(
                mWifiConnected, // only show wifi in the cluster if connected
                mWifiIconId,
                mWifiActivityIconId,
                mContentDescriptionWifi);
        if (mIsWimaxEnabled && mWimaxConnected) {
            // wimax is special
            cluster.setMobileDataIndicators(
                    Phone.GEMINI_SIM_1,
                    true,
                    mAlwaysShowCdmaRssi ? mPhoneSignalIconId : mWimaxIconId,
                    mMobileActivityIconId,
                    mDataTypeIconId,
                    mContentDescriptionWimax,
                    mContentDescriptionDataType);
        } else {
            // normal mobile data
            cluster.setMobileDataIndicators(
                    Phone.GEMINI_SIM_1,
                    mHasMobileDataFeature && mMobileVisible,
                    mPhoneSignalIconId,
                    mMobileActivityIconId,
                    mDataTypeIconId,
                    mContentDescriptionPhoneSignal,
                    mContentDescriptionDataType);
            // [SystemUI] Support "Dual SIM". {
            cluster.setMobileDataIndicators(
                    Phone.GEMINI_SIM_2,
                    mHasMobileDataFeature && mMobileVisibleGemini,
                    mPhoneSignalIconIdGemini,
                    mMobileActivityIconIdGemini,
                    mDataTypeIconIdGemini,
                    mContentDescriptionPhoneSignalGemini,
                    mContentDescriptionDataTypeGemini);
            // [SystemUI] Support "Dual SIM". }
        }
        cluster.setIsAirplaneMode(mAirplaneMode);
        cluster.apply();
    }

    public void setStackedMode(boolean stacked) {
        mDataAndWifiStacked = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Xlog.d(TAG, "onReceive, intent action is " + action);
        if (action.equals(WifiManager.RSSI_CHANGED_ACTION)
                || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
                || action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            updateWifiState(intent);
            refreshViews();
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            // [SystemUI] Support "Dual SIM". {
            int slotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
            updateSimState(slotId, intent);
            updateDataIcon(slotId);
            refreshViews(slotId);
            // [SystemUI] Support "Dual SIM". }
        } else if (action.equals(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION)) {
            // [SystemUI] Support "Dual SIM". {
            int slotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
            updateNetworkName(slotId,
                    intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                    intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                    intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                    intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
            refreshViews(slotId);
            // [SystemUI] Support "Dual SIM". }
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                 action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
            updateConnectivity(intent);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
        } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
            refreshViews();
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            updateAirplaneMode();
            refreshViews();
        } else if (action.equals(WimaxManagerConstants.NET_4G_STATE_CHANGED_ACTION) ||
            action.equals(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION) ||
            action.equals(WimaxManagerConstants.WIMAX_NETWORK_STATE_CHANGED_ACTION)) {
            updateWimaxState(intent);
            refreshViews();
        }
        // [SystemUI] Support "Dual SIM". {
        else if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
            SIMHelper.updateSIMInfos(context);
            int type = intent.getIntExtra("type", -1);
            long simId = intent.getLongExtra("simid", -1);
            if (type == 1) {
                // color changed
                updateDataNetType(Phone.GEMINI_SIM_1);
                updateDataNetType(Phone.GEMINI_SIM_2);
                updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
                updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
                updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
                updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
            }
        } else if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
            int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
            updateDataNetType(slotId);
            updateTelephonySignalStrength(slotId);
            updateSignalBackgroundBySlotId(slotId);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_INSERTED_STATUS)) {
            SIMHelper.updateSIMInfos(context);
            updateDataNetType(Phone.GEMINI_SIM_1);
            updateDataNetType(Phone.GEMINI_SIM_2);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
            Xlog.d(TAG, "onReceive from TelephonyIntents.ACTION_SIM_INFO_UPDATE");
            mSimCardReady = true;
            SIMHelper.updateSIMInfos(context);
            updateDataNetType(Phone.GEMINI_SIM_1);
            updateDataNetType(Phone.GEMINI_SIM_2);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
        } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
            Xlog.d(TAG, "onReceive from android.intent.action.ACTION_SHUTDOWN_IPO");
            mSimCardReady = false;
        }
        // [SystemUI] Support "Dual SIM". }
    }

    // ===== Telephony ==============================================================

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, sim1 before.");
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, signalStrength=" + signalStrength.getLevel());
            mSignalStrength = signalStrength;
            if(IS_CT)
            	updateDataNetType(Phone.GEMINI_SIM_1);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_1);
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, sim1 after.");
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, sim1 before.");
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, state=" + state.getState());
            mServiceState = state;
            //BEGIN [20120301][ALPS00245624]
            //mDataNetType = mServiceState.getRadioTechnology();
            TelephonyManager mTelephonyManager = TelephonyManager.getDefault();
            mDataNetType = mTelephonyManager.getNetworkTypeGemini(Phone.GEMINI_SIM_1);
            Xlog.d(TAG,"PhoneStateListener:onServiceStateChanged sim1 mDataNetType= "+mDataNetType);
            //END   [20120301][ALPS00245624]
			updateDataNetType(Phone.GEMINI_SIM_1);            
			updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
            
            updateDataIcon(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_1);
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, sim1 after.");
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, sim1 before.");
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, state=" + state);
            // In cdma, if a voice call is made, RSSI should switch to 1x.
            if (isCdma(Phone.GEMINI_SIM_1)) {
            	if(IS_CT)
            		updateDataNetType(Phone.GEMINI_SIM_1);
                updateTelephonySignalStrength(Phone.GEMINI_SIM_1);
                updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_1);
                refreshViews(Phone.GEMINI_SIM_1);
            }
            if (IS_CU && FeatureOption.MTK_DT_SUPPORT) {
                updateDataNetType(Phone.GEMINI_SIM_1);
                updateDataIcon(Phone.GEMINI_SIM_1);
                refreshViews(Phone.GEMINI_SIM_1);
            } else {
            //updateDataNetType(Phone.GEMINI_SIM_1);
            updateDataIcon(Phone.GEMINI_SIM_1);
            //refreshViews(Phone.GEMINI_SIM_1);
            updateDataNetType(Phone.GEMINI_SIM_2);
            updateDataIcon(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_1);
            }
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, sim1 after.");
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, sim1 before.");
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, state=" + state + " type=" + networkType);
            mDataState = state;
            mDataNetType = networkType;
            updateDataNetType(Phone.GEMINI_SIM_1);
            updateDataIcon(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_1);
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, sim1 after.");
        }

        @Override
        public void onDataActivity(int direction) {
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, sim1 before.");
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, direction=" + direction);
            mDataActivity = direction;
            updateDataIcon(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_1);
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, sim1 after.");
        }
    };

    // [SystemUI] Support "Dual SIM".
    private final void updateSimState(int slotId, Intent intent) {
        IccCard.State tempSimState = null;

        String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
        if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            tempSimState = IccCard.State.ABSENT;
        }
        else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            tempSimState = IccCard.State.READY;
        }
        else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason = intent.getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
            if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                tempSimState = IccCard.State.PIN_REQUIRED;
            }
            else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                tempSimState = IccCard.State.PUK_REQUIRED;
            }
            else {
                tempSimState = IccCard.State.NETWORK_LOCKED;
            }
        } else {
            tempSimState = IccCard.State.UNKNOWN;
        }

        if (tempSimState != null) {
            if (slotId == Phone.GEMINI_SIM_1) {
                mSimState = tempSimState;
            } else {
                mSimStateGemini = tempSimState;
            }
        }
    }

    // [SystemUI] Support "Dual SIM".
    private boolean isCdma(int slotId) {
        SignalStrength tempSignalStrength;
        if (slotId == Phone.GEMINI_SIM_1) {
            tempSignalStrength = mSignalStrength;
        } else {
            tempSignalStrength = mSignalStrengthGemini;
        }
        return (tempSignalStrength != null) && !tempSignalStrength.isGsm();
    }

    // [SystemUI] Support "Dual SIM".
    private boolean hasService(int slotId) {
        ServiceState tempServiceState;
        if (slotId == Phone.GEMINI_SIM_1) {
            tempServiceState = mServiceState;
        } else {
            tempServiceState = mServiceStateGemini;
        }
        if (tempServiceState != null) {
            switch (tempServiceState.getState()) {
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_POWER_OFF:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    private void updateAirplaneMode() {
        mAirplaneMode = (Settings.System.getInt(mContext.getContentResolver(),
            Settings.System.AIRPLANE_MODE_ON, 0) == 1);
    }

    // [SystemUI] Support "Dual SIM".
    private final void updateTelephonySignalStrength(int slotId) {
        boolean handled = false;

        boolean tempMobileVisible = false;
        boolean tempSIMCUSignVisible = true;
        int tempPhoneSignalIconId[] = {0,0};
        int tempDataSignalIconId = -1;
        ServiceState tempServiceState = null;
        SignalStrength tempSignalStrength = null;
        String tempContentDescriptionPhoneSignal = "";
        int tempLastSignalLevel[] = {-1,-1};

        if (slotId == Phone.GEMINI_SIM_1) {
            tempServiceState = mServiceState;
            tempSignalStrength = mSignalStrength;
        } else {
            tempServiceState = mServiceStateGemini;
            tempSignalStrength = mSignalStrengthGemini;
        }

       
        if (!mSimCardReady) {
            Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId +"), the SIMs initialization of framework has not been ready.");
            tempMobileVisible = false;
            handled = true;
        }
       

        // null signal state
        if (!handled && !isSimInserted(slotId)) {
            Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId +"), is null signal.");
            if (IS_CU) {
            	if (IS_CT) {// support CT
					tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim_ct;
				} else {
					if (slotId == Phone.GEMINI_SIM_1) {
						tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim1;
					} else {
						tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim2;
					}
				}
                tempMobileVisible = true;
                tempSIMCUSignVisible = false;
                Xlog.d(TAG, " null signal state isSimInserted(" + slotId + ") = "+isSimInserted(slotId) + 
                		"  hasService= "+ hasService(slotId)+" tempSIMCUSignVisible= "+tempSIMCUSignVisible);
            } else if(IS_CMCC) {
                tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim;
                tempMobileVisible = true;
                Xlog.d("cmccnullicon", "updateTelephonySignalStrength(" + slotId +"), CMCC null signal.");
            } else {
                tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_0;
                tempMobileVisible = false;
            }
            handled = true;
            Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId +"), null signal");
        }

        // searching state
        if (!handled && tempServiceState != null) {
            int regState = tempServiceState.getRegState();
            Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId +"), regState=" + regState);
            if (regState == ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING) {
            	Xlog.d(TAG, " searching state hasService= "+ hasService(slotId));
                if (slotId == Phone.GEMINI_SIM_1) {
                    tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_searching;
                } else {
                    tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_searching;
                }           
                if (IS_CU) {
                	tempMobileVisible = true;
                }
                handled = true;
                Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId +"), searching");
            }
        }
		// check radio_off model
		if (!handled  && (tempServiceState == null
				|| (!hasService(slotId) && !tempServiceState.isEmergencyOnly()))) {
                Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + ") tempServiceState = " + tempServiceState  );
			if (this.isSimInserted(slotId)) {
				Xlog.d(TAG, "SimIndicatorState = " + TelephonyManagerEx.getDefault().getSimIndicatorStateGemini(slotId));
				if (Phone.SIM_INDICATOR_RADIOOFF == TelephonyManagerEx
						.getDefault().getSimIndicatorStateGemini(slotId)) {
					if(IS_CU){
						tempSIMCUSignVisible = true;
					}
					if (slotId == Phone.GEMINI_SIM_1) {

						int simColorId = SIMHelper.getSIMColorIdBySlot(
								mContext, slotId);
						if (simColorId > -1 && simColorId < 4) {
							tempPhoneSignalIconId[0] = tempDataSignalIconId = TelephonyIconsGemini.FLIGHT_MODE[simColorId];
						}
						
					} else {

						int simColorId = SIMHelper.getSIMColorIdBySlot(
								mContext, slotId);
						if (simColorId > -1 && simColorId < 4) {
							tempPhoneSignalIconId[0] = tempDataSignalIconId = TelephonyIconsGemini.FLIGHT_MODE[simColorId];
						}
					
					}
					handled = true;
				}
			}
                        //handled = true;
		}
        // signal level state
		if (!handled) {
			boolean hasService = hasService(slotId);
			Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + "), hasService=" + hasService);
			if (!hasService) {
				if (CHATTY)
					Slog.d(TAG, "updateTelephonySignalStrength: !hasService()");
				if (IS_CU) {
					if (IS_CT) {// support CT
						tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim_ct;
					} else {
						if (slotId == Phone.GEMINI_SIM_1) {
							tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim1;
						} else {
							tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim2;
						}
					}
					
					tempSIMCUSignVisible = false;
	                Xlog.d(TAG, " signal level state isSimInserted(" + slotId + ") = "+isSimInserted(slotId) + 
	                		"  hasService= "+ hasService(slotId)+" tempSIMCUSignVisible= "+tempSIMCUSignVisible);
				} else if(IS_CMCC) {
	                tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_null_sim;
	                tempMobileVisible = true;
	                Xlog.d("cmccnullicon", "updateTelephonySignalStrength(" + slotId +"), CMCC service null signal.");
	            } else {
					tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_0;
					tempDataSignalIconId = R.drawable.zzz_stat_sys_signal_0;
				}
			} else {
                if (tempSignalStrength == null) {
                    if (CHATTY) Slog.d(TAG, "updateTelephonySignalStrength: mSignalStrength == null");
                    tempPhoneSignalIconId[0] = R.drawable.zzz_stat_sys_signal_0;
                    tempDataSignalIconId = R.drawable.zzz_stat_sys_signal_0;
                    tempContentDescriptionPhoneSignal = mContext.getString(AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0]);
                } else {
                    int iconLevel[]={0,0};
                    int[][] iconList = {{},{}};
                    //tempLastSignalLevel = iconLevel = tempSignalStrength.getLevel();
                    if (isCdma(slotId) && mAlwaysShowCdmaRssi) {
                    	tempLastSignalLevel[0]= iconLevel[0] = tempSignalStrength.getCdmaLevel();
                        Slog.d(TAG, "mAlwaysShowCdmaRssi=" + mAlwaysShowCdmaRssi
                                + " set to cdmaLevel=" + mSignalStrength.getCdmaLevel()
                                + " instead of level=" + mSignalStrength.getLevel());
                    } else {
                    	tempLastSignalLevel[0]= iconLevel[0] = tempSignalStrength.getLevel();
                    }
					DataNetType tempDataNetType = null;
                    if(slotId == Phone.GEMINI_SIM_1) {
                    	tempDataNetType = mDataNetType3G;
                    } else {
                    	tempDataNetType = mDataNetType3GGemini;
                    }
                    if(IS_CT) {
						if (tempDataNetType == DataNetType._1X_3G) {
							tempLastSignalLevel[0] = iconLevel[0] = tempSignalStrength
									.getEvdoLevel();
							tempLastSignalLevel[1] = iconLevel[1] = tempSignalStrength
									.getCdmaLevel();
							Xlog.d(TAG," CT SlotId ("
													+ slotId
													+ ") two signal strength : tempLastSignalLevel[0] = "
													+ ""
													+ tempLastSignalLevel[0]
													+ "  tempLastSignalLevel[1] = "
													+ tempLastSignalLevel[1]);
						}
                    }
                    
                    boolean isRoaming;
                    if (isCdma(slotId)) {
                        isRoaming = isCdmaEri(slotId);
                    } else {
                        // Though mPhone is a Manager, this call is not an IPC
                        isRoaming = mPhone.isNetworkRoamingGemini(slotId);
                    }
                    Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + "), isRoaming=" + isRoaming + ", mInetCondition=" + mInetCondition);
                    int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                    if (simColorId == -1) {
                        return;
                    }

                    //  int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                    Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + "), simColorId=" + simColorId);
                    if (IS_CMCC) {
                        iconList[0] = TelephonyIconsGemini.getTelephonyCMCCSignalStrengthIconList(simColorId);
                    } else {
                        iconList[0] = TelephonyIconsGemini.getTelephonySignalStrengthIconList(simColorId);
                    }
                    if(IS_CT) {
						if (tempDataNetType == DataNetType._1X_3G) {
							iconList[0] = TelephonyIconsGemini.getTelephonySignalStrengthIconList(simColorId, 0);
							iconList[1] = TelephonyIconsGemini.getTelephonySignalStrengthIconList(simColorId, 1);
							tempPhoneSignalIconId[1] = iconList[1][iconLevel[1]];
						}
                    }
                    
                    tempPhoneSignalIconId[0] = iconList[0][iconLevel[0]];
                    Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + "), tempDataNetType = "+tempDataNetType+" , simColorId=" + simColorId+"  tempPhoneSignalIconId[0] = " +
                    		""+tempPhoneSignalIconId[0]+"  tempPhoneSignalIconId[1] = "+tempPhoneSignalIconId[1]);

                    if (IS_CMCC) {
                        tempContentDescriptionPhoneSignal = mContext.getString(AccessibilityContentDescriptions.PHONE_CMCC_SIGNAL_STRENGTH[iconLevel[0]]);
                    } else {
                    	tempContentDescriptionPhoneSignal = mContext.getString(AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[iconLevel[0]]);
                    }
                        
                    tempDataSignalIconId = tempPhoneSignalIconId[0];
                    
                }
            }
        }

        if (slotId == Phone.GEMINI_SIM_1) {
            mPhoneSignalIconId = tempPhoneSignalIconId;
            mDataSignalIconId = tempDataSignalIconId;
            mContentDescriptionPhoneSignal = tempContentDescriptionPhoneSignal;
            mLastSignalLevel = tempLastSignalLevel;
            mMobileVisible = tempMobileVisible;
        } else {
            mPhoneSignalIconIdGemini = tempPhoneSignalIconId;
            mDataSignalIconIdGemini = tempDataSignalIconId;
            mContentDescriptionPhoneSignalGemini = tempContentDescriptionPhoneSignal;
            mLastSignalLevelGemini = tempLastSignalLevel;
            mMobileVisibleGemini = tempMobileVisible;
        }
        if(IS_CU){
            Xlog.d(TAG, " updateTelephonySignalStrength(" + slotId + ") tempSIMCUSignVisible= "+tempSIMCUSignVisible);
            if (tempPhoneSignalIconId[0] == -1){
            	tempSIMCUSignVisible = false;
            }
            for (SignalCluster cluster : mSignalClusters) {
                	Xlog.d(TAG, "updateTelephonySignalStrength(" + slotId + ") mSIMCUSignVisible= "+mSIMCUSignVisible);
                    cluster.setRoamingFlag(mIsRoaming, mIsRoamingGemini);
                    cluster.setSIMState(slotId, tempSIMCUSignVisible);                    
            }
        }
        
    }
	public enum DataNetType {_1X,_3G,_1X_3G,_G};
    // [SystemUI] Support "Dual SIM".
    private final void updateDataNetType(int slotId) {
        int tempDataNetType;
        DataNetType tempDataNetType3G = DataNetType._G;

        if (slotId == Phone.GEMINI_SIM_1) {
            tempDataNetType = mDataNetType;
        } else {
            tempDataNetType = mDataNetTypeGemini;
        }
        Xlog.d(TAG, "updateDataNetType(" + slotId + "), DataNetType=" + tempDataNetType + ".");

        int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
        if (simColorId == -1 ) {
            return;
        }
        Xlog.d(TAG, "updateDataNetType(" + slotId + "), simColorId=" + simColorId);

        int[] tempDataIconList;
        int tempDataTypeIconId;
        String tempContentDescriptionDataType;
        if (mIsWimaxEnabled && mWimaxConnected) {
            // wimax is a special 4g network not handled by telephony
        	if (IS_CT) {
            	tempDataIconList = TelephonyIconsGemini.DATA_4G_CT;
                tempDataTypeIconId = TelephonyIconsGemini.DATA_4G_CT[simColorId];
            } else {
            	tempDataIconList = TelephonyIconsGemini.DATA_4G;
            	tempDataTypeIconId = TelephonyIconsGemini.DATA_4G[simColorId];
            }
            
            tempContentDescriptionDataType = mContext.getString(
                    R.string.accessibility_data_connection_4g);
        } else {
            switch (tempDataNetType) {
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    if (!mShowAtLeastThreeGees) {
                        
                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_G_CT;
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_G;
                        }

                        tempDataTypeIconId = 0;
                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_gprs);
                        break;
                    } else {
                        // fall through
                    }
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    if (!mShowAtLeastThreeGees) {
      
                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_E_CT;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_E_CT[simColorId];
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_E;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_E[simColorId];
                        }

                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_edge);
                        break;
                    } else {
                        // fall through
                    }
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    tempDataNetType3G = DataNetType._3G;

                    if (IS_CT) {
                    	tempDataIconList = TelephonyIconsGemini.DATA_3G_CT;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_3G_CT[simColorId];
                    } else {
                    	tempDataIconList = TelephonyIconsGemini.DATA_3G;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_3G[simColorId];
                    }
                    tempContentDescriptionDataType = mContext.getString(
                            R.string.accessibility_data_connection_3g);
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    tempDataNetType3G = DataNetType._3G;
                    if (mHspaDataDistinguishable) {

                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_H_CT;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_H_CT[simColorId];
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_H;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_H[simColorId];
                        }
                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_3_5g);
                    } else {

                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_3G_CT;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_3G_CT[simColorId];
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_3G;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_3G[simColorId];
                        }
                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_3g);
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    // display 1xRTT for IS95A/B
                	tempDataNetType3G = DataNetType._1X;
                    
                    if (IS_CT) {
                    	tempDataIconList = TelephonyIconsGemini.DATA_1X_CT;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_1X_CT[simColorId];
                    } else {
                    	tempDataIconList = TelephonyIconsGemini.DATA_1X;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_1X[simColorId];
                    }
                    tempContentDescriptionDataType = mContext.getString(
                            R.string.accessibility_data_connection_cdma);
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                	tempDataNetType3G = DataNetType._1X;
                   
                    if (IS_CT) {
                    	tempDataIconList = TelephonyIconsGemini.DATA_1X_CT;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_1X_CT[simColorId];
                    } else {
                    	tempDataIconList = TelephonyIconsGemini.DATA_1X;
                    	tempDataTypeIconId = TelephonyIconsGemini.DATA_1X[simColorId];
                    }
                    
                    tempContentDescriptionDataType = mContext.getString(
                            R.string.accessibility_data_connection_cdma);
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    tempDataNetType3G = DataNetType._1X_3G;
                    
                    if (IS_CT) {
                    	tempDataIconList = TelephonyIconsGemini.DATA_3G_CT;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_3G_CT[simColorId];
                    } else {
                    	tempDataIconList = TelephonyIconsGemini.DATA_3G;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_3G[simColorId];
                    }
                    tempContentDescriptionDataType = mContext.getString(
                            R.string.accessibility_data_connection_3g);
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:

                    if (IS_CT) {
                    	tempDataIconList = TelephonyIconsGemini.DATA_4G_CT;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_4G_CT[simColorId];
                    } else {
                        tempDataIconList = TelephonyIconsGemini.DATA_4G;
                        tempDataTypeIconId = TelephonyIconsGemini.DATA_4G[simColorId];
                    }
                    tempContentDescriptionDataType = mContext.getString(
                            R.string.accessibility_data_connection_4g);
                    break;
                default:
                    if (!mShowAtLeastThreeGees) {
                    	tempDataNetType3G = DataNetType._G;
                        
                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_G_CT;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_G_CT[simColorId];
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_G;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_G[simColorId];
                        }
                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_gprs);
                    } else {
                    	tempDataNetType3G = DataNetType._3G;
                        
                        if (IS_CT) {
                        	tempDataIconList = TelephonyIconsGemini.DATA_3G_CT;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_3G_CT[simColorId];
                        } else {
                        	tempDataIconList = TelephonyIconsGemini.DATA_3G;
                            tempDataTypeIconId = TelephonyIconsGemini.DATA_3G[simColorId];
                        }
                        tempContentDescriptionDataType = mContext.getString(
                                R.string.accessibility_data_connection_3g);
                    }
                    break;
            }
        }
        if ((isCdma(slotId) && isCdmaEri(slotId)) || mPhone.isNetworkRoamingGemini(slotId)) {

            if (IS_CT) {
            	tempDataTypeIconId = TelephonyIconsGemini.ROAMING_CT[simColorId];
        	} else {
        		tempDataTypeIconId = TelephonyIconsGemini.ROAMING[simColorId];
        	}
	    if(slotId == Phone.GEMINI_SIM_1) {
            	mIsRoaming = true;
            } else {
            	mIsRoamingGemini = true;
            }
        } else {
        	if(slotId == Phone.GEMINI_SIM_1) {
            	mIsRoaming = false;
            } else {
            	mIsRoamingGemini = false;
            }
        }

        Xlog.d(TAG, "updateDataNetType(" + slotId + "), DataNetType3G=" + tempDataNetType3G 
        		+" tempDataTypeIconId= "+ tempDataTypeIconId + ".");
        if (slotId == Phone.GEMINI_SIM_1) {
            mDataNetType3G = tempDataNetType3G;
            mDataIconList = tempDataIconList;
            mDataTypeIconId = tempDataTypeIconId;
            mContentDescriptionDataType = tempContentDescriptionDataType;
        } else {
            mDataNetType3GGemini = tempDataNetType3G;
            mDataIconListGemini = tempDataIconList;
            mDataTypeIconIdGemini = tempDataTypeIconId;
            mContentDescriptionDataTypeGemini = tempContentDescriptionDataType;
        }
    }

    // [SystemUI] Support "Dual SIM".
    boolean isCdmaEri(int slotId) {
        ServiceState tempServiceState;
        if (slotId == Phone.GEMINI_SIM_1) {
            tempServiceState = mServiceState;
        } else {
            tempServiceState = mServiceStateGemini;
        }

        if (tempServiceState != null) {
            final int iconIndex = tempServiceState.getCdmaEriIconIndex();
            if (iconIndex != EriInfo.ROAMING_INDICATOR_OFF) {
                final int iconMode = tempServiceState.getCdmaEriIconMode();
                if (iconMode == EriInfo.ROAMING_ICON_MODE_NORMAL
                        || iconMode == EriInfo.ROAMING_ICON_MODE_FLASH) {
                    return true;
                }
            }
        }
        return false;
    }

    // [SystemUI] Support "Dual SIM".
    private final void updateDataIcon(int slotId) {
        int iconId = 0;
        boolean visible = true;
        TelephonyManager mTelephonyManager;
        int callState1;
        int callState2;
        DataNetType tempNetType3G = null;
        IccCard.State tempSimState;
        int tempDataState;
        int tempDataActivity;
        int[] tempDataIconList;
        if (slotId == Phone.GEMINI_SIM_1) {
            tempSimState = mSimState;
            tempDataState = mDataState;
            tempDataActivity = mDataActivity;
            tempDataIconList = mDataIconList;
            tempNetType3G = mDataNetType3G;
        } else {
            tempSimState = mSimStateGemini;
            tempDataState = mDataStateGemini;
            tempDataActivity = mDataActivityGemini;
            tempDataIconList = mDataIconListGemini;
            tempNetType3G = mDataNetType3GGemini;
        }

        Xlog.d(TAG, "updateDataIcon(" + slotId + "), SimState=" + tempSimState + ", DataState=" + tempDataState + 
        		", DataActivity=" + tempDataActivity+ ", tempNetType3G="+tempNetType3G);
        
        if (!isCdma(slotId)) {
            // GSM case, we have to check also the sim state
            if (tempSimState == IccCard.State.READY || tempSimState == IccCard.State.UNKNOWN) {
            	if (IS_CU && FeatureOption.MTK_DT_SUPPORT) {
            		
                mTelephonyManager = TelephonyManager.getDefault();
                    int callState = mTelephonyManager.getCallStateGemini(slotId);
                    Xlog.d(TAG, "updateDataIcon(" + slotId +"), Dual talk callState is " + callState +  ".");
                    
                    if (!(tempNetType3G == DataNetType._3G)) {
    	                if (hasService(slotId) && tempDataState == TelephonyManager.DATA_CONNECTED
	                		&& callState == TelephonyManager.CALL_STATE_IDLE
	                        && Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 1) {

                        int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                        Xlog.d(TAG, "updateDataIcon(" + slotId + "), simColorId=" + simColorId);
                        if (simColorId > -1) {
                        	iconId = tempDataIconList[simColorId];
                        }
                    
	                } else {
	                    iconId = 0;
	                    visible = false;
	                }
                } else {
                	if (hasService(slotId)
                            && tempDataState == TelephonyManager.DATA_CONNECTED
                            && Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 1) {


  	                        int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
  	                        Xlog.d(TAG, "updateDataIcon(" + slotId + "), simColorId=" + simColorId);
  	                        if (simColorId > -1) {
  	                        	iconId = tempDataIconList[simColorId];
  	                        }    	  	                        
                    } else {
	                    iconId = 0;
	                    visible = false;
                    }
            }
    		
    	} else {
            mTelephonyManager = TelephonyManager.getDefault();
                callState1 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_1);
                callState2 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_2);
                Xlog.d(TAG, "updateDataIcon(" + slotId +"), callState1 is " + callState1 + ", callState2 is " + callState2 + ".");
                
                if (!(tempNetType3G == DataNetType._3G)) {
	                if (hasService(slotId) && tempDataState == TelephonyManager.DATA_CONNECTED
	                		&& callState1 == TelephonyManager.CALL_STATE_IDLE
	                        && callState2 == TelephonyManager.CALL_STATE_IDLE
	                        && Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 1) {
                    if (IS_CU) {
                        switch (tempDataActivity) {
                            case TelephonyManager.DATA_ACTIVITY_IN:
                                iconId = tempDataIconList[1];
                                break;
                            case TelephonyManager.DATA_ACTIVITY_OUT:
                                iconId = tempDataIconList[2];
                                break;
                            case TelephonyManager.DATA_ACTIVITY_INOUT:
                                iconId = tempDataIconList[3];
                                break;
                            default:
                                iconId = tempDataIconList[0];
                                break;
                        }
                    } else {
                        int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                        Xlog.d(TAG, "updateDataIcon(" + slotId + "), simColorId=" + simColorId);
                        	iconId = tempDataIconList[simColorId];
                        }
	                } else {
	                    iconId = 0;
	                    visible = false;
	                }
                } else {
                    int none3GCallState = callState2;
                        final ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                        if (telephony != null) {
                            try {
                                if (telephony.get3GCapabilitySIM() == Phone.GEMINI_SIM_2) {
                                    none3GCallState = callState1;
                                }
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    if (hasService(slotId)
                            && tempDataState == TelephonyManager.DATA_CONNECTED
                            && Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 1) {

                	if (IS_CU) {
  	                        switch (tempDataActivity) {
  	                            case TelephonyManager.DATA_ACTIVITY_IN:
  	                                iconId = tempDataIconList[1];
  	                                break;
  	                            case TelephonyManager.DATA_ACTIVITY_OUT:
  	                                iconId = tempDataIconList[2];
  	                                break;
  	                            case TelephonyManager.DATA_ACTIVITY_INOUT:
  	                                iconId = tempDataIconList[3];
  	                                break;
  	                            default:
  	                                iconId = tempDataIconList[0];
  	                                break;
  	                        }
  	                    } else {
  	                        int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
  	                        Xlog.d(TAG, "updateDataIcon(" + slotId + "), simColorId=" + simColorId);
  	                        	iconId = tempDataIconList[simColorId];
  	                        }
                    } else {
	                    iconId = 0;
	                    visible = false;
                    }
                }
    	}
                
            } else {
                iconId = R.drawable.stat_sys_no_sim;
                visible = false; // no SIM? no data
            }
        } else {
            Xlog.d(TAG, "updateDataIcon(" + slotId + "), at cdma mode");
            // CDMA case, mDataActivity can be also DATA_ACTIVITY_DORMANT
            if (hasService(slotId) && tempDataState == TelephonyManager.DATA_CONNECTED) {

                int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                Xlog.d(TAG, "updateDataIcon(" + slotId + "), simColorId=" + simColorId);
                if (simColorId > -1) {
                	iconId = tempDataIconList[simColorId];
                }
                
            } else {
                iconId = 0;
                visible = false;
            }
        }

        // yuck - this should NOT be done by the status bar
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneDataConnectionState(mPhone.getNetworkTypeGemini(slotId), visible);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        Xlog.d(TAG, "updateDataIcon(" + slotId + "), iconId=" + iconId + ", visible=" + visible);
        if (slotId == Phone.GEMINI_SIM_1) {
            mDataDirectionIconId = iconId;
            mDataConnected = visible;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	//
            } else {
            if (mDataConnected) {
                mDataConnectedGemini = false;
            }
            }
        } else {
            mDataDirectionIconIdGemini = iconId;
            mDataConnectedGemini = visible;
            if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
            	//
            } else {
            if (mDataConnectedGemini) {
                mDataConnected = false;
            }
        }
    }
    }

    // [SystemUI] Support "Dual SIM".
    void updateNetworkName(int slotId, boolean showSpn, String spn, boolean showPlmn, String plmn) {
        Slog.d(TAG, "updateNetworkName(" + slotId + "), showSpn=" + showSpn + " spn=" + spn + " showPlmn=" + showPlmn + " plmn=" + plmn);

        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && plmn != null) {
            str.append(plmn);
            something = true;
        }
        if (showSpn && spn != null) {
            if (something) {
                str.append(mNetworkNameSeparator);
            }
            str.append(spn);
            something = true;
        }

        if (slotId == Phone.GEMINI_SIM_1) {
            if (something) {
                mNetworkName = str.toString();
            } else {
                mNetworkName = mNetworkNameDefault;
            }
            Slog.d(TAG, "updateNetworkName(" + slotId + "), mNetworkName=" + mNetworkName);
        } else {
            if (something) {
                mNetworkNameGemini = str.toString();
            } else {
                mNetworkNameGemini = mNetworkNameDefault;
            }
            Slog.d(TAG, "updateNetworkName(" + slotId + "), mNetworkNameGemini=" + mNetworkNameGemini);
        }
    }

    // ===== Wifi ===================================================================

    class WifiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        mWifiChannel.sendMessage(Message.obtain(this,
                                AsyncChannel.CMD_CHANNEL_FULL_CONNECTION));
                    } else {
                        Slog.e(TAG, "Failed to connect to wifi");
                    }
                    break;
                case WifiManager.DATA_ACTIVITY_NOTIFICATION:
                    if (msg.arg1 != mWifiActivity) {
                        mWifiActivity = msg.arg1;
                        refreshViews();
                    }
                    break;
                default:
                    //Ignore
                    break;
            }
        }
    }

    private void updateWifiState(Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
             final NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			mWifiConnected = networkInfo != null && networkInfo.isConnected();
			if (mWifiConnected) {
				WifiInfo wifiInfo = ((WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE))
						.getConnectionInfo();
				if (wifiInfo != null) {
					int newRssi = wifiInfo.getRssi();
					int newSignalLevel = WifiManager.calculateSignalLevel(
							newRssi, WifiIcons.WIFI_LEVEL_COUNT);
					Slog.d(TAG, "updateWifiState: mWifiLevel = " + mWifiLevel
							+ "  newRssi=" + newRssi + " newSignalLevel = "
							+ newSignalLevel);
					if (newSignalLevel != mWifiLevel) {
						mWifiLevel = newSignalLevel;
					}
				}
			}

        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            final NetworkInfo networkInfo = (NetworkInfo)
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean wasConnected = mWifiConnected;
            mWifiConnected = networkInfo != null && networkInfo.isConnected();
            // If we just connected, grab the inintial signal strength and ssid
            if (mWifiConnected && !wasConnected) {
                // try getting it out of the intent first
                WifiInfo info = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if (info == null) {
                    info = mWifiManager.getConnectionInfo();
                }
                if (info != null) {
                    mWifiSsid = huntForSsid(info);
                } else {
                    mWifiSsid = null;
                }
            } else if (!mWifiConnected) {
                mWifiSsid = null;
            }
            // Apparently the wifi level is not stable at this point even if we've just connected to
            // the network; we need to wait for an RSSI_CHANGED_ACTION for that. So let's just set
            // it to 0 for now
            if (mWifiConnected) {
				WifiInfo wifiInfo = ((WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE))
						.getConnectionInfo();
				if (wifiInfo != null) {
					int newRssi = wifiInfo.getRssi();
					int newSignalLevel = WifiManager.calculateSignalLevel(
							newRssi, WifiIcons.WIFI_LEVEL_COUNT);
					Slog.d(TAG, "updateWifiState: mWifiLevel = " + mWifiLevel
							+ "  newRssi=" + newRssi + " newSignalLevel = "
							+ newSignalLevel);
					if (newSignalLevel != mWifiLevel) {
						mWifiLevel = newSignalLevel;
					}
				}
			}
        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            if (mWifiConnected) {
                mWifiRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
                mWifiLevel = WifiManager.calculateSignalLevel(
                        mWifiRssi, WifiIcons.WIFI_LEVEL_COUNT);
            }
        }

        updateWifiIcons();
    }

    private void updateWifiIcons() {
        if (mWifiConnected) {
            mWifiIconId = WifiIcons.WIFI_SIGNAL_STRENGTH[mInetCondition][mWifiLevel];
            mContentDescriptionWifi = mContext.getString(
                    AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH[mWifiLevel]);
        } else {
            if (mDataAndWifiStacked) {
                mWifiIconId = 0;
            } else {
                mWifiIconId = mWifiEnabled ? WifiIcons.WIFI_SIGNAL_STRENGTH[0][0] : 0;
            }
            mContentDescriptionWifi = mContext.getString(R.string.accessibility_no_wifi);
        }
    }

    private String huntForSsid(WifiInfo info) {
        String ssid = info.getSSID();
        if (ssid != null) {
            return ssid;
        }
        // OK, it's not in the connectionInfo; we have to go hunting for it
        List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration net : networks) {
            if (net.networkId == info.getNetworkId()) {
                return net.SSID;
            }
        }
        return null;
    }

    // ===== Wimax ===================================================================
    private final void updateWimaxState(Intent intent) {
        final String action = intent.getAction();
        boolean wasConnected = mWimaxConnected;
        if (action.equals(WimaxManagerConstants.NET_4G_STATE_CHANGED_ACTION)) {
            int wimaxStatus = intent.getIntExtra(WimaxManagerConstants.EXTRA_4G_STATE,
                    WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            mIsWimaxEnabled = (wimaxStatus ==
                    WimaxManagerConstants.NET_4G_STATE_ENABLED);
        } else if (action.equals(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION)) {
            mWimaxSignal = intent.getIntExtra(WimaxManagerConstants.EXTRA_NEW_SIGNAL_LEVEL, 0);
        } else if (action.equals(WimaxManagerConstants.WIMAX_NETWORK_STATE_CHANGED_ACTION)) {
            mWimaxState = intent.getIntExtra(WimaxManagerConstants.EXTRA_WIMAX_STATE,
                    WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            mWimaxExtraState = intent.getIntExtra(
                    WimaxManagerConstants.EXTRA_WIMAX_STATE_DETAIL,
                    WimaxManagerConstants.NET_4G_STATE_UNKNOWN);
            mWimaxConnected = (mWimaxState ==
                    WimaxManagerConstants.WIMAX_STATE_CONNECTED);
            mWimaxIdle = (mWimaxExtraState == WimaxManagerConstants.WIMAX_IDLE);
        }
        updateDataNetType(Phone.GEMINI_SIM_1);
        updateWimaxIcons();
    }

    private void updateWimaxIcons() {
        if (mIsWimaxEnabled) {
            if (mWimaxConnected) {
                if (mWimaxIdle)
                    mWimaxIconId[0] = WimaxIcons.WIMAX_IDLE;
                else
                    mWimaxIconId[0] = WimaxIcons.WIMAX_SIGNAL_STRENGTH[mInetCondition][mWimaxSignal];
                mContentDescriptionWimax = mContext.getString(
                        AccessibilityContentDescriptions.WIMAX_CONNECTION_STRENGTH[mWimaxSignal]);
            } else {
                mWimaxIconId[0] = WimaxIcons.WIMAX_DISCONNECTED;
                mContentDescriptionWimax = mContext.getString(R.string.accessibility_no_wimax);
            }
        } else {
            mWimaxIconId[0] = 0;
        }
    }


    // ===== Full or limited Internet connectivity ==================================

    private void updateConnectivity(Intent intent) {
        if (CHATTY) {
            Slog.d(TAG, "updateConnectivity: intent=" + intent);
        }

        NetworkInfo info = (NetworkInfo)(intent.getParcelableExtra(
                ConnectivityManager.EXTRA_NETWORK_INFO));
        int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);

        if (CHATTY) {
            Slog.d(TAG, "updateConnectivity: networkInfo=" + info);
            Slog.d(TAG, "updateConnectivity: connectionStatus=" + connectionStatus);
        }

        mInetCondition = (connectionStatus > INET_CONDITION_THRESHOLD ? 1 : 0);
        Xlog.d(TAG, "updateConnectivity, mInetCondition=" + mInetCondition);

        if (info != null && info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
            mBluetoothTethered = info.isConnected();
        } else {
            mBluetoothTethered = false;
        }

        // [SystemUI] Support "Dual SIM". {
        // We want to update all the icons, all at once, for any condition change
        int slotId = intent.getIntExtra(ConnectivityManager.EXTRA_SIM_ID, Phone.GEMINI_SIM_1);
        updateDataNetType(slotId);
        updateWimaxIcons();
        updateDataIcon(slotId);
        updateTelephonySignalStrength(slotId);
        // [SystemUI] Support "Dual SIM". {
        updateWifiIcons();
    }


    // ===== Update the views =======================================================

    void refreshViews() {
        // [SystemUI] Support "Dual SIM". {
        refreshViews(Phone.GEMINI_SIM_1);
        refreshViews(Phone.GEMINI_SIM_2);
        // [SystemUI] Support "Dual SIM". }
    }

    // [SystemUI] Support "Dual SIM".
    void refreshViews(int slotId) {
        Context context = mContext;

        int combinedSignalIconId = 0;
        int combinedActivityIconId = 0;
        String combinedLabel = "";
        String wifiLabel = "";
        String mobileLabel = "";
        int N;

        boolean tempDataConnected;
        DataNetType tempDataNetType3G;
        String tempNetworkName;
        ServiceState tempServiceState;
        SignalStrength tempSignalStrength;
        int tempDataSignalIconId;
        int tempPhoneSignalIconId[] = {0,0};
        int tempDataActivity;
        String tempContentDescriptionPhoneSignal = "";
        String tempContentDescriptionDataType = "";
        String tempContentDescriptionCombinedSignal = "";        
        if (slotId == Phone.GEMINI_SIM_1) {
            tempDataConnected = mDataConnected;
            tempDataNetType3G = mDataNetType3G;
            tempNetworkName = mNetworkName;
            tempServiceState = mServiceState;
            tempSignalStrength = mSignalStrength;
            tempDataActivity = mDataActivity;
            tempDataSignalIconId = mDataSignalIconId;
            tempPhoneSignalIconId = mPhoneSignalIconId;
            tempContentDescriptionPhoneSignal = mContentDescriptionPhoneSignal;
            tempContentDescriptionDataType = mContentDescriptionDataType;
        } else {
            tempDataConnected = mDataConnectedGemini;
            tempDataNetType3G = mDataNetType3GGemini;
            tempNetworkName = mNetworkNameGemini;
            tempServiceState = mServiceStateGemini;
            tempSignalStrength = mSignalStrengthGemini;
            tempDataActivity = mDataActivityGemini;
            tempDataSignalIconId = mDataSignalIconIdGemini;
            tempPhoneSignalIconId = mPhoneSignalIconIdGemini;
            tempContentDescriptionPhoneSignal = mContentDescriptionPhoneSignalGemini;
            tempContentDescriptionDataType = mContentDescriptionDataTypeGemini;                   
        }

        if (!mHasMobileDataFeature) {
        	tempDataSignalIconId = tempPhoneSignalIconId[0] = tempPhoneSignalIconId[1] = 0;
            mobileLabel = "";
        } else {
            // We want to show the carrier name if in service and either:
            //   - We are connected to mobile data, or
            //   - We are not connected to mobile data, as long as the *reason* packets are not
            //     being routed over that link is that we have better connectivity via wifi.
            // If data is disconnected for some other reason but wifi is connected, we show nothing.
            // Otherwise (nothing connected) we show "No internet connection".

           if (!sIsScreenLarge) {
            if (mDataConnected) {
                mobileLabel = tempNetworkName;
            } else if (mWifiConnected) {
                if (hasService(slotId)) {
                    mobileLabel = tempNetworkName;
                } else {
                    mobileLabel = "";
                }
            } else {
                mobileLabel
                    = context.getString(R.string.status_bar_settings_signal_meter_disconnected);
             }
           } else {             
               if (hasService(slotId)) {
			   	 mobileLabel = tempNetworkName;
               } else {
                 mobileLabel = "";
               }             
            }
            
        
        Xlog.d(TAG, "refreshViews(" + slotId + "), DataConnected=" + tempDataConnected);
        
        if (tempDataConnected) {
            combinedSignalIconId = tempDataSignalIconId;
            int tempMobileActivityIconId;

            switch (tempDataActivity) {
            case TelephonyManager.DATA_ACTIVITY_IN:
                if (IS_CT) {
                	tempMobileActivityIconId = R.drawable.stat_sys_signal_in_ct;
                } else {
                    tempMobileActivityIconId = R.drawable.stat_sys_signal_in;
                }
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                if (IS_CT) {
                	tempMobileActivityIconId = R.drawable.stat_sys_signal_out_ct;
                } else {
                    tempMobileActivityIconId = R.drawable.stat_sys_signal_out;
                }
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                if (IS_CT) {
                	tempMobileActivityIconId = R.drawable.stat_sys_signal_inout_ct;
                } else {
                    tempMobileActivityIconId = R.drawable.stat_sys_signal_inout;
                }
                break;
            default:
                tempMobileActivityIconId = 0;
                break;
            }
            
            combinedLabel = mobileLabel;
            combinedActivityIconId = tempMobileActivityIconId;
            combinedSignalIconId = tempDataSignalIconId; // set by updateDataIcon()
            tempContentDescriptionCombinedSignal = tempContentDescriptionDataType;

            if (slotId == Phone.GEMINI_SIM_1) {
                mMobileActivityIconId = tempMobileActivityIconId;
                if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
                	//
                } else {
                mMobileActivityIconIdGemini = -1;
                }
            } else {
                mMobileActivityIconIdGemini = tempMobileActivityIconId;
                if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
                	//
                } else {
                mMobileActivityIconId = -1;
            }
            }
            Xlog.d(TAG, "refreshViews(" + slotId + "), mMobileActivityIconId=" + mMobileActivityIconId + ", mMobileActivityIconIdGemini=" + mMobileActivityIconIdGemini);
		} else {
			    
			if (slotId == Phone.GEMINI_SIM_1) {
                combinedActivityIconId = 0;
				mMobileActivityIconId = 0;
				 if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
	                	//
	                } else {
				mMobileActivityIconIdGemini = -1;
	                }
			} else {
                combinedActivityIconId = -1;
				mMobileActivityIconIdGemini = 0;
				 if(IS_CU && FeatureOption.MTK_DT_SUPPORT){
	                	//
	                } else {
				mMobileActivityIconId = -1;
			}
        }
        }
        }
        
        if (mWifiConnected) {
            if (mWifiSsid == null) {
            	wifiLabel = context.getString(R.string.status_bar_settings_signal_meter_wifi_nossid);
                mWifiActivityIconId = 0; // no wifis, no bits
            } else {
            	wifiLabel = mWifiSsid;
                if (DEBUG) {
                    wifiLabel += "xxxxXXXXxxxxXXXX";
                }
                switch (mWifiActivity) {
                    case WifiManager.DATA_ACTIVITY_IN:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_in;
                        break;
                    case WifiManager.DATA_ACTIVITY_OUT:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_out;
                        break;
                    case WifiManager.DATA_ACTIVITY_INOUT:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_inout;
                        break;
                    case WifiManager.DATA_ACTIVITY_NONE:
                        mWifiActivityIconId = 0;
                        break;
                }
            }
            combinedLabel = wifiLabel;
            combinedActivityIconId = mWifiActivityIconId;
            combinedSignalIconId = mWifiIconId; // set by updateWifiIcons()
            tempContentDescriptionCombinedSignal = mContentDescriptionWifi;
        } else {
            if (mHasMobileDataFeature) {
                wifiLabel = "";
            } else {
                wifiLabel = context.getString(R.string.status_bar_settings_signal_meter_disconnected);
            }
        }

        if (mBluetoothTethered) {
        	combinedLabel = mContext.getString(R.string.bluetooth_tethered);
            combinedSignalIconId = mBluetoothTetherIconId;
            tempContentDescriptionCombinedSignal = mContext.getString(
                    R.string.accessibility_bluetooth_tether);
        }

        if (mAirplaneMode &&
                (tempServiceState == null || (!hasService(slotId) && !tempServiceState.isEmergencyOnly()))) {
            // Only display the flight-mode icon if not in "emergency calls only" mode.

            // look again; your radios are now airplanes
            Xlog.d(TAG, "refreshViews(" + slotId + "), AirplaneMode=" + mAirplaneMode);
            tempContentDescriptionPhoneSignal = mContext.getString(R.string.accessibility_airplane_mode);
            if (this.isSimInserted(slotId)) {
                if (slotId == Phone.GEMINI_SIM_1) {
	                    int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
	                    Xlog.d(TAG, "refreshViews(" + slotId + "), simColorId=" + simColorId);
	                    if (simColorId > -1){
	                    	mPhoneSignalIconId[0] = mDataSignalIconId = TelephonyIconsGemini.FLIGHT_MODE[simColorId];
	                    }	               
		                mDataTypeIconId = 0;
		                tempDataSignalIconId = mDataSignalIconId;
	                } else {
	                    int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
	                    Xlog.d(TAG, "refreshViews(" + slotId + "), simColorId=" + simColorId);
	                    if (simColorId > -1){
	                    	mPhoneSignalIconIdGemini[0] = mDataSignalIconIdGemini = TelephonyIconsGemini.FLIGHT_MODE[simColorId];
	                    }
	                
	                    mDataTypeIconIdGemini = 0;
	                    tempDataSignalIconId = mDataSignalIconIdGemini;
                }
            }
            
            // combined values from connected wifi take precedence over airplane mode
            if (mWifiConnected) {
                // Suppress "No internet connection." from mobile if wifi connected.
                mobileLabel = "";
            } else {
                if (mHasMobileDataFeature) {
                    // let the mobile icon show "No internet connection."
                    wifiLabel = "";
                } else {
                    wifiLabel = context.getString(R.string.status_bar_settings_signal_meter_disconnected);
                    combinedLabel = wifiLabel;
                }
                tempContentDescriptionCombinedSignal = tempContentDescriptionPhoneSignal;
                combinedSignalIconId = tempDataSignalIconId;
            }
            
        }
        else if (!tempDataConnected && !mWifiConnected && !mBluetoothTethered && !mWimaxConnected) {
            // pretty much totally disconnected

        	combinedLabel = context.getString(R.string.status_bar_settings_signal_meter_disconnected);
            // On devices without mobile radios, we want to show the wifi icon
            if (!sIsScreenLarge)
            {
              combinedSignalIconId =
                  mHasMobileDataFeature ? tempDataSignalIconId : mWifiIconId;
              tempContentDescriptionCombinedSignal = mHasMobileDataFeature
                  ? tempContentDescriptionDataType : mContentDescriptionWifi;
            }
            else
            {
            if (mHasMobileDataFeature == false)
            {
                combinedSignalIconId = mWifiIconId;
                tempContentDescriptionCombinedSignal = mContentDescriptionWifi;
            }
            else
            {
                 if ((slotId == Phone.GEMINI_SIM_2) && (mDataConnected == true))
                 {
                	 combinedLabel = mNetworkName;
                 	 combinedSignalIconId = mDataSignalIconId;
                    tempContentDescriptionCombinedSignal = mContentDescriptionDataType;
                 }
                 else
                 if ((slotId == Phone.GEMINI_SIM_1) && (mDataConnectedGemini == true))
                 {
                	 combinedLabel = mNetworkNameGemini;
                    combinedSignalIconId = mDataSignalIconIdGemini;
                    tempContentDescriptionCombinedSignal = mContentDescriptionDataTypeGemini;
                 }
                 else
                 {
                 	combinedSignalIconId = mWifiIconId;
                 	tempContentDescriptionCombinedSignal = tempContentDescriptionDataType;
                 }
            }
            }

            IccCard.State tempSimState;
            int cmccDataTypeIconId;
            if (slotId == Phone.GEMINI_SIM_1) {
                tempSimState = mSimState;
                cmccDataTypeIconId = mDataTypeIconId;
            } else {
                tempSimState = mSimStateGemini;
                cmccDataTypeIconId = mDataTypeIconIdGemini;
            }

            int dataTypeIconId= 0;
            if ((isCdma(slotId) && isCdmaEri(slotId)) || mPhone.isNetworkRoamingGemini(slotId)) {

                int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, slotId);
                Xlog.d(TAG, "refreshViews(" + slotId + "), simColorId=" + simColorId);
                if (simColorId > -1) {
                	if (IS_CT) {
                		dataTypeIconId = TelephonyIconsGemini.ROAMING_CT[simColorId];
                	} else {
                    	dataTypeIconId = TelephonyIconsGemini.ROAMING[simColorId];
                	}
                }
                if(slotId == Phone.GEMINI_SIM_1) {
                    	mIsRoaming = true;
                    } else {
                    	mIsRoamingGemini = true;
                    }
                
            } else {
            	if(slotId == Phone.GEMINI_SIM_1) {
                	mIsRoaming = false;
                } else {
                	mIsRoamingGemini = false;
                }
                dataTypeIconId = 0;
            }
            Xlog.d(TAG, "refreshViews(" + slotId + "), dataTypeIconId=" + dataTypeIconId);
            if (slotId == Phone.GEMINI_SIM_1) {
                mDataTypeIconId = dataTypeIconId;
            } else {
                mDataTypeIconIdGemini = dataTypeIconId;
            }

            if ((IS_CMCC||FeatureOption.MTK_NETWORK_TYPE_ALWAYS_ON)
                    && hasService(slotId)
                    && (tempSimState == IccCard.State.READY || tempSimState == IccCard.State.UNKNOWN)) {
                Xlog.d(TAG, "refreshViews(" + slotId + "), SimState=" + tempSimState + ", mDataTypeIconId=" + cmccDataTypeIconId);
                if (slotId == Phone.GEMINI_SIM_1) {
                    mDataTypeIconId = cmccDataTypeIconId;
                } else {
                    mDataTypeIconIdGemini = cmccDataTypeIconId;
                }
        	}
		}

        //int tempPhoneSignalIconId;
        int tempDataDirectionIconId;
        int tempDataTypeIconId;
        int tempMobileActivityIconId;        
        if (slotId == Phone.GEMINI_SIM_1) {
            tempPhoneSignalIconId = mPhoneSignalIconId;
            tempDataDirectionIconId = mDataDirectionIconId;
            tempDataTypeIconId = mDataTypeIconId;
            tempMobileActivityIconId = mMobileActivityIconId;
            mContentDescriptionCombinedSignal = tempContentDescriptionCombinedSignal;
        } else {
            tempPhoneSignalIconId = mPhoneSignalIconIdGemini;
            tempDataDirectionIconId = mDataDirectionIconIdGemini;
            tempDataTypeIconId = mDataTypeIconIdGemini;
            tempMobileActivityIconId = mMobileActivityIconIdGemini;
            mContentDescriptionCombinedSignalGemini = tempContentDescriptionCombinedSignal;
        }

        if (DEBUG) {
            Slog.d(TAG, "refreshViews connected={"
                    + (mWifiConnected?" wifi":"")
                    + (tempDataConnected?" data":"")
                    + " } level="
                    + ((tempSignalStrength == null)?"??":Integer.toString(tempSignalStrength.getLevel()))
                    + " combinedSignalIconId=0x"
                    + Integer.toHexString(combinedSignalIconId)
                    + "/" + getResourceName(combinedSignalIconId)
                    + " combinedActivityIconId=0x" + Integer.toHexString(combinedActivityIconId)
                    + " mAirplaneMode=" + mAirplaneMode
                    + " mDataActivity=" + tempDataActivity
                    + " mPhoneSignalIconId=0x" + Integer.toHexString(tempPhoneSignalIconId[0])
                    + " mPhoneSignalIconId2=0x" + Integer.toHexString(tempPhoneSignalIconId[1])
                    + " mDataDirectionIconId=0x" + Integer.toHexString(tempDataDirectionIconId)
                    + " mDataSignalIconId=0x" + Integer.toHexString(tempDataSignalIconId)
                    + " mDataTypeIconId=0x" + Integer.toHexString(tempDataTypeIconId)
                    + " mWifiIconId=0x" + Integer.toHexString(mWifiIconId)
                    + " mBluetoothTetherIconId=0x" + Integer.toHexString(mBluetoothTetherIconId));
        }

        boolean tempMobileVisible;
        boolean tempLastMobileVisible;
        int tempSIMBackground;
        int tempLastSIMBackground;
        int tempLastPhoneSignalIconId[];
        int tempLastDataTypeIconId;
        if (slotId == Phone.GEMINI_SIM_1) {
            tempMobileVisible = mMobileVisible;
            tempLastMobileVisible = mLastMobileVisible;
            tempSIMBackground = mSIMBackground;
            tempLastSIMBackground = mLastSIMBackground;
            tempLastPhoneSignalIconId = mLastPhoneSignalIconId;
            tempLastDataTypeIconId= mLastDataTypeIconId;
        } else {
            tempMobileVisible = mMobileVisibleGemini;
            tempLastMobileVisible = mLastMobileVisibleGemini;
            tempSIMBackground = mSIMBackgroundGemini;
            tempLastSIMBackground = mLastSIMBackgroundGemini;
            tempLastPhoneSignalIconId = mLastPhoneSignalIconIdGemini;
            tempLastDataTypeIconId= mLastDataTypeIconIdGemini;
        }

        if (tempLastMobileVisible           != tempMobileVisible
         || tempLastSIMBackground           != tempSIMBackground
         || tempLastPhoneSignalIconId[0]    != tempPhoneSignalIconId[0]
         || tempLastPhoneSignalIconId[1]    != tempPhoneSignalIconId[1]
         || mLastDataDirectionOverlayIconId != combinedActivityIconId
         || mLastWifiIconId                 != mWifiIconId
         || mLastWimaxIconId                != mWimaxIconId[0]
         || tempLastDataTypeIconId             != tempDataTypeIconId )
        {
            Xlog.d(TAG, "refreshViews(" + slotId + "), set parameters to signal cluster view.");
            // NB: the mLast*s will be updated later
            for (SignalCluster cluster : mSignalClusters) {
                cluster.setWifiIndicators(
                        mWifiConnected, // only show wifi in the cluster if connected
                        mWifiIconId,
                        mWifiActivityIconId,
                        mContentDescriptionWifi);
                if (tempSIMBackground > 0) {
                    cluster.setSIMBackground(slotId, tempSIMBackground);
                }
//                if(IS_CU){
//                	Xlog.d("SIMCUSign", "refreshViews(" + slotId + ") mSIMCUSignVisible= "+mSIMCUSignVisible);
//                    cluster.setSIMState(slotId, mSIMCUSignVisible);
//                    
//                }
  
                Xlog.d(TAG, "refreshViews(" + slotId + "), tempPhoneSignalIconId0 = "+tempPhoneSignalIconId[0]
                        +"  tempPhoneSignalIconId1 = "+tempPhoneSignalIconId[1]                                                                                    
                		+"  tempMobileActivityIconId= "+tempMobileActivityIconId
                		+"  tempDataTypeIconId= "+tempDataTypeIconId);
//                Xlog.d("NullSignal", "refreshViews(" + slotId + "), mHasMobileDataFeature= "+mHasMobileDataFeature
//                		+"  tempMobileVisible= "+tempMobileVisible);
                cluster.setMobileDataIndicators(
                        slotId,
                        mHasMobileDataFeature && tempMobileVisible,
                        tempPhoneSignalIconId,
                        tempMobileActivityIconId,
                        tempDataTypeIconId,
                        tempContentDescriptionPhoneSignal,
                        tempContentDescriptionDataType);
                cluster.setIsAirplaneMode(mAirplaneMode);
                // refreshSignalCluster(cluster);
            }
        }
        for (SignalCluster cluster : mSignalClusters) {
        	cluster.setRoamingFlag(mIsRoaming, mIsRoamingGemini);
            cluster.setDataConnected(slotId, tempDataConnected);
            cluster.setDataNetType3G(slotId, tempDataNetType3G);
        }
        
        //for cluster apply
        for (SignalCluster cluster : mSignalClusters) {
            cluster.apply();
        }

        if (tempLastMobileVisible  != tempMobileVisible) {
            if (slotId == Phone.GEMINI_SIM_1) {
                mLastMobileVisible = tempMobileVisible;
            } else {
                mLastMobileVisibleGemini = tempMobileVisible;
            }
        }

        if (tempLastSIMBackground != tempSIMBackground) {
            if (slotId == Phone.GEMINI_SIM_1) {
                mLastSIMBackground = tempSIMBackground;
            } else {
                mLastSIMBackgroundGemini = tempSIMBackground;
            }
        }

        // the phone icon on phones
        if (!sIsScreenLarge)
		{
        if (tempLastPhoneSignalIconId != tempPhoneSignalIconId) {
            if (slotId == Phone.GEMINI_SIM_1) {
                mLastPhoneSignalIconId = tempPhoneSignalIconId;
            } else {
                mLastPhoneSignalIconIdGemini = tempPhoneSignalIconId;
            }
            N = mPhoneSignalIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mPhoneSignalIconViews.get(i);
                if (tempPhoneSignalIconId[0] == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
	                v.setImageResource(tempPhoneSignalIconId[0]);
	                v.setContentDescription(tempContentDescriptionPhoneSignal);
            	}
        	}
        }
		}
		else
		{			
			if (tempLastPhoneSignalIconId != tempPhoneSignalIconId) {
				final ImageView v;
        	if (slotId == Phone.GEMINI_SIM_1) {
                	mLastPhoneSignalIconId = tempPhoneSignalIconId;
					v = mPhoneSignalIconViews.get(0);
            	} else {
                	mLastPhoneSignalIconIdGemini = tempPhoneSignalIconId;
					v = mPhoneSignalIconViews.get(1);
            	}

				  if (v != null) {
				    if ((tempPhoneSignalIconId[0] == 0) || (!hasService(slotId))) {
                	  v.setVisibility(View.GONE);
	                } else {
    	              v.setVisibility(View.VISIBLE);
        	          v.setImageResource(tempPhoneSignalIconId[0]);
            	      v.setContentDescription(tempContentDescriptionPhoneSignal);
	               }							 	     
        	}            	
    	    }
	}

        // the data icon on phones
        if (mLastDataDirectionIconId != tempDataDirectionIconId) {
            mLastDataDirectionIconId = tempDataDirectionIconId;
            N = mDataDirectionIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataDirectionIconViews.get(i);
                if(tempDataDirectionIconId == 0){
                   if (!sIsScreenLarge)
                      v.setVisibility(View.INVISIBLE);
                   else
                      v.setVisibility(View.GONE);
                } else {
                   v.setVisibility(View.VISIBLE);
                   v.setImageResource(tempDataDirectionIconId);
                   v.setContentDescription(tempContentDescriptionDataType);
                }
            }
       }
        // the wifi icon on phones
        if (mLastWifiIconId != mWifiIconId) {
            mLastWifiIconId = mWifiIconId;
            N = mWifiIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mWifiIconViews.get(i);
                if (mWifiIconId == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mWifiIconId);
                    v.setContentDescription(mContentDescriptionWifi);
                }
            }
        }

        // the wimax icon on phones
        if (mLastWimaxIconId != mWimaxIconId[0]) {
            mLastWimaxIconId = mWimaxIconId[0];
            N = mWimaxIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mWimaxIconViews.get(i);
                if (mWimaxIconId[0] == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mWimaxIconId[0]);
                    v.setContentDescription(mContentDescriptionWimax);
                }
           }
        }
        // the combined data signal icon
        if (mLastCombinedSignalIconId != combinedSignalIconId) {
            mLastCombinedSignalIconId = combinedSignalIconId;
            N = mCombinedSignalIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mCombinedSignalIconViews.get(i);
                if (!sIsScreenLarge)
                {
                  v.setImageResource(combinedSignalIconId);
                  v.setContentDescription(tempContentDescriptionCombinedSignal);
                }
                else
                {
                  if ( (mWifiConnected ==true) || (mDataConnected == true)|| (mDataConnectedGemini == true))
            	  {
            	     v.setVisibility(View.VISIBLE);
                     v.setImageResource(combinedSignalIconId);
                     v.setContentDescription(tempContentDescriptionCombinedSignal);
            	  }
            	  else
            	  {
               	   v.setVisibility(View.GONE);            	  
            	  }
                }
            }
        }

        // the data network type overlay
		if (!sIsScreenLarge)
		{
        if ((tempLastDataTypeIconId != tempDataTypeIconId) ||(mWifiConnected ==true && sIsScreenLarge)) {
            //mLastDataTypeIconId = tempDataTypeIconId;
            if (slotId == Phone.GEMINI_SIM_1) {
            	mLastDataTypeIconId = tempDataTypeIconId;
            } else {
            	mLastDataTypeIconIdGemini = tempDataTypeIconId;
            }
            N = mDataTypeIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataTypeIconViews.get(i);
                if ((tempDataTypeIconId == 0) && (sIsScreenLarge==false) ) {
                    v.setVisibility(View.GONE); 
                } else if ((sIsScreenLarge==true) && ((tempDataTypeIconId == 0) || (mWifiConnected ==true))) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(tempDataTypeIconId);
                    v.setContentDescription(tempContentDescriptionDataType);
                }
            }
        }
		} else {
	            //mLastDataTypeIconId = tempDataTypeIconId;
            final ImageView v;
    	        if (slotId == Phone.GEMINI_SIM_1) {
            	mLastDataTypeIconId = tempDataTypeIconId;
				v = mDataTypeIconViews.get(0);
            	    } else {
    	       	mLastDataTypeIconIdGemini = tempDataTypeIconId;
				v = mDataTypeIconViews.get(1);
        	        }
			
    	    if ((tempLastDataTypeIconId != tempDataTypeIconId) ||(mWifiConnected ==true && sIsScreenLarge)) {
				if ((tempDataTypeIconId == 0) && (sIsScreenLarge==false) ) {
    	                v.setVisibility(View.GONE); 
                } else if ((sIsScreenLarge==true) && ((tempDataTypeIconId == 0) || (mWifiConnected ==true))) {
                	    v.setVisibility(View.GONE);					
	                } else {
    	                v.setVisibility(View.VISIBLE);
					v.setImageResource(tempDataTypeIconId);
        	            v.setContentDescription(tempContentDescriptionDataType);
	            }				        
        	}
		}

        // the data direction overlay
        if (mLastDataDirectionOverlayIconId != combinedActivityIconId) {
            if (DEBUG) {
                Slog.d(TAG, "changing data overlay icon id to " + combinedActivityIconId);
            }
            mLastDataDirectionOverlayIconId = combinedActivityIconId;
            N = mDataDirectionOverlayIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataDirectionOverlayIconViews.get(i);
                if (combinedActivityIconId == 0) {
                    if (!sIsScreenLarge)
                       v.setVisibility(View.INVISIBLE);
                    else
                       v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(combinedActivityIconId);
                    v.setContentDescription(tempContentDescriptionDataType);
                }
            }
        }
       
        // the combinedLabel in the notification panel
        if (!mLastCombinedLabel.equals(combinedLabel)) {
            mLastCombinedLabel = combinedLabel;
            N = mCombinedLabelViews.size();
            for (int i=0; i<N; i++) {
                TextView v = mCombinedLabelViews.get(i);
                v.setText(combinedLabel);
            }
        }

        // wifi label
        N = mWifiLabelViews.size();
        for (int i=0; i<N; i++) {
            TextView v = mWifiLabelViews.get(i);
            if ("".equals(wifiLabel)) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
                v.setText(wifiLabel);
            }
        }

        // mobile label
		if (!sIsScreenLarge)
		{
        N = mMobileLabelViews.size();
        for (int i=0; i<N; i++) {
            TextView v = mMobileLabelViews.get(i);
            if ("".equals(mobileLabel)) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
                v.setText(mobileLabel);
            }
        }
		} else {			
			TextView v;
    	    if (slotId == Phone.GEMINI_SIM_1) {
				v = mMobileLabelViews.get(0);
			} else {
    	        v = mMobileLabelViews.get(1);
			}
			
			if (v != null)
			{
    	        if ("".equals(mobileLabel)) {
        	        v.setVisibility(View.GONE);
            	} else {
                	v.setVisibility(View.VISIBLE);
	                v.setText(mobileLabel);
    	        }
        	}
		}
        
    }

    // [SystemUI] Support "Dual SIM".
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NetworkControllerGemini state:");
        pw.println("  - telephony ------");
        pw.print("  hasService(1)=");
        pw.println(hasService(Phone.GEMINI_SIM_1));
        pw.print("  hasService(2)=");
        pw.println(hasService(Phone.GEMINI_SIM_2));
        pw.print("  mHspaDataDistinguishable=");
        pw.println(mHspaDataDistinguishable);
        pw.print("  mDataConnected=");
        pw.println(mDataConnected);
        pw.print("  mDataConnectedGemini=");
        pw.println(mDataConnectedGemini);
        pw.print("  mSimState=");
        pw.println(mSimState);
        pw.print("  mSimStateGemini=");
        pw.println(mSimStateGemini);
        pw.print("  mPhoneState=");
        pw.println(mPhoneState);
        pw.print("  mDataState=");
        pw.println(mDataState);
        pw.print("  mDataStateGemini=");
        pw.println(mDataStateGemini);
        pw.print("  mDataActivity=");
        pw.println(mDataActivity);
        pw.print("  mDataActivityGemini=");
        pw.println(mDataActivityGemini);
        pw.print("  mDataNetType=");
        pw.print(mDataNetType);
        pw.print("/");
        pw.println(TelephonyManager.getNetworkTypeName(mDataNetType));
        pw.print("  mDataNetTypeGemini=");
        pw.print(mDataNetTypeGemini);
        pw.print("/");
        pw.println(TelephonyManager.getNetworkTypeName(mDataNetTypeGemini));
        pw.print("  mServiceState=");
        pw.println(mServiceState);
        pw.print("  mServiceStateGemini=");
        pw.println(mServiceStateGemini);
        pw.print("  mSignalStrength=");
        pw.println(mSignalStrength);
        pw.print("  mSignalStrengthGemini=");
        pw.println(mSignalStrengthGemini);
        pw.print("  mLastSignalLevel=");
        pw.println(mLastSignalLevel);
        pw.print("  mLastSignalLevelGemini=");
        pw.println(mLastSignalLevelGemini);
        pw.print("  mNetworkName=");
        pw.println(mNetworkName);
        pw.print("  mNetworkNameGemini=");
        pw.println(mNetworkNameGemini);
        pw.print("  mNetworkNameDefault=");
        pw.println(mNetworkNameDefault);
        pw.print("  mNetworkNameSeparator=");
        pw.println(mNetworkNameSeparator.replace("\n","\\n"));
        pw.print("  mPhoneSignalIconId=0x");
        pw.print(Integer.toHexString(mPhoneSignalIconId[0]));
        pw.print("/");
        pw.println(getResourceName(mPhoneSignalIconId[0]));
        pw.print("  mPhoneSignalIconIdGemini=0x");
        pw.print(Integer.toHexString(mPhoneSignalIconIdGemini[0]));
        pw.print("/");
        pw.println(getResourceName(mPhoneSignalIconIdGemini[0]));
        pw.print("  mDataDirectionIconId=");
        pw.print(Integer.toHexString(mDataDirectionIconId));
        pw.print("/");
        pw.println(getResourceName(mDataDirectionIconId));
        pw.print("  mDataDirectionIconIdGemini=");
        pw.print(Integer.toHexString(mDataDirectionIconIdGemini));
        pw.print("/");
        pw.println(getResourceName(mDataDirectionIconIdGemini));
        pw.print("  mDataSignalIconId=");
        pw.print(Integer.toHexString(mDataSignalIconId));
        pw.print("/");
        pw.println(getResourceName(mDataSignalIconId));
        pw.print("  mDataSignalIconIdGemini=");
        pw.print(Integer.toHexString(mDataSignalIconIdGemini));
        pw.print("/");
        pw.println(getResourceName(mDataSignalIconIdGemini));
        pw.print("  mDataTypeIconId=");
        pw.print(Integer.toHexString(mDataTypeIconId));
        pw.print("/");
        pw.println(getResourceName(mDataTypeIconId));
        pw.print("  mDataTypeIconIdGemini=");
        pw.print(Integer.toHexString(mDataTypeIconIdGemini));
        pw.print("/");
        pw.println(getResourceName(mDataTypeIconIdGemini));

        pw.println("  - wifi ------");
        pw.print("  mWifiEnabled=");
        pw.println(mWifiEnabled);
        pw.print("  mWifiConnected=");
        pw.println(mWifiConnected);
        pw.print("  mWifiRssi=");
        pw.println(mWifiRssi);
        pw.print("  mWifiLevel=");
        pw.println(mWifiLevel);
        pw.print("  mWifiSsid=");
        pw.println(mWifiSsid);
        pw.println(String.format("  mWifiIconId=0x%08x/%s",
                    mWifiIconId, getResourceName(mWifiIconId)));
        pw.print("  mWifiActivity=");
        pw.println(mWifiActivity);

        if (mWimaxSupported) {
            pw.println("  - wimax ------");
            pw.print("  mIsWimaxEnabled="); pw.println(mIsWimaxEnabled);
            pw.print("  mWimaxConnected="); pw.println(mWimaxConnected);
            pw.print("  mWimaxIdle="); pw.println(mWimaxIdle);
            pw.println(String.format("  mWimaxIconId=0x%08x/%s",
                        mWimaxIconId[0], getResourceName(mWimaxIconId[0])));
            pw.println(String.format("  mWimaxSignal=%d", mWimaxSignal));
            pw.println(String.format("  mWimaxState=%d", mWimaxState));
            pw.println(String.format("  mWimaxExtraState=%d", mWimaxExtraState));
        }

        pw.println("  - Bluetooth ----");
        pw.print("  mBtReverseTethered=");
        pw.println(mBluetoothTethered);

        pw.println("  - connectivity ------");
        pw.print("  mInetCondition=");
        pw.println(mInetCondition);

        pw.println("  - icons ------");
        pw.print("  mLastPhoneSignalIconId=0x");
        pw.print(Integer.toHexString(mLastPhoneSignalIconId[0]));
        pw.print("/");
        pw.println(getResourceName(mLastPhoneSignalIconId[0]));
        pw.print("  mLastPhoneSignalIconId1=0x");
        pw.print(Integer.toHexString(mLastPhoneSignalIconId[1]));
        pw.print("/");
        pw.println(getResourceName(mLastPhoneSignalIconId[1]));
        pw.print("  mLastPhoneSignalIconIdGemini=0x");
        pw.print(Integer.toHexString(mLastPhoneSignalIconIdGemini[0]));
        pw.print("/");
        pw.println(getResourceName(mLastPhoneSignalIconIdGemini[0]));
        pw.print("  mLastPhoneSignalIconIdGemini1=0x");
        pw.print(Integer.toHexString(mLastPhoneSignalIconIdGemini[1]));
        pw.print("/");
        pw.println(getResourceName(mLastPhoneSignalIconIdGemini[1]));
        pw.print("  mLastDataDirectionIconId=0x");
        pw.print(Integer.toHexString(mLastDataDirectionIconId));
        pw.print("/");
        pw.println(getResourceName(mLastDataDirectionIconId));
        pw.print("  mLastDataDirectionOverlayIconId=0x");
        pw.print(Integer.toHexString(mLastDataDirectionOverlayIconId));
        pw.print("/");
        pw.println(getResourceName(mLastDataDirectionOverlayIconId));
        pw.print("  mLastWifiIconId=0x");
        pw.print(Integer.toHexString(mLastWifiIconId));
        pw.print("/");
        pw.println(getResourceName(mLastWifiIconId));
        pw.print("  mLastCombinedSignalIconId=0x");
        pw.print(Integer.toHexString(mLastCombinedSignalIconId));
        pw.print("/");
        pw.println(getResourceName(mLastCombinedSignalIconId));
        pw.print("  mLastDataTypeIconId=0x");
        pw.print(Integer.toHexString(mLastDataTypeIconId));
        pw.print("/");
        pw.println(getResourceName(mLastDataTypeIconId));
        pw.print("  mLastCombinedLabel=");
        pw.print(mLastCombinedLabel);
        pw.println("");
    }

    private String getResourceName(int resId) {
        if (resId != 0) {
            final Resources res = mContext.getResources();
            try {
                return res.getResourceName(resId);
            } catch (android.content.res.Resources.NotFoundException ex) {
                return "(unknown)";
            }
        } else {
            return "(null)";
        }
    }

    // [SystemUI] Support "Dual SIM". {


    // whether the SIMs initialization of framework is ready.
    private boolean mSimCardReady = false;
    private static final boolean IS_CU = SIMHelper.isCU() || SIMHelper.isCT();
    private static final boolean IS_CT = SIMHelper.isCT();

	  // telephony
    boolean mDataConnectedGemini;
    IccCard.State mSimStateGemini = IccCard.State.READY;
    int mDataNetTypeGemini = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    int mDataStateGemini = TelephonyManager.DATA_DISCONNECTED;
    int mDataActivityGemini = TelephonyManager.DATA_ACTIVITY_NONE;
    ServiceState mServiceStateGemini;
    SignalStrength mSignalStrengthGemini;
    int[] mDataIconListGemini;
    String mNetworkNameGemini;
    int mPhoneSignalIconIdGemini[] = {0,0};
    int mDataDirectionIconIdGemini;
    int mDataSignalIconIdGemini;
    int mDataTypeIconIdGemini;
    int mMobileActivityIconIdGemini;
    int mLastSignalLevelGemini[] = {0,0};

    String mContentDescriptionPhoneSignalGemini;
    String mContentDescriptionCombinedSignalGemini;
    String mContentDescriptionDataTypeGemini;

    // our ui
    int mLastPhoneSignalIconIdGemini[] = {-1,-1};
    int mLastDataTypeIconIdGemini = -1;

    boolean mMobileVisible = false, mMobileVisibleGemini = false;
    int mSIMBackground = -1, mSIMBackgroundGemini = -1;
    boolean mLastMobileVisible = true, mLastMobileVisibleGemini = true;
    int mLastSIMBackground, mLastSIMBackgroundGemini;

    DataNetType mDataNetType3G = null, mDataNetType3GGemini = null;
    boolean mSIMCUSignVisible = true;
    LinearLayout mCarrier1 = null;
    LinearLayout mCarrier2 = null;
    View mCarrierDivider = null;

    PhoneStateListener mPhoneStateListenerGemini = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, sim2 before.");
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, signalStrength=" + signalStrength.getLevel());
            mSignalStrengthGemini = signalStrength;
            if(IS_CT)
            	updateDataNetType(Phone.GEMINI_SIM_2);
            updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_2);
            Xlog.d(TAG, "PhoneStateListener:onSignalStrengthsChanged, sim2 after.");
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, sim2 before.");
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, state=" + state.getState());
            mServiceStateGemini = state;
            //BEGIN [20120301][ALPS00245624]
            //mDataNetTypeGemini = mServiceStateGemini.getRadioTechnology();
            TelephonyManager mTelephonyManager = TelephonyManager.getDefault();
            mDataNetTypeGemini = mTelephonyManager.getNetworkTypeGemini(Phone.GEMINI_SIM_2);
            Xlog.d(TAG,"PhoneStateListener:onServiceStateChanged sim2 mDataNetTypeGemini= "+mDataNetTypeGemini);
            //END   [20120301][ALPS00245624]
			updateDataNetType(Phone.GEMINI_SIM_2);            
			updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
            updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
            
            updateDataIcon(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_2);
            Xlog.d(TAG, "PhoneStateListener:onServiceStateChanged, sim2 after.");
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, sim2 before.");
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, state=" + state);
            // In cdma, if a voice call is made, RSSI should switch to 1x.
            if (isCdma(Phone.GEMINI_SIM_2)) {
            	if(IS_CT)
            		updateDataNetType(Phone.GEMINI_SIM_2);
                updateTelephonySignalStrength(Phone.GEMINI_SIM_2);
                updateSignalBackgroundBySlotId(Phone.GEMINI_SIM_2);
                refreshViews(Phone.GEMINI_SIM_2);
            }
            //updateDataNetType(Phone.GEMINI_SIM_2);
            updateDataIcon(Phone.GEMINI_SIM_2);
            //refreshViews(Phone.GEMINI_SIM_2);
            updateDataNetType(Phone.GEMINI_SIM_1);
            
            updateDataIcon(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_1);
            refreshViews(Phone.GEMINI_SIM_2);
            Xlog.d(TAG, "PhoneStateListener:onCallStateChanged, sim2 after.");
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, sim2 before.");
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, state=" + state + " type=" + networkType);
            mDataStateGemini = state;
            mDataNetTypeGemini = networkType;
            updateDataNetType(Phone.GEMINI_SIM_2);
            updateDataIcon(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_2);
            Xlog.d(TAG, "PhoneStateListener:onDataConnectionStateChanged, sim2 after.");
        }

        @Override
        public void onDataActivity(int direction) {
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, sim2 before.");
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, direction=" + direction);
            mDataActivityGemini = direction;
            updateDataIcon(Phone.GEMINI_SIM_2);
            refreshViews(Phone.GEMINI_SIM_2);
            Xlog.d(TAG, "PhoneStateListener:onDataActivity, sim2 after.");
        }
    };

    // Only for "Dual SIM".
    private void updateSignalBackgroundBySlotId(int slotId) {
        if (slotId != 0 && slotId != 1) {
            Xlog.d(TAG, "updateSignalBackgroundBySlotId(" + slotId + "), the slotId=" + slotId + ".");
            return;
        }
        boolean simInserted = false;
        if (IS_CU) {
            simInserted = isSimInserted(slotId);
            Xlog.d(TAG, "updateSignalBackgroundBySlotId(" + slotId + "), simInserted=" + simInserted);
        }
        SIMInfo simInfo = SIMHelper.getSIMInfoBySlot(mContext, slotId);
        boolean b;
        if (IS_CU) {
            b = !simInserted || simInfo == null;
        } else {
            b = simInfo == null;
        }
        //
        boolean tempMobileVisible = false;
        int tempResId = -1;
        if (b) {
            Xlog.d(TAG, "updateSignalBackgroundBySlotId(" + slotId + "), the simInfo is null.");
            if (IS_CU) {
                tempResId = com.mediatek.internal.R.drawable.sim_background_locked;
                tempMobileVisible = true;
            } else {
                tempMobileVisible = false;
            }
            updateOperatorInfo();
        } else {
            tempMobileVisible = true;
            updateOperatorInfo();
            tempResId = simInfo.mSimBackgroundRes;
        }
        Xlog.d(TAG, "updateSignalBackgroundBySlotId(" + slotId + "), MobileVisible=" + tempMobileVisible + ", ResId=" + tempResId);
        if (tempMobileVisible && tempResId <= 0) {
            tempMobileVisible = false;
        }
        
        if ( IS_CMCC && (!isSimInserted(slotId)) ){
        	tempMobileVisible = true;
        } 


        if (slotId == Phone.GEMINI_SIM_1) {
            mMobileVisible = tempMobileVisible;
            mSIMBackground = tempResId;
        } else {
            mMobileVisibleGemini = tempMobileVisible;
            mSIMBackgroundGemini = tempResId;
        }
        refreshViews(slotId);
    }

    // Only for "Dual SIM".
    private void updateSignalBackgroundBySimId(long simId) {
        Xlog.d(TAG, "updateSignalBackgroundBySimId, the simId is " + simId + ".");
        if (simId == -1) {
            return;
        }
        SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if (simInfo == null) {
            Xlog.d(TAG, "updateSignalBackgroundBySimId, the simInfo is null.");
            return;
        }
    }

    // Only for "Dual SIM".
    public void setCarrierGemini(LinearLayout carrier1, LinearLayout carrier2, View carrierDivider) {
        this.mCarrier1 = carrier1;
        this.mCarrier2 = carrier2;
        this.mCarrierDivider = carrierDivider;
    }

    // Only for "Dual SIM".
    private void updateOperatorInfo() {
        if (mCarrier1 == null || mCarrier2 == null) {
            return;
        }
        boolean sim1Inserted = isSimInserted(Phone.GEMINI_SIM_1);
        boolean sim2Inserted = isSimInserted(Phone.GEMINI_SIM_2);
        mCarrier1.setVisibility(sim1Inserted ? View.VISIBLE : View.GONE);
        mCarrier2.setVisibility(sim2Inserted ? View.VISIBLE : View.GONE);
        Xlog.d(TAG, "updateOperatorInfo, sim1Inserted is " + sim1Inserted + ", sim2Inserted is " + sim2Inserted + ".");
        if (!sim1Inserted && !sim2Inserted) {
            sim1Inserted = true;
            mCarrier1.setVisibility(View.VISIBLE);
            Xlog.d(TAG, "updateOperatorInfo, force the slotId 0 to visible.");
        }
        // correct the gravity properties
        if (sim1Inserted != sim2Inserted) {
            if (sim1Inserted) {
                mCarrier1.setGravity(Gravity.CENTER);
            } else {
                mCarrier2.setGravity(Gravity.CENTER);
            }
            mCarrierDivider.setVisibility(View.GONE);
        } else {
            mCarrier1.setGravity(Gravity.RIGHT);
            mCarrier2.setGravity(Gravity.LEFT);
            mCarrierDivider.setVisibility(View.VISIBLE);
        }
    }

    // Only for "Dual SIM".
    private boolean isSimInserted(int slotId) {
        boolean simInserted = false;
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (phone != null) {
            try {
                simInserted = phone.isSimInsert(slotId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Xlog.d(TAG, "isSimInserted(" + slotId + "), SimInserted=" + simInserted);
        return simInserted;
    }

    // [SystemUI] Support "Dual SIM". }
}
