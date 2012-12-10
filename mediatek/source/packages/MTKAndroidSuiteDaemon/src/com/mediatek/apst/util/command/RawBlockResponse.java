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

import com.mediatek.apst.util.command.ResponseCommand;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: RawBlockResponse
 * <p>Package: com.mediatek.apst.util.command
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description: 
 * <p>A RawBlockResponse contains a byte array to store massive data. Due to 
 * low efficiency on transmitting massive Java objects, it's recommended to 
 * transmit massive data in raw bytes instead.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class RawBlockResponse extends ResponseCommand {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 2L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private byte[] raw;
    
    private int progress;
    
    private int total;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Creates a RawBlockResponse with the specified feature ID and token.
     * @param featureId Feature ID of the response command.
     * @param requestToken The token to set(typically, the one of the request). 
     */
    public RawBlockResponse(int featureId, int requestToken){
        super(featureId, requestToken);
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
    
    /**
     * Gets the current progress.
     * @return The current progress.
     */
    public int getProgress(){
        return progress;
    }
    
    /**
     * Gets the total value(the max value of progress). 
     * @return The total value.
     */
    public int getTotal(){
        return total;
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
    
    /**
     * Sets the current progress. It may takes more than one response when there 
     * is too great a deal of data need to send as the reply, and in that case, 
     * every single response can use this value to indicate the current 
     * progress.
     * @param progress The progress value to set.
     */
    public void setProgress(int progress){
        this.progress = progress;
    }
    
    /**
     * Sets the total value(the max value of progress). 
     * @param total The total value to set.
     */
    public void setTotal(int total){
        this.total = total;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Gets a builder for creating a RawBlockResponse. Also initializes the 
     * builder with the specified raw block size and feature ID.
     * @param rawBlockSize The raw block size. This will be set as the size of 
     * byte buffer, and no more bytes can be added when the buffer is full.
     * @param featureId The feature ID to set for the RawBlockResponse.
     * @return A builder instance for creating a RawBlockResponse.
     */
    public static Builder builder(int rawBlockSize, int featureId){
        return new Builder(rawBlockSize, featureId);
    }

    /**
     * Gets a builder for creating a RawBlockResponse. Also initializes the 
     * builder with the specified feature ID. Raw block size will be set as the 
     * default value.
     * @param featureId The feature ID to set for the RawBlockResponse.
     * @return A builder instance for creating a RawBlockResponse.
     * @see RawTransUtil#DEFAULT_BUFFER_SIZE
     */
    public static Builder builder(int featureId){
        return new Builder(featureId);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    /**
     * Class Name: RawBlockResponse.Builder
     * <p>Package: com.mediatek.apst.util.command
     * <p>Created on: 2010-12-18
     * <p>
     * <p>Description: 
     * <p>Builder class help to create and fill a RawBlockResponse.
     * <p>
     * @author mtk80734 Siyang.Miao
     * @version V1.0
     */
    public static class Builder{
        
        private RawBlockResponse cmd;
        private ByteBuffer buffer;
        private int featureId;
        private int rawBlockSize;

        /**
         * Creates a builder for creating a RawBlockResponse. Also initializes 
         * the builder with the specified raw block size and feature ID.
         * @param rawBlockSize The raw block size. This will be set as the size 
         * of byte buffer, and no more bytes can be added when the buffer is 
         * full.
         * @param featureId The feature ID to set for the RawBlockResponse.
         */
        protected Builder(int rawBlockSize, int featureId){
            this.featureId = featureId;
            if (this.rawBlockSize > 0){
                this.rawBlockSize = rawBlockSize;
            } else {
                System.out.println("[RawBlockResponse] Invalid block size. " +
                        "Block will be allocated by " + 
                        RawTransUtil.DEFAULT_BUFFER_SIZE + 
                        " bytes by default.");
                this.rawBlockSize = RawTransUtil.DEFAULT_BUFFER_SIZE;
            }
            reset();
        }
        
        /**
         * Creates a builder for creating a RawBlockResponse. Also initializes 
         * the builder with the specified feature ID. Raw block size will be set 
         * as the default value.
         * @param featureId The feature ID to set for the RawBlockResponse.
         * @return A builder instance for creating a RawBlockResponse.
         * @see RawTransUtil#DEFAULT_BUFFER_SIZE
         */
        protected Builder(int featureId){
            this.featureId = featureId;
            this.rawBlockSize = RawTransUtil.DEFAULT_BUFFER_SIZE;
            reset();
        }
        
        /**
         * Override it. Should return a new RawBlockResponse instance. This 
         * instance will be returned when build() is invoked.
         * @return The new RawBlockResponse instance
         * @see #build()
         */
        protected RawBlockResponse onCreateCommand(int featureId){
            return new RawBlockResponse(featureId, -1);
        }
        
        /**
         * Gets the command wrapped in.
         * @return The command instance.
         */
        protected RawBlockResponse command(){
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
         * Makes the command token with the specified one.
         * @param token The token to set.
         * @return The builder.
         */
        public Builder makeToken(int token){
            cmd.setToken(token);
            return this;
        }
        
        /**
         * Makes the command progress value with the specified one.
         * @param progress The progress value to set.
         * @return The builder.
         */
        public Builder makeProgress(int progress){
            cmd.setProgress(progress);
            return this;
        }
        
        /**
         * Makes the command total value with the specified one.
         * @param total The total value to set.
         * @return The builder.
         */
        public Builder makeTotal(int total){
            cmd.setTotal(total);
            return this;
        }
        
        /**
         * Reset the builder for creating a new RawBlockResponse.
         */
        public void reset(){
            this.cmd = onCreateCommand(this.featureId);
            if (null == this.buffer){
                this.buffer = ByteBuffer.allocate(this.rawBlockSize);
            } else {
                this.buffer.clear();
            }
        }
        
        /**
         * Returns a RawBlockResponse with data supplied to the builder.
         * @return The RawBlockResponse instance.
         */
        public RawBlockResponse build(){
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
