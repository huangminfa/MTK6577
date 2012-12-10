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

import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;

import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.cdma.CDMALTEPhone;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;

//MTK-START [mtk04070][111117][ALPS00093395]MTK added
import android.os.SystemProperties;
import com.android.internal.telephony.gemini.*;
import com.mediatek.featureoption.FeatureOption;
//MTK-END [mtk04070][111117][ALPS00093395]MTK added

/**
 * {@hide}
 */
public class PhoneFactory {
    static final String LOG_TAG = "PHONE";
    static final int SOCKET_OPEN_RETRY_MILLIS = 2 * 1000;
    static final int SOCKET_OPEN_MAX_RETRY = 3;

    //***** Class Variables

    static private Phone sProxyPhone = null;
    static private CommandsInterface sCommandsInterface = null;

    static private boolean sMadeDefaults = false;
    static private PhoneNotifier sPhoneNotifier;
    static private Looper sLooper;
    static private Context sContext;

    static final int preferredCdmaSubscription = RILConstants.PREFERRED_CDMA_SUBSCRIPTION;

    //***** Class Methods

    //MTK-START [mtk04070][111117][ALPS00093395]Use MTKPhoneFactory
    public static void makeDefaultPhones(Context context) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true){
            SystemProperties.set(Phone.GEMINI_DEFAULT_SIM_MODE, String.valueOf(RILConstants.NETWORK_MODE_GEMINI));
        }else{
            SystemProperties.set(Phone.GEMINI_DEFAULT_SIM_MODE, String.valueOf(RILConstants.NETWORK_MODE_WCDMA_PREF));
        }
        
        MTKPhoneFactory.makeDefaultPhone(context);
    }

    /*
     * This function returns the type of the phone, depending
     * on the network mode.
     *
     * @param network mode
     * @return Phone Type
     */
    public static int getPhoneType(int networkMode) {
		return MTKPhoneFactory.getPhoneType(networkMode);
    }

    public static Phone getDefaultPhone() {
		return MTKPhoneFactory.getDefaultPhone();
    }

    public static Phone getCdmaPhone() {
		return MTKPhoneFactory.getCdmaPhone();
    }

    public static Phone getGsmPhone() {
		return MTKPhoneFactory.getGsmPhone();
    }

    public static Phone getCdmaPhone(int simId) {
        return MTKPhoneFactory.getCdmaPhone(simId);
    }

    public static Phone getGsmPhone(int simId) {
        return MTKPhoneFactory.getGsmPhone(simId);
    }

    /**
     * Makes a {@link SipPhone} object.
     * @param sipUri the local SIP URI the phone runs on
     * @return the {@code SipPhone} object or null if the SIP URI is not valid
     */
    public static SipPhone makeSipPhone(String sipUri) {
        return MTKPhoneFactory.getSipPhone(sipUri);
    }

//MTK-START [mtk04258][20120806][International card support]
    public static int getExternalModemSlot() {
        if (FeatureOption.EVDO_DT_SUPPORT) {
            int slot = 0;
            slot = SystemProperties.getInt("ril.external.md", 0);
	    Log.d(LOG_TAG, "getExternalModemSlot slot="+slot);
            return slot - 1;
        } else {
            return -1;
        }
    }

    public static boolean isDualTalkMode() {
        if (FeatureOption.EVDO_DT_SUPPORT)
            return (SystemProperties.getInt("mediatek.gemini", 0) == 0);
        else
            return FeatureOption.MTK_DT_SUPPORT;
    }
//MTK-END [mtk04258][20120806][International card support]
}
