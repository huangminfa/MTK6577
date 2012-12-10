package com.mediatek.ngin3d;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;

import java.io.IOException;
import java.util.HashMap;

public class VideoProxy {
    private static final String TAG = "Ngin3d.VideoProxy";

    public static final int INVALID_SEGMENT_ID = -1;
    public static final int REPLAY_SEGMENT_ID = -2;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private Uri mUri;
    private MediaPlayer mPlayer;
    private HashMap<Integer, Pair<Integer, Integer>> mSegments;

    private int mSegmentId = INVALID_SEGMENT_ID;
    private int mDuration;
    private int mLoopStartMs;
    private int mLoopEndMs;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mCurrentState = STATE_IDLE;
    private int mIntentState = STATE_IDLE;
    private Handler mLoopHandler;
    private Runnable mLoopRunner;
    private boolean mEnableMusicPause;
    private boolean mEnableLooping;

    private final MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mediaPlayer) {
            setCurrentState(STATE_PREPARED);
            notifyPreparedListener(mediaPlayer);
            try {
                mVideoWidth = mediaPlayer.getVideoWidth();
                mVideoHeight = mediaPlayer.getVideoHeight();
                mDuration = mediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer object has been released. Exception : " + e);
                return;
            }

            // TODO : seek
            if (mVideoWidth == 0 || mVideoHeight == 0) {
                 // TODO : report size issue.
                if (mIntentState == STATE_PLAYING) {
                    start(mSegmentId);
                }
            } else {
                if (mIntentState == STATE_PLAYING) {
                    start(mSegmentId);
                }
            }
        }
    };

    private final MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            setAllState(STATE_PLAYBACK_COMPLETED);
            notifyCompletionListener(mediaPlayer);
        }
    };

    private final MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
            try {
                mVideoWidth = mediaPlayer.getVideoWidth();
                mVideoHeight = mediaPlayer.getVideoHeight();
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer object has been released. Exception : " + e);
                return;
            }
        }
    };

    private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mediaPlayer, int frameworkErr, int implErr) {
            setAllState(STATE_ERROR);
            if (notifyErrorListener(mediaPlayer, frameworkErr, implErr)) {
                return true;
            }
            return true;
        }
    };

    private Surface mSurface;
    private int mFrameCount;
    private volatile int mTotalFrameCount;
    private long mFrameCountingStart;
    private double mFPS;

    private final VideoTexture.SurfaceTextureListener mSurfaceTextureCallback = new VideoTexture.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
            mSurface = new Surface(surfaceTexture);
            openVideo();
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture) {
            final boolean isValidState = (mIntentState == STATE_PLAYING);
            if (mPlayer != null && isValidState) {
                // TODO : seek issue
                start(mSegmentId);
            }
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
            release(true);
            return true;
        }

        private static final boolean ENABLE_FPS_DUMP = true;
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            if (ENABLE_FPS_DUMP) {
                long now = System.nanoTime();
                if (mFrameCountingStart == 0) {
                    mFrameCountingStart = now;
                } else if ((now - mFrameCountingStart) > 1000000000) {
                    mFPS = (double) mFrameCount * 1000000000 / (now - mFrameCountingStart);
                    Log.v(TAG, "fps: " + mFPS);
                    mFrameCountingStart = now;
                    mFrameCount = 0;
                }
                ++mFrameCount;
                ++mTotalFrameCount;
            }
        }
    };

    // Send notification to client
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;

    Context mCtx;
    public VideoProxy(Context context, VideoTexture client) {
        mCtx = context;
        initializeView(client);
    }

    public boolean setLooping(final boolean enableLooping) {
        mEnableLooping = enableLooping;
        return mEnableLooping;
    }

    public boolean setBackgroundMusicPauseEnabled(final boolean pause) {
        mEnableMusicPause = pause;
        return mEnableMusicPause;
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, HashMap<Integer, Pair<Integer, Integer>> segments) {
        mUri = uri;
        mSegments = segments;
        prepareVideo();
    }

    public void prepareVideo() {
        openVideo();
    }

    private void initializeView(VideoTexture client) {
        mVideoWidth = 0;
        mVideoHeight = 0;
        client.setSurfaceTextureListener(mSurfaceTextureCallback);
        setAllState(STATE_IDLE);
    }

    private void openVideo() {
        if (mUri == null || mSurface == null) {
            return;
        }

        if (mEnableMusicPause) {
            sendMusicPauseRequest();
        }

        release(false);
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(mPreparedListener);
            mPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mPlayer.setOnCompletionListener(mCompletionListener);
            mPlayer.setOnErrorListener(mErrorListener);
            mPlayer.setDataSource(mCtx,  mUri);
            mPlayer.setLooping(true);
            mPlayer.setSurface(mSurface);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.prepareAsync();
            setCurrentState(STATE_PREPARING);
        } catch (IOException ex) {
            Log.e(TAG, "IOException : " + ex);
            setAllState(STATE_ERROR);
            mErrorListener.onError(mPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "IllegalArgumentException : " + ex);
            setAllState(STATE_ERROR);
            mErrorListener.onError(mPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    private void prepareLoopTimer(final int startMs, final int endMs) {
        if (mEnableLooping) {
            if (mLoopHandler == null) {
                mLoopHandler = new Handler();
            }

            mLoopStartMs = startMs;
            mLoopEndMs = endMs;
            if (mLoopRunner == null) {
                mLoopRunner = new Runnable() {
                    public void run() {
                        seekToAndStart(mLoopStartMs, mLoopEndMs);
                    }
                };
            }
            mLoopHandler.postDelayed(mLoopRunner, mLoopEndMs - mLoopStartMs);
        }
    }

    private void removeLoopTimer() {
        if (mLoopHandler != null && mLoopRunner != null) {
            mLoopHandler.removeCallbacks(mLoopRunner);
            mLoopRunner = null;
        }
    }

    public void start() {
        if (isPlayable()) {
            mPlayer.start();
            setCurrentState(STATE_PLAYING);
        }
        setIntentState(STATE_PLAYING);
    }

    private boolean isValidPeriod(final int startMs, final int endMs) {
        return (startMs <= endMs) && (startMs >= 0 && startMs <= mDuration) && (endMs >= 0 && endMs <= mDuration);
    }

    public void start(int segmentId) {
        if (segmentId != REPLAY_SEGMENT_ID) {
            mSegmentId = segmentId;
        }
        if (mSegments == null
            || segmentId == INVALID_SEGMENT_ID) {
            start();
        } else {
            final Pair seg = mSegments.get(mSegmentId);
            if (seg != null) {
                final int startMs = (Integer)seg.first;
                final int endMs = (Integer)seg.second;
                if (isValidPeriod(startMs, endMs)) {
                    seekToAndStart(startMs, endMs);
                }
            }
        }
        setIntentState(STATE_PLAYING);
    }

    private void
    seekToAndStart(final int startMs, final int endMs) {
        if (isPlayable()) {
            pause();
            seekTo(startMs);
            start();
            prepareLoopTimer(startMs, endMs);
        }
    }

    public void pause() {
        if (isPlayable() && isPlaying()) {
            removeLoopTimer();
            mPlayer.pause();
            setCurrentState(STATE_PAUSED);
        }
        setIntentState(STATE_PAUSED);
    }

    public void stopPlayback() {
        if (mPlayer != null) {
            removeLoopTimer();
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            setAllState(STATE_IDLE);
        }
    }

    public void seekTo(int mSec) {
        if (isPlayable()) {
            mPlayer.seekTo(mSec);
        }
    }

    public void release(boolean clearIntent) {
        if (mPlayer != null) {
            removeLoopTimer();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            setCurrentState(STATE_IDLE);
            if (clearIntent) {
                setIntentState(STATE_IDLE);
            }
        }
    }

    public boolean isPlaying() {
        return (isPlayable() && mPlayer.isPlaying());
    }

    private boolean isPlayable() {
        return (mPlayer != null
                && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE
                && mCurrentState != STATE_PREPARING);
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setmOnErrorListener(MediaPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private boolean notifyCompletionListener(MediaPlayer mediaplayer) {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mediaplayer);
            return true;
        }
        return false;
    }

    private boolean notifyPreparedListener(MediaPlayer mediaplayer) {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mediaplayer);
            return true;
        }
        return false;
    }

    private boolean notifyErrorListener(MediaPlayer mediaplayer, int frameworkErr, int implErr) {
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(mediaplayer, frameworkErr, implErr);
            return true;
        }
        return false;
    }

    private void sendMusicPauseRequest() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mCtx.sendBroadcast(i);
    }

    private void setCurrentState(int state) {
        mCurrentState = state;
    }

    private void setIntentState(int state) {
        mIntentState = state;
    }

    private void setAllState(int state) {
        setCurrentState(state);
        setIntentState(state);
    }
}
