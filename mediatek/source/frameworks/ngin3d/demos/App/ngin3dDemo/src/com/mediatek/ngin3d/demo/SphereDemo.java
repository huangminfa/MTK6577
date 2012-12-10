package com.mediatek.ngin3d.demo;

import android.os.Bundle;

import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Sphere;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class SphereDemo extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Sphere earth = Sphere.createFromResource(getResources(),
            R.drawable.earth);

        earth.setPosition(new Point(400, 240, 0));
        // Scale Y -1 as UI-Perspective is Y-down, but model is Y-up
        earth.setScale(new Scale(400, -400, 400));
        mStage.add(earth);

        Timeline timeline = new Timeline(5000);
        timeline.addListener(new Timeline.Listener() {
            private Rotation mRotation = new Rotation();

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                mRotation.set(0, 1, 0, 360.f * timeline.getProgress());
                earth.setRotation(mRotation);
            }

            public void onStarted(Timeline timeline) {
            }

            public void onMarkerReached(Timeline timeline, int elapsedMsecs,
                String marker, int direction) {
            }

            public void onPaused(Timeline timeline) {
            }

            public void onCompleted(Timeline timeline) {
            }

            public void onLooped(Timeline timeline) {
            }
        });

        timeline.setLoop(true);
        timeline.start();
    }

}
