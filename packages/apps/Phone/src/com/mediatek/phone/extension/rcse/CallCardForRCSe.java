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

import com.android.phone.CallCard;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import com.android.phone.R;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;

/**
 * "Call card" UI element: the in-call screen contains a tiled layout of call
 * cards, each representing the state of a current "call" (ie. an active call,
 * a call on hold, or an incoming call.)
 */
public class CallCardForRCSe extends CallCard implements View.OnClickListener {

    private static final String LOG_TAG = "CallCardForRCSe";
    private static final boolean DBG = true;

    private ViewGroup mCenterArea;
    private ViewGroup mWholeArea;

    private boolean mIsCenterAreaFullScreen;

    public CallCardForRCSe(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DBG) log("CallCardForRCSe constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);

    }

    @Override
    protected void onFinishInflate() {
        if (DBG) log("onFinishInflate()");
        super.onFinishInflate();
        mCenterArea = (ViewGroup) findViewById(R.id.centerAreaForSharing);
        mWholeArea = (ViewGroup) findViewById(R.id.largeAreaForSharing);
        mWholeArea.setOnClickListener(this);
    }

    protected void updateCallInfoLayout(Phone.State state) {
        if (DBG) log("updateCallInfoLayout(), state = " + state);
        if (shouldResetLayoutMargin()) {
            ViewGroup.MarginLayoutParams callInfoLp =
                    (ViewGroup.MarginLayoutParams) getLayoutParams();
            callInfoLp.bottomMargin = 0;  // Equivalent to setting
                                          // android:layout_marginBottom in XML
            if (DBG) log("  ==> callInfoLp.bottomMargin: 0");
            setLayoutParams(callInfoLp);
        } else {
            if (DBG) log("updateCallInfoLayout(), not share video, just call super");
            super.updateCallInfoLayout(state);
        }
    }

    /**
     * Updates the state of all UI elements on the CallCard, based on the
     * current state of the phone.
     */
    public void updateState(CallManager cm) {
        super.updateState(cm);
        if (DBG) log("updateState(" + cm + ")...");
        if (RCSeUtils.canShare(cm)) {
            if (DBG) log("updateState(), can share" );
            // have capability to share
            mCallStateLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rcse_indicaton, 0, 0, 0);
            mCallStateLabel.setCompoundDrawablePadding((int) (mDensity * 5));
            if (InCallScreenRCSeExtension.isTransferingFile()) {
                if (DBG) log("updateState(), is transfering file");
                // share file
                mPhoto.setVisibility(View.INVISIBLE);
                mCenterArea.setVisibility(View.VISIBLE);
                if (mIsCenterAreaFullScreen) {
                    fullDisplayCenterArea(false);
                }
            } else if (InCallScreenRCSeExtension.isDisplayingFile()) {
                if (DBG) log("updateState(), is displaying file");
                mPhoto.setVisibility(View.INVISIBLE);
                mCenterArea.setVisibility(View.VISIBLE);
            } else {
                if (DBG) log("updateState(), not sharing file");
                // not share file
                mPhoto.setVisibility(View.VISIBLE);
                mCenterArea.setVisibility(View.GONE);
                if (mIsCenterAreaFullScreen) {
                    fullDisplayCenterArea(false);
                }
            }

            if (InCallScreenRCSeExtension.isSharingVideo()) {
                if (DBG) log("updateState(), is sharing video");
                // share video
                mWholeArea.setVisibility(View.VISIBLE);
            } else {
                if (DBG) log("updateState(), not sharing video" );
                mWholeArea.setVisibility(View.GONE);
                hideStatusBar(false);
            }

            if (InCallScreenRCSeExtension.isSharingVideo() ||
                    (InCallScreenRCSeExtension.isDisplayingFile() && mIsCenterAreaFullScreen)) {
                hideStatusBar(true);
            } else {
                hideStatusBar(false);
            }
        } else {
            if (DBG) log("updateState(), can not share" );
            // Clear out any icons
            mCallStateLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mCenterArea.setVisibility(View.GONE);
            mWholeArea.setVisibility(View.GONE);
            hideStatusBar(false);
            if (mIsCenterAreaFullScreen) {
                fullDisplayCenterArea(false);
            }
        }
    }

    // View.OnClickListener implementation
    public void onClick(View view) {
        int id = view.getId();
        if (DBG) log("onClick(View " + view + ", id " + id + ")...");

        switch (id) {
            case R.id.largeAreaForSharing:
                int visibility = mInCallScreen.getInCallTouchUiVisibility();
                if (DBG) log("large area for sharing is clicked, visibility is " + visibility);
                mInCallScreen.setInCallTouchUiVisibility(visibility == View.VISIBLE 
                                                         ? View.INVISIBLE : View.VISIBLE);
                return;
            case R.id.centerAreaForSharing:
                if (DBG) log("center area for sharing is clicked");
                if (InCallScreenRCSeExtension.isDisplayingFile()) {
                    hideStatusBar(!mIsCenterAreaFullScreen);
                    mInCallScreen.setInCallTouchUiVisibility(mIsCenterAreaFullScreen ? View.INVISIBLE
                                                                                     : View.VISIBLE);
                    fullDisplayCenterArea(!mIsCenterAreaFullScreen);
                }
                return;
        }
    }

    /*
    public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
        if (DBG) log("onQueryComplete(), token = " + token + ", cookie = " + cookie + ", CallInfo = " + ci);
        super.onQueryComplete(token, cookie, ci);
        if (cookie instanceof Call) {
            if (ci.contactExists) {
                if (DBG) log("onQueryComplete(), contact exits");
                if (null != InCallScreenRCSeExtension.getShareFilePlugIn()) {
                    InCallScreenRCSeExtension.getShareFilePlugIn().registerForCapabilityChange(ci.phoneNumber);
                }
                if (null != InCallScreenRCSeExtension.getShareFilePlugIn()) {
                    InCallScreenRCSeExtension.getShareVideoPlugIn().registerForCapabilityChange(ci.phoneNumber);
                }
            }
        }
    } */

    private void hideStatusBar(final boolean isHide) {
        if (DBG) log("hideStatusBar(), isHide = " + isHide);
        WindowManager.LayoutParams attrs = mInCallScreen.getWindow().getAttributes();
        if (isHide) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mInCallScreen.getWindow().setAttributes(attrs);
    }

    private void fullDisplayCenterArea(final boolean isFullDisplay) {
        if (DBG) log("fullDisplayCenterArea(), isFullDisplay = " + isFullDisplay);
        if (isFullDisplay) {
            mIsCenterAreaFullScreen = true;
            mPrimaryCallBanner.setVisibility(View.GONE);
            mCallStateLabel.setVisibility(View.GONE);
        } else {
            mIsCenterAreaFullScreen = false;
            mPrimaryCallBanner.setVisibility(View.VISIBLE);
            mCallStateLabel.setVisibility(View.VISIBLE);
        }
    }

    private boolean shouldResetLayoutMargin() {
        if (DBG) log("shouldResetLayoutMargin()");
        if (!RCSeUtils.canShare(InCallScreenRCSeExtension.getCallManager())) {
            if (DBG) log("Can not share, so no need reset layout margin");
            return false;
        }
        if (InCallScreenRCSeExtension.isSharingVideo()) {
            if (DBG) log("is sharing video, so need reset layout margin");
            return true;
        }
        if (InCallScreenRCSeExtension.isDisplayingFile() &&
                mIsCenterAreaFullScreen) {
            if (DBG) log("is displaying file and full screen, so need reset layout margin");
            return true;
        }
        return false;
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

}
