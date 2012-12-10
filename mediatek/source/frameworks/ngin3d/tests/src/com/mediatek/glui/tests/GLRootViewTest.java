package com.mediatek.glui.tests;

import android.graphics.Matrix;
import android.test.ActivityInstrumentationTestCase2;
import static android.test.MoreAsserts.assertNotEqual;
import android.util.DisplayMetrics;
import android.view.animation.Transformation;
import com.mediatek.glui.GLRootView;
import com.mediatek.glui.GLView;
import com.mediatek.ngin3d.tests.PresentationStubActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GLRootViewTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {
    public GLRootViewTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    private PresentationStubActivity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testTransformation() {
        GLRootView glRootView = new GLRootView(mActivity);
        assertEquals(glRootView.pushTransform(), glRootView.getTransformation());
        glRootView.popTransform();
        Transformation t1= glRootView.getTransformation();
        Transformation t2 = glRootView.obtainTransformation();
        Matrix f1 = t1.getMatrix();
        Matrix f2 = t2.getMatrix();
        float a1 = t1.getAlpha();
        float a2 = t2.getAlpha();
        assertEquals(f1, f2);
        assertEquals(a1, a2);
        assertNotEqual(t1, t2);
    }

    public void testDpToPixel() {
        GLRootView glRootView = new GLRootView(mActivity);
         DisplayMetrics metrics = new DisplayMetrics();
            mActivity.getWindowManager()
            .getDefaultDisplay().getMetrics(metrics);
        float f = metrics.density;

        f = f*2f+0.5f;
        assertThat(GLRootView.dpToPixel(mActivity, 2), is((int)f));
    }

    public void testContentView() {
        GLRootView glRootView = new GLRootView(mActivity);
        GLView content = new GLView();
        glRootView.setContentPane(content);
        assertEquals(content, glRootView.getContentPane());
    }

}
