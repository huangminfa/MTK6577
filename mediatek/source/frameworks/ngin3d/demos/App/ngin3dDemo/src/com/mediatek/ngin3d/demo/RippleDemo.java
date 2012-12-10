package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class RippleDemo extends StageActivity {
    private static final String TAG = "RippleDemo";

    private static final String REFLECTION = "reflection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted" + animation);
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted" + animation);
            }
        };

        // This demo uses an animation designed for a depracated, left-handed,
        // coordinate system.  To use this on the current right-handed system
        // we need to use a special projection.
        // This projection MUST NOT be used for new code.
        mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

        final Image photo = Image.createFromResource(getResources(), R.drawable.portal_photo_demo);
        mStage.add(photo);
        Animation animation = AnimationLoader.loadAnimation(this, R.raw.landscape_mainmenu_in_top_photo);
        animation.setTarget(photo);
        animation.addListener(l);
        animation.start();

        final Image surface = Image.createFromResource(getResources(), R.drawable.portal_photo_demo);
        surface.setRenderingHint(REFLECTION, true);
        mStage.add(surface);
        surface.setPosition(new Point(341, 337, 86));
        surface.setScale(new Scale(111.5f, 100, 100));
        surface.setRotation(new Rotation(273, 0, 0));

    }
}
