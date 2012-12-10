package com.mediatek.audioprofile;

import android.net.Uri;
import com.mediatek.audioprofile.IAudioProfileListener;

/**
 * {@hide}
 */
interface IAudioProfileService {

    void setActiveProfile(String profileKey);
    String addProfile();
    boolean deleteProfile(String profileKey);
    void reset();
    int getProfileCount();
    List<String> getAllProfileKeys();
    List<String> getPredefinedProfileKeys();
    List<String> getCustomizedProfileKeys();
    boolean isNameExist(String name);
    String getActiveProfileKey();
    String getLastActiveProfileKey();
    
    Uri getRingtoneUri(String profileKey, int type);
    int getStreamVolume(String profileKey, int streamType);
    boolean getVibrationEnabled(String profileKey);
    boolean getDtmfToneEnabled(String profileKey);
    boolean getSoundEffectEnabled(String profileKey);
    boolean getLockScreenEnabled(String profileKey);
    boolean getHapticFeedbackEnabled(String profileKey);
    List<String> getProfileStateString(String profileKey);
    String getProfileName(String profileKey);
    
    void setRingtoneUri(String profileKey, int type, in Uri ringtoneUri);
    void setStreamVolume(String profileKey, int streamType, int index);
    void setVibrationEnabled(String profileKey, boolean enabled);
    void setDtmfToneEnabled(String profileKey, boolean enabled);
    void setSoundEffectEnabled(String profileKey, boolean enabled);
    void setLockScreenEnabled(String profileKey, boolean enabled);
    void setHapticFeedbackEnabled(String profileKey, boolean enabled);
    void setProfileName(String profileKey, String newName);
    
    boolean isActive(String profileKey);
    boolean isRingtoneExist(in Uri uri);
    int getStreamMaxVolume(int streamType);
    Uri getDefaultRingtone(int type);
    
    oneway void listenAudioProfie(IAudioProfileListener callback, int event);
}