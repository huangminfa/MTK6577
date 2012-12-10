package com.mediatek.world3d;

import android.app.Activity;
import android.os.Bundle;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.animation.AnimationLoader;

public class World3D extends Activity{
    private Stage mStage;
    private RotationManager mSceneManager;
    private GestureDetector mGestureDetector;

    private void setupBackground() {
        Image backgroundImage = Image.createFromResource(getResources(), R.drawable.bg_stereo);
        backgroundImage.setPosition(new Point(0.5f, 0.5f, 600f, true));
        backgroundImage.setScale(new Scale(2.5f, 2.5f));
        backgroundImage.setAlphaSource(Plane.OPAQUE);
        mStage.add(backgroundImage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world3d);

        mStage = new Stage(new TinyUiHandler());
        mStage.setProjection(Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -2111.0f);

        WorldStageView customView = new WorldStageView(this, mStage);
        customView.activateStereo3D(true);

        FrameLayout layout = (FrameLayout)findViewById(R.id.stage_root);
        layout.addView(customView);

        AnimationLoader.setCacheDir(getCacheDir());
        mGestureDetector = new GestureDetector(this, new TinyGestureListener());

        setupBackground();

        mSceneManager = new RotationManager(this, mStage);
        mSceneManager.init();
        mSceneManager.startTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        return super.dispatchTouchEvent(e) || mGestureDetector.onTouchEvent(e);
    }

    private class TinyGestureListener extends GestureDetector.SimpleOnGestureListener {        
        final private Point mHitPoint = new Point(0, 0);
        private boolean isPositive(float val) { return (val < 0); }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }

            mHitPoint.set(event.getX(), event.getY(), 0);
            mSceneManager.hit(mStage.hitTest(new Point(event.getX(), event.getY())), mHitPoint);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelX = Math.abs(velocityX);
            float absVelY = Math.abs(velocityY);
            final int flingThreshold = 200;
            if (absVelX > absVelY) {
                if (absVelX < flingThreshold) {
                    return false;
                }
                mSceneManager.rotate(isPositive(velocityX));
            }
            return true;
        }
    }

    private static class TinyUiHandler implements UiHandler {
        final private Handler mHandler = new Handler();
        public void post(Runnable runnable) {
            mHandler.post(runnable);
        }
    }
}