package com.mediatek.ngin3d.tests;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.TextureAtlas;

/**
 * Add description here.
 */
public class TextureAtlasTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    public TextureAtlasTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    @SmallTest
    public void testLoading() {
        final PresentationStubActivity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextureAtlas.getDefault().add(activity.getResources(), 0, 0);
                    fail("Should throw exception.");
                } catch (Resources.NotFoundException e) {
                    // expected
                }
            }
        });
        getInstrumentation().waitForIdleSync();
    }
}
