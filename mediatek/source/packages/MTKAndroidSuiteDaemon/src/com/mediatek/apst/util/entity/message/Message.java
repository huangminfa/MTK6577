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

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Message
 * <p>Package: com.mediatek.apst.util.entity.message
 * <p>Created on: 2010-8-2
 * <p>
 * <p>Description: 
 * <p>Base message entity. Only basic informations are encapsulated.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Message extends DatabaseRecordEntity implements Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_SMS = 1;
    
    public static final int TYPE_MMS = 2;
    
    public static final int BOX_NONE = -1;
    /**
     *  Box for received messages
     */
    public static final int BOX_INBOX = 1;
    /**
     *  Box for sent messages
     */
    public static final int BOX_SENT = 2;
    /**
     *  Box for message drafts
     */
    public static final int BOX_DRAFT = 3;
    /**
     *  Box for outgoing messages
     */
    public static final int BOX_OUTBOX = 4;
    /**
     *  Box for failed outgoing messages
     */
    public static final int BOX_FAILED = 5;
    /**
     *  Box for messages to send later
     */
    public static final int BOX_QUEUED = 6;
    
    // For MTK DUAL-SIM feature.
    public static final int SIM_NONE = -1;
    /**
     * Represents the ID of SIM(single-SIM).
     */
    public static final int SIM_ID = 0;
    /**
     * Represents the ID of SIM 1(dual-SIM).
     */
    public static final int SIM1_ID = 0;
    /**
     * Represents the ID of SIM 2(dual-SIM).
     */
    public static final int SIM2_ID = 1;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    private long threadId;
    
    private TargetAddress target;
    
    private long date;
    
    private int box;
    
    private boolean read;
    
    private String subject;
    
    private boolean locked;
    
    // For ICS4.0, Added by Yu 2011-12-11
    private int date_sent;
    
    // For MTK DUAL-SIM feature.
    private int simId;
    
    // The name of the sim card. Added by Shaoying Han
    private String simName;
    
    // The number of the sim card. Added by Shaoying Han
    private String simNumber;
    
    // The ICCId of the sim card. Added by Shaoying Han
    private String simICCId;

	

	//==============================================================
    // Constructors                                                 
    //==============================================================
    public Message(long id) {
        super(id);
        this.threadId = ID_NULL;
        this.box = BOX_NONE;
        this.read = false;
        this.locked = false;
        // For MTK DUAL-SIM feature.
        this.simId = SIM_ID;
    }
    
    public Message(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    public long getThreadId() {
        return threadId;
    }
    
    public TargetAddress getTarget() {
        return target;
    }
    
    public long getDate() {
        return date;
    }
    
    public int getBox() {
        return box;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public int getDate_sent() {
        return date_sent;
    }
    
    // For MTK DUAL-SIM feature.
    public int getSimId(){
        return simId;
    }
    
    public String getSimName() {
		return simName;
	}
    
    public String getSimNumber() {
		return simNumber;
	}

    public String getSimICCId() {
		return simICCId;
	}

    //==============================================================
    // Setters                                                      
    //==============================================================
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
    
    public void setTarget(TargetAddress target) {
        if (null == target){
            // To guarantee safety, target address should not be null.
            this.target = new TargetAddress();
        } else {
            this.target = target;
        }
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public void setBox(int box) {
        this.box = box;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public void setDate_sent(int date_sent) {
        this.date_sent = date_sent;
    }
    
    // For MTK DUAL-SIM feature.
    public void setSimId(int simId){
        this.simId = simId;
    }
    
    public void setSimName(String simName) {
    	this.simName = simName;
	}
    
	public void setSimNumber(String simNumber) {
		this.simNumber = simNumber;
	}
    
	public void setSimICCId(String simICCId) {
		this.simICCId = simICCId;
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
    public Message clone() throws CloneNotSupportedException{
        Message copy = (Message)(super.clone());
        
        copy.target = this.target.clone();
        
        return copy;
    }
    
    public static boolean isOutGoing(int box){
        return  (box == BOX_SENT)
            || (box == BOX_OUTBOX)
            || (box == BOX_FAILED)
            || (box == BOX_QUEUED);
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // threadId
        buffer.putLong(this.threadId);
        // target
        this.target.writeRaw(buffer);
        // date
        buffer.putLong(this.date);
        // box
        buffer.putInt(this.box);
        // read
        RawTransUtil.putBoolean(buffer, this.read);
        // subject
        RawTransUtil.putString(buffer, this.subject);
        // locked
        RawTransUtil.putBoolean(buffer, this.locked);
        // For MTK DUAL-SIM feature.
        // simId
        buffer.putInt(this.simId);
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // threadId
        this.threadId = buffer.getLong();
        // target
        this.target = new TargetAddress();
        this.target.readRaw(buffer);
        // date
        this.date = buffer.getLong();
        // box
        this.box = buffer.getInt();
        // read
        this.read = RawTransUtil.getBoolean(buffer);
        // subject
        this.subject = RawTransUtil.getString(buffer);
        // locked
        this.locked = RawTransUtil.getBoolean(buffer);
        // For MTK DUAL-SIM feature.
        // simId
        this.simId = buffer.getInt();
        
    }

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // threadId
        this.threadId = buffer.getLong();
        // target
        this.target = new TargetAddress();
        this.target.readRaw(buffer);
        // date
        this.date = buffer.getLong();
        // box
        this.box = buffer.getInt();
        // read
        this.read = RawTransUtil.getBoolean(buffer);
        // subject
        this.subject = RawTransUtil.getString(buffer);
        // locked
        this.locked = RawTransUtil.getBoolean(buffer);
        // For MTK DUAL-SIM feature.
        // simId
        this.simId = buffer.getInt();
        
        if (versionCode >= 0x00000002) {
        	// sim name
        	this.simName = RawTransUtil.getString(buffer);
        	// sim number
        	this.simNumber = RawTransUtil.getString(buffer);
        	// sim ICCId
        	this.simICCId = RawTransUtil.getString(buffer);
        }
        
//        if (versionCode >= 1150 ) {
//            this.date_sent = buffer.getInt();
//        }
    }

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // threadId
        buffer.putLong(this.threadId);
        // target
        this.target.writeRaw(buffer);
        // date
        buffer.putLong(this.date);
        // box
        buffer.putInt(this.box);
        // read
        RawTransUtil.putBoolean(buffer, this.read);
        // subject
        RawTransUtil.putString(buffer, this.subject);
        // locked
        RawTransUtil.putBoolean(buffer, this.locked);
        // For MTK DUAL-SIM feature.
        // simId
        buffer.putInt(this.simId);
        
        if (versionCode >= 0x00000002) {
        	// sim name
        	RawTransUtil.putString(buffer, this.simName);
        	// sim number
        	RawTransUtil.putString(buffer, this.simNumber);
        	// sim ICCId
        	RawTransUtil.putString(buffer, this.simICCId);
        }
        
//        if (versionCode >= 1150 ) {
//            buffer.putInt(this.date_sent);
//        }
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
