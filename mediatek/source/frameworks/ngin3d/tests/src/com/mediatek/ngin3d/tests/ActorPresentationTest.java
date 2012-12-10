package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Empty;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.android.StageView;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.Ngin3d;

public class ActorPresentationTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    public ActorPresentationTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    private StageView mStageView;
    protected PresentationEngine mPresentationEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mStageView = getActivity().getStageView();
        mStageView.waitSurfaceReady();
        mPresentationEngine = mStageView.getPresentationEngine();
    }

    public void testActorPresentation() {
        Actor actor = new Empty();
        actor.realize(mPresentationEngine);

        // Because the A3M presentation engine always returns an axis-angle as
        // the value of PROP_ROTATION, whereas the Jirr presentation engine
        // always returns Euler angles (on account of the native
        // representations of rotations differing)
        Rotation expectedRotation = new Rotation(0.0f, 1.0f, 0.0f, 0.0f);
        assertEquals(expectedRotation,
            actor.getPresentationValue(Actor.PROP_ROTATION));

        assertEquals(Actor.PROP_POSITION.defaultValue(), actor.getPresentationValue(Actor.PROP_POSITION));
        assertEquals(Actor.PROP_SCALE.defaultValue(), actor.getPresentationValue(Actor.PROP_SCALE));
        assertEquals(Actor.PROP_VISIBLE.defaultValue(), actor.getPresentationValue(Actor.PROP_VISIBLE));
        assertEquals(Actor.PROP_ANCHOR_POINT.defaultValue(), actor.getPresentationValue(Actor.PROP_ANCHOR_POINT));
        actor.unrealize();
    }
}
