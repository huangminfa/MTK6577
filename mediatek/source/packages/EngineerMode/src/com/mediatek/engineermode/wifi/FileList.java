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

package com.mediatek.engineermode.wifi;

import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * A minimal "Hello, World!" application.
 */
public class FileList extends Activity {
	/**
	 * Called with the activity is first created.
	 */
	private final String TAG = "EM/WIFI_FileList";

	private ListView mFileList;
	private List<String> items = null;
	private final String EEPROMFilePath = "/data/data/com.mediatek.engineermode/EEPROM";
	private final String DPDFilePath = "/data/data/com.mediatek.engineermode/DPD";
	private String filePath = EEPROMFilePath;	//by default
	private final int DELETE_FILE_MENU = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.file_name_list);
		
		mFileList = (ListView) findViewById(R.id.file_list);
		if(null != mFileList)
		{
			this.registerForContextMenu(mFileList);
		}
		else
		{
			Xlog.w(TAG, "findViewById(R.id.file_list) failed");
		}
		
		items = new ArrayList<String>();
		
//		String f = getFilesDir().getAbsolutePath();
//		Log.d(TAG, f);
		
//		filePath = f.toString() + "/myData";
		Intent intent = this.getIntent();
		if(intent != null)
		{
			String sIndentifier = intent.getStringExtra("identifier");
			if(null != sIndentifier)
			{
				filePath = sIndentifier.equals("EEPROM") ? EEPROMFilePath : DPDFilePath;
			}
			else
			{
				Xlog.w(TAG, "intent.getStringExtra() failed");
			}
		}
		else
		{
			Xlog.w(TAG, "this.getIntent() failed");
		}
		File fileName = new File(filePath);
		if(null == fileName)
		{
			FileList.this.setResult(RESULT_CANCELED, null);
			finish();
		}
		else if (fileName.exists()) 
		{
			File[] files = fileName.listFiles();
			if(files != null)
			{
				for (int i = 0; i < files.length; i++) {
					items.add(files[i].getName());
				}
			}
			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, items);
			if(null != mFileList)
			{
				mFileList.setAdapter(adapter);
				mFileList.setOnItemClickListener(new OnItemClickListener() {
	
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
								Object obj = mFileList.getItemAtPosition(arg2);
						// TODO Auto-generated method stub
						if(null != obj)
						{
							String name = obj.toString();
							Bundle b = new Bundle();
							if(null != b)
							{
								b.putString("FILENAME", name);
							}
							else
							{
								FileList.this.setResult(RESULT_CANCELED, null);
							}
							Intent intent = new Intent();
							if(null != intent)
							{
								intent.putExtras(b);
								FileList.this.setResult(RESULT_OK, intent);
							}
							else
							{
								FileList.this.setResult(RESULT_CANCELED, null);
							}
							
						}
						else
						{
							Xlog.w(TAG, "mFileList.getItemAtPosition(arg2) failed");
							FileList.this.setResult(RESULT_CANCELED, null);
						}
						finish();
					}
				});
			}
		}

	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_FILE_MENU, 0, "Delete");
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_FILE_MENU:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			String name = filePath + "/" + items.get(info.position);
			File file = new File(name);
			file.delete();
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	public void onStop() {
		super.onStop();
		finish();
	}
}
