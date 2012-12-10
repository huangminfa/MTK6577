/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.util.command.backup;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.mediatek.apst.util.command.RawBlockResponse;
import com.mediatek.apst.util.entity.contacts.ContactData;
import com.mediatek.apst.util.entity.contacts.ContactDataAdapter;
import com.mediatek.apst.util.entity.contacts.RawContact;

/**
 * Class Name: RestoreContactsRsp
 * <p>Package: com.mediatek.apst.util.command.contacts
 * <p>Created on: 2010-12-18
 * <p>
 * <p>Description:
 * <p>Response for importing multiple contacts(with contact data). This response
 * contains some information that PC side need to know like database ID for
 * every inserted record.
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class RestoreContactsRsp extends RawBlockResponse {
    //==============================================================
    // Constants
    //==============================================================
    private static final long serialVersionUID = 2L;

    /**
     * Indicates that the response contains raw contacts insertion results.
     */
    public static final int PHASE_RAW_CONTACT = 0;
    /**
     * Indicates that the response contains contacts data insertion results.
     */
    public static final int PHASE_CONTACT_DATA = 1;

    //==============================================================
    // Fields
    //==============================================================
    private int phase;

    //==============================================================
    // Constructors
    //==============================================================
    public RestoreContactsRsp(int token){
        super(FEATURE_BACKUP, token);
    }

    //==============================================================
    // Getters
    //==============================================================
    /**
     * Gets the current phase. For the insertion will be done in 2 steps on
     * target side(first raw contacts and then contact data), the response can
     * be 2 types, <b>PHASE_RAW_CONTACT</b> or <b>PHASE_CONTACT_DATA</b>.
     * @return The current phase.
     * @see #PHASE_RAW_CONTACT
     * @see #PHASE_CONTACT_DATA
     */
    public int getPhase(){
        return this.phase;
    }

    //==============================================================
    // Setters
    //==============================================================
    /**
     * Sets the current phase. For the insertion will be done in 2 steps on
     * target side(first raw contacts and then contact data), the response can
     * be 2 types, <b>PHASE_RAW_CONTACT</b> or <b>PHASE_CONTACT_DATA</b>.
     * @param phase The current phase to set.
     * @see #PHASE_RAW_CONTACT
     * @see #PHASE_CONTACT_DATA
     */
    public void setPhase(int phase){
        this.phase = phase;
    }

    //==============================================================
    // Methods
    //==============================================================
    /**
     * Gets a list of inserted raw contacts.
     * @return The list of inserted raw contacts. Returns null if current phase
     * is not PHASE_RAW_CONTACT.
     * @see #PHASE_RAW_CONTACT
     */
    public ArrayList<RawContact> getAllRawContacts(int versionCode){
        if (PHASE_RAW_CONTACT != phase){
            // Phase dismatch
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(this.getRaw());
        int count = buffer.getInt();
        ArrayList<RawContact> results = new ArrayList<RawContact>(count);
        for (int i = 0; i < count; i++){
            RawContact item = new RawContact();
            // item.readRaw(buffer); Changed by Shaoying Han
            item.readRawWithVersion(buffer, versionCode);
            results.add(item);
        }
        return results;
    }

    /**
     * Gets a list of inserted contact data.
     * @return The list of inserted contact data. Returns null if current phase
     * is not PHASE_CONTACT_DATA.
     * @see #PHASE_CONTACT_DATA
     */
    public ArrayList<ContactData> getAllContactData(int versionCode){
        if (PHASE_CONTACT_DATA != phase){
            // Phase dismatch
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(this.getRaw());
        int count = buffer.getInt();
        ArrayList<ContactData> results = new ArrayList<ContactData>(count);
        for (int i = 0; i < count; i++){
            // ContactData item = ContactDataAdapter.readRaw(buffer);
        	// Modified by Shaoying Han
            ContactData item = ContactDataAdapter.readRaw(buffer, versionCode);
            results.add(item);
        }
        return results;
    }

    //==============================================================
    // Inner & Nested classes
    //==============================================================
}
