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
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
//import android.provider.ContactsContract.CommonDataKinds;
//import com.android.vcard.VCardConfig;
//import com.android.vcard.VCardEntryConstructor;
//import com.android.vcard.VCardParser_V21;
//import com.android.vcard.VCardParser_V30;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import java.io.IOException;


public class ContactBackupComposer extends Composer {
    private int mIdx;
    private static final String CONTACTTAG = "Contact:";
    private static final String fileappendix = ".vcf";
    private VCardComposer mVCardComposer;
    private int mCount;

    public ContactBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_CONTACT;
	}

    @Override
    public int getCount() {
        Log.d(LogTag.BACKUP, CONTACTTAG + "getCount():" + mCount);
        return mCount;
    }

    @Override
    public boolean init() {
        boolean result = false;
        mCount = 0;
        Cursor cur = mContext.getContentResolver()
            .query(Contacts.CONTENT_URI,
                   null,
                   null,
                   null,
                   null);

        if (cur != null) {
            cur.moveToFirst();
            mCount = cur.getCount();
            cur.close();
        }

        mVCardComposer = new VCardComposer(mContext, VCardConfig.VCARD_TYPE_V21_GENERIC, true);
        if(mVCardComposer.init(Contacts.CONTENT_URI, null, null, null, null)) {
            result = true;
        } else {
            mVCardComposer = null;
        }

        Log.d(LogTag.BACKUP, CONTACTTAG + "init():" + result + ",count:" + mCount);

        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mVCardComposer != null) {
            result = mVCardComposer.isAfterLast();
        }

        Log.d(LogTag.BACKUP, CONTACTTAG + "isAfterLast():" + result);
        return result;
    }

    @Override
    protected boolean implementComposeOneEntity() {
        boolean result = false;
        if (mVCardComposer != null && !mVCardComposer.isAfterLast()) {
            String zipContactName = "contacts/contact" + Integer.toString(++mIdx) + fileappendix;
            String tmpVcard = mVCardComposer.createOneEntry();

            if(tmpVcard != null && tmpVcard.length() > 0) {
                try {
                    mZipHandler.addFile(zipContactName, tmpVcard);
                    result = true;
                } catch (IOException e) {
                    if (super.mReporter != null) {
                        super.mReporter.onErr(e);
                    }
                }
            }

            Log.d(LogTag.BACKUP, CONTACTTAG + "add " + zipContactName + ",result:" + result);
        }

        return result;
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mVCardComposer != null) {
            mVCardComposer.terminate();
            mVCardComposer = null;
        }
    }
}
