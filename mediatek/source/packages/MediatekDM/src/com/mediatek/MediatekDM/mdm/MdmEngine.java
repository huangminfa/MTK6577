/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.MediatekDM.mdm;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import com.mediatek.MediatekDM.DmBootstrapHandler;
import com.mediatek.MediatekDM.mdm.MdmException.MdmError;
import com.mediatek.MediatekDM.mdm.NIAMsgHandler.UIMode;
import com.mediatek.MediatekDM.mdm.SessionStateObserver.SessionState;
import com.mediatek.MediatekDM.mdm.SessionStateObserver.SessionType;

import android.content.Context;
import android.util.Log;

public class MdmEngine {
    public static final String TAG = "MDM/MdmEngine";
    private PLLogger mLogger;
    private Context mContext;
    private MmiFactory mMmiFactory;
    private PLFactory mPLFactory;
    private PLStorage mPLStorage;
    private static MdmEngine mInstance = null;
    private MmiObserverImpl mMmiObserver;
    private MmiViewContext mMmiViewContext;

    static {
        System.loadLibrary("jni_mdm");
    }

    private List<SessionStateObserver> mSessionStateObservers = new LinkedList<SessionStateObserver>();
    private SessionInitiator mSessionInitiator;
    private NIAMsgHandler mNIAMsgHandler;

    public MdmEngine(Context context, MmiFactory mmiFactory, PLFactory plFactory) throws MdmException {
        this(context, mmiFactory, plFactory, new MdmDefaultLogger());
    }

    public MdmEngine(Context context, MmiFactory mmiFactory, PLFactory plFactory, PLLogger logger) throws MdmException {
        synchronized (MdmEngine.class) {
            /* Only one instance is allowed */
            Assert.assertEquals(null, mInstance);
            mContext = context;
            mMmiFactory = mmiFactory;
            mPLFactory = plFactory;
            mLogger = logger;
            mPLStorage = plFactory.getStorage();
            mMmiObserver = new MmiObserverImpl();
            /* The value of this object will be filled when MMI component are created. */
            mMmiViewContext = new MmiViewContext(null, 0, 0);

            mInstance = this;

            if (0 != _create()) {
                throw new MdmException(MdmError.INTERNAL);
            }
        }
    }

    private native int _create();

    public void destroy() {
        Log.d(TAG, "MdmEngine destroy");

        _destroy();
    }

    private native int _destroy();

    public void start() throws MdmException {
        Log.d(TAG, "MdmEngine start");

        if (0 != _start()) {
            throw new MdmException(MdmError.INTERNAL);
        }
    }

    private native int _start();

    /* TODO */
    public native boolean isIdle();

    private static class MdmDefaultLogger extends MdmLog implements PLLogger {

        @Override
        public void logMsg(MdmLogLevel level, String message) {
            switch (level) {
            case DEBUG:
                d(TAG, message);
                break;
            case ERROR:
                e(TAG, message);
                break;
            case INFO:
                i(TAG, message);
                break;
            case VERBOSE:
                v(TAG, message);
                break;
            case WARNING:
                w(TAG, message);
                break;
            }
        }
    }


    public void unregisterSessionStateObserver(SessionStateObserver observer) {
        mSessionStateObservers.remove(observer);
    }

    public void stop() {
        _stop();
    }

    private native int _stop();

    public void registerSessionStateObserver(SessionStateObserver observer) {
        mSessionStateObservers.add(observer);

    }

    private void notifySessionStateObservers(SessionType type, SessionState state, int lastError) {
        for (SessionStateObserver observer : mSessionStateObservers) {
            observer.notify(type, state, lastError, mSessionInitiator);
        }
    }

    public void setConnectionTimeout(int timeout) {
        // TODO Auto-generated method stub
        Log.d(TAG, "setConnectionTimeout: " + timeout);
        if (0 != _setConnectionTimeout(timeout)) {
            Log.e(TAG, "setConnectionTimeout failed");
        }
    }

    private native int _setConnectionTimeout(int timeout);

    public void setDefaultLogLevel(MdmLogLevel level) {
        // TODO Auto-generated method stub

    }

    public void notifyDLSessionProceed() throws MdmException {
        // TODO Auto-generated method stub

    }

    public void cancelSession() throws MdmException {
        // TODO Auto-generated method stub
        if (0 != _cancelSession()) {
            throw new MdmException(MdmError.INTERNAL);
        }

    }

    private native int _cancelSession();

    public void triggerNIADmSession(byte[] message,	SessionInitiator initiator, NIAMsgHandler handler) throws MdmException {
        Log.d(TAG, "triggerNIADmSession called");
        mSessionInitiator = initiator;
        mNIAMsgHandler = handler;
        if (0 != _triggerNIADmSession(message, initiator, handler)) {
            throw new MdmException(MdmError.INTERNAL);
        }
    }

    private native int _triggerNIADmSession(byte[] message,	SessionInitiator initiator, NIAMsgHandler handler);

    private void notifyNIAMsgHandler(UIMode uiMode, short dmVersion, byte[] vendorSpecificData) throws MdmException
    {
        mNIAMsgHandler.notify(uiMode, dmVersion, vendorSpecificData, mSessionInitiator);
    }

    public void triggerDMSession(String account, String genericAlertType, byte[] message, SessionInitiator initiator) throws MdmException {
        Log.d(TAG, "triggerDMSession called");
        mSessionInitiator = initiator;
        if (0 != _triggerDMSession(account, genericAlertType, message)) {
            throw new MdmException(MdmError.INTERNAL);
        }
    }

    private native int _triggerDMSession(String account, String genericAlertType, byte[] message);

    public void notifyNIASessionProceed() throws MdmException {
        // TODO Auto-generated method stub
        Log.d(TAG, "notifyNIASessionProceed called");
        if (0 != _notifyNIASessionProceed()) {
            throw new MdmException(MdmError.INTERNAL);
        }
    }

    private native int _notifyNIASessionProceed();

    public void triggerBootstrapSession(Object object, BootProfile profile,
                                        CpSecurity security, String mac, byte[] message,
                                        DmBootstrapHandler handler,
                                        DmBootstrapHandler handler2) throws MdmException {
        // TODO Auto-generated method stub

    }

    public void setComponentLogLevel(MdmComponent component, MdmLogLevel warning) {
        // TODO Auto-generated method stub

    }

    public void pauseSession() {
        // TODO
    }

    public void resumeSession() {
        // TODO
    }

    private static class MmiObserverImpl implements MmiObserver {

        /* keep these values sync with c code */
        public static final int OMC_MMI_UNDEFINED_SCREEN = 0;
        public static final int OMC_MMI_INITIAL_SCREEN = 1;
        public static final int OMC_MMI_AUTH_SCREEN = 2;
        public static final int OMC_MMI_AUTH_FAIL_SCREEN = 3;
        public static final int OMC_MMI_IN_SESSION_SCREEN = 4;
        public static final int OMC_MMI_SERVER_INFO_SCREEN = 5;
        public static final int OMC_MMI_CONTINUE_ABORT_SCREEN = 6;
        public static final int OMC_MMI_ENTER_DETAILS_SCREEN = 7;
        public static final int OMC_MMI_SINGLE_CHOICE_SCREEN = 8;
        public static final int OMC_MMI_MULTIPLE_CHOICE_SCREEN = 9;
        public static final int OMC_MMI_EXIT_FAIL_SCREEN = 10;
        public static final int OMC_MMI_EXIT_OK_SCREEN = 11;
        public static final int OMC_MMI_SYNC_FAIL_SCREEN = 12;

        public int mScreenType = OMC_MMI_UNDEFINED_SCREEN;

        @Override
        public native void notifyChoicelistSelection(int bitflags);

        @Override
        public native void notifyCancelEvent();

        @Override
        public native void notifyConfirmationResult(boolean confirmed);

        @Override
        public native void notifyInfoMsgClosed();

        @Override
        public native void notifyInputResult(String userInput);

        @Override
        public native void notifyTimeoutEvent();
    }
}

