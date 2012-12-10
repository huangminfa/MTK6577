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

package  com.android.pqtuningtool.app;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.Metadata;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import  com.android.pqtuningtool.ui.Log;
import  com.android.pqtuningtool.util.MtkLog;

/**
 * MTKVideoView enhance the streaming videoplayer process and UI.
 * It only supports MTKMediaController. 
 * If you set android's default MediaController,
 * some state will not be shown well.
 * Moved from the package android.widget
 */
public class MTKVideoView extends VideoView {
    private static final String TAG = "Gallery3D/MTKVideoView";
    private static final boolean LOG = true;
    //support screen mode.
    public static final int SCREENMODE_BIGSCREEN = 1;
    public static final int SCREENMODE_FULLSCREEN = 2;
    public static final int SCREENMODE_CROPSCREEN = 4;
    private int mScreenMode = SCREENMODE_BIGSCREEN;
    
    //add info listener to get info whether can get meta data or not for rtsp.
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    
    private boolean mIsOnlyAudio = false;
    //when the streaming type is live, metadata maybe not right when prepared.
    private boolean mHasGotMetaData = false;
    private boolean mHasGotPreparedCallBack = false;
    private static final int MSG_START_VIDEO = 1;

    private Handler mHander = new Handler(){

        public void handleMessage(Message msg) {
            if (LOG) MtkLog.v(TAG, "handleMessage() to do prepare. msg=" + msg);
            switch(msg.what) {
            case MSG_START_VIDEO:
                if (mMediaPlayer == null || mUri == null) {
                    MtkLog.w(TAG, "Cannot prepare play! mMediaPlayer=" + mMediaPlayer + ", mUri=" + mUri);
                } else {
                    doPreparedIfReady(mMediaPlayer);
                }
                break;
            default:
                MtkLog.w(TAG, "Unhandled message " + msg);
                break;
            }
        }
        
    };
    
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (LOG) MtkLog.v(TAG, "onInfo() what:" + what + " extra:" + extra);
            if (mOnInfoListener != null && mOnInfoListener.onInfo(mp, what, extra)) {
                return true;
            } else {
                if (what == MediaPlayer.MEDIA_INFO_METADATA_CHECK_COMPLETE) {
                    mHasGotMetaData = true;
                    doPreparedIfReady(mMediaPlayer);
                    return true;
                }
            }
            return false;
        }
        
    };
    
    private void doPreparedIfReady(MediaPlayer mp) {
        if (LOG) MtkLog.v(TAG, "doPreparedIfReady() mHasGotPreparedCallBack=" + mHasGotPreparedCallBack
                + ", mHasGotMetaData=" + mHasGotMetaData
                + ", mVideoLayoutStatus=" + mVideoLayoutStatus
                + ", mCurrentState=" + mCurrentState);
        if (mHasGotPreparedCallBack && mHasGotMetaData && isLayoutVideoReady()) {
            doPrepared(mp);
            //clear the wait video layout flag.
            //so other info o relayot will not refresh the video status.
            mVideoLayoutStatus = VideoLayoutStatus.DONE;
        }
    }
    
    private enum VideoLayoutStatus {
        UNKNOWN,
        WAIT,
        REDAY,
        DONE
    }
    
    private VideoLayoutStatus mVideoLayoutStatus = VideoLayoutStatus.UNKNOWN;
    
    private boolean isLayoutVideoReady() {
        if (LOG) MtkLog.v(TAG, "isLayoutVideoReady() mVideoLayoutStatus=" + mVideoLayoutStatus);
        if (mVideoLayoutStatus == VideoLayoutStatus.REDAY) {
            return true;
        }
        return false;
    }
    
    public MTKVideoView(Context context) {
        super(context);
        initialize();
    }

    public MTKVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MTKVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
    
    private void initialize() {
        mPreparedListener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (LOG) MtkLog.v(TAG, "mPreparedListener.onPrepared(" + mp + ")");
                //Here we can get meta data from mediaplayer.
                // Get the capabilities of the player for this stream
                Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
                                          MediaPlayer.BYPASS_METADATA_FILTER);
                if (data != null) {
                    mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
                            || data.getBoolean(Metadata.PAUSE_AVAILABLE);
                    mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
                            || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
                    mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
                            || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
                    if (data.has(Metadata.VIDEO_HEIGHT) 
                            && data.has(Metadata.VIDEO_WIDTH)
                            && data.getInt(Metadata.VIDEO_HEIGHT) > 0
                            && data.getInt(Metadata.VIDEO_WIDTH) > 0) {
                        mIsOnlyAudio = false;
                        MtkLog.v(TAG, "mIsOnlyAudio=false " + data);
                    } else {
                        mIsOnlyAudio = true;
                        MtkLog.v(TAG, "mIsOnlyAudio=true " + data);
                    }
                } else {
                    mCanPause = mCanSeekBack = mCanSeekForward = true;
                    MtkLog.w(TAG, "Metadata is null!");
                }
                if (LOG) MtkLog.v(TAG, "isOnlyAudio=" + mIsOnlyAudio + ", mCanPause=" + mCanPause);
                mHasGotPreparedCallBack = true;
                //If only audio, video size will be (0,0),
                //so we do not wait layout video.
                if (mIsOnlyAudio) {
                    mVideoLayoutStatus = VideoLayoutStatus.REDAY;
                }
                doPreparedIfReady(mMediaPlayer);
            }
        };
        
        mErrorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                if (mCurrentState == STATE_ERROR) {
                    Log.w(TAG, "Duplicate error message. error message has been sent! " +
                            "error=(" + framework_err + "," + impl_err + ")");
                    return true;
                }
                //record error position and duration
                //here disturb the original logic
                mSeekWhenPrepared = getCurrentPosition();
                mDuration = getDuration();
                mCurrentState = STATE_ERROR;
                mTargetState = STATE_ERROR;
                if (mMediaController != null) {
                    mMediaController.hide();
                }

                /* If an error handler has been supplied, use it and finish. */
                if (mOnErrorListener != null) {
                    if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                        return true;
                    }
                }

                /* Otherwise, pop up an error dialog so the user knows that
                 * something bad has happened. Only try and pop up the dialog
                 * if we're attached to a window. When we're going away and no
                 * longer have a window, don't bother showing the user an error.
                 */
                if (getWindowToken() != null) {
                    Resources r = mContext.getResources();
                    int messageId;
                    
                    if (framework_err == MediaPlayer.MEDIA_ERROR_BAD_FILE) {
                        messageId = com.mediatek.R.string.VideoView_error_text_bad_file;
                    } else if (framework_err == MediaPlayer.MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER) {
                        messageId = com.mediatek.R.string.VideoView_error_text_cannot_connect_to_server;
                    } else if (framework_err == MediaPlayer.MEDIA_ERROR_TYPE_NOT_SUPPORTED) {
                        messageId = com.mediatek.R.string.VideoView_error_text_type_not_supported;
                    } else if (framework_err == MediaPlayer.MEDIA_ERROR_DRM_NOT_SUPPORTED) {
                        messageId = com.mediatek.R.string.VideoView_error_text_drm_not_supported;
                    } else if (framework_err == MediaPlayer.MEDIA_ERROR_INVALID_CONNECTION) {
                        messageId = com.mediatek.internal.R.string.VideoView_error_text_invalid_connection;
                    } else if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                        messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
                    } else {
                        messageId = com.android.internal.R.string.VideoView_error_text_unknown;
                    }
                    
                    new AlertDialog.Builder(mContext)
                            .setTitle(com.android.internal.R.string.VideoView_error_title)
                            .setMessage(messageId)
                            .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            /* If we get here, there is no onError listener, so
                                             * at least inform them that the video is over.
                                             */
                                            if (mOnCompletionListener != null) {
                                                mOnCompletionListener.onCompletion(mMediaPlayer);
                                            }
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                }
                return true;
            }
        };
        
        mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mCurrentBufferPercentage = percent;
                if (mOnBufferingUpdateListener != null) {
                    mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
                }
                if (LOG) MtkLog.v(TAG, "onBufferingUpdate() Buffering percent: " + percent);
                if (LOG) MtkLog.v(TAG, "onBufferingUpdate() mTargetState=" + mTargetState);
                if (LOG) MtkLog.v(TAG, "onBufferingUpdate() mCurrentState=" + mCurrentState);
            }
        };
        
        mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                if (LOG) MtkLog.v(TAG, "OnVideoSizeChagned(" + width + "," + height + ")");
                if (LOG) MtkLog.v(TAG, "OnVideoSizeChagned(" + mVideoWidth + "," + mVideoHeight + ")");
                if (LOG) MtkLog.v(TAG, "OnVideoSizeChagned() mCurrentState=" + mCurrentState);
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    if (mCurrentState == STATE_PREPARING) {
                        mVideoLayoutStatus = VideoLayoutStatus.WAIT;
                        MTKVideoView.this.requestLayout();
                    }
                }
            }
        };
        
        getHolder().removeCallback(mSHCallback);
        mSHCallback = new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (LOG) Log.v(TAG, "surfaceChanged(" + holder + ", " + format + ", " + w + ", " + h + ")");
                if (LOG) Log.v(TAG, "surfaceChanged() mMediaPlayer=" + mMediaPlayer + ", mTargetState=" + mTargetState
                        + ", mVideoWidth=" + mVideoWidth + ", mVideoHeight=" + mVideoHeight);
                mSurfaceWidth = w;
                mSurfaceHeight = h;
                boolean isValidState =  (mTargetState == STATE_PLAYING);
                boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
                if (mMediaPlayer != null && isValidState && hasValidSize) {
                    if (mSeekWhenPrepared != 0) {
                        seekTo(mSeekWhenPrepared);
                    }
                    start();
                }
            }

            public void surfaceCreated(SurfaceHolder holder) {
                if (LOG) Log.v(TAG, "surfaceChanged(" + holder + ")");
                mSurfaceHolder = holder;
                openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // after we return from this we can't use the surface any more
                if (LOG) Log.v(TAG, "surfaceDestroyed(" + holder + ")");
                mSurfaceHolder = null;
                if (mMediaController != null) mMediaController.hide();
                release(true);
            }
        };
        getHolder().addCallback(mSHCallback);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure");
        int width = 0;
        int height = 0;
        switch (mScreenMode) {
        case SCREENMODE_BIGSCREEN:
            width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            height = getDefaultSize(mVideoHeight, heightMeasureSpec);
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                } else if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else {
                    //Log.i("@@@", "aspect ratio is correct: " +
                            //width+"/"+height+"="+
                            //mVideoWidth+"/"+mVideoHeight);
                }
            }
            break;
        case SCREENMODE_FULLSCREEN:
            width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            height = getDefaultSize(mVideoHeight, heightMeasureSpec);
//            if (width < height) {
//                MtkLog.w(TAG, "only support landscape! exchange width and height.");
//                int temp = width;
//                width = height;
//                height = temp;
//            }
            break;
        case SCREENMODE_CROPSCREEN:
            width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            height = getDefaultSize(mVideoHeight, heightMeasureSpec);
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //extend width to be cropped
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //extend height to be cropped
                    height = width * mVideoHeight / mVideoWidth;
                } else {
                    //do nothing
                }
            }
            break;
        default:
            MtkLog.w(TAG, "wrong screen mode : " + mScreenMode);
            break;
        }
        if (LOG) MtkLog.v(TAG, "onMeasure() set size: " + width + 'x' + height);
        if (LOG) MtkLog.v(TAG, "onMeasure() video size: " + mVideoWidth + 'x' + mVideoHeight);
        setMeasuredDimension(width, height);
        //when OnMeasure ok, start video.
        if (mVideoLayoutStatus == VideoLayoutStatus.WAIT) {
            mVideoLayoutStatus = VideoLayoutStatus.REDAY;
            mHander.sendEmptyMessage(MSG_START_VIDEO);
        }
    }
    
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (LOG) Log.v(TAG, "onTouchEvent(" + ev + ")");
//        if (mMediaController != null) {
//            toggleMediaControlsVisiblity();
//        }
//        return false;
//    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                                     keyCode != KeyEvent.KEYCODE_MENU &&
                                     keyCode != KeyEvent.KEYCODE_CALL &&
                                     keyCode != KeyEvent.KEYCODE_ENDCALL &&
                                     keyCode != KeyEvent.KEYCODE_CAMERA;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (event.getRepeatCount() == 0 && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else if (keyCode ==  KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || 
                    keyCode ==  KeyEvent.KEYCODE_MEDIA_NEXT ||
                    keyCode ==  KeyEvent.KEYCODE_MEDIA_PREVIOUS ||
                    keyCode ==  KeyEvent.KEYCODE_MEDIA_REWIND ||
                    keyCode ==  KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                    keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK) {
                //consume media action, so if video view if front,
                //other media player will not play any sounds.
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mIsOnlyAudio = true;
        mDuration = -1;
        setResumed(true);
        super.setVideoURI(uri, headers);
    }
    
    public void setVideoURI(Uri uri, Map<String, String> headers, boolean hasGotMetaData) {
        if (LOG) MtkLog.v(TAG, "setVideoURI(" + uri + ", " + headers + ")");
        //clear the flags
        mHasGotMetaData = hasGotMetaData;
        setVideoURI(uri, headers);
    }
    
    private void clearVideoInfo() {
        if (LOG) Log.v(TAG, "clearVideoInfo()");
        mHasGotPreparedCallBack = false;
        mVideoLayoutStatus = VideoLayoutStatus.UNKNOWN;
        //remove the wait messure message
        mHander.removeMessages(MSG_START_VIDEO);
    }
    
    @Override
    protected void openVideo() {
        if (LOG) Log.v(TAG, "openVideo() mUri=" + mUri + ", mSurfaceHolder=" + mSurfaceHolder
                + ", mSeekWhenPrepared=" + mSeekWhenPrepared + ", mMediaPlayer=" + mMediaPlayer
                + ", mOnResumed=" + mOnResumed);
        clearVideoInfo();
        if (!mOnResumed || mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        if ("".equalsIgnoreCase(String.valueOf(mUri))) {
            Log.w(TAG, "Unable to open content: " + mUri);
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            //end update status.
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            //mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            //attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        if (LOG) Log.v(TAG, "openVideo() mUri=" + mUri + ", mSurfaceHolder=" + mSurfaceHolder
                + ", mSeekWhenPrepared=" + mSeekWhenPrepared + ", mMediaPlayer=" + mMediaPlayer);
    }

    private void doPrepared(MediaPlayer mp) {
        if (LOG) MtkLog.v(TAG, "doPrepared(" + mp + ") start");
        mCurrentState = STATE_PREPARED;
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mMediaPlayer);
        }
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();

        int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
        }
        
        if (mTargetState == STATE_PLAYING) {
            start();
        }
        if (LOG) MtkLog.v(TAG, "doPrepared() end video size: " + mVideoWidth + "," + mVideoHeight
                + ", mTargetState=" + mTargetState + ", mCurrentState=" + mCurrentState);
    }
    
    public void setScreenMode(int screenMode) {
        mScreenMode = screenMode;
        this.requestLayout();
    }
    
    public int getScreenMode() {
        return mScreenMode;
    }
    
    private boolean mOnResumed;
    /**
     * surfaceCreate will invoke openVideo after the activity stoped.
     * Here set this flag to avoid openVideo after the activity stoped.
     * @param resume
     */
    public void setResumed(boolean resume) {
        if (LOG) MtkLog.v(TAG, "setResumed(" + resume + ") mUri=" + mUri + ", mOnResumed=" + mOnResumed);
        mOnResumed = resume;
    }
    
    @Override
    public void resume() {
        if (LOG) MtkLog.v(TAG, "resume() mTargetState=" + mTargetState + ", mCurrentState=" + mCurrentState);
        setResumed(true);
        openVideo();
    }
    
    @Override
    public void suspend() {
        if (LOG) MtkLog.v(TAG, "suspend() mTargetState=" + mTargetState + ", mCurrentState=" + mCurrentState);
        super.suspend();
    }
    
    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
        if (LOG) MtkLog.v(TAG, "setInfoListener(" + l + ")");
    }
    
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
        if (LOG) MtkLog.v(TAG, "setOnBufferingUpdateListener(" + l + ")");
    }
    
    @Override
    public int getCurrentPosition() {
        int position = 0;
        if (mSeekWhenPrepared > 0) {
            //if connecting error before seek,
            //we should remember this position for retry
            position = mSeekWhenPrepared;
        } else if (isInPlaybackState()) {
            position = mMediaPlayer.getCurrentPosition();
        }
        if (LOG) MtkLog.v(TAG, "getCurrentPosition() return " + position
                + ", mSeekWhenPrepared=" + mSeekWhenPrepared);
        return position;
    }
    
    //clear the seek position any way.
    //this will effect the case: stop video before it's seek completed.
    public void clearSeek() {
        if (LOG) MtkLog.v(TAG, "clearSeek() mSeekWhenPrepared=" + mSeekWhenPrepared);
        mSeekWhenPrepared = 0;
    }
    
    public boolean isOnlyAudio() {
        if (LOG) MtkLog.v(TAG, "isOnlyAudio() return " + mIsOnlyAudio);
        return mIsOnlyAudio;
    }
    
    public boolean isTargetPlaying() {
        if (LOG) Log.v(TAG, "isTargetPlaying() mTargetState=" + mTargetState);
        return mTargetState == STATE_PLAYING;
    }
    
    public boolean isTargetError() {
        if (LOG) Log.v(TAG, "isTargetError() mTargetState=" + mTargetState);
        return mTargetState == STATE_ERROR;
    }
    
    public void dump() {
        if (LOG) Log.v(TAG, "dump() mUri=" + mUri
                + ", mTargetState=" + mTargetState + ", mCurrentState=" + mCurrentState
                + ", mSeekWhenPrepared=" + mSeekWhenPrepared + ", mIsOnlyAudio=" + mIsOnlyAudio
                + ", mVideoWidth=" + mVideoWidth + ", mVideoHeight=" + mVideoHeight
                + ", mMediaPlayer=" + mMediaPlayer + ", mSurfaceHolder=" + mSurfaceHolder);
    }
    
    @Override
    public void seekTo(int msec) {
        if (LOG) Log.v(TAG, "seekTo(" + msec + ") isInPlaybackState()=" + isInPlaybackState());
        super.seekTo(msec);
    }
    
    @Override
    protected void release(boolean cleartargetstate) {
        if (LOG) Log.v(TAG, "release(" + cleartargetstate + ") mMediaPlayer=" + mMediaPlayer);
        super.release(cleartargetstate);
    }
    
    //for duration displayed
    public void setDuration(int duration) {
        if (LOG) Log.v(TAG, "setDuration(" + duration + ")");
        mDuration = (duration > 0 ? -duration : duration);
    }
    
    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        //mDuration = -1;
        return mDuration;
    }

}
