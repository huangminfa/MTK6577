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
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;

public class PictureBackupComposer extends Composer {
    private ArrayList<String> mList = null;
    public static final String PICTURETAG = "Picture:"; 
    private static final String[] projection = new String[] {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
    };
    private static final Uri[] mPictureUri = {
        Images.Media.INTERNAL_CONTENT_URI, 
        Images.Media.EXTERNAL_CONTENT_URI
    };
    private Cursor[] mPictureCur = {null, null};
    // private static final Uri[] mPictureUri = {
    //     Images.Media.EXTERNAL_CONTENT_URI
    // };
    // private Cursor[] mPictureCur = {null};
    //private int mPictureCount = -1;

    public PictureBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_PICTURE;
	}

    @Override
    public int getCount() {
        int count = 0;
        for (Cursor cur : mPictureCur) {
            if (cur != null && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        Log.d(LogTag.BACKUP, PICTURETAG + "getCount():" + count);
        return count;
    }

    @Override
    public boolean init() {
        boolean result = false;
        for (int i = 0; i < mPictureCur.length; ++i) {
            if (mPictureUri[i] == Images.Media.EXTERNAL_CONTENT_URI) {
                String path = BackupRestoreUtils.getStoragePath();
                String externalSDPath = "%" + path.subSequence(0, path.lastIndexOf(File.separator) + 1) + "%";
                mPictureCur[i] = mContext.getContentResolver()
                    .query(mPictureUri[i], 
                           projection,
                           MediaStore.Images.Media.DATA + " not like ?",
                           new String[] {externalSDPath},
                           null);
            } else {
                mPictureCur[i] = mContext.getContentResolver()
                    .query(mPictureUri[i], 
                           projection,
                           null,
                           null,
                           null);                
            }

            if (mPictureCur[i] != null) {
                mPictureCur[i].moveToFirst();
                result = true;
            }
        }

        if (result) {
            mList = new ArrayList<String>();
        }
        Log.d(LogTag.BACKUP, PICTURETAG + "init():" + result + ",count:" + getCount());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        for (Cursor cur : mPictureCur) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(LogTag.BACKUP, PICTURETAG + "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean implementComposeOneEntity() {
        boolean result = false;
        for (int i = 0; i < mPictureCur.length; ++i) {
            if (mPictureCur[i] != null && !mPictureCur[i].isAfterLast()) {
                int dataColumn = mPictureCur[i].getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String data = mPictureCur[i].getString(dataColumn);

                String tmpName = "pictures" +
                    data.subSequence(
                                     data.lastIndexOf(File.separator),
                                     data.length())
                    .toString();

                String destName = getZipName(tmpName);
                if (destName != null) {
                    try {
                        mZipHandler.addFileByFileName(data, destName);
                        mList.add(destName);
                        result = true;
                    } catch (IOException e) {
                        if (super.mReporter != null) {
                            super.mReporter.onErr(e);
                        }
                        Log.d(LogTag.BACKUP, PICTURETAG + "zip file fail");
                    }
                }
                Log.d(LogTag.BACKUP, PICTURETAG + "pic:" + data + ",destName:" + destName);
                mPictureCur[i].moveToNext();
                break;
            }
        }

        return result;
    }


    private String getZipName(String name) {
        if (!mList.contains(name)) {
            return name;
        } else {
            return rename(name);
        }
    }

    private String rename(String name) {
        String tmpName;
        int id = name.lastIndexOf(".");
        int id2, leftLen;
        for (int i = 1; i < (1<<12); ++i) {
            leftLen = 255 - (1 + Integer.toString(i).length() + name.length() - id);
            id2 = id <= leftLen ? id : leftLen;
            tmpName = name.subSequence(0, id2) + "~" + i + name.subSequence(id, name.length());
            if (!mList.contains(tmpName)) {
                return tmpName;
            }
        }

        return null;
    }

    // @Override
    // public void onStart() {
    //     super.onStart();
    // }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mList != null) {
            mList.clear();
        }

        for (Cursor cur : mPictureCur) {
            if(cur != null) {
                cur.close();
                cur = null;
            }
        }
    }
}
