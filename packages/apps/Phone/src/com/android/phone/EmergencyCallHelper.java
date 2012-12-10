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

package com.android.phone;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.phone.Constants.CallStatusCode;
import com.android.phone.InCallUiState.ProgressIndicationType;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.provider.Telephony.SIMInfo;
import android.util.Log;

import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.MTKCallManager;
import com.android.phone.PhoneFeatureConstants.FeatureOption;

import java.util.List;
import android.os.SystemProperties;

/**
 * Helper class for the {@link CallController} that implements special
 * behavior related to emergency calls.  Specifically, this class handles
 * the case of the user trying to dial an emergency number while the radio
 * is off (i.e. the device is in airplane mode), by forcibly turning the
 * radio back on, waiting for it to come up, and then retrying the
 * emergency call.
 *
 * This class is instantiated lazily (the first time the user attempts to
 * make an emergency call from airplane mode) by the the
 * {@link CallController} singleton.
 */
public class EmergencyCallHelper extends Handler {
    private static final String TAG = "EmergencyCallHelper";
    private static final boolean DBG = true;

    // Number of times to retry the call, and time between retry attempts.
    // MTK old code set MAX_NUM_RETRIES as 0, but does not know reason,
    // Here just keep google default count 6
    public static final int MAX_NUM_RETRIES = 6;
    public static final long TIME_BETWEEN_RETRIES = 5000;  // msec

    // Timeout used with our wake lock (just as a safety valve to make
    // sure we don't hold it forever).
    public static final long WAKE_LOCK_TIMEOUT = 5 * 60 * 1000;  // 5 minutes in msec

    // !!! Below is from MTK code, need check whether needed
    //private static boolean dialing_ecc = false;

    // Handler message codes; see handleMessage()
    private static final int START_SEQUENCE = 1;
    private static final int SERVICE_STATE_CHANGED = 2;
    private static final int DISCONNECT = 3;
    private static final int RETRY_TIMEOUT = 4;
    private static final int SERVICE_STATE_CHANGED2 = 5;

    private CallController mCallController;
    private PhoneApp mApp;
    private CallManager mCM;
    private MTKCallManager mCMGemini;
    private Phone mPhone;
    private String mNumber;  // The emergency number we're trying to dial
    private int mNumRetriesSoFar;
    
    private int mSlot = -1;
    
    private static final String[] CDMA_ECC = {"110", "119", "120", "122"};

    // Wake lock we hold while running the whole sequence
    private PowerManager.WakeLock mPartialWakeLock;

    public EmergencyCallHelper(CallController callController) {
        if (DBG) log("EmergencyCallHelper constructor...");
        mCallController = callController;
        mApp = PhoneApp.getInstance();
        mCM =  mApp.mCM;
        mCMGemini = mApp.mCMGemini;
        mPhone = mApp.phone;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case START_SEQUENCE:
                startSequenceInternal(msg);
                break;
            case SERVICE_STATE_CHANGED:
            case SERVICE_STATE_CHANGED2:
                onServiceStateChanged(msg);
                break;
            case DISCONNECT:
                onDisconnect(msg);
                break;
            case RETRY_TIMEOUT:
                onRetryTimeout(msg);
                break;
            default:
                Log.wtf(TAG, "handleMessage: unexpected message: " + msg);
                break;
        }
    }

    /**
     * Starts the "emergency call from airplane mode" sequence.
     *
     * This is the (single) external API of the EmergencyCallHelper class.
     * This method is called from the CallController placeCall() sequence
     * if the user dials a valid emergency number, but the radio is
     * powered-off (presumably due to airplane mode.)
     *
     * This method kicks off the following sequence:
     * - Power on the radio
     * - Listen for the service state change event telling us the radio has come up
     * - Then launch the emergency call
     * - Retry if the call fails with an OUT_OF_SERVICE error
     * - Retry if we've gone 5 seconds without any response from the radio
     * - Finally, clean up any leftover state (progress UI, wake locks, etc.)
     *
     * This method is safe to call from any thread, since it simply posts
     * a message to the EmergencyCallHelper's handler (thus ensuring that
     * the rest of the sequence is entirely serialized, and runs only on
     * the handler thread.)
     *
     * This method does *not* force the in-call UI to come up; our caller
     * is responsible for doing that (presumably by calling
     * PhoneApp.displayCallScreen().)
     */
    public void startEmergencyCallFromAirplaneModeSequence(String number) {
        if (DBG) log("startEmergencyCallFromAirplaneModeSequence('" + number + "')...");
        Message msg = obtainMessage(START_SEQUENCE, number);
        sendMessage(msg);
    }

    /**
     * Actual implementation of startEmergencyCallFromAirplaneModeSequence(),
     * guaranteed to run on the handler thread.
     * @see startEmergencyCallFromAirplaneModeSequence()
     */
    private void startSequenceInternal(Message msg) {
        if (DBG) log("startSequenceInternal(): msg = " + msg);

        // First of all, clean up any state (including mPartialWakeLock!)
        // left over from a prior emergency call sequence.
        // This ensures that we'll behave sanely if another
        // startEmergencyCallFromAirplaneModeSequence() comes in while
        // we're already in the middle of the sequence.
        cleanup();

        mNumber = (String) msg.obj;
        if (DBG) log("- startSequenceInternal: Got mNumber: '" + mNumber + "'");

        mNumRetriesSoFar = 0;

        // Reset mPhone to whatever the current default phone is right now.
        /**
         * change feature by mediatek .inc
         * original android code :
         * mPhone = mApp.mCM.getDefaultPhone();
         * description : use mApp.phone for gemini support
         */
        mPhone = mApp.phone;

        // Wake lock to make sure the processor doesn't go to sleep midway
        // through the emergency call sequence.
        PowerManager pm = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        // Acquire with a timeout, just to be sure we won't hold the wake
        // lock forever even if a logic bug (in this class) causes us to
        // somehow never call cleanup().
        if (DBG) log("- startSequenceInternal: acquiring wake lock");
        mPartialWakeLock.acquire(WAKE_LOCK_TIMEOUT);

        // No need to check the current service state here, since the only
        // reason the CallController would call this method in the first
        // place is if the radio is powered-off.
        //
        // So just go ahead and turn the radio on.

        powerOnRadio();  // We'll get an onServiceStateChanged() callback
                         // when the radio successfully comes up.

        // Next step: when the SERVICE_STATE_CHANGED event comes in,
        // we'll retry the call; see placeEmergencyCall();
        // But also, just in case, start a timer to make sure we'll retry
        // the call even if the SERVICE_STATE_CHANGED event never comes in
        // for some reason.
        startRetryTimer();

        // And finally, let the in-call UI know that we need to
        // display the "Turning on radio..." progress indication.
        mApp.inCallUiState.setProgressIndication(ProgressIndicationType.TURNING_ON_RADIO);

        // (Our caller is responsible for calling mApp.displayCallScreen().)
    }

    /**
     * Handles the SERVICE_STATE_CHANGED event.
     *
     * (Normally this event tells us that the radio has finally come
     * up.  In that case, it's now safe to actually place the
     * emergency call.)
     */
    private void onServiceStateChanged(Message msg) {
        ServiceState state = (ServiceState) ((AsyncResult) msg.obj).result;
        if (DBG) log("onServiceStateChanged()...  new state = " + state);

        // Possible service states:
        // - STATE_IN_SERVICE        // Normal operation
        // - STATE_OUT_OF_SERVICE    // Still searching for an operator to register to,
        //                           // or no radio signal
        // - STATE_EMERGENCY_ONLY    // Phone is locked; only emergency numbers are allowed
        // - STATE_POWER_OFF         // Radio is explicitly powered off (airplane mode)

        // Once we reach either STATE_IN_SERVICE or STATE_EMERGENCY_ONLY,
        // it's finally OK to place the emergency call.
        /**
         * change feature by mediatek .inc
         * original android code:
         * boolean okToCall = (state.getState() == ServiceState.STATE_IN_SERVICE)
                || (state.getState() == ServiceState.STATE_EMERGENCY_ONLY);
         * description : when there are no sim cards inserted, service state is out of service
         * so change the condition
         */
        int iSlotId = SERVICE_STATE_CHANGED == msg.what ? 
                Phone.GEMINI_SIM_1 : Phone.GEMINI_SIM_2;
        boolean okToCall = state.getState() != ServiceState.STATE_POWER_OFF;
        //only care the expected slot
        if (mSlot != -1) {
            if (mSlot != iSlotId) {
                okToCall = false;
            }
        }

        if (okToCall) {
            // Woo hoo!  It's OK to actually place the call.
            if (DBG) log("onServiceStateChanged: ok to call!");

            // Deregister for the service state change events.
            unregisterForServiceStateChanged();

            // Take down the "Turning on radio..." indication.
            mApp.inCallUiState.clearProgressIndication();
            
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int iSimId = SERVICE_STATE_CHANGED == msg.what ? 
                                 Phone.GEMINI_SIM_1 : Phone.GEMINI_SIM_2;
                if (mSlot != -1 && mSlot == iSimId) {
                    placeEmergencyCall(iSimId);
                }
            } else {
                placeEmergencyCall(-1);
            }

            // The in-call UI is probably still up at this point,
            // but make sure of that:
            mApp.displayCallScreen(true);
        } else {
            // The service state changed, but we're still not ready to call yet.
            // (This probably was the transition from STATE_POWER_OFF to
            // STATE_OUT_OF_SERVICE, which happens immediately after powering-on
            // the radio.)
            //
            // So just keep waiting; we'll probably get to either
            // STATE_IN_SERVICE or STATE_EMERGENCY_ONLY very shortly.
            // (Or even if that doesn't happen, we'll at least do another retry
            // when the RETRY_TIMEOUT event fires.)
            if (DBG) log("onServiceStateChanged: not ready to call yet, keep waiting...");
        }
    }

    /**
     * Handles a DISCONNECT event from the telephony layer.
     *
     * Even after we successfully place an emergency call (after powering
     * on the radio), it's still possible for the call to fail with the
     * disconnect cause OUT_OF_SERVICE.  If so, schedule a retry.
     */
    private void onDisconnect(Message msg) {
        Connection conn = (Connection) ((AsyncResult) msg.obj).result;
        Connection.DisconnectCause cause = conn.getDisconnectCause();
        if (DBG) log("onDisconnect: connection '" + conn
                     + "', addr '" + conn.getAddress() + "', cause = " + cause);

        if (cause == Connection.DisconnectCause.OUT_OF_SERVICE) {
            // Wait a bit more and try again (or just bail out totally if
            // we've had too many failures.)
            //if (DBG) log("- onDisconnect: OUT_OF_SERVICE, need to retry...");
            //scheduleRetryOrBailOut();
            //For fta case.
            cleanup();
        } else {
            // Any other disconnect cause means we're done.
            // Either the emergency call succeeded *and* ended normally,
            // or else there was some error that we can't retry.  In either
            // case, just clean up our internal state.)

            if (DBG) log("==> Disconnect event; clean up...");
            cleanup();

            // Nothing else to do here.  If the InCallScreen was visible,
            // it would have received this disconnect event too (so it'll
            // show the "Call ended" state and finish itself without any
            // help from us.)
        }
    }

    /**
     * Handles the retry timer expiring.
     */
    private void onRetryTimeout(Message msg) {
        Phone.State phoneState = Phone.State.IDLE;
        int serviceState;
        int slot = 0;

        phoneState = mCM.getState();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            slot = msg.arg1;
            serviceState = ((GeminiPhone)mPhone).getServiceStateGemini(slot).getState();
        } else {
            serviceState = mPhone.getServiceState().getState();
        }
        if (DBG) log("onRetryTimeout():  phone state " + phoneState
                     + ", service state " + serviceState
                     + ", mNumRetriesSoFar = " + mNumRetriesSoFar);

        // - If we're actually in a call, we've succeeded.
        //
        // - Otherwise, if the radio is now on, that means we successfully got
        //   out of airplane mode but somehow didn't get the service state
        //   change event.  In that case, try to place the call.
        //
        // - If the radio is still powered off, try powering it on again.

        if (phoneState == Phone.State.OFFHOOK) {
            if (DBG) log("- onRetryTimeout: Call is active!  Cleaning up...");
            cleanup();
            return;
        }

        if (serviceState != ServiceState.STATE_POWER_OFF) {
            // Woo hoo -- we successfully got out of airplane mode.

            // Deregister for the service state change events; we don't need
            // these any more now that the radio is powered-on.
            unregisterForServiceStateChanged();

            // Take down the "Turning on radio..." indication.
            mApp.inCallUiState.clearProgressIndication();

           if (FeatureOption.MTK_GEMINI_SUPPORT) {
               placeEmergencyCall(slot);
           } else
               placeEmergencyCall(-1);  // If the call fails, placeEmergencyCall()
                                        // will schedule a retry.
        } else {
            // Uh oh; we've waited the full TIME_BETWEEN_RETRIES and the
            // radio is still not powered-on.  Try again...

            if (DBG) log("- Trying (again) to turn on the radio...");
            powerOnRadio();  // Again, we'll (hopefully) get an onServiceStateChanged()
                             // callback when the radio successfully comes up.

            // ...and also set a fresh retry timer (or just bail out
            // totally if we've had too many failures.)
            scheduleRetryOrBailOut();
        }

        // Finally, the in-call UI is probably still up at this point,
        // but make sure of that:
        mApp.displayCallScreen(true);
    }

    /**
     * Attempt to power on the radio (i.e. take the device out
     * of airplane mode.)
     *
     * Additionally, start listening for service state changes;
     * we'll eventually get an onServiceStateChanged() callback
     * when the radio successfully comes up.
     */
    private void powerOnRadio() {
        if (DBG)
            log("- powerOnRadio()...");

        // We're about to turn on the radio, so arrange to be notified
        // when the sequence is complete.
        registerForServiceStateChanged();

        // If airplane mode is on, we turn it off the same way that the
        // Settings activity turns it off.
        int dualSimMode = 0;
        boolean bOffAirplaneMode = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT)
            dualSimMode = Settings.System.getInt(mApp.getContentResolver(),
                    Settings.System.DUAL_SIM_MODE_SETTING,
                    Settings.System.DUAL_SIM_MODE_SETTING_DEFAULT);

        if (DBG) Log.d(TAG, "dualSimMode = " + dualSimMode);
        if (Settings.System.getInt(mApp.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) > 0) {
            if (DBG) log("==> Turning off airplane mode...");

            // Change the system setting
            Settings.System.putInt(mApp.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);

            // Post the intent
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", false);
            mApp.sendBroadcast(intent);
            bOffAirplaneMode = true;
        } else if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            // Otherwise, for some strange reason the radio is off
            // (even though the Settings database doesn't think we're
            // in airplane mode.) In this case just turn the radio
            // back on.
            if (DBG) log("==> (Apparently) not in airplane mode; manually powering radio on...");
            mPhone.setRadioPower(true);
        }
        
        if (mSlot != -1) {
            int newmode = this.getProperDualSimMode(mSlot, dualSimMode);
            Settings.System.putInt(mApp.getContentResolver(),
                    Settings.System.DUAL_SIM_MODE_SETTING, newmode);
            final Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
            intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, newmode);
            mApp.sendBroadcast(intent);
            log("power on radio with mSlot = " + mSlot);
        } else if (FeatureOption.MTK_GEMINI_SUPPORT
                && (!bOffAirplaneMode || (bOffAirplaneMode && needSetDualSimMode(dualSimMode)))) {
            int mode = getProperDualSimMode();
            if (DBG) log("powering on radio with dualsim mode = " + mode + " ...");
            Settings.System.putInt(mApp.getContentResolver(),
                    Settings.System.DUAL_SIM_MODE_SETTING, mode);
            final Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
            intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, mode);
            mApp.sendBroadcast(intent);
        }
    }

    /**
     * Actually initiate the outgoing emergency call.
     * (We do this once the radio has successfully been powered-up.)
     *
     * If the call succeeds, we're done.
     * If the call fails, schedule a retry of the whole sequence.
     */
    private void placeEmergencyCall(int simId) {
        if (DBG) log("placeEmergencyCall()...");

        // Place an outgoing call to mNumber.
        // Note we call PhoneUtils.placeCall() directly; we don't want any
        // of the behavior from CallController.placeCallInternal() here.
        // (Specifically, we don't want to start the "emergency call from
        // airplane mode" sequence from the beginning again!)

        registerForDisconnect();  // Get notified when this call disconnects

        if (DBG) log("- placing call to '" + mNumber + "'..." + " simId = " + simId);
        int callStatus;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            callStatus = PhoneUtils.placeCallGemini(mApp,
                                                    mPhone,
                                                    mNumber,
                                                    null,  // contactUri
                                                    true,  // isEmergencyCall
                                                    null,
                                                    simId);  // gatewayUri
            if (DBG) log("- PhoneUtils.placeCallGemini() returned status = " + callStatus);
        } else {
            callStatus = PhoneUtils.placeCall(mApp,
                                              mPhone,
                                              mNumber,
                                              null,  // contactUri
                                              true,  // isEmergencyCall
                                              null);  // gatewayUri
            if (DBG) log("- PhoneUtils.placeCall() returned status = " + callStatus);
        }

        boolean success;
        // Note PhoneUtils.placeCall() returns one of the CALL_STATUS_*
        // constants, not a CallStatusCode enum value.
        switch (callStatus) {
            case Constants.CALL_STATUS_DIALED:
                success = true;
                break;

            case Constants.CALL_STATUS_DIALED_MMI:
            case Constants.CALL_STATUS_FAILED:
            default:
                // Anything else is a failure, and we'll need to retry.
                Log.w(TAG, "placeEmergencyCall(): placeCall() failed: callStatus = " + callStatus);
                success = false;
                break;
        }

        if (success) {
            if (DBG) log("==> Success from PhoneUtils.placeCall()!");
            // Ok, the emergency call is (hopefully) under way.

            // We're not done yet, though, so don't call cleanup() here.
            // (It's still possible that this call will fail, and disconnect
            // with cause==OUT_OF_SERVICE.  If so, that will trigger a retry
            // from the onDisconnect() method.)
        } else {
            if (DBG) log("==> Failure.");
            // Wait a bit more and try again (or just bail out totally if
            // we've had too many failures.)
            scheduleRetryOrBailOut();
        }
    }

    /**
     * Schedules a retry in response to some failure (either the radio
     * failing to power on, or a failure when trying to place the call.)
     * Or, if we've hit the retry limit, bail out of this whole sequence
     * and display a failure message to the user.
     */
    private void scheduleRetryOrBailOut() {
        mNumRetriesSoFar++;
        if (DBG) log("scheduleRetryOrBailOut()...  mNumRetriesSoFar is now " + mNumRetriesSoFar);

        if (mNumRetriesSoFar > MAX_NUM_RETRIES) {
            Log.w(TAG, "scheduleRetryOrBailOut: hit MAX_NUM_RETRIES; giving up...");
            cleanup();
            // ...and have the InCallScreen display a generic failure
            // message.
            mApp.inCallUiState.setPendingCallStatusCode(CallStatusCode.CALL_FAILED);
        } else {
            if (DBG) log("- Scheduling another retry...");
            startRetryTimer();
            mApp.inCallUiState.setProgressIndication(ProgressIndicationType.RETRYING);
        }
    }

    /**
     * Clean up when done with the whole sequence: either after
     * successfully placing *and* ending the emergency call, or after
     * bailing out because of too many failures.
     *
     * The exact cleanup steps are:
     * - Take down any progress UI (and also ask the in-call UI to refresh itself,
     *   if it's still visible)
     * - Double-check that we're not still registered for any telephony events
     * - Clean up any extraneous handler messages (like retry timeouts) still in the queue
     * - Make sure we're not still holding any wake locks
     *
     * Basically this method guarantees that there will be no more
     * activity from the EmergencyCallHelper until the CallController
     * kicks off the whole sequence again with another call to
     * startEmergencyCallFromAirplaneModeSequence().
     *
     * Note we don't call this method simply after a successful call to
     * placeCall(), since it's still possible the call will disconnect
     * very quickly with an OUT_OF_SERVICE error.
     */
    private void cleanup() {
        if (DBG) log("cleanup()...");

        // Take down the "Turning on radio..." indication.
        mApp.inCallUiState.clearProgressIndication();

        unregisterForServiceStateChanged();
        unregisterForDisconnect();
        cancelRetryTimer();

        // Release / clean up the wake lock
        if (mPartialWakeLock != null) {
            if (mPartialWakeLock.isHeld()) {
                if (DBG) log("- releasing wake lock");
                mPartialWakeLock.release();
            }
            mPartialWakeLock = null;
        }

        // And finally, ask the in-call UI to refresh itself (to clean up the
        // progress indication if necessary), if it's currently visible.
        mApp.updateInCallScreen();
    }

    private void startRetryTimer() {
        removeMessages(RETRY_TIMEOUT);
        /**
         * change feature by mediatek .inc
         * description : add slot id to RETRY_TIMEOUT message for gemini
         * original android code:
         sendEmptyMessageDelayed(RETRY_TIMEOUT, TIME_BETWEEN_RETRIES);
         */
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            int dualSimMode = 0;
            dualSimMode = Settings.System.getInt(mApp.getContentResolver(),
                    Settings.System.DUAL_SIM_MODE_SETTING,
                    Settings.System.DUAL_SIM_MODE_SETTING_DEFAULT);
            int slot = Phone.GEMINI_SIM_1;
            if (mSlot != -1) {
                slot = mSlot;
            } else if (needSetDualSimMode(dualSimMode)) {
                slot = getProperDualSimMode() == 1 ? Phone.GEMINI_SIM_1 : Phone.GEMINI_SIM_2;
            } else if (dualSimMode == 2) {
                slot = Phone.GEMINI_SIM_2;
            }
            
            Message msg = obtainMessage(RETRY_TIMEOUT, slot, 0);
            sendMessageDelayed(msg, TIME_BETWEEN_RETRIES);
            if(DBG) log("startRetryTimer, slot = " + slot);
        } else
            sendEmptyMessageDelayed(RETRY_TIMEOUT, TIME_BETWEEN_RETRIES);
        /**
         * change feature by mediatek .inc
         */
    }

    private void cancelRetryTimer() {
        removeMessages(RETRY_TIMEOUT);
    }

    private void registerForServiceStateChanged() {
        // Unregister first, just to make sure we never register ourselves
        // twice.  (We need this because Phone.registerForServiceStateChanged()
        // does not prevent multiple registration of the same handler.)
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            ((GeminiPhone)mPhone).unregisterForServiceStateChangedGemini(this, Phone.GEMINI_SIM_1);
            ((GeminiPhone)mPhone).unregisterForServiceStateChangedGemini(this, Phone.GEMINI_SIM_2);
            // !!!! Need to check whether registerForServiceStateChangedGemini() function has EmergencyCallInfo as input parameter
            ((GeminiPhone)mPhone).registerForServiceStateChangedGemini(this, SERVICE_STATE_CHANGED, null, Phone.GEMINI_SIM_1);
            ((GeminiPhone)mPhone).registerForServiceStateChangedGemini(this, SERVICE_STATE_CHANGED2, null, Phone.GEMINI_SIM_2);
        } else {
            mPhone.unregisterForServiceStateChanged(this);  // Safe even if not currently registered
            mPhone.registerForServiceStateChanged(this, SERVICE_STATE_CHANGED, null);
        }
    }

    private void unregisterForServiceStateChanged() {
        // This method is safe to call even if we haven't set mPhone yet.
        if (mPhone != null) {
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                ((GeminiPhone)mPhone).unregisterForServiceStateChangedGemini(this, Phone.GEMINI_SIM_1);
                ((GeminiPhone)mPhone).unregisterForServiceStateChangedGemini(this, Phone.GEMINI_SIM_2);
                // Clean up any pending messages too
                removeMessages(SERVICE_STATE_CHANGED);
                removeMessages(SERVICE_STATE_CHANGED2);
            } else {
                mPhone.unregisterForServiceStateChanged(this);  // Safe even if unnecessary
                removeMessages(SERVICE_STATE_CHANGED);  // Clean up any pending messages too
            }
        }
    }

    private void registerForDisconnect() {
        // Note: no need to unregister first, since
        // CallManager.registerForDisconnect() automatically prevents
        // multiple registration of the same handler.
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mCMGemini.registerForDisconnectGemini(this, DISCONNECT, null, Phone.GEMINI_SIM_1);
            mCMGemini.registerForDisconnectGemini(this, DISCONNECT, null, Phone.GEMINI_SIM_2);
        } else {
            mCM.registerForDisconnect(this, DISCONNECT, null);
        }
    }

    private void unregisterForDisconnect() {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mCMGemini.unregisterForDisconnectGemini(this, Phone.GEMINI_SIM_1);
            mCMGemini.unregisterForDisconnectGemini(this, Phone.GEMINI_SIM_2);
            // Clean up any pending messages too
            removeMessages(SERVICE_STATE_CHANGED);
        } else {
            mCM.unregisterForDisconnect(this);  // Safe even if not currently registered
            removeMessages(DISCONNECT);  // Clean up any pending messages too
        }
    }

    static final int DUALSIM_OFF = 0;
    static final int SIM1_ONLY = 1;
    static final int SIM2_ONLY = 2;
    static final int DUALSIM_ON = 3;
    
    private boolean needSetDualSimMode(int lastMode) {
        
        List<SIMInfo> list = SIMInfo.getInsertedSIMList(mApp);
        if (list == null || list.size() == 0) {
            return true;
        }
        
        //dual radio off, off airplane mode not open radios
        if (DUALSIM_OFF == lastMode) {
            return true;
        }
        
        //dual radio on, even one sim will be ok
        if (DUALSIM_ON == lastMode) {
            return false;
        }
        
        for (SIMInfo info : list) {
            if (lastMode == info.mSlot + 1) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isCDMASpecialEcc(String number) {
        log("isCDMASpecialEcc number = " + number);
        boolean isEcc = false;
        if (!com.mediatek.featureoption.FeatureOption.EVDO_DT_SUPPORT) {
            log("isCDMASpecialEcc: not cdma platform");
            return isEcc;
        }
        
        int length = CDMA_ECC.length;
        for (int i = 0; i < length; i++) {
            log("isCDMASpecialEcc : " + CDMA_ECC[i]);
            if (CDMA_ECC[i].equals(number)) {
                isEcc = true;
                break;
            }
        }
        log("isCDMASpecialEcc: isEcc = " + isEcc);
        return isEcc;
    }
    
    private int getProperDualSimMode() {
        int mode = 1;
        List<SIMInfo> list = SIMInfo.getInsertedSIMList(mApp);
      
        if (list != null && list.size() == 1 && list.get(0).mSlot == Phone.GEMINI_SIM_2) {
            mode = 2;
        }
        
        //On C+G project, the default Radio is CDMA, so if no sim insert,
        //the GSM radio maybe not turn on
        if (list != null && list.size() == 0) {
            if (isCDMASpecialEcc(mNumber)) {
                log("getProperDualSimMode: match cdma ecc case.");
                mode = 2;
            }
        }
        if (DBG) {
            Log.d(TAG, "getProperDualSimMode = " + mode);
        }
        return mode;
    }
    
    /**
     * Add for dualtalk c+g project to get the proper dualsim mode according
     * to the ecc and needed slot.
     * @param slot
     * @param dualSimMode
     * @return
     */
    private int getProperDualSimMode(int slot, int dualSimMode) {
        int mode = 0;
        log("getProperDualSimMode slot = " + slot + " dualSimMode = " + dualSimMode);
        List<SIMInfo> list = SIMInfoWrapper.getDefault().getInsertedSimInfoList();
        if (list == null || list.size() == 0 /*|| list.size() == 1*/) {
            log("getProperDualSimMode (no or one sim) return mode  = " + (slot + 1));
            return slot + 1;
        }
        
        if (dualSimMode == DUALSIM_OFF) {
            //no sim on, so open the expected sim only.
            mode = slot + 1;
        } else if (dualSimMode == SIM1_ONLY) {
            if (slot + 1 == SIM1_ONLY) {
                mode = SIM1_ONLY;
            } else if (slot + 1 == SIM2_ONLY) {
                mode = DUALSIM_ON;
            }
        } else if (dualSimMode == SIM2_ONLY) {
            if (slot + 1 == SIM1_ONLY) {
                mode = DUALSIM_ON;
            } else if (slot + 1 == SIM2_ONLY) {
                mode = SIM2_ONLY;
            }
        } else if (dualSimMode == DUALSIM_ON) {
            mode = DUALSIM_ON;
        }
        log("getProperDualSimMode return mode = " + mode);
        return mode;
    }

    /**
     * Check if the slot is valid
     * @param slot the slot id
     * @return true or false
     */
    private boolean isValidSlot(int slot) {
        if (slot == Phone.GEMINI_SIM_1 || slot == Phone.GEMINI_SIM_2) {
            return true;
        } else {
            return false;
        }
    }
    
    public void startEmergencyCallExt(String number, int slot) {
        log("startEmergencyCallExt: number == " + number + "  slot == " + slot);
        //Because it is an object member, so reset it firstly
        mSlot = -1;
        
        if (slot < 0) {
            log("startEmergencyCallExt: slot error!");
            return;
        }
        
        mNumber = number;
//        EmergencyRuleHandler eccRuleHandler = new EmergencyRuleHandler(number);
//        mSlot = eccRuleHandler.getPreferedSlot();
        if (isValidSlot(slot)) {
            mSlot = slot;
            boolean isRadioOn = true;
            if (mPhone instanceof GeminiPhone) {
                GeminiPhone dualPhone = (GeminiPhone) mPhone;
                isRadioOn = dualPhone.isRadioOnGemini(mSlot);
                if (DBG) {
                    log("startEmergencyCallExt (dualsim) isRadioOn = " + isRadioOn);
                }
            } else {
                isRadioOn = mPhone.getServiceState().getState() != ServiceState.STATE_POWER_OFF;
                if (DBG) {
                    log("startEmergencyCallExt (single sim) isRadioOn = " + isRadioOn);
                }
            }
            
            if (isRadioOn) {
                placeEmergencyCall(mSlot);
                log("startEmergencyCallExt: place call directly.");
            } else {
                log("startEmergencyCallExt: place call after turn on radio.");
                Message msg = obtainMessage(START_SEQUENCE, number);
                sendMessage(msg);
            }
        }
    }
    //
    // Debugging
    //

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
