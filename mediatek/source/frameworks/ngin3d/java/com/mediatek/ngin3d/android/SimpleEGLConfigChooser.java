package com.mediatek.ngin3d.android;

/**
 * This class will choose a RGBA_8888 surface with
 * or without a depth buffer.
 *
 */
public class SimpleEGLConfigChooser extends ComponentSizeChooser {
    public SimpleEGLConfigChooser(boolean withDepthBuffer) {
        super(8, 8, 8, 8, withDepthBuffer ? 16 : 0, 0);
    }
}
