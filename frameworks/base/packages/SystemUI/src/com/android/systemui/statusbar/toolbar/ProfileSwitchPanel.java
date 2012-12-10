package com.android.systemui.statusbar.toolbar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.util.Configurable;
import com.mediatek.audioprofile.AudioProfileState;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.audioprofile.AudioProfileListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public final class ProfileSwitchPanel extends LinearLayout implements Configurable {
    private static final String TAG = "ProfileSwitchPanelView";
    private static final boolean DBG = true;

    private boolean mUpdating = false;
    
    private static final int COUNT = 4;
    
    private ToolBarView mToolBarView;

    private AudioProfileManager mProfileManager;
    private AudioManager mAudioManager;
    
    private List<String> mProfileKeys;

    // audio profile panel views
    private ConfigurationIconView mNormalProfileIcon;
    private ConfigurationIconView mMettingProfileIcon;
    private ConfigurationIconView mMuteProfileIcon;
    private ConfigurationIconView mOutdoorSwitchIcon;
    
    private ImageView mNormalOnIndicator;
    private ImageView mMettingOnIndicator;
    private ImageView mMuteOnIndicator;
    private ImageView mOutdoorOnIndicator;
    
    private Drawable mIndicatorView;

	private View.OnClickListener mProfileSwitchListener = new View.OnClickListener() {
		public void onClick(View v) {
			for (int i = 0; i < mProfileKeys.size(); i++) {
				if (v.getTag().equals(mProfileKeys.get(i))) {
					if (DBG) {
					    Xlog.i(TAG, "onClick called, profile clicked is: "+ mProfileKeys.get(i));
					}
					String key = mProfileKeys.get(i);
					updateAudioProfile(key);
					Scenario senario = AudioProfileManager.getScenario(key);
					updateProfileView(senario);
					//if (!senario.equals(AudioProfile.Scenario.SILENT)) {
					//	mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					//}
				}
			}
		}
	};

	private AudioProfileListener mAudioProfileListenr = new AudioProfileListener() {
		@Override
		public void onAudioProfileChanged(String profileKey) {
			if (profileKey != null ) {
				if (!mUpdating) {
					// AudioProfile is no ready, so skip update
					return;
				}
				Scenario senario = AudioProfileManager.getScenario(profileKey);
				if (DBG) {
					Xlog.i(TAG, "onReceive called, profile type is: "+ senario);
				}
				if (senario != null) {
					updateProfileView(senario);
				}
			}
		}
	};

    public ProfileSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }
    
    public void buildProfileIconViews() {
    	mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mProfileManager = (AudioProfileManager) mContext.getSystemService(Context.AUDIOPROFILE_SERVICE);
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        
        mIndicatorView = this.getResources().getDrawable(R.drawable.zzz_light_on);
        
        mNormalProfileIcon = (ConfigurationIconView)this.getChildAt(0);
        mMuteProfileIcon = (ConfigurationIconView)this.getChildAt(1);
        mMettingProfileIcon = (ConfigurationIconView)this.getChildAt(2);
        mOutdoorSwitchIcon = (ConfigurationIconView)this.getChildAt(3);
        
        mNormalProfileIcon.setConfigName(R.string.normal);
        mMuteProfileIcon.setConfigName(R.string.mute);
        mMettingProfileIcon.setConfigName(R.string.meeting);
        mOutdoorSwitchIcon.setConfigName(R.string.outdoor);
        
        mNormalProfileIcon.setClickListener(mProfileSwitchListener);
        mNormalProfileIcon.setTagForIcon(AudioProfileManager.getProfileKey(Scenario.GENERAL));

        mMuteProfileIcon.setClickListener(mProfileSwitchListener);
        mMuteProfileIcon.setTagForIcon(AudioProfileManager.getProfileKey(Scenario.SILENT));

        mMettingProfileIcon.setClickListener(mProfileSwitchListener);
        mMettingProfileIcon.setTagForIcon(AudioProfileManager.getProfileKey(Scenario.MEETING));

        mOutdoorSwitchIcon.setClickListener(mProfileSwitchListener);
        mOutdoorSwitchIcon.setTagForIcon(AudioProfileManager.getProfileKey(Scenario.OUTDOOR));
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    void setUpdates(boolean update) {
        if (update != mUpdating) {
            Xlog.i(TAG, "setUpdates: update = " + update);
            mProfileKeys = new ArrayList<String>();
            mProfileKeys = mProfileManager.getPredefinedProfileKeys();
            mUpdating = update;
            if (update) {
                // Register for Intent broadcasts for the clock and battery
                Xlog.i(TAG, "setUpdates: listenAudioProfie with mAudioProfileListenr = " + mAudioProfileListenr);
                mProfileManager.listenAudioProfie(mAudioProfileListenr.getCallback(), AudioProfileListener.LISTEN_AUDIOPROFILE_CHANGEG);
            } else {
                mProfileManager.listenAudioProfie(mAudioProfileListenr.getCallback(), AudioProfileListener.LISTEN_NONE);
            }
        }
    }

    private void updateProfileView(Scenario scenario) {
        loadDisabledProfileResouceForAll();
        loadEnabledProfileResource(scenario);

    }

    private void loadDisabledProfileResouceForAll() {
        mNormalProfileIcon.setConfigDrawable(R.drawable.zzz_normal_profile_disable);
        mMettingProfileIcon.setConfigDrawable(R.drawable.zzz_meeting_profile_disable);
        mMuteProfileIcon.setConfigDrawable(R.drawable.zzz_mute_profile_disable);
        mOutdoorSwitchIcon.setConfigDrawable(R.drawable.zzz_outdoor_profile_disable);
        
        mNormalProfileIcon.setOnIndicator(false);
        mMettingProfileIcon.setOnIndicator(false);
        mMuteProfileIcon.setOnIndicator(false);
        mOutdoorSwitchIcon.setOnIndicator(false);
    }

    private void loadEnabledProfileResource(Scenario scenario) {
    	if (DBG) {
    	    Xlog.i(TAG, "loadEnabledProfileResource called, profile is: " + scenario);
    	}
        switch (scenario) {
        case GENERAL:
            mNormalProfileIcon.setConfigDrawable(R.drawable.zzz_normal_profile_enable);
            mNormalProfileIcon.setOnIndicator(true);
            break;
        case MEETING:
            mMettingProfileIcon.setConfigDrawable(R.drawable.zzz_meeting_profile_enable);
            mMettingProfileIcon.setOnIndicator(true);
            break;
        case OUTDOOR:
            mOutdoorSwitchIcon.setConfigDrawable(R.drawable.zzz_outdoor_profile_enable);
            mOutdoorSwitchIcon.setOnIndicator(true);
            break;
        case SILENT:
            mMuteProfileIcon.setConfigDrawable(R.drawable.zzz_mute_profile_enable);
            mMuteProfileIcon.setOnIndicator(true);
            break;
        }
    }

    private void updateAudioProfile(String key) {
        if (key == null) {
            return;
        }
        
        
        if (DBG) {
            Xlog.i(TAG, "updateAudioProfile called, selected profile is: " + key);
    	}
        
        /*if(senario.equals(AudioProfile.Scenario.SILENT)){
	    	if(audioProfile.getVibrationEnabled()){
	    		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	    	} else{
	    		mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	    	}
	    }*/
        
        mProfileManager.setActiveProfile(key);
        if (DBG) {
            Xlog.i(TAG, "updateAudioProfile called, setActiveProfile is: " + key);
    	}
    }
    
    @Override
    public void initConfigurationState() {
        if (mProfileManager.getActiveProfileKey() != null) {
            updateProfileView(AudioProfileManager.getScenario(mProfileManager.getActiveProfileKey()));
        }
    }
    
    public void enlargeTouchRegion() {
    	mNormalProfileIcon.enlargeTouchRegion();
        mMettingProfileIcon.enlargeTouchRegion();
        mMuteProfileIcon.enlargeTouchRegion();
        mOutdoorSwitchIcon.enlargeTouchRegion();
    }

    public void updateResources(){
    	  mNormalProfileIcon.setConfigName(R.string.normal);
        mMuteProfileIcon.setConfigName(R.string.mute);
        mMettingProfileIcon.setConfigName(R.string.meeting);
        mOutdoorSwitchIcon.setConfigName(R.string.outdoor);
    }
}