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

import com.android.phone.InCallTouchUi;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.CallManager;
import android.view.LayoutInflater;
import com.android.phone.R;
import android.widget.ImageButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.graphics.drawable.LayerDrawable;

/**
 * In-call onscreen touch UI elements, used on some platforms.
 *
 * This widget is a fullscreen overlay, drawn on top of the
 * non-touch-sensitive parts of the in-call UI (i.e. the call card).
 */
public class InCallTouchUiForRCSe extends InCallTouchUi {

    private static final String LOG_TAG = "InCallTouchUiForRCSe";
    private static final boolean DBG = true;
    
    private ViewGroup mEndSharingVideoButtonWrapper;
    private ImageButton mEndSharingVideoButton;
    private ImageButton mShareFileButton;
    private ImageButton mShareVideoButton;
    private ViewGroup mInCallControlArea;

    public InCallTouchUiForRCSe(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DBG) log("InCallTouchUiForRCSe constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);

    }

    protected void inflate(Context context) {
        if (DBG) log("InCallTouchUiForRCSe inflate()...");
        // Inflate our contents, and add it (to ourself) as a child.
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(
                R.layout.incall_touch_ui,  // resource
                this,                           // root
                true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (DBG) log("InCallTouchUiForRCSe onFinishInflate(this = " + this + ")...");
        mEndSharingVideoButton = (ImageButton) findViewById(R.id.endSharingVideo);
        mEndSharingVideoButton.setOnClickListener(this);
        mEndSharingVideoButtonWrapper = (ViewGroup) findViewById(R.id.endSharingVideoWrapper);
        mShareFileButton = (ImageButton) findViewById(R.id.shareFileButton);
        mShareFileButton.setOnClickListener(this);
        mShareVideoButton = (ImageButton) findViewById(R.id.shareVideoButton);
        mShareVideoButton.setOnClickListener(this);
        mInCallControlArea = (ViewGroup) findViewById(R.id.inCallControlArea);
    }

    private void setCompoundButtonBackgroundTransparency(CompoundButton button, int transparency) {
        LayerDrawable layers = (LayerDrawable) button.getBackground();
        layers.findDrawableByLayerId(R.id.compoundBackgroundItem).setAlpha(transparency);
    }

    private void updateBottomButtons(CallManager cm) {
        if (RCSeUtils.canShare(cm)) {
            if (InCallScreenRCSeExtension.isSharingVideo()) {
                if (DBG) log("updateBottomButtons(), is sharing video");
                mEndSharingVideoButtonWrapper.setVisibility(View.VISIBLE);
                mShareFileButton.setVisibility(View.VISIBLE);
                mShareVideoButton.setVisibility(View.VISIBLE);
                mHoldButton.setVisibility(View.GONE);
                mInCallControlArea.getBackground().setAlpha(200);
                setCompoundButtonBackgroundTransparency(mMuteButton, 150);
                setCompoundButtonBackgroundTransparency(mAudioButton, 150);
            } else {
                if (DBG) log("updateBottomButtons(), not sharing video");
                mEndSharingVideoButtonWrapper.setVisibility(View.GONE);
                mShareFileButton.setVisibility(View.VISIBLE);
                mShareVideoButton.setVisibility(View.VISIBLE);
                mHoldButton.setVisibility(View.GONE);
                mInCallControlArea.getBackground().setAlpha(255);
                setCompoundButtonBackgroundTransparency(mMuteButton, 255);
                setCompoundButtonBackgroundTransparency(mAudioButton, 255);
            }
            mDialpadButton.setVisibility(View.GONE);
        } else {
            mShareFileButton.setVisibility(View.GONE);
            mShareVideoButton.setVisibility(View.GONE);
            mDialpadButton.setVisibility(View.VISIBLE);
            mEndSharingVideoButtonWrapper.setVisibility(View.GONE);
            mHoldButton.setVisibility(View.VISIBLE);
            mInCallControlArea.getBackground().setAlpha(255);
            setCompoundButtonBackgroundTransparency(mMuteButton, 255);
            setCompoundButtonBackgroundTransparency(mAudioButton, 255);
        }
    }

    /**
     * Updates the visibility and/or state of our UI elements, based on
     * the current state of the phone.
     */
    public void updateState(CallManager cm) {
        super.updateState(cm);
        if (DBG) log("updateState()");
        updateBottomButtons(cm);
    }

    // View.OnClickListener implementation
    public void onClick(View view) {
        int id = view.getId();
        if (DBG) log("onClick(View " + view + ", id " + id + ")...");

        switch (id) {
            case R.id.endSharingVideo:
            case R.id.shareFileButton:
            case R.id.shareVideoButton:
                mInCallScreen.handleOnscreenButtonClick(id);
                return;
        }
        super.onClick(view);
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
