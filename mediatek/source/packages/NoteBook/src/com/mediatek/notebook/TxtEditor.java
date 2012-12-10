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

package com.mediatek.notebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.mediatek.notebook.NoteView.maxLengthFilter;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class TxtEditor extends Activity{
	private EditText mText;
	String filePath;
	private int maxLength = 10000;
	private Toast maxNoteToast = null;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.ic_title_bar_done);
        setContentView(R.layout.txt_reader_edit);
        mText = (EditText) findViewById(R.id.context);
        filePath = getIntent().getAction();
        String content = getIntent().getType();
        mText.setText(content);
        mText.setFilters(new InputFilter[] { new maxLengthFilter(maxLength)});
        LayoutInflater inflater = (LayoutInflater) getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
		View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);
		View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
		saveMenuItem.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) 
		    {
		        doSaveAction();
            }
		});
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
		        ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
		        ActionBar.DISPLAY_SHOW_TITLE);
		getActionBar().setCustomView(customActionBarView);
    }


    public void doSaveAction() 
	{  	
		String text = mText.getText().toString();
		if (text.equals(""))
		{
			Toast.makeText(this, R.string.empty_file, Toast.LENGTH_LONG).show();
		}
		else
		{
			NotePad.Notes.SAVE_FILE = true;
			writeToFile();
			finish();
		}		
	}
	public void writeToFile()
    {
    	String content = mText.getText().toString();
    	FileOutputStream os = null;
        try
        {
        	File myFile = new File(filePath);
        	os = new FileOutputStream(myFile);
        	OutputStreamWriter writer = new OutputStreamWriter(os, "gb2312");
        	writer.write(content);
        	writer.close();
        	os.close();
        }
        catch(FileNotFoundException e)
        {
    		Log.e("MyTest", "createFile:",e);
        }
	    catch(IOException e)
	    {
	    	Log.e("MyTest", "write file",e);
	    }
	    finally
	    {
	    	if(os!=null)
	    {
	    try{
		    os.flush();
		    os.close();
		    Log.e("MyTest", "close");
	    }catch(IOException e)
	    {
	    	Log.e("MyTest", "close file",e);
	    }
		}
	}
  }
	class maxLengthFilter implements InputFilter
	{
		private int mMaxLength;
		public maxLengthFilter(int max) 
		{
            mMaxLength = max - 1;
            maxNoteToast = Toast.makeText(TxtEditor.this, R.string.file_full, Toast.LENGTH_SHORT);
        }
		public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) 
		{

			int keep = mMaxLength - (dest.length() - (dend - dstart));
			if (keep < (end - start)) 
			{
				maxNoteToast.show();
			}	
			if (keep <= 0) 
			{
				return "";
			} 
			else if (keep >= end - start) 
			{
				return null; // keep original
			} 
			else 
			{
				return source.subSequence(start, start + keep);
			}
		}
	}
}
