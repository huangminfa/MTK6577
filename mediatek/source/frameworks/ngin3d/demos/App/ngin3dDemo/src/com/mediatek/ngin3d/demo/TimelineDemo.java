package com.mediatek.ngin3d.demo;

import android.app.Activity;
import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class TimelineDemo extends Activity {

    private Stage mStage = new Stage();
    private StageView mStageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStageView = new StageView(this, mStage);
        setContentView(mStageView);

        final Image danger = Image.createFromResource(getResources(), R.drawable.danger);
        mStage.add(danger);

        Timeline timeline = new Timeline(2000);
        timeline.addListener(new Timeline.Listener() {
            private Point mPosition = new Point();

            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                mPosition.x = 400 * elapsedMsecs / timeline.getRunningDuration();
                mPosition.y = 200 * elapsedMsecs / timeline.getRunningDuration();
                mPosition.z = 100 * elapsedMsecs / timeline.getRunningDuration();
                danger.setPosition(mPosition);
            }

            public void onStarted(Timeline timeline) {}
            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {}
            public void onPaused(Timeline timeline) {}
            public void onCompleted(Timeline timeline) {}
            public void onLooped(Timeline timeline) {}
        });

        timeline.setLoop(true);
        timeline.setAutoReverse(true);
        timeline.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStageView.onResume();
    }

    @Override
    protected void onPause() {
        mStageView.onPause();
        super.onPause();
    }
}
