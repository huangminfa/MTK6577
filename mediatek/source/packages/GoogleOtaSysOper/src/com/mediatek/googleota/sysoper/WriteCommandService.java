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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WriteCommandService extends Service {
    public static final String COMMAND_PATH = "/cache/recovery";
    public static final String COMMAND_FILE = "/cache/recovery/command";
    public static final String COMMAND_PART1 = "--update_package=";
    public static final String COMMAND_PART2 = "COMMANDPART2";

    public static final String TAG = "GoogleOta";
    
    private String commandLine = COMMAND_PART1;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "WriteCommandService:onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "WriteCommandService:onDestroy");
    }

    public int onStartCommand(Intent intent, int flags, int startId) 
    {
    	Log.i(TAG, "WriteCommandService:onStartCommand");
    	String part2 = intent.getStringExtra(COMMAND_PART2);
    	commandLine = commandLine + part2;
        Log.i(TAG, "WriteCommandService:onStartCommand. commandLine="+commandLine);
    	addCommandFile();
        return START_STICKY;  
    }
    
    private void addCommandFile() {
    	Log.i(TAG, "WriteCommandService:addCommandFile");
    	OutputStream commandfile = null;
    	try {
    		File recovery = new File(COMMAND_PATH);
    		if (!recovery.exists()) {
    		    boolean cr = recovery.mkdirs();
    		    Log.i(TAG, "WriteCommandService:addCommandFile, recovey not exist, try to create result ="+cr);
    		}
    		File file = new File(COMMAND_FILE);
    		if (file.exists()) {
    			file.delete();
    			Log.i(TAG, "WriteCommandService:addCommandFile, delete existed command file");
    		}
    		file.createNewFile();
    	    commandfile = new BufferedOutputStream(new FileOutputStream(file));
    	    commandfile.write(commandLine.getBytes());
    	    Log.i(TAG, "WriteCommandService:addCommandFile, command.getBytes() = "+commandLine.getBytes());
    	} catch (Exception e) {
    		Log.i(TAG, "WriteCommandService:addCommandFile, create command file error");
    		e.printStackTrace();
    	}
   	    finally {
	    	if (commandfile != null) {
	    		try {
	    		    commandfile.close();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
                stopSelf();
	    }
    }
}
