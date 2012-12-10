package com.android.settings.deviceinfo;

import android.preference.Preference;
import android.content.Context;
import com.android.settings.R;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.CompoundButton;

import com.mediatek.xlog.Xlog;

public class RadioButtonPreference extends Preference implements View.OnClickListener{
    private final static int TITLE_ID  = R.id.preference_title;
    private final static int BUTTON_ID = R.id.preference_radiobutton;
    private TextView     mPreferenceTitle  = null;
    private RadioButton  mPreferenceButton = null;
    private CharSequence mTitleValue       = "";
    private boolean      mChecked          = false;
    private String 		 mMountPath;
    
    private String       TAG               = "RadioButtonPreference";
    public RadioButtonPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_radiobutton);
    }
    
    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);
        Xlog.d(TAG, "getview");
        mPreferenceTitle  = (TextView)view.findViewById(TITLE_ID);
        mPreferenceTitle.setText(mTitleValue);
        mPreferenceButton = (RadioButton)view.findViewById(BUTTON_ID);
        mPreferenceButton.setOnClickListener(this);
        mPreferenceButton.setChecked(mChecked);
        return view;
    }
    
    @Override        
    public void setTitle(CharSequence title) {
        if (null == mPreferenceTitle) {
            mTitleValue = title;
        }
        if (!title.equals(mTitleValue)) {
            mTitleValue = title;
            mPreferenceTitle.setText(mTitleValue);
        }
    }

    @Override
    public CharSequence getTitle() {
        return mTitleValue;
    }

    public boolean isChecked() {
        return mChecked;
    }
    
    @Override
    public void onClick(View v) {
        boolean newValue = !isChecked();

        if (false == newValue) {
            Xlog.d(TAG, "button.onClick return");
            return;
        }
        
        if (setChecked(newValue)){
            callChangeListener(newValue);
            Xlog.d(TAG, "button.onClick");
        } 
    }
    
    @Override
    protected void onClick() {

        super.onClick();
        
        boolean newValue = !isChecked();

        if (false == newValue) {
            Xlog.d(TAG, "preference.onClick return");
            return;
        }

        if (setChecked(newValue)){
            callChangeListener(newValue);
            Xlog.d(TAG, "preference.onClick"); 
        }       
    }

    public boolean setChecked(boolean checked) {
        if (null == mPreferenceButton) {
            Xlog.d(TAG, "setChecked return");
            mChecked = checked;
            return false;
        }
        
        if (mChecked != checked) {
            mPreferenceButton.setChecked(checked);
            mChecked = checked;
            return true;
        }
        return false;
    }
    
    public void setPath(String path){
    	mMountPath = path;
    }
    
    public String getPath(){
    	return mMountPath;
    }
}

