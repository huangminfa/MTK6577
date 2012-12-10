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

package com.mediatek.apst.util.command.contacts;

import com.mediatek.apst.util.command.RequestCommand;
import com.mediatek.apst.util.entity.contacts.RawContact;

/**
 * Class Name: UpdateDetailedContactReq
 * <p>Package: com.mediatek.apst.util.command.contacts
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>Request for updating a contact(with contact data).
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class UpdateDetailedContactReq extends RequestCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private long updateId;
    
    private RawContact newOne;
    
    // For MTK SIM Contacts feature.
    private int sourceLocation;
    // For MTK SIM Contacts feature.
    private String simName;
    // For MTK SIM Contacts feature.
    private String simNumber;
    // For MTK SIM Contacts feature.
    private String simEmail;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public UpdateDetailedContactReq(){
        super(FEATURE_CONTACTS);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public long getUpdateId(){
        return updateId;
    }
    
    public RawContact getNewOne(){
        return newOne;
    }
    
    // For MTK SIM Contacts feature.
    /**
     * Gets the source location of the contact to update. Valid values can be 
     * SOURCE_PHONE, SOURCE_SIM, SOURCE_SIM1 and SOURCE_SIM2.
     * @return The source location.
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_PHONE
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM1
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM2
     */
    public int getSourceLocation(){
        return sourceLocation;
    }
    // For MTK SIM Contacts feature.
    public String getSimName(){
        return simName;
    }
    // For MTK SIM Contacts feature.
    public String getSimNumber(){
        return simNumber;
    }
    
    // For MTK SIM Contacts feature.
    public String getSimEmail() {
        return simEmail;
    }
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setUpdateId(long updateId){
        this.updateId = updateId;
    }
    
    public void setNewOne(RawContact newOne){
        this.newOne = newOne;
    }
    
    // For MTK SIM Contacts feature.
    /**
     * Sets the source location of the contact to update. Valid values can be 
     * SOURCE_PHONE, SOURCE_SIM, SOURCE_SIM1 and SOURCE_SIM2.
     * @param sourceLocation The source location to set.
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_PHONE
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM1
     * @see com.mediatek.apst.util.entity.contacts.RawContact#SOURCE_SIM2
     */
    public void setSourceLocation(int sourceLocation){
        this.sourceLocation = sourceLocation;
    }
    // For MTK SIM Contacts feature.
    public void setSimName(String simName){
        this.simName = simName;
    }
    // For MTK SIM Contacts feature.
    public void setSimNumber(String simNumber){
        this.simNumber = simNumber;
    }
    
    // For MTK SIM Contacts feature.
    public void setSimEmail(String simEmail) {
        this.simEmail = simEmail;
    }
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
