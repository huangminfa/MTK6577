package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.demo.R;

/**
 * Add description here.
 */
public class AnimationDemo extends StageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Image uniball = Image.createFromResource(getResources(), R.drawable.uniball);

        BasicAnimation move = new PropertyAnimation(uniball, "position", new Point(0, 0), new Point(480, 800))
                .setMode(Mode.EASE_IN_OUT_CUBIC)
                .setLoop(true)
                .setAutoReverse(true);
        BasicAnimation rotate = new PropertyAnimation(uniball, "rotation", new Rotation(0, 0, 0), new Rotation(0, 0, 360))
                .setMode(Mode.EASE_IN_SINE)
                .setLoop(true)
                .setAutoReverse(true);

        mStage.add(uniball);
        move.start();
        rotate.start();
    }
}
