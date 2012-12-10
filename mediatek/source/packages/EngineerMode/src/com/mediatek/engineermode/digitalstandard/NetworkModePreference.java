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

package com.mediatek.engineermode.digitalstandard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.drawable.Drawable;

import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.preference.ListPreference;

import android.provider.Telephony;

import com.mediatek.engineermode.Elog;
import com.mediatek.engineermode.R;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;

import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.featureoption.FeatureOption;

public class NetworkModePreference extends ListPreference {

    private static final String TAG = "NetworkModePreference";
    private Context mContext;
    private Phone mGeminiPhone;
    private IntentFilter mIntentFilter;
    private LayoutInflater mFlater;
    private boolean IsMultipleCards = false;
    private String mSimNum;
    private int mColor;
    private String mName;
    private int mNumDisplayFormat;
    private int mStatus;

    private static final int DISPLAY_NONE = 0;
    private static final int DISPLAY_FIRST_FOUR = 1;
    private static final int DISPLAY_LAST_FOUR = 2;

    static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
    private TelephonyManagerEx mTelephonyManager;

    public NetworkModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFlater = LayoutInflater.from(context);

        mGeminiPhone = (Phone) PhoneFactory.getDefaultPhone();

        mTelephonyManager = TelephonyManagerEx.getDefault();

    }

    public void setMultiple(boolean value) {

        IsMultipleCards = value;

        if (IsMultipleCards == true) {

            SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext,Phone.GEMINI_SIM_1);
            if (info == null) {
                Elog.e(TAG, "can not get slot 1 sim info");
                IsMultipleCards = false;
            } else {
                mSimNum = info.mNumber;
                mColor = info.mColor;
                mName = info.mDisplayName;
                mNumDisplayFormat = info.mDispalyNumberFormat;

                mStatus = mTelephonyManager
                        .getSimIndicatorStateGemini(info.mSlot);

            }

        }
    }

    void setStatus(int status) {
        mStatus = status;
        notifyChanged();
    }

    public void updateSummary() {
        setSummary(getEntry());

    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = super.getView(convertView, parent);

        if (IsMultipleCards == false) {
            setSummary(getEntry());

        } else {

            // TODO Auto-generated method stub

            view = mFlater.inflate(R.layout.preference_sim_info, null);

            TextView viewTitle = (TextView) view
                    .findViewById(android.R.id.title);
            if ((mName != null) && (viewTitle != null)) {
                viewTitle.setText(mName);

                Elog.i(TAG, "mName is " + mName);
            }
            TextView viewSummary = (TextView) view.findViewById(android.R.id.summary);

            if (viewSummary != null) {
                if ((mSimNum != null) && (mSimNum.length() != 0)) {
                    viewSummary.setText(mSimNum);

                    Elog.i(TAG, "mSimNum is " + mSimNum);
                } else {
                    viewSummary.setVisibility(View.GONE);
                }
            }

            ImageView imageStatus = (ImageView) view
                    .findViewById(R.id.simStatus);

            if (imageStatus != null) {

                int res = Utils.getStatusResource(mStatus);

                if (res == -1) {
                    imageStatus.setVisibility(View.GONE);
                } else {
                    imageStatus.setImageResource(res);
                }

            }

            TextView view3g = (TextView) view.findViewById(R.id.sim3g);

            if (view3g != null) {
                view3g.setVisibility(View.GONE);

            }

            RelativeLayout viewSim = (RelativeLayout) view
                    .findViewById(R.id.simIcon);

            if (viewSim != null) {

                int res = Utils.getSimColorResource(mColor);

                if (res < 0) {
                    viewSim.setBackgroundDrawable(null);
                } else {
                    viewSim.setBackgroundResource(res);
                }

            }

            CheckBox ckRadioOn = (CheckBox) view
                    .findViewById(R.id.Check_Enable);

            if (ckRadioOn != null) {
                ckRadioOn.setVisibility(View.GONE);
            }

            TextView textNum = (TextView) view.findViewById(R.id.simNum);

            if ((textNum != null) && (mSimNum != null)) {

                switch (mNumDisplayFormat) {
                case DISPLAY_NONE: {
                    textNum.setVisibility(View.GONE);
                    break;

                }
                case DISPLAY_FIRST_FOUR: {

                    if (mSimNum.length() >= 4) {
                        textNum.setText(mSimNum.substring(0, 4));
                    } else {
                        textNum.setText(mSimNum);
                    }
                    break;
                }
                case DISPLAY_LAST_FOUR: {

                    if (mSimNum.length() >= 4) {
                        textNum.setText(mSimNum.substring(mSimNum.length() - 4));
                    } else {
                        textNum.setText(mSimNum);
                    }
                    break;
                }

                }
            }

            boolean bEnable = (mStatus == Phone.SIM_INDICATOR_RADIOOFF) ? false : true;

            if ((view != null) && (viewTitle != null) && (viewSummary != null)) {
                view.setEnabled(bEnable);
                viewTitle.setEnabled(bEnable);
                viewSummary.setEnabled(bEnable);
                setEnabled(bEnable);
                Elog.i(TAG, "Radio state is " + bEnable);
            }

        }

        return view;
    }

}
