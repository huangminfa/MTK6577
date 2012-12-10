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

package com.android.internal.telephony.gsm;

import android.os.Message;
import android.util.Log;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardApplication;
import com.android.internal.telephony.IccConstants;
import com.android.internal.telephony.IccFileHandler;
import com.android.internal.telephony.Phone;

/**
 * {@hide}
 */
public final class SIMFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "GSM";
    private Phone mPhone;

    //***** Instance Variables

    //***** Constructor

    SIMFileHandler(GSMPhone phone) {
        super(phone);
        mPhone = phone;
    }

    public void dispose() {
        super.dispose();
    }

    protected void finalize() {
        Log.d(LOG_TAG, "SIMFileHandler finalized");
    }

    //***** Overridden from IccFileHandler

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }

    protected String getEFPath(int efid) {
		return getEFPath(efid, false);
	}
    protected String getEFPath(int efid, boolean is7FFF) {
        // TODO(): DF_GSM can be 7F20 or 7F21 to handle backward compatibility.
        // Implement this after discussion with OEMs.
        IccCard card = phone.getIccCard();
        String DF_APP = DF_GSM;

        if ((card != null) && (card.isApplicationOnIcc(IccCardApplication.AppType.APPTYPE_USIM))) {
            DF_APP = DF_USIM;
        }
        
        switch(efid) {
        case EF_ICCID:
            return null;
        case EF_EXT6:
        case EF_MWIS:
        case EF_MBI:
        case EF_SPN:
        case EF_AD:
        case EF_MBDN:
        case EF_PNN:
        case EF_SPDI:
        case EF_SST:
        case EF_CFIS:
            return /*MF_SIM +*/ DF_APP;

        case EF_MAILBOX_CPHS:
        case EF_VOICE_MAIL_INDICATOR_CPHS:
        case EF_CFF_CPHS:
        case EF_SPN_CPHS:
        case EF_SPN_SHORT_CPHS:
        case EF_INFO_CPHS:
        case EF_CSP_CPHS:
            return /*MF_SIM +*/ DF_GSM; // ALPS00355093
        case EF_GID1:
        case EF_GID2:     
        case EF_ECC:
        case EF_OPL:
            return /*MF_SIM +*/ DF_APP;
        case EF_PBR:
            // we only support global phonebook.
            return /*MF_SIM +*/ (is7FFF? DF_USIM : DF_TELECOM) + DF_PHONEBOOK;
        }
        String path = getCommonIccEFPath(efid);
        if (path == null) {
            // The EFids in USIM phone book entries are decided by the card manufacturer.
            // So if we don't match any of the cases above and if its a USIM return
            // the phone book path.
            path = "";
            if (card != null) {
                switch(efid) {
                case EF_FDN:
                case EF_MSISDN:
                case EF_SDN:
                case EF_EXT2:
                case EF_EXT3:
                case EF_SMS:
                    if (card.isApplicationOnIcc(IccCardApplication.AppType.APPTYPE_USIM)) {
                        return DF_APP;
                    } else {
                        return DF_TELECOM;
                    }
                }
                return /*MF_SIM +*/ DF_TELECOM + DF_PHONEBOOK;
            }
            Log.e(LOG_TAG, "Error: EF Path being returned in null");
        }
        return path;
    }

    protected void logd(String msg) {
        Log.d(LOG_TAG, "[SIMFileHandler] " + msg);
    }

    protected void loge(String msg) {
        Log.e(LOG_TAG, "[SIMFileHandler] " + msg);
    }
}
