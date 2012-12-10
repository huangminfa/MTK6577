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

import java.util.Set;
import java.util.Map.Entry;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * TODO [OPP] total / done will be received from update event, need to keep in db ?
 */
public class BluetoothShareTask {

	public static final int ID_NULL = -519;

	public static final int
	TYPE_OPPC_GROUP_START	= 0,
	TYPE_OPPC_PUSH		= TYPE_OPPC_GROUP_START + 1,
	TYPE_OPPC_PULL		= TYPE_OPPC_GROUP_START + 2,
	TYPE_OPPC_GROUP_END	= TYPE_OPPC_GROUP_START + 9,
	TYPE_OPPS_GROUP_START	= 10,
	TYPE_OPPS_PUSH		= TYPE_OPPS_GROUP_START + 1,
	TYPE_OPPS_PULL		= TYPE_OPPS_GROUP_START + 2,
	TYPE_OPPS_GROUP_END	= TYPE_OPPS_GROUP_START + 9,
	TYPE_BIPI_GROUP_START   = 20,
        TYPE_BIPI_PUSH          = TYPE_BIPI_GROUP_START + 1,
	TYPE_BIPI_GROUP_END	= TYPE_BIPI_GROUP_START + 9,
	TYPE_BIPR_GROUP_START   = 30,
        TYPE_BIPR_PUSH          = TYPE_BIPR_GROUP_START + 1,
	TYPE_BIPR_GROUP_END	= TYPE_BIPR_GROUP_START + 9;
	public static final int
	STATE_PENDING		= 1,	// initial: user confirmed
	STATE_REJECTING		= 2,	// temp: rejecting => rejected
	STATE_ABORTING		= 3,	// temp: aborting => aborted
	STATE_ONGOING		= 4,	// temp: ongoing => failure / success
	STATE_REJECTED		= 5,	// finish - rejected
	STATE_ABORTED		= 6,	// finish - aborted
	STATE_FAILURE		= 7,	// finish - failure
	STATE_SUCCESS		= 8,	// finish - success
	STATE_CLEARED		= 9;	// cleared - cleared by user

	/**
	 * Select Condition
	 */
	// finished task: SUCCESS or FAILURE
	public static final String SC_FINISHED_TASK = BluetoothShareTaskMetaData.TASK_STATE + " in (" + BluetoothShareTask.STATE_SUCCESS + "," + BluetoothShareTask.STATE_FAILURE + ")";
	// incoming task: server-push + client-pull
	public static final String SC_INCOMING_TASK = BluetoothShareTaskMetaData.TASK_TYPE + " in (" + BluetoothShareTask.TYPE_OPPS_PUSH + "," + BluetoothShareTask.TYPE_OPPC_PULL + "," + BluetoothShareTask.TYPE_BIPR_PUSH + ")";
	// outgoing task: server-pull + client-push 
	public static final String SC_OUTGOING_TASK = BluetoothShareTaskMetaData.TASK_TYPE + " in (" + BluetoothShareTask.TYPE_OPPC_PUSH + "," + BluetoothShareTask.TYPE_OPPS_PULL + "," + BluetoothShareTask.TYPE_BIPI_PUSH + ")";

	/**
	 * Bluetooth Share Task Metadata
	 */
	public static interface BluetoothShareTaskMetaData extends BaseColumns {

		public static final String TABLE_NAME = "share_tasks";

		public static final Uri CONTENT_URI = Uri.parse( "content://" + BluetoothShareProvider.AUTHORITY + "/" + TABLE_NAME );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mtkbt.share.task";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mtkbt.share.task";
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		// metadata
		public static final String TASK_TYPE = "type";
		public static final String TASK_STATE = "state";
		public static final String TASK_RESULT = "result";

		// request
		public static final String TASK_OBJECT_NAME = "name";
		public static final String TASK_OBJECT_URI = "uri";
		public static final String TASK_OBJECT_FILE = "data";
		public static final String TASK_MIMETYPE = "mime";
		public static final String TASK_PEER_NAME = "peer_name";
		public static final String TASK_PEER_ADDR = "peer_addr";

		// progress
		public static final String TASK_TOTAL_BYTES = "total";
		public static final String TASK_DONE_BYTES = "done";

		// timestamp
		public static final String TASK_CREATION_DATE = "creation";
		public static final String TASK_MODIFIED_DATE = "modified";
	}

	public Uri getTaskUri(){

		if( this.id == ID_NULL ){

			throw new IllegalStateException( "null id task can't get uri" );
		}
		else {
			return Uri.withAppendedPath( BluetoothShareTaskMetaData.CONTENT_URI, Integer.toString( this.id ) );
		}
	}

	// metadata
	private int id = ID_NULL;
	private int type;
	private int state;
	private String result;
	private String data;

	// request
	private String objectName;
	private String objectUri;
	private String mimeType;
	private String peerName;
	private String peerAddr;

	// progress
	private long totalBytes;
	private long doneBytes;

	// timestamp
	private long creationDate = 0;
	private long modifiedDate = 0;


	public BluetoothShareTask( int type ){

		this.type = type;
	}

	public BluetoothShareTask( Cursor cursor ){

		this.id = cursor.getInt( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData._ID) );
		this.type = cursor.getInt( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_TYPE) );
		this.state = cursor.getInt( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_STATE) );
		this.result = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_RESULT) );

		this.objectName = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_OBJECT_NAME) );
		this.objectUri = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_OBJECT_URI) );
		this.data = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_OBJECT_FILE) );
		this.mimeType = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_MIMETYPE) );
		this.peerName = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_PEER_NAME) );
		this.peerAddr = cursor.getString( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_PEER_ADDR) );

		this.totalBytes = cursor.getLong( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_TOTAL_BYTES) );
		this.doneBytes = cursor.getLong( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_DONE_BYTES) );

		this.creationDate = cursor.getLong( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_CREATION_DATE) );
		this.modifiedDate = cursor.getLong( cursor.getColumnIndexOrThrow(BluetoothShareTaskMetaData.TASK_MODIFIED_DATE) );
	}

	/**
	 * create ContentValues for ContentProvider operations
	 * 
	 * @return
	 */
	public ContentValues getContentValues(){

		ContentValues values = new ContentValues();

		// existing record
		if( this.id != ID_NULL ){
			values.put( BluetoothShareTaskMetaData._ID, this.id );
		}
		if( this.creationDate != 0 ){
			values.put( BluetoothShareTaskMetaData.TASK_CREATION_DATE, this.creationDate );
		}
		if( this.modifiedDate != 0 ){
			values.put( BluetoothShareTaskMetaData.TASK_MODIFIED_DATE, this.modifiedDate );
		}
		values.put( BluetoothShareTaskMetaData.TASK_TYPE, this.type );
		values.put( BluetoothShareTaskMetaData.TASK_STATE, this.state );
		values.put( BluetoothShareTaskMetaData.TASK_RESULT, this.result );

		values.put( BluetoothShareTaskMetaData.TASK_OBJECT_NAME, this.objectName );
		values.put( BluetoothShareTaskMetaData.TASK_OBJECT_URI, this.objectUri );
		values.put( BluetoothShareTaskMetaData.TASK_OBJECT_FILE, this.data );
		values.put( BluetoothShareTaskMetaData.TASK_MIMETYPE, this.mimeType );
		values.put( BluetoothShareTaskMetaData.TASK_PEER_NAME, this.peerName );
		values.put( BluetoothShareTaskMetaData.TASK_PEER_ADDR, this.peerAddr );

		values.put( BluetoothShareTaskMetaData.TASK_TOTAL_BYTES, this.totalBytes );
		values.put( BluetoothShareTaskMetaData.TASK_DONE_BYTES, this.doneBytes );

		return values;
	}

	public String getPrintableString(){

		StringBuilder res = new StringBuilder();
		ContentValues cv = this.getContentValues();
		Set<Entry<String, Object>> set = cv.valueSet();
		for( Entry<String, Object> e : set ){

			res.append( "[" ).append( e.getKey() ).append( "=" ).append( e.getValue() ).append( "]" );
		}
		return res.toString();
	}

	public boolean isOppcTask(){

		return ( TYPE_OPPC_GROUP_START < this.type && this.type < TYPE_OPPC_GROUP_END );
	}

	public boolean isOppsTask(){

		return ( TYPE_OPPS_GROUP_START < this.type && this.type < TYPE_OPPS_GROUP_END );
	}

	public static enum Direction { in, out };
	public Direction getDirection(){
		switch( this.type ){
			case BluetoothShareTask.TYPE_OPPC_PULL:
			case BluetoothShareTask.TYPE_OPPS_PUSH:
			case BluetoothShareTask.TYPE_BIPR_PUSH:
				return Direction.in; //R.drawable.bluetooth_opp_pull_anim0;
			case BluetoothShareTask.TYPE_OPPC_PUSH:
			case BluetoothShareTask.TYPE_OPPS_PULL:
			case BluetoothShareTask.TYPE_BIPI_PUSH:
				return Direction.out; //R.drawable.bluetooth_opp_push_anim0;
			default:
				return Direction.out;
		}
	}
	
	/**********************************************************************************************************
	 * Getter / Setter
	 **********************************************************************************************************/

	public int getState() {
		return this.state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getObjectName() {
		return this.objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getObjectUri() {
		return this.objectUri;
	}

	public void setObjectUri(String objectUri) {
		this.objectUri = objectUri;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(String mimeType) {

		if( mimeType != null ){

			// MIME type matching in the Android framework is case-sensitive (unlike formal RFC MIME types).
			// As a result, you should always specify MIME types using lowercase letters.
			this.mimeType = mimeType.toLowerCase();
		}
		else {
			this.mimeType = mimeType;
		}
	}

	public String getPeerName() {
		return this.peerName;
	}

	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}

	public String getPeerAddr() {
		return this.peerAddr;
	}

	public void setPeerAddr(String peerAddr) {
		this.peerAddr = peerAddr;
	}

	public long getTotalBytes() {
		return this.totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public long getDoneBytes() {
		return this.doneBytes;
	}

	public void setDoneBytes(long doneBytes) {
		this.doneBytes = doneBytes;
	}

	public long getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public long getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return this.type;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
