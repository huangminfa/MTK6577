package com.android.settings.gemini;


import android.content.Context;
import android.content.Intent;

import android.graphics.drawable.Drawable;

import android.preference.Preference;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.internal.telephony.Phone;
import com.android.settings.R;

public class SimInfoEnablePreference extends SimInfoPreference implements
		OnClickListener {
	
	interface OnPreferenceClickCallback {

	    public void  onPreferenceClick(long simid);
	}
	
    final static String TAG = "SimInfoEnablePreference";
    private OnCheckedChangeListener mSwitchChangeListener;;
    private Context mContext;
    private boolean mRadioOn;
    private OnPreferenceClickCallback mClickCallback;

	public SimInfoEnablePreference(Context context, String name, String number,
			int SimSlot, int status, int color, int DisplayNumberFormat,
			long key) {
		super(context, name, number, SimSlot, status, color,
				DisplayNumberFormat, key, true);
		mContext = context;
		mRadioOn = true;
        setLayoutResource(R.layout.preference_sim_info_enabler);
	}

	@Override
	public View getView(View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
       View view = super.getView(convertView, parent);
       
        
        Switch ckRadioOn = (Switch)view.findViewById(R.id.Check_Enable);
        
        if (ckRadioOn != null) {
        	if(mSwitchChangeListener!=null) {
        		ckRadioOn.setClickable(true);
        		//ckRadioOn.setFocusable(true);
        		ckRadioOn.setOnCheckedChangeListener(mSwitchChangeListener);
        	}
        }
        View siminfoLayout = view.findViewById(R.id.sim_info_layout);
        if ((siminfoLayout != null) && siminfoLayout instanceof LinearLayout) {
        	siminfoLayout.setOnClickListener(this);
        	//siminfoLayout.setFocusable(true);
        } 
        

		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == null)
			return;

		if ((v.getId() != R.id.Check_Enable)&&(mClickCallback != null)) {
			mClickCallback.onPreferenceClick(Long.valueOf(getKey()));
		}

	}
	
    void SetCheckBoxClickListener(OnCheckedChangeListener listerner) {
    	mSwitchChangeListener = listerner;

    }
    
    void setClickCallback(OnPreferenceClickCallback callBack) {
    	mClickCallback = callBack;
    }
    boolean isRadioOn(){
    	return mRadioOn;
    }
    
    void setRadioOn(boolean radioOn){
    	mRadioOn = radioOn;
    	
    }

}
