/**
 * 
 */
package com.android.settings.audioprofile;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.content.res.TypedArray;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.xlog.Xlog;

/**
 * @author mtk80800
 *
 */
public class AudioProfilePreference extends Preference implements CompoundButton.OnCheckedChangeListener{
	
    private static final String XLogTAG = "Settings/AudioP";
    private static final String TAG = "AudioProfilePreference:";
    
	private static CompoundButton mCurrentChecked = null;
	private static String activeKey = null;
	
	private String mPreferenceTitle = null;
	private String mPreferenceSummary = null;
	
	private TextView mTextView = null;
	private TextView mSummary = null;
	private RadioButton mCheckboxButton = null;
	
	private AudioProfileManager mProfileManager;
	private Context             mContext;
    private String              mKey;
	
    public AudioProfilePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mContext = context;
        
        //get the title from audioprofile_settings.xml
        if(super.getTitle() != null){
        	mPreferenceTitle = super.getTitle().toString();
        }
        
        //get the summary from audioprofile_settings.xml
        if(super.getSummary() != null){
        	mPreferenceSummary = super.getSummary().toString();
        }
        
        mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        
        mKey = getKey();
        
        if(Utils.isCmccLoad()) {
            setLayoutResource(R.layout.audio_profile_item_cmcc);
        } else {
            setLayoutResource(R.layout.audio_profile_item);
        }
        Xlog.d(XLogTAG, TAG + "new AudioProfilePreference setLayoutResource");
    }  
    
    public AudioProfilePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }    
    
    public AudioProfilePreference(Context context) {
        this(context, null);
    }
    
     public void setProfileKey(String key) {
    	 setKey(key);
    	 mKey = key;
     }
     
    @Override
    public View getView(View convertView, ViewGroup parent) {
    	Xlog.d(XLogTAG, TAG + "getView from " + getKey());
        View view = super.getView(convertView, parent);

        mCheckboxButton = (RadioButton)view.findViewById(R.id.radiobutton);
        
        if (mCheckboxButton != null){
            //mCheckboxButton.setOnCheckedChangeListener(this);
        	mCheckboxButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Xlog.d(XLogTAG, TAG + "onClick " + getKey());
					
		            if(!mCheckboxButton.equals(mCurrentChecked)) {
				        if (mCurrentChecked != null) {
				            mCurrentChecked.setChecked(false);
				            
							mCheckboxButton.setChecked(true);
					        mCurrentChecked = mCheckboxButton;
					        mProfileManager.setActiveProfile(mKey);
				        } 
		            } else {
		            	Xlog.d(XLogTAG, TAG + "Click the active profile, do nothing return" );
		            }
			    }
			});
        	
        	mCheckboxButton.setChecked(isChecked());
        	if(isChecked()) {
        		setChecked();
        	}
        }

        mTextView = (TextView) view.findViewById(R.id.profiles_text);
        if(mPreferenceTitle != null){
            mTextView.setText(mPreferenceTitle);
        } else {
            Xlog.d(XLogTAG, TAG + "PreferenceTitle is null");
        }
        
        mSummary = (TextView) view.findViewById(R.id.profiles_summary);
        dynamicShowSummary();        	

        return view;
    }

    public void dynamicShowSummary() {
        Xlog.d(XLogTAG, TAG + mKey + " dynamicShowSummary");
        
    	if(mSummary != null) {
    		Scenario scenario = mProfileManager.getScenario(mKey);
    		if((Scenario.GENERAL).equals(scenario) || (Scenario.CUSTOM).equals(scenario)) {
                
    			boolean VibrationEnabled = mProfileManager.getVibrationEnabled(mKey);
    			
                Xlog.d(XLogTAG, TAG + "VibrationEnabled" + VibrationEnabled);
                
    			if(VibrationEnabled) {
    				mSummary.setText(mContext.getString(R.string.ring_vibrate_summary));
    			} else {
    				mSummary.setText(mContext.getString(R.string.ring_summary));
    			}
    		} else {
    	        if(mPreferenceSummary != null){
    			    mSummary.setText(mPreferenceSummary);
    		    }
    	    }
    	} else {
            if( mSummary != null){
            	Xlog.d(XLogTAG, TAG + "summary object is null");
            }
    	}
    }
    
    public void onClick() {
    	
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Xlog.d(XLogTAG, TAG + "onCheckedChanged " + isChecked + getKey());
        
    	if (isChecked) {
            if (mCurrentChecked != null) {
                mCurrentChecked.setChecked(false);
            } 
            mCurrentChecked = buttonView;
            
            mProfileManager.setActiveProfile(mKey);
        }
    }
    
    public boolean isChecked() {
        if(activeKey != null){
    	    return getKey().equals(activeKey);
        }
        return false;
    }

    public void setChecked() {
    	activeKey = getKey();
        if(mCheckboxButton != null){
            if(!mCheckboxButton.equals(mCurrentChecked)) {
    	        if (mCurrentChecked != null) {
    	            mCurrentChecked.setChecked(false);
    	        } 
                Xlog.d(XLogTAG, TAG + "setChecked" + getKey());
            	mCheckboxButton.setChecked(true);
    	        mCurrentChecked = mCheckboxButton;
            }

        } else {
            Xlog.d(XLogTAG, TAG + "mCheckboxButton is null");
        }
    }
    
    public void setTitle(String title, boolean setToProfile){
        mPreferenceTitle = title;
        if(setToProfile) {
        	mProfileManager.setProfileName(mKey, title);
        }
        if(mTextView!=null){
            mTextView.setText(title);
        }
    }

    public String getTitle(){
        return mPreferenceTitle;
    }
}
