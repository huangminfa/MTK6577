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

package com.android.GeminiDataServiceTestCases;

import android.net.ConnectivityManager;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;

import com.android.DataServiceTestCases.DataServiceTestCase;
import com.android.DataServiceTestCases.DataServiceUtil;
import com.android.FunctionTest.Assert;

public class GeminiMultiplePdpTest extends DataServiceTestCase {
	
    public GeminiMultiplePdpTest() {
		super(null);
	}
	
    public GeminiMultiplePdpTest(Handler reportHander) {
        super(reportHander);
    }

	@Override
	public void setup() {
        //mResultHandlerAcvivity.onResult("******************************");
        reportMessage("setup");
        resetInitCondition();
		mAborted = false;
	}

	@Override
    public void run() {
        checkMms(0);
        // checkMms(1);
        long gprsValue = Settings.System.getLong(AppContext.getContentResolver(),
                "gprs_connection_sim_setting",
                -5);
        SIMInfo sim0 = Telephony.SIMInfo.getSIMInfoBySlot(AppContext, 0);
        SIMInfo sim1 = Telephony.SIMInfo.getSIMInfoBySlot(AppContext, 1);

        if (sim0 != null && sim1 != null) {
            long slot0 = sim0.mSimId;
            long slot1 = sim1.mSimId;

            gprsValue = (gprsValue == slot0 ? slot1 : slot0);
            Assert.assertEquals(true, DataServiceUtil.switchSimData(gprsValue).getResult());
            Assert.assertEquals(true, isTcpConnected());
            checkMms(0);
            // checkMms(1);
            gprsValue = (gprsValue == slot0 ? slot1 : slot0);
            Assert.assertEquals(true, DataServiceUtil.switchSimData(gprsValue).getResult());
        }

        Assert.assertEquals(true, DataServiceUtil.switchOnWifi().getResult());
        Assert.assertEquals(true, isTcpConnected());
        checkMms(0);
        // checkMms(1);
        Assert.assertEquals(true, DataServiceUtil.switchOffWifi().getResult());
        Assert.assertEquals(true, isTcpConnected());
	}

	@Override
	public void tearDown() {
        reportMessage("tearDown");
	}

    private void checkMms(int radioNum) {
        Assert.assertEquals(true, DataServiceUtil.enableMms(radioNum).getResult());
        String mmsProxy = DataServiceUtil.getConnectedMmsProxyHost();

        if (mmsProxy != null) {
            if (DataServiceUtil.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS,
                    mmsProxy)) {
                Assert.assertEquals(true, isTcpConnected(mmsProxy));
            }
        }
        Assert.assertEquals(true, DataServiceUtil.disableMms(radioNum).getResult());
    }

	@Override
	public String getDescription() {
        return "Gemini multiple PDP test";
	}

}
