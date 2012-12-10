package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Add description here.
 */
public class AnimationStress extends StageActivity {
    private static final String TAG = "ScriptDemo";

    private List<Animation> animationList = new ArrayList<Animation>();
    private final Image[] photo = new Image[4];
    private int Count = 0;
    private static final int[] PHOTO_FRAMES = new int[] {
        R.drawable.photo_01,
        R.drawable.photo_02,
        R.drawable.photo_03,
        R.drawable.photo_04,
    };

    private static final int[] ENTER = new int[] {
            R.raw.photo_swap_enter_photo1_ani,
            R.raw.photo_swap_enter_photo2_ani,
            R.raw.photo_swap_enter_photo3_ani,
            R.raw.photo_swap_enter_photo4_ani,
            R.raw.photo_swap_enter_slideshow_ani
    };

    private static final int[] EXIT = new int[] {
            R.raw.photo_swap_exit_photo1_ani,
            R.raw.photo_swap_exit_photo2_ani,
            R.raw.photo_swap_exit_photo3_ani,
            R.raw.photo_swap_exit_photo4_ani,
            R.raw.photo_swap_exit_slideshow_ani
    };

    private static final int[] NEXT_ENTER = new int[] {
            R.raw.photo_next_enter_photo1_ani,
            R.raw.photo_next_enter_photo2_ani,
            R.raw.photo_next_enter_photo3_ani,
            R.raw.photo_next_enter_photo4_ani,
            R.raw.photo_next_enter_slideshow_ani
    };

    private static final int[] NEXT_EXIT = new int[] {
            R.raw.photo_next_exit_photo1_ani,
            R.raw.photo_next_exit_photo2_ani,
            R.raw.photo_next_exit_photo3_ani,
            R.raw.photo_next_exit_photo4_ani,
            R.raw.photo_next_exit_slideshow_ani
    };

    private static final int[] LAST_ENTER = new int[] {
            R.raw.photo_last_enter_photo1_ani,
            R.raw.photo_last_enter_photo2_ani,
            R.raw.photo_last_enter_photo3_ani,
            R.raw.photo_last_enter_photo4_ani,
            R.raw.photo_last_enter_slideshow_ani
    };

    private static final int[] LAST_EXIT = new int[] {
            R.raw.photo_last_exit_photo1_ani,
            R.raw.photo_last_exit_photo2_ani,
            R.raw.photo_last_exit_photo3_ani,
            R.raw.photo_last_exit_photo4_ani,
            R.raw.photo_last_exit_slideshow_ani
    };

    private static final int[] ANIMATIONS = new int[] {
        R.raw.photo_swap_enter_photo1_ani, R.raw.photo_swap_enter_photo2_ani,
        R.raw.photo_swap_enter_photo3_ani, R.raw.photo_swap_enter_photo4_ani,
        R.raw.photo_swap_enter_slideshow_ani
    };

    private static final int PHOTO_PER_PAGE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted" + animation);
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted" + animation);
                Count ++;
                Count %= 300;
                Animation an = animationList.get(Count);
                an.setTarget(photo[Count % 4]);
                an.addListener(this);
                an.start();
            }
        };

        // This demo uses an animation designed for a depracated, left-handed,
        // coordinate system.  To use this on the current right-handed system
        // we need to use a special projection.
        // This projection MUST NOT be used for new code.
        mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

        for (int i = 0; i < PHOTO_PER_PAGE; i++) {
            photo[i] = Image.createFromResource(getResources(), PHOTO_FRAMES[i]);
            mStage.add(photo[i]);
            Animation animation = AnimationLoader.loadAnimation(this, ANIMATIONS[i]);
            animation.setTarget(photo[i]);
            animation.addListener(l);
            animation.start();
        }

        for (int i = 0 ; i < 20 ; i ++) {
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, ENTER[j]));
            }
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, EXIT[j]));
            }
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, NEXT_ENTER[j]));
            }
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, NEXT_EXIT[j]));
            }
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, LAST_ENTER[j]));
            }
            for (int j = 0 ; j < PHOTO_PER_PAGE ; j ++) {
                animationList.add(AnimationLoader.loadAnimation(this, LAST_EXIT[j]));
            }
        }
    }
}
