/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.target.data.proxy.sysinfo;

import com.mediatek.apst.target.data.proxy.ContextBasedProxy;
import com.mediatek.apst.target.util.Config;
import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.util.entity.message.Message;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.os.storage.StorageManager;
import com.mediatek.featureoption.FeatureOption;

/**
 * Class Name: SystemInfoProxy
 * <p>Package: com.mediatek.apst.target.proxy.sysinfo
 * <p>Created on: 2010-8-6
 * <p>
 * <p>Description: 
 * <p>Proxy class provides system info related database operations. 
 * <p>Support platform: Android 2.2(Froyo)
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class SystemInfoProxy extends ContextBasedProxy{
    //==============================================================
    // Constants                                                    
    //==============================================================
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /** Singleton instance. */
    private static SystemInfoProxy mInstance = null;
    
    private StorageManager mStorageManager = null;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    private SystemInfoProxy(Context context){
        super(context);
        setProxyName("SystemInfoProxy");
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public synchronized static SystemInfoProxy getInstance(Context context){
        if (null == mInstance){
            mInstance = new SystemInfoProxy(context);
        } else {
            mInstance.setContext(context);
        }
        return mInstance;
    }
    
    public static String getDevice() {
        return Build.DEVICE;
    }
    
    public static String getFirmwareVersion() {
        return Build.VERSION.RELEASE;
    }
    
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }
    
    public static String getModel() {
        return Build.MODEL;
    }
    
    public static String getSdPath(){
        return Environment.getExternalStorageDirectory().getPath();
    }
    
    public static long getSdTotalSpace() {
        if (isSdMounted()){
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            StatFs statFs = new StatFs(sdcard);
            long totalSpace = (long)statFs.getBlockSize() * 
            statFs.getBlockCount();
            
            return totalSpace;
        } else {
            return -1;
        }
    }
    
    public static long getSdAvailableSpace() {
        if (isSdMounted()){
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            StatFs statFs = new StatFs(sdcard);
            long availableSpace = (long)statFs.getBlockSize() * 
            statFs.getAvailableBlocks(); 
            
            return availableSpace;
        } else {
            return -1;
        }
    }
    
    public static String getInternalStoragePath(){
        return Environment.getDataDirectory().getPath();
    }
    
    public static long getInternalTotalSpace() {
        String data = Environment.getDataDirectory().getPath();
        StatFs statFs = new StatFs(data);
        long totalSpace = (long)statFs.getBlockSize() * 
        statFs.getBlockCount(); 
        
        return totalSpace;
    }
    
    public static long getInternalAvailableSpace() {
        String data = Environment.getDataDirectory().getPath();
        StatFs statFs = new StatFs(data);
        long availableSpace = (long)statFs.getBlockSize() * 
        statFs.getAvailableBlocks(); 
        
        return availableSpace;
    }
    
    public static int getSimState(int simId){
        int simState = TelephonyManager.SIM_STATE_ABSENT;
        if(Config.MTK_GEMINI_SUPPORT){
            if (Message.SIM1_ID == simId){
                simState = TelephonyManager.getDefault().getSimStateGemini(
                        com.android.internal.telephony.Phone.GEMINI_SIM_1);
            } else if (Message.SIM2_ID == simId){
                simState = TelephonyManager.getDefault().getSimStateGemini(
                        com.android.internal.telephony.Phone.GEMINI_SIM_2);
            }
        } else {
            if (Message.SIM_ID == simId){
                simState = TelephonyManager.getDefault().getSimState();
            }
        }
        Debugger.logD(new Object[]{simId}, 
                "simId=" + simId + ", simState=" + simState);
        return simState;
    }

//    // get the sim card iccid
//    public static String getIccid(int simId) {
//        String icCid = TelephonyManager.getDefault().getSimSerialNumberGemini(simId);
//        return icCid;
//
//    }

    public static boolean isSimAccessible(int simState){
        boolean b = false;
        
        switch (simState){
        case TelephonyManager.SIM_STATE_READY:
            b = true;
            break;
            
        case TelephonyManager.SIM_STATE_ABSENT:
        case TelephonyManager.SIM_STATE_PIN_REQUIRED:
        case TelephonyManager.SIM_STATE_PUK_REQUIRED:
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            b = false;
            break;
            
        case TelephonyManager.SIM_STATE_UNKNOWN:
            b= false;
            break;
            
        default:
            b = false;
            break;
        }
        
        return b;
    }
    
    /**
     * Get whether SIM card is accessible.
     * @return True if accessible, otherwise false.
     */
    public boolean isSimAccessible(){
        TelephonyManager telMgr = (TelephonyManager)getContext()
            .getSystemService(Context.TELEPHONY_SERVICE);
        
        return isSimAccessible(telMgr.getSimState());
    }
    
    public static boolean isSim1Accessible(){
        boolean b = false;
        if (Config.MTK_GEMINI_SUPPORT){
            int simState = TelephonyManager.getDefault().getSimStateGemini(
                    com.android.internal.telephony.Phone.GEMINI_SIM_1);
            
            b = isSimAccessible(simState);
        }
        return b;
    }
    
    public static boolean isSim2Accessible(){
        boolean b = false;
        if (Config.MTK_GEMINI_SUPPORT){
            int simState = TelephonyManager.getDefault().getSimStateGemini(
                    com.android.internal.telephony.Phone.GEMINI_SIM_2);
            
            b = isSimAccessible(simState);
        }
        return b;
    }
    
    public static boolean isSdPresent(){
        if (Environment.MEDIA_REMOVED.equals(
                Environment.getExternalStorageState()) || 
            Environment.MEDIA_BAD_REMOVAL.equals(
                Environment.getExternalStorageState())){
            return false;
        } else {
            return true;
        }
    }
    
    public static boolean isSdMounted() {
        if (Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState()) || 
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                Environment.getExternalStorageState())){
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isSdReadable(){
        return isSdMounted();
    }
    
    public static boolean isSdWriteable(){
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
    
    
    public boolean[] checkSDCardState() {
        // check media availability
        Debugger.logI("FeatureOption.MTK_EMMC_SUPPORT = " + FeatureOption.MTK_EMMC_SUPPORT);
        boolean[] SDstate = new boolean[2];
        String mSDCardPath = null, mSDCard2Path = null;
        String[] storagePathList = mStorageManager.getVolumePaths();
        if (storagePathList != null) {
            if (storagePathList.length >= 2) {
                Debugger.logI("storagePathList.length >= 2");
                mSDCardPath = storagePathList[0];
                mSDCard2Path = storagePathList[1];
                if (null != mSDCardPath) {
                    String state = null;
                    state = mStorageManager.getVolumeState(mSDCardPath);
                    SDstate[0] = Environment.MEDIA_MOUNTED.equals(state);
                } else {
                    SDstate[0] = false;
                }

                if (null != mSDCard2Path) {
                    String state = null;
                    state = mStorageManager.getVolumeState(mSDCard2Path);
                    SDstate[1] = Environment.MEDIA_MOUNTED.equals(state);
                } else {
                    SDstate[1] = false;
                }
                
                if (!FeatureOption.MTK_EMMC_SUPPORT) {
                    Debugger.logI("FeatureOption.MTK_EMMC_SUPPORT = false");
                    SDstate[1] = SDstate[0];
                    SDstate[0] = false;
                }
            } else if (storagePathList.length == 1) {
                Debugger.logI("storagePathList.length == 1");
                mSDCardPath = storagePathList[0];

                if (null != mSDCardPath) {
//                    if (FeatureOption.MTK_EMMC_SUPPORT) {
                    if (FeatureOption.MTK_EMMC_SUPPORT) {
                        String state = null;
                        state = mStorageManager.getVolumeState(mSDCardPath);
                        SDstate[0] = Environment.MEDIA_MOUNTED.equals(state);
                    } else {
                        String state = null;
                        state = mStorageManager.getVolumeState(mSDCardPath);
                        SDstate[1] = Environment.MEDIA_MOUNTED.equals(state);
                    }
                } else {
                    SDstate[0] = false;
                    SDstate[1] = false;
                }
            }

        }
        return SDstate;
    }

    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
