package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Dimension;
import junit.framework.TestCase;

public class ActorBoxTest extends TestCase {
    Box mBox;

    @SmallTest
    public void testActorBoxDimension() {
        float x1 = 1.0f;
        float x2 = 0.0f;
        float y1 = 1.0f;
        float y2 = 0.0f;
        float width, height;

        try {
            mBox = new Box(x1, y1, x2, y2);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
        }

        x2 = 2.0f;
        y2 = 3.0f;
        width = x2 - x1;
        height = y2 - y1;
        mBox = new Box(x1, y1, x2, y2);
        assertEquals(mBox.getWidth(), width);
        assertEquals(mBox.getHeight(), height);
        Dimension d1 = new Dimension(width, height);
        Dimension d2 = mBox.getSize();
        assertEquals(d1, d2);
    }
}
