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

package com.mediatek.apst.target.data.proxy;

import java.util.ArrayList;

import com.mediatek.apst.target.util.Debugger;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.RemoteException;

public class ObservedContentResolver {
    // ==============================================================
    // Constants
    // ==============================================================

    // ==============================================================
    // Fields
    // ==============================================================

    private ContentResolver mCR;

    private ISelfChangeObserver mSelfChangeOb;

    // ==============================================================
    // Constructors
    // ==============================================================

    public ObservedContentResolver(ContentResolver cr) {
        mCR = cr;
    }

    // ==============================================================
    // Getters
    // ==============================================================
    public ISelfChangeObserver getSelfChangeObserver() {
        return mSelfChangeOb;
    }

    // ==============================================================
    // Setters
    // ==============================================================

    // ==============================================================
    // Methods
    // ==============================================================

    public void registerSelfChangeObserver(ISelfChangeObserver ob) {
        mSelfChangeOb = ob;
    }

    public void unregisterSelfChangeObserver() {
        mSelfChangeOb = null;
    }

    public void setInnerContentResolver(ContentResolver cr) {
        mCR = cr;
    }

    private void selfChangeStart() {
        if (mSelfChangeOb != null) {
            mSelfChangeOb.onSelfChangeStart();
        } else {
            Debugger.logW(new Object[] {}, "mSelfChangeOb is null");
        }
    }

    private void selfChangeDone() {
        if (mSelfChangeOb != null) {
            mSelfChangeOb.onSelfChangeDone();
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor rt;
        rt = mCR.query(uri, projection, selection, selectionArgs, sortOrder);
        return rt;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Uri rt = null;
        if (null != mSelfChangeOb) {
            synchronized (mSelfChangeOb) {
                selfChangeStart();
                try {
                    rt = mCR.insert(uri, values);
                } catch (SQLException e) {
                    Debugger.logE(new Object[] { uri, values }, null, e);
                }
                try {
                    mSelfChangeOb.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                selfChangeDone();
            }
        } else {
            selfChangeStart();
            try {
                rt = mCR.insert(uri, values);
            } catch (SQLException e) {
                Debugger.logE(new Object[] { uri, values }, null, e);
            }
            selfChangeDone();
        }
        return rt;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int rt = 0;
        selfChangeStart();
        try {
            rt = mCR.bulkInsert(uri, values);
        } catch (SQLException e) {
            Debugger.logE(new Object[] { uri, values }, null, e);
        }
        selfChangeDone();
        return rt;
    }

    public int update(Uri uri, ContentValues values, String where,
            String[] selectionArgs) {
        int rt = 0;
        selfChangeStart();
        try {
            rt = mCR.update(uri, values, where, selectionArgs);
        } catch (SQLException e) {
            Debugger.logE(new Object[] { uri, values, where, selectionArgs },
                    null, e);
        }
        selfChangeDone();
        return rt;
    }

    public int delete(Uri uri, String where, String[] selectionArgs) {
        int rt = 0;
        selfChangeStart();
        try {
            rt = mCR.delete(uri, where, selectionArgs);
        } catch (SQLException e) {
            Debugger.logE(new Object[] { uri, where, selectionArgs }, null, e);
        }
        selfChangeDone();
        return rt;
    }

    public ContentProviderResult[] applyBatch(String authority,
            ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        ContentProviderResult[] rt = null;
        selfChangeStart();
        try {
            rt = mCR.applyBatch(authority, operations);
        } catch (SQLException e) {
            Debugger.logE(new Object[] { authority, operations }, null, e);
        }
        selfChangeDone();
        return rt;
    }

    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
