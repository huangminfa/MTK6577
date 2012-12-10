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
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

public class MessageRestoreComposer extends Composer {
    private List<Composer> mComposers;
    private static final String MESSAGETAG = "Message:";
    private long mTime;

	public MessageRestoreComposer(Context context) {
		super(context);
        mComposers = new ArrayList<Composer>();
        mComposers.add(new SmsRestoreComposer(context));
        mComposers.add(new MmsRestoreComposer(context));
	}

    @Override
    public void setZipFileName(String fileName) {
        super.setZipFileName(fileName);
        for (Composer composer : mComposers) {
            composer.setZipFileName(fileName);
        }
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_MESSAGE;
	}

    @Override
    public int getCount() {
        int count = 0;
        for (Composer composer : mComposers) {
            if (composer != null) {
                count += composer.getCount();
            }
        }

        Log.d(LogTag.RESTORE, MESSAGETAG + "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = true;
        mTime = System.currentTimeMillis();
        for (Composer composer : mComposers) {
            if (composer != null) {
                if (!composer.init()) {
                    result = false;
                }
            }
        }

        Log.d(LogTag.RESTORE, MESSAGETAG + "init():" + result + ",count:" + getCount());
        return result;
    }


    @Override
    public boolean isAfterLast() {
        boolean result = true;
        for (Composer composer : mComposers) {
            if (composer != null && !composer.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(LogTag.RESTORE, MESSAGETAG + "isAfterLast():" + result);
        return result;
    }


    @Override
    public boolean implementComposeOneEntity() {
        for (Composer composer : mComposers) {
            if (composer != null && !composer.isAfterLast()) {
                return composer.composeOneEntity();
            }
        }

        return false;
    }


    private boolean deleteAllMessage() {
        boolean result = false;
        int count = 0;
        if (mContext != null) {
            Log.d(LogTag.RESTORE, MESSAGETAG + "begin delete:" + System.currentTimeMillis());
            count = mContext.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"),
                                                         "date < ?",
                                                         new String[]{Long.toString(mTime)});
            Log.d(LogTag.RESTORE, MESSAGETAG + "end delete:" + System.currentTimeMillis());

            result = true;
        }

        Log.d(LogTag.RESTORE, MESSAGETAG + "deleteAllMessage(),result" + result + "," + count + " deleted!");
        return result;
    }


    @Override
    public void onStart() {
        super.onStart();
        // for (Composer composer : mComposers) {
        //     if (composer != null) {
        //         composer.onStart();
        //     }
        // }
        deleteAllMessage();
        Log.d(LogTag.RESTORE, MESSAGETAG + "onStart()");
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (Composer composer : mComposers) {
            if (composer != null) {
                composer.onEnd();
            }
        }
        Log.d(LogTag.RESTORE, MESSAGETAG + "onEnd()");
    }

}
