/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.target.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context; 
import android.provider.Telephony.SIMInfo;
import android.telephony.TelephonyManager;

import com.mediatek.apst.target.data.provider.message.SmsContent;
import com.mediatek.apst.util.command.sysinfo.SimDetailInfo;
import com.mediatek.apst.util.entity.contacts.RawContact;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;

public abstract class Global {
	// ==============================================================
	// Constants
	// ==============================================================
	// 400K is fastest on emulator
	// private static final int DEFAULT_BUFFER_SIZE_EMULATOR = 400000;
	// 800K is fastest on OPPO
	// private static final int DEFAULT_BUFFER_SIZE_TARGET = 800000;
	// public static final int DEFAULT_BUFFER_SIZE_PC = 400000;
	public static final int DEFAULT_BUFFER_SIZE = 800000;
	
	// mContext will be initialed in MainService. Added by Shaoying Han
	public static Context mContext;

	// ==============================================================
	// Fields
	// ==============================================================
	private static ByteBuffer mByteBuffer = ByteBuffer
			.allocateDirect(DEFAULT_BUFFER_SIZE);

	// ==============================================================
	// Constructors
	// ==============================================================

	// ==============================================================
	// Getters
	// ==============================================================
	public static ByteBuffer getByteBuffer() {
		return mByteBuffer;
	}

	public static String getSimName(int simId) {
		String simName = "";
        Debugger.logD(new Object[] { simId }, "The simId is " + simId);
		if (simId < SmsContent.SIM_ID_MIM || simId > SmsContent.SIM_ID_MAX) {
			Debugger.logW("The simId is wrong!");
			return simName;
		}
		if (null != mContext) {
			try {
				SIMInfo info = SIMInfo.getSIMInfoById(mContext, simId);
				if ( null!=info ) {
					simName = info.mDisplayName;
					Debugger.logD(new Object[] { simId }, "The simName is " + simName);
				}
			} catch (Exception e) {
				Debugger.logW("Exception when getSIMInfoById ");
				simName = "Exception";
			}
		} else {
			Debugger.logW("mContext is null");
		}
		
		return simName;
	}
	
	public static int getSourceLocationById(int indicateSimOrPhone) {
		int simSlot = RawContact.SOURCE_NONE;
		Debugger.logD(new Object[] { indicateSimOrPhone }, "The indicateSimOrPhone is " + indicateSimOrPhone);
		if (indicateSimOrPhone == -1) {
			return RawContact.SOURCE_PHONE;
		}
		if (indicateSimOrPhone < SmsContent.SIM_ID_MIM || indicateSimOrPhone > SmsContent.SIM_ID_MAX) {
			return simSlot;
		}
		if (null != mContext) { 
			try {
				SIMInfo info = SIMInfo.getSIMInfoById(mContext, indicateSimOrPhone);
				if ( null!=info ) {
					if (Config.MTK_GEMINI_SUPPORT) {
						simSlot = info.mSlot + 1;
					} else {
						simSlot = info.mSlot;
					}
				}
				
			} catch (Exception e) {
				Debugger.logW("Exception when getSIMInfoById ");
				simSlot = RawContact.SOURCE_NONE;
			}
			
			Debugger.logW("The sim slot from SIMInfo is " + simSlot);
		} else {
			Debugger.logW("mContext is null");
		}
		
		return simSlot;
	}
	
	public static SimDetailInfo getSimInfoById(int simId) {
		if (simId < SmsContent.SIM_ID_MIM || simId > SmsContent.SIM_ID_MAX) {
			Debugger.logW("The simId is wrong! The simId is " + simId);
			return new SimDetailInfo();
		}
		if (null != mContext) {
			try {
				SIMInfo info = SIMInfo.getSIMInfoById(mContext, simId);
				SimDetailInfo deailInfo = new SimDetailInfo();
				if ( null!=info ) {
					deailInfo.setColor(info.mColor);
					deailInfo.setDisplayName(info.mDisplayName);
					deailInfo.setNumber(info.mNumber);
					deailInfo.setSimId((int)info.mSimId);
					deailInfo.setICCId(info.mICCId);
					deailInfo.setSlotId(info.mSlot);
				}
				return deailInfo;
			} catch (Exception e) {
				Debugger.logW("Exception when getSIMInfoById ");
			}
			
		} else {
			Debugger.logW("mContext is null");
		}
		
		return new SimDetailInfo();
	}
	
	public static SimDetailInfo getSimInfoBySlot(int slotId) {
		Debugger.logD(new Object[] { slotId }, "The slotId is " + slotId);
		if (null != mContext) {
			try {
				SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, slotId);
				SimDetailInfo deailInfo = new SimDetailInfo();
				if ( null!=info ) {
					deailInfo.setColor(info.mColor);
					deailInfo.setDisplayName(info.mDisplayName);
					deailInfo.setNumber(info.mNumber);
					deailInfo.setSimId((int)info.mSimId);
					deailInfo.setICCId(info.mICCId);
					deailInfo.setSlotId(info.mSlot);
				}			
	            final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
	                    .getService(Context.TELEPHONY_SERVICE));
                if (Config.MTK_GEMINI_SUPPORT) {
                    if ("USIM".equals(iTel.getIccCardTypeGemini(slotId))) {
                        deailInfo.setSIMType(SimDetailInfo.SIM_TYPE_USIM);
                    } else {
                        deailInfo.setSIMType(SimDetailInfo.SIM_TYPE_SIM);
                    }
                } else {
                    if ("USIM".equals(iTel.getIccCardType())) {
                        deailInfo.setSIMType(SimDetailInfo.SIM_TYPE_USIM);
                    } else {
                        deailInfo.setSIMType(SimDetailInfo.SIM_TYPE_SIM);
                    }
                }                
				return deailInfo;
			} catch (Exception e) {
			    e.printStackTrace();
				Debugger.logW("Exception when getSIMInfoById ");
			}						
        } else {
            Debugger.logW("mContext is null");
		}
		
		return new SimDetailInfo();
    }
	
	public static int getSimIdBySlot(int slotId) {
	    Debugger.logD(new Object[] { slotId }, "The slotId is " + slotId);
	    if (null != mContext) {
            try {
                SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, slotId);
                if ( null!=info ) {
                    Debugger.logD(new Object[] { slotId }, "The simId is " + (int)info.mSimId);
                    return  (int)info.mSimId;
                }                          
            } catch (Exception e) {
                e.printStackTrace();
                Debugger.logE("Exception when getSIMInfoById ");
            }                       
        } else {
            Debugger.logW("mContext is null");
        }        
        return -1;	    
	}

    public static List<SimDetailInfo> getAllSIMList() {
        if (null != mContext) {
            List<SimDetailInfo> detailInfoList = new ArrayList<SimDetailInfo>();
            try {
                List<SIMInfo> list = SIMInfo.getAllSIMList(mContext);
                if (null != list) {
                    int size = list.size();
                    // detailInfoList = new ArrayList<SimDetailInfo>();
                    for (int i = 0; i < size; i++) {
                        SIMInfo info = list.get(i);
                        SimDetailInfo deailInfo = new SimDetailInfo();
						if ( null!=info ) {
							deailInfo.setColor(info.mColor);
							deailInfo.setDisplayName(info.mDisplayName);
							deailInfo.setNumber(info.mNumber);
							deailInfo.setSimId((int)info.mSimId);
							deailInfo.setICCId(info.mICCId);
							deailInfo.setSlotId(info.mSlot);
							detailInfoList.add(deailInfo);
						}
					}
				}
				return detailInfoList;
			} catch (Exception e) {
				Debugger.logW("Exception when getSIMInfoById ");
			}
			
		} else {
			Debugger.logW("mContext is null");
		}
		
		return null;
	}
	// ==============================================================
	// Setters
	// ==============================================================

	// ==============================================================
	// Methods
	// ==============================================================

	// ==============================================================
	// Inner & Nested classes
	// ==============================================================
}
