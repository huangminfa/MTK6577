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

package com.mediatek.cmmb.app;

import com.mediatek.mbbms.MBBMSStore.DB;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spanned;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.text.method.MovementMethod;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.MotionEvent;
import android.util.Log;
import android.util.AttributeSet;
import android.widget.TextView;

public class LocationAdviceActivity extends Activity {
    private static final String TAG = "CMMB::LocationAdviceActivity";
	
    private ModeSwitchManager mModeSwitchManager;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		final TextView advice = new TextView(this);
		String c_a_m = getString(R.string.click_and_modify);
		String location = DB.Setting.getSettingValue(getContentResolver(),DB.Setting.NAME_SG_AREA);
		if (location == null || location.equals("")) {
			location = getString(R.string.unknown_location);
		} else {
			location = Utils.SavedLocation.getDisplayName(location);
		}
		
		String area_warn = getString(R.string.area_warn,location,c_a_m);
		final int start = area_warn.indexOf(c_a_m);
		final int end = start + c_a_m.length();
		SpannableStringBuilder area_warn_spannable = new SpannableStringBuilder(area_warn);
		area_warn_spannable.setSpan(new ClickableSpan() {
				public void onClick(View widget) {
					Intent intent = new Intent(Intent.ACTION_PICK,null,
						LocationAdviceActivity.this,LocationSelector.class);
					intent.putExtra(Utils.EXTRA_LOCATION_MODE,Utils.LOCATION_MODE_NORMAL);
					intent.setType(Utils.LOCATION_MIME_TYPE);
					startActivityForResult(intent,0);
				}
			},start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		advice.setMovementMethod(LinkMovementMethod.getInstance());
		advice.setText(area_warn_spannable);		
		advice.setPadding(30,50,30,0);
		advice.setTextAppearance(this, android.R.style.TextAppearance_Large);		
		advice.setTextColor(getResources().getColorStateList(R.color.linked_text_color));
		advice.setLongClickable(false);
        setContentView(advice);
        mModeSwitchManager = new ModeSwitchManager(this,null,savedInstanceState);
		
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
			finish();
		}    
    }	
	
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);    
        mModeSwitchManager.onSaveInstanceState(state);
    }

    @Override
    public void onStart() {
        super.onStart();
        mModeSwitchManager.onActivityStart();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        mModeSwitchManager.onActivityStop();
    }			
}
