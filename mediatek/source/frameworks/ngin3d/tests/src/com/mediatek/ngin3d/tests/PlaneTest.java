package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.presentation.ImageDisplay;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Add description here.
 */
public class PlaneTest extends Ngin3dTest {

    @SmallTest
    public void testColor() {
        Plane plane = new Plane<ImageDisplay>();

        Color color = new Color(128, 128, 128);
        plane.setColor(color);
        assertEquals(color, plane.getColor());

        plane.setMaterialType(10);
        assertThat(plane.getMaterialType(), is(10));

        Dimension dimension = new Dimension(100, 200);
        plane.setSize(dimension);
        assertEquals(dimension, plane.getSize());
    }

}
