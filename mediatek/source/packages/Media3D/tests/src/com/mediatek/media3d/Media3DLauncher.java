package com.mediatek.media3d;

import android.app.Activity;
import android.test.LaunchPerformanceBase;
import android.os.Bundle;

public class Media3DLauncher extends LaunchPerformanceBase {

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        mIntent.setClassName(getTargetContext(), "com.mediatek.media3d.Main");
        start();
    }

    /**
     * Calls LaunchApp and finish.
     */
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
