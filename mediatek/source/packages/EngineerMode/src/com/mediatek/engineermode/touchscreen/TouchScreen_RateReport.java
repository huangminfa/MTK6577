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

package com.mediatek.engineermode.touchscreen;

import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.View;

import java.util.ArrayList;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Window;

/**
 * Demonstrates wrapping a layout in a ScrollView.
 */

public class TouchScreen_RateReport extends Activity {

	MyView v = null;
	DisplayMetrics me = new DisplayMetrics();

	private static String TAG = "EM/TouchScreen/RR";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(new MyView(this));

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(v = new MyView(this));
	}

	@Override
	public void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();
		getWindowManager().getDefaultDisplay().getMetrics(me);
	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");
		super.onPause();
	}

	public class MyView extends View {

		private ArrayList<PointerData> mPtsStatus = new ArrayList<PointerData>();
		private int mPointerNumDetected = 0;

		public MyView(Context c) {
			super(c);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			Xlog.v(TAG, "-->onDraw");
			int textsize = 15;
			canvas.drawText("Pointer number detected: "
					+ String.valueOf(mPointerNumDetected), 3, textsize + 10,
					getPaint(4, textsize));

			for (int idx = 0; idx < mPtsStatus.size(); idx++) {
				PointerData pt = mPtsStatus.get(idx);

				pt.SetUTimeStamp();
				pt.CalculateRate();
				String s = String.format("pid=%2d, X=%3d, Y=%3d.", pt.pid,
						pt.lastX, pt.lastY);
				String ss = String.format("Rate=%dHz, Count=%d, Time=%dms",
						pt.rate, pt.cnt, pt.mills);

				int x = 3;
				int y = 10 + (textsize * 3) + idx * 3 * textsize;

				canvas.drawText(s, x, y, getPaint(idx, textsize));
				canvas.drawText(ss, x, y + textsize, getPaint(idx, textsize));

			}

		}

		private class PointerData {
			volatile private boolean isDown;
			volatile private long downTime;
			volatile private long upTime;
			volatile public int cnt;
			volatile public int rate;
			volatile public int mills;
			volatile public int pid;
			volatile public int lastX;
			volatile public int lastY;

			public void SetDTimeStamp() {
				downTime = System.currentTimeMillis();
			}

			public void SetUTimeStamp() {
				upTime = System.currentTimeMillis();
			}

			public void CalculateRate() {
				if ((mills = (int) (upTime - downTime)) != 0)
					rate = (int) ((1000L * cnt) / mills);
				else
					rate = -1;
			}

			public void Clean() {
				downTime = 0;
				upTime = 0;
				;
				cnt = 0;
				pid = 0;
				rate = 0;
				isDown = false;
			}

		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			Xlog.v(TAG, "-->onTouchEvent");
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;

			int ptIdx = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN
					|| actionCode == MotionEvent.ACTION_DOWN) {
				PointerData pt = null;
				if (ptIdx >= mPtsStatus.size())// new point is added.
				{
					pt = new PointerData();
					mPtsStatus.add(pt);
				} else {
					pt = mPtsStatus.get(ptIdx);
				}
				pt.Clean();
				pt.pid = ptIdx;
				pt.SetDTimeStamp();
				pt.isDown = true;
			} else if (actionCode == MotionEvent.ACTION_POINTER_UP
					|| actionCode == MotionEvent.ACTION_UP) {
				PointerData pt = null;
				if (ptIdx >= mPtsStatus.size())// new point is added.
				{
					// never happened.
					pt = new PointerData();
					mPtsStatus.add(pt);
				} else {
					pt = mPtsStatus.get(ptIdx);
				}
				pt.SetUTimeStamp();
				pt.isDown = false;
				// pt.CalculateRate();
			}
			if (actionCode == MotionEvent.ACTION_UP) {
				mPointerNumDetected = 0;
			}

			int pointCt = event.getPointerCount();
			if (actionCode == MotionEvent.ACTION_UP) {
				mPointerNumDetected = 0;
			} else {
				mPointerNumDetected = pointCt;
			}
			// Log.i("MTXX", "Pointer counts = "+pointCt);
			Xlog.v(TAG, "Pointer counts = "+pointCt + " mPtsStatus.size()= " + mPtsStatus.size());
			
			try {
				for (int i = 0; i < pointCt; i++) {
					
					if (i < mPtsStatus.size())// new point is added.
					{				
						int notZeroBasedPid = event.getPointerId(i);
						calcMinId(notZeroBasedPid);
						int pid = notZeroBasedPid - mMinPtrId;
		
						PointerData pt = mPtsStatus.get(pid);
						pt.cnt++;
						pt.lastX = (int) event.getX(i);
						pt.lastY = (int) event.getY(i);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				Xlog.d(TAG, e.getMessage());
			}


			invalidate();
			return true;
		}

		private int mMinPtrId = -1;

		private void calcMinId(int currentId) {
			if (mMinPtrId == -1) {
				mMinPtrId = currentId;
			} else {
				mMinPtrId = mMinPtrId < currentId ? mMinPtrId : currentId;
			}
		}

		public void Clear() {
			mPtsStatus.clear();
			invalidate();
		}

		Paint getPaint(int idx, int textsize) {
			final int[][] RGB = { { 255, 0, 0 }, { 0, 255, 0 }, { 0, 0, 255 },
					{ 255, 255, 0 }, { 0, 255, 255 }, { 255, 0, 255 },
					{ 255, 255, 255 }, };
			Paint paint = new Paint();
			paint.setAntiAlias(false);
			if (idx < 7) {
				paint.setARGB(255, RGB[idx][0], RGB[idx][1], RGB[idx][2]);
			} else {
				paint.setARGB(255, 255, 255, 255);
			}
			// 60=a*10+b;
			// int textsize = (int)(10);
			paint.setTextSize(textsize);
			return paint;
		}

	}

}
