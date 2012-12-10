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

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Attendee
 * <p>
 * Package: com.mediatek.apst.util.entity.calendar
 * <p>
 * Created on: 2011-05-21
 * <p>
 * <p>
 * Description:
 * <p>
 * Attendee entity
 * 
 * @author mtk81022 Shaoying.Han
 * @version V1.0
 */
public class Attendee extends DatabaseRecordEntity implements Cloneable {
	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;
	
	private long eventId;
	private String attendeeName;
	private String attendeeEmail;
	private int attendeeStatus;
	private int attendeeRelationShip;
	private int attendeeType;
	
	// ==============================================================
	// Constructors
	// ==============================================================
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The database id of the Attendee.
	 * 
	 */
	public Attendee(long id) {
		super(id);
		this.eventId = ID_NULL;
		this.attendeeName = null;
		this.attendeeEmail = null;
	}

	public Attendee() {
		this(ID_NULL);
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String getAttendeeName() {
		return attendeeName;
	}

	public void setAttendeeName(String attendeeName) {
		this.attendeeName = attendeeName;
	}

	public String getAttendeeEmail() {
		return attendeeEmail;
	}

	public void setAttendeeEmail(String attendeeEmail) {
		this.attendeeEmail = attendeeEmail;
	}

	public int getAttendeeStatus() {
		return attendeeStatus;
	}

	public void setAttendeeStatus(int attendeeStatus) {
		this.attendeeStatus = attendeeStatus;
	}

	public int getAttendeeRelationShip() {
		return attendeeRelationShip;
	}

	public void setAttendeeRelationShip(int attendeeRelationShip) {
		this.attendeeRelationShip = attendeeRelationShip;
	}

	public int getAttendeeType() {
		return attendeeType;
	}

	public void setAttendeeType(int attendeeType) {
		this.attendeeType = attendeeType;
	}

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferUnderflowException {
		// TODO Auto-generated method stub
		super.readRawWithVersion(buffer, versionCode);
		
		// eventId
		this.eventId = buffer.getLong();
		// attendeeName
		this.attendeeName = RawTransUtil.getString(buffer);
		// attendeeEmail
		this.attendeeEmail = RawTransUtil.getString(buffer);
		// attendeeStatus
		this.attendeeStatus = buffer.getInt();
		// attendeeRelationShip
		this.attendeeRelationShip = buffer.getInt();
		// attendeeType
		this.attendeeType = buffer.getInt();
	}

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode) throws NullPointerException,
			BufferOverflowException {
		// TODO Auto-generated method stub
		super.writeRawWithVersion(buffer, versionCode);
		
		// eventId
		buffer.putLong(this.eventId);
		// attendeeName
		RawTransUtil.putString(buffer, this.attendeeName);
		// attendeeEmail
		RawTransUtil.putString(buffer, this.attendeeEmail);
		// attendeeStatus
		buffer.putInt(this.attendeeStatus);
		// attendeeRelationShip
		buffer.putInt(this.attendeeRelationShip);
		// attendeeType
		buffer.putInt(this.attendeeType);
	}
	
}
