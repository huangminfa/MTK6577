package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.view.MotionEvent;
import com.mediatek.ngin3d.*;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.*;
import com.mediatek.ngin3d.demo.R;

public class TransitionEffectDemo extends StageActivity {
    private Image mGImg1;
    private Image mGImg2;
    private boolean flipped;

    // Note that although this appears to be a 'landscape' demo the coordinates
    // are for a portrait configuration, so rotation is around the Y axis.
    private static final int XPOS = 240;
    private static final int YPOS = 400;
    private static final int ZPOS = -600; // move away while flipping

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGImg1 = Image.createFromResource(getResources(), R.drawable.trans1);

        mGImg1.setAnchorPoint(new Point(0.5f, 0.5f));
        mGImg1.setPosition(new Point(XPOS, YPOS, 0));

        mGImg2 = Image.createFromResource(getResources(), R.drawable.trans2);
        mGImg2.setAnchorPoint(new Point(0.5f, 0.5f));
        mGImg2.setPosition(new Point(XPOS, YPOS, 0));
        mGImg2.setRotation(new Rotation(0, -180, 0));

        mStage.setTransition(Transition.FLY);
        mStage.add(mGImg1);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mStage.isTransitionComplete()) {
                if (flipped) {
                    Point p2 = mGImg1.getPosition();
                    Point p1 = mGImg2.getPosition();
                    Image tmpImage;
                    tmpImage = mGImg1;
                    mGImg1 = mGImg2;
                    mGImg2 = tmpImage;

                    mGImg1.setPosition(p1);
                    mGImg2.setPosition(p2);
                }

                AnimationGroup firstGroup = new AnimationGroup();
                firstGroup.add(new PropertyAnimation(mGImg1, "color", new Color(255, 255, 255), new Color(0, 0, 0)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200))
                        .add(new PropertyAnimation(mGImg1, "rotation", new Rotation(0, 0, 0), new Rotation(0, -180, 0)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200))
                        .add(new PropertyAnimation(mGImg1, "position", new Point(XPOS, YPOS, 0), new Point(XPOS, YPOS, ZPOS)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200));

                AnimationGroup lastGroup = new AnimationGroup();
                lastGroup.add(new PropertyAnimation(mGImg2, "color", new Color(0, 0, 0), new Color(255, 255, 255)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200))
                        .add(new PropertyAnimation(mGImg2, "rotation", new Rotation(0, 180, 0), new Rotation(0, 0, 0)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200))
                        .add(new PropertyAnimation(mGImg2, "position", new Point(XPOS, YPOS, ZPOS), new Point(XPOS, YPOS, 0)).setMode(Mode.EASE_IN_OUT_CUBIC).setDuration(1200));

                mStage.setTransition(lastGroup, firstGroup);
                mStage.replace(mGImg1, mGImg2);
                flipped = true;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}