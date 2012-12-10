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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.service.wallpaper.WallpaperService;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import com.mediatek.vlw.Utils.LoopMode;

public class VideoEditor extends Activity {
	static final String TAG = "VideoEditor";
	static final boolean DEBUG = true;
	
	private static final int PICK_VIDEO_REQUEST = 0;
	private static final int PICK_FOLDER_REQUEST = 1;
	private static final int PICK_CAMERA_REQUEST = 2;
	private static final int DIALOG_SELECT_VIDEO = 1;
	private static String VIDEO_LIVE_WALLPAPER_PACKAGE = "com.mediatek.vlw";
	private static String VIDEO_LIVE_WALLPAPER_CLASS = "com.mediatek.vlw.VideoLiveWallpaper";
	
	private Intent mWallpaperIntent;
	private VideoView mPlayer;
	private VLWMediaController mMediaController;
	private TextView mFolderInfo;
	private TextView mVieoTitle;
	private ImageButton mPlayPause;
	private Button mSetWallpaper;
	
	// settings information
	private SharedPreferences mSharedPref;
	private int mStartTime;
	private int mEndTime;
	private int mCurrentPos;
	private Uri mUri;
	private ArrayList<Uri> mUriList;
	private ArrayList<Uri> mUriInvalid;
	private String mBucketId;
	private int mMode;
	private LoopMode mLoopMode = LoopMode.ALL;
	private boolean mIsOpening;
	private boolean mClosed;
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
    // states we should take care of
	private static final int STATE_ERROR              = -1;
	private static final int STATE_IDLE               = 0;
	private static final int STATE_PLAYING            = 1;
	private static final int STATE_PAUSED             = 2;
	private static final int STATE_PREPARED           = 3;
	private static final int STATE_PLAYBACK_COMPLETED = 4;

	private final Handler mHandler = new Handler();
	
	private final VLWMediaController.Callback mCallback = new VLWMediaController.Callback() {
		@Override
		public void updateUI(boolean isPlaying) {
			updatePausePlay(isPlaying);
		}

		@Override
		public void updateState(int start, int end) {
			mStartTime = start;
			mEndTime = end;
		}
	};
	
	private final OnErrorListener mOnErrorListener = new OnErrorListener() {
		
		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			// handle all errors, play another video or revert to default video
			if (mBucketId != null) {
				// update mUriList
				mUriList = Utils.getUrisFromBucketId(VideoEditor.this, mBucketId);
				if (mUriInvalid == null) {
					mUriInvalid = new ArrayList<Uri>();
				}
				if (!mUriInvalid.contains(mUri)) {
					mUriInvalid.add(mUri);
				}
				mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
				if (mMode >= 0) {
					mUri = mUriList.get(mMode);
				} else {
					Xlog.w(TAG, "Error: No valid videos, play default video");
					clear(false, true, true);
					mMode = 0;
				}
			} else {
				Xlog.w(TAG, "errors, play default video");
				clear(false, false);
			}
			Utils.showInfo(VideoEditor.this, R.string.VideoScene_error_text_unknown, true);
			startPlayback();
			return true;
		}
	};
	
	private final OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
	    @Override
	    public void onPrepared(MediaPlayer mp) {
	        mCurrentState = STATE_PREPARED;
	        int duration = mPlayer.getDuration();
	        int height = mp.getVideoHeight();
	        int width = mp.getVideoWidth();
	        if (mIsOpening) {
	            mEndTime = (mEndTime == VideoScene.DEFAULT_END && duration > 0) ? duration : mEndTime;
	            if (mMediaController != null) {
	                mMediaController.initControllerState(mStartTime, mEndTime, duration);
	            }
	            play();
	            if (height == 0 && width == 0) {
	                if (mUriInvalid == null) {
	                    mUriInvalid = new ArrayList<Uri>();
	                }
	                if (!mUriInvalid.contains(mUri)) {
	                    mUriInvalid.add(mUri);
	                }
	                Xlog.w(TAG, "onPrepared() warning: " + mUri + " is invalid:" +
	                        " w=" + width + ",h=" + height);
	            }
	            mIsOpening = false;
	        } else if (mMediaController != null){
	            if (DEBUG) {
                    Xlog.d(TAG, "traceBack curPos=" + mCurrentPos);
                }
	            mMediaController.traceBack(mCurrentPos);
	        } else {
	            mPlayer.seekTo(mCurrentPos);
	        }
	    }
	};
	
	private final OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
		// if we set endTime, the media controller will take care of this, but if 
		// not, catch it here
		@Override
		public void onCompletion(MediaPlayer mp) {
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			// Loop mode
			final int duration = mPlayer.getDuration();
            if (DEBUG) {
                Xlog.d(TAG, "onCompletion mCurrentState = " + mCurrentState + ",duration = "
                        + duration + ",mTargetState = " + mTargetState + ",mBucketId = "
                        + mBucketId + ",mMode = " + mMode + ",mLoopMode = " + mLoopMode
                        + ",mUriList = " + mUriList + ",mUriInvalid = " + mUriInvalid);
            }
            
			if (mTargetState == STATE_PLAYING) {
				if (mBucketId != null) {
					mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
					if (mMode >= 0) {
						Uri oldUri = mUri;
						mUri = mUriList.get(mMode);
						if (oldUri.equals(mUri)) {
							mHandler.post(new Runnable() {

								public void run() {
									// TODO Auto-generated method stub
									play();
									Xlog.v(TAG,"play single video in folder mode");
								}
							});
						} else {
							oldUri = mUri;
							clear(false, false);
							mUri = oldUri;
							// Make sure MediaPlayerService has disconnected from SurfaceTexture
							stopPlayback();
							startPlayback();
						}
					} else {
						mp.seekTo(0);
						mp.start();
					}

				} else if (mMediaController != null) {
					mHandler.post(new Runnable() {

						public void run() {
							// TODO Auto-generated method stub
							mMediaController.initControllerState(mStartTime,
									mEndTime, duration);
							mMediaController.play();
							mTargetState = STATE_PLAYING;
							Xlog.v(TAG,"play the same video in single video mode");
						}
					});
				}
			}
		}
	};
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        // when sdcard is removed, notify user and then play the default video
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
        	Xlog.i(TAG," mReceive intent action=" + action + " path=" + path);
        	if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || 
            		Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action) || 
            		Intent.ACTION_MEDIA_REMOVED.equals(action) || 
            		Intent.ACTION_MEDIA_EJECT.equals(action)) {
        		String videoPath = mUri.getPath();
            	if (videoPath != null && videoPath.contains(path)) {
            		Xlog.w(TAG, "action: " + action + " revert to default video. sdcard path: " + path 
            				+ " absolute path: " + videoPath + " mUri: " + mUri);
            		Utils.showInfo(VideoEditor.this, R.string.VideoScene_error_sdcard_unmounted, true);
            		clear(false, true, true);
            		
                	startPlayback();
            	}
            }
        }
    };
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.editor);
		mWallpaperIntent = null;
		mSharedPref = getSharedPreferences(VideoScene.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		
		mPlayer = (VideoView) findViewById(R.id.player);
		mPlayer.setOnPreparedListener(mOnPreparedListener);
		mPlayer.setOnErrorListener(mOnErrorListener);
		mPlayer.setOnCompletionListener(mOnCompletionListener);
		
		mVieoTitle = (TextView) findViewById(R.id.video_title);
		mPlayPause = (ImageButton) findViewById(R.id.play_pause);
		mPlayPause.requestFocus();
		mSetWallpaper = (Button) findViewById(R.id.set_wallpaper);
		
		loadSettings();
		// Restore saved state if needed.
		if (savedInstanceState != null) {
			String uriString = savedInstanceState.getString(VideoScene.WALLPAPER_URI);
			if (uriString != null) {
				Uri uri = Uri.parse(uriString);
				if (uri != null && !uri.equals(mUri)) {
					mUri = uri;
					if (DEBUG) {
						Xlog.d(TAG, "onCreate() restore saved uri=" + uri);
					}
				}
			}
			int start = savedInstanceState.getInt(VideoScene.START_TIME);
			if (mStartTime != start && start != VideoScene.DEFAULT_START) {
				mStartTime = start;
				if (DEBUG) {
					Xlog.d(TAG, "onCreate() restore saved start time=" + start);
				}
			}
			int end = savedInstanceState.getInt(VideoScene.END_TIME);
			if (mEndTime != end && end != VideoScene.DEFAULT_END) {
				mEndTime = end;
				if (DEBUG) {
					Xlog.d(TAG, "onCreate() restore saved end time=" + end);
				}
			}
			int pos = savedInstanceState.getInt(VideoScene.CURRENT_POSITION);
			if (mCurrentPos != pos && pos != 0) {
				mCurrentPos = pos;
				if (DEBUG) {
					Xlog.d(TAG, "onCreate() restore saved position=" + pos);
				}
			}
		}
		startPlayback();
		
		if (mReceiver != null) {
			Xlog.i(TAG, "onCreate() register receiver: " + mReceiver);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	        filter.addAction(Intent.ACTION_MEDIA_EJECT);
	        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
	        filter.addDataScheme("file");
			registerReceiver(mReceiver, filter);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		pause();
		
		// Calling this to avoid flashing preview video image when wake up if
		// vlw has set as wallpaper and suspend from preview Activity
		// Side-effect: editor bar jumping when onResume() without onStart() every time
		mPlayer.setVisibility(View.INVISIBLE);
		
		// ALPS 235722 avoid mPlayPause button has black background when the Activity is gone.
		mPlayPause.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPlayer.setVisibility(View.VISIBLE);
		
		// ALPS 235722 avoid mPlayPause button has black background when the Activity is gone.
		mPlayPause.setVisibility(View.VISIBLE);
		
		// if it is playing when Activity.onPause, just let user to start it
		// this will avoid some issue when wake up but still on lockscreen
		// update TextView displayed count
		if(mBucketId != null){
			resetInfoPanel();
			String title = null;
			title = queryTitle(mBucketId);
			if (title != null) {
				mVieoTitle.setText(title);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			Xlog.i(TAG, "onDestroy() unregister receiver: " + mReceiver);
		}
		// must stop playback of this video, avoid same video file access conflict
		if (isInPlaybackState()) {
			stopPlayback();
		}
		
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// After onPause() return, Media is stopped and this will always get zero
		int pos = mPlayer.getCurrentPosition();
		mCurrentPos = (pos != 0) ? pos : mCurrentPos;
		outState.putString(VideoScene.WALLPAPER_URI, mUri.toString());
		outState.putInt(VideoScene.START_TIME, mStartTime);
		outState.putInt(VideoScene.END_TIME, mEndTime);
		outState.putInt(VideoScene.CURRENT_POSITION, mCurrentPos);
		if (DEBUG) {
			Xlog.d(TAG, "onSaveInstanceState() mUri=" + mUri + ", mStartTime="
					+ mStartTime + ", mEndTime=" + mEndTime + ", mCurrentPos="
					+ mCurrentPos);
		}
	}

	private void resetInfoPanel() {
		if (!mSetWallpaper.isEnabled()) {
			mSetWallpaper.setEnabled(true);
		}
		if (mBucketId != null && mUriList.size() > 0) {
			Xlog.w(TAG, "resetInfoPanel, show folder info");
			// detach media controller to VideoView if necessary
			if (mMediaController != null) {
				mMediaController.setVisibility(View.GONE);
				mMediaController = null;
				mPlayer.setMediaController(mMediaController);
			}
			
			if (mFolderInfo == null) {
				mFolderInfo = (TextView) findViewById(R.id.folder_info);
				mFolderInfo.setVisibility(View.VISIBLE);
			}
			//updata displayed video count
			mUriList = Utils.getUrisFromBucketId(VideoEditor.this, mBucketId);
			// update folder info any way
			String info = Utils.queryFolderInfo(this, mBucketId);
			int count = 0; 
			if (mUriList != null) {
				count = mUriList.size();
			}
			info = "<b>" + count + "</b> " + getResources().getString(
					R.string.folder_info) + "<b>" + info + "</b>";
			Spanned span = Html.fromHtml(info);
			mFolderInfo.setText(span);
			
		} else {
			Xlog.w(TAG, "resetInfoPanel, show media controller");
			if (mFolderInfo != null) {
				mFolderInfo.setVisibility(View.GONE);
				mFolderInfo = null;
			}
			// attach media controller to VideoView if necessary
			if (mMediaController == null) {
				mMediaController = (VLWMediaController) findViewById(R.id.media_controller);
				mMediaController.setVisibility(View.VISIBLE);
				mMediaController.setMediaPlayer(mPlayer);
				mMediaController.setAnchorView(mPlayer);
				mMediaController.addCallback(mCallback);
				mPlayer.setMediaController(mMediaController);
			}
		}

	}
	
	private void updatePausePlay(boolean isPlaying) {
		if (isPlaying) {
			mCurrentState = STATE_PLAYING;
			mTargetState = STATE_PLAYING;
			if (mPlayPause != null) {
				mPlayPause.setImageResource(R.drawable.pause);
			}
		} else {
			mCurrentState = STATE_PAUSED;
			mTargetState = STATE_PAUSED;
			if (mPlayPause != null) {
				mPlayPause.setImageResource(R.drawable.play);
			}
		}
	}
	
	private void pause() {
        if (DEBUG) {
            Xlog.d(TAG, "------pause mMediaController = " + mMediaController + ",mCurrentState = "
                    + mCurrentState + ",mTargetState = " + mTargetState);
        }

        int pos = mPlayer.getCurrentPosition();
        mCurrentPos = (pos != 0) ? pos : mCurrentPos;
        
		if (mMediaController != null) {
			if (mCurrentState == STATE_PLAYING) {
				mTargetState = STATE_PAUSED;
				mMediaController.pause();
			}
			
		} else {
			if (mCurrentState == STATE_PLAYING) {
				mTargetState = STATE_PAUSED;
				mPlayer.pause();
			}
			updatePausePlay(false);
		}
	}
	
	private void play() {
        if (DEBUG) {
            Xlog.d(TAG, "play mMediaController = " + mMediaController + ",mCurrentState = "
                    + mCurrentState + ",mTargetState = " + mTargetState);
        }
        // seek to the right position
        if (mPlayer != null && mCurrentPos != 0) {
        	mPlayer.seekTo(mCurrentPos);
        }
		if (mMediaController != null) {
			if (mCurrentState != STATE_PLAYING) {
				mTargetState = STATE_PLAYING;
				mMediaController.play();
			}
			
		} else {
			if (mCurrentState != STATE_PLAYING) {
				mTargetState = STATE_PLAYING;
				mPlayer.start();
			}
			updatePausePlay(true);
		}
		
	}
	
	private boolean isInPlaybackState() {
		return (mPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE);
	}
	
	private void startPlayback() {
		// use thumbnail bitmap to check invalid video;
		mUri = checkThumbnailBitmap(VideoEditor.this, mUri);
		// update info panel state
		resetInfoPanel();
		
		if (mUri != null && mPlayer != null) {
			// Need to relayout or something is wrong when switch video.
			mPlayer.getHolder().setSizeFromLayout();
			mIsOpening = true;
			mPlayer.setVideoURI(mUri);
			play();
			String title = null;
			if (mBucketId != null) {
				title = queryTitle(mBucketId);
			} else {
				title = queryTitle(mUri);
			}
			
			if (title != null) {
				mVieoTitle.setText(title);

			} else {
				Xlog.w(TAG, "get file name failed, set to default");
			}
		}
	}
	
	private void stopPlayback() {
		if (mPlayer != null) {
			mPlayer.stopPlayback();
		}
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}
	
	private String queryTitle(String bucketId) {
		Xlog.i(TAG, "queryTitle, bucketId=" + bucketId);
		String title = null;
		title = Utils.queryFolderInfo(this, bucketId);
		if (title != null) {
			if (mMode >= 0)
				title = title + "(" + (mMode + 1) + "/" + mUriList.size() + ")";
			else {
				title = title + "(1"+ "/" + mUriList.size() + ")";
			}
		}

		return title;
	}
	
	/**
	 * get video title from the media database
	 * 
	 * @param uri
	 * @return
	 */
	private String queryTitle(Uri uri) {
		if (uri == null) {
			Xlog.w(TAG, "Uri is null, return null");
			return null;
		}
		String title = null;
		Cursor cursor = null;
		try {
			// video from SDCARD
			String[] proj = { MediaStore.Video.Media.DISPLAY_NAME };
			ContentResolver cr = this.getContentResolver();
			cursor = cr.query(uri, proj, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int ci = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
				title = cursor.getString(ci);
			} else {
				// video from resource
				title = uri.getLastPathSegment();
			}
		} catch (Exception e) {
			// ignore
			Xlog.e(TAG, "Exception ");
		}finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		return title;
	}
	
	/**
	 * use thumbnail bitmap to check invalid video, if normal, just return the uri, otherwise, return next normal one or null;
	 */
	private Uri checkThumbnailBitmap(Context context, Uri uri){
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(context, uri);
			Bitmap bitmap = retriever.getFrameAtTime();
			boolean findInvalidVideo = false;
			if(bitmap == null){
				Xlog.v(TAG,"thumbnail bitmap == null");
				findInvalidVideo = true;
			} else {
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				Xlog.v(TAG,"thumbnail bitmap.getWidth() = "+bitmap.getWidth()+",bitmap.getHeight()="+bitmap.getHeight());
				if(width == 0 && height == 0)
					findInvalidVideo = true;
			}
			if(findInvalidVideo){
				if (mUriInvalid == null) {
					mUriInvalid = new ArrayList<Uri>();
				}
				if (!mUriInvalid.contains(mUri)) {
					mUriInvalid.add(mUri);
				}
				Xlog.w(TAG, "thumbnail find unsuport video: " + mUri);
				// if folder mode, then play next video.
				if (mBucketId != null) {
					// update mUriList
					mUriList = Utils.getUrisFromBucketId(VideoEditor.this, mBucketId);

					mMode = Utils.getLoopIndex(mLoopMode, mMode, mUriList, mUriInvalid);
					if (mMode >= 0) {
						mUri = mUriList.get(mMode);
	    				return checkThumbnailBitmap(context, mUri);
					} else {
						Xlog.w(TAG, "Error: No valid videos, the folder cann't be set as wallpaper");
						return uri;
					}										
				}
			}
			return uri;
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
			Xlog.e(TAG, "corrupt video file ", ex);
			return uri;
		} catch ( SecurityException ex){
			// Assume this is a corrupt video file
			Xlog.e(TAG, "corrupt video file ", ex);
			return uri;
		} catch ( IllegalStateException ex){
			// Assume this is a corrupt video file
			Xlog.e(TAG, "corrupt video file ", ex);
			return uri;
		} catch (RuntimeException ex) {
			Xlog.d(TAG, "error: ", ex);
			return uri;
		}
		finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
	}

	/**
	 * get title of video from video's media metadata
	 * @param context
	 * @param uri
	 * @return
	 */
	private String queryTitle(Context context, Uri uri) {
		if (uri == null) {
			Xlog.w(TAG, "Uri is null, return null");
			return null;
		}
		String title = null;
		if (DEBUG) {
			Xlog.i(TAG, "query Uri " + uri);
		}
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(context, uri);
			title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
			Xlog.e(TAG, "corrupt video file ", ex);
		} catch (RuntimeException ex) {
			Xlog.d(TAG, "error: ", ex);
		}
		finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		if (title == null) {
			title = uri.getLastPathSegment();
		}
		return title;
	}
	
	/**
	 * get state info from shared preference. if no video URI is set then use
	 * the default video URI
	 */
	private void loadSettings() {
		Uri uri = Uri.parse(getResources().getString(R.string.default_video_path));

		if (mSharedPref == null) {
			Xlog.w(TAG, "has no SharedPreferences, use default");
			mUri = uri;
			mStartTime = VideoScene.DEFAULT_START;
			mEndTime = VideoScene.DEFAULT_END;
			mCurrentPos = VideoScene.DEFAULT_START;
		} else {
			mBucketId = mSharedPref.getString(VideoScene.BUCKET_ID, null);
			String uriString = mSharedPref.getString(VideoScene.WALLPAPER_URI, uri.toString());
			mUri = Uri.parse(uriString);
			mStartTime = (int)mSharedPref.getLong(VideoScene.START_TIME, VideoScene.DEFAULT_START);
			mEndTime = (int)mSharedPref.getLong(VideoScene.END_TIME, VideoScene.DEFAULT_END);
			mCurrentPos = (int)mSharedPref.getLong(VideoScene.CURRENT_POSITION, VideoScene.DEFAULT_START);
		}
		if (DEBUG) {
			Xlog.i(TAG, String.format(
				"restore from preference, bucket id %s, Uri %s, start time %d, " +
				"end time %d, paused position %d", mBucketId, mUri, mStartTime, 
				mEndTime, mCurrentPos));
		}
		if (mBucketId != null) {
			mUriList = Utils.getUrisFromBucketId(this, mBucketId);
			for (int index = 0; index < mUriList.size(); index++) {
				if (mUriList.get(index).equals(mUri)) {
					mMode = index;
					break;
				}
			}
		}
	}

	/**
	 * if this func is called, we know user change the video so we must update
	 * all infos especially must reset the CURRENT_POSITION to START_TIME
	 * 
	 * @hide
	 */
	private void saveSettings() {

		Editor edit = mSharedPref.edit();
		edit.putString(VideoScene.BUCKET_ID, mBucketId);
		edit.putString(VideoScene.WALLPAPER_URI, mUri.toString());
		edit.putLong(VideoScene.START_TIME, (long)mStartTime);
		edit.putLong(VideoScene.END_TIME, (long)mEndTime);
		edit.putLong(VideoScene.CURRENT_POSITION, (long)mStartTime);
		edit.commit();

		if (DEBUG) {
			Xlog.i(TAG, String.format(
				"save settings, bucketId %s, Uri %s, start time %d, end time %d, paused position %d",
				mBucketId, mUri, mStartTime, mEndTime, mCurrentPos));
		}
	}
	
	private void clear (boolean clearPrefs, boolean clearBucketId, boolean clearList) {
		if (clearList) {
			if (mUriList != null) {
				mUriList.clear();
			}
			if (mUriInvalid != null) {
				mUriInvalid.clear();
			}
		}
		clear(clearPrefs, clearBucketId);
	}
	
	/**
	 * clear all current state info, let VideoScene take care of shared preference
	 */
	private void clear(boolean clearPrefs, boolean clearBucketId) {
		if (clearBucketId) {
			mBucketId = null;
		}
		
		mUri = Uri.parse(getResources().getString(R.string.default_video_path));
		mStartTime = VideoScene.DEFAULT_START;
		mEndTime = VideoScene.DEFAULT_END;
		mCurrentPos = VideoScene.DEFAULT_START;
		if (clearPrefs) {
			if (mSharedPref == null) {
				if (DEBUG) {
					Xlog.e(TAG, "we lost the shared preferences");
				}
				return;
			}
			Editor edit = mSharedPref.edit();
			edit.putString(VideoScene.BUCKET_ID, mBucketId);
			edit.putString(VideoScene.WALLPAPER_URI, mUri.toString());
			edit.putLong(VideoScene.START_TIME, mStartTime);
			edit.putLong(VideoScene.END_TIME, mEndTime);
			edit.putLong(VideoScene.CURRENT_POSITION, mCurrentPos);
			edit.commit();
			if (DEBUG) {
				Xlog.i(TAG, "clear(), reset the default state into shared_prefs");
			}
		}
	}
	
	// TODO: THIS SHOULD HAPPEN IN AN ASYNCTASK
    private void findLiveWallpaper() {
    	if (mWallpaperIntent != null) {
    		ComponentName vlw = mWallpaperIntent.getComponent();
    		if (vlw != null && 
    			vlw.getPackageName().equals(VIDEO_LIVE_WALLPAPER_PACKAGE) &&
    			vlw.getClassName().equals(VIDEO_LIVE_WALLPAPER_CLASS)) {
    			return;
    		}
    	}
    	PackageManager pkgmgr = getPackageManager();
        List<ResolveInfo> list = pkgmgr.queryIntentServices(
                new Intent(WallpaperService.SERVICE_INTERFACE),
                PackageManager.GET_META_DATA);

        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            ComponentInfo ci = resolveInfo.serviceInfo;
            WallpaperInfo info;
            try {
                info = new WallpaperInfo(this, resolveInfo);
            } catch (XmlPullParserException e) {
                Xlog.w(TAG, "Skipping wallpaper " + ci, e);
                continue;
            } catch (IOException e) {
                Xlog.w(TAG, "Skipping wallpaper " + ci, e);
                continue;
            }

            String packageName = info.getPackageName();
            String className = info.getServiceName();
            //Xlog.i(TAG, "packageName: " + packageName);
            //Xlog.i(TAG, "className: " + className);
            if (packageName.equals(VIDEO_LIVE_WALLPAPER_PACKAGE) && 
            		className.equals(VIDEO_LIVE_WALLPAPER_CLASS)) {
            	mWallpaperIntent = new Intent(WallpaperService.SERVICE_INTERFACE);
            	mWallpaperIntent.setClassName(packageName, className);
            	break;
            }
            
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Xlog.d(TAG, "onActivityResult request code = " + requestCode + ", resultCode = "
                    + resultCode + ",data = " + data);
        }
    	if (resultCode == Activity.RESULT_OK && data != null) {
    		Uri uri = null;
    		switch (requestCode) {
    		case PICK_VIDEO_REQUEST:
    			uri = data.getData();
    			// if come here from folder selected session, change session and
    			// select this single video anyway. 
    			if ( uri != null && (!uri.equals(mUri) || mBucketId != null) ) {
    				// reset current state
    				clear(false, true, true);
    				String videoPath = null;
    				if (!Utils.isDefaultVideo(uri)) {
    					videoPath = Utils.getVideoPath(this, uri);
        				mUri = Uri.fromFile(new File(videoPath));
    				} else {
    					videoPath = uri.getPath();
    					mUri = uri;
    				}
    				Xlog.d(TAG, "PICK_VIDEO_REQUEST, uri=" + uri + " mUri=" + mUri);
    				startPlayback();
    			}
    			break;
    			
    		case PICK_FOLDER_REQUEST:    			
    			String bucketId = data.getStringExtra("bucketId");
    			
    			if (bucketId != null && !bucketId.equals(mBucketId)) {
    				mBucketId = bucketId;
    				mUriList = Utils.getUrisFromBucketId(this, bucketId);
    				if (! mUriList.isEmpty()) {
    					uri = mUriList.get(0);
    					// reset this to update the title
    					mMode = 0;
    				}
    				if (uri != null) {
    					// reset current state
    					clear(false, false);
    					if (mUriInvalid != null) {
    						mUriInvalid.clear();
    					}
    					Xlog.d(TAG, "PICK_FOLDER_REQUEST,  " + "bucketId=" 
    							+ bucketId + ", " + mUriList.size() 
    							+ " videos selected, uri=" + uri);
        				mUri = uri;
    					startPlayback();
    				}
    			}

    			break;
    			
    		case PICK_CAMERA_REQUEST:
    			uri = data.getData();
                String videoPath = uri != null ? Utils.getVideoPath(this, uri) : null;
    			if (uri != null && videoPath != null) {
    				// reset current state
    				clear(false, true);
    				mUri = Uri.fromFile(new File(videoPath));
    				Xlog.d(TAG, "PICK_CAMERA_REQUEST, uri=" + uri + ", mUri=" + mUri);
    				startPlayback();
    			}
    			
    			break;
    			
    		default:
    			Xlog.e(TAG, "unknown request");
    		}
    	}
	}
   
    @Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_SELECT_VIDEO) {
			return new SelectVideo().createDialog();
		}
		
		return super.onCreateDialog(id);
	}

	/**
     * Displays the select sdcard video dialog and launches, if necessary, the
     * appropriate activity.
     */
    private class SelectVideo implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener, DialogInterface.OnDismissListener,
            DialogInterface.OnShowListener {

        private AddAdapter mAdapter;

        Dialog createDialog() {
            mAdapter = new AddAdapter(VideoEditor.this);

            final AlertDialog.Builder builder = new AlertDialog.Builder(VideoEditor.this);
            builder.setTitle(getString(R.string.menu_item_add_item));
            builder.setAdapter(mAdapter, this);

            builder.setInverseBackgroundForced(true);

            AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(this);
            dialog.setOnDismissListener(this);
            dialog.setOnShowListener(this);

            return dialog;
        }

        public void onCancel(DialogInterface dialog) {
            
            cleanup();
        }

        public void onDismiss(DialogInterface dialog) {
        	// TODO
        }

        private void cleanup() {
            try {
                dismissDialog(DIALOG_SELECT_VIDEO);
            } catch (Exception e) {
                // An exception is thrown if the dialog is not visible, which is fine
            }
        }

        /**
         * Handle the action clicked in the "select video" dialog.
         */
        public void onClick(DialogInterface dialog, int which) {
            cleanup();
            
            AddAdapter.ListItem listItem = (AddAdapter.ListItem) mAdapter.getItem(which);
            Intent pickIntent = null;
            Intent wrapperIntent = null;
            switch (listItem.actionTag) {
                case AddAdapter.ITEM_VIDEO:
            		pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            		pickIntent.setType("video/*");
            		wrapperIntent = Intent.createChooser(pickIntent, null);
            		startActivityForResult(wrapperIntent, PICK_VIDEO_REQUEST);
                    break;

                case AddAdapter.ITEM_FOLDER: 
                	pickIntent = new Intent("com.mediatek.action.PICK_VIDEO_FOLDER");
                	pickIntent.setType("video/*");
                	wrapperIntent = Intent.createChooser(pickIntent, null);
                	startActivityForResult(wrapperIntent, PICK_FOLDER_REQUEST);
                    break;
                    
                default:
                	Xlog.e(TAG, "unknown item actionTag: " + listItem.actionTag);
                	break;
            }
        }

        public void onShow(DialogInterface dialog) {
        	// TODO
        }
    }
    
	// button hook
	@SuppressWarnings({ "UnusedDeclaration" })
	public void setLiveWallpaper(View v) {
		if(checkUri()){			
			Utils.showInfo(VideoEditor.this, R.string.VideoScene_error_be_set, true);
			// stop it from being pressed so crazy
			mSetWallpaper.setEnabled(false);
			return;
		}
		// save state info
		saveSettings();
		// set live wallpaper
		findLiveWallpaper();
		if (mWallpaperIntent == null) {
			Xlog.e(TAG, "can not find Video Live Wallpaper package");
			return;
		}
		// must stop playback of this video, avoid same video file access conflict
		stopPlayback();
		
		try {
			Xlog.i(TAG, "<< Set Video Live Wallpaper... >>");
			WallpaperManager wpm = WallpaperManager.getInstance(this);
			wpm.getIWallpaperManager().setWallpaperComponent(
                    mWallpaperIntent.getComponent());
			wpm.setWallpaperOffsetSteps(0.5f, 0.0f);
			wpm.setWallpaperOffsets(v.getRootView().getWindowToken(), 0.5f,
					0.0f);
			setResult(RESULT_OK);
		} catch (RemoteException e) {
			// do nothing
		} catch (RuntimeException e) {
			// up to here, WPMS will revert to static wallpaper but not terminate app
			Xlog.w(TAG, "Failure setting wallpaper", e);
		}
		finish();
		mClosed = true;
	}

	private boolean checkUri() {
		// check invalid
		if(mUriList != null && !mUriList.isEmpty() && mUriInvalid != null && mUriInvalid.size() != mUriList.size()){
			return false;
		}else if(mUriInvalid != null && mUriInvalid.size() > 0 && mUriInvalid.contains(mUri)){
			return true;
		}
		return false;
	}

	// button hook
	@SuppressWarnings({ "UnusedDeclaration" })
	public void selectVideo(View v) {
		if (!mClosed) {
			showDialog(DIALOG_SELECT_VIDEO);
		}
	}
	
	// button hook
	@SuppressWarnings({ "UnusedDeclaration" })
	public void selectDefaultVideo(View v) {
		Intent pickIntent = new Intent(this, VideoChooser.class);
		pickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(pickIntent, PICK_VIDEO_REQUEST);
	}
	
	// button hook
	@SuppressWarnings({ "UnusedDeclaration" })
	public void captureVideo(View v) {
		float ratio = Utils.queryResolutionRatio(this);
		Xlog.i(TAG, "To captureVideo, ratio=" + ratio);
		
		Intent pickIntent = new Intent("android.media.action.VIDEO_CAPTURE");
		pickIntent.putExtra("identity", "com.mediatek.vlw");
		pickIntent.putExtra("ratio", ratio);
		Intent wrapperIntent = Intent.createChooser(pickIntent, null);
		startActivityForResult(wrapperIntent, PICK_CAMERA_REQUEST);
	}
	
	// button hook
	@SuppressWarnings({ "UnusedDeclaration" })
	public void updatePausePlay(View v) {
		if (mPlayer != null) {
			if (mCurrentState == STATE_PLAYING) {
				pause();
			} else {
				play();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// do nothing. override to resolve issue caused by 
		// capturing video in landscape mode, when it returns,
		// this activity will be destroyed and restart
	}
	
	
}
