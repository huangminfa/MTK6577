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

package com.mediatek.apst.target.ftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.target.util.Debugger;

public class FtpServer extends Thread {

	private static final Object[] TAG = new Object[] { "APST/FTP" };

	private final int FTP_PORT = 2222;
	ServerSocket ftpsocket = null;
	private List<FtpConnection> ftpConnThreads = new ArrayList<FtpConnection>();

	public void run() {
		Socket client = null;
		try {
			ftpsocket = new ServerSocket(FTP_PORT);
			Debugger.logI(TAG, "listening port: " + FTP_PORT);
			for (;;) {
				client = ftpsocket.accept();
				FtpConnection newFTPConn = new FtpConnection(client);
				newFTPConn.start();
				registerSessionThread(newFTPConn);
			}
		} catch (SocketException se) {
			Debugger.logW(TAG, "SocketException: " + se.getMessage());
		} catch (Exception e) {
			Debugger.logW(TAG, "Exception: ftpsocket " + e.getMessage());
		}
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				Debugger.logW(TAG, "IOException: client " + e.getMessage());
			}
		}
	}

	public void registerSessionThread(FtpConnection ftpConnect) {
		synchronized (this) {
			List<FtpConnection> toBeRemoved = new ArrayList<FtpConnection>();
			for (FtpConnection ftpConn : ftpConnThreads) {
				if (!ftpConn.isAlive()) {
					Debugger.logI(TAG, "Cleaning up finished session...");
					try {
						ftpConn.join();
						Debugger.logI(TAG, "Thread joined");
						toBeRemoved.add(ftpConn);
						ftpConn.closeSocket(); // make sure socket closed
					} catch (InterruptedException e) {
						Debugger.logI(TAG, "Interrupted while joining");
					}
				}
			}
			for (FtpConnection removeThread : toBeRemoved) {
				ftpConnThreads.remove(removeThread);
			}
			ftpConnThreads.add(ftpConnect);
		}
		Debugger.logI(TAG, "Registered session thread");
	}

	@Override
	public void destroy() {
		if (ftpsocket != null) {
			try {
				ftpsocket.close();
				ftpsocket = null;
			} catch (IOException e) {
				Debugger.logW(TAG, "IOException: ftpsocket " + e.getMessage());
			}
		}
	}
}
