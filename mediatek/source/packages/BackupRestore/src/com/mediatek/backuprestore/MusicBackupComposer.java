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
import android.provider.MediaStore.Audio;
import android.provider.MediaStore;
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Describe class <code>MusicBackupComposer</code> here.
 *
 * @author 
 * @version 1.0
 */
public class MusicBackupComposer extends Composer {
    public static final String MUSICTAG = "Music:"; 
    private ArrayList<String> mList;
    private static final Uri[] mMusicUri = {
        //Audio.Media.INTERNAL_CONTENT_URI,
        Audio.Media.EXTERNAL_CONTENT_URI
    };
    private Cursor[] mMusicCur = { null };

    private static final String[] projection = new String[] {
        Audio.Media._ID,
        Audio.Media.DATA
    };

    /**
     * Creates a new <code>MusicBackupComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public MusicBackupComposer(Context context) {
        super(context);
    }
    
    /**
     * Describe <code>init</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean init() {
        boolean result = false;
        for(int i = 0; i < mMusicCur.length; ++i) {
            if(mMusicUri[i] == Audio.Media.EXTERNAL_CONTENT_URI) {
                String path = BackupRestoreUtils.getStoragePath();
                String externalSDPath = "%" + path.subSequence(0, path.lastIndexOf(File.separator) + 1) + "%";
                mMusicCur[i] = mContext.getContentResolver().query(mMusicUri[i],
                                                                   projection,
                                                                   Audio.Media.DATA + " not like ?",
                                                                   new String[] {externalSDPath},
                                                                   null);
            } else {
                mMusicCur[i] = mContext.getContentResolver().query(mMusicUri[i],
                                                                   projection,
                                                                   null,
                                                                   null,
                                                                   null);
            }
            if(mMusicCur[i] != null) {
                mMusicCur[i].moveToFirst();
                result = true;
            }
        }

        mList = new ArrayList<String>();
        Log.d(LogTag.BACKUP, MUSICTAG + "init():" + result + ",count:" + getCount());
        return result;
    }

    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getModuleType() {
        return ModuleType.TYPE_MUSIC;
    }

    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    public final int getCount() {
        int count = 0;
        for (Cursor cur : mMusicCur) {
            if (cur != null && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        Log.d(LogTag.BACKUP, MUSICTAG + "getCount():" + count);
        return count;
    }

    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean isAfterLast() {
        boolean result = true;
        for (Cursor cur : mMusicCur) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(LogTag.BACKUP, MUSICTAG + "isAfterLast():" + result);
        return result;
    }

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public final boolean implementComposeOneEntity() {
        boolean result = false;
        for(int i = 0; i < mMusicCur.length; ++i) {
            if(mMusicCur[i] != null && !mMusicCur[i].isAfterLast()) {
                int dataColumn = mMusicCur[i].getColumnIndexOrThrow(Audio.Media.DATA);
                String data = mMusicCur[i].getString(dataColumn);
                try {
                    String tmpName = "music" +
                        data.subSequence(data.lastIndexOf(File.separator),
                                         data.length())
                        .toString();
                    String destName = getZipName(tmpName);
                    if(destName != null) {
                        try {
                            mZipHandler.addFileByFileName(data, destName);
                            mList.add(destName);
                            result = true;
                        } catch(IOException e) {
                            if(super.mReporter != null) {
                                super.mReporter.onErr(e);
                            }
                            Log.d(LogTag.BACKUP, MUSICTAG + "zip file fail");
                        }
                    }

                    Log.d(LogTag.BACKUP, MUSICTAG + data + ",destName:" + destName);
                } catch(StringIndexOutOfBoundsException e) {
                    Log.d(LogTag.BACKUP, MUSICTAG + " StringIndexOutOfBoundsException");
                }

                mMusicCur[i].moveToNext();
                break;
            }
        }

        return result;
    }
 

    /**
     * Describe <code>onStart</code> method here.
     *
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Describe <code>onEnd</code> method here.
     *
     */
    @Override
    public void onEnd() {
        super.onEnd();
        if(mList != null) {
            mList.clear();
        }

        for (Cursor cur : mMusicCur) {
            if(cur != null) {
                cur.close();
                cur = null;
            }
        }
    }


    /**
     * Describe <code>getZipName</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String getZipName(String name) {
        if (!mList.contains(name)) {
            return name;
        } else {
            return rename(name);
        }
    }


    /**
     * Describe <code>rename</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
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
}
