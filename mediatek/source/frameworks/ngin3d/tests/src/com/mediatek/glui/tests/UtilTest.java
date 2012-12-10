package com.mediatek.glui.tests;

import com.mediatek.glui.Util;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.tests.Ngin3dTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UtilTest extends Ngin3dTest {

    public static void testUtil() {
        assertThat(Util.clamp(3, 0, 2), is(2));
        assertTrue(Util.isPowerOf2(4));

        Container c1 = new Container();
        Container c2 = new Container();
        assertFalse(Util.equals(c1, c2));

        assertThat(Util.nextPowerOf2(8), is(8));
    }
}
