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

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;

import com.mediatek.bluetooth.Options;
import com.mediatek.bluetooth.opp.mmi.OppLog;
import com.mediatek.bluetooth.opp.mmi.Utils;
import com.mediatek.bluetooth.share.BluetoothShareTask;

/**
 * Objective: using native services (JNI, async) to provide a sync-version API
 */
public class OppServiceNative extends Service {

	private static final String NATIVE_LIB = "extopp_jni";

	// thread for receiving message from bt-task
	private MessageListener messageListener = null;

	// OPPC task state (for abort handling)
	private TaskState oppcTaskState = TaskState.Idle;
	// OPPC Event queue ( messageListener will add message into this queue )
	private EventQueue<OppEvent> oppcEventQueue = new EventQueue<OppEvent>(OppConstants.OPPC_OPERATION_TIMEOUT, OppConstants.OPPC_OPERATION_RETURN_THRESHOLD);

	// OPPS task state (for abort handling)
	private TaskState oppsTaskState = TaskState.Idle;
	// OPPS Event queue ( messageListener will add message into this queue )
	private EventQueue<OppEvent> oppsEventQueue = new EventQueue<OppEvent>(OppConstants.OPPS_OPERATION_TIMEOUT, OppConstants.OPPS_OPERATION_RETURN_THRESHOLD);

	protected boolean isServiceNativeEnabled = false;
	
	/****************************************************************************************************
	 * Android Service Implementation
	 ****************************************************************************************************/

	@Override
	public void onCreate(){

		super.onCreate();

		// native init
		this.objectInitNative();	// object level initialization
		this.isServiceNativeEnabled = this.enableServiceNative();	// setup communication channel with BT-Task
		if( this.isServiceNativeEnabled ){

			// listen message from bttask
			if( this.messageListener == null )
			{
				this.messageListener = new MessageListener();
				this.messageListener.startup();
			}
		}
	}

	@Override
	public void onDestroy() {

		// stop listen message from bttask
		if( this.isServiceNativeEnabled ){
            /*
			if( this.messageListener != null ){
				this.messageListener.shutdown();
                // wait message listener thread exit
                try{
					this.messageListener.join(1000);
					OppLog.w( "message listener join returned" );
				}
                catch( InterruptedException ex ){
			        OppLog.w( "message listener interrupted" );
                }				
			}	
                   */
			// native deinit
			this.isServiceNativeEnabled = false;
			this.disableServiceNative();	// release communication channel with BT-Task
		}
		this.objectDeinitNative();	// object level de-initialization
		super.onDestroy();
	}

	@Override
	public IBinder onBind( Intent intent ){

		OppLog.w( "unsupported function: OppServiceNative.onBind()" );
		return null;
	}

	/**
	 * callback from jni ( external adaptation )
	 * 
	 * @param event
	 * @param parameters
	 */
	@SuppressWarnings("unused")
	private void jniCallback( int event, String[] parameters ){

		OppEvent newEvent = new OppEvent(event, parameters);

		// log callback info
		if( Options.LL_DEBUG ){

			OppLog.d( "jni cb event: " + newEvent.toString() );
		}

		// oppc event
		if( OppEvent.BT_OPPC_GROUP_START < event && event < OppEvent.BT_OPPC_GROUP_END ){

			this.oppcEventQueue.notifyNewEvent( newEvent );
		}
		// opps event
		else if( OppEvent.BT_OPPS_GROUP_START < event && event < OppEvent.BT_OPPS_GROUP_END ){

			this.oppsEventQueue.notifyNewEvent( newEvent );
		}
		else {
			OppLog.e( "invalid jni cb event[" + event + "]" );
		}
	}

	/****************************************************************************************************
	 * OPPC API
	 ****************************************************************************************************/

	/**
	 * TODO [OPP] before return => check queue is empty => if not, event is abnormal ( non-final event but return ).
	 * DISCONNECT event is normal case 
	 */
	private void checkQueue( EventQueue<OppEvent> queue ){

		if( queue.size() != 0 ){

			OppLog.w( "queue size[" + queue.size() + "]: " + queue.getPrintableString() );
		}
	}

	/**
	 * used to enable opp client
	 * 
	 * @return
	 */
	public boolean oppcEnable(){

		this.checkQueue( this.oppcEventQueue );
		this.oppcEventQueue.clear();
		this.oppcEnableNative();	// async call
		try {
			while( true ){

				OppEvent event = this.oppcEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppcEnable get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPC_ENABLE_SUCCESS ){

					this.checkQueue( this.oppcEventQueue );
					return true;
				}
				else if( event.event == OppEvent.BT_OPPC_ENABLE_FAIL ){

					this.checkQueue( this.oppcEventQueue );
					return false;
				}
				else {
					OppLog.e( "invalid oppcEnable event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppcEnable thread interrupted" );
			return false;
		}
	}

	/**
	 * used to disable opp client
	 * 
	 * @return
	 */
	public boolean oppcDisable(){

		this.checkQueue( this.oppcEventQueue );
		this.oppcEventQueue.clear();
		this.oppcDisableNative();	// async call
		try {
			while( true ){
				OppEvent event = this.oppcEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppcDisable get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPC_DISABLE_SUCCESS ){

					this.checkQueue( this.oppcEventQueue );
					return true;
				}
				else if( event.event == OppEvent.BT_OPPC_DISABLE_FAIL ){

					this.checkQueue( this.oppcEventQueue );
					return false;
				}
				else {
					OppLog.e( "invalid oppcDisable event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppcDisable thread interrupted" );
			return false;
		}
	}

	private Uri oppcCurrentTask;

	public void oppcSetCurrentTask( Uri task ){

		this.oppcCurrentTask = task;
	}

	public Uri oppcGetCurrentTask(){

		return this.oppcCurrentTask;
	}

	public boolean oppcConnect( String peerAddr ){

		this.checkQueue( this.oppcEventQueue );
		this.oppcEventQueue.clear();
		this.oppcConnectNative( peerAddr );	// async call
		try {
			while( true ){
				OppEvent event = this.oppcEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppcConnect get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPC_CONNECTED ){

					this.checkQueue( this.oppcEventQueue );
					return true;
				}
				else if( event.event == OppEvent.BT_OPPC_DISCONNECT ){

					// disconnect => connect failed
					this.checkQueue( this.oppcEventQueue );
					return false;
				}
				else {
					OppLog.e( "invalid oppcConnect event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppcConnect thread interrupted" );
			return false;
		}
	}

	public boolean oppcDisconnect(){

		this.checkQueue( this.oppcEventQueue );
		this.oppcEventQueue.clear();
		this.oppcDisconnectNative();	// async call
		try {
			while( true ){
				OppEvent event = this.oppcEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppcDisconnect get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPC_DISCONNECT ){

					this.checkQueue( this.oppcEventQueue );
					return true;
				}
				else {
					OppLog.e( "invalid oppcDisconnect event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppcDisconnect thread interrupted" );
			return false;
		}
	}

	/**
	 * used to cancel current blocking operation: oppcPushObject()
	 */
	public boolean oppcAbort( Uri task ){

		if( task == null || !task.equals(this.oppcCurrentTask) ){

			return false;
		}

		if( this.oppcTaskState != TaskState.Aborted ){

			// needn't to cancel stack because stack is waiting for ack
			this.oppcTaskState = TaskState.Aborting;
		}
		return true;
	}

	/**
	 * used to push object to remote bluetooth device (only push, NOT includes: connect and disconnect )
	 * 
	 * @param task
	 * @param handler
	 * @return
	 * @throws InterruptedException
	 */
	public boolean oppcPush( BluetoothShareTask task, OppTaskHandler handler ) throws InterruptedException {

		this.checkQueue( this.oppcEventQueue );

		OppLog.d( "oppc push object: " + task.getPrintableString() );

		// clear event queue
		this.oppcEventQueue.clear();

		// set task state to running (maybe oppcAbort() called)
		if( this.oppcTaskState == TaskState.Idle ){

			this.oppcTaskState = TaskState.Running;
		}

		// call native function (async)
		this.oppcPushNative( task.getMimeType(), task.getObjectName(), task.getData() );

		// local variables
		int lastProgress = 0;
		long lastUpdateTime = 0;
		OppEvent event;
		String[] parameters;
		while( true ){

			// eventually the BT_OPPS_DISCONNECT will happen
			if( this.oppcTaskState == TaskState.Aborting ){

				// aborted => command sent
				this.oppcTaskState = TaskState.Aborted;
				this.oppcAbortNative();
			}

			// process next event
			event = this.oppcEventQueue.waitNewEvent();
			if( event == null ){

				OppLog.w( "oppcPush get NULL event (no available event and return-threshold is reach)" );
				this.oppcDisconnectNative();
				continue;
			}

			switch( event.event ){
				case OppEvent.BT_OPPC_PROGRESS_UPDATE:
					if( this.oppcTaskState == TaskState.Running ){

						// let update period > 800ms
						long now = SystemClock.uptimeMillis();
						if( (now - lastUpdateTime) > 800 ){

							lastUpdateTime = now;
						}
						else {
							continue;
						}

						// update task
						parameters = event.parameters;
						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( Long.parseLong( parameters[0] ) );
						task.setTotalBytes( Long.parseLong( parameters[1] ) );

						// reduce the progress update frequency for large file ( max: 100 times )
						int curProgress = (int)(task.getDoneBytes()*100L / task.getTotalBytes());
						if( lastProgress < curProgress ){

							lastProgress = curProgress;
							handler.onObjectChange( task );
						}
					}
					break;
				case OppEvent.BT_OPPC_PUSH_START:
					if( this.oppcTaskState == TaskState.Running ){

						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( 0 );
						handler.onObjectChange( task );
					}
					break;
				case OppEvent.BT_OPPC_PUSH_SUCCESS:
					// override abort if stack finished this task (remote device received)
					this.oppcTaskState = TaskState.Idle;
					task.setState( BluetoothShareTask.STATE_SUCCESS );
					handler.onObjectChange( task );
					return true;
				case OppEvent.BT_OPPC_CONNECTED:
					// aborted indication
					this.oppcTaskState = TaskState.Idle;
					task.setState( BluetoothShareTask.STATE_ABORTED );
					handler.onObjectChange( task );
					return true;
				case OppEvent.BT_OPPC_PUSH_FAIL:
					this.oppcTaskState = TaskState.Idle;
					parameters = event.parameters;
					task.setState( BluetoothShareTask.STATE_FAILURE );
					//task.setResult( "GOEP" + Integer.parseInt( parameters[0] ) );
					OppLog.w( "oppcPushObject - push response: GOEP RSP[" + Utils.getGoepResponseCodeString( parameters[0] ) + "]" );
					handler.onObjectChange( task );
					return true;
				case OppEvent.BT_OPPC_DISCONNECT:
					// user cancel will need disconnect event to confirm the result
					this.oppcTaskState = TaskState.Idle;
					task.setState( BluetoothShareTask.STATE_FAILURE );
					handler.onObjectChange( task );
					return false;
				default:
					OppLog.e( "oppcPushObject invalid event: [" + event.event + "]" );
			}
		}
	}
	
	/**
	 * used to push object to remote bluetooth device (it includes: connect / push / disconnect )
	 * 
	 * @param task
	 * @param handler
	 * @return
	 * @throws InterruptedException
	 */
	public boolean oppcPushObject( BluetoothShareTask task, OppTaskHandler handler ) throws InterruptedException {

		this.checkQueue( this.oppcEventQueue );

		OppLog.d( "oppc push object: " + task.getPrintableString() );

		// clear event queue
		this.oppcEventQueue.clear();

		// set task state to running (maybe oppcAbort() called)
		if( this.oppcTaskState == TaskState.Idle ){

			this.oppcTaskState = TaskState.Running;
		}

		// call native function (async)
		this.oppcPushObjectNative( task.getPeerAddr(), task.getMimeType(), task.getObjectName(), task.getData() );

		// local variables
		int lastProgress = 0;
		long lastUpdateTime = 0;
		boolean result = false;
		OppEvent event;
		String[] parameters;
		while( true ){

			// eventually the BT_OPPS_DISCONNECT will happen
			if( this.oppcTaskState == TaskState.Aborting ){

				// aborted => command sent
				this.oppcTaskState = TaskState.Aborted;

				// no disconnect event in queue (or the connection is disconnected)
				if( !this.oppcEventQueue.contains( new OppEvent( OppEvent.BT_OPPC_DISCONNECT, null ) ) ){

					this.oppcDisconnectNative();
				}
			}

			// process next event
			event = this.oppcEventQueue.waitNewEvent();
			if( event == null ){

				OppLog.w( "oppcPushObject get NULL event (no available event and return-threshold is reach)" );
				this.oppcDisconnectNative();
				continue;
			}
			switch( event.event ){
				case OppEvent.BT_OPPC_PROGRESS_UPDATE:
					if( this.oppcTaskState == TaskState.Running ){

						// let update period > 800ms
						long now = SystemClock.uptimeMillis();
						if( (now - lastUpdateTime) > 800 ){

							lastUpdateTime = now;
						}
						else {
							continue;
						}

						// update task
						parameters = event.parameters;
						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( Long.parseLong( parameters[0] ) );
						task.setTotalBytes( Long.parseLong( parameters[1] ) );

						// reduce the progress update frequency for large file ( max: 100 times )
						int curProgress = (int)(task.getDoneBytes()*100L / task.getTotalBytes());
						if( lastProgress < curProgress ){

							lastProgress = curProgress;
							handler.onObjectChange( task );
						}
					}
					break;
				case OppEvent.BT_OPPC_PUSH_START:
					if( this.oppcTaskState == TaskState.Running ){

						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( 0 );
						handler.onObjectChange( task );
					}
					break;
				case OppEvent.BT_OPPC_PUSH_SUCCESS:
					// override abort if stack finished this task (remote device received)
					this.oppcTaskState = TaskState.Running;
					task.setState( BluetoothShareTask.STATE_SUCCESS );
					handler.onObjectChange( task );
					result = true;
					break;
				case OppEvent.BT_OPPC_PUSH_FAIL:
					if( this.oppcTaskState == TaskState.Running ){

						parameters = event.parameters;
						task.setState( BluetoothShareTask.STATE_FAILURE );
						//task.setResult( "GOEP" + Integer.parseInt( parameters[0] ) );
						OppLog.w( "oppcPushObject - push response: GOEP RSP[" + Utils.getGoepResponseCodeString( parameters[0] ) + "]" );
						handler.onObjectChange( task );
						result = false;
						break;
					}
					break;
				case OppEvent.BT_OPPC_DISCONNECT:
					result = this.oppcFinalEvent( task, result );
					handler.onObjectChange( task );
					return result;
				default:
					OppLog.e( "oppcPushObject invalid event: [" + event.event + "]" );
			}
		}
	}

	private boolean oppcFinalEvent( BluetoothShareTask task, boolean result ){

		// canceled by user
		if( this.oppcTaskState != TaskState.Running ){

			task.setState( BluetoothShareTask.STATE_ABORTED );
		}
		// normal transfer result
		else if( !result ){

			task.setState( BluetoothShareTask.STATE_FAILURE );
		}

		// final event
		this.oppcTaskState = TaskState.Idle;
		this.checkQueue( this.oppcEventQueue );
		return result;
	}

	/****************************************************************************************************
	 * OPPS API
	 * @throws InterruptedException
	 ****************************************************************************************************/

	// keep the authorization state for connection
	boolean isAuthorized = false;

	/**
	 * used to enable opp server
	 * 
	 * @return
	 */
	public boolean oppsEnable(){

		this.checkQueue( this.oppsEventQueue );

		this.isAuthorized = false;
		this.oppsEventQueue.clear();
		this.oppsEnableNative();	// async call
		try {
			while( true ){
				OppEvent event = this.oppsEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppsEnable get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPS_ENABLE_SUCCESS ){

					this.checkQueue( this.oppsEventQueue );
					return true;
				}
				else if( event.event == OppEvent.BT_OPPS_ENABLE_FAIL ){

					this.checkQueue( this.oppsEventQueue );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPS_DISABLE_SUCCESS ){

					// skip event for pre-disable operation (for abnormal restart)
				}
				else {
					OppLog.e( "invalid oppsEnable event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppsEnable thread interrupted" );
			return false;
		}
	}

	/**
	 * used to disable opp server
	 * 
	 * @return
	 */
	public boolean oppsDisable(){

		this.checkQueue( this.oppsEventQueue );

		this.oppsEventQueue.clear();
		this.oppsDisableNative();	// async call
		try {
			while( true ){
				OppEvent event = this.oppsEventQueue.waitNewEvent();
				if( event == null ){

					OppLog.e( "oppsDisable get NULL event (no available event and return-threshold is reach)" );
					return false;
				}
				else if( event.event == OppEvent.BT_OPPS_DISABLE_SUCCESS ){

					this.checkQueue( this.oppsEventQueue );
					return true;
				}
				else if( event.event == OppEvent.BT_OPPS_DISABLE_FAIL ){

					this.checkQueue( this.oppsEventQueue );
					return false;
				}
				else {
					OppLog.e( "invalid oppsDisable event: [" + event.event + "]" );
				}
			}
		}
		catch( InterruptedException ex ){

			OppLog.w( "oppsDisable thread interrupted" );
			return false;
		}
	}

	/**
	 * wait for access request from opp client
	 * 
	 * @return OppUiEvent.BT_OPPS_PUSH_ACCESS_REQUEST / OppUiEvent.BT_OPPS_PULL_ACCESS_REQUEST
	 * @throws InterruptedException
	 */
	public OppEvent oppsWaitForAccessRequest() throws InterruptedException {

		// loop until valid event
		while( true ){

			OppEvent event = this.oppsEventQueue.waitNewEvent(0);	// waitTimeout = 0 (no timeout)
			if( event == null ){

				// oppsStopListenDisconnect be called after task is finished ( Thread is too slow )
				continue;
			}
			switch( event.event ){
				case OppEvent.BT_OPPS_PUSH_ACCESS_REQUEST:
				case OppEvent.BT_OPPS_PULL_ACCESS_REQUEST:
					if( this.oppsTaskState == TaskState.Idle ){

						this.oppsTaskState = TaskState.Running;	// reset abort flag
					}
					return event;
				case OppEvent.BT_OPPS_DISCONNECT:
					// ignore previous request disconnect
					isAuthorized = false;
					continue;
				default:
					OppLog.e( "invalid oppsWaitForAccessRequest event: [" + event.event + "]" );
			}
		}
	}

	boolean isListenDisconnect = false;
	public boolean oppsListenDisconnect() throws InterruptedException {

		isListenDisconnect = true;
		OppEvent event = this.oppsEventQueue.waitNewEvent(0);	// waitTimeout = 0 (no timeout)
		isListenDisconnect = false;

		if( event != null && event.event == OppEvent.BT_OPPS_DISCONNECT ){

			if( Options.LL_DEBUG ){

				OppLog.d( "oppsListenDisconnect(): disconnect event happened!" );
			}

			// disconnected
			isAuthorized = false;
			return true;
		}
		else {
			// listening canceled: timeout happened
			if( event != null ){

				OppLog.w( "oppsListenDisconnect() - unexpected event:" + event );
			}
			return false;
		}
	}

	public void oppsStopListenDisconnect(){

		if( Options.LL_DEBUG ){

			OppLog.d( "oppsStopListenDisconnect(): is listening=[" + isListenDisconnect + "]" );
		}

		if( isListenDisconnect ){

			this.oppsEventQueue.cancelWaitNewEvent();
		}
	}

	private Uri oppsCurrentTask;

	public void oppsSetCurrentTask( Uri task ){

		this.oppsCurrentTask = task;
	}

	public Uri oppsGetCurrentTask(){
		
		return this.oppsCurrentTask;
	}

	public boolean oppsIsAuthorized(){

		return this.isAuthorized;
	}

	/**
	 * used to cancel current blocking operation: oppsAccessResponse()
	 */
	public boolean oppsAbort( Uri task ){

		if( task == null || !task.equals(this.oppsCurrentTask) ){

			return false;
		}

		if( this.oppsTaskState != TaskState.Aborted ){

			this.oppsTaskState = TaskState.Aborting;

			// cancel stack and disconnect immediately
			this.oppsEventQueue.cancelWaitNewEvent();
		}
		return true;
	}

	/**
	 * used to respond client access request, the process status will be post back to handler
	 * 
	 * @param task
	 * @param handler
	 * @return
	 * @throws InterruptedException
	 */
	public boolean oppsAccessResponse( BluetoothShareTask task, OppTaskHandler handler ) throws InterruptedException {

		// prepare native function parameters: response-code, allow-size, filename
		int goepStatus = ( task.getState() == BluetoothShareTask.STATE_PENDING ) ? OppConstants.GOEP.STATUS_SUCCESS : OppConstants.GOEP.FORBIDDEN;
		String filename = task.getData();
		filename = ( filename == null ) ? "" : filename;
		String[] responseParams = { Long.toString( task.getTotalBytes() ), filename };

		// call native function (async)
		OppLog.d( "oppsAccessResponse: goep[" + goepStatus + "], size[" + responseParams[0] + "], file[" + responseParams[1] + "]" );
		this.oppsAccessResponseNative( goepStatus, responseParams );

		// reject access => send out PUSH_RSP and return (no more UI event) 
		if( goepStatus != OppConstants.GOEP.STATUS_SUCCESS ){

			this.oppsTaskState = TaskState.Idle;
			task.setState( BluetoothShareTask.STATE_REJECTED );
			handler.onObjectChange( task );
			return true;
		}
		else {
			isAuthorized = true;
		}

		// local variables
		int lastProgress = 0;
		long lastUpdateTime = 0;
		boolean result = false;
		OppEvent event;
		String[] parameters;

		// loop until terminate event arrival: BT_OPPS_DISCONNECT
		while( true ){

			// eventually the BT_OPPS_DISCONNECT will happen
			if( this.oppsTaskState == TaskState.Aborting ){

				this.oppsTaskState = TaskState.Aborted;

				// no disconnect event in queue (or the connection is disconnected)
				if( !this.oppsEventQueue.contains( new OppEvent( OppEvent.BT_OPPS_DISCONNECT, null ) ) ){

					this.oppsDisconnectNative();
				}
			}

			// process next event
			event = this.oppsEventQueue.waitNewEvent();
			if( event == null ){

				OppLog.w( "oppsAccessResponse get NULL event (be canceled or no available event => return-threshold is reach)" );
				this.oppsDisconnectNative();
				continue;
			}
			switch( event.event ){
				case OppEvent.BT_OPPS_PROGRESS_UPDATE:
					if( this.oppsTaskState == TaskState.Running ){

						// let update period > 800ms
						long now = SystemClock.uptimeMillis();
						if( (now - lastUpdateTime) > 800 ){

							lastUpdateTime = now;
						}
						else {
							continue;
						}

						// update task
						parameters = event.parameters;
						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( Long.parseLong( parameters[0] ) );

						// reduce the progress update frequency for large file ( max: 100 times )
						long totalBytes = task.getTotalBytes();
						// handle the done-bytes > total-bytes case
						if( task.getDoneBytes() > totalBytes ){
							totalBytes = task.getDoneBytes();
						}
						// handle total-bytes = 0 case (but actual size > 0) 
						int curProgress = 100;
						if( totalBytes > 0 ){
							curProgress = (int)(100L * task.getDoneBytes() / totalBytes);
						}
						if( lastProgress < curProgress ){

							lastProgress = curProgress;
							handler.onObjectChange( task );
						}
					}
					break;
				case OppEvent.BT_OPPS_PUSH_START:
					// add condition: "task.getState() == OppTask.STATE_PENDING"
					// because ext layer may issue BT_OPPS_PUSH_START when disconnect during pushing
					if( this.oppsTaskState == TaskState.Running && task.getState() == BluetoothShareTask.STATE_PENDING ){

						task.setState( BluetoothShareTask.STATE_ONGOING );
						task.setDoneBytes( 0 );
						handler.onObjectChange( task );
					}
					break;
				case OppEvent.BT_OPPS_PUSH_SUCCESS:
					this.oppsTaskState = TaskState.Running;
					task.setState( BluetoothShareTask.STATE_SUCCESS );
					result = this.oppsFinalEvent( task, true );
					handler.onObjectChange( task );
					return result;
				case OppEvent.BT_OPPS_PUSH_FAIL:
					parameters = event.parameters;
					task.setState( BluetoothShareTask.STATE_FAILURE );
					//task.setResult( "GOEP" + Integer.parseInt( parameters[0] ) );
					OppLog.w( "oppsAccessResponse get fail response :GOEP RSP[" + Utils.getGoepResponseCodeString( parameters[0] ) + "]" );
					result = this.oppsFinalEvent( task, false );
					handler.onObjectChange( task );
					return result;
				case OppEvent.BT_OPPS_DISCONNECT:
					OppLog.d( "oppsAccessResponse push disconnect - state[" + task.getState() + "]" );
					isAuthorized = false;
					result = this.oppsFinalEvent( task, false );
					handler.onObjectChange( task );
					return result;
				default:
					OppLog.e( "invalid oppsAccessResponse event: [" + event.event + "]" );
			}
		}
	}

	private boolean oppsFinalEvent( BluetoothShareTask task, boolean result ){

		// rejected by user
		if( task.getState() == BluetoothShareTask.STATE_REJECTING ){

			task.setState(BluetoothShareTask.STATE_REJECTED );
		}
		// canceled by user
		else if( this.oppsTaskState != TaskState.Running ){

			task.setState( BluetoothShareTask.STATE_ABORTED );
		}
		// normal transfer result
		else if( !result ){

			task.setState( BluetoothShareTask.STATE_FAILURE );
		}
		this.oppsTaskState = TaskState.Idle;
		this.checkQueue( this.oppsEventQueue );
		return result;
	}

	/****************************************************************************************************
	 * Helper Class
	 ****************************************************************************************************/

	// Thread for receive message from BT-Task
	class MessageListener extends Thread {

		public MessageListener(){

			super( "BtOppMessageListener" );
		}

		@Override
		public void run() {

			OppLog.i( "BtTask MessageListener thread starting..." );

			// change process priority
			Process.setThreadPriority( Process.THREAD_PRIORITY_BACKGROUND );
			// start receive message from bt-task
			OppServiceNative.this.startListenNative();

			OppLog.i( "BtTask MessageListener thread stopped.");
		}

		public void startup(){

			this.start();
		}

		public void shutdown(){

			// this native method will break waiting loop and let current thread stop
			OppServiceNative.this.stopListenNative();
		}
	}

	enum TaskState { Running, Aborting, Aborted, Idle }

	/****************************************************************************************************
	 * JNI - Bluetooth Profile External Adaptation
	 ****************************************************************************************************/

	/**
	 * used by jni to keep reference
	 */
	@SuppressWarnings("unused")
	private int mNativeData;

	static {
		// load native lib and do class level initialization
		System.loadLibrary(NATIVE_LIB);
		classInitNative();
	}
	// Test
	protected native boolean testJNI(int integer, String[] parameters);

	// JNI
	protected native static void classInitNative();
	protected native void objectInitNative();
	protected native void objectDeinitNative();

	// Service
	protected native boolean enableServiceNative();
	protected native void startListenNative();
	protected native void stopListenNative();
	protected native void disableServiceNative();

	// API - Client
	protected native boolean oppcEnableNative();
	protected native boolean oppcDisableNative();
	protected native boolean oppcConnectNative(String destAddr);
	protected native boolean oppcPushNative(String mimeType, String objectName, String filename);
	protected native boolean oppcPushObjectNative(String destAddr, String mimeType, String objectName, String filename);
	protected native boolean oppcPullObjectNative();
	protected native boolean oppcExchangeObjectNative(String destAddr, String mimeType, String objectName, String filename);
	protected native boolean oppcAbortNative();
	protected native boolean oppcDisconnectNative();

	// API - Server
	protected native boolean oppsEnableNative();
	protected native boolean oppsDisableNative();
	protected native boolean oppsAccessResponseNative(int goepStatus, String[] parameters);
	protected native boolean oppsDisconnectNative();
}
