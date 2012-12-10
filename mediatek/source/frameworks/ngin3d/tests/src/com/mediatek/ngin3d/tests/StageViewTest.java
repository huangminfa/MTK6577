package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.animation.PropertyAnimation;

import static android.test.MoreAsserts.assertNotEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class StageViewTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    protected Stage mStage;
    private StageView mStageView;

    public StageViewTest() {
        super(PresentationStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mStage = getActivity().getStageView().getStage();
        mStageView = getActivity().getStageView();
        mStageView.waitSurfaceReady();
    }

    @SmallTest
    public void testScreenShot() {
        assertNotNull(mStageView.getScreenShot());
    }

    @SmallTest
    public void testRendering() throws InterruptedException {

        Integer start = new Integer(0);
        Integer end = new Integer(255);
        Plane plane = new Plane();
        getActivity().getStage().add(plane);
        PropertyAnimation ani = new PropertyAnimation(plane, "opacity", start, end);
        ani.start();
        mStageView.pauseRendering();
        Thread.sleep(500);
        float p1 = ani.getProgress();
        Thread.sleep(500);
        float p2 = ani.getProgress();
        assertEquals(p1, p2);
        assertTrue(ani.isStarted());
        assertEquals(mStageView.getRenderMode(), StageView.RENDERMODE_WHEN_DIRTY);

        mStageView.resumeRendering();
        assertTrue(ani.isStarted());
        Thread.sleep(1000);

        float p3 = ani.getProgress();
        assertNotEqual(p2, p3);
    }

    public void testFrameInterrval() {
        mStageView.setRenderMode(StageView.RENDERMODE_CONTINUOUSLY);
        mStage.getFrameInterval();
        assertThat(mStage.getFrameInterval(), greaterThan(0));
    }

}
