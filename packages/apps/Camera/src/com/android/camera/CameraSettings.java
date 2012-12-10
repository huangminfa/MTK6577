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

package com.android.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

/**
 *  Provides utilities and keys for Camera settings.
 */
public class CameraSettings {
    private static final int NOT_FOUND = -1;

    public static final String KEY_VERSION = "pref_version_key";
    public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
    public static final String KEY_RECORD_LOCATION = RecordLocationPreference.KEY;
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN = "pref_camera_first_use_hint_shown_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN = "pref_video_first_use_hint_shown_key";

    //Mediatek feature begin
    public static final String KEY_NORMAL_CAPTURE_KEY = "pref_camera_normal_capture_key";
    public static final String KEY_HDR_CAPTURE_KEY = "pref_camera_hdr_key";
    public static final String KEY_FD_MODE = "pref_camera_fd_key";
    public static final String KEY_ISO = "pref_camera_iso_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_coloreffect_key";
    public static final String KEY_FOCUS_METER = "pref_camera_focusmeter_key";
    public static final String KEY_EXPOSURE_METER = "pref_camera_exposuremeter_key";
    public static final String KEY_CAMERA_ZSD = "pref_camera_zsd_key";

    //video
    public static final String KEY_VIDEO_SCENE_MODE = "pref_video_scenemode_key";
    public static final String KEY_VIDEO_FOCUS_MODE = "pref_camera_video_focusmode_key";
    public static final String KEY_VIDEO_DURATION = "pref_camera_video_duration_key";
    public static final String KEY_VIDEO_RECORD_AUDIO = "pref_camera_recordaudio_key";
    // HD Recording 
    public static final String KEY_VIDEO_HD_AUDIO_RECORDING = "pref_camera_video_hd_recording_key";
    
    public static final String KEY_EDGE = "pref_camera_edge_key";
    public static final String KEY_HUE = "pref_camera_hue_key";
    public static final String KEY_SATURATION = "pref_camera_saturation_key";
    public static final String KEY_BRIGHTNESS = "pref_camera_brightness_key";
    public static final String KEY_CONTRAST = "pref_camera_contrast_key";
    public static final String KEY_SELF_TIMER = "pref_camera_self_timer_key";
    public static final String KEY_ANTI_BANDING = "pref_camera_antibanding_key";
    public static final String KEY_VIDEO_EIS = "pref_video_eis_key";
    public static final String KEY_CONTINUOUS_NUMBER = "pref_camera_shot_number";
    public static final String KEY_VIDEO_FIRST_USE_HINT_PAUSE_SHOWN = "pref_pause_hint";
    
    public static final String FOCUS_METER_SPOT = "spot";
    public static final String WHITE_BALANCE_AUTO = "auto";
    public static final String COLOR_EFFECT_NONE = "none";
    public static final String MAX_ISO_SPEED = "1600";
    public static final String ISO_SPEED_1600 = "1600";
    public static final String ISO_SPEED_800 = "800";
    public static final String ISO_AUTO = "auto";
    public static final String IMG_SIZE_FOR_HIGH_ISO = "1280x960";      // Limit pic size to 1M
    public static final String IMG_SIZE_FOR_PANORAMA = "1600x1200";     // Limit pic size to 2M
    public static final String FACE_DETECTION_DEFAULT = "on";
    public static final String SELF_TIMER_OFF = "0";
    public static final String DIP_MEDIUM = "middle";
    public static final String STEREO3D_ENABLE = "1";
    public static final String STEREO3D_DISABLE = "0";
    // Continuous shot number
	public static final String DEFAULT_CAPTURE_NUM = "40";

    //Mediatek feature end
    
    public static final String EXPOSURE_DEFAULT_VALUE = "0";

    public static final int CURRENT_VERSION = 5;
    public static final int CURRENT_LOCAL_VERSION = 2;

    public static final int DEFAULT_VIDEO_DURATION = 0; // no limit

    private static final String TAG = "CameraSettings";

    private final Context mContext;
    private final Parameters mParameters;
    private final CameraInfo[] mCameraInfo;
    private final int mCameraId;

    public CameraSettings(Activity activity, Parameters parameters,
                          int cameraId, CameraInfo[] cameraInfo) {
        mContext = activity;
        mParameters = parameters;
        mCameraId = cameraId;
        mCameraInfo = cameraInfo;
    }

    public PreferenceGroup getPreferenceGroup(int preferenceRes) {
        PreferenceInflater inflater = new PreferenceInflater(mContext);
        PreferenceGroup group =
                (PreferenceGroup) inflater.inflate(preferenceRes);
        initPreference(group);
        return group;
    }

    public static String getDefaultVideoQuality(int cameraId,
            String defaultQuality) {
        int quality = Integer.valueOf(defaultQuality);
        if (CamcorderProfile.hasProfile(cameraId, quality)) {
            return defaultQuality;
        }
        return Integer.toString(CamcorderProfile.QUALITY_MTK_HIGH);
    }

    public static void initialCameraPictureSize(
            Context context, Parameters parameters) {
        // When launching the camera app first time, we will set the picture
        // size to the first one in the list defined in "arrays.xml" and is also
        // supported by the driver.
        List<Size> supported = parameters.getSupportedPictureSizes();
        if (supported == null) return;
        for (String candidate : context.getResources().getStringArray(
                R.array.pref_camera_picturesize_entryvalues)) {
            if (setCameraPictureSize(candidate, supported, parameters)) {
                SharedPreferences.Editor editor = ComboPreferences
                        .get(context).edit();
                editor.putString(KEY_PICTURE_SIZE, candidate);
                editor.apply();
                return;
            }
        }
        Log.e(TAG, "No supported picture size found");
    }

    public static void removePreferenceFromScreen(
            PreferenceGroup group, String key) {
        removePreference(group, key);
    }

    public static boolean setCameraPictureSize(
            String candidate, List<Size> supported, Parameters parameters) {
        int index = candidate.indexOf('x');
        if (index == NOT_FOUND) return false;
        int width = Integer.parseInt(candidate.substring(0, index));
        int height = Integer.parseInt(candidate.substring(index + 1));
        for (Size size : supported) {
            if (size.width == width && size.height == height) {
                parameters.setPictureSize(width, height);
                return true;
            }
        }
        return false;
    }

    private void initPreference(PreferenceGroup group) {
        ListPreference videoQuality = group.findPreference(KEY_VIDEO_QUALITY);
        ListPreference timeLapseInterval = group.findPreference(KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        ListPreference pictureSize = group.findPreference(KEY_PICTURE_SIZE);
        ListPreference whiteBalance =  group.findPreference(KEY_WHITE_BALANCE);
        ListPreference sceneMode = group.findPreference(KEY_SCENE_MODE);
        ListPreference flashMode = group.findPreference(KEY_FLASH_MODE);
        ListPreference focusMode = group.findPreference(KEY_FOCUS_MODE);
        ListPreference exposure = group.findPreference(KEY_EXPOSURE);
        IconListPreference cameraIdPref =
                (IconListPreference) group.findPreference(KEY_CAMERA_ID);
        ListPreference videoFlashMode =
                group.findPreference(KEY_VIDEOCAMERA_FLASH_MODE);
        ListPreference videoEffect = group.findPreference(KEY_VIDEO_EFFECT);
        
        //Mediatek feature begin
        ListPreference videoDuration = group.findPreference(KEY_VIDEO_DURATION);
        ListPreference normalCapture = group.findPreference(KEY_NORMAL_CAPTURE_KEY);
        ListPreference iso = group.findPreference(KEY_ISO);
        ListPreference focusMeter = group.findPreference(KEY_FOCUS_METER);
        ListPreference antiBanding = group.findPreference(KEY_ANTI_BANDING);
        ListPreference exposureMeter = group.findPreference(KEY_EXPOSURE_METER);
        ListPreference hdrCapMode = group.findPreference(KEY_HDR_CAPTURE_KEY);
        ListPreference colorEffect = group.findPreference(KEY_COLOR_EFFECT);
        ListPreference edge = group.findPreference(KEY_EDGE);
        ListPreference hue = group.findPreference(KEY_HUE);
        ListPreference sat = group.findPreference(KEY_SATURATION);
        ListPreference brightness = group.findPreference(KEY_BRIGHTNESS);
        ListPreference contrast = group.findPreference(KEY_CONTRAST);
        ListPreference eis = group.findPreference(KEY_VIDEO_EIS);
        ListPreference videoFocusMode = group.findPreference(KEY_VIDEO_FOCUS_MODE);
        ListPreference videoSceneMode = group.findPreference(KEY_VIDEO_SCENE_MODE);

        List<String> supportCaptureModes = mParameters.getSupportedCaptureMode();
        if (supportCaptureModes == null) {
        	normalCapture = null;
        	hdrCapMode = null;
        }
        if (normalCapture != null && 
        		supportCaptureModes.indexOf(Parameters.CAPTURE_MODE_BURST_SHOT) != NOT_FOUND) {
        	//Burst shot supported
        	CharSequence values[] = normalCapture.getEntryValues();
        	for(CharSequence value : values) {
        		if (value.toString().startsWith(Parameters.CAPTURE_MODE_BURST_SHOT)) {
        			supportCaptureModes.add(value.toString());
        		}
        	}
        }
        if (hdrCapMode != null ) {
        	if (supportCaptureModes.indexOf(Parameters.CAPTURE_MODE_HDR) == NOT_FOUND) {
            	filterUnsupportedOptions(group, hdrCapMode, null);
            }
        }
        if (normalCapture != null) {
        	filterUnsupportedOptions(group, normalCapture, supportCaptureModes);
        }
        if (iso != null){
            filterUnsupportedOptions(group, iso, mParameters.getSupportedISOSpeed());
        }
        if (colorEffect != null) {
            filterUnsupportedOptions(group,
                    colorEffect, mParameters.getSupportedColorEffects());
        }
        if (focusMeter != null){
            filterUnsupportedOptions(group, focusMeter, mParameters.getSupportedFocusMeter());
        }
        if (antiBanding != null){
            filterUnsupportedOptions(group, antiBanding, mParameters.getSupportedAntibanding());
        }
        if (exposureMeter != null){
            filterUnsupportedOptions(group, exposureMeter, mParameters.getSupportedExposureMeter());
        }
        if (edge != null){
            filterUnsupportedOptions(group, edge, mParameters.getSupportedEdgeMode());
        }
        if (hue != null){
            filterUnsupportedOptions(group, hue, mParameters.getSupportedHueMode());
        }
        if (sat != null){
            filterUnsupportedOptions(group, sat, mParameters.getSupportedSaturationMode());
        }
        if (brightness != null){
            filterUnsupportedOptions(group, brightness, mParameters.getSupportedBrightnessMode());
        }
        if (contrast != null){
            filterUnsupportedOptions(group, contrast, mParameters.getSupportedContrastMode());
        }
        if (videoSceneMode != null) {
        	filterUnsupportedOptions(group, videoSceneMode, mParameters.getSupportedSceneModes());
        }
        if (videoDuration != null) {
        	CamcorderProfile profile = CamcorderProfile.getMtk(CamcorderProfile.QUALITY_MTK_LOW);
        	CharSequence[] entries = videoDuration.getEntries();
        	if (profile != null) {
		        // Modify video duration settings.
		        // The first entry is for MMS video duration, and we need to fill
		        // in the device-dependent value (in seconds).
		        entries[0] = String.format(Locale.ENGLISH, 
		                entries[0].toString(), profile.duration);
        	} else {
        		entries[0] = String.format(Locale.ENGLISH, 
		                entries[0].toString(), 30);
        	}
        }
        if (eis != null && !"true".equals(mParameters.get("video-stabilization-supported"))) {
        	filterUnsupportedOptions(group, eis, null);
        }

        if (!FeatureOption.MTK_AUDIO_HD_REC_SUPPORT) {
        	removePreference(group, KEY_VIDEO_HD_AUDIO_RECORDING);
        }

        if (!mParameters.isZSDSupported()) {
        	removePreference(group, KEY_CAMERA_ZSD);
        }
        //Mediatek feature end

        // Since the screen could be loaded from different resources, we need
        // to check if the preference is available here
        if (videoQuality != null) {
            filterUnsupportedOptions(group, videoQuality, getMTKSupportedVideoQuality());
        }

        if (pictureSize != null) {
            filterUnsupportedOptions(group, pictureSize, sizeListToStringList(
                    mParameters.getSupportedPictureSizes()));
        }
        if (whiteBalance != null) {
        	if (Util.CMCC) {
				whiteBalance.setEntries(
						mContext.getResources().getTextArray(
								R.array.pref_camera_whitebalance_entries_cmcc));
        	}
            filterUnsupportedOptions(group,
                    whiteBalance, mParameters.getSupportedWhiteBalance());
        }
        if (sceneMode != null) {
            filterUnsupportedOptions(group,
                    sceneMode, mParameters.getSupportedSceneModes());
        }
        if (flashMode != null) {
            filterUnsupportedOptions(group,
                    flashMode, mParameters.getSupportedFlashModes());
        }
        if (focusMode != null) {
            if (mParameters.getMaxNumFocusAreas() == 0) {
                filterUnsupportedOptions(group,
                        focusMode, mParameters.getSupportedFocusModes());
            } else {
                // Remove the focus mode if we can use tap-to-focus.
                removePreference(group, focusMode.getKey());
            }
        }
        if (videoFocusMode != null) {
            filterUnsupportedOptions(group,
                    videoFocusMode, mParameters.getSupportedFocusModes());
        }
        if (videoFlashMode != null) {
            filterUnsupportedOptions(group,
                    videoFlashMode, mParameters.getSupportedFlashModes());
        }
        if (exposure != null) buildExposureCompensation(group, exposure);
        if (cameraIdPref != null) buildCameraId(group, cameraIdPref);

        if (timeLapseInterval != null) resetIfInvalid(timeLapseInterval);
        if (videoEffect != null) {
            initVideoEffect(group, videoEffect);
            resetIfInvalid(videoEffect);
        }
    }

    private void buildExposureCompensation(
            PreferenceGroup group, ListPreference exposure) {
        int max = mParameters.getMaxExposureCompensation();
        int min = mParameters.getMinExposureCompensation();
        if (max == 0 && min == 0) {
            removePreference(group, exposure.getKey());
            return;
        }
        float step = mParameters.getExposureCompensationStep();

        // show only integer values for exposure compensation
        int maxValue = (int) Math.floor(max * step);
        int minValue = (int) Math.ceil(min * step);
        CharSequence entries[] = new CharSequence[maxValue - minValue + 1];
        CharSequence entryValues[] = new CharSequence[maxValue - minValue + 1];
        for (int i = minValue; i <= maxValue; ++i) {
            entryValues[maxValue - i] = Integer.toString(Math.round(i / step));
            StringBuilder builder = new StringBuilder();
            if (i > 0) builder.append('+');
            entries[maxValue - i] = builder.append(i).toString();
        }
        exposure.setEntries(entries);
        exposure.setEntryValues(entryValues);
    }

    private void buildCameraId(
            PreferenceGroup group, IconListPreference preference) {
        int numOfCameras = mCameraInfo.length;
        if (numOfCameras < 2) {
            removePreference(group, preference.getKey());
            return;
        }

        CharSequence[] entryValues = new CharSequence[2];
        for (int i = 0; i < mCameraInfo.length; ++i) {
            int index =
                    (mCameraInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT)
                    ? CameraInfo.CAMERA_FACING_FRONT
                    : CameraInfo.CAMERA_FACING_BACK;
            if (entryValues[index] == null) {
                entryValues[index] = "" + i;
                if (entryValues[((index == 1) ? 0 : 1)] != null) break;
            }
        }
        preference.setEntryValues(entryValues);
    }

    private static boolean removePreference(PreferenceGroup group, String key) {
        for (int i = 0, n = group.size(); i < n; i++) {
            CameraPreference child = group.get(i);
            if (child instanceof PreferenceGroup) {
                if (removePreference((PreferenceGroup) child, key)) {
                    return true;
                }
            }
            if (child instanceof ListPreference &&
                    ((ListPreference) child).getKey().equals(key)) {
                group.removePreference(i);
                return true;
            }
        }
        return false;
    }

    private void filterUnsupportedOptions(PreferenceGroup group,
            ListPreference pref, List<String> supported) {

        // Remove the preference if the parameter is not supported or there is
        // only one options for the settings.
        if (supported == null || supported.size() <= 1) {
            removePreference(group, pref.getKey());
            return;
        }

        pref.filterUnsupported(supported);
        if (pref.getEntries().length <= 1) {
            removePreference(group, pref.getKey());
            return;
        }

        resetIfInvalid(pref);
    }

    private void resetIfInvalid(ListPreference pref) {
        // Set the value to the first entry if it is invalid.
        String value = pref.getValue();
        if (pref.findIndexOfValue(value) == NOT_FOUND) {
            pref.setValueIndex(0);
        }
    }

    private static List<String> sizeListToStringList(List<Size> sizes) {
        ArrayList<String> list = new ArrayList<String>();
        for (Size size : sizes) {
            list.add(String.format("%dx%d", size.width, size.height));
        }
        return list;
    }

    public static void upgradeLocalPreferences(SharedPreferences pref) {
        int version;
        try {
            version = pref.getInt(KEY_LOCAL_VERSION, 0);
        } catch (Exception ex) {
            version = 0;
        }
        if (version == CURRENT_LOCAL_VERSION) return;

        SharedPreferences.Editor editor = pref.edit();
        if (version == 1) {
            // We use numbers to represent the quality now. The quality definition is identical to
            // that of CamcorderProfile.java.
            editor.remove("pref_video_quality_key");
        }
        editor.putInt(KEY_LOCAL_VERSION, CURRENT_LOCAL_VERSION);
        editor.apply();
    }

    public static void upgradeGlobalPreferences(SharedPreferences pref) {
        int version;
        try {
            version = pref.getInt(KEY_VERSION, 0);
        } catch (Exception ex) {
            version = 0;
        }
        if (version == CURRENT_VERSION) return;

        SharedPreferences.Editor editor = pref.edit();
        if (version == 0) {
            // We won't use the preference which change in version 1.
            // So, just upgrade to version 1 directly
            version = 1;
        }
        if (version == 1) {
            // Change jpeg quality {65,75,85} to {normal,fine,superfine}
            String quality = pref.getString(KEY_JPEG_QUALITY, "85");
            if (quality.equals("65")) {
                quality = "normal";
            } else if (quality.equals("75")) {
                quality = "fine";
            } else {
                quality = "superfine";
            }
            editor.putString(KEY_JPEG_QUALITY, quality);
            version = 2;
        }
        if (version == 2) {
            editor.putString(KEY_RECORD_LOCATION,
                    pref.getBoolean(KEY_RECORD_LOCATION, false)
                    ? RecordLocationPreference.VALUE_ON
                    : RecordLocationPreference.VALUE_NONE);
            version = 3;
        }
        if (version == 3) {
            // Just use video quality to replace it and
            // ignore the current settings.
            editor.remove("pref_camera_videoquality_key");
            editor.remove("pref_camera_video_duration_key");
        }

        editor.putInt(KEY_VERSION, CURRENT_VERSION);
        editor.apply();
    }

    public static int readPreferredCameraId(SharedPreferences pref) {
        return Integer.parseInt(pref.getString(KEY_CAMERA_ID, "0"));
    }

    public static void writePreferredCameraId(SharedPreferences pref,
            int cameraId) {
        Editor editor = pref.edit();
        editor.putString(KEY_CAMERA_ID, Integer.toString(cameraId));
        editor.apply();
    }

    public static int readExposure(ComboPreferences preferences) {
        String exposure = preferences.getString(
                CameraSettings.KEY_EXPOSURE,
                EXPOSURE_DEFAULT_VALUE);
        try {
            return Integer.parseInt(exposure);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid exposure: " + exposure);
        }
        return 0;
    }

    public static int readEffectType(SharedPreferences pref) {
        String effectSelection = pref.getString(KEY_VIDEO_EFFECT, "none");
        if (effectSelection.equals("none")) {
            return EffectsRecorder.EFFECT_NONE;
        } else if (effectSelection.startsWith("goofy_face")) {
            return EffectsRecorder.EFFECT_GOOFY_FACE;
        } else if (effectSelection.startsWith("backdropper")) {
            return EffectsRecorder.EFFECT_BACKDROPPER;
        }
        Log.e(TAG, "Invalid effect selection: " + effectSelection);
        return EffectsRecorder.EFFECT_NONE;
    }

    public static Object readEffectParameter(SharedPreferences pref) {
        String effectSelection = pref.getString(KEY_VIDEO_EFFECT, "none");
        if (effectSelection.equals("none")) {
            return null;
        }
        int separatorIndex = effectSelection.indexOf('/');
        String effectParameter =
                effectSelection.substring(separatorIndex + 1);
        if (effectSelection.startsWith("goofy_face")) {
            if (effectParameter.equals("squeeze")) {
                return EffectsRecorder.EFFECT_GF_SQUEEZE;
            } else if (effectParameter.equals("big_eyes")) {
                return EffectsRecorder.EFFECT_GF_BIG_EYES;
            } else if (effectParameter.equals("big_mouth")) {
                return EffectsRecorder.EFFECT_GF_BIG_MOUTH;
            } else if (effectParameter.equals("small_mouth")) {
                return EffectsRecorder.EFFECT_GF_SMALL_MOUTH;
            } else if (effectParameter.equals("big_nose")) {
                return EffectsRecorder.EFFECT_GF_BIG_NOSE;
            } else if (effectParameter.equals("small_eyes")) {
                return EffectsRecorder.EFFECT_GF_SMALL_EYES;
            }
        } else if (effectSelection.startsWith("backdropper")) {
            // Parameter is a string that either encodes the URI to use,
            // or specifies 'gallery'.
            return effectParameter;
        }

        Log.e(TAG, "Invalid effect selection: " + effectSelection);
        return null;
    }


    public static void restorePreferences(Context context,
            ComboPreferences preferences, Parameters parameters) {
        int currentCameraId = readPreferredCameraId(preferences);

        // Clear the preferences of both cameras.
        int backCameraId = CameraHolder.instance().getBackCameraId();
        if (backCameraId != -1) {
            preferences.setLocalId(context, backCameraId);
            Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }
        int frontCameraId = CameraHolder.instance().getFrontCameraId();
        if (frontCameraId != -1) {
            preferences.setLocalId(context, frontCameraId);
            Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }

        // Switch back to the preferences of the current camera. Otherwise,
        // we may write the preference to wrong camera later.
        preferences.setLocalId(context, currentCameraId);

        upgradeGlobalPreferences(preferences.getGlobal());
        upgradeLocalPreferences(preferences.getLocal());

        // Write back the current camera id because parameters are related to
        // the camera. Otherwise, we may switch to the front camera but the
        // initial picture size is that of the back camera.
        initialCameraPictureSize(context, parameters);
        writePreferredCameraId(preferences, currentCameraId);
    }

    private ArrayList<String> getSupportedVideoQuality() {
        ArrayList<String> supported = new ArrayList<String>();
        // Check for supported quality
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_1080P));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_720P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_720P));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_480P));
        }

        return supported;
    }

    private void initVideoEffect(PreferenceGroup group, ListPreference videoEffect) {
        CharSequence[] values = videoEffect.getEntryValues();

        boolean goofyFaceSupported =
                EffectsRecorder.isEffectSupported(EffectsRecorder.EFFECT_GOOFY_FACE);
        boolean backdropperSupported =
                EffectsRecorder.isEffectSupported(EffectsRecorder.EFFECT_BACKDROPPER) &&
                mParameters.isAutoExposureLockSupported() &&
                mParameters.isAutoWhiteBalanceLockSupported();

        ArrayList<String> supported = new ArrayList<String>();
        for (CharSequence value : values) {
            String effectSelection = value.toString();
            if (!goofyFaceSupported && effectSelection.startsWith("goofy_face")) continue;
            if (!backdropperSupported && effectSelection.startsWith("backdropper")) continue;
            supported.add(effectSelection);
        }

        filterUnsupportedOptions(group, videoEffect, supported);
    }
    
    //Mediatek merge begin
    public static void initialCameraPictureSize(
            Context context, Parameters parameters, int orientation) {
        // When launching the camera app first time, we will set the picture
        // size to the first one in the list defined in "arrays.xml" and is also
        // supported by the driver.
        List<Size> supported = parameters.getSupportedPictureSizes();
        if (supported == null) return;
        for (String candidate : context.getResources().getStringArray(
                R.array.pref_camera_picturesize_entryvalues)) {
            if (setCameraPictureSize(candidate, supported, parameters, orientation)) {
                SharedPreferences.Editor editor = ComboPreferences
                        .get(context).edit();
                editor.putString(KEY_PICTURE_SIZE, candidate);
                editor.apply();
                return;
            }
        }
        Log.e(TAG, "No supported picture size found");
    }

    public static boolean setCameraPictureSize(
            String candidate, List<Size> supported, Parameters parameters, int orientation) {
        int index = candidate.indexOf('x');
        if (index == NOT_FOUND) return false;
        int width = Integer.parseInt(candidate.substring(0, index));
        int height = Integer.parseInt(candidate.substring(index + 1));
        Xlog.i(TAG, "setCameraPictureSize, orientation: " + orientation);
        if (orientation == 0 || orientation == 180) {
	        for (Size size: supported) {
	            if (size.width == width && size.height == height) {
	                parameters.setPictureSize(height, width);
	                return true;
	            }
	        }
        } else {
        	for (Size size: supported) {
	            if (size.width == width && size.height == height) {
	                parameters.setPictureSize(width, height);
	                return true;
	            }
	        }
        }
        return false;
    }

    private ArrayList<String> getMTKSupportedVideoQuality() {
        ArrayList<String> supported = new ArrayList<String>();
        // Check for supported quality
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_MTK_LOW)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_MTK_LOW));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_MTK_MEDIUM)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_MTK_MEDIUM));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_MTK_HIGH)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_MTK_HIGH));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_MTK_FINE)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_MTK_FINE));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_MTK_1080P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_MTK_1080P));
        }

        return supported;
    }
    //Mediatek merge end
}
