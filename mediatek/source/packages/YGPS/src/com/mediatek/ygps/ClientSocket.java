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

package com.mediatek.ygps;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mediatek.xlog.Xlog;

public class ClientSocket {

    private static final String TAG = "EM/YGPS_ClientSocket";
    private static final int SERVER_PORT = 7000;
    private Socket localSocket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private String command = null;
    private String response = null;
    private BlockingQueue<String> queue = null;
    private YGPSActivity acti = null;
    private Thread sendThread = null;
    private byte[] buffer = null;

    public ClientSocket(YGPSActivity acti) {
        Xlog.v(TAG, "ClientSocket constructor");
        this.acti = acti;
        queue = new LinkedBlockingQueue<String>();
        buffer = new byte[1024];
        sendThread = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        command = queue.take();
                        Xlog.v(TAG, "Queue take command:" + command);
                    } catch (InterruptedException ie) {
                        Xlog.w(TAG, "Take command interrupted:"
                                + ie.getMessage());
                        return;
                    }
                    startClient();
                    if (null != out && null != in) {
                        try {
                            // in = new
                            // DataInputStream(localSocket.getInputStream());
                            out.writeBytes(command);
                            out.write('\r');
                            out.write('\n');
                            out.flush();
                            String result = null;
                            int line = 0;
                            int count = -1;
                            while ((count = in.read(buffer)) != -1) {
                                line++;
                                result = new String(buffer, 0, count);
                                Xlog.v(TAG, "line: " + line + " sentence: "
                                        + result);
                                if (result.contains("PMTK")) {
                                    response = result;
                                    Xlog.v(TAG, "Get response from MNL: "
                                            + result);
                                    break;
                                }
                                if (line > 10) {
                                    response = "TIMEOUT";
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            Xlog.w(TAG, "sendCommand IOException: "
                                    + e.getMessage());
                            response = "ERROR";
                        }
                    } else {
                        Xlog.d(TAG, "out is null");
                        response = "ERROR";
                    }
                    if (null != ClientSocket.this.acti) {
                        ClientSocket.this.acti.getResponse(response);
                    }
                    command = null;
                    closeClient();
                }
            }
        });
        sendThread.start();
    }

    private void startClient() {
        Xlog.v(TAG, "enter startClient");
        if (null != localSocket && localSocket.isConnected()) {
            Xlog.d(TAG, "localSocket has started, return");
            return;
        }
        try {
            localSocket = new Socket("127.0.0.1", SERVER_PORT);
            localSocket.setSoTimeout(2000);
            out = new DataOutputStream(localSocket.getOutputStream());
            in = new DataInputStream(localSocket.getInputStream());
        } catch (UnknownHostException e) {
            Xlog.w(TAG, e.getMessage());
        } catch (IOException e) {
            Xlog.w(TAG, e.getMessage());
        } catch (Exception e) {
            Xlog.w(TAG, "startClient exception");
        }
    }

    private void closeClient() {
        Xlog.v(TAG, "enter closeClient");
        try {
            if (null != in) {
                in.close();
            }
            if (null != out) {
                out.close();
            }
            if (null != localSocket) {
                localSocket.close();
            }
        } catch (IOException e) {
            Xlog.w(TAG, "closeClient IOException: " + e.getMessage());
        } finally {
            localSocket = null;
            in = null;
            out = null;
        }
    }

    public void endClient() {
        Xlog.v(TAG, "enter endClient");
        sendThread.interrupt();
        Xlog.v(TAG, "Queue remaining:" + queue.size());
        closeClient();
        acti = null;
    }

    public void sendCommand(String command) {
        Xlog.v(TAG, "enter sendCommand");
        String sendComm = "$" + command + "*" + calcCS(command);
        Xlog.v(TAG, "Send command: " + sendComm);
        if (!sendThread.isAlive()) {
            Xlog.v(TAG, "sendThread is not alive");
            sendThread.start();
        }
        if (command.equals(sendComm) || queue.contains(sendComm)) {
            Xlog.v(TAG,"send command return because of hasn't handle the same");
            return;
        }
        try {
            queue.put(sendComm);
        } catch (InterruptedException ie) {
            Xlog.w(TAG, "send command interrupted:" + ie.getMessage());
        }
    }

    private String calcCS(String command) {
        if (null == command || "".equals(command)) {
            return "";
        }
        byte[] ba = command.toUpperCase().getBytes();
        short cs = 0;
        for (byte b : ba) {
            cs ^= b;
        }
        return String.format("%1$02x", cs & 0xFF).toUpperCase();
    }
}

