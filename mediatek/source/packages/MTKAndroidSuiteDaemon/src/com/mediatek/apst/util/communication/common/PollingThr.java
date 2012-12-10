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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.mediatek.apst.util.communication.comm.CommFactory;
import com.mediatek.apst.util.communication.connManager.ConnDisconnectExpt;
import com.mediatek.apst.util.communication.connManager.ConnDisconnectInfo;
import com.mediatek.apst.util.communication.connManager.ConnManageEntity;

/** 
 * Class Name: PollingThr
 * <p>Package: com.mediatek.apst.util.communication.common
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Runnable for polling data from socket IO stream.
 * <p>
 * @author mtk80251
 * @version V1.0
 */
public class PollingThr implements Runnable {
	private ObjectInputStream mOis;
	private ObjectOutputStream mOos;
	private Dispatcher mDispatcher;
	private boolean bRunning;
	
	private int mRole;
	
	/**
	 * Constructor.
	 */
	public PollingThr() {
		mOis = null;
		mDispatcher = null;
		bRunning = false;
		mRole = -1;
	}
	
	/**
	 * Constructor with parameters.
	 * @param ois ObjectInputStream used for the polling thread.
	 * @param oos ObjectOutputStream used for the polling thread.
	 * @param dispatcher Dispatcher used for the polling thread.
	 * @param role Which side is polling thread working on.
	 */
	public PollingThr(ObjectInputStream ois, ObjectOutputStream oos, Dispatcher dispatcher, int role) {
		mOis = ois;
		mOos = oos;
		mDispatcher = dispatcher;
		mRole = role;
        bRunning = false;
	}
	
	/**
	 * Set whether the runnable should run. 
	 */
	public void setRunning(boolean bRunning) {
		this.bRunning = bRunning;
	}
	
	//@Override
	public void run() {
		if(mDispatcher == null || mOis == null){
			System.out.println("[PollingThr]The dispatcher or stream object is null!");
			return;
		}
		bRunning = true;
		while(bRunning){
			System.out.println("[PollingThr]waiting for data...");
			try {
				Object obj = mOis.readObject();
				if(obj != null){
					System.out.println("[PollingThr:info]Recieved data");
					if(obj instanceof String){
						if(obj.equals("DISC")){
							System.out.println("[PollingThr]Get DISC Symbol! The polling thread will exit!");
							if(mRole == CommFactory.host_side){
							    // Send 'DISC' to stop remote PC polling thread
								mOos.writeObject("DISC");  
							}
							System.out.println("[PollingThr]Stop normally!");
							// Here should notify connection management
							if(mRole == CommFactory.host_side){
								mDispatcher.dispatch(new ConnDisconnectInfo(ConnManageEntity.connection_status_success));
							}
							bRunning = false;
							return;
						}
					}
					mDispatcher.dispatch(obj);
				}
				else{
					System.out.println("[PollingThr:warning]Get NULL! We will ignore it!");
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("[PollingThr:error]The polling thread get IO Exception, and the thread is stoped!");
				mDispatcher.dispatch(new ConnDisconnectExpt(e));
				bRunning = false;
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				bRunning = false;
                return;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

		}
	}
	
	/**
	 * Whether the runnable is running.
	 * @return True if running, otherwise false.
	 */
	public boolean isRunning() {
		return bRunning;
	}
	
}
