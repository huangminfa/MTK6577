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

package com.mediatek.engineermode.baseband;

import com.mediatek.engineermode.emsvr.*;
//import com.mediatek.engineermode.baseband.EMbaseband;
import com.mediatek.engineermode.R;

import android.app.Activity;
import android.content.Context;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.mediatek.engineermode.ShellExe;
import java.io.File;
import java.io.IOException;


public class Baseband extends Activity  implements OnClickListener {


	private Button mBtnRead;
	private Button mBtnWrite;
	private EditText mEditAddr;
	private EditText mEditLen;	
	private EditText mEditVal;
	
	private TextView mInfo;

	private String TAG = "EM-Baseband";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.baseband);

		mBtnRead = (Button) findViewById(R.id.Baseband_Read);
		mBtnWrite = (Button) findViewById(R.id.Baseband_Write);
		mEditAddr = (EditText) findViewById(R.id.Baseband_Addr);
		mEditLen = (EditText) findViewById(R.id.Baseband_Len);		
		mEditVal = (EditText) findViewById(R.id.Baseband_Val);
		
		mInfo = (TextView) findViewById(R.id.Baseband_Info);
		if(mBtnRead == null
        		|| mBtnWrite == null
        		|| mEditAddr == null
        		|| mEditLen == null
        		|| mEditVal == null
        		|| mInfo == null)
        {
        	Xlog.v(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }	

		mBtnRead.setOnClickListener(this);
		mBtnWrite.setOnClickListener(this);

	}
	


	@Override
	public void onPause() {
		//EMbaseband.End();
		super.onPause();		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();	
			
				
	}

	@Override
	protected void onStop() {
		super.onStop();		
	}	
	
	private void ShowDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		String addrString = mEditAddr.getText().toString();
		String lenString = mEditLen.getText().toString();
		String valString = mEditVal.getText().toString();
			if (null == addrString || null == lenString) {
				Toast.makeText(this, "Please input the addr or length.",
						Toast.LENGTH_LONG).show();				
				return;
			}
			
				long addrValue = 0;
				long lenValue = 0;
				long valValue = 0;
			try{
				addrValue = Long.parseLong(addrString, 16);
				lenValue = Long.parseLong(lenString, 10);			

			}
				catch(NumberFormatException e)
				{
					Toast.makeText(this, "Error: Bad hex number.",
						Toast.LENGTH_LONG).show();	
					return;
				}
				
			if (lenValue <= 0) {
				Toast.makeText(this, "Length is too small.",
						Toast.LENGTH_LONG).show();				
				return;
			}
			if (lenValue > 1024) {
				Toast.makeText(this, "Max Length is 1024",
					       Toast.LENGTH_LONG).show();				
				return;
			}

	
		if (arg0.getId() == mBtnRead.getId()) {	
		
			try{				
				Long.parseLong(addrString, 16);
				Long.parseLong(lenString, 10);

			}
				catch(NumberFormatException e)
				{
					Toast.makeText(this, "Error: Bad number format.",
						Toast.LENGTH_LONG).show();	
					return;
				}
				
			AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_BASEBAND);
        A.WriteParamNo(4);
        A.WriteParamString("r");
        A.WriteParamString(addrString);
        A.WriteParamString(lenString);
        A.WriteParamString("0");
        
        if(!result)
        {
        	Toast.makeText(this, "Error: pipe",
						Toast.LENGTH_LONG).show();	
						
						return;
        }
        
      

        FunctionReturn r;
        do
        {
            r = A.GetNextResult();
            if(r.returnString == "")
            	break;
            mInfo.setText(r.returnString);
            
        }while(r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);        
        if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
            //error
            mInfo.setText("Error");
        }	
							
			
		} else if (arg0.getId() == mBtnWrite.getId()) {
			
			try{				
				Long.parseLong(addrString, 16);
				Long.parseLong(lenString, 10);
				Long.parseLong(valString, 16);

			}
				catch(NumberFormatException e)
				{
					Toast.makeText(this, "Error: Bad number format.",
						Toast.LENGTH_LONG).show();	
					return;
				}
			
			
				
				AFMFunctionCallEx A = new AFMFunctionCallEx();
        boolean result = A.StartCallFunctionStringReturn(AFMFunctionCallEx.FUNCTION_EM_BASEBAND);
         A.WriteParamNo(4);
         A.WriteParamString("w");
         A.WriteParamString(addrString);
         A.WriteParamString(lenString);
         A.WriteParamString(valString);
        
        if(!result)
        {
        	Toast.makeText(this, "Error: pipe",
						Toast.LENGTH_LONG).show();	
						
						return;
        }
        

        FunctionReturn r;
        do
        {
            r = A.GetNextResult();
            if(r.returnString == "")
            	break;
            mInfo.setText(r.returnString);
            
        }while(r.returnCode == AFMFunctionCallEx.RESULT_CONTINUE);        
        if (r.returnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
            //error
            mInfo.setText("Error");
        }	
			
		}  else {			
			 mInfo.setText("are you kidding me ?!");
		}
		
	
	}
	

}
