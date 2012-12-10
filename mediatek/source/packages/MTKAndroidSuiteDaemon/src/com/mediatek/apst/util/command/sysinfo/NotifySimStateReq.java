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

package com.mediatek.apst.util.command.sysinfo;

import com.mediatek.apst.util.command.RequestCommand;

/**
 * Class Name: NotifySimStateReq
 * <p>Package: com.mediatek.apst.util.command.sysinfo
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>Notify about SIM state changing.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class NotifySimStateReq extends RequestCommand {
	// ==============================================================
	// Constants
	// ==============================================================

	private static final long serialVersionUID = 2L;
	// ==============================================================
	// Fields
	// ==============================================================
	private boolean simAccessible;
	private boolean sim1Accessible;
	private boolean sim2Accessible;
	// Added by Shaoying Han 2011-04-08
	private SimDetailInfo simDetailInfo;
	private SimDetailInfo sim1DetailInfo;
	private SimDetailInfo sim2DetailInfo;
	private boolean bInfoChanged;
	private int contactsCount;

	// ==============================================================
	// Constructors
	// ==============================================================
	public NotifySimStateReq() {
		super(FEATURE_MAIN);
	}

	// ==============================================================
	// Getters
	// ==============================================================
	public boolean isSimAccessible() {
		return simAccessible;
	}

	public boolean isSim1Accessible() {
		return sim1Accessible;
	}

	public boolean isSim2Accessible() {
		return sim2Accessible;
	}

	public int getContactsCount() {
		return contactsCount;
	}

	public SimDetailInfo getSimDetailInfo() {
		return simDetailInfo;
	}

	public SimDetailInfo getSim1DetailInfo() {
		return sim1DetailInfo;
	}

	public SimDetailInfo getSim2DetailInfo() {
		return sim2DetailInfo;
	}

	public boolean isInfoChanged() {
		return bInfoChanged;
	}

	// ==============================================================
	// Setters
	// ==============================================================
	public void setSimAccessible(boolean simAccessible) {
		this.simAccessible = simAccessible;
	}

	public void setSim1Accessible(boolean sim1Accessible) {
		this.sim1Accessible = sim1Accessible;
	}

	public void setSim2Accessible(boolean sim2Accessible) {
		this.sim2Accessible = sim2Accessible;
	}

	public void setContactsCount(int contactsCount) {
		this.contactsCount = contactsCount;
	}

	public void setSimDetailInfo(SimDetailInfo simDetailInfo) {
		this.simDetailInfo = simDetailInfo;
	}

	public void setSim1DetailInfo(SimDetailInfo sim1DetailInfo) {
		this.sim1DetailInfo = sim1DetailInfo;
	}

	public void setSim2DetailInfo(SimDetailInfo sim2DetailInfo) {
		this.sim2DetailInfo = sim2DetailInfo;
	}

	public void setInfoChanged(boolean bInfoChanged) {
		this.bInfoChanged = bInfoChanged;
	}
	// ==============================================================
	// Methods
	// ==============================================================
	// ==============================================================
	// Inner & Nested classes
	// ==============================================================
}
