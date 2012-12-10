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

import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.ProgressReporter;
import com.mediatek.backuprestore.Composer;

public abstract class Composer {

	protected Context mContext;
    protected ProgressReporter mReporter;
    protected BackupZip mZipHandler;
    protected String mZipFileName;

	public Composer(Context context) {
		mContext = context;
	}


    public void setZipHandler(BackupZip handler) {
        mZipHandler = handler;
    }

    public void setZipFileName(String fileName) {
        mZipFileName = fileName;
    }

    public void setReporter(ProgressReporter reporter) {
        mReporter = reporter;
    }

    protected boolean mIsCancel = false;
    synchronized public void setCancel( boolean cancel ){
        mIsCancel = cancel;
    }

    synchronized public boolean isCancel(){
        return mIsCancel;
    }

	abstract public int getModuleType();

	abstract public int getCount();

	private int mComposeredCount = 0;

	public int getComposed() {
		return mComposeredCount;
	}

    public void increaseComposed() {
		++mComposeredCount;
        if (mReporter != null) {
            mReporter.onOneFinished(this);
         }
	}

	abstract public boolean init();

	public void onStart() {
        if (mReporter != null) {
            mReporter.onStart(this);
        }
	}

	public void onEnd() {
        if (mReporter != null) {
            mReporter.onEnd(this, (getCount() == mComposeredCount && mComposeredCount > 0) ? true : false);
        }
	}

	abstract public boolean isAfterLast();

    public boolean composeOneEntity() {
		boolean result = implementComposeOneEntity();
        if (result) {
            ++mComposeredCount;
        }

        if (mReporter != null) {
            mReporter.onOneFinished(this);
        }

        return result;
	}

	abstract protected boolean implementComposeOneEntity();
}
