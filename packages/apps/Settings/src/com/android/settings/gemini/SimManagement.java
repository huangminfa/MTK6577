package com.android.settings.gemini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.AsyncResult;
import android.net.sip.SipManager;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;

import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;

import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.featureoption.FeatureOption;

import android.provider.Settings;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.ITelephony;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.IccCard;
import com.mediatek.featureoption.FeatureOption;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.xlog.Xlog;

class SimItem {
    public boolean mIsSim = true;
    public String mName = null;
    public String mNumber = null;
    public int mDispalyNumberFormat = 0;
    public int mColor = -1;
    public int mSlot = -1;
    public long mSimID = -1;
    public int mState = Phone.SIM_INDICATOR_NORMAL;
    
    //Constructor for not real sim
    public SimItem (String name, int color,long simID) {
    	mName = name;
    	mColor = color;
    	mIsSim = false;
    	mSimID = simID;
    }
    //constructor for sim
    public SimItem (SIMInfo siminfo) {
    	mIsSim = true;
    	mName = siminfo.mDisplayName;
    	mNumber = siminfo.mNumber;
    	mDispalyNumberFormat = siminfo.mDispalyNumberFormat;
    	mColor = siminfo.mColor;
    	mSlot = siminfo.mSlot;
    	mSimID = siminfo.mSimId;
    }
}

public class SimManagement extends SettingsPreferenceFragment 
			implements Preference.OnPreferenceChangeListener, SimInfoEnablePreference.OnPreferenceClickCallback {


	
    private static final String TAG = "SimManagementSettings";
    private static final String KEY_SIM_INFO_CATEGORY = "sim_info";
    private static final String KEY_GENERAL_SETTINGS_CATEGORY = "general_settings";
    private static final String KEY_DEFAULT_SIM_SETTINGS_CATEGORY = "default_sim";
    private static final String KEY_SIM_CONTACTS_SETTINGS = "contacts_sim";
    private static final String KEY_VOICE_CALL_SIM_SETTING = "voice_call_sim_setting";
    
    private static final String KEY_VIDEO_CALL_SIM_SETTING = "video_call_sim_setting";
    private static final String KEY_SMS_SIM_SETTING = "sms_sim_setting";
    private static final String KEY_GPRS_SIM_SETTING = "gprs_sim_setting";
    //MTK_OP02_PROTECT_START   
    private static final String KEY_NETWORK_MODE_SETTING = "gsm_umts_preferred_network_mode_key";
    private static final String KEY_NETWORK_MODE_SETTING_GEMINI = "gsm_umts_preferred_network_mode_gemini_key";
    
    private static final String KEY_3G_SERVICE_SETTING = "3g_service_settings";   
    
    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    //MTK_OP02_PROTECT_END  
    
    //time out message event
    private static final int EVENT_DETACH_TIME_OUT = 2000;
    private static final int EVENT_ATTACH_TIME_OUT = 2001;
    //time out length
    private static final int DETACH_TIME_OUT_LENGTH = 10000;
    private static final int ATTACH_TIME_OUT_LENGTH = 30000;
    
    private static final int PIN1_REQUEST_CODE = 302;
    private static final int VEDIO_CALL_OFF = -1;
    
    //MTK_OP02_PROTECT_START
    private static final int NUMERIC_MIN_LENGTH = 5;
    
    private static final String optr = SystemProperties.get("ro.operator.optr");
    //MTK_OP02_PROTECT_END
    private static final String baseband =  SystemProperties.get("gsm.baseband.capability");
    
    private Map<Long, Integer> mSimIdToIndexMap;

    private static boolean mHasNetworkMode = false;
    private static boolean mScreenEnable = true;
    private static boolean mAllSimRadioOff = false;
    private static boolean mHasSim = false;
    private static boolean mGprsTargSim = false;
    //MTK_OP02_PROTECT_START
    private static boolean mIsCU = false;
    //MTK_OP02_PROTECT_END
    private static boolean m3gSupport = false;   
    private static boolean mVTCallSupport = true;
	private static boolean mVoipAvailable = true;
    private boolean mIs3gOff = false;	
    private static final String TRANSACTION_START = "com.android.mms.transaction.START";
    private static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";
    private static final String MMS_TRANSACTION = "mms.transaction";
       
    

    private Map<Long,SIMInfo> mSimMap;

    private int mDualSimMode = 0;

    private long SIM_ID_INVALID = -5;

    private long mVTTargetTemp;   

    
    private boolean mIsSlot1Insert = false;
    private boolean mIsSlot2Insert = false;
    
    private static final int VOICE_CALL_SIM_INDEX = 0;
    private static final int VIDEO_CALL_SIM_INDEX = 1;
    private static final int SMS_SIM_INDEX = 2;
    private static final int GPRS_SIM_INDEX = 3;
    
    private static final int TYPE_SIM_NAME = 0;
    private static final int TYPE_SIM_COLOR = 1;
    private static final int TYPE_SIM_NUMBER = 2;
    private static final int TYPE_SIM_NUMBER_FORMAT = 3;
    
    private static final int DIALOG_ACTIVATE = 1000;
    private static final int DIALOG_DEACTIVATE = 1001;
    private static final int DIALOG_WAITING = 1004;
    private static final int DIALOG_NETWORK_MODE_CHANGE = 1005;
    private static final int DIALOG_3G_MODEM_SWITCHING = 1006;
    private static final int DIALOG_3G_MODEM_SWITCH_CONFIRM = 1007;
    private static final int DIALOG_GPRS_SWITCH_CONFIRM = 1008;
    

    private SimInfoEnablePreference mSlot1SimPref;
    private SimInfoEnablePreference mSlot2SimPref;    

    
    private DefaultSimPreference mVoiceCallSimSetting;
    private DefaultSimPreference mVideoCallSimSetting;
    private DefaultSimPreference mSmsSimSetting;
    private DefaultSimPreference mGprsSimSetting;
    
    private PreferenceScreen mSimAndContacts;
    
    //MTK_OP02_PROTECT_START
    private NetworkModePreference mNetworkMode;
    private PreferenceScreen mNetworkModeGemini;
    private PreferenceScreen m3gService;
    //MTK_OP02_PROTECT_END
    private TelephonyManagerEx mTelephonyManagerEx;
    private TelephonyManager mTelephonyManager;
    private ITelephony iTelephony;
    private StatusBarManager mStatusBarManager;

    private static ContentObserver mGprsDefaultSIMObserver;

    
    private static final int EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE = 1;    
    
	private List<SimItem> mSimItemListVoice = new ArrayList<SimItem>();
	private List<SimItem> mSimItemListVideo = new ArrayList<SimItem>();
	private List<SimItem> mSimItemListSms = new ArrayList<SimItem>();
	private List<SimItem> mSimItemListGprs = new ArrayList<SimItem>();    
	private List<Long> mSimMapKeyList = null;
 
    private boolean mIsSIMModeSwitching = false;
    private boolean mIsGprsSwitching = false;
    private boolean mIsModemSwitching = false;

    private IntentFilter mIntentFilter;
    
    private int mIsShowDlg = -1;// -1: none; 0: radio on; 1 radio off; 2: data switching; 3: modem switch
    //MTK_OP02_PROTECT_START
    private String[] mNetworkModeSummary;
    //MTK_OP02_PROTECT_END


    private int[] mDataSwitchMsgStr = {
    		R.string.gemini_3g_disable_warning_case0,
    		R.string.gemini_3g_disable_warning_case1,
    		R.string.gemini_3g_disable_warning_case2,
    		R.string.gemini_3g_disable_warning_case3,
    		R.string.gemini_3g_disable_warning_case4
    		};
    
    private int mDataSwitchMsgIndex = -1;
    private CellConnMgr mCellConnMgr;

    private Runnable mServiceComplete = new Runnable() {
        public void run() {
            //
        }
    };
  
    private boolean sIsVoiceCapable = true; 
    private boolean isVoiceCapable(){
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	sIsVoiceCapable = (telephony != null && telephony.isVoiceCapable());
	return sIsVoiceCapable;
    }

    private boolean sIsSmsCapable = true;
    private boolean isSmsCapable(){
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        sIsSmsCapable = (telephony != null && telephony.isSmsCapable());
        return sIsSmsCapable;
    }

	private Handler mDualSimModeChangedHander = new Handler() {
	    public void handleMessage(Message msg){

	        switch(msg.what){
	            case EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE:
	            	if(getActivity() == null) {
		                Xlog.i(TAG, "getActivity is null!");
	            		return;
	            	}
                    Xlog.i(TAG, "dual sim mode changed!+mIsSIMModeSwitching="+mIsSIMModeSwitching);
                    if (mIsSIMModeSwitching){
                        dealWithSwtichComplete();
                    }
	            }
	        }
	    };
	    
	private Messenger mMessenger = new Messenger(mDualSimModeChangedHander);
	private BroadcastReceiver mSimInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
					if (mHasSim == false) {
						Xlog.i(TAG,"receiver: TelephonyIntents.ACTION_SIM_INFO_UPDATE+mHasSim="+ mHasSim);
						updateSimInfoAsNoSim();
					} else {
						Xlog.i(TAG,"receiver: TelephonyIntents.ACTION_SIM_INFO_UPDATE+mHasSim="+ mHasSim);
						initSimMap();
						addSimInfoPreference();
						initDefaultSimPreference();
					}

			}
		}
	};
	private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
		
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            String action = intent.getAction();
            
            if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
            	
            	long simid = intent.getLongExtra("simid", -1);
            	int type = intent.getIntExtra("type", -1);
            	Xlog.i(TAG,"receiver: Intent.SIM_SETTINGS_INFO_CHANGED");
            	Xlog.i(TAG,"type is "+type+" simid is "+simid);         	
            	updateSimInfo(simid,type);
            	updateDefaultSimInfo(simid);
            	
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)){
            	Xlog.i(TAG,"receiver: TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED");
                int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                int simStatus = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
                int simNum = mSimMap.size();
                Xlog.i(TAG, "slotid is " +slotId + "status is "+ simStatus+" simNum="+simNum);
                if ((simNum>0)&&(slotId>=0)&&(simStatus>=0)) {
                	updateSimState(slotId,simStatus); 
                	updateDefaultSimState(slotId,simStatus);
                }

            } else if (action.equals(TelephonyIntents.ACTION_SIM_NAME_UPDATE)){
            	Xlog.i(TAG,"receiver: TelephonyIntents.ACTION_SIM_NAME_UPDATE");
            	int slotid = intent.getIntExtra("simId", -1);
            	
            	if(slotid<0)
            		return;
            	
    	    	SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(context, slotid);
    	    	if(siminfo != null){
    	    		long simID = siminfo.mSimId;
                	Xlog.i(TAG,"slotid is "+slotid);         	
                	updateSimInfo(simID,0);
                	updateDefaultSimInfo(simID);
    	    	}
            }  else if (action.equals(TRANSACTION_START)){
            	Xlog.i(TAG, "receiver: TRANSACTION_START");
            	mScreenEnable = false;
            	mGprsSimSetting.setEnabled(!mAllSimRadioOff && mScreenEnable && mHasSim);
                Dialog dlg = mGprsSimSetting.getDialog();
                if(dlg != null){
    	        	if(true == dlg.isShowing()){
    	        		dlg.dismiss();
    	        	}
                } 
            }else if (action.equals(TRANSACTION_STOP)){
                Xlog.i(TAG, "receiver: TRANSACTION_STOP");
                mScreenEnable = true;
                mGprsSimSetting.setEnabled(!mAllSimRadioOff && mScreenEnable && mHasSim);
                   Dialog dlg = mGprsSimSetting.getDialog();
                   if(dlg != null){
                	   if(true == dlg.isShowing()){
                		   dlg.dismiss();
                	   }
                    }
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
            		mAllSimRadioOff = intent.getBooleanExtra("state", false)
            						||(Settings.System.getInt(context.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, -1) == 0);
                	Xlog.i(TAG, "airplane mode changed to "+mAllSimRadioOff);
            		mGprsSimSetting.setEnabled(!mAllSimRadioOff && mScreenEnable && mHasSim);
            		
	            	//MTK_OP02_PROTECT_START
					if (mHasNetworkMode == true) {
	
						boolean Sim1Ready = false;
	
						try{
							if ((mIsSlot1Insert)&&(iTelephony != null)) {
								Sim1Ready = iTelephony.isRadioOnGemini(Phone.GEMINI_SIM_1);
							}
						} catch (RemoteException e){
							Xlog.e(TAG, "iTelephony exception");
							return;
						}
						mNetworkMode.setEnabled(Sim1Ready);
						mNetworkModeGemini.setEnabled(Sim1Ready);
					}
	        	    //MTK_OP02_PROTECT_END

			} else if (action.equals(GeminiPhone.EVENT_3G_SWITCH_DONE)) {

				Xlog.i(TAG, "receiver: GeminiPhone.EVENT_3G_SWITCH_DONE");

    	        
    	        if(mIsShowDlg == 3) {
        	        mIsShowDlg = -1;
        	        if(isResumed()) {
        	        	removeDialog(DIALOG_3G_MODEM_SWITCHING);
        	        }

    	        }
				mIsModemSwitching = false;
				if(mStatusBarManager != null) {
					mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
					
				}

				updateVideoCallDefaultSIM();

			} else if (action.equals(GeminiPhone.EVENT_3G_SWITCH_LOCK_CHANGED)) {

				Xlog.i(TAG, "receiver: GeminiPhone.EVENT_3G_SWITCH_LOCK_CHANGED");

				boolean lockState = intent.getBooleanExtra(GeminiPhone.EXTRA_3G_SWITCH_LOCKED, false);
				if(mVTCallSupport){
				  mVideoCallSimSetting.setEnabled(!(mIs3gOff||lockState||(!mHasSim)));
				  Xlog.d(TAG,"mIs3gOff="+mIs3gOff+" lockState="+lockState+" mHasSim="+mHasSim);
				}
            } else if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)){
                //Get reason from this intent
                String reason = intent.getStringExtra(Phone.STATE_CHANGE_REASON_KEY);
                String apnTypeList = intent.getStringExtra(Phone.DATA_APN_TYPE_KEY);
                Phone.DataState state = getMobileDataState(intent);

                int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
                Xlog.i(TAG, "mDataConnectionReceiver simId is : " + simId);
                Xlog.i(TAG, "mDataConnectionReceiver state is : " + state);
                Xlog.i(TAG, "mDataConnectionReceiver reason is : " + reason);
                Xlog.i(TAG, "mDataConnectionReceiver apn type is : " + apnTypeList);
               
                if ((Phone.APN_TYPE_DEFAULT.equals(apnTypeList))&&(state == Phone.DataState.CONNECTED)&&(mIsGprsSwitching == true)) {
                    timerHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
        	        if(mIsShowDlg == 2) {
            	        mIsShowDlg = -1;
            	        if(isResumed()){
            	        	Xlog.i(TAG, "isResumed()"); 
            	        	removeDialog(DIALOG_WAITING);       	        	
            	        }
        	        }

                    updateGprsSettings();
                    mIsGprsSwitching = false;
                    	
                } else if((Phone.APN_TYPE_DEFAULT.equals(apnTypeList))&&(state == Phone.DataState.DISCONNECTED)&&(mIsGprsSwitching == true)) {
                    	
                    if(mGprsTargSim == false) {
                        timerHandler.removeMessages(EVENT_DETACH_TIME_OUT);
            	        if(mIsShowDlg == 2) {
                	        mIsShowDlg = -1;
                	        if(isResumed()){
                	        	removeDialog(DIALOG_WAITING);       	        	
                	        }
            	        }
                        updateGprsSettings();
                        mIsGprsSwitching = false;
                    }
                }

            } 
    	    //MTK_OP02_PROTECT_START
            else if (action.equals(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE)){

            	
				if (!intent.getBooleanExtra(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE, true)) {
					Xlog.i(TAG,"BroadcastReceiver: network mode change failed! restore the old value.");
					Settings.Secure.putInt(
									getContentResolver(),Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(GeminiUtils.OLD_NETWORK_MODE, 0));
				} else {
					Xlog.i(TAG,"BroadcastReceiver: network mode change succeed! set the new value.");
					Settings.Secure.putInt(
									getContentResolver(),Settings.Secure.PREFERRED_NETWORK_MODE,
									intent.getIntExtra(GeminiUtils.NEW_NETWORK_MODE, 0));
				}
		        mNetworkMode.updateSummary();
		        
		        if(mIsShowDlg == 4){
		        	mIsShowDlg = -1;
		        	if(isResumed()) {
		                removeDialog(DIALOG_NETWORK_MODE_CHANGE);
		        	}
		        }
                
			}
    	    //MTK_OP02_PROTECT_END
        }
	};
    
	protected void updateGprsSettings(){
		long dataconnectionID = Settings.System.getLong(getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
		Xlog.i(TAG, "dataconnectionID =" +dataconnectionID);
		
		if(dataconnectionID>0){
			Integer intIndex = mSimIdToIndexMap.get(dataconnectionID);
			if(intIndex == null){
				return;
			}
    		int index = intIndex.intValue();
    		SIMInfo siminfo = mSimMap.get(dataconnectionID);
    		if((index >=0)&&(siminfo != null)){
    			
        		mGprsSimSetting.setInitValue(index);
        		mGprsSimSetting.setSummary(siminfo.mDisplayName);

    		}
		} else if (dataconnectionID == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
			int nSim = mSimMap.size();
			mGprsSimSetting.setInitValue(nSim);
			mGprsSimSetting.setSummary(R.string.gemini_default_sim_never);

		}

	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MTK_OP02_PROTECT_START
        if((optr != null)&&(optr.equals("OP02"))) {
			mIsCU = true;
        }
        //MTK_OP02_PROTECT_END
        String baseband =  SystemProperties.get("gsm.baseband.capability");
        
        Xlog.i(TAG, "baseband is "+baseband);
        
        if((baseband != null)&&(baseband.length()!=0)&&(Integer.parseInt(baseband)>3)) {
        	m3gSupport = true;
        }
        if((!m3gSupport)||(!FeatureOption.MTK_VT3G324M_SUPPORT)) {
        	mVTCallSupport = false;
        }
        // For 3G data sms / 3G data only / WiFi only project
        isSmsCapable();
        isVoiceCapable();
        if(!sIsVoiceCapable){
        	mVTCallSupport = false;
        }      
    	//MTK_OP02_PROTECT_START
        mHasNetworkMode = mIsCU&&(TelephonyManager.getDefault().getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA);
    	//MTK_OP02_PROTECT_END
		int voipEnable = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.ENABLE_INTERNET_CALL, 0);
		mVoipAvailable = SipManager.isVoipSupported(getActivity()) && (voipEnable != 0);

        addPreferencesFromResource(R.xml.sim_management);

        
        mTelephonyManagerEx = TelephonyManagerEx.getDefault();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);		
		iTelephony = ITelephony.Stub.asInterface(ServiceManager
				.getService("phone"));
		mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);	
        mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        mIntentFilter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
        mIntentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntentFilter.addAction(TRANSACTION_START);
        mIntentFilter.addAction(TRANSACTION_STOP);
        mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_NAME_UPDATE);

        if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
    		mIntentFilter.addAction(GeminiPhone.EVENT_3G_SWITCH_DONE);
    		mIntentFilter.addAction(GeminiPhone.EVENT_3G_SWITCH_LOCK_CHANGED);       	
        }
        mSimMap = new HashMap<Long,SIMInfo>();
        mSimIdToIndexMap = new HashMap<Long, Integer>();

        mSimAndContacts = (PreferenceScreen)findPreference(KEY_SIM_CONTACTS_SETTINGS); 
        mVoiceCallSimSetting = (DefaultSimPreference) findPreference(KEY_VOICE_CALL_SIM_SETTING); 
        mSmsSimSetting = (DefaultSimPreference) findPreference(KEY_SMS_SIM_SETTING);  
        mGprsSimSetting = (DefaultSimPreference) findPreference(KEY_GPRS_SIM_SETTING);  	        
        mVideoCallSimSetting = (DefaultSimPreference) findPreference(KEY_VIDEO_CALL_SIM_SETTING);
       
        mGprsSimSetting.setType(GeminiUtils.TYPE_GPRS);  
        mGprsSimSetting.setOnPreferenceChangeListener(this);
        if(sIsVoiceCapable){
        	mVoiceCallSimSetting.setType(GeminiUtils.TYPE_VOICECALL);
        	mVoiceCallSimSetting.setOnPreferenceChangeListener(this);
        	mSmsSimSetting.setType(GeminiUtils.TYPE_SMS);
        	mSmsSimSetting.setOnPreferenceChangeListener(this);
        	if(mVideoCallSimSetting != null) {
        		if(mVTCallSupport){
                  mVideoCallSimSetting.setType(GeminiUtils.TYPE_VIDEOCALL);
                  mVideoCallSimSetting.setOnPreferenceChangeListener(this);
                  } else {
                	  PreferenceGroup defaultSIMSettingsCategory = (PreferenceGroup) findPreference(
                			  										KEY_DEFAULT_SIM_SETTINGS_CATEGORY);
                	  if(defaultSIMSettingsCategory != null) {
                		  defaultSIMSettingsCategory.removePreference(mVideoCallSimSetting);
                	  }
                  }
        	}
        }
        else{
        	PreferenceGroup useToRemove = (PreferenceGroup) findPreference(KEY_DEFAULT_SIM_SETTINGS_CATEGORY);
        	useToRemove.removePreference(mVoiceCallSimSetting);
        	useToRemove.removePreference(mVideoCallSimSetting);
        	if(!sIsSmsCapable){
        		useToRemove.removePreference(mSmsSimSetting);
        	}
	        else{
	            mSmsSimSetting.setType(GeminiUtils.TYPE_SMS);
        	    mSmsSimSetting.setOnPreferenceChangeListener(this);
           	}
        }
        //MTK_OP02_PROTECT_START
       //For CU customization
        PreferenceGroup GeneralSettingsCategory = (PreferenceGroup) findPreference(KEY_GENERAL_SETTINGS_CATEGORY);
        mNetworkMode = (NetworkModePreference) findPreference(KEY_NETWORK_MODE_SETTING);
        mNetworkModeGemini = (PreferenceScreen) findPreference(KEY_NETWORK_MODE_SETTING_GEMINI);
		m3gService = (PreferenceScreen) findPreference(KEY_3G_SERVICE_SETTING);
		if (!mHasNetworkMode) {
           if (GeneralSettingsCategory != null){
            	if (mNetworkMode != null) {
            		GeneralSettingsCategory.removePreference(mNetworkMode);
            	}
            	if(mNetworkModeGemini != null) {
                	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
            	}
            	if(m3gService != null) {
                	GeneralSettingsCategory.removePreference(m3gService);
            	}
            }
        } else { 
        	
        	if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
        		GeneralSettingsCategory.removePreference(mNetworkMode);
            	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
        	} else {
            	GeneralSettingsCategory.removePreference(m3gService);
            	try {
        			if(iTelephony != null) {	
        				mIsSlot1Insert = iTelephony.isSimInsert(Phone.GEMINI_SIM_1);
        				mIsSlot2Insert = iTelephony.isSimInsert(Phone.GEMINI_SIM_2);

        			}
            	} catch (RemoteException e){
    				Xlog.e(TAG, "iTelephony exception");
    				return;
    			}
            	if(mIsSlot1Insert == false) {
            		mNetworkMode.setEnabled(false);
                	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
            	} else if (mIsSlot2Insert == false) {
                    
                	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
                	mNetworkMode.setOnPreferenceChangeListener(this); 
                    mIntentFilter.addAction(GeminiUtils.NETWORK_MODE_CHANGE_RESPONSE);

            	} else {
            		GeneralSettingsCategory.removePreference(mNetworkMode);
            		String[] mTempCopy = getResources().getStringArray(R.array.gsm_umts_network_preferences_choices);

            		mNetworkModeSummary = new String[3];
            		mNetworkModeSummary[0] = mTempCopy[0];
            		mNetworkModeSummary[1] = mTempCopy[2];
            		mNetworkModeSummary[2] = mTempCopy[1];
            	}
            	removeNetworkMode();
        	}
        	
		}
        //MTK_OP02_PROTECT_END
		// For common load
		initSimMap();
        int nSimNum = mSimMap.size();
        if (nSimNum > 0) {
        	mHasSim = true;
        	registerMgrOnCreate();
        } else {
        	mHasSim = false;
        	setNoSimInfoUi();
        }
		mGprsDefaultSIMObserver = new ContentObserver(new Handler()){
            @Override
            public void onChange(boolean selfChange) {
                Xlog.i(TAG, "Gprs connection SIM changed");
    			long dataconnectionID = Settings.System.getLong(getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
    			updateDefaultSimValue(GeminiUtils.TYPE_GPRS, dataconnectionID);
            }
    	};
		if(savedInstanceState != null){
			Xlog.d(TAG,"saved instance not null ,means we need init default sim preference to avoid problems");
			initDefaultSimPreference();
		}
		IntentFilter intentfilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
		getActivity().registerReceiver(mSimInfoReceiver, intentfilter);
    }
	
	private void removeNetworkMode(){
		PreferenceGroup GeneralSettingsCategory = (PreferenceGroup) findPreference(KEY_GENERAL_SETTINGS_CATEGORY);
		if(GeneralSettingsCategory != null){
			if (mNetworkMode != null) {
	    		GeneralSettingsCategory.removePreference(mNetworkMode);
	    	}
	    	if(mNetworkModeGemini != null) {
	        	GeneralSettingsCategory.removePreference(mNetworkModeGemini);
	    	}
		}
	}
	
    private void registerMgrOnCreate() {
    	try {
    		if(iTelephony != null) {
    			iTelephony.registerForSimModeChange(mMessenger.getBinder(), EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE);
        		
    		}
    	} catch (RemoteException e){
    		Xlog.e(TAG, "iTelephony exception");
    		return;
    	}
	    getActivity().registerReceiver(mSimReceiver, mIntentFilter);
		addSimInfoPreference();
		if(mVTCallSupport&& (!FeatureOption.MTK_GEMINI_3G_SWITCH) && (!mIsSlot1Insert)) {
			mVideoCallSimSetting.setEnabled(false);
			Xlog.d(TAG,"mVideoCallSimSetting set disable");
		}  
        mCellConnMgr = new CellConnMgr(mServiceComplete);
        mCellConnMgr.register(getActivity());
        mGprsSimSetting.SetCellConnMgr(mCellConnMgr);  
    }
    private void setNoSimInfoUi(){
    	addNoSimIndicator();
    	getPreferenceScreen().setEnabled(false);
    	int voipEnable = android.provider.Settings.System.getInt(getContentResolver(),
								android.provider.Settings.System.ENABLE_INTERNET_CALL, 0);
    	if(sIsVoiceCapable){
    		if(voipEnable != 0){
    			mVoiceCallSimSetting.setEnabled(true);
    		}
    		else{
    			mVoiceCallSimSetting.setEnabled(false);
    		}
    	}
    }
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    if (mSimMap.size()>0) {
        	try {
        		if(iTelephony != null) {
            		iTelephony.unregisterForSimModeChange(mMessenger.getBinder());
        		}

        	} catch (RemoteException e){
        		Xlog.e(TAG, "iTelephony exception");
        		return;
        	}
	        getActivity().unregisterReceiver(mSimReceiver);
	        mCellConnMgr.unregister();
	    }
	    getActivity().unregisterReceiver(mSimInfoReceiver);
	    mDualSimMode = Settings.System.getInt(this.getContentResolver(),Settings.System.DUAL_SIM_MODE_SETTING, -1);        
        Xlog.i(TAG, "has attach msg = " + timerHandler.hasMessages(EVENT_ATTACH_TIME_OUT));	    
        Xlog.i(TAG, "has detach msg = " + timerHandler.hasMessages(EVENT_DETACH_TIME_OUT));	
        Xlog.i(TAG, "has sim mode msg = " + mDualSimModeChangedHander.hasMessages(EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE));	
	    timerHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
	    timerHandler.removeMessages(EVENT_DETACH_TIME_OUT);
	    mDualSimModeChangedHander.removeMessages(EVENT_DUAL_SIM_MODE_CHANGED_COMPLETE);
        Xlog.i(TAG, "onDestroy: mDualSimMode value is : " + mDualSimMode);
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		
		if(KEY_SIM_CONTACTS_SETTINGS.equals(preference.getKey())) {
    	    if (mSimMap.size() == 1) {
    	        for (Long simid: mSimMapKeyList) {
    	        	SIMInfo siminfo = mSimMap.get(simid);
        			if (siminfo != null) {
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.gemini.GeminiSIMTetherInfo");
                        
                        int slot = siminfo.mSlot;
                        
                        if(slot>=0) {
        	                intent.putExtra("simid", siminfo.mSimId);
        	                mSimAndContacts.setIntent(intent);
                        }

        			}
    	        }
    			

    	    } else {

    	    	Bundle extras = new Bundle();
    	    	extras.putInt("type", SimListEntrance.SIM_CONTACTS_SETTING_INDEX);
    			startFragment(this, SimListEntrance.class.getCanonicalName(), -1, extras, R.string.gemini_contacts_sim_title);
    			Xlog.i(TAG,"startFragment(this, SimListEntrance.class.getCanonicalName(), -1, extras);");
    	    }
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}


	@Override
	public void onStart() {	
	
		super.onStart();
        getListView().setItemsCanFocus(true);
	}
	@Override
	public void onResume() {
		super.onResume();
        Xlog.d(TAG,"onResume mIsShowDlg="+mIsShowDlg);
		int voipEnable = android.provider.Settings.System.getInt(getContentResolver(),
							android.provider.Settings.System.ENABLE_INTERNET_CALL, 0);
		mVoipAvailable = SipManager.isVoipSupported(getActivity()) && (voipEnable != 0);
		getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.GPRS_CONNECTION_SIM_SETTING),
                    								false, mGprsDefaultSIMObserver);
		if(mSimMap.size() >= 0) {
			setPreferenceProperty();
		}
		// deal with the problem that dialog fragment could not be dismissed after onSaveInstanceState
		switch(mIsShowDlg) {
		case 0:
			showDialog(DIALOG_ACTIVATE);
			setCancelable(false);
			break;
		case 1:
    		showDialog(DIALOG_DEACTIVATE);
    		setCancelable(false);
			break;
		case 2:
    		showDialog(DIALOG_WAITING);
    		setCancelable(false);
			break;
		case 3:
    		showDialog(DIALOG_3G_MODEM_SWITCHING);
    		setCancelable(false);
			break;
		case 4:
    		showDialog(DIALOG_NETWORK_MODE_CHANGE);
    		setCancelable(false);
			break;
		default:
			break;
		}
		initDefaultSimPreference();
	}

	


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// deal with the problem that dialog fragment could not be dismissed after onSaveInstanceState
                Xlog.d(TAG,"onPause   mIsShowDlg="+mIsShowDlg);

		switch(mIsShowDlg) {
		case 0:
			removeDialog(DIALOG_ACTIVATE);
			break;
		case 1:
			removeDialog(DIALOG_DEACTIVATE);
			break;
		case 2:
			removeDialog(DIALOG_WAITING);
			break;
		case 3:
			removeDialog(DIALOG_3G_MODEM_SWITCHING);
			break;
		case 4:
			removeDialog(DIALOG_NETWORK_MODE_CHANGE);
			break;
		default:
			break;
		}
		getContentResolver().unregisterContentObserver(mGprsDefaultSIMObserver);
	    //MTK_OP02_PROTECT_START
		if (mHasNetworkMode) {
			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}
	    //MTK_OP02_PROTECT_END
        


	}


	private void updateDefaultSIMSummary(DefaultSimPreference pref, Long simid) {
		
		if(simid>0) {
        	SIMInfo siminfo = mSimMap.get(simid);
        	
        	if(siminfo!=null) {
            	pref.setSummary(siminfo.mDisplayName);      		
        	}

		} else if (simid == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
			pref.setSummary(R.string.gemini_intenet_call);
		} else if (simid == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
			pref.setSummary(R.string.gemini_default_sim_always_ask);
		} else if (simid == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
			pref.setSummary(R.string.gemini_default_sim_never);
		}
			
	}
    

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        Xlog.i(TAG, "Enter onPreferenceChange function.");
        
        final String key = arg0.getKey();
        // TODO Auto-generated method stub
        if (KEY_VOICE_CALL_SIM_SETTING.equals(key)) {
        	Settings.System.putLong(getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, (Long)arg1);
        	
            Intent intent = new Intent(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            intent.putExtra("simid", (Long)arg1);
			getActivity().sendBroadcast(intent);
            updateDefaultSIMSummary(mVoiceCallSimSetting,(Long)arg1);
		} else if (KEY_VIDEO_CALL_SIM_SETTING.equals(key)) {
			if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
				
				mVTTargetTemp = mVideoCallSimSetting.getValue();
				showDialog(DIALOG_3G_MODEM_SWITCH_CONFIRM);
		    	setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							updateVideoCallDefaultSIM();
						}
					} 
		    	 );
			}
        } else if (KEY_SMS_SIM_SETTING.equals(key)) {
        	
        	Settings.System.putLong(getContentResolver(), Settings.System.SMS_SIM_SETTING, (Long)arg1);
        	
            Intent intent = new Intent(Intent.ACTION_SMS_DEFAULT_SIM_CHANGED);
            
            intent.putExtra("simid", (Long)arg1);      	
            getActivity().sendBroadcast(intent);
            updateDefaultSIMSummary(mSmsSimSetting,(Long)arg1);
        	
        } else if (KEY_GPRS_SIM_SETTING.equals(key)) {
        	
			long value = ((Long) arg1).longValue();
			
			if(value == 0) {
				switchGprsDefautlSIM(value);
				return true;
			}

			SIMInfo siminfo = mSimMap.get(value);
			
			if(siminfo == null)
				return false;
						
			boolean isInRoaming = mTelephonyManager.isNetworkRoamingGemini(siminfo.mSlot);
			mDataSwitchMsgIndex = -1;
			if(isInRoaming) {
				boolean isRoamingDataAllowed = (siminfo.mDataRoaming == SimInfo.DATA_ROAMING_ENABLE);
				if(isRoamingDataAllowed) {
					if((siminfo.mSlot != GeminiUtils.m3GSlotID)&&(FeatureOption.MTK_GEMINI_3G_SWITCH)) {
						mDataSwitchMsgIndex=mIsCU?2:1;
					}
				} else {
					if((mIs3gOff)||(!mIs3gOff&&(siminfo.mSlot == GeminiUtils.m3GSlotID))||(!FeatureOption.MTK_GEMINI_3G_SWITCH)) {
						mDataSwitchMsgIndex=0;
					} else if((siminfo.mSlot != GeminiUtils.m3GSlotID)&&(FeatureOption.MTK_GEMINI_3G_SWITCH))
						mDataSwitchMsgIndex=mIsCU?4:3;
				}
			} else {
				if((siminfo.mSlot != GeminiUtils.m3GSlotID)&&(FeatureOption.MTK_GEMINI_3G_SWITCH)) {
					mDataSwitchMsgIndex=mIsCU?2:1;
				}
			}

			if(mDataSwitchMsgIndex == -1) {
				
				switchGprsDefautlSIM(value);
				
			} else {
				showDialog(DIALOG_GPRS_SWITCH_CONFIRM);
		    	setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						updateGprsSettings();
					}
				} 
	    	 );
			}

        } 
	    //MTK_OP02_PROTECT_START
        else if(KEY_NETWORK_MODE_SETTING.equals(key)) {

        	
            int oldNetworkMode = Settings.Secure.getInt(
                    getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
            
    		int newNetworkMode = Integer.valueOf((String) arg1).intValue();
    		
    		newNetworkMode = GeminiUtils.getNetworkMode(newNetworkMode);
    		
    		Settings.Secure.putInt(getContentResolver(),
    				Settings.Secure.PREFERRED_NETWORK_MODE,
    				newNetworkMode);	
    		
    		if(newNetworkMode != oldNetworkMode) {
    			
                Intent intent = new Intent(GeminiUtils.NETWORK_MODE_CHANGE_BROADCAST,
                        null);
                intent.putExtra(GeminiUtils.OLD_NETWORK_MODE, oldNetworkMode);
                intent.putExtra(GeminiUtils.NETWORK_MODE_CHANGE_BROADCAST, newNetworkMode);
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
       			showDialog(DIALOG_NETWORK_MODE_CHANGE);
       			mIsShowDlg = 4;
       			setCancelable(false);
       			getActivity().sendBroadcast(intent);
    			
    		}
        
        	
        }
	    //MTK_OP02_PROTECT_END
        return true;
    }
    
    private void showProgressDlg(boolean isActivating){
        if(true == isActivating)
        {
        	if(false == getActivity().isFinishing()){
            	Xlog.i(TAG,"DIALOG_ACTIVATE");

        		showDialog(DIALOG_ACTIVATE);
        		mIsShowDlg = 0;
        		setCancelable(false);
        	}else{
        		Xlog.i(TAG, "Activity isFinishing, state error......");
        	}
        }
        else
        {
        	if(false == getActivity().isFinishing()){
            	Xlog.i(TAG,"DIALOG_DEACTIVATE");

        		showDialog(DIALOG_DEACTIVATE);
        		mIsShowDlg = 1;
        		setCancelable(false);
        	}else{
        		Xlog.i(TAG, "Activity isFinishing, state error......");
        	}
        }
    }
    

    @Override
	public Dialog onCreateDialog(int id) {
    	  ProgressDialog dialog = new ProgressDialog(getActivity());
    	  Builder builder = new AlertDialog.Builder(getActivity());
		    AlertDialog alertDlg;
    	  switch (id) {
            case DIALOG_ACTIVATE:                
                dialog.setMessage(getResources().getString(R.string.gemini_sim_mode_progress_activating_message));
                dialog.setIndeterminate(true);
                return dialog;
                    
            case DIALOG_DEACTIVATE:               
                dialog.setMessage(getResources().getString(R.string.gemini_sim_mode_progress_deactivating_message));
                dialog.setIndeterminate(true);
                return dialog;
                    

		         case DIALOG_WAITING:
 		        dialog.setMessage(getResources().getString(R.string.gemini_data_connection_progress_message));
		        dialog.setIndeterminate(true);
		        return dialog;    
		             
		    case DIALOG_NETWORK_MODE_CHANGE:                
			    dialog.setMessage(getResources().getString(R.string.gemini_data_connection_progress_message));
			    dialog.setIndeterminate(true);
			    return dialog; 
			    
		     case DIALOG_GPRS_SWITCH_CONFIRM:
		    	 builder.setTitle(android.R.string.dialog_alert_title);
		    	 builder.setIcon(android.R.drawable.ic_dialog_alert);
		    	 
		    	 if((mDataSwitchMsgIndex>=0)&&(mDataSwitchMsgIndex<=4)) {
			    	 builder.setMessage(getResources().getString(mDataSwitchMsgStr[mDataSwitchMsgIndex]));
 
		    	 }

		    	 builder.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// TODO Auto-generated method stub
									// use to judge whether the click is correctly done!
									
									if((mDataSwitchMsgIndex == 0)|(mDataSwitchMsgIndex == 3)||(mDataSwitchMsgIndex == 4)){
										enableDataRoaming(mGprsSimSetting.getValue());
									}
									switchGprsDefautlSIM(mGprsSimSetting.getValue());
									
								}
							});
				builder.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// TODO Auto-generated method stub
									// use to judge whether the click is correctly done!
									updateGprsSettings();
								}
							});
		    	 alertDlg = builder.create();

		    	 return alertDlg;

		     case DIALOG_3G_MODEM_SWITCH_CONFIRM:
		    	 builder.setTitle(android.R.string.dialog_alert_title);
		    	 builder.setIcon(android.R.drawable.ic_dialog_alert);
		    	 builder.setMessage(getResources().getString(R.string.gemini_3g_modem_switch_confirm_message));
		    	 builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// TODO Auto-generated method stub
							// use to judge whether the click is correctly done!
							switchVideoCallDefaultSIM(mVTTargetTemp);
							
						}
					});
		    	 builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// TODO Auto-generated method stub
							// use to judge whether the click is correctly done!
							updateVideoCallDefaultSIM();
						}
					});

		    	 alertDlg = builder.create();
		    	 return alertDlg;
			
		     case DIALOG_3G_MODEM_SWITCHING:

		    	 dialog.setMessage(getResources().getString(
		    			 R.string.gemini_3g_modem_switching_message));
		    	 dialog.setIndeterminate(true);
		    	 Window win = dialog.getWindow();
		    	 WindowManager.LayoutParams lp = win.getAttributes();
		    	 lp.flags |= WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;
		    	 win.setAttributes(lp);
		    	 return dialog;

           default:
               return null;
        }
    }
    
    private void addSimInfoPreference () {
    	
        PreferenceGroup SimInfoListCategory = (PreferenceGroup) findPreference(
        		KEY_SIM_INFO_CATEGORY);
        
        if(SimInfoListCategory == null)
        	return;
        SimInfoListCategory.removeAll();
        

        
        for (Long simid: mSimMapKeyList) {
        	SIMInfo siminfo = mSimMap.get(simid);
        	
        	if (siminfo == null) {
        		break;
        	}

        	Xlog.i(TAG, "siminfo.mDisplayName = " +siminfo.mDisplayName);
        	Xlog.i(TAG, "siminfo.mNumber = " +siminfo.mNumber);
        	Xlog.i(TAG, "siminfo.mSlot = " +siminfo.mSlot);
        	Xlog.i(TAG, "siminfo.mColor = " +siminfo.mColor);
        	Xlog.i(TAG, "siminfo.mDispalyNumberFormat = " +siminfo.mDispalyNumberFormat);
        	Xlog.i(TAG, "siminfo.mSimId = " +siminfo.mSimId);
        	
        	
        	int status = mTelephonyManagerEx.getSimIndicatorStateGemini(siminfo.mSlot);
        	SimInfoEnablePreference simInfoPref = new SimInfoEnablePreference(getActivity(), siminfo.mDisplayName,
        			siminfo.mNumber, siminfo.mSlot, status, siminfo.mColor, 
        			siminfo.mDispalyNumberFormat, siminfo.mSimId);
        	
        	Xlog.i(TAG, "simid status is  "+status);
        	
        	if (simInfoPref != null) {
        		simInfoPref.setClickCallback(this);
 				if (iTelephony != null) {
					try {
						boolean isRadioOn = iTelephony.isRadioOnGemini(siminfo.mSlot);
						simInfoPref.setCheck(isRadioOn);
						simInfoPref.setRadioOn(isRadioOn);
					} catch (RemoteException e){
						Xlog.e(TAG, "iTelephony exception");

					}

				}
        		if (siminfo.mSlot==Phone.GEMINI_SIM_1) {
        			mIsSlot1Insert = true;
        			mSlot1SimPref = simInfoPref;
        			simInfoPref.SetCheckBoxClickListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
					Xlog.i(TAG, "receive sim1 click intent!");
		                	boolean bChecked = mSlot1SimPref.getCheck();			                
		                	if(mIsSIMModeSwitching == true) {
		                		Xlog.i(TAG, "mIsSIMModeSwitching == true");
			                	mSlot1SimPref.setCheck(bChecked);
		                		return;
		                	} else {
		                		mIsSIMModeSwitching = true;
		                		Xlog.i(TAG, "set mIsSIMModeSwitching true");
			                	mSlot1SimPref.setCheck(!bChecked);
		                	}
                   			dealSim1Change(); 
					}
				});
        		} else if (siminfo.mSlot==Phone.GEMINI_SIM_2) {
        			mIsSlot2Insert = true;
        			mSlot2SimPref = simInfoPref;
				//it will switch to airplane mode in the following case
				simInfoPref.SetCheckBoxClickListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
			    	@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Xlog.i(TAG, "receive sim2 click intent!");
			                boolean bChecked = mSlot2SimPref.getCheck();	
			                if(mIsSIMModeSwitching == true) {
			                	Xlog.i(TAG, "mIsSIMModeSwitching == true");
				                mSlot2SimPref.setCheck(bChecked);
			                	return;
			                } else {
			                	mIsSIMModeSwitching = true;
			                	Xlog.i(TAG, "set mIsSIMModeSwitching true");
				                mSlot2SimPref.setCheck(!bChecked);
			                }
			                dealSim2Change();
					}
				});
        		}
        		SimInfoListCategory.addPreference(simInfoPref);
        	}
        
        }

    }
    
    private void addNoSimIndicator () {
    	
    	
        PreferenceGroup SimInfoListCategory = (PreferenceGroup) findPreference(
        		KEY_SIM_INFO_CATEGORY);
        
        Preference pref = new Preference(getActivity());
        
        if (pref!=null) {
            pref.setTitle(R.string.gemini_no_sim_indicator);
            SimInfoListCategory.addPreference(pref);       	
        }

    }
    
       private void dealSim1Change(){
    	


    	
        mDualSimMode = Settings.System.getInt(this.getContentResolver(),
                Settings.System.DUAL_SIM_MODE_SETTING, -1);
        
        Xlog.i(TAG, "dealSim1Change mDualSimMode value is : " + mDualSimMode);
        
        Intent intent;
        Xlog.i(TAG, "mIsSlot1Insert = "+mIsSlot1Insert+"; mIsSlot2Insert ="+mIsSlot2Insert);       
        //see if it is airplane mode, if yes
        if(1 == Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1)){
            Xlog.i(TAG, "airplane mode is on");    	
            //two sim insert, change to sim1 only
            if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
            {                

                Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 1);
                intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                getActivity().sendBroadcast(intent);

                  
                showProgressDlg(true);
            }
            //sim1 insert, change to sim1 only
            else if((true == mIsSlot1Insert) && (false == mIsSlot2Insert))
            {
 
                Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 1);
                intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                getActivity().sendBroadcast(intent);

                  
                showProgressDlg(true);
            }            
            return;
        }
  
        switch(mDualSimMode){
        	case 0: {
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 1);
                intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 1);
                getActivity().sendBroadcast(intent);
                 
                showProgressDlg(true);
                break;
        	}
        	
            case 1: {
            	
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 0);
                intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 0);
                getActivity().sendBroadcast(intent);

                showProgressDlg(false);
                break;
            }
            case 2: {

                //two sim insert, change to dual sim mode
                if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
                {
                    Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 3);
                    intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                    intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 3);
                    getActivity().sendBroadcast(intent);
                 
                    showProgressDlg(true);
                }
                //sim1 insert, change to sim1 only, sim2 can not used any more
                else if((true == mIsSlot1Insert) && (false == mIsSlot2Insert))
                {
                    Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 1);
                    intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                    intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 1);
                    getActivity().sendBroadcast(intent);

                        showProgressDlg(true);
                    }
            	

                break;
            }

            case 3:
                //two sim insert, change to sim2 only
                if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
                {
 
                    Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 2);
                    intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                    intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 2);
                    getActivity().sendBroadcast(intent);
 

                    showProgressDlg(false);
                }

                break;
            default:
                Xlog.i(TAG, "dual sim mode error.");
                break;
        }
        
    }
    

	private void dealSim2Change(){
    	
        mDualSimMode = Settings.System.getInt(this.getContentResolver(),
                Settings.System.DUAL_SIM_MODE_SETTING, -1);
        
        Xlog.i(TAG, "dealSim2Change mDualSimMode value is : " + mDualSimMode);
        
        Intent intent;
        
        //see if it is airplane mode, if yes
        if(1 == Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1)){
            //two sim insert, change to sim2 only
            if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
            {
                Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 2);
                intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                getActivity().sendBroadcast(intent);
                showProgressDlg(true);
            }

            //sim2 insert, change to sim2 only
            else if((false == mIsSlot1Insert) && (true == mIsSlot2Insert))
            {
                Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 2);
                intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                getActivity().sendBroadcast(intent);
                   
                showProgressDlg(true);
            }
            
            return;
        }
        switch(mDualSimMode){
    		case 0: {
    			Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 2);
    			intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
    			intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 2);
    			getActivity().sendBroadcast(intent);
             
    			showProgressDlg(true);
    			break;
    		}
            case 1: {

                //two sim insert, change to dual sim mode
                if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
                {
                    Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 3);
                    intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                    intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 3);
                    getActivity().sendBroadcast(intent);
                      
                        showProgressDlg(true);
                    }
                //sim2 insert, change to sim2 only mode and sim1 can not be used any more
                else if((false == mIsSlot1Insert) && (true == mIsSlot2Insert))
                {
                        Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 2);
                        intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                        intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 2);
                        getActivity().sendBroadcast(intent);
                      
                        showProgressDlg(true);
                    }
     		
            	

                break;
            }

            case 2: {
                Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 0);
                intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 0);
                getActivity().sendBroadcast(intent);

                showProgressDlg(false);
                break;
            }

            case 3: {
                //two sim insert, change to sim1 only
                if((true == mIsSlot1Insert) && (true == mIsSlot2Insert))
                {
                    Settings.System.putInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, 1);
                    intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                    intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, 1);
                    getActivity().sendBroadcast(intent);
                   
                    showProgressDlg(false);
                }
                break;
            }
            default:
                Xlog.i(TAG, "dual sim mode error.");
                break;
        }        
    }

	    
	    private void dealWithSwtichComplete(){
	    	
	        
	        for (Long simid: mSimMapKeyList) {
	        	SIMInfo siminfo = mSimMap.get(simid);

	        	
		         SimInfoEnablePreference simInfoPref = (SimInfoEnablePreference) findPreference(String.valueOf(siminfo.mSimId)); 
		         
			if ((simInfoPref != null)&&(iTelephony != null)) {

				try {
					boolean newState = iTelephony.isRadioOnGemini(siminfo.mSlot);
					boolean oldState = simInfoPref.isRadioOn();
					simInfoPref.setRadioOn(newState);

					simInfoPref.setCheck(newState);
		        	 //MTK_OP02_PROTECT_START

					if ((mHasNetworkMode == true)
							&& (siminfo.mSlot == Phone.GEMINI_SIM_1)) {
						mNetworkMode.setEnabled(newState);
						mNetworkModeGemini.setEnabled(newState);
					}
		        	 //MTK_OP02_PROTECT_END
					Xlog.i(TAG, "mIsSIMModeSwitching is " + mIsSIMModeSwitching
							+ " newState is " + newState + " oldState is "
							+ oldState);
				} catch (RemoteException e){
					Xlog.e(TAG, "iTelephony exception");
					return;
				}


			}
		        
	        	
	        }

            Xlog.i(TAG, "next will remove the progress dlg+mIsShowDlg="+mIsShowDlg);
	        

	        if((mIsShowDlg == 0)||(mIsShowDlg == 1)) {
                boolean isResumed = isResumed();
                Xlog.d(TAG,"isResumed="+isResumed);
                if(isResumed) {
		        	removeDialog((mIsShowDlg == 0)?DIALOG_ACTIVATE:DIALOG_DEACTIVATE);
		        }
		        mIsShowDlg = -1; 
	        }
	        
    

	        //switch
	        if(false == mIsSIMModeSwitching){
	        	Xlog.i(TAG, "mIsSIMModeSwitching value error......");
	        }
	        mIsSIMModeSwitching = false;
	        Xlog.e(TAG,"mIsSIMModeSwitching is "+mIsSIMModeSwitching);
	        
	        
	        mAllSimRadioOff = (Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1)==1)
						||(Settings.System.getInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, -1) == 0);

	        mGprsSimSetting.setEnabled(!mAllSimRadioOff && mScreenEnable && mHasSim);
	

	    }

	    
	    private void dealwithAttach(){
	    	mIsGprsSwitching = true;
	        timerHandler.sendEmptyMessageDelayed(EVENT_ATTACH_TIME_OUT, ATTACH_TIME_OUT_LENGTH);
            showDialog(DIALOG_WAITING);
            mIsShowDlg = 2;
            setCancelable(false);

	    }
	    
	    private void dealwithDetach(){
	    	mIsGprsSwitching = true;
	        timerHandler.sendEmptyMessageDelayed(EVENT_DETACH_TIME_OUT, DETACH_TIME_OUT_LENGTH);
            showDialog(DIALOG_WAITING);
            mIsShowDlg = 2;
            setCancelable(false);
	    }
	    
	    private static Phone.DataState getMobileDataState(Intent intent) {
	        String str = intent.getStringExtra(Phone.STATE_KEY);
	        if (str != null) {
	            return Enum.valueOf(Phone.DataState.class, str);
	        } else {
	            return Phone.DataState.DISCONNECTED;
	        }
	    }
	    
	    private Handler timerHandler = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case EVENT_DETACH_TIME_OUT:
	                    Xlog.i(TAG, "detach time out......");


	        	        if(mIsShowDlg == 2) {

		        	        mIsShowDlg = -1;

		        	        if(isResumed()) {
		        	        	removeDialog(DIALOG_WAITING);
		        	        }
	        	        }
                        updateGprsSettings();
//                        Toast.makeText(mContext, getString(R.string.data_connection_detach_timeout_error_msg), Toast.LENGTH_LONG).show();
                        mIsGprsSwitching = false;
	                    break;
	                case EVENT_ATTACH_TIME_OUT:
	                    Xlog.i(TAG, "attach time out......");

	        	        if(mIsShowDlg == 2) {

		        	        mIsShowDlg = -1;

		        	        if(isResumed()) {
		        	        	removeDialog(DIALOG_WAITING);
		        	        }
	        	        }
                        updateGprsSettings();

//	                    Toast.makeText(mContext, getString(R.string.data_connection_attach_timeout_error_msg), Toast.LENGTH_LONG).show();
                        mIsGprsSwitching = false;
	                    break;
	            }
	        }
	    };
	    //MTK_OP02_PROTECT_START
	    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
	    	@Override
	    	public void onCallStateChanged(int state, String incomingNumber) {
	    		super.onCallStateChanged(state, incomingNumber);
	    		Xlog.i(TAG,"onCallStateChanged ans state is "+state);
	    		switch(state){
	    		case TelephonyManager.CALL_STATE_IDLE: {
	    			if (isCallStateIdle() == true) {

	    				boolean Sim1Ready = false;

	    				if ((mIsSlot1Insert) && (iTelephony != null)) {
	    					try {
	    						Sim1Ready = iTelephony.isRadioOnGemini(Phone.GEMINI_SIM_1);
	    					} catch (RemoteException e){
	    						Xlog.e(TAG, "iTelephony exception");
	    						return;
	    					}

	    				}

					mNetworkMode.setEnabled(Sim1Ready);
					mNetworkModeGemini.setEnabled(Sim1Ready);

	    			}
	    		}
					break;
	    		default:
	    			break;

			}
		}
	};
	    //MTK_OP02_PROTECT_END
	    
	    protected void initDefaultSimPreference () {
	    	
	        //initialize the default sim preferences
        	mSimItemListVoice.clear();
        	mSimItemListSms.clear();
        	mSimItemListGprs.clear();

        		
	        mSimItemListVideo.clear();	    	

	    	SimItem simitem;

	    	int k=0;
    	
	        for (Long simid: mSimMapKeyList) {
	        	SIMInfo siminfo = mSimMap.get(simid);
	        	
	        	if (siminfo != null) {
	        		
	        		simitem = new SimItem(siminfo);
		        	int state = mTelephonyManagerEx.getSimIndicatorStateGemini(siminfo.mSlot);
		        	simitem.mState = state;
		        	
		        	
//		        	Xlog.i(TAG, "!!!!!!!!!!!!!!simitem.mState is " +simitem.mState);

		        	mSimItemListVoice.add(simitem);
		        	mSimItemListSms.add(simitem);
		        	mSimItemListGprs.add(simitem);
		        	if (mVTCallSupport) {
		        		
		        		if((siminfo.mSlot == Phone.GEMINI_SIM_1)
		        				||(FeatureOption.MTK_GEMINI_3G_SWITCH)){
				        	mSimItemListVideo.add(simitem);
		        		} 
		        	}
		    		mSimIdToIndexMap.put(Long.valueOf(siminfo.mSimId), k);
		        	

	        	}
	        	
	        	k++;
	        	
	        }
	        
		if (mVoipAvailable == true) {
		        simitem = new SimItem (this.getString(R.string.gemini_intenet_call), 8, Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
		        mSimItemListVoice.add(simitem);
	        }
	        

		    simitem = new SimItem (this.getString(R.string.gemini_default_sim_always_ask), -1, Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK);
		    mSimItemListVoice.add(simitem);  
	        mSimItemListSms.add(simitem);        	

       	
	        simitem = new SimItem (this.getString(R.string.gemini_default_sim_never), -1, Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER);
	        mSimItemListGprs.add(simitem);	
	        Xlog.i(TAG, "mSimItemListVoice size is "+mSimItemListVoice.size());
	        Xlog.i(TAG, "mSimItemListVideo size is "+mSimItemListVideo.size());
	        Xlog.i(TAG, "mSimItemListSms size is "+mSimItemListSms.size());
	        Xlog.i(TAG, "mSimItemListGprs size is "+mSimItemListGprs.size());
            //mVoiceCallSimSetting.setInitData(mSimItemListVoice); 
            mSmsSimSetting.setInitData(mSimItemListSms);    
	    
	      if(sIsVoiceCapable){
	        mVoiceCallSimSetting.setInitData(mSimItemListVoice);
	      }
              if(mVTCallSupport == true){
                  mVideoCallSimSetting.setInitData(mSimItemListVideo);	
              }
            mGprsSimSetting.setInitData(mSimItemListGprs);	            
   	        
	    }
	    

	    
	    protected void updateSimInfo(long simID, int type) {
	    	SIMInfo siminfo = SIMInfo.getSIMInfoById(getActivity(), simID);
	    	
	    	if (siminfo != null) {
	    		mSimMap.put(Long.valueOf(simID), siminfo);
	    		SimInfoEnablePreference pref = (SimInfoEnablePreference)findPreference(String.valueOf(simID));
	    		if(pref == null) {
	    			return;
	    		}
	    		switch (type) {
	    		case 0:
	    			pref.setName(siminfo.mDisplayName);
	    			return;
	    		case 1:
	    			pref.setColor(siminfo.mColor);
	    			return;
	    		case 2:
	    			pref.setNumber(siminfo.mNumber);
	    			return;
	    		case 3:
	    			pref.setNumDisplayFormat(siminfo.mDispalyNumberFormat);
	    			return;
	    		}
	    	}
                sortSimMap();
	    }
	    
	    protected void updateSimState(int slotID, int state) {
	    	SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(), slotID);
	    	
	    	if (siminfo != null) {
	    		
	    		SimInfoEnablePreference pref = (SimInfoEnablePreference)findPreference(String.valueOf(siminfo.mSimId));
	    		if (pref == null) {
	    			Xlog.i(TAG, "simid status of sim "+siminfo.mSimId + "is  "+state+ " pref is null");
	    			return;
	    		}
	    		pref.setStatus(state);
	        	Xlog.i(TAG, "simid status of sim "+siminfo.mSimId + "is  "+state);

	    	}
	    }
	    

	protected void updateDefaultSimState(int slotID, int state) {

    	SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(), slotID);
    	
    	if (siminfo != null) {
    		
			Integer intIndex = mSimIdToIndexMap.get(Long.valueOf(siminfo.mSimId));
			if(intIndex == null){
				return;
			}
    		int index = intIndex.intValue();

    		
    		Xlog.i(TAG, "index is" +index);
			SimItem simitem = new SimItem(siminfo);
			simitem.mState = state;
			updateDefaultSimItemList(index, simitem, (slotID == Phone.GEMINI_SIM_1)?true:false);

        	Xlog.i(TAG, "simid status of sim "+siminfo.mSimId + "is  "+state);

        	
    	}


	}
	    	

	    
	    protected void updateDefaultSimInfo(long simID) {
	    	
	    	SIMInfo siminfo = SIMInfo.getSIMInfoById(getActivity(), simID);
	    	
	    	if (siminfo != null) {
	    		
				Integer intIndex = mSimIdToIndexMap.get(siminfo.mSimId);
				if(intIndex == null){
					return;
				}
	    		int index = intIndex.intValue();
	    		
	    		
				SimItem simitem = new SimItem(siminfo);
	        	int state = mTelephonyManagerEx.getSimIndicatorStateGemini(siminfo.mSlot);

				simitem.mState = state;
				updateDefaultSimItemList(index, simitem, (siminfo.mSlot == Phone.GEMINI_SIM_1)?true:false);


	        	Xlog.i(TAG, "simid status of sim "+siminfo.mSimId + "is  "+state);
	    }
	}

	    protected void updateDefaultSimItemList(int index, SimItem simitem, boolean Slot3g){
	    	mSimItemListVoice.set(index, simitem);
	    	SimItem item = mSimItemListVoice.get(index);
	    	Xlog.i(TAG, "item state is "+item.mState);
	    	//mVoiceCallSimSetting.SetData(mSimItemListVoice);
	    	mSimItemListSms.set(index, simitem);
			mSmsSimSetting.SetData(mSimItemListSms);
			mSimItemListGprs.set(index, simitem);	
			mGprsSimSetting.SetData(mSimItemListGprs);
			if(sIsVoiceCapable){
			  mVoiceCallSimSetting.SetData(mSimItemListVoice);
			}
			Xlog.i(TAG, "mVTCallSupport="+mVTCallSupport+" FeatureOption.MTK_GEMINI_3G_SWITCH="
										 +FeatureOption.MTK_GEMINI_3G_SWITCH+" mSimItemListVideo.size()="
										 +mSimItemListVideo.size());
			if (mVTCallSupport) {
				if(!FeatureOption.MTK_GEMINI_3G_SWITCH){
					if(Slot3g && mSimItemListVideo.size()>0) {
						mSimItemListVideo.set(0, simitem);					
					}
				} else {
					if (mSimItemListVideo.size()>0)
						mSimItemListVideo.set(index, simitem);
				}
				mVideoCallSimSetting.SetData(mSimItemListVideo);
			}
		}
	    
	private void updateDefaultSimValue(int type, long simId) {

		if (simId < Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
			return;
		}

		if (simId == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {

			if (type == GeminiUtils.TYPE_GPRS) {
				mGprsSimSetting.setInitValue(mSimMap.size());
				mGprsSimSetting.setSummary(R.string.gemini_default_sim_never);
			}
		} else {
			
			Integer intIndex = mSimIdToIndexMap.get(simId);
			if(intIndex == null){
				return;
			}
    		int index = intIndex.intValue();

			if (index < 0) {
				return;
			}

			SIMInfo siminfo = SIMInfo.getSIMInfoById(getActivity(), simId);

			if (siminfo == null) {
				return;
			}
			if (type == GeminiUtils.TYPE_GPRS) {

				mGprsSimSetting.setInitValue(index);
				mGprsSimSetting.setSummary(siminfo.mDisplayName);
			}
		}

	}

	/**
	 * update video call default SIM value and summary
	 */

	private void updateVideoCallDefaultSIM() {
		Xlog.d(TAG,"updateVideoCallDefaultSIM()+mVTCallSupport="+mVTCallSupport);
		if (iTelephony != null) {

			try {
				int videocallSlotID = iTelephony.get3GCapabilitySIM();
				Xlog.d(TAG,"updateVideoCallDefaultSIM()---videocallSlotID="+videocallSlotID);
				GeminiUtils.m3GSlotID = videocallSlotID;
				
				if (videocallSlotID < 0)
					return;

				SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(getActivity(), videocallSlotID);

				if (siminfo != null) {
					Integer intIndex = mSimIdToIndexMap.get(siminfo.mSimId);
					Xlog.d(TAG,"updateVideoCallDefaultSIM()---intIndex="+intIndex);
					if (intIndex == null) {
						return;
					}
					int index = intIndex.intValue();
					Xlog.d(TAG,"updateVideoCallDefaultSIM()---index="+index);
					if ((index >= 0) && (siminfo != null)) {
					  if(mVTCallSupport){
						mVideoCallSimSetting.setInitValue(index);
						mVideoCallSimSetting.setSummary(siminfo.mDisplayName);
					  }
					}
				} else {
					  if(mVTCallSupport){
						Xlog.d(TAG,"mVideoCallSimSetting.setInitValue(-1)");
						mVideoCallSimSetting.setInitValue(-1);
					  }
				}
			} catch (RemoteException e){
				Xlog.e(TAG, "iTelephony exception");
				return;
			}


		}
	}

	    private void initSimMap() {
	    	List<SIMInfo> simList = SIMInfo.getInsertedSIMList(getActivity());
	    	mSimMap.clear();
	    	Xlog.i(TAG, "sim number is "+simList.size());
	    	for (SIMInfo siminfo:simList) {
	    		mSimMap.put(Long.valueOf(siminfo.mSimId), siminfo);
	    	}
	    	sortSimMap();
	    }
	  
	  private void sortSimMap(){
              if(mSimMap == null){
	          return; 
	      }

	      mSimMapKeyList = (List<Long>)(new ArrayList(mSimMap.keySet()));
	      if(mSimMap.size() < 2){
	          return; 
	      }

	      Long a = mSimMapKeyList.get(0);
	      Long b = mSimMapKeyList.get(1);
	      if(mSimMap.get(a).mSlot > mSimMap.get(b).mSlot){
	          mSimMapKeyList.clear();
		  mSimMapKeyList.add(b);
		  mSimMapKeyList.add(a);
	      }
	  } 
	    
	    //MTK_OP02_PROTECT_START
	    private boolean isCallStateIdle(){
	    	
	    	int stateSim1 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_1);
	    	int stateSim2 = mTelephonyManager.getCallStateGemini(Phone.GEMINI_SIM_2);
	    	Xlog.i(TAG,"stateSim1 is "+stateSim1+" stateSim2 is "+stateSim2);
	    	
	    	if((stateSim1 == TelephonyManager.CALL_STATE_IDLE)&&(stateSim2 == TelephonyManager.CALL_STATE_IDLE)) {
	    		return true;
	    	} else {
	    		return false;
	    	}
	    }
	    //MTK_OP02_PROTECT_END
 
	/**
	 * Check if voip is supported and is enabled
	 */
	private boolean isVoipAvailable() {
		int IsInternetCallEnabled = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.ENABLE_INTERNET_CALL, 0);

		return (SipManager.isVoipSupported(getActivity()))
				&& (IsInternetCallEnabled != 0);

	}

	/**
	 * switch data connection default SIM
	 * @param value: sim id of the new default SIM
	 */
	private void switchGprsDefautlSIM(long value) {

		if(value <0) {
			return;
		}
		
		long GprsValue = Settings.System.getLong(getContentResolver(),
				Settings.System.GPRS_CONNECTION_SIM_SETTING,
				Settings.System.DEFAULT_SIM_NOT_SET);
		if(value == GprsValue) {
			return;
		}		
		Intent intent = new Intent(Intent.ACTION_DATA_DEFAULT_SIM_CHANGED);
		intent.putExtra("simid", value);



		mGprsTargSim = (value > 0) ? true : false;

		if (mGprsTargSim == true) {
			dealwithAttach();
		} else {
			dealwithDetach();

		}

		getActivity().sendBroadcast(intent);
	}
	
	private void enableDataRoaming(long value){

    	try {
			if(iTelephony != null) {
				iTelephony.setDataRoamingEnabledGemini(true, SIMInfo.getSlotById(getActivity(), value));

			}
    	} catch (RemoteException e){
			Xlog.e(TAG, "iTelephony exception");
			return;
		}
		SIMInfo.setDataRoaming(getActivity(),SimInfo.DATA_ROAMING_ENABLE, value);

		
	}
	
	/**
	 * switch 3g modem SIM
	 * @param slotID
	 */
	private void switchVideoCallDefaultSIM(long value) {
		Xlog.i(TAG, "switchVideoCallDefaultSIM to "+value);
		

		if (iTelephony != null) {
			
			SIMInfo siminfo = SIMInfo.getSIMInfoById(getActivity(), value);
			Xlog.i(TAG, "siminfo = "+siminfo);	
			
			if(siminfo == null)
				return;

			try {
				
				Xlog.i(TAG, "sim slot  = "+siminfo.mSlot);
				if (iTelephony.set3GCapabilitySIM(siminfo.mSlot) == true) {
					Xlog.i(TAG, "result is true");
					mIsModemSwitching = true;
					if(mStatusBarManager != null) {
						mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);						
					}

					showDialog(DIALOG_3G_MODEM_SWITCHING);
					mIsShowDlg = 3;
					setCancelable(false);

				} else {
					updateVideoCallDefaultSIM();
				}
			} catch (RemoteException e){
				Xlog.e(TAG, "iTelephony exception");
				return;
			}

		}
	}

	private void updateSimInfoAsNoSim() {
		Xlog.i(TAG, "updateSimInfoAsNoSim()");
		initSimMap();
		if (mSimMap.size() > 0) {
			mHasSim = true;
			registerMgrOnCreate();
			setPreferenceProperty();
			initDefaultSimPreference();
			if(!getPreferenceScreen().isEnabled())
				getPreferenceScreen().setEnabled(true);
		}
	}
	private void setPreferenceProperty(){
		
		long voicecallID = Settings.System.getLong(getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
		long smsID = Settings.System.getLong(getContentResolver(), Settings.System.SMS_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
		long dataconnectionID = Settings.System.getLong(getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
		int videocallSlotID = VEDIO_CALL_OFF;
		if(!FeatureOption.MTK_GEMINI_3G_SWITCH) {
			videocallSlotID = Phone.GEMINI_SIM_1;
		} else {
			try {
				if (iTelephony != null) {
					videocallSlotID = iTelephony.get3GCapabilitySIM();
					GeminiUtils.m3GSlotID = videocallSlotID;
				}
			} catch (RemoteException e){
				Xlog.e(TAG, "iTelephony exception");
				return;
			}
		}
		Xlog.i(TAG, "voicecallID =" +voicecallID+" smsID ="+smsID+
					" dataconnectionID =" +dataconnectionID+
				    " videocallSlotID =" +videocallSlotID);
		int k=0;
        for (Long simid: mSimMapKeyList) {
        	SIMInfo siminfo = mSimMap.get(simid);
        	if (siminfo != null) {
	        	if (simid == voicecallID) {
	        		if(sIsVoiceCapable){
	        			mVoiceCallSimSetting.setInitValue(k);
	        			mVoiceCallSimSetting.setSummary(siminfo.mDisplayName);
	        			}
	        		}
	        	Xlog.i(TAG, "siminfo.mSlot  = "+siminfo.mSlot );
	        	if ((mVTCallSupport == true)&&(siminfo.mSlot == videocallSlotID)) {
					Xlog.i(TAG, "set init video call"+ k);
					if(!FeatureOption.MTK_GEMINI_3G_SWITCH) {
						mVideoCallSimSetting.setInitValue(0);
					} else {
						Xlog.i(TAG, "mVideoCallSimSetting.setInitValue("+k+")");
						mVideoCallSimSetting.setInitValue(k);
					}
	        		mVideoCallSimSetting.setSummary(siminfo.mDisplayName);
	        	}
	        	if (simid == smsID) {
	        		mSmsSimSetting.setInitValue(k);
	        		mSmsSimSetting.setSummary(siminfo.mDisplayName);
	        	}
	        	if (simid == dataconnectionID) {
	        		mGprsSimSetting.setInitValue(k);
	        		mGprsSimSetting.setSummary(siminfo.mDisplayName);
	        	}
        		String key = String.valueOf(siminfo.mSimId);
        		SimInfoEnablePreference simInfoPref = (SimInfoEnablePreference) findPreference(key); 
				if ((simInfoPref != null)&&(iTelephony != null)) {
					try {
						boolean isRadioOn = iTelephony.isRadioOnGemini(siminfo.mSlot);
						simInfoPref.setCheck(isRadioOn);
						simInfoPref.setRadioOn(isRadioOn);
					} catch (RemoteException e){
						Xlog.e(TAG, "iTelephony exception");
						return;
					}
				}
        	}
        	k++;
        }
        int nSim= mSimMap.size();
		if(sIsVoiceCapable){
		      if (voicecallID == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
		 	      mVoiceCallSimSetting.setInitValue(nSim);
				  mVoiceCallSimSetting.setSummary(R.string.gemini_intenet_call);
		      } else if (voicecallID == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
				  mVoiceCallSimSetting.setInitValue((mVoipAvailable == true)?(nSim+1):nSim);
				  mVoiceCallSimSetting.setSummary(R.string.gemini_default_sim_always_ask);
		      } else if(voicecallID == Settings.System.DEFAULT_SIM_NOT_SET) {
		    	  mVoiceCallSimSetting.setInitValue((int)Settings.System.DEFAULT_SIM_NOT_SET);
				  mVoiceCallSimSetting.setSummary(R.string.apn_not_set);
		      }
		}
        if(smsID == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
    		mSmsSimSetting.setInitValue(nSim);
    		mSmsSimSetting.setSummary(R.string.gemini_default_sim_always_ask);
        } else if(smsID == Settings.System.DEFAULT_SIM_NOT_SET) {
        	mSmsSimSetting.setSummary(R.string.apn_not_set);
        }
        if(dataconnectionID == Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER) {
    		mGprsSimSetting.setInitValue(nSim);
    		mGprsSimSetting.setSummary(R.string.gemini_default_sim_never);
        } else if(dataconnectionID == Settings.System.DEFAULT_SIM_NOT_SET) {
        	mGprsSimSetting.setSummary(R.string.apn_not_set);
        }
		if (mVTCallSupport) {
			if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
				if (videocallSlotID == VEDIO_CALL_OFF) {
					mIs3gOff = true;
					mVideoCallSimSetting.setSummary(R.string.gemini_default_sim_3g_off);
					} else{
						mIs3gOff = false;
					}
				try {
					if (iTelephony != null) {
						mVideoCallSimSetting.setEnabled(!(mIs3gOff||iTelephony.is3GSwitchLocked()||(!mHasSim)));
						Xlog.i(TAG, "mIs3gOff="+mIs3gOff+" mHasSim="+mHasSim);
						Xlog.i(TAG, "iTelephony.is3GSwitchLocked() is "+ iTelephony.is3GSwitchLocked());
						}
					}catch (RemoteException e){
						Xlog.e(TAG, "iTelephony exception");
						return;
					}
			 } else {
				 long videocallID = Settings.System.getLong(getContentResolver(),
						Settings.System.VIDEO_CALL_SIM_SETTING,Settings.System.DEFAULT_SIM_NOT_SET);
				 if (videocallID == Settings.System.DEFAULT_SIM_NOT_SET) {
					 mVideoCallSimSetting.setSummary(R.string.apn_not_set);
					 }
				 }
			}
		mScreenEnable = (Settings.System.getInt(this.getContentResolver(),
    										MMS_TRANSACTION, 0) == 0)?true:false;
		mAllSimRadioOff = (Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1)==1)
    					||(Settings.System.getInt(this.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, -1) == 0);

		//when there is an call, disable this item
	    mGprsSimSetting.setEnabled(!mAllSimRadioOff && mScreenEnable && mHasSim);
	    Xlog.i(TAG, "mGprsSimSetting.setEnabled = "+mGprsSimSetting.isEnabled() +" in onResume");
	    Xlog.i(TAG, "mAllSimRadioOff = "+mAllSimRadioOff+" mScreenEnable = "+mScreenEnable+" mHasSim = "+mHasSim);
	    //MTK_OP02_PROTECT_START
		//  For CU customization
		if (mHasNetworkMode) {
			setNetworkModePref();
		}
    //MTK_OP02_PROTECT_END
	
	}
	private void setNetworkModePref(){
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        if(isCallStateIdle() == false){
        	mNetworkMode.setEnabled(false);
        	mNetworkModeGemini.setEnabled(false);
		} else {
			boolean Sim1Ready = false;
			try {
				if ((mIsSlot1Insert)&&(iTelephony != null)) {
					Sim1Ready = iTelephony.isRadioOnGemini(Phone.GEMINI_SIM_1);
				}
			} catch (RemoteException e){
				Xlog.e(TAG, "iTelephony exception");
				return;
			}
        	mNetworkMode.setEnabled(Sim1Ready);
        	mNetworkModeGemini.setEnabled(Sim1Ready);
        }
		if((mIsSlot1Insert == true)&&(mIsSlot2Insert == true)) {
			if(mNetworkModeSummary != null) {
		        int settingsNetworkMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE,
		                preferredNetworkMode);
		        if((settingsNetworkMode>=0)&&(settingsNetworkMode<=2)){
			        mNetworkModeGemini.setSummary(mNetworkModeSummary[settingsNetworkMode]);      	
		        }
			}
		} else {
	        int settingsNetworkMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.PREFERRED_NETWORK_MODE,
	                preferredNetworkMode);
	        if((settingsNetworkMode>=0)&&(settingsNetworkMode<=2)) {
		        mNetworkMode.setValue(Integer.toString(settingsNetworkMode));
		        mNetworkMode.updateSummary();
	        }
		}
	}

	@Override
	public void onPreferenceClick(long simid) {
		// TODO Auto-generated method stub
		
		Bundle extras = new Bundle();
		extras.putLong(GeminiUtils.EXTRA_SIMID, simid);
		startFragment(this, SimInfoEditor.class.getCanonicalName(), -1, extras, R.string.gemini_sim_info_title);
		Xlog.i(TAG, "startFragment "+ SimInfoEditor.class.getCanonicalName());
	}

}
