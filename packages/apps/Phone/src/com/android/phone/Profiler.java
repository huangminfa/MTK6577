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

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;

/**
 * Profiling utilities for the Phone app.
 */
public class Profiler {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;

    // Let the compiler optimize all this code out unless we're actively
    // doing profiling runs.
    // TODO: Instead of doing all these "if (PROFILE)" checks here, every
    // place that *calls* any of these methods should check the value of
    // Profiler.PROFILE first, so the method calls will get optimized out
    // too.
    private static final boolean PROFILE = false;

    static long sTimeCallScreenRequested;
    static long sTimeCallScreenOnCreate;
    static long sTimeCallScreenCreated;

    // TODO: Clean up any usage of these times.  (There's no "incoming call
    // panel" in the Phone UI any more; incoming calls just go straight to the
    // regular in-call UI.)
    static long sTimeIncomingCallPanelRequested;
    static long sTimeIncomingCallPanelOnCreate;
    static long sTimeIncomingCallPanelCreated;

    /** This class is never instantiated. */
    private Profiler() {
    }

    static void profileViewCreate(Window win, String tag) {
        if (false) {
            ViewParent p = (ViewParent) win.getDecorView();
            while (p instanceof View) {
                p = ((View) p).getParent();
            }
            //((ViewRoot)p).profile();
            //((ViewRoot)p).setProfileTag(tag);
        }
    }

    static void callScreenRequested() {
        if (PROFILE) {
            sTimeCallScreenRequested = SystemClock.uptimeMillis();
        }
    }

    static void callScreenOnCreate() {
        if (PROFILE) {
            sTimeCallScreenOnCreate = SystemClock.uptimeMillis();
        }
    }

    static void callScreenCreated() {
        if (PROFILE) {
            sTimeCallScreenCreated = SystemClock.uptimeMillis();
            dumpCallScreenStat();
        }
    }

    private static void dumpCallScreenStat() {
        if (PROFILE) {
            log(">>> call screen perf stats <<<");
            log(">>> request -> onCreate = " +
                    (sTimeCallScreenOnCreate - sTimeCallScreenRequested));
            log(">>> onCreate -> created = " +
                    (sTimeCallScreenCreated - sTimeCallScreenOnCreate));
        }
    }

    static void incomingCallPanelRequested() {
        if (PROFILE) {
            sTimeIncomingCallPanelRequested = SystemClock.uptimeMillis();
        }
    }

    static void incomingCallPanelOnCreate() {
        if (PROFILE) {
            sTimeIncomingCallPanelOnCreate = SystemClock.uptimeMillis();
        }
    }

    static void incomingCallPanelCreated() {
        if (PROFILE) {
            sTimeIncomingCallPanelCreated = SystemClock.uptimeMillis();
            dumpIncomingCallPanelStat();
        }
    }

    private static void dumpIncomingCallPanelStat() {
        if (PROFILE) {
            log(">>> incoming call panel perf stats <<<");
            log(">>> request -> onCreate = " +
                    (sTimeIncomingCallPanelOnCreate - sTimeIncomingCallPanelRequested));
            log(">>> onCreate -> created = " +
                    (sTimeIncomingCallPanelCreated - sTimeIncomingCallPanelOnCreate));
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, "[Profiler] " + msg);
    }

    /* below are added by mediatek .inc */
    private static final String PROFILER_TAG = "PhoneProfiler";
    private static final boolean enablePhoneProfiler = true;

    public static final String OutgoingCallBroadcasterEnterOnCreate = "+OutgoingCallBroadcaster.onCreate";
    public static final String OutgoingCallBroadcasterLeaveOnCreate = "-OutgoingCallBroadcaster.onCreate";

    public static final String IsLocalEmergencyNumberEnter = "+PhoneNumberUtils.isLocalEmergencyNumber";
    public static final String IsLocalEmergencyNumberExit = "-PhoneNumberUtils.isLocalEmergencyNumber";

    public static final String IsPotentialLocalEmergencyNumberEnter = "+PhoneNumberUtils.isPotentialLocalEmergencyNumber";
    public static final String IsPotentialLocalEmergencyNumberExit = "-PhoneNumberUtils.isPotentialLocalEmergencyNumber";

    public static final String OutgoingBroadcasterReceiverEnterOnReceive = "+OutgoingBroadcasterReceiver.onReceiver";
    public static final String OutgoingBroadcasterReceiverLeaveOnReceive = "-OutgoingBroadcasterReceiver.onReceiver";

    public static final String CallOptionHandlerEnterStartActivity = "+CallOptionHandler.StartActivity";
    public static final String CallOptionHandlerLeaveStartActivity = "-CallOptionHandler.StartActivity";
    public static final String CallOptionHandlerEnterOnClick = "+CallOptionHandler.onClick";
    public static final String CallOptionHandlerLeaveOnClick = "-CallOptionHandler.onClick";
    public static final String CallOptionHandlerEnterRun = "+CallOptionHandler.run";
    public static final String CallOptionHandlerLeaveRun = "-CallOptionHandler.run";

    public static final String CallOptionHelperEnterMakeVoiceCall = "+CallOptionHelper.makeVoiceCall";
    public static final String CallOptionHelperLeaveMakeVoiceCall = "-CallOptionHelper.makeVoiceCall";

    public static final String CallControllerEnterPlaceCallInternal = "+CallController.placeCallInternal";
    public static final String CallControllerLeavePlaceCallInternal = "-CallController.placeCallInternal";

    public static final String InCallScreenEnterOnCreate = "+InCallScreen.onCreate";
    public static final String InCallScreenLeaveOnCreate = "-InCallScreen.onCreate";

    public static final String InCallScreenEnterOnResume = "+InCallScreen.onResume";
    public static final String InCallScreenLeaveOnResume = "-InCallScreen.onResume";

    public static final String InCallScreenPostDraw = "InCallScreen.PostDraw";

    public static final String OutgoingCallReceiverEnterActionOnReceive = "+OutgoingCallReceiver.onReceive action";
    public static final String OutgoingCallReceiverLeaveActionOnReceive = "-OutgoingCallReceiver.onReceive action";
    public static final String OutgoingCallReceiverEnterBroadcastOnReceive = "+OutgoingCallReceiver.onReceive broadcast";
    public static final String OutgoingCallReceiverLeaveBroadcastOnReceive = "-OutgoingCallReceiver.onReceive broadcast";

    public static final String PhoneUtilsEnterPlaceCallGemini = "+PhoneUtils.placeCallGemini";
    public static final String PhoneUtilsLeavePlaceCallGemini = "-PhoneUtils.placeCallGemini";

    public static void trace(String msg) {
        if(enablePhoneProfiler)
            Log.d(PROFILER_TAG, msg);
    }
}
