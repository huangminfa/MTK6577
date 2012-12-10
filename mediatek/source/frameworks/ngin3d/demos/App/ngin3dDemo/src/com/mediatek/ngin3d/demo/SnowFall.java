package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.os.Handler;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.demo.R;
import java.util.Random;

/**
 * Add description here.
 */
public class SnowFall extends StageActivity {

    private Handler mHandler = new Handler();
    private Random mRandom = new Random(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private final int NEW_FLAKE_INTERVAL = 200;

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.postDelayed(mNewFlake, NEW_FLAKE_INTERVAL);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mNewFlake);
        super.onPause();
    }

    private Runnable mNewFlake = new Runnable() {
        public void run() {
            final Image flake = Image.createFromResource(getResources(), R.drawable.flake);

            int startX = mRandom.nextInt(800);
            int endX = mRandom.nextInt(800);
            float scale = 0.5f + mRandom.nextFloat();
            float speed = 1.0f + mRandom.nextFloat();

            flake.setScale(new Scale(scale, scale));
            Animation ani = new PropertyAnimation(flake, "position", new Point(startX, -100), new Point(endX, 500)) {
                @Override
                public void onCompleted(int direction) {
                    mStage.remove(mTarget);
                }
            }.setDuration((int) (5000 * speed));

            ani.start();
            mStage.add(flake);

            mHandler.postDelayed(this, NEW_FLAKE_INTERVAL);
        }
    };
}
