package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class DragAnimationDemo extends StageActivity {
    private static final String TAG = "DragAnimationDemo";

    private static final int[] PHOTO_FRAMES = new int[] {
        R.drawable.photo_01,
        R.drawable.photo_02,
        R.drawable.photo_03,
        R.drawable.photo_04,
    };

    private static final int[] WEATHER_FRAMES = new int[] {
        R.drawable.icon_moon,
        R.drawable.icon_sun,
        R.drawable.icon_nightcloud,
        R.drawable.icon_sun2,
    };

    private static final int[] LAST_ENTER = new int[] {
        R.raw.photo_last_enter_photo1_ani, R.raw.photo_last_enter_photo2_ani,
        R.raw.photo_last_enter_photo3_ani, R.raw.photo_last_enter_photo4_ani
    };

    private static final int[] LAST_EXIT = new int[] {
        R.raw.photo_last_exit_photo1_ani, R.raw.photo_last_exit_photo2_ani,
        R.raw.photo_last_exit_photo3_ani, R.raw.photo_last_exit_photo4_ani
    };

    private AnimationGroup mGroup = new AnimationGroup();
    private static final int PHOTO_PER_PAGE = 4;
    private GestureDetector mGestureDetector;
    private int mDistanceY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted " + animation);
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted " + animation);
                if (animation.getDirection() == Animation.FORWARD) {
                    animation.setDirection(Animation.BACKWARD);
                } else {
                    animation.setDirection(Animation.FORWARD);
                }
            }

            public void onMarkerReached(Animation animation, int direction, String marker) {
                android.util.Log.v(TAG, "onMarkerReached " + marker + " Direction: " + direction);
            }
        };

        for (int i = 0; i < PHOTO_PER_PAGE; i++) {
            final Image photo = Image.createFromResource(getResources(), PHOTO_FRAMES[i]);
            final Image weather = Image.createFromResource(getResources(), WEATHER_FRAMES[i]);
            mStage.add(photo);
            mStage.add(weather);

            // This demo uses an animation designed for a depracated, left-handed,
            // coordinate system.  To use this on the current right-handed system
            // we need to use a special projection.
            // This projection MUST NOT be used for new code.
            mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

            Animation photoAni = AnimationLoader.loadAnimation(this, LAST_ENTER[i]);
            Animation weatherAni = AnimationLoader.loadAnimation(this, LAST_EXIT[i]);
            photoAni.setTarget(photo);
            weatherAni.setTarget(weather);

            mGroup.add(photoAni);
            mGroup.add(weatherAni);
        }
        mGroup.addListener(l);
        mGroup.disableOptions(Animation.START_TARGET_WITH_INITIAL_VALUE);
        mGroup.start();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mDistanceY += distanceY;
            if (mGroup.getDirection() == Animation.BACKWARD) {
                if (mDistanceY > 0) {
                    mDistanceY = 0;
                }
                mGroup.setProgress(1 + (float)mDistanceY / 400f);
            } else {
                if (mDistanceY < 0) {
                    mDistanceY = 0;
                }
                mGroup.setProgress((float)mDistanceY / 400f);
            }

            Log.v(TAG, "disX:" + distanceX + "disY:" + distanceY);
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mGroup.start();
            float progress = (float)mDistanceY / 400f;
            if (mGroup.getDirection() == Animation.BACKWARD) {
                if ((1 + progress) > 0.5) {
                    mGroup.reverse();
                }
            } else {
                if (progress < 0.5) {
                    mGroup.reverse();
                }
            }
            mDistanceY = 0;
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        boolean handled = super.dispatchTouchEvent(m);
        if (!handled) {
            handled = mGestureDetector.onTouchEvent(m);
        }

        if (!handled) {
            handled = onTouchEvent(m);
        }
        return handled;
    }
}
