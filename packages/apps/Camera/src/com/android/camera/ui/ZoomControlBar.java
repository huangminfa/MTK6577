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

import com.android.camera.R;
import com.android.camera.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

/**
 * A view that contains camera zoom control and its layout.
 */
public class ZoomControlBar extends ZoomControl {
    private static final String TAG = "ZoomControlBar";
    private static final int THRESHOLD_FIRST_MOVE = Util.dpToPixel(10); // pixels
    // Space between indicator icon and the zoom-in/out icon.
    private static final int ICON_SPACING = Util.dpToPixel(12);
    private static final int THRESHOLD_MOVE = Util.dpToPixel(6); // pixels
    private ZoomSlider mSliderBar;
    private boolean mStartChanging;
    private int mSliderPosition = 0;
    private int mSliderLength;
    private int mWidth;
    private int mIconWidth;
    private int mTotalIconWidth;
	private int mLastSetSliderPosition = 0;	

    public ZoomControlBar(Context context, AttributeSet attrs) {
        super(context, attrs,true);
        mSliderBar = new ZoomSlider(context, attrs);
        addView(mSliderBar);   
		mSliderBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        mSliderBar.setActivated(activated);
    }

    private int getSliderPosition(int x) {
        // Calculate the absolute offset of the slider in the zoom control bar.
        // For left-hand users, as the device is rotated for 180 degree for
        // landscape mode, the zoom-in bottom should be on the top, so the
        // position should be reversed.
        int pos; // the relative position in the zoom slider bar
        if (mOrientation == 90) {
            pos = mWidth - mTotalIconWidth - x;
        } else {
            pos = x - mTotalIconWidth;
        }
        if (pos < 0) pos = 0;
        if (pos > mSliderLength) pos = mSliderLength;
        return pos;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mIconWidth = mZoomIn.getMeasuredWidth();
        mTotalIconWidth = mIconWidth + ICON_SPACING;
        mSliderLength = mWidth  - (2 * mTotalIconWidth);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled() || (mWidth == 0)) return false;
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setActivated(false);
                closeZoomControl();
                break;

            case MotionEvent.ACTION_DOWN:
                setActivated(true);
                mStartChanging = false;
            case MotionEvent.ACTION_MOVE:
                int pos = getSliderPosition((int) event.getX());
                if (!mStartChanging) {
                    // Make sure the movement is large enough before we start
                    // changing the zoom.
                    int delta = mSliderPosition - pos;
                    if ((delta > THRESHOLD_FIRST_MOVE) ||
                            (delta < -THRESHOLD_FIRST_MOVE)) {
                        mStartChanging = true;
                    }
                }
				
                if (mStartChanging) {
                    mSliderPosition = pos;
					if (mLastSetSliderPosition - mSliderPosition > THRESHOLD_MOVE || 
							mLastSetSliderPosition - mSliderPosition < -THRESHOLD_MOVE) {	
						performZoom(1.0d * mSliderPosition / mSliderLength);
						mLastSetSliderPosition = mSliderPosition;
						mSliderBar.setSliderPosition(mSliderPosition);
					}

				}
        }
        return true;
    }

    @Override
    public void setOrientation(int orientation) {
        // layout for the left-hand camera control
        if ((orientation == 90) || (mOrientation == 90)) requestLayout();
        super.setOrientation(orientation);
		mSliderBar.setOrientation(orientation);
    }

    @Override
    protected void onLayout(
            boolean changed, int left, int top, int right, int bottom) {
        if (mZoomMax == 0) return;
        int height = bottom - top;
        // For left-hand users, as the device is rotated for 180 degree,
        // the zoom-in button should be on the top.
        if (mOrientation == 90) {
            mZoomIn.layout(0, 0, mIconWidth, height);
            mZoomOut.layout(mWidth - mIconWidth, 0, mWidth, height);
        } else {
            mZoomOut.layout(0, 0, mIconWidth, height);
            mZoomIn.layout(mWidth - mIconWidth, 0, mWidth, height);
        }
		int sliderWidth = mSliderBar.getSliderWidth();
        mSliderBar.layout(mTotalIconWidth - sliderWidth/2, 0, mWidth - mTotalIconWidth + sliderWidth/2, height);
    }

    @Override
    public void setZoomIndex(int index) {
        super.setZoomIndex(index);
        mSliderPosition = -1; // -1 means invalid
        mLastSetSliderPosition = mSliderPosition;
        mSliderBar.setSliderPosition((int)((double) mSliderLength * mZoomIndex / mZoomMax));
        requestLayout();
    }	
}
