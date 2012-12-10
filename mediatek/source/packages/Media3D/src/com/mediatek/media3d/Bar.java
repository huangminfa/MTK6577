package com.mediatek.media3d;

import android.util.Log;
import android.view.MotionEvent;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Point;

public abstract class Bar extends Container {
    public boolean onTouchEvent(MotionEvent event) {
        if (Media3D.DEBUG) {
            Log.v(TAG, "onTouchEvent, action = " + event.getAction() + "(x, y) = (" + event.getX() + " ," + event.getY() + ")");
        }
        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_MOVE) {
            return false;
        }
        Actor hit = hitTest(new Point(event.getX(), event.getY()));
        if (hit == null) {
            onHitNothing();
            return false;
        }

        return onHit(hit, event.getAction());
    }

    protected abstract boolean onHit(Actor hit, int action);
    protected abstract boolean onHitNothing();
}