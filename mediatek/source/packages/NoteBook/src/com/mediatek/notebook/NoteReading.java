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


import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class NoteReading extends Activity{
	private Uri mUri;
	private static Uri currentUri;
	private Cursor mCursor;
	private int position;
	AlertDialog dlg;
	private boolean mDeleteDialogVisible = false;
	protected static final String BUNDLE_KEY_DELETE_DIALOG_VISIBLE = "key_delete_dialog_visible";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteslist_item_reading);
        ActionBar actionbar = getActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
	}

	protected void onResume ()
	{
		super.onResume();
		if (NotePad.Notes.NOTE_COUNT == 0)
		{
			finish();
			return;
		}
		if (mDeleteDialogVisible && dlg == null) {
			AlertDialog.Builder bld= new AlertDialog.Builder(this);
            bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which){
            		NoteReading.this.getContentResolver().delete(currentUri, null, null);
            		NotePad.Notes.DELETE_FLAG = true;
            		finish();
            	}
            });
            bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
            bld.setCancelable(true);
            bld.setMessage(getString(R.string.delete_confirm));
            bld.setTitle(getString(R.string.delete_confirm_title));
            dlg = bld.create();
            dlg.show();
        }
        Resources resource = (Resources) getBaseContext().getResources();   
        ColorStateList color_work = (ColorStateList) resource.getColorStateList(R.color.work); 
        ColorStateList color_personal = (ColorStateList) resource.getColorStateList(R.color.personal); 
        ColorStateList color_family = (ColorStateList) resource.getColorStateList(R.color.family); 
        ColorStateList color_study = (ColorStateList) resource.getColorStateList(R.color.study); 
        mUri = getIntent().getData();
        mCursor = managedQuery(
        		mUri,            
                NotePad.Notes.PROJECTION,   
                null,         
                null,         
                null          
            );
        mCursor.moveToFirst();
        TextView mTextGroup = (TextView) findViewById(R.id.group);
        TextView mTextModify = (TextView) findViewById(R.id.modify_time);
        TextView mTextContext = (TextView) findViewById(R.id.context);
        int contextNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
        int idNoteIndex = mCursor.getColumnIndex(NotePad.Notes._ID);
        int groupNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_GROUP);
        int modifyNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CREATE_DATE);
        String note = mCursor.getString(contextNoteIndex);
        String group = mCursor.getString(groupNoteIndex);
        String modify = mCursor.getString(modifyNoteIndex);
        int noteId = mCursor.getInt(idNoteIndex);
        currentUri = Uri.parse(NotePad.Notes.CONTENT_URI +"/"+ noteId);
        mTextContext.setText(note);
        mTextContext.setMovementMethod(ScrollingMovementMethod.getInstance());
        modify = CurrentDay(modify); 
        mTextModify.setText(modify);
        String gp = getGroup(group);
        mTextGroup.setText(gp);
        if (gp.equals(getString(R.string.menu_personal)))
		{
        	mTextGroup.setTextColor(color_personal);
		}
		else if (gp.equals(getString(R.string.menu_work)))
		{
			mTextGroup.setTextColor(color_work);
		}
		else if (gp.equals(getString(R.string.menu_family)))
		{
			mTextGroup.setTextColor(color_family);
		}
		else if (gp.equals(getString(R.string.menu_study)))
		{
			mTextGroup.setTextColor(color_study);
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_KEY_DELETE_DIALOG_VISIBLE, mDeleteDialogVisible);
    }

	@Override
    public void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mDeleteDialogVisible = outState.getBoolean(BUNDLE_KEY_DELETE_DIALOG_VISIBLE, false);
    }
	
	public String getGroup(String i)
    {
    	Resources resource = (Resources)this.getResources(); 
    	String group_work = (String) resource.getString(R.string.menu_work); 
        String group_personal = (String) resource.getString(R.string.menu_personal);
        String group_family = (String) resource.getString(R.string.menu_family);
        String group_study = (String) resource.getString(R.string.menu_study);
        if (i.equals("1"))
    	{
    		return group_work;
    	}
    	else if (i.equals("2"))
    	{
    		return group_personal;
    	}
    	else if (i.equals("3"))
    	{
    		return group_family;
    	}
    	else if (i.equals("4"))
    	{
    		return group_study;
    	}
    	else
    	{
    		return "";
    	}
    }
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_reading_menu, menu);        
        return super.onCreateOptionsMenu(menu);
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_edit_current:
        	Intent it = new Intent(this, NoteView.class);
        	it.setData(currentUri);
        	this.startActivity(it);
        	finish();
            break;
        case R.id.menu_delete_current:
        	AlertDialog.Builder bld= new AlertDialog.Builder(this);
            bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which){
            		NoteReading.this.getContentResolver().delete(currentUri, null, null);
            		NotePad.Notes.DELETE_FLAG = true;
            		finish();
            	}
            });
            bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
            bld.setCancelable(true);
            bld.setMessage(getString(R.string.delete_confirm));
            bld.setTitle(getString(R.string.delete_confirm_title));
            dlg = bld.create();
            dlg.show();
            mDeleteDialogVisible = true;
            NotePad.Notes.DELETE_NUM = 1;
        	break;
        default:
        	finish();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	public String CurrentDay(String modifyDay)
    {
        Calendar c = Calendar.getInstance();
        String current_day;
    	String []mt = modifyDay.split(" ");
    	String current_year = String.valueOf(c.get(Calendar.YEAR));
    	String current_month = NotePad.Notes.MONTH[c.get(Calendar.MONTH)];
    	if (c.get(Calendar.DAY_OF_MONTH) < 10)
    	{
    		current_day = "0" + String.valueOf(c.get(Calendar.DAY_OF_MONTH)); 
    	}
    	else 
    	{
    		current_day = String.valueOf(c.get(Calendar.DAY_OF_MONTH)); 
    	}
    	String save_year = mt[0];
    	String save_month = mt[1];
    	String save_day = mt[2];
    	if (current_year.equals(save_year))
    	{
    		if (current_month.equals(save_month) && current_day.equals(save_day))
            {
            	return mt[3];
            }
    		else
    		{
    			return mt[1] + " " + mt[2] + " " + mt[3];
    		}
    			
    	}
        else
        {
        	return modifyDay;
        }
    }

}
