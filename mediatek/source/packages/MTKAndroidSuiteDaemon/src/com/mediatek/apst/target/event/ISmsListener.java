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

package com.mediatek.apst.target.event;

import com.mediatek.apst.target.event.Event;

public interface ISmsListener {
    //==============================================================
    // Constants                                                    
    //==============================================================
    // Arguments for SMS sent event
    /**
     * Message ID in database. <b>Type:long</b>.
     */
    public static final String SMS_ID = "sms_id";
    /**
     * Message date. <b>Type: long</b>.
     */
    public static final String DATE = "date";
    /**
     * Is message sent successfully. <b>Type: boolean</b>.
     */
    public static final String SENT = "sent";
    // Arguments for SMS received event
    /**
     * SMS received time(Message date in INBOX should be later than this). 
     * <b>Type: long</b>.
     */
    public static final String AFTER_TIME_OF = "after_time_of";
    /**
     * Message address. <b>Type: String</b>.
     */
    public static final String ADDRESS = "address";
    /**
     * SMS body. <b>Type: String</b>.
     */
    public static final String BODY = "body";
    // Arguments for SMS insertion event
    /**
     * SMS. <b>Type: Sms</b>.
     */
    public static final String SMS = "sms";
    // Arguments for SMS content changed event
    /**
     * Is SMS content changed by our daemon. <b>Type: boolean</b>.
     */
    public static final String BY_SELF = "by_self";
    
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public void onSmsSent(Event event);
    
    public void onSmsReceived(Event event);
    
    public void onSmsInserted(Event event);
    
    //==============================================================
    // Inner & Nested classes                                                      
    //==============================================================
    
}
