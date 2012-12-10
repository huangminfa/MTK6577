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

package com.mediatek.notebook;

import com.mediatek.notebook.NotePad;
import com.mediatek.notebook.NoteAdapter.NoteItem;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NotesList extends ListActivity {

    private static final String TAG = "NotesList";
    private static final int COLUMN_INDEX_TITLE = 1;
	public static final int REFRESH = 0;
    private int flag = 0;
    private TextView textview;
    public TextView countView;
    ProgressDialog pdlg;
    
    private ContentObserver cob = new ContentObserver(new Handler()) {    
        @Override 
        public boolean deliverSelfNotifications() { 
            return super.deliverSelfNotifications(); 
        } 
        @Override 
        public void onChange(boolean selfChange) { 
            super.onChange(selfChange);
            flag = 1;
        } 
    }; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        setContentView(R.layout.noteslist_item_main);  
        Intent intent = getIntent();
        if (intent.getData() == null) 
        {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        ViewGroup view  = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.notelist_action_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(view, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, 
        		                                                 ActionBar.LayoutParams.WRAP_CONTENT,
                                                                 Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        textview = (TextView)view.findViewById(R.id.note_count);
        countView = textview;
        textview.setText("0");     
        getContentResolver().registerContentObserver(getIntent().getData(),
                true,
                cob);      
        //getListView().setOnCreateContextMenuListener(this);
    }
    
    protected void onResume() {
        super.onResume();
        /*String[] dataColumns = { NotePad.Notes.COLUMN_NAME_NOTE, 
        		                 NotePad.Notes.COLUMN_NAME_CREATE_DATE,
        		                 NotePad.Notes.COLUMN_NAME_GROUP} ;
        int[] viewIDs = { R.id.title, R.id.create_time, R.id.group};
        SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
                      this,                             
                      R.layout.noteslist_item_context,          
                      null,                           
                      dataColumns,
                      viewIDs
              );*/
        pdlg = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        QueryHandler qh = new QueryHandler(this.getContentResolver(), this, null);
        qh.startQuery(0, 
        		      pdlg, 
  		              getIntent().getData(), 
  		              NotePad.Notes.PROJECTION, 
  		              null, 
  		              null, 
  		              NotePad.Notes.DEFAULT_SORT_ORDER);
        //setListAdapter(adapter);           	
        Resources resource = (Resources) getBaseContext().getResources();
		String delete_success = (String)resource.getString(R.string.delete_success);
		String delete_pre = (String)resource.getString(R.string.delete_number_pre);
		String delete_next = (String)resource.getString(R.string.delete_number_next);
		String delete_hole_string = delete_success + "\r\n" +delete_pre + " " + String.valueOf(NotePad.Notes.DELETE_NUM) + " " + delete_next;
        if (NotePad.Notes.DELETE_FLAG == true)
        {
			NotePad.Notes.NOTIFICATION_ID ++;
        	NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    		Notification notification = new Notification(R.drawable.ic_notification_delete, 
    				                              delete_hole_string,
    				                              System.currentTimeMillis());
    		notification.flags = Notification.FLAG_AUTO_CANCEL;
    		CharSequence contentTitle = delete_success;  
            CharSequence contentText = delete_pre + " " + String.valueOf(NotePad.Notes.DELETE_NUM) + " " + delete_next;  
            Intent it = new Intent(this, NotesList.class);  
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, it, 0);       
            notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);  
      
            nm.notify(NotePad.Notes.NOTIFICATION_ID, notification); 
            NotePad.Notes.DELETE_FLAG = false;
        }
        if (NotePad.Notes.SAVE_NOTE_FLAG == 1)
        {
        	Toast.makeText(this, R.string.note_saved, Toast.LENGTH_LONG).show();
        	NotePad.Notes.SAVE_NOTE_FLAG = 0;
        }
        else if (NotePad.Notes.SAVE_NONE_NOTE == true)
        {
        	Toast.makeText(this, R.string.save_none, Toast.LENGTH_LONG).show();
        	NotePad.Notes.SAVE_NONE_NOTE = false;
        }
        if (flag == 1)
        {
        	queryUpdateData();
        	flag = 0;
        }  
    } 
    
    public void queryUpdateData()
    {
    	pdlg = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        QueryHandler qh = new QueryHandler(this.getContentResolver(), this, null);
        qh.startQuery(0, 
  		              pdlg, 
  		              getIntent().getData(), 
  		              NotePad.Notes.PROJECTION, 
  		              null, 
  		              null, 
  		              NotePad.Notes.DEFAULT_SORT_ORDER);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {   
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);   
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
    	super.onPrepareOptionsMenu(menu);
    	if (NotePad.Notes.NOTE_COUNT == 0)
    	{
    		menu.findItem(R.id.menu_muti_delete).setEnabled(false);
    		menu.findItem(R.id.menu_sort_by_modify).setEnabled(false);
    		menu.findItem(R.id.menu_sort_by_tab).setEnabled(false);
    	}
    	else
    	{
    		menu.findItem(R.id.menu_muti_delete).setEnabled(true);
    		menu.findItem(R.id.menu_sort_by_modify).setEnabled(true);
    		menu.findItem(R.id.menu_sort_by_tab).setEnabled(true);
    	}
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent it;
        switch (item.getItemId()) {
        case R.id.menu_add:
           it = new Intent(this, NoteView.class);
           this.startActivity(it);
           return true;
        case R.id.menu_muti_delete:
        	it = new Intent(this, NoteDelete.class);
        	it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		this.startActivity(it);
            return true;
        case R.id.menu_sort_by_tab:
        	NotePad.Notes.DEFAULT_SORT_ORDER = "notegroup DESC, modified DESC";
        	queryUpdateData();
        	return true;
        case R.id.menu_sort_by_modify:
        	NotePad.Notes.DEFAULT_SORT_ORDER = "modified DESC";
        	queryUpdateData();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        if (cursor == null) {
            return;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), 
                                        Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }*/

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
        switch (item.getItemId()) {
        case R.id.context_open:
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
            return true;

        case R.id.context_copy:
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newUri(   
                    getContentResolver(),               
                    "Note",                             
                    noteUri)                            
            );
            return true;

        case R.id.context_delete:
            getContentResolver().delete(
                noteUri,  
                null,     
                null      
            );
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        NoteItem noteitem = (NoteItem)l.getAdapter().getItem(position);
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), noteitem.id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else { 
        	Intent it = new Intent(this, NoteReading.class);
        	it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	it.setData(uri);
        	this.startActivity(it);
        	
        }
    }
}
