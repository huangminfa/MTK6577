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

package com.mediatek.omacp.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import com.mediatek.xlog.Xlog;

import com.mediatek.omacp.R;
import com.mediatek.omacp.parser.ApplicationClass;
import com.mediatek.omacp.parser.NapdefClass;
import com.mediatek.omacp.parser.ProxyClass;
import com.mediatek.omacp.parser.ApplicationClass.AppAuth;
import com.mediatek.omacp.parser.ApplicationClass.Resource;

public class OmacpMessageUtils {

	private static final String XLOG = "Omacp/OmacpMessageUtils";
	
	private static final boolean DEBUG = true;
	
	//constants for AppIds
	static final String BROWSER_APPID = "w2";
	static final String MMS_APPID = "w4";
	static final String DM_APPID = "w7";
	static final String SMTP_APPID = "25";
	static final String POP3_APPID = "110";
	static final String IMAP4_APPID = "143";
	static final String RTSP_APPID = "554";
	static final String SUPL_APPID = "ap0004";
	static final String MMS_2_APPID = "ap0005";
	static final String APN_APPID = "apn";
	static final String DS_APID = "w5";
	static final String IMPS_APPID = "wA";
	
	public static ArrayList<String> getValidApplicationNameSet(Context context, ArrayList<ApplicationClass> apList, ArrayList<NapdefClass> napList){
		ArrayList<String> list = new ArrayList<String>();
		
		if(apList != null){
			int size = apList.size();		
			for(int i = 0; i < size; i ++){
				ApplicationClass application = apList.get(i);
				String appId = application.APPID;
				String name = null;
				if(application.APPID.equalsIgnoreCase(SMTP_APPID) || application.APPID.equalsIgnoreCase(POP3_APPID)
						|| application.APPID.equalsIgnoreCase(IMAP4_APPID)){
					name = context.getString(R.string.email_app_name);
				}else if(application.APPID.equalsIgnoreCase(MMS_APPID)){//remove invalid mms setting if it only has mmsc, because it moved to apn
					if(application.CM != null){
						name = getAppName(context, appId);
					}
				}else if(application.APPID.equalsIgnoreCase(MMS_2_APPID)){
					if(application.CM != null || application.RM != null
							|| application.MS != null || application.PC_ADDR != null || application.Ma != null){
						//if ap0005 mms setting only has mmsc, then ignore it, because it has been moved to apn
						name = getAppName(context, appId);
					}
					
				}else{
					name = getAppName(context, appId);
				}
				if(name != null && !list.contains(name)){
					list.add(name);
				}
			}
		}		
		
		if(napList != null && napList.size() != 0){
			list.add(context.getString(R.string.apn_app_name));
		}
		
		return list;
	}
	
	public static String getAppName(Context context, String appId){
		String name = null;
		
		if(appId.equalsIgnoreCase(MMS_APPID)){
			name = context.getString(R.string.mms_app_name);
		}else if(appId.equalsIgnoreCase(MMS_2_APPID)){
			name = context.getString(R.string.mms_app_name);
		}else if(appId.equalsIgnoreCase(BROWSER_APPID)){
			name = context.getString(R.string.browser_app_name);
		}else if(appId.equalsIgnoreCase(APN_APPID)){
			name = context.getString(R.string.apn_app_name);
		}else if(appId.equalsIgnoreCase(IMAP4_APPID)){
			name = context.getString(R.string.email_app_name);
		}else if(appId.equalsIgnoreCase(POP3_APPID)){
			name = context.getString(R.string.email_app_name);
		}else if(appId.equalsIgnoreCase(SMTP_APPID)){
			name = context.getString(R.string.email_app_name);
		}else if(appId.equalsIgnoreCase(DM_APPID)){
			name = context.getString(R.string.dm_app_name);
		}else if(appId.equalsIgnoreCase(SUPL_APPID)){
			name = context.getString(R.string.agps_app_name);
		}else if(appId.equalsIgnoreCase(RTSP_APPID)){
			name = context.getString(R.string.rtsp_app_name);
		}else if(appId.equalsIgnoreCase(DS_APID)){
			name = context.getString(R.string.ds_app_name);
		}else if(appId.equalsIgnoreCase(IMPS_APPID)){
			name = context.getString(R.string.imps_app_name);
		}else{
			Xlog.e(XLOG, "OmacpMessageUtils getAppName unknown app.");
		}
		
		return name;
	}
	
	public static String getSummary(Context context, String savedSummary){
		if(DEBUG){
			Xlog.i(XLOG, "OmacpMessageUtils savedSummary is : " + savedSummary);
		}		
		
		String summary = context.getString(R.string.application_label);
		
		while(savedSummary != null && savedSummary.length() > 0){
			int sep = savedSummary.indexOf(",");
	        String appId = null;
	        if (sep >= 0) {
	        	appId = savedSummary.substring(0, sep);
	        	
	        	if(savedSummary.length() > sep + 1){
	        		savedSummary = savedSummary.substring(sep + 1);
	        	}else{
	        		savedSummary = null;
	        	}
	        	
	        	String name = getAppName(context, appId);
	        	if(null != name){
	        		summary += name;
	        		summary += ", ";
	        	}
	        }
		}
		
		if(summary.equalsIgnoreCase(context.getString(R.string.application_label))){
			Xlog.e(XLOG, "OmacpMessageUtils summary is null.");
			summary = null;
		}else{
			summary = summary.substring(0, summary.length() - 2);
		}
		
		return summary;		
	}
	
	public static SpannableStringBuilder getSettingsDetailInfo(Context context, ArrayList<ApplicationClass> apList,
			ArrayList<NapdefClass> napList, ArrayList<ProxyClass> pxList) {
		// TODO Auto-generated method stub
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		
//		final int color = android.R.styleable.Theme_textColorSecondary;
		
		String mmsSetting = null;
		String emailAccountName = null;
		String inboundEmailSetting = null;
		String outboundEmailSetting = null;
		String emailInboundType = null;
		
		if(apList != null){
			int size = apList.size();
			for(int i = 0; i < size; i ++){
				
				ApplicationClass application = apList.get(i);
				
				if(DEBUG){
					Xlog.i(XLOG, "OmacpMessageUtils getSettingsDetailInfo application is : " + application);
				}				
				
				String settings = null;
				
				//append application's parameters
				if(application.APPID.equalsIgnoreCase(BROWSER_APPID)){ //Browser					
					SpannableStringBuilder browserInfo = getBrowserSettingInfo(context, application);
					if(browserInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(browserInfo);
					}
				}else if(application.APPID.equalsIgnoreCase(MMS_APPID) || application.APPID.equalsIgnoreCase(MMS_2_APPID)){
					if(mmsSetting == null){
						mmsSetting = getMmsSettingsInfo(context, application);
					}					
				}else if(application.APPID.equalsIgnoreCase(DM_APPID)){ //DM
					SpannableStringBuilder dmInfo = getDMSettingInfo(context, application);
					if(dmInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(dmInfo);
					}
				}else if(application.APPID.equalsIgnoreCase(SMTP_APPID)){
					if(emailAccountName == null && application.PROVIDER_ID != null){
						emailAccountName = application.PROVIDER_ID;
					}
					
					if(outboundEmailSetting == null){
						outboundEmailSetting = getEmailSettingInfo(context, application);
						if(application.APPAUTH.size() != 0){
							if(outboundEmailSetting == null){
								outboundEmailSetting = "";
							}
							outboundEmailSetting += "\n";
							outboundEmailSetting += context.getString(R.string.email_need_sign_label);
							if(OmacpApplicationCapability.email_outbound_auth_type == false){
								outboundEmailSetting += context.getString(R.string.info_unsupport);
							}else{
								if(application.APPAUTH.get(0).AAUTHTYPE != null){									
									outboundEmailSetting += context.getString(R.string.email_need_sign_yes);
								}else{
									outboundEmailSetting += context.getString(R.string.email_need_sign_no);
								}
							}
						}
					}
					
				}else if(application.APPID.equalsIgnoreCase(POP3_APPID) || application.APPID.equalsIgnoreCase(IMAP4_APPID)){
					if(emailAccountName == null && application.PROVIDER_ID != null){
						emailAccountName = application.PROVIDER_ID;
					}
					
					if(emailInboundType == null && application.APPID.equalsIgnoreCase(POP3_APPID)){
						emailInboundType = context.getString(R.string.email_pop3_app_name);
					}else if(emailInboundType == null && application.APPID.equalsIgnoreCase(IMAP4_APPID)){
						emailInboundType = context.getString(R.string.email_imap4_app_name);
					}
					
					if(inboundEmailSetting == null){
						inboundEmailSetting = getEmailSettingInfo(context, application);						
					}
					
				}else if(application.APPID.equalsIgnoreCase(RTSP_APPID)){
					SpannableStringBuilder rtspInfo = getRtspSettingInfo(context, application);
					if(rtspInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(rtspInfo);
					}	
				}else if(application.APPID.equalsIgnoreCase(SUPL_APPID)){ //Supl
					SpannableStringBuilder suplInfo = getSuplSettingInfo(context, application);
					if(suplInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(suplInfo);
					}
				}else if(application.APPID.equalsIgnoreCase(DS_APID)){//ds
					SpannableStringBuilder dsInfo = getDsSettingInfo(context, application);
					if(dsInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(dsInfo);
					}
				}else if(application.APPID.equalsIgnoreCase(IMPS_APPID)){//imps
					SpannableStringBuilder impsInfo = getDsSettingInfo(context, application);
					if(impsInfo != null){
						if(info.length() > 0){
							info.append("\n");
						}
						info.append(impsInfo);
					}
				}else{
					Xlog.e(XLOG, "OmacpMessageUtils getSettingsDetailInfo appid unknown.");
				}				
			}
			
			if(mmsSetting != null){
				if(info.length() > 0){
					info.append("\n");
				}
				addApplicationLabel(context, info, MMS_APPID);
				if(OmacpApplicationCapability.mms == false){
					info.append("\n");
					info.append(context.getString(R.string.info_unsupport));
				}else{
					info.append(mmsSetting);
				}
			}
			
			//Email
			if(outboundEmailSetting != null || inboundEmailSetting != null){
				if(info.length() > 0){
					info.append("\n");
				}
				
				int before = info.length();
				//append "application: + ap name"
				info.append(context.getString(R.string.application_label));
				info.append(context.getString(R.string.email_app_name));
				info.setSpan(new ForegroundColorSpan(android.graphics.Color.MAGENTA), before,
		              info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				info.setSpan(new StyleSpan(Typeface.BOLD), before,
		                  info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				
				if(OmacpApplicationCapability.email == false){
					info.append("\n");
					info.append(context.getString(R.string.info_unsupport));
				}else{
					//account name
					if(emailAccountName != null){
						info.append("\n");
						info.append(context.getString(R.string.email_account_label));
						if(OmacpApplicationCapability.email_provider_id == false){
							info.append(context.getString(R.string.info_unsupport));
						}else{
							info.append(emailAccountName);
						}					
					}				
					
					//outbound setting info
					if(outboundEmailSetting != null){
						info.append("\n");
						
						before = info.length();
						info.append(context.getString(R.string.email_smtp_app_name));
						info.setSpan(new StyleSpan(Typeface.BOLD), before,
				                  info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						
						info.append(outboundEmailSetting);
					}
					
					//inbound setting info
					if(inboundEmailSetting != null && emailInboundType != null){
						info.append("\n");
						
						before = info.length();
						info.append(emailInboundType);
						info.setSpan(new StyleSpan(Typeface.BOLD), before,
				                  info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						
						info.append(inboundEmailSetting);
					}
				}
			}
			
		}
		
		if(napList != null){ //Apn	
			SpannableStringBuilder apnInfo = getApnSettingInfo(context, napList, apList, pxList);
			if(apnInfo != null){
				if(info.length() > 0){
					info.append("\n");
				}
				info.append(apnInfo);
			}
		}
		
		if(DEBUG){
			Xlog.d(XLOG, "OmacpMessageUtils getSettingsDetailInfo info is : " + info.toString());
		}
		
		return info;
	}
	
	private static String getMmsSettingsInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils addMmsSettingsInfo application is null.");
			return null;
		}
		
		//if w4 mms setting only has mmsc, then ignore it, because it has been moved to apn
		if(application.APPID.equalsIgnoreCase(MMS_APPID) && application.CM == null){
			Xlog.e(XLOG, "OmacpMessageUtils invalid w4 mms setting.");
			return null;
		}
		
		//if ap0005 mms setting only has mmsc, then ignore it, because it has been moved to apn
		if(application.APPID.equalsIgnoreCase(MMS_2_APPID) && application.CM == null && application.RM == null
				&& application.MS == null && application.PC_ADDR == null && application.Ma == null){
			Xlog.e(XLOG, "OmacpMessageUtils invalid ap0005 mms setting.");
			return null;
		}
		
		String mmsSetting = null;
		
//		//mmsc name
//		if(application.NAME != null){
//			if(mmsSetting == null){
//				mmsSetting = "";
//				mmsSetting += getElement(context, context.getString(R.string.mmsc_name_label), 
//						OmacpApplicationCapability.mms_mmsc_name, application.NAME);
//			}else if(mmsSetting.contains(context.getString(R.string.mmsc_name_label)) == false){
//				mmsSetting += getElement(context, context.getString(R.string.mmsc_name_label), 
//						OmacpApplicationCapability.mms_mmsc_name, application.NAME);			
//			}
//		}
		
//		//proxy
//		if(application.TO_PROXY != null){
//			if(mmsSetting == null && application.TO_PROXY.size() != 0){
//				mmsSetting = "";
//				mmsSetting += getElement(context, context.getString(R.string.proxy_label), 
//						OmacpApplicationCapability.mms_to_proxy, application.TO_PROXY.get(0));
//			}else if(application.TO_PROXY.size() != 0 && mmsSetting.contains(context.getString(R.string.proxy_label)) == false){
//				mmsSetting += getElement(context, context.getString(R.string.proxy_label), 
//						OmacpApplicationCapability.mms_to_proxy, application.TO_PROXY.get(0));
//			}			
//		}
//		
//		//napid
//		if(application.TO_NAPID != null)
//			if(mmsSetting == null && application.TO_NAPID.size() != 0){
//				mmsSetting += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.mms_to_napid, application.TO_NAPID.get(0));
//			}else if(application.TO_NAPID.size() != 0 && mmsSetting.contains(context.getString(R.string.nap_label)) == false){
//				mmsSetting += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.mms_to_napid, application.TO_NAPID.get(0));
//		}
		
//		//mmsc
//		if(application.ADDR != null){
//			if(mmsSetting == null){				
//				if(application.ADDR.size() != 0 && application.ADDR.get(0) != null){
//					mmsSetting = "";
//					mmsSetting += getElement(context, context.getString(R.string.mmsc_label), 
//							OmacpApplicationCapability.mms_mmsc, application.ADDR.get(0));
//				}else if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
//					mmsSetting = "";
//					mmsSetting += getElement(context, context.getString(R.string.mmsc_label), 
//							OmacpApplicationCapability.mms_mmsc, application.APPADDR.get(0).ADDR);
//				}				
//			}else{
//				if(application.ADDR.size() != 0 && application.ADDR.get(0) != null
//					&& mmsSetting.contains(context.getString(R.string.mmsc_label)) == false){
//					mmsSetting += getElement(context, context.getString(R.string.mmsc_label), 
//							OmacpApplicationCapability.mms_mmsc, application.ADDR.get(0));
//				}else if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null 
//						&& mmsSetting.contains(context.getString(R.string.mmsc_label)) == false){
//					mmsSetting += getElement(context, context.getString(R.string.mmsc_label), 
//							OmacpApplicationCapability.mms_mmsc, application.APPADDR.get(0).ADDR);
//				}
//			}
//		}
		
		//cm
		if(application.CM != null)
			if(mmsSetting == null){
				mmsSetting = "";
				mmsSetting += getElement(context, context.getString(R.string.mms_cm_label), 
						OmacpApplicationCapability.mms_cm, application.CM);
			}else if(mmsSetting.contains(context.getString(R.string.mms_cm_label)) == false){
				
				mmsSetting += getElement(context, context.getString(R.string.mms_cm_label), 
						OmacpApplicationCapability.mms_cm, application.CM);
		}
		
		//rm
		if(application.RM != null){
			if(mmsSetting == null){
				mmsSetting = "";
				mmsSetting += getElement(context, context.getString(R.string.mms_rm_label), 
						OmacpApplicationCapability.mms_rm, application.RM);
			}else if(mmsSetting.contains(context.getString(R.string.mms_rm_label)) == false){
				mmsSetting += getElement(context, context.getString(R.string.mms_rm_label), 
						OmacpApplicationCapability.mms_rm, application.RM);
			}
		}
		
		//ms
		if(application.MS != null){
			if(mmsSetting == null){
				mmsSetting = "";
				mmsSetting += getElement(context, context.getString(R.string.mms_ms_label), 
						OmacpApplicationCapability.mms_ms, application.MS);
			}else if(mmsSetting.contains(context.getString(R.string.mms_ms_label)) == false){
				mmsSetting += getElement(context, context.getString(R.string.mms_ms_label), 
						OmacpApplicationCapability.mms_ms, application.MS);
			}
		}
		
		//pc addr
		if(application.PC_ADDR != null){
			if(mmsSetting == null){
				mmsSetting = "";
				mmsSetting += getElement(context, context.getString(R.string.mms_pc_addr_label), 
						OmacpApplicationCapability.mms_pc_addr, application.PC_ADDR);
			}else if(mmsSetting.contains(context.getString(R.string.mms_pc_addr_label)) == false){
				mmsSetting += getElement(context, context.getString(R.string.mms_pc_addr_label), 
						OmacpApplicationCapability.mms_pc_addr, application.PC_ADDR);
			}
		}
		
		//ma
		if(application.Ma != null){
			if(mmsSetting == null){
				mmsSetting = "";
				mmsSetting += getElement(context, context.getString(R.string.mms_ma_label), 
						OmacpApplicationCapability.mms_ma, application.Ma);
			}else if(mmsSetting.contains(context.getString(R.string.mms_ma_label)) == false){
				mmsSetting += getElement(context, context.getString(R.string.mms_ma_label), 
						OmacpApplicationCapability.mms_ma, application.Ma);
			}
		}
		
		return mmsSetting;		
	}
	
	private static String getElement(Context context, String label, boolean capability, String value){
		String element = "\n";		
		element += label;
		if(capability == false){					
			element += context.getString(R.string.info_unsupport);
		}else{
			element += value;
		}
		return element;
	}
	
	private static void addApplicationLabel(Context context, SpannableStringBuilder info, String appId){
		if(info == null){
			Xlog.e(XLOG, "OmacpMessageUtils addApplicationLabel info is null.");
			return;
		}
		
		int before = info.length();
		//append "application: + ap name"
		info.append(context.getString(R.string.application_label));
		info.append(getAppName(context, appId));
		info.setSpan(new ForegroundColorSpan(android.graphics.Color.MAGENTA), before,
              info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		info.setSpan(new StyleSpan(Typeface.BOLD), before,
                  info.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	}
	
	private static String getEmailSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getEmailInboundSettingInfo application is null.");
			return null;
		}
		
		boolean isOutbound = false;
		if(application.APPID.equalsIgnoreCase(SMTP_APPID)){
			isOutbound = true;
		}
		
		String info = null;
		if(application.NAME != null){
			if(info == null){
				info = "";
			}			
			info += getElement(context, context.getString(R.string.email_setting_name_label), 
					OmacpApplicationCapability.email_setting_name, application.NAME);
		}
		
//		if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//			if(info == null){
//				info = "";
//			}			
//			info += getElement(context, context.getString(R.string.nap_label), 
//					OmacpApplicationCapability.email_to_napid, application.TO_NAPID.get(0));
//		}
		
		//user name
		if(application.APPAUTH.size() != 0){
			if(application.APPAUTH.get(0).AAUTHNAME != null){
				if(info == null){
					info = "";
				}
				info += getEmailElement(context, context.getString(R.string.user_name_label), 
						OmacpApplicationCapability.email_outbound_user_name, 
						OmacpApplicationCapability.email_inbound_user_name, isOutbound, application.APPAUTH.get(0).AAUTHNAME);

				//password
				if(application.APPAUTH.get(0).AAUTHSECRET != null){
					if(info == null){
						info = "";
					}
					info += getEmailElement(context, context.getString(R.string.password_label), 
							OmacpApplicationCapability.email_outbound_password, 
							OmacpApplicationCapability.email_inbound_password, isOutbound, application.APPAUTH.get(0).AAUTHSECRET);
				}
			}				
		}

		//server
		if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
			if(info == null){
				info = "";
			}
			info += getEmailElement(context, context.getString(R.string.server_address_label), 
					OmacpApplicationCapability.email_outbound_addr, 
					OmacpApplicationCapability.email_inbound_addr, isOutbound, application.APPADDR.get(0).ADDR);
		}else if(application.ADDR.size() != 0 && application.ADDR.get(0) != null){
			if(info == null){
				info = "";
			}
			info += getEmailElement(context, context.getString(R.string.server_address_label), 
					OmacpApplicationCapability.email_outbound_addr, 
					OmacpApplicationCapability.email_inbound_addr, isOutbound, application.ADDR.get(0));	
		} 
		
		//port
		if(application.APPADDR.get(0).PORT.size() != 0 && application.APPADDR.get(0).PORT.get(0).PORTNBR != null){
			if(info == null){
				info = "";
			}
			info += getEmailElement(context, context.getString(R.string.port_number_label), 
					OmacpApplicationCapability.email_outbound_port_number, 
					OmacpApplicationCapability.email_inbound_port_number, isOutbound, application.APPADDR.get(0).PORT.get(0).PORTNBR);
		}
		
		//secure
		if(application.APPADDR.get(0).PORT.size() != 0 && application.APPADDR.get(0).PORT.get(0).SERVICE.size() != 0
				&& application.APPADDR.get(0).PORT.get(0).SERVICE.get(0) != null){
			if(info == null){
				info = "";
			}
			info += getEmailElement(context, context.getString(R.string.email_secure_label), 
					OmacpApplicationCapability.email_outbound_secure, 
					OmacpApplicationCapability.email_inbound_secure, isOutbound, application.APPADDR.get(0).PORT.get(0).SERVICE.get(0));
		}
		
		if(isOutbound == true){
			if(application.FROM != null){
				if(info == null){
					info = "";
				}			
				info += getElement(context, context.getString(R.string.email_from_label), 
						OmacpApplicationCapability.email_from, application.FROM);
			}
			
			if(application.RT_ADDR != null){
				if(info == null){
					info = "";
				}			
				info += getElement(context, context.getString(R.string.email_rt_address_label), 
						OmacpApplicationCapability.email_rt_addr, application.RT_ADDR);
			}
		}
		
		return info;
	}
	
	private static String getEmailElement(Context context, String label, boolean outCapability, boolean inCapability, boolean isOutbound, String value){
		String element = "\n";		
		element += label;
		if(isOutbound == true){
			if(outCapability == true){
				element += value;
			}else{
				element += context.getString(R.string.info_unsupport);
			}
		}else{
			if(inCapability == true){
				element += value;
			}else{
				element += context.getString(R.string.info_unsupport);
			}
		}
		return element;
	}
	
	private static SpannableStringBuilder getRtspSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getRtspSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.rtsp == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{
			//server id
			if(application.PROVIDER_ID != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_id_label), 
						OmacpApplicationCapability.rtsp_provider_id, application.PROVIDER_ID);
			}
			
			//server name
			if(application.NAME != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.name_label), 
						OmacpApplicationCapability.rtsp_name, application.NAME);
			}
			
//			//proxy
//			if(application.TO_PROXY != null && application.TO_PROXY.size() != 0 && application.TO_PROXY.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.proxy_label), 
//						OmacpApplicationCapability.browser_to_proxy, application.TO_PROXY.get(0));
//			}
//			
//			//napid
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.browser_to_napid, application.TO_NAPID.get(0));
//			}
			
			if(application.MAX_BANDWIDTH != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.rtsp_max_bandwidth_label), 
						OmacpApplicationCapability.rtsp_max_bandwidth, application.MAX_BANDWIDTH);
			}
			
			if(application.NETINFO != null){
				for(int p = 0; p < application.NETINFO.size(); p ++){
					if(settings == null){
						settings = "";
					}			
					settings += getElement(context, context.getString(R.string.rtsp_netinfo_label), 
							OmacpApplicationCapability.rtsp_net_info, application.NETINFO.get(p));
				}				
			}
			
			if(application.MAX_UDP_PORT != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.rtsp_max_udp_port_label), 
						OmacpApplicationCapability.rtsp_max_udp_port, application.MAX_UDP_PORT);
			}
			
			if(application.MIN_UDP_PORT != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.rtsp_min_udp_port_label), 
						OmacpApplicationCapability.rtsp_min_udp_port, application.MIN_UDP_PORT);
			}
			
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}
			return info;
		}		
	}
	
	private static SpannableStringBuilder getImpsSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getDsSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.imps == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{
			//server id
			if(application.PROVIDER_ID != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_id_label), 
						OmacpApplicationCapability.imps_provider_id, application.PROVIDER_ID);
			}
			//server name
			if(application.NAME != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_name_label), 
						OmacpApplicationCapability.imps_server_name, application.NAME);
			}
			//content type
			if(application.AACCEPT != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.content_type_label), 
						OmacpApplicationCapability.imps_content_type, application.AACCEPT);
			}
			//server address
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.imps_server_address, application.APPADDR.get(0).ADDR);
			}else if(application.ADDR.size() != 0 && application.ADDR.get(0) != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.imps_server_address, application.ADDR.get(0));
			}
			//addr type
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDRTYPE != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_addr_type_label), 
						OmacpApplicationCapability.imps_address_type, application.APPADDR.get(0).ADDRTYPE);
			}
//			//proxy
//			if(application.TO_PROXY != null && application.TO_PROXY.size() != 0 && application.TO_PROXY.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.imps_to_proxy, application.TO_PROXY.get(0));
//			}
//			//napid
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.imps_to_napid, application.TO_NAPID.get(0));
//			}
			//auth
			if(application.APPAUTH != null && application.APPAUTH.size() != 0){
				AppAuth auth = application.APPAUTH.get(0);
				if(auth.AAUTHLEVEL != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_level_label), 
							OmacpApplicationCapability.imps_auth_level, auth.AAUTHLEVEL);
				}
				
				if(auth.AAUTHNAME != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.user_name_label), 
							OmacpApplicationCapability.imps_auth_name, auth.AAUTHNAME);
				}
				
				if(auth.AAUTHSECRET != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.password_label), 
							OmacpApplicationCapability.imps_auth_secret, auth.AAUTHSECRET);
				}
			}
			//services
			if(application.SERVICES != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.imps_services_label), 
						OmacpApplicationCapability.imps_services, application.SERVICES);
			}
			//cid prefix
			if(application.CIDPREFIX != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.imps_cid_prefix_label), 
						OmacpApplicationCapability.imps_client_id_prefix, application.CIDPREFIX);
			}
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}
			return info;
		}
	}
	
	private static SpannableStringBuilder getDsSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getDsSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.ds == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{
			//server name
			if(application.NAME != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_name_label), 
						OmacpApplicationCapability.ds_server_name, application.NAME);
			}
//			//proxy
//			if(application.TO_PROXY != null && application.TO_PROXY.size() != 0 && application.TO_PROXY.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.ds_to_proxy, application.TO_PROXY.get(0));
//			}
//			//napid
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.ds_to_napid, application.TO_NAPID.get(0));
//			}
			//server id
			if(application.PROVIDER_ID != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_id_label), 
						OmacpApplicationCapability.supl_provider_id, application.PROVIDER_ID);
			}
			//server address
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.ds_server_address, application.APPADDR.get(0).ADDR);
			}else if(application.ADDR.size() != 0 && application.ADDR.get(0) != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.ds_server_address, application.ADDR.get(0));
			}
			//addr type
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDRTYPE != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_addr_type_label), 
						OmacpApplicationCapability.ds_address_type, application.APPADDR.get(0).ADDRTYPE);
			}
			//port number
			if(application.ADDR != null && application.APPADDR.size() != 0 && application.APPADDR.get(0).PORT.size() != 0
					&& application.APPADDR.get(0).PORT.get(0).PORTNBR != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.port_number_label), 
						OmacpApplicationCapability.ds_port_number, application.APPADDR.get(0).PORT.get(0).PORTNBR);
			}
			//auth
			if(application.APPAUTH != null && application.APPAUTH.size() != 0){
				AppAuth auth = application.APPAUTH.get(0);
				if(auth.AAUTHLEVEL != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_level_label), 
							OmacpApplicationCapability.ds_auth_level, auth.AAUTHLEVEL);
				}
				
				if(auth.AAUTHTYPE != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_type_label), 
							OmacpApplicationCapability.ds_auth_type, auth.AAUTHTYPE);
				}
				
				if(auth.AAUTHNAME != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.user_name_label), 
							OmacpApplicationCapability.ds_auth_name, auth.AAUTHNAME);
				}
				
				if(auth.AAUTHSECRET != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.password_label), 
							OmacpApplicationCapability.ds_auth_secret, auth.AAUTHSECRET);
				}
				
				if(auth.AAUTHDATA != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_data_label), 
							OmacpApplicationCapability.ds_auth_data, auth.AAUTHDATA);
				}
			}
			//resource databases
			ArrayList<Resource> resourceList = application.RESOURCE;
			for(int j = 0; j < resourceList.size(); j++){
				Resource resource = resourceList.get(j);
				
				String database = null;
				
				if(resource.URI != null){
					//database name
					if(resource.NAME != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.ds_database_name_label), 
								OmacpApplicationCapability.ds_database_name, resource.NAME);
					}
					//database uri
					if(database == null){
						database = "";					
					}
					database += getElement(context, context.getString(R.string.ds_database_url_label), 
							OmacpApplicationCapability.ds_database_url, resource.URI);
					
					//content type
					if(resource.AACCEPT != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.content_type_label), 
								OmacpApplicationCapability.ds_database_content_type, resource.AACCEPT);
					}
					//auth type
					if(resource.AAUTHTYPE != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.auth_type_label), 
								OmacpApplicationCapability.ds_database_auth_type, resource.AAUTHTYPE);
					}				
					//auth name
					if(resource.AAUTHNAME != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.user_name_label), 
								OmacpApplicationCapability.ds_database_auth_name, resource.AAUTHNAME);
					}
					//auth secret
					if(resource.AAUTHSECRET != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.password_label), 
								OmacpApplicationCapability.ds_database_auth_secret, resource.AAUTHSECRET);
					}
					//cliuri
					if(resource.CLIURI != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.ds_cliuri_label), 
								OmacpApplicationCapability.ds_client_database_url, resource.CLIURI);
					}
					//sync type
					if(resource.SYNCTYPE != null){
						if(database == null){
							database = "";					
						}
						database += getElement(context, context.getString(R.string.ds_sync_type_label), 
								OmacpApplicationCapability.ds_sync_type, resource.SYNCTYPE);
					}
					if(database != null){
						settings += database;
					}
				}
			}
			
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}
			return info;
		}
	}
	
	private static SpannableStringBuilder getSuplSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getSuplSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.supl == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{
			//server id
			if(application.PROVIDER_ID != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_id_label), 
						OmacpApplicationCapability.supl_provider_id, application.PROVIDER_ID);
			}
			
			//server name
			if(application.NAME != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_name_label), 
						OmacpApplicationCapability.supl_server_name, application.NAME);
			}
			
//			//napid
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";
//				}			
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.supl_to_napid, application.TO_NAPID.get(0));
//			}
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.supl_server_addr, application.APPADDR.get(0).ADDR);
			}else if(application.ADDR.size() != 0 && application.ADDR.get(0) != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.supl_server_addr, application.ADDR.get(0));
			}
			
			//addr type
			if(application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDRTYPE != null){
				if(settings == null){
					settings = "";
				}			
				settings += getElement(context, context.getString(R.string.server_addr_type_label), 
						OmacpApplicationCapability.supl_addr_type, application.APPADDR.get(0).ADDRTYPE);
			}
			
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}
			return info;
		}
	}
	
	private static SpannableStringBuilder getDMSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getDMSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.dm == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{
			if(application.PROVIDER_ID != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.server_id_label), 
						OmacpApplicationCapability.dm_provider_id, application.PROVIDER_ID);
			}
			
			if(application.NAME != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.server_name_label), 
						OmacpApplicationCapability.dm_server_name, application.NAME);
			}
			
//			if(application.TO_PROXY != null && application.TO_PROXY.size() != 0 && application.TO_PROXY.get(0) != null){
//				if(settings == null){
//					settings = "";					
//				}
//				settings += getElement(context, context.getString(R.string.proxy_label), 
//						OmacpApplicationCapability.dm_to_proxy, application.TO_PROXY.get(0));
//			}
//			
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";					
//				}
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.dm_to_napid, application.TO_NAPID.get(0));
//			}
			if(application.ADDR != null && application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDR != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.dm_server_address, application.APPADDR.get(0).ADDR);
			}else if(application.ADDR != null && application.ADDR.size() != 0 && application.ADDR.get(0) != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.server_address_label), 
						OmacpApplicationCapability.dm_server_address, application.ADDR.get(0));
			}
			
			if(application.ADDR != null && application.APPADDR.size() != 0 && application.APPADDR.get(0).ADDRTYPE != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.server_addr_type_label), 
						OmacpApplicationCapability.dm_addr_type, application.APPADDR.get(0).ADDRTYPE);
			}
			
			if(application.ADDR != null && application.APPADDR.size() != 0 && application.APPADDR.get(0).PORT.size() != 0
					&& application.APPADDR.get(0).PORT.get(0).PORTNBR != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.port_number_label), 
						OmacpApplicationCapability.dm_port_number, application.APPADDR.get(0).PORT.get(0).PORTNBR);
			}
			
			if(application.APPAUTH != null && application.APPAUTH.size() != 0){
				AppAuth auth = application.APPAUTH.get(0);
				if(auth.AAUTHLEVEL != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_level_label), 
							OmacpApplicationCapability.dm_auth_level, auth.AAUTHLEVEL);
				}
				
				if(auth.AAUTHTYPE != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_type_label), 
							OmacpApplicationCapability.dm_auth_type, auth.AAUTHTYPE);
				}
				
				if(auth.AAUTHNAME != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.user_name_label), 
							OmacpApplicationCapability.dm_auth_name, auth.AAUTHNAME);
				}
				
				if(auth.AAUTHSECRET != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.password_label), 
							OmacpApplicationCapability.dm_auth_secret, auth.AAUTHSECRET);
				}
				
				if(auth.AAUTHDATA != null){
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.auth_data_label), 
							OmacpApplicationCapability.dm_auth_data, auth.AAUTHDATA);
				}
			}
			
			if(application.INIT != null){
				if(settings == null){
					settings = "";					
				}
				settings += "\n";
				settings += context.getString(R.string.dm_init_label);
				if(OmacpApplicationCapability.dm_init == false){
					settings += context.getString(R.string.info_unsupport);
				}else{
					if(application.INIT.equalsIgnoreCase("1")){					
						settings += context.getString(R.string.yes);
					}else{
						settings += context.getString(R.string.no);
					}
				}
			}
			
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}
			return info;
		}
	}
	
	private static SpannableStringBuilder getBrowserSettingInfo(Context context, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpMessageUtils getBrowserSettingInfo application is null.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String settings = null;
		if(OmacpApplicationCapability.browser == false){
			addApplicationLabel(context, info, application.APPID);
			info.append("\n");
			info.append(context.getString(R.string.info_unsupport));
			return info;
		}else{			
//			//proxy
//			if(application.TO_PROXY != null && application.TO_PROXY.size() != 0 && application.TO_PROXY.get(0) != null){
//				if(settings == null){
//					settings = "";					
//				}
//				settings += getElement(context, context.getString(R.string.proxy_label), 
//						OmacpApplicationCapability.browser_to_proxy, application.TO_PROXY.get(0));
//			}
//			
//			//napid
//			if(application.TO_NAPID != null && application.TO_NAPID.size() != 0 && application.TO_NAPID.get(0) != null){
//				if(settings == null){
//					settings = "";					
//				}
//				settings += getElement(context, context.getString(R.string.nap_label), 
//						OmacpApplicationCapability.browser_to_napid, application.TO_NAPID.get(0));
//			}
			
			//bookmark folder
			if(application.NAME != null){
				if(settings == null){
					settings = "";					
				}
				settings += getElement(context, context.getString(R.string.bookmark_folder_label), 
						OmacpApplicationCapability.browser_bookmark_folder, application.NAME);
			}
			
			ArrayList<Resource> resourceList = application.RESOURCE;		
			String homePage = null;
			for(int j = 0; j < resourceList.size(); j++){
				Resource resource = resourceList.get(j);
				if(resource.URI != null){
					if(resource.NAME != null){
						if(settings == null){
							settings = "";					
						}
						settings += getElement(context, context.getString(R.string.bookmark_name_label), 
								OmacpApplicationCapability.browser_bookmark_name, resource.NAME);
					}
					
					if(settings == null){
						settings = "";					
					}
					settings += getElement(context, context.getString(R.string.bookmark_label), 
							OmacpApplicationCapability.browser_bookmark, resource.URI);
					
					if(resource.AAUTHNAME != null){
						if(settings == null){
							settings = "";					
						}
						settings += getElement(context, context.getString(R.string.user_name_label), 
								OmacpApplicationCapability.browser_username, resource.AAUTHNAME);
					}
					
					if(resource.AAUTHSECRET != null){
						if(settings == null){
							settings = "";					
						}
						settings += getElement(context, context.getString(R.string.password_label), 
								OmacpApplicationCapability.browser_password, resource.AAUTHSECRET);
					}
					
					if(resource.STARTPAGE != null && resource.STARTPAGE.equalsIgnoreCase("1")){
						if(homePage == null){
							homePage = resource.URI;
						}
					}
				}
			}
			if(homePage != null){
				settings += "\n";
				settings += context.getString(R.string.homepage_label);
				if(OmacpApplicationCapability.browser_homepage == false){
					settings += context.getString(R.string.info_unsupport);
				}else{
					settings += homePage;
				}				
			}
			if(settings != null){
				addApplicationLabel(context, info, application.APPID);
				info.append(settings);
			}else{
				addApplicationLabel(context, info, application.APPID);
				info.append(context.getString(R.string.info_unavaliable));
			}
			return info;
		}		
		
	}
	
	private static SpannableStringBuilder getApnSettingInfo(Context context, ArrayList<NapdefClass> napList, 
			ArrayList<ApplicationClass> apList, ArrayList<ProxyClass> pxList){
		if(napList == null || napList.size() == 0){
			Xlog.e(XLOG, "OmacpMessageUtils getApnSettingInfo napList is null or size is 0.");
			return null;
		}
		
		SpannableStringBuilder info = new SpannableStringBuilder();
		String napSettings = null;
		int napSize = napList.size();
		for(int i = 0; i < napSize; i ++){
			NapdefClass nap = napList.get(i);
			// BEARER parameter is optional
			// if(nap.BEARER.size() != 0 && nap.BEARER.get(0).equalsIgnoreCase("GSM-GPRS")){

				//Name
				if(nap.NAME != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}

					napSettings += context.getString(R.string.name_label);
					napSettings += nap.NAME;
				}
				
				//APN
				if(nap.NAP_ADDRESS != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.apn_apn_label);
					napSettings += nap.NAP_ADDRESS;
				}
				
				//Proxy and port
				String proxy = null;
				String port = null;
				
				boolean flag = false;
				ProxyClass px = null;
				if(pxList != null){
					for(int n = 0; n < pxList.size(); n ++){
						px = pxList.get(n);
						if(px.PXPHYSICAL != null && px.PXPHYSICAL.size() != 0){
							ArrayList<String> toNapIdList = px.PXPHYSICAL.get(0).TO_NAPID;
							if(toNapIdList != null){
								for(int m = 0; m < toNapIdList.size(); m ++){
									String toNapId = toNapIdList.get(m);
									if(nap.NAPID.equalsIgnoreCase(toNapId)){
										flag = true;
										break;
									}
								}
								if(flag == true){
									break;
								}
							}									
						}								
					}
					
					if(flag == true && px != null){
						if(px.PXPHYSICAL.size() != 0){
							if(px.PXPHYSICAL.get(0).PXADDR != null){
								if(napSettings == null){
									napSettings = "\n";
								}else{
									napSettings += "\n";
								}
								napSettings += context.getString(R.string.proxy_label);
								napSettings += px.PXPHYSICAL.get(0).PXADDR;
								
								proxy = px.PXPHYSICAL.get(0).PXADDR;
							}
							
							if(px.PXPHYSICAL.get(0).PORT.size() != 0){
								if(px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR != null){
									if(napSettings == null){
										napSettings = "\n";
									}else{
										napSettings += "\n";
									}
									napSettings += context.getString(R.string.port_number_label);
									napSettings += px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR;
									
									port = px.PXPHYSICAL.get(0).PORT.get(0).PORTNBR;
								}
							}
						}
					}
				}
				
				//Username
				if(nap.NAPAUTHINFO.size() != 0 && nap.NAPAUTHINFO.get(0).AUTHNAME != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.user_name_label);
					napSettings += nap.NAPAUTHINFO.get(0).AUTHNAME;
				}
				
				//Password
				if(nap.NAPAUTHINFO.size() != 0 && nap.NAPAUTHINFO.get(0).AUTHSECRET != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.password_label);
					napSettings += nap.NAPAUTHINFO.get(0).AUTHSECRET;
				}
				
				//add mms related settings info
				String mmsc = null;
				if(apList != null){
					for(int n = 0; n < apList.size(); n ++){
						if(apList.get(n).APPID.equalsIgnoreCase(MMS_APPID) || apList.get(n).APPID.equalsIgnoreCase(MMS_2_APPID)){
							if(apList.get(n).ADDR.size() != 0 && apList.get(n).ADDR.get(0) != null){
								mmsc = apList.get(n).ADDR.get(0);
							}else if(apList.get(n).APPADDR.size() != 0 && apList.get(n).APPADDR.get(0).ADDR != null){
								mmsc = apList.get(n).APPADDR.get(0).ADDR;
							}
						}
					}
				}
				if(mmsc != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.mmsc_label);
					napSettings += mmsc;
					
					if(proxy != null){
						napSettings += "\n";
						napSettings += context.getString(R.string.apn_mms_proxy_label);
						napSettings += proxy;
					}
					
					if(port != null){
						napSettings += "\n";
						napSettings += context.getString(R.string.apn_mms_port_label);
						napSettings += port;
					}
				}
				
				
				//Authentication type
				if(nap.NAPAUTHINFO.size() != 0 && nap.NAPAUTHINFO.get(0).AUTHTYPE != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.apn_auth_type_label);
					napSettings += nap.NAPAUTHINFO.get(0).AUTHTYPE;
				}
				
				//APN type
				String apnType = getAPNType(apList);					
				if(apnType != null){
					if(napSettings == null){
						napSettings = "\n";
					}else{
						napSettings += "\n";
					}
					
					napSettings += context.getString(R.string.apn_type_label);
					napSettings += apnType;
				}
				
				if(napSettings != null){
					if(i > 0){
						info.append("\n");
					}					
					
					addApplicationLabel(context, info, APN_APPID);
					info.append(napSettings);
					
					napSettings = null;
				}
//			}
			
		}
		return info;
	}
	
	public static String getAPNType(ArrayList<ApplicationClass> apList){
		String apnType = null;
		if(apList != null){
			for(int n = 0; n < apList.size(); n ++){
				if(apList.get(n).APPID.equalsIgnoreCase(BROWSER_APPID)){
					if(apnType != null){
						apnType += ",default";
					}else{
						apnType = "default";
					}
				}else if((apList.get(n).APPID.equalsIgnoreCase(MMS_APPID) || apList.get(n).APPID.equalsIgnoreCase(MMS_2_APPID))
						&& (apnType == null || !apnType.contains("mms"))){
					if(apnType != null){
						apnType += ",mms";
					}else{
						apnType = "mms";
					}
				}else if(apList.get(n).APPID.equalsIgnoreCase(SUPL_APPID)){
					if(apnType != null){
						apnType += ",supl";
					}else{
						apnType = "supl";
					}
				}
			}
		}
		return apnType;
	}

}
