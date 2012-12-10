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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FaceView extends View implements FocusIndicator, Rotatable {
    private final String TAG = "FaceView";
    private final boolean LOGV = false;
    // The value for android.hardware.Camera.setDisplayOrientation.
    private int mDisplayOrientation;
    // The orientation compensation for the face indicator to make it look
    // correctly in all device orientations. Ex: if the value is 90, the
    // indicator should be rotated 90 degrees counter-clockwise.
    private int mOrientation;
    private boolean mMirror;
    private boolean mPause;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Face[] mFaces;
    private Drawable mFaceIndicator;
    private final Drawable mDrawableFocusing;
    private final Drawable mDrawableFocused;
    private final Drawable mDrawableFocusFailed;
    private final Drawable mDrawableBeautyFace;
    private boolean mEnableBeauty;
    private int mLastFaceNum;
    private FocusIndicatorView mFocusIndicator;

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawableFocusing = getResources().getDrawable(R.drawable.ic_focus_focusing);
        mDrawableFocused = getResources().getDrawable(R.drawable.ic_focus_face_focused);
        mDrawableFocusFailed = getResources().getDrawable(R.drawable.ic_focus_failed);
        mDrawableBeautyFace = getResources().getDrawable(R.drawable.ic_facebeautify_frame);
        mFaceIndicator = mDrawableFocusing;
    }

    public void setFaces(Face[] faces) {
    	int num = faces.length;
        if (LOGV) Log.v(TAG, "Num of faces=" + num);
        if (mPause || (num == 0 && mLastFaceNum == 0)) return;
        mFaces = faces;
        mLastFaceNum = num;
        if (num > 0 && mFocusIndicator != null) {
        	mFocusIndicator.clear();
        }
        invalidate();
    }

    public void setDisplayOrientation(int orientation) {
        mDisplayOrientation = orientation;
        if (LOGV) Log.v(TAG, "mDisplayOrientation=" + orientation);
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        invalidate();
    }

    public void setMirror(boolean mirror) {
        mMirror = mirror;
        if (LOGV) Log.v(TAG, "mMirror=" + mirror);
    }

    public boolean faceExists() {
        return (mFaces != null && mFaces.length > 0);
    }

    @Override
    public void showStart() {
        mFaceIndicator = mDrawableFocusing;
        invalidate();
    }

    @Override
    public void showSuccess() {
        mFaceIndicator = mDrawableFocused;
        invalidate();
    }

    @Override
    public void showFail() {
        mFaceIndicator = mDrawableFocusFailed;
        invalidate();
    }

    @Override
    public void clear() {
        // Face indicator is displayed during preview. Do not clear the
        // drawable.
        mFaceIndicator = mDrawableFocusing;
        mFaces = null;
        invalidate();
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
    }
    
    public void enableFaceBeauty(boolean enable) {
    	mEnableBeauty = enable;
    	if (!mEnableBeauty) {
    		mFaceIndicator = mDrawableFocusing;
    	}
    }

    public void setFocusIndicator(FocusIndicatorView focusIndicator) {
    	mFocusIndicator = focusIndicator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces != null && mFaces.length > 0) {
        	if (mEnableBeauty && mFaceIndicator == mDrawableFocusing) {
            	mFaceIndicator = mDrawableBeautyFace;
        	}
            // Prepare the matrix.
            Util.prepareMatrix(mMatrix, mMirror, mDisplayOrientation, getWidth(), getHeight());

            // Focus indicator is directional. Rotate the matrix and the canvas
            // so it looks correctly in all orientations.
            canvas.save();
            mMatrix.postRotate(mOrientation); // postRotate is clockwise
            canvas.rotate(-mOrientation); // rotate is counter-clockwise (for canvas)
            for (int i = 0; i < mFaces.length; i++) {
                // Transform the coordinates.
                mRect.set(mFaces[i].rect);
                if (LOGV) Util.dumpRect(mRect, "Original rect");
                mMatrix.mapRect(mRect);
                if (LOGV) Util.dumpRect(mRect, "Transformed rect");

                mFaceIndicator.setBounds(Math.round(mRect.left), Math.round(mRect.top),
                        Math.round(mRect.right), Math.round(mRect.bottom));
                mFaceIndicator.draw(canvas);
            }
            canvas.restore();
        }
        super.onDraw(canvas);
    }
}
