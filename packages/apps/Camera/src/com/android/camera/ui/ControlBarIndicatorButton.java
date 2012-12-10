package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.camera.R;

import com.android.camera.CameraSettings;
import com.android.camera.IconListPreference;
import com.android.camera.CameraPreference.OnPreferenceChangedListener;

public class ControlBarIndicatorButton extends AbstractIndicatorButton 
				implements BasicSettingPopup.Listener{
	
	private IconListPreference mPreference;
	private OnPreferenceChangedListener mListener;
	
	public ControlBarIndicatorButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initializeIndicatorPref(IconListPreference preference) {
		mPreference = preference;
		reloadPreference();
	}
	
	public void setSettingChangedListener(OnPreferenceChangedListener listener) {
		mListener = listener;
    }
	
	@Override
	public void overrideSettings(String... keyvalues) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void overrideSettings(String key, String value, String[] values) {
		
	}
	
	@Override
    protected void initializePopup() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup) getRootView().findViewById(R.id.frame_layout);

        BasicSettingPopup normalCapture = (BasicSettingPopup) inflater.inflate(
                R.layout.basic_setting_popup, root, false);
        normalCapture.initialize(mPreference);
        normalCapture.setSettingChangedListener((BasicSettingPopup.Listener)this);
        mPopup = normalCapture;
        root.addView(mPopup);
    }
	 
	public void onSettingChanged() {
		reloadPreference();
        // Dismiss later so the activated state can be updated before dismiss.
        dismissPopupDelayed();
        if (mListener != null) {
            mListener.onSharedPreferenceChanged();
        }
	}
	
	@Override
	public void reloadPreference() {
	    int[] iconIds = mPreference.getLargeIconIds();
	    if (iconIds != null) {
	        // Each entry has a corresponding icon.
	        int index = mPreference.findIndexOfValue(mPreference.getValue());
	        setImageResource(iconIds[index]);
	    } else {
	        // The preference only has a single icon to represent it.
	        setImageResource(mPreference.getSingleIcon());
	    }
	    super.reloadPreference();
	}
	
	public void forceReloadPreference() {
		mPreference.reloadValue();
		reloadPreference();
	}
	
}
