package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.demo.R;
import com.mediatek.ngin3d.Transaction;
import com.mediatek.ngin3d.android.StageActivity;

import java.util.Random;

public class ImplicitAnimationDemo extends StageActivity {

    private Random mRandom = new Random(System.currentTimeMillis());
    private Actor[] mActors = new Actor[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < mActors.length; ++i)
        {
            mActors[i] = Image.createFromResource(getResources(), R.drawable.caster);
            mActors[i].setPosition(new Point(0, 0, 0, true));
            mStage.add(mActors[i]);
        }

        changeProperties();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            changeProperties();
            return true;
        }
        
        return super.onTouchEvent(event);
    }

    private void changeProperties() {
        Transaction.beginImplicitAnimation();
        for (int i = 0; i < mActors.length; ++i) {
            mActors[i].setPosition(new Point(mRandom.nextFloat(), mRandom.nextFloat(), 0, true));
            mActors[i].setRotation(new Rotation(0, 0, mRandom.nextFloat() * 360));
        }
        Transaction.commit();
    }
}
