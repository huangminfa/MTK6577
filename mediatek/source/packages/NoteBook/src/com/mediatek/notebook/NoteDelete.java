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

import com.mediatek.notebook.NoteAdapter.NoteItem;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteDelete extends ListActivity {
	QueryHandler qh;
	ProgressDialog pdlg;
	public List<NoteItem> list = new ArrayList<NoteItem>(); 
 
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        NotePad.Notes.NOTE_DELETE = this;
        NotePad.Notes.DELETE_NUM = 0;
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        NotePad.Notes.actionbar = ab;
        ab.setIcon(R.drawable.ic_title_bar_done);
        ab.setTitle("0" + "  " + getString(R.string.title_bar_selected));
        setContentView(R.layout.noteslist_item_main);
        Intent intent = getIntent();
        if (intent.getData() == null) 
        {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        //getListView().setOnCreateContextMenuListener(this);
        
    }
    
    @Override
    protected void onResume()
    {
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
                      viewIDs);*/
        pdlg = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        qh = new QueryHandler(this.getContentResolver(), null, this);
        qh.startQuery(2, 
        		      pdlg, 
  		              getIntent().getData(), 
  		              NotePad.Notes.PROJECTION, 
  		              null, 
  		              null, 
  		              null);
        //setListAdapter(adapter);	    	
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_delete_menu, menu);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NoteDelete.class), null, intent, 0, null);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) 
    { 
    	if (NotePad.Notes.DELETE_NUM == 0)
    	{
    		menu.findItem(R.id.menu_no_select).setIcon(R.drawable.ic_clear_select_disable);
    		menu.findItem(R.id.menu_no_select).setEnabled(false);
    		menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_menu_delete_disable);
    		menu.findItem(R.id.menu_delete).setEnabled(false);
    	}
    	else
    	{
    		menu.findItem(R.id.menu_no_select).setIcon(R.drawable.ic_clear_select);
    		menu.findItem(R.id.menu_no_select).setEnabled(true);
    		menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_menu_delete_selected);
    		menu.findItem(R.id.menu_delete).setEnabled(true);
    	}
    	if (NotePad.Notes.NOTE_COUNT == NotePad.Notes.DELETE_NUM)
    	{
    		menu.findItem(R.id.menu_all_select).setIcon(R.drawable.ic_select_all_disable);
    		menu.findItem(R.id.menu_all_select).setEnabled(false);
    	}
    	else
    	{
    		menu.findItem(R.id.menu_all_select).setIcon(R.drawable.ic_select_all);
    		menu.findItem(R.id.menu_all_select).setEnabled(true);
    	}
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) {
        case R.id.menu_delete:
        	if (NotePad.Notes.ADAPTER.selectedNumber() > 0)
        	{
        		AlertDialog.Builder bld= new AlertDialog.Builder(this);
	            bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener()
	            {
	            	public void onClick(DialogInterface dialog, int which)
	            	{
	            		NotePad.Notes.ADAPTER.deleteSelectedNote();
	            		NotePad.Notes.DELETE_FLAG = true;
	            		finish();
	            	}
	            });
	            bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
	            bld.setCancelable(true);
	            bld.setMessage(getString(R.string.delete_confirm));
	            bld.setTitle(getString(R.string.delete_confirm_title));
	            AlertDialog dlg = bld.create();
	            dlg.show();
        	}
        	else
        	{
        		Toast.makeText(this, R.string.no_selected, Toast.LENGTH_LONG).show();
        	}
            return true;
        case R.id.menu_all_select:
        	setCheckBoxStatus(true);
        	NotePad.Notes.ADAPTER.selectAllOrNoCheckbox(true);
        	invalidateOptionsMenu();
        	return true;
        case R.id.menu_no_select:
        	setCheckBoxStatus(false);
        	NotePad.Notes.ADAPTER.selectAllOrNoCheckbox(false);
        	invalidateOptionsMenu();
        	return true;
        default:
        	finish();
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void setCheckBoxStatus (boolean status)
    {
    	ListView listView = (ListView)findViewById(android.R.id.list);  
        for(int i = 0; i < listView.getChildCount(); i++)
        {  
            View view = listView.getChildAt(i);  
            CheckBox cb = (CheckBox)view.findViewById(R.id.isdelete);  
            cb.setChecked(status); 
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
    	CheckBox cb = (CheckBox)v.findViewById(R.id.isdelete);
    	NotePad.Notes.ADAPTER.checkboxClickAction(position);
    	if (cb.isChecked())
    	{
    		cb.setChecked(false);
    	}
    	else
    	{
    		cb.setChecked(true);
    	}
    	
    }
}