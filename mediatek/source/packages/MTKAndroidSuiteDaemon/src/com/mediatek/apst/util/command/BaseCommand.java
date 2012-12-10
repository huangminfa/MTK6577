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

package com.mediatek.apst.util.command;

import com.mediatek.apst.util.communication.common.TransportEntity;

/**
 * Class Name: BaseCommand
 * <p>Package: com.mediatek.apst.util.command
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Base class of commands. All communication commands should derive from it.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public abstract class BaseCommand extends TransportEntity {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    // RESERVED for connection related commands
    /*public static final int FEATURE_CONN      = 0x00000000;*/
    /** Feature ID for main frame. */
    public static final int FEATURE_MAIN        = 0x00000001;
    /** Feature ID for contacts. */
    public static final int FEATURE_CONTACTS    = 0x00000010;
    /** Feature ID for messages. */
    public static final int FEATURE_MESSAGE     = 0x00000100;
    /** Feature ID for applications. */
    public static final int FEATURE_APPLICATION = 0x00001000;
    /** Feature ID for outlook sync. */
    public static final int FEATURE_SYNC        = 0x00010000;
    /** Feature ID for media sync. */
    public static final int FEATURE_MEDIA       = 0x00100000;
    /** Feature ID for bookmark. */
    public static final int FEATURE_BOOKMARK    = 0x01100000;
    /** Feature ID for calendar. */
    public static final int FEATURE_CALENDAR    = 0x01000000;
    /** Feature ID for calendar sync. */
    public static final int FEATURE_CALENDAR_SYNC  = 0x00011000;
    /** Feature ID for backup. */
    public static final int FEATURE_BACKUP  = 0x01110000;
    
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Creates a new BaseCommand instance with a specified feature ID.
     * @param featureId Feature ID of the command.
     */
    public BaseCommand(int featureId){
        super(featureId);
    }
    
    /**
     * Creates a new BaseCommand instance.
     */
    public BaseCommand(){
        super();
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
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
