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

import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.BaseTypes;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Nickname
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>Nickname data of a contact.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Nickname extends ContactData implements BaseTypes, Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 4;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/nickname";

    // Type define constants
    public static final int TYPE_DEFAULT        = 1;
    public static final int TYPE_OTHER_NAME     = 2;
    public static final int TYPE_MAIDEN_NAME    = 3;
    public static final int TYPE_SHORT_NAME     = 4;
    public static final int TYPE_INITIALS       = 5;
    
    //==============================================================
    // Fields                                                       
    //==============================================================

    private String name;

    private int type;

    private String label;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    public Nickname(long id){
        super(id, MIME_TYPE);
        type = TYPE_NONE;
    }
    
    public Nickname(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public Nickname clone() throws CloneNotSupportedException{
        Nickname copy = (Nickname)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // name
        size += RawTransUtil.sizeOfString(this.name);
        // type
        size += RawTransUtil.INT;
        // label
        size += RawTransUtil.sizeOfString(this.label);
        return size;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // name
        RawTransUtil.putString(buffer, this.name);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // name
        this.name = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
