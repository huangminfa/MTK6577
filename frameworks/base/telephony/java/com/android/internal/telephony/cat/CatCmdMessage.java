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

import android.os.Parcel;
import android.os.Parcelable;
import com.mediatek.featureoption.FeatureOption;

/**
 * Class used to pass CAT messages from telephony to application. Application
 * should call getXXX() to get commands's specific values.
 *
 */
public class CatCmdMessage implements Parcelable {
	// members
	CommandDetails mCmdDet;
	private TextMessage mTextMsg;
	private Menu mMenu;
	private Input mInput;
	private BrowserSettings mBrowserSettings = null;
	private ToneSettings mToneSettings = null;
	private CallSettings mCallSettings = null;
//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        private byte[] mSmsPdu = null;
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

	//Add by Huibin Mao MTK80229
	//ICS Migration start
	// parameters for BIP
	public BearerDesc mBearerDesc = null;
	public int mBufferSize = 0;
	public OtherAddress mLocalAddress = null;
	public TransportProtocol mTransportProtocol = null;
	public OtherAddress mDataDestinationAddress = null;

	public String mApn = null;
	public String mLogin = null;
	public String mPwd = null;

	public int mChannelDataLength = 0;
	public int mRemainingDataLength = 0;
	public byte[] mChannelData = null;

	public ChannelStatus mChannelStatus = null;

	public int mCloseCid = 0;
	public int mSendDataCid = 0;
	//ICS Migration end
	/*
	 * Container for Launch Browser command settings.
	 */
	public class BrowserSettings {
		public String url;
		public LaunchBrowserMode mode;
	}

	/*
	 * Container for Call Setup command settings.
	 */
	public class CallSettings {
		public TextMessage confirmMsg;
		public TextMessage callMsg;

                //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                public TextMessage setupMsg;
                //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
	}

	CatCmdMessage(CommandParams cmdParams) {
		mCmdDet = cmdParams.cmdDet;
		switch(getCmdType()) {
		case SET_UP_MENU:
		case SELECT_ITEM:
			mMenu = ((SelectItemParams) cmdParams).menu;
			break;
		case DISPLAY_TEXT:
		case SET_UP_IDLE_MODE_TEXT:
		case SEND_DTMF:
          //if (! FeatureOption.EVDO_DT_VIA_SUPPORT) {
		//case SEND_SMS:
          //}
		case SEND_SS:
		case SEND_USSD:
		//Add by Huibin Mao MTK80229
		//ICS Migration start
		case REFRESH:
		//ICS Migration end
			mTextMsg = ((DisplayTextParams) cmdParams).textMsg;
			break;
            //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
            //if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                case SEND_SMS:
                    if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                    mTextMsg = ((SendSmsParams) cmdParams).textMsg;
                    mSmsPdu = ((SendSmsParams) cmdParams).smsPdu;
               }
                    break;
            //}
            //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
		case GET_INPUT:
		case GET_INKEY:
			mInput = ((GetInputParams) cmdParams).input;
			break;
		case LAUNCH_BROWSER:
			mTextMsg = ((LaunchBrowserParams) cmdParams).confirmMsg;
			mBrowserSettings = new BrowserSettings();
			mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).url;
			mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mode;
			break;
		case PLAY_TONE:
			PlayToneParams params = (PlayToneParams) cmdParams;
			mToneSettings = params.settings;
			mTextMsg = params.textMsg;
			break;
		case SET_UP_CALL:
			mCallSettings = new CallSettings();
			mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).confirmMsg;
			mCallSettings.callMsg = ((CallSetupParams) cmdParams).callMsg;
                        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                        if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                            mCallSettings.setupMsg = ((CallSetupParams) cmdParams).setupMsg;
			}
                        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
			break;
		//Add by Huibin Mao MTK80229
		//ICS Migration start
		case OPEN_CHANNEL:
			mBearerDesc             = ((OpenChannelParams)cmdParams).bearerDesc;
			mBufferSize             = ((OpenChannelParams)cmdParams).bufferSize;
			mLocalAddress           = ((OpenChannelParams)cmdParams).localAddress;
			mTransportProtocol      = ((OpenChannelParams)cmdParams).transportProtocol;
			mDataDestinationAddress = ((OpenChannelParams)cmdParams).dataDestinationAddress;
			mTextMsg                = ((OpenChannelParams)cmdParams).textMsg;

			if(mBearerDesc != null) {
				if(mBearerDesc.bearerType == BipUtils.BEARER_TYPE_GPRS) {
					mApn   = ((OpenChannelParams)cmdParams).gprsParams.accessPointName;
					mLogin = ((OpenChannelParams)cmdParams).gprsParams.userLogin;
					mPwd   = ((OpenChannelParams)cmdParams).gprsParams.userPwd;
				}
			} else {
				CatLog.d("[BIP]", "Invalid BearerDesc object");
			}
			break;
		case CLOSE_CHANNEL:
			mTextMsg = ((CloseChannelParams)cmdParams).textMsg;
			mCloseCid = ((CloseChannelParams)cmdParams).mCloseCid;
			break;
		case RECEIVE_DATA:
			mTextMsg = ((ReceiveDataParams)cmdParams).textMsg;
			mChannelDataLength = ((ReceiveDataParams)cmdParams).channelDataLength;
			break;
		case SEND_DATA:
			mTextMsg = ((SendDataParams)cmdParams).textMsg;
			mChannelData = ((SendDataParams)cmdParams).channelData;
			mSendDataCid = ((SendDataParams)cmdParams).mSendDataCid;
			break;
		case GET_CHANNEL_STATUS:
			mTextMsg = ((GetChannelStatusParams)cmdParams).textMsg;
			break;
		//ICS Migration end
		}
	}

	public CatCmdMessage(Parcel in) {
		mCmdDet = in.readParcelable(null);
		mTextMsg = in.readParcelable(null);
		mMenu = in.readParcelable(null);
		mInput = in.readParcelable(null);
		switch (getCmdType()) {
		case LAUNCH_BROWSER:
			mBrowserSettings = new BrowserSettings();
			mBrowserSettings.url = in.readString();
			mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
			break;
		case PLAY_TONE:
			mToneSettings = in.readParcelable(null);
			break;
		case SET_UP_CALL:
			mCallSettings = new CallSettings();
			mCallSettings.confirmMsg = in.readParcelable(null);
			mCallSettings.callMsg = in.readParcelable(null);
                        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                        if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                            mCallSettings.setupMsg = in.readParcelable(null);
                        }
                        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
			break;
		//Add by Huibin Mao MTK80229
		//ICS Migration start
		case OPEN_CHANNEL:
			mBearerDesc = in.readParcelable(null);
			break;
		//ICS Migration end
		}
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mCmdDet, 0);
		dest.writeParcelable(mTextMsg, 0);
		dest.writeParcelable(mMenu, 0);
		dest.writeParcelable(mInput, 0);
		switch(getCmdType()) {
		case LAUNCH_BROWSER:
			dest.writeString(mBrowserSettings.url);
			dest.writeInt(mBrowserSettings.mode.ordinal());
			break;
		case PLAY_TONE:
			dest.writeParcelable(mToneSettings, 0);
			break;
		case SET_UP_CALL:
			dest.writeParcelable(mCallSettings.confirmMsg, 0);
			dest.writeParcelable(mCallSettings.callMsg, 0);
                        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                        if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                            dest.writeParcelable(mCallSettings.setupMsg, 0);
                        }
                        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
			break;
		//Add by Huibin Mao MTK80229
		//ICS Migration start
		case OPEN_CHANNEL:
			dest.writeParcelable(mBearerDesc, 0);
			break;
		//ICS Migration end
		}
	}

	public static final Parcelable.Creator<CatCmdMessage> CREATOR = new Parcelable.Creator<CatCmdMessage>() {
		public CatCmdMessage createFromParcel(Parcel in) {
			return new CatCmdMessage(in);
		}

		public CatCmdMessage[] newArray(int size) {
			return new CatCmdMessage[size];
		}
	};

	public int describeContents() {
		return 0;
	}
	//Add by Huibin Mao MTK80229
	//ICS Migration start
	/* external API to be used by application */
	public int getCmdQualifier() {
		return mCmdDet.commandQualifier;
	}
	//ICS Migration end

	public AppInterface.CommandType getCmdType() {
		return AppInterface.CommandType.fromInt(mCmdDet.typeOfCommand);
	}

	public Menu getMenu() {
		return mMenu;
	}

	public Input geInput() {
		return mInput;
	}

	public TextMessage geTextMessage() {
		return mTextMsg;
	}

        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        public void setTextMessage(String text) {
            mTextMsg.text = text;
        }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

	public BrowserSettings getBrowserSettings() {
		return mBrowserSettings;
	}

	public ToneSettings getToneSettings() {
		return mToneSettings;
	}

	public CallSettings getCallSettings() {
		return mCallSettings;
	}

        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        public byte[] getSmsPdu() {
            return mSmsPdu;
        }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
}
