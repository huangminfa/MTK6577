package com.android.systemui.statusbar.toolbar;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.systemui.R;
import com.android.systemui.statusbar.toolbar.SimIconsListView.SimItem;
import com.android.systemui.statusbar.util.Configurable;
import com.android.systemui.statusbar.util.SIMHelper;
import com.android.systemui.statusbar.util.StateTracker;
import com.mediatek.featureoption.FeatureOption; //FOR RDA_BT_SUPPORT
import com.mediatek.xlog.Xlog;

/**
 * [SystemUI] Support "Notification toolbar".
 */
public final class ConnectionSwitchPanel extends LinearLayout implements Configurable {
    private static final String TAG = "ConnectionSwitchPanelView";
    private static final boolean DBG = true;
    
    private static final String TRANSACTION_START = "com.android.mms.transaction.START";
    private static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";
    
    private static final boolean IS_CMCC = SystemProperties.get("ro.operator.optr").equals("OP01");
    private static final boolean IS_CU = SystemProperties.get("ro.operator.optr").equals("OP02");

    private boolean mUpdating = false;
    
    private static final int COUNT = 5;

    private Context mContext;
    
    private ToolBarView mToolBarView;
    
    private ConfigurationIconView mWifiIcon;
    private ConfigurationIconView mBluetoothIcon;
    private ConfigurationIconView mGpsIcon;
    private ConfigurationIconView mMobileIcon;
    private ConfigurationIconView mAirlineModeIcon;
    
    private Drawable mIndicatorView;

    private WifiStateTracker mWifiStateTracker;
    private BluetoothStateTracker mBluetoothStateTracker;
    private GpsStateTracker mGpsStateTracker;
    private MobileStateTracker mMobileStateTracker;
    private AirlineModeStateTracker mAirlineModeStateTracker;
    
    private AlertDialog mSwitchDialog;
    private SimIconsListView mSwitchListview;
    
    // whether the SIMs initialization of framework is ready.
    private boolean mSimCardReady = false;
    
    //time out message event
    private static final int EVENT_DETACH_TIME_OUT = 2000;
    private static final int EVENT_ATTACH_TIME_OUT = 2001;
    private static final int EVENT_DATA_CONNECTION_RESULT = 2010;
    
    //time out length
    private static final int DETACH_TIME_OUT_LENGTH = 10000;
    private static final int ATTACH_TIME_OUT_LENGTH = 30000;
    
    private int mDataConnectionSimSlot;
    
    private TelephonyManager mTelephonyManager;
    
    private Handler mDataTimerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        	long simId;
            switch (msg.what) {
                case EVENT_DETACH_TIME_OUT:
                	simId = Settings.System.getLong(mContext.getContentResolver(), 
                			Settings.System.GPRS_CONNECTION_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
                	Xlog.e(TAG, "detach time out......simId is " + simId);
                    mMobileStateTracker.setIsUserSwitching(false);
                    mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
                    mMobileIcon.getConfigView().setVisibility(VISIBLE);
                    if (simId > 0) {
                        mMobileIcon.getIndicatorView().setVisibility(VISIBLE);
                        int resId = SIMHelper.getDataConnectionIconIdBySlotId(mContext, 
                        		SIMInfo.getSlotById(mContext, simId));
                        if (resId == -1) {
                    		return;
                    	} else {
                    		if (resId != -1) {
                    			mMobileIcon.getConfigView().setImageResource(resId);
    						}
                    	}
                    } else {
                    	mMobileIcon.getIndicatorView().setVisibility(GONE);
                    	mMobileIcon.getConfigView().setImageResource(mMobileStateTracker.getDisabledResource());
                    }
                    break;
                case EVENT_ATTACH_TIME_OUT:
                	simId = Settings.System.getLong(mContext.getContentResolver(), 
                			Settings.System.GPRS_CONNECTION_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
                	Xlog.e(TAG, "attach time out......simId is " + simId);
                	mMobileStateTracker.setIsUserSwitching(false);
                    mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
                    mMobileIcon.getConfigView().setVisibility(VISIBLE);
                    if (simId > 0) {
                        mMobileIcon.getIndicatorView().setVisibility(VISIBLE);
                        int resId = SIMHelper.getDataConnectionIconIdBySlotId(mContext, 
                        		SIMInfo.getSlotById(mContext, simId));
                        if (resId == -1) {
                    		return;
                    	} else {
                    		if (resId != -1) {
                    			mMobileIcon.getConfigView().setImageResource(resId);
    						}
                    	}
                    } else {
                    	mMobileIcon.getIndicatorView().setVisibility(GONE);
                    	mMobileIcon.getConfigView().setImageResource(mMobileStateTracker.getDisabledResource());
                    }
                    break;
                case EVENT_DATA_CONNECTION_RESULT:
                	if(mToolBarView.isStatusBarExpanded()){
                        Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_LONG).show();
                	}
                	break;
            }
        }
    };
    
    private ContentObserver mMobileStateChangeObserver = new ContentObserver (new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	if(!mMobileStateTracker.getIsUserSwitching()) {
        		mMobileStateTracker.setImageViewResources(mContext);
        	}
        }
    };
    
    private ContentObserver mMobileStateForSingleCardChangeObserver = new ContentObserver (new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	mMobileStateTracker.onActualStateChange(mContext, null);
        	mMobileStateTracker.setImageViewResources(mContext);
        }
    };
    
    private int mServiceState1;
    private int mServiceState2;
    
    PhoneStateListener mPhoneStateListener1 = new PhoneStateListener() {

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Xlog.i(TAG, "PhoneStateListener1.onServiceStateChanged: serviceState="+serviceState);
            mServiceState1 = serviceState.getState();
            onAirplaneModeChanged();
        }            

    };

    PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Xlog.i(TAG, "PhoneStateListener2.onServiceStateChanged: serviceState="+serviceState);
            mServiceState2 = serviceState.getState();
            onAirplaneModeChanged();

        }                
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DBG) {
                Xlog.i(TAG, "onReceive called, action is " + action);
            }
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                mWifiStateTracker.onActualStateChange(context, intent);
                mWifiStateTracker.setImageViewResources(context);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                mBluetoothStateTracker.onActualStateChange(context, intent);
                mBluetoothStateTracker.setImageViewResources(context);
            } else if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                mGpsStateTracker.onActualStateChange(context, intent);
                mGpsStateTracker.setImageViewResources(context);
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	boolean enabled = intent.getBooleanExtra("state", false);
            	if (DBG) {
            	    Xlog.i(TAG, "airline mode changed: state is " + enabled);
                }
            	mMobileStateTracker.setAirlineMode(enabled);
            	mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
            	mMobileStateTracker.setImageViewResources(context);
            	if (IS_CMCC && FeatureOption.MTK_WLAN_SUPPORT) {
            		mWifiStateTracker.setAirlineMode(enabled);
            	    mWifiIcon.getConfigView().setEnabled(mWifiStateTracker.isClickable());
            	}
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
            	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        			Phone.DataState state = getMobileDataState(intent);
        			boolean isApnType=false;
        			String types=intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
        			if(types!=null){
        				String[] typeArray=types.split(",");
        				for(String type:typeArray){
        					if(Phone.APN_TYPE_DEFAULT.equals(type)){
        						isApnType=true;
        						break;
        					}
        				}
        			}
        			if (DBG) {
        			    Xlog.d(TAG,"isApnType = "+isApnType+" , state = "+state+" , mMobileStateTracker.mGprsTargSim = " + mMobileStateTracker.mGprsTargSim
        					    +" , mMobileStateTracker.mIsMmsOngoing = "+mMobileStateTracker.mIsMmsOngoing);
                    }
        			if (isApnType && ( (state == Phone.DataState.CONNECTED) || (state == Phone.DataState.DISCONNECTED) 
        					&& mMobileStateTracker.mGprsTargSim==false &&  mMobileStateTracker.mIsMmsOngoing==false ) ) {
        				mMobileStateTracker.onActualStateChange(context, intent);
                        mMobileStateTracker.setImageViewResources(context);
        			}
            	} else {
            		// do nothing
            	}
            } else if(action.equals(TRANSACTION_START)){
	        	mMobileStateTracker.setIsMmsOngoing(true);
	        	mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
	        } else if(action.equals(TRANSACTION_STOP)){
	        	mMobileStateTracker.setIsMmsOngoing(false);
	        	mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
	        }
    }};
     
    public void updateForSimReady() {
        Xlog.i(TAG, "Panel sim ready called");
        mSimCardReady = true;
        List<SIMInfo> simInfos = SIMHelper.getSIMInfoList(mContext);
        if (simInfos == null || simInfos.size() <= 0) {
        	mMobileStateTracker.setHasSim(false);
        } else {
        	mMobileStateTracker.setHasSim(true);
        }
        mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
        mMobileStateTracker.setImageViewResources(mContext);
    }
    
    /**
     * When siminfo changed, for example siminfo's background resource changed, need to update data connection
     * button's background.
     * 
     * @param intent The intent to use, used to get extra sim id information.
     */
    public void updateSimInfo(Intent intent) {
    	if (FeatureOption.MTK_GEMINI_SUPPORT) {
    		int type = intent.getIntExtra("type", -1);
            if (type == 1) {
                long simId = intent.getLongExtra("simid", -1);
                long currentSimId = SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING);
                if (simId == currentSimId) {
                    if (DBG) {
                        Xlog.i(TAG, "sim setting changed, simId is " + simId);
                    }
                	int resId = SIMHelper.getDataConnectionIconIdBySlotId(mContext, SIMInfo.getSlotById(mContext, simId));
                	if (DBG) {
                	    Xlog.i(TAG, "sim resId is " + resId);
                    }
                    if (resId == -1) {
                		return;
                	} else {
                		if (resId != -1) {
                			mMobileIcon.getConfigView().setImageResource(resId);
						}
                	}
                }
            }
    	} else {
    		// do nothing
    	}

    }

    /**
     * Called when we've received confirmation that the airplane mode was set.
     */
    private void onAirplaneModeChanged() {
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
        	// [ALPS00225004]
            // When AirplaneMode On, make sure both phone1 and phone2 are radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF ||
                	mServiceState2 != ServiceState.STATE_POWER_OFF) {
                    Xlog.i(TAG, "Unfinish! serviceState1:" + mServiceState1 + " serviceState2:" + mServiceState2);
                    return;
                }
            }   
        } else {
           /// [ALPS00127431]
            // When AirplaneMode On, make sure phone is radio off
            if (airplaneModeEnabled) {
                if (mServiceState1 != ServiceState.STATE_POWER_OFF) {
                    Xlog.i(TAG, "Unfinish! serviceState:" + mServiceState1);
                    return;
                }
            }
        }
        Xlog.i(TAG, "onServiceStateChanged called, inAirplaneMode is: " + airplaneModeEnabled);
        Intent intent = new Intent();
        intent.putExtra("state", airplaneModeEnabled);
        mAirlineModeStateTracker.onActualStateChange(mContext, intent);
        mAirlineModeStateTracker.setImageViewResources(mContext);
    }
    
    private boolean isCallStateIdle(){
    	int stateSim1 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_1);
    	int stateSim2 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_2);
    	Xlog.i(TAG,"stateSim1 is "+stateSim1+" stateSim2 is "+stateSim2);
    	return (stateSim1 == TelephonyManager.CALL_STATE_IDLE)&&(stateSim2 == TelephonyManager.CALL_STATE_IDLE);
    }
    
    private static Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(Phone.DataState.class, str);
        } else {
            return Phone.DataState.DISCONNECTED;
        }
    }

    public ConnectionSwitchPanel(Context context) {
        this(context, null);
    }

    public ConnectionSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }
    
    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }

    public void buildIconViews() {
    	if (FeatureOption.MTK_WLAN_SUPPORT) {
    	    mWifiStateTracker = new WifiStateTracker();
    	}
        if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
        	mBluetoothStateTracker = new BluetoothStateTracker();
        }
        if (FeatureOption.MTK_GPS_SUPPORT) {
        	mGpsStateTracker = new GpsStateTracker();
        }
        mMobileStateTracker = new MobileStateTracker();
        mAirlineModeStateTracker = new AirlineModeStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        ConfigurationIconView configIconView;
        if (FeatureOption.MTK_WLAN_SUPPORT) {
        	configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
        	configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        	mWifiIcon = configIconView;
        	mWifiIcon.setConfigName(R.string.wifi);
        	mWifiIcon.setClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
			WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			if (mWifiManager != null) {
		            int wifiApState = mWifiManager.getWifiApState();
		            if ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED)) {
		                mWifiManager.setWifiApEnabled(null, false);
		            }
			}
                    	mWifiStateTracker.toggleState(mContext);
                }
            });
        }
        if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
        	configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
        	configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
            mBluetoothIcon = configIconView;
        	mBluetoothIcon.setConfigName(R.string.bluetooth);
        	mBluetoothIcon.setClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothStateTracker.toggleState(mContext);
                }
            });
        }
        if (FeatureOption.MTK_GPS_SUPPORT) {
        	configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
        	configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
            mGpsIcon = configIconView;
        	mGpsIcon.setConfigName(R.string.gps);
        	mGpsIcon.setClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGpsStateTracker.toggleState(mContext);
                }
            });
        }
        
        configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
    	configIconView.setOrientation(LinearLayout.VERTICAL);
        this.addView(configIconView, layutparams);
        mMobileIcon = configIconView;
        mMobileIcon.setConfigName(R.string.mobile);
        mMobileIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMobileStateTracker.IsDataDialogShown() || mSwitchDialog != null && mSwitchDialog.isShowing()) {
	        		return;
	        	}
                mMobileStateTracker.toggleState(mContext);
            }
        });
    	
    	configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.zzz_toolbar_configuration_icon_view, null);
    	configIconView.setOrientation(LinearLayout.VERTICAL);
        this.addView(configIconView, layutparams);
        mAirlineModeIcon = configIconView;
        mAirlineModeIcon.setConfigName(R.string.offline);
        mAirlineModeIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Xlog.i("ClickEvent", "AirPlane button click");
                mAirlineModeStateTracker.setAirPlaneModeClickable(false);
            	mAirlineModeStateTracker.toggleState(mContext);
            	postDelayed(new Runnable() {
            		public void run() {
            			mAirlineModeStateTracker.setAirPlaneModeClickable(true);
            			mAirlineModeStateTracker.getImageButtonView().setEnabled(mAirlineModeStateTracker.isClickable());
            		}
            	}, 600);
            }
        });
        
        mIndicatorView = this.getResources().getDrawable(R.drawable.zzz_light_on);
    }
    
    private AlertDialog createDialog(View v, int resId) {
    	AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setCancelable(true)
        .setTitle(resId)
        .setView(v, 0, 0, 0, 0)
        .setInverseBackgroundForced(true)
        .setNegativeButton(android.R.string.cancel,
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mSwitchDialog != null) {
                	mSwitchDialog.hide();
                }
            }
            });
    	AlertDialog alertDialog = b.create();
    	alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL);
    	return alertDialog;
    }
    
    public void dismissDialogs() {
    	if (mSwitchDialog != null) {
        	mSwitchDialog.dismiss();
        }
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
                IntentFilter filter = new IntentFilter();
                if (FeatureOption.MTK_WLAN_SUPPORT) {
                    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                }
                if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
                    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                }
                if (FeatureOption.MTK_GPS_SUPPORT) {
                    filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                }

                // for mobile config
                filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
                filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                filter.addAction(TRANSACTION_START);
                filter.addAction(TRANSACTION_STOP);                

                mContext.registerReceiver(mIntentReceiver, filter);
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
	                mContext.getContentResolver().registerContentObserver(
	                        Settings.System.getUriFor( Settings.System.GPRS_CONNECTION_SIM_SETTING),
	                        true, mMobileStateChangeObserver);
                } else {
                	mContext.getContentResolver().registerContentObserver(
	                        Settings.Secure.getUriFor(Settings.Secure.MOBILE_DATA),
	                        true, mMobileStateForSingleCardChangeObserver);
                }
                
                // get notified of phone state changes
                TelephonyManager telephonyManager =
                        (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                	telephonyManager.listenGemini(mPhoneStateListener1, PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_1);
                    telephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_2);
                } else {
                    telephonyManager.listen(mPhoneStateListener1, PhoneStateListener.LISTEN_SERVICE_STATE);
                }  
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                	mContext.getContentResolver().unregisterContentObserver(mMobileStateChangeObserver);
                } else {
                	mContext.getContentResolver().unregisterContentObserver(mMobileStateForSingleCardChangeObserver);
                }
                TelephonyManager telephonyManager =
                        (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                	telephonyManager.listenGemini(mPhoneStateListener1, 0, Phone.GEMINI_SIM_1);
                    telephonyManager.listenGemini(mPhoneStateListener2, 0, Phone.GEMINI_SIM_2);
                } else {
                    telephonyManager.listen(mPhoneStateListener1, 0);
                } 
            }
        }
    }

    /**
     * Subclass of StateTracker to get/set Wifi state.
     */
    private final class WifiStateTracker extends StateTracker {
    	
    	private boolean mIsAirlineMode = false;
    	
    	public void setAirlineMode(boolean enable) {
    		if (DBG) {
    		    Xlog.i(TAG, "Mobile setAirlineMode called, enabled is: " + enable);
    		}
    		mIsAirlineMode = enable;
    	}
    	
    	public boolean isClickable() {
    	    Xlog.i(TAG, "wifi mIsAirlineMode is " + mIsAirlineMode + ", mIsUserSwitching is " + mIsUserSwitching);
            return !mIsAirlineMode && super.isClickable();
    	}
    	
        @Override
        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return STATE_DISABLED;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Xlog.d(TAG, "No wifiManager.");
                setCurrentState(context, STATE_DISABLED);
                return;
            }

            // Actually request the wifi change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    /**
                     * Disable tethering if enabling Wifi
                     */
                    // delete these statement, from zte73 we support tether and wifi both eanbled
                    /*int wifiApState = wifiManager.getWifiApState();
                    if (desiredState && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        wifiManager.setWifiApEnabled(null, false);
                    }*/
                    wifiManager.setWifiEnabled(desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            setCurrentState(context, wifiStateToFiveState(wifiState));
        }

        @Override
        public int getDisabledResource() {
            return R.drawable.zzz_wifi_off;
        }

        @Override
        public int getEnabledResource() {
        	return R.drawable.zzz_wifi_enable;
        }
        
        @Override
        public int getInterMedateResource() {
        	return R.drawable.zzz_stat_sys_wifi_switch_anim;
        }

        @Override
        public ImageView getImageButtonView() {
            return mWifiIcon.getConfigView();
        }

        /**
         * Converts WifiManager's state values into our Wifi/Bluetooth-common
         * state values.
         */
        private int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    return STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLED:
                    return STATE_ENABLED;
                case WifiManager.WIFI_STATE_DISABLING:
                    return STATE_TURNING_OFF;
                case WifiManager.WIFI_STATE_ENABLING:
                    return STATE_TURNING_ON;
                default:
                    return STATE_DISABLED;
            }
        }

		@Override
		public ImageView getIndicatorView() {
			// TODO Auto-generated method stub
			return mWifiIcon.getIndicatorView();
		}
		
		@Override
		public ImageView getSwitchingGifView() {
	    	return mWifiIcon.getSwitchingGifView();
	    }
    }

    /**
     * Subclass of StateTracker to get/set Bluetooth state.
     */
    private final class BluetoothStateTracker extends StateTracker {
        @Override
        public int getActualState(Context context) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
            	return STATE_DISABLED;
            }
            return bluetoothStateToFiveState(bluetoothAdapter.getState());
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                setCurrentState(context, STATE_DISABLED);
            	return;
            }
            // Actually request the Bluetooth change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    if (desiredState) {
                        bluetoothAdapter.enable();
                    } else {
                        bluetoothAdapter.disable();
                    }
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                return;
            }
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            setCurrentState(context, bluetoothStateToFiveState(bluetoothState));
        }

        /**
         * Converts BluetoothAdapter's state values into our
         * Wifi/Bluetooth-common state values.
         */
        private int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_OFF:
                    return STATE_DISABLED;
                case BluetoothAdapter.STATE_ON:
                    return STATE_ENABLED;
                case BluetoothAdapter.STATE_TURNING_ON:
                    return STATE_TURNING_ON;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return STATE_TURNING_OFF;
                default:
                    return STATE_UNKNOWN;
            }
        }

        public int getDisabledResource() {
        	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
            	return R.drawable.zzz_bluetooth_disable;
            }
            return R.drawable.zzz_bluetooth_off;
        }

        public int getEnabledResource() {
            return R.drawable.zzz_bluetooth_enable;
        }

        public ImageView getImageButtonView() {
            return mBluetoothIcon.getConfigView();
        }
        
        public int getInterMedateResource() {
        	return R.drawable.zzz_stat_sys_bt_switch_anim;
        }
        
        @Override
		public ImageView getIndicatorView() {
			// TODO Auto-generated method stub
			return mBluetoothIcon.getIndicatorView();
		}
        
        @Override
		public ImageView getSwitchingGifView() {
	    	return mBluetoothIcon.getSwitchingGifView();
	    }
    }

    /**
     * Subclass of StateTracker for GPS state.
     */
    private final class GpsStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            ContentResolver resolver = context.getContentResolver();
            boolean on = Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER);
            return on ? STATE_ENABLED : STATE_DISABLED;
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
                    Settings.Secure.setLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER, desiredState);
                    return desiredState;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    setCurrentState(context, result ? STATE_ENABLED : STATE_DISABLED);
                    setImageViewResources(context);
                }
            }.execute();
        }

        public int getDisabledResource() {
            return R.drawable.zzz_gps_off;
        }

        public int getEnabledResource() {
            return R.drawable.zzz_gps_enable;
        }

        public ImageView getImageButtonView() {
            return mGpsIcon.getConfigView();
        }
        
        @Override
        public ImageView getIndicatorView() {
            return mGpsIcon.getIndicatorView();
        }
    }
    
    /**
     * Subclass of StateTracker for Mobile state.
     */
    private final class MobileStateTracker extends StateTracker {
    	
    	private boolean mGprsTargSim = false;
    	private boolean mIsAirlineMode = false;
    	private boolean mHasSim = false;
    	private boolean mIsMmsOngoing = false;
    	
    	private boolean mIsDataDialogShown = false;
    	
    	public void setHasSim(boolean enable) {
    		mHasSim = enable;
    	}
    	
    	public void setAirlineMode(boolean enable) {
    		if (DBG) {
    		    Xlog.i(TAG, "Mobile setAirlineMode called, enabled is: " + enable);
    		}
    		mIsAirlineMode = enable;
    	}
    	
    	public void setIsMmsOngoing(boolean enable) {
    		mIsMmsOngoing = enable;
    	}
    	
    	public void setIsUserSwitching (boolean enable) {
    		mIsUserSwitching = enable;
    	}
    	
    	public boolean getIsUserSwitching () {
    		return mIsUserSwitching;
    	}
    	
    	public boolean IsDataDialogShown () {
    		return mIsDataDialogShown;
    	}
    	
    	public boolean isClickable() {
    	    Xlog.i(TAG, "mobile mHasSim is " + mHasSim + ", mIsAirlineMode is " + mIsAirlineMode + ", mIsMmsOngoing is " + mIsMmsOngoing + ", mIsUserSwitching is " + mIsUserSwitching);
    		if (mHasSim && !mIsAirlineMode && !mIsMmsOngoing && super.isClickable()) {
    			return true;
    		} else {
    			return false;
    		}
    	}

        @Override
        public int getActualState(Context context) {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		long simId = SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING);
    	    	if (DBG) {
    	    	    Xlog.i(TAG, "MobileStateTracker.getActualState called, simId is" + simId);
    	    	}
    	    	return ((simId > 0) && (!mIsAirlineMode) && (getEnabledResource() != -1))? STATE_ENABLED : STATE_DISABLED;
        	} else {
        		if (!mHasSim || mIsAirlineMode) {
        			return STATE_DISABLED;
        		}
        		ConnectivityManager cm =
                    (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        		if (cm != null) {
        			return cm.getMobileDataEnabled()?STATE_ENABLED:STATE_DISABLED;
        		} else {
        			return STATE_DISABLED;
        		}
        	}
        }
        
        @Override
        public void toggleState(Context context) {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		mIsDataDialogShown = true;
	        	postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mSwitchListview == null) {
							mSwitchListview = new SimIconsListView(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING);
							mSwitchListview.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									if (view != null && !view.isEnabled()) {
										return;
									}
									final SimItem simItem = (SimItem) parent.getItemAtPosition(position);
									if (simItem != null) {
										if ( simItem.mIsSim && simItem.mSimID == SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING)) {
											mSwitchDialog.dismiss();
											return;
										} else if (!simItem.mIsSim && SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING) == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
											mSwitchDialog.dismiss();
											return;
										}
										// pop up a warning dialog when the selected is not 3g one.
										if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
											int curSlotId = simItem.mSlot;
											try {
												ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
												int g3SlotId = iTelephony.get3GCapabilitySIM();
												Xlog.d(TAG, "show3GSwitchWarningDialog, g3SlotId=" + g3SlotId + " curSlotId=" + curSlotId + ".");
												if (curSlotId != -1 && curSlotId != g3SlotId) {
													// show disable 3g warning dialog.
													Builder builder = new AlertDialog.Builder(mContext);
													builder.setTitle(android.R.string.dialog_alert_title);
													builder.setIcon(android.R.drawable.ic_dialog_alert);
													if(IS_CU) {
													    builder.setMessage(getResources().getString(R.string.gemini_3g_disable_warning_cu));
													} else {
													    builder.setMessage(getResources().getString(R.string.gemini_3g_disable_warning));
													}
													builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int whichButton) {
															switchDataConnectionMode(simItem);
														}
													});
													builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int whichButton) {
															mSwitchDialog.dismiss();
														}
													});
													AlertDialog alertDlg = builder.create();
													
													alertDlg.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_SUB_PANEL);
													alertDlg.show();
												} else {
													switchDataConnectionMode(simItem);
												}
											} catch (RemoteException e) {
												return;
											}
										} else {
											switchDataConnectionMode(simItem);
										}
									} else {
									    Xlog.e(TAG, "MobileIcon clicked and clicked a null sim item");
										return;
									}
								}
						    });
						}
						ViewGroup parent = (ViewGroup)mSwitchListview.getParent();
						if (parent != null) {
							parent.removeView(mSwitchListview);
						}
						mSwitchDialog = createDialog(mSwitchListview, R.string.mobile);
			            mSwitchListview.initSimList();
			            mSwitchListview.notifyDataChange();
						mSwitchDialog.show();
						mIsDataDialogShown =false;
					}
				},  ViewConfiguration.getPressedStateDuration());
        	} else {
        		super.toggleState(context);
        	}
        }

		private void switchDataConnectionMode(SimItem simItem) {
			mMobileStateTracker.setIsUserSwitching(true);
			if (simItem.mIsSim) {
				mGprsTargSim = true;
				mDataTimerHandler.sendEmptyMessageDelayed(EVENT_ATTACH_TIME_OUT, ATTACH_TIME_OUT_LENGTH);
			} else {
				mGprsTargSim = false;
				mDataTimerHandler.sendEmptyMessageDelayed(EVENT_DETACH_TIME_OUT, DETACH_TIME_OUT_LENGTH);
			}
			mMobileStateTracker.getImageButtonView().setVisibility(View.GONE);
			mMobileStateTracker.getIndicatorView().setVisibility(View.GONE);
			int resId = mMobileStateTracker.getInterMedateResource();
			if (resId != -1) {
				mMobileStateTracker.getSwitchingGifView().setImageResource(resId);
				mMobileStateTracker.getSwitchingGifView().setVisibility(View.VISIBLE);
			}
			mMobileStateTracker.getImageButtonView().setEnabled(false);
			AnimationDrawable mFrameDrawable=(AnimationDrawable)getSwitchingGifView().getDrawable();
			if(mFrameDrawable!=null && !mFrameDrawable.isRunning()){
				mFrameDrawable.start();
			}
			
			Intent intent = new Intent();
			intent.putExtra(Phone.MULTI_SIM_ID_KEY, simItem.mSimID);
			intent.setAction(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
			mContext.sendBroadcast(intent);
			mSwitchDialog.dismiss();
		}

        @Override
        public void onActualStateChange(Context context, Intent intent) {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		mIsUserSwitching = false;
                setCurrentState(context, mobileStateToFiveState(intent));
        	} else {
        		int currentState = getActualState(context);
        		if(DBG) {
        		    Xlog.i(TAG, "single card onActualStateChange called, currentState is " +  currentState);
        		}
        		setCurrentState(context, currentState);
        	}
        }
        
		private int mobileStateToFiveState(Intent intent) {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				Phone.DataState state = getMobileDataState(intent);
				int simSlotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
                if(DBG) {
                    Xlog.i(TAG, "mobileStateToFiveState simSlotId is : " + simSlotId);
                    Xlog.i(TAG, "mobileStateToFiveState state is : " + state);
                }
				int currentState;
				if (state != null) {
					switch (state) {
					case CONNECTED:
						mDataTimerHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
						SIMInfo simInfo = SIMHelper.getSIMInfoBySlot(mContext, simSlotId);
						if (simInfo == null) {
						    Xlog.e(TAG, "MobileStateTracker mobileStateToFiveState error for simInfo, slotId is " + simSlotId);
							return STATE_UNKNOWN;
						}
						String name = simInfo.mDisplayName;
						String message = getResources().getString(
								R.string.data_connection_connected, name);
						mDataTimerHandler.obtainMessage(EVENT_DATA_CONNECTION_RESULT, message).sendToTarget();
						currentState = STATE_ENABLED;
						break;
					case DISCONNECTED:
						mDataTimerHandler.removeMessages(EVENT_DETACH_TIME_OUT);
						currentState = STATE_DISABLED;
						break;
					default:
						currentState = STATE_UNKNOWN;
					}
				} else {
					currentState = STATE_UNKNOWN;
				}
				return currentState;
        	} else {
        		return STATE_UNKNOWN;
        	}
		}

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		// do nothing
        	} else {
        		final ContentResolver resolver = context.getContentResolver();
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... args) {
                    	ConnectivityManager cm =
                            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    	boolean enabled = cm.getMobileDataEnabled();
                		cm.setMobileDataEnabled(!enabled);
                		return null;
                    }
                }.execute();
        	}
        }

        public int getDisabledResource() {
        	if (isAirplaneModeOn(mContext)) {
        		return R.drawable.zzz_mobile_disable;
        	} else {
        		return R.drawable.zzz_mobile_off;
        	}
        }

        public int getEnabledResource() {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
    	    	long simId = SIMHelper.getDefaultSIM(mContext, Settings.System.GPRS_CONNECTION_SIM_SETTING);
    	    	if (simId < 0) {
    	    	    Xlog.e(TAG, "Mobile StateTracker getEnabledResource error, selected simId is " + simId);
    	    		return -1;
    	    	} else if (simId == 0){
    	    		return getDisabledResource();
    	    	} else {
    		    	SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
    		    	if (simInfo == null) {
    		    	    Xlog.e(TAG, "Mobile StateTracker getEnabledResource error, selected simId is " + simId);
    		    		return -1;
    		    	}
    		    	int slotId = simInfo.mSlot;
    		    	mDataConnectionSimSlot = slotId;
    		    	return SIMHelper.getDataConnectionIconIdBySlotId(mContext, slotId);
    	    	}
        	} else {
        		//need implementation
        		return R.drawable.zzz_mobile_enable;
        	}
        }

        public ImageView getImageButtonView() {
            return mMobileIcon.getConfigView();
        }
        
        @Override
		public ImageView getIndicatorView() {
			// TODO Auto-generated method stub
			return mMobileIcon.getIndicatorView();
		}
        
        @Override
        public int getInterMedateResource() {
        	return R.drawable.zzz_stat_sys_mobile_switch_anim;
        }
        
        @Override
		public ImageView getSwitchingGifView() {
	    	return mMobileIcon.getSwitchingGifView();
	    }
    }

    /**
     * Subclass of StateTracker for GPS state.
     */
    private final class AirlineModeStateTracker extends StateTracker {
    	private boolean mAirPlaneModeClickable = true;
    	
    	public void setAirPlaneModeClickable(boolean enable) {
    		if (DBG) {
    		    Xlog.i(TAG, "setAirPlaneModeClickable called, enabled is: " + enable);
    		}
    		mAirPlaneModeClickable = enable;
    	}

        @Override
        public int getActualState(Context context) {
            return isAirplaneModeOn(mContext) ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
        	boolean enabled = intent.getBooleanExtra("state", false);
            setCurrentState(context, enabled ? STATE_ENABLED : STATE_DISABLED);
        }
        
        @Override
        public void toggleState(Context context) {
        	if (getIsUserSwitching()) {
        	    Xlog.i(TAG, "toggleState user is swithing, so just return");
        		return;
        	}
        	if (Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
                // Launch ECM exit dialog
                Intent ecmDialogIntent =
                        new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null);
                ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(ecmDialogIntent);
            } else {
            	boolean airlineMode = isAirplaneModeOn(mContext);
            	setIsUserSwitching(true);
            	getImageButtonView().setEnabled(isClickable());
            	Xlog.i(TAG, "Airplane toogleState: " + isClickable() + ", current airlineMode is " + airlineMode);
            	Settings.System.putInt(
                        mContext.getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON,
                        airlineMode ? 0 : 1);
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                intent.putExtra("state", !airlineMode);
                mContext.sendBroadcast(intent);
            }
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            // Do nothing, for we have done all operation in toggleState
        }

        public int getDisabledResource() {
            return R.drawable.zzz_flight_mode_off;
        }

        public int getEnabledResource() {
            return R.drawable.zzz_flight_mode_on;
        }

        public ImageView getImageButtonView() {
            return mAirlineModeIcon.getConfigView();
        }
        
        @Override
        public ImageView getIndicatorView() {
            return mAirlineModeIcon.getIndicatorView();
        }
        
        public boolean isClickable() {
    	    Xlog.i(TAG, "mAirPlaneModeClickable is " + mAirPlaneModeClickable + " super.isClickable is " + super.isClickable());
            return mAirPlaneModeClickable && super.isClickable();
    	}
        
    }

    @Override
    public void initConfigurationState() {
    	boolean isAirlineModeOn = isAirplaneModeOn(mContext);
    	if (FeatureOption.MTK_WLAN_SUPPORT) {
    		if (IS_CMCC) {
    			mWifiStateTracker.setAirlineMode(isAirlineModeOn);
    		}
    		mWifiStateTracker.setImageViewResources(mContext);
        }
        if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
        	mBluetoothStateTracker.setImageViewResources(mContext);
        }
        if (FeatureOption.MTK_GPS_SUPPORT) {
        	mGpsStateTracker.setImageViewResources(mContext);
        }
        mAirlineModeStateTracker.setImageViewResources(mContext);
        mMobileStateTracker.setAirlineMode(isAirlineModeOn);
        mMobileStateTracker.setHasSim(false);
        mMobileStateTracker.setCurrentState(mContext, StateTracker.STATE_DISABLED);
        mMobileStateTracker.setImageViewResources(mContext);
        
        mSimCardReady = SystemProperties.getBoolean(TelephonyProperties.PROPERTY_SIM_INFO_READY, false);
        if (mSimCardReady) {
            Xlog.i(TAG, "Oops, sim ready, maybe phone is drop down and restarted");
        	List<SIMInfo> simInfos = SIMHelper.getSIMInfoList(mContext);
            if (simInfos == null || simInfos.size() <= 0) {
            	mMobileStateTracker.setHasSim(false);
            } else {
            	mMobileStateTracker.setHasSim(true);
            }
            mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
            mMobileStateTracker.setImageViewResources(mContext);
        }
    }
    
    public void enlargeTouchRegion() {
    	if (FeatureOption.MTK_WLAN_SUPPORT) {
    		mWifiIcon.enlargeTouchRegion();
        }
        if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
        	mBluetoothIcon.enlargeTouchRegion();
        }
        if (FeatureOption.MTK_GPS_SUPPORT) {
        	mGpsIcon.enlargeTouchRegion();
        }
        mMobileIcon.enlargeTouchRegion();
        mAirlineModeIcon.enlargeTouchRegion();
    }
    
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
    
    public void updateResources(){
    	if (FeatureOption.MTK_WLAN_SUPPORT) {
    		mWifiIcon.setConfigName(R.string.wifi);
        }
        if (FeatureOption.MTK_BT_SUPPORT || FeatureOption.RDA_BT_SUPPORT) {
        	mBluetoothIcon.setConfigName(R.string.bluetooth);
        }
        if (FeatureOption.MTK_GPS_SUPPORT) {
        	mGpsIcon.setConfigName(R.string.gps);
        }
         mMobileIcon.setConfigName(R.string.mobile);
         mAirlineModeIcon.setConfigName(R.string.offline);
         
         if(mSwitchDialog!=null){
        	 mSwitchDialog.setTitle(R.string.mobile);
         }
         if(mSwitchListview!=null){
        	 mSwitchListview.updateResources();
         }
    }
}
