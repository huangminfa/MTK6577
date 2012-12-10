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

package com.mediatek.bluetooth.map.util;

import android.telephony.TelephonyManager;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.bluetooth.map.MAP;
import android.provider.Telephony.SIMInfo;

import android.content.Context;

public class NetworkUtil{
	public static int SIM1 = 0; //indicate slot 1
	public static int SIM2 = 1; //indicate slot 2
	
	public static boolean isGeminiSupport(){
		if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int getNetworkType(){
		return TelephonyManager.getDefault().getPhoneType(); 	
	}
	//
	public static int getGeminiNetworkType(int slotId){
		return TelephonyManager.getDefault().getPhoneTypeGemini(slotId);
	}

	public static int getSmsType() {
		int type = getNetworkType();
		switch (type) {
			case TelephonyManager.PHONE_TYPE_GSM:
				return MAP.MSG_TYPE_SMS_GSM;
			case TelephonyManager.PHONE_TYPE_CDMA:
				return MAP.MSG_TYPE_SMS_CDMA;
			default:
				return 0;				
		}
	}
	public static int getGeminiSmsType(int slotId){
		int type = getGeminiNetworkType(slotId);
		switch (type) {
			case TelephonyManager.PHONE_TYPE_GSM:
				return MAP.MSG_TYPE_SMS_GSM;
			case TelephonyManager.PHONE_TYPE_CDMA:
				return MAP.MSG_TYPE_SMS_CDMA;
			default:
				return 0;				
		}
	}

	public static String getPhoneNumber(int sim){
		if (isGeminiSupport()) {
			return TelephonyManager.getDefault().getLine1NumberGemini(sim);
		} else {
			return TelephonyManager.getDefault().getLine1Number();
		}
	}	
	/*simid is different with slot id in telephony*/
	public static long getSimIdBySlotId(Context context, int slot){
		long simId = -1;
		SIMInfo info = 	SIMInfo.getSIMInfoBySlot(context, slot);
		if (info != null){
			simId = info.mSimId;
		} 
		return simId;
	}
	//if no slot for the sim id, return -1;
	public static int getSlotBySimId(Context context, long simId){
		return SIMInfo.getSlotById(context, simId);
	}

}
