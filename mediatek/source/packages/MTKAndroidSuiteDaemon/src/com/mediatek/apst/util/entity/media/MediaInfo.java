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

package com.mediatek.apst.util.entity.media;

import java.io.Serializable;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.IRawTransferable;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: MediaInfo
 * <p>Package: com.mediatek.apst.util.entity.media
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Entity contains media file information.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class MediaInfo implements Serializable, IRawTransferable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    /** All types. */
    public static final int ALL = 511;
    public static final int ALARMS = 1;
    public static final int DCIM = 2;
    public static final int DOWNLOADS = 4;
    public static final int MOVIES = 8;
    public static final int MUSIC = 16;
    public static final int NOTIFICATIONS = 32;
    public static final int PICTURES = 64;
    public static final int PODCASTS = 128;
    public static final int RINGTONES = 256;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private int contentType;
    
    private String path;
    
    private long fileLength;
    
    private long lastModified;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public MediaInfo(){}
    
    public MediaInfo(int contentType, String path){
        this.contentType = contentType;
        this.path = path;
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public int getContentType() {
        return contentType;
    }
    
    public String getPath() {
        return path;
    }
    
    public long getFileLenght() {
        return fileLength;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
    
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public static int allTypes(){
        return ALL;
    }
    
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        buffer.putInt(this.contentType);
        RawTransUtil.putString(buffer, this.path);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        this.contentType = buffer.getInt();
        this.path = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
