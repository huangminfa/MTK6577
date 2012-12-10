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

package com.android.FuncitonTester;

import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.CallControlTestCases.CallControlUtil;
import com.android.DataServiceTestCases.DataServiceUtil;
import com.android.DataServiceTestCases.Ping;
import com.android.FuncitonTester.R;
import com.android.FunctionTest.TestCase;
import com.android.FunctionTest.TestCaseFactory;
import com.android.FunctionTest.TestRunner;
import com.android.NetworkTestCases.NetworkUtil;

public class FunctionTesterActivity extends Activity {
    /** Called when the activity is first created. */
	static final String TAG = "DataServiceTester";
	
	Button mBtnRun;
	Button mBtnClear;
	TextView mTextOutput;
	ScrollView mScrollView;
	HandlerThread mTestThread;
	static public TestRunner mTestRunner;
	
	Ping mPinger;
	
	static public Context mContext;
	
	static long now = System.currentTimeMillis (); 
	static final long timezoneOffset = TimeZone.getDefault().getOffset(now);
	public boolean mStarted = false;
	
    static public Context getCurrentContext() {
    	return mContext;
    }
    
    // Test cases registered here.
    // The test case class should extends TestCase 
    // and provide a default constructor.
    static final public String[] TestCaseNames = new String[] {
            "com.android.PublicApiTestCase.TelephonyInfoTest",
            "com.android.NetworkTestCases.AirplaneModeTest",
            "com.android.CallControlTestCases.MoCallTest",
            "com.android.CallControlTestCases.MTCallTest",
            "com.android.SimCardTest.SimAdnTest",
            "com.android.DataServiceTestCases.PingTest",
            "com.android.DataServiceTestCases.DataOnOffTest",
            "com.android.DataServiceTestCases.NetworkModeTest",
            "com.android.DataServiceTestCases.Sim3GSwitchTest",
            "com.android.DataServiceTestCases.MultiplePdpTest",
            "com.android.DataServiceTestCases.WifiSwitchTest",
            "com.android.GeminiDataServiceTestCases.GeminiDataSimSwitchTest",
            "com.android.GeminiDataServiceTestCases.GeminiMultiplePdpTest"
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        mBtnRun = (Button) findViewById(R.id.buttonRun);
        mBtnClear = (Button) findViewById(R.id.buttonClear);
        mTextOutput = (TextView) findViewById(R.id.textView1);
        mBtnRun.setOnClickListener(listener_run);
        mBtnRun.setText("Start");
        mBtnClear.setOnClickListener(listener_run);
        mBtnClear.setText("Clear");
        
        mScrollView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        mTestThread = new HandlerThread("mTestThread");
        mTestThread.start();
        mTestRunner = new TestRunner(mTestThread.getLooper(), mResultHandler);
        
        DataServiceUtil.Init();
        CallControlUtil.Init();
        NetworkUtil.Init();
    }
   
    public void onDestory() {
        mTestRunner.stopTest();
    	DataServiceUtil.Deinit();
        CallControlUtil.Deinit();
        NetworkUtil.Deinit();
    	mTestThread.quit();
    }
    
    Button.OnClickListener listener_run = new OnClickListener()
	{
		public void onClick(View v) { 
            Log.d(TAG, "onClick");
		    if (v.getId() == mBtnRun.getId()) {
    			if (!mStarted) {
    				mTestRunner.resetTestCase();
    				SharedPreferences perferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    for (String testCaseName : FunctionTesterActivity.TestCaseNames) {
    				    TestCase testCase = TestCaseFactory.getTestCase(testCaseName, mResultHandler);
                        if (testCase != null && perferences.getBoolean(testCaseName, false)) {
                            mTestRunner.addTestCase(testCase);
                            testCase.setSelected(perferences.getBoolean(testCaseName, false));
                        }
    				}
                    if (mTestRunner.testcaseSize() < 1) {
                        return;
                    }
    				mTestRunner.startTest(perferences.getBoolean("Infinite", false), false);
    				mStarted = true;
    				mBtnRun.setText("Stop");
    			} else {
    				mTestRunner.stopTest();
    				mStarted = false;
    				mBtnRun.setText("Start");
                    mBtnRun.setEnabled(false);
    			}
    			
    		} else if (v.getId() == mBtnClear.getId()) {
    		    mResultHandler.obtainMessage(2, null).sendToTarget();
            }
    	}
	};

	Handler mResultHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			    case TestCase.ReportMessage.REPORT_MESSAGE:
                    mTextOutput.append((String) msg.obj + "\n");
			        final int scrollAmount = mTextOutput.getLayout().getLineTop(mTextOutput.getLineCount())-mTextOutput.getHeight();    
			        // if there is no need to scroll, scrollAmount will be <=0     
			        if(scrollAmount>0)         
			            mTextOutput.scrollTo(0, scrollAmount);     
			        else         
			            mTextOutput.scrollTo(0,0); 
			        break;
			    case 2:
			        mTextOutput.setText(""); 
			        mTextOutput.scrollTo(0,0); 
			        break;
			    case 55:
			        mStarted = false;
                    mBtnRun.setEnabled(true);
			        mBtnRun.setText("Start");
                    break;
            }
			
		}
	};
		
	@Override    
	public boolean onCreateOptionsMenu(Menu menu) {                
		menu.addSubMenu( 0, 0, 0, "Options" );                    
		return super.onCreateOptionsMenu(menu);    
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) 
	{ 
		super.onOptionsItemSelected(item); 
		switch(item.getItemId()){ 
		case 0: 
			startActivity(new Intent(this, Options.class));
			break;
		}
		return false;
	}
}