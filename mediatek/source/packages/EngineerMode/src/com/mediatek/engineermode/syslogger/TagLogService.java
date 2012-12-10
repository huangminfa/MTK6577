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

package com.mediatek.engineermode.syslogger;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.os.SystemProperties;

import com.mediatek.engineermode.R;

import java.io.File;
import java.io.IOException;

/**
 * 
 * 
 * @author mtk54043
 * 
 */
public class TagLogService extends Service {

    private static final String TAG = "Syslog_taglog";

    private String mTag = "";
    private String mDbPathFromAee; // e.g: "/mnt/sdcard/mtklog/aee_exp/db.00"

    // provider tag log information to log2server
    private String mTagLogResult; // cancel , failed , successful
    private String[] mLogPathInTagLog; // log path in tag log folder
    private Bundle mDataFromAee; // aee information
    private String mModemLogPath; // modem log path from intent provide by modem log tool

    private boolean[] mLogToolStatus; // Status of MDLog/MobileLog/NetLog
    private boolean mIsStartService;
    private boolean mIsNormalExcep;
    private boolean mIsInputDialogClicked; // Use to check time out
    private boolean mIsModemExp;
    private boolean mIsModemLogReady;

    private UIHandler mUIHandler;
    private FunctionHandler mFunctionHandler;

    private AlertDialog mDialog;
    private ProgressDialog mProgressDialog;

    private InternalReceiver internalReceicer;    

    // private NotificationManager mNotiMgr;

    private static final String LOGTOOLFOLDER[] = { SysUtils.MODEM_LOG_FOLDER,
            SysUtils.MOBILE_LOG_FOLDER, SysUtils.NET_LOG_FOLDER,
            SysUtils.EXTMODEM_LOG_FOLDER };

    // The tag log path to log2server
    private static final String LOGPATHKEY[] = {
            SysUtils.BROADCAST_KEY_MDLOG_PATH,
            SysUtils.BROADCAST_KEY_MOBILELOG_PATH,
            SysUtils.BROADCAST_KEY_NETLOG_PATH };

    @Override
    public void onCreate() {
        Log.d(TAG, "TagLogService-->onCreate");
        init();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TagLogService-->onStartCommand");
        if (null == intent) {
            Log.e(TAG, "Intent is null!!");
            return super.onStartCommand(intent, flags, startId);
        }

        if (mIsStartService) {
            Log.e(TAG, "The service is runing!");
            Toast.makeText(this, R.string.taglog_busy, Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        } else {
            mIsStartService = true;
        }

        mDataFromAee = intent.getExtras(); // Exception info from aee intent

        mUIHandler.sendEmptyMessage(SysUtils.EVENT_CREATE_INPUTDIALOG);

        Log.d(TAG, "TagLogService-->onStartCommand End");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TagLogService-->onDestroy");
        deInit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "TagLogService-->onBind");
        return null;
    }

    // --------------- -------- ****** -------- ****** -------- ---------------

    private void init() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SysUtils.ACTION_MDLOG_READY);
        internalReceicer = new InternalReceiver();
        registerReceiver(internalReceicer, intentFilter);
        mIsStartService = false;
        mLogPathInTagLog = new String[SysUtils.LOG_QUANTITY];
        mUIHandler = new UIHandler();

        HandlerThread ht = new HandlerThread("taglog_function");
        ht.start();
        mFunctionHandler = new FunctionHandler(ht.getLooper());
    }

    private void deInit() {

        // if (!mIsNormalExcep) {
        // clearRedScreen();
        // }

        unregisterReceiver(internalReceicer);

        mIsStartService = false; // Service is destroyed

        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        if (null != mFunctionHandler) {
            mFunctionHandler.getLooper().quit();
            mFunctionHandler = null;
        }
        
        if (null != mUIHandler) {
            mUIHandler = null;
        }

        mLogToolStatus = null;

        /** new feature in JB */
        //sendIntentToLog2Server();
        mLogPathInTagLog = null;
        
        //Toast.makeText(this, R.string.taglog_exit, Toast.LENGTH_SHORT).show();
    }

    private void sendIntentToLog2Server() {
        Intent intent = new Intent();
        intent.setAction(SysUtils.ACTION_TAGLOG_TO_LOG2SERVER);
        if (null != mTagLogResult) {
            intent.putExtra(SysUtils.BROADCAST_KEY_TAGLOG_RESULT,
                            mTagLogResult);
            Log.d(TAG, SysUtils.BROADCAST_KEY_TAGLOG_RESULT + " = "
                    + mTagLogResult);
        } else {
            // maybe service are destroyed unexpectedly
            Log.e(TAG, "mTagLogResult is null!");
        }

        if (null != mLogPathInTagLog) {
            for (int i = 0; i < mLogPathInTagLog.length; i++) {
                if (null != mLogPathInTagLog[i]) {
                    intent.putExtra(LOGPATHKEY[i], mLogPathInTagLog[i]);
                    Log.d(TAG, LOGPATHKEY[i] + " = " + mLogPathInTagLog[i]);
                } else {
                    Log.e(TAG, "mLogPathInTagLog[" + i + "]" + "= null!");
                }
            }
        } else {
            Log.e(TAG, "mLogPathInTagLog is null");
        }
        if (null != mDataFromAee) {
            intent.putExtras(mDataFromAee);
        } else {
            Log.e(TAG, "Data From Aee is null");
        }
        TagLogService.this.sendBroadcast(intent);
        Log.d(TAG, "send intent to Log2Server");
    }

    private class UIHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.d(TAG, " MyHandler handleMessage --> start " + msg.what);
            switch (msg.what) {
            case SysUtils.EVENT_CREATE_INPUTDIALOG:
                //Log.e(TAG, "TagLogService ANR Test --> Init exp start");
                String exp_path = mDataFromAee.getString(SysUtils.EXTRA_KEY_EXP_PATH);
                String exp_file_name = mDataFromAee
                        .getString(SysUtils.EXTRA_KEY_EXP_NAME);
                String zz_file_name = mDataFromAee.getString(SysUtils.EXTRA_KEY_EXP_ZZ);
                if (exp_path == null || exp_file_name == null || zz_file_name == null) {
                    Log.d(TAG, "params are not valid!");
                    TagLogService.this.stopSelf();
                    return;
                }
                mDbPathFromAee = exp_path;
                String zz_file = exp_path + zz_file_name;
                ExceptionInfo exp_info = new ExceptionInfo();
                String exp_file = exp_path + exp_file_name;
                Log.e(TAG, "exp_path: " + exp_path);
                Log.e(TAG, "exp_file: " + exp_file);
                exp_info.setmPath(exp_file);
                try {
                    exp_info.initFieldsFromZZ(zz_file);
                } catch (IOException e) {
                    Log.e(TAG, "fail to init exception info:" + e.getMessage());
                    TagLogService.this.stopSelf();
                    return;
                }

                String expType = exp_info.getmType();
                String expDiscription = exp_info.getmDiscription();
                String expProcess = exp_info.getmProcess();
                if (null != expType) {
                    Log.i(TAG, "exp_info.getmType(): " + expType);
                }
                if (null != expDiscription) {
                    Log.i(TAG, "exp_info.getmDiscription(): " + expDiscription);
                }
                if (null != expProcess) {
                    Log.i(TAG, "exp_info.getmProcess(): " + expProcess);
                }

                if (expType != null && expType.endsWith("Externel (EE)")) {
                    Log.i(TAG, "expType == External (EE) " + expType);
                    mIsModemExp = true;
                }
                //Log.e(TAG, "TagLogService ANR Test <-- Init exp end");
                /** get status of log tool after insure exception type */
                mLogToolStatus = getLogToolStatus();                
                for (int i = 0; i < mLogToolStatus.length; i++) {
                    if (mIsModemExp || mLogToolStatus[i]) {
                        break;
                    }
                    if (i == mLogToolStatus.length - 1) {
                        //mUIHandler.sendEmptyMessage(SysUtils.EVENT_ALL_LOGTOOL_STOPED);
                        createDialog(SysUtils.DIALOG_ALL_LOGTOOL_STOPED);
                        return;
                    }
                }

                createDialog(SysUtils.DIALOG_INPUT);
                mUIHandler.sendEmptyMessageDelayed(
                                SysUtils.EVENT_CHECK_INPUTDIALOG_TIMEOUT,
                                2 * 60 * 1000); // check time out

                // playTipSound(TagLogService.this, mWarningSoundUri);
                break;
            case SysUtils.EVENT_CHECK_INPUTDIALOG_TIMEOUT:
                if (!mIsInputDialogClicked) {
                    Log.w(TAG, "time out");
//                    startOrStopAllLogTool(false);
//                    try {
//                        Thread.sleep(5 * 1000); // wait 5s 
//                    } catch (InterruptedException e) {
//
//                    }
//                    startOrStopAllLogTool(true);
                    mTagLogResult = SysUtils.BROADCAST_VAL_TAGLOG_CANCEL;
                    TagLogService.this.stopSelf();
                }
                break;
            case SysUtils.SD_LOCK_OF_SPACE:
                createDialog(SysUtils.DIALOG_LOCK_OF_SDSPACE);
                break;
            case SysUtils.SD_NOT_EXIST:
                dismissProgressDialog();
                createDialog(SysUtils.SD_NOT_EXIST);
                break;
            case SysUtils.SD_NOT_WRITABLE:
                dismissProgressDialog();
                createDialog(SysUtils.SD_NOT_WRITABLE);
                break;
            case SysUtils.EVENT_ALL_LOGTOOL_STOPED:
                createDialog(SysUtils.DIALOG_ALL_LOGTOOL_STOPED);
                break;
            case SysUtils.EVENT_ZIP_LOG_SUCCESS:
                dismissProgressDialog();
                mTagLogResult = SysUtils.BROADCAST_VAL_TAGLOG_SUCCESS;
                Toast.makeText(TagLogService.this, R.string.taglog_compress_done, Toast.LENGTH_SHORT).show();
                TagLogService.this.stopSelf();
                break;
            case SysUtils.EVENT_ZIP_LOG_FAIL:
                dismissProgressDialog();
                createDialog(SysUtils.DIALOG_ZIP_LOG_FAIL);
                break;
            case SysUtils.DIALOG_START_PROGRESS:
                createProgressDialog();
                break;
            case SysUtils.DIALOG_END_PROGRESS:
                dismissProgressDialog();
                break;
            }
            Log.d(TAG, " MyHandler handleMessage --> end " + msg.what);
        }
    }

    private class FunctionHandler extends Handler {

        public FunctionHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, " SubHandler handleMessage --> start " + msg.what);
            switch (msg.what) {
            case SysUtils.EVENT_ZIP_ALL_LOG:
                String SDCardPath = getSDCardPath();
                String[] logToolPath = getLogPath();
                File[] currentLogFolder = new File[SysUtils.LOG_QUANTITY];
                int SDCardSpaceStatus = SDCardUtils.checkSdCardSpace(
                        SDCardPath, logToolPath);
                if (SDCardSpaceStatus != SysUtils.SD_NORMAL) {
                    mUIHandler.sendEmptyMessage(SDCardSpaceStatus);
                    return;
                }

                mLogToolStatus = getLogToolStatus(); // get status of log tools

                startOrStopAllLogTool(false); // stop all log tool.

                for (int i = 0; i < mLogToolStatus.length; i++) {
                    if (mLogToolStatus[i]) {
                        currentLogFolder[i] = getCurrentLogFolder_new(logToolPath[i]);
                    }
                }
                startOrStopAllLogTool(true); // start all log tool

                String tagLogFolderName = SDCardUtils.createTagLogFolder(
                        SDCardPath, mTag);

                mLogPathInTagLog = zipAllLogAndDelete(currentLogFolder,
                        tagLogFolderName);

                // for test
                for (int i = 0; i < SysUtils.LOG_QUANTITY; i++) {
                    if (mLogPathInTagLog[i] == null) {
                        Log.d(TAG, "mLogPathInTagLog[" + i + "]" + "= null");
                    } else {
                        Log.d(TAG, "mLogPathInTagLog[" + i + "]" + "="
                                + mLogPathInTagLog[i]);
                    }
                }

                SDCardUtils.writeFolderToTagFolder(mDbPathFromAee,
                        tagLogFolderName);

                if (mIsModemExp) {
                    if (mIsModemLogReady) {
                        Log.d(TAG, "Modem Log is Ready");
                        zipLogAndDelete(mModemLogPath, tagLogFolderName);
                    } else {
                        try {
                            Log.d(TAG, "Modem Log is not Ready , wait 1 min");
                            // Thread.sleep(60 * 1000);
                            Thread.sleep(1 * 1000); // for test
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Catch InterruptedException");
                        }

                        if (mIsModemLogReady) {
                            zipLogAndDelete(mModemLogPath, tagLogFolderName);
                        } else {
                            Log.d(TAG, "Modem Log is not Ready , search modem log from SD card!");
                            mLogPathInTagLog[0] = zipLogAndDelete(
                                    getMdLogFromSDcard(logToolPath[0]),
                                    tagLogFolderName);
                        }
                    } 
                }

                boolean isZipSuccess = true;
                for (int i = 0; i < SysUtils.LOG_QUANTITY; i++) {
                    if (mLogToolStatus[i] && mLogPathInTagLog[i] == null) {
                        isZipSuccess = false;
                    }
                }
                if (isZipSuccess) {
                    mUIHandler.sendEmptyMessage(SysUtils.EVENT_ZIP_LOG_SUCCESS);
                } else {
                    mUIHandler.sendEmptyMessage(SysUtils.EVENT_ZIP_LOG_FAIL);
                }
                break;
            }
            Log.d(TAG, " SubHandler handleMessage --> end " + msg.what);
        }
    }

    /**
     * 
     * @return Status of MDLog/MobileLog/NetLog
     */

    private boolean[] getLogToolStatus() {
        Log.i(TAG, "Check log tool status");
        boolean toolStatus[] = new boolean[SysUtils.LOG_QUANTITY];
        String usermode = SystemProperties.get(SysUtils.PROP_MD);
        if (null != usermode && usermode.equalsIgnoreCase(SysUtils.PROP_ON)
                && !mIsModemExp) {
            Log.i(TAG, "getLogToolStatus: ModemLog is running");
            toolStatus[0] = true;
        }
        usermode = SystemProperties.get(SysUtils.PROP_MOBILE);
        if (null != usermode && usermode.equalsIgnoreCase(SysUtils.PROP_ON)) {
            Log.i(TAG, "getLogToolStatus: MobileLog is running");
            toolStatus[1] = true;
        }

        usermode = SystemProperties.get(SysUtils.PROP_NETWORK);
        if (null != usermode && usermode.equalsIgnoreCase(SysUtils.PROP_ON)) {
            Log.i(TAG, "getLogToolStatus: NetLog is running");
            toolStatus[2] = true;
        }

        // usermode = SystemProperties.get(SysUtils.PROP_EXTMD);
        // if (null != usermode && usermode.equalsIgnoreCase(SysUtils.PROP_ON))
        // {
        // Log.i(TAG, "getLogToolStatus: ExtModemLog is running");
        // toolStatus[3] = true;
        // }
        return toolStatus;
    }

    /**
     * 
     * @return "/mnt/sdcard" or "/mnt/sdcard2"
     */

    private String getSDCardPath() {
        String usermode = SystemProperties.get(SysUtils.PER_LOG2SD);
        if (usermode.equals(SysUtils.EXTERNALSDPATH)) {
            return SysUtils.EXTERNALSDPATH;
        } else {
            File sdCardFile = Environment.getExternalStorageDirectory();
            String sdCardState = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(sdCardState)
                    && !Environment.MEDIA_SHARED.equals(sdCardState)) {
                Log.e(TAG, "SD card not ready, current state=" + sdCardState);
                return null;
            }
            return sdCardFile.getAbsolutePath();
        }
    }

    /**
     * 
     * @return Path of MDLog/MobileLog/NetLog
     */
    private String[] getLogPath() {
        String[] logToolPath = new String[SysUtils.LOG_QUANTITY];
        String SDCardPath = getSDCardPath();
        if (SDCardPath == null) {
            return null;
        }
        for (int i = 0; i < logToolPath.length; i++) {
            logToolPath[i] = SDCardPath + File.separator + LOGTOOLFOLDER[i];
        }
        return logToolPath;
    }

    /**
     * 
     * @param isStart
     *            : true? start log tool:stop log tool
     * @param index
     *            : which log tool
     */
    private void startOrStopLogTool(boolean isStart, int index) {
        Intent intent = new Intent(SysUtils.BROADCAST_ACTION);
        intent.putExtra(SysUtils.BROADCAST_KEY_SELFTEST,
                SysUtils.BROADCAST_VAL_SELFTEST);
        intent.putExtra(SysUtils.BROADCAST_KEY_SRC_FROM,
                SysUtils.BROADCAST_VAL_SRC_HQ);

        if (isStart) {
            intent.putExtra(SysUtils.BROADCAST_KEY_COMMAND,
                    SysUtils.BROADCAST_VAL_COMMAND_START);
        } else {
            intent.putExtra(SysUtils.BROADCAST_KEY_COMMAND,
                    SysUtils.BROADCAST_VAL_COMMAND_STOP);
        }

        switch (index) {
        case 0: // modem log
            intent.putExtra(SysUtils.BROADCAST_KEY_SRC_TO,
                    SysUtils.BROADCAST_VAL_SRC_MD);
            break;
        case 1: // mobile log
            intent.putExtra(SysUtils.BROADCAST_KEY_SRC_TO,
                    SysUtils.BROADCAST_VAL_SRC_MOBILE);
            break;
        case 2: // net log
            intent.putExtra(SysUtils.BROADCAST_KEY_SRC_TO,
                    SysUtils.BROADCAST_VAL_SRC_NETWORK);
            break;
        case 3: // extmodem log
            intent.putExtra(SysUtils.BROADCAST_KEY_SRC_TO,
                    SysUtils.BROADCAST_VAL_SRC_EXTMD);
            break;
        default:
            break;
        }
        sendBroadcast(intent);
    }

    private void startOrStopAllLogTool(boolean isStart) {

        if (mLogToolStatus != null) {
            for (int i = 0; i < mLogToolStatus.length; i++) {
                if (mLogToolStatus[i]) {
                    startOrStopLogTool(isStart, i);
                }
            }
        }
    }

    private void startAllLogTool() {
        for (int i = 0; i < SysUtils.LOG_QUANTITY; i++) {
            startOrStopLogTool(true, i);
        }
    }

    private void createProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(TagLogService.this);
            if (null != mProgressDialog) {
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setTitle(R.string.taglog_title);
                mProgressDialog.setMessage(getResources().getText(
                        R.string.taglog_msg_compress_log));

//                mProgressDialog.setCancelable(false);
                Window win = mProgressDialog.getWindow();
                win.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mProgressDialog.show();
            } else {
                Log.e(TAG, "new mProgressDialog failed");
            }
        }
    }

    private void dismissProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void createDialog(int id) {
        Builder builder = new AlertDialog.Builder(TagLogService.this);
        switch (id) {
        case SysUtils.DIALOG_INPUT: // input tag
            mIsInputDialogClicked = false;
            final EditText inputText = new EditText(TagLogService.this);
            inputText.setKeyListener(new NumberKeyListener() {

                public int getInputType() {
                    return InputType.TYPE_TEXT_FLAG_CAP_WORDS;
                }

                @Override
                protected char[] getAcceptedChars() {
                    char[] numberChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
                            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0',
                            '1', '2', '3', '4', '5', '6', '7', '8', '9', '_',
                            ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                            'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
                    return numberChars;
                }
            });
            builder.setTitle(R.string.taglog_title).setMessage(
                    R.string.taglog_msg_input).setView(inputText)
                    .setPositiveButton(android.R.string.ok,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    // inputText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    InputMethodManager inputManager = (InputMethodManager) getBaseContext()
                                            .getSystemService(
                                                    Context.INPUT_METHOD_SERVICE);
                                    inputManager.hideSoftInputFromWindow(
                                            inputText.getWindowToken(), 0);
                                    mIsInputDialogClicked = true;

                                    mTag = inputText.getText().toString()
                                            .trim();
                                    if (mTag.equals("")) {
                                        mTag = "taglog";
                                    }
                                    Log.i(TAG, "Input tag: " + mTag);
                                    //Toast.makeText(TagLogService.this, "", Toast.LENGTH_SHORT).show();
                                    mUIHandler
                                            .sendEmptyMessage(SysUtils.DIALOG_START_PROGRESS);
                                    mFunctionHandler
                                            .sendEmptyMessage(SysUtils.EVENT_ZIP_ALL_LOG);
                                }
                            }).setNegativeButton(android.R.string.cancel,
                            new OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    // inputText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    mIsInputDialogClicked = true;

                                    dialog.dismiss();
                                    // startOrStopAllLogTool(false);
                                    // startOrStopAllLogTool(true);
                                    mTagLogResult = SysUtils.BROADCAST_VAL_TAGLOG_CANCEL;
                                    TagLogService.this.stopSelf();
                                }
                            });
            break;
        case SysUtils.DIALOG_ALL_LOGTOOL_STOPED: // all log tool are stopped
            builder.setTitle(R.string.taglog_title).setMessage(
                    R.string.taglog_msg_logtool_stopped).setPositiveButton(
                    android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startAllLogTool();
                            TagLogService.this.stopSelf();
                        }
                    }).setNegativeButton(android.R.string.cancel,
                    new OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            TagLogService.this.stopSelf();
                        }
                    });
            break;
        case SysUtils.DIALOG_LOCK_OF_SDSPACE:
            builder.setTitle(R.string.taglog_title_warning).setMessage(
                    R.string.taglog_msg_no_space).setPositiveButton(
                    android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mTagLogResult = SysUtils.BROADCAST_VAL_TAGLOG_FAILED;
                            TagLogService.this.stopSelf();
                        }
                    });
            break;
        case SysUtils.DIALOG_ZIP_LOG_FAIL:
            builder.setTitle(R.string.taglog_title_warning).setMessage(
                    R.string.taglog_msg_zip_log_fail).setPositiveButton(
                    android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startOrStopAllLogTool(true);
                            dialog.dismiss();
                            mTagLogResult = SysUtils.BROADCAST_VAL_TAGLOG_FAILED;
                            TagLogService.this.stopSelf();
                        }
                    });
            break;
        case SysUtils.SD_NOT_EXIST:
            builder.setTitle(R.string.taglog_title_warning).setMessage(
                    R.string.taglog_msg_no_sdcard).setPositiveButton(
                    android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            TagLogService.this.stopSelf();
                        }
                    });
            break;
        case SysUtils.SD_NOT_WRITABLE:
            builder.setTitle(R.string.taglog_title_warning).setMessage(
                    R.string.taglog_msg_sdcard_not_writtable)
                    .setPositiveButton(android.R.string.ok,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                    TagLogService.this.stopSelf();
                                }
                            });
            break;
        default:
            break;
        }
        mDialog = builder.create();
        mDialog.setCancelable(false);
        Window win = mDialog.getWindow();
        win.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mDialog.show();
    }

    /**
     * 
     * @param currentLogFolder
     *            : The current using log folder
     * @param tagLogFolderName
     *            : Then current using tag log folder
     * @return
     */
    private String[] zipAllLogAndDelete(File[] currentLogFolder,
            String tagLogFolderName) {
        String[] currentTaglogPaths = new String[SysUtils.LOG_QUANTITY];
        for (int i = 0; i < SysUtils.LOG_QUANTITY; i++) {
            if (mLogToolStatus[i] && null != currentLogFolder[i]) {
                String currentTaglogPath = zipLogAndDelete(currentLogFolder[i],
                        tagLogFolderName);
                if (null == currentTaglogPath) {
                    currentTaglogPaths[i] = null;
                } else {
                    currentTaglogPaths[i] = currentTaglogPath;
                    Log.i(TAG, "currentTaglogPaths[" + i + "]" + "="
                            + currentTaglogPath);
                }
            } else {
                currentTaglogPaths[i] = null;
            }
        }
        return currentTaglogPaths;
    }

    /**
     * Compress log, put the zip file in SD card
     * 
     * @return Compressed log file absolute path
     */
    private String zipLogAndDelete(String LogFolderPath, String tagLogFolderName) {
        Log.d(TAG, "-->zipLog()");
        String zipResultPath = null;
        if (null == LogFolderPath) {
            Log.e(TAG, "LogFolderPath is null!");
            return null;
        }

        File LogFolder = new File(LogFolderPath);
        if (!LogFolder.exists()) {
            return null;
        }
        File[] logFolderList = LogFolder.listFiles();
        if (logFolderList == null || logFolderList.length == 0) {
            Log.e(TAG, "Found no detail log in log folder");
            return null;
        }

        File neededLogFolder = getCurrentLogFolder_new(logFolderList);
        if (neededLogFolder != null) {
            String targetLogFileName = tagLogFolderName + File.separator
                    + neededLogFolder.getName() + SysUtils.ZIP_LOG_SUFFIX;
            Log.i(TAG, "targetLogFileName :" + targetLogFileName);
            boolean zipResult = ZipManager.zipFileOrFolder(neededLogFolder
                    .getAbsolutePath(), targetLogFileName);
            if (!zipResult) {
                Log.e(TAG, "Fail to zip log folder: "
                        + neededLogFolder.getAbsolutePath());
                return null;
            } else {
                Log.i(TAG, "Zip log success, target log file="
                        + targetLogFileName);
                zipResultPath = targetLogFileName;

                // -- for change 1
                // SDCardUtils.deleteFolder(neededLogFolder); // delete
            }
        } else {
            Log.e(TAG, "Fail to get needed log folder");
        }

        Log.d(TAG, "<--zipLog(), zipResultPath=" + zipResultPath);
        return zipResultPath;
    }

    /**
     * Compress log, put the zip file in SD card
     * 
     * @return Compressed log file absolute path
     */
    private String zipLogAndDelete(File neededLogFolder, String tagLogFolderName) {
        Log.d(TAG, "-->zipLog()");
        String zipResultPath = null;

        if (neededLogFolder != null) {
            String targetLogFileName = tagLogFolderName + File.separator
                    + neededLogFolder.getName() + SysUtils.ZIP_LOG_SUFFIX;
            Log.i(TAG, "targetLogFileName :" + targetLogFileName);
            boolean zipResult = ZipManager.zipFileOrFolder(neededLogFolder
                    .getAbsolutePath(), targetLogFileName);
            if (!zipResult) {
                Log.e(TAG, "Fail to zip log folder: "
                        + neededLogFolder.getAbsolutePath());
                return null;
            } else {
                Log.i(TAG, "Zip log success, target log file="
                        + targetLogFileName);
                zipResultPath = targetLogFileName;

                SDCardUtils.deleteFolder(neededLogFolder); // delete primary log folder
            }
        } else {
            Log.e(TAG, "Needed log folder path is null!!");
        }

        Log.d(TAG, "<--zipLog(), zipResultPath=" + zipResultPath);
        return zipResultPath;
    }

    /**
     * Get the current using log folder, by lastModified time Take the latest
     * folder modified in the past 10 minute
     * 
     * @param logFolderList
     * @return return needed folder, if none found, return null
     */
    private File getCurrentLogFolder_new(File[] logFolderList) {
        Log.d(TAG, "-->getCurrentLogFolder()");
        long timeAway = 10 * 60 * 1000;
        File neededFile = null;
        long nowTime = System.currentTimeMillis();
        Log.i(TAG, "Current time=" + ZipManager.translateTime(nowTime));
        for (File file : logFolderList) {
            long modifiedTime = getFolderLastModifyTime(file);
            Log.i(TAG, "Loop log folder:  name=" + file.getName()
                    + ", modified time="
                    + ZipManager.translateTime(modifiedTime));
            if (Math.abs(nowTime - modifiedTime) < timeAway) {
                timeAway = Math.abs(nowTime - modifiedTime);
                neededFile = file;
            }
        }
        if (neededFile != null) {
            Log.i(TAG, "Selected log folder name=[" + neededFile.getName());
        } else {
            Log.e(TAG, "Could not get needed log folder.");
            // ---
            long temp = 0;
            for (File file : logFolderList) {
                long modifiedTime = file.lastModified();
                if (modifiedTime > temp && file.isDirectory()) {
                    temp = modifiedTime;
                    neededFile = file;
                }
            }
            
            if (neededFile != null) {
                Log.i(TAG, "Selected log folder name=[" + neededFile.getName()
                        + "], last modified time="
                        + ZipManager.translateTime(neededFile.lastModified()));
            } else {
                Log.e(TAG, "There is no folder");
            }
            // --
        }
        Log.d(TAG, "<--getCurrentLogFolder()");
        return neededFile;
    }

    /**
     * Get the current using log folder, by lastModified time Take the latest
     * folder modified in the past 10 minute
     * 
     * @param FolderPath
     * @return return needed folder, if none found, return null
     */
    private File getCurrentLogFolder_new(String folderPath) {
        Log.d(TAG, "-->get currentLog folder in " + folderPath);

        if (folderPath == null) {
            return null;
        }
        File logFolder = new File(folderPath);
        if (!logFolder.exists()) {
            Log.e(TAG, "getCurrentLogFolder() the folder isn't exist!");
            return null;
        }
        File[] logFolderList = logFolder.listFiles();
        if (logFolderList.length <= 0) {
            Log.e(TAG, "There is no folder in " + folderPath);
            return null;
        }
        
        File neededFile = null;
        
        long timeAway = 10 * 60 * 1000;
        long nowTime = System.currentTimeMillis();
        Log.i(TAG, "Current time=" + ZipManager.translateTime(nowTime));
        for (File file : logFolderList) {
            long modifiedTime = getFolderLastModifyTime(file);
            Log.i(TAG, "Loop log folder:  name=" + file.getName()
                    + ", modified time="
                    + ZipManager.translateTime(modifiedTime));
            if (Math.abs(nowTime - modifiedTime) < timeAway) {
                timeAway = Math.abs(nowTime - modifiedTime);
                neededFile = file;
            }
        }
        if (neededFile != null) {
            Log.i(TAG, "Selected log folder name=[" + neededFile.getName());
        } else {
            Log.e(TAG, "Could not get needed log folder.");
            // ---
            long temp = 0;
            for (File file : logFolderList) {
                long modifiedTime = file.lastModified();
                if (modifiedTime > temp && file.isDirectory()) {
                    temp = modifiedTime;
                    neededFile = file;
                }
            }
            
            if (neededFile != null) {
                Log.i(TAG, "Selected log folder name=[" + neededFile.getName()
                        + "], last modified time="
                        + ZipManager.translateTime(neededFile.lastModified()));
            } else {
                Log.e(TAG, "There is no folder in " + folderPath);
            }
            // --
        }
        Log.d(TAG, "<--getCurrentLogFolder()");
        return neededFile;
    }

    /**
     * Since folder's last modify time seems can not work very well in Linux, we
     * will loop each file in it to get the accurate time. As file may be
     * modified all the time, 10 second inaccuracy in the future will be
     * allowed. TODO Attention: This method may not work well if user modify
     * system time, since file modified in future will be ignored
     * 
     * @return 0 if no valid file
     */
    private long getFolderLastModifyTime(File file) {
        Log.v(TAG, "-->getFolderLastModifyTime(), path="
                + (file == null ? "NUll" : file.getAbsoluteFile()));
        long result = 0;
        if (file == null || !file.exists()) {
            Log.d(TAG, "Given file not exist.");
            return result;
        }
        long currentTime = System.currentTimeMillis();
        if (file.isFile()) {
            Log.w(TAG, "You should give me a folder. But still can work here.");
            long time = file.lastModified();
            Log.v(TAG, file.getAbsolutePath() + " modified at "
                    + ZipManager.translateTime(time));
            if (currentTime - time > -10 * 1000) {// past or 10s in future
                result = time;
            }
        } else {
            File[] fileList = file.listFiles();
            for (File subFile : fileList) {
                long time = 0;
                if (subFile.isFile()) {
                    Log.v(TAG, subFile.getAbsolutePath() + " modified at "
                            + ZipManager.translateTime(currentTime));
                    time = subFile.lastModified();
                    if (currentTime - time < -10 * 1000) {// file in future
                        time = 0;
                    }
                } else {
                    time = getFolderLastModifyTime(subFile);
                }
                if (Math.abs(time - currentTime) < Math.abs(result
                        - currentTime)) {
                    result = time;
                }
            }
        }
        Log.v(TAG, "<--getFolderLastModifyTime(), time="
                + ZipManager.translateTime(result));
        return result;
    }

    private long getFolderLastModifyTime2(File file) {
        Log.v(TAG, "-->getFolderLastModifyTime(), path="
                + (file == null ? "NUll" : file.getAbsoluteFile()));
        long result = 0;
        if (file == null || !file.exists()) {
            Log.d(TAG, "Given file not exist.");
            return result;
        }
        long currentTime = System.currentTimeMillis();
        if (file.isFile()) {
            Log.w(TAG, "You should give me a folder. But still can work here.");
            long time = file.lastModified();
            Log.v(TAG, file.getAbsolutePath() + " modified at "
                    + ZipManager.translateTime(time));
            if (currentTime - time > -10 * 1000) {// past or 10s in future
                result = time;
            }
        } else {
            File[] fileList = file.listFiles();
            for (File subFile : fileList) {
                long time = 0;
                if (subFile.isFile()) {
                    Log.v(TAG, subFile.getAbsolutePath() + " modified at "
                            + ZipManager.translateTime(currentTime));
                    time = subFile.lastModified();
                    if (currentTime - time < -10 * 1000) {// file in future
                        time = 0;
                    }
                } else {
                    time = getFolderLastModifyTime(subFile);
                }
                if (Math.abs(time - currentTime) < Math.abs(result
                        - currentTime)) {
                    result = time;
                }
            }
        }
        Log.v(TAG, "<--getFolderLastModifyTime(), time="
                + ZipManager.translateTime(result));
        return result;
    }

    private void clearRedScreen() {
        Log.d(TAG, "--> Clear Red Screen");
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("aee -c dal");
        } catch (IOException e) {
            Log.e(TAG, "--> Clear Red Screen catch IOException");
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.e(TAG, "--> Clear Red Screen catch SecurityException");
            e.printStackTrace();
        }
    }

    /**
     * Play tip sound
     * 
     * @param context
     * @param defaultUri
     */
    private void playTipSound(Context context, Uri defaultUri) {
        Log.d(TAG, "play sound tip.");
        if (defaultUri != null) {
            Ringtone mRingtone = RingtoneManager.getRingtone(context,
                    defaultUri);
            if (mRingtone != null) {
                mRingtone.play();
            }
        }
    }

    private boolean mIsFirstReceive = true;

    class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SysUtils.ACTION_MDLOG_READY == intent.getAction()) {
                Log.d(TAG, "InternalReceiver --> "
                        + SysUtils.ACTION_MDLOG_READY);
                if (!mIsFirstReceive) {
                    Log.d(TAG, "This broadcast have received!");
                    return;
                } else {
                    mIsFirstReceive = false;
                }
                mIsModemLogReady = true;
                mModemLogPath = intent
                        .getStringExtra(SysUtils.BROADCAST_KEY_MDEXP_LOGPATH);
                if (mModemLogPath == null) {
                    Log.e(TAG, " mModemLogPath is null");
                } else {
                    Log.d(TAG, " mModemLogPath is: " + mModemLogPath);
                }
            }
        }
    }

    private File getMdLogFromSDcard(String modemLogPath) {

        Log.d(TAG, "-->getMdLogFromSDcard()");
        File modemLogFolder = new File(modemLogPath);
        if (!modemLogFolder.exists()) {
            Log.e(TAG, "getMdLogFromSDcard() the folder isn't exist!");
            return null;
        }
        File[] logFolderList = modemLogFolder.listFiles();
        long timeAway = 10 * 60 * 1000;
        File neededFile = null;
        long nowTime = System.currentTimeMillis();
        Log.i(TAG, "Current time=" + ZipManager.translateTime(nowTime));
        for (File file : logFolderList) {
            if (file.getName().contains("EE")) {
                long modifiedTime = getFolderLastModifyTime(file);
                Log.i(TAG, "Loop log folder:  name=" + file.getName()
                        + ", modified time="
                        + ZipManager.translateTime(modifiedTime));
                if (Math.abs(nowTime - modifiedTime) < timeAway) {
                    timeAway = Math.abs(nowTime - modifiedTime);
                    neededFile = file;
                }
            }
        }
        if (neededFile != null) {
            Log.i(TAG, "Selected modem log folder name=["
                    + neededFile.getName());
            checkMDLogDumpDone(neededFile);
        } else {
            Log.e(TAG, "Could not get needed log folder.");
            long temp = 0;
            for (File file : logFolderList) {
                long modifiedTime = file.lastModified();
                if (modifiedTime > temp && file.isDirectory()) {
                    temp = modifiedTime;
                    neededFile = file;
                }
            }
            if (neededFile != null) {
                Log.e(TAG, "Selected log folder name=[" + neededFile.getName()
                        + "], last modified time="
                        + ZipManager.translateTime(neededFile.lastModified()));
            } else {
                Log.e(TAG, "There is no folder");
            }
        }
        Log.d(TAG, "<--getMdLogFromSDcard()");
        return neededFile;
    }

    private void checkMDLogDumpDone(File modemLogFile) {
        File[] modemLogFileList = modemLogFile.listFiles();
        if (modemLogFileList == null) {
            return;
        }
        File memoryLogFile = null;
        long frontTime = System.currentTimeMillis();
        while (true) {
            for (File file : modemLogFileList) {
                if (file.getName().startsWith("Memory")) {
                    memoryLogFile = file;
                    break;
                }
            }
            if (memoryLogFile == null) {
                Log.e(TAG, "memoryLogFile don't exit!");
                break;
            }
            String memoryLogFileName = memoryLogFile.getName();
            if (memoryLogFileName.endsWith(".bin.bin")) {
                Log.i(TAG, "memoryLogFile end with .bin.bin");
                long lastTime = System.currentTimeMillis();
                if (lastTime - frontTime >= 10 * 60 * 1000) {
                    Log.e(TAG, "Modem log dump time more than 10 min, please check it is normal! ");
                    break;
                }
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Catch InterruptedException");
                }
            } else if (memoryLogFileName.endsWith(".bin")) {
                Log.d(TAG, "Modem Log Dump done: " + memoryLogFileName);
                break;
            } else {
                Log.e(TAG, "memoryLogFile don't end with .bin!");
                break;
            }
        }
    }
}
