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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class AppBackupComposer extends Composer {
    private static final String APPTAG = "APP:";
    private List<ApplicationInfo> mAppList = null;
    private int appIdx;

    public AppBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_APP;
	}

    @Override
    public int getCount() {
        int count = 0;
        if (mAppList != null && mAppList.size() > 0) {
            count = mAppList.size();
        }

        Log.d(LogTag.BACKUP, APPTAG + "getCount():" + count);
        return count;
    }


    @Override
    public boolean init() {
        boolean result = false;
        mAppList = getUserApp(mContext);
        if (mAppList != null) {
            result =  true;
            appIdx = 0;
        }

        Log.d(LogTag.BACKUP, APPTAG + "init():" + result);
        return result;
    }

    public boolean isAfterLast() {
        boolean result = true;
        if (mAppList != null && appIdx < mAppList.size()) {
            result = false;
        }

        Log.d(LogTag.BACKUP, APPTAG + "isAfterLast():" + result);
        return result;
    }


    @Override
    public boolean implementComposeOneEntity() {
        boolean result = false;
        if (mAppList != null && appIdx < mAppList.size()) {
            ApplicationInfo app = mAppList.get(appIdx);
            String appName = app.publicSourceDir;
            String appFileName = app.packageName + ".apk";
            appFileName = "apps/" + appFileName;

            CharSequence tmpLable = "";
            if (app.uid == -1) {
                tmpLable = getApkFileLable(mContext, app.sourceDir, app);
            } else {
                tmpLable = app.loadLabel(mContext.getPackageManager());
            }
            String lable = (tmpLable == null) ? app.packageName : tmpLable.toString();

            Log.d(LogTag.BACKUP, APPTAG + appIdx + ":" + appName +
                  ",pacageName:" + app.packageName +
                  ",sourceDir:" + app.sourceDir +
                  ",dataDir:" + app.dataDir +
                  ",lable:" + lable +
                  ",mZipHandler:" + mZipHandler.getZipFileName());

            try {
                mZipHandler.addFileByFileName(appName, appFileName);
                mZipHandler.addFolder(app.dataDir,//"/mnt/sdcard/DCIM",
                                       "apps" + File.separator + app.dataDir.subSequence(app.dataDir.lastIndexOf("/") + 1,
                                                                                         app.dataDir.length())
                                       .toString());
                
                Log.d(LogTag.BACKUP, APPTAG + "addFile " + appName + "success");
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                Log.d(LogTag.BACKUP, APPTAG + "addFile:" + appName + "fail");
            }

            ++appIdx;
            result = true;
        }

        return result;
    }


    public static List<ApplicationInfo> getUserApp(Context context) {
        List<ApplicationInfo> userApp = null;
        if (context != null) {
            PackageManager pkgMannage = context.getPackageManager();

            List<ApplicationInfo> appList = pkgMannage.getInstalledApplications(0);
            userApp = new ArrayList<ApplicationInfo>();
            for (ApplicationInfo app : appList) {
                if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                    && !app.packageName.equalsIgnoreCase(context.getPackageName())) {
                    userApp.add(app);
                }
            }
        }

        return userApp;
    }



    public CharSequence getApkFileLable(Context context, String apkPath, ApplicationInfo appInfo) {
        if (context == null || appInfo == null || apkPath == null || !(new File(apkPath).exists())) {
            return null;
        }

        Resources pRes = mContext.getResources();

        AssetManager assmgr = new AssetManager();
        assmgr.addAssetPath(apkPath);

        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());

        CharSequence label = null;
        if (appInfo.labelRes != 0) {
            label = (String)res.getText(appInfo.labelRes);
        }

        return label;
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mAppList != null) {
            mAppList.clear();
        }

        Log.d(LogTag.RESTORE, APPTAG + " onEnd()");
    }
}