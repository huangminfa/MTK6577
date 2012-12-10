/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.internal.telephony.cat;

import com.android.internal.telephony.IccFileHandler;
//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
import com.android.internal.telephony.cdma.RuimFileHandler;
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
import com.android.internal.telephony.IccUtils;

import android.os.Handler;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import android.os.Message;

/**
 * Class used for queuing raw ril messages, decoding them into CommanParams
 * objects and sending the result back to the CAT Service.
 */
class RilMessageDecoder extends StateMachine {

	// constants
	private static final int CMD_START = 1;
	private static final int CMD_PARAMS_READY = 2;

	// members
        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
	private static RilMessageDecoder sInstance = null;
	private boolean cPhone = false;
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

	private CommandParamsFactory mCmdParamsFactory = null;
	private RilMessage mCurrentRilMessage = null;
	private Handler mCaller = null;

	//Add by Huibin Mao Mtk80229
	//ICS Migration start
	private static RilMessageDecoder sInstanceSim1 = null;    //mtk02374 GEMINI
	private static RilMessageDecoder sInstanceSim2 = null;

	/**
	 * SIM ID for GEMINI
	 */
	public static final int GEMINI_SIM_1 = 0;
	public static final int GEMINI_SIM_2 = 1;
	//ICS Migration end

	// States
	private StateStart mStateStart = new StateStart();
	private StateCmdParamsReady mStateCmdParamsReady = new StateCmdParamsReady();

	/**
	 * Get the singleton instance, constructing if necessary.
	 *
	 * @param caller
	 * @param fh
	 * @return RilMesssageDecoder
	 */
         //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
         public static synchronized RilMessageDecoder getInstance(Handler caller, RuimFileHandler fh) {
             if (sInstance == null) {
                 sInstance = new RilMessageDecoder(caller, fh);
                 sInstance.start();
             }
             return sInstance;
        }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

	public static synchronized RilMessageDecoder getInstance(Handler caller, IccFileHandler fh, int simId) {
		RilMessageDecoder tempInstance = null;

		if ((GEMINI_SIM_2 == simId && sInstanceSim2 == null) ||(GEMINI_SIM_1 == simId && sInstanceSim1 == null)) {
			tempInstance = new RilMessageDecoder(caller, fh);
			tempInstance.start();

		    if (GEMINI_SIM_2 == simId) {
			sInstanceSim2 = tempInstance;
			return sInstanceSim2;
		    } else {
			sInstanceSim1 = tempInstance;
			return sInstanceSim1;
		    }
            } else if ((GEMINI_SIM_2 == simId && sInstanceSim2 != null)) {
                return sInstanceSim2;
            } else if ((GEMINI_SIM_1 == simId && sInstanceSim1 != null)) {
                return sInstanceSim1;
            }

            return null;
        
	}


	/**
	 * Start decoding the message parameters,
	 * when complete MSG_ID_RIL_MSG_DECODED will be returned to caller.
	 *
	 * @param rilMsg
	 */
	public void sendStartDecodingMessageParams(RilMessage rilMsg) {
		Message msg = obtainMessage(CMD_START);
		msg.obj = rilMsg;
		sendMessage(msg);
	}

	/**
	 * The command parameters have been decoded.
	 *
	 * @param resCode
	 * @param cmdParams
	 */
	public void sendMsgParamsDecoded(ResultCode resCode, CommandParams cmdParams) {
		Message msg = obtainMessage(RilMessageDecoder.CMD_PARAMS_READY);
		msg.arg1 = resCode.value();
		msg.obj = cmdParams;
		sendMessage(msg);
	}

	private void sendCmdForExecution(RilMessage rilMsg) {
		Message msg = mCaller.obtainMessage(CatService.MSG_ID_RIL_MSG_DECODED,
				new RilMessage(rilMsg));
		msg.sendToTarget();
	}

        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        private RilMessageDecoder(Handler caller, RuimFileHandler fh) {
            super("RilMessageDecoder");

            addState(mStateStart);
            addState(mStateCmdParamsReady);
            setInitialState(mStateStart);

            mCaller = caller;
            mCmdParamsFactory = CommandParamsFactory.getInstance(this, fh);
        }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

	private RilMessageDecoder(Handler caller, IccFileHandler fh) {
		super("RilMessageDecoder");

		addState(mStateStart);
		addState(mStateCmdParamsReady);
		setInitialState(mStateStart);

		mCaller = caller;

		//Add by Huibin Mao Mtk80229
		//ICS Migration start
		// mCmdParamsFactory = CommandParamsFactory.getInstance(this, fh);
		CatLog.d(this, "mCaller is " + mCaller.getClass().getName());
		mCmdParamsFactory = CommandParamsFactory.getInstance(this, fh, ((CatService)mCaller).getContext());
		//ICS Migration end
	}

	private class StateStart extends State {
		@Override
		public boolean processMessage(Message msg) {
			if (msg.what == CMD_START) {
				if (decodeMessageParams((RilMessage)msg.obj)) {
					transitionTo(mStateCmdParamsReady);
				}
			} else {
				CatLog.d(this, "StateStart unexpected expecting START=" +
						CMD_START + " got " + msg.what);
			}
			return true;
		}
	}

	private class StateCmdParamsReady extends State {
		@Override
		public boolean processMessage(Message msg) {
			if (msg.what == CMD_PARAMS_READY) {
				mCurrentRilMessage.mResCode = ResultCode.fromInt(msg.arg1);
				mCurrentRilMessage.mData = msg.obj;
				sendCmdForExecution(mCurrentRilMessage);
				transitionTo(mStateStart);
			} else {
				CatLog.d(this, "StateCmdParamsReady expecting CMD_PARAMS_READY="
						+ CMD_PARAMS_READY + " got " + msg.what);
				deferMessage(msg);
			}
			return true;
		}
	}

	private boolean decodeMessageParams(RilMessage rilMsg) {
		boolean decodingStarted;

		mCurrentRilMessage = rilMsg;
		switch(rilMsg.mId) {
		case CatService.MSG_ID_SESSION_END:
		case CatService.MSG_ID_CALL_SETUP:
			mCurrentRilMessage.mResCode = ResultCode.OK;
			sendCmdForExecution(mCurrentRilMessage);
			decodingStarted = false;
			break;
		case CatService.MSG_ID_PROACTIVE_COMMAND:
		case CatService.MSG_ID_EVENT_NOTIFY:
		case CatService.MSG_ID_REFRESH:
			byte[] rawData = null;
			try {
				rawData = IccUtils.hexStringToBytes((String) rilMsg.mData);
			} catch (Exception e) {
				// zombie messages are dropped
				CatLog.d(this, "decodeMessageParams dropping zombie messages");
				decodingStarted = false;
				break;
			}
			try {
				// Start asynch parsing of the command parameters.
				mCmdParamsFactory.setCDMAPhone(cPhone);
				mCmdParamsFactory.make(BerTlv.decode(rawData));
				decodingStarted = true;
			} catch (ResultException e) {
				// send to Service for proper RIL communication.
				//Add by Huibin Mao Mtk80229
				//ICS Migration start
				mCurrentRilMessage.mId = CatService.MSG_ID_SESSION_END;
				//ICS Migration end
				mCurrentRilMessage.mResCode = e.result();
				sendCmdForExecution(mCurrentRilMessage);
				decodingStarted = false;
			}
			break;
		default:
			decodingStarted = false;
			break;
		}
		return decodingStarted;
	}
	
    public void setCDMAPhoneFlag(boolean flag) {
        CatLog.d(this, " setCDMAPhoneFlag.");
        cPhone = flag;
    }
}
