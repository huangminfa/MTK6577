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
 * Class Name: Events
 * <p>
 * Package: com.mediatek.apst.util.entity.calendar
 * <p>
 * Created on: 2011-05-09
 * <p>
 * <p>
 * Description:
 * <p>
 * Events entity
 * 
 * @author mtk81022 Shaoying.Han
 * @version V1.0
 */

public class CalendarEvent extends DatabaseRecordEntity implements Cloneable {

	// ==============================================================
	// Constants
	// ==============================================================
	private static final long serialVersionUID = 2L;
	public static final String GMT_BEIJING_8 = "Asia/Shanghai";

	// ==============================================================
	// Fields
	// ==============================================================
	
	// calendar id
	private long calendarId;
	// 1.what
	private String title;
	// 2.time from
	private long timeFrom;
	// 3.time to
	private long timeTo;
	// 4.isAllDay
	private boolean isAllDay;
	// 5.where
	private String eventLocation;
	// 6.description
	private String description;
	// 7.calendarOwner
	private String  calendarOwner;
	// 8.guests
	private List<Attendee> attendees;
	// 9.repetition
	private String  repetition;
	// 10.reminds List
	private List<Reminder> reminders;
	// 11.lastModify
	private long modifyTime;
	// 12.createTime
	private long createTime;
	// 13.duration
	private String duration;
	// 14.timeZone
	private String timeZone;
	

	// 15.Transparency   Show me as
	private int transparency;
	// 16.Visibility  Privacy
	private int privacy;
	
	//for ics
	// availability
	private int availability;
	// accessLevel
	private int accessLevel;
	

	

	// ==============================================================
	// Constructors
	// ==============================================================
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The database id of the CalendarEvent.
	 * 
	 */
	public CalendarEvent(long id) {
		super(id);
		this.attendees = new ArrayList<Attendee>();
		this.reminders = new ArrayList<Reminder>();
		this.timeZone = GMT_BEIJING_8;
		this.transparency = 0;
		this.privacy = 0;
	}

	public CalendarEvent() {
		this(ID_NULL);
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}

	public long getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(long timeTo) {
		this.timeTo = timeTo;
	}

	public boolean isAllDay() {
		return isAllDay;
	}

	public void setAllDay(boolean isAllDay) {
		this.isAllDay = isAllDay;
	}

	public long getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(long timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	public String getRepetition() {
		return repetition;
	}

	public void setRepetition(String repetition) {
		this.repetition = repetition;
	}

	public List<Reminder> getReminders() {
		return reminders;
	}

	public void setReminders(List<Reminder> reminders) {
		this.reminders = reminders;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(long calendarId) {
		this.calendarId = calendarId;
	}
	
	public String getCalendarOwner() {
		return calendarOwner;
	}

	public void setCalendarOwner(String calendarOwner) {
		this.calendarOwner = calendarOwner;
	}
	
	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public int getTransparency() {
		return transparency;
	}

	public void setTransparency(int transparency) {
		this.transparency = transparency;
	}

	public int getPrivacy() {
		return privacy;
	}

	public void setPrivacy(int privacy) {
		this.privacy = privacy;
    }
	
    // added by Yu 2011-12-8
    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
	    
	// ==============================================================
	// Methods
	// ==============================================================

	public void addAttendee(Attendee attendee) {
		if (null == attendee) {
			return;
		}
		if (null != this.attendees) {
			this.attendees.add(attendee);
		}
	}
	
	public void addReminder(Reminder reminder) {
		if (null == reminder) {
			return;
		}
		if (null != this.reminders) {
			this.reminders.add(reminder);
		}
	}
	
	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
		// TODO Auto-generated method stub
		super.readRawWithVersion(buffer, versionCode);
		
		//calendar id
		this.calendarId = buffer.getLong();
		// title
		this.title = RawTransUtil.getString(buffer);
		// timeFrom
		this.timeFrom = buffer.getLong();
		// timeTo
		this.timeTo = buffer.getLong();
		// isAllDay
		this.isAllDay = RawTransUtil.getBoolean(buffer);
		// eventLocation
		this.eventLocation = RawTransUtil.getString(buffer);
		// description
		this.description = RawTransUtil.getString(buffer);
		// calendar owner
		this.calendarOwner = RawTransUtil.getString(buffer);
		// attendees
		int size = buffer.getInt();
		if (size > 0) {
			this.attendees = new ArrayList<Attendee>(size);
			for (int i = 0; i < size; i++) {
				Attendee attendee = new Attendee();
				attendee.readRawWithVersion(buffer, versionCode);
				this.attendees.add(attendee);
			}
		}
		// repetition
		this.repetition = RawTransUtil.getString(buffer);
		// reminders
		size = buffer.getInt();
		if (size > 0) {
			this.reminders = new ArrayList<Reminder>(size);
			for (int i = 0; i < size; i++) {
				Reminder reminder = new Reminder();
				reminder.readRawWithVersion(buffer, versionCode);
				this.reminders.add(reminder);
			}
		}
		// modifyTime
		this.modifyTime = buffer.getLong();
		// createTime
		this.createTime = buffer.getLong();
		// duration
		this.duration = RawTransUtil.getString(buffer);
        // timeZone
        this.timeZone = RawTransUtil.getString(buffer);

        if (versionCode < 1150) {
            // transparency
            this.transparency = buffer.getInt();
            // privacy
            this.privacy = buffer.getInt();
        } else {
            // availability
            this.availability = buffer.getInt();
            // accessLevel
            this.accessLevel = buffer.getInt();          
        }  
	}

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
		// TODO Auto-generated method stub
		super.writeRawWithVersion(buffer, versionCode);
		
		// calendar id
		buffer.putLong(this.calendarId);
		// title
		RawTransUtil.putString(buffer, this.title);
		// timeFrom
		buffer.putLong(this.timeFrom);
		// timeTo
		buffer.putLong(this.timeTo);
		// isAllDay
		RawTransUtil.putBoolean(buffer, this.isAllDay);
		// eventLocation
		RawTransUtil.putString(buffer, this.eventLocation);
		// description
		RawTransUtil.putString(buffer, this.description);
		// calendar owner
		RawTransUtil.putString(buffer, this.calendarOwner);
		// attendees
		if (null != this.attendees) {
			buffer.putInt(this.attendees.size());
			for (Attendee attendee : attendees) {
				attendee.writeRawWithVersion(buffer, versionCode);
			}
		} else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
		// repetition
		RawTransUtil.putString(buffer, this.repetition);
		// reminders
		if (null != this.reminders) {
			buffer.putInt(this.reminders.size());
			for (Reminder reminder : reminders) {
				reminder.writeRawWithVersion(buffer, versionCode);
			}
		} else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
		// modifyTime
		buffer.putLong(this.modifyTime);
        // createTime
        buffer.putLong(this.createTime);
        // duration
        RawTransUtil.putString(buffer, this.duration);
        // timeZone
        RawTransUtil.putString(buffer, this.timeZone);

        if (versionCode < 1150) {
            // transparency
            buffer.putInt(this.transparency);
            // privacy
            buffer.putInt(this.privacy);
        } else {
            // availability
            buffer.putInt(this.availability);
            // accessLevel
            buffer.putInt(this.accessLevel);
        }
    }
}
