package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class AspectRatioDemo extends StageActivity {
    private static final String TAG = "AspectRatioDemo";

    private static final int[] PHOTO_FRAMES = new int[] {
        R.drawable.earth,
        R.drawable.danger,
        R.drawable.icon_nightcloud,
        R.drawable.photo_04,
    };

    private static final int[] ANIMATIONS = new int[] {
        R.raw.photo_next_enter_photo1_ani, R.raw.photo_next_enter_photo2_ani,
        R.raw.photo_next_enter_photo3_ani, R.raw.photo_next_enter_photo4_ani,
        R.raw.photo_swap_enter_slideshow_ani
    };

    private static final int PHOTO_PER_PAGE = 4;
    Image[] photo = new Image[PHOTO_PER_PAGE];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted" + animation);
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted" + animation);
                for(int i = 0 ; i < PHOTO_PER_PAGE ; i ++)
                    photo[i].setKeepAspectRatio(true);
            }
        };

        // This demo uses an animation designed for a depracated, left-handed,
        // coordinate system.  To use this on the current right-handed system
        // we need to use a special projection.
        // This projection MUST NOT be used for new code.
        mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );


        for (int i = 0; i < PHOTO_PER_PAGE; i++) {
            photo[i] = Image.createFromResource(getResources(), PHOTO_FRAMES[i]);
            photo[i].setSize(new Dimension(600,200));
            photo[i].setKeepAspectRatio(false);
            mStage.add(photo[i]);
            Animation animation = AnimationLoader.loadAnimation(this, ANIMATIONS[i]);
            animation.setTarget(photo[i]);
            animation.addListener(l);
            animation.start();
        }
    }
}
