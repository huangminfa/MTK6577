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

package com.mediatek.apst.util.entity;

import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Class Name: DatabaseRecordEntity
 * <p>Package: com.mediatek.apst.util.entity
 * <p>Created on: 2010-6-10
 * <p>
 * <p>Description: 
 * <p>Abstract base class represents a database record.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public abstract class DatabaseRecordEntity implements SqliteBoolean, Cloneable, 
Serializable, IRawTransferable, IRawTransferableWithVersion {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    /**
     * Id value represents the record is not stored in database.
     */
    public static final long ID_NULL = -1L;
    /*
     * Id value represents the record needs deleting in database. This typically 
     * occurs in a record passed by external modules/applications, which means 
     * the record is deleted on external modules/applications, and need further 
     * deletion in our database.
     */
    /*public static final long ID_DELETED = -255;*/
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    /**
     * The unique id for one database record.
     */
    private long _id; 
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    /**
     * Construct a new database record with the specified id.
     */
    public DatabaseRecordEntity(long id){
        this._id = id;
    }
    
    /**
     * Construct a new database record with the default id(ID_NULL).
     * @see #ID_NULL
     */
    public DatabaseRecordEntity(){
        this._id = ID_NULL;
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    /**
     * @return The unique id.
     */
    public long getId(){
        return _id;
    }
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    /**
     * @param id The value to set.
     */
    public void setId(long id){
        this._id = id;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    //@Override
    public DatabaseRecordEntity clone() throws CloneNotSupportedException {
        DatabaseRecordEntity copy = (DatabaseRecordEntity)(super.clone());
        
        return copy;
    }
    /*
    public boolean isDeleted(){
        return (this._id == ID_DELETED);
    }
    
    public void setDeleted(){
        this._id = ID_DELETED;
    }
    */
    /*
    //@Override
    public int getRawSize(){
        // _id
        return RawTransUtil.LONG;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        // _id
        buffer.putLong(this._id);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        // _id
        this._id = buffer.getLong();
    }
 // Yu for ICS 
	//@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
		// _id
		this._id = buffer.getLong();
	}

	//@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
		// _id
        buffer.putLong(this._id);
	}
    
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
