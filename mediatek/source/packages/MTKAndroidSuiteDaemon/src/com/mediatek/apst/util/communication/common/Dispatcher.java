/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.util.communication.common;

import java.util.Hashtable;

/**
 * Class Name: Dispatcher
 * <p>Package: com.mediatek.apst.util.communication.common
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Dispatch data and manage token.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public class Dispatcher {
	
	private static Dispatcher instance; //Singleton object
	private static int token;
	private Hashtable<Integer, ICallback> callbackMap;
	private boolean isEcho;

	/**
	 * Constructor.
	 */
	private Dispatcher() {
		callbackMap = new Hashtable<Integer, ICallback>();
		token = 0;
		isEcho = false;
	}
	
	public boolean isEcho() {
		return isEcho;
	}

	public void setEcho(boolean isEcho) {
		this.isEcho = isEcho;
	}
	
	/**
	 * Get the single instance of Dispatcher.
	 * @return The single instance.
	 */
	public synchronized static Dispatcher getInstance() {
		if(instance == null){
			instance = new Dispatcher();
		}
		return instance;
	}
	
	/**
	 * Get a new token for new request. Typically, a new token represents a new 
	 * communication, both the new request and the corresponding response should 
	 * use this token.
	 * @return The new token.
	 */
	public int getToken(){
		return token++;
	}
	
	/**
	 * Reset token to 0.
	 */
	public void resetToken(){
		token = 0;
	}
	
	/**
     * Register callback for handling received data with the specified feature 
     * ID.
     * @param featureID Feature ID
     * @param callback The callback. Should be an instance implemented ICallback 
     * interface.
     * @return True if the callback is registered successfully, otherwise false.
     * @see ICallback
     */
	public boolean installCallback(int featureID, ICallback callback){
		if(callbackMap == null){
			System.out.println("[Dispatcher]The Hashtable has not been init!");
			return false;
		}
		if(callback == null){
			System.out.println("[Dispatcher]The callback function is null!");
			return false;			
		}
			
		if(null != callbackMap.put(featureID, callback))
			System.out.println("[Dispatcher]The same key has already existed before! Now will replace it.");
		else
			System.out.println("[Dispatcher]It is a new key!");
		return true;
	}
	
	/**
	 * Get the callback registered with the specified feature ID.
	 * @param featureID The feature ID.
	 * @return The corresponding callback registered.
	 */
	private ICallback getCallback(int featureID){
		if(callbackMap == null){
			System.out.println("[Dispatcher]The Hashtable has not been init!");
			return null;
		}
		return callbackMap.get(featureID);
	}
	
	/**
	 * Unregister the callback registered with the specified feature ID.
	 * @param featureID The feature Id.
	 * @return True if unregistered successfully, otherwise false.
	 */
	public boolean uninstallCallback(int featureID){
		if(callbackMap == null){
			System.out.println("[Dispatcher]The Hashtable has not been init!");
			return false;
		}
		if(null == callbackMap.remove(featureID)){
			System.out.println("[Dispatcher]The feature ID has not been registered!");
			return false;
		}
		return true;
	}
	
	/**
	 * Dispatch data. Typically, the data object is an instance of 
	 * TransportEntity, so it will be dispatched according to its feature ID. 
	 * In other cases, it is usually a special data defined in communication 
	 * protocol and will be handled specially.
	 * @param obj Data object to dispatch.
	 * @return False if exception or other error occurs, otherwise true.
	 * @see TransportEntity
	 */
	public boolean dispatch(Object obj){
		if(obj instanceof TransportEntity){
			int id = ((TransportEntity)obj).getFeatureID();
			int tok = ((TransportEntity)obj).getToken();
			System.out.println("[Dispatcher:info]Recieve data feature ID is:"+ id +", token is "+ tok);
			ICallback cb = this.getCallback(id);
			if(cb == null){
				System.out.println("[Dispatcher:error]The callback can not be found!");
				return false;
			}
			cb.execute((TransportEntity)obj);
			return true;
		}
		else if(obj instanceof String){
			if(((String)(obj)).equals("PCST")){
				System.out.println("[Dispatcher:info]Get the echo!");
				isEcho = true;
			}
			return true;
		}
		else{
			System.out.println("[Dispatcher]The object is not a instance of TransportEntity! Cast type failed!");
			return false;
		}
	}

}
