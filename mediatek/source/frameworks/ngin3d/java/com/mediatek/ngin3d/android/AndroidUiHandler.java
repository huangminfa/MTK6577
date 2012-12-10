package com.mediatek.ngin3d.android;

import android.os.Handler;
import com.mediatek.ngin3d.UiHandler;

public final class AndroidUiHandler {

    private AndroidUiHandler() {
        // Do nothing
    }

    public static UiHandler create() {
        final Handler handler = new Handler();
        UiHandler uiHandler = new UiHandler() {
            public void post(Runnable runnable) {
                handler.post(runnable);
            }
        };

        return uiHandler;
    }

}
