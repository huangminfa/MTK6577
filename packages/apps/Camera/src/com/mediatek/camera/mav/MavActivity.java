/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.camera.mav;

import com.android.camera.ActivityBase;
import com.android.camera.ui.ZoomControl;
import com.android.camera.CameraDisabledException;
import com.android.camera.CameraHardwareException;
import com.android.camera.CameraHolder;
import com.android.camera.Exif;
import com.android.camera.MenuHelper;
import com.android.camera.ModePicker;
import com.android.camera.OnClickAttr;
import com.android.camera.PreviewFrameLayout;
import com.android.camera.R;
import com.android.camera.RotateDialogController;
import com.android.camera.ShutterButton;
import com.android.camera.Storage;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.ui.PopupManager;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.SharePopup;
import com.android.camera.LocationManager;
import com.android.camera.FocusManager;
import com.android.camera.OnScreenHint;
import com.android.camera.ComboPreferences;
import com.android.camera.RecordLocationPreference;
import com.android.camera.CameraSettings;
import com.mediatek.featureoption.FeatureOption;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.CameraSound;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Collections;

/**
 * Activity to handle Mav capturing.
 */
public class MavActivity extends ActivityBase implements FocusManager.Listener,
        ModePicker.OnModeChangeListener,ShutterButton.OnShutterButtonListener,
        SurfaceHolder.Callback,MavController.CaptureEventListener,
        View.OnTouchListener,android.hardware.Camera.ErrorCallback,LocationManager.Listener  {
    private static final String TAG = "MavActivity";

    private MavController mMavController;
    private PreviewFrameLayout mPreviewFrameLayout;
    private SurfaceHolder mSurfaceHolder = null;
    private SurfaceView mSurfaceView; 
	private ImageView mGpsIndicator;
	private ImageView mWhiteBalanceIndicator;
			
    private static final int MSG_FINAL_IMAGE_READY = 1;
    private static final int FIRST_TIME_INIT = 2;
    private static final int MSG_GENERATE_FINAL_IMAGE_ERROR = 3;
    private static final int MSG_CLEAR_SCREEN_DELAY = 5;
    private static final int MSG_GET_THUMBNAIL_DONE = 6;	

    private static final int SCREEN_DELAY = 2 * 60 * 1000;

    private static final int PREVIEW_STOPPED = 0;
    private static final int IDLE = 1;  // preview is active
    // Focus is in progress. The exact focus state is in Focus.java.
    private static final int FOCUSING = 2;
    private static final int SNAPSHOT_IN_PROGRESS = 3;
    private int mCameraState = PREVIEW_STOPPED;

    private static final int REVIEW_DURATION = 2000;

    private int mDisplayRotation;
    private boolean mPausing;
    private View mFullLayout;
    private RotateLayout mOnScreenDisplayLayout;
    private RotateLayout mOnScreenProgress;
    private TextView mRemainPictureView;
    private ShutterButton mShutterButton;

    private final int mCameraId = CameraHolder.instance().getBackCameraId();
    private RotateImageView mThumbnailView;
    private Thumbnail mThumbnail;
	private boolean mThumbnailUpdated;		
    private SharePopup mSharePopup;
	private RotateTextToast mRotateTextToast;
    private ModePicker mModePicker;
    private long mTimeTaken;
    private long mPicturesRemaining;	

	private Size mPictureSize;

    private MyOrientationEventListener mOrientationEventListener;
    // The value could be 0, 90, 180, 270 for the 4 different orientations measured in clockwise
    // respectively.
    private int mOrientation= OrientationEventListener.ORIENTATION_UNKNOWN;

    private int mOrientationCompensation;
    private RotateDialogController mRotateDialog;
    private RotateLayout mFocusAreaIndicator;
    private CameraSound mCameraSound;
	private String mNameFormat;

    private LocationManager mLocationManager;

    private FocusManager mFocusManager;
    private Parameters mParameters;
	ComboPreferences mPreferences;
    private boolean mFocusAreaSupported;
    private boolean mMeteringAreaSupported;
    private boolean mAeLockSupported;
    private boolean mAwbLockSupported;

	private static final int ZOOM_STOPPED = 0;
	private static final int ZOOM_START = 1;
	private static final int ZOOM_STOPPING = 2;

	private int mZoomState = ZOOM_STOPPED;
	private boolean mSmoothZoomSupported = false;
	private int mZoomValue; // The current zoom value.
	private int mZoomMax;
	private int mTargetZoomValue;
	private ZoomControl mZoomControl;
    private final ZoomListener mZoomListener = new ZoomListener();
    private ViewGroup mAlertControlBar;
    private RotateLayout mCancelGroup;
    private RotateImageView mCancelButton;

	private Runnable mFalseShutterCallback = new Runnable() {
        @Override
        public void run() {
			//simulate an onShutter event since it is not supported in this mode.
			mFocusManager.onShutter();
    	}
	}; 

	private Runnable mUpdateHintRunnable = new Runnable() {
        public void run() {
            updateStorageHint();
        }
    };

	private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
				Log.d(TAG, "handleMessage what= "+msg.what);
            
                switch (msg.what) {
                    case FIRST_TIME_INIT:
						initializeFirstTime();
                        break;					
                    case MSG_FINAL_IMAGE_READY:
                        mRotateDialog.dismissDialog();
                        if (mCameraState != SNAPSHOT_IN_PROGRESS) {
                            hidePostControlAlert();
                        }
                        // Set the thumbnail bitmap here because mThumbnailView must be accessed
                        // from the UI thread.
                        if (msg.obj != null) {
							mThumbnailUpdated = true;
							mThumbnail = (Thumbnail)msg.obj;
						}
                        updateThumbnailButton();
						checkStorage();
                        // Share popup may still have the reference to the old thumbnail. Clear it.
                        mSharePopup = null;
                        //resetToPreview();
                        break;
                    case MSG_GENERATE_FINAL_IMAGE_ERROR:
						showCaptureError();
                        break;
                    case MSG_CLEAR_SCREEN_DELAY:
                        getWindow().clearFlags(WindowManager.LayoutParams.
                                FLAG_KEEP_SCREEN_ON);
                        break;
                    case MSG_GET_THUMBNAIL_DONE:
						if (!mThumbnailUpdated) {
							mThumbnailUpdated = true;
							mThumbnail = (Thumbnail)msg.obj;
						}
						updateThumbnailButton();
                        break;							
                }
            }
    };	
	
    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
			//Log.d(TAG, "onOrientationChanged orientation = "+orientation);
        
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(MavActivity.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                setOrientationIndicator(mOrientationCompensation);
            }
        }
    }

    private void setOrientationIndicator(int degree) {
		
		//Log.d(TAG, "setOrientationIndicator degree = "+degree);
		if (mCameraState == SNAPSHOT_IN_PROGRESS) return;
        if (mSharePopup != null) mSharePopup.setOrientation(degree);
        if (mStorageHint != null) mStorageHint.setOrientation(degree);
        Rotatable[] rotateLayout = {
                    (Rotatable) mRotateDialog,
                    (Rotatable) mModePicker,
                    (Rotatable) mThumbnailView,
                    (Rotatable) mFocusAreaIndicator,
                    mZoomControl};
        for (Rotatable r : rotateLayout) {
        	r.setOrientation(degree);
        }		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return false;
    	/*
        super.onCreateOptionsMenu(menu);
        addBaseMenuItems(menu);
        return true;
        */
    }

    private void addBaseMenuItems(Menu menu) {
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_CAMERA, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_CAMERA);
            }
        });
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_VIDEO, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_VIDEO);
            }
        });
        MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_PANORAMA, new Runnable() {
            public void run() {
                switchToOtherMode(ModePicker.MODE_PANORAMA);
            }
        });
    }

    public static int getExifOrientation(ExifInterface exif) {
        int degree = 0;
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }

	public void onMergeStarted() {
		if (!mPausing) {
			mRotateDialog.showWaitingDialog(
					getResources().getString(R.string.pano_review_saving_indication_str));
			mRotateTextToast.changeTextContent(R.string.empty);
			mCancelButton.setEnabled(false);
		}
	}	

	public void onCaptureDone(boolean isMerge) {
		
		Log.d(TAG, "onCaptureDone isMerge "+isMerge);
		if (mCameraState == SNAPSHOT_IN_PROGRESS) {
			resetCapture(true);
		}		
		
					if (isMerge) {			
						new Thread() {
							@Override
							public void run() {
									final String name = createName(mNameFormat,mTimeTaken);
					                final String fpath = Storage.generateMpoFilepath(name);	

									ExifInterface exif = null;
									try {
										exif = new ExifInterface(fpath);
									} catch (IOException ex) {
										Log.e(TAG, "cannot read exif", ex);
									}
									int orientation = getExifOrientation(exif);
									int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
									int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
									Log.d(TAG, "onCaptureDone.run orientation "+orientation+" w * h:"+width+"*"+height);
									Uri uri = Storage.addImage(getContentResolver(), name, mTimeTaken, null,
										orientation,width,height,Storage.PICTURE_TYPE_MPO);
									Thumbnail tb = null;
									if (uri != null) {
										int widthRatio = (int) Math.ceil((double) width
												/ mFullLayout.getWidth());
										int heightRatio = (int) Math.ceil((double) height
												/ mFullLayout.getHeight());
										int inSampleSize = Integer.highestOneBit(
												Math.max(widthRatio, heightRatio));
										tb = Thumbnail.createThumbnail(
												fpath, orientation, inSampleSize, uri);
										//TBD:this is to avoid the write operation in onPause,should be further optimized.
										if (tb != null) {
											tb.saveTo(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
										}
										Util.broadcastNewPicture(MavActivity.this, uri);
									}
					mHandler.sendMessage(mHandler.obtainMessage(MSG_FINAL_IMAGE_READY,0,0,tb));
								}
						}.start();
					}
				}

    @Override
    public void onCreate(Bundle icicle) {
		Log.d(TAG, "onCreate");

		mPreferences = new ComboPreferences(this);
        String[] defaultFocusModes = getResources().getStringArray(
                R.array.pref_camera_focusmode_default_array);		
        mFocusManager = new FocusManager(mPreferences, defaultFocusModes);
        mPreferences.setLocalId(this, mCameraId);
		
        super.onCreate(icicle);

        Window window = getWindow();
        Util.enterLightsOutMode(window);
        Util.initializeScreenBrightness(window, getContentResolver());

        createContentView();
        mSurfaceView = (SurfaceView) findViewById(R.id.pano_renderer);
		mSurfaceView.setOnTouchListener(this);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mOrientationEventListener = new MyOrientationEventListener(this);
		mMavController = new MavController(this,this);
        mAlertControlBar = (ViewGroup) findViewById(R.id.capture_control_bar);
        mCancelGroup = (RotateLayout)findViewById(R.id.capture_cancel_group);
        mCameraSound = new CameraSound();
    }

	protected void onPostCreate(Bundle saveInstanceState) {
		super.onPostCreate(saveInstanceState);
		if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			mOnScreenDisplayLayout.setOrientation(270);
			mOnScreenProgress.setOrientation(270);
			mMavController.setOrientation(270);
			mRotateTextToast = new RotateTextToast(this,R.string.toast_mav_how_to_capture
					,270,(ViewGroup) findViewById(R.id.frame));
        } else {
			mRotateTextToast = new RotateTextToast(this,R.string.toast_mav_how_to_capture
					,0,(ViewGroup) findViewById(R.id.frame));			
		}		
		mRotateTextToast.showTransparent();
	}

    private void setupCamera() throws CameraHardwareException, CameraDisabledException {		
		Log.d(TAG, "setupCamera");
        openCamera();
        mParameters = mCameraDevice.getParameters();
		initializeCapabilities();
        setupCaptureParams(mParameters);
        configureCamera(mParameters);
    }

    private void openCamera() throws CameraHardwareException, CameraDisabledException {
		Log.d(TAG, "openCamera");
        mCameraDevice = Util.openCamera(this, mCameraId);
		mMavController.setCamera(mCameraDevice);
    }
		
    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

	private void initOnScreenIndicator() {
		mGpsIndicator = (ImageView) findViewById(R.id.onscreen_gps_indicator);
		mWhiteBalanceIndicator = (ImageView) findViewById(R.id.onscreen_white_balance_indicator);
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

    private void initializeCapabilities() {
        Util.setModeSupport(mParameters);
        mModePicker.setModeSupport();
        mFocusManager.initializeParameters(mParameters);
        mFocusAreaSupported = (mParameters.getMaxNumFocusAreas() > 0
                && isSupported(Parameters.FOCUS_MODE_AUTO,
                        mParameters.getSupportedFocusModes()));
        mMeteringAreaSupported = (mParameters.getMaxNumMeteringAreas() > 0);
        mAeLockSupported = mParameters.isAutoExposureLockSupported();
        mAwbLockSupported = mParameters.isAutoWhiteBalanceLockSupported();
    }

	private void unlockAeAwb(){
		if (mCameraState != PREVIEW_STOPPED) {
			mFocusManager.setAeAwbLock(false); // Unlock AE and AWB.
			setFocusParameters();
		}
	}
	
    public void setFocusParameters() {
        if (mAeLockSupported) {
            mParameters.setAutoExposureLock(mFocusManager.getAeAwbLock());
        }

        if (mAwbLockSupported) {
            mParameters.setAutoWhiteBalanceLock(mFocusManager.getAeAwbLock());
    	}

        if (mFocusAreaSupported) {
            mParameters.setFocusAreas(mFocusManager.getFocusAreas());
        }

        if (mMeteringAreaSupported) {
            // Use the same area for focus and metering.
            mParameters.setMeteringAreas(mFocusManager.getMeteringAreas());
        }

		//if we do not add this, the AF will not work properly.
		mParameters.setFocusMode(mFocusManager.getFocusMode());
        configureCamera(mParameters);
    }

    private void setupCaptureParams(Parameters parameters) {
        // Set the preview frame aspect ratio according to the picture size.
        mPictureSize = mParameters.getPictureSize();		
        mPreviewFrameLayout.setAspectRatio((double) mPictureSize.width / mPictureSize.height);

        int camOri = CameraHolder.instance().getCameraInfo()[mCameraId].orientation;
        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mParameters.getSupportedPreviewSizes();
        Size optimalSize = Util.getOptimalPreviewSize(this,
                sizes, (double) mPictureSize.width / mPictureSize.height);
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
            // sizes, so set and read the mParameters to get lastest values
            mCameraDevice.setParameters(mParameters);
            mParameters = mCameraDevice.getParameters();
        }
        Log.v(TAG, "Preview size is " + optimalSize.width + "x" + optimalSize.height);

        mParameters.setRecordingHint(false);

        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera application.
        List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            mParameters.setPreviewFrameRate(max);
        }

        // Disable video stabilization. Convenience methods not available in API
        // level <= 14
        String vstabSupported = mParameters.get("video-stabilization-supported");
        if ("true".equals(vstabSupported)) {
            mParameters.set("video-stabilization", "false");
        }		

		//reset zoom value.
		mParameters.setZoom(0);

		updateCameraParametersPreference();
    }

	private void updateCameraParametersPreference(){
        // Set white balance parameter.
        String whiteBalance = mPreferences.getString(
                    CameraSettings.KEY_WHITE_BALANCE,
                    getString(R.string.pref_camera_whitebalance_default));
        if (isSupported(whiteBalance,
                    mParameters.getSupportedWhiteBalance())) {
        	mParameters.setWhiteBalance(whiteBalance);
        } 				

		//AE
		String exposureMeter = mPreferences.getString(
                CameraSettings.KEY_EXPOSURE_METER,
                getString(R.string.pref_camera_exposuremeter_default));
		if (isSupported(exposureMeter, mParameters.getSupportedExposureMeter())){
			mParameters.setExposureMeter(exposureMeter);
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

		//ISO
	    String isoSpeed = mPreferences.getString(
	                    CameraSettings.KEY_ISO,
	                    getString(R.string.pref_camera_iso_default));
	    if (isSupported(isoSpeed, mParameters.getSupportedISOSpeed())){
	        mParameters.setISOSpeed(isoSpeed);
	    }	

		//anti-flincker
		String antibanding = mPreferences.getString(
                CameraSettings.KEY_ANTI_BANDING,
                getString(R.string.pref_camera_antibanding_default));
        if (isSupported(antibanding, mParameters.getSupportedAntibanding())){
            mParameters.setAntibanding(antibanding);
        }		

		// Set zoom.
        updateCameraParametersZoom();
    }

    private void updateCameraParametersZoom() {
        // Set zoom.
        if (mParameters.isZoomSupported()) {
            mParameters.setZoom(mZoomValue);
        }
    }

    private void configureCamera(Parameters parameters) {
        mCameraDevice.setParameters(parameters);
    }

    private boolean switchToOtherMode(int mode) {
        if (isFinishing()) {
            return false;
        }
        MenuHelper.gotoMode(mode, this);
        finish();
        return true;
    }

    public boolean onModeChanged(int mode) {
        if (mode != ModePicker.MODE_MAV) {
            return switchToOtherMode(mode);
        } else {
            return true;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
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
        if (mCameraDevice == null) return;

        // Sometimes surfaceChanged is called after onPause or before onResume.
        // Ignore it.
        if (mPausing || isFinishing()) return;

        if (mCameraState == PREVIEW_STOPPED) {
            startPreview();
        } else {
            if (Util.getDisplayRotation(this) != mDisplayRotation) {
                setDisplayOrientation();
                }
            if (holder.isCreating()) {
                // Set preview display if the surface is being created and preview
                // was already started. That means preview display was set to null
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

    public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		
        stopPreview();
        mSurfaceHolder = null;
    }

	public void setCapturePath() {
		mTimeTaken = System.currentTimeMillis();
		final String name = createName(mNameFormat,mTimeTaken);
		final String fpath = Storage.generateMpoFilepath(name);
		mParameters.setCapturePath(fpath);		
		}

    // Preview area is touched. Handle touch focus.
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (mPausing || mCameraDevice == null || !mFirstTimeInitialized
                || mCameraState == SNAPSHOT_IN_PROGRESS) {
            return false;
	}

        // Check if metering area or focus area is supported.
        if (!mFocusAreaSupported && !mMeteringAreaSupported) return false;

        return mFocusManager.onTouch(e);
    }	

    public boolean capture() {
		Log.d(TAG, "capture");		
        // If we are already in the middle of taking a snapshot then ignore.
        if (mCameraState == SNAPSHOT_IN_PROGRESS || mCameraDevice == null) {
            return false;
        }

        // Set rotation and gps data.
        Util.setRotationParameter(mParameters, mCameraId, mOrientation);
        Location loc = mLocationManager.getCurrentLocation();
        Util.setGpsParameters(mParameters, loc);
		//set path
		setCapturePath();
        mCameraDevice.setParameters(mParameters);		
		
		if (!mMavController.start()) {//it is still busy.
            return false;
		}		

        showPostControlAlert();
        mShutterButton.setEnabled(false);
        keepScreenOnAwhile();
        setCameraState(SNAPSHOT_IN_PROGRESS);
		mHandler.postDelayed(mFalseShutterCallback, 300);
		return true;
    }

    private void stopCapture(boolean isMerge) {
		Log.d(TAG, "stopCapture isMerge = "+isMerge);

		resetCapture(!isMerge);
		//Mav can only merge when it get enough number of images (15 at present) 
		//so we can only set false here. 
		mMavController.stop(false);
    }

    private void resetCapture(boolean finish) {
		Log.d(TAG, "resetCapture finish = "+finish);
		//if we need to wait for merge,unlockAeAwb must be called after we receive the last callback.
		//so if isMerge = true,we will do it later in onCaptureDone.		
		if (mCameraState == SNAPSHOT_IN_PROGRESS && finish) {
			unlockAeAwb();	
        	setCameraState(IDLE);
		}
		setOrientationIndicator(mOrientationCompensation);				
		mShutterButton.setEnabled(true);
        keepScreenOnAwhile();
    }

    private void setCameraState(int state) {
        mCameraState = state;
        switch (state) {
            case SNAPSHOT_IN_PROGRESS:
            case FOCUSING:
                enableCameraControls(false);
                break;
            case IDLE:
            case PREVIEW_STOPPED:
                enableCameraControls(true);
                break;
        }
    }	
    private void enableCameraControls(boolean enable) {
        if (mModePicker != null) mModePicker.setEnabled(enable);
        if (mThumbnailView != null) mThumbnailView.setEnabled(enable);
        if (mZoomControl != null) mZoomControl.setEnabled(enable);
    }

	private long pictureSize() {
        return Storage.getSize("mav");
	}
	
    private void checkStorage() {
        mPicturesRemaining = Storage.getAvailableSpace();
        if (mPicturesRemaining > Storage.LOW_STORAGE_THRESHOLD) {
            mPicturesRemaining = (mPicturesRemaining - Storage.LOW_STORAGE_THRESHOLD)
                    / pictureSize();
        } else if (mPicturesRemaining > 0) {
            mPicturesRemaining = 0;
        }

		if (mPicturesRemaining < 0) {
			mRemainPictureView.setText("0");
		} else {
			mRemainPictureView.setText(String.valueOf(mPicturesRemaining));
		}

		updateStorageHint();
    }

    private void createContentView() {
        setContentView(R.layout.mtk_panorama);

        mFullLayout = (View) findViewById(R.id.full_layout);
        mThumbnailView = (RotateImageView) findViewById(R.id.thumbnail);
        mThumbnailView.enableFilter(false);

        mModePicker = (ModePicker) findViewById(R.id.mode_picker);
        mModePicker.setVisibility(View.VISIBLE);
        mModePicker.setOnModeChangeListener(this);
        mModePicker.setCurrentMode(ModePicker.MODE_MAV);

        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setEnabled(true);
        mShutterButton.setBackgroundResource(R.drawable.btn_shutter_pan);
        mShutterButton.setOnShutterButtonListener(this);

    	mOnScreenDisplayLayout = (RotateLayout) findViewById(R.id.on_screen_display);
    	mOnScreenProgress = (RotateLayout) findViewById(R.id.on_screen_progress);
    	mRemainPictureView = (TextView) findViewById(R.id.remain_pictures);
        mFocusAreaIndicator = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);		
        mRotateDialog = new RotateDialogController(this, R.layout.rotate_dialog,(ViewGroup)findViewById(R.id.frame));

        mCancelButton = (RotateImageView) findViewById(R.id.btn_cancel_capture);

		mZoomControl = (ZoomControl) findViewById(R.id.zoom_control);
		initThumbnailButton();
        if (CameraHolder.instance().getStereo3DSupport()) {
            mRemainPictureView.setVisibility(View.GONE);
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

	private void onZoomValueChanged(int index) {
		// Not useful to change zoom value when the activity is paused.
		if (mPausing)
			return;

		Log.i(TAG, "Set zoom value to Camera Device");
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
			updateCameraParametersZoom();
			mCameraDevice.setParameters(mParameters);
		}
	}

	private void initializeZoom() {
		// Get the parameter to make sure we have the up-to-date zoom value.
		mParameters = mCameraDevice.getParameters();
		if (!initializeZoomMax(mParameters))
			return;
		mZoomControl.setZoomIndex(mParameters.getZoom());
		mZoomControl.setSmoothZoomSupported(false);
		mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
		mCameraDevice.setZoomChangeListener(mZoomListener);
	}	

    @Override
    public void onShutterButtonClick() {
    	//tricky here, just hide modepicker.
		if (mModePicker != null) {
			mModePicker.onOtherPopupShowed();
		}

        // If mSurfaceTexture == null then GL setup is not finished yet.
        // No buttons can be pressed.
        if (!mFirstTimeInitialized || mPausing || mSurfaceHolder == null || 
				mPicturesRemaining <= 0 || mFocusManager.isFocusingSnapOnFinish() ) {
			return;
        }
        // Since this button will stay on the screen when capturing, we need to check the state
        // right now.
        switch (mCameraState) {
            case IDLE:
	    case FOCUSING:
                mCameraSound.playSound(CameraSound.START_VIDEO_RECORDING);
				//it seems that call startCapture immediately will postpone the output of the sound.
				//in order to let the sound output first,sleep some time and then startCapture.				
				/*try {					
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					//ignore
				}*/
                mFocusManager.doSnap();
                break;
            case SNAPSHOT_IN_PROGRESS:
				mCameraSound.playSound(CameraSound.STOP_VIDEO_RECORDING);
                stopCapture(true);
				break;
			default:
				break;
        }
    }

    private boolean canTakePicture() {
        return isCameraIdle() && (mPicturesRemaining > 0);
    }	

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        if (mPausing || mCameraState == SNAPSHOT_IN_PROGRESS) return;

        // Do not do focus if there is not enough storage.
        if (pressed && !canTakePicture()) return;

        if (pressed) {
            mFocusManager.onShutterDown();
        } else {
            mFocusManager.onShutterUp();
        }
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



    @OnClickAttr
    public void onThumbnailClicked(View v) {
        if (mPausing || mSurfaceHolder== null) return;
        showSharePopup();
    }

    private void showSharePopup() {
        if (mThumbnail == null) return;
        Uri uri = mThumbnail.getUri();
        if (mSharePopup == null || !uri.equals(mSharePopup.getUri())) {
            // The orientation compensation is set to 0 here because we only support landscape.
            mSharePopup = new SharePopup(this, uri, mThumbnail.getBitmap(),
                    mOrientationCompensation,
                    mPreviewFrameLayout);
        }
        mSharePopup.setOrientation(mOrientationCompensation);
        mSharePopup.showAtLocation(mThumbnailView, Gravity.NO_GRAVITY, 0, 0);
    }

    private static String createName(String format, long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    private static String getExifOrientation(int orientation) {
        switch (orientation) {
            case 0:
                return String.valueOf(ExifInterface.ORIENTATION_NORMAL);
            case 90:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_90);
            case 180:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_180);
            case 270:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_270);
            default:
                throw new AssertionError("invalid: " + orientation);
            }
    }

	//do the stop sequence carefully in order not to cause driver crash.
	private void safeStop(){
		//maybe stop capture(stopAUTORAMA or stopMAV) is ongoing,then it is not allowed to stopPreview. 
		CameraHolder holder = CameraHolder.instance();
		Log.d(TAG, "check stopAsync thread state, if running,we must wait");
		mMavController.checkStopProcess();
		synchronized(holder) {
			stopPreview();
		}
		//By doing closeCamera before stopCapture
        //we could skip doing stop capture(stopAUTORAMA or stopMAV)  later. 		
		closeCamera();	
        //Note: mCameraState will be changed in stopPreview and closeCamera
		stopCapture(false);
	}


    @Override
    protected void onPause() {
		Log.d(TAG, "onPause");
        super.onPause();

        mPausing = true;
		
		safeStop();
		
		mRotateDialog.dismissDialog();
		
        if (mSharePopup != null) mSharePopup.dismiss();
        hidePostControlAlert();

        mHandler.removeCallbacks(mUpdateHintRunnable);
        if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }

        if (mLocationManager != null) mLocationManager.recordLocation(false);

        resetScreenOn();
        mOrientationEventListener.disable();		
        mCameraSound.release();
        mFocusManager.removeMessages();		
    }

    @Override
    protected void doOnResume() {
		Log.d(TAG, "doOnResume");
		
        mPausing = false;

		Storage.updateDefaultDirectory(this, true);
		
        if (mCameraState == PREVIEW_STOPPED) {
	        try {
	        	setupCamera();
				startPreview();
				Log.d(TAG, "setupCamera after startPreview");
	        } catch (CameraHardwareException e) {
	            Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
	            return;
	        } catch (CameraDisabledException e) {
	            Util.showErrorAndFinish(this, R.string.camera_disabled);
	            return;
	        }
        }
        mHandler.postDelayed(mUpdateHintRunnable, 200);

        keepScreenOnAwhile();
		
        if (mSurfaceHolder != null) {
            // If first time initialization is not finished, put it in the
            // message queue.
            if (!mFirstTimeInitialized) {
                mHandler.sendEmptyMessage(FIRST_TIME_INIT);
            } else {
                initializeSecondTime();
            }
        }
        // Dismiss open menu if exists.
        PopupManager.getInstance(this).notifyShowPopup(null);
    }

    private void closeCamera() {
		
		Log.d(TAG, "closeCamera");
        if (mCameraDevice != null) {
            CameraHolder.instance().release();
            mCameraDevice.setErrorCallback(null);
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
        int orientation = Util.getDisplayOrientation(mDisplayRotation,mCameraId);
        mCameraDevice.setDisplayOrientation(orientation);
    }

    private void startPreview() {
		
		Log.d(TAG, "startPreview mPausing = "+mPausing);
        if (mPausing || isFinishing()) return;
        mFocusManager.resetTouchFocus();
        // If we're previewing already, stop the preview first (this will blank
        // the screen).
        if (mCameraState != PREVIEW_STOPPED) stopPreview();

        mCameraDevice.setErrorCallback(this);
        setPreviewDisplay(mSurfaceHolder);
		setDisplayOrientation();

        mFocusManager.setAeAwbLock(false); // Unlock AE and AWB.
            
        try {
            Log.v(TAG, "startPreview");
            mCameraDevice.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        mFocusManager.onPreviewStarted();		
        setCameraState(IDLE);
    }

    private void stopPreview() {
		Log.d(TAG, "stopPreview mCameraState = "+mCameraState);
        if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
            Log.v(TAG, "stopPreview");
            mCameraDevice.stopPreview();
        }
        mFocusManager.onPreviewStopped();
        setCameraState(PREVIEW_STOPPED);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mCameraState != SNAPSHOT_IN_PROGRESS) keepScreenOnAwhile();
    }

    private void resetScreenOn() {
        mHandler.removeMessages(MSG_CLEAR_SCREEN_DELAY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(MSG_CLEAR_SCREEN_DELAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessageDelayed(MSG_CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }

    private boolean mFirstTimeInitialized;
	private OnScreenHint mStorageHint;

    private void updateStorageHint() {
        String noStorageText = null;

        if (mPicturesRemaining == Storage.UNAVAILABLE) {
            noStorageText = getString(R.string.no_storage);
        } else if (mPicturesRemaining == Storage.PREPARING) {
            noStorageText = getString(R.string.preparing_sd);
        } else if (mPicturesRemaining == Storage.UNKNOWN_SIZE) {
            noStorageText = getString(R.string.access_sd_fail);
        } else if (mPicturesRemaining < 1L) {
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

    @Override
	public void onDestroy(){
		super.onDestroy();
		if (mFirstTimeInitialized) {
			unregisterReceiver(mReceiver);
		} else {
			mHandler.removeMessages(FIRST_TIME_INIT);
		}
    }	

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received intent action=" + action);
			
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Storage.updateDefaultDirectory(MavActivity.this,true);
                checkStorage();
				updateThumbnail();
            } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
				if (Storage.MOUNT_POINT.equals(intent.getData().getPath())) {
					if (mCameraState == SNAPSHOT_IN_PROGRESS) {
						stopCapture(false);
					}
	                Storage.updateDefaultDirectory(MavActivity.this,true);     				
                	checkStorage();
					mThumbnail = null;//hide thumbnail.
                	updateThumbnailButton();
            	}
            } else if (action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                checkStorage();
            }
        }
    };	

    private void initializeFirstTime() {
		Log.d(TAG, "initializeFirstTime");
        if (mFirstTimeInitialized) return;

		mNameFormat = getResources().getString(R.string.pano_file_name_format);
		int orientation = Util.getDisplayOrientation(Util.getDisplayRotation(this),
						mCameraId);
        mFocusManager.initialize(mFocusAreaIndicator, mSurfaceView, null, this,
                false, orientation);

		initOnScreenIndicator();
		updateWhiteBalanceOnScreenIndicator(mParameters.getWhiteBalance());		
		
		initializeZoom();
        // Initialize location sevice.
        mLocationManager = new LocationManager(this, this);		        
        boolean recordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());
        mLocationManager.recordLocation(recordLocation);	
		
		mOrientationEventListener.enable();
        checkStorage();
		installIntentFilter();
        mFirstTimeInitialized = true;		
    }

    // If the activity is paused and resumed, this method will be called in
    // onResume.
    private void initializeSecondTime() {
        // Start orientation listener as soon as possible because it takes
        // some time to get first orientation.
		mOrientationEventListener.enable();

        // Start location update if needed.
        boolean recordLocation = RecordLocationPreference.get(
                mPreferences, getContentResolver());
        mLocationManager.recordLocation(recordLocation);

        updateThumbnail();
        checkStorage();
    }		

    private void installIntentFilter() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
    }

	private void showCaptureError(){
	    mRotateDialog.dismissDialog();
	    if (!mPausing) {
	    	//TBD: replace with MAV specific strings.
	    	final String dialogTitle = getResources().getString(R.string.camera_label);
	    	final String dialogOk = getResources().getString(R.string.dialog_ok);
	    	final String dialogPanoramaFailedString =
	    			getResources().getString(R.string.mav_dialog_save_failed);							
	    	mRotateDialog.showAlertDialog(
	    			dialogTitle, dialogPanoramaFailedString,
	    			dialogOk, null,null, null);
	    }
	}

	public void onError(int error, android.hardware.Camera camera) {		
        if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
        	throw new RuntimeException("Media server died.");
        } else if (error == android.hardware.Camera.CAMERA_ERROR_NO_MEMORY) {
            Util.showErrorAndFinish(this, R.string.capture_memory_not_enough);
        } else if (error == android.hardware.Camera.CAMERA_ERROR_RESET) {
			if (mCameraState == SNAPSHOT_IN_PROGRESS) {
				showCaptureError();
				stopCapture(false);
			}
		}
	}


    @Override
    public void onBackPressed() {
        if (!isCameraIdle()) {
            // ignore backs while we're taking a picture
            return;
        } 
		super.onBackPressed();
    }		

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                	onShutterButtonFocus(true);
                }
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
					if (mCameraState != SNAPSHOT_IN_PROGRESS) {
                		onShutterButtonClick();
					}
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // If we get a dpad center event without any focused view, move
                // the focus to the shutter button and press it.
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
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

	private class ZoomChangeListener implements 
		ZoomControl.OnZoomChangedListener {
		// only for immediate zoom
		@Override
		public void onZoomValueChanged(int index) {
			MavActivity.this.onZoomValueChanged(index);
		}

		// only for smooth zoom
		@Override
		public void onZoomStateChanged(int state) {
			if (mPausing)
				return;
		
			Log.v(TAG, "zoom picker state =" + state);
			if (state == ZoomControl.ZOOM_IN) {
				MavActivity.this.onZoomValueChanged(mZoomMax);
			} else if (state == ZoomControl.ZOOM_OUT) {
				MavActivity.this.onZoomValueChanged(0);
			} else {
				mTargetZoomValue = -1;
				if (mZoomState == ZOOM_START) {
					mZoomState = ZOOM_STOPPING;
					mCameraDevice.stopSmoothZoom();
				}
			}
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Only show the menu when camera is idle.
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(isCameraIdle());
        }
        return true;
    }	

    private boolean isCameraIdle() {
        return (mCameraState == IDLE) || (mFocusManager.isFocusCompleted());
    }  
	
    private long mFocusStartTime;
    private final AutoFocusCallback mAutoFocusCallback =
            new AutoFocusCallback();
    private final class AutoFocusCallback
            implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(
                boolean focused, android.hardware.Camera camera) {
            if (mPausing) return;

            Log.v(TAG, "mAutoFocusTime = " + (System.currentTimeMillis() - mFocusStartTime) + "ms");
            mFocusManager.onAutoFocus(focused);
        }
    }	

    public void autoFocus() {
        mFocusStartTime = System.currentTimeMillis();
        mCameraDevice.autoFocus(mAutoFocusCallback);
    }

    public void cancelAutoFocus() {
        mCameraDevice.cancelAutoFocus();
        setFocusParameters();
    }
	
    public void playSound(int soundId) {
        mCameraSound.playSound(soundId);
    }

    public void startFaceDetection(){}
    public void stopFaceDetection(){}	
    public boolean readyToCapture() {return true;}
    public boolean doSmileShutter() {return false;}

    @OnClickAttr
    public void onCaptureCancelClicked(View v) {
        Log.i(TAG, "onCaptureCancelClicked");
        mMavController.mCancelOnClickListener.onClick(null);
        hidePostControlAlert();
    }

    private void showPostControlAlert() {
        Util.fadeOut(mShutterButton, mThumbnailView, mModePicker,
                mZoomControl, mRemainPictureView);
        if (mFocusAreaIndicator != null) mFocusAreaIndicator.setVisibility(View.GONE);
        mShutterButton.setEnabled(false);

        Util.fadeIn(mAlertControlBar);
        mAlertControlBar.setEnabled(true);
        if (mFocusAreaIndicator != null) mFocusAreaIndicator.setVisibility(View.VISIBLE);
        mFocusManager.updateFocusUI();
        mCancelButton.setEnabled(true);
    }

    public void hidePostControlAlert() {
        Util.fadeOut(mAlertControlBar);
        mAlertControlBar.setEnabled(false);

        Util.fadeIn(mShutterButton, mThumbnailView, mModePicker,
                mZoomControl, mRemainPictureView);
        mRotateTextToast.changeTextContent(R.string.toast_mav_how_to_capture);
        mShutterButton.setEnabled(true);
    }

    public void onKeyPressed(boolean ok) {
        Log.d(TAG, "onKeyPressed ok = "+ok);        
        if (mCameraState == SNAPSHOT_IN_PROGRESS) {
            stopCapture(ok);
        }
    }
}
