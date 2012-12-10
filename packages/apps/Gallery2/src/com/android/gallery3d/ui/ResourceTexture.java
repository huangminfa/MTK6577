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

package com.android.gallery3d.ui;

import com.android.gallery3d.common.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

// ResourceTexture is a texture whose Bitmap is decoded from a resource.
// By default ResourceTexture is not opaque.
public class ResourceTexture extends UploadedTexture {

    private static final String TAG = "Gallery2/ResourceTexture";
    protected final Context mContext;
    protected final int mResId;

    public ResourceTexture(Context context, int resId) {
        mContext = Utils.checkNotNull(context);
        mResId = resId;
        setOpaque(false);
    }

    @Override
    protected Bitmap onGetBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), mResId, options);
        } catch (OutOfMemoryError oe) {
            // if out of memory, try to decode 1/4 size bitmap
            options.inSampleSize = 2;
            try {
                bmp = BitmapFactory.decodeResource(mContext.getResources(), mResId, options);
            } catch (OutOfMemoryError oe2) {
                // if still out of memory, try to decode 1/16 size bitmap
                options.inSampleSize = 4;
                try {
                    bmp = BitmapFactory.decodeResource(mContext.getResources(), mResId, options);
                } catch (OutOfMemoryError oe4) {
                    Log.e(TAG, "Resource " + mResId + " decode failed due to unresolvable memory insufficiency!", oe4);
                }
            }
        }
        return bmp;
    }

    @Override
    protected void onFreeBitmap(Bitmap bitmap) {
        if (!inFinalizer()) {
            bitmap.recycle();
        }
    }
}
