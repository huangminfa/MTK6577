/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.phone;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Process; // NFC SEEK
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
// NFC SEEK start
import com.android.internal.telephony.IccIoResult;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.CommandException;
// NFC SEEK end

import java.util.List;
import java.util.ArrayList;
// NFC SEEK start
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// NFC SEEK end

//MTK-START [mtk04070][111117][ALPS00093395]MTK added
import android.util.Slog;
import android.os.SystemProperties;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.CommandException;
import android.telephony.BtSimapOperResponse;
import com.mediatek.featureoption.FeatureOption;
import java.util.Iterator;
import com.android.internal.telephony.Call;
import com.mediatek.vt.VTManager;
import com.android.internal.telephony.IccFileHandler;
//MTK-END [mtk04070][111117][ALPS00093395]MTK added
//MTK-START [mtk03851][111216]MTK added
import android.os.Messenger;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
//MTK-END [mtk03851][111216]MTK added
import com.android.internal.telephony.PhoneProxy;

/**
 * Implementation of the ITelephony interface.
 */
public class PhoneInterfaceManager extends ITelephony.Stub {
    private static final String LOG_TAG = "PhoneInterfaceManager";
    //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
    private static final boolean DBG = true;//(PhoneApp.DBG_LEVEL >= 2);
    //MTK-END [mtk04070][111117][ALPS00093395]MTK modified

    // Message codes used with mMainThreadHandler
    private static final int CMD_HANDLE_PIN_MMI = 1;
    private static final int CMD_HANDLE_NEIGHBORING_CELL = 2;
    private static final int EVENT_NEIGHBORING_CELL_DONE = 3;
    private static final int CMD_ANSWER_RINGING_CALL = 4;
    private static final int CMD_END_CALL = 5;  // not used yet
    private static final int CMD_SILENCE_RINGER = 6;
    // NFC SEEK start
    private static final int CMD_EXCHANGE_APDU = 9; //<-- changed because of the number is used
    private static final int EVENT_EXCHANGE_APDU_DONE = 10;
    private static final int CMD_OPEN_CHANNEL = 11;
    private static final int EVENT_OPEN_CHANNEL_DONE = 12;
    private static final int CMD_CLOSE_CHANNEL = 13;
    private static final int EVENT_CLOSE_CHANNEL_DONE = 14;
    private static final int CMD_SIM_IO = 15;
    private static final int EVENT_SIM_IO_DONE = 16;
    private static final int CMD_GET_ATR = 17;
    private static final int EVENT_GET_ATR_DONE = 18;
    private int lastError;
    private static final class IccAPDUArgument {

        public int channel, cla, command, p1, p2, p3;
        public String data;

        public IccAPDUArgument(int cla, int command, int channel,
                int p1, int p2, int p3, String data) {
            this.channel = channel;
            this.cla = cla;
            this.command = command;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.data = data;
        }
    }	
    private static final int CMD_EXCHANGE_APDU_1 = 19;
    private static final int CMD_EXCHANGE_APDU_2 = 20;
    private static final int CMD_OPEN_CHANNEL_1 = 21;
    private static final int CMD_OPEN_CHANNEL_2 = 22;
    private static final int CMD_CLOSE_CHANNEL_1 = 23;
    private static final int CMD_CLOSE_CHANNEL_2 = 24;
    private static final int CMD_SIM_IO_1 = 25;
    private static final int CMD_SIM_IO_2 = 26;
    private static final int CMD_GET_ATR_1 = 27;
    private static final int CMD_GET_ATR_2 = 28;
    private static final int EVENT_EXCHANGE_APDU_1_DONE = 29;
    private static final int EVENT_EXCHANGE_APDU_2_DONE = 30;
    private static final int EVENT_OPEN_CHANNEL_1_DONE = 36;
    private static final int EVENT_OPEN_CHANNEL_2_DONE = 37;
    private static final int EVENT_CLOSE_CHANNEL_1_DONE = 38;
    private static final int EVENT_CLOSE_CHANNEL_2_DONE = 39;
    private static final int EVENT_SIM_IO_1_DONE = 40;
    private static final int EVENT_SIM_IO_2_DONE = 41;
    private static final int EVENT_GET_ATR_1_DONE = 42;
    private static final int EVENT_GET_ATR_2_DONE = 43;

//MTK-START [mtk04258][20120806][International card support]
    private static final int CMD_GET_OPERATOR_LIMIT = 50;
    private static final int EVENT_GET_OPERATOR_LIMIT_DONE = 51;
//MTK-END [mtk04258][20120806][International card support]

    private int lastError1;
    private int lastError2;
    // NFC SEEK end

    /** The singleton instance. */
    private static PhoneInterfaceManager sInstance;

    PhoneApp mApp;
    Phone mPhone;
    CallManager mCM;
    MainThreadHandler mMainThreadHandler;

    //MTK-START [mtk04070][111117][ALPS00093395]MTK added
    /* Fion add start */
    private static final int CMD_END_CALL_GEMINI = 7; 
    private static final int CMD_ANSWER_RINGING_CALL_GEMINI = 8;
    /* Fion add end */
    private static final int CMD_HANDLE_GET_SCA = 31;
    private static final int CMD_GET_SCA_DONE = 32;
    private static final int CMD_HANDLE_SET_SCA = 33;
    private static final int CMD_SET_SCA_DONE = 34;

    /* Adjust modem radio power for Lenovo SAR requirement. */
    private static final int CMD_ADJUST_MODEM_RADIO_POWER = 35;

    /* 3G switch start */
    private ArrayList<Integer> m3GSwitchLocks = new ArrayList<Integer>();
    private static int m3GSwitchLockCounter;
    /* 3G switch end */

    private class PinMmiGemini {
        public String dialString;
        public Integer simId;

        public PinMmiGemini(String dialString, Integer simId) {
            this.dialString = dialString;
            this.simId = simId;
        }
    }

    private class ScAddrGemini {
		public String scAddr;
		public int simId;

		public ScAddrGemini(String addr, int id) {
			this.scAddr = addr;
			if(id == Phone.GEMINI_SIM_1 || id == Phone.GEMINI_SIM_2) {
				simId = id;
			} else {
			    simId = Phone.GEMINI_SIM_1;
			}
		}
     }
    //MTK-END [mtk04070][111117][ALPS00093395]MTK added


    /**
     * A request object for use with {@link MainThreadHandler}. Requesters should wait() on the
     * request after sending. The main thread will notify the request when it is complete.
     */
    private static final class MainThreadRequest {
        /** The argument to use for the request */
        public Object argument;
        /** The result of the request that is run on the main thread */
        public Object result;

        public MainThreadRequest(Object argument) {
            this.argument = argument;
        }
    }

    /**
     * A handler that processes messages on the main thread in the phone process. Since many
     * of the Phone calls are not thread safe this is needed to shuttle the requests from the
     * inbound binder threads to the main thread in the phone process.  The Binder thread
     * may provide a {@link MainThreadRequest} object in the msg.obj field that they are waiting
     * on, which will be notified when the operation completes and will contain the result of the
     * request.
     *
     * <p>If a MainThreadRequest object is provided in the msg.obj field,
     * note that request.result must be set to something non-null for the calling thread to
     * unblock.
     */
    private final class MainThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MainThreadRequest request;
            Message onCompleted;
            AsyncResult ar;

            switch (msg.what) {
                case CMD_HANDLE_PIN_MMI:
                    //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
                    PinMmiGemini pinmmi;
                    request = (MainThreadRequest) msg.obj;
                    pinmmi = (PinMmiGemini) request.argument;
                    if (pinmmi.simId != -1) {
                    request.result = Boolean.valueOf(
                                ((GeminiPhone)mPhone).handlePinMmiGemini(pinmmi.dialString, pinmmi.simId));
                    } else {
                        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                        {
                            request.result = Boolean.valueOf(
                                    ((GeminiPhone)mPhone).handlePinMmi(pinmmi.dialString));
                        }
                        else
                        {
                    request.result = Boolean.valueOf(
                                    mPhone.handlePinMmi(pinmmi.dialString));
                        }
                    }
                    //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                case CMD_HANDLE_NEIGHBORING_CELL:
                    request = (MainThreadRequest) msg.obj;
                    onCompleted = obtainMessage(EVENT_NEIGHBORING_CELL_DONE,
                            request);

                    //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
                    if (request.argument == null) {
                    mPhone.getNeighboringCids(onCompleted);
                    } else {
                        Integer simId = (Integer)request.argument;
                        ((GeminiPhone)mPhone).getNeighboringCidsGemini(onCompleted, simId.intValue());
                    }                   
                    //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
                    break;

                case EVENT_NEIGHBORING_CELL_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else {
                        // create an empty list to notify the waiting thread
                        request.result = new ArrayList<NeighboringCellInfo>();
                    }
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                case CMD_ANSWER_RINGING_CALL:
                    //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
                	if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                	{
                		if( PhoneApp.getInstance().isVTRinging())
                		{
                			try
                			{
                 				PhoneApp.getInstance().getInCallScreenInstance().getInCallTouchUi().touchAnswerCall();
                			}catch(Exception ex)  
                    		{}
                		}
                		else
                		{
                			answerRingingCallInternal();
                		}
                	}
                	else
                	{
                    answerRingingCallInternal();
                	}
                    //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
                    break;

                case CMD_SILENCE_RINGER:
                    silenceRingerInternal();
                    break;

                case CMD_END_CALL:
                    //MTK-START [mtk04070][111117][ALPS00093395]MTK added
                	/*if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                	{         
                		try{
                			InCallScreen ics = PhoneApp.getInstance().getInCallScreenInstance();
                			if(!PhoneApp.getInstance().isVTActive()){
                				ics.getVTInCallScreenInstance().setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_CLOSE);     			
                    		}else{
                    			ics.getVTInCallScreenInstance().setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_OPEN);
                    		}
                			ics.getVTInCallScreenInstance().updateVTScreen(ics.getVTInCallScreenInstance().getVTScreenMode());
                		}catch(Exception ex)
                		{}
                	
                	}*/
                    //MTK-END [mtk04070][111117][ALPS00093395]MTK added
                	
                    request = (MainThreadRequest) msg.obj;
                    boolean hungUp = false;
                    int phoneType = mPhone.getPhoneType();
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        // CDMA: If the user presses the Power button we treat it as
                        // ending the complete call session
                        hungUp = PhoneUtils.hangupRingingAndActive(mPhone);
                    } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                        // GSM: End the call as per the Phone state
                        hungUp = PhoneUtils.hangup(mCM);
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }
                    if (DBG) log("CMD_END_CALL: " + (hungUp ? "hung up!" : "no call to hang up"));
                    request.result = hungUp;
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
                    break;

                //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
                case CMD_END_CALL_GEMINI:
                    if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                    {
                    	if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                    	{   
                    		try{                	
                    			InCallScreen ics2 = PhoneApp.getInstance().getInCallScreenInstance();
                    			if(!PhoneApp.getInstance().isVTActive()){
                    				ics2.getVTInCallScreenInstance().setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_CLOSE);     			
                        		}else{
                        			ics2.getVTInCallScreenInstance().setVTScreenMode(VTCallUtils.VTScreenMode.VT_SCREEN_OPEN);
                        		}
                    			ics2.getVTInCallScreenInstance().updateVTScreen(ics2.getVTInCallScreenInstance().getVTScreenMode()); 
                    		}catch(Exception ex)  
                    		{}  
                    	}
                    	
                        request = (MainThreadRequest) msg.obj;
                        boolean hungUpGemini = false;
                        int simId = (int)(msg.arg1);
                    
                        log("CMD_END_CALL_GEMINI: msg.arg1" + simId);
                    
                        int phoneTypeGemini = ((GeminiPhone)mPhone).getPhoneTypeGemini(simId);
                    
                        if (phoneTypeGemini == Phone.PHONE_TYPE_CDMA) {
                            hungUpGemini = PhoneUtils.hangupRingingAndActive(mPhone);
                        } else if (phoneTypeGemini == Phone.PHONE_TYPE_GSM) {
                            hungUpGemini = PhoneUtils.hangup(mCM);
                        } else {
                            throw new IllegalStateException("Unexpected phone type: " + phoneTypeGemini);
                        }
                    
                        if (DBG) log("CMD_END_CALL_GEMINI: " + (hungUpGemini ? "hung up!" : "no call to hang up"));
                        request.result = hungUpGemini;
                        synchronized (request) {
                            request.notifyAll();
                        }                
                    }
                    break;
                    
                case CMD_ANSWER_RINGING_CALL_GEMINI:
                    if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                    {
                    	if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                    	{        
                               
                    		if( PhoneApp.getInstance().isVTRinging())
                    		{
                    			try
                    			{
                    				PhoneApp.getInstance().getInCallScreenInstance().getInCallTouchUi().touchAnswerCall();
                    			}catch(Exception ex)  
                        		{}
                    		}
                    		else
                    		{
                    			answerRingingCallInternal();
                    		}
                                
                    	}
                    	else
                    	{
                    		answerRingingCallInternal();
                    	}
                    }
                    break;

                case CMD_HANDLE_GET_SCA:
					request = (MainThreadRequest)msg.obj;
					onCompleted = obtainMessage(CMD_GET_SCA_DONE, request);

					if(request.argument == null) {
						// non-gemini
					} else {
					    ScAddrGemini sca = (ScAddrGemini)request.argument;
						int simId = sca.simId;
						if(FeatureOption.MTK_GEMINI_SUPPORT) {
							Log.d(LOG_TAG, "[sca get sc gemini");
						    ((GeminiPhone)mPhone).getSmscAddressGemini(onCompleted, simId);
						} else  {
						    Log.d(LOG_TAG, "[sca get sc single");
						    mPhone.getSmscAddress(onCompleted);
						}
					}
					break;

				case CMD_GET_SCA_DONE:
					ar = (AsyncResult)msg.obj;
					request = (MainThreadRequest)ar.userObj;

					if(ar.exception == null && ar.result != null) {
						Log.d(LOG_TAG, "[sca get result");
						request.result = ar.result;
					} else {
					    Log.d(LOG_TAG, "[sca Fail to get sc address");
						request.result = new String("");
					}

					synchronized(request) {
						Log.d(LOG_TAG, "[sca notify sleep thread");
						request.notifyAll();
					}
					break;

				case CMD_HANDLE_SET_SCA:
					request = (MainThreadRequest)msg.obj;
					onCompleted = obtainMessage(CMD_SET_SCA_DONE, request);

					ScAddrGemini sca = (ScAddrGemini)request.argument;
					if(sca.simId == -1) {
						// non-gemini
					} else {
					    if(FeatureOption.MTK_GEMINI_SUPPORT) {
							Log.d(LOG_TAG, "[sca set sc gemini");
						    ((GeminiPhone)mPhone).setSmscAddressGemini(sca.scAddr, onCompleted, sca.simId);
					    } else {
					        Log.d(LOG_TAG, "[sca set sc single");
					        mPhone.setSmscAddress(sca.scAddr, onCompleted);
					    }
					}
					break;
				case CMD_SET_SCA_DONE:
					ar = (AsyncResult)msg.obj;
                	request = (MainThreadRequest)ar.userObj;
                	if(ar.exception != null) {
                		Log.d(LOG_TAG, "[sca Fail: set sc address");
                	} else {
                	    Log.d(LOG_TAG, "[sca Done: set sc address");
                	}
                	request.result = new Object();

					synchronized(request) {
                		request.notifyAll();
                	}
					break;
                //MTK-END [mtk04070][111117][ALPS00093395]MTK added
				
               // NFC SEEK start
               case CMD_SIM_IO:
                    request = (MainThreadRequest) msg.obj;
                    IccAPDUArgument parameters =
                            (IccAPDUArgument) request.argument;
                    onCompleted = obtainMessage(EVENT_SIM_IO_DONE,request);
                    mPhone.getIccCard().exchangeSimIO( parameters.cla, /* fileID */
                           parameters.command,
                            parameters.p1, parameters.p2, parameters.p3,
                            parameters.data, onCompleted);
                    break;

               case EVENT_SIM_IO_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                        lastError = 0;
                    } else {
                        request.result = new IccIoResult(0x6f, 0, (byte[])null);
                        lastError = 1;
                        if ((ar.exception != null) &&
                                (ar.exception instanceof CommandException)) {
                            if (((CommandException)ar.exception).getCommandError() ==
                                    CommandException.Error.INVALID_PARAMETER) {
                                lastError = 5;
                    }}}
                    synchronized (request) { request.notifyAll(); }
                    break;		

               case CMD_SIM_IO_1:
                    request = (MainThreadRequest) msg.obj;
                    IccAPDUArgument parameters1 =
                            (IccAPDUArgument) request.argument;
                    onCompleted = obtainMessage(EVENT_SIM_IO_1_DONE,request);
                    ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_1).exchangeSimIO( parameters1.cla, /* fileID */
                           parameters1.command,
                            parameters1.p1, parameters1.p2, parameters1.p3,
                            parameters1.data, onCompleted);
                    break;

               case CMD_SIM_IO_2:
                    request = (MainThreadRequest) msg.obj;
                    IccAPDUArgument parameters2 =
                            (IccAPDUArgument) request.argument;
                    onCompleted = obtainMessage(EVENT_SIM_IO_2_DONE,request);
                    ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_2).exchangeSimIO( parameters2.cla, /* fileID */
                           parameters2.command,
                            parameters2.p1, parameters2.p2, parameters2.p3,
                            parameters2.data, onCompleted);
                    break;

               case EVENT_SIM_IO_1_DONE:
               case EVENT_SIM_IO_2_DONE:
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                        if(msg.what == EVENT_SIM_IO_1_DONE)
                            lastError1 = 0;
                        else
                            lastError2 = 0;
                    } else {
                        request.result = new IccIoResult(0x6f, 0, (byte[])null);
                        if(msg.what == EVENT_SIM_IO_1_DONE)
                            lastError1 = 1;
                        else
                            lastError2 = 1;
                        if ((ar.exception != null) &&
                                (ar.exception instanceof CommandException)) {
                            if (((CommandException)ar.exception).getCommandError() ==
                                    CommandException.Error.INVALID_PARAMETER) {
                                if(msg.what == EVENT_SIM_IO_1_DONE)
                                    lastError1 = 5;
                                else
                                    lastError2 = 5;
                    }}}
                    synchronized (request) { request.notifyAll(); }
                    break;	

					
               case CMD_EXCHANGE_APDU:
                   request = (MainThreadRequest) msg.obj;
                   IccAPDUArgument argument =
                           (IccAPDUArgument) request.argument;
                   onCompleted = obtainMessage(EVENT_EXCHANGE_APDU_DONE,
                           request);
                   mPhone.getIccCard().exchangeAPDU(argument.cla,
                           argument.command,
                           argument.channel, argument.p1, argument.p2,
                           argument.p3, argument.data, onCompleted);
                   break;

               case EVENT_EXCHANGE_APDU_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null && ar.result != null) {
                       request.result = ar.result;
                       lastError = 0;
                   } else {
                       request.result = new IccIoResult(0x6f, 0,
                               (byte[])null);
                       lastError = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.INVALID_PARAMETER) {
                               lastError = 5;
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_EXCHANGE_APDU_1:
                   request = (MainThreadRequest) msg.obj;
                   IccAPDUArgument argument1 =
                           (IccAPDUArgument) request.argument;
                   onCompleted = obtainMessage(EVENT_EXCHANGE_APDU_1_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_1).exchangeAPDU(argument1.cla,
                           argument1.command,
                           argument1.channel, argument1.p1, argument1.p2,
                           argument1.p3, argument1.data, onCompleted);
                   break;

               case CMD_EXCHANGE_APDU_2:
                   request = (MainThreadRequest) msg.obj;
                   IccAPDUArgument argument2 =
                           (IccAPDUArgument) request.argument;
                   onCompleted = obtainMessage(EVENT_EXCHANGE_APDU_2_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_2).exchangeAPDU(argument2.cla,
                           argument2.command,
                           argument2.channel, argument2.p1, argument2.p2,
                           argument2.p3, argument2.data, onCompleted);
                   break;

               case EVENT_EXCHANGE_APDU_1_DONE:
               case EVENT_EXCHANGE_APDU_2_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null && ar.result != null) {
                       request.result = ar.result;
                       if(msg.what == EVENT_EXCHANGE_APDU_1_DONE)
                           lastError1 = 0;
                       else
                           lastError2 = 0;
                   } else {
                       request.result = new IccIoResult(0x6f, 0,
                               (byte[])null);
                       if(msg.what == EVENT_EXCHANGE_APDU_1_DONE)
                           lastError1 = 1;
                       else
                           lastError2 = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.INVALID_PARAMETER) {
                               if(msg.what == EVENT_EXCHANGE_APDU_1_DONE)
                                   lastError1 = 5;
                               else
                                   lastError2 = 5;
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_OPEN_CHANNEL:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_OPEN_CHANNEL_DONE,
                           request);
                   mPhone.getIccCard().openLogicalChannel(
                           (String)request.argument, onCompleted);
                   break;

               case EVENT_OPEN_CHANNEL_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null && ar.result != null) {
                       request.result = new Integer(((int[])ar.result)[0]);
                       lastError = 0;
                   } else {
                       request.result = new Integer(0);
                       lastError = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.MISSING_RESOURCE) {
                               lastError = 2;
                           } else {
                               if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.NO_SUCH_ELEMENT) {
                                   lastError = 3;
                               }
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_OPEN_CHANNEL_1:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_OPEN_CHANNEL_1_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_1).openLogicalChannel(
                           (String)request.argument, onCompleted);
                   break;

               case CMD_OPEN_CHANNEL_2:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_OPEN_CHANNEL_2_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_2).openLogicalChannel(
                           (String)request.argument, onCompleted);
                   break;

               case EVENT_OPEN_CHANNEL_1_DONE:
               case EVENT_OPEN_CHANNEL_2_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null && ar.result != null) {
                       request.result = new Integer(((int[])ar.result)[0]);
                       if(msg.what == EVENT_OPEN_CHANNEL_1_DONE)
                           lastError1 = 0;
                       else
                           lastError2 = 0;
                   } else {
                       request.result = new Integer(0);
                       if(msg.what == EVENT_OPEN_CHANNEL_1_DONE)
                           lastError1 = 1;
                       else
                           lastError2 = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.MISSING_RESOURCE) {
                               if(msg.what == EVENT_OPEN_CHANNEL_1_DONE)
                                   lastError1 = 2;
                               else
                                   lastError2 = 2;
                           } else {
                               if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.NO_SUCH_ELEMENT) {
                                   if(msg.what == EVENT_OPEN_CHANNEL_1_DONE)
                                       lastError1 = 3;
                                   else
                                       lastError2 = 3;
                               }
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_CLOSE_CHANNEL:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_CLOSE_CHANNEL_DONE,
                           request);
                   mPhone.getIccCard().closeLogicalChannel(
                           ((Integer)request.argument).intValue(),
                           onCompleted);
                   break;

               case EVENT_CLOSE_CHANNEL_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null) {
                       request.result = new Integer(0);
                       lastError = 0;
                   } else {
                       request.result = new Integer(-1);
                       lastError = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.INVALID_PARAMETER) {
                               lastError = 5;
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_CLOSE_CHANNEL_1:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_CLOSE_CHANNEL_1_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_1).closeLogicalChannel(
                           ((Integer)request.argument).intValue(),
                           onCompleted);
                   break;

               case CMD_CLOSE_CHANNEL_2:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_CLOSE_CHANNEL_2_DONE,
                           request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_2).closeLogicalChannel(
                           ((Integer)request.argument).intValue(),
                           onCompleted);
                   break;

               case EVENT_CLOSE_CHANNEL_1_DONE:
               case EVENT_CLOSE_CHANNEL_2_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if (ar.exception == null) {
                       request.result = new Integer(0);
                       if(msg.what == EVENT_CLOSE_CHANNEL_1_DONE)
                           lastError1 = 0;
                       else
                           lastError2 = 0;
                   } else {
                       request.result = new Integer(-1);
                       if(msg.what == EVENT_CLOSE_CHANNEL_1_DONE)
                           lastError1 = 1;
                       else
                           lastError2 = 1;
                       if ((ar.exception != null) &&
                               (ar.exception instanceof CommandException)) {
                           if (((CommandException)ar.exception)
                                   .getCommandError() ==
                                   CommandException.Error.INVALID_PARAMETER) {
                               if(msg.what == EVENT_CLOSE_CHANNEL_1_DONE)
                                   lastError1 = 5;
                               else
                                   lastError2 = 5;
                           }
                       }
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_GET_ATR:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_GET_ATR_DONE,
                            request);
                   mPhone.getIccCard().iccGetATR(onCompleted);
                   break;

               case EVENT_GET_ATR_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if(ar.exception == null) {
                       request.result = ar.result;
                       lastError = 0;
                   } else {
                       request.result = null;
                       lastError = 1;
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;

               case CMD_GET_ATR_1:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_GET_ATR_1_DONE,
                            request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_1).iccGetATR(onCompleted);
                   break;

               case CMD_GET_ATR_2:
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_GET_ATR_2_DONE,
                            request);
                   ((GeminiPhone)mPhone).getIccCardGemini(Phone.GEMINI_SIM_2).iccGetATR(onCompleted);
                   break;

               case EVENT_GET_ATR_1_DONE:
               case EVENT_GET_ATR_2_DONE:
                   ar = (AsyncResult) msg.obj;
                   request = (MainThreadRequest) ar.userObj;
                   if(ar.exception == null) {
                       request.result = ar.result;
                       if(msg.what == EVENT_GET_ATR_1_DONE)
                           lastError1 = 0;
                       else
                           lastError2 = 0;
                   } else {
                       request.result = null;
                       if(msg.what == EVENT_GET_ATR_1_DONE)
                           lastError1 = 1;
                       else
                           lastError2 = 1;
                   }
                   synchronized (request) {
                       request.notifyAll();
                   }
                   break;
               // NFC SEEK end

//MTK-START [mtk04258][20120806][International card support]
               case CMD_GET_OPERATOR_LIMIT:
                   Log.d(LOG_TAG, "CMD_GET_OPERATOR_LIMIT");
                   request = (MainThreadRequest) msg.obj;
                   onCompleted = obtainMessage(EVENT_GET_OPERATOR_LIMIT_DONE,
                            request);
                   ((GeminiPhone)mPhone).getLimitServiceNetworkOperator(((Integer)request.argument).intValue(), onCompleted);
                   break;

               case EVENT_GET_OPERATOR_LIMIT_DONE:
                   Log.d(LOG_TAG, "EVENT_GET_OPERATOR_LIMIT_DONE");
                    ar = (AsyncResult) msg.obj;
                    request = (MainThreadRequest) ar.userObj;
                    if (ar.exception == null && ar.result != null) {
                        request.result = ar.result;
                    } else {
                        String[] result = new String[3];
						result[0] = "";
						result[1] = "";
						result[2] = "";

						request.result = result;
                    }
                    // Wake up the requesting thread
                    synchronized (request) {
                        request.notifyAll();
                    }
			       break;	   
//MTK-END [mtk04258][20120806][International card support]

                default:
                    Log.w(LOG_TAG, "MainThreadHandler: unexpected message code: " + msg.what);
                    break;
            }
        }
    }

    /**
     * Posts the specified command to be executed on the main thread,
     * waits for the request to complete, and returns the result.
     * @see sendRequestAsync
     */
    private Object sendRequest(int command, Object argument) {
        if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
            throw new RuntimeException("This method will deadlock if called from the main thread.");
        }

        MainThreadRequest request = new MainThreadRequest(argument);
        Message msg = mMainThreadHandler.obtainMessage(command, request);
        msg.sendToTarget();

        // Wait for the request to complete
        synchronized (request) {
            while (request.result == null) {
                try {
                    request.wait();
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }
        return request.result;
    }

    /**
     * Asynchronous ("fire and forget") version of sendRequest():
     * Posts the specified command to be executed on the main thread, and
     * returns immediately.
     * @see sendRequest
     */
    private void sendRequestAsync(int command) {
        mMainThreadHandler.sendEmptyMessage(command);
    }

    /**
     * Initialize the singleton PhoneInterfaceManager instance.
     * This is only done once, at startup, from PhoneApp.onCreate().
     */
    /* package */ static PhoneInterfaceManager init(PhoneApp app, Phone phone) {
        synchronized (PhoneInterfaceManager.class) {
            if (sInstance == null) {
                sInstance = new PhoneInterfaceManager(app, phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    /** Private constructor; @see init() */
    private PhoneInterfaceManager(PhoneApp app, Phone phone) {
        mApp = app;
        mPhone = phone;
        mCM = PhoneApp.getInstance().mCM;
        mMainThreadHandler = new MainThreadHandler();
        publish();
    }

    private void publish() {
        if (DBG) log("publish: " + this);

        ServiceManager.addService("phone", this);
    }

    //
    // Implementation of the ITelephony interface.
    //

    public void dial(String number) {
        if (DBG) log("dial: " + number);
        // No permission check needed here: This is just a wrapper around the
        // ACTION_DIAL intent, which is available to any app since it puts up
        // the UI before it does anything.

        String url = createTelUrl(number);
        if (url == null) {
            return;
        }

        // PENDING: should we just silently fail if phone is offhook or ringing?
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        /* Fion add start */
        Phone.State state;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK	
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }	
        /* Fion add end */
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
        if (state != Phone.State.OFFHOOK && state != Phone.State.RINGING) {
            Intent  intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mApp.startActivity(intent);
        }
    }

    public void call(String number) {
        if (DBG) log("call: " + number);

        // This is just a wrapper around the ACTION_CALL intent, but we still
        // need to do a permission check since we're calling startActivity()
        // from the context of the phone app.
        enforceCallPermission();

        String url = createTelUrl(number);
        if (url == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApp.startActivity(intent);
    }

    private boolean showCallScreenInternal(boolean specifyInitialDialpadState,
                                           boolean initialDialpadState) {
        if (!PhoneApp.sVoiceCapable) {
            // Never allow the InCallScreen to appear on data-only devices.
            return false;
        }
        if (isIdle()) {
            return false;
        }
        // If the phone isn't idle then go to the in-call screen
        long callingId = Binder.clearCallingIdentity();
        try {
            Intent intent;
            if (specifyInitialDialpadState) {
                intent = PhoneApp.createInCallIntent(initialDialpadState);
            } else {
                intent = PhoneApp.createInCallIntent();
            }
            try {
                mApp.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // It's possible that the in-call UI might not exist
                // (like on non-voice-capable devices), although we
                // shouldn't be trying to bring up the InCallScreen on
                // devices like that in the first place!
                Log.w(LOG_TAG, "showCallScreenInternal: "
                      + "transition to InCallScreen failed; intent = " + intent);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
        return true;
    }

    // Show the in-call screen without specifying the initial dialpad state.
    public boolean showCallScreen() {
        return showCallScreenInternal(false, false);
    }

    // The variation of showCallScreen() that specifies the initial dialpad state.
    // (Ideally this would be called showCallScreen() too, just with a different
    // signature, but AIDL doesn't allow that.)
    public boolean showCallScreenWithDialpad(boolean showDialpad) {
        return showCallScreenInternal(true, showDialpad);
    }

    /**
     * End a call based on call state
     * @return true is a call was ended
     */
    public boolean endCall() {
        enforceCallPermission();
        return (Boolean) sendRequest(CMD_END_CALL, null);
    }

    public void answerRingingCall() {
        if (DBG) log("answerRingingCall...");
        // TODO: there should eventually be a separate "ANSWER_PHONE" permission,
        // but that can probably wait till the big TelephonyManager API overhaul.
        // For now, protect this call with the MODIFY_PHONE_STATE permission.
        enforceModifyPermission();
        sendRequestAsync(CMD_ANSWER_RINGING_CALL);
    }

    /**
     * Make the actual telephony calls to implement answerRingingCall().
     * This should only be called from the main thread of the Phone app.
     * @see answerRingingCall
     *
     * TODO: it would be nice to return true if we answered the call, or
     * false if there wasn't actually a ringing incoming call, or some
     * other error occurred.  (In other words, pass back the return value
     * from PhoneUtils.answerCall() or PhoneUtils.answerAndEndActive().)
     * But that would require calling this method via sendRequest() rather
     * than sendRequestAsync(), and right now we don't actually *need* that
     * return value, so let's just return void for now.
     */
    private void answerRingingCallInternal() {
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        /* Fion add start */
        boolean hasRingingCall , hasActiveCall , hasHoldingCall;

        //Temp solution for ALPS00283807
        if (DualTalkUtils.isSupportDualTalk) {
            PhoneApp.getInstance().getInCallScreenInstance().getInCallTouchUi().touchAnswerCall();
            return ;
        }
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            hasRingingCall = !((GeminiPhone)mPhone).getRingingCall().isIdle();
            hasActiveCall = !((GeminiPhone)mPhone).getForegroundCall().isIdle();
            hasHoldingCall = !((GeminiPhone)mPhone).getBackgroundCall().isIdle();
        }
        else
        {
            hasRingingCall = !mPhone.getRingingCall().isIdle();
            hasActiveCall = !mPhone.getForegroundCall().isIdle();
            hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
        }	
        /* Fion add end */
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified

	Call ringing = mCM.getFirstActiveRingingCall();

        if (hasRingingCall) {
            //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
            //final boolean hasActiveCall = !mPhone.getForegroundCall().isIdle();
            //final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
            //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
            if (hasActiveCall && hasHoldingCall) {
                // Both lines are in use!
                // TODO: provide a flag to let the caller specify what
                // policy to use if both lines are in use.  (The current
                // behavior is hardwired to "answer incoming, end ongoing",
                // which is how the CALL button is specced to behave.)
                PhoneUtils.answerAndEndActive(mCM, ringing);
                return;
            } else {
                // answerCall() will automatically hold the current active
                // call, if there is one.
                PhoneUtils.answerCall(ringing);
                return;
            }
        } else {
            /* Solve [ALPS00272855]Can not answer SIP MT call via headset, mtk04070, 20120425  */
            /* Check if the type of ringing call is SIP */
            hasRingingCall = mCM.hasActiveRingingCall();
	    if (hasRingingCall) {
		Phone phone = mCM.getRingingPhone();
		int phoneType = phone.getPhoneType();
	        if (phoneType == Phone.PHONE_TYPE_SIP) {
                   log("answerRingingCallInternal: answering (SIP)...");
                   if (mCM.hasActiveFgCall() &&
                      mCM.getFgPhone().getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                      // Similar to the PHONE_TYPE_CDMA handling.
                      // The incoming call is SIP call and the ongoing
                      // call is a CDMA call. The CDMA network does not
                      // support holding an active call, so there's no
                      // way to swap between a CDMA call and a SIP call.
                      // So for now, we just don't allow a CDMA call and
                      // a SIP call to be active at the same time.We'll
                      // "answer incoming, end ongoing" in this case.
                      if (DBG) log("answerRingingCallInternal: answer SIP incoming and end CDMA ongoing");
                      PhoneUtils.answerAndEndActive(mCM, ringing);
                   } else if (mCM.hasActiveFgCall() &&
                             (mCM.getFgPhone().getPhoneType() != Phone.PHONE_TYPE_CDMA) &&
                             mCM.hasActiveBgCall()) {
                             PhoneUtils.answerAndEndActive(mCM, ringing);
                   } else {
                     PhoneUtils.answerCall(ringing);
                   }
	        }
	    }/* hasRingingCall */
            return;
        }
    }

    public void silenceRinger() {
        if (DBG) log("silenceRinger...");
        // TODO: find a more appropriate permission to check here.
        // (That can probably wait till the big TelephonyManager API overhaul.
        // For now, protect this call with the MODIFY_PHONE_STATE permission.)
        enforceModifyPermission();
        sendRequestAsync(CMD_SILENCE_RINGER);
    }

    /**
     * Internal implemenation of silenceRinger().
     * This should only be called from the main thread of the Phone app.
     * @see silenceRinger
     */
    private void silenceRingerInternal() {
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        /* Fion add start */
        Phone.State state;
        /*if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK	
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }*/
        state = mCM.getState();
        if ((state == Phone.State.RINGING)
            && mApp.notifier.isRinging()) {
        /* Fion add end */
            // Ringer is actually playing, so silence it.
            if (DBG) log("silenceRingerInternal: silencing...");
            //Yunfei.Liu Google removed setAudioConotrolState on Android2.3, further check this
            //PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_IDLE);
            mApp.notifier.silenceRinger();
        }
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
    }

    public boolean isOffhook() {
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        /* Solve [ALPS00292626]The SIP call can't be ended when we has enabled "Power button ends call". mtk04070, 20120530 */
        Phone.State state = mCM.getState();
        /*
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK	
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }			
        */
        
        Log.d(LOG_TAG, "state = " + state);
		
        return (state == Phone.State.OFFHOOK);
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
    }

    public boolean isRinging() {
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        Phone.State state = mCM.getState();
        /*if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK
            
            //Give an chance to get the correct status for SIP on gemini platform
            if (state == Phone.State.IDLE)
            {
            	state = mCM.getState();
            }
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }*/			
        return (state == Phone.State.RINGING);
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
    }

    public boolean isIdle() {
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        Phone.State state;
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK	
            if (state == Phone.State.IDLE)
            {
                state = mCM.getState();
            }
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }						
        return (state == Phone.State.IDLE);
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
    }

    public boolean isSimPinEnabled() {
        enforceReadPermission();
        return (PhoneApp.getInstance().isSimPinEnabled());
    }

    public boolean supplyPin(String pin) {
        enforceModifyPermission();
        final UnlockSim checkSimPin = new UnlockSim(mPhone.getIccCard());
        checkSimPin.start();
        return checkSimPin.unlockSim(null, pin);
    }

    public boolean supplyPuk(String puk, String pin) {
        enforceModifyPermission();
        final UnlockSim checkSimPuk = new UnlockSim(mPhone.getIccCard());
        checkSimPuk.start();
        return checkSimPuk.unlockSim(puk, pin);
    }

    /**
     * Helper thread to turn async call to {@link SimCard#supplyPin} into
     * a synchronous one.
     */
    private static class UnlockSim extends Thread {

        private final IccCard mSimCard;

        private boolean mDone = false;
        private boolean mResult = false;

        // For replies from SimCard interface
        private Handler mHandler;

        // For async handler to identify request type
        private static final int SUPPLY_PIN_COMPLETE = 100;
        //MTK-START [mtk04070][111117][ALPS00093395]MTK added
        private static final int SUPPLY_PUK_COMPLETE = 101;
        //MTK-END [mtk04070][111117][ALPS00093395]MTK added

        public UnlockSim(IccCard simCard) {
            mSimCard = simCard;
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (UnlockSim.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case SUPPLY_PIN_COMPLETE:
                            //MTK-START [mtk04070][111117][ALPS00093395]MTK added
                            case SUPPLY_PUK_COMPLETE:
                            //MTK-END [mtk04070][111117][ALPS00093395]MTK added
                                Log.d(LOG_TAG, "SUPPLY_PIN_COMPLETE");
                                synchronized (UnlockSim.this) {
                                    mResult = (ar.exception == null);
                                    mDone = true;
                                    UnlockSim.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                UnlockSim.this.notifyAll();
            }
            Looper.loop();
        }

        /*
         * Use PIN or PUK to unlock SIM card
         *
         * If PUK is null, unlock SIM card with PIN
         *
         * If PUK is not null, unlock SIM card with PUK and set PIN code
         */
        synchronized boolean unlockSim(String puk, String pin) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, SUPPLY_PIN_COMPLETE);

            if (puk == null) {
                mSimCard.supplyPin(pin, callback);
            } else {
                mSimCard.supplyPuk(puk, pin, callback);
            }

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }
    }

    public void updateServiceLocation() {
        // No permission check needed here: this call is harmless, and it's
        // needed for the ServiceState.requestStateUpdate() call (which is
        // already intentionally exposed to 3rd parties.)
        mPhone.updateServiceLocation();
    }

    public boolean isRadioOn() {
       //MTK-START [mtk04070][111117][ALPS00093395]For supporting Gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT != true) {           
        return mPhone.getServiceState().getState() != ServiceState.STATE_POWER_OFF;
        }
        else {
            return ((GeminiPhone)mPhone).getServiceStateGemini(Phone.GEMINI_SIM_1).getState() != ServiceState.STATE_POWER_OFF || 
                        ((GeminiPhone)mPhone).getServiceStateGemini(Phone.GEMINI_SIM_2).getState() != ServiceState.STATE_POWER_OFF;
        }
       //MTK-END [mtk04070][111117][ALPS00093395]For supporting Gemini
    }

    public void toggleRadioOnOff() {
        enforceModifyPermission();
       //MTK-START [mtk04070][111117][ALPS00093395]For supporting Gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT != true)   
        mPhone.setRadioPower(!isRadioOn());
        else        
            ((GeminiPhone)mPhone).setRadioMode(isRadioOn() ?  GeminiNetworkSubUtil.MODE_FLIGHT_MODE:GeminiNetworkSubUtil.MODE_DUAL_SIM);
       //MTK-END [mtk04070][111117][ALPS00093395]For supporting Gemini
    }
    public boolean setRadio(boolean turnOn) {
        enforceModifyPermission();
        if ((mPhone.getServiceState().getState() != ServiceState.STATE_POWER_OFF) != turnOn) {
            toggleRadioOnOff();
        }
        return true;
    }

    public boolean enableDataConnectivity() {
        enforceModifyPermission();
        ConnectivityManager cm =
                (ConnectivityManager)mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(true);
        return true;
    }

    public int enableApnType(String type) {
        enforceModifyPermission();
        return mPhone.enableApnType(type);
    }

    public int disableApnType(String type) {
        enforceModifyPermission();
        return mPhone.disableApnType(type);
    }

    public boolean disableDataConnectivity() {
        enforceModifyPermission();
        ConnectivityManager cm =
                (ConnectivityManager)mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(false);
        return true;
    }

    public boolean isDataConnectivityPossible() {
        return mPhone.isDataConnectivityPossible();
    }

    public boolean handlePinMmi(String dialString) {
        enforceModifyPermission();
        //MTK-START [mtk04070][111117][ALPS00093395]MTK modified
        return (Boolean) sendRequest(CMD_HANDLE_PIN_MMI, new PinMmiGemini(dialString, -1));
        //MTK-END [mtk04070][111117][ALPS00093395]MTK modified
    }

    public void cancelMissedCallsNotification() {
        enforceModifyPermission();
        mApp.notificationMgr.cancelMissedCallNotification();
    }

    public int getCallState() {
        return DefaultPhoneNotifier.convertCallState(mPhone.getState());
    }

    public int getDataState() {
        return DefaultPhoneNotifier.convertDataState(mPhone.getDataConnectionState());
    }

    public int getDataActivity() {
        return DefaultPhoneNotifier.convertDataActivityState(mPhone.getDataActivityState());
    }

    public Bundle getCellLocation() {
        try {
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from ACCESS_COARSE_LOCATION since this
            // is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }
        Bundle data = new Bundle();
        mPhone.getCellLocation().fillInNotifierBundle(data);
        return data;
    }

    public void enableLocationUpdates() {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        mPhone.enableLocationUpdates();
    }

    public void disableLocationUpdates() {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        mPhone.disableLocationUpdates();
    }

    @SuppressWarnings("unchecked")
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        try {
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check
            // for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from
            // ACCESS_COARSE_LOCATION since this is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }

        ArrayList<NeighboringCellInfo> cells = null;

        try {
            cells = (ArrayList<NeighboringCellInfo>) sendRequest(
                    CMD_HANDLE_NEIGHBORING_CELL, null);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "getNeighboringCellInfo " + e);
        }

        return (List <NeighboringCellInfo>) cells;
    }


    //
    // Internal helper methods.
    //

    /**
     * Make sure the caller has the READ_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceReadPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE, null);
    }

    /**
     * Make sure the caller has the MODIFY_PHONE_STATE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceModifyPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE, null);
    }

    /**
     * Make sure the caller has the CALL_PHONE permission.
     *
     * @throws SecurityException if the caller does not have the required permission
     */
    private void enforceCallPermission() {
        mApp.enforceCallingOrSelfPermission(android.Manifest.permission.CALL_PHONE, null);
    }


    private String createTelUrl(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }

        StringBuilder buf = new StringBuilder("tel:");
        buf.append(number);
        return buf.toString();
    }

    private void log(String msg) {
        Log.d(LOG_TAG, "[PhoneIntfMgr] " + msg);
    }

    public int getActivePhoneType() {
        return mPhone.getPhoneType();
    }

    /**
     * Returns the CDMA ERI icon index to display
     */
    public int getCdmaEriIconIndex() {
        return mPhone.getCdmaEriIconIndex();
    }

    /**
     * Returns the CDMA ERI icon mode,
     * 0 - ON
     * 1 - FLASHING
     */
    public int getCdmaEriIconMode() {
        return mPhone.getCdmaEriIconMode();
    }

    /**
     * Returns the CDMA ERI text,
     */
    public String getCdmaEriText() {
        return mPhone.getCdmaEriText();
    }

    /**
     * Returns true if CDMA provisioning needs to run.
     */
    public boolean needsOtaServiceProvisioning() {
        return mPhone.needsOtaServiceProvisioning();
    }

    /**
     * Returns the unread count of voicemails
     */
    public int getVoiceMessageCount() {
        return mPhone.getVoiceMessageCount();
    }

    /**
     * Returns the network type
     */
    public int getNetworkType() {
        int radiotech = mPhone.getServiceState().getRadioTechnology();
        switch(radiotech) {
            case ServiceState.RADIO_TECHNOLOGY_GPRS:
                return TelephonyManager.NETWORK_TYPE_GPRS;
            case ServiceState.RADIO_TECHNOLOGY_EDGE:
                return TelephonyManager.NETWORK_TYPE_EDGE;
            case ServiceState.RADIO_TECHNOLOGY_UMTS:
                return TelephonyManager.NETWORK_TYPE_UMTS;
            case ServiceState.RADIO_TECHNOLOGY_HSDPA:
                return TelephonyManager.NETWORK_TYPE_HSDPA;
            case ServiceState.RADIO_TECHNOLOGY_HSUPA:
                return TelephonyManager.NETWORK_TYPE_HSUPA;
            case ServiceState.RADIO_TECHNOLOGY_HSPA:
                return TelephonyManager.NETWORK_TYPE_HSPA;
            case ServiceState.RADIO_TECHNOLOGY_IS95A:
            case ServiceState.RADIO_TECHNOLOGY_IS95B:
                return TelephonyManager.NETWORK_TYPE_CDMA;
            case ServiceState.RADIO_TECHNOLOGY_1xRTT:
                return TelephonyManager.NETWORK_TYPE_1xRTT;
            case ServiceState.RADIO_TECHNOLOGY_EVDO_0:
                return TelephonyManager.NETWORK_TYPE_EVDO_0;
            case ServiceState.RADIO_TECHNOLOGY_EVDO_A:
                return TelephonyManager.NETWORK_TYPE_EVDO_A;
            case ServiceState.RADIO_TECHNOLOGY_EVDO_B:
                return TelephonyManager.NETWORK_TYPE_EVDO_B;
            case ServiceState.RADIO_TECHNOLOGY_EHRPD:
                return TelephonyManager.NETWORK_TYPE_EHRPD;
            case ServiceState.RADIO_TECHNOLOGY_LTE:
                return TelephonyManager.NETWORK_TYPE_LTE;
            case ServiceState.RADIO_TECHNOLOGY_HSPAP:
                return TelephonyManager.NETWORK_TYPE_HSPAP;
            default:
                return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
    }

    /**
     * @return true if a ICC card is present
     */
    public boolean hasIccCard() {
        return mPhone.getIccCard().hasIccCard();
    }

    /**
     * Return if the current radio is LTE on CDMA. This
     * is a tri-state return value as for a period of time
     * the mode may be unknown.
     *
     * @return {@link Phone#LTE_ON_CDMA_UNKNOWN}, {@link Phone#LTE_ON_CDMA_FALSE}
     * or {@link PHone#LTE_ON_CDMA_TRUE}
     */
    public int getLteOnCdmaMode() {
        return mPhone.getLteOnCdmaMode();
    }

    // NFC SEEK start
    public byte[] transmitIccSimIO(int fileID, int command,
                                               int p1, int p2, int p3, String filePath) {
        log("NFC test for transmitIccSimIO");
        //if (Binder.getCallingUid() != Process.SMARTCARD_UID)
          //  throw new SecurityException("Only Smartcard API may access UICC");
        Log.d(LOG_TAG, "Exchange SIM_IO " + fileID + ":" + command + " " +
                 p1 + " " + p2 + " " + p3 + ":" + filePath);
        IccIoResult response =
                (IccIoResult)sendRequest(CMD_SIM_IO,
                        new IccAPDUArgument(fileID, command, -1,
                        p1, p2, p3, filePath));
        Log.d(LOG_TAG, "Exchange SIM_IO [R]" + response);
        byte[] result=null; int length=2;
        if (response.payload != null) {
            length=2+response.payload.length;
            result=new byte[length];
            System.arraycopy(response.payload,0,result,0,response.payload.length);
        } else result=new byte[length];
        Log.d(LOG_TAG,"Exchange SIM_IO [L] "+length);
        result[length-1]=(byte)response.sw2;
        result[length-2]=(byte)response.sw1;
        return result;
    }

    public byte[] transmitIccSimIOGemini(int fileID, int command,
                                               int p1, int p2, int p3, String filePath, int simId) {
        /* Disable NFC SEEK Project code, need to modify Process to support -----
        if (Binder.getCallingUid() != Process.SMARTCARD_UID)
            throw new SecurityException("Only Smartcard API may access UICC");*/
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("transmitIccSimIOGemini: simId " + simId + " error");
            return null;
        }

        int commandID = CMD_SIM_IO_1;
        if (simId == Phone.GEMINI_SIM_2)
            commandID = CMD_SIM_IO_2;

        log("Exchange SIM_IO Gemini " + fileID + ":" + command + " " +
                 p1 + " " + p2 + " " + p3 + ":" + filePath + ", " + simId);

        IccIoResult response =
                (IccIoResult)sendRequest(commandID,
                        new IccAPDUArgument(fileID, command, -1,
                        p1, p2, p3, filePath));

        log("Exchange SIM_IO Gemini [R]" + response);
        byte[] result=null; int length=2;
        if (response.payload != null) {
            length=2+response.payload.length;
            result=new byte[length];
            System.arraycopy(response.payload,0,result,0,response.payload.length);
        } else result=new byte[length];
        log("Exchange SIM_IO Gemini [L] "+length);
        result[length-1]=(byte)response.sw2;
        result[length-2]=(byte)response.sw1;
        return result;
    }
	
    private String exchangeIccAPDU(int cla, int command,
            int channel, int p1, int p2, int p3, String data) {
        log("NFC test for exchangeIccAPDU");
        //if (Binder.getCallingUid() != Process.SMARTCARD_UID)
          //  throw new SecurityException("Only Smartcard API may access UICC");
        Log.d(LOG_TAG, "> exchangeAPDU " + channel + " " + cla + " " +
                command + " " + p1 + " " + p2 + " " + p3 + " " + data);
        IccIoResult response =
                (IccIoResult)sendRequest(CMD_EXCHANGE_APDU,
                        new IccAPDUArgument(cla, command, channel,
                        p1, p2, p3, data));
        Log.d(LOG_TAG, "< exchangeAPDU " + response);
        String s = Integer.toHexString(
                (response.sw1 << 8) + response.sw2 + 0x10000).substring(1);
        if (response.payload != null)
            s = IccUtils.bytesToHexString(response.payload) + s;
        return s;
    }

    private String exchangeIccAPDUGemini(int cla, int command,
            int channel, int p1, int p2, int p3, String data, int simId) {
        /* Disable NFC SEEK Project code, need to modify Process to support -----
        if (Binder.getCallingUid() != Process.SMARTCARD_UID)
            throw new SecurityException("Only Smartcard API may access UICC");*/
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("exchangeIccAPDUGemini: simId " + simId + " error");
            return null;
        }

        int commandID = CMD_EXCHANGE_APDU_1;
        if (simId == Phone.GEMINI_SIM_2)
            commandID = CMD_EXCHANGE_APDU_2;

        log("> exchangeAPDUGemini " + channel + " " + cla + " " +
                command + " " + p1 + " " + p2 + " " + p3 + " " + data + ", " + simId);
        IccIoResult response =
                (IccIoResult)sendRequest(commandID,
                        new IccAPDUArgument(cla, command, channel,
                        p1, p2, p3, data));
        log("< exchangeAPDUGemini " + response);
        String s = Integer.toHexString(
                (response.sw1 << 8) + response.sw2 + 0x10000).substring(1);
        if (response.payload != null)
            s = IccUtils.bytesToHexString(response.payload) + s;
        return s;
    }

    public String transmitIccBasicChannel(int cla, int command,
            int p1, int p2, int p3, String data) {
        log("NFC test for transmitIccBasicChannel");
        return exchangeIccAPDU(cla, command, 0, p1, p2, p3, data);
    }

    public String transmitIccBasicChannelGemini(int cla, int command,
            int p1, int p2, int p3, String data, int simId) {
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("transmitIccBasicChannelGemini: simId " + simId + " error");
            return null;
        }
        return exchangeIccAPDUGemini(cla, command, 0, p1, p2, p3, data, simId);
    }

    public String transmitIccLogicalChannel(int cla, int command,
            int channel, int p1, int p2, int p3, String data) {
        log("NFC test for transmitIccLogicalChannel");
        return exchangeIccAPDU(cla, command, channel, p1, p2, p3, data);
    }

    public String transmitIccLogicalChannelGemini(int cla, int command,
            int channel, int p1, int p2, int p3, String data, int simId) {
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("transmitIccLogicalChannelGemini: simId " + simId + " error");
            return null;
        }

        return exchangeIccAPDUGemini(cla, command, channel, p1, p2, p3, data, simId);
    }
 
    public int openIccLogicalChannel(String AID) {
        log("NFC test for openIccLogicalChannel");
        //if (Binder.getCallingUid() != Process.SMARTCARD_UID)
          //  throw new SecurityException("Only Smartcard API may access UICC");
        Log.d(LOG_TAG, "> openIccLogicalChannel " + AID);
        Integer channel = (Integer)sendRequest(CMD_OPEN_CHANNEL, AID);
        Log.d(LOG_TAG, "< openIccLogicalChannel " + channel);
        return channel.intValue();
    }

    public int openIccLogicalChannelGemini(String AID, int simId) {
        /* Disable NFC SEEK Project code, need to modify Process to support -----
        if (Binder.getCallingUid() != Process.SMARTCARD_UID)
            throw new SecurityException("Only Smartcard API may access UICC");*/
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("openIccLogicalChannelGemini: simId " + simId + " error");
            return 0;
        }

        int commandID = CMD_OPEN_CHANNEL_1;
        if (simId == Phone.GEMINI_SIM_2)
            commandID = CMD_OPEN_CHANNEL_2;

        log("> openIccLogicalChannelGemini " + AID + ", " + simId);
        Integer channel = (Integer)sendRequest(commandID, AID);
        log("< openIccLogicalChannelGemini " + channel);
        return channel.intValue();
    }

    public boolean closeIccLogicalChannel(int channel) {
        log("NFC test for closeIccLogicalChannel");
        //if (Binder.getCallingUid() != Process.SMARTCARD_UID)
          //  throw new SecurityException("Only Smartcard API may access UICC");
        Log.d(LOG_TAG, "> closeIccLogicalChannel " + channel);
        Integer err = (Integer)sendRequest(CMD_CLOSE_CHANNEL,
                new Integer(channel));
        Log.d(LOG_TAG, "< closeIccLogicalChannel " + err +", " + err.intValue());
        String strrr = err.toString();
        Log.d(LOG_TAG, "< closeIccLogicalChannel2 " + strrr);
        if(err.intValue() == 0)
            return true;
        return false;
    }

    public boolean closeIccLogicalChannelGemini(int channel, int simId) {
        /* Disable NFC SEEK Project code, need to modify Process to support -----
        if (Binder.getCallingUid() != Process.SMARTCARD_UID)
            throw new SecurityException("Only Smartcard API may access UICC");*/
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("closeIccLogicalChannelGemini: simId " + simId + " error");
            return false;
        }

        int commandID = CMD_CLOSE_CHANNEL_1;
        if (simId == Phone.GEMINI_SIM_2)
            commandID = CMD_CLOSE_CHANNEL_2;

        log("> closeIccLogicalChannelGemini " + channel + ", " + simId);
        Integer err = (Integer)sendRequest(commandID,
                new Integer(channel));
        log("< closeIccLogicalChannelGemini " + err);
        if(err.intValue() == 0)
            return true;
        return false;
    }

    public int getLastError() {
        log("NFC test for getLastError");
        return lastError;
    }

    public int getLastErrorGemini(int simId) {
        if(simId == Phone.GEMINI_SIM_1)
            return lastError1;
        else if(simId == Phone.GEMINI_SIM_2)
            return lastError2;
        else {
            log("getLastErrorGemini parameter " + simId + " error");
            return 1;
        }
    }

    public String getIccATR() {
        log("NFC test for getIccATR");
        //if (Binder.getCallingUid() != Process.SMARTCARD_UID)
          //  throw new SecurityException("Only Smartcard API may access UICC");
        Log.d(LOG_TAG, "> getIccATR ");
        String response = (String)sendRequest(CMD_GET_ATR, null);
        Log.d(LOG_TAG, "< getIccATR: " + response);
        return response;
    }

    public String getIccATRGemini(int simId) {
        if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
            log("getIccATRGemini: simId " + simId + " error");
            return null;
        }

        int commandID = CMD_GET_ATR_1;
        if (simId == Phone.GEMINI_SIM_2)
            commandID = CMD_GET_ATR_2;

        log("> getIccATRGemini " + ", " + simId);
        String response = (String)sendRequest(commandID, null);
        log("< getIccATRGemini: " + response);
        return response;
    }
    // NFC SEEK end

    //MTK-START [mtk04070][111117][ALPS00093395]MTK proprietary methods
    public boolean isVoiceIdle(){
        Phone.State state;
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            state = ((GeminiPhone)mPhone).getState();  // IDLE, RINGING, or OFFHOOK	
        }
        else
        {
            state = mPhone.getState();  // IDLE, RINGING, or OFFHOOK
        }
        
        return (state == Phone.State.IDLE);    
    }
    
   public String getIccCardType() {
  	 return mPhone.getIccCard().getIccCardType();
   }
  

   public int btSimapConnectSIM(int simId,  BtSimapOperResponse btRsp) {
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       sendBtSapTh.setBtOperResponse(btRsp);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
         sendBtSapTh.start();
       }
       int ret = sendBtSapTh.btSimapConnectSIM(simId);
       Log.d(LOG_TAG, "btSimapConnectSIM ret is " + ret + " btRsp.curType " + btRsp.getCurType() 
	 	+ " suptype " + btRsp.getSupportType() + " atr " + btRsp.getAtrString());	
       return ret;	
  
   }

   public int btSimapDisconnectSIM() {
   	Log.d(LOG_TAG, "btSimapDisconnectSIM");
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
           sendBtSapTh.start();
       } 
       return sendBtSapTh.btSimapDisconnectSIM();
   }

   public int btSimapApduRequest(int type, String cmdAPDU,  BtSimapOperResponse btRsp) {
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       sendBtSapTh.setBtOperResponse(btRsp);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
           sendBtSapTh.start();
       }
       return sendBtSapTh.btSimapApduRequest(type, cmdAPDU);
   }

   public int btSimapResetSIM(int type,  BtSimapOperResponse btRsp) {
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       sendBtSapTh.setBtOperResponse(btRsp);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
           sendBtSapTh.start();
       }
       return sendBtSapTh.btSimapResetSIM(type);
   }

   public int btSimapPowerOnSIM(int type,  BtSimapOperResponse btRsp) {
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       sendBtSapTh.setBtOperResponse(btRsp);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
           sendBtSapTh.start();
       }
       return sendBtSapTh.btSimapPowerOnSIM(type);
   }

   public int btSimapPowerOffSIM() {
       final SendBtSimapProfile sendBtSapTh = SendBtSimapProfile.getInstance(mPhone);
       if(sendBtSapTh.getState() == Thread.State.NEW) {
           sendBtSapTh.start();
       }
       return sendBtSapTh.btSimapPowerOffSIM();
   }
       /**
     * Helper thread to turn async call to {@link Phone#sendBTSIMProfile} into
     * a synchronous one.
     */
    private static class SendBtSimapProfile extends Thread {
        private Phone mBtSapPhone;
        private boolean mDone = false;
        private String mStrResult = null;
        private ArrayList mResult;
	private int mRet = 1;	
        private BtSimapOperResponse mBtRsp;
        private Handler mHandler;

        private static SendBtSimapProfile sInstance;
        static final Object sInstSync = new Object();
        // For async handler to identify request type
        private static final int BTSAP_CONNECT_COMPLETE = 300;
	private static final int BTSAP_DISCONNECT_COMPLETE = 301;	
        private static final int BTSAP_POWERON_COMPLETE = 302;
        private static final int BTSAP_POWEROFF_COMPLETE = 303;
        private static final int BTSAP_RESETSIM_COMPLETE = 304;
        private static final int BTSAP_TRANSFER_APDU_COMPLETE = 305;

        public static SendBtSimapProfile getInstance(Phone phone) {
            synchronized (sInstSync) {
                if (sInstance == null) {
                    sInstance = new SendBtSimapProfile(phone);
                }
            }
            return sInstance;
        }
        private SendBtSimapProfile(Phone phone) {
            mBtSapPhone = phone;
            mBtRsp = null;
        } 


        public void setBtOperResponse(BtSimapOperResponse btRsp) {
            mBtRsp = btRsp;
        } 
	
        @Override
        public void run() {
            Looper.prepare();
            synchronized (SendBtSimapProfile.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case BTSAP_CONNECT_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	
					     Log.e(LOG_TAG, "Exception BTSAP_CONNECT, Exception:" + ar.exception);
                                    } else {
                                        mStrResult = (String)(ar.result);
                                        Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE  mStrResult " + mStrResult);	 
                                        String[] splited = mStrResult.split(",");

                                        try {	
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setSupportType(Integer.parseInt(splited[1].trim()));
                                            mBtRsp.setAtrString(splited[2]);
                                            Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE curType " + mBtRsp.getCurType() + " SupType " + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.d(LOG_TAG, "NumberFormatException" );
                                        }

                                        mRet = 0;
					     //Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE curType " + (String)(mResult.get(0)) + " SupType " + (String)(mResult.get(1)) + " ATR " + (String)(mResult.get(2)));					 
					 }
					 
					//Log.d(LOG_TAG, "BTSAP_CONNECT_COMPLETE curType " + mBtRsp.getCurType() + " SupType " + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());				
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
				case BTSAP_DISCONNECT_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_DISCONNECT_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	
                                        Log.e(LOG_TAG, "Exception BTSAP_DISCONNECT, Exception:" + ar.exception);	 
                                    } else {
                                        mRet = 0;
                                    }
                                    Log.d(LOG_TAG, "BTSAP_DISCONNECT_COMPLETE result is "+ mRet);				
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;
				case BTSAP_POWERON_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	 
					     Log.e(LOG_TAG, "Exception POWERON_COMPLETE, Exception:" + ar.exception);	 
                                    } else {
                                        mStrResult = (String)(ar.result);
                                        Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE  mStrResult " + mStrResult);	 
                                        String[] splited = mStrResult.split(",");

                                        try {	
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setAtrString(splited[1]);
                                            Log.d(LOG_TAG, "BTSAP_POWERON_COMPLETE curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.d(LOG_TAG, "NumberFormatException" );
                                        }
                                        mRet = 0;
                                    }
			
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;		
				case BTSAP_POWEROFF_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_POWEROFF_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	
                                        Log.e(LOG_TAG, "Exception BTSAP_POWEROFF, Exception:" + ar.exception);	 
                                    } else {
                                        mRet = 0;
                                    }
                                    Log.d(LOG_TAG, "BTSAP_POWEROFF_COMPLETE result is " + mRet);				
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;	
				case BTSAP_RESETSIM_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	
                                        Log.e(LOG_TAG, "Exception BTSAP_RESETSIM, Exception:" + ar.exception);	 
                                    } else {
                                        mStrResult = (String)(ar.result);
                                        Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE  mStrResult " + mStrResult);	 
                                        String[] splited = mStrResult.split(",");

                                        try {	
                                            mBtRsp.setCurType(Integer.parseInt(splited[0].trim()));
                                            mBtRsp.setAtrString(splited[1]);
                                            Log.d(LOG_TAG, "BTSAP_RESETSIM_COMPLETE curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());
                                        } catch (NumberFormatException e) {
                                            Log.d(LOG_TAG, "NumberFormatException" );
                                        }
                                        mRet = 0;
                                    }

                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;						
				case BTSAP_TRANSFER_APDU_COMPLETE:								
                                Log.d(LOG_TAG, "BTSAP_TRANSFER_APDU_COMPLETE");
                                synchronized (SendBtSimapProfile.this) {
                                    if (ar.exception != null) {
                                        CommandException ce = (CommandException) ar.exception;
                                        if (ce.getCommandError() == CommandException.Error.BT_SAP_CARD_REMOVED){
                                            mRet = 4;
                                        }else if (ce.getCommandError() == CommandException.Error.BT_SAP_NOT_ACCESSIBLE){
                                            mRet = 2;
                                        }else {
                                            mRet = 1;
                                        }	 
						 
                                        Log.e(LOG_TAG, "Exception BTSAP_TRANSFER_APDU, Exception:" + ar.exception);	 
                                    } else {
                                        mBtRsp.setApduString((String)(ar.result));
                                        Log.d(LOG_TAG, "BTSAP_TRANSFER_APDU_COMPLETE result is " + mBtRsp.getApduString());				
                                        mRet = 0;
                                    }
					
                                    mDone = true;
                                    SendBtSimapProfile.this.notifyAll();
                                }
                                break;						
                        }
                    }
                };
                SendBtSimapProfile.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized int btSimapConnectSIM(int simId) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_CONNECT_COMPLETE);
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(0, 0, null, callback, simId); 
            } else {
                mBtSapPhone.sendBTSIMProfile(0, 0, null, callback);
            }
			
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
	    	 	
            Log.d(LOG_TAG, "done");	 
            if (mRet == 0) {
		 //parse result	
                if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                    ((GeminiPhone)mBtSapPhone).setBtConnectedSimId(simId);
                    Log.d(LOG_TAG, "synchronized btSimapConnectSIM GEMINI connect Sim is " + ((GeminiPhone)mBtSapPhone).getBtConnectedSimId());  
                }
                Log.d(LOG_TAG, "btSimapConnectSIM curType " + mBtRsp.getCurType() + " SupType " + mBtRsp.getSupportType() + " ATR " + mBtRsp.getAtrString());		 
	     } else {
                ret = mRet;
	     }
		 
	     Log.d(LOG_TAG, "synchronized btSimapConnectSIM ret " + ret);   	 
	     return ret;
        }	

        synchronized int btSimapDisconnectSIM() {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM");		
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_DISCONNECT_COMPLETE);
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int simId = ((GeminiPhone)mBtSapPhone).getBtConnectedSimId();
                Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM GEMINI connect Sim is " + simId);			 
                if(simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
                    ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(1, 0, null, callback, simId);
                } else {                     
                    ret = 7; //No sim has been connected
                    return ret;
                }
            } else {
                Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM  not gemini " );	
                mBtSapPhone.sendBTSIMProfile(1, 0, null, callback);
            }
		 
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)	{
		 if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                    ((GeminiPhone)mBtSapPhone).setBtConnectedSimId(-1);
		 }
            }	
            ret = mRet;
            Log.d(LOG_TAG, "synchronized btSimapDisconnectSIM ret " + ret);   	 
            return ret;
        }

        synchronized int btSimapResetSIM(int type) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_RESETSIM_COMPLETE);
			
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int simId = ((GeminiPhone)mBtSapPhone).getBtConnectedSimId();
                Log.d(LOG_TAG, "synchronized btSimapResetSIM GEMINI connect Sim is " + simId);			 
                if(simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
                    ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(4, type, null, callback, simId);
                } else {                     
                    ret = 7; //No sim has been connected
                    return ret;
                }		 	
            } else {
                mBtSapPhone.sendBTSIMProfile(4, type, null, callback);
            }

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)	{
                Log.d(LOG_TAG, "btSimapResetSIM curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());		 
            } else {
                ret = mRet;
            }	

            Log.d(LOG_TAG, "synchronized btSimapResetSIM ret " + ret);   	 
            return ret;
        }

        synchronized int btSimapPowerOnSIM(int type)  {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_POWERON_COMPLETE);

            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int simId = ((GeminiPhone)mBtSapPhone).getBtConnectedSimId();
                Log.d(LOG_TAG, "synchronized btSimapPowerOnSIM GEMINI connect Sim is " + simId);			 
                if(simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
                    ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(2, type, null, callback, simId);
                } else {                     
                    ret = 7; //No sim has been connected
                    return ret;
                }			 	
            } else {
                mBtSapPhone.sendBTSIMProfile(2, type, null, callback);
            }

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)	{
                Log.d(LOG_TAG, "btSimapPowerOnSIM curType " + mBtRsp.getCurType() + " ATR " + mBtRsp.getAtrString());		 
            } else {
	        ret = mRet;
            }	
            Log.d(LOG_TAG, "synchronized btSimapPowerOnSIM ret " + ret);    
            return ret;
        }

        synchronized int btSimapPowerOffSIM() {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_POWEROFF_COMPLETE);

            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int simId = ((GeminiPhone)mBtSapPhone).getBtConnectedSimId();
	         Log.d(LOG_TAG, "synchronized btSimapPowerOffSIM GEMINI connect Sim is " + simId);			 
	         if(simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
	             ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(3, 0, null, callback, simId);
	         } else {                     
	             ret = 7; //No sim has been connected
	             return ret;
	         }			 	
            } else {
	         mBtSapPhone.sendBTSIMProfile(3, 0, null, callback);
            }
   
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            ret = mRet;
            Log.d(LOG_TAG, "synchronized btSimapPowerOffSIM ret " + ret);     
            return ret;
        }
	 
        synchronized int btSimapApduRequest(int type, String cmdAPDU) {
            int ret = 0;
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mDone = false;
            Message callback = Message.obtain(mHandler, BTSAP_TRANSFER_APDU_COMPLETE);

            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                int simId = ((GeminiPhone)mBtSapPhone).getBtConnectedSimId();
                Log.d(LOG_TAG, "synchronized btSimapApduRequest GEMINI connect Sim is " + simId);			 
                if(simId == Phone.GEMINI_SIM_1 || simId == Phone.GEMINI_SIM_2) {
                    ((GeminiPhone)mBtSapPhone).sendBTSIMProfileGemini(5, type, cmdAPDU, callback, simId);
                } else {                     
                    ret = 7; //No sim has been connected
                    return ret;
                }	
            } else {
                mBtSapPhone.sendBTSIMProfile(5, type, cmdAPDU, callback);
            }

            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            if (mRet == 0)	{
                Log.d(LOG_TAG, "btSimapApduRequest APDU " + mBtRsp.getApduString());		 
            } else {
                ret = mRet;
            }	

            Log.d(LOG_TAG, "synchronized btSimapApduRequest ret " + ret);  	 
            return ret;
        }
    }
	   
  public String simAuth(String strRand) {
        final SimAuth doSimAuth = new SimAuth(mPhone);
        doSimAuth.start();
        return doSimAuth.doSimAuth(strRand);
    }

    public String uSimAuth(String strRand, String strAutn) {
        final SimAuth doUSimAuth = new SimAuth(mPhone);
        doUSimAuth.start();
        return doUSimAuth.doUSimAuth(strRand, strAutn);
    }

    /**
     * Helper thread to turn async call to {@link #SimAuthentication} into
     * a synchronous one.
     */
    private static class SimAuth extends Thread {
      //  private final IccCard mSimCard;
        private Phone mSAPhone;
        private boolean mDone = false;
        private String mResult = null;

        // For replies from SimCard interface
        private Handler mHandler;

        // For async handler to identify request type
        private static final int SIM_AUTH_COMPLETE = 200;
        private static final int USIM_AUTH_COMPLETE = 201;

 	public SimAuth(Phone phone) {
            mSAPhone = phone;
        } 
        @Override
        public void run() {
            Looper.prepare();
            synchronized (SimAuth.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        switch (msg.what) {
                            case SIM_AUTH_COMPLETE:
                            case USIM_AUTH_COMPLETE:
                                Log.d(LOG_TAG, "SIM_AUTH_COMPLETE");
                                synchronized (SimAuth.this) {
					 if (ar.exception != null) {
					     mResult = null;	 
					 } else {
					     mResult = (String)(ar.result);
					 }
					Log.d(LOG_TAG, "SIM_AUTH_COMPLETE result is " + mResult);				
                                    mDone = true;
                                    SimAuth.this.notifyAll();
                                }
                                break;
                        }
                    }
                };
                SimAuth.this.notifyAll();
            }
            Looper.loop();
        }

        synchronized String doSimAuth(String strRand) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, SIM_AUTH_COMPLETE);

            mSAPhone.doSimAuthentication(strRand, callback);
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }

        synchronized String doUSimAuth(String strRand, String strAutn) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, USIM_AUTH_COMPLETE);

            mSAPhone.doUSimAuthentication(strRand, strAutn, callback);
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }

	 synchronized String doSimAuthGemini(String strRand,  int simId ) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, SIM_AUTH_COMPLETE);

            ((GeminiPhone)mSAPhone).doSimAuthenticationGemini(strRand,  callback, simId);
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }

	 synchronized String doUSimAuthGemini(String strRand, String strAutn, int simId) {

            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Message callback = Message.obtain(mHandler, USIM_AUTH_COMPLETE);

            ((GeminiPhone)mSAPhone).doUSimAuthenticationGemini(strRand, strAutn, callback, simId);
            while (!mDone) {
                try {
                    Log.d(LOG_TAG, "wait for done");
                    wait();
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            Log.d(LOG_TAG, "done");
            return mResult;
        }		
    }
    
    public boolean setRadioOff() {
        enforceModifyPermission();
        if (FeatureOption.MTK_GEMINI_SUPPORT != true)   
            mPhone.setRadioPower(false, true);
        else
            ((GeminiPhone)mPhone).setRadioMode(GeminiNetworkSubUtil.MODE_POWER_OFF);
        return true;
    }
    
    public int getPreciseCallState() {
        return DefaultPhoneNotifier.convertCallState(mCM.getState());
    }
    
    /**
     * Return ture if the ICC card is a test card
     * @hide
     */
    public boolean isTestIccCard() {
        if (FeatureOption.MTK_GEMINI_SUPPORT == false) {
            String imsi = mPhone.getSubscriberId();
            if (imsi != null) {
                return imsi.substring(0, 5).equals("00101");
            } else {
                return false;
            }
        } else {
            String imsi1 = ((GeminiPhone)mPhone).getSubscriberIdGemini(Phone.GEMINI_SIM_1);
            String imsi2 = ((GeminiPhone)mPhone).getSubscriberIdGemini(Phone.GEMINI_SIM_2);
            boolean isTestIccCard1 = false;
            boolean isTestIccCard2 = false;
            
            if (imsi1 != null) {
                isTestIccCard1 = imsi1.substring(0, 5).equals("00101");
            } 
            if (imsi2 != null) {
                isTestIccCard2 = imsi2.substring(0, 5).equals("00101");
            } 

            return isTestIccCard1 || isTestIccCard2;
        }
    }

    /**
    * Return true if the FDN of the ICC card is enabled
    */
    public boolean isFDNEnabled() {
        if (FeatureOption.MTK_GEMINI_SUPPORT != true) {   
            return mPhone.getIccCard().getIccFdnEnabled();
    	} else {
    	     return ((GeminiPhone)mPhone).getIccCard().getIccFdnEnabled();
    	}
    }
    
   /**
     *get the services state for default SIM
     * @return sim indicator state.    
     *
    */ 
    public int getSimIndicatorState(){         
        return mPhone.getSimIndicateState();

   }

   /**
     *get the network service state for default SIM
     * @return service state.    
     *
    */ 
    public Bundle getServiceState(){
        Bundle data = new Bundle();
        mPhone.getServiceState().fillInNotifierBundle(data);
        return data;     
    }

  /**
     * @return true if phone book is ready.    
    */ 
   public boolean isPhbReady(){
       return mPhone.getIccCard().isPhbReady();
   }

 /* ================Add for Gemini============================*/

/* Fion add start */

    public void dialGemini(String Number, int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
        if (DBG) log("dialGemini: " + Number);
        if (DBG) log("dialGemini simId: " + simId);
            Phone.State state;

            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return;
            }

            String url = createTelUrl(Number);
            if (url == null) {
                return;
            }

            /* get phone state */
            if (simId == Phone.GEMINI_SIM_1)
            {
                state = ((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_1);
            }
            else
            {
                state = ((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_2);        	
            }

            if (state != Phone.State.OFFHOOK && state != Phone.State.RINGING) {
                Intent  intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
                mApp.startActivity(intent);
            }
        }
        return ;
    }
    
    public void callGemini(String number, int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {        
        if (DBG) log("callGemini: " + number);
        if (DBG) log("callGemini simId: " + simId);
    	
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
    	  return ;
    }

            enforceCallPermission();

            String url = createTelUrl(number);
            if (url == null) {
                return;
            }

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Let OutgoingcallBroadcaster to handle this call intent
            //intent.setClassName(mApp, PhoneApp.getCallScreenClassName());
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
            mApp.startActivity(intent);    	
    	}
        return ;
    }

    private boolean showCallScreenInternalGemini(boolean specifyInitialDialpadState,
                                           boolean initialDialpadState, int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {
            if (isIdleGemini(simId)) {
                return false;
            }
        
            long callingId = Binder.clearCallingIdentity();
            try {
                Intent intent;
                if (specifyInitialDialpadState) {
                    intent = PhoneApp.createInCallIntent(initialDialpadState);
                } else {
                    intent = PhoneApp.createInCallIntent();
                }
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
                mApp.startActivity(intent);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        return true;
    }

    public boolean showCallScreenGemini(int simId) {
        if (DBG) log("showCallScreenGemini simId: " + simId);
        
        return true;
    }

    public boolean showCallScreenWithDialpadGemini(boolean showDialpad, int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {
        if (DBG) log("showCallScreenWithDialpadGemini simId: " + simId);   
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return false;
            }        
            showCallScreenInternalGemini(true, showDialpad, simId);
        }
        return true;
    }

    public boolean endCallGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {        
        if (DBG) log("endCallGemini simId: " + simId); 
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return false;
            } 
        
            enforceCallPermission();
        
            if (Looper.myLooper() == mMainThreadHandler.getLooper()) {
                throw new RuntimeException("This method will deadlock if called from the main thread.");
            }

            MainThreadRequest request = new MainThreadRequest(null);
            Message msg = mMainThreadHandler.obtainMessage(CMD_END_CALL_GEMINI, simId, 0, request);
            msg.sendToTarget();

            synchronized (request) {
                while (request.result == null) {
                    try {
                        request.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return (Boolean)(request.result);
        }
        else
        {
            return false;
        }

    }

    public void answerRingingCallGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {         
        if (DBG) log("answerRingingCallGemini simId: " + simId); 
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
        return ;
    }
    
            enforceModifyPermission();

            sendRequestAsync(CMD_ANSWER_RINGING_CALL_GEMINI);  /* review if need modify for not */
        }    
        return ;
    }
    
    /* seem no need this Gemini api : review it */
    public void silenceRingerGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {        
        if (DBG) log("silenceRingerGemini simId: " + simId); 
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return;
            }
            enforceModifyPermission();
            sendRequestAsync(CMD_SILENCE_RINGER);       
        }       
        return ;
    }

    public boolean isOffhookGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {         
        if (DBG) log("isOffhookGemini simId: " + simId);
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return false;
            }
        
            if (simId == Phone.GEMINI_SIM_1)
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_1) == Phone.State.OFFHOOK);
            }
            else
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_2) == Phone.State.OFFHOOK);
            }
        }
        else
        {
            return false;
        }
    }

    public boolean isRingingGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {         
        if (DBG) log("isRingingGemini simId: " + simId);
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return false;
            }

            if (simId == Phone.GEMINI_SIM_1)
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_1) == Phone.State.RINGING);
            }
            else
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_2) == Phone.State.RINGING);
            }
        }
        else
        {
            return false;
        }
    }

    public boolean isIdleGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {        
        if (DBG) log("isIdleGemini simId: " + simId);
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return false;
            }

            if (simId == Phone.GEMINI_SIM_1)
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_1) == Phone.State.IDLE);
            }
            else
            {
                return (((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_2) == Phone.State.IDLE);
            }
        }
        else
        {
           return false;
        }
    }    
    
    /* seem no need this Gemini api : review it */
    public void cancelMissedCallsNotificationGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {         
        if (DBG) log("cancelMissedCallsNotificationGemini simId: " + simId); 
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return;
            }

            enforceModifyPermission();
            mApp.notificationMgr.cancelMissedCallNotification();
        }    
        return ;
    }

    public int getCallStateGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        {          
            if (DBG) log("getCallStateGemini simId: " + simId); 

            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return 0;
            }
        
            if (simId == Phone.GEMINI_SIM_1)
            {
                return DefaultPhoneNotifier.convertCallState(((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_1));
            }    
            else
            {
                return DefaultPhoneNotifier.convertCallState(((GeminiPhone)mPhone).getStateGemini(Phone.GEMINI_SIM_2));
            }
        }    
        return 0;
    }
     
    public int getActivePhoneTypeGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)                                                
        { 
        if (DBG) log("getActivePhoneTypeGemini simId: " + simId);
        
            if (simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2)
            {
                if (DBG) log("dialGemini: wrong sim id");
                return 0;
            }

            if (simId == Phone.GEMINI_SIM_1)
            {
                return ((GeminiPhone)mPhone).getPhoneTypeGemini(Phone.GEMINI_SIM_1);
            }
            else
            {
                return ((GeminiPhone)mPhone).getPhoneTypeGemini(Phone.GEMINI_SIM_2);
            }        
        }    
        else
        {
            return 0;
        }
        
    }

/* Fion add end */
    public boolean supplyPinGemini(String pin, int simId) {
        enforceModifyPermission();
        final UnlockSim checkSimPin = new UnlockSim(((GeminiPhone)mPhone).getIccCardGemini(simId));
        checkSimPin.start();
        return checkSimPin.unlockSim(null,pin);        
    }

    public boolean supplyPukGemini(String puk, String pin, int simId) {
        enforceModifyPermission();
        final UnlockSim checkSimPin = new UnlockSim(((GeminiPhone)mPhone).getIccCardGemini(simId));
        checkSimPin.start();
        return checkSimPin.unlockSim(puk, pin);        
    }

    public boolean handlePinMmiGemini(String dialString, int simId) {
        enforceModifyPermission();
        return (Boolean) sendRequest(CMD_HANDLE_PIN_MMI, new PinMmiGemini(dialString,simId));        
    }

   public String getIccCardTypeGemini(int simId) {
  	return ((GeminiPhone)mPhone).getIccCardGemini(simId).getIccCardType();
   }

    public String simAuthGemini(String strRand, int simId) {
	Log.d(LOG_TAG, "simAuthGemini  strRand is " + strRand + " simId " + simId);	
        final SimAuth doSimAuth = new SimAuth(mPhone);
        doSimAuth.start();
	 String strRes = doSimAuth.doSimAuthGemini(strRand, simId);	
	 Log.d(LOG_TAG, "simAuthGemini Result is " + strRes);
        return strRes;
    }

    public String uSimAuthGemini(String strRand, String strAutn, int simId) {
        final SimAuth doUSimAuth = new SimAuth(mPhone);
        doUSimAuth.start();	 
        return doUSimAuth.doUSimAuthGemini(strRand, strAutn, simId);
    }

    public void updateServiceLocationGemini(int simId) {
    	Slog.w(LOG_TAG,"Warning,updateServiceLocationGemini",new Throwable("tst"));
        ((GeminiPhone)mPhone).updateServiceLocationGemini(simId);
    }

    public void enableLocationUpdatesGemini(int simId) {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        ((GeminiPhone)mPhone).enableLocationUpdatesGemini(simId);
    }

    public void disableLocationUpdatesGemini(int simId) {
        mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONTROL_LOCATION_UPDATES, null);
        ((GeminiPhone)mPhone).disableLocationUpdatesGemini(simId);
    }

    public Bundle getCellLocationGemini(int simId) {
        try {
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from ACCESS_COARSE_LOCATION since this
            // is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }
        Bundle data = new Bundle();
        ((GeminiPhone)mPhone).getCellLocationGemini(simId).fillInNotifierBundle(data);
        return data;        
    }

    @SuppressWarnings("unchecked")
    public List<NeighboringCellInfo> getNeighboringCellInfoGemini(int simId) {
        try {
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION, null);
        } catch (SecurityException e) {
            // If we have ACCESS_FINE_LOCATION permission, skip the check
            // for ACCESS_COARSE_LOCATION
            // A failure should throw the SecurityException from
            // ACCESS_COARSE_LOCATION since this is the weaker precondition
            mApp.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }

        ArrayList<NeighboringCellInfo> cells = null;

        try {
            cells = (ArrayList<NeighboringCellInfo>) sendRequest(
                    CMD_HANDLE_NEIGHBORING_CELL, new Integer(simId));
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "getNeighboringCellInfo " + e);
        }

        return (List <NeighboringCellInfo>) cells;        
    }

    public boolean isRadioOnGemini(int simId) {
        return ((GeminiPhone)mPhone).isRadioOnGemini(simId);
    }

    public int getPendingMmiCodesGemini(int simId)
    {
        return ((GeminiPhone)mPhone).getPendingMmiCodesGemini(simId).size();
    }

    public boolean isSimInsert(int simId) {
        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            return ((GeminiPhone)mPhone).isSimInsert(simId);
        }else {
            return mPhone.isSimInsert();
        }
    }

    public void setGprsConnType(int type, int simId) {
        ((GeminiPhone)mPhone).setGprsConnType(type, simId);
    }

    public void setGprsTransferType(int type) {   
        mPhone.setGprsTransferType(type, null);
    }

     /*Add by mtk80372 for Barcode number*/
    public void getMobileRevisionAndIMEI(int type, Message message){
        mPhone.getMobileRevisionAndIMEI(type, message);
    }
     
     /*Add by mtk80372 for Barcode number*/
     public String getSN(){
         //return mPhone.getSN();
         return SystemProperties.get("gsm.serial");
     }
    public void setGprsTransferTypeGemini(int type, int simId) {
        ((GeminiPhone)mPhone).setGprsTransferTypeGemini(type, null, simId);
    }

    public int getNetworkTypeGemini(int simId) {                
        int radiotech = ((GeminiPhone)mPhone).getServiceStateGemini(simId).getRadioTechnology();

        switch(radiotech) {
            case ServiceState.RADIO_TECHNOLOGY_GPRS:
                return TelephonyManager.NETWORK_TYPE_GPRS;
            case ServiceState.RADIO_TECHNOLOGY_EDGE:
                return TelephonyManager.NETWORK_TYPE_EDGE;
            case ServiceState.RADIO_TECHNOLOGY_UMTS:
                return TelephonyManager.NETWORK_TYPE_UMTS;
            case ServiceState.RADIO_TECHNOLOGY_HSDPA:
                return TelephonyManager.NETWORK_TYPE_HSDPA;
            case ServiceState.RADIO_TECHNOLOGY_HSUPA:
                return TelephonyManager.NETWORK_TYPE_HSUPA;
            case ServiceState.RADIO_TECHNOLOGY_HSPA:
                return TelephonyManager.NETWORK_TYPE_HSPA;
            case ServiceState.RADIO_TECHNOLOGY_IS95A:
            case ServiceState.RADIO_TECHNOLOGY_IS95B:
                return TelephonyManager.NETWORK_TYPE_CDMA;
            case ServiceState.RADIO_TECHNOLOGY_1xRTT:
                return TelephonyManager.NETWORK_TYPE_1xRTT;
            case ServiceState.RADIO_TECHNOLOGY_EVDO_0:
                return TelephonyManager.NETWORK_TYPE_EVDO_0;
            case ServiceState.RADIO_TECHNOLOGY_EVDO_A:
                return TelephonyManager.NETWORK_TYPE_EVDO_A;
            default:
                return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
    }    

    /**
     * @return true if a ICC card is present
     */
    public boolean hasIccCardGemini(int simId) {
        return ((GeminiPhone)mPhone).getIccCardGemini(simId).hasIccCard();
    }

    /**
     * Return ture if the ICC card is a test card
     * @hide
     */
    public boolean isTestIccCardGemini(int simId) {
        String imsi =((GeminiPhone)mPhone).getSubscriberIdGemini(simId);

        return imsi.substring(0, 5).equals("00101");
    }
    
    
    public int enableDataConnectivityGemini(int simId) {
        enforceModifyPermission();
        return ((GeminiPhone)mPhone).enableDataConnectivityGemini(simId);
    }

    public int enableApnTypeGemini(String type, int simId) {
        enforceModifyPermission();
        return ((GeminiPhone)mPhone).enableApnTypeGemini(type, simId);
    }

    public int disableApnTypeGemini(String type, int simId) {
        enforceModifyPermission();
        return ((GeminiPhone)mPhone).disableApnTypeGemini(type, simId);
    }
    
    public int cleanupApnTypeGemini(String apnType, int simId) {
        enforceModifyPermission();
        return ((GeminiPhone)mPhone).cleanupApnTypeGemini(apnType, simId);
    }

    public int disableDataConnectivityGemini(int simId) {
        enforceModifyPermission();
        return ((GeminiPhone)mPhone).disableDataConnectivityGemini(simId);
    }

    public boolean isDataConnectivityPossibleGemini(int simId) {
        return ((GeminiPhone)mPhone).isDataConnectivityPossibleGemini(simId);
    }

    public int getDataStateGemini(int simId) {
        return DefaultPhoneNotifier.convertDataState(((GeminiPhone)mPhone).getDataConnectionStateGemini(simId));
    }

    public int getDataActivityGemini(int simId) {
        return DefaultPhoneNotifier.convertDataActivityState(((GeminiPhone)mPhone).getDataActivityStateGemini(simId));
    }

    public int getVoiceMessageCountGemini(int simId) {
        return mPhone.getVoiceMessageCount();
    }

    public void setDefaultPhone(int simId) {
        log("setDefaultPhone to " + ((simId==Phone.GEMINI_SIM_1)?"SIM1":"SIM2"));
        ((GeminiPhone)mPhone).setDefaultPhone(simId);
    }
   /**
    * Return true if the FDN of the ICC card is enabled
    */
    public boolean isFDNEnabledGemini(int simId) {
        log("isFDNEnabled  " + ((simId==Phone.GEMINI_SIM_1)?"SIM1":"SIM2"));
        return ((GeminiPhone)mPhone).getIccCardGemini(simId).getIccFdnEnabled();
    }

    public boolean isVTIdle()
    {
    	if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
    	{
    		return PhoneApp.getInstance().isVTIdle() ;
    	}
    	else
    	{
    		return true;
    	}
    }

   /**
     * @param simId Indicate which sim(slot) to query
     * @return true if phone book is ready. 
     *
    */ 
   public boolean isPhbReadyGemini(int simId){
       return ((GeminiPhone)mPhone).getIccCardGemini(simId).isPhbReady();
   }
   
   public String getScAddressGemini(int simId) {
	    Log.d(LOG_TAG, "getScAddressGemini: enter");
		  if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
    		Log.d(LOG_TAG, "[sca Invalid sim id");
    		return null;
    	}

		  final ScAddrGemini addr = new ScAddrGemini(null, simId);

		  Thread sender = new Thread() {
			  public void run() {
				  try {
			          addr.scAddr = (String)sendRequest(CMD_HANDLE_GET_SCA, addr);
		          } catch(RuntimeException e) {
                      Log.e(LOG_TAG, "[sca getScAddressGemini " + e);
		          }
			  }
		  };
		  sender.start();
		  try {
			  Log.d(LOG_TAG, "[sca thread join");
			  sender.join();
		  } catch(InterruptedException e) {
		    Log.d(LOG_TAG, "[sca throw interrupted exception");
		  }

		  Log.d(LOG_TAG, "getScAddressGemini: exit with " + addr.scAddr);

		  return addr.scAddr;
   }
   
	public void setScAddressGemini(String address, int simId) {
    Log.d(LOG_TAG, "setScAddressGemini: enter");
		if(simId != Phone.GEMINI_SIM_1 && simId != Phone.GEMINI_SIM_2) {
    		Log.d(LOG_TAG, "[sca Invalid sim id");
    		return;
    	}
		
		final ScAddrGemini addr = new ScAddrGemini(address, simId);

		Thread sender = new Thread() {
			public void run() {
				try {
			        addr.scAddr = (String)sendRequest(CMD_HANDLE_SET_SCA, addr);
		        } catch(RuntimeException e) {
                    Log.e(LOG_TAG, "[sca setScAddressGemini " + e);
		        }
			}
		};
		sender.start();
		try {
			Log.d(LOG_TAG, "[sca thread join");
			sender.join();
		} catch(InterruptedException e) {
		   Log.d(LOG_TAG, "[sca throw interrupted exception");
		}
		
		Log.d(LOG_TAG, "setScAddressGemini: exit");
	}

  /**
     *get the services state for specified SIM
     * @param simId Indicate which sim(slot) to query
     * @return sim indicator state.
     *
    */ 
    public int getSimIndicatorStateGemini(int simId){       
        return  ((GeminiPhone)mPhone).getSimIndicateStateGemini(simId);
    }

   /**
     *get the network service state for specified SIM
     * @param simId Indicate which sim(slot) to query
     * @return service state.
     *
    */ 
    public Bundle getServiceStateGemini(int simId){
        Bundle data = new Bundle();
        ((GeminiPhone)mPhone).getServiceStateGemini(simId).fillInNotifierBundle(data);
        return data; 
    }

    /**
     * @return SMS default SIM. 
     */ 
    public int getSmsDefaultSim() {
        if (FeatureOption.MTK_GEMINI_ENHANCEMENT == true) {
            return ((GeminiPhone)mPhone).getSmsDefaultSim();
        } else {
            return SystemProperties.getInt(Phone.GEMINI_DEFAULT_SIM_PROP, Phone.GEMINI_SIM_1);
        }
    }
    
    public boolean isRejectAllVoiceCall(){
    	return PhoneApp.getInstance().isRejectAllVoiceCall();
    }
    
    public boolean isRejectAllVideoCall(){
    	return PhoneApp.getInstance().isRejectAllVideoCall();
    }
    
    public boolean isRejectAllSIPCall(){
    	return PhoneApp.getInstance().isRejectAllSIPCall();
    }

/* 3G switch start */
    public int get3GCapabilitySIM() {
        if (FeatureOption.MTK_GEMINI_ENHANCEMENT == true)
            return ((GeminiPhone)mPhone).get3GCapabilitySIM();
        else
            return Phone.GEMINI_SIM_1;
    }

    public boolean set3GCapabilitySIM(int simId) {
        boolean result = false;
        if (m3GSwitchLocks.isEmpty()) {
            Phone.State state = mCM.getState();
            if (state == Phone.State.IDLE) {
                if (FeatureOption.MTK_GEMINI_ENHANCEMENT == true)
                    result = ((GeminiPhone)mPhone).set3GCapabilitySIM(simId);
            } else {
                Log.w(LOG_TAG, "Phone is not idle, cannot 3G switch [" + state + "]");
            }
        }else {
            Log.w(LOG_TAG, "3G switch locked, cannot 3G switch [" + m3GSwitchLocks + "]");
        }
        return result;
    }

    public int aquire3GSwitchLock() {
        Integer lock = new Integer(m3GSwitchLockCounter++);
        m3GSwitchLocks.add(lock);
        
        Intent intent = new Intent(GeminiPhone.EVENT_3G_SWITCH_LOCK_CHANGED);
        intent.putExtra(GeminiPhone.EXTRA_3G_SWITCH_LOCKED, true);
        mApp.getApplicationContext().sendBroadcast(intent);
        
        Log.i(LOG_TAG, "aquire 3G lock: " + lock);
        return lock;
    }

    public boolean release3GSwitchLock(int lockId) {
        boolean result = false;
        int index = 0;
        Iterator<Integer> it = m3GSwitchLocks.iterator();
        while (it.hasNext()) {
            int storedLockId = it.next();
            if (storedLockId == lockId) {
                int removedLockId = m3GSwitchLocks.remove(index);
                result = (lockId == removedLockId);
                Log.i(LOG_TAG, "removed 3G lockId: " + removedLockId + "[" + lockId + "]");

                Intent intent = new Intent(GeminiPhone.EVENT_3G_SWITCH_LOCK_CHANGED);
                intent.putExtra(GeminiPhone.EXTRA_3G_SWITCH_LOCKED, !m3GSwitchLocks.isEmpty());
                mApp.getApplicationContext().sendBroadcast(intent);
                break;
            }
            ++index;
        }
        return result;
    }

    public boolean is3GSwitchLocked() {
        return !m3GSwitchLocks.isEmpty();
    }
/* 3G switch end */
    
    public String getInterfaceName(String apnType) {
         return mPhone.getInterfaceName(apnType);
    }
    public String getIpAddress(String apnType) {
        return mPhone.getIpAddress(apnType);
    }
    public String getGateway(String apnType) {
        return mPhone.getGateway(apnType);
    }
    
    public String getInterfaceNameGemini(String apnType, int slot) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            return ((GeminiPhone)mPhone).getInterfaceNameGemini(apnType, slot);
        } else {
            return getInterfaceName(apnType);
        }
   }
   public String getIpAddressGemini(String apnType, int slot) {
       if (FeatureOption.MTK_GEMINI_SUPPORT) {
           return ((GeminiPhone)mPhone).getIpAddressGemini(apnType, slot);
       } else {
           return getIpAddress(apnType);
       }
   }
   public String getGatewayGemini(String apnType, int slot) {
       if (FeatureOption.MTK_GEMINI_SUPPORT) {
           return ((GeminiPhone)mPhone).getGatewayGemini(apnType, slot);
       } else {
           return getGateway(apnType);
       }
   }

    public int[] getAdnStorageInfo(int simId) {
        final QueryAdnInfoThread TheadA = new QueryAdnInfoThread(simId,mPhone);
        TheadA.start();       
        return TheadA.GetAdnStorageInfo(); 
    }   

    private static class QueryAdnInfoThread extends Thread {
    
        private final int mSimId;
        private boolean mDone = false;
        private int[] recordSize;
    
        private Handler mHandler;
            
        Phone myPhone;
        // For async handler to identify request type
        private static final int EVENT_QUERY_PHB_ADN_INFO = 100;
    
        public QueryAdnInfoThread(int simId, Phone myP) {
            mSimId = simId;
               
            myPhone = myP;
        }
    
        @Override
        public void run() {
            Looper.prepare();
            synchronized (QueryAdnInfoThread.this) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                          
                        switch (msg.what) {
                            case EVENT_QUERY_PHB_ADN_INFO:
                                Log.d(LOG_TAG, "EVENT_QUERY_PHB_ADN_INFO");
                                synchronized (QueryAdnInfoThread.this) {
                                    mDone = true;
                                    int[] info = (int[]) (ar.result);
                                    if(info!=null){
                                        recordSize = new int[4];
                                        recordSize[0] = info[0]; // # of remaining entries
                                        recordSize[1] = info[1]; // # of total entries
                                        recordSize[2] = info[2]; // # max length of number
                                        recordSize[3] = info[3]; // # max length of alpha id
                                        Log.d(LOG_TAG,"recordSize[0]="+ recordSize[0]+",recordSize[1]="+ recordSize[1] +
                                                         "recordSize[2]="+ recordSize[2]+",recordSize[3]="+ recordSize[3]);
                                    }
                                    else {
                                        recordSize = new int[4];
                                        recordSize[0] = 0; // # of remaining entries
                                        recordSize[1] = 0; // # of total entries
                                        recordSize[2] = 0; // # max length of number
                                        recordSize[3] = 0; // # max length of alpha id                                           
                                    }
                                    QueryAdnInfoThread.this.notifyAll();
                                      
                                }
                                break;
                            }
                      }
                };
                QueryAdnInfoThread.this.notifyAll();
            }
            Looper.loop();
        }
    
        public int[] GetAdnStorageInfo() {   
            synchronized (QueryAdnInfoThread.this) { 
                while (mHandler == null) {
                    try {                
                        QueryAdnInfoThread.this.wait();
                          
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Message response = Message.obtain(mHandler, EVENT_QUERY_PHB_ADN_INFO);
                  
                // protected PhoneBase myPhone = (GeminiPhone)mPhone.getPhonebyId(mSimId);
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                	IccFileHandler filehandle = ((GeminiPhone)myPhone).getIccFileHandlerGemini(QueryAdnInfoThread.this.mSimId);
                	filehandle.getPhbRecordInfo(response);
                }
                else {
                	IccFileHandler filehandle =((PhoneProxy) myPhone).getIccFileHandler();
                	filehandle.getPhbRecordInfo(response);
                }
                while (!mDone) {
                    try {
                        Log.d(LOG_TAG, "wait for done");
                        QueryAdnInfoThread.this.wait();                    
                    } catch (InterruptedException e) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                }
                Log.d(LOG_TAG, "done");
                return recordSize;
            }
        }
    }
    //MTK-END [mtk04070][111117][ALPS00093395]MTK proprietary methods
    //MTK-START [mtk03851][111216][ALPS00093395]MTK proprietary methods
    private ArrayList<MessengerWrapper> mMessengerWrapperList = new ArrayList<MessengerWrapper>();

    private class MessengerWrapper {
        private Messenger mMessenger;

        private Handler mInternalHandler = new Handler(mMainThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    Log.d(LOG_TAG, "MessengerWrapper callback triggered: " + msg.what);
                    mMessenger.send(Message.obtain(this, msg.what));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        public MessengerWrapper(IBinder binder) {
            mMessenger = new Messenger(binder);
        }

        public Messenger getMessenger() {
            return mMessenger;
        }

        public Handler getHandler() {
            return mInternalHandler;
        }
    };

    public void registerForSimModeChange(IBinder binder, int what) {
        if (binder != null) {
            Log.d(LOG_TAG, "registerForSimModeChange: " + binder + ", " + what);
            MessengerWrapper messengerWrapper = new MessengerWrapper(binder);
            mMessengerWrapperList.add(messengerWrapper);
            ((GeminiPhone)mPhone).registerForSimModeChange(messengerWrapper.getHandler(), what, null);
        }
    }

    public void unregisterForSimModeChange(IBinder binder) {
        Iterator<MessengerWrapper> iter = mMessengerWrapperList.iterator();
        while (iter.hasNext()) {
            MessengerWrapper messengerWrapper = (MessengerWrapper)iter.next();
            if (messengerWrapper.getMessenger().getBinder() == binder) {
                ((GeminiPhone)mPhone).unregisterForSimModeChange(messengerWrapper.getHandler());
                mMessengerWrapperList.remove(messengerWrapper);
                break;
            }
        }
    }

    public void setDataRoamingEnabledGemini(boolean enable,int simId) {
        ((GeminiPhone)mPhone).setDataRoamingEnabledGemini(enable, simId);
    }

    public void setRoamingIndicatorNeddedProperty(boolean property1, boolean property2) {
        SystemProperties.set("gsm.roaming.indicator.needed", property1 ? "true" : "false");
        SystemProperties.set("gsm.roaming.indicator.needed.2", property2 ? "true" : "false");
    }

    /**
      Description : Adjust modem radio power for Lenovo SAR requirement.
	  AT command format: AT+ERFTX=<op>,<para1>,<para2>
	  Description : When <op>=1	 -->  TX power reduction
				    <para1>:  2G L1 reduction level, default is 0
				    <para2>:  3G L1 reduction level, default is 0
				    level scope : 0 ~ 64
      Arthur      : mtk04070
      Date        : 2012.01.09
      Return value: True for success, false for failure
    */
    public boolean adjustModemRadioPower(int level_2G, int level_3G) {
        boolean result = ((level_2G >= 0) && (level_2G <= 255) && (level_3G >= 0) && (level_3G <= 255));
        Log.d(LOG_TAG, "adjustModemRadioPower");
        if (true == result) {
            String cmdStr[] = { "AT+ERFTX=1,", "" };
            cmdStr[0] = cmdStr[0] + level_2G + "," + level_3G;
            mPhone.invokeOemRilRequestStrings(cmdStr, 
		   	                mMainThreadHandler.obtainMessage(CMD_ADJUST_MODEM_RADIO_POWER));
            Log.d(LOG_TAG, cmdStr[0] + " ");
        }
        
        return result;
    }

    // ALPS00296353 MVNO START
    public String getSpNameInEfSpn() {
        if(FeatureOption.MTK_GEMINI_SUPPORT == false)
            return mPhone.getSpNameInEfSpn();
        Log.d(LOG_TAG, "PhoneInterfaceManager: getSpNameInEfSpn() not Single-Card");
        return null;
    }
 
    public String getSpNameInEfSpnGemini(int simId) {
        if(FeatureOption.MTK_GEMINI_SUPPORT == true)
            return mPhone.getSpNameInEfSpn(simId);
        Log.d(LOG_TAG, "PhoneInterfaceManager: getSpNameInEfSpn() not Gemini-Card");
        return null;
    }

    public String isOperatorMvnoForImsi() {
        if(FeatureOption.MTK_GEMINI_SUPPORT == false)
            return mPhone.isOperatorMvnoForImsi();
        Log.d(LOG_TAG, "PhoneInterfaceManager: isOperatorMvnoForImsi() not Single-Card");
        return null;
    }
 
    public String isOperatorMvnoForImsiGemini(int simId) {
        if(FeatureOption.MTK_GEMINI_SUPPORT == true)
            return mPhone.isOperatorMvnoForImsi(simId);
        Log.d(LOG_TAG, "PhoneInterfaceManager: isOperatorMvnoForImsi() not Gemini-Card");
        return null;
    }

    public boolean isIccCardProviderAsMvno() {
        if (FeatureOption.MTK_GEMINI_SUPPORT == false)
            return mPhone.isIccCardProviderAsMvno();
        log("isIccCardProviderAsMvno(: not Single-Card");
        return false;
    }

    public boolean isIccCardProviderAsMvnoGemini(int simId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
            return mPhone.isIccCardProviderAsMvno(simId);
        log("isIccCardProviderAsMvnoGemini(int simId): not Gemini-Card");
        return false;
    }
    // ALPS00296353 MVNO END

//MTK-START [mtk04258][20120806][International card support]
    public int getInternationalCardType(int simId) {
        if (!FeatureOption.EVDO_DT_SUPPORT ||
            !(mPhone instanceof GeminiPhone))
            return -1;
        Log.d(LOG_TAG, "[SimSw] getInternationalCardType simId="+simId);

        return ((GeminiPhone)mPhone).getInternationalCardType(simId);
    }

    public int setPhoneType(int simId, int phoneType)  {
        if (!FeatureOption.EVDO_DT_SUPPORT ||
            !(mPhone instanceof GeminiPhone))
            return -1;
        Log.d(LOG_TAG, "[SimSw] setPhoneType simId="+simId+" phoneType="+phoneType);

        return ((GeminiPhone)mPhone).setPhoneType(simId, phoneType);
    }

    public int continueMdInit(int simId) {
        if (!FeatureOption.EVDO_DT_SUPPORT ||
            !(mPhone instanceof GeminiPhone))
            return -1;
        Log.d(LOG_TAG, "[SimSw] continueMdInit simId="+simId);

        return ((GeminiPhone)mPhone).continueMdInit(simId);
    }

    public String[] getLimitServiceNetworkOperator(int simId) {
        if (!FeatureOption.EVDO_DT_SUPPORT ||
            !(mPhone instanceof GeminiPhone))
            return null;
        Log.d(LOG_TAG, "[SimSw] getLimitServiceNetworkOperator simId="+simId);

		String[] response = (String[])sendRequest(CMD_GET_OPERATOR_LIMIT, 
							new Integer(simId));
        Log.d(LOG_TAG, "< getLimitServiceNetworkOperator: " + response[0] + ","+
			                                                  response[1] + ","+
			                                                  response[2]);
        return response;
    }
//MTK-END [mtk04258][20120806][International card support]
    //MTK-END [mtk03851][111216][ALPS00093395]MTK proprietary methods
}
