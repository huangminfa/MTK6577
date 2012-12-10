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

import com.mediatek.notebook.NotePad.Notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NoteView extends Activity{	
	private Spinner spinner; 
	private String notegroup;
	private Cursor mCursor;
	private EditText mText;
	private Uri mUri;
	private Toast maxNoteToast = null;
	private int maxLength = 1500;

		
	public void onCreate(Bundle savedInstanceState) {
		int position = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noteslist_item_editor);
        spinner = (Spinner)findViewById(R.id.spinner1);
        ActionBar ab = getActionBar();
        ab.setIcon(R.drawable.ic_title_bar_done);
        ab.setHomeButtonEnabled(true);
        String Data[] = getData(); 
        LayoutInflater inflater = (LayoutInflater) getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
		View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);
		View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
		saveMenuItem.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		        doSaveAction();
		    }
		});
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
		        ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
		        ActionBar.DISPLAY_SHOW_TITLE);
		ab.setCustomView(customActionBarView);
        BaseAdapter ba = new BaseAdapter()
        {
			public int getCount() {
				return 5;
			}
			public Object getItem(int arg0) {
				return null;
			}
			public long getItemId(int arg0) {
				return 0;
			}

			public View getView(int arg0, View arg1, ViewGroup arg2) {
				String Data_UI[] = getDataUI();
				Resources resource = (Resources) getBaseContext().getResources();   
				ColorStateList text_color = (ColorStateList) resource.getColorStateList(R.color.text);   
				LinearLayout layout = new LinearLayout(NoteView.this);
				layout.setOrientation(LinearLayout.HORIZONTAL);
				TextView tvcolor = new TextView(NoteView.this);
				tvcolor.setWidth(9);
				tvcolor.setHeight(40);
				switch(arg0)
				{
					case 0:
						tvcolor.setBackgroundResource(R.color.none);
						break;
					case 1:
						tvcolor.setBackgroundResource(R.color.work);
						break;
					case 2:
						tvcolor.setBackgroundResource(R.color.personal);
						break;
					case 3:
						tvcolor.setBackgroundResource(R.color.family);
						break;
					case 4:
						tvcolor.setBackgroundResource(R.color.study);
						break;
				}
				layout.addView(tvcolor);
				TextView tvgroup = new TextView(NoteView.this);
				tvgroup.setGravity(Gravity.RIGHT);
				tvgroup.setText(Data_UI[arg0]);
				tvgroup.setTextSize(16);
				tvgroup.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				tvgroup.setGravity(Gravity.RIGHT);
				tvgroup.setTextColor(text_color);
				layout.addView(tvgroup);
				return layout;
			}
        	
        };
        spinner.setAdapter(ba);  
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener()); 
        mText = (EditText) findViewById(R.id.note);
        mText.setFilters(new InputFilter[] { new maxLengthFilter(maxLength) });
        mUri = getIntent().getData();
        if (mUri == null)
        {
        	mText.setText("");
        }
        else
        {
	        mCursor = managedQuery(
	                mUri,         
	                NotePad.Notes.PROJECTION,   
	                null,         
	                null,         
	                null          
	            );
	        mCursor.moveToFirst();
	        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
	        int groupNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_GROUP);
	        String note = mCursor.getString(colNoteIndex);
	        String group = mCursor.getString(groupNoteIndex);
	        String gp = getGroup(group);
	        mText.setText(note);
	        for (int i = 0; i < Data.length; i++)
	        {
	        	if (Data[i].equals(gp))
	        		position = i;
	        }
        }
        spinner.setSelection(position);
	}
	
	class maxLengthFilter implements InputFilter
	{
		private int mMaxLength;
		public maxLengthFilter(int max) {
            mMaxLength = max - 1;
            maxNoteToast = Toast.makeText(NoteView.this, R.string.editor_full,
                    Toast.LENGTH_SHORT);
        }
		public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {

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
	@Override
	protected void onResume()
	{
		super.onResume();
		if (spinner.isPopupShowing())
		{
			spinner.dismissPopup();
		}
	}
	class SpinnerSelectedListener implements OnItemSelectedListener{  
		public void onItemSelected(AdapterView<?> arg0, View arg1, int which, long arg3) 
		{  
			String Data[] = getData(); 
			notegroup = Data[which];
		}
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}   
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
        //inflater.inflate(R.menu.note_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void doSaveAction() 
    {
    	String text = mText.getText().toString();
    	String Data[] = getData(); 
    	int i = 0;
    	for (i = 0; i < 5; i ++)
    	{
    		if (notegroup.equals(Data[i]))	
    		{
    			break;
    		}
    	}
    	if (mUri == null)
    	{
    		mUri = Notes.CONTENT_URI;
    		ContentValues values = new ContentValues();
			if (text.equals(""))
			{
				finish();
				NotePad.Notes.SAVE_NONE_NOTE = true;
				return;
			}
			else
			{
				values.put(NotePad.Notes.COLUMN_NAME_GROUP, String.valueOf(i));
				values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
	    		values.put(NotePad.Notes.COLUMN_NAME_TITLE, text);
			}
    		Uri retrun_uri = getContentResolver().insert(mUri, values);
    		if (retrun_uri == null)
    		{
    			mUri = null;
    			Toast.makeText(this, R.string.sdcard_full, Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    			finish();
    			NotePad.Notes.SAVE_NOTE_FLAG = 1;
    		}
    	}
    	else
    	{
    		if (text.equals(""))
    		{
    			Toast.makeText(this, R.string.empty_note, Toast.LENGTH_LONG).show();
    			return;
    		}
    		else
    		{
    			updateNote(text, null, String.valueOf(i));
    			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    			finish();
        		NotePad.Notes.SAVE_NOTE_FLAG = 1;
    		}
    	}
    }
	private String[] getData()
	{
		Resources resource = (Resources) getBaseContext().getResources();
		String group_work = (String)resource.getString(R.string.menu_work);
    	String group_none = (String)resource.getString(R.string.menu_none);
    	String group_personal = (String)resource.getString(R.string.menu_personal);
    	String group_family = (String)resource.getString(R.string.menu_family);
    	String group_study = (String)resource.getString(R.string.menu_study);
		String[] Data = {group_none, group_work, group_personal, group_family, group_study};
		return Data;
	}
	
	private String[] getDataUI()
	{
		Resources resource = (Resources) getBaseContext().getResources();
		String group_work = (String)resource.getString(R.string.menu_work_ui);
    	String group_none = (String)resource.getString(R.string.menu_none);
    	String group_personal = (String)resource.getString(R.string.menu_personal_ui);
    	String group_family = (String)resource.getString(R.string.menu_family_ui);
    	String group_study = (String)resource.getString(R.string.menu_study_ui);
		String[] Data_UI = {group_none, group_work, group_personal, group_family, group_study};
		return Data_UI;
	}

	private final void updateNote(String text, String title, String group) {
		String year, month, day, hour, minute;
		int i = 0;
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        year = String.valueOf(c.get(Calendar.YEAR));
        month = String.valueOf(c.get(Calendar.MONTH));
        day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(c.get(Calendar.MINUTE));
        if (c.get(Calendar.MONTH) < 10)
        {
        	month = "0" + month;
        }
        if (c.get(Calendar.DAY_OF_MONTH) < 10)
        {
        	day = "0" + day;
        }
        if (c.get(Calendar.HOUR_OF_DAY) < 10)
        {
        	hour = "0" + hour;
        }
        if (c.get(Calendar.MINUTE) < 10)
        {
        	minute = "0" + minute;
        }
        String modify_time = String.valueOf(year) +
                             " " + 
                             NotePad.Notes.MONTH[c.get(Calendar.MONTH)] +
					         " " + 
					         day + 
					         " " +
					         hour +
					         ":" +
					         minute;
        values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, modify_time);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        String Data[] = getData();
        for (i = 0; i < 5; i ++)
        {
        	if (notegroup == Data[i])
        	{
        		break;
        	}
        }
        values.put(NotePad.Notes.COLUMN_NAME_GROUP, String.valueOf(i));
		/*if (notegroup == Data[0])
		{
			values.put(NotePad.Notes.COLUMN_NAME_GROUP, "");
		}
		else
		{
			values.put(NotePad.Notes.COLUMN_NAME_GROUP, notegroup);
		}*/
        getContentResolver().update(
                mUri,    
                values,  
                null,   
                null    
            );

    }
}
