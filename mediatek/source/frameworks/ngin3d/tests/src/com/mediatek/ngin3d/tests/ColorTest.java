package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Color;
import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

/**
 * Add description here.
 */
public class ColorTest extends TestCase {

    @SmallTest
    public void testDefaultValue() {
        Color c = new Color(0, 1, 2);
        assertEquals(255, c.alpha);
        assertEquals(0, c.red);
        assertEquals(1, c.green);
        assertEquals(2, c.blue);

        Color c2 = c.copy();
        assertEquals(c, c2);
    }

    public void testColorSetting() {
        Color c = new Color(0, 1, 2, 0);
        Color tmpColor = c.red(1);
        assertThat(tmpColor.red, is(1));
        tmpColor = c.green(0);
        assertThat(tmpColor.green, is(0));
        tmpColor = c.blue(1);
        assertThat(tmpColor.blue, is(1));
        tmpColor = c.alpha(1);
        assertThat(tmpColor.alpha, is(1));
    }

    public void testLight() {
        Color c = new Color(1, 1, 1);
        Color tmpColor = c.brighter();
        assertThat(tmpColor.red, greaterThan(1));
        assertThat(tmpColor.green, greaterThan(1));
        assertThat(tmpColor.blue, greaterThan(1));

        Color c1 = new Color(100, 100, 100);
        tmpColor = c1.darker();
        assertThat(tmpColor.red, lessThan(100));
        assertThat(tmpColor.green, lessThan(100));
        assertThat(tmpColor.blue, lessThan(100));
    }

    public void testHLS() {
        Color c = new Color(1, 1, 1);
        c.setHls(0f, 0.1f, 0f);
        assertThat(c.red, is(26));

        c.setHls(1.2f, 0.5f, 0.8f);
        float h = (1.2f - (float) Math.floor(1.2f)) * 6.0f;
        float f = h - (float) Math.floor(h);
        float p = 0.5f * (1.0f - 0.8f);
        float q = 0.5f * (1.0f - 0.8f * f);
        float r = (int) (q * 255.0f + 0.5f);
        float g = (int) (0.5f * 255.0f + 0.5f);
        float b = (int) (p * 255.0f + 0.5f);

        assertThat(c.red, is((int)r));
        assertThat(c.green, is((int)g));
        assertThat(c.blue, is((int)b));
    }

}
