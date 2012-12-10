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

package com.mediatek.engineermode;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.mediatek.xlog.Xlog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class AutoAnswer extends Activity implements OnClickListener {
	
	private Button btn;
	private String TAG = "EM-AutoAnswer";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_answer);
        
        btn = (Button) findViewById(R.id.AutoAnswer_Btn);
        if(btn == null)
        {
        	Xlog.v(TAG, "clocwork worked...");	
    		//not return and let exception happened.
        }
        btn.setOnClickListener(this);
        
        SharedPreferences sh = this.getSharedPreferences("AutoAnswer", MODE_WORLD_READABLE);
        boolean flag = sh.getBoolean("flag", false);
      Xlog.v(TAG, "onCreate flag is :" + flag);

        if(false == flag)
        {
        	btn.setText("Enable");
        }
        else if(true == flag)
        {
        	btn.setText("Disable");
        }
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId() == btn.getId())
		{
			if("Enable" == btn.getText())
			{
				SharedPreferences sh = this.getSharedPreferences("AutoAnswer", MODE_WORLD_READABLE);
				SharedPreferences.Editor editor = sh.edit();
				editor.putBoolean("flag", true);
				editor.commit();
				btn.setText("Disable");
				Xlog.v(TAG, "Auto Answer has been enabled.");
			}
			else if("Disable" == btn.getText())
			{
				SharedPreferences sh = this.getSharedPreferences("AutoAnswer", MODE_WORLD_READABLE);
				SharedPreferences.Editor editor = sh.edit();
				editor.putBoolean("flag", false);
				editor.commit();
				btn.setText("Enable");
				Xlog.v(TAG, "Auto Answer has been disabled.");
			}
		}
	}
	
	
}
