/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.android.internal.telephony.Phone;

public class ApnPreference extends Preference implements
        CompoundButton.OnCheckedChangeListener, OnClickListener {
    final static String TAG = "ApnPreference";

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public ApnPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     */
    public ApnPreference(Context context) {
        super(context);
        init();
    }

    private static String mSelectedKey = null;
    private static CompoundButton mCurrentChecked = null;
    private boolean mProtectFromCheckedChange = false;
    private boolean mSelectable = true;
    
    private int mSourceType = 2;
    private int mSimId = 0;
    private static boolean mIsCU = false;
    private boolean mEditable = true;

    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);

        View widget = view.findViewById(R.id.apn_radiobutton);
        if ((widget != null) && widget instanceof RadioButton) {
            RadioButton rb = (RadioButton) widget;
            if (mSelectable) {
                rb.setOnCheckedChangeListener(this);

                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = rb;
                    mSelectedKey = getKey();
                }

                mProtectFromCheckedChange = true;
                rb.setChecked(isChecked);
                mProtectFromCheckedChange = false;
            } else {
                rb.setVisibility(View.GONE);
            }
        }

        View textLayout = view.findViewById(R.id.text_layout);
        if ((textLayout != null) && textLayout instanceof RelativeLayout) {
            textLayout.setOnClickListener(this);
        }

        return view;
    }

    private void init() {
        setLayoutResource(R.layout.apn_preference_layout);
    }

    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }

    public void setChecked() {
        mSelectedKey = getKey();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "ID: " + getKey() + " :" + isChecked);
        if (mProtectFromCheckedChange) {
            return;
        }

        if (isChecked) {
            if (mCurrentChecked != null) {
                mCurrentChecked.setChecked(false);
            }
            mCurrentChecked = buttonView;
            mSelectedKey = getKey();
            callChangeListener(mSelectedKey);
        } else {
            mCurrentChecked = null;
            mSelectedKey = null;
        }
    }

    public void onClick(android.view.View v) {
        if ((v != null) && (R.id.text_layout == v.getId())) {
            Context context = getContext();
            if (context != null) {
                int pos = Integer.parseInt(getKey());
                Uri orig;
                if(Utils.opType == Utils.OpIndex.OP_CMCC) {

                    if (mSimId == ApnSettings.SIM_CARD_2){
                        orig = Telephony.Carriers.SIM2Carriers.CONTENT_URI;
                    } else if (mSimId == ApnSettings.SIM_CARD_1) {
                        orig = Telephony.Carriers.SIM1Carriers.CONTENT_URI;
                    } else {
                        orig = Telephony.Carriers.CONTENT_URI;
                    }
                } else {
                    if (mSimId == ApnSettings.SIM_CARD_2) {
                        orig = Telephony.Carriers.GeminiCarriers.CONTENT_URI;
                    } else {
                        orig = Telephony.Carriers.CONTENT_URI;
                    }
                }

                Uri url = ContentUris.withAppendedId(orig, pos);

                Intent it = new Intent(Intent.ACTION_EDIT, url);
                it.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimId);
                it.putExtra("readOnly",!mEditable);
                context.startActivity(it);   

	        Log.d(TAG, "mSimId " + mSimId);      
	        Log.d(TAG, "orig " + orig);                                
            }
        }
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public boolean getSelectable() {
        return mSelectable;
    }

    public void setSimId(int simid) {
        mSimId = simid;
    }

    public void setApnEditable(boolean isEditable) {
        mEditable = isEditable;
    }

    public int getSourceType() {
        return mSourceType;
    }

    public void setSourceType(int type) {
        mSourceType = type;
    }
    
}
