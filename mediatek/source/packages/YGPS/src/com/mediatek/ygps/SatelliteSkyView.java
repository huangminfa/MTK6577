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

package com.mediatek.ygps;

/*
 Copyright (C) 2009  Ludwig M Brinckmann <ludwigbrinckmann@gmail.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

import com.mediatek.ygps.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.mediatek.xlog.Xlog;
import android.content.res.Resources;

public class SatelliteSkyView extends View {

	public final static String TAG = "SatelliteSkyView";
	private Paint mGridPaint;
	private Paint mTextPaint;
	private Paint mBackground;
	private Bitmap mSatelliteBitmapUsed;
	private Bitmap mSatelliteBitmapUnused;
	private Bitmap mSatelliteBitmapNoFix;

	private float mBitmapAdjustment;

	private SatelliteDataProvider mProvider = null;
	private int mSatellites = 0;
	private int[] mPrns = new int[SatelliteDataProvider.maxSatellites];
	private float[] mElevation = new float[SatelliteDataProvider.maxSatellites];
	private float[] mAzimuth = new float[SatelliteDataProvider.maxSatellites];
	private float[] mSnrs = new float[SatelliteDataProvider.maxSatellites];

	private float[] mX = new float[SatelliteDataProvider.maxSatellites];
	private float[] mY = new float[SatelliteDataProvider.maxSatellites];
	private int[] mUsedInFixMask = new int[8];

	private void computeXY() {
		for (int i = 0; i < mSatellites; ++i) {
			double theta = -(mAzimuth[i] - 90);
			double rad = theta * Math.PI / 180.0;
			mX[i] = (float) Math.cos(rad);
			mY[i] = -(float) Math.sin(rad);

			mElevation[i] = 90 - mElevation[i];
		}
	}

	public SatelliteSkyView(Context context) {
		this(context, null);
	}

	public SatelliteSkyView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SatelliteSkyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mGridPaint = new Paint();
		mGridPaint.setColor(0xFFDDDDDD);
		mGridPaint.setAntiAlias(true);
		mGridPaint.setStyle(Style.STROKE);
		mGridPaint.setStrokeWidth(1.0f);
		mBackground = new Paint();
		mBackground.setColor(0xFF4444DD);

		mTextPaint = new Paint();
		mTextPaint.setColor(0xFFFFFFFF);
		mTextPaint.setTextSize(15.0f);
		mTextPaint.setTextAlign(Align.CENTER);

		Resources res = getResources();
		if (null != res) {
			BitmapDrawable satgreen = (BitmapDrawable) res
					.getDrawable(R.drawable.satgreen);
			if (null != satgreen) {
				mSatelliteBitmapUsed = satgreen.getBitmap();
			} else {
				Xlog.i(TAG,"get BitmapDrawable getDrawable(R.drawable.satgreen) failed");
			}
			BitmapDrawable satyellow = (BitmapDrawable) res
					.getDrawable(R.drawable.satyellow);
			if (null != satyellow) {
				mSatelliteBitmapUnused = satyellow.getBitmap();
			} else {
				Xlog.i(TAG,"get BitmapDrawable getDrawable(R.drawable.satyellow)) failed");
			}
			BitmapDrawable satred = (BitmapDrawable) res
					.getDrawable(R.drawable.satred);
			if (null != satred) {
				mSatelliteBitmapNoFix = satred.getBitmap();
			} else {
				Xlog.i(TAG, "get BitmapDrawable getDrawable(xxx) failed");
			}
		} else {
			Xlog.i(TAG, "get resource getResources() failed");
		}

		// mSatelliteBitmapUsed =
		// ((BitmapDrawable)getResources().getDrawable(R.drawable.satgreen)).getBitmap();
		// mSatelliteBitmapUnused =
		// ((BitmapDrawable)getResources().getDrawable(R.drawable.satyellow)).getBitmap();
		// mSatelliteBitmapNoFix =
		// ((BitmapDrawable)getResources().getDrawable(R.drawable.satred)).getBitmap();
                if (null != mSatelliteBitmapUsed) {
		    mBitmapAdjustment = mSatelliteBitmapUsed.getHeight() / 2;
                }
	}

	void setDataProvider(SatelliteDataProvider provider) {
		mProvider = provider;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float centerY = getHeight() / 2;
		float centerX = getWidth() / 2;
		int radius;
		if (centerX > centerY) {
			radius = (int) (getHeight() / 2) - 12;
		} else {
			radius = (int) (getWidth() / 2) - 12;
		}
		final Paint gridPaint = mGridPaint;
		final Paint textPaint = mTextPaint;
		canvas.drawPaint(mBackground);
		canvas.drawCircle(centerX, centerY, radius, gridPaint);
		canvas.drawCircle(centerX, centerY, radius * 3 / 4, gridPaint);
		canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
		canvas.drawCircle(centerX, centerY, radius >> 2, gridPaint);
		canvas.drawLine(centerX, centerY - (radius >> 2), centerX, centerY
				- radius, gridPaint);
		canvas.drawLine(centerX, centerY + (radius >> 2), centerX, centerY
				+ radius, gridPaint);
		canvas.drawLine(centerX - (radius >> 2), centerY, centerX - radius,
				centerY, gridPaint);
		canvas.drawLine(centerX + (radius >> 2), centerY, centerX + radius,
				centerY, gridPaint);
		double scale = radius / 90.0;
		if (mProvider != null) {
			mSatellites = mProvider.getSatelliteStatus(mPrns, mSnrs,
					mElevation, mAzimuth, 0, 0, mUsedInFixMask);
			computeXY();
		}
		for (int i = 0; i < mSatellites; ++i) {
			if (mElevation[i] >= 90 || mAzimuth[i] < 0 || mPrns[i] <= 0) {
				continue;
			}
			double a = mElevation[i] * scale;
			int x = (int) Math.round(centerX + (mX[i] * a) - mBitmapAdjustment);
			int y = (int) Math.round(centerY + (mY[i] * a) - mBitmapAdjustment);
//			if (0 == (mUsedInFixMask[0]) || mSnrs[i] <= 0) { // red
			if (!isUsedInFix(0) || mSnrs[i] <= 0) {
				canvas.drawBitmap(mSatelliteBitmapNoFix, x, y, gridPaint);
				// } else if (0 != (mUsedInFixMask[0] & (1<<(32-mPnrs[i])))){
//			} else if (0 != (mUsedInFixMask[0] & (1 << (mPrns[i] - 1)))) { // green
			} else if (isUsedInFix(mPrns[i])) {
				canvas.drawBitmap(mSatelliteBitmapUsed, x, y, gridPaint);
			} else { // yellow
				canvas.drawBitmap(mSatelliteBitmapUnused, x, y, gridPaint);
			}
			canvas.drawText(Integer.toString(mPrns[i]), x, y, textPaint);
		}
	}
	
    private boolean isUsedInFix(int prn) {
        boolean result = false;
        if (0 >= prn) {
            for (int mask : mUsedInFixMask) {
                if (0 != mask) {
                    result = true;
                    break;
                }
            }
        } else {
            prn = prn - 1;
            int index = prn / 32;
            int bit = prn % 32;
            result = (0 != (mUsedInFixMask[index] & (1 << bit)));
        }
        return result;
    }
}
