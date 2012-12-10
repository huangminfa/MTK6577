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

package com.mediatek.apst.util.command.sysinfo;

import java.io.Serializable;

/**
 * Class Name: SimDetailInfo
 * <p>
 * com.mediatek.apst.util.command.sysinfo
 * <p>
 * Created on: 2011-03-28
 * <p>
 * <p>
 * Description:
 * <p>
 * SIM information.
 * <p>
 * 
 * @author mtk81022 Shaoying.Han
 * @version V1.0
 */
public class SimDetailInfo implements Serializable {
	
	private static final long serialVersionUID = 2L;
	
	public static final int versionCode = 1;
	public static final int SLOT_ID_SINGLE = 0;
	public static final int SLOT_ID_ONE = 0;
	public static final int SLOT_ID_TWO = 1;
	
	
	private String mDisplayName = "";
	private String mNumber = "";
	private String mICCId = "";
	private int mSlotId = -255;
	
	private int mSIMType;
	public static final int SIM_TYPE_SIM = 2;
	public static final int SIM_TYPE_USIM = 3;
	
	private int mColor;
	
	private int mSimId;

	public String getDisplayName() {
		return mDisplayName;
	}
	public void setDisplayName(String mDisplayName) {
		this.mDisplayName = mDisplayName;
	}
	
	public String getNumber() {
		return mNumber;
	}
	public void setNumber(String mNumber) {
		this.mNumber = mNumber;
	}
	
	public int getColor() {
		return mColor;
	}
	public void setColor(int mColor) {
		this.mColor = mColor;
	}
	
	public int getSimId() {
		return mSimId;
	}
	public void setSimId(int mSimId) {
		this.mSimId = mSimId;
	}
	public String getICCId() {
		return mICCId;
	}
	public void setICCId(String mICCId) {
		this.mICCId = mICCId;
	}
	public int getSlotId() {
		return mSlotId;
	}
	public void setSlotId(int mSlotId) {
		this.mSlotId = mSlotId;
	}
    public int getSIMType() {
        return mSIMType;
    }

    public void setSIMType(int mSIMType) {
        this.mSIMType = mSIMType;
    }
}
