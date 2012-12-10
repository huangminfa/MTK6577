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

package com.mediatek.android.content;

import java.util.ArrayList;

import com.mediatek.apst.target.data.proxy.ObservedContentResolver;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.os.RemoteException;

public class ContentProviderOperationBatch {
    // ==============================================================
    // Constants
    // ==============================================================
    // At most 499 operations allowed per batch
    public static final int CAPACITY = 499;

    // ==============================================================
    // Fields
    // ==============================================================
    private ObservedContentResolver mOCR;

    private ArrayList<ContentProviderOperation> mOps;

    private int mark;

    // ==============================================================
    // Constructors
    // ==============================================================
    public ContentProviderOperationBatch(ObservedContentResolver ocr) {
        mOCR = ocr;
        mOps = new ArrayList<ContentProviderOperation>(CAPACITY);
    }

    // ==============================================================
    // Getters
    // ==============================================================

    // ==============================================================
    // Setters
    // ==============================================================

    // ==============================================================
    // Methods
    // ==============================================================
    /**
     * Append one content provider operation to this batch.
     * 
     * @param op
     *            New content provider operation to append to this batch.
     * @return True upon success, false if the batch is full.
     */
    public boolean append(ContentProviderOperation op) {
        if (isFull()) {
            return false;
        } else {
            mOps.add(op);
            return true;
        }
    }

    public void save() {
        mark = mOps.size();
    }

    public void rollback() {
        while (mOps.size() > mark) {
            // mOps is array based, so remove from the end
            mOps.remove(mOps.size() - 1);
        }
    }

    public int capacity() {
        return CAPACITY;
    }

    public int size() {
        return mOps.size();
    }

    public int remaining() {
        return CAPACITY - mOps.size();
    }

    public ContentProviderResult[] apply(String authority)
            throws RemoteException, OperationApplicationException {
        ContentProviderResult[] dataResults = null;
        dataResults = mOCR.applyBatch(authority, mOps);
        return dataResults;
    }

    public void clear() {
        mOps.clear();
    }

    public boolean isFull() {
        if (mOps.size() < CAPACITY) {
            return false;
        } else {
            return true;
        }
    }

    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
