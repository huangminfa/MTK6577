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

package com.mediatek.smsreg;

import java.util.List;
import com.mediatek.featureoption.FeatureOption;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.Build;
import com.android.internal.telephony.Phone;
import com.mediatek.xlog.Xlog;

class SmsBuilder {
	private String TAG = "SmsReg/SmsBuilder";
	private TelephonyManager teleMgr = null;	
	private int simId = 0;
	private ConfigInfoGenerator configInfo;
	
	SmsBuilder(Context context) {
		teleMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		configInfo = XMLGenerator.getInstance(SmsRegConst.CONFIG_PATH);
		if (configInfo == null) {
			Xlog.e(TAG, "get XMLGenerator instance failed!");
		}
	}

	String getSmsContent(ConfigInfoGenerator configGenerator, int simCard) {
		simId = simCard;
		Xlog.i(TAG, "SimId = " + simId);
		if(simId < 0){
			Xlog.e(TAG,"SimId is not valid!");
			return null;
		}
		List<SmsInfoUnit> smsInfoList = configGenerator.getSmsInfoList();
		if(smsInfoList == null){
			Xlog.e(TAG,"there is no sms segment in config file!");
			return null;
		}
		String smsContext = new String();
		for (int i = 0; i < smsInfoList.size(); i++) {
			SmsInfoUnit smsUnit = smsInfoList.get(i);
			String smsUnitContent = smsUnit.getContent();
			Xlog.i(TAG, "smsUnit = " + smsUnitContent);
			// if the smsUnit content is null, then return null message and DO
			// NOT to register
			String smsContent = getContentInfo(configGenerator, smsUnitContent);
			if (smsContent != null) {
				String prefix = smsUnit.getPrefix();
				String postfix = smsUnit.getPostfix();
				if(prefix != null){
					smsContext += prefix;					
				}
				smsContext += smsContent;
				if(postfix != null){
					smsContext += postfix;
				}
//				if (smsUnit.getPrefix() == null && smsUnit.getPostfix() == null) {
//					smsContext = smsContext + smsContent;
//				} else if (smsUnit.getPostfix() == null) {
//					smsContext = smsContext + smsUnit.getPrefix()+ smsContent;
//				} else if (smsUnit.getPrefix() == null) {
//					smsContext = smsContext + smsContent + smsUnit.getPostfix();
//				} else {
//					smsContext = smsContext
//							+ smsUnit.getPrefix()
//							+ getContentInfo(configGenerator, smsUnit
//									.getContent()) + smsUnit.getPostfix();
//				}
			} else {
				Xlog.w(TAG, "The smsUnit [" + smsUnitContent
						+ "] content is null");
				smsContext = null;
				break;
			}
		}
		Xlog.i(TAG, "sms context: " + smsContext);
		return smsContext;
	}

	public String getContentInfo(ConfigInfoGenerator configGenerator,
			String command) {
		if (command.equals("getimsi")) {
			if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
	            Xlog.i(TAG, "get imsi by simId " + simId);
				return teleMgr.getSubscriberIdGemini(simId);
			} else {
				return teleMgr.getSubscriberId();
			}
		} else if (command.equals("getimei")) {
			if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
	            Xlog.i(TAG, "get imei by simId " + simId);
			    String imei= teleMgr.getDeviceIdGemini(simId);
//				String imei= teleMgr.getDeviceIdGemini(Phone.GEMINI_SIM_1);
//				Log.i(TAG, "return the first SIM card's IMEI " + imei);
//				if(imei == null){
//					imei = teleMgr.getDeviceIdGemini(Phone.GEMINI_SIM_2);
//					Log.i(TAG, "return the second SIM card's IMEI " + imei);
//				}				
				return imei;
			} else {
				Xlog.i(TAG, "return IMEI " + teleMgr.getDeviceId());
				return teleMgr.getDeviceId();
			}

		} else if (command.equals("getversion")) {
			Xlog.i(TAG, "return version " + Build.DISPLAY);
			return Build.DISPLAY;
		} else if (command.equals("getproduct")) {
			Xlog.i(TAG, " return product('MTK' is for test): " + Build.MODEL);
			// return Build.MODEL;
			// for test only begin
//			if (configInfo != null) {
//				String operatorID = configInfo.getOperatorName();
//				Xlog.e(TAG, "get product by operator " + operatorID);
//				if (operatorID.equals("cmcc")) {
//					return "MTK_73V3_2";
//				} else if (operatorID.equals("cu")) {
//					return "6573";
//				} else {
//					Xlog.w(TAG, "there is not cmcc or cu, " +
//							"return the build model");
//					return Build.MODEL;
//				}
//			}
			return "MTKMDMP1";
			// for test only end

		} else if (command.equals("getvendor")) {
			if (configInfo != null) {
				String manufacturerName = configInfo.getManufacturerName();
				Xlog.i(TAG, "getvendor the manufacturer name  is "
						+ manufacturerName);
				return manufacturerName;
			}
			// for test only end
		} else if (command.equals("getOem")) {
			String oemName = configGenerator.getOemName();
			if (oemName == null) {
				// Here should use the system api to get oem name;
				return null;
			}
			return oemName;
		} else {
			Xlog.w(TAG, "The wrong command");
		}
		return null;
	}
}
