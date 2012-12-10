package com.mediatek.ngin3d;

import com.mediatek.ngin3d.presentation.Graphics3d;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * A canvas that can draw by OpenGL ES API.
 */
public class Canvas3d extends Actor<Graphics3d> implements Graphics3d.Renderer {

    /**
     * Override this method to provide custom drawing using OpenGL API.
     * 
     * @param g3d graphics 3D object
     */
    public void onDrawFrame(Graphics3d g3d) {
        // do nothing by default
    }

    ///////////////////////////////////////////////////////////////////////////
    // Presentation
    
    /**
     * @hide
     */
    @Override
    protected Graphics3d createPresentation(PresentationEngine engine) {
        Graphics3d g3d = engine.createGraphics3d();
        g3d.setRenderer(this);
        return g3d;
    }
}
