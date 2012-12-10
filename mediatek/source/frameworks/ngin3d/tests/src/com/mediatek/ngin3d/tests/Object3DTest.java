package com.mediatek.ngin3d.tests;

import android.opengl.GLSurfaceView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.utils.Ngin3dException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class Object3DTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    protected Stage mStage;
    private StageView mStageView;
    protected PresentationEngine mPresentationEngine;

    public Object3DTest() {
        super(PresentationStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mStageView = getActivity().getStageView();
        mStageView.waitSurfaceReady();
        mPresentationEngine = mStageView.getPresentationEngine();
    }

    @SmallTest
    public void testObject3DAnimation() throws InterruptedException, ExecutionException {
        final Glo3D landscape = Glo3D.createFromAsset("landscape.glo");
        final Glo3D tree_bend_gail = Glo3D.createFromAsset("tree_bend_gail.glo");

        getActivity().getStage().add(landscape);
        getActivity().getStage().add(tree_bend_gail);

        BasicAnimation treeGail = tree_bend_gail.getAnimation();
        assertThat(treeGail.getDuration(), is(0));
        assertEquals(tree_bend_gail, treeGail.getTarget());

        FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
            public Boolean call() {
                landscape.realize(mPresentationEngine);
                tree_bend_gail.realize(mPresentationEngine);
                return true;
            }
        });

        mStageView.runInGLThread(task);
        task.get().booleanValue();

        treeGail = tree_bend_gail.getAnimation();
        assertThat(treeGail.getDuration(), is(greaterThan(0)));

        treeGail.start();
        Thread.sleep(treeGail.getDuration() / 2);
        assertTrue(treeGail.isStarted());

        treeGail.waitForCompletion();
        assertFalse(treeGail.isStarted());

        try {
            treeGail.setTarget(landscape);
            fail("Should throw exception because Object3DAnimation can not change target.");
        } catch (Ngin3dException e) {
            // expected
        }
        assertEquals(tree_bend_gail, treeGail.getTarget());

        treeGail.setLoop(true);

        treeGail.start();
        Thread.sleep(treeGail.getDuration() * 2);
        assertTrue(treeGail.isStarted());

        treeGail.stop();
        assertFalse(treeGail.isStarted());
    }
}
