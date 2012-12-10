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

import java.util.ArrayList;
import java.util.List;

import com.mediatek.notebook.NotePad.Notes;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class NoteAdapter extends BaseAdapter{

     private Context con;
     private Cursor cur;
     private int which;
     private ViewHolder holder;
     public List<NoteItem> list = new ArrayList<NoteItem>(); 
     public List<NoteItem> mylist = new ArrayList<NoteItem>(); 
     
     String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, 
    		                  NotePad.Notes.COLUMN_NAME_CREATE_DATE,
    		                  NotePad.Notes.COLUMN_NAME_GROUP} ;
     int[] viewIDs = { R.id.title, R.id.create_time, R.id.group};
     class ViewHolder 
     {
    	 TextView group;
    	 TextView create_time;
    	 TextView note;
    	 CheckBox checkbox;
     }
     class NoteItem
     {
    	 public int id;
    	 public String note;
    	 public String create_time;
    	 public boolean isselect;
    	 public String notegroup;
     }
     public NoteAdapter (Context context, Cursor cursor, int token) {
    	    con = context;
    	    cur = cursor;
    	    which = token;
     }
	 public int getCount() {
		 return cur.getCount();
	 }
	 public Object getItem(int position) {
		return list.get(position);
	 }
	 public long getItemId(int position) {
		return position;
	 }
	 public View getView(final int position, View convertView, ViewGroup parent) {
		 holder = new ViewHolder();
		 Resources resource = (Resources) con.getResources(); 
         ColorStateList color_work = (ColorStateList) resource.getColorStateList(R.color.work); 
         ColorStateList color_personal = (ColorStateList) resource.getColorStateList(R.color.personal); 
         ColorStateList color_family = (ColorStateList) resource.getColorStateList(R.color.family); 
         ColorStateList color_study = (ColorStateList) resource.getColorStateList(R.color.study); 
         String group_work = (String) resource.getString(R.string.menu_work); 
         String group_personal = (String) resource.getString(R.string.menu_personal);
         String group_family = (String) resource.getString(R.string.menu_family);
         String group_study = (String) resource.getString(R.string.menu_study);
		 convertView = LayoutInflater.from(con).inflate(R.layout.noteslist_item_context, null);   
		 TextView title = (TextView)convertView.findViewById(R.id.title); 
		 TextView createTime = (TextView)convertView.findViewById(R.id.create_time); 
		 TextView groupColor = (TextView)convertView.findViewById(R.id.groupcolor); 
		 TextView notegroup = (TextView)convertView.findViewById(R.id.group);
		 NoteItem item = list.get(position); 
		 title.setText(item.note);
		 createTime.setText(item.create_time);
		 if (item.notegroup.equals(group_personal))
		 {
			 notegroup.setTextColor(color_personal);
			 groupColor.setBackgroundResource(R.color.personal);
		 }
		 else if (item.notegroup.equals(group_work))
		 {
			 notegroup.setTextColor(color_work);
			 groupColor.setBackgroundResource(R.color.work);
		 }
		 else if (item.notegroup.equals(group_family))
		 {
			 notegroup.setTextColor(color_family);
			 groupColor.setBackgroundResource(R.color.family);
		 }
		 else if (item.notegroup.equals(group_study))
		 {
			 notegroup.setTextColor(color_study);
			 groupColor.setBackgroundResource(R.color.study);
		 }
		 else
		 {
			 groupColor.setBackgroundResource(R.color.none);
		 }
		 notegroup.setText(item.notegroup);
		 convertView.setTag(holder);
		 if (which == 2)
		 {
		   		CheckBox cb = (CheckBox)convertView.findViewById(R.id.isdelete);
		   		cb.setVisibility(0); 		
		   		if (item.isselect == true)
		   		{
		   			cb.setChecked(true);
		   		}
		   		else
		   		{
		   			cb.setChecked(false);
		   		}
		 }
		 if (which == 0)
		 {
		   		TextView tv = (TextView)convertView.findViewById(R.id.groupcolor);
		   		tv.setVisibility(0);
		 }
	  	 holder.checkbox = (CheckBox) convertView.findViewById(R.id.isdelete);
	  	 holder.checkbox.setOnClickListener(new CheckBox.OnClickListener(){
			public void onClick(View v) {
				checkboxClickAction(position);
			}    	    
	    });
	  	return convertView;
	 }
	    public void checkboxClickAction(int position)
	    {
		Resources resource = (Resources) con.getResources(); 
		String selected = (String) resource.getString(R.string.title_bar_selected); 
		NoteItem item = list.get(position);
		if (item.isselect == true)
		{
		    item.isselect = false;
		}
		else
		{
		    item.isselect = true;
		}
		int count = selectedNumber();
		NotePad.Notes.DELETE_NUM = count;
		NotePad.Notes.NOTE_DELETE.invalidateOptionsMenu();
		NotePad.Notes.actionbar.setTitle(String.valueOf(count) + "  " + selected);
	    }    	    
		public void addList(NoteItem item){ 
		    list.add(item);   
		}
		public void deleteSelectedNote()
		{
			for (int i = 0; i < cur.getCount(); i ++)
			{
				if (list.get(i).isselect == true)
				{
					int noteId = list.get(i).id;
					Uri muri = Uri.parse(Notes.CONTENT_URI +"/"+ noteId);
			        con.getContentResolver().delete(muri, null, null);
				}
			}
		}
		public void selectAllOrNoCheckbox(boolean userSelect)
		{
			Resources resource = (Resources) con.getResources(); 
			String selected = (String) resource.getString(R.string.title_bar_selected); 
			for (int i = 0; i < cur.getCount(); i ++)
			{
				list.get(i).isselect = userSelect;
				int count = selectedNumber();
				NotePad.Notes.DELETE_NUM = count;
				NotePad.Notes.actionbar.setTitle(String.valueOf(count) + "  " + selected);
			}
		}
		public int selectedNumber()
		{
			int count = 0;
			for (int i = 0; i < cur.getCount(); i ++)
			{
				if(list.get(i).isselect == true)
				{
					count ++;
				}
			}
			NotePad.Notes.DELETE_NUM = count;
			return count;
		}
}
