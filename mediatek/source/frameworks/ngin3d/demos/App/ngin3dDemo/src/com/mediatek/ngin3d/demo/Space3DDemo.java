
package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Sphere;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.PropertyAnimation;

/**
 * Add description here.
 */
public class Space3DDemo extends StageActivity {

    private int mEarthSize = 200;
    private int mMoomSize = (int) (mEarthSize / 3.66);
    private GestureDetector mGestureDetector;

    public class MyGestureDetector extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {

            if (e1.getAction() != MotionEvent.ACTION_POINTER_DOWN
                    || e1.getAction() != MotionEvent.ACTION_POINTER_UP) {
                if (distanceX < -20) {
                    Log.e("rrr", "rrr");
                }
                if (distanceX > 20) {
                    Log.e("lll", "lll");
                }

                if (distanceY > 20) {
                    Log.e("uuu", "uuu");
                }
                if (distanceY < -20) {
                    Log.e("ddd", "ddd");
                }
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent upEvent) {

            return true;
        }

    }

    public boolean onTouchEvent(MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(new MyGestureDetector());

        final Sphere earth = Sphere.createFromResource(getResources(), R.drawable.earth_atmos_2048);

        final Sphere moom = Sphere.createFromResource(getResources(), R.drawable.moon_1024);

        Container container = new Container();
        // container.add(earth);
        container.add(moom);

        // earth.setPosition(new Point(400, 240, 0));
        // earth.setPosition(new Point(0.5f, 0.5f, true));
        earth.setScale(new Scale(mEarthSize, -mEarthSize, mEarthSize));

        // moom.setPosition(new Point(0.7f, 0.7f, true));
        moom.setScale(new Scale(mMoomSize, -mMoomSize, mMoomSize));

        container.setPosition(new Point(400f, 240f, false));
        earth.setPosition(new Point(400f, 240f, false));
        moom.setPosition(new Point(268.2f, 0f, 134.16f, false));

        // container.setPosition(new Point(0.5f, 0.5f, false));
        // earth.setPosition(new Point(0.5f, 0.5f, false));
        // moom.setPosition(new Point(0.7f, 0.7f, false));
        mStage.add(earth, container);

        /*
         * Timeline timeline = new Timeline(24000); timeline.addListener(new
         * Timeline.Listener() { private Rotation mRotation = new Rotation();
         * public void onNewFrame(Timeline timeline, int elapsedMsecs) { // if
         * (Ngin3d.usingA3m()) { // mRotation.set(0, 1, 0, 360.f *
         * timeline.getProgress()); // } // else{ mRotation.set(0, 1, 0, 360.f *
         * timeline.getProgress()); // } earth.setRotation(mRotation);
         * //moom.setRotation(mRotation); } public void onStarted(Timeline
         * timeline) { } public void onMarkerReached(Timeline timeline, int
         * elapsedMsecs, String marker, int direction) { } public void
         * onPaused(Timeline timeline) { } public void onCompleted(Timeline
         * timeline) { } public void onLooped(Timeline timeline) { } });
         * timeline.setLoop(true); timeline.start(); Timeline timelineForMoom =
         * new Timeline(24000*27); timelineForMoom.addListener(new
         * Timeline.Listener() { private Rotation mRotation = new Rotation();
         * public void onNewFrame(Timeline timelineForMoom, int elapsedMsecs) {
         * // if (Ngin3d.usingA3m()) { // mRotation.set(0, 1, 0, 360.f *
         * timeline.getProgress()); // } // else{ mRotation.set(0, 1, 0, 360.f *
         * timelineForMoom.getProgress()); // } //earth.setRotation(mRotation);
         * moom.setRotation(mRotation); } public void onStarted(Timeline
         * timeline) { } public void onMarkerReached(Timeline timeline, int
         * elapsedMsecs, String marker, int direction) { } public void
         * onPaused(Timeline timeline) { } public void onCompleted(Timeline
         * timeline) { } public void onLooped(Timeline timeline) { } });
         * timelineForMoom.setLoop(true); timelineForMoom.start();
         */
        new PropertyAnimation(earth, "rotation", new Rotation(0, 0, 0), new Rotation(0, 360, 0))
                .setDuration(24000).setLoop(true).start();
        // new PropertyAnimation(moom, "rotation", new Rotation(0, 0, 0), new
        // Rotation(0, 0, 0))
        // .setDuration(5000).setLoop(true).start();

        new PropertyAnimation(container, "rotation", new Rotation(0, 0, 0), new Rotation(0, 360, 0))
                .setDuration(24000).setLoop(true).start();
    }

}
