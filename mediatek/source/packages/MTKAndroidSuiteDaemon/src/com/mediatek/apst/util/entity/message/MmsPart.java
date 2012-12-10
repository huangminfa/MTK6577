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

package com.mediatek.apst.util.entity.message;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: MmsPart
 * <p>
 * Package: com.mediatek.apst.util.entity.message
 * <p>
 * Created on: 2010-8-4
 * <p>
 * <p>
 * Description:
 * <p>
 * Represents MMS parts. Typically, one attachment(audio, image, etc.) should
 * <p>
 * be one MMS part.
 * <p>
 * 
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class MmsPart extends DatabaseRecordEntity implements Cloneable {
    // ==============================================================
    // Constants
    // ==============================================================
    private static final long serialVersionUID = 1L;

    public static final String CT_SMIL = "application/smil";
    public static final String CT_JPG = "image/jpeg";
    public static final String CT_GIF = "image/gif";
    public static final String CT_TXT = "text/plain";
    public static final String CT_AMR = "audio/amr";
    public static final String CT_MIDI = "audio/midi";

    // ==============================================================
    // Fields
    // ==============================================================
    private long mmsId;

    private int sequence;

    private String contentType;

    private String name;

    private String charset; // Integer in sqlite

    private String cid;

    private String cl;

    private String dataPath;

    private String text;

    private byte[] byteArray;

    // ==============================================================
    // Constructors
    // ==============================================================
    public MmsPart(long id) {
        super(id);
        this.mmsId = ID_NULL;
    }

    public MmsPart() {
        this(ID_NULL);
    }

    // ==============================================================
    // Getters
    // ==============================================================
    public long getMmsId() {
        return mmsId;
    }

    public int getSequence() {
        return sequence;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getCharset() {
        return charset;
    }

    public String getCid() {
        return cid;
    }

    public String getCl() {
        return cl;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getText() {
        return text;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    // ==============================================================
    // Setters
    // ==============================================================
    public void setMmsId(long mmsId) {
        this.mmsId = mmsId;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setCl(String cl) {
        this.cl = cl;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    // ==============================================================
    // Methods
    // ==============================================================
    /**
     * Deep copy.
     * 
     * @return A deep copy.
     * @throws CloneNotSupportedException
     */
    // @Override
    public MmsPart clone() throws CloneNotSupportedException {
        MmsPart copy = (MmsPart) (super.clone());
        return copy;
    }

    // @Override
    public void writeRaw(ByteBuffer buffer) throws NullPointerException {
        super.writeRaw(buffer);
        // mmsId
        buffer.putLong(mmsId);
        // sequence
        buffer.putInt(sequence);
        // contentType
        RawTransUtil.putString(buffer, contentType);
        // name
        RawTransUtil.putString(buffer, name);
        // charset
        // buffer.putInt(charset);
        RawTransUtil.putString(buffer, charset);
        // cid
        RawTransUtil.putString(buffer, cid);
        // cl
        RawTransUtil.putString(buffer, cl);
        // dataPath
        RawTransUtil.putString(buffer, dataPath);
        // text
        RawTransUtil.putString(buffer, text);

        if (dataPath != null) {
            if (byteArray != null) {
                buffer.putInt(byteArray.length);
                buffer.put(byteArray);
            } else {
                buffer.putInt(0);
            }
        }
    }

    // @Override
    public void readRaw(ByteBuffer buffer) throws NullPointerException {
        super.readRaw(buffer);
        // mmsId
        this.mmsId = buffer.getLong();
        // sequence
        this.sequence = buffer.getInt();
        // contentType
        this.contentType = RawTransUtil.getString(buffer);
        // name
        this.name = RawTransUtil.getString(buffer);
        // charset
        // this.charset = buffer.getInt();
        this.charset = RawTransUtil.getString(buffer);
        // cid
        this.cid = RawTransUtil.getString(buffer);
        // cl
        this.cl = RawTransUtil.getString(buffer);
        // dataPath
        this.dataPath = RawTransUtil.getString(buffer);
        // text
        this.text = RawTransUtil.getString(buffer);
        if (dataPath != null) {
            int length = buffer.getInt();
            byteArray = new byte[length];
            buffer.get(byteArray);
        }
    }

    @Override
    public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // mmsId
        buffer.putLong(mmsId);
        // sequence
        buffer.putInt(sequence);
        // contentType
        RawTransUtil.putString(buffer, contentType);
        // name
        RawTransUtil.putString(buffer, name);
        // charset
        // buffer.putInt(charset);
        RawTransUtil.putString(buffer, charset);
        // cid
        RawTransUtil.putString(buffer, cid);
        // cl
        RawTransUtil.putString(buffer, cl);
        // dataPath
        RawTransUtil.putString(buffer, dataPath);
        // text
        RawTransUtil.putString(buffer, text);
        if (dataPath != null) {
            if (byteArray != null) {
                buffer.putInt(byteArray.length);
                buffer.put(byteArray);
            } else {
                buffer.putInt(0);
            }
        }
    }

    @Override
    public void readRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // mmsId
        this.mmsId = buffer.getLong();
        // sequence
        this.sequence = buffer.getInt();
        // contentType
        this.contentType = RawTransUtil.getString(buffer);
        // name
        this.name = RawTransUtil.getString(buffer);
        // charset
        // this.charset = buffer.getInt();
        this.charset = RawTransUtil.getString(buffer);
        // cid
        this.cid = RawTransUtil.getString(buffer);
        // cl
        this.cl = RawTransUtil.getString(buffer);
        // dataPath
        this.dataPath = RawTransUtil.getString(buffer);
        // text
        this.text = RawTransUtil.getString(buffer);
        if (dataPath != null) {
            int length = buffer.getInt();
            if (length != 0) {
                byteArray = new byte[length];
                buffer.get(byteArray);
            } else {
                byteArray = null;
            }
        }
    }
    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
