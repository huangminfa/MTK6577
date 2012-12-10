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

package com.mediatek.audioprofile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

/**
 * AudioProfileManager provides access to audioprofile mode control.
 * <p>
 * Use <code>Context.getSystemService(Context.AUDIOPROFILE_SERVICE)</code> to get
 * an instance of this class.
 */
public class AudioProfileManager {
    private static final String TAG = "AudioProfileManager";
    private final Context mContext;
    private static IAudioProfileService mService;
    
    /**
     * The profile settings that an audio profile should have.
     */
    public enum ProfileSettings {
        ringer_stream, notification_stream, videocall_Stream,
        ringer_volume, notification_volume, alarm_volume,
        vibration_enabled, dtmftone_enabled, soundeffect_enbled, hapticfeedback_enabled, lockscreensound_enabled
    }
    
    /**
     * The scenario that an audio profile is designed for.
     */
    public enum Scenario {
        GENERAL, SILENT, MEETING, OUTDOOR, CUSTOM
    }
    
    /** The max AudioProfile count. */
    public static final int MAX_PROFILES_COUNT = 10;
    
    /** The predefined AudioProfile count. */
    public static final int PREDEFINED_PROFILES_COUNT = 4;
    
    /** The audio stream for the phone ring. */
    public static final int STREAM_RING = AudioManager.STREAM_RING;

    /** The audio stream for notifications. */
    public static final int STREAM_NOTIFICATION = AudioManager.STREAM_NOTIFICATION;

    /** The audio stream for alarm. */
    public static final int STREAM_ALARM = AudioManager.STREAM_ALARM;
    
    /**
     * Type that refers to sounds that are used for the phone ringer.
     *
     * @see #getRingtoneUri(Context, int)
     * @see #setRingtoneUri(Context, int, Uri)
     */
    public static final int TYPE_RINGTONE = RingtoneManager.TYPE_RINGTONE;

    /**
     * Type that refers to sounds that are used for notifications.
     * 
     * @see #getRingtoneUri(Context, int)
     * @see #setRingtoneUri(Context, int, Uri)
     */
    public static final int TYPE_NOTIFICATION = RingtoneManager.TYPE_NOTIFICATION;

    /**
     * Type that refers to sounds that are used for the video call.
     *
     * @see #getRingtoneUri(Context, int)
     * @see #setRingtoneUri(Context, int, Uri)
     */
    public static final int TYPE_VIDEO_CALL = RingtoneManager.TYPE_VIDEO_CALL;
    
    /** Broadcast intent action indicating that the audio profile has changed. */
    public static final String ACTION_PROFILE_CHANGED = 
        "com.mediatek.audioprofile.ACTION_PROFILE_CHANGED";

    /** The scenario type of new audio profile. */
    public static final String EXTRA_PROFILE_SCENARIO = 
        "com.mediatek.audioprofile.EXTRA_PROFILE_SCENARIO";

    /** The prefixe of audio profile keys. */
    public static final String PROFILE_PREFIX = "mtk_audioprofile_";

    /** The suffixes of the settings' keys. */
    private static final String SUFFIX_RINGER_URI = "_ringtone";
    private static final String SUFFIX_NOTIFICATION_URI = "_notification_sound";
    private static final String SUFFIX_VIDEO_CALL_URI = "_video_call";
    private static final String SUFFIX_RINGER_VOLUME = "_volume_ring";
    private static final String SUFFIX_ALARM_VOLUME = "_volume_alarm";
    private static final String SUFFIX_NOTIFICATION_VOLUME = "_volume_notification";
    private static final String SUFFIX_VIBRATION = "_vibrate_on";
    private static final String SUFFIX_SOUNDEFFECT = "_sound_effects_enabled";
    private static final String SUFFIX_DTMFTONE = "_dtmf_tone_enabled";
    private static final String SUFFIX_HAPTICFEEDBACK = "_haptic_feedback_enabled";
    private static final String SUFFIX_LOCK_SCRREN = "_lockscreen_sounds_enabled";
    private static final String SUFFIX_NOTIFICATION_USE_RING = "_notifications_use_ring_volume";
    protected static final String SUFFIX_NAME = "_name";
    protected static final String SUFFIX_KEY = "_key";

    /** The key used to store the active audio profile. */
    public static final String KEY_ACTIVE_PROFILE = "mtk_audioprofile_active";

    /** The key used to store the previous active audio profile. */
    public static final String LAST_ACTIVE_PROFILE = "mtk_audioprofile_last_active";
    
    /** The key used to store whether one custom audio profile was deleted when it was set active.*/
    public static final String LAST_ACTIVE_CUSTOM_DELETED = "mtk_audioprofile_custom_deleted";

    /** The key used to store the default ringtone of voice call. */
    public static final String KEY_DEFAULT_RINGTONE = "mtk_audioprofile_default_ringtone";

    /** The key used to store the default notification sound. */
    public static final String KEY_DEFAULT_NOTIFICATION = "mtk_audioprofile_default_notification";

    /** The key used to store the default ringtone of video call. */
    public static final String KEY_DEFAULT_VIDEO_CALL = "mtk_audioprofile_default_video_call";

    /** Volume returned when unSupported stream type was passed in. */
    public static final int UNSUPPORT_STREAM_VOLUME = 0;

    /** Default ringtone for normal calls. */
    public static final Uri DEFAULT_RINGER_STREAM_URI = Settings.System.DEFAULT_RINGTONE_URI;

    /** Default ringtone for notification. */
    public static final Uri DEFAULT_NOTIFICATION_STREAM_URI = Settings.System.DEFAULT_NOTIFICATION_URI;

    /** Default ringtone for video calls. */
    public static final Uri DEFAULT_VIDEO_STREAM_URI = Settings.System.DEFAULT_VIDEO_CALL_URI;

    // add for CMCC project
    private static final String OP = android.os.SystemProperties.get("ro.operator.optr");
    protected static final boolean IS_CMCC = ("OP01").equals(OP);
    
    /** Default values of ringer volume for different audio profiles. */
    private static final int DEFAULT_RINGER_VOLUME_GENERAL = 4;
    private static final int DEFAULT_RINGER_VOLUME_SILENT = 0;
    private static final int DEFAULT_RINGER_VOLUME_MEETING = 0;
    private static final int DEFAULT_RINGER_VOLUME_OUTDOOR = IS_CMCC ? 6 : 7;

    /** Default values of notification volume for different audio profiles. */
    private static final int DEFAULT_NOTIFICATION_VOLUME_GENERAL = 4;
    private static final int DEFAULT_NOTIFICATION_VOLUME_SILENT = 0;
    private static final int DEFAULT_NOTIFICATION_VOLUME_MEETING = 0;
    private static final int DEFAULT_NOTIFICATION_VOLUME_OUTDOOR = IS_CMCC ? 6 : 7;

    /** Default values of alarm volume for different audio profiles. */
    private static final int DEFAULT_ALARM_VOLUME_GENERAL = 4;
    private static final int DEFAULT_ALARM_VOLUME_SILENT = 0;
    private static final int DEFAULT_ALARM_VOLUME_MEETING = IS_CMCC ? 4 : 0;
    private static final int DEFAULT_ALARM_VOLUME_OUTDOOR = IS_CMCC ? 6 : 7;

    /** Default values of vibration for different audio profiles. */
    private static final boolean DEFAULT_VIBRATION_GENERAL = false;
    private static final boolean DEFAULT_VIBRATION_SILENT = false;
    private static final boolean DEFAULT_VIBRATION_MEETING = true;
    private static final boolean DEFAULT_VIBRATION_OUTDOOR = true;

    /**
     * Default values that indicate whether the audible DTMF tone should be
     * played by the dialer when dialing.
     */
    private static final boolean DEFAULT_DTMFTONE_GENERAL = true;
    private static final boolean DEFAULT_DTMFTONE_SILENT = false;
    private static final boolean DEFAULT_DTMFTONE_MEETING = false;
    private static final boolean DEFAULT_DTMFTONE_OUTDOOR = true;

    /**
     * Default values of sound effect(Key clicks, lid open/close...) for
     * different audio profiles.
     */
    private static final boolean DEFAULT_SOUNDEFFECT_GENERAL = false;
    private static final boolean DEFAULT_SOUNDEFFECT_SILENT = false;
    private static final boolean DEFAULT_SOUNDEFFECT_MEETING = false;
    private static final boolean DEFAULT_SOUNDEFFECT_OUTDOOR = false;
    
    /** Default values that indicate whether the lock screen sound are enabled. */
    private static final boolean DEFAULT_LOCK_SCREEN_GENERAL = true;
    private static final boolean DEFAULT_LOCK_SCREEN_SILENT = false;
    private static final boolean DEFAULT_LOCK_SCREEN_MEETING = false;
    private static final boolean DEFAULT_LOCK_SCREEN_OUTDOOR = true;

    /** Default values that indicate whether the haptic feedback are enabled. */
    private static final boolean DEFAULT_HAPTIC_FEEDBACK_GENERAL = IS_CMCC ? false : true;
    private static final boolean DEFAULT_HAPTIC_FEEDBACK_SILENT = false;
    private static final boolean DEFAULT_HAPTIC_FEEDBACK_MEETING = IS_CMCC ? true : false;
    private static final boolean DEFAULT_HAPTIC_FEEDBACK_OUTDOOR = true;
    
    
    // Default AudioProfile states
    private static final HashMap<Integer, AudioProfileState> DEFAULTSTATES;

    static {
        DEFAULTSTATES = new HashMap<Integer, AudioProfileState>(4);
        Uri[] defaultUri = new Uri[3];
        int[] defaultVolume = new int[3];
        boolean[] defaultEnabled = new boolean[5];
       
        // Ringer,video Call,Notification default uri for general profile
        defaultUri[0] = DEFAULT_RINGER_STREAM_URI;
        defaultUri[1] = DEFAULT_NOTIFICATION_STREAM_URI;
        defaultUri[2] = DEFAULT_VIDEO_STREAM_URI;
        // Ringer,alarm,Notification default volume for general profile
        defaultVolume[0] = DEFAULT_RINGER_VOLUME_GENERAL;
        defaultVolume[1] = DEFAULT_NOTIFICATION_VOLUME_GENERAL;
        defaultVolume[2] = DEFAULT_ALARM_VOLUME_GENERAL;
        // Vibration,Sound Effect,DtmfTone,HapticFeedback,LockScreen Sound default enabled for general profile
        defaultEnabled[0] = DEFAULT_VIBRATION_GENERAL;
        defaultEnabled[1] = DEFAULT_DTMFTONE_GENERAL;
        defaultEnabled[2] = DEFAULT_SOUNDEFFECT_GENERAL;
        defaultEnabled[3] = DEFAULT_LOCK_SCREEN_GENERAL;
        defaultEnabled[4] = DEFAULT_HAPTIC_FEEDBACK_GENERAL;
        // Init general state and push it to DEFAULTSTATES
        AudioProfileState generalState = new AudioProfileState(defaultUri, defaultVolume, defaultEnabled);
        DEFAULTSTATES.put(Scenario.GENERAL.ordinal(), generalState);

        // Ringer,video Call,Notification default uri for silent profile
        defaultUri[0] = DEFAULT_RINGER_STREAM_URI;
        defaultUri[1] = DEFAULT_NOTIFICATION_STREAM_URI;
        defaultUri[2] = DEFAULT_VIDEO_STREAM_URI;
        // Ringer,alarm,Notification default volume for silent profile
        defaultVolume[0] = DEFAULT_RINGER_VOLUME_SILENT;
        defaultVolume[1] = DEFAULT_NOTIFICATION_VOLUME_SILENT;
        defaultVolume[2] = DEFAULT_ALARM_VOLUME_SILENT;
        // Vibration,Sound Effect,DtmfTone,HapticFeedback,LockScreen Sound default enabled for silent profile
        defaultEnabled[0] = DEFAULT_VIBRATION_SILENT;
        defaultEnabled[1] = DEFAULT_DTMFTONE_SILENT;
        defaultEnabled[2] = DEFAULT_SOUNDEFFECT_SILENT;
        defaultEnabled[3] = DEFAULT_LOCK_SCREEN_SILENT;
        defaultEnabled[4] = DEFAULT_HAPTIC_FEEDBACK_SILENT;
        // Init silent state and push it to DEFAULTSTATES
        AudioProfileState silentState = new AudioProfileState(defaultUri, defaultVolume, defaultEnabled);
        DEFAULTSTATES.put(Scenario.SILENT.ordinal(), silentState);

        // Ringer,video Call,Notification default uri for meeting profile
        defaultUri[0] = DEFAULT_RINGER_STREAM_URI;
        defaultUri[1] = DEFAULT_NOTIFICATION_STREAM_URI;
        defaultUri[2] = DEFAULT_VIDEO_STREAM_URI;
        // Ringer,alarm,Notification default volume for meeting profile
        defaultVolume[0] = DEFAULT_RINGER_VOLUME_MEETING;
        defaultVolume[1] = DEFAULT_NOTIFICATION_VOLUME_MEETING;
        defaultVolume[2] = DEFAULT_ALARM_VOLUME_MEETING;
        // Vibration,Sound Effect,DtmfTone,HapticFeedback,LockScreen Sound default enabled for meeting profile
        defaultEnabled[0] = DEFAULT_VIBRATION_MEETING;
        defaultEnabled[1] = DEFAULT_DTMFTONE_MEETING;
        defaultEnabled[2] = DEFAULT_SOUNDEFFECT_MEETING;
        defaultEnabled[3] = DEFAULT_LOCK_SCREEN_MEETING;
        defaultEnabled[4] = DEFAULT_HAPTIC_FEEDBACK_MEETING;
        // Init meeting state and push it to DEFAULTSTATES
        AudioProfileState meetingState = new AudioProfileState(defaultUri, defaultVolume, defaultEnabled);
        DEFAULTSTATES.put(Scenario.MEETING.ordinal(), meetingState);

        // Ringer,video Call,Notification default uri for outdoor profile
        defaultUri[0] = DEFAULT_RINGER_STREAM_URI;
        defaultUri[1] = DEFAULT_NOTIFICATION_STREAM_URI;
        defaultUri[2] = DEFAULT_VIDEO_STREAM_URI;
        // Ringer,alarm,Notification default volume for outdoor profile
        defaultVolume[0] = DEFAULT_RINGER_VOLUME_OUTDOOR;
        defaultVolume[1] = DEFAULT_NOTIFICATION_VOLUME_OUTDOOR;
        defaultVolume[2] = DEFAULT_ALARM_VOLUME_OUTDOOR;
        // Vibration,Sound Effect,DtmfTone,HapticFeedback,LockScreen Sound default enabled for outdoor profile
        defaultEnabled[0] = DEFAULT_VIBRATION_OUTDOOR;
        defaultEnabled[1] = DEFAULT_DTMFTONE_OUTDOOR;
        defaultEnabled[2] = DEFAULT_SOUNDEFFECT_OUTDOOR;
        defaultEnabled[3] = DEFAULT_LOCK_SCREEN_OUTDOOR;
        defaultEnabled[4] = DEFAULT_HAPTIC_FEEDBACK_OUTDOOR;
        // Init outdoor state and push it to DEFAULTSTATES
        AudioProfileState outdoorState = new AudioProfileState(defaultUri, defaultVolume, defaultEnabled);
        DEFAULTSTATES.put(Scenario.OUTDOOR.ordinal(), outdoorState);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Construction and get AudioProfileSevices
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @hide
     */
    public AudioProfileManager(Context context) {
        mContext = context;
    }
    
    private static IAudioProfileService getService()
    {
        if (mService != null) {
            return mService;
        }
        IBinder binder = ServiceManager.getService(Context.AUDIOPROFILE_SERVICE);
        mService = IAudioProfileService.Stub.asInterface(binder);
        return mService;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Get default state method
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Gets the default settings .
     * 
     * @param profileKey
     *            The key of the profile which's default settings to be
     *            retrieved.
     * @return The corresponding default settings of profiles except that 
     *         it returns the GENERAL profile's default setting when scenario is CUSTOM.
     */
    public AudioProfileState getDefaultState(String profileKey) {
        Scenario scenario = getScenario(profileKey);
        if(scenario == null) {
            Log.w(TAG, "getDefaultState: get null scenario and use custom scenario default!");
            scenario = Scenario.CUSTOM;
        }
        if (IS_CMCC) {
            Log.d(TAG, "getDefaultState from CMCC!");
        } else {
            Log.d(TAG, "getDefaultState from default!");
        }
        int index = scenario.ordinal();
        if(index >= DEFAULTSTATES.size()) {
            return DEFAULTSTATES.get(0);
        } else {
            return DEFAULTSTATES.get(index);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Get key methods
    ///////////////////////////////////////////////////////////////////////////////////////////////
  
    /**
     * Gets the key for persisting setting that indicates whether phone should
     * vibrate for incoming calls.
     * 
     * @param profileKey
     *            The key of the profile.
     * @return The key for persisting setting.
     */
    public static String getVibrationKey(String profileKey) {
        if(profileKey != null) {
            return profileKey + SUFFIX_VIBRATION;
        }
        return null;
    }

    /**
     * Gets the key for persisting setting that indicates whether sound should be
     * played when making screen selection.
     * 
     * @param profileKey
         *            The key of the profile.
     * @return The key for persisting setting.
     * 
     */
    public static String getSoundEffectKey(String profileKey) {
        if (profileKey != null) {
            return profileKey + SUFFIX_SOUNDEFFECT;
        }
        return null;
    }

    /**
     * Gets the key for persisting setting that indicates whether sound should
     * be played when using dial pad.
     * 
     * @param profileKey
         *            The key of the profile.
     * @return The key for persisting setting.
     */
    public static String getDtmfToneKey(String profileKey) {
        if (profileKey != null) {
            return profileKey + SUFFIX_DTMFTONE;
        }
        return null;
    }

    /**
     * Gets the key for persisting the setting that indicates whether the phone
     * should vibrate when pressing soft keys and on certain UI interactions.
     * 
     * @param profileKey
         *            The key of the profile..
     * @return The key for persisting setting.
     */
    public static String getHapticKey(String profileKey) {
        if (profileKey != null) {
            return profileKey + SUFFIX_HAPTICFEEDBACK;
        }
        return null;
    }

    /**
     * Gets the key for persisting volume.
     * 
     * @param profileKey
         *            The key of the profile.
     * @param type
     *            The stream type whose volume index to be persisted.
     *            One of {@link AudioProfile#STREAM_RING},
     *            {@link AudioProfile#STREAM_NOTIFICATION},
     *            or {@link AudioProfile#STREAM_ALARM}.
     * @return The key for persisting setting.
     */
    public static String getStreamVolumeKey(String profileKey, int type) {
        if (profileKey != null) {
            String volumeKey = null;
            switch (type) {
                case STREAM_RING:
                    volumeKey = profileKey + SUFFIX_RINGER_VOLUME;
                    break;
                    
                case STREAM_NOTIFICATION:
                    volumeKey = profileKey + SUFFIX_NOTIFICATION_VOLUME;
                    break;
                    
                case STREAM_ALARM:
                    volumeKey = profileKey + SUFFIX_ALARM_VOLUME;
                    break;

                default:
                    Log.e(TAG, "getStreamVolumeKey with unsupport type!");
                    return null;
            }
            return volumeKey;
        } else {
            Log.e(TAG, "getStreamVolumeKey with null profile key!");
            return null;
        }
    }

    /**
     * Gets the key for persisting stream volume.
     * 
     * @param profileKey
         *            The key of the profile.
     * @param type
     *            The type whose default sound to be persisted. One of
     *            {@link AudioProfile#TYPE_RINGTONE},
     *            {@link AudioProfile#TYPE_NOTIFICATION},
     *            or {@link AudioProfile#TYPE_VOICE_CALL}.
     * @return The key for persisting setting.
     */
    public static String getStreamUriKey(String profileKey, int type) {
        if (profileKey != null) {
            String uriKey = null;
            switch (type) {
                case TYPE_RINGTONE:
                    uriKey = profileKey + SUFFIX_RINGER_URI;
                    break;
                case TYPE_NOTIFICATION:
                    uriKey = profileKey + SUFFIX_NOTIFICATION_URI;
                    break;
                case TYPE_VIDEO_CALL:
                    uriKey = profileKey + SUFFIX_VIDEO_CALL_URI;
                    break;
                default:
                    Log.e(TAG, "getStreamUriKey with unsupport type!");
                    return null;
                }
            
            return uriKey;
        } else {
            Log.e(TAG, "getStreamUriKey with null profile key!");
            return null;
        }
    }
    
    /**
     * Gets the keys to save the default ringtone for predefined scenarios except for silent.
     * 
     * @param type Stream type. One of
         *            {@link AudioProfile#TYPE_RINGTONE},
         *            {@link AudioProfile#TYPE_NOTIFICATION},
         *            or {@link AudioProfile#TYPE_VOICE_CALL}.
     * @return
     */
    public static List<String> getStreamUriKeys(int type) {
        List<String> keys = new ArrayList<String>();
        for (Scenario scenario : Scenario.values()) {
            if (!scenario.equals(Scenario.SILENT) && !scenario.equals(Scenario.CUSTOM)) {
                String prefix = PROFILE_PREFIX + scenario.toString().toLowerCase();
                String key = getStreamUriKey(prefix, type);
                if (key != null) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    /**
     * Gets the key for persisting the setting that indicates whether sounds
     * should be played when the keyguard is shown and dismissed.
     * 
     * @param profileKey
         *            The key of the profile.
     * @return The key for persisting setting.
     */
    public static String getLockScreenKey(String profileKey) {
        if(profileKey != null) {
            return profileKey + SUFFIX_LOCK_SCRREN;
        }
        return null;
    }

    /**
     * Gets the key for persisting the setting that indicates whether
     * notification use ring volume.
     * 
     * @param profileKey
         *            The key of the profile.
     * @return The key for persisting setting.
     */
    public static String getNotificationUseRingKey(String profileKey) {
        if(profileKey != null) {
            return profileKey + SUFFIX_NOTIFICATION_USE_RING;
        }
        return null;
    }
    
    /**
     * Gets the key for persisting the profile's name.
     * 
     * @param profileKey
     *            The key of the profile.
     * @return The key for persisting profile's name.
     */
    public static String getProfileNameKey(String profileKey) {
        if(profileKey != null) {
            return profileKey + SUFFIX_NAME;
        }
        return null;
    }
    
    /**
     * Gets the key for persisting the profile's key.
     * 
     * @param profileKey
     *            The key of the profile.
     * @return
     */
    public static String getKey(String profileKey) {
        if(profileKey != null) {
            return profileKey + SUFFIX_KEY;
        }
        return null;
    }
    
    /**
     * Gets the profile's key from scenario type.
     * 
     * @param scenario The scenario type.
     * @return The scenario's profile key. If scenario is one of four predefine types of scenario,
     * the profile key can be used immediately, is it is custom one, the profile key should be add
     * more information to mark off custom profiles.
     */
    public static String getProfileKey(Scenario scenario) {
        if(scenario == null) {
            Log.e(TAG, "getProfileKey with null scenario!");
            return null;
        }
        return PROFILE_PREFIX + scenario.toString().toLowerCase();
    }
    
    /**
     * Gets the scenario type from profile's key.
     * 
     * @param profileKey The profile's key.
     * @return The scenario type of this profile. If the key doesn't contains 
     *         the scenario, returns {@link Scenario#CUSTOM}.
     */
    public static Scenario getScenario(String profileKey) {
        if(profileKey == null) {
            Log.w(TAG, "getScenario: Null key! Return CUSTOM as default!");
            return Scenario.CUSTOM;
        }
        
        int keyLen = profileKey.length();
        int startIndex = PROFILE_PREFIX.length();
        if(keyLen < startIndex) {
            Log.w(TAG, "getScenario: Invalid key :" + profileKey + ",Return CUSTOM as default!");
            return Scenario.CUSTOM;
            }
        
        int endIndex = profileKey.indexOf('_', startIndex);
                
        String scenarioStr = profileKey.substring(startIndex,
                endIndex == -1 ? profileKey.length() : endIndex);
        try {
            return Scenario.valueOf(scenarioStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Can not convert string " 
                    + (scenarioStr == null ? "null" : scenarioStr) 
                    + " to Scenario type!");
            return Scenario.CUSTOM;
        }
    }
    
    /**
     * Gets all the keys used to store the settings of one audio profile.
     * 
     * @param profileKey Audio profile key.
     * @return
     */
    public static List<String> getAllKeys(String profileKey) {
        if(profileKey == null) {
            Log.e(TAG, "getAllKeys: Null profileKey!");
            return null;
        }
        List<String> keys = new ArrayList<String>(14);
        keys.add(getKey(profileKey));
        keys.add(getProfileNameKey(profileKey));
        keys.add(getNotificationUseRingKey(profileKey));
        keys.add(getLockScreenKey(profileKey));
        keys.add(getHapticKey(profileKey));
        keys.add(getDtmfToneKey(profileKey));
        keys.add(getSoundEffectKey(profileKey));
        keys.add(getVibrationKey(profileKey));
        keys.add(getStreamVolumeKey(profileKey, STREAM_ALARM));
        keys.add(getStreamVolumeKey(profileKey, STREAM_NOTIFICATION));
        keys.add(getStreamVolumeKey(profileKey, STREAM_RING));
        keys.add(getStreamUriKey(profileKey, TYPE_NOTIFICATION));
        keys.add(getStreamUriKey(profileKey, TYPE_RINGTONE));
        keys.add(getStreamUriKey(profileKey, TYPE_VIDEO_CALL));
        return keys;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Set active profile, add profile, delete profile and reset profiles
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets the active profile with given profile key.
     * 
     * @param profileKey The key of the profile that set to be active.
     */
    public void setActiveProfile(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "setActiveProfile with null profile key!");
            return;
        }
        
        Log.d(TAG, "setActiveProfile: profileKey = " + profileKey);
        IAudioProfileService service = getService();
        try {
            service.setActiveProfile(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setActiveProfile", e);
        }
    }
    
    /**
     * Adds a new {@link Scenario#CUSTOM} type profile.
     * 
     * @return The new profile key.
     */
    public String addProfile() {
        IAudioProfileService service = getService();
        try {
            return service.addProfile();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setActiveProfile", e);
            return null;
        }
    }
    
    /**
     * Deletes a {@link Scenario#CUSTOM} type profile.
     * 
     * @param profileKey The key of the profile that to be deleted.
     * @return True if delete succeed, otherwise false.
     */
    public boolean deleteProfile(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "deleteProfile with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.deleteProfile(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in deleteProfile", e);
            return false;
        }
    }
    
    /**
     * Reset all profiles.
     */
    public void reset() {
        IAudioProfileService service = getService();
        try {
            service.reset();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in reset", e);
        }
    }
    
    /**
     * Gets the number of current existing profiles. 
     * Include the predefined and custom ones.
     * 
     * @return The number of existing profiles.
     */
    public int getProfileCount() {
        IAudioProfileService service = getService();
        try {
            return service.getProfileCount();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in deleteProfile", e);
            return MAX_PROFILES_COUNT;
        }
    }
    
    /**
     * Gets the all existed profiles' keys.
     * 
     * @return The existed profiles' keys.
     */
    public List<String> getAllProfileKeys() {
        IAudioProfileService service = getService();
        try {
            return service.getAllProfileKeys();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getAllProfileKeys", e);
            return null;
        }
    }

    
    
    /**
     * Gets predefined profiles' keys.
     * 
     * @return The predefined profiles' keys.
     */
    public List<String> getPredefinedProfileKeys() {
        IAudioProfileService service = getService();
        try {
            return service.getPredefinedProfileKeys();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getPredefinedProfileKeys", e);
            return null;
        }
    }
    
    /**
     * Gets customized profiles' keys.
     * 
     * @return The customized profiles' keys.
     */
    public List<String> getCustomizedProfileKeys() {
        IAudioProfileService service = getService();
        try {
            return service.getCustomizedProfileKeys();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getCustomizedProfileKeys", e);
            return null;
        }
    }
    

    /**
     * Checks out whether the name existed.
     * 
     * @param name The name to be checked.
     * @return True if the specified name had existed or if the name 
     *         is null or empty, false otherwise.
     */
    public boolean isNameExist(String name) {
        if(name == null || name.equals("")) {
            Log.w(TAG, "isNameExist: Null or empty name!");
            return true;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.isNameExist(name);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setActiveProfile", e);
            return true;
        }
    }
    
    /**
     * Gets the key of active profile.
     * 
     * @return The key of the active profile.
     * 
     */
    public String getActiveProfileKey() {
        IAudioProfileService service = getService();
        try {
            return service.getActiveProfileKey();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getActiveProfileKey", e);
            return null;
        }
    }
    
    /**
     * Gets the key of previous non-silent active profile.
     * 
     * @return The key of last non-silent active profile.
     */
    public String getLastActiveProfileKey() {
        IAudioProfileService service = getService();
        try {
            return service.getLastActiveProfileKey();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getLastActiveProfileKey", e);
            return null;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Get methods to get profile settings with given profile key from persisted profile states
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Gets the {@link Uri} of the default sound for a given ring tone type.
     *
     * @param profileKey The profile key ringtone uri is returned.
     * @param type The type whose default sound should be set. One of
     *            {@link #TYPE_RINGTONE}, {@link #TYPE_NOTIFICATION}
     *            or {@link #TYPE_VIDEO_CALL}.
     * @returns A {@link Uri} pointing to the default sound to set.
     * @see #setRingtoneUri(Context, int, Uri)
     */
    public Uri getRingtoneUri(String profileKey, int type) {
        if (profileKey == null) {
            Log.e(TAG, "getRingtoneUri with null profile key!");
            return null;
        }
        
        if ((type != TYPE_RINGTONE) && (type != TYPE_NOTIFICATION) && (type != TYPE_VIDEO_CALL)) {
            Log.e(TAG, "getRingtoneUri with unsupport stream type!");
            return null;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getRingtoneUri(profileKey, type);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getRingtoneUri", e);
            return null;
        }
    }
    
    /**
     * Returns the current volume index for a particular stream.
     * @param profileKey The profile key whose volume index is returned.
     * @param streamType The stream type whose volume index is returned. 
     *                   One of {@link #STREAM_RING}, {@link #STREAM_NOTIFICATION}  
     *                   or {@link #STREAM_ALARM}.
     *                   
     * @return The current volume index for the stream.
     * @see #getStreamMaxVolume(int)
     * @see #setStreamVolume(int, int, int)
     */
    public int getStreamVolume(String profileKey, int streamType) {
        if (profileKey == null) {
            Log.e(TAG, "getStreamVolume with null profile key!");
            return UNSUPPORT_STREAM_VOLUME;
        }
        
        if ((streamType != STREAM_RING) && (streamType != STREAM_NOTIFICATION) && (streamType != STREAM_ALARM)) {
            Log.e(TAG, "getStreamVolume with unsupport stream type!");
            return UNSUPPORT_STREAM_VOLUME;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getStreamVolume(profileKey, streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getStreamVolume", e);
            return UNSUPPORT_STREAM_VOLUME;
        }
    }
    
    /**
     * Gets whether the phone should vibrate for incoming calls.
     *
     * @param profileKey The profile key whose DtmfTone whether enabled is returned.
     * @return The current vibration status, if enabled return true, otherwise false.
     * @see #setVibrationEnabled(boolean)
     */
    public boolean getVibrationEnabled(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getVibrationEnabled with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getVibrationEnabled(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getVibrationEnabled", e);
            return false;
        }
    }
    
    /**
     * Gets whether tone should be played when using dial pad with the given profile key.
     * 
     * @param profileKey The profile key whose DtmfTone whether enabled is returned.
     * @return The current DtmfTone status, if enabled return true, otherwise false.
     * @see #setDtmfToneEnabled(boolean)
     */
    public boolean getDtmfToneEnabled(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getDtmfToneEnabled with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getDtmfToneEnabled(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getDtmfToneEnabled", e);
            return false;
        }
    }
    
    /**
     * Gets whether sound should be played when making screen selection.
     * 
     * @param profileKey The profile key whose SoundEffect whether enabled is returned.
     * @return The current SoundEffect status, if enabled return true, otherwise false.
     * @see #setSoundEffectEnabled(boolean)
     */
    public boolean getSoundEffectEnabled(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getSoundEffectEnabled with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getSoundEffectEnabled(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getSoundEffectEnabled", e);
            return false;
        }
    }
    
    /**
     * Gets whether sound should be played when lock or unlock screen.
     * 
     * @param profileKey The profile key whose LockScreen whether enabled is returned.
     * @return The current LockScreen status, if enabled return true, otherwise false.
     * @see #setLockScreenEnabled(String, boolean)
     */
    public boolean getLockScreenEnabled(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getLockScreenEnabled with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getLockScreenEnabled(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getLockScreenEnabled", e);
            return false;
        }
    }
    
    /**
     * Gets whether the phone should vibrate when pressing soft keys and on certain UI interactions.
     * 
     * @param profileKey The profile key whose HapticFeedback whether enabled is returned.
     * @return The current HapticFeedback status, if enabled return true, otherwise false.
     * @see #setHapticFeedbackEnabled(boolean)
     */
    public boolean getHapticFeedbackEnabled(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getHapticFeedbackEnabled with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getHapticFeedbackEnabled(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getHapticFeedbackEnabled", e);
            return false;
        }
    }
    
    /**
     * Gets the profile state from profile states hash map with given profile key.
     * 
     * @param profileKey The profile key.
     * @return The current profile state referred to given profile key.
     * 
     */
    public AudioProfileState getProfileState(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getProfileState with null profile key!");
            return null;
        }
        
        int size = ProfileSettings.values().length;
        List<String> state = new ArrayList<String>(size);
        
        IAudioProfileService service = getService();
        try {
            state = service.getProfileStateString(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getProfileState", e);
            return null;
        }
        Uri[] profileUri = new Uri[3];
        int[] profileVolume = new int[3];
        boolean[] profileEnabled = new boolean[5];
        
        String value = state.get(ProfileSettings.ringer_stream.ordinal());
        profileUri[0] = (value == null ? null : Uri.parse(value));
        value = state.get(ProfileSettings.notification_stream.ordinal());
        profileUri[1] = (value == null ? null : Uri.parse(value));
        value = state.get(ProfileSettings.notification_stream.ordinal());
        profileUri[2] = (value == null ? null : Uri.parse(value));
        
        value = state.get(ProfileSettings.ringer_volume.ordinal());
        profileVolume[0] = (value == null ? 0 : Integer.valueOf(value));
        value = state.get(ProfileSettings.notification_volume.ordinal());
        profileVolume[1] = (value == null ? 0 : Integer.valueOf(value));
        value = state.get(ProfileSettings.alarm_volume.ordinal());
        profileVolume[2] = (value == null ? 0 : Integer.valueOf(value));
        
        value = state.get(ProfileSettings.vibration_enabled.ordinal());
        profileEnabled[0] =Boolean.valueOf(value);
        value = state.get(ProfileSettings.dtmftone_enabled.ordinal());
        profileEnabled[1] =Boolean.valueOf(value);
        value = state.get(ProfileSettings.soundeffect_enbled.ordinal());
        profileEnabled[2] =Boolean.valueOf(value);
        value = state.get(ProfileSettings.lockscreensound_enabled.ordinal());
        profileEnabled[3] =Boolean.valueOf(value);
        value = state.get(ProfileSettings.hapticfeedback_enabled.ordinal());
        profileEnabled[4] =Boolean.valueOf(value);
        
        AudioProfileState profileState = new AudioProfileState(profileUri, profileVolume, profileEnabled);
        Log.d(TAG, "getProfileState for profileKey = " + profileKey + ": " + profileState.toString());
        return profileState;
    }
    
    /**
     * Returns the name of given custom profile.
     * 
     * @param profileKey The custom profile key.
     * @return profile name
     * @see #setProfileName(String, String)
     */
    public String getProfileName(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "getProfileName with null profile key!");
            return null;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.getProfileName(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getProfileName", e);
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Set methods to set profile setting to database with given profile key
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets the {@link Uri} of the default sound for a given ring tone type and profile key.
     *
     * @param profileKey The profile key given to set ringtone uri.
     * @param type The type whose default sound should be set. One of
     *            {@link #TYPE_RINGTONE}, {@link #TYPE_NOTIFICATION}, 
     *             or {@link #TYPE_VIDEO_CALL}.
     * @param ringtoneUri A {@link Uri} pointing to the default sound to set.
     * @see #getRingtoneUri(Context, int)
     */
    public void setRingtoneUri(String profileKey, int type, Uri ringtoneUri) {
        if (profileKey == null) {
            Log.e(TAG, "setStreamVolume with null profile key!");
            return;
        }
        
        if ((type != TYPE_RINGTONE) && (type != TYPE_NOTIFICATION) && (type != TYPE_VIDEO_CALL)) {
            Log.e(TAG, "setStreamVolume with unsupport stream type!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setRingtoneUri(profileKey, type, ringtoneUri);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setRingtoneUri", e);
        }
    }

    /**
     * Sets the volume index for a particular stream to database.
     *
     * @param profileKey The profile key given to set stream volume.
     * @param streamType The stream whose volume index should be set.
     *                   One of {@link #STREAM_RING}, {@link #STREAM_NOTIFICATION}  
     *                   or {@link #STREAM_ALARM}.
     * @param index The volume index to set.
     * @see #getStreamMaxVolume(int)
     * @see #getStreamVolume(int)
     */
    public void setStreamVolume(String profileKey, int streamType, int index) {
        if (profileKey == null) {
            Log.e(TAG, "setStreamVolume with null profile key!");
            return;
        }
        
        if ((streamType != STREAM_RING) && (streamType != STREAM_NOTIFICATION) && (streamType != STREAM_ALARM)) {
            Log.e(TAG, "setStreamVolume with unsupport stream type!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setStreamVolume(profileKey, streamType, index);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setStreamVolume", e);
        }
    }
    
    /**
     * Sets whether the phone should vibrate for incoming calls.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether vibration enabled.
     * @see #getVibrationEnabled()
     */
    public void setVibrationEnabled(String profileKey, boolean enabled) {
        if (profileKey == null) {
            Log.e(TAG, "setVibrationEnabled with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setVibrationEnabled(profileKey, enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setVibrationEnabled", e);
        }
    }
    
    /**
     * Gets whether tone should be played when using dial pad.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether DtmfTone enabled.
     * @see #setDtmfToneEnabled(boolean)
     */
    public void setDtmfToneEnabled(String profileKey, boolean enabled) {
        if (profileKey == null) {
            Log.e(TAG, "setDtmfToneEnabled with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setDtmfToneEnabled(profileKey, enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setDtmfToneEnabled", e);
        }
    }
    
    /**
     * Sets whether sound should be played when making screen selection.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether SoundEffect enabled.
     * @see #getSoundEffectEnabled()
     */
    public void setSoundEffectEnabled(String profileKey, boolean enabled) {
        if (profileKey == null) {
            Log.e(TAG, "setSoundEffectEnabled with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setSoundEffectEnabled(profileKey, enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setSoundEffectEnabled", e);
        }
    }

    /**
     * Sets whether sound should be played when lock or unlock screen.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether LockScreen sound enabled.
     * @see #getLockScreenEnabled(String)
     */
    public void setLockScreenEnabled(String profileKey, boolean enabled) {
        if (profileKey == null) {
            Log.e(TAG, "setLockScreenEnabled with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setLockScreenEnabled(profileKey, enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setLockScreenEnabled", e);
        }
    }
    
    /**
     * Sets whether the phone should vibrate when pressing soft keys and on certain UI interactions.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether HapticFeedback enabled.
     * @see #getHapticFeedbackEnabled(String)
     */
    public void setHapticFeedbackEnabled(String profileKey, boolean enabled) {
        if (profileKey == null) {
            Log.e(TAG, "setHapticFeedbackEnabled with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setHapticFeedbackEnabled(profileKey, enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setHapticFeedbackEnabled", e);
        }
    }

    /**
     * Sets the given profile's name.
     * 
     * @param profileKey The key of the profile.
     * @param newName The new name to be set.
     * @see #getProfileName(String)
     */
    public void setProfileName(String profileKey, String newName) {
        if (profileKey == null) {
            Log.e(TAG, "setProfileName with null profile key!");
            return;
        }
        
        IAudioProfileService service = getService();
        try {
            service.setProfileName(profileKey, newName);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setProfileName", e);
        }
    } 
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Other methods
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Checks whether this given profile is the active one.
     * 
     * @param profileKey The profile key .
     * @return True if the given profile is active, otherwise false.
     */
    public boolean isActive(String profileKey) {
        if (profileKey == null) {
            Log.e(TAG, "isActive with null profile key!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.isActive(profileKey);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isActive", e);
            return false;
        }
    }
    
    public boolean isRingtoneExist(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "isRingtoneExist with null uri!");
            return false;
        }
        
        IAudioProfileService service = getService();
        try {
            return service.isRingtoneExist(uri);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isRingtoneExist", e);
            return false;
        }
    }
    
    /**
     * Returns the maximum volume index for a particular stream.
     *
     * @param streamType The stream type whose maximum volume index is returned. Currently only
     *                   {@link #STREAM_RING}, {@link #STREAM_NOTIFICATION} and 
     *                   {@link #STREAM_ALARM} are supported.
     * @return The maximum valid volume index for the stream.
     * @see #getStreamVolume(int)
     */
    public int getStreamMaxVolume(int streamType) {
        IAudioProfileService service = getService();
        try {
            return service.getStreamMaxVolume(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isRingtoneExist", e);
            return UNSUPPORT_STREAM_VOLUME;
        }
    }
    
    /**
     * Returns the default ringtone for a particular stream.
     *
     * @param type The type whose default sound should be set. One of
     *            {@link #TYPE_RINGTONE}, {@link #TYPE_NOTIFICATION}
     *            or {@link #TYPE_VIDEO_CALL}.
     * @return The default ringtone uri.
     * @see #setRingtoneUri(String, int, Uri)
     */
    public Uri getDefaultRingtone(int type) {
        IAudioProfileService service = getService();
        try {
            return service.getDefaultRingtone(type);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isRingtoneExist", e);
            return null;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Register a AudiopPofile listener callback to AudioProfileService
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Register the IAudioProfileListener callback to AudioProfileService to listen AudioProfile changed.
     * 
     * @param callback AudioProfileListener callback.
     * @param event The event for listener.
     * 
     * */
    public void listenAudioProfie(IAudioProfileListener callback, int event) {
        IAudioProfileService service = getService();
        try {
            service.listenAudioProfie(callback, event);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in listenAudioProfie", e);
        }
    }
    
}
