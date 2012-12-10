/*
 * Copyright (c) 2010 Inside Secure, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opennfc.extension.nfc.api;

import java.util.Hashtable;

import android.util.Log;


/**
 * OpenNFCExtManager: a class to send requests and receive replies via OpenNFC Extensions
 *
 */ 
final class OpenNFCExtManager {
	
	static
	{
		System.loadLibrary("nfc_ext_jni");
	}
	
	private static final String TAG = OpenNFCExtManager.class.getSimpleName();
	
/* client's identifiers: these constants should correspond to the appropriate C constants */	
	final public static int CARD_EMULATION = 1;
	
	final private static OpenNFCExtManager manager = new OpenNFCExtManager();
	
	private OpenNFCExtManager() {
		initialize();
	}

	private native void initialize();

	
	/**
	 * Get the only OpenNFCExtManager instance
	 * @return OpenNFCExtManager instance
	 */
	public static OpenNFCExtManager getManager() {
		return manager;
	}
	
	/**
	 * Send a command and get the reply that contains the requested data via OpenNFC Extensions.
	 * The method is synchronous, i.e. blocks till the reply is received or it fails.
	 * @param extClientId client's identifier
	 * @param cmd command
	 * @param data command's data
	 * @return reply to the command (status and the data)
	 * @throws InterruptedException if command fails
	 */
	public OpenNFCExtReplyMessage getData(int extClientId, byte cmd, byte[] data) throws InterruptedException {
		return getReply(extClientId, cmd, data);
	}
	
	/**
	 * Send a command that doesn't require reply via OpenNFC Extensions.
	 * The method is synchronous, i.e. blocks till the command is received by the OpenNFC Extensions server or it fails.
	 * @param extClientId  client's identifier
	 * @param cmd command
	 * @param data command's data
	 * @return status of the command
	 * @throws InterruptedException if command fails
	 */
	public int sendData(int extClientId, byte cmd, byte[] data)	throws InterruptedException {
		OpenNFCExtReplyMessage msg = getReply(extClientId, cmd, data);
		return msg.getStatus();
	}

	private OpenNFCExtReplyMessage getReply(int extClientId, byte cmd, byte[] data) 
			throws InterruptedException {

		Request req = new Request();
		synchronized (req) {
			nativeSendData(extClientId, cmd, req.getId(), ((data != null) ? data : new byte[0]));
			Log.d(TAG, "getReply(): before wait(): reqId=" + req.getId() + ", req=" +
					req);
			req.wait();			
		}
		Log.d(TAG, "getReply(): after wait(): reqId=" + req.getId() +", status=" + req.getReply().getStatus());
		Log.d(TAG, "getReply(): data = " + Utils.toHexadecimal(req.getReply().getData()));
		
		return req.getReply();
	}
	
	private static void replyCallback(int reqId, int status, byte[] data) {

		Request req = Request.getRequest(reqId);
		Log.d(TAG, "replyCallback(): req=" + req + ", reqId=" + reqId +", status=" + status);
		OpenNFCExtReplyMessage reply = new OpenNFCExtReplyMessage(status, data);
		req.setReply(reply);
		synchronized (req) {
			req.notify();			
		}
	}

	private native void nativeSendData(int extClientId, byte cmd, int reqId, byte[] data);
	
	static class Request {
		private static int requestIdCntr = 1;

		private int requestId;
		OpenNFCExtReplyMessage reply;

		private static Hashtable<Integer, Request> table = new Hashtable<Integer, Request>();
		
		private static synchronized int createRequestId() {
			return requestIdCntr++;
		}
		
		Request() {
			requestId = createRequestId();
			table.put(new Integer(requestId), this);
		}

		int getId() {
			return requestId;
		}
		
		OpenNFCExtReplyMessage getReply() {
			return reply;
		}

		void setReply(OpenNFCExtReplyMessage data) {
			reply = data;
		}
		
		static Request getRequest(int reqId) {
			return table.remove(new Integer(reqId));
		}
	}

	
}