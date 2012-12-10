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

package com.mediatek.apst.util.command.message;

import com.mediatek.apst.util.command.ResponseCommand;
import com.mediatek.apst.util.entity.message.Message;

/**
 * Class Name: ResendSmsRsp
 * <p>Package: com.mediatek.apst.util.command.message
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>Response for resending a SMS message.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ResendSmsRsp extends ResponseCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    public static final String ERR_SMS_NOT_EXIST = 
        "Sms to resend does not exist!";
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private long date;
    
    // For MTK DUAL-SIM feature.
    private int simId;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public ResendSmsRsp(int requestToken){
        super(FEATURE_MESSAGE, requestToken);
        // For MTK DUAL-SIM feature.
        this.simId = Message.SIM_ID;
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public long getDate(){
        return date;
    }
    
    // For MTK DUAL-SIM feature.
    /**
     * Gets via which SIM card the SMS message is resent.
     * @return The ID of SIM via which the SMS message is resent. 
     * Valid value can be SIM_ID, SIM1_ID or SIM2_ID.
     * @see com.mediatek.apst.util.entity.message.Message#SIM_ID
     * @see com.mediatek.apst.util.entity.message.Message#SIM1_ID
     * @see com.mediatek.apst.util.entity.message.Message#SIM2_ID
     */
    public int getSimId(){
        return this.simId;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setDate(long date){
        this.date = date;
    }
    
    // For MTK DUAL-SIM feature.
    /**
     * Sets via which SIM car the SMS message is resent.
     * @param simId The ID of SIM via which the SMS message is resent. 
     * Valid value can be SIM_ID, SIM1_ID or SIM2_ID.
     * @see com.mediatek.apst.util.entity.message.Message#SIM_ID
     * @see com.mediatek.apst.util.entity.message.Message#SIM1_ID
     * @see com.mediatek.apst.util.entity.message.Message#SIM2_ID
     */
    public void setSimId(int simId){
        this.simId = simId;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
