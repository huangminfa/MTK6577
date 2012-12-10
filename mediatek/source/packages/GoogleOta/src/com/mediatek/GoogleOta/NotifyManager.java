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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;
import android.content.Context;

public class NotifyManager {
    public static final int NOTIFY_NEW_VERSION = 1;
    public static final int NOTIFY_DOWNLOADING =2;
    public static final int NOTIFY_DL_COMPLETED =3;
    public static final String  OTA_CLIENT_INTENT= "com.mediatek.intent.GoogleOtaClient";
    public static final String TAG = "NotifyManager:";

    private Notification.Builder mNotification;
    private int mNotificationType;
    private Context mNotificationContext;
    private NotificationManager mNotificationManager;


    public NotifyManager(Context context) {
        mNotificationContext = context;
        mNotification = null;
        mNotificationManager = (NotificationManager) mNotificationContext.getSystemService(Context.NOTIFICATION_SERVICE);		
    }
    
    public void showNewVersionNotification(String version) {
        mNotificationType = NOTIFY_NEW_VERSION;
        CharSequence contentTitle = mNotificationContext.getText(R.string.notification_content_title);
        configAndShowNotification(R.drawable.stat_download_detected, contentTitle,version, OTA_CLIENT_INTENT);
    }

    public void showDownloadCompletedNotification(String version) {
        mNotificationType = NOTIFY_DL_COMPLETED;
        CharSequence contentTitle = mNotificationContext.getText(R.string.notification_content_title) + " " + version;
        String contentText = mNotificationContext.getString(R.string.notification_content_text_completed);
        configAndShowNotification(R.drawable.stat_download_completed, contentTitle, contentText,OTA_CLIENT_INTENT);
    }

    public void showDownloadingNotificaton(String version, int currentProgress) {
        mNotificationType = NOTIFY_DOWNLOADING;
        String contentTitle = mNotificationContext.getString(R.string.notification_content_title);// + " "+ version;
        setNotificationProgress(R.drawable.stat_download_downloading, contentTitle, currentProgress);
    }

    private void setNotificationProgress(int iconDrawableId, String contentTitle, int currentProgress) {       
        if(mNotification == null) {
            mNotification = new Notification.Builder(mNotificationContext);
        	if(mNotification == null) {
        	   	Util.logInfo(TAG, "mNotification == null"); 
    		
        		return;
        	}
        	mNotification.setAutoCancel(true)
        	.setOngoing(true)
    		.setContentTitle(contentTitle)
    		.setSmallIcon(iconDrawableId)
    		.setWhen(System.currentTimeMillis())
    		.setContentIntent(getPendingIntenActivity(OTA_CLIENT_INTENT));

        }
        String percent = "" + currentProgress + "%";
        mNotification.setProgress(100, currentProgress, false)
        .setContentInfo(percent);

        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
    }

    private PendingIntent getPendingIntenActivity(String intentFilter) {
    	Util.logInfo(TAG, "getPendingIntenActivity, intentFilter = "+intentFilter); 
        Intent notificationIntent = new Intent(intentFilter); 
        PendingIntent contentIntent = PendingIntent.getActivity(mNotificationContext,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT); 
        return contentIntent;
    }

    private void configAndShowNotification(int iconDrawableId, CharSequence contentTitle , String contentText, String intentFilter) { 
    	mNotification = new Notification.Builder(mNotificationContext);
    	
    	if(mNotification == null) {
    	   	Util.logInfo(TAG, "mNotification == null"); 
		
    		return;
    	}
    	mNotification.setAutoCancel(true)
    		.setContentTitle(contentTitle)
    		.setContentText(contentText)
    		.setSmallIcon(iconDrawableId)
    		.setWhen(System.currentTimeMillis())
    		.setContentIntent(getPendingIntenActivity(intentFilter));

        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
        mNotification = null;
    }

    public void clearNotification(int notificationId) {
        mNotificationManager.cancel(notificationId);
        mNotification = null;
        return;
    }
}
