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
 * Grahics3D Presentation for A3M
 */
package com.mediatek.ngin3d.a3m;

import com.mediatek.a3m.SceneNode;
import com.mediatek.ngin3d.presentation.Graphics3d;

/**
 * The presentation that provides 3D graphics drawing.
 * @hide
 */
// \todo implement
public class Graphics3dPresentation extends SceneNodePresentation<SceneNode>
    implements Graphics3d {

    private Renderer mRenderer;

    public Graphics3dPresentation(A3mPresentationEngine engine) {
        super(engine);
    }

    /**
     * Un-initialize this object
     */
    @Override
    public void onUninitialize() {
        mRenderer = null;
        super.onUninitialize();
    }

    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    public void render(int frame) {
        // do nothing by default
    }

    public void onDrawFrame() {
        if (mRenderer != null) {
            mRenderer.onDrawFrame(this);
        }
    }
}
