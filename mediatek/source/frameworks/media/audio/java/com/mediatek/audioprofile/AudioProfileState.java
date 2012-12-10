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

import android.net.Uri;

public class AudioProfileState {
    /** Profile key.*/
    public String mProfileKey;
    
    /** Voice call ringtone uri.*/
    public Uri mRingerStream;
    
    /** Video call ringtone uri. */
    public Uri mVideoCallStream;
    
    /** Notification ringtone uri.*/
    public Uri mNotificationStream;
    
    /** Voice call ringtone volume.*/
    public int mRingerVolume;
    
    /** Alarm volume.*/
    public int mAlarmVolume;
    
    /** Notification volume.*/
    public int mNotificationVolume;
    
    /** Whether the phone should vibrate for incoming calls.*/
    public boolean mVibrationEnabled;
    
    /** Whether sound should be played when making screen selection.*/
    public boolean mSoundEffectEnbled;
    
    /** Whether sound should be played when using dial pad.*/
    public boolean mDtmfToneEnabled;
    
    /** Whether the phone should vibrate when pressing soft keys and on
     * certain UI interactions.*/
    public boolean mHapticFeedbackEnabled;
    
    /** Whether to play sounds when the keyguard is shown and dismissed.*/
    public boolean mLockScreenSoundEnabled;
    
    /** Whether the notification use the volume of ring.*/
    public boolean mNoficationUseRingVolume;
    
    /**
     * Creates a new AudioProfileState instance with given values.
     * 
     * @param uri The {@link Uri} array saved ringer, notification and videocall uri.<br/>
     * {@link mRingerStream}      = uri[0]<br/>
     * {@link mNotificationStream}= uri[1]<br/>
     * {@link mVideoCallStream}   = uri[2]<p/>
     * 
     * @param volume The volume array saved ringer, notification and alarm volume.<br/>
     * {@link mRingerVolume}      = volume[0]<br/>
     * {@link mNotificationVolume}= volume[1]<br/>
     * {@link mAlarmVolume}       = volume[2]<p/>
     * 
     * @param enalbed Whether vibration, dtmftone, sound effect, lockscreen sound and haptic feedback enabled<br/>
     * {@link mVibrationEnabled}        = enalbed[0]<br/>
     * {@link mDtmfToneEnabled}         = enalbed[1]<br/>
     * {@link mSoundEffectEnbled}       = enalbed[2]<br/>
     * {@link mLockScreenSoundEnabled}  = enalbed[3]<br/>
     * {@link mHapticFeedbackEnabled}   = enalbed[4]
     */
    public AudioProfileState(Uri[] uri, int[] volume, boolean[] enalbed) {
        
        mRingerStream = uri[0];
        mNotificationStream = uri[1];
        mVideoCallStream = uri[2];
        
        mRingerVolume = volume[0];
        mNotificationVolume = volume[0];
        mAlarmVolume = volume[0];
        
        mVibrationEnabled = enalbed[0];
        mDtmfToneEnabled = enalbed[1];
        mSoundEffectEnbled = enalbed[2];
        mLockScreenSoundEnabled = enalbed[3];
        mHapticFeedbackEnabled = enalbed[4];
        
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("volume_ringtone = ").append(this.mRingerVolume).append(",");
        buffer.append("volume_notification = ").append(this.mNotificationVolume).append(",");
        buffer.append("volume_alarm = ").append(this.mAlarmVolume).append(",");
        
        buffer.append("vibrate_on = ").append(this.mVibrationEnabled).append(",");
        buffer.append("dtmf_tone = ").append(this.mDtmfToneEnabled).append(",");
        buffer.append("sound_effects = ").append(this.mSoundEffectEnbled).append(",");
        buffer.append("lockscreen_sounds = ").append(this.mLockScreenSoundEnabled).append(",");
        buffer.append("haptic_feedback = ").append(this.mHapticFeedbackEnabled).append(",");
        
        buffer.append("ringtone = ").append(this.mRingerStream).append(",");
        buffer.append("notification_sound = ").append(this.mNotificationStream).append(",");
        buffer.append("video_call = ").append(this.mVideoCallStream);
        return buffer.toString();
    }
}
