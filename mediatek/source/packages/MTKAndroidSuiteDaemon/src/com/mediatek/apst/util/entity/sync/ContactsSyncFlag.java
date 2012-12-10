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

package com.mediatek.apst.util.entity.sync;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.RawTransUtil;

/**
 * Class Name: ContactsSyncFlag
 * <p>
 * Package: com.mediatek.apst.util.entity.sync
 * <p>
 * Created on: 2010-12-18
 * <p>
 * <p>
 * Description:
 * <p>
 * A brief flag which contains necessary information to tell whether and how to
 * sync the contact for outlook contacts sync module.
 * <p>
 *
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class ContactsSyncFlag extends DatabaseRecordEntity {
    // ==============================================================
    // Constants
    // ==============================================================

    private static final long serialVersionUID = 2L;
    // ==============================================================
    // Fields
    // ==============================================================
    private int version;
    private String displayName;
    /**
     * The modify time of the contact. Added by Shaoying Han
     */
    private long modifyTime;

    // ==============================================================
    // Constructors
    // ==============================================================
    // ==============================================================
    // Getters
    // ==============================================================
    public int getVersion() {
        return version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    // ==============================================================
    // Setters
    // ==============================================================
    public void setVersion(int version) {
        this.version = version;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    // ==============================================================
    // Methods
    // ==============================================================
    // @Override
    /**
     * @deprecated
     */
    public void writeRaw(ByteBuffer buffer) throws NullPointerException {
        super.writeRaw(buffer);
        buffer.putInt(this.version);
        RawTransUtil.putString(buffer, this.displayName);
    }

    // @Override
    /**
     * @deprecated
     */
    public void readRaw(ByteBuffer buffer) throws NullPointerException {
        super.readRaw(buffer);
        this.version = buffer.getInt();
        this.displayName = RawTransUtil.getString(buffer);
    }

    @Override
    public void readRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferUnderflowException {
        // TODO Auto-generated method stub
        super.readRawWithVersion(buffer, versionCode);
        this.version = buffer.getInt();
        this.displayName = RawTransUtil.getString(buffer);
        if (versionCode >= 0x00000002) {
            this.modifyTime = buffer.getLong();
        }
    }

    @Override
    public void writeRawWithVersion(ByteBuffer buffer, int versionCode)
            throws NullPointerException, BufferOverflowException {
        // TODO Auto-generated method stub
        super.writeRawWithVersion(buffer, versionCode);
        buffer.putInt(this.version);
        RawTransUtil.putString(buffer, this.displayName);
        if (versionCode >= 0x00000002) {
            buffer.putLong(this.modifyTime);
        }
    }
    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
