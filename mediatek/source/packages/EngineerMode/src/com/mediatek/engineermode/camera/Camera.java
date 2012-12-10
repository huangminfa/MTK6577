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

package com.mediatek.engineermode.camera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.xlog.Xlog;
import com.mediatek.engineermode.R;

public class Camera extends Activity implements OnItemClickListener {

	private static final int DIALOG_AF = 1;
	// private static final int DIALOG_RAW_CAPTURE = 2;
	private static final int DIALOG_RAW_CAPTURE_MODE = 2;
	private static final int DIALOG_RAW_CAPTURE_TYPE = 3;
	private static final int DIALOG_ANTI_FLICKER = 4;
	private static final int DIALOG_SET_STEP = 5;
	private static final int DIALOG_ISO = 6;
	private int mMode = 0;
	private int mRawCaptureMode = 1;
	private int mRawCaptureType = 0;
	private String mAntiFlicker = "50";
	private String mISO = "AUTO";
	private int mStep = 1;
	private String TAG = "EM/Camera";

	private Intent intent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Xlog.i(TAG, "Camera->onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		ListView Camera_listView = (ListView) findViewById(R.id.ListView_Camera);

		List<String> items = new ArrayList<String>();
		items.add("AF EM");
		items.add("Raw Capture Mode");
		items.add("Raw Type");
		items.add("Flicker");
		items.add("ISO");
		items.add("Start Preview");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		Camera_listView.setAdapter(adapter);
		Camera_listView.setOnItemClickListener(this);
		setPreferencesTodefault();
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case 0:
			showDialog(DIALOG_AF);
			break;
		case 1:
			showDialog(DIALOG_RAW_CAPTURE_MODE);
			break;
		case 2:
			showDialog(DIALOG_RAW_CAPTURE_TYPE);
			break;
		case 3:
			showDialog(DIALOG_ANTI_FLICKER);
			break;
		case 4:
			showDialog(DIALOG_ISO);
			break;
		case 5:
			Xlog.v(TAG, "before start Camera_Preview");
			if (null == intent) {// not select mode yet
				intent = new Intent();
			}
			intent.setClass(this, Camera_Preview.class);
			final SharedPreferences preferences = this.getSharedPreferences(
					"camera_settings", android.content.Context.MODE_PRIVATE);

			intent.putExtra("AFMode", preferences.getInt("AFMode", 0));
			Xlog.v(TAG, "AFMode has been set to "
					+ intent.getIntExtra("AFMode", 0));
			intent.putExtra("AFStep", preferences.getInt("AFStep", 0));
			Xlog.v(TAG, "AFStep has been set to "
					+ intent.getIntExtra("AFStep", 0));
			intent.putExtra("RawCaptureMode", preferences.getInt(
					"RawCaptureMode", 1));
			Xlog.v(TAG, "RawCaptureMode has been set to "
					+ intent.getIntExtra("RawCaptureMode", 1));
			intent.putExtra("RawType", preferences.getInt("RawType", 0));
			Xlog.v(TAG, "RawType has been set to "
					+ intent.getIntExtra("RawType", 0));
			intent.putExtra("AntiFlicker", preferences.getString("AntiFlicker",
					"50"));
			Xlog.v(TAG, "AntiFlicker has been set to "
					+ intent.getStringExtra("AntiFlicker"));
			intent.putExtra("ISO", preferences.getString("ISO", "AUTO"));
			Xlog.v(TAG, "ISO has been set to " + intent.getStringExtra("ISO"));
			
			this.startActivity(intent);
			Xlog.v(TAG, "after start Camera_Preview ");
			intent = null;// in order to let user must select one mode before
			break;
		}
	}

	private void setPreferencesTodefault() {
		Xlog.v(TAG, "Camera->setPreferencesTodefault()");
		final SharedPreferences preferences = getSharedPreferences(
				"camera_settings",
				android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("AFStep", 0);
		editor.putInt("AFMode", 0);
		editor.putInt("RawCaptureMode",1);
		editor.putInt("RawType", 0);
		editor.putString("AntiFlicker", "50");
		editor.putString("ISO", "AUTO");
		editor.commit();
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		Builder builder;
		AlertDialog alertDlg;

		switch (id) {
		case DIALOG_SET_STEP:
			String setStepItems[] = { "1", "2", "4" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set step");
			builder.setSingleChoiceItems(setStepItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mStep = 1 << whichButton;
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("AFStep", mStep);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putInt("AFStep", mStep);
								editor.commit();
								Xlog.i(TAG, "AFStep : " + mStep);
							} catch (NullPointerException ne) {
								Xlog.i(TAG, ne.toString());
							}

						}
					});
			alertDlg = builder.create();
			return alertDlg;
		case DIALOG_AF:
			String setRouteItems[] = { "Normal AF", "AF Bracket", "Full Scan",
					"Full Scan Repeat", "AF Repeat", "AF and Full Repeat" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set AF Mode");
			builder.setSingleChoiceItems(setRouteItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							mMode = whichButton;
							Xlog.i(TAG, "Set AF Route has choice "
									+ String.valueOf(mMode));
							if (mMode == 1) {
								showDialog(DIALOG_SET_STEP);
							}
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("TestMode", 1);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								if (0 == mMode) { // Normal AF
									mStep = 0;
									editor.putInt("AFStep", mStep);
								}  else if (1 == mMode) {
									
								} else {
									mStep = 1;
									editor.putInt("AFStep", mStep);
								}
								editor.putInt("AFMode", mMode);
								editor.commit();
								Xlog.i(TAG, "AF mode :" + mMode);
								Xlog.i(TAG, "AF step :" + mStep);
							} catch (NullPointerException ne) {
								Xlog.i(TAG, ne.toString());
							}
						}
					});
			alertDlg = builder.create();
			return alertDlg;

		case DIALOG_RAW_CAPTURE_MODE:
			String setModeItems[] = { "Preview Mode", "Capture Mode",
					"JPEG Only" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set Raw Capture Mode");
			builder.setSingleChoiceItems(setModeItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							mRawCaptureMode = whichButton + 1;
							Xlog.i(TAG, "Set Raw Capture Mode has choice "
									+ String.valueOf(mRawCaptureMode));
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("RawCaptureMode",
								// mRawCaptureMode);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								editor
										.putInt("RawCaptureMode",
												mRawCaptureMode);
								editor.commit();
								Xlog.i(TAG, "Raw Capture mode :"
										+ mRawCaptureMode);
							} catch (NullPointerException ne) {
								Xlog.i(TAG, ne.toString());
							}
						}
					});
			alertDlg = builder.create();
			return alertDlg;

		case DIALOG_RAW_CAPTURE_TYPE:
			String setTypeItems[] = { "Processed Raw", "Pure Raw" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set Raw Type");
			builder.setSingleChoiceItems(setTypeItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							mRawCaptureType = whichButton;
							Xlog.i(TAG, "Set Raw Type has choice "
									+ String.valueOf(mRawCaptureType));
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("RawType", mRawCaptureType);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putInt("RawType", mRawCaptureType);
								editor.commit();
								Xlog.i(TAG, "Raw Type :" + mRawCaptureType);
							} catch (NullPointerException ne) {
								Xlog.e(TAG, ne.toString());
							}
						}
					});
			alertDlg = builder.create();
			return alertDlg;
		case DIALOG_ANTI_FLICKER:
			String flickerItems[] = { "50HZ", "60HZ" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set Flicker");
			builder.setSingleChoiceItems(flickerItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							switch (whichButton) {
							case 0:
								mAntiFlicker = "50";
								break;
							case 1:
								mAntiFlicker = "60";
								break;
							}
							Xlog
									.i(TAG, "Set Flicker has choice "
											+ mAntiFlicker);
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("AntiFlicker", mAntiFlicker);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putString("AntiFlicker", mAntiFlicker);
								editor.commit();
								Xlog.i(TAG, "intent's mAntiFlicker = "
										+ mAntiFlicker);
							} catch (NullPointerException ne) {
								Xlog.e(TAG, ne.toString());
							}
						}
					});
			alertDlg = builder.create();
			return alertDlg;
		case DIALOG_ISO:
			String isoItems[] = { "AUTO", "ISO100", "ISO150", "ISO200",
					"ISO300", "ISO400", "ISO600", "ISO800", "ISO1600" };
			final String isoItemsValue[] = { "AUTO", "100", "150", "200",
					"300", "400", "600", "800", "1600" };
			builder = new AlertDialog.Builder(Camera.this);
			builder.setTitle("Set ISO");
			builder.setSingleChoiceItems(isoItems, 0,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							if (whichButton >= 0
									&& whichButton <= isoItemsValue.length - 1) {
								mISO = isoItemsValue[whichButton];
							} else {
								mISO = isoItemsValue[0];
								Xlog.i(TAG,
										"Out of Array length. Set mISO whichButton = "
												+ whichButton);
							}

							Xlog.i(TAG, "Set mISO has choice " + mISO);
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							// use to judge whether the click is correctly done!
							try {
								// if (intent == null)
								// intent = new Intent();
								// intent.putExtra("ISO", mISO);
								final SharedPreferences preferences = getSharedPreferences(
										"camera_settings",
										android.content.Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putString("ISO", mISO);
								editor.commit();
								Xlog.i(TAG, "intent's mISO = " + mISO);
							} catch (NullPointerException ne) {
								Xlog.i(TAG, ne.toString());
							}
						}
					});
			alertDlg = builder.create();
			return alertDlg;

		default:
			return null;
		}
	}
}
