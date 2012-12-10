/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.settings.R;
import com.android.settings.Utils;

import android.app.Service;

import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.audioprofile.AudioProfileState;
import com.mediatek.audioprofile.AudioProfileListener;
import com.mediatek.xlog.Xlog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.SeekBarDialogPreference;
import android.preference.Preference.BaseSavedState;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Special preference type that allows configuration of both the ring volume and
 * notification volume.
 */
public class RingerVolumePreference extends SeekBarDialogPreference implements
        View.OnKeyListener { 
    private static final String TAG = "Settings/VolPref";
	private static final boolean LOGV = true;
	
    private static final String OP = android.os.SystemProperties.get("ro.operator.optr");
	private static final boolean IS_CMCC = Utils.isCmccLoad();
	
    private String mKey;
    private SeekBarVolumizer [] mSeekBarVolumizer;
    private VolumeReceiver mReceiver;
    private AudioManager mAudioManager;
	private AudioProfileManager profileManager;
    
	private boolean isDlgDismissed = true;
    private static final int[] SEEKBAR_ID = new int[] {
        R.id.notification_volume_seekbar,
        R.id.ringer_volume_seekbar,
        R.id.alarm_volume_seekbar
    };
    //set for volume
    private static final int[] SEEKBAR_TYPE = new int[] {
    	AudioProfileManager.STREAM_NOTIFICATION,
    	AudioProfileManager.STREAM_RING,
    	AudioProfileManager.STREAM_ALARM
    };
    //set for ringtone
    private static final int[] STREAM_TYPE = new int[] {
    	AudioProfileManager.TYPE_NOTIFICATION,
    	AudioProfileManager.TYPE_RINGTONE,
    	RingtoneManager.TYPE_ALARM
    };
    
    private static final int[] CHECKBOX_VIEW_ID = new int[] {
        R.id.ringer_mute_button,
        R.id.notification_mute_button,
        R.id.alarm_mute_button
    };
    
    private static final int[] SEEKBAR_UNMUTED_RES_ID = new int[] {
        com.android.internal.R.drawable.ic_audio_ring_notif,
        com.android.internal.R.drawable.ic_audio_notification,
        com.android.internal.R.drawable.ic_audio_alarm
    };
    
	private ImageView[] mImageViews;
    //private SeekBarVolumizer mNotificationSeekBarVolumizer;
    private TextView mNotificationVolumeTitle;
    
    public void setProfile(String key){
    	mKey = key;
    }
    public RingerVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_ringervolume);
        setDialogIcon(R.drawable.ic_settings_sound);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        profileManager = (AudioProfileManager)context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        mSeekBarVolumizer = new SeekBarVolumizer[SEEKBAR_ID.length];
        mImageViews = new ImageView[SEEKBAR_UNMUTED_RES_ID.length];
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Context context = getContext();
        mReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
        
        context.registerReceiver(mReceiver, filter);
        isDlgDismissed = false;
        Log.d(TAG, "set isDlgDismissed to false ");
        LinearLayout layout = (LinearLayout)view.findViewById(R.id.volume);
        ImageView ic = new ImageView(context){
            @Override
            public boolean isFocused(){
                return true;
            }
        };
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,0);
        if(layout!=null){
            layout.addView(ic,params);
        }
        
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
        	ImageView imageview = (ImageView) view.findViewById(CHECKBOX_VIEW_ID[i]);
        	if(imageview != null){
        		imageview.setImageResource(SEEKBAR_UNMUTED_RES_ID[i]);
        	}
        	
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            if(seekBar!=null){
                if(i==0){
                    seekBar.requestFocus();
                }
                mSeekBarVolumizer[i] = new SeekBarVolumizer(context, seekBar,
                    SEEKBAR_TYPE[i]);
                seekBar.setOnKeyListener(this);
            }
        }

        //mNotificationVolumeTitle = (TextView) view.findViewById(R.id.notification_volume_title);        
        view.setFocusableInTouchMode(true);
        
        // Disable either ringer+notifications or notifications
        int id;
        if (!Utils.isVoiceCapable(getContext())) {
            id = R.id.ringer_section;
            mSeekBarVolumizer[1].setVisible(false);
        } else {
            id = R.id.notification_section;
            mSeekBarVolumizer[0].setVisible(false);
        }
        View hideSection = view.findViewById(id);
        
        hideSection.setVisibility(View.GONE);
        
        profileManager.listenAudioProfie(mListener.getCallback(), AudioProfileListener.LISTEN_RINGER_VOLUME_CHANGED);
    }

	public void StopPlaying() {
    	
    	if (mSeekBarVolumizer != null){
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {           	
            	if(vol != null && vol.isPlaying()){
            		Log.d(TAG, "IsPlaying");
                    vol.stopSample();
                    Log.d(TAG, "StopPlaying");
            	}
            }
        }    	
    }
    
	public void RevertVolume() {
        Log.d(TAG, "isDlgDismissed" + isDlgDismissed);
		if(isDlgDismissed) {
    		return;
    	}
    	if (mSeekBarVolumizer != null){
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {           	
            	if(vol != null){
                    vol.revertVolume();
                    vol.resume();
            	}
            }
        }  
	}
	
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // If key arrives immediately after the activity has been cleaned up.
        if (mSeekBarVolumizer == null) return true;

        boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        for (SeekBarVolumizer vol:mSeekBarVolumizer){
        	if(vol != null && vol.getSeekBar() != null && vol.getSeekBar().isFocused()){
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        if (isdown) {
                            vol.changeVolumeBy(-1);
                        }
                        return true;
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        if (isdown) {
                            vol.changeVolumeBy(1);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        }
        return true;
    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
    	if(volumizer == null) return;
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) vol.stopSample();
        }
    }
    
     @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(mSeekBarVolumizer == null) return;
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            vol.stopSample();
        }
        if (!positiveResult) {
            Xlog.d(TAG, "Cacel: Original checked.");
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {
               if (vol != null && vol.getVisible()) {
                    vol.revertVolume();
                    vol.getSeekBar().setOnKeyListener(null);
                    vol.stop();
                    vol = null;
                }
            }
        }else{
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {
                if (vol != null && vol.getVisible()) {
                    vol.saveVolume();
                    vol.getSeekBar().setOnKeyListener(null);
                    vol.stop();
                    vol = null;
                }
            }
        }
        isDlgDismissed = true;
        Log.d(TAG, "set isDlgDismissed to true");
        getContext().unregisterReceiver(mReceiver);
        profileManager.listenAudioProfie(mListener.getCallback(), AudioProfileListener.LISTEN_NONE);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        if(mSeekBarVolumizer != null){
            VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                SeekBarVolumizer vol = mSeekBarVolumizer[i];
                if (vol != null) {
                    vol.onSaveInstanceState(volumeStore[i]);
                }
            }
        }
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if(mSeekBarVolumizer != null){
            VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                SeekBarVolumizer vol = mSeekBarVolumizer[i];
                if (vol != null) {
                    vol.onRestoreInstanceState(volumeStore[i]);
                }
            }
        }
    }
    public static class VolumeStore {
        public int volume = -1;
        public int originalVolume = -1;
        public int systemVolume = -1;
    }
    
    private static class SavedState extends BaseSavedState {
        VolumeStore [] mVolumeStore;

        public SavedState(Parcel source) {
            super(source);
            mVolumeStore = new VolumeStore[SEEKBAR_ID.length];
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                mVolumeStore[i] = new VolumeStore();
                mVolumeStore[i].volume = source.readInt();
                mVolumeStore[i].originalVolume = source.readInt();
                mVolumeStore[i].systemVolume = source.readInt();
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                dest.writeInt(mVolumeStore[i].volume);
                dest.writeInt(mVolumeStore[i].originalVolume);
                dest.writeInt(mVolumeStore[i].systemVolume);
            }
        }

        VolumeStore[] getVolumeStore(int count) {
            if (mVolumeStore == null || mVolumeStore.length != count) {
                mVolumeStore = new VolumeStore[count];
                for (int i = 0; i < count; i++) {
                    mVolumeStore[i] = new VolumeStore();
                }
            }
            return mVolumeStore;
        }

        public SavedState(Parcelable superState) {
            super(superState);
            
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

        private Context mContext;
        private Handler mHandler = new Handler();

        private int mStreamType;
        public  Ringtone mRingtone;
        public int systemVolume = -1;
        private int originalVolume = -1;
        private int mLastProgress = -1;
        private SeekBar mSeekBar;
        private Uri defaultUri = null;
        private boolean mAdjustToZero = false;
        
        public boolean IsActive = false;
        private boolean IsSilentProfileActive = false;
        
        private boolean IsVisible = true;

        
        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
            mContext = context;

            mStreamType = streamType;
            mSeekBar = seekBar;
            
            initSeekBar(seekBar);
        }

        private void initSeekBar(SeekBar seekBar) {

        	seekBar.setMax(profileManager.getStreamMaxVolume(mStreamType));
            
            systemVolume = mAudioManager.getStreamVolume(mStreamType);
            Xlog.d(TAG, ""+mStreamType+" get Original SYSTEM Volume: " + systemVolume);
            
            originalVolume = profileManager.getStreamVolume(mKey, mStreamType);
            Xlog.d(TAG, ""+mStreamType+" get Original Volume: " + originalVolume);            
            
            IsActive = profileManager.isActive(mKey);
            //if the volume is changed to 1 for ringer mode changed and we can't receive the 
            //broadcast to adjust the volume, sync the profile volume with the system
            if(IsActive) {
            	if(systemVolume != originalVolume) {
            		Xlog.d(TAG, " sync "+mStreamType+" original Volume to" + systemVolume);
            		originalVolume = systemVolume;
            	}
            }
          
            mLastProgress = originalVolume;
            seekBar.setProgress(mLastProgress);
            seekBar.setOnSeekBarChangeListener(this);

            if(mStreamType == AudioProfileManager.STREAM_RING){
                defaultUri = profileManager.getRingtoneUri(mKey, AudioProfileManager.TYPE_RINGTONE);
            }else if (mStreamType == AudioProfileManager.STREAM_NOTIFICATION){
                defaultUri = profileManager.getRingtoneUri(mKey, AudioProfileManager.TYPE_NOTIFICATION);
            }else if  (mStreamType == AudioProfileManager.STREAM_ALARM){
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }
            
            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);
            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
        }
        
        public void setVisible(boolean visible) {
        	IsVisible = visible;
        }
        
        public boolean getVisible() {
        	return IsVisible;
        }
        
        public void stop() {
            mSeekBar.setOnSeekBarChangeListener(null);
            mContext = null;
            mHandler = null;
        }

        public boolean isPlaying() {
        	
        	if (mRingtone != null) {
        		return mRingtone.isPlaying();
        	}
        	
        	return false;
        }
        
        public void resume() {
            
            systemVolume = mAudioManager.getStreamVolume(mStreamType);
            Xlog.d(TAG, ""+mStreamType+" get Original SYSTEM Volume: " + systemVolume);
            
            originalVolume = profileManager.getStreamVolume(mKey, mStreamType);
            Xlog.d(TAG, ""+mStreamType+" get Original Volume: " + originalVolume);            
            
            IsActive = profileManager.isActive(mKey);
            //if the volume is changed to 1 for ringer mode changed and we can't receive the 
            //broadcast to adjust the volume, sync the profile volume with the system
            if(IsActive) {
            	if(systemVolume != originalVolume) {
            		Xlog.d(TAG, " sync "+mStreamType+" original Volume to" + systemVolume);
            		originalVolume = systemVolume;
            	}
            }
          
            mLastProgress = originalVolume;
            if(mSeekBar != null){
                mSeekBar.setProgress(mLastProgress);
            }
        }
        
        public void revertVolume() {
        	 Xlog.d(TAG, ""+mStreamType+" revert Last Volume "+ originalVolume);
        	 
        	 //if(profileManager.isActive(mKey)) {
                 profileManager.setStreamVolume(mKey, mStreamType, originalVolume);
                 if(mStreamType == AudioProfileManager.STREAM_RING) {
                	 profileManager.setStreamVolume(mKey, AudioProfileManager.STREAM_NOTIFICATION, originalVolume);
                 }
        	 //}
                 
             if(profileManager.isActive(mKey)) {
              	 Xlog.d(TAG, ""+mStreamType+" Active, Revert system Volume "+ originalVolume);
                  setVolume(mStreamType, originalVolume,false);     
             } else {
                 if(!isSilentProfileActive()){
                	 Xlog.d(TAG, ""+mStreamType+" not Active, Revert system Volume "+systemVolume);
                	 setVolume(mStreamType, systemVolume, false);
                 }
             }
        }

        public void saveVolume(){
            Xlog.d(TAG, ""+mStreamType+" Save Last Volume "+mLastProgress);
            
            profileManager.setStreamVolume(mKey, mStreamType, mLastProgress);
            if(mStreamType == AudioProfileManager.STREAM_RING) {
           	    profileManager.setStreamVolume(mKey, AudioProfileManager.STREAM_NOTIFICATION, mLastProgress);
            }
            
            if(!profileManager.isActive(mKey)){            	
                if(!isSilentProfileActive()){
                	Xlog.d(TAG, ""+mStreamType+" not Active, Revert system Volume "+systemVolume);
                    setVolume(mStreamType, systemVolume, false);
                }

            } else {
                Xlog.d(TAG, ""+mStreamType+" Active, save system Volume "+ mLastProgress);
                setVolume(mStreamType, mLastProgress, false);
            }

        }
        
        private void setVolume(int StreamType, int volume, boolean flag) {
            if(StreamType == AudioProfileManager.STREAM_RING){
            	
                // MTK_OP01_PROTECT_START--->
                if(IS_CMCC || flag) {
                	mAudioManager.setAudioProfileStreamVolume(mStreamType, volume, 0);
                    mAudioManager.setAudioProfileStreamVolume(AudioProfileManager.STREAM_NOTIFICATION, volume, 0);
                } else 
                // <---MTK_OP01_PROTECT_END
                {
                	mAudioManager.setStreamVolume(mStreamType, volume, 0);
                    mAudioManager.setStreamVolume(AudioProfileManager.STREAM_NOTIFICATION, volume, 0);
                }
                                
            } else {
            	// MTK_OP01_PROTECT_START--->
            	if(IS_CMCC || flag) {
                	mAudioManager.setAudioProfileStreamVolume(StreamType, volume, 0);
                } else 
                // <---MTK_OP01_PROTECT_END
                {
                	mAudioManager.setStreamVolume(StreamType, volume, 0);
                }
            }
        }
        
        private boolean isSilentProfileActive() {
        	return mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
        }
        
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromTouch) {
        	Xlog.d(TAG, "onProgressChanged" + ": progress" + progress + " : fromTouch" + fromTouch);
        	mLastProgress = progress;
            if (!fromTouch) {
                return;
            }
            postSetVolume(progress);
        }

        void postSetVolume(int progress) {
            // Do the volume changing separately to give responsive UI
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }
    
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mRingtone!=null && !mRingtone.isPlaying()) {
                sample();
            }
        }

        public void run() {
        	sample();
        }

        private void sample() {
            onSampleStarting(this);
        	
            Xlog.d(TAG, "sample, set system Volume "+ mLastProgress);
            if(!isSilentProfileActive()) {
                setVolume(mStreamType, mLastProgress, true);
            }
        	
        	if(mRingtone != null){
            	Xlog.d(TAG,"stream type "+mStreamType + " play sample");
                mRingtone.play();
            }
        }

        public void stopSample() {
            if (mRingtone != null) {
            	Xlog.d(TAG,"stream type "+mStreamType + " stop sample");
                mRingtone.stop();
            }
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }
        
        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            postSetVolume(mSeekBar.getProgress());
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = originalVolume;
                volumeStore.systemVolume = systemVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mLastProgress = volumeStore.volume;
                originalVolume = volumeStore.originalVolume;
                systemVolume = volumeStore.systemVolume;
                postSetVolume(mLastProgress);
            }
        }
    }

    private class VolumeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {
                int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                if(streamType != AudioManager.STREAM_RING) return;
                if (mSeekBarVolumizer[1] != null) {
                    SeekBar seekBar = mSeekBarVolumizer[1].getSeekBar();
                    if (seekBar == null) return;
                    int volume = mAudioManager.getStreamVolume(streamType);
                    Xlog.d(TAG,"AudioManager Volume " + volume);
                    Xlog.d(TAG,"seekbar progress " + seekBar.getProgress());
                    if(seekBar.getProgress()!=volume){
                        if(volume >= 0) {
                            mSeekBarVolumizer[1].systemVolume = volume;
                            Xlog.d(TAG,"is SystemVolume Changed "+volume);
                        }
                    }   
                }
             }
        }
    }
    
    
    private AudioProfileListener mListener = new AudioProfileListener(){
        @Override
        public void onRingerVolumeChanged(int oldVolume, int newVolume, String extra) {
            Xlog.d(TAG, extra + " :onRingerVolumeChanged from " + oldVolume + " to " + newVolume);
            if (mKey.equals(extra) && mSeekBarVolumizer[1] != null) {
                SeekBar seekBar = mSeekBarVolumizer[1].getSeekBar();
                if (seekBar == null) return;
                if(seekBar.getProgress() != newVolume && newVolume >= 0){
                    seekBar.setProgress(newVolume);
                    Xlog.d(TAG, "Profile Ringer volume change: mSeekBar.setProgress++ "+ newVolume);
                }
            }   
        }
    };
       
}
