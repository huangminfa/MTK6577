package com.mediatek.ngin3d.tests;

import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Dimension;
import junit.framework.TestCase;

public class BoxDimensionTest extends TestCase {
    public void testDimension() {
        Dimension d = new Dimension(100f, 100f);
        assertTrue(d.equals(100f, 100f));

        assertTrue(Dimension.isValidSize(100f));
    }

    public void testBox() {
        Box b = new Box(1, 1, 1, 1);
        Box b1 = new Box(1, 1, 1, 1);

        assertEquals(b.hashCode(), b1.hashCode());
    }
}
