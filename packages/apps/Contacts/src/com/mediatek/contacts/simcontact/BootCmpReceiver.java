package com.mediatek.contacts.simcontact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.Settings;
import android.os.Bundle;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
//import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.*;

import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.Telephony.SIMInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.IccCard;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService2;

public class BootCmpReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCmpReceiver";
	private static Context mContext = null;
    
    private static String INTENT_SIM_FILE_CHANGED = "android.intent.action.sim.SIM_FILES_CHANGED"; 
    private static String INTENT_SIM_FILE_CHANGED_2 = "android.intent.action.sim.SIM_FILES_CHANGED_2";

    private static boolean[] PHBREADYRECEIVED;

    public BootCmpReceiver() {
        if (PHBREADYRECEIVED == null) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                PHBREADYRECEIVED = new boolean[2];
            } else {
                PHBREADYRECEIVED = new boolean[1];
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.i(TAG, "In onReceive ");
        final String action = intent.getAction();
        Log.i(TAG, "action is " + action);

        if (action.equals(TelephonyIntents.ACTION_PHB_STATE_CHANGED)) {
            processPhoneBookChanged(intent);
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            processAirplaneModeChanged(intent);
        } else if (action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)) {
            processDualSimModeChanged(intent);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
            Log.i(TAG, "processSimInfoUpdate");
//            processSimInfoUpdate(intent);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            processSimStateChanged(intent);
        } else if (action.equals(INTENT_SIM_FILE_CHANGED)) {// SIM REFERSH
            processSimFilesChanged(0);
        } else if (action.equals(INTENT_SIM_FILE_CHANGED_2)) {// SIM REFRESH
            processSimFilesChanged(1);
        } else if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
            processSimInfoUpdateForSettingChanged(intent);
        } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
    	    processIpoShutDown();
        } else if (action.equals("android.intent.action.ACTION_PHONE_RESTART")) {
            processPhoneReset(intent);
        }
    }

    public void startSimService(int slotId, int workType) {
        Intent intent = null;
        if (slotId == 0) {
            intent = new Intent(mContext, StartSIMService.class);
        } else {
            intent = new Intent(mContext, StartSIMService2.class);
        }
        
        intent.putExtra(AbstractStartSIMService.SERVICE_SLOT_KEY, slotId);
        intent.putExtra(AbstractStartSIMService.SERVICE_WORK_TYPE, workType);
        Log.i(TAG, "[startSimService]slotId:" + slotId + "|workType:" +workType);
        mContext.startService(intent);
    }
    
    void processPhoneBookChanged(Intent intent) {
        Log.i(TAG, "processPhoneBookChanged");
        boolean phbReady = intent.getBooleanExtra("ready", false);
        int slotId = intent.getIntExtra("simId", -10);
        Log.i(TAG, "[processPhoneBookChanged]phbReady:" + phbReady + "|slotId:" + slotId);
        if (phbReady && slotId >= 0) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                PHBREADYRECEIVED[slotId] = true;
                Log.i(TAG, "Gemini PHBREADYRECEIVED[" + slotId + "] = " + PHBREADYRECEIVED[slotId]);
            } else {
                PHBREADYRECEIVED[0] = true;
                Log.i(TAG, "Single PHBREADYRECEIVED[0] = " + PHBREADYRECEIVED[slotId]);
            }
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
    }
    
    void processAirplaneModeChanged(Intent intent) {
        Log.i(TAG, "processAirplaneModeChanged");
        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.i(TAG, "[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
        if (isAirplaneModeOn) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
        } else {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
        }
    }
    
    /**
     * Dual Sim mode is only for Gemini Feature.
     * 0 for none sim, 1 for sim1 only, 2 for sim2 only, 3 for dual sim
     * And the deefault mode 3
     * 
     * The change map is as following 
     *  
     *              => (Mode 1) <=
     *            ==              == 
     * (Mode 3) <=                  => (Mode 0)
     *            ==              ==
     *              => (Mode 2) <=
     * 
     * @param intent
     */
    void processDualSimModeChanged(Intent intent) {
        Log.i(TAG, "processDualSimModeChanged");
        // Intent.EXTRA_DUAL_SIM_MODE = "mode";
        int type = intent.getIntExtra("mode", -1);
        
        SharedPreferences prefs = mContext.getSharedPreferences(
                "sim_setting_preference", Context.MODE_PRIVATE);
        int prevType = prefs.getInt("dual_sim_mode", 3);
        
        Log.i(TAG, "[processDualSimModeChanged]type:" + type + "|prevType:" + prevType);
        switch(type) {
        case 0: {
            if (prevType == 1) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else if (prevType == 2) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 1: {
            if (prevType == 0) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 3) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 2: {
            if (prevType == 0) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 3) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
            break;
        }
        case 3: {
            if (prevType == 1) {
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else if (prevType == 2) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
            break;
        }
            default:
                break;
        }
        
      SharedPreferences.Editor editor= prefs.edit();
      editor.putInt("dual_sim_mode", type);
      editor.commit();
    }
    
    void processSimStateChanged(Intent intent) {
        Log.i(TAG, "processSimStateChanged");
        String phoneName = intent.getStringExtra(Phone.PHONE_NAME_KEY);
        String iccState = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
        int slotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);

        Log.i(TAG, "mPhoneName:" + phoneName + "|mIccStae:" + iccState
                + "|mySlotId:" + slotId);
        // Check SIM state, and start service to remove old sim data if sim
        // is not ready.
        if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(iccState)) {
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
        if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(iccState)
                || IccCard.INTENT_VALUE_ICC_LOCKED.equals(iccState)
                || IccCard.INTENT_VALUE_LOCKED_NETWORK.equals(iccState)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
        boolean phoneBookReadyReceived = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            phoneBookReadyReceived = PHBREADYRECEIVED[slotId];
            Log.i(TAG, "Gemini PHBREADYRECEIVED[" + slotId + "] = " + PHBREADYRECEIVED[slotId]);
        } else {
            phoneBookReadyReceived = PHBREADYRECEIVED[0];
            Log.i(TAG, "Single PHBREADYRECEIVED[0]" + PHBREADYRECEIVED[slotId]);
        }
        Log.i(TAG, "phoneBookReadyReceived = " + phoneBookReadyReceived);
        if (phoneBookReadyReceived && SimCardUtils.isPhoneBookReady(slotId)
                && IccCard.INTENT_VALUE_ICC_READY.equals(iccState)) {
            Log.i(TAG, "PhoneBook is ready and ICC is ready");
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
    }
    
    void processSimFilesChanged(int slotId) {
        Log.i(TAG, "processSimStateChanged:" + slotId);
        if (SimCardUtils.isPhoneBookReady(slotId)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }
    
    void processSimInfoUpdateForSettingChanged(Intent intent) {
        Log.i(TAG, "processSimInfoUpdateForSettingChanged:" + intent.toString());
        SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        } else {
            SIMInfoWrapper.getDefault();
        }
    }
    
    void processSimInfoUpdate(Intent intent) {
        Log.i(TAG, "processSimInfoUpdate:" + intent.toString());
        SIMInfoWrapper simInfoCache = SIMInfoWrapper.getDefault();
        if (simInfoCache == null)
            return;
        
        SharedPreferences prefs = mContext.getSharedPreferences(
                "sim_setting_preference", Context.MODE_PRIVATE);
        
        long oldSimIdInSlot0 = prefs.getLong("slot_0", -1);
        long oldSimIdInSlot1 = prefs.getLong("slot_1", -1);
        
        simInfoCache.updateSimInfoCache();
        
        List<SIMInfo> allSimInfoList = SIMInfo.getAllSIMList(mContext);
        
        boolean slot0Update= false;
        boolean slot1Update= false;
        
        for(SIMInfo simInfo: allSimInfoList) {
            long newSimId = simInfo.mSimId;
            int newSlotId = simInfo.mSlot;
            long oldSimId = -1;
            if (newSlotId == SimCardUtils.SimSlot.SLOT_ID1) {
                //for Single card, or slot0 in Gemini.
                if (oldSimIdInSlot0 != newSimId) {
                    oldSimId = oldSimIdInSlot0;
                    SharedPreferences.Editor editor= prefs.edit();
                    editor.putLong("slot_0", newSimId);
                    editor.commit();
                }
                slot0Update = true;
            } else if (newSlotId == SimCardUtils.SimSlot.SLOT_ID2) {
                //Only for slot1 in Gemini
                if (oldSimIdInSlot1 != newSimId) {
                    oldSimId = oldSimIdInSlot1;
                    SharedPreferences.Editor editor= prefs.edit();
                    editor.putLong("slot_1", newSimId);
                    editor.commit();
                }
                slot1Update = true;
            }
            if (oldSimId >= 0) {
                final long prevSimId = oldSimId;
                final long currSimId = newSimId;
                new Thread() {
                    public void run() {
                        
                    ContentValues values = new ContentValues(1);
                    values.put(RawContacts.INDICATE_PHONE_SIM, currSimId);
                    String where = RawContacts.INDICATE_PHONE_SIM + "=" + prevSimId;
                    
                    mContext.getContentResolver().update(RawContacts.CONTENT_URI,
                            values, where, null);
                    mContext.getContentResolver().update(Contacts.CONTENT_URI,
                            values, where, null);
                    }
                }.start();
            }
        }
        if (!slot0Update) {
            SharedPreferences.Editor editor= prefs.edit();
            editor.putLong("slot_0", -1);
            editor.commit();
        }
        if (!slot1Update) {
            SharedPreferences.Editor editor= prefs.edit();
            editor.putLong("slot_1", -1);
            editor.commit();
        }
    }

    void processIpoShutDown() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        } else {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
    }

    void processPhoneReset(Intent intent) {
	    Log.i(TAG, "processPhoneReset");
	    SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        }
    	if (FeatureOption.MTK_GEMINI_SUPPORT) {
	        int slotId = intent.getIntExtra("SimId", -1);
	        if(slotId != -1){    
		        Log.i(TAG, "processPhoneReset" + slotId);
            	startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
	        }
        } else {
	    Log.i(TAG, "processPhoneReset0");
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }
}
