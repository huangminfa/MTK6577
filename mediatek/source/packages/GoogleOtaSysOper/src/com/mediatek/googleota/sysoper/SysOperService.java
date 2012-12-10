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

package com.mediatek.googleota.sysoper;
import java.io.File;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class SysOperService extends Service {
    public static final int MSG_REBOOT_TARGET      = 1;
    public static final int MSG_CREATE_COMMANDFILE = 2;
    public static final int MSG_DELETE_COMMANDFILE = 3;
    public static final String TAG = "GoogleOta";

    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    @Override
    public void onCreate() {
        super.onCreate();
    	Log.i(TAG,"SysOperService:onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    	Log.i(TAG,"SysOperService:onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.i(TAG,"SysOperService:onBind");
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG,"SysOperService:handleMessage, msg.what="+msg.what);
            switch (msg.what) {
            case MSG_REBOOT_TARGET:
                break;
            case MSG_CREATE_COMMANDFILE:
                break;
            case MSG_DELETE_COMMANDFILE:
                Bundle data = msg.getData();
                if (data != null) {
                    String commandfile = data.getString("COMMANDFILE");
                    Log.i(TAG,"SysOperService:handleMessage, msg.data.commandfile="+commandfile);
                    deleteCommandFile(commandfile);
                }
                break;
            default:
                super.handleMessage(msg);
            }
        SysOperService.this.stopSelf();
        }
    }

    private void deleteCommandFile(String path){
        Log.i(TAG, "SysOperService:deleteCommandFile");
        if (path == null) {
            Log.i(TAG, "SysOperService:deleteCommandFile, program error, need debug");
            return;
        }
        File file = new File(path);
        if (file == null)
        {
            return;
        }
        if (!file.exists()) {
            Log.i(TAG, "SysOperService:deleteCommandFile, command does not exist");
    	} else {
            Log.i(TAG, "SysOperService:deleteCommandFile, command exist, delete it");
            file.delete();
        }
    }
}
