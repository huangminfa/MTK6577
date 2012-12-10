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
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.View;

import java.util.ArrayList;
import java.util.Vector;

import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.os.Environment;
import android.app.AlertDialog;
import java.io.File;
import java.util.Date;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * Demonstrates wrapping a layout in a ScrollView.
 */

public class TouchScreen_MultiTouch extends Activity {
	public static final int ClearCanvas_ID = 1;
	public static final int SetPtSize_ID = 2;
	public static final int DisplayHistory_ID = 3;
	MyView v = null;
	volatile boolean mDisplayHistory = true;
	DisplayMetrics me = new DisplayMetrics();

	public int mPointSize = 1;
	private static String TAG = "EM/TouchScreen/MT";

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
		final SharedPreferences preferences = this.getSharedPreferences(
				"touch_screen_settings", android.content.Context.MODE_PRIVATE);
		String file = preferences.getString("filename", "N");
		if (!file.equals("N")) {
			final String commPath = file;
			new Thread() {
				public void run() {
					String[] cmd = { "/system/bin/sh", "-c",
							"echo [ENTER_MULTI_TOUCH] >> " + commPath }; // file
					int ret;
					try {
						ret = TouchScreen_ShellExe.execCommand(cmd);
						if (0 == ret) {
							Xlog.v(TAG, "-->onResume Start logging...");
					
						} else {
							Xlog.v(TAG, "-->onResume Logging failed!");
						}
					} catch (IOException e) {
						Xlog.e(TAG, e.toString());
					}
				}
			}.start();

		}
		mPointSize = preferences.getInt("size", 10);
		getWindowManager().getDefaultDisplay().getMetrics(me);
	}

	@Override
	public void onPause() {
		Xlog.v(TAG, "-->onPause");
		final SharedPreferences preferences = this.getSharedPreferences(
				"touch_screen_settings", android.content.Context.MODE_PRIVATE);
		String file = preferences.getString("filename", "N");
		if (!file.equals("N")) {
			String[] cmd = { "/system/bin/sh", "-c",
					"echo [LEAVE_MULTI_TOUCH] >> " + file }; // file

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
				Xlog.e(TAG, e.toString());
			}

		}

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, ClearCanvas_ID, 0, "Clean Table");
		menu.add(0, SetPtSize_ID, 0, "Set Point Size");

		menu.add(0, DisplayHistory_ID, 0, "Hide History");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (mDisplayHistory) {
			menu.getItem(2).setTitle("Hide History");
		} else {
			menu.getItem(2).setTitle("Show History");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
		case ClearCanvas_ID:
			v.Clear();
			break;
		case DisplayHistory_ID:
			if (mDisplayHistory) {
				mDisplayHistory = false;
			} else {
				mDisplayHistory = true;
			}

			v.invalidate();

			break;
		case SetPtSize_ID:
			// v.Clear();
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			new AlertDialog.Builder(this).setTitle(
					"Insert pixel size of point [1-10]").setView(input)
					.setPositiveButton("OK", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							if (input.getText() != null
									&& input.getText().toString() != "") {
								int sz;
								try {
									sz = Integer.valueOf(input.getText()
											.toString());
								} catch (NumberFormatException e) {
									return;
								}
								if (sz < 1) {
									TouchScreen_MultiTouch.this.mPointSize = 1;
								} else if (sz > 10) {
									TouchScreen_MultiTouch.this.mPointSize = 10;
								} else {
									TouchScreen_MultiTouch.this.mPointSize = sz;
								}
								final SharedPreferences preferences = TouchScreen_MultiTouch.this
										.getSharedPreferences(
												"touch_screen_settings",
												android.content.Context.MODE_PRIVATE);
								preferences.edit().putInt("size",
										TouchScreen_MultiTouch.this.mPointSize)
										.commit();

								v.invalidate();
							} else {
								Xlog.w(TAG, ">>>>>>>>>>>>>>DIALOG edit null");
							}
						}
					}).setNegativeButton("Cancel", null).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(mi);
	}

	public class MyView extends View {

		public ArrayList<Vector<Vector<TouchScreen_PointDataStruct>>> mInputIds = new ArrayList<Vector<Vector<TouchScreen_PointDataStruct>>>();
		public ArrayList<TouchScreen_PointStatusStruct> mPtsStatus = new ArrayList<TouchScreen_PointStatusStruct>();

		public MyView(Context c) {
			super(c);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			int fingerNum = mInputIds.size();
			for (int idx = 0; idx < fingerNum; idx++) {
				Vector<Vector<TouchScreen_PointDataStruct>> inputIdx = mInputIds
						.get(idx);
				Paint targetPaint = getPaint(idx);
				int N0 = inputIdx.size();
				Xlog.i(TAG, "input size: " + N0);
				for (int N = 0; N < N0; N++) {
					Vector<TouchScreen_PointDataStruct> line = inputIdx.get(N);
					int Nx = line.size();
					Xlog.i(TAG, "Line" + N + " size " + Nx);
					if (Nx > 2) {
						int lastX = line.get(0).coordinateX;
						int lastY = line.get(0).coordinateY;

						for (int i = 1; i < Nx; i++) {
							int x = line.get(i).coordinateX;
							int y = line.get(i).coordinateY;
							float fat_size = line.get(i).fat_size;
							// canvas.drawLine(lastX, lastY, x, y,
							// mTargetPaint);
							// canvas.drawPoint(lastX, lastY, mPaint);
							// canvas.drawCircle(lastX, lastY, fat_size * 100,
							// targetPaint);
							if (mDisplayHistory) {
								canvas.drawCircle(lastX, lastY, mPointSize,
										targetPaint);
							}
							// canvas.drawPoint(lastX, lastY, mTargetPaint);
							// Log.i("MTXXS", "point size: " + mPointSize);
							lastX = x;
							lastY = y;
						}
						TouchScreen_PointDataStruct last = line.get(Nx - 1);
						if (N == N0 - 1)// last line
						{
							String s = "pid " + String.valueOf(last.pid)
									+ " x=" + String.valueOf(last.coordinateX)
									+ ", y=" + String.valueOf(last.coordinateY);
							Rect rect = new Rect();
							targetPaint.getTextBounds(s, 0, s.length(), rect);

							int x = last.coordinateX - rect.width() / 2;
							int y = last.coordinateY - rect.height() * 3;

							if (x < 0) {
								x = 0;
							} else if (x > me.widthPixels - rect.width()) {
								x = me.widthPixels - rect.width();
							}

							if (y < rect.height()) {
								y = rect.height();
							} else if (y > me.heightPixels) {
								y = me.heightPixels;
							}

							canvas.drawText(s, x, y, targetPaint);
							canvas.drawCircle(last.coordinateX,
									last.coordinateY, mPointSize * 3,
									targetPaint);
						}
					}
				}
			}

		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;

			int ptIdx = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN
					|| actionCode == MotionEvent.ACTION_DOWN) {
				if (ptIdx >= mPtsStatus.size())// new point is added.
				{
					TouchScreen_PointStatusStruct pt = new TouchScreen_PointStatusStruct();
					pt.isDown = true;
					mPtsStatus.add(pt);
				} else {
					TouchScreen_PointStatusStruct pt = mPtsStatus.get(ptIdx);
					pt.isDown = true;
				}
			} else if (actionCode == MotionEvent.ACTION_POINTER_UP
					|| actionCode == MotionEvent.ACTION_UP) {
				if (ptIdx >= mPtsStatus.size())// new point is added.
				{
					TouchScreen_PointStatusStruct pt = new TouchScreen_PointStatusStruct();
					pt.isDown = false;
					mPtsStatus.add(pt);
				} else {
					TouchScreen_PointStatusStruct pt = mPtsStatus.get(ptIdx);
					pt.isDown = false;
				}
			}

			for (int idx = 0; idx < mPtsStatus.size(); idx++) {
				TouchScreen_PointStatusStruct st = mPtsStatus.get(idx);
				if (st.isDown) {
					if (!st.isNewLine) {
						Vector<TouchScreen_PointDataStruct> newLine = new Vector<TouchScreen_PointDataStruct>();
						if (idx >= mInputIds.size()) {
							mInputIds
									.add(new Vector<Vector<TouchScreen_PointDataStruct>>());
						}
						mInputIds.get(idx).add(newLine);
						st.isNewLine = true;
					}
				} else {
					st.isNewLine = false;
				}
			}

			int pointCt = event.getPointerCount();
			// Log.i("MTXX", "Pointer counts = "+pointCt);
			for (int i = 0; i < pointCt; i++) {
				int notZeroBasedPid = event.getPointerId(i);
				calcMinId(notZeroBasedPid);
				int pid = notZeroBasedPid - mMinPtrId;

				if (true) {// ptDown
					try{
						TouchScreen_PointDataStruct n = new TouchScreen_PointDataStruct();
						// Log.i("MTXX", "new pointDataStruct ok0");
						n.action = actionCode;
						n.coordinateX = (int) event.getX(i);
						n.coordinateY = (int) event.getY(i);
						n.pid = pid;
						n.pressure = event.getPressure(i);
						n.fat_size = event.getSize(pid);
						// Log.i("MTXX", "Fat size = " + n.fat_size); //always 0.0,
						// maybe not supported by driver.
						n.fat_size = n.fat_size > 0.01f ? n.fat_size : 0.01f;
						Vector<TouchScreen_PointDataStruct> currentline = mInputIds
								.get(pid).get(mInputIds.get(pid).size() - 1);
						currentline.add(n);
					}catch(Exception e){
						Xlog.i(TAG,  "get point data fail!!");
						Xlog.i(TAG,  e.toString());
					}

					// mLine0.add(n);
					// Log.i("MTXX", "line0 add ok0");
				}

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
			for (Vector<Vector<TouchScreen_PointDataStruct>> inputId : mInputIds) {
				for (Vector<TouchScreen_PointDataStruct> m : inputId) {
					m.clear();
				}
				inputId.clear();
			}
			mPtsStatus.clear();
			invalidate();
		}

		Paint getPaint(int idx) {
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
			int textsize = (int) (mPointSize * 3.63 + 7.37);
			paint.setTextSize(textsize);
			return paint;
		}

	}

}
