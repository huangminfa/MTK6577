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

package com.mediatek.omacp.parser;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import com.mediatek.xlog.Xlog;

import com.mediatek.omacp.R;
import com.mediatek.omacp.parser.ApplicationClass.AppAddr;
import com.mediatek.omacp.parser.ApplicationClass.AppAuth;
import com.mediatek.omacp.parser.ApplicationClass.Port;
import com.mediatek.omacp.parser.ApplicationClass.Resource;
import com.mediatek.omacp.parser.NapdefClass.NapAuthInfo;
import com.mediatek.omacp.parser.NapdefClass.Validity;
import com.mediatek.omacp.parser.ProxyClass.PxAuthInfo;
import com.mediatek.omacp.parser.ProxyClass.PxPhysical;

public class OmacpParserUtils {

	private static final String XLOG = "Omacp/OmacpParserUtils";
	
	private static final boolean DEBUG = true;
	
	public static void handlePxParameters(String listType, String type, String parmName, String parmValue, ProxyClass px) {
		// TODO Auto-generated method stub
		if(px == null){
			Xlog.e(XLOG, "OmacpParserUtils handlePxParameters px is null.");
			return;
		}
		
		ArrayList<PxAuthInfo> pxAuthInfoList = px.PXAUTHINFO;
		ArrayList<Port> portList = px.PORT;
		ArrayList<PxPhysical> physicalList = px.PXPHYSICAL;
		
		if(type.equalsIgnoreCase("PXLOGICAL")){
			if(parmName.equalsIgnoreCase("PROXY-ID") && px.PROXY_ID == null){
				px.PROXY_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("PROXY-PW") && px.PROXY_PW == null){
				px.PROXY_PW = parmValue;
			}else if(parmName.equalsIgnoreCase("PPGAUTH-TYPE") && px.PPGAUTH_TYPE == null){
				px.PPGAUTH_TYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("PROXY-PROVIDER-ID") && px.PROXY_PROVIDER_ID == null){
				px.PROXY_PROVIDER_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("NAME") && px.NAME == null){
				px.NAME = parmValue;
			}else if(parmName.equalsIgnoreCase("DOMAIN")){
				px.DOMAIN.add(parmValue);
			}else if(parmName.equalsIgnoreCase("TRUST") && px.TRUST == null){
				px.TRUST = "1"; //take no value, just exists
			}else if(parmName.equalsIgnoreCase("MASTER") && px.MASTER == null){
				px.MASTER = "1"; //take no value, just exists
			}else if(parmName.equalsIgnoreCase("STARTPAGE") && px.STARTPAGE == null){
				px.STARTPAGE = parmValue;
			}else if(parmName.equalsIgnoreCase("BASAUTH-ID") && px.BASAUTH_ID == null){
				px.BASAUTH_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("BASAUTH-PW") && px.BASAUTH_PW == null){
				px.BASAUTH_PW = parmValue;
			}else if(parmName.equalsIgnoreCase("WSP-VERSION") && px.WSP_VERSION == null){
				px.WSP_VERSION = parmValue;
			}else if(parmName.equalsIgnoreCase("PUSHENABLED") && px.PUSHENABLED == null){
				px.PUSHENABLED = parmValue;
			}else if(parmName.equalsIgnoreCase("PULLENBALED") && px.PULLENBALED == null){
				px.PULLENBALED = parmValue;
			}
		}else if(type.equalsIgnoreCase("PXAUTHINFO")){
			int size = pxAuthInfoList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handlePxParameters PXAUTHINFO size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("PXAUTH-TYPE") && pxAuthInfoList.get(size - 1).PXAUTH_TYPE == null){
				pxAuthInfoList.get(size - 1).PXAUTH_TYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("PXAUTH-ID") && pxAuthInfoList.get(size - 1).PXAUTH_ID == null){
				pxAuthInfoList.get(size - 1).PXAUTH_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("PXAUTH-PW") && pxAuthInfoList.get(size - 1).PXAUTH_PW == null){
				pxAuthInfoList.get(size - 1).PXAUTH_PW = parmValue;
			}
		}else if(type.equalsIgnoreCase("PORT")){
			int size = portList.size();
			if(listType.equalsIgnoreCase("PXLOGICAL")){
				if(size == 0){
					Xlog.e(XLOG, "OmacpParserUtils handlePxParameters PORT size is 0.");
					return;
				}
				if(parmName.equalsIgnoreCase("PORTNBR") && portList.get(size - 1).PORTNBR == null){
					portList.get(size - 1).PORTNBR = parmValue;
				}else if(parmName.equalsIgnoreCase("SERVICE")){
					portList.get(size - 1).SERVICE.add(parmValue);
				}
			}else if(listType.equalsIgnoreCase("PXPHYSICAL")){
				int pxPhysicalSize = px.PXPHYSICAL.size();
				if(pxPhysicalSize == 0){
					Xlog.e(XLOG, "OmacpParserUtils handlePxParameters PXPHYSICAL size is 0.");
					return;
				}
				int portSize = px.PXPHYSICAL.get(pxPhysicalSize - 1).PORT.size();
				if(portSize == 0){
					Xlog.e(XLOG, "OmacpParserUtils handlePxParameters PXPHYSICAL PORT size is 0.");
					return;
				}
				if(parmName.equalsIgnoreCase("PORTNBR") && px.PXPHYSICAL.get(pxPhysicalSize - 1).PORT.get(portSize - 1).PORTNBR == null){
					px.PXPHYSICAL.get(pxPhysicalSize - 1).PORT.get(portSize - 1).PORTNBR = parmValue;
				}else if(parmName.equalsIgnoreCase("SERVICE")){
					px.PXPHYSICAL.get(pxPhysicalSize - 1).PORT.get(portSize - 1).SERVICE.add(parmValue);
				}
			}
			
		}else if(type.equalsIgnoreCase("PXPHYSICAL")){
			int size = physicalList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handlePxParameters PXPHYSICAL size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("PHYSICAL-PROXY-ID") && physicalList.get(size - 1).PHYSICAL_PROXY_ID == null){
				physicalList.get(size - 1).PHYSICAL_PROXY_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("DOMAIN")){
				physicalList.get(size - 1).DOMAIN.add(parmValue);
			}else if(parmName.equalsIgnoreCase("PXADDR") && physicalList.get(size - 1).PXADDR == null){
				physicalList.get(size - 1).PXADDR = parmValue;
			}else if(parmName.equalsIgnoreCase("PXADDRTYPE") && physicalList.get(size - 1).PXADDRTYPE == null){
				physicalList.get(size - 1).PXADDRTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("PXADDR-FQDN") && physicalList.get(size - 1).PXADDR_FQDN == null){
				physicalList.get(size - 1).PXADDR_FQDN = parmValue;
			}else if(parmName.equalsIgnoreCase("WSP-VERSION") && physicalList.get(size - 1).WSP_VERSION == null){
				physicalList.get(size - 1).WSP_VERSION = parmValue;
			}else if(parmName.equalsIgnoreCase("PUSHENABLED") && physicalList.get(size - 1).PUSHENABLED == null){
				physicalList.get(size - 1).PUSHENABLED = parmValue;
			}else if(parmName.equalsIgnoreCase("PULLENABLED") && physicalList.get(size - 1).PULLENABLED == null){
				physicalList.get(size - 1).PULLENABLED = parmValue;
			}else if(parmName.equalsIgnoreCase("TO-NAPID")){
				physicalList.get(size - 1).TO_NAPID.add(parmValue);
			}
		}

	}

	public static void handleNapParameters(String type, String parmName, String parmValue, NapdefClass nap) {
		// TODO Auto-generated method stub
		if(nap == null){
			Xlog.e(XLOG, "OmacpParserUtils handleNapParameters nap is null.");
			return;
		}
		
		ArrayList<NapAuthInfo> napAuthInfoList = nap.NAPAUTHINFO;
		ArrayList<Validity> validityList = nap.VALIDITY;
		
		if(type.equalsIgnoreCase("NAPDEF")){
			if(parmName.equalsIgnoreCase("NAPID") && nap.NAPID == null){
				nap.NAPID = parmValue;
			}else if(parmName.equalsIgnoreCase("BEARER")){
				nap.BEARER.add(parmValue);
			}else if(parmName.equalsIgnoreCase("NAME") && nap.NAME == null){
				nap.NAME = parmValue;
			}else if(parmName.equalsIgnoreCase("INTERNET") && nap.INTERNET == null){
				nap.INTERNET = "1"; //take no value, just exists
			}else if(parmName.equalsIgnoreCase("NAP-ADDRESS") && nap.NAP_ADDRESS == null){
				nap.NAP_ADDRESS = parmValue;
			}else if(parmName.equalsIgnoreCase("NAP-ADDRTYPE") && nap.NAP_ADDRTYPE == null){
				nap.NAP_ADDRTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("DNS-ADDR")){
				nap.DNS_ADDR.add(parmValue);
			}else if(parmName.equalsIgnoreCase("CALLTYPE") && nap.CALLTYPE == null){
				nap.CALLTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("LOCAL_ADDR") && nap.LOCAL_ADDR == null){
				nap.LOCAL_ADDR = parmValue;
			}else if(parmName.equalsIgnoreCase("LOCAL_ADDRTYPE") && nap.LOCAL_ADDRTYPE == null){
				nap.LOCAL_ADDRTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("LINKSPEED") && nap.LINKSPEED == null){
				nap.LINKSPEED = parmValue;
			}else if(parmName.equalsIgnoreCase("DNLINKSPEED") && nap.DNLINKSPEED == null){
				nap.DNLINKSPEED = parmValue;
			}else if(parmName.equalsIgnoreCase("LINGER") && nap.LINGER == null){
				nap.LINGER = parmValue;
			}else if(parmName.equalsIgnoreCase("DELIVERY-ERR-SDU") && nap.DELIVERY_ERR_SDU == null){
				nap.DELIVERY_ERR_SDU = parmValue;
			}else if(parmName.equalsIgnoreCase("DELIVERY-ORDER") && nap.DELIVERY_ORDER == null){
				nap.DELIVERY_ORDER = parmValue;
			}else if(parmName.equalsIgnoreCase("TRAFFIC-CLASS") && nap.TRAFFIC_CLASS == null){
				nap.TRAFFIC_CLASS = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-SDU-SIZE") && nap.MAX_SDU_SIZE == null){
				nap.MAX_SDU_SIZE = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-BITRATE-UPLINK") && nap.MAX_BITRATE_UPLINK == null){
				nap.MAX_BITRATE_UPLINK = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-BITRATE-DNLINK") && nap.MAX_BITRATE_DNLINK == null){
				nap.MAX_BITRATE_DNLINK = parmValue;
			}else if(parmName.equalsIgnoreCase("RESIDUAL-BER") && nap.RESIDUAL_BER == null){
				nap.RESIDUAL_BER = parmValue;
			}else if(parmName.equalsIgnoreCase("SDU-ERROR-RATIO") && nap.SDU_ERROR_RATIO == null){
				nap.SDU_ERROR_RATIO = parmValue;
			}else if(parmName.equalsIgnoreCase("TRAFFIC-HANDL-PROI") && nap.TRAFFIC_HANDL_PROI == null){
				nap.TRAFFIC_HANDL_PROI = parmValue;
			}else if(parmName.equalsIgnoreCase("TRANSFER-DELAY") && nap.TRANSFER_DELAY == null){
				nap.TRANSFER_DELAY = parmValue;
			}else if(parmName.equalsIgnoreCase("GUARANTEED-BITRATE-UPLINK") && nap.GUARANTEED_BITRATE_UPLINK == null){
				nap.GUARANTEED_BITRATE_UPLINK = parmValue;
			}else if(parmName.equalsIgnoreCase("GUARANTEED-BITRATE-DNLINK") && nap.GUARANTEED_BITRATE_DNLINK == null){
				nap.GUARANTEED_BITRATE_DNLINK = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-NUM-RETRY") && nap.MAX_NUM_RETRY == null){
				nap.MAX_NUM_RETRY = parmValue;
			}else if(parmName.equalsIgnoreCase("FIRST-RETRY-TIMEOUT") && nap.FIRST_RETRY_TIMEOUT == null){
				nap.FIRST_RETRY_TIMEOUT = parmValue;
			}else if(parmName.equalsIgnoreCase("REREG-THRESHOLD") && nap.REREG_THRESHOLD == null){
				nap.REREG_THRESHOLD = parmValue;
			}else if(parmName.equalsIgnoreCase("T-BIT") && nap.T_BIT == null){
				nap.T_BIT = "1"; //take no value, just exists
			}			
		}else if(type.equalsIgnoreCase("NAPAUTHINFO")){
			int size = napAuthInfoList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleNapParameters NAPAUTHINFO size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("AUTHTYPE") && napAuthInfoList.get(size - 1).AUTHTYPE == null){
				napAuthInfoList.get(size - 1).AUTHTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("AUTHNAME") && napAuthInfoList.get(size - 1).AUTHNAME == null){
				napAuthInfoList.get(size - 1).AUTHNAME = parmValue;
			}else if(parmName.equalsIgnoreCase("AUTHSECRET") && napAuthInfoList.get(size - 1).AUTHSECRET == null){
				napAuthInfoList.get(size - 1).AUTHSECRET = parmValue;
			}else if(parmName.equalsIgnoreCase("AUTH_ENTITY")){
				napAuthInfoList.get(size - 1).AUTH_ENTITY.add(parmValue);
			}else if(parmName.equalsIgnoreCase("SPI") && napAuthInfoList.get(size - 1).SPI == null){
				napAuthInfoList.get(size - 1).SPI = parmValue;
			}
		}else if(type.equalsIgnoreCase("VALIDITY")){
			int size = validityList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleNapParameters VALIDITY size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("COUNTRY") && validityList.get(size - 1).COUNTRY == null){
				validityList.get(size - 1).COUNTRY = parmValue;
			}else if(parmName.equalsIgnoreCase("NETWORK") && validityList.get(size - 1).NETWORK == null){
				validityList.get(size - 1).NETWORK = parmValue;
			}else if(parmName.equalsIgnoreCase("SID") && validityList.get(size - 1).SID == null){
				validityList.get(size - 1).SID = parmValue;
			}else if(parmName.equalsIgnoreCase("SOC") && validityList.get(size - 1).SOC == null){
				validityList.get(size - 1).SOC = parmValue;
			}else if(parmName.equalsIgnoreCase("VALIDUNTIL") && validityList.get(size - 1).VALIDUNTIL == null){
				validityList.get(size - 1).VALIDUNTIL = parmValue;
			}
		}
	}

	public static void handleApParameters(String type, String parmName, String parmValue, ApplicationClass application){
		if(application == null){
			Xlog.e(XLOG, "OmacpParserUtils handleApParameters application is null.");
			return;
		}
		
		ArrayList<AppAddr> appAddrList = application.APPADDR;
		ArrayList<AppAuth> appAuthList = application.APPAUTH;
		ArrayList<Resource> resourceList = application.RESOURCE;
		
		if(type.equalsIgnoreCase("APPLICATION")){ //application part
			if(parmName.equalsIgnoreCase("APPID") && application.APPID == null){
				application.APPID = parmValue;
			}else if(parmName.equalsIgnoreCase("PROVIDER-ID") && application.PROVIDER_ID == null){
				application.PROVIDER_ID = parmValue;
			}else if(parmName.equalsIgnoreCase("NAME") && application.NAME == null){
				application.NAME = parmValue;
			}else if(parmName.equalsIgnoreCase("AACCEPT") && application.AACCEPT == null){
				application.AACCEPT = parmValue;
			}else if(parmName.equalsIgnoreCase("APROTOCOL") && application.APROTOCOL == null){
				application.APROTOCOL = parmValue;
			}else if(parmName.equalsIgnoreCase("TO-PROXY")){
				application.TO_PROXY.add(parmValue);
			}else if(parmName.equalsIgnoreCase("TO-NAPID")){
				application.TO_NAPID.add(parmValue);
			}else if(parmName.equalsIgnoreCase("ADDR") && type.equalsIgnoreCase("APPLICATION")){
				application.ADDR.add(parmValue);
			}//application specefic parameters
			else if(parmName.equalsIgnoreCase("CM") && application.CM == null){
				application.CM = parmValue;
			}else if(parmName.equalsIgnoreCase("RM") && application.RM == null){
				application.RM = parmValue;
			}else if(parmName.equalsIgnoreCase("MS") && application.MS == null){
				application.MS = parmValue;
			}else if(parmName.equalsIgnoreCase("PC-ADDR") && application.PC_ADDR == null){
				application.PC_ADDR = parmValue;
			}else if(parmName.equalsIgnoreCase("Ma") && application.Ma == null){
				application.Ma = parmValue;
			}else if(parmName.equalsIgnoreCase("INIT") && application.INIT == null){
				application.INIT = "1";
			}else if(parmName.equalsIgnoreCase("FROM") && application.FROM == null){
				application.FROM = parmValue;
			}else if(parmName.equalsIgnoreCase("RT-ADDR") && application.RT_ADDR == null){
				application.RT_ADDR = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-BANDWIDTH") && application.MAX_BANDWIDTH == null){
				application.MAX_BANDWIDTH = parmValue;
			}else if(parmName.equalsIgnoreCase("NETINFO")){
				application.NETINFO.add(parmValue);
			}else if(parmName.equalsIgnoreCase("MIN-UDP-PORT") && application.MIN_UDP_PORT == null){
				application.MIN_UDP_PORT = parmValue;
			}else if(parmName.equalsIgnoreCase("MAX-UDP-PORT") && application.MAX_UDP_PORT == null){
				application.MAX_UDP_PORT = parmValue;
			}else if(parmName.equalsIgnoreCase("SERVICES") && application.SERVICES == null){
				application.SERVICES = parmValue;
			}else if(parmName.equalsIgnoreCase("CIDPREFIX") && application.CIDPREFIX == null){
				application.CIDPREFIX = parmValue;
			}
		}else if(type.equalsIgnoreCase("APPADDR")){ //appaddr part
			int size = appAddrList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleApParameters APPADDR size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("ADDR") && appAddrList.get(size - 1).ADDR == null){
				appAddrList.get(size - 1).ADDR = parmValue;
			}else if(parmName.equalsIgnoreCase("ADDRTYPE") && application.APPADDR.get(size - 1).ADDRTYPE == null){
				appAddrList.get(size - 1).ADDRTYPE = parmValue;
			}
		}else if(type.equalsIgnoreCase("PORT")){ //port part
			int addrSize = appAddrList.size();
			if(addrSize == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleApParameters APPADDR size is 0.");
				return;
			}
			int size = appAddrList.get(addrSize - 1).PORT.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleApParameters PORT size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("PORTNBR") && appAddrList.get(addrSize - 1).PORT.get(size - 1).PORTNBR == null){
				appAddrList.get(addrSize - 1).PORT.get(size - 1).PORTNBR = parmValue;
			}else if(parmName.equalsIgnoreCase("SERVICE")){
				appAddrList.get(addrSize - 1).PORT.get(size - 1).SERVICE.add(parmValue);
			}
		}else if(type.equalsIgnoreCase("APPAUTH")){ //appauth part
			int size = appAuthList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleApParameters APPAUTH size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("AAUTHLEVEL") && appAuthList.get(size - 1).AAUTHLEVEL == null){
				appAuthList.get(size - 1).AAUTHLEVEL = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHTYPE") && appAuthList.get(size - 1).AAUTHTYPE == null){
				appAuthList.get(size - 1).AAUTHTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHNAME") && appAuthList.get(size - 1).AAUTHNAME == null){
				appAuthList.get(size - 1).AAUTHNAME = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHSECRET") && appAuthList.get(size - 1).AAUTHSECRET == null){
				appAuthList.get(size - 1).AAUTHSECRET = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHDATA") && appAuthList.get(size - 1).AAUTHDATA == null){
				appAuthList.get(size - 1).AAUTHDATA = parmValue;
			}
		}else if(type.equalsIgnoreCase("RESOURCE")){ //resource part
			int size = resourceList.size();
			if(size == 0){
				Xlog.e(XLOG, "OmacpParserUtils handleApParameters RESOURCE size is 0.");
				return;
			}
			if(parmName.equalsIgnoreCase("URI") && resourceList.get(size - 1).URI == null){
				resourceList.get(size - 1).URI = parmValue;
			}else if(parmName.equalsIgnoreCase("NAME") && resourceList.get(size - 1).NAME == null){
				resourceList.get(size - 1).NAME = parmValue;
			}else if(parmName.equalsIgnoreCase("AACCEPT") && resourceList.get(size - 1).AACCEPT == null){
				resourceList.get(size - 1).AACCEPT = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHTYPE") && resourceList.get(size - 1).AAUTHTYPE == null){
				resourceList.get(size - 1).AAUTHTYPE = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHNAME") && resourceList.get(size - 1).AAUTHNAME == null){
				resourceList.get(size - 1).AAUTHNAME = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHSECRET") && resourceList.get(size - 1).AAUTHSECRET == null){
				resourceList.get(size - 1).AAUTHSECRET = parmValue;
			}else if(parmName.equalsIgnoreCase("AAUTHDATA") && resourceList.get(size - 1).AAUTHDATA == null){
				resourceList.get(size - 1).AAUTHDATA = parmValue;
			}else if(parmName.equalsIgnoreCase("STARTPAGE") && resourceList.get(size - 1).STARTPAGE == null){
				resourceList.get(size - 1).STARTPAGE = "1"; //take no value, just exists
			}else if(parmName.equalsIgnoreCase("CLIURI") && resourceList.get(size - 1).CLIURI == null){
				resourceList.get(size - 1).CLIURI = parmName; //take no value, just exists
			}else if(parmName.equalsIgnoreCase("SYNCTYPE") && resourceList.get(size - 1).SYNCTYPE == null){
				resourceList.get(size - 1).SYNCTYPE = parmName; //take no value, just exists
			}
		}
	}
	
	public static ArrayList<ApplicationClass> removeInvalidApSettings(ArrayList<ApplicationClass> apList){
		if(apList == null){
			Xlog.e(XLOG, "OmacpParserUtils removeDuplicateApSettings apList is null.");
			return null;
		}
		
		//remove duplicate application settings
		for(int i = 0; i < apList.size(); i ++){
			String appId = apList.get(i).APPID;
			if(null == appId){
				apList.remove(i);
				continue;
			}
			
			for(int j = i + 1; j < apList.size(); j ++){
				if(apList.get(j).APPID.equalsIgnoreCase(appId)){
					if(DEBUG){
						Xlog.i(XLOG, "OmacpParserUtils removeDuplicateApSettings duplicate application settings, " +
								"will remove " + j + " " + "element");
					}
					
					apList.remove(j);
					j --;
				}
			}
		}
		
		return apList;
	}

}
