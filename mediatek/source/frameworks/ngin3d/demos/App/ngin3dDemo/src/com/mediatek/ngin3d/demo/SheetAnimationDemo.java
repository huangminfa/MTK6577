package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.MasterClock;
import com.mediatek.ngin3d.animation.SpriteAnimation;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.demo.R;

public class SheetAnimationDemo extends StageActivity {
    private static final String TAG = "SheetAnimationDemo";
    private static final int  INT_MAX = 1000;

    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MasterClock.setDefault(new MasterClock());

        final Image target = Image.createFromResource(getResources(), R.drawable.hamtaro);
        target.setPosition(new Point(0.5f, 0.5f, true));
        mStage.add(target);

        SpriteAnimation.SpriteSheet sheet = new SpriteAnimation.SpriteSheet(getResources(), R.drawable.hamtaro, R.raw.hamtaro);
        final SpriteAnimation sprite = new SpriteAnimation(target, sheet, 1500);

        Animation.Listener l = new Animation.Listener() {
            public void onStarted(Animation animation) {
                android.util.Log.v(TAG, "onStarted " + animation);
                zero();
                sprite.clear();
                sprite.addSprite("gripper1.png");
            }

            public void onCompleted(Animation animation) {
                android.util.Log.v(TAG, "onCompleted " + animation);
                if (mCount > INT_MAX) {
                    mCount = 0;
                }
                switch (mCount % 4) {
                case 0:
                    sprite.clear();
                    sprite.addSprite("Hamtaro_left1.png");
                    sprite.addSprite("Hamtaro_left2.png");
                    sprite.addSprite("Hamtaro_left3.png");
                    sprite.addSprite("Hamtaro_left4.png");

                    BasicAnimation move = new PropertyAnimation(target, "position", new Point(400, 240), new Point(700, 240))
                        .setMode(Mode.LINEAR);
                    move.setDuration(1500);
                    move.start();
                    break;

                case 1:
                    sprite.clear();
                    sprite.addSprite("Hamtaro_right1.png");
                    sprite.addSprite("Hamtaro_right2.png");
                    sprite.addSprite("Hamtaro_right3.png");
                    sprite.addSprite("Hamtaro_right4.png");

                    BasicAnimation move2 = new PropertyAnimation(target, "position", new Point(700, 240), new Point(400, 240))
                        .setMode(Mode.LINEAR);
                    move2.setDuration(1500);
                    move2.start();
                    break;

                case 2:
                    sprite.clear();
                    sprite.addSprite("gripper1.png");
                    sprite.addSprite("gripper2.png");
                    sprite.addSprite("gripper3.png");
                    sprite.addSprite("gripper4.png");
                    sprite.addSprite("gripper3.png");
                    sprite.addSprite("gripper2.png");
                    sprite.addSprite("gripper1.png");
                    break;

                default:
                    sprite.clear();
                    sprite.addSprite("gripper1.png");
                }
                mCount++;
            }

            public void onMarkerReached(Animation animation, int direction, String marker) {
                android.util.Log.v(TAG, "onMarkerReached " + marker + " Direction: " + direction);
            }
        };

        sprite.addListener(l);
        sprite.setLoop(true);
        sprite.start();
    }

    private void zero() {
        mCount = 0;
    }
}
