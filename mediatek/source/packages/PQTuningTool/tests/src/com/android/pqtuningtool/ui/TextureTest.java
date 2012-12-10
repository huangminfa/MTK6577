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

package  com.android.pqtuningtool.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.test.suitebuilder.annotation.SmallTest;

import javax.microedition.khronos.opengles.GL11;

import junit.framework.TestCase;

@SmallTest
public class TextureTest extends TestCase {
    @SuppressWarnings("unused")
    private static final String TAG = "TextureTest";

    class MyBasicTexture extends BasicTexture {
        int mOnBindCalled;
        int mOpaqueCalled;

        MyBasicTexture(GLCanvas canvas, int id) {
            super(canvas, id, BasicTexture.STATE_UNLOADED);
        }

        @Override
        protected boolean onBind(GLCanvas canvas) {
            mOnBindCalled++;
            return true;
        }

        public boolean isOpaque() {
            mOpaqueCalled++;
            return true;
        }

        void upload() {
            mState = STATE_LOADED;
        }
    }

    @SmallTest
    public void testBasicTexture() {
        GL11 glStub = new GLStub();
        GLCanvas canvas = new GLCanvasImpl(glStub);
        MyBasicTexture texture = new MyBasicTexture(canvas, 47);

        assertEquals(47, texture.getId());
        texture.setSize(1, 1);
        assertEquals(1, texture.getWidth());
        assertEquals(1, texture.getHeight());
        assertEquals(1, texture.getTextureWidth());
        assertEquals(1, texture.getTextureHeight());
        texture.setSize(3, 5);
        assertEquals(3, texture.getWidth());
        assertEquals(5, texture.getHeight());
        assertEquals(4, texture.getTextureWidth());
        assertEquals(8, texture.getTextureHeight());

        assertFalse(texture.isLoaded(canvas));
        texture.upload();
        assertTrue(texture.isLoaded(canvas));

        // For a different GL, it's not loaded.
        GLCanvas canvas2 = new GLCanvasImpl(new GLStub());
        assertFalse(texture.isLoaded(canvas2));

        assertEquals(0, texture.mOnBindCalled);
        assertEquals(0, texture.mOpaqueCalled);
        texture.draw(canvas, 100, 200, 1, 1);
        assertEquals(1, texture.mOnBindCalled);
        assertEquals(1, texture.mOpaqueCalled);
        texture.draw(canvas, 0, 0);
        assertEquals(2, texture.mOnBindCalled);
        assertEquals(2, texture.mOpaqueCalled);
    }

    @SmallTest
    public void testRawTexture() {
        GL11 glStub = new GLStub();
        GLCanvas canvas = new GLCanvasImpl(glStub);
        RawTexture texture = RawTexture.newInstance(canvas);
        texture.onBind(canvas);

        GLCanvas canvas2 = new GLCanvasImpl(new GLStub());
        try {
            texture.onBind(canvas2);
            fail();
        } catch (RuntimeException ex) {
            // expected.
        }

        assertTrue(texture.isOpaque());
    }

    @SmallTest
    public void testColorTexture() {
        GLCanvasMock canvas = new GLCanvasMock();
        ColorTexture texture = new ColorTexture(0x12345678);

        texture.setSize(42, 47);
        assertEquals(texture.getWidth(), 42);
        assertEquals(texture.getHeight(), 47);
        assertEquals(0, canvas.mFillRectCalled);
        texture.draw(canvas, 0, 0);
        assertEquals(1, canvas.mFillRectCalled);
        assertEquals(0x12345678, canvas.mFillRectColor);
        assertEquals(42f, canvas.mFillRectWidth);
        assertEquals(47f, canvas.mFillRectHeight);
        assertFalse(texture.isOpaque());
        assertTrue(new ColorTexture(0xFF000000).isOpaque());
    }

    private class MyUploadedTexture extends UploadedTexture {
        int mGetCalled;
        int mFreeCalled;
        Bitmap mBitmap;
        @Override
        protected Bitmap onGetBitmap() {
            mGetCalled++;
            Config config = Config.ARGB_8888;
            mBitmap = Bitmap.createBitmap(47, 42, config);
            return mBitmap;
        }
        @Override
        protected void onFreeBitmap(Bitmap bitmap) {
            mFreeCalled++;
            assertSame(mBitmap, bitmap);
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @SmallTest
    public void testUploadedTexture() {
        GL11 glStub = new GLStub();
        GLCanvas canvas = new GLCanvasImpl(glStub);
        MyUploadedTexture texture = new MyUploadedTexture();

        // draw it and the bitmap should be fetched.
        assertEquals(0, texture.mFreeCalled);
        assertEquals(0, texture.mGetCalled);
        texture.draw(canvas, 0, 0);
        assertEquals(1, texture.mGetCalled);
        assertTrue(texture.isLoaded(canvas));
        assertTrue(texture.isContentValid(canvas));

        // invalidate content and it should be freed.
        texture.invalidateContent();
        assertFalse(texture.isContentValid(canvas));
        assertEquals(1, texture.mFreeCalled);
        assertTrue(texture.isLoaded(canvas));  // But it's still loaded

        // draw it again and the bitmap should be fetched again.
        texture.draw(canvas, 0, 0);
        assertEquals(2, texture.mGetCalled);
        assertTrue(texture.isLoaded(canvas));
        assertTrue(texture.isContentValid(canvas));

        // recycle the texture and it should be freed again.
        texture.recycle();
        assertEquals(2, texture.mFreeCalled);
        // TODO: these two are broken and waiting for fix.
        //assertFalse(texture.isLoaded(canvas));
        //assertFalse(texture.isContentValid(canvas));
    }

    class MyTextureForMixed extends BasicTexture {
        MyTextureForMixed(GLCanvas canvas, int id) {
            super(canvas, id, BasicTexture.STATE_UNLOADED);
        }

        @Override
        protected boolean onBind(GLCanvas canvas) {
            return true;
        }

        public boolean isOpaque() {
            return true;
        }
    }

    @SmallTest
    public void testBitmapTexture() {
        Config config = Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(47, 42, config);
        assertFalse(bitmap.isRecycled());
        BitmapTexture texture = new BitmapTexture(bitmap);
        texture.recycle();
        assertFalse(bitmap.isRecycled());
        bitmap.recycle();
        assertTrue(bitmap.isRecycled());
    }
}
