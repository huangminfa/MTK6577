package com.android.systemui.statusbar.toolbar;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.util.Configurable;
import com.android.systemui.statusbar.util.StateTracker;
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public final class ConfigurationSwitchPanel extends LinearLayout implements Configurable {
    private static final String TAG = "ConfigurationSwitchPanel";
    private static final boolean DBG = true;
    
    /**
     * Minimum and maximum brightnesses. Don't go to 0 since that makes the
     * display unusable
     */
    public static final int MINIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_DIM + 10;

    public static final int MAXIMUM_BACKLIGHT = android.os.Power.BRIGHTNESS_ON;

    public static final int DEFAULT_BACKLIGHT = (int) (android.os.Power.BRIGHTNESS_ON * 0.4f);
    
    public static final int MINIMUM_TIMEOUT = 15000;

    public static final int MEDIUM_TIMEOUT = 30000;

    public static final int MAXIMUM_TIMEOUT = 60000;
    
    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private boolean mUpdating = false;
    
    private static final int COUNT = 3;

    private Context mContext;
    private ToolBarView mToolBarView;
    
    private ConfigurationIconView mBrightnessIcon;
    private ConfigurationIconView mTimeoutIcon;
    private ConfigurationIconView mAutoRatationIcon;
    
    private Drawable mIndicatorView;

    private BrightnessStateTracker mBrightnessStateTracker;
    private TimeoutStateTracker mTimeoutStateTracker;
    private AutoRotationStateTracker mAutoRotationStateTracker;
    
    private ContentObserver mBrightnessChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	mBrightnessStateTracker.onActualStateChange(mContext, null);
        	mBrightnessStateTracker.setImageViewResources(mContext);
        }
    };
    
    private ContentObserver mBrightnessModeChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	mBrightnessStateTracker.onActualStateChange(mContext, null);
        	mBrightnessStateTracker.setImageViewResources(mContext);
        }
    };
    
    private ContentObserver mTimeoutChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	mTimeoutStateTracker.onActualStateChange(mContext, null);
        	mTimeoutStateTracker.setImageViewResources(mContext);
        }
    };
    
    private ContentObserver mAutoRotationChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mAutoRotationStateTracker.onActualStateChange(mContext, null);
            mAutoRotationStateTracker.setImageViewResources(mContext);
        }
    };

    public ConfigurationSwitchPanel(Context context) {
        this(context, null);
    }

    public ConfigurationSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }

    public void buildIconViews() {
    	
        mBrightnessStateTracker = new BrightnessStateTracker();
        mTimeoutStateTracker = new TimeoutStateTracker();
        mAutoRotationStateTracker = new AutoRotationStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        
        mBrightnessIcon = (ConfigurationIconView)this.getChildAt(0);
        mTimeoutIcon = (ConfigurationIconView)this.getChildAt(1);
        mAutoRatationIcon = (ConfigurationIconView)this.getChildAt(2);
        
        mBrightnessIcon.setConfigName(R.string.brightness);
        mTimeoutIcon.setConfigName(R.string.timeout);
        mAutoRatationIcon.setConfigName(R.string.autorotate);

        mBrightnessIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mBrightnessStateTracker.toggleState(mContext);
            }
        });
        mTimeoutIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mTimeoutStateTracker.toggleState(mContext);
            }
        });
        mAutoRatationIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mAutoRotationStateTracker.toggleState(mContext);
            }
        });
    }
    
    @Override
    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    void setUpdates(boolean update) {
        if (update != mUpdating) {
            mUpdating = update;
            if (update) {
                mContext.getContentResolver().registerContentObserver(
	                        Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
	                        true, mBrightnessChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        true, mBrightnessModeChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT),
                        true, mTimeoutChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        true, mAutoRotationChangeObserver);
            } else {
                mContext.getContentResolver().unregisterContentObserver(mBrightnessChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mBrightnessModeChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mTimeoutChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mAutoRotationChangeObserver);
            }
        }
    }

    /**
     * Subclass of StateTracker to get/set Wifi state.
     */
    private final class BrightnessStateTracker extends StateTracker {
    	
        @Override
        public int getActualState(Context context) {
            return STATE_ENABLED;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
        	setCurrentState(context, STATE_ENABLED);
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
        	setCurrentState(context, STATE_ENABLED);
        }

        @Override
        public int getDisabledResource() {
            return R.drawable.zzz_brightness_off;
        }

        @Override
        public int getEnabledResource() {
        	boolean brightnessMode = getBrightnessMode(mContext);
        	if (brightnessMode) {
        		return R.drawable.zzz_brightness_auto;
        	} else {
        	    return R.drawable.zzz_brightness_on;
        	}
        }

        @Override
        public ImageView getImageButtonView() {
            return mBrightnessIcon.getConfigView();
        }

		@Override
		public ImageView getIndicatorView() {
			// TODO Auto-generated method stub
			ImageView imageView;
			imageView = mBrightnessIcon.getIndicatorView();
			boolean brightnessMode = getBrightnessMode(mContext);
			if (DBG) {
			    Xlog.i(TAG, "Brightnesst getIndicatorView: brightnessMode is " + brightnessMode);
			}
        	if (brightnessMode) {
        		imageView.setImageDrawable(null);
        		return imageView;
        	}
			int brightness = getBrightness(mContext);
			switch (brightness) {
            case DEFAULT_BACKLIGHT:
            	imageView.setImageResource(R.drawable.zzz_light_middle);
                break;
            case MAXIMUM_BACKLIGHT:
            	imageView.setImageResource(R.drawable.zzz_light_fully);
                break;
            case MINIMUM_BACKLIGHT:
            	imageView.setImageResource(R.drawable.zzz_light_low);
                break;
			}
			return imageView;
		}
		
		@Override
		public void toggleState(Context context) {
			toggleBrightness(context);
		}
    }

    /**
     * Subclass of StateTracker to get/set Bluetooth state.
     */
    private final class TimeoutStateTracker extends StateTracker {
    	
        @Override
        public int getActualState(Context context) {
            return STATE_ENABLED;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
        	setCurrentState(context, STATE_ENABLED);
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            
            setCurrentState(context, STATE_ENABLED);
        }

        public int getDisabledResource() {
            return R.drawable.zzz_timeout_off;
        }

        public int getEnabledResource() {
            return R.drawable.zzz_timeout_on;
        }

        public ImageView getImageButtonView() {
            return mTimeoutIcon.getConfigView();
        }
        
        @Override
		public ImageView getIndicatorView() {
			// TODO Auto-generated method stub
			ImageView imageView;
			imageView = mTimeoutIcon.getIndicatorView();
			int brightness = getTimeout(mContext);
			switch (brightness) {
            case MINIMUM_TIMEOUT:
            	imageView.setImageResource(R.drawable.zzz_light_low);
                break;
            case MEDIUM_TIMEOUT:
            	imageView.setImageResource(R.drawable.zzz_light_middle);
                break;
            case MAXIMUM_TIMEOUT:
            	imageView.setImageResource(R.drawable.zzz_light_fully);
                break;
			}
			return imageView;
		}
        
        @Override
		public void toggleState(Context context) {
			toggleTimeout(context);
		}
    }

    /**
     * Subclass of StateTracker for AutoRotation state.
     */
    private final class AutoRotationStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            int state = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1);
            if (state == 1) {
                return STATE_ENABLED;
            } else if (state == 0) {
                return STATE_DISABLED;
            } else {
                return STATE_UNKNOWN;
            }
        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
            // Note: the broadcast location providers changed intent
            // doesn't include an extras bundles saying what the new value is.
            setCurrentState(context, getActualState(context));
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... args) {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION, desiredState ? 1 : 0);
                    return desiredState;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    setCurrentState(context, result ? STATE_ENABLED : STATE_DISABLED);
                }
            }.execute();
        }

        public int getDisabledResource() {
            return R.drawable.zzz_auto_rotation_off;
        }

        public int getEnabledResource() {
            return R.drawable.zzz_auto_rotation_enable;
        }

        public ImageView getImageButtonView() {
            return mAutoRatationIcon.getConfigView();
        }
        
        @Override
        public ImageView getIndicatorView() {
            return mAutoRatationIcon.getIndicatorView();
        }
    }
    
    /**
     * Increases or decreases the brightness.
     * 
     * @param context
     */
    private void toggleBrightness(Context context) {
    	try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                ContentResolver cr = context.getContentResolver();
                int brightness = Settings.System.getInt(cr,
                        Settings.System.SCREEN_BRIGHTNESS);
                int brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                //Only get brightness setting if available
                if (context.getResources().getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available)) {
                    brightnessMode = Settings.System.getInt(cr,
                            Settings.System.SCREEN_BRIGHTNESS_MODE);
                }

                // Rotate AUTO -> MINIMUM -> DEFAULT -> MAXIMUM
                // Technically, not a toggle...
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    brightness = MINIMUM_BACKLIGHT;
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                } else if (brightness < DEFAULT_BACKLIGHT) {
                    brightness = DEFAULT_BACKLIGHT;
                } else if (brightness < MAXIMUM_BACKLIGHT) {
                    brightness = MAXIMUM_BACKLIGHT;
                } else {
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                    brightness = MINIMUM_BACKLIGHT;
                }

                if (context.getResources().getBoolean(
                        com.android.internal.R.bool.config_automatic_brightness_available)) {
                    // Set screen brightness mode (automatic or manual)
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            brightnessMode);
                } else {
                    // Make sure we set the brightness if automatic mode isn't available
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                }
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                    power.setBacklightBrightness(brightness);
                    Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, brightness);
                }
            }
        } catch (RemoteException e) {
            Xlog.d(TAG, "toggleBrightness: " + e);
        } catch (Settings.SettingNotFoundException e) {
            Xlog.d(TAG, "toggleBrightness: " + e);
        }
    }
    
    /**
     * Gets state of brightness.
     * 
     * @param context
     * @return true if more than moderately bright.
     */
    public static int getBrightness(Context context) {
    	if (DBG) {
    	    Xlog.i(TAG, "getBrightness called.");
    	}
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                int brightness = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                if (brightness <= MINIMUM_BACKLIGHT) {
                    brightness = MINIMUM_BACKLIGHT;
                } else if (brightness <= DEFAULT_BACKLIGHT) {
                    brightness = DEFAULT_BACKLIGHT;
                } else {
                    brightness = MAXIMUM_BACKLIGHT;
                }
                return brightness;
            }
        } catch (Exception e) {
            Xlog.d(TAG, "getBrightness: " + e);
        }
        return DEFAULT_BACKLIGHT;
    }
    
    /**
     * Gets state of brightness mode.
     *
     * @param context
     * @return true if auto brightness is on.
     */
    private static boolean getBrightnessMode(Context context) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                int brightnessMode = Settings.System.getInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE);
                return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            }
        } catch (Exception e) {
            Xlog.d(TAG, "getBrightnessMode: " + e);
        }
        return false;
    }
    
    /**
     * Increases or decreases the brightness.
     * 
     * @param context
     */
    private void toggleTimeout(Context context) {
        try {
        	ContentResolver cr = context.getContentResolver();
        	int timeout = Settings.System.getInt(cr, 
        			SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        	if (DBG) {
        	    Xlog.i(TAG, "toggleTimeout, before is " + timeout);
        	}
        	if (timeout <= MINIMUM_TIMEOUT) {
        		timeout = MEDIUM_TIMEOUT;
        	} else if (timeout <= MEDIUM_TIMEOUT) {
        		timeout = MAXIMUM_TIMEOUT;
        	} else {
        		timeout = MINIMUM_TIMEOUT;
        	}
        	Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        	if (DBG) {
        	    Xlog.i(TAG, "toggleTimeout, after is " + timeout);
        	}
        } catch (Exception e) {
            Xlog.d(TAG, "toggleTimeout: " + e);
        }
    }
    
    /**
     * Gets state of brightness.
     * 
     * @param context
     * @return true if more than moderately bright.
     */
    public static int getTimeout(Context context) {
        try {
        	int timeout = Settings.System.getInt(context.getContentResolver(), SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
            if (timeout <= MINIMUM_TIMEOUT) {
            	timeout = MINIMUM_TIMEOUT;
            } else if (timeout <= MEDIUM_TIMEOUT) {
            	timeout = MEDIUM_TIMEOUT;
            } else {
            	timeout = MAXIMUM_TIMEOUT;
            }
            return timeout;
        } catch (Exception e) {
            Xlog.d(TAG, "getTimeout: " + e);
        }
        return MEDIUM_TIMEOUT;
    }
    
    @Override
    public void initConfigurationState() {
    	mBrightnessStateTracker.setImageViewResources(mContext);
        mTimeoutStateTracker.setImageViewResources(mContext);
        mAutoRotationStateTracker.setImageViewResources(mContext);
    }
    
    public void enlargeTouchRegion() {
    	mBrightnessIcon.enlargeTouchRegion();
    	mTimeoutIcon.enlargeTouchRegion();
    	mAutoRatationIcon.enlargeTouchRegion();
    }
    
    public void updateResources(){
    	mBrightnessIcon.setConfigName(R.string.brightness);
    	mTimeoutIcon.setConfigName(R.string.timeout);
    	mAutoRatationIcon.setConfigName(R.string.autorotate);
    }
}
