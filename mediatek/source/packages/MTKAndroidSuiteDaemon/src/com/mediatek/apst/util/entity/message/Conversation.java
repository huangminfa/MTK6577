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

package com.mediatek.apst.util.entity.message;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Conversation
 * <p>Package: com.mediatek.apst.util.entity.message
 * <p>Created on: 2010-8-4
 * <p>
 * <p>Description: 
 * <p>Represents a conversation(thread).
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Conversation extends DatabaseRecordEntity implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private TargetAddress target;
    
    private long date;
    
    private String snippet;
    
    private List<Message> messages;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    public Conversation(long id) {
        super(id);
        this.messages = new ArrayList<Message>();
    }
    
    public Conversation(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public TargetAddress getTarget() {
        return target;
    }
    
    public long getDate() {
        return date;
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setTarget(TargetAddress target) {
        this.target = target;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Deep copy. 
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    //@Override
    public Conversation clone() throws CloneNotSupportedException{
        Conversation copy = (Conversation)(super.clone());
        
        copy.messages = new ArrayList<Message>();
        for (Message msg : this.messages){
            copy.messages.add(msg);
        }
        
        return copy;
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // target
        this.target.writeRaw(buffer);
        // date
        buffer.putLong(this.date);
        // snippet
        RawTransUtil.putString(buffer, this.snippet);
        // messages
        if (null != messages){
            buffer.putInt(messages.size());
            for (Message message : messages){
                message.writeRaw(buffer);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // target
        this.target.readRaw(buffer);
        // date
        this.date = buffer.getLong();
        // snippet
        this.snippet = RawTransUtil.getString(buffer);
        // messages
        int size = buffer.getInt();
        if (size >= 0){
            this.messages = new ArrayList<Message>(size);
            for (int i = 0; i < size; i++){
                Message message = new Message();
                message.readRaw(buffer);
                messages.add(message);
            }
        } else {
            this.messages = null;
        }
    }

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // target
        this.target.readRaw(buffer);
        // date
        this.date = buffer.getLong();
        // snippet
        this.snippet = RawTransUtil.getString(buffer);
        // messages
        int size = buffer.getInt();
        if (size >= 0){
            this.messages = new ArrayList<Message>(size);
            for (int i = 0; i < size; i++){
                Message message = new Message();
                message.readRawWithVersion(buffer, versionCode);
                messages.add(message);
            }
        } else {
            this.messages = null;
        }
    }

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // target
        this.target.writeRaw(buffer);
        // date
        buffer.putLong(this.date);
        // snippet
        RawTransUtil.putString(buffer, this.snippet);
        // messages
        if (null != messages){
            buffer.putInt(messages.size());
            for (Message message : messages){
                message.writeRawWithVersion(buffer, versionCode);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
