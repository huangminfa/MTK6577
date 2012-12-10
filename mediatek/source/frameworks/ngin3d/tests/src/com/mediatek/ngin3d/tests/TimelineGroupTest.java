package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.animation.Timeline;
import com.mediatek.ngin3d.animation.TimelineGroup;
import junit.framework.TestCase;

/**
 * Add description here.
 */
public class TimelineGroupTest extends TestCase {

        private class TimelineListenerTest implements Timeline.Listener {
            public boolean mCompleted = false;

            public void onStarted(Timeline timeline) {
            }
            public void onNewFrame(Timeline timeline, int elapsedMsecs) {
            }
            public void onMarkerReached(Timeline timeline, int elapsedMsecs, String marker, int direction) {
            }
            public void onPaused(Timeline timeline) {
            }
            public void onCompleted(Timeline timeline) {
                mCompleted = true;
            }
            public void onLooped(Timeline timeline) {
            }
        }

    @SmallTest
    public void testAutoStop() {
        TimelineGroup group = new TimelineGroup(3000);
        Timeline t1 = new Timeline(1000);
        Timeline t2 = new Timeline(2000);

        TimelineListenerTest listenerTest = new TimelineListenerTest();
        group.addListener(listenerTest);

        group.attach(t1);
        group.attach(t2);

        group.start();
        t1.start();
        t2.start();

        group.doTick(0);
        group.doTick(500);
        group.doTick(1000);
        assertFalse(listenerTest.mCompleted);
        assertFalse("Animation t1 should stop on its end time", t1.isStarted());
        group.doTick(2000);
        assertFalse("Animation t2 should stop on its end time", t2.isStarted());
        assertFalse("TimelineGroup should stop automatically", group.isStarted());
        assertTrue("TimelineGroup should be empty", group.isEmpty());
        assertTrue(listenerTest.mCompleted);
    }
}
