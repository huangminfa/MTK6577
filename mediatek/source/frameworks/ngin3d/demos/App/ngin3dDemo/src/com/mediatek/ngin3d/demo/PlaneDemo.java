package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class PlaneDemo extends StageActivity {

    private static final int[] ANIMATIONS = new int[] {
        R.raw.photo_swap_enter_photo1_ani, R.raw.photo_swap_enter_photo2_ani,
        R.raw.photo_swap_enter_photo3_ani, R.raw.photo_swap_enter_photo4_ani,
        R.raw.photo_swap_enter_slideshow_ani
    };

    private static final int PLANE_PER_PAGE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Container c = new Container();
        c.setColor(new Color(128, 128, 128, 128));
        mStage.add(c);

        // This demo uses an animation designed for a depracated, left-handed,
        // coordinate system.  To use this on the current right-handed system
        // we need to use a special projection.
        // This projection MUST NOT be used for new code.
        mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

        for (int i = 0; i < PLANE_PER_PAGE; i++) {
            final Plane plane = Plane.create(new Dimension(300, 200));
            c.add(plane);
            if(i == 0) {
                plane.setColor(new Color(255, 0, 0, 10));
                plane.setMaterialType(ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA);
            }
            else if (i == 1) {
                plane.setColor(new Color(0, 255, 0, 10));
                plane.setMaterialType(ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA);
            }
            else if (i == 2) {
                plane.setColor(new Color(0, 0, 255, 10));
                plane.setMaterialType(ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA);
            }
            else if (i == 3) {
                plane.setColor(new Color(0, 255, 255, 10));
                plane.setMaterialType(ImageDisplay.EMT_TRANSPARENT_VERTEX_ALPHA);
            }

            Animation animation = AnimationLoader.loadAnimation(this, ANIMATIONS[i]);
            animation.setTarget(plane);
            animation.start();
        }

        Timeline timeline = new Timeline(5000);
        timeline.addListener(new Timeline.Listener() {
            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
                int color = (int)(255 * timeline.getProgress());
                c.setColor(new Color(color , color, color, color));
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
