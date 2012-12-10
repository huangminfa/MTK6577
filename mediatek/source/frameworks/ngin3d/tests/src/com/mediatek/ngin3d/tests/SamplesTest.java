package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.animation.Samples;
import junit.framework.TestCase;

public class SamplesTest extends TestCase {

    @SmallTest
    public void testSamples() {
        Samples samples = new Samples(Samples.TRANSLATE);
        assertEquals(Samples.TRANSLATE, samples.getType());
    }

    public void testSampleString() {
        Samples samples = new Samples(Samples.TRANSLATE);
        String[] s = {"test1", "test2"};
        Samples sample1 = samples.add("test", s);
        assertEquals(s, sample1.getString("test"));

        int[] i = {1, 2, 3};
        Samples sample2 = samples.add("test2", i);
        assertNull(sample2.remove("test2").getInt("test2"));
    }

}
