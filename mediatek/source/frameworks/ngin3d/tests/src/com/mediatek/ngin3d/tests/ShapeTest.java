package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Shape;

public class ShapeTest extends Ngin3dTest {

    @SmallTest
    public void testShapeProperties() {
        Shape shape = new Shape();

        shape.dumpProperties();
    }

    @SmallTest
    public void testTextPresentation() {
        Shape shape = new Shape();
        mStage.add(shape);

        mStage.realize(mPresentationEngine);
    }
}
