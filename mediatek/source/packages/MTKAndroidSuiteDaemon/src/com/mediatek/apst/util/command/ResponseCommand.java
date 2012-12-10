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

/**
 * Class Name: ResponseCommand
 * <p>Package: com.mediatek.apst.util.command
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>Base class of all response commands. A response command is a command send 
 * out as a reply for a previous request. All response commands should derive 
 * from it.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public abstract class ResponseCommand extends BaseCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    /** 
     * Status code representing the previous request is handled successfully. 
     */
    public static final int SC_OK = 1;
    /**
     * Status code representing the previous request is handled unsuccessfully.
     */
    public static final int SC_FAILED = 2;
    /**
     * Status code representing the previous request has invalid arguments.
     */
    public static final int SC_INVALID_ARGUMENTS = 3;
    /**
     * Status code representing the previous request is an unsupported one.
     */
    public static final int SC_UNSUPPORTED_REQUEST = 4;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /**
     * Represents the result status of the corresponding request handling.
     */
    private int statusCode;
    /**
     * Additional message can be set to provide more information about the 
     * error.
     */
    private String errorMessage;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Creates a response command with the specified feature ID and token.
     * @param featureId Feature ID of the response command.
     * @param requestToken The token to set(typically, the one of the request). 
     */
    public ResponseCommand(int featureId, int requestToken) {
        super(featureId);
        this.statusCode = SC_OK;
        this.errorMessage = null;
        this.setToken(requestToken);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /**
     * Get the status code.
     * @return The status code.
     */
    public int getStatusCode(){
        return statusCode;
    }
    
    /**
     * Get the error message if set previously.
     * @return The error message.
     */
    public String getErrorMessage(){
        return errorMessage;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    /**
     * Set the status code.
     * @param statusCode The status code to set.
     */
    public void setStatusCode(int statusCode){
        this.statusCode = statusCode;
    }
    
    /**
     * Set the error message.
     * @param errorMessage The error message to set.
     */
    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
