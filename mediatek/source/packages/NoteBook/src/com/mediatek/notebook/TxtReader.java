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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.http.util.EncodingUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class TxtReader extends Activity{
	private String fileName;
	private String content;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
        setContentView(R.layout.txt_reader_view);
        TextView textview = (TextView)findViewById(R.id.context);
        textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        Uri uri = this.getIntent().getData();
        fileName = uri.getPath();
        StringTokenizer st = new StringTokenizer(uri.getLastPathSegment(), ".");
        actionbar.setTitle(st.nextToken());
    }
    protected void onResume()
    {
    	super.onResume();
    	if (NotePad.Notes.SAVE_FILE == true)
    	{
    		Toast.makeText(this, R.string.file_saved, Toast.LENGTH_LONG).show();
    		NotePad.Notes.SAVE_FILE = false;
    	}
    	readFromFile(fileName);
    }

    void readFromFile(String fileName)
    {
	    FileInputStream is = null;
	    try
	    {
	    	is = new FileInputStream(fileName);
	    	byte buffer[] = new byte[is.available()];
	    	if (is.available() > 20000)
	    	{
	    		Toast.makeText(this, R.string.large_file, Toast.LENGTH_LONG).show();
	    		finish();
	    		return;
	    	}
	    	is.read(buffer);
	    	content = EncodingUtils.getString(buffer, "GB2312");
	    	TextView tv = (TextView)findViewById(R.id.context);
	    	tv.setText(content);
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
	    	if(is!=null)
	    {
	    try
	    {
	    	is.close();
	    }
	    catch(IOException e)
	    {
	    	Log.e("MyTest", "close file",e);
	    }
        }
      } 
    }
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.txt_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_edit_current:
        	Intent it = new Intent(this, TxtEditor.class);
        	it.setAction(fileName);
        	it.setType(content);
        	startActivity(it);
            break;
        case R.id.menu_delete_current:
        	AlertDialog.Builder bld= new AlertDialog.Builder(this);
            bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which){
            		File file = new File(fileName);
            		file.delete();
            		finish();
            	}
            });
            bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
            bld.setCancelable(true);
            bld.setMessage(getString(R.string.delete_file_confirm));
            bld.setTitle(getString(R.string.delete_file_confirm_title));
            AlertDialog dlg = bld.create();
            dlg.show();
        	break;
        default:
        	finish();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
}
