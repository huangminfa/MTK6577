package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.Canvas3d;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.presentation.PresentationEngine;

public class Canvas3dTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    public Canvas3dTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    protected StageView mStageView;
    protected PresentationEngine mPresentationEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mStageView = getActivity().getStageView();
        mStageView.waitSurfaceReady();
        mPresentationEngine = mStageView.getPresentationEngine();
    }

    public void testCreatePresentation() {
        Canvas3d canvas3d = new Canvas3d();
        canvas3d.realize(mPresentationEngine);

        // Because the A3M presentation engine always returns an axis-angle as
        // the value of PROP_ROTATION, whereas the Jirr presentation engine
        // always returns Euler angles (on account of the native
        // representations of rotations differing)
        Rotation expectedRotation = new Rotation(0.0f, 1.0f, 0.0f, 0.0f);
        assertEquals(expectedRotation,
            canvas3d.getPresentationValue(Canvas3d.PROP_ROTATION));
    }
}
