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

import com.mediatek.notebook.NoteAdapter.NoteItem;
import com.mediatek.notebook.NotePad.Notes;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;


public class QueryHandler extends AsyncQueryHandler{
	private NotesList myContext;
	private NoteDelete nd;
	private Cursor cur;
	ProgressDialog dlg;
	public QueryHandler(ContentResolver cr, 
			            NotesList context, 
			            NoteDelete notedelete) {
		super(cr);
		myContext = context;
		nd = notedelete;
	}
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {  

   	 cur = cursor;
   	 NotePad.Notes.NOTE_COUNT = cursor.getCount();
   	 dlg = (ProgressDialog)cookie;
   	 if (token == 0)
   	 {
   	 if (cursor.getCount() > 0)
   	 {
   		myContext.countView.setText(String.valueOf(cursor.getCount()));
   	 }
   	 else
   	 {
   		myContext.countView.setText("");
   	 }
	   	NotePad.Notes.ADAPTER = new NoteAdapter(myContext, cursor, token);
	   	NotePad.Notes.ADAPTER.notifyDataSetChanged();
	   	setData(cursor);
	   	myContext.setListAdapter(NotePad.Notes.ADAPTER);
	   	ListView lv = myContext.getListView();
	   	lv.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				return true;
			}
	   	});
	 }
   	 else if (token == 2)
   	 {
   		NotePad.Notes.ADAPTER = new NoteAdapter(nd, cursor, token);
   		NotePad.Notes.ADAPTER.list = nd.list;
	    setData(cursor);
	   	nd.setListAdapter(NotePad.Notes.ADAPTER);
	    nd.list = NotePad.Notes.ADAPTER.list;
   	 }
   	dlg.cancel();
    }
    public void setData(Cursor cursor)
    {
    	NoteItem item;
    	String note;
    	String modifyTime;
    	String currentTime;
    	String notegroup;
    	int id;
    	if (cursor.moveToFirst())
    	{
    		int idColumn = cursor.getColumnIndex(Notes._ID);
    		int titleColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_NOTE);
    		int modifyColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_CREATE_DATE);
    		int groupColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_GROUP);
    		do
    		{
    			id = cur.getInt(idColumn);
    			note = cur.getString(titleColumn);
    			modifyTime = cur.getString(modifyColumn);
    			notegroup = cur.getString(groupColumn);
    			item = NotePad.Notes.ADAPTER.new NoteItem();
    			item.id = id;
    			item.note = note;
    			currentTime = CurrentDay(modifyTime);
    			item.create_time = currentTime;
    			item.notegroup = getGroup(notegroup);
    			NotePad.Notes.ADAPTER.addList(item);   
    		}
    		while(cur.moveToNext());
    	}
    }

    public String CurrentDay(String modifyDay)
    {
    	String current_day;
        Calendar c = Calendar.getInstance();
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
    
    public int selectedNumber()
    {
    	int count = NotePad.Notes.ADAPTER.selectedNumber();
    	return count;
    }
    public String getGroup(String i)
    {
    	Resources resource;
    	if (myContext == null)
    	{
    		resource = (Resources) nd.getResources(); 
    	}
    	else
    	{
    		resource = (Resources) myContext.getResources(); 
    	}
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
}
