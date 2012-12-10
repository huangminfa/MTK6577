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

package com.mediatek.bluetooth.opp.adp;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.mediatek.bluetooth.Options;
import com.mediatek.bluetooth.opp.mmi.OppLog;
import com.mediatek.bluetooth.opp.mmi.UriDataUtils;
import com.mediatek.bluetooth.share.BluetoothShareTask;
import com.mediatek.bluetooth.share.BluetoothShareTask.BluetoothShareTaskMetaData;
import com.mediatek.bluetooth.util.BtLog;
import com.mediatek.bluetooth.util.SystemUtils;

/**
 * 1. ContentProvider consumer
 * 2. Thread management
 */
public class OppService extends OppServiceNative {

	private OppManager oppManager;

	private OppTaskWorkerThread oppcWorker;
	private OppTaskWorkerThread oppsWorker;

	// For ALPS00118268 & ALPS00235236, task thread can't interuppt properly
	private boolean isTaskWorkThreadInteruppted = false;

	// For ALPS00231774, contentProvider operations remove out from OPPService to oppcWorkerThread
	private boolean isOppcResetTask = false;
	// For ALPS0026, contentProvider operations remove out from OPPService to oppsWorkerThread
	private boolean isOppsResetTask = false;

	private String oppcCurrentStoragePath = null;
	private String oppsCurrentStoragePath = null;
	
	private BroadcastReceiver sdcardBroadcastReceiver = null;

	private void registerSdcardBroadcastReceiver(){

		OppLog.i( "OppService.registerSdcardBroadcastReceiver()[+]" );
		
		this.sdcardBroadcastReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive( Context context, Intent intent ){

				OppLog.i( "OppService.BroadcastReceiver.onReceive()[+]");

				Uri path = intent.getData();
				if( path != null ){

					OppLog.d( "OppService: path[" + path.getPath() + "], oppc[" + oppcCurrentStoragePath + "], opps[" + oppsCurrentStoragePath + "]" );

					File oppcCurStorage = SystemUtils.getExternalStorageDirectory( context, oppcCurrentStoragePath );
					if( oppcCurStorage != null && oppcCurStorage.getAbsolutePath().equals(path.getPath()) ){
						oppcDisconnectNative();
					}
					File oppsCurStorage = SystemUtils.getExternalStorageDirectory( context, oppsCurrentStoragePath );
					if( oppsCurStorage != null && oppsCurStorage.getAbsolutePath().equals(path.getPath()) ){
						oppsDisconnectNative();
					}
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		this.registerReceiver( this.sdcardBroadcastReceiver, intentFilter );
	}

	private void unregisterSdCardBroadcastReceiver(){

		OppLog.i( "OppService.unregisterSdCardBroadcastReceiver()[+]" );
		
		if( this.sdcardBroadcastReceiver != null ){

			this.unregisterReceiver( this.sdcardBroadcastReceiver );
		}
	}

	@Override
	public void onCreate(){

		OppLog.i( "OppService.onCreate()[+]" );

		// required for object initialization and check the result (or ANR: ALPS00092662)
		super.onCreate();
		if( !this.isServiceNativeEnabled ){

			OppLog.w( "OppService native onCreate failed." );
			// service int failed -> stop self
			BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_ABNORMAL );
			BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_ABNORMAL );
			this.stopSelf();
			return;
		}

		// initialization
		this.oppManager = OppManager.getInstance(this);
		this.oppManager.setOppService(this);
		this.oppManager.cancelAllNotification();

		// register sdcard broadcast receiver
		this.registerSdcardBroadcastReceiver();
		
		// enable oppc ( context init ) => must be called before worker-thread start
		BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_ENABLING );
		if( this.oppcEnable() ){

			BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_ENABLED );
		}
		else {
			BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_ABNORMAL );
		}

		BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_ENABLING );
		if( this.oppsEnable() ){

			BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_ENABLED );
		}
		else {
			BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_ABNORMAL );
		}

		// start worker thread
		this.oppcWorker = new OppTaskWorkerThread( "BtOppc", new OppcTaskHandler() );
		this.oppcWorker.start();
		this.oppsWorker = new OppTaskWorkerThread( "BtOpps", new OppsTaskHandler() );
		this.oppsWorker.start();

		// reset task
		// If pengding too much task in content provider, ANR will happen
		//this.oppManager.oppcResetTaskState();
		//this.oppManager.oppsResetTaskState();
  
       
		// process pending oppc task(s)
		//this.oppcWorker.notifyNewTask();

		OppLog.i( "OppService.onCreate()[-]" );
	}

	@Override
	public void onDestroy(){

		OppLog.i( "OppService.onDestroy()[+]" );

		if( this.isServiceNativeEnabled ){
		
			// cancel all notification (user action entry point)
			this.oppManager.oppOnServiceStop();

			// stop worker thread => must be called before diable (or event maybe received)
			OppLog.d( "OppService.onDestroy() interrupt OppTaskWorkerThread..." );
			this.oppcWorker.interrupt();
			this.oppsWorker.interrupt();

			this.isTaskWorkThreadInteruppted = true;

			// disable opp service
			OppLog.d( "OppService.onDestroy() disable oppc/opps native service..." );
			if( this.oppcDisable() ){

				BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_DISABLED );
			}
			else {
				BluetoothOppService.sendActivationBroadcast( this, true, BluetoothOppService.STATE_ABNORMAL );
			}
			if( this.oppsDisable() ){

				BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_DISABLED );
			}
			else {
				BluetoothOppService.sendActivationBroadcast( this, false, BluetoothOppService.STATE_ABNORMAL );
			}

			// register sdcard broadcast receiver
			this.unregisterSdCardBroadcastReceiver();
		}

		// required for object destroy
		OppLog.d( "OppService.onDestroy() call native destroy()..." );
		super.onDestroy();	// OppServiceNative.onDestroy()

		// reset OppService
		if( this.oppManager != null ){

			this.oppManager.setOppService( null );
		}

		OppLog.i( "OppService.onDestroy()[-]" );
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ){

		OppLog.i( "OppService.onStartCommand()[+]" );

		// action
		if( this.isServiceNativeEnabled && intent != null ){

			String action = intent.getAction();
			OppLog.d( "OppService.onStartCommand() action: " + action );

			// oppc
			if( OppConstants.OppService.ACTION_OPPC_START.equals(action) ){

				this.oppcWorker.notifyNewTask();
			}
			// opps
			else if( OppConstants.OppService.ACTION_OPPS_START.equals(action) ){

				// stop listen before handle opps task
				this.oppsStopListenDisconnect();
				this.oppsWorker.notifyNewTask();
			}
			else {
				//OppLog.e( "OppService.onStartCommand unsupported action: " + action );
				// default start opps: from BluetoothReceiver
				this.oppsWorker.notifyNewTask();
			}
		}
		else {
			OppLog.w( "OppService.onStartCommand() warn: isServiceNativeEnabled[" + this.isServiceNativeEnabled + "] or null Intent" );
		}

		OppLog.i( "OppService.onStartCommand()[-]" );
		return super.onStartCommand( intent, flags, startId );
	}

	/******************************************************************************************************
	 * OPPC Service Implementation
	 ******************************************************************************************************/
	class OppcTaskHandler implements OppTaskHandler {

		public boolean beforeWait() throws InterruptedException{

			// oppc task is from UI (user)
          
			// check pending task will remove to oppc thread to avoid ANR in OPP Service (ANR: ALPS00231774)
			if( !OppService.this.isOppcResetTask ){
				
				OppService.this.isOppcResetTask = true;
				
				OppLog.d( "oppc beforeWait() - oppcResetTaskState() " );
				OppService.this.oppManager.oppcResetTaskState();
				
				// begain to process oppc pending task
				ContentResolver contentResolver = OppService.this.getContentResolver();

			    // query all pending tasks
			    Cursor cursor = contentResolver.query(
					BluetoothShareTaskMetaData.CONTENT_URI,
					new String[]{ BluetoothShareTaskMetaData._ID },
					BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " + BluetoothShareTaskMetaData.TASK_STATE + " = ?",
					new String[]{
							Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_START ),
							Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_END ),
							Integer.toString( BluetoothShareTask.STATE_PENDING )
					},
					BluetoothShareTaskMetaData._ID + " ASC" );

			    // construct result list
			    List<Uri> newTaskList = Collections.emptyList();
			    try {
				    newTaskList = OppService.this.oppManager.getOppTaskList(cursor);
				    OppLog.d( "oppc beforeWait() - task count: " + newTaskList.size() );
			    }
			    finally {
				    if( cursor != null ){
				    	cursor.close();
				    	cursor = null;
				    }
			    }

			    // acquire wake-lock
			    OppService.this.oppManager.acquireWakeLock();
			    try {
				    // loop for all tasks
				    for( Uri newTask : newTaskList ){

					    OppLog.d( " oppc beforeWait() processing task: " + newTask );
					    this.processBatchPush( newTask );
				    }
			    }
			    finally {
				    // release wake-lock
				    OppService.this.oppManager.releaeWakeLock();
		    	}
			}
	
			return true;
		}

		public void afterWait() throws InterruptedException {

			ContentResolver contentResolver = OppService.this.getContentResolver();

			// query all pending tasks
			Cursor cursor = contentResolver.query(
					BluetoothShareTaskMetaData.CONTENT_URI,
					new String[]{ BluetoothShareTaskMetaData._ID },
					BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " + BluetoothShareTaskMetaData.TASK_STATE + " = ?",
					new String[]{
							Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_START ),
							Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_END ),
							Integer.toString( BluetoothShareTask.STATE_PENDING )
					},
					BluetoothShareTaskMetaData._ID + " ASC" );

			// construct result list
			List<Uri> newTaskList = Collections.emptyList();
			try {
				newTaskList = OppService.this.oppManager.getOppTaskList(cursor);
				OppLog.d( "oppc afterWait() - task count: " + newTaskList.size() );
			}
			finally {
				if( cursor != null ){
					cursor.close();
					cursor = null;
				}
			}

			// acquire wake-lock
			OppService.this.oppManager.acquireWakeLock();
			try {
				// loop for all tasks
				for( Uri newTask : newTaskList ){

					OppLog.d( " oppc afterWait() processing task: " + newTask );
					this.processBatchPush( newTask );
				}
			}
			finally {
				// release wake-lock
				OppService.this.oppManager.releaeWakeLock();
			}
		}

		private void processBatchPush( Uri taskUri ) throws InterruptedException {

			// attributes for content-provider operations
			ContentResolver contentResolver = OppService.this.getContentResolver();
			String pendingTaskWhere = BluetoothShareTaskMetaData.TASK_STATE + "=" + BluetoothShareTask.STATE_PENDING;

			// query current task and get batch peerAddr and timestamp
			BluetoothShareTask initTask = null;
			Cursor cursor = contentResolver.query( taskUri, null, pendingTaskWhere, null, null );
			try {
				if( cursor == null || !cursor.moveToFirst() ){

					return;	// e.g. aborted by user
				}
				initTask = new BluetoothShareTask( cursor );
			}
			finally {
				if( cursor != null ){
					cursor.close();
					cursor = null;
				}
			}

			// query all tasks in the same batch
			cursor = contentResolver.query(
					BluetoothShareTaskMetaData.CONTENT_URI,
					new String[]{ BluetoothShareTaskMetaData._ID },
					BluetoothShareTaskMetaData.TASK_TYPE + " = ? AND " + 
					BluetoothShareTaskMetaData.TASK_STATE + " = ? AND " +
					BluetoothShareTaskMetaData.TASK_PEER_ADDR + " = ? AND " +
					BluetoothShareTaskMetaData.TASK_CREATION_DATE + " = ?",
					new String[]{
							Integer.toString( BluetoothShareTask.TYPE_OPPC_PUSH ),
							Integer.toString( BluetoothShareTask.STATE_PENDING ),
							initTask.getPeerAddr(),
							Long.toString( initTask.getCreationDate() )
					},
					BluetoothShareTaskMetaData._ID + " ASC" );

			// construct result list
			List<Uri> batchTaskList = Collections.emptyList();
			try {
				batchTaskList = OppService.this.oppManager.getOppTaskList(cursor);
				OppLog.d( "oppc processBatchPush() - task count: " + batchTaskList.size() );
			}
			finally {
				if( cursor != null ){
					cursor.close();
					cursor = null;
				}
			}

			// no task in this batch (shouldn't happen)
			if( batchTaskList.size() < 1 )	return;

			// connect to peer device
			boolean isConnected = OppService.this.oppcConnect( initTask.getPeerAddr() );
			boolean isDisconnected = false;

			// loop all batch tasks
			try {
				// send state changed broadcast: connected
				if( isConnected ){
					BluetoothOppService.sendStateChangedBroadcast( OppService.this, initTask, true );
				}

				// constants for task update
				ContentValues values = new ContentValues();
				values.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_ONGOING );

				for( Uri newTask : batchTaskList ){

					if( OppService.this.isTaskWorkThreadInteruppted ){
                        OppLog.i( "OppTaskWorkerThread had been interuppted, stop current task." );

						// after disable oppservice, need not disconnect by upper layer   
						isDisconnected = true;
						break;
					}
						
					OppLog.d( " oppc processBatchPush() processing task: " + newTask );

					// update content provider: state = ongoing
					int count = contentResolver.update( newTask, values, pendingTaskWhere, null );
					if( count != 1 ){
						OppLog.i( "skip non-pending task: " + newTask );
						continue;
					}

					// lock current task ( can accept abort request )
					OppService.this.oppcSetCurrentTask( newTask );

					// query new state of task
					cursor = contentResolver.query(
							newTask,
							null,	// all columns
							null,
							null,
							null );

					BluetoothShareTask task = null;
					try {
						if( cursor == null || !cursor.moveToFirst() ){
	
							// e.g. aborted by user
							continue;
						}
						task = new BluetoothShareTask( cursor );

						// task maybe canceled after oppcSetCurrentTask() but before task query()
						if( task.getState() == BluetoothShareTask.STATE_ABORTING ){

							BtLog.i( "handle aborting task before push it." );
							task.setState( BluetoothShareTask.STATE_ABORTED );
							this.onObjectChange( task );
							continue;
						}

						// start monitor eject broadcast first 
						oppcCurrentStoragePath = task.getData();

						// isStorageMounted = InternalStorage || MountedExternalStorage
						boolean isStorageMounted = 
							( SystemUtils.getExternalStorageDirectory(OppService.this, oppcCurrentStoragePath) == null ) || 
							SystemUtils.isExternalStorageMounted(OppService.this, oppcCurrentStoragePath);
						
						if( isConnected && !isDisconnected && isStorageMounted ){

							// open uri data for task
							UriDataUtils.openUriData( OppService.this, Uri.parse( task.getObjectUri() ) );
							// call push api
							isDisconnected = !OppService.this.oppcPush( task, this );
							oppcCurrentStoragePath = null;
							// close uri data
							UriDataUtils.closeUriData( OppService.this, Uri.parse( task.getObjectUri() ) );
						}
						else {
							// batch connection failed => all task fail
							task.setState( BluetoothShareTask.STATE_FAILURE );
							this.onObjectChange( task );
						}
					}
					finally {
						// reset current task
						oppcCurrentStoragePath = null;
						OppService.this.oppcSetCurrentTask( null );
						if( cursor != null ){
							cursor.close();
							cursor = null;
						}
					}
				}
			}
			finally {
				// send state changed broadcast: disconnected
				if( isConnected ){
					if( ( !isDisconnected ) && ( !OppService.this.isTaskWorkThreadInteruppted ) )	OppService.this.oppcDisconnect();
					BluetoothOppService.sendStateChangedBroadcast( OppService.this, initTask, false );
					// sleep and let bttask can handle disconnect properly
					Thread.sleep( 660 );
				}
			}
			
		}

		public void onObjectChange( BluetoothShareTask task ){

			if( Options.LL_DEBUG ){

				OppLog.d( "oppc onObjectChange() for taskId[" + task.getId() + "], state[" + task.getState() + "]" );
			}

			task.setModifiedDate( System.currentTimeMillis() );

			// notify
			OppService.this.oppManager.notifyOppTask( task );

			// update content provider
			if ( task.getState() == BluetoothShareTask.STATE_ONGOING && task.getDoneBytes() != 0 ){

				// skip progress update event => don't update db, update ui only
				// do db update for every progress update will cause:
				// 1. MMI thread (OppTaskWorkerThread) slower than EXT Thread (MessageListener)
				// 2. UI progress display is out of sync.
			}
			else {
				OppService.this.getContentResolver().update( task.getTaskUri(), task.getContentValues(), null, null );
			}
		}
	}

	/******************************************************************************************************
	 * OPPS Service Implementation
	 ******************************************************************************************************/
	class OppsTaskHandler implements OppTaskHandler {

		// used to verify current access request is processed
		boolean isBusy = false;

		/**
		 * before waiting for user confirmation
		 */
		public boolean beforeWait() throws InterruptedException {
			
			if( !OppService.this.isOppsResetTask ){
				
				OppService.this.isOppsResetTask = true;

				// reset task(delete DB record) will remove to oppsWorkThread to avoid ANR in OPP Service (ANR: ALPS00268876) 
				OppLog.d( "opps beforeWait() - oppsResetTaskState() " );
				OppService.this.oppManager.oppsResetTaskState();
			}
			
			if( this.isBusy )	return true;

			// waiting for incoming request
			OppEvent ind = OppService.this.oppsWaitForAccessRequest();
			String[] args = ind.parameters;

			// start activity to handle request confirmation
			if( ind.event == OppEvent.BT_OPPS_PUSH_ACCESS_REQUEST ){

				// pupup notification & dialog to confirm request
				boolean isConfirmed = OppService.this.oppManager.oppsStartPushActivity( OppService.this.oppsIsAuthorized(), args[0], args[1], args[2], Long.parseLong(args[3]) );
				this.isBusy = true;

				// listen disconnect event from stack
				if( !isConfirmed ){

					boolean isDisconnected = OppService.this.oppsListenDisconnect();
					if( isDisconnected ){
						// connection timeout or canceled by peer device
						isBusy = false;	// request done
						OppService.this.oppManager.oppsCancelPushActivity();
						return false;
					}
				}

				// user confirmed the request (accept or reject)
				return true;
			}
			else {
				OppLog.e( "opps beforeWait(): get unsupported event(oppsRequestIndication)" );
				OppService.this.oppsAccessResponseNative( OppConstants.GOEP.NOT_IMPLEMENTED, new String[]{ "0", "" } );
				this.isBusy = false;
				return false;	// wait for next indication
			}
		}

		public void afterWait() throws InterruptedException {

			// query all pending tasks
			Cursor cursor = OppService.this.getContentResolver().query(
					BluetoothShareTaskMetaData.CONTENT_URI,
					new String[]{ BluetoothShareTaskMetaData._ID },
					BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " + BluetoothShareTaskMetaData.TASK_STATE + " in ( ?, ? )",
					new String[]{
							Integer.toString( BluetoothShareTask.TYPE_OPPS_GROUP_START ),
							Integer.toString( BluetoothShareTask.TYPE_OPPS_GROUP_END ),
							Integer.toString( BluetoothShareTask.STATE_PENDING ),
							Integer.toString( BluetoothShareTask.STATE_REJECTING )
					},
					BluetoothShareTaskMetaData._ID + " ASC" );

			// construct result list
			List<Uri> newTaskList = Collections.emptyList();
			try {
				newTaskList = OppService.this.oppManager.getOppTaskList(cursor);
				OppLog.d( "opps afterWait(): task count: " + newTaskList.size() );
			}
			finally {

				if( cursor != null ){

					cursor.close();
					cursor = null;
				}
			}

			// acquire wake-lock
			OppService.this.oppManager.acquireWakeLock();
			try {
				for( Uri newTask : newTaskList ){

					OppLog.d( "opps afterWait() processing task:" + newTask );

					// lock cuurent task
					OppService.this.oppsSetCurrentTask( newTask );

					// query new state of task
					cursor = OppService.this.getContentResolver().query(
							newTask,
							null,	// all columns
							BluetoothShareTaskMetaData.TASK_STATE + "=" + BluetoothShareTask.STATE_PENDING + " OR " +
							BluetoothShareTaskMetaData.TASK_STATE + "=" + BluetoothShareTask.STATE_REJECTING,
							null,
							null );

					BluetoothShareTask task = null;
					try {
						if( cursor == null || !cursor.moveToFirst() ){

							// maybe canceled by user
							continue;
						}
						task = new BluetoothShareTask( cursor );

						// send state changed broadcast: connected
						BluetoothOppService.sendStateChangedBroadcast( OppService.this, task, true );
						// call push api
						oppsCurrentStoragePath = task.getData();
						OppService.this.oppsAccessResponse( task, this );
						OppService.this.oppsSetCurrentTask( null );
					}
					finally {
						oppsCurrentStoragePath = null;
						if( task != null ){

							// send state changed broadcast: disconnected
							BluetoothOppService.sendStateChangedBroadcast( OppService.this, task, false );
						}

						if( cursor!= null )	cursor.close();
					}
				}
			}
			finally {
				// release wake-lock
				OppService.this.oppManager.releaeWakeLock();

				// request done
				this.isBusy = false;
			}
		}

		public void onObjectChange(BluetoothShareTask task) {

			if( Options.LL_DEBUG ){

				OppLog.d( "opps onObjectChange() for taskId=" + task.getId() + ",state=" + task.getState() );
			}

			task.setModifiedDate( System.currentTimeMillis() );

			OppService.this.oppManager.notifyOppTask( task );

			// update content provider
			if ( task.getState() == BluetoothShareTask.STATE_ONGOING && task.getDoneBytes() != 0 ){

				// skip progress update event => don't update db, update ui only
				// do db update for every progress update will cause:
				// 1. MMI thread (OppTaskWorkerThread) slower than EXT Thread (MessageListener)
				// 2. UI progress display is out of sync.
			}
			else {
				OppService.this.getContentResolver().update( task.getTaskUri(), task.getContentValues(), null, null );
			}
		}
	}
}
