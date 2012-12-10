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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * This class simplifies the serialization test.
 *
 */
public class SerializationTester {

    /*
     * --------------------------------------------------------------------
     * Class variables
     * --------------------------------------------------------------------
     */

    // the last deserialized object
    private static Object lastOutput = null;

    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    private SerializationTester() {

    }

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Serialize an object and then deserialize it.
     *
     * @param inputObject
     *            the input object
     * @return the deserialized object
     */
    public static <T> T getDeserializedObject(T inputObject)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(inputObject);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object outputObject = ois.readObject();
        lastOutput = outputObject;
        ois.close();
        return (T) outputObject;
    }

    /**
     * Tests the serialization and deserialization of const objects.
     *
     * @param inputObject
     *            A const object
     * @return true if the deserialized object is the same as the input object,
     *         otherwise false
     * @throws Exception
     *             If any occurs.
     */
    public static boolean assertSame(Object inputObject) throws Exception {
        return inputObject == getDeserializedObject(inputObject);
    }

    /**
     * Tests the serialization and deserialization of instance objects.
     *
     * @param inputObject
     *            An object
     * @return true if the deserialized object is equal to the input object,
     *         otherwise false
     * @throws Exception
     *             If any occurs.
     */
    public static boolean assertEquals(Object inputObject) throws Exception {
        return inputObject.equals(getDeserializedObject(inputObject));
    }

    /**
     * Tests the serialization compatibility with reference const objects.
     *
     * @param obj
     *            the object to be checked
     * @param fileName
     *            the serialization output file generated by reference
     * @return true if compatible, otherwise false
     * @throws Exception
     *             If any occurs.
     */
    public static boolean assertCompatibilitySame(Object obj, String fileName)
            throws Exception {
        return obj == readObject(obj, fileName);
    }

    /**
     * Tests the serialization compatibility with reference for instance
     * objects.
     *
     * @param obj
     *            the object to be checked
     * @param fileName
     *            the serialization output file generated by reference
     * @return true if compatible, otherwise false
     * @throws Exception
     *             If any occurs.
     */
    public static boolean assertCompatibilityEquals(Object obj, String fileName)
            throws Exception {
        return obj.equals(readObject(obj, fileName));
    }

    /**
     * Deserialize an object from a file.
     *
     * @param obj
     *            the object to be serialized if no serialization file is found
     * @param fileName
     *            the serialization file
     * @return the deserialized object
     * @throws Exception
     *             If any occurs.
     */
    public static Object readObject(Object obj, String fileName)
            throws Exception {
        InputStream input = null;
        ObjectInputStream oinput = null;
        URL url = SerializationTester.class.getResource(
                fileName);
        if (null == url) {
            // serialization file does not exist, create one in the current dir
            writeObject(obj, new File(fileName).getName());
            throw new Error(
                    "Serialization file does not exist, created in the current dir.");
        }
        input = url.openStream();
        try {
            oinput = new ObjectInputStream(input);
            Object newObj = oinput.readObject();
            return newObj;
        } finally {
            try {
                if (null != oinput) {
                    oinput.close();
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /*
     * Creates a serialization output.
     *
     * @param obj the object to be serialized @param fileName the output file
     * @throws Exception If any occurs.
     */
    public static void writeObject(Object obj, String fileName)
            throws Exception {
        // String path = SerializationTester.class.getResource(".").getPath();
        // if (path.endsWith(".")) {
        // path = path.substring(0, path.length() - 1);
        // }
        // if (!path.endsWith("/")) {
        // path += "/";
        // }
        // path += fileName;
        // System.out.println(path);
        OutputStream output = null;
        ObjectOutputStream ooutput = null;
        try {
            output = new FileOutputStream(fileName);
            ooutput = new ObjectOutputStream(output);
            ooutput.writeObject(obj);
        } finally {
            try {
                if (null != ooutput) {
                    ooutput.close();
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                if (null != output) {
                    output.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Gets the last deserialized object.
     *
     * @return the last deserialized object
     */
    public static Object getLastOutput() {
        return lastOutput;
    }

    /*
     * For test purpose.
     */
    public static void main(String[] args) {
    }
}
