package com.mediatek.ngin3d.tests;

import android.test.InstrumentationTestCase;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.presentation.PresentationEngine;

public class Ngin3dTest extends InstrumentationTestCase {

    protected Stage mStage;
    protected PresentationEngine mPresentationEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mStage = new Stage();
        mPresentationEngine = Ngin3d.createPresentationEngine(mStage);

        mPresentationEngine.initialize(480, 800,
                getInstrumentation().getContext().getResources());
    }

    @Override
    protected void tearDown() throws Exception {
        mPresentationEngine.uninitialize();

        mPresentationEngine = null;
        mStage = null;

        super.tearDown();
    }
}
