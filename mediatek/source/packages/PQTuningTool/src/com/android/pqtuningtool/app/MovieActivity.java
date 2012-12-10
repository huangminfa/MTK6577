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

/*
 * Copyright (C) 2007 The Android Open Source Project
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

package  com.android.pqtuningtool.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;

import android.app.NotificationManagerPlus;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.ui.Log;
import  com.android.pqtuningtool.util.MtkLog;
import  com.android.pqtuningtool.util.MtkUtils;

/**
 * This activity plays a video from a specified URI.
 */
public class MovieActivity extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";

    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    private Uri mUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.root);
        Intent intent = getIntent();
        initUri(intent);
        initializeActionBar(intent);
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        
        mPlayer = new MoviePlayer(rootView, this, mMovieInfo, savedInstanceState,
                !mFinishOnCompletion) {
            @Override
            public void onCompletion() {
                if (LOG) MtkLog.v(TAG, "onCompletion() mFinishOnCompletion=" + mFinishOnCompletion);
                if (mFinishOnCompletion) {
                    finish();
                }
            }
        };
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);
        
        enableNMP();
    }

    private void initializeActionBar(Intent intent) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title == null) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(mOriginalUri,
                        new String[] {VideoColumns.TITLE}, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    title = cursor.getString(0);
                }
            } catch (Throwable t) {
                Log.w(TAG, "cannot get title from: " + intent.getDataString(), t);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (title == null) {
            title = enhanceTitle();
        }
        if (title != null) actionBar.setTitle(title);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        boolean local = MtkUtils.isLocalFile(mOriginalUri, mMimeType);
        if (local && !MtkUtils.canShare(this, mOriginalUri)) {
            //do not show share
        } else {
            getMenuInflater().inflate(R.menu.movie, menu);
            ShareActionProvider provider = GalleryActionBar.initializeShareActionProvider(menu);

            if (provider != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                if (local) {
                    intent.setType("video/*");
                    intent.putExtra(Intent.EXTRA_STREAM, mOriginalUri);
                } else {
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(mOriginalUri));
                }
                provider.setShareIntent(intent);
            }
        }
        handleCreateMenu(menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        handlePrepareMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (handleSelectedMenu(item)) {
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();
        registerScreenOff();
        startListening();
        enableStereo();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();
        if (mControlResumed) {
            mPlayer.onStop();
            mControlResumed = false;
        }
        unregisterScreenOff();
        stopListening();
        restoreStereo();
        if (LOG) MtkLog.v(TAG, "onStop() mFocused=" + mFocused
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
    }

    @Override
    public void onPause() {
        if (LOG) MtkLog.v(TAG, "onPause() mFocused=" + mFocused
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        mResumed = false;
        if (mControlResumed) {
            mControlResumed = !mPlayer.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (LOG) MtkLog.v(TAG, "onResume() mFocused=" + mFocused
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        mResumed = true;
        if (mFocused && mResumed && !mControlResumed) {
            mPlayer.onResume();
            mControlResumed = true;
        }
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (LOG) MtkLog.v(TAG, "onWindowFocusChanged(" + hasFocus + ") mFocused=" + mFocused
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        mFocused = hasFocus;
        if (mFocused && mResumed && !mControlResumed) {
            mPlayer.onResume();
            mControlResumed = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPlayer.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mPlayer.onDestroy();
        super.onDestroy();
        clearNotifications();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mPlayer.onKeyUp(keyCode, event)
                || super.onKeyUp(keyCode, event);
    }

    private static final boolean LOG = true;
    private boolean mResumed = false;
    private boolean mFocused = false;
    private boolean mControlResumed = false;
    
    //for sdp over http
    private static final String VIDEO_SDP_MIME_TYPE = "application/sdp";
    private static final String VIDEO_SDP_TITLE = "rtsp://";
    private static final String VIDEO_FILE_SCHEMA = "file";
    private static final String VIDEO_MIME_TYPE = "video/*";
    private String mMimeType;
    private Uri mOriginalUri;
    private MovieInfo mMovieInfo = new MovieInfo();
    
    private void initUri(Intent intent) {
        mOriginalUri = intent.getData();
        String mimeType = intent.getType();
        mMovieInfo = new MovieInfo();
        if (VIDEO_SDP_MIME_TYPE.equalsIgnoreCase(mimeType)
                && VIDEO_FILE_SCHEMA.equalsIgnoreCase(mOriginalUri.getScheme())) {
            mUri = Uri.parse(VIDEO_SDP_TITLE + mOriginalUri);
            mMimeType = mimeType;
            mMovieInfo.setUri(mUri, mMimeType);
        } else {
            mUri = mOriginalUri;
            mMimeType = mimeType;
            mMovieInfo.setUri(mUri, mMimeType);
            enableList();
        }
        if (LOG) MtkLog.v(TAG, "initUri(" + mOriginalUri + ") mUri=" + mUri + ", mMimeType=" + mMimeType);
    }
    
    //for cmcc streaming notifications
    private NotificationManagerPlus mPlusNotification;
    private static final String MMS_AUTHORITY = "mms";
    
    private void enableNMP() {
        if ((MtkUtils.isCmccOperator() && MMS_AUTHORITY.equalsIgnoreCase(mUri.getAuthority())) ||
                (MtkUtils.isCuOperator() && !MtkUtils.isLocalFile(mUri, mMimeType))) {
            mPlusNotification = new NotificationManagerPlus.ManagerBuilder(this)
                    .setPositiveButton(getString(android.R.string.ok), null)
                    .setNeutralButton(null, null)
                    .setNegativeButton(null, null)
                    .setOnFirstShowListener(mPlayer)
                    .setOnLastDismissListener(mPlayer)
                    .create();
        }
        if (LOG) MtkLog.v(TAG, "enableNMP() mFinishOnCompletion=" + mFinishOnCompletion);
    }
    
    private void startListening() {
        if (mPlusNotification != null) {
            mPlusNotification.startListening();
        }
        if (LOG) MtkLog.v(TAG, "startListening() mPlusNotification=" + mPlusNotification);
    }
    
    private void stopListening() {
        if (mPlusNotification != null) {
            mPlusNotification.stopListening();
        }
        if (LOG) MtkLog.v(TAG, "stopListening() mPlusNotification=" + mPlusNotification);
    }
    
    private void clearNotifications() {
        if (mPlusNotification != null) {
            mPlusNotification.clearAll();
        }
        if (LOG) MtkLog.v(TAG, "clearNotifications() mPlusNotification=" + mPlusNotification);
    }
    
    //for live streaming.
    //we do not stop live streaming when other dialog overlays it.
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOG) MtkLog.v(TAG, "onReceive(" + intent.getAction() + ") mControlResumed=" + mControlResumed);
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                //Only stop video.
                if (mControlResumed) {
                    mPlayer.onStop();
                    mControlResumed = false;
                }
            }
        }
        
    };
    
    private void registerScreenOff() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffReceiver, filter);
    }
    
    private void unregisterScreenOff() {
        unregisterReceiver(mScreenOffReceiver);
    }
    
    //for cmcc input uri and setting
    private static final int MENU_INPUT_URL = 1;
    private static final int MENU_SETTINGS = 2;
    private static final int MENU_STOP = 3;
    private static final int MENU_LOOP = 4;
    private static final int MENU_LOOP_ICON = 5;
    private static final int MENU_OUTPUT = 6;
    private static final int MENU_DETAIL = 8;
    private static final int MENU_BOOKMARK_ADD = 9;
    private static final int MENU_BOOKMARK_DISPLAY = 10;
    private static final int MENU_NEXT = 11;
    private static final int MENU_PREVIOUS = 12;
    private MenuItem mLoopIcon;
    private MenuItem mLoopButton;
    private MenuItem mStereoMenu;
    private MenuItem mBookmarks;
    private MenuItem mBookmarkAdd;
    private MenuItem mDetail;
    private MenuItem mNext;
    private MenuItem mPrevious;
    
    private void gotoInputUrl() {
        String appName = getClass().getName();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("about:blank"));
        intent.putExtra("inputUrl", true);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, appName);
        startActivity(intent);
        if (LOG) MtkLog.v(TAG, "gotoInputUrl() appName=" + appName);
    }
    
    private void gotoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
                | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        startActivity(intent);
        if (LOG) MtkLog.v(TAG, "gotoInputUrl()");
    }
    
    private void gotoBookmark() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
                | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        startActivity(intent);
    }
    
    private boolean handleCreateMenu(Menu menu) {
      //when in rtsp streaming type, generally it only has one uri.
        if (MtkUtils.isCmccOperator()) {
            mLoopIcon = menu.add(0, MENU_LOOP_ICON, 0, "")
                .setIcon(R.drawable.ic_menu_loop);
            mLoopIcon.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            mPrevious = menu.add(0, MENU_PREVIOUS, 0, R.string.previous);
            mNext = menu.add(0, MENU_NEXT, 0, R.string.next);
            mLoopButton = menu.add(0, MENU_LOOP, 0, R.string.loop);
            menu.add(0, MENU_STOP, 0, R.string.stop);
            mStereoMenu = menu.add(0, MENU_OUTPUT, 0, R.string.single_track);
            mBookmarkAdd = menu.add(0, MENU_BOOKMARK_ADD, 0, R.string.bookmark_add);
            mBookmarks = menu.add(0, MENU_BOOKMARK_DISPLAY, 0, R.string.bookmark_display);
            mDetail = menu.add(0, MENU_DETAIL, 0, R.string.media_detail);
            menu.add(0, MENU_INPUT_URL, 0, R.string.input_url);
            menu.add(0, MENU_SETTINGS, 0, R.string.streaming_settings);
            
            return true;
        }
        return false;
    }
    
    private boolean handleSelectedMenu(MenuItem item) {
        if (LOG) MtkLog.v(TAG, "handleCmccMenu(" + item + ")");
        if (item == null) return false;
        switch(item.getItemId()) {
        case MENU_INPUT_URL:
            gotoInputUrl();
            return true;
        case MENU_SETTINGS:
            gotoSettings();
            return true;
        case MENU_STOP:
            mPlayer.stopVideo();
            return true;
        case MENU_LOOP:
            mPlayer.setLoop(!mPlayer.getLoop());
            updateLoop();
            return true;
        case MENU_LOOP_ICON:
            mPlayer.setLoop(!mPlayer.getLoop());
            updateLoop();
            return true;
        case MENU_BOOKMARK_ADD:
            mPlayer.addBookmark(mMimeType);
            return true;
        case MENU_BOOKMARK_DISPLAY:
            gotoBookmark();
            return true;
        case MENU_DETAIL:
            mPlayer.showDetail();
            return true;
        case MENU_OUTPUT:
            setStereo(!mCurrentStereo);
            return true;
        case MENU_PREVIOUS:
            mPlayer.startPreviousVideo();
            return true;
        case MENU_NEXT:
            mPlayer.startNextVideo();
            return true;
        default:
            return false;
        }
    }
    
    private void updateLoop() {
        if (LOG) MtkLog.v(TAG, "updateLoop() mLoopIcon=" + mLoopIcon);
        if (mLoopIcon != null && mLoopButton != null) {
            if (MtkUtils.isLocalFile(mMovieInfo.getCurrentUri(), mMovieInfo.getCurrentMimeType())) {
                mLoopIcon.setVisible(true);
                mLoopButton.setVisible(true);
            } else {
                mLoopIcon.setVisible(false);
                mLoopButton.setVisible(false);
            }
            boolean newLoop = mPlayer.getLoop();
            if (newLoop) {
                mLoopButton.setTitle(R.string.single);
                mLoopButton.setIcon(R.drawable.ic_menu_unloop);
                mLoopIcon.setIcon(R.drawable.ic_menu_loop);
            } else {
                mLoopButton.setTitle(R.string.loop);
                mLoopButton.setIcon(R.drawable.ic_menu_loop);
                mLoopIcon.setIcon(R.drawable.ic_menu_unloop);
            }
        }
    }

    private boolean handlePrepareMenu(Menu menu) {
        if (MtkUtils.isLocalFile(mMovieInfo.getCurrentUri(), mMovieInfo.getCurrentMimeType())) {
            if (mBookmarkAdd != null) {
                mBookmarkAdd.setVisible(false);
            }
            if (mBookmarks != null) {
                mBookmarks.setVisible(false);
            }
            if (mDetail != null) {
                mDetail.setVisible(false);
            }
        } else {
            if (mBookmarkAdd != null) {
                mBookmarkAdd.setVisible(true);
            }
            if (mBookmarks != null) {
                mBookmarks.setVisible(true);
            }
            if (mDetail != null) {
                mDetail.setVisible(true);
            }
        }
        updatePrevNext();
        updateLoop();
        mCurrentStereo = getStereo();
        updateStereoIcon();
        return true;
    }
    
    private void updatePrevNext() {
        if (LOG) MtkLog.v(TAG, "updatePrevNext() mPlayer.getLoop()=" + mPlayer.getLoop()
                + ", mMovieInfo.size()=" + mMovieInfo.size());
        if (mPrevious != null && mNext != null) {
            if (mMovieInfo.size() > 1 || mPlayer.getLoop()) {
                mNext.setVisible(true);
                mPrevious.setVisible(true);
            } else {
                mNext.setVisible(false);
                mPrevious.setVisible(false);
            }
            if (!mPlayer.getLoop()) {
                if (mMovieInfo.isFirst()) {
                    mPrevious.setEnabled(false);
                } else {
                    mPrevious.setEnabled(true);
                }
                if (mMovieInfo.isLast()) {
                    mNext.setEnabled(false);
                } else {
                    mNext.setEnabled(true);
                }
            } else {
                mPrevious.setEnabled(true);
                mNext.setEnabled(true);
            }
        }
    }
    
    //enhance the title feature
    private String enhanceTitle() {
        String title = null;
        if ("content".equals(mUri.getScheme())) {
            title = getTitleFromDisplayName();
        }
        if (title == null && "content".equals(mUri.getScheme())) {
            title = getTitleFromData();
        }
        if (title == null) {
            title = getTitleFromUri();
        }
        if (LOG) MtkLog.v(TAG, "getVideoTitle() return " + title);
        return title;
    }
    
    private String getTitleFromDisplayName() {
        String title = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mUri,
                    new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                title = cursor.getString(0);
           }
        } catch(Exception ex) {
            Log.e(TAG, "Cannot get video title from _display_name. " + mUri, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) Log.v(TAG, "getTitleFromDisplayName() title=" + title);
        return title;
    }
    
    private String getTitleFromUri() {
        String title = null;
        title = Uri.decode(mUri.getLastPathSegment());
        if (LOG) MtkLog.v(TAG, "getTitleFromUri() uri=" + mUri + ", title=" + title);
        return title;
    }
    
    private String getTitleFromData() {
        String title = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mUri,
                    new String[]{"_data"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(cursor.getString(0));
                title = file.getName();
           }
        } catch(Exception ex) {
            MtkLog.e(TAG, "Cannot get video title from data. " + mUri, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "getTitleFromData() title=" + title);
        return title;
    }
    
    private static final String KEY_STEREO = "EnableStereoOutput";
    private boolean mOriginalStereo;
    private boolean mCurrentStereo;
    private AudioManager mAudioManager;
    private boolean getStereo() {
        if (mStereoMenu == null) {
            return true;
        }
        boolean isstereo = false;
        if (mAudioManager == null) {
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        }
        String stereo = mAudioManager.getParameters(KEY_STEREO);
        String key = KEY_STEREO + "=1";
        if (stereo != null && stereo.indexOf(key) > -1) {
            isstereo = true;
        } else {
            isstereo = false;
        }
        if (LOG) Log.v(TAG, "getStereo() isstereo=" + isstereo + ", stereo=" + stereo + ", key=" + key);
        return isstereo;
    }
    
    private void setStereo(boolean flag) {
        String value = KEY_STEREO + "=" + (flag ? "1" : "0");
        if (mAudioManager == null) {
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.setParameters(value);
        if (LOG) Log.v(TAG, "setStereo(" + flag + ") value=" + value);
    }
    
    private void updateStereoIcon() {
        if (mStereoMenu != null) {
            if (mCurrentStereo) {
                mStereoMenu.setTitle(R.string.single_track);
            } else {
                mStereoMenu.setTitle(R.string.stereo);
            }
        }
    }
    
    private void enableStereo() {
        if (MtkUtils.isCmccOperator()) {
            mOriginalStereo = getStereo();
            mCurrentStereo = mOriginalStereo;
            updateStereoIcon();
        }
    }
    
    private void restoreStereo() {
        if (MtkUtils.isCmccOperator()) {
            setStereo(mOriginalStereo);
        }
    }
    
    private void enableList() {
        if (MtkUtils.isCmccOperator()) {
            mListTask = new VideoListTask();
            mListTask.execute(getIntent());
        }
    }
    
    class MovieInfo {
        boolean[] errors;
        private int mIndex;
        private int mSize;
        private final ArrayList<Uri> mUris = new ArrayList<Uri>();
        private ArrayList<String> mMimeTypes = new ArrayList<String>();
        
        boolean setUri(Uri uri, String mimeType) {
            if (uri == null) {
                return false;
            }
            mUris.clear();
            mIndex = 0;
            mSize = 1;
            mUris.add(uri);
            mMimeTypes.clear();
            mMimeTypes.add(mimeType);
            errors = new boolean[mSize];
            if (LOG) Log.i(TAG, "setUri(" + uri + ", " + mimeType + ") uri=" + mUris.get(0));
            return true;
        }
        boolean setMimeType(ArrayList<String> mimeTypes) {
            mMimeTypes.clear();
            if (mimeTypes == null || mimeTypes.size() < 1) {
                return false;
            }
//            int size = mimeTypes.size();
//            for(int i = 0; i < size; i++) {
            mMimeTypes = mimeTypes;
//            }
            if (LOG) Log.i(TAG, "setMimeType() mMimeTypes.size=" + mMimeTypes.size());
            return true;
        }
        
        boolean setUri(ArrayList<String> uris) {
            if (uris == null || uris.size() < 1) {
                return false;
            }
            int size = uris.size();
            mUris.clear();
            mIndex = 0;
            mSize = size;
            for(int i = 0; i < size; i++) {
                mUris.add(Uri.parse(uris.get(i)));
            }
            errors = new boolean[mSize];
            return true;
        }
        
        //If only one uri, it alway returns that one.
        Uri getCurrentUri() {
            Uri uri = mUris.get(mIndex);
            if (LOG) Log.i(TAG, "getCurrentUri() mIndex=" + mIndex + ", uri=" + uri);
            return uri;
        }
        
        String getCurrentMimeType() {
            String mime = mMimeTypes.get(mIndex);
            if (LOG) Log.i(TAG, "getCurrentMimeType() mIndex=" + mIndex + ", mime=" + mime);
            return mime;
        }
        
        void moveToNext() {
            mIndex++;
            if (mIndex >= mSize) {
                mIndex -= mSize;
            }
            if (LOG) Log.i(TAG, "moveToNext() end mIndex=" + mIndex + ", mSize=" + mSize);
        }
        
        void moveToPrevious() {
            mIndex--;
            if (mIndex < 0) {
                mIndex += mSize;
            }
            if (LOG) Log.i(TAG, "moveToPrevious() end mIndex=" + mIndex + ", mSize=" + mSize);
        }
        
        void moveTo(int position) {
            mIndex = position;
            if (mIndex >= mSize) {
                mIndex -= mSize;
            }
            if (mIndex < 0) {
                mIndex += mSize;
            }
            if (LOG) Log.i(TAG, "moveTo(" + position + ") end mIndex=" + mIndex + ", mSize=" + mSize);
        }
        
        int index() {
            return mIndex;
        }
        
        int size() {
            return mSize;
        }
        
        boolean isLast() {
            return (mIndex == (mSize - 1));
        }
        
        boolean isFirst() {
            return (mIndex == 0);
        }
        
        void clearErrors() {
            for(int i = 0; i < mSize; i++) {
                errors[i] = false;
            }
        }
        
        boolean isAllError() {
            boolean all = true;
            for(int i = 0; i < mSize; i++) {
                if (LOG) Log.i(TAG, "errors[" + i + "]=" + errors[i]);
                if (errors[i] == false) {
                    all = false;
                    break;
                }
            }
            return all;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mSize; i++) {
                sb.append(", uri[" + i + "]=" + String.valueOf(mUris.get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        
    }
    
    private static final String EXTRA_ALL_VIDEO_FOLDER = "EXTRA_ALL_VIDEO_FOLDER";
    private VideoListTask mListTask;
    private class VideoListTask extends AsyncTask<Intent, Void, Integer> {
        @Override
        protected void onPostExecute(Integer result) {
            if (LOG) Log.v(TAG, "onPostExecute() isCancelled()=" + isCancelled());
            if (isCancelled()) return;
            updatePrevNext();
        }
        
        @Override
        protected Integer doInBackground(Intent... params) {
            Intent intent = getIntent();
            boolean allVideoFolder = false;
            if (intent.hasExtra(EXTRA_ALL_VIDEO_FOLDER)) {
                allVideoFolder = intent.getBooleanExtra(EXTRA_ALL_VIDEO_FOLDER, false);
            } else {
                if (LOG) Log.v(TAG, "doInBackground() no all video folder.");
            }
            if (LOG) Log.v(TAG, "doInBackground() allVideoFolder=" + allVideoFolder);
            Uri uri = intent.getData();
            String mime = intent.getType();
            if (allVideoFolder) {//get all list
                if (MtkUtils.isLocalFile(uri, mime)) {
                    String uristr = String.valueOf(uri);
                    if (uristr.toLowerCase().startsWith("content://media")) {
                        //from gallery, gallery3D
                        long curId = Long.parseLong(uri.getPathSegments().get(3));
                        fillUriList(null, null, curId);
                    } else if (uristr.toLowerCase().startsWith("file://")) {
                        //will not occur
                    }
                } else {
                    //do nothing
                }
            } else {//get current list
                if (MtkUtils.isLocalFile(uri, mime)) {
                    String uristr = String.valueOf(uri);
                    if (uristr.toLowerCase().startsWith("content://media")) {
                        Cursor cursor = getContentResolver().query(uri,
                                new String[]{MediaStore.Video.Media.BUCKET_ID},
                                null, null, null);
                        long bucketId = -1;
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                bucketId = cursor.getLong(0);
                            }
                            cursor.close();
                        }
                        long curId = Long.parseLong(uri.getPathSegments().get(3));
                        fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                                new String[]{String.valueOf(bucketId)},
                                curId);
                    } else if (uristr.toLowerCase().startsWith("file://")) {
                        String data = Uri.decode(uri.toString());
                        String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";
                        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                new String[]{"_id", MediaStore.Video.Media.BUCKET_ID},
                                where, null, null);
                        long bucketId = -1;
                        long curId = -1;
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                curId = cursor.getLong(0);
                                bucketId = cursor.getLong(1);
                            }
                            cursor.close();
                        }
                        fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                                new String[]{String.valueOf(bucketId)},
                                curId);
                    }
                } else {
                    //do nothing
                }
            }
            if (LOG) Log.v(TAG, "doInBackground() done");
            return 1;
        }

        private void fillUriList(String where, String[] whereArgs, long curId) {
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{"_id", "_data", "mime_type"},
                    where,
                    whereArgs,
                    MediaStore.Video.Media.DATE_TAKEN + " DESC, " + MediaStore.Video.Media._ID + " DESC ");
            int curPosition = 0;
            boolean find = false;
            if (cursor != null) {
                ArrayList<String> list = new ArrayList<String>();
                ArrayList<String> types = new ArrayList<String>();
                while(cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String data = cursor.getString(1);
                    String type = cursor.getString(2);
                    if (!find && id == curId) {
                        find = true;
                        curPosition = list.size();
                        type = mMimeType;
                    }
                    list.add(data);
                    types.add(type);
                }
                mMovieInfo.setUri(list);
                mMovieInfo.setMimeType(types);
                mMovieInfo.moveTo(curPosition);
                cursor.close();
            }
        }
    }
}
