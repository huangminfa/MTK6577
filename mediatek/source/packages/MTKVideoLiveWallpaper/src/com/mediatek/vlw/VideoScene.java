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

package com.mediatek.vlw;

import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.io.File;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageVolume;
import android.os.FileObserver;
import com.mediatek.xlog.Xlog;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

//import com.mediatek.tvout.TvOutVlw;
import com.mediatek.tvOut.TvOut;

import com.mediatek.vlw.Utils.LoopMode;

public class VideoScene {
	static final String TAG = "VideoScene";
	static final boolean DEBUG = true;
	static final String SHARED_PREFS_FILE = "vlw";
	static final String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
	static final String ACTION_PRE_SHUTDOWN = "android.intent.action.ACTION_PRE_SHUTDOWN";
	
	private final Context mContext;
	private final boolean mPreview;
	private int mStartTime;
	private int mEndTime;
	private int mCurrentPos;
	final WallpaperManager wpm;
	final WindowManager wm;
	// settable by the client
	private Uri mUri;
	
	// Restore the video selected last time when sdcard is ready if needed
	private Uri mPrevUri;
	private String mPrevBucketId;
	private int mPrevStartTime;
	private int mPrevEndTime;
	private int mPrevCurrentPos;
	
	private Map<String, String> mHeaders;
	private int mDuration;
	final SharedPreferences mSharedPref;
	final TvOut mTvOut;

	// video edit control
	static final int DEFAULT_START = 0;
	static final int DEFAULT_END = 600000;	// 10 min
	static final String WALLPAPER_URI = "uri";
	static final String START_TIME = "start";
	static final String END_TIME = "end";
	static final String CURRENT_POSITION = "pos";
	static final String BUCKET_ID = "bucketId";
	static final String WALLPAPER_WIDTH = "width";
	static final String WALLPAPER_HEIGHT = "height";
	
	// previous video info
	static final String WALLPAPER_URI_PREV = "uri_prev";
	static final String START_TIME_PREV = "start_prev";
	static final String END_TIME_PREV = "end_prev";
	static final String CURRENT_POSITION_PREV = "pos_prev";
	static final String BUCKET_ID_PREV = "bucketId_prev";	
	
	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	// mCurrentState is a VideoScene object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoScene object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private int sWallpaperWidth = -1;		// original wallpaper size, restore this
	private int sWallpaperHeight = -1;		// when exit vlw if it is changed
	
	private OnCompletionListener mOnCompletionListener;
	private OnPreparedListener mOnPreparedListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private long mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private int mStateWhenSuspended; // state before calling suspend()

	private static final int MSG_MONITOR_POSITION = 1;
	private static final int MSG_RELEASE_TIMER = 3;
	private static final int MSG_RELOAD_VIDEO = 4;
	private static final int MSG_CLEAR = 5;
	private static final int MSG_INVALIDVIDEO = 0;
	private static final int TIME_OUT = 1000;	//  every 1 sec to check if play to end
	private static final int RELOAD_TIME_OUT = 2000;
	private static final int CLEAR_TIME_OUT = 30000;
	private static final int RELEASE_TIME_OUT = 300000;	// if 5 min has no playing, then release all media resources
	private static final float WALLPAPER_SCREENS_SPAN = 1.25f;
	private static final float WALLPAPER_MAX_SCALE_PERCENT = 2.0f;
	
	// judge and support sepcial streaming type
	private int mStreamingType = -1;
	private static final int STREAMING_HTTP = 1;
	private static final int STREAMING_RTSP = 2;
	private static final int STREAMING_SDP = 3;
	private int mBufferWaitTimes = 0;
	private boolean mCanGetMetaData;
	private boolean mHaveGetPreparedCallBack;
	private static final int MEDIA_CANGETMETADATA = 803;
	private boolean mWaitingReload;
	private boolean mHasShutdown;
	private boolean mStartFromBoot = true;
	private int 	mMode;
	private LoopMode mLoopMode = LoopMode.ALL;
	private ArrayList<Uri> mUriList;
	private ArrayList<Uri> mUriInvalid;
	private String  mBucketId;
	private boolean mVisible;
	private ArrayList<String> mStoragesList;
	
	// for video observer
	private FileObserver mFileObserver;
	private String mObserverPath;
	private String mMovingFile;
	
	public VideoScene(Context context, SurfaceHolder holder, boolean isPreview) {
		mContext = context;
		mSharedPref = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        mTvOut = new TvOut();
		mSurfaceHolder = holder;
		mPreview = isPreview;
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
		wpm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		if (sWallpaperWidth <= 0 || sWallpaperHeight <= 0) {
			Display disp = wm.getDefaultDisplay();
			int dispW = disp.getWidth();
			int dispH = disp.getHeight();

			if(dispH > dispW){
			       sWallpaperWidth = dispW * 2;
			       sWallpaperHeight = dispH;
			}else{
				sWallpaperWidth = dispH * 2;
				sWallpaperHeight = dispW;
			}			
		}
		Xlog.i(TAG, "original wallpaper width=" + sWallpaperWidth + ", height=" + sWallpaperHeight);
	}

	private void relayout(int videoWidth, int videoHeight) {
    	Display disp = wm.getDefaultDisplay();
    	final int dispW = disp.getWidth();
    	final int dispH = disp.getHeight();
		int wpW = sWallpaperWidth;
		int wpH = sWallpaperHeight;
		final int curWpW = wpm.getDesiredMinimumWidth();
		final int curWpH = wpm.getDesiredMinimumHeight();
		Xlog.d(TAG, String.format("relayout, display: (%d,%d), video: (%d,%d), " +
				"previous wallpaper: (%d,%d)", dispW, dispH, videoWidth, videoHeight, 
				curWpW, curWpH));
		// If needed, restore wallpaper size to be normal size
		if (wpW != curWpW || wpH != curWpH) {
			Xlog.w(TAG, String.format("Wallpaper size changed, restore it: " +
					"(%d,%d)-->(%d,%d)", curWpW, curWpH, sWallpaperWidth, 
					sWallpaperHeight));
			wpm.suggestDesiredDimensions(sWallpaperWidth, sWallpaperHeight);
		}
		
		// just use the real suggested wallpaper surface size others wanted(Launcher)
		int surfaceW = videoWidth;
		int surfaceH = videoHeight;
		float scaleW = wpW / (float)videoWidth;
		float scaleH = wpH / (float)videoHeight;
		float scale = 1.0f;
		boolean relayoutSurface = false;
		boolean relayoutWallpaper = false;

		// Need to resize surface if video size is not the same with wallpaper, 
		// or need to reset previous value to fit current video size
		if ((wpW != videoWidth || wpH != videoHeight) || 
				(videoWidth != mSurfaceWidth || videoHeight != mSurfaceHeight)) {
			relayoutSurface = true;
			if (scaleW > scaleH) {
				scale = scaleW;
			} else {
				scale = scaleH;
			}
        }
		
		// must preserve the video width/height ratio
        surfaceW *= scale;
        surfaceH *= scale;
        
        float wr = (surfaceW - wpW) / (float)wpW;
        float hr = (surfaceH - wpH) / (float)wpH;
        float percent = hr > wr ? hr : wr;
        if (percent < WALLPAPER_MAX_SCALE_PERCENT/2) {
        	Xlog.d(TAG, "just scale video to fit wallpaper, percent=" + percent + ", scale=" + scale);
        } else if (percent < WALLPAPER_MAX_SCALE_PERCENT) {
        	Xlog.d(TAG, "big difference percent: " + percent + ", scale=" + scale + 
        			", reset wallpaper width to dispW * " + WALLPAPER_SCREENS_SPAN);
        	wpW = (int)(dispW * WALLPAPER_SCREENS_SPAN);
        	scaleW = wpW / (float)videoWidth;
    		// Need to resize surface if video size is not the same with wallpaper, 
    		// or need to reset previous value to fit current video size
    		if ((wpW != videoWidth || wpH != videoHeight) || 
    				(videoWidth != mSurfaceWidth || videoHeight != mSurfaceHeight)) {
    			relayoutSurface = true;
    			if (scaleW > scaleH) {
    				scale = scaleW;
    			} else {
    				scale = scaleH;
    			}
            }
			relayoutWallpaper = true;
			
        } else {
        	Xlog.e(TAG, "TODO: need to rotate the video, percent=" + percent + ", scale=" + scale);
			scale = 1.0f;
        }
        surfaceW = videoWidth;
        surfaceH = videoHeight;
        surfaceW *= scale;
        surfaceH *= scale;
        if (relayoutSurface) {
        	Xlog.d(TAG, String.format("resize surface: (%d,%d)-->(%d,%d)", 
        			mSurfaceWidth, mSurfaceHeight, surfaceW, surfaceH));
        	mSurfaceWidth = surfaceW;
        	mSurfaceHeight = surfaceH;
        	mSurfaceHolder.setFixedSize(surfaceW, surfaceH);
        }
        if (relayoutWallpaper) {
        	wpm.suggestDesiredDimensions(surfaceW, surfaceH);
        	Xlog.w(TAG, String.format("resize wallpaper: (%d,%d)-->(%d,%d)", 
        			curWpW, curWpH, surfaceW, surfaceH));
        }

	}
	
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_MONITOR_POSITION:
				long pos = getCurrentPosition();
				long duration = getDuration();
				if (duration == -1) {
					Message message = obtainMessage(MSG_MONITOR_POSITION);
					sendMessageDelayed(message, TIME_OUT);
				} else if (mEndTime < duration) {
					if (pos > mEndTime) {
						seekTo(mStartTime);
					}
					Message message = obtainMessage(MSG_MONITOR_POSITION);
					sendMessageDelayed(message, TIME_OUT);
				} else {
					removeMessages(MSG_MONITOR_POSITION);
				}
				break;

			case MSG_RELEASE_TIMER:
				// So long as RELEASE_TIME_OUT no playing, so release all resources
				release(false);
				removeMessages(MSG_RELEASE_TIMER);
				break;
				
			case MSG_RELOAD_VIDEO:
				/*if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
					mWaitingReload = false;
					removeMessages(MSG_RELOAD_VIDEO);
				} else {
					openVideo();
					start();
					Message message = obtainMessage(MSG_RELOAD_VIDEO);
					sendMessageDelayed(message, RELOAD_TIME_OUT);
				}*/
				String videoPath = mUri.getPath();
				if (!Utils.isExternalFileExists(videoPath)) {
					Message message = obtainMessage(MSG_RELOAD_VIDEO);
					sendMessageDelayed(message, RELOAD_TIME_OUT);
					Xlog.w(TAG, "cannot query video path, reload it in " + RELOAD_TIME_OUT/1000 + "sec");
				} else {
					mWaitingReload = false;
					removeMessages(MSG_RELOAD_VIDEO);
					removeMessages(MSG_CLEAR);
					// seek to the right position when we leave
					// we should not do this in preview mode
					if (!mPreview) {
						mSeekWhenPrepared = mCurrentPos;
					}
					openVideo();
					if (mVisible) {
	                	start();
	                }
				}
				break;
				
			case MSG_CLEAR:
				removeMessages(MSG_CLEAR);
				removeMessages(MSG_RELOAD_VIDEO);
				// avoid delayed boot broadcast message and others
				if(!mWaitingReload){
					return;
				}
				Xlog.w(TAG, "MSG_CLEAR sdcard removed, play default video");
				mWaitingReload = false;
				Utils.showInfo(mContext, R.string.VideoScene_error_text_unknown, true);
                release(false);
                clear(true, true);
                openVideo();
                if (mVisible) {
                	start();
                }
				break;
				
			case MSG_INVALIDVIDEO:
				Xlog.d(TAG, "find the invalid video");
				mUriList = Utils.getUrisFromBucketId(mContext, mBucketId);
            	if (mUriList.isEmpty()) {
            		Xlog.i(TAG, "invalid video folder, play the default video");
                    release(false);
                    clear(true, true);
                    openVideo();
                    if (mVisible) {
                    	start();
                    }
            	} else {
            		if (mUriInvalid == null) {
            			mUriInvalid = new ArrayList<Uri>();
    				}
            		// This make the new invalid Uri as the last item
            		if (mUri != null && !mUriInvalid.contains(mUri)) {
    					mUriInvalid.add(mUri);
    				}
            		
            		Xlog.i(TAG, "video playing is removed or invalid in selected folder, play another video");
            		release(false);
            		mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
            		if (mMode >= 0) {
    					mUri = mUriList.get(mMode);
    				} else {
    					Xlog.d(TAG, "No valid video in this folder, play default video, size=" + mUriList.size());
    					clear(true, true);
    					mMode = 0;
    				}
            		// save in SharedPref so that play video correctly if user
            		// delete the playing video then reboot device by taking
            		// out battery
            		saveSettings();
            		openVideo();
            	}
				removeMessages(MSG_INVALIDVIDEO);
			default:
				Xlog.w(TAG, "unknown message " + msg.what);
				break;
			}
		}
	};

	final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Xlog.v(TAG,"mShutdownReceiver intent = "+intent.getAction());
			if (Intent.ACTION_SHUTDOWN.equals(intent.getAction()) || Intent.ACTION_REBOOT.equals(intent.getAction())
					|| ACTION_PRE_SHUTDOWN.equals(intent.getAction())) {
				if (DEBUG) {
					Xlog.i(TAG, "shutdown: ACTION_SHUTDOWN, mUri = " + mUri +", intent.getAction() == " +intent.getAction() );
				}
				// ipo reboot issue. after shutdown we would not response to MSG_CLEAR and MSG_VIDEO_RELOAD
				// we may miss ACTION_IPO_BOOT
				mHandler.removeMessages(MSG_RELOAD_VIDEO);
				
				mHasShutdown =  true;
				// IPO issue, init() is missing
				mStartFromBoot = true;
				// save current state and try to restart video wallpaper when next start up
				saveSettings();
				release(true);
				// Must restart FileObserver if needed
				stopAndReleaseVideoObserver();
				
			} else if (ACTION_BOOT_IPO.equals(intent.getAction())) {
				// IPO issue, init() is missing
				mStartFromBoot = true;
				mHasShutdown = false;

            	if (!Utils.isDefaultVideo(mUri) && !isInPlaybackState()) {
            		// only when first time ACTION_MEDIA_MOUNTED that MSG_RELOAD_VIDEO will be sent.
            		if (!mHandler.hasMessages(MSG_CLEAR)) {
            			mHandler.sendEmptyMessageDelayed(MSG_CLEAR, CLEAR_TIME_OUT);
            		}
            	}
			}
		}
	};
	
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        // when sdcard is removed, play the default video
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
        	final StorageVolume sv = (StorageVolume) intent.getExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
        	String path = null;
        	if (sv != null) {
        		path = sv.getPath();
        	}
        	if (path == null) {
				Uri data = intent.getData();
				if (data != null && data.getScheme().equals("file")) {
					path = data.getPath();
				}
			}
        	// Need to use '/' at the end to verify sdcard or sdcard2
        	path += "/";
			Xlog.i(TAG,"Receive intent action=" + action + " path=" + path);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            	if (mStoragesList == null) {
            		mStoragesList = new ArrayList<String>(2);
            	}
            	if (path != null && !mStoragesList.contains(path)) {
            		mStoragesList.add(path);
            	}

            	if ((mMediaPlayer == null || !mMediaPlayer.isPlaying())
            			&& mStartFromBoot && path != null && mUri.getPath().contains(path)) {
            		// only when first time ACTION_MEDIA_MOUNTED that MSG_RELOAD_VIDEO will be sent.
                	if (!Utils.isDefaultVideo(mUri)) {
            			if (!mHandler.hasMessages(MSG_RELOAD_VIDEO)) {
            				mHandler.sendEmptyMessage(MSG_RELOAD_VIDEO);
            			}
						mWaitingReload = true;
                	}
                	
            	} else if (mPrevUri != null){
            		String videoPath = mPrevUri.getPath();
            		String swapPath = Utils.swapSdcardUri(mPrevUri).getPath();
            		// TODO: need to translate the BucketId when sdcard-swap happen
                	if (mPrevBucketId != null) {
                		mBucketId = mPrevBucketId;
                		ArrayList<Uri> uris = Utils.getUrisFromBucketId(mContext, mPrevBucketId);
                		if (!uris.isEmpty()) {
                			mUriList = uris;
                			if (Utils.isExternalFileExists(videoPath)) {
                				for (int index = 0; index < uris.size(); index++) {
                					if (uris.get(index).equals(mPrevUri)) {
                						mMode = index;
                						break;
                					}
                				}
                				mUri = mPrevUri;
                				mCurrentPos = mPrevCurrentPos;

                			} else {
                				mMode = 0;
                				mUri = mUriList.get(mMode);
                				mCurrentPos = 0;
                				Xlog.w(TAG, "file does not exists, play next " + mUri);
                			}

                		} else if (Utils.isExternalFileExists(videoPath)){
                			// MediaScanner is scanning now, cannot query videos
                			// just play mPrevUri and query it again later
                			mUri = mPrevUri;
                    		mStartTime = mPrevStartTime;
                    		mEndTime = mPrevEndTime;
                    		mCurrentPos = mPrevCurrentPos;
                    		
                		} else if (Utils.isExternalFileExists(swapPath)) {
                            mUri = Uri.fromFile(new File(swapPath));
                            mBucketId = null;
                            mStartTime = mPrevStartTime;
                            mEndTime = mPrevEndTime;
                            mCurrentPos = mPrevCurrentPos;
                            
                        } else {
                			// Cannot query videos from mPrevBucketId and 
                			// mPrevUri doesn't exist, cannot wait
                			Xlog.w(TAG, "cannot reload videos selected last time");
                			return;
                		}

                	} else if (Utils.isExternalFileExists(videoPath)){
                		mUri = mPrevUri;
                		mBucketId = null;
                		mStartTime = mPrevStartTime;
                		mEndTime = mPrevEndTime;
                		mCurrentPos = mPrevCurrentPos;

                	} else if (Utils.isExternalFileExists(swapPath)) {
                	    mUri = Uri.fromFile(new File(swapPath));
                        mBucketId = null;
                        mStartTime = mPrevStartTime;
                        mEndTime = mPrevEndTime;
                        mCurrentPos = mPrevCurrentPos;
                        
                	} else {
                		Xlog.w(TAG, "video file selected last time does not exists");
                		return;
                	}
                	Xlog.i(TAG,"Restore the video last time. mPrevUri=" + mPrevUri 
                			+ " mPrevBucketId=" + mPrevBucketId + " mPrevStartTime=" 
                			+ mPrevStartTime + " mPrevEndTime=" + mPrevEndTime
                			+ " mPrevCurrentPos=" + mPrevCurrentPos);
                	// clean up and save current state
                	mPrevUri = null;
                	mPrevBucketId = null;
                	Editor edit = mSharedPref.edit();
                	edit.putString(BUCKET_ID, mBucketId);
                	edit.putString(WALLPAPER_URI, mUri.toString());
                	edit.putLong(CURRENT_POSITION, mCurrentPos);
                	if (mBucketId == null) {
                		edit.putLong(START_TIME, mStartTime);
                		edit.putLong(END_TIME, mEndTime);
                	}
                	edit.commit();
                	// seek to the right position when we leave
    				// we should not do this in preview mode
    				if (!mPreview) {
    					mSeekWhenPrepared = mCurrentPos;
    				}
                	openVideo();
                	
                	// when sdcard being mounted, the status mHasShutdown should be false.
                	mHasShutdown = false;
            	}

            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || 
            		Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action) || 
            		Intent.ACTION_MEDIA_REMOVED.equals(action) || 
            		Intent.ACTION_MEDIA_EJECT.equals(action)) {
            	if (mHasShutdown) {
            		Xlog.i(TAG, "Has been shutdown, Ignore");
            		return;
            	}
            	if (mStoragesList != null && path != null && mStoragesList.contains(path)) {
            		mStoragesList.remove(path);
            	}
            	String videoPath = mUri.getPath();
            	if (videoPath != null && path != null && videoPath.contains(path)) {
            		Xlog.w(TAG, "action: " + action + " revert to default video. sdcard path: " + path 
            				+ " absolute path: " + videoPath + " mUri: " + mUri);
            		Utils.showInfo(mContext, R.string.VideoScene_error_sdcard_unmounted, true);
            		// Save current sdcard video state, restore it when sdcard
            		// is ready if needed
            		mPrevBucketId = mBucketId;
            		mPrevUri = mUri;
            		mPrevStartTime = mStartTime;
            		mPrevEndTime = mEndTime;
            		mPrevCurrentPos = getCurrentPosition();
            		
            		stopAndReleaseVideoObserver();
                    release(false);
                    clear(true, true);
                    openVideo();
                    if (mVisible) {
                    	start();
                    }
            	}
                
            }
        }
    };
    
    private boolean checkMediaState() {
    	boolean res = false;
    	String state = Environment.getExternalStorageState();
    	
		 if (Environment.MEDIA_REMOVED.equals(state) || 
				 Environment.MEDIA_BAD_REMOVAL.equals(state) || 
				 Environment.MEDIA_UNMOUNTABLE.equals(state) ||
				 Environment.MEDIA_NOFS.equals(state)) {
			 // no sdcard, so set default video 
			 Xlog.i(TAG, "check sdcard state: " + state);
			 res = false;
		 } else if (Environment.MEDIA_UNMOUNTED.equals(state) ||
				 Environment.MEDIA_CHECKING.equals(state) ||
				 Environment.MEDIA_MOUNTED.equals(state) || 
				 Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ||
				 Environment.MEDIA_SHARED.equals(state)) {
			// has sdcard, but has not ready and mounted now, so wait for it
			 Xlog.i(TAG, "check sdcard state: " + state);
			 res = true;
		 } else {
			 Xlog.w(TAG, "check sdcard state, uncaught sdcard state: " + state);
			 res = false;
		 }
		 
		 return res;
    }

	public void setVisibility(boolean visible) {
		// Just do this when the visibility is really changed, because SCREEN_ON/OFF
		// will be also converted to onVisibilityChanged() by WMS for WallpaperService
		if (DEBUG) {
			Xlog.d(TAG, "setVisibility(" + visible + ")");
		}
		if (mVisible != visible) {
			mVisible = visible;
			if (visible) {
				// restore the up-to-date state info
				checkEnvironment();
				addAndStartVideoObserver();
				// seek to the right position when we leave
				// we should not do this in preview mode
				if (!mPreview) {
					mSeekWhenPrepared = mCurrentPos;
				}
				// if this called following init() at the first time
				start();
				
			} else {
				// remember the current position when we leave
				// we should not do this in preview mode
				if (!mPreview) {
					saveSettings();
				}
				pause();
				// release the MediaPlayer
				release(true);
				if (mBucketId == null) {
					mHandler.removeMessages(MSG_MONITOR_POSITION);
				}
			}

		}
	}

	private void loadSettings() {
		Uri uri = Uri.parse(mContext.getResources().getString(R.string.default_video_path));
		if (mSharedPref == null) {
			mUri = uri;
			mStartTime = DEFAULT_START;
			mEndTime = DEFAULT_END;
			mCurrentPos = DEFAULT_START;
			
		} else {
			mBucketId = mSharedPref.getString(BUCKET_ID, null);
			String uriString = mSharedPref.getString(WALLPAPER_URI, uri.toString());
			mUri = Uri.parse(uriString);
			mStartTime = (int)mSharedPref.getLong(START_TIME, DEFAULT_START);
			mEndTime = (int)mSharedPref.getLong(END_TIME, DEFAULT_END);
			mCurrentPos = (int)mSharedPref.getLong(CURRENT_POSITION, DEFAULT_START);
			
			//load previous info
			mPrevBucketId = mSharedPref.getString(BUCKET_ID_PREV, null);
			String prevUriString = mSharedPref.getString(WALLPAPER_URI_PREV, null);
			if (prevUriString != null){
				mPrevUri = Uri.parse(prevUriString);
			} else {
				mPrevUri = null;
			}
			mPrevStartTime = (int)mSharedPref.getLong(START_TIME_PREV, DEFAULT_START);
			mPrevEndTime = (int)mSharedPref.getLong(END_TIME_PREV, DEFAULT_END);
			mPrevCurrentPos = (int)mSharedPref.getLong(CURRENT_POSITION_PREV, DEFAULT_START);
		}
		Xlog.i(TAG, String.format("restore shared_prefs, " +
				"mStartTime=%d, mEndTime=%d, mCurrentPos=%d, mUri=%s, mBucketId=%s", 
				mStartTime, mEndTime, mCurrentPos, mUri.toString(), mBucketId));
		if (mBucketId != null) {
			// If come here from boot start, this will fail because sd card is not ready now
			// Should reload uris again somewhere!
			mUriList = Utils.getUrisFromBucketId(mContext, mBucketId);
			for (int index = 0; index < mUriList.size(); index++) {
				if (mUriList.get(index).equals(mUri)) {
					mMode = index;
					break;
				}
			}
		}
	}

	/**
	 * clear all current state and the shared preference to default
	 * called when media file is removed or user set static wallpaper
	 * @hide
	 */
	private void clear(boolean clearPrefs, boolean clearBucketId) {
		if (clearBucketId) {
			mBucketId = null;
			if (mUriList != null) {
				mUriList.clear();
			}
			if (mUriInvalid != null) {
				mUriInvalid.clear();
			}
		}
		mUri = Uri.parse(mContext.getResources().getString(R.string.default_video_path));
		mStartTime = DEFAULT_START;
		mEndTime = DEFAULT_END;
		mCurrentPos = DEFAULT_START;
		
		if (clearPrefs) {
			if (mSharedPref == null) {
				if (DEBUG) {
					Xlog.e(TAG, "we lost the shared preferences");
				}
				return;
			}
			Editor edit = mSharedPref.edit();
			edit.putString(BUCKET_ID, mBucketId);
			edit.putString(WALLPAPER_URI, mUri.toString());
			edit.putLong(START_TIME, (long)mStartTime);
			edit.putLong(END_TIME, (long)mEndTime);
			edit.putLong(CURRENT_POSITION, (long)mCurrentPos);
			
			//save previous info
			edit.putString(BUCKET_ID_PREV, mPrevBucketId);
			if (mPrevUri != null){
				edit.putString(WALLPAPER_URI_PREV, mPrevUri.toString());
			} else {
				edit.putString(WALLPAPER_URI_PREV, null);
			}
			edit.putLong(START_TIME_PREV, (long)mPrevStartTime);
			edit.putLong(END_TIME_PREV, (long)mPrevEndTime);
			edit.putLong(CURRENT_POSITION_PREV, mPrevCurrentPos);
			
			edit.commit();
			if (DEBUG) {
				Xlog.i(TAG, "clean(), reset the default state into shared_prefs");
			}
		}
		
	}
	
	private void saveSettings() {
		Editor edit = mSharedPref.edit();
		mCurrentPos = getCurrentPosition();
		edit.putString(WALLPAPER_URI, mUri.toString());
		edit.putLong(CURRENT_POSITION, (long)mCurrentPos);
		edit.commit();
		if (DEBUG) {
			Xlog.i(TAG, String.format("save current position: %d, current Uri: %s", mCurrentPos, mUri));
		}
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
	}

	public void startPlayback() {
		// seek to the right position when we leave
		// we should not do this in preview mode
		if (!mPreview) {
			mSeekWhenPrepared = mCurrentPos;
		}
		openVideo();
		// set mTargetState = STATE_PLAYING to play the video when it prepared
		start();
		// if user set end-time, we should stop at that time and restart again from start-time
		// No need to monitor folder videos
		if (mBucketId == null) {
			mHandler.sendEmptyMessage(MSG_MONITOR_POSITION);
		}
	}

	public void stopPlayback() {
		// remember the current position when we leave
		// we should not do this in preview mode
		if (!mPreview) {
			saveSettings();
		}
		// release all resources
		release(false);
		if (mBucketId == null) {
			mHandler.removeMessages(MSG_MONITOR_POSITION);
		}
	}

	private void judgeStreamingType() {
		if (mUri == null) {
			if (DEBUG) {
				Xlog.w(TAG, "mUri is null, cannot judge streaming type.");
			}
			return;
		}
		if (DEBUG) {
			Xlog.i(TAG, "judgeStreamingType, mUri=" + mUri);
		}
		String scheme = mUri.getScheme();
		mCanGetMetaData = false;
		if ("rtsp".equalsIgnoreCase(scheme)) {
			if (mUri.toString().toLowerCase(Locale.ENGLISH).endsWith(".sdp")) {
				mStreamingType = STREAMING_SDP;
				if (DEBUG) {
					Xlog.i(TAG, "SDP streaming type.");
				}
			} else {
				mStreamingType = STREAMING_RTSP;
				if (DEBUG) {
					Xlog.i(TAG, "RTSP streaming type.");
				}
			}
		} else if ("http".equalsIgnoreCase(scheme)) {
			mStreamingType = STREAMING_HTTP;
			mCanGetMetaData = true;
			if (DEBUG) { 
				Xlog.i(TAG, "HTTP streaming type.");
			}
		} else {
			// the local video can get meta data.
			// only streaming must wait.
			mCanGetMetaData = true;
			if (DEBUG) {
				Xlog.i(TAG, "Local Video streaming type.");
			}
		}
	}

	private void openVideo() {
		judgeStreamingType();
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			
			if (DEBUG) {
				Xlog.i(TAG, "open video path: " + mUri);
				Xlog.i(TAG, "context: " + mContext);
			}
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
			mMediaPlayer.setVolume(0, 0);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			// attachMediaController();
		} catch (IOException ex) {
			Xlog.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Xlog.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}

	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			if (width != 0 && height != 0) {
				if (DEBUG) {
					Xlog.i(TAG, "OnVideoSizeChangedListener, width=" + width + ",height=" + height + "," +
							"mVideoWidth=" + mVideoWidth + ",mVideoHeight=" + mVideoHeight);
				}
				if (mVideoWidth != width || mVideoHeight != height) {
					Xlog.d(TAG, "Video size changed (" + mVideoWidth + "/" + mVideoHeight + 
							")->(" + width + "/" + height + "), relayout surface");
					mVideoWidth = width;
					mVideoHeight = height;
					relayout(width, height);
				}
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			mHaveGetPreparedCallBack = true;
			if (mCanGetMetaData) {
				// if can get metadata, do it.
				doPrepared(mp);
			} else {
				// wait can get metadata info to do prepare.
			}
			if (DEBUG) {
				Xlog.i(TAG, "onPrepared");
				Xlog.i(TAG, "can get metadata:" + String.valueOf(mCanGetMetaData));
			}
		}
	};

	private final MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (what == MEDIA_CANGETMETADATA) {
				mCanGetMetaData = true;
				if (mHaveGetPreparedCallBack) {
					doPrepared(mp);
				} else {
					// wait prepare to do prepare.
				}
				return true;
			}
			else if (what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_SUPPORTED){
				mHandler.sendEmptyMessage(MSG_INVALIDVIDEO);
				Xlog.v(TAG,"OnInfoListener found MEDIA_INFO_VIDEO_NOT_SUPPORTED");
				if (mTargetState == STATE_PLAYING) {
					start();
				}
				return true;
			}
			return false;
		}
	};

	private void doPrepared(MediaPlayer mp) {
		if (DEBUG) {
			Xlog.i(TAG, "do prepared.");
		}
		mCurrentState = STATE_PREPARED;
		
		mStartFromBoot = false;
		mHandler.removeMessages(MSG_CLEAR);
		
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
		} else {
			mCanPause = mCanSeekBack = mCanSeekForward = true;
		}

		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mMediaPlayer);
		}

		long seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
												// changed after seekTo() call
		if (seekToPosition != 0) {
			seekTo(seekToPosition);
		}
		final int duration = mp.getDuration();
		mEndTime = (mEndTime == VideoScene.DEFAULT_END) ? duration : mEndTime;
		final int width = mp.getVideoWidth();
		final int height = mp.getVideoHeight();
		if (width != 0 && height != 0) {
			Xlog.i(TAG, "doPrepared, video size: " + width + "/" + height);
			if (mVideoWidth != width || mVideoHeight != height) {
				Xlog.d(TAG, "Video size changed (" + mVideoWidth + "/" + mVideoHeight + 
						")->(" + width + "/" + height + "), relayout surface");
				mVideoWidth = width;
				mVideoHeight = height;
				relayout(mVideoWidth, mVideoHeight);
			}
			
			if (mTargetState == STATE_PLAYING && mVisible) {
				start();
			}
		} else {
			// We don't know the video size yet, but should start anyway.
			// The video size might be reported to us later.
			//Xlog.v(TAG,"BLOCK HERE>>>>> width = "+width+", height = "+height+", mTargetState = "+mTargetState);
			if (mTargetState == STATE_PLAYING && mVisible) {
				start();
			}
		}
	}

	private final MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			// Reload uris again
			if (mBucketId != null || (mUriList != null && mUriList.isEmpty())) {
				mUriList = Utils.getUrisFromBucketId(mContext, mBucketId);
				Xlog.d(TAG, "getUrisFromBucketId: " + mUriList.size() + " videos");
			}
			Xlog.d(TAG, "onCompletion() mLoopMode=" + mLoopMode + " mBucketId=" + mBucketId +
					", mUri=" + mUri + ", mMode=" + mMode);
			if (mBucketId != null && mUriList.size() > 1) {
				// clear all state info except for mBucketId
				clear(false, false);
				
				mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
				if (mMode >= 0) {
					mUri = mUriList.get(mMode);
				} else {
					clear(true, true);
					mMode = 0;
				}
				// Must do this for start(), it assume mUri in SharedPref can be changed only by user
				Editor editor = mSharedPref.edit();
				editor.putString(WALLPAPER_URI, mUri.toString());
				editor.apply();
				// Make sure MediaPlayerService has disconnected from SurfaceTexture
				release(false);
				startPlayback();
				
			} else {
				// loop mode
				seekTo(mStartTime);
				start();
			}
		}
	};

	// Note: mErrorListener is default and mOnErrorListener is one given by user
	// to handle what they want
	private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Xlog.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;

			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
					return true;
				}
			}

			int messageId;

			if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
				messageId = R.string.VideoScene_error_text_invalid_progressive_playback;
			} else {
				messageId = R.string.VideoScene_error_text_unknown;
			}
			if (mContext == null) {
				Xlog.e(TAG, "mContext is null");
				return true;
			}
			
			if (!mStartFromBoot && !mWaitingReload) {
				Utils.showInfo(mContext, messageId, true);
			}
			// revert to static wallpaper if error happens on default video
			if (Utils.isDefaultVideo(mUri)) {
				Utils.showInfo(mContext, R.string.VideoScene_error_text_unknown, true);
                mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
				            release(true);
                            openVideo();
                        }
                }, RELOAD_TIME_OUT);
				return true;
			}
			
			boolean hasSDCard = checkMediaState();
			Xlog.i(TAG, "mStartFromBoot=" + mStartFromBoot + ", has sdcard: "
					+ hasSDCard + ", mHasShutdown=" + mHasShutdown 
					+ ", mUri=" + mUri + ", mBucketId=" + mBucketId);

			if ((mStartFromBoot && hasSDCard) || mHasShutdown) {
				release(false);
				if (!mWaitingReload) {
					Utils.showInfo(mContext, R.string.VideoScene_reload_after_mount, true);
					Xlog.w(TAG, "Start from boot and has sdcard, wait for its preparing");
					mWaitingReload = true;
				}
				return true;

			} else if (mBucketId != null && hasSDCard) {
				// update mUriList
				mUriList = Utils.getUrisFromBucketId(mContext, mBucketId);
				if (mUriList.isEmpty()) {
					Xlog.i(TAG, "invalid video folder, play the default video");
					release(false);
					clear(true, true);
					openVideo();
					start();
				} else {
					if (mUriInvalid == null) {
						mUriInvalid = new ArrayList<Uri>();
					}
					// This make the new invalid Uri as the last item
					if (mUri != null && !mUriInvalid.contains(mUri)) {
						mUriInvalid.add(mUri);
					}

					Xlog.i(TAG, "video playing is removed or invalid in selected folder, play another video");
					release(false);
					mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
					if (mMode >= 0) {
						mUri = mUriList.get(mMode);
					} else {
						Xlog.d(TAG, "No valid video in this folder, play default video, size=" + mUriList.size());
						clear(true, true);
						mMode = 0;
					}
					// save in SharedPref so that play video correctly if user
					// delete the playing video then reboot device by taking
					// out battery
					saveSettings();

					openVideo();
					start();
				}
			} else {
				Xlog.w(TAG, "media file doesn't exist, play the default video");
				release(false);
				clear(true, true);
				openVideo();
				start();
			}

			return true;
		}
	};

	private final MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
			if (DEBUG) {
				Xlog.i(TAG, "Buffering percent: " + percent);
				Xlog.i(TAG, "mBufferWaitTimes: " + mBufferWaitTimes);
			}
			// Here wait for the correct status to update the UI.
			if (mBufferWaitTimes > 0) {
				if (percent == 100) {
					mBufferWaitTimes -= 1;
					if (DEBUG)
						Xlog.i(TAG, "mBufferWaitTimes: " + mBufferWaitTimes);
				}
				return;
			}
			updateBufferState(percent);
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
		}
	};

	private void updateBufferState(int percent) {
		if (mStreamingType == STREAMING_RTSP) {
			if (percent >= 100) {
				mCanPause = true;
				mCanSeekBack = true;
				mCanSeekForward = true;
				// TODO we should hide buffer state on the progressive bar here
				// if needed
			} else {
				mCanPause = false;
				mCanSeekBack = false;
				mCanSeekForward = false;
				// TODO we should show buffer state on the progressive bar here
				// if needed
			}
		} else if (mStreamingType == STREAMING_SDP) {
			// TODO we should show buffer state on the progressive bar here if
			// needed
		}
	}

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoScene will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		mOnBufferingUpdateListener = l;
	}

	public void resize(SurfaceHolder holder, int w, int h) {
		if (DEBUG) {
			Xlog.d(TAG, "onSurfaceChanged(" + w + "," + h + "), sh: " + holder);
		}
	}

	public void init(SurfaceHolder holder) {
		if (DEBUG) {
			Xlog.i(TAG, "init VideoScene, sh: " + holder);
		}
		mStartFromBoot = true;
		
		loadSettings();
		mSurfaceHolder = holder;

		// seek to the right position saved last time
		// we should not do this in preview mode
		if (!mPreview) {
			mSeekWhenPrepared = mStartTime;
		}
		openVideo();
		
		if (mReceiver != null && mContext != null) {
			if (DEBUG) {
				Xlog.i(TAG, "register receiver: " + mReceiver);
			}
			IntentFilter filter = new IntentFilter();
			// when sdcard is available. Can not guarantee receiving this Broadcast
			// if sdcard mounted before VideoScene is started
			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			// when sdcard is unavailable
			filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
			filter.addAction(Intent.ACTION_MEDIA_REMOVED);
			filter.addAction(Intent.ACTION_MEDIA_EJECT);
	        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	        filter.addDataScheme("file");
	        // when external storage scanning complete
	        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
	        
			mContext.registerReceiver(mReceiver, filter);
		}
		
		/* 
		 * Because if we want to receive unmount broadcast, we should addDataScheme("file") 
		 * to filter,but this will lead we can not receive the shut down broadcast, so we 
		 * register a new receiver to deal with the shut down broadcast.
		 */
		if (mShutdownReceiver != null && mContext != null) {
			IntentFilter filter = new IntentFilter();
			// saveSettings when shut down
			filter.addAction(Intent.ACTION_SHUTDOWN);
			filter.addAction(ACTION_PRE_SHUTDOWN);
			filter.addAction(ACTION_BOOT_IPO);
			filter.addAction(Intent.ACTION_REBOOT);
			mContext.registerReceiver(mShutdownReceiver, filter);
		}

        mTvOut.disableVideoMode(true);
        Xlog.w(TAG,"start vlw, disable video mode");
        
        if (!Utils.isDefaultVideo(mUri) && !isInPlaybackState()) {
    		// only when first time ACTION_MEDIA_MOUNTED that MSG_RELOAD_VIDEO will be sent.
    		if (!mHandler.hasMessages(MSG_CLEAR)) {
    			mHandler.sendEmptyMessageDelayed(MSG_CLEAR, CLEAR_TIME_OUT);
    		}
    	}
	}

	public void destroy() {
		mStartFromBoot = false;
		// after we return from this we can't use the surface any more
		mSurfaceHolder = null;
		release(true);
		// clear all info saved in shared_prefs when we destroy the Video Live
		// wallpaper
		if (!mPreview) {
			clear(true, true);
		}
		
		stopAndReleaseVideoObserver();
		
		if (DEBUG) {
			Xlog.i(TAG, "destroy VideoScene");
		}
		
		if (mReceiver != null && mContext != null) {
			if (DEBUG) {
				Xlog.i(TAG, "unregister receiver: " + mReceiver);
			}
			mContext.unregisterReceiver(mReceiver);
		}
		
		if (mShutdownReceiver != null && mContext != null) {
			mContext.unregisterReceiver(mShutdownReceiver);
		}

        mTvOut.disableVideoMode(false);
        Xlog.w(TAG,"exit vlw, enable video mode");
        final int curWidth = wpm.getDesiredMinimumWidth();
        final int curHeight = wpm.getDesiredMinimumHeight();
        if (sWallpaperWidth != curWidth || sWallpaperHeight != curHeight) {
        	Xlog.i(TAG, String.format("restore wallpaper dimensions: (%d,%d)-->(%d,%d)", 
        			curWidth, curHeight, sWallpaperWidth, sWallpaperHeight));
        	wpm.suggestDesiredDimensions(sWallpaperWidth, sWallpaperHeight);
        }
	}

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			MediaPlayer temp = mMediaPlayer;
			mMediaPlayer = null;
			temp.stop();
			temp.release();
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

    private void checkEnvironment() {
        Uri uri = null;
        String buckekId = null;
       
        try {
            uri = Uri.parse(mSharedPref.getString(WALLPAPER_URI, null));
            buckekId = mSharedPref.getString(BUCKET_ID, null);
        } catch (NullPointerException e) {
            // ignore
            Xlog.e(TAG, "Read in SharedPref failed");
        }

        boolean videoChangeOrReopen = buckekId != null;
        if (mMediaPlayer == null && !mWaitingReload) {
            // seek to the right position when we leave
            // we should not do this in preview mode
            if (!mPreview) {
                mSeekWhenPrepared = mCurrentPos;
            }
            Xlog.i(TAG, "checkEnvironment() MediaPlayer released, open video");
            openVideo();

        } else if (buckekId != null && !buckekId.equals(mBucketId)) {
            Xlog.i(TAG, "checkEnvironment() select folder from sharedpref: " + buckekId);
            clear(false, true);
            mBucketId = buckekId;
            mUri = uri;
            videoChangeOrReopen = true;
            openVideo();

        } else if (uri != null && !mUri.equals(uri)) {
            Xlog.i(TAG, "checkEnvironment() change video from sharedpref: " + uri);
            clear(false, true);
            mUri = uri;
            videoChangeOrReopen = true;
            
            // Need not reserve the selected sdcard video info last time now, 
            // because user want to set another video
            mPrevBucketId = null;
            mPrevUri = null;
            
            openVideo();
        }
        mBucketId = buckekId;
        int start = (int)mSharedPref.getLong(START_TIME, mStartTime);
        int end = (int)mSharedPref.getLong(END_TIME, mEndTime);
        // if user change start time and set, show the change immediately
        if (!videoChangeOrReopen && start != mStartTime) {
            Xlog.i(TAG, "checkEnvironment() change start from sharedpref: " + start);
            mStartTime = start;
            mCurrentPos = start;
        }
        // otherwise, just apply only when the end is previous to current position
        if (!videoChangeOrReopen && end != mEndTime) {
            Xlog.i(TAG, "checkEnvironment() change end from sharedpref: " + end);
            mEndTime = end;
            if (mMediaPlayer != null) {
                if (mCurrentPos > end) {
                	mCurrentPos = mStartTime;
                }
            }
        }
    }
    
    private void addAndStartVideoObserver() {
    	if (mUri == null || Utils.isDefaultVideo(mUri) 
    			|| !Utils.isExternalFileExists(mUri.getPath())) {
    		return;
    	}
    	final String directory = new File(mUri.getPath()).getParent();
    	if (mFileObserver != null && directory.equals(mObserverPath)) {
    		return;
    	}
    	mObserverPath = directory;
    	stopAndReleaseVideoObserver();
    	if (DEBUG) {
    		Xlog.d(TAG, "======FileObserver start to monitor " + mObserverPath);
    	}
		mFileObserver = new FileObserver(
				mObserverPath, DELETE | DELETE_SELF | MOVED_FROM | MOVED_TO | MOVE_SELF){
			@Override
			public void onEvent(int event, String path) {
				if (DEBUG) {
					Xlog.d(TAG, "FileObserver::onEvent(0x" + Integer.toHexString(event) + "," + path + ")");
				}
				String videoPath = new File(mUri.getPath()).getName();
				if ((event & DELETE) == DELETE) {
					// A file was deleted from the monitored directory
					if (videoPath.equals(path)) {
						Xlog.w(TAG, "FileObserver::onEvent() media file has beed deleted");
						if (mBucketId != null) {
							playDefaultVideoOrNextVideo();
							if (mUriInvalid != null) {
								mUriInvalid.clear();
							}
						} else {
							release(false);
							clear(true, true);
							openVideo();
						}
					}
				}
				
				if ((event & MOVED_FROM) == MOVED_FROM) {
					// A file or subdirectory was moved from the monitored directory
					if (videoPath.equals(path)) {
						mMovingFile = path;
					}
				}
				
				if ((event & MOVED_TO) == MOVED_TO) {
					// A new file or subdirectory was created under the monitored directory
					if (mMovingFile != null && mMovingFile.equals(videoPath)) {
						Xlog.d(TAG, "FileObserver::onEvent() media file has beed renamed "
								+ mMovingFile + " --> " + path);
						mMovingFile = null;
						mUri = Uri.fromFile(new File(mObserverPath + "/" + path));
						// continue to play this video
						saveSettings();
						release(false);
						openVideo();
						// We will start() when vlw is visible but not here!!!
					}
				}
				
				if ((event & DELETE_SELF) == DELETE_SELF) {
					// The monitored directory was deleted; monitoring effectively stops.
					// input path is null. 
					// This msg can be ignored because DELETE will come before it
					Xlog.w(TAG, "FileObserver::onEvent() parent directory has beed deleted");
					if (mUriInvalid != null) {
						mUriInvalid.clear();
					}
				}
				
				if ((event & MOVE_SELF) == MOVE_SELF) {
					// The monitored directory was moved; monitoring continues. 
					// input path is null
					// The bucket_id must be changed, need to update
					Xlog.w(TAG, "FileObserver::onEvent() parent directory has beed renamed");
					if (mUriInvalid != null) {
						mUriInvalid.clear();
					}
					release(false);
					clear(true, true);
					openVideo();
				}
			}
		};
		mFileObserver.startWatching();
    }
    
    private void playDefaultVideoOrNextVideo(){
    	// update mUriList
		mUriList = Utils.getUrisFromBucketId(mContext, mBucketId);
		if (mUriList.isEmpty()) {
			Xlog.i(TAG, "playDefaultVideoOrNextVideo(), play the default video");
			release(false);
			clear(true, true);
			openVideo();
		} else {
			Xlog.i(TAG, "playDefaultVideoOrNextVideo(),video playing is removed or invalid in selected folder, play another video");
			release(false);
			mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
			if (mMode >= 0) {
				mUri = mUriList.get(mMode);
				if(!Utils.isExternalFileExists(mUri.getPath())) {	
					clear(true, true);
					mMode = 0;
				}
			} else {
				Xlog.d(TAG, "playDefaultVideoOrNextVideo(),No valid video in this folder, play default video, size=" + mUriList.size());
				clear(true, true);
				mMode = 0;
			}
			// save in SharedPref so that play video correctly if user
			// delete the playing video then reboot device by taking
			// out battery
			saveSettings();
			openVideo();
		}
    }
    
    private void stopAndReleaseVideoObserver(){
		if(mFileObserver != null){
			mFileObserver.stopWatching();
		}
		mFileObserver = null;
    }
	
	public void start() {
		if (mHasShutdown) {
			Xlog.w(TAG, "shuting down, do not start to play");
			return;
		}
		checkEnvironment();
		addAndStartVideoObserver();
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
		// if user set end-time, we should stop at that time and restart again from start-time
		if (mBucketId == null) {
			mHandler.sendEmptyMessage(MSG_MONITOR_POSITION);
		}
		mHandler.removeMessages(MSG_RELEASE_TIMER);
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
				mCurrentPos = mMediaPlayer.getCurrentPosition();
			}
		}
		mTargetState = STATE_PAUSED;
		if (mBucketId == null) {
			mHandler.removeMessages(MSG_MONITOR_POSITION);
		}
		// start timer
		mHandler.sendEmptyMessageDelayed(MSG_RELEASE_TIMER, RELEASE_TIME_OUT);
	}
	
    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }
    
	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
		    mCurrentPos = mMediaPlayer.getCurrentPosition();
		}
		return mCurrentPos;
	}

	public void seekTo(long msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo((int)msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	public boolean isPreview() {
		return mPreview;
	}

	public int getSurfaceWidth() {
		return mSurfaceWidth;
	}

	public int getSurfaceHeight() {
		return mSurfaceHeight;
	}

	public Bundle doCommand(String action, int x, int y, int z, Bundle extras,
			boolean resultRequested) {
		return null;
	}

}
