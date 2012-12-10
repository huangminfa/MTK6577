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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mediatek.audioprofile.IAudioProfileListener;

/**
 * A listener class for AudioProfile changes in specific profile states
 * on the device, including audioprofile type, ringer mode and volume.
 * <p>
 * Override the methods for the state that you wish to receive updates for, and
 * pass your AudioProfileListener object, along with bitwise-or of the LISTEN_
 * flags to {@link AudioProfileListener#listen TelephonyManager.listen()}.
 * <p>
 * 
 * @hide
 */
public class AudioProfileListener {

    /**
     * Stop listening for updates.
     * @hide
     */
    public static final int LISTEN_NONE = 0;

    /**
     * Listen for changes to the audioprofile .
     *
     * @see #onAudioProfileChanged
     * @hide
     */
    public static final int LISTEN_AUDIOPROFILE_CHANGEG                     = 0x00000001;

    /**
     * Listen for changes to the ringermode .
     *
     * @see #onRingerModeChanged
     * @hide
     */
    public static final int LISTEN_RINGERMODE_CHANGED                       = 0x00000002;

    /**
     * Listen for changes to the ringer volume .
     *
     * @see #onVolumeChanged
     * @hide
     */
    public static final int LISTEN_RINGER_VOLUME_CHANGED                   = 0x00000004;

    
    public AudioProfileListener() {
    }

    /**
     * Callback invoked when AudioProfile type changes.
     * 
     * @param profileKey The key of the profile that need to update.
     * 
     * @see com.mediatek.audioprofile.AudioProfileManager.Scenario
     * @see com.mediatek.audioprofile.AudioProfileManager#getProfileKey(Scenario scenario)
     * @hide
     */
    public void onAudioProfileChanged(String profileKey) {
        // default implementation empty
    }

    /**
     * Callback invoked when RingerMode changes.
     *
     */
    public void onRingerModeChanged(int ringerMode) {
        // default implementation empty
    }

    /**
     * Callback invoked when system volume changes.
     */
    public void onRingerVolumeChanged(int oldVolume, int newVolume, String extra) {
        // default implementation empty
    }

    /**
     * The callback methods need to be called on the handler thread where
     * this object was created.  If the binder did that for us it'd be nice.
     */
    IAudioProfileListener callback = new IAudioProfileListener.Stub() {
        public void onAudioProfileChanged(String profileKey) {
            Message.obtain(mHandler, LISTEN_AUDIOPROFILE_CHANGEG, 0, 0, profileKey).sendToTarget();
        }

        public void onRingerModeChanged(int ringerMode) {
            Message.obtain(mHandler, LISTEN_RINGERMODE_CHANGED, ringerMode, 0, null).sendToTarget();
        }

        public void onRingerVolumeChanged(int oldVolume, int newVolume, String extra) {
            Message.obtain(mHandler, LISTEN_RINGER_VOLUME_CHANGED, oldVolume, newVolume, extra).sendToTarget();
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LISTEN_AUDIOPROFILE_CHANGEG:
                    AudioProfileListener.this.onAudioProfileChanged((String) msg.obj);
                    break;
                case LISTEN_RINGERMODE_CHANGED:
                    AudioProfileListener.this.onRingerModeChanged(msg.arg1);
                    break;
                case LISTEN_RINGER_VOLUME_CHANGED:
                    AudioProfileListener.this.onRingerVolumeChanged(msg.arg1, msg.arg2, (String) msg.obj);
                    break;
            }
        }
    };

    
    /**
     * Get calback method.
     * @hide
     */
    public IAudioProfileListener getCallback() {
        return callback;
    }
}
