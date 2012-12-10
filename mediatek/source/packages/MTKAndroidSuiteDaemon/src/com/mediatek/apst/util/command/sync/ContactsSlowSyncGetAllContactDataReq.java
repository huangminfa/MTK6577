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

package com.mediatek.apst.util.command.sync;

import com.mediatek.apst.util.command.RequestCommand;

/**
 * Class Name: ContactsSlowSyncGetAllContactDataReq
 * <p>Package: com.mediatek.apst.util.command.sync
 * <p>Created on: 2010-12-20
 * <p>
 * <p>Description: 
 * <p>Request for getting multiple contacts data in slow sync.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ContactsSlowSyncGetAllContactDataReq extends RequestCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private long contactIdLimit;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public ContactsSlowSyncGetAllContactDataReq(){
        super(FEATURE_SYNC);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /**
     * Gets the max ID of contacts to return. Contacts slow sync firstly adds 
     * all outlook contacts into phone, then adds all phone contacts into 
     * outlook. Because outlook contacts have been added into phone in the first 
     * step, we should only adds contacts originally on phone into outlook in 
     * the second step. Thus, we use a max ID of original phone contacts to 
     * indicate which contacts to get in the second step(for contacts are 
     * added with ID increasing). Note that it is the database ID of raw 
     * contacts, not contact data.
     * @return The max ID of contacts to return.
     */
    public long getContactIdLimit(){
        return contactIdLimit;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    /**
     * Sets the max ID of contacts to return. Contacts slow sync firstly adds 
     * all outlook contacts into phone, then adds all phone contacts into 
     * outlook. Because outlook contacts have been added into phone in the first 
     * step, we should only adds contacts originally on phone into outlook in 
     * the second step. Thus, we use a max ID of original phone contacts to 
     * indicate which contacts to get in the second step(for contacts are 
     * added with ID increasing). Note that it is the database ID of raw 
     * contacts, not contact data.
     * @param contactIdLimit The max ID of contacts to return.
     */
    public void setContactIdLimit(long contactIdLimit){
        this.contactIdLimit = contactIdLimit;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
