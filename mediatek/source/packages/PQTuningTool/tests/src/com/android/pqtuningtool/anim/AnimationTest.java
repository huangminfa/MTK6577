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
 * Copyright (C) 2010 The Android Open Source Project
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

package  com.android.pqtuningtool.anim;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.animation.Interpolator;

import junit.framework.TestCase;

@SmallTest
public class AnimationTest extends TestCase {
    private static final String TAG = "AnimationTest";

    public void testFloatAnimation() {
        FloatAnimation a = new FloatAnimation(0f, 1f, 10);  // value 0 to 1.0, duration 10
        a.start();                 // start animation
        assertTrue(a.isActive());  // should be active now
        a.calculate(0);            // set start time = 0
        assertTrue(a.get() == 0);  // start value should be 0
        a.calculate(1);            // calculate value for time 1
        assertFloatEq(a.get(), 0.1f);
        a.calculate(5);            // calculate value for time 5
        assertTrue(a.get() == 0.5);//
        a.calculate(9);            // calculate value for time 9
        assertFloatEq(a.get(), 0.9f);
        a.calculate(10);           // calculate value for time 10
        assertTrue(!a.isActive()); // should be inactive now
        assertTrue(a.get() == 1.0);//
        a.start();                 // restart
        assertTrue(a.isActive());  // should be active now
        a.calculate(5);            // set start time = 5
        assertTrue(a.get() == 0);  // start value should be 0
        a.calculate(5+9);          // calculate for time 5+9
        assertFloatEq(a.get(), 0.9f);
    }

    private static class MyInterpolator implements Interpolator {
        public float getInterpolation(float input) {
            return 4f * (input - 0.5f);  // maps [0,1] to [-2,2]
        }
    }

    public void testInterpolator() {
        FloatAnimation a = new FloatAnimation(0f, 1f, 10);  // value 0 to 1.0, duration 10
        a.setInterpolator(new MyInterpolator());
        a.start();                 // start animation
        a.calculate(0);            // set start time = 0
        assertTrue(a.get() == -2); // start value should be -2
        a.calculate(1);            // calculate value for time 1
        assertFloatEq(a.get(), -1.6f);
        a.calculate(5);            // calculate value for time 5
        assertTrue(a.get() == 0);  //
        a.calculate(9);            // calculate value for time 9
        assertFloatEq(a.get(), 1.6f);
        a.calculate(10);           // calculate value for time 10
        assertTrue(a.get() == 2);  //
    }

    public static void assertFloatEq(float expected, float actual) {
        if (Math.abs(actual - expected) > 1e-6) {
            Log.v(TAG, "expected: " + expected + ", actual: " + actual);
            fail();
        }
    }
}
