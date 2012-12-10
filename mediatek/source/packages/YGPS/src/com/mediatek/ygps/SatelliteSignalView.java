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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class SatelliteSignalView extends View {

	private Paint mLinePaint;
	private Paint mThinLinePaint;
	private Paint mBarPaintUsed;
	private Paint mBarPaintUnused;
	private Paint mBarPaintNoFix;
	private Paint mBarOutlinePaint;
	private Paint mTextPaint;
	private Paint mBackground;
	
	
	private SatelliteDataProvider mProvider = null;
	private int mSatellites = 0;
	private int[] mPrns = new int[SatelliteDataProvider.maxSatellites];
	private float[] mSnrs = new float[SatelliteDataProvider.maxSatellites];
	private int[] mUsedInFixMask = new int[8];

	public SatelliteSignalView(Context context) {
		this(context, null);
	}
	public SatelliteSignalView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SatelliteSignalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mLinePaint = new Paint();
		mLinePaint.setColor(0xFFDDDDDD);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(1.0f);

		mThinLinePaint = new Paint(mLinePaint);
		mThinLinePaint.setStrokeWidth(0.5f);
		
		mBarPaintUsed = new Paint();
		mBarPaintUsed.setColor(0xFF00BB00);
		mBarPaintUsed.setAntiAlias(true);
		mBarPaintUsed.setStyle(Style.FILL);
		mBarPaintUsed.setStrokeWidth(1.0f);

		mBarPaintUnused = new Paint(mBarPaintUsed);
		mBarPaintUnused.setColor(0xFFFFCC33);
	
		mBarPaintNoFix = new Paint(mBarPaintUsed);
		mBarPaintNoFix.setStyle(Style.STROKE);
	
		mBarOutlinePaint = new Paint();
		mBarOutlinePaint.setColor(0xFFFFFFFF);
		mBarOutlinePaint.setAntiAlias(true);
		mBarOutlinePaint.setStyle(Style.STROKE);
		mBarOutlinePaint.setStrokeWidth(1.0f);

		mTextPaint = new Paint();
		mTextPaint.setColor(0xFFFFFFFF);
		mTextPaint.setTextSize(10.0f);
		mTextPaint.setTextAlign(Align.CENTER);
		mBackground = new Paint();
		mBackground.setColor(0xFF222222);
		
	}
	
	void setDataProvider(SatelliteDataProvider provider){
		mProvider = provider;
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        final int height = getHeight();
        final float rowHeight = (float) Math.floor(height / 5.0);
        final float baseline = height - rowHeight + 5;
        final float maxHeight = rowHeight * 4;
        final float scale = maxHeight / 100.0F;

        if (null != mProvider) {
            mSatellites = mProvider.getSatelliteStatus(mPrns, mSnrs, null,
                    null, 0, 0, mUsedInFixMask);
            for (int i = 0; i < mSatellites; i++) {
                if (mSnrs[i] < 0) {
                    mSnrs[i] = 0;
                }
            }
        }
        int devide = 15;
        if (mSatellites > 32) {
            devide = mSatellites;
        } else if (mSatellites > 30) {
            devide = 32;
        } else if (mSatellites > 25) {
            devide = 30;
        } else if (mSatellites > 20) {
            devide = 25;
        } else if (mSatellites > 15) {
            devide = 20;
        }
        final float slotWidth = (float) Math.floor(width / devide);
        final float barWidth = slotWidth / 4 * 3;
        final float fill = slotWidth - barWidth;
        float margin = (width - slotWidth * devide) / 2;

        canvas.drawPaint(mBackground);
        canvas.drawLine(0, baseline, width, baseline, mLinePaint);
        float y = baseline - (100 * scale);
        canvas.drawLine(0, y, getWidth(), y, mThinLinePaint);
        y = baseline - (50 * scale);
        canvas.drawLine(0, y, getWidth(), y, mThinLinePaint);
        y = baseline - (25 * scale);
        canvas.drawLine(0, y, getWidth(), y, mThinLinePaint);
        y = baseline - (75 * scale);
        canvas.drawLine(0, y, getWidth(), y, mThinLinePaint);
        int drawn = 0;
        for (int i = 0; i < mSatellites; i++) {
            if (0 >= mPrns[i]) {
                continue;
            }
            float left = margin + (drawn * slotWidth) + fill / 2;
            float top = baseline - (mSnrs[i] * scale);
            float right = left + barWidth;
            float center = left + barWidth / 2;
//            if (0 == mUsedInFixMask[0]) {
            if (!isUsedInFix(0)) {
                canvas.drawRect(left, top, right, baseline, mBarPaintNoFix);
//            } else if (0 != (mUsedInFixMask[0] & (1 << (mPrns[i] - 1)))) {
            } else if (isUsedInFix(mPrns[i])) {
                canvas.drawRect(left, top, right, baseline, mBarPaintUsed);
            } else {
                canvas.drawRect(left, top, right, baseline, mBarPaintUnused);
            }
            canvas.drawRect(left, top, right, baseline, mBarOutlinePaint);
            String tmp = String.format("%2.0f", mSnrs[i]);
            canvas.drawText(tmp, center, top - fill, mTextPaint);
            canvas.drawText(Integer.toString(mPrns[i]), center, baseline
                    + 8 +fill, mTextPaint);
            drawn += 1;
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
