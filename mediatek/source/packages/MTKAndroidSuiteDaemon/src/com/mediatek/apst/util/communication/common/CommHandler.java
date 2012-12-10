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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class Name: CommHandler
 * <p>Package: com.mediatek.apst.util.communication.common
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Communication handler handles connection creation and closing, data 
 * sending and receiving.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public abstract class CommHandler {
	protected Socket socket;
	protected Dispatcher dispatcher;
	protected PollingThr pollingThr;
	protected Thread pThread;
	protected ObjectOutputStream oos;
	protected ObjectInputStream ois;
	protected ByteArrayOutputStream bos;
    protected ByteArrayInputStream bis;
    
    /**
     * Default port for communication.
     */
	public static final int DEFAULT_PORT = 43708;
	/**
	 * ADB forward port for communication.
	 */
	public static final int FORWARD_PORT = 34238;
	/**
	 * Default timeout for creating connection.
	 */
	public static final int DEFAULT_TIMEOUT = 4000;
	
	/**
	 * Constructor.
	 */
	public CommHandler() {
		dispatcher = Dispatcher.getInstance();
		socket = null;
		pThread = null;
		oos = null;
		ois = null;
	}
	
	/**
	 * Abstract method for creating a new connection.
	 * @return True if connection is created successfully, otherwise false.
	 */
	public abstract boolean createConnection();
	
    /**
     * Abstract method for closing the current connection.
     * @return True if connection is closed successfully, otherwise false.
     * @throws IOException
     */
    public abstract boolean closeConnection() throws IOException;
    
    /**
     * Register callback for handling received data with the specified feature 
     * ID.
     * @param featureID Feature ID
     * @param callback The callback. Should be an instance implemented ICallback 
     * interface.
     * @return True if the callback is registered successfully, otherwise false.
     * @see ICallback
     */
    public boolean registerCallback(int featureID, ICallback callback){
        return dispatcher.installCallback(featureID, callback);
    }
    
	/**
	 * Send data via socket connection.
	 * @param primitive Data to be sent. This data should be an instance of 
	 * <b>TransportEntity</b>.
	 * @param featureID The feature ID of the data. This value will determine 
	 * how the data will be dispatched on the other side of communication.
	 * @return The token of the data.
	 * @see TransportEntity
	 */
	public synchronized int sendPrimitive(TransportEntity primitive, int featureID){
		if(socket == null || oos == null){
			System.out.println("[CommHandler][E]The socket is null, send failed!");	
			return -1;			
		}
		if(primitive == null){
			System.out.println("[CommHandler][E]The primitive is null, send failed!");	
			return -1;
		}
		primitive.setFeatureID(featureID);
		int tok = primitive.getToken();
		if(tok == -1){
			tok = dispatcher.getToken();
			primitive.setToken(tok);
		}
		try {
		    //long start = System.currentTimeMillis();  
			oos.writeObject(primitive);
            //System.out.println("[CommHandler][I]Write object cost: " + (System.currentTimeMillis() - start)); 
			oos.flush();
			oos.reset();
			System.out.println("[CommHandler][I]The primitive has been sent," +
					"feature ID is:"+primitive.getFeatureID()+", " +
							"token is: "+primitive.getToken());
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return tok;
	}
	
	/**
	 * Stop polling data from socket IO stream.
	 * @return True if stopping is successful, otherwise false. Notice that when 
	 * the polling thread is null or not in a valid polling state, the return 
	 * value will also be false.
	 */
	public boolean stopPolling(){
		if(pThread == null || !pollingThr.isRunning() || !pThread.isAlive())
			return false;
		pThread.interrupt();
		return true;
	}
}
