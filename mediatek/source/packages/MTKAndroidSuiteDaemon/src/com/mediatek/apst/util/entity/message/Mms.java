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
import java.util.ArrayList;
import java.util.List;

import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: Mms
 * <p>
 * Package: com.mediatek.apst.util.entity.message
 * <p>
 * Created on: 2010-8-4
 * <p>
 * <p>
 * Description:
 * <p>
 * MMS entity.
 * <p>
 * 
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class Mms extends Message {
    // ==============================================================
    // Constants
    // ==============================================================

    private static final long serialVersionUID = 1L;
    // ==============================================================
    // Fields
    // ==============================================================
    private String contentType;

    private List<MmsPart> parts;

    private String m_id;

    private String sub_cs; // Integer in database

    private String m_size; // Integer in database

    private String tr_id;

    private String d_rpt; // Integer in database

    private String m_cls;

    private String m_type; // Integer in database

    private String v; // Integer in database

    private String seen; // Integer in database

    // ==============================================================
    // Constructors
    // ==============================================================
    public Mms(long id) {
        super(id);
        this.parts = new ArrayList<MmsPart>();
    }

    public Mms() {
        this(ID_NULL);
    }

    // ==============================================================
    // Getters
    // ==============================================================
    public String getContentType() {
        return contentType;
    }

    public List<MmsPart> getParts() {
        return parts;
    }

    public String getM_id() {
        return m_id;
    }

    public String getSub_cs() {
        return sub_cs;
    }

    public String getM_size() {
        return m_size;
    }

    public String getTr_id() {
        return tr_id;
    }

    public String getD_rpt() {
        return d_rpt;
    }

    public String getM_cls() {
        return m_cls;
    }

    public String getM_type() {
        return m_type;
    }

    public String getV() {
        return v;
    }

    public String getSeen() {
        return seen;
    }

    // ==============================================================
    // Setters
    // ==============================================================
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setParts(List<MmsPart> parts) {
        this.parts = parts;
    }

    public void setM_id(String mId) {
        this.m_id = mId;
    }

    public void setD_rpt(String dRpt) {
        this.d_rpt = dRpt;
    }

    public void setTr_id(String trId) {
        this.tr_id = trId;
    }

    public void setM_size(String mSize) {
        this.m_size = mSize;
    }

    public void setSub_cs(String subCs) {
        this.sub_cs = subCs;
    }

    public void setM_cls(String mCls) {
        this.m_cls = mCls;
    }

    public void setM_type(String mType) {
        this.m_type = mType;
    }

    public void setV(String v) {
        this.v = v;
    }

    public void setSeen(String seen) {
        this.seen = seen;
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
    public Mms clone() throws CloneNotSupportedException {
        Mms copy = (Mms) (super.clone());

        copy.parts = new ArrayList<MmsPart>();
        for (MmsPart part : this.parts) {
            copy.parts.add(part);
        }

        return copy;
    }

    @Override
    public void readRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // contentType
        this.contentType = RawTransUtil.getString(buffer);
    }

    @Override
    public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // contentType
        RawTransUtil.putString(buffer, contentType);
    }

    public void writeAllWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferOverflowException {
        super.writeRawWithVersion(buffer, versionCode);
        // contentType

        RawTransUtil.putString(buffer, contentType);
        // m_id
        RawTransUtil.putString(buffer, m_id);

        // sub_cs
        // buffer.putInt(sub_cs);
        RawTransUtil.putString(buffer, sub_cs);

        // m_cls
        RawTransUtil.putString(buffer, m_cls);

        // m_type
        // buffer.putInt(m_type);
        RawTransUtil.putString(buffer, m_type);

        // v
        // buffer.putInt(v);
        RawTransUtil.putString(buffer, v);

        // m_size
        // buffer.putInt(m_size);
        RawTransUtil.putString(buffer, m_size);

        // tr_id
        RawTransUtil.putString(buffer, tr_id);

        // d_rpt
        RawTransUtil.putString(buffer, d_rpt);

        // seen
        RawTransUtil.putString(buffer, seen);

        // parts
        if (null != parts) {
            buffer.putInt(parts.size());
            for (MmsPart part : parts) {
                part.writeRawWithVersion(buffer, versionCode);
            }
        } else {
            buffer.putInt(RawTransUtil.LENGTH_NULL);
        }
    }

    public void readAllWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferUnderflowException {
        super.readRawWithVersion(buffer, versionCode);
        // contentType
        this.contentType = RawTransUtil.getString(buffer);

        // m_id
        this.m_id = RawTransUtil.getString(buffer);

        // sub_cs
        // this.sub_cs = buffer.getInt();
        this.sub_cs = RawTransUtil.getString(buffer);

        // m_cls
        this.m_cls = RawTransUtil.getString(buffer);

        // m_type
        // this.m_type = buffer.getInt();
        this.m_type = RawTransUtil.getString(buffer);

        // v
        // this.v = buffer.getInt();
        this.v = RawTransUtil.getString(buffer);

        // m_size
        // this.m_size = buffer.getInt();
        this.m_size = RawTransUtil.getString(buffer);

        // tr_id
        this.tr_id = RawTransUtil.getString(buffer);

        // d_rpt
        this.d_rpt = RawTransUtil.getString(buffer);

        // seen
        this.seen = RawTransUtil.getString(buffer);

        // parts
        int size = buffer.getInt();
        if (RawTransUtil.LENGTH_NULL != size) {
            parts = new ArrayList<MmsPart>(size);
            for (int i = 0; i < size; i++) {
                MmsPart part = new MmsPart();
                part.readRawWithVersion(buffer, versionCode);
                parts.add(part);
            }
        } else {
            this.parts = null;
        }
    }
    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
