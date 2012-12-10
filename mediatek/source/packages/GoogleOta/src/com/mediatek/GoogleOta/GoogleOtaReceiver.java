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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import android.os.RemoteException;
import android.os.ServiceManager;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.GoogleOta.Util.DownloadDescriptor;

public class GoogleOtaReceiver extends BroadcastReceiver { 
    private static final int NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1 = Calendar.MONDAY;
    private static final int NEWVERSION_AUTO_QUERY_DAY_OF_WEEK2 = Calendar.THURSDAY;
    private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_AUTO_QUERY_NEWVERSION = "com.mediatek.GoogleOta.AUTO_QUERY_NEWVERSION";
    public static final String ACTION_UPDATE_REMIND = "com.mediatek.GoogleOta.UPDATE_REMIND";
    public static final String ACTION_UPDATE_REPORT = "com.mediatek.GoogleOta.UPDATE_REPORT";
    public static final String ACTION_REPORT_ACTIVITY = "com.mediatek.intent.GoogleOtaReport";

    public static final String TAG = "GoogleOtaReceiver:";

    private Context mContext;

	@Override
    public void onReceive(Context context, Intent intent) {
		mContext = context;
        String action = intent.getAction();
        Util.logInfo(TAG,"onReceive");
        if(action.equalsIgnoreCase(ACTION_CONNECTIVITY_CHANGE)) {
            Util.logInfo(TAG, "ACTION_CONNECTIVITY_CHANGE");
            if(!Util.isNetWorkAvailable(context)) {
                return;
            }
            DownloadStatus downloadStatus = DownloadStatus.getInstance(context);
            
            String strQueryDate = downloadStatus.getQueryDate();
            if((strQueryDate !=null)&&(hasQueryThisDay(strQueryDate)))
            	return;
            
            
            int status = downloadStatus.getDLSessionStatus();
            if (status == IDownloadStatus.STATE_QUERYNEWVERSION && getQueryRequest()) {
            	Util.logInfo("ACTION_CONNECTIVITY_CHANGE: network start to query new version");
                Util.serAlarm(context,AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+30000,ACTION_AUTO_QUERY_NEWVERSION);
            } else {
            	Util.serAlarm(context,AlarmManager.RTC, getNextAlarmTime(),ACTION_AUTO_QUERY_NEWVERSION);
                Util.logInfo(TAG, "ACTION_CONNECTIVITY_CHANGE, status = "+status+", need not query, set next query alarm");
            }
		}
        else if(action.equalsIgnoreCase(ACTION_AUTO_QUERY_NEWVERSION)) {
            Util.logInfo(TAG, "ACTION_AUTO_QUERY_NEWVERSION");
            if(!Util.isNetWorkAvailable(context)) {
            	Util.logInfo(TAG, "ACTION_AUTO_QUERY_NEWVERSION, network is not available");
                return;
            }
            DownloadStatus mDownLoadStatus = DownloadStatus.getInstance(context);
            

            int status = mDownLoadStatus.getDLSessionStatus();
            Util.logInfo(TAG, "ACTION_AUTO_QUERY_NEWVERSION, status = "+status);
            boolean updateFlag = getUpdateStatus();
            if (status == IDownloadStatus.STATE_DLPKGCOMPLETE && updateFlag) {
            	resetDescriptionInfo(mDownLoadStatus);
                status = IDownloadStatus.STATE_QUERYNEWVERSION;
                Util.logInfo(TAG, "ACTION_AUTO_QUERY_NEWVERSION, update complete");
            }
            
            if (status == IDownloadStatus.STATE_QUERYNEWVERSION && getQueryRequest()) {
            	context.startService(new Intent(context, GoogleOtaService.class));
            } else {
            	Util.serAlarm(context,AlarmManager.RTC, getNextAlarmTime(),ACTION_AUTO_QUERY_NEWVERSION);
                Util.logInfo(TAG, "ACTION_AUTO_QUERY_NEWVERSION, status = "+status+", need not query, set next query alarm");
            }
        }
		else if(action.equalsIgnoreCase(ACTION_BOOT_COMPLETED)) {
            Util.logInfo(TAG, "ACTION_BOOT_COMPLETED");
            DownloadStatus mDownLoadStatus = DownloadStatus.getInstance(context);
            

            int status = mDownLoadStatus.getDLSessionStatus();
            Util.logInfo(TAG, "ACTION_BOOT_COMPLETED, status = "+status);
            boolean updateFlag = getUpdateStatus();
            if (status == IDownloadStatus.STATE_DOWNLOADING) {
            	mDownLoadStatus.setDLSessionStatus(IDownloadStatus.STATE_PAUSEDOWNLOAD);
            	Util.logInfo(TAG, "ACTION_BOOT_COMPLETED, abnormal stop during downloading, will be re-query version");
            }
            if (status == IDownloadStatus.STATE_DLPKGCOMPLETE && updateFlag) {
                Util.serAlarm(context,AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+3000,ACTION_UPDATE_REPORT);
            	resetDescriptionInfo(mDownLoadStatus);
                status = IDownloadStatus.STATE_QUERYNEWVERSION;
                Util.logInfo(TAG, "ACTION_BOOT_COMPLETED, update complete");
            }
			
            
            String strQueryDate = mDownLoadStatus.getQueryDate();
            if((strQueryDate !=null)&&(hasQueryThisDay(strQueryDate)))
            	return;
            
            if (status == IDownloadStatus.STATE_QUERYNEWVERSION && getQueryRequest()) {
                Util.logInfo("ACTION_BOOT_COMPLETED: query day to query new version");
                Util.serAlarm(context,AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+30000,ACTION_AUTO_QUERY_NEWVERSION);
            } else {
            	Util.serAlarm(context,AlarmManager.RTC, getNextAlarmTime(),ACTION_AUTO_QUERY_NEWVERSION);
                Util.logInfo(TAG, "ACTION_BOOT_COMPLETED, status = "+status+", need not query, set next query alarm");
            }
        }
        else if(action.equalsIgnoreCase(ACTION_UPDATE_REMIND)) {
            Util.logInfo(TAG, "ACTION_UPDATE_REMIND"); 
            NotifyManager notification = new NotifyManager(context);
            DownloadStatus mDownloadStatus = DownloadStatus.getInstance(context);
            String version = mDownloadStatus.getVersion();
            notification.showDownloadCompletedNotification(version);
        }
        else if(action.equalsIgnoreCase(ACTION_UPDATE_REPORT)) {
            Util.logInfo(TAG, "ACTION_UPDATE_REPORT"); 
            startReportActivity();
        }
    }
	
	private boolean hasQueryThisDay(String queryDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Util.DATE_FORMAT);
        String strDate = dateFormat.format(new Date());
        return strDate.equals(queryDate);
	}
    private long getNextAlarmTime() {
    	
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        
        Util.logInfo(TAG, "current time is "+ format.format(calendar.getTime()));
        
        int currDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int daysToAdd = 0;
        if(currDayOfWeek < NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1) {
        	daysToAdd = NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1 - currDayOfWeek;
        } else if((currDayOfWeek >= NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1)&&(currDayOfWeek < NEWVERSION_AUTO_QUERY_DAY_OF_WEEK2)) {
        	daysToAdd = NEWVERSION_AUTO_QUERY_DAY_OF_WEEK2 - currDayOfWeek;
        } else {
        	daysToAdd = NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1 + Calendar.SATURDAY - currDayOfWeek;
        }
        
        calendar.add(Calendar.DATE, daysToAdd);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Util.logInfo(TAG, "alarm time is "+ format.format(calendar.getTime()));
        return calendar.getTimeInMillis();
    }
    
    boolean getQueryRequest() {
        Calendar calendar = Calendar.getInstance();
        if(NEWVERSION_AUTO_QUERY_DAY_OF_WEEK1 == calendar.get(Calendar.DAY_OF_WEEK) || NEWVERSION_AUTO_QUERY_DAY_OF_WEEK2 == calendar.get(Calendar.DAY_OF_WEEK)) {
        	return true;
        }
        return false;
    }

    private boolean getUpdateStatus() {
    	Util.logInfo(TAG, "getUpdateStatus");
    	boolean upgradeStarted = false;
    	
    	if(FeatureOption.MTK_EMMC_SUPPORT) {
            DownloadStatus downloadStatus = DownloadStatus.getInstance(mContext);
            upgradeStarted = downloadStatus.getUpgradeStartedState();
    	} else {
            File file = new File(getFullPathResultFileName());
            upgradeStarted = file.exists();
    	}


        if (!upgradeStarted) {
            Util.logInfo(TAG, "getUpdateStatus, no update");

        } else {
            Util.logInfo(TAG, "getUpdateStatus, update finished");
            File imgf = new File(Util.getPackageFileName(mContext));
            if (imgf.exists()) {
                Util.deleteFile(Util.getPackageFileName(mContext));
            }
       }
       return upgradeStarted;
    }
    
    private String getResultFileName() {
    	return mContext.getResources().getString(R.string.address_update_result);
    }

    private String getFullPathResultFileName() {
    	File dir = mContext.getFilesDir();
    	String path = dir.getPath();
    	return path+"/"+getResultFileName();
    }
    
    private boolean getUpdateResult() {
    	Util.logInfo(TAG, "getUpdateResult");

    	if(FeatureOption.MTK_EMMC_SUPPORT) {
            try {
                IBinder binder = ServiceManager.getService("GoogleOtaBinder");
                GoogleOtaBinder agent = GoogleOtaBinder.Stub.asInterface (binder); 
                if (agent == null) {
                    Util.logInfo(TAG, "GoogleOtaBinder is null.");
                    return false;
                }
                return agent.readUpgradeResult();

            } catch (Exception e) {
                Util.logInfo(TAG, "Exception accur when set flag to reboot into recovery mode.");
                e.printStackTrace ();
                return false;
            }
    	} else {

        	try {
        		File file = new File(getFullPathResultFileName());
                if (!file.exists()) {
                	Util.logInfo(TAG, "getUpdateResult, report file not exist");
                    return false;
                }
                InputStream resStr = mContext.openFileInput(getResultFileName());
                if (resStr == null) {
                    Util.logInfo(TAG, "getUpdateResult, inputstream error > "+getResultFileName());
                    return false;
                }
                byte [] b = new byte[10];
                int num = resStr.read(b);
                Util.logInfo(TAG, "getUpdateResult, num = "+num);
                if (num < 0) {
                    return false;
                }
                resStr.close();
                String string = new String(b, 0, num);
                int result = Integer.valueOf(string);
                Util.logInfo(TAG, "getUpdateResult, result = "+result);
                if (result == 1) {
             	    return true;
                } else {
                    return false;
                }
        	} catch (Exception e) {
                e.printStackTrace();
                return false;
        	}
        
    	}

    }
    
    private void resetDescriptionInfo(DownloadStatus ds) {
    	Util.logInfo(TAG, "resetDescriptionInfo");
        File imgf = new File(Util.getPackageFileName(mContext));
        if (imgf.exists()) {
            Util.logInfo(TAG, "resetDescriptionInfo, image exist, delete it");
            Util.deleteFile(Util.getPackageFileName(mContext));
        }
        ds.setDownLoadPercent(-1);
        ds.setDLSessionStatus(DownloadStatus.STATE_QUERYNEWVERSION);
        DownloadDescriptor dd = ds.getDownloadDescriptor();
        dd.deltaId = -1;
        dd.size = -1;
        dd.newNote = null;
        dd.version = null;
        ds.setDownloadDesctiptor(dd);
    }

    private void startReportActivity() {
        boolean result = getUpdateResult();
    	Util.logInfo(TAG, "getUpdateResult="+result);
        Intent report = new Intent();
        report.putExtra(Util.ERROR_CODE, result);
        report.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        report.setAction(ACTION_REPORT_ACTIVITY);
        mContext.startActivity(report);
        resetUpdateResult();
    }
    
    private void resetUpdateResult() {
    	Util.logInfo(TAG, "deleteUpdateResult");
        DownloadStatus downloadStatus = DownloadStatus.getInstance(mContext);
        downloadStatus.setUpgradeStartedState(false);

    }
}
