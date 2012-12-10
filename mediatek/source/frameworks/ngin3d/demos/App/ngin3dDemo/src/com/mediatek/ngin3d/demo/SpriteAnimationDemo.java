package com.mediatek.ngin3d.demo;

import android.content.res.Resources;
import android.os.Bundle;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.android.StageActivity;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.SpriteAnimation;
import com.mediatek.ngin3d.demo.R;

public class SpriteAnimationDemo extends StageActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources resources = getResources();
        Image target = Image.createFromResource(resources, R.drawable.angel1);
        mStage.add(target);

        SpriteAnimation sprite = new SpriteAnimation(target, 200);
        sprite.addSprite(resources, R.drawable.angel1);
        sprite.addSprite(resources, R.drawable.angel2);
        sprite.addSprite(resources, R.drawable.angel1);
        sprite.addSprite(resources, R.drawable.angel3);

        BasicAnimation move = new PropertyAnimation(target, Actor.PROP_POSITION, new Point(0, 0), new Point(800, 480))
            .setMode(Mode.EASE_IN_OUT_CUBIC)
            .setLoop(true)
            .setAutoReverse(true);
        BasicAnimation scale = new PropertyAnimation(target, Actor.PROP_SCALE, new Scale(0.5f, 0.5f, 0.5f), new Scale(2, 2, 2))
            .setMode(Mode.LINEAR)
            .setLoop(true)
            .setAutoReverse(true);

        Image target2 = Image.createFromResource(resources, R.drawable.hamtro_walk);
        target2.setPosition(new Point(200, 50));
        mStage.add(target2);
        SpriteAnimation walk = new SpriteAnimation(
            resources,
            target2,
            R.drawable.hamtro_walk,
            5000,
            resources.getDimensionPixelSize(R.dimen.hamtaro_sprite_width),
            resources.getDimensionPixelSize(R.dimen.hamtaro_sprite_height));

        BasicAnimation walkMove = new PropertyAnimation(target2, Actor.PROP_POSITION, new Point(200, 50), new Point(200, 400))
            .setMode(Mode.LINEAR)
            .setLoop(true)
            .setAutoReverse(true)
            .setDuration(2500);

        sprite.setLoop(true);
        sprite.start();
        move.start();
        scale.start();
        walk.setLoop(true);
        walk.start();
        walkMove.start();
    }
}
