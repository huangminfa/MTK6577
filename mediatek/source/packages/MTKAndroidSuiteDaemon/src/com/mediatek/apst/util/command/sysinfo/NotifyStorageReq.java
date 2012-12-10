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

package com.mediatek.apst.util.command.sysinfo;

import com.mediatek.apst.util.command.RequestCommand;

/**
 * Class Name: NotifyStorageReq
 * <p>Package: com.mediatek.apst.util.command.sysinfo
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>Notify about storage changing.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class NotifyStorageReq extends RequestCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    /**
     * Indicates internal storage.
     */
    public static final int INTERNAL = 1;
    /**
     * Indicates external storage.
     */
    public static final int EXTERNAL = 2;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private int storageType;
    private long totalSpace;
    private long availableSpace;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public NotifyStorageReq(){
        super(FEATURE_MAIN);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public int getStorageType() {
        return this.storageType;
    }
    
    public long getTotalSpace() {
        return this.totalSpace;
    }

    public long getAvailableSpace() {
        return this.availableSpace;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }
    
    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }
    
    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
