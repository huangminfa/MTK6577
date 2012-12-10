package com.mediatek.ngin3d.demo;

import android.os.Bundle;
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
public class ScriptDemo extends StageActivity {
    private static final String TAG = "ScriptDemo";

    private static final int[] PHOTO_FRAMES = new int[] {
        R.drawable.photo_01,
        R.drawable.photo_02,
        R.drawable.photo_03,
        R.drawable.photo_04,
    };

    private static final int[] ANIMATIONS = new int[] {
        R.raw.photo_swap_enter_right_photo1_ani, R.raw.photo_swap_enter_right_photo2_ani,
        R.raw.photo_swap_enter_right_photo3_ani, R.raw.photo_swap_enter_right_photo4_ani,
        R.raw.photo_swap_enter_slideshow_ani
    };

    private AnimationGroup mGroup = new AnimationGroup();
    private static final int PHOTO_PER_PAGE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted " + animation);
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted " + animation);
            }

            public void onMarkerReached(Animation animation, int direction, String marker) {
                android.util.Log.v(TAG, "onMarkerReached " + marker + " Direction: " + direction);
            }
        };

        for (int i = 0; i < PHOTO_PER_PAGE; i++) {
            final Image photo = Image.createFromResource(getResources(), PHOTO_FRAMES[i]);
            mStage.add(photo);

            // This demo uses an animation designed for a depracated, left-handed,
            // coordinate system.  To use this on the current right-handed system
            // we need to use a special projection.
            // This projection MUST NOT be used for new code.
            mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

            Animation animation = AnimationLoader.loadAnimation(this, ANIMATIONS[i]);
            mGroup.add(animation);
            animation.setTarget(photo);
            animation.addListener(l);
        }
        mGroup.setLoop(true);
        mGroup.setAutoReverse(true);
        mGroup.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            reverseAnimations();
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void reverseAnimations() {
        mGroup.reverse();
    }
}
