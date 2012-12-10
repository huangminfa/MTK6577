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

package com.mediatek.bluetooth.map;

import android.content.Context;
import android.util.Log;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import android.content.SharedPreferences;

import com.mediatek.bluetooth.map.BluetoothMapServerService;
import com.mediatek.bluetooth.map.ControllerListener;
import com.mediatek.bluetooth.map.cache.*;
import com.mediatek.bluetooth.map.util.*;
import com.mediatek.xlog.Xlog;

class Instance implements ControllerListener{
	private final String TAG = "MAPInstance";
	//private final String ROOT_PATH = "/data/@btmtk/profile/map";

	private final String NAME_BASE = "instance";
	private final static String ACCOUNT_ID_SETTING = "ACCOUNT_ID_SETTING";
	private final static String SIM_ID_SETTING = "SIM_ID_SETTING";
	/*MAS settings*/
	private String mCurrentFolder = MAP.ROOT_PATH;
	
	private int mInstanceId;    
	private String mName;
	private int mType;
	private int mSimId = -1;
	private long mAccountId = -1;
	HashMap<Integer, Controller> mControllers;
	BluetoothDevice mDevice;

	//THE state is neccessary
	private boolean mRegistered = false;
	private boolean mMasConnected = false;
	private boolean mMnsConnected = false;

	private MessageListRequest 		mMsgListReqCache ;
	private MessageListObject  		mMsgListRspCache ;
	private BMessageObject			mMessageCache	;	
	private FolderListRequest		mFolderListReqCache	;
	private MessageRequest			mMsgReqCache ;
	private SetFolderRequest		mFolderReqCache ;

	private BluetoothMapServerService mServer;

	private Context mContext;
	private Listener mListener;

	public interface Listener {
		void onInstanceChanged(BluetoothDevice device, EventReport report);
	}

	//TODO: set id and simid based on cofiguration
	Instance(Context context, int type,int simId,int accountId){
		mContext = context;
		mSimId = simId;
		mAccountId = accountId;
		mInstanceId = InstanceUtil.assignInstanceId();
		mName = NAME_BASE + mInstanceId;
		mType = type;
		initCache();
	//	addMessageController(type);
		initController();
	}

	Instance(Context context, int type,int simId){
		log("simid:"+simId+",type: "+type);
		mContext = context;
		mSimId = simId;
		mInstanceId = InstanceUtil.assignInstanceId();
		mName = NAME_BASE + mInstanceId;
		mType = type;
		initCache();
	//	addMessageController(type);
		initController();
		
//		mCurrentFolder = MAP.ROOT_PATH;

		//temp for test, will be removed later
//		mSimId = 1;
	}
	Instance(Context context, int type){
		mContext = context;
		mInstanceId = InstanceUtil.assignInstanceId();
		mName = NAME_BASE + "_"+mInstanceId;
		mType = type;
		initCache();
	}

	private void initCache(){
		mControllers 			= new HashMap<Integer, Controller>();
		mMsgListReqCache		= new MessageListRequest(mInstanceId);
		mMsgListRspCache		= new MessageListObject();
	//	mMessageCache			= new BMessageObject(mCurrentFolder, mName);	
		mMessageCache			= new BMessageObject(mContext,mName);	
		mFolderListReqCache 	= new FolderListRequest(mInstanceId);
		mMsgReqCache			= new MessageRequest(mInstanceId);
		mFolderReqCache			= new SetFolderRequest(mInstanceId);

		initFolderStructure();
	}
	private void initController(){
		SharedPreferences sp = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		int type = mType;
		if (isMsgTypeSupported(MAP.MSG_TYPE_EMAIL)) {
			mAccountId = sp.getLong(ACCOUNT_ID_SETTING, -1);
			addMessageController(MAP.MSG_TYPE_EMAIL);
		} 
		if (isMsgTypeSupported(MAP.MSG_TYPE_SMS_CDMA) ||
			isMsgTypeSupported(MAP.MSG_TYPE_SMS_GSM) ||
			isMsgTypeSupported(MAP.MSG_TYPE_MMS)) {
			boolean simset = sp.getBoolean(SIM_ID_SETTING+mSimId, true);
			if (simset){				
				enableSim(false);
			}
		}
	}

	public boolean isMsgTypeSupported(int type){
		int temp = mType & type;
		if (temp > 0) {
			return true;
		} else {
			return false;
		}
	}
	public int getInstanceId(){
		return mInstanceId;
	}

	public String getName(){
		return mName;
	}
	public int getType(){
		return mType;
	}	

	public int getSimId(){
		return mSimId;
	}
	public long getAccountId(){
		EmailController controller = (EmailController)mControllers.get(Integer.valueOf(MAP.MSG_TYPE_EMAIL));
		if (controller != null) {
			mAccountId = controller.getAccount();
		} else {
			mAccountId = -1;
		}
		return mAccountId;
	}

	public String getRootPath(){
		return MAP.ROOT_PATH;
	}
	public BluetoothDevice getDevice(){
		return mDevice;
	}

	//todo: check whether the object is occupied 
	public MessageListRequest getMessageListCache(){
		return mMsgListReqCache;
	}
	public MessageRequest getMessageReqCache() {
		return mMsgReqCache;
	}
	public FolderListRequest getFolderListReqCache(){
		return mFolderListReqCache;
	}	
	public SetFolderRequest getFolderReqCache(){
		return mFolderReqCache;
	}
	public MessageListObject getMsgListRspCache(){
		return mMsgListRspCache;
	}
	public BMessageObject getBMessageObject(){
		return mMessageCache;
	}
	public boolean addMessageController(int type) {
		if ((type & MAP.MSG_TYPE_EMAIL) > 0){
			if (!mControllers.containsKey(MAP.MSG_TYPE_EMAIL)) {
				EmailController controller = new EmailController(mContext, this, mAccountId);
				mControllers.put(Integer.valueOf(MAP.MSG_TYPE_EMAIL),controller);
				mAccountId = controller.getAccount();
			}
		} 
		if ((type & MAP.MSG_TYPE_MMS) > 0 ) {
			if (!mControllers.containsKey(MAP.MSG_TYPE_MMS)) {
				mControllers.put(Integer.valueOf(MAP.MSG_TYPE_MMS), new MmsController(mContext, this, mSimId));
			}
		}
		if ((type & MAP.MSG_TYPE_SMS_CDMA) > 0) {
			if (!mControllers.containsKey(MAP.MSG_TYPE_SMS_CDMA)) {
				mControllers.put(Integer.valueOf(MAP.MSG_TYPE_SMS_CDMA), 
								new SmsController(mContext, this, mSimId, MAP.MSG_TYPE_SMS_CDMA));
			}
		}
		if ((type & MAP.MSG_TYPE_SMS_GSM) > 0 ) {
			if (!mControllers.containsKey(MAP.MSG_TYPE_SMS_GSM)) {
				mControllers.put(Integer.valueOf(MAP.MSG_TYPE_SMS_GSM), 
								new SmsController(mContext, this, mSimId, MAP.MSG_TYPE_SMS_GSM));
			}
		}	
		return true;
	}	
	public void removeMessageController(int type) {
		if (mControllers.containsKey(type)) {
			mControllers.remove(type);
		}
		if (!mControllers.containsKey(MAP.MSG_TYPE_EMAIL)){
			mAccountId = -1;
		}
	}


	//TODO: when enable ,just activiate related controller
	public boolean enableSim(boolean isSave){
		int type;
		log("enableSim():"+mSimId+", isSave:"+isSave);
		log("SIM_ID_SETTING+mSimId:"+SIM_ID_SETTING+mSimId);
		if (isSave) {
			SharedPreferences.Editor editor = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
			editor.putBoolean(SIM_ID_SETTING+mSimId, true);
			editor.apply();
		}
		if (NetworkUtil.isGeminiSupport()) {
			type = NetworkUtil.getGeminiSmsType(mSimId);
		} else {
			type = NetworkUtil.getSmsType();
		}
		//add mms conntroller
		if (isMsgTypeSupported(MAP.MSG_TYPE_MMS)) {
			addMessageController(MAP.MSG_TYPE_MMS);
		}
		//add sms controller
		if (type == MAP.MSG_TYPE_SMS_CDMA && 
			isMsgTypeSupported(MAP.MSG_TYPE_SMS_CDMA)) {
			addMessageController(MAP.MSG_TYPE_SMS_CDMA);
		} else if (type == MAP.MSG_TYPE_SMS_GSM &&
					isMsgTypeSupported(MAP.MSG_TYPE_SMS_GSM)){
			addMessageController(MAP.MSG_TYPE_SMS_GSM);		
		} else {
			log("unexpected sim type:"+ type);
			return false;
		}
		return true;
	}
	//TODO : when disable, just deactiviate related controller
	public void disableSim(){
		log("disableSim():"+mSimId);
		log("SIM_ID_SETTING+mSimId:"+SIM_ID_SETTING+mSimId);
		removeMessageController(MAP.MSG_TYPE_MMS);
		removeMessageController(MAP.MSG_TYPE_SMS_CDMA);
		removeMessageController(MAP.MSG_TYPE_SMS_GSM);
		
		SharedPreferences.Editor editor = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
		editor.putBoolean(SIM_ID_SETTING+mSimId, false);
		editor.apply();
		
	}

	public boolean updateMessageController(int type, long value){
		//only Email is permitted to change account
		boolean result = false;
		EmailController controller;
		if (type != MAP.MSG_TYPE_EMAIL) {
			return false;
		}
		controller = (EmailController)mControllers.get(type);
		if (controller != null) {
			result =controller.setAccount(value);
			if (result) {
				mAccountId = value;
				SharedPreferences.Editor editor = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
				editor.putLong(ACCOUNT_ID_SETTING, value);
				editor.apply();
			}
		}
		
		return result;
	}
	public boolean containsMessageController(int type) {
		return mControllers.containsKey(type);
	}
	

	public void onInstanceRegistered(){
		log("onInstanceRegistered");
		mRegistered = true;
	}

	public void onInstanceDeregistered(){
		log("onInstanceDeregistered");
		mRegistered = false;
		if (mMnsConnected) {
			deregisterCallback();
			mMnsConnected = false;
		}
		
	}
	public void onDeviceConnected (BluetoothDevice device) {
		log("onDeviceConnected():"+ device.getName());
		if (!mRegistered) {
			log("instance has not been registered");
			return;
		}
		if (mMasConnected) {
			log("error:the instance has been connected");
			return;
		}
		
			mDevice = device;
			mMasConnected = true;

		Iterator iterator= mControllers.entrySet().iterator();
		while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				Controller con = (Controller)entry.getValue();			
				con.onStart();
		}
		
	}
	
	public void onDeviceDisconnected(BluetoothDevice device){
		log("onDeviceDisconnected: "+ device.getName());
		if (!mMasConnected) {
			log("the mas connection in the mas has been disconnected");
			return;
		}
		if (mDevice != null && mDevice.equals(device)) {
			//reset mas connection flag
			mMasConnected = false;
			//when MAS disconnected, the event will not be sent to MCE.
			//and the resource will be kept for other devices
			if (mMnsConnected) {
				deregisterCallback();
				mMnsConnected = false;
			}
			clearCache();
			mDevice = null;
			mCurrentFolder = MAP.ROOT_PATH;
		} else {
			log("error: the device is null, or other device disconnect event is received");
		}
	}
	
	private void clearCache(){
		Iterator iterator= mControllers.entrySet().iterator();
		while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				Controller con = (Controller)entry.getValue();			
				con.onStop();
		}
	}
	
	private ArrayList<HashSet<String>> folderHirarchy = new ArrayList<HashSet<String>> ();

	private void initFolderStructure(){	
		HashSet<String> rootFolder = new HashSet<String>();
		rootFolder.add("");
		folderHirarchy.add(rootFolder);

		HashSet<String> firstFolder = new HashSet<String>();
		firstFolder.add(MAP.Mailbox.TELECOM);
		folderHirarchy.add(firstFolder);

		HashSet<String> secondFolder = new HashSet<String>();
		secondFolder.add(MAP.Mailbox.MSG);
		folderHirarchy.add(secondFolder);

		HashSet<String> thirdFolder = new HashSet<String>();
		thirdFolder.add(MAP.Mailbox.INBOX);
		thirdFolder.add(MAP.Mailbox.OUTBOX);
		thirdFolder.add(MAP.Mailbox.SENT);
		thirdFolder.add(MAP.Mailbox.DELETED);
		thirdFolder.add(MAP.Mailbox.DRAFT);
		folderHirarchy.add(thirdFolder);
		
	}
	public int setFolder(SetFolderRequest req) {
		
		String folder = req.getFolder();
		String address = req.getAddress();
		if (!isMasConnected() || folder == null) {			
			return MAP.RESULT_ERROR;
		}
		long id = req.getMasId();
		log("setFolder(): new folder is "+ folder);
		folder = folder.toLowerCase();
		
		if (id == mInstanceId && mDevice.getAddress().equals(address) 
			&& isValidPath(folder)) {			
			mCurrentFolder = folder;
		} else {
			log("mas id mismatch or device mismatch");
			return MAP.RESULT_ERROR;
		}
		return MAP.RESULT_OK;		
	}

	private boolean isValidPath(String path){
		String[] subpath;	
		int index;
 		if (path == null || path.indexOf(MAP.ROOT_PATH) != 0) {
			return false;
		}
		String virtualPath = path.substring(MAP.ROOT_PATH.length());
		if (virtualPath == null || virtualPath.length() == 0) {
			return true;
		} else {
			subpath = virtualPath.split("/");
			if (subpath != null && subpath.length > 0 && subpath[0] == null) {
				subpath[0] = "";
			}
		}
		for (index = 0; index < subpath.length && index < folderHirarchy.size(); index++) {
			log(subpath[index]);
			if (!folderHirarchy.get(index).contains(subpath[index])) {
				break;
			}
		}
		if (index == subpath.length) {
			return true;
		} else {
			return false;
		}
	}

	public int updateInbox() {
		if (!isMasConnected()) {			
			return MAP.RESULT_ERROR;
		}		
		Controller controller = mControllers.get(MAP.MSG_TYPE_EMAIL);
		if (controller != null) {
			controller.updateInbox();
		}
		return MAP.RESULT_OK;		
	}
	//when msg type is null or zero, 
	public MessageListObject getMessagelist(MessageListRequest req) {
		int msgType =  req.getMessageType();
		Controller[] controllers;
		log("getMessagelist(): type is" + msgType);
		boolean isSizeLimited = false;
		if (!isMasConnected()) {			
			return null;
		}
		
		mMsgListRspCache.reset();
		if (req.getListSize() == 0 ) {
			isSizeLimited = true;
		}

		if (msgType == MAP.MSG_TYPE_ALL) {
			msgType = mType;
		} else {
			msgType = msgType & mType;
		}

		controllers = getControllersByType(msgType);

		if (controllers != null && controllers.length != 0) {
			for (Controller con: controllers) {
				con.getMessageList(req);
			}
		}	
	
		return getMsgListRspCache();
	}

	//TODO: optimize the procedure
	public FolderListObject[] getFolderlist(FolderListRequest req) {
		log("getFolderlist");
		ArrayList<FolderListObject> folderList;		
		int size = req.getSize();
		int offset = req.getOffset();
		int index;
		FolderListObject[] object;
		if (!isMasConnected()) {			
			return null;
		}
		if (size< 0 ) {
			log("size is smaller than 0");
			return null;
		}
		folderList = new ArrayList<FolderListObject>();

		String[] childs = getChildFolders(mCurrentFolder);
		
		if (childs == null || childs.length == 0) {			
			
			return null;
		}
		for (index = offset; index < childs.length; index++) {
			FolderListObject folder = new FolderListObject();
			folder.setName(childs[index]);
			folderList.add(folder);
		} 
		object = folderList.toArray(new FolderListObject[folderList.size()]);
		log("child folder size "+childs.length+", foldersize is "+ object.length);
		return object;
	}
	//TODO: the API should be in Util.class
	private String[] getChildFolders(String current){	
		ArrayList<String> childFolders = new ArrayList<String>();
		if (current == null) {
			return null;
		}
		if (!mRegistered) {
			log("instance has not been registered");
			return childFolders.toArray(new String[childFolders.size()]);
		}
		String[] list = current.split("/");
			
		if (list.length == 0) {
			childFolders.add(MAP.Mailbox.TELECOM);
		} else {
			String last = list[list.length -1].toLowerCase();
			log("\n "+list.length);
			if (last.matches("map")) {
				childFolders.add(MAP.Mailbox.TELECOM);
			} else if (last.matches(MAP.Mailbox.TELECOM)) {
				childFolders.add(MAP.Mailbox.MSG);
			} else if (last.matches(MAP.Mailbox.MSG)) {
				childFolders.add(MAP.Mailbox.DRAFT);
				childFolders.add(MAP.Mailbox.INBOX);
				childFolders.add(MAP.Mailbox.OUTBOX);
				childFolders.add(MAP.Mailbox.DELETED);
				childFolders.add(MAP.Mailbox.SENT);
			} else {
				log("unexpected folder:"+last);
			}
		}
		return childFolders.toArray(new String[childFolders.size()]);
	}

	public boolean pushMessage(BMessageObject msg) {
		boolean flag = false;
		if (!isMasConnected()) {			
			return false;
		}
		Controller controller = mControllers.get(msg.getMessageType());
		if (controller != null) {
			controller.pushMessage(msg);
			flag = true;
		}
		msg.reset();
		return flag;
		
	}
	public BMessageObject getMessage(MessageRequest req) {
		int type = HandleUtil.getMessageType(req.getHandle());
		log("getMessage(): type->"+type);		
		if (!isMasConnected()) {			
			return null;
		}
		mMessageCache.reset();
		Controller controller = mControllers.get(type);
		if (controller != null) {
			return controller.getMessage(req);			
		} 
		return null;
		
	}
	
	public boolean setMessageStatus(StatusSwitchRequest req) {
		long handle = req.getHandle();
		int indicator = req.getIndicator();
		int value = req.getValue();		
		int msgType = HandleUtil.getMessageType(handle);
		long id = HandleUtil.getId(handle);
		boolean result = false;

		log ("setMessageStatus(): handle is "+ handle+ "indicator is "+indicator+"value is "+ value);

		if (!isMasConnected()) {			
			return false;
		}
		
		Controller controller = mControllers.get(msgType);
		if(controller != null) {
			if (indicator == MAP.STATUS_SWITCH_DELETE) {
				int deleteStatus;
				if (value == MAP.STATUS_SWITCH_YES) {
					result = controller.deleteMessage(id);
				} else if (value == MAP.STATUS_SWITCH_NO) {
					result = controller.restoreMessage(id);
				} else {
					log("invalid status value");	
					return false;
				}
			} else if (indicator == MAP.STATUS_SWITCH_READ) {
				//switch value to read status
				int readStatus;
				if (value == MAP.STATUS_SWITCH_YES) {
					readStatus = MAP.READ_STATUS;
				} else if (value == MAP.STATUS_SWITCH_NO) {
					readStatus = MAP.UNREAD_STATUS;
				} else {
					log("invalid status value");	
					return false;
				}
				result = controller.setMessageStatus(id, readStatus);
			} else {
				log("invalid indicator: "+ indicator);
			}
		} else {
			log("invalid message type:"+msgType);
		}
		return result;		
	}

	private Controller[] getControllersByType(int type){
		ArrayList<Controller> cache = new ArrayList<Controller>();
	//	int msg = MAP.MSG_TYPE_ALL;		
		int msgKind = 4;
		int mask = MAP.MSG_TYPE_SMS_GSM;
		int index = 0;
		int msg = 0;
		
		while (index < msgKind) {
			msg = mask & type;
			if (containsMessageController(msg)) {
				cache.add(mControllers.get(msg));
			}
			mask = mask << 1;
			index++;
		}
		return cache.toArray(new Controller[cache.size()]);
	}

	

	public void registerCallback(Listener listener) {
		log("registerCallback()");
		if(!isMasConnected()) {
			log("MAS has not been connected");
			return;
		}
		//set mns connection flag
		mMnsConnected = true;
		
		Iterator controllers= mControllers.entrySet().iterator();
		while (controllers.hasNext()) {
			Map.Entry entry = (Map.Entry)controllers.next();
			((Controller)entry.getValue()).registerListener(this);
		}
		mListener = listener;
	}
	public void deregisterCallback() {
		log("deregisterCallback()");
		//reset mMnsConnected
		if (!mMnsConnected) {			
			log("mns is not connected");
			return;
		}
		mMnsConnected = false;

		//when mas and mns are both disconnected, set device as null 
//		if(!isMasConnected() && mDevice != null) {
//			mDevice = null;
//		}
		
		Iterator controllers= mControllers.entrySet().iterator();
		while (controllers.hasNext()) {
			Map.Entry entry = (Map.Entry)controllers.next();
			((Controller)entry.getValue()).deregisterListener();
		}
		mListener = null;
	}

	
	
	public void onNewMessage(long id,int msgtype) {
		EventReport report;
		if (!isMnsConnected()) {			
			return;
		}
		report = new EventReport(mInstanceId);
		report.notifyNewMessageEvent(id ,msgtype,MAP.Mailbox.INBOX);
		if (mListener != null) {
			mListener.onInstanceChanged(mDevice, report);
		}
	}
	public void onMessageDeleted(long id,int msgtype, String mailbox){
		EventReport report;
		if (!isMnsConnected()) {
			return;
		}
		report = new EventReport(mInstanceId);
		report.notifyMessageDeleted(id ,msgtype,mailbox);
		if (mListener != null) {
			mListener.onInstanceChanged(mDevice, report);
		}
	}
	public void onMessageSent(long id,int msgtype, int result){
		EventReport report;
		if (!isMnsConnected()) {
			return;
		}
		report = new EventReport(mInstanceId);
		report.notifySendResult(id ,msgtype,MAP.Mailbox.SENT, result);
		if (mListener != null) {
			mListener.onInstanceChanged(mDevice, report);
		}		
	}
	public void onMessageDelivered(long id,int msgtype, int result){
		EventReport report;
		if (!isMnsConnected()) {
			return;
		}
		report = new EventReport(mInstanceId);
		report.notifyDeliverResult(id ,
							msgtype,MAP.Mailbox.SENT, result);
		if (mListener != null) {
			mListener.onInstanceChanged(mDevice, report);
		}
	}
	public void onMessageShifted(long id,int msgtype, String oldMailbox, String newMailbox){
		EventReport report;
		if (!isMnsConnected()) {
			return;
		}
		report = new EventReport(mInstanceId);
		report.notifyMessageShifted(id ,msgtype,oldMailbox, newMailbox);
		if (mListener != null) {
			mListener.onInstanceChanged(mDevice, report);
		}	
	}

	private boolean isMasConnected(){
 		boolean state = mRegistered && mMasConnected;
		if (!state){
			log("instance has not initialized or no mas connection exist");
		}
		return state;
	}
	private boolean isMnsConnected(){
 		boolean state = mRegistered && mMnsConnected && mDevice != null;
		if (!state){
			log("instance has not initialized or no mns connection exist or device is null");
		}
		return state;
	}

	private void log(String info){
		if (null != info){
			Xlog.v(TAG, info);
		}
	}
		
		
}
