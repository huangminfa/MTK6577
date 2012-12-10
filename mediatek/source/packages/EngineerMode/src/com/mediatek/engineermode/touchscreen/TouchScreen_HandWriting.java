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

import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetricsInt;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.VelocityTracker;
import android.view.View;

import java.util.ArrayList;
import android.util.DisplayMetrics;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.os.Environment;
import android.app.AlertDialog;
import java.io.File;
import java.util.Date;
import java.io.IOException;
import android.content.DialogInterface;
import java.text.SimpleDateFormat;

/**
 * Demonstrates wrapping a layout in a ScrollView.
 */

public class TouchScreen_HandWriting extends Activity {

	public static final int ClearCanvas_ID = Menu.FIRST;
	MyView v = null;

	private int mZoom = 1;
	private String TAG = "EM/TouchScreen/HW";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(new MyView(this));

		Xlog.v(TAG, "onCreate start");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(v = new MyView(this));
		Xlog.v(TAG, "onCreate success");

	}

	@Override
	public void onResume() {
		Xlog.v(TAG, "-->onResume");
		super.onResume();
		final SharedPreferences preferences = this.getSharedPreferences(
				"touch_screen_settings", android.content.Context.MODE_PRIVATE);
		String file = preferences.getString("filename", "N");
		if (!file.equals("N")) {
			String[] cmd = { "/system/bin/sh", "-c",
					"echo [ENTER_HAND_WRITING] >> " + file }; // file

			int ret;
			try {
				ret = TouchScreen_ShellExe.execCommand(cmd);
				if (0 == ret) {
					Toast.makeText(this, "Start logging...", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(this, "Logging failed!", Toast.LENGTH_LONG)
							.show();
				}
			} catch (IOException e) {
				Xlog.w(TAG, e.toString());
			}

		}

	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");
		final SharedPreferences preferences = this.getSharedPreferences(
				"touch_screen_settings", android.content.Context.MODE_PRIVATE);
		String file = preferences.getString("filename", "N");
		if (!file.equals("N")) {
			String[] cmd = { "/system/bin/sh", "-c",
					"echo [LEAVE_HAND_WRITING] >> " + file }; // file

			int ret;
			try {
				ret = TouchScreen_ShellExe.execCommand(cmd);
				if (0 == ret) {
					Toast.makeText(this, "Stop logging...", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(this, "Logging failed!", Toast.LENGTH_LONG)
							.show();
				}
			} catch (IOException e) {
				Xlog.w(TAG, e.toString());
			}

		}

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ClearCanvas_ID, 0, "Clean Table.");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
		case ClearCanvas_ID:
			v.Clear();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(mi);
	}

	public class PT {
		public Float x;
		public Float y;

		public PT(Float x, Float y) {
			this.x = x;
			this.y = y;
		}
	};

	public class MyView extends View {
		private final Paint mTextPaint;
		private final Paint mTextBackgroundPaint;
		private final Paint mTextLevelPaint;
		private final Paint mPaint;
		private final Paint mTargetPaint;
		private final FontMetricsInt mTextMetrics = new FontMetricsInt();
		public ArrayList<ArrayList<PT>> mLines = new ArrayList<ArrayList<PT>>();
		ArrayList<PT> curLine;
		public ArrayList<VelocityTracker> mVelocityList = new ArrayList<VelocityTracker>();
		private int mHeaderBottom;
		private boolean mCurDown;
		private int mCurX;
		private int mCurY;
		private float mCurPressure;
		private int mCurWidth;
		private VelocityTracker mVelocity;

		public MyView(Context c) {
			super(c);

			DisplayMetrics dm = new DisplayMetrics();
			dm = TouchScreen_HandWriting.this.getApplicationContext()
					.getResources().getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			if ((480 == screenWidth && 800 == screenHeight)
					|| (800 == screenWidth && 480 == screenHeight)) {
				mZoom = 2;
			}

			mTextPaint = new Paint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setTextSize(10 * mZoom);
			mTextPaint.setARGB(255, 0, 0, 0);
			mTextBackgroundPaint = new Paint();
			mTextBackgroundPaint.setAntiAlias(false);
			mTextBackgroundPaint.setARGB(128, 255, 255, 255);
			mTextLevelPaint = new Paint();
			mTextLevelPaint.setAntiAlias(false);
			mTextLevelPaint.setARGB(192, 255, 0, 0);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setARGB(255, 255, 255, 255);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(2);
			mTargetPaint = new Paint();
			mTargetPaint.setAntiAlias(false);
			mTargetPaint.setARGB(192, 0, 255, 0);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(1);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			mTextPaint.getFontMetricsInt(mTextMetrics);
			mHeaderBottom = -mTextMetrics.ascent + mTextMetrics.descent + 2;
			Xlog.v(TAG, "Metrics: ascent=" + mTextMetrics.ascent + " descent="
					+ mTextMetrics.descent + " leading=" + mTextMetrics.leading
					+ " top=" + mTextMetrics.top + " bottom="
					+ mTextMetrics.bottom);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int w = getWidth() / 5;
			int base = -mTextMetrics.ascent + 1;
			int bottom = mHeaderBottom;
			canvas.drawRect(0, 0, w - 1, bottom, mTextBackgroundPaint);
			canvas.drawText("X: " + mCurX, 1, base, mTextPaint);

			canvas.drawRect(w, 0, (w * 2) - 1, bottom, mTextBackgroundPaint);
			canvas.drawText("Y: " + mCurY, 1 + w, base, mTextPaint);

			canvas
					.drawRect(w * 2, 0, (w * 3) - 1, bottom,
							mTextBackgroundPaint);
			canvas.drawRect(w * 2, 0, (w * 2) + (mCurPressure * w) - 1, bottom,
					mTextLevelPaint);
			canvas.drawText("Pres: " + mCurPressure, 1 + w * 2, base,
					mTextPaint);

			canvas
					.drawRect(w * 3, 0, (w * 4) - 1, bottom,
							mTextBackgroundPaint);
			int Xvelocity = mVelocity == null ? 0 : (int) (Math.abs(mVelocity
					.getXVelocity()) * 1000);
			canvas.drawText("XVel: " + Xvelocity, 1 + w * 3, base, mTextPaint);

			canvas.drawRect(w * 4, 0, getWidth(), bottom, mTextBackgroundPaint);
			int Yvelocity = mVelocity == null ? 0 : (int) (Math.abs(mVelocity
					.getYVelocity()) * 1000);
			canvas.drawText("YVel: " + Yvelocity, 1 + w * 4, base, mTextPaint);

			int lineSz = mLines.size();
			int k = 0;
			for (k = 0; k < lineSz; k++) {
				ArrayList<PT> m = mLines.get(k);

				float lastX = 0, lastY = 0;
				mPaint.setARGB(255, 0, 255, 255);
				int sz = m.size();
				int i = 0;
				for (i = 0; i < sz; i++) {
					PT n = m.get(i);
					if (i > 0) {
						canvas.drawLine(lastX, lastY, n.x, n.y, mTargetPaint);
						canvas.drawPoint(lastX, lastY, mPaint);
					}

					lastX = n.x;
					lastY = n.y;
				}

				VelocityTracker v = mVelocityList.get(k);
				if (v != null) {
					mPaint.setARGB(255, 255, 0, 0);
					float xVel = v.getXVelocity() * (1000 / 60);
					float yVel = v.getYVelocity() * (1000 / 60);
					canvas.drawLine(lastX, lastY, lastX + xVel, lastY + yVel,
							mPaint);
				} else {
					canvas.drawPoint(lastX, lastY, mPaint);
				}

				if (mCurDown) {
					canvas.drawLine(0, (int) mCurY, getWidth(), (int) mCurY,
							mTargetPaint);
					canvas.drawLine((int) mCurX, 0, (int) mCurX, getHeight(),
							mTargetPaint);
					int pressureLevel = (int) (mCurPressure * 255);
					mPaint
							.setARGB(255, pressureLevel, 128,
									255 - pressureLevel);
					canvas.drawPoint(mCurX, mCurY, mPaint);
					canvas.drawCircle(mCurX, mCurY, mCurWidth, mPaint);
				}

			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {

				mVelocity = VelocityTracker.obtain();
				mVelocityList.add(mVelocity);

				curLine = new ArrayList<PT>();
				mLines.add(curLine);
			}
			mVelocity.addMovement(event);
			mVelocity.computeCurrentVelocity(1);
			final int N = event.getHistorySize();
			for (int i = 0; i < N; i++) {
				curLine.add(new PT(event.getHistoricalX(i), event
						.getHistoricalY(i)));
			}
			curLine.add(new PT(event.getX(), event.getY()));
			mCurDown = action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE;
			mCurX = (int) event.getX();
			mCurY = (int) event.getY();
			mCurPressure = event.getPressure();
			mCurWidth = (int) (event.getSize() * (getWidth() / 3));

			invalidate();
			return true;
		}

		public void Clear() {
			for (ArrayList<PT> m : mLines) {
				m.clear();
			}
			mLines.clear();
			mVelocityList.clear();
			invalidate();
		}

	}
}
