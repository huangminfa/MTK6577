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

package com.mediatek.smsreg;

import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.telephony.*;
import android.telephony.gemini.GeminiSmsManager;
import android.content.BroadcastReceiver;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.*;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.android.internal.telephony.Phone;
import android.os.IBinder;
import android.telephony.ServiceState;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.util.Log;
import com.android.internal.telephony.TelephonyIntents;

import static android.provider.Telephony.Intents.EXTRA_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_SPN;
import static android.provider.Telephony.Intents.EXTRA_SPN;
import static android.provider.Telephony.Intents.SPN_STRINGS_UPDATED_ACTION;
import static android.provider.Telephony.Intents.ACTION_DUAL_SIM_MODE_SELECT;
import static android.provider.Telephony.Intents.ACTION_GPRS_CONNECTION_TYPE_SELECT;
import static android.provider.Telephony.Intents.ACTION_UNLOCK_KEYGUARD;

import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;

import com.android.internal.telephony.IccCard;
import com.mediatek.xlog.Xlog;

public class SmsRegService extends Service {
	private String TAG = "SmsReg/Service";
	private Boolean isSendMsg = false;
	private String[] IMSI = new String[2];
	private String simIMSI = null;
	private String savedIMSI;
	private int CustomOperatorID;
	private Boolean timeout = false;
	private int runCount = 0;
	private Boolean serviceAlive = true;

	private TelephonyManager mTelephonyManager;
	private InfoPersistentor mInfoPersistentor;
	private SmsBuilder mSmsBuilder;
	private SmsManager mSmsManager;
	private ConfigInfoGenerator xmlG;

	private Timer mTimer = new Timer();
	private TimerTask mtimerTask;
	private final long searchNetDelay = 90000; //wait 1.5min for search signal
	
	private SimStateReceiver simStateReceiver;
	private SmsReceivedReceiver smsReceivedReceiver;

	public void onCreate() {

		mSmsManager = SmsManager.getDefault();
		Xlog.e(TAG,"SmsRegService onCreate.");
		// create XMLgenerator object
		xmlG = XMLGenerator.getInstance(SmsRegConst.CONFIG_PATH);
		if (xmlG == null) {
			Xlog.e(TAG, "Init XMLGenerator error!");
			return;
		}

		mSmsBuilder = new SmsBuilder(this);
		mTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyManager == null) {
			Xlog.e(TAG, "TelephonyManager service is not exist!");
		}
		mInfoPersistentor = new InfoPersistentor();
		
	}
	
	public void onStop(){
		Xlog.i(TAG, "SmsRegService stop");
	}

	public void onDestory() {
		Xlog.i(TAG, "SmsRegService destory");
		mSmsManager = null;
		mTelephonyManager = null;
		mInfoPersistentor = null;
		mSmsBuilder = null;
		xmlG = null;		
	}

	public void onStart(Intent intent, int startId) {
		Xlog.e(TAG,"SmsReg service on start");		
		super.onStart(intent, startId);
		// if xmlG is init error the service should be stopped
		
		if (xmlG == null) {
			Xlog.e(TAG, "XMLGenerator instance init error!");
			stopSelf();
			return;
		}
		
		if(intent == null){
			Xlog.w(TAG, "intent is null!");
			return;
		}
		
		String action = intent.getAction();
		Xlog.i(TAG, "SmsReg service onStart, action = " + action);
		if(action == null){
			Xlog.w(TAG,"intent action is null!");
			return;
		}
		
		if (action.equals("BOOTCOMPLETED")) {
			simStateReceiver = new SimStateReceiver();			
			this.registerReceiver(simStateReceiver, 
					new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
			
			smsReceivedReceiver = new SmsReceivedReceiver();
			this.registerReceiver(smsReceivedReceiver, 
					new IntentFilter("android.intent.action.DM_REGISTER_SMS_RECEIVED"));
			
			boolean isGemini = FeatureOption.MTK_GEMINI_SUPPORT;
			Xlog.i(TAG, "isGemini = " + isGemini);
			// register the phonestate listener
			if (isGemini) {
					Xlog.i(TAG, "Regist service state listener for sim1.");
					mTelephonyManager.listenGemini(mPhoneStateListener,
							PhoneStateListener.LISTEN_SERVICE_STATE,
							Phone.GEMINI_SIM_1);
					Xlog.i(TAG, "Regist service state listener gemini for sim2.");
					mTelephonyManager.listenGemini(mPhoneStateListenerGemini,
							PhoneStateListener.LISTEN_SERVICE_STATE,
							Phone.GEMINI_SIM_2);
			} else {
					Xlog.i(TAG, "Regist service state listener for sim.");
					mTelephonyManager.listen(mPhoneStateListener,
							PhoneStateListener.LISTEN_SERVICE_STATE);
			}
			
			mtimerTask = new TimerTask() {
				@Override
				public void run() {
					Xlog.i(TAG, "timer run, schedule = " + searchNetDelay);
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
				}
			};
			// start the timer
			mTimer.schedule(mtimerTask, searchNetDelay);
			

		} else if (action.equals("SIM_STATE_CHANGED")) {			
			String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
	        if (!IccCard.INTENT_VALUE_ICC_LOADED.equals(stateExtra)) {
				Xlog.w(TAG, "sim state is not loaded");
				return;
			}
	
	        Xlog.i(TAG, "sim state is loaded");
			
			if (!isSendMsg) {
				// send register message
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					getSimCardMatchCustomizedGemini();
					int simNum = -1;
					if (IMSI[0] != null) {
						simNum = Phone.GEMINI_SIM_1;
					} else if (IMSI[1] != null) {
						simNum = Phone.GEMINI_SIM_2;
					} else {
						Xlog.e(TAG, "No sim card or the sim card is not "
								+ "the customized operator");
						return;
					}

					savedIMSI = mInfoPersistentor.getSavedIMSI();
					Xlog.i(TAG, "the savedIMSI = [" + savedIMSI + "]");
					Boolean isReg = isRegisterGemini(IMSI, savedIMSI);
					if (!isReg) {
						Xlog.w(TAG, "The sim card in this gemini phone is not "
								+ "registered, need register");
						sendRegisterMessageGemini(simNum);
					} else {
						Xlog.w(TAG, "The gemini phone has registered already");
						stopService();
					}

				} else {
					// FeatureOption.MTK_GEMINI_SUPPORT == false
					getSimCardMatchCustomized();
					if (simIMSI != null) {
						savedIMSI = mInfoPersistentor.getSavedIMSI();
						Xlog.w(TAG, "the savedIMSI = [" + savedIMSI + "]");
						Boolean isReg = isRegister(simIMSI, savedIMSI);
						if (!isReg) {
							Xlog.w(TAG, "The phone is not registered, need register");
							sendRegisterMessage();
						} else {
							Xlog.w(TAG, "The phone has registered already");
							stopService();
						}
					} else {
						Xlog.e(TAG, "No sim card or the sim card is not"
								+ " the customized operator");
						return;
					}
				}

			} else {
				Xlog.w(TAG, "the register message has been sent");
			}

		}else if(action.equals("REGISTER_SMS_RECEIVED")){
			Xlog.i(TAG,"broadcast REGISTER_SMS_RECEIVED has received");
			Xlog.w(TAG, "Save the IMSI");
			String IMSI = intent.getStringExtra("IMSI");
			Xlog.w(TAG, "The IMSI to save is  = [" + IMSI + "]");
			if (IMSI != null && !(IMSI.equals(""))) {
				InfoPersistentor mInfoPersistentor = new InfoPersistentor();
				mInfoPersistentor.setSavedIMSI(IMSI);
			}			
			stopService();
		}else {
			Xlog.e(TAG, "Get the wrong intent");
			serviceAlive = false;
		}
		if (serviceAlive == false) {
			Xlog.w(TAG, "serviceAlive is false");
			stopService();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			timeout = true;
			Log.i(TAG,"the timer is timeout, delay time is "+ searchNetDelay);
			stopService();
		}

	};

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			Log.i(TAG,"Service state change sim:"+serviceState.getState());
			if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
//				// stop the timer
//				mTimer.cancel();
//				// the receive count is add
//				runCount++;
//				// if the time is out
//				if (timeout == true || runCount != 1) {
//					Xlog.e(TAG, "the timer is out or this is not the "
//							+ "first time received NETCONNETED intent");
//					serviceAlive = false;
//					return;
//				}

				if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
					getSimCardMatchCustomizedGemini();
					if (IMSI[0] != null) {
						savedIMSI = mInfoPersistentor.getSavedIMSI();
						Xlog.i(TAG, "the savedIMSI = [" + savedIMSI + "]");
						Boolean isReg = isRegisterGemini(IMSI, savedIMSI);
						if (!isReg) {
							Xlog.w(TAG, "The sim card in this phone is not"
									+ " registered, need register");
							sendRegisterMessageGemini(Phone.GEMINI_SIM_1);

						} else {
							Xlog.w(TAG, "The gemini register msg has been sent already!");
							stopService();
						}
					} else {
						Xlog.e(TAG, "Sim card 1 is not the right operator");
					}
				} else {
					getSimCardMatchCustomized();
					if (simIMSI != null) {
						savedIMSI = mInfoPersistentor.getSavedIMSI();
						Xlog.i(TAG, "the savedIMSI = [" + savedIMSI + "]");
						Boolean isReg = isRegister(simIMSI, savedIMSI);
						if (!isReg) {
							Xlog.w(TAG, "The sim card in this phone is "
									+ "not registered, need register");
							sendRegisterMessage();
						} else {
							Xlog.w(TAG, "The phone has been registered already!");
							stopService();
						}

					}
				}

			}
		}
	};

	private PhoneStateListener mPhoneStateListenerGemini = new PhoneStateListener() {
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			Log.i(TAG,"Service state change sim gemini:"+serviceState);
			if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
//				// stop the timer
//				mTimer.cancel();
//				// the receive count is add
//				runCount++;
//				// if the time is out
//				if (timeout == true || runCount != 1) {
//					Xlog.e(TAG, "the timer is out or this is not the first "
//							+ "time received NETCONNETED intent");
//					serviceAlive = false;
//					return;
//				}

				getSimCardMatchCustomizedGemini();
				if (IMSI[0] == null && IMSI[1] != null) {
					savedIMSI = mInfoPersistentor.getSavedIMSI();
					Xlog.i(TAG, "the savedIMSI = [" + savedIMSI + "]");
					Boolean isReg = isRegisterGemini(IMSI, savedIMSI);
					if (!isReg) {
						Xlog.w(TAG, "The sim card in this phone is not "
								+ "registered, need register");
						sendRegisterMessageGemini(Phone.GEMINI_SIM_2);
					} else {
						Xlog.w(TAG, "The phone gemini has been registered already!");
						stopService();
					}
				} else {
					Xlog.e(TAG, "Sim2 do not need to register or "
							+ "sim2 is not the right operator");
				}

			}
		}
	};

	private void getSimCardMatchCustomized() {
		// the phone is for operator
		// which operator
		String operatorID = xmlG.getOperatorName();
		String operatorNumber[] = xmlG.getNetworkNumber();
		Xlog.i(TAG, "the operator Id = " + operatorID
				+ ", operatorNumber.length = " + operatorNumber.length);
		// get sim card state
		int simState = mTelephonyManager.getSimState();
		if (TelephonyManager.SIM_STATE_READY == simState) {
			String currentSimOperator = mTelephonyManager.getSimOperator();
			Xlog.i(TAG, "there is a sim card is ready the operator is "
					+ currentSimOperator);
			if(currentSimOperator==null || currentSimOperator.trim().equals("")){
				Xlog.i(TAG, "operator is null, do nothing. ");
				return;
			}
			int j = 0;
			for (; j < operatorNumber.length; j++) {
				String configedOperaterNumber = operatorNumber[j];
				if (configedOperaterNumber != null
						&& configedOperaterNumber.equals(currentSimOperator)) {
					Xlog.i(TAG, "the ready sim card operator is "
							+ operatorNumber[j]);
					// get ISMI
					simIMSI = mTelephonyManager.getSubscriberId();
					Xlog.i(TAG, "the current imsi is " + simIMSI);
					break;
				}
			}
			if (j >= operatorNumber.length) {
				Xlog.e(TAG, "There is no sim card operator is matched current"
						+ "operator number.");
			}
		} else {
			Xlog.w(TAG, "Sim state is not ready, state = " + simState);
		}
	}

	private void getSimCardMatchCustomizedGemini() {
		String operatorID = xmlG.getOperatorName();
		String operatorNumber[] = xmlG.getNetworkNumber();
		Xlog.i(TAG, "the operator Id = " + operatorID
				+ ", operatorNumber.length = " + operatorNumber.length);
		for (int i = 0; i < SmsRegConst.GEMSIM.length; i++) {
			// get sim card state
			int simState = mTelephonyManager
					.getSimStateGemini(SmsRegConst.GEMSIM[i]);
			if (TelephonyManager.SIM_STATE_READY == simState) {

				String currentOperator = mTelephonyManager
						.getSimOperatorGemini(SmsRegConst.GEMSIM[i]);
				Xlog.i(TAG, "there is a sim card is ready the operator is "
						+ currentOperator);
				if(currentOperator==null || currentOperator.trim().equals("")){
					Xlog.i(TAG, "operator is null, continue next one. ");
					continue;
				}
				for (int j = 0; j < operatorNumber.length; j++) {
					String configuredOperator = operatorNumber[j];
					Xlog.i(TAG, "the phone is for the operator[ "
							+ configuredOperator + "]");
					// Xlog.i(TAG, "current operator:"+currentOperator);
					if (configuredOperator != null
							&& configuredOperator.equals(currentOperator)) {
						IMSI[i] = mTelephonyManager
								.getSubscriberIdGemini(SmsRegConst.GEMSIM[i]);
						Xlog.i(TAG, "current IMSI[" + i + "]=" + IMSI[i]);
						break;
					}
				}
			}
		}// for
	}

	private Boolean isRegister(String IMSI, String savedIMSI) {
		if (savedIMSI != null) {
			Xlog.i(TAG, "The saved IMSI =[" + savedIMSI + "]");
			Xlog.i(TAG, "The current IMSI =[" + IMSI + "]");

			if (IMSI != null && IMSI.equals(savedIMSI)) {
				Xlog.w(TAG, "The SIM card and device have rigistered");
				return true;
			}
		}
		return false;
	}

	private Boolean isRegisterGemini(String[] IMSI, String savedIMSI) {
		if (savedIMSI != null) {
			Xlog.i(TAG, "The saved IMSI =[" + savedIMSI + "]");
			Xlog.i(TAG, "The current IMSI0 =[" + IMSI[0] + "]");
			Xlog.i(TAG, "The current IMSI1 =[" + IMSI[1] + "]");

			if (IMSI[0] != null && IMSI[0].equals(savedIMSI)) {
				Xlog.i(TAG, "The SIM1 have registered already.");
				return true;
			}
			if(IMSI[1] != null && IMSI[1].equals(savedIMSI)){
				Xlog.i(TAG, "The SIM2 have registered already.");
				return true;
			}
		}
		return false;
	}

	private void sendRegisterMessage() {
		Xlog.i(TAG, "send register message begin...");
		// check the country if it is roaming international
		String simCountryIso = mTelephonyManager.getSimCountryIso();
		String networkIso = mTelephonyManager.getNetworkCountryIso();
		Xlog.i(TAG, "simCountryIso = " + simCountryIso);
		Xlog.i(TAG, " networkIso= " + networkIso);
		if (simCountryIso.equals(networkIso)) {
			String smsRegMsg = mSmsBuilder.getSmsContent(xmlG, 0);
			Xlog.i(TAG, "SmsRegMsg = " + smsRegMsg);
			if (smsRegMsg != null) {
				String optAddr = xmlG.getSmsNumber();
				Short optPort = xmlG.getSmsPort();
				Short srcPort = xmlG.getSrcPort();
				Xlog.i(TAG, "Operator's sms number = " + optAddr);
				Xlog.i(TAG, "Operator's sms port = " + optPort);
				Xlog.i(TAG, "Src port = " + srcPort);
				String operatorID = xmlG.getOperatorName();
				if (mSmsManager != null) {
					if (operatorID.equalsIgnoreCase("cu")
							|| operatorID.equalsIgnoreCase("cmcc")) {
						if(!isSendMsg){
							PendingIntent mPendingIntent = getSendPendingIntent(0);
							mSmsManager.sendDataMessage(optAddr, null, optPort,
									srcPort, smsRegMsg.getBytes(), mPendingIntent,
									null);
							Xlog.i(TAG, "send register message end, "
									+ "RegMsg is send out!");
							isSendMsg = true;
						}else{
							Xlog.w(TAG, "RegMsg has been sent already. ");
						}						
						
					} else {
						Xlog.w(TAG, "RegMsg is not send, "
								+ "it is not the operator cu or cmcc");
					}
				} else {
					Xlog.e(TAG, "Send RegMsg failed, mSmsManager is null");
				}
			} else {
				Xlog.e(TAG,
						"Send RegMsg failed, The Sms Register message is null");
				serviceAlive = false;
			}
		}
	}

	private void sendRegisterMessageGemini(int simId) {
		Xlog.i(TAG, "send register message gemini begin...");
		String simCountryIso = mTelephonyManager.getSimCountryIsoGemini(simId);
		String networkIso = mTelephonyManager.getNetworkCountryIsoGemini(simId);
		Xlog.i(TAG, "simCountryIso = " + simCountryIso);
		Xlog.i(TAG, " networkIso= " + networkIso);
		if (simCountryIso.equals(networkIso)) {
			String smsRegMsg = mSmsBuilder.getSmsContent(xmlG, simId);
			Xlog.i(TAG, "SmsRegMsg = " + smsRegMsg);
			if (smsRegMsg != null) {
				String optAddr = xmlG.getSmsNumber();
				Short optPort = xmlG.getSmsPort();
				Short srcPort = xmlG.getSrcPort();
				Xlog.i(TAG, "Operator's sms number = " + optAddr);
				Xlog.i(TAG, "Operator's sms port = " + optPort);
				Xlog.i(TAG, "Src port = " + srcPort);
				String operatorID = xmlG.getOperatorName();
				if (operatorID.equalsIgnoreCase("cu")
						|| operatorID.equalsIgnoreCase("cmcc")) {
					if(!isSendMsg){
						PendingIntent mPendingIntent = getSendPendingIntent(simId);
						GeminiSmsManager.sendDataMessageGemini(optAddr, null,
								optPort, srcPort, smsRegMsg.getBytes(), simId,
								mPendingIntent, null);
						Xlog.i(TAG, "send register message end, "
								+ "RegMsg gemini is send out!");
						isSendMsg = true;
					}else{
						Xlog.w(TAG, "RegMsg gemini has been sent already. ");
					}
					
				} else {
					Xlog.w(TAG, "RegMsg is not send, "
							+ "it is not the operator cu or cmcc");
				}

			} else {
				Xlog.e(TAG,
						"Send RegMsg failed, The Sms Register message is null");
				serviceAlive = false;
			}
		}else{
			Xlog.w(TAG,
			"SimCountryIso is not equals with NetworkCountryIso, do nothing");
		}
	}

	private PendingIntent getSendPendingIntent(int simId) {
		Xlog.i(TAG, "get Pending Intent begin, simId = " + simId);
		String IMSI = null;
		Intent mIntent = new Intent();
		mIntent.setAction("android.intent.action.DM_REGISTER_SMS_RECEIVED");
		if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
			Xlog.i(TAG, "put extra SimID, SimID = " + simId);
			mIntent.putExtra("SimID", simId);
			IMSI = mTelephonyManager.getSubscriberIdGemini(simId);
		} else {
			IMSI = mTelephonyManager.getSubscriberId();
		}
		Xlog.i(TAG, "put extra IMSI, IMSI = " + IMSI);
		mIntent.putExtra("IMSI", IMSI);

		PendingIntent mSendPendingIntent = PendingIntent.getBroadcast(this, 0,
				mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Xlog.i(TAG, "get Pending Intent end");
		return mSendPendingIntent;
	}
	
	protected void stopService(){
		
		Xlog.i(TAG, "stop service.");
		
		if(FeatureOption.MTK_GEMINI_SUPPORT){
			
			if(mPhoneStateListener != null){
				Xlog.i(TAG, "unRegist service state listener for sim1.");
				mTelephonyManager.listenGemini(mPhoneStateListener,
						PhoneStateListener.LISTEN_NONE,
						Phone.GEMINI_SIM_1);
				mPhoneStateListener = null;
			}
			
			if(mPhoneStateListenerGemini != null){
				Xlog.i(TAG, "unRegist service state listener gemini for sim2.");
				mTelephonyManager.listenGemini(mPhoneStateListenerGemini,
					PhoneStateListener.LISTEN_NONE,
					Phone.GEMINI_SIM_2);
				mPhoneStateListenerGemini = null;
			}			
			
		}else{
			if(mPhoneStateListener != null){
				Xlog.i(TAG, "unRegist service state listener for sim.");
				mTelephonyManager.listen(mPhoneStateListener,
						PhoneStateListener.LISTEN_NONE);
				mPhoneStateListener = null;
			}			
		}	
		
		
		if(simStateReceiver != null){
			Xlog.i(TAG, "unRegist sim state receiver.");
			unregisterReceiver(simStateReceiver);
			simStateReceiver = null;
		}
		
		if(smsReceivedReceiver != null){
			Xlog.i(TAG, "unRegist smsReceived receiver.");
			unregisterReceiver(smsReceivedReceiver);
			smsReceivedReceiver = null;
		}
		
		if(mTimer != null){
			Xlog.i(TAG, "cancel timer.");
			mTimer.cancel();
			mTimer = null;
			if(mtimerTask != null){
				mtimerTask = null;
			}
		}		
		
		stopSelf();
	}
	
	
	class SimStateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Xlog.i(TAG, "sim state changed");
//			Intent intent = new Intent();
			intent.setAction("SIM_STATE_CHANGED");
			intent.setClass(context, SmsRegService.class);
			context.startService(intent);	
			
		}
		
	}
	
	class SmsReceivedReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {		
			intent.setAction("REGISTER_SMS_RECEIVED");
			intent.setClass(context, SmsRegService.class);
			context.startService(intent);
						
		}
		
	}

}
