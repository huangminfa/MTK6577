package com.mediatek.ngin3d;

import com.mediatek.ngin3d.presentation.Presentation;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * An empty actor that display nothing. For testing purpose only.
 * @hide
 */
public class Empty extends Actor<Presentation> {

    protected Presentation createPresentation(PresentationEngine engine) {
        return engine.createEmpty();
    }

}
