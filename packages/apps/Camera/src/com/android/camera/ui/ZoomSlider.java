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

import com.android.camera.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view that contains camera zoom slider and bar.
 */
public class ZoomSlider extends View {
    private static final String TAG = "ZoomSlider";

    private Drawable mBarDrawable;
    private Drawable mSliderDrawable;
    private int mSliderPosition = 0;
    private int mOrientation;
	
    public ZoomSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBarDrawable = context.getResources().getDrawable(R.drawable.zoom_slider_bar);
		mBarDrawable.setCallback(this);
		mBarDrawable.setVisible(true, false);	
		
        mSliderDrawable = context.getResources().getDrawable(R.drawable.ic_zoom_slider);
		mSliderDrawable.setCallback(this);
		mSliderDrawable.setVisible(true, false);
    }

	public void setSliderPosition(int pos) {
		mSliderPosition = pos;
		invalidate();
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int sliderWidth = mSliderDrawable.getIntrinsicWidth();    
		mBarDrawable.setBounds(sliderWidth/2,0,w - sliderWidth/2,h);
    }

    @Override    
	protected void drawableStateChanged() {
        Drawable d = mBarDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }	

	public int getSliderWidth() {
		return mSliderDrawable.getIntrinsicWidth();
	}

    public void setOrientation(int orientation) {
        if ((orientation == 90) || (mOrientation == 90)) invalidate();		
        mOrientation = orientation;		
    }	

	@Override
    protected void onDraw(Canvas canvas) {
		mBarDrawable.draw(canvas);

        int sliderWidth = mSliderDrawable.getIntrinsicWidth();
        int sliderHeight = mSliderDrawable.getIntrinsicHeight();
		
        int pos; // slider position
        if (mOrientation == 90) {
            pos = getWidth() - mSliderPosition - sliderWidth;
        } else {
            pos = mSliderPosition;
        }
		
		int sliderTop = (getHeight() - sliderHeight)/2;
		mSliderDrawable.setBounds(pos,sliderTop,pos+sliderWidth,sliderTop + sliderHeight);
		mSliderDrawable.draw(canvas);
    }
}
