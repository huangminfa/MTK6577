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

import android.content.Context;
import android.content.ContentUris;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.database.Cursor;

import android.provider.Telephony;

import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Thread;

import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import com.mediatek.featureoption.FeatureOption;
import android.database.Cursor;

import static com.android.internal.telephony.cat.CatService.MSG_ID_CONN_MGR_TIMEOUT;

class BipManager {
    private static BipManager instance1 = null;
    private static BipManager instance2 = null;

    private CatService mHandler = null;
    private CatCmdMessage mCurrentCmd = null;

    private Context mContext = null;
    private GSMPhone mPhone = null;
    private ConnectivityManager mConnMgr = null;

    BearerDesc mBearerDesc = null;
    int mBufferSize = 0;
    OtherAddress mLocalAddress = null;
    TransportProtocol mTransportProtocol = null;
    OtherAddress mDataDestinationAddress = null;
    int mLinkMode = 0;
    boolean mAutoReconnected = false;

    String mApn = null;
    String mLogin = null;
    String mPassword = null;

    final int NETWORK_TYPE = ConnectivityManager.TYPE_MOBILE;

    private int mChannelStatus = BipUtils.CHANNEL_STATUS_UNKNOWN;
    private int mChannelId = 1;
    private Channel mChannel = null;
    private ChannelStatus mChannelStatusDataObject = null;
    private boolean isParamsValid = false;
    
    private static final int CONN_MGR_TIMEOUT = 30 * 1000;
    private boolean isConnMgrIntentTimeout = false;


    private BipManager(Context context, GSMPhone phone, CatService handler) {
        CatLog.d("[BIP]", "Construct BipManager");

        if(context == null || phone == null) {
            CatLog.d("[BIP]", "Fail to construct BipManager");
        }

        mContext = context;
        mPhone = phone;
        mConnMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mHandler = handler;

        IntentFilter connFilter = new IntentFilter();
        connFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE);
        mContext.registerReceiver(mNetworkConnReceiver, connFilter);
    }

    public static BipManager getInstance(Context context, GSMPhone phone, CatService handler, int simId) {
        if(simId == Phone.GEMINI_SIM_1 && instance1 == null) {
            CatLog.d("[BIP]", "Construct instance for sim 1");
            instance1 = new BipManager(context, phone, handler);
        } else if(simId == Phone.GEMINI_SIM_2 && instance2 == null) {
            CatLog.d("[BIP]", "Construct instance for sim 2");
            instance2 = new BipManager(context, phone, handler);
        }

        if(simId == Phone.GEMINI_SIM_1) {
            return instance1;
        } else if(simId == Phone.GEMINI_SIM_2) {
            return instance2;
        }

        return null;
    }

    private int getDataConnectionFromSetting(){
        int currentDataConnectionSimId = -1;

        if(FeatureOption.MTK_GEMINI_ENHANCEMENT == true){
            long currentDataConnectionMultiSimId =  Settings.System.getLong(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
            currentDataConnectionSimId = SIMInfo.getSlotById(mContext, currentDataConnectionMultiSimId);
        }else{
            currentDataConnectionSimId =  Settings.System.getInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT) - 1;            
        }

        CatLog.d("[BIP]", "Default Data Setting value=" + currentDataConnectionSimId);

        return currentDataConnectionSimId;
    }

    public void openChannel(CatCmdMessage cmdMsg, Message response) {
        int result = Phone.APN_TYPE_NOT_AVAILABLE;
        CatLog.d("[BIP]", "BM-openChannel: enter");
        int ret = ErrorValue.NO_ERROR;
        CatLog.d("[BIP]", "BM-openChannel: init channel status object");
        
        isConnMgrIntentTimeout = false;

        cmdMsg.mChannelStatus = new ChannelStatus(mChannelId, ChannelStatus.CHANNEL_STATUS_NO_LINK, ChannelStatus.CHANNEL_STATUS_INFO_NO_FURTHER_INFO);
        mCurrentCmd = cmdMsg;

        if(mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN
                || mChannelStatus == BipUtils.CHANNEL_STATUS_ONDEMAND) {
            CatLog.d("[BIP]", "Channel is already open");
        }

        boolean isParametersModified = false;

        mBearerDesc = cmdMsg.mBearerDesc;
        CatLog.d("[BIP]", "BM-openChannel: bearer type " + cmdMsg.mBearerDesc.bearerType);

        mBufferSize = cmdMsg.mBufferSize;
        CatLog.d("[BIP]", "BM-openChannel: buffer size " + cmdMsg.mBufferSize);

        mLocalAddress = cmdMsg.mLocalAddress;
        if(cmdMsg.mLocalAddress != null) {
            CatLog.d("[BIP]", "BM-openChannel: local address " + cmdMsg.mLocalAddress.address.toString());
        } else {
            CatLog.d("[BIP]", "BM-openChannel: local address is null");
        }

        mTransportProtocol = cmdMsg.mTransportProtocol;
        if(cmdMsg.mTransportProtocol != null) {
            CatLog.d("[BIP]", "BM-openChannel: transport protocol type/port "
                    + cmdMsg.mTransportProtocol.protocolType + "/" + cmdMsg.mTransportProtocol.portNumber);
        } else {
            CatLog.d("[BIP]", "BM-openChannel: transport protocol is null");
        }

        mDataDestinationAddress = cmdMsg.mDataDestinationAddress;
        if(cmdMsg.mDataDestinationAddress != null) {
            CatLog.d("[BIP]", "BM-openChannel: dest address " + cmdMsg.mDataDestinationAddress.address.toString());
        } else {
            CatLog.d("[BIP]", "BM-openChannel: dest address is null");
        }

        mApn = (cmdMsg.mApn == null) ? "TestGp.rs" : cmdMsg.mApn;
        if(cmdMsg.mApn != null) {
            CatLog.d("[BIP]", "BM-openChannel: apn " + cmdMsg.mApn);
        } else {
            CatLog.d("[BIP]", "BM-openChannel: apn default TestGp.rs");
            mCurrentCmd.mApn = mApn;
        }

        mLogin = cmdMsg.mLogin;
        CatLog.d("[BIP]", "BM-openChannel: login " + cmdMsg.mLogin);
        mPassword = cmdMsg.mPwd;
        CatLog.d("[BIP]", "BM-openChannel: password " + cmdMsg.mPwd);

        mLinkMode = ((cmdMsg.mCmdDet.commandQualifier & 0x01) == 1) ?
                BipUtils.LINK_ESTABLISHMENT_MODE_IMMEDIATE : BipUtils.LINK_ESTABLISHMENT_MODE_ONDEMMAND;

        CatLog.d("[BIP]", "BM-openChannel: mLinkMode " + cmdMsg.mCmdDet.commandQualifier);

        mAutoReconnected = ((cmdMsg.mCmdDet.commandQualifier & 0x02) == 0) ? false : true;

        //if(mBearerDesc.bearerType == BipUtils.BEARER_TYPE_GPRS) {
        //  CatLog.d("[BIP]", "BM-openChannel: Set QoS params");
        //  SystemProperties.set(BipUtils.KEY_QOS_PRECEDENCE, String.valueOf(mBearerDesc.precedence));
        //  SystemProperties.set(BipUtils.KEY_QOS_DELAY, String.valueOf(mBearerDesc.delay));
        //  SystemProperties.set(BipUtils.KEY_QOS_RELIABILITY, String.valueOf(mBearerDesc.reliability));
        //  SystemProperties.set(BipUtils.KEY_QOS_PEAK, String.valueOf(mBearerDesc.peak));
        //  SystemProperties.set(BipUtils.KEY_QOS_MEAN, String.valueOf(mBearerDesc.mean));
        //}

        setApnParams(mApn, mLogin, mPassword);
        SystemProperties.set("gsm.stk.bip", "1");

        //Wait for APN is ready. This is a tempoarily solution

        CatLog.d("[BIP]", "BM-openChannel: call startUsingNetworkFeature:" + mPhone.getMySimId());

        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            if(getDataConnectionFromSetting() == mPhone.getMySimId()){
                CatLog.d("[BIP]", "Start to establish data connection" + mPhone.getMySimId());
                result = mConnMgr.startUsingNetworkFeatureGemini(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL,mPhone.getMySimId());
            }
        }else{
            // result = mConnMgr.startUsingNetworkFeatureGemini(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL,mPhone.getMySimId());
            result = mConnMgr.startUsingNetworkFeature(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL);
        }

        if(result == Phone.APN_ALREADY_ACTIVE) {
            CatLog.d("[BIP]", "BM-openChannel: APN already active");
            if(requestRouteToHost() == false) {
                CatLog.d("[BIP]", "BM-openChannel: Fail - requestRouteToHost");
                ret = ErrorValue.NETWORK_CURRENTLY_UNABLE_TO_PROCESS_COMMAND;
            }
            isParamsValid = true;

            CatLog.d("[BIP]", "BM-openChannel: establish data channel");
            ret = establishLink();
            if(ret == ErrorValue.NO_ERROR || ret == ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION) {
                CatLog.d("[BIP]", "BM-openChannel: channel is activated");
                cmdMsg.mChannelStatus.isActivated = true;
            } else {
                CatLog.d("[BIP]", "BM-openChannel: channel is un-activated");
                cmdMsg.mChannelStatus.isActivated = false;
            }

            response.arg1 = ret;
            response.obj = cmdMsg;
            mCurrentCmd = cmdMsg;
            mHandler.sendMessage(response);
        } else if(result == Phone.APN_REQUEST_STARTED) {
            CatLog.d("[BIP]", "BM-openChannel: APN request started");
            isParamsValid = true;
            
            Message timerMsg = mHandler.obtainMessage(MSG_ID_CONN_MGR_TIMEOUT);
            timerMsg.obj = cmdMsg;
            mHandler.sendMessageDelayed(timerMsg, CONN_MGR_TIMEOUT);
        } else {
            CatLog.d("[BIP]", "BM-openChannel: startUsingNetworkFeature FAIL");
            ret = ErrorValue.NETWORK_CURRENTLY_UNABLE_TO_PROCESS_COMMAND;
            cmdMsg.mChannelStatus.isActivated = false;

            response.arg1 = ret;
            response.obj = cmdMsg;
            mCurrentCmd = cmdMsg;
            mHandler.sendMessage(response);
        }
        CatLog.d("[BIP]", "BM-openChannel: exit");
    }

    public void closeChannel(CatCmdMessage cmdMsg, Message response) {
        CatLog.d("[BIP]", "BM-closeChannel: enter");

        if(mChannelStatus == BipUtils.CHANNEL_STATUS_CLOSE || mChannelStatus == BipUtils.CHANNEL_STATUS_UNKNOWN) {
            CatLog.d("[BIP]", "BM-closeChannel: channel has already been closed");
            response.arg1 = ErrorValue.CHANNEL_ALREADY_CLOSED;
        }else if(mChannelId != cmdMsg.mCloseCid){
            CatLog.d("[BIP]", "BM-closeChannel: channel id is wrong");
            response.arg1 = ErrorValue.CHANNEL_ID_NOT_VALID;
        }else if(mChannel != null) {
            mChannel.closeChannel();
            mChannel = null;
            mChannelStatus = BipUtils.CHANNEL_STATUS_CLOSE;
            CatLog.d("[BIP]", "BM-closeChannel: stop data connection");
            //mConnMgr.stopUsingNetworkFeature(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL);
            
            if(FeatureOption.MTK_GEMINI_SUPPORT == true)
            {
                CatLog.d("[BIP]", "stopUsingNetworkFeature getDataConnectionFromSetting  ==" + mPhone.getMySimId());
                mConnMgr.stopUsingNetworkFeatureGemini(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL,mPhone.getMySimId());
            }
            else
            {
                mConnMgr.stopUsingNetworkFeature(NETWORK_TYPE, Phone.FEATURE_ENABLE_SUPL);
            }
        }

        isParamsValid = false;

        response.obj = cmdMsg;
        mHandler.sendMessage(response);
        CatLog.d("[BIP]", "BM-closeChannel: exit");
    }

    public void receiveData(CatCmdMessage cmdMsg, Message response) {
        int requestCount = cmdMsg.mChannelDataLength;
        ReceiveDataResult result = new ReceiveDataResult();

        if(mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN
                || mChannelStatus == BipUtils.CHANNEL_STATUS_SERVER_CLOSE) {
            if(requestCount > BipUtils.MAX_APDU_SIZE) {
                CatLog.d("[BIP]", "BM-receiveData: Modify channel data length to MAX_APDU_SIZE");
                requestCount = BipUtils.MAX_APDU_SIZE;
            }

            if(mChannel != null) {
                Thread recvThread = new Thread(new RecvDataRunnable(requestCount, result, cmdMsg, response));
                recvThread.start();
                //result = mChannel.receiveData(requestCount);
                //if(result != null) {
                //  cmdMsg.mChannelData = result.buffer;
                //  cmdMsg.mRemainingDataLength = result.remainingCount;
                //  response.arg1 = ErrorValue.NO_ERROR;
                //  response.obj = cmdMsg;
                //  mHandler.sendMessage(response);
                //}
            }
        } else {
            // response ResultCode.BIP_ERROR
            CatLog.d("[BIP]", "BM-receiveData: Channel status is invalid " + mChannelStatus);
            response.arg1 = ErrorValue.BIP_ERROR;
            response.obj = cmdMsg;
            mHandler.sendMessage(response);
        }
    }

    public void sendData(CatCmdMessage cmdMsg, Message response) 
    {
        CatLog.d("[BIP]", "sendData: Enter");
        Thread rt = new Thread(new SendDataThread(cmdMsg,response));
        rt.start();
        CatLog.d("[BIP]", "sendData: Leave");
    }


    protected class SendDataThread implements Runnable 
    {
        CatCmdMessage cmdMsg;
        Message response;

        SendDataThread(CatCmdMessage Msg,Message resp)
        {
            CatLog.d("[BIP]", "SendDataThread Init");
            cmdMsg = Msg;
            response = resp;
        }

        @Override
        public void run() 
        {
            CatLog.d("[BIP]", "SendDataThread Run Enter");
            int ret = ErrorValue.NO_ERROR;

            byte[] buffer = cmdMsg.mChannelData;
            int mode = cmdMsg.mCmdDet.commandQualifier & 0x01;

            if(mChannelId != cmdMsg.mSendDataCid)
            {
                CatLog.d("[BIP]", "SendDataThread Run mChannelId != cmdMsg.mSendDataCid");
                ret = ErrorValue.CHANNEL_ID_NOT_VALID;
            }
            else if(mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN)
            {
                CatLog.d("[BIP]", "SendDataThread Run mChannel.sendData");
                ret = mChannel.sendData(buffer, mode);
                response.arg2 = mChannel.getTxAvailBufferSize();
            }
            else
            {
                CatLog.d("[BIP]", "SendDataThread Run CHANNEL_ID_NOT_VALID");
                ret = ErrorValue.CHANNEL_ID_NOT_VALID;
            }

            
            response.arg1 = ret;
            response.obj = cmdMsg;
            CatLog.d("[BIP]", "SendDataThread Run mHandler.sendMessage(response);");
            mHandler.sendMessage(response);
        }
    }

    public void getChannelStatus(CatCmdMessage cmdMsg, Message response) {
        int ret = ErrorValue.NO_ERROR;

        if(mChannelStatus == BipUtils.CHANNEL_STATUS_CLOSE || mChannelStatus == BipUtils.CHANNEL_STATUS_UNKNOWN){
            CatLog.d("[BIP]", "getChannelStatus: close");
            response.arg1 = ret;
            cmdMsg.mChannelStatus = new ChannelStatus(0,  ChannelStatus.CHANNEL_STATUS_NO_LINK, ChannelStatus.CHANNEL_STATUS_INFO_NO_FURTHER_INFO);         
        }else if(mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN){
            CatLog.d("[BIP]", "getChannelStatus: open:" + mChannelId);
            response.arg1 = ret;
            cmdMsg.mChannelStatus = new ChannelStatus(mChannelId,  ChannelStatus.CHANNEL_STATUS_LINK, ChannelStatus.CHANNEL_STATUS_INFO_NO_FURTHER_INFO);         
        }

        response.obj = cmdMsg;
        mHandler.sendMessage(response);
    }



    private boolean requestRouteToHost() {
        CatLog.d("[BIP]", "requestRouteToHost");
        byte[] addressBytes = null;
        if(mDataDestinationAddress != null) {
            addressBytes = mDataDestinationAddress.address.getAddress();
        } else {
            CatLog.d("[BIP]", "mDataDestinationAddress is null");
            return false;
        }
        int addr = 0;
        addr =  ((addressBytes[3] & 0xFF) << 24)
        | ((addressBytes[2] & 0xFF) << 16)
        | ((addressBytes[1] & 0xFF) <<  8)
        | ( addressBytes[0] & 0xFF);

        return mConnMgr.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_SUPL, addr);
    }

    private boolean checkNetworkInfo(NetworkInfo nwInfo, NetworkInfo.State exState) {
        if(nwInfo == null) {
            return false;
        }

        int type = nwInfo.getType();
        NetworkInfo.State state = nwInfo.getState();
        CatLog.d("[BIP]", "network type is " + ((type == ConnectivityManager.TYPE_MOBILE) ? "MOBILE" : "WIFI"));
        CatLog.d("[BIP]", "network state is " + state);

        if(type == ConnectivityManager.TYPE_MOBILE && state == exState) {
            return true;
        }

        return false;
    }

    private int establishLink() {
        int ret = ErrorValue.NO_ERROR;
        if(mTransportProtocol.protocolType == BipUtils.TRANSPORT_PROTOCOL_TCP_REMOTE) {
            CatLog.d("[BIP]", "BM-establishLink: establish a TCP link");
            /*
            mChannel = new TcpChannel(mChannelId, mLinkMode, mTransportProtocol.protocolType,
                    mDataDestinationAddress.address, mTransportProtocol.portNumber, mBufferSize,
                    mHandler);*/

            mChannel = new UdpChannel(mChannelId, mLinkMode, mTransportProtocol.protocolType,
                    mDataDestinationAddress.address, mTransportProtocol.portNumber, mBufferSize,
                    mHandler);
            
            ret = mChannel.openChannel(mCurrentCmd);
            if(ret == ErrorValue.NO_ERROR) {
                ret = ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION;
                mChannelStatus = BipUtils.CHANNEL_STATUS_OPEN;
            } else {
                mChannelStatus = BipUtils.CHANNEL_STATUS_ERROR;
            }
        } else if(mTransportProtocol.protocolType == BipUtils.TRANSPORT_PROTOCOL_UDP_REMOTE) {
            // establish upd link
            CatLog.d("[BIP]", "BM-establishLink: establish a UDP link");
            mChannel = new UdpChannel(mChannelId, mLinkMode, mTransportProtocol.protocolType,
                    mDataDestinationAddress.address, mTransportProtocol.portNumber, mBufferSize,
                    mHandler);
            ret = mChannel.openChannel(mCurrentCmd);
            if(ret == ErrorValue.NO_ERROR) {
                mChannelStatus = BipUtils.CHANNEL_STATUS_OPEN;
            } else {
                mChannelStatus = BipUtils.CHANNEL_STATUS_ERROR;
            }
        } else {
            CatLog.d("[BIP]", "BM-establishLink: unsupported channel type");
            ret = ErrorValue.UNSUPPORTED_TRANSPORT_PROTOCOL_TYPE;
            mChannelStatus = BipUtils.CHANNEL_STATUS_ERROR;
        }

        CatLog.d("[BIP]", "BM-establishLink: ret:" + ret);
        return ret;
    }

    private void setApnParams(String apn, String user, String pwd) {
        CatLog.d("[BIP]", "BM-setApnParams: enter");
        if(apn == null) {
            CatLog.d("[BIP]", "BM-setApnParams: No apn parameters");
            return;
        }

        Uri uri = null;
        String numeric = null;
        String mcc = null;
        String mnc = null;
        String apnType = "supl";

        /*
        * M for telephony provider enhancement
        */
        if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
            CatLog.d("[BIP]", "BM-setApnParams: URI use telephony provider enhancement");
            if(mPhone != null) {
                if(mPhone.getMySimId() == Phone.GEMINI_SIM_1) {
                    uri = Telephony.Carriers.SIM1Carriers.CONTENT_URI;
                } else if(mPhone.getMySimId() == Phone.GEMINI_SIM_2) {
                    uri = Telephony.Carriers.SIM2Carriers.CONTENT_URI;
                } else {
                    CatLog.d("[BIP]", "BM-setApnParams: invalid sim id");
                }
            } else {
                CatLog.d("[BIP]", "BM-setApnParams: mPhone is null, can't set URI");
                return;
            }
        } else {
            CatLog.d("[BIP]", "BM-setApnParams: URI use normal single card");
            uri = Telephony.Carriers.CONTENT_URI;
        }

        if(uri == null) {
            CatLog.d("[BIP]", "BM-setApnParams: Invalid uri");
        }

        numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        if(numeric != null && numeric.length() >= 4) {
            Cursor cursor = null;
            mcc = numeric.substring(0, 3);
            mnc = numeric.substring(3);
            CatLog.d("[BIP]", "BM-setApnParams: mcc = " + mcc + ", mnc = " + mnc);
            String selection = "name = 'BIP' and numeric = '" + mcc + mnc + "'";

            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (mPhone.getMySimId() == Phone.GEMINI_SIM_1) {
                    cursor = mContext.getContentResolver().query(
                            Telephony.Carriers.SIM1Carriers.CONTENT_URI, null, selection, null, null);
                } else {
                    cursor = mContext.getContentResolver().query(
                            Telephony.Carriers.SIM2Carriers.CONTENT_URI, null, selection, null, null); 
                }
            } else {
                cursor = mContext.getContentResolver().query(
                        Telephony.Carriers.CONTENT_URI, null, selection, null, null);
            }

            if (cursor != null) {
                ContentValues values = new ContentValues();
                values.put(Telephony.Carriers.NAME, "BIP");     
                values.put(Telephony.Carriers.APN, apn);
                values.put(Telephony.Carriers.USER, user);
                values.put(Telephony.Carriers.PASSWORD, pwd);
                values.put(Telephony.Carriers.TYPE, apnType);
                values.put(Telephony.Carriers.MCC, mcc);
                values.put(Telephony.Carriers.MNC, mnc);
                values.put(Telephony.Carriers.NUMERIC, mcc + mnc);

                if(cursor.getCount() == 0){
                    //int updateResult = mContext.getContentResolver().update(
                    //    uri, values, selection, selectionArgs);
                    CatLog.d("[BIP]", "BM-setApnParams: insert one record");
                    Uri newRow = mContext.getContentResolver().insert(uri, values);
                    if(newRow != null) {
                        CatLog.d("[BIP]", "insert a new record into db");
                    } else {
                        CatLog.d("[BIP]", "Fail to insert apn params into db");
                    }
                }else{
                    CatLog.d("[BIP]", "BM-setApnParams: update one record");
                    mContext.getContentResolver().update(uri, values, selection, null);             
                }

                cursor.close();
            }
            // cursor.close();
        }

        CatLog.d("[BIP]", "BM-setApnParams: exit");
    }

    public int getChannelId() {
        CatLog.d("[BIP]", "BM-getChannelId: channel id is " + mChannelId);
        return mChannelId;
    }


    protected class ConnectivityChangeThread implements Runnable 
    {
        Intent intent;

        ConnectivityChangeThread(Intent in)
        {
            CatLog.d("[BIP]", "ConnectivityChangeThread Init");
            intent = in;
        }

        @Override
        public void run() 
        {
            CatLog.d("[BIP]", "ConnectivityChangeThread Enter");
            CatLog.d("[BIP]", "Connectivity changed");
            int ret = ErrorValue.NO_ERROR;
            Message response = null;

            NetworkInfo info = (NetworkInfo)intent.getExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            if(info == null) {
                return;
            }

            int type = info.getType();
            NetworkInfo.State state = info.getState();
            CatLog.d("[BIP]", "network type is " + type);
            CatLog.d("[BIP]", "network state is " + state);


            if(type == ConnectivityManager.TYPE_MOBILE_SUPL) {
                if(state == NetworkInfo.State.CONNECTED){
                    if(requestRouteToHost() == false) {
                        CatLog.d("[BIP]", "Fail - requestRouteToHost");
                        ret = ErrorValue.NETWORK_CURRENTLY_UNABLE_TO_PROCESS_COMMAND;
                    }
                    ret = establishLink();
                    if(ret == ErrorValue.NO_ERROR || ret == ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION) {
                        CatLog.d("[BIP]", "channel is activated");
                        mCurrentCmd.mChannelStatus.isActivated = true;
                    } else {
                        CatLog.d("[BIP]", "channel is un-activated");
                        mCurrentCmd.mChannelStatus.isActivated = false;
                    }
                    response = mHandler.obtainMessage(CatService.MSG_ID_OPEN_CHANNEL_DONE, ret, 0, mCurrentCmd);
                    mHandler.sendMessage(response);
                }else if(state == NetworkInfo.State.DISCONNECTED) {
                    CatLog.d("[BIP]", "network state - disconnected");

                    if(mChannelStatus != BipUtils.CHANNEL_STATUS_OPEN){
                        ret = ErrorValue.NETWORK_CURRENTLY_UNABLE_TO_PROCESS_COMMAND;
                        mChannelStatus = BipUtils.CHANNEL_STATUS_CLOSE;
                        mCurrentCmd.mChannelStatus.isActivated = false;

                        response = mHandler.obtainMessage(CatService.MSG_ID_OPEN_CHANNEL_DONE, ret, 0, mCurrentCmd);
                        mHandler.sendMessage(response);
                    }else{
                        CatLog.d("[BIP]", "this is a drop link");
                        mChannelStatus = BipUtils.CHANNEL_STATUS_CLOSE;
                        mCurrentCmd.mChannelStatus.isActivated = false;

                        CatResponseMessage resMsg = new CatResponseMessage(CatService.EVENT_LIST_ELEMENT_CHANNEL_STATUS);
                        byte[] additionalInfo = new byte[4];
                        additionalInfo[0] = (byte) 0xB8; // Channel status
                        additionalInfo[1] = 0x02;
                        additionalInfo[2] = (byte) (getChannelId()|ChannelStatus.CHANNEL_STATUS_NO_LINK);
                        additionalInfo[3] = ChannelStatus.CHANNEL_STATUS_INFO_LINK_DROPED;

                        resMsg.setSourceId(0x82);
                        resMsg.setDestinationId(0x81);
                        resMsg.setAdditionalInfo(additionalInfo);
                        resMsg.setOneShot(false);
                        CatLog.d(this,"onEventDownload for channel status");
                        mHandler.onEventDownload(resMsg);
                    }
                }
            }
        }
    }

    private BroadcastReceiver mNetworkConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION_IMMEDIATE)
                    && isParamsValid == true && isConnMgrIntentTimeout == false) {
                CatLog.d("[BIP]", "Connectivity changed onReceive Enter");
                mHandler.removeMessages(MSG_ID_CONN_MGR_TIMEOUT);
                
                Thread rt = new Thread(new ConnectivityChangeThread(intent));
                rt.start();
                CatLog.d("[BIP]", "Connectivity changed onReceive Leave");
            }
        }
    };
    
    public void setConnMgrTimeoutFlag(boolean flag) {
        isConnMgrIntentTimeout = flag;
    }

    private class RecvDataRunnable implements Runnable {
        int requestDataSize;
        ReceiveDataResult result;
        CatCmdMessage cmdMsg;
        Message response;
      
        public RecvDataRunnable(int size, ReceiveDataResult result, CatCmdMessage cmdMsg, Message response) {
            this.requestDataSize = size;
            this.result = result;
            this.cmdMsg = cmdMsg;
            this.response = response;
        }
        
        public void run() {
            CatLog.d("[BIP]", "BM-receiveData: start to receive data");
            int errCode = mChannel.receiveData(requestDataSize, result);
            CatLog.d("[BIP]", "BM-receiveData: result code = " + errCode);
            cmdMsg.mChannelData = result.buffer;
            cmdMsg.mRemainingDataLength = result.remainingCount;
            response.arg1 = errCode;
            response.obj = cmdMsg;
            mHandler.sendMessage(response);
            CatLog.d("[BIP]", "BM-receiveData: end to receive data");
        }
    }
}

class ReceiveDataResult {
    public byte[] buffer = null;
    public int requestCount = 0;
    public int remainingCount = 0;
}