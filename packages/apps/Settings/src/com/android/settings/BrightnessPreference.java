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

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class BrightnessPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {
	private final String TAG = "BrightnessPreference";
    private SeekBar mSeekBar;
    private CheckBox mCheckBox;

    private int mOldBrightness;
    private int mOldAutomatic;

    private boolean mAutomaticAvailable;

    private boolean mRestoredOldState;
    private boolean mModeChangeOut=false;//brightness mode state modify by other app
    private boolean mModeChangeSelf=false;//brightness mode state modify by self 
    private boolean mFirstLaunch=false;
    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private int mScreenBrightnessDim =
	    getContext().getResources().getInteger(com.android.internal.R.integer.config_screenBrightnessDim);
    private static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessChanged();
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };

    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAutomaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);

        setDialogLayoutResource(R.layout.preference_dialog_brightness);
        setDialogIcon(R.drawable.ic_settings_display);
    }

    @Override
    protected void showDialog(Bundle state) {
    	if (mAutomaticAvailable && getBrightnessMode(0)!=0) {
      		 mFirstLaunch=true;
      	 Log.i(TAG, "onBindDialogView---mFirstLaunch="+mFirstLaunch);
      	 }
        super.showDialog(state);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);
        //mRestoredOldState = false;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
        mOldBrightness = getBrightness(0);
        mSeekBar.setProgress(mOldBrightness - mScreenBrightnessDim);

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        if (mAutomaticAvailable) {
            mCheckBox.setOnCheckedChangeListener(this);
            mOldAutomatic = getBrightnessMode(0);
            mCheckBox.setChecked(mOldAutomatic != 0);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setBrightness(progress + mScreenBrightnessDim);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	setModeChangeState();
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (!isChecked) {
            setBrightness(mSeekBar.getProgress() + mScreenBrightnessDim);
        }
    }
    /*if onCheckedChanged() is called first set mModeChangeSelf =true
     * when getBrightnessMode() is called there is no need to update mOldAutomatic since
     * need to restore to old state if is interrupted by other process
    */
    private void setModeChangeState() {
    	if (true == mFirstLaunch) {
    		// when first time launch the dialog with checkbox is checked no need to set mModeChangeSelf=true as it
    		// won't call getBrightnessMode() so jump over this when launch first time
    		mFirstLaunch = false;
    	}
    	else {//if brightness mode change by self set mModeChangeSelf as true so when call onBrightnessModeChanged() it no need to change old state
    		if (false == mModeChangeOut) {
        		mModeChangeSelf = true;
        	}
    	}
    	//Reset if brightness mode changed by other app
    	if (true == mModeChangeOut && false == mModeChangeSelf) {
    		mModeChangeOut=false;
    	}
	}

    private int getBrightness(int defaultValue) {
        int brightness = defaultValue;
        try {
            brightness = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException snfe) {
        }
        return brightness;
    }

    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        }
        return brightnessMode;
    }

    private void onBrightnessChanged() {
    	Log.i(TAG, "onBrightnessChanged");
        int brightness = getBrightness(MAXIMUM_BACKLIGHT);
        mSeekBar.setProgress(brightness - mScreenBrightnessDim);
        mOldBrightness = brightness;
    }

    private void onBrightnessModeChanged() {
    	Log.i(TAG, "onBrightnessModeChanged");
        boolean checked = getBrightnessMode(0) != 0;
        updateOldAutomaticMode();
        mCheckBox.setChecked(checked);
    }

    private void updateOldAutomaticMode() {
    	Log.i(TAG, "updateOldAutomaticMode");
    	if (false == mModeChangeSelf){
        	mModeChangeOut=true;
        }
        if (mModeChangeOut == true && mModeChangeSelf ==false) {//only if brightness mode changed by other app, need to change mOldAutomatic 
        	mOldAutomatic = getBrightnessMode(0);
        	Log.i(TAG, "updateOldAutomaticMode+mOldAutomatic="+mOldAutomatic);
        }
        if (mModeChangeSelf == true && mModeChangeOut == false) {//Reset if brightness mode changed by self 
        	mModeChangeSelf = false;
        }
	}

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        final ContentResolver resolver = getContext().getContentResolver();
	//Unregister the observer when dialog closed
	resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mBrightnessModeObserver);

        if (positiveResult) {
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    mSeekBar.getProgress() + mScreenBrightnessDim);
        } 
	else {
            restoreOldState();
        }

        
    }

    private void restoreOldState() {
       // if (mRestoredOldState) return;

        if (mAutomaticAvailable) {
            setMode(mOldAutomatic);
        }
        if (!mAutomaticAvailable || mOldAutomatic == 0) {
            setBrightness(mOldBrightness);
            updateSeekBarPos(mOldBrightness);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    mOldBrightness);
        }
        //mRestoredOldState = true;
    }
    
    private void updateSeekBarPos(int value) {
		if(mSeekBar!=null) {
			Log.i(TAG,"updateSeekBar position" );
			mSeekBar.setProgress(value - mScreenBrightnessDim);
		}
	}

    private void setBrightness(int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            //Only set backlight value when screen is on
            if (power != null && power.isScreenOn()) {
                power.setBacklightBrightness(brightness);
            }
        } catch (RemoteException doe) {
            
        }
    }

    private void setMode(int mode) {
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            mSeekBar.setVisibility(View.GONE);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
        }
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) 
        	return superState;
        Log.i(TAG,"onSaveInstanceState");
        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.automatic = mCheckBox.isChecked();
        myState.progress = mSeekBar.getProgress();
        myState.oldAutomatic = mOldAutomatic == 1;
        myState.oldProgress = mOldBrightness;

        // Restore the old state when the activity or dialog is being paused
        restoreOldState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
    	Log.i(TAG,"onRestoreInstanceState");
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOldBrightness = myState.oldProgress;
        mOldAutomatic = myState.oldAutomatic ? 1 : 0;
        setMode(myState.automatic ? 1 : 0);
        setBrightness(myState.progress + mScreenBrightnessDim);
    }

    private static class SavedState extends BaseSavedState {

        boolean automatic;
        boolean oldAutomatic;
        int progress;
        int oldProgress;

        public SavedState(Parcel source) {
            super(source);
            automatic = source.readInt() == 1;
            progress = source.readInt();
            oldAutomatic = source.readInt() == 1;
            oldProgress = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(automatic ? 1 : 0);
            dest.writeInt(progress);
            dest.writeInt(oldAutomatic ? 1 : 0);
            dest.writeInt(oldProgress);
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
}

