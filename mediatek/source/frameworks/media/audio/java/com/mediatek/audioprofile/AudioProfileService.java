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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.audioprofile.AudioProfileManager.ProfileSettings;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileListener;
import com.mediatek.audioprofile.IAudioProfileListener;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

public class AudioProfileService extends IAudioProfileService.Stub {
    private static final String TAG = "AudioProfileService";
    
    // Message to handle AudioProfile changed(first persist profile settings
    private static final int MSG_PERSIST_VOICECALL_RINGTONE_TO_SYSTEM          = 1;
    private static final int MSG_PERSIST_NOTIFICATION_RINGTONE_TO_SYSTEM       = 2;
    private static final int MSG_PERSIST_VIDEOCALL_RINGTONE_TO_SYSTEM          = 3;
    private static final int MSG_PERSIST_DTMF_TONE_TO_SYSTEM                   = 4;
    private static final int MSG_PERSIST_SOUND_EFFECT_TO_SYSTEM                = 5;
    private static final int MSG_PERSIST_LOCKSCREEN_SOUND_TO_SYSTEM            = 6;
    private static final int MSG_PERSIST_HAPTIC_FEEDBACK_TO_SYSTEM             = 7;
    private static final int MSG_PERSIST_RINGER_VOLUME_TO_DATABASE             = 8;
    private static final int MSG_PERSIST_NOTIFICATION_VOLUME_TO_DATABASE       = 9;
    private static final int MSG_PERSIST_ALARM_VOLUME_TO_DATABASE              = 10;
    private static final int MSG_PERSIST_VOICECALL_RINGTONE_TO_DATABASE        = 11;
    private static final int MSG_PERSIST_NOTIFICATION_RINGTONE_TO_DATABASE     = 12;
    private static final int MSG_PERSIST_VIDEOCALL_RINGTONE_TO_DATABASE        = 13;
    private static final int MSG_PERSIST_VIBRATION_TO_DATABASE                 = 14;
    private static final int MSG_PERSIST_DTMF_TONE_TO_DATABASE                 = 15;
    private static final int MSG_PERSIST_SOUND_EFFECT_TO_DATABASE              = 16;
    private static final int MSG_PERSIST_LOCKSCREEN_SOUND_TO_DATABASE          = 17;
    private static final int MSG_PERSIST_HAPTIC_FEEDBACK_TO_DATABASE           = 18;
    private static final int MSG_PERSIST_PROFILE_NAME_TO_DATABASE              = 19;
    
    private static final int MSG_DELAY_SET_VIBRATE_AVOID_CTS_FAIL              = 20;
    
    // Avoid CTS fail we should delay to set vibration
    private static final long DELAY_TIME_AVOID_CTS_FAIL = 20000;
    // Add for CMCC project
    private static final boolean IS_CMCC = AudioProfileManager.IS_CMCC;
    
    // Add to identify silent notification
    private static final Uri SILENT_NOTIFICATION_URI = Uri.parse("com.mediatek.uri.silent_notificaton");
    
    private Context mContext;
    private ContentResolver mContentResolver;
    /** @see OverrideSystemThread */
    private OverrideSystemThread mOverrideSystemThread;
    private Handler mAudioProfileHandler;
    private AudioManager mAudioManager;
    private AudioProfileManager mAudioProfileManager;
    private String mActiveProfileKey;
    private String mLastActiveProfileKey;
    private ArrayList<Record> mRecords = new ArrayList<Record>();
    private List<String> mKeys = new ArrayList<String>();
    
    // Delay set vibrate to avoid CTS fail
    private boolean mDelaySetVibrate = false;
    /** 
     * The profile states save all the persisted settings with all profiles. Init it when service
     * first start and maintain this hash map when profiles settings have been changed.
     */
    private HashMap<String,AudioProfileState> mProfileStates = new HashMap<String,AudioProfileState>(AudioProfileManager.MAX_PROFILES_COUNT);
    
    /** 
     * The custom profile's names.
     */
    private HashMap<String,String> mCustomProfileName = new HashMap<String,String>();
    
    /** 
     * The active custom profile has been deleted, if it is true, we should override system with
     * all persist values.
     */
    private boolean mCustomActiveProfileDeleted = true;
    
    private int mRingerMode = -1;
    
    /** 
     * Record the status that user whether change the last active profile's settings
     * (three type volume and three type ringtone), if it is true, we should synchronize 
     * these changes to system when we set last active profile to be active one.
     */
    private ArrayList<Boolean> mShouldSyncToSystem = new ArrayList<Boolean>();
    
    /** 
     * RingerMode change listener
     */
    private AudioProfileListener mRingerModeListenr = new AudioProfileListener() {
        @Override
        public void onRingerModeChanged(int newRingerMode) {
            int ringerMode = mAudioManager.getRingerMode();
            if (ringerMode != newRingerMode) {
                Log.d(TAG, "onRingerModeChanged: ringermode is not latest: new = "
                        + newRingerMode + ", latest = " + ringerMode);
            }
            if (ringerMode != mRingerMode ) {
                Scenario activeScenario = AudioProfileManager.getScenario(mActiveProfileKey);
                // Only when ringermode has been change by other app, we should change profile.
                if (IS_CMCC) {
                    Log.d(TAG, "CMCC: onRingerModeChanged: ringermode changed by other app, change profile! ringerMode = " + ringerMode);
                    mRingerMode = ringerMode;
                    switch (ringerMode) {
                        case AudioManager.RINGER_MODE_SILENT:
                        case AudioManager.RINGER_MODE_VIBRATE:
                            // RingerMode has been changed to be silent or vibrate, if profile
                            // is not silent, change active to silent.
                            if (!Scenario.SILENT.equals(activeScenario)) {
                                Log.d(TAG, "CMCC: RingerMode change to SILENT or VIBRATE, change profile to silent!");
                                setActiveProfile(AudioProfileManager.getProfileKey(Scenario.SILENT), false);
                            }
                            break;
                            
                        case AudioManager.RINGER_MODE_NORMAL:
                            // RingerMode has been changed to be normal, if profile is silent,
                            // set active to last active profile.
                            if (Scenario.SILENT.equals(activeScenario)) {
                                Log.d(TAG, "CMCC: RingerMode change to NORMAL, change profile to last active profile!");
                                setActiveProfile(mLastActiveProfileKey, false);
                            }
                            break;
                    }
                } else {
                    Log.d(TAG, "onRingerModeChanged: ringermode changed by other app, change profile! ringerMode = " + ringerMode);
                    mRingerMode = ringerMode;
                    switch (ringerMode) {
                        case AudioManager.RINGER_MODE_SILENT:
                            // RingerMode has been changed to be silent, if profile is not
                            // silent, set active to silent.
                            if (!Scenario.SILENT.equals(activeScenario)) {
                                Log.v(TAG, "RingerMode change to SILENT, change profile to silent!");
                                setActiveProfile(AudioProfileManager.getProfileKey(Scenario.SILENT), false);
                            }
                            break;

                        case AudioManager.RINGER_MODE_VIBRATE:
                            // RingerMode has been changed to be vibrate, if profile is not
                            // meeting, set active to meeting.
                            if (!Scenario.MEETING.equals(activeScenario)) {
                                Log.v(TAG, "RingerMode change to VIBRATE, change profile to meeting!");
                                setActiveProfile(AudioProfileManager.getProfileKey(Scenario.MEETING), false);
                            }
                            break;
                            
                        case AudioManager.RINGER_MODE_NORMAL:
                            // RingerMode has been changed to be normal, if profile is silent
                            // or meeting, set active to last active profile.
                            if (Scenario.SILENT.equals(activeScenario) || Scenario.MEETING.equals(activeScenario)) {
                                Log.v(TAG, "RingerMode change to NORMAL, change profile to last active profile!");
                                int systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_RING);
                                syncRingerVolumeToProfile(mLastActiveProfileKey, systemVolume);
                                setActiveProfile(mLastActiveProfileKey, false);
                            }
                            break;
                    }
                }
            } else {
                Log.d(TAG, "onRingerModeChanged with the same as profile ringermode! ringerMode = " +ringerMode);
            }
        }
    };
    
    /** 
     * Ringer volume change listener
     */
    private AudioProfileListener mRingerVolumeListenr = new AudioProfileListener() {
        @Override
        public void onRingerVolumeChanged(int oldVolume, int newVolume, String extra) {
            if (oldVolume == newVolume) {
                Log.w(TAG, "onRingerVolumeChanged with Volume don't change, do nothing!");
                return;
            }
            AudioProfileState activeProfileState = mProfileStates.get(mActiveProfileKey);
            Scenario activeScenario = AudioProfileManager.getScenario(mActiveProfileKey);
            // Sysn the volume change to active profile volume
            // MTK_OP01_PROTECT_START--->
            if (IS_CMCC) {
                if (!Scenario.SILENT.equals(activeScenario) && activeProfileState.mRingerVolume != newVolume) {
                    notifyRingerVolumeChanged(oldVolume, newVolume, mActiveProfileKey);
                    syncRingerVolumeToProfile(mActiveProfileKey, newVolume);
                    Log.d(TAG, "CMCC: onRingerVolumeChanged: ringer volume changed, sysn to active profile except silent!");
                }
            // <---MTK_OP01_PROTECT_END
            } else {
                switch (activeScenario) {
                    case OUTDOOR:
                        if ((newVolume > 0) && (newVolume != 7)) {
                            // If the active profile is outdoor and volume has been changed(must > 0),
                            // we should change it to general profile in non-CMCC project
                            String generalProfilekey = AudioProfileManager.getProfileKey(Scenario.GENERAL);
                            syncRingerVolumeToProfile(generalProfilekey, newVolume);
                            setActiveProfile(generalProfilekey);
                            Log.d(TAG, "onRingerVolumeChanged in outdoor profile, so change to general profile!");
                        }
                        break;

                    case GENERAL:
                    case CUSTOM:
                        if (activeProfileState.mRingerVolume != newVolume) {
                            // If active profile is general or custom and volume changed,
                            // sync to active profile.
                            notifyRingerVolumeChanged(oldVolume, newVolume, mActiveProfileKey);
                            syncRingerVolumeToProfile(mActiveProfileKey, newVolume);
                            Log.d(TAG, "onRingerVolumeChanged: ringer volume changed, sysn to active profile except silent, meeting and outdoor!");
                        }
                        break;
                    
                    default:
                        // This is a special case: when system volume change from non-zero to zero 
                        // AudioManager will first change RingerMode from normal to vibrate,
                        // and then change volume to 0. but we want first change the profile's
                        // volume to be 0, and then change profile to match new RingerMode.So at this
                        // case we should sync this volume change to last active profile.
                        
                        if (oldVolume > 0 && newVolume == 0) {
                            notifyRingerVolumeChanged(oldVolume, newVolume, mLastActiveProfileKey);
                            syncRingerVolumeToProfile(mLastActiveProfileKey, newVolume);
                            Log.d(TAG, "onRingerVolumeChanged: sync volume 1->0 to last active profile when it cause ringemode change!");
                        }
                        break;
                       
                }
            }
        }
    };
    
    /** 
     * Ringtone observer, when ringtone changed from other app, synchronize to active profile.
     */
    private ContentObserver mRingtoneObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            synchronized (mActiveProfileKey) {
                Scenario activeScenario = AudioProfileManager.getScenario(mActiveProfileKey);
                AudioProfileState activeState = getProfileState(mActiveProfileKey);
                if (IS_CMCC) {
                    if (!Scenario.SILENT.equals(activeScenario)) {
                        // If ringtone has been changed and the active profile is non-silent profile,
                        // synchronize the current system ringtone to active profile.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.RINGTONE);
                        Uri systemUri = (uriString == null ? null : Uri.parse(uriString));
                        
                        if ((activeState.mRingerStream == null && systemUri != null) || 
                                activeState.mRingerStream != null && !activeState.mRingerStream.equals(systemUri)) {
                            activeState.mRingerStream = systemUri;
                            persistRingtoneUriToDatabase(mActiveProfileKey, AudioProfileManager.TYPE_RINGTONE, systemUri);
                            Log.d(TAG, "Ringtone changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Ringtone changed by itself, do nothing!");
                        }
                    }
                } else {
                    if (Scenario.GENERAL.equals(activeScenario) || Scenario.OUTDOOR.equals(activeScenario)) {
                        // If ringtone has been changed and the active profile is general or outdoor
                        // profile, synchronize the current system ringtone to both profiles.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.RINGTONE);
                        Uri systemUri = (uriString == null ? null : Uri.parse(uriString));
                        
                        if ((activeState.mRingerStream == null && systemUri != null) || 
                                activeState.mRingerStream != null && !activeState.mRingerStream.equals(systemUri)) {
                            String generalKey = AudioProfileManager.getProfileKey(Scenario.GENERAL);
                            String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
                            getProfileState(generalKey).mRingerStream = systemUri;
                            getProfileState(outdoorKey).mRingerStream = systemUri;
                            persistRingtoneUriToDatabase(generalKey, AudioProfileManager.TYPE_RINGTONE, systemUri);
                            persistRingtoneUriToDatabase(outdoorKey, AudioProfileManager.TYPE_RINGTONE, systemUri);
                            Log.d(TAG, "Ringtone changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Ringtone changed by itself, do nothing!");
                        }
                    } else if (Scenario.CUSTOM.equals(activeScenario)) {
                        // If ringtone has been changed and the active profile is custom profile,
                        // synchronize the current system ringtone to active profile.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.RINGTONE);
                        Uri systemUri = (uriString == null ? null : Uri.parse(uriString));
                        
                        if ((activeState.mRingerStream == null && systemUri != null) || 
                                activeState.mRingerStream != null && !activeState.mRingerStream.equals(systemUri)) {
                            activeState.mRingerStream = systemUri;
                            persistRingtoneUriToDatabase(mActiveProfileKey, AudioProfileManager.TYPE_RINGTONE, systemUri);
                            Log.d(TAG, "Ringtone changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Ringtone changed by itself, do nothing!");
                        }
                    }
                }
            }
        }
    };
    
    /** 
     * Notification observer, when Notification changed from other app, synchronize to active profile.
     */
    private ContentObserver mNotificationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            synchronized (mActiveProfileKey) {
                Scenario activeScenario = AudioProfileManager.getScenario(mActiveProfileKey);
                AudioProfileState activeState = getProfileState(mActiveProfileKey);
                if (IS_CMCC) {
                    if (!Scenario.SILENT.equals(activeScenario)) {
                        // If notification has been changed and the active profile is non-silent profile,
                        // synchronize the current system notification to active profile.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.NOTIFICATION_SOUND);
                        Uri systemUri = (uriString == null ? SILENT_NOTIFICATION_URI : Uri.parse(uriString));
                        
                        if ((activeState.mNotificationStream == null && systemUri != null) || 
                                activeState.mNotificationStream != null && !activeState.mNotificationStream.equals(systemUri)) {
                            activeState.mNotificationStream = systemUri;
                            persistRingtoneUriToDatabase(mActiveProfileKey, AudioProfileManager.TYPE_NOTIFICATION, systemUri);
                            Log.d(TAG, "Notification changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Notification changed by itself, do nothing!");
                        }
                    }
                } else {
                    if (Scenario.GENERAL.equals(activeScenario) || Scenario.OUTDOOR.equals(activeScenario)) {
                        // If notification has been changed and the active profile is general or outdoor
                        // profile, synchronize the current system notification to both profiles.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.NOTIFICATION_SOUND);
                        Uri systemUri = (uriString == null ? SILENT_NOTIFICATION_URI : Uri.parse(uriString));
                        
                        if ((activeState.mNotificationStream == null && systemUri != null) || 
                                activeState.mNotificationStream != null && !activeState.mNotificationStream.equals(systemUri)) {
                            String generalKey = AudioProfileManager.getProfileKey(Scenario.GENERAL);
                            String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
                            getProfileState(generalKey).mNotificationStream = systemUri;
                            getProfileState(outdoorKey).mNotificationStream = systemUri;
                            persistRingtoneUriToDatabase(generalKey, AudioProfileManager.TYPE_NOTIFICATION, systemUri);
                            persistRingtoneUriToDatabase(outdoorKey, AudioProfileManager.TYPE_NOTIFICATION, systemUri);
                            Log.d(TAG, "Notification changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Notification changed by itself, do nothing!");
                        }
                    } else if (Scenario.CUSTOM.equals(activeScenario)) {
                        // If notification has been changed and the active profile is custom profile,
                        // synchronize the current system notification to active profile.
                        String uriString = Settings.System.getString(mContentResolver, Settings.System.NOTIFICATION_SOUND);
                        Uri systemUri = (uriString == null ? SILENT_NOTIFICATION_URI : Uri.parse(uriString));
                        
                        if ((activeState.mNotificationStream == null && systemUri != null) || 
                                activeState.mNotificationStream != null && !activeState.mNotificationStream.equals(systemUri)) {
                            activeState.mNotificationStream = systemUri;
                            persistRingtoneUriToDatabase(mActiveProfileKey, AudioProfileManager.TYPE_NOTIFICATION, systemUri);
                            Log.d(TAG, "Notification changed by other app in non-silent profile, synchronize to active profile: new uri = " + systemUri);
                        } else {
                            Log.d(TAG, "Notification changed by itself, do nothing!");
                        }
                    }
                }
            }
        }
    };
    
    /**
     * When first boot completed, we should update default rongtone from null to really value after
     * media scanner finished, because MedioScanner start after AudioProfileService. 
     */
    private BroadcastReceiver mUpgradeReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mUpgradeReceiver>>> update profile: action = " + action);
            Uri defaultRingtoneUri = getDefaultRingtone(AudioProfileManager.TYPE_RINGTONE);
            Uri defaultNotificationUri = getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION);
            Uri defaultVideoCallUri = getDefaultRingtone(AudioProfileManager.TYPE_VIDEO_CALL);
            
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                synchronized (mProfileStates) {
                    for (String profileKey : mKeys) {
                        AudioProfileState profileState = mProfileStates.get(profileKey);
                        // Voice call
                        if (null == profileState.mRingerStream) {
                            profileState.mRingerStream = defaultRingtoneUri;
                        }
                        // Notification
                        if (null == profileState.mNotificationStream) {
                            profileState.mNotificationStream = defaultNotificationUri;
                        }
                        // Video call
                        if (null == profileState.mVideoCallStream) {
                            profileState.mVideoCallStream = defaultVideoCallUri;
                        }
                    }
                }
            }
            mContext.unregisterReceiver(mUpgradeReceiver);
            Log.d(TAG, "mUpgradeReceiver<<< unregister receiver!");
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /** @hide */
    public AudioProfileService(Context context) {
        Log.v(TAG, "Initial AudioProfileService!");
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mAudioProfileManager = (AudioProfileManager)mContext.getSystemService(Context.AUDIOPROFILE_SERVICE);
        
        // Initial active profile key and last active profile key, if not exist, use general as defalut.
        String activeProfileKey = Settings.System.getString(mContentResolver, AudioProfileManager.KEY_ACTIVE_PROFILE);
        mActiveProfileKey = (activeProfileKey == null ? AudioProfileManager.getProfileKey(Scenario.GENERAL) : activeProfileKey);
        String lastActiveProfileKey = Settings.System.getString(mContentResolver, AudioProfileManager.LAST_ACTIVE_PROFILE);
        mLastActiveProfileKey = (lastActiveProfileKey == null ? AudioProfileManager.getProfileKey(Scenario.GENERAL) : lastActiveProfileKey);
        mCustomActiveProfileDeleted = Boolean.valueOf(Settings.System.getString(mContentResolver, AudioProfileManager.LAST_ACTIVE_CUSTOM_DELETED));

        createOverrideSystemThread();
        
        mKeys = readProfileKeys();
        for(String profileKey : mKeys) {
            readPersistedSettings(profileKey);
        }
        
        // Listen RingerMode and RingerVolume changed
        mRingerMode = mAudioManager.getRingerMode();
        mAudioManager.listenRingerModeAndVolume(mRingerModeListenr.callback, AudioProfileListener.LISTEN_RINGERMODE_CHANGED);
        mAudioManager.listenRingerModeAndVolume(mRingerVolumeListenr.callback, AudioProfileListener.LISTEN_RINGER_VOLUME_CHANGED);

        // Observer ringtone and notification changed
        mContentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.RINGTONE), false, mRingtoneObserver);
        mContentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.NOTIFICATION_SOUND), false, mNotificationObserver);
        
        // Initial mShouldSyncToSystem
        readShouldSyncToSystem();
        
        // Register media scan finish receiver for When first boot a phone, AudioProfileService are
        // initial before media scanner, so we should update the default ringtone when media scanner
        // finish initial database.
        IntentFilter filter = new IntentFilter();
        if (null == getDefaultRingtone(AudioProfileManager.TYPE_RINGTONE)) {
            filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            filter.addDataScheme("file");
            mContext.registerReceiver(mUpgradeReceiver, filter);
        } else if (null == RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_RINGTONE)) {
            // Check actual ringtone, if it is null, set to default ringtone.
            persistRingtoneUriToSystem(AudioProfileManager.TYPE_RINGTONE);
        } else if (null == RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_VIDEO_CALL)) {
            persistRingtoneUriToSystem(AudioProfileManager.TYPE_VIDEO_CALL);
        }
        
        // Check the four default profiles' settings whether match it's type.
        checkDefaultProfiles();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Set active profile, add profile, delete profile and reset profiles
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Sets the active profile with given profile key.
     * 
     * @param profileKey The key of the profile that set to be active.
     * @param shouldSetRingerMode Whether need to set ringer mode, if ringer mode change by other app
     * shouldSetRingerMode is false, otherwise it is true.
     */
    private void setActiveProfile(String profileKey, boolean shouldSetRingerMode) {

        String oldProfileKey = getActiveProfileKey();
        String newProfileKey = profileKey;
        Log.d(TAG, "setActiveProfile>>>: oldProfileKey = " + oldProfileKey + ", newProfileKey = " + newProfileKey + ", shouldSetRingerMode = " + shouldSetRingerMode);
        if(!newProfileKey.equals(oldProfileKey)) {
            synchronized (mActiveProfileKey) {
                setActiveKey(newProfileKey);
                Scenario newScenario = AudioProfileManager.getScenario(newProfileKey);
                Scenario oldScenario = AudioProfileManager.getScenario(oldProfileKey);
                boolean overrideSystem = true;
                // Set RingerMode to match different AudioProfiles to different RingerModes
                int ringerMode = mAudioManager.getRingerMode();
                

                // MTK_OP01_PROTECT_START--->
                if (IS_CMCC) {
                    switch (newScenario) {
                        case SILENT: {
                            // Set RingerMode: if vibration is enabled, set to VIBRATE, is not set to SILENT
                            if (shouldSetRingerMode) {
                                if(getProfileState(newProfileKey).mVibrationEnabled){
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                    mRingerMode = AudioManager.RINGER_MODE_VIBRATE;
                                    Log.d(TAG, "CMCC: setActiveProfile: RingerMode is NORMAL, now set VIBRATE");
                                }else{
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                    mRingerMode = AudioManager.RINGER_MODE_SILENT;
                                    Log.d(TAG, "CMCC: setActiveProfile: RingerMode is NORMAL, now set SILENT");
                                }
                            }
                            // Persisted settings to override system 
                            boolean lastActiveChanged = setLastActiveKey(oldProfileKey);
                            if(lastActiveChanged && mCustomActiveProfileDeleted) {
                                setCustomActiveDeleted(false);
                            }
                            overrideSystem = false;
                            break;
                        }
                        
                        default: {
                            // Set RingerMode:
                            if (shouldSetRingerMode) {
                                if (ringerMode != AudioManager.RINGER_MODE_NORMAL ) {
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                    mRingerMode = AudioManager.RINGER_MODE_NORMAL;
                                    Log.d(TAG, "CMCC: setActiveProfile: RingerMode is VIBRATE or SILENT, now set NORMAL");
                                } else {
                                    Log.d(TAG, "CMCC: setActiveProfile: RingerMode is already NORMAL");
                                }
                            }
                            // Persisted settings to override system 
                            if (Scenario.SILENT.equals(oldScenario) && newProfileKey.equals(mLastActiveProfileKey)) {
                                // If the new profile is same as last active profile when set silent
                                // to non-silent, we should not override system except the last active
                                // profile key has been reset caused by custom active profile delete.
                                overrideSystem = mCustomActiveProfileDeleted;
                                
                                // In this case, when user changed the last active profile volumes and
                                // ringtones, we should persist it to system
                                syncVolumeToSystem();
                                syncRingtoneToSystem();
                            }
                            break;
                        }
                    }
                // <---MTK_OP01_PROTECT_END
                } else {
                    switch (newScenario) {
                        case SILENT: {
                            // Set RingerMode:
                            if (shouldSetRingerMode) {
                                if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                    mRingerMode = AudioManager.RINGER_MODE_SILENT;
                                    Log.d(TAG, "setActiveProfile: RingerMode is not SILENT, now set SILENT");
                                } else {
                                    Log.d(TAG, "setActiveProfile: RingerMode is already SILENT");
                                }
                            }
                            // Persisted settings to override system 
                            if (Scenario.GENERAL.equals(oldScenario) || Scenario.CUSTOM.equals(oldScenario)) {
                                // If set profile from general or custom to silent or meeting
                                // set it to be last active key.
                                boolean lastActiveChanged = setLastActiveKey(oldProfileKey);
                                if(lastActiveChanged && mCustomActiveProfileDeleted) {
                                    setCustomActiveDeleted(false);
                                }
                            }
                            overrideSystem = false;
                            break;
                        }
                        
                        case MEETING: {
                            // Set RingerMode:
                            if (shouldSetRingerMode) {
                                if (ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                    mRingerMode = AudioManager.RINGER_MODE_VIBRATE;
                                    Log.d(TAG, "setActiveProfile: RingerMode is not VIBRATE, now set VIBRATE");
                                } else {
                                    Log.d(TAG, "setActiveProfile: RingerMode is already VIBRATE");
                                }
                            }
                            // Persisted settings to override system 
                            if (Scenario.GENERAL.equals(oldScenario) || Scenario.CUSTOM.equals(oldScenario)) {
                                // If set profile from general or custom to silent or meeting
                                // set it to be last active key.
                                boolean lastActiveChanged = setLastActiveKey(oldProfileKey);
                                if(lastActiveChanged && mCustomActiveProfileDeleted) {
                                    setCustomActiveDeleted(false);
                                }
                                
                            }
                            overrideSystem = false;
                            break;
                        }
                       
                        default: {
                            // Set RingerMode:
                            if (shouldSetRingerMode) {
                                if (ringerMode != AudioManager.RINGER_MODE_NORMAL ) {
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                    mRingerMode = AudioManager.RINGER_MODE_NORMAL;
                                    Log.d(TAG, "setActiveProfile: RingerMode is VIBRATE or SILENT, now set NORMAL");
                                } else {
                                    Log.d(TAG, "setActiveProfile: RingerMode is already NORMAL");
                                }
                            }
                            // New plan can set general and custom volume to 0 and ask automatically
                            // change volume to 1 when it is set to be active profile.
                            if (getProfileState(newProfileKey).mRingerVolume == 0) {
                                int volume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_RING);
                                syncRingerVolumeToProfile(newProfileKey, volume);
                                Log.d(TAG, "setActiveProfile: profile volume is 0, change to " + volume);
                            }
                            // Persisted settings to override system 
                            if ((Scenario.SILENT.equals(oldScenario) || Scenario.MEETING.equals(oldScenario))
                                    && newProfileKey.equals(mLastActiveProfileKey)) {
                                // If the new profile is same as last active profile when set profile
                                // from silent(or meeting) to other profiles, we should not override
                                // system except the last active profile key has been reset caused
                                // by custom active profile delete.
                                overrideSystem = mCustomActiveProfileDeleted;
                                
                                // In this case, when user changed the last active profile volumes and
                                // ringtones, we should persist it to system
                                syncVolumeToSystem();
                                syncRingtoneToSystem();
                            }
                            break;
                        }
                    }
                }
                
                // Override system
                persistValues(overrideSystem);
                // Notify AudioProfile changed
                notifyAudioProfileChanged();
                // Notify ringer volume changed
                int ringerVolume = getProfileState(newProfileKey).mRingerVolume;
                notifyRingerVolumeChanged(ringerVolume, ringerVolume, newProfileKey);
                Log.d(TAG, "setActiveProfile<<<");
            }
        } else {
            Log.w(TAG, "setActiveProfile with same profile key with active profile key, do nothing!");
        }
    }
    
    /**
     * set the active profile with given profile key.
     * 
     * @param profileKey The key of the profile that set to be active.
     */
    public void setActiveProfile(String profileKey) {
        setActiveProfile(profileKey, true);
    }
    
    private boolean setActiveKey(String profileKey) {
        boolean succeed = Settings.System.putString(mContentResolver, AudioProfileManager.KEY_ACTIVE_PROFILE, profileKey);
        mActiveProfileKey = profileKey;
        Log.d(TAG, "setActiveKey: succeed = " + succeed + ", profileKey = " + profileKey);
        return succeed;
    }
    
    private boolean setLastActiveKey(String profileKey) {
        boolean succeed = Settings.System.putString(mContentResolver, AudioProfileManager.LAST_ACTIVE_PROFILE, profileKey);
        mLastActiveProfileKey = profileKey;
        // Last active profile key changed, reset mShouldSyncToSystem to default.
        for (int i = 0; i < mShouldSyncToSystem.size(); i++) {
            mShouldSyncToSystem.set(i, false);
        }
        Log.d(TAG, "setLastActiveKey: succeed = " + succeed + ", profileKey = " + profileKey);
        return succeed;
    }
    
    private boolean setCustomActiveDeleted(boolean deleted) {
        boolean succeed = Settings.System.putString(mContentResolver, AudioProfileManager.LAST_ACTIVE_CUSTOM_DELETED, String.valueOf(deleted));
        mCustomActiveProfileDeleted = deleted;
        Log.d(TAG, "setCustomActiveDeleted: changed = " + succeed);
        return succeed;
    }

    /**
     * Gets the key of active profile.
     * 
     * @return The key of the active profile.
     * 
     */
    public String getActiveProfileKey() {
        synchronized (mActiveProfileKey) {
            Log.d(TAG, "getActiveProfile: profileKey = " + mActiveProfileKey);
            return mActiveProfileKey;
        }
    }
    
    /**
     * Gets the key of previous non-silent active profile.
     * 
     * @return The key of last non-silent active profile.
     */
    public String getLastActiveProfileKey() {
        synchronized (mActiveProfileKey) {
            Log.d(TAG, "getLastActiveProfileKey: profileKey = " + mLastActiveProfileKey);
            return mLastActiveProfileKey;
        }
    }
    
    /**
     * Notify the change of the audio profile.
     */
    private void notifyAudioProfileChanged() {
        if(mActiveProfileKey == null) {
            Log.e(TAG, "notifyAudioProfileChanged falled, because active profile key is null!");
            return;
        }
        if(!mRecords.isEmpty()) {
            Log.d(TAG, "notifyAudioProfileChanged: New profile = " + mActiveProfileKey + ", clients = " + mRecords.size());
            synchronized (mRecords) {
                Iterator<Record> iterator = mRecords.iterator();
                while (iterator.hasNext()) {
                    Record record = (Record) iterator.next();
                    if (record.mEvent == AudioProfileListener.LISTEN_AUDIOPROFILE_CHANGEG) {
                        try {
                            record.mCallback.onAudioProfileChanged(mActiveProfileKey);
                        } catch (RemoteException e) {
                            iterator.remove();
                            Log.e(TAG, "Dead object in notifyAudioProfileChanged,"
                                    + " remove listener's callback: record.mBinder = "
                                    + record.mBinder + ", clients = " + mRecords.size()
                                    + ", exception = "+ e);
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "notifyAudioProfileChanged falled, because there are no listener!");
        }
        
    }
    
    /**
     * Notify the ringer volume chenge of the audio profile.
     */
    private void notifyRingerVolumeChanged(int oldVolume, int newVolume, String profileKey) {
        if(mActiveProfileKey == null) {
            Log.e(TAG, "notifyRingerVolumeChanged falled, because active profile key is null!");
            return;
        }
        if(!mRecords.isEmpty()) {
            Log.d(TAG, "notifyRingerVolumeChanged: oldVolume = " + oldVolume + ", newVolume = "
                    + newVolume + ", profile = " + profileKey + ", client = " + mRecords.size());
            synchronized (mRecords) {
                Iterator<Record> iterator = mRecords.iterator();
                while (iterator.hasNext()) {
                    Record record = (Record) iterator.next();
                    if (record.mEvent == AudioProfileListener.LISTEN_RINGER_VOLUME_CHANGED) {
                        try {
                            record.mCallback.onRingerVolumeChanged(oldVolume, newVolume, profileKey);
                        } catch (RemoteException e) {
                            iterator.remove();
                            Log.e(TAG, "Dead object in notifyAudioProfileChanged,"
                                    + " remove listener's callback: record.mBinder = "
                                    + record.mBinder + ", clients = " + mRecords.size()
                                    + ", exception = "+ e);
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "notifyRingerVolumeChanged falled, because there are no listener!");
        }
        
    }
    
    /**
     * Adds a new {@link Scenario#CUSTOM} type profile.
     * 
     * @return The new profile key.
     */
    public String addProfile() {
        if(getProfileCount() >= AudioProfileManager.MAX_PROFILES_COUNT) {
            Log.e(TAG, "addProfile: Number of custom audio profile has reached upper limit!");
            return null;
        }
        
        // general a custom profile key and init it profile state with general default settings.
        String newKey = genCustomKey();
        AudioProfileState defaultState = mAudioProfileManager.getDefaultState(newKey);
        Uri[] profileUri = new Uri[3];
        int[] profileVolume = new int[3];
        boolean[] profileEnabled = new boolean[5];
        
        profileUri[0] = getDefaultRingtone(AudioProfileManager.TYPE_RINGTONE);
        profileUri[1] = getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION);
        profileUri[2] = getDefaultRingtone(AudioProfileManager.TYPE_VIDEO_CALL);
        
        
        profileVolume[0] = defaultState.mRingerVolume;
        profileVolume[1] = defaultState.mNotificationVolume;
        profileVolume[2] = defaultState.mAlarmVolume;
        
        profileEnabled[0] = defaultState.mVibrationEnabled;
        profileEnabled[1] = defaultState.mDtmfToneEnabled;
        profileEnabled[2] = defaultState.mSoundEffectEnbled;
        profileEnabled[3] = defaultState.mLockScreenSoundEnabled;
        profileEnabled[4] = defaultState.mHapticFeedbackEnabled;
        AudioProfileState newProfileState = new AudioProfileState(profileUri, profileVolume, profileEnabled);
        
        // Put the custom profilekey's key to database
        String name = AudioProfileManager.getKey(newKey);
        boolean succeed = Settings.System.putString(mContentResolver, name, newKey);
        // If put the profile key to database successes,
        if (succeed) {
            synchronized (mProfileStates) {
                mKeys.add(newKey);
                mProfileStates.put(newKey, newProfileState);
            }
            Log.d(TAG, "addProfile: key = " + newKey + ", state = " + newProfileState.toString());
        }  else {
            Log.e(TAG, "addProfile: Failed!");
        }
        return newKey;
    }

    /**
     * Deletes a {@link Scenario#CUSTOM} type profile.
     * 
     * @param profileKey The key of the profile that to be deleted.
     * @return True if delete succeed, otherwise false.
     */
    public boolean deleteProfile(String profileKey) {
        if(profileKey == null) {
            Log.e(TAG, "deleteProfile: Null key!");
            return false;
        }
        
        List<String> keyList = AudioProfileManager.getAllKeys(profileKey);
        StringBuilder sb = new StringBuilder();
        sb.append(Settings.System.NAME);
        sb.append(" in (");
        int size = keyList.size();
        for(int i = 0; i < size - 1; i++) {
            sb.append("?,");
        }
        sb.append("?)");
        String where = sb.toString();
        int deleted = mContentResolver.delete(Settings.System.CONTENT_URI, where, keyList.toArray(new String[size]));
        Log.d(TAG, "deleteProfile: where = " + where + ", deleted = " + deleted);
        if(deleted > 0) {
            synchronized (mProfileStates) {
                mKeys.remove(profileKey);
                mProfileStates.remove(profileKey);
                mCustomProfileName.remove(profileKey);
            }
            if(profileKey.equals(mLastActiveProfileKey)) {
                Log.d(TAG, "deleteProfile: Custom active deleted and set to default.");
                setCustomActiveDeleted(true);
                setLastActiveKey(AudioProfileManager.getProfileKey(Scenario.GENERAL));
            }
            Log.d(TAG, "deleteProfile: mKeys = " + mKeys + ", mCustomProfileName = " + mCustomProfileName);
            return true;
        } else {
            Log.e(TAG, "deleteProfile: Failed to delete " + profileKey);
            return false;
        }
    }
    
    /**
     * Reset all profiles.
     */
    public void reset() {
        Log.d(TAG, "reset start!");
        String generalKey = AudioProfileManager.getProfileKey(Scenario.GENERAL);
        boolean isGeneralActive = isActive(generalKey);
        synchronized (mActiveProfileKey) {
            // First restore general profile to default value and set general to be active profile,
            // if the active profile is general, just persist default value to system, otherwise 
            // set general to be active profile.

            restoreToDefaultValues(generalKey);
            if (isGeneralActive) {
                persistValues(true);
            } else {
                setActiveKey(generalKey);
                int ringerMode = mAudioManager.getRingerMode();
                if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                persistValues(true);
                notifyAudioProfileChanged();
            }
            Log.d(TAG, "reset: profileKey = " + generalKey + ", state = " + mProfileStates.get(generalKey));

            // Second restore other three predefine profile to default and delete custom profile if exist.
            List<String> allKeys = new ArrayList<String>();
            allKeys.addAll(mKeys);
            allKeys.remove(generalKey);
            for (String profileKey : allKeys) {
                Scenario scenaria = AudioProfileManager.getScenario(profileKey);
                if (Scenario.CUSTOM.equals(scenaria)) {
                    deleteProfile(profileKey);
                } else {
                    restoreToDefaultValues(profileKey);
                }
                Log.d(TAG, "reset: profileKey = " + profileKey + ", state = " + mProfileStates.get(profileKey));
            }
            Log.d(TAG, "reset finish!");
        }
    }
    
    /**
     * Generates a new unique key for a custom profile.
     * 
     * @return The newly generated unique key for custom profile.
     */
    private String genCustomKey() {
        int maxCustom = AudioProfileManager.MAX_PROFILES_COUNT - AudioProfileManager.PREDEFINED_PROFILES_COUNT;
        Random rand = new Random(System.currentTimeMillis());
        String key = null;
        do {
            int customNo = rand.nextInt() % maxCustom;
            if(customNo < 0) {
                customNo = -customNo;
            }
            key = AudioProfileManager.PROFILE_PREFIX 
                    + Scenario.CUSTOM.toString().toLowerCase() 
                    + "_" + String.valueOf(customNo);
        } while (mKeys.contains(key));
        Log.v(TAG, "genCustomKey: newKey = " + key);
        return key;
    }
    /**
     * Checks out whether the name existed.
     * 
     * @param name The name to be checked.
     * @return True if the specified name had existed or if the name 
     *         is null or empty, false otherwise.
     */
    public boolean isNameExist(String name) {
        boolean isExisted = mCustomProfileName.containsValue(name);
        Log.d(TAG, "isNameExist: name = " + name + ", isExisted = " + isExisted + ", mCustomProfileName = " + mCustomProfileName);
        return isExisted;
    }
    
    /**
     * Gets the number of current existing profiles. 
     * Include the predefined and custom ones.
     * 
     * @return The number of existing profiles.
     */
    public int getProfileCount() {
        synchronized (mProfileStates) {
            int count = mKeys.size();
            Log.d(TAG, "getProfileCount: count = " + count);
            return count;
        }
    }
    
    /**
     * Gets the existed profiles' keys.
     * 
     * @return The existed profiles' keys.
     */
    public List<String> getAllProfileKeys() {
        synchronized (mProfileStates) {
            ArrayList<String> allKeys = new ArrayList<String>();
            allKeys.addAll(mKeys);
            Log.d(TAG, "getAllProfileKeys: keys = " + allKeys);
            return allKeys;
        }
    }

    
    
    /**
     * Gets predefined profiles' keys.
     * 
     * @return The predefined profiles' keys.
     */
    public List<String> getPredefinedProfileKeys() {
        int size = Scenario.values().length;
        List<String> keys = new ArrayList<String>(size);
        for(int i = 0; i < size - 1; i++) {
            Scenario scenario = Scenario.values()[i];
            if(scenario != null) {
                keys.add(AudioProfileManager.getProfileKey(scenario));
            }
        }
        Log.d(TAG, "getPredefinedProfileKeys: " + keys);
        return keys;
    }
    
    /**
     * Gets customized profiles' keys.
     * 
     * @return The customized profiles' keys.
     */
    public List<String> getCustomizedProfileKeys() {
        // If profiles count is equal PREDEFINED_PROFILES_COUNT, there are no custom profiles
        if (getProfileCount() <= AudioProfileManager.PREDEFINED_PROFILES_COUNT) {
            return null;
        }
        // Remove all the predefined profile keys from all keys list and return it to be custom one
        List<String> customizedProfileKeys = new ArrayList<String>();
        customizedProfileKeys.addAll(mKeys);
        for(int i = 0; i < AudioProfileManager.PREDEFINED_PROFILES_COUNT; i++) {
            Scenario scenario = Scenario.values()[i];
            customizedProfileKeys.remove(AudioProfileManager.getProfileKey(scenario));
        }
        Log.d(TAG, "getCustomizedProfileKeys: " + customizedProfileKeys);
        return customizedProfileKeys;
    }
    
    /**
     * Persists profile's settings.
     * 
     * @param overrideSystem Whether override volume and ringtone to system .
     */
    private void persistValues(boolean overrideSystem) {
        AudioProfileState activeProfileState = mProfileStates.get(mActiveProfileKey);
        if (null != activeProfileState) {
            Log.d(TAG, "persistValues: override = " + overrideSystem + ", state = " + activeProfileState.toString());
        } else {
            Log.e(TAG, "persistValues error with no " + mActiveProfileKey + " in " + mProfileStates);
        }
        
        // First persist vibration to avoid CTS fail
        persistVibrationToSystem();
        
        if(overrideSystem) {
            persistStreamVolumeToSystem(AudioProfileManager.STREAM_RING);
            persistStreamVolumeToSystem(AudioProfileManager.STREAM_NOTIFICATION);
            persistStreamVolumeToSystem(AudioProfileManager.STREAM_ALARM);
            persistRingtoneUriToSystem(AudioProfileManager.TYPE_RINGTONE);
            persistRingtoneUriToSystem(AudioProfileManager.TYPE_NOTIFICATION);
            persistRingtoneUriToSystem(AudioProfileManager.TYPE_VIDEO_CALL);
        }
        
        persistDtmfToneToSystem();
        persistSoundEffectToSystem();
        persistLockScreenToSystem();
        persistHapticFeedbackToSystem();
    }
    
    /**
     * Restores the given profile to default values.
     * 
     * @param profileKey The key of the profile to be restored.
     */
    private void restoreToDefaultValues(String profileKey) {
        // Refresh the profile state in mProfileStates with default profile state, and delete all
        // value exist in database refer to the given profile
        AudioProfileState defaultState = mAudioProfileManager.getDefaultState(profileKey);
        AudioProfileState profileState = getProfileState(profileKey);
        synchronized (mProfileStates) {
            profileState.mRingerStream = getDefaultRingtone(AudioProfileManager.TYPE_RINGTONE);
            profileState.mNotificationStream = getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION);
            profileState.mVideoCallStream = getDefaultRingtone(AudioProfileManager.TYPE_VIDEO_CALL);
            
            profileState.mRingerVolume = defaultState.mRingerVolume;
            profileState.mNotificationVolume = defaultState.mNotificationVolume;
            profileState.mAlarmVolume = defaultState.mAlarmVolume;
            
            profileState.mVibrationEnabled = defaultState.mVibrationEnabled;
            profileState.mDtmfToneEnabled = defaultState.mDtmfToneEnabled;
            profileState.mSoundEffectEnbled = defaultState.mSoundEffectEnbled;
            profileState.mLockScreenSoundEnabled = defaultState.mLockScreenSoundEnabled;
            profileState.mHapticFeedbackEnabled = defaultState.mHapticFeedbackEnabled;
        }
        List<String> keyList = AudioProfileManager.getAllKeys(profileKey);
        StringBuilder sb = new StringBuilder();
        sb.append(Settings.System.NAME);
        sb.append(" in (");
        int size = keyList.size();
        for(int i = 0; i < size - 1; i++) {
            sb.append("?,");
        }
        sb.append("?)");
        String where = sb.toString();
        int deleted = mContentResolver.delete(Settings.System.CONTENT_URI, where, keyList.toArray(new String[size]));
        Log.d(TAG, "restoreToDefaultValues: profileKey = " + profileKey + ", state = " + getProfileState(profileKey).toString());
        
    }
    
    /**
     * sync the three type volumes to system that has been changed in last active profile's settings.
     * One of {@link #STREAM_RING}, {@link #STREAM_NOTIFICATION}  
     *                   or {@link #STREAM_ALARM}.
     * @see #setStreamVolume(String, int, int)
     * @see #getStreamVolume(String, int)
     */
    private void syncVolumeToSystem() {
        int systemVolume = 0;
        int profileVolume = 0;
        // Ringer volume
        if (mShouldSyncToSystem.get(ProfileSettings.ringer_volume.ordinal())) {
            systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_RING);
            profileVolume = getStreamVolume(mActiveProfileKey, AudioProfileManager.STREAM_RING);
            mShouldSyncToSystem.add(ProfileSettings.ringer_volume.ordinal(), false);
            if (profileVolume != systemVolume) {
                persistStreamVolumeToSystem(AudioProfileManager.STREAM_RING);
                Log.d(TAG, "syncVolumeToSystem: profileKey = " + mActiveProfileKey + ", streamType = " + AudioProfileManager.STREAM_RING + ", volume = " + profileVolume);
            }
        }
        
        // Notification volume
        if (mShouldSyncToSystem.get(ProfileSettings.notification_volume.ordinal())) {
            systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_NOTIFICATION);
            profileVolume = getStreamVolume(mActiveProfileKey, AudioProfileManager.STREAM_NOTIFICATION);
            mShouldSyncToSystem.add(ProfileSettings.notification_volume.ordinal(), false);
            if (profileVolume != systemVolume) {
                persistStreamVolumeToSystem(AudioProfileManager.STREAM_NOTIFICATION);
                Log.d(TAG, "syncVolumeToSystem: profileKey = " + mActiveProfileKey + ", streamType = " + AudioProfileManager.STREAM_NOTIFICATION + ", volume = " + profileVolume);
            }
        }
        if (mShouldSyncToSystem.get(ProfileSettings.alarm_volume.ordinal())) {
            systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_ALARM);
            profileVolume = getStreamVolume(mActiveProfileKey, AudioProfileManager.STREAM_ALARM);
            mShouldSyncToSystem.add(ProfileSettings.alarm_volume.ordinal(), false);
            if (profileVolume != systemVolume) {
                persistStreamVolumeToSystem(AudioProfileManager.STREAM_ALARM);
                Log.d(TAG, "syncVolumeToSystem: profileKey = " + mActiveProfileKey + ", streamType = " + AudioProfileManager.STREAM_ALARM + ", volume = " + profileVolume);
            }
        }
    }
    
    /**
     * sync the three type ringtones to system that has been changed in last active profile's settings.
     * One of {@link #TYPE_RINGTONE}, {@link #TYPE_NOTIFICATION}, or {@link #TYPE_VIDEO_CALL}.
     * 
     * @see #setRingtoneUri(String, int, Uri)
     * @see #getRingtoneUri(String, int)
     */
    private void syncRingtoneToSystem() {
        Uri systemUri = null;
        Uri profileUri = null;
        
        // Ringtone
        if (mShouldSyncToSystem.get(ProfileSettings.ringer_stream.ordinal())) {
            systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_RINGTONE);
            profileUri = getRingtoneUri(mActiveProfileKey, AudioProfileManager.TYPE_RINGTONE);
            mShouldSyncToSystem.add(ProfileSettings.ringer_stream.ordinal(), false);
            if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                persistRingtoneUriToSystem(AudioProfileManager.TYPE_RINGTONE);
                Log.d(TAG, "syncRingtoneToSystem: profileKey = " + mActiveProfileKey + ", type = " + AudioProfileManager.TYPE_RINGTONE + ", Uri = " + profileUri);
            }
        }
        
        // Notification
        if (mShouldSyncToSystem.get(ProfileSettings.notification_stream.ordinal())) {
            systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_NOTIFICATION);
            profileUri = getRingtoneUri(mActiveProfileKey, AudioProfileManager.TYPE_NOTIFICATION);
            mShouldSyncToSystem.add(ProfileSettings.notification_stream.ordinal(), false);
            if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                persistRingtoneUriToSystem(AudioProfileManager.TYPE_NOTIFICATION);
                Log.d(TAG, "syncRingtoneToSystem: profileKey = " + mActiveProfileKey + ", type = " + AudioProfileManager.TYPE_NOTIFICATION + ", Uri = " + profileUri);
            }
        }
        
        // Vediocall
        if (mShouldSyncToSystem.get(ProfileSettings.videocall_Stream.ordinal())) {
            systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_VIDEO_CALL);
            profileUri = getRingtoneUri(mActiveProfileKey, AudioProfileManager.TYPE_VIDEO_CALL);
            mShouldSyncToSystem.add(ProfileSettings.videocall_Stream.ordinal(), false);
            if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                persistRingtoneUriToSystem(AudioProfileManager.TYPE_VIDEO_CALL);
                Log.d(TAG, "syncRingtoneToSystem: profileKey = " + mActiveProfileKey + ", type = " + AudioProfileManager.TYPE_VIDEO_CALL + ", Uri = " + profileUri);
            }
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
        if (profileKey != null) {
            Uri uri = null;
            switch (type) {
                case AudioProfileManager.TYPE_RINGTONE:
                    uri = getProfileState(profileKey).mRingerStream;
                    break;
                case AudioProfileManager.TYPE_NOTIFICATION:
                    uri = getProfileState(profileKey).mNotificationStream;
                    break;
                case AudioProfileManager.TYPE_VIDEO_CALL:
                    uri = getProfileState(profileKey).mVideoCallStream;
                    break;
                default:
                    Log.e(TAG, "getRingtoneUri with unsupport type!");
                    return null;
                }
            
            if (SILENT_NOTIFICATION_URI.equals(uri)) {
                // If the uri is special SILENT_NOTIFICATION_URI, return null to make ringtonepicker
                // select silent on checked.
                uri = null;
            } else if (uri == null || !isRingtoneExist(uri)) {
                // When the uri is null or not exist, use default ringtone.
                uri = getDefaultRingtone(type);
            }
            
            Log.d(TAG, "getRingtoneUri: profileKey = " + profileKey + ", type = " + type + ", uri = " + uri);
            return uri;
        } else {
            Log.e(TAG, "getRingtoneUri with null profile key!");
            return null;
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
        return mAudioManager.getStreamMaxVolume(streamType);
    }
    
    /**
     * Ensures the stream volume is in valid range. 
     * 
     * @param streamType The stream type.
     * @param volume The stream volume.
     * @return
     */
    private int getStreamValidVolume(int streamType, int volume) {
        int max = this.getStreamMaxVolume(streamType);
        if(volume < 0) {
            volume = 0;
        } else if(volume > max) {
            volume = max;
        }
        return volume;
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
        
        if (profileKey != null) {
            int volume = 0;
            switch (streamType) {
                case AudioProfileManager.STREAM_RING:
                    volume = getProfileState(profileKey).mRingerVolume;
                    break;
                case AudioProfileManager.STREAM_NOTIFICATION:
                    volume = getProfileState(profileKey).mNotificationVolume;
                    break;
                case AudioProfileManager.STREAM_ALARM:
                    volume = getProfileState(profileKey).mAlarmVolume;
                    break;
                default:
                    Log.e(TAG, "getStreamVolume with unsupport type!");
                    return AudioProfileManager.UNSUPPORT_STREAM_VOLUME;
                }
            int validVolume = getStreamValidVolume(streamType, volume);
            Log.d(TAG, "getStreamVolume: profileKey = " + profileKey + ", streamType = " + streamType + ", volume = " + validVolume);
            return validVolume;
        } else {
            Log.e(TAG, "getStreamVolume with null profile key!");
            return 0;
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
        boolean enabled =getProfileState(profileKey).mVibrationEnabled;
        Log.d(TAG, "getVibrationEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
        return enabled;
    }
    
    /**
     * Gets whether tone should be played when using dial pad with the given profile key.
     * 
     * @param profileKey The profile key whose DtmfTone whether enabled is returned.
     * @return The current DtmfTone status, if enabled return true, otherwise false.
     * @see #setDtmfToneEnabled(boolean)
     */
    public boolean getDtmfToneEnabled(String profileKey) {
        boolean enabled =getProfileState(profileKey).mDtmfToneEnabled;
        Log.d(TAG, "getDtmfToneEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
        return enabled;
    }
    
    /**
     * Gets whether sound should be played when making screen selection.
     * 
     * @param profileKey The profile key whose SoundEffect whether enabled is returned.
     * @return The current SoundEffect status, if enabled return true, otherwise false.
     * @see #setSoundEffectEnabled(boolean)
     */
    public boolean getSoundEffectEnabled(String profileKey) {
        boolean enabled =getProfileState(profileKey).mSoundEffectEnbled;
        Log.d(TAG, "getSoundEffectEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
        return enabled;
    }
    
    /**
     * Gets whether sound should be played when lock or unlock screen.
     * 
     * @param profileKey The profile key whose LockScreen whether enabled is returned.
     * @return The current LockScreen status, if enabled return true, otherwise false.
     * @see #setLockScreenEnabled(String, boolean)
     */
    public boolean getLockScreenEnabled(String profileKey) {
        boolean enabled =getProfileState(profileKey).mLockScreenSoundEnabled;
        Log.d(TAG, "getLockScreenEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
        return enabled;
    }
    
    /**
     * Gets whether the phone should vibrate when pressing soft keys and on certain UI interactions.
     * 
     * @param profileKey The profile key whose HapticFeedback whether enabled is returned.
     * @return The current HapticFeedback status, if enabled return true, otherwise false.
     * @see #setHapticFeedbackEnabled(boolean)
     */
    public boolean getHapticFeedbackEnabled(String profileKey) {
        boolean enabled =getProfileState(profileKey).mHapticFeedbackEnabled;
        Log.d(TAG, "getHapticFeedbackEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
        return enabled;
    }
    
    /**
     * Gets the profile state from profile states hash map with given profile key.
     * 
     * @param profileKey The profile key.
     * @return The current profile state referred to given profile key.
     * 
     */
    private AudioProfileState getProfileState(String profileKey) {
        synchronized (mProfileStates) {
            AudioProfileState profileState = mProfileStates.get(profileKey);
            if (profileState == null) {
                // New a new profile state to add to profile state
                Log.w(TAG, "getProfileState of " + profileKey + "is null, so create new one instead!");
                readPersistedSettings(profileKey);
                profileState = mProfileStates.get(profileKey);
            }
            return profileState;
        }
    }
    
    /**
     * Gets a string list of the profile state from profile states hash map with given profile key.
     * 
     * @param profileKey The profile key.
     * @return A string list of the current profile state referred to given profile key.
     * 
     */
    public List<String> getProfileStateString(String profileKey) {
        AudioProfileState profileState = mProfileStates.get(profileKey);
        int size = ProfileSettings.values().length;
        List<String> state = new ArrayList<String>(size);
        state.add(ProfileSettings.ringer_stream.ordinal(), profileState.mRingerStream.toString());
        state.add(ProfileSettings.notification_stream.ordinal(), profileState.mNotificationStream.toString());
        state.add(ProfileSettings.videocall_Stream.ordinal(), profileState.mVideoCallStream.toString());
        
        state.add(ProfileSettings.ringer_volume.ordinal(), String.valueOf(profileState.mRingerVolume));
        state.add(ProfileSettings.notification_volume.ordinal(), String.valueOf(profileState.mNotificationVolume));
        state.add(ProfileSettings.alarm_volume.ordinal(), String.valueOf(profileState.mAlarmVolume));
        
        state.add(ProfileSettings.vibration_enabled.ordinal(), String.valueOf(profileState.mVibrationEnabled));
        state.add(ProfileSettings.dtmftone_enabled.ordinal(), String.valueOf(profileState.mDtmfToneEnabled));
        state.add(ProfileSettings.soundeffect_enbled.ordinal(), String.valueOf(profileState.mSoundEffectEnbled));
        state.add(ProfileSettings.lockscreensound_enabled.ordinal(), String.valueOf(profileState.mLockScreenSoundEnabled));
        state.add(ProfileSettings.hapticfeedback_enabled.ordinal(), String.valueOf(profileState.mHapticFeedbackEnabled));
        
        Log.d(TAG, "getProfileStateString for profileKey = " + profileKey + ": " + state);
        return state;
    }
    
    /**
     * Returns the name of given custom profile.
     * 
     * @param profileKey The custom profile key.
     * @return profile name
     * @see #setProfileName(String, String)
     */
    public String getProfileName(String profileKey) {
        String profileName = mCustomProfileName.get(profileKey);
        Log.d(TAG, "getProfileName: profileKey = " + profileKey + ", profileName = " + profileName);
        return profileName;
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
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setRingtoneUri profile state not exist!");
            return;
        }
        switch (type) {
            case AudioProfileManager.TYPE_RINGTONE:
                if ((profileState.mRingerStream == null && ringtoneUri != null) || 
                        profileState.mRingerStream != null && !profileState.mRingerStream.equals(ringtoneUri)) {
                    // If the profile's ringtone uri is different from given uri, set the uri to be
                    // profile's ringtone into database and save this change to profile states hash map
                    persistRingtoneUriToDatabase(profileKey, type, ringtoneUri);
                    synchronized (mProfileStates) {
                        profileState.mRingerStream = ringtoneUri;
                    }
                    // If the profile is active profile, persist it to system
                    if (isActive(profileKey)) {
                        persistRingtoneUriToSystem(type);
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.ringer_stream.ordinal(), true);
                    }
                    
                    // Synchronize general to outdoor
                    if (!IS_CMCC) {
                        // New plan in non CMCC project ask synchronize general and outdoor profile's settings,
                        // so if profile is general, we should set value to outdoor at the same time.
                        Scenario scenario = AudioProfileManager.getScenario(profileKey);
                        if (Scenario.GENERAL.equals(scenario)) {
                            String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
                            AudioProfileState outdoorState = getProfileState(outdoorKey);
                            persistRingtoneUriToDatabase(outdoorKey, type, ringtoneUri);
                            synchronized (mProfileStates) {
                                outdoorState.mRingerStream = ringtoneUri;
                            }
                            // If the profile is active profile, persist it to system
                            if (isActive(outdoorKey)) {
                                persistRingtoneUriToSystem(type);
                            }
                            Log.v(TAG, "setRingtoneUri: synchronize general to outdoor!");
                        }
                    }
                }
                break;
            case AudioProfileManager.TYPE_NOTIFICATION:
                if ((profileState.mNotificationStream == null && ringtoneUri != null) || 
                        profileState.mNotificationStream != null && !profileState.mNotificationStream.equals(ringtoneUri)) {
                    // When set uri is null, use SILENT_NOTIFICATION_URI instead of it.
                    if (null == ringtoneUri) {
                        ringtoneUri = SILENT_NOTIFICATION_URI;
                    }
                    
                    // If the profile's ringtone uri is different from given uri, set the uri to be
                    // profile's ringtone into database and save this change to profile states hash map
                    persistRingtoneUriToDatabase(profileKey, type, ringtoneUri);
                    synchronized (mProfileStates) {
                        profileState.mNotificationStream = ringtoneUri;
                    }
                    // If the profile is active profile, persist it to system
                    if (isActive(profileKey)) {
                        persistRingtoneUriToSystem(type);
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.notification_stream.ordinal(), true);
                    }
                    
                    // Synchronize general to outdoor
                    if (!IS_CMCC) {
                        // New plan in non CMCC project ask synchronize general and outdoor profile's settings,
                        // so if profile is general, we should set value to outdoor at the same time.
                        Scenario scenario = AudioProfileManager.getScenario(profileKey);
                        if (Scenario.GENERAL.equals(scenario)) {
                            String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
                            AudioProfileState outdoorState = getProfileState(outdoorKey);
                            persistRingtoneUriToDatabase(outdoorKey, type, ringtoneUri);
                            synchronized (mProfileStates) {
                                outdoorState.mNotificationStream = ringtoneUri;
                            }
                            // If the profile is active profile, persist it to system
                            if (isActive(outdoorKey)) {
                                persistRingtoneUriToSystem(type);
                            }
                            Log.v(TAG, "setRingtoneUri: synchronize general to outdoor!");
                        }
                    }
                }
                break;
            case AudioProfileManager.TYPE_VIDEO_CALL:
                if ((profileState.mVideoCallStream == null && ringtoneUri != null) || 
                        profileState.mVideoCallStream != null && !profileState.mVideoCallStream.equals(ringtoneUri)) {
                    // If the profile's ringtone uri is different from given uri, set the uri to be
                    // profile's ringtone into database and save this change to profile states hash map
                    persistRingtoneUriToDatabase(profileKey, type, ringtoneUri);
                    synchronized (mProfileStates) {
                        profileState.mVideoCallStream = ringtoneUri;
                    }
                    // If the profile is active profile, persist it to system
                    if (isActive(profileKey)) {
                        persistRingtoneUriToSystem(type);
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.videocall_Stream.ordinal(), true);
                    }
                    
                    // Synchronize general to outdoor
                    if (!IS_CMCC) {
                        // New plan in non CMCC project ask synchronize general and outdoor profile's settings,
                        // so if profile is general, we should set value to outdoor at the same time.
                        Scenario scenario = AudioProfileManager.getScenario(profileKey);
                        if (Scenario.GENERAL.equals(scenario)) {
                            String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
                            AudioProfileState outdoorState = getProfileState(outdoorKey);
                            persistRingtoneUriToDatabase(outdoorKey, type, ringtoneUri);
                            synchronized (mProfileStates) {
                                outdoorState.mVideoCallStream = ringtoneUri;
                            }
                            // If the profile is active profile, persist it to system
                            if (isActive(outdoorKey)) {
                                persistRingtoneUriToSystem(type);
                            }
                            Log.v(TAG, "setRingtoneUri: synchronize general to outdoor!");
                        }
                    }
                }
                break;
        }
        Log.d(TAG, "setRingtoneUri: profileKey = " + profileKey + ", type = " + type + ", uri = " + ringtoneUri);
        
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
        int validIndex = this.getStreamValidVolume(streamType, index);
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setStreamVolume profile state not exist!");
            return;
        }
        
        switch (streamType) {
            case AudioProfileManager.STREAM_RING:
                if (profileState.mRingerVolume != validIndex) {
                    // If the profile's volume is different from given volume, set the volume to be
                    // profile's volume into database and save this change to profile states hash map
                    persistStreamVolumeToDatabase(profileKey, streamType, validIndex);
                    synchronized (mProfileStates) {
                        profileState.mRingerVolume = validIndex;
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.ringer_volume.ordinal(), true);
                    }
                }
                break;
            case AudioProfileManager.STREAM_NOTIFICATION:
                if (profileState.mNotificationVolume != validIndex) {
                    // If the profile's volume is different from given volume, set the volume to be
                    // profile's volume into database and save this change to profile states hash map
                    persistStreamVolumeToDatabase(profileKey, streamType, validIndex);
                    synchronized (mProfileStates) {
                        profileState.mNotificationVolume = validIndex;
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.notification_volume.ordinal(), true);
                    }
                }
                break;
            case AudioProfileManager.STREAM_ALARM:
                if (profileState.mAlarmVolume != validIndex) {
                    // If the profile's volume is different from given volume, set the volume to be
                    // profile's volume into database and save this change to profile states hash map
                    persistStreamVolumeToDatabase(profileKey, streamType, validIndex);
                    synchronized (mProfileStates) {
                        profileState.mAlarmVolume = validIndex;
                    }
                    
                    // If the profile is last active profile, we should save user change and sync 
                    // to system when the profile was set to active profile.
                    if (profileKey.endsWith(mLastActiveProfileKey)) {
                        mShouldSyncToSystem.set(ProfileSettings.alarm_volume.ordinal(), true);
                    }
                }
        }
        Log.d(TAG, "setStreamVolume: profileKey = " + profileKey + ", streamType = " + streamType + ", volume = " + validIndex);
    }
    
    /**
     * Sets whether the phone should vibrate for incoming calls.
     * 
     * @param profileKey The profile key given to set vibration enabled.
     * @param enabled Whether vibration enabled.
     * @see #getVibrationEnabled()
     */
    public void setVibrationEnabled(String profileKey, boolean enabled) {
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setVibrationEnabled profile state not exist!");
            return;
        }
        
        if(profileState.mVibrationEnabled != enabled) {
            // Only persist silent profile's vibrate_in_silent parameter. 
            // For it only has impacts in RINGER_MODE_SILENT and RINGER_MODE_VIBRATE.
            if(Scenario.SILENT.equals(AudioProfileManager.getScenario(profileKey))) {
                Settings.System.putInt(mContentResolver, Settings.System.VIBRATE_IN_SILENT,
                        enabled ? 1 : 0);
                //If the profile is silent and is active,change ringermode
                if (isActive(profileKey)) {
                    if (enabled) {
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        mRingerMode = AudioManager.RINGER_MODE_VIBRATE;
                        Log.d(TAG, "setVibrationEnabled true,change RingerMode to VIBRATE");
                    } else {
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        mRingerMode = AudioManager.RINGER_MODE_SILENT;
                        Log.d(TAG, "setVibrationEnabled false,change RingerMode to SILENT");
                    }
                }
            }
            // Save this change to profile states hash map
            persistVibrationToDatabase(profileKey, enabled);
            synchronized (mProfileStates) {
                profileState.mVibrationEnabled = enabled;
            }
            // If the profile is active profile, persist it to system
            if(isActive(profileKey)) {
                persistVibrationToSystem();
            }
            Log.d(TAG, "setVibrationEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
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
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setDtmfToneEnabled profile state not exist!");
            return;
        }
        
        if(profileState.mDtmfToneEnabled != enabled) {
            // Save this change to profile states hash map
            persistDtmfToneToDatabase(profileKey, enabled);
            synchronized (mProfileStates) {
                profileState.mDtmfToneEnabled = enabled;
            }
            // If the profile is active profile, persist it to system
            if(isActive(profileKey)) {
                persistDtmfToneToSystem();
            }
            Log.d(TAG, "setDtmfToneEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
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
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setSoundEffectEnabled profile state not exist!");
            return;
        }
        
        if(profileState.mSoundEffectEnbled != enabled) {
            // Save this change to profile states hash map
            persistSoundEffectToDatabase(profileKey, enabled);
            synchronized (mProfileStates) {
                profileState.mSoundEffectEnbled = enabled;
            }
            // If the profile is active profile, persist it to system
            if(isActive(profileKey)) {
                persistSoundEffectToSystem();
            }
            Log.d(TAG, "setSoundEffectEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
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
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setLockScreenEnabled profile state not exist!");
            return;
        }
        
        if(profileState.mLockScreenSoundEnabled != enabled) {
            // Save this change to profile states hash map
            persistLockScreenToDatabase(profileKey, enabled);
            synchronized (mProfileStates) {
                profileState.mLockScreenSoundEnabled = enabled;
            }
            // If the profile is active profile, persist it to system
            if(isActive(profileKey)) {
                persistLockScreenToSystem();
            }
            Log.d(TAG, "setLockScreenEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
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
        AudioProfileState profileState = getProfileState(profileKey);
        if (profileState == null) {
            Log.e(TAG, "setHapticFeedbackEnabled profile state not exist!");
            return;
        }
        
        if(profileState.mHapticFeedbackEnabled != enabled) {
            // Save this change to profile states hash map
            persistHapticFeedbackToDatabase(profileKey, enabled);
            synchronized (mProfileStates) {
                profileState.mHapticFeedbackEnabled = enabled;
            }
            // If the profile is active profile, persist it to system
            if(isActive(profileKey)) {
                persistHapticFeedbackToSystem();
            }
            Log.d(TAG, "setHapticFeedbackEnabled: profileKey = " + profileKey + ", enabled = " + enabled);
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
        String profileName = mCustomProfileName.get(profileKey);
        if((profileName != null && !profileName.equals(newName)) 
                || (profileName == null && newName != null)) {
            // Save this change to profile states hash map
            persistProfileNameToDatabase(profileKey, newName);
            mCustomProfileName.put(profileKey, newName);
            Log.d(TAG, "setProfileName: profileKey = " + profileKey + ", newName = " + newName);
        } else {
            Log.e(TAG, "setProfileName with Null name!");
        }
    } 
   

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // PersistToDatabase methods to persist settings to database with given profile key and value
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Persist the ringtone uri of particular stream to database.
     * @param profileKey The key of the profile to be persist.
     * @param type The type whose default sound should be set. 
     *             One of {@link AudioProfileManager#TYPE_RINGTONE},
     *             {@link AudioProfileManager#TYPE_NOTIFICATION},
     *             or {@link AudioProfileManager#TYPE_VIDEO_CALL}.
     * @param uri The uri to be persist to database
     */
    private void persistRingtoneUriToDatabase(String profileKey, int type, Uri uri) {
        Message msg = new Message();
        String name = AudioProfileManager.getStreamUriKey(profileKey, type);
        Bundle bundle = new Bundle();
        bundle.putString(name, (uri == null ? null : uri.toString()));
        switch (type) {
            case AudioProfileManager.TYPE_RINGTONE:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_VOICECALL_RINGTONE_TO_DATABASE, -1, -1, name);
                break;
                
            case AudioProfileManager.TYPE_NOTIFICATION:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_NOTIFICATION_RINGTONE_TO_DATABASE, -1, -1, name);
                break;
                
            case AudioProfileManager.TYPE_VIDEO_CALL:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_VIDEOCALL_RINGTONE_TO_DATABASE, -1, -1, name);
                break;
                
        }
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the ringtone volume of particular stream to database.
     * @param profileKey The key of the profile to be persist.
     * @param streamType The stream type whose volume index to be persisted.
     *             One of {@link AudioProfileManager#STREAM_RING},
     *             {@link AudioProfileManager#STREAM_NOTIFICATION},
     *             or {@link AudioProfileManager#STREAM_ALARM}.
     * @param value The volume value to be persist to database
     */
    private void persistStreamVolumeToDatabase(String profileKey, int streamType, int value) {
        Message msg = new Message();
        String name = AudioProfileManager.getStreamVolumeKey(profileKey, streamType);
        switch (streamType) {
            case AudioProfileManager.STREAM_RING:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_RINGER_VOLUME_TO_DATABASE, value, -1, name);
                break;
                
            case AudioProfileManager.STREAM_NOTIFICATION:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_NOTIFICATION_VOLUME_TO_DATABASE, value, -1, name);
                break;
                
            case AudioProfileManager.STREAM_ALARM:
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_ALARM_VOLUME_TO_DATABASE, value, -1, name);
                break;
                
        }
        msg.sendToTarget();
    }
    
    /**
     * Persist the setting to database that indicates whether phone should vibrate for incoming calls.
     * @param profileKey The key of the profile to be persist.
     * @param enabled The vibration status to be persist to database
     */
    private void persistVibrationToDatabase(String profileKey, boolean enabled) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getVibrationKey(profileKey);
        bundle.putString(name, String.valueOf(enabled).toString());
        bundle.putString("Vibration", String.valueOf(enabled));
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_VIBRATION_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the setting to database that indicates whether sound should be played 
     * when using dial pad.
     * @param profileKey The key of the profile to be persist.
     * @param enabled The DtmfTone status to be persist to database
     */
    private void persistDtmfToneToDatabase(String profileKey, boolean enabled) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getDtmfToneKey(profileKey);
        bundle.putString(name, String.valueOf(enabled));
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_DTMF_TONE_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the setting to database that indicates whether sound should be played 
     * when making screen selection.
     * @param profileKey The key of the profile to be persist.
     * @param enabled The SoundEffect status to be persist to database
     */
    private void persistSoundEffectToDatabase(String profileKey, boolean enabled) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getSoundEffectKey(profileKey);
        bundle.putString(name, String.valueOf(enabled));
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_SOUND_EFFECT_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the setting to database that indicates whether to play sounds 
     * when the keyguard is shown and dismissed.
     * @param profileKey The key of the profile to be persist.
     * @param enabled The LockScreenSound status to be persist to database
     */
    private void persistLockScreenToDatabase(String profileKey, boolean enabled) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getLockScreenKey(profileKey);
        bundle.putString(name, String.valueOf(enabled));
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_LOCKSCREEN_SOUND_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the setting to database that indicates whether the phone should vibrate 
     * when pressing soft keys and on certain UI interactions.
     * @param profileKey The key of the profile to be persist.
     * @param enabled The LockScreenSound status to be persist to database
     */
    private void persistHapticFeedbackToDatabase(String profileKey, boolean enabled) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getHapticKey(profileKey);
        bundle.putString(name, String.valueOf(enabled));
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_HAPTIC_FEEDBACK_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the profile name to database.
     * @param profileKey The key of the profile to be persist.
     * @param profileName The profile name to be persist to database
     */
    private void persistProfileNameToDatabase(String profileKey, String profileName) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = AudioProfileManager.getProfileNameKey(profileKey);
        bundle.putString(name, profileName);
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_PROFILE_NAME_TO_DATABASE, -1, -1, name);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // PersistToSystem methods to persist active profile settings to system
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Persist the active profile ringtone uri to system to make it effective immediately 
     * with given type
     * 
     * @param type The type whose default sound should be set. 
     *             One of {@link AudioProfileManager#TYPE_RINGTONE},
     *             {@link AudioProfileManager#TYPE_NOTIFICATION},
     *             or {@link AudioProfileManager#TYPE_VIDEO_CALL}.
     */
    private void persistRingtoneUriToSystem(int type) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        String name = null;
        Uri uri = null;
        
        switch (type) {
            case AudioProfileManager.TYPE_RINGTONE:
                name = String.valueOf(type);
                uri = getProfileState(mActiveProfileKey).mRingerStream;
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_VOICECALL_RINGTONE_TO_SYSTEM, -1, -1, name);
                break;
                
            case AudioProfileManager.TYPE_NOTIFICATION:
                name = String.valueOf(type);
                uri = getProfileState(mActiveProfileKey).mNotificationStream;
                // If uri is the special SILENT_NOTIFICATION_URI, use null to persist it to system
                if (SILENT_NOTIFICATION_URI.equals(uri)) {
                    uri = null;
                }
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_NOTIFICATION_RINGTONE_TO_SYSTEM, -1, -1, name);
                break;
                
            case AudioProfileManager.TYPE_VIDEO_CALL:
                name = String.valueOf(type);
                uri = getProfileState(mActiveProfileKey).mVideoCallStream;
                msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_VIDEOCALL_RINGTONE_TO_SYSTEM, -1, -1, name);
                break;
            default:
                Log.e(TAG, "persistRingtoneUriToSystem with unsupport type!");
                return;
        }
        bundle.putString(name, (uri == null ? null : uri.toString()));
        msg.setData(bundle);
        msg.sendToTarget();
    }
    
    /**
     * Persist the active profile volume to system to make it effective immediately 
     * with given type
     * 
     * @param type The stream type whose volume index to be persisted.
     *             One of {@link AudioProfileManager#STREAM_RING},
     *             {@link AudioProfileManager#STREAM_NOTIFICATION},
     *             or {@link AudioProfileManager#STREAM_ALARM}.
     */
    private void persistStreamVolumeToSystem(int streamType) {
        int flags = 0;
        int volume = 0;
        if (AudioProfileManager.IS_CMCC) {
            switch (streamType) {
                case AudioProfileManager.STREAM_RING:
                    volume = getProfileState(mActiveProfileKey).mRingerVolume;
                    mAudioManager.setAudioProfileStreamVolume(AudioManager.STREAM_RING, volume , flags);
                    break;
                case AudioProfileManager.STREAM_NOTIFICATION:
                    volume = getProfileState(mActiveProfileKey).mNotificationVolume;
                    mAudioManager.setAudioProfileStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, flags);
                    break;
                case AudioProfileManager.STREAM_ALARM:
                    volume = getProfileState(mActiveProfileKey).mAlarmVolume;
                    mAudioManager.setAudioProfileStreamVolume(AudioManager.STREAM_ALARM, volume, flags);
                    break;
                default:
                    Log.e(TAG, "CMCC: persistStreamVolumeToSystem with unsupport type!");
                    return;
            }
            Log.d(TAG, "CMCC: persistStreamVolumeToSystem: streamType = " + streamType + ", volume = " + volume);
        } else {
            switch (streamType) {
                case AudioProfileManager.STREAM_RING:
                    volume = getProfileState(mActiveProfileKey).mRingerVolume;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING, volume , flags);
                    break;
                case AudioProfileManager.STREAM_NOTIFICATION:
                    volume = getProfileState(mActiveProfileKey).mNotificationVolume;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, flags);
                    break;
                case AudioProfileManager.STREAM_ALARM:
                    volume = getProfileState(mActiveProfileKey).mAlarmVolume;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, flags);
                    break;
                default:
                    Log.e(TAG, "persistStreamVolumeToSystem with unsupport type!");
                    return;
            }
            Log.d(TAG, "persistStreamVolumeToSystem: streamType = " + streamType + ", volume = " + volume);
        }
    }
    
    /**
     * Persist the active profile vibration status to system to make it effective immediately.
     */
    private void persistVibrationToSystem() {
        // Avoid CTS fail, so when test CTS delay to set vibrate
        int vibratinRinger = mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        int vibratinNotification = mAudioManager.getVibrateSetting(
                AudioManager.VIBRATE_TYPE_NOTIFICATION);
        Log.d(TAG, "persistVibrationToSystem current vibrate status: ringer = " + vibratinRinger
                + ", notification = " + vibratinNotification);
        
        if (vibratinRinger != vibratinNotification) {
            Log.d(TAG, "persistVibrationToSystem different vibrate settings,"
                    + " so CTS test running, delay 10 sec to set vibration!");
            mAudioProfileHandler.removeMessages(MSG_DELAY_SET_VIBRATE_AVOID_CTS_FAIL);
            mAudioProfileHandler.sendEmptyMessageDelayed(MSG_DELAY_SET_VIBRATE_AVOID_CTS_FAIL,
                    DELAY_TIME_AVOID_CTS_FAIL);
            mDelaySetVibrate = true;
            return;
        }
        if (mDelaySetVibrate) {
            Log.d(TAG, "persistVibrationToSystem: CTS test running,delay 20 sec to set vibration!");
            return;
        }
        
        // If vibrate on for ringer has been checked,use VIBRATE_SETTING_ON
        // Otherwise use VIBRATE_SETTING_ONLY_SILENT.
        int vibrationStatus = getProfileState(mActiveProfileKey).mVibrationEnabled ?
                AudioManager.VIBRATE_SETTING_ON : AudioManager.VIBRATE_SETTING_ONLY_SILENT;
       
        mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibrationStatus);
        mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, vibrationStatus);
        Log.d(TAG, "persistVibrationToSystem set ringer and notification vibrate to: "
                + vibrationStatus);
    }
    
    /**
     * Persist the active profile DtmfTone status to system to make it effective immediately.
     */
    private void persistDtmfToneToSystem() {
        Message msg = new Message();
        String name = Settings.System.DTMF_TONE_WHEN_DIALING;
        int enabled = getProfileState(mActiveProfileKey).mDtmfToneEnabled ? 1 : 0;
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_DTMF_TONE_TO_SYSTEM, enabled, -1, name);
        msg.sendToTarget();
    }
    
    /**
     * Persist the active profile SoundEffect status to system to make it effective immediately.
     */
    private void persistSoundEffectToSystem() {
        Message msg = new Message();
        String name = Settings.System.SOUND_EFFECTS_ENABLED;
        int enabled = getProfileState(mActiveProfileKey).mSoundEffectEnbled ? 1 : 0;
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_SOUND_EFFECT_TO_SYSTEM, enabled, -1, name);
        msg.sendToTarget();
    }
    
    /**
     * Persist the active profile LockScreen status to system to make it effective immediately.
     */
    private void persistLockScreenToSystem() {
        Message msg = new Message();
        String name = Settings.System.LOCKSCREEN_SOUNDS_ENABLED;
        int enabled = getProfileState(mActiveProfileKey).mLockScreenSoundEnabled ? 1 : 0;
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_LOCKSCREEN_SOUND_TO_SYSTEM, enabled, -1, name);
        msg.sendToTarget();
    }
    
    /**
     * Persist the active profile HapticFeedback status to system to make it effective immediately.
     */
    private void persistHapticFeedbackToSystem() {
        Message msg = new Message();
        String name = Settings.System.HAPTIC_FEEDBACK_ENABLED;
        int enabled = getProfileState(mActiveProfileKey).mHapticFeedbackEnabled ? 1 : 0;
        msg = mAudioProfileHandler.obtainMessage(MSG_PERSIST_HAPTIC_FEEDBACK_TO_SYSTEM, enabled, -1, name);
        msg.sendToTarget();
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
        synchronized (mActiveProfileKey) {
            boolean actived = (profileKey != null && profileKey.equals(mActiveProfileKey));
            Log.d(TAG, "isActive: profileKey = " + profileKey + ", actived = " + actived);
            return actived;
        }
    }
    
    public boolean isRingtoneExist(Uri uri) {
        try {
            AssetFileDescriptor fd = mContentResolver.openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return false;
            } else {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
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
        String uriString = null;
        switch (type) {
            case AudioProfileManager.TYPE_RINGTONE:
                uriString = Settings.System.getString(mContentResolver, AudioProfileManager.KEY_DEFAULT_RINGTONE);
                break;
            case AudioProfileManager.TYPE_NOTIFICATION:
                uriString = Settings.System.getString(mContentResolver, AudioProfileManager.KEY_DEFAULT_NOTIFICATION);
                break;
            case AudioProfileManager.TYPE_VIDEO_CALL:
                uriString = Settings.System.getString(mContentResolver, AudioProfileManager.KEY_DEFAULT_VIDEO_CALL);
                break;
            default:
                Log.e(TAG, "getRingtoneUri with unsupport type!");
                return null;
            }
        Uri uri = (uriString == null ? null : Uri.parse(uriString));
        Log.d(TAG, "getDefaultRingtone: type = " + type + ", default uri = " + uri);
        return uri;
    }
    
    /**
     * Read all the existed profiles' keys to mKey list from database.
     * 
     * @return The existed profiles' keys.
     */
    private List<String> readProfileKeys() {
        List<String> keys = new ArrayList<String>();
        
        // Gets the predefined profiles' keys.
        keys.addAll(getPredefinedProfileKeys());
        
        //Gets the custom profiles's keys.
        String nameColumn = Settings.System.NAME;
        String valueColumn = Settings.System.VALUE;
        
        String[] projection = new String[] {
                Settings.System._ID,
                valueColumn
        };
        
        String customPrefix = AudioProfileManager.getProfileKey(Scenario.CUSTOM);
        StringBuffer selection = new StringBuffer();
        selection.append(nameColumn).append(" like '").append(customPrefix).append("_%")
                .append(AudioProfileManager.SUFFIX_KEY).append("'");
        Log.d(TAG, "readProfileKeys: selection = " + selection.toString());
        
        String sortOrder = Settings.System._ID + " desc";
        Cursor cursor = mContentResolver.query(
                Settings.System.CONTENT_URI, 
                projection, 
                selection.toString(), 
                null, 
                sortOrder);
        if(cursor != null) {
            int valueIndex = cursor.getColumnIndex(valueColumn);
            while (cursor.moveToNext()) {
                String key = cursor.getString(valueIndex);
                if (null == key || "".equals(key)) {
                    Log.e(TAG, "readProfileKeys: Null custom key!");
                    continue;
                }
                keys.add(key);
                Log.d(TAG, "readProfileKeys: Get custom key: " + key);
            }
            
            cursor.close();
        } else {
            Log.e(TAG, "getProfileKeys: Null cursor!");
        }
        
        return keys;
    }
    
    /**
     * Reads the persisted settings to {@link mProfileStates} hash map.
     * 
     * @param profileKey The profile key.
     */
    private void readPersistedSettings(String profileKey) {
        
        if(profileKey == null) {
            Log.e(TAG, "readPersistedSettings with Null profile key!");
            return;
        }
        // query all value refer to profile key in database to be the init profile states values
        String[] projection = new String[] {
                Settings.System._ID,
                Settings.System.NAME,
                Settings.System.VALUE
        }; 
        String selection = Settings.System.NAME + " like '" + profileKey + "%'" ;
        Cursor cursor = mContentResolver.query(Settings.System.CONTENT_URI, 
                projection, selection, null, null);
        HashMap<String, String> initValues = new HashMap<String, String>();
        if(cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(Settings.System.NAME);
            int valueIndex = cursor.getColumnIndex(Settings.System.VALUE);
            do {
                String name = cursor.getString(nameIndex);
                String value = cursor.getString(valueIndex);
                initValues.put(name, value);
            } while(cursor.moveToNext()); 
        } else {
            Log.w(TAG, "readPersistedSettings: No value for " + profileKey);
        }
        
        if(cursor != null) {
            cursor.close();
        }
        
        Uri[] persistedUri = new Uri[3];
        int[] persistedVolume = new int[3];
        boolean[] persistedEnabled = new boolean[5];
        String name = null;
        AudioProfileState defaultState = mAudioProfileManager.getDefaultState(profileKey);
        // first get persisted Ringer,video Call and Notification uri
        name = AudioProfileManager.getStreamUriKey(profileKey, AudioProfileManager.TYPE_RINGTONE);
        persistedUri[0] = getPersistedValue(name, initValues, getDefaultRingtone(AudioProfileManager.TYPE_RINGTONE));
        
        name = AudioProfileManager.getStreamUriKey(profileKey, AudioProfileManager.TYPE_NOTIFICATION);
        persistedUri[1] = getPersistedValue(name, initValues, getDefaultRingtone(AudioProfileManager.TYPE_NOTIFICATION));
        
        name = AudioProfileManager.getStreamUriKey(profileKey, AudioProfileManager.TYPE_VIDEO_CALL);
        persistedUri[2] = getPersistedValue(name, initValues, getDefaultRingtone(AudioProfileManager.TYPE_VIDEO_CALL));
        
        // Second get persisted Ringer,Notification and alarm volume
        name = AudioProfileManager.getStreamVolumeKey(profileKey, AudioProfileManager.STREAM_RING);
        persistedVolume[0] = getPersistedValue(name, initValues, defaultState.mRingerVolume);

        name = AudioProfileManager.getStreamVolumeKey(profileKey, AudioProfileManager.STREAM_NOTIFICATION);
        persistedVolume[1] = getPersistedValue(name, initValues, defaultState.mNotificationVolume);
        
        name = AudioProfileManager.getStreamVolumeKey(profileKey, AudioProfileManager.STREAM_ALARM);
        persistedVolume[2] = getPersistedValue(name, initValues, defaultState.mAlarmVolume);
        
        // Third get persisted vibration,sound effect,dtmf tone,haptic feedback and lock screen sound whether enabled
        name = AudioProfileManager.getVibrationKey(profileKey);
        persistedEnabled[0] = getPersistedValue(name, initValues, defaultState.mVibrationEnabled);
        
        name = AudioProfileManager.getDtmfToneKey(profileKey);
        persistedEnabled[1] = getPersistedValue(name, initValues, defaultState.mDtmfToneEnabled);
        
        name = AudioProfileManager.getSoundEffectKey(profileKey);
        persistedEnabled[2] = getPersistedValue(name, initValues, defaultState.mSoundEffectEnbled);
        
        name = AudioProfileManager.getLockScreenKey(profileKey);
        persistedEnabled[3] = getPersistedValue(name, initValues, defaultState.mLockScreenSoundEnabled);
        
        name = AudioProfileManager.getHapticKey(profileKey);
        persistedEnabled[4] = getPersistedValue(name, initValues, defaultState.mHapticFeedbackEnabled);
        
        // Put persisted state to mProfileStates
        AudioProfileState persistedState = new AudioProfileState(persistedUri, persistedVolume, persistedEnabled);
        mProfileStates.put(profileKey, persistedState);
        // If profile is custom, put it profile name to mCustomProfileName
        if (Scenario.CUSTOM.equals(AudioProfileManager.getScenario(profileKey))) {
            name = AudioProfileManager.getProfileNameKey(profileKey);
            String profileName = initValues.get(name);
            mCustomProfileName.put(profileKey, profileName);
        }
       
        Log.d(TAG, "readPersistedSettings with " + profileKey + ": " + persistedState.toString());
    }
    
    /**
     * Initial mShouldSyncToSystem, when initial AudioProfileService.if active profile is silent
     * or meeting, we should initial mShouldSyncToSystem to make sure it enable after power up again.
     * @see #mShouldSyncToSystem
     */
    private void readShouldSyncToSystem() {
        for (int i = 0; i < ProfileSettings.values().length; i++) {
            mShouldSyncToSystem.add(false);
        }
        Scenario activeScenario = AudioProfileManager.getScenario(mActiveProfileKey);
        if (IS_CMCC) {
            // We only need initial mShouldSyncToSystem when active profile is silent and 
            // the profile's volumes(or ringtones) don't equal to system.
            if (Scenario.SILENT.equals(activeScenario)) {
                // Voluem
                int systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_RING);
                int profileVolume = getProfileState(mActiveProfileKey).mRingerVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.ringer_volume.ordinal(), true);
                }
                
                systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_NOTIFICATION);
                profileVolume = getProfileState(mActiveProfileKey).mNotificationVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.notification_volume.ordinal(), true);
                }
                
                systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_ALARM);
                profileVolume = getProfileState(mActiveProfileKey).mNotificationVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.alarm_volume.ordinal(), true);
                }
                
                // Ringtone
                Uri systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_RINGTONE);
                Uri profileUri = getProfileState(mActiveProfileKey).mRingerStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.ringer_stream.ordinal(), true);
                }
                
                systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_NOTIFICATION);
                profileUri = getProfileState(mActiveProfileKey).mNotificationStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.notification_stream.ordinal(), true);
                }
                
                systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_VIDEO_CALL);
                profileUri = getProfileState(mActiveProfileKey).mVideoCallStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.videocall_Stream.ordinal(), true);
                }
            }
            Log.d(TAG, "CMCC: readShouldSyncToSystem: mShouldSyncToSystem = " + mShouldSyncToSystem);
        } else {
            // We only need initial mShouldSyncToSystem when active profile is silent or meeting and 
            // the profile's volumes(or ringtones) don't equal to system.
            if (Scenario.SILENT.equals(activeScenario) || Scenario.MEETING.equals(activeScenario)) {
                // Voluem
                int systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_RING);
                int profileVolume = getProfileState(mActiveProfileKey).mRingerVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.ringer_volume.ordinal(), true);
                }
                
                systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_NOTIFICATION);
                profileVolume = getProfileState(mActiveProfileKey).mNotificationVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.notification_volume.ordinal(), true);
                }
                
                systemVolume = mAudioManager.getStreamVolume(AudioProfileManager.STREAM_ALARM);
                profileVolume = getProfileState(mActiveProfileKey).mNotificationVolume;
                if (profileVolume != systemVolume) {
                    mShouldSyncToSystem.set(ProfileSettings.alarm_volume.ordinal(), true);
                }
                
                // Ringtone
                Uri systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_RINGTONE);
                Uri profileUri = getProfileState(mActiveProfileKey).mRingerStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.ringer_stream.ordinal(), true);
                }
                
                systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_NOTIFICATION);
                profileUri = getProfileState(mActiveProfileKey).mNotificationStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.notification_stream.ordinal(), true);
                }
                
                systemUri = RingtoneManager.getActualDefaultRingtoneUri(mContext, AudioProfileManager.TYPE_VIDEO_CALL);
                profileUri = getProfileState(mActiveProfileKey).mVideoCallStream;
                if ((profileUri != null && !profileUri.equals(systemUri)) || (profileUri == null && systemUri != null)) {
                    mShouldSyncToSystem.set(ProfileSettings.videocall_Stream.ordinal(), true);
                }
            }
            Log.d(TAG, "readShouldSyncToSystem: mShouldSyncToSystem = " + mShouldSyncToSystem);
        }
    }
    
    /**
     * Check the default profiles' settings. In FDD branch silent and meeting must be default value 
     * and outdoor must be same as general except volume and vibration(outdoor has max volume and 
     * always vibrate)
     */
    private void checkDefaultProfiles() {
        if (IS_CMCC) {
            return;
        }
        Log.d(TAG, "checkDefaultProfiles>>>");
        String generalKey = AudioProfileManager.getProfileKey(Scenario.GENERAL);
        String outdoorKey = AudioProfileManager.getProfileKey(Scenario.OUTDOOR);
        List<String> defaultKeys = getPredefinedProfileKeys();
        defaultKeys.remove(generalKey);
        for (String profileKey : defaultKeys) {
            restoreToDefaultValues(profileKey);
        }
        // Sync ringtones from general to outdoor(outdoor has max volume, always vibrate and it's
        // default four effect settings.)
        AudioProfileState generalState = getProfileState(generalKey);
        AudioProfileState outdoorState = getProfileState(outdoorKey);
        
        // Sync three type ringtones
        outdoorState.mRingerStream = generalState.mRingerStream;
        outdoorState.mNotificationStream = generalState.mNotificationStream;
        outdoorState.mVideoCallStream = generalState.mVideoCallStream;
        
        // Make sure notification volume is the same as ringer volume
        for (String profileKey : mKeys) {
            getProfileState(profileKey).mNotificationVolume = getProfileState(profileKey).mRingerVolume;
        }
        Log.d(TAG, "checkDefaultProfiles<<<");
    }
    
    /**
     * Sync the ringer(also has notification) volume to profile. Because when other app change system
     * volume to non-zero at silent or meeting profile, it will cause ringermode change and will make
     * AudioProfileService to change profile to match this ringermode, so we should sync the volume
     * to profile volume to make profile's volume equal to system's.
     * One of {@link #TYPE_RINGTONE}, {@link #TYPE_NOTIFICATION}.
     * 
     * @param profileKey The profile key to sync volume.
     * @param volume The volume to sync to profile.
     */
    private void syncRingerVolumeToProfile(String profileKey, int volume) {
        if (getProfileState(profileKey).mRingerVolume != volume) {
            mAudioProfileHandler.removeMessages(MSG_PERSIST_RINGER_VOLUME_TO_DATABASE);
            mAudioProfileHandler.removeMessages(MSG_PERSIST_NOTIFICATION_VOLUME_TO_DATABASE);
            persistStreamVolumeToDatabase(profileKey, AudioProfileManager.STREAM_RING, volume);
            persistStreamVolumeToDatabase(profileKey, AudioProfileManager.STREAM_NOTIFICATION, volume);
            getProfileState(profileKey).mRingerVolume = volume;
            getProfileState(profileKey).mNotificationVolume = volume;
            Log.d(TAG, "syncRingerVolumeToProfile: profileKey = " + profileKey + ", volume = " + volume);
        }
    }
    
    /**
     * Gets the uri of particular stream from database.
     * 
     * @param name The name of the setting to be retrieved.
     * @param initValues A container that holds the settings' values of a profile get from database.
     * @param defaultUri The uri to be returned when the setting does't exist.
     * @return The persisted value if the setting exists in database, otherwise the defaultValue. 
     */
    private Uri getPersistedValue(String name, HashMap<String, String> initValues, Uri defaultUri) {
        if (name != null) {
            String uriString = initValues.get(name);
            return (uriString == null ? defaultUri : Uri.parse(uriString));
        }
        return defaultUri;
    }
    
    /**
     * Gets the persisted setting from database.
     * 
     * @param name The name of the setting to be retrieved.
     * @param initValues A container that holds the settings' values of a profile get from database.
     * @param defaultValue The value to be returned when the setting does't exist.
     * @return The persisted value if the setting exists in database, otherwise the defaultValue. 
     */
    private int getPersistedValue(String name, HashMap<String, String> initValues, int defaultValue) {
        if(name != null) {
            String value = initValues.get(name);
            return (value == null ? defaultValue : Integer.valueOf(value));
        }
        return defaultValue;
    }

    /**
     * Gets the persisted setting from database.
     * 
     * @param name The name of the setting to be retrieved.
     * @param initValues A container that holds the settings' values of a profile get from database.
     * @param defaultValue The value to be returned when the setting does't exist.
     * @return The persisted value if the setting exists in database, otherwise the defaultValue. 
     */
    private boolean getPersistedValue(String name, HashMap<String, String> initValues, boolean defaultValue) {
        if(name != null) {
            String value = initValues.get(name);
            return (value == null ? defaultValue : Boolean.valueOf(value));
        }
        return defaultValue;
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
        if (event != 0) {
            synchronized (mRecords) {
                // register callback in AudioProfileService, if the callback is exist, just replace the event.
                Record record = null;
                find_and_add: {
                    IBinder binder = callback.asBinder();
                    final int N = mRecords.size();
                    for (int i = 0; i < N; i++) {
                        record = mRecords.get(i);
                        if (binder == record.mBinder) {
                            break find_and_add;
                        }
                    }
                    record = new Record();
                    record.mBinder = binder;
                    record.mCallback = callback;
                    mRecords.add(record);
                }
                record.mEvent = event;
                
                if (event == AudioProfileListener.LISTEN_AUDIOPROFILE_CHANGEG) {
                    try {
                        record.mCallback.onAudioProfileChanged(mActiveProfileKey);
                    } catch (RemoteException e) {
                        remove(record.mBinder);
                        Log.e(TAG, "Dead object in listenAudioProfie, remove listener's callback!" + e);
                    }
                } else if (event == AudioProfileListener.LISTEN_RINGER_VOLUME_CHANGED) {
                    try {
                        record.mCallback.onRingerVolumeChanged(getProfileState(mActiveProfileKey).mRingerVolume, getProfileState(mActiveProfileKey).mRingerVolume, mActiveProfileKey);
                    } catch (RemoteException e) {
                        remove(record.mBinder);
                        Log.e(TAG, "Dead object in listenAudioProfie, remove listener's callback!" + e);
                    }
                }
                Log.d(TAG, "listenAudioProfie with event = " + event
                        + " sucessed, record.mBinder = " + record.mBinder 
                        + " ,clients = "  + mRecords.size());
            }
        } else {
            remove(callback.asBinder());
            Log.d(TAG, "listenAudioProfie with LISTEN_NONE, so remove this listener's callback!");
        }
    }
    
    private void remove(IBinder binder) {
        synchronized (mRecords) {
            Iterator<Record> iterator = mRecords.iterator();
            while (iterator.hasNext()) {
                Record record = (Record) iterator.next();
                if (record.mBinder.equals(binder)) {
                    iterator.remove();
                    Log.d(TAG, "removed AudioProfile change listener for: record.mBinder = "
                            + record.mBinder + ", clients = " + mRecords.size());
                }
            }
        }
    }
    
    private static class Record {

        IBinder mBinder;

        IAudioProfileListener mCallback;

        int mEvent;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // AudoProfile handler to handle persisted message
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /** Thread that handles persist values override system. */
    private class OverrideSystemThread extends Thread {
        OverrideSystemThread() {
            super("AudioService");
        }

        @Override
        public void run() {
            // Set this thread up so the handler will work on it
            Looper.prepare();

            synchronized(AudioProfileService.this) {
                mAudioProfileHandler = new AudioProfileHandler();

                // Notify that the handler has been created
                AudioProfileService.this.notify();
            }

            // Listen for volume change requests that are set by VolumePanel
            Looper.loop();
        }
    }
    
    private void createOverrideSystemThread() {
        mOverrideSystemThread = new OverrideSystemThread();
        mOverrideSystemThread.start();

        synchronized(this) {
            while (mAudioProfileHandler == null) {
                try {
                    // Wait for mAudioProfileHandler to be set by the other thread
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on AudioProfileHandler.");
                }
            }
        }
    
    }
    
    class AudioProfileHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
            String name = (String) msg.obj;
            int valueInt = msg.arg1;
            String valueSting = null;
            Bundle bundle = msg.getData();
            if (null != bundle) {
                valueSting = bundle.getString(name);
            }
            Log.d(TAG, "handleMessage what = " + msg.what + ", name = " + name + ", value = " + valueInt + " or " +  valueSting);
            switch (msg.what) {
                case MSG_PERSIST_VOICECALL_RINGTONE_TO_SYSTEM: 
                    RingtoneManager.setActualDefaultRingtoneUri(
                            mContext, AudioProfileManager.TYPE_RINGTONE, (valueSting == null ? null : Uri.parse(valueSting)));
                    break;
                    
                case MSG_PERSIST_NOTIFICATION_RINGTONE_TO_SYSTEM: 
                    RingtoneManager.setActualDefaultRingtoneUri(
                            mContext, AudioProfileManager.TYPE_NOTIFICATION, (valueSting == null ? null : Uri.parse(valueSting)));
                    break;
                    
                case MSG_PERSIST_VIDEOCALL_RINGTONE_TO_SYSTEM:
                    RingtoneManager.setActualDefaultRingtoneUri(
                            mContext, AudioProfileManager.TYPE_VIDEO_CALL, (valueSting == null ? null : Uri.parse(valueSting)));
                    break;
                    
                case MSG_PERSIST_DTMF_TONE_TO_SYSTEM: 
                    Settings.System.putInt(mContentResolver, name, msg.arg1);
                    break;
                    
                case MSG_PERSIST_SOUND_EFFECT_TO_SYSTEM:
                    Settings.System.putInt(mContentResolver, name, msg.arg1);
                    break;
                    
                case MSG_PERSIST_LOCKSCREEN_SOUND_TO_SYSTEM:
                    Settings.System.putInt(mContentResolver, name, msg.arg1);
                    break;
                    
                case MSG_PERSIST_HAPTIC_FEEDBACK_TO_SYSTEM:
                    Settings.System.putInt(mContentResolver, name, msg.arg1);
                    break;
                    
                // Persist value to database in handler to avoid ANR.
                case MSG_PERSIST_RINGER_VOLUME_TO_DATABASE:
                    Settings.System.putInt(mContentResolver, name, valueInt);
                    break;
                    
                case MSG_PERSIST_NOTIFICATION_VOLUME_TO_DATABASE:
                    Settings.System.putInt(mContentResolver, name, valueInt);
                    break;
                    
                case MSG_PERSIST_ALARM_VOLUME_TO_DATABASE:
                    Settings.System.putInt(mContentResolver, name, valueInt);
                    break;
                    
                case MSG_PERSIST_VOICECALL_RINGTONE_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_NOTIFICATION_RINGTONE_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_VIDEOCALL_RINGTONE_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_VIBRATION_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_DTMF_TONE_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_SOUND_EFFECT_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_LOCKSCREEN_SOUND_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_PERSIST_HAPTIC_FEEDBACK_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                
                case MSG_PERSIST_PROFILE_NAME_TO_DATABASE:
                    Settings.System.putString(mContentResolver, name, valueSting);
                    break;
                    
                case MSG_DELAY_SET_VIBRATE_AVOID_CTS_FAIL:
                    mDelaySetVibrate = false;
                    int vibrationStatus = getProfileState(mActiveProfileKey).mVibrationEnabled ?
                            AudioManager.VIBRATE_SETTING_ON :
                                AudioManager.VIBRATE_SETTING_ONLY_SILENT;
                   
                    mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                            vibrationStatus);
                    mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
                            vibrationStatus);
                    Log.d(TAG, "CTS test finish, set vibrate again to make function normal!");
                    break;
                    
                default:
                    Log.e(TAG, "Unsupport handle message!");
                    return;
            }
        }
    }
}
