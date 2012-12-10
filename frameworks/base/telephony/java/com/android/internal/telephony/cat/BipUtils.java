/*
 * Copyright (C) 2006-2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class BipUtils {
    final public static int MAX_APDU_SIZE = 210;

    final public static int MIN_CHANNEL_ID = 1;
    final public static int MAX_CHANNEL_ID = 1;

    final public static int MAX_CHANNELS_GPRS_ALLOWED = 3;
    final public static int MAX_CHANNELS_CSD_ALLOWED  = 1;

    final public static int BEARER_TYPE_UNKNOWN = 0;
    final public static int BEARER_TYPE_CSD     = 0x01;
    final public static int BEARER_TYPE_GPRS    = 0x02;
    final public static int BEARER_TYPE_DEFAULT = 0x03;

    final public static int MIN_BUFFERSIZE_TCP     = 255;
    final public static int MAX_BUFFERSIZE_TCP     = 1400;
    final public static int DEFAULT_BUFFERSIZE_TCP = 1024;
    final public static int MIN_BUFFERSIZE_UDP     = 255;
    final public static int MAX_BUFFERSIZE_UDP     = 1400;
    final public static int DEFAULT_BUFFERSIZE_UDP = 1024;


    final public static int TRANSPORT_PROTOCOL_UNKNOWN    = 0;
    final public static int TRANSPORT_PROTOCOL_UDP_REMOTE = 0x01;
    final public static int TRANSPORT_PROTOCOL_TCP_REMOTE = 0x02;
    final public static int TRANSPORT_PROTOCOL_SERVER     = 0x03;
    final public static int TRANSPORT_PROTOCOL_UDP_LOCAL  = 0x04;
    final public static int TRANSPORT_PROTOCOL_TCP_LOCAL  = 0x05;

    final public static int ADDRESS_TYPE_UNKNOWN = 0;
    final public static int ADDRESS_TYPE_IPV4 = 0x21;
    final public static int ADDRESS_TYPE_IPV6 = 0x57;

    final public static int ADDRESS_IPV4_LENGTH = 4;
    final public static int ADDRRES_IPV6_LENGTH = 16;

    final public static int CHANNEL_STATUS_UNKNOWN      = 0;
    final public static int CHANNEL_STATUS_ONDEMAND     = 0x01;
    final public static int CHANNEL_STATUS_CLOSE        = 0x02;
    final public static int CHANNEL_STATUS_SERVER_CLOSE = 0x03;
    final public static int CHANNEL_STATUS_OPEN         = 0x04;
    final public static int CHANNEL_STATUS_LINK_DROPPED = 0x05;
    final public static int CHANNEL_STATUS_TIMEOUT      = 0x06;
    final public static int CHANNEL_STATUS_ERROR        = 0x07;

    final public static int LINK_ESTABLISHMENT_MODE_IMMEDIATE = 0;
    final public static int LINK_ESTABLISHMENT_MODE_ONDEMMAND = 1;

    final public static int SEND_DATA_MODE_IMMEDIATE = 1;
    final public static int SEND_DATA_MODE_STORED = 0;

    final public static String KEY_QOS_CID = "cid";
    final public static String KEY_QOS_PRECEDENCE = "precedence";
    final public static String KEY_QOS_DELAY = "delay";
    final public static String KEY_QOS_RELIABILITY = "reliability";
    final public static String KEY_QOS_PEAK = "peak";
    final public static String KEY_QOS_MEAN = "mean";
}

class ErrorValue {
    final public static int NO_ERROR = 0;
    // final public static int BUFFER_SIZE_MODIFIED = NO_ERROR + 1;
    final public static int NETWORK_CURRENTLY_UNABLE_TO_PROCESS_COMMAND = NO_ERROR + 2;
    final public static int COMMAND_PERFORMED_WITH_MODIFICATION = NO_ERROR + 3;
    final public static int UNSUPPORTED_TRANSPORT_PROTOCOL_TYPE = NO_ERROR + 4;
    final public static int BIP_ERROR = NO_ERROR + 5;
    final public static int ME_IS_BUSY_ON_CALL = NO_ERROR + 6;
    final public static int CHANNEL_ID_NOT_VALID = NO_ERROR + 7;
    final public static int CHANNEL_ALREADY_CLOSED = NO_ERROR + 8;
    final public static int MISSING_DATA = NO_ERROR + 9;
}

/*
class BearerDesc {
      public int bearerType = 0;

      public int precedence = 0;
      public int delay = 0;
      public int reliability = 0;
      public int peak = 0;
      public int mean = 0;
      public int pdpType = 0;

      public int dataCompression = 0;
      public int headerCompression = 0;

      public int dataRate = 0;
      public int bearerService = 0;
      public int connectionElement = 0;

      BearerDesc() {}  
}
 */


class OtherAddress {
    public int addressType = BipUtils.ADDRESS_TYPE_UNKNOWN;
    public byte[] rawAddress = null;
    public InetAddress address = null;

    OtherAddress(int type, byte[] rawData, int index) throws UnknownHostException {
        try {
            this.addressType = type;
            if(BipUtils.ADDRESS_TYPE_IPV4 == addressType) {
                this.rawAddress = new byte[BipUtils.ADDRESS_IPV4_LENGTH];
                System.arraycopy(rawData, index, this.rawAddress, 0, this.rawAddress.length);
                this.address = InetAddress.getByAddress(this.rawAddress);
            } else if(BipUtils.ADDRESS_TYPE_IPV6 == addressType) {
                // not support IPv6
            } else {
                // no action
            }
        } catch(IndexOutOfBoundsException e) {
            CatLog.d("[BIP]", "OtherAddress: out of bounds");
            this.rawAddress = null;
            this.address = null;
        } //catch(UnknownHostExcetpion e2) {
        //    this.rawAddress = null;
        //  this.address = null;
        //}
    }
}


class TransportProtocol {
    public int protocolType = 0;
    public int portNumber = 0;

    TransportProtocol(int type, int port) {
        this.protocolType = type;
        this.portNumber = port;
    }
}


class ChannelStatus {
    final public static int CHANNEL_STATUS_NO_LINK = 0x00;
    final public static int CHANNEL_STATUS_LINK = 0x80;
    final public static int CHANNEL_STATUS_INFO_NO_FURTHER_INFO = 0x00;
    final public static int CHANNEL_STATUS_INFO_LINK_DROPED = 0x05;

    public boolean isActivated = false;
    public int mChannelId;
    public int mChannelStatus;
    public int mChannelStatusInfo;

    ChannelStatus(int cid, int status, int info) {
        mChannelId = cid;
        mChannelStatus = status;
        mChannelStatusInfo = info;
    }
}




