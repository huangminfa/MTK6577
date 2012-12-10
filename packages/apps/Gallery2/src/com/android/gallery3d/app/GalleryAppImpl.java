/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.gadget.WidgetUtils;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.util.CacheManager;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.MtkUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.File;

public class GalleryAppImpl extends Application implements GalleryApp {

    private static final String TAG = "GalleryAppImpl";
    private static final String DOWNLOAD_FOLDER = "download";
    private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

    private ImageCacheService mImageCacheService;
    private DataManager mDataManager;
    private ThreadPool mThreadPool;
    private DownloadCache mDownloadCache;
    
    // for closing cache when SD card got unmounted
    private BroadcastReceiver mStorageReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        GalleryUtils.initialize(this);
        WidgetUtils.initialize(this);
        PicasaSource.initialize(this);

        com.android.gallery3d.util.MediatekFeature.initialize(this);
        
        registerStorageReceiver();
    }

    public Context getAndroidContext() {
        return this;
    }

    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    public synchronized ImageCacheService getImageCacheService() {
        if (mImageCacheService == null) {
            mImageCacheService = new ImageCacheService(getAndroidContext());
        }
        return mImageCacheService;
    }

    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    public synchronized DownloadCache getDownloadCache() {
        if (mDownloadCache == null) {
            //File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);
            File cacheDir = new File(MtkUtils.getMTKExternalCacheDir(this), DOWNLOAD_FOLDER);

            if (!cacheDir.isDirectory()) cacheDir.mkdirs();

            if (!cacheDir.isDirectory()) {
                throw new RuntimeException(
                        "fail to create: " + cacheDir.getAbsolutePath());
            }
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
        }
        return mDownloadCache;
    }
    
    // for closing/re-opening cache
    private void registerStorageReceiver() {
        MtkLog.d(TAG, ">> registerStorageReceiver");
        // register BroadcastReceiver for SD card mount/unmount broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        mStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleStorageIntentAsync(intent);
            }
        };
        registerReceiver(mStorageReceiver, filter);
        MtkLog.d(TAG, "<< registerStorageReceiver: receiver registered");
    }
    
    private void handleStorageIntentAsync(final Intent intent) {
        new Thread() {
            public void run() {
                String action = intent.getAction();
                String storagePath = intent.getData().getPath();
                String defaultPath = MtkUtils.getMtkDefaultPath();
                MtkLog.d(TAG, "storage receiver: action=" + action);
                MtkLog.d(TAG, "intent path=" + storagePath + ", default path=" + defaultPath);
                
                if (storagePath == null || !storagePath.equalsIgnoreCase(defaultPath)) {
                    MtkLog.w(TAG, "ejecting storage is not cache storage!!");
                    return;
                }
                if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                    // close and disable cache
                    MtkLog.i(TAG, "-> closing CacheManager");
                    CacheManager.storageStateChanged(false);
                    MtkLog.i(TAG, "<- closing CacheManager");
                    // clear refs in ImageCacheService
                    if (mImageCacheService != null) {
                        MtkLog.i(TAG, "-> closing cache service");
                        mImageCacheService.closeCache();
                        MtkLog.i(TAG, "<- closing cache service");
                    }
                } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    // enable cache but not open it explicitly
                    MtkLog.i(TAG, "-> opening CacheManager");
                    CacheManager.storageStateChanged(true);
                    MtkLog.i(TAG, "<- opening CacheManager");
                    // re-open cache in ImageCacheService
                    if (mImageCacheService != null) {
                        MtkLog.i(TAG, "-> opening cache service");
                        mImageCacheService.openCache();
                        MtkLog.i(TAG, "<- opening cache service");
                    }
                } else {
                    MtkLog.w(TAG, "undesired action '" + action + "' for storage receiver!");
                }
            }
        }.start();
    }
}
