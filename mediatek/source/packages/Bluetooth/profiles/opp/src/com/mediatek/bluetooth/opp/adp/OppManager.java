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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.Handler.Callback;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

import com.mediatek.activity.CancelableActivity;
import com.mediatek.bluetooth.BluetoothProfile;
import com.mediatek.bluetooth.Options;
import com.mediatek.bluetooth.R;
import com.mediatek.bluetooth.opp.mmi.OppLog;
import com.mediatek.bluetooth.opp.mmi.UriData;
import com.mediatek.bluetooth.opp.mmi.UriDataUtils;
import com.mediatek.bluetooth.opp.mmi.Utils;
import com.mediatek.bluetooth.share.BluetoothShareNotification;
import com.mediatek.bluetooth.share.BluetoothShareTask;
import com.mediatek.bluetooth.share.BluetoothShareTask.BluetoothShareTaskMetaData;
import com.mediatek.bluetooth.share.BluetoothShareTask.Direction;
import com.mediatek.bluetooth.util.BtLog;
import com.mediatek.bluetooth.util.MediaScanner;
import com.mediatek.bluetooth.util.MimeUtils;
import com.mediatek.bluetooth.util.NotificationFactory;
import com.mediatek.bluetooth.util.SystemUtils;

/**
 * @author Jerry Hsu
 * 
 * 1. implement as an "singleton" object.
 */
public class OppManager implements Callback {

	// singleton
	private static OppManager instance = null;

	// bluetooth
	private BluetoothAdapter bluetoothAdapter;

	// context objects
	private Context applicationContext;
	private ContentResolver contentResolver;
	private NotificationManager notificationManager;
	private PowerManager powerManager;
	private WakeLock wakeLock;

	// OppTask cache for OPPC ( keep request before device selected )
	private ArrayList<BluetoothShareTask> oppcTaskCache = null;

	// keep current task for OPPS
	private BluetoothShareTask oppsTask = null;

	// run job in another Thread to prevent ANR
	private LooperThread bgRunner = new LooperThread( "OppManagerExecuter", Process.THREAD_PRIORITY_BACKGROUND, this );

	// opp service
	private OppService oppService;

	// singleton
	public static synchronized OppManager getInstance( Context context ){

		if( instance == null ){

			instance = new OppManager();
			instance.init(context);
		}
		return instance;
	}

	/**
	 * Private constructor
	 */
	private OppManager(){}

	/**
	 * initialize context objects
	 * 
	 * @param context
	 */
	private void init( Context context ){

		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if( this.bluetoothAdapter == null ){

			OppLog.w( "Bluetooth is not supported in this hardware platform (null BluetoothAdapter)." );
			return;
		}

		this.applicationContext = context.getApplicationContext();
		this.contentResolver = this.applicationContext.getContentResolver();
		this.notificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.powerManager = (PowerManager)this.applicationContext.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "Bluetooth" );
		this.bgRunner.start();
	}

	/********************************************************************************************
	 * OPPC API
	 ********************************************************************************************/

	/**
	 * add OppTask into taskCache (wait for device selected event to complete this task)
	 * 
	 * @param task
	 */
	public synchronized void oppcCacheTask( final BluetoothShareTask task ){

		//OppLog.d( "OppManager.oppcCacheTask()[+]");

		if( this.oppcTaskCache == null ){

			this.oppcTaskCache =  new ArrayList<BluetoothShareTask>(3);
		}

		// cache task
		this.oppcTaskCache.add(task);
	}

	/**
	 * use selected device to commit current OppTask (in taskCache)
	 * 
	 * @param device
	 */
	public synchronized void oppcSubmitTask( final BluetoothDevice device ){

		// log
		if( Options.LL_DEBUG ){

			OppLog.d( "oppcSubmitTask for device[" + device.getName() + "][" + device.getAddress() + "]" );
			//for( BluetoothShareTask task : this.oppcTaskCache ){

				//OppLog.d( "oppcSubmitTask task objectUri:" + task.getObjectUri() );
			//}
		}

		if( this.oppcTaskCache != null ){

			// copy the cached object list (and then the next round of sharing operation can continue without ConcurrentModificationException)
			Object[] param = new Object[]{ device, this.oppcTaskCache };
			this.oppcTaskCache = null;

			// run the job in another thread to prevent ANR
			this.bgRunner.mHandler.sendMessage( this.bgRunner.mHandler.obtainMessage( MSG_OPPC_SUBMIT_TASK, param ) );
		}
	}

	/**
	 * should be called when service start and will:
	 * 1. remove finish tasks (success, failure, aborted, aborting, rejecting)
	 * 2. reset the state of tasks:
	 *   2.1. ongoing => pending
	 *   2.2. fail => pending (retry)
	 * 3. notify worker-thread to process all pending tasks.
	 */
	protected void oppcResetTaskState(){

		// remove finish tasks
		this.contentResolver.delete(
				BluetoothShareTaskMetaData.CONTENT_URI,
				BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " +
				BluetoothShareTaskMetaData.TASK_STATE + " in ( ?, ?, ?, ?, ?, ? )",
				new String[]{
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_START ),
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_END ),
						Integer.toString( BluetoothShareTask.STATE_SUCCESS ),
						Integer.toString( BluetoothShareTask.STATE_FAILURE ),
						Integer.toString( BluetoothShareTask.STATE_ABORTED ),
						Integer.toString( BluetoothShareTask.STATE_ABORTING ),
						Integer.toString( BluetoothShareTask.STATE_REJECTING ),
						Integer.toString( BluetoothShareTask.STATE_CLEARED )
				} );

		// reset task: state / done-bytes / result
		ContentValues cv = new ContentValues();
		cv.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_PENDING );
		cv.put( BluetoothShareTaskMetaData.TASK_DONE_BYTES, 0 );
		cv.put( BluetoothShareTaskMetaData.TASK_RESULT, "" );
		this.contentResolver.update(
				BluetoothShareTaskMetaData.CONTENT_URI,
				cv,
				BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " + BluetoothShareTaskMetaData.TASK_STATE + " = ?",
				new String[]{
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_START ),
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_END ),
						Integer.toString( BluetoothShareTask.STATE_ONGOING )
				} );

		// notify user (via Android Notification)
		Cursor cursor = this.contentResolver.query(
				BluetoothShareTaskMetaData.CONTENT_URI,
				null,
				BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ? AND " + BluetoothShareTaskMetaData.TASK_STATE + " = ?",
				new String[]{
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_START ),
						Integer.toString( BluetoothShareTask.TYPE_OPPC_GROUP_END ),
						Integer.toString( BluetoothShareTask.STATE_PENDING )
				},
				BluetoothShareTaskMetaData._ID + " ASC" );

		try {
			if( cursor == null || !cursor.moveToFirst() ){

				BtLog.i( "oppcResetTaskState() - can't find any OPPC pending task to restart." );
				return;
			}

			if( Options.LL_DEBUG ){

				OppLog.d( "oppc found [" + cursor.getCount() + "] pending tasks after reset (creating notification for them)." );
			}
			for( ; !cursor.isAfterLast(); cursor.moveToNext() ){

				this.notifyOppTask( new BluetoothShareTask(cursor) );
			}
		}
		finally {

			if( cursor != null )	cursor.close();
		}
	}

	public Uri oppcGetCurrentTask(){

		return this.oppService.oppcGetCurrentTask();
	}
	
	/********************************************************************************************
	 * OPPS API
	 ********************************************************************************************/

	/**
	 * check the parameter and storage is valid
	 */
	public String oppsCheckCurrentTask(){

		// check parameter
		if( this.oppsTask == null || this.oppsTask.getPeerAddr() == null || this.oppsTask.getObjectName() == null || this.oppsTask.getTotalBytes() < 0 ){

			if( this.oppsTask == null ){

				OppLog.e( "current opps task is null => can't check it" );
			}
			else {
				OppLog.i( "invalid opps new task parameters: peerAddr[" + this.oppsTask.getPeerAddr() + "], objectName[" + this.oppsTask.getObjectName() + "], totalByte[" + this.oppsTask.getTotalBytes() + "]" );
			}
			return this.applicationContext.getString( R.string.bt_opps_push_toast_invalid_request );
		}

		// check storage writable size
		long as = SystemUtils.getReceivedFilePathAvailableSize(this.applicationContext);
		if( as <= this.oppsTask.getTotalBytes() ){

			OppLog.i( "storage is not available for opps new task: available[" + as + "] v.s. required[" + this.oppsTask.getTotalBytes() + "]" );
			return this.applicationContext.getString( R.string.bt_opps_toast_storage_unavailable );
		}

		// check ok
		return null;
	}
	
	/**
	 * start Activity to process incoming Push request
	 * 
	 * @param peerAddr
	 * @param objectName
	 * @param mimeType
	 * @param totalBytes
	 */
	protected boolean oppsStartPushActivity( boolean isAuthorized, String peerAddr, String objectName, String mimeType, long totalBytes ){

		if( Options.LL_DEBUG ){

			OppLog.d( "oppsStartPushActivity for: authorized[" + isAuthorized + "], device[" + peerAddr + "], object[" + objectName + "], mime[" + mimeType + "], size[" + totalBytes + "]" );
		}

		if ( MimeUtils.VCARD_TYPE.equals( mimeType ) ) {
			objectName = MimeUtils.applyVcardExt( objectName, 256 );
		}

		// prepare task object
		String peerName = this.getDeviceName( peerAddr );

		// check mimeType
		if( mimeType == null || mimeType.length() == 0 || mimeType.endsWith("*") ){

			mimeType = MimeUtils.getMimeType( objectName );
		}

		// create task object
		this.oppsTask = new BluetoothShareTask( BluetoothShareTask.TYPE_OPPS_PUSH );
		this.oppsTask.setPeerAddr(peerAddr);
		this.oppsTask.setPeerName(peerName);
		this.oppsTask.setObjectName(objectName);
		this.oppsTask.setMimeType(mimeType);
		this.oppsTask.setTotalBytes(totalBytes);
		this.oppsTask.setData( Utils.getValidStoragePath( this.applicationContext, this.oppsTask.getObjectName() ) );	// need to check filename valid

		// insert content-provider and get id (for notification)
		Uri newUri = this.contentResolver.insert( BluetoothShareTaskMetaData.CONTENT_URI, this.oppsTask.getContentValues() );
		this.oppsTask.setId( Integer.parseInt( newUri.getLastPathSegment() ) );

		// print debug message
		if( Options.LL_DEBUG ){

			OppLog.d( "opps newTask: " + this.oppsTask.getPrintableString() );
		}

		// check task
		String errMessage = this.oppsCheckCurrentTask();
		if( errMessage != null ){

			this.bgRunner.mHandler.sendMessage( this.bgRunner.mHandler.obtainMessage( MSG_SHOW_TOAST, errMessage ) );
			//Toast.makeText( applicationContext, errMessage, Toast.LENGTH_LONG ).show();
			this.oppsSubmitTask( BluetoothShareTask.STATE_REJECTING );
			return true;	// result confirmed: task submitted
		}

		// check authorization
		if( isAuthorized ){

			this.oppsSubmitTask( BluetoothShareTask.STATE_PENDING );
			return true;	// result confirmed: task submitted
		}
		else {
		
			// create and send notification
			int notificationId = NotificationFactory.getProfileNotificationId( BluetoothProfile.ID_OPP, this.oppsTask.getId() );
			Notification notification = OppNotificationFactory.getOppIncomingNotification( this.applicationContext, this.oppsTask );

			// send timeout notification
			this.notificationManager.notify( notificationId, notification );
			return false;	// result undetermined
		}
	}
	
	public void oppsSendCurrentIncomingNotification(){

		synchronized( this.oppsTask ){

			// incoming request is submitted (accept or reject)
			if( this.oppsTask == null )	return;

			// re-sned incoming notification after activity stopped
			int notificationId = NotificationFactory.getProfileNotificationId( BluetoothProfile.ID_OPP, this.oppsTask.getId() );
			Notification n = OppNotificationFactory.getOppIncomingNotification( this.applicationContext, this.oppsTask );
			n.defaults = 0;	// cancel vibration / sound effects
			this.notificationManager.notify( notificationId, n );
		}
	}

	protected void oppsCancelPushActivity(){

		if( this.oppsTask == null )	return;

		// For ALPS00313159, while user accept or reject =>oppssubmit task, disconnect event also
		// happend at this time, oppsTask will be set as null, synchronized null pointer => exception.
		try {
			
			synchronized( this.oppsTask ){
			
				// cancel Notification
				int notificationId = NotificationFactory.getProfileNotificationId( BluetoothProfile.ID_OPP, this.oppsTask.getId() );
			
				this.notificationManager.cancel(notificationId);
			
				// cancel Activity
				CancelableActivity.sendCancelActivityIntent( this.applicationContext, this.oppsTask.getId() );
			
				// reject the request => because it's disconnected, the state should be rejected (not rejecting)

				// For ALPS00293690, while push request timeout happend, server should add 
				// this record to Bluetooth Share History List and notify received failed.
				//this.oppsSubmitTask( BluetoothShareTask.STATE_REJECTED );
				OppLog.d( "oppsCancelPushActivity: STATE_REJECTED -> STATE_FAILURE" );
				this.oppsSubmitTask( BluetoothShareTask.STATE_FAILURE);

			}
		}catch( Exception e ){

		    OppLog.d( "oppsCancelPushActivity catch exception, this.oppstask:" + this.oppsTask );
		}

	}

	/**
	 * after user confirm the incoming request, this function will be called to accept or reject the request.
	 * 
	 * @param isAccept
	 */
	public void oppsSubmitTask( int taskSate ){

		if( Options.LL_DEBUG ){

			OppLog.d( "oppsSubmitTask for task: " + this.oppsTask );
		}

		if( this.oppsTask == null ){

			OppLog.e( "current opps task is null => can't submit it" );
			return;
		}

		synchronized( this.oppsTask ){

			if( this.oppsTask == null ){
	
				OppLog.i( "duplicated submit [Rejecting] opps task (timeout and user) => skip one" );
				return;
			}

			// update filename for saving (rename if file already exists)
			// if user reject or miss incoming file request, needn't create empty file in sdcard
			if( (taskSate == BluetoothShareTask.STATE_REJECTING) || (taskSate == BluetoothShareTask.STATE_REJECTED) 
				|| ( taskSate == BluetoothShareTask.STATE_FAILURE)){
				//this.oppsTask.setData("");    
			}
			else{
				
				String filename = this.oppsTask.getData();
				File file = SystemUtils.createNewFileForSaving( filename );
				if( file != null ){
					this.oppsTask.setData(file.getAbsolutePath());
				}
			}

			// update content provider
			this.oppsTask.setState( taskSate );
			int count = this.contentResolver.update(
					Uri.withAppendedPath( BluetoothShareTaskMetaData.CONTENT_URI, Integer.toString(this.oppsTask.getId()) ),
					this.oppsTask.getContentValues(), null, null );
			if( count != 1 ){
	
				OppLog.w( "oppsSubmitTask(): update task fail: count[" + count + "], id[" + this.oppsTask.getId() + "]" );
			}

			// notify user
			this.notifyOppTask( this.oppsTask );

			// start service
			this.oppsStartService();

			// reset opps task
			this.oppsTask = null;
		}
	}

	/**
	 * start OPP(server-role) service - register OPP and accept incoming requests.
	 */
	public void oppsStartService(){

		// start service to process request
		Intent intent = new Intent( this.applicationContext, OppService.class );
		intent.setAction( OppConstants.OppService.ACTION_OPPS_START );
		this.applicationContext.startService( intent );
	}

	public void oppsResetTaskState(){

		// remove finish tasks
		this.contentResolver.delete(
				BluetoothShareTaskMetaData.CONTENT_URI,
				BluetoothShareTaskMetaData.TASK_TYPE + " between ? and ?",
				new String[]{
						Integer.toString( BluetoothShareTask.TYPE_OPPS_GROUP_START ),
						Integer.toString( BluetoothShareTask.TYPE_OPPS_GROUP_END )
				} );
	}

	public Uri oppsGetCurrentTask(){

		return this.oppService.oppsGetCurrentTask();
	}
	
	/********************************************************************************************
	 * Common API
	 ********************************************************************************************/

	protected void oppOnServiceStop(){

		// cancel all notification (user action entry point)
		this.cancelAllNotification();

		// finish active Activity (popped dialog)
		CancelableActivity.sendCancelActivityIntent( this.applicationContext, CancelableActivity.NULL_CANCEL_ID );
	}
	
	public void oppAbortDeviceTasks( String bdAddr ){

		if( bdAddr == null )	return;

		OppLog.i( "oppAbortDeviceTasks(): " + bdAddr );

		Cursor cursor = this.contentResolver.query( BluetoothShareTaskMetaData.CONTENT_URI,
					new String[]{ BluetoothShareTaskMetaData._ID },
					BluetoothShareTaskMetaData.TASK_PEER_ADDR + " = ? and " +
					BluetoothShareTaskMetaData.TASK_STATE + " in ( ?, ? )",
					new String[]{
						bdAddr,
						Integer.toString(BluetoothShareTask.STATE_ONGOING),
						Integer.toString(BluetoothShareTask.STATE_PENDING) },
					null );
		List<Uri> uriList = this.getOppTaskList(cursor);
		for( Uri uri : uriList ){

			OppLog.i( "oppAbortDeviceTasks(): aborting task " + uri );
			this.oppAbortTask( uri );
		}
	}

	public BluetoothDevice oppQueryTaskDevice( Uri taskUri ){

		if( taskUri == null )	return null;

		Cursor cursor = this.contentResolver.query( taskUri, 
				new String[]{ BluetoothShareTaskMetaData.TASK_PEER_ADDR },
				null, null, null );

		if( cursor == null || !cursor.moveToFirst() ){

			OppLog.e( "oppQueryTask cannot find task for uri: " + taskUri );
			return null;
		}
		try {
			String peerAddr = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_PEER_ADDR) );
			return this.bluetoothAdapter.getRemoteDevice(peerAddr);
		}
		finally {
			if( cursor != null )	cursor.close();
		}
	}
	
	public void oppAbortTask( Uri taskUri ){

		// check service ready
		if( this.oppService == null ){

			OppLog.e( "oppService is null => can't abort task:[" + taskUri + "]" );
			return;
		}

		// update pending and ongoing task only ( other states can't be aborted )
		// update content provider: ongoing -> aborting / pending -> aborted
		ContentValues values = new ContentValues();
		values.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_ABORTING );
		int count = this.contentResolver.update( taskUri, values,
				BluetoothShareTaskMetaData.TASK_STATE + "=" + Integer.toString( BluetoothShareTask.STATE_ONGOING ),
				null );
		if( count != 1 ){
			values.put( BluetoothShareTaskMetaData.TASK_STATE, BluetoothShareTask.STATE_ABORTED );
			count = this.contentResolver.update( taskUri, values,
					BluetoothShareTaskMetaData.TASK_STATE + "=" + Integer.toString( BluetoothShareTask.STATE_PENDING ),
					null );
		}
		else {
			OppLog.d( "oppAbortTask() => STATE_ABORTING" );
		}

		// only execute cancel when task is under proper state
		if( count == 1 ){

			OppLog.d( "oppAbortTask() => STATE_ABORTING or STATE_ABORTED" );

			boolean isOngoing = true;
			BluetoothShareTask task = this.oppQueryTask( taskUri );
			if( task == null ){

				// this shouldn't happen => just update its state to aborting/aborted 
				OppLog.e( "can't find task for uri[" + taskUri + "] => can't abort this task" );
				return;
			}
			else if( task.isOppcTask() ){

				isOngoing = this.oppService.oppcAbort( taskUri );
			}
			else if( task.isOppsTask() ){

				isOngoing = this.oppService.oppsAbort( taskUri );
			}

			// notify task according to new state: aborting or aborted (or any updated by other thread)
			OppLog.d( "try to notify aborting/aborted task: isOngoing[" + isOngoing + "], state[" + task.getState() + "]" );
			this.notifyOppTask( task );
		}
		else {
			// normal case: task finished and user can't cancel it
			OppLog.i( "can't find proper task to cancel in db. found[" + count + "] task(s) for Uri[" + taskUri + "]" );

			// abnormal case: can't find in db => cancel pending notification ( no content )
			BluetoothShareTask task = this.oppQueryTask( taskUri );
			if( task == null ){

				OppLog.w( "can't find task to cancel for Uri[" + taskUri + "]" ); 
				this.cancelNotification( Integer.parseInt( taskUri.getLastPathSegment() ) );
			}
		}
	}

	/**
	 * query OppTask via specific Uri
	 * 
	 * @param taskUri
	 * @return
	 */
	protected BluetoothShareTask oppQueryTask( Uri taskUri ){

		Cursor cursor = this.contentResolver.query( taskUri, null, null, null, null );
		try {
			if( cursor == null || !cursor.moveToFirst() ){

				OppLog.e( "oppQueryTask cannot find task for uri: " + taskUri );
				return null;
			}
			return new BluetoothShareTask( cursor );
		}
		finally {

			if( cursor != null )	cursor.close();
		}
	}

	public String getDeviceName( String address ){

		return this.bluetoothAdapter.getRemoteDevice(address).getName();
	}

	public void acquireWakeLock(){

		this.wakeLock.acquire();
	}

	public void releaeWakeLock(){

		if( this.wakeLock.isHeld() ){

			this.wakeLock.release();
		}
	}

	// for call back service from mmi (e.g. cancel)
	protected void setOppService( OppService oppService ){

		this.oppService = oppService;
	}

	public List<Uri> getOppTaskList( Cursor cursor ){

		if( cursor == null )	return Collections.emptyList();

		List<Uri> result = new ArrayList<Uri>(cursor.getCount());
		for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
		{
			// current task uri
			int id = cursor.getInt( cursor.getColumnIndex(BluetoothShareTaskMetaData._ID) );
			result.add( Uri.parse( BluetoothShareTaskMetaData.CONTENT_URI + "/" + id ) );
		}
		return result;
	}

	/**
	 * 
	 * @param context
	 * @param task
	 */
	protected void notifyOppTask( BluetoothShareTask task ){

		Notification n;
		int pid = NotificationFactory.getProfileNotificationId( BluetoothProfile.ID_OPP, task.getId() );

		// scan downloaded file
		if( task.getState() == BluetoothShareTask.STATE_SUCCESS && task.getDirection() != Direction.out ){

			// request media scan
			OppLog.i( "create MediaScanner for newly received file:" + task.getData() + "," + task.getMimeType() );
			new MediaScanner( this.applicationContext, task.getData(), task.getMimeType(), null, 0 );
		}

		switch( task.getState() ){
			case BluetoothShareTask.STATE_ONGOING:
				n = OppNotificationFactory.getOppOngoingNotification( this.applicationContext, task );
				this.notificationManager.notify( pid, n );
				break;
			case BluetoothShareTask.STATE_SUCCESS:
			case BluetoothShareTask.STATE_FAILURE:
				//For ICS usability - add toast message as transferring failed via BT
				//Added by mtk04254
				if(task.getState() == BluetoothShareTask.STATE_FAILURE){
					if (task.getDirection() == Direction.out)
						this.bgRunner.mHandler.sendMessage( this.bgRunner.mHandler.obtainMessage( MSG_SHOW_TOAST, this.applicationContext.getString( R.string.bt_share_mgmt_tab_dialog_message_resend, task.getObjectName(), task.getPeerName() ) ) );
					else if (task.getDirection() == Direction.in)
						this.bgRunner.mHandler.sendMessage( this.bgRunner.mHandler.obtainMessage( MSG_SHOW_TOAST, this.applicationContext.getString( R.string.bt_share_mgmt_tab_dialog_message_recfail, task.getObjectName(), task.getPeerName() ) ) );
				}
				// send out TransferPage Intent
				n = BluetoothShareNotification.getShareManagementNotification( this.applicationContext );
				this.notificationManager.notify( NotificationFactory.NID_SHARE_MGMT_NOTIFICATION, n );
			case BluetoothShareTask.STATE_ABORTING:
			case BluetoothShareTask.STATE_ABORTED:
				// states that need to cancel CancelableActivity (OppCancelActivity)
				CancelableActivity.sendCancelActivityIntent( this.applicationContext, task.getId() );
			case BluetoothShareTask.STATE_REJECTING:
			case BluetoothShareTask.STATE_REJECTED:
				// states that need to cancel ongoing notification
				this.notificationManager.cancel( pid );
				break;
			case BluetoothShareTask.STATE_PENDING:
				//n = OppNotificationFactory.getOppPendingNotification( this.applicationContext, task );
				//break;
			default:
				// no notification required
				OppLog.d( "cancel notification for unhandled state[" + task.getState() + "] - id:" + pid );
				this.notificationManager.cancel( pid );
				return;
		}
	}

	/**
	 * cancel notification
	 * 
	 * @param context
	 * @param id
	 */
	protected void cancelNotification( int id ){

		this.notificationManager.cancel( NotificationFactory.getProfileNotificationId( BluetoothProfile.ID_OPP, id ) );
	}

	/**
	 * cancel all application notifications (cross-profile)
	 * 
	 * @param context
	 */
	protected void cancelAllNotification(){

		this.notificationManager.cancelAll();
	}

	private static final int MSG_OPPC_SUBMIT_TASK = 1;
	private static final int MSG_SHOW_TOAST = 2;
	private OppcTaskTransferThread oppcTaskTransfer;
	public boolean handleMessage( Message msg ){

		switch( msg.what )
		{
		case MSG_OPPC_SUBMIT_TASK:
		    Object[] param = (Object[])msg.obj;
		    oppcTaskTransfer = new OppcTaskTransferThread( "BtOppcTaskTransferThread", new Object[]{param[0], param[1]} );
		    oppcTaskTransfer.start();
			break;
		case MSG_SHOW_TOAST:
			String message = (String)msg.obj;
			Toast.makeText( applicationContext, message, Toast.LENGTH_LONG ).show();
			break;
		}
		return false;
	}
	
	static class LooperThread extends Thread {
		private int threadPriority;
		private Callback callback;
		public Handler mHandler;
		public LooperThread( String name, int threadPriority, Callback callback ){
			super( name );
			this.threadPriority = threadPriority;
			this.callback = callback;

			// For ALPS00117959
			mHandler = new Handler(this.callback);
		}
		public void run(){
			Process.setThreadPriority( this.threadPriority );
			Looper.prepare();
			//mHandler = new Handler(this.callback);
			Looper.loop();
		}
	}

    public class OppcTaskTransferThread extends Thread {

	    private Object[] param;
	    public OppcTaskTransferThread( String name, Object[] param ){

		    super( name );
		    this.param = param;
	    }

	    @Override
	    public void run() {

		    OppLog.d( "Oppc Task handler thread start: thread name - " + this.getName() );
		    Process.setThreadPriority( Process.THREAD_PRIORITY_BACKGROUND );

			this.oppcHandleTask();

			OppLog.d( "Oppc Task handler thread end: thread name - " + this.getName() );
            
		}

		public synchronized void oppcHandleTask(){
			// device info
			long batchTimestamp = System.currentTimeMillis();
			BluetoothDevice device = (BluetoothDevice)param[0];
			ArrayList<BluetoothShareTask> cachedTasks = (ArrayList<BluetoothShareTask>)param[1];
			String deviceName = device.getName();
			String deviceAddr = device.getAddress();
			for( BluetoothShareTask task : cachedTasks ){

				UriData ud = UriDataUtils.getUriData( applicationContext, Uri.parse( task.getObjectUri() ) );
				if( ud != null ){
	
					task.setObjectName( ud.getName() );
					task.setData( ud.getData() );
					task.setTotalBytes( ud.getSize() );
					task.setState( BluetoothShareTask.STATE_PENDING );
				}
				else {
					OppLog.w( "oppcSubmitTask - invalid task object: " + task.getPrintableString() );
					// can't find object for given Uri (e.g. SDCard unmounted)
					//task.setObjectName( "object not found" );
					task.setState( BluetoothShareTask.STATE_FAILURE );
				}					

				// insert into ContentProvider
				task.setPeerName( deviceName );
				task.setPeerAddr( deviceAddr );
				task.setCreationDate( batchTimestamp );	// batch identifier
				Uri newUri = contentResolver.insert( BluetoothShareTaskMetaData.CONTENT_URI, task.getContentValues() );
	
				if( Options.LL_DEBUG ){
	
					//OppLog.d( "oppcSubmitTask committed task: [" + newUri.toString() + "]" );
				}
	
				// notify user
				task.setId( Integer.parseInt( newUri.getLastPathSegment() ) );
				notifyOppTask( task );
			}
	
			// clear cache
			cachedTasks.clear();
			cachedTasks = null;
	
			// start service to process request
			Intent intent = new Intent( applicationContext, OppService.class );
			intent.setAction( OppConstants.OppService.ACTION_OPPC_START );
			applicationContext.startService( intent );

		}
	}

	
}
