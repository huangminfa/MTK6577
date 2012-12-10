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
import java.io.File;
import java.io.IOException;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import java.util.ArrayList;


public class PictureRestoreComposer extends Composer {
    private static final String PICTURETAG = "Picture:";
    private int mIdx;
    private ArrayList<String> mFileNameList;
    private static final String[] projection = new String[] {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
    };
    private String mDestPath = null;
    public PictureRestoreComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_PICTURE;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }

        Log.d(LogTag.RESTORE, PICTURETAG + "getCount():" + count);
        return count;
    }


    public boolean init() {
        boolean result = false;
        mFileNameList = new ArrayList<String>();
        try {
            mFileNameList = (ArrayList<String>)BackupZip.GetFileList(mZipFileName, true, true, "pictures/.*");
            String path = BackupRestoreUtils.getStoragePath();
            mDestPath = path.subSequence(0, path.lastIndexOf(File.separator)) + File.separator + mZipFileName.subSequence(mZipFileName.lastIndexOf(File.separator) + 1, mZipFileName.lastIndexOf(".")).toString() + File.separator + "image";
            result = true;
        } catch (IOException e) {
        }

        Log.d(LogTag.RESTORE, PICTURETAG + "init():" + result + ",count:" + mFileNameList.size());
        return result;
    }


    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIdx >= mFileNameList.size()) ? true : false;
        }

        Log.d(LogTag.RESTORE, PICTURETAG + "isAfterLast():" + result);
        return result;
    }


    public boolean implementComposeOneEntity() {
        boolean result = false;
        if (mDestPath == null) {
            return result;
        }

        String picName = mFileNameList.get(mIdx++);
        String destFileName = mDestPath + 
            picName.subSequence(picName.lastIndexOf("/"), picName.length()).toString();

        Log.d(LogTag.RESTORE,
              PICTURETAG + "restoreOnePicture(),zipFileName:" + mZipFileName +
              "\npicName:" + picName +
              "\ndestFileName:" + destFileName);

        try {
            BackupZip.unZipFile(mZipFileName, picName, destFileName);
            ContentValues v = new ContentValues();
            File f = new File(destFileName);
            v.put(Images.Media.SIZE, f.length());
            v.put("_data", destFileName);
            Uri tmpUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
            Log.d(LogTag.RESTORE, PICTURETAG + "tmpUri:" + tmpUri);
            f = null;
            result = true;
        } catch (IOException e) {
            if (super.mReporter != null) {
                super.mReporter.onErr(e);
            }
            Log.d(LogTag.RESTORE, PICTURETAG + "unzipfile failed");
        } catch (Exception e) {
        }

        return result;
    }

    private void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                int count = mContext.getContentResolver()
                    .delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + " like ?",
                            new String[] {file.getAbsolutePath()});
                Log.d(LogTag.RESTORE, 
                      PICTURETAG + "deleteFolder():" + count + ":" + file.getAbsolutePath());
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles(); 
                for (int i = 0; i < files.length; ++i) { 
                    this.deleteFolder(files[i]);
                }
            }

            file.delete();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDestPath != null) {
            File tmpFolder = new File(mDestPath);
            deleteFolder(tmpFolder);
        }

        Log.d(LogTag.RESTORE, PICTURETAG + "onStart()");
    }
}
