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

import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import java.util.Calendar;
import java.util.TimeZone;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.android.internal.telephony.cat.AppInterface.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

abstract class ResponseData {
    /**
     * Format the data appropriate for TERMINAL RESPONSE and write it into
     * the ByteArrayOutputStream object.
     */
    public abstract void format(ByteArrayOutputStream buf);

    public static void writeLength(ByteArrayOutputStream buf, int length) {
        // As per ETSI 102.220 Sec7.1.2, if the total length is greater
        // than 0x7F, it should be coded in two bytes and the first byte
        // should be 0x81.
        if (length > 0x7F) {
            buf.write(0x81);
        }
        buf.write(length);
    }
}

//MTK-START [mtk80950][120810][ALPS00XXXXXX] add UTK
class LocalInformationResponseData extends ResponseData {
    private int mLocalInfoType;
    private LocalInfo mInfo;
    private Date mDate = new Date(System.currentTimeMillis());
    private int year, month, day, hour, minute, second, zone, tempzone;
    private int mMCC, mIMSI, mSID, mNID, mBaseID,mBaseLAT,mBaseLong;
    private String languageCode = Locale.getDefault().getLanguage();

    public LocalInformationResponseData(int type, LocalInfo info) {
        super();
        this.mLocalInfoType = type;
        this.mInfo = info;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
    if (buf == null){
        return;
    }

    switch(mLocalInfoType){
        case 0://local info
        {
            CatLog.d(this, "LocalInformationResponseData local info ");
            int tag = 0x13;
            buf.write(tag);
            buf.write(0x0F);//length

            buf.write(mInfo.MCC & 0xFF);
            buf.write(mInfo.MCC >> 8);

            buf.write(mInfo.IMSI_11_12);

            buf.write(mInfo.SID & 0xFF);
            buf.write(mInfo.SID >> 8);

            buf.write(mInfo.NID & 0xFF);
            buf.write(mInfo.NID >> 8);

            buf.write(mInfo.BASE_ID & 0xFF);
            buf.write(mInfo.BASE_ID >> 8);

            buf.write(mInfo.BASE_LAT & 0xFF);
            buf.write((mInfo.BASE_LAT & 0xFF00 ) >> 8);
            buf.write(mInfo.BASE_LAT >> 16);

            buf.write(mInfo.BASE_LONG & 0xFF);
            buf.write((mInfo.BASE_LONG & 0xFF00 ) >> 8);
            buf.write(mInfo.BASE_LONG >> 16);

            CatLog.d(this,"MCC:"+mInfo.MCC + "IMSI:"+mInfo.IMSI_11_12+"SID:"+mInfo.SID+"NID:"
                +mInfo.NID+"BASEID:"+mInfo.BASE_ID+"BASELAT:"+mInfo.BASE_LAT+"BASELONG:"+mInfo.BASE_LONG);
        }
        break;
        case 3://data and time
        {
            CatLog.d(this, "LocalInformationResponseData format DateTime " + "Year:"+mDate.getYear()+ "Month:" + mDate.getMonth() + "Day:" + mDate.getDate());
            CatLog.d(this, "Hour:"+ mDate.getHours() + "Minutes:" + mDate.getMinutes() + "Seconds:"+ mDate.getSeconds());


            year = UtkConvTimeToTPTStamp((mDate.getYear() + 1900)%100);
            month = UtkConvTimeToTPTStamp(mDate.getMonth() + 1);
            day  = UtkConvTimeToTPTStamp(mDate.getDate());
            hour = UtkConvTimeToTPTStamp(mDate.getHours());
            minute = UtkConvTimeToTPTStamp(mDate.getMinutes());
            second = UtkConvTimeToTPTStamp(mDate.getSeconds());

            TimeZone defaultZone = TimeZone. getDefault();
            tempzone = defaultZone.getRawOffset()/3600/1000;
            zone = (tempzone < 0 ) ?
                UtkConvTimeToTPTStamp(-tempzone* 4) | 0x80 :
                UtkConvTimeToTPTStamp(tempzone* 4);


            CatLog.d(this, "TimeZone:"+ "rawzone:" + defaultZone.getRawOffset()+"tempzone" +tempzone +"zone" + zone);

            int tag = 0x26;
            buf.write(tag);
            buf.write(0x07);
            buf.write(year);
            buf.write(month);
            buf.write(day);
            buf.write(hour);
            buf.write(minute);
            buf.write(second);
            buf.write(zone);
        }
        break;
        case 4://language
        {
            CatLog.d(this, "LocalInformationResponseData format Language: "+ languageCode);
            int tag = 0x2d;
            buf.write(tag);
            buf.write(0x02);
            byte[] data = languageCode.getBytes();
            for(byte b : data)
            {
                buf.write(b);
            }
        }
        break;
        case 6://access technology
        {
            CatLog.d(this, "LocalInformationResponseData technology = " + mInfo.Technology);

            int tag = 0x3F;
            buf.write(tag);
            buf.write(0x01);//length
            buf.write(mInfo.Technology);
        }
        break;
      }

    }

    public int UtkConvTimeToTPTStamp(int TimeDate){
        return ((TimeDate%10)<<4) + TimeDate/10;
    }

}
//MTK-END [mtk80950][120810][ALPS00XXXXXX] add UTK

class SelectItemResponseData extends ResponseData {
    // members
    private int id;

    public SelectItemResponseData(int id) {
        super();
        this.id = id;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        // Item identifier object
        int tag = 0x80 | ComprehensionTlvTag.ITEM_ID.value();
        buf.write(tag); // tag
        buf.write(1); // length
        buf.write(id); // identifier of item chosen
    }
}

class GetInkeyInputResponseData extends ResponseData {
    // members
    private boolean mIsUcs2;
    private boolean mIsPacked;
    private boolean mIsYesNo;
    private boolean mYesNoResponse;
    public String mInData;

    // GetInKey Yes/No response characters constants.
    protected static final byte GET_INKEY_YES = 0x01;
    protected static final byte GET_INKEY_NO = 0x00;

    public GetInkeyInputResponseData(String inData, boolean ucs2, boolean packed) {
        super();
        this.mIsUcs2 = ucs2;
        this.mIsPacked = packed;
        this.mInData = inData;
        this.mIsYesNo = false;
    }

    public GetInkeyInputResponseData(boolean yesNoResponse) {
        super();
        this.mIsUcs2 = false;
        this.mIsPacked = false;
        this.mInData = "";
        this.mIsYesNo = true;
        this.mYesNoResponse = yesNoResponse;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            return;
        }

        // Text string object
        int tag = 0x80 | ComprehensionTlvTag.TEXT_STRING.value();
        buf.write(tag); // tag

        byte[] data;

        if (mIsYesNo) {
            data = new byte[1];
            data[0] = mYesNoResponse ? GET_INKEY_YES : GET_INKEY_NO;
        } else if (mInData != null && mInData.length() > 0) {
            try {
                if (mIsUcs2) {
                    // data = mInData.getBytes("UTF-16");
                    data = mInData.getBytes("UTF-16BE");
                } else if (mIsPacked) {
                    //int size = mInData.length();

                    //byte[] tempData = GsmAlphabet
                    //         .stringToGsm7BitPacked(mInData);
                    byte[] tempData = GsmAlphabet
                    .stringToGsm7BitPacked(mInData, 0, 0);
                    final int size = tempData.length - 1;
                    data = new byte[size];
                    // Since stringToGsm7BitPacked() set byte 0 in the
                    // returned byte array to the count of septets used...
                    // copy to a new array without byte 0.
                    System.arraycopy(tempData, 1, data, 0, size);
                } else {
                    data = GsmAlphabet.stringToGsm8BitPacked(mInData);
                }
            } catch (UnsupportedEncodingException e) {
                data = new byte[0];
            } catch (EncodeException e) {
                data = new byte[0];
            }
        } else {
            data = new byte[0];
        }

            // length - one more for data coding scheme.
            writeLength(buf, data.length + 1);

        // data coding scheme
        if (mIsUcs2) {
            buf.write(0x08); // UCS2
        } else if (mIsPacked) {
            buf.write(0x00); // 7 bit packed
        } else {
            buf.write(0x04); // 8 bit unpacked
        }

        for (byte b : data) {
            buf.write(b);
        }
    }
}

// For "PROVIDE LOCAL INFORMATION" command.
// See TS 31.111 section 6.4.15/ETSI TS 102 223
// TS 31.124 section 27.22.4.15 for test spec
class LanguageResponseData extends ResponseData {
    private String lang;

    public LanguageResponseData(String lang) {
        super();
        this.lang = lang;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            return;
        }

        // Text string object
        int tag = 0x80 | ComprehensionTlvTag.LANGUAGE.value();
        buf.write(tag); // tag

        byte[] data;

        if (lang != null && lang.length() > 0) {
            data = GsmAlphabet.stringToGsm8BitPacked(lang);
        }
        else {
            data = new byte[0];
        }

        buf.write(data.length);

        for (byte b : data) {
            buf.write(b);
        }
    }
}

// For "PROVIDE LOCAL INFORMATION" command.
// See TS 31.111 section 6.4.15/ETSI TS 102 223
// TS 31.124 section 27.22.4.15 for test spec
class DTTZResponseData extends ResponseData {
    private Calendar calendar;

    public DTTZResponseData(Calendar cal) {
        super();
        calendar = cal;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            return;
        }

        // DTTZ object
        int tag = 0x80 | CommandType.PROVIDE_LOCAL_INFORMATION.value();
        buf.write(tag); // tag

        byte[] data = new byte[8];

        data[0] = 0x07; // Write length of DTTZ data

        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        // Fill year byte
        data[1] = byteToBCD(calendar.get(java.util.Calendar.YEAR) % 100);

        // Fill month byte
        data[2] = byteToBCD(calendar.get(java.util.Calendar.MONTH) + 1);

        // Fill day byte
        data[3] = byteToBCD(calendar.get(java.util.Calendar.DATE));

        // Fill hour byte
        data[4] = byteToBCD(calendar.get(java.util.Calendar.HOUR_OF_DAY));

        // Fill minute byte
        data[5] = byteToBCD(calendar.get(java.util.Calendar.MINUTE));

        // Fill second byte
        data[6] = byteToBCD(calendar.get(java.util.Calendar.SECOND));

        String tz = SystemProperties.get("persist.sys.timezone", "");
        if (TextUtils.isEmpty(tz)) {
            data[7] = (byte) 0xFF;    // set FF in terminal response
        } else {
            TimeZone zone = TimeZone.getTimeZone(tz);
            int zoneOffset = zone.getRawOffset() + zone.getDSTSavings();
            data[7] = getTZOffSetByte(zoneOffset);
        }

        for (byte b : data) {
            buf.write(b);
        }
    }

    private byte byteToBCD(int value) {
        if (value < 0 && value > 99) {
            CatLog.d(this, "Err: byteToBCD conversion Value is " + value +
            " Value has to be between 0 and 99");
            return 0;
        }

        return (byte) ((value / 10) | ((value % 10) << 4));
    }

    private byte getTZOffSetByte(long offSetVal) {
        boolean isNegative = (offSetVal < 0);

        /*
         * The 'offSetVal' is in milliseconds. Convert it to hours and compute
         * offset While sending T.R to UICC, offset is expressed is 'quarters of
         * hours'
         */

        long tzOffset = offSetVal / (15 * 60 * 1000);
        tzOffset = (isNegative ? -1 : 1) * tzOffset;
        byte bcdVal = byteToBCD((int) tzOffset);
        // For negative offsets, put '1' in the msb
        return isNegative ?  (bcdVal |= 0x08) : bcdVal;
    }

}
//Add by Huibin Mao Mtk80229
//ICS Migration start
class ProvideLocalInformationResponseData extends ResponseData {
    // members
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int timezone;
    private byte[] language;
    private boolean mIsDate;
    private boolean mIsLanguage;

    public ProvideLocalInformationResponseData(int year, int month, int day, 
            int hour,int minute, int second, int timezone) {
        super();
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.timezone = timezone;       
        this.mIsDate = true;
        this.mIsLanguage = false;
    }

    public ProvideLocalInformationResponseData(byte[] language) {
        super();
        this.language = language;       
        this.mIsDate = false;
        this.mIsLanguage = true;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if (mIsDate == true) {

            int tag = 0x80 | ComprehensionTlvTag.DATE_TIME_AND_TIMEZONE.value();

            buf.write(tag); // tag
            buf.write(7); // length
            buf.write(year);
            buf.write(month);
            buf.write(day);
            buf.write(hour);
            buf.write(minute);
            buf.write(second);
            buf.write(timezone);

        } else if (mIsLanguage == true) {

            int tag = 0x80 | ComprehensionTlvTag.LANGUAGE.value();

            buf.write(tag); // tag
            buf.write(2); // length        
            for (byte b : language) {
                buf.write(b);
            }  
        }
    }
}

class OpenChannelResponseData extends ResponseData {
    ChannelStatus mChannelStatus = null;
    BearerDesc mBearerDesc = null;
    int mBufferSize = 0;

    OpenChannelResponseData(ChannelStatus channelStatus, BearerDesc bearerDesc, int bufferSize) {
        super();
        if(channelStatus != null) {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: channelStatus cid/status"
                    + channelStatus.mChannelId + "/" + channelStatus.mChannelStatus);
        } else {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: channelStatus is null");
        }
        if(bearerDesc != null) {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: bearerDesc bearerType" + bearerDesc.bearerType);
        } else {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: bearerDesc is null");
        }
        if(bufferSize > 0) {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: buffer size is " + bufferSize);
        } else {
            CatLog.d("[BIP]", "OpenChannelResponseData-constructor: bearerDesc is invalid " + bufferSize);
        }

        mChannelStatus = channelStatus;
        mBearerDesc = bearerDesc;
        mBufferSize = bufferSize;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if(buf == null) {
            CatLog.d("[BIP]", "OpenChannelResponseData-format: buf is null");
            return;
        }

        if(mBearerDesc.bearerType != BipUtils.BEARER_TYPE_GPRS) {
            CatLog.d("[BIP]", "OpenChannelResponseData-format: bearer type is not gprs");
            return;
        }

        int tag;

        if(/* mChannelStatus != null  && */mBearerDesc != null && mBufferSize > 0) {
            if(mChannelStatus != null) {
                CatLog.d("[BIP]", "OpenChannelResponseData-format: Write channel status into TR");
                tag = ComprehensionTlvTag.CHANNEL_STATUS.value();
                CatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag);
                buf.write(tag);
                CatLog.d("[BIP]", "OpenChannelResponseData-format: length: " + 0x02);
                buf.write(0x02);
                CatLog.d("[BIP]", "OpenChannelResponseData-format: channel id & isActivated: "
                        + (mChannelStatus.mChannelId | (mChannelStatus.isActivated ? 0x80 : 0x00)));
                buf.write(mChannelStatus.mChannelId | (mChannelStatus.isActivated ? 0x80 : 0x00));
                CatLog.d("[BIP]", "OpenChannelResponseData-format: channel status: " + mChannelStatus.mChannelStatus);
                buf.write(mChannelStatus.mChannelStatus);
            }

            CatLog.d("[BIP]", "Write bearer description into TR");
            tag = ComprehensionTlvTag.BEARER_DESCRIPTION.value();
            CatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag);
            buf.write(tag);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: length: " + 0x07);
            buf.write(0x07);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: bearer type: " + mBearerDesc.bearerType);
            buf.write(mBearerDesc.bearerType);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: precedence: " + mBearerDesc.precedence);
            buf.write(mBearerDesc.precedence);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: delay: " + mBearerDesc.delay);
            buf.write(mBearerDesc.delay);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: reliability: " + mBearerDesc.reliability);
            buf.write(mBearerDesc.reliability);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: peak: " + mBearerDesc.peak);
            buf.write(mBearerDesc.peak);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: mean: " + mBearerDesc.mean);
            buf.write(mBearerDesc.mean);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: pdp type: " + mBearerDesc.pdpType);
            buf.write(mBearerDesc.pdpType);

            CatLog.d("[BIP]", "Write buffer size into TR");
            tag = ComprehensionTlvTag.BUFFER_SIZE.value();
            CatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag);
            buf.write(tag);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: length: " + 0x02);
            buf.write(0x02);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: length(hi-byte): " + (mBufferSize >> 8));
            buf.write(mBufferSize >> 8);
            CatLog.d("[BIP]", "OpenChannelResponseData-format: length(low-byte): " + (mBufferSize & 0xff));
            buf.write(mBufferSize & 0xff);
        }else {
            CatLog.d("[BIP]", "Miss ChannelStatus, BearerDesc or BufferSize");
        }
    }
}

class SendDataResponseData extends ResponseData {
    int mTxBufferSize = 0;

    SendDataResponseData(int size) {
        super();
        mTxBufferSize = size;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if(buf == null) {
            return;
        }

        int tag;

        tag = 0x80 | ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value();
        buf.write(tag);
        buf.write(1);
        if(mTxBufferSize >= 0xFF){
            buf.write(0xFF);
        }else{
            buf.write(mTxBufferSize);
        }
    }
}

class ReceiveDataResponseData extends ResponseData {
    byte[] mData = null;
    int mRemainingCount = 0;

    ReceiveDataResponseData(byte[] data, int remaining) {
        super();
        mData = data;
        mRemainingCount = remaining;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if(buf == null) {
            return;
        }

        int tag;

        tag = 0x80 | ComprehensionTlvTag.CHANNEL_DATA.value();
        buf.write(tag);

        if(mData != null) {
            if(mData.length >= 0x80){
                buf.write(0x81);
            }
    
            buf.write(mData.length);
            buf.write(mData, 0, mData.length);
        } else {
            buf.write(0);
        }

        tag  =0x80 | ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value();
        buf.write(tag);
        buf.write(0x01);

        CatLog.d("[BIP]", "ReceiveDataResponseData: length: " + mRemainingCount);

        if(mRemainingCount >= 0xFF){
            buf.write(0xFF);
        }else{
            buf.write(mRemainingCount);
        }
    }
}

class GetChannelStatusResponseData extends ResponseData {
    int mChannelId = 0;
    int mChannelStatus = 0;
    int mChannelStatusInfo = 0;

    GetChannelStatusResponseData(int cid, int status, int statusInfo) {
        mChannelId = cid;
        mChannelStatus = status;
        mChannelStatusInfo = statusInfo;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if(buf == null) {
            return;
        }

        int tag = 0x80 | ComprehensionTlvTag.CHANNEL_STATUS.value();
        buf.write(tag);
        buf.write(0x02);
        buf.write((mChannelId & 0x07) | mChannelStatus);
        buf.write(mChannelStatusInfo);
    }
}
//ICS Migration end

