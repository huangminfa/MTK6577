package com.mediatek.world3d;

import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Point;

public interface RotateItem {
    public void onRotate();
    public void onClick(Point point);
    public void onFocus();
    public void onDefocus();
    public void onIdle();
    public Actor getTitle();
}