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

package com.android.gallery3d.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.provider.OpenableColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.ActivityChooserView;

import android.app.NotificationManagerPlus;

import com.android.gallery3d.R;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.MtkLog;
import com.android.gallery3d.util.MtkUtils;
import com.android.gallery3d.util.StereoHelper;

import android.content.res.Configuration;
import android.view.SubMenu;
import android.view.ViewGroup;

import com.android.gallery3d.ui.ConvergenceBarManager;
import com.android.gallery3d.ui.StereoVideoLayout;

/**
 * This activity plays a video from a specified URI.
 */
public class MovieActivity extends Activity implements MovieInfoUpdateListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";

    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    //private Uri mUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.root);
        Intent intent = getIntent();
        initMovieInfo(intent);
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

        if (mFeatureSwitcher.isEnabledStereoVideo()) {
            // M: added for stereo image manual convergence tuning
            mConvBarManager = new ConvergenceBarManager(getApplicationContext(),
                                                        (ViewGroup)rootView);
            mConvBarManager.setConvergenceListener(mConvChangeListener);
            // M: added for video layout adjustment
            mVideoLayout = new StereoVideoLayout(getApplicationContext(),
                                                        (ViewGroup)rootView);
            mVideoLayout.setVideoLayoutListener(mVideoLayoutListener);
        }
    }

    private void initializeActionBar(Intent intent) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        Boolean support3D = getSupport3DFromIntent();
        enhanceActionBar(title, support3D);
        setActionBarTitle(mMovieInfo.getTitle());
        if (support3D != null) mMovieInfo.setSupport3D(support3D);
        initial3DIcon(mMovieInfo.getSupport3D());
        if (LOG) MtkLog.v(TAG, "initializeActionBar() mMovieInfo=" + mMovieInfo);
    }
    
    public void setActionBarTitle(String title) {
        if (LOG) MtkLog.v(TAG, "setActionBarTitle(" + title + ")");
        ActionBar actionBar = getActionBar();
        if (title != null) actionBar.setTitle(title);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        boolean local = MtkUtils.isLocalFile(mOriginalUri, mMovieInfo.getMimeType());
        if (!MtkUtils.canShare(getIntent().getExtras()) || (local && !MtkUtils.canShare(this, mOriginalUri))) {
            //do not show share
        } else {
            getMenuInflater().inflate(R.menu.movie, menu);
            mShareMenu = menu.findItem(R.id.action_share);
            ShareActionProvider provider = GalleryActionBar.initializeShareActionProvider(menu);
            mShareProvider = provider;
            /// M: share provider is singleton, we should refresh our history file.
            mShareProvider.setShareHistoryFileName(SHARE_HISTORY_FILE);
            refreshShareProvider(mMovieInfo);
        }
        handleCreateMenu(menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mInConvergenceTuningMode || mInVideoLayoutMode) {
            //if we are tuning depth or adjust video layout,
            //do not allow options menu
            return false;
        }

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
        enableStereoAudio();
        if (LOG) MtkLog.v(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();
        if (mControlResumed && mPlayer != null) {
            mPlayer.onStop();
            mControlResumed = false;
        }
        unregisterScreenOff();
        stopListening();
        restoreStereoAudio();
        if (LOG) MtkLog.v(TAG, "onStop() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
    }

    @Override
    public void onPause() {
        if (LOG) MtkLog.v(TAG, "onPause() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        mResumed = false;
        if (mControlResumed && mPlayer != null) {
            mControlResumed = !mPlayer.onPause();
        }
        super.onPause();
        collapseShareMenu();
    }

    @Override
    public void onResume() {
        if (LOG) MtkLog.v(TAG, "onResume() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        mResumed = true;
        if (!isKeyguardLocked() && mResumed && !mControlResumed && mPlayer != null) {
            mPlayer.onResume();
            mControlResumed = true;
        }
        super.onResume();
        
        // M: popup stereo first run hint if necessary
        if (mConvBarManager != null) {
            if (mMovieInfo.getSupport3D()) {
                mConvBarManager.onStereoMediaOpened(false);
            }
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (LOG) MtkLog.v(TAG, "onWindowFocusChanged(" + hasFocus + ") isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        if (hasFocus && !isKeyguardLocked() && mResumed && !mControlResumed && mPlayer != null) {
            mPlayer.onResume();
            mControlResumed = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPlayer != null) {
            mPlayer.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
        if (mMovieInfo != null) {
            mMovieInfo.cancelList();
        }
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
    private boolean mControlResumed = false;
    
    //for sdp over http
    private static final String VIDEO_SDP_MIME_TYPE = "application/sdp";
    private static final String VIDEO_SDP_TITLE = "rtsp://";
    private static final String VIDEO_FILE_SCHEMA = "file";
    private static final String VIDEO_MIME_TYPE = "video/*";
    private Uri mOriginalUri;
    private MovieItem mMovieInfo;
    
    private void initMovieInfo(Intent intent) {
        mFeatureSwitcher = new DefaultMovieFeatureSwitcher();
        mOriginalUri = intent.getData();
        String mimeType = intent.getType();
        if (VIDEO_SDP_MIME_TYPE.equalsIgnoreCase(mimeType)
                && VIDEO_FILE_SCHEMA.equalsIgnoreCase(mOriginalUri.getScheme())) {
            mMovieInfo = new MovieItem(VIDEO_SDP_TITLE + mOriginalUri, mimeType, null, false);
        } else {
            mMovieInfo = new MovieItem(mOriginalUri, mimeType, null, false);
            if (mFeatureSwitcher.isEnabledVideoList()) {
                mMovieInfo.fillVideoList(this, intent, this);
            }
        }
        if (LOG) MtkLog.v(TAG, "initMovieInfo(" + mOriginalUri + ") mMovieInfo=" + mMovieInfo);
    }
    
    //for fullscreen streaming notifications
    private NotificationManagerPlus mPlusNotification;
    private void enableNMP() {
        if (mFeatureSwitcher.isEnabledFullscreenNotification(mOriginalUri, mMovieInfo)) {
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
    
    //for optional menu
    private static final int MENU_INPUT_URL = 1;
    private static final int MENU_SETTINGS = 2;
    private static final int MENU_STOP = 3;
    private static final int MENU_LOOP = 4;
    private static final int MENU_OUTPUT = 5;
    private static final int MENU_DETAIL = 6;
    private static final int MENU_BOOKMARK_ADD = 7;
    private static final int MENU_BOOKMARK_DISPLAY = 8;
    private static final int MENU_NEXT = 9;
    private static final int MENU_PREVIOUS = 10;
    private static final int MENU_3D_ICON = 11;
    private static final int MENU_AC = 12;
    private static final int MENU_MC = 13;
    private static final int MENU_STEREO_LAYOUTS = 14;

    private MenuItem mMenuLoopButton;
    private MenuItem mMenuStereoAudio;
    private MenuItem mMenuBookmarks;
    private MenuItem mMenuBookmarkAdd;
    private MenuItem mMenuDetail;
    private MenuItem mMenuNext;
    private MenuItem mMenuPrevious;
    private MenuItem mMenuStop;
    private MenuItem mMenuStereoVideoIcon;
    private MenuItem mMenuAC;
    private MenuItem mMenuMC;
    private MenuItem mStereoLayouts;

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
        if (mFeatureSwitcher.isEnabledStereoVideo()) {
            mMenuStereoVideoIcon = menu.add(0, MENU_3D_ICON, 0, R.string.stereo3d_mode_switchto_2d);
            mMenuStereoVideoIcon.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            initial3DIcon(mMovieInfo.getSupport3D());
            // M: added for video convergence feature
            mMenuAC = menu.add(0, MENU_AC, 0, 
                               R.string.stereo3d_switch_auto_convergence);
            mMenuAC.setCheckable(true);
            mMenuMC = menu.add(0, MENU_MC, 0, 
                               R.string.stereo3d_convergence_menu);
            mStereoLayouts = menu.add(0, MENU_STEREO_LAYOUTS, 0, 
                                 R.string.stereo3d_video_layout);
        }
        if (mFeatureSwitcher.isEnabledStop()) {
            mMenuStop = menu.add(0, MENU_STOP, 0, R.string.stop);
        }
        if (mFeatureSwitcher.isEnabledVideoList() && mMovieInfo.isEnabledVideoList(getIntent())) {
            mMenuPrevious = menu.add(0, MENU_PREVIOUS, 0, R.string.previous);
            mMenuNext = menu.add(0, MENU_NEXT, 0, R.string.next);
        }
        if (mFeatureSwitcher.isEnabledLoop()) {
            mMenuLoopButton = menu.add(0, MENU_LOOP, 0, R.string.loop);
        }
        if (mFeatureSwitcher.isEnabledStereoAudio()) {
            mMenuStereoAudio = menu.add(0, MENU_OUTPUT, 0, R.string.single_track);
        }
        if (mFeatureSwitcher.isEnabledBookmark()) {
            mMenuBookmarkAdd = menu.add(0, MENU_BOOKMARK_ADD, 0, R.string.bookmark_add);
            mMenuBookmarks = menu.add(0, MENU_BOOKMARK_DISPLAY, 0, R.string.bookmark_display);
        }
        if (mFeatureSwitcher.isEnabledServerDetail()) {
            mMenuDetail = menu.add(0, MENU_DETAIL, 0, R.string.media_detail);
        }
        if (mFeatureSwitcher.isEnabledInputUri()) {
            menu.add(0, MENU_INPUT_URL, 0, R.string.input_url);
        }
        if (mFeatureSwitcher.isEnabledStreamingSettings()) {
            menu.add(0, MENU_SETTINGS, 0, R.string.streaming_settings);
        }
        return true;
    }
    
    private boolean handleSelectedMenu(MenuItem item) {
        if (LOG) MtkLog.v(TAG, "handleSelectedMenu(" + item + ")");
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
        case MENU_BOOKMARK_ADD:
            mPlayer.addBookmark();
            return true;
        case MENU_BOOKMARK_DISPLAY:
            gotoBookmark();
            return true;
        case MENU_DETAIL:
            mPlayer.showDetail();
            return true;
        case MENU_OUTPUT:
            mCurrentStereoAudio = !mCurrentStereoAudio;
            setStereoAudio(mCurrentStereoAudio);
            return true;
        case MENU_PREVIOUS:
            mPlayer.startPreviousVideo();
            return true;
        case MENU_NEXT:
            mPlayer.startNextVideo();
            return true;
        case MENU_3D_ICON:
            mPlayer.setStereo3D(mMovieInfo.getStereoLayout(), !mPlayer.getStereo3D());
            update3DIcon();
            return true;
        case MENU_AC:
            case R.id.action_switch_auto_convergence:
                item.setChecked(!item.isChecked());
                StereoHelper.setACEnabled((Context)this, false, item.isChecked());
                // tell SurfaceFlinger not to do AC rendering  
                updateConvergenceOffset();
            return true;
        case MENU_MC:
            enterDepthTuningMode();
            return true;
        case MENU_STEREO_LAYOUTS:
            enterVideoLayoutMode();
            return true;
        default:
            return false;
        }
    }
    
    private void updateLoop() {
        if (LOG) MtkLog.v(TAG, "updateLoop() mLoopButton=" + mMenuLoopButton);
        if (mMenuLoopButton != null) {
            if (MtkUtils.isLocalFile(mMovieInfo.getUri(), mMovieInfo.getMimeType())) {
                mMenuLoopButton.setVisible(true);
            } else {
                mMenuLoopButton.setVisible(false);
            }
            boolean newLoop = mPlayer.getLoop();
            if (newLoop) {
                mMenuLoopButton.setTitle(R.string.single);
                mMenuLoopButton.setIcon(R.drawable.ic_menu_unloop);
            } else {
                mMenuLoopButton.setTitle(R.string.loop);
                mMenuLoopButton.setIcon(R.drawable.ic_menu_loop);
            }
        }
    }

    private boolean handlePrepareMenu(Menu menu) {
        if (MtkUtils.isLocalFile(mMovieInfo.getUri(), mMovieInfo.getMimeType())) {
            if (mMenuBookmarkAdd != null) {
                mMenuBookmarkAdd.setVisible(false);
            }
            if (mMenuBookmarks != null) {
                mMenuBookmarks.setVisible(false);
            }
            if (mMenuDetail != null) {
                mMenuDetail.setVisible(false);
            }
        } else {
            if (mMenuBookmarkAdd != null) {
                mMenuBookmarkAdd.setVisible(true);
            }
            if (mMenuBookmarks != null) {
                mMenuBookmarks.setVisible(true);
            }
            if (mMenuDetail != null) {
                mMenuDetail.setVisible(true);
            }
        }
        updatePrevNext();
        updateLoop();
        updateStereoAudioIcon();
        updateStop();
        update3DIcon();

        if (mFeatureSwitcher.isEnabledStereoVideo()) {
            boolean supportsStereo = 
                StereoHelper.isStereo(mMovieInfo.getStereoLayout());
            mMenuAC.setEnabled(supportsStereo);
            mMenuAC.setVisible(supportsStereo);
            boolean isAcEnable = StereoHelper.getACEnabled((Context)this, false);
            mMenuAC.setChecked(isAcEnable);

            mMenuMC.setEnabled(supportsStereo && isAcEnable);
            mMenuMC.setVisible(supportsStereo && isAcEnable);
        }

        return true;
    }
    
    private void updateStop() {
        if (mPlayer != null && mMenuStop != null) {
            mMenuStop.setEnabled(mPlayer.enableStop());
        }
    }
    
    private void updatePrevNext() {
        if (LOG) MtkLog.v(TAG, "updatePrevNext()");
        if (mMenuPrevious != null && mMenuNext != null) {
            if (mMovieInfo.isFirst() && mMovieInfo.isLast()) {//only one movie
                mMenuNext.setVisible(false);
                mMenuPrevious.setVisible(false);
            } else {
                mMenuNext.setVisible(true);
                mMenuPrevious.setVisible(true);
            }
            if (mMovieInfo.isFirst()) {
                mMenuPrevious.setEnabled(false);
            } else {
                mMenuPrevious.setEnabled(true);
            }
            if (mMovieInfo.isLast()) {
                mMenuNext.setEnabled(false);
            } else {
                mMenuNext.setEnabled(true);
            }
        }
    }
    
    //enhance the title feature
    private void enhanceActionBar(String title, Boolean support3D) {
        String scheme = mMovieInfo.getUri().getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {//from file manager
            if (title == null || support3D == null) {
                setInfoFromMediaData(mMovieInfo, title, support3D);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            if ("media".equals(mMovieInfo.getUri().getAuthority())) {//from media database
                if (title == null || support3D == null) {
                    setInfoFromMediaUri(mMovieInfo, title, support3D);
                }
            } else {
                if (title == null) {
                    title = getTitleFromDisplayName(mMovieInfo.getUri());
                }
                if (title == null) {
                    title = getTitleFromData(mMovieInfo.getUri());
                }
            }
        }
        if (mMovieInfo.getTitle() == null) {
            if (title == null) {
                title = getTitleFromUri(mMovieInfo.getUri());
            }
            mMovieInfo.setTitle(title);
        }
        if (LOG) MtkLog.v(TAG, "enhanceActionBar() " + mMovieInfo);
    }
    
    private void setInfoFromMediaUri(MovieItem movieInfo, String title, Boolean support3D) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(movieInfo.getUri(),
                    PROJECTION, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (title == null) {
                    movieInfo.setTitle(cursor.getString(INDEX_TITLE));
                }
                if (support3D == null) {
                    boolean support = MtkUtils.isStereo3D(cursor.getString(INDEX_SUPPORT_3D));
                    movieInfo.setSupport3D(support);
                }
                movieInfo.setStereoLayout(cursor.getInt(INDEX_SUPPORT_3D));
                movieInfo.setConvergence(cursor.getInt(INDEX_CONVERGENCE));
                movieInfo.setId(cursor.getInt(INDEX_ID));
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "setInfoFromMediaUri() " + movieInfo);
    }
    
    private void setInfoFromMediaData(MovieItem movieInfo, String title, Boolean support3D) {
        Cursor cursor = null;
        try {
            String data = Uri.decode(movieInfo.getUri().toString());
            data = data.replaceAll("'", "''");
            String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";
            cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                         PROJECTION, where, null, null);
            MtkLog.v(TAG, "setInfoFromMediaData() cursor=" + cursor.getCount());
            if (cursor != null && cursor.moveToFirst()) {
                if (title == null) {
                    movieInfo.setTitle(cursor.getString(INDEX_TITLE));
                }
                if (support3D == null) {
                    boolean support = MtkUtils.isStereo3D(cursor.getString(INDEX_SUPPORT_3D));
                    movieInfo.setSupport3D(support);
                }
                movieInfo.setStereoLayout(cursor.getInt(INDEX_SUPPORT_3D));
                movieInfo.setConvergence(cursor.getInt(INDEX_CONVERGENCE));
                movieInfo.setId(cursor.getInt(INDEX_ID));
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "setInfoFromMediaData() " + movieInfo);
    }
    
    private String getTitleFromDisplayName(Uri uri) {
        String title = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri,
                    new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                title = cursor.getString(0);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "getTitleFromDisplayName() return " + title);
        return title;
    }
    
    private String getTitleFromUri(Uri uri) {
        String title = Uri.decode(uri.getLastPathSegment());
        if (LOG) MtkLog.v(TAG, "getTitleFromUri() return " + title);
        return title;
    }
    
    private String getTitleFromData(Uri uri) {
        String title = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri,
                    new String[]{"_data"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(cursor.getString(0));
                title = file.getName();
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (LOG) MtkLog.v(TAG, "getTitleFromData() return " + title);
        return title;
    }
    
    private static final String KEY_STEREO = "EnableStereoOutput";
    private boolean mSystemStereoAudio;
    private boolean mCurrentStereoAudio;
    private boolean mIsInitedStereoAudio;
    private AudioManager mAudioManager;
    private boolean getStereoAudio() {
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
        if (LOG) MtkLog.v(TAG, "getStereoAudio() isstereo=" + isstereo + ", stereo=" + stereo + ", key=" + key);
        return isstereo;
    }
    
    private void setStereoAudio(boolean flag) {
        String value = KEY_STEREO + "=" + (flag ? "1" : "0");
        if (mAudioManager == null) {
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        }
        mAudioManager.setParameters(value);
        if (LOG) MtkLog.v(TAG, "setStereoAudio(" + flag + ") value=" + value);
    }
    
    private void updateStereoAudioIcon() {
        if (mMenuStereoAudio != null) {
            if (mCurrentStereoAudio) {
                mMenuStereoAudio.setTitle(R.string.single_track);
            } else {
                mMenuStereoAudio.setTitle(R.string.stereo);
            }
        }
    }
    
    private void enableStereoAudio() {
        if (mFeatureSwitcher.isEnabledStereoAudio()) {
            if (LOG) MtkLog.v(TAG, "enableStereoAudio() mIsInitedStereoAudio=" + mIsInitedStereoAudio
                    + ", mCurrentStereoAudio=" + mCurrentStereoAudio);
            mSystemStereoAudio = getStereoAudio();
            if (!mIsInitedStereoAudio) {
                mCurrentStereoAudio = mSystemStereoAudio;
                mIsInitedStereoAudio = true;
            } else {
                //if activity is not from onCreate()
                //restore old stereo type
                setStereoAudio(mCurrentStereoAudio);
            }
            updateStereoAudioIcon();
        }
    }
    
    private void restoreStereoAudio() {
        if (mFeatureSwitcher.isEnabledStereoAudio()) {
            setStereoAudio(mSystemStereoAudio);
        }
    }
    
    private KeyguardManager mKeyguardManager;
    private boolean isKeyguardLocked() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        }
        // isKeyguardSecure excludes the slide lock case.
        boolean locked = (mKeyguardManager != null) && mKeyguardManager.inKeyguardRestrictedInputMode();
        if (LOG) MtkLog.v(TAG, "isKeyguardLocked() locked=" + locked + ", mKeyguardManager=" + mKeyguardManager);
        return locked;
    }
    
    //for 2D/3D
    private static final String EXTRA_SUPPORT_3D = "Support3D";
    private static final String COLUMN_SUPPORT_3D = MediaStore.Video.Media.STEREO_TYPE;
    private static final String COLUMN_CONVERGENCE = MediaStore.Video.Media.CONVERGENCE;
    
    // projection when query database
    private static final String [] PROJECTION =
                new String[] { VideoColumns.TITLE,
                               COLUMN_SUPPORT_3D,
                               COLUMN_CONVERGENCE,
                               VideoColumns._ID};
    private static int INDEX_TITLE = 0;
    private static int INDEX_SUPPORT_3D = 1;
    private static int INDEX_CONVERGENCE = 2;
    private static int INDEX_ID = 3;

    private void updateStereoLayout(int stereoLayout, boolean updateDB) {
        Log.i(TAG, "updateStereoLayout:stereoLayout="+stereoLayout);
        if (updateDB) {
            StereoHelper.updateStereoLayout((Context)this,
                             mMovieInfo.getUri(), stereoLayout);
        }
        mMovieInfo.setStereoLayout(stereoLayout);
        mPlayer.setStereo3D(stereoLayout);
        update3DIcon();
    }

    private void resetStereoMode() {
        mPlayer.setStereo3D(mMovieInfo.getStereoLayout());
        update3DIcon();
    }

    private Boolean getSupport3DFromIntent() {
        Bundle extra = getIntent().getExtras();
        Boolean support = null;
        if (extra != null && extra.containsKey(EXTRA_SUPPORT_3D)) {
            support = extra.getBoolean(EXTRA_SUPPORT_3D);
        }
        if (LOG) MtkLog.v(TAG, "getSupport3DFromIntent() return " + support);
        return support;
    }
    
    public void update3DIcon() {
        if (mMenuStereoVideoIcon != null) {
            boolean current3D = mPlayer.getStereo3D();
            if (current3D) {
                mMenuStereoVideoIcon.setIcon(R.drawable.ic_switch_to_2d);
                mMenuStereoVideoIcon.setTitle(R.string.stereo3d_mode_switchto_2d);
            } else {
                mMenuStereoVideoIcon.setIcon(R.drawable.ic_switch_to_3d);
                mMenuStereoVideoIcon.setTitle(R.string.stereo3d_mode_switchto_3d);
            }
        }
        if (LOG) MtkLog.v(TAG, "update3DIcon() mSupport3DIcon=" + mMenuStereoVideoIcon);
    }
    
    private void initial3DIcon(boolean support3D) {
        if (mMenuStereoVideoIcon != null) {
            //in phase II, 2D video can be display as 3D video, so we no longer
            //hide 2D/3D icon
            //mMenuStereoVideoIcon.setVisible(support3D);
            update3DIcon();
        }
        if (LOG) MtkLog.v(TAG, "initial3DIcon(" + support3D + ") mSupport3DIcon=" + mMenuStereoVideoIcon);
    }
    
    public void refreshMovieInfo(MovieItem info) {
        mMovieInfo = info;
        setActionBarTitle(info.getTitle());
        initial3DIcon(info.getSupport3D());
        updatePrevNext();
        refreshShareProvider(info);
        if (LOG) MtkLog.v(TAG, "refreshMovieInfo(" + info + ")");
    }

    @Override
    public void onListFilled() {
        if (LOG) MtkLog.v(TAG, "onListFilled()");
        refreshMovieInfo(mMovieInfo);
    }

    private ShareActionProvider mShareProvider;
    private void refreshShareProvider(MovieItem info) {
        if (mShareProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (MtkUtils.isLocalFile(info.getUri(), info.getMimeType())) {
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, info.getUri());
            } else {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(info.getUri()));
            }
            mShareProvider.setShareIntent(intent);
        }
        if (LOG) MtkLog.v(TAG, "refreshShareProvider() mShareProvider=" + mShareProvider);
    }

    //for dynamic feature switcher
    private MovieFeatureSwitcher mFeatureSwitcher;
    
    /* M: ActivityChooseView's popup window will not dismiss
     * when user press power key off and on quickly.
     * Here dismiss the popup window if need.
     * Note: dismissPopup() will check isShowingPopup().
     * @{
     */
    private MenuItem mShareMenu;
    private void collapseShareMenu() {
        if (mShareMenu != null &&  mShareMenu.getActionView() instanceof ActivityChooserView) {
            ActivityChooserView chooserView = (ActivityChooserView)mShareMenu.getActionView();
            if (LOG) MtkLog.v(TAG, "collapseShareMenu() chooserView.isShowingPopup()=" + chooserView.isShowingPopup());
            chooserView.dismissPopup();
        }
    }
    /* @} */

    /// M: share history file name
    private static final String SHARE_HISTORY_FILE = "video_share_history_file";

    // M: added for stereo feature
    private int getOffsetForSF(int value) {
        return value - CENTER;
    }

    public void updateConvergenceOffset() {
        if (!StereoHelper.isStereo(mMovieInfo.getStereoLayout())) return;
        //if AC (Auto Convergence) is off, tell SurfaceFlinger not to enable AC
        if (!StereoHelper.getACEnabled((Context)this, false)) {
            mPlayer.setParameter(StereoHelper.KEY_PARAMETER_3D_OFFSET,
                                 StereoHelper.VALUE_PARAMETER_3D_AC_OFF);
            return;
        }

        mStoredProgress = mMovieInfo.getConvergence();
        if (LOG) MtkLog.i(TAG, "updateConvergenceOffset:mStoredProgress="+mStoredProgress);
        int videoConvergence = mStoredProgress < 0 ? CENTER : mStoredProgress;
        mPlayer.setParameter(StereoHelper.KEY_PARAMETER_3D_OFFSET,
                             getOffsetForSF(videoConvergence));
    }

    private void enterDepthTuningMode() {
        View rootView = findViewById(R.id.root);
        mStoredProgress = mMovieInfo.getConvergence();
        if (LOG) MtkLog.i(TAG, "enterDepthTuningMode:mStoredProgress="+mStoredProgress);
        if (mStoredProgress < 0) mStoredProgress = CENTER;

        mConvBarManager.enterConvTuningMode((ViewGroup)rootView,
            convergenceValues, activeFlags, mStoredProgress);
    }

    private void enterVideoLayoutMode() {
        View rootView = findViewById(R.id.root);
        mStoredStereoLayout = mMovieInfo.getStereoLayout();
        if (LOG) MtkLog.i(TAG, "enterVideoLayoutMode()");

        mVideoLayout.enterVideoLayoutMode((ViewGroup)rootView, mStoredStereoLayout);
    }

    private void updateConvergence(int progress) {
        StereoHelper.updateConvergence((Context)this, 
                         false, mMovieInfo.getId(), progress);
    }

    // for stereo3D phase 2: convergence manual tuning
    private int[] convergenceValues = {0, 10, 20, 30, 40, 50, 60, 70, 80};
    private int[] activeFlags = {0, 0, 0, 0, 10, 0, 0, 0, 0};
    private final int CENTER = 40;

    private boolean mInConvergenceTuningMode = false;
    private int mStoredProgress;
    private int mTempProgressWhenPause;
    private ConvergenceBarManager mConvBarManager;
    private ConvergenceBarManager.ConvergenceChangeListener
        mConvChangeListener = new ConvergenceBarManager.ConvergenceChangeListener() {
        
        @Override
        public void onLeaveConvTuningMode(boolean saveValue, int value) {
            mInConvergenceTuningMode = false;
            
            if (saveValue) {
                // save current value to DB
                updateConvergence(value);
                mStoredProgress = value;
                mMovieInfo.setConvergence(value);
            } else {
                if (mPlayer == null) return;
                mPlayer.setParameter(StereoHelper.KEY_PARAMETER_3D_OFFSET,
                                     getOffsetForSF(mStoredProgress));
            }
            // restore status of time bar and action bar
            updateMeidaPlayerUI();
        }
        
        @Override
        public void onEnterConvTuningMode() {
            mInConvergenceTuningMode = true;
            resetStereoMode();
            // restore status of time bar and action bar
            updateMeidaPlayerUI();
        }
        
        @Override
        public void onConvValueChanged(int value) {
            if (mPlayer == null) return;
            mPlayer.setParameter(StereoHelper.KEY_PARAMETER_3D_OFFSET,
                                 getOffsetForSF(value));
        }

        @Override
        public void onFirstRunHintShown() {
            // we borrow convergence tuning sequence here
            onEnterConvTuningMode();
        }

        @Override
        public void onFirstRunHintDismissed() {
            // we borrow convergence tuning sequence here
            mInConvergenceTuningMode = false;
        }
    };

    private boolean mInVideoLayoutMode = false;
    private int mStoredStereoLayout;
    private StereoVideoLayout mVideoLayout;
    private StereoVideoLayout.VideoLayoutListener
        mVideoLayoutListener = new StereoVideoLayout.VideoLayoutListener() {
        
        @Override
        public void onLeaveVideoLayoutMode(boolean saveValue, int value) {
            if (LOG) MtkLog.i(TAG, "onLeaveVideoLayoutMode(saveValue=" +
                                   saveValue + ", value=" + value + ")");
            mInVideoLayoutMode = false;

            if (saveValue) {
                // save current value to DB
                updateStereoLayout(value, true);
                mStoredStereoLayout = value;
            } else {
                updateStereoLayout(mStoredStereoLayout, false);
            }
            // restore status of time bar and action bar
            updateMeidaPlayerUI();
        }
        
        @Override
        public void onEnterVideoLayoutMode() {
            mInVideoLayoutMode = true;
            resetStereoMode();
            // restore status of time bar and action bar
            updateMeidaPlayerUI();
        }
        
        @Override
        public void onVideoLayoutChanged(int stereoLayout) {
            if (LOG) MtkLog.i(TAG, "onVideoLayoutChanged(stereoLayout="+stereoLayout+")");
            //update stereo layout
            updateStereoLayout(stereoLayout, false);
        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        if (mInConvergenceTuningMode && mConvBarManager != null) {
            mConvBarManager.reloadFirstRun();
            mConvBarManager.reloadConvergenceBar();
        }
        if (mInVideoLayoutMode && mVideoLayout != null) {
            mVideoLayout.reloadVideoLayout();
        }
    }

    @Override
    public void onBackPressed() {
        if (mInConvergenceTuningMode && mConvBarManager != null) {
            mConvBarManager.dismissFirstRun();
            mConvBarManager.leaveConvTuningMode(false);
        } else if (mInVideoLayoutMode && mVideoLayout != null) {
            mVideoLayout.leaveVideoLayoutMode(false);
        } else {
            super.onBackPressed();
        }
    }
    
    // M: for showing first run hint
    public void onInfoFromPlayer() {
        if (mConvBarManager != null) {
            if (mMovieInfo.getSupport3D()) {
                mConvBarManager.onStereoMediaOpened(false);
            }
        }
    }
    
    public boolean isPartialVisible() {
        return mInConvergenceTuningMode || mInVideoLayoutMode;
    }

    private void updateMeidaPlayerUI() {
        if (mPlayer != null) {
            mPlayer.updateUIByHide();
        }
    }
}
