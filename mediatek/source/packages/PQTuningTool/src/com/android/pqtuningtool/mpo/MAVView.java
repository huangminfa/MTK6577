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

package  com.android.pqtuningtool.mpo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;

import android.view.Display;
import android.app.Activity;

import  com.android.pqtuningtool.util.Log;

public class MAVView extends MPOView {
    
    private static final String TAG = "MAVView";
    private Bitmap[] mBitmapArr;
    //private Bitmap mMiddleFrame;
    //private int mMiddleFrameIndex = 0;
    private float[] mRectifyValue = {0, 0, 0};
    private boolean mFirstTime = true;
    private int mLastIndex = 0xFFFF;
    private static int BASE_ANGLE = 15;
    private Matrix mBaseMatrix = new Matrix();
    public boolean mResponsibility = false;
    public RectifySensorListener mRectifySensorListener = new RectifySensorListener();
    
    private long mChangedTime = 0;

    public MAVView(Context context) {
        super(context);
    }
    
    public MAVView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    class RectifySensorListener implements SensorEventListener {
        
        public void onSensorChanged(SensorEvent event) {
            mRectifyValue[0] = event.values[0];
            mRectifyValue[1] = event.values[1];
            mRectifyValue[2] = event.values[2];
        }
        
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        //workaround for Gygro sensor HW limitation.
        //As sensor continues to report small movement, wrongly
        //indicating that the phone is slowly moving, we should
        //filter the small movement.
        final float xSmallRotateTH = 0.03f;
        //xSmallRotateTH indicating the threshold of max "small
        //rotation". This varible is determined by experiments
        //based on MT6575 platform. May be adjusted on other chips.
        if (Math.abs(event.values[0]) < xSmallRotateTH) {
            return;
        }

        if (!mResponsibility) {
            Log.d(TAG,"onSensorChanged:not ready to change bitmap");
            return;
        }
        mValue = event.values[0] + OFFSET;
        if (timestamp != 0 && Math.abs(mValue) > TH) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            angle[1] += mValue * dT * 180 / Math.PI;
            if (mFirstTime) {
                angle[0] = angle[1] - BASE_ANGLE;
                angle[2] = angle[1] + BASE_ANGLE;
                mFirstTime = false;
            } else if (angle[1] <= angle[0]) {
                angle[0] = angle[1];
                angle[2] = angle[0] + 2 * BASE_ANGLE;
            } else if (angle[1] >= angle[2]) {
                angle[2] = angle[1];
                angle[0] = angle[2] - 2 * BASE_ANGLE;
            }
            if (mBitmapArr != null) {
                int index = (int) (angle[1] - angle[0]) * mBitmapArr.length / (2 * BASE_ANGLE);
                if (index >= 0 && index < mBitmapArr.length) {
                    if (mLastIndex == 0xFFFF || mLastIndex != index) {
                        Log.i(TAG, "setImageBitmap: bitmap[" + (mBitmapArr.length - index - 1) + "]");
                        setImageBitmap(mBitmapArr[mBitmapArr.length - index -1]);
                        mLastIndex = index;
                    }
                }
            }

//            Log.i(TAG, "angle[0]: " + angle[0] + " angle[1]: " + angle[1] + " angle[2]" + angle[2]);
        }
        timestamp = event.timestamp;

    }
    
    private float mValue = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp = 0;
    private float angle[] = {0,0,0};
    private static final float TH = 0.001f;
    private static final float OFFSET = 0.0f;
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        if (!checkSelf() || !checkBitmap(bm)) {
            Log.w(TAG,"setImageBitmap:either Bitmap or ImageView's dimen invalid");
            return;
        }
        //set imageView's drawable
        super.setImageBitmap(bm);
        //change imageView's matrix
        setBitmapMatrix(bm);
    }
    
    public void setBitmapArr(Bitmap[] bitmapArr) {
        mBitmapArr = bitmapArr;
    }

    private boolean checkBitmap(Bitmap bitmap) {
        if (null == bitmap) {
            Log.w(TAG,"checkBitmap:in passed Bitmap is null!");
            return false;
        }

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        if (w <= 0 || h <= 0) {
            Log.w(TAG,"checkBitmap:invalid dimension of Bitmap!");
            return false;
        }

        return true;
    }

    private boolean checkSelf() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) {
            Log.w(TAG,"checkSelf:invalid dimension of ImageView!");
            return false;
        }

        return true;
    }

    private void setBitmapMatrix(Bitmap bitmap) {
        if (!checkSelf() || !checkBitmap(bitmap)) {
            Log.w(TAG,"setBitmapMatrix:either Bitmap or ImageView's dimen invalid");
            mBaseMatrix.reset();
            setImageMatrix(mBaseMatrix);
            return;
        }

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();

        float widthScale = viewWidth / w;
        float heightScale = viewHeight / h;

        float scale = Math.min(widthScale, heightScale);

        mBaseMatrix.reset();
        mBaseMatrix.postScale(scale, scale);
        mBaseMatrix.postTranslate(
                (viewWidth - w * scale) / 2F,
                (viewHeight - h * scale) / 2F);
        //set ImageView's matrix
        setImageMatrix(mBaseMatrix);
    }
    
    public void resycleBitmapArr() {
        if (mBitmapArr != null) {
            for (int i = 0; i < mBitmapArr.length; ++i) {
                Log.i(TAG, "bitmap[" + i + "] is recycled");
                if (mBitmapArr[i] != null) {
                    mBitmapArr[i].recycle();
                    mBitmapArr[i] = null;
                }
            }
        }
    }

}
