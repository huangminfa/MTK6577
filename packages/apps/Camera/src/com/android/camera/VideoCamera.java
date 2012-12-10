/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.camera;

import com.android.camera.ui.CameraPicker;
import com.android.camera.ui.IndicatorControlContainer;
import com.android.camera.ui.IndicatorControlWheelContainer;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.SharePopup;
import com.android.camera.ui.TimeTextView;
import com.android.camera.ui.ZoomControl;

import com.mediatek.xlog.Xlog;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaRecorder.HDRecordMode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.storage.StorageVolume;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.mediatek.featureoption.FeatureOption;

import android.filterpacks.videosink.MediaRecorderStopException;

/**
 * The Camcorder activity.
 */
public class VideoCamera extends ActivityBase
        implements CameraPreference.OnPreferenceChangedListener,
        ShutterButton.OnShutterButtonListener, SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener,
        ModePicker.OnModeChangeListener, View.OnTouchListener,
        EffectsRecorder.EffectsListener,
        LocationManager.Listener,
        ShutterButton.OnShutterButtonLongPressListener,
        EffectsRecorder.OnSurfaceStateChangeListener,
        FocusManager.Listener{

    private static final String TAG = "videocamera";

    private static final int CHECK_DISPLAY_ROTATION = 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int UPDATE_RECORD_TIME = 5;
    private static final int ENABLE_SHUTTER_BUTTON = 6;
    private static final int SHOW_TAP_TO_SNAPSHOT_TOAST = 7;
    private static final int UPDATE_STORAGE = 11;
    private static final int MSG_GET_THUMBNAIL_DONE = 10;	
    private static final int SHOW_LONG_TAP_TO_PAUSE_TOAST = 14;
	
    private static final int SCREEN_DELAY = 2 * 60 * 1000;

    // The brightness settings used when it is set to automatic in the system.
    // The reason why it is set to 0.7 is just because 1.0 is too bright.
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;

    private static final boolean SWITCH_CAMERA = true;
    private static final boolean SWITCH_VIDEO = false;

    private static final long SHUTTER_BUTTON_TIMEOUT = 500L; // 500ms

    private static final int[] TIME_LAPSE_VIDEO_QUALITY = {
            CamcorderProfile.QUALITY_TIME_LAPSE_1080P,
            CamcorderProfile.QUALITY_TIME_LAPSE_720P,
            CamcorderProfile.QUALITY_TIME_LAPSE_480P,
            CamcorderProfile.QUALITY_TIME_LAPSE_CIF,
            CamcorderProfile.QUALITY_TIME_LAPSE_QVGA,
            CamcorderProfile.QUALITY_TIME_LAPSE_QCIF};

    private static final int[] VIDEO_QUALITY = {
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_CIF,
            CamcorderProfile.QUALITY_QVGA,
            CamcorderProfile.QUALITY_QCIF};
    private static final String[] pref_camera_video_hd_recording_entryvalues = {
            "normal",
            "indoor"
    };

    /**
     * An unpublished intent flag requesting to start recording straight away
     * and return as soon as recording is stopped.
     * TODO: consider publishing by moving into MediaStore.
     */
    private static final String EXTRA_QUICK_CAPTURE =
            "android.intent.extra.quickCapture";

    private boolean mSnapshotInProgress = false;

    private static final String EFFECT_BG_FROM_GALLERY = "gallery";

    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    private ComboPreferences mPreferences;
    private PreferenceGroup mPreferenceGroup;

    private View mPreviewPanel;  // The container of PreviewFrameLayout.
    private PreviewFrameLayout mPreviewFrameLayout;
    private SurfaceHolder mSurfaceHolder = null;
    private IndicatorControlContainer mIndicatorControlContainer;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private View mReviewControl;
    private RotateDialogController mRotateDialog;

    private Toast mNoShareToast;
    // An review image having same size as preview. It is displayed when
    // recording is stopped in capture intent.
    private ImageView mReviewImage;
    // A popup window that contains a bigger thumbnail and a list of apps to share.
    private SharePopup mSharePopup;
    // The bitmap of the last captured video thumbnail and the URI of the
    // original video.
    private Thumbnail mThumbnail;
    // An imageview showing showing the last captured picture thumbnail.
    private RotateImageView mThumbnailView;
	private boolean mThumbnailUpdated;	
    private Rotatable mReviewCancelButton;
    private Rotatable mReviewDoneButton;
    private Rotatable mReviewPlayButton;
    private ModePicker mModePicker;
    private ShutterButton mShutterButton;
    private TimeTextView mRecordingTimeView;
    private RotateLayout mBgLearningMessageRotater;
    private View mBgLearningMessageFrame;
    private LinearLayout mLabelsLinearLayout;

    private boolean mIsVideoCaptureIntent;
    private boolean mQuickCapture;

    private boolean mOpenCameraFail = false;
    private boolean mCameraDisabled = false;

    private long mStorageSpace;

    private MediaRecorder mMediaRecorder;
    private EffectsRecorder mEffectsRecorder;
    private boolean mEffectsDisplayResult;

    private int mEffectType = EffectsRecorder.EFFECT_NONE;
    private Object mEffectParameter = null;
    private String mEffectUriFromGallery = null;
    private String mPrefVideoEffectDefault;
    private boolean mResetEffect = true;
    public static final String RESET_EFFECT_EXTRA = "reset_effect";
    public static final String BACKGROUND_URI_GALLERY_EXTRA = "background_uri_gallery";

    private boolean mMediaRecorderRecording = false;
    private long mRecordingStartTime;
    private boolean mRecordingTimeCountsDown = false;
    private RotateLayout mRecordingTimeRect;
    private long mOnResumeTime;
    // The video file that the hardware camera is about to record into
    // (or is recording into.)
    private String mVideoFilename;
    private ParcelFileDescriptor mVideoFileDescriptor;

    // The video file that has already been recorded, and that is being
    // examined by the user.
    private String mCurrentVideoFilename;
    private Uri mCurrentVideoUri;
    private ContentValues mCurrentVideoValues;

    private CamcorderProfile mProfile;
    private int mAudioMode;

    // The video duration limit. 0 menas no limit.
    private int mMaxVideoDurationInMs;

    // Time Lapse parameters.
    private boolean mCaptureTimeLapse = false;
    // Default 0. If it is larger than 0, the camcorder is in time lapse mode.
    private int mTimeBetweenTimeLapseFrameCaptureMs = 0;
    private View mTimeLapseLabel;

    private int mDesiredPreviewWidth;
    private int mDesiredPreviewHeight;

    boolean mPausing = false;
    boolean mPreviewing = false; // True if preview is started.
    // The display rotation in degrees. This is only valid when mPreviewing is
    // true.
    private int mDisplayRotation;

    private ContentResolver mContentResolver;

    private LocationManager mLocationManager;

    private final Handler mHandler = new MainHandler();
    private Parameters mParameters;
    private Parameters mInitialParams;

    // multiple cameras support
    private int mNumberOfCameras;
    private int mCameraId;
    private int mFrontCameraId;
    private int mBackCameraId;

    private GestureDetector mPopupGestureDetector;

    private MyOrientationEventListener mOrientationListener;
    // The degrees of the device rotated clockwise from its natural orientation.
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    // The orientation compensation for icons and thumbnails. Ex: if the value
    // is 90, the UI components should be rotated 90 degrees counter-clockwise.
    private int mOrientationCompensation = 0;
    // The orientation compenstaion when we start recording.
    private int mOrientationCompensationAtRecordStart;

    private static final int ZOOM_STOPPED = 0;
    private static final int ZOOM_START = 1;
    private static final int ZOOM_STOPPING = 2;

    private int mZoomState = ZOOM_STOPPED;
    private boolean mSmoothZoomSupported = false;
    private int mZoomValue;  // The current zoom value.
    private int mZoomMax;
    private int mTargetZoomValue;
    private ZoomControl mZoomControl;
    private final ZoomListener mZoomListener = new ZoomListener();

    //Mediatek feature begin
    private static final String VIDEO_WALL_PAPER = "com.mediatek.vlw";

    private ImageView mGpsIndicator;
    private ImageView mFlashIndicator;
    private ImageView mSceneIndicator;
    private ImageView mWhiteBalanceIndicator;
    // A view group that contains all the small indicators.
    private Rotatable mOnScreenIndicators;
    private VideoSizeManager mVideoSizeManager;

    private String mSceneMode;
    private boolean mSceneChanged;
    private boolean mIsVideoWallPaperIntent;
    private String mRemainTimeString;
    private int mQualityId = -1;
    private boolean mRecordAudio = true;
    private boolean mEnableRecordBtn = true;
    private boolean mRestoringPreference = false;
    private boolean mSurfaceReady = true;

    private Drawable mRecordingPaused;
    private Drawable mRecording;

    private static final boolean LOGI = true;
    private static final boolean STOP_RECORDING_ASYNC = true;
    private static final boolean MTK_AUDIO_HD_REC_SUPPORT = FeatureOption.MTK_AUDIO_HD_REC_SUPPORT;

    private FocusManager mFocusManager;
    private RotateLayout mFocusAreaIndicator;
    private boolean mFocusAreaSupported;
    private boolean mMeteringAreaSupported;
    private boolean mAeLockSupported;
    private boolean mAwbLockSupported;
    private static boolean mIsAutoFocusCallback = false;
    private View mPreviewFrame;
    private static final int VIDEO_FOCUS_INIT = 20;
    private boolean mInitFocusFirstTime = false;
    private boolean mSingleStartRecording = false;
    private boolean mSingleAutoModeSupported = false;
    private int mFocusState = 0;
    private static final int FOCUSING = 1;
    private static final int FOCUSED = 2;
    private static final int FOCUS_IDLE = 3;
    private static final int START_FOCUSING = -1;

    //if set timelapse,the quality will increase 1000
    private static final int QUALITY_ID = CamcorderProfile.QUALITY_MTK_1080P + 1000;

    //M:Snapshot
    private Handler mStoreSnapHandler;
    private static final int UPDATE_SNAP_THUMNAIL = 15;
    private Uri mSnapUri;
    private Location mSnapLocation;
    private byte[] mSnapJpegData;
    private Button mSnapshotButton;
    private boolean mSnapButtonInvisible = false;
    private boolean mStopVideoRecording = false;

    private Runnable mUpdateHintRunnable = new Runnable() {
        public void run() {
        	showStorageHint();
        }
    };
    //

    // This Handler is used to post message back onto the main thread of the
    // application
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case ENABLE_SHUTTER_BUTTON:
                	Log.i(TAG, "enable shutter ENABLE_SHUTTER_BUTTON");
                	mShutterButton.setEnabled(true);
                    break;

                case CLEAR_SCREEN_DELAY: {
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                }

                case UPDATE_RECORD_TIME: {
                    updateRecordingTime();
                    break;
                }

                case CHECK_DISPLAY_ROTATION: {
                    // Restart the preview if display rotation has changed.
                    // Sometimes this happens when the device is held upside
                    // down and camera app is opened. Rotation animation will
                    // take some time and the rotation value we have got may be
                    // wrong. Framework does not have a callback for this now.
                    if ((Util.getDisplayRotation(VideoCamera.this) != mDisplayRotation)
                            && !mMediaRecorderRecording) {
                        startPreview();
                    }
                    if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                        mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
                    }
                    break;
                }

                case SHOW_TAP_TO_SNAPSHOT_TOAST: {
                    showTapToSnapshotToast();
                    break;
                }

                case UPDATE_STORAGE: {
					if (!mMediaRecorderRecording) {
		            	mStorageSpace = Storage.getAvailableSpace();
		                updateRemainTimeString();
		            	if (mRecordingTimeView != null) {
		            		setTime(mRemainTimeString);
		            	}
					}
                	break;
                }
                case MSG_GET_THUMBNAIL_DONE: {
					if (!mThumbnailUpdated) {
						mThumbnailUpdated = true;
						mThumbnail = (Thumbnail)msg.obj;
					}
					updateThumbnailButton();
                    break;				
                }
                case SHOW_LONG_TAP_TO_PAUSE_TOAST: {
                    showLongTapToPauseToast();
                    break;
                }
                case VIDEO_FOCUS_INIT: {
                    if(!mInitFocusFirstTime){
                        initializeVideoFocus();
                        }
                    break;
                }
                case UPDATE_SNAP_THUMNAIL:{
                    //updateSnapThumnail();
                    showVideoSnapshotUI(false);
                    mSnapshotButton.setEnabled(true);
                    break;
                }
                default:
                    Log.v(TAG, "Unhandled message: " + msg.what);
                    break;
            }
        }
    }

    private BroadcastReceiver mReceiver = null;

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @SuppressWarnings("deprecation")
		@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received intent action=" + action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            	if (Storage.checkMountPoint(
            			(StorageVolume)intent.getExtra(StorageVolume.EXTRA_STORAGE_VOLUME))) {
            		updateAndShowStorageHint();
                    if (STOP_RECORDING_ASYNC) {
            			stopVideoRecordingAsync();
            		} else {
            			stopVideoRecording();
            		}
            	}
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                updateAndShowStorageHint();
                updateThumbnail();
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                // SD card unavailable
                // handled in ACTION_MEDIA_EJECT
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                Toast.makeText(VideoCamera.this,
                        getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                updateAndShowStorageHint();
            }
        }
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                getString(R.string.video_file_name_format));

        return dateFormat.format(date);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.i(TAG, "onCreate");
        Util.initializeScreenBrightness(getWindow(), getContentResolver());

        mPreferences = new ComboPreferences(this);
        CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
        mCameraId = CameraSettings.readPreferredCameraId(mPreferences);

        //Testing purpose. Launch a specific camera through the intent extras.
        int intentCameraId = Util.getCameraFacingIntentExtras(this);
        if (intentCameraId != -1) {
            mCameraId = intentCameraId;
        }

        mPreferences.setLocalId(this, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());

        mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();
        mPrefVideoEffectDefault = getString(R.string.pref_video_effect_default);
        // Do not reset the effect if users are switching between back and front
        // cameras.
        mResetEffect = getIntent().getBooleanExtra(RESET_EFFECT_EXTRA, true);
        if (icicle != null) {
            mResetEffect = true;
        }
        Log.i(TAG, "mResetEffect = " + mResetEffect);
        // If background replacement was on when the camera was switched, the
        // background uri will be sent via the intent.
        mEffectUriFromGallery = getIntent().getStringExtra(BACKGROUND_URI_GALLERY_EXTRA);
        resetEffect();
        mIsVideoCaptureIntent = isVideoCaptureIntent();
        mIsVideoWallPaperIntent = isVideoWallPaperIntent();

        String[] defaultFocusMode = getResources().getStringArray(R.array.pref_camera_video_focusmode_entryvalues);
        mFocusManager = new FocusManager(mPreferences, defaultFocusMode,true);

        /*
         * To reduce startup time, we start the preview in another thread.
         * We make sure the preview is started at the end of onCreate.
         */
        Thread startPreviewThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mCameraDevice = Util.openCamera(VideoCamera.this, mCameraId);
                    mInitialParams = mCameraDevice.getParameters();
                    Util.setModeSupport(mInitialParams);
                    readVideoPreferences();
                    initializeCapabilities();
                    startPreview();
                } catch (CameraHardwareException e) {
                    mOpenCameraFail = true;
                } catch (CameraDisabledException e) {
                    mCameraDisabled = true;
                }
            }
        });
        startPreviewThread.start();
        Log.v(TAG, "startPreviewThread.start()");

        Util.enterLightsOutMode(getWindow());

        mContentResolver = getContentResolver();

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.video_camera);
        Log.v(TAG, "setContentView end");
        if (mIsVideoCaptureIntent) {
            mReviewDoneButton = (Rotatable) findViewById(R.id.btn_done);
            mReviewPlayButton = (Rotatable) findViewById(R.id.btn_play);
            mReviewCancelButton = (Rotatable) findViewById(R.id.btn_cancel);
            findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
        } else {
			mThumbnailView = (RotateImageView) findViewById(R.id.thumbnail);
			mThumbnailView.enableFilter(false);
			mThumbnailView.setVisibility(View.VISIBLE);
			initThumbnailButton();
            mModePicker = (ModePicker) findViewById(R.id.mode_picker);
            mModePicker.setVisibility(View.VISIBLE);
            mModePicker.setOnModeChangeListener(this);
            mSnapshotButton = (Button)findViewById(R.id.btn_snapshot);
            mSnapshotButton.setOnClickListener(mSnapShotListener);
        }
        initOnScreenIndicator();

        mRotateDialog = new RotateDialogController(this, R.layout.rotate_dialog);

        mPreviewPanel = findViewById(R.id.frame_layout);
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
        mReviewImage = (ImageView) findViewById(R.id.review_image);
        mOnScreenIndicators = (Rotatable) findViewById(R.id.on_screen_indicators);

        // don't set mSurfaceHolder here. We have it set ONLY within
        // surfaceCreated / surfaceDestroyed, other parts of the code
        // assume that when it is set, the surface is also set.
        SurfaceView preview = (SurfaceView) findViewById(R.id.camera_preview);
        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Intent intent = getIntent();
        mQuickCapture = intent.getBooleanExtra(EXTRA_QUICK_CAPTURE, false) ||
		                mIsVideoWallPaperIntent;

        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setBackgroundResource(R.drawable.btn_shutter_video);
        mShutterButton.setOnShutterButtonListener(this);
        mShutterButton.setOnShutterButtonLongPressListener(this);
        mShutterButton.requestFocus();

        // Disable the shutter button if effects are ON since it might take
        // a little more time for the effects preview to be ready. We do not
        // want to allow recording before that happens. The shutter button
        // will be enabled when we get the message from effectsrecorder that
        // the preview is running. This becomes critical when the camera is
        // swapped.
        if (effectsActive()) {
            mShutterButton.setEnabled(false);
        }

        mRecordingTimeView = (TimeTextView) findViewById(R.id.recording_time);
        mRecordingTimeRect = (RotateLayout) findViewById(R.id.recording_time_rect);
        mOrientationListener = new MyOrientationEventListener(this);
        mTimeLapseLabel = findViewById(R.id.time_lapse_label);

        Resources resources = getResources();
        mRecordingPaused = resources.getDrawable(R.drawable.ic_recording_pause_indicator);
        mRecording = resources.getDrawable(R.drawable.ic_recording_indicator);
        
        // The R.id.labels can only be found in phone layout. For tablet, the id is
        // R.id.labels_w1024. That is, mLabelsLinearLayout should be null in tablet layout.
        mLabelsLinearLayout = (LinearLayout) findViewById(R.id.labels);

        mBgLearningMessageRotater = (RotateLayout) findViewById(R.id.bg_replace_message);
        mBgLearningMessageFrame = findViewById(R.id.bg_replace_message_frame);

        mLocationManager = new LocationManager(this, this);

        mFocusAreaIndicator = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);

        Log.v(TAG, "prepare to join startPreviewThread");
        // Make sure preview is started.
        try {
            startPreviewThread.join();
            if (mOpenCameraFail) {
                Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
                return;
            } else if (mCameraDisabled) {
                Util.showErrorAndFinish(this, R.string.camera_disabled);
                return;
            }
        } catch (InterruptedException ex) {
            // ignore
        }

        Log.v(TAG, "startPreviewThread end");
        if (!mIsVideoCaptureIntent) mModePicker.setModeSupport();
        showTimeLapseUI(mCaptureTimeLapse);
        //initializeVideoSnapshot();
        resizeForPreviewAspectRatio();

        mBackCameraId = CameraHolder.instance().getBackCameraId();
        mFrontCameraId = CameraHolder.instance().getFrontCameraId();

        initializeIndicatorControl();
        updateOnScreenIndicators();
        mRestoreRecordUI = false;

        HandlerThread handlerThread = new HandlerThread("snapshotThread");
        handlerThread.start();
        mStoreSnapHandler = new Handler(handlerThread.getLooper());
        Log.v(TAG, "onCreate end");
    }

    private void loadCameraPreferences() {
        CameraSettings settings = new CameraSettings(this, mInitialParams,
                mCameraId, CameraHolder.instance().getCameraInfo());
        // Remove the video quality preference setting when the quality is given in the intent.
        mPreferenceGroup = filterPreferenceScreenByIntent(
                settings.getPreferenceGroup(R.xml.video_preferences));
    }

    private boolean collapseCameraControls() {
        //tricky here, just hide modepicker.
        if (mModePicker != null) {
        	mModePicker.onOtherPopupShowed();
        }
        if ((mIndicatorControlContainer != null)
                && mIndicatorControlContainer.dismissSettingPopup()) {
            return true;
        }
        return false;
    }

    private void enableCameraControls(boolean enable) {
        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.setEnabled(enable);
            updateSceneModeUI();
        }
        if(!mIsVideoCaptureIntent){
            if (!enable) {
                if ((!mSnapButtonInvisible) && (FeatureOption.MTK_VSS_SUPPORT)) {
                    mModePicker.setVisibility(View.GONE);
                    mSnapshotButton.setEnabled(!enable);
                    mSnapshotButton.setVisibility(View.VISIBLE); 
                } else {
                    mModePicker.setEnabled(enable);
                    mSnapshotButton.setVisibility(View.GONE);
                }
             } else if (mModePicker != null) {
                        mModePicker.setVisibility(View.VISIBLE);
                        mModePicker.setEnabled(enable);
                        mSnapshotButton.setVisibility(View.GONE);
                    }
            }
    }

    private void initializeIndicatorControl() {
        Log.v(TAG, "initializeIndicatorControl");
        mIndicatorControlContainer =
                (IndicatorControlContainer) findViewById(R.id.indicator_control);
        if (mIndicatorControlContainer == null) return;
        loadCameraPreferences();

        final String[] SETTING_KEYS = {
                    CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                    CameraSettings.KEY_WHITE_BALANCE,
                    CameraSettings.KEY_COLOR_EFFECT,
                    CameraSettings.KEY_VIDEO_EFFECT,
                    CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL};
        final String[] OTHER_SETTING_KEYS = {
        			CameraSettings.KEY_VIDEO_QUALITY,
                    CameraSettings.KEY_RECORD_LOCATION,
                    CameraSettings.KEY_VIDEO_EIS,
                    CameraSettings.KEY_VIDEO_FOCUS_MODE,
                    CameraSettings.KEY_EXPOSURE,
                    CameraSettings.KEY_VIDEO_SCENE_MODE,
                    CameraSettings.KEY_VIDEO_DURATION,
                    CameraSettings.KEY_VIDEO_RECORD_AUDIO,
                    CameraSettings.KEY_EDGE,
                    CameraSettings.KEY_HUE,
                    CameraSettings.KEY_SATURATION,
                    CameraSettings.KEY_BRIGHTNESS,
                    CameraSettings.KEY_CONTRAST,
                    CameraSettings.KEY_ANTI_BANDING,
                    CameraSettings.KEY_VIDEO_HD_AUDIO_RECORDING};
        mIndicatorControlContainer.initialize(this, mPreferenceGroup,
                mParameters.isZoomSupported(), SETTING_KEYS, OTHER_SETTING_KEYS);

        mIndicatorControlContainer.setListener(this);
        mPopupGestureDetector = new GestureDetector(this,
                new PopupGestureListener());
        updateSceneModeUI();
        if (effectsActive()) {
            mIndicatorControlContainer.overrideSettings(
                    CameraSettings.KEY_VIDEO_QUALITY,
                    Integer.toString(CamcorderProfile.QUALITY_MTK_MEDIUM));
        }
    }


    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(VideoCamera.this);

            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                if (effectsActive() && mEffectsRecorder != null) {
                    mEffectsRecorder.setOrientationHint(
                            mOrientationCompensation % 360);
                }

                setOrientationIndicator(mOrientationCompensation);
                // Do not rotate the icons during recording because the video
                // orientation is fixed after recording.
                if (!mMediaRecorderRecording) {
                    if (mIsVideoWallPaperIntent && mPreviewing) {
                    	onSharedPreferenceChanged();
                    }
                }
            }

            // Show the toast after getting the first orientation changed.
            if (mHandler.hasMessages(SHOW_TAP_TO_SNAPSHOT_TOAST)) {
                mHandler.removeMessages(SHOW_TAP_TO_SNAPSHOT_TOAST);
                showTapToSnapshotToast();
            }
        }
    }

    private void setOrientationIndicator(int orientation) {
        Rotatable[] indicators = {mThumbnailView, mModePicker, mSharePopup,
                mBgLearningMessageRotater, mIndicatorControlContainer, mOnScreenIndicators,
                mReviewDoneButton, mReviewPlayButton, mReviewCancelButton, mRotateDialog, mFocusAreaIndicator};
        if (mMediaRecorderRecording && (mFocusAreaIndicator != null)) {
            mFocusAreaIndicator.setOrientation(orientation);
            return;
        }
        for (Rotatable indicator : indicators) {
            if (indicator != null) indicator.setOrientation(orientation);
        }

        // We change the orientation of the linearlayout only for phone UI because when in portrait
        // the width is not enough.
        
        //MTK modified:because some lcd's width is not long enough to completely contain
        //mLabelsLinearLayout,always use vertical orientation. 
        /*
        if (mLabelsLinearLayout != null) {
            if (((orientation / 90) & 1) == 1) {
                mLabelsLinearLayout.setOrientation(mLabelsLinearLayout.VERTICAL);
            } else {
                mLabelsLinearLayout.setOrientation(mLabelsLinearLayout.HORIZONTAL);
            }
        }
        }*/
        if (mStorageHint != null) mStorageHint.setOrientation(orientation);
        mRecordingTimeRect.setOrientation(mOrientationCompensation);
    }

    private void startPlayVideoActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mCurrentVideoUri, convertOutputFormatToMimeType(mProfile.fileFormat));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Couldn't view video " + mCurrentVideoUri, ex);
        }
    }

    @OnClickAttr
    public void onThumbnailClicked(View v) {
        if (!mMediaRecorderRecording && mThumbnail != null) {
            showSharePopup();
        }
    }

    @OnClickAttr
    public void onReviewRetakeClicked(View v) {
        deleteCurrentVideo();
        hideAlert();
    }

    @OnClickAttr
    public void onReviewPlayClicked(View v) {
        startPlayVideoActivity();
    }

    @OnClickAttr
    public void onReviewDoneClicked(View v) {
        doReturnToCaller(true);
    }

    @OnClickAttr
    public void onReviewCancelClicked(View v) {
    	if (STOP_RECORDING_ASYNC) {
	    	mStoppingAction = STOP_RETURN_UNVALID;
	        stopVideoRecordingAsync();
    	} else {
    		stopVideoRecording();
            doReturnToCaller(false);
    	}
    }

    private void onStopVideoRecording(boolean valid) {
        mStopVideoRecording = true;
        mEffectsDisplayResult = true;
        if (STOP_RECORDING_ASYNC) {
        	if (LOGI) Log.i(TAG, "onStopVideoRecording valid = " + String.valueOf(valid));
	        if (mIsVideoCaptureIntent) {
	            if (mQuickCapture) {
	            	if (valid) {
	            		mStoppingAction = STOP_RETURN;
	            	} else {
	            		mStoppingAction = STOP_RETURN_UNVALID;
	            	}
	            } else if (!effectsActive()) {
	            	mStoppingAction = STOP_SHOW_ALERT;
	            }
	        }
	        stopVideoRecordingAsync();
        } else {
        	stopVideoRecording();
            if (mIsVideoCaptureIntent) {
                if (mQuickCapture) {
                    doReturnToCaller(valid);
                } else if (!effectsActive()) {
                    showAlert();
                }
            } else if (!effectsActive()) {
                getThumbnail();
            }
        }
        //add for camera pause feature, clear pref
        clearLongTapToPausePref();
    }

    public void onProtectiveCurtainClick(View v) {
        // Consume clicks
    }

    @Override
    public void onShutterButtonClick() {
    	// performClick action may has been posted to queue in viewRootImpl
        if (collapseCameraControls() || mPausePerformed || !mEnableRecordBtn || !mSurfaceReady) return;

        boolean stop = mMediaRecorderRecording;
        boolean pause = mMediaRecoderRecordingPaused;

        if (stop) {
        	if (!mMediaRecoderRecordingPaused) {
                onStopVideoRecording(true);
            } else {
                try {
                    mMediaRecorder.start(); // Recording is now started
                    //update indicator to recording icon
                    mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(
                    		mRecording, null, null, null);
                    mShutterButton.setBackgroundResource(R.drawable.btn_shutter_video_recording);
                    mRecordingStartTime = SystemClock.uptimeMillis() - mVideoRecordedDuration;
                    mVideoRecordedDuration = 0;
                    mMediaRecoderRecordingPaused = false;
                } catch (RuntimeException e) {
                    Log.e("Camera", "Could not start media recorder. ", e);
                    Toast.makeText(this, R.string.toast_video_recording_not_available,
                		Toast.LENGTH_LONG).show();
                    releaseMediaRecorder();
                    return;
                }
            }
        } else {
            mSnapButtonInvisible = false;
            mStopVideoRecording = false;
            if ((mCameraId == mFrontCameraId) || effectsActive()) {
                mSnapButtonInvisible = true;
            }
            startVideoRecording();
        }
        mShutterButton.setEnabled(false);

        // Keep the shutter button disabled when in video capture intent
        // mode and recording is stopped. It'll be re-enabled when
        // re-take button is clicked.
        if (!(mIsVideoCaptureIntent && stop)) {
            mHandler.sendEmptyMessageDelayed(
                    ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
        } else if (pause) {
    		//resume recording from pause
    		mHandler.sendEmptyMessageDelayed(
                    ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
        }
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
    	if (!pressed) {
    		mPausePerformed = false;
    	}
    }

    private OnScreenHint mStorageHint;

    private void updateAndShowStorageHint() {
        mStorageSpace = Storage.getAvailableSpace();
        updateRemainTimeString();
        VideoCamera.this.runOnUiThread(new Runnable() {
        	public void run() {
        		if (mRecordingTimeView != null && !mMediaRecorderRecording) {
            		setTime(mRemainTimeString);
            	}
        	}
        });
        showStorageHint();
    }

    private void showStorageHint() {
    	this.runOnUiThread(new Runnable(){
    		public void run() {
    			String errorMessage = null;
    	        if (mStorageSpace == Storage.UNAVAILABLE) {
    	            errorMessage = getString(R.string.no_storage);
    	        } else if (mStorageSpace == Storage.PREPARING) {
    	            errorMessage = getString(R.string.preparing_sd);
    	        } else if (mStorageSpace == Storage.UNKNOWN_SIZE) {
    	            errorMessage = getString(R.string.access_sd_fail);
    	        } else if (mStorageSpace < Storage.LOW_STORAGE_THRESHOLD) {
    	            errorMessage = getString(Util.getNotEnoughSpaceAlertMessageId());
    	        }

    	        if (errorMessage != null) {
    	            if (mStorageHint == null) {
    	                mStorageHint = OnScreenHint.makeText(VideoCamera.this, errorMessage);
    	            } else {
    	                mStorageHint.setText(errorMessage);
    	            }
    	            mStorageHint.show();
    	            mStorageHint.setOrientation(mOrientationCompensation);
    	        } else if (mStorageHint != null) {
    	            mStorageHint.cancel();
    	            mStorageHint = null;
    	        }
    		}
    	});
    }

    private void readVideoPreferences() {
    	
    	//merge mediatek scene mode
    	mParameters = mCameraDevice.getParameters();
    	getScenePreference();
    	
        // The preference stores values from ListPreference and is thus string type for all values.
        // We need to convert it to int manually.
        String defaultQuality = CameraSettings.getDefaultVideoQuality(mCameraId,
                getResources().getString(R.string.pref_video_quality_default));
        String videoQuality =
                mPreferences.getString(CameraSettings.KEY_VIDEO_QUALITY,
                        defaultQuality);
        int quality = Integer.valueOf(videoQuality);
        
        Intent intent = getIntent();
        if (mIsVideoWallPaperIntent) {
        	quality = CamcorderProfile.QUALITY_MTK_HIGH;
        } else {
	        // Set video quality.
	        if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
	            int extraVideoQuality =
	                    intent.getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
	            if (extraVideoQuality > 0) {
	            	if (CamcorderProfile.hasProfile(mCameraId, extraVideoQuality)) {
	            		quality = extraVideoQuality;
	            	} else {
	            		if (CamcorderProfile.hasProfile(mCameraId,
	            				CamcorderProfile.QUALITY_MTK_MEDIUM)) {
		            		quality = CamcorderProfile.QUALITY_MTK_MEDIUM;
		            	} else {
		            		quality = CamcorderProfile.QUALITY_MTK_HIGH;
		            	}
	            	}
	            } else {  // 0 is mms.
	                quality = CamcorderProfile.QUALITY_MTK_LOW;
	            }
	        }
        }

        // Set video duration limit. The limit is read from the preference,
        // unless it is specified in the intent.
        if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
            int seconds =
                    intent.getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0);
            mMaxVideoDurationInMs = 1000 * seconds;
        } else {
        	String minutes = mPreferences.getString(CameraSettings.KEY_VIDEO_DURATION,
        			getResources().getString(R.string.pref_camera_video_duration_default));
            if (minutes.equals("-1")) {
            	CamcorderProfile profile = CamcorderProfile.getMtk(CamcorderProfile.QUALITY_MTK_LOW);
            	if (profile != null) {
            		mMaxVideoDurationInMs = 1000 * profile.duration;
            	} else {
            		mMaxVideoDurationInMs = 1000 * 30;
            	}
            } else {
                mMaxVideoDurationInMs = 60 * 1000 * Integer.parseInt(minutes);
            }
        }

        // Set effect
        mEffectType = CameraSettings.readEffectType(mPreferences);
        if (mEffectType != EffectsRecorder.EFFECT_NONE) {
            mEffectParameter = CameraSettings.readEffectParameter(mPreferences);
            // Set quality to HIGH for effects, unless intent is overriding it
            if (!intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
            	if (CamcorderProfile.hasProfile(
            			mCameraId, CamcorderProfile.QUALITY_MTK_LIVE_EFFECT)) {
            		quality = CamcorderProfile.QUALITY_MTK_LIVE_EFFECT;
            	} else {
	            	if (mCameraId == 0) {
	            		quality = CamcorderProfile.QUALITY_MTK_MEDIUM;
	            	} else {
	            		quality = CamcorderProfile.QUALITY_MTK_HIGH;
	            	}
            	}
            }
            // On initial startup, can get here before indicator control is
            // enabled. In that case, UI quality override handled in
            // initializeIndicatorControl.
            if (mIndicatorControlContainer != null) {
                mIndicatorControlContainer.overrideSettings(
                        CameraSettings.KEY_VIDEO_QUALITY,
                        Integer.toString(CamcorderProfile.QUALITY_MTK_MEDIUM));
            }
        } else {
            mEffectParameter = null;
            if (mIndicatorControlContainer != null) {
                mIndicatorControlContainer.overrideSettings(
                        CameraSettings.KEY_VIDEO_QUALITY,
                        null);
            }
        }
        Log.i(TAG, "readVideoPreferences -> mEffectType = " + mEffectType);
        // Read time lapse recording interval.
        String frameIntervalStr = mPreferences.getString(
                CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                getString(R.string.pref_video_time_lapse_frame_interval_default));
        mTimeBetweenTimeLapseFrameCaptureMs = Integer.parseInt(frameIntervalStr);

        mCaptureTimeLapse = (mTimeBetweenTimeLapseFrameCaptureMs != 0);
        // TODO: This should be checked instead directly +1000.
        if (mCaptureTimeLapse) quality += 1000;
        mProfile = CamcorderProfile.getMtk(mCameraId, quality);
        boolean bitrateChanged = false;
        if (mQualityId != quality) {
        	mQualityId = quality;
			bitrateChanged = true;			
        }

        mRecordAudio = "on".equals(mPreferences.getString(CameraSettings.KEY_VIDEO_RECORD_AUDIO,
        		getString(R.string.pref_camera_recordaudio_default)));

        mVideoSizeManager.getVideoSize(mProfile, mParameters.getSupportedPreviewSizes(), mOrientationCompensation);
        /*
        //Mediatek compensate install orientation of sensor.
        int camOri = CameraHolder.instance().getCameraInfo()[mCameraId].orientation;
        if (camOri == 180 || camOri == 0) {
        	int temp = mProfile.videoFrameWidth;
        	mProfile.videoFrameWidth = mProfile.videoFrameHeight;
        	mProfile.videoFrameHeight = temp;
        }
		*/
        getDesiredPreviewSize();

        //Mediatek modify to meet lower frame sensor
        List<Integer> supportedFrameRates = mParameters.getSupportedPreviewFrameRates();
        if (!isSupported(mProfile.videoFrameRate, supportedFrameRates)) {
        	int maxFrame = getMaxSupportedPreviewFrameRate(supportedFrameRates);
        	mProfile.videoBitRate = (mProfile.videoBitRate * maxFrame) / mProfile.videoFrameRate;
        	mProfile.videoFrameRate = maxFrame;
			bitrateChanged = true;
        }

        if (Parameters.SCENE_MODE_NIGHT.equals(mSceneMode)) {
        	//TODO check and load night mode profiles.
        	mProfile.videoFrameRate /= 2;
            mProfile.videoBitRate /= 2;
			bitrateChanged = true;			
        }

        if (bitrateChanged) {
        	updateAndShowStorageHint();
        }

        if (mRecordAudio) {
	        String mode = mPreferences.getString(
	        		CameraSettings.KEY_VIDEO_HD_AUDIO_RECORDING,
	        		getString(R.string.video_hd_recording_default));
	        if (mode.equals(pref_camera_video_hd_recording_entryvalues[0])){
	        	mAudioMode = HDRecordMode.NORMAL;
            } else if (mode.equals(pref_camera_video_hd_recording_entryvalues[1])) {
            	mAudioMode = HDRecordMode.INDOOR;
            } else {
            	mAudioMode = HDRecordMode.OUTDOOR;
            }
        } else {
        	mAudioMode = HDRecordMode.NORMAL;
        }
    }

    private void writeDefaultEffectToPrefs()  {
        ComboPreferences.Editor editor = mPreferences.edit();
        editor.putString(CameraSettings.KEY_VIDEO_EFFECT,
                getString(R.string.pref_video_effect_default));
        editor.apply();
    }

    private void getDesiredPreviewSize() {
    	
    	//getParameters move to readVideoPreference.
        //mParameters = mCameraDevice.getParameters();
        if (mParameters.getSupportedVideoSizes() == null || effectsActive()) {
            mDesiredPreviewWidth = mProfile.videoFrameWidth;
            mDesiredPreviewHeight = mProfile.videoFrameHeight;
        } else {  // Driver supports separates outputs for preview and video.
            List<Size> sizes = mParameters.getSupportedPreviewSizes();
            Size preferred = mParameters.getPreferredPreviewSizeForVideo();
            int product = preferred.width * preferred.height;
            Iterator it = sizes.iterator();
            // Remove the preview sizes that are not preferred.
            while (it.hasNext()) {
                Size size = (Size) it.next();
                if (size.width * size.height > product) {
                    it.remove();
                }
            }
            Size optimalSize = Util.getOptimalPreviewSize(this, sizes,
                (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
            mDesiredPreviewWidth = optimalSize.width;
            mDesiredPreviewHeight = optimalSize.height;
        }
        Log.v(TAG, "mDesiredPreviewWidth=" + mDesiredPreviewWidth +
                ". mDesiredPreviewHeight=" + mDesiredPreviewHeight);
    }

    private void resizeForPreviewAspectRatio() {
        if (mProfile.videoFrameWidth < mProfile.videoFrameHeight && !mIsVideoWallPaperIntent) {
        	mPreviewFrameLayout.setAspectRatio(
                    (double) mProfile.videoFrameHeight / mProfile.videoFrameWidth);
        } else {
	        mPreviewFrameLayout.setAspectRatio(
	                (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
        }
    }

    @Override
    protected void doOnResume() {
        if (mOpenCameraFail || mCameraDisabled) return;

        Log.i(TAG, "doOnResume");
        mPausing = false;
        int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
        if (mCameraId != cameraId) {
            CameraSettings.writePreferredCameraId(mPreferences, mCameraId);
        }
        mZoomValue = 0;

        //Mediatek multi Storage
        Storage.updateDefaultDirectory(this, false);

        Log.v(TAG, "showVideoSnapshotUI");
        showVideoSnapshotUI(false);

        // Start orientation listener as soon as possible because it takes
        // some time to get first orientation.
        mOrientationListener.enable();
        if (!mPreviewing) {
            if (resetEffect()) {
                mBgLearningMessageFrame.setVisibility(View.GONE);
                mIndicatorControlContainer.reloadPreferences();
            }
            try {
                mCameraDevice = Util.openCamera(this, mCameraId);
                readVideoPreferences();
                resizeForPreviewAspectRatio();
                startPreview();
                updateSceneModeUI();
            } catch (CameraHardwareException e) {
                Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
                return;
            } catch (CameraDisabledException e) {
                Util.showErrorAndFinish(this, R.string.camera_disabled);
                return;
            }
        }

        // Initializing it here after the preview is started.
        initializeZoom();

        keepScreenOnAwhile();

        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        mReceiver = new MyBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);
        mStorageSpace = Storage.getAvailableSpace();
        
        //mediatek update remaining time
        updateRemainTimeString();
        if (mRemainTimeString != null) {
        	mRecordingTimeView.setVisibility(View.VISIBLE);
        	setTime(mRemainTimeString);
        }

        mHandler.postDelayed(mUpdateHintRunnable, 200);

        Log.v(TAG, "recordLocation initialize");
        // Initialize location sevice.
        boolean recordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());
        mLocationManager.recordLocation(recordLocation);

        if (!mIsVideoCaptureIntent) {
            updateThumbnail();  // Update the last video thumbnail.
            mModePicker.setCurrentMode(ModePicker.MODE_VIDEO);
        }

        if (mPreviewing) {
            mOnResumeTime = SystemClock.uptimeMillis();
            mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
        }
        // Dismiss open menu if exists.
        PopupManager.getInstance(this).notifyShowPopup(null);

    	//make sure RecordingTimeView is showed.
    	mRecordingTimeView.setVisibility(View.VISIBLE);

        if (STOP_RECORDING_ASYNC) {
	        if (isVideoSaving()) {
	        	mRotateDialog.showWaitingDialog(
	        			getResources().getString(R.string.wait));
	        } else {
        		mRotateDialog.dismissDialog();
	        	if (mRestoreRecordUI) {
	        		mRestoreRecordUI = false;
	        		setOrientationIndicator(mOrientationCompensation);
	        		if (!mIsVideoCaptureIntent) {
	        			getThumbnail();
	        		}
	            	restoreVideoUI();
	            } else {
                    enablePreviewFocusingUI(true);
                }
                checkZoomForQuality(true);
	        }
        }
        Log.v(TAG, "onResume end");
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            if (effectsActive()) {
                mEffectsRecorder.setPreviewDisplay(
                        mSurfaceHolder,
                        mSurfaceWidth,
                        mSurfaceHeight);
            } else {
            	if (mSurfaceReady) {
            		mCameraDevice.setPreviewDisplay(holder);
            	}
            }
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private void startPreview() {
        Log.v(TAG, "startPreview");

        mFocusManager.resetTouchFocus();

        mCameraDevice.setErrorCallback(mErrorCallback);
        if (mPreviewing == true) {
            mCameraDevice.stopPreview();
            if (effectsActive() && mEffectsRecorder != null) {
                mEffectsRecorder.release();
            }
            mPreviewing = false;
        }

        mDisplayRotation = Util.getDisplayRotation(this);
        int orientation = Util.getDisplayOrientation(mDisplayRotation, mCameraId);
        mCameraDevice.setDisplayOrientation(orientation);
        setCameraParameters();

        if (!effectsActive()) {
            setPreviewDisplay(mSurfaceHolder);
            try {
                mCameraDevice.startPreview();
            } catch (Throwable ex) {
                closeCamera();
                throw new RuntimeException("startPreview failed", ex);
            }
        } else {
            initializeEffectsPreview();
            Log.v(TAG, "effectsStartPreview");
            mEffectsRecorder.startPreview();
        }

        mZoomState = ZOOM_STOPPED;
        mPreviewing = true;
        mFocusManager.onPreviewStarted();
    }

    private void closeCamera() {
        Log.v(TAG, "closeCamera");
        if (mCameraDevice == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        if (mEffectsRecorder != null) {
            mEffectsRecorder.release();
        }
        mEffectType = EffectsRecorder.EFFECT_NONE;
        CameraHolder.instance().release();
        mCameraDevice.setZoomChangeListener(null);
        mCameraDevice.setErrorCallback(null);
        //touch AE/AF
        mFocusManager.unRegisterCAFCallback(mCameraDevice);
        mFocusManager.onCameraReleased();
        mSingleStartRecording = false;
        mIsAutoFocusCallback = false;
        mCameraDevice = null;
        mPreviewing = false;
        mSnapshotInProgress = false;
    }

    private void finishRecorderAndCloseCamera() {
        // This is similar to what mShutterButton.performClick() does,
        // but not quite the same.
    	if (STOP_RECORDING_ASYNC) {
			if (mMediaRecorderRecording) {
	        	mEffectsDisplayResult = true;
	            if (mIsVideoCaptureIntent) {
	                if (!effectsActive()) mStoppingAction = STOP_SHOW_ALERT;
	            }
	            stopVideoRecordingAsync();
	        } else {
	        	/**
	        	 *  always release media recorder.
	        	 *  if video saving task is ongoing, let SavingTask do this job.
	        	 */
	            if (!effectsActive() && !isVideoSaving()) {
	                releaseMediaRecorder();
	            }
	        }
	        if (isVideoSaving()) {
	        	synchronized(mVideoSavingTask) {
	        		if (!mRecorderCameraReleased) {
		        		try {
				        	if (LOGI) Log.i(TAG, "Wait for camera releasing done in MediaRecorder");
				        	mVideoSavingTask.wait();
			        	} catch(InterruptedException e) {
			        		Log.d(TAG, "Got notify from Media recorder");
			        	}
	        		}
	        	}
	        } else {
	        	Log.d(TAG, "finishRecorderAndCloseCamera, closeVideoFileDescriptor");
	        	/**
	        	 * closeVideoFileDescriptor here
	        	 * if media recorder is stopping in videoSavingTask, do the job later.
	        	 */
	        	if (!mEffectsDisplayResult) {
	        		closeVideoFileDescriptor();
	        	}
	        }
    	} else {
    		if (mMediaRecorderRecording) {
            	mEffectsDisplayResult = true;
                if (mIsVideoCaptureIntent) {
                    stopVideoRecording();
                    if (!effectsActive()) showAlert();
                } else {
                    stopVideoRecording();
                    if (!effectsActive()) getThumbnail();
                }
            } else {
                stopVideoRecording();
            }
        }
        closeCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPausing = true;
        Log.i(TAG, "onPause");

        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.dismissSettingPopup();
        }

        if(!mIsVideoCaptureIntent && isImageSaving()){
            try{
                mStoreSnapImageThread.join();
                } catch (InterruptedException ex) {
             // ignore
                }
        }
        boolean recordState = mMediaRecorderRecording;

        if (mCameraDevice != null ){
            mCameraDevice.cancelAutoFocus();
            }
        mFocusManager.onPreviewStopped();

        finishRecorderAndCloseCamera();
        if (!STOP_RECORDING_ASYNC) closeVideoFileDescriptor();

        if (mSharePopup != null) mSharePopup.dismiss();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        resetScreenOn();

        if (recordState && mThumbnail != null) {
        	mThumbnail.deleteFrom(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
        } else if (!mIsVideoCaptureIntent && mThumbnail != null && !mThumbnail.fromFile()) {
            mThumbnail.saveTo(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
        }

        mHandler.removeCallbacks(mUpdateHintRunnable);
        if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }

        if (mThumbnail != null) {
            mThumbnail = null;
        }
        mOrientationListener.disable();
        mLocationManager.recordLocation(false);

        mHandler.removeMessages(VIDEO_FOCUS_INIT);
        mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
        mFocusManager.removeMessages();
        mIsAutoFocusCallback = false;
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (!mMediaRecorderRecording) keepScreenOnAwhile();
    }

    @Override
    public void onBackPressed() {
        if (mPausing || (STOP_RECORDING_ASYNC && isVideoSaving())) return;
        if (mMediaRecorderRecording) {
            onStopVideoRecording(false);
        } else if (!collapseCameraControls()) {
            setPreference(CameraSettings.KEY_COLOR_EFFECT, Parameters.EFFECT_NONE);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Do not handle any key if the activity is paused.
        if (mPausing) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA:
                if (event.getRepeatCount() == 0) {
                	//note mBgLearningMessageFrame is initialized in onCreate
                    if (mEnableRecordBtn &&
                    		mReviewImage.getVisibility() != View.VISIBLE &&
                    		mBgLearningMessageFrame.getVisibility() == View.GONE) {
                    	if (LOGI) Log.i(TAG, "KEYCODE_CAMERA");
                		mShutterButton.performClick();
                	}
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getRepeatCount() == 0) {
                    if (mEnableRecordBtn &&
                    		mReviewImage.getVisibility() != View.VISIBLE &&
                    		mBgLearningMessageFrame.getVisibility() == View.GONE) {
                		mShutterButton.performClick();
                	}
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (mMediaRecorderRecording) return true;
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA:
                mShutterButton.setPressed(false);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Make sure we have a surface in the holder before proceeding.
        if (holder.getSurface() == null) {
            Log.d(TAG, "holder.getSurface() == null");
            return;
        }

        Log.v(TAG, "surfaceChanged. w=" + w + ". h=" + h);

        mSurfaceHolder = holder;
        mSurfaceWidth = w;
        mSurfaceHeight = h;

        if (mPausing) {
            // We're pausing, the screen is off and we already stopped
            // video recording. We don't want to start the camera again
            // in this case in order to conserve power.
            // The fact that surfaceChanged is called _after_ an onPause appears
            // to be legitimate since in that case the lockscreen always returns
            // to portrait orientation possibly triggering the notification.
            return;
        }

        // The mCameraDevice will be null if it is fail to connect to the
        // camera hardware. In this case we will show a dialog and then
        // finish the activity, so it's OK to ignore it.
        if (mCameraDevice == null) return;

        // Set preview display if the surface is being created. Preview was
        // already started. Also restart the preview if display rotation has
        // changed. Sometimes this happens when the device is held in portrait
        // and camera app is opened. Rotation animation takes some time and
        // display rotation in onCreate may not be what we want.
        if (mPreviewing && (Util.getDisplayRotation(this) == mDisplayRotation)
                && holder.isCreating()) {
            setPreviewDisplay(holder);
            long switchEndTime = System.currentTimeMillis();
            Xlog.i(TAG, "[Performance test][Camera][Camera] camera switch to videocamera end ["+ switchEndTime +"]");
            Xlog.i(TAG, "[Performance test][Camera][VideoCamera] videocamera switch Main To Sub end ["+ switchEndTime +"]");
            Xlog.i(TAG, "[Performance test][Camera][VideoCamera] videocamera switch Sub To Main end ["+ switchEndTime +"]");
        } else {
			if (STOP_RECORDING_ASYNC) {
				if (LOGI) Log.i(TAG, "surfaceChanged --> stopVideoRecordingAsync");
				stopVideoRecordingAsync();
			} else {
				stopVideoRecording();
			}
			if (mRestoringPreference) {
				//TODO There is different way between normal launch and video capture when restore.
				readVideoPreferences();
                resizeForPreviewAspectRatio();
                startPreview();
                updateSceneModeUI();
                updateOnScreenIndicators();
                mRestoringPreference = false;
			} else {
				startPreview();
			}
        }
        if(!mInitFocusFirstTime){
            mHandler.sendEmptyMessage(VIDEO_FOCUS_INIT);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
    }

    private void gotoGallery() {
        MenuHelper.gotoCameraVideoGallery(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return false;
    	/*
        super.onCreateOptionsMenu(menu);

        if (mIsVideoCaptureIntent) {
            // No options menu for attach mode.
            return false;
        } else {
            addBaseMenuItems(menu);
        }
        return true;
        */
    }

    private boolean isVideoCaptureIntent() {
        String action = getIntent().getAction();
        return (MediaStore.ACTION_VIDEO_CAPTURE.equals(action));
    }

    private void doReturnToCaller(boolean valid) {
        Intent resultIntent = new Intent();
        int resultCode;
        if (valid) {
            resultCode = RESULT_OK;
            resultIntent.setData(mCurrentVideoUri);
            if (mIsVideoWallPaperIntent) {
            	Util.setLastUri(mCurrentVideoUri);
            }
        } else {
            resultCode = RESULT_CANCELED;
        }
        setResultEx(resultCode, resultIntent);
        finish();
    }

    private void cleanupEmptyFile() {
        if (mVideoFilename != null) {
            File f = new File(mVideoFilename);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mVideoFilename);
                mVideoFilename = null;
            }
        }
    }

    // Prepares media recorder.
    private void initializeRecorder() {
        Log.v(TAG, "initializeRecorder");
        // If the mCameraDevice is null, then this activity is going to finish
        if (mCameraDevice == null) return;
        if (mSurfaceHolder == null) {
            Log.v(TAG, "Surface holder is null. Wait for surface changed.");
            return;
        }

        Intent intent = getIntent();
        Bundle myExtras = intent.getExtras();

        long requestedSizeLimit = 0;
        closeVideoFileDescriptor();
        if (mIsVideoCaptureIntent && myExtras != null) {
            Uri saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (saveUri != null) {
                try {
                    mVideoFileDescriptor =
                            mContentResolver.openFileDescriptor(saveUri, "rw");
                    mCurrentVideoUri = saveUri;
                } catch (java.io.FileNotFoundException ex) {
                    // invalid uri
                    Log.e(TAG, ex.toString());
                }
            }
            requestedSizeLimit = myExtras.getLong(MediaStore.EXTRA_SIZE_LIMIT);
        }
        mMediaRecorder = new MediaRecorder();

        // Unlock the camera object before passing it to media recorder.
        mCameraDevice.unlock();
        mMediaRecorder.setCamera(mCameraDevice);
        if (!mCaptureTimeLapse && mRecordAudio) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //mMediaRecorder.setProfile(mProfile);
        mMediaRecorder.setOutputFormat(mProfile.fileFormat);
        mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
        mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
        if (!mCaptureTimeLapse && mRecordAudio) {
        	mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
        	mMediaRecorder.setAudioChannels(mProfile.audioChannels);
        	mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
        	mMediaRecorder.setAudioEncoder(mProfile.audioCodec);
        	if (MTK_AUDIO_HD_REC_SUPPORT) {
        		mMediaRecorder.setHDRecordMode(mAudioMode, true);
        	}
        }

        mMediaRecorder.setMaxDuration(mMaxVideoDurationInMs);
        if (mCaptureTimeLapse) {
            mMediaRecorder.setCaptureRate((1000 / (double) mTimeBetweenTimeLapseFrameCaptureMs));
            mMediaRecorder.setTimeLapseEnable();
        }

        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mMediaRecorder.setLocation((float) loc.getLatitude(),
                    (float) loc.getLongitude());
        }

        // Set output file.
        // Try Uri in the intent first. If it doesn't exist, use our own
        // instead.
        if (mVideoFileDescriptor != null) {
            mMediaRecorder.setOutputFile(mVideoFileDescriptor.getFileDescriptor());
        } else {
            generateVideoFilename(mProfile.fileFormat);
            mMediaRecorder.setOutputFile(mVideoFilename);
        }

        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        /**
		 * Set maximum file size.
		 * Change LOW_STORAGE_THRESHOLD to RECORD_LOW_STORAGE_THRESHOLD,
         * make it more likely that recording can match the file size.
         */
        long maxFileSize = mStorageSpace - Storage.RECORD_LOW_STORAGE_THRESHOLD;
        if (requestedSizeLimit > 0 && requestedSizeLimit < maxFileSize) {
            maxFileSize = requestedSizeLimit;
        }

        try {
            mMediaRecorder.setMaxFileSize(maxFileSize);
        } catch (RuntimeException exception) {
            // We are going to ignore failure of setMaxFileSize here, as
            // a) The composer selected may simply not support it, or
            // b) The underlying media framework may not handle 64-bit range
            // on the size restriction.
        }

        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        // Note that mOrientation here is the device orientation, which is the opposite of
        // what activity.getWindowManager().getDefaultDisplay().getRotation() would return,
        // which is the orientation the graphics need to rotate in order to render correctly.
        int rotation = 0;
        if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - mOrientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + mOrientation) % 360;
            }
        } else {
        	//Get the right original orientation
        	CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
            rotation = info.orientation;
        }
        mMediaRecorder.setOrientationHint(rotation);
        mOrientationCompensationAtRecordStart = mOrientationCompensation;

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFilename, e);
            releaseMediaRecorder();
            throw new RuntimeException(e);
        }

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);
        if (STOP_RECORDING_ASYNC) {
        	mMediaRecorder.setOnCameraReleasedListener(this);
        }
    }

    private void initializeEffectsPreview() {
        Log.v(TAG, "initializeEffectsPreview");
        // If the mCameraDevice is null, then this activity is going to finish
        if (mCameraDevice == null) return;

        boolean inLandscape =
                (getRequestedOrientation() ==
                 ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];

        mEffectsDisplayResult = false;
        mEffectsRecorder = new EffectsRecorder(this);

        // TODO: Confirm none of the foll need to go to initializeEffectsRecording()
        // and none of these change even when the preview is not refreshed.
        mEffectsRecorder.setAppToLandscape(inLandscape);
        mEffectsRecorder.setCamera(mCameraDevice);
        mEffectsRecorder.setCameraFacing(info.facing);
        mEffectsRecorder.setProfile(mProfile);
        mEffectsRecorder.setEffectsListener(this);
        mEffectsRecorder.setOnInfoListener(this);
        mEffectsRecorder.setOnErrorListener(this);
        mEffectsRecorder.setSurfaceStateListener(this);

        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        int rotation = 0;
        if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            rotation = mOrientationCompensation % 360;
        }
        mEffectsRecorder.setOrientationHint(rotation);

        mOrientationCompensationAtRecordStart = mOrientationCompensation;

        mEffectsRecorder.setPreviewDisplay(
                mSurfaceHolder,
                mSurfaceWidth,
                mSurfaceHeight);

        if (mEffectType == EffectsRecorder.EFFECT_BACKDROPPER &&
                ((String) mEffectParameter).equals(EFFECT_BG_FROM_GALLERY)) {
            mEffectsRecorder.setEffect(mEffectType, mEffectUriFromGallery);
        } else {
            mEffectsRecorder.setEffect(mEffectType, mEffectParameter);
        }
    }

    private void initializeEffectsRecording() {
        Log.v(TAG, "initializeEffectsRecording");

        Intent intent = getIntent();
        Bundle myExtras = intent.getExtras();

        long requestedSizeLimit = 0;
        closeVideoFileDescriptor();
        if (mIsVideoCaptureIntent && myExtras != null) {
            Uri saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (saveUri != null) {
                try {
                    mVideoFileDescriptor =
                            mContentResolver.openFileDescriptor(saveUri, "rw");
                    mCurrentVideoUri = saveUri;
                } catch (java.io.FileNotFoundException ex) {
                    // invalid uri
                    Log.e(TAG, ex.toString());
                }
            }
            requestedSizeLimit = myExtras.getLong(MediaStore.EXTRA_SIZE_LIMIT);
        }

        mEffectsRecorder.setProfile(mProfile);
        mEffectsRecorder.setMuteAudio(!mRecordAudio);
        // important to set the capture rate to zero if not timelapsed, since the
        // effectsrecorder object does not get created again for each recording
        // session
        if (mCaptureTimeLapse) {
            mEffectsRecorder.setCaptureRate((1000 / (double) mTimeBetweenTimeLapseFrameCaptureMs));
        } else {
            mEffectsRecorder.setCaptureRate(0);
        }

        // Set output file
        if (mVideoFileDescriptor != null) {
            mEffectsRecorder.setOutputFile(mVideoFileDescriptor.getFileDescriptor());
        } else {
            generateVideoFilename(mProfile.fileFormat);
            mEffectsRecorder.setOutputFile(mVideoFilename);
        }

        // Set maximum file size.
        long maxFileSize = mStorageSpace - Storage.LOW_STORAGE_THRESHOLD;
        if (requestedSizeLimit > 0 && requestedSizeLimit < maxFileSize) {
            maxFileSize = requestedSizeLimit;
        }
        mEffectsRecorder.setMaxFileSize(maxFileSize);
        mEffectsRecorder.setMaxDuration(mMaxVideoDurationInMs);
    }

    private void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            cleanupEmptyFile();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mRecorderCameraReleased = true;
        }
        mVideoFilename = null;
    }

    private void releaseEffectsRecorder() {
        Log.v(TAG, "Releasing effects recorder.");
        if (mEffectsRecorder != null) {
            cleanupEmptyFile();
            mEffectsRecorder.release();
            mEffectsRecorder = null;
        }
        mVideoFilename = null;
    }

    private void generateVideoFilename(int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = createName(dateTaken);
        // Used when emailing.
        String filename = title + convertOutputFormatToFileExt(outputFileFormat);
        String mime = convertOutputFormatToMimeType(outputFileFormat);
        mVideoFilename = Storage.DIRECTORY + '/' + filename;
        mCurrentVideoValues = new ContentValues(7);
        mCurrentVideoValues.put(Video.Media.TITLE, title);
        mCurrentVideoValues.put(Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(Video.Media.DATA, mVideoFilename);
        mCurrentVideoValues.put(Video.Media.RESOLUTION,
                Integer.toString(mProfile.videoFrameWidth) + "x" +
                Integer.toString(mProfile.videoFrameHeight));
        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mCurrentVideoValues.put(Video.Media.LATITUDE, loc.getLatitude());
            mCurrentVideoValues.put(Video.Media.LONGITUDE, loc.getLongitude());
        }
        Log.v(TAG, "New video filename: " + mVideoFilename);
    }

    private long getDuration() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mCurrentVideoFilename);
        return Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    private void addVideoToMediaStore() {
        if (mVideoFileDescriptor == null) {
            Uri videoTable = Uri.parse("content://media/external/video/media");
            mCurrentVideoValues.put(Video.Media.SIZE,
                    new File(mCurrentVideoFilename).length());
            long duration = getDuration();
            if (duration > 0) {
                mCurrentVideoValues.put(Video.Media.DURATION, duration);
            } else {
                Log.w(TAG, "Video duration <= 0 : " + duration);
            }
            try {
                mCurrentVideoUri = mContentResolver.insert(videoTable,
                        mCurrentVideoValues);
                sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_VIDEO,
                        mCurrentVideoUri));
            } catch (Exception e) {
                // We failed to insert into the database. This can happen if
                // the SD card is unmounted.
                mCurrentVideoUri = null;
                mCurrentVideoFilename = null;
            } finally {
                Log.v(TAG, "Current video URI: " + mCurrentVideoUri);
            }
        }
        mCurrentVideoValues = null;
    }

    private void deleteCurrentVideo() {
        // Remove the video and the uri if the uri is not passed in by intent.
        if (mCurrentVideoFilename != null) {
            deleteVideoFile(mCurrentVideoFilename);
            mCurrentVideoFilename = null;
            if (mCurrentVideoUri != null) {
                mContentResolver.delete(mCurrentVideoUri, null, null);
                mCurrentVideoUri = null;
            }
        }
        updateAndShowStorageHint();
    }

    private void deleteVideoFile(String fileName) {
        Log.v(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    private void addBaseMenuItems(Menu menu) {
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_CAMERA, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_CAMERA);
            }
        });
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_PANORAMA, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_PANORAMA);
            }
        });
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_MAV, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_MAV);
            }
        });

        if (mNumberOfCameras > 1) {
            menu.add(R.string.switch_camera_id)
                    .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    CameraSettings.writePreferredCameraId(mPreferences,
                            ((mCameraId == mFrontCameraId)
                            ? mBackCameraId : mFrontCameraId));
                    onSharedPreferenceChanged();
                    return true;
                }
            }).setIcon(android.R.drawable.ic_menu_camera);
        }
    }

    private PreferenceGroup filterPreferenceScreenByIntent(
            PreferenceGroup screen) {
        Intent intent = getIntent();
        if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
            CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_VIDEO_QUALITY);
        }

        if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
            CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_VIDEO_QUALITY);
        }
        
        if (mIsVideoWallPaperIntent) {
        	CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_VIDEO_QUALITY);
        	CameraSettings.removePreferenceFromScreen(screen,
        			CameraSettings.KEY_VIDEO_EFFECT);
        }
        
        if (mIsVideoCaptureIntent) {
        	CameraSettings.removePreferenceFromScreen(screen,
        			CameraSettings.KEY_VIDEO_DURATION);
        }
        return screen;
    }

    // from MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "MediaRecorder error. what=" + what + ". extra=" + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            // We may have run out of space on the sdcard.
            if (STOP_RECORDING_ASYNC) {
        		stopVideoRecordingAsync();
        	} else {
        		stopVideoRecording();
            }
        } else if (extra == MediaRecorder.MEDIA_RECORDER_ENCODER_ERROR) {
        	onStopVideoRecording(true);
        	mRotateDialog.showAlertDialog(
        			getString(R.string.camera_error_title),
        			getString(R.string.video_encoder_error),
        			getString(R.string.dialog_ok),  null, null, null);
        }
    }

    // from MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            if (mMediaRecorderRecording) onStopVideoRecording(true);
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            if (mMediaRecorderRecording) onStopVideoRecording(true);

            // Show the toast.
            Toast.makeText(this, R.string.video_reach_size_limit,
                    Toast.LENGTH_LONG).show();
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_CAMERA_RELEASE) {
        	if (mVideoSavingTask != null) {
        		synchronized(mVideoSavingTask) {
        			if (LOGI) Log.i(TAG, "MediaRecorder camera released, notify job wait for camera release");
        			mRecorderCameraReleased = true;
        			mVideoSavingTask.notifyAll();
        		}
        	}
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_START_TIMER) {
            if (!mCaptureTimeLapse) {
	            mRecordingStartTime = SystemClock.uptimeMillis();
	            mStartTimeLapse = mRecordingStartTime - mRecordStartCalledTime;
	            // used in effect record , it is always bigger than 1500, then times=2 is OK
	            if (1500 < mStartTimeLapse) {
	            	mEquallyDurationTimes--;
	            }
	            updateRecordingTime();
            }
        }
    }

    /*
     * Make sure we're not recording music playing in the background, ask the
     * MediaPlaybackService to pause playback.
     */
    private void pauseAudioPlayback() {
        // Shamelessly copied from MediaPlaybackService.java, which
        // should be public, but isn't.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");

        sendBroadcast(i);
    }

    // For testing.
    public boolean isRecording() {
        return mMediaRecorderRecording;
    }

    private void startVideoRecording() {
        Log.v(TAG, "startVideoRecording");

        if(mSingleAutoModeSupported && isContinousFocusMode()){
            mSingleStartRecording = true;
            setAutoFocusMode();
        }
        updateAndShowStorageHint();
        if (mStorageSpace < Storage.LOW_STORAGE_THRESHOLD) {
            Log.v(TAG, "Storage issue, ignore the start request");
            return;
        }

        mCurrentVideoUri = null;
        if (effectsActive()) {
            initializeEffectsRecording();
            if (mEffectsRecorder == null) {
                Log.e(TAG, "Fail to initialize effect recorder");
                return;
            }
        } else {
            initializeRecorder();
            if (mMediaRecorder == null) {
                Log.e(TAG, "Fail to initialize media recorder");
                return;
            }
        }

        pauseAudioPlayback();

        if (effectsActive()) {
            try {
                mEffectsRecorder.startRecording();
            } catch (RuntimeException e) {
                Log.e(TAG, "Could not start effects recorder. ", e);
                releaseEffectsRecorder();
                return;
            }
        } else {
            try {
                mMediaRecorder.start(); // Recording is now started
            } catch (RuntimeException e) {
                Log.e(TAG, "Could not start media recorder. ", e);
                releaseMediaRecorder();
                // If start fails, frameworks will not lock the camera for us.
                mCameraDevice.lock();
                return;
            }
        }
        enableCameraControls(false);

        mRecorderCameraReleased = false;
        mStoppingAction = STOP_NORMAL;
        mMediaRecorderRecording = true;
        showRecordingUI(true);
        mEquallyDurationTimes = 3;
        
        mRecordStartLapseTimes = 0;
        mRecordingStartTime = SystemClock.uptimeMillis();
        mRecordStartCalledTime = SystemClock.uptimeMillis();
        mStartTimeLapse = 0;
    	updateRecordingTime();
        if (!mCaptureTimeLapse) {
        	// we just update recordingtime once,because it will be called in onInfo(xxx).
        	// update time to 00:00 or 00:30
            mHandler.removeMessages(UPDATE_RECORD_TIME);
        }

        //add for camera pause feature
        mMediaRecoderRecordingPaused = false;
        mVideoRecordedDuration = 0;

        keepScreenOn();

        //show video pause hint if needed
        showVideoPauseHintInFirstTime();
    }

    private void showRecordingUI(boolean recording) {
        if (recording) {
            mIndicatorControlContainer.dismissSecondLevelIndicator();
            if (mThumbnailView != null) mThumbnailView.setEnabled(false);
            mShutterButton.setBackgroundResource(R.drawable.btn_shutter_video_recording);
            setTime("");
            mRecordingTimeView.setVisibility(View.VISIBLE);
            if (mReviewControl != null) mReviewControl.setVisibility(View.GONE);
            if (mCaptureTimeLapse) {
                if (Util.isTabletUI()) {
                    ((IndicatorControlWheelContainer) mIndicatorControlContainer)
                            .startTimeLapseAnimation(
                                    mTimeBetweenTimeLapseFrameCaptureMs,
                                    mRecordingStartTime);
                }
            }
        } else {
            if (mThumbnailView != null) mThumbnailView.setEnabled(true);
            mShutterButton.setBackgroundResource(R.drawable.btn_shutter_video);
            //mRecordingTimeView.setVisibility(View.GONE);
            if (mRemainTimeString != null) {
            	setTime(mRemainTimeString);
            }
            mRecordingTimeView.setVisibility(View.VISIBLE);
            if (mReviewControl != null) mReviewControl.setVisibility(View.VISIBLE);
            if (mCaptureTimeLapse) {
                if (Util.isTabletUI()) {
                    ((IndicatorControlWheelContainer) mIndicatorControlContainer)
                            .stopTimeLapseAnimation();
                }
            }
        }
    }

    private void getThumbnail() {
        if (mCurrentVideoUri != null) {
            Bitmap videoFrame = Thumbnail.createVideoThumbnail(mCurrentVideoFilename,
                    mPreviewFrameLayout.getWidth());
            if (videoFrame != null) {
                mThumbnail = new Thumbnail(mCurrentVideoUri, videoFrame, 0);
				mThumbnailUpdated = true;
                mThumbnailView.setBitmap(mThumbnail.getBitmap());
                // Share popup may still have the reference to the old thumbnail. Clear it.
                mSharePopup = null;
            }
        }
    }

    private void showAlert() {
        Bitmap bitmap = null;
        if (mVideoFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnail(mVideoFileDescriptor.getFileDescriptor(),
                    mPreviewFrameLayout.getWidth());
        } else if (mCurrentVideoFilename != null) {
            bitmap = Thumbnail.createVideoThumbnail(mCurrentVideoFilename,
                    mPreviewFrameLayout.getWidth());
        }
        if (bitmap != null) {
            // MetadataRetriever already rotates the thumbnail. We should rotate
            // it to match the UI orientation (and mirror if it is front-facing camera).
            CameraInfo[] info = CameraHolder.instance().getCameraInfo();
            boolean mirror = (info[mCameraId].facing == CameraInfo.CAMERA_FACING_FRONT);
            bitmap = Util.rotateAndMirror(bitmap, -mOrientationCompensationAtRecordStart,
                    mirror);
            mReviewImage.setImageBitmap(bitmap);
            mReviewImage.setVisibility(View.VISIBLE);
        }

        Util.fadeOut(mShutterButton);
        Util.fadeOut(mIndicatorControlContainer);
        int[] pickIds = {R.id.btn_retake, R.id.btn_done, R.id.btn_play};
        for (int id : pickIds) {
            Util.fadeIn(findViewById(id));
        }

        showTimeLapseUI(false);
    }

    private void hideAlert() {
        mReviewImage.setVisibility(View.GONE);
        mShutterButton.setEnabled(true);
        enableCameraControls(true);

        int[] pickIds = {R.id.btn_retake, R.id.btn_done, R.id.btn_play};
        for (int id : pickIds) {
            Util.fadeOut(findViewById(id));
        }
        Util.fadeIn(mShutterButton);
        Util.fadeIn(mIndicatorControlContainer);

        if (mCaptureTimeLapse) {
            showTimeLapseUI(true);
        }
    }

    private void stopVideoRecording() {
        Log.v(TAG, "stopVideoRecording");
        if (mMediaRecorderRecording) {
            boolean shouldAddToMediaStoreNow = false;
	        if (mMediaRecoderRecordingPaused) {
	        	mRecordingStartTime = SystemClock.uptimeMillis() - mVideoRecordedDuration;
	        }
            try {
                if (effectsActive()) {
                    // This is asynchronous, so we can't add to media store now because thumbnail
                    // may not be ready. In such case addVideoToMediaStore is called later
                    // through a callback from the MediaEncoderFilter to EffectsRecorder,
                    // and then to the VideoCamera.
                    mEffectsRecorder.stopRecording();
                } else {
                    mMediaRecorder.setOnErrorListener(null);
                    mMediaRecorder.setOnInfoListener(null);
                    mMediaRecorder.stop();
                    shouldAddToMediaStoreNow = true;
                }
                mCurrentVideoFilename = mVideoFilename;
                Log.v(TAG, "Setting current video filename: "
                        + mCurrentVideoFilename);
            } catch (RuntimeException e) {
                Log.e(TAG, "stop fail",  e);
                if (mVideoFilename != null) deleteVideoFile(mVideoFilename);
            }
            updateAndShowStorageHint();
            resetRecordingTimeViewUI();
            mMediaRecorderRecording = false;
            showRecordingUI(false);
            if (!mIsVideoCaptureIntent) {
                enableCameraControls(true);
            }
            // The orientation was fixed during video recording. Now make it
            // reflect the device orientation as video recording is stopped.
            setOrientationIndicator(mOrientationCompensation);
            keepScreenOnAwhile();
            if (shouldAddToMediaStoreNow) {
                addVideoToMediaStore();
            }
        }
        // always release media recorder
        if (!effectsActive()) {
            releaseMediaRecorder();
        }
    }

    private void resetScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }

    private void keepScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void updateThumbnail() {
		final ContentResolver contentResolver = getContentResolver();		
        if ((mThumbnail == null || !Util.isUriValid(mThumbnail.getUri(), contentResolver))) {
			if (mThumbnail != null) {//means that the thumbnail is not valid.
				mThumbnail = null;
        		updateThumbnailButton();
    		}
			
			mThumbnailUpdated = false;
			new Thread() {
				@Override
				public void run() {						
					Log.d(TAG, "Thumbnail.getLastThumbnail >>>");
	        		Thumbnail tb = Thumbnail.getLastThumbnail(contentResolver);
					Log.d(TAG, "Thumbnail.getLastThumbnail <<< is null:" + (tb == null));
					mHandler.sendMessage(mHandler.obtainMessage(MSG_GET_THUMBNAIL_DONE,0,0,tb));					
				}
			}.start();
       	}
    }

    private void updateThumbnailButton() {
    	if (!mIsVideoCaptureIntent) {
    		//mThumbnailView will be null in VideoCaptureIntent mode
	        if (mThumbnail != null) {
	            mThumbnailView.setBitmap(mThumbnail.getBitmap());
	        } else {
	            mThumbnailView.setBitmap(null);
	        }
    	}
    }

    private void initThumbnailButton() {
		mThumbnailUpdated = false;
        // Load the thumbnail from the disk.
		new Thread() {
			@Override
			public void run() {	
				Log.d(TAG, "Thumbnail.loadFrom >>>");
	            Thumbnail tb = Thumbnail.loadFrom(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
				Log.d(TAG, "Thumbnail.loadFrom <<< is null:" + (tb == null));
					
				ContentResolver contentResolver = getContentResolver();		
				if ((tb == null || !Util.isUriValid(tb.getUri(), contentResolver))) {
					Log.d(TAG, "Thumbnail.getLastThumbnail >>>");
					tb = Thumbnail.getLastThumbnail(contentResolver);
					Log.d(TAG, "Thumbnail.getLastThumbnail <<< is null:" + (tb == null));
        		}
				mHandler.sendMessage(mHandler.obtainMessage(MSG_GET_THUMBNAIL_DONE,0,0,tb));					
    		}
		}.start();    
        updateThumbnailButton();
    }
	
    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / MILLISECOND; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);

            timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }

        return timeStringBuilder.toString();
    }

    private long getTimeLapseVideoLength(long deltaMs) {
        // For better approximation calculate fractional number of frames captured.
        // This will update the video time at a higher resolution.
        double numberOfFrames = (double) deltaMs / mTimeBetweenTimeLapseFrameCaptureMs;
        return (long) (numberOfFrames / mProfile.videoFrameRate * 1000);
    }

    private void updateRecordingTime() {
    	Log.v(TAG, "updateRecordingTime mStartTimeLapse=" + mStartTimeLapse);
        if (!mMediaRecorderRecording || isVideoSaving()) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        long delta = now - mRecordingStartTime;

        if (mMediaRecoderRecordingPaused) {
            delta = mVideoRecordedDuration;
        }

        if (mEquallyDurationTimes > 0 && !mCaptureTimeLapse) {
        	delta = delta + mStartTimeLapse;
        }

        // Starting a minute before reaching the max duration
        // limit, we'll countdown the remaining time instead.
        boolean countdownRemainingTime = (mMaxVideoDurationInMs != 0
                && delta >= mMaxVideoDurationInMs - 60000);

        long deltaAdjusted = delta;
        if (countdownRemainingTime) {
            deltaAdjusted = Math.max(0, mMaxVideoDurationInMs - deltaAdjusted) + 999;
        }
        String text;

        long targetNextUpdateDelay;
        if (!mCaptureTimeLapse) {
    		MILLISECOND = (int) (1000 + ((!countdownRemainingTime && mEquallyDurationTimes > 0) ? mStartTimeLapse / 3 : 0));
        	text = millisecondToTimeString(deltaAdjusted, false);
            targetNextUpdateDelay = 1000;
        } else {
        	MILLISECOND = 1000;
            // The length of time lapse video is different from the length
            // of the actual wall clock time elapsed. Display the video length
            // only in format hh:mm:ss.dd, where dd are the centi seconds.
            text = millisecondToTimeString(getTimeLapseVideoLength(delta), true);
            targetNextUpdateDelay = mTimeBetweenTimeLapseFrameCaptureMs;
        }

        // the time between the time when mediarecorder.start and the time when video is truly recording
        // is about 200~300ms,we will share the timelapse to the first 3 seconds.
        // it means that 1100 ms in every seconds.
        if (mEquallyDurationTimes > 0 && !mCaptureTimeLapse) {
        	targetNextUpdateDelay = targetNextUpdateDelay + mStartTimeLapse / 3;
        }
        if (!mMediaRecoderRecordingPaused) {
        	mEquallyDurationTimes--;
        }

        mCurrentShowIndicator = 1 - mCurrentShowIndicator;
        if (mMediaRecoderRecordingPaused && 1 == mCurrentShowIndicator) {
        	mRecordingTimeView.setVisibility(View.INVISIBLE);
        } else {
        	mRecordingTimeView.setVisibility(View.VISIBLE);
        }
        setTime(text);

        if (mRecordingTimeCountsDown != countdownRemainingTime) {
            // Avoid setting the color on every update, do it only
            // when it needs changing.
            mRecordingTimeCountsDown = countdownRemainingTime;

            int color = getResources().getColor(countdownRemainingTime
                    ? R.color.recording_time_remaining_text
                    : R.color.recording_time_elapsed_text);

            mRecordingTimeView.setTextColor(color);
        }

        long actualNextUpdateDelay = 500;
        if (!mMediaRecoderRecordingPaused) {
        	actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
        }
        Log.i(TAG, "actualNextUpdateDelay: " + String.valueOf(actualNextUpdateDelay));
        if (mHandler.hasMessages(UPDATE_RECORD_TIME)) {
            mHandler.removeMessages(UPDATE_RECORD_TIME);
        }
        mHandler.sendEmptyMessageDelayed(
                UPDATE_RECORD_TIME, actualNextUpdateDelay);
    }

    private static boolean isSupported(Object value, List<?> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    private void setCameraParameters() {
        mParameters = mCameraDevice.getParameters();

        //setParameters may happen at this block,
        //so make sure It's execute before setPreviewSize
        //or make sure preview is not ongoing now.
        if (mSceneChanged) {
        	mParameters.setSceneMode(mSceneMode);
            mCameraDevice.setParameters(mParameters);

            // Setting scene mode will change the settings of flash mode,
            // white balance, and focus mode. Here we read back the
            // parameters, so we can know those settings.
            mParameters = mCameraDevice.getParameters();
            mHandler.sendEmptyMessage(UPDATE_STORAGE);
        }
        
        mParameters.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
        mParameters.setPreviewFrameRate(mProfile.videoFrameRate);

        if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {

        	// Set white balance parameter.
	        String whiteBalance = mPreferences.getString(
	                CameraSettings.KEY_WHITE_BALANCE,
	                getString(R.string.pref_camera_whitebalance_default));
	        if (isSupported(whiteBalance,
	                mParameters.getSupportedWhiteBalance())) {
	            mParameters.setWhiteBalance(whiteBalance);
	        } else {
	            whiteBalance = mParameters.getWhiteBalance();
	            if (whiteBalance == null) {
	                whiteBalance = Parameters.WHITE_BALANCE_AUTO;
	            }
	        }
	        
	        //Image adjustment
	        String hue = mPreferences.getString(
		            CameraSettings.KEY_HUE,
		            getString(R.string.pref_camera_hue_default));
			if (isSupported(hue, mParameters.getSupportedHueMode())){
				mParameters.setHueMode(hue);
			}
			String brightness = mPreferences.getString(
	                CameraSettings.KEY_BRIGHTNESS,
	                getString(R.string.pref_camera_brightness_default));
			if (isSupported(brightness, mParameters.getSupportedBrightnessMode())){
				mParameters.setBrightnessMode(brightness);
			}
	        String edge = mPreferences.getString(
	                CameraSettings.KEY_EDGE,
	                getString(R.string.pref_camera_edge_default));
	        if (isSupported(edge, mParameters.getSupportedEdgeMode())){
	            mParameters.setEdgeMode(edge);
	        }
	        String saturation = mPreferences.getString(
	                        CameraSettings.KEY_SATURATION,
	                        getString(R.string.pref_camera_saturation_default));
	        if (isSupported(saturation, mParameters.getSupportedSaturationMode())){
	            mParameters.setSaturationMode(saturation);
	        }
	        String contrast = mPreferences.getString(
	                    CameraSettings.KEY_CONTRAST,
	                    getString(R.string.pref_camera_edge_default));
	        if (isSupported(contrast, mParameters.getSupportedContrastMode())){
	            mParameters.setContrastMode(contrast);
	        }

	        //TODO check here (google default logic has been change here)
	        // Set continuous auto focus. 
	        List<String> supportedFocus = mParameters.getSupportedFocusModes();
	        String focusMode = mPreferences.getString(
	        		CameraSettings.KEY_VIDEO_FOCUS_MODE,
	        		getString(R.string.pref_camera_video_focusmode_default));
	        if (isSupported(focusMode, supportedFocus)) {
	            setFocusParameters();
	        }
        } else {
            String focusMode = mParameters.getFocusMode();
            if ((Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) && (mFocusManager != null)) {
                Log.v(TAG,"unRegisterCAFCallback");
                mFocusManager.unRegisterCAFCallback(mCameraDevice);
            }
        }
            

        // Set flash mode.
        String flashMode = mPreferences.getString(
                CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                getString(R.string.pref_camera_video_flashmode_default));
        List<String> supportedFlash = mParameters.getSupportedFlashModes();
        if (isSupported(flashMode, supportedFlash)) {
            mParameters.setFlashMode(flashMode);
        } else {
            flashMode = mParameters.getFlashMode();
            if (flashMode == null) {
                flashMode = getString(
                        R.string.pref_camera_flashmode_no_flash);
            }
        }

        // Set exposure compensation
        int value = CameraSettings.readExposure(mPreferences);
        int max = mParameters.getMaxExposureCompensation();
        int min = mParameters.getMinExposureCompensation();
        if (value >= min && value <= max) {
            mParameters.setExposureCompensation(value);
        } else {
            Log.w(TAG, "invalid exposure range: " + value);
        }
        
        // Set zoom.
        setZoomParameter();
        mParameters.setRecordingHint(true);

        String eis = mPreferences.getString(CameraSettings.KEY_VIDEO_EIS,
        		getString(R.string.pref_camera_eis_default));
        // Enable video stabilization. Convenience methods not available in API
        // level <= 14
        String vstabSupported = mParameters.get("video-stabilization-supported");
        if ("true".equals(vstabSupported)) {
        	if ("on".equals(eis)) {
        		mParameters.set("video-stabilization", "true");
        	} else {
        		mParameters.set("video-stabilization", "false");
        	}
        }

        String antibanding = mPreferences.getString(
                CameraSettings.KEY_ANTI_BANDING,
                getString(R.string.pref_camera_antibanding_default));
        if (isSupported(antibanding, mParameters.getSupportedAntibanding())){
            mParameters.setAntibanding(antibanding);
        }

        String colorEffect = mPreferences.getString(
                CameraSettings.KEY_COLOR_EFFECT,
                getString(R.string.pref_camera_coloreffect_default));
		if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
			mParameters.setColorEffect(colorEffect);
		}

        // Set picture size.
        // The logic here is different from the logic in still-mode camera.
        // There we determine the preview size based on the picture size, but
        // here we determine the picture size based on the preview size.
        if (mParameters.isVideoSnapshotSupported()){
             List<Size> supported = mParameters.getSupportedPictureSizes();
             Size optimalSize = Util.getOptimalVideoSnapshotPictureSize(supported,
                 (double) mDesiredPreviewWidth / mDesiredPreviewHeight);
             Size original = mParameters.getPictureSize();
             if (!original.equals(optimalSize)) {
                 mParameters.setPictureSize(optimalSize.width, optimalSize.height);
             }
             Log.v(TAG, "Video snapshot size is " + optimalSize.width + "x" +
                 optimalSize.height);
        } else {
          mParameters.setPictureSize(mDesiredPreviewWidth,mDesiredPreviewHeight);
        }

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                CameraProfile.QUALITY_HIGH);
        mParameters.setJpegQuality(jpegQuality);

        mParameters.setCameraMode(Parameters.CAMERA_MODE_MTK_VDO);
        mCameraDevice.setParameters(mParameters);
        // Keep preview size up to date.
        mParameters = mCameraDevice.getParameters();
    }

    private boolean switchToOtherMode(int mode) {
        if (isFinishing()) return false;
        mHandler.removeMessages(VIDEO_FOCUS_INIT);
        MenuHelper.gotoMode(mode, this);
        finish();
        return true;
    }

    public boolean onModeChanged(int mode) {
        if (mode != ModePicker.MODE_VIDEO) {
            return switchToOtherMode(mode);
        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EffectsRecorder.EFFECT_BACKDROPPER:
                if (resultCode == RESULT_OK) {
                    // onActivityResult() runs before onResume(), so this parameter will be
                    // seen by startPreview from onResume()
                    mEffectUriFromGallery = ((Uri) data.getData()).toString();
                    Log.v(TAG, "Received URI from gallery: " + mEffectUriFromGallery);
                    mResetEffect = false;
                } else {
                    mEffectUriFromGallery = null;
                    Log.w(TAG, "No URI from gallery");
                    mResetEffect = true;
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result sent to Camera!");
                break;
        }
    }

    @Override
    public void onEffectsUpdate(int effectId, int effectMsg) {
    	Log.i(TAG, "onEffectsUpdate : " + String.valueOf(effectMsg));
        if (effectMsg == EffectsRecorder.EFFECT_MSG_EFFECTS_STOPPED) {
            // Effects have shut down. Hide learning message if any,
            // and restart regular preview.
            mBgLearningMessageFrame.setVisibility(View.GONE);
            checkQualityAndStartPreview();
            checkZoomForQuality(true);
			if (mRestoringPreference) {
				if (mIsVideoCaptureIntent) {
					// In current setting, default video size be same with effect size in videocapture mode.
					// So SurfaceChanged will not be trigger, so move some block to here.
					mRestoringPreference = false;
					updateSceneModeUI();
					updateOnScreenIndicators();
				}
			}
        } else if (effectMsg == EffectsRecorder.EFFECT_MSG_RECORDING_DONE) {
            // TODO: This assumes the codepath from onStopVideoRecording.  It
            // does not appear to cause problems for the other codepaths, but
            // should be properly thought through.
        	updateEffectRecordingUI();
        	if (mStoppingAction != STOP_RETURN_UNVALID) {
                addVideoToMediaStore();
                if (mIsVideoCaptureIntent) {
                    if (!mQuickCapture) {
                        showAlert();
                    }
                } else {
                    getThumbnail();
                }
        	} else {
        		setResultEx(RESULT_CANCELED);
        		finish();
        	}
            if (mPausing) {
            	closeVideoFileDescriptor();
            }
        } else if (effectMsg == EffectsRecorder.EFFECT_MSG_PREVIEW_RUNNING) {
            // Enable the shutter button once the preview is complete.
        	updateEffectRecordingUI();
        } else if (effectId == EffectsRecorder.EFFECT_BACKDROPPER) {
            switch (effectMsg) {
                case EffectsRecorder.EFFECT_MSG_STARTED_LEARNING:
                    mBgLearningMessageFrame.setVisibility(View.VISIBLE);
                    break;
                case EffectsRecorder.EFFECT_MSG_DONE_LEARNING: {
                	//make sure recording UI is reseted.
                	updateEffectRecordingUI();
                }
                case EffectsRecorder.EFFECT_MSG_SWITCHING_EFFECT:
                    mBgLearningMessageFrame.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void onCancelBgTraining(View v) {
        // Remove training message
        mBgLearningMessageFrame.setVisibility(View.GONE);
        // Write default effect out to shared prefs
        writeDefaultEffectToPrefs();
        // Tell the indicator controller to redraw based on new shared pref values
        mIndicatorControlContainer.reloadPreferences();
        // Tell VideoCamer to re-init based on new shared pref values.
        onSharedPreferenceChanged();
    }

    @Override
    public synchronized void onEffectsError(Exception exception, String fileName) {
        // TODO: Eventually we may want to show the user an error dialog, and then restart the
        // camera and encoder gracefully. For now, we just delete the file and bail out.
        if (fileName != null && new File(fileName).exists()) {
            deleteVideoFile(fileName);
        }
        if (exception instanceof MediaRecorderStopException) {
            Log.w(TAG, "Problem recoding video file. Removing incomplete file.");
            updateEffectRecordingUI();
            return;
        }
        if (!mPausing) {
        	Util.showErrorAndFinish(this, R.string.video_live_effect_error);
        }
		//throw new RuntimeException("Error during recording!", exception);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void onOverriddenPreferencesClicked() {
    }

    public void onRestorePreferencesClicked() {
        Runnable runnable = new Runnable() {
            public void run() {
                restorePreferences();
            }
        };
        mRotateDialog.showAlertDialog(
                getString(R.string.confirm_restore_title),
                getString(R.string.confirm_restore_message),
                getString(android.R.string.ok), runnable,
                getString(android.R.string.cancel), null);
    }

    private void restorePreferences() {
        // Reset the zoom. Zoom value is not stored in preference.
        if (mParameters.isZoomSupported()) {
            mZoomValue = 0;
            setCameraParameters();
            mZoomControl.setZoomIndex(0);
        }

        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.dismissSettingPopup();
            CameraSettings.restorePreferences(this, mPreferences,
                    mParameters);
            mIndicatorControlContainer.reloadPreferences();
            mRestoringPreference = true;
            onSharedPreferenceChanged();
        }
    }

    private boolean effectsActive() {
        return (mEffectType != EffectsRecorder.EFFECT_NONE);
    }

    public void onSharedPreferenceChanged() {
        // ignore the events after "onPause()" or preview has not started yet
        if (mPausing) return;
        Log.i(TAG, "onSharedPreferenceChanged");
        synchronized (mPreferences) {
            // If mCameraDevice is not ready then we can set the parameter in
            // startPreview().
            if (mCameraDevice == null) return;

            boolean recordLocation = RecordLocationPreference.get(
                    mPreferences, getContentResolver());
            mLocationManager.recordLocation(recordLocation);

            // Check if the current effects selection has changed
            if (updateEffectSelection()) {
            	updateSceneModeUI();
            	return;
            }

            Log.i(TAG, "Get Effect Type in onSharedPreferenceChanged mEffectType = " + mEffectType);
            // Check if camera id is changed.
            int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
            if (mCameraId != cameraId) {
            	mIndicatorControlContainer.disableCameraPicker();
                // Restart the activity to have a crossfade animation.
                // TODO: Use SurfaceTexture to implement a better and faster
                // animation.
                Intent intent;
                if (mIsVideoCaptureIntent) {
                    // If the intent is video capture, stay in video capture mode.
                    intent = getIntent();
                } else {
                    intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
                }
                // To maintain the same background in background replacer, we
                // need to send the background video uri via the Intent (apart
                // from the condition that the effects should not be reset).
                intent.putExtra(BACKGROUND_URI_GALLERY_EXTRA, mEffectUriFromGallery);
                intent.putExtra(RESET_EFFECT_EXTRA, false);
                mHandler.removeCallbacksAndMessages(null);
                MenuHelper.gotoVideoMode(this, intent);
                finish();
            } else {
                readVideoPreferences();
                if (effectsActive() && 
                		!(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)) {
                	if (mProfile.videoFrameWidth > mProfile.videoFrameHeight) {
                		//just workaround for scale issue, this job should be do in effectsRecorder.
                		int tmp = mProfile.videoFrameWidth;
                		mProfile.videoFrameWidth = mProfile.videoFrameHeight;
                		mProfile.videoFrameHeight = tmp;
                	}
                }
                showTimeLapseUI(mCaptureTimeLapse);
                // We need to restart the preview if preview size is changed.
                Size size = mParameters.getPreviewSize();
                if (size.width != mDesiredPreviewWidth
                        || size.height != mDesiredPreviewHeight) {
                    if (!effectsActive()) {
                        mCameraDevice.stopPreview();
                    } else {
                        mEffectsRecorder.release();
                    }
                    resizeForPreviewAspectRatio();
                    startPreview(); // Parameters will be set in startPreview().
                } else {
                    setCameraParameters();
                }
                updateSceneModeUI();
            }
        }
        updateOnScreenIndicators();
        checkZoomForQuality(true);
        mRestoringPreference = false;
    }

    private boolean updateEffectSelection() {
        int previousEffectType = mEffectType;
        Object previousEffectParameter = mEffectParameter;
        mEffectType = CameraSettings.readEffectType(mPreferences);
        mEffectParameter = CameraSettings.readEffectParameter(mPreferences);

        if (mEffectType == previousEffectType) {
            if (mEffectType == EffectsRecorder.EFFECT_NONE) return false;
            if (mEffectParameter.equals(previousEffectParameter)) return false;
        }
        Log.v(TAG, "New effect selection: " + mPreferences.getString(
                CameraSettings.KEY_VIDEO_EFFECT, "none"));

        if (mEffectType == EffectsRecorder.EFFECT_NONE) {
            // Stop effects and return to normal preview
            mEffectsRecorder.stopPreview();
            return true;
        }
        if (mEffectType == EffectsRecorder.EFFECT_BACKDROPPER &&
            ((String) mEffectParameter).equals(EFFECT_BG_FROM_GALLERY)) {
            // Request video from gallery to use for background
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setDataAndType(Video.Media.EXTERNAL_CONTENT_URI,
                             "video/*");
            i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(i, EffectsRecorder.EFFECT_BACKDROPPER);
            return true;
        }
        if (previousEffectType == EffectsRecorder.EFFECT_NONE) {
            // Stop regular preview and start effects.
            mCameraDevice.stopPreview();
            checkQualityAndStartPreview();
        } else {
            // Switch currently running effect
            mEffectsRecorder.setEffect(mEffectType, mEffectParameter);
        }
        return true;
    }

    // Verifies that the current preview view size is correct before starting
    // preview. If not, resets the surface holder and resizes the view.
    private void checkQualityAndStartPreview() {
        readVideoPreferences();
        showTimeLapseUI(mCaptureTimeLapse);
        Size size = mParameters.getPreviewSize();
        if (size.width != mDesiredPreviewWidth
                || size.height != mDesiredPreviewHeight) {
            resizeForPreviewAspectRatio();
        } else {
            // Start up preview again
            startPreview();
        }
    }

    private void showTimeLapseUI(boolean enable) {
        if (mTimeLapseLabel != null) {
            mTimeLapseLabel.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    private void showSharePopup() {
        Uri uri = mThumbnail.getUri();
        if (mSharePopup == null || !uri.equals(mSharePopup.getUri())) {
            mSharePopup = new SharePopup(this, uri, mThumbnail.getBitmap(),
                    mOrientationCompensation, mPreviewPanel);
        }
        mSharePopup.showAtLocation(mThumbnailView, Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        // Check if the popup window should be dismissed first.
        if (mPopupGestureDetector != null && mPopupGestureDetector.onTouchEvent(m)) {
            return true;
        }
        return super.dispatchTouchEvent(m);
    }

    private class PopupGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            // Check if the popup window is visible.
            View popup = mIndicatorControlContainer.getActiveSettingPopup();
            if (popup == null) return false;

            // Let popup window or indicator wheel handle the event by
            // themselves. Dismiss the popup window if users touch on other
            // areas.
            if (!Util.pointInView(e.getX(), e.getY(), popup)
                    && !Util.pointInView(e.getX(), e.getY(), mIndicatorControlContainer)) {
                mIndicatorControlContainer.dismissSettingPopup();
                // Let event fall through.
            }
            return false;
        }
    }

    private class ZoomChangeListener implements ZoomControl.OnZoomChangedListener {
        // only for immediate zoom
        @Override
        public void onZoomValueChanged(int index) {
            VideoCamera.this.onZoomValueChanged(index);
        }

        // only for smooth zoom
        @Override
        public void onZoomStateChanged(int state) {
            if (mPausing) return;

            Log.v(TAG, "zoom picker state=" + state);
            if (state == ZoomControl.ZOOM_IN) {
                VideoCamera.this.onZoomValueChanged(mZoomMax);
            } else if (state == ZoomControl.ZOOM_OUT){
                VideoCamera.this.onZoomValueChanged(0);
            } else {
                mTargetZoomValue = -1;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mCameraDevice.stopSmoothZoom();
                }
            }
        }
    }

    private void initializeZoom() {
        Log.v(TAG, "initializeZoom");
        mZoomControl = (ZoomControl) findViewById(R.id.zoom_control);
        // Get the parameter to make sure we have the up-to-date zoom value.
        mParameters = mCameraDevice.getParameters();
        if (!mParameters.isZoomSupported()) return;

        mZoomMax = mParameters.getMaxZoom();
        // Currently we use immediate zoom for fast zooming to get better UX and
        // there is no plan to take advantage of the smooth zoom.
        mZoomControl.setZoomMax(mZoomMax);
        mZoomControl.setZoomIndex(mParameters.getZoom());
        mZoomControl.setSmoothZoomSupported(mSmoothZoomSupported);
        mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
        //mCameraDevice.setZoomChangeListener(mZoomListener);
    }

    private final class ZoomListener
            implements android.hardware.Camera.OnZoomChangeListener {
        @Override
        public void onZoomChange(int value, boolean stopped, android.hardware.Camera camera) {
            Log.v(TAG, "Zoom changed: value=" + value + ". stopped=" + stopped);
            mZoomValue = value;

            // Update the UI when we get zoom value.
            mZoomControl.setZoomIndex(value);

            // Keep mParameters up to date. We do not getParameter again in
            // takePicture. If we do not do this, wrong zoom value will be set.
            mParameters.setZoom(value);

            if (stopped && mZoomState != ZOOM_STOPPED) {
                if (mTargetZoomValue != -1 && value != mTargetZoomValue) {
                    mCameraDevice.startSmoothZoom(mTargetZoomValue);
                    mZoomState = ZOOM_START;
                } else {
                    mZoomState = ZOOM_STOPPED;
                }
            }
        }
    }

    private void onZoomValueChanged(int index) {
        // Not useful to change zoom value when the activity is paused.
        if (mPausing) return;

        if (mSmoothZoomSupported) {
            if (mTargetZoomValue != index && mZoomState != ZOOM_STOPPED) {
                mTargetZoomValue = index;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mCameraDevice.stopSmoothZoom();
                }
            } else if (mZoomState == ZOOM_STOPPED && mZoomValue != index) {
                mTargetZoomValue = index;
                mCameraDevice.startSmoothZoom(index);
                mZoomState = ZOOM_START;
            }
        } else {
            mZoomValue = index;
            setZoomParameter();
        }
    }

    private void initializeVideoSnapshot() {
        Log.v(TAG, "initializeVideoSnapshot");
        if (!mIsVideoCaptureIntent) {
            findViewById(R.id.camera_preview).setOnTouchListener(this);
            // Show the tap to focus toast if this is the first start.
            if (mPreferences.getBoolean(
                        CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, true)) {
                // Delay the toast for one second to wait for orientation.
                mHandler.sendEmptyMessageDelayed(SHOW_TAP_TO_SNAPSHOT_TOAST, 1000);
            }
        }
    }

    void showVideoSnapshotUI(boolean enabled) {
        if (!mIsVideoCaptureIntent) {
            mPreviewFrameLayout.showBorder(enabled);
            if ((mQualityId == CamcorderProfile.QUALITY_MTK_1080P) || (mQualityId == QUALITY_ID )) {
                mIndicatorControlContainer.enableZoom(false);
            } else {
                mIndicatorControlContainer.enableZoom(!enabled);
            }
            mShutterButton.setEnabled(!enabled);
            Log.i(TAG, "enable shutter, showVideoSnapshotUI,enabled is " + enabled);
        }
    }

    // Preview area is touched. Take a picture.
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        Log.v(TAG,"onTouch");
        String focusMode = mParameters.getFocusMode();
        if (focusMode == null || Parameters.FOCUS_MODE_INFINITY.equals(focusMode) || (Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)))
            return false;
        if (mPausing || mCameraDevice == null ){
            //|| !mFirstTimeInitialized|| mCameraState == SNAPSHOT_IN_PROGRESS) {
            return false;
        }

        // Check if metering area or focus area is supported.
        if (!mFocusAreaSupported && !mMeteringAreaSupported) return false;
        if(mMediaRecorderRecording){
            setFocusState(START_FOCUSING);
            }
        return mFocusManager.onTouch(e);
    }

    private final class JpegPictureCallback implements PictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte [] jpegData, android.hardware.Camera camera) {
            Log.v(TAG, "onPictureTaken");
            mSnapshotInProgress = false;
            mSnapLocation = mLocation;
            mSnapJpegData = jpegData;
            mStoreSnapHandler.post(mStoreSnapImageThread);
        }
    }

    private void storeImage(final byte[] data, Location loc) {
        long dateTaken = System.currentTimeMillis();
        String title = Util.createJpegName(dateTaken);
        int orientation = Exif.getOrientation(data);
        Size s = mParameters.getPictureSize();
        mSnapUri = Storage.addImage(mContentResolver, title, dateTaken, loc, orientation, data,
                s.width, s.height, Storage.PICTURE_TYPE_JPG,
                MediaStore.Images.Media.STEREO_TYPE_2D);

        /*
        if (mSnapUri != null) {
            // Create a thumbnail whose width is equal or bigger than that of the preview.
            int ratio = (int) Math.ceil((double) mParameters.getPictureSize().width
                    / mPreviewFrameLayout.getWidth());
            int inSampleSize = Integer.highestOneBit(ratio);
            mThumbnail = Thumbnail.createThumbnail(mSnapJpegData, orientation, inSampleSize, mSnapUri);
            mHandler.sendEmptyMessage(UPDATE_SNAP_THUMNAIL);
            
        }else{
            Log.v(TAG,"snapUri is null");
        }
        */
        mHandler.sendEmptyMessage(UPDATE_SNAP_THUMNAIL);
    }

    private boolean resetEffect() {
        if (mResetEffect) {
            String value = mPreferences.getString(CameraSettings.KEY_VIDEO_EFFECT,
                    mPrefVideoEffectDefault);
            if (!mPrefVideoEffectDefault.equals(value)) {
                writeDefaultEffectToPrefs();
                return true;
            }
        }
        mResetEffect = true;
        return false;
    }

    private String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private void closeVideoFileDescriptor() {
        if (mVideoFileDescriptor != null) {
            try {
                mVideoFileDescriptor.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close fd", e);
            }
            mVideoFileDescriptor = null;
            mShouldAddToMediaStoreNow = false;
        }
    }

    private void showTapToSnapshotToast() {
        new RotateTextToast(this, R.string.video_snapshot_hint, mOrientation)
                .show();
        // Clear the preference.
        Editor editor = mPreferences.edit();
        editor.putBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, false);
        editor.apply();
    }
    
    public void dumpSizes(List<Size> sizes, String msg) {
    	Log.i(TAG, msg + " Sizes :");
    	for (Size size : sizes) {
    		Log.i(TAG, "width: " + size.width + " height: " + size.height);
    	}
    }
    
    /**
     * getScenePreference() should only be called by
     * readVideoPreference()  !!
     */
    private void getScenePreference() {
    	mSceneChanged = false;
    	// Since change scene mode may change supported values,
        // Set scene mode first,

    	mSceneMode = mPreferences.getString(
                CameraSettings.KEY_VIDEO_SCENE_MODE,
                getString(R.string.pref_camera_scenemode_default));

        if (isSupported(mSceneMode, mParameters.getSupportedSceneModes())) {
            if (!mSceneMode.equals(mParameters.getSceneMode())) {
            	mSceneChanged = true;
            }
        } else {
        	mSceneMode = mParameters.getSceneMode();
        	if (mSceneMode == null) {
                mSceneMode = Parameters.SCENE_MODE_AUTO;
            }
        }
    }
    
    private void updateSceneModeUI() {
        String eis = (mQualityId == CamcorderProfile.QUALITY_MTK_1080P) ? "off" : null;
    	if (effectsActive()) {
    		overrideEffectCameraSettings(
                    mParameters.getWhiteBalance(), mParameters.getFocusMode(),
                    mParameters.getHueMode(), mParameters.getEdgeMode(),
                    mParameters.getSaturationMode(), mParameters.getContrastMode(),
                    mParameters.getBrightnessMode(), mParameters.getSceneMode(),
                    mParameters.getColorEffect(), "0", mParameters.getAntibanding(),
                    "normal", "off");
    	} else {
    		String audioMode = mRecordAudio ? null : getString(R.string.video_hd_recording_default);
	        // If scene mode is set, we cannot set flash mode, white balance, and
	        // focus mode, instead, we read it from driver
	        if (!Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
	        	overrideEffectCameraSettings(
	                    mParameters.getWhiteBalance(), mParameters.getFocusMode(),
	                    mParameters.getHueMode(), mParameters.getEdgeMode(),
	                    mParameters.getSaturationMode(), mParameters.getContrastMode(),
	                    mParameters.getBrightnessMode(), null, null, null,
	                    null, audioMode, eis);
	        } else {
	        	overrideEffectCameraSettings(null, null, null,
	            		null, null, null, null, null, null, null,
	            		null, audioMode, eis);
	        }
    	}
    }
    
    private void overrideCameraSettings(
            final String whiteBalance, final String focusMode,
            final String hue, final String edge,
            final String saturation, final String contrast,
            final String brightness) {
        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.overrideSettings(
                    CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
                    CameraSettings.KEY_VIDEO_FOCUS_MODE, focusMode,
                    CameraSettings.KEY_HUE, hue,
                    CameraSettings.KEY_EDGE, edge,
                    CameraSettings.KEY_SATURATION, saturation,
                    CameraSettings.KEY_CONTRAST, contrast,
                    CameraSettings.KEY_BRIGHTNESS, brightness);
        }
    }

    private void overrideEffectCameraSettings(
            final String whiteBalance, final String focusMode,
            final String hue, final String edge,
            final String saturation, final String contrast,
            final String brightness, final String sceneMode,
            final String colorEffect, final String ev,
            final String antiBanding,
            final String audioHD, final String eis) {
        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.overrideSettings(
                    CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
                    CameraSettings.KEY_VIDEO_FOCUS_MODE, focusMode,
                    CameraSettings.KEY_HUE, hue,
                    CameraSettings.KEY_EDGE, edge,
                    CameraSettings.KEY_SATURATION, saturation,
                    CameraSettings.KEY_CONTRAST, contrast,
                    CameraSettings.KEY_BRIGHTNESS, brightness,
                    CameraSettings.KEY_VIDEO_SCENE_MODE, sceneMode,
                    CameraSettings.KEY_COLOR_EFFECT, colorEffect,
                    CameraSettings.KEY_EXPOSURE, ev,
                    CameraSettings.KEY_ANTI_BANDING, antiBanding,
                    CameraSettings.KEY_VIDEO_HD_AUDIO_RECORDING, audioHD,
                    CameraSettings.KEY_VIDEO_EIS, eis);
        }
    }

    private int getMaxSupportedPreviewFrameRate(List<Integer> supportedPreviewRate) {
        int maxFrameRate = 0;
        for (int rate : supportedPreviewRate) {
            if (rate > maxFrameRate) {
            	maxFrameRate = rate;
            }
        }
        return maxFrameRate;
    }

    private boolean isVideoWallPaperIntent() {
        Intent intent = getIntent();
        if ((VIDEO_WALL_PAPER.equals(intent.getStringExtra("identity")))) {
        	float aspectio = intent.getFloatExtra("ratio", 1.2f);
        	mVideoSizeManager = new VideoWallPaperVideoSize(aspectio);
        	return true;
        } else {
        	mVideoSizeManager = new NormalVideoSize();
        	return false;
        }
    }
    
    private String getTimeString (long ms){

        long seconds = ms / 1000; // round to nearest

        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        String secondsString = Long.toString(remainderSeconds);
        if (secondsString.length() < 2) {
            secondsString = "0" + secondsString;
        }
        String minutesString = Long.toString(remainderMinutes);
        if (minutesString.length() < 2) {
            minutesString = "0" + minutesString;
        }
        String text = minutesString + ":" + secondsString;
        if (hours > 0) {
            String hoursString = Long.toString(hours);
            if (hoursString.length() < 2) {
                hoursString = "0" + hoursString;
            }
            text = hoursString + ":" + text;
        }
        return text;
    }
    
    private void updateRemainTimeString() {
    	long bytePerMs = ((mProfile.videoBitRate + mProfile.audioBitRate) >> 3) / 1000;
        long ms = mStorageSpace < Storage.LOW_STORAGE_THRESHOLD ?
        		0 : (mStorageSpace - Storage.LOW_STORAGE_THRESHOLD) / bytePerMs;
        mRemainTimeString = getTimeString(ms);
    }

    private void initOnScreenIndicator() {
        Log.v(TAG, "initOnScreenIndicator");
    	mFlashIndicator = (ImageView) findViewById(R.id.onscreen_flash_indicator);
    	mGpsIndicator = (ImageView) findViewById(R.id.onscreen_gps_indicator);
    	mSceneIndicator = (ImageView) findViewById(R.id.onscreen_scene_indicator);
    	mWhiteBalanceIndicator =
                (ImageView) findViewById(R.id.onscreen_white_balance_indicator);
    }
    
    private void updateWhiteBalanceOnScreenIndicator(String value) {
        if (mWhiteBalanceIndicator == null) {
            return;
        }
        if (Parameters.WHITE_BALANCE_AUTO.equals(value)) {
            mWhiteBalanceIndicator.setVisibility(View.GONE);
        } else {
            if (Parameters.WHITE_BALANCE_FLUORESCENT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_fluorescent);
            } else if (Parameters.WHITE_BALANCE_INCANDESCENT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_incandescent);
            } else if (Parameters.WHITE_BALANCE_DAYLIGHT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_sunlight);
            } else if (Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_cloudy);
            } else if (Parameters.WHITE_BALANCE_SHADE.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_shade);
            } else if (Parameters.WHITE_BALANCE_TWILIGHT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_twlight);
            } else if (Parameters.WHITE_BALANCE_TUNGSTEN.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_tungsten);
            } else if (Parameters.WHITE_BALANCE_WARM_FLUORESCENT.equals(value)) {
                mWhiteBalanceIndicator.setImageResource(R.drawable.ic_indicators_warmfluorescent);
            }
            mWhiteBalanceIndicator.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateOnScreenIndicators() {
        boolean isAutoScene = !(Parameters.SCENE_MODE_AUTO.equals(mParameters.getSceneMode()));
        updateSceneOnScreenIndicator(isAutoScene);
        updateFlashOnScreenIndicator(mParameters.getFlashMode());
        updateWhiteBalanceOnScreenIndicator(mParameters.getWhiteBalance());
    }
    
    private void updateSceneOnScreenIndicator(boolean isVisible) {
        if (mSceneIndicator == null) {
            return;
        }
        mSceneIndicator.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
    
    private void updateFlashOnScreenIndicator(String value) {
        if (mFlashIndicator == null) {
            return;
        }
        if (Parameters.FLASH_MODE_TORCH.equals(value)) {
            mFlashIndicator.setImageResource(R.drawable.ic_indicators_landscape_flash_on);
            mFlashIndicator.setVisibility(View.VISIBLE);
        } else if (Parameters.FLASH_MODE_OFF.equals(value)) {
            mFlashIndicator.setVisibility(View.GONE);
        }
    }
    
    public void showGpsOnScreenIndicator(boolean hasSignal) {
        if (mGpsIndicator == null) {
            return;
        }
        if (hasSignal) {
            mGpsIndicator.setImageResource(R.drawable.ic_viewfinder_gps_on);
        } else {
            mGpsIndicator.setImageResource(R.drawable.ic_viewfinder_gps_no_signal);
        }
        mGpsIndicator.setVisibility(View.VISIBLE);
    }

    public void hideGpsOnScreenIndicator() {
        if (mGpsIndicator == null) {
            return;
        }
        mGpsIndicator.setVisibility(View.GONE);
    }

    //Mediatek feature begin ------------------------->
    private static final int STOP_NORMAL = 1;
    private static final int STOP_RETURN = 2;
    private static final int STOP_RETURN_UNVALID = 3;
    private static final int STOP_SHOW_ALERT = 4;

    private static int MILLISECOND = 1000;

    private int mStoppingAction = STOP_NORMAL;

    private boolean mMediaRecoderRecordingPaused = false;
    private boolean mPausePerformed = false;
    private long mVideoRecordedDuration = 0;
    private int mEquallyDurationTimes = 3;
    private int mCurrentShowIndicator = 0;
    private boolean mShouldAddToMediaStoreNow;
    private boolean mRestoreRecordUI = false;
    private boolean mRecorderCameraReleased = true;
    private long mRecordStartLapseTimes = 0;
    private long mRecordStartCalledTime = 0;
    private long mStartTimeLapse = 0;

    private Thread mVideoSavingTask;
    private Runnable mVideoSavedRunnable = new Runnable() {
    	public void run() {
    		if (LOGI) Log.i(TAG, "enter mVideoSavedRunnable, update UI/Video");
            if (!mPausing) {
            	restoreVideoUI();
                // The orientation was fixed during video recording. Now make it
                // reflect the device orientation as video recording is stopped.
                setOrientationIndicator(mOrientationCompensation);
                keepScreenOnAwhile();
                mRotateDialog.dismissDialog();
            } else {
            	mRestoreRecordUI = true;
            	showRecordingUI(false);
                if (mRecordingTimeView != null) {
            		//update indicator to recording icon
            		mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(
            				mRecording, null, null, null);
            	}
            }
            setEnableRecordingUI(true);
            if (mShouldAddToMediaStoreNow) {
                addVideoToMediaStore();
            }
            int action = (mPausing && mStoppingAction != STOP_NORMAL) ? 
            		STOP_SHOW_ALERT : mStoppingAction;
            switch (action) {
                case STOP_NORMAL: {
                	if (!mPausing && !effectsActive()) {
                    	getThumbnail();
                    }
                	break;
                }
                case STOP_SHOW_ALERT: {
                	showAlert();
                	break;
                }
                case STOP_RETURN_UNVALID: {
                	doReturnToCaller(false);
                	break;
                }
                case STOP_RETURN : {
                	doReturnToCaller(true);
                	break;
                }
            }
            if (mPausing && !mEffectsDisplayResult) {
            	closeVideoFileDescriptor();
            }
            if (!mPausing && ((mFocusState == START_FOCUSING) ||(mFocusState == FOCUSING)|| mSingleAutoModeSupported)) {
                changeFocusState();
               return;
           }
            mIsAutoFocusCallback = false;
            if (LOGI) Log.i(TAG, "Quit mVideoSavedRunnable");
    	}
    };

    public void updateEffectRecordingUI() {
    	if (!mPausing) {
        	restoreVideoUI();
            // The orientation was fixed during video recording. Now make it
            // reflect the device orientation as video recording is stopped.
            setOrientationIndicator(mOrientationCompensation);
            keepScreenOnAwhile();
            mRotateDialog.dismissDialog();
        } else {
        	mRestoreRecordUI = true;
        	showRecordingUI(false);
            if (mRecordingTimeView != null) {
        		//update indicator to recording icon
        		mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(
        				mRecording, null, null, null);
        	}
        }
        setEnableRecordingUI(true);
    }

    public void onShutterButtonLongPressed() {
    	if (mMediaRecorderRecording
    			&& !mMediaRecoderRecordingPaused && !effectsActive()) {
    		mPausePerformed = true;
            try {
                mMediaRecorder.pause();
                mShutterButton.setBackgroundResource(R.drawable.btn_shutter_video);
            } catch (RuntimeException e) {
                Log.e("Camera", "Could not pause media recorder. ", e);
            }
            //update indicator to pause icon
            mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(
            		mRecordingPaused, null, null, null);
            mVideoRecordedDuration = SystemClock.uptimeMillis() - mRecordingStartTime;
            mMediaRecoderRecordingPaused = true;
        }
    }

    private void showVideoPauseHintInFirstTime() {
        if (mPreferences.getBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_PAUSE_SHOWN, true)
        		&& !effectsActive()) {
            // Delay the toast for one second 
            mHandler.sendEmptyMessageDelayed(SHOW_LONG_TAP_TO_PAUSE_TOAST, 1000);
        }
    }

    private void showLongTapToPauseToast() {
        new RotateTextToast(this, R.string.toast_video_recording_pause_hint, mOrientationCompensation).show();
    }

    private void clearLongTapToPausePref() {
        // Clear the preference.
        Editor editor = mPreferences.edit();
        editor.putBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_PAUSE_SHOWN, false);
        editor.commit();
    }

    private void stopVideoRecordingAsync() {
        Log.v(TAG, "stopVideoRecordingAsync");
        if (isVideoSaving()) return;
        if (mMediaRecorderRecording) {
	        setEnableRecordingUI(false);
	        if (!mPausing && mMediaRecorderRecording) {
		        mRotateDialog.showWaitingDialog(
		        		getResources().getString(R.string.wait));
	        }

	        mVideoSavingTask = new Thread(new Runnable() {
	    		public void run() {
	    			if (LOGI) Log.i(TAG, "mVideoSavingTask Start -->");

	    			//add saving thread to avoid blocking main thread
	    			mShouldAddToMediaStoreNow = false;
	                if (mMediaRecorderRecording) {
	        	        if (mMediaRecoderRecordingPaused) {
	        	        	mRecordingStartTime = SystemClock.uptimeMillis() - mVideoRecordedDuration;
	        	        }
	                    try {
	                        if (effectsActive()) {
	                            // This is asynchronous, so we can't add to media store now because thumbnail
	                            // may not be ready. In such case addVideoToMediaStore is called later
	                            // through a callback from the MediaEncoderFilter to EffectsRecorder,
	                            // and then to the VideoCamera.
	                            mEffectsRecorder.stopRecording();
	                        } else {
	                            mMediaRecorder.setOnErrorListener(null);
	                            mMediaRecorder.setOnInfoListener(null);
	                            mMediaRecorder.stop();
	                            mMediaRecorder.setOnCameraReleasedListener(null);
	                            mShouldAddToMediaStoreNow = true;
	                        }
	                        mCurrentVideoFilename = mVideoFilename;
	                        Log.v(TAG, "Setting current video filename: " + mCurrentVideoFilename);
	                    } catch (RuntimeException e) {
	                        Log.e(TAG, "stop fail",  e);
	                        if (mVideoFilename != null) deleteVideoFile(mVideoFilename);
	                    }
	                }
	                mMediaRecorderRecording = false;
	                // always release media recorder
	                if (!effectsActive()) {
	                    releaseMediaRecorder();
	                }
	                synchronized(mVideoSavingTask) {
	                	if (LOGI) Log.i(TAG, "MediaRecorder.stop() done, notifyAll");
	        			mVideoSavingTask.notifyAll();
	        			mHandler.removeCallbacks(mVideoSavedRunnable);
	        			if (!effectsActive()) {
	        				mHandler.post(mVideoSavedRunnable);
	        			}
	                    if (LOGI) Log.i(TAG, "<-- Quit mVideoSavingTask");
	        		}
	    		}
	        });
	        mVideoSavingTask.start();
        } else {
        	if (!effectsActive()) {
                releaseMediaRecorder();
            }
        	if (mStoppingAction == STOP_RETURN_UNVALID) {
        		doReturnToCaller(false);
        	}
        }
    }

    public boolean isVideoSaving() {
    	return mVideoSavingTask != null && mVideoSavingTask.isAlive();
    }

    public void resetRecordingTimeViewUI() {
        mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(
        		mRecording, null, null, null);
        int color = getResources().getColor(R.color.recording_time_elapsed_text);
        mRecordingTimeView.setTextColor(color);
        mRecordingTimeCountsDown = false;
    }

    public void restoreVideoUI() {
    	updateAndShowStorageHint();
    	resetRecordingTimeViewUI();
    	showRecordingUI(false);
        if (!mIsVideoCaptureIntent) {
            enableCameraControls(true);
        }
    }

    public void setEnableRecordingUI(boolean enabled) {
    	mEnableRecordBtn = enabled;
    	mHandler.removeMessages(ENABLE_SHUTTER_BUTTON);
        mShutterButton.setEnabled(enabled);
        checkZoomForQuality(enabled);
    }

	public void setTime(String ms) {
		mRecordingTimeView.setTime(ms);
	}	
	
	//in order to improve the zoom performance,we do not use setParameters to avoid
	//frequent GC process(caused by parameters.flatten).Instead,we modify startSmoothZoom()
	//in driver to let it do the same job as setParameters(with updated zoom value) do in driver.
	//That means the parameters saved in driver will also be updated.So we just update the parameters
	//saved in Android framework to keep them sync and call "modified" startSmoothZoom.
    private void setZoomParameter() {
		// Set zoom.
        if (mParameters.isZoomSupported()) {
            mParameters.setZoom(mZoomValue);
        }

        mCameraDevice.startSmoothZoom(mZoomValue);
    }

    public void setPreference(String key, String value) {
		Editor editor = mPreferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

    public void onStateChange(boolean surfaceReady) {
    	Log.i(TAG, "Surface state report ready = " + surfaceReady);
    	mSurfaceReady = surfaceReady;
    	if (mSurfaceReady && !effectsActive()
    			&& !mPausing && mCameraDevice != null) {
    		try {
    			mCameraDevice.setPreviewDisplay(mSurfaceHolder);
    			mPreviewFrameLayout.invalidate();
            } catch (Throwable ex) {
                closeCamera();
                throw new RuntimeException("setPreviewDisplay failed", ex);
            }
    	}
    }

    public interface VideoSizeManager {
    	public void getVideoSize(CamcorderProfile profile, List<Size> previewSizes, int orientation);
    }

    public class NormalVideoSize implements VideoSizeManager{
    	public void getVideoSize(CamcorderProfile profile, List<Size> previewSizes, int orientation) {
    		//nothing
    	}
    }

    public class VideoWallPaperVideoSize implements VideoSizeManager{
    	private float mVideoAspectRatio;

    	public VideoWallPaperVideoSize(float aspectio) {
    		mVideoAspectRatio = aspectio;
    	}

    	public void getVideoSize(CamcorderProfile profile, List<Size> previewSizes, int orientation) {
    		/*
    		float aspectRatio = 1.0f;
    		Size finalSize = previewSizes.get(0);
    		Iterator it = previewSizes.iterator();
    		it.next();
            // Remove the preview sizes that are not preferred.
            while (it.hasNext()) {
                Size size = (Size) it.next();
                aspectRatio = size.width / size.height;
                if (Math.abs(finalSize.width / finalSize.height - mVideoAspectRatio)
                		> Math.abs(aspectRatio - mVideoAspectRatio)) {
                	finalSize = size;
                }
            }
            if (orientation % 180 == 0) {
            	profile.videoFrameWidth = finalSize.height;
            	profile.videoFrameHeight = finalSize.width;
            } else {
            	profile.videoFrameWidth = finalSize.width;
            	profile.videoFrameHeight = finalSize.height;
            }
            */
    		if (Util.dpToPixel(2) == 3) {
	    		if (orientation % 180 == 0) {
	            	profile.videoFrameWidth = 368;
	            	profile.videoFrameHeight = 480;
	            } else {
	            	profile.videoFrameWidth = 640;
	            	profile.videoFrameHeight = 480;
	            }
    		}
    		//TODO should fill supported size suitable in other resolution.
            Log.i(TAG, "VideoSizeManager videoFrameWidth: " + profile.videoFrameWidth + " videoFrameHeight: " + profile.videoFrameHeight);
    	}
    }
    
    //touch AE/AF
    private void initializeVideoFocus(){
        mPreviewFrame = findViewById(R.id.camera_preview);
        mPreviewFrame.setOnTouchListener(this);
        int orientation = Util.getDisplayOrientation(mDisplayRotation, mCameraId);
        CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
        boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
        mFocusManager.initialize(mFocusAreaIndicator, mPreviewFrame, null,
                this, mirror, orientation);
        mInitFocusFirstTime = true;
    }

    private boolean isTouchModeSupported(){
        String defaultFocusMode = getResources().getString(R.string.pref_camera_video_focusmode_default);
        String videoFocusMode = mPreferences.getString(CameraSettings.KEY_VIDEO_FOCUS_MODE,defaultFocusMode);
        return videoFocusMode.equals(defaultFocusMode);
     }

    private void initializeCapabilities(){
        mFocusManager.initializeParameters(mParameters);
        mFocusAreaSupported = (mParameters.getMaxNumFocusAreas() > 0
                && isSupported(Parameters.FOCUS_MODE_AUTO,
                        mParameters.getSupportedFocusModes()));
        mMeteringAreaSupported = (mParameters.getMaxNumMeteringAreas() > 0);
        mAeLockSupported = mParameters.isAutoExposureLockSupported();
        mAwbLockSupported = mParameters.isAutoWhiteBalanceLockSupported();
    }

    private long mFocusStartTime;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, android.hardware.Camera camera) {
            if (mPausing) return;
            Log.v(TAG, "mAutoFocusTime = " + (System.currentTimeMillis() - mFocusStartTime) + "ms"+",mFocusManager.onAutoFocus(focused)");
            setFocusState(FOCUSED);
            mFocusManager.onAutoFocus(focused);
            mIsAutoFocusCallback = true;
        }
    }   

    public void autoFocus() {
        // TODO Auto-generated method stub
        mFocusStartTime = System.currentTimeMillis();
        Log.v(TAG,"autoFocus");
        mCameraDevice.autoFocus(mAutoFocusCallback);
        setFocusState(FOCUSING);
    }

    public void cancelAutoFocus() {
        // TODO Auto-generated method stub
        Log.v(TAG,"cancelAutoFocus");
        if(mCameraDevice != null){
            mCameraDevice.cancelAutoFocus();
        }
        setFocusState(FOCUS_IDLE);
        if(!(mSingleStartRecording && mSingleAutoModeSupported && mIsAutoFocusCallback )) {
            setFocusParameters();
        }
        mIsAutoFocusCallback = false;
    }

    public boolean capture() {
        // TODO Auto-generated method stub
        return false;
    }

    public void startFaceDetection() {
        // TODO Auto-generated method stub
    }

    public void stopFaceDetection() {
        // TODO Auto-generated method stub
    }

    public void setFocusParameters() {
        // TODO Auto-generated method stub
        if (mAeLockSupported) {
            mParameters.setAutoExposureLock(mFocusManager.getAeAwbLock());
        }
        
        if (mAwbLockSupported) {
            mParameters.setAutoWhiteBalanceLock(mFocusManager.getAeAwbLock());
        }

        if ((mFocusAreaSupported) && (!mIsAutoFocusCallback)) {
            mParameters.setFocusAreas(mFocusManager.getFocusAreas());
        }

        if ((mMeteringAreaSupported) && (!mIsAutoFocusCallback)){
            // Use the same area for focus and metering.
            mParameters.setMeteringAreas(mFocusManager.getMeteringAreas());
        }

        //if we do not add this, the AF will not work properly.
        mParameters.setFocusMode(mFocusManager.getFocusMode());
        mCameraDevice.setParameters(mParameters);
        
        if (Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(mFocusManager.getFocusMode())) {
            Log.v(TAG,"registerCAFCallback");
           mFocusManager.registerCAFCallback(mCameraDevice);
        } else {
            Log.v(TAG,"unRegisterCAFCallback");
            mFocusManager.unRegisterCAFCallback(mCameraDevice);
        }
    }

    public void playSound(int soundId) {
        // TODO Auto-generated method stub
    }

    public boolean readyToCapture() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean doSmileShutter() {
        // TODO Auto-generated method stub
        return false;
    }

    private void setAutoFocusMode(){
        if (isSupported(Parameters.FOCUS_MODE_AUTO,mParameters.getSupportedFocusModes())) {
            String focusMode = Parameters.FOCUS_MODE_AUTO;
            mParameters.setFocusMode(focusMode);
            mCameraDevice.setParameters(mParameters);
            Log.v(TAG,"set focus mode is auto");
        }
    }

    private void changeFocusState(){
        Log.v(TAG,"changeFocusState");
        if (mCameraDevice != null) {
            mCameraDevice.cancelAutoFocus();
        }
        mSingleStartRecording = false;
        mIsAutoFocusCallback = false;
        mFocusManager.resetTouchFocus();
        setFocusParameters();
        mFocusManager.updateFocusUI();
    }

    private boolean isContinousFocusMode(){
        String focusMode = mParameters.getFocusMode();
        Log.v(TAG,"isContinousFocusMode,before focus mode is " + focusMode);
        if (Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(focusMode)){
            return true;
        }
        return false;
    }

    private void setFocusState(int state){
        mFocusState = state;
        if(mMediaRecorderRecording ||mPausing ){
            return;
        }
        
        switch (state) {
            case FOCUSING:
                enablePreviewFocusingUI(false);
                break;
           case FOCUS_IDLE:
           case FOCUSED:
               enablePreviewFocusingUI(true);
               break;
           default:
               break;
        }
    }
    private void enablePreviewFocusingUI(boolean enable){
        Log.v(TAG,"enablePreviewFocusingUI,enable is"+enable);
        if (mIndicatorControlContainer != null) {
            mIndicatorControlContainer.setEnabled(enable);
            }
        if (mModePicker != null) {
            mModePicker.setVisibility(View.VISIBLE);
            mModePicker.setEnabled(enable);
            mShutterButton.setEnabled(enable);
        }
    }
    //end Touch AE/AF

    //M:snapshot
    private OnClickListener mSnapShotListener = new OnClickListener(){
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.v(TAG,"mStopVideoRecording is " + mStopVideoRecording);
            if (mStopVideoRecording){
                return;
            }
            Util.setRotationParameter(mParameters, mCameraId, mOrientation);
            Location loc = mLocationManager.getCurrentLocation();
            Util.setGpsParameters(mParameters, loc);
            mCameraDevice.setParameters(mParameters);
            Log.v(TAG, "Video snapshot start");
            mCameraDevice.takePicture(null, null, null, new JpegPictureCallback(loc));
            showVideoSnapshotUI(true);
            mSnapshotButton.setEnabled(false);
            mSnapshotInProgress = true;
        }
    };

    private Thread mStoreSnapImageThread = new Thread(){
        public void run(){
            storeImage(mSnapJpegData,mSnapLocation);
            }
    };

    private void updateSnapThumnail(){
        if(!mPausing){
            mThumbnailUpdated = true;
            if (mThumbnail != null && !mIsVideoCaptureIntent) {
                mThumbnailView.setBitmap(mThumbnail.getBitmap());
            }
        }
        // Share popup may still have the reference to the old thumbnail. Clear it.
        mSharePopup = null;
        Util.broadcastNewPicture(this, mSnapUri);
    }

    private boolean isImageSaving(){
        return mStoreSnapImageThread != null && mStoreSnapImageThread.isAlive();
    }

    //1080P
    public void checkZoomForQuality(boolean enable) {
        if ((mQualityId == CamcorderProfile.QUALITY_MTK_1080P) || (mQualityId == QUALITY_ID )) {
            mZoomControl.setEnabled(false);
            mZoomValue = 0;
            mZoomControl.setZoomIndex(mZoomValue);
            if (!mPausing) setZoomParameter();
        } else {
            mZoomControl.setEnabled(enable);
        }
    }
	//mediatek added end
}
