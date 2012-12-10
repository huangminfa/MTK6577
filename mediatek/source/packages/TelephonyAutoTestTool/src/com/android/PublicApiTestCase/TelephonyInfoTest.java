/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.android.PublicApiTestCase;

import java.util.List;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import com.android.FunctionTest.TestCase;
import com.android.internal.telephony.ITelephony;

public class TelephonyInfoTest extends TestCase {

    static private ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager
            .getService("phone"));

    public TelephonyInfoTest() {
        super(null);
    }

    public TelephonyInfoTest(Handler reportHander) {
        super(reportHander);
    }

    @Override
    public void setup() {
        reportMessage("setup");
        mAborted = false;
    }

    @Override
    public void run() {
        String defaultApn = "default";
        
        TelephonyManager tm = TelephonyManager.getDefault();
        
        String strRes = tm.getDeviceSoftwareVersion();
        reportMessage("telephonyManager.getDeviceSoftwareVersion()=" + strRes);
        strRes = tm.getDeviceId();
        reportMessage("telephonyManager.getDeviceId()=" + strRes);
        CellLocation cl = tm.getCellLocation();
        reportMessage("telephonyManager.getCellLocation()=" + cl.toString());
        List<NeighboringCellInfo> ncInfo = tm.getNeighboringCellInfo();
        reportMessage("telephonyManager.getNeighboringCellInfo()=");
        for (NeighboringCellInfo info : ncInfo) {
            reportMessage("info.toString()" + info.toString());
        }
        
        int intRes = tm.getCurrentPhoneType();
        reportMessage("telephonyManager.getCurrentPhoneType()=" + intRes);
        intRes = tm.getPhoneType();
        reportMessage("telephonyManager.(getPhoneType)=" + intRes);
        strRes = tm.getNetworkOperatorName();
        reportMessage("telephonyManager.getNetworkOperatorName()=" + strRes);
        strRes = tm.getNetworkOperator();
        reportMessage("telephonyManager.getNetworkOperator()=" + strRes);
        strRes = tm.getNetworkCountryIso();
        reportMessage("telephonyManager.getNetworkCountryIso()=" + strRes);
        intRes = tm.getNetworkType();
        reportMessage("telephonyManager.getNetworkType()=" + intRes);
        intRes = TelephonyManager.getNetworkClass(TelephonyManager.NETWORK_TYPE_HSPA);
        reportMessage("static TelephonyManager.getNetworkClass(TelephonyManager.NETWORK_TYPE_HSPA)="
                + intRes);
        strRes = tm.getNetworkTypeName();
        reportMessage("telephonyManager.getNetworkTypeName()=" + strRes);
        strRes = TelephonyManager.getNetworkTypeName(TelephonyManager.NETWORK_TYPE_HSPA);
        reportMessage("static TelephonyManager.getNetworkTypeName(TelephonyManager.NETWORK_TYPE_HSPA)="
                + strRes);
        intRes = tm.getSimState();
        reportMessage("telephonyManager.(getSimState)=" + intRes);
        strRes = tm.getSimOperator();
        reportMessage("telephonyManager.getSimOperator()=" + strRes);
        strRes = tm.getSimOperatorName();
        reportMessage("telephonyManager.getSimOperatorName()=" + strRes);
        strRes = tm.getSimCountryIso();
        reportMessage("telephonyManager.getSimCountryIso()=" + strRes);
        strRes = tm.getSimSerialNumber();
        reportMessage("telephonyManager.getSimSerialNumber()=" + strRes);
        intRes = tm.getLteOnCdmaMode();
        reportMessage("telephonyManager.getLteOnCdmaMode()=" + intRes);
        strRes = tm.getSubscriberId();
        reportMessage("telephonyManager.getSubscriberId()=" + strRes);
        strRes = tm.getLine1Number();
        reportMessage("telephonyManager.getLine1Number()=" + strRes);
        strRes = tm.getLine1AlphaTag();
        reportMessage("telephonyManager.getLine1AlphaTag()=" + strRes);
        strRes = tm.getMsisdn();
        reportMessage("telephonyManager.getMsisdn()=" + strRes);
        strRes = tm.getVoiceMailNumber();
        reportMessage("telephonyManager.getVoiceMailNumber()=" + strRes);
        strRes = tm.getCompleteVoiceMailNumber();
        reportMessage("telephonyManager.getCompleteVoiceMailNumber()=" + strRes);
        intRes = tm.getVoiceMessageCount();
        reportMessage("telephonyManager.getVoiceMessageCount()=" + intRes);
        strRes = tm.getVoiceMailAlphaTag();
        reportMessage("telephonyManager.getVoiceMailAlphaTag()=" + strRes);
        strRes = tm.getIsimImpi();
        reportMessage("telephonyManager.getIsimImpi()=" + strRes);
        strRes = tm.getIsimDomain();
        reportMessage("telephonyManager.getIsimDomain()=" + strRes);
        String[] arstr = tm.getIsimImpu();
        reportMessage("telephonyManager.getIsimImpu()=");
        for (String info : arstr) {
            reportMessage("=" + info);
        }

        intRes = tm.getCallState();
        reportMessage("telephonyManager.getCallState()=" + intRes);
        intRes = tm.getDataActivity();
        reportMessage("telephonyManager.getDataActivity()=" + intRes);
        intRes = tm.getDataState();
        reportMessage("telephonyManager.getDataState()=" + intRes);
        intRes = tm.getCdmaEriIconIndex();
        reportMessage("telephonyManager.getCdmaEriIconIndex()=" + intRes);
        intRes = tm.getCdmaEriIconMode();
        reportMessage("telephonyManager.getCdmaEriIconMode()=" + intRes);
        strRes = tm.getCdmaEriText();
        reportMessage("telephonyManager.getCdmaEriText()=" + strRes);
        strRes = tm.getDeviceIdGemini(0);
        reportMessage("telephonyManager.getDeviceIdGemini(0)=" + strRes);
        CellLocation clres = tm.getCellLocationGemini(0);
        reportMessage("telephonyManager.getCellLocationGemini(0)=" + clres.toString());
        ncInfo = tm.getNeighboringCellInfoGemini(0);
        reportMessage("telephonyManager.getNeighboringCellInfoGemini(0)=");
        for (NeighboringCellInfo info : ncInfo) {
            reportMessage("info.toString()" + info.toString());
        }
        reportMessage("telephonyManager.getNeighboringCellInfoGemini(0)=" + strRes);
        intRes = tm.getPhoneTypeGemini(0);
        reportMessage("telephonyManager.getPhoneTypeGemini(0)=" + intRes);
        strRes = tm.getNetworkOperatorNameGemini(0);
        reportMessage("telephonyManager.getNetworkOperatorNameGemini(0)=" + strRes);
        strRes = tm.getNetworkOperatorGemini(0);
        reportMessage("telephonyManager.getNetworkOperatorGemini(0)=" + strRes);
        strRes = tm.getNetworkCountryIsoGemini(0);
        reportMessage("telephonyManager.getNetworkCountryIsoGemini(0)=" + strRes);
        intRes = tm.getNetworkTypeGemini(0);
        reportMessage("telephonyManager.getNetworkTypeGemini(0)=" + intRes);
        strRes = tm.getNetworkTypeNameGemini(0);
        reportMessage("telephonyManager.getNetworkTypeNameGemini(0)=" + strRes);
        intRes = tm.getSimStateGemini(0);
        reportMessage("telephonyManager.getSimStateGemini(0)=" + intRes);
        strRes = tm.getSimOperatorGemini(0);
        reportMessage("telephonyManager.getSimOperatorGemini(0)=" + strRes);
        strRes = tm.getSimOperatorNameGemini(0);
        reportMessage("telephonyManager.getSimOperatorNameGemini(0)=" + strRes);
        strRes = tm.getSimCountryIsoGemini(0);
        reportMessage("telephonyManager.getSimCountryIsoGemini(0)=" + strRes);
        strRes = tm.getSimSerialNumberGemini(0);
        reportMessage("telephonyManager.getSimSerialNumberGemini(0)=" + strRes);
        strRes = tm.getSubscriberIdGemini(0);
        reportMessage("telephonyManager.getSubscriberIdGemini(0)=" + strRes);
        strRes = tm.getLine1NumberGemini(0);
        reportMessage("telephonyManager.getLine1NumberGemini(0)=" + strRes);
        strRes = tm.getLine1AlphaTagGemini(0);
        reportMessage("telephonyManager.getLine1AlphaTagGemini(0)=" + strRes);
        strRes = tm.getVoiceMailNumberGemini(0);
        reportMessage("telephonyManager.getVoiceMailNumberGemini(0)=" + strRes);
        intRes = tm.getVoiceMessageCountGemini(0);
        reportMessage("telephonyManager.getVoiceMessageCountGemini(0)=" + intRes);
        strRes = tm.getVoiceMailAlphaTagGemini(0);
        reportMessage("telephonyManager.getVoiceMailAlphaTagGemini(0)=" + strRes);
        strRes = tm.getIccCardType();
        reportMessage("telephonyManager.getIccCardType()=" + strRes);
        intRes = tm.getCallStateGemini(0);
        reportMessage("telephonyManager.getCallStateGemini(0)=" + intRes);
        intRes = tm.getDataStateGemini(0);
        reportMessage("telephonyManager.getDataStateGemini(0)=" + intRes);
        strRes = tm.getIccCardTypeGemini(0);
        reportMessage("telephonyManager.getIccCardTypeGemini(0)=" + strRes);
        strRes = tm.getSN();
        reportMessage("telephonyManager.getSN()=" + strRes);
        //void getMobileRevisionAndIMEI(int type, Message message);
        intRes = tm.getSmsDefaultSim();
        reportMessage("telephonyManager.getSmsDefaultSim()=" + intRes);
        strRes = tm.getDeviceIdGemini(1);
        reportMessage("telephonyManager.getDeviceIdGemini(1)=" + strRes);
        cl = tm.getCellLocationGemini(1);
        reportMessage("telephonyManager.getCellLocation(1)=" + cl.toString());
        reportMessage("telephonyManager.getCellLocationGemini(1)=" + strRes);
        ncInfo = tm.getNeighboringCellInfoGemini(1);
        reportMessage("telephonyManager.getNeighboringCellInfoGemini(1)=");
        for (NeighboringCellInfo info : ncInfo) {
            reportMessage("info.toString()" + info.toString());
        }
        reportMessage("telephonyManager.getNeighboringCellInfoGemini(1)=" + strRes);
        intRes = tm.getPhoneTypeGemini(1);
        reportMessage("telephonyManager.getPhoneTypeGemini(1)=" + intRes);
        strRes = tm.getNetworkOperatorNameGemini(1);
        reportMessage("telephonyManager.getNetworkOperatorNameGemini(1)=" + strRes);
        strRes = tm.getNetworkOperatorGemini(1);
        reportMessage("telephonyManager.getNetworkOperatorGemini(1)=" + strRes);
        strRes = tm.getNetworkCountryIsoGemini(1);
        reportMessage("telephonyManager.getNetworkCountryIsoGemini(1)=" + strRes);
        intRes = tm.getNetworkTypeGemini(1);
        reportMessage("telephonyManager.getNetworkTypeGemini(1)=" + intRes);
        strRes = tm.getNetworkTypeNameGemini(1);
        reportMessage("telephonyManager.getNetworkTypeNameGemini(1)=" + strRes);
        intRes = tm.getSimStateGemini(1);
        reportMessage("telephonyManager.getSimStateGemini(1)=" + intRes);
        strRes = tm.getSimOperatorGemini(1);
        reportMessage("telephonyManager.getSimOperatorGemini(1)=" + strRes);
        strRes = tm.getSimOperatorNameGemini(1);
        reportMessage("telephonyManager.getSimOperatorNameGemini(1)=" + strRes);
        strRes = tm.getSimCountryIsoGemini(1);
        reportMessage("telephonyManager.getSimCountryIsoGemini(1)=" + strRes);
        strRes = tm.getSimSerialNumberGemini(1);
        reportMessage("telephonyManager.getSimSerialNumberGemini(1)=" + strRes);
        strRes = tm.getSubscriberIdGemini(1);
        reportMessage("telephonyManager.getSubscriberIdGemini(1)=" + strRes);
        strRes = tm.getLine1NumberGemini(1);
        reportMessage("telephonyManager.getLine1NumberGemini(1)=" + strRes);
        strRes = tm.getLine1AlphaTagGemini(1);
        reportMessage("telephonyManager.getLine1AlphaTagGemini(1)=" + strRes);
        strRes = tm.getVoiceMailNumberGemini(1);
        reportMessage("telephonyManager.getVoiceMailNumberGemini(1)=" + strRes);
        intRes = tm.getVoiceMessageCountGemini(1);
        reportMessage("telephonyManager.getVoiceMessageCountGemini(1)=" + intRes);
        strRes = tm.getVoiceMailAlphaTagGemini(1);
        reportMessage("telephonyManager.getVoiceMailAlphaTagGemini(1)=" + strRes);
        intRes = tm.getCallStateGemini(1);
        reportMessage("telephonyManager.getCallStateGemini(1)=" + intRes);
        intRes = tm.getDataStateGemini(1);
        reportMessage("telephonyManager.getDataStateGemini(1)=" + intRes);
        strRes = tm.getIccCardTypeGemini(1);
           reportMessage("telephonyManager.getIccCardTypeGemini(1)=" + strRes);
        reportMessage("tm.isNetworkRoaming()=" + tm.isNetworkRoaming());
        reportMessage("tm.isVoiceCapable()=" + tm.isVoiceCapable());
        reportMessage("tm.isSmsCapable()=" + tm.isSmsCapable());
        reportMessage("tm.isNetworkRoamingGemini(0)=" + tm.isNetworkRoamingGemini(0));
            reportMessage("tm.isNetworkRoamingGemini(1)=" + tm.isNetworkRoamingGemini(1));
        reportMessage("tm.isVoiceCapable()=" + tm.isVoiceCapable());
        reportMessage("tm.isSmsCapable()=" + tm.isSmsCapable());
        reportMessage("tm.isNetworkRoamingGemini(0)=" + tm.isNetworkRoamingGemini(0));
        reportMessage("tm.isNetworkRoamingGemini(1)=" + tm.isNetworkRoamingGemini(1));

        
        //String strResult = iTelephony.getInterfaceDescriptor();//
        
        try {
            android.os.Bundle bundleResult = iTelephony.getCellLocation();
            
            List<android.telephony.NeighboringCellInfo> resultCellInfo = iTelephony.getNeighboringCellInfo();
            reportMessage("iTelephony.NeighboringCellInfo");
            for (android.telephony.NeighboringCellInfo info : resultCellInfo) {
                reportMessage("info.toString()" + info.toString());
            }

            int intResult = iTelephony.getCallState();
            reportMessage("iTelephony.getCallState() = " + intResult);

            intResult = iTelephony.getDataActivity();
            reportMessage("iTelephony.getDataActivity() = " + intResult);

            intResult = iTelephony.getDataState();
            reportMessage("iTelephony.getDataState() = " + intResult);

            intResult = iTelephony.getActivePhoneType();
            reportMessage("iTelephony.getActivePhoneType() = " + intResult);

            intResult = iTelephony.getCdmaEriIconIndex();
            reportMessage("iTelephony.getCdmaEriIconIndex() = " + intResult);

            intResult = iTelephony.getCdmaEriIconMode();
            reportMessage("iTelephony.getCdmaEriIconMode() = " + intResult);

            String strResult = iTelephony.getCdmaEriText();
            reportMessage("iTelephony.getCdmaEriText() = " + strResult);

            intResult = iTelephony.getVoiceMessageCount();
            reportMessage("iTelephony.getVoiceMessageCount() = " + intResult);

            intResult = iTelephony.getNetworkType();
            reportMessage("iTelephony.getNetworkType() = " + intResult);

            intResult = iTelephony.getLteOnCdmaMode();
            reportMessage("iTelephony.getLteOnCdmaMode() = " + intResult);

            strResult = iTelephony.getIccCardType();
            reportMessage("iTelephony.getIccCardType() = " + strResult);

            intResult = iTelephony.getPreciseCallState();
            reportMessage("iTelephony.getPreciseCallState() = " + intResult);

            intResult = iTelephony.getPendingMmiCodesGemini(0);
            reportMessage("iTelephony.getPendingMmiCodesGemini(0) = " + intResult);

            intResult = iTelephony.getCallStateGemini(0);
            reportMessage("iTelephony.getCallStateGemini(0) = " + intResult);

            intResult = iTelephony.getActivePhoneTypeGemini(0);
            reportMessage("iTelephony.getActivePhoneTypeGemini(0) = " + intResult);

            strResult = iTelephony.getIccCardTypeGemini(0);
            reportMessage("iTelephony.getIccCardTypeGemini(0) = " + strResult);

            bundleResult = iTelephony.getCellLocationGemini(0);

            resultCellInfo = iTelephony.getNeighboringCellInfoGemini(0);
            reportMessage("iTelephony.NeighboringCellInfo(0)");
            for (android.telephony.NeighboringCellInfo info : resultCellInfo) {
                reportMessage("info.toString()" + info.toString());
            }
            resultCellInfo = iTelephony.getNeighboringCellInfoGemini(1);
            reportMessage("iTelephony.NeighboringCellInfo(1)");
            for (android.telephony.NeighboringCellInfo info : resultCellInfo) {
                reportMessage("info.toString()" + info.toString());
            }

            // void voidResult = iTelephony.getMobileRevisionAndIMEI(int type,
            // android.os.Message message);

            strResult = iTelephony.getSN();
            reportMessage("iTelephony.getSN() = " + strResult);

            intResult = iTelephony.getNetworkTypeGemini(0);
            reportMessage("iTelephony.getNetworkTypeGemini(0) = " + intResult);
            intResult = iTelephony.getNetworkTypeGemini(1);
            reportMessage("iTelephony.getNetworkTypeGemini(1) = " + intResult);

            intResult = iTelephony.getDataStateGemini(0);
            reportMessage("iTelephony.getDataStateGemini(0) = " + intResult);
            intResult = iTelephony.getDataStateGemini(1);
            reportMessage("iTelephony.getDataStateGemini(1) = " + intResult);

            intResult = iTelephony.getDataActivityGemini(0);
            reportMessage("iTelephony.getDataActivityGemini(0) = " + intResult);
            intResult = iTelephony.getDataActivityGemini(1);
            reportMessage("iTelephony.getDataActivityGemini(1) = " + intResult);

            intResult = iTelephony.getVoiceMessageCountGemini(0);
            reportMessage("iTelephony.getVoiceMessageCountGemini(0) = " + intResult);
            intResult = iTelephony.getVoiceMessageCountGemini(1);
            reportMessage("iTelephony.getVoiceMessageCountGemini(1) = " + intResult);

            intResult = iTelephony.getSimIndicatorState();
            reportMessage("iTelephony.getSimIndicatorState() = " + intResult);

            intResult = iTelephony.getSimIndicatorStateGemini(0);
            reportMessage("iTelephony.getSimIndicatorStateGemini(0) = " + intResult);
            intResult = iTelephony.getSimIndicatorStateGemini(1);
            reportMessage("iTelephony.getSimIndicatorStateGemini(1) = " + intResult);

            bundleResult = iTelephony.getServiceState();
            // bundleResult.getInt(key)

            bundleResult = iTelephony.getServiceStateGemini(0);

            strResult = iTelephony.getScAddressGemini(0);
            reportMessage("iTelephony.getScAddressGemini(0) = " + strResult);
            strResult = iTelephony.getScAddressGemini(1);
            reportMessage("iTelephony.getScAddressGemini(1) = " + strResult);

            intResult = iTelephony.getSmsDefaultSim();
            reportMessage("iTelephony.getSmsDefaultSim() = " + intResult);

            intResult = iTelephony.get3GCapabilitySIM();
            reportMessage("iTelephony.get3GCapabilitySIM() = " + intResult);

            strResult = iTelephony.getInterfaceName(defaultApn);
            reportMessage("iTelephony.getInterfaceName() = " + strResult);

            strResult = iTelephony.getIpAddress(defaultApn);
            reportMessage("iTelephony.getIpAddress() = " + strResult);

            strResult = iTelephony.getGateway(defaultApn);
            reportMessage("iTelephony.getGateway() = " + strResult);

            strResult = iTelephony.getInterfaceNameGemini(defaultApn, 0);
            reportMessage("iTelephony.getInterfaceNameGemini(0) = " + strResult);
            strResult = iTelephony.getInterfaceNameGemini(defaultApn, 1);
            reportMessage("iTelephony.getInterfaceNameGemini(1) = " + strResult);

            strResult = iTelephony.getIpAddressGemini(defaultApn, 0);
            reportMessage("iTelephony.getIpAddressGemini(0) = " + strResult);
            strResult = iTelephony.getIpAddressGemini(defaultApn, 1);
            reportMessage("iTelephony.getIpAddressGemini(1) = " + strResult);

            strResult = iTelephony.getGatewayGemini(defaultApn, 0);
            reportMessage("iTelephony.getGatewayGemini(0) = " + strResult);
            strResult = iTelephony.getGatewayGemini(defaultApn, 1);
            reportMessage("iTelephony.getGatewayGemini(1) = " + strResult);

            int[] ArResult = iTelephony.getAdnStorageInfo(0);
            reportMessage("iTelephony.getAdnStorageInfo(0)");
            for (int info : ArResult) {
                reportMessage("info = " + info);
            }
            ArResult = iTelephony.getAdnStorageInfo(1);
            reportMessage("iTelephony.getAdnStorageInfo(1)");
            for (int info : ArResult) {
                reportMessage("info = " + info);
            }
            
            reportMessage("iTelephony.isOffhook() = " + iTelephony.isOffhook());
            reportMessage("iTelephony.isRinging() = " + iTelephony.isRinging());
            reportMessage("iTelephony.isIdle() = " + iTelephony.isIdle());
            reportMessage("iTelephony.isRadioOn() = " + iTelephony.isRadioOn());
            reportMessage("iTelephony.isSimPinEnabled() = " + iTelephony.isSimPinEnabled());
            reportMessage("iTelephony.isDataConnectivityPossible() = " + iTelephony.isDataConnectivityPossible());
            reportMessage("iTelephony.isVoiceIdle() = " + iTelephony.isVoiceIdle());
            reportMessage("iTelephony.isTestIccCard() = " + iTelephony.isTestIccCard());
            reportMessage("iTelephony.isFDNEnabled() = " + iTelephony.isFDNEnabled());
            reportMessage("iTelephony.isOffhookGemini(0) = " + iTelephony.isOffhookGemini(0));
            reportMessage("iTelephony.isRingingGemini(0) = " + iTelephony.isRingingGemini(0));
            reportMessage("iTelephony.isIdleGemini(0) = " + iTelephony.isIdleGemini(0));
            reportMessage("iTelephony.isRadioOnGemini(0) = " + iTelephony.isRadioOnGemini(0));
            reportMessage("iTelephony.isSimInsert(0) = " + iTelephony.isSimInsert(0));
            reportMessage("iTelephony.isTestIccCardGemini(0) = "
                    + iTelephony.isTestIccCardGemini(0));
            reportMessage("iTelephony.isDataConnectivityPossibleGemini(0) = "
                    + iTelephony.isDataConnectivityPossibleGemini(0));
            reportMessage("iTelephony.isFDNEnabledGemini(0) = " + iTelephony.isFDNEnabledGemini(0));
            reportMessage("iTelephony.isOffhookGemini(1) = " + iTelephony.isOffhookGemini(1));
            reportMessage("iTelephony.isRingingGemini(1) = " + iTelephony.isRingingGemini(1));
            reportMessage("iTelephony.isIdleGemini(1) = " + iTelephony.isIdleGemini(1));
            reportMessage("iTelephony.isRadioOnGemini(1) = " + iTelephony.isRadioOnGemini(1));
            reportMessage("iTelephony.isSimInsert(1) = " + iTelephony.isSimInsert(1));
            reportMessage("iTelephony.isTestIccCardGemini(1) = "
                    + iTelephony.isTestIccCardGemini(1));
            reportMessage("iTelephony.isDataConnectivityPossibleGemini(1) = "
                    + iTelephony.isDataConnectivityPossibleGemini(1));
            reportMessage("iTelephony.isFDNEnabledGemini(1) = " + iTelephony.isFDNEnabledGemini(1));
            reportMessage("iTelephony.isVTIdle() = " + iTelephony.isVTIdle());
            reportMessage("iTelephony.isPhbReady() = " + iTelephony.isPhbReady());
            reportMessage("iTelephony.isPhbReadyGemini() = " + iTelephony.isPhbReadyGemini(0));
            reportMessage("iTelephony.isPhbReadyGemini() = " + iTelephony.isPhbReadyGemini(1));
            reportMessage("iTelephony.isRejectAllVoiceCall() = " + iTelephony.isRejectAllVoiceCall());
            reportMessage("iTelephony.isRejectAllVideoCall() = " + iTelephony.isRejectAllVideoCall());
            reportMessage("iTelephony.isRejectAllSIPCall() = " + iTelephony.isRejectAllSIPCall());
            reportMessage("iTelephony.is3GSwitchLocked() = " + iTelephony.is3GSwitchLocked());
            reportMessage("iTelephony.isOffhook() = " + iTelephony.isOffhook());
            reportMessage("iTelephony.isRinging() = " + iTelephony.isRinging());
            reportMessage("iTelephony.isIdle() = " + iTelephony.isIdle());
            reportMessage("iTelephony.isRadioOn() = " + iTelephony.isRadioOn());
            reportMessage("iTelephony.isSimPinEnabled() = " + iTelephony.isSimPinEnabled());
            reportMessage("iTelephony.isDataConnectivityPossible() = " + iTelephony.isDataConnectivityPossible());
            reportMessage("iTelephony.isVoiceIdle() = " + iTelephony.isVoiceIdle());
            reportMessage("iTelephony.isTestIccCard() = " + iTelephony.isTestIccCard());
            reportMessage("iTelephony.isFDNEnabled() = " + iTelephony.isFDNEnabled());
            reportMessage("iTelephony.isOffhookGemini(0) = " + iTelephony.isOffhookGemini(0));
            reportMessage("iTelephony.isRingingGemini(0) = " + iTelephony.isRingingGemini(0));
            reportMessage("iTelephony.isIdleGemini(0) = " + iTelephony.isIdleGemini(0));
            reportMessage("iTelephony.isRadioOnGemini(0) = " + iTelephony.isRadioOnGemini(0));
            reportMessage("iTelephony.isSimInsert(0) = " + iTelephony.isSimInsert(0));
            reportMessage("iTelephony.isTestIccCardGemini(0) = "
                    + iTelephony.isTestIccCardGemini(0));
            reportMessage("iTelephony.isDataConnectivityPossibleGemini(0) = "
                    + iTelephony.isDataConnectivityPossibleGemini(0));
            reportMessage("iTelephony.isFDNEnabledGemini(0) = " + iTelephony.isFDNEnabledGemini(0));
            reportMessage("iTelephony.isOffhookGemini(1) = " + iTelephony.isOffhookGemini(1));
            reportMessage("iTelephony.isRingingGemini(1) = " + iTelephony.isRingingGemini(1));
            reportMessage("iTelephony.isIdleGemini(1) = " + iTelephony.isIdleGemini(1));
            reportMessage("iTelephony.isRadioOnGemini(1) = " + iTelephony.isRadioOnGemini(1));
            reportMessage("iTelephony.isSimInsert(1) = " + iTelephony.isSimInsert(1));
            reportMessage("iTelephony.isTestIccCardGemini(1) = "
                    + iTelephony.isTestIccCardGemini(1));
            reportMessage("iTelephony.isDataConnectivityPossibleGemini(1) = "
                    + iTelephony.isDataConnectivityPossibleGemini(1));
            reportMessage("iTelephony.isFDNEnabledGemini(1) = " + iTelephony.isFDNEnabledGemini(1));
            reportMessage("iTelephony.isVTIdle() = " + iTelephony.isVTIdle());
            reportMessage("iTelephony.isPhbReady() = " + iTelephony.isPhbReady());
            reportMessage("iTelephony.isPhbReadyGemini(0) = " + iTelephony.isPhbReadyGemini(0));
            reportMessage("iTelephony.isPhbReadyGemini(0) = " + iTelephony.isPhbReadyGemini(1));
            reportMessage("iTelephony.isRejectAllVoiceCall() = " + iTelephony.isRejectAllVoiceCall());
            reportMessage("iTelephony.isRejectAllVideoCall() = " + iTelephony.isRejectAllVideoCall());
            reportMessage("iTelephony.isRejectAllSIPCall() = " + iTelephony.isRejectAllSIPCall());
            reportMessage("iTelephony.is3GSwitchLocked() = " + iTelephony.is3GSwitchLocked());

        } catch (RemoteException e) {

            e.printStackTrace();
        }
        
    }

    @Override
    public void tearDown() {
        reportMessage("tearDown");

    }

    @Override
    public String getDescription() {
        return "API TelephonyInfoTest test";
    }

}
