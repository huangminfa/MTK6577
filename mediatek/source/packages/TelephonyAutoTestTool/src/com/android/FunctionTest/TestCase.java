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

package com.android.FunctionTest;

import java.util.TimeZone;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.android.DataServiceTestCases.DataServiceUtil;
import com.android.FuncitonTester.FunctionTesterActivity;

abstract public class TestCase {
    static final String TAG = "TestCase";
    //static protected ResultCallback mResultHandlerAcvivity = null;
    protected static Context AppContext = FunctionTesterActivity.getCurrentContext();
	static protected TelephonyManager mTelephonyManager = (TelephonyManager) AppContext.getSystemService(Context.TELEPHONY_SERVICE);	
	protected boolean mAborted = false;
	public boolean mSelected = false;
    public boolean mPassed = true;
	
    static private Handler mReportHandler;
    
   
    public static class ReportMessage {
        static final private boolean REPORT_TIMESTAMP = true;
        static final public int REPORT_MESSAGE = 0;
        static final long TIMEZONE_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis ());
        
        private String mMsgString;

        public ReportMessage(String reportString) {
            mMsgString = reportString;
        }
        
        static private String timestamp() {
            final long timeInMillis = System.currentTimeMillis() + TIMEZONE_OFFSET;
            long remdr = timeInMillis;
            final int ms = (int)( remdr % 1000 );
            remdr /= 1000;
            final int seconds = (int)( remdr % 60 );
            remdr /= 60;
            final int minutes = (int)( remdr % 60 );
            remdr /= 60;
            final int hours = (int)( remdr % 24 );
            //final int days = (int)( remdr / 24 );
            return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, ms);
        }
        
        public String getReportString() {
            if (REPORT_TIMESTAMP) {
                return timestamp() + " "+ mMsgString;
            } else {
                return mMsgString;
            }
        }
    }
    
	public TestCase(Handler reportHander) {
        if (mReportHandler == null ||
                mReportHandler != reportHander) {
            mReportHandler = reportHander;
		}
	}
	
	static public void sendMessage(CharSequence message) {

        if (mReportHandler != null) {
            mReportHandler.obtainMessage(ReportMessage.REPORT_MESSAGE, 
                    new ReportMessage(message.toString()).getReportString()).sendToTarget();
        }
    }
	
	public void reportMessage(CharSequence message) {
        if (message == null) {
            return;
        }

        String testCaseName = getClass().getName();
        // Log.d(TAG, "reportMessage:" + message.toString() + " mReportHandler="
        // + mReportHandler);
	    StringBuilder outMessage = new StringBuilder();
        outMessage.append(testCaseName.substring(testCaseName.lastIndexOf(".") + 1,
                testCaseName.length()));
        outMessage.append(" ");
	    outMessage.append(message.toString());
        sendMessage(outMessage.toString());
	}
	
	public void abortTest() {
		DataServiceUtil.abortTest();
		reportMessage("abortTest.");
		mAborted = true;
	}

	public void Sleep(int mTime) {
		try {
			Thread.sleep(mTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setSelected(boolean selected) {
	    mSelected = selected;
	}
	
	public boolean getSelected() {
        return mSelected;
    }

    public void runTestCase() {
        mAborted = false;
        try {
            run();
        } catch (Throwable t) {
            reportMessage(t.getMessage());
            reportMessage("Failed");
            mAborted = true;
            return;
        }
        reportMessage("Passed");
    }

	abstract public void setup();
    abstract public void run();
	abstract public void tearDown();
	abstract public String getDescription();
}
