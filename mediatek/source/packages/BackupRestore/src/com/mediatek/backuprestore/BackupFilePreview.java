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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
/**
 * get the bakcup infomations in backup.xml file
 */
public class BackupFilePreview {

	
	
	private File mFile = null;	
	private BackupXmlInfo mXmlInfo = null;
	
	public BackupFilePreview(File file) {
		if (file == null) {
			Log.e(LogTag.RESTORE,  "BackupFilePreview constractor error! file is null");
		}
		mFile = file;		
		extractValues(file);
	}
	


	/**
	 * get values from special file
	 * 
	 * @param context
	 */
	private void extractValues(File file) {
		setFileSize(getSizeFromFile(file));
		getXmlInfo(file);
		if (mXmlInfo != null){
		    Date date = getFileBackupTime(mXmlInfo);
			if (date != null){
				setBackupTime(formatDisplayTime(date));
			}
		}else{
		    Log.e(LogTag.RESTORE, "xml Info is null: file = " + mFile.getAbsolutePath());
		}
	}
	
	private void getXmlInfo(File file){
		String xml = BackupZip.readFile(file.getAbsolutePath(), BackupXml.BACKXMLNAME);
		if (xml != null){
			mXmlInfo = BackupXmlParser.parse(xml);
		}		
	}
	
	public File getFile() {
		return mFile;
	}


	/*
	 * 
	 * @param info
	 * @return;
	 */
	
	private Date getFileBackupTime(BackupXmlInfo info) {
		Date date = null;
		String backupTime = info.getBackupDateString();
		
		if (backupTime != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				date = dateFormat.parse(backupTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}
	
	
	private String formatDisplayTime(Date date) {
		String displayTime = null;
		if (date != null) {
			SimpleDateFormat dateFomart = new SimpleDateFormat(
					"yyyy-MM-dd  HH:mm");
			displayTime = dateFomart.format(date);
		}
		return displayTime;
	}

	/**
	 * edit: the property of function change from private to public by laitt
	 * 
	 * @param file
	 * @param context
	 */
	public long getSizeFromFile(File file) {
		long size = 0;
		if (file != null) {
			size = file.length();
		}
		return size;
	}


	public String backupTime;

	public String getBackupTime() {
		return backupTime;
	}

	public void setBackupTime(String backupTime) {
		this.backupTime = backupTime;
	}

	private long fileSize;

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}


	public int getBackupModules() {
	    return peekBackupModules();
	}


	private int peekBackupModules() {
		int iModulse = 0;
	    if (mXmlInfo != null){
		if (mXmlInfo.getContactNum() > 0){
			iModulse |= ModuleType.TYPE_CONTACT;
		}
		
	        if (mXmlInfo.getSmsNum() > 0 || mXmlInfo.getMmsNum() > 0) {
			iModulse |= ModuleType.TYPE_MESSAGE;
		}
		if (mXmlInfo.getAppNum() > 0) {
			iModulse |= ModuleType.TYPE_APP;
		}
		
		if (mXmlInfo.getCalendarNum() > 0){
			iModulse |= ModuleType.TYPE_CALENDAR;
		}
		
		if (mXmlInfo.getPictureNum() > 0){
			iModulse |= ModuleType.TYPE_PICTURE;
		}
		
		if (mXmlInfo.getMusicNum() > 0){
			iModulse |= ModuleType.TYPE_MUSIC;
		}

		if (mXmlInfo.getNoteBookNum() > 0){
			iModulse |= ModuleType.TYPE_NOTEBOOK;
		}

	    }		
		return iModulse;
	}

	public int getTypeCount(int type){
	    int num = 0;
	    if (mXmlInfo != null){
	    switch (type){
	    case ModuleType.TYPE_CONTACT:
	        num = mXmlInfo.getContactNum();
	        break;
	        
	    case ModuleType.TYPE_MESSAGE:
	            num = mXmlInfo.getSmsNum() + mXmlInfo.getMmsNum();
	        break;
	        
	    case ModuleType.TYPE_CALENDAR:
	        num = mXmlInfo.getCalendarNum();
	        break;
	    
	    case ModuleType.TYPE_PICTURE:
	        num = mXmlInfo.getPictureNum();
            break;
	    
	    case ModuleType.TYPE_APP:
	        num = mXmlInfo.getAppNum();
            break;    

	    case ModuleType.TYPE_MUSIC:
	        num = mXmlInfo.getMusicNum();
            break;    

	    case ModuleType.TYPE_NOTEBOOK:
	        num = mXmlInfo.getNoteBookNum();
            break;    

	    }
	    }
	    return num;
	}
}
