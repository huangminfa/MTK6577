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

package com.mediatek.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDevicePicker;
import android.bluetooth.BluetoothUuid;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;

import android.net.Uri;
import android.provider.MediaStore;



import com.mediatek.bluetooth.R;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

public class BluetoothShareGatewayActivity extends Activity {
	private static final String TAG = "BluetoothShareGatewayActivity";

	private static final String KEY_INTENT = "intent";

	public static final String ACTION_DEVICE_SELECTED = "com.mediatek.bluetooth.sharegateway.action.DEVICE_SELECTED";
	public static final String EXTRA_DEVICE_ADDRESS = "com.mediatek.bluetooth.sharegateway.extra.DEVICE_ADDRESS";
	public static final String ACTION_SETTINGS = "com.mediatek.bluetooth.sharegateway.action.ACTION_SETTINGS";
	private static final String ACTION_SEND_BIP_FILES = "com.mediatek.bluetooth.sharegateway.action.ACTION_SEND_BIP_FILES";

	public static final String ACTION_SEND = "com.mediatek.bluetooth.sharegateway.action.SEND";

	private static final int BLUETOOTH_DEVICE_REQUEST = 1;

	private static BluetoothAdapter mAdapter;
	private static Intent mIntent;
	private static String mType;
        private static boolean mBip;

	private static boolean mReentry = false;

	public static final ParcelUuid[] BIP_PROFILE_UUIDS = new ParcelUuid[] { BluetoothUuid.BipResponder };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate......");

		mReentry = false;

		Intent intent = getIntent();
		String action = intent.getAction();


                Uri uri = intent.getParcelableExtra( Intent.EXTRA_STREAM);
                if ( null == uri ) {
                    Xlog.e(TAG, "uri is null");
                }
                else {
			 Xlog.v(TAG, "uri = "+uri.toString());
                    mBip = false;
                    if( "content".equals(uri.getScheme()) ) {
                        if( MediaStore.AUTHORITY.equals(uri.getAuthority()) ) {
                             mBip = true;
                        }
                    }  
                }
				
		ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		
		if ( null == uris ) {
			Xlog.e(TAG, "uris is null");
		}
		else {
			mBip = false;
			for (Uri tmpUri : uris) {
				Xlog.v(TAG, "uri = "+tmpUri.toString());
				if( "content".equals(tmpUri.getScheme()) ) {
					if( MediaStore.AUTHORITY.equals(tmpUri.getAuthority()) ) {
						mBip = true;
					}else{					
						mBip = false;
						break;
					}
				}else{					
					mBip = false;
					break;
				}  
			}
		}

		if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			mType = intent.getType();
			Xlog.v(TAG, "mType = "+mType);
			mIntent = intent;

			mAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mAdapter == null) {
				Xlog.e(TAG, "bluetooth is not started! ");
				finish();
				return;
			}

			if (mAdapter.isEnabled()) {
				Xlog.v(TAG, "bluetooth is available");

                                BluetoothDevice remoteDevice = intent.getParcelableExtra(EXTRA_DEVICE_ADDRESS);
                                Xlog.v(TAG, "Received BT device selected intent, bt device: " + remoteDevice);

                                if (null == remoteDevice) {
                                    Xlog.i(TAG, "remote device is null");
				    startDevicePicker();
                                } else {
                                    profileDispatcher(remoteDevice);
                                    finish();
                                }
			} else {
				Xlog.w(TAG, "bluetooth is not available! ");
				Xlog.v(TAG, "turning on bluetooth......");

				Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.startActivityForResult(in, BLUETOOTH_DEVICE_REQUEST);
			}

		} else if (ACTION_DEVICE_SELECTED.equals(action)) {

			Xlog.v(TAG, "return from device picker");
			if (null == mIntent) {
				if (null != savedInstanceState) {
					mIntent = savedInstanceState.getParcelable(KEY_INTENT);
					mType = mIntent.getType();
				} else {
					finish();
					return;
				}
			}

			BluetoothDevice remoteDevice = intent.getParcelableExtra(EXTRA_DEVICE_ADDRESS);
			Xlog.v(TAG, "Received BT device selected intent, bt device: " + remoteDevice);


			if (null == remoteDevice) {
				Xlog.e(TAG, "remote device is null");
			} else {
                                profileDispatcher(remoteDevice);
/*
				mIntent.putExtra(EXTRA_DEVICE_ADDRESS, remoteDevice);

				ParcelUuid[] uuids = remoteDevice.getUuids();

				if ( mBip == true &&  mType.startsWith("image") && BluetoothUuid.containsAnyUuid(uuids, BIP_PROFILE_UUIDS)) {
					Log.v(TAG, "BIP is supported");
					mIntent.setClassName("com.mediatek.bluetooth", "com.mediatek.bluetooth.bip.BipInitEntryActivity");
				} else {
					Log.v(TAG, "OPP is supported");
					if( FeatureOption.MTK_BT_PROFILE_OPP ){
						mIntent.setClassName("com.mediatek.bluetooth", "com.mediatek.bluetooth.opp.mmi.OppClientActivity");
					}
					else {
						Toast.makeText( this, R.string.bt_base_profile_feature_disabled, Toast.LENGTH_SHORT ).show();
						finish();
						return;
					}
				}
				startActivity(mIntent);
*/
			}

			finish();


		} else {
			Xlog.e(TAG, "unsupported action: " + action);
			finish();
		}
	}

	@Override
	public void onStart() {
		Xlog.v(TAG, "onStart......");
		super.onStart();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_INTENT, mIntent);
	}

	
	@Override
	public void onResume() {
		Xlog.v(TAG, "onResume......");
		super.onResume();
		if (mReentry) {
			Xlog.v(TAG, "onResume forget......");
			finish();
		}
	}
	
	@Override
	public void onPause() {
		Xlog.v(TAG, "onPause......");
		super.onPause();
		//   finish();
		//mReentry = true;
	}

	@Override
	public void onStop() {
		Xlog.v(TAG, "onStop......");
		super.onStop();
		//   finish();
		//mReentry = true;
	}

	@Override
	public void onDestroy() {
		Xlog.v(TAG, "onDestroy......");

		//unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BLUETOOTH_DEVICE_REQUEST) {
			mReentry = false;
			if (Activity.RESULT_OK == resultCode) {
				// Bluetooth device is ready
				startDevicePicker();
				//finish();
			} else {
				this.finish();
			}
		}//BLUETOOTH_DEVICE_REQUEST end
	}

	private void startDevicePicker() {		
		Intent in = new Intent(ACTION_SETTINGS);
		Bundle intentBundle = new Bundle();
		intentBundle.putBoolean("BipFlag", mBip);
		intentBundle.putString("Type",mType);
		intentBundle.putParcelable("Intent", mIntent);
		in.putExtras(intentBundle);
		sendBroadcast(in);
		Xlog.v(TAG, "Start Device Picker!");
		mReentry = true;

		Intent in_toBDP = new Intent(BluetoothDevicePicker.ACTION_LAUNCH);
		in_toBDP.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		in_toBDP.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, true);
		in_toBDP.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE, Options.APPLICATION_PACKAGE_NAME);
		in_toBDP.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS, BluetoothShareGatewayReceiver.class
				.getName());
		if(FeatureOption.MTK_BT_PROFILE_OPP)
			in_toBDP.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,BluetoothDevicePicker.FILTER_TYPE_TRANSFER);
		if(FeatureOption.MTK_BT_PROFILE_BIP)			
			in_toBDP.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE_1,BluetoothDevicePicker.FILTER_TYPE_BIP);
		startActivity(in_toBDP);
	}

        private void profileDispatcher(BluetoothDevice remoteDevice){
            mIntent.putExtra(EXTRA_DEVICE_ADDRESS, remoteDevice);
            ParcelUuid[] uuids = remoteDevice.getUuids();

            if ( FeatureOption.MTK_BT_PROFILE_BIP &&
                 mBip == true &&
                 mType.startsWith("image") &&
                 BluetoothUuid.containsAnyUuid(uuids, BIP_PROFILE_UUIDS))
            {
			Xlog.v(TAG, "BIP is supported");
			Intent in = new Intent(ACTION_SEND_BIP_FILES);
			Bundle intentBundle = new Bundle();
			intentBundle.putParcelable("Intent", mIntent);
			in.putExtras(intentBundle);
			sendBroadcast(in);
            }
            else
            {
                Xlog.v(TAG, "OPP is supported");
                if( FeatureOption.MTK_BT_PROFILE_OPP )
                {
                    mIntent.setClassName("com.mediatek.bluetooth", "com.mediatek.bluetooth.opp.mmi.OppClientActivity");
                }
                else 
                {
                    Toast.makeText( this, R.string.bt_base_profile_feature_disabled, Toast.LENGTH_SHORT ).show();
                    return;
                }
		startActivity(mIntent);
            }
        }

}
