package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.animation.MasterClock;
import com.mediatek.ngin3d.animation.Timeline;
import junit.framework.TestCase;

public class MasterClockTest extends TestCase {

    @SmallTest
    public void testDefaultValues() {
        Double timeScale = MasterClock.getDefault().getTimeScale();
        assertEquals(1.0, timeScale);
    }

    @SmallTest
    public void testTimeScale() {
        try {
            MasterClock.getDefault().setTimeScale(-1.0);
            fail("Should throw exception when timescale is negative");
        } catch (IllegalArgumentException e) {
            // expected
        }

        MasterClock.getDefault().setTimeScale(2.0);
        assertEquals(2.0, MasterClock.getDefault().getTimeScale());
    }

    @SmallTest
    public void testRegister() {
        Timeline timeline = new Timeline(1000);
        MasterClock.register(timeline);
        MasterClock.unregister(timeline);
        MasterClock.register(timeline);
        MasterClock.getDefault().tick();
        assertTrue("Timeline should be registered", MasterClock.getDefault().isTimelineRegistered(timeline));

        MasterClock.unregister(timeline);
        MasterClock.register(timeline);
        MasterClock.unregister(timeline);
        MasterClock.getDefault().tick();
        assertFalse("Timeline should already be unregistered", MasterClock.getDefault().isTimelineRegistered(timeline));

        MasterClock.register(timeline);
        MasterClock.unregister(timeline);
        MasterClock.register(timeline);
        MasterClock.getDefault().tick();
        MasterClock.cleanup();
        assertFalse("Timeline should be cleaned", MasterClock.getDefault().isTimelineRegistered(timeline));
    }
}
