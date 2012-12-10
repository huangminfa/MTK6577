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

package com.android.camera;

import com.android.camera.ui.CameraPicker;
import com.android.camera.ui.ControlBarIndicatorButton;
import com.android.camera.ui.FaceView;
import com.android.camera.ui.IndicatorControlContainer;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.SharePopup;
import com.android.camera.ui.ZoomControl;

import com.mediatek.xlog.Xlog;
import com.mediatek.featureoption.FeatureOption;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.CameraSound;
import android.hardware.Camera.SmileCallback;
import android.location.Location;
import android.media.CameraProfile;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

/** The Camera activity which can preview and take pictures. */
public class Camera extends ActivityBase implements FocusManager.Listener,
		View.OnTouchListener, ShutterButton.OnShutterButtonListener,
		SurfaceHolder.Callback, ModePicker.OnModeChangeListener,
		FaceDetectionListener, CameraPreference.OnPreferenceChangedListener,
		LocationManager.Listener {

	private static final String TAG = "camera";
	private static final boolean LOGI = true;

	private static final int CROP_MSG = 1;
	private static final int FIRST_TIME_INIT = 2;
	private static final int CLEAR_SCREEN_DELAY = 3;
	private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 4;
	private static final int CHECK_DISPLAY_ROTATION = 5;
	private static final int SHOW_TAP_TO_FOCUS_TOAST = 6;
	private static final int UPDATE_THUMBNAIL = 7;
	protected static final int UPDATE_STORAGE = 8;
	protected static final int PICTURES_SAVING_DONE = 9;
	private static final int MSG_GET_THUMBNAIL_DONE = 10;
	protected static final int UPDATE_CAPTURE_INDICATOR = 11;
	private static final int SHOW_SINGLE3D_GUIDE = 12;

	// The subset of parameters we need to update in setCameraParameters().
	private static final int UPDATE_PARAM_INITIALIZE = 1;
	private static final int UPDATE_PARAM_ZOOM = 2;
	private static final int UPDATE_PARAM_PREFERENCE = 4;
	private static final int UPDATE_PARAM_ALL = -1;

	// When setCameraParametersWhenIdle() is called, we accumulate the subsets
	// needed to be updated in mUpdateSet.
	private int mUpdateSet;

	private static final int SCREEN_DELAY = 2 * 60 * 1000;

	private static final int ZOOM_STOPPED = 0;
	private static final int ZOOM_START = 1;
	private static final int ZOOM_STOPPING = 2;

	private int mZoomState = ZOOM_STOPPED;
	private boolean mSmoothZoomSupported = false;
	private int mZoomValue; // The current zoom value.
	private int mZoomMax;
	private int mTargetZoomValue;
	private ZoomControl mZoomControl;

	private Parameters mParameters;
	private Parameters mInitialParams;
	private boolean mFocusAreaSupported;
	private boolean mMeteringAreaSupported;
	private boolean mAeLockSupported;
	private boolean mAwbLockSupported;

	private MyOrientationEventListener mOrientationListener;
	// The degrees of the device rotated clockwise from its natural orientation.
	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	// The orientation compensation for icons and thumbnails. Ex: if the value
	// is 90, the UI components should be rotated 90 degrees counter-clockwise.
	private int mOrientationCompensation = 0;
	private ComboPreferences mPreferences;

	private static final String sTempCropFilename = "crop-temp";

	private ContentProviderClient mMediaProviderClient;
	private SurfaceHolder mSurfaceHolder = null;
	protected ShutterButton mShutterButton;
	private GestureDetector mPopupGestureDetector;
	private boolean mOpenCameraFail = false;
	private boolean mCameraDisabled = false;
	private boolean mFaceDetectionStarted = false;

	private View mPreviewPanel; // The container of PreviewFrameLayout.
	private PreviewFrameLayout mPreviewFrameLayout;
	private View mPreviewFrame; // Preview frame area.
	protected RotateDialogController mRotateDialog;

	// A popup window that contains a bigger thumbnail and a list of apps to
	// share.
	private SharePopup mSharePopup;
	// The bitmap of the last captured picture thumbnail and the URI of the
	// original picture.
	private Thumbnail mThumbnail;
	// An imageview showing showing the last captured picture thumbnail.
	private RotateImageView mThumbnailView;
	private boolean mThumbnailUpdated;
	private ModePicker mModePicker;
	private FaceView mFaceView;
	private RotateLayout mFocusAreaIndicator;
	private Rotatable mReviewCancelButton;
	private Rotatable mReviewDoneButton;

	// mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.
	private String mCropValue;
	private Uri mSaveUri;

	// Small indicators which show the camera settings in the viewfinder.
	private TextView mExposureIndicator;
	private ImageView mGpsIndicator;
	private ImageView mFlashIndicator;
	private ImageView mSceneIndicator;
	private ImageView mWhiteBalanceIndicator;
	private ImageView mFocusIndicator;
	// A view group that contains all the small indicators.
	private Rotatable mOnScreenIndicators;

	// We use a thread in ImageSaver to do the work of saving images and
	// generating thumbnails. This reduces the shot-to-shot time.
	protected ImageSaver mImageSaver;

	private CameraSound mCameraSound;

	private Runnable mDoSnapRunnable = new Runnable() {
		public void run() {
			mModeActor.onShutterButtonClick();
		}
	};

	private final StringBuilder mBuilder = new StringBuilder();
	private final Formatter mFormatter = new Formatter(mBuilder);
	private final Object[] mFormatterArgs = new Object[1];

	/**
	 * An unpublished intent flag requesting to return as soon as capturing is
	 * completed.
	 * 
	 * TODO: consider publishing by moving into MediaStore.
	 */
	private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";

	// The display rotation in degrees. This is only valid when mCameraState is
	// not PREVIEW_STOPPED.
	private int mDisplayRotation;
	// The value for android.hardware.Camera.setDisplayOrientation.
	private int mDisplayOrientation;
	private boolean mPausing;
	private boolean mFirstTimeInitialized;
	boolean mIsImageCaptureIntent;

	private static final int PREVIEW_STOPPED = 0;
	protected static final int IDLE = 1; // preview is active
	// Focus is in progress. The exact focus state is in Focus.java.
	private static final int FOCUSING = 2;
	private static final int SNAPSHOT_IN_PROGRESS = 3;
	private static final int SELFTIMER_COUNTING = 4;
	protected static final int SAVING_PICTURES = 5;
	private int mCameraState = PREVIEW_STOPPED;
	private boolean mSnapshotOnIdle = false;

	private ContentResolver mContentResolver;
	private boolean mDidRegister = false;

	private LocationManager mLocationManager;

	private final ShutterCallback mShutterCallback = new ShutterCallback();
	private final PostViewPictureCallback mPostViewPictureCallback = new PostViewPictureCallback();
	private final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
	private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private final ZoomListener mZoomListener = new ZoomListener();
	private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

	private long mFocusStartTime;
	private long mCaptureStartTime;
	private long mShutterCallbackTime;
	private long mPostViewPictureCallbackTime;
	private long mRawPictureCallbackTime;
	private long mJpegPictureCallbackTime;
	private long mOnResumeTime;
	private long mPicturesRemaining;
	private byte[] mJpegImageData;

	// These latency time are for the CameraLatency test.
	public long mAutoFocusTime;
	public long mShutterLag;
	public long mShutterToPictureDisplayedTime;
	public long mPictureDisplayedToJpegCallbackTime;
	public long mJpegCallbackFinishTime;

	// This handles everything about focus.
	/* package */FocusManager mFocusManager;
	private String mSceneMode;
	private Toast mNotSelectableToast;
	private Toast mNoShareToast;

	/* package */final Handler mHandler = new MainHandler();
	/* package */IndicatorControlContainer mIndicatorControlContainer;
	private PreferenceGroup mPreferenceGroup;

	// multiple cameras support
	private int mNumberOfCameras;
	private int mCameraId;
	private int mFrontCameraId;
	private int mBackCameraId;

	private boolean mQuickCapture;

	// Mediatek feature merge begin
	/* package */String mChangedkey;
	private ControlBarIndicatorButton mNormalCaptureIndicatorButton;
	/* package */TextView mRemainPictureView;

	private RotateImageView mStillCapturePicker;
	private ModeActor mModeActor;
	private RotateImageView mSingle3DSwitch;
	private RotateLayout mSingle3DControlBar;
	private RotateTextToast mSingle3DGuide;
	public View mSingle3DCancel;
    private String mStereo3DType;
    private int mPictureFormat = Storage.PICTURE_TYPE_JPG;
	private Rotatable mOnScreenProgress;

	// On-screen indicator
	protected ImageView mASDIndicator;
	protected ImageView mHDRIndicator;

	private boolean mKeyHalfPressed = false;
	private boolean mCameraKeyLongPressed = false;
	private boolean mRestoringPreference = false;
	// Constants of camera setting / setting values
	public static final String KEY_LAST_THUMB_URI = "last_thumb_uri";
	protected SelfTimerManager mSelftimerManager;

    private static boolean mIsAutoFocusCallback = false;

    protected static int sOrientationNow = 0;
	// Mediatek feature merge end

	/**
	 * This Handler is used to post message back onto the main thread of the
	 * application
	 */
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLEAR_SCREEN_DELAY: {
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			}

			case FIRST_TIME_INIT: {
				initializeFirstTime();
				break;
			}

			case SET_CAMERA_PARAMETERS_WHEN_IDLE: {
				setCameraParametersWhenIdle(0);
				break;
			}

			case CHECK_DISPLAY_ROTATION: {
				// Set the display orientation if display rotation has changed.
				// Sometimes this happens when the device is held upside
				// down and camera app is opened. Rotation animation will
				// take some time and the rotation value we have got may be
				// wrong. Framework does not have a callback for this now.
				if (Util.getDisplayRotation(Camera.this) != mDisplayRotation) {
					setDisplayOrientation();
				}
				if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
					mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION,
							100);
				}
				break;
			}

			case SHOW_TAP_TO_FOCUS_TOAST: {
				showTapToFocusToast();
				break;
			}

			case UPDATE_THUMBNAIL: {
				if (!mModeActor.isBurstShotInternal()
						&& mImageSaver != null) {
					mImageSaver.updateThumbnail();
					checkStorage();
				}
				break;
			}

			case UPDATE_STORAGE: {
				checkStorage();
				break;
			}

			case PICTURES_SAVING_DONE: {
				if (!mPausing) {
			        if (mCameraState == SAVING_PICTURES) {
						setCameraState(IDLE);
						checkStorage();
					}
	                hidePostSingle3DControlAlert();
                    // Set the thumbnail bitmap here because mThumbnailView must be accessed
                    // from the UI thread.
                    if (msg.obj != null) {
                        mThumbnailUpdated = true;
                        mThumbnail = (Thumbnail)msg.obj;
                    }
                    updateThumbnailButton();
				}
				break;
			}

			case MSG_GET_THUMBNAIL_DONE: {
				if (!mThumbnailUpdated) {
					mThumbnailUpdated = true;
					mThumbnail = (Thumbnail) msg.obj;
				}
				updateThumbnailButton();
				break;
			}

			case UPDATE_CAPTURE_INDICATOR: {
				// send from pickModeActor, modeActor should be ready now
				if (mNormalCaptureIndicatorButton != null) {
					mModeActor.updateCaptureModeButton(mNormalCaptureIndicatorButton, true);
				}
				break;
			}
			
			case SHOW_SINGLE3D_GUIDE: {
				showSingle3DGuide(R.string.single3d_guide_shutter);
			}
			}
		}
	}

	private void resetExposureCompensation() {
		String value = mPreferences.getString(CameraSettings.KEY_EXPOSURE,
				CameraSettings.EXPOSURE_DEFAULT_VALUE);
		if (!CameraSettings.EXPOSURE_DEFAULT_VALUE.equals(value)) {
			Editor editor = mPreferences.edit();
			editor.putString(CameraSettings.KEY_EXPOSURE, "0");
			editor.apply();
			if (mIndicatorControlContainer != null) {
				mIndicatorControlContainer.reloadPreferences();
			}
		}
	}

	private void keepMediaProviderInstance() {
		// We want to keep a reference to MediaProvider in camera's lifecycle.
		// TODO: Utilize mMediaProviderClient instance to replace
		// ContentResolver calls.
		if (mMediaProviderClient == null) {
			mMediaProviderClient = getContentResolver()
					.acquireContentProviderClient(MediaStore.AUTHORITY);
		}
	}

	// Snapshots can only be taken after this is called. It should be called
	// once only. We could have done these things in onCreate() but we want to
	// make preview screen appear as soon as possible.
	private void initializeFirstTime() {
		if (mFirstTimeInitialized)
			return;

		// Create orientation listenter. This should be done first because it
		// takes some time to get first orientation.
		mOrientationListener = new MyOrientationEventListener(Camera.this);
		mOrientationListener.enable();

		// Initialize location sevice.
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				getContentResolver());
		initOnScreenIndicator();
		mLocationManager.recordLocation(recordLocation);

		keepMediaProviderInstance();
		checkStorage();

		// Initialize last picture button.
		mContentResolver = getContentResolver();
		if (!mIsImageCaptureIntent) { // no thumbnail in image capture intent
			initThumbnailButton();
		}

		// Initialize shutter button.
		mShutterButton.setOnShutterButtonListener(mModeActor.getShutterButtonListener());
		mShutterButton.setVisibility(View.VISIBLE);

		// Initialize focus UI.
		mPreviewFrame = findViewById(R.id.camera_preview);
		mFaceView = (FaceView) findViewById(R.id.face_view);
		mPreviewFrame.setOnTouchListener(this);
		mFocusAreaIndicator = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
		CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
		boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
		mFocusManager.initialize(mFocusAreaIndicator, mPreviewFrame, mFaceView,
				this, mirror, mDisplayOrientation);
		if (mImageSaver == null) {
			mImageSaver = new ImageSaver();
		}
		Util.initializeScreenBrightness(getWindow(), getContentResolver());
		installIntentFilter();
		initializeZoom();
		updateOnScreenIndicators();
		startFaceDetection();
		// Show the tap to focus toast if this is the first start.
		if (mFocusAreaSupported
				&& mPreferences.getBoolean(
						CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, true)) {
			// Delay the toast for one second to wait for orientation.
			mHandler.sendEmptyMessageDelayed(SHOW_TAP_TO_FOCUS_TOAST, 1000);
		}

		mNameFormat = getResources().getString(R.string.pano_file_name_format);
		mFirstTimeInitialized = true;
        long switchEndTime = System.currentTimeMillis();
        Xlog.i(TAG, "[Performance test][Camera][Camera] camera switch Main To Sub end ["+ switchEndTime +"]");
        Xlog.i(TAG, "[Performance test][Camera][Camera] camera switch Sub To Main end ["+ switchEndTime +"]");
        Xlog.i(TAG, "[Performance test][Camera][VideoCamera] videocamera switch to camera end ["+ switchEndTime +"]");
        Xlog.i(TAG, "[Performance test][Camera][Camera] camera launch end ["+ switchEndTime +"]");
		addIdleHandler();
	}

	private static final MessageQueue.IdleHandler sProcessIdleHandler = new MessageQueue.IdleHandler() {
        public boolean queueIdle() {
            Storage.ensureOSXCompatible();
            return false;
        }
    };
    
	private void addIdleHandler() {
		MessageQueue queue = Looper.myQueue();
		queue.addIdleHandler(sProcessIdleHandler);
	}

	private void updateThumbnail() {
		final ContentResolver contentResolver = getContentResolver();
		if ((mThumbnail == null || !Util.isUriValid(mThumbnail.getUri(),
				contentResolver))) {
			if (mThumbnail != null) {// means that the thumbnail is not valid.
				mThumbnail = null;
				updateThumbnailButton();
			}

			mThumbnailUpdated = false;
			new Thread() {
				@Override
				public void run() {
					Log.d(TAG, "Thumbnail.getLastThumbnail >>>");
					Thumbnail tb = Thumbnail.getLastThumbnail(contentResolver);
					Log.d(TAG, "Thumbnail.getLastThumbnail <<< is null:"
							+ (tb == null));
					mHandler.sendMessage(mHandler.obtainMessage(
							MSG_GET_THUMBNAIL_DONE, 0, 0, tb));
				}
			}.start();
		}
	}

	private void updateThumbnailButton() {
		if (mThumbnail != null) {
			mThumbnailView.setBitmap(mThumbnail.getBitmap());
		} else {
			mThumbnailView.setBitmap(null);
		}
	}

	private void initThumbnailButton() {
		mThumbnailUpdated = false;
		// Load the thumbnail from the disk.
		new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "Thumbnail.loadFrom >>>");
				Thumbnail tb = Thumbnail.loadFrom(new File(getFilesDir(),
						Thumbnail.LAST_THUMB_FILENAME));
				Log.d(TAG, "Thumbnail.loadFrom <<< is null:" + (tb == null));

				ContentResolver contentResolver = getContentResolver();
				if ((tb == null || !Util.isUriValid(tb.getUri(),
						contentResolver))) {
					Log.d(TAG, "Thumbnail.getLastThumbnail >>>");
					tb = Thumbnail.getLastThumbnail(contentResolver);
					Log.d(TAG, "Thumbnail.getLastThumbnail <<< is null:"
							+ (tb == null));
				}
				mHandler.sendMessage(mHandler.obtainMessage(
						MSG_GET_THUMBNAIL_DONE, 0, 0, tb));
			}
		}.start();
		updateThumbnailButton();
	}

	// If the activity is paused and resumed, this method will be called in
	// onResume.
	private void initializeSecondTime() {
		// Start orientation listener as soon as possible because it takes
		// some time to get first orientation.
		mOrientationListener.enable();

		// Start location update if needed.
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				getContentResolver());
		mLocationManager.recordLocation(recordLocation);

		installIntentFilter();
		if (mImageSaver == null) {
			mImageSaver = new ImageSaver();
		}
		initializeZoom();
		keepMediaProviderInstance();
		checkStorage();
		hidePostCaptureAlert();

		if (!mIsImageCaptureIntent) {
			updateThumbnail();
			mModePicker.setCurrentMode(ModePicker.MODE_CAMERA);
		}
	}

	private class ZoomChangeListener implements
			ZoomControl.OnZoomChangedListener {
		// only for immediate zoom
		@Override
		public void onZoomValueChanged(int index) {
			Camera.this.onZoomValueChanged(index);
		}

		// only for smooth zoom
		@Override
		public void onZoomStateChanged(int state) {
			if (mPausing)
				return;

			Log.v(TAG, "zoom picker state=" + state);
			if (state == ZoomControl.ZOOM_IN) {
				Camera.this.onZoomValueChanged(mZoomMax);
			} else if (state == ZoomControl.ZOOM_OUT) {
				Camera.this.onZoomValueChanged(0);
			} else {
				mTargetZoomValue = -1;
				if (mZoomState == ZOOM_START) {
					mZoomState = ZOOM_STOPPING;
					mCameraDevice.stopSmoothZoom();
				}
			}
		}
	}

	private boolean initializeZoomMax(Parameters parameters) {
		if (!parameters.isZoomSupported())
			return false;
		mZoomMax = parameters.getMaxZoom();
		// Currently we use immediate zoom for fast zooming to get better UX and
		// there is no plan to take advantage of the smooth zoom.
		mZoomControl.setZoomMax(mZoomMax);		
		return true;		
	}	

	private void initializeZoom() {
		// Get the parameter to make sure we have the up-to-date zoom value.
		mParameters = mCameraDevice.getParameters();
		if (!initializeZoomMax(mParameters))
			return;
		mZoomControl.setZoomIndex(mParameters.getZoom());
		mZoomControl.setSmoothZoomSupported(mSmoothZoomSupported);
		mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
		mCameraDevice.setZoomChangeListener(mZoomListener);
	}

	private void onZoomValueChanged(int index) {
		// Not useful to change zoom value when the activity is paused.
		if (mPausing)
			return;

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
			setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);
		}
	}

	@Override
	public void startFaceDetection() {
		if (mFaceDetectionStarted
				|| mCameraState != IDLE	|| !mModeActor.enableFD(mCameraId)) {
			return;
		}
		if (mParameters.getMaxNumDetectedFaces() > 0) {
			mFaceDetectionStarted = true;
			if (mFaceView == null) {
				//startFaceDetection may happen before initializeFirstTime.
				mFaceView = (FaceView) findViewById(R.id.face_view);
			}
			mFaceView.clear();
			mFaceView.setVisibility(View.VISIBLE);
			mFaceView.setDisplayOrientation(mDisplayOrientation);
			CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
			mFaceView.setMirror(info.facing == CameraInfo.CAMERA_FACING_FRONT);
			mFaceView.resume();
			mCameraDevice.setFaceDetectionListener(this);
			mCameraDevice.startFaceDetection();
		}
	}

	@Override
	public void stopFaceDetection() {
		if (!mFaceDetectionStarted)
			return;
		if (mParameters.getMaxNumDetectedFaces() > 0) {
			mFaceDetectionStarted = false;
			mCameraDevice.setFaceDetectionListener(null);
			mCameraDevice.stopFaceDetection();
			if (mFaceView != null)
				mFaceView.clear();
		}
	}

	private class PopupGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			if (!mFirstTimeInitialized) return false;
			// Check if the popup window is visible.
			View popup = mIndicatorControlContainer.getActiveSettingPopup();
			if (popup == null) {
				View popupWindow = null;
				if (mNormalCaptureIndicatorButton != null) {
					popupWindow = mNormalCaptureIndicatorButton.getPopupWindow();
				}
				if (popupWindow == null) return false;
				if (!Util.pointInView(e.getX(), e.getY(), popupWindow)
						&& !Util.pointInView(e.getX(), e.getY(),
								mNormalCaptureIndicatorButton)
						&& !Util.pointInView(e.getX(), e.getY(), mPreviewFrame)) {
					mNormalCaptureIndicatorButton.dismissPopup();
					// Let event fall through.
				}
				return false;
			}

			// Let popup window, indicator control or preview frame handle the
			// event by themselves. Dismiss the popup window if users touch on
			// other areas.
			if (!Util.pointInView(e.getX(), e.getY(), popup)
					&& !Util.pointInView(e.getX(), e.getY(),
							mIndicatorControlContainer)
					&& !Util.pointInView(e.getX(), e.getY(), mPreviewFrame)) {
				mIndicatorControlContainer.dismissSettingPopup();
				// Let event fall through.
			}
			return false;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent m) {
		// Check if the popup window should be dismissed first.
		if (mPopupGestureDetector != null
				&& mPopupGestureDetector.onTouchEvent(m)) {
			return true;
		}

		return super.dispatchTouchEvent(m);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "Received intent action=" + action);
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_CHECKING)) {
				checkStorage();
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				checkStorage();
				if (!mIsImageCaptureIntent) {
					updateThumbnail();
				}
			}
		}
	};

	private void initOnScreenIndicator() {
		mGpsIndicator = (ImageView) findViewById(R.id.onscreen_gps_indicator);
		mExposureIndicator = (TextView) findViewById(R.id.onscreen_exposure_indicator);
		mFlashIndicator = (ImageView) findViewById(R.id.onscreen_flash_indicator);
		mSceneIndicator = (ImageView) findViewById(R.id.onscreen_scene_indicator);
		mWhiteBalanceIndicator = (ImageView) findViewById(R.id.onscreen_white_balance_indicator);
		mFocusIndicator = (ImageView) findViewById(R.id.onscreen_focus_indicator);

		// Mediatek migration begin
		mASDIndicator = (ImageView) findViewById(R.id.asd_indicator);
		mHDRIndicator = (ImageView) findViewById(R.id.hdr_indicator);
		// Mediatek migration end
	}

	@Override
	public void showGpsOnScreenIndicator(boolean hasSignal) {
		if (mGpsIndicator == null) {
			return;
		}
		if (hasSignal) {
			mGpsIndicator.setImageResource(R.drawable.ic_viewfinder_gps_on);
		} else {
			mGpsIndicator
					.setImageResource(R.drawable.ic_viewfinder_gps_no_signal);
		}
		mGpsIndicator.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideGpsOnScreenIndicator() {
		if (mGpsIndicator == null) {
			return;
		}
		mGpsIndicator.setVisibility(View.GONE);
	}

	private void updateExposureOnScreenIndicator(int value) {
		if (mExposureIndicator == null) {
			return;
		}
		if (value == 0) {
			mExposureIndicator.setText("");
			mExposureIndicator.setVisibility(View.GONE);
		} else {
			float step = mParameters.getExposureCompensationStep();
			mFormatterArgs[0] = value * step;
			mBuilder.delete(0, mBuilder.length());
			mFormatter.format("%+1.1f", mFormatterArgs);
			String exposure = mFormatter.toString();
			mExposureIndicator.setText(exposure);
			mExposureIndicator.setVisibility(View.VISIBLE);
		}
	}

	private void updateFlashOnScreenIndicator(String value) {
		if (mFlashIndicator == null) {
			return;
		}
		if (Parameters.FLASH_MODE_AUTO.equals(value)) {
			mFlashIndicator
					.setImageResource(R.drawable.ic_indicators_landscape_flash_auto);
			mFlashIndicator.setVisibility(View.VISIBLE);
		} else if (Parameters.FLASH_MODE_ON.equals(value)) {
			mFlashIndicator
					.setImageResource(R.drawable.ic_indicators_landscape_flash_on);
			mFlashIndicator.setVisibility(View.VISIBLE);
		} else if (Parameters.FLASH_MODE_OFF.equals(value)) {
			mFlashIndicator.setVisibility(View.GONE);
		}
	}

	private void updateSceneOnScreenIndicator(boolean isVisible) {
		if (mSceneIndicator == null) {
			return;
		}
		mSceneIndicator.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	private void updateWhiteBalanceOnScreenIndicator(String value) {
		if (mWhiteBalanceIndicator == null) {
			return;
		}
		if (Parameters.WHITE_BALANCE_AUTO.equals(value)) {
			mWhiteBalanceIndicator.setVisibility(View.GONE);
		} else {
			if (Parameters.WHITE_BALANCE_FLUORESCENT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_fluorescent);
			} else if (Parameters.WHITE_BALANCE_INCANDESCENT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_incandescent);
			} else if (Parameters.WHITE_BALANCE_DAYLIGHT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_sunlight);
			} else if (Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_cloudy);
			} else if (Parameters.WHITE_BALANCE_SHADE.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_shade);
			} else if (Parameters.WHITE_BALANCE_TWILIGHT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_twlight);
			} else if (Parameters.WHITE_BALANCE_TUNGSTEN.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_tungsten);
			} else if (Parameters.WHITE_BALANCE_WARM_FLUORESCENT.equals(value)) {
				mWhiteBalanceIndicator
						.setImageResource(R.drawable.ic_indicators_warmfluorescent);
			}
			mWhiteBalanceIndicator.setVisibility(View.VISIBLE);
		}
	}

	private void updateFocusOnScreenIndicator(String value) {
		if (mFocusIndicator == null) {
			return;
		}
		if (Parameters.FOCUS_MODE_INFINITY.equals(value)) {
			mFocusIndicator
					.setImageResource(R.drawable.ic_indicators_landscape);
			mFocusIndicator.setVisibility(View.VISIBLE);
		} else if (Parameters.FOCUS_MODE_MACRO.equals(value)) {
			mFocusIndicator.setImageResource(R.drawable.ic_indicators_macro);
			mFocusIndicator.setVisibility(View.VISIBLE);
		} else {
			mFocusIndicator.setVisibility(View.GONE);
		}
	}

	private void updateOnScreenIndicators() {
		boolean notAutoScene = !(Parameters.SCENE_MODE_AUTO.equals(mParameters
				.getSceneMode()));
		updateSceneOnScreenIndicator(notAutoScene);
		updateExposureOnScreenIndicator(
				Integer.valueOf(mParameters.getExposureCompensation()));
		updateFlashOnScreenIndicator(mParameters.getFlashMode());
		updateWhiteBalanceOnScreenIndicator(mParameters.getWhiteBalance());
		updateFocusOnScreenIndicator(mParameters.getFocusMode());
		updateCaptureModeIndicatorOnScreen();

		hideOnScreenIndicator();
	}

	private final class ShutterCallback implements
			android.hardware.Camera.ShutterCallback {
		public void onShutter() {
			mShutterCallbackTime = System.currentTimeMillis();
			mShutterLag = mShutterCallbackTime - mCaptureStartTime;
			Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
			mFocusManager.onShutter();
			mModeActor.onShutter();
		}
	}

	private final class PostViewPictureCallback implements PictureCallback {
		public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
			mPostViewPictureCallbackTime = System.currentTimeMillis();
			Log.v(TAG, "mShutterToPostViewCallbackTime = "
					+ (mPostViewPictureCallbackTime - mShutterCallbackTime)
					+ "ms");
		}
	}

	private final class RawPictureCallback implements PictureCallback {
		public void onPictureTaken(byte[] rawData,
				android.hardware.Camera camera) {
			mRawPictureCallbackTime = System.currentTimeMillis();
			Log.v(TAG, "mShutterToRawCallbackTime = "
					+ (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
		}
	}

	private final class JpegPictureCallback implements PictureCallback {
		Location mLocation;

		public JpegPictureCallback(Location loc) {
			mLocation = loc;
		}

		public void onPictureTaken(final byte[] jpegData,
				final android.hardware.Camera camera) {
			mKeyHalfPressed = false;
			if (mPausing) {
				return;
			}

			mJpegPictureCallbackTime = System.currentTimeMillis();
			// If postview callback has arrived, the captured image is displayed
			// in postview callback. If not, the captured image is displayed in
			// raw picture callback.
			if (mPostViewPictureCallbackTime != 0) {
				mShutterToPictureDisplayedTime = mPostViewPictureCallbackTime
						- mShutterCallbackTime;
				mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
						- mPostViewPictureCallbackTime;
			} else {
				mShutterToPictureDisplayedTime = mRawPictureCallbackTime
						- mShutterCallbackTime;
				mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
						- mRawPictureCallbackTime;
			}
			Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
					+ mPictureDisplayedToJpegCallbackTime + "ms");

			if (!mIsImageCaptureIntent) {
				if(Util.CMCC) {
					 long delay = 1200 - mPictureDisplayedToJpegCallbackTime;
					 if (delay <= 0) {
						 startPreview();
						 startFaceDetection();
					 } else {
						 mHandler.postDelayed(new Runnable() {
							 public void run() {
								 startPreview();
								 startFaceDetection();
								 checkStorage();
							 }
						 }, delay);
					 }
				} else {
					startPreview();
					startFaceDetection();
				}
			}
	        long switchEndTime = System.currentTimeMillis();
	        Xlog.i(TAG, "[Performance test][Camera][Camera] camera capture end ["+ switchEndTime +"]");

			if (!mIsImageCaptureIntent) {
				Size s = mParameters.getPictureSize();
				mImageSaver.addImage(jpegData, mLocation, s.width, s.height, Storage.generateStereoType(mStereo3DType));
				mModeActor.onPictureTaken(mLocation, s.width, s.height);
			} else {
				mJpegImageData = jpegData;
				if (!mQuickCapture) {
					showPostCaptureAlert();
				} else {
					doAttach();
				}
			}
			
			// Check this in advance of each shot so we don't add to shutter
			// latency. It's true that someone else could write to the SD card
			// in
			// the mean time and fill it, but that could have happened between
			// the
			// shutter press and saving the JPEG too.
			checkStorage();

			long now = System.currentTimeMillis();
			mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
			Log.v(TAG, "mJpegCallbackFinishTime = " + mJpegCallbackFinishTime
					+ "ms");
			mJpegPictureCallbackTime = 0;
		}
	}

	private final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, android.hardware.Camera camera) {
			if (mPausing)
				return;

			mAutoFocusTime = System.currentTimeMillis() - mFocusStartTime;
			Log.v(TAG, "mAutoFocusTime = " + mAutoFocusTime + "ms" + ", camera State = " + String.valueOf(mCameraState));
			if (mCameraState == FOCUSING) {
				setCameraState(IDLE);
			}
			mFocusManager.onAutoFocus(focused);
			mIsAutoFocusCallback = true;
		}
	}

	private final class ZoomListener implements
			android.hardware.Camera.OnZoomChangeListener {
		@Override
		public void onZoomChange(int value, boolean stopped,
				android.hardware.Camera camera) {
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

	// Each SaveRequest remembers the data needed to save an image.
	private static class SaveRequest {
		byte[] data;
		Location loc;
		int width, height;
		long dateTaken;
		int previewWidth;
		int stereo3DType;
	}

	// We use a queue to store the SaveRequests that have not been completed
	// yet. The main thread puts the request into the queue. The saver thread
	// gets it from the queue, does the work, and removes it from the queue.
	//
	// There are several cases the main thread needs to wait for the saver
	// thread to finish all the work in the queue:
	// (1) When the activity's onPause() is called, we need to finish all the
	// work, so other programs (like Gallery) can see all the images.
	// (2) When we need to show the SharePop, we need to finish all the work
	// too, because we want to show the thumbnail of the last image taken.
	//
	// If the queue becomes too long, adding a new request will block the main
	// thread until the queue length drops below the threshold (QUEUE_LIMIT).
	// If we don't do this, we may face several problems: (1) We may OOM
	// because we are holding all the jpeg data in memory. (2) We may ANR
	// when we need to wait for saver thread finishing all the work (in
	// onPause() or showSharePopup()) because the time to finishing a long queue
	// of work may be too long.
	class ImageSaver extends Thread {
		// Enlarge limit of queue size to reduce the occurrence of ANR
		private static final int QUEUE_LIMIT = 6;
		private static final int QUEUE_BUSY = 3;
		private static final long HEAVY_WRITE_LINE = 30000000;
		private static final long BUFF_FULL = 45000000;

		private ArrayList<SaveRequest> mQueue;
		private Thumbnail mPendingThumbnail;
		private Object mUpdateThumbnailLock = new Object();
		private boolean mStop;
		// M:enlarge queue limit for continuous shot feature
		private int mQueueLimit = QUEUE_LIMIT;

		// Runs in main thread
		public ImageSaver() {
			mQueue = new ArrayList<SaveRequest>();
			start();
		}

		// M:
		public boolean isFull() {
			long totalToWrite = 0;
			synchronized (this) {
				for (SaveRequest r : mQueue) {
					if (r.data != null) {
						totalToWrite += r.data.length;
					}
				}
			}
			if (totalToWrite > BUFF_FULL) {
				return true;
			}
			return !checkStorage(totalToWrite);
		}

		public boolean isHeavyWriting() {
			long totalToWrite = 0;
			synchronized (this) {
				for (SaveRequest r : mQueue) {
					if (r.data != null) {
						totalToWrite += r.data.length;
					}
				}
			}
			if (totalToWrite > HEAVY_WRITE_LINE) {
				return true;
			}
			return false;
		}

		public synchronized void enlargeQueueLimit() {
			mQueueLimit = 99;
		}

		public synchronized void resetQueueLimit() {
			mQueueLimit = QUEUE_LIMIT;
		}
		// Runs in main thread
		public void addImage(final byte[] data, Location loc, int width,
				int height, int stereo3DType) {
			SaveRequest r = new SaveRequest();
			r.data = data;
			r.loc = (loc == null) ? null : new Location(loc); // make a copy
			r.width = width;
			r.height = height;
			r.dateTaken = System.currentTimeMillis();
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				r.previewWidth = mPreviewFrameLayout.getHeight();
			} else {
				r.previewWidth = mPreviewFrameLayout.getWidth();
			}
			r.stereo3DType = stereo3DType;
			synchronized (this) {
				Log.i(TAG, "jmac, add thread, add to queue,current size = " + mQueue.size());
				while (mQueue.size() >= mQueueLimit) {
					try {
						Log.i(TAG, "jmac, add thread, queue full, wait, current size = "+ mQueue.size());
						wait();
						Log.i(TAG, "jmac, add thread, wait done, current size = " + mQueue.size());
					} catch (InterruptedException ex) {
						// ignore.
					}
				}
				mQueue.add(r);
				notifyAll(); // Tell saver thread there is new work to do.
			}
		}

		// Runs in saver thread
		@Override
		public void run() {
			while (true) {
				SaveRequest r;
				synchronized (this) {
					if (mQueue.isEmpty()) {
						notifyAll(); // notify main thread in waitDone

						// Note that we can only stop after we saved all images
						// in the queue.
						if (mStop)
							break;

						try {
							wait();
						} catch (InterruptedException ex) {
							// ignore.
						}
						continue;
					}
					r = mQueue.get(0);
				}
				Log.i(TAG, "jmac, dequeue thread, before save file, current size = " + mQueue.size());
				storeImage(r.data, r.loc, r.width, r.height, r.dateTaken,
						r.previewWidth, r.stereo3DType);
				synchronized (this) {
					mQueue.remove(0);
					notifyAll(); // the main thread may wait in addImage
					Log.i(TAG, "jmac, dequeue thread, save file done, current size = " + mQueue.size());
				}
			}
		}

		// Runs in main thread
		public void waitDone() {
			synchronized (this) {
				while (!mQueue.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException ex) {
						// ignore.
					}
				}
			}
			updateThumbnail();
		}

		public void waitDoneInSubThread() {
			synchronized (this) {
				while (!mQueue.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException ex) {
						// ignore.
					}
				}
			}
		}

		// Runs in main thread
		public void finish() {
			waitDone();
			synchronized (this) {
				mStop = true;
				notifyAll();
			}
			try {
				join();
			} catch (InterruptedException ex) {
				// ignore.
			}
		}

		public void saveDoneFinish() {
			synchronized (this) {
				mStop = true;
				notifyAll();
			}
		}

		public boolean isBusy() {
			return mQueue.size() > QUEUE_BUSY;
		}
		// Runs in main thread (because we need to update mThumbnailView in the
		// main thread)
		public void updateThumbnail() {
			Thumbnail t;
			synchronized (mUpdateThumbnailLock) {
				mHandler.removeMessages(UPDATE_THUMBNAIL);
				t = mPendingThumbnail;
				mPendingThumbnail = null;
			}

			if (t != null) {
				mThumbnail = t;
				mThumbnailUpdated = true;
				mThumbnailView.setBitmap(mThumbnail.getBitmap());
			}
			// Share popup may still have the reference to the old thumbnail.
			// Clear it.
			mSharePopup = null;
		}

		// Runs in saver thread
		private void storeImage(final byte[] data, Location loc, int width,
				int height, long dateTaken, int previewWidth, int stereo3DType) {
			String title = Util.createJpegName(dateTaken);
			int orientation = Exif.getOrientation(data);
			Uri uri = Storage.addImage(mContentResolver, title, dateTaken, loc,
					orientation, data, width, height, mPictureFormat, stereo3DType);
			if (uri != null) {
				boolean needThumbnail;
				synchronized (this) {
					// If the number of requests in the queue (include the
					// current one) is greater than 1, we don't need to generate
					// thumbnail for this image. Because we'll soon replace it
					// with the thumbnail for some image later in the queue.
					//needThumbnail = (mQueue.size() <= mQueueLimit / QUEUE_LIMIT);
					needThumbnail = mModeActor.updateThumbnailInSaver(
							mQueue.size(), mQueueLimit / QUEUE_LIMIT);
				}
				if (needThumbnail) {
					// Create a thumbnail whose width is equal or bigger than
					// that of the preview.
					int ratio = (int) Math.ceil((double) width / previewWidth);
					int inSampleSize = Integer.highestOneBit(ratio);
					Thumbnail t = Thumbnail.createThumbnail(data, orientation,
							inSampleSize, uri, stereo3DType);
					synchronized (mUpdateThumbnailLock) {
						// We need to update the thumbnail in the main thread,
						// so send a message to run updateThumbnail().
						mPendingThumbnail = t;
						mHandler.sendEmptyMessage(UPDATE_THUMBNAIL);
					}
				}
				Util.broadcastNewPicture(Camera.this, uri);
			}
		}
	}

	/* package */void setCameraState(int state) {
		mCameraState = state;
		switch (state) {
		case SNAPSHOT_IN_PROGRESS:
		case FOCUSING:
		case SELFTIMER_COUNTING:
		case SAVING_PICTURES:
			enableCameraControls(false);
			break;
		case IDLE:
		case PREVIEW_STOPPED:
			enableCameraControls(true);
			break;
		}
	}

	@Override
	public boolean capture() {
		// If we are already in the middle of taking a snapshot then ignore.
		if (mCameraState == SNAPSHOT_IN_PROGRESS || mCameraDevice == null) {
			return false;
		}
		Log.i(TAG, "capture begin");
		mCaptureStartTime = System.currentTimeMillis();
		mPostViewPictureCallbackTime = 0;
		mJpegImageData = null;

		// Mediatek merge begin
		mModeActor.ensureCaptureTempPath();
		// Mediatek merge end

		// Set rotation and gps data.
		Util.setRotationParameter(mParameters, mCameraId, mOrientation);
		Location loc = mLocationManager.getCurrentLocation();
		Util.setGpsParameters(mParameters, loc);
		mCameraDevice.setParameters(mParameters);

		// Mediatek merge begin
		/*
		 * mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
		 * mPostViewPictureCallback, new JpegPictureCallback(loc));
		 */

		if (!mModeActor.applySpecialCapture()) {
			PictureCallback jpegPicturecallback = mModeActor
					.getPictureCallback(loc);
			if (jpegPicturecallback == null) {
				jpegPicturecallback = new JpegPictureCallback(loc);
			}
			mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
					mPostViewPictureCallback, jpegPicturecallback);
		}
		// Mediatek merge end

		mFaceDetectionStarted = false;
		setCameraState(SNAPSHOT_IN_PROGRESS);
		return true;
	}

	@Override
	public void setFocusParameters() {
		setCameraParameters(UPDATE_PARAM_PREFERENCE);
	}

	@Override
	public void playSound(int soundId) {
		mCameraSound.playSound(soundId);
	}

	private boolean saveDataToFile(String filePath, byte[] data) {
		FileOutputStream f = null;
		try {
			f = new FileOutputStream(filePath);
			f.write(data);
		} catch (IOException e) {
			return false;
		} finally {
			Util.closeSilently(f);
		}
		return true;
	}

	private void getPreferredCameraId() {
		mPreferences = new ComboPreferences(this);
		CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
		mCameraId = CameraSettings.readPreferredCameraId(mPreferences);

		// Testing purpose. Launch a specific camera through the intent extras.
		int intentCameraId = Util.getCameraFacingIntentExtras(this);
		if (intentCameraId != -1) {
			mCameraId = intentCameraId;
		}
	}

	Thread mCameraOpenThread = new Thread(new Runnable() {
		public void run() {
			try {
				mCameraDevice = Util.openCamera(Camera.this, mCameraId);
			} catch (CameraHardwareException e) {
				mOpenCameraFail = true;
			} catch (CameraDisabledException e) {
				mCameraDisabled = true;
			}
		}
	});

	Thread mCameraPreviewThread = new Thread(new Runnable() {
		public void run() {
			initializeCapabilities();
			startPreview();
		}
	});

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (LOGI) Log.i(TAG, "@@@ onCreate Bundle = " + String.valueOf(icicle));
		getPreferredCameraId();
		String[] defaultFocusModes = getResources().getStringArray(
				R.array.pref_camera_focusmode_default_array);
		mFocusManager = new FocusManager(mPreferences, defaultFocusModes);

		/*
		 * To reduce startup time, we start the camera open and preview threads.
		 * We make sure the preview is started at the end of onCreate.
		 */
		mCameraOpenThread.start();

		mIsImageCaptureIntent = isImageCaptureIntent();
		setContentView(R.layout.camera);
		if (mIsImageCaptureIntent) {
			mReviewDoneButton = (Rotatable) findViewById(R.id.btn_done);
			mReviewCancelButton = (Rotatable) findViewById(R.id.btn_cancel);
			findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
		} else {
			mThumbnailView = (RotateImageView) findViewById(R.id.thumbnail);
			mThumbnailView.enableFilter(false);
			mThumbnailView.setVisibility(View.VISIBLE);
		}

		mRotateDialog = new RotateDialogController(this, R.layout.rotate_dialog);

		mPreferences.setLocalId(this, mCameraId);
		CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
		// Mediatek merge begin
		resetCaptureMode();
		// Mediatek merge end

		mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();
		mQuickCapture = getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);

		// we need to reset exposure for the preview
		resetExposureCompensation();

		Util.enterLightsOutMode(getWindow());

		// don't set mSurfaceHolder here. We have it set ONLY within
		// surfaceChanged / surfaceDestroyed, other parts of the code
		// assume that when it is set, the surface is also set.
		SurfaceView preview = (SurfaceView) findViewById(R.id.camera_preview);
		SurfaceHolder holder = preview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Make sure camera device is opened.
		try {
			mCameraOpenThread.join();
			mCameraOpenThread = null;
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
		mCameraPreviewThread.start();

		if (mIsImageCaptureIntent) {
			setupCaptureParams();
		} else {
			mModePicker = (ModePicker) findViewById(R.id.mode_picker);
			mModePicker.setVisibility(View.VISIBLE);
			mModePicker.setOnModeChangeListener(this);
			mModePicker.setCurrentMode(ModePicker.MODE_CAMERA);
		}

        mSingle3DSwitch = (RotateImageView) findViewById(R.id.single3D_switch);
        mSingle3DControlBar = (RotateLayout) findViewById(R.id.single3d_control_bar);
        mSingle3DCancel = findViewById(R.id.btnSingle3DCancel);
        mOnScreenProgress = (RotateLayout) findViewById(R.id.on_screen_progress);
		mZoomControl = (ZoomControl) findViewById(R.id.zoom_control);
		mOnScreenIndicators = (Rotatable) findViewById(R.id.on_screen_indicators);
		mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
		mLocationManager = new LocationManager(this, this);

		mBackCameraId = CameraHolder.instance().getBackCameraId();
		mFrontCameraId = CameraHolder.instance().getFrontCameraId();

		// Wait until the camera settings are retrieved.
		synchronized (mCameraPreviewThread) {
			try {
                if (mCameraPreviewThread.getState() != Thread.State.TERMINATED) {
					mCameraPreviewThread.wait();
                }
			} catch (InterruptedException ex) {
				// ignore
			}
		}

		// Do this after starting preview because it depends on camera
		// parameters.
		initializeIndicatorControl();
		if (!mIsImageCaptureIntent) mModePicker.setModeSupport();
		// 3Dadd start
        initializeSingle3DControl();
		// 3Dadd end
		// Mediatek: Advance the initialization of zoom max value to make ZoomControlBar 
		// to be showed quickly.See ZoomControlBar.onLayout.Also depends on camera
		// parameters so put it here.
		initializeZoomMax(mInitialParams);
		mCameraSound = new CameraSound();

		// Make sure preview is started.
		try {
			mCameraPreviewThread.join();
		} catch (InterruptedException ex) {
			// ignore
		}
		mCameraPreviewThread = null;
	}

	private void overrideCameraSettings(
			final String whiteBalance, final String focusMode,
			final String evValue) {
		Camera.this.runOnUiThread(new Runnable() {
			public void run() {
				if (mIndicatorControlContainer != null) {
					mIndicatorControlContainer.overrideSettings(
							// Move parameter flash mode to scene independent
							// CameraSettings.KEY_FLASH_MODE, flashMode,
							CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
							CameraSettings.KEY_FOCUS_MODE, focusMode,
							CameraSettings.KEY_EXPOSURE, evValue);
				}
			}
		});
	}

	private void updateSceneModeUI() {
		if (mPausing) return;
		// If scene mode is set, we cannot set flash mode, white balance, and
		// focus mode, instead, we read it from driver
		if (!Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
			overrideCameraSettings(
					mParameters.getWhiteBalance(), mParameters.getFocusMode(),
					mParameters.getExposure());

			// Mediatek migration begin
			mModeActor.updateSceneModeUI(false);
			// Mediatek migration end
		} else {
			overrideCameraSettings(null, null, null);

			// Mediatek migration begin
			mModeActor.updateSceneModeUI(true);
			// Mediatek migration end
		}
	}

	private void loadCameraPreferences() {
		CameraSettings settings = new CameraSettings(this, mInitialParams,
				mCameraId, CameraHolder.instance().getCameraInfo());
		mPreferenceGroup = filterPreferenceScreenByIntent(
                settings.getPreferenceGroup(R.xml.camera_preferences));
        if (!isSupported(CameraSettings.KEY_CONTINUOUS_NUMBER, mInitialParams.getSupportedCaptureMode())) {
            CameraSettings.removePreferenceFromScreen(mPreferenceGroup,
                    CameraSettings.KEY_CONTINUOUS_NUMBER);
        }
	}

	private void initializeIndicatorControl() {
        // setting the indicator buttons.
        mIndicatorControlContainer =
                (IndicatorControlContainer) findViewById(R.id.indicator_control);
        if (mIndicatorControlContainer == null) return;
        loadCameraPreferences();

        ArrayList<String> SETTING_KEYS_ARRAY = new ArrayList<String>();
        ArrayList<String> OTHER_SETTING_KEYS_ARRAY = new ArrayList<String>();
        SETTING_KEYS_ARRAY.add(CameraSettings.KEY_HDR_CAPTURE_KEY);
        SETTING_KEYS_ARRAY.add(CameraSettings.KEY_FLASH_MODE);
        SETTING_KEYS_ARRAY.add(CameraSettings.KEY_WHITE_BALANCE);
        SETTING_KEYS_ARRAY.add(CameraSettings.KEY_COLOR_EFFECT);
        SETTING_KEYS_ARRAY.add(CameraSettings.KEY_SCENE_MODE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CAMERA_ZSD);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_RECORD_LOCATION);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_FOCUS_MODE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EXPOSURE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CONTINUOUS_NUMBER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_SELF_TIMER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EDGE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_HUE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_SATURATION);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_BRIGHTNESS);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CONTRAST);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_PICTURE_SIZE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_ISO);
                //default for touch focus(move spot)
                //CameraSettings.KEY_FOCUS_METER,
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EXPOSURE_METER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_ANTI_BANDING);
        
        final String[] SETTING_KEYS = SETTING_KEYS_ARRAY.toArray(new String[]{});
        final String[] OTHER_SETTING_KEYS = OTHER_SETTING_KEYS_ARRAY.toArray(new String[]{});

        mIndicatorControlContainer.initialize(this, mPreferenceGroup,
                mParameters.isZoomSupported(),
                SETTING_KEYS, OTHER_SETTING_KEYS);
        updateSceneModeUI();
        mIndicatorControlContainer.setListener(this);
        mPopupGestureDetector = new GestureDetector(this,
                new PopupGestureListener());
        
        initializeControlBarIndicator();
    }

	private void reInitializeIndicatorControl() {
        if (mIndicatorControlContainer == null) return;
        loadCameraPreferences();

        ArrayList<String> OTHER_SETTING_KEYS_ARRAY = new ArrayList<String>();
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CAMERA_ZSD);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_RECORD_LOCATION);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_FOCUS_MODE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CONTINUOUS_NUMBER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EXPOSURE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_SELF_TIMER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EDGE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_HUE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_SATURATION);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_BRIGHTNESS);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_CONTRAST);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_PICTURE_SIZE);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_ISO);
                //default for touch focus(move spot)
                //CameraSettings.KEY_FOCUS_METER,
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_EXPOSURE_METER);
        OTHER_SETTING_KEYS_ARRAY.add(CameraSettings.KEY_ANTI_BANDING);

        final String[] OTHER_SETTING_KEYS = OTHER_SETTING_KEYS_ARRAY.toArray(new String[]{});

        mIndicatorControlContainer.reInitializeOtherSetting(mPreferenceGroup,
                OTHER_SETTING_KEYS);
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
		if ((mNormalCaptureIndicatorButton != null)
				&& mNormalCaptureIndicatorButton.dismissPopup()) {
			return true;
		}
		return false;
	}

	private void enableCameraControls(boolean enable) {
		if (mIndicatorControlContainer != null) {
			mIndicatorControlContainer.setEnabled(enable);
			if( mCameraState != SELFTIMER_COUNTING){
                updateSceneModeUI();
            }
		}
		if (mNormalCaptureIndicatorButton != null) {
			mModeActor.updateCaptureModeButton(mNormalCaptureIndicatorButton, enable);
		}
		if (mModePicker != null)
			mModePicker.setEnabled(enable);
		if (mZoomControl != null) {
			mModeActor.updateZoomControl(mZoomControl, enable);
		}
		if (mThumbnailView != null)
			mThumbnailView.setEnabled(enable);
		if (mShutterButton != null) {
			mShutterButton.setEnabled(enable);
		}
        if (mSingle3DSwitch != null) {
            mSingle3DSwitch.setEnabled(enable);
        }
	}

	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			// We keep the last known orientation. So if the user first orient
			// the camera then point the camera to floor or sky, we still have
			// the correct orientation.
			if (orientation == ORIENTATION_UNKNOWN)
				return;
			mOrientation = Util.roundOrientation(orientation, mOrientation);
			// When the screen is unlocked, display rotation may change. Always
			// calculate the up-to-date orientationCompensation.
			int orientationCompensation = mOrientation
					+ Util.getDisplayRotation(Camera.this);
            sOrientationNow = mOrientation;
			if (mOrientationCompensation != orientationCompensation) {
				mOrientationCompensation = orientationCompensation;
				setOrientationIndicator(mOrientationCompensation);
			}

			// Show the toast after getting the first orientation changed.
			if (mHandler.hasMessages(SHOW_TAP_TO_FOCUS_TOAST)) {
				mHandler.removeMessages(SHOW_TAP_TO_FOCUS_TOAST);
				showTapToFocusToast();
			}
		}
	}

	private void setOrientationIndicator(int orientation) {
		Rotatable[] indicators = { mThumbnailView, mModePicker, mSharePopup,
				mZoomControl, mFocusAreaIndicator,
				mFaceView, mReviewCancelButton, mReviewDoneButton,
				mRotateDialog, mNormalCaptureIndicatorButton};
		for (Rotatable indicator : indicators) {
			if (indicator != null)
				indicator.setOrientation(orientation);
		}
		if (mStorageHint != null) mStorageHint.setOrientation(orientation);
		if (mModeActor != null) mModeActor.setOrientation(orientation);

		// the next indicators should not be rotated in 3d mode
        if (Util.getS3DMode() && orientation != 270) return;
		if (mOnScreenIndicators != null)
		    mOnScreenIndicators.setOrientation(orientation);
        if (mIndicatorControlContainer != null)
            mIndicatorControlContainer.setOrientation(orientation);
        if (mOnScreenProgress != null)
            mOnScreenProgress.setOrientation(orientation);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMediaProviderClient != null) {
			mMediaProviderClient.release();
			mMediaProviderClient = null;
		}
	}

	/* package */void checkStorage() {
		mPicturesRemaining = Storage.getAvailableSpace();
		if (mPicturesRemaining > Storage.LOW_STORAGE_THRESHOLD) {
			long pictureSize = mModeActor == null ? Storage.PICTURE_SIZE
					: mModeActor.pictureSize();
			mPicturesRemaining = (mPicturesRemaining - Storage.LOW_STORAGE_THRESHOLD)
					/ pictureSize;
		} else if (mPicturesRemaining > 0) {
			mPicturesRemaining = 0;
		}
		if (mPicturesRemaining < 1) {
			mModeActor.lowStorage();
		}
		if (mCameraState == IDLE) {
			if (mPicturesRemaining < 0) {
				mRemainPictureView.setText("0");
			} else {
				mRemainPictureView.setText(String.valueOf(mPicturesRemaining));
			}
		}
		updateStorageHint();
	}

	/* package */boolean checkStorage(long waitingForWrite) {
		long pictureRemaining = Storage.getAvailableSpace() - waitingForWrite;
		if (pictureRemaining > Storage.LOW_STORAGE_THRESHOLD) {
			long pictureSize = mModeActor == null ? Storage.PICTURE_SIZE
					: mModeActor.pictureSize();
			pictureRemaining = (pictureRemaining - Storage.LOW_STORAGE_THRESHOLD)
					/ pictureSize;
		} else if (pictureRemaining > 0) {
			pictureRemaining = 0;
		}
		return pictureRemaining >= 1;
	}

	@OnClickAttr
	public void onThumbnailClicked(View v) {
		if (isCameraIdle() && mThumbnail != null) {
	        if (Util.getS3DMode()) {
	           Util.viewUri(mThumbnail.getUri(), this);
	        } else {
	           showSharePopup();
	        }
		}
	}

	@OnClickAttr
	public void onReviewRetakeClicked(View v) {
		hidePostCaptureAlert();
		startPreview();
		startFaceDetection();
	}

	@OnClickAttr
	public void onReviewDoneClicked(View v) {
		doAttach();
	}

	@OnClickAttr
	public void onReviewCancelClicked(View v) {
		doCancel();
	}

	private void doAttach() {
		if (mPausing) {
			return;
		}

		byte[] data = mJpegImageData;

		if (mCropValue == null) {
			// First handle the no crop case -- just return the value. If the
			// caller specifies a "save uri" then write the data to it's
			// stream. Otherwise, pass back a scaled down version of the bitmap
			// directly in the extras.
			if (mSaveUri != null) {
				OutputStream outputStream = null;
				try {
					outputStream = mContentResolver.openOutputStream(mSaveUri);
					outputStream.write(data);
					outputStream.close();

					setResultEx(RESULT_OK);
					finish();
				} catch (IOException ex) {
					// ignore exception
				} finally {
					Util.closeSilently(outputStream);
				}
			} else {
				Intent intent = getIntent();
				int orientation = Exif.getOrientation(data);
            	if (intent.getBooleanExtra("OP01", false)) {
            		Size s = mParameters.getPictureSize();
            		long dateTaken = System.currentTimeMillis();
            		String title = Util.createJpegName(dateTaken);
    				Uri uri = Storage.addImage(getContentResolver(), title,
    						dateTaken, mLocationManager.getCurrentLocation(),
    						orientation, data, s.width, s.height, Storage.PICTURE_TYPE_JPG,
    						MediaStore.Images.Media.STEREO_TYPE_2D);
            		intent.setData(uri);
            		setResult(RESULT_OK, intent);
            		finish();
            	} else {
					Bitmap bitmap = Util.makeBitmap(data, 50 * 1024);
					bitmap = Util.rotate(bitmap, orientation);
					setResultEx(RESULT_OK,
							new Intent("inline-data").putExtra("data", bitmap));
					finish();
            	}
			}
		} else {
			// Save the image to a temp file and invoke the cropper
			Uri tempUri = null;
			FileOutputStream tempStream = null;
			try {
				File path = getFileStreamPath(sTempCropFilename);
				path.delete();
				tempStream = openFileOutput(sTempCropFilename, 0);
				tempStream.write(data);
				tempStream.close();
				tempUri = Uri.fromFile(path);
			} catch (FileNotFoundException ex) {
				setResultEx(Activity.RESULT_CANCELED);
				finish();
				return;
			} catch (IOException ex) {
				setResultEx(Activity.RESULT_CANCELED);
				finish();
				return;
			} finally {
				Util.closeSilently(tempStream);
			}

			Bundle newExtras = new Bundle();
			if (mCropValue.equals("circle")) {
				newExtras.putString("circleCrop", "true");
			}
			if (mSaveUri != null) {
				newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, mSaveUri);
			} else {
				newExtras.putBoolean("return-data", true);
			}

			Intent cropIntent = new Intent("com.android.camera.action.CROP");

			cropIntent.setData(tempUri);
			cropIntent.putExtras(newExtras);

			startActivityForResult(cropIntent, CROP_MSG);
		}
	}

	private void doCancel() {
		setResultEx(RESULT_CANCELED, new Intent());
		finish();
	}

	@Override
	public void onShutterButtonFocus(boolean pressed) {
		if (mPausing || collapseCameraControls() || mModeActor.skipFocus()
				|| mCameraState == SNAPSHOT_IN_PROGRESS
				|| mModeActor.isSelfTimerEnabled())
			return;

		// Do not do focus if there is not enough storage.
		if (pressed && !canTakePicture())
			return;

		if (LOGI) Log.i(TAG, "onShutterButtonFocus pressed = " + String.valueOf(pressed));
		if (pressed) {
			mFocusManager.onShutterDown();
		} else {
			mFocusManager.onShutterUp();
		}
	}

	@Override
	public void onShutterButtonClick() {
		if (mPausing || collapseCameraControls())
			return;

		// Do not take the picture if there is not enough storage.
		if (!mModeActor.canShot()) {
			Log.i(TAG, "Not enough space or storage not ready. remaining="
					+ mPicturesRemaining);
			return;
		}

		Log.v(TAG, "onShutterButtonClick: mCameraState=" + mCameraState);

		// If the user wants to do a snapshot while the previous one is still
		// in progress, remember the fact and do it after we finish the previous
		// one and re-start the preview. Snapshot in progress also includes the
		// state that autofocus is focusing and a picture will be taken when
		// focus callback arrives.
		if (mFocusManager.isFocusingSnapOnFinish()
				|| mCameraState == SNAPSHOT_IN_PROGRESS
				|| mCameraState == SAVING_PICTURES) {
			if (!mIsImageCaptureIntent) {
				mSnapshotOnIdle = true;
			}
			return;
		}

		if (mImageSaver.isBusy()) {
			new RotateTextToast(this, R.string.camera_saving_busy, mOrientation, 
					(ViewGroup) findViewById(R.id.frame)).show();
			return;
		}

		mSnapshotOnIdle = false;
		if (mModeActor.checkSelfTimerMode()) {
			setCameraState(SELFTIMER_COUNTING);
			return;
		}
		mFocusManager.doSnap();
	}

	private OnScreenHint mStorageHint;

	private void updateStorageHint() {
		String noStorageText = null;

		if (mPicturesRemaining == Storage.UNAVAILABLE) {
			noStorageText = getString(R.string.no_storage);
		} else if (mPicturesRemaining == Storage.PREPARING) {
			noStorageText = getString(R.string.preparing_sd);
		} else if (mPicturesRemaining == Storage.UNKNOWN_SIZE) {
			noStorageText = getString(R.string.access_sd_fail);
		} else if (!mModeActor.canShot()) {
			noStorageText = getString(Util.getNotEnoughSpaceAlertMessageId());
		}

		if (noStorageText != null) {
			if (mStorageHint == null) {
				mStorageHint = OnScreenHint.makeText(this, noStorageText);
			} else {
				mStorageHint.setText(noStorageText);
			}
			mStorageHint.show();
			mStorageHint.setOrientation(mOrientationCompensation);
		} else if (mStorageHint != null) {
			mStorageHint.cancel();
			mStorageHint = null;
		}
	}

	private void installIntentFilter() {
		// install an intent filter to receive SD card related events.
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		intentFilter.addDataScheme("file");
		registerReceiver(mReceiver, intentFilter);
		mDidRegister = true;
	}

	@Override
	protected void doOnResume() {
		if (mOpenCameraFail || mCameraDisabled)
			return;

		mPausing = false;
		int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
		if (mCameraId != cameraId) {
			CameraSettings.writePreferredCameraId(mPreferences, mCameraId);
		}
		mJpegPictureCallbackTime = 0;
		mZoomValue = 0;

		mModeActor.onResume();

		Log.i(TAG, "Camera State = " + String.valueOf(mCameraState));
		// Start the preview if it is not started.
		if (mCameraState == PREVIEW_STOPPED) {
			try {
				mCameraDevice = Util.openCamera(this, mCameraId);
				initializeCapabilities();
				resetExposureCompensation();
				startPreview();
				startFaceDetection();
				//Mediatek migration begin
				syncUIWithPreference();
				//Mediatek migration end
			} catch (CameraHardwareException e) {
				Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
				return;
			} catch (CameraDisabledException e) {
				Util.showErrorAndFinish(this, R.string.camera_disabled);
				return;
			}
		}

		if (mSurfaceHolder != null) {
			// If first time initialization is not finished, put it in the
			// message queue.
			if (!mFirstTimeInitialized) {
				mHandler.sendEmptyMessage(FIRST_TIME_INIT);
			} else {
				initializeSecondTime();
			}
		}
		keepScreenOnAwhile();

		if (mCameraState == IDLE) {
			mOnResumeTime = SystemClock.uptimeMillis();
			mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
		}
		// Dismiss open menu if exists.
		PopupManager.getInstance(this).notifyShowPopup(null);

		Log.i(TAG, "doOnresume end");
	}

	@Override
	protected void onPause() {
		mPausing = true;
		mSnapshotOnIdle = false;

		// Mediatek merge begin
		if (mModeActor != null) {
			mModeActor.onPausePre();
		}
		// Mediatek merge end

		stopPreview();
		// Close the camera now because other activities may need to use it.
		closeCamera();
		if (mCameraSound != null)
			mCameraSound.release();
		resetScreenOn();

		// Clear UI.
		collapseCameraControls();
		if (mSharePopup != null)
			mSharePopup.dismiss();
		if (mFaceView != null)
			mFaceView.clear();
		hidePostSingle3DControlAlert();

		if (mFirstTimeInitialized) {
			mOrientationListener.disable();
			if (!mIsImageCaptureIntent && mThumbnail != null
					&& !mThumbnail.fromFile()) {
				mThumbnail.saveTo(new File(getFilesDir(),
						Thumbnail.LAST_THUMB_FILENAME));
			}
		}

		if (mDidRegister) {
			unregisterReceiver(mReceiver);
			mDidRegister = false;
		}
		if (mLocationManager != null)
			mLocationManager.recordLocation(false);
		updateExposureOnScreenIndicator(0);

		if (mStorageHint != null) {
			mStorageHint.cancel();
			mStorageHint = null;
		}

		// If we are in an image capture intent and has taken
		// a picture, we just clear it in onPause.
		mJpegImageData = null;

		// Remove the messages in the event queue.
		mHandler.removeMessages(FIRST_TIME_INIT);
		mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
		mFocusManager.removeMessages();

		// Mediatek merge begin
		if (mModeActor != null) {
			mModeActor.onPause();
		}
        mIsAutoFocusCallback = false;
		// Mediatek merge end

		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CROP_MSG: {
				Intent intent = new Intent();
				if (data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						intent.putExtras(extras);
					}
				}
				setResultEx(resultCode, intent);
				finish();
	
				File path = getFileStreamPath(sTempCropFilename);
				path.delete();
	
				break;
			}
			default: {
				mModeActor.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	protected boolean canTakePicture() {
		boolean retVal = isCameraIdle();
		if (mModeActor == null) {
			retVal = retVal && (mPicturesRemaining > 0);
		} else {
			retVal = retVal && mModeActor.canShot();
		}
		return retVal;
	}

	@Override
	public void autoFocus() {
		if (LOGI) Log.i(TAG, "autoFocus");
		mFocusStartTime = System.currentTimeMillis();
		mCameraDevice.autoFocus(mAutoFocusCallback);
		setCameraState(FOCUSING);
	}

	@Override
	public void cancelAutoFocus() {
		if (LOGI) Log.i(TAG, "cancelAutoFocus");
		mCameraDevice.cancelAutoFocus();
		if (mCameraState != SELFTIMER_COUNTING
			&& mCameraState != SNAPSHOT_IN_PROGRESS) {
			setCameraState(IDLE);
		}
		setCameraParameters(UPDATE_PARAM_PREFERENCE);
	}

	// Preview area is touched. Handle touch focus.
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (mPausing || mCameraDevice == null || !mFirstTimeInitialized
				|| mCameraState == SNAPSHOT_IN_PROGRESS
				|| mCameraState == PREVIEW_STOPPED
				|| mCameraState == SAVING_PICTURES) {
			return false;
		}

		// Do not trigger touch focus if popup window is opened.
		if (collapseCameraControls())
			return false;

		// Check if metering area or focus area is supported.
		if (!mFocusAreaSupported && !mMeteringAreaSupported)
			return false;
		
		if (mModeActor.isSelfTimerCounting()) {
			return false;
		}

		String focusMode = mParameters.getFocusMode();
		if (focusMode == null
				|| Parameters.FOCUS_MODE_INFINITY.equals(focusMode))
			return false;

		return mFocusManager.onTouch(e);
	}

	@Override
	public void onBackPressed() {
		if (!isCameraIdle()) {
			if (mCameraState == SELFTIMER_COUNTING) {
				mModeActor.breakTimer();
			}
			return;
		} else if (mModeActor != null && mModeActor.doCancelCapture()) {
			//just cancel smile searching state.
		} else if (!collapseCameraControls()) {
			setPreference(CameraSettings.KEY_COLOR_EFFECT, Parameters.EFFECT_NONE);
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
			if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
				if (mPausing || collapseCameraControls()
						|| mCameraState == SNAPSHOT_IN_PROGRESS
						|| mModeActor.isSelfTimerEnabled())
					return true;

				// Do not do focus if there is not enough storage.
				if (!canTakePicture())
					return true;
				mKeyHalfPressed = true;
				mFocusManager.onShutterDown();
			}
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if (mFirstTimeInitialized && !mCameraKeyLongPressed
					&& event.getRepeatCount() > 0) {
				mModeActor.onShutterButtonLongPressed();
				mCameraKeyLongPressed = true;
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			// If we get a dpad center event without any focused view, move
			// the focus to the shutter button and press it.
			if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
				// Start auto-focus immediately to reduce shutter lag. After
				// the shutter button gets the focus, onShutterButtonFocus()
				// will be called again but it is fine.
				if (collapseCameraControls())
					return true;
				onShutterButtonFocus(true);
				if (mShutterButton.isInTouchMode()) {
					mShutterButton.requestFocusFromTouch();
				} else {
					mShutterButton.requestFocus();
				}
				mShutterButton.setPressed(true);
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
			if (mFirstTimeInitialized) {
				mModeActor.onShutterButtonFocus(false);
				if (mPausing || collapseCameraControls()
						|| mCameraState == SNAPSHOT_IN_PROGRESS
						|| mModeActor.isSelfTimerEnabled()) {
					return true;
				}
				mKeyHalfPressed = false;
				mFocusManager.onShutterUp();
			}
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if (mFirstTimeInitialized && !mCameraKeyLongPressed
					&& event.getRepeatCount() == 0) {
				mModeActor.onShutterButtonClick();
			}
			mCameraKeyLongPressed = false;
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

		// We need to save the holder for later use, even when the mCameraDevice
		// is null. This could happen if onResume() is invoked after this
		// function.
		mSurfaceHolder = holder;

		// The mCameraDevice will be null if it fails to connect to the camera
		// hardware. In this case we will show a dialog and then finish the
		// activity, so it's OK to ignore it.
		if (mCameraDevice == null)
			return;

		// Sometimes surfaceChanged is called after onPause or before onResume.
		// Ignore it.
		if (mPausing || isFinishing())
			return;

		// Set preview display if the surface is being created. Preview was
		// already started. Also restart the preview if display rotation has
		// changed. Sometimes this happens when the device is held in portrait
		// and camera app is opened. Rotation animation takes some time and
		// display rotation in onCreate may not be what we want.
		if (mCameraState == PREVIEW_STOPPED) {
			startPreview();
			startFaceDetection();
		} else {
			if (Util.getDisplayRotation(this) != mDisplayRotation) {
				setDisplayOrientation();
			}
			if (holder.isCreating()) {
				// Set preview display if the surface is being created and
				// preview
				// was already started. That means preview display was set to
				// null
				// and we need to set it now.
				setPreviewDisplay(holder);
			}
		}

		// If first time initialization is not finished, send a message to do
		// it later. We want to finish surfaceChanged as soon as possible to let
		// user see preview first.
		if (!mFirstTimeInitialized) {
			mHandler.sendEmptyMessage(FIRST_TIME_INIT);
		} else {
			initializeSecondTime();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPreview();
		mSurfaceHolder = null;
	}

	private void closeCamera() {
		if (mCameraDevice != null) {
			CameraHolder.instance().release();
			mFaceDetectionStarted = false;
			mCameraDevice.setZoomChangeListener(null);
			mCameraDevice.setFaceDetectionListener(null);
			mCameraDevice.setErrorCallback(null);
			mFocusManager.unRegisterCAFCallback(mCameraDevice);
			mCameraDevice = null;
			setCameraState(PREVIEW_STOPPED);
			mFocusManager.onCameraReleased();
		}
	}

	private void setPreviewDisplay(SurfaceHolder holder) {
		try {
			mCameraDevice.setPreviewDisplay(holder);
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("setPreviewDisplay failed", ex);
		}
	}

	private void setDisplayOrientation() {
		mDisplayRotation = Util.getDisplayRotation(this);
		mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation,
				mCameraId);
		mCameraDevice.setDisplayOrientation(mDisplayOrientation);
		if (mFaceView != null) {
			mFaceView.setDisplayOrientation(mDisplayOrientation);
		}
		if (mModeActor != null) {
			mModeActor.setDisplayOrientation(mDisplayOrientation);
		}
	}

	private void startPreview() {
		if (mPausing || isFinishing())
			return;

		mFocusManager.resetTouchFocus();

		mCameraDevice.setErrorCallback(mErrorCallback);

		// If we're previewing already, stop the preview first (this will blank
		// the screen).
		if (mCameraState != PREVIEW_STOPPED)
			stopPreview();

		setPreviewDisplay(mSurfaceHolder);
		setDisplayOrientation();

		if (!mSnapshotOnIdle) {
			// If the focus mode is continuous autofocus, call cancelAutoFocus
			// to
			// resume it because it may have been paused by autoFocus call.
			if (Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(mFocusManager
					.getFocusMode())) {
				mCameraDevice.cancelAutoFocus();
			}
			mFocusManager.setAeAwbLock(false); // Unlock AE and AWB.
		}
		setCameraParameters(UPDATE_PARAM_ALL);

		// Inform the mainthread to go on the UI initialization.
		if (mCameraPreviewThread != null) {
			synchronized (mCameraPreviewThread) {
				mCameraPreviewThread.notify();
			}
		}

		try {
			Log.v(TAG, "startPreview");
			mCameraDevice.startPreview();
		} catch (Throwable ex) {
			closeCamera();
			throw new RuntimeException("startPreview failed", ex);
		}

		mZoomState = ZOOM_STOPPED;
		setCameraState(IDLE);
		mFocusManager.onPreviewStarted();

		if (mSnapshotOnIdle) {
			mHandler.post(mDoSnapRunnable);
		}

		// notify again to make sure main thread is wake-up.
		if (mCameraPreviewThread != null) {
			synchronized (mCameraPreviewThread) {
				mCameraPreviewThread.notify();
			}
		}
	}

	private void stopPreview() {
		if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
			Log.v(TAG, "stopPreview");
            // maybe stop capture(stop3DShot) is ongoing,then it is not allowed to stopPreview.
            if (mModeActor != null) mModeActor.checkStopProcess();
			mCameraDevice.cancelAutoFocus(); // Reset the focus.
			mCameraDevice.stopPreview();
			mFaceDetectionStarted = false;
		}
		setCameraState(PREVIEW_STOPPED);
		mFocusManager.onPreviewStopped();
	}

	private static boolean isSupported(String value, List<String> supported) {
		return supported == null ? false : supported.indexOf(value) >= 0;
	}

	private void updateCameraParametersInitialize() {
		// Reset preview frame rate to the maximum because it may be lowered by
		// video camera application.
		List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
		if (frameRates != null) {
			Integer max = Collections.max(frameRates);
			mParameters.setPreviewFrameRate(max);
		}

		mParameters.setRecordingHint(false);

		// Disable video stabilization. Convenience methods not available in API
		// level <= 14
		String vstabSupported = mParameters
				.get("video-stabilization-supported");
		if ("true".equals(vstabSupported)) {
			mParameters.set("video-stabilization", "false");
		}
	}

	private void updateCameraParametersZoom() {
		// Set zoom.
		if (mParameters.isZoomSupported()) {
			mParameters.setZoom(mZoomValue);
		}
	}

	private void updateCameraParametersPreference() {

		// Since change scene mode may change supported values,
		// Set scene mode first,
		mSceneMode = mPreferences.getString(CameraSettings.KEY_SCENE_MODE,
				getString(R.string.pref_camera_scenemode_default));
		if (isSupported(mSceneMode, mParameters.getSupportedSceneModes())) {
			if (!mParameters.getSceneMode().equals(mSceneMode)) {
				mParameters.setSceneMode(mSceneMode);
				mCameraDevice.setParameters(mParameters);

				// Setting scene mode will change the settings of flash mode,
				// white balance, and focus mode. Here we read back the
				// parameters, so we can know those settings.
				mParameters = mCameraDevice.getParameters();
			}
		} else {
			mSceneMode = mParameters.getSceneMode();
			if (mSceneMode == null) {
				mSceneMode = Parameters.SCENE_MODE_AUTO;
			}
		}

		if (mAeLockSupported) {
			mParameters.setAutoExposureLock(mFocusManager.getAeAwbLock());
		}

		if (mAwbLockSupported) {
			mParameters.setAutoWhiteBalanceLock(mFocusManager.getAeAwbLock());
		}

        if ((mFocusAreaSupported) && (!mIsAutoFocusCallback)) {
           mParameters.setFocusAreas(mFocusManager.getFocusAreas());
        }

        if ((mMeteringAreaSupported) && (!mIsAutoFocusCallback)) {
           // Use the same area for focus and metering.
           mParameters.setMeteringAreas(mFocusManager.getMeteringAreas());
        }

		int camOri = CameraHolder.instance().getCameraInfo()[mCameraId].orientation;
		Size prePictureSize = mParameters.getPictureSize();
		// Set picture size.
		String pictureSize = mPreferences.getString(
				CameraSettings.KEY_PICTURE_SIZE, null);
		if (pictureSize == null) {
			CameraSettings.initialCameraPictureSize(this, mParameters, camOri);
		} else {
			List<Size> supported = mParameters.getSupportedPictureSizes();
			CameraSettings.setCameraPictureSize(pictureSize, supported,
					mParameters, camOri);
		}

		// Set the preview frame aspect ratio according to the picture size.
		Size size = mParameters.getPictureSize();

		/*
		 * Tricky code here, should really careful here. picture size has been
		 * swap in camOri == 0 || camOri == 180 case here size is restore again
		 * to maximize preview.
		 */
		double aspectWtoH = 0.0;
		if ((camOri == 0 || camOri == 180) && size.height > size.width) {
			aspectWtoH = (double) size.height / size.width;
		} else {
			aspectWtoH = (double) size.width / size.height;
		}

		mPreviewPanel = findViewById(R.id.frame_layout);
		mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
		mPreviewFrameLayout.setAspectRatio(aspectWtoH);

		// Set a preview size that is closest to the viewfinder height and has
		// the right aspect ratio.
		List<Size> sizes = mParameters.getSupportedPreviewSizes();
		Size optimalSize = Util.getOptimalPreviewSize(this, sizes, aspectWtoH);
		Size original = mParameters.getPreviewSize();

		Log.i(TAG, " Sensor[" + mCameraId + "]'s orientation is " + camOri);
		if (!original.equals(optimalSize)) {

			/*
			 * Tricky code here, should really careful here. swap width and
			 * height when camera orientation = 0 or 180
			 */
			if (camOri == 0 || camOri == 180) {
				mParameters.setPreviewSize(optimalSize.height,
						optimalSize.width);
			} else {
				mParameters.setPreviewSize(optimalSize.width,
						optimalSize.height);
			}

			// Zoom related settings will be changed for different preview
			// sizes, so set and read the parameters to get lastest values
			mCameraDevice.setParameters(mParameters);
			mParameters = mCameraDevice.getParameters();
		}
		Log.v(TAG, "Preview size is " + optimalSize.width + "x"
				+ optimalSize.height);

		// Set JPEG quality.
		int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(
				mCameraId, CameraProfile.QUALITY_HIGH);
		mParameters.setJpegQuality(jpegQuality);

		// For the following settings, we need to check if the settings are
		// still supported by latest driver, if not, ignore the settings.

		if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {

			// Set white balance parameter.
			String whiteBalance = mPreferences.getString(
					CameraSettings.KEY_WHITE_BALANCE,
					getString(R.string.pref_camera_whitebalance_default));
			if (isSupported(whiteBalance,
					mParameters.getSupportedWhiteBalance())) {
				mParameters.setWhiteBalance(whiteBalance);
			}

			if (mKeyHalfPressed) {
				String focusMode = Parameters.FOCUS_MODE_AUTO;
				if (!isSupported(focusMode,
						mParameters.getSupportedFocusModes())) {
					focusMode = Parameters.FOCUS_MODE_INFINITY;
				}
				mFocusManager.overrideFocusMode(focusMode);
				mParameters.setFocusMode(focusMode);
				mFocusManager.unRegisterCAFCallback(mCameraDevice);
			} else {
				// Set focus mode.
				mFocusManager.overrideFocusMode(null);
				String focusMode = mFocusManager.getFocusMode();
				mParameters.setFocusMode(focusMode);
				if (Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) {
					mFocusManager.registerCAFCallback(mCameraDevice);
				} else {
					mFocusManager.unRegisterCAFCallback(mCameraDevice);
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
		} else {
			if (mKeyHalfPressed) {
				String focusMode = Parameters.FOCUS_MODE_AUTO;
				if (!isSupported(focusMode,
						mParameters.getSupportedFocusModes())) {
					focusMode = Parameters.FOCUS_MODE_INFINITY;
				}
				mFocusManager.overrideFocusMode(focusMode);
				mParameters.setFocusMode(focusMode);
				mFocusManager.unRegisterCAFCallback(mCameraDevice);
			} else {
				String focusMode = mParameters.getFocusMode();
				mFocusManager.overrideFocusMode(focusMode);
				if (Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) {
					mFocusManager.registerCAFCallback(mCameraDevice);
				} else {
					mFocusManager.unRegisterCAFCallback(mCameraDevice);
				}
			}
		}

		// Set flash mode.
		String flashMode = mPreferences.getString(
				CameraSettings.KEY_FLASH_MODE,
				getString(R.string.pref_camera_flashmode_default));
		List<String> supportedFlash = mParameters.getSupportedFlashModes();
		if (isSupported(flashMode, supportedFlash)) {
			mParameters.setFlashMode(flashMode);
		}

		// Mediatek merge begin
		mModeActor.setMtkCameraParameters(mSceneMode, prePictureSize);
		// Mediatek merge end
	}

	// We separate the parameters into several subsets, so we can update only
	// the subsets actually need updating. The PREFERENCE set needs extra
	// locking because the preference can be changed from GLThread as well.
	private void setCameraParameters(int updateSet) {
		mParameters = mCameraDevice.getParameters();

		if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
			updateCameraParametersInitialize();
		}

		if ((updateSet & UPDATE_PARAM_ZOOM) != 0) {
			updateCameraParametersZoom();
		}

		if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
			// Mediatek merge begin
			pickModeActor();
			if (mFirstTimeInitialized) checkStorage();
			// Mediatek merge end
			updateCameraParametersPreference();
			mIsAutoFocusCallback = false;
		}

		mCameraDevice.setParameters(mParameters);
	}

	// If the Camera is idle, update the parameters immediately, otherwise
	// accumulate them in mUpdateSet and update later.
	private void setCameraParametersWhenIdle(int additionalUpdateSet) {
		mUpdateSet |= additionalUpdateSet;
		if (mCameraDevice == null) {
			// We will update all the parameters when we open the device, so
			// we don't need to do anything now.
			mUpdateSet = 0;
			return;
		} else if (isCameraIdle()) {
			setCameraParameters(mUpdateSet);
			updateSceneModeUI();
			mUpdateSet = 0;
		} else {
			if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
				mHandler.sendEmptyMessageDelayed(
						SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
			}
		}
	}

	private void gotoGallery() {
		MenuHelper.gotoCameraImageGallery(this);
	}

	private boolean isCameraIdle() {
		return (mCameraState == IDLE) || (mFocusManager.isFocusCompleted());
	}

	private boolean isImageCaptureIntent() {
		String action = getIntent().getAction();
		return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action));
	}

	private void setupCaptureParams() {
		Bundle myExtras = getIntent().getExtras();
		if (myExtras != null) {
			mSaveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
			mCropValue = myExtras.getString("crop");
		}
	}

	private void showPostCaptureAlert() {
		if (mIsImageCaptureIntent) {
			Util.fadeOut(mIndicatorControlContainer);
			Util.fadeOut(mShutterButton);

			int[] pickIds = { R.id.btn_retake, R.id.btn_done };
			for (int id : pickIds) {
				View button = findViewById(id);
				button.setClickable(true);
				Util.fadeIn(button);
			}
		}
	}

	private void hidePostCaptureAlert() {
		if (mIsImageCaptureIntent) {
			int[] pickIds = { R.id.btn_retake, R.id.btn_done };
			for (int id : pickIds) {
				View button = findViewById(id);
				button.setClickable(false);
				Util.fadeOut(button);
			}

			Util.fadeIn(mShutterButton);
			Util.fadeIn(mIndicatorControlContainer);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// Only show the menu when camera is idle.
		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(isCameraIdle());
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
		/*
		super.onCreateOptionsMenu(menu);

		if (mIsImageCaptureIntent) {
			// No options menu for attach mode.
			return false;
		} else {
			addBaseMenuItems(menu);
		}
		return true;
		*/
	}

	private void addBaseMenuItems(Menu menu) {
		MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_VIDEO,
				new Runnable() {
					public void run() {
						switchToOtherMode(ModePicker.MODE_VIDEO);
					}
				});
		MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_PANORAMA,
				new Runnable() {
					public void run() {
						switchToOtherMode(ModePicker.MODE_PANORAMA);
					}
				});
		MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_MAV,
				new Runnable() {
					public void run() {
						switchToOtherMode(ModePicker.MODE_MAV);
					}
				});

		if (mNumberOfCameras > 1) {
			menu.add(R.string.switch_camera_id)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							CameraSettings
									.writePreferredCameraId(
											mPreferences,
											((mCameraId == mFrontCameraId) ? mBackCameraId
													: mFrontCameraId));
							onSharedPreferenceChanged();
							return true;
						}
					}).setIcon(android.R.drawable.ic_menu_camera);
		}
	}

	private boolean switchToOtherMode(int mode) {
		if (isFinishing())
			return false;
		if (mImageSaver != null)
			mImageSaver.waitDone();
		MenuHelper.gotoMode(mode, Camera.this);
		mHandler.removeMessages(FIRST_TIME_INIT);
		finish();
		return true;
	}

	public boolean onModeChanged(int mode) {
		if (mode != ModePicker.MODE_CAMERA) {
			return switchToOtherMode(mode);
		} else {
			return true;
		}
	}

	public void onSharedPreferenceChanged() {
		// ignore the events after "onPause()"
		if (mPausing) return;

		if (LOGI) Log.i(TAG, "onSharedPreferenceChanged");
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				getContentResolver());
		mLocationManager.recordLocation(recordLocation);

		int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
		if (mCameraId != cameraId) {
			mIndicatorControlContainer.disableCameraPicker();
			// Restart the activity to have a crossfade animation.
			// TODO: Use SurfaceTexture to implement a better and faster
			// animation.
			if (mIsImageCaptureIntent) {
				// If the intent is camera capture, stay in camera capture mode.
				MenuHelper.gotoCameraMode(this, getIntent());
			} else {
                mHandler.removeCallbacksAndMessages(null);
				MenuHelper.gotoCameraMode(this);
				if (mImageSaver != null) {
				    mImageSaver.finish();
				    mImageSaver = null;
				}
			}

			finish();
		} else {
			String zsdPara = mParameters.getZSDMode();
			String zsdPref = mPreferences.getString("pref_camera_zsd_key", 
					getString(R.string.pref_camera_zsd_default));
			if (!mParameters.isZSDSupported() || zsdPref.equals(zsdPara)) {
				setCameraParametersWhenIdle(UPDATE_PARAM_PREFERENCE);
			} else {
				mHandler.removeMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE);
				startPreview();
				startFaceDetection();
			}
		}

		updateOnScreenIndicators();
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		keepScreenOnAwhile();
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

	public void onRestorePreferencesClicked() {
		if (mPausing)
			return;
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
			setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);
			mZoomControl.setZoomIndex(0);
		}
		if (mIndicatorControlContainer != null) {
			mIndicatorControlContainer.dismissSettingPopup();
			CameraSettings.restorePreferences(Camera.this, mPreferences,
					mParameters);
            if (Util.getS3DMode()) {
                setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
                        Parameters.CAPTURE_MODE_S3D);
            }
			mIndicatorControlContainer.reloadPreferences();
			if (mNormalCaptureIndicatorButton != null) {
				mNormalCaptureIndicatorButton.dismissPopup();
				mNormalCaptureIndicatorButton.reloadPreference();
			}
			mRestoringPreference = true;
			// mRestoringPreference will be reset to false in pickModeActor.
			onSharedPreferenceChanged();
		}
	}

	public void onOverriddenPreferencesClicked() {
		if (mPausing)
			return;
		if (mNotSelectableToast == null) {
			String str = getResources().getString(
					R.string.not_selectable_in_scene_mode);
			mNotSelectableToast = Toast.makeText(Camera.this, str,
					Toast.LENGTH_SHORT);
		}
		mNotSelectableToast.show();
	}

	private void showSharePopup() {
		mImageSaver.waitDone();
		Uri uri = mThumbnail.getUri();
		if (mSharePopup == null || !uri.equals(mSharePopup.getUri())) {
			// SharePopup window takes the mPreviewPanel as its size reference.
			mSharePopup = new SharePopup(this, uri, mThumbnail.getBitmap(),
					mOrientationCompensation, mPreviewPanel);
		}
		mSharePopup.showAtLocation(mThumbnailView, Gravity.NO_GRAVITY, 0, 0);
	}

	@Override
	public void onFaceDetection(Face[] faces, android.hardware.Camera camera) {
		mFaceView.setFaces(faces);
	}

	private void showTapToFocusToast() {
		new RotateTextToast(this, R.string.tap_to_focus, mOrientation, 
				(ViewGroup) findViewById(R.id.frame)).show();
		// Clear the preference.
		Editor editor = mPreferences.edit();
		editor.putBoolean(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, false);
		editor.apply();
	}

	private void initializeCapabilities() {
		mInitialParams = mCameraDevice.getParameters();
		Util.setModeSupport(mInitialParams);
		mFocusManager.initializeParameters(mInitialParams);
		mFocusAreaSupported = (mInitialParams.getMaxNumFocusAreas() > 0 && isSupported(
				Parameters.FOCUS_MODE_AUTO,
				mInitialParams.getSupportedFocusModes()));
		mMeteringAreaSupported = (mInitialParams.getMaxNumMeteringAreas() > 0);
		mAeLockSupported = mInitialParams.isAutoExposureLockSupported();
		mAwbLockSupported = mInitialParams.isAutoWhiteBalanceLockSupported();
	}

	// Mediatek feature begin
	private void pickModeActor() {
		boolean restoringPreference = mRestoringPreference;
		mRestoringPreference = false;
		if (mSelftimerManager == null) {
			mSelftimerManager = new SelfTimerManager(getMainLooper());
		}
		if (mIsImageCaptureIntent) {
			mModeActor = new ActorNormal(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, Parameters.CAPTURE_MODE_NORMAL);
			return;
		}
		String captureMode = mPreferences.getString(
				CameraSettings.KEY_HDR_CAPTURE_KEY, "off");
		if (captureMode.equals("off")) {
			captureMode = mPreferences.getString(
					CameraSettings.KEY_NORMAL_CAPTURE_KEY,
					Parameters.CAPTURE_MODE_NORMAL);
		} else {
			captureMode = Parameters.CAPTURE_MODE_HDR;
			setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
					Parameters.CAPTURE_MODE_NORMAL);
		}

		Log.i(TAG, "Picking capture mode : " + captureMode);
		if (mModeActor != null && mModeActor.checkMode(captureMode)) {
			mModeActor.updateMembers(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences);
			mModeActor.updateModePreference();
			return;
		} else if (mModeActor != null) {
			mModeActor.restoreModeUI(restoringPreference);
		}

		if (Parameters.CAPTURE_MODE_NORMAL.equals(captureMode)) {
			if (isSupported(Parameters.CAPTURE_MODE_CONTINUOUS_SHOT,
					mParameters.getSupportedCaptureMode())) {
				mModeActor = new ActorContinuousShot(this, mCameraDevice, mParameters,
						mPreferenceGroup, mPreferences, Parameters.CAPTURE_MODE_CONTINUOUS_SHOT);
			} else {
				mModeActor = new ActorNormal(this, mCameraDevice, mParameters,
						mPreferenceGroup, mPreferences, captureMode);
			}
		} else if (Parameters.CAPTURE_MODE_BEST_SHOT.equals(captureMode)) {
			mModeActor = new ActorBest(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (Parameters.CAPTURE_MODE_EV_BRACKET_SHOT.equals(captureMode)) {
			mModeActor = new ActorEv(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (captureMode.startsWith(Parameters.CAPTURE_MODE_BURST_SHOT)) {
			int burstNo = Integer.parseInt(captureMode.substring(9));
			mModeActor = new ActorBurst(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences,
					Parameters.CAPTURE_MODE_BURST_SHOT, burstNo);
		} else if (Parameters.CAPTURE_MODE_SMILE_SHOT.equals(captureMode)) {
			mModeActor = new ActorSmile(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (Parameters.CAPTURE_MODE_HDR.equals(captureMode)) {
			mModeActor = new ActorHdr(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (Parameters.CAPTURE_MODE_ASD.equals(captureMode)) {
			mModeActor = new ActorAsd(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (Parameters.CAPTURE_MODE_FB.equals(captureMode)) {
			mModeActor = new ActorFaceBeauty(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else if (Parameters.CAPTURE_MODE_S3D.equals(captureMode)) {
			Log.v(TAG, "should switch to 3d");
			// Setting autorama for debugging.
			mModeActor = new ActorSingle3DAutorama(this, mCameraDevice, mParameters,
					mPreferenceGroup, mPreferences, captureMode);
		} else {
			throw new RuntimeException("wrong capture mode");
		}

		if (mNormalCaptureIndicatorButton != null) {
			mModeActor.updateCaptureModeButton(mNormalCaptureIndicatorButton, true);
		} else {
			mHandler.sendEmptyMessage(UPDATE_CAPTURE_INDICATOR);
		}
		if (mZoomControl != null) {
			mModeActor.updateZoomControl(mZoomControl, true);
		}
		mModeActor.setDisplayOrientation(mDisplayOrientation);
		// TODO check the timing of set sceneMode related setting
		mModeActor.updateModePreference();
	}

	public void initializeControlBarIndicator() {
		mNormalCaptureIndicatorButton = (ControlBarIndicatorButton) findViewById(R.id.normal_capture_button);
		if (mNormalCaptureIndicatorButton == null)
			return;
		IconListPreference capturePref = (IconListPreference) mPreferenceGroup
				.findPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY);
		if (capturePref != null) {
			mNormalCaptureIndicatorButton.initializeIndicatorPref(capturePref);
			mNormalCaptureIndicatorButton.setSettingChangedListener(this);
			if (!mIsImageCaptureIntent) {
				mNormalCaptureIndicatorButton.setVisibility(View.VISIBLE);
			}
		}
		mRemainPictureView = (TextView) findViewById(R.id.remain_pictures);
		if (supportSingle3dSwitch()) {
			mRemainPictureView.setVisibility(View.GONE);
			LinearLayout remainPictureLayout = (LinearLayout) findViewById(R.id.remain_pictures_layout);
			LayoutParams lp = (LayoutParams) remainPictureLayout.getLayoutParams();
			lp.height = getResources().getDimensionPixelSize(R.dimen.switch_indicators_height);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
		}
	}

	public boolean readyToCapture() {
		return mModeActor.readyToCapture();
	}

	public boolean doSmileShutter() {
		return mModeActor.doSmileShutter();
	}

	public boolean isZooming() {
		Log.i(TAG, "mZoomState = " + mZoomState);
		return (mZoomState != ZOOM_STOPPED);
	}

	public void logCaptureTime() {
		mJpegPictureCallbackTime = System.currentTimeMillis();
		// If postview callback has arrived, the captured image is displayed
		// in postview callback. If not, the captured image is displayed in
		// raw picture callback.
		if (mPostViewPictureCallbackTime != 0) {
			mShutterToPictureDisplayedTime = mPostViewPictureCallbackTime
					- mShutterCallbackTime;
			mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
					- mPostViewPictureCallbackTime;
		} else {
			mShutterToPictureDisplayedTime = mRawPictureCallbackTime
					- mShutterCallbackTime;
			mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
					- mRawPictureCallbackTime;
		}
		Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
				+ mPictureDisplayedToJpegCallbackTime + "ms");
	}

	/*package*/ void actorCapture() {
		capture();
	}

	protected void overrideSettings(final String... keyvalues) {
		Camera.this.runOnUiThread(new Runnable(){
			public void run() {
				if (mIndicatorControlContainer != null) {
					mIndicatorControlContainer.overrideSettings(keyvalues);
				}
			}
		});
	}

	protected void overrideSettings(final String key, final String value, final String[] values) {
		Camera.this.runOnUiThread(new Runnable(){
			public void run() {
				if (mIndicatorControlContainer != null) {
					mIndicatorControlContainer.overrideSettings(key, value, values);
				}
			}
		});
	}

	public void resumePreview() {
		if (!mIsImageCaptureIntent) {
			startPreview();
			startFaceDetection();
		}
		checkStorage();
	}

	public void onTimerStart() {

	}

	public void onTimerStop() {
		cancelAutoFocus();
	}

	public void setPreference(String key, String value) {
		Editor editor = mPreferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public int getCameraId() {
		return mCameraId;
	}

	public long getPictureRemaining() {
		return mPicturesRemaining;
	}

	public void resetCaptureMode() {
		if (!isFromInternal() && !mIsImageCaptureIntent) {
			setPreference(CameraSettings.KEY_HDR_CAPTURE_KEY, "off");
			setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
					Parameters.CAPTURE_MODE_NORMAL);
		}
        if (Util.getS3DMode()) {
            setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
                    Parameters.CAPTURE_MODE_S3D);
        } else {
            setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
                    Parameters.CAPTURE_MODE_NORMAL);
        }
	}

	protected Parameters getCameraParameters() {
		return mParameters;
	}

	public PreviewFrameLayout getPreviewFrameLayout() {
		return mPreviewFrameLayout;
	}

	public void updateThumbnailButton(Thumbnail thumb) {
		mThumbnail = thumb;
		updateThumbnailButton();
	}

	public void updateCaptureModeIndicatorOnScreen() {
		mModeActor.updateCaptureModeIndicator();
	}

	private boolean isFromInternal() {
		Intent intent = getIntent();
		String action = intent.getAction();
		Log.i(TAG, "Check action = " + action);
		return (MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action) && MenuHelper
				.isCameraModeSwitching(intent));
	}

	public void syncUIWithPreference() {
		updateSceneModeUI();
		mIndicatorControlContainer.reloadPreferences();
		updateOnScreenIndicators();
	}

	public void resetZoomControl() {
		mZoomValue = 0;
		if (mZoomControl != null) {
			mZoomControl.setZoomIndex(0);
		}
	}

	private PreferenceGroup filterPreferenceScreenByIntent(
            PreferenceGroup screen) {
        if (mIsImageCaptureIntent) {
        	CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_HDR_CAPTURE_KEY);
        }
        if (Util.getS3DMode()) {
            CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_PICTURE_SIZE);
        }
        return screen;
    }

	/**
	 * The function is to ensure FD is stopped
	 * in front sensor except Smile shot.
	 */
	public void ensureFDState(boolean enable) {
		if (mCameraState != IDLE) {
			return;
		}
		if (enable) {
			startFaceDetection();
		} else {
			CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				stopFaceDetection();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (mSelftimerManager != null) {
			mSelftimerManager.releaseTone();
			mSelftimerManager = null;
		}
		if (mImageSaver != null) {
			mImageSaver.saveDoneFinish();
			mImageSaver = null;
		}
		if (mThumbnail != null) {
		    mThumbnail = null;
		}
		super.onDestroy();
	}

    @OnClickAttr
    public void onSingle3DClicked(View v) {
        if (isCameraIdle() && mSingle3DSwitch != null) {
            Util.switch3DMode();
            if (Util.getS3DMode()) {
                setPreference(CameraSettings.KEY_HDR_CAPTURE_KEY, "off");
                setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
                        Parameters.CAPTURE_MODE_S3D);
            } else {
                setPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY,
                        Parameters.CAPTURE_MODE_NORMAL);
            }
            collapseCameraControls();
            reInitializeIndicatorControl();
            onSharedPreferenceChanged();
            // we need change UI visibility in 2/3D mode.
            switchSingle3DModeUI();
        }
    }

    private void switchSingle3DModeUI() {
        if (mIsImageCaptureIntent) return;
        int visibility = Util.getS3DMode() ? View.GONE : View.VISIBLE;
        if (mNormalCaptureIndicatorButton != null)
            mNormalCaptureIndicatorButton.setVisibility(visibility);
        if (mIndicatorControlContainer != null) 
            mIndicatorControlContainer.setCameraPickerVisibility(visibility);

        Util.setModeSupport(mParameters);
        mModePicker.setModeSupport();
        if (Util.getS3DMode()) {
            mSingle3DSwitch.setImageResource(R.drawable.btn_ic_3d_switch_off);
            if (mOnScreenIndicators != null)
                mOnScreenIndicators.setOrientation(270);
            if (mIndicatorControlContainer != null)
                mIndicatorControlContainer.setOrientation(270);
            mHandler.sendEmptyMessage(SHOW_SINGLE3D_GUIDE);
        } else {
            mSingle3DSwitch.setImageResource(R.drawable.btn_ic_3d_switch_on);
            setOrientationIndicator(mOrientationCompensation);
            if (mSingle3DGuide != null) mSingle3DGuide.hide();
        }
    }

    private void hideOnScreenIndicator() {
        if (!Util.getS3DMode()) return;
        if (mExposureIndicator != null) mExposureIndicator.setVisibility(View.GONE);
        if (mFlashIndicator != null) mFlashIndicator.setVisibility(View.GONE);
        if (mSceneIndicator != null) mSceneIndicator.setVisibility(View.GONE);
        if (mWhiteBalanceIndicator != null) mWhiteBalanceIndicator.setVisibility(View.GONE);
        if (mFocusIndicator != null) mFocusIndicator.setVisibility(View.GONE);
        if (mASDIndicator != null) mASDIndicator.setVisibility(View.GONE);
        if (mHDRIndicator != null) mHDRIndicator.setVisibility(View.GONE);
    }

    public boolean supportSingle3dSwitch() {
        List<String> supportCaptureModes = mInitialParams.getSupportedCaptureMode();
        if (FeatureOption.MTK_S3D_SUPPORT && supportCaptureModes != null && 
                supportCaptureModes.indexOf(Parameters.CAPTURE_MODE_S3D) != -1) {
            return true;
        }
        return false;
	}

    public void showSingle3DGuide(int textResourceId) {
        if (mSingle3DGuide == null) {
            mSingle3DGuide = new RotateTextToast(this, textResourceId, 270,
                    (ViewGroup) findViewById(R.id.frame));
        } else {
            mSingle3DGuide.changeTextContent(textResourceId);
        }
    	mSingle3DGuide.showTransparent();
    }

    private void resetCapture(boolean finish) {
		Log.d(TAG, "resetCapture finish = "+finish);
			//if we need to wait for merge,unlockAeAwb must be called after we receive the last callback.
			//so if isMerge = true,we will do it later in onCaptureDone.
		if (mCameraState == SNAPSHOT_IN_PROGRESS && finish) {
			//unlockAeAwb();
        	setCameraState(IDLE);
		}
		setOrientationIndicator(mOrientationCompensation);			
		mShutterButton.setEnabled(true);
        keepScreenOnAwhile();
    }

    public long mTimeTaken;
	private String mNameFormat;
    public void onAutoramaCaptureDone(boolean isMerge) {
    	Log.d(TAG, "onAutoramaCaptureDone isMerge " + isMerge);
		if (mCameraState == SNAPSHOT_IN_PROGRESS) {
			resetCapture(true);
		}

		if (isMerge) {
			new Thread() {
				@Override
				public void run() {
					final String name = Util.createName(mNameFormat, mTimeTaken);
	                final String fpath = Storage.generateFilepath(name, Storage.PICTURE_TYPE_MPO_3D);

					ExifInterface exif = null;
					try {
						exif = new ExifInterface(fpath);
					} catch (IOException ex) {
						Log.e(TAG, "cannot read exif", ex);
					}
					int orientation = Util.getExifOrientation(exif);
					int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
					int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
					Log.d(TAG, "onCaptureDone.run orientation "
							+ orientation + " w * h:" + width + "*" + height);
					Uri uri = Storage.addImage(getContentResolver(), name, mTimeTaken, null,
						orientation,width,height,Storage.PICTURE_TYPE_MPO_3D);
					Thumbnail tb = null;									
					if (uri != null) {
						int widthRatio = (int) Math.ceil((double) width
								/ mPreviewFrameLayout.getWidth());
						int heightRatio = (int) Math.ceil((double) height
								/ mPreviewFrameLayout.getHeight());
						int inSampleSize = Integer.highestOneBit(
								Math.max(widthRatio, heightRatio));
						tb = Thumbnail.createThumbnail(
								fpath, orientation, inSampleSize, uri, 
								MediaStore.Images.Media.STEREO_TYPE_SIDE_BY_SIDE);
						//TBD:this is to avoid the write operation in onPause,should be further optimized.
						if (tb != null) {
							tb.saveTo(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
						}
						Util.broadcastNewPicture(Camera.this, uri);
					}
					// Temp solution
					mModeActor.mHandler.sendEmptyMessage(ModeActor.MSG_BURST_SAVING_DONE);
					mHandler.sendMessage(mHandler.obtainMessage(PICTURES_SAVING_DONE, 0, 0, tb));
				}
			}.start();
		}
    }

    public void onAutoramaMergeStarted() {
		if (!mPausing) {
			mRotateDialog.showWaitingDialog(
					getResources().getString(R.string.savingImage));
			mSingle3DCancel.setEnabled(false);
		}
    }

	public void onAutoramaBtnPressed(boolean okKey) {
		Log.d(TAG, "onKeyPressed ok = " + okKey);		
		if (mCameraState == SNAPSHOT_IN_PROGRESS) {
			stopCapture(okKey);
	        hidePostSingle3DControlAlert();
		}
	}

	public void stopCapture(boolean isMerge) {
		ActorSingle3DAutorama autorama3D = (ActorSingle3DAutorama) mModeActor;
		//only do merge when already have captured images.
		if (!autorama3D.hasCaptured()) {
			isMerge = false;
		}
		resetCapture(!isMerge);
		autorama3D.stop(isMerge);
	}

	public void showPostSingle3DControlAlert() {
        if (mModeActor instanceof ActorSingle3DAutorama) {
            Util.fadeOut(mShutterButton, mThumbnailView, mModePicker);
            mSingle3DSwitch.setVisibility(View.GONE);
            mIndicatorControlContainer.setVisibility(View.GONE);
            mSingle3DControlBar.setVisibility(View.VISIBLE);
            if (mFocusAreaIndicator != null) mFocusAreaIndicator.setVisibility(View.GONE);
        }
    }

	public void hidePostSingle3DControlAlert() {
		if (mModeActor instanceof ActorSingle3DAutorama) {
			mSingle3DControlBar.setVisibility(View.GONE);
			mSingle3DCancel.setEnabled(true);
			mThumbnailView.setVisibility(View.VISIBLE);
			mShutterButton.setVisibility(View.VISIBLE);
			mModePicker.setVisibility(View.VISIBLE);
			mSingle3DSwitch.setVisibility(View.VISIBLE);
			mIndicatorControlContainer.setVisibility(View.VISIBLE);
            if (mFocusAreaIndicator != null) mFocusAreaIndicator.setVisibility(View.VISIBLE);
	        showSingle3DGuide(R.string.single3d_guide_shutter);
		}
	}

	public void setCapturePath() {
		mTimeTaken = System.currentTimeMillis();
		final String name = Util.createName(mNameFormat, mTimeTaken);
		final String fpath = Storage.generateFilepath(name, Storage.PICTURE_TYPE_MPO_3D);
		mParameters.setCapturePath(fpath);		
	}

    public void setCancelButtonOnClickListener(OnClickListener onCancelClickListener) {
        mSingle3DCancel.setOnClickListener(onCancelClickListener);
    }

    private void initializeSingle3DControl() {
        Log.v(TAG, "initializeSingle3DControl "+ mSingle3DSwitch);
        if (mSingle3DSwitch != null && !mIsImageCaptureIntent) {
            if (supportSingle3dSwitch()) {
                mSingle3DSwitch.setImageResource(R.drawable.btn_ic_3d_switch_on);
                mSingle3DSwitch.setVisibility(View.VISIBLE);
            } else {
                mSingle3DSwitch.setVisibility(View.GONE);
            }
            if (Util.getS3DMode()) {
                switchSingle3DModeUI();
            }
        }
    }

    public int getOrientation() {
    	return mOrientation;
    }

    public long getRemainPictures() {
    	return mPicturesRemaining;
    }

    public boolean checkCameraState() {
    	Log.i(TAG, "Check camera state in ModeActor");
    	if (mPausing || collapseCameraControls())
			return false;

		// If the user wants to do a snapshot while the previous one is still
		// in progress, remember the fact and do it after we finish the previous
		// one and re-start the preview. Snapshot in progress also includes the
		// state that autofocus is focusing and a picture will be taken when
		// focus callback arrives.
		if (mFocusManager.isFocusingSnapOnFinish()
				|| mCameraState == SNAPSHOT_IN_PROGRESS
				|| mCameraState == SAVING_PICTURES) {
			return false;
		}

		if (mImageSaver.isBusy()) {
			new RotateTextToast(this, R.string.camera_saving_busy, mOrientation, 
					(ViewGroup) findViewById(R.id.frame)).show();
			return false;
		}

		mSnapshotOnIdle = false;
		return true;
    }

    public PreferenceGroup getPreferenceGroup() {
    	return mPreferenceGroup;
    }
	// Mediatek feature end
}
