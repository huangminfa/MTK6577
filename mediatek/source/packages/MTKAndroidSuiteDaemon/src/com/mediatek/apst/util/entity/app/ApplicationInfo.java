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

package com.mediatek.apst.util.entity.app;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.IRawTransferable;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: ApplicationInfo
 * <p>Package: com.mediatek.apst.util.entity.app
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Entity contains application information.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ApplicationInfo implements Serializable, IRawTransferable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    /**
     * Type representing system application.
     */
    public static final int TYPE_SYSTEM = 1;
    /**
     * Type representing downloaded application.
     */
    public static final int TYPE_DOWNLOADED = 2;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private int type;
    
    private long apkSize;
    
    /*private long dataSize;*/
    
    private int sdkVersion;
    
    private int uid;
    
    private byte[] iconBytes;
    
    private String packageName;
    
    private String label;

    private String description;
    
    private String versionName;

    private String sourceDirectory;
    
    private String dataDirectory;
    
    private String[] requestedPermissions;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public ApplicationInfo(){}
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Get application type.
     * @return Application type. Valid value is <b>TYPE_SYSTEM</b> and 
     * <b>TYPE_DOWNLOADED</b>.
     * @see #TYPE_SYSTEM
     * @see #TYPE_DOWNLOADED
     */
    public int getType() {
        return type;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getVersionName() {
        return versionName;
    }
    /*
    public long getDataSize() {
        return dataSize;
    }
    */
    public long getApkSize() {
        return apkSize;
    }
    
    public String[] getRequestedPermissions() {
        return requestedPermissions;
    }
    
    public int getSdkVersion() {
        return sdkVersion;
    }

    public int getUid() {
        return uid;
    }
    
    public byte[] getIconBytes() {
        return iconBytes;
    }
    
    public String getSourceDirectory() {
        return sourceDirectory;
    }
    
    public String getDataDirectory() {
        return dataDirectory;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * Set the application type.
     * @param type Application type. Valid value is <b>TYPE_SYSTEM</b> and 
     * <b>TYPE_DOWNLOADED</b>.
     * @see #TYPE_SYSTEM
     * @see #TYPE_DOWNLOADED
     */
    public void setType(int type) {
        this.type = type;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    /*
    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }
    */
    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }
    
    public void setRequestedPermissions(String[] requestedPermissions) {
        this.requestedPermissions = requestedPermissions;
    }
    
    public void setSdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public void setIconBytes(byte[] iconBytes) {
        this.iconBytes = iconBytes;
    }
    
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }
    
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        buffer.putInt(this.type);
        buffer.putLong(this.apkSize);
        /*buffer.putLong(this.dataSize);*/
        buffer.putInt(this.sdkVersion);
        buffer.putInt(this.uid);
        RawTransUtil.putBytes(buffer, this.iconBytes);
        RawTransUtil.putString(buffer, this.packageName);
        RawTransUtil.putString(buffer, this.label);
        RawTransUtil.putString(buffer, this.description);
        RawTransUtil.putString(buffer, this.versionName);
        RawTransUtil.putString(buffer, this.sourceDirectory);
        RawTransUtil.putString(buffer, this.dataDirectory);
        RawTransUtil.putStringArray(buffer, this.requestedPermissions);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        this.type = buffer.getInt();
        this.apkSize = buffer.getLong();
        /*this.dataSize = buffer.getLong();*/
        this.sdkVersion = buffer.getInt();
        this.uid = buffer.getInt();
        this.iconBytes = RawTransUtil.getBytes(buffer);
        this.packageName = RawTransUtil.getString(buffer);
        this.label = RawTransUtil.getString(buffer);
        this.description = RawTransUtil.getString(buffer);
        this.versionName = RawTransUtil.getString(buffer);
        this.sourceDirectory = RawTransUtil.getString(buffer);
        this.dataDirectory = RawTransUtil.getString(buffer);
        this.requestedPermissions = RawTransUtil.getStringArray(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
