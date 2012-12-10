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

package com.mediatek.apst.util.entity;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Class Name: RawTransUtil
 * <p>Package: com.mediatek.apst.util.entity
 * <p>Created on: 2010-12-17
 * <p>
 * <p>Description: 
 * <p>Utility class help to transfer non-primitive data into raw bytes. 
 * <p>
 * @author mtk80734 Siyang.Miao
 * @version V1.0
 */
public class RawTransUtil {
    //==============================================================
    // Constants                                                    
    //==============================================================
    /** An <b>int</b> primitive cost 4 bytes to store. */
    public static final int INT = 4;
    /** An <b>long</b> primitive cost 8 bytes to store. */
    public static final int LONG = 8;
    /** An <b>short</b> primitive cost 2 bytes to store. */
    public static final int SHORT = 2;
    /** An <b>byte</b> primitive cost 1 bytes to store. */
    public static final int BYTE = 1;
    /** An <b>float</b> primitive cost 4 bytes to store. */
    public static final int FLOAT = 4;
    /** An <b>double</b> primitive cost 8 bytes to store. */
    public static final int DOUBLE = 8;
    /** An <b>char</b> primitive cost 2 bytes to store. */
    public static final int CHAR = 2;
    /** An <b>boolean</b> primitive cost 1 bytes to store. */
    public static final int BOOLEAN = 1;
    
    /**
     * The value representing the length of a null array.
     */
    public static final int LENGTH_NULL = -1;
    /**
     * Default size for allocating byte buffer.
     */
    public static final int DEFAULT_BUFFER_SIZE = 800000;
    
    /**
     * UTF-8 encoding.
     */
    public static final String UTF8 = "UTF-8";
    //==============================================================
    // Fields                                                       
    //==============================================================
    
    //==============================================================
    // Constructors                                                 
    //==============================================================
    
    //==============================================================
    // Getters                                                      
    //==============================================================
    
    //==============================================================
    // Setters                                                      
    //==============================================================
    
    //==============================================================
    // Methods                                                      
    //==============================================================
    /**
     * Allocate a byte buffer with default size.
     * @return The byte buffer.
     * @see #DEFAULT_BUFFER_SIZE
     */
    public static ByteBuffer allocateDefaultBuffer(){
        return ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Get raw size of a String.
     * @param str Target String.
     * @return The raw size.
     */
    public static final int sizeOfString(String str){
        // Always set 4 bytes storing the length of the String
        int size = INT;
        // Other bytes store the content of String
        if (null != str){
            size += CHAR * str.length();
        }
        return size;
    }

    /**
     * Get raw size of a byte array.
     * @param arr Target byte array.
     * @return The raw size.
     */
    public static final int sizeOfBytes(byte[] arr){
        // Always set 4 bytes which store the length of the array
        int size = INT;
        // Other bytes store the values
        if (null != arr){
            size += BYTE * arr.length;  
        }
        return size;
    }
    
    /**
     * Read a boolean primitive from the specified byte buffer.
     * @param buffer The byte buffer.
     * @return The boolean primitive.
     * @throws NullPointerException
     * @throws BufferUnderflowException
     */
    public static final boolean getBoolean(ByteBuffer buffer) throws 
    NullPointerException, BufferUnderflowException {
        // Actually, we use 1 byte to store the boolean primitive, 
        // 1 represents true, 0 represents false
        return buffer.get() == (byte)0 ? false : true;
    }
    
    /**
     * Write a boolean primitive into the specified byte buffer. 
     * @param buffer The byte buffer.
     * @param b The boolean primitive.
     * @throws NullPointerException
     * @throws BufferOverflowException
     */
    public static final void putBoolean(ByteBuffer buffer, boolean b) throws 
    NullPointerException, BufferOverflowException {
        // Actually, we use 1 byte to store the boolean primitive, 
        // 1 represents true, 0 represents false
        buffer.put((byte) (b ? 1 : 0));
    }
    
    /**
     * Read a byte array from the specified byte buffer.
     * @param buffer The byte buffer.
     * @return The byte array.
     * @throws NullPointerException
     * @throws BufferUnderflowException
     */
    public static final byte[] getBytes(ByteBuffer buffer) throws 
    NullPointerException, BufferUnderflowException {
        // length of the array
        int len = buffer.getInt();
        if (len >= 0){
            byte[] arr = new byte[len];
            buffer.get(arr);
            return arr;
        } else {
            return null;
        }
    }
    
    /**
     * Write a byte array into the specified byte buffer. 
     * @param buffer The byte buffer.
     * @param bytes The byte array.
     * @throws NullPointerException
     * @throws BufferOverflowException
     */
    public static final void putBytes(ByteBuffer buffer, byte[] bytes) throws 
    NullPointerException, BufferOverflowException {
        if (null != bytes){
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        } else {
            buffer.putInt(LENGTH_NULL);
        }
    }
    
    /**
     * Read a String object from the specified byte buffer.
     * @param buffer The byte buffer.
     * @return The String object.
     * @throws NullPointerException
     * @throws BufferUnderflowException
     */
    public static final String getString(ByteBuffer buffer) throws 
    NullPointerException, BufferUnderflowException {
        String out = null;
        // length of the String
        int len = buffer.getInt();
        if (len >= 0){
            char[] arr = new char[len];
            for (int i = 0; i < len; i++){
                arr[i] = buffer.getChar();
            }
            out = new String(arr);
        }
        return out;
    }
    
    /**
     * Write a String into the specified byte buffer. 
     * @param buffer The byte buffer.
     * @param str The String object.
     * @throws NullPointerException
     * @throws BufferOverflowException
     */
    public static final void putString(ByteBuffer buffer, String str) throws 
    NullPointerException, BufferOverflowException {
        if (null != str){
            char[] arr = str.toCharArray();
            buffer.putInt(arr.length);
            for (int i = 0; i < arr.length; i++){
                buffer.putChar(arr[i]);
            }
        } else {
            buffer.putInt(LENGTH_NULL);
        }
    }
    
    /**
     * Read a String array object from the specified byte buffer.
     * @param buffer The byte buffer.
     * @return The String array.
     * @throws NullPointerException
     * @throws BufferUnderflowException
     */
    public static final String[] getStringArray(ByteBuffer buffer) throws 
    NullPointerException, BufferUnderflowException {
        String[] out = null;
        // length of the array
        int len = buffer.getInt();
        if (len >= 0){
            out = new String[len];
            for (int i = 0; i < len; i++){
                out[i] = getString(buffer);
            }
        }
        return out;
    }
    
    /**
     * Write a String array into the specified byte buffer. 
     * @param buffer The byte buffer.
     * @param arr The String array.
     * @throws NullPointerException
     * @throws BufferOverflowException
     */
    public static final void putStringArray(ByteBuffer buffer, String[] arr) 
    throws NullPointerException, BufferOverflowException {
        if (null != arr){
            buffer.putInt(arr.length);
            for (int i = 0; i < arr.length; i++){
                putString(buffer, arr[i]);
            }
        } else {
            buffer.putInt(LENGTH_NULL);
        }
    }
    
    //==============================================================
    // Inner & Nested classes                                               
    //==============================================================
}
