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
 * Class Name: Im
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-6-17
 * <p>
 * <p>Description: 
 * <p>IM data of a contact.
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Im extends ContactData implements BaseTypes, Cloneable {
    //==============================================================
    // Constants                                                    
    //==============================================================
    private static final long serialVersionUID = 1L;
    
    public static final int MIME_TYPE = 2;
    
    public static final String MIME_TYPE_STRING = 
        "vnd.android.cursor.item/im";

    // Type define constants
    public static final int TYPE_HOME   = 1;
    public static final int TYPE_WORK   = 2;
    public static final int TYPE_OTHER  = 3;
    
    // Protocol define constants
    public static final int PROTOCOL_NONE = -255;
    public static final int PROTOCOL_CUSTOM = -1;
    public static final int PROTOCOL_AIM = 0;
    public static final int PROTOCOL_MSN = 1;
    public static final int PROTOCOL_YAHOO = 2;
    public static final int PROTOCOL_SKYPE = 3;
    public static final int PROTOCOL_QQ = 4;
    public static final int PROTOCOL_GOOGLE_TALK = 5;
    public static final int PROTOCOL_ICQ = 6;
    public static final int PROTOCOL_JABBER = 7;
    public static final int PROTOCOL_NETMEETING = 8;
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    private String data;

    private int type;

    private String label;

    private int protocol;
    
    private String customProtocol;
    
    //==============================================================
    // Constructors                                                 
    //==============================================================

    public Im(long id){
        super(id, MIME_TYPE);
        type = TYPE_NONE;
        protocol = PROTOCOL_NONE;
    }
    
    public Im(){
        this(ID_NULL);
    }
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    public String getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public int getProtocol() {
        return protocol;
    }

    public String getCustomProtocol() {
        return customProtocol;
    }

    //==============================================================
    // Setters                                                      
    //==============================================================
    
    public void setData(String data) {
        this.data = data;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setCustomProtocol(String customProtocol) {
        this.customProtocol = customProtocol;
    }
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    public String getMimeTypeString(){
        return MIME_TYPE_STRING;
    }
    
    //@Override
    public Im clone() throws CloneNotSupportedException{
        Im copy = (Im)(super.clone());
        
        return copy;
    }
    /*
    //@Override
    public int getRawSize(){
        int size = super.getRawSize();
        // data
        size += RawTransUtil.sizeOfString(this.data);
        // type
        size += RawTransUtil.INT;
        // label
        size += RawTransUtil.sizeOfString(this.label);
        // protocol
        size += RawTransUtil.INT;
        // customProtocol
        size += RawTransUtil.sizeOfString(this.customProtocol);
        return size;
    }
    */
    //@Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException{
        super.writeRaw(buffer);
        // data
        RawTransUtil.putString(buffer, this.data);
        // type
        buffer.putInt(this.type);
        // label
        RawTransUtil.putString(buffer, this.label);
        // protocol
        buffer.putInt(this.protocol);
        // customProtocol
        RawTransUtil.putString(buffer, this.customProtocol);
    }
    
    //@Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException{
        super.readRaw(buffer);
        // data
        this.data = RawTransUtil.getString(buffer);
        // type
        this.type = buffer.getInt();
        // label
        this.label = RawTransUtil.getString(buffer);
        // protocol
        this.protocol = buffer.getInt();
        // customProtocol
        this.customProtocol = RawTransUtil.getString(buffer);
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
    
}
