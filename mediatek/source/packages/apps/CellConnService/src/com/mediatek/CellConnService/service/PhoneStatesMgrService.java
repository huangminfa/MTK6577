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

package com.mediatek.CellConnService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.content.Context;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.AsyncResult;
import android.telephony.TelephonyManager;

import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import com.mediatek.featureoption.FeatureOption;
import android.os.ServiceManager;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.gemini.GeminiPhone;

import android.os.RemoteCallbackList;

import com.mediatek.CellConnService.IPhoneStatesCallback;
import com.mediatek.CellConnService.IPhoneStatesMgrService;
import com.mediatek.CellConnService.CellConnMgr;

public class PhoneStatesMgrService extends Service implements Runnable {

	private static final String TAG = "PhoneStatesMgrService";
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;

	public static final int PHONE_STATE_UNKNOWN = -1;

	public static final int PHONE_STATE_NORMAL = 0;
	public static final int PHONE_STATE_RADIOOFF = 1;
	public static final int PHONE_STATE_RADIOON = 2;
	public static final int PHONE_STATE_PIN1LOCKED = 3;
	public static final int PHONE_STATE_PIN1UNLOCKED = 4;
	public static final int PHONE_STATE_PUK1LOCKED = 5;
	public static final int PHONE_STATE_PUK1UNLOCKED = 6;
	public static final int PHONE_STATE_SIMMELOCKED = 7;
	public static final int PHONE_STATE_SIMMEUNLOCKED = 8;
	public static final int PHONE_STATE_ROAMING = 9;
	public static final int PHONE_STATE_NOSIM = 10;
	public static final int PHONE_STATE_SIMNOTREADY = 11;
	public static final int PHONE_STATE_FDNENABLE_MARK = 0xf000;

	public static String phoneStateToString(int phoneState) {
		switch (phoneState) {
		case PHONE_STATE_UNKNOWN:
			return "PHONE_STATE_UNKNOWN";
		case PHONE_STATE_NORMAL:
			return "PHONE_STATE_NORMAL";
		case PHONE_STATE_RADIOOFF:
			return "PHONE_STATE_RADIOOFF";
		case PHONE_STATE_RADIOON:
			return "PHONE_STATE_RADIOON";
		case PHONE_STATE_PIN1LOCKED:
			return "PHONE_STATE_PIN1LOCKED";
		case PHONE_STATE_PIN1UNLOCKED:
			return "PHONE_STATE_PIN1UNLOCKED";
		case PHONE_STATE_PUK1LOCKED:
			return "PHONE_STATE_PUK1LOCKED";
		case PHONE_STATE_PUK1UNLOCKED:
			return "PHONE_STATE_PUK1UNLOCKED";
		case PHONE_STATE_SIMMELOCKED:
			return "PHONE_STATE_SIMMELOCKED";
		case PHONE_STATE_SIMMEUNLOCKED:
			return "PHONE_STATE_SIMMEUNLOCKED";
		case PHONE_STATE_ROAMING:
			return "PHONE_STATE_ROAMING";
		case PHONE_STATE_NOSIM:
			return "PHONE_STATE_NOSIM";
		case PHONE_STATE_FDNENABLE_MARK:
			return "PHONE_STATE_FDNENABLE";
		case PHONE_STATE_SIMNOTREADY:
			return "PHONE_STATE_SIMNOTREADY";
		default:
			return "PHONE_STATE_NULL";
		}
	}

	private static final int MSG_ID_CHECKRADIO = 200;
	private static final int MSG_ID_CHECKPIN1 = 201;
	private static final int MSG_ID_CHECKPUK1 = 202;
	private static final int MSG_ID_CHECKSIMMELOCK = 203;
	private static final int MSG_ID_CHECKFDN = 204;
	private static final int MSG_ID_CHECKPIN2 = 205;
	private static final int MSG_ID_CHECKPUK2 = 206;
	private static final int MSG_ID_CHECKROAMING = 207;

	public static String msgIdToString(int msgId) {
		switch (msgId) {
		case MSG_ID_CHECKRADIO:
			return "MSG_ID_CHECKRADIO";
		case MSG_ID_CHECKPIN1:
			return "MSG_ID_CHECKPIN1";
		case MSG_ID_CHECKPUK1:
			return "MSG_ID_CHECKPUK1";
		case MSG_ID_CHECKSIMMELOCK:
			return "MSG_ID_CHECKSIMMELOCK";
		case MSG_ID_CHECKFDN:
			return "MSG_ID_CHECKFDN";
		case MSG_ID_CHECKPIN2:
			return "MSG_ID_CHECKPIN2";
		case MSG_ID_CHECKPUK2:
			return "MSG_ID_CHECKPUK2";
		case MSG_ID_CHECKROAMING:
			return "MSG_ID_CHECKROAMING";
		default:
			return "MSG_ID_NULL";
		}
	}

	public static final int REQUEST_TYPE_UNKNOWN = 300;
	// public static final int REQUEST_TYPE_RADIO = 301;
	public static final int REQUEST_TYPE_PIN1 = 302;
	// public static final int REQUEST_TYPE_PIN_SIMMELOCK = 303;
	public static final int REQUEST_TYPE_FDN = 304;
	public static final int REQUEST_TYPE_PIN2 = 305;
	public static final int REQUEST_TYPE_ROAMING = 306;

	public static String reqestTypeToString(int reqType) {
		switch (reqType) {
		case REQUEST_TYPE_UNKNOWN:
			return "REQUEST_TYPE_UNKNOWN";
		case REQUEST_TYPE_PIN1:
			return "REQUEST_TYPE_PIN1";
		case REQUEST_TYPE_FDN:
			return "REQUEST_TYPE_FDN";
		case REQUEST_TYPE_PIN2:
			return "REQUEST_TYPE_PIN2";
		case REQUEST_TYPE_ROAMING:
			return "REQUEST_TYPE_ROAMING";
		default:
			return "REQUEST_TYPE_NULL";
		}
	}

	private int mSIMCount = 0;
	private int[] mPhoneStates;
	//private int[] mSIMStatus;

	private Context mContext;

	private boolean mUserConfirmed = false;
	private ArrayList<RequestItem> mICallBackList;
	private int mPreferSlot = 0;

	// Begin: for Dialog Activity
	static PhoneStatesMgrService sInstance;
	static final String CONFIRM_TYPE = "confirm_type";
	static final String CONFIRM_CARDNAME = "confirm_cardName";
	static final String CONFIRM_SLOT = "confirm_slot";
	static final String CONFIRM_ROAMINGWITHPREFER = "confirm_roamingWithPrefer";
	static final String CONFIRM_RESULT = "confirm_result";
	static final String CONFIRM_RESULT_PREFERSLOT = "confirm_result_preferSlot";
		
	static final int CONFIRM_TYPE_RADIO = 401;
	static final int CONFIRM_TYPE_PIN = 402;
	static final int CONFIRM_TYPE_SIMMELOCK = 403;
	static final int CONFIRM_TYPE_FDN = 404;
	static final int CONFIRM_TYPE_ROAMING = 405;
	static final int CONFIRM_TYPE_SIMLOCKED = 406;
	static final int CONFIRM_TYPE_SLOTLOCKED = 407;

	public static String confirmTypeToString(int confirmType) {
		switch (confirmType) {
		case CONFIRM_TYPE_RADIO:
			return "CONFIRM_TYPE_RADIO";
		case CONFIRM_TYPE_PIN:
			return "CONFIRM_TYPE_PIN";
		case CONFIRM_TYPE_SIMMELOCK:
			return "CONFIRM_TYPE_SIMMELOCK";
		case CONFIRM_TYPE_FDN:
			return "CONFIRM_TYPE_FDN";
		case CONFIRM_TYPE_ROAMING:
			return "CONFIRM_TYPE_ROAMING";
		case CONFIRM_TYPE_SIMLOCKED:
			return "CONFIRM_TYPE_SIMLOCKED";
		case CONFIRM_TYPE_SLOTLOCKED:
			return "CONFIRM_TYPE_SLOTLOCKED";
		default:
			return "CONFIRM_TYPE_NULL";
		}
	}
	
	static final int CONFIRM_RESULT_INVALID = 450;
	static final int CONFIRM_RESULT_OK = 451;
	static final int CONFIRM_RESULT_CANCEL = 452;
	static final int CONFIRM_RESULT_DISMISS = 453;
	
	public static String confirmResultToString(int confirmResult) {
		switch (confirmResult) {
		case CONFIRM_RESULT_OK:
			return "CONFIRM_RESULT_OK";
		case CONFIRM_RESULT_CANCEL:
			return "CONFIRM_RESULT_CANCEL";
		case CONFIRM_RESULT_DISMISS:
			return "CONFIRM_RESULT_DISMISS";
		case CONFIRM_RESULT_INVALID:
			return "CONFIRM_RESULT_INVALID";
		default:
			return "CONFIRM_RESULT_NULL";
		}
	}

	public static final String START_TYPE = "start_type";
	public static final String START_TYPE_REQ = "request";
	public static final String START_TYPE_RSP = "response";

	static final String VERIFY_TYPE = "verfiy_type";
	static final String VERIFY_RESULT = "verfiy_result";
	static final int VERIFY_TYPE_PIN = 501;
	static final int VERIFY_TYPE_PUK = 502;
	static final int VERIFY_TYPE_SIMMELOCK = 503;
	static final int VERIFY_TYPE_PIN2 = 504;
	static final int VERIFY_TYPE_PUK2 = 505;

	public static String verifyTypeToString(int verifyType) {
		switch (verifyType) {
		case VERIFY_TYPE_PIN:
			return "VERIFY_TYPE_PIN";
		case VERIFY_TYPE_PUK:
			return "VERIFY_TYPE_PUK";
		case VERIFY_TYPE_SIMMELOCK:
			return "VERIFY_TYPE_SIMMELOCK";
		case VERIFY_TYPE_PIN2:
			return "VERIFY_TYPE_PIN2";
		case VERIFY_TYPE_PUK2:
			return "VERIFY_TYPE_PUK2";
		default:
			return "VERIFY_TYPE_NULL";
		}
	}

	// End: for Dialog Activity
	
    private static final int SIMLOCK_TYPE_PIN = 1;
    private static final int SIMLOCK_TYPE_SIMMELOCK = 2;
    
    private static final int UNLOCK_ICC_SML_QUERYLEFTTIMES = 110;
    private static final int GET_MELOCK_RETRYCOUNT = 111;
    
    private int mResult = CellConnMgr.RESULT_UNKNOWN;
    
    private boolean mRequestNoPrefer = false;

	static PhoneStatesMgrService getInstance() {
		return sInstance;
	}

	IPhoneStatesMgrService.Stub mBinder = new IPhoneStatesMgrService.Stub() {
		public int verifyPhoneState(int slot, int reqType,
				IPhoneStatesCallback cb) throws RemoteException {
			
			if (0 == (reqType & CellConnMgr.FLAG_SUPPRESS_CONFIRMDLG)) {
				Log.d(TAG, "verifyPhoneState suppress confirm dialog flag is false");
			    mUserConfirmed = false;
			} else {
				Log.d(TAG, "verifyPhoneState suppress confirm dialog flag is true");
				mUserConfirmed = true;
				reqType &= ~CellConnMgr.FLAG_SUPPRESS_CONFIRMDLG;
			}
			
			if (0 == (reqType & CellConnMgr.FLAG_REQUEST_NOPREFER)) {
				Log.d(TAG, "verifyPhoneState request no prefer flag is false");
				mRequestNoPrefer = false;
			} else {
				Log.d(TAG, "verifyPhoneState request no prefer flag is true");
				mRequestNoPrefer = true;
				reqType &= ~CellConnMgr.FLAG_REQUEST_NOPREFER;
			}
			
			Log.d(TAG, "verifyPhoneState slot = " + slot + " reqType = "
					+ reqestTypeToString(reqType));
			
			if (slot < 0) {
				Log.e(TAG, "verifyPhoneState slot is invalid");
				cb.onComplete(CellConnMgr.RESULT_EXCEPTION);
				return CellConnMgr.RESULT_EXCEPTION;
			}
			
			if (true != FeatureOption.MTK_GEMINI_SUPPORT) {
				slot = 0;
			}
			
			if (0 == getRetryPinCount(slot)) {
				mPhoneStates[slot] = PHONE_STATE_UNKNOWN;
			}
			
			Log.d(TAG, "verifyPhoneState mPhoneStates[" + slot + "] = " + phoneStateToString(mPhoneStates[slot]));
			
			boolean bSimReady = isSIMReady(slot);
			Log.d(TAG, "verifyPhoneState isSIMReady = " + bSimReady);

			if (bSimReady) {
				if ((PHONE_STATE_NORMAL == mPhoneStates[slot])
						|| (!isRadioOff(slot) && !pinRequest(slot)
								&& !pukRequest(slot) && !simMELockRequest(slot))) {
					Log.d(TAG, "verifyPhoneState phone state is normal");
					mPhoneStates[slot] = PHONE_STATE_NORMAL;
					if ((reqType == REQUEST_TYPE_PIN1)
							|| (REQUEST_TYPE_FDN == reqType && !fdnRequest(slot))
							|| (REQUEST_TYPE_ROAMING == reqType && !roamingRequest(slot))) {
						Log
								.d(TAG,
										"verifyPhoneState respond with RESULT_STATE_NORMAL to request");
						mPhoneStates[slot] = PHONE_STATE_NORMAL;
						if (REQUEST_TYPE_ROAMING == reqType) {
							cb.onCompleteWithPrefer(CellConnMgr.RESULT_STATE_NORMAL, slot);
						} else {
							cb.onComplete(CellConnMgr.RESULT_STATE_NORMAL);
						}
						return CellConnMgr.RESULT_STATE_NORMAL;
					}
				}
			}
			
			if (slot >= mSIMCount) {
				cb.onComplete(CellConnMgr.RESULT_ABORT);
				Log.e(TAG, "verifyPhoneState The param slot is invalid");
				return CellConnMgr.RESULT_EXCEPTION;
			}

			Log.d(TAG, "verifyPhoneState enter the phone states machine");
			if (null == cb) {
				Log.e(TAG, "verifyPhoneState call back is null");
				cb.onComplete(CellConnMgr.RESULT_EXCEPTION);
				return CellConnMgr.RESULT_EXCEPTION;
			}

			RequestItem reqItem = new RequestItem(slot, reqType, cb);
			if (null == reqItem) {
				Log.e(TAG, "verifyPhoneState reqItem is null");
				cb.onComplete(CellConnMgr.RESULT_EXCEPTION);
				return CellConnMgr.RESULT_EXCEPTION;
			}
			
			mResult = CellConnMgr.RESULT_UNKNOWN;
			synchronized (mICallBackList) {
				mICallBackList.add(reqItem);
			}
			
			if (!bSimReady && !isRadioOff(slot)) {
				Log.d(TAG, "verifyPhoneState exit");
				return CellConnMgr.RESULT_WAIT;
			}

			// setup phone states machine
			Message msg = Message.obtain(mServiceHandler, MSG_ID_CHECKRADIO);
			msg.arg1 = slot;

			if (mServiceHandler == null) {
				Log.d(TAG, "verifyPhoneState wait looper");
				waitForLooper();
			}
			
			mServiceHandler.sendMessage(msg);
			Log.d(TAG, "verifyPhoneState --");
			return CellConnMgr.RESULT_WAIT;
		}
	};

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "BroadcastReceiver onReceive");
			if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
				Log.d(TAG, "BroadcastReceiver AIRPLANE_MODE_CHANGED");
				boolean airplaneModeON = intent.getBooleanExtra("state", false);
				Log.d(TAG, "BroadcastReceiver AIRPLANE_MODE_CHANGED airplaneModeON = " + airplaneModeON);

				if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
					Log.d(TAG, "BroadcastReceiver AIRPLANE_MODE_CHANGED for gemini");
					if (airplaneModeON) {
						// All of radio off
						for (int i = 0; i < mSIMCount; ++i) {
							mPhoneStates[i] = PHONE_STATE_RADIOOFF;
						}
					} else {
						// All of radio on
						//for (int i = 0; i < mSIMCount; ++i) {
						//	mPhoneStates[i] = PHONE_STATE_RADIOON;
						//}
					    
					    // Air plane mode is off, check the dualSim mode
					    int dualSimMode = getDualSimMode();
					    Log.d(TAG, "BroadcastReceiver dualSimMode = " + dualSimMode);
					    switch (dualSimMode) {
					        case 0:
                                mPhoneStates[0] = PHONE_STATE_RADIOOFF;
                                mPhoneStates[1] = PHONE_STATE_RADIOOFF;
					            break;
					        case 1:
                                mPhoneStates[0] = PHONE_STATE_RADIOON;
                                mPhoneStates[1] = PHONE_STATE_RADIOOFF;
					            break;
					        case 2:
                                mPhoneStates[0] = PHONE_STATE_RADIOOFF;
                                mPhoneStates[1] = PHONE_STATE_RADIOON;
					            break;
					        case 3: 
                                mPhoneStates[0] = PHONE_STATE_RADIOON;
                                mPhoneStates[1] = PHONE_STATE_RADIOON;
					            break;
					        default:
                                mPhoneStates[0] = PHONE_STATE_RADIOOFF;
                                mPhoneStates[1] = PHONE_STATE_RADIOOFF;
					            break;
					    }
					}
					return;
				} else {
					Log.d(TAG, "BroadcastReceiver AIRPLANE_MODE_CHANGED single sim");
					if (airplaneModeON) {
						// radio off
						mPhoneStates[0] = PHONE_STATE_RADIOOFF;
					} else {
						// radio on
						mPhoneStates[0] = PHONE_STATE_RADIOON;
					}
				}
			} else if (Intent.ACTION_DUAL_SIM_MODE_CHANGED.equals(intent
					.getAction())) {
				Log.d(TAG, "BroadcastReceiver ACTION_DUAL_SIM_MODE_CHANGED");

				/*******************
				 * DUALSIM mode = 0: all power off DUALSIM mode = 1: SIM1 only
				 * DUALSIM mode = 2: SIM2 only DUALSIM mode = 3: all power on
				 ******************/
				// only apply for Gemini SIM1 and SIM2
				int dualSimMode = intent.getIntExtra(
						Intent.EXTRA_DUAL_SIM_MODE, 0);
				Log.d(TAG, "BroadcastReceiver duslSimMode = " + dualSimMode);

				if (0 == dualSimMode) {
					// All of radio off
					for (int i = 0; i < mSIMCount; ++i) {
						mPhoneStates[i] = PHONE_STATE_RADIOOFF;
					}
				} else if (1 == dualSimMode) {
					// SIM1 only
					mPhoneStates[0] = PHONE_STATE_RADIOON;
					mPhoneStates[1] = PHONE_STATE_RADIOOFF;
				} else if (2 == dualSimMode) {
					// SIM2 only
					mPhoneStates[0] = PHONE_STATE_RADIOOFF;
					mPhoneStates[1] = PHONE_STATE_RADIOON;
				} else if (3 == dualSimMode) {
					// All on
					mPhoneStates[0] = PHONE_STATE_RADIOON;
					mPhoneStates[1] = PHONE_STATE_RADIOON;
				}
			} else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent
					.getAction())) {
				Log.d(TAG, intent.toString() + intent
						.getStringExtra(IccCard.INTENT_KEY_ICC_STATE));
				String stateExtra = intent
						.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
				int slot = 0;
				if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
					slot = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY,
							Phone.GEMINI_SIM_1);
				}

				if (slot >= mSIMCount) {
					Log
							.e(TAG,
									"BroadcastReceiver SIM State changed slot is invalid");
					return;
				}

				if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is ICC_ABSENT");
					mPhoneStates[slot] = PHONE_STATE_NOSIM;
					broadcastCallback(slot, CellConnMgr.RESULT_STATE_NORMAL);			
					return;
				} else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is ICC_READY");
					mPhoneStates[slot] = PHONE_STATE_NORMAL;
					broadcastCallback(slot, REQUEST_TYPE_PIN1, CellConnMgr.RESULT_OK);
					return;
				} else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is ICC_LOCKED");
					final String lockedReason = intent
							.getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
					if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
						Log.d(TAG, "BroadcastReceiver slot " + slot
								+ " state is LOCKED_ON_PIN");
						mPhoneStates[slot] = PHONE_STATE_PIN1LOCKED;
						Message msg = Message.obtain(mServiceHandler,
								MSG_ID_CHECKPIN1);
						msg.arg1 = slot;
						mServiceHandler.sendMessage(msg);
						return;
					} else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK
							.equals(lockedReason)) {
						Log.d(TAG, "BroadcastReceiver slot " + slot
								+ " state is LOCKED_ON_PUK");
						mPhoneStates[slot] = PHONE_STATE_PUK1LOCKED;
						Message msg = Message.obtain(mServiceHandler,
								MSG_ID_CHECKPUK1);
						msg.arg1 = slot;
						mServiceHandler.sendMessage(msg);
						return;
					} else {
						Log.d(TAG, "BroadcastReceiver slot " + slot
								+ " state is ICC_UNKNOWN");
						mPhoneStates[slot] = PHONE_STATE_UNKNOWN;
						return;
					}
				} else if (IccCard.INTENT_VALUE_LOCKED_NETWORK
						.equals(stateExtra)) {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is LOCKED_NETWORK");
					mPhoneStates[slot] = PHONE_STATE_SIMMELOCKED;
					Message msg = Message.obtain(mServiceHandler,
							MSG_ID_CHECKSIMMELOCK);
					msg.arg1 = slot;
					mServiceHandler.sendMessage(msg);
					return;
				} else if (IccCard.INTENT_VALUE_ICC_NOT_READY
						.equals(stateExtra)) {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is NOT_READY");
					mPhoneStates[slot] = PHONE_STATE_SIMNOTREADY;
					return;
				} else {
					Log.d(TAG, "BroadcastReceiver slot " + slot
							+ " state is UNKNOWN");
					mPhoneStates[slot] = PHONE_STATE_UNKNOWN;
					return;
				}
			}
		}
	};

	private final class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Log.d(TAG, "handleMessage: process incoming message");

			RequestItem reqItem;
			synchronized (mICallBackList) {
				if (mICallBackList.isEmpty()) {
					Log.e(TAG, "handleMessage no item to handle");
					return;
				}

				reqItem = mICallBackList.get(0);
			}

			if (null == reqItem) {
				Log.e(TAG,
						"handleMessage mICallBackList the first item is null");
				return;
			}

			int handleSlot = reqItem.getReqSlot();
			if (msg.arg1 != handleSlot) {
				Log.d(TAG, "handleMessage: the handleSlot is not match the msg.args");
				return;
			}

			// process incoming messages here
			switch (msg.what) {
			case MSG_ID_CHECKRADIO: {
				Log.d(TAG, "handleMessage: check radio off");

				// TODO handle radio off
				if (PHONE_STATE_RADIOOFF == mPhoneStates[handleSlot]
						|| isRadioOff(handleSlot)) {

					Log.d(TAG, "handleMessage: radio is off");
					mPhoneStates[handleSlot] = PHONE_STATE_RADIOOFF;

					mUserConfirmed = true;
					showConfirmDlg(reqItem, CONFIRM_TYPE_RADIO);
					break;
				} else {
					mPhoneStates[handleSlot] = PHONE_STATE_RADIOON;
				}
			}

			case MSG_ID_CHECKPIN1:
				Log.d(TAG, "handleMessage: check PIN1 state");
				// TODO handle PIN1

				if (PHONE_STATE_PIN1LOCKED == mPhoneStates[handleSlot]
						|| pinRequest(handleSlot)) {
					if (!mUserConfirmed) {
						mUserConfirmed = true;
						showConfirmDlg(reqItem, CONFIRM_TYPE_PIN);
					} else {
						showVerifyDlg(reqItem, VERIFY_TYPE_PIN);
					}
					break;
				}

			case MSG_ID_CHECKPUK1:
				Log.d(TAG, "handleMessage: check PUK1 state");
				// TODO handle puk1
				if (pukRequest(handleSlot) && (0 == getRetryPukCount(handleSlot))) {
					Log.d(TAG, "handleMessage PUK no retry");
					//broadcastCallback(handleSlot, REQUEST_TYPE_PIN1, CellConnMgr.RESULT_OK);
					showConfirmDlg(reqItem, CONFIRM_TYPE_SIMLOCKED);
					break;
				}
				
				if (PHONE_STATE_PUK1LOCKED == mPhoneStates[handleSlot]
						|| pukRequest(handleSlot)) {
					if (!mUserConfirmed) {
						mUserConfirmed = true;
						showConfirmDlg(reqItem, CONFIRM_TYPE_PIN);
					} else {
						showVerifyDlg(reqItem, VERIFY_TYPE_PUK);
					}
					break;
				}

			case MSG_ID_CHECKSIMMELOCK:
				Log.d(TAG, "handleMessage: check SIM-ME lock state");
				// TODO handle SIM ME Lock
				if (simMELockRequest(handleSlot) && (0 == getRetryMELockCount(handleSlot))) {
					Log.d(TAG, "handleMessage SIM-ME lock no retry");
					showConfirmDlg(reqItem, CONFIRM_TYPE_SLOTLOCKED);
					break;
				}
				
				if (PHONE_STATE_SIMMELOCKED == mPhoneStates[handleSlot]
						|| simMELockRequest(handleSlot)) {
					if (!mUserConfirmed) {
						mUserConfirmed = true;
						showConfirmDlg(reqItem, CONFIRM_TYPE_SIMMELOCK);
					} else {
						showVerifyDlg(reqItem, VERIFY_TYPE_SIMMELOCK);
					}
					break;
				} else {
					Log.d(TAG, "handleMessage check SIM-ME lock phone state is normal");
					mPhoneStates[handleSlot] = PHONE_STATE_NORMAL;
					broadcastCallback(handleSlot, REQUEST_TYPE_PIN1, CellConnMgr.RESULT_STATE_NORMAL);
				}
				break;

			case MSG_ID_CHECKFDN:
				Log.d(TAG, "handleMessage: check MSG_ID_CHECKFDN state");

				// TODO handle FDN
				if (fdnRequest(handleSlot)) {
					showConfirmDlg(reqItem, CONFIRM_TYPE_FDN);
				} else {
					mPhoneStates[handleSlot] &= ~PHONE_STATE_FDNENABLE_MARK;
					broadcastCallback(handleSlot, REQUEST_TYPE_FDN, CellConnMgr.RESULT_OK);
				}
				break;

			case MSG_ID_CHECKPIN2:
				Log.d(TAG, "handleMessage: check PIN2 state");
				// TODO handle PIN2
				showVerifyDlg(reqItem, VERIFY_TYPE_PIN2);
				break;

			case MSG_ID_CHECKROAMING:
				Log.d(TAG, "handleMessage: check roaming");
				// TODO handle roaming
				if (roamingRequest(handleSlot)) {
					showConfirmDlg(reqItem, CONFIRM_TYPE_ROAMING);
				} else {
					if (CellConnMgr.RESULT_OK == mResult) {
						broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_ROAMING,
								CellConnMgr.RESULT_OK);
					} else {
						broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_ROAMING,
								CellConnMgr.RESULT_STATE_NORMAL);
					}
				}
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "onCreate");

		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			this.mSIMCount = 2;
		} else {
			this.mSIMCount = 1;
		}

		this.mPhoneStates = new int[this.mSIMCount];
		if (null == mPhoneStates) {
			Log.e(TAG, "onCreate new mPhoneStates failed");
			return;
		}
		
/*		this.mSIMStatus = new int[this.mSIMCount];
		if (null == mSIMStatus) {
			Log.e(TAG, "onCreate new mSIMStatus failed");
			return;
		}*/

		for (int i = 0; i < this.mSIMCount; ++i) {
			this.mPhoneStates[i] = PHONE_STATE_UNKNOWN;
		}

		IntentFilter itFilter = new IntentFilter();
		if (null == itFilter) {
			Log.e(TAG, "onCreate new intent failed");
			return;
		}

		mICallBackList = new ArrayList<RequestItem>();
		if (null == mICallBackList) {
			Log.e(TAG, "onCreate new mICallBackList failed");
			return;
		}

		Thread serviceThread = new Thread(null, this,
				"Phone States ServiceThread");
		if (null == serviceThread) {
			Log.e(TAG, "onCreate new serviceThread failed");
			return;
		}

		serviceThread.start();

		itFilter.addAction(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
		itFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		itFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

		registerReceiver(mIntentReceiver, itFilter);

		waitForLooper();
		this.mServiceHandler.sendEmptyMessage(0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStart");
		super.onStart(intent, startId);
		waitForLooper();

		// onStart() method can be passed a null intent
		if (intent == null) {
			return;
		}

		String startType = intent.getStringExtra(START_TYPE);
		if (null == startType) {
			return;
		}

		if (START_TYPE_RSP.equals(intent.getStringExtra(START_TYPE))) {
			// response
			Log.d(TAG, "onStart response");

			RequestItem reqItem;
			synchronized (mICallBackList) {
				int listSize = this.mICallBackList.size();
				if (listSize <= 0) {
					Log.d(TAG, "onStart response callback list is null");
					return;
				}
				reqItem = (RequestItem) mICallBackList.get(0);
			}

			if (null == reqItem) {
				Log.e(TAG, "onStart response reqItem is null");
				return;
			}

			int confirmType = intent.getIntExtra(CONFIRM_TYPE, 0);
			if (0 != confirmType) {
				int confirmRet = intent.getIntExtra(CONFIRM_RESULT, CONFIRM_RESULT_INVALID);
				Log.d(TAG, "confirm response and confirmType = "
						+ confirmTypeToString(confirmType) + " confirmRet = " 
						+ confirmResultToString(confirmRet));
				switch (confirmType) {
				case CONFIRM_TYPE_RADIO:
					if (CONFIRM_RESULT_OK == confirmRet) {
						this.powerRadioOn(reqItem.getReqSlot());
					} else if (CONFIRM_RESULT_CANCEL == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					} else {
						removeRequest();
					}
					break;

				case CONFIRM_TYPE_PIN:
					if (CONFIRM_RESULT_OK == confirmRet) {
						// call xu
						showVerifyDlg(reqItem, VERIFY_TYPE_PIN);
					} else if (CONFIRM_RESULT_CANCEL == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					} else {
						removeRequest();
					}
					break;

				case CONFIRM_TYPE_SIMMELOCK:
					if (CONFIRM_RESULT_OK == confirmRet) {
						// call xu
						showVerifyDlg(reqItem, VERIFY_TYPE_SIMMELOCK);
					} else if (CONFIRM_RESULT_CANCEL == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					} else {
						removeRequest();
					}
					break;

				case CONFIRM_TYPE_FDN:
					if (CONFIRM_RESULT_OK == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					} else if (CONFIRM_RESULT_CANCEL == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					} else {
						removeRequest();
					}
					break;

				case CONFIRM_TYPE_ROAMING:
					this.mPreferSlot = intent.getIntExtra(CONFIRM_RESULT_PREFERSLOT, -1);
					if (CONFIRM_RESULT_OK == confirmRet) {
						if (CellConnMgr.RESULT_OK == mResult) {
							broadcastCallback(reqItem.getReqSlot(),
									REQUEST_TYPE_ROAMING,
									CellConnMgr.RESULT_OK);
						} else {
							broadcastCallback(reqItem.getReqSlot(),
									REQUEST_TYPE_ROAMING,
									CellConnMgr.RESULT_STATE_NORMAL);
						}
						if (0 == Settings.System.getInt(this
								.getContentResolver(),
								Settings.System.ROAMING_REMINDER_MODE_SETTING,
								-1)) {
/*							Settings.System.putInt(getContentResolver(),
									Settings.System.ROAMING_INDICATION_NEEDED,
									0);*/
							clearRoamingNeeded(reqItem.getReqSlot());
							//setRoaming2Local(reqItem.getReqSlot());
                            
						}
					} else if (CONFIRM_RESULT_CANCEL == confirmRet) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_ROAMING, CellConnMgr.RESULT_ABORT);
					} else {
						removeRequest();
					}
					break;
					
				case CONFIRM_TYPE_SIMLOCKED:
				case CONFIRM_TYPE_SLOTLOCKED:
					this.broadcastCallback(reqItem.getReqSlot(),
							REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_ABORT);
					break;

				default:
					break;
				}

				return;
			}

			int verifyType = intent.getIntExtra(VERIFY_TYPE, 0);
			if (0 != verifyType) {
				Log.d(TAG, "verify response and verifyType = "
						+ verifyTypeToString(verifyType));
				switch (verifyType) {
				case VERIFY_TYPE_PIN:
				case VERIFY_TYPE_PUK:
				case VERIFY_TYPE_SIMMELOCK:
					if (intent.getBooleanExtra(VERIFY_RESULT, false)) {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_PIN1, CellConnMgr.RESULT_OK);
					} else {
						this.broadcastCallback(reqItem.getReqSlot(),
								REQUEST_TYPE_PIN1, CellConnMgr.RESULT_ABORT);
					}
					break;

				case VERIFY_TYPE_PIN2:
				case VERIFY_TYPE_PUK2:
					if (intent.getBooleanExtra(VERIFY_RESULT, false)) {
						if (REQUEST_TYPE_PIN2 == reqItem.getReqType()
								|| REQUEST_TYPE_FDN == reqItem.getReqType()) {
							this.broadcastCallback(reqItem.getReqSlot(),
									reqItem.getReqType(), CellConnMgr.RESULT_OK);
						} else if (REQUEST_TYPE_ROAMING == reqItem.getReqType()) {
							Message msg = Message.obtain(this.mServiceHandler,
									MSG_ID_CHECKROAMING);
							msg.arg1 = reqItem.getReqSlot();
							this.mServiceHandler.sendMessage(msg);
						}
					} else {
						this.broadcastCallback(reqItem.getReqSlot(), reqItem
								.getReqType(), CellConnMgr.RESULT_ABORT);
					}
					break;

				default:
					break;
				}

				return;
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mIntentReceiver);

		waitForLooper();
		mServiceLooper.quit();

		Log.e(TAG, "onDestroy ");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onBind ");
		return mBinder;
	}

	private class RequestItem {
		int mReqSlot;
		int mReqType;
		IPhoneStatesCallback mICallback;

		RequestItem() {
			mReqSlot = -1;
			mReqType = REQUEST_TYPE_UNKNOWN;
			mICallback = null;
		}

		RequestItem(int reqSlot, int reqType, IPhoneStatesCallback iCallback) {
			this.mReqSlot = reqSlot;
			this.mReqType = reqType;
			this.mICallback = iCallback;
		}

		int getReqSlot() {
			return mReqSlot;
		}

		int getReqType() {
			return mReqType;
		}

		IPhoneStatesCallback getCallback() {
			if (null == this.mICallback) {
				Log.e(TAG, "RequestItem callback interface is null");
			}
			return mICallback;
		}
	}
	
	private void removeRequest() {
		Log.d(TAG, "removeRequest");
		synchronized (this.mICallBackList) {
			
			int listSize = this.mICallBackList.size();
			Log.d(TAG, "removeRequest callbacklist size = " + listSize);

			if (!this.mICallBackList.isEmpty()) {
				mICallBackList.remove(0);
			}
		}
	}

	//ALPS00299289
	private void broadcastCallback(int slot, int nRet) {
		synchronized (this.mICallBackList) {
		    int listSize = this.mICallBackList.size();
		    Log.d(TAG, "broadcastCallback by slot callbacklist size = " + listSize + " slot = " + slot);
		    for (int i = 0; i < listSize; ++i) {
				RequestItem reqItem = (RequestItem) mICallBackList.get(i);
				IPhoneStatesCallback iCallback = reqItem.getCallback();
				
				if (null == iCallback) {
					Log.e(TAG, "broadcastCallback by slot get call back is null");
					continue;
				}
				
				if (slot == reqItem.getReqSlot()) {
					try {
						if (REQUEST_TYPE_ROAMING == reqItem.getReqType()) {
							iCallback.onCompleteWithPrefer(nRet, mPreferSlot);
						} else {
							iCallback.onComplete(nRet);
						}
					} catch (RemoteException e) {
						Log.d(TAG, e.toString());
					}
					mICallBackList.remove(i);
					i--;
					listSize--;
					Log.d(TAG, "broadcastCallback by slot remove[" + i + "]" + " type = " + reqItem.getReqType() + " size = " + this.mICallBackList.size());
					continue;
				}

		    }
	    }
	}

	private void broadcastCallback(int slot, int reqType, int nRet) {
		Log.d(TAG, "broadcastCallback ++ reqType is "
				+ reqestTypeToString(reqType));

		if (!this.isSIMReady(slot) && (!this.isRadioOff(slot))) {
			Log.d(TAG, "broadcastCallback sim not ready");
			return;
		}

		synchronized (this.mICallBackList) {
			int listSize = this.mICallBackList.size();
			Log.d(TAG, "broadcastCallback[1] callbacklist size = " + listSize);
			if (0 == listSize || this.mICallBackList.isEmpty()) {
				Log.d(TAG, "broadcastCallback callback list is empty return [1]");
				return;
			}

			if ((CellConnMgr.RESULT_ABORT == nRet) && listSize > 0) {
				RequestItem reqItem = (RequestItem) mICallBackList.get(0);
				IPhoneStatesCallback iCallback = reqItem.getCallback();
				try {
					iCallback.onComplete(nRet);
				} catch (RemoteException e) {
					Log.e(TAG, e.toString());
				}
				mICallBackList.remove(0);
				Log.d(TAG, "broadcastCallback remove[0]");
				// listSize--;
				return;
			}

			for (int i = 0; i < listSize; ++i) {
				RequestItem reqItem = (RequestItem) mICallBackList.get(i);
				IPhoneStatesCallback iCallback = reqItem.getCallback();
				if (null == iCallback) {
					Log.e(TAG, "broadcastCallback get call back is null");
					continue;
				}
				if (reqType == reqItem.getReqType()
						&& slot == reqItem.getReqSlot()) {
					try {
						if (REQUEST_TYPE_ROAMING == reqType) {
							iCallback.onCompleteWithPrefer(nRet, mPreferSlot);
						} else {
							iCallback.onComplete(nRet);
						}
					} catch (RemoteException e) {
						Log.d(TAG, e.toString());
					}
					mICallBackList.remove(i);
					Log.d(TAG, "broadcastCallback remove[" + i + "]");
					i--;
					listSize--;
					Log.d(TAG,
							"broadcastCallback onComplete is called and current callbacklist size is "
									+ this.mICallBackList.size());
					continue;
				}

				if (REQUEST_TYPE_FDN == reqItem.getReqType()) {
					Message msg = Message.obtain(this.mServiceHandler,
							MSG_ID_CHECKFDN);
					msg.arg1 = reqItem.getReqSlot();
					this.mServiceHandler.sendMessage(msg);
					return;
				}

				if (REQUEST_TYPE_PIN2 == reqItem.getReqType()) {
					Message msg = Message.obtain(this.mServiceHandler,
							MSG_ID_CHECKPIN2);
					msg.arg1 = reqItem.getReqSlot();
					this.mServiceHandler.sendMessage(msg);
					return;
				}

				if (REQUEST_TYPE_ROAMING == reqItem.getReqType()) {
					mResult = nRet;
					Message msg = Message.obtain(this.mServiceHandler,
							MSG_ID_CHECKROAMING);
					msg.arg1 = reqItem.getReqSlot();
					this.mServiceHandler.sendMessage(msg);
					return;
				}
			}
		}
	}

	private void showConfirmDlg(RequestItem reqItem, int confirmType) {

		Log.d(TAG, "showConfirmDlg confirmType = "
				+ confirmTypeToString(confirmType));

		if (null == reqItem) {
			Log.e(TAG, "showConfirmDlg reqItem is null");
			return;
		}

		SIMInfo simInfo;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			simInfo = SIMInfo.getSIMInfoBySlot(getBaseContext(), reqItem
					.getReqSlot());
			if (null == simInfo) {
				Log.e(TAG, "showConfirmDlg: check radio get simInfo is null");
				broadcastCallback(reqItem.getReqSlot(), REQUEST_TYPE_UNKNOWN, CellConnMgr.RESULT_EXCEPTION);
				return;
			}
		}

		if ((CONFIRM_TYPE_PIN == confirmType) || (CONFIRM_TYPE_SIMMELOCK == confirmType)) {
			int slot = reqItem.getReqSlot();
			Log.d(TAG, "showConfirmDlg slot = " + slot);

			int simlockType = 0;
			if (CONFIRM_TYPE_PIN == confirmType) {
				simlockType = this.SIMLOCK_TYPE_PIN;
			} else if (CONFIRM_TYPE_SIMMELOCK == confirmType) {
				simlockType = this.SIMLOCK_TYPE_SIMMELOCK;
			}
/*			
			if (!isSimLockDisplay(slot, simlockType)) {
				Log.d(TAG, "showConfirmDlg sim lock screen simlockType = "
						+ simlockType + " has not yet shown");
				return;
			}
*/			
            if(simlockType == SIMLOCK_TYPE_PIN)
            {            	
				if (!isPINLockDisplay(slot)) {
						Log.d(TAG, "showConfirmDlg pin lock screen pinlockType = "
								+ simlockType + " has not yet shown");
						return;
					}

			}

		}

		String cardName;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			cardName = simInfo.mDisplayName;
			if (null == cardName) {
				cardName = new String("");
			}
		} else {
			cardName = "SIM";
		}
		mIdleIntent = new Intent(getBaseContext(), ConfirmDlgActivity.class);
		mIdleIntent.putExtra(CONFIRM_TYPE, confirmType);
		mIdleIntent.putExtra(CONFIRM_CARDNAME, cardName);
		mIdleIntent.putExtra(CONFIRM_SLOT, reqItem.getReqSlot());
		if (CONFIRM_TYPE_ROAMING == confirmType) {
			int nInsertedSIM = 0;
			List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(getBaseContext());
			if (null == simInfoList) {
				Log.e(TAG, "showConfirmDlg getInsertedSIMList is null");
			}
			if (null != simInfoList) {
			    nInsertedSIM = simInfoList.size();
			    if (!mRequestNoPrefer && (nInsertedSIM > 1) && (!isRoaming(((reqItem.getReqSlot() == 0)? 1 : 0)))) {
			        mIdleIntent.putExtra(CONFIRM_ROAMINGWITHPREFER, true);
			    } else {
			    	mIdleIntent.putExtra(CONFIRM_ROAMINGWITHPREFER, false);
			    }
			}
		}
		mIdleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//ALPS00283281
		//startActivity(it);
		Log.d(TAG, "showConfirmDlg() waiting queue idle");
                /* Solve [ALPS00347427]DUT sometimes do not show'SIM turn off' screen after call a number in airplane mode */
		Looper.getMainLooper().getQueue().addIdleHandler(new Idler());
	}

	//ALPS00283281
	private Intent mIdleIntent= null;
	private class Idler implements MessageQueue.IdleHandler {
	    public final boolean queueIdle() {
	        Log.d(TAG, "queueIdle() show confirm dialog, wait");
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {}
	        
	        startActivity(mIdleIntent);
	        return false;
	    }
	}

	private void showVerifyDlg(RequestItem reqItem, int verifyType) {

		Log.d(TAG, "showVerifyDlg verifyType = "
				+ verifyTypeToString(verifyType));
		if (null == reqItem) {
			Log.e(TAG, "showVerifyDlg reqItem is null");
			return;
		}

		int slot = reqItem.getReqSlot();
		Log.d(TAG, "showVerifyDlg slot = " + slot);
		
		int simlockType = 0;
		if (VERIFY_TYPE_PIN == verifyType || VERIFY_TYPE_PUK == verifyType) {
			simlockType = this.SIMLOCK_TYPE_PIN;
		} else if (VERIFY_TYPE_SIMMELOCK == verifyType) {
			simlockType = this.SIMLOCK_TYPE_SIMMELOCK;
		}
/*		
		if (!isSimLockDisplay(slot, simlockType)) {
			Log.d(TAG, "showVerifyDlg sim lock screen simlockType = " + simlockType + " has not yet shown");
			return;
		}
*/
	    if(simlockType == SIMLOCK_TYPE_PIN){
        
		if (!isPINLockDisplay(slot)) {
				Log.d(TAG, "showVerifyDlg pin lock screen pinlockType = "
						+ simlockType + " has not yet shown");
				return;
			}
	    }
		Intent it = new Intent();
		if (null == it) {
			Log.e(TAG, "showVerifyDlg new intent failed");
			return;
		}

		if (VERIFY_TYPE_PIN == verifyType || VERIFY_TYPE_PUK == verifyType) {
			it.setAction("com.android.phone.SetupUnlockPINLock");
		} else if (VERIFY_TYPE_SIMMELOCK == verifyType) {
			it.setAction("com.android.phone.SetupUnlockSIMLock");
		} else if (VERIFY_TYPE_PIN2 == verifyType
				|| VERIFY_TYPE_PUK2 == verifyType) {
			it.setAction("com.android.phone.SetupUnlockPIN2Lock");
		}

		it.putExtra("Phone.GEMINI_SIM_ID_KEY", slot);

		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(it);
	}

	private boolean isRadioOff(int slotId) {
		Log.d(TAG, "isRadioOff verify slot " + slotId);
		boolean bRadioOn = true;
		try {
			ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
			
			if (null == iTel) {
				Log.d(TAG, "isRadioOff iTel is null");
				return false;
			}

			if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
				bRadioOn = iTel.isRadioOnGemini(slotId);
			} else {
				bRadioOn = iTel.isRadioOn();
			}

		} catch (RemoteException ex) {
			ex.printStackTrace();
		}

		Log.d(TAG, "isRadioOff slot " + slotId + " radio off? " + !bRadioOn);
		return (!bRadioOn);
	}

	private void powerRadioOn(int slot) {
		// Change the system setting
		// airplane mode
		Log.d(TAG, "powerRadioOn +++");
		if (!this.isRadioOff(slot)) {
			Log.d(TAG, "powerRadioOn radio is on");
			return;
		}

		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			if (1 == Settings.System.getInt(this.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, -1)) {
				Log.d(TAG, "powerRadioOn: airplane mode is on");
				Settings.System.putInt(this.getContentResolver(),
						Settings.System.AIRPLANE_MODE_ON, 0);
				this.sendBroadcast(new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state", false));
				if (0 == slot) {
					// power on SIM1, and keep SIM2 power-off, change the mode to
					// SIM1 only
					Log.d(TAG, "powerRadioOn change to SIM1 only");
					// mDualSimMode = 1;
					int dualSimMode = 1;
					Settings.System.putInt(this.getContentResolver(),
							Settings.System.DUAL_SIM_MODE_SETTING, dualSimMode);
					Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
					intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, dualSimMode);
					this.sendBroadcast(intent);
					// showProgressDlg(true);
				} else if (1 == slot) {
					// power on SIM2, and keep SIM1 power-off, change the mode to
					// SIM2 only
					Log.d(TAG, "powerRadioOn change to SIM2 only");
					// mDualSimMode = 2;
					int dualSimMode = 2;
					Settings.System.putInt(this.getContentResolver(),
							Settings.System.DUAL_SIM_MODE_SETTING, dualSimMode);
					Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
					intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, dualSimMode);
					this.sendBroadcast(intent);
					// showProgressDlg(false);
				}
			} else {
				Log.d(TAG, "powerRadioOn: airplane mode is off");
				// Check dualSim mode
				int dualSimMode = getDualSimMode();
				Log.d(TAG, "powerRadioOn: airplane mode is off and dualSimMode = " + dualSimMode);
				if (dualSimMode == 0) {
				    // all of radio are powered off, set the dualSim mode to (slot + 1)
				    dualSimMode = slot + 1;
				} else {
				    // one of radio is powered on, to set the requested radio to powered on is equal 
				    // to set all of radio to powered on, so , set the dualSim mode to 3
				    dualSimMode = 3; // all of radios is on
				}
				broadcastDualSimModeEvent(dualSimMode);
			}
		} else {
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			Settings.System.putInt(this.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);
			this.sendBroadcast(intent);
		}
	}
	
	private void broadcastDualSimModeEvent(int dualSimMode) {
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.DUAL_SIM_MODE_SETTING, dualSimMode);
        Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, dualSimMode);
        this.sendBroadcast(intent);
	}
	
   private void broadcastAirPlaneModeEvent(int airPlaneMode) {
       Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
       Settings.System.putInt(this.getContentResolver(),
               Settings.System.AIRPLANE_MODE_ON, airPlaneMode);
       this.sendBroadcast(intent);
    }
	
    private int getDualSimMode() {
        return Settings.System.getInt(this.getContentResolver(),
                Settings.System.DUAL_SIM_MODE_SETTING, -1);
    }

	public boolean hasIccCardGemini(int slot) {

		boolean bRet = false;

		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.hasIccCardGemini(slot);
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				final ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (null != iTelephony) {
					bRet = iTelephony.hasIccCard();
				}
			} catch (RemoteException ex) {
				ex.printStackTrace();
			}
		}

		return bRet;
	}

	public boolean pinRequest(int slot) {
		Log.d(TAG, "pinRequest slot = " + slot);
		boolean simPINReq = false;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			simPINReq = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
					.getDefault().getSimStateGemini(slot));
		} else {
			simPINReq = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
					.getDefault().getSimState());
		}
		
		Log.d(TAG, "pinRequest result = " + simPINReq);
		return simPINReq;
	}

	public boolean pukRequest(int slot) {
		Log.d(TAG, "pukRequest slot = " + slot);
		boolean simPUKReq = false;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			Log.d(TAG, "pukRequest slot = " + slot + " SimState = " + TelephonyManager
					.getDefault().getSimStateGemini(slot));
			simPUKReq = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
					.getDefault().getSimStateGemini(slot) || (0 == this.getRetryPinCount(slot)));
		} else {
			simPUKReq = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
					.getDefault().getSimState() || (0 == this.getRetryPinCount(slot)));
		}
		
		Log.d(TAG, "pukRequest result = " + simPUKReq);
		return simPUKReq;
	}
	
	private int getRetryPinCount(final int slot) {
		if (slot == Phone.GEMINI_SIM_2)
			return SystemProperties.getInt("gsm.sim.retry.pin1.2", -1);
		else
			return SystemProperties.getInt("gsm.sim.retry.pin1", -1);
	}
	
	private int getRetryPukCount(final int slot) {
		if (slot == Phone.GEMINI_SIM_2)
			return SystemProperties.getInt("gsm.sim.retry.puk1.2", -1);
		else
			return SystemProperties.getInt("gsm.sim.retry.puk1", -1);
	}
	
	private int getRetryMELockCount(final int slot) {
		Log.d(TAG, "getRetryMELockCount slot = " + slot);

		mMELockHandler = new HanderEx(getMainLooper()) {
			public void handleMessage(Message msg) {
				AsyncResult ar = (AsyncResult) msg.obj;
				switch (msg.what) {
				case GET_MELOCK_RETRYCOUNT:
				{
					Log.d(TAG, "getRetryMELockCount GET_MELOCK_RETRYCOUNT +");
					int nMELockType = 0;
					int slot = msg.arg1;
					if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
						GeminiPhone mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
						nMELockType = mGeminiPhone.getIccCardGemini(slot)
								.getNetworkPersoType();
						Log.d(TAG, "getRetryMELockCount gemini nMELockType = " + nMELockType);
						mGeminiPhone.getIccCardGemini(slot).QueryIccNetworkLock(
								nMELockType,
								0,
								null,
								null,
								null,
								null,
								mMELockHandler.obtainMessage(UNLOCK_ICC_SML_QUERYLEFTTIMES));
					} else {
						Phone phone = PhoneFactory.getDefaultPhone();;
						nMELockType = phone.getIccCard().getNetworkPersoType();
						Log.d(TAG, "getRetryMELockCount single nMELockType = " + nMELockType);
						phone.getIccCard().QueryIccNetworkLock(
								nMELockType,
								0,
								null,
								null,
								null,
								null,
								mMELockHandler.obtainMessage(UNLOCK_ICC_SML_QUERYLEFTTIMES));
					}
					
					Log.d(TAG, "getRetryMELockCount GET_MELOCK_RETRYCOUNT -");
				}
				break;
				
				case UNLOCK_ICC_SML_QUERYLEFTTIMES: {
					Log.d(TAG, "getRetryMELockCount UNLOCK_ICC_SML_QUERYLEFTTIMES+");
					if (ar.exception != null) {
						Log.e(TAG, "getRetryMELockCount AsyncResult exception");
					} else {
						int[] LockState = (int[]) ar.result;
						setRetryCount(LockState[2]);
						synchronized (this) {
							this.notify();
						}
						break;
					}
					Log.d(TAG, "getRetryMELockCount UNLOCK_ICC_SML_QUERYLEFTTIMES -");
				}

				default:
					super.handleMessage(msg);

				}
			}
		};

                // ALPS00315209
		synchronized (mMELockHandler) {
		    Message msg = mMELockHandler.obtainMessage(GET_MELOCK_RETRYCOUNT);
		    msg.arg1 = slot;
		    mMELockHandler.sendMessage(msg);
			try {
                                Log.d(TAG, "getRetryMELockCount waiting...");
				mMELockHandler.wait();
			} catch (InterruptedException e) {
				Log.d(TAG, "getRetryMELockCount exception");
				e.printStackTrace();
			}
		}
		Log.d(TAG, "getRetryMELockCount nRetryCount = " + mMELockHandler.getRetryCount());
		
		return mMELockHandler.getRetryCount();
	}

	public boolean simMELockRequest(int slot) {
		Log.d(TAG, "simMELockRequest slot = " + slot);
		boolean simPINReq = false;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			simPINReq = (TelephonyManager.SIM_STATE_NETWORK_LOCKED == TelephonyManager
					.getDefault().getSimStateGemini(slot));
		} else {
			simPINReq = (TelephonyManager.SIM_STATE_NETWORK_LOCKED == TelephonyManager
					.getDefault().getSimState());
		}
		
		Log.d(TAG, "simMELockRequest result = " + simPINReq);
		return simPINReq;
	}
	
	public boolean isSIMReady(int slot) {
		Log.d(TAG, "isSIMReady slot = " + slot);
		boolean simReady = false;
		int nSimState = TelephonyManager.SIM_STATE_UNKNOWN;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			nSimState = TelephonyManager.getDefault().getSimStateGemini(slot);
		} else {
			nSimState = TelephonyManager.getDefault().getSimState();
		}
		
		Log.d(TAG, "isSIMReady simstate = " + nSimState);
		simReady = (nSimState == TelephonyManager.SIM_STATE_ABSENT 
				|| nSimState == TelephonyManager.SIM_STATE_PIN_REQUIRED
				|| nSimState == TelephonyManager.SIM_STATE_PUK_REQUIRED
				|| nSimState == TelephonyManager.SIM_STATE_NETWORK_LOCKED
				|| nSimState == TelephonyManager.SIM_STATE_READY);
		//simReady = (nSimState != TelephonyManager.SIM_STATE_UNKNOWN);
		
		Log.d(TAG, "isSIMReady result = " + simReady);
		return simReady;
	}
	
	/*
	 * public static boolean fdnRequest(int slot) {
	 * 
	 * Phone phone = PhoneFactory.getDefaultPhone(); if (null == phone) {
	 * Log.e(TAG, "fdnRequest phone is null"); return false; } IccCard iccCard;
	 * if (true == FeatureOption.MTK_GEMINI_SUPPORT) { iccCard = ((GeminiPhone)
	 * phone).getIccCardGemini(slot); } else { iccCard = phone.getIccCard(); }
	 * 
	 * return iccCard.getIccFdnEnabled(); }
	 */
	public boolean fdnRequest(int slot) {

		boolean bRet = false;

		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel) {
			Log.e(TAG, "fdnRequest iTel is null");
			return false;
		}

		try {
			if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
				bRet = iTel.isFDNEnabledGemini(slot);
			} else {
				bRet = iTel.isFDNEnabled();
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

		Log.d(TAG, "fdnRequest fdn enable is " + bRet);
		return bRet;
	}
	
	public boolean isRoamingNeeded(int slot) {
		Log.d(TAG, "isRoamingNeeded slot = " + slot);
		if (slot == Phone.GEMINI_SIM_2) {
			Log.d(TAG, "isRoamingNeeded = " + SystemProperties.getBoolean("gsm.roaming.indicator.needed.2", false));
			return SystemProperties.getBoolean("gsm.roaming.indicator.needed.2", false);
		} else {
			Log.d(TAG, "isRoamingNeeded = " + SystemProperties.getBoolean("gsm.roaming.indicator.needed", false));
			return SystemProperties.getBoolean("gsm.roaming.indicator.needed", false);
		}
	}
	
	public void clearRoamingNeeded(int slot) {
		Log.d(TAG, "clearRoamingNeeded slot = " + slot);
		if (slot == Phone.GEMINI_SIM_2) {
			SystemProperties.set("gsm.roaming.indicator.needed.2", "false");
		} else {
			SystemProperties.set("gsm.roaming.indicator.needed", "false");
		}
	}
	public boolean isRoaming2Local(int slot) {
	Log.d(TAG, "isRoaming2Local slot = " + slot);
	if (slot == Phone.GEMINI_SIM_2) {		
		Log.d(TAG, "isRoaming2Local = " + SystemProperties.getBoolean("gsm.roaming.indicator.tolocal.2", false));
		return SystemProperties.getBoolean("gsm.roaming.indicator.tolocal.2", false);
	} else {
		Log.d(TAG, "isRoaming2Local = " + SystemProperties.getBoolean("gsm.roaming.indicator.tolocal", false));
		return SystemProperties.getBoolean("gsm.roaming.indicator.tolocal", false);
	}
}
	public void setRoaming2Local(int slot) {
	Log.d(TAG, "setRoaming2Local slot = " + slot);
	if (slot == Phone.GEMINI_SIM_2) {
		SystemProperties.set("gsm.roaming.indicator.tolocal.2", "true");
	} else {
		SystemProperties.set("gsm.roaming.indicator.tolocal", "true");
	}
}
	
	private boolean isRoaming(int slot) {
		boolean bRoaming = false;
		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
			bRoaming = TelephonyManager.getDefault().isNetworkRoamingGemini(slot);
		} else {
			bRoaming = TelephonyManager.getDefault().isNetworkRoaming();
		}
		
		Log.d(TAG, "isRoaming slot = " + slot + " roaming = " + bRoaming);		
		return bRoaming;
	}
		
	public boolean roamingRequest(int slot) {
		Log.d(TAG, "roamingRequest slot = " + slot);
		
		if (isRoaming(slot)) {
       		Log.d(TAG, "roamingRequest slot = " + slot + " is roaming");
       	} else {
       		Log.d(TAG, "roamingRequest slot = " + slot + " is not roaming");
       		return false;
       	}
		
		if (0 == Settings.System.getInt(this.getContentResolver(),
				Settings.System.ROAMING_REMINDER_MODE_SETTING, -1) 
				&& isRoamingNeeded(slot)) {
			Log.d(TAG, "roamingRequest reminder once and need to indicate");
			return true;
		}
		
		if (1 == Settings.System.getInt(this.getContentResolver(),
				Settings.System.ROAMING_REMINDER_MODE_SETTING, -1)) {
			Log.d(TAG, "roamingRequest reminder always");
			return true;
		}

		Log.d(TAG, "roamingRequest result = false");
		return false;
	}
	
    private boolean isSimLockDisplay(int slot, int type) {
    	if (slot < 0) {
    		return false;
    	}
    	Log.d(TAG, "isSimLockDisplay slot = " + slot);
    	Long simLockState = Settings.System.getLong(this.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	Long bitSet = simLockState;
    	
    	Log.d(TAG, "isSimLockDisplay simLockState = " + simLockState);
    	
    	bitSet = bitSet>>>2*slot;
    	if (SIMLOCK_TYPE_PIN == type) {
    		if (0x1L == (bitSet & 0x1L)) {
    			Log.d(TAG, "isSimLockDisplay SIMLOCK_TYPE_PIN return true");
    			return true;
    		} else {
    			Log.d(TAG, "isSimLockDisplay SIMLOCK_TYPE_PIN return false");
    			return false;
    		}
    	} else if (SIMLOCK_TYPE_SIMMELOCK == type) {
    		bitSet = bitSet>>>1;
    		if (0x1L == (bitSet & 0x1L)) {
    			Log.d(TAG, "isSimLockDisplay SIMLOCK_TYPE_SIMMELOCK return true");
    			return true;
    		} else {
    			Log.d(TAG, "isSimLockDisplay SIMLOCK_TYPE_SIMMELOCK return false");
    			return false;
    		}
    	}
    	
    	Log.d(TAG, "isSimLockDisplay return true ");
    	return true;
    }
    private boolean isPINLockDisplay(int slot) {
    	if (slot < 0) {
    		return false;
    	}
    	Log.d(TAG, "isPINLockDisplay slot = " + slot);
    	Long simLockState = Settings.System.getLong(this.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	Long bitSet = simLockState;
    	
    	Log.d(TAG, "isPINLockDisplay pinLockState = " + simLockState);
    	
    	bitSet = bitSet>>>2*slot;
    		if (0x1L == (bitSet & 0x1L)) {
    			Log.d(TAG, "isPINLockDisplay SIMLOCK_TYPE_PIN return true");
    			return true;
    		} else {
    			Log.d(TAG, "isPINLockDisplay SIMLOCK_TYPE_PIN return false");
    			return false;
    		}
    	
    }
	

	private void waitForLooper() {
		while (mServiceHandler == null) {
			synchronized (this) {
				try {
					Log.d(TAG, "waitForLooper");
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();

		mServiceLooper = Looper.myLooper();
		mServiceHandler = new ServiceHandler();
		Log.d(TAG, "run");
		if (null == mServiceHandler) {
			Log.d(TAG, "mServiceHandler is null");
		}
		Looper.loop();
	}
	
	class HanderEx extends Handler {
		private int mRetryCount = 0;
		public int getRetryCount() {
			return mRetryCount;
		}
		
		public HanderEx (Looper looper){
			super(looper);
		}
		
		public void setRetryCount(int retryCount) {
			mRetryCount = retryCount;
		}
	}
	
	HanderEx mMELockHandler;
}
