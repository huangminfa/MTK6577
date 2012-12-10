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

package com.mediatek.apst.target.data.proxy.app;

import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.target.data.proxy.ContextBasedProxy;
import com.mediatek.apst.target.data.proxy.IRawBlockConsumer;
import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.target.util.GraphicUtils;
import com.mediatek.apst.util.entity.RawTransUtil;
import com.mediatek.apst.util.entity.app.ApplicationInfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;

/**
 * Class Name: ApplicationProxy
 * <p>Package: com.mediatek.apst.target.data.proxy.app
 * <p>Created on: 2010-8-12
 * <p>
 * <p>Description: 
 * <p>Facade of the sub system of applications related operations.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ApplicationProxy extends ContextBasedProxy {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /** Singleton instance. */
    private static ApplicationProxy mInstance = null;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    private ApplicationProxy(Context context){
        super(context);
        setProxyName("ApplicationProxy");
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
    public synchronized static ApplicationProxy getInstance(Context context){
        if (null == mInstance){
            mInstance = new ApplicationProxy(context);
        } else {
            mInstance.setContext(context);
        }
        return mInstance;
    }
    
    private PackageManager getPackageManager(){
        return getContext().getPackageManager();
    }
    
    private ApplicationInfo getEntity(PackageInfo pkg, int destIconW, 
            int destIconH){
        if (null == pkg){
            return null;
        }
        
        android.content.pm.ApplicationInfo app = pkg.applicationInfo;
        if (null == app){
            // Non-application, ignore
            return null;
        }
        ApplicationInfo entity = new ApplicationInfo();
        entity.setPackageName(app.packageName);
        entity.setSdkVersion(app.targetSdkVersion);
        entity.setUid(app.uid);
        if ((app.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) 
                != 0){
            entity.setType(ApplicationInfo.TYPE_SYSTEM);
        } else {
            entity.setType(ApplicationInfo.TYPE_DOWNLOADED);
        }
        entity.setVersionName(pkg.versionName);
        entity.setRequestedPermissions(pkg.requestedPermissions);
        entity.setDataDirectory(app.dataDir);
        entity.setSourceDirectory(app.sourceDir);
        // Get APK size
        long apkSize = 0L;
        if (app.sourceDir != null){
            File apkFile = new File(app.sourceDir);
            if (apkFile.exists()){
                apkSize = apkFile.length();
            }
        }
        entity.setApkSize(apkSize);
        // Get label and description
        CharSequence cs = null;
        cs = app.loadLabel(getPackageManager());
        if (cs != null){
            entity.setLabel(cs.toString());
        } else {
            entity.setLabel(null);
        }
        cs = app.loadDescription(getPackageManager());
        if (cs != null){
            entity.setDescription(cs.toString());
        } else {
            entity.setDescription(null);
        }
        // Get icon
        Drawable drbIcon = app.loadIcon(getPackageManager());
        app = null;
        System.gc();
        if (destIconW < 0){
            destIconW = drbIcon.
            getIntrinsicWidth();
        }
        if (destIconH < 0){
            destIconH = drbIcon.getIntrinsicHeight();
        }
        entity.setIconBytes(GraphicUtils.drawable2Bytes(drbIcon, destIconW, 
                destIconH, CompressFormat.PNG, 50));
        
        return entity;
    }
    
    public void fastGetAllApplications(IRawBlockConsumer consumer, 
            ByteBuffer buffer, int destIconW, int destIconH){
        if (null == consumer){
            Debugger.logE(new Object[]{consumer, buffer, destIconW, destIconH}, 
                    "Consumer is null.");
            return;
        }
        if (null == buffer){
            Debugger.logW(new Object[]{consumer, buffer, destIconW, destIconH}, 
                    "Byte buffer should not be null. Auto allocate " + 
                    RawTransUtil.DEFAULT_BUFFER_SIZE + " bytes now");
            buffer = ByteBuffer.allocate(RawTransUtil.DEFAULT_BUFFER_SIZE);
        }
        
        buffer.clear();
        // Reserve the first 4 bytes for storing items count
        buffer.putInt(0);
        int itemCount = 0;
        int blockNo = 0;
        int totalNo = getApplicationsCount();
        List<PackageInfo> pkgs = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkg : pkgs){
            ApplicationInfo entity = getEntity(pkg, destIconW, destIconH);
            if (null == entity){
                continue;
            }
            ++itemCount;
            // Write bytes into buffer
            buffer.mark();
            try {
                entity.writeRaw(buffer);
            } catch (BufferOverflowException e){
                buffer.reset();
                buffer.putInt(0, itemCount);
                buffer.flip();
                byte[] raw = new byte[buffer.limit()];
                buffer.get(raw);
                blockNo += itemCount;
                consumer.consume(raw, blockNo, totalNo);
                // Reset for new block
                buffer.clear();
                // Reserve the first 4 bytes for storing items count
                buffer.putInt(0);
                itemCount = 0;
            }
        }
        // The last block
        buffer.putInt(0, itemCount);
        buffer.flip();
        byte[] raw = new byte[buffer.limit()];
        buffer.get(raw);
        blockNo += itemCount;
        consumer.consume(raw, blockNo, totalNo);
        buffer.clear();
        
    }
    
    public void fastGetAllApps2Backup(IRawBlockConsumer consumer, 
            ByteBuffer buffer, int destIconW, int destIconH){
        if (null == consumer){
            Debugger.logE(new Object[]{consumer, buffer, destIconW, destIconH}, 
                    "Consumer is null.");
            return;
        }
        if (null == buffer){
            Debugger.logW(new Object[]{consumer, buffer, destIconW, destIconH}, 
                    "Byte buffer should not be null. Auto allocate " + 
                    RawTransUtil.DEFAULT_BUFFER_SIZE + " bytes now");
            buffer = ByteBuffer.allocate(RawTransUtil.DEFAULT_BUFFER_SIZE);
        }
        
        buffer.clear();
        // Reserve the first 4 bytes for storing items count
        buffer.putInt(0);
        int itemCount = 0;
        int blockNo = 0;
        int totalNo = 0;
        List<PackageInfo> pkgs = getPackageManager().getInstalledPackages(0);
        for (PackageInfo pkg : pkgs){
            ApplicationInfo entity = getEntity(pkg, destIconW, destIconH);
            if (null == entity){
                continue;
            }
            if (entity.getType() == ApplicationInfo.TYPE_DOWNLOADED) {
            	totalNo++;
            }
        }
        for (PackageInfo pkg : pkgs){
            ApplicationInfo entity = getEntity(pkg, destIconW, destIconH);
            if (null == entity || entity.getType() != ApplicationInfo.TYPE_DOWNLOADED){
                continue;
            }
            ++itemCount;
            // Write bytes into buffer
            buffer.mark();
            try {
                entity.writeRaw(buffer);
            } catch (BufferOverflowException e){
                buffer.reset();
                buffer.putInt(0, itemCount);
                buffer.flip();
                byte[] raw = new byte[buffer.limit()];
                buffer.get(raw);
                blockNo += itemCount;
                consumer.consume(raw, blockNo, totalNo);
                // Reset for new block
                buffer.clear();
                // Reserve the first 4 bytes for storing items count
                buffer.putInt(0);
                itemCount = 0;
            }
        }
        // The last block
        buffer.putInt(0, itemCount);
        buffer.flip();
        byte[] raw = new byte[buffer.limit()];
        buffer.get(raw);
        blockNo += itemCount;
        consumer.consume(raw, blockNo, totalNo);
        buffer.clear();
        
    }
    
    public ArrayList<ApplicationInfo> getApplicationsForUid(int uid){
        ArrayList<ApplicationInfo> entities = new ArrayList<ApplicationInfo>(0);
        String[] pkgNames = getPackageManager().getPackagesForUid(uid);
        for (String pkgName : pkgNames){
            PackageInfo pkg;
            try {
                pkg = getPackageManager().getPackageInfo(pkgName, 0);
                // Set width and height to -1, to get the original size
                ApplicationInfo entity = getEntity(pkg, -1, -1);
                pkg = null;
                System.gc();
                if (null == entity){
                    continue;
                } else {
                    entities.add(entity);
                }
            } catch (NameNotFoundException e) {
                Debugger.logE(new Object[]{uid}, null, e);
                continue;
            }
        }
        
        return entities;
    }
    
    // Statistic ---------------------------------------------------------------
    /**
     * Get count of applications.
     * @return Count of applications in total.
     */
    public int getApplicationsCount(){
        return getPackageManager().getInstalledApplications(0).size();
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
