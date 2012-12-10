/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Texture Cache Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import android.graphics.Bitmap;
import android.util.Log;

import com.mediatek.a3m.Texture2D;
import com.mediatek.ngin3d.presentation.BitmapGenerator;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ImageSource;
import com.mediatek.ngin3d.utils.Ngin3dException;

/**
 * A texture cache that maps texture source (file, resource, or anything else)
 * to their texture object in memory. This class can prevent loading multiple
 * texture for the same 'source'.
 * @hide
 */
public class TextureCache {
    private static final String TAG = "TextureCache";
    private static final int MAX_GENERATOR_LIST_SIZE = 24;
    private static final String PVR = "pvr";

    private final LinkedList<BitmapGenerator> mGeneratorCache =
            new LinkedList<BitmapGenerator>();

    private final A3mPresentationEngine mEngine;

    public TextureCache(A3mPresentationEngine engine) {
        mEngine = engine;
    }

    /**
     * Release the texture from this cache object
     * @param key  texture key
     */
    public void release(Object key) {
        // \todo implement
        Log.e(TAG, "release() not implemented.");
    }

    /**
     *  Gets the texture from android bitmap.
     * @param bitmap   android bitmap
     * @return   specific texture object
     */
    protected Texture2D getTexture(Bitmap bitmap) {
        if (!Bitmap.Config.ARGB_8888.equals(bitmap.getConfig())) {
            Log.e(TAG, "Bitmap formats other than ARGB_8888 not supported");
            return null;
        }

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        ByteBuffer byteBuffer = ByteBuffer.allocate(width * height * 4);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bitmapArray = byteBuffer.array();

        Texture2D texture = mEngine.getAssetPool().createTexture2D(
                width, height,
                Texture2D.FORMAT_RGBA, Texture2D.TYPE_UNSIGNED_BYTE,
                bitmapArray);
        return texture;
    }

    /**
     * Gets the texture from file name.
     * @param filename   file name of the texture data
     * @return   specific texture object
     */
    protected Texture2D getTexture(String filename) {
        return (mEngine.getAssetPool().getTexture2D(filename));
    }

    /**
     * Gets the texture by resource ID
     * @param id Resource ID of the texture
     * @return   specific texture object
     */
    protected Texture2D getTexture(int id) {
        String name = mEngine.getResources().getResourceName(id);
        return (mEngine.getAssetPool().getTexture2D(name));
    }

    /**
     * Gets texture by giving types of source.
     * @param src  image types. could be file, bitmap, bitmap generator,
     * and android resource
     * @return  texture object
     */
    public Texture2D getTexture(ImageSource src) {
        Texture2D result;
        switch (src.srcType) {
        case ImageSource.FILE: {
            result = getTexture((String) src.srcInfo);
            break;
        }

        case ImageSource.BITMAP: {
            Bitmap bitmap = (Bitmap) src.srcInfo;
            result = getTexture(bitmap);

            if ((src.options & ImageSource.RECYCLE_AFTER_USE) != 0) {
                bitmap.recycle();
            }
            break;
        }

        case ImageSource.RES_ID: {
            result = getTexture(((ImageDisplay.Resource) src.srcInfo).resId);
            break;
        }

        case ImageSource.BITMAP_GENERATOR: {
            BitmapGenerator generator = (BitmapGenerator) src.srcInfo;
            // If generator has cached bitmap, add it to generator cache.
            if (generator.getCachedBitmap() != null) {
                addToGeneratorCache(generator);
            }
            Bitmap bitmap = generator.getBitmap();
            result = getTexture(bitmap);

            if ((src.options & ImageSource.RECYCLE_AFTER_USE) != 0) {
                bitmap.recycle();
            }
            break;
        }

        case ImageSource.VIDEO_TEXTURE: {
            // We require a 'virtual' texture to link Android video and renderer
            Texture2D texture = mEngine.getAssetPool().createTexture2D(
                0, 0, 0, 0, null);
            result = texture;
            break;
        }

        default:
            throw new Ngin3dException("Unsupported image source");
        }
        return  result;
    }

    protected void addToGeneratorCache(BitmapGenerator generator) {
        if (mGeneratorCache.contains(generator)) {
            // Move the generator to the end of list
            mGeneratorCache.remove(generator);
            mGeneratorCache.addLast(generator);
        } else {
            mGeneratorCache.add(generator);
            if (mGeneratorCache.size() > MAX_GENERATOR_LIST_SIZE) {
                mGeneratorCache.removeFirst().free();
            }
        }
    }
}
