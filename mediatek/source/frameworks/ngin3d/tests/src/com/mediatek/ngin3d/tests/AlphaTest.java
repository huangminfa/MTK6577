package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.animation.Alpha;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.Timeline;
import junit.framework.TestCase;

public class AlphaTest extends TestCase {

    @SmallTest
    public void testDefaultValue() {
        Alpha alpha = new Alpha();
        assertEquals(0.0f, alpha.getAlpha());
        assertEquals(Mode.LINEAR, alpha.getMode());

        Timeline timeline1 = new Timeline(2000);
        alpha.setTimeline(timeline1);
        assertEquals(timeline1, alpha.getTimeline());

        Timeline timeline2 = new Timeline(2000);
        alpha.setTimeline(timeline2);
        assertEquals(timeline2, alpha.getTimeline());
    }

    public void testAlphaMode() {
        Timeline timeline = new Timeline(2000);
        Alpha alpha = new Alpha(timeline, Mode.LINEAR);
        float delta = 0.001f;
        float p = 0;
        timeline.setProgress(p);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_CUBIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_CUBIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUART);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUART);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_SINE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_SINE);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_SINE);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_EXPO);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_OUT_EXPO);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_EXPO);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_CIRC);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_OUT_CIRC);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_CIRC);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_ELASTIC);
        assertEquals(alpha.getAlpha(), p, delta);

        alpha.setMode(Mode.EASE_OUT_ELASTIC);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_ELASTIC);
        assertEquals(alpha.getAlpha(), p, delta);

        alpha.setMode(Mode.EASE_IN_BACK);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_OUT_BACK);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_BACK);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_BOUNCE);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_OUT_BOUNCE);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        alpha.setMode(Mode.EASE_IN_OUT_BOUNCE);
        assertEquals(Math.abs(alpha.getAlpha()), p);

        p = 1.0f;
        timeline.setProgress(p);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUAD);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_CUBIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_CUBIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUART);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUART);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_QUINT);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_SINE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_SINE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_SINE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_EXPO);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_EXPO);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_EXPO);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_CIRC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_CIRC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_CIRC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_ELASTIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_ELASTIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_ELASTIC);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_BACK);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_BACK);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_BACK);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_BOUNCE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_OUT_BOUNCE);
        assertEquals(alpha.getAlpha(), p);

        alpha.setMode(Mode.EASE_IN_OUT_BOUNCE);
        assertEquals(alpha.getAlpha(), p);

    }


}
