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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.Composer;
import java.io.IOException;


/**
 * Describe class <code>NoteBookBackupComposer</code> here.
 *
 * @author 
 * @version 1.0
 */
public class NoteBookBackupComposer extends Composer {
    private static final String NOTEBOOKTAG = "NoteBook:";
    private Cursor mCur;
    private NoteBookXmlComposer mXmlComposer;

    public NoteBookBackupComposer(Context context) {
        super(context);
    }

    /**
     * Describe <code>init</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean init() {
        boolean result = false;
        Uri uri = Uri.parse("content://com.mediatek.notebook.NotePad/notes");
        try {
            mCur = mContext.getContentResolver().query(uri, null, null, null, null);
            if(mCur != null) {
                mCur.moveToFirst();
                result = true;
            }
        } catch(Exception e) {
            mCur = null;
            Log.d(LogTag.BACKUP, NOTEBOOKTAG + "read Uri exception");
        }

        Log.d(LogTag.BACKUP, NOTEBOOKTAG + "init():" + result + ",count::" + (mCur != null ? mCur.getCount() : 0));
        return result;
    }

    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getModuleType() {
        return ModuleType.TYPE_NOTEBOOK;
    }

    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getCount() {
        int count = 0;
        if(mCur != null) {
            count = mCur.getCount();
        }

        Log.d(LogTag.BACKUP, NOTEBOOKTAG + "getCount():" + count);
        return count;
    }

    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isAfterLast() {
        boolean result = true;
        if(mCur != null) {
            result = mCur.isAfterLast();
        }

        Log.d(LogTag.BACKUP, NOTEBOOKTAG + "isAfterLast():" + result);
        return result;
    }

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean implementComposeOneEntity() {
        boolean result = false;
        if(mCur != null && !mCur.isAfterLast()) {
            if(mXmlComposer != null) {
                try {
                    String title = mCur.getString(mCur.getColumnIndexOrThrow("title"));
                    String note = mCur.getString(mCur.getColumnIndexOrThrow("note"));
                    String created = mCur.getString(mCur.getColumnIndexOrThrow("created"));
                    String modified = mCur.getString(mCur.getColumnIndexOrThrow("modified"));
                    String notegroup = mCur.getString(mCur.getColumnIndexOrThrow("notegroup"));
                    NoteBookXmlInfo record = new NoteBookXmlInfo(title, note, created, modified, notegroup);
                    mXmlComposer.addOneMmsRecord(record);
                    result = true;
                } catch(IllegalArgumentException e) {
                }
            }

            mCur.moveToNext();
        }

        return result;
    }

    /**
     * Describe <code>onStart</code> method here.
     *
     */
    public void onStart() {
        super.onStart();
        if ((mXmlComposer = new NoteBookXmlComposer()) != null) {
            mXmlComposer.startCompose();
        }
    }


    /**
     * Describe <code>onEnd</code> method here.
     *
     */
    public void onEnd() {
        super.onEnd();
        if (mXmlComposer != null) {
            mXmlComposer.endCompose();
            String tmpXmlInfo = mXmlComposer.getXmlInfo();
            if (getComposed() > 0 && tmpXmlInfo != null) {
                try {
                    mZipHandler.addFile("notebook/notebook.xml", tmpXmlInfo);
                } catch (IOException e) {
                    if (super.mReporter != null) {
                        super.mReporter.onErr(e);
                    }
                }
            }
        }

        if(mCur != null) {
            mCur.close();
            mCur = null;
        }
    }

}
