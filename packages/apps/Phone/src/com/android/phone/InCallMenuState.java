package com.android.phone;

import android.util.Log;
import android.view.ViewConfiguration;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.phone.PhoneFeatureConstants.FeatureOption;

public class InCallMenuState {

    private static final String TAG = "InCallMenuState";

    public boolean canHangupAll;
    public boolean canHangupHolding;
    public boolean canHangupActiveAndAnswerWaiting;
    public boolean canECT;
    public boolean canVTVoiceAnswer;
    public boolean canMuteRinger;

    public boolean hasPermanentMenuKey;

    protected InCallScreen mInCallScreen;
    protected CallManager mCM;

    public InCallMenuState(InCallScreen incallScreen, CallManager cm) {
        mInCallScreen = incallScreen;
        mCM = cm;

        hasPermanentMenuKey = ViewConfiguration.get(mInCallScreen).hasPermanentMenuKey();
    }

    public void update() {
        canHangupAll = canHangupAll(mCM);
        canHangupHolding = canHangupHolding(mCM);
        canHangupActiveAndAnswerWaiting = canHangupActiveAndAnswerWaiting(mCM);
        canECT = canECT(mCM);
        canVTVoiceAnswer = canVTVoiceAnswer();
        canMuteRinger = canMuteRinger();
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static boolean canHangupAll(CallManager cm) {
        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();
        Call rgCall = cm.getFirstActiveRingingCall();

        boolean retval = false;
        if(null != bgCall && bgCall.getState() == Call.State.HOLDING) {
            if(null != fgCall && fgCall.getState() == Call.State.ACTIVE)
                retval = true;
            else if(PhoneUtils.hasActivefgEccCall(cm))
                retval = true;
        }

        if(rgCall.getState() == Call.State.INCOMING || rgCall.getState() == Call.State.WAITING) {
            if(bgCall.getState() == Call.State.HOLDING || fgCall.getState() == Call.State.ACTIVE)
                retval = true;
        }

        log("canHangupAll = "+retval);
        return retval;
    }

    public static boolean canHangupHolding(CallManager cm) {
        Call bgCall = cm.getFirstActiveBgCall();
        return bgCall.getState() != Call.State.IDLE;
    }

    public static boolean canHangupActiveAndAnswerWaiting(CallManager cm) {
        boolean retval = false;

        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();
        Call rgCall = cm.getFirstActiveRingingCall();

        final boolean isFgActive  = fgCall.getState() == Call.State.ACTIVE;
        final boolean isBgIdle = bgCall.getState() == Call.State.IDLE;
        final boolean isRgWaiting = rgCall.getState() == Call.State.WAITING;

        if(isFgActive && isBgIdle && isRgWaiting) {
            if(fgCall.getPhone() == rgCall.getPhone() 
                    && !rgCall.getLatestConnection().isVideo())
                retval = true;
        }

        return retval;
    }

    public static boolean canECT(CallManager cm) {
        boolean retval = false;

        final boolean hasActiveFgCall = cm.hasActiveFgCall();
        final boolean hasActiveBgCall = cm.hasActiveBgCall();
        final boolean hasActiveRingingCall = cm.hasActiveRingingCall();

        if(hasActiveRingingCall)
        {
          retval = false;
          return retval;
        }

        if(hasActiveFgCall && hasActiveBgCall) {
            final boolean isFgSipPhone = cm.getActiveFgCall().getPhone().getPhoneType() == Phone.PHONE_TYPE_SIP;
            final boolean isBgSipPhone = cm.getFirstActiveBgCall().getPhone().getPhoneType() == Phone.PHONE_TYPE_SIP;
            if(!isFgSipPhone && !isBgSipPhone)
                retval = true;
        }

        return retval;
    }
    
    public static boolean canVTVoiceAnswer() {
        if (FeatureOption.MTK_PHONE_VT_VOICE_ANSWER == true
                && FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            if (PhoneApp.getInstance().isVTRinging()) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean canMuteRinger() {
        return PhoneApp.getInstance().ringer.isRinging();
    }
    
    public static boolean canIncomingMenuShow(CallManager cm) {
        return InCallMenuState.canHangupActiveAndAnswerWaiting(cm) ||
               //InCallMenuState.canMuteRinger() ||
               InCallMenuState.canVTVoiceAnswer();
    }
}
