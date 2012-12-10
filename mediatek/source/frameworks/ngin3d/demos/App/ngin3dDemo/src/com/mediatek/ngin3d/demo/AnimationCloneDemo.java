package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.AnimationLoader;
import com.mediatek.ngin3d.demo.R;

/**
 * This demo shows how to clone an animation.
 */
public class AnimationCloneDemo extends StageActivity {

    private AnimationGroup mGroup = new AnimationGroup();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Container c1 = new Container();
        Container c2 = new Container();
        c1.setPosition(new Point(0.2f, 0.2f, 0, true));
        c2.setPosition(new Point(0.6f, 0.6f, 0, true));
        final Image photo1 = Image.createFromResource(getResources(), R.drawable.photo_01);
        final Image photo2 = Image.createFromResource(getResources(), R.drawable.photo_01);
        c1.add(photo1);
        c2.add(photo2);
        mStage.add(c1, c2);

        // This demo uses an animation designed for a depracated, left-handed,
        // coordinate system.  To use this on the current right-handed system
        // we need to use a special projection.
        // This projection MUST NOT be used for new code.
        mStage.setProjection( Stage.UI_PERSPECTIVE_LHC, 2.0f, 3000.0f, -1111.0f );

        Animation ani1 = AnimationLoader.loadAnimation(this, R.raw.photo_photofly);
        Animation ani2 = ani1.clone();
        ani1.setTarget(photo1);
        ani2.setTarget(photo2);

        mGroup.add(ani1);
        mGroup.add(ani2);
        mGroup.setLoop(true).setAutoReverse(true).start();
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
