package com.android.settings.audioprofile;


import java.util.Observable;
import java.util.Observer;

import android.app.Dialog;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.ContactsContract.ProfileSyncState;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

import android.telephony.TelephonyManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

public class Editprofile extends SettingsPreferenceFragment{
    public final static String KEY_VIBRATE = "phone_vibrate";
    public final static String KEY_VOLUME = "ring_volume";
    public final static String KEY_CATEGORY_RINGTONE = "ringtone";
    public final static String KEY_RINGTONE = "phone_ringtone";
    public final static String KEY_VIDEO_RINGTONE = "video_call_ringtone";
    public final static String KEY_CATEGORY_NOTIFICATION = "notifications";
    public final static String KEY_NOTIFY = "notifications_ringtone";
    public final static String KEY_DTMF_TONE = "audible_touch_tones";
    public final static String KEY_SOUND_EFFECTS = "audible_selection";
    public final static String KEY_LOCK_SOUNDS = "screen_lock_sounds";
    public final static String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    
    private static final String TAG = "Settings/EditProfile";
    
    private CheckBoxPreference mVibrat;
    private CheckBoxPreference mDtmfTone;
    private CheckBoxPreference mSoundEffects;
    private CheckBoxPreference mHapticFeedback;
    private CheckBoxPreference mLockSounds;
    
    private RingerVolumePreference volume;
    
    private AudioManager mAudioManager;
    private boolean silentMode;
    private AudioProfileManager mProfileManager;
    
    private ContentQueryMap mContentQueryMap;

    private Observer mSettingsObserver;
    private String   mKey;

    private int mCurOrientation;

    private boolean sIsVoiceCapable = true;
    private boolean isVoiceCapable(){
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sIsVoiceCapable = (telephony != null && telephony.isVoiceCapable());
        return sIsVoiceCapable;
    }

    private boolean sIsSmsCapable = true;
    private boolean isSmsCapable(){
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sIsSmsCapable = (telephony != null && telephony.isSmsCapable());
        return sIsSmsCapable;
    }   
    
//     If Silent Mode, remove all sound selections, include Volume, Ringtone, Notifications,
//     touch tones, sound effects, lock sounds.
//     For Volume, Ringtone and Notifications, need to set the profile's Scenario.
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        
	isSmsCapable();
        isVoiceCapable();
        
        addPreferencesFromResource(R.xml.edit_profile_prefs);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        final PreferenceActivity parentActivity = (PreferenceActivity)getActivity();
        final boolean singlePane = parentActivity.onIsHidingHeaders() || !parentActivity.onIsMultiPane();
        Bundle bundle;
        if(singlePane){
        	bundle = parentActivity.getIntent().getBundleExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
        } else {
        	bundle = this.getArguments();
        }
        Xlog.d(TAG, "onCreate activity = " + parentActivity + ",singlePane = " + singlePane + 
        		",bundle = " + bundle + ",this = " + this);
        
        String key = bundle.getString("profileKey");
	    mKey = key;
	    
        mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIOPROFILE_SERVICE);
        Scenario scenario = mProfileManager.getScenario(key);

        silentMode = scenario.equals(Scenario.SILENT);
         
         PreferenceScreen parent = getPreferenceScreen();
         
        mVibrat = (CheckBoxPreference) findPreference(KEY_VIBRATE);
        if(mVibrat != null){
            mVibrat.setPersistent(false);
            mVibrat.setChecked(mProfileManager.getVibrationEnabled(key));
        }
        mDtmfTone = (CheckBoxPreference) findPreference(KEY_DTMF_TONE);
        if(mDtmfTone != null){
            mDtmfTone.setPersistent(false);
            mDtmfTone.setChecked(mProfileManager.getDtmfToneEnabled(key));
            if(silentMode){
                parent.removePreference(mDtmfTone);
            }
        }
        mSoundEffects = (CheckBoxPreference) findPreference(KEY_SOUND_EFFECTS);
        if(mSoundEffects != null){
            mSoundEffects.setPersistent(false);
            mSoundEffects.setChecked(mProfileManager.getSoundEffectEnabled(key));
            if(silentMode){
                parent.removePreference(mSoundEffects);
            }
        }
        mLockSounds = (CheckBoxPreference) findPreference(KEY_LOCK_SOUNDS);
        if(mLockSounds != null){
            mLockSounds.setPersistent(false);
            mLockSounds.setChecked(mProfileManager.getLockScreenEnabled(key));
            if(silentMode){
                parent.removePreference(mLockSounds);
            }
        }
        mHapticFeedback = (CheckBoxPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        if(mHapticFeedback != null){
            mHapticFeedback.setPersistent(false);
            mHapticFeedback.setChecked(mProfileManager.getHapticFeedbackEnabled(key));
        }

        volume = (RingerVolumePreference) findPreference(KEY_VOLUME);
        if(volume != null){
            volume.setProfile(key);
            if(silentMode){
                parent.removePreference(volume);
            }
        }

        PreferenceGroup parentRingtone = (PreferenceGroup) findPreference(KEY_CATEGORY_RINGTONE);
        PreferenceGroup parentNotify = (PreferenceGroup) findPreference(KEY_CATEGORY_NOTIFICATION);
        if(silentMode){
            // Silent Mode remove all phone ringtone, notification ringtone
            parent.removePreference(parentRingtone);
            parent.removePreference(parentNotify);
            return;
        }

        DefaultRingtonePreference notify = (DefaultRingtonePreference) parentNotify.findPreference(KEY_NOTIFY);
        if(notify != null){
            notify.setStreamType(DefaultRingtonePreference.NOTIFICATION_TYPE);
            notify.setProfile(key);
            notify.setRingtoneType(AudioProfileManager.TYPE_NOTIFICATION);
        }

    	if(sIsVoiceCapable){
	        DefaultRingtonePreference ringtone = (DefaultRingtonePreference) parentRingtone.findPreference(KEY_RINGTONE);
	        DefaultRingtonePreference videoRingtone = (DefaultRingtonePreference) parentRingtone.findPreference(KEY_VIDEO_RINGTONE);

                if(!FeatureOption.MTK_VT3G324M_SUPPORT) {
                    parentRingtone.removePreference(videoRingtone);
                    ringtone.setTitle(R.string.ringtone_title);
                    ringtone.setSummary(R.string.ringtone_summary);
                }

                if(ringtone != null){
                    ringtone.setStreamType(DefaultRingtonePreference.RING_TYPE);
            	    ringtone.setProfile(key);
                    ringtone.setRingtoneType(AudioProfileManager.TYPE_RINGTONE);
                }

                if(videoRingtone != null){
                    videoRingtone.setStreamType(DefaultRingtonePreference.RING_TYPE);
                    videoRingtone.setProfile(key);
                    videoRingtone.setRingtoneType(AudioProfileManager.TYPE_VIDEO_CALL);
                }
	    }
            else if(sIsSmsCapable){
                parent.removePreference(mDtmfTone);
                parent.removePreference(parentRingtone);
	    }
            else{
                parent.removePreference(mVibrat);
                parent.removePreference(mDtmfTone);
                parent.removePreference(parentRingtone);
    	    }	    
    	
    	mCurOrientation = this.getResources().getConfiguration().orientation;
    }

    @Override
    public void onStart() {
        
        super.onStart();
        // listen for vibrate_in_silent settings changes
        Cursor settingsCursor = getContentResolver().query(Settings.System.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{mProfileManager.getVibrationKey(mKey)},
                null);
        mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
    }

    
    @Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        Log.d(TAG, "onPause");
        if(volume != null) {
        	Log.d(TAG, "pref is not null");
        	volume.StopPlaying();
        	volume.RevertVolume();
        }
	}

    @Override
    public void onStop() {
        super.onStop();
        if (mSettingsObserver != null) {
            mContentQueryMap.deleteObserver(mSettingsObserver);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        final String name = mProfileManager.getVibrationKey(mKey);

        if(silentMode) {
        	if (mSettingsObserver == null) {
                mSettingsObserver = new Observer() {
                    public void update(Observable o, Object arg) {
                    	Log.d(TAG, "update");
                    	if(mVibrat != null) {
                    		String VibrateEnabled = Settings.System.getString(getContentResolver(),
                                    name);
                    		if(VibrateEnabled != null){
                            	mVibrat.setChecked(VibrateEnabled.equals("true"));
                               	Log.d(TAG, "vibrate setting is " + VibrateEnabled.equals("true"));
                    		}
 
                    	}
                    }
                };
                mContentQueryMap.addObserver(mSettingsObserver);
            }
        }        
    }
    //If Silent profile is active, change RINGER_MODE once vibrate checked or unchecked.
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference ){
        if(preference == mVibrat){
            boolean isVibrate = mVibrat.isChecked();
        	Log.d(TAG, "set vibrate" + isVibrate);
            mProfileManager.setVibrationEnabled(mKey, isVibrate);
        }else if(preference == mDtmfTone){
            mProfileManager.setDtmfToneEnabled(mKey, mDtmfTone.isChecked());
        }else if(preference == mSoundEffects){
            mProfileManager.setSoundEffectEnabled(mKey, mSoundEffects.isChecked());
        }else if(preference == mLockSounds){
            mProfileManager.setLockScreenEnabled(mKey, mLockSounds.isChecked());
        }else if(preference == mHapticFeedback){
            mProfileManager.setHapticFeedbackEnabled(mKey, mHapticFeedback.isChecked());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged: newConfig = " + newConfig
                + ",mCurOrientation = " + mCurOrientation + ",this = " + this);        
        super.onConfigurationChanged(newConfig);
        if (newConfig != null && newConfig.orientation != mCurOrientation) {
            mCurOrientation = newConfig.orientation;
        }
        this.getListView().clearScrapViewsIfNeeded();
    }
    
}
