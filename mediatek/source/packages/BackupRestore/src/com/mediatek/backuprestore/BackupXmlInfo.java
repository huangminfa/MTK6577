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

package com.mediatek.backuprestore;

import java.util.Date;

public class BackupXmlInfo {
//	private Date backupdate = null;
	private String backupdate_string = null;
	private String devicetype = null;
	private String system = null;
	private int contact_num = 0;
	private int sms_num = 0;
	private int mms_num = 0;
	private int calendar_num = 0;
	private int app_num = 0;
	private int picture_num = 0;
    private int music_num = 0;
    private int notebook_num = 0;

	
	public void setBackupDate(Date date) {
		
	}
	public void setBackupDate(String dateString) {
		backupdate_string = dateString;
	}
	public String getBackupDateString() {
		return backupdate_string;
	}
	
	public void setDevicetype(String type) {
		devicetype = type;
	}
	public String getDevicetype() {
		return devicetype;
	}
	
	public void setSystem(String sys) {
		system = sys;
	}
	public String getSystem() {
		return system;
	}
	
	public void setContactNum(int num) {
		contact_num = num;
	}
	public int getContactNum() {
		return contact_num;
	}
	
	public void setSmsNum(int num) {
		sms_num = num;
	}
	public int getSmsNum() {
		return sms_num;
	}
	
	public void setMmsNum(int num) {
		mms_num = num;
	}
	public int getMmsNum() {
		return mms_num;
	}

	public void setCalendarNum(int num) {
		calendar_num = num;
	}
	public int getCalendarNum() {
		return calendar_num;
	}
	
	public void setAppNum(int num) {
		app_num = num;
	}

	public int getAppNum() {
		return app_num;
	}
	
	public void setPictureNum(int num) {
		picture_num = num;
	}

	public int getPictureNum() {
		return picture_num;
	}

    public void setMusicNum(int num) {
        music_num = num;
    }

    public int getMusicNum() {
        return music_num;
    }

    public void setNoteBookNum(int num) {
        notebook_num = num;
    }

    public int getNoteBookNum() {
        return notebook_num;
    }

    public int getTotalNum() {
        return contact_num + sms_num + mms_num + calendar_num + app_num + picture_num + music_num + notebook_num;
    }
}
