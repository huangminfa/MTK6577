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

package com.android.camera.ui;

import com.mediatek.xlog.Xlog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A textview that only do relayout when the length of the text changed to improve performance. 
 * only used for display time string with format like hh:mm::ss(num. of h can be more than 2).
 */
public class TimeTextView extends TextView {
    private static final String TAG = "TimeTextView";

	private boolean mWidthFixed = false;
    public TimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	public void setTime(String ms) {
		//Xlog.d(TAG,"setTime ms = "+ms);
		if (ms.length() != getText().length()) {
			Xlog.d(TAG,"setTime length !=");
			
			//need relayout.
			setMaxWidth(Integer.MAX_VALUE);
			setMinWidth(0);
			mWidthFixed = false;
		} 
		setText(ms);
	}

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int w = (right - left);
		Xlog.d(TAG,"onLayout w = "+w);
		if (!mWidthFixed) {
			
			Xlog.d(TAG,"onLayout !mWidthFixed");
			//we will get 0 during the initialization of this view,don't set it.
			//Otherwise it will disappear from the screen.
			if (w > 0) {
				//fix the width to skip relayout,see TextView.checkForRelayout().
				setWidth(w);
				mWidthFixed = true;
			}
		}
    }	
}
