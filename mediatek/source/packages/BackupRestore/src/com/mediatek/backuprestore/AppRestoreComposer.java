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
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
//import android.content.pm.PackageInstallObserver;
//import com.android.commands.pm.PackageInstallObserver;
//import com.android.packageinstaller.InstallAppProgress.PackageInstallObserver;
import java.io.IOException;

public class AppRestoreComposer extends Composer {
    private static final String APPTAG = "App:";
    private int mIdx;
    private ArrayList<String> mFileNameList;

    private Object mLock = new Object();

    public AppRestoreComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_APP;
	}


    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }

        Log.d(LogTag.RESTORE, APPTAG + "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        mFileNameList = new ArrayList<String>();
        try {
            mFileNameList = (ArrayList<String>)BackupZip.GetFileList(mZipFileName, true, true, "apps/.*\\.apk");
            result = true;
        } catch (IOException e) {
        }

        Log.d(LogTag.RESTORE, APPTAG + "init():" + result + ",count:" + mFileNameList.size());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIdx >= mFileNameList.size()) ? true : false;
        }

        Log.d(LogTag.RESTORE, APPTAG + "isAfterLast():" + result);
        return result;
    }


    public boolean implementComposeOneEntity() {
        boolean result = false;

        String apkFileName = mFileNameList.get(mIdx++);
        String destFileName = BackupRestoreUtils.getStoragePath() + 
             "/" + "temp" +
            apkFileName.subSequence(apkFileName.lastIndexOf("/"), apkFileName.length()).toString();

        Log.d(LogTag.RESTORE,
              APPTAG + "restoreOneMms(),mZipFileName:" + mZipFileName +
              "\napkFileName:" + apkFileName +
              "\ndestFileName:" + destFileName);

        try {
            BackupZip.unZipFile(mZipFileName, apkFileName, destFileName);

            File apkFile = new File(destFileName);
            if (apkFile != null && apkFile.exists()) {
                PackageManager pkgManager = mContext.getPackageManager();
                PackageInstallObserver obs = new PackageInstallObserver();

                pkgManager.installPackage(Uri.fromFile(apkFile),
                                          obs,
                                          PackageManager.INSTALL_REPLACE_EXISTING,
                                          "test");

                synchronized (mLock) {
                    while (!obs.finished) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (obs.result == PackageManager.INSTALL_SUCCEEDED) {
                        result = true;
                        Log.d(LogTag.RESTORE, APPTAG + "install success");
                    } else {
                        Log.d(LogTag.RESTORE, APPTAG + "install fail, result:" + obs.result);
                    }
                }

                apkFile.delete();
            } else {
                Log.d(LogTag.RESTORE, APPTAG + "install failed");
            }
        } catch (IOException e) {
            if (super.mReporter != null) {
                super.mReporter.onErr(e);
            }
            Log.d(LogTag.RESTORE, APPTAG + "unzipfile failed");
        } catch (Exception e) {
        }

        return result;
    }

    private void delteTempFolder() {
        String folder = BackupRestoreUtils.getStoragePath() + "/temp";
        File file = new File(folder);
        if (file.exists() && file.isDirectory()) {
            File files[] = file.listFiles();
            for (File f : files) {
                if (f.getName().matches(".*\\.apk")) {
                    f.delete();
                }
            }

            file.delete();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        delteTempFolder();
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }
        delteTempFolder();
        Log.d(LogTag.RESTORE, APPTAG + " onEnd()");
    }

    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        boolean finished = false;
        int result;

        public void packageInstalled(String name, int status) {
            synchronized(mLock) {
                finished = true;
                result = status;
                mLock.notifyAll();
            }
        }
    }

}
