/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.phone;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.net.sip.SipManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.ServiceManager;
import com.mediatek.dmagent.DMAgent;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IExtendedNetworkService;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.cdma.CdmaConnection;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.sip.SipPhone;
import android.provider.Telephony.SIMInfo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.gemini.*;
import com.android.phone.PhoneFeatureConstants.FeatureOption;
import android.os.ServiceManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;

/**
 * Misc utilities for the Phone app.
 */
public class PhoneUtils {
    private static final String LOG_TAG = "PhoneUtils";
//Google: private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final boolean DBG = true;

    /** Control stack trace for Audio Mode settings */
    private static final boolean DBG_SETAUDIOMODE_STACK = false;

    /** Identifier for the "Add Call" intent extra. */
    static final String ADD_CALL_MODE_KEY = "add_call_mode";

    // State of the Phone's audio modes
    // Each state can move to the other states, but within the state only certain
    //  transitions for AudioManager.setMode() are allowed.
    static final int AUDIO_IDLE = 0;  /** audio behaviour at phone idle */
    static final int AUDIO_RINGING = 1;  /** audio behaviour while ringing */
    static final int AUDIO_OFFHOOK = 2;  /** audio behaviour while in call. */
    private static int sAudioBehaviourState = AUDIO_IDLE;

    /** Speaker state, persisting between wired headset connection events */
    private static boolean sIsSpeakerEnabled = false;

    /** Hash table to store mute (Boolean) values based upon the connection.*/
    private static Hashtable<Connection, Boolean> sConnectionMuteTable =
        new Hashtable<Connection, Boolean>();

    /** Static handler for the connection/mute tracking */
    private static ConnectionHandler mConnectionHandler;

    /** Phone state changed event*/
    private static final int PHONE_STATE_CHANGED = -1;

//MTK begin
    static final int PHONE_SPEECH_INFO = -2;
    private static final int PHONE_STATE_CHANGED2 = -3;
    static final int PHONE_SPEECH_INFO2 = -4;
//MTK end


    /** Define for not a special CNAP string */
    private static final int CNAP_SPECIAL_CASE_NO = -1;

    // Extended network service interface instance
    private static IExtendedNetworkService mNwService = null;
    // used to cancel MMI command after 15 seconds timeout for NWService requirement
    private static Message mMmiTimeoutCbMsg = null;

    /** Noise suppression status as selected by user */
    private static boolean sIsNoiseSuppressionEnabled = true;
    private static final int MIN_LENGTH = 6;
    private static final int MIN_WIDTH = 270;

     /** For TTY usage */
    private static String ttyMode = "tty_off";
    private static boolean isOpen = false;
    
    public static void setTtyMode (String mode)
    {
        ttyMode = mode;
    }
    
    public static void openTTY() {
        if (!PhoneApp.getInstance().isEnableTTY()) {
            return;
        }
        Context context = PhoneApp.getInstance();
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (!ttyMode.equals("tty_off") && !isOpen)
        {
            audioManager.setParameters("tty_mode="+ ttyMode);
            isOpen = true;
        }
    }
    
    public static String DUALMIC_MODE = "Enable_Dual_Mic_Setting";

    //Add for recording the USSD dialog, and use to dismiss the dialog when enter airplane mode
    private static Dialog mDialog = null;
    private static boolean mbMmiFinished = false;
    //Used for the activity ussd dialog
    static UssdAlertActivity mUssdActivity = null;
    private static MmiCode curCode = null;

    // MTK_OP01_PROTECT_START
    // MTK_OP02_PROTECT_START
    public static String mOptr = null;
    public static String mSpec = null;
    public static String mSeg = null;
    // MTK_OP02_PROTECT_END
    // MTK_OP01_PROTECT_END	

    /**
     * Handler that tracks the connections and updates the value of the
     * Mute settings for each connection as needed.
     */
    private static class ConnectionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case PHONE_STATE_CHANGED:
//MTK add below one line
                case PHONE_STATE_CHANGED2:
                    if (DBG) log("ConnectionHandler: updating mute state for each connection");

                    CallManager cm = (CallManager) ar.userObj;

                    // update the foreground connections, if there are new connections.
                    // Have to get all foreground calls instead of the active one
                    // because there may two foreground calls co-exist in shore period
                    // (a racing condition based on which phone changes firstly)
                    // Otherwise the connection may get deleted.
                    List<Connection> fgConnections = new ArrayList<Connection>();
                    for (Call fgCall : cm.getForegroundCalls()) {
                        if (!fgCall.isIdle()) {
                            fgConnections.addAll(fgCall.getConnections());
                        }
                    }
                    for (Connection cn : fgConnections) {
                        if (sConnectionMuteTable.get(cn) == null) {
                            sConnectionMuteTable.put(cn, Boolean.FALSE);
                        }
                    }

                    // mute is connection based operation, we need loop over
                    // all background calls instead of the first one to update
                    // the background connections, if there are new connections.
                    List<Connection> bgConnections = new ArrayList<Connection>();
                    for (Call bgCall : cm.getBackgroundCalls()) {
                        if (!bgCall.isIdle()) {
                            bgConnections.addAll(bgCall.getConnections());
                        }
                    }
                    for (Connection cn : bgConnections) {
                        if (sConnectionMuteTable.get(cn) == null) {
                          sConnectionMuteTable.put(cn, Boolean.FALSE);
                        }
                    }

                    // Check to see if there are any lingering connections here
                    // (disconnected connections), use old-school iterators to avoid
                    // concurrent modification exceptions.
                    Connection cn;
                    for (Iterator<Connection> cnlist = sConnectionMuteTable.keySet().iterator();
                            cnlist.hasNext();) {
                        cn = cnlist.next();
                        if (!fgConnections.contains(cn) && !bgConnections.contains(cn)) {
                            if (DBG) log("connection '" + cn + "' not accounted for, removing.");
                            cnlist.remove();
                        }
                    }

                    // Restore the mute state of the foreground call if we're not IDLE,
                    // otherwise just clear the mute state. This is really saying that
                    // as long as there is one or more connections, we should update
                    // the mute state with the earliest connection on the foreground
                    // call, and that with no connections, we should be back to a
                    // non-mute state.
                    if (cm.getState() != Phone.State.IDLE) {
                        restoreMuteState();
                    } else {
                        setMuteInternal(cm.getFgPhone(), false);
                    }

                    break;
//MTK begin:
                case PHONE_SPEECH_INFO2:
                     if (DBG) log("ConnectionHandler: PHONE_SPEECH_INFO2");
                     MTKCallManager cm4 = (MTKCallManager) ar.userObj;
                     setAudioMode();
                     cm4.unregisterForSpeechInfoGemini(mConnectionHandler, Phone.GEMINI_SIM_2);
                     break;
                case PHONE_SPEECH_INFO:
                    if (DBG) log("ConnectionHandler: PHONE_SPEECH_INFO");
                     //This is ugly, but now no more elegant way to resolved this.
                    boolean isMTKCm = ar.userObj instanceof MTKCallManager;
                    if(FeatureOption.MTK_GEMINI_SUPPORT && isMTKCm){
                    	MTKCallManager cm3 = (MTKCallManager) ar.userObj;
                    	setAudioMode();
                        cm3.unregisterForSpeechInfoGemini(mConnectionHandler, Phone.GEMINI_SIM_1);
                    } else {
                        CallManager cm2 = (CallManager) ar.userObj;
                        setAudioMode();
                        cm2.unregisterForSpeechInfo(mConnectionHandler);
                    }
                    break;
//MTK end
            }
        }
    }


    private static ServiceConnection ExtendedNetworkServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            if (DBG) log("Extended NW onServiceConnected");
            mNwService = IExtendedNetworkService.Stub.asInterface(iBinder);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            if (DBG) log("Extended NW onServiceDisconnected");
            mNwService = null;
        }
    };

    /**
     * Register the ConnectionHandler with the phone, to receive connection events
     */
    public static void initializeConnectionHandler(CallManager cm) {
        if (mConnectionHandler == null) {
            mConnectionHandler = new ConnectionHandler();
        }

        // pass over cm as user.obj
//Google code: cm.registerForPreciseCallStateChanged(mConnectionHandler, PHONE_STATE_CHANGED, cm);
//MTK begin:
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            MTKCallManager cmGemini = PhoneApp.getInstance().mCMGemini;
            cmGemini.registerForPreciseCallStateChangedGemini(mConnectionHandler, PHONE_STATE_CHANGED, cm, Phone.GEMINI_SIM_1); 
            cmGemini.registerForPreciseCallStateChangedGemini(mConnectionHandler, PHONE_STATE_CHANGED2, cm, Phone.GEMINI_SIM_2);
        } else {			
            cm.registerForPreciseCallStateChanged(mConnectionHandler, PHONE_STATE_CHANGED, cm);
        }
//MTK end

        // Extended NW service
        Intent intent = new Intent("com.android.ussd.IExtendedNetworkService");
        cm.getDefaultPhone().getContext().bindService(intent,
                ExtendedNetworkServiceConnection, Context.BIND_AUTO_CREATE);
        if (DBG) log("Extended NW bindService IExtendedNetworkService");

    }

    /** This class is never instantiated. */
    private PhoneUtils() {
    }

//MTK begin:
    //static method to set the audio control state.
    static void setAudioControlState(int newState) {
        sAudioBehaviourState = newState;
    }

    static int getAudioControlState() {
        return sAudioBehaviourState;
    }
//MTK end

    /**
     * Answer the currently-ringing call.
     *
     * @return true if we answered the call, or false if there wasn't
     *         actually a ringing incoming call, or some other error occurred.
     *
     * @see answerAndEndHolding()
     * @see answerAndEndActive()
     */
    static boolean answerCall(Call ringing) {
        if (DBG) log("answerCall(" + ringing + ")...");
        final PhoneApp app = PhoneApp.getInstance();

        // If the ringer is currently ringing and/or vibrating, stop it
        // right now (before actually answering the call.)
        app.getRinger().stopRing();
//MTK add below one line:
        PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);

        boolean answered = false;
        Phone phone = ringing.getPhone();
        boolean phoneIsCdma = (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
        BluetoothHandsfree bthf = null;

        if (phoneIsCdma) {
            // Stop any signalInfo tone being played when a Call waiting gets answered
            if (ringing.getState() == Call.State.WAITING) {
                final CallNotifier notifier = app.notifier;
                notifier.stopSignalInfoTone();
            }
        }

        if (ringing != null && ringing.isRinging()) {
            if (DBG) log("answerCall: call state = " + ringing.getState());
            try {
                if (phoneIsCdma) {
                    if (app.cdmaPhoneCallState.getCurrentCallState()
                            == CdmaPhoneCallState.PhoneCallState.IDLE) {
                        // This is the FIRST incoming call being answered.
                        // Set the Phone Call State to SINGLE_ACTIVE
                        app.cdmaPhoneCallState.setCurrentCallState(
                                CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE);
                    } else {
                        // This is the CALL WAITING call being answered.
                        // Set the Phone Call State to CONF_CALL
                        app.cdmaPhoneCallState.setCurrentCallState(
                                CdmaPhoneCallState.PhoneCallState.CONF_CALL);
                        // Enable "Add Call" option after answering a Call Waiting as the user
                        // should be allowed to add another call in case one of the parties
                        // drops off
                        app.cdmaPhoneCallState.setAddCallMenuStateAfterCallWaiting(true);

                        // If a BluetoothHandsfree is valid we need to set the second call state
                        // so that the Bluetooth client can update the Call state correctly when
                        // a call waiting is answered from the Phone.
                        bthf = app.getBluetoothHandsfree();
                        if (bthf != null) {
                            bthf.cdmaSetSecondCallState(true);
                        }
                    }
                }

                //if (DBG) log("sPhone.acceptCall");
                app.mCM.acceptCall(ringing);
                answered = true;

                // Always reset to "unmuted" for a freshly-answered call
                //setMute(false);

                setAudioMode();

                // Check is phone in any dock, and turn on speaker accordingly
                activateSpeakerIfDocked(phone);

                if (FeatureOption.MTK_TB_APP_CALL_FORCE_SPEAKER_ON == true)
                {
                  BluetoothHandsfree BtHandsfree = app.getBluetoothHandsfree();

                  if (!app.isHeadsetPlugged() && !(BtHandsfree != null && BtHandsfree.isAudioOn()) && !(isSpeakerOn(phone.getContext())))
                  {
                    Log.e("MTK_TB_APP_CALL_FORCE_SPEAKER_ON", "PhoneUtils.turnOnSpeaker");
                    turnOnSpeaker(phone.getContext(), true, true);
                  }
                }
            } catch (CallStateException ex) {
                Log.w(LOG_TAG, "answerCall: caught " + ex, ex);

                if (phoneIsCdma) {
                    // restore the cdmaPhoneCallState and bthf.cdmaSetSecondCallState:
                    app.cdmaPhoneCallState.setCurrentCallState(
                            app.cdmaPhoneCallState.getPreviousCallState());
                    if (bthf != null) {
                        bthf.cdmaSetSecondCallState(false);
                    }
                }
            }
        }
        return answered;
    }

    /**
     * Smart "hang up" helper method which hangs up exactly one connection,
     * based on the current Phone state, as follows:
     * <ul>
     * <li>If there's a ringing call, hang that up.
     * <li>Else if there's a foreground call, hang that up.
     * <li>Else if there's a background call, hang that up.
     * <li>Otherwise do nothing.
     * </ul>
     * @return true if we successfully hung up, or false
     *              if there were no active calls at all.
     */
    static boolean hangup(CallManager cm) {
        boolean hungup = false;
        Call ringing = null;
        Call fg = null;
        Call bg = null;
        DualTalkUtils dtUtils = null;
        
        if (DualTalkUtils.isSupportDualTalk) {
            dtUtils = DualTalkUtils.getInstance();
        }
        
        if (DualTalkUtils.isSupportDualTalk && dtUtils.hasMultipleRingingCall()) {
            //this can't be reached.
            ringing = dtUtils.getFirstActiveRingingCall();
            hangupForDualTalk(ringing);
            return true;
        } else if (DualTalkUtils.isSupportDualTalk && dtUtils.hasDualHoldCallsOnly()) {
            fg = dtUtils.getFirstActiveBgCall();
            ringing = dtUtils.getFirstActiveRingingCall();
        } else if (DualTalkUtils.isSupportDualTalk 
                && (dtUtils.isDualTalkMultipleHoldCase() || dtUtils.isCdmaAndGsmActive())) {
            fg = dtUtils.getActiveFgCall();
            bg = dtUtils.getFirstActiveBgCall();
            ringing = dtUtils.getFirstActiveRingingCall();
        } else {
            ringing = cm.getFirstActiveRingingCall();
            fg = cm.getActiveFgCall();
            bg = cm.getFirstActiveBgCall();
        }

        if (!ringing.isIdle()) {
            if (DBG) log("hangup(): hanging up ringing call");
            hungup = hangupRingingCall(ringing);
        } else if (!fg.isIdle() || fg.state == Call.State.DISCONNECTING) {
            if (DBG) log("hangup(): hanging up foreground call");
            hungup = hangup(fg);
        } else if (!bg.isIdle() || bg.state == Call.State.DISCONNECTING) {
            if (DBG) log("hangup(): hanging up background call");
            hungup = hangup(bg);
        } else {
            // No call to hang up!  This is unlikely in normal usage,
            // since the UI shouldn't be providing an "End call" button in
            // the first place.  (But it *can* happen, rarely, if an
            // active call happens to disconnect on its own right when the
            // user is trying to hang up..)
            log("hangup(): no active call to hang up");
        }
        if (DBG) log("==> hungup = " + hungup);

        return hungup;
    }

    static boolean hangupRingingCall(Call ringing) {
        if (DBG) log("hangup ringing call");
        int phoneType = ringing.getPhone().getPhoneType();
        Call.State state = ringing.getState();

        if (state == Call.State.INCOMING) {
            // Regular incoming call (with no other active calls)
            log("hangupRingingCall(): regular incoming call: hangup()");
            return hangup(ringing);
        } else if (state == Call.State.WAITING) {
            // Call-waiting: there's an incoming call, but another call is
            // already active.
            // TODO: It would be better for the telephony layer to provide
            // a "hangupWaitingCall()" API that works on all devices,
            // rather than us having to check the phone type here and do
            // the notifier.sendCdmaCallWaitingReject() hack for CDMA phones.
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                // CDMA: Ringing call and Call waiting hangup is handled differently.
                // For Call waiting we DO NOT call the conventional hangup(call) function
                // as in CDMA we just want to hangup the Call waiting connection.
                log("hangupRingingCall(): CDMA-specific call-waiting hangup");
                final CallNotifier notifier = PhoneApp.getInstance().notifier;
                notifier.sendCdmaCallWaitingReject();
                return true;
            } else {
                // Otherwise, the regular hangup() API works for
                // call-waiting calls too.
                log("hangupRingingCall(): call-waiting call: hangup()");
                return hangup(ringing);
            }
        } else {
            // Unexpected state: the ringing call isn't INCOMING or
            // WAITING, so there's no reason to have called
            // hangupRingingCall() in the first place.
            // (Presumably the incoming call went away at the exact moment
            // we got here, so just do nothing.)
            Log.w(LOG_TAG, "hangupRingingCall: no INCOMING or WAITING call");
            return false;
        }
    }

    static boolean hangupActiveCall(Call foreground) {
        if (DBG) log("hangup active call");
        return hangup(foreground);
    }

    static boolean hangupHoldingCall(Call background) {
        if (DBG) log("hangup holding call");
        return hangup(background);
    }

    /**
     * Used in CDMA phones to end the complete Call session
     * @param phone the Phone object.
     * @return true if *any* call was successfully hung up
     */
    static boolean hangupRingingAndActive(Phone phone) {
        boolean hungUpRingingCall = false;
        boolean hungUpFgCall = false;
/*Google code
        Call ringingCall = phone.getRingingCall();
        Call fgCall = phone.getForegroundCall();
*/
        /*
         * This is ugly, but we have no choice
         * ALPS00301362 & ALPS00301179
         */
        if (DualTalkUtils.isSupportDualTalk) {
            DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
            InCallScreen screen = PhoneApp.getInstance().getInCallScreenInstance();
            if ((dt != null) 
                    && (dt.hasMultipleRingingCall() || (dt.isCdmaAndGsmActive() && PhoneApp.getInstance().mCM.hasActiveRingingCall())) 
                    && (screen != null)) {
                screen.hangupRingingCall();
                return true;
            } else if (!dt.isMultiplePhoneActive()) {
                //This means the cdma phone is idle, but gsm phone not idle
                //see ALPS00354912 for details.
                Phone firstPhone = dt.getFirstPhone();
                log("Meet GSM phone case in hangupRingingAndActive!");
                if (firstPhone != null && firstPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
                    return hangup(PhoneApp.getInstance().mCM);
                }
            }
        }
        
//MTK begin:
        Call ringingCall , fgCall ;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            ringingCall = ((GeminiPhone)phone).getRingingCall();
            fgCall = ((GeminiPhone)phone).getForegroundCall();
        } else {
            ringingCall = phone.getRingingCall();
            fgCall = phone.getForegroundCall();
        }
//MTK end

        // Hang up any Ringing Call
        if (!ringingCall.isIdle()) {
            if (DBG) log("hangupRingingAndActive: Hang up Ringing Call");
            hungUpRingingCall = hangupRingingCall(ringingCall);
        }

        // Hang up any Active Call
        if (!fgCall.isIdle()) {
            if (DBG) log("hangupRingingAndActive: Hang up Foreground Call");
            hungUpFgCall = hangupActiveCall(fgCall);
        }

        return hungUpRingingCall || hungUpFgCall;
    }

    /**
     * Trivial wrapper around Call.hangup(), except that we return a
     * boolean success code rather than throwing CallStateException on
     * failure.
     *
     * @return true if the call was successfully hung up, or false
     *         if the call wasn't actually active.
     */
    static boolean hangup(Call call) {
        try {
            CallManager cm = PhoneApp.getInstance().mCM;
            //Resolved for ALPS00036146
            if (call.getState() == Call.State.ACTIVE && cm.hasActiveBgCall() && !cm.hasActiveRingingCall()) {
                // handle foreground call hangup while there is background call
                if (DBG) log("- hangup(Call): hangupForegroundResumeBackground...");
                DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
                if (DualTalkUtils.isSupportDualTalk && dt.isDualTalkMultipleHoldCase()) {
                    cm.hangupForegroundResumeBackground(dt.getFirstActiveBgCall());
                } else {
                    cm.hangupForegroundResumeBackground(cm.getFirstActiveBgCall());
                }
            } else if (call.getState() == Call.State.ACTIVE && cm.hasActiveBgCall() && cm.hasActiveRingingCall()) {
                Call fg = cm.getActiveFgCall();
                Call bg = cm.getFirstActiveBgCall();
                if (fg.getPhone() == bg.getPhone() 
                        && fg.getPhone().getPhoneType() == Phone.PHONE_TYPE_GSM
                        && cm.getRingingPhone().getPhoneType() == Phone.PHONE_TYPE_SIP) {
                    cm.hangupActiveCall(call);
                } else {
                    call.hangup();
                }
            } else {
                log("- hangup(Call): regular hangup()...");
                call.hangup();
            }
            return true;
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "Call hangup: caught " + ex, ex);
        }

        return false;
    }

    /**
     * Trivial wrapper around Connection.hangup(), except that we silently
     * do nothing (rather than throwing CallStateException) if the
     * connection wasn't actually active.
     */
    static void hangup(Connection c) {
        try {
            if (c != null) {
                c.hangup();
            }
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "Connection hangup: caught " + ex, ex);
        }
    }

    static boolean answerAndEndHolding(CallManager cm, Call ringing) {
        if (DBG) log("end holding & answer waiting: 1");
        if (!hangupHoldingCall(cm.getFirstActiveBgCall())) {
            Log.e(LOG_TAG, "end holding failed!");
            return false;
        }

        if (DBG) log("end holding & answer waiting: 2");
        return answerCall(ringing);

    }

    static boolean answerAndEndActive(CallManager cm, Call ringing) {
        if (DBG) log("answerAndEndActive()...");

        Phone fgPhone = cm.getActiveFgCall().getPhone();
        Phone ringingPhone = ringing.getPhone();

        // Unlike the answerCall() method, we *don't* need to stop the
        // ringer or change audio modes here since the user is already
        // in-call, which means that the audio mode is already set
        // correctly, and that we wouldn't have started the ringer in the
        // first place.

        // hanging up the active call also accepts the waiting call
        // while active call and waiting call are from the same phone
        // i.e. both from GSM phone
        if ( !hangupActiveCall(cm.getActiveFgCall())) {
            Log.w(LOG_TAG, "end active call failed!");
            return false;
        }

        // since hangupActiveCall() also accepts the ringing call
        // check if the ringing call was already answered or not
        // only answer it when the call still is ringing
        /*if (ringing.isRinging()) {
            return answerCall(ringing);
        }*/
        if (fgPhone != ringingPhone
            || (fgPhone == ringingPhone && (fgPhone instanceof SipPhone))) {
            return answerCall(ringing);
        }

        return true;
    }

    /**
     * For a CDMA phone, advance the call state upon making a new
     * outgoing call.
     *
     * <pre>
     *   IDLE -> SINGLE_ACTIVE
     * or
     *   SINGLE_ACTIVE -> THRWAY_ACTIVE
     * </pre>
     * @param app The phone instance.
     */
    private static void updateCdmaCallStateOnNewOutgoingCall(PhoneApp app) {
        if (app.cdmaPhoneCallState.getCurrentCallState() ==
            CdmaPhoneCallState.PhoneCallState.IDLE) {
            // This is the first outgoing call. Set the Phone Call State to ACTIVE
            app.cdmaPhoneCallState.setCurrentCallState(
                CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE);
        } else {
            // This is the second outgoing call. Set the Phone Call State to 3WAY
            app.cdmaPhoneCallState.setCurrentCallState(
                CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE);
        }
    }

//MTK begin:
    static void placeCallRegister(Phone phone) {
    	boolean isSipCall = phone.getPhoneType() == Phone.PHONE_TYPE_SIP;
        if (DBG) log("placeCallRegister: ");
        if (FeatureOption.MTK_GEMINI_SUPPORT && !isSipCall) {
            MTKCallManager cmGemini = PhoneApp.getInstance().mCMGemini;    	
            cmGemini.registerForSpeechInfoGemini(mConnectionHandler, PHONE_SPEECH_INFO, cmGemini, Phone.GEMINI_SIM_1);
            cmGemini.registerForSpeechInfoGemini(mConnectionHandler, PHONE_SPEECH_INFO2, cmGemini, Phone.GEMINI_SIM_2);
        } else {
            CallManager cm = PhoneApp.getInstance().mCM;		    
            cm.registerForSpeechInfo(mConnectionHandler, PHONE_SPEECH_INFO, cm);
        }		
    }
//MTK end

    /**
     * Dial the number using the phone passed in.
     *
     * If the connection is establised, this method issues a sync call
     * that may block to query the caller info.
     * TODO: Change the logic to use the async query.
     *
     * @param context To perform the CallerInfo query.
     * @param phone the Phone object.
     * @param number to be dialed as requested by the user. This is
     * NOT the phone number to connect to. It is used only to build the
     * call card and to update the call log. See above for restrictions.
     * @param contactRef that triggered the call. Typically a 'tel:'
     * uri but can also be a 'content://contacts' one.
     * @param isEmergencyCall indicates that whether or not this is an
     * emergency call
     * @param gatewayUri Is the address used to setup the connection, null
     * if not using a gateway
     *
     * @return either Constants.CALL_STATUS_DIALED or Constants.CALL_STATUS_FAILED
     */
    public static int placeCallGemini(Context context, Phone phone,
            String number, Uri contactRef, boolean isEmergencyCall,
            Uri gatewayUri, int simId) {
        Profiler.trace(Profiler.PhoneUtilsEnterPlaceCallGemini);
        if (DBG) log("placeCall '" + number + "' GW:'" + gatewayUri + "'");
        
        if (PhoneApp.getInstance().mCM.getState() == Phone.State.IDLE) {
            PhoneApp.getInstance().notifier.resetBeforeCall();
            setAudioMode();
        }
        
        if (!PhoneApp.getInstance().isVTIdle()) {
        	return Constants.CALL_STATUS_FAILED;
        }
        
        final PhoneApp app = PhoneApp.getInstance();

        boolean useGateway = false;
        if (null != gatewayUri &&
            !isEmergencyCall &&
            PhoneUtils.isRoutableViaGateway(number)) {  // Filter out MMI, OTA and other codes.
            useGateway = true;
        }

        int status = Constants.CALL_STATUS_DIALED;
        Connection connection;
        String numberToDial;
        if (useGateway) {
            // TODO: 'tel' should be a constant defined in framework base
            // somewhere (it is in webkit.)
            if (null == gatewayUri || !Constants.SCHEME_TEL.equals(gatewayUri.getScheme())) {
                Log.e(LOG_TAG, "Unsupported URL:" + gatewayUri);
                return Constants.CALL_STATUS_FAILED;
            }

            // We can use getSchemeSpecificPart because we don't allow #
            // in the gateway numbers (treated a fragment delim.) However
            // if we allow more complex gateway numbers sequence (with
            // passwords or whatnot) that use #, this may break.
            // TODO: Need to support MMI codes.
            numberToDial = gatewayUri.getSchemeSpecificPart();
        } else {
            numberToDial = number;
        }

        try {
// Google code:  connection = app.mCM.dial(phone, numberToDial);
            boolean isSipPhone = phone instanceof SipPhone;
            if (FeatureOption.MTK_GEMINI_SUPPORT && !isSipPhone) {
                if (simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
                    log("placeCallExt , simId " + simId);
                    connection = PhoneApp.getInstance().mCMGemini.dialGemini(phone, numberToDial, simId);
                } else {
                    int defaultSimId = SystemProperties.getInt(Phone.GEMINI_DEFAULT_SIM_PROP, -1);
                    log("placeCallExt , default simId " + simId);
                    connection = PhoneApp.getInstance().mCMGemini.dialGemini(phone, numberToDial, defaultSimId);
                }
            } else {
                connection = PhoneApp.getInstance().mCM.dial(phone, numberToDial);
            }
        } catch (CallStateException ex) {
            // CallStateException means a new outgoing call is not currently
            // possible: either no more call slots exist, or there's another
            // call already in the process of dialing or ringing.
            Log.w(LOG_TAG, "Exception from app.mCM.dial()", ex);
            return Constants.CALL_STATUS_FAILED;

            // Note that it's possible for CallManager.dial() to return
            // null *without* throwing an exception; that indicates that
            // we dialed an MMI (see below).
        }

        //Becasue of support G+C, the GeminiPhone may contains CDMAPhone, so
        //must get the exactly phone type:
        int phoneType = Phone.PHONE_TYPE_GSM;
        if (FeatureOption.MTK_GEMINI_SUPPORT && phone instanceof GeminiPhone) {
            phoneType = ((GeminiPhone)phone).getPhonebyId(simId).getPhoneType();
        } else {
            phoneType = phone.getPhoneType();
        }

        // On GSM phones, null is returned for MMI codes
        if (null == connection) {
            if (phoneType == Phone.PHONE_TYPE_GSM && gatewayUri == null) {
                if (DBG) log("dialed MMI code: " + number);
                status = Constants.CALL_STATUS_DIALED_MMI;
                // Set dialed MMI command to service
                if (mNwService != null) {
                    try {
                        mNwService.setMmiString(number);
                        if (DBG) log("Extended NW bindService setUssdString (" + number + ")");
                    } catch (RemoteException e) {
                        mNwService = null;
                    }
                }
            } else {
                status = Constants.CALL_STATUS_FAILED;
            }
        } else {
            //need GeminiPhone public the api to get the exactly phone
            if (phoneType == Phone.PHONE_TYPE_CDMA /*&& (simId == Phone.GEMINI_SIM_2)*/) {
                updateCdmaCallStateOnNewOutgoingCall(app);
                if (DBG) log("call updateCdmaCallStateOnNewOutgoingCall for cdma phone.");
            }

            // Clean up the number to be displayed.
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                number = CdmaConnection.formatDialString(number);
            }
//MTK add below one line:
			// !!!!! Need to check whether below line code is needed or not
        	PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);

            number = PhoneNumberUtils.extractNetworkPortion(number);
            number = PhoneNumberUtils.convertKeypadLettersToDigits(number);
            number = PhoneNumberUtils.formatNumber(number);

            if (gatewayUri == null) {
                // phone.dial() succeeded: we're now in a normal phone call.
                // attach the URI to the CallerInfo Object if it is there,
                // otherwise just attach the Uri Reference.
                // if the uri does not have a "content" scheme, then we treat
                // it as if it does NOT have a unique reference.
                String content = context.getContentResolver().SCHEME_CONTENT;
                if ((contactRef != null) && (contactRef.getScheme().equals(content))) {
                    Object userDataObject = connection.getUserData();
                    if (userDataObject == null) {
                        connection.setUserData(contactRef);
                    } else {
                        // TODO: This branch is dead code, we have
                        // just created the connection which has
                        // no user data (null) by default.
                        if (userDataObject instanceof CallerInfo) {
                        ((CallerInfo) userDataObject).contactRefUri = contactRef;
                        } else {
                        ((CallerInfoToken) userDataObject).currentInfo.contactRefUri =
                            contactRef;
                        }
                    }
                }
            } else {
                // Get the caller info synchronously because we need the final
                // CallerInfo object to update the dialed number with the one
                // requested by the user (and not the provider's gateway number).
                CallerInfo info = null;
                String content = phone.getContext().getContentResolver().SCHEME_CONTENT;
                if ((contactRef != null) && (contactRef.getScheme().equals(content))) {
                    info = CallerInfo.getCallerInfo(context, contactRef);
                }

                // Fallback, lookup contact using the phone number if the
                // contact's URI scheme was not content:// or if is was but
                // the lookup failed.
                if (null == info) {
//Google code:      info = CallerInfo.getCallerInfo(context, number);
//MTK begin:        
					if (FeatureOption.MTK_GEMINI_SUPPORT) {
				        info = CallerInfo.getCallerInfoGemini(context, number, simId);
				    } else {
				        info = CallerInfo.getCallerInfo(context, number);
				    }
//MTK end
                }
                info.phoneNumber = number;
                connection.setUserData(info);
            }
            //setAudioMode();

            if (DBG) log("about to activate speaker");
            // Check is phone in any dock, and turn on speaker accordingly
            activateSpeakerIfDocked(phone);
        }
        Profiler.trace(Profiler.PhoneUtilsLeavePlaceCallGemini);
        return status;
    }

//MTK begin:
    public static int placeCall(Context context, Phone phone,
                                String number, Uri contactRef, boolean isEmergencyCall,
                                Uri gatewayUri) {
		return placeCallGemini(context, phone, number, contactRef, isEmergencyCall,
                        gatewayUri, -1);
	}

//MTK end
    /**
     * Wrapper function to control when to send an empty Flash command to the network.
     * Mainly needed for CDMA networks, such as scenarios when we need to send a blank flash
     * to the network prior to placing a 3-way call for it to be successful.
     */
    static void sendEmptyFlash(Phone phone) {
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            Call fgCall = phone.getForegroundCall();
            if (fgCall.getState() == Call.State.ACTIVE) {
                // Send the empty flash
                if (DBG) Log.d(LOG_TAG, "onReceive: (CDMA) sending empty flash to network");
                switchHoldingAndActive(phone.getBackgroundCall());
            }
        }
    }

    /**
     * @param heldCall is the background call want to be swapped
     */
    public static void switchHoldingAndActive(Call heldCall) {
        if (DBG) log("switchHoldingAndActive()...");
        try {
            CallManager cm = PhoneApp.getInstance().mCM;
            if (heldCall.isIdle()) {
                // no heldCall, so it is to hold active call
                cm.switchHoldingAndActive(cm.getFgPhone().getBackgroundCall());
            } else {
                // has particular heldCall, so to switch
                cm.switchHoldingAndActive(heldCall);
            }
            setAudioMode(cm);
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "switchHoldingAndActive: caught " + ex, ex);
        }
    }

    /**
     * Restore the mute setting from the earliest connection of the
     * foreground call.
     */
    static Boolean restoreMuteState() {
        Phone phone = PhoneApp.getInstance().mCM.getFgPhone();

        //get the earliest connection
//Google: Connection c = phone.getForegroundCall().getEarliestConnection();

        Connection c;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            c = PhoneApp.getInstance().mCM.getActiveFgCall().getEarliestConnection();
        } else {
            c = phone.getForegroundCall().getEarliestConnection();
        }
        // only do this if connection is not null.
        if (c != null) {

            int phoneType = phone.getPhoneType();

            // retrieve the mute value.
            Boolean shouldMute = null;

            // In CDMA, mute is not maintained per Connection. Single mute apply for
            // a call where  call can have multiple connections such as
            // Three way and Call Waiting.  Therefore retrieving Mute state for
            // latest connection can apply for all connection in that call
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                shouldMute = sConnectionMuteTable.get(
                        phone.getForegroundCall().getLatestConnection());
            } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                    || (phoneType == Phone.PHONE_TYPE_SIP)) {
                shouldMute = sConnectionMuteTable.get(c);
            }
            if (shouldMute == null) {
                if (DBG) log("problem retrieving mute value for this connection.");
                shouldMute = Boolean.FALSE;
            }

            // set the mute value and return the result.
            setMute (shouldMute.booleanValue());
            return shouldMute;
        }
        return Boolean.valueOf(getMute());
    }

    static void mergeCalls() {
        mergeCalls(PhoneApp.getInstance().mCM);
    }

    static void mergeCalls(CallManager cm) {
        int phoneType = cm.getFgPhone().getPhoneType();
        
        DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
        if (DualTalkUtils.isSupportDualTalk) {
            Call call = dt.getActiveFgCall();
            if (call != null) {
                phoneType = call.getPhone().getPhoneType();
            }
        }
        
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (DBG) log("mergeCalls(): CDMA...");
            PhoneApp app = PhoneApp.getInstance();
            if (app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                // Set the Phone Call State to conference
                app.cdmaPhoneCallState.setCurrentCallState(
                        CdmaPhoneCallState.PhoneCallState.CONF_CALL);

                // Send flash cmd
                // TODO: Need to change the call from switchHoldingAndActive to
                // something meaningful as we are not actually trying to swap calls but
                // instead are merging two calls by sending a Flash command.
                if (DBG) log("- sending flash...");
                switchHoldingAndActive(cm.getFirstActiveBgCall());
            } else if (DualTalkUtils.isSupportDualTalk) {
                 //DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
                 Call fg = dt.getActiveFgCall();
                 if (DBG) log("For this case, we don't know how to do exactly, so just switch the cdma call");
                 try {
                     fg.getPhone().switchHoldingAndActive();
                 } catch (Exception e) {
                     log(e.toString());
                 }
            }
        } else {
            try {
                if (DBG) log("mergeCalls(): calling cm.conference()...");
                //DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
                if (DualTalkUtils.isSupportDualTalk && dt.isDualTalkMultipleHoldCase()) {
                    cm.conference(dt.getFirstActiveBgCall());
                } else {
                    cm.conference(cm.getFirstActiveBgCall());
                }
            } catch (CallStateException ex) {
                Log.w(LOG_TAG, "mergeCalls: caught " + ex, ex);
                if (phoneType == Phone.PHONE_TYPE_SIP)
                {
                	InCallScreen incall = PhoneApp.getInstance().getInCallScreenInstance();
                	AsyncResult ar = new AsyncResult(null,  Phone.SuppService.CONFERENCE, null);
                	if (incall != null) incall.onSuppServiceFailed(ar);
                }
            }
        }
    }

    static void separateCall(Connection c) {
        try {
            if (DBG) log("separateCall: " + c.getAddress());
            c.separate();
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "separateCall: caught " + ex, ex);
        }
    }

    /**
     * Handle the MMIInitiate message and put up an alert that lets
     * the user cancel the operation, if applicable.
     *
     * @param context context to get strings.
     * @param mmiCode the MmiCode object being started.
     * @param buttonCallbackMessage message to post when button is clicked.
     * @param previousAlert a previous alert used in this activity.
     * @return the dialog handle
     */
    static Dialog displayMMIInitiate(Context context,
                                          MmiCode mmiCode,
                                          Message buttonCallbackMessage,
                                          Dialog previousAlert) {
        if (DBG) log("displayMMIInitiate: " + mmiCode);
        if (previousAlert != null) {
            previousAlert.dismiss();
        }

        // The UI paradigm we are using now requests that all dialogs have
        // user interaction, and that any other messages to the user should
        // be by way of Toasts.
        //
        // In adhering to this request, all MMI initiating "OK" dialogs
        // (non-cancelable MMIs) that end up being closed when the MMI
        // completes (thereby showing a completion dialog) are being
        // replaced with Toasts.
        //
        // As a side effect, moving to Toasts for the non-cancelable MMIs
        // also means that buttonCallbackMessage (which was tied into "OK")
        // is no longer invokable for these dialogs.  This is not a problem
        // since the only callback messages we supported were for cancelable
        // MMIs anyway.
        //
        // A cancelable MMI is really just a USSD request. The term
        // "cancelable" here means that we can cancel the request when the
        // system prompts us for a response, NOT while the network is
        // processing the MMI request.  Any request to cancel a USSD while
        // the network is NOT ready for a response may be ignored.
        //
        // With this in mind, we replace the cancelable alert dialog with
        // a progress dialog, displayed until we receive a request from
        // the the network.  For more information, please see the comments
        // in the displayMMIComplete() method below.
        //
        // Anything that is NOT a USSD request is a normal MMI request,
        // which will bring up a toast (desribed above).
        // Optional code for Extended USSD running prompt
        if (mNwService != null) {
            if (DBG) log("running USSD code, displaying indeterminate progress.");
            // create the indeterminate progress dialog and display it.
            ProgressDialog pd = new ProgressDialog(context);
            CharSequence textmsg = "";
            try {
                textmsg = mNwService.getMmiRunningText();

            } catch (RemoteException e) {
                mNwService = null;
                textmsg = context.getText(R.string.ussdRunning);
            }
            if (DBG) log("Extended NW displayMMIInitiate (" + textmsg+ ")");
            pd.setMessage(textmsg);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            pd.show();
            // trigger a 15 seconds timeout to clear this progress dialog
            mMmiTimeoutCbMsg = buttonCallbackMessage;
            try {
                mMmiTimeoutCbMsg.getTarget().sendMessageDelayed(buttonCallbackMessage, 15000);
            } catch(NullPointerException e) {
                mMmiTimeoutCbMsg = null;
            }
            return pd;
        }

        boolean isCancelable = (mmiCode != null) && mmiCode.isCancelable();

        if (!isCancelable) {
            if (DBG) log("not a USSD code, displaying status toast.");
            CharSequence text = context.getText(R.string.mmiStarted);
            Toast.makeText(context, text, Toast.LENGTH_SHORT)
                .show();
            return null;
        } else {
            if (DBG) log("running USSD code, displaying indeterminate progress.");

            // create the indeterminate progress dialog and display it.
            ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage(context.getText(R.string.ussdRunning));
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            pd.show();

            return pd;
        }

    }

    /**
     * Handle the MMIComplete message and fire off an intent to display
     * the message.
     *
     * @param context context to get strings.
     * @param mmiCode MMI result.
     * @param previousAlert a previous alert used in this activity.
     */
//MTK begin:
    static void displayMMIComplete(final Phone phone, Context context, final MmiCode mmiCode,
            Message dismissCallbackMessage,
            AlertDialog previousAlert) {
            displayMMICompleteExt(phone, context, mmiCode, dismissCallbackMessage, previousAlert, Phone.GEMINI_SIM_1);
    }
	
    static void displayMMICompleteExt(final Phone phone, Context context, final MmiCode mmiCode,
            Message dismissCallbackMessage, AlertDialog previousAlert, final int simId) {
//MTK end
        final PhoneApp app = PhoneApp.getInstance();
        CharSequence text;
        int title = 0;  // title for the progress dialog, if needed.
        MmiCode.State state = mmiCode.getState();

        if (DBG) log("displayMMIComplete: state=" + state);
        // Clear timeout trigger message
        if(mMmiTimeoutCbMsg != null) {
            try{
                mMmiTimeoutCbMsg.getTarget().removeMessages(mMmiTimeoutCbMsg.what);
                if (DBG) log("Extended NW displayMMIComplete removeMsg");
            } catch (NullPointerException e) {
            }
            mMmiTimeoutCbMsg = null;
        }

        curCode = mmiCode;

        switch (state) {
            case PENDING:
                // USSD code asking for feedback from user.
                text = mmiCode.getMessage();
                if (DBG) log("- using text from PENDING MMI message: '" + text + "'");
                break;
            case CANCELLED:
                text = context.getText(R.string.mmiCancelled);
                if(isOrangeSupport()){
                    return;
                }
                break;
            case COMPLETE:
				mbMmiFinished = true;
                if (app.getPUKEntryActivity() != null) {
                    // if an attempt to unPUK the device was made, we specify
                    // the title and the message here.
                    title = com.android.internal.R.string.PinMmi;
                    text = context.getText(R.string.puk_unlocked);
                    break;
                }
                // All other conditions for the COMPLETE mmi state will cause
                // the case to fall through to message logic in common with
                // the FAILED case.

            case FAILED:
                text = mmiCode.getMessage();
                if (DBG) log("- using text from MMI message: '" + text + "'");
//MTK begin:
//                if (mDialog != null) {
//                    DismissMMIDialog();
//                    return ;
//                }
                
                if (mUssdActivity != null) {
                    mUssdActivity.dismiss();
                }
//MTK end
                break;
            default:
                throw new IllegalStateException("Unexpected MmiCode state: " + state);
        }

        if (previousAlert != null) {
            previousAlert.dismiss();
        }

        /**
         * Delete by Mediatek Begin. 
         * This dialog is not useful but also cause some problem when Sim me lock exists after
         * PUK locked is unlocked by **05*... way, so delete them
         */
        // Check to see if a UI exists for the PUK activation.  If it does
        // exist, then it indicates that we're trying to unblock the PUK.
        /*
        if ((app.getPUKEntryActivity() != null) && (state == MmiCode.State.COMPLETE)) {
            if (DBG) log("displaying PUK unblocking progress dialog.");

            // create the progress dialog, make sure the flags and type are
            // set correctly.
            ProgressDialog pd = new ProgressDialog(app);
            pd.setTitle(title);
            pd.setMessage(text);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // display the dialog
            pd.show();

            // indicate to the Phone app that the progress dialog has
            // been assigned for the PUK unlock / SIM READY process.
            app.setPukEntryProgressDialog(pd);

        } else {
        */
        /**
         * Delete by Mediatek End.
         */
        if ((app.getPUKEntryActivity() == null) || (state != MmiCode.State.COMPLETE)) {
            // In case of failure to unlock, we'll need to reset the
            // PUK unlock activity, so that the user may try again.
            if (app.getPUKEntryActivity() != null) {
                app.setPukEntryActivity(null);
            }

            // A USSD in a pending state means that it is still
            // interacting with the user.
            if (state != MmiCode.State.PENDING) {
                if (DBG) log("MMI code has finished running.");

                // Replace response message with Extended Mmi wording
                if (mNwService != null) {
                    try {
                        text = mNwService.getUserMessage(text);
                    } catch (RemoteException e) {
                        mNwService = null;
                    }
                    if (DBG) log("Extended NW displayMMIInitiate (" + text + ")");
                    if (text == null || text.length() == 0)
                        return;
                }

/*Google code:  // displaying system alert dialog on the screen instead of
                // using another activity to display the message.  This
                // places the message at the forefront of the UI.
                AlertDialog newDialog = new AlertDialog.Builder(context)
                        .setMessage(text)
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true)
                        .create();

                newDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                newDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                newDialog.show();
*/
//MTK begin:
                // inflate the layout with the scrolling text area for the dialog.
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_ussd_response, null);
                TextView msg = (TextView) dialogView.findViewById(R.id.msg);
                msg.setText(text);    
                TextView ussdUpdateView = (TextView) dialogView.findViewById(R.id.ussd_update);
                ussdUpdateView.setVisibility(View.GONE);
                EditText inputText = (EditText) dialogView.findViewById(R.id.input_field);
                inputText.setVisibility(View.GONE);
                /*
                 * auto update the UI,because some App change the Phone mode that will cause 
                 * some USSD response information lost.
                 * For example ,when Camera first init.
                 */
                autoUpdateUssdReponseUi(dialogView);
                // displaying system alert dialog on the screen instead of
                // using another activity to display the message.  This
                // places the message at the forefront of the UI.
//                AlertDialog newDialog = new AlertDialog.Builder(context)
//                        .setView(dialogView)
//                        .setPositiveButton(R.string.ok, null)
//                        .setCancelable(true)
//                        .create();
//
//                newDialog.getWindow().setType(
//                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
//                newDialog.getWindow().addFlags(
//                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                //newDialog.show();

                displayMmiDialog(context, text, UssdAlertActivity.USSD_DIALOG_NOTIFICATION, simId);
                
//MTk end
            } else {
                if (DBG) log("USSD code has requested user input. Constructing input dialog.");

                // USSD MMI code that is interacting with the user.  The
                // basic set of steps is this:
                //   1. User enters a USSD request
                //   2. We recognize the request and displayMMIInitiate
                //      (above) creates a progress dialog.
                //   3. Request returns and we get a PENDING or COMPLETE
                //      message.
                //   4. These MMI messages are caught in the PhoneApp
                //      (onMMIComplete) and the InCallScreen
                //      (mHandler.handleMessage) which bring up this dialog
                //      and closes the original progress dialog,
                //      respectively.
                //   5. If the message is anything other than PENDING,
                //      we are done, and the alert dialog (directly above)
                //      displays the outcome.
                //   6. If the network is requesting more information from
                //      the user, the MMI will be in a PENDING state, and
                //      we display this dialog with the message.
                //   7. User input, or cancel requests result in a return
                //      to step 1.  Keep in mind that this is the only
                //      time that a USSD should be canceled.

                // inflate the layout with the scrolling text area for the dialog.
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_ussd_response, null);
//MTK begin:
                TextView msg = (TextView) dialogView.findViewById(R.id.msg);
                msg.setText(text);
                msg.setWidth(MIN_WIDTH);
//MTK end
                // get the input field.
                final EditText inputText = (EditText) dialogView.findViewById(R.id.input_field);
                //Disable the long click, because we haven't the window context, see ALPS00241709 for details.
                inputText.setLongClickable(false);
//MTK add below line:
                inputText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(182)});
                // specify the dialog's click listener, with SEND and CANCEL logic.
                final DialogInterface.OnClickListener mUSSDDialogListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            switch (whichButton) {
                                case DialogInterface.BUTTON_POSITIVE:
//Google code:                       phone.sendUssdResponse(inputText.getText().toString());
//MTK begin:
                                     if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                         Log.w(LOG_TAG, "displayMMIcomplete sendUssdResponseGemini button, simId:"+simId); 
                                         Log.w(LOG_TAG, "displayMMIcomplete USSR string :"+inputText.getText().toString()); 										
                                         ((GeminiPhone)phone).sendUssdResponseGemini(inputText.getText().toString(), simId);
                                     } else {
                                         phone.sendUssdResponse(inputText.getText().toString());
                                     }									
                                     mDialog = null;
                                     mUssdActivity = null;
//MTK end
                                     break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    if (mmiCode.isCancelable()) {
                                        mmiCode.cancel();
                                    }
//MTK add below line:
                                    mDialog = null;
                                    mUssdActivity = null;
                                    break;
                            }
                        }
                    };
///MTK begin:
             final DialogInterface.OnCancelListener mUSSDDialogCancelListener =
                        new DialogInterface.OnCancelListener() {
                            
                            public void onCancel(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                if (mmiCode.isCancelable()) {
                                    mmiCode.cancel();
                                }
                            }
                        };
///MTK end
                // build the dialog
                final AlertDialog newDialog = new AlertDialog.Builder(context)
                        /*.setMessage(text)*/
                        .setView(dialogView)
                        .setPositiveButton(R.string.send_button, /*mUSSDDialogListener*/null)
                        .setNegativeButton(R.string.cancel, /*mUSSDDialogListener*/null)
                        .setCancelable(false)
                        .create();
//MTK begin:
                newDialog.setOnCancelListener(mUSSDDialogCancelListener);
                //mDialog = newDialog;
//MTK end
                // attach the key listener to the dialog's input field and make
                // sure focus is set.
                final View.OnKeyListener mUSSDDialogInputListener =
                    new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_CALL:
//MTK add below one line:
                                    return true;
                                case KeyEvent.KEYCODE_ENTER:
								// !!!!! Need to check KeyEvent.ACTION_DOWN or UP
/*Google code begin:                          if(event.getAction() == KeyEvent.ACTION_DOWN) {
                                        phone.sendUssdResponse(inputText.getText().toString());
                                        newDialog.dismiss();
                                    }
                                    return true;
Google code end*/
//MTK begin:
                                    if(event.getAction() == event.ACTION_UP) {
                                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                            Log.w(LOG_TAG, "sdisplayMMIcomplete sendUssdResponseGemini key");
                                            if (inputText.getText().toString().length() == 0) {
                                                return false;
                                            }
                                            ((GeminiPhone)phone).sendUssdResponseGemini(inputText.getText().toString(), simId);
                                        } else {
                                            phone.sendUssdResponse(inputText.getText().toString());
                                        }						 
                                        newDialog.dismiss();
                                        return true;
                                    } else {
                                        // do not process "enter" key when keydown, avoid "enter" key in inputText
                                        return true;
                                    }
//MTK end
                            }
                            return false;
                        }
                    };
                inputText.setOnKeyListener(mUSSDDialogInputListener);
                inputText.requestFocus();

                // set the window properties of the dialog
                newDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                newDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                // now show the dialog!
                //newDialog.show();
                displayMmiDialog(context, text, UssdAlertActivity.USSD_DIALOG_REQUEST, simId);
                
//MTK begin:
//                if (newDialog != null) {
//                    Button bt = newDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                    bt.setEnabled(false);
//                }
//                inputText.addTextChangedListener(new TextWatcher(){
//                    public void beforeTextChanged(CharSequence s, int start,
//                            int count, int after) {
//                    }
//                    
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    }
//                    
//                    public void afterTextChanged(Editable s) {
//                        int count = s == null ? 0 : s.length();
//                        if (count > 0) {
//                            newDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//                        } else {
//                            newDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//                        }
//                    }
//                });
//MTK end
            }
        }
    }

    /**
     * Cancels the current pending MMI operation, if applicable.
     * @return true if we canceled an MMI operation, or false
     *         if the current pending MMI wasn't cancelable
     *         or if there was no current pending MMI at all.
     *
     * @see displayMMIInitiate
     */
/* Google static boolean cancelMmiCode(Phone phone) {
        List<? extends MmiCode> pendingMmis = phone.getPendingMmiCodes(); */
//MTK begin:
    static boolean cancelMmiCodeExt(Phone phone, int simId) {
        log("cancelMmiCode....");
        List<? extends MmiCode> pendingMmis=null;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            pendingMmis = ((GeminiPhone)phone).getPendingMmiCodesGemini(simId);
        } else {
            pendingMmis = phone.getPendingMmiCodes();
        }
//MTk end
        int count = pendingMmis.size();
        if (DBG) log("cancelMmiCode: num pending MMIs = " + count);

        boolean canceled = false;
        if (count > 0) {
            // assume that we only have one pending MMI operation active at a time.
            // I don't think it's possible to enter multiple MMI codes concurrently
            // in the phone UI, because during the MMI operation, an Alert panel
            // is displayed, which prevents more MMI code from being entered.
            MmiCode mmiCode = pendingMmis.get(0);
            if (mmiCode.isCancelable()) {
                mmiCode.cancel();
                canceled = true;
            }
        }

        //clear timeout message and pre-set MMI command
        if (mNwService != null) {
            try {
                mNwService.clearMmiString();
            } catch (RemoteException e) {
                mNwService = null;
            }
        }
        if (mMmiTimeoutCbMsg != null) {
            mMmiTimeoutCbMsg = null;
        }
        return canceled;
    }

    public static class VoiceMailNumberMissingException extends Exception {
        VoiceMailNumberMissingException() {
            super();
        }

        VoiceMailNumberMissingException(String msg) {
            super(msg);
        }
    }

    /**
     * Gets the phone number to be called from an intent.  Requires a Context
     * to access the contacts database, and a Phone to access the voicemail
     * number.
     *
     * <p>If <code>phone</code> is <code>null</code>, the function will return
     * <code>null</code> for <code>voicemail:</code> URIs;
     * if <code>context</code> is <code>null</code>, the function will return
     * <code>null</code> for person/phone URIs.</p>
     *
     * <p>If the intent contains a <code>sip:</code> URI, the returned
     * "number" is actually the SIP address.
     *
     * @param context a context to use (or
     * @param intent the intent
     *
     * @throws VoiceMailNumberMissingException if <code>intent</code> contains
     *         a <code>voicemail:</code> URI, but <code>phone</code> does not
     *         have a voicemail number set.
     *
     * @return the phone number (or SIP address) that would be called by the intent,
     *         or <code>null</code> if the number cannot be found.
     */
    static String getNumberFromIntent(Context context, Intent intent)
            throws VoiceMailNumberMissingException {
        Uri uri = intent.getData();
        String scheme = uri.getScheme();

        // The sip: scheme is simple: just treat the rest of the URI as a
        // SIP address.
        if (Constants.SCHEME_SIP.equals(scheme)) {
            return uri.getSchemeSpecificPart();
        }

        // Otherwise, let PhoneNumberUtils.getNumberFromIntent() handle
        // the other cases (i.e. tel: and voicemail: and contact: URIs.)

        final String number = PhoneNumberUtils.getNumberFromIntent(intent, context);

        // Check for a voicemail-dialing request.  If the voicemail number is
        // empty, throw a VoiceMailNumberMissingException.
        if (Constants.SCHEME_VOICEMAIL.equals(scheme) &&
                (number == null || TextUtils.isEmpty(number)))
            throw new VoiceMailNumberMissingException();

        return number;
    }

    /**
     * Returns the caller-id info corresponding to the specified Connection.
     * (This is just a simple wrapper around CallerInfo.getCallerInfo(): we
     * extract a phone number from the specified Connection, and feed that
     * number into CallerInfo.getCallerInfo().)
     *
     * The returned CallerInfo may be null in certain error cases, like if the
     * specified Connection was null, or if we weren't able to get a valid
     * phone number from the Connection.
     *
     * Finally, if the getCallerInfo() call did succeed, we save the resulting
     * CallerInfo object in the "userData" field of the Connection.
     *
     * NOTE: This API should be avoided, with preference given to the
     * asynchronous startGetCallerInfo API.
     */
    static CallerInfo getCallerInfo(Context context, Connection c) {
        CallerInfo info = null;

        if (c != null) {
            //See if there is a URI attached.  If there is, this means
            //that there is no CallerInfo queried yet, so we'll need to
            //replace the URI with a full CallerInfo object.
            Object userDataObject = c.getUserData();
            if (userDataObject instanceof Uri) {
                info = CallerInfo.getCallerInfo(context, (Uri) userDataObject);
                if (info != null) {
                    c.setUserData(info);
                }
            } else {
                if (userDataObject instanceof CallerInfoToken) {
                    //temporary result, while query is running
                    info = ((CallerInfoToken) userDataObject).currentInfo;
                } else {
                    //final query result
                    info = (CallerInfo) userDataObject;
                }
                if (info == null) {
                    // No URI, or Existing CallerInfo, so we'll have to make do with
                    // querying a new CallerInfo using the connection's phone number.
                    String number = c.getAddress();

                    if (DBG) log("getCallerInfo: number = " + number);

                    if (!TextUtils.isEmpty(number)) {
//Google code:          info = CallerInfo.getCallerInfo(context, number);
//MTK begin:
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {            
                            PhoneApp app = PhoneApp.getInstance();
                            int simId = Phone.GEMINI_SIM_1;
                            if (((GeminiPhone)app.phone).getStateGemini(Phone.GEMINI_SIM_2) != Phone.State.IDLE) {
                                simId = Phone.GEMINI_SIM_2;
                            }
                            info = CallerInfo.getCallerInfoGemini(context, number, simId);
                        } else {
                            info = CallerInfo.getCallerInfo(context, number);
                        }
//MTk end
                        if (info != null) {
                            c.setUserData(info);
                        }
                    }
                }
            }
        }
        return info;
    }

    /**
     * Class returned by the startGetCallerInfo call to package a temporary
     * CallerInfo Object, to be superceded by the CallerInfo Object passed
     * into the listener when the query with token mAsyncQueryToken is complete.
     */
    public static class CallerInfoToken {
        /**indicates that there will no longer be updates to this request.*/
        public boolean isFinal;

        public CallerInfo currentInfo;
        public CallerInfoAsyncQuery asyncQuery;
    }

    /**
     * Start a CallerInfo Query based on the earliest connection in the call.
     */
    static CallerInfoToken startGetCallerInfo(Context context, Call call,
            CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie) {
        Connection conn = null;
        int phoneType = call.getPhone().getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                || (phoneType == Phone.PHONE_TYPE_SIP)) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }

        return startGetCallerInfo(context, conn, listener, cookie);
    }

    /**
     * Start a CallerInfo Query based on the earliest connection in the call.
     */
    static CallerInfoToken startGetCallerInfo(Context context, Call call,
            CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie, boolean needClearUserData) {
        Connection conn = null;
        int phoneType = call.getPhone().getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                || (phoneType == Phone.PHONE_TYPE_SIP)) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }

        if (null != conn && needClearUserData) {
            conn.clearUserData();
        }

        return startGetCallerInfo(context, conn, listener, cookie);
    }

    /**
     * place a temporary callerinfo object in the hands of the caller and notify
     * caller when the actual query is done.
     */
    static CallerInfoToken startGetCallerInfo(Context context, Connection c,
            CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie) {
        CallerInfoToken cit;

        boolean isSipConn = false;
        if (c == null) {
            //TODO: perhaps throw an exception here.
            cit = new CallerInfoToken();
            cit.asyncQuery = null;
            return cit;
        }
        
        isSipConn = c.getCall().getPhone().getPhoneType() == Phone.PHONE_TYPE_SIP;

        Object userDataObject = c.getUserData();

        // There are now 3 states for the Connection's userData object:
        //
        //   (1) Uri - query has not been executed yet
        //
        //   (2) CallerInfoToken - query is executing, but has not completed.
        //
        //   (3) CallerInfo - query has executed.
        //
        // In each case we have slightly different behaviour:
        //   1. If the query has not been executed yet (Uri or null), we start
        //      query execution asynchronously, and note it by attaching a
        //      CallerInfoToken as the userData.
        //   2. If the query is executing (CallerInfoToken), we've essentially
        //      reached a state where we've received multiple requests for the
        //      same callerInfo.  That means that once the query is complete,
        //      we'll need to execute the additional listener requested.
        //   3. If the query has already been executed (CallerInfo), we just
        //      return the CallerInfo object as expected.
        //   4. Regarding isFinal - there are cases where the CallerInfo object
        //      will not be attached, like when the number is empty (caller id
        //      blocking).  This flag is used to indicate that the
        //      CallerInfoToken object is going to be permanent since no
        //      query results will be returned.  In the case where a query
        //      has been completed, this flag is used to indicate to the caller
        //      that the data will not be updated since it is valid.
        //
        //      Note: For the case where a number is NOT retrievable, we leave
        //      the CallerInfo as null in the CallerInfoToken.  This is
        //      something of a departure from the original code, since the old
        //      code manufactured a CallerInfo object regardless of the query
        //      outcome.  From now on, we will append an empty CallerInfo
        //      object, to mirror previous behaviour, and to avoid Null Pointer
        //      Exceptions.
//MTK begin:
        int simId = Phone.GEMINI_SIM_1;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            PhoneApp app = PhoneApp.getInstance();
            if (((GeminiPhone)app.phone).getStateGemini(Phone.GEMINI_SIM_2) != Phone.State.IDLE) {
                simId = Phone.GEMINI_SIM_2;
            }
        }
//MTK end
        if (userDataObject instanceof Uri) {
            // State (1): query has not been executed yet

            //create a dummy callerinfo, populate with what we know from URI.
            cit = new CallerInfoToken();
            cit.currentInfo = new CallerInfo();
            cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                    (Uri) userDataObject, sCallerInfoQueryListener, c);
            cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
            cit.isFinal = false;

            c.setUserData(cit);

            if (DBG) log("startGetCallerInfo: query based on Uri: " + userDataObject);

        } else if (userDataObject == null) {
            // No URI, or Existing CallerInfo, so we'll have to make do with
            // querying a new CallerInfo using the connection's phone number.
            String number = c.getAddress();

            if (DBG) {
                log("PhoneUtils.startGetCallerInfo: new query for phone number...");
                log("- number (address): " + number);
                log("- c: " + c);
                log("- phone: " + c.getCall().getPhone());
                int phoneType = c.getCall().getPhone().getPhoneType();
                log("- phoneType: " + phoneType);
                switch (phoneType) {
                    case Phone.PHONE_TYPE_NONE: log("  ==> PHONE_TYPE_NONE"); break;
                    case Phone.PHONE_TYPE_GSM: log("  ==> PHONE_TYPE_GSM"); break;
                    case Phone.PHONE_TYPE_CDMA: log("  ==> PHONE_TYPE_CDMA"); break;
                    case Phone.PHONE_TYPE_SIP: log("  ==> PHONE_TYPE_SIP"); break;
                    default: log("  ==> Unknown phone type"); break;
                }
            }

            cit = new CallerInfoToken();
            cit.currentInfo = new CallerInfo();

            // Store CNAP information retrieved from the Connection (we want to do this
            // here regardless of whether the number is empty or not).
            cit.currentInfo.cnapName =  c.getCnapName();
            cit.currentInfo.name = cit.currentInfo.cnapName; // This can still get overwritten
                                                             // by ContactInfo later
            cit.currentInfo.numberPresentation = c.getNumberPresentation();
            cit.currentInfo.namePresentation = c.getCnapNamePresentation();

            if (DBG) {
                log("startGetCallerInfo: number = " + number);
                log("startGetCallerInfo: CNAP Info from FW(1): name="
                    + cit.currentInfo.cnapName
                    + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
            }

            // handling case where number is null (caller id hidden) as well.
            if (!TextUtils.isEmpty(number)) {
                // Check for special CNAP cases and modify the CallerInfo accordingly
                // to be sure we keep the right information to display/log later
                number = modifyForSpecialCnapCases(context, cit.currentInfo, number,
                        cit.currentInfo.numberPresentation);

                cit.currentInfo.phoneNumber = number;
                // For scenarios where we may receive a valid number from the network but a
                // restricted/unavailable presentation, we do not want to perform a contact query
                // (see note on isFinal above). So we set isFinal to true here as well.
                if (cit.currentInfo.numberPresentation != Connection.PRESENTATION_ALLOWED) {
                    cit.isFinal = true;
                } else {
                    if (DBG) log("==> Actually starting CallerInfoAsyncQuery.startQuery()...");
/*Google code:      cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                            number, sCallerInfoQueryListener, c); */
//MTK begin:
                if (FeatureOption.MTK_GEMINI_SUPPORT && !isSipConn) {
                    cit.asyncQuery = CallerInfoAsyncQuery.startQueryGemini(QUERY_TOKEN, context,
                            number, sCallerInfoQueryListener, c, simId);
                } else {
                    cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                            number, sCallerInfoQueryListener, c);
                }
//MTK end
                    cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
                    cit.isFinal = false;
                }
            } else {
                // This is the case where we are querying on a number that
                // is null or empty, like a caller whose caller id is
                // blocked or empty (CLIR).  The previous behaviour was to
                // throw a null CallerInfo object back to the user, but
                // this departure is somewhat cleaner.
                if (DBG) log("startGetCallerInfo: No query to start, send trivial reply.");
                cit.isFinal = true; // please see note on isFinal, above.
            }

            c.setUserData(cit);

            if (DBG) log("startGetCallerInfo: query based on number: " + number);

        } else if (userDataObject instanceof CallerInfoToken) {
            // State (2): query is executing, but has not completed.

            // just tack on this listener to the queue.
            cit = (CallerInfoToken) userDataObject;

            // handling case where number is null (caller id hidden) as well.
            if (cit.asyncQuery != null) {
                cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);

                if (DBG) log("startGetCallerInfo: query already running, adding listener: " +
                        listener.getClass().toString());
            } else {
                // handling case where number/name gets updated later on by the network
                String updatedNumber = c.getAddress();
                if (DBG) log("startGetCallerInfo: updatedNumber initially = " + updatedNumber);
                if (!TextUtils.isEmpty(updatedNumber)) {
                    // Store CNAP information retrieved from the Connection
                    cit.currentInfo.cnapName =  c.getCnapName();
                    // This can still get overwritten by ContactInfo
                    cit.currentInfo.name = cit.currentInfo.cnapName;
                    cit.currentInfo.numberPresentation = c.getNumberPresentation();
                    cit.currentInfo.namePresentation = c.getCnapNamePresentation();

                    updatedNumber = modifyForSpecialCnapCases(context, cit.currentInfo,
                            updatedNumber, cit.currentInfo.numberPresentation);

                    cit.currentInfo.phoneNumber = updatedNumber;
                    if (DBG) log("startGetCallerInfo: updatedNumber=" + updatedNumber);
                    if (DBG) log("startGetCallerInfo: CNAP Info from FW(2): name="
                            + cit.currentInfo.cnapName
                            + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
                    // For scenarios where we may receive a valid number from the network but a
                    // restricted/unavailable presentation, we do not want to perform a contact query
                    // (see note on isFinal above). So we set isFinal to true here as well.
                    if (cit.currentInfo.numberPresentation != Connection.PRESENTATION_ALLOWED) {
                        cit.isFinal = true;
                    } else {
/*Google code:          cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                                updatedNumber, sCallerInfoQueryListener, c); */
//MTK begin:
                        if (FeatureOption.MTK_GEMINI_SUPPORT && !isSipConn) {
                            cit.asyncQuery = CallerInfoAsyncQuery.startQueryGemini(QUERY_TOKEN, context,
                                    updatedNumber, sCallerInfoQueryListener, c, simId);

                        } else {
                            cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                                    updatedNumber, sCallerInfoQueryListener, c);
                        }
//MTK end
                        cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
                        cit.isFinal = false;
                    }
                } else {
                    if (DBG) log("startGetCallerInfo: No query to attach to, send trivial reply.");
                    if (cit.currentInfo == null) {
                        cit.currentInfo = new CallerInfo();
                    }
                    // Store CNAP information retrieved from the Connection
                    cit.currentInfo.cnapName = c.getCnapName();  // This can still get
                                                                 // overwritten by ContactInfo
                    cit.currentInfo.name = cit.currentInfo.cnapName;
                    cit.currentInfo.numberPresentation = c.getNumberPresentation();
                    cit.currentInfo.namePresentation = c.getCnapNamePresentation();

                    if (DBG) log("startGetCallerInfo: CNAP Info from FW(3): name="
                            + cit.currentInfo.cnapName
                            + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
                    cit.isFinal = true; // please see note on isFinal, above.
                }
            }
        } else {
            // State (3): query is complete.

            // The connection's userDataObject is a full-fledged
            // CallerInfo instance.  Wrap it in a CallerInfoToken and
            // return it to the user.

            cit = new CallerInfoToken();
            cit.currentInfo = (CallerInfo) userDataObject;
            cit.asyncQuery = null;
            cit.isFinal = true;
            // since the query is already done, call the listener.
            if (DBG) log("startGetCallerInfo: query already done, returning CallerInfo");
            if (DBG) log("==> cit.currentInfo = " + cit.currentInfo);
        }
        return cit;
    }

    /**
     * Static CallerInfoAsyncQuery.OnQueryCompleteListener instance that
     * we use with all our CallerInfoAsyncQuery.startQuery() requests.
     */
    private static final int QUERY_TOKEN = -1;
    static CallerInfoAsyncQuery.OnQueryCompleteListener sCallerInfoQueryListener =
        new CallerInfoAsyncQuery.OnQueryCompleteListener () {
            /**
             * When the query completes, we stash the resulting CallerInfo
             * object away in the Connection's "userData" (where it will
             * later be retrieved by the in-call UI.)
             */
            public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
                if (DBG) log("query complete, updating connection.userdata");
                Connection conn = (Connection) cookie;
                boolean isSipConn = conn.getCall().getPhone().getPhoneType() == Phone.PHONE_TYPE_SIP;
                // Added a check if CallerInfo is coming from ContactInfo or from Connection.
                // If no ContactInfo, then we want to use CNAP information coming from network
                if (DBG) log("- onQueryComplete: CallerInfo:" + ci);
                if (ci.contactExists || ci.isEmergencyNumber() || ci.isVoiceMailNumber()) {
                    // If the number presentation has not been set by
                    // the ContactInfo, use the one from the
                    // connection.

                    // TODO: Need a new util method to merge the info
                    // from the Connection in a CallerInfo object.
                    // Here 'ci' is a new CallerInfo instance read
                    // from the DB. It has lost all the connection
                    // info preset before the query (see PhoneUtils
                    // line 1334). We should have a method to merge
                    // back into this new instance the info from the
                    // connection object not set by the DB. If the
                    // Connection already has a CallerInfo instance in
                    // userData, then we could use this instance to
                    // fill 'ci' in. The same routine could be used in
                    // PhoneUtils.
                    if (0 == ci.numberPresentation) {
                        ci.numberPresentation = conn.getNumberPresentation();
                    }
                } else {
                    // No matching contact was found for this number.
                    // Return a new CallerInfo based solely on the CNAP
                    // information from the network.

                    CallerInfo newCi = getCallerInfo(null, conn);

                    // ...but copy over the (few) things we care about
                    // from the original CallerInfo object:
                    if (newCi != null) {
                        newCi.phoneNumber = ci.phoneNumber; // To get formatted phone number
                        newCi.geoDescription = ci.geoDescription; // To get geo description string
                        ci = newCi;
                    }
                }

                //We don't show the voice mail when the voice mail is set by cell network
                if (isSipConn && !ci.contactExists && !ci.isEmergencyNumber() && ci.isVoiceMailNumber()) {
                    ci.phoneNumber = conn.getAddress();
                }

                if (DBG) log("==> Stashing CallerInfo " + ci + " into the connection...");
                conn.setUserData(ci);
            }
        };


    /**
     * Returns a single "name" for the specified given a CallerInfo object.
     * If the name is null, return defaultString as the default value, usually
     * context.getString(R.string.unknown).
     */
    static String getCompactNameFromCallerInfo(CallerInfo ci, Context context) {
        if (DBG) log("getCompactNameFromCallerInfo: info = " + ci);

        String compactName = null;
        if (ci != null) {
            if (TextUtils.isEmpty(ci.name)) {
                // Perform any modifications for special CNAP cases to
                // the phone number being displayed, if applicable.
                compactName = modifyForSpecialCnapCases(context, ci, ci.phoneNumber,
                                                        ci.numberPresentation);
            } else {
                // Don't call modifyForSpecialCnapCases on regular name. See b/2160795.
                compactName = ci.name;
            }
        }

        if ((compactName == null) || (TextUtils.isEmpty(compactName))) {
            // If we're still null/empty here, then check if we have a presentation
            // string that takes precedence that we could return, otherwise display
            // "unknown" string.
            if (ci != null && ci.numberPresentation == Connection.PRESENTATION_RESTRICTED) {
                compactName = context.getString(R.string.private_num);
            } else if (ci != null && ci.numberPresentation == Connection.PRESENTATION_PAYPHONE) {
                compactName = context.getString(R.string.payphone);
            } else {
                compactName = context.getString(R.string.unknown);
            }
        }
        if (DBG) log("getCompactNameFromCallerInfo: compactName=" + compactName);
        return compactName;
    }

    /**
     * Returns true if the specified Call is a "conference call", meaning
     * that it owns more than one Connection object.  This information is
     * used to trigger certain UI changes that appear when a conference
     * call is active (like displaying the label "Conference call", and
     * enabling the "Manage conference" UI.)
     *
     * Watch out: This method simply checks the number of Connections,
     * *not* their states.  So if a Call has (for example) one ACTIVE
     * connection and one DISCONNECTED connection, this method will return
     * true (which is unintuitive, since the Call isn't *really* a
     * conference call any more.)
     *
     * @return true if the specified call has more than one connection (in any state.)
     */
    static boolean isConferenceCall(Call call) {
        // CDMA phones don't have the same concept of "conference call" as
        // GSM phones do; there's no special "conference call" state of
        // the UI or a "manage conference" function.  (Instead, when
        // you're in a 3-way call, all we can do is display the "generic"
        // state of the UI.)  So as far as the in-call UI is concerned,
        // Conference corresponds to generic display.
        final PhoneApp app = PhoneApp.getInstance();
        int phoneType = call.getPhone().getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            CdmaPhoneCallState.PhoneCallState state = app.cdmaPhoneCallState.getCurrentCallState();
            if ((state == CdmaPhoneCallState.PhoneCallState.CONF_CALL)
                    || ((state == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && !app.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing())) {
                return true;
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            return call.isMptyCall;
        }else if (phoneType == Phone.PHONE_TYPE_SIP) {
        	List<Connection> connections = call.getConnections();
          if (connections != null && connections.size() > 1) {
              return true;
          }
        }
        return false;

        // TODO: We may still want to change the semantics of this method
        // to say that a given call is only really a conference call if
        // the number of ACTIVE connections, not the total number of
        // connections, is greater than one.  (See warning comment in the
        // javadoc above.)
        // Here's an implementation of that:
        //        if (connections == null) {
        //            return false;
        //        }
        //        int numActiveConnections = 0;
        //        for (Connection conn : connections) {
        //            if (DBG) log("  - CONN: " + conn + ", state = " + conn.getState());
        //            if (conn.getState() == Call.State.ACTIVE) numActiveConnections++;
        //            if (numActiveConnections > 1) {
        //                return true;
        //            }
        //        }
        //        return false;
    }

    /**
     * Launch the Dialer to start a new call.
     * This is just a wrapper around the ACTION_DIAL intent.
     */
    static void startNewCall(final CallManager cm) {
        final PhoneApp app = PhoneApp.getInstance();

        // Sanity-check that this is OK given the current state of the phone.
        if (!okToAddCall(cm)) {
            Log.w(LOG_TAG, "startNewCall: can't add a new call in the current state");
            dumpCallManager();
            //Let this go, if there are one sip call and one gsm call.
            //return;
        }

        // if applicable, mute the call while we're showing the add call UI.
        if (cm.hasActiveFgCall()) {
            setMuteInternal(cm.getActiveFgCall().getPhone(), true);
            // Inform the phone app that this mute state was NOT done
            // voluntarily by the User.
            app.setRestoreMuteOnInCallResume(true);
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // when we request the dialer come up, we also want to inform
        // it that we're going through the "add call" option from the
        // InCallScreen / PhoneUtils.
        intent.putExtra(ADD_CALL_MODE_KEY, true);

        try {
            app.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            log("No activity found for intent: " + intent);
        }

    }

//MTK add begin:
    /**
     * Brings up the UI used to handle an incoming call.
     *
     * Originally, this brought up an IncomingCallPanel instance
     * (which was a subclass of Dialog) on top of whatever app
     * was currently running.  Now, we take you directly to the
     * in-call screen, whose CallCard automatically does the right
     * thing if there's a Call that's currently ringing.
     */
    static void showIncomingCallUi() {
        if (DBG) log("showIncomingCallUi()...");
        PhoneApp app = PhoneApp.getInstance();

        // Before bringing up the "incoming call" UI, force any system
        // dialogs (like "recent tasks" or the power dialog) to close first.
        try {
            ActivityManagerNative.getDefault().closeSystemDialogs("call");
        } catch (RemoteException e) {
        }

        // Go directly to the in-call screen.
        // (No need to do anything special if we're already on the in-call
        // screen; it'll notice the phone state change and update itself.)

        // But first, grab a full wake lock.  We do this here, before we
        // even fire off the InCallScreen intent, to make sure the
        // ActivityManager doesn't try to pause the InCallScreen as soon
        // as it comes up.  (See bug 1648751.)
        //
        // And since the InCallScreen isn't visible yet (we haven't even
        // fired off the intent yet), we DON'T want the screen to actually
        // come on right now.  So *before* acquiring the wake lock we need
        // to call preventScreenOn(), which tells the PowerManager that
        // the screen should stay off even if someone's holding a full
        // wake lock.  (This prevents any flicker during the "incoming
        // call" sequence.  The corresponding preventScreenOn(false) call
        // will come from the InCallScreen when it's finally ready to be
        // displayed.)
        //
        // TODO: this is all a temporary workaround.  The real fix is to add
        // an Activity attribute saying "this Activity wants to wake up the
        // phone when it's displayed"; that way the ActivityManager could
        // manage the wake locks *and* arrange for the screen to come on at
        // the exact moment that the InCallScreen is ready to be displayed.
        // (See bug 1648751.)
        app.preventScreenOn(true);
        app.requestWakeState(PhoneApp.WakeState.FULL);

        // Fire off the InCallScreen intent.
        app.displayCallScreen(true);
    }
//MTK add end

    static void turnOnSpeaker(Context context, boolean flag, boolean store) {
        turnOnSpeaker(context, flag, store, true);
    }
    
    static void turnOnSpeaker(Context context, boolean flag, boolean store, boolean isUpdateNotification) {
        if (DBG) log("turnOnSpeaker(flag=" + flag + ", store=" + store + ")...");
        final PhoneApp app = PhoneApp.getInstance();

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(flag);

        // record the speaker-enable value
        if (store) {
            sIsSpeakerEnabled = flag;
        }

        // Update the status bar icon
        if (isUpdateNotification) {
            app.notificationMgr.updateSpeakerNotification(flag);
        }

        // We also need to make a fresh call to PhoneApp.updateWakeState()
        // any time the speaker state changes, since the screen timeout is
        // sometimes different depending on whether or not the speaker is
        // in use.
        app.updateWakeState();

        // Update the Proximity sensor based on speaker state
        app.updateProximitySensorMode(app.mCM.getState());

        app.mCM.setEchoSuppressionEnabled(flag);
    }

    /**
     * Restore the speaker mode, called after a wired headset disconnect
     * event.
     */
    static void restoreSpeakerMode(Context context) {
        if (DBG) log("restoreSpeakerMode, restoring to: " + sIsSpeakerEnabled);

        // change the mode if needed.
        if (FeatureOption.MTK_TB_APP_CALL_FORCE_SPEAKER_ON == true)
        {
          if (!isSpeakerOn(context))
          	turnOnSpeaker(context, true, false, false);
        }
        else
        {
          if (isSpeakerOn(context) != sIsSpeakerEnabled) {
             turnOnSpeaker(context, sIsSpeakerEnabled, false);
          }
        }
    }

    static boolean isSpeakerOn(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }


    static void turnOnNoiseSuppression(Context context, boolean flag, boolean store) {
        if (DBG) log("turnOnNoiseSuppression: " + flag);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return;
        }

        if (flag) {
            audioManager.setParameters("noise_suppression=auto");
        } else {
            audioManager.setParameters("noise_suppression=off");
        }

        // record the speaker-enable value
        if (store) {
            sIsNoiseSuppressionEnabled = flag;
        }

        // TODO: implement and manage ICON

    }

    static void restoreNoiseSuppression(Context context) {
        if (DBG) log("restoreNoiseSuppression, restoring to: " + sIsNoiseSuppressionEnabled);

        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return;
        }

        // change the mode if needed.
        if (isNoiseSuppressionOn(context) != sIsNoiseSuppressionEnabled) {
            turnOnNoiseSuppression(context, sIsNoiseSuppressionEnabled, false);
        }
    }

    static boolean isNoiseSuppressionOn(Context context) {

        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return false;
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String noiseSuppression = audioManager.getParameters("noise_suppression");
        if (DBG) log("isNoiseSuppressionOn: " + noiseSuppression);
        if (noiseSuppression.contains("off")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * Mute / umute the foreground phone, which has the current foreground call
     *
     * All muting / unmuting from the in-call UI should go through this
     * wrapper.
     *
     * Wrapper around Phone.setMute() and setMicrophoneMute().
     * It also updates the connectionMuteTable and mute icon in the status bar.
     *
     */
    static void setMute(boolean muted) {
        CallManager cm = PhoneApp.getInstance().mCM;

        // make the call to mute the audio
        setMuteInternal(cm.getFgPhone(), muted);

        // update the foreground connections to match.  This includes
        // all the connections on conference calls.
        for (Connection cn : cm.getActiveFgCall().getConnections()) {
            if (sConnectionMuteTable.get(cn) == null) {
                if (DBG) log("problem retrieving mute value for this connection.");
            }
            sConnectionMuteTable.put(cn, Boolean.valueOf(muted));
        }
    }

    /**
     * Internally used muting function.
     */
    private static void setMuteInternal(Phone phone, boolean muted) {
        if (DBG) log("setMuteInternal: " + muted);
        final PhoneApp app = PhoneApp.getInstance();
        Context context = phone.getContext();
        boolean routeToAudioManager =
            context.getResources().getBoolean(R.bool.send_mic_mute_to_AudioManager);
        if (routeToAudioManager) {
            AudioManager audioManager =
                (AudioManager) phone.getContext().getSystemService(Context.AUDIO_SERVICE);
            if (DBG) log("setMuteInternal: using setMicrophoneMute(" + muted + ")...");
            audioManager.setMicrophoneMute(muted);
        } else {
            if (DBG) log("setMuteInternal: using phone.setMute(" + muted + ")...");
            phone.setMute(muted);
        }
        app.notificationMgr.updateMuteNotification();
    }

    /**
     * Get the mute state of foreground phone, which has the current
     * foreground call
     */
    static boolean getMute() {
        final PhoneApp app = PhoneApp.getInstance();

        boolean routeToAudioManager =
            app.getResources().getBoolean(R.bool.send_mic_mute_to_AudioManager);
        if (routeToAudioManager) {
            AudioManager audioManager =
                (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
            return audioManager.isMicrophoneMute();
        } else {
            return app.mCM.getMute();
        }
    }

    /* package */ static void setAudioMode() {
        setAudioMode(PhoneApp.getInstance().mCM);
    }

    /**
     * Sets the audio mode per current phone state.
     */
    /* package */ static void setAudioMode(CallManager cm) {
        if (DBG) Log.d(LOG_TAG, "setAudioMode()..." + cm.getState());

        Context context = PhoneApp.getInstance();
        AudioManager audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
        int modeBefore = audioManager.getMode();
        boolean isSipPhone = cm.getFgPhone().getPhoneType() == Phone.PHONE_TYPE_SIP;
        int mode = getExpectedAudioMode();
        if (PhoneUtils.isSupportFeature("TTY") && !isSipPhone)
        {
            if ((mode == AudioManager.MODE_NORMAL) && (!ttyMode.equals("tty_off") && isOpen))
            {
                audioManager.setParameters("tty_mode="+ "tty_off");
                isOpen = false;
            }
        }

        cm.setAudioMode();

        if (PhoneUtils.isSupportFeature("TTY") == true && !isSipPhone)
        {
            if ((mode == AudioManager.MODE_IN_CALL) && (!ttyMode.equals("tty_off") && !isOpen))
            {
                audioManager.setParameters("tty_mode="+ ttyMode);
                isOpen = true;
            }
        }
        int modeAfter = audioManager.getMode();

        if (modeBefore != modeAfter) {
            // Enable stack dump only when actively debugging ("new Throwable()" is expensive!)
            if (DBG_SETAUDIOMODE_STACK) Log.d(LOG_TAG, "Stack:", new Throwable("stack dump"));
        } else {
            if (DBG) Log.d(LOG_TAG, "setAudioMode() no change: "
                    + audioModeToString(modeBefore));
        }
    }
    
    static int getExpectedAudioMode()
    {
    	int mode = AudioManager.MODE_NORMAL;
    	CallManager cm = PhoneApp.getInstance().mCM;
    	switch (cm.getState()) {
	        case RINGING:
	            mode = AudioManager.MODE_RINGTONE;
	            break;
	        case OFFHOOK:
	            Phone fgPhone = cm.getFgPhone();
	            // Enable IN_CALL mode while foreground call is in DIALING,
	            // ALERTING, ACTIVE and DISCONNECTING state and not from sipPhone
	            if (cm.getActiveFgCallState() != Call.State.IDLE
	                    && cm.getActiveFgCallState() != Call.State.DISCONNECTED
	                    && !(fgPhone instanceof SipPhone)) {
	                mode = AudioManager.MODE_IN_CALL;
	            }
	            break;
        }
    	
    	return mode;
    }
    
    private static String audioModeToString(int mode) {
        switch (mode) {
            case AudioManager.MODE_INVALID: return "MODE_INVALID";
            case AudioManager.MODE_CURRENT: return "MODE_CURRENT";
            case AudioManager.MODE_NORMAL: return "MODE_NORMAL";
            case AudioManager.MODE_RINGTONE: return "MODE_RINGTONE";
            case AudioManager.MODE_IN_CALL: return "MODE_IN_CALL";
            default: return String.valueOf(mode);
        }
    }

    /**
     * Handles the wired headset button while in-call.
     *
     * This is called from the PhoneApp, not from the InCallScreen,
     * since the HEADSETHOOK button means "mute or unmute the current
     * call" *any* time a call is active, even if the user isn't actually
     * on the in-call screen.
     *
     * @return true if we consumed the event.
     */
    /* package */ static boolean handleHeadsetHook(Phone phone, KeyEvent event) {
        if (DBG) log("handleHeadsetHook()..." + event.getAction() + " " + event.getRepeatCount());
        final PhoneApp app = PhoneApp.getInstance();

        // If the phone is totally idle, we ignore HEADSETHOOK events
        // (and instead let them fall through to the media player.)
/*Google: if (phone.getState() == Phone.State.IDLE) {
            return false;
        } */
//MTK begin:
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if (((GeminiPhone)phone).getState() == Phone.State.IDLE) {
                return false;
            }
        } else {
            if (phone.getState() == Phone.State.IDLE) {
                return false;
            }
        }
//MTK end

        // Ok, the phone is in use.
        // The headset button button means "Answer" if an incoming call is
        // ringing.  If not, it toggles the mute / unmute state.
        //
        // And in any case we *always* consume this event; this means
        // that the usual mediaplayer-related behavior of the headset
        // button will NEVER happen while the user is on a call.

/*Google: final boolean hasRingingCall = !phone.getRingingCall().isIdle();
        final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle(); */
//MTK begin:
        final boolean hasRingingCall , hasActiveCall , hasHoldingCall;

        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            hasRingingCall = !((GeminiPhone)phone).getRingingCall().isIdle();
            hasActiveCall = !((GeminiPhone)phone).getForegroundCall().isIdle();
            hasHoldingCall = !((GeminiPhone)phone).getBackgroundCall().isIdle();
        } else {
            hasRingingCall = !phone.getRingingCall().isIdle();
            hasActiveCall = !phone.getForegroundCall().isIdle();
            hasHoldingCall = !phone.getBackgroundCall().isIdle();
        }
//MTK end

        if (hasRingingCall &&
            event.getRepeatCount() == 0 &&
            event.getAction() == KeyEvent.ACTION_UP) {
            // If an incoming call is ringing, answer it (just like with the
            // CALL button):
            int phoneType = phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                answerCall(phone.getRingingCall());
            } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                    || (phoneType == Phone.PHONE_TYPE_SIP)) {
                if (hasActiveCall && hasHoldingCall) {
                    if (DBG) log("handleHeadsetHook: ringing (both lines in use) ==> answer!");
                    answerAndEndActive(app.mCM, phone.getRingingCall());
                } else {
                    if (DBG) log("handleHeadsetHook: ringing ==> answer!");
                    // answerCall() will automatically hold the current
                    // active call, if there is one.
                    answerCall(phone.getRingingCall());
                }
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        } else {
            // No incoming ringing call.
            if (event.isLongPress()) {
                if (DBG) log("handleHeadsetHook: longpress -> hangup");
                hangup(app.mCM);
            }
            else if (event.getAction() == KeyEvent.ACTION_UP &&
                     event.getRepeatCount() == 0) {
//Google:       Connection c = phone.getForegroundCall().getLatestConnection();
//MTK begin:
                Connection c;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    c = ((GeminiPhone)phone).getForegroundCall().getLatestConnection();
                } else {
                    c = phone.getForegroundCall().getLatestConnection();
                }
//MTK end
                // If it is NOT an emg #, toggle the mute state. Otherwise, ignore the hook.
                if (c != null && !PhoneNumberUtils.isLocalEmergencyNumber(c.getAddress(),
                                                                          PhoneApp.getInstance())) {
                    if (getMute()) {
                        if (DBG) log("handleHeadsetHook: UNmuting...");
                        setMute(false);
                    } else {
                        if (DBG) log("handleHeadsetHook: muting...");
                        setMute(true);
                    }
                }
            }
        }

        // Even if the InCallScreen is the current activity, there's no
        // need to force it to update, because (1) if we answered a
        // ringing call, the InCallScreen will imminently get a phone
        // state change event (causing an update), and (2) if we muted or
        // unmuted, the setMute() call automagically updates the status
        // bar, and there's no "mute" indication in the InCallScreen
        // itself (other than the menu item, which only ever stays
        // onscreen for a second anyway.)
        // TODO: (2) isn't entirely true anymore. Once we return our result
        // to the PhoneApp, we ask InCallScreen to update its control widgets
        // in case we changed mute or speaker state and phones with touch-
        // screen [toggle] buttons need to update themselves.

        return true;
    }

    /**
     * Look for ANY connections on the phone that qualify as being
     * disconnected.
     *
     * @return true if we find a connection that is disconnected over
     * all the phone's call objects.
     */
    /* package */ static boolean hasDisconnectedConnections(CallManager cm) {
/*Google:  return hasDisconnectedConnections(phone.getForegroundCall()) ||
                hasDisconnectedConnections(phone.getBackgroundCall()) ||
                hasDisconnectedConnections(phone.getRingingCall()); */
//MTK begin:
        return hasDisconnectedConnections(cm.getActiveFgCall()) ||
            hasDisconnectedConnections(cm.getFirstActiveBgCall()) ||
            hasDisconnectedConnections(cm.getFirstActiveRingingCall());
//MTK end
    }

    /**
     * Iterate over all connections in a call to see if there are any
     * that are not alive (disconnected or idle).
     *
     * @return true if we find a connection that is disconnected, and
     * pending removal via
     * {@link com.android.internal.telephony.gsm.GsmCall#clearDisconnected()}.
     */
    private static final boolean hasDisconnectedConnections(Call call) {
        // look through all connections for non-active ones.
        for (Connection c : call.getConnections()) {
            if (!c.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public static boolean holdAndActiveFromDifPhone(CallManager cm)
    {
        boolean isDiffrentPhone = false;
        List<Phone> array = cm.getAllPhones();
        boolean found = false;
        for (Phone p : array)
        {
            if (p.getState() == Phone.State.OFFHOOK)
            {
                if (!found)
                {
                    found = true;
                }else {
                    isDiffrentPhone = true;
                    break;
                }
            }
        }
        
        return isDiffrentPhone;
    }
    //
    // Misc UI policy helper functions
    //
    
    //For Google default, the swap button and hold button no dependency,
    //but about our solution, the swap and hold is exclusive:If the hold button display, the swap must hide
    //so we need this method to make sure the swap can be displayed
    static boolean okToShowSwapButton(CallManager cm) {
        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();
        DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
        Call realFgCall = dt == null ? null : dt.getActiveFgCall();
        if (DualTalkUtils.isSupportDualTalk && (dt.isCdmaAndGsmActive() 
                || realFgCall != null && realFgCall.getPhone().getPhoneType() == Phone.PHONE_TYPE_CDMA 
                && PhoneUtils.hasMultipleConnections(realFgCall))) {
            return true;
        } else if ((fgCall.getState().isAlive() && bgCall.getState() == Call.State.HOLDING)
            || holdAndActiveFromDifPhone(cm)) {
            return true;
        }
        
        return false;
    }

    /**
     * @return true if we're allowed to swap calls, given the current
     * state of the Phone.
     */
    /* package */ static boolean okToSwapCalls(CallManager cm) {
        int phoneType = cm.getDefaultPhone().getPhoneType();
        DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
        if (DualTalkUtils.isSupportDualTalk) {
            Call call = dt.getActiveFgCall();
            if (call != null) {
                phoneType = call.getPhone().getPhoneType();
            }
        }
        
        if (DualTalkUtils.isSupportDualTalk && dt.isCdmaAndGsmActive()) {
            return true;
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            // CDMA: "Swap" is enabled only when the phone reaches a *generic*.
            // state by either accepting a Call Waiting or by merging two calls
            
            //For dualtalk solution, we return false always, because we will enable the "merge call" button for
            //the same use
            if (DualTalkUtils.isSupportDualTalk) {
                return false;
            }
            
            PhoneApp app = PhoneApp.getInstance();
            return (app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.CONF_CALL);
        } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                || (phoneType == Phone.PHONE_TYPE_SIP)) {
            // GSM: "Swap" is available if both lines are in use and there's no
            // incoming call.  (Actually we need to verify that the active
            // call really is in the ACTIVE state and the holding call really
            // is in the HOLDING state, since you *can't* actually swap calls
            // when the foreground call is DIALING or ALERTING.)
            return !cm.hasActiveRingingCall()
                    && ((cm.getActiveFgCall().getState() == Call.State.ACTIVE || PhoneUtils.hasActivefgEccCall(cm))
                    && (cm.getFirstActiveBgCall().getState() == Call.State.HOLDING)
                    /*|| holdAndActiveFromDifPhone(cm)*/);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    
    /* package */ static boolean hasActivefgEccCall(CallManager cm) {
        return PhoneUtils.hasActivefgEccCall(cm.getActiveFgCall());
    }

    static boolean isVoicemailNumber(Uri uri) {
        return uri != null && "voicemail".equals(uri.getScheme().toString());
    }

    static boolean hasActivefgEccCall(Call call) {
        if(call == null)
            return false;
        Connection connection = call.getEarliestConnection();
        return (call.getState() == Call.State.DIALING || call.getState() == Call.State.ALERTING) && 
                connection != null &&
                !TextUtils.isEmpty(connection.getAddress()) &&
                PhoneNumberUtils.isEmergencyNumber(connection.getAddress());
    }
    
    static boolean isEccCall(Call call) {
        Connection connection = call.getEarliestConnection();
        return (connection != null &&
                !TextUtils.isEmpty(connection.getAddress()) &&
                PhoneNumberUtils.isEmergencyNumber(connection.getAddress()));
    }

    /**
     * @return true if we're allowed to merge calls, given the current
     * state of the Phone.
     */
    /* package */ static boolean okToMergeCalls(CallManager cm) {
        int phoneType = cm.getFgPhone().getPhoneType();
        DualTalkUtils dt = PhoneApp.getInstance().notifier.mDualTalk;
        if (DualTalkUtils.isSupportDualTalk) {
            Call call = dt.getActiveFgCall();
            if (call != null) {
                phoneType = call.getPhone().getPhoneType();
            }
        }
        
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            // CDMA: "Merge" is enabled only when the user is in a 3Way call.
            PhoneApp app = PhoneApp.getInstance();
            return ((app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && !app.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing());
        } else {
            // GSM: "Merge" is available if both lines are in use and there's no
            // incoming call, *and* the current conference isn't already
            // "full".
            // TODO: shall move all okToMerge logic to CallManager
            return !cm.hasActiveRingingCall() && cm.hasActiveFgCall()
                    && cm.getActiveFgCall().getState() != Call.State.DIALING 
                    && cm.getActiveFgCall().getState() != Call.State.ALERTING 
                    && cm.hasActiveBgCall() 
                    && (cm.canConference(cm.getFirstActiveBgCall())
                            || (DualTalkUtils.isSupportDualTalk && PhoneApp.getInstance().notifier.mDualTalk.isDualTalkMultipleHoldCase()));
        }
    }

    /**
     * @return true if the UI should let you add a new call, given the current
     * state of the Phone.
     */
    /* package */ static boolean okToAddCall(CallManager cm) {
        Phone phone = cm.getActiveFgCall().getPhone();

        // "Add call" is never allowed in emergency callback mode (ECM).
        if (isPhoneInEcm(phone)) {
            return false;
        }

        int phoneType = phone.getPhoneType();
        final Call.State fgCallState = cm.getActiveFgCall().getState();
        
        if (!fgCallState.isAlive()) {
            phone = cm.getFirstActiveBgCall().getPhone();
            phoneType = phone.getPhoneType();
        }
        
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
           // CDMA: "Add call" button is only enabled when:
           // - ForegroundCall is in ACTIVE state
           // - After 30 seconds of user Ignoring/Missing a Call Waiting call.
            PhoneApp app = PhoneApp.getInstance();
            return ((fgCallState == Call.State.ACTIVE)
                    && (app.cdmaPhoneCallState.getAddCallMenuStateAfterCallWaiting()));
        } else if ((phoneType == Phone.PHONE_TYPE_GSM)
                || (phoneType == Phone.PHONE_TYPE_SIP)) {
            // GSM: "Add call" is available only if ALL of the following are true:
            // - There's no incoming ringing call
            // - There's < 2 lines in use
            // - The foreground call is ACTIVE or IDLE or DISCONNECTED.
            //   (We mainly need to make sure it *isn't* DIALING or ALERTING.)
            final boolean hasRingingCall = cm.hasActiveRingingCall();
            final boolean hasActiveCall = cm.hasActiveFgCall();
            final boolean hasHoldingCall = cm.hasActiveBgCall();
            boolean allLinesTaken = hasActiveCall && hasHoldingCall;
            
            if (DualTalkUtils.isSupportDualTalk && allLinesTaken) {
                allLinesTaken = !PhoneApp.getInstance().notifier.mDualTalk.canAddCallForDualTalk();
            }

            return !hasRingingCall
                    && !allLinesTaken
                    && ((fgCallState == Call.State.ACTIVE)
                        || (fgCallState == Call.State.IDLE)
                        || (fgCallState == Call.State.DISCONNECTED));
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }

    /**
     * Based on the input CNAP number string,
     * @return _RESTRICTED or _UNKNOWN for all the special CNAP strings.
     * Otherwise, return CNAP_SPECIAL_CASE_NO.
     */
    private static int checkCnapSpecialCases(String n) {
        if (n.equals("PRIVATE") ||
                n.equals("P") ||
                n.equals("RES")) {
            if (DBG) log("checkCnapSpecialCases, PRIVATE string: " + n);
            return Connection.PRESENTATION_RESTRICTED;
        } else if (n.equals("UNAVAILABLE") ||
                n.equals("UNKNOWN") ||
                n.equals("UNA") ||
                n.equals("U")) {
            if (DBG) log("checkCnapSpecialCases, UNKNOWN string: " + n);
            return Connection.PRESENTATION_UNKNOWN;
        } else {
            if (DBG) log("checkCnapSpecialCases, normal str. number: " + n);
            return CNAP_SPECIAL_CASE_NO;
        }
    }

    /**
     * Handles certain "corner cases" for CNAP. When we receive weird phone numbers
     * from the network to indicate different number presentations, convert them to
     * expected number and presentation values within the CallerInfo object.
     * @param number number we use to verify if we are in a corner case
     * @param presentation presentation value used to verify if we are in a corner case
     * @return the new String that should be used for the phone number
     */
    /* package */ static String modifyForSpecialCnapCases(Context context, CallerInfo ci,
            String number, int presentation) {
        // Obviously we return number if ci == null, but still return number if
        // number == null, because in these cases the correct string will still be
        // displayed/logged after this function returns based on the presentation value.
        if (ci == null || number == null) return number;

        if (DBG) log("modifyForSpecialCnapCases: initially, number=" + number
                + ", presentation=" + presentation + " ci " + ci);

        // "ABSENT NUMBER" is a possible value we could get from the network as the
        // phone number, so if this happens, change it to "Unknown" in the CallerInfo
        // and fix the presentation to be the same.
        if (number.equals(context.getString(R.string.absent_num))
                && presentation == Connection.PRESENTATION_ALLOWED) {
            number = context.getString(R.string.unknown);
            ci.numberPresentation = Connection.PRESENTATION_UNKNOWN;
        }

        // Check for other special "corner cases" for CNAP and fix them similarly. Corner
        // cases only apply if we received an allowed presentation from the network, so check
        // if we think we have an allowed presentation, or if the CallerInfo presentation doesn't
        // match the presentation passed in for verification (meaning we changed it previously
        // because it's a corner case and we're being called from a different entry point).
        if (ci.numberPresentation == Connection.PRESENTATION_ALLOWED
                || (ci.numberPresentation != presentation
                        && presentation == Connection.PRESENTATION_ALLOWED)) {
            int cnapSpecialCase = checkCnapSpecialCases(number);
            if (cnapSpecialCase != CNAP_SPECIAL_CASE_NO) {
                // For all special strings, change number & numberPresentation.
                if (cnapSpecialCase == Connection.PRESENTATION_RESTRICTED) {
                    number = context.getString(R.string.private_num);
                } else if (cnapSpecialCase == Connection.PRESENTATION_UNKNOWN) {
                    number = context.getString(R.string.unknown);
                }
                if (DBG) log("SpecialCnap: number=" + number
                        + "; presentation now=" + cnapSpecialCase);
                ci.numberPresentation = cnapSpecialCase;
            }
        }
        if (DBG) log("modifyForSpecialCnapCases: returning number string=" + number);
        return number;
    }

    //
    // Support for 3rd party phone service providers.
    //

    /**
     * Check if all the provider's info is present in the intent.
     * @param intent Expected to have the provider's extra.
     * @return true if the intent has all the extras to build the
     * in-call screen's provider info overlay.
     */
    /* package */ static boolean hasPhoneProviderExtras(Intent intent) {
        if (null == intent) {
            return false;
        }
        final String name = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        final String gatewayUri = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI);

        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(gatewayUri);
    }

    /**
     * Copy all the expected extras set when a 3rd party provider is
     * used from the source intent to the destination one.  Checks all
     * the required extras are present, if any is missing, none will
     * be copied.
     * @param src Intent which may contain the provider's extras.
     * @param dst Intent where a copy of the extras will be added if applicable.
     */
    /* package */ static void checkAndCopyPhoneProviderExtras(Intent src, Intent dst) {
        if (!hasPhoneProviderExtras(src)) {
            Log.d(LOG_TAG, "checkAndCopyPhoneProviderExtras: some or all extras are missing.");
            return;
        }

        dst.putExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE,
                     src.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE));
        dst.putExtra(InCallScreen.EXTRA_GATEWAY_URI,
                     src.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI));
    }

    /**
     * Get the provider's label from the intent.
     * @param context to lookup the provider's package name.
     * @param intent with an extra set to the provider's package name.
     * @return The provider's application label. null if an error
     * occurred during the lookup of the package name or the label.
     */
    /* package */ static CharSequence getProviderLabel(Context context, Intent intent) {
        String packageName = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);

            return pm.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Get the provider's icon.
     * @param context to lookup the provider's icon.
     * @param intent with an extra set to the provider's package name.
     * @return The provider's application icon. null if an error occured during the icon lookup.
     */
    /* package */ static Drawable getProviderIcon(Context context, Intent intent) {
        String packageName = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        PackageManager pm = context.getPackageManager();

        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Return the gateway uri from the intent.
     * @param intent With the gateway uri extra.
     * @return The gateway URI or null if not found.
     */
    /* package */ static Uri getProviderGatewayUri(Intent intent) {
        String uri = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI);
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }

    /**
     * Return a formatted version of the uri's scheme specific
     * part. E.g for 'tel:12345678', return '1-234-5678'.
     * @param uri A 'tel:' URI with the gateway phone number.
     * @return the provider's address (from the gateway uri) formatted
     * for user display. null if uri was null or its scheme was not 'tel:'.
     */
    /* package */ static String formatProviderUri(Uri uri) {
        if (null != uri) {
            if (Constants.SCHEME_TEL.equals(uri.getScheme())) {
                return PhoneNumberUtils.formatNumber(uri.getSchemeSpecificPart());
            } else {
                return uri.toString();
            }
        }
        return null;
    }

    /**
     * Check if a phone number can be route through a 3rd party
     * gateway. The number must be a global phone number in numerical
     * form (1-800-666-SEXY won't work).
     *
     * MMI codes and the like cannot be used as a dial number for the
     * gateway either.
     *
     * @param number To be dialed via a 3rd party gateway.
     * @return true If the number can be routed through the 3rd party network.
     */
    /* package */ static boolean isRoutableViaGateway(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        number = PhoneNumberUtils.stripSeparators(number);
        if (!number.equals(PhoneNumberUtils.convertKeypadLettersToDigits(number))) {
            return false;
        }
        number = PhoneNumberUtils.extractNetworkPortion(number);
        return PhoneNumberUtils.isGlobalPhoneNumber(number);
    }

   /**
    * This function is called when phone answers or places a call.
    * Check if the phone is in a car dock or desk dock.
    * If yes, turn on the speaker, when no wired or BT headsets are connected.
    * Otherwise do nothing.
    */
    public static void activateSpeakerIfDocked(Phone phone) {
        if (DBG) log("activateSpeakerIfDocked()...");

        if (PhoneApp.mDockState != Intent.EXTRA_DOCK_STATE_UNDOCKED) {
            if (DBG) log("activateSpeakerIfDocked(): In a dock -> may need to turn on speaker.");
            PhoneApp app = PhoneApp.getInstance();
            BluetoothHandsfree bthf = app.getBluetoothHandsfree();

            if (!app.isHeadsetPlugged() && !(bthf != null && bthf.isAudioOn())) {
                turnOnSpeaker(phone.getContext(), true, true);
            }
        }
    }


    /**
     * Returns whether the phone is in ECM ("Emergency Callback Mode") or not.
     */
    /* package */ static boolean isPhoneInEcm(Phone phone) {
        if ((phone != null) && TelephonyCapabilities.supportsEcm(phone)) {
            // For phones that support ECM, return true iff PROPERTY_INECM_MODE == "true".
            // TODO: There ought to be a better API for this than just
            // exposing a system property all the way up to the app layer,
            // probably a method like "inEcm()" provided by the telephony
            // layer.
            String ecmMode =
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (ecmMode != null) {
                return ecmMode.equals("true");
            }
        }
        return false;
    }

    /**
     * Returns the most appropriate Phone object to handle a call
     * to the specified number.
     *
     * @param cm the CallManager.
     * @param scheme the scheme from the data URI that the number originally came from.
     * @param number the phone number, or SIP address.
     */
    public static Phone pickPhoneBasedOnNumber(CallManager cm,
            String scheme, String number, String primarySipUri) {
        if (DBG) log("pickPhoneBasedOnNumber: scheme " + scheme
                + ", number " + number + ", sipUri " + primarySipUri);

        if (primarySipUri != null) {
            Phone phone = getSipPhoneFromUri(cm, primarySipUri);
            if (phone != null) return phone;
        }
        
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            return PhoneApp.getInstance().mCMGemini.getDefaultPhoneGemini();
        } 
        else return cm.getDefaultPhone();
    }

    public static Phone getSipPhoneFromUri(CallManager cm, String target) {
        for (Phone phone : cm.getAllPhones()) {
            if (phone.getPhoneType() == Phone.PHONE_TYPE_SIP) {
                String sipUri = ((SipPhone) phone).getSipUri();
                if (target.equals(sipUri)) {
                    if (DBG) log("- pickPhoneBasedOnNumber:" +
                            "found SipPhone! obj = " + phone + ", "
                            + phone.getClass());
                    return phone;
                }
            }
        }
        return null;
    }

    public static boolean isRealIncomingCall(Call.State state) {
        return (state == Call.State.INCOMING && !PhoneApp.getInstance().mCM.hasActiveFgCall() && !PhoneApp.getInstance().mCM.hasActiveBgCall());
	}

    private static boolean sVoipSupported = false;
    static {
        PhoneApp app = PhoneApp.getInstance();
        sVoipSupported = SipManager.isVoipSupported(app)
                && app.getResources().getBoolean(com.android.internal.R.bool.config_built_in_sip_phone)
                && app.getResources().getBoolean(com.android.internal.R.bool.config_voice_capable);
    }

    /**
     * @return true if this device supports voice calls using the built-in SIP stack.
     */
    static boolean isVoipSupported() {
        return sVoipSupported;
    }

    //
    // General phone and call state debugging/testing code
    //

    /* package */ static void dumpCallState(Phone phone) {
        PhoneApp app = PhoneApp.getInstance();
        Log.d(LOG_TAG, "dumpCallState():");
//MTK begin:
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.d(LOG_TAG, "- GeminiPhone: " + ((GeminiPhone)phone) + ", name = " + ((GeminiPhone)phone).getPhoneName()
                  + ", state = " + ((GeminiPhone)phone).getState());

            Log.d(LOG_TAG, "- GeminiPhone Sim1: " + ((GeminiPhone)phone) + ", name = " + ((GeminiPhone)phone).getPhoneName()
                  + ", state = " + ((GeminiPhone)phone).getStateGemini(Phone.GEMINI_SIM_1));			

            Log.d(LOG_TAG, "- GeminiPhone Sim2: " + ((GeminiPhone)phone) + ", name = " + ((GeminiPhone)phone).getPhoneName()
                  + ", state = " + ((GeminiPhone)phone).getStateGemini(Phone.GEMINI_SIM_2));						
        } else {
            Log.d(LOG_TAG, "- Phone: " + phone + ", name = " + phone.getPhoneName()
                  + ", state = " + phone.getState());
        }
//MTK end

        StringBuilder b = new StringBuilder(128);

//MTK begin:
        Call call;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            call = ((GeminiPhone)phone).getForegroundCall();
        } else {
            call = phone.getForegroundCall();
	 }
//MTK end

        b.setLength(0);
        b.append("  - FG call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());

//MTK begin:
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            call = ((GeminiPhone)phone).getBackgroundCall();
        } else {
            call = phone.getBackgroundCall();
	    }
//MTK end
        b.setLength(0);
        b.append("  - BG call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());

//MTK begin:
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            call = ((GeminiPhone)phone).getRingingCall();
        } else {
            call = phone.getRingingCall();
	    }
//MTK end
        b.setLength(0);
        b.append("  - RINGING call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());


        final boolean hasRingingCall = !phone.getRingingCall().isIdle();
        final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle();
        final boolean allLinesTaken = hasActiveCall && hasHoldingCall;
        b.setLength(0);
        b.append("  - hasRingingCall ").append(hasRingingCall);
        b.append(" hasActiveCall ").append(hasActiveCall);
        b.append(" hasHoldingCall ").append(hasHoldingCall);
        b.append(" allLinesTaken ").append(allLinesTaken);
        Log.d(LOG_TAG, b.toString());

        // On CDMA phones, dump out the CdmaPhoneCallState too:
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if (app.cdmaPhoneCallState != null) {
                Log.d(LOG_TAG, "  - CDMA call state: "
                      + app.cdmaPhoneCallState.getCurrentCallState());
            } else {
                Log.d(LOG_TAG, "  - CDMA device, but null cdmaPhoneCallState!");
            }
        }

        // Watch out: the isRinging() call below does NOT tell us anything
        // about the state of the telephony layer; it merely tells us whether
        // the Ringer manager is currently playing the ringtone.
        boolean ringing = app.getRinger().isRinging();
        Log.d(LOG_TAG, "  - Ringer state: " + ringing);
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    static void dumpCallManager() {
        Call call;
        CallManager cm = PhoneApp.getInstance().mCM;
        StringBuilder b = new StringBuilder(128);



        Log.d(LOG_TAG, "############### dumpCallManager() ##############");
        // TODO: Don't log "cm" itself, since CallManager.toString()
        // already spews out almost all this same information.
        // We should fix CallManager.toString() to be more minimal, and
        // use an explicit dumpState() method for the verbose dump.
        // Log.d(LOG_TAG, "CallManager: " + cm
        //         + ", state = " + cm.getState());
        Log.d(LOG_TAG, "CallManager: state = " + cm.getState());
        b.setLength(0);
        call = cm.getActiveFgCall();
        b.append(" - FG call: ").append(cm.hasActiveFgCall()? "YES ": "NO ");
        b.append(call);
        b.append( "  State: ").append(cm.getActiveFgCallState());
        b.append( "  Conn: ").append(cm.getFgCallConnections());
        Log.d(LOG_TAG, b.toString());
        b.setLength(0);
        call = cm.getFirstActiveBgCall();
        b.append(" - BG call: ").append(cm.hasActiveBgCall()? "YES ": "NO ");
        b.append(call);
        b.append( "  State: ").append(cm.getFirstActiveBgCall().getState());
        b.append( "  Conn: ").append(cm.getBgCallConnections());
        Log.d(LOG_TAG, b.toString());
        b.setLength(0);
        call = cm.getFirstActiveRingingCall();
        b.append(" - RINGING call: ").append(cm.hasActiveRingingCall()? "YES ": "NO ");
        b.append(call);
        b.append( "  State: ").append(cm.getFirstActiveRingingCall().getState());
        Log.d(LOG_TAG, b.toString());



        for (Phone phone : CallManager.getInstance().getAllPhones()) {
            if (phone != null) {
                Log.d(LOG_TAG, "Phone: " + phone + ", name = " + phone.getPhoneName()
                        + ", state = " + phone.getState());
                b.setLength(0);
                call = phone.getForegroundCall();
                b.append(" - FG call: ").append(call);
                b.append( "  State: ").append(call.getState());
                b.append( "  Conn: ").append(call.hasConnections());
                Log.d(LOG_TAG, b.toString());
                b.setLength(0);
                call = phone.getBackgroundCall();
                b.append(" - BG call: ").append(call);
                b.append( "  State: ").append(call.getState());
                b.append( "  Conn: ").append(call.hasConnections());
                Log.d(LOG_TAG, b.toString());b.setLength(0);
                call = phone.getRingingCall();
                b.append(" - RINGING call: ").append(call);
                b.append( "  State: ").append(call.getState());
                b.append( "  Conn: ").append(call.hasConnections());
                Log.d(LOG_TAG, b.toString());
            }
        }

        Log.d(LOG_TAG, "############## END dumpCallManager() ###############");
    }

    //Used for MMI
    
    private static void autoUpdateUssdReponseUi(View dialogView){
        TextView justForUpdate = (TextView) dialogView.findViewById(R.id.ussd_update);
        justForUpdate.setWidth(1);
        justForUpdate.setText(R.string.fdn_contact_name_number_invalid);
        justForUpdate.setFocusableInTouchMode(true);
    }
    
    //Add for recording the USSD dialog, and use to cancel the dialog when enter airplane mode
    public static void DismissMMIDialog()
    {
        if (null != mDialog)
        {
            mDialog.cancel();
            mDialog = null;
        }
    }
    
    public static boolean isShowUssdDialog()
    {
        return mUssdActivity != null;
    }

    static public boolean getMmiFinished()
    {
    	return mbMmiFinished;
    }
    
    static public void setMmiFinished(boolean state)
    {
    	mbMmiFinished = state;
    }
    //End Used for MMI

    // MTK_OP01_PROTECT_START
    // MTK_OP02_PROTECT_START
    public static String getOptrProperties() {
        if (null == mOptr) {
            mOptr = SystemProperties.get("ro.operator.optr");
            if (DBG) log("getOptrProperties, the mOptr is " + mOptr);
            if (null == mOptr) {
                mOptr = "";
            }
        }
        return mOptr;   
    } 

    public static String getSpecProperties() {
        if (null == mSpec) {
            mSpec = SystemProperties.get("ro.operator.spec");			 
            if (DBG) log("getSpecProperties, the mSpec is " + mSpec);
            if (null == mSpec) {
                mSpec = "";
            }
        }
        return mSpec;			
    }

    public static String getSegProperties() {
        if (null == mSeg) {
            mSeg = SystemProperties.get("ro.operator.seg");			
            if (DBG) log("getSegProperties, the mSeg is " + mSeg);
            if (null == mSeg) {
                mSeg = "";
            }
        }
        return mSeg;
    }
    // MTK_OP02_PROTECT_END
    // MTK_OP01_PROTECT_END

    public static boolean isDMLocked(){
        boolean locked = false;
        try{
            IBinder binder=ServiceManager.getService("DMAgent");
            DMAgent agent = null;
            if(binder != null)agent=DMAgent.Stub.asInterface (binder); 
            if(agent != null)locked=agent.isLockFlagSet ();
        }catch (Exception e) {
            e.printStackTrace ();
        } 
        if (DBG) log("isDMLocked(): locked = " + locked);
        return locked;
    }

    public static void setDualMicMode(String dualMic)
    {
        Context context = PhoneApp.getInstance().getApplicationContext();
        if (context == null) return;
        AudioManager audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setParameters(PhoneUtils.DUALMIC_MODE + "=" + dualMic);
    }
    
    public static boolean isSupportFeature(String feature) {
        if (feature == null) {
            return false;
        }
        
        if (feature.equals("TTY")) {
            return FeatureOption.MTK_TTY_SUPPORT;
        } else if (feature.equals("DUAL_MIC")) {
            return FeatureOption.MTK_DUAL_MIC_SUPPORT;
        } else if (feature.equals("IP_DIAL")) {
            return true;
        } else if (feature.equals("3G_SWITCH")) {
            return FeatureOption.MTK_GEMINI_3G_SWITCH;
        } else if (feature.equals("VT_VOICE_RECORDING")) {
            return true;
        } else if (feature.equals("VT_VIDEO_RECORDING")) {
            return true;
        } else if (feature.equals("PHONE_VOICE_RECORDING")) {
            return FeatureOption.MTK_PHONE_VOICE_RECORDING;
        }
        return false;
    }
    
    public static boolean isVideoCall(Call call) {
        if (call == null /*|| !call.getState().isAlive()*/) {
            return false;
        }
        
        Connection c = call.getLatestConnection();
        if (c == null) {
            return false;
        } else {
            return c.isVideo();
        }
    }

    /* below are added by mediatek .inc */
    public static boolean hasPendingMmi() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            GeminiPhone phone = (GeminiPhone) PhoneApp.getInstance().phone;
            final int mmiCount1 = ((GeminiPhone)phone).getPendingMmiCodesGemini(Phone.GEMINI_SIM_1).size();
            final int mmiCount2 = ((GeminiPhone)phone).getPendingMmiCodesGemini(Phone.GEMINI_SIM_2).size();
            return mmiCount1 > 0 || mmiCount2 > 0;
        } else {
            return PhoneApp.getInstance().phone.getPendingMmiCodes().size() > 0;
        }
    }

    // The private added extra for call, should be added code here
    public static void checkAndCopyPrivateExtras(final Intent origIntent, Intent newIntent) {
        int slot = origIntent.getIntExtra(Constants.EXTRA_SLOT_ID, -1);
        if (-1 != slot) {
            newIntent.putExtra(Constants.EXTRA_SLOT_ID, slot);
        }
        if(PhoneApp.sVideoCallSupport) {
            boolean isVideoCall = origIntent.getBooleanExtra(Constants.EXTRA_IS_VIDEO_CALL, false);
            if (isVideoCall) {
                newIntent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, isVideoCall);
            }
        }
        long simId = origIntent.getLongExtra(Constants.EXTRA_ORIGINAL_SIM_ID, Settings.System.DEFAULT_SIM_NOT_SET);
        if (-1 != simId) {
            newIntent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, simId);
        }
        boolean isIpCall = origIntent.getBooleanExtra(Constants.EXTRA_IS_IP_DIAL, false);
        if (isIpCall) {
            newIntent.putExtra(Constants.EXTRA_IS_IP_DIAL, isIpCall);
        }
        boolean isFollowSimManagement = origIntent.getBooleanExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, false);
        if (isFollowSimManagement) {
            newIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, isFollowSimManagement);
        }
    }
    
    public static void setMMICommandToService(final String number) {
        if (mNwService != null) {
            try {
                mNwService.setMmiString(number);
                if (DBG) log("Extended NW bindService setUssdString (" + number + ")");
            } catch (RemoteException e) {
                mNwService = null;
            }
        }
    }
    
    public static String getNetworkOperatorName() {
        String operatorName = null;
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            GeminiPhone phone = (GeminiPhone) PhoneApp.getInstance().phone;
            if(phone.getStateGemini(Phone.GEMINI_SIM_2) != Phone.State.IDLE) {
                operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2);
            } else if(phone.getStateGemini(Phone.GEMINI_SIM_1) != Phone.State.IDLE) {
                operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
            }
            //Give a chance for get mmi information
            if (operatorName == null && PhoneApp.getInstance().mCM.getState() == Phone.State.IDLE)
            {
                if (phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_1).size() != 0)
                {
                    operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
                }else if (phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_2).size() != 0)
                {
                    operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2);
                }
            }
        } else {
            operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
        }
        if(DBG) log("operatorName = "+operatorName);
        return operatorName;
    }
    
    public static SIMInfo getActiveSimInfo() {
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            GeminiPhone phone = (GeminiPhone) PhoneApp.getInstance().phone;
            int slot = -1;
            
            if(phone.getStateGemini(Phone.GEMINI_SIM_2) != Phone.State.IDLE) {
                slot = Phone.GEMINI_SIM_2;
            } else if(phone.getStateGemini(Phone.GEMINI_SIM_1) != Phone.State.IDLE) {
                slot = Phone.GEMINI_SIM_1;
            }
            
            if (slot == -1) {
                if (phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_1).size() != 0) {
                    slot = Phone.GEMINI_SIM_1;
                }else if (phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_2).size() != 0) {
                    slot = Phone.GEMINI_SIM_2;
                }
                if(DBG) log("updateSimIndicator, running mmi, slot = "+slot);
                return null;
            } else {
                SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoBySlot(slot);
                if(simInfo != null) {
                    if(DBG) log("updateSimIndicator slot = "+slot+" simInfo :");
                    if(DBG) log("displayName = " + simInfo.mDisplayName);
                    if(DBG) log("color       = " + simInfo.mColor);
                }
                return simInfo;
            }
        } else {
            // For single sim card case, no need get sim info
            return null;
        }
    }
    
    /**
     * Returns the special card title used in emergency callback mode (ECM),
     * which shows your own phone number.
     */
    public static String getECMCardTitle(Context context, Phone phone) {
        String rawNumber = phone.getLine1Number();  // may be null or empty
        String formattedNumber;
        if (!TextUtils.isEmpty(rawNumber)) {
            formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
        } else {
            formattedNumber = context.getString(R.string.unknown);
        }
        String titleFormat = context.getString(R.string.card_title_my_phone_number);
        return String.format(titleFormat, formattedNumber);
    }
    
    public static long getDiskAvailableSize() {
        StorageManager storageManager = (StorageManager) PhoneApp.getInstance().getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            log("-----story manager is null----");
            return -1;
        }
        File sdCardDirectory = new File(storageManager.getDefaultPath());
        StatFs statfs;
        try {
            if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                statfs = new StatFs(sdCardDirectory.getPath());
            } else {
                log("-----diskSpaceAvailable: sdCardDirectory is null----");
                return -1;
            }
        } catch (IllegalArgumentException e) {
            log("-----diskSpaceAvailable: IllegalArgumentException----");
            return -1;
        }
        long blockSize = statfs.getBlockSize();
        long availBlocks = statfs.getAvailableBlocks();
        long totalSize = blockSize * availBlocks;
        return totalSize;
    }

    // The unit of input parameter is BYTE
    public static boolean diskSpaceAvailable(long sizeAvailable) {
        return (getDiskAvailableSize() - sizeAvailable) > 0;
    }

    public static boolean isExternalStorageMounted() {
        StorageManager storageManager = (StorageManager) PhoneApp.getInstance().getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            log("-----story manager is null----");
            return false;
        }
        String storageState = storageManager.getVolumeState(storageManager.getDefaultPath());
        return storageState.equals(Environment.MEDIA_MOUNTED) ? true : false;
    }
    
    static void displayMmiDialog(Context context, CharSequence text, int type, int slot) {
        Intent intent = new Intent();
        intent.setClass(context, com.android.phone.UssdAlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(UssdAlertActivity.USSD_MESSAGE_EXTRA, text);
        intent.putExtra(UssdAlertActivity.USSD_TYPE_EXTRA, type);
        intent.putExtra(UssdAlertActivity.USSD_SLOT_ID, slot);
        context.startActivity(intent);
    }
    
    static void cancelUssdDialog() {
        if (curCode != null && curCode.isCancelable()) {
            curCode.cancel();
        }
    }

    //This is ugly, but we have no choice because of SipPhone doesn't implement the method
    public static void hangupAllCalls() {
        CallManager cm = PhoneApp.getInstance().mCM;
        List<Phone> phones = cm.getAllPhones();
        try {
            for (Phone phone : phones) {
                Call fg = phone.getForegroundCall();
                Call bg = phone.getBackgroundCall();
                if (phone.getState() != Phone.State.IDLE) {
                    if (!(phone instanceof SipPhone)) {
                        log(phone.toString() + "   " + phone.getClass().toString());
                        if ((fg != null && fg.getState().isAlive()) && (bg != null && bg.getState().isAlive())) {
                            phone.hangupAll();
                        } else if (fg != null && fg.getState().isAlive()) {
                            fg.hangup();
                        } else if (bg != null && bg.getState().isAlive()) {
                            bg.hangup();
                        }
                    } else {
                        log(phone.toString() + "   " + phone.getClass().toString());
                        if (fg != null && fg.getState().isAlive()) {
                            fg.hangup();
                        }
                        if (bg != null && bg.getState().isAlive()) {
                            bg.hangup();
                        }
                    }
                } else {
                    log("Phone is idle  " + phone.toString() + "   " + phone.getClass().toString());
                }
            }
        } catch (Exception e) {
            log(e.toString());
        }
    }
    
    public static SIMInfo getVTUsedSimInfo() {
        final int slot = FeatureOption.MTK_GEMINI_3G_SWITCH ? PhoneApp.getInstance().phoneMgr.get3GCapabilitySIM() : 0;
        if (slot == -1) {
            return null;
        }
        SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoBySlot(slot);
        if(simInfo != null) {
            if(DBG) log("updateSimIndicator slot = "+slot+" simInfo :");
            if(DBG) log("displayName = " + simInfo.mDisplayName);
            if(DBG) log("color       = " + simInfo.mColor);
        }
        return simInfo;
    }
    
    public static String getVTNetworkOperatorName() {
        String operatorName = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            final int slot = FeatureOption.MTK_GEMINI_3G_SWITCH ? PhoneApp.getInstance().phoneMgr.get3GCapabilitySIM() : 0;
            if (slot == -1) {
                return null;
            }
            GeminiPhone phone = (GeminiPhone) PhoneApp.getInstance().phone;
            if (Phone.GEMINI_SIM_2 == slot) {
                operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA_2);
            } else if (Phone.GEMINI_SIM_1 == slot) {
                operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
            }
        } else {
            operatorName = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
        }
        if(DBG) log("operatorName = "+operatorName);
        return operatorName;
    }
    
    public static void hangupForDualTalk(Call call) {
        Phone phone = call.getPhone();
//        if (phone instanceof SipPhone) {
//            call.hangup();
//        } else {
//            
//        }
        try {
            call.hangup();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
        
    }

    /* Orange customization begin */
    public static boolean isOrangeSupport() {
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && "OP03".equals(optr)) {
            return true;
        }
        return false;
    }
    /* Orange customization end */

    
    public static boolean hasMultipleConnections(Call call) {
        if (call == null) {
            return false;
        }
        
        return call.getConnections().size() > 1;
    }

    public static int getDefaultStorageType(){
        StorageManager storageManager =
            (StorageManager) PhoneApp.getInstance().getSystemService(Context.STORAGE_SERVICE);
        //get the provided path list
        if (null == storageManager) {
            return -1;
        }
        String defaultStoragePath = storageManager.getDefaultPath();
        StorageVolume[] Volumes = storageManager.getVolumeList();
        if (null == Volumes) {
            return -1;
        }
        if (!storageManager.getVolumeState(defaultStoragePath).equals(Environment.MEDIA_MOUNTED)) {
            return -1;
        }
        log("Volumes.length:" + Volumes.length);
        for (int i = 0; i < Volumes.length; ++i) {
            if (Volumes[i].getPath().equals(defaultStoragePath)) {
                if (Volumes[i].isRemovable()) {
                    return Constants.STORAGE_TYPE_SD_CARD;
                } else {
                    return Constants.STORAGE_TYPE_PHONE_STORAGE;
                }
            }
        }
        return -1;
    }

    public static int getMountedStorageCount(){
        StorageManager storageManager =
            (StorageManager) PhoneApp.getInstance().getSystemService(Context.STORAGE_SERVICE);
        //get the provided path list
        if (null == storageManager) {
            return 0;
        }
        String defaultStoragePath = storageManager.getDefaultPath();
        StorageVolume[] Volumes = storageManager.getVolumeList();
        if (null == Volumes) {
            return 0;
        }
        log("Volumes.length:" + Volumes.length);
        int count = 0;
        for (int i = 0; i < Volumes.length; ++i) {
            if (storageManager.getVolumeState(Volumes[i].getPath()).equals(Environment.MEDIA_MOUNTED)) {
                ++count;
            }
        }
        log("Volumes count:" + count);
        return count;
    }

    public static boolean getShouldSendToVoiceMailFlag(final Connection c) {
        log("getShouldSendToVoiceMailFlag()");
        if (null == c) {
            log("getShouldSendToVoiceMailFlag(), connection is null");
            return false;
        }
        Object userDataObject = c.getUserData();
        if (userDataObject instanceof CallerInfo) {
            CallerInfo callerInfo = (CallerInfo) userDataObject;
            log("instanceof CallerInfo, flag is " + callerInfo.shouldSendToVoicemail);
            return callerInfo.shouldSendToVoicemail;
        }
        return false;
    }

    /*
     * New Feature by Mediatek Begin.
     * M:AAS
     */
    public static final String FEATURE_AAS = "OP03";
    public static final String ACCOUNT_TYPE_USIM = "USIM Account";

    /**
     * check whether "OP03" is enabled.
     * @return return true if "OP03" enabled, otherwise return false.
     */
    public static boolean isOp03Enabled() {
        return FEATURE_AAS.equals(SystemProperties.get("ro.operator.optr"));
    }

    /**
     * check whether "AAS" feature is enabled.
     * @param accountType the contact's account type, it seems like "USIM Account".
     * @return
     */
    public static boolean isAasEnabled(String accountType) {
        if (isOp03Enabled() && ACCOUNT_TYPE_USIM.equals(accountType)) {
            return true;
        }
        return false;
    }

    public static String getAasLabel(CallerInfo info) {
        String label = "";
        if (info.accountType != null && isAasEnabled(info.accountType)) {
            try {
                final int slotId = SIMInfoWrapper.getDefault().getSimSlotById(info.indicate);
                final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                if (iIccPhb != null) {
                    label = iIccPhb.getUSIMAASById(info.numberType);
                }
            } catch (Exception e) {
                log("****Exception in getAasLabel():" + e.getMessage());
            }
        }
        log("  ==> getAasLabel() label=" + label);
        return label;
    }

    private static IIccPhoneBook getIIccPhoneBook(int slotId) {
        String serviceName;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            serviceName = (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_2) ? "simphonebook2"
                    : "simphonebook";
        } else {
            serviceName = "simphonebook";
        }
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager
                .getService(serviceName));
        return iIccPhb;
    }

    public static int getSlotByPhoneType(int phontType) {
        int slot = Phone.GEMINI_SIM_1;
        TelephonyManager telephony = TelephonyManager.getDefault();
        if (phontType == Phone.PHONE_TYPE_CDMA) {
            if (telephony.getPhoneTypeGemini(Phone.GEMINI_SIM_1) == Phone.PHONE_TYPE_CDMA) {
                slot = Phone.GEMINI_SIM_1;
            } else if (telephony.getPhoneTypeGemini(Phone.GEMINI_SIM_2) == Phone.PHONE_TYPE_CDMA) {
                slot = Phone.GEMINI_SIM_2;
            }
        } else if (phontType == Phone.PHONE_TYPE_GSM) {
            if (telephony.getPhoneTypeGemini(Phone.GEMINI_SIM_1) == Phone.PHONE_TYPE_GSM) {
                slot = Phone.GEMINI_SIM_1;
            } else if (telephony.getPhoneTypeGemini(Phone.GEMINI_SIM_2) == Phone.PHONE_TYPE_GSM) {
                slot = Phone.GEMINI_SIM_2;
            }
        }
        log("getSlotByPhoneType with phontType = " + phontType + " and return slot = " + slot);
        return slot;
    }
    
    /**
     * In some case, the all phones are IDLE, but we need the call information to update the 
     * CallCard for DISCONNECTED or DISCONNECTING status
     * @param call
     * @return
     */
    public static SIMInfo getSimInfoByCall(Call call) {
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            if (call == null || call.getPhone() == null) {
                return null;
            }
        
            Phone phone = call.getPhone();
            //This an temp solution for VIA, the cdma is always in slot 2.
            if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                return SIMInfoWrapper.getDefault().getSimInfoBySlot(PhoneUtils.getSlotByPhoneType(Phone.PHONE_TYPE_CDMA));
            }
        
            String serialNumber = phone.getIccSerialNumber();
            SIMInfo info = SIMInfoWrapper.getDefault().getSimInfoBySlot(Phone.GEMINI_SIM_1);
            if (info != null && (info.mICCId != null) && (info.mICCId.equals(serialNumber))) {
                return info;
            }
        
            SIMInfo info2 = SIMInfoWrapper.getDefault().getSimInfoBySlot(Phone.GEMINI_SIM_2);
            if (info2 != null && (info2.mICCId != null) && (info2.mICCId.equals(serialNumber))) {
                return info2;
            }
        
            return null;
        } else { 
            return null;
        }
    }
    
    /*
     * New Feature by Mediatek End.
     */
}
