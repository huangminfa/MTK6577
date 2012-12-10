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

package com.android.DataServiceTestCases;

import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;

import com.android.FunctionTest.TestCase;

public abstract class DataServiceTestCase extends TestCase {

    public DataServiceTestCase(Handler reportHander) {
        super(reportHander);
        // TODO Auto-generated constructor stub
    }

    public boolean isTcpConnected() {
        boolean result = false;

        result = Ping.doPing();
        if (result) {
            reportMessage("Pinging test host pass.");
        } else {
            reportMessage("Pinging test host fail.");
        }
        return result;
    }

    public boolean isTcpConnected(String host) {
        boolean result = false;

        result = Ping.doPing(host);
        if (result) {
            reportMessage("Pinging test host pass.");
        } else {
            reportMessage("Pinging test host fail.");
        }
        return result;
    }

    static public void resetInitCondition() {
        NetworkInfo mobileInfo = DataServiceUtil.getMobileConnInfo();
        NetworkInfo mmsInfo = DataServiceUtil.getMmsConnInfo();
        NetworkInfo wifiInfo = DataServiceUtil.getWifiConnInfo();

        if (!mobileInfo.isConnectedOrConnecting()) {
            SIMInfo sim0 = Telephony.SIMInfo.getSIMInfoBySlot(AppContext, 0);
            if (sim0 != null) {
                DataServiceUtil.switchSimData(sim0.mSimId);
            }
        }

        if (mmsInfo.isConnectedOrConnecting()) {
            DataServiceUtil.disableMms(mmsInfo.getSimId());
        }

        if (wifiInfo.isConnectedOrConnecting()) {
            DataServiceUtil.switchOffWifi();
        }

        if (DataServiceUtil.get3GCapabilitySIM() != 0) {
            DataServiceUtil.set3GCapabilitySIM(0);
        }
    }
}
