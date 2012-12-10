/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.internal.telephony;

import android.net.LinkCapabilities;
import android.net.LinkProperties;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.TelephonyProperties;

import com.mediatek.featureoption.FeatureOption;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * broadcast intents
 */
public class DefaultPhoneNotifier implements PhoneNotifier {

    static final String LOG_TAG = "GSM";
    private static final boolean DBG = true;
    private ITelephonyRegistry mRegistry;
    //MTK-START [mtk04070][111125][ALPS00093395]MTK added
    /* Add by vendor for Multiple PDP Context */
    private static final String TAG = "DefaultPhoneNotifier";
    //MTK-END [mtk04070][111125][ALPS00093395]MTK added
    private int mSimId;

    //MTK modified, make it public
    public DefaultPhoneNotifier() {
        mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
    }

    public void notifyPhoneState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        String incomingNumber = "";
        if (ringingCall != null && ringingCall.getEarliestConnection() != null){
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        try {
            mRegistry.notifyCallState(convertCallState(sender.getState()), incomingNumber);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyServiceState(Phone sender) {
        ServiceState ss = sender.getServiceState();
        if (ss == null) {
            ss = new ServiceState();
            ss.setStateOutOfService();
        }
        try {
            mRegistry.notifyServiceState(ss);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifySignalStrength(Phone sender) {
        try {
            mRegistry.notifySignalStrength(sender.getSignalStrength());
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyMessageWaitingChanged(Phone sender) {
        try {
            mRegistry.notifyMessageWaitingChanged(sender.getMessageWaitingIndicator());
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyCallForwardingChanged(Phone sender) {
        try {
            mRegistry.notifyCallForwardingChanged(sender.getCallForwardingIndicator());
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyDataActivity(Phone sender) {
        try {
            mRegistry.notifyDataActivity(convertDataActivityState(sender.getDataActivityState()));
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    //MTK-START [mtk04070][111125][ALPS00093395]MTK modified
    public void notifyDataConnection(Phone sender, String reason, String apnType,
            Phone.DataState state) {

        doNotifyDataConnection(sender, reason, apnType, state);
    }
    //MTK-END [mtk04070][111125][ALPS00093395]MTK modified

    private void doNotifyDataConnection(Phone sender, String reason, String apnType,
            Phone.DataState state) {
        // TODO
        // use apnType as the key to which connection we're talking about.
        // pass apnType back up to fetch particular for this one.
        TelephonyManager telephony = TelephonyManager.getDefault();
        LinkProperties linkProperties = null;
        LinkCapabilities linkCapabilities = null;
        boolean roaming = false;

        if (state == Phone.DataState.CONNECTED) {
            linkProperties = sender.getLinkProperties(apnType);
            linkCapabilities = sender.getLinkCapabilities(apnType);
        }
        ServiceState ss = sender.getServiceState();
        if (ss != null) roaming = ss.getRoaming();

        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            networkType = ((telephony!=null) ? telephony.getNetworkType() : TelephonyManager.NETWORK_TYPE_UNKNOWN);
        } else {
            networkType = ((telephony!=null) ? telephony.getNetworkTypeGemini(sender.getMySimId()) : TelephonyManager.NETWORK_TYPE_UNKNOWN);
        }
        log("doNotifyDataConnection " + "apnType=" + apnType + ",networkType=" + networkType + ", state=" + state);
        notifyDataStateChangeCallback(
                state.toString(),
                reason,
                sender.getActiveApnHost(apnType),
                apnType,
                sender.isDataConnectivityPossible(apnType),
                mSimId);
        try {
            mRegistry.notifyDataConnection(
                    convertDataState(state),
                    sender.isDataConnectivityPossible(apnType),
                    reason,
                    sender.getActiveApnHost(apnType),
                    apnType,
                    linkProperties,
                    linkCapabilities,
                    networkType,
                    roaming);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyDataConnectionFailed(Phone sender, String reason, String apnType) {
        try {
            mRegistry.notifyDataConnectionFailed(reason, apnType);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyCellLocation(Phone sender) {
        Bundle data = new Bundle();
        sender.getCellLocation().fillInNotifierBundle(data);
        try {
            mRegistry.notifyCellLocation(data);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    public void notifyOtaspChanged(Phone sender, int otaspMode) {
        try {
            mRegistry.notifyOtaspChanged(otaspMode);
        } catch (RemoteException ex) {
            // system process is dead
        }
    }

    private void log(String s) {
        Log.d(LOG_TAG, "[PhoneNotifier] " + s);
    }

    /**
     * Convert the {@link State} enum into the TelephonyManager.CALL_STATE_* constants
     * for the public API.
     */
    public static int convertCallState(Phone.State state) {
        switch (state) {
            case RINGING:
                return TelephonyManager.CALL_STATE_RINGING;
            case OFFHOOK:
                return TelephonyManager.CALL_STATE_OFFHOOK;
            default:
                return TelephonyManager.CALL_STATE_IDLE;
        }
    }

    /**
     * Convert the TelephonyManager.CALL_STATE_* constants into the {@link State} enum
     * for the public API.
     */
    public static Phone.State convertCallState(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                return Phone.State.RINGING;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return Phone.State.OFFHOOK;
            default:
                return Phone.State.IDLE;
        }
    }

    /**
     * Convert the {@link DataState} enum into the TelephonyManager.DATA_* constants
     * for the public API.
     */
    public static int convertDataState(Phone.DataState state) {
        switch (state) {
            case CONNECTING:
                return TelephonyManager.DATA_CONNECTING;
            case CONNECTED:
                return TelephonyManager.DATA_CONNECTED;
            case SUSPENDED:
                return TelephonyManager.DATA_SUSPENDED;
            default:
                return TelephonyManager.DATA_DISCONNECTED;
        }
    }

    /**
     * Convert the TelephonyManager.DATA_* constants into {@link DataState} enum
     * for the public API.
     */
    public static Phone.DataState convertDataState(int state) {
        switch (state) {
            case TelephonyManager.DATA_CONNECTING:
                return Phone.DataState.CONNECTING;
            case TelephonyManager.DATA_CONNECTED:
                return Phone.DataState.CONNECTED;
            case TelephonyManager.DATA_SUSPENDED:
                return Phone.DataState.SUSPENDED;
            default:
                return Phone.DataState.DISCONNECTED;
        }
    }

    /**
     * Convert the {@link DataState} enum into the TelephonyManager.DATA_* constants
     * for the public API.
     */
    public static int convertDataActivityState(Phone.DataActivityState state) {
        switch (state) {
            case DATAIN:
                return TelephonyManager.DATA_ACTIVITY_IN;
            case DATAOUT:
                return TelephonyManager.DATA_ACTIVITY_OUT;
            case DATAINANDOUT:
                return TelephonyManager.DATA_ACTIVITY_INOUT;
            case DORMANT:
                return TelephonyManager.DATA_ACTIVITY_DORMANT;
            default:
                return TelephonyManager.DATA_ACTIVITY_NONE;
        }
    }

    /**
     * Convert the TelephonyManager.DATA_* constants into the {@link DataState} enum
     * for the public API.
     */
    public static Phone.DataActivityState convertDataActivityState(int state) {
        switch (state) {
            case TelephonyManager.DATA_ACTIVITY_IN:
                return Phone.DataActivityState.DATAIN;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return Phone.DataActivityState.DATAOUT;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return Phone.DataActivityState.DATAINANDOUT;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return Phone.DataActivityState.DORMANT;
            default:
                return Phone.DataActivityState.NONE;
        }
    }

    //MTK-START [mtk04070][111125][ALPS00093395]MTK proprietary methods
    public DefaultPhoneNotifier(int simId) {
        mSimId = simId;
        if (simId == Phone.GEMINI_SIM_1) {
            mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                        "telephony.registry"));
        } else {
            mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                        "telephony.registry2"));
        }
    }

    public void notifyDataConnection(Phone sender, String reason, String apnType,
            boolean disconnectPdpFlag) {
        // TODO Auto-generated method stub
        TelephonyManager telephony = TelephonyManager.getDefault();
        try {
            /*
            mRegistry.notifyDataConnection(
                    convertDataState(sender.getDataConnectionState()),
                    sender.isDataConnectivityPossible(), reason,
                    sender.getActiveApn(),
                    sender.getActiveApnTypes(),
                    sender.getInterfaceName(null),
                    ((telephony!=null) ? telephony.getNetworkType() :
                    TelephonyManager.NETWORK_TYPE_UNKNOWN),
                    sender.getGateway(null));
            */
            /* getDataConnectionState(apnType) is newly added by vendor */
            /* If apnType is null: getDataConnectionState(null) will invoke DataConnectionTracker's getState() to obtain overall data state */
            Log.e(TAG, "apnType is " + apnType);
            int state = convertDataState(sender.getDataConnectionState(apnType));
            boolean isEnable = sender.isDataConnectivityPossible(apnType);
            String activeApnString = sender.getApnForType(apnType);
            //String[] activeApnTypes = sender.getActiveApnTypes();
            String[] activeApnTypes = apnType == null ? new String[]{} : new String[]{apnType};
            String interfaceName = sender.getInterfaceName(apnType);
            int networkType;

            if (FeatureOption.MTK_GEMINI_SUPPORT != true) {
                networkType = ((telephony!=null) ? telephony.getNetworkType() : TelephonyManager.NETWORK_TYPE_UNKNOWN);
            } else {
                networkType = ((telephony!=null) ? telephony.getNetworkTypeGemini(sender.getMySimId()) : TelephonyManager.NETWORK_TYPE_UNKNOWN);
            }

            /* Add by MTK03594 */
            /*Get gateway according to APN type*/
            String gateway = sender.getGateway(apnType);
            
            Log.e(TAG, "convertDataState(sender.getDataConnectionState(apnType) is " + state);
            Log.e(TAG, "sender.isDataConnectivityPossible(apnType) is " + isEnable);
            Log.e(TAG, "reason is " + reason);
            Log.e(TAG, "sender.getActiveApn() is " + activeApnString);
            Log.e(TAG, "sender.getActiveApnTypes() is " + (apnType == null ? null: activeApnTypes[0]));
            Log.e(TAG, "sender.getInterfaceName(apnType) is " + interfaceName);
            Log.e(TAG, "((telephony!=null) ? telephony.getNetworkType() : TelephonyManager.NETWORK_TYPE_UNKNOWN) is " + networkType);
            mRegistry.notifyAllDataConnection(state, isEnable, reason, activeApnString, activeApnTypes, interfaceName, networkType, disconnectPdpFlag, gateway);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private int networkStringToType(String networkType) {
        int ret = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        if (networkType.equals("GPRS")) {
                ret = TelephonyManager.NETWORK_TYPE_GPRS;
        } else if (networkType.equals("EDGE")) {
                ret = TelephonyManager.NETWORK_TYPE_EDGE;
        } else if (networkType.equals("UMTS")) {
                ret = TelephonyManager.NETWORK_TYPE_UMTS;
        } else if (networkType.equals("HSDPA")) {
                ret = TelephonyManager.NETWORK_TYPE_HSDPA;
        } else if (networkType.equals("HSUPA")) {
                ret = TelephonyManager.NETWORK_TYPE_HSUPA;
        } else if (networkType.equals("HSPA")) {
                ret =TelephonyManager.NETWORK_TYPE_HSPA;
        }

        if (DBG) log("networkStringToType(): networkType=" + networkType + " ret=" + ret);
        return ret;
    }    

    public interface IDataStateChangedCallback {
        void onDataStateChanged(String state, String reason, String apnName,
            String apnType, boolean unavailable, int simId);
    }

    private ArrayList<IDataStateChangedCallback> mDataStateChangedCallbacks = new ArrayList<IDataStateChangedCallback>();

    private void notifyDataStateChangeCallback(String state, String reason, String apnName,
            String apnType, boolean unavailable, int simId)
    {
        Iterator<IDataStateChangedCallback> iterator = mDataStateChangedCallbacks.iterator();
        while (iterator.hasNext()) {
            IDataStateChangedCallback callback = iterator.next();
            callback.onDataStateChanged(state, reason, apnName, apnType, unavailable, simId);
        }
    }

    public void registerDataStateChangeCallback(IDataStateChangedCallback callback) {
        mDataStateChangedCallbacks.add(callback);
    }

    public void unregisterDataStateChangeCallback(IDataStateChangedCallback callback) {
        mDataStateChangedCallbacks.remove(callback);
    }
    //MTK-END [mtk04070][111125][ALPS00093395]MTK proprietary methods
}
