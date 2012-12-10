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

import com.mediatek.omacp.parser.ApplicationClass.Port;

public class ProxyClass {
	
	public String PROXY_ID;
	public String PROXY_PW;
	public String PPGAUTH_TYPE;
	public String PROXY_PROVIDER_ID;
	public String NAME;
	public ArrayList<String> DOMAIN = new ArrayList<String>();
	public String TRUST;
	public String MASTER;
	public String STARTPAGE;
	public String BASAUTH_ID;
	public String BASAUTH_PW;
	public String WSP_VERSION;
	public String PUSHENABLED;
	public String PULLENBALED;
	
	public ArrayList<PxAuthInfo> PXAUTHINFO = new ArrayList<PxAuthInfo>();
	public ArrayList<Port> PORT = new ArrayList<Port>();
	public ArrayList<PxPhysical> PXPHYSICAL = new ArrayList<PxPhysical>();
	
	public static class PxAuthInfo{
		public String PXAUTH_TYPE;
		public String PXAUTH_ID;
		public String PXAUTH_PW;
		
		@Override
	    public String toString() {
			return "PXAUTH_TYPE: " + PXAUTH_TYPE + "\n"
			+ "PXAUTH_ID: " + PXAUTH_ID + "\n"
			+ "PXAUTH_PW: " + PXAUTH_PW + "\n";
		}
	}
	
	public static class PxPhysical{
		public String PHYSICAL_PROXY_ID;
		public ArrayList<String> DOMAIN = new ArrayList<String>();
		public String PXADDR;
		public String PXADDRTYPE;
		public String PXADDR_FQDN;
		public String WSP_VERSION;
		public String PUSHENABLED;
		public String PULLENABLED;
		public ArrayList<String> TO_NAPID = new ArrayList<String>();
		
		public ArrayList<Port> PORT = new ArrayList<Port>();
		
		@Override
	    public String toString() {
			return "PHYSICAL_PROXY_ID: " + PHYSICAL_PROXY_ID + "\n"
			+ "DOMAIN: " + DOMAIN + "\n"
			+ "PXADDR: " + PXADDR + "\n"
			+ "PXADDRTYPE: " + PXADDRTYPE + "\n"
			+ "PXADDR_FQDN: " + PXADDR_FQDN + "\n"
			+ "WSP_VERSION: " + WSP_VERSION + "\n"
			+ "PUSHENABLED: " + PUSHENABLED + "\n"
			+ "PULLENABLED: " + PULLENABLED + "\n"
			+ "TO_NAPID: " + TO_NAPID + "\n"
			+ "PORT: " + PORT + "\n";
		}
	}
	
	@Override
    public String toString() {
		return "PROXY_ID: " + PROXY_ID + "\n"
		+ "PROXY_PW: " + PROXY_PW + "\n"
		+ "PPGAUTH_TYPE: " + PPGAUTH_TYPE + "\n"
		+ "PROXY_PROVIDER_ID: " + PROXY_PROVIDER_ID + "\n"
		+ "NAME: " + NAME + "\n"
		+ "DOMAIN: " + DOMAIN + "\n"
		+ "TRUST: " + TRUST + "\n"
		+ "MASTER: " + MASTER + "\n"
		+ "STARTPAGE: " + STARTPAGE + "\n"
		+ "BASAUTH_ID: " + BASAUTH_ID + "\n"
		+ "BASAUTH_PW: " + BASAUTH_PW + "\n"
		+ "WSP_VERSION: " + WSP_VERSION + "\n"
		+ "PUSHENABLED: " + PUSHENABLED + "\n"
		+ "PULLENBALED: " + PULLENBALED + "\n"
		+ "PXAUTHINFO: " + PXAUTHINFO + "\n"
		+ "PORT: " + PORT + "\n"
		+ "PXPHYSICAL: " + PXPHYSICAL + "\n";
	}
	
}
