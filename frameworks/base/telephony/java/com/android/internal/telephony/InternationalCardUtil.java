/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

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

package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.gsm.SpnOverride;

final public class InternationalCardUtil {
    public static final String LOG_TAG = "InternationalCardUtil";
    public static final String MCC_CHINA = "460";
    public static final String MCC_MACAO = "455";

    public static final int CARD_TYPE_UNKNOWN                      = 0;
    public static final int CARD_TYPE_CHINA_UNICOM_SINGLE_CDMA     = 1;
    public static final int CARD_TYPE_CHINA_TELECOM_DUAL_MODE      = 2;
    public static final int CARD_TYPE_GENERIC_SINGLE_CDMA          = 3;
    public static final int CARD_TYPE_GENERIC_DUAL_MODE            = 4;
    public static final int CARD_TYPE_GSM                          = 5;

    public static final int NETWORK_TECH_MODE_INTERNATIONAL = 0;
    public static final int NETWORK_TECH_MODE_CDMA_ONLY     = 1;
    public static final int NETWORK_TECH_MODE_GSM_ONLY      = 2;

    public static int[] cardType = new int[2];
    public static int[] networkMode = new int[2];
    public static int[] userSelectedMode = new int[2];
    public static boolean performingCDMA = (SystemProperties.getInt("mediatek.gemini", 0) == 0);

	static public void init(Context context) {
	    cardType[0] = Settings.System.getInt(context.getContentResolver(),
                       "gsm.internationalcard.sim0.type", 0);
		cardType[1] = Settings.System.getInt(context.getContentResolver(),
                       "gsm.internationalcard.sim1.type", 0);
    }


    static private boolean isChinaCDMACard(String cdmaIMEI) {
        return cdmaIMEI.startsWith("46003") || cdmaIMEI.startsWith("45502");
    }
    
    /**
     * Is the ICC card supports dual-mode
     */
    static public boolean isInternationalCardInserted(int simId) {
        return cardType[simId] == CARD_TYPE_CHINA_TELECOM_DUAL_MODE
                || cardType[simId] == CARD_TYPE_GENERIC_DUAL_MODE;
    }

    public static int getCardType(int simId) {
        return cardType[simId];
    }
    
    public static void updateCardType(Context context, int simId, String cdmaIMSI, String gsmIMSI) {
        cardType[simId] = parseCardType(cdmaIMSI, gsmIMSI);
        Settings.System.putInt(context.getContentResolver(),
                            "gsm.internationalcard.sim"+simId+".type",
                            cardType[simId]);
        Log.d(LOG_TAG, "updateCardType simId:" + simId +
			           ", cdmaIMSI: " + cdmaIMSI +
			           ", gsmIMSI: " + gsmIMSI + 
			           ", cardType[simId]:" + cardType[simId]);
    }

    /**
        * Get the current ICC type of the 5 card typies
        */
    public static int parseCardType(String cdmaIMSI, String gsmIMSI) {
        String gsmMCCMNC;
        if (TextUtils.isEmpty(cdmaIMSI) && !TextUtils.isEmpty(gsmIMSI)) {
            return CARD_TYPE_GSM;
        } else if (!TextUtils.isEmpty(cdmaIMSI) && TextUtils.isEmpty(gsmIMSI)) {
            return CARD_TYPE_GENERIC_SINGLE_CDMA;
        } else if (!TextUtils.isEmpty(cdmaIMSI) && !TextUtils.isEmpty(gsmIMSI)) {
            SpnOverride spnOverride = SpnOverride.getInstance();
            if (isChinaCDMACard(cdmaIMSI)) {
                gsmMCCMNC = gsmIMSI.substring(0,5);
				Log.d(LOG_TAG, "parseCardType gsmMCCMNC:" + gsmMCCMNC);
                if (gsmMCCMNC.equals("46099")||
                    gsmIMSI.equals(cdmaIMSI) ||
                    !spnOverride.containsCarrier(gsmMCCMNC)) {
                    return CARD_TYPE_CHINA_UNICOM_SINGLE_CDMA;
                } else {
                    return CARD_TYPE_CHINA_TELECOM_DUAL_MODE;
                }
            } else {
                return CARD_TYPE_GENERIC_DUAL_MODE;
            }
        } else {
            return CARD_TYPE_UNKNOWN;
        }
    }

    public static void setNetworkTechMode(int simId, int mode) {
        networkMode[simId] = mode;
    }

    public static int getNetworkTechMode(int simId) {
        return networkMode[simId];
    }

    public static void setUserSelectedMode(Context context, int simId, int mode) {
        userSelectedMode[simId] = mode;
        Settings.System.putInt(context.getContentResolver(),
                "gsm.internationalcard.network.mode.sim"+simId, mode);
    }

    public static int getUserSelectedMode(Context context, int simId) {
        if (simId == -1) return NETWORK_TECH_MODE_CDMA_ONLY;
        userSelectedMode[simId] = Settings.System.getInt(context.getContentResolver(),
                "gsm.internationalcard.network.mode.sim"+simId, 0);
        return userSelectedMode[simId];
    }

    public static boolean needSecondNWSelect(String registeredPLMN) {
        return registeredPLMN.startsWith(MCC_CHINA) || registeredPLMN.startsWith(MCC_MACAO);
    }

    public static void setDualSimMode(boolean dualTalkSUpport) {
        performingCDMA = dualTalkSUpport;
    }
}
