/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static android.net.NetworkPolicy.LIMIT_DISABLED;
import static android.net.NetworkPolicy.WARNING_DISABLED;
import static android.net.NetworkPolicyManager.EXTRA_NETWORK_TEMPLATE;
import static android.net.NetworkPolicyManager.POLICY_NONE;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkPolicyManager.computeLastCycleBoundary;
import static android.net.NetworkPolicyManager.computeNextCycleBoundary;
import static android.net.NetworkTemplate.MATCH_MOBILE_3G_LOWER;
import static android.net.NetworkTemplate.MATCH_MOBILE_4G;
import static android.net.NetworkTemplate.MATCH_MOBILE_ALL;
import static android.net.NetworkTemplate.MATCH_WIFI;
import static android.net.NetworkTemplate.buildTemplateEthernet;
import static android.net.NetworkTemplate.buildTemplateMobile3gLower;
import static android.net.NetworkTemplate.buildTemplateMobile4g;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifi;
import static android.net.NetworkTemplate.buildTemplateMobileAllGemini;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.Time.TIMEZONE_UTC;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.android.internal.util.Preconditions.checkNotNull;
import static com.android.settings.Utils.prepareCustomPreferencesList;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.Preference;
import android.provider.Settings;
import android.provider.Telephony.SimInfo;
import android.provider.Telephony.SIMInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;

import com.android.settings.drawable.InsetBoundsDrawable;
import com.android.settings.net.ChartData;
import com.android.settings.net.ChartDataLoader;
import com.android.settings.net.NetworkPolicyEditor;
import com.android.settings.net.SummaryForAllUidLoader;
import com.android.settings.net.UidDetail;
import com.android.settings.net.UidDetailProvider;
import com.android.settings.widget.ChartDataUsageView;
import com.android.settings.widget.ChartDataUsageView.DataUsageChartListener;
import com.android.settings.widget.PieChartView;
import com.google.android.collect.Lists;

import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.CellConnService.CellConnMgr;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import libcore.util.Objects;

/**
 * Panel show data usage history across various networks, including options to
 * inspect based on usage cycle and control through {@link NetworkPolicy}.
 */
public class DataUsageSummary extends Fragment {
    private static final String TAG = "DataUsage";
    private static final boolean LOGD = true;

    // TODO: remove this testing code
    private static final boolean TEST_ANIM = false;
    private static final boolean TEST_RADIOS = false;
    private static final String TEST_RADIOS_PROP = "test.radios";

    private static final String TAB_3G = "3g";
    private static final String TAB_4G = "4g";
    private static final String TAB_MOBILE = "mobile";
    private static final String TAB_WIFI = "wifi";
    private static final String TAB_ETHERNET = "ethernet";
    private static final String TAB_SIM_1 = "sim1";
    private static final String TAB_SIM_2 = "sim2";

    private static final String TAG_CONFIRM_DATA_DISABLE = "confirmDataDisable";
    private static final String TAG_CONFIRM_DATA_ROAMING = "confirmDataRoaming";
    private static final String TAG_CONFIRM_LIMIT = "confirmLimit";
    private static final String TAG_CYCLE_EDITOR = "cycleEditor";
    private static final String TAG_WARNING_EDITOR = "warningEditor";
    private static final String TAG_LIMIT_EDITOR = "limitEditor";
    private static final String TAG_CONFIRM_RESTRICT = "confirmRestrict";
    private static final String TAG_DENIED_RESTRICT = "deniedRestrict";
    private static final String TAG_CONFIRM_APP_RESTRICT = "confirmAppRestrict";
    private static final String TAG_APP_DETAILS = "appDetails";

    private static final int LOADER_CHART_DATA = 2;
    private static final int LOADER_SUMMARY = 3;

    private static final long KB_IN_BYTES = 1024;
    private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
    private static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
	//private static final int MAX_SIZE = 999*1024;
	private static final int LIMIT_MAX_SIZE = 999*1024;
	private static final int WARNING_MAX_SIZE = 900*1024;

    private INetworkManagementService mNetworkService;
    private INetworkStatsService mStatsService;
    private INetworkPolicyManager mPolicyService;
    private ConnectivityManager mConnService;

    private static final String PREF_FILE = "data_usage";
    private static final String PREF_SHOW_WIFI = "show_wifi";
    private static final String PREF_SHOW_ETHERNET = "show_ethernet";

    private SharedPreferences mPrefs;

    private TabHost mTabHost;
    private ViewGroup mTabsContainer;
    private TabWidget mTabWidget;
    private ListView mListView;
    private DataUsageAdapter mAdapter;

    /** Distance to inset content from sides, when needed. */
    private int mInsetSide = 0;

    private ViewGroup mHeader;

    private ViewGroup mNetworkSwitchesContainer;
    private LinearLayout mNetworkSwitches;
    private Switch mDataEnabled;
    private View mDataEnabledView;
    private CheckBox mDisableAtLimit;
    private View mDisableAtLimitView;

    private View mCycleView;
    private Spinner mCycleSpinner;
    private CycleAdapter mCycleAdapter;

    private ChartDataUsageView mChart;
    private TextView mUsageSummary;
    private TextView mEmpty;

    private View mAppDetail;
    private ImageView mAppIcon;
    private ViewGroup mAppTitles;
    private PieChartView mAppPieChart;
    private TextView mAppForeground;
    private TextView mAppBackground;
    private Button mAppSettings;

    private LinearLayout mAppSwitches;
    private CheckBox mAppRestrict;
    private View mAppRestrictView;

    private boolean mShowWifi = false;
    private boolean mShowEthernet = false;

    private NetworkTemplate mTemplate;
    private ChartData mChartData;

    private int[] mAppDetailUids = null;

    private Intent mAppSettingsIntent;

    private NetworkPolicyEditor mPolicyEditor;

    private String mCurrentTab = null;
    private String mIntentTab = null;

    private MenuItem mMenuDataRoaming;
    private MenuItem mMenuRestrictBackground;

    /** Flag used to ignore listeners during binding. */
    private boolean mBinding;

    private UidDetailProvider mUidDetailProvider;

    /** For Gemini phone */
  	private static final String ACTION_POLICYMGR_CREATED =
            "com.mediatek.server.action.ACTION_POLICY_CREATED";    
    List<SIMInfo> mSimList ;
    boolean mHaveSim1Tab = false;
    boolean mHaveSim2Tab = false;
    String  mSim1Name;
    String  mSim2Name;
    long    mSimId1;
    long    mSimId2;
    int mSimStatus1;
    int mSimStatus2;
    private MenuItem mMenuDataRoamingSim1;
    private MenuItem mMenuDataRoamingSim2;   
    private ITelephony mITelephony;
    private TelephonyManagerEx mTelephonyManager;
    private IntentFilter mIntentFilter;    
    private boolean mIsUserEnabled = false;
    private String mSavedCurrentTab = null;
    private CellConnMgr mCellConnMgr;
    private CycleAdapter mCycleAdapterSim1;
    private CycleAdapter mCycleAdapterSim2;
	private CycleAdapter mCycleAdapterOther;
	private static boolean mIsSwitching = false;
    //time out message event
    private static final int EVENT_DETACH_TIME_OUT = 2000;
    private static final int EVENT_ATTACH_TIME_OUT = 2001;
    //time out length
    private static final int DETACH_TIME_OUT_LENGTH = 10000;
    private static final int ATTACH_TIME_OUT_LENGTH = 30000;	
    //For single simCard Mode,whether there is a simCard inserted
    private boolean mHaveMobileSim = false;

	/**Fix me : this shall get from somewhere else.
	*/
    private static final int PIN1_REQUEST_CODE = 302;
    
    private static boolean sIsWifiOnly = false;
    /** M: identify whether data connection is open & close in dataUsage,CR ALPS00355823 */
    private boolean mIsUserEnabledNoneGemini = false;
    
    
    private ContentObserver	mGprsDefaultSIMObserver = new ContentObserver(new Handler()){
            @Override
            public void onChange(boolean selfChange) {
                Xlog.i(TAG, "Gprs connection SIM changed");
                mIsUserEnabled = false;
                mSavedCurrentTab =  mTabHost.getCurrentTabTag();
                updateGeminiSimStatus();       
                updateBody();      
            }
    	};

    private ContentObserver	mAirplaneObserver = new ContentObserver(new Handler()){
            @Override
            public void onChange(boolean selfChange) {
                Xlog.i(TAG, "airplane mode changed"); 
                updateBody();      
            }
    	};

    /**
     * M: add a ContentObserver to sync dataconnection status in dataUsage and title bar in
     *    none gemini mode,CR ALPS00355823
     */
    private ContentObserver mDataConnectionObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                if (mIsUserEnabledNoneGemini) {
                   //if is user enable & disable data connection in dataUsage,do not update UI.
                   mIsUserEnabledNoneGemini = false;
                } else {
                   Xlog.i(TAG, "Data connection state changed(none gemini mode)");
                   mMobileDataEnabled = mConnService.getMobileDataEnabled();
                   updatePolicy(false);
                }
            }
        };

    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            String action = intent.getAction();
            boolean needUpdate = false;
            
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
            	
                int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                int simStatus = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
                Xlog.i(TAG, "receive notification: state of sim slot "+ slotId + " is "+simStatus);
                if ((slotId>=0)&&(simStatus>=0)) {
                	needUpdate = true;
                }
            	
            }else if(action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
               needUpdate = true;
            }else if(action.equals(ACTION_POLICYMGR_CREATED)){
                Xlog.d(TAG,"receive new policy");
                mPolicyEditor.read();
                if (mCycleAdapter != null) {
                    updatePolicy(true); 
                }
          
            }else if(action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)){
		String reason = intent.getStringExtra(Phone.STATE_CHANGE_REASON_KEY);
		String apnTypeList = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
		Phone.DataState state;
		String str = intent.getStringExtra(Phone.STATE_KEY);
		
		if (str != null) {
		    state = Enum.valueOf(Phone.DataState.class, str);
		} else {
		    state = Phone.DataState.DISCONNECTED;
		}

		int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
		
		Xlog.i(TAG, "DataConnectionReceiver simId is : " + simId);
		Xlog.i(TAG, "DataConnectionReceiver state is : " + state);
		Xlog.i(TAG, "DataConnectionReceiver reason is : " + reason);
		Xlog.i(TAG, "DataConnectionReceiver apn type is : " + apnTypeList);
                Xlog.i(TAG, "DataConnectionReceiver phone state : " + str);				   
		if (reason == null || (!Phone.APN_TYPE_DEFAULT.equals(apnTypeList))){
			return;
		}				
		if (reason.equals(Phone.REASON_DATA_ENABLED)&&(state == Phone.DataState.CONNECTED)&&(mIsSwitching == true)) {
		        timerHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
			mIsSwitching = false;
			mDataEnabled.setEnabled(true);
			if (mCycleAdapter != null) {
		            updatePolicy(true); 
		         } 
		    Xlog.d(TAG,"attach over");
		}
		if (reason.equals(Phone.REASON_DATA_DETACHED)&&(state == Phone.DataState.DISCONNECTED)&&(mIsSwitching == true)){
		        timerHandler.removeMessages(EVENT_DETACH_TIME_OUT);
		        mIsSwitching = false;
		        mDataEnabled.setEnabled(true);
		        if (mCycleAdapter != null) {
                           updatePolicy(true); 
                         }
		        Xlog.d(TAG,"detach over");
		}									
            }
            if(needUpdate){
                mSavedCurrentTab =  mTabHost.getCurrentTabTag();
                Xlog.d(TAG,"mSavedCurrentTab " + mSavedCurrentTab + " ");
                updateGeminiSimStatus();       
                updateBody();        	
            }
        }
	};
	
	private void updateGeminiSimStatus(){
        mSimList = SIMInfo.getInsertedSIMList(getActivity());
        mHaveSim1Tab = false;
        mHaveSim2Tab = false;
        for(SIMInfo info : mSimList){
            Xlog.d(TAG,"sim info slot: " + info.mSlot + " display name : " + info.mDisplayName + " sim id " + info.mSimId);
            if( info.mSlot == Phone.GEMINI_SIM_1){
                mHaveSim1Tab = true;
                mSimId1 = info.mSimId;
                mSimStatus1 = mTelephonyManager.getSimIndicatorStateGemini(info.mSlot);
                Xlog.d(TAG,"mSimStatus1 " + mSimStatus1);
            }else if(info.mSlot == Phone.GEMINI_SIM_2){
                mHaveSim2Tab = true;
                mSimId2 = info.mSimId;
                mSimStatus2 = mTelephonyManager.getSimIndicatorStateGemini(info.mSlot);
                Xlog.d(TAG,"mSimStatus2 " + mSimStatus2);
            }                
        }               	    
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	int orientation = getActivity().getResources().getConfiguration().orientation;
    	int win_orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;;
    	Xlog.i(TAG,"current config orienation " + orientation);
    	if(orientation == Configuration.ORIENTATION_LANDSCAPE){
    		win_orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    	}
        getActivity().setRequestedOrientation(win_orientation);
        super.onCreate(savedInstanceState);

        mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        mStatsService = INetworkStatsService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mPolicyService = INetworkPolicyManager.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_POLICY_SERVICE));
        mConnService = (ConnectivityManager) getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);

        mPrefs = getActivity().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
       	mTelephonyManager = TelephonyManagerEx.getDefault();        
        if(FeatureOption.MTK_GEMINI_SUPPORT){

            updateGeminiSimStatus();
            mITelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));            
            mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
            mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
            mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_NAME_UPDATE);
            mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mIntentFilter.addAction(ACTION_POLICYMGR_CREATED);
			mIntentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);		   

        }else{
        	
        	 try{
            	 mITelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone")); 
            	 mHaveMobileSim = mITelephony.isSimInsert(Phone.GEMINI_SIM_1);
             }catch(RemoteException e){
                 Xlog.i(TAG, "RemoteException happens......");
             }
        }
        
        mCellConnMgr = new CellConnMgr(null);
        mCellConnMgr.register(getActivity());
        mPolicyEditor = new NetworkPolicyEditor(mPolicyService);
        mPolicyEditor.read();

        mShowWifi = mPrefs.getBoolean(PREF_SHOW_WIFI, false);
        mShowEthernet = mPrefs.getBoolean(PREF_SHOW_ETHERNET, false);

        setHasOptionsMenu(true);

        if(Utils.isWifiOnly(getActivity())){
                sIsWifiOnly = true;
        }	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Xlog.d(TAG,"onDestoryView");
        final Context context = inflater.getContext();
        final View view = inflater.inflate(R.layout.data_usage_summary, container, false);

        mUidDetailProvider = new UidDetailProvider(context);

        mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        mTabsContainer = (ViewGroup) view.findViewById(R.id.tabs_container);
        mTabWidget = (TabWidget) view.findViewById(android.R.id.tabs);
        mListView = (ListView) view.findViewById(android.R.id.list);

        // decide if we need to manually inset our content, or if we should rely
        // on parent container for inset.
        final boolean shouldInset = mListView.getScrollBarStyle()
                == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (shouldInset) {
            mInsetSide = view.getResources().getDimensionPixelOffset(
                    com.android.internal.R.dimen.preference_fragment_padding_side);
        } else {
            mInsetSide = 0;
        }

        // adjust padding around tabwidget as needed
        prepareCustomPreferencesList(container, view, mListView, true);

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(mTabListener);

        mHeader = (ViewGroup) inflater.inflate(R.layout.data_usage_header, mListView, false);
        mHeader.setClickable(true);

        mListView.addHeaderView(mHeader, null, true);
        mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
        if (mInsetSide > 0) {
            // inset selector and divider drawables
            insetListViewDrawables(mListView, mInsetSide);
            mHeader.setPadding(mInsetSide, 0, mInsetSide, 0);
        }

        {
            // bind network switches
            mNetworkSwitchesContainer = (ViewGroup) mHeader.findViewById(
                    R.id.network_switches_container);
            mNetworkSwitches = (LinearLayout) mHeader.findViewById(R.id.network_switches);

            
            mDataEnabled = new Switch(inflater.getContext());
            mDataEnabledView = inflatePreference(inflater, mNetworkSwitches, mDataEnabled);
            mDataEnabled.setOnCheckedChangeListener(mDataEnabledListener);
            mNetworkSwitches.addView(mDataEnabledView);

            mDisableAtLimit = new CheckBox(inflater.getContext());
            mDisableAtLimit.setClickable(false);
            mDisableAtLimit.setFocusable(false);
            mDisableAtLimitView = inflatePreference(inflater, mNetworkSwitches, mDisableAtLimit);
            
            mDisableAtLimitView.setClickable(true);
            mDisableAtLimitView.setFocusable(true);
            mDisableAtLimitView.setOnClickListener(mDisableAtLimitListener);
            mNetworkSwitches.addView(mDisableAtLimitView);
        }

        // bind cycle dropdown
        mCycleView = mHeader.findViewById(R.id.cycles);
        mCycleSpinner = (Spinner) mCycleView.findViewById(R.id.cycles_spinner);
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            mCycleAdapterSim1 = new CycleAdapter(context);
            mCycleAdapterSim2 = new CycleAdapter(context);
			mCycleAdapterOther = new CycleAdapter(context); 
        }else{    
            mCycleAdapter = new CycleAdapter(context);
        }    
        if(!FeatureOption.MTK_GEMINI_SUPPORT){
            mCycleSpinner.setAdapter(mCycleAdapter);
        }
        mCycleSpinner.setOnItemSelectedListener(mCycleListener);

        mChart = (ChartDataUsageView) mHeader.findViewById(R.id.chart);
        mChart.setListener(mChartListener);
        mChart.bindNetworkPolicy(null);

        {
            // bind app detail controls
            mAppDetail = mHeader.findViewById(R.id.app_detail);
            mAppIcon = (ImageView) mAppDetail.findViewById(R.id.app_icon);
            mAppTitles = (ViewGroup) mAppDetail.findViewById(R.id.app_titles);
            mAppPieChart = (PieChartView) mAppDetail.findViewById(R.id.app_pie_chart);
            mAppForeground = (TextView) mAppDetail.findViewById(R.id.app_foreground);
            mAppBackground = (TextView) mAppDetail.findViewById(R.id.app_background);
            mAppSwitches = (LinearLayout) mAppDetail.findViewById(R.id.app_switches);

            mAppSettings = (Button) mAppDetail.findViewById(R.id.app_settings);
            mAppSettings.setOnClickListener(mAppSettingsListener);

            mAppRestrict = new CheckBox(inflater.getContext());
            mAppRestrict.setClickable(false);
            mAppRestrict.setFocusable(false);
            mAppRestrictView = inflatePreference(inflater, mAppSwitches, mAppRestrict);
            mAppRestrictView.setClickable(true);
            mAppRestrictView.setFocusable(true);
            mAppRestrictView.setOnClickListener(mAppRestrictListener);
            mAppSwitches.addView(mAppRestrictView);
        }

        mUsageSummary = (TextView) mHeader.findViewById(R.id.usage_summary);
        mEmpty = (TextView) mHeader.findViewById(android.R.id.empty);

        // only assign layout transitions once first layout is finished
        mListView.getViewTreeObserver().addOnGlobalLayoutListener(mFirstLayoutListener);

        mAdapter = new DataUsageAdapter(mUidDetailProvider, mInsetSide);
        mListView.setOnItemClickListener(mListListener);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
    	Xlog.d(TAG,"onResume");
        super.onResume();
        mIsUserEnabled = false;
        // pick default tab based on incoming intent
        final Intent intent = getActivity().getIntent();
        mIntentTab = computeTabFromIntent(intent);

        // this kicks off chain reaction which creates tabs, binds the body to
        // selected network, and binds chart, cycles and detail list.
        updateTabs();

        // kick off background task to update stats
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // wait a few seconds before kicking off
                    Thread.sleep(2 * DateUtils.SECOND_IN_MILLIS);
                    mStatsService.forceUpdate();
                } catch (InterruptedException e) {
                } catch (RemoteException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (isAdded()) {
                    updateBody();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            getActivity().getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.GPRS_CONNECTION_SIM_SETTING),
	                    false, mGprsDefaultSIMObserver);
			getActivity().registerReceiver(mSimReceiver, mIntentFilter);            			
        }else{
            getActivity().getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.AIRPLANE_MODE_ON),
	                    false, mAirplaneObserver);
            /** M: Register ContentObserver to observe data connection status change,CR ALPS00355823 */
            getActivity().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.MOBILE_DATA),
                        false, mDataConnectionObserver);        
        }
    }
    @Override
    public void onPause(){
       	Xlog.d(TAG,"onPause");
        super.onPause();
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            getActivity().getContentResolver().unregisterContentObserver(mGprsDefaultSIMObserver);
			mSavedCurrentTab =  mTabHost.getCurrentTabTag();		             
            getActivity().unregisterReceiver(mSimReceiver);

        }else{
            getActivity().getContentResolver().unregisterContentObserver(mAirplaneObserver);
            /** M: UnRegister the mDataConnectionObserver ContentObserver,CR ALPS00355823 */
            getActivity().getContentResolver().unregisterContentObserver(mDataConnectionObserver);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_usage, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final Context context = getActivity();
        final boolean appDetailMode = isAppDetailMode();
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            menu.findItem(R.id.data_usage_menu_roaming).setVisible(false);
            if(mHaveSim1Tab){
                mMenuDataRoamingSim1 = menu.findItem(R.id.data_usage_menu_roaming_sim1);
                mMenuDataRoamingSim1.setTitle(getString(R.string.data_usage_menu_roaming)+" " + mSim1Name);
                mMenuDataRoamingSim1.setVisible(!appDetailMode);
                if(mSimStatus1 == Phone.SIM_INDICATOR_RADIOOFF){
                    mMenuDataRoamingSim1.setEnabled(false);
                }else{
                    mMenuDataRoamingSim1.setChecked(getDataRoaming(Phone.GEMINI_SIM_1));
                }    
            }else{
                menu.findItem(R.id.data_usage_menu_roaming_sim1).setVisible(false);
            }
            if(mHaveSim2Tab){
                mMenuDataRoamingSim2 = menu.findItem(R.id.data_usage_menu_roaming_sim2);
                mMenuDataRoamingSim2.setTitle(getString(R.string.data_usage_menu_roaming)+" " + mSim2Name);
                mMenuDataRoamingSim2.setVisible(!appDetailMode);
                if(mSimStatus2 == Phone.SIM_INDICATOR_RADIOOFF){
                    mMenuDataRoamingSim2.setEnabled(false);
                }else{
                    mMenuDataRoamingSim2.setChecked(getDataRoaming(Phone.GEMINI_SIM_2));
                }    
            }else{
                menu.findItem(R.id.data_usage_menu_roaming_sim2).setVisible(false);
            }
        }else{
            menu.findItem(R.id.data_usage_menu_roaming_sim1).setVisible(false);
            menu.findItem(R.id.data_usage_menu_roaming_sim2).setVisible(false);
            if( mHaveMobileSim ){
            	mMenuDataRoaming = menu.findItem(R.id.data_usage_menu_roaming);
                mMenuDataRoaming.setVisible(hasMobileRadio(context) && !appDetailMode);
                mMenuDataRoaming.setChecked(getDataRoaming());
            }else{
            	menu.findItem(R.id.data_usage_menu_roaming).setVisible(false);
            }
        }
            mMenuRestrictBackground = menu.findItem(R.id.data_usage_menu_restrict_background);
        if(!sIsWifiOnly){
	    mMenuRestrictBackground.setVisible(!appDetailMode);
            mMenuRestrictBackground.setChecked(getRestrictBackground());
	   } else {
	         mMenuRestrictBackground.setVisible(false);
	    }

        final MenuItem split4g = menu.findItem(R.id.data_usage_menu_split_4g);
        split4g.setVisible(hasMobile4gRadio(context) && !appDetailMode);
        split4g.setChecked(isMobilePolicySplit());

        final MenuItem showWifi = menu.findItem(R.id.data_usage_menu_show_wifi);
        if (hasWifiRadio(context) && hasMobileRadio(context)) {
            showWifi.setVisible(!appDetailMode);
            showWifi.setChecked(mShowWifi);
        } else {
            showWifi.setVisible(false);
            mShowWifi = true;
        }

        final MenuItem showEthernet = menu.findItem(R.id.data_usage_menu_show_ethernet);
        if (hasEthernet(context) && hasMobileRadio(context)) {
            showEthernet.setVisible(!appDetailMode);
            showEthernet.setChecked(mShowEthernet);
        } else {
            showEthernet.setVisible(false);
            mShowEthernet = true;
       	}
    	
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
            case R.id.data_usage_menu_roaming_sim1: 
            case R.id.data_usage_menu_roaming_sim2: {
                final boolean dataRoaming = !item.isChecked();
                int simSlot = 
                    item.getItemId() == R.id.data_usage_menu_roaming_sim1?Phone.GEMINI_SIM_1:Phone.GEMINI_SIM_2;
                if (dataRoaming) {
                    ConfirmDataRoamingFragment.show(this,simSlot);
                } else {
                    // no confirmation to disable roaming
                    setDataRoaming(simSlot,false);
                }
                return true;
            }
            
            case R.id.data_usage_menu_roaming: {
                final boolean dataRoaming = !item.isChecked();
                if (dataRoaming) {
                    ConfirmDataRoamingFragment.show(this);
                } else {
                    // no confirmation to disable roaming
                    setDataRoaming(false);
                }
                return true;
            }
            case R.id.data_usage_menu_restrict_background: {
                final boolean restrictBackground = !item.isChecked();
                if (restrictBackground) {
                    if (hasLimitedNetworks()) {
                        ConfirmRestrictFragment.show(this);
                    } else {
                        DeniedRestrictFragment.show(this);
                    }
                } else {
                    // no confirmation to drop restriction
                    setRestrictBackground(false);
                }
                return true;
            }
            case R.id.data_usage_menu_split_4g: {
                final boolean mobileSplit = !item.isChecked();
                setMobilePolicySplit(mobileSplit);
                item.setChecked(isMobilePolicySplit());
                updateTabs();
                return true;
            }
            case R.id.data_usage_menu_show_wifi: {
                mShowWifi = !item.isChecked();
                mPrefs.edit().putBoolean(PREF_SHOW_WIFI, mShowWifi).apply();
                item.setChecked(mShowWifi);
                updateTabs();
                return true;
            }
            case R.id.data_usage_menu_show_ethernet: {
                mShowEthernet = !item.isChecked();
                mPrefs.edit().putBoolean(PREF_SHOW_ETHERNET, mShowEthernet).apply();
                item.setChecked(mShowEthernet);
                updateTabs();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
    	Xlog.d(TAG,"onDestoryView");
        super.onDestroyView();

        mDataEnabledView = null;
        mDisableAtLimitView = null;

        mUidDetailProvider.clearCache();
        mUidDetailProvider = null;
    }

    @Override
    public void onDestroy() {
        Xlog.d(TAG,"onDestory");
        if (this.isRemoving()) {
            getFragmentManager()
                    .popBackStack(TAG_APP_DETAILS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        mCellConnMgr.unregister();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onDestroy();
    }

    /**
     * Listener to setup {@link LayoutTransition} after first layout pass.
     */
    private OnGlobalLayoutListener mFirstLayoutListener = new OnGlobalLayoutListener() {
        /** {@inheritDoc} */
        public void onGlobalLayout() {
            mListView.getViewTreeObserver().removeGlobalOnLayoutListener(mFirstLayoutListener);

            mTabsContainer.setLayoutTransition(buildLayoutTransition());
            mHeader.setLayoutTransition(buildLayoutTransition());
            mNetworkSwitchesContainer.setLayoutTransition(buildLayoutTransition());

            final LayoutTransition chartTransition = buildLayoutTransition();
            chartTransition.setStartDelay(LayoutTransition.APPEARING, 0);
            chartTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
            chartTransition.setAnimator(LayoutTransition.APPEARING, null);
            chartTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
            mChart.setLayoutTransition(chartTransition);
        }
    };

    private static LayoutTransition buildLayoutTransition() {
        final LayoutTransition transition = new LayoutTransition();
        if (TEST_ANIM) {
            transition.setDuration(1500);
        }
        transition.setAnimateParentHierarchy(false);
        return transition;
    }

    /**
     * Rebuild all tabs based on {@link NetworkPolicyEditor} and
     * {@link #mShowWifi}, hiding the tabs entirely when applicable. Selects
     * first tab, and kicks off a full rebind of body contents.
     */
    private void updateTabs() {
        final Context context = getActivity();
        mTabHost.clearAllTabs();
        Xlog.d(TAG,"clear All Tabs...");

        final boolean mobileSplit = isMobilePolicySplit();
        if (mobileSplit && hasMobile4gRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_3G, R.string.data_usage_tab_3g));
            mTabHost.addTab(buildTabSpec(TAB_4G, R.string.data_usage_tab_4g));
        }else if(FeatureOption.MTK_GEMINI_SUPPORT){
            if(mHaveSim1Tab){
                SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(),Phone.GEMINI_SIM_1);
                if(siminfo != null){
                    mTabHost.addTab(buildTabSpec(TAB_SIM_1, siminfo.mDisplayName));
                    mSim1Name = siminfo.mDisplayName;
                }    
            }
            if(mHaveSim2Tab){
                SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(),Phone.GEMINI_SIM_2);
                if(siminfo != null){
                    mTabHost.addTab(buildTabSpec(TAB_SIM_2, siminfo.mDisplayName));
                    mSim2Name = siminfo.mDisplayName;
                }
            }      
        }else if (mHaveMobileSim && hasMobileRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_MOBILE, R.string.data_usage_tab_mobile));
        }
        if (mShowWifi && hasWifiRadio(context)) {
            mTabHost.addTab(buildTabSpec(TAB_WIFI, R.string.data_usage_tab_wifi));
        }
        if (mShowEthernet && hasEthernet(context)) {
            mTabHost.addTab(buildTabSpec(TAB_ETHERNET, R.string.data_usage_tab_ethernet));
        }

        final boolean multipleTabs = mTabWidget.getTabCount() > 1;
        mTabWidget.setVisibility(multipleTabs ? View.VISIBLE : View.GONE);
        if (mIntentTab != null) {
            Xlog.d(TAG,"Intent tab "  + mIntentTab + "   ");
            if (Objects.equal(mIntentTab, mTabHost.getCurrentTabTag())) {
                // already hit updateBody() when added; ignore
                updateBody();
            } else {
                Xlog.d(TAG,"set Intent tab " );
                mTabHost.setCurrentTabByTag(mIntentTab);
            }
            mIntentTab = null;
        } else if(mSavedCurrentTab != null){
            Xlog.d(TAG,"saved curernt tabs " + mSavedCurrentTab + " ");
            if(!Objects.equal(mSavedCurrentTab, mTabHost.getCurrentTabTag())){
                mTabHost.setCurrentTabByTag(mSavedCurrentTab);         
            }    
            mSavedCurrentTab = null;
            updateBody();            
        }else {
            // already hit updateBody() when added; ignore
            if(mTabWidget.getTabCount() == 0){
                updateBody();
            }
        }
    }

    /**
     * Factory that provide empty {@link View} to make {@link TabHost} happy.
     */
    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        /** {@inheritDoc} */
        public View createTabContent(String tag) {
            return new View(mTabHost.getContext());
        }
    };

    /**
     * Build {@link TabSpec} with thin indicator, and empty content.
     */
    private TabSpec buildTabSpec(String tag, int titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(getText(titleRes)).setContent(
                mEmptyTabContent);
    }

    private TabSpec buildTabSpec(String tag, String title) {
        return mTabHost.newTabSpec(tag).setIndicator(title).setContent(mEmptyTabContent);
    }

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        /** {@inheritDoc} */
        public void onTabChanged(String tabId) {
            // user changed tab; update body
            mIsUserEnabled = false;
            updateBody();
        }
    };

    /**
     * Update body content based on current tab. Loads
     * {@link NetworkStatsHistory} and {@link NetworkPolicy} from system, and
     * binds them to visible controls.
     */
    private void updateBody() {
        mBinding = true;
        if (!isAdded()) return;

        final Context context = getActivity();
        final String currentTab = mTabHost.getCurrentTabTag();

        if (currentTab == null) {
            Log.w(TAG, "no tab selected; hiding body");
            mListView.setVisibility(View.GONE);
            return;
        } else {
            mListView.setVisibility(View.VISIBLE);
        }

        final boolean tabChanged = !currentTab.equals(mCurrentTab);
        mCurrentTab = currentTab;

        if (LOGD) Log.d(TAG, "updateBody() with currentTab= " + currentTab + " ");

		Xlog.d(TAG,"updateBody " + mIsSwitching);
        mDataEnabledView.setVisibility(View.VISIBLE);
		if(FeatureOption.MTK_GEMINI_SUPPORT){
			mCycleAdapter= mCycleAdapterOther;	
		}
        if (TAB_SIM_1.equals(currentTab)) {
            if(mSimStatus1 == Phone.SIM_INDICATOR_RADIOOFF){
                //Sim radio off , cannot set data connection for it.
                mDataEnabledView.setVisibility(View.GONE);
                mDisableAtLimitView.setVisibility(View.GONE);
                //mDataEnabledView.setEnabled(false);
                //mDisableAtLimitView.setEnabled(false);
                Xlog.d(TAG,"disable sim 1 enable because radio off");
            }
            else{
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            }
			
			mDataEnabled.setEnabled(!mIsSwitching);
			
            mCycleAdapter= mCycleAdapterSim1;
            mTemplate = buildTemplateMobileAllGemini(getSubscriberId(context,Phone.GEMINI_SIM_1),mSimId1);

        }else if (TAB_SIM_2.equals(currentTab)) {
            if(mSimStatus2 == Phone.SIM_INDICATOR_RADIOOFF){
                //Sim radio off , cannot set data connection for it.
                mDataEnabledView.setVisibility(View.GONE);
                mDisableAtLimitView.setVisibility(View.GONE);
                Xlog.d(TAG,"disable sim 2 enable because radio off");
                //mDataEnabledView.setEnabled(false);
                //mDisableAtLimitView.setEnabled(false);
            }
            else{
                setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);
                setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);
            }
			mDataEnabled.setEnabled(!mIsSwitching);
            mCycleAdapter= mCycleAdapterSim2;
            mTemplate = buildTemplateMobileAllGemini(getSubscriberId(context,Phone.GEMINI_SIM_2),mSimId2);

        }else if (TAB_MOBILE.equals(currentTab)) {
	        setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_mobile_limit);			
			if(isAirplaneModeOn(getActivity())){
                mDataEnabledView.setVisibility(View.GONE);
                mDisableAtLimitView.setVisibility(View.GONE);
			}else{
				setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_mobile);	
			}
            mTemplate = buildTemplateMobileAll(getActiveSubscriberId(context));

        } else if (TAB_3G.equals(currentTab)) {
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_3g);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_3g_limit);
            // TODO: bind mDataEnabled to 3G radio state
            mTemplate = buildTemplateMobile3gLower(getActiveSubscriberId(context));

        } else if (TAB_4G.equals(currentTab)) {
            setPreferenceTitle(mDataEnabledView, R.string.data_usage_enable_4g);
            setPreferenceTitle(mDisableAtLimitView, R.string.data_usage_disable_4g_limit);
            // TODO: bind mDataEnabled to 4G radio state
            mTemplate = buildTemplateMobile4g(getActiveSubscriberId(context));

        } else if (TAB_WIFI.equals(currentTab)) {
            // wifi doesn't have any controls
            mDataEnabledView.setVisibility(View.GONE);
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateWifi();

        } else if (TAB_ETHERNET.equals(currentTab)) {
            // ethernet doesn't have any controls
            mDataEnabledView.setVisibility(View.GONE);
            mDisableAtLimitView.setVisibility(View.GONE);
            mTemplate = buildTemplateEthernet();

        } else {
            throw new IllegalStateException("unknown tab: " + currentTab);
        }
		mCycleSpinner.setAdapter(mCycleAdapter);
        // kick off loader for network history
        // TODO: consider chaining two loaders together instead of reloading
        // network history when showing app detail.
        getLoaderManager().restartLoader(LOADER_CHART_DATA,
                ChartDataLoader.buildArgs(mTemplate, mAppDetailUids), mChartDataCallbacks);

        // detail mode can change visible menus, invalidate
        getActivity().invalidateOptionsMenu();

        mBinding = false;
    }

    public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),  
			Settings.System.AIRPLANE_MODE_ON, 0) != 0; 
    }


    private boolean isAppDetailMode() {
        return mAppDetailUids != null;
    }

    private int getAppDetailPrimaryUid() {
        return mAppDetailUids[0];
    }

    /**
     * Update UID details panels to match {@link #mAppDetailUids}, showing or
     * hiding them depending on {@link #isAppDetailMode()}.
     */
    private void updateAppDetail() {
        Xlog.d(TAG,"updateAppDetail");
        final Context context = getActivity();
        final PackageManager pm = context.getPackageManager();
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        if (isAppDetailMode()) {
            mAppDetail.setVisibility(View.VISIBLE);
            mCycleAdapter.setChangeVisible(false);
        } else {
            mAppDetail.setVisibility(View.GONE);
            mCycleAdapter.setChangeVisible(true);

            // hide detail stats when not in detail mode
            mChart.bindDetailNetworkStats(null);
            return;
        }

        // remove warning/limit sweeps while in detail mode
        mChart.bindNetworkPolicy(null);

        // show icon and all labels appearing under this app
        final int primaryUid = getAppDetailPrimaryUid();
        final UidDetail detail = mUidDetailProvider.getUidDetail(primaryUid, true);
        mAppIcon.setImageDrawable(detail.icon);

        mAppTitles.removeAllViews();
        if (detail.detailLabels != null) {
            for (CharSequence label : detail.detailLabels) {
                mAppTitles.addView(inflateAppTitle(inflater, mAppTitles, label));
            }
        } else {
            mAppTitles.addView(inflateAppTitle(inflater, mAppTitles, detail.label));
        }

        // enable settings button when package provides it
        // TODO: target torwards entire UID instead of just first package
        final String[] packageNames = pm.getPackagesForUid(primaryUid);
        if (packageNames != null && packageNames.length > 0) {
            mAppSettingsIntent = new Intent(Intent.ACTION_MANAGE_NETWORK_USAGE);
            mAppSettingsIntent.setPackage(packageNames[0]);
            mAppSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);

            final boolean matchFound = pm.resolveActivity(mAppSettingsIntent, 0) != null;
            mAppSettings.setEnabled(matchFound);

        } else {
            mAppSettingsIntent = null;
            mAppSettings.setEnabled(false);
        }

        updateDetailData();

        if (NetworkPolicyManager.isUidValidForPolicy(context, primaryUid)
                && !getRestrictBackground() && isBandwidthControlEnabled()) {
            setPreferenceTitle(mAppRestrictView, R.string.data_usage_app_restrict_background);
            if (hasLimitedNetworks()) {
                setPreferenceSummary(mAppRestrictView,
                        getString(R.string.data_usage_app_restrict_background_summary));
            } else {
                setPreferenceSummary(mAppRestrictView,
                        getString(R.string.data_usage_app_restrict_background_summary_disabled));
            }

            mAppRestrictView.setVisibility(View.VISIBLE);
            mAppRestrict.setChecked(getAppRestrictBackground());

        } else {
            mAppRestrictView.setVisibility(View.GONE);
        }
        Xlog.d(TAG,"updateAppDetail done");
    }

    private void setPolicyWarningBytes(long warningBytes) {
        if (LOGD) Log.d(TAG, "setPolicyWarningBytes()");
        mPolicyEditor.setPolicyWarningBytes(mTemplate, warningBytes);
        updatePolicy(false);
    }

    private void setPolicyLimitBytes(long limitBytes) {
        if (LOGD) Log.d(TAG, "setPolicyLimitBytes()");
        long warningBytes = mPolicyEditor.getPolicyWarningBytes(mTemplate);
       
        if( limitBytes != LIMIT_DISABLED &&  warningBytes > limitBytes ){
        	if(warningBytes < 995 * GB_IN_BYTES){
        		
        		limitBytes = warningBytes + GB_IN_BYTES * 3 ;
        	}else{
        		
        		limitBytes = warningBytes;
        	}
        }
     
        mPolicyEditor.setPolicyLimitBytes(mTemplate, limitBytes);
        updatePolicy(false);
    }

    /**
     * Local cache of value, used to work around delay when
     * {@link ConnectivityManager#setMobileDataEnabled(boolean)} is async.
     */
    private Boolean mMobileDataEnabled;

    private boolean isMobileDataEnabled() {
        if (mMobileDataEnabled != null) {
            // TODO: deprecate and remove this once enabled flag is on policy
            return mMobileDataEnabled;
        } else {
            return mConnService.getMobileDataEnabled();
        }
    }

    private boolean isMobileDataEnabled(long simId){
        boolean result = mConnService.getMobileDataEnabledGemini(simId);
        Xlog.d(TAG,"isMoblieDataEnabled for simId " + simId + " " + result);
        return result;
    }

    private void setMobileDataEnabled(boolean enabled) {
        /** M: identify whether data connection is open & close in dataUsage,CR ALPS00355823 */
        mIsUserEnabledNoneGemini = true;
        if (LOGD) Log.d(TAG, "setMobileDataEnabled()");
        mConnService.setMobileDataEnabled(enabled);
        mMobileDataEnabled = enabled;
        updatePolicy(false);
    }

    private void setMobileDataEnabled(long simId, boolean enabled){
         Xlog.d(TAG,"setMobileDataEnabled for simId " + simId + " " + enabled);
		mIsSwitching = true; 
        if(enabled){ 
            mConnService.setMobileDataEnabledGemini(simId);
			timerHandler.sendEmptyMessageDelayed(EVENT_ATTACH_TIME_OUT, ATTACH_TIME_OUT_LENGTH);
        }else{
            mConnService.setMobileDataEnabledGemini(0);
			timerHandler.sendEmptyMessageDelayed(EVENT_DETACH_TIME_OUT, DETACH_TIME_OUT_LENGTH);			
            mIsUserEnabled = false;
        }
        updatePolicy(false);
    }
    private boolean isNetworkPolicyModifiable(NetworkPolicy policy) {
        boolean sim_ready = true;
        if(TAB_SIM_1.equals(mCurrentTab)){
             sim_ready = (mSimStatus1 != Phone.SIM_INDICATOR_RADIOOFF) ;
        }
        if(TAB_SIM_2.equals(mCurrentTab)){
             sim_ready = (mSimStatus2 != Phone.SIM_INDICATOR_RADIOOFF) ;
        }
		Xlog.i(TAG,"isNetworkPolicyModifiable policy : " + policy + " sim_ready " + sim_ready);
        return policy != null && sim_ready && isBandwidthControlEnabled() && mDataEnabled.isChecked();
    }

    private boolean isBandwidthControlEnabled() {
        try {
            boolean result = mNetworkService.isBandwidthControlEnabled();
            Xlog.d(TAG,"isBandWidthControlEnabled " + result);
            return result;
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with INetworkManagementService: " + e);
            return false;
        }
    }

    private boolean getDataRoaming() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Secure.getInt(resolver, Settings.Secure.DATA_ROAMING, 0) != 0;
    }

    private boolean getDataRoaming(int slotId){
       	SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(), slotId);
        Xlog.d(TAG,"get data Romaing for " + slotId + " result " + siminfo.mDataRoaming);
        return siminfo.mDataRoaming == SimInfo.DATA_ROAMING_ENABLE;
    }
    
    private void setDataRoaming(boolean enabled) {
        // TODO: teach telephony DataConnectionTracker to watch and apply
        // updates when changed.
        final ContentResolver resolver = getActivity().getContentResolver();
        Settings.Secure.putInt(resolver, Settings.Secure.DATA_ROAMING, enabled ? 1 : 0);
        mMenuDataRoaming.setChecked(enabled);
    }
    
    private void setDataRoaming(int slotId, boolean enabled){       
   		//mGeminiPhone.setDataRoamingEnabledGemini(enabled, slotId);
 		 Xlog.d(TAG,"set data Romaing for " + slotId + " result " + enabled);
   		if(mITelephony != null){
            try{
       		    mITelephony.setDataRoamingEnabledGemini(enabled,slotId);
            }catch(RemoteException e){
                Xlog.e(TAG,"data roaming setting remote exception");
            }
        }else{
            Xlog.e(TAG,"iTelephony is null , error !");
        }
		SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(getActivity(), slotId);
		
        if(enabled){
    		SIMInfo.setDataRoaming(getActivity(),SimInfo.DATA_ROAMING_ENABLE,simInfo.mSimId );
        }else{
        	SIMInfo.setDataRoaming(getActivity(),SimInfo.DATA_ROAMING_DISABLE, simInfo.mSimId);
        }
    }

    private boolean getRestrictBackground() {
        try {
            return mPolicyService.getRestrictBackground();
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with policy service: " + e);
            return false;
        }
    }

    private void setRestrictBackground(boolean restrictBackground) {
        if (LOGD) Log.d(TAG, "setRestrictBackground()");
        try {
            mPolicyService.setRestrictBackground(restrictBackground);
            mMenuRestrictBackground.setChecked(restrictBackground);
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with policy service: " + e);
        }
    }

    private boolean getAppRestrictBackground() {
        final int primaryUid = getAppDetailPrimaryUid();
        final int uidPolicy;
        try {
            uidPolicy = mPolicyService.getUidPolicy(primaryUid);
        } catch (RemoteException e) {
            // since we can't do much without policy, we bail hard.
            throw new RuntimeException("problem reading network policy", e);
        }

        return (uidPolicy & POLICY_REJECT_METERED_BACKGROUND) != 0;
    }

    private void setAppRestrictBackground(boolean restrictBackground) {
        if (LOGD) Log.d(TAG, "setAppRestrictBackground()");
        final int primaryUid = getAppDetailPrimaryUid();
        try {
            mPolicyService.setUidPolicy(primaryUid,
                    restrictBackground ? POLICY_REJECT_METERED_BACKGROUND : POLICY_NONE);
        } catch (RemoteException e) {
            throw new RuntimeException("unable to save policy", e);
        }

        mAppRestrict.setChecked(restrictBackground);
    }

    /**
     * Update chart sweeps and cycle list to reflect {@link NetworkPolicy} for
     * current {@link #mTemplate}.
     */
    private void updatePolicy(boolean refreshCycle) {
        if (isAppDetailMode()) {
            mNetworkSwitches.setVisibility(View.GONE);
        } else {
            mNetworkSwitches.setVisibility(View.VISIBLE);
        }

        // TODO: move enabled state directly into policy
        if (TAB_MOBILE.equals(mCurrentTab)) {
            mBinding = true;
            mDataEnabled.setChecked(isMobileDataEnabled());
            mBinding = false;
        }else if (TAB_SIM_1.equals(mCurrentTab)) {
            mBinding = true;
            mDataEnabled.setChecked(isMobileDataEnabled(mSimId1)||mIsUserEnabled );
            mBinding = false;
        }else if (TAB_SIM_2.equals(mCurrentTab)){
            mBinding = true;
            mDataEnabled.setChecked(isMobileDataEnabled(mSimId2)||mIsUserEnabled);
            mBinding = false;
        }        

        final NetworkPolicy policy = mPolicyEditor.getPolicy(mTemplate);
		if(mDisableAtLimitView == null ){
	            Xlog.i(TAG,"mDisableAtLimitView should not be null here !!!");
				return;
		}
        if (isNetworkPolicyModifiable(policy)) {
            Xlog.d(TAG,"network policy  modifiable, checkbox on");
            mDisableAtLimitView.setVisibility(View.VISIBLE);
			mPolicyEditor.setPolicyActive(policy);
            mDisableAtLimit.setChecked(policy != null && policy.limitBytes != LIMIT_DISABLED);
            if (!isAppDetailMode()) {
                mChart.bindNetworkPolicy(policy);
            }

        } else {
            // controls are disabled; don't bind warning/limit sweeps
            Xlog.d(TAG,"network policy not modifiable, no warning limit/sweeps.");
            mDisableAtLimitView.setVisibility(View.GONE);
            mChart.bindNetworkPolicy(null);
        }

        if (refreshCycle) {
            // generate cycle list based on policy and available history
            updateCycleList(policy);
        }
    }

    /**
     * Rebuild {@link #mCycleAdapter} based on {@link NetworkPolicy#cycleDay}
     * and available {@link NetworkStatsHistory} data. Always selects the newest
     * item, updating the inspection range on {@link #mChart}.
     */
    private void updateCycleList(NetworkPolicy policy) {
        // stash away currently selected cycle to try restoring below
        final CycleItem previousItem = (CycleItem) mCycleSpinner.getSelectedItem();
        mCycleAdapter.clear();

        final Context context = mCycleSpinner.getContext();

        long historyStart = Long.MAX_VALUE;
        long historyEnd = Long.MIN_VALUE;
        if (mChartData != null) {
            historyStart = mChartData.network.getStart();
            historyEnd = mChartData.network.getEnd();
        }

        final long now = System.currentTimeMillis();
        if (historyStart == Long.MAX_VALUE) historyStart = now;
        if (historyEnd == Long.MIN_VALUE) historyEnd = now + 1;

        boolean hasCycles = false;
        if (policy != null) {
            // find the next cycle boundary
            long cycleEnd = computeNextCycleBoundary(historyEnd, policy);

            // walk backwards, generating all valid cycle ranges
            while (cycleEnd > historyStart) {
                final long cycleStart = computeLastCycleBoundary(cycleEnd, policy);
                Log.d(TAG, "generating cs=" + cycleStart + " to ce=" + cycleEnd + " waiting for hs="
                        + historyStart);
                mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                cycleEnd = cycleStart;
                hasCycles = true;
            }

            // one last cycle entry to modify policy cycle day
            mCycleAdapter.setChangePossible(isNetworkPolicyModifiable(policy));
        }

        if (!hasCycles) {
            // no policy defined cycles; show entry for each four-week period
            long cycleEnd = historyEnd;
            while (cycleEnd > historyStart) {
                final long cycleStart = cycleEnd - (DateUtils.WEEK_IN_MILLIS * 4);
                mCycleAdapter.add(new CycleItem(context, cycleStart, cycleEnd));
                cycleEnd = cycleStart;
            }

            mCycleAdapter.setChangePossible(false);
        }

        // force pick the current cycle (first item)
        if (mCycleAdapter.getCount() > 0) {
            final int position = mCycleAdapter.findNearestPosition(previousItem);
            mCycleSpinner.setSelection(position);

            // only force-update cycle when changed; skipping preserves any
            // user-defined inspection region.
            final CycleItem selectedItem = mCycleAdapter.getItem(position);
            if (!Objects.equal(selectedItem, previousItem)) {
                mCycleListener.onItemSelected(mCycleSpinner, null, position, 0);
            } else {
                // but still kick off loader for detailed list
                updateDetailData();
            }
        } else {
            updateDetailData();
        }
    }

	private Handler timerHandler = new Handler(){
		
		public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_DETACH_TIME_OUT:
                case EVENT_ATTACH_TIME_OUT:
					Xlog.d(TAG,"timer expired update switch enabled");
				    mDataEnabled.setEnabled(true);
					mIsSwitching = false;
					//updatePolicy(true);
	        }
	    }
	};
	private void onDataEnableChangeGemini(boolean dataEnabled,int slotId, long simId){
		if(isMobileDataEnabled(simId) == dataEnabled){
			return;
		}
        if (dataEnabled) {
			if(mTelephonyManager.getSimIndicatorStateGemini(slotId) ==  Phone.SIM_INDICATOR_LOCKED){
				mCellConnMgr.handleCellConn(slotId, PIN1_REQUEST_CODE);	
				Xlog.d(TAG,"Data enable check change request pin");
				mDataEnabled.setChecked(false);
			}else{
                setMobileDataEnabled(simId,true);
                mIsUserEnabled = true;
			}
        } else {
            ConfirmDataDisableFragment.show(DataUsageSummary.this,simId);
        }		
	}
    private OnCheckedChangeListener mDataEnabledListener = new OnCheckedChangeListener() {
        /** {@inheritDoc} */
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mBinding) return;

            final boolean dataEnabled = isChecked;
            final String currentTab = mCurrentTab;
            Xlog.d(TAG,"Data enable check change " + currentTab+ " "+ dataEnabled);
			 
            if(TAB_SIM_1.equals(currentTab)){
				onDataEnableChangeGemini(dataEnabled,Phone.GEMINI_SIM_1,mSimId1);                
            }else if(TAB_SIM_2.equals(currentTab)){
				onDataEnableChangeGemini(dataEnabled,Phone.GEMINI_SIM_2,mSimId2);
            }else if (TAB_MOBILE.equals(currentTab)) {
                if (dataEnabled) {
                    //setMobileDataEnabled(true);
					if(mTelephonyManager.getSimIndicatorState() ==  Phone.SIM_INDICATOR_LOCKED){
						mCellConnMgr.handleCellConn(0, PIN1_REQUEST_CODE);	
						Xlog.d(TAG,"Data enable check change request pin single card");
						mDataEnabled.setChecked(false);
					}else{
		                setMobileDataEnabled(true);
					}                    
                } else {
                    // disabling data; show confirmation dialog which eventually
                    // calls setMobileDataEnabled() once user confirms.
                    ConfirmDataDisableFragment.show(DataUsageSummary.this);
                }
            }

            updatePolicy(true);
        }
    };

    private View.OnClickListener mDisableAtLimitListener = new View.OnClickListener() {
        /** {@inheritDoc} */
        public void onClick(View v) {
            final boolean disableAtLimit = !mDisableAtLimit.isChecked();
            if (disableAtLimit) {
                // enabling limit; show confirmation dialog which eventually
                // calls setPolicyLimitBytes() once user confirms.
                ConfirmLimitFragment.show(DataUsageSummary.this);
            } else {
                setPolicyLimitBytes(LIMIT_DISABLED);
            }
        }
    };

    private View.OnClickListener mAppRestrictListener = new View.OnClickListener() {
        /** {@inheritDoc} */
        public void onClick(View v) {
            final boolean restrictBackground = !mAppRestrict.isChecked();

            if (restrictBackground) {
                if (hasLimitedNetworks()) {
                    // enabling restriction; show confirmation dialog which
                    // eventually calls setRestrictBackground() once user
                    // confirms.
                    ConfirmAppRestrictFragment.show(DataUsageSummary.this);
                } else {
                    // no limited networks; show dialog to guide user towards
                    // setting a network limit. doesn't mutate restrict state.
                    DeniedRestrictFragment.show(DataUsageSummary.this);
                }
            } else {
                setAppRestrictBackground(false);
            }
        }
    };

    private OnClickListener mAppSettingsListener = new OnClickListener() {
        /** {@inheritDoc} */
        public void onClick(View v) {
            // TODO: target torwards entire UID instead of just first package
            startActivity(mAppSettingsIntent);
        }
    };

    private OnItemClickListener mListListener = new OnItemClickListener() {
        /** {@inheritDoc} */
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Context context = view.getContext();
            final AppUsageItem app = (AppUsageItem) parent.getItemAtPosition(position);
            final UidDetail detail = mUidDetailProvider.getUidDetail(app.uids[0], true);
            AppDetailsFragment.show(DataUsageSummary.this, app.uids, detail.label);
        }
    };

    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        /** {@inheritDoc} */
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final CycleItem cycle = (CycleItem) parent.getItemAtPosition(position);
            if (cycle instanceof CycleChangeItem) {
                // show cycle editor; will eventually call setPolicyCycleDay()
                // when user finishes editing.
                CycleEditorFragment.show(DataUsageSummary.this);

                // reset spinner to something other than "change cycle..."
                mCycleSpinner.setSelection(0);

            } else {
                if (LOGD) {
                    Log.d(TAG, "showing cycle " + cycle + ", start=" + cycle.start + ", end="
                            + cycle.end + "]");
                }

                // update chart to show selected cycle, and update detail data
                // to match updated sweep bounds.
                mChart.setVisibleRange(cycle.start, cycle.end);

                updateDetailData();
            }
        }

        /** {@inheritDoc} */
        public void onNothingSelected(AdapterView<?> parent) {
            // ignored
        }
    };

    /**
     * Update details based on {@link #mChart} inspection range depending on
     * current mode. In network mode, updates {@link #mAdapter} with sorted list
     * of applications data usage, and when {@link #isAppDetailMode()} update
     * app details.
     */
    private void updateDetailData() {
        if (LOGD) Log.d(TAG, "updateDetailData()");

        final long start = mChart.getInspectStart();
        final long end = mChart.getInspectEnd();
        final long now = System.currentTimeMillis();

        final Context context = getActivity();

        NetworkStatsHistory.Entry entry = null;
        if (isAppDetailMode() && mChartData != null && mChartData.detail != null) {
            // bind foreground/background to piechart and labels
            entry = mChartData.detailDefault.getValues(start, end, now, entry);
            final long defaultBytes = entry.rxBytes + entry.txBytes;
            entry = mChartData.detailForeground.getValues(start, end, now, entry);
            final long foregroundBytes = entry.rxBytes + entry.txBytes;

            mAppPieChart.setOriginAngle(175);

            mAppPieChart.removeAllSlices();
            mAppPieChart.addSlice(foregroundBytes, Color.parseColor("#d88d3a"));
            mAppPieChart.addSlice(defaultBytes, Color.parseColor("#666666"));

            mAppPieChart.generatePath();

            mAppBackground.setText(Formatter.formatFileSize(context, defaultBytes));
            mAppForeground.setText(Formatter.formatFileSize(context, foregroundBytes));

            // and finally leave with summary data for label below
            entry = mChartData.detail.getValues(start, end, now, null);

            getLoaderManager().destroyLoader(LOADER_SUMMARY);

        } else {
            if (mChartData != null) {
                entry = mChartData.network.getValues(start, end, now, null);
            }

            // kick off loader for detailed stats
            getLoaderManager().restartLoader(LOADER_SUMMARY,
                    SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
        }

        final long totalBytes = entry != null ? entry.rxBytes + entry.txBytes : 0;
        final String totalPhrase = Formatter.formatFileSize(context, totalBytes);
        final String rangePhrase = formatDateRange(context, start, end, false);

        mUsageSummary.setText(
                getString(R.string.data_usage_total_during_range, totalPhrase, rangePhrase));
    }

    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<
            ChartData>() {
        /** {@inheritDoc} */
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(getActivity(), mStatsService, args);
        }

        /** {@inheritDoc} */
        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            Xlog.d(TAG, "ChartDataLoader finished ");
            mChartData = data;
            mChart.bindNetworkStats(mChartData.network);
            mChart.bindDetailNetworkStats(mChartData.detail);

            // calcuate policy cycles based on available data
            updatePolicy(true);
            updateAppDetail();

            // force scroll to top of body when showing detail
            if (mChartData.detail != null && mListView.getScrollY() == 0) {
                mListView.smoothScrollToPosition(0);
            }
        }

        /** {@inheritDoc} */
        public void onLoaderReset(Loader<ChartData> loader) {
            mChartData = null;
            mChart.bindNetworkStats(null);
            mChart.bindDetailNetworkStats(null);
        }
    };

    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<
            NetworkStats>() {
        /** {@inheritDoc} */
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(getActivity(), mStatsService, args);
        }

        /** {@inheritDoc} */
        public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
            Xlog.d(TAG,"SummaryForAllUidLoader finished");
            mAdapter.bindStats(data);
            updateEmptyVisible();
        }

        /** {@inheritDoc} */
        public void onLoaderReset(Loader<NetworkStats> loader) {
            mAdapter.bindStats(null);
            updateEmptyVisible();
        }

        private void updateEmptyVisible() {
            final boolean isEmpty = mAdapter.isEmpty() && !isAppDetailMode();
            mEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    };

    private boolean isMobilePolicySplit() {
        final Context context = getActivity();
        if (hasMobileRadio(context)) {
            final String subscriberId = getActiveSubscriberId(context);
            return mPolicyEditor.isMobilePolicySplit(subscriberId);
        } else {
            return false;
        }
    }

    private void setMobilePolicySplit(boolean split) {
        final String subscriberId = getActiveSubscriberId(getActivity());
        mPolicyEditor.setMobilePolicySplit(subscriberId, split);
    }

    private static String getActiveSubscriberId(Context context) {
        final TelephonyManager telephony = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return telephony.getSubscriberId();
    }

    private static String getSubscriberId(Context context, int simId){
        final TelephonyManager telephony = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return telephony.getSubscriberIdGemini(simId);   
    }

    private DataUsageChartListener mChartListener = new DataUsageChartListener() {
        /** {@inheritDoc} */
        public void onInspectRangeChanged() {
            if (LOGD) Log.d(TAG, "onInspectRangeChanged()");
            updateDetailData();
        }

        /** {@inheritDoc} */
        public void onWarningChanged() {
            setPolicyWarningBytes(mChart.getWarningBytes());
        }

        /** {@inheritDoc} */
        public void onLimitChanged() {
            setPolicyLimitBytes(mChart.getLimitBytes());
        }

        /** {@inheritDoc} */
        public void requestWarningEdit() {
            WarningEditorFragment.show(DataUsageSummary.this);
        }

        /** {@inheritDoc} */
        public void requestLimitEdit() {
            LimitEditorFragment.show(DataUsageSummary.this);
        }
    };

    /**
     * List item that reflects a specific data usage cycle.
     */
    public static class CycleItem implements Comparable<CycleItem> {
        public CharSequence label;
        public long start;
        public long end;

        CycleItem(CharSequence label) {
            this.label = label;
        }

        public CycleItem(Context context, long start, long end) {
            this.label = formatDateRange(context, start, end, true);
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return label.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CycleItem) {
                final CycleItem another = (CycleItem) o;
                return start == another.start && end == another.end;
            }
            return false;
        }

        /** {@inheritDoc} */
        public int compareTo(CycleItem another) {
            return Long.compare(start, another.start);
        }
    }

    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static final java.util.Formatter sFormatter = new java.util.Formatter(
            sBuilder, Locale.getDefault());

    public static String formatDateRange(Context context, long start, long end, boolean utcTime) {
        final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH;
        final String timezone = utcTime ? TIMEZONE_UTC : null;

        synchronized (sBuilder) {
            sBuilder.setLength(0);
            return DateUtils
                    .formatDateRange(context, sFormatter, start, end, flags, timezone).toString();
        }
    }

    /**
     * Special-case data usage cycle that triggers dialog to change
     * {@link NetworkPolicy#cycleDay}.
     */
    public static class CycleChangeItem extends CycleItem {
        public CycleChangeItem(Context context) {
            super(context.getString(R.string.data_usage_change_cycle));
        }
    }

    public static class CycleAdapter extends ArrayAdapter<CycleItem> {
        private boolean mChangePossible = false;
        private boolean mChangeVisible = false;

        private final CycleChangeItem mChangeItem;

        public CycleAdapter(Context context) {
            super(context, android.R.layout.simple_spinner_item);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mChangeItem = new CycleChangeItem(context);
        }

        public void setChangePossible(boolean possible) {
            mChangePossible = possible;
            updateChange();
        }

        public void setChangeVisible(boolean visible) {
            mChangeVisible = visible;
            updateChange();
        }

        private void updateChange() {
            remove(mChangeItem);
            if (mChangePossible && mChangeVisible) {
                add(mChangeItem);
            }
        }

        /**
         * Find position of {@link CycleItem} in this adapter which is nearest
         * the given {@link CycleItem}.
         */
        public int findNearestPosition(CycleItem target) {
            if (target != null) {
                final int count = getCount();
                for (int i = count - 1; i >= 0; i--) {
                    final CycleItem item = getItem(i);
                    if (item instanceof CycleChangeItem) {
                        continue;
                    } else if (item.compareTo(target) >= 0) {
                        return i;
                    }
                }
            }
            return 0;
        }
    }

    private static class AppUsageItem implements Comparable<AppUsageItem> {
        public int[] uids;
        public long total;

        public AppUsageItem(int uid) {
            uids = new int[] { uid };
        }

        public void addUid(int uid) {
            if (contains(uids, uid)) return;
            final int length = uids.length;
            uids = Arrays.copyOf(uids, length + 1);
            uids[length] = uid;
        }

        /** {@inheritDoc} */
        public int compareTo(AppUsageItem another) {
            return Long.compare(another.total, total);
        }
    }

    /**
     * Adapter of applications, sorted by total usage descending.
     */
    public static class DataUsageAdapter extends BaseAdapter {
        private final UidDetailProvider mProvider;
        private final int mInsetSide;

        private ArrayList<AppUsageItem> mItems = Lists.newArrayList();
        private long mLargest;

        public DataUsageAdapter(UidDetailProvider provider, int insetSide) {
            mProvider = checkNotNull(provider);
            mInsetSide = insetSide;
        }

        /**
         * Bind the given {@link NetworkStats}, or {@code null} to clear list.
         */
        public void bindStats(NetworkStats stats) {
            mItems.clear();

            final AppUsageItem systemItem = new AppUsageItem(android.os.Process.SYSTEM_UID);
            final SparseArray<AppUsageItem> knownUids = new SparseArray<AppUsageItem>();

            NetworkStats.Entry entry = null;
            final int size = stats != null ? stats.size() : 0;
            for (int i = 0; i < size; i++) {
                entry = stats.getValues(i, entry);

                final int uid = entry.uid;
                final boolean isApp = uid >= android.os.Process.FIRST_APPLICATION_UID
                        && uid <= android.os.Process.LAST_APPLICATION_UID;
                if (isApp || uid == UID_REMOVED || uid == UID_TETHERING) {
                    AppUsageItem item = knownUids.get(uid);
                    if (item == null) {
                        item = new AppUsageItem(uid);
                        knownUids.put(uid, item);
                        mItems.add(item);
                    }

                    item.total += entry.rxBytes + entry.txBytes;
                } else {
                    systemItem.total += entry.rxBytes + entry.txBytes;
                    systemItem.addUid(uid);
                }
            }

            if (systemItem.total > 0) {
                mItems.add(systemItem);
            }

            Collections.sort(mItems);
            mLargest = (mItems.size() > 0) ? mItems.get(0).total : 0;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).uids[0];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.data_usage_item, parent, false);

                if (mInsetSide > 0) {
                    convertView.setPadding(mInsetSide, 0, mInsetSide, 0);
                }
            }

            final Context context = parent.getContext();

            final TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
            final ProgressBar progress = (ProgressBar) convertView.findViewById(
                    android.R.id.progress);

            // kick off async load of app details
            final AppUsageItem item = mItems.get(position);
            UidDetailTask.bindView(mProvider, item, convertView);

            text1.setText(Formatter.formatFileSize(context, item.total));

            final int percentTotal = mLargest != 0 ? (int) (item.total * 100 / mLargest) : 0;
            progress.setProgress(percentTotal);

            return convertView;
        }
    }

    /**
     * Empty {@link Fragment} that controls display of UID details in
     * {@link DataUsageSummary}.
     */
    public static class AppDetailsFragment extends Fragment {
        private static final String EXTRA_UIDS = "uids";

        public static void show(DataUsageSummary parent, int[] uids, CharSequence label) {
            if (!parent.isAdded()) return;

            final Bundle args = new Bundle();
            args.putIntArray(EXTRA_UIDS, uids);

            final AppDetailsFragment fragment = new AppDetailsFragment();
            fragment.setArguments(args);
            fragment.setTargetFragment(parent, 0);

            final FragmentTransaction ft = parent.getFragmentManager().beginTransaction();
            ft.add(fragment, TAG_APP_DETAILS);
            ft.addToBackStack(TAG_APP_DETAILS);
            ft.setBreadCrumbTitle(label);
            ft.commit();
        }

        @Override
        public void onStart() {
            super.onStart();
            final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
            target.mAppDetailUids = getArguments().getIntArray(EXTRA_UIDS);
            Xlog.d(TAG,"AppDetailsFragment start ");
            target.updateBody();
        }

        @Override
        public void onStop() {
            super.onStop();
            final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
            target.mAppDetailUids = null;
            target.updateBody();
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link NetworkPolicy#limitBytes}.
     */
    public static class ConfirmLimitFragment extends DialogFragment {
        private static final String EXTRA_MESSAGE = "message";
        private static final String EXTRA_LIMIT_BYTES = "limitBytes";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final Resources res = parent.getResources();
            final CharSequence message;
            final long limitBytes;

            // TODO: customize default limits based on network template
            final String currentTab = parent.mCurrentTab;
            if (TAB_3G.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_tab_3g);
                limitBytes = 5 * GB_IN_BYTES;
            } else if (TAB_4G.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_tab_4g);
                limitBytes = 5 * GB_IN_BYTES;
            } else if (TAB_SIM_1.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_list_mobile);
                limitBytes = 5 * GB_IN_BYTES;
            } else if (TAB_SIM_2.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_list_mobile);
                limitBytes = 5 * GB_IN_BYTES;
            } else if (TAB_MOBILE.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_list_mobile);
                limitBytes = 5 * GB_IN_BYTES;
            } else if (TAB_WIFI.equals(currentTab)) {
                message = buildDialogMessage(res, R.string.data_usage_tab_wifi);
                limitBytes = 5 * GB_IN_BYTES;
            } else {
                throw new IllegalArgumentException("unknown current tab: " + currentTab);
            }

            final Bundle args = new Bundle();
            args.putCharSequence(EXTRA_MESSAGE, message);
            args.putLong(EXTRA_LIMIT_BYTES, limitBytes);

            final ConfirmLimitFragment dialog = new ConfirmLimitFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_LIMIT);
        }

        private static CharSequence buildDialogMessage(Resources res, int networkResId) {
            return res.getString(R.string.data_usage_limit_dialog, res.getString(networkResId));
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final CharSequence message = getArguments().getCharSequence(EXTRA_MESSAGE);
            final long limitBytes = getArguments().getLong(EXTRA_LIMIT_BYTES);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_limit_dialog_title);
            builder.setMessage(message);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
                    if (target != null) {
                        target.setPolicyLimitBytes(limitBytes);
                    }
                }
            });

            return builder.create();
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#cycleDay}.
     */
    public static class CycleEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final CycleEditorFragment dialog = new CycleEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CYCLE_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_cycle_editor, null, false);
            final NumberPicker cycleDayPicker = (NumberPicker) view.findViewById(R.id.cycle_day);

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final int cycleDay = editor.getPolicyCycleDay(template);

            cycleDayPicker.setMinValue(1);
            cycleDayPicker.setMaxValue(31);
            cycleDayPicker.setValue(cycleDay);
            cycleDayPicker.setWrapSelectorWheel(true);

            builder.setTitle(R.string.data_usage_cycle_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final int cycleDay = cycleDayPicker.getValue();
                            editor.setPolicyCycleDay(template, cycleDay);
                            target.updatePolicy(true);
                        }
                    });

            return builder.create();
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#warningBytes}.
     */
    public static class WarningEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final WarningEditorFragment dialog = new WarningEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_WARNING_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_bytes_editor, null, false);
            final NumberPicker bytesPicker = (NumberPicker) view.findViewById(R.id.bytes);

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final long warningBytes = editor.getPolicyWarningBytes(template);
            final long limitBytes = editor.getPolicyLimitBytes(template);

            bytesPicker.setMinValue(0);
            if (limitBytes != LIMIT_DISABLED) {
                bytesPicker.setMaxValue((int) (limitBytes / MB_IN_BYTES) - 1);
            } else {
                //bytesPicker.setMaxValue(Integer.MAX_VALUE);
                bytesPicker.setMaxValue(WARNING_MAX_SIZE);
            }
            bytesPicker.setValue((int) (warningBytes / MB_IN_BYTES));
            bytesPicker.setWrapSelectorWheel(false);

            builder.setTitle(R.string.data_usage_warning_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // clear focus to finish pending text edits
                            bytesPicker.clearFocus();

                            final long bytes = bytesPicker.getValue() * MB_IN_BYTES;
                            editor.setPolicyWarningBytes(template, bytes);
                            target.updatePolicy(false);
                        }
                    });

            return builder.create();
        }
    }

    /**
     * Dialog to edit {@link NetworkPolicy#limitBytes}.
     */
    public static class LimitEditorFragment extends DialogFragment {
        private static final String EXTRA_TEMPLATE = "template";

        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_TEMPLATE, parent.mTemplate);

            final LimitEditorFragment dialog = new LimitEditorFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_LIMIT_EDITOR);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
            final NetworkPolicyEditor editor = target.mPolicyEditor;

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

            final View view = dialogInflater.inflate(R.layout.data_usage_bytes_editor, null, false);
            final NumberPicker bytesPicker = (NumberPicker) view.findViewById(R.id.bytes);

            final NetworkTemplate template = getArguments().getParcelable(EXTRA_TEMPLATE);
            final long warningBytes = editor.getPolicyWarningBytes(template);
            final long limitBytes = editor.getPolicyLimitBytes(template);

            bytesPicker.setMaxValue(LIMIT_MAX_SIZE);
            if (warningBytes != WARNING_DISABLED && warningBytes > 0) {
                bytesPicker.setMinValue((int) (warningBytes / MB_IN_BYTES) + 1);
            } else {
                bytesPicker.setMinValue(0);
            }
            bytesPicker.setValue((int) (limitBytes / MB_IN_BYTES));
            bytesPicker.setWrapSelectorWheel(false);

            builder.setTitle(R.string.data_usage_limit_editor_title);
            builder.setView(view);

            builder.setPositiveButton(R.string.data_usage_cycle_editor_positive,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // clear focus to finish pending text edits
                            bytesPicker.clearFocus();

                            final long bytes = bytesPicker.getValue() * MB_IN_BYTES;
                            editor.setPolicyLimitBytes(template, bytes);
                            target.updatePolicy(false);
                        }
                    });

            return builder.create();
        }
    }
    /**
     * Dialog to request user confirmation before disabling data.
     */
    public static class ConfirmDataDisableFragment extends DialogFragment {

        public static void show(DataUsageSummary parent) {
            show(parent , -1);
        }
        public static void show(DataUsageSummary parent,long simId) {
            if (!parent.isAdded()) return;

            final ConfirmDataDisableFragment dialog = new ConfirmDataDisableFragment();
            dialog.setTargetFragment(parent, (int)simId);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_DISABLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.data_usage_disable_mobile);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
                    long simId = (long)getTargetRequestCode();
                    if (target != null) {
                        // TODO: extend to modify policy enabled flag.
                        if(simId != -1){
                            target.setMobileDataEnabled(simId,false);                            
                        }else{
                            target.setMobileDataEnabled(false);
                        }
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link Settings.Secure#DATA_ROAMING}.
     */
    public static class ConfirmDataRoamingFragment extends DialogFragment {

        public static void show(DataUsageSummary parent) {
            show(parent,-1);
        }
        public static void show(DataUsageSummary parent,int slotId) {
            if (!parent.isAdded()) return;

            final ConfirmDataRoamingFragment dialog = new ConfirmDataRoamingFragment();
            dialog.setTargetFragment(parent, slotId);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_DATA_ROAMING);

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.roaming_reenable_title);
            builder.setMessage(R.string.roaming_warning);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
                    int simId = getTargetRequestCode();
                    if (target != null) {
                        if(simId!= -1){
                            target.setDataRoaming(simId,true);
                        }else{
                            target.setDataRoaming(true);
                        } 
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link INetworkPolicyManager#setRestrictBackground(boolean)}.
     */
    public static class ConfirmRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final ConfirmRestrictFragment dialog = new ConfirmRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_restrict_background_title);
            builder.setMessage(getString(R.string.data_usage_restrict_background));

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
                    if (target != null) {
                        target.setRestrictBackground(true);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    /**
     * Dialog to inform user that {@link #POLICY_REJECT_METERED_BACKGROUND}
     * change has been denied, usually based on
     * {@link DataUsageSummary#hasLimitedNetworks()}.
     */
    public static class DeniedRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final DeniedRestrictFragment dialog = new DeniedRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_DENIED_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_app_restrict_background);
            builder.setMessage(R.string.data_usage_restrict_denied_dialog);
            builder.setPositiveButton(android.R.string.ok, null);

            return builder.create();
        }
    }

    /**
     * Dialog to request user confirmation before setting
     * {@link #POLICY_REJECT_METERED_BACKGROUND}.
     */
    public static class ConfirmAppRestrictFragment extends DialogFragment {
        public static void show(DataUsageSummary parent) {
            if (!parent.isAdded()) return;

            final ConfirmAppRestrictFragment dialog = new ConfirmAppRestrictFragment();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_CONFIRM_APP_RESTRICT);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.data_usage_app_restrict_dialog_title);
            builder.setMessage(R.string.data_usage_app_restrict_dialog);

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final DataUsageSummary target = (DataUsageSummary) getTargetFragment();
                    if (target != null) {
                        target.setAppRestrictBackground(true);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    /**
     * Compute default tab that should be selected, based on
     * {@link NetworkPolicyManager#EXTRA_NETWORK_TEMPLATE} extra.
     */
    private String computeTabFromIntent(Intent intent) {
        final NetworkTemplate template = intent.getParcelableExtra(EXTRA_NETWORK_TEMPLATE);
        if (template == null) return null;

        switch (template.getMatchRule()) {
            case MATCH_MOBILE_3G_LOWER:
                return TAB_3G;
            case MATCH_MOBILE_4G:
                return TAB_4G;
            case MATCH_MOBILE_ALL:
                if(FeatureOption.MTK_GEMINI_SUPPORT){
                    String subscriber = template.getSubscriberId();
                    Xlog.d(TAG,"computeTabFromIntent, subscriber " + subscriber);                    
                    if(subscriber == null){
                        Xlog.e(TAG,"the subscriber error , null!");
                        return TAB_SIM_1;
                    }
                    if(subscriber.equals(getSubscriberId(getActivity(),Phone.GEMINI_SIM_1))){
                        return TAB_SIM_1;
                    }else if(subscriber.equals(getSubscriberId(getActivity(),Phone.GEMINI_SIM_2))){
                        return TAB_SIM_2;
                    }else{
                        Xlog.e(TAG,"the subscriber error , no mataching!");
                        return TAB_SIM_1;                        
                    }                    
                }else{
                    return TAB_MOBILE;
                }
            case MATCH_WIFI:
                return TAB_WIFI;
            default:
                return null;
        }
    }

    /**
     * Background task that loads {@link UidDetail}, binding to
     * {@link DataUsageAdapter} row item when finished.
     */
    private static class UidDetailTask extends AsyncTask<Void, Void, UidDetail> {
        private final UidDetailProvider mProvider;
        private final AppUsageItem mItem;
        private final View mTarget;

        private UidDetailTask(UidDetailProvider provider, AppUsageItem item, View target) {
            mProvider = checkNotNull(provider);
            mItem = checkNotNull(item);
            mTarget = checkNotNull(target);
        }

        public static void bindView(
                UidDetailProvider provider, AppUsageItem item, View target) {
            final UidDetailTask existing = (UidDetailTask) target.getTag();
            if (existing != null) {
                existing.cancel(false);
            }

            final UidDetail cachedDetail = provider.getUidDetail(item.uids[0], false);
            if (cachedDetail != null) {
                bindView(cachedDetail, target);
            } else {
                target.setTag(new UidDetailTask(provider, item, target).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR));
            }
        }

        private static void bindView(UidDetail detail, View target) {
            final ImageView icon = (ImageView) target.findViewById(android.R.id.icon);
            final TextView title = (TextView) target.findViewById(android.R.id.title);

            if (detail != null) {
                icon.setImageDrawable(detail.icon);
                title.setText(detail.label);
            } else {
                icon.setImageDrawable(null);
                title.setText(null);
            }
        }

        @Override
        protected void onPreExecute() {
            bindView(null, mTarget);
        }

        @Override
        protected UidDetail doInBackground(Void... params) {
            return mProvider.getUidDetail(mItem.uids[0], true);
        }

        @Override
        protected void onPostExecute(UidDetail result) {
            bindView(result, mTarget);
        }
    }

    /**
     * Test if device has a mobile data radio.
     */
    private static boolean hasMobileRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("mobile");
        }

        final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        // mobile devices should have MOBILE network tracker regardless of
        // connection status.
        return conn.getNetworkInfo(TYPE_MOBILE) != null;
    }

    /**
     * Test if device has a mobile 4G data radio.
     */
    private static boolean hasMobile4gRadio(Context context) {
        if (!NetworkPolicyEditor.ENABLE_SPLIT_POLICIES) {
            return false;
        }
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("4g");
        }

        final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        final TelephonyManager telephony = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);

        // WiMAX devices should have WiMAX network tracker regardless of
        // connection status.
        final boolean hasWimax = conn.isNetworkSupported(TYPE_WIMAX);
        final boolean hasLte = telephony.getLteOnCdmaMode() == Phone.LTE_ON_CDMA_TRUE;
        return hasWimax || hasLte;
    }

    /**
     * Test if device has a Wi-Fi data radio.
     */
    private static boolean hasWifiRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("wifi");
        }

        final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return conn.isNetworkSupported(TYPE_WIFI);
    }

    /**
     * Test if device has an ethernet network connection.
     */
    private static boolean hasEthernet(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("ethernet");
        }

        final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return conn.isNetworkSupported(TYPE_ETHERNET);
    }

    /**
     * Inflate a {@link Preference} style layout, adding the given {@link View}
     * widget into {@link android.R.id#widget_frame}.
     */
    private static View inflatePreference(LayoutInflater inflater, ViewGroup root, View widget) {
        final View view = inflater.inflate(R.layout.preference, root, false);
        final LinearLayout widgetFrame = (LinearLayout) view.findViewById(
                android.R.id.widget_frame);
        widgetFrame.addView(widget, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return view;
    }

    private static View inflateAppTitle(
            LayoutInflater inflater, ViewGroup root, CharSequence label) {
        final TextView view = (TextView) inflater.inflate(
                R.layout.data_usage_app_title, root, false);
        view.setText(label);
        return view;
    }

    /**
     * Test if any networks are currently limited.
     */
    private boolean hasLimitedNetworks() {
        return !buildLimitedNetworksList().isEmpty();
    }

    /**
     * Build string describing currently limited networks, which defines when
     * background data is restricted.
     */
    private CharSequence buildLimitedNetworksString() {
        final List<CharSequence> limited = buildLimitedNetworksList();

        // handle case where no networks limited
        if (limited.isEmpty()) {
            limited.add(getText(R.string.data_usage_list_none));
        }

        return TextUtils.join(limited);
    }

    /**
     * Build list of currently limited networks, which defines when background
     * data is restricted.
     */
    private List<CharSequence> buildLimitedNetworksList() {
        final Context context = getActivity();
        final String subscriberId = getActiveSubscriberId(context);

        // build combined list of all limited networks
        final ArrayList<CharSequence> limited = Lists.newArrayList();
        final String currentTab = mTabHost.getCurrentTabTag();




   		if(FeatureOption.MTK_GEMINI_SUPPORT){	
	        if (mHaveSim1Tab&&mPolicyEditor.hasLimitedPolicy(buildTemplateMobileAllGemini(getSubscriberId(context,Phone.GEMINI_SIM_1),mSimId1))){
		   	    limited.add(getText(R.string.data_usage_list_mobile));           	
	        }
			if (mHaveSim2Tab && mPolicyEditor.hasLimitedPolicy(buildTemplateMobileAllGemini(getSubscriberId(context,Phone.GEMINI_SIM_2),mSimId2))){
			    limited.add(getText(R.string.data_usage_list_mobile));			
	        }
		}else if ( mPolicyEditor.hasLimitedPolicy(buildTemplateMobileAll(subscriberId))) {
            limited.add(getText(R.string.data_usage_list_mobile));
        }
        if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobile3gLower(subscriberId))) {
            limited.add(getText(R.string.data_usage_tab_3g));
        }
        if (mPolicyEditor.hasLimitedPolicy(buildTemplateMobile4g(subscriberId))) {
            limited.add(getText(R.string.data_usage_tab_4g));
        }
        if (mPolicyEditor.hasLimitedPolicy(buildTemplateWifi())) {
            limited.add(getText(R.string.data_usage_tab_wifi));
        }
        if (mPolicyEditor.hasLimitedPolicy(buildTemplateEthernet())) {
            limited.add(getText(R.string.data_usage_tab_ethernet));
        }

        return limited;
    }

    /**
     * Inset both selector and divider {@link Drawable} on the given
     * {@link ListView} by the requested dimensions.
     */
    private static void insetListViewDrawables(ListView view, int insetSide) {
        final Drawable selector = view.getSelector();
        final Drawable divider = view.getDivider();

        // fully unregister these drawables so callbacks can be maintained after
        // wrapping below.
        final Drawable stub = new ColorDrawable(Color.TRANSPARENT);
        view.setSelector(stub);
        view.setDivider(stub);

        view.setSelector(new InsetBoundsDrawable(selector, insetSide));
        view.setDivider(new InsetBoundsDrawable(divider, insetSide));
    }

    /**
     * Set {@link android.R.id#title} for a preference view inflated with
     * {@link #inflatePreference(LayoutInflater, ViewGroup, View)}.
     */
    private static void setPreferenceTitle(View parent, int resId) {
        final TextView title = (TextView) parent.findViewById(android.R.id.title);
        title.setText(resId);
    }

    /**
     * Set {@link android.R.id#summary} for a preference view inflated with
     * {@link #inflatePreference(LayoutInflater, ViewGroup, View)}.
     */
    private static void setPreferenceSummary(View parent, CharSequence string) {
        final TextView summary = (TextView) parent.findViewById(android.R.id.summary);
        summary.setVisibility(View.VISIBLE);
        summary.setText(string);
    }

    private static boolean contains(int[] haystack, int needle) {
        for (int value : haystack) {
            if (value == needle) {
                return true;
            }
        }
        return false;
    }
}

