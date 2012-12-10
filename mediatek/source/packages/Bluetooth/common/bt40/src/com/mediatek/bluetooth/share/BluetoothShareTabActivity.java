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

package com.mediatek.bluetooth.share;

import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Process;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

import com.mediatek.activity.MessageActivity;
import com.mediatek.bluetooth.BluetoothShareGatewayActivity;
import com.mediatek.bluetooth.R;
import com.mediatek.bluetooth.util.NotificationFactory;
import com.mediatek.bluetooth.util.SystemUtils;
import com.mediatek.bluetooth.share.BluetoothShareTask.BluetoothShareTaskMetaData;
import com.mediatek.bluetooth.util.BtLog;

public class BluetoothShareTabActivity extends Activity implements OnItemClickListener, Callback {

	private static final String EXTRA_KEY_DIR = "isOutgoing";

	private static final String INCOMING_SELECTION = BluetoothShareTask.SC_INCOMING_TASK + " AND " + BluetoothShareTask.SC_FINISHED_TASK;
	private static final String OUTGOING_SELECTION = BluetoothShareTask.SC_OUTGOING_TASK + " AND " + BluetoothShareTask.SC_FINISHED_TASK;

	private static Handler handler;

	// create Intent to start OppSharePageActivity
	protected static Intent getIntent( Context context, boolean isOutgoing ){

		Intent intent = new Intent( context, BluetoothShareTabActivity.class );
		intent.putExtra( EXTRA_KEY_DIR, isOutgoing );
		return intent;
	}

	private Cursor cursor = null;

	@Override
	protected void onCreate( Bundle savedInstanceState ){

		BtLog.d( "BluetoothShareTabActivity.onCreate()[+]" );
		super.onCreate(savedInstanceState);

		// intent
		Intent intent = this.getIntent();
		boolean isOutgoing = intent.getBooleanExtra( EXTRA_KEY_DIR, false );

		// layout
		this.setContentView( R.layout.bt_share_mgmt_tab );
		ListView listView = (ListView)this.findViewById( R.id.listView );

		// query all tasks
		this.cursor = this.managedQuery( BluetoothShareTaskMetaData.CONTENT_URI,
				null,
				isOutgoing ? OUTGOING_SELECTION : INCOMING_SELECTION,
				BluetoothShareTaskMetaData._ID + " DESC" );

		// create list adapter
		if( this.cursor != null ){

			BluetoothShareTabAdapter listAdapter = new BluetoothShareTabAdapter( this, R.layout.bt_share_mgmt_item, this.cursor );
			listView.setAdapter( listAdapter );
			listView.setScrollBarStyle( View.SCROLLBARS_INSIDE_INSET );
			listView.setOnCreateContextMenuListener( this );
			listView.setOnItemClickListener( this );
		}

		// create handler
		if( handler == null ){

			handler = new Handler(this);
		}
	}

	/*****************************************************
	 * Option Menu => Clear List
	 *****************************************************/
	@Override
	public boolean onCreateOptionsMenu( Menu menu ){

		// create options menu (clear) when cursor is initialized
		if( this.cursor != null ){
			//this.getMenuInflater().inflate( R.menu.bt_share_mgmt_tab_menu, menu );

			menu.add(Menu.NONE, R.id.bt_share_mgmt_tab_menu_clear, 0, R.string.bt_share_mgmt_tab_menu_clear)
			    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu( Menu menu ){

		// disable options menu (clear) according to the item count
		menu.findItem( R.id.bt_share_mgmt_tab_menu_clear ).setEnabled( (this.cursor.getCount() > 0) );
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){

		// implement menu action: "clear"
		if( item.getItemId() ==  R.id.bt_share_mgmt_tab_menu_clear ){
			this.clearAllTasks();
			return true;
		}
		return false;
	}
	/**
	 * clear all tasks in the current list
	 */
	private void clearAllTasks(){
        
		int columnIndex = this.cursor.getColumnIndexOrThrow( BluetoothShareTaskMetaData._ID );
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for( this.cursor.moveToFirst(); !this.cursor.isAfterLast(); this.cursor.moveToNext() ){
			// compose Uri for the task and clear it
			int id = this.cursor.getInt( columnIndex );
			Uri uri = Uri.withAppendedPath( BluetoothShareTaskMetaData.CONTENT_URI, Integer.toString(id) );
			uris.add(uri);
		}
		
		Object[] param = new Object[]{ this.cursor, uris};
		handler.sendMessage(handler.obtainMessage( CLEAR_ALL_TASK, param ) );
	}
	/**
	 * clear specific task (update state = CLEARED)
	 * 
	 * @param uri
	 */
	private void clearShareTask( Uri uri ){

		handler.sendMessage( handler.obtainMessage( CLEAR_SHARE_TASK, uri ) );
	}

	/**
	 * Implement interface: OnItemClickListener
	 */
	@Override
	public void onItemClick( AdapterView<?> parent, View view, int position, long id ){

		// find the item that is clicked
		this.cursor.moveToPosition( position );
		final BluetoothShareTask task = new BluetoothShareTask(this.cursor);

		if( task.getDirection() == BluetoothShareTask.Direction.in ){

			if( task.getState() == BluetoothShareTask.STATE_SUCCESS ){

				// open downloaded file
				Intent openFileIntent = SystemUtils.getOpenFileIntent( this, task.getData(), task.getMimeType() );
				this.startActivity(openFileIntent);
			}
			else if( task.getState() == BluetoothShareTask.STATE_FAILURE ){

				Intent intent = MessageActivity.createIntent( this,
						this.getString( R.string.bt_share_mgmt_tab_dialog_title ), // title
						this.getString( R.string.bt_share_mgmt_tab_dialog_message_recfail, task.getObjectName(), task.getPeerName() ), // message
						this.getString( R.string.bt_share_mgmt_tab_dialog_no ) );
				this.startActivity(intent);
			}
		}
		else if( task.getDirection() == BluetoothShareTask.Direction.out ){

			if( task.getState() == BluetoothShareTask.STATE_FAILURE ){

				// check to re-send the failed outgoing file
				Intent resendIntent = new Intent( Intent.ACTION_SEND );
				resendIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				resendIntent.setType( task.getMimeType() );
				resendIntent.putExtra( Intent.EXTRA_STREAM, Uri.parse( task.getObjectUri() ) );
				resendIntent.putExtra( BluetoothShareGatewayActivity.EXTRA_DEVICE_ADDRESS, BluetoothAdapter.getDefaultAdapter().getRemoteDevice( task.getPeerAddr() ) );

				Intent intent = MessageActivity.createIntent( this,
						this.getString( R.string.bt_share_mgmt_tab_dialog_title ), // title
						this.getString( R.string.bt_share_mgmt_tab_dialog_message_resend, task.getObjectName(), task.getPeerName() ), // message
						this.getString( R.string.bt_share_mgmt_tab_dialog_yes ), // positive button
						resendIntent, // positive intent
						this.getString( R.string.bt_share_mgmt_tab_dialog_no ) ); // negative button
				this.startActivity(intent);
			}
			else if( task.getState() == BluetoothShareTask.STATE_SUCCESS ){

				Intent intent = MessageActivity.createIntent( this,
						this.getString( R.string.bt_share_mgmt_tab_dialog_title ), // title
						this.getString( R.string.bt_share_mgmt_tab_dialog_message_sent, task.getObjectName(), task.getPeerName() ), // message
						this.getString( R.string.bt_share_mgmt_tab_dialog_no ) );
				this.startActivity(intent);
			}
		}

		// clear clicked item
		this.clearShareTask( task.getTaskUri() );
	}

	private static final int CLEAR_SHARE_TASK = 1;
	private static final int CLEAR_ALL_TASK = 2;
	private BtShareClearHistoryThread oppClearThread;
	
	@Override
	public boolean handleMessage( Message message ){
		
		BtLog.d( "handleMessage: " + message.what );
		
		switch( message.what ){
			case CLEAR_SHARE_TASK:
			
			    // update share task
			    ContentValues updateValues = new ContentValues();
			    updateValues.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_CLEARED );
				this.getContentResolver().update( (Uri)message.obj, updateValues, null, null );
				
				// doesn't need to check empty list
				if( this.cursor.getCount() > 1 )	return true;
				
				// check empty list and cancel notification if necessary
				BtLog.d( "clear all items in list and trigger check event" );
				Cursor c = null;
				try {
					c = this.getContentResolver().query(
						BluetoothShareTaskMetaData.CONTENT_URI, 
						null,
						BluetoothShareTask.SC_FINISHED_TASK,
						null,
						null );
				
					if( c.getCount() == 0 ){
				
						BtLog.d( "No record to be showed and cancel notification" );					
						((NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel( NotificationFactory.NID_SHARE_MGMT_NOTIFICATION );
					}
				}
				catch( Exception ex ){
				
					BtLog.e( "check empty share list error:", ex );
				}
				finally {
					if( c != null ) c.close();
				}
				
				break;
			case CLEAR_ALL_TASK:
			    Object[] param = (Object[])message.obj;				
			    oppClearThread = new BtShareClearHistoryThread( new Object[]{param[0], param[1]} );
			    oppClearThread.start();
			    break;

			default:
				BtLog.d( "handleMessage: Unknown Message!" );
				break;

		}

		return false;
	}
	

    public class BtShareClearHistoryThread extends Thread {
	   private Cursor cursor = null;
	   private ArrayList<Uri> uris;

	   public BtShareClearHistoryThread (Object[] param){
		   super( "BtShareClearHistoryThread" );
		   this.cursor = (Cursor) param[0];
		   this.uris = (ArrayList<Uri>) param[1];
	   }

	   @Override
	   public void run() {

		   BtLog.d( "BtShareClearHistoryThread start: [+]" );
		   Process.setThreadPriority( Process.THREAD_PRIORITY_BACKGROUND );

		   this.oppClearAllItems();

		   BtLog.d( "BtShareClearHistoryThread End: [-]" );
		   
	   }

	   public synchronized void oppClearAllItems(){

		   if( this.cursor == null ){
               BtLog.d( "this.cursor is null " );
			   return;
		   }
           //BtLog.d( "this.cursor.getCount(): " + this.cursor.getCount() );
		   
		   int columnIndex = this.cursor.getColumnIndexOrThrow( BluetoothShareTaskMetaData._ID );

		   for( Uri uri: this.uris ){
			   ContentValues updateValues = new ContentValues();
			   updateValues.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_CLEARED );
			   BluetoothShareTabActivity.this.getContentResolver().update( uri, updateValues, null, null );
		   } 
           
		   // check empty list and cancel notification if necessary
		   BtLog.d( "clear all items in list and trigger check event" );
		   Cursor c = null;
		   try {
			   c = BluetoothShareTabActivity.this.getContentResolver().query(
				   BluetoothShareTaskMetaData.CONTENT_URI, 
				   null,
				   BluetoothShareTask.SC_FINISHED_TASK,
				   null,
				   null );
		   
			   if( c.getCount() == 0 ){
		   
				   BtLog.d( "No record to be showed and cancel notification" ); 				   
				   ((NotificationManager)BluetoothShareTabActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel( NotificationFactory.NID_SHARE_MGMT_NOTIFICATION );
			   }
		   }
		   catch( Exception ex ){
		   
			   BtLog.e( "check empty share list error:", ex );
		   }
		   finally {
			   if( c != null ) c.close();
		   }


	   }
   }


	
}
