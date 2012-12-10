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

public class NapdefClass {
	
	public String NAPID;
	public ArrayList<String> BEARER = new ArrayList<String>();
	public String NAME;
	public String INTERNET;
	public String NAP_ADDRESS;
	public String NAP_ADDRTYPE;
	public ArrayList<String> DNS_ADDR = new ArrayList<String>();
	public String CALLTYPE;
	public String LOCAL_ADDR;
	public String LOCAL_ADDRTYPE;
	public String LINKSPEED;
	public String DNLINKSPEED;
	public String LINGER;
	public String DELIVERY_ERR_SDU;
	public String DELIVERY_ORDER;
	public String TRAFFIC_CLASS;
	public String MAX_SDU_SIZE;
	public String MAX_BITRATE_UPLINK;
	public String MAX_BITRATE_DNLINK;
	public String RESIDUAL_BER;
	public String SDU_ERROR_RATIO;
	public String TRAFFIC_HANDL_PROI;
	public String TRANSFER_DELAY;
	public String GUARANTEED_BITRATE_UPLINK;
	public String GUARANTEED_BITRATE_DNLINK;
	public String MAX_NUM_RETRY;
	public String FIRST_RETRY_TIMEOUT;
	public String REREG_THRESHOLD;
	public String T_BIT;
	
	public ArrayList<NapAuthInfo> NAPAUTHINFO = new ArrayList<NapAuthInfo>();
	public ArrayList<Validity> VALIDITY = new ArrayList<Validity>();
	
	public static class NapAuthInfo{
		public String AUTHTYPE;
		public String AUTHNAME;
		public String AUTHSECRET;
		public ArrayList<String> AUTH_ENTITY = new ArrayList<String>();
		public String SPI;
		
		@Override
	    public String toString() {
			return "AUTHTYPE: " + AUTHTYPE + "\n"
			+ "AUTHNAME: " + AUTHNAME + "\n"
			+ "AUTHSECRET: " + AUTHSECRET + "\n"
			+ "AUTH_ENTITY: " + AUTH_ENTITY + "\n"
			+ "SPI: " + SPI + "\n";
		}
	}
	
	public static class Validity{
		public String COUNTRY;
		public String NETWORK;
		public String SID;
		public String SOC;
		public String VALIDUNTIL;
		
		@Override
	    public String toString() {
			return "COUNTRY: " + COUNTRY + "\n"
			+ "NETWORK: " + NETWORK + "\n"
			+ "SID: " + SID + "\n"
			+ "SOC: " + SOC + "\n"
			+ "VALIDUNTIL: " + VALIDUNTIL + "\n";
		}
	}
	
	@Override
    public String toString() {
		return "APPID: " + NAPID + "\n"
		+ "BEARER: " + BEARER + "\n"
		+ "NAME: " + NAME + "\n"
		+ "INTERNET: " + INTERNET + "\n"
		+ "NAP_ADDRESS: " + NAP_ADDRESS + "\n"
		+ "NAP_ADDRTYPE: " + NAP_ADDRTYPE + "\n"
		+ "DNS_ADDR: " + DNS_ADDR + "\n"
		+ "CALLTYPE: " + CALLTYPE + "\n"
		+ "LOCAL_ADDR: " + LOCAL_ADDR + "\n"
		+ "LOCAL_ADDRTYPE: " + LOCAL_ADDRTYPE + "\n"
		+ "LINKSPEED: " + LINKSPEED + "\n"
		+ "DNLINKSPEED: " + DNLINKSPEED + "\n"
		+ "LINGER: " + LINGER + "\n"
		+ "DELIVERY_ERR_SDU: " + DELIVERY_ERR_SDU + "\n"
		+ "DELIVERY_ORDER: " + DELIVERY_ORDER + "\n"
		+ "TRAFFIC_CLASS: " + TRAFFIC_CLASS + "\n"
		+ "MAX_SDU_SIZE: " + MAX_SDU_SIZE + "\n"
		+ "MAX_BITRATE_UPLINK: " + MAX_BITRATE_UPLINK + "\n"
		+ "MAX_BITRATE_DNLINK: " + MAX_BITRATE_DNLINK + "\n"
		+ "RESIDUAL_BER: " + RESIDUAL_BER + "\n"
		+ "SDU_ERROR_RATIO: " + SDU_ERROR_RATIO + "\n"
		+ "TRAFFIC_HANDL_PROI: " + TRAFFIC_HANDL_PROI + "\n"
		+ "TRANSFER_DELAY: " + TRANSFER_DELAY + "\n"
		+ "GUARANTEED_BITRATE_UPLINK: " + GUARANTEED_BITRATE_UPLINK + "\n"
		+ "GUARANTEED_BITRATE_DNLINK: " + GUARANTEED_BITRATE_DNLINK + "\n"
		+ "MAX_NUM_RETRY: " + MAX_NUM_RETRY + "\n"
		+ "FIRST_RETRY_TIMEOUT: " + FIRST_RETRY_TIMEOUT + "\n"
		+ "REREG_THRESHOLD: " + REREG_THRESHOLD + "\n"
		+ "T_BIT: " + T_BIT + "\n"
		+ "NAPAUTHINFO: " + NAPAUTHINFO + "\n"
		+ "VALIDITY: " + VALIDITY + "\n";
	}
}
