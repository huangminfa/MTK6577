/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.cat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InterruptedIOException;

abstract class Channel {
    protected int mChannelId = -1;
    protected int mChannelStatus = BipUtils.CHANNEL_STATUS_UNKNOWN;
    protected int mLinkMode = 0;
    protected int mProtocolType = BipUtils.TRANSPORT_PROTOCOL_UNKNOWN;

    protected InetAddress mAddress = null;
    protected int mPort = 0;
    private CatService mHandler = null;

    protected int mBufferSize = 0;
    protected byte[] mRxBuffer = null;
    protected byte[] mTxBuffer = null;
    protected int mRxBufferCount = 0;
    protected int mRxBufferOffset = 0;
    protected int mTxBufferCount = 0;

    protected ReceiveDataResult mRecvDataRet = null;
    protected int needCopy = 0;

    protected boolean isChannelOpened = false;
    
    protected static final int SOCKET_TIMEOUT = 3000;

    protected Object mLock;
    Channel(int cid, int linkMode, int protocolType, InetAddress address, int port, int bufferSize, CatService handler) {
        this.mChannelId = cid;
        this.mLinkMode = linkMode;
        this.mProtocolType = protocolType;
        this.mAddress = address;
        this.mPort = port;
        this.mBufferSize = bufferSize;
        this.mLock = new Object();
        this.mHandler = handler;
    }

    abstract public int openChannel(CatCmdMessage cmdMsg);
    abstract public int closeChannel();
    abstract public ReceiveDataResult receiveData(int requestSize);
    abstract public int receiveData(int requestSize, ReceiveDataResult rdr);
    abstract public int sendData(byte[] data, int mode);
    abstract public int getTxAvailBufferSize(); 

    public void dataAvailable(int bufferSize){
        CatResponseMessage resMsg = new CatResponseMessage(CatService.EVENT_LIST_ELEMENT_DATA_AVAILABLE);

        byte[] additionalInfo = new byte[7];
        additionalInfo[0] = (byte) 0xB8; // Channel status
        additionalInfo[1] = 0x02;
        additionalInfo[2] = (byte) (getChannelId() | ChannelStatus.CHANNEL_STATUS_LINK);
        additionalInfo[3] = ChannelStatus.CHANNEL_STATUS_INFO_NO_FURTHER_INFO;

        additionalInfo[4] = (byte) 0xB7; // Channel data length
        additionalInfo[5] = 0x01;

        if(bufferSize > 0xff){
            additionalInfo[6] = (byte) 0xff;
        }else{
            additionalInfo[6] = (byte) bufferSize;
        }

        resMsg.setSourceId(0x82);
        resMsg.setDestinationId(0x81);
        resMsg.setAdditionalInfo(additionalInfo);
        resMsg.setOneShot(false);
        CatLog.d(this,"onEventDownload for dataAvailable");
        mHandler.onEventDownload(resMsg);
    }

    public int getChannelStatus() {
        return mChannelStatus;
    }

    public int getChannelId() {
        return mChannelId;
    }

    protected int checkBufferSize() {
        int minBufferSize = 0;
        int maxBufferSize = 0;
        int defaultBufferSize = 0;

        if(mProtocolType == BipUtils.TRANSPORT_PROTOCOL_TCP_LOCAL || mProtocolType == BipUtils.TRANSPORT_PROTOCOL_TCP_REMOTE) {
            minBufferSize = BipUtils.MIN_BUFFERSIZE_TCP;
            maxBufferSize = BipUtils.MAX_BUFFERSIZE_TCP;
            defaultBufferSize = BipUtils.DEFAULT_BUFFERSIZE_TCP;
        } else if(mProtocolType == BipUtils.TRANSPORT_PROTOCOL_UDP_LOCAL || mProtocolType == BipUtils.TRANSPORT_PROTOCOL_UDP_REMOTE) {
            minBufferSize = BipUtils.MIN_BUFFERSIZE_UDP;
            maxBufferSize = BipUtils.MAX_BUFFERSIZE_UDP;
            defaultBufferSize = BipUtils.DEFAULT_BUFFERSIZE_UDP;
        }

        CatLog.d("[BIP]", "mBufferSize:" + mBufferSize + " minBufferSize:" + minBufferSize + " maxBufferSize:" + maxBufferSize);

        if(mBufferSize >= minBufferSize && mBufferSize <= maxBufferSize) {
            CatLog.d("[BIP]", "buffer size is normal");
            return ErrorValue.NO_ERROR;
        } else {
            if(mBufferSize > maxBufferSize) {
                CatLog.d("[BIP]", "buffer size is too large, change it to maximum value");
                mBufferSize = maxBufferSize;
            } else {
                CatLog.d("[BIP]", "buffer size is too small, change it to default value");
                mBufferSize = defaultBufferSize;
            }
        }

        if(mBufferSize < BipUtils.MAX_APDU_SIZE) {
            CatLog.d("[BIP]", "buffer size is smaller than 255, change it to MAX_APDU_SIZE");
            mBufferSize  = BipUtils.MAX_APDU_SIZE;
        }

        return ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION;
    }

    protected class UdpReceiverThread implements Runnable {
        DatagramSocket udpSocket;

        UdpReceiverThread(DatagramSocket s){
            udpSocket = s;
        }

        @Override
        public void run() {
            byte[] localBuffer=new byte[BipUtils.MAX_BUFFERSIZE_UDP];

            CatLog.d("[BIP]", "UdpReceiverThread run");
            DatagramPacket recvPacket=new DatagramPacket(localBuffer,localBuffer.length);
            try{
                //while(true){                        
                    CatLog.d("[BIP]", "Before UdpReceiverThread: Receive data from network");
                    udpSocket.receive(recvPacket);
                    int recvLen = recvPacket.getLength();
                    CatLog.d("[BIP]", "UdpReceiverThread: Receive data from network:" + recvLen);
                    if(recvLen >= 0){
                        System.arraycopy(localBuffer, 0, mRxBuffer, 0, recvLen);
                        mRxBufferCount = recvLen;
                        mRxBufferOffset = 0;
                        dataAvailable(mRxBufferCount);
                    }
                    CatLog.d("[BIP]", "UdpReceiverThread: buffer data:" + mRxBufferCount);            
                //}
            }catch(Exception e){
                CatLog.d("[BIP]", "Error in UdpReceiverThread"); 
                e.printStackTrace(); 
            }
        }
    }

    protected class TcpReceiverThread implements Runnable {
        DataInputStream di;

        TcpReceiverThread(DataInputStream s){
            di = s;
        }

        @Override
        public void run() {
            byte[] localBuffer=new byte[BipUtils.MAX_BUFFERSIZE_TCP];

            CatLog.d("[BIP]", "TcpReceiverThread run");

            try{
                //while(true){                        
                    CatLog.d("[BIP]", "Before TcpReceiverThread: Receive data from network");
                    int recvLen = di.read(localBuffer);
                    CatLog.d("[BIP]", "TcpReceiverThread: Receive data from network:" + recvLen);
                    if(recvLen >= 0){
                        System.arraycopy(localBuffer, 0, mRxBuffer, 0, recvLen);
                        mRxBufferCount = recvLen;
                        mRxBufferOffset = 0;
                        dataAvailable(mRxBufferCount);
                    }
                    CatLog.d("[BIP]", "TcpReceiverThread: buffer data:" + mRxBufferCount);            
                //}
            }catch(Exception e){
                e.printStackTrace(); 
            }
        }
    }     
}


class TcpChannel extends Channel {
    Socket mSocket = null;
    DataInputStream mInput = null;
    BufferedOutputStream mOutput = null;
    Thread rt;

    TcpChannel(int cid, int linkMode, int protocolType, InetAddress address, int port, int bufferSize, CatService handler) {
        super(cid, linkMode, protocolType, address, port, bufferSize, handler);
    }

    public int openChannel(CatCmdMessage cmdMsg) {
        int ret = ErrorValue.NO_ERROR;

        if(mLinkMode == BipUtils.LINK_ESTABLISHMENT_MODE_IMMEDIATE) {
            try {
                mSocket = new Socket(mAddress, mPort);
                if(mSocket.isConnected()) {
                    mChannelStatus = BipUtils.CHANNEL_STATUS_OPEN;
                } else {
                    mChannelStatus = BipUtils.CHANNEL_STATUS_ERROR;
                }
            } catch(IOException e) {
                CatLog.d("[BIP]", "Fail to create tcp socket");
                return ErrorValue.BIP_ERROR;
            } catch(Exception e2) {
                CatLog.d("[BIP]", "fail to create tcp socket " + e2);
                return ErrorValue.BIP_ERROR;
            }

            if(mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN) {
                try {
                    CatLog.d("[BIP]", "TCP stream is open");
                    mInput = new DataInputStream(mSocket.getInputStream());
                    mOutput = new BufferedOutputStream(mSocket.getOutputStream());
                    rt = new Thread(new TcpReceiverThread(mInput));
                    rt.start();
                } catch(IOException e) {
                    CatLog.d("[BIP]", "Fail to create data stream");
                    return ErrorValue.BIP_ERROR;
                }
            } else {
                CatLog.d("[BIP]", "tcp socket is not open");
                return ErrorValue.BIP_ERROR;
            }

            ret = checkBufferSize();
            if(ret == ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION) {
                CatLog.d("[BIP]", "TcpChannel-openChannel: buffer size is modified");
                cmdMsg.mBufferSize = mBufferSize;
            }
            mRxBuffer = new byte[mBufferSize];
            mTxBuffer = new byte[mBufferSize];
        }

        return ret;
    }

    public int closeChannel() {
        int ret = ErrorValue.NO_ERROR;

        try {
            mInput.close();
            mOutput.close();
            mSocket.close();
        } catch(IOException e) {
        } finally {
            mSocket = null;
            mRxBuffer = null;
            mTxBuffer = null;
            mChannelStatus = BipUtils.CHANNEL_STATUS_CLOSE;
        }

        return ret;
    }

    public ReceiveDataResult receiveData(int requestCount) {

        ReceiveDataResult ret = new ReceiveDataResult();


        ret.buffer = new byte[requestCount];
        CatLog.d("[BIP]", "receiveData " + mRxBufferCount + "/" + requestCount + "/" + mRxBufferOffset);

        if(mRxBufferCount >= requestCount) {
            try {
                CatLog.d("[BIP]", "Start to copy data from buffer");

                System.arraycopy(mRxBuffer, mRxBufferOffset, ret.buffer, 0, requestCount);
                mRxBufferCount -= requestCount;
                mRxBufferOffset += requestCount;
                ret.remainingCount = mRxBufferCount;
            } catch(IndexOutOfBoundsException e) {}
        } else {
            int needCopy = requestCount;
            int canCopy = mRxBufferCount;
            int countCopied = 0;
            boolean canExitLoop = false;

            while(!canExitLoop) {
                if(needCopy > canCopy) {
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, ret.buffer, countCopied, canCopy);
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                        countCopied += canCopy;
                        needCopy -= canCopy;
                    } catch(IndexOutOfBoundsException e) {}
                } else {
                    try {
                        System.arraycopy(mRxBufferCount, mRxBufferOffset, ret.buffer, countCopied, canCopy);
                        mRxBufferOffset += needCopy;
                        countCopied += needCopy;
                        needCopy = 0;
                    } catch(IndexOutOfBoundsException e) {}
                }

                if(needCopy == 0) {
                    canExitLoop = true;
                } else {
                    try {
                        int count = mInput.read(mRxBuffer, 0, mRxBuffer.length);
                        mRxBufferCount = count;
                        mRxBufferOffset = 0;
                    } catch(IOException e) {}
                }
            }
        }

        return ret;
    }

    public int sendData(byte[] data, int mode) {
        int ret = ErrorValue.NO_ERROR;
        int txRemaining = mTxBuffer.length - mTxBufferCount;


        CatLog.d("[BIP]", "sendData: size of buffer:" + data.length + " mode:" + mode);
        CatLog.d("[BIP]", "sendData: size of buffer:" + mTxBuffer.length + " count:" + mTxBufferCount);

        try {
            if(txRemaining >= data.length) {
                System.arraycopy(data, 0, mTxBuffer, mTxBufferCount, data.length);
                mTxBufferCount += data.length;
            } else {
                CatLog.d("[BIP]", "sendData - tx buffer is not enough");
            }
        } catch(IndexOutOfBoundsException e) {
            return ErrorValue.BIP_ERROR;
        }

        if(mode == BipUtils.SEND_DATA_MODE_IMMEDIATE && mChannelStatus == BipUtils.CHANNEL_STATUS_OPEN) {
            try {
                CatLog.d("[BIP]", "SEND_DATA_MODE_IMMEDIATE:" + mTxBuffer.length + " count:" + mTxBufferCount);
                mOutput.write(mTxBuffer, 0, mTxBufferCount);
                mOutput.flush();
                mTxBufferCount = 0;
            } catch(IOException e) {
                e.printStackTrace();
                return ErrorValue.BIP_ERROR;
            }
        }

        return ret;
    }

    public int getTxAvailBufferSize() {
        int txRemaining = mTxBuffer.length - mTxBufferCount;
        CatLog.d("[BIP]", "available tx buffer size:" + txRemaining);
        return txRemaining;
    }


    public int receiveData(int requestSize, ReceiveDataResult rdr) {
        CatLog.d("[BIP]", "new receiveData method");
        int ret = ErrorValue.NO_ERROR;
        
        if(rdr == null) {
            CatLog.d("[BIP]", "rdr is null");
            return ErrorValue.BIP_ERROR;
        }
        
        CatLog.d("[BIP]", "receiveData " + mRxBufferCount + "/" + requestSize + "/" + mRxBufferOffset);
        
        rdr.buffer = new byte[requestSize];
        if(mRxBufferCount >= requestSize) {
            CatLog.d("[BIP]", "rx buffer has enough data");
            try {
                System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, 0, requestSize);
                mRxBufferOffset += requestSize;
                mRxBufferCount -= requestSize;
                rdr.remainingCount = mRxBufferCount;
            } catch(IndexOutOfBoundsException e) {
                CatLog.d("[BIP]", "fail copy rx buffer out 1");
                return ErrorValue.BIP_ERROR;
            }
        } else {
            CatLog.d("[BIP]", "rx buffer is insufficient");
            int needCopy = requestSize;
            int canCopy = mRxBufferCount;
            int hasCopied = 0;
            boolean canExit = false;
            
            while(canExit != true) {
                CatLog.d("[BIP]", "hasCopied/needCopy: " + hasCopied + "/" + needCopy);
                if(needCopy > canCopy) {
                    CatLog.d("[BIP]", "canCopy=" + canCopy);
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, hasCopied, canCopy);
                        hasCopied += canCopy;
                        needCopy -= canCopy;
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                    } catch(IndexOutOfBoundsException e) {
                        CatLog.d("[BIP]", "fail copy rx buffer out 2");
                        return ErrorValue.BIP_ERROR;
                    }
                } else {
                    CatLog.d("[BIP]", "copy will complete");
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, hasCopied, needCopy);
                        hasCopied += canCopy;
                        needCopy = 0;
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                    }catch(IndexOutOfBoundsException e) {
                        CatLog.d("[BIP]", "fail copy rx buffer out 3");
                        return ErrorValue.BIP_ERROR;
                    }
                }
                
                if(needCopy <= 0) {
                    CatLog.d("[BIP]", "can exit the loop");
                    rdr.remainingCount = mRxBufferCount;
                    canExit = true;
                } else {
                    try {
                        mSocket.setSoTimeout(SOCKET_TIMEOUT);
                        mRxBufferCount = mInput.read(mRxBuffer, 0, mRxBuffer.length);
                        mRxBufferOffset = 0;
                    } catch(InterruptedIOException e) {
                        CatLog.d("[BIP]", "receive timeout");
                        rdr.remainingCount = 0;
                        byte[] temp = new byte[hasCopied];
                        try {
                            CatLog.d("[BIP]", "just copy " + hasCopied);
                            System.arraycopy(rdr.buffer, 0, temp, 0, hasCopied);
                        } catch(IndexOutOfBoundsException e2) {
                            CatLog.d("[BIP]", "fail to process hasCopied data");
                            rdr.remainingCount = 0;
                            ret = ErrorValue.BIP_ERROR;
                            break;
                        }
                        rdr.buffer = temp;
                        ret = ErrorValue.MISSING_DATA;
                        break;
                    } catch(Exception ex) {
                        rdr.remainingCount = 0;
                        ret = ErrorValue.BIP_ERROR;
                        break;
                    }
                }
            } // end of while
        }
        
        return ret;
    }
}


class UdpChannel extends Channel {
    DatagramSocket mSocket = null;
    private static final int SOCKET_TIMEOUT = 3000;
    Thread rt;

    UdpChannel(int cid, int linkMode, int protocolType, InetAddress address, int port, int bufferSize, CatService handler) {
        super(cid, linkMode, protocolType, address, port, bufferSize, handler);
    }

    public int openChannel(CatCmdMessage cmdMsg) {
        int ret = ErrorValue.NO_ERROR;

        CatLog.d("[BIP]", "UDP link mode:" + mLinkMode);

        if(mLinkMode == BipUtils.LINK_ESTABLISHMENT_MODE_IMMEDIATE) {
            try {
                mSocket = new DatagramSocket();
                mChannelStatus = BipUtils.CHANNEL_STATUS_OPEN;
                rt = new Thread(new UdpReceiverThread(mSocket));
                rt.start();
                CatLog.d("[BIP]", "UdpChannel: sock status:" + mChannelStatus);
            } catch(Exception e) {
                e.printStackTrace();    
            }

            ret = checkBufferSize();
            if(ret == ErrorValue.COMMAND_PERFORMED_WITH_MODIFICATION) {
                CatLog.d("[BIP]", "UdpChannel-openChannel: buffer size is modified");
                cmdMsg.mBufferSize = mBufferSize;
            }
            mRxBuffer = new byte[mBufferSize];
            mTxBuffer = new byte[mBufferSize];
        }

        return ret;
    }

    public int closeChannel() {
        int ret = ErrorValue.NO_ERROR;

        CatLog.d("[BIP]", "closeChannel enter");

        if(mSocket != null) {

            CatLog.d("[BIP]", "closeSocket enter");

            mSocket.close();
            mChannelStatus = BipUtils.CHANNEL_STATUS_CLOSE;

            mSocket = null;
            mRxBuffer = null;
            mTxBuffer = null;
        }

        return ret;
    }

    public ReceiveDataResult receiveData(int requestCount) {
        ReceiveDataResult ret = new ReceiveDataResult();
        ret.buffer = new byte[requestCount];

        CatLog.d("[BIP]", "receiveData " + mRxBufferCount + "/" + requestCount + "/" + mRxBufferOffset);

        if(mRxBufferCount >= requestCount) {
            try {
                System.arraycopy(mRxBuffer, mRxBufferOffset, ret.buffer, 0, requestCount);
                mRxBufferOffset += requestCount;
                mRxBufferCount -= requestCount;
                ret.remainingCount = mRxBufferCount;
            } catch(IndexOutOfBoundsException e) {}
        } else {
            int needCopy = requestCount;
            int canCopy = mRxBufferCount;
            int countCopied = 0;
            boolean canExitLoop = false;

            while(!canExitLoop) {
                if(needCopy > canCopy) {
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, ret.buffer, countCopied, canCopy);
                        countCopied += canCopy;
                        needCopy -= canCopy;
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                    } catch(IndexOutOfBoundsException e) {}
                } else {
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, ret.buffer, countCopied, needCopy);
                        mRxBufferOffset += needCopy;
                        mRxBufferCount -= needCopy;
                        countCopied += needCopy;
                        needCopy = 0;
                    } catch(IndexOutOfBoundsException e) {}
                }

                if(needCopy == 0) {
                    canExitLoop = true;
                } else {
                    try {
                        mSocket.setSoTimeout(SOCKET_TIMEOUT);
                        DatagramPacket packet = new DatagramPacket(mRxBuffer, mRxBuffer.length);
                        mSocket.receive(packet);
                        mRxBufferOffset = 0;
                        mRxBufferCount = packet.getLength();
                    } catch(Exception e) {
                        e.printStackTrace();
                    } 
                }
            }
        }

        return ret;
    }

    public int sendData(byte[] data, int mode) {
        int ret = ErrorValue.NO_ERROR;         
        int txRemaining = mTxBuffer.length - mTxBufferCount;

        CatLog.d("[BIP]", "sendData: size of data:" + data.length + " mode:" + mode);
        CatLog.d("[BIP]", "sendData: size of buffer:" + mTxBuffer.length + " count:" + mTxBufferCount);

        if(txRemaining >= data.length) {
            try {
                System.arraycopy(data, 0, mTxBuffer, mTxBufferCount, data.length);
                mTxBufferCount += data.length;
            } catch(IndexOutOfBoundsException e) {}
        } else {
            CatLog.d("[BIP]", "sendData - tx buffer is not enough:" + txRemaining);
        }

        if(mode == BipUtils.SEND_DATA_MODE_IMMEDIATE) {
            CatLog.d("[BIP]", "Send UDP data(" + mAddress + ":" + mPort + "):" + mTxBuffer.length + " count:" + mTxBufferCount);

            DatagramPacket packet = new DatagramPacket(mTxBuffer, 0, mTxBufferCount, mAddress, mPort);
            if(mSocket != null) {
                try {
                    mSocket.send(packet);
                    mTxBufferCount = 0;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    public int getTxAvailBufferSize() {
        int txRemaining = mTxBuffer.length - mTxBufferCount;
        CatLog.d("[BIP]", "available tx buffer size:" + txRemaining);
        return txRemaining;
    }
    
    public int receiveData(int requestSize, ReceiveDataResult rdr) {
        CatLog.d("[BIP]", "new receiveData method");
        int ret = ErrorValue.NO_ERROR;
        
        if(rdr == null) {
            CatLog.d("[BIP]", "rdr is null");
            return ErrorValue.BIP_ERROR;
        }
        
        CatLog.d("[BIP]", "receiveData " + mRxBufferCount + "/" + requestSize + "/" + mRxBufferOffset);
        
        rdr.buffer = new byte[requestSize];
        if(mRxBufferCount >= requestSize) {
            CatLog.d("[BIP]", "rx buffer has enough data");
            try {
                System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, 0, requestSize);
                mRxBufferOffset += requestSize;
                mRxBufferCount -= requestSize;
                rdr.remainingCount = mRxBufferCount;
            } catch(IndexOutOfBoundsException e) {
                CatLog.d("[BIP]", "fail copy rx buffer out 1");
                return ErrorValue.BIP_ERROR;
            }
        } else {
            CatLog.d("[BIP]", "rx buffer is insufficient");
            int needCopy = requestSize;
            int canCopy = mRxBufferCount;
            int hasCopied = 0;
            boolean canExit = false;
            
            while(canExit != true) {
                CatLog.d("[BIP]", "hasCopied/needCopy: " + hasCopied + "/" + needCopy);
                if(needCopy > canCopy) {
                    CatLog.d("[BIP]", "canCopy=" + canCopy);
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, hasCopied, canCopy);
                        hasCopied += canCopy;
                        needCopy -= canCopy;
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                    } catch(IndexOutOfBoundsException e) {
                        CatLog.d("[BIP]", "fail copy rx buffer out 2");
                        return ErrorValue.BIP_ERROR;
                    }
                } else {
                    CatLog.d("[BIP]", "copy will complete");
                    try {
                        System.arraycopy(mRxBuffer, mRxBufferOffset, rdr.buffer, hasCopied, needCopy);
                        hasCopied += canCopy;
                        needCopy = 0;
                        mRxBufferOffset += canCopy;
                        mRxBufferCount -= canCopy;
                    }catch(IndexOutOfBoundsException e) {
                        CatLog.d("[BIP]", "fail copy rx buffer out 3");
                        return ErrorValue.BIP_ERROR;
                    }
                }
                
                if(needCopy <= 0) {
                    CatLog.d("[BIP]", "can exit the loop");
                    rdr.remainingCount = mRxBufferCount;
                    canExit = true;
                } else {
                    try {
                        mSocket.setSoTimeout(SOCKET_TIMEOUT);
                        DatagramPacket p = new DatagramPacket(mRxBuffer, mRxBuffer.length);
                        mSocket.receive(p);
                        mRxBufferOffset = 0;
                        mRxBufferCount = p.getLength();
                    } catch(InterruptedIOException e) {
                        CatLog.d("[BIP]", "receive timeout");
                        rdr.remainingCount = 0;
                        byte[] temp = new byte[hasCopied];
                        try {
                            CatLog.d("[BIP]", "just copy " + hasCopied);
                            System.arraycopy(rdr.buffer, 0, temp, 0, hasCopied);
                        } catch(IndexOutOfBoundsException e2) {
                            CatLog.d("[BIP]", "fail to process hasCopied data");
                            rdr.remainingCount = 0;
                            ret = ErrorValue.BIP_ERROR;
                            break;
                        }
                        rdr.buffer = temp;
                        ret = ErrorValue.MISSING_DATA;
                        break;
                    } catch(Exception ex) {
                        rdr.remainingCount = 0;
                        ret = ErrorValue.BIP_ERROR;
                        break;
                    }
                }
            } // end of while
        }
        
        return ret;
    }
}
