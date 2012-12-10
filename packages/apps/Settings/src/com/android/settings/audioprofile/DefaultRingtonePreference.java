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

package com.android.settings.audioprofile;

import com.android.settings.Utils;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.xlog.Xlog;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;

public class DefaultRingtonePreference extends RingtonePreference {
	private static final String TAG = "Settings/Rt_Pref";
    
	public static final String RING_TYPE = "RING";
	public static final String NOTIFICATION_TYPE = "NOTIFICATION";
	
    // add for CMCC project
    private static final String OP = android.os.SystemProperties.get("ro.operator.optr");
    private static final boolean IS_CMCC = Utils.isCmccLoad();
    
    private AudioProfileManager mProfileManager = null;
    private String mKey;
    private Context mContext;
    private String mStreamType;
    public DefaultRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mProfileManager = (AudioProfileManager)mContext.getSystemService(Context.AUDIOPROFILE_SERVICE);
    }

    public void setProfile(String key){
    	mKey = key;
    }
    
    public void setStreamType(String streamType) {
    	mStreamType = streamType;
    }
    
    @Override
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        /*
         * Since this preference is for choosing the default ringtone, it
         * doesn't make sense to show a 'Default' item.
         */
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        
        if(mStreamType.equals(RING_TYPE)) {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        }
        
        if(IS_CMCC) {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_MORE_RINGTONES, true);
        } else {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_MORE_RINGTONES, false);
        }
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        mProfileManager.setRingtoneUri(mKey, getRingtoneType(), ringtoneUri);
    }

    @Override
    protected Uri onRestoreRingtone() {
    	int type = getRingtoneType();
    	Xlog.d(TAG, "onRestoreRingtone: type = " + type);
    	
        Uri uri = mProfileManager.getRingtoneUri(mKey, type);
        Xlog.d(TAG, "onRestoreRingtone: uri = " + (uri == null ? "null" : uri.toString()));
        
        return uri;
    }
}
