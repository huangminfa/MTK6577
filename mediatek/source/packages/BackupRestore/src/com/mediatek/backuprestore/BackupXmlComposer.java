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

import java.io.IOException;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;
import android.util.Xml;

public class BackupXmlComposer {
    XmlSerializer serializer = null;
    public String composeXml(BackupXmlInfo info) {
        serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);

            serializer.startDocument("UTF-8", null);
            // serializer.text("\n");
            // <root>
            serializer.startTag("", BackupXml.ROOT);
            // serializer.text("\n");

            // <backupdate>
            serializer.startTag("", BackupXml.BACKUPDATE);
            serializer.text(info.getBackupDateString());
            serializer.endTag("", BackupXml.BACKUPDATE);
            // serializer.text("\n");
        
            // <devicetype>
            serializer.startTag("", BackupXml.DEVICETYPE);
            serializer.text(info.getDevicetype());
            serializer.endTag("", BackupXml.DEVICETYPE);
            // serializer.text("\n");
            // <system>
            serializer.startTag("", BackupXml.SYSTEM);
            serializer.text(info.getSystem());
            serializer.endTag("", BackupXml.SYSTEM);
            // serializer.text("\n");
        
            // <component_list>
            serializer.startTag("", BackupXml.COMPONENT_LIST);
            // serializer.text("\n");
            // <component/>
            if (info.getContactNum() > 0) {
                addcomponentTag(BackupXml.CONTACTS, BackupXml.CONTACTS, BackupXml.CONTACTS, info.getContactNum());
            }

            if (info.getSmsNum() > 0) {
                addcomponentTag(BackupXml.SMS, BackupXml.SMS, BackupXml.SMS, info.getSmsNum());
            }

            if (info.getMmsNum() > 0) {
                addcomponentTag(BackupXml.MMS, BackupXml.MMS, BackupXml.MMS, info.getMmsNum());
            }

            if (info.getCalendarNum() > 0) {
                addcomponentTag(BackupXml.CALENDAR, BackupXml.CALENDAR,BackupXml.CALENDAR, info.getCalendarNum());
            }

            if (info.getAppNum() > 0) {
                addcomponentTag(BackupXml.APP, BackupXml.APP,BackupXml.APP, info.getAppNum());
            }

            if (info.getPictureNum() > 0) {
                addcomponentTag(BackupXml.PICTURE, BackupXml.PICTURE, BackupXml.PICTURE, info.getPictureNum());
            }

            if (info.getMusicNum() > 0) {
                addcomponentTag(BackupXml.MUSIC, BackupXml.MUSIC, BackupXml.MUSIC, info.getMusicNum());
            }

            if (info.getNoteBookNum() > 0) {
                addcomponentTag(BackupXml.NOTEBOOK, BackupXml.NOTEBOOK, BackupXml.NOTEBOOK, info.getNoteBookNum());
            }

            // </component_list>
            serializer.endTag("", BackupXml.COMPONENT_LIST);
            // serializer.text("\n");

            // </root>
            serializer.endTag("", BackupXml.ROOT);
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addcomponentTag(String name, String id, String folder, int count) throws IOException{
        serializer.startTag("", BackupXml.COMPONENT);

        serializer.attribute("", BackupXml.NAME, name);
        serializer.attribute("", BackupXml.ID, id);    
        // serializer.text("\n");
        
        serializer.startTag("", BackupXml.FOLDER);
        serializer.text(folder);
        serializer.endTag("", BackupXml.FOLDER);
        // serializer.text("\n");

        serializer.startTag("", BackupXml.COUNT);
        serializer.text(Integer.toString(count));
        serializer.endTag("", BackupXml.COUNT);
        // serializer.text("\n");

        serializer.endTag("", BackupXml.COMPONENT);
        // serializer.text("\n");
    }
}
