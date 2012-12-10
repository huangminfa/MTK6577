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

package com.mediatek.apst.util.entity.calendar;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Calendar
 * <p>
 * Package: com.mediatek.apst.util.entity.calendar
 * <p>
 * Created on: 2011-05-09
 * <p>
 * <p>
 * Description:
 * <p>
 * Calendar entity
 * 
 * @author mtk81022 Shaoying.Han
 * @version V1.0
 */
public class Calendar extends DatabaseRecordEntity implements Cloneable {

	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;

	// ==============================================================
	// Fields
	// ==============================================================
	private String name;
	private String displayName;
	private String ownerAccount;

	private List<CalendarEvent> events;

	// ==============================================================
	// Constructors
	// ==============================================================
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The database id of the Calendar.
	 * 
	 */
	public Calendar(long id) {
		super(id);
		this.name = null;
		this.displayName = null;
		this.ownerAccount = null;
		this.events = new ArrayList<CalendarEvent>();
	}

	public Calendar() {
		this(ID_NULL);
	}

	// ==============================================================
	// Getters and Setters
	// ==============================================================
	public List<CalendarEvent> getEvents() {
		return events;
	}

	public void setEvents(List<CalendarEvent> events) {
		this.events = events;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getOwnerAccount() {
		return ownerAccount;
	}

	public void setOwnerAccount(String ownerAccount) {
		this.ownerAccount = ownerAccount;
	}
	// ==============================================================
	// Methods
	// ==============================================================
	
	public void addEvent(CalendarEvent event) {
		if (null == event) {
			return;
		}
		if (null != this.events) {
			this.events.add(event);
		}
		
	}
	
	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferUnderflowException {
		// TODO Auto-generated method stub
		super.readRawWithVersion(buffer, versionCode);

		// name
		this.name = RawTransUtil.getString(buffer);
		// displayName
		this.displayName = RawTransUtil.getString(buffer);
		// ownerAccount
		this.ownerAccount = RawTransUtil.getString(buffer);

		// events
		int size = buffer.getInt();
		if (size > 0) {
			this.events = new ArrayList<CalendarEvent>(size);
			for (int i = 0; i < size; i++) {
				CalendarEvent event = new CalendarEvent();
				event.readRawWithVersion(buffer, versionCode);
				this.events.add(event);
			}
		}
	}

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferOverflowException {
		// TODO Auto-generated method stub
		super.writeRawWithVersion(buffer, versionCode);

		// name
		RawTransUtil.putString(buffer, this.name);

		// displayName
		RawTransUtil.putString(buffer, this.displayName);

		// ownerAccount
		RawTransUtil.putString(buffer, this.ownerAccount);

		// events
		if (null != this.events) {
			buffer.putInt(this.events.size());
			for (CalendarEvent event : events) {
				event.writeRawWithVersion(buffer, versionCode);
			}
		} else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
	}

}
