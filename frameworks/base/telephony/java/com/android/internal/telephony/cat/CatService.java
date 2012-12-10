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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;

//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
import com.android.internal.telephony.cat.LocalInfo;
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccFileHandler;
import com.android.internal.telephony.IccRecords;

//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
import com.android.internal.telephony.cdma.RuimCard;
import com.android.internal.telephony.cdma.RuimFileHandler;
import com.android.internal.telephony.cdma.RuimRecords;
import com.android.internal.telephony.cdma.SmsMessage;
import android.telephony.TelephonyManager;
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
//Add by Huibin Mao MTK80229
//ICS Migration start
import java.util.Calendar;
import java.util.Date;
import java.net.UnknownHostException;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.Phone;
import android.util.Config;
import android.view.IWindowManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
//ICS Migration end

import android.telephony.ServiceState;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import com.mediatek.featureoption.FeatureOption;


class RilMessage {
    int mId;
    Object mData;
    ResultCode mResCode;

    RilMessage(int msgId, String rawData) {
        mId = msgId;
        mData = rawData;
    }

    RilMessage(RilMessage other) {
        this.mId = other.mId;
        this.mData = other.mData;
        this.mResCode = other.mResCode;
    }
}

/**
 * Class that implements SIM Toolkit Telephony Service. Interacts with the RIL
 * and application.
 *
 * {@hide}
 */
public class CatService extends Handler implements AppInterface {

    // Class members
    private static IccRecords mIccRecords;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    private static RuimRecords mRuimRecords;
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    // Service members.
    // Protects singleton instance lazy initialization.
    private static final Object sInstanceLock = new Object();
//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    private static CatService sInstance;
//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    private CommandsInterface mCmdIf;
    private Context mContext;
    private CatCmdMessage mCurrentCmd = null;
    private CatCmdMessage mMenuCmd = null;

    private RilMessageDecoder mMsgDecoder = null;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    private LocalInfo mLocalInfo = new LocalInfo();
    private RuimFileHandler rfh = null;
    private IccFileHandler ifh = null;
    
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    // Service constants.
    static final int MSG_ID_SESSION_END              = 1;
    static final int MSG_ID_PROACTIVE_COMMAND        = 2;
    static final int MSG_ID_EVENT_NOTIFY             = 3;
    static final int MSG_ID_CALL_SETUP               = 4;
    static final int MSG_ID_REFRESH                  = 5;
    static final int MSG_ID_RESPONSE                 = 6;
    static final int MSG_ID_SIM_READY                = 7;
    static final int MSG_ID_EVENT_DOWNLOAD           = 8;
    static final int MSG_ID_RIL_MSG_DECODED          = 10;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    static final int MSG_ID_RIL_LOCAL_INFO           = 12;
    static final int MSG_ID_RIL_REFRESH_RESULT       = 13;
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    // Events to signal SIM presence or absent in the device.
//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
//if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
    private static final int MSG_ID_SIM_LOADED       = 21;
//} else {
    private static final int MSG_ID_ICC_RECORDS_LOADED       = 20;
//}
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    private static final int DEV_ID_KEYPAD      = 0x01;
    private static final int DEV_ID_DISPLAY     = 0x02;
    private static final int DEV_ID_EARPIECE    = 0x03;
    private static final int DEV_ID_UICC        = 0x81;
    private static final int DEV_ID_TERMINAL    = 0x82;
    private static final int DEV_ID_NETWORK     = 0x83;
    static final String STK_DEFAULT = "Defualt Message";

    //Add by Huibin Mao MTK80229
    //ICS Migration start
    private static CatService sInstanceSim1;  //mtk02374 GEMINI
    private static CatService sInstanceSim2;    
    private static String sInst1 = "sInstanceSim1";
    private static String sInst2 = "sInstanceSim2";
    private static DBHelper db = null;
    
    private GSMPhone mPhone;
    private byte[] mEventList;

    static final int MSG_ID_OPEN_CHANNEL_DONE        = 30;
    static final int MSG_ID_SEND_DATA_DONE           = 31;
    static final int MSG_ID_RECEIVE_DATA_DONE        = 32;
    static final int MSG_ID_CLOSE_CHANNEL_DONE       = 33;
    static final int MSG_ID_GET_CHANNEL_STATUS_DONE  = 34;
    static final int MSG_ID_CONN_MGR_TIMEOUT         = 35;

    // Event List Elements
    static final int EVENT_LIST_ELEMENT_MT_CALL               = 0x00;
    static final int EVENT_LIST_ELEMENT_CALL_CONNECTED        = 0x01;
    static final int EVENT_LIST_ELEMENT_CALL_DISCONNECTED     = 0x02;
    static final int EVENT_LIST_ELEMENT_LOCATION_STATUS       = 0x03;
    static final int EVENT_LIST_ELEMENT_USER_ACTIVITY         = 0x04;
    static final int EVENT_LIST_ELEMENT_IDLE_SCREEN_AVAILABLE = 0x05;
    static final int EVENT_LIST_ELEMENT_CARD_READER_STATUS    = 0x06;
    static final int EVENT_LIST_ELEMENT_LANGUAGE_SELECTION    = 0x07;
    static final int EVENT_LIST_ELEMENT_BROWSER_TERMINATION   = 0x08;
    static final int EVENT_LIST_ELEMENT_DATA_AVAILABLE        = 0x09;
    static final int EVENT_LIST_ELEMENT_CHANNEL_STATUS        = 0x0A;

    static final int ADDITIONAL_INFO_FOR_BIP_NO_SPECIFIC_CAUSE = 0x00;
    static final int ADDITIONAL_INFO_FOR_BIP_NO_CHANNEL_AVAILABLE = 0x01;
    static final int ADDITIONAL_INFO_FOR_BIP_CHANNEL_CLOSED = 0x02;
    static final int ADDITIONAL_INFO_FOR_BIP_CHANNEL_ID_NOT_AVAILABLE = 0x03;
    static final int ADDITIONAL_INFO_FOR_BIP_REQUESTED_BUFFER_SIZE_NOT_AVAILABLE = 0x04;
    static final int ADDITIONAL_INFO_FOR_BIP_SECURITY_ERROR = 0x05;
    static final int ADDITIONAL_INFO_FOR_BIP_REQUESTED_INTERFACE_TRANSPORT_LEVEL_NOT_AVAILABLE = 0x06;

    final static String IDLE_SCREEN_INTENT_NAME = "android.intent.action.IDLE_SCREEN_NEEDED";
    final static String IDLE_SCREEN_ENABLE_KEY = "_enable";
    final static String USER_ACTIVITY_INTENT_NAME = "android.intent.action.stk.USER_ACTIVITY.enable";
    final static String USER_ACTIVITY_ENABLE_KEY = "state";
    static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    static final String ACTION_PREBOOT_IPO = "android.intent.action.ACTION_PREBOOT_IPO";

    /**
     * SIM ID for GEMINI
     */
    public static final int GEMINI_SIM_1 = 0;
    public static final int GEMINI_SIM_2 = 1;
    private int simId = 0;
    BipManager mBipMgr = null;
    //ICS Migration end
    //[20120420,mtk80601,ALPS264008]
    private int ResultCodeFlag = -1;
    private boolean CDMAPhone_flag = false; 


    /* Intentionally private for singleton */
    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    private CatService(CommandsInterface ci, RuimRecords ur, Context context,
            RuimFileHandler fh, RuimCard uc) {
        if (ci == null || ur == null || context == null || fh == null
                || uc == null) {
            throw new NullPointerException(
                    "Service: Input parameters must not be null");
        }
        mCmdIf = ci;
        mContext = context;
        rfh = fh;

        // Get the RilMessagesDecoder for decoding the messages.
        mMsgDecoder = RilMessageDecoder.getInstance(this, fh);

        // Register ril events handling.
        mCmdIf.setOnUtkSessionEnd(this, MSG_ID_SESSION_END, null);
        mCmdIf.setOnUtkProactiveCmd(this, MSG_ID_PROACTIVE_COMMAND, null);
        mCmdIf.setOnUtkEvent(this, MSG_ID_EVENT_NOTIFY, null);
        //mCmdIf.setOnSimRefresh(this, MSG_ID_REFRESH, null);

        mRuimRecords = ur;

        // Register for SIM ready event.
    if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
        mRuimRecords.registerForRecordsLoaded(this, MSG_ID_SIM_LOADED, null);
        CDMAPhone_flag = true;
    }
        mCmdIf.reportUtkServiceIsRunning(null);
        CatLog.d(this, "UTK CatService: is running");
    }
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    private CatService(GSMPhone phone,CommandsInterface ci, IccRecords ir, Context context,
            IccFileHandler fh, IccCard ic, int simId) {
        if (ci == null || ir == null || context == null || fh == null
                || ic == null) {
            throw new NullPointerException(
            "Service: Input parameters must not be null");
        }
        mCmdIf = ci;
        mContext = context;
        ifh = fh;

        //Add by Huibin Mao MTK80229
        //ICS Migration start
        mPhone = phone;
        simId = Phone.GEMINI_SIM_1;
        if (mPhone != null) {
        simId = phone.getMySimId();
        }
        CatLog.d("[BIP]", "simId " + simId);
        //ICS Migration end

        // Get the RilMessagesDecoder for decoding the messages.
        mMsgDecoder = RilMessageDecoder.getInstance(this, fh, simId);

        // Register ril events handling.
        mCmdIf.setOnCatSessionEnd(this, MSG_ID_SESSION_END, null);
        mCmdIf.setOnCatProactiveCmd(this, MSG_ID_PROACTIVE_COMMAND, null);
        mCmdIf.setOnCatEvent(this, MSG_ID_EVENT_NOTIFY, null);
        mCmdIf.setOnCatCallSetUp(this, MSG_ID_CALL_SETUP, null);
        //mCmdIf.setOnSimRefresh(this, MSG_ID_REFRESH, null);

        mIccRecords = ir;

        // Register for SIM ready event.
        mCmdIf.registerForSIMReady(this, MSG_ID_SIM_READY, null);
        mCmdIf.registerForRUIMReady(this, MSG_ID_SIM_READY, null);
        mCmdIf.registerForNVReady(this, MSG_ID_SIM_READY, null);
        mIccRecords.registerForRecordsLoaded(this, MSG_ID_ICC_RECORDS_LOADED, null);

        //Add by Huibin Mao MTK80229
        //ICS Migration start
        CatLog.d(this, "Get BipManager");
        mBipMgr = BipManager.getInstance(context, phone, this, simId);
        IntentFilter intentFilter = new IntentFilter(ACTION_SHUTDOWN_IPO);
        mContext.registerReceiver(CatServiceReceiver, intentFilter);
        
        db = new DBHelper(context);
        CatLog.d(this, "STK CatService: is running");
        //ICS Migration end
    }

    public void dispose() {
        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
     if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
        mRuimRecords.unregisterForRecordsLoaded(this);
        mCmdIf.unSetOnUtkSessionEnd(this);
        mCmdIf.unSetOnUtkProactiveCmd(this);
        mCmdIf.unSetOnUtkEvent(this);
     } else {
        mIccRecords.unregisterForRecordsLoaded(this);
        mCmdIf.unSetOnCatSessionEnd(this);
        mCmdIf.unSetOnCatProactiveCmd(this);
        mCmdIf.unSetOnCatEvent(this);
        mCmdIf.unSetOnCatCallSetUp(this);
     }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
        this.removeCallbacksAndMessages(null);
    }

    protected void finalize() {
        CatLog.d(this, "Service finalized");
    }

    private void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg == null) {
            return;
        }

        // dispatch messages
        CommandParams cmdParams = null;
        
        CatLog.d(this, "handleRilMsg " + rilMsg.mId);
        
        switch (rilMsg.mId) {
        case MSG_ID_EVENT_NOTIFY:
            if (rilMsg.mResCode == ResultCode.OK) {
                cmdParams = (CommandParams) rilMsg.mData;
                if (cmdParams != null) {
                    handleProactiveCommand(cmdParams);
                }
            }
            else {
                //Add by Huibin Mao MTK80229
                //ICS Migration start
                CatLog.d(this, "event notify error code: " + rilMsg.mResCode);
                cmdParams = (CommandParams)rilMsg.mData;
                if(cmdParams != null && cmdParams.cmdDet.typeOfCommand == 0x40) {
                    CatLog.d("[BIP]", "Open Channel with ResultCode");
                    handleProactiveCommand(cmdParams);
                }
                //ICS Migration end
            }
            break;
        case MSG_ID_PROACTIVE_COMMAND:
                try {
                    cmdParams = (CommandParams) rilMsg.mData;
                    CatLog.d(this, "handleRilMsg cmdParams!=null =" + (cmdParams!=null) +" rilMsg.mResCode = " + rilMsg.mResCode);
                } catch (ClassCastException e) {
                    // for error handling : cast exception
                    CatLog.d(this, "Fail to parse proactive command");
                    sendTerminalResponse(mCurrentCmd.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD,
                                         false, 0x00, null);
                    break;
                }
            if (cmdParams != null) {
                if (rilMsg.mResCode == ResultCode.OK) {                 
                    ResultCodeFlag = 0;
                    handleProactiveCommand(cmdParams);
                } else if (rilMsg.mResCode == ResultCode.PRFRMD_ICON_NOT_DISPLAYED){                
                    ResultCodeFlag = 4;
                    handleProactiveCommand(cmdParams);
                } else {
                    // for proactive commands that couldn't be decoded
                    // successfully respond with the code generated by the
                    // message decoder.
                    CatLog.d("CAT", "SS-handleMessage: invalid proactive command: "
                            + cmdParams.cmdDet.typeOfCommand);
                    sendTerminalResponse(cmdParams.cmdDet, rilMsg.mResCode,
                            false, 0, null);
                }
            }
            break;
        case MSG_ID_REFRESH:
            cmdParams = (CommandParams) rilMsg.mData;
            if (cmdParams != null) {
                handleProactiveCommand(cmdParams);
            }
            break;
        case MSG_ID_SESSION_END:
            handleSessionEnd();
            break;
        case MSG_ID_CALL_SETUP:
            // prior event notify command supplied all the information
            // needed for set up call processing.
            break;
        }
    }

    /**
     * Handles RIL_UNSOL_STK_EVENT_NOTIFY or RIL_UNSOL_STK_PROACTIVE_COMMAND command
     * from RIL.
     * Sends valid proactive command data to the application using intents.
     * RIL_REQUEST_STK_SEND_TERMINAL_RESPONSE will be send back if the command is
     * from RIL_UNSOL_STK_PROACTIVE_COMMAND.
     */
    private void handleProactiveCommand(CommandParams cmdParams) {
        CatLog.d(this, cmdParams.getCommandType().name());

            CharSequence message;
        CatCmdMessage cmdMsg = new CatCmdMessage(cmdParams);

        //Add by Huibin Mao MTK80229
        //ICS Migration start
        Message response = null;
        //ICS Migration end
        
        // add for [ALPS00245360] should not show DISPLAY_TEXT dialog when alarm booting
        boolean isAlarmState = false;
        boolean isFlightMode = false;
        int flightMode = 0;

        switch (cmdParams.getCommandType()) {
        case SET_UP_MENU:
            if (removeMenu(cmdMsg.getMenu())) {
                mMenuCmd = null;
            } else {
                mMenuCmd = cmdMsg;
            }
            if(ResultCodeFlag == 0){
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false, 0, null);
            }
            else if(ResultCodeFlag == 4){
                sendTerminalResponse(cmdParams.cmdDet,ResultCode.PRFRMD_ICON_NOT_DISPLAYED,false,0,null);
            }
            else{
            }
            break;
        case DISPLAY_TEXT:
            // when application is not required to respond, send an immediate response.
            /*
                    if (!cmdMsg.geTextMessage().responseNeeded) {
                        sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false, 0, null);

                    }*/
            // add for [ALPS00245360] should not show DISPLAY_TEXT dialog when alarm booting
            isAlarmState = isAlarmBoot();
            try {
                flightMode = Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
            } catch(SettingNotFoundException e) {
                CatLog.d(this,"fail to get property from Settings");
                flightMode = 0;
            }
            isFlightMode = (flightMode != 0);
            CatLog.d(this, "isAlarmState = " + isAlarmState + ", isFlightMode = " + isFlightMode + ", flightMode = " + flightMode);

            if(isAlarmState && isFlightMode) {
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false, 0, null);
                return;
            }
            
            // add for SetupWizard
            if(checkSetupWizardInstalled() == true) {
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                return;
            }
            
            break;
        case REFRESH:
            // ME side only handles refresh commands which meant to remove IDLE
            // MODE TEXT.
        //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
            CatLog.d(this,"CatService handleProactiveCommand Do refresh");
            int type = 1;
            if (cmdParams.cmdDet.commandQualifier == 0 || cmdParams.cmdDet.commandQualifier == 1
                ||cmdParams.cmdDet.commandQualifier == 2 || cmdParams.cmdDet.commandQualifier == 3) {
                type = 1;
            } else if (cmdParams.cmdDet.commandQualifier == 4) {
                type = 2;
            }
            mCmdIf.requestUtkRefresh(type, obtainMessage(MSG_ID_RIL_REFRESH_RESULT));
            mRuimRecords.handleRuimRefresh(cmdParams.cmdDet.commandQualifier);
            cmdParams.cmdDet.typeOfCommand = CommandType.SET_UP_IDLE_MODE_TEXT.value();
            sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false, 0, null);
        } else {
            cmdParams.cmdDet.typeOfCommand = CommandType.SET_UP_IDLE_MODE_TEXT.value();
            CatLog.d(this, "remove event list because of SIM Refresh");
            mEventList = null;
        }
        //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
            break;
        case SET_UP_IDLE_MODE_TEXT:
            // sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
            //        0, null);

            if(((DisplayTextParams)cmdParams).textMsg.icon != null
                    && ((DisplayTextParams)cmdParams).textMsg.iconSelfExplanatory == false
                    && ((DisplayTextParams)cmdParams).textMsg.text == null){
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false,
                        0, null);        
                return;
            } else {
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                        0, null);
            }
            break;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        case PROVIDE_LOCAL_INFORMATION:
        if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
          if (cmdParams.cmdDet.commandQualifier == 0 ||
                cmdParams.cmdDet.commandQualifier == 6){
                CatLog.d(this, "Local information get AT data");
                mCmdIf.getUtkLocalInfo(obtainMessage(MSG_ID_RIL_LOCAL_INFO));
                mCurrentCmd = cmdMsg;
            }
            else{
                CatLog.d(this, "handleCmdResponse Local info");
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                    0, new LocalInformationResponseData(cmdParams.cmdDet.commandQualifier, mLocalInfo));
                mCurrentCmd = null;
            }
            return;
      } else {
            ResponseData resp = null;

            if (cmdParams.cmdDet.commandQualifier == 0x03) {

                Calendar cal = Calendar.getInstance();
                int temp = 0;
                int hibyte = 0;
                int lobyte = 0;
                byte[] datetime= new byte[7];

                temp = cal.get(Calendar.YEAR) - 2000;
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;
                datetime[0] = (byte)(lobyte | hibyte);

                temp = cal.get(Calendar.MONTH) + 1;
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;              
                datetime[1] = (byte)(lobyte | hibyte);

                temp = cal.get(Calendar.DATE);
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;              
                datetime[2] = (byte)(lobyte | hibyte);

                temp = cal.get(Calendar.HOUR_OF_DAY);
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;
                datetime[3] = (byte)(lobyte | hibyte);

                temp = cal.get(Calendar.MINUTE);                
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;
                datetime[4] = (byte)(lobyte | hibyte);

                temp = cal.get(Calendar.SECOND);                
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;
                datetime[5] = (byte)(lobyte | hibyte);

                // the ZONE_OFFSET is expressed in quarters of an hour
                temp = cal.get(Calendar.ZONE_OFFSET)/(15 * 60 * 1000);
                hibyte = temp / 10;
                lobyte = (temp % 10) << 4;
                datetime[6] = (byte)(lobyte | hibyte);


                resp = new ProvideLocalInformationResponseData(datetime[0],
                        datetime[1], datetime[2], datetime[3], datetime[4], datetime[5], datetime[6]);

                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                        0, resp);

                return;
            } else if (cmdParams.cmdDet.commandQualifier == 0x04) {

                byte [] lang = new byte[2];
                Locale locale = Locale.getDefault();

                lang[0] = (byte)locale.getLanguage().charAt(0);
                lang[1] = (byte)locale.getLanguage().charAt(1);


                resp = new ProvideLocalInformationResponseData(lang);

                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                        0, resp);

                return;
            }
            return;
         }//end EVDO_DT_VIA_SUPPORT
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

        case LAUNCH_BROWSER:
                if ((((LaunchBrowserParams) cmdParams).confirmMsg.text != null)
                        && (((LaunchBrowserParams) cmdParams).confirmMsg.text.equals(STK_DEFAULT))) {
                    message = mContext.getText(com.android.internal.R.string.launchBrowserDefault);
                    ((LaunchBrowserParams) cmdParams).confirmMsg.text = message.toString();
                }
                break;
        case SELECT_ITEM:
            // add for [ALPS00245360] should not show DISPLAY_TEXT dialog when alarm booting
            isAlarmState = isAlarmBoot();
            try {
                flightMode = Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
            } catch(SettingNotFoundException e) {
                CatLog.d(this,"fail to get property from Settings");
                flightMode = 0;
            }
            isFlightMode = (flightMode != 0);
            CatLog.d(this, "isAlarmState = " + isAlarmState + ", isFlightMode = " + isFlightMode + ", flightMode = " + flightMode);
            if(isAlarmState && isFlightMode) {
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.UICC_SESSION_TERM_BY_USER, false, 0, null);
                return;
            }
        case GET_INPUT:
        case GET_INKEY:
                break;
        case SEND_DTMF:
    //if (! FeatureOption.EVDO_DT_VIA_SUPPORT) {
     //   case SEND_SMS:
    //}
        case SEND_SS:
        case SEND_USSD:
                    if ((((DisplayTextParams)cmdParams).textMsg.text != null)
                            && (((DisplayTextParams)cmdParams).textMsg.text.equals(STK_DEFAULT))) {
                        message = mContext.getText(com.android.internal.R.string.sending);
                        ((DisplayTextParams)cmdParams).textMsg.text = message.toString();
                    }
                break;
        case PLAY_TONE:
                break;
            case SET_UP_CALL:
                    if ((((CallSetupParams) cmdParams).confirmMsg.text != null)
                            && (((CallSetupParams) cmdParams).confirmMsg.text.equals(STK_DEFAULT))) {
                        message = mContext.getText(com.android.internal.R.string.SetupCallDefault);
                        ((CallSetupParams) cmdParams).confirmMsg.text = message.toString();
                    }
                break;
        //Add by Huibin Mao MTK80229
        //ICS Migration start
        case SET_UP_EVENT_LIST:
            mEventList = ((SetupEventListParams)cmdParams).eventList;
            return;
        case OPEN_CHANNEL:
            CatLog.d(this, "SS-handleProactiveCommand: process OPEN_CHANNEL");
            if(mPhone.getState() != Phone.State.IDLE) {
                CatLog.d("[BIP]", "SS-handleProactiveCommand: ME is busy on call");
                cmdMsg.mChannelStatus = new ChannelStatus(mBipMgr.getChannelId(), ChannelStatus.CHANNEL_STATUS_NO_LINK, ChannelStatus.CHANNEL_STATUS_INFO_NO_FURTHER_INFO);
                cmdMsg.mChannelStatus.isActivated = false;

                mCurrentCmd = cmdMsg;
                response = obtainMessage(MSG_ID_OPEN_CHANNEL_DONE, ErrorValue.ME_IS_BUSY_ON_CALL, 0, cmdMsg);
                response.sendToTarget();
                return;
            }
            break;
        case CLOSE_CHANNEL:
            CatLog.d(this, "SS-handleProactiveCommand: process CLOSE_CHANNEL");
            response = obtainMessage(MSG_ID_CLOSE_CHANNEL_DONE);
            mBipMgr.closeChannel(cmdMsg, response);
            break;
        case RECEIVE_DATA:
            CatLog.d(this, "SS-handleProactiveCommand: process RECEIVE_DATA");
            response = obtainMessage(MSG_ID_RECEIVE_DATA_DONE);
            mBipMgr.receiveData(cmdMsg, response);
            break;
        case SEND_DATA:
            CatLog.d(this, "SS-handleProactiveCommand: process SEND_DATA");
            response = obtainMessage(MSG_ID_SEND_DATA_DONE);
            mBipMgr.sendData(cmdMsg, response);
            break;
        case GET_CHANNEL_STATUS:
            CatLog.d(this, "SS-handleProactiveCommand: process GET_CHANNEL_STATUS");
            response = obtainMessage(MSG_ID_GET_CHANNEL_STATUS_DONE);
            mBipMgr.getChannelStatus(cmdMsg, response);
            break;
        //ICS Migration end

//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        case SEND_SMS:
            // nothing to do on telephony!
            break;
        case MORE_TIME:
            sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                    0, null);
            //There is no need to notify utkapp there is more time command
            //just send a respond is enougth
            return;
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

        default:
            CatLog.d(this, "SS-handleProactiveCommand: Unsupported command");
            return;
        }
        mCurrentCmd = cmdMsg;
        Intent intent = null;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
        if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
            intent = new Intent(AppInterface.UTK_CMD_ACTION);
            CatLog.d(this, "SS-handleProactiveCommand UTK: sending UTK_CMD_ACTION");
    } 
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    if (mPhone != null) {
        if (mPhone.getMySimId() == GEMINI_SIM_2) {
            intent = new Intent(AppInterface.CAT_CMD_ACTION_2);
            CatLog.d(this, "SS-handleProactiveCommand: sending CAT_CMD_ACTION_2");
        } else {
            intent = new Intent(AppInterface.CAT_CMD_ACTION);
            CatLog.d(this, "SS-handleProactiveCommand: sending CAT_CMD_ACTION");
        }
    }
        intent.putExtra("STK CMD", cmdMsg);
        mContext.sendBroadcast(intent);
    }

    /**
     * Handles RIL_UNSOL_STK_SESSION_END unsolicited command from RIL.
     *
     */
    private void handleSessionEnd() {
        CatLog.d(this, "SS-handleSessionEnd: SESSION END");
        Intent intent = null;

        mCurrentCmd = mMenuCmd;
    
    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
        intent = new Intent(AppInterface.UTK_SESSION_END_ACTION);
    } 

    if (mPhone != null) {
        if (mPhone.getMySimId() == GEMINI_SIM_2) {
            intent = new Intent(AppInterface.CAT_SESSION_END_ACTION_2);
        } else {
            intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        }
    }
        mContext.sendBroadcast(intent);
    }

    private void sendTerminalResponse(CommandDetails cmdDet,
            ResultCode resultCode, boolean includeAdditionalInfo,
            int additionalInfo, ResponseData resp) {

        if (cmdDet == null) {
            CatLog.d(this, "SS-sendTR: cmdDet is null");
            return;
        }
        CatLog.d(this, "SS-sendTR: command type is " + cmdDet.typeOfCommand);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        Input cmdInput = null;
        if (mCurrentCmd != null) {
            cmdInput = mCurrentCmd.geInput();
        }

        // command details
        int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
        if (cmdDet.compRequired) {
            tag |= 0x80;
        }
        buf.write(tag);
        buf.write(0x03); // length
        buf.write(cmdDet.commandNumber);
        buf.write(cmdDet.typeOfCommand);
        buf.write(cmdDet.commandQualifier);

        // device identities
        // According to TS102.223/TS31.111 section 6.8 Structure of
        // TERMINAL RESPONSE, "For all SIMPLE-TLV objects with Min=N,
        // the ME should set the CR(comprehension required) flag to
        // comprehension not required.(CR=0)"
        // Since DEVICE_IDENTITIES and DURATION TLVs have Min=N,
        // the CR flag is not set.
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); // length
        buf.write(DEV_ID_TERMINAL); // source device id
        buf.write(DEV_ID_UICC); // destination device id

        // result
        tag = 0x80 | ComprehensionTlvTag.RESULT.value();
        buf.write(tag);
        int length = includeAdditionalInfo ? 2 : 1;
        buf.write(length);
        buf.write(resultCode.value());

        // additional info
        if (includeAdditionalInfo) {
            buf.write(additionalInfo);
        }

        // Fill optional data for each corresponding command
        if (resp != null) {
            CatLog.d(this, "SS-sendTR: write response data into TR");
            resp.format(buf);
        } else {
            encodeOptionalTags(cmdDet, resultCode, cmdInput, buf);
        }

        byte[] rawData = buf.toByteArray();
        String hexString = IccUtils.bytesToHexString(rawData);
        if (Config.LOGD) {
            CatLog.d(this, "TERMINAL RESPONSE: " + hexString);
        }

        mCmdIf.sendTerminalResponse(hexString, null);
    }

    private void encodeOptionalTags(CommandDetails cmdDet,
            ResultCode resultCode, Input cmdInput, ByteArrayOutputStream buf) {
        CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdType != null) {
            switch (cmdType) {
            case GET_INKEY:
                // ETSI TS 102 384,27.22.4.2.8.4.2.
                // If it is a response for GET_INKEY command and the response timeout
                // occured, then add DURATION TLV for variable timeout case.
                if ((resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value()) &&
                        (cmdInput != null) && (cmdInput.duration != null)) {
                    getInKeyResponse(buf, cmdInput);
                }
                break;
            case PROVIDE_LOCAL_INFORMATION:
                if ((cmdDet.commandQualifier == CommandParamsFactory.LANGUAGE_SETTING) &&
                        (resultCode.value() == ResultCode.OK.value())) {
                    getPliResponse(buf);
                }
                break;
            default:
                CatLog.d(this, "encodeOptionalTags() Unsupported Cmd:" + cmdDet.typeOfCommand);
                break;
            }
        } else {
            CatLog.d(this, "encodeOptionalTags() bad Cmd:" + cmdDet.typeOfCommand);
        }
    }

    private void getInKeyResponse(ByteArrayOutputStream buf, Input cmdInput) {
        int tag = ComprehensionTlvTag.DURATION.value();

        buf.write(tag);
        buf.write(0x02); // length
        buf.write(cmdInput.duration.timeUnit.SECOND.value()); // Time (Unit,Seconds)
        buf.write(cmdInput.duration.timeInterval); // Time Duration
    }

    private void getPliResponse(ByteArrayOutputStream buf) {

        // Locale Language Setting
        String lang = SystemProperties.get("persist.sys.language");

        if (lang != null) {
            // tag
            int tag = ComprehensionTlvTag.LANGUAGE.value();
            buf.write(tag);
            ResponseData.writeLength(buf, lang.length());
            buf.write(lang.getBytes(), 0, lang.length());
        }
    }

    private void sendMenuSelection(int menuId, boolean helpRequired) {
        
        CatLog.d("CatService", "sendMenuSelection SET_UP_MENU");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        // tag
        int tag = BerTlv.BER_MENU_SELECTION_TAG;
        buf.write(tag);

        // length
        buf.write(0x00); // place holder

        // device identities
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); // length
        buf.write(DEV_ID_KEYPAD); // source device id
        buf.write(DEV_ID_UICC); // destination device id

        // item identifier
        tag = 0x80 | ComprehensionTlvTag.ITEM_ID.value();
        buf.write(tag);
        buf.write(0x01); // length
        buf.write(menuId); // menu identifier chosen

        // help request
        if (helpRequired) {
            tag = ComprehensionTlvTag.HELP_REQUEST.value();
            buf.write(tag);
            buf.write(0x00); // length
        }

        byte[] rawData = buf.toByteArray();

        // write real length
        int len = rawData.length - 2; // minus (tag + length)
        rawData[1] = (byte) len;

        String hexString = IccUtils.bytesToHexString(rawData);
        
        CatLog.d("CatService", "sendMenuSelection before  mCmdIf.sendEnvelope(hexString, null);");
        mCmdIf.sendEnvelope(hexString, null);
        CatLog.d("CatService", "sendMenuSelection before  mCmdIf.sendEnvelope(hexString, null);");
    }

    private void eventDownload(int event, int sourceId, int destinationId,
            byte[] additionalInfo, boolean oneShot) {

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        // remove the event list?
        if (null == mEventList || mEventList.length == 0){
            return;
        }
        // If there is no specific event in the event list,
        // StkService should not send ENVELOPE command to SIM
        for (int index = 0; index < mEventList.length; ){
            if (mEventList[index] == event){
                //if (true == oneShot){
                if (event ==  EVENT_LIST_ELEMENT_IDLE_SCREEN_AVAILABLE) {
                    CatLog.d(this, "SS-eventDownload: event is IDLE_SCREEN_AVAILABLE");
                    CatLog.d(this, "SS-eventDownload: sent intent with idle = false");
                    Intent intent = new Intent(IDLE_SCREEN_INTENT_NAME);
                    intent.putExtra(IDLE_SCREEN_ENABLE_KEY, false);
                    mContext.sendBroadcast(intent);

                    //IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                    //try {
                    //    wm.setEventDownloadNeeded(false);
                    //} catch (RemoteException e) {
                    //    StkLog.d(this, "Exception when set EventDownloadNeeded = false in WindowManager");
                    //}
                } else if(event == EVENT_LIST_ELEMENT_USER_ACTIVITY) {
                    CatLog.d(this, "SS-eventDownload: event is USER_ACTIVITY");
                    Intent intent = new Intent(USER_ACTIVITY_INTENT_NAME);
                    intent.putExtra(USER_ACTIVITY_ENABLE_KEY, false);
                    mContext.sendBroadcast(intent);
                }

                if(true == oneShot) {
                    mEventList[index] = 0;
                }
                //}
                break;
            }   
            else{
                index++;
                if(index == mEventList.length){
                    return;
                }
            }
        }

        // tag
        int tag = BerTlv.BER_EVENT_DOWNLOAD_TAG;
        buf.write(tag);

        // length
        buf.write(0x00); // place holder, assume length < 128.

        // event list
        tag = 0x80 | ComprehensionTlvTag.EVENT_LIST.value();
        buf.write(tag);
        buf.write(0x01); // length
        buf.write(event); // event value

        // device identities
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); // length
        buf.write(sourceId); // source device id
        buf.write(destinationId); // destination device id

        // additional information
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }

        byte[] rawData = buf.toByteArray();

        // write real length
        int len = rawData.length - 2; // minus (tag + length)
        rawData[1] = (byte) len;

        String hexString = IccUtils.bytesToHexString(rawData);

        mCmdIf.sendEnvelope(hexString, null);
    }

    /**
     * Used for instantiating/updating the Service from the GsmPhone or CdmaPhone constructor.
     *
     * @param ci CommandsInterface object
     * @param ir IccRecords object
     * @param context phone app context
     * @param fh Icc file handler
     * @param ic Icc card
     * @return The only Service object in the system
     */

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    public static CatService getInstance(CommandsInterface ci, RuimRecords ur,
            Context context, RuimFileHandler fh, RuimCard uc) {
            	
        CatLog.d("CatService", "UTK get Instance");
        if (sInstance == null) {
            if (ci == null || ur == null || context == null || fh == null
                    || uc == null) {
                return null;
            }
            //HandlerThread thread = new HandlerThread("Cat Telephony service");
            //thread.start();
            sInstance = new CatService(ci, ur, context, fh, uc);
            CatLog.d("CatService", "UTK NEW sInstance");
        } else if ((ur != null) && (mRuimRecords != ur)) {
            CatLog.d("CatService", "UTK Reinitialize the Service with RuimRecords");
            //CatLog.d("CatService", String.format(
            //        "Reinitialize the Service with SIMRecords sr=0x%x.", ur));
if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
            sInstance.mCmdIf = ci;
            sInstance.mContext = context;

            // Get the RilMessagesDecoder for decoding the messages.
            sInstance.mMsgDecoder = RilMessageDecoder.getInstance(sInstance, fh);

            // Register ril events handling.
            sInstance.mCmdIf.setOnUtkSessionEnd(sInstance, MSG_ID_SESSION_END, null);
            sInstance.mCmdIf.setOnUtkProactiveCmd(sInstance, MSG_ID_PROACTIVE_COMMAND, null);
            sInstance.mCmdIf.setOnUtkEvent(sInstance, MSG_ID_EVENT_NOTIFY, null);
}
            mRuimRecords = ur;

            // re-Register for SIM ready event.
            mRuimRecords.registerForRecordsLoaded(sInstance, MSG_ID_SIM_LOADED, null);
            CatLog.d("CatService", "UTK ur changed reinitialize and return current sInstance");
        } else {
            CatLog.d("CatService", "UTK Return current sInstance");
        }
        return sInstance;
    }
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    // mtk02374 GEMINI
    public static CatService getInstance(GSMPhone phone,CommandsInterface ci, IccRecords ir,
            Context context, IccFileHandler fh, IccCard ic, int simId) 
    {
        synchronized (sInstanceLock) 
        {
            String cmd = null;
            if ((GEMINI_SIM_2 == simId && sInstanceSim2 == null) ||(GEMINI_SIM_1 == simId && sInstanceSim1 == null)) 
            {
                CatService tempInstance = null;
                if (ci == null || ir == null || context == null || fh == null
                        || ic == null) {
                    return null;
                }
                //HandlerThread thread = new HandlerThread("Cat Telephony service");
                //thread.start();
                tempInstance  = new CatService(phone,ci, ir, context, fh, ic,simId);
                if (GEMINI_SIM_2 == simId) {
                    CatLog.d(tempInstance, "read data from sInstSim2");
                    cmd = db.readDataFromDB(sInst2);
                    CatLog.d(tempInstance, "NEW sInstanceSim2");
                    sInstanceSim2 = tempInstance;
                } else {
                    CatLog.d(tempInstance, "read data from sInstSim1");
                    cmd = db.readDataFromDB(sInst1);
                    CatLog.d(tempInstance, "NEW sInstanceSim1");
                    sInstanceSim1 = tempInstance;
                }
                handleProactiveCmdFromDB(tempInstance, cmd);
            }
            else if ((ir != null) && (mIccRecords != ir)) 
            {
                CatLog.d("CatService", "Reinitialize the Service with SIMRecords");
                mIccRecords = ir;

                // re-Register for SIM ready event.
                //mIccRecords.registerForRecordsLoaded(sInstance, MSG_ID_ICC_RECORDS_LOADED, null);
                // re-Register for SIM ready event.
                if (GEMINI_SIM_2 == simId) 
                {
                    CatLog.d("CatService", "read data from sInstSim2");
                    cmd = db.readDataFromDB(sInst2);
                    mIccRecords.registerForRecordsLoaded(sInstanceSim2, MSG_ID_ICC_RECORDS_LOADED, null);
                    handleProactiveCmdFromDB(sInstanceSim2, cmd);
                } 
                else 
                {
if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
                    sInstanceSim1.mCmdIf = ci;
                    sInstanceSim1.mContext = context;
                    sInstanceSim1.mPhone = phone;

                    simId = Phone.GEMINI_SIM_1;
                    if (sInstanceSim1.mPhone != null) {
                        simId = phone.getMySimId();
                    }

                    // Get the RilMessagesDecoder for decoding the messages.
                    sInstanceSim1.mMsgDecoder = RilMessageDecoder.getInstance(sInstanceSim1, fh, simId);

                    // Register ril events handling.
                    sInstanceSim1.mCmdIf.setOnCatSessionEnd(sInstanceSim1, MSG_ID_SESSION_END, null);
                    sInstanceSim1.mCmdIf.setOnCatProactiveCmd(sInstanceSim1, MSG_ID_PROACTIVE_COMMAND, null);
                    sInstanceSim1.mCmdIf.setOnCatEvent(sInstanceSim1, MSG_ID_EVENT_NOTIFY, null);
                    sInstanceSim1.mCmdIf.setOnCatCallSetUp(sInstanceSim1, MSG_ID_CALL_SETUP, null);
                    //sInstanceSim1.mCmdIf.setOnSimRefresh(sInstanceSim1, MSG_ID_REFRESH, null);
        
                    // Register for SIM ready event.
                    sInstanceSim1.mCmdIf.registerForSIMReady(sInstanceSim1, MSG_ID_SIM_READY, null);
                    sInstanceSim1.mCmdIf.registerForRUIMReady(sInstanceSim1, MSG_ID_SIM_READY, null);
                    sInstanceSim1.mCmdIf.registerForNVReady(sInstanceSim1, MSG_ID_SIM_READY, null);
}

                    CatLog.d("CatService", "read data from sInstSim1");
                    cmd = db.readDataFromDB(sInst1);
                    mIccRecords.registerForRecordsLoaded(sInstanceSim1, MSG_ID_ICC_RECORDS_LOADED, null);
                    handleProactiveCmdFromDB(sInstanceSim1, cmd);
                }            
                CatLog.d("CatService", "sr changed reinitialize and return current sInstance");
            } 
            else 
            {
                CatLog.d("CatService", "Return current sInstance");
            }

            if (GEMINI_SIM_2 == simId) 
            {
                return sInstanceSim2;
            } 
            else 
            {
                return sInstanceSim1;
            }
        }

    }


    public static CatService getInstance(CommandsInterface ci,IccRecords ir,Context context,IccFileHandler fh, IccCard ic) 
    {
        return getInstance(null, ci, ir, context, fh, ic, GEMINI_SIM_1);
    }

    /**
     * Used by application to get an AppInterface object.
     *
     * @return The only Service object in the system
     */
    public static AppInterface getInstance(int simId) {
        return getInstance(null, null, null, null, null, null, simId);
    }

    /**
     * Used by application to get an AppInterface object.
     *
     * @return The only Service object in the system
     */
    public static AppInterface getInstance() {
        CatLog.d("CatService", "Get Default Instance");
        return getInstance(null, null, null, null, null, null, GEMINI_SIM_1);
    }

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    public static AppInterface getInstance2() {
        CatLog.d("CatService", "UTK Get Default Instance2");
            return getInstance(null, null, null, null, null);
    }
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

    /* when read set up menu data from db, handle it*/
    private static void handleProactiveCmdFromDB(CatService inst, String data) {
        if(data == null) {
    	    CatLog.d("CatService", "handleProactiveCmdFromDB: cmd = null");
    	    return;	
    	}
    	    
    	CatLog.d("CatService", " handleProactiveCmdFromDB: cmd = " + data + " from: " + inst);
        RilMessage rilMsg = new RilMessage(MSG_ID_PROACTIVE_COMMAND, data);
        inst.mMsgDecoder.sendStartDecodingMessageParams(rilMsg);
        CatLog.d("CatService", "handleProactiveCmdFromDB: over");
    }
    
    /* if the second byte is "81", and the seventh byte is "25", this cmd is valid set up menu cmd
     * if the second byte is not "81", but the sixth byte is "25", this cmd is valid set up menu cmd, too.
     * else, it is not a set up menu, no need to save it into db
     */
    private boolean isSetUpMenuCmd(String cmd) {
        	boolean validCmd = false;
        	
        	if(cmd == null) {
        	    return false;	
        	}
        	
        	if((cmd.charAt(2) == '8') && (cmd.charAt(3) == '1')) {
        	    if((cmd.charAt(12) == '2') && (cmd.charAt(13) == '5')) {
        	        validCmd = true;
        	    }
        	} else {
        	    if((cmd.charAt(10) == '2') && (cmd.charAt(11) == '5')) {
        	        validCmd = true;	
        	    }	
        	}
        	
        	return validCmd;
    }
    
    @Override
    public void handleMessage(Message msg) {
        CatCmdMessage cmd = null;
        ResponseData resp = null;
        int ret = 0;

        switch (msg.what) {
        case MSG_ID_SESSION_END:
        case MSG_ID_PROACTIVE_COMMAND:
        case MSG_ID_EVENT_NOTIFY:
        case MSG_ID_REFRESH:
            CatLog.d(this, "ril message arrived");
            String data = null;
            if (msg.obj != null) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar != null && ar.result != null) {
                    try {
                        data = (String) ar.result;
                        
                        //if the data is valid set up cmd, save it into db
                        boolean isValid = isSetUpMenuCmd(data);
                        if(isValid && this == sInstanceSim1) {
                            CatLog.d(this, "ril message arrived : save data to db 1");
                            db.saveDataToDB(sInst1, data);	
                        } else if (isValid && this == sInstanceSim2) {
                            CatLog.d(this, "ril message arrived : save data to db 2");
                            db.saveDataToDB(sInst2, data);	
                        }
                        
                    } catch (ClassCastException e) {
                        break;
                    }
                }
            }
            if (data == null) {
                CatLog.d(this, "data is null .  FZLFZLFZL....");
            }

            if (mMsgDecoder == null) {
                CatLog.d(this, "mMsgDecoder is null .  FZLFZLFZL....");
                if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
                	  CatLog.d(this, "mMsgDecoder is null .  2 reference");
                    mMsgDecoder = RilMessageDecoder.getInstance(this, rfh);
                } else {
                	  CatLog.d(this, "mMsgDecoder is null .  3 reference.");
                	  mMsgDecoder = RilMessageDecoder.getInstance(this, ifh, simId);
                }
            }

            mMsgDecoder.setCDMAPhoneFlag(CDMAPhone_flag);
            mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, data));
            break;
        case MSG_ID_CALL_SETUP:
            mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
            break;

    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    //if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
        case MSG_ID_SIM_LOADED:
            break;
    //} else {
        case MSG_ID_ICC_RECORDS_LOADED:
            break;
    //}
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK
            
        case MSG_ID_RIL_MSG_DECODED:
            handleRilMsg((RilMessage) msg.obj);
            break;
        case MSG_ID_RESPONSE:
            handleCmdResponse((CatResponseMessage) msg.obj);
            break;
        case MSG_ID_EVENT_DOWNLOAD:
            handleEventDownload((CatResponseMessage) msg.obj);
            break;
        case MSG_ID_OPEN_CHANNEL_DONE:
            ret = msg.arg1;
            cmd = (CatCmdMessage)msg.obj;
            // resp = new OpenChannelResponseData(cmd.mChannelStatus, cmd.mBearerDesc, cmd.mBufferSize);
            if(ret == ErrorValue.NO_ERROR) {
                resp = new OpenChannelResponseData(cmd.mChannelStatus, cmd.mBearerDesc, cmd.mBufferSize);
                CatLog.d("[BIP]", "SS-handleMessage: open channel successfully");
                sendTerminalResponse(mCurrentCmd.mCmdDet, ResultCode.OK, false, 0, resp);
            } else if(ret == ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION) {
                resp = new OpenChannelResponseData(cmd.mChannelStatus, cmd.mBearerDesc, cmd.mBufferSize);
                CatLog.d("[BIP]", "SS-handleMessage: open channel with modified parameters");
                sendTerminalResponse(mCurrentCmd.mCmdDet, ResultCode.PRFRMD_WITH_MODIFICATION, false, 0, resp);
            } else if(ret == ErrorValue.ME_IS_BUSY_ON_CALL) {
                resp = new OpenChannelResponseData(null, cmd.mBearerDesc, cmd.mBufferSize);
                CatLog.d("[BIP]", "SS-handleMessage: ME is busy on call");
                sendTerminalResponse(mCurrentCmd.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true,
                        ADDITIONAL_INFO_FOR_BIP_CHANNEL_CLOSED, resp);
            } else {
                resp = new OpenChannelResponseData(cmd.mChannelStatus, cmd.mBearerDesc, cmd.mBufferSize);
                CatLog.d("[BIP]", "SS-handleMessage: open channel failed");
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, false, 0, resp);
            }
            break;
        case MSG_ID_SEND_DATA_DONE:
            ret = msg.arg1;
            int size = msg.arg2;
            cmd = (CatCmdMessage)msg.obj;
            resp = new SendDataResponseData(size);
            if(ret == ErrorValue.NO_ERROR) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.OK, false, 0, resp);
            } else if(ret == ErrorValue.CHANNEL_ID_NOT_VALID){
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, true, ADDITIONAL_INFO_FOR_BIP_CHANNEL_ID_NOT_AVAILABLE, null);
            } else {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, false, 0, resp);
            }
            break;
        case MSG_ID_RECEIVE_DATA_DONE:
            ret = msg.arg1;
            cmd = (CatCmdMessage)msg.obj;
            byte[] buffer = cmd.mChannelData;
            int remainingCount = cmd.mRemainingDataLength;

            resp = new ReceiveDataResponseData(buffer, remainingCount);
            if(ret == ErrorValue.NO_ERROR) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.OK, false, 0, resp);
            } else if(ret == ErrorValue.MISSING_DATA) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.PRFRMD_WITH_MISSING_INFO, false, 0, resp);
            } else {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, false, 0, null);
            }
            break;
        case MSG_ID_CLOSE_CHANNEL_DONE:
            cmd = (CatCmdMessage)msg.obj;
            if(msg.arg1 == ErrorValue.NO_ERROR) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.OK, false, 0, null);
            } else if(msg.arg1 == ErrorValue.CHANNEL_ID_NOT_VALID) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, true, ADDITIONAL_INFO_FOR_BIP_CHANNEL_ID_NOT_AVAILABLE, null);
            } else if(msg.arg1 == ErrorValue.CHANNEL_ALREADY_CLOSED) {
                sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, true, ADDITIONAL_INFO_FOR_BIP_CHANNEL_CLOSED, null);
            }
            break;
        case MSG_ID_GET_CHANNEL_STATUS_DONE:
            ret = msg.arg1;
            cmd = (CatCmdMessage)msg.obj;
            int cid = ((ChannelStatus)cmd.mChannelStatus).mChannelId;
            int status = ((ChannelStatus)cmd.mChannelStatus).mChannelStatus;
            int statusInfo = ((ChannelStatus)cmd.mChannelStatus).mChannelStatusInfo;

            CatLog.d("[BIP]", "SS-handleCmdResponse: MSG_ID_GET_CHANNEL_STATUS_DONE:" + cid + ":" + status + ":" + statusInfo);
            resp = new GetChannelStatusResponseData(cid, status, statusInfo);
            sendTerminalResponse(cmd.mCmdDet, ResultCode.OK, false, 0, resp);
            break;
        case MSG_ID_CONN_MGR_TIMEOUT:
            cmd = (CatCmdMessage)msg.obj;
            resp = new OpenChannelResponseData(cmd.mChannelStatus, cmd.mBearerDesc, cmd.mBufferSize);
            CatLog.d("[BIP]", "SS-handleMessage: timeout for ConnMgr intent. " + cmd.mCmdDet.typeOfCommand);
            sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, false, 0, resp);
            
            mBipMgr.setConnMgrTimeoutFlag(true);
            break;
        case MSG_ID_SIM_READY:
            CatLog.d(this, "SIM ready. Reporting STK service running now...");
            mCmdIf.reportStkServiceIsRunning(null);
            break;
    
    //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
    //if (FeatureOption.EVDO_DT_VIA_SUPPORT) {
        case MSG_ID_RIL_LOCAL_INFO:
        {
            AsyncResult aresult = (AsyncResult) msg.obj;

            if (aresult.result != null) {
                int info[] = (int[])aresult.result;

                if (info.length == 8) {
                    mLocalInfo.Technology = info[0];
                    mLocalInfo.MCC = info[1];
                    mLocalInfo.IMSI_11_12 = info[2];
                    mLocalInfo.SID = info[3];
                    mLocalInfo.NID = info[4];
                    mLocalInfo.BASE_ID = info[5];
                    mLocalInfo.BASE_LAT = info[6];
                    mLocalInfo.BASE_LONG = info[7];
                } else {
                    CatLog.d(sInstance, "MSG_ID_RIL_LOCAL_INFO error");
                }
            }
            sendTerminalResponse(mCurrentCmd.mCmdDet, ResultCode.OK, false,
                0, new LocalInformationResponseData(mCurrentCmd.mCmdDet.commandQualifier, mLocalInfo));
            mCurrentCmd = null;
            break;
        }
        case MSG_ID_RIL_REFRESH_RESULT:{
            CatLog.d(this, "MSG_ID_RIL_REFRESH_RESULT  Complete! ");
            Intent intent = new Intent();
            intent.setAction("com.android.contacts.action.CONTACTS_INIT_RETRY_ACTION");
            mContext.sendBroadcast(intent);
            mCurrentCmd = null;
            break;
        }
    //}//end  EVDO_DT_VIA_SUPPORT
    //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

        default:
            throw new AssertionError("Unrecognized CAT command: " + msg.what);
        }
    }

    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        if (resMsg == null) {
            return;
        }
        // queue a response message.
        Message msg = this.obtainMessage(MSG_ID_RESPONSE, resMsg);
        msg.sendToTarget();
    }

    public synchronized void onEventDownload(CatResponseMessage resMsg) {
        if (resMsg == null) {
            return;
        }
        // queue a response message.
        Message msg = this.obtainMessage(MSG_ID_EVENT_DOWNLOAD, resMsg);
        msg.sendToTarget();
    }
    private boolean validateResponse(CatResponseMessage resMsg) {
        boolean ret = false;
        if (mCurrentCmd != null) {
            ret = (resMsg.cmdDet.compareTo(mCurrentCmd.mCmdDet));
            CatLog.d(this, "SS-validateResponse: ret=" + ret + 
                " [" + resMsg.cmdDet.typeOfCommand + 
                "/" + mCurrentCmd.mCmdDet.typeOfCommand + "]");
            return ret;
        }
        CatLog.d(this, "SS-validateResponse: mCurrentCmd is null");
        return false;
    }

    private boolean removeMenu(Menu menu) {
        try {
            if (menu.items.size() == 1 && menu.items.get(0) == null) {
                return true;
            }
        } catch (NullPointerException e) {
            CatLog.d(this, "Unable to get Menu's items size");
            return true;
        }
        return false;
    }

    private void handleEventDownload(CatResponseMessage resMsg) {
        eventDownload(resMsg.event,resMsg.sourceId, resMsg.destinationId, 
                resMsg.additionalInfo, resMsg.oneShot);
    }

    private void handleCmdResponse(CatResponseMessage resMsg) {
        // Make sure the response details match the last valid command. An invalid
        // response is a one that doesn't have a corresponding proactive command
        // and sending it can "confuse" the baseband/ril.
        // One reason for out of order responses can be UI glitches. For example,
        // if the application launch an activity, and that activity is stored
        // by the framework inside the history stack. That activity will be
        // available for relaunch using the latest application dialog
        // (long press on the home button). Relaunching that activity can send
        // the same command's result again to the CatService and can cause it to
        // get out of sync with the SIM.
        if (!validateResponse(resMsg)) {
            return;
        }
        ResponseData resp = null;
        boolean helpRequired = false;
        CommandDetails cmdDet = resMsg.getCmdDetails();
        CatLog.d(this, "handleCmdResponse:resMsg.resCode = "+resMsg.resCode);
        switch (resMsg.resCode) {
        case HELP_INFO_REQUIRED:
            helpRequired = true;
            // fall through
        case OK:
        case PRFRMD_WITH_PARTIAL_COMPREHENSION:
        case PRFRMD_WITH_MISSING_INFO:
        case PRFRMD_WITH_ADDITIONAL_EFS_READ:
        case PRFRMD_ICON_NOT_DISPLAYED:
        case PRFRMD_MODIFIED_BY_NAA:
        case PRFRMD_LIMITED_SERVICE:
        case PRFRMD_WITH_MODIFICATION:
        case PRFRMD_NAA_NOT_ACTIVE:
        case PRFRMD_TONE_NOT_PLAYED:
            switch (AppInterface.CommandType.fromInt(cmdDet.typeOfCommand)) {
            case SET_UP_MENU:
                CatLog.d("CatService", "SET_UP_MENU");
                helpRequired = resMsg.resCode == ResultCode.HELP_INFO_REQUIRED;
                sendMenuSelection(resMsg.usersMenuSelection, helpRequired);
                return;
            case SELECT_ITEM:
                CatLog.d("CatService", "SELECT_ITEM");
                resp = new SelectItemResponseData(resMsg.usersMenuSelection);
                break;
            case GET_INPUT:
            case GET_INKEY:
                Input input = mCurrentCmd.geInput();
                if (!input.yesNo) {
                    // when help is requested there is no need to send the text
                    // string object.
                    if (!helpRequired) {
                        resp = new GetInkeyInputResponseData(resMsg.usersInput,
                                input.ucs2, input.packed);
                    }
                } else {
                    resp = new GetInkeyInputResponseData(
                            resMsg.usersYesNoSelection);
                }
                break;
            case DISPLAY_TEXT:
            case LAUNCH_BROWSER:
                break;
            case SET_UP_CALL:
                //MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
                if (FeatureOption.EVDO_DT_VIA_SUPPORT && CDMAPhone_flag) {
                    mCmdIf.handleCallSetupRequestFromUim(resMsg.usersConfirm, null);
                } else {
                // mCmdIf.handleCallSetupRequestFromSim(resMsg.usersConfirm, null);
                mCmdIf.handleCallSetupRequestFromSim(resMsg.usersConfirm, resMsg.resCode.value(), null);
                // No need to send terminal response for SET UP CALL. The user's
                // confirmation result is send back using a dedicated ril message
                // invoked by the CommandInterface call above.
                }
                //MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

                mCurrentCmd = null;
                return;
            case OPEN_CHANNEL:
                CatLog.d("[BIP]", "SS-handleCmdResponse: user accept to open channel");
                Message response = obtainMessage(MSG_ID_OPEN_CHANNEL_DONE);
                if(mCurrentCmd != null) {
                    mBipMgr.openChannel(mCurrentCmd, response);
                } else {
                    CatLog.d("[BIP]", "SS-handleCmdResponse: invalid OPEN_CHANNEL");
                }
                return;
            }
            break;
        case NO_RESPONSE_FROM_USER:
        case UICC_SESSION_TERM_BY_USER:
        case BACKWARD_MOVE_BY_USER:
        case CMD_DATA_NOT_UNDERSTOOD:
            switch (AppInterface.CommandType.fromInt(cmdDet.typeOfCommand)) {
            case SET_UP_CALL:
                CatLog.d(this, "SS-handleCmdResponse: [BACKWARD_MOVE_BY_USER] userConfirm["
                        + resMsg.usersConfirm + "] resultCode[" + resMsg.resCode.value() + "]");
                mCmdIf.handleCallSetupRequestFromSim(false, ResultCode.BACKWARD_MOVE_BY_USER.value(), null);
                break;
            }
            resp = null;
            break;
        case TERMINAL_CRNTLY_UNABLE_TO_PROCESS:
        case NETWORK_CRNTLY_UNABLE_TO_PROCESS:
            switch (AppInterface.CommandType.fromInt(cmdDet.typeOfCommand)) {           
            case SET_UP_CALL:
                mCmdIf.handleCallSetupRequestFromSim(resMsg.usersConfirm, resMsg.resCode.value(), null);
                // No need to send terminal response for SET UP CALL. The user's
                // confirmation result is send back using a dedicated ril message
                // invoked by the CommandInterface call above.
                mCurrentCmd = null;
                return;  
            case DISPLAY_TEXT:
                if (resMsg.additionalInfo != null && resMsg.additionalInfo.length > 0 && (int)(resMsg.additionalInfo[0]) != 0) {
                    sendTerminalResponse(cmdDet, resMsg.resCode, true, (int)(resMsg.additionalInfo[0]), resp);
                    mCurrentCmd = null;
                    return;
                }
                break;
            default:
                break;
            }
            break;
        case USER_NOT_ACCEPT:
            switch(AppInterface.CommandType.fromInt(cmdDet.typeOfCommand)) {
            case OPEN_CHANNEL:
                CatLog.d("[BIP]", "SS-handleCmdResponse: User don't accept open channel");
                if(mCurrentCmd != null 
                        // && mCurrentCmd.mChannelStatus != null 
                        && mCurrentCmd.mBearerDesc != null 
                        && mCurrentCmd.mBufferSize > 0) {
                    // resp = new OpenChannelResponseData(mCurrentCmd.mChannelStatus, mCurrentCmd.mBearerDesc, mCurrentCmd.mBufferSize);
                    resp = new OpenChannelResponseData(null, mCurrentCmd.mBearerDesc, mCurrentCmd.mBufferSize);
                    sendTerminalResponse(cmdDet, resMsg.resCode, false, 0, resp);
                } else {
                    if(mCurrentCmd == null) {
                        CatLog.d("[BIP]", "SS-handleCmdResponse: mCurrent is null");
                    } else {
                        CatLog.d("[BIP]", "SS-handleCmdResponse: other params is invalid");
                    }
                }
                return;
            default:
                break;
            }
            break;
        case LAUNCH_BROWSER_ERROR:
            if(cmdDet.typeOfCommand == AppInterface.CommandType.LAUNCH_BROWSER.value()) {
                CatLog.d(this, "send TR for LAUNCH_BROWSER_ERROR");
                sendTerminalResponse(cmdDet, resMsg.resCode, true, 0x02, null);
                return;
            }
            break;
        default:
            return;
        }
        sendTerminalResponse(cmdDet, resMsg.resCode, false, 0, resp);
        mCurrentCmd = null;
    }

    public Context getContext() {
        return mContext;
    }

    private BroadcastReceiver CatServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CatLog.d(this, "CatServiceReceiver action: " + action);
            if(action == ACTION_SHUTDOWN_IPO) {
                CatLog.d(this, "remove event list because of ipo shutdown");
                mEventList = null;
            }
        }
    };
    
    // MTK-START [ALPS00092673] Orange feature merge back added by mtk80589 in 2011.11.15
    /*
      Detail description:
      This feature provides a interface to get menu title string from EF_SUME
    */
    // MTK_OP03_PROTECT_START
    public String getMenuTitleFromEf() {
        return mIccRecords.getMenuTitleFromEf();
    }
    // MTK_OP03_PROTECT_END
    // MTK-END [ALPS00092673] Orange feature merge back added by mtk80589 in 2011.11.15
    
    // add for [ALPS00245360] should not show DISPLAY_TEXT dialog when alarm booting
    public boolean isAlarmBoot(){
        String bootReason = SystemProperties.get("sys.boot.reason");
        return (bootReason != null && bootReason.equals("1"));
    }
    
    private boolean checkSetupWizardInstalled() {
        final String packageName = "com.google.android.setupwizard";
        final String activityName = "com.google.android.setupwizard.SetupWizardActivity";
        
        PackageManager pm = mContext.getPackageManager();
        if(pm == null) {
            CatLog.d(this, "fail to get PM");
            return false;
        }
        
        // ComponentName cm = new ComponentName(packageName, activityName);
        boolean isPkgInstalled = true;
        try {
            pm.getInstallerPackageName(packageName);
        } catch(IllegalArgumentException e) {
            CatLog.d(this, "fail to get SetupWizard package");
            isPkgInstalled = false;
        }
        
        if(isPkgInstalled == true) {
            int pkgEnabledState = pm.getComponentEnabledSetting(new ComponentName(packageName, activityName));
            if(pkgEnabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || pkgEnabledState ==  PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                CatLog.d(this, "should not show DISPLAY_TEXT immediately");
                return true;
            }
        }
        
        CatLog.d(this, "isPkgInstalled = false");
        return false;
    }
}
