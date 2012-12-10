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

package com.android.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.camera.R;
import com.android.camera.ui.RotateLayout;

/**
 * A on-screen hint is a view containing a little message for the user and will
 * be shown on the screen continuously.  This class helps you create and show
 * those.
 *
 * <p>
 * When the view is shown to the user, appears as a floating view over the
 * application.
 * <p>
 * The easiest way to use this class is to call one of the static methods that
 * constructs everything you need and returns a new {@code OnScreenHint} object.
 */
public class OnScreenHint {
    static final String TAG = "OnScreenHint";

    int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    int mX, mY;
    float mHorizontalMargin;
    float mVerticalMargin;
    View mView;
    View mNextView;
    int mOrientation = 0;

    private final WindowManager.LayoutParams mParams =
            new WindowManager.LayoutParams();
    private final WindowManager mWM;
    private final Handler mHandler = new Handler();

    /**
     * Construct an empty OnScreenHint object.  You must call {@link #setView}
     * before you can call {@link #show}.
     *
     * @param context  The context to use.  Usually your
     *                 {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     */
    public OnScreenHint(Context context) {
        mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mY = context.getResources().getDimensionPixelSize(
                R.dimen.hint_y_offset);
        mX = context.getResources().getDimensionPixelSize(
                R.dimen.hint_x_offset);

        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.windowAnimations = R.style.Animation_OnScreenHint;
        mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        mParams.setTitle("OnScreenHint");
    }

    /**
     * Show the view on the screen.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        mHandler.post(mShow);
    }

    /**
     * Close the view if it's showing.
     */
    public void cancel() {
        mHandler.post(mHide);
    }

    /**
     * Make a standard hint that just contains a text view.
     *
     * @param context  The context to use.  Usually your
     *                 {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     */
    public static OnScreenHint makeText(Context context, CharSequence text) {
        OnScreenHint result = new OnScreenHint(context);

        LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.on_screen_hint, null);
        TextView tv = (TextView) v.findViewById(R.id.message);
        tv.setText(text);

        result.mNextView = v;

        return result;
    }

    /**
     * Update the text in a OnScreenHint that was previously created using one
     * of the makeText() methods.
     * @param s The new text for the OnScreenHint.
     */
    public void setText(CharSequence s) {
        if (mNextView == null) {
            throw new RuntimeException("This OnScreenHint was not "
                    + "created with OnScreenHint.makeText()");
        }
        TextView tv = (TextView) mNextView.findViewById(R.id.message);
        if (tv == null) {
            throw new RuntimeException("This OnScreenHint was not "
                    + "created with OnScreenHint.makeText()");
        }
        tv.setText(s);
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
		if (mView == null || mView.getParent() == null) {
            mHandler.postDelayed(mRotate, 50);
            return;
        } else {
            mHandler.post(mRotate);
        }
    }

    private void handleRotate() {
		if (mView == null || mView.getParent() == null) {
            mHandler.postDelayed(mRotate, 50);
            return;
        }
		
		mHandler.removeCallbacks(mRotate);
		((RotateLayout)mView).setOrientation(mOrientation);
        setParamsWithOrientation();
        mWM.updateViewLayout(mView, mParams);
    }

    private void setParamsWithOrientation() {
    	Log.v("EX", "setParamsWithOrientation mOrientation=" + mOrientation);
        int gravity = 0;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (mOrientation == 0 || mOrientation == 180) {
        	gravity = (mOrientation == 0 ? Gravity.BOTTOM : Gravity.TOP) | Gravity.CENTER_HORIZONTAL;
        	mParams.x = 0;
        	mParams.y = mY;
        } else {
    		gravity = (mOrientation == 270 ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
        	mParams.x = mX;
        	mParams.y = 0;
            mParams.height = 500;
        }
        
        mParams.gravity = gravity;
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK)
                == Gravity.FILL_HORIZONTAL) {
            mParams.horizontalWeight = 1.0f;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK)
                == Gravity.FILL_VERTICAL) {
            mParams.verticalWeight = 1.0f;
        }
    }

    private synchronized void handleShow() {
        if (mNextView != null) {
            // remove the old view if necessary
            handleHide();
            mView = mNextView;
            
            setParamsWithOrientation();
            mParams.verticalMargin = mVerticalMargin;
            mParams.horizontalMargin = mHorizontalMargin;
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mWM.addView(mView, mParams);
        }
    }

    private synchronized void handleHide() {
        if (mView != null) {
            // note: checking parent() just to make sure the view has
            // been added...  i have seen cases where we get here when
            // the view isn't yet added, so let's try not to crash.
            if (mView.getParent() != null) {
                mWM.removeView(mView);
            }
            mView = null;
        }
    }

    private final Runnable mShow = new Runnable() {
        public void run() {
            handleShow();
        }
    };

    private final Runnable mHide = new Runnable() {
        public void run() {
            handleHide();
        }
    };

    private final Runnable mRotate = new Runnable() {
        public void run() {
            handleRotate();
        }
    };
}

