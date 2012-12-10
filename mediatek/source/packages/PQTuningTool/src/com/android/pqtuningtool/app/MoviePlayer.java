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
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmManagerClient.DrmOperationListener;
import android.drm.DrmStore;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import  com.android.pqtuningtool.R;
import  com.android.pqtuningtool.common.BlobCache;
import  com.android.pqtuningtool.util.CacheManager;
import  com.android.pqtuningtool.util.GalleryUtils;
import  com.android.pqtuningtool.util.MtkLog;
import  com.android.pqtuningtool.util.MtkUtils;
import  com.android.pqtuningtool.app.MovieActivity.MovieInfo;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Locale;

import android.app.NotificationManagerPlus;

public class MoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        NotificationManagerPlus.OnFirstShowListener, 
        NotificationManagerPlus.OnLastDismissListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    private static final String KEY_VIDEO_POSITION = "video-position";
    private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
    private final MTKVideoView mVideoView;
    private final Bookmarker mBookmarker;
    private Uri mUri;
    private final Handler mHandler = new Handler();
    private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private final ActionBar mActionBar;
    private final ControllerOverlay mController;

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;

    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };

    public MoviePlayer(View rootView, final MovieActivity movieActivity, MovieInfo info,
            Bundle savedInstance, boolean canReplay) {
        mContext = movieActivity.getApplicationContext();
        mVideoView = (MTKVideoView) rootView.findViewById(R.id.surface_view);
        mBookmarker = new Bookmarker(movieActivity);
        mActionBar = movieActivity.getActionBar();
        mActivityContext = movieActivity;
        mUri = info.getCurrentUri();
        mCanReplay = canReplay;
        judgeStreamingType(info.getCurrentUri(), info.getCurrentMimeType());
        mMovieInfo = info;
        
        mController = new MovieControllerOverlay(mContext);
        ((ViewGroup)rootView).addView(mController.getView());
        mController.setListener(this);
        mController.setCanReplay(canReplay);
        
        //for toast more info and live streaming
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        //we move this behavior to startVideo()
        //mVideoView.setVideoURI(mUri, null, !mWaitMetaData);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });
        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control at this point.
        mVideoView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    mController.show();
                }
            }
        });

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPAUSE);
        movieActivity.sendBroadcast(i);

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            mScreenMode = savedInstance.getInt(KEY_VIDEO_SCREEN_MODE, MTKVideoView.SCREENMODE_BIGSCREEN);
            onRestoreInstanceState(savedInstance);
            //just set flag to avoid start video automatically.
            mHasKilled = true;
        } else {
            mFirstBePlayed = true;
            final BookmarkerInfo bookmark = mBookmarker.getBookmark(mUri);
            if (bookmark != null) {
                showResumeDialog(movieActivity, bookmark);
            } else {
                startVideo();
            }
        }
        setScreenMode();
    }

    private void showSystemUi(boolean visible) {
        int flag = visible ? 0 : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LOW_PROFILE;
        mVideoView.setSystemUiVisibility(flag);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
        //for more details
        outState.putLong(KEY_VIDEO_LAST_DISCONNECT_TIME, mLastDisconnectTime);
        outState.putBoolean(KEY_VIDEO_IS_LOOP, mIsLoop);
        outState.putInt(KEY_VIDEO_SCREEN_MODE, mScreenMode);
        outState.putInt(KEY_VIDEO_LAST_DURATION, mVideoLastDuration);
        if (LOG) MtkLog.v(TAG, "onSaveInstanceState(" + outState + ")");
    }

    private void showResumeDialog(Context context, final BookmarkerInfo bookmark) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.resume_playing_title);
        builder.setMessage(String.format(
                context.getString(R.string.resume_playing_message),
                GalleryUtils.formatDuration(context, bookmark.bookmark / 1000)));
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onCompletion();
            }
        });
        builder.setPositiveButton(
                R.string.resume_playing_resume, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVideo();
                mVideoView.seekTo(bookmark.bookmark);
                mVideoView.setDuration(bookmark.duration);
            }
        });
        builder.setNegativeButton(
                R.string.resume_playing_restart, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVideo();
            }
        });
        builder.show();
    }

    public boolean onPause() {
        if (LOG) MtkLog.v(TAG, "onPuase() isLiveStreaming()=" + isLiveStreaming());
        if (isLiveStreaming()) {
            return false;
        } else {
            doOnPause();
            return true;
        }
    }
    
    //we should stop video anyway after this function called.
    public void onStop() {
        if (LOG) MtkLog.v(TAG, "onStop() mHasPaused=" + mHasPaused);
        if (mHasPaused) {
            //do nothing if video has been paused
        } else {
            doOnPause();
        }
    }
    
    private void doOnPause() {
        long start = System.currentTimeMillis();
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        int position = mVideoView.getCurrentPosition();
        mVideoPosition = position > 0 ? position : mVideoPosition;
        int duration = mVideoView.getDuration();
        mVideoLastDuration = duration > 0 ? duration : mVideoLastDuration;
        mBookmarker.setBookmark(mUri, mVideoPosition, mVideoLastDuration);
        long end1 = System.currentTimeMillis();
        mVideoView.suspend();
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
        mVideoView.setResumed(false);//avoid start after surface created
        mVideoView.setVisibility(View.INVISIBLE);//Workaround for last-seek frame difference
        
        long end2 = System.currentTimeMillis();
        mController.clearBuffering();//to end buffer state
        recordDisconnectTime();
        if (LOG) MtkLog.v(TAG, "doOnPause() save video info consume:" + (end1 - start));
        if (LOG) MtkLog.v(TAG, "doOnPause() suspend video consume:" + (end2 - end1));
        if (LOG) MtkLog.v(TAG, "doOnPause() mVideoPosition=" + mVideoPosition + ", mResumeableTime=" + mResumeableTime
                + ", mVideoLastDuration=" + mVideoLastDuration);
    }

    public void onResume() {
        if (LOG) MtkLog.v(TAG, "onResume() mHasPaused=" + mHasPaused + ", mIsShowDialog=" + mIsShowDialog
                + ", mVideoPosition=" + mVideoPosition + ", mResumeableTime=" + mResumeableTime
                + ", mHasKilled=" + mHasKilled + ", mVideoLastDuration=" + mVideoLastDuration);
        mVideoView.setVisibility(View.VISIBLE);
        if (mIsShowDialog) {
            //wait for user's operation
            return;
        }
        if (!passDisconnectCheck()) {
            return;
        }
        if (mHasPaused || mHasKilled) {
            if (mHasKilled) {
                doStartVideo();
                mVideoView.seekTo(mVideoPosition);
                mVideoView.setDuration(mVideoLastDuration);
            } else if (mHasPaused) {
                prepareController();
                mVideoView.seekTo(mVideoPosition);
                mVideoView.setDuration(mVideoLastDuration);
                if (!mVideoView.isTargetError()) {
                    mVideoView.resume();
                }
            }
            mVideoView.dump();
            // If we have slept for too long, pause the play
            // If is live streaming, do not pause it too
            // In CMCC project, we do not pause video.
            if (System.currentTimeMillis() > mResumeableTime && !isLiveStreaming()
                    && !MtkUtils.isCmccOperator()) {
                pauseVideo();
            }
            mHasPaused = false;
        }
        mHandler.post(mProgressChecker);
    }

    private void prepareController() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            if (mVideoView.isTargetPlaying()) {
                mController.showLoading();
                mController.setPlayingInfo(isLiveStreaming());
                mHandler.removeCallbacks(mPlayingChecker);
                mHandler.postDelayed(mPlayingChecker, 250);
            } else if (mVideoView.isTargetError()) {
                mController.showReconnectingError();
            } else {
                mController.showPaused();
            }
        } else if (mVideoView.isTargetPlaying()) {
            mController.showPlaying();
        } else {
            mController.showPaused();
        }
    }

    public void onDestroy() {
        mVideoView.stopPlayback();
        mAudioBecomingNoisyReceiver.unregister();
        clearTimeoutDialog();
    }

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (mDragging || (!mShowing && !mVideoView.isOnlyAudio())) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        mController.setTimes(position, duration);
        return position;
    }

    private void doStartVideo() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            mController.setPlayingInfo(isLiveStreaming());
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mController.showPlaying();
        }
        mVideoView.setVideoURI(mUri, null, !mWaitMetaData);
        mVideoView.start();
        //we may start video from stopVideo,
        //this case, we should reset canReplay flag according canReplay and loop 
        boolean canReplay = mIsLoop ? mIsLoop : mCanReplay;
        mController.setCanReplay(canReplay);
        setProgress();
    }

    private void startVideo() {
        if (!MtkUtils.handleDrmFile(mActivityContext, mUri, new VideoStartListener())) {
            doStartVideo();
        }
    }

    private void playVideo() {
        mVideoView.start();
        mController.showPlaying();
        setProgress();
    }

    private void pauseVideo() {
        mVideoView.pause();
        if (mVideoView.canPause()) {
            mController.showPaused();
        } else {
            //do not update pause status
        }
        setProgress();
    }

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        if (LOG) MtkLog.v(TAG, "onError(" + player + ", " + arg1 + ", " + arg2 + ") mIsShowDialog=" + mIsShowDialog);
        //if we are showing a dialog, cancel the error dialog
        if (mIsShowDialog) {
            return true;
        }
        int framework_err = arg1;
        if (framework_err == MediaPlayer.MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER) {
            //get the last position for retry
            mRetryPosition = mVideoView.getCurrentPosition();
            mRetryDuration = mVideoView.getDuration();
            mRetryCount++;
            if (reachRetryCount()) {
                mController.showReconnectingError();
            } else {
                mController.showReconnecting(mRetryCount);
                retry();
            }
            return true;
        }

        mHandler.removeCallbacksAndMessages(null);
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (LOG) MtkLog.v(TAG, "onCompletion() mIsLoop=" + mIsLoop + ", mCanReplay=" + mCanReplay);
        if (mMovieInfo.isAllError()) {
            MtkLog.w(TAG, "All error, exit the video player!");
            mMovieInfo.clearErrors();
            mActivityContext.finish();
            return;
        }
        if (mMovieInfo.isLast()) {
            if (mIsLoop) {
                startNextVideo();
            } else {//original logic
                mController.showEnded();
                onCompletion();
            }
        } else {
            startNextVideo();
        }
    }

    public void startNextVideo() {
        if (!mMovieInfo.isLast() || mIsLoop) {
            int position = mVideoView.getCurrentPosition();
            int duration = mVideoView.getDuration();
            mBookmarker.setBookmark(mUri, position, duration);
            mVideoView.stopPlayback();
            clearLocalInfo();
            clearServerInfo();
            mMovieInfo.moveToNext();
            mUri = mMovieInfo.getCurrentUri();
            startVideo();
        } else {
            MtkLog.e(TAG, "Cannot play the next video! " + mMovieInfo);
        }
        mActivityContext.closeOptionsMenu();
    }
    
    public void startPreviousVideo() {
        if (!mMovieInfo.isFirst() || mIsLoop) {
            mVideoView.stopPlayback();
            clearLocalInfo();
            clearServerInfo();
            mMovieInfo.moveToPrevious();
            mUri = mMovieInfo.getCurrentUri();
            startVideo();
        } else {
            MtkLog.e(TAG, "Cannot play the previous video! " + mMovieInfo);
        }
        mActivityContext.closeOptionsMenu();
    }
    
    public void onCompletion() {
    }

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            if (mVideoView.canPause()) {
                pauseVideo();
            }
        } else {
            playVideo();
        }
    }

    @Override
    public void onSeekStart() {
        if (LOG) MtkLog.v(TAG, "onSeekStart() mDragging=" + mDragging);
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        if (LOG) MtkLog.v(TAG, "onSeekMove(" + time + ") mDragging=" + mDragging);
        if (!mDragging) {//disable dragging seek
            mVideoView.seekTo(time);
        }
    }

    @Override
    public void onSeekEnd(int time) {
        if (LOG) MtkLog.v(TAG, "onSeekEnd(" + time + ") mDragging=" + mDragging);
        mDragging = false;
        mVideoView.seekTo(time);
        setProgress();
    }

    @Override
    public void onShown() {
        mShowing = true;
        mActionBar.show();
        showSystemUi(true);
        setProgress();
    }

    @Override
    public void onHidden() {
        mShowing = false;
        mActionBar.hide();
        showSystemUi(false);
    }

    @Override
    public void onReplay() {
        if (LOG) MtkLog.v(TAG, "onReplay() isRetrying()=" + isRetrying());
        mFirstBePlayed = true;
        if (isRetrying()) {//from connecting error
            clearRetry();
            int errorPosition = mVideoView.getCurrentPosition();
            int errorDuration = mVideoView.getDuration();
            startVideo();
            if (errorPosition > 0) {
                mVideoView.seekTo(errorPosition);
                mVideoView.setDuration(errorDuration);
            }
            if (LOG) MtkLog.v(TAG, "onReplay() errorPosition=" + errorPosition
                    + ", errorDuration=" + errorDuration);
        } else {
            startVideo();
        }
    }

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Some headsets will fire off 7-10 events on a single click
        if (event.getRepeatCount() > 0) {
            return isMediaKey(keyCode);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // TODO: Handle next / previous accordingly, for now we're
                // just consuming the events.
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return isMediaKey(keyCode);
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    // We want to pause when the headset is unplugged.
    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        public void register() {
            mContext.registerReceiver(this,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVideoView.isPlaying()) pauseVideo();
        }
    }
    
    //for log flag, if set this false, will improve run speed.
    private static final boolean LOG = true;
    //for cmcc streaming notifications
    private boolean mLastPlaying;
    public void onFirstShow(DialogInterface dialog) {
        pauseIfNeed();
        if (LOG) MtkLog.v(TAG, "onFirstShow() mLastPlaying=" + mLastPlaying);
    }

    public void onLastDismiss(DialogInterface dialog) {
        resumeIfNeed();
        if (LOG) MtkLog.v(TAG, "onLastDismiss() mLastPlaying=" + mLastPlaying);
    }
    
    //for cmcc server timeout case 
    //please remember to clear this value when changed video.
    private int mServerTimeout = -1;
    private long mLastDisconnectTime;
    private boolean mIsShowDialog = false;
    private AlertDialog mServerTimeoutDialog;
    
    //check whether disconnect from server timeout or not.
    //if timeout, return false. otherwise, return true.
    private boolean passDisconnectCheck() {
        if (MtkUtils.isCmccOperator() && !isFullBuffer()) {
            //record the time disconnect from server
            long now = System.currentTimeMillis();
            if (LOG) MtkLog.v(TAG, "now=" + now + ", mLastPauseTime=" + mLastDisconnectTime + ", mServerTimeout=" + mServerTimeout);
            if (mServerTimeout > 0 && (now - mLastDisconnectTime) > mServerTimeout) {
                //disconnect time more than server timeout, notify user
                notifyServerTimeout();
                return false;
            }
        }
        return true;
    }
    
    private void recordDisconnectTime() {
        if (MtkUtils.isCmccOperator() && !isFullBuffer()) {
            //record the time disconnect from server
            mLastDisconnectTime = System.currentTimeMillis();
        }
        if (LOG) MtkLog.v(TAG, "recordDisconnectTime() mLastDisconnectTime=" + mLastDisconnectTime);
    }
    
    private void clearServerInfo() {
        mServerTimeout = -1;
    }
    
    private void notifyServerTimeout() {
        if (mServerTimeoutDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext);
            mServerTimeoutDialog = builder.setTitle(R.string.server_timeout_title)
                .setMessage(R.string.server_timeout_message)
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (LOG) MtkLog.v(TAG, "NegativeButton.onClick() mIsShowDialog=" + mIsShowDialog);
                        mController.showEnded();
                        onCompletion();
                    }
                    
                })
                .setPositiveButton(R.string.resume_playing_resume, new OnClickListener() {
    
                    public void onClick(DialogInterface dialog, int which) {
                        if (LOG) MtkLog.v(TAG, "PositiveButton.onClick() mIsShowDialog=" + mIsShowDialog);
                        startVideo();
                    }
                    
                })
                .create();
            mServerTimeoutDialog.setOnDismissListener(new OnDismissListener() {
                    
                    public void onDismiss(DialogInterface dialog) {
                        if (LOG) MtkLog.v(TAG, "mServerTimeoutDialog.onDismiss()");
                        mIsShowDialog = false;
                    }
                    
                });
            mServerTimeoutDialog.setOnShowListener(new OnShowListener() {

                    public void onShow(DialogInterface dialog) {
                        if (LOG) MtkLog.v(TAG, "mServerTimeoutDialog.onShow()");
                        mIsShowDialog = true;
                    }
                    
                });
        }
        mServerTimeoutDialog.show();
    }
    
    private void clearTimeoutDialog() {
        if (mServerTimeoutDialog != null && mServerTimeoutDialog.isShowing()) {
            mServerTimeoutDialog.dismiss();
        }
        mServerTimeoutDialog = null;
    }

    private Activity mActivityContext;//for dialog and toast context
    private boolean mFirstBePlayed = false;//for toast more info
    private boolean mHasKilled = false;//for killed case
    
    private void clearLocalInfo() {
        mVideoPosition = 0;
        mVideoLastDuration = 0;
    }

    private void getVideoInfo(MediaPlayer mp) {
        Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
                MediaPlayer.BYPASS_METADATA_FILTER);
        if (data != null) {
            if (data.has(Metadata.SERVER_TIMEOUT)) {
                mServerTimeout = data.getInt(Metadata.SERVER_TIMEOUT);
                MtkLog.i(TAG, "get server timeout from metadata. mServerTimeout=" + mServerTimeout);
            }
            if (data.has(Metadata.TITLE)) {
                mTitle = data.getString(Metadata.TITLE);
            }
            if (data.has(Metadata.AUTHOR)) {
                mAuthor = data.getString(Metadata.AUTHOR);
            }
            if (data.has(Metadata.COPYRIGHT)) {
                mCopyRight = data.getString(Metadata.COPYRIGHT);
            }
        } else {
            MtkLog.w(TAG, "Metadata is null!");
        }
        int duration = mp.getDuration();
        if (duration <= 0) {
            mStreamingType = STREAMING_SDP;//correct it
        } else {
            //correct sdp to rtsp
            if (mStreamingType == STREAMING_SDP) {
                mStreamingType = STREAMING_RTSP;
            }
        }
        if (LOG) MtkLog.v(TAG, "getVideoInfo() mServerTimeout=" + mServerTimeout + ", duration=" + duration + ", mStreamingType=" + mStreamingType);
    }
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (LOG) MtkLog.v(TAG, "onPrepared(" + mp + ")");
        getVideoInfo(mp);
        boolean onlyAudio = mVideoView.isOnlyAudio();
        mController.setBottomPanel(onlyAudio);
        if (!isLocalFile()) {//hear we get the correct streaming type.
            mController.setPlayingInfo(isLiveStreaming());
        }
        boolean canPause = mVideoView.canPause();
        boolean canSeek = mVideoView.canSeekBackward() && mVideoView.canSeekForward();
        mController.setCanPause(canPause);
        mController.setCanScrubbing(canSeek);
        if (!canPause && !mVideoView.isTargetPlaying()) {
            mVideoView.start();
        }
        if (LOG) MtkLog.v(TAG, "onPrepared() canPause=" + canPause + ", canSeek=" + canSeek);
    }
    
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (LOG) MtkLog.v(TAG, "onInfo() what:" + what + " extra:" + extra);
        if (what == MediaPlayer.MEDIA_INFO_GET_BUFFER_DATA) {
            //this means streaming player has got the display data
            //so we can retry connect server if it has connection error.
            clearRetry();
            return true;
        }
        if (mFirstBePlayed && what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_SUPPORTED) {
            Toast.makeText(mActivityContext, R.string.VideoView_info_text_video_not_supported, Toast.LENGTH_SHORT).show();
            mFirstBePlayed = false;
            return true;
        }
        return false;
    }
    
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (!mPauseBuffering) {
            boolean fullBuffer = isFullBuffer();
            mController.showBuffering(fullBuffer, percent);
        }
        if (LOG) MtkLog.v(TAG, "onBufferingUpdate(" + percent + ") mPauseBuffering=" + mPauseBuffering);
    }
    
    //for screen mode feature
    private final static String KEY_VIDEO_SCREEN_MODE = "video_screen_mode";
    private int mScreenMode = MTKVideoView.SCREENMODE_BIGSCREEN;
    private void setScreenMode() {
        mVideoView.setScreenMode(mScreenMode);
        mController.setScreenMode(mScreenMode);
        if (LOG) MtkLog.v(TAG, "setScreenMode() mScreenMode=" + mScreenMode);
    }
    
    @Override
    public void OnScreenModeChanged(int newMode) {
        mScreenMode = newMode;
        mVideoView.setScreenMode(newMode);
        mController.show();
        if (LOG) MtkLog.v(TAG, "OnScreenModeClicked(" + newMode + ")");
    }
    
    public void stopVideo() {
        mVideoView.clearSeek();
        mVideoView.stopPlayback();
        //clear video info
        //mVideoView.clearUri();
        clearServerInfo();
        clearLocalInfo();
        mFirstBePlayed = false;
        mController.setCanReplay(true);
        mController.showEnded();
    }
    
    //for loop feature.
    private boolean mIsLoop;
    private final boolean mCanReplay;
    public void setLoop(boolean loop) {
        if (LOG) MtkLog.v(TAG, "setLoop(" + loop + ") mIsLoop=" + mIsLoop);
        if (isLocalFile()) {
            mIsLoop = loop;
            mController.setCanReplay(loop);
        } else {
            //do nothing
        }
    }
    
    public boolean getLoop() {
        if (LOG) MtkLog.v(TAG, "getLoop() return " + mIsLoop);
        return mIsLoop;
    }

    //for re-connect feature
    private int mRetryDuration;
    private int mRetryPosition;
    private int mRetryCount;
    public void retry() {
        mVideoView.setVideoURI(mUri, null, !mWaitMetaData);
        mVideoView.seekTo(mRetryPosition);//retry it from old position
        mVideoView.setDuration(mRetryDuration);
        mVideoView.start();
        if (LOG) MtkLog.v(TAG, "retry() mRetryCount=" + mRetryCount + ", mRetryPosition=" + mRetryPosition);
    }
    
    public void clearRetry() {
        if (LOG) MtkLog.v(TAG, "clearRetry() mRetryCount=" + mRetryCount);
        mRetryCount = 0;
    }
    
    public boolean reachRetryCount() {
        if (LOG) MtkLog.v(TAG, "reachRetryCount() mRetryCount=" + mRetryCount);
        if (mRetryCount > 3) {
            return true;
        }
        return false;
    }
    
    public int getRetryCount() {
        if (LOG) MtkLog.v(TAG, "getRetryCount() return " + mRetryCount);
        return mRetryCount;
    }
    
    public boolean isRetrying() {
        boolean retry = false;
        if (mRetryCount > 0) {
            retry = true;
        }
        if (LOG) MtkLog.v(TAG, "isRetrying() mRetryCount=" + mRetryCount);
        return retry;
    }
    
    //for more detail in been killed case
    private final static String KEY_VIDEO_CAN_SEEK = "video_can_seek";
    private final static String KEY_POSITION_WHEN_PAUSED = "Position_when_paused";
    private final static String KEY_VIDEO_LAST_DISCONNECT_TIME = "last_disconnect_time";
    private final static String KEY_VIDEO_IS_LOOP = "video_is_loop";
    private final static String KEY_VIDEO_LAST_DURATION = "video_last_duration";
    
    private int mVideoLastDuration;//for duration displayed in init state
    
    private void onRestoreInstanceState(Bundle icicle) {
        if (LOG) MtkLog.v(TAG, "onRestoreInstanceState(" + icicle + ")");
        mLastDisconnectTime = icicle.getLong(KEY_VIDEO_LAST_DISCONNECT_TIME);
        mIsLoop = icicle.getBoolean(KEY_VIDEO_IS_LOOP, false);
        mVideoLastDuration = icicle.getInt(KEY_VIDEO_LAST_DURATION);
        if (mIsLoop) {
            mController.setCanReplay(true);
        } else {
            //will get can replay from intent.
        }
        if (LOG) MtkLog.v(TAG, "onRestoreInstanceState() mLastDisconnectTime=" + mLastDisconnectTime
                + ", mVideoPosition=" + mVideoPosition + ", mResumeableTime=" + mResumeableTime
                + ", mIsLoop=" + mIsLoop + ", mVideoLastDuration=" + mVideoLastDuration);
    }
    
    //for streaming feature
    //judge and support sepcial streaming type
    public static final int STREAMING_LOCAL = 0;
    public static final int STREAMING_HTTP = 1;
    public static final int STREAMING_RTSP = 2;
    public static final int STREAMING_SDP = 3;
    
    private boolean mWaitMetaData;
    private int mStreamingType = STREAMING_LOCAL;
    
    private void judgeStreamingType(Uri uri, String mimeType) {
        if (LOG) MtkLog.v(TAG, "judgeStreamingType(" + uri + ")");
        if (uri == null) {
            if (LOG) MtkLog.w(TAG, "uri is null, cannot judge streaming type.");
            return;
        }
        String scheme = uri.getScheme();
        mWaitMetaData = true;
        if (MtkUtils.isSdpStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_SDP;
        } else if (MtkUtils.isRtspStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_RTSP;
        } else if (MtkUtils.isHttpStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_HTTP;
            mWaitMetaData = false;
        } else {
            mStreamingType = STREAMING_LOCAL;
            mWaitMetaData = false;
        }
        if (LOG) MtkLog.v(TAG, "mStreamingType=" + mStreamingType
                + " mCanGetMetaData=" + mWaitMetaData);
    }
    
    public boolean isFullBuffer() {
        if (mStreamingType == STREAMING_RTSP || mStreamingType == STREAMING_SDP) {
            return false;
        }
        return true;
    }
    
    public boolean isLocalFile() {
        if (mStreamingType == STREAMING_LOCAL) {
            return true;
        }
        return false;
    }
    
    public boolean isLiveStreaming() {
        boolean isLive = false;
        if (mStreamingType == STREAMING_SDP) {
            isLive = true;
        }
        if (LOG) MtkLog.v(TAG, "isLiveStreaming() return " + isLive);
        return isLive;
    }
    
    public boolean isOnlyAudio() {
        return mVideoView.isOnlyAudio();
    }
    
    //for DRM feature
    private class VideoStartListener implements DrmOperationListener {
        
        public void onOperated(int type) {
            if (LOG) MtkLog.v(TAG, "VideoStartListener.onOperated(" + type + ")");
            switch(type) {
            case DrmOperationListener.CONTINUE:
                doStartVideo();
                MtkUtils.consume(mContext, mUri, DrmStore.Action.PLAY);
                break;
            case DrmOperationListener.STOP:
                onCompletion();
                break;
            }
        }
    }
    
    private boolean mLastCanPaused;
    private boolean mPauseBuffering;
    private void pauseIfNeed() {
        mLastCanPaused = mVideoView.canPause();
        if (mLastCanPaused) {
            mLastPlaying = mVideoView.isPlaying();
            mController.clearBuffering();
            mPauseBuffering = true;
            mVideoView.pause();//puase it every case
        }
        if (LOG) MtkLog.v(TAG, "pauseIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                + ", mPauseBuffering=" + mPauseBuffering);
    }
    
    private void resumeIfNeed() {
        if (mLastCanPaused) {
            if (mLastPlaying) {
                mPauseBuffering = false;
                mVideoView.start();
                mController.showPlaying();
            } else {
                mController.showPaused();
            }
        }
        if (LOG) MtkLog.v(TAG, "resumeIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                + ", mPauseBuffering=" + mPauseBuffering);
    }
    
    private String mAuthor;
    private String mTitle;
    private String mCopyRight;
    
    public void showDetail() {
        DetailDialog detailDialog = new DetailDialog(mActivityContext, mTitle, mAuthor, mCopyRight);
        detailDialog.setTitle(R.string.media_detail);
        detailDialog.setOnShowListener(new OnShowListener() {
            
            @Override
            public void onShow(DialogInterface dialog) {
                if (LOG) MtkLog.v(TAG, "showDetail.onShow()");
                pauseIfNeed();
            }
        });
        detailDialog.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (LOG) MtkLog.v(TAG, "showDetail.onDismiss()");
                resumeIfNeed();
            }
        });
        detailDialog.show();
    }
    
    private BookmarkEnhance mBookmark;
    public void addBookmark(String mimeType) {
        if (mBookmark == null) {
            mBookmark = new BookmarkEnhance(mActivityContext);
        }
        String uri = String.valueOf(mUri);
        if (mBookmark.exists(uri)) {
            Toast.makeText(mActivityContext, R.string.bookmark_exist, Toast.LENGTH_SHORT).show();
        } else {
            mBookmark.insert(mTitle, uri, mimeType, 0);
            Toast.makeText(mActivityContext, R.string.bookmark_add_success, Toast.LENGTH_SHORT).show();
        }
        if (LOG) MtkLog.v(TAG, "addBookmark(" + mimeType + ") mTitle=" + mTitle + ", mUri=" + mUri);
    }
    
    private MovieInfo mMovieInfo;
}

class Bookmarker {
    private static final String TAG = "Bookmarker";

    private static final String BOOKMARK_CACHE_FILE = "bookmark";
    private static final int BOOKMARK_CACHE_MAX_ENTRIES = 100;
    private static final int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
    private static final int BOOKMARK_CACHE_VERSION = 1;

    private static final int HALF_MINUTE = 30 * 1000;
    private static final int TWO_MINUTES = 4 * HALF_MINUTE;

    private final Context mContext;

    public Bookmarker(Context context) {
        mContext = context;
    }

    public void setBookmark(Uri uri, int bookmark, int duration) {
        if (LOG) MtkLog.v(TAG, "setBookmark(" + bookmark + ", " + duration + ")");
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(uri.toString());
            dos.writeInt(bookmark);
            dos.writeInt(Math.abs(duration));
            dos.flush();
            cache.insert(uri.hashCode(), bos.toByteArray());
        } catch (Throwable t) {
            Log.w(TAG, "setBookmark failed", t);
        }
    }

    public BookmarkerInfo getBookmark(Uri uri) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            byte[] data = cache.lookup(uri.hashCode());
            if (data == null) return null;

            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(data));

            String uriString = dis.readUTF(dis);
            int bookmark = dis.readInt();
            int duration = dis.readInt();

            if (!uriString.equals(uri.toString())) {
                return null;
            }

            if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                    || (bookmark > (duration - HALF_MINUTE))) {
                return null;
            }
            return new BookmarkerInfo(bookmark, duration);
        } catch (Throwable t) {
            Log.w(TAG, "getBookmark failed", t);
        }
        return null;
    }
    
    private static final boolean LOG = true;
    
}

class BookmarkerInfo {
    public final int bookmark;
    public final int duration;
    
    public BookmarkerInfo(int bookmark, int duration) {
        this.bookmark = bookmark;
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
        .append("BookmarkInfo(bookmark=")
        .append(bookmark)
        .append(", duration=")
        .append(duration)
        .append(")")
        .toString();
    }
}
