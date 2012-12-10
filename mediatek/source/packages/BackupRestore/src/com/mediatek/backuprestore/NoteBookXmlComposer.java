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

package com.mediatek.backuprestore;

import android.util.Xml;
import java.io.IOException;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;


/**
 * Describe class NoteBookXmlComposer here.
 *
 *
 * Created: Sun May 13 17:10:16 2012
 *
 * @author
 * @version 1.0
 */
public class NoteBookXmlComposer {

    XmlSerializer serializer = null;
    StringWriter writer = null;

    /**
     * Creates a new <code>NoteBookXmlComposer</code> instance.
     *
     */
    public NoteBookXmlComposer() {
    }

    public boolean startCompose() {
        boolean result = false;
        serializer = Xml.newSerializer();
        writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            //serializer.startDocument("UTF-8", null);
            serializer.startDocument(null, false);
            serializer.startTag("", "NoteBook");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean endCompose() {
        boolean result = false;
        try {
            serializer.endTag("", "NoteBook");
            serializer.endDocument();
            result = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public boolean addOneMmsRecord(NoteBookXmlInfo record) {
        boolean result = false;
        try {
            serializer.startTag("", NoteBookXmlInfo.RECORD);
            if(record.getTitle() != null) {
                serializer.attribute("", NoteBookXmlInfo.TITLE, record.getTitle());
            }

            if(record.getNote() != null) {
                serializer.attribute("", NoteBookXmlInfo.NOTE, record.getNote());
            }

            if(record.getCreated() != null) {
                serializer.attribute("", NoteBookXmlInfo.CREATED, record.getCreated());
            }

            if(record.getModified() != null) {
                serializer.attribute("", NoteBookXmlInfo.MODIFIED, record.getModified());
            }

            if(record.getNoteGroup() != null) {
                serializer.attribute("", NoteBookXmlInfo.NOTEGROUP, record.getNoteGroup());
            }

            serializer.endTag("", NoteBookXmlInfo.RECORD);

            result = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getXmlInfo() {
        if (writer != null) {
            return writer.toString();
        }

        return null;
    }

}
