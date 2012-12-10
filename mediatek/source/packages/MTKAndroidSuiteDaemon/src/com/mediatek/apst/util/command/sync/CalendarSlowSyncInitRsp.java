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

package com.mediatek.apst.util.command.sync;

import com.mediatek.apst.util.command.ResponseCommand;

/**
 * Class Name: CalendarSlowSyncInitRsp
 * <p>
 * Package: com.mediatek.apst.util.command.sync
 * <p>
 * Created on: 2011-05-25
 * <p>
 * <p>
 * Description:
 * <p>
 * Response for initializing a calendar slow sync.
 * <p>
 * 
 * @author mtk81022 Shaoying Han
 * @version V1.0
 */
public class CalendarSlowSyncInitRsp extends ResponseCommand {
	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;

	// ==============================================================
	// Fields
	// ==============================================================
	private long currentMaxId;

	// ==============================================================
	// Constructors
	// ==============================================================
	public CalendarSlowSyncInitRsp(int token) {
		super(FEATURE_CALENDAR_SYNC, token);
	}

	// ==============================================================
	// Getters
	// ==============================================================
	/**
	 * Gets the max ID of CalendarEvent currently. CalendarEvent
	 * slow sync firstly adds all outlook CalendarEvent into phone, then adds
	 * all CalendarEvent into outlook. Because outlook CalendarEvent have
	 * been added into phone in the first step, we should only adds
	 * CalendarEvent originally on phone into outlook in the second step. Thus,
	 * we use a max ID of original phone CalendarEvent to indicate which
	 * CalendarEvent to get in the second step(for CalendarEvent are added with
	 * ID increasing).
	 * 
	 * @return The max ID of raw CalendarEvent on phone currently.
	 */
	public long getCurrentMaxId() {
		return currentMaxId;
	}

	// ==============================================================
	// Setters
	// ==============================================================
	/**
	 * Sets the max ID of raw CalendarEvent on phone currently. CalendarEvent
	 * slow sync firstly adds all outlook CalendarEvent into phone, then adds
	 * all phone CalendarEvent into outlook. Because outlook CalendarEvent have
	 * been added into phone in the first step, we should only adds
	 * CalendarEvent originally on phone into outlook in the second step. Thus,
	 * we use a max ID of original phone CalendarEvent to indicate which
	 * CalendarEvent to get in the second step(for CalendarEvent are added with
	 * ID increasing).
	 * 
	 * @param currentMaxId
	 *            The max ID of raw CalendarEvent on phone currently.
	 */
	public void setCurrentMaxId(long currentMaxId) {
		this.currentMaxId = currentMaxId;
	}

	// ==============================================================
	// Methods
	// ==============================================================

	// ==============================================================
	// Inner & Nested classes
	// ==============================================================
}
