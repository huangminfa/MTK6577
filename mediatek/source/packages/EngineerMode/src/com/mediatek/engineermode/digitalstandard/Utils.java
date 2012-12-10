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

package com.mediatek.engineermode.digitalstandard;

import android.content.Context;
import com.android.internal.telephony.Phone;
import android.provider.Telephony;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

/**
 * Contains utility functions for getting framework resource
 */
public class Utils {


	static int getStatusResource(int state) {
		
	    Elog.i("Utils gemini", "!!!!!!!!!!!!!state is "+state);
		switch (state) {
		case Phone.SIM_INDICATOR_RADIOOFF:
			return com.mediatek.internal.R.drawable.sim_radio_off;
		case Phone.SIM_INDICATOR_LOCKED:
			return com.mediatek.internal.R.drawable.sim_locked;
		case Phone.SIM_INDICATOR_INVALID:
			return com.mediatek.internal.R.drawable.sim_invalid;
		case Phone.SIM_INDICATOR_SEARCHING:
			return com.mediatek.internal.R.drawable.sim_searching;
		case Phone.SIM_INDICATOR_ROAMING:
			return com.mediatek.internal.R.drawable.sim_roaming;
		case Phone.SIM_INDICATOR_CONNECTED:
			return com.mediatek.internal.R.drawable.sim_connected;
		case Phone.SIM_INDICATOR_ROAMINGCONNECTED:
			return com.mediatek.internal.R.drawable.sim_roaming_connected;
		default:
			return -1;
		}
	}
	
	static int getSimColorResource(int color) {
		
		if((color>=0)&&(color<=7)) {
			return Telephony.SIMBackgroundRes[color];
		} else {
			return -1;
		}

		
	}
	static int getNetworkMode(int mode) {
		
		int networkMode = 0;
		switch (mode) {

		case Phone.NT_MODE_WCDMA_ONLY:			
		case Phone.NT_MODE_GSM_ONLY:			
		case Phone.NT_MODE_WCDMA_PREF:
			networkMode = mode;
			break;
		default:
			networkMode = Phone.PREFERRED_NT_MODE;
		}
		
		return networkMode;
		
	}
	
	static final int TYPE_VOICECALL = 1;
	static final int TYPE_VIDEOCALL = 2;
	static final int TYPE_SMS = 3;
	static final int TYPE_GPRS = 4;
	
	static boolean mNeed3GText = false;
    static final String OLD_NETWORK_MODE = "com.android.phone.OLD_NETWORK_MODE";
    static final String NEW_NETWORK_MODE = "NEW_NETWORK_MODE";

    static final String NETWORK_MODE_CHANGE_BROADCAST = "com.android.phone.NETWORK_MODE_CHANGE";
    static final String NETWORK_MODE_CHANGE_RESPONSE = "com.android.phone.NETWORK_MODE_CHANGE_RESPONSE";


}
