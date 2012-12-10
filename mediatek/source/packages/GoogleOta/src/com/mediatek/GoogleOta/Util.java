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
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mediatek.featureoption.FeatureOption;


public class Util {
    public static final String TAG = "GoogleOta";
    public static final String ERROR_CODE = "ERRORCODE";

    public static final String COMMAND_FILE = "/cache/recovery/command";
    public static final String COMMAND_PART2 = "COMMANDPART2";
    public static final int FAULT_TOLERANT_BUFFER = 1024;
    public static final int SDCARD_STATE_LOST         = 0;
    public static final int SDCARD_STATE_UNMOUNT      = 1;
    public static final int SDCARD_STATE_INSUFFICIENT = 2;
    public static final int SDCARD_STATE_OK           = 3;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String EXTERNAL_USB_STORAGE = "usbotg";
    private static String mExternalSDCardPath = null;
    private static final String OTA_PKG_FOLDER = "/googleota";
    private static final String PACKAGE_NAME = "/update.zip";
    
    public static final String OTA_PATH_IN_RECOVERY = "/sdcard/googleota/update.zip";
    
    public static void logInfo(String log) {
        Log.i(TAG, log);
    }
    
    public static void logError(String log) {
        Log.e(TAG, log);
    }
    
    public static void logInfo(String title, String log) {
        Log.i(TAG, title + log);
    }

    public static class OTAresult {
    	public static final int ERROR_RUN = -1;
        public static final int CHECK_OK = 0;
        public static final int ERROR_INVALID_ARGS = 1;
        public static final int ERROR_OTA_FILE = 2;
        public static final int ERROR_FILE_OPEN = 3;
        public static final int ERROR_FILE_WRITE = 4;
        public static final int ERROR_OUT_OF_MEMORY = 5;
        public static final int ERROR_PARTITION_SETTING = 6;
        public static final int ERROR_ONLY_FULL_CHANGE_SIZE = 7;
        public static final int ERROR_ACCESS_SD = 8;
        public static final int ERROR_ACCESS_USERDATA = 9;
        public static final int ERROR_SD_FREE_SPACE = 10;
        public static final int ERROR_SD_WRITE_PROTECTED = 11;
        public static final int ERROR_USERDATA_FREE_SPACE = 12;
        public static final int ERROR_MATCH_DEVICE = 13;
        public static final int ERROR_DIFFERENTIAL_VERSION = 14;
        public static final int ERROR_BUILD_PROP = 15;
    }
    
    private static boolean isExSdcardInserted() {
    	IBinder service = ServiceManager.getService("mount");
        logInfo("Util:service is " + service);
        if (service != null) {
        	IMountService mountService = IMountService.Stub.asInterface(service);
            logInfo("Util:mountService is " + mountService);
            if(mountService == null) {
            	return false;
            } 
            try {
                return mountService.isSDExist();
            } catch (RemoteException e) {
                logInfo("Util:RemoteException when isSDExist: " + e);
                return false;
            }
 
        } else {
        	return false;
        }
    }
    
    public static boolean isSdcardAvailable(Context context) {
    	

    	
    	if(FeatureOption.MTK_2SDCARD_SWAP || FeatureOption.MTK_SHARED_SDCARD) {
    		
    		getExternalSDCardPath(context);

    		if(mExternalSDCardPath != null) {
        		StorageManager storManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        		
        		if(storManager == null)
        			return false;

        		return (storManager.getVolumeState(mExternalSDCardPath).equals(Environment.MEDIA_MOUNTED));

    		}else 
    			return false;
    		

    	}
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logInfo("Util:isSdcardAvailable true");
            return true;
        }
        else {
            logError("Util:sSdcardAvailable false");
            return false;
        }
    }
    
    public static String getExternalSDCardPath(Context context) {

    		StorageManager storManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
    		
    		if(storManager == null) 
    			return null;
    		
        	StorageVolume[] volumes = storManager.getVolumeList();
        	
        	if(volumes == null) 
        		return null;
        	
            for(int i = 0; i < volumes.length; i++){
                	if((volumes[i] != null)&&(volumes[i].isRemovable())) {
                		
                		String path = volumes[i].getPath();
                		if((path != null) &&(!path.contains(EXTERNAL_USB_STORAGE))) {
                			mExternalSDCardPath = path;
                	        logInfo("mExternalSDCardPath = "+mExternalSDCardPath);

                			return path;
                		}

                	}
    	}
    	
    	return null;
    }
    
    /**
     * @param miniSize the minimized size needed for checking, unit is byte
     * @return true if the sdcard left size is greater than miniSize 
     * */
    public static int checkSdcardIsAvailable(Context context, long miniSize) {
        logInfo("Util:CheckSdcardIsAvailable miniSize = "+miniSize);
        if(!isSdcardAvailable(context)) {
            return SDCARD_STATE_LOST;
        }
        
        long insufficientSpace = checkSdcardSpaceNeeded(context, miniSize);
        
        if(insufficientSpace == -1) {
        	logError("Util:checkSdcardIsAvailable false, card mount error");
            return SDCARD_STATE_UNMOUNT;
        } else if(insufficientSpace > 0) {
            return SDCARD_STATE_INSUFFICIENT;
        }

        return SDCARD_STATE_OK;
    }
    
    /**
     * @param miniSize the minimized size needed for checking, unit is byte
     * @return true if the sdcard left size is greater than miniSize 
     * */
    public static long checkSdcardSpaceNeeded(Context context, long miniSize) {
        logInfo("Util:CheckSdcardIsAvailable miniSize = "+miniSize);

        try {
        	String externalPath;
        	
        	if(FeatureOption.MTK_2SDCARD_SWAP || FeatureOption.MTK_SHARED_SDCARD) {
        		
        		if(mExternalSDCardPath == null){
        			getExternalSDCardPath(context);
        			
        		}
        		externalPath = mExternalSDCardPath;
        	} else {
    	        File sdcardSystem = Environment.getExternalStorageDirectory();
    	        externalPath = sdcardSystem.getPath();
        	}

	        StatFs statfs = new StatFs(externalPath);
	        long blockSize = statfs.getBlockSize();
	        long blockCount = statfs.getAvailableBlocks();
	        long totalSize = (long)blockSize * blockCount;

	        if (totalSize < miniSize) {
	            logError("Util:checkSdcardIsAvailable false, totalSize = "+totalSize);
	            return miniSize - totalSize;
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	logError("Util:checkSdcardIsAvailable false, card mount error");
            return -1;
        }
        return 0;
    }
    
    public static long getFileSize(String path) {
        logInfo("Util:getFileSize enter");
        File file = new File(path);
        if (!file.isFile()) {
        	logInfo("Util:getFileSize it is not a file");
			return -1;
		}
		if(!file.exists()){
			logInfo("Util:getFileSize file is not exists");
			return -1;
		}
		return file.length();
	}
    
    public static void deleteFile(String path) {
        logInfo("Util:deleteFile:"+ path);
        File file = new File(path);

		if(file.isFile()) {
	        
			if(!file.exists()){
				logInfo("Util:deleteFile file is not exists");
				return;
			}
			
			boolean result = file.delete();
	        logInfo("Util:deleteFile result is:"+ result);
			return;
			
		}
		if(file.isDirectory()) {
	        logInfo("Util:deleteFile:"+ "Is directory");
			String[] strFileList = file.list();
			
			if(strFileList == null) {
				return;
			}
			
			for(String strName:strFileList) {
				
				if(path.endsWith(File.separator)) {
					deleteFile(path+strName);
				} else {
					deleteFile(path+File.separator+strName);
				}

			}
		}

	}

    public static boolean isNetWorkAvailable(Context context) {
        logInfo("Util:isNetWorkAvailable enter");
        boolean ret = false;
        if(context == null) {
        	logError("Util:isNetWorkAvailable context = null");
            return ret;
        }
        try {
            ConnectivityManager connetManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connetManager == null) {
                logError("Util:isNetWorkAvailable connetManager = null");
                return ret;
            }
            NetworkInfo[] infos=connetManager.getAllNetworkInfo();
            if (infos == null) {
                return ret;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if(infos[i].isConnected() && infos[i].isAvailable()) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e) {
        	logError("Util:isNetWorkAvailable Exception");
        	e.printStackTrace();
        }
        logInfo("Util:isNetWorkAvailable network is ok: "+ret);
        return ret;
    }
    
    public static boolean isSpecifiedNetWorkAvailable(Context context, String typeName) {
    	logInfo("Util:isSpecifiedNetWorkAvailable enter");
        boolean ret = false;
        if(context == null) {
        	logError("Util:isSpecifiedNetWorkAvailable context = null");
            return ret;
        }
        
        if (typeName == null || typeName.length() <= 0) {
        	logError("Util:isSpecifiedNetWorkAvailable typeName = "+typeName+", a wrong network");
            return ret;
        }
        try {
            ConnectivityManager connetManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connetManager == null) {
                logError("Util:isSpecifiedNetWorkAvailable connetManager = null");
                return ret;
            }
            NetworkInfo[] infos=connetManager.getAllNetworkInfo();
            if (infos == null) {
                return ret;
            }
        	for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if(infos[i].getTypeName().endsWith(typeName) && infos[i].isConnected() && infos[i].isAvailable()) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e) {
        	logError("Util:isSpecifiedNetWorkAvailable Exception");
        	e.printStackTrace();
        }
        logInfo("Util:isSpecifiedNetWorkAvailable networw is well");
        return ret;
    }
    
    public static class DeviceInfo {
        String imei;
        String sim;
        String sn;
        String operator;
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        logInfo("Util:getDeviceInfo enter");
        if(context == null) {
            logError("Util:getDeviceInfo context is null");
            return null;
        }
        TelephonyManager teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.imei = teleMgr.getDeviceId();
        deviceInfo.sim = teleMgr.getLine1Number();
        deviceInfo.sn = teleMgr.getSimSerialNumber();
        deviceInfo.operator = teleMgr.getNetworkOperator();

        return deviceInfo;
    }

     public static String getDeviceVersionInfo() {
        logInfo("Util:getDeviceVersionInfo enter");
        String versionInfo = "";
        String oem = SystemProperties.get("ro.product.manufacturer");
        String product = SystemProperties.get("ro.product.device");
        String lang = SystemProperties.get("ro.product.locale.language");
        String buildnumber = SystemProperties.get("ro.build.display.id");
        String oper = SystemProperties.get("ro.operator.optr");
        if (oem == null) {
        	oem = "null";
        }
        if (product == null) {
        	product = "null";
        }
        if (lang == null) {
        	lang = "null";
        }
        if (buildnumber == null) {
        	buildnumber = "null";
        }
        if (oper == null) {
        	oper = "null";
        }
        oem = oem.replaceAll("_","\\$");
        product = product.replaceAll("_","\\$");
        lang = lang.replaceAll("_","\\$");
        buildnumber = buildnumber.replaceAll("_","\\$");
        oper = oper.replaceAll("_","\\$");
        if (oper.equalsIgnoreCase("OP01")) {
            oper = "CMCC";
        } else if (oper.equalsIgnoreCase("OP02")) {
            oper = "CU";
        } else if (oper.equalsIgnoreCase("OP03")) {
            oper = "Orange";
        } else {
            oper = "null";
        }
        versionInfo = oem + "_" + product + "_" + lang + "_" + buildnumber + "_" + oper;
        logInfo("Util:getDeviceVersionInfo, versionInfo = "+versionInfo);
        return versionInfo;
    }
    
    public static class DownloadDescriptor {
        public long size;
        public int deltaId;
        public String version;
        public String newNote;
    }

	public static void serAlarm(Context context, int alarmType, long time, String action){
		logInfo("Util:serAlarm enter, time = "+time + ", current time = "+Calendar.getInstance().getTimeInMillis());
		Intent it = new Intent();
		it.setAction(action);
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.cancel(operation);
		alarmMgr.set(alarmType, time, operation);
	}
	
	public static void clearNotification(Context context, int notifyId){
		NotifyManager mNotification = new NotifyManager(context);
		mNotification.clearNotification(notifyId);
	}
	
    static String getPackagePathName(Context context) {
    	
    	if(FeatureOption.MTK_2SDCARD_SWAP || FeatureOption.MTK_SHARED_SDCARD) {
    		if(mExternalSDCardPath == null){
    			getExternalSDCardPath(context);
    			
    		}
    	   	return mExternalSDCardPath + OTA_PKG_FOLDER;

    	}else {
    		 File sdcardSystem = Environment.getExternalStorageDirectory();
    		 
    		 if(sdcardSystem != null) {
    			 return sdcardSystem.getPath() + OTA_PKG_FOLDER;
    		 }else {
    			 return null;
    		 }

    	}
     }
    
    static String getPackageFileName(Context context) {
    	String path = getPackagePathName(context);
    	Util.logInfo(TAG, "getPackageFileName, filename="+path+Util.PACKAGE_NAME);
    	return path+Util.PACKAGE_NAME;
    }

}
