package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Point;
import junit.framework.TestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Add description here.
 */
public class PointTest extends TestCase {

    @SmallTest
    public void testEquals() {
        Point a = new Point(1, 1);
        Point b = new Point(1, 1, 0);
        Point c = new Point(1, 1, 1);
        assertThat(a, is(equalTo(b)));
        assertThat(b, is(equalTo(a)));
        assertThat(a, not(equalTo(c)));
        assertThat(c, not(equalTo(a)));
        assertThat(b, not(equalTo(c)));
        assertThat(c, not(equalTo(a)));
    }



    public void testPoint() {
        Point p = new Point(true);
        Point p1 = new Point(10f, 10f, 10f, true);
        p.set(10f, 10f, 10f);

        assertTrue(p.equals(p1));
    }


}
