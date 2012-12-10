
package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Timeline;

/**
 * Add description here.
 */
public class AnchorDemo extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Image danger = Image.createFromResource(getResources(), R.drawable.danger);

        danger.setPosition(new Point(0.5f, 0.5f, true));

        mStage.add(danger);

        Timeline timeline = new Timeline(2000);
        timeline.addListener(new Timeline.Listener() {

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {

                danger.setAnchorPoint(new Point(1.0f * timeline.getProgress(), 1.0f * timeline
                        .getProgress(), 1.0f * timeline.getProgress()));
            }

            public void onStarted(Timeline timeline) {
            }

            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker,
                    int direction) {
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
