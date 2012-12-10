
package com.android.InternationalCardTester;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.cdma.RuimCard;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.InternationalCardUtil;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.telephony.TelephonyManagerEx;


public class GlobalModeService extends Service {
    private static final String TAG = "GlobalModeService";
    
    private TelephonyManager mTelephonyManager;
    private TelephonyManagerEx mTelephonyManagerEx;
    private int mServiceState;
    private GlobalModeHandler mHandler;
    private Handler mSecNWSelHandler = new Handler();
        
    private Context mContext;
    private int mEVDOModemSlot;
    static private boolean mDevicePowerOn = true;
    static private boolean mSerachedGSM = false;
    static private boolean mSerachedCDMA = false;
    private int mGetMccMncRetryCnt = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManagerEx = TelephonyManagerEx.getDefault();
        
        mHandler = new GlobalModeHandler(Looper.myLooper());
        mContext = getApplicationContext();
        mEVDOModemSlot = InternationalCardTesterActivity.getExternalModemSlot();
        mTelephonyManager.listenGemini(mPhoneStateListener, 
			             PhoneStateListener.LISTEN_SERVICE_STATE, mEVDOModemSlot);
		
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        intentFilter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
        intentFilter.addAction("android.intent.action.ACTION_BOOT_IPO");
        registerReceiver(mIntentReceiver, intentFilter);    
        super.onCreate();  
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mIntentReceiver);    
        super.onDestroy();  
    }
    
    @Override
    public void onStart(Intent intent, int startid) {
        Log.d(TAG, "onStart");
        super.onStart(intent, startid);  
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BroadcastReceiver: " + intent.getAction());
            if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
                int simInsertedStatus = intent.getIntExtra("simInsertedStatus", 0);
                int newCdmaSimSlot = intent.getIntExtra("newSIMSlot", 0x00);
                newCdmaSimSlot &= (mEVDOModemSlot == Phone.GEMINI_SIM_1 ? 0x01:0x02);
                int menuSelected = InternationalCardUtil.getUserSelectedMode(mContext, mEVDOModemSlot);
                int secNWSel = Settings.System.getInt(mContext.getContentResolver(),
                       InternationalCardTesterActivity.SECOND_NW_SELECTION_SETTING, 1);
                
                Log.d(TAG, "TelephonyIntents.ACTION_SIM_INFO_UPDATE simInsertedStatus="+simInsertedStatus);
                Log.d(TAG, "TelephonyIntents.ACTION_SIM_INFO_UPDATE newCdmaSimSlot="+newCdmaSimSlot);
                Log.d(TAG, "TelephonyIntents.ACTION_SIM_INFO_UPDATE menuSelected="+menuSelected);
                Log.d(TAG, "TelephonyIntents.ACTION_SIM_INFO_UPDATE secNWSel="+secNWSel);
                if ((mEVDOModemSlot == Phone.GEMINI_SIM_1 && (simInsertedStatus & 0x01) == 0x00) ||
                    (mEVDOModemSlot == Phone.GEMINI_SIM_2 && (simInsertedStatus & 0x02) == 0x00)){
                    // No international card inserted, return directly.
                    return;
                } else if (newCdmaSimSlot != 0x00){
                    // Set to "CDMA only" mode if CDMA SIM was changed
                    // CDMAPhone SIM iccid changed, keep G+C dual talk

                    //Move to GeminiPhone for avoid register to network before switch to target network
                } else if (menuSelected == InternationalCardUtil.NETWORK_TECH_MODE_GSM_ONLY) {
                    // Chenge to Gemini mode from DT mode according user menu setting
                    // Prove it because it's too late to chage to GSM
                    int phoneType = mTelephonyManager.getPhoneTypeGemini(mEVDOModemSlot);
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        //Move to GeminiPhone for avoid register to network before switch to target network
                    } else {
                        if (isGSM2Stage()) {
                            // Continue MD init
                            Log.d(TAG, "GSM continueMdInit");
                            mTelephonyManager.continueMdInit(mEVDOModemSlot);
                        }
                    }
                } else if (menuSelected == InternationalCardUtil.NETWORK_TECH_MODE_INTERNATIONAL) {
                    // Start second NW selection in international mode
                    if (InternationalCardTesterActivity.isDualTalkMode()) {
                        if (secNWSel == 1) {
                            Log.d(TAG, "CDMA Second NW selection");
                            mGetMccMncRetryCnt = 0;
                            mSecNWSelHandler.postDelayed(startSecondNWSelectionCDMA, 1000);
                        }
                    } else {
                        if (secNWSel == 1) {
                            Log.d(TAG, "GSM Second NW selection");
                            mGetMccMncRetryCnt = 0;
                            mSecNWSelHandler.postDelayed(startSecondNWSelectionGSM, 1000);
                        } else {
                            if (isGSM2Stage()) {
                               // Continue MD init
                               Log.d(TAG, "GSM continueMdInit");
                               mTelephonyManager.continueMdInit(mEVDOModemSlot);
                            }
                        }
                    }
                }
            } else  if (intent.getAction().equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
            } else  if (intent.getAction().equals("android.intent.action.ACTION_BOOT_IPO")) {
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.d(TAG, "onServiceStateChanged serviceState=" + serviceState);
            mServiceState = serviceState.getState();

            // Remove all pending message that changes network type
            mHandler.removeCallbacksAndMessages(null);

            // Lose signal test
            int menuSelected = InternationalCardUtil.getUserSelectedMode(mContext, mEVDOModemSlot);
            if (mServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
                if (menuSelected != InternationalCardUtil.NETWORK_TECH_MODE_INTERNATIONAL) {
                    Message msg = mHandler.obtainMessage(GlobalModeHandler.REQUEST_DISPLAY_CONFIRM_DIALOG);
                    mHandler.sendMessageDelayed(msg, 60000);
                } else {
                    Message msg = mHandler.obtainMessage(GlobalModeHandler.REQUEST_SWITCH_TECH_MODE);
                    mHandler.sendMessageDelayed(msg, 60000);
                }
            }
        }
    };

    class GlobalModeHandler extends Handler {
        static final int REQUEST_SWITCH_TECH_MODE = 0;
        static final int REQUEST_DISPLAY_CONFIRM_DIALOG = 1;

        GlobalModeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_SWITCH_TECH_MODE:
                    int phoneType = mTelephonyManager.getPhoneTypeGemini(mEVDOModemSlot);
                    setPhoneType(mEVDOModemSlot,
                        phoneType == Phone.PHONE_TYPE_CDMA ? Phone.PHONE_TYPE_GSM: Phone.PHONE_TYPE_CDMA);
                    break;

                case REQUEST_DISPLAY_CONFIRM_DIALOG:
                    if (mServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
                        Intent intent1 = new Intent();
                        intent1.setComponent(new ComponentName("com.android.InternationalCardTester",
                            "com.android.InternationalCardTester.InternationalCardTesterActivity"));
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.putExtra("confirmDialog", true);
                        startActivity(intent1);
                    }
                break;
            }
        }
    }
    
    private Runnable startSecondNWSelectionGSM = new Runnable() {
        public void run() {
            mSerachedGSM = true;
            String[] mccMnc = mTelephonyManager.getLimitServiceNetworkOperator(mEVDOModemSlot);
            Log.d(TAG, "startSecondNWSelectionGSM mEVDOModemSlot="+mEVDOModemSlot+", mccMnc="+mccMnc[2]);

            if (InternationalCardTesterActivity.isDualTalkMode())
                 return;

            // Endless to get MCC MNC
            if (TextUtils.isEmpty( mccMnc[2]) || mccMnc[2].startsWith("0")) {
                Log.d(TAG, "REQUEST_GET_LIMIT_SERVICE_OPERATOR send again");
                mSecNWSelHandler.postDelayed(startSecondNWSelectionGSM, 2000);
                return;
            }

            if (mccMnc[2].startsWith("460") || 
                mccMnc[2].startsWith("455")) {
                setPhoneType(mEVDOModemSlot, Phone.PHONE_TYPE_CDMA);
            } else {
                if (mSerachedCDMA) {
                    if (isGSM2Stage()) {
                        // Continue MD init
                        Log.d(TAG, "GSM continueMdInit");
                        mTelephonyManager.continueMdInit(mEVDOModemSlot);
                    }
                } else if (mccMnc[2].startsWith("466") ||
                           mccMnc[2].startsWith("440") ||
                           mccMnc[2].startsWith("441") ||
                           mccMnc[2].startsWith("310") ||
                           mccMnc[2].startsWith("311") ||
                           mccMnc[2].startsWith("316") ||
                           mccMnc[2].startsWith("450") ||
                           mccMnc[2].startsWith("302")) {
                    setPhoneType(mEVDOModemSlot, Phone.PHONE_TYPE_CDMA);
                }
            }
        }
    };

    private Runnable startSecondNWSelectionCDMA = new Runnable() {
        public void run() {
            mSerachedCDMA = true;
            String mccMnc = mTelephonyManager.getNetworkOperatorGemini(mEVDOModemSlot);
            Log.d(TAG, "startSecondNWSelectionCDMA mEVDOModemSlot="+mEVDOModemSlot+", mccMnc="+mccMnc);

            int gsmSlot = (mEVDOModemSlot == Phone.GEMINI_SIM_1 ? Phone.GEMINI_SIM_2 : Phone.GEMINI_SIM_1);
            String gsmMccMnc = mTelephonyManager.getNetworkOperatorGemini(gsmSlot);
            Log.d(TAG, "startSecondNWSelectionCDMA gsmMccMnc=" + gsmMccMnc);

            if (!InternationalCardTesterActivity.isDualTalkMode())
                 return;

            if (mGetMccMncRetryCnt++ > 10) {
                // Continue CDMA registration
                return;
            }

            if (TextUtils.isEmpty( mccMnc) || mccMnc.startsWith("0")) {
                Log.d(TAG, "getNetworkOperatorGemini again");
                mSecNWSelHandler.postDelayed(startSecondNWSelectionCDMA, 2000);
                return;
            }

            if (mccMnc.startsWith("460") || 
                mccMnc.startsWith("455") || 
                gsmMccMnc.startsWith("460") || 
                gsmMccMnc.startsWith("455")) {
                // Continue CDMA registration
                return;
            } else {
                if (mccMnc.startsWith("466") ||
                    mccMnc.startsWith("440") ||
                    mccMnc.startsWith("441") ||
                    mccMnc.startsWith("310") ||
                    mccMnc.startsWith("311") ||
                    mccMnc.startsWith("316") ||
                    mccMnc.startsWith("450") ||
                    mccMnc.startsWith("302") ||
                    gsmMccMnc.startsWith("466") ||
                    gsmMccMnc.startsWith("440") ||
                    gsmMccMnc.startsWith("441") ||
                    gsmMccMnc.startsWith("310") ||
                    gsmMccMnc.startsWith("311") ||
                    gsmMccMnc.startsWith("316") ||
                    gsmMccMnc.startsWith("450") ||
                    gsmMccMnc.startsWith("302")) {
                    // Continue CDMA registration
                    return;
                } else if (mSerachedGSM) {
                    // Continue CDMA registration
                    return;
                } else {
                    setPhoneType(mEVDOModemSlot, Phone.PHONE_TYPE_GSM);
                }
            }
        }
    };

    public static boolean isGSM2Stage() {
        int mode = 0;
        mode = SystemProperties.getInt("gsm.2stage.enable", 0);
        String strMode = SystemProperties.get("gsm.2stage.enable");
        Log.d(TAG, "gsm.2stage.enable mode="+mode+", strMode="+strMode);
        return (mode == 1 ? true : false);
    }
	
    private void setPhoneType(int simId, int type) {
        Log.d(TAG, "setPhoneType simId="+simId+", type"+type);		
        mTelephonyManager.setPhoneType(simId, type);
    }
}
