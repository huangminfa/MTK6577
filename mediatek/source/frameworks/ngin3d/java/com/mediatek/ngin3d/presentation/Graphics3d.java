package com.mediatek.ngin3d.presentation;

/**
 * Provides 3D drawing.
 */
public interface Graphics3d extends Presentation {
    interface Renderer {
        void onDrawFrame(Graphics3d g3d);
    }

    void setRenderer(Renderer renderer);
}
