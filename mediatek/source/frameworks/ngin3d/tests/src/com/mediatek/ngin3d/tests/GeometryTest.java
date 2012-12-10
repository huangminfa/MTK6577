package com.mediatek.ngin3d.tests;


import com.mediatek.ngin3d.Geometry;

public class GeometryTest extends Ngin3dTest {

    public void testGeometry() {
        Geometry geometry = new Geometry(1, 1, 1, 1);
        Geometry geometry2 = new Geometry(1, 1, 1, 1);
        Geometry geometryNoParam = new Geometry();

        assertFalse(geometry.equals(geometryNoParam));
        assertEquals(geometry2.hashCode(), geometry.hashCode());

    }
}
