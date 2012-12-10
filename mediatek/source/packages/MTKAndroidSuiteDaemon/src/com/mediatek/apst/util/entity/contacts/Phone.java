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

package com.mediatek.apst.util.entity.contacts;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.BaseTypes;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Phone
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Phone data of a contact.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Phone extends ContactData implements BaseTypes, Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 7;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/phone_v2";
    
    /**
     * Column indicate_phone_or_sim_contact default value.
     */
    public static final int DEFAULT_BINDING_SIM_ID = -1;
    
    // Type define constants
    public static final int TYPE_HOME           = 1;
    public static final int TYPE_MOBILE         = 2;
    public static final int TYPE_WORK           = 3;
    public static final int TYPE_FAX_WORK       = 4;
    public static final int TYPE_FAX_HOME       = 5;
    public static final int TYPE_PAGER          = 6;
    public static final int TYPE_OTHER          = 7;
    public static final int TYPE_CALLBACK       = 8;
    public static final int TYPE_CAR            = 9;
    public static final int TYPE_COMPANY_MAIN   = 10;
    public static final int TYPE_ISDN           = 11;
    public static final int TYPE_MAIN           = 12;
    public static final int TYPE_OTHER_FAX      = 13;
    public static final int TYPE_RADIO          = 14;
    public static final int TYPE_TELEX          = 15;
    public static final int TYPE_TTY_TDD        = 16;
    public static final int TYPE_WORK_MOBILE    = 17;
    public static final int TYPE_WORK_PAGER     = 18;
    public static final int TYPE_ASSISTANT      = 19;
    public static final int TYPE_MMS            = 20;
    
    //==============================================================
    // Fields                                                       
    //==============================================================

    private String number;
    
    private int type;
    
    private String label;
    
    private int bindingSimId;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================

	public Phone(long id){
        super(id, MIME_TYPE);
        type = TYPE_NONE;
        bindingSimId = DEFAULT_BINDING_SIM_ID;
    }
    
    public Phone(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public String getNumber() {
        return number;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }
    
    public int getBindingSimId() {
		return bindingSimId;
	}

    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setNumber(String number) {
        this.number = number;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

	public void setBindingSimId(int bindingSimId) {
		this.bindingSimId = bindingSimId;
	}
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public Phone clone() throws CloneNotSupportedException{
        Phone copy = (Phone)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // number
        size += RawTransUtil.sizeOfString(this.number);
        // type
        size += RawTransUtil.INT;
        // label
        size += RawTransUtil.sizeOfString(this.label);
        return size;
    }
    */
    //@Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // number
        RawTransUtil.putString(buffer, this.number);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
    }
    
    //@Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // number
        this.number = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
    }

	@Override
	public void readRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferUnderflowException {
		super.readRaw(buffer);
		// number
        this.number = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
        
        if (versionCode >= 0x00000002) {
        	// bindingSimId
        	this.bindingSimId = buffer.getInt();
        }
	}

	@Override
	public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
			throws NullPointerException, BufferOverflowException {
		super.writeRaw(buffer);
		// number
        RawTransUtil.putString(buffer, this.number);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
        
        if (versionCode >= 0x00000002) {
        	// bindingSimId
        	buffer.putInt(this.bindingSimId);
        }
	}
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
