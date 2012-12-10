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

package com.mediatek.android.content;

import android.content.ContentValues;

public class MeasuredContentValues {
    // ==============================================================
    // Constants
    // ==============================================================

    // ==============================================================
    // Fields
    // ==============================================================
    /** Actual content values */
    private ContentValues values;
    /** Parcel size of the content values */
    private int parcelSize;

    // ==============================================================
    // Constructors
    // ==============================================================
    public MeasuredContentValues() {
        // 8 is preferred initiate size of ContentValues by default
        this(8);
    }

    public MeasuredContentValues(int initSize) {
        this.values = new ContentValues(initSize);
        // An integer will be written to tell the map size in content values
        // in parcel
        this.parcelSize = 4;
    }

    // ==============================================================
    // Getters
    // ==============================================================

    // ==============================================================
    // Setters
    // ==============================================================

    // ==============================================================
    // Methods
    // ==============================================================
    public ContentValues getValues() {
        return this.values;
    }

    public int measure() {
        return this.parcelSize;
    }

    public int measureValue(String value) {
        /*
         * A String value will be written in parcel as: 1 Integer(4 bytes):
         * Value type 1 Integer(4 bytes): String length Every 2 char takes 4
         * bytes (both 1 and 2 chars takes 4 bytes) Always a bonus 4 bytes
         * (maybe for '/0'?)
         * 
         * So cost 4 + 4 + 4 * (value.length() / 2 + 1) bytes
         */
        if (null == value) {
            return 4;
        } else {
            return 4 * (value.length() / 2 + 3);
        }
    }

    public int measureValue(byte[] value) {
        /*
         * A byte[] will be written as: 1 Integer(4 bytes): Value type 1
         * Integer(4 bytes): byte array length Every 1 byte in array takes 1
         * byte
         * 
         * So cost 4 + 4 + value.length bytes
         */
        if (null == value) {
            return 4;
        } else {
            return 8 + value.length;
        }
    }

    public int measureValue(Integer value) {
        /*
         * A Integer will be written as: 1 Integer(4 bytes): Value type 1
         * Integer(4 bytes): Value
         * 
         * So cost 8 bytes
         */
        if (null == value) {
            return 4;
        } else {
            return 8;
        }
    }

    public int measureValue(Long value) {
        /*
         * A Long will be written as: 1 Integer(4 bytes): Value type 1 Long(8
         * bytes): Value
         * 
         * So cost 12 bytes
         */
        if (null == value) {
            return 4;
        } else {
            return 12;
        }
    }

    public void clear() {
        values.clear();
        // An integer will be written to tell the map size in content values
        // in parcel
        parcelSize = 4;
    }

    public void put(String key, Integer value) {
        if (values.containsKey(key)) {
            // Do nothing
        } else {
            parcelSize += measureValue(key) + measureValue(value);
        }
        // Put actual content values
        values.put(key, value);
    }

    public void put(String key, Long value) {
        if (values.containsKey(key)) {
            // Do nothing
        } else {
            parcelSize += measureValue(key) + measureValue(value);
        }
        // Put actual content values
        values.put(key, value);
    }

    public void put(String key, String value) {
        if (values.containsKey(key)) {
            String old = values.getAsString(key);
            parcelSize += measureValue(value) - measureValue(old);
        } else {
            parcelSize += measureValue(key) + measureValue(value);
        }
        // Put actual content values
        values.put(key, value);
    }

    public void put(String key, byte[] value) {
        if (values.containsKey(key)) {
            byte[] old = values.getAsByteArray(key);
            parcelSize += measureValue(value) - measureValue(old);
        } else {
            parcelSize += measureValue(key) + measureValue(value);
        }
        // Put actual content values
        values.put(key, value);
    }

    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
