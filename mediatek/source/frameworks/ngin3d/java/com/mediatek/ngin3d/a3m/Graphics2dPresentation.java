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
 * Graphics2D Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.presentation.Graphics2d;
import com.mediatek.ngin3d.presentation.ImageSource;

/**
 * The presentation that provides 2D graphics drawing.
 * @hide
 */
// \todo Investigate why this exists in the presentation layer when it has no
// A3M-specific code
public class Graphics2dPresentation extends
    RectSceneNodePresentation implements Graphics2d {

    private static final String TAG = "Graphics2dPresentation";

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Box mBox;
    private Dimension mDimension;

    /**
     * Initializes this object with A3M presentation engine
     *
     * @param engine A3mPresentationEngine
     */
    public Graphics2dPresentation(A3mPresentationEngine engine) {
        super(engine);
        mBox = new Box();
        mDimension = new Dimension();
    }

    /**
     * Un-initialize this object
     */
    @Override
    public void onUninitialize() {
        destroyCanvas();

        mBitmap = null;
        mCanvas = null;
        mBox = null;
        mDimension = null;

        super.onUninitialize();
    }

    /**
     * Gets the result of this canvas
     */
    public Canvas getCanvas() {
        return mCanvas;
    }

    /**
     * Begin drawing on the canvas with specified width, height
     * and background color.
     *
     * @param width           in pixels
     * @param height          in pixels
     * @param backgroundColor background color
     * @return result canvas
     */
    public Canvas beginDraw(int width, int height, int backgroundColor) {
        createCanvas(width, height, backgroundColor);
        return mCanvas;
    }

    private void createCanvas(int width, int height, int backgroundColor) {
        // Try reusing bitmap when the dimension is big enough and does not
        // change too much
        int bw = mBitmap == null ? 0 : mBitmap.getWidth();
        int bh = mBitmap == null ? 0 : mBitmap.getHeight();
        if (bw < width || bh < height
                || bw > width + 100 || bh > height + 100) {
            destroyCanvas();

            mBitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            if (backgroundColor != Color.TRANSPARENT) {
                mCanvas.drawColor(backgroundColor, PorterDuff.Mode.SRC);
            }
        } else {
            mCanvas.drawColor(backgroundColor, PorterDuff.Mode.SRC);
        }
        mBox.set(0, 0, width, height);
        mDimension.set(width, height);
    }

    public void endDraw() {
        convertToTexture();
        fitTextureSize();
    }

    private void convertToTexture() {
        setImageSource(new ImageSource(ImageSource.BITMAP, mBitmap));
    }

    private void fitTextureSize() {
        setSourceRect(mBox);
        setSize(mDimension);
    }

    private void destroyCanvas() {
        if (mBitmap != null) {
            if (Ngin3d.DEBUG) {
                Log.v(TAG, "Recycle bitmap: " + mBitmap);
            }
            mBitmap.recycle();
            mBitmap = null;
        }
        mCanvas = null;
    }
}
