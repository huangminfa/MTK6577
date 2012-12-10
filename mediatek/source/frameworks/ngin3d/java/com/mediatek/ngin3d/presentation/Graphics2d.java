package com.mediatek.ngin3d.presentation;

import android.graphics.Canvas;

/**
 * Provides 2D drawing.
 */
public interface Graphics2d extends ImageDisplay {

    /**
     * Begin drawing on the canvas with specified width and height.
     *
     * @param width in pixels
     * @param height in pixels
     * @param backgroundColor background color
     * @return the canvas to draw
     */
    Canvas beginDraw(int width, int height, int backgroundColor);

    /**
     * Finish the drawing.
     */
    void endDraw();

}
