package com.android.InternationalCardTester;

import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.InternationalCardUtil;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.telephony.TelephonyManagerEx;

public class InternationalCardTesterActivity extends Activity {
    /** Called when the activity is first created. */
    static final String TAG = "InternationalCardTesterActivity";

    static final public String SECOND_NW_SELECTION_SETTING = "second.nw.selection";
    static private Context mContext;
    static private int mServiceState;
    static private TelephonyManager mTelephonyManager;

    static public boolean mSecondNWSeclection;
    private CheckBox mChkBtnSlot1, mChkBtnSlot2, mChkBtnSecSel;
    private RadioButton mRBthInter,mRBthCDAM, mRBtnGSM;
    private RadioGroup mRadiogroupMode;
    private TelephonyManagerEx mTelephonyManagerEx;
    private Timer mTimer;
    private ITelephony mITelephony;
    private AlertDialog mConfirmDialog;
    private boolean mSelfKill;
    private boolean mShowConfirmDialog;
    private int mEVDOModemSlot;
    private int mCardType;
    private int mSimStat;
    private ProgressDialog mPd = null;

            
    static public PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            Log.d(TAG, "onServiceStateChanged STATE_IN_SERVICE");
            mServiceState = serviceState.getState();
        }
    };
    
    static public Context getCurrentContext() {
        return mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        NotificationManager nMgr = 
            (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(1212);
    
        setContentView(R.layout.main);
        mChkBtnSlot1 = (CheckBox) findViewById(R.id.CDMASwitch);
        mChkBtnSlot2 = (CheckBox) findViewById(R.id.GSMSwitch);
        mRBthInter = (RadioButton) findViewById(R.id.rdoBtnInter);
        mRBthCDAM = (RadioButton) findViewById(R.id.rdoBtnCDMA);
        mRBtnGSM = (RadioButton) findViewById(R.id.rdoBtnGSM);
        mRadiogroupMode=(RadioGroup)findViewById(R.id.MrdogMode);
        mChkBtnSecSel = (CheckBox) findViewById(R.id.SecSel);
        

        int secNWSel = Settings.System.getInt(mContext.getContentResolver(),
                       SECOND_NW_SELECTION_SETTING, 1);
        mSecondNWSeclection = (secNWSel == 1);
        mChkBtnSecSel.setChecked(mSecondNWSeclection);

        
        mTelephonyManagerEx = TelephonyManagerEx.getDefault();
        mTelephonyManager = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
        mITelephony = ITelephony.Stub.asInterface(ServiceManager
                .getService("phone"));
            
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE);
                

        mEVDOModemSlot = getExternalModemSlot();
        mChkBtnSlot1.setOnCheckedChangeListener(Ckblistener);
        mChkBtnSlot2.setOnCheckedChangeListener(Ckblistener);
        mChkBtnSlot1.setEnabled(false);
        mChkBtnSlot2.setEnabled(false);
                
        mRadiogroupMode.setOnCheckedChangeListener(changeMode);

        mChkBtnSlot2.setEnabled(false);

        mRBthInter.setEnabled(false);
        mRBthCDAM.setEnabled(false);
        mRBtnGSM.setEnabled(false);
        mRadiogroupMode.setEnabled(false);
		
        int phoneType = mTelephonyManager.getPhoneTypeGemini(mEVDOModemSlot);
		// Get last type setting in GSM mode
        mCardType = mTelephonyManager.getInternationalCardType(mEVDOModemSlot);
        mSimStat = mTelephonyManager.getSimStateGemini(mEVDOModemSlot);
        Log.d(TAG,"onCreate:mSimStat =" + mSimStat + ", cardType=" + mCardType);
        if (mSimStat != TelephonyManager.SIM_STATE_ABSENT &&
            (mCardType == InternationalCardUtil.CARD_TYPE_CHINA_TELECOM_DUAL_MODE ||
             mCardType == InternationalCardUtil.CARD_TYPE_GENERIC_DUAL_MODE)) {

            mRBthInter.setEnabled(true);
            mRBthCDAM.setEnabled(true);
            mRBtnGSM.setEnabled(true);
            mRadiogroupMode.setEnabled(true);
        } else if (mSimStat == TelephonyManager.SIM_STATE_ABSENT) {
            Settings.System.putInt(mContext.getContentResolver(),
                "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot,
                InternationalCardUtil.NETWORK_TECH_MODE_CDMA_ONLY);
        }

        int userSelectedMode = Settings.System.getInt(mContext.getContentResolver(),
                              "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot, 1);    
        switch (userSelectedMode)
        {
            case InternationalCardUtil.NETWORK_TECH_MODE_INTERNATIONAL:
                mRBthInter.setChecked(true);
                break;
            case InternationalCardUtil.NETWORK_TECH_MODE_CDMA_ONLY:
                mRBthCDAM.setChecked(true);
                break;
            case InternationalCardUtil.NETWORK_TECH_MODE_GSM_ONLY:
                mRBtnGSM.setChecked(true);
                break;
        }

        if (getIntent().hasExtra("selfkill")) {
            Bundle b = getIntent().getExtras();
            mSelfKill = b.getBoolean("selfkill");
        }
        else if (getIntent().hasExtra("confirmDialog")) {
            Bundle b = getIntent().getExtras();
            mShowConfirmDialog = b.getBoolean("confirmDialog");
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mIntentReceiver, intentFilter);    
    }
    
    public void onStart() {
        Log.d(TAG, "onStart");
        if (mSelfKill) {
            TextView noticeTextview = (TextView) findViewById(R.id.textviewNoice);
            noticeTextview.setText("Dual mode SIM detected");
            noticeTextview.append("Change to CDMA?");
            noticeTextview.setTextSize(20);
            noticeTextview.setTextColor(Color.GREEN);
            noticeTextview.setVisibility(View.VISIBLE);
    
            mTimer = new Timer(true);
            // Close activity after 30s
            mTimer.schedule(new timerTask(), 15000, 1);
        } else if (mShowConfirmDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                InternationalCardTesterActivity.this);
            builder.setMessage("Switch to international mode?");
            builder.setPositiveButton("Yes", dialogClickListener);
            builder.setNegativeButton("No", dialogClickListener);
            mTimer = new Timer(true);
            // Close activity after 30s
            mTimer.schedule(new timerTask(), 15000, 1);
    
            mConfirmDialog = builder.show();
        }
        super.onStart();
    }

    public class timerTask extends TimerTask
    {
        public void run()
        {
            Log.d(TAG, "onCalcelTImer");
            this.cancel();
            InternationalCardTesterActivity.this.finish();
        }
    };
   
    public void onDestory() {
        unregisterReceiver(mIntentReceiver);
    }

    private CheckBox.OnCheckedChangeListener Ckblistener = new CheckBox.OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if (mChkBtnSlot1.isChecked() == true)
            {
            }
            if (mChkBtnSlot2.isChecked() == true)
            {
            }
            if (mChkBtnSecSel.isChecked() == true)
            {
                Settings.System.putInt(mContext.getContentResolver(),
                                    SECOND_NW_SELECTION_SETTING, 1);
                mChkBtnSecSel.setChecked(mSecondNWSeclection);
                mSecondNWSeclection = true;
            } else {
                Settings.System.putInt(mContext.getContentResolver(),
                                    SECOND_NW_SELECTION_SETTING, 0);
                mSecondNWSeclection = false;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BroadcastReceiver: " + intent.getAction());
            if (intent.getAction().equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
            } else if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
                if (mPd != null) {
                    mPd.dismiss();
                    mPd = null;
                }
                if (mCardType == InternationalCardUtil.CARD_TYPE_CHINA_TELECOM_DUAL_MODE ||
                    mCardType == InternationalCardUtil.CARD_TYPE_GENERIC_DUAL_MODE) {
                    
                    mRBthInter.setEnabled(true);
                    mRBthCDAM.setEnabled(true);
                    mRBtnGSM.setEnabled(true);
                    mRadiogroupMode.setEnabled(true);
                }
            } else if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                String value = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
                int phoneId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                if (value.equals(IccCard.INTENT_VALUE_ICC_LOADED) &&
                    phoneId == mEVDOModemSlot) {
                    mCardType = mTelephonyManager.getInternationalCardType(mEVDOModemSlot);
                    Log.d(TAG,"RuimLoaded: cardType=" + mCardType);
                    if (mCardType == InternationalCardUtil.CARD_TYPE_CHINA_TELECOM_DUAL_MODE ||
                         mCardType == InternationalCardUtil.CARD_TYPE_GENERIC_DUAL_MODE) {

                        mRBthInter.setEnabled(true);
                        mRBthCDAM.setEnabled(true);
                        mRBtnGSM.setEnabled(true);
                        mRadiogroupMode.setEnabled(true);
                    }
                }
            }
        }
    };
        
    private RadioGroup.OnCheckedChangeListener changeMode = new RadioGroup.OnCheckedChangeListener()
    {
        public void onCheckedChanged(RadioGroup group, int checkedId)
        {
            switch (checkedId)
            {
                case R.id.rdoBtnInter:
                    Settings.System.putInt(mContext.getContentResolver(),
                            "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot,
                            InternationalCardUtil.NETWORK_TECH_MODE_INTERNATIONAL);
                    if (mServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
                        int phoneType = mTelephonyManager.getPhoneTypeGemini(mEVDOModemSlot);
                        setPhoneType(mEVDOModemSlot,
                            phoneType == Phone.PHONE_TYPE_CDMA ? Phone.PHONE_TYPE_GSM: Phone.PHONE_TYPE_CDMA);
                    }
                    break;
                case R.id.rdoBtnCDMA:
                    Log.d(TAG, "onCheckedChanged setPhoneType PHONE_TYPE_CDMA");
                    setPhoneType(mEVDOModemSlot,
                        Phone.PHONE_TYPE_CDMA);
                    Settings.System.putInt(mContext.getContentResolver(),
                            "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot,
                            InternationalCardUtil.NETWORK_TECH_MODE_CDMA_ONLY);
                    break;
                case R.id.rdoBtnGSM:
                    Log.d(TAG, "onCheckedChanged setPhoneType PHONE_TYPE_GSM");
                    setPhoneType(mEVDOModemSlot, Phone.PHONE_TYPE_GSM);
                    Settings.System.putInt(mContext.getContentResolver(),
                            "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot,
                            InternationalCardUtil.NETWORK_TECH_MODE_GSM_ONLY);
                    break;
                default:
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //Yes button clicked
                mRBthInter.setChecked(true);
                Settings.System.putInt(mContext.getContentResolver(),
                    "gsm.internationalcard.network.mode.sim"+mEVDOModemSlot,
                    InternationalCardUtil.NETWORK_TECH_MODE_INTERNATIONAL);
                int phoneType = mTelephonyManager.getPhoneTypeGemini(mEVDOModemSlot);
                setPhoneType(mEVDOModemSlot,
                    phoneType == Phone.PHONE_TYPE_CDMA ? Phone.PHONE_TYPE_GSM: Phone.PHONE_TYPE_CDMA);

                break;
    
            case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
            }
        }
    };

    private void setPhoneType(int simId, int type) {
        Log.d(TAG, "setPhoneType simId="+simId+", type"+type);
        int phoneType = mTelephonyManager.getPhoneTypeGemini(simId);
        Log.d(TAG, "setPhoneType phoneType = "+phoneType);
		
        if (phoneType == type)
            return;
		
        if (mSimStat == TelephonyManager.SIM_STATE_ABSENT)
            return;
		
		mCardType = mTelephonyManager.getInternationalCardType(mEVDOModemSlot);
        Log.d(TAG,"onCreate:mSimStat =" + mSimStat + ", cardType=" + mCardType);
        if (mCardType != InternationalCardUtil.CARD_TYPE_CHINA_TELECOM_DUAL_MODE &&
            mCardType != InternationalCardUtil.CARD_TYPE_GENERIC_DUAL_MODE) {

            return;
        }
		
		
        mTelephonyManager.setPhoneType(simId, type);
        mRBthInter.setEnabled(false);
        mRBthCDAM.setEnabled(false);
        mRBtnGSM.setEnabled(false);
        mPd = ProgressDialog.show(InternationalCardTesterActivity.this, 
                                  "International Card", "Switching...");
    }

    public static int getExternalModemSlot() {
        int slot = 0;
        slot = SystemProperties.getInt("ril.external.md", 0);
        Log.d(TAG, "getExternalModemSlot slot="+slot);
        return slot - 1;
    }

    public static boolean isDualTalkMode() {
        int mode = 0;
        mode = SystemProperties.getInt("mediatek.gemini", 0);
        Log.d(TAG, "isDualTalkMode mode="+mode);
        return (mode == 0 ? true : false);
    }
}
