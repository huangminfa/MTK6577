package com.android.camera;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.camera.ui.ControlBarIndicatorButton;
import com.android.camera.ui.IndicatorControlContainer;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.ZoomControl;
import com.android.camera.CameraSettings;
import com.android.camera.ShutterButton.OnShutterButtonListener;
import com.android.camera.ShutterButton.OnShutterButtonLongPressListener;

import com.android.camera.R;
import com.mediatek.camera.ui.ProgressIndicator;
import com.mediatek.xlog.Xlog;

import android.hardware.Camera.ASDCallback;
import android.hardware.Camera.AUTORAMACallback;
import android.hardware.Camera.AUTORAMAMVCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Camera.SmileCallback;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import static com.android.camera.Camera.IDLE;
import static com.android.camera.Camera.SAVING_PICTURES;
import static com.android.camera.Camera.PICTURES_SAVING_DONE;
import static com.android.camera.Camera.UPDATE_STORAGE;

public abstract class ModeActor 
		implements SelfTimerManager.SelfTimerListener, 
		OnShutterButtonListener, OnShutterButtonLongPressListener {
	protected static final String TAG = "ModeActor";
	protected static final boolean LOG = true;
	private static final boolean SKIP_FOCUS_ON_CAPTURE = true;

	protected final String mModeName;

	private SelfTimerManager mSelftimerManager;

	// from camera.java
	protected com.android.camera.Camera mCamera;
	protected android.hardware.Camera mCameraDevice;
	protected android.hardware.Camera.Parameters mParameters;
	protected ComboPreferences mPreferences;
	protected PreferenceGroup mPreferenceGroup;
	protected FocusManager mFocusManager;
	protected Thumbnail mThumbnail;

	protected final boolean mIsImageCaptureIntent;
	protected final Handler mHandler;
	protected boolean mPausing;

	protected Location mLastJpegLoc;// for EV bracket shot. should be review by
									// chongliang
	protected Uri mLastUri;
	private RotateDialogController mSavingDlg;

	private long mTimeSelfTimerStart;

	// should not equal with com.android.camera.Camera.MainHandler's message id.
	static final int EV_SELECT = 9;
	static final int MSG_FIRE_EV_SELECTOR = 10;
	static final int MSG_EV_SAVING_DONE = 13;
	static final int MSG_SHOW_SAVING_HINT = 14;
	static final int MSG_BURST_SAVING_DONE = 15;
	static final int MSG_CHANGE_HUD_STATE = 18;
	static final int MSG_EV_SEL_DONE = 19;

	private class ModeActorHandler extends Handler {
		public ModeActorHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "ModeActorHandler msg = " + msg.what);
			switch (msg.what) {
			case MSG_EV_SEL_DONE:
				new Thread() {
					public void run() {
						saveEVPictures();
					}
				}.start();
				break;
			case MSG_EV_SAVING_DONE:
			case MSG_BURST_SAVING_DONE:
				updateSavingHint(false);
				if (mThumbnail != null) {
					mCamera.updateThumbnailButton(mThumbnail);
				}
				if (mCamera.supportSingle3dSwitch()) {
					mCamera.mRemainPictureView.setVisibility(View.GONE);
				}
				onBurstSaveDone();
				break;
			}
		}
	};

	public ModeActor(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		mCamera = camera;
		mCameraDevice = cameraDevice;
		mParameters = parameters;
		mPreferenceGroup = preferenceGroup;

		mPreferences = preferences;
		mHandler = new ModeActorHandler(mCamera.getMainLooper());
		mIsImageCaptureIntent = mCamera.mIsImageCaptureIntent;
		mFocusManager = mCamera.mFocusManager;
		mSelftimerManager = mCamera.mSelftimerManager;
		mSelftimerManager.setTimerListener(this);
		mCamera.mShutterButton.setOnShutterButtonListener(mCamera);
		mCamera.mShutterButton.setOnShutterButtonLongPressListener(this);

		mModeName = modeName;
	}

	public void updateMembers(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences) {
		mCamera = camera;
		mCameraDevice = cameraDevice;
		mParameters = parameters;
		mPreferenceGroup = preferenceGroup;
		mPreferences = preferences;
	}

	public void saveEVPictures() {
		// TODO Auto-generated method stub
	}

	public void saveBulkPictures(String[] filenames, Location loc) {
		Size size = mParameters.getPictureSize();
		long dateTaken = System.currentTimeMillis();
		String title = Util.createJpegName(dateTaken);

		int count = 0;
		Uri uri = null;
		String filepath = null;
		int orientation = 0;
		for (String oldfile : filenames) {
			if (oldfile != null) {
				String saveName = title + ("_" + String.valueOf(count));
				count++;
				filepath = Storage.generateFilepath(saveName);

				File fOld = new File(oldfile);
				File fNew = new File(filepath);
				fOld.renameTo(fNew);

				ExifInterface exif = null;
				try {
					exif = new ExifInterface(filepath);
				} catch (IOException ex) {
					Log.e(TAG, "cannot read exif", ex);
				}

				orientation = Util.getExifOrientation(exif);
				uri = Storage.addImage(mCamera.getContentResolver(), saveName,
						dateTaken, loc, orientation, size.width, size.height,
						Storage.PICTURE_TYPE_JPG);
			}
		}
		if (uri != null) {
			PreviewFrameLayout layout = mCamera.getPreviewFrameLayout();
			int widthRatio = (int) Math.ceil((double) size.width
					/ layout.getWidth());
			int heightRatio = (int) Math.ceil((double) size.height
					/ layout.getHeight());
			int sampleSize = Integer.highestOneBit(Math.max(widthRatio,
					heightRatio));
			mThumbnail = Thumbnail.createThumbnail(filepath, orientation,
					sampleSize, uri);
			Util.broadcastNewPicture(mCamera, uri);
		}
	}

	public void updateCaptureModeButton(ControlBarIndicatorButton buttonn,
			boolean enable) {
		if (buttonn != null) {
			buttonn.setEnabled(enable);
		}
	}

	public void updateZoomControl(ZoomControl zoomControl, boolean enable) {
		if (zoomControl != null) {
			zoomControl.setEnabled(enable);
		}
	}

	protected void setCaptureMode() {
		mParameters.setCaptureMode(mModeName);
	}

	public boolean isSupported(String value, List<String> supported) {
		return supported != null && supported.indexOf(value) >= 0;
	}

	protected void setMtkCameraParameters(String sceneMode, Size prePictureSize) {
		mParameters = mCamera.getCameraParameters();
		mParameters.setCameraMode(Parameters.CAMERA_MODE_MTK_PRV);
		setCaptureMode();
		String isoSpeed = null;
		if (Parameters.SCENE_MODE_AUTO.equals(sceneMode)) {
			setScenemodeSettings();
			// iso is one item related to scene mode, as iso will effect
			// picture size, so set it out of setScenemodeSettings()
			// iso
			isoSpeed = mPreferences.getString(CameraSettings.KEY_ISO,
					getString(R.string.pref_camera_iso_default));
			if (isSupported(isoSpeed, mParameters.getSupportedISOSpeed())) {
				mParameters.setISOSpeed(isoSpeed);
			}
		} else {
			// keep scene related setting get from driver.
		}

		String colorEffect = mPreferences.getString(
				CameraSettings.KEY_COLOR_EFFECT,
				getString(R.string.pref_camera_coloreffect_default));
		if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
			mParameters.setColorEffect(colorEffect);
		}

		String antibanding = mPreferences.getString(
				CameraSettings.KEY_ANTI_BANDING,
				getString(R.string.pref_camera_antibanding_default));
		if (isSupported(antibanding, mParameters.getSupportedAntibanding())) {
			mParameters.setAntibanding(antibanding);
		}

		// force set parameter request by capture mode.
		setCaptureModeSettings();

		if (mParameters.isZSDSupported()) {
			String zsdPref = mPreferences.getString("pref_camera_zsd_key", 
					getString(R.string.pref_camera_zsd_default));
			mParameters.setZSDMode(zsdPref);
		}
		// use actual camera ISO value instead.
		isoSpeed = mParameters.getISOSpeed();
		int camOri = getCamOri(mCamera.getCameraId());
		if (isoSpeed == null) {
			isoSpeed = getString(R.string.pref_camera_iso_default);
		}
		if (isoSpeed.equals(CameraSettings.ISO_SPEED_1600)
				|| isoSpeed.equals(CameraSettings.ISO_SPEED_800)) {
			List<Size> supported = mParameters.getSupportedPictureSizes();
			CameraSettings.setCameraPictureSize(
					CameraSettings.IMG_SIZE_FOR_HIGH_ISO, supported,
					mParameters, camOri);

			mCamera.overrideSettings(CameraSettings.KEY_PICTURE_SIZE,
					CameraSettings.IMG_SIZE_FOR_HIGH_ISO);
		} else {
			mCamera.overrideSettings(CameraSettings.KEY_PICTURE_SIZE, null);
		}

		Size size = mParameters.getPictureSize();
		if (prePictureSize.width != size.width) {
			mCamera.mHandler.sendEmptyMessage(UPDATE_STORAGE);
		}
	}

	private void setScenemodeSettings() {
		// AE
		String exposureMeter = mPreferences.getString(
				CameraSettings.KEY_EXPOSURE_METER,
				getString(R.string.pref_camera_exposuremeter_default));
		if (isSupported(exposureMeter, mParameters.getSupportedExposureMeter())) {
			mParameters.setExposureMeter(exposureMeter);
		}

		// AF
		// touch focus for default, should set movespot?
		String focusMeter = CameraSettings.FOCUS_METER_SPOT;
		if (isSupported(focusMeter, mParameters.getSupportedFocusMeter())) {
			if (LOG) Xlog.i(TAG, "setSceneModeSetting, focusMeter: " + focusMeter);
			mParameters.setFocusMeter(focusMeter); // still apply focus meter
													// setting
		}

		// Image adjustment
		String hue = mPreferences.getString(CameraSettings.KEY_HUE,
				getString(R.string.pref_camera_hue_default));
		if (isSupported(hue, mParameters.getSupportedHueMode())) {
			mParameters.setHueMode(hue);
		}
		String brightness = mPreferences.getString(
				CameraSettings.KEY_BRIGHTNESS,
				getString(R.string.pref_camera_brightness_default));
		if (isSupported(brightness, mParameters.getSupportedBrightnessMode())) {
			mParameters.setBrightnessMode(brightness);
		}
		String edge = mPreferences.getString(CameraSettings.KEY_EDGE,
				getString(R.string.pref_camera_edge_default));
		if (isSupported(edge, mParameters.getSupportedEdgeMode())) {
			mParameters.setEdgeMode(edge);
		}
		String saturation = mPreferences.getString(
				CameraSettings.KEY_SATURATION,
				getString(R.string.pref_camera_saturation_default));
		if (isSupported(saturation, mParameters.getSupportedSaturationMode())) {
			mParameters.setSaturationMode(saturation);
		}
		String contrast = mPreferences.getString(CameraSettings.KEY_CONTRAST,
				getString(R.string.pref_camera_edge_default));
		if (isSupported(contrast, mParameters.getSupportedContrastMode())) {
			mParameters.setContrastMode(contrast);
		}
	}

	public PictureCallback getPictureCallback(Location loc) {
		return null;
	}

	public boolean canShot() {
		long picturesRemaining = mCamera.getRemainPictures();
		return picturesRemaining > 0;
	}

	public long pictureSize() {
		mParameters = mCamera.getCameraParameters();
		Size size = mParameters.getPictureSize();
		String pictureSize = ((size.width > size.height) ? (size.width + "x" + size.height)
				: (size.height + "x" + size.width));
		String pictureFormat = pictureSize + "-" + "superfine";
		return Storage.PICTURE_SIZE_TABLE.get(pictureFormat);
	}

	public int getCamOri(int cameraId) {
		return CameraHolder.instance().getCameraInfo()[cameraId].orientation;
	}

	public boolean checkMode(String newMode) {
		if (newMode == null) {
			throw new RuntimeException("can not set Capture mode = null");
		}
		return newMode.equals(mModeName);
	}

	public boolean readyToCapture() {
		return true;
	}

	public boolean doCancelCapture() {
		return false;
	}

	public boolean doSmileShutter() {
		return false;
	}

	/**
	 * <p>
	 * update setting menu, it may overlay with scene mode settings.
	 * 
	 * @param autoScene
	 */
	public void updateCaptureModeUI(boolean autoScene) {
		if (autoScene) {
			mCamera.overrideSettings(CameraSettings.KEY_WHITE_BALANCE, null,
					CameraSettings.KEY_SCENE_MODE, null);
			// Focus menu cannot be set now.
			// mCamera.overrideSettings(CameraSettings.KEY_FOCUS_MODE, null,
			// null);
		} else {
			mCamera.overrideSettings(CameraSettings.KEY_SCENE_MODE, null);
		}
		overrideSelfTimer(false);
	}

	/**
	 * <p>
	 * update preference setting, it will be call at beginning of
	 * setCameraParameters in Camera. it will change settings due to the capture
	 * mode.
	 */
	public void updateModePreference() {
		// nothing to override in normal case
	}

	/**
	 * <p>
	 * As Scene mode will effect other settings, and more scene mode related
	 * setting is add in Mediatek load, this function will make sure settings is
	 * in right status. It will be called by updateSceneModeUI in Camera.
	 */
	public void updateSceneModeUI(boolean autoScene) {
		mParameters = mCamera.getCameraParameters();
		String isoSpeed = mParameters.getISOSpeed();
		if (!autoScene) {
			mCamera.overrideSettings(
					// CameraSettings.KEY_FOCUS_METER,
					// mParameters.getFocusMeter(),
					CameraSettings.KEY_ISO, isoSpeed,
					CameraSettings.KEY_EXPOSURE_METER,
					mParameters.getExposureMeter(), CameraSettings.KEY_EDGE,
					mParameters.getEdgeMode(), CameraSettings.KEY_SATURATION,
					mParameters.getSaturationMode(),
					CameraSettings.KEY_CONTRAST, mParameters.getContrastMode(),
					CameraSettings.KEY_HUE, mParameters.getHueMode(),
					CameraSettings.KEY_BRIGHTNESS, mParameters.getBrightnessMode());
		} else {
			mCamera.overrideSettings(
					// CameraSettings.KEY_FOCUS_METER, null,
					CameraSettings.KEY_ISO, null,
					CameraSettings.KEY_EXPOSURE_METER, null,
					CameraSettings.KEY_EDGE, null,
					CameraSettings.KEY_SATURATION, null,
					CameraSettings.KEY_CONTRAST, null,
					CameraSettings.KEY_HUE,	null,
					CameraSettings.KEY_BRIGHTNESS, null);
		}
		// picture size
		if (CameraSettings.ISO_SPEED_1600.equals(isoSpeed)
				|| CameraSettings.ISO_SPEED_800.equals(isoSpeed)) {
			mCamera.overrideSettings(CameraSettings.KEY_PICTURE_SIZE,
					CameraSettings.IMG_SIZE_FOR_HIGH_ISO);
		} else {
			mCamera.overrideSettings(CameraSettings.KEY_PICTURE_SIZE, null);
		}
		updateCaptureModeUI(autoScene);
	}

	/**
	 * <p>
	 * Just update settings relate to capture mode to driver.
	 */
	public void setCaptureModeSettings() {
		// nothing happen for normal case.
	}

	public String getCaptureTempPath() {
		return null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		return;
	}

	/**
	 * <p>
	 * onScreen hint for saving
	 */
	public void updateSavingHint(boolean bSaving) {
		if (mPausing) return;
		mSavingDlg = mCamera.mRotateDialog;
		if (bSaving) {
			mSavingDlg.showWaitingDialog(mCamera.getResources().getString(
					R.string.savingImage));
		} else if (mSavingDlg != null) {
			mSavingDlg.dismissDialog();
			mSavingDlg = null;
		}
	}

	public void onPausePre() {
		mPausing = true;
		if (LOG) Log.i(TAG, "onPausePre() mPausing=" + mPausing);

		if (mCamera.isZooming() && mParameters.isSmoothZoomSupported()) {
			if (LOG) Log.i(TAG, "onPause stop smooth zoom!!");
			mCameraDevice.stopSmoothZoom();
		}
		mSelftimerManager.breakTimer();
		if (mCamera.isFinishing()) {
			writePreference(CameraSettings.KEY_SELF_TIMER, "0");
		}
	}

	public void onPause() {
		if (LOG) Log.i(TAG, "onPause() mPausing=" + mPausing);
		mHandler.removeMessages(MSG_EV_SAVING_DONE);

		// remove saving hint since we don't need it anymore
		mHandler.removeMessages(MSG_SHOW_SAVING_HINT);
	}

	public void onResume() {
		if (LOG) Log.i(TAG, "onResume() mPausing=" + mPausing);
		mPausing = false; // default value
		updateSavingHint(false);
		Storage.updateDefaultDirectory(mCamera, true);
	}

	public String getCaptureMode() {
		return mModeName;
	}

	public String getString(int resId) {
		return mCamera.getString(resId);
	}

	public void handleSDcardUnmount() {
	}

	public void writePreference(final String... keyvalues) {
		if (keyvalues.length % 2 != 0) {
			throw new IllegalArgumentException();
		}
		Editor editor = mPreferences.edit();
		String key;
		String value;
		for (int i = 0; i < keyvalues.length; i += 2) {
			key = keyvalues[i];
			value = keyvalues[i + 1];
			editor.putString(key, value);
		}
		editor.apply();
		if (mCamera.mIndicatorControlContainer != null) {
			mCamera.mIndicatorControlContainer.reloadPreferences();
		}
	}

	public int computeRemaining(int remaining) {
		if (remaining > 0) {
			remaining = (remaining > 2) ? (remaining - 2) : 0;
		}
		return remaining;
	}

	public void ensureCaptureTempPath() {
		mParameters = mCamera.getCameraParameters();
		String capPath = getCaptureTempPath();
		// smile and normal have no path.
		if (capPath != null) {
			mParameters.setCapturePath(capPath);
		}
	}

	public int getPictureBytes(String PictureSizeQuality) {
		int pictureBytes = Storage.PICTURE_SIZE_TABLE.get(PictureSizeQuality);
		return pictureBytes;
	}

	public void overrideSelfTimer(boolean override) {
		String selfTimer = null;
		if (override) {
			selfTimer = CameraSettings.SELF_TIMER_OFF;
		}
		mCamera.overrideSettings(CameraSettings.KEY_SELF_TIMER, selfTimer);
		if (selfTimer == null) {
			selfTimer = mPreferences.getString(CameraSettings.KEY_SELF_TIMER,
					CameraSettings.SELF_TIMER_OFF);
		}
		mSelftimerManager.setSelfTimerMode(selfTimer);
	}

	protected String createName(long dateTaken) {
		Date date = new Date(dateTaken);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				getString(R.string.image_file_name_format));

		return dateFormat.format(date);
	}

	public boolean checkSelfTimerMode() {
		return mSelftimerManager.checkSelfTimerMode();
	}

	public boolean isSelfTimerEnabled() {
		return mSelftimerManager.isSelfTimerEnabled();
	}

	public boolean isSelfTimerCounting() {
		return mSelftimerManager.isSelfTimerCounting();
	}

	public void breakTimer() {
		mSelftimerManager.breakTimer();
		mCamera.setCameraState(IDLE);
	}

	public void onTimerTimeout() {
		if (!mPausing) {
			//mFocusManager.onShutterDown();
			mFocusManager.doSnap();
			//mFocusManager.onShutterUp();
		}
	}

	/**
	 * Selftimer callback
	 */
	public void onTimerStart() {
		// ...
	}

	/**
	 * Selftimer callback
	 */
	public void onTimerStop() {
		// ...
	}

	/**
	 * <p>
	 * reset some ui to normal state when leaving the capture mode.
	 */
	public void restoreModeUI(boolean preferenceRestored) {
		//
	}

	public void onShutter() {
		//
	}

	public void updateCaptureModeIndicator() {
	}

	/**
	 * <p>
	 * Save extra data when picture is captured.
	 */
	public void onPictureTaken(Location location, int width, int height) {
	}

	public boolean isBurstShotInternal() {
		return false;
	}

	public boolean applySpecialCapture() {
		return false;
	}

	public void setDisplayOrientation(int orientation) {
		//
	}

	public boolean skipFocus() {
		return SKIP_FOCUS_ON_CAPTURE;
	}

	public boolean enableFD(int cameraId) {
		CameraInfo info = CameraHolder.instance().getCameraInfo()[cameraId];
		return info.facing != CameraInfo.CAMERA_FACING_FRONT;
	}

	public void setOrientation(int orientation) {

	}

	public void lowStorage() {
		// Reserve for continuous shoot
	}

	public ShutterButton.OnShutterButtonListener getShutterButtonListener() {
		return mCamera;
	}

	public void onBurstSaveDone() {
		//
	}


	@Override
	public void onShutterButtonFocus(boolean pressed) {
		//
	}

	@Override
	public void onShutterButtonClick() {
		mCamera.onShutterButtonClick();
	}

	@Override
	public void onShutterButtonLongPressed() {
		mPreferenceGroup = mCamera.getPreferenceGroup();
		ListPreference pref = mPreferenceGroup.findPreference(CameraSettings.KEY_NORMAL_CAPTURE_KEY);
		if (pref == null) return;
		String showing = pref.getEntry() + mCamera.getString(R.string.camera_continuous_not_supported);
		new RotateTextToast(mCamera, showing, mCamera.getOrientation()).show();
	}

	public boolean updateThumbnailInSaver(int queueSize, int sizeRatio) {
		return queueSize <= sizeRatio;
	}

    public void checkStopProcess() {}
}

class ActorNormal extends ModeActor {

	public ActorNormal(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
	}

	public void updateModePreference() {
		mCameraDevice.setASDCallback(null);
	}

	@Override
	public int computeRemaining(int remaining) {
		return remaining;
	}

}

class ActorBest extends ModeActor {
	public ActorBest(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
	}

	public void updateModePreference() {
		mCameraDevice.setASDCallback(null);
	}
}

class ActorEv extends ModeActor {
	private boolean mIsEvMutiCallBack = true;
	private String[] mEVPaths = new String[MAX_EV_NUM];

	private int mCurrentEVNum = 0;
	private String mRestoreSceneMode;
	protected static final int MAX_EV_NUM = 3;
	private static final int SLEEP_TIME_FOR_SHUTTER_SOUND = 140;

	protected String mEVPrefix = "/cap0";
	protected static final String INTENT_EV_IMG_PREFIX = "/icp0";
	protected String[] mEvImageSelected = new String[MAX_EV_NUM];

	public ActorEv(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
		mRestoreSceneMode = preferences.getString(
				CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO);
	}

	public void updateModePreference() {
		if (!mIsImageCaptureIntent && !mPausing) {
			writeEVPreferenceSettings();
		}
		mCameraDevice.setASDCallback(null);
	}

	@Override
	public void setCaptureModeSettings() {
		mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
	}

	@Override
	public void updateCaptureModeUI(boolean autoScene) {
		mCamera.overrideSettings(
				CameraSettings.KEY_SCENE_MODE, mParameters.getSceneMode());
		overrideSelfTimer(false);
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		if (!preferenceRestored) {
			restoreEVPreferenceSettings();
		}
		mCamera.overrideSettings(CameraSettings.KEY_SCENE_MODE, null);
	}

	private void writeEVPreferenceSettings() {
		writePreference(CameraSettings.KEY_SCENE_MODE,
				Parameters.SCENE_MODE_AUTO);
	}

	private void restoreEVPreferenceSettings() {
		writePreference(CameraSettings.KEY_SCENE_MODE, mRestoreSceneMode);
	}

	@Override
	public String getCaptureTempPath() {
		String capPath = null;
		if (mIsImageCaptureIntent) {
			capPath = Storage.DIRECTORY + "/" + INTENT_EV_IMG_PREFIX + "0";
		} else {
			capPath = Storage.DIRECTORY + "/" + mEVPrefix + "0";
		}
		return capPath;
	}

	@Override
	public PictureCallback getPictureCallback(Location loc) {
		if (true == mIsEvMutiCallBack) {
			return new EVJpegPictureMultiCallback(loc);
		} else {
			return new EVJpegPictureCallback(loc);
		}
	}

	class EVJpegPictureCallback implements PictureCallback {
		public EVJpegPictureCallback(Location loc) {
			mLastJpegLoc = loc;
		}

		public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
			fireEvSelector();
		}
	}

	class EVJpegPictureMultiCallback implements PictureCallback {
		public EVJpegPictureMultiCallback(Location loc) {
			mLastJpegLoc = loc;
		}

		public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
			mCurrentEVNum++;
			saveEvPictureForMultiCallBack(data, mCurrentEVNum, mLastJpegLoc);
			if (MAX_EV_NUM == mCurrentEVNum) {
				multiCallBackfireEvSelector();
				mCurrentEVNum = 0;
			}
		}
	}

	private void fireEvSelector() {
		if (mPausing) {
			return;
		}
		Intent picImgIntent = new Intent(mCamera, PicturePicker.class);
		Bundle param = new Bundle();
		long t = System.currentTimeMillis();
		String newPrefix = Storage.DIRECTORY + "/" + createName(t);
		String evPrefix = Storage.DIRECTORY
				+ (mIsImageCaptureIntent ? INTENT_EV_IMG_PREFIX : mEVPrefix);
		String[] paths = new String[MAX_EV_NUM];
		for (int i = 0; i < MAX_EV_NUM; i++) {
			// rename file to prevent multiple entry of camera cause ev temp
			// file is
			// overwritten
			File f = new File(evPrefix + i);
			String newName = new String(newPrefix + i);
			File fNew = new File(newName);
			f.renameTo(fNew);
			paths[i] = newName;
		}
		param.putStringArray(PicturePicker.FILE_PATHS, paths);

		if (mIsImageCaptureIntent) {
			param.putInt(PicturePicker.PICTURES_TO_PICK, 1);
			// align intent capture behavior
			picImgIntent.setFlags(picImgIntent.getFlags()
					| Intent.FLAG_ACTIVITY_NO_HISTORY);
		}
		picImgIntent.putExtras(param);

		try {
			Log.e(TAG, "sleep 140ms for shuttersound");
			Thread.sleep(SLEEP_TIME_FOR_SHUTTER_SOUND); // sleep 140ms for shuttersound played out
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mCamera.startActivityForResult(picImgIntent, EV_SELECT);
		mCamera.setCameraState(IDLE);
	}

	protected void saveEvPictureForMultiCallBack(byte[] data, int count,
			Location loc) {
		if (mPausing) {
			return;
		}
		long dateTaken = System.currentTimeMillis();
		String fileName = Util.createJpegName(dateTaken);
		String filePath = new String(Storage.DIRECTORY + "/" + fileName + ".jpg");
		mEVPaths[count - 1] = filePath;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			out.write(data);
		} catch (Exception e) {
			Log.e(TAG, "Failed to write image", e);
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	private void multiCallBackfireEvSelector() {
		if (mPausing) {
			return;
		}
		Intent picImgIntent = new Intent(mCamera, PicturePicker.class);
		Bundle param = new Bundle();

		param.putStringArray(PicturePicker.FILE_PATHS, mEVPaths);

		if (mIsImageCaptureIntent) {
			param.putInt(PicturePicker.PICTURES_TO_PICK, 1);
			// align intent capture behavior
			picImgIntent.setFlags(picImgIntent.getFlags()
					| Intent.FLAG_ACTIVITY_NO_HISTORY);
		}
		picImgIntent.putExtras(param);

		try {
			Log.e(TAG, "sleep 140ms for shuttersound");
			Thread.sleep(SLEEP_TIME_FOR_SHUTTER_SOUND); // sleep 140ms for shuttersound played out
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mCamera.startActivityForResult(picImgIntent, EV_SELECT);
		mCamera.setCameraState(IDLE);
	}

	// called by camera
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (LOG)
			Log.i(TAG, "onActivityResult: requestCode:" + requestCode
					+ " resultCode:" + resultCode);
		if (EV_SELECT == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras == null) {
					Log.e(TAG, "onActivityResult, EV_SELECT, extra == null");
					return;
				}
				mEvImageSelected = extras
						.getStringArray(PicturePicker.FILE_PATHS);
				mHandler.sendEmptyMessage(MSG_EV_SEL_DONE);
				// let handler do the job.
			} else {
				if (mIsEvMutiCallBack) {
					for (String path : mEVPaths) {
						if (path != null) {
							new File(path).delete();
						}
					}
				} else {
					String evPrefix = mIsImageCaptureIntent ? INTENT_EV_IMG_PREFIX
							: mEVPrefix;
					for (int i = 0; i < MAX_EV_NUM; i++) {
						String s = new String(Storage.DIRECTORY + evPrefix + i);
						new File(s).delete();
					}
				}
			}
		}
		if (mIsEvMutiCallBack) {
			for (int i = 0; i < MAX_EV_NUM; i++) {
				mEVPaths[i] = null;
			}
		}
	}

	@Override
	public void saveEVPictures() {
		//TODO Should reuse code about inserting picture to provider.
		Uri uri = null;
		int orientation = 0;
		Size size = mParameters.getPictureSize();
		String lastFilePath = null;
		for (String filePath : mEvImageSelected) {
			if (filePath == null) continue;
			String title = filePath.substring(
					filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));

			File file = new File(filePath);
			if (!file.exists()) continue;

			ExifInterface exif = null;
			try {
				exif = new ExifInterface(filePath);
			} catch (IOException ex) {
				Log.e(TAG, "cannot read exif", ex);
			}

			orientation = Util.getExifOrientation(exif);
			// Insert into MediaStore.
			ContentValues values = new ContentValues(9);
			values.put(ImageColumns.TITLE, title);
			values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
			values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
			values.put(ImageColumns.MIME_TYPE, "image/jpeg");
			values.put(ImageColumns.DATA, filePath);
			values.put(ImageColumns.SIZE, file.length());
			values.put(ImageColumns.ORIENTATION, orientation);
			values.put(ImageColumns.WIDTH, size.width);
			values.put(ImageColumns.HEIGHT, size.height);

			if (mLastJpegLoc != null) {
				values.put(ImageColumns.LATITUDE, mLastJpegLoc.getLatitude());
				values.put(ImageColumns.LONGITUDE, mLastJpegLoc.getLongitude());
			}

			try {
				uri = mCamera.getContentResolver().insert(
						Images.Media.EXTERNAL_CONTENT_URI, values);
				lastFilePath = filePath;
			} catch (Throwable th) {
				// This can happen when the external volume is already mounted, but
				// MediaScanner has not notify MediaProvider to add that volume.
				// The picture is still safe and MediaScanner will find it and
				// insert it into MediaProvider. The only problem is that the user
				// cannot click the thumbnail to review the picture.
				Log.e(TAG, "Failed to write MediaStore" + th);
			}
		}
		for (int i = 0; i < MAX_EV_NUM; i++) {
			mEvImageSelected[i] = null;
		}
		if (uri != null) {
			PreviewFrameLayout layout = mCamera.getPreviewFrameLayout();
			int widthRatio = (int) Math.ceil((double) size.width
					/ layout.getWidth());
			int heightRatio = (int) Math.ceil((double) size.height
					/ layout.getHeight());
			int sampleSize = Integer.highestOneBit(Math.max(widthRatio,
					heightRatio));
			mThumbnail = Thumbnail.createThumbnail(lastFilePath, orientation,
					sampleSize, uri);
			Util.broadcastNewPicture(mCamera, uri);
		}
		mHandler.sendEmptyMessage(MSG_EV_SAVING_DONE);
	}

	@Override
	public boolean canShot() {
		long picturesRemaining = mCamera.getRemainPictures();
		return picturesRemaining >= 3;
	}

	@Override
	public void onPausePre() {
		super.onPausePre();
		if (mIsEvMutiCallBack && mCurrentEVNum > 0) {
			mCurrentEVNum = 0;
			new Scavenger(mEVPaths);
			for (int i = 0; i < MAX_EV_NUM; i++) {
				mEVPaths[i] = null;
			}
		}
	}

	public class Scavenger extends Thread {
		private String[] mAbanbonedImages = new String[MAX_EV_NUM];

		public Scavenger(String[] paths) {
			 for (int i = 0; i < MAX_EV_NUM; i++) {
				 if (paths[i] != null) {
					 mAbanbonedImages[i] = new String(paths[i]);
				 }
			 }
			 start();
		}

		public void run() {
			for (String pic : mAbanbonedImages) {
				if (pic != null) {
					new File(pic).delete();
				}
			}
		}
	}
}

class ActorBurst extends ModeActor {

	private boolean mIsBurstMutiCallBack = true;
	private final int mBurstShotNum;
	private int mCurrentShotsNum = 0;
	private final String mCaptureMode = Parameters.CAPTURE_MODE_BURST_SHOT;
	private String mBurstPrefix = "burst";

	public ActorBurst(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName, int burstNumber) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
		mBurstShotNum = burstNumber;
	}

	@Override
	public String getCaptureTempPath() {
		return Storage.DIRECTORY + "/" + mBurstPrefix + "00";
	}

	@Override
	protected void setCaptureMode() {
		mParameters.setCaptureMode(mCaptureMode);
		mParameters.setBurstShotNum(mBurstShotNum);
	}

	public void updateModePreference() {
		mCameraDevice.setASDCallback(null);
	}

	@Override
	public int computeRemaining(int remaining) {
		return remaining;
	}

	@Override
	public PictureCallback getPictureCallback(Location loc) {
		if (true == mIsBurstMutiCallBack) {
			return new BurstJpegPictureMultiCallback(loc);
		} else {
			return new BurstJpegPictureCallback(loc);
		}
	}

	class BurstJpegPictureCallback implements PictureCallback {
		public BurstJpegPictureCallback(Location loc) {
			mLastJpegLoc = loc;
		}

		public void onPictureTaken(final byte[] data, android.hardware.Camera camera) {
			mCurrentShotsNum = 0;
			updateSavingHint(true);
			mCamera.resumePreview();
			mCamera.setCameraState(SAVING_PICTURES);
			new Thread() {
				@Override
				public void run() {
					String[] fileNames = new String[mBurstShotNum];
					String capPath = Storage.DIRECTORY + "/" + mBurstPrefix;
					for (int i = 0; i < mBurstShotNum; i++) {
						if (i < 10) {
							fileNames[i] = new String(capPath + "0" + i);
						} else {
							fileNames[i] = new String(capPath + i);
						}
						// if (LOG) Log.i(TAG, "Generating filename " + i + ": "
						// + fileNames[i]);
					}
					saveBurstPicture(fileNames, mLastJpegLoc);
					mCamera.mHandler.sendEmptyMessage(PICTURES_SAVING_DONE);
				}
			}.start();
		}
	}

	class BurstJpegPictureMultiCallback implements PictureCallback {
		public BurstJpegPictureMultiCallback(Location loc) {
			mLastJpegLoc = loc;
		}

		public void onPictureTaken(final byte[] data, android.hardware.Camera camera) {

			mCurrentShotsNum++;
			TextView burstTips = mCamera.mRemainPictureView;
			burstTips.setText(mCurrentShotsNum + "/" + mBurstShotNum);

			Size size = mParameters.getPictureSize();
			mCamera.mImageSaver.addImage(data, mLastJpegLoc, size.width,
					size.height, MediaStore.ThreeDimensionColumns.STEREO_TYPE_2D);

			Log.i(TAG, "onPictureTaken: mCurrentShotsNum = "
					+ mCurrentShotsNum	+ ", mBurstShotNum = " + mBurstShotNum);
			if (mCurrentShotsNum >= mBurstShotNum) {
				mCamera.mImageSaver.setPriority(Thread.NORM_PRIORITY);
				mCamera.resumePreview();
				mCamera.setCameraState(SAVING_PICTURES);
				mCurrentShotsNum = 0;
				updateSavingHint(true);
				new Thread() {
					@Override
					public void run() {
						mCamera.mImageSaver.waitDoneInSubThread();
						mHandler.sendEmptyMessage(MSG_BURST_SAVING_DONE);
						mCamera.mHandler.sendEmptyMessage(PICTURES_SAVING_DONE);
					}
				}.start();
			}
		}
	}

	protected void saveBurstPicture(String filenames[], Location loc) {
		saveBulkPictures(filenames, loc);
		mHandler.sendEmptyMessage(MSG_BURST_SAVING_DONE);
	}

	@Override
	public void onShutter() {

    }

	@Override
	public void onPausePre() {
		super.onPausePre();
		mCurrentShotsNum = 0;
	}

	@Override
	public boolean canShot() {
		long picturesRemaining = mCamera.getRemainPictures();
		return picturesRemaining >= mBurstShotNum;
	}

	@Override
	public void ensureCaptureTempPath() {
		//will going to capture
		mCamera.mImageSaver.setPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public boolean isBurstShotInternal() {
		return mCurrentShotsNum > 0 && mCurrentShotsNum < mBurstShotNum;
	}

	@Override
	public void onShutterButtonLongPressed() {
		// Do nothing
	}
}

class ActorSmile extends ModeActor implements View.OnClickListener {

	private final int SMILESHOT_STANDBY = 0;
	private final int SMILESHOT_IN_PROGRESS = 1;

	private final ActorSmileCallback mSmileCallback = new ActorSmileCallback();
	private int mStatus = SMILESHOT_STANDBY;

	private String[] mSmileScenes = mCamera.getResources().getStringArray(
			R.array.pref_camera_scenemode_for_smileshot_entryvalues);

	public ActorSmile(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
	}

	public void updateModePreference() {
		mCameraDevice.setASDCallback(null);
		String sceneMode = mPreferences.getString(CameraSettings.KEY_SCENE_MODE,
				getString(R.string.pref_camera_scenemode_default));
		if (!Util.hasItem(sceneMode, mSmileScenes)) {
			// not supported scene mode in smile shot mode
			writePreference(CameraSettings.KEY_SCENE_MODE, "auto");
		}
	}

	@Override
	public void updateCaptureModeUI(boolean autoScene) {
		String sceneMode = mParameters.getSceneMode();
		mCamera.overrideSettings(CameraSettings.KEY_SCENE_MODE, sceneMode,
				mSmileScenes);

		overrideSelfTimer(true);
	}

	@Override
	public boolean readyToCapture() {
		if (LOG) Log.i(TAG, " readyToCapture? mStatus = " + String.valueOf(mStatus));
		if (mStatus != SMILESHOT_IN_PROGRESS) {
			openSmileShutterMode();
			return false;
		}
		return true;
	}

	@Override
	public boolean doCancelCapture() {
		if (mCameraDevice == null)
			return false;
		if (mStatus == SMILESHOT_IN_PROGRESS) {
			stopSmileDetection();
			return true;
		} else {
			mStatus = SMILESHOT_STANDBY;
			return false;
		}
	}

	@Override
	public void handleSDcardUnmount() {
		if (mCameraDevice == null) return;
		if (mStatus == SMILESHOT_IN_PROGRESS) {
			stopSmileDetection();
		}
		mCameraDevice.setSmileCallback(null);
	}

	@Override
	public void onPausePre() {
		if (mStatus == SMILESHOT_IN_PROGRESS) {
			stopSmileDetection();
		}
		super.onPausePre();
	}

	@Override
	public boolean doSmileShutter() {
		if (LOG) Log.i(TAG, "doSmileShutter mStatus = " + String.valueOf(mStatus));
		if (mStatus == SMILESHOT_IN_PROGRESS) {
			// already in smile shutter mode, capture directly.
			mCamera.actorCapture();
			stopSmileDetection();
			return true;
		}
		return false;
	}

	@Override
	public int computeRemaining(int remaining) {
		return remaining;
	}

	@Override
	public String getCaptureTempPath() {
		return null;
	}

	private void openSmileShutterMode() {
		if (LOG) Log.i(TAG, "openSmileShutterMode ");
		if (mCameraDevice == null) {
			Log.e(TAG, "CameraDevice is null, ignore");
			return;
		}
		mStatus = SMILESHOT_IN_PROGRESS;
		startSmileDetection(mSmileCallback);
	}

	private final class ActorSmileCallback implements
			android.hardware.Camera.SmileCallback {
		public void onSmile() {
			if (LOG) Log.i(TAG, "smile detected, mstat:" + mStatus);
			if (!mPausing) {
				mCamera.actorCapture();
				stopSmileDetection();
			}
		}
	}

	public void startSmileDetection(SmileCallback callback) {
		/*
		 * if (!mIsImageCaptureIntent){
		 * findViewById(R.id.review_thumbnail).setVisibility(View.GONE); View p
		 * = (View) findViewById(R.id.btn_capture_cancel).getParent(); if (p!=
		 * null) { p.setVisibility(View.VISIBLE); }
		 * findViewById(R.id.camera_switch_set).setVisibility(View.INVISIBLE); }
		 */
		ImageView iconSmile = (ImageView) mCamera
				.findViewById(R.id.btn_smile_cancel);
		iconSmile.setOnClickListener(this);
		iconSmile.setVisibility(View.VISIBLE);
		mCameraDevice.setSmileCallback(callback);
		mCameraDevice.startSDPreview();
	}

	public void stopSmileDetection() {
		ImageView iconSmile = (ImageView) mCamera
				.findViewById(R.id.btn_smile_cancel);
		iconSmile.setVisibility(View.GONE);
		mCameraDevice.cancelSDPreview();
		mCamera.checkStorage();
		mStatus = SMILESHOT_STANDBY;
	}

	public void onClick(View v) {
		doCancelCapture();
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		doCancelCapture();
		mCamera.ensureFDState(false);
		mCamera.overrideSettings(CameraSettings.KEY_SCENE_MODE, null, null);
	}

	@Override
	protected void setCaptureMode() {
		super.setCaptureMode();
		mCamera.ensureFDState(true);
	}
}

class ActorHdr extends ModeActor {
	private static final boolean SAVE_ORIGINAL_PICTURE = true;
	private String mOriginalPath;
	private String mRestoreSceneMode;
	private String mRestoreFlashMode;

	public ActorHdr(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
		mRestoreSceneMode = preferences.getString(
				CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO);
		mRestoreFlashMode = parameters.getFlashMode();
	}

	private void writeHdrPreferenceSettings() {
		writePreference(CameraSettings.KEY_SCENE_MODE,
				Parameters.SCENE_MODE_AUTO);
	}

	private void restoreHdrPreferenceSettings() {
		if (mRestoreSceneMode != null) {
			writePreference(CameraSettings.KEY_SCENE_MODE, mRestoreSceneMode);
		}
	}

	@Override
	public void updateModePreference() {
		if (!mIsImageCaptureIntent && !mPausing) {
			writeHdrPreferenceSettings();
		}
		mCameraDevice.setASDCallback(null);
	}

	@Override
	public void setCaptureModeSettings() {
		mCamera.resetZoomControl();
		if (mParameters.isZoomSupported()) {
			mParameters.setZoom(0);
		}
		if (mParameters.getSupportedFlashModes() != null) {
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		}
		mParameters.setColorEffect(Parameters.EFFECT_NONE);
		mParameters.setExposureCompensation(0);
		mParameters.setISOSpeed(CameraSettings.ISO_AUTO);
		mParameters.setHueMode(CameraSettings.DIP_MEDIUM);
		mParameters.setBrightnessMode(CameraSettings.DIP_MEDIUM);
		mParameters.setEdgeMode(CameraSettings.DIP_MEDIUM);
		mParameters.setSaturationMode(CameraSettings.DIP_MEDIUM);
		mParameters.setContrastMode(CameraSettings.DIP_MEDIUM);
	}

	@Override
	public void updateCaptureModeIndicator() {
		ImageView indicator = mCamera.mHDRIndicator;
		if (indicator != null) {
			indicator.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void updateCaptureModeUI(boolean autoScene) {
		// hdr
		overrideSelfTimer(false);
		mCamera.overrideSettings(CameraSettings.KEY_FLASH_MODE,
				mParameters.getFlashMode(), CameraSettings.KEY_SCENE_MODE,
				mParameters.getSceneMode(), CameraSettings.KEY_COLOR_EFFECT,
				mParameters.getColorEffect(), CameraSettings.KEY_EXPOSURE,
				String.valueOf(mParameters.getExposureCompensation()),
				CameraSettings.KEY_ISO, mParameters.getISOSpeed(),
				CameraSettings.KEY_EDGE, mParameters.getEdgeMode(),
				CameraSettings.KEY_SATURATION, mParameters.getSaturationMode(),
				CameraSettings.KEY_CONTRAST, mParameters.getContrastMode(),
				CameraSettings.KEY_HUE, mParameters.getHueMode(),
				CameraSettings.KEY_BRIGHTNESS, mParameters.getBrightnessMode());
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		if (!preferenceRestored) {
			restoreHdrPreferenceSettings();
		}
		ImageView indicator = mCamera.mHDRIndicator;
		if (indicator != null) {
			indicator.setVisibility(View.GONE);
		}
		mCamera.overrideSettings(
				CameraSettings.KEY_FLASH_MODE, null,
				CameraSettings.KEY_COLOR_EFFECT, null);
		if (mParameters.getSupportedFlashModes() != null
				&& mRestoreFlashMode != null) {
			mCamera.getCameraParameters().setFlashMode(mRestoreFlashMode);
		}
		mRestoreFlashMode = null;
		mRestoreSceneMode = null;
	}

	@Override
	public void ensureCaptureTempPath() {
		mParameters = mCamera.getCameraParameters();
		if (SAVE_ORIGINAL_PICTURE) {
			long dateTaken = System.currentTimeMillis();
			String title = Util.createJpegName(dateTaken);
			mOriginalPath = Storage.generateFilepath(title);
			mParameters.setCapturePath(mOriginalPath);
		} else {
			mOriginalPath = null;
			mParameters.setCapturePath(null);
		}
	}

	@Override
	public void onPictureTaken(Location location, int width, int height) {
		if (mOriginalPath == null) {
			updateSavingHint(false);
			return;
		}
		File file = new File(mOriginalPath);
		if (!file.exists()) {
			updateSavingHint(false);
			return;
		}
		String title = mOriginalPath.substring(
				mOriginalPath.lastIndexOf('/') + 1,
				mOriginalPath.lastIndexOf('.'));
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(mOriginalPath);
		} catch (IOException ex) {
			Log.e(TAG, "cannot read exif", ex);
		}

		int orientation = Util.getExifOrientation(exif);

		// Insert into MediaStore.
		ContentValues values = new ContentValues(9);
		values.put(ImageColumns.TITLE, title);
		values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
		values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
		values.put(ImageColumns.MIME_TYPE, "image/jpeg");
		values.put(ImageColumns.DATA, mOriginalPath);
		values.put(ImageColumns.SIZE, file.length());
		values.put(ImageColumns.ORIENTATION, orientation);
		values.put(ImageColumns.WIDTH, width);
		values.put(ImageColumns.HEIGHT, height);

		if (location != null) {
			values.put(ImageColumns.LATITUDE, location.getLatitude());
			values.put(ImageColumns.LONGITUDE, location.getLongitude());
		}

		try {
			mCamera.getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Throwable th) {
			// This can happen when the external volume is already mounted, but
			// MediaScanner has not notify MediaProvider to add that volume.
			// The picture is still safe and MediaScanner will find it and
			// insert it into MediaProvider. The only problem is that the user
			// cannot click the thumbnail to review the picture.
			Log.e(TAG, "Failed to write MediaStore" + th);
		}
		updateSavingHint(false);
	}

	@Override
	public void onPausePre() {
		super.onPausePre();
		if (mCamera.isFinishing()) {
			// exiting camera, restore flash mode before quit.
			restoreHdrPreferenceSettings();
		}
	}

	@Override
	public void updateCaptureModeButton(ControlBarIndicatorButton button,
			boolean enable) {
		if (button != null) {
			button.setEnabled(false);
			button.forceReloadPreference();
		}
	}

	@Override
	public void updateZoomControl(ZoomControl zoomControl, boolean enable) {
		if (zoomControl != null) {
			zoomControl.setEnabled(false);
		}
	}

	@Override
	public void onShutter() {
		if (LOG)
			Log.i(TAG, "HDR onShutter");
		updateSavingHint(true);
		mCamera.setCameraState(SAVING_PICTURES);
	}

	@Override
	public void onShutterButtonLongPressed() {
		ListPreference pref = mPreferenceGroup.findPreference(CameraSettings.KEY_HDR_CAPTURE_KEY);
		String showing = pref.getTitle() + mCamera.getString(R.string.camera_continuous_not_supported);
		new RotateTextToast(mCamera, showing, mCamera.getOrientation()).show();
	}
}

class ActorAsd extends ModeActor {
	private int mCurrentASDMode = -1;
	private TypedArray mASDDrawableIds;
	private TypedArray mASDModes;
	private final ASDCaptureCallback mASDCaptureCallback = new ASDCaptureCallback();

	public ActorAsd(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
	}

	@Override
	public void updateCaptureModeUI(boolean autoScene) {
		mCamera.overrideSettings(CameraSettings.KEY_COLOR_EFFECT,
				mParameters.getColorEffect(), CameraSettings.KEY_WHITE_BALANCE,
				mParameters.getWhiteBalance(), CameraSettings.KEY_SCENE_MODE,
				mParameters.getSceneMode());

		if (mCurrentASDMode == -1) {
			updateAsdIndicator(R.drawable.ic_camera_asd_auto);
		} else {
			if (mASDDrawableIds == null) {
				mASDDrawableIds = mCamera.getResources().obtainTypedArray(
						R.array.drawable_array_asd_mode);
				mASDModes = mCamera.getResources().obtainTypedArray(
						R.array.array_asd_mode);
			}
			updateAsdIndicator(mASDDrawableIds
					.getResourceId(mCurrentASDMode, 0));
		}
		overrideSelfTimer(false);
	}

	@Override
	public void setCaptureModeSettings() {
		mParameters.setWhiteBalance(CameraSettings.WHITE_BALANCE_AUTO);
		mParameters.setColorEffect(CameraSettings.COLOR_EFFECT_NONE);
	}

	private final class ASDCaptureCallback implements ASDCallback {
		public void onDetecte(int scene) {
			// TODO be careful logic here
			/*
			 * Back light is not supported in feature set but icon would still
			 * be showed
			 */
			if (LOG) Xlog.i(TAG, "onDetected: " + scene 
					+ ", mCurrentASDMode = " + mCurrentASDMode);
			if (mPausing) return;

			if (mASDDrawableIds == null) {
				mASDDrawableIds = mCamera.getResources().obtainTypedArray(
						R.array.drawable_array_asd_mode);
				mASDModes = mCamera.getResources().obtainTypedArray(
						R.array.array_asd_mode);
			}
			String sceneMode = mASDModes.getString(scene);
			if (LOG) Xlog.i(TAG, "Scene mode from ASD: " + sceneMode);
			if (mCurrentASDMode == scene) {
				return;
			}
			if (isSupported(sceneMode, mParameters.getSupportedSceneModes())) {
				writePreference(CameraSettings.KEY_SCENE_MODE, sceneMode);
				mCurrentASDMode = scene;
				mCamera.onSharedPreferenceChanged();
			} else {
				/**
				 * make sure icon is up to date, back light mode is not in
				 * feature table, so it will get here. The icon is show for back
				 * light anyway, tricky here.
				 */
				int resId = mASDDrawableIds.getResourceId(scene, 0);
				updateAsdIndicator(resId);
				mCurrentASDMode = scene;
			}
		}
	}

	@Override
	public void updateModePreference() {
		mCameraDevice.setASDCallback(mASDCaptureCallback);
	}

	public void updateAsdIndicator(final int resId) {
		mCamera.runOnUiThread(new Runnable() {
			public void run() {
				ImageView indicator = mCamera.mASDIndicator;
				if (indicator != null) {
					if (resId > 0) {
						indicator.setImageResource(resId);
						indicator.setVisibility(View.VISIBLE);
					} else {
						indicator.setVisibility(View.GONE);
					}
				}
			}
		});
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		updateAsdIndicator(-1);
		mCamera.overrideSettings(CameraSettings.KEY_COLOR_EFFECT, null);
		mCamera.overrideSettings(CameraSettings.KEY_WHITE_BALANCE, null);
		writePreference(CameraSettings.KEY_SCENE_MODE,
				Parameters.SCENE_MODE_AUTO);
	}

	@Override
	public void onPausePre() {
		super.onPausePre();
		if (mCamera.isFinishing()) {
			// exiting camera, reset scene mode before quit.
			writePreference(CameraSettings.KEY_SCENE_MODE,
					Parameters.SCENE_MODE_AUTO);
		}
	}
}

class ActorFaceBeauty extends ModeActor {
	private static final boolean SAVE_ORIGINAL_PICTURE = true;
	private String mOriginalPath;
	private String mRestoreSceneMode;
	private String mRestoreFlashMode;

	public ActorFaceBeauty(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice,
			android.hardware.Camera.Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences,
				modeName);
		mRestoreSceneMode = preferences.getString(
				CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO);
		mRestoreFlashMode = mParameters.getFlashMode();
	}

	@Override
	public void setCaptureMode() {
		super.setCaptureMode();
		mFocusManager.enableFaceBeauty(true);
	}

	@Override
	public void updateCaptureModeUI(boolean autoScene) {
		mCamera.overrideSettings(
				CameraSettings.KEY_FLASH_MODE, mParameters.getFlashMode(),
				CameraSettings.KEY_SCENE_MODE, mParameters.getSceneMode(),
				CameraSettings.KEY_WHITE_BALANCE, mParameters.getWhiteBalance(),
				CameraSettings.KEY_COLOR_EFFECT, mParameters.getColorEffect(),
				CameraSettings.KEY_EXPOSURE, String.valueOf(mParameters.getExposureCompensation()),
				CameraSettings.KEY_ISO, mParameters.getISOSpeed(),
				CameraSettings.KEY_EDGE, mParameters.getEdgeMode(),
				CameraSettings.KEY_SATURATION, mParameters.getSaturationMode(),
				CameraSettings.KEY_CONTRAST, mParameters.getContrastMode(),
				CameraSettings.KEY_HUE, mParameters.getHueMode(),
				CameraSettings.KEY_BRIGHTNESS, mParameters.getBrightnessMode());
		overrideSelfTimer(false);
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		if (!preferenceRestored) {
			restoreFBPreferenceSettings();
		}
		mFocusManager.enableFaceBeauty(false);
		mCamera.overrideSettings(
				CameraSettings.KEY_COLOR_EFFECT, null,
				CameraSettings.KEY_FLASH_MODE, null);
		if (mParameters.getSupportedFlashModes() != null
				&& mRestoreFlashMode != null) {
			mCamera.getCameraParameters().setFlashMode(mRestoreFlashMode);
		}
		mRestoreFlashMode = null;
		mRestoreSceneMode = null;
	}

	@Override
	public void updateModePreference() {
		if (!mIsImageCaptureIntent && !mPausing) {
			writeFBPreferenceSettings();
		}
		mCameraDevice.setASDCallback(null);
	}

	@Override
	public void setCaptureModeSettings() {
		mCamera.resetZoomControl();
		if (mParameters.isZoomSupported()) {
			mParameters.setZoom(0);
		}
		if (mParameters.getSupportedFlashModes() != null) {
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		}
		mParameters.setColorEffect(Parameters.EFFECT_NONE);
		mParameters.setExposureCompensation(0);
		mParameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
	}

	@Override
	public void ensureCaptureTempPath() {
		mParameters = mCamera.getCameraParameters();
		if (SAVE_ORIGINAL_PICTURE) {
			long dateTaken = System.currentTimeMillis();
			String title = Util.createJpegName(dateTaken);
			mOriginalPath = Storage.generateFilepath(title);
			mParameters.setCapturePath(mOriginalPath);
		} else {
			mOriginalPath = null;
			mParameters.setCapturePath(null);
		}
	}

	@Override
	public void onPictureTaken(Location location, int width, int height) {
		if (mOriginalPath == null) {
			return;
		}
		File file = new File(mOriginalPath);
		if (!file.exists())
			return;
		String title = mOriginalPath.substring(
				mOriginalPath.lastIndexOf('/') + 1,
				mOriginalPath.lastIndexOf('.'));
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(mOriginalPath);
		} catch (IOException ex) {
			Log.e(TAG, "cannot read exif", ex);
		}

		int orientation = Util.getExifOrientation(exif);

		// Insert into MediaStore.
		ContentValues values = new ContentValues(9);
		values.put(ImageColumns.TITLE, title);
		values.put(ImageColumns.DISPLAY_NAME, title + ".jpg");
		values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
		values.put(ImageColumns.MIME_TYPE, "image/jpeg");
		values.put(ImageColumns.DATA, mOriginalPath);
		values.put(ImageColumns.SIZE, file.length());
		values.put(ImageColumns.ORIENTATION, orientation);
		values.put(ImageColumns.WIDTH, width);
		values.put(ImageColumns.HEIGHT, height);

		if (location != null) {
			values.put(ImageColumns.LATITUDE, location.getLatitude());
			values.put(ImageColumns.LONGITUDE, location.getLongitude());
		}

		Uri uri = null;
		try {
			uri = mCamera.getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Throwable th) {
			// This can happen when the external volume is already mounted, but
			// MediaScanner has not notify MediaProvider to add that volume.
			// The picture is still safe and MediaScanner will find it and
			// insert it into MediaProvider. The only problem is that the user
			// cannot click the thumbnail to review the picture.
			Log.e(TAG, "Failed to write MediaStore" + th);
		}
	}

	private void writeFBPreferenceSettings() {
		writePreference(CameraSettings.KEY_SCENE_MODE,
				Parameters.SCENE_MODE_AUTO);
	}

	private void restoreFBPreferenceSettings() {
		if (mRestoreSceneMode != null) {
			writePreference(CameraSettings.KEY_SCENE_MODE, mRestoreSceneMode);
		}
	}

	@Override
	public void updateZoomControl(ZoomControl zoomControl, boolean enable) {
		if (zoomControl != null) {
			zoomControl.setEnabled(false);
		}
	}

	@Override
	public void onPausePre() {
		super.onPausePre();
		if (mCamera.isFinishing()) {
			// exiting camera, restore flash mode before quit.
			restoreFBPreferenceSettings();
		}
	}

    @Override
	public boolean enableFD(int cameraId) {
		return true;
	}
}

class ActorContinuousShot extends ModeActor {

	public static final int CAPTURE_NORMAL = 1;
	private static final int UPDATE_THUMB_COUNT = 2;
	private int mMaxCaptureNum = Integer.parseInt(CameraSettings.DEFAULT_CAPTURE_NUM);
	private boolean mContinuousShotPerformed = false;
	private int mCurrentShotsNum = 0;
	private SoundPool mBurstSound;
	private int mSoundID;
	private int mStreamID;
	private Thread mWaitSavingDoneThread;
	private int mUpdateCount = 0;

	public ActorContinuousShot(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice, Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences, modeName);
		mCamera.mShutterButton.setOnShutterButtonListener(this);
		mBurstSound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mSoundID = mBurstSound.load("/system/media/audio/ui/camera_shutter.ogg", 1);
	}

	public void onShutterButtonLongPressed() {
		Log.v(TAG, "onShutterButtonLongPressed!");
		if (!mCamera.checkCameraState() || !canShot()) return;
		mCamera.mImageSaver.enlargeQueueLimit();
		mContinuousShotPerformed = true;
		mCurrentShotsNum = 0;
		mParameters = mCamera.getCameraParameters();
		mParameters.setCaptureMode(Parameters.CAPTURE_MODE_CONTINUOUS_SHOT);
		mParameters.setBurstShotNum(mMaxCaptureNum);
		mCameraDevice.setParameters(mParameters);
		mFocusManager.doSnap();
		mStreamID = mBurstSound.play(mSoundID, 1.0f, 1.0f, 1, -1, 1.0f);
	}

	public void onShutterButtonFocus(boolean pressed) {
		Log.i(TAG, "onShutterButtonFocus, pressed = " + pressed);
		if (!pressed && mContinuousShotPerformed && !mPausing) {
			Log.i(TAG, "Button up Msg received, start to Cancel continuous shot");
			mContinuousShotPerformed = false;
			mCameraDevice.cancelContinuousShot();
			mBurstSound.stop(mStreamID);
			updateSavingHint(true);
			mCamera.setCameraState(SAVING_PICTURES);
			mWaitSavingDoneThread = new Thread() {
				@Override
				public void run() {
					mCamera.mImageSaver.waitDoneInSubThread();
					mHandler.sendEmptyMessage(MSG_BURST_SAVING_DONE);
				}
			};
			mWaitSavingDoneThread.start();
		}
	}

	@Override
	public void onPausePre() {
		super.onPausePre();
		if (mContinuousShotPerformed) {
			mContinuousShotPerformed = false;
			mCameraDevice.cancelContinuousShot();
			mBurstSound.stop(mStreamID);
			mCurrentShotsNum = 0;
		}
	}

	@Override
	public void onResume() {
		mPausing = false;
		if (LOG) Log.i(TAG, "onResume() mPausing=" + mPausing);
		Storage.updateDefaultDirectory(mCamera, true);

		if (mWaitSavingDoneThread != null && mWaitSavingDoneThread.isAlive()) {
			updateSavingHint(true);
		} else {
			updateSavingHint(false);
		}
	}

	public void onShutterButtonClick() {
		if (!mContinuousShotPerformed && !mPausing) {
			mParameters = mCamera.getCameraParameters();
			mParameters.setCaptureMode(Parameters.CAPTURE_MODE_NORMAL);
			mParameters.setBurstShotNum(CAPTURE_NORMAL);
			mCameraDevice.setParameters(mParameters);
			mCamera.onShutterButtonClick();
		}
	}

	@Override
	public void onBurstSaveDone() {
		if (!mPausing) {
			mCamera.resumePreview();
		}
	}

	@Override
	public PictureCallback getPictureCallback(Location loc) {
		if (mContinuousShotPerformed) {
			return new ContinuousJpegPictureCallback(loc);
		} else {
			return null;
		}
	}

	class ContinuousJpegPictureCallback implements PictureCallback {
		public ContinuousJpegPictureCallback(Location loc) {
			mLastJpegLoc = loc;
		}

		public void onPictureTaken(final byte[] data, android.hardware.Camera camera) {

			Size size = mParameters.getPictureSize();
			mCamera.mImageSaver.addImage(data, mLastJpegLoc, size.width, size.height,
				MediaStore.ThreeDimensionColumns.STEREO_TYPE_2D);
			// Not adding number when capture canceled
			if (mContinuousShotPerformed) mCurrentShotsNum++;
			if (mCurrentShotsNum == mMaxCaptureNum
					|| mCamera.mImageSaver.isFull()) {
				onShutterButtonFocus(false);
			} else if (mCamera.mImageSaver.isHeavyWriting()) {
				mCameraDevice.slowdownContinuousShot();
			}
			Log.i(TAG, "Continuous Shot, onPictureTaken: mCurrentShotsNum = "
					+ mCurrentShotsNum + " mContinuousShotPerformed = " + mContinuousShotPerformed);
		}
	}

	@Override
	public void restoreModeUI(boolean preferenceRestored) {
		mCamera.mImageSaver.resetQueueLimit();
		mBurstSound.unload(mSoundID);
	}

	public void lowStorage() {
		onShutterButtonFocus(false);
	}

	@Override
	public void updateSavingHint(boolean bSaving) {
		if (mPausing) return;
		RotateDialogController savingDlg = mCamera.mRotateDialog;
		if (bSaving) {
			savingDlg.showWaitingDialog(
					String.format(Locale.ENGLISH, mCamera.getResources().getString(
							R.string.continuous_saving_pictures),
							mCurrentShotsNum == mMaxCaptureNum ? mCurrentShotsNum : mCurrentShotsNum + 1));
		} else if (savingDlg != null) {
			savingDlg.dismissDialog();
			savingDlg = null;
		}
	}

	@Override
	public ShutterButton.OnShutterButtonListener getShutterButtonListener() {
		return this;
	}

	@Override
	public void setMtkCameraParameters(String sceneMode, Size prePictureSize) {
		super.setMtkCameraParameters(sceneMode, prePictureSize);
		String capNum = mPreferences.getString(
				CameraSettings.KEY_CONTINUOUS_NUMBER, CameraSettings.DEFAULT_CAPTURE_NUM);
		mMaxCaptureNum = Integer.parseInt(capNum);
	}

	@Override
	public boolean checkMode(String newMode) {
		// Continuous shot is enable in normal capture mode.
		return super.checkMode(newMode)
				|| Parameters.CAPTURE_MODE_NORMAL.equals(newMode);
	}

	@Override
	public boolean updateThumbnailInSaver(int queueSize, int sizeRatio) {
		boolean retVal = false;
		mUpdateCount++;
		if (mUpdateCount == UPDATE_THUMB_COUNT) {
			mUpdateCount = 0;
			retVal =  true;
		} else if (queueSize <= UPDATE_THUMB_COUNT) {
			retVal = true;
		}
		return retVal;
	}
}

class ActorSingle3DAutorama extends ModeActor 
		implements AUTORAMACallback, AUTORAMAMVCallback {

	private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;

	private static final int DIRECTION_RIGHT  = 0;
	private static final int DIRECTION_LEFT = 1;
	private static final int DIRECTION_UP  = 2;
	private static final int DIRECTION_DOWN = 3;
	private static final int DIRECTION_UNKNOWN = 4;

    private static final int AUTORAMA_IDLE = 0;
    private static final int AUTORAMA_STARTED = 1;
    private static final int AUTORAMA_MERGING = 2;

    private static final int TARGET_DISTANCE_HORIZONTAL = 32;
    private static final int TARGET_DISTANCE_VERTICAL = 24;

	private int mDirection = DIRECTION_UNKNOWN;
	private int mState;

    private static final int NUM_AUTORAMA_CAPTURE = 2;
    private int mCurrentNum = 0;

    private boolean mStopping;
	private View mPanoView;
	private ImageView mCenterWindow;	
	private View mNaviWindow;
	private ProgressIndicator mProgressIndicator;
	private Drawable mNormalWindowDrawable;		
	private Size mPreviewSize;
	private int mDisplayOrientaion;
	private int mOrientaion = 270;
	private boolean mPaused;

	private Object mLocker = new Object();
	private boolean mStopProcess = false;
	private boolean mShowingCollimatedDrawable = false;

	public ActorSingle3DAutorama(com.android.camera.Camera camera,
			android.hardware.Camera cameraDevice, Parameters parameters,
			PreferenceGroup preferenceGroup, ComboPreferences preferences,
			String modeName) {
		super(camera, cameraDevice, parameters, preferenceGroup, preferences, modeName);
		initializeViews(camera);
		mProgressIndicator = new ProgressIndicator(camera, ProgressIndicator.TYPE_SINGLE3D);
		mProgressIndicator.setVisibility(View.GONE);
		mProgressIndicator.setOrientation(mOrientaion);
	}

    @Override
    public void updateCaptureModeButton(ControlBarIndicatorButton button,
            boolean enable) {
        if (button != null) {
            button.setEnabled(enable);
            button.forceReloadPreference();
        }
    }

    @Override
    public void updateCaptureModeUI(boolean autoScene) {
        Size size = mParameters.getPictureSize();
        mCamera.overrideSettings(
                CameraSettings.KEY_SCENE_MODE, null,
                CameraSettings.KEY_HDR_CAPTURE_KEY, "off",
                CameraSettings.KEY_FLASH_MODE, "off");
        overrideSelfTimer(true);
    }

    @Override
    public void setCaptureModeSettings() {
        if (mParameters.getSupportedFlashModes() != null) {
            mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        }
    }

	private void initializeViews(Activity activity) {
		mPanoView = activity.findViewById(R.id.pano_view);

		mCenterWindow = (ImageView)activity.findViewById(R.id.center_window);
		mNaviWindow = activity.findViewById(R.id.navi_window);

		android.content.res.Resources res = activity.getResources();
		mNormalWindowDrawable = res.getDrawable(R.drawable.ic_pano_normal_window); 
		mCamera.setCancelButtonOnClickListener(mOnCancelClickListener);
    }

	public boolean hasCaptured() {
		Xlog.d(TAG, "hasCaptured mCurrentNum: " + mCurrentNum);
		return mState != AUTORAMA_IDLE && mCurrentNum > 0;
	}

	@Override
	public void setDisplayOrientation(int orientation) {
		Log.i(TAG, "setOrientation : " + orientation);
		mDisplayOrientaion = orientation;
	}

	private View.OnClickListener mOnCancelClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			mCamera.onAutoramaBtnPressed(false);
		}
	};

	public boolean start() {
		Log.i(TAG, "Start to capture");
		mCameraDevice = mCamera.mCameraDevice;
		if (mCameraDevice != null && mState == AUTORAMA_IDLE && !mStopping) {
			mState = AUTORAMA_STARTED;
			mCurrentNum = 0;
			mShowingCollimatedDrawable = false;
			mDirection = DIRECTION_UNKNOWN;
			mPaused = false;

			doStart();

			mPanoView.setVisibility(View.VISIBLE);
			mCenterWindow.setVisibility(View.VISIBLE);
			mProgressIndicator.setProgress(0);
			mProgressIndicator.setVisibility(View.VISIBLE);
			mCamera.showPostSingle3DControlAlert();
			mCamera.showSingle3DGuide(R.string.single3d_guide_move);
			return true;
		} else {
			Xlog.d(TAG, "start mState: " + mState);
			return false;			
		}
	}

	private void stopAsync(final boolean isMerge) {
		Xlog.d(TAG, "stopAsync mStopping: " + mStopping);

		if (mStopping) return;

		mStopping = true;
        Thread stopThread = new Thread(new Runnable() {
        	public void run() {
            	doStop(isMerge);
				mHandler.post(new Runnable() {
					 public void run() {
						 mStopping = false;
						 if (!isMerge) {//if isMerge is true, onHardwareStopped will be called in onCapture.
						 	onHardwareStopped(false);
						 	hideSingle3DUI();
						 }
					 }
				});

		        synchronized (mLocker) {
					mStopProcess = false;
					mLocker.notifyAll();
		        }				
            }
        });
        synchronized (mLocker) {
			mStopProcess = true;
		}
        stopThread.start();
	}

    public void checkStopProcess() {
        while (mStopProcess) {
            try {
                synchronized (mLocker) {
                    Log.v(TAG, "Stop3DSHOT is running, so wait for it.");
                    mLocker.wait();
                    Log.v(TAG, "Stop3DSHOT is done");
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

	private void doStart() {
		Xlog.d(TAG, "doStart");		
		mCameraDevice.setAUTORAMACallback(this);
		mCameraDevice.setAUTORAMAMVCallback(this);
        if (Util.getS3DMode()) {
            mCameraDevice.start3DSHOT(NUM_AUTORAMA_CAPTURE);
        } else {
            mCameraDevice.startAUTORAMA(NUM_AUTORAMA_CAPTURE);
        }
	}

	private void doStop(boolean isMerge) {
		Xlog.d(TAG, "doStop isMerge "+isMerge);
		
		if (mCameraDevice != null) {
			CameraHolder holder = CameraHolder.instance();
			synchronized(holder) {
				if (holder.isSameCameraDevice(mCameraDevice)) {//means that hw was shutdown and no need to call stop anymore.
                    if (Util.getS3DMode()) {
                        Xlog.d(TAG, "mCameraDevice.stop3DSHOT()");
                        mCameraDevice.stop3DSHOT(isMerge ? 1 : 0);
                    } else {
                        mCameraDevice.stopAUTORAMA(isMerge ? 1 : 0);
                    }
				} else {
					Xlog.w(TAG, "doStop device is release? ");
				}
			}
		}
    }

	private void onHardwareStopped(boolean isMerge) {
		Xlog.d(TAG, "onHardwareStopped isMerge: " + isMerge);

		if (isMerge) {
			mCameraDevice.setAUTORAMACallback(null);
			mCameraDevice.setAUTORAMAMVCallback(null);
		}	
		mCamera.onAutoramaCaptureDone(isMerge);
	}

    public void hideSingle3DUI() {
        mPanoView.setVisibility(View.INVISIBLE);
        mNaviWindow.setVisibility(View.INVISIBLE);
        mHandler.postDelayed(new Runnable(){
            public void run() {
                mProgressIndicator.setVisibility(View.INVISIBLE);
            }
        },50);
    }

	public void stop(boolean isMerge) {
		Xlog.d(TAG, "stop mState: " + mState);
		
		if (mCameraDevice != null && mState == AUTORAMA_STARTED) {
			mState = isMerge ? AUTORAMA_MERGING : AUTORAMA_IDLE;
			if (!isMerge) {
				mCameraDevice.setAUTORAMACallback(null);
				mCameraDevice.setAUTORAMAMVCallback(null);
			} else {
			    mCamera.onAutoramaMergeStarted();
	            hideSingle3DUI();
	            mCamera.showSingle3DGuide(R.string.empty);
			}			
			stopAsync(isMerge);
		} 
	}
	
    public void onCapture() {
        Xlog.i(TAG, "onCapture: " + mCurrentNum + ",mState: " + mState);
		if (mState == AUTORAMA_IDLE) return;

		if (mCurrentNum == NUM_AUTORAMA_CAPTURE || mState == AUTORAMA_MERGING) {
            Xlog.i(TAG, "autorama done");
	        mState = AUTORAMA_IDLE;
			onHardwareStopped(true);
        } else if (mCurrentNum >= 0 && mCurrentNum < NUM_AUTORAMA_CAPTURE) {
           if (mCurrentNum == 0) mFocusManager.onShutter();
		   mProgressIndicator.setProgress(mCurrentNum+1);
           mNaviWindow.setVisibility(View.INVISIBLE);
		   
		   mCenterWindow.setImageResource(R.anim.window_collimate);
		   AnimationDrawable animation = (AnimationDrawable) mCenterWindow.getDrawable();
		   animation.start();
		   if (mShowingCollimatedDrawable) {
				mHandler.removeCallbacksAndMessages(null);
		   }
		   mShowingCollimatedDrawable = true;
           mHandler.postDelayed(new Runnable() {
           		public void run() {
                	mShowingCollimatedDrawable = false;
                	((AnimationDrawable) mCenterWindow.getDrawable()).stop();
                	if (mState != AUTORAMA_STARTED) {
                        mPanoView.setVisibility(View.INVISIBLE);
                        mNaviWindow.setVisibility(View.INVISIBLE);
                	}
					mCenterWindow.setImageDrawable(mNormalWindowDrawable);
                }
           }, 500);
        } else {
            Xlog.w(TAG, "onCapture is called in abnormal state");
		}

		mCurrentNum++;
		if (mCurrentNum == NUM_AUTORAMA_CAPTURE) {
			stop(true);
		}
    }

    private void drawNaviWindow() {
        int cwx = mCenterWindow.getLeft()+mCenterWindow.getPaddingLeft();
        int cwy = mCenterWindow.getTop()+mCenterWindow.getPaddingTop();
        mNaviWindow.layout(cwx,
                cwy + TARGET_DISTANCE_HORIZONTAL,
                cwx + mNaviWindow.getWidth(),
                cwy + TARGET_DISTANCE_HORIZONTAL + mNaviWindow.getHeight());
        mNaviWindow.setVisibility(View.VISIBLE);
    }
    
    public void onFrame(int xy, int direction) {
        Log.v("T", "xy="+xy+ " direction="+direction);

    	if (mPaused || direction == DIRECTION_UNKNOWN || mShowingCollimatedDrawable || 
				mState != AUTORAMA_STARTED || mCurrentNum < 1) {
    	    if (!mPaused) drawNaviWindow();
    	    return;
    	}

    	short x = (short)((xy & 0xFFFF0000) >> 16);
        short y = (short)(xy & 0x0000FFFF);

		int cwx = mCenterWindow.getLeft()+mCenterWindow.getPaddingLeft();
		int cwy = mCenterWindow.getTop()+mCenterWindow.getPaddingTop();
		float x_ratio = (float)mPanoView.getWidth() / (float)mPreviewSize.width;
		float y_ratio = (float)mPanoView.getHeight() / (float)mPreviewSize.height;	
		
		//assume that the activity's requested orientation is same as the lcm orientation.
		//if not,the following caculation would be wrong!!
		if (mDisplayOrientaion == 180){
			if (direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
				direction = 1 - direction;
			} else {
				direction = 5 - direction;
			}
			x = (short)-x;
			y = (short)-y;
		} else if (mDisplayOrientaion == 90) {
			float temp = x_ratio;
			x_ratio = y_ratio;
			y_ratio = -temp;

			int temp2 = cwx;
			cwx = cwy;
			cwy = temp2;
		}
		
        x *= x_ratio;
        y *= y_ratio;
		
        int screenPosX = 0;
        int screenPosY = 0;
        
        switch (direction) {
        case DIRECTION_RIGHT:
        	screenPosX = -x + cwx + (int)(TARGET_DISTANCE_HORIZONTAL * x_ratio);
        	screenPosY = -y + cwy;
        	break;
        case DIRECTION_LEFT:
        	screenPosX = -x + cwx + (int)(-TARGET_DISTANCE_HORIZONTAL * x_ratio);
        	screenPosY = -y + cwy;
        	break;
        case DIRECTION_UP:
        	screenPosX = -x + cwx;
        	screenPosY = -y + cwy + (int)(-TARGET_DISTANCE_VERTICAL * y_ratio);
        	break;
        case DIRECTION_DOWN:
        	screenPosX = -x + cwx;
        	screenPosY = -y + cwy + (int)(TARGET_DISTANCE_VERTICAL * y_ratio);
        	break;
        }
		Xlog.i(TAG, "onFrame x = "+x/x_ratio + " y = "+y/y_ratio + " cwx = "+cwx + " cwy = "+cwy+" screenPosX = "+screenPosX 
			+ " screenPosY = "+screenPosY);

		int w = mNaviWindow.getWidth();
		int h = mNaviWindow.getHeight();
		if (mDisplayOrientaion == 90) {
			if (direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
				direction = 3 - direction;
			} else {
				direction -= 2;
			}

			int temp = screenPosX;
			screenPosX = screenPosY;
			screenPosY = temp;

			temp = w;
			w = h;
			h = temp;				
		}			
		if (screenPosX < 0) {
		    screenPosX = 0;
		}
		if (screenPosY < 0) {
		    screenPosY = 0;
		}
		if (mPanoView.getWidth() < screenPosX+w) {
		    screenPosX = mPanoView.getWidth() - w;
		}
        if (mPanoView.getHeight() < screenPosY+h) {
            screenPosY = mPanoView.getHeight() - h;
        }
        mNaviWindow.setVisibility(View.VISIBLE);
		mNaviWindow.layout(screenPosX,screenPosY,
		        screenPosX+w,screenPosY+h);
    }

    @Override
    public boolean applySpecialCapture() {
    	//set path
    	mCamera.setCapturePath();
        mCameraDevice.setParameters(mParameters);
    	start();
    	return true;
    }

    @Override
	protected void setCaptureMode() {
        mParameters.setCaptureMode(mModeName);
    	mPreviewSize = mParameters.getPreviewSize();
    	mCamera.stopFaceDetection();
    }

    @Override
    public void restoreModeUI(boolean preferenceRestored) {
        mCamera.overrideSettings(
                CameraSettings.KEY_HDR_CAPTURE_KEY, null,
                CameraSettings.KEY_FLASH_MODE, null);
        overrideSelfTimer(true);
    	mProgressIndicator.setVisibility(View.INVISIBLE);
    	mCamera.startFaceDetection();
    }

    @Override
	public boolean enableFD(int cameraId) {
		return false;
	}

    @Override
    public void setOrientation(int orientation) {
        mOrientaion = orientation;
        if (mOrientaion == 270) 
            mProgressIndicator.setOrientation(mOrientaion);
    }

    @Override
    public void onPausePre() {
        mPaused = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mState = AUTORAMA_IDLE;
        mProgressIndicator.setProgress(0);// reset to 0
        mProgressIndicator.setVisibility(View.INVISIBLE);
        mPanoView.setVisibility(View.INVISIBLE);
        mNaviWindow.setVisibility(View.GONE);
        mCenterWindow.setVisibility(View.GONE);
    }

    @Override
    public void onShutterButtonLongPressed() { }
}

