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

public class ApplicationClass {
	
	public String APPID;
	public String PROVIDER_ID;
	public String NAME;
	public String AACCEPT;
	public String APROTOCOL;
	
	public ArrayList<String> TO_PROXY = new ArrayList<String>();
	public ArrayList<String> TO_NAPID = new ArrayList<String>();
	public ArrayList<String> ADDR = new ArrayList<String>();
	
	public ArrayList<AppAddr> APPADDR = new ArrayList<AppAddr>();
	public ArrayList<AppAuth> APPAUTH = new ArrayList<AppAuth>();
	public ArrayList<Resource> RESOURCE = new ArrayList<Resource>();
	
	//OMNA denifition attributes
	
	//MMS
	public String CM;
	public String RM;
	public String MS;
	public String PC_ADDR;
	public String Ma;
	
	//DM
	public String INIT;
	
	//SMTP
	public String FROM;
	public String RT_ADDR;
	
	//RTSP
	public String MAX_BANDWIDTH;
	public ArrayList<String> NETINFO = new ArrayList<String>();
	public String MIN_UDP_PORT;
	public String MAX_UDP_PORT;
	
	//IMPS
	public String SERVICES;
	public String CIDPREFIX;
	
	public static class AppAddr{
		public String ADDR;
		public String ADDRTYPE;
		public ArrayList<Port> PORT = new ArrayList<Port>();
		
		@Override
	    public String toString() {
			return "ADDR: " + ADDR + "\n"
			+ "ADDRTYPE: " + ADDRTYPE + "\n"
			+ "PORT: " + PORT  + "\n";
		}
	}
	
	public static class Port{
		public String PORTNBR;
		public ArrayList<String> SERVICE = new ArrayList<String>();
		
		@Override
	    public String toString() {
			return "PORTNBR: " + PORTNBR + "\n"
			+ "SERVICE: " + SERVICE  + "\n";
		}
	}
	
	public static class AppAuth{
		public String AAUTHLEVEL;
		public String AAUTHTYPE;
		public String AAUTHNAME;
		public String AAUTHSECRET;
		public String AAUTHDATA;
		
		@Override
	    public String toString() {
			return "AAUTHLEVEL: " + AAUTHLEVEL + "\n"
			+ "AAUTHTYPE: " + AAUTHTYPE + "\n"
			+ "AAUTHNAME: " + AAUTHNAME + "\n"
			+ "AAUTHSECRET: " + AAUTHSECRET + "\n"
			+ "AAUTHDATA: " + AAUTHDATA  + "\n";
		}
	}
	
	public static class Resource{
		public String URI;
		public String NAME;
		public String AACCEPT;
		public String AAUTHTYPE;
		public String AAUTHNAME;
		public String AAUTHSECRET;
		public String AAUTHDATA;
		public String STARTPAGE;
		//DS
		public String CLIURI;
		public String SYNCTYPE;
		
		@Override
	    public String toString() {
			return "URI: " + URI + "\n"
			+ "NAME: " + NAME + "\n"
			+ "AACCEPT: " + AACCEPT + "\n"
			+ "AAUTHTYPE: " + AAUTHTYPE + "\n"
			+ "AAUTHNAME: " + AAUTHNAME + "\n"
			+ "AAUTHSECRET: " + AAUTHSECRET + "\n"
			+ "AAUTHDATA: " + AAUTHDATA + "\n"
			+ "STARTPAGE: " + STARTPAGE  + "\n";
		}
	}
	
	@Override
    public String toString() {
		return "APPID: " + APPID + "\n"
		+ "PROVIDER_ID: " + PROVIDER_ID + "\n"
		+ "NAME: " + NAME + "\n"
		+ "AACCEPT: " + AACCEPT + "\n"
		+ "APROTOCOL: " + APROTOCOL + "\n"
		+ "TO_PROXY: " + TO_PROXY + "\n"
		+ "TO_NAPID: " + TO_NAPID + "\n"
		+ "ADDR: " + ADDR + "\n"
		+ "APPADDR: " + APPADDR + "\n"
		+ "APPAUTH: " + APPAUTH + "\n"
		+ "RESOURCE: " + RESOURCE  + "\n";
	}

}
