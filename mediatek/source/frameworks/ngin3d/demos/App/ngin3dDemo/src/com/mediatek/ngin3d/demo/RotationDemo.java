package com.mediatek.ngin3d.demo;

import android.os.Bundle;

import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class RotationDemo extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Image danger = Image.createFromResource(getResources(), R.drawable.danger);

        danger.setPosition(new Point(0.5f, 0.5f, true));

        mStage.add(danger);

        Timeline timeline = new Timeline(2000);
        timeline.addListener(new Timeline.Listener() {
            private Rotation mRotation = new Rotation();

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                mRotation.set(0, 0, 1, 360.f * timeline.getProgress());
                danger.setRotation(mRotation);
            }

            public void onStarted(Timeline timeline) {
            }

            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {
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