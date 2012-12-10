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

import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.mediatek.apst.target.util.Debugger;

public abstract class OperationBatchHelper {
    // ==============================================================
    // Constants
    // ==============================================================

    // ==============================================================
    // Fields
    // ==============================================================
    private ContentProviderOperationBatch mOpBatch;

    // ==============================================================
    // Constructors
    // ==============================================================
    public OperationBatchHelper(ContentProviderOperationBatch opBatch) {
        this.mOpBatch = opBatch;
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
    public void run(int batchSize) {
        /*
         * // Empty operation batch mOpBatch.clear();
         */
        for (int i = 0, executedPosition = 0; i < batchSize; i++) {
            onAppend(mOpBatch, i);
            // Operations batch has reached the max allowed size,
            // or the last operation has been appended to the batch,
            // We need to apply the batch immediately and release memory after
            if (mOpBatch.isFull() || i == batchSize - 1) {
                // Apply batch
                ContentProviderResult[] opResults = null;
                try {
                    opResults = onApply(mOpBatch);
                } catch (RemoteException e) {
                    Debugger.logE(getName(), "run", new Object[] { batchSize },
                            "Exception occurs in onApply(" + mOpBatch + ")", e);
                    return;
                } catch (OperationApplicationException e) {
                    Debugger.logE(getName(), "run", new Object[] { batchSize },
                            "Exception occurs in onApply(" + mOpBatch + ")", e);
                    return;
                } finally {
                    // Clear operations
                    mOpBatch.clear();
                }

                if (null != opResults) {
                    for (ContentProviderResult result : opResults) {
                        onOperationResult(result, executedPosition);
                        ++executedPosition;
                    }
                }
            }
        }
    }

    /**
     * Override it. Provide a class name/tag for log.
     * 
     * @return
     */
    public String getName() {
        return "OperationBatchHelper";
    };

    /**
     * Override it
     * 
     * @param opBatch
     */
    public abstract void onAppend(ContentProviderOperationBatch opBatch,
            int appendPosition);

    /**
     * Override it
     * 
     * @param opBatch
     */
    public abstract ContentProviderResult[] onApply(
            ContentProviderOperationBatch opBatch) throws RemoteException,
            OperationApplicationException;

    /**
     * Override it
     * 
     * @param opResult
     */
    public abstract void onOperationResult(ContentProviderResult opResult,
            int executedPosition);

    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
