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

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class RotateTextToast {
    private static final int TOAST_DURATION = 5000; // milliseconds
    ViewGroup mLayoutRoot;
    RotateLayout mToast;
    Handler mHandler;
    FrameLayout mMessageParent;
    boolean mAlignTop;
    int mOrientation;
    int mTextResourceId;

    public RotateTextToast(Activity activity, int textResourceId, int orientation) {
        mLayoutRoot = (ViewGroup) activity.getWindow().getDecorView();
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.rotate_text_toast, mLayoutRoot);
        mToast = (RotateLayout) v.findViewById(R.id.rotate_toast);
        TextView tv = (TextView) mToast.findViewById(R.id.message);
        mTextResourceId = textResourceId;
        tv.setText(mTextResourceId);
        mOrientation = orientation;
        mToast.setOrientation(orientation);
        mHandler = new Handler();
    }

    public RotateTextToast(Activity activity, String textStr, int orientation) {
        mLayoutRoot = (ViewGroup) activity.getWindow().getDecorView();
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.rotate_text_toast, mLayoutRoot);
        mToast = (RotateLayout) v.findViewById(R.id.rotate_toast);
        TextView tv = (TextView) mToast.findViewById(R.id.message);
        tv.setText(textStr);
        mOrientation = orientation;
        mToast.setOrientation(orientation);
        mHandler = new Handler();
    }

    public RotateTextToast(Activity activity, int textResourceId, int orientation, ViewGroup root) {
        mLayoutRoot = root;
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.rotate_text_toast, mLayoutRoot);
        mToast = (RotateLayout) v.findViewById(R.id.rotate_toast);
		if (root instanceof RelativeLayout) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mToast.getLayoutParams(); 
			params.addRule(RelativeLayout.CENTER_IN_PARENT); 		
		}		
        TextView tv = (TextView) mToast.findViewById(R.id.message);
        mTextResourceId = textResourceId;
        tv.setText(mTextResourceId);
        mOrientation = orientation;
        mToast.setOrientation(orientation);
        mHandler = new Handler();
    }	

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            Util.fadeOut(mToast);
            mLayoutRoot.removeView(mToast);
            mToast = null;
        }
    };

    public void show() {
        mToast.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mRunnable, TOAST_DURATION);
    }

    public void showTransparentForAWhile() {
        showTransparent();
        mHandler.postDelayed(mRunnable, TOAST_DURATION);
    }

    public void showTransparent() {
        mAlignTop = true;
        mMessageParent = (FrameLayout) mToast.findViewById(R.id.messageParent);
        mMessageParent.setBackgroundColor(0);
        mMessageParent.setPadding(mMessageParent.getPaddingLeft(), 12,
                mMessageParent.getPaddingRight(), 12);
        adjustLayout(mOrientation);
        Util.fadeIn(mToast);
    }

	public void setOrientation(int orientation) {
		//TBD:need to solve the ghost when doing fading and changing orientation at the same time.
		if (mToast != null && mToast.getVisibility() == View.VISIBLE) {
			adjustLayout(orientation);
			mToast.setOrientation(orientation);
		}
	}

	private void adjustLayout(int orientation) {
		if (mAlignTop && mLayoutRoot instanceof RelativeLayout) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mToast.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			params.setMargins(0, 0, 0, 0);
			switch (orientation) {
			case 0:
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				break;
			case 90:
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case 180:
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.setMargins(0, 0, 0, 60);
				break;
			case 270:
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			}
			TextView tv = (TextView) mToast.findViewById(R.id.message);
			tv.setWidth(500);
	        tv.setGravity(Gravity.CENTER);

	        mToast.requestLayout();
		}
	}

    public void hide() {
		if (mToast != null && mToast.getVisibility() == View.VISIBLE) {
	        mToast.setVisibility(View.GONE);
	        mHandler.removeCallbacksAndMessages(null);
		}
    }

    public void changeTextContent(int textResourceId) {
        if (mTextResourceId == textResourceId) return; 
        Util.fadeOut(mToast);
        TextView tv = (TextView) mToast.findViewById(R.id.message);
        mTextResourceId = textResourceId;
        tv.setText(mTextResourceId);
        Util.fadeIn(mToast);
    }
}
