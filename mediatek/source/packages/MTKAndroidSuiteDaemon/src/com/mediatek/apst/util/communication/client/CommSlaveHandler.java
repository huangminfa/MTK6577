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

package com.mediatek.apst.util.communication.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


import com.mediatek.apst.util.communication.comm.CommFactory;
import com.mediatek.apst.util.communication.common.CommHandler;
import com.mediatek.apst.util.communication.common.PollingThr;

/**
 * Class Name: CommSlaveHandler
 * <p>Package: com.mediatek.apst.util.communication.client
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Communication handler on slave side.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public class CommSlaveHandler extends CommHandler{
	private static final String LOCALHOST_IP_ADDRESS = "127.0.0.1";
	
	/**
	 * Constructor.
	 */
	public CommSlaveHandler() {
		super();
	}
	
	//@Override
	public boolean closeConnection() throws IOException {
		if(socket == null){
		    // the socket has been closed.
			System.out.println("[CommSlaveHandler][E]The socket has already been closed!You can not close it!");
			return false;
		}
		try {
		    oos.writeObject("DISC");
		} catch (Exception e){
		    System.out.println("[CommSlaveHandler][E]Write 'DISC' failed!" );
		}
		
		int cnt = 0;
		while(pollingThr.isRunning() && pThread.isAlive() && cnt < 5){
			try {
				Thread.sleep(100);
				cnt ++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(pollingThr.isRunning() || pThread.isAlive()){
			System.out.println("[CommSlaveHandler][W]We have stop the polling thread, but it still work, so interrupt it!");
			pThread.interrupt();
		}
		else
			System.out.println("[CommSlaveHandler][I]The Polling thread stopped normally!");
		System.out.println("[CommSlaveHandler][I]Interrupt the send data thread!");
		ois.close();
		try {
		    oos.close();
		} catch (Exception e){
            System.out.println("[CommSlaveHandler][E]Close object outputstream failed!" );
        }
		if(null != socket) {
		    socket.close();
		    socket = null;
		}
		dispatcher.setEcho(false);
		pollingThr.setRunning(false);
		System.out.println("[CommSlaveHandler][I]All cloesd!");
		return true;
	}
	
	//@Override
	public boolean createConnection(){
		if(socket != null){
			System.out.println("[CommSlaveHandler][W]The socket is used! Create connection failed!");
            //FIXME Return true or false?
			return true;
		}
		try {
			socket = new Socket(LOCALHOST_IP_ADDRESS, FORWARD_PORT);
		} catch (UnknownHostException e) {
			socket = null;
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			socket = null;
			e.printStackTrace();
			return false;
		}
		if(socket == null){
			System.out.println("[CommSlaveHandler][E]The socket is create failed!");
			return false;
		}
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.useProtocolVersion(java.io.ObjectStreamConstants.PROTOCOL_VERSION_2);
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			try {
				socket.close();
				socket = null;
			} catch (IOException e1) {
				e1.printStackTrace();
				socket = null;
			}
			return false;
		}
		
		
		pollingThr = new PollingThr(ois, oos, dispatcher, CommFactory.slave_side);
		pThread = new Thread(pollingThr);
		pThread.start();
		
		int cnt = 0;
		while(!dispatcher.isEcho() && cnt < 5){
			try {
				Thread.sleep(100);
				cnt++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(cnt >= 5){
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			socket = null;
			pThread.interrupt();
			dispatcher.setEcho(false);
			pollingThr.setRunning(false);
			System.out.println("[CommSlaveHandler][E]Time out when wait for the connection echo!");
			return false;
		}
		System.out.println("[CommSlaveHandler][I]Get the echo when connect!");
				
		return true;
	}

}
