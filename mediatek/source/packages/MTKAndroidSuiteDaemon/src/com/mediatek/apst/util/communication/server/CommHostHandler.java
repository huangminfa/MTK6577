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

package com.mediatek.apst.util.communication.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

import com.mediatek.apst.util.communication.comm.CommFactory;
import com.mediatek.apst.util.communication.common.CommHandler;
import com.mediatek.apst.util.communication.common.PollingThr;

/**
 * Class Name: CommHostHandler
 * <p>Package: com.mediatek.apst.util.communication.server
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Communication handler on host side.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public class CommHostHandler extends CommHandler{
	private ServerSocket serverSocket;

	/**
	 * Constructor.
	 */
	public CommHostHandler() {
		super();
		serverSocket = null;
	}
	
	//@Override
	public boolean createConnection(){
	    try {
    		if(serverSocket != null) {
    		    if (socket != null) {
        			System.out.println("[CommHostHandler][W]The socket is used! create connection failed!");
                    //FIXME Return true or false?
        			return true;
    		    } else {
    		        socket = serverSocket.accept();
    		    }
    		} else {
	            serverSocket = new ServerSocket(DEFAULT_PORT);
	            serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
	            System.out.println("[CommHostHandler][I]The select port is " + DEFAULT_PORT);
	            socket = serverSocket.accept();
    		}

            if(socket == null){
                System.out.println("[CommHostHandler][E]The socket server wait timeout!");
                serverSocket.close();
                serverSocket = null;
                return false;
            }
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.useProtocolVersion(java.io.ObjectStreamConstants.PROTOCOL_VERSION_2);
            ois = new ObjectInputStream(socket.getInputStream());
            pollingThr = new PollingThr(ois, oos, dispatcher,CommFactory.host_side);
            pThread = new Thread(pollingThr);
            pThread.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            oos.writeObject("PCST");
            oos.flush();
	    } catch (IOException e) {
            try {
                if (null != serverSocket && !serverSocket.isClosed()){
                    serverSocket.close();   
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            socket = null;
            serverSocket = null;
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	//@Override
	public boolean closeConnection() throws IOException{
		if(serverSocket == null || socket == null){
			System.out.println("[CommHostHandler][W]The socket has already been closed!");
			return false;
		}
		
		if(pollingThr.isRunning() || pThread.isAlive()){
			System.out.println("[CommHostHandler][W]We have stop the polling thread, but it still work, so interrupt it!");
			pThread.interrupt();
		}
		System.out.println("[CommHostHandler][I]Interrupt the send data thread!");
		ois.close();
		oos.close();
		socket.close();
		socket = null;
		serverSocket.close();
		serverSocket = null;
		dispatcher.setEcho(false);
		pollingThr.setRunning(false);
		System.out.println("[CommSlaveHandler][I]All cloesd!");
		return true;
	}

}
