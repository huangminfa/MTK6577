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

import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: RawBlockRequest
 * <p>Package: com.mediatek.apst.util.command
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>A RawBlockRequest contains a byte array to store massive data. Due to 
 * low efficiency on transmitting massive Java objects, it's recommended to 
 * transmit massive data in raw bytes instead.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class RawBlockRequest extends RequestCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private byte[] raw;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Creates a RawBlockRequest with the specified feature ID.
     * @param featureId Feature ID of the request command.
     */
    public RawBlockRequest(int featureId){
        super(featureId);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /**
     * Gets the raw bytes data.
     * @return The raw bytes.
     */
    public byte[] getRaw(){
        return raw;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    /**
     * Sets the raw bytes data.
     * @param raw The raw bytes to set.
     */
    public void setRaw(byte[] raw){
        if (null == raw){
            // Raw data should not be null. It must at least contain 4 bytes 
            // representing data items count(0 by default).
            this.raw = new byte[4];
        } else {
            this.raw = raw;
        }
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Gets a builder for creating a RawBlockRequest. Also initializes the 
     * builder with the specified raw block size and feature ID.
     * @param rawBlockSize The raw block size. This will be set as the size of 
     * byte buffer, and no more bytes can be added when the buffer is full.
     * @param featureId The feature ID to set for the RawBlockRequest.
     * @return A builder instance for creating a RawBlockRequest.
     */
    public static Builder builder(int rawBlockSize, int featureId){
        return new Builder(rawBlockSize, featureId);
    }
    
    /**
     * Gets a builder for creating a RawBlockRequest. Also initializes the 
     * builder with the specified feature ID. Raw block size will be set as the 
     * default value.
     * @param featureId The feature ID to set for the RawBlockRequest.
     * @return A builder instance for creating a RawBlockRequest.
     * @see RawTransUtil#DEFAULT_BUFFER_SIZE
     */
    public static Builder builder(int featureId){
        return new Builder(featureId);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    /**
     * Class Name: RawBlockRequest.Builder
     * <p>Package: com.mediatek.apst.util.command
     * <p>Created on: 2010-12-18
     * <p>
     * <p>Description: 
     * <p>Builder class help to create and fill a RawBlockRequest.
     * <p>
     * @author mtk80734 Siyang.Miao
     * @version V1.0
     */
    public static class Builder{
        
        private RawBlockRequest cmd;
        private ByteBuffer buffer;
        private int featureId;
        private int rawBlockSize;
        
        /**
         * Creates a builder for creating a RawBlockRequest. Also initializes 
         * the builder with the specified raw block size and feature ID.
         * @param rawBlockSize The raw block size. This will be set as the size 
         * of byte buffer, and no more bytes can be added when the buffer is 
         * full.
         * @param featureId The feature ID to set for the RawBlockRequest.
         */
        protected Builder(int rawBlockSize, int featureId){
            this.featureId = featureId;
            if (this.rawBlockSize > 0){
                this.rawBlockSize = rawBlockSize;
            } else {
                System.out.println("[RawBlockRequest] Invalid block size. " +
                        "Block will be allocated by " + 
                        RawTransUtil.DEFAULT_BUFFER_SIZE + 
                        " bytes by default.");
                this.rawBlockSize = RawTransUtil.DEFAULT_BUFFER_SIZE;
            }
            reset();
        }
        
        /**
         * Creates a builder for creating a RawBlockRequest. Also initializes 
         * the builder with the specified feature ID. Raw block size will be set 
         * as the default value.
         * @param featureId The feature ID to set for the RawBlockRequest.
         * @return A builder instance for creating a RawBlockRequest.
         * @see RawTransUtil#DEFAULT_BUFFER_SIZE
         */
        protected Builder(int featureId){
            this.featureId = featureId;
            this.rawBlockSize = RawTransUtil.DEFAULT_BUFFER_SIZE;
            reset();
        }
        
        /**
         * Override it. Should return a new RawBlockRequest instance. This 
         * instance will be returned when build() is invoked.
         * @return The new RawBlockRequest instance
         * @see #build()
         */
        protected RawBlockRequest onCreateCommand(int featureId){
            return new RawBlockRequest(featureId);
        }
        
        /**
         * Gets the command wrapped in.
         * @return The command instance.
         */
        protected RawBlockRequest command(){
            return cmd;
        }
        
        /**
         * Gets the byte buffer for putting data.
         * @return The command instance.
         */
        protected ByteBuffer buffer(){
            return buffer;
        }
        
        /**
         * Reset the builder for creating a new RawBlockRequest.
         */
        public void reset(){
            // Create a new wrapped-in RawBlockRequest instance
            this.cmd = onCreateCommand(this.featureId);
            if (null == this.buffer){
                // Create the byte buffer if it does not exist yet
                this.buffer = ByteBuffer.allocate(this.rawBlockSize);
            } else {
                // Clear the byte buffer if it already exists
                this.buffer.clear();
            }
        }
        
        /**
         * Returns a RawBlockRequest with data supplied to the builder.
         * @return The RawBlockRequest instance.
         */
        public RawBlockRequest build(){
            buffer.flip();
            byte[] raw = new byte[buffer.limit()];
            buffer.get(raw);
            cmd.setRaw(raw);
            buffer.clear();
            buffer = null;
            return cmd;
        }
        
    }

}
