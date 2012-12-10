package com.mediatek.ngin3d.tests;


import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Canvas2d;

public class Canvas2dTest extends Ngin3dTest {

    public void testDirtyRect() {
        Box box = new Box(1f, 1f, 1f, 1f);
        Canvas2d canvas2d = new Canvas2d();
        canvas2d.setDirtyRect(box);
        assertEquals(box, canvas2d.getDirtyRect());
    }

}
