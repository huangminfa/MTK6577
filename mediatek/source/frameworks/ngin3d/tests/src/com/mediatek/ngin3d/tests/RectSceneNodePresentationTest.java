package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.a3m.A3mPresentationEngine;
import com.mediatek.ngin3d.a3m.RectSceneNodePresentation;
import com.mediatek.ngin3d.presentation.PresentationEngine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RectSceneNodePresentationTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {
    public RectSceneNodePresentationTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    private PresentationEngine mPE;

    @Override
    protected void setUp() throws Exception {
       super.setUp();
        mPE = Ngin3d.createPresentationEngine(new Stage());
        mPE.initialize(480, 800);
    }

    public void testRectSceneNode() {
        RectSceneNodePresentation rectSceneNodePresentation =
            new RectSceneNodePresentation((A3mPresentationEngine) mPE);
        rectSceneNodePresentation.initialize(null);

        rectSceneNodePresentation.setRepeat(2, 2);
        assertThat(rectSceneNodePresentation.getRepeatX(), is(2));
        assertThat(rectSceneNodePresentation.getRepeatY(), is(2));

        rectSceneNodePresentation.setKeepAspectRatio(true);
        assertTrue(rectSceneNodePresentation.isKeepAspectRatio());

        rectSceneNodePresentation.setFilterQuality(1);
        assertThat(rectSceneNodePresentation.getFilterQuality(), is(1));

    }

}
