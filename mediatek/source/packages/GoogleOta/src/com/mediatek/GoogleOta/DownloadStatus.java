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

package com.mediatek.GoogleOta;

import android.content.Context;
import android.content.SharedPreferences;

import com.mediatek.GoogleOta.Util.DownloadDescriptor;

public class DownloadStatus implements IDownloadStatus {
    private static final String OTA_PREFERENCE = "googleota";
    private static final String OTA_PRE_STATUS = "downlaodStatus";
    private static final String OTA_PRE_DOWNLOAND_PERCENT = "downloadpercent";
    private static final String OTA_PRE_IMAGE_SIZE = "imageSize";
    private static final String OTA_PRE_VER = "version";
    private static final String OTA_PRE_VER_NOTE = "versionNote";
    private static final String OTA_PRE_DELTA_ID = "versionDeltaId";
    private static final String OTA_UNZ_STATUS = "isunzip";
    private static final String OTA_REN_STATUS = "isrename";
    private static final String OTA_QUERY_DATE = "query_date";
    private static final String OTA_UPGRADE_STATED = "upgrade_stated";
    private SharedPreferences mPreference = null;
    private DownloadDescriptor mVersionInfo = null;
    
    private static DownloadStatus mDownloadStatus = null;
    private static final String TAG = "DownloadStatus:";


    static synchronized DownloadStatus getInstance(Context context) {
    	Util.logInfo(TAG, "getInstance, context = "+context);
        mDownloadStatus = new DownloadStatus(context);
        return mDownloadStatus;
    }
    
    private DownloadStatus(Context context) {
    	Util.logInfo(TAG, "DownLoadStatus, create a download status object.");
        mPreference = context.getSharedPreferences(OTA_PREFERENCE, Context.MODE_WORLD_READABLE);
    }
    
    public long getUpdateImageSize() {		
        return mPreference.getLong(OTA_PRE_IMAGE_SIZE, -1);
    }

    public int getDLSessionDeltaId() {
        return mPreference.getInt(OTA_PRE_DELTA_ID, -1);
    }

    public String getVersion() {	
        return mPreference.getString(OTA_PRE_VER, "");	 		
    }

    public String getVersionNote() {		
        return mPreference.getString(OTA_PRE_VER_NOTE, "");
    }

    public int getDownLoadPercent() {
    	int per = mPreference.getInt(OTA_PRE_DOWNLOAND_PERCENT, -1);
    	Util.logInfo(TAG, "getDLSessionRenameState, get percent = "+per);
        return per;
    }
    
    public void setDownLoadPercent(int percent) {
    	Util.logInfo(TAG, "setDownLoadPercent, percent = "+percent);
        mPreference.edit().putInt(OTA_PRE_DOWNLOAND_PERCENT, percent).commit();
    }
	
    public DownloadDescriptor getDownloadDescriptor() {		
        if (mVersionInfo == null) {
            mVersionInfo = new  DownloadDescriptor();
            mVersionInfo.size = getUpdateImageSize();
            mVersionInfo.version = getVersion();
            mVersionInfo.newNote = getVersionNote();
            mVersionInfo.deltaId = getDLSessionDeltaId();
        }
        return mVersionInfo;		
    }
    
    public void setDownloadDesctiptor(DownloadDescriptor savedDd) {
    	if (savedDd == null) {
    		Util.logError("DownloadStatus:setDownloadDesctiptor, savedDd = null");
    	    return;
    	}
    	Util.logInfo(TAG, "setDownloadDesctiptor, savedDd.version = "+savedDd.version);
    	Util.logInfo(TAG, "setDownloadDesctiptor, savedDd.size = "+savedDd.size);
        mVersionInfo = savedDd;
        mPreference.edit().putString(OTA_PRE_VER_NOTE, savedDd.newNote).commit();
        mPreference.edit().putLong(OTA_PRE_IMAGE_SIZE, savedDd.size).commit();
        if (savedDd.version != null) {
            mPreference.edit().putString(OTA_PRE_VER, savedDd.version).commit();
        }
        mPreference.edit().putInt(OTA_PRE_DELTA_ID, savedDd.deltaId).commit();
    }
	
    public int getDLSessionStatus() {
        int status = mPreference.getInt(OTA_PRE_STATUS, IDownloadStatus.STATE_QUERYNEWVERSION);
        //Util.logInfo(TAG, "getDLSessionStatus, get status = "+status);
        return status;
    }
	
    public void setDLSessionStatus(int status) {
    	Util.logInfo(TAG, "setDLSessionStatus, status = "+status);
        mPreference.edit().putInt(OTA_PRE_STATUS, status).commit();
    }

    public boolean getDLSessionUnzipState() {
        boolean status = mPreference.getBoolean(OTA_UNZ_STATUS, false);
        Util.logInfo(TAG, "getDLSessionUnzipState, get status = "+status);
        return status;
    }
	
    public void setDLSessionUnzipState(boolean status) {
    	Util.logInfo(TAG, "setDLSessionUnzipState, status = "+status);
        mPreference.edit().putBoolean(OTA_UNZ_STATUS, status).commit();
    }
    
    public boolean getDLSessionRenameState() {
        boolean status = mPreference.getBoolean(OTA_REN_STATUS, false);
        Util.logInfo(TAG, "getDLSessionRenameState, get status = "+status);
        return status;
    }
	
    public void setDLSessionRenameState(boolean status) {
    	Util.logInfo(TAG, "setDLSessionRenameState, status = "+status);
        mPreference.edit().putBoolean(OTA_REN_STATUS, status).commit();
    }
    
    public String getQueryDate(){
    	
    	String strTime = mPreference.getString(OTA_QUERY_DATE, null);
    	Util.logInfo(TAG, "getQueryTime, time = "+strTime);
    	return strTime;
    }
    
    public void setQueryDate(String strTime){
    	Util.logInfo(TAG, "setQueryTime, time = "+strTime);
        mPreference.edit().putString(OTA_QUERY_DATE, strTime).commit();
    }
    
    public boolean getUpgradeStartedState(){
        boolean status = mPreference.getBoolean(OTA_UPGRADE_STATED, false);
        Util.logInfo(TAG, "getUpgradeStartedState, get status = "+status);
        return status;
    }
    
    public void setUpgradeStartedState(boolean status) {
    	Util.logInfo(TAG, "setUpgradeStartedState, status = "+status);
        mPreference.edit().putBoolean(OTA_UPGRADE_STATED, status).commit();
    }
}
