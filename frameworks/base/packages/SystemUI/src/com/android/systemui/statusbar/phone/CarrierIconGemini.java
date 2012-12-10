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

package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.internal.telephony.Phone;
import com.android.systemui.statusbar.util.SIMHelper;
import com.mediatek.xlog.Xlog;

/**
 * This class is used to present the carriers information of dual SIM.
 */
// [SystemUI] Support dual SIM.
public class CarrierIconGemini extends ImageView {
    private static final String TAG = "CarrierIconGemini";

    private boolean mAttached;
    private int mSlotId = -1;

    public CarrierIconGemini(Context context) {
        this(context, null);
    }

    public CarrierIconGemini(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarrierIconGemini(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSlotId(int slotId) {
        this.mSlotId = slotId;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
            filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "onReceive, intent action is " + action + ".");
            if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
                SIMHelper.updateSIMInfos(context);
                int type = intent.getIntExtra("type", -1);
                long simId = intent.getLongExtra("simid", -1);
                if (type == 1) {
                    // color changed
                    setSIMInfo(simId);
                }
            } else if (action.equals(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION)) {
                int slotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
                if (slotId == mSlotId) {
                    setSIMInfo(slotId);
                }
            }
        }
    };

    public void setSIMInfoBySlot(int slotId) {
        if (slotId != 0 && slotId != 1) {
            Xlog.d(TAG, "updateCarrierBySlotId(" + slotId + "), the slotId=" + slotId + ".");
            return;
        }
        SIMInfo simInfo = SIMHelper.getSIMInfoBySlot(mContext, slotId);
        if (simInfo != null) {
            Xlog.d(TAG, "updateCarrierBySlotId(" + slotId + "), simId=" + simInfo.mSimId + ", simName=" + simInfo.mDisplayName);
            if (slotId == Phone.GEMINI_SIM_1) {
                this.setSIMInfo(simInfo);
            } else {
                this.setSIMInfo(simInfo);
            }
        }
    }

    public boolean setSIMInfo(long simId) {
        Xlog.d(TAG, "setSIMInfo(" + mSlotId + "), simId is " + simId + ".");
        if (simId <= 0) {
            Xlog.d(TAG, "setSIMInfo(" + mSlotId + "), the simId is <= 0.");
            return false;
        }
        SIMInfo simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if (simInfo == null) {
            Xlog.d(TAG, "setSIMInfo(" + mSlotId + "), the simInfo is null.");
            return false;
        }
        setSIMInfo(simInfo);
        return true;
    }

    public void setSIMInfo(SIMInfo simInfo) {
        if (simInfo == null) {
            Xlog.d(TAG, "setSIMInfo(" + mSlotId + "), the simInfo is null.");
            return;
        }
        if (mSlotId == simInfo.mSlot) {
            Xlog.d(TAG, "setSIMInfo(" + mSlotId + "), res=" + simInfo.mSimBackgroundRes);
            this.setImageResource(simInfo.mSimBackgroundRes);
        }
    }
}
