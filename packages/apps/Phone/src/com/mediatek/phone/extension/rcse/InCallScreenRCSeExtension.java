/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.phone.extension.rcse;

import com.android.phone.InCallScreen;
import android.util.Log;
import com.android.phone.R;
import android.view.Menu;
import android.view.MenuItem;
import com.android.phone.*;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import com.mediatek.phone.extension.rcse.ICallScreenHost;
import com.mediatek.pluginmanager.Plugin;
import com.mediatek.pluginmanager.PluginManager;
import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
import com.mediatek.phone.extension.rcse.ICallScreenPlugIn;
import com.mediatek.phone.extension.InCallScreenExtension;

public class InCallScreenRCSeExtension extends InCallScreenExtension {

    private static final String LOG_TAG = "InCallScreenRCSeExtension";
    private static final boolean DBG = true;

    private InCallScreen mInCallScreen;
    private static CallManager mCM;

    private ShareFileCallScreenHost mShareFileHost;
    private ShareVideoCallScreenHost mShareVideoHost;

    private static ICallScreenPlugIn mShareFilePlugIn;
    private static ICallScreenPlugIn mShareVideoPlugIn;

    public InCallScreenRCSeExtension() {
    }

    public void onCreate(Bundle icicle, InCallScreen inCallScreen, CallManager cm) {
        if (DBG) log("onCreate(), icicle = " + icicle + ", InCallScreen = " + inCallScreen + ", cm = " + cm);
        mInCallScreen = inCallScreen;
        mCM = cm;
        mShareFileHost = new ShareFileCallScreenHost();
        mShareVideoHost = new ShareVideoCallScreenHost();
        // set host to plug-in
        if (null != mShareFilePlugIn) {
            mShareFilePlugIn.setCallScreenHost(mShareFileHost);
        }
        if (null != mShareVideoPlugIn) {
            mShareVideoPlugIn.setCallScreenHost(mShareVideoHost);
        }
    }

    public void onDestroy(InCallScreen inCallScreen) {
        if (DBG) log("onDestroy(), inCallScreen is " + inCallScreen);
        if (mInCallScreen == inCallScreen) {
            mInCallScreen = null;
        }
        if (mShareFileHost == mShareFilePlugIn.getCallScreenHost()) {
            mShareFilePlugIn.setCallScreenHost(null);
        }
        if (mShareVideoHost == mShareVideoPlugIn.getCallScreenHost()) {
            mShareVideoPlugIn.setCallScreenHost(null);
        }
    }

    public int getLayoutResID() {
        if (DBG) log("getLayoutResID(), id = " + R.layout.incall_screen_rcse);
        return R.layout.incall_screen_rcse;
    }

    public void setupMenuItems(Menu menu, InCallMenuState menuState,
                               InCallControlState inCallControlState) {

        if (DBG) log("setupMenuItems()");
        final MenuItem addMenu = menu.findItem(R.id.menu_add_call);
        final MenuItem holdMenu = menu.findItem(R.id.menu_hold_voice);

        final Call ringingCall = mCM.getFirstActiveRingingCall();

        if (RCSeUtils.canShare(mCM)) {
            if (DBG) log("setupMenuItems(), can share");
            if (isSharingVideo()) {
                if (DBG) log("setupMenuItems(), is sharing video");
                // share video
                if(ringingCall.getState() == Call.State.IDLE) {
                    for (int i=0; i < menu.size(); ++i) {
                        menu.getItem(i).setVisible(false);
                    }
                    holdMenu.setVisible(inCallControlState.canHold);
                    if (inCallControlState.canHold) {
                        if (inCallControlState.onHold) {
                            holdMenu.setTitle(R.string.incall_toast_unhold);
                        } else {
                            holdMenu.setTitle(R.string.incall_toast_hold);
                        }
                    }
                    if(!menuState.hasPermanentMenuKey) {
                        if (inCallControlState.canAddCall) {
                            if (addMenu != null) addMenu.setVisible(true);
                        }
                    }
                }
            } else {
                if (DBG) log("setupMenuItems(), not share video");
                holdMenu.setVisible(inCallControlState.canHold);
                if (inCallControlState.canHold) {
                    if (inCallControlState.onHold) {
                        holdMenu.setTitle(R.string.incall_toast_unhold);
                    } else {
                        holdMenu.setTitle(R.string.incall_toast_hold);
                    }
                }
            }
        }
    }

    public boolean handleOnScreenMenuItemClick(MenuItem menuItem) {
        switch(menuItem.getItemId()) {

            case R.id.menu_hold_voice:
                if (DBG) log("hold voice menu item is clicked");
                if ((isTransferingFile())) {
                    mShareFilePlugIn.stop();
                } else if (isSharingVideo()) {
                    mShareVideoPlugIn.stop();
                }
                onHoldMenuClick();
                break;
        }
        return false;
    }

    public boolean onPhoneStateChanged(CallManager cm) {
        if (DBG) log("onPhoneStateChanged(), cm = " + cm);
        if (RCSeUtils.canShareFromCallState(cm)) {
            String number = RCSeUtils.getRCSePhoneNumber(cm);
            if (null != number ) {
                if (null != mShareFilePlugIn) {
                    mShareFilePlugIn.registerForCapabilityChange(number);
                }
                if (null != mShareVideoPlugIn) {
                    mShareVideoPlugIn.registerForCapabilityChange(number);
                }
            }
        }
        if (RCSeUtils.shouldStop(cm)) {
            if ((isTransferingFile())) {
                mShareFilePlugIn.stop();
            } else if (isSharingVideo()) {
                mShareVideoPlugIn.stop();
            }
        }
        return false;
    }

    private void onHoldMenuClick() {
        if (DBG) log("onHoldMenuClick()");
        final boolean hasActiveCall = mCM.hasActiveFgCall();
        final boolean hasHoldingCall = mCM.hasActiveBgCall();
        log("onHoldMenuClick: hasActiveCall = " + hasActiveCall
                                                + ", hasHoldingCall = " + hasHoldingCall);
        boolean newHoldState;
        boolean holdButtonEnabled;
        if (hasActiveCall && !hasHoldingCall) {
            PhoneUtils.switchHoldingAndActive(mCM.getFirstActiveBgCall());
        } else if (!hasActiveCall && hasHoldingCall) {
            PhoneUtils.switchHoldingAndActive(mCM.getFirstActiveBgCall());
        } else {
            // Either zero or 2 lines are in use; "hold/unhold" is meaningless.
        }
    }

    public static boolean isSupport(Context context) {
        PluginManager<ICallScreenPlugIn> pm = PluginManager.<ICallScreenPlugIn>create(
                context, ICallScreenPlugIn.class.getName());
        try {
            if (pm.getPluginCount() > 0) {
                Plugin<ICallScreenPlugIn> plugIn = pm.getPlugin(0);
                if (null != plugIn) {
                    log("create share file and video plug in object");
                    mShareFilePlugIn = plugIn.createObject(Constants.META_DATA_NAME_TYPE_SHARE_FILE);
                    mShareVideoPlugIn = plugIn.createObject(Constants.META_DATA_NAME_TYPE_SHARE_VIDEO);
                }
            }
        } catch (ObjectCreationException e) {
            log("create plugin object failed");
            e.printStackTrace();
        }
        if (DBG) log("isSupport(), result = " + 
                ((null != mShareFilePlugIn) || (null != mShareVideoPlugIn) ? true : false));
        return (null != mShareFilePlugIn) || (null != mShareVideoPlugIn);
    }

    public boolean dismissDialogs() {
        if (null != mShareFilePlugIn) {
            mShareFilePlugIn.dismissDialog();
        }
        if (null != mShareVideoPlugIn) {
            mShareVideoPlugIn.dismissDialog();
        }
        return false;
    }

    public static ICallScreenPlugIn getShareFilePlugIn() {
        return mShareFilePlugIn;
    }

    public static ICallScreenPlugIn getShareVideoPlugIn() {
        return mShareVideoPlugIn;
    }

    public static CallManager getCallManager() {
        return mCM;
    }

    public static boolean isCapabilityToShare(String number) {
        if (DBG) log("isCapabilityToShare(), number = " + number);
        if (null == mShareFilePlugIn && null == mShareVideoPlugIn) {
            if (DBG) log("both plug-in are null, no capability");
            return false;
        }
        boolean isCapabilityToShareFile = false;
        boolean isCapabilityToShareVideo = false;
        if (null != mShareFilePlugIn && mShareFilePlugIn.getCapability(number)) {
            if (DBG) log("Share file plugIn has capability");
            isCapabilityToShareFile = true;
        }
        if (null != mShareVideoPlugIn && mShareVideoPlugIn.getCapability(number)) {
            if (DBG) log("Share video plugIn has capability");
            isCapabilityToShareVideo = true;
        }
        return isCapabilityToShareFile || isCapabilityToShareVideo;
    }

    public static boolean isTransferingFile() {
        if (null == mShareFilePlugIn) {
            return false;
        }
        return Constants.SHARE_FILE_STATE_TRANSFERING == mShareFilePlugIn.getState();
    }

    public static boolean isDisplayingFile() {
        if (null == mShareFilePlugIn) {
            return false;
        }
        return Constants.SHARE_FILE_STATE_DISPLAYING == mShareFilePlugIn.getState();
    }

    public static boolean isSharingVideo() {
        if (null == mShareVideoPlugIn) {
            return false;
        }
        return Constants.SHARE_VIDEO_STATE_SHARING == mShareVideoPlugIn.getState();
    }

    public boolean onDisconnect(Connection cn) {
        if (DBG) log("onDisconnect(), cn = " + cn);
        if (null != mShareFilePlugIn) {
            //if (isTransferingFile()) {
            //    mShareFilePlugIn.stop();
            //}
            mShareFilePlugIn.unregisterForCapabilityChange(cn.getAddress());
        }
        if (null != mShareVideoPlugIn) {
            //if (isSharingVideo()) {
            //    mShareVideoPlugIn.stop();
            //}
            mShareVideoPlugIn.unregisterForCapabilityChange(cn.getAddress());
        }
        return false;
    }

    public boolean updateScreen(CallManager callManager, boolean isForegroundActivity) {
        if (RCSeUtils.canShare(callManager)) {
            if (isSharingVideo() || isDisplayingFile()) {
                return false;
            }
        }
        mInCallScreen.setInCallTouchUiVisibility(View.VISIBLE);
        return false;
    }

    public boolean handleOnscreenButtonClick(int id) {
        switch (id) {
            case R.id.endSharingVideo:
                if (DBG) log("end sharing video button is clicked");
                if (null != mShareVideoPlugIn) {
                    mShareVideoPlugIn.stop();
                }
                return true;
            case R.id.shareFileButton:
                if (DBG) log("share file button is clicked");
                if (null != mShareFilePlugIn) {
                    String phoneNumber =
                        RCSeUtils.getRCSePhoneNumber(mCM);
                    if (null != phoneNumber) {
                        mShareFilePlugIn.start(phoneNumber);
                    }
                }
                return true;
            case R.id.shareVideoButton:
                if (DBG) log("share video button is clicked");
                if (null != mShareVideoPlugIn) {
                    String phoneNumber =
                        RCSeUtils.getRCSePhoneNumber(mCM);
                    if (null != phoneNumber) {
                        mShareVideoPlugIn.start(phoneNumber);
                    }
                }
                return true;
            }
        return false;
    }

    public class ShareFileCallScreenHost implements ICallScreenHost {

        public ShareFileCallScreenHost() {
        
        }

        public ViewGroup requestAreaForDisplay() {
            if (DBG) log("ShareFileCallScreenHost::requestAreaForDisplay()");
            return (ViewGroup) mInCallScreen.findViewById(R.id.centerAreaForSharing);
        }

        public void onStateChange(final int state) {
            if (DBG) log("ShareFileCallScreenHost::onStateChange(), state = " + state);
            if (null != mInCallScreen) {
                mInCallScreen.requestUpdateScreen();
            }
        }

        public void onCapabilityChange(String number, boolean isSupport) {
            if (DBG) log("ShareFileCallScreenHost::onCapabilityChange(), number = "
                    + number + ", isSupport = " + isSupport);
            if (null != mInCallScreen) {
                mInCallScreen.requestUpdateScreen();
            }
        }

        public Activity getCallScreenActivity() {
            return mInCallScreen;
        }

        public boolean getCapabilityByCallState() {
            return RCSeUtils.canShareFromCallState(mCM);
        }
    }

    public class ShareVideoCallScreenHost implements ICallScreenHost {

        public ShareVideoCallScreenHost() {
        
        }

        public ViewGroup requestAreaForDisplay() {
            if (DBG) log("ShareVideoCallScreenHost::requestAreaForDisplay()");
            return (ViewGroup) mInCallScreen.findViewById(R.id.largeAreaForSharing);
        }

        public void onStateChange(final int state) {
            if (DBG) log("ShareVideoCallScreenHost::onStateChange(), state = " + state);
            if (null != mInCallScreen) {
                mInCallScreen.requestUpdateScreen();
            }
        }

        public void onCapabilityChange(String number, boolean isSupport) {
            if (DBG) log("ShareVideoCallScreenHost::onCapabilityChange(), number = "
                    + number + ", isSupport = " + isSupport);
            if (null != mInCallScreen) {
                mInCallScreen.requestUpdateScreen();
            }
        }

        public Activity getCallScreenActivity() {
            return mInCallScreen;
        }

        public boolean getCapabilityByCallState() {
            return RCSeUtils.canShareFromCallState(mCM);
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
