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

/**
 * Class Name: ContactDataAdapter
 * <p>Package: com.mediatek.apst.util.entity.contacts
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>A helper class for getting <b>ContactData</b> instance from raw bytes 
 * when the instance is actually one of a derived class but it's not clear what 
 * class it is.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public abstract class ContactDataAdapter {
    //==============================================================
    // Constants                                                    
    //==============================================================
    
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Read a ContactData instance from the raw bytes. But besides the generic 
     * fields of ContactData, this instance also contains all fields of the 
     * child class. This method will find out the MIME type of the ContactData 
     * and then parse bytes with the corresponding rule to return an instance 
     * of appropriate child class. 
     * @param buffer The byte buffer.
     * @return The ContactData instance.
     */
    public static ContactData readRaw(ByteBuffer buffer, int versionCode) 
    throws NullPointerException{
        ContactData generic = null;
        // Get mime type of ContactData. Use absolute get in order to keep 
        // buffer position flag.
        int mimeType = buffer.getInt(buffer.position() 
                + 16/* offset of mimeType */);
        // Read specific fields of derived class
        switch (mimeType){
        case GroupMembership.MIME_TYPE:
            generic = new GroupMembership();
            ((GroupMembership)generic).readRaw(buffer);
            break;
            
        case StructuredName.MIME_TYPE:
            generic = new StructuredName();
            ((StructuredName)generic).readRaw(buffer);
            break;
            
        case Phone.MIME_TYPE:
            generic = new Phone();
            //((Phone)generic).readRaw(buffer); 
            // Modified by Shaoying Han
            ((Phone)generic).readRawWithVersion(buffer, versionCode);
            break;
            
        case Photo.MIME_TYPE:
            generic = new Photo();
            ((Photo)generic).readRaw(buffer);
            break;
            
        case Email.MIME_TYPE:
            generic = new Email();
            ((Email)generic).readRaw(buffer);
            break;
            
        case Im.MIME_TYPE:
            generic = new Im();
            ((Im)generic).readRaw(buffer);
            break;
            
        case StructuredPostal.MIME_TYPE:
            generic = new StructuredPostal();
            ((StructuredPostal)generic).readRaw(buffer);
            break;
            
        case Organization.MIME_TYPE:
            generic = new Organization();
            ((Organization)generic).readRaw(buffer);
            break;
            
        case Note.MIME_TYPE:
            generic = new Note();
            ((Note)generic).readRaw(buffer);
            break;
            
        case Nickname.MIME_TYPE:
            generic = new Nickname();
            ((Nickname)generic).readRaw(buffer);
            break;
            
        case Website.MIME_TYPE:
            generic = new Website();
            ((Website)generic).readRaw(buffer);
            break;
        
        default:
            break;
        }
        return generic;
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
