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

package com.android.DataServiceTestCases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Ping extends Thread {
	final static String TAG = "DataServiceTester::Ping";
	
	public static final int PING_RESULT = 1;
    public static final int DEFAULT_PING_TIMES = 15;
	public static final String DEFAULT_PING_HOST = "www.mediatek.com";
	public static final String DEFAULT_PING_HOST1 = "www.google.com";
	
	Handler mHandler;
	int mRepeatTimes; 
	String mHost;
	
	public Ping (Handler handler, int repeat, String host) {
		super();
		mHandler = handler;
		mRepeatTimes = repeat; 
		mHost = host;
	}
	
	public Ping () {
		super();
		mHandler = null;
		mRepeatTimes = 0; 
		mHost = null;
	}
	
    static public boolean doPing(String host) {
        return doPing(DEFAULT_PING_TIMES, host);
    }

	static public boolean doPing() {
	    return doPing(DEFAULT_PING_TIMES, DEFAULT_PING_HOST);
	}
	 
    static public boolean doPing(int repeat, String host) {
        boolean passed = false;
		Process process = null;  
		try {  
			process = new ProcessBuilder()  
			.command("ping", host)  
			.redirectErrorStream(true)  
			.start();  
			
			Log.v("DataServiceTester", "command: ping " + host);
			
			InputStream in = process.getInputStream();  
			BufferedReader br = new BufferedReader(new InputStreamReader(in));  
			String s = null;  
            while((s = br.readLine())!= null && repeat-- > 0)  
            {  
                Log.v("DataServiceTester", "command: " + s);
                if (s.contains("icmp_seq=")) {
                    passed = true;
                    break;
                }
            }  
		}  
        catch(IOException e)  
        {  
            e.printStackTrace(System.out);  
            System.err.println("Failed to create process.");  
            System.exit(1);  
        }  
        finally {  
            process.destroy();
        }
        return passed;
	}

	
	public void run() {  
		runDoPing();
	}
	
	private void runDoPing()  
    {  
		Process process=null;  
		try {  
			process = new ProcessBuilder()  
			.command("ping", mHost)  
			.redirectErrorStream(true)  
			.start();  
			
			InputStream in = process.getInputStream();  
			BufferedReader br = new BufferedReader(new InputStreamReader(in));  
			String s = null;  
            while((s = br.readLine())!= null && mRepeatTimes-- > 0)  
            {  
            	Log.v(TAG, s);
            	Message msg = mHandler.obtainMessage(PING_RESULT, s);    
            	msg.sendToTarget();
            }  
		}  
        catch(IOException e)  
        {  
            e.printStackTrace(System.out);  
            System.err.println("Failed to create process.");  
            System.exit(1);  
        }  
        finally {  
         process.destroy();  
       }  
     } 
}
