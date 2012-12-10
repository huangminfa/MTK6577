package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Empty;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.TextureAtlas;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class StageTest extends Ngin3dTest {

    @SmallTest
    public void testDefaultStage() {
        assertNotNull(mStage);
        assertEquals(0, mStage.getChildrenCount());
    }

    @SmallTest
    public void testStageProperties() {
        mStage.setName("stage");
        assertEquals("stage", mStage.getName());

        // Check projection mode parameters pass through OK
        mStage.setProjection(0, 1.0f, 2.0f, 3.0f);
        Stage.ProjectionConfig config = (Stage.ProjectionConfig) mStage.getProjection();
        assertEquals(0, config.mode);
        assertEquals(1.f, config.zNear);
        assertEquals(2.f, config.zFar);
        assertEquals(3.f, config.zStage);

        // Check legal mode range 0-2
        mStage.setProjection(1, 1.0f, 2.0f, 3.0f);
        config = (Stage.ProjectionConfig) mStage.getProjection();
        assertEquals(1, config.mode);
        mStage.setProjection(2, 1.0f, 2.0f, 3.0f);
        config = (Stage.ProjectionConfig) mStage.getProjection();
        assertEquals(2, config.mode);

        Color bkgColor = new Color(1, 1, 1);
        mStage.setBackgroundColor(bkgColor);
        assertThat(mStage.getBackgroundColor(), is(equalTo(bkgColor)));

        mStage.applyChanges(mPresentationEngine);
    }

    @SmallTest
    public void testRealize() {
        mStage.realize(mPresentationEngine);
        mStage.unrealize();
        mStage.realize(mPresentationEngine);
    }

    @SmallTest
    public void testAddingActor() {
        for (int i = 0; i < 10; i++) {
            mStage.add(new Empty());
        }
        assertEquals(10, mStage.getChildrenCount());
        mStage.realize(mPresentationEngine);
    }

    @SmallTest
    public void testCamera() {
        Point position = new Point(0, 0, 2);
        Point lookAt = new Point(1, 1, 1);
        mStage.setCamera(position, lookAt);
        Stage.Camera camera = mStage.getCamera();
        assertThat(camera.position, is(equalTo(position)));
        assertThat(camera.lookAt, is(equalTo(lookAt)));
    }

    public void testAddTextureAtlas() {
        mStage.addTextureAtlas(getInstrumentation().getContext().getResources(), R.raw.media3d_altas, R.raw.media3d);
        TextureAtlas.getDefault().cleanup();
        assertTrue(TextureAtlas.getDefault().isEmpty());
    }

    public void testFPS() {
        mStage.setMaxFPS(10);
        assertThat(mStage.getMaxFPS(), is(10));
    }

    public void testSize() {
        assertThat(mStage.getHeight(), is(800));
        assertThat(mStage.getWidth(), is(480));
    }

    public void testInnerClass() {
        float[] f = {0f, 0f, 0f};

        Stage.ProjectionConfig con1 = new Stage.ProjectionConfig(2, f[0], f[1], f[2]);
        Stage.ProjectionConfig con2 = new Stage.ProjectionConfig(2, f[0], f[1], f[2]);
        assertTrue(con1.equals(con2));
    }

}
