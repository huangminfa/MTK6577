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

package com.mediatek.MediatekDM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileDescriptor;

import android.content.Context;
import android.util.Log;

import com.mediatek.MediatekDM.mdm.PLFile;
import com.mediatek.MediatekDM.mdm.PLStorage.AccessMode;

import com.mediatek.MediatekDM.DmConst.TAG;

import android.util.Log;

public class DmPLDeltaFile implements PLFile {

    public DmPLDeltaFile(String fileName, Context context, AccessMode accessMode) throws IOException {
        deltaFileContext = context;
        deltaFileName = fileName;
        tempFileName = "[DmPLDeltaFile] temp_" + deltaFileName;
        Log.i(TAG.PL, "[DmPLDeltaFile] fileName = " + deltaFileName + ", tempFileName = " + tempFileName);
        mAccessMode = accessMode;
        switch (mAccessMode) {
        case READ:
            mInputStream = deltaFileContext.openFileInput(deltaFileName);
            break;
        case WRITE:
            mOutputStream = deltaFileContext.openFileOutput(tempFileName, Context.MODE_WORLD_READABLE);
            break;
        }
    }

    public void close(boolean commitClose) throws IOException {
        //if commitClose is false means close the read stream, else close the write stream
        if (commitClose == false && mAccessMode == AccessMode.READ) {
            mInputStream.close();
        }
        Log.i(TAG.PL, "[DmPLDeltaFile] commitClose =  " + commitClose + "mAccessMode = " + mAccessMode);
        //if (commitClose == true)
        if (commitClose == true && mAccessMode == AccessMode.WRITE)
        {
            mOutputStream.close();
            File deltaSrc = deltaFileContext.getFileStreamPath(tempFileName);
            File deltaDst = deltaFileContext.getFileStreamPath(deltaFileName);
            Log.i(TAG.PL, "[DmPLDeltaFile] before rename");
            boolean isOk = deltaSrc.renameTo(deltaDst);
            if (!isOk) {
                Log.e(TAG.PL, "[DmPLDeltaFile] Could not rename " + deltaSrc.getName() + " to " + deltaDst.getName() + "!!!" );
            }
            if (deltaSrc.exists()) {
                deltaDst.delete();
            }
        }
        return;
    }

    public int read(byte[] buffer) throws IOException {
        int ret = 0;
        //only input stream can read
        if (mAccessMode == AccessMode.WRITE) {
            throw new IOException("[DmPLDeltaFile] Attempt to read from an output stream");
        }
        try {
            ret = mInputStream.read(buffer);
        } catch (IOException e) {
            Log.e(TAG.PL, "[DmPLDeltaFile] Read delta file error!");
        }
        return ret;
    }

    public void write(byte[] data) throws IOException {
        //only output stream can write
        if (mAccessMode == AccessMode.READ) {
            throw new IOException("[DmPLDeltaFile] Attempt to write to an input stream");
        }
        try {
            Log.i(TAG.PL, "[DmPLDeltaFile] The data write to delta.zip  is ["+data+"]");
            mOutputStream.write(data);
            mOutputStream.flush();
            FileDescriptor fd = mOutputStream.getFD();
            fd.sync();
        } catch (IOException e) {
            Log.e(TAG.PL, "[DmPLDeltaFile] Read delta file error!");
        }
        return;
    }

    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;
    private Context deltaFileContext;
    private AccessMode mAccessMode;
    private String tempFileName = null;
    private String deltaFileName = null;
//	private String TAG = "DmPLDeletaFile";
//	private String TAGS = "DmService:DmPLDeletaFile";
}
