package com.mediatek.ngin3d;

import android.util.Log;
import com.mediatek.ngin3d.a3m.A3mPresentationEngine;
import com.mediatek.ngin3d.presentation.PresentationEngine;

/**
 * Provide static method to create presentation engine
 * @hide
 */
public final class Ngin3d {
    public static final boolean DEBUG = false;
    public static final String TAG = "ngin3d";
    public static final String VERSION = "1.1.1229";

    private Ngin3d() {
        // Do nothing
    }

    public static PresentationEngine createPresentationEngine(Stage stage) {
        Log.d(TAG, "ngin3d version:" + VERSION);
        return new A3mPresentationEngine(stage);
    }
}
