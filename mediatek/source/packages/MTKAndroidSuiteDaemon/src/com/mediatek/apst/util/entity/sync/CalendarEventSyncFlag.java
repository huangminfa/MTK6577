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

package com.mediatek.apst.util.entity.sync;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: CalendarSyncFlag
 * <p>
 * Package: com.mediatek.apst.util.entity.sync
 * <p>
 * Created on: 2011-05-25
 * <p>
 * <p>
 * Description:
 * <p>
 * A brief flag which contains necessary information to tell whether and how to
 * sync the calendar for outlook calendar sync module.
 * <p>
 *
 * @author mtk81022 Shaoying Han
 * @version V1.0
 */
public class CalendarEventSyncFlag extends DatabaseRecordEntity {
	// ==============================================================
    // Constants
    // ==============================================================

    private static final long serialVersionUID = 2L;
    /**
     * The modify time of the event.
     */
    private long modifyTime;
    
    /**
     * The modify time of the event.
     */
    private long calendarId;
    
    private String title;
    
    private String timeZone;
    
    private long timeFrom;

	public long getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(long timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(long calendarId) {
		this.calendarId = calendarId;
	}

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferUnderflowException {
		// TODO Auto-generated method stub
		super.readRawWithVersion(buffer, versionCode);
		// modifyTime
		this.modifyTime = buffer.getLong();
		// calendarId
		this.calendarId = buffer.getLong();
		// title
		this.title = RawTransUtil.getString(buffer);
		// timeZone
		this.timeZone = RawTransUtil.getString(buffer);
		// timeFrom
		this.timeFrom = buffer.getLong();
		
	}

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferOverflowException {
		// TODO Auto-generated method stub
		super.writeRawWithVersion(buffer, versionCode);
		// modifyTime
		buffer.putLong(this.modifyTime);
		// calendarId
		buffer.putLong(this.calendarId);
		// title
		RawTransUtil.putString(buffer, this.title);
		// timeZone
		RawTransUtil.putString(buffer, this.timeZone);
		// timeFrom
		buffer.putLong(this.timeFrom);
	}

}
