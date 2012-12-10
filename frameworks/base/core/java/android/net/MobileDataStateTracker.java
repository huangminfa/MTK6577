/*
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

package android.net;

import static com.android.internal.telephony.DataConnectionTracker.CMD_SET_POLICY_DATA_ENABLE;
import static com.android.internal.telephony.DataConnectionTracker.CMD_SET_USER_DATA_ENABLE;
import static com.android.internal.telephony.DataConnectionTracker.DISABLED;
import static com.android.internal.telephony.DataConnectionTracker.ENABLED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;

import com.android.internal.telephony.DataConnectionTracker;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.util.AsyncChannel;
import com.mediatek.featureoption.FeatureOption;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * Track the state of mobile data connectivity. This is done by
 * receiving broadcast intents from the Phone process whenever
 * the state of data connectivity changes.
 *
 * {@hide}
 */
public class MobileDataStateTracker implements NetworkStateTracker {

    private static final String TAG = "MobileDataStateTracker";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;

    private Phone.DataState mMobileDataState;
    private ITelephony mPhoneService;

    private String mApnType;
    private NetworkInfo mNetworkInfo;
    private boolean mTeardownRequested = false;
    private Handler mTarget;
    private Context mContext;
    private LinkProperties mLinkProperties;
    private LinkCapabilities mLinkCapabilities;
    private boolean mPrivateDnsRouteSet = false;
    private boolean mDefaultRouteSet = false;

    // NOTE: these are only kept for debugging output; actual values are
    // maintained in DataConnectionTracker.
    protected boolean mUserDataEnabled = true;
    protected boolean mPolicyDataEnabled = true;

    private Handler mHandler;
    private AsyncChannel mDataConnectionTrackerAc;
    private Messenger mMessenger;

    private static MobileDataStateReceiver mMobileDataStateReceiver;//use the static instance to reduce receiver

    /**
     * Create a new MobileDataStateTracker
     * @param netType the ConnectivityManager network type
     * @param tag the name of this network
     */
    public MobileDataStateTracker(int netType, String tag) {
        mNetworkInfo = new NetworkInfo(netType,
                TelephonyManager.getDefault().getNetworkType(), tag,
                TelephonyManager.getDefault().getNetworkTypeName());
        mApnType = networkTypeToApnType(netType);
    }

    /**
     * Begin monitoring data connectivity.
     *
     * @param context is the current Android context
     * @param target is the Hander to which to return the events.
     */
    public void startMonitoring(Context context, Handler target) {
        mTarget = target;
        mContext = context;

        mHandler = new MdstHandler(target.getLooper(), this);

        if (mMobileDataStateReceiver == null) {
            mMobileDataStateReceiver = new MobileDataStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED_MOBILE);
            filter.addAction(TelephonyIntents.ACTION_DATA_CONNECTION_FAILED);
            filter.addAction(DataConnectionTracker.ACTION_DATA_CONNECTION_TRACKER_MESSENGER);
            mContext.registerReceiver(mMobileDataStateReceiver, filter);
        }
        mMobileDataStateReceiver.addTracker(mApnType, this);
        mMobileDataState = Phone.DataState.DISCONNECTED;
    }

    static class MdstHandler extends Handler {
        private MobileDataStateTracker mMdst;

        MdstHandler(Looper looper, MobileDataStateTracker mdst) {
            super(looper);
            mMdst = mdst;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                    if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                        if (VDBG) {
                            mMdst.log("MdstHandler connected");
                        }
                        mMdst.mDataConnectionTrackerAc = (AsyncChannel) msg.obj;
                    } else {
                        if (VDBG) {
                            mMdst.log("MdstHandler %s NOT connected error=" + msg.arg1);
                        }
                    }
                    break;
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED:
                    if (VDBG) mMdst.log("Disconnected from DataStateTracker");
                    mMdst.mDataConnectionTrackerAc = null;
                    break;
                default: {
                    if (VDBG) mMdst.log("Ignorning unknown message=" + msg);
                    break;
                }
            }
        }
    }

    public boolean isPrivateDnsRouteSet() {
        return mPrivateDnsRouteSet;
    }

    public void privateDnsRouteSet(boolean enabled) {
        mPrivateDnsRouteSet = enabled;
    }

    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    public boolean isDefaultRouteSet() {
        return mDefaultRouteSet;
    }

    public void defaultRouteSet(boolean enabled) {
        mDefaultRouteSet = enabled;
    }

    /**
     * This is not implemented.
     */
    public void releaseWakeLock() {
    }

    private static class MobileDataStateReceiver extends BroadcastReceiver {
        private HashMap<String, MobileDataStateTracker> mTrackerMap = new HashMap<String, MobileDataStateTracker>();

        public void addTracker(String apnType, MobileDataStateTracker tracker) {
            Slog.e(TAG, "MobileDataStateReceiver add target tracker [" + apnType + "]");
            mTrackerMap.put(apnType, tracker);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED_MOBILE)) {
                String apnType = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
                MobileDataStateTracker tracker = mTrackerMap.get(apnType);
                if (tracker == null) {
                    return;
                }

                if (VDBG)
                    Slog.d(TAG, "MobileDataStateReceiver received: ACTION_ANY_DATA_CONNECTION_STATE_CHANGED_MOBILE [" + apnType + "]");

                int slot = 0;
                int curSlot = 0;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    slot = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                    curSlot = tracker.mNetworkInfo.getSimId();
                }
                tracker.log("Intent from SIM " + slot + ", current SIM " + curSlot + ", current DataState " + tracker.mMobileDataState);
               
                if (tracker.mMobileDataState == Phone.DataState.CONNECTED) {
                	if (slot != curSlot) {
                		tracker.log("Receive peer SIM data state.ignor!");
                		return;
                	}
                }
                
                
                tracker.mNetworkInfo.setSubtype(TelephonyManager.getDefault().getNetworkType(),
                        TelephonyManager.getDefault().getNetworkTypeName());
                Phone.DataState state = Enum.valueOf(Phone.DataState.class,
                        intent.getStringExtra(Phone.STATE_KEY));
                String reason = intent.getStringExtra(Phone.STATE_CHANGE_REASON_KEY);
                String apnName = intent.getStringExtra(Phone.DATA_APN_KEY);
                tracker.mNetworkInfo.setRoaming(intent.getBooleanExtra(Phone.DATA_NETWORK_ROAMING_KEY, false));
                if (VDBG) {
                    tracker.log(tracker.mApnType + " setting isAvailable to " +
                            !intent.getBooleanExtra(Phone.NETWORK_UNAVAILABLE_KEY,false));
                }
               tracker.mNetworkInfo.setIsAvailable(!intent.getBooleanExtra(Phone.NETWORK_UNAVAILABLE_KEY, false));

                if (DBG) {
                    tracker.log("Received state=" + state + ", old=" + tracker.mMobileDataState +
                        ", reason=" + (reason == null ? "(unspecified)" : reason));
                }
                if (tracker.mMobileDataState != state) {
                    tracker.mMobileDataState = state;
                    switch (state) {
                        case DISCONNECTED:
                            if(tracker.isTeardownRequested()) {
                                tracker.setTeardownRequested(false);
                            }
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                tracker.setDetailedStateGemini(DetailedState.DISCONNECTED, reason, apnName, slot);
                            } else {
                                tracker.setDetailedState(DetailedState.DISCONNECTED, reason, apnName);
                            }
                            // can't do this here - ConnectivityService needs it to clear stuff
                            // it's ok though - just leave it to be refreshed next time
                            // we connect.
                            //if (DBG) log("clearing mInterfaceName for "+ mApnType +
                            //        " as it DISCONNECTED");
                            //mInterfaceName = null;
                            break;
                        case CONNECTING:
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                tracker.setDetailedStateGemini(DetailedState.CONNECTING, reason, apnName, slot);
                            } else {
                                tracker.setDetailedState(DetailedState.CONNECTING, reason, apnName);
                            }
                            break;
                        case SUSPENDED:
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                tracker.setDetailedStateGemini(DetailedState.SUSPENDED, reason, apnName, slot);
                            } else {
                                tracker.setDetailedState(DetailedState.SUSPENDED, reason, apnName);
                            }
                            break;
                        case CONNECTED:
                            tracker.mLinkProperties = intent.getParcelableExtra(
                                    Phone.DATA_LINK_PROPERTIES_KEY);
                            if (tracker.mLinkProperties == null) {
                                tracker.loge("CONNECTED event did not supply link properties.");
                                tracker.mLinkProperties = new LinkProperties();
                            }
                            tracker.mLinkCapabilities = intent.getParcelableExtra(
                                    Phone.DATA_LINK_CAPABILITIES_KEY);
                            if (tracker.mLinkCapabilities == null) {
                                tracker.loge("CONNECTED event did not supply link capabilities.");
                                tracker.mLinkCapabilities = new LinkCapabilities();
                            }
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                tracker.setDetailedStateGemini(DetailedState.CONNECTED, reason, apnName, slot);
                            } else {
                                tracker.setDetailedState(DetailedState.CONNECTED, reason, apnName);
                            }
                            
                            //Add by MTK03594
                            tracker.setInterfaceMTU();
                            
                            break;
                    }
                } else {
                    // There was no state change. Check if LinkProperties has been updated.
                    if (TextUtils.equals(reason, Phone.REASON_LINK_PROPERTIES_CHANGED)) {
                        tracker.mLinkProperties = intent.getParcelableExtra(Phone.DATA_LINK_PROPERTIES_KEY);
                        if (tracker.mLinkProperties == null) {
                            tracker.loge("No link property in LINK_PROPERTIES change event.");
                            tracker.mLinkProperties = new LinkProperties();
                        }
                        // Just update reason field in this NetworkInfo
                        tracker.mNetworkInfo.setDetailedState(tracker.mNetworkInfo.getDetailedState(), reason,
                                                      tracker.mNetworkInfo.getExtraInfo());
                        Message msg = tracker.mTarget.obtainMessage(EVENT_CONFIGURATION_CHANGED,
                                                            tracker.mNetworkInfo);
                        msg.sendToTarget();
                    }
                    
                    if (reason != null
                            && (reason.equals(Phone.REASON_APN_FAILED) 
                                    || reason.equals(Phone.REASON_NO_SUCH_PDP) ||
                                    reason.equals(Phone.REASON_VOICE_CALL_ENDED))
                            && apnType != null
                            && !apnType.equals(Phone.APN_TYPE_DEFAULT)) {
                        tracker.log("Handle Phone.REASON_APN_FAILED OR  Phone.REASON_NO_SUCH_PDP from GeminiDataSubUtil");
                        if (state == Phone.DataState.DISCONNECTED) {
                            if (tracker.isTeardownRequested()) {
                                tracker.setTeardownRequested(false);
                            }
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                tracker.setDetailedStateGemini(DetailedState.DISCONNECTED, reason, apnName, slot);
                            } else {
                                tracker.setDetailedState(DetailedState.DISCONNECTED, reason, apnName);
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(TelephonyIntents.ACTION_DATA_CONNECTION_FAILED)) {
                String apnType = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
                MobileDataStateTracker tracker = mTrackerMap.get(apnType);
                if (tracker == null) {
                    return;
                }
                if (DBG)
                   Slog.d(TAG, "MobileDataStateReceiver received: ACTION_ANY_DATA_CONNECTION_FAILED ignore [" + apnType + "]");

                int slot = 0;
                String reason = intent.getStringExtra(Phone.FAILURE_REASON_KEY);
                String apnName = intent.getStringExtra(Phone.DATA_APN_KEY);
                if (DBG) {
                    tracker.log("Received " + intent.getAction() +
                                " broadcast" + reason == null ? "" : "(" + reason + ")");
                }
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    slot = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,Phone.GEMINI_SIM_1);
                    tracker.setDetailedStateGemini(DetailedState.FAILED, reason, apnName, slot);
                } else {
                    tracker.setDetailedState(DetailedState.FAILED, reason, apnName);
                }
            } else if (intent.getAction().equals(DataConnectionTracker.ACTION_DATA_CONNECTION_TRACKER_MESSENGER)) {
                if (VDBG) Slog.d(TAG, "MobileDataStateReceiver received: ACTION_DATA_CONNECTION_TRACKER_MESSENGER");
                Messenger messenger = intent.getParcelableExtra(DataConnectionTracker.EXTRA_MESSENGER);
                Collection<MobileDataStateTracker> collection = mTrackerMap.values();
                Iterator<MobileDataStateTracker> iter = collection.iterator();
                while (iter.hasNext()) {
                    MobileDataStateTracker tracker = iter.next();
                    tracker.mMessenger = messenger;
                    AsyncChannel ac = new AsyncChannel();
                    ac.connect(tracker.mContext, tracker.mHandler, tracker.mMessenger);
                }
            } else {
                if (DBG) Slog.d(TAG, "MobileDataStateReceiver received: ignore " + intent.getAction());
            }
        }
    }

    private void getPhoneService(boolean forceRefresh) {
        if ((mPhoneService == null) || forceRefresh) {
            mPhoneService = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        }
    }

    /**
     * Report whether data connectivity is possible.
     */
    public boolean isAvailable() {
        return mNetworkInfo.isAvailable();
    }

    /**
     * Return the system properties name associated with the tcp buffer sizes
     * for this network.
     */
    public String getTcpBufferSizesPropName() {
        int netType;
        String networkTypeStr = "unknown";
        TelephonyManager tm = new TelephonyManager(mContext);
        //TODO We have to edit the parameter for getNetworkType regarding CDMA
        if (FeatureOption.MTK_GEMINI_SUPPORT != true)
        {
            netType = tm.getNetworkType();
        }
        else
        {
            netType = tm.getNetworkTypeGemini(mNetworkInfo.getSimId());
        }

        //TODO We have to edit the parameter for getNetworkType regarding CDMA
        switch(netType) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
            networkTypeStr = "gprs";
            break;
        case TelephonyManager.NETWORK_TYPE_EDGE:
            networkTypeStr = "edge";
            break;
        case TelephonyManager.NETWORK_TYPE_UMTS:
            networkTypeStr = "umts";
            break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            networkTypeStr = "hsdpa";
            break;
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            networkTypeStr = "hsupa";
            break;
        case TelephonyManager.NETWORK_TYPE_HSPA:
            networkTypeStr = "hspa";
            break;
        case TelephonyManager.NETWORK_TYPE_CDMA:
            networkTypeStr = "cdma";
            break;
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            networkTypeStr = "1xrtt";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            networkTypeStr = "evdo";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            networkTypeStr = "evdo";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
            networkTypeStr = "evdo";
            break;
        case TelephonyManager.NETWORK_TYPE_IDEN:
            networkTypeStr = "iden";
            break;
        case TelephonyManager.NETWORK_TYPE_LTE:
            networkTypeStr = "lte";
            break;
        case TelephonyManager.NETWORK_TYPE_EHRPD:
            networkTypeStr = "ehrpd";
            break;
        default:
            loge("unknown network type: " + tm.getNetworkType());
        }
        return "net.tcp.buffersize." + networkTypeStr;
    }

    /**
     * Tear down mobile data connectivity, i.e., disable the ability to create
     * mobile data connections.
     * TODO - make async and return nothing?
     */
    public boolean teardown() {
        setTeardownRequested(true);
        return (setEnableApn(mApnType, false) != Phone.APN_REQUEST_FAILED);
    }

    /**
     * Record the detailed state of a network, and if it is a
     * change from the previous state, send a notification to
     * any listeners.
     * @param state the new @{code DetailedState}
     * @param reason a {@code String} indicating a reason for the state change,
     * if one was supplied. May be {@code null}.
     * @param extraInfo optional {@code String} providing extra information about the state change
     */
    private void setDetailedState(NetworkInfo.DetailedState state, String reason,
            String extraInfo) {
        if (DBG) log("setDetailed state, old ="
                + mNetworkInfo.getDetailedState() + " and new state=" + state);
        if (state != mNetworkInfo.getDetailedState()) {
            boolean wasConnecting = (mNetworkInfo.getState() == NetworkInfo.State.CONNECTING);
            String lastReason = mNetworkInfo.getReason();
            /*
             * If a reason was supplied when the CONNECTING state was entered, and no
             * reason was supplied for entering the CONNECTED state, then retain the
             * reason that was supplied when going to CONNECTING.
             */
            if (wasConnecting && state == NetworkInfo.DetailedState.CONNECTED && reason == null
                    && lastReason != null)
                reason = lastReason;
            mNetworkInfo.setDetailedState(state, reason, extraInfo);
            Message msg = mTarget.obtainMessage(EVENT_STATE_CHANGED, new NetworkInfo(mNetworkInfo));
            msg.sendToTarget();
        } else if(reason != null && (reason.equals(Phone.REASON_NO_SUCH_PDP) || reason.equals(Phone.REASON_APN_FAILED)) && state == DetailedState.DISCONNECTED){            
            mNetworkInfo.setDetailedState(state, reason, extraInfo);
            Message msg = mTarget.obtainMessage(EVENT_STATE_CHANGED, mNetworkInfo);
            msg.sendToTarget();
        }
    }

    public void setTeardownRequested(boolean isRequested) {
        mTeardownRequested = isRequested;
    }

    public boolean isTeardownRequested() {
        return mTeardownRequested;
    }

    /**
     * Re-enable mobile data connectivity after a {@link #teardown()}.
     * TODO - make async and always get a notification?
     */
    public boolean reconnect() {
        boolean retValue = false; //connected or expect to be?
        
        setTeardownRequested(false);
        switch (setEnableApn(mApnType, true)) {
            case Phone.APN_ALREADY_ACTIVE:
                // need to set self to CONNECTING so the below message is handled.
                //TODO: need to notify the app
                retValue = true;
                break;
            case Phone.APN_REQUEST_STARTED:
                // set IDLE here , avoid the following second FAILED not sent out
                if (!mNetworkInfo.isConnectedOrConnecting()) {
                    mNetworkInfo.setDetailedState(DetailedState.IDLE, null, null);
                }
                retValue = true;
                break;
            case Phone.APN_REQUEST_FAILED:
            case Phone.APN_TYPE_NOT_AVAILABLE:
                break;
            default:
                loge("Error in reconnect - unexpected response.");
                break;
        }
        return retValue;
    }

    /**
     * Turn on or off the mobile radio. No connectivity will be possible while the
     * radio is off. The operation is a no-op if the radio is already in the desired state.
     * @param turnOn {@code true} if the radio should be turned on, {@code false} if
     */
    public boolean setRadio(boolean turnOn) {
        getPhoneService(false);
        /*
         * If the phone process has crashed in the past, we'll get a
         * RemoteException and need to re-reference the service.
         */
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) {
                loge("Ignoring mobile radio request because could not acquire PhoneService");
                break;
            }

            try {
                return mPhoneService.setRadio(turnOn);
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }

        loge("Could not set radio power to " + (turnOn ? "on" : "off"));
        return false;
    }

    @Override
    public void setUserDataEnable(boolean enabled) {
        if (DBG) log("setUserDataEnable: E enabled=" + enabled);
        final AsyncChannel channel = mDataConnectionTrackerAc;
        if (channel != null) {
            channel.sendMessage(CMD_SET_USER_DATA_ENABLE, enabled ? ENABLED : DISABLED);
            mUserDataEnabled = enabled;
            if (VDBG) log("setUserDataEnable: X enabled=" + enabled);
        }
    }

    @Override
    public void setPolicyDataEnable(boolean enabled) {
        if (DBG) log("setPolicyDataEnable(enabled=" + enabled + ")");
        final AsyncChannel channel = mDataConnectionTrackerAc;
        if (channel != null) {
            channel.sendMessage(CMD_SET_POLICY_DATA_ENABLE, enabled ? ENABLED : DISABLED);
            mPolicyDataEnabled = enabled;
        }
    }

    /**
     * carrier dependency is met/unmet
     * @param met
     */
    public void setDependencyMet(boolean met) {
        Bundle bundle = Bundle.forPair(DataConnectionTracker.APN_TYPE_KEY, mApnType);
        try {
            if (DBG) log("setDependencyMet: E met=" + met);
            Message msg = Message.obtain();
            msg.what = DataConnectionTracker.CMD_SET_DEPENDENCY_MET;
            msg.arg1 = (met ? DataConnectionTracker.ENABLED : DataConnectionTracker.DISABLED);
            msg.setData(bundle);
            mDataConnectionTrackerAc.sendMessage(msg);
            if (VDBG) log("setDependencyMet: X met=" + met);
        } catch (NullPointerException e) {
            loge("setDependencyMet: X mAc was null" + e);
        }
    }

    @Override
    public String toString() {
        final CharArrayWriter writer = new CharArrayWriter();
        final PrintWriter pw = new PrintWriter(writer);
        pw.print("Mobile data state: "); pw.println(mMobileDataState);
        pw.print("Data enabled: user="); pw.print(mUserDataEnabled);
        pw.print(", policy="); pw.println(mPolicyDataEnabled);
        return writer.toString();
    }

   /**
     * Internal method supporting the ENABLE_MMS feature.
     * @param apnType the type of APN to be enabled or disabled (e.g., mms)
     * @param enable {@code true} to enable the specified APN type,
     * {@code false} to disable it.
     * @return an integer value representing the outcome of the request.
     */
    private int setEnableApn(String apnType, boolean enable) {
        getPhoneService(false);
        /*
         * If the phone process has crashed in the past, we'll get a
         * RemoteException and need to re-reference the service.
         */
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) {
                loge("Ignoring feature request because could not acquire PhoneService");
                break;
            }

            try {
                if (enable) {
                    return mPhoneService.enableApnType(apnType);
                } else {
                    return mPhoneService.disableApnType(apnType);
                }
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }

        loge("Could not " + (enable ? "enable" : "disable") + " APN type \"" + apnType + "\"");
        return Phone.APN_REQUEST_FAILED;
    }

    public static String networkTypeToApnType(int netType) {
        switch(netType) {
            case ConnectivityManager.TYPE_MOBILE:
                return Phone.APN_TYPE_DEFAULT;  // TODO - use just one of these
            case ConnectivityManager.TYPE_MOBILE_MMS:
                return Phone.APN_TYPE_MMS;
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                return Phone.APN_TYPE_SUPL;
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return Phone.APN_TYPE_DUN;
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                return Phone.APN_TYPE_HIPRI;
            case ConnectivityManager.TYPE_MOBILE_FOTA:
                return Phone.APN_TYPE_FOTA;
            case ConnectivityManager.TYPE_MOBILE_IMS:
                return Phone.APN_TYPE_IMS;
            case ConnectivityManager.TYPE_MOBILE_CBS:
                return Phone.APN_TYPE_CBS;
            case ConnectivityManager.TYPE_MOBILE_DM:
                return Phone.APN_TYPE_DM;
            case ConnectivityManager.TYPE_MOBILE_NET:
                return Phone.APN_TYPE_NET;
            case ConnectivityManager.TYPE_MOBILE_WAP:
                return Phone.APN_TYPE_WAP;
            case ConnectivityManager.TYPE_MOBILE_CMMAIL:
                return Phone.APN_TYPE_CMMAIL;
            default:
                sloge("Error mapping networkType " + netType + " to apnType.");
                return null;
        }
    }

    /**
     * @see android.net.NetworkStateTracker#getLinkProperties()
     */
    public LinkProperties getLinkProperties() {
        return new LinkProperties(mLinkProperties);
    }

    /**
     * @see android.net.NetworkStateTracker#getLinkCapabilities()
     */
    public LinkCapabilities getLinkCapabilities() {
        return new LinkCapabilities(mLinkCapabilities);
    }

    private void log(String s) {
        Slog.d(TAG, mApnType + ": " + s);
    }

    private void loge(String s) {
        Slog.e(TAG, mApnType + ": " + s);
    }

    static private void sloge(String s) {
        Slog.e(TAG, s);
    }
    
    //MTK-START [mtk04070][111128][ALPS00093395]MTK proprietary methods
    public boolean teardownGemini(int radioNum) {
        setTeardownRequested(true);
        return (setEnableApnGemini(mApnType, false, radioNum) != Phone.APN_REQUEST_FAILED);
    }
    
    public boolean reconnectGemini(int radioNum) {
        boolean retValue = false; //connected or expect to be?
        boolean isIdle_request = isIdleGemini(radioNum);
        boolean isIdle_peer = isIdleGemini(radioNum == Phone.GEMINI_SIM_1 ? Phone.GEMINI_SIM_2 : Phone.GEMINI_SIM_1);
        boolean isConcurrentVoiceAndData = isConcurrentVoiceAndData(radioNum);
        log("isIdle_request=" + isIdle_request 
                + ",isIdle_peer=" + isIdle_peer 
                + ",isConcurrentVoiceAndData=" + isConcurrentVoiceAndData);

        if(PhoneFactory.isDualTalkMode()) {
            if ((!isIdle_request && !isConcurrentVoiceAndData)) {
                return false;
            }
        } else {
            if ((!isIdle_request && !isConcurrentVoiceAndData) || !isIdle_peer) {
                return false;
            }
        } 

        
        setTeardownRequested(false);
        switch (setEnableApnGemini(mApnType, true, radioNum)) {
            case Phone.APN_ALREADY_ACTIVE:
                // need to set self to CONNECTING so the below message is handled.
                //TODO: need to notify the app
                retValue = true;
                break;
            case Phone.APN_REQUEST_STARTED:
                // set IDLE here , avoid the following second FAILED not sent out
                if (!mNetworkInfo.isConnectedOrConnecting()) {
                    mNetworkInfo.setDetailedState(DetailedState.IDLE, null, null);
                }
                retValue = true;
                break;
            case Phone.APN_REQUEST_FAILED:
            case Phone.APN_TYPE_NOT_AVAILABLE:
                break;
            default:
                loge("Error in reconnect - unexpected response.");
                break;
        }
        return retValue;
    	  //Temporary mark
        /* Add by mtk01411 */
        /*
        int enable_apn_result = -1;
        setTeardownRequested(false);
	      Log.d(TAG,  "gemini before setEnableApnGemini() and mApnType is " + mApnType + " ,radioNum is " + radioNum);	
        enable_apn_result = setEnableApnGemini(mApnType, true,radioNum);
        Log.d(TAG, NetTypePrefix + "reconnect(): enable_apn_result=" + enable_apn_result);
        switch (enable_apn_result) {
            case Phone.APN_ALREADY_ACTIVE:
                // TODO - remove this when we get per-apn notifications
                mEnabled = true;
                // need to set self to CONNECTING so the below message is handled.
                mMobileDataState = Phone.DataState.CONNECTING;
                setDetailedStateGemini(DetailedState.CONNECTING, Phone.REASON_APN_CHANGED, null, radioNum);
                //send out a connected message
                Intent intent = new Intent(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
                intent.putExtra(Phone.STATE_KEY, Phone.DataState.CONNECTED.toString());
                intent.putExtra(Phone.STATE_CHANGE_REASON_KEY, Phone.REASON_APN_CHANGED);
                intent.putExtra(Phone.DATA_APN_TYPES_KEY, mApnType);
                intent.putExtra(Phone.DATA_APN_KEY, mApnName);
                intent.putExtra(Phone.DATA_IFACE_NAME_KEY, mInterfaceName);
                intent.putExtra(Phone.NETWORK_UNAVAILABLE_KEY, false);
		            intent.putExtra(ConnectivityManager.EXTRA_SIM_ID,radioNum);		
                if (mStateReceiver != null) mStateReceiver.onReceive(mContext, intent);
                break;
            case Phone.APN_REQUEST_STARTED:
                mEnabled = true;
                // no need to do anything - we're already due some status update intents
                break;
            case Phone.APN_REQUEST_FAILED:
                if (mPhoneService == null && mApnType == Phone.APN_TYPE_DEFAULT) {
                    // on startup we may try to talk to the phone before it's ready
                    // just leave mEnabled as it is for the default apn.
                    return false;
                }
                // else fall through: It means that if returned value is APN_REQUEST_FAILED, mEnabled is also set as false
            case Phone.APN_TYPE_NOT_AVAILABLE:
                mEnabled = false;
                break;
            // Add by mtk01411
            case Phone.APN_REQUEST_FAILED_DUE_TO_RADIO_OFF:
                Log.d(TAG, NetTypePrefix + "Can't enable Apn due to radio off");
                // fall through the following cases: Same handle procedure
            case Phone.APN_TYPE_NOT_AVAILABLE_DUE_TO_RECORDS_NOT_LOADED:
                mEnabled = false;
                mEnabledApnFailNoRecordsLoaded = true;
                Log.d(TAG, NetTypePrefix + "Can't enable Apn due to sim records are not loaded");
                break;
            // Add by mtk01411
            case Phone.APN_TYPE_DISABLE_ONGOING:
                if(mApnType == Phone.APN_TYPE_DEFAULT || mApnType == Phone.APN_TYPE_MMS){
                    // Set mEnabled as true: Let MobileDataStateTracker can update its state later when receiving broadcast intent
                    mEnabled = true;
                    // Set teardownReq as true: To avoid the MMS invokes startUsingNetworkFeature() again but mis-understand NW is already active
                    setTeardownRequested(true);
                    Log.d(TAG, NetTypePrefix + "Previous Disable APN Ongoing:Set both mEnabled and teardownReq as true");
                }else{
                    Log.d(TAG, NetTypePrefix + "Can't enable Apn due to Previous Disable APN is Ongoing");
                    mEnabled = false;                    
                }
                break;
            default:
                Log.e(TAG, NetTypePrefix + "Error in reconnect - unexpected response.");
                mEnabled = false;
                break;
        }
        return mEnabled;
        */
        //Temporary mark
    	
        //return true;
    }
 
    public boolean setInterfaceMTU() {
        TelephonyManager tm = TelephonyManager.getDefault();
        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        String interfaceName = mLinkProperties.getInterfaceName();
                        
        if(tm == null || interfaceName == null){
           Slog.e(TAG, "The TelephonyManager or mInterfaceName is null");
           return false;
        }
        
        networkType = tm.getNetworkType();
        Slog.d(TAG, "configure mtu size of interface name " + interfaceName + " with network type " + networkType);
        
        if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE_MMS){
            if(networkType < TelephonyManager.NETWORK_TYPE_UMTS){
               Slog.d(TAG, "configure mtu size:" + mContext.getResources().getInteger(com.android.internal.R.integer.config_mms_mtu_2g_size));
               return NetworkUtils.setMtuByInterface(interfaceName, mContext.getResources().getInteger(com.android.internal.R.integer.config_mms_mtu_2g_size));
            }else{
               Slog.d(TAG, "configure mtu size:" + mContext.getResources().getInteger(com.android.internal.R.integer.config_mms_mtu_3g_size));
               return NetworkUtils.setMtuByInterface(interfaceName, mContext.getResources().getInteger(com.android.internal.R.integer.config_mms_mtu_3g_size));
           }
        }
        
        if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            if(networkType < TelephonyManager.NETWORK_TYPE_UMTS){
               Slog.d(TAG, "configure mtu size:" + mContext.getResources().getInteger(com.android.internal.R.integer.config_mobile_mtu_less_size));
               return NetworkUtils.setMtuByInterface(interfaceName, mContext.getResources().getInteger(com.android.internal.R.integer.config_mobile_mtu_less_size));
            }else{
               Slog.d(TAG, "configure mtu size:" + mContext.getResources().getInteger(com.android.internal.R.integer.config_mobile_mtu_default_size));
               return NetworkUtils.setMtuByInterface(interfaceName, mContext.getResources().getInteger(com.android.internal.R.integer.config_mobile_mtu_default_size));
           }
        }

        return false;
    }
    
    //MTK-END [mtk04070][111128][ALPS00093395]MTK proprietary methods
    public boolean isConcurrentVoiceAndData(int simId) {
        int networkType = 0;
        if (FeatureOption.MTK_GEMINI_SUPPORT != true) {
            networkType = SystemProperties.getInt(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, TelephonyManager.NETWORK_TYPE_UNKNOWN);
        } else {
            if(Phone.GEMINI_SIM_1 == simId){
                networkType = SystemProperties.getInt(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, TelephonyManager.NETWORK_TYPE_UNKNOWN);
            }else{
                networkType = SystemProperties.getInt(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE_2, TelephonyManager.NETWORK_TYPE_UNKNOWN);
            }
        }
        Slog.e(TAG, "isConcurrentVoiceAndDatagemini,networkType:"+networkType+",simId:"+simId);
        return networkType >=  (ServiceState.RADIO_TECHNOLOGY_UMTS);
    }
    
    public boolean isConcurrentVoiceAndData() {
        int networkType = SystemProperties.getInt(TelephonyProperties.PROPERTY_CS_NETWORK_TYPE, TelephonyManager.NETWORK_TYPE_UNKNOWN);
        Slog.e(TAG, "isConcurrentVoiceAndData,networkType:"+networkType);
        return networkType >=  (ServiceState.RADIO_TECHNOLOGY_UMTS);
    }
    
    public boolean isIdleGemini(int radioNum){
        getPhoneService(false);
        
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) break;

            try {
                return mPhoneService.isIdleGemini(radioNum);
            } catch (RemoteException e) {
                // First-time failed, get the phone service again
                if (retry == 0) getPhoneService(true);
            }
        }
        return false;
    }
    public boolean isIdle(){
        getPhoneService(false);
        
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) break;

            try {
                return mPhoneService.isVoiceIdle();
            } catch (RemoteException e) {
                // First-time failed, get the phone service again
                if (retry == 0) getPhoneService(true);
            }
        }
        return false;
    }
    
    /*added for gemini*/
    private int setEnableApnGemini(String apnType, boolean enable, int radioNum) {
        getPhoneService(false);
        /*
         * If the phone process has crashed in the past, we'll get a
         * RemoteException and need to re-reference the service.
         */
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) {
                log("Ignoring feature request because could not acquire PhoneService");
                break;
            }

            try {
                if (enable) {
                    log("gemini before enableApnTypeGemini() and mApnType is " + mApnType + " ,radioNum is " + radioNum);               
                    return mPhoneService.enableApnTypeGemini(apnType,radioNum);
                } else {
                    return mPhoneService.disableApnTypeGemini(apnType,radioNum);
                }
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }

        log("Could not " + (enable ? "enable" : "disable")
                + " APN type \"" + apnType + "\"");
        return Phone.APN_REQUEST_FAILED;
    }
    
    public void setDetailedStateGemini(NetworkInfo.DetailedState state, String reason, String extraInfo, int simId) {
        if (DBG) log( "setDetailed state, old ="+mNetworkInfo.getDetailedState()+" and new state="+state + " simId is " + simId);
        if (state != mNetworkInfo.getDetailedState()) {
            boolean wasConnecting = (mNetworkInfo.getState() == NetworkInfo.State.CONNECTING);
            String lastReason = mNetworkInfo.getReason();
            /*
             * If a reason was supplied when the CONNECTING state was entered, and no
             * reason was supplied for entering the CONNECTED state, then retain the
             * reason that was supplied when going to CONNECTING.
             */
            if (wasConnecting && state == NetworkInfo.DetailedState.CONNECTED && reason == null
                    && lastReason != null)
                reason = lastReason;
            mNetworkInfo.setDetailedStateGemini(state, reason, extraInfo, simId);
            Message msg = mTarget.obtainMessage(EVENT_STATE_CHANGED, mNetworkInfo);
            msg.sendToTarget();
        }else if(reason != null && (reason.equals(Phone.REASON_NO_SUCH_PDP) || reason.equals(Phone.REASON_APN_FAILED)) && state == DetailedState.DISCONNECTED){            
            mNetworkInfo.setDetailedStateGemini(state, reason, extraInfo, simId);            
            Message msg = mTarget.obtainMessage(EVENT_STATE_CHANGED, mNetworkInfo);
            msg.sendToTarget();
        }
    }   
}
