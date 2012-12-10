package com.mediatek.contacts;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.ServiceManager;
import android.provider.Telephony.SIMInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;

import com.android.contacts.util.Constants;
import com.mediatek.CellConnService.CellConnMgr;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

public class CallOptionHelper {

    private static final String TAG = "CallOptionHelper";

    public static final int DIAL_TYPE_SIP = 0;
    public static final int DIAL_TYPE_VIDEO = 1;
    public static final int DIAL_TYPE_VOICE = 2;

    public static final int MAKE_CALL_REASON_OK = 0;

    public static final int MAKE_CALL_REASON_3G_SERVICE_OFF = 1;
    public static final int MAKE_CALL_REASON_SIP_DISABLED = 2;
    public static final int MAKE_CALL_REASON_SIP_NO_INTERNET = 3;
    public static final int MAKE_CALL_REASON_SIP_START_SETTINGS = 4;
    public static final int MAKE_CALL_REASON_ASK = 5;
    public static final int MAKE_CALL_REASON_ASSOCIATE_MISSING = 6;
    public static final int MAKE_CALL_REASON_MISSING_VOICE_MAIL_NUMBER = 7;
    public static final int MAKE_CALL_REASON_MAX = MAKE_CALL_REASON_MISSING_VOICE_MAIL_NUMBER;

    protected Context mContext;

    protected Callback mCallback;

    protected CallOptionHelper(Context context) {
        mContext = context;
    }

    public static CallOptionHelper getInstance(Context context) {
        return new CallOptionHelper(context);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    public void makeCall(Intent intent) {
        log("makeCall, intent = " + intent + " uri = " + intent.getData());
        int type = DIAL_TYPE_VOICE;

        if(intent.getBooleanExtra(Constants.EXTRA_IS_VIDEO_CALL, false))
            type = DIAL_TYPE_VIDEO;
        
        boolean followSimSetting = intent.getBooleanExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, false);

        final Uri uri = intent.getData();
        final String scheme = uri.getScheme();
        
        //with sip scheme and tell us not follow the sim management, then it can be out by sip phone
        if(scheme.equals("sip") && !followSimSetting)
            type = DIAL_TYPE_SIP;

        // for voice mail, dial type is always VOICE
        if(Constants.VOICEMAIL_URI.equals(intent.getData().toString()))
            type = DIAL_TYPE_VOICE;

        String number = "";
        try {
            number = PhoneNumberUtils.getNumberFromIntent(intent, mContext);
        } catch(Exception e) {
            log(e.getMessage());
        }

        final long originalSim = intent.getLongExtra(Constants.EXTRA_ORIGINAL_SIM_ID, Settings.System.DEFAULT_SIM_NOT_SET);

        makeCall(number, type, originalSim);
    }

    public class CallbackArgs {

        public int reason;
        public int type;
        public String number;
        public Object args;
        public long id;

        public CallbackArgs() {
            //
        }

        public CallbackArgs(int _reason, int _type, long _id, String _number, Object _args) {
            reason = _reason;
            type = _type;
            number = _number;
            args = _args;
            id = _id;
        }
    }

    public class AssociateSimMissingArgs {
        public static final int ASSOCIATE_SIM_MISSING_YES_NO = 0;
        public static final int ASSOCIATE_SIM_MISSING_YES_OTHER = 1;

        public SIMInfo viaSimInfo;
        public long suggested;

        // ASSOCIATE_SIM_MISSING_YES_NO : only one sim insert, show dialog with 'Yes' or 'No'
        // ASSOCIATE_SIM_MISSING_YES_OTHER : more than one sim inserted, show dialog with 'Yes or other'
        public int type;

        public AssociateSimMissingArgs() {
            //
        }
    }

    public interface Callback {
        public void onMakeCall(CallbackArgs args);
    }

    protected void makeVoiceCall(String number, int type, long originalSim, CallbackArgs callbackArgs) {
        Profiler.trace(Profiler.CallOptionHelperEnterMakeVoiceCall);
        long suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
        long associateSim = (int)Settings.System.DEFAULT_SIM_NOT_SET;
        int associateSimInserts = 0;
        boolean originalSimInsert = false;

        final SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getDefault();

        final long defaultSim = Settings.System.getLong(mContext.getContentResolver(),
                Settings.System.VOICE_CALL_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);

        final ITelephony phoneMgr = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

        SIMInfo simInfo;
        final ArrayList associateSims = SimAssociateHandler.getInstance().query(number);
        final boolean hasAssociateSims = associateSims != null && associateSims.size() > 0;
        if(hasAssociateSims) {
            for(Object item : associateSims) {
                final int temp = ((Integer)item).intValue();
                final int slot = simInfoWrapper.getSimSlotById(temp);
                try {
                    if(slot >= 0 && phoneMgr.isSimInsert(slot)) {
                        associateSimInserts++;
                        associateSim = temp;
                    }
                } catch(Exception e) {
                    log(e.getMessage());
                }
            }
        }

        // for cta case, when there are no sim cards inserted it should can make a call...
        if(simInfoWrapper.getInsertedSimCount() == 0 && defaultSim != Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            final boolean internetCallOn = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0) == 1;

            // if internet call option is off then dial out directly
            if(!internetCallOn) {
                callbackArgs.type = DIAL_TYPE_VOICE;
                callbackArgs.id = 0;
                callbackArgs.reason = MAKE_CALL_REASON_OK;
                return;
            } else {
                // internet call option is on, ask user whether to use sip ?
                callbackArgs.reason = MAKE_CALL_REASON_ASK;
                callbackArgs.args = Long.valueOf(Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
                return;
            }
        }

        if(defaultSim == Settings.System.DEFAULT_SIM_NOT_SET)
            return;

        if(originalSim != Settings.System.DEFAULT_SIM_NOT_SET) {
            final int slot = simInfoWrapper.getSimSlotById((int)originalSim);
            try {
                originalSimInsert = slot >= 0 && phoneMgr.isSimInsert(slot);                
            } catch(Exception e) {
                log(e.getMessage());
                originalSimInsert = false;
            }
            
            if (originalSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                originalSimInsert = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0) == 1;
            }
        }

        log("makeVoiceCall, number = "+number+" type = "+type+" originalSim = "+originalSim+" associateSims = "+associateSims);

        // default sim is always ask
        if(defaultSim == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
            //In some case there is no sim inserted and no internet account, but setting tell us that always ask, it's strange!!
            //ALPS00235140
            final int enabled = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
            final int count = simInfoWrapper.getInsertedSimCount();
            
            if (enabled == 0 && count == 0) {
                callbackArgs.reason = MAKE_CALL_REASON_OK;
                return ;
            }
            // default sim is always ask, show sim selection dialog
            // but we must found if there is any sim to be suggested
            log("always, associateSimInserts = "+associateSimInserts+" originalSim = "+originalSim);
            if(associateSimInserts > 1) {
                suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
            } else if(associateSimInserts == 1) {
                suggestedSim = associateSim;
            } else if(originalSimInsert)
                suggestedSim = originalSim;

            callbackArgs.args = suggestedSim;
            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            Profiler.trace(Profiler.CallOptionHelperLeaveMakeVoiceCall);
            return;
        } 
        
        //ALPS00230449, when set default sim as internet and dialout a call from call with original sim
        if (!hasAssociateSims && originalSim !=  Settings.System.DEFAULT_SIM_NOT_SET 
                && defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET 
                && originalSim != defaultSim) {
            if (originalSimInsert) {
                callbackArgs.args = originalSim;
            } else {
                callbackArgs.args = Settings.System.DEFAULT_SIM_NOT_SET;
            }
            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            return ;
        }

        //ALPS00234703, when set default sim as internet and dialout from a call with associate
        if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            if (hasAssociateSims 
                    || (originalSim != Settings.System.DEFAULT_SIM_NOT_SET && originalSim != Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)) {
                if(associateSimInserts > 1) {
                    suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
                } else if(associateSimInserts == 1) {
                    suggestedSim = associateSim;
                } else if(originalSimInsert) {
                    suggestedSim = originalSim;
                }
                
                callbackArgs.args = suggestedSim;
                callbackArgs.reason = MAKE_CALL_REASON_ASK;
                
                return ;
            }
            
            callbackArgs.type = CallOptionHelper.DIAL_TYPE_SIP;
            callbackArgs.reason = MAKE_CALL_REASON_OK;
            return;
        }

        // no associate sim nor originalSim
        if(originalSim == Settings.System.DEFAULT_SIM_NOT_SET && !hasAssociateSims) {
            log("deaultSim = "+defaultSim);
            callbackArgs.reason = MAKE_CALL_REASON_OK;
            callbackArgs.id = defaultSim;
            Profiler.trace(Profiler.CallOptionHelperLeaveMakeVoiceCall);
            return;
        }

        // only has original sim
        if(originalSim != Settings.System.DEFAULT_SIM_NOT_SET && !hasAssociateSims) {
            // only has original sim
            if(defaultSim == originalSim || !originalSimInsert) {
                // ok, dial out
                callbackArgs.reason = MAKE_CALL_REASON_OK;
                callbackArgs.id = defaultSim;
                return;
            }

            // originalSim is not insert, show sim selection dialog
            suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
            if(originalSimInsert)
                suggestedSim = originalSim;

            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            callbackArgs.args = suggestedSim;
            return;
        }

        // only has associate sim
        if(originalSim == Settings.System.DEFAULT_SIM_NOT_SET && hasAssociateSims) {
            // more than 2 associate sims !!!
            if(associateSimInserts >= 2) {
                callbackArgs.args = Settings.System.DEFAULT_SIM_NOT_SET;
                callbackArgs.reason = MAKE_CALL_REASON_ASK;
                return;
            }

            if (associateSims.size() == 1) {
                associateSim = ((Integer)associateSims.get(0)).intValue();
            } else if (associateSims.size() >= 2) {
                //get the inserted sim
                for(Object item : associateSims) {
                    final int temp = ((Integer)item).intValue();
                    final int slot = simInfoWrapper.getSimSlotById(temp);
                    try {
                        if(slot >= 0 && phoneMgr.isSimInsert(slot)) {
                            associateSim = temp;
                            break;
                        }
                    } catch(Exception e) {
                        log(e.getMessage());
                    }
                }
            }
            
            if(associateSimInserts == 1) {
                if(defaultSim == associateSim) {
                    callbackArgs.reason = MAKE_CALL_REASON_OK;
                    callbackArgs.id = defaultSim;
                    return;
                } else {
                    callbackArgs.reason = MAKE_CALL_REASON_ASK;
                    callbackArgs.args = associateSim;
                }
                return;
            }
        }

        // both has orignalSim and associateSim ...
        if(defaultSim == originalSim && defaultSim == associateSim) {
            callbackArgs.reason = MAKE_CALL_REASON_OK;
            callbackArgs.id = defaultSim;
            return;
        }
        
      //default is the orignal sim and associateSim missing
        if (defaultSim == originalSim && hasAssociateSims && associateSimInserts == 0) {
            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            callbackArgs.args = originalSim;
            return ;
        }
        
      //associateSim missing and the original sim exist (ALPS00251172)
        if (originalSim != Settings.System.DEFAULT_SIM_NOT_SET &&
            hasAssociateSims && associateSimInserts == 0) {
            final int slot = simInfoWrapper.getSimSlotById((int)originalSim);
            try {
                if ((slot >= 0) && (originalSim != defaultSim) && (phoneMgr.isSimInsert(slot))) {
                    callbackArgs.args = originalSim;
                    callbackArgs.reason = MAKE_CALL_REASON_ASK;
                    return ;
                }
            } catch (Exception e) {
                log("catch the remote exception!");
            }
        }
        
        if (associateSimInserts >= 2) {
            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            callbackArgs.args = Settings.System.DEFAULT_SIM_NOT_SET;
            return ;
        }

        if(associateSimInserts == 1) {
            callbackArgs.reason = MAKE_CALL_REASON_ASK;
            callbackArgs.args = associateSim;
        } else {
          //If the associate sim missing, the associateSim maybe not be set.
            if (associateSim == Settings.System.DEFAULT_SIM_NOT_SET) {
                associateSim = ((Integer)associateSims.get(0)).intValue();
            }
            callbackArgs.id = associateSim;
            callbackArgs.reason = MAKE_CALL_REASON_ASSOCIATE_MISSING;
            AssociateSimMissingArgs associateSimMissingArgs = new AssociateSimMissingArgs();
            if (SIMInfoWrapper.getDefault().getInsertedSimCount() <= 1) {
                associateSimMissingArgs.type = AssociateSimMissingArgs.ASSOCIATE_SIM_MISSING_YES_NO;

                final long viaSimId = originalSimInsert ? originalSim : defaultSim;

                if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)
                    associateSimMissingArgs.suggested = Settings.System.VOICE_CALL_SIM_SETTING_INTERNET;
                else {
                    associateSimMissingArgs.viaSimInfo = simInfoWrapper.getSimInfoById((int)viaSimId);
                }
            } else {
                associateSimMissingArgs.type = AssociateSimMissingArgs.ASSOCIATE_SIM_MISSING_YES_OTHER;
                associateSimMissingArgs.suggested = originalSimInsert ? originalSim : defaultSim;
                if (defaultSim != Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK 
                        && defaultSim != Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                    associateSimMissingArgs.viaSimInfo = simInfoWrapper.getSimInfoById((int)defaultSim);
                }
            }
            callbackArgs.args = associateSimMissingArgs;
        }
    }

    protected void makeCall(String number, int type, long originalSim) {
        Intent intent = null;
        CallbackArgs callbackArgs = new CallbackArgs(MAKE_CALL_REASON_OK, DIAL_TYPE_VOICE, 0, number, null);
        log("makeCall, number = "+number+" type = "+type+" originalSim = "+originalSim);
        switch(type) {
            case DIAL_TYPE_SIP:
                final int enabled = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                if(enabled == 1)
                    callbackArgs.type = DIAL_TYPE_SIP;
                else
                    callbackArgs.reason = MAKE_CALL_REASON_SIP_DISABLED;
                break;
            case DIAL_TYPE_VIDEO:{
                    final ITelephony phoneMgr = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

                    int slot;
                    try {
                        slot = FeatureOption.MTK_GEMINI_3G_SWITCH ? phoneMgr.get3GCapabilitySIM() : 0;
                    } catch(Exception e) {
                        log(e.getMessage());
                        slot = 0;
                    }

                    if (slot == -1)
                        callbackArgs.reason = MAKE_CALL_REASON_3G_SERVICE_OFF;
                    else {
                        callbackArgs.id = slot;
                        callbackArgs.type = DIAL_TYPE_VIDEO;
                    }
                }
                break;
            case DIAL_TYPE_VOICE:
                makeVoiceCall(number, type, originalSim, callbackArgs);
                break;
        }

        mCallback.onMakeCall(callbackArgs);
    }

}
