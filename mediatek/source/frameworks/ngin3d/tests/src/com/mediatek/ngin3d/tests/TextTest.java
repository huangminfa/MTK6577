package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Text;

public class TextTest extends Ngin3dTest {

    @SmallTest
    public void testTextProperties() {
        Text text = new Text("Hello!");
        assertEquals("Hello!", text.getText());

        text.setShadowLayer(2, 2, 2, Color.RED.getRgb());
        assertEquals(2.f, text.getShadowLayer().radius);
        assertEquals(2.f, text.getShadowLayer().dx);
        assertEquals(2.f, text.getShadowLayer().dy);
        assertEquals(Color.RED.getRgb(), text.getShadowLayer().color);
        text.setTextColor(Color.GREEN);
        assertEquals(Color.GREEN, text.getTextColor());
        text.setTextSize(32);
        assertEquals(32.f, text.getTextSize());

        text.dumpProperties();
    }

    @SmallTest
    public void testTextPresentation() {
        Text text = new Text();
        mStage.add(text);

        mStage.realize(mPresentationEngine);
    }

}
