package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Empty;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Transaction;
import com.mediatek.ngin3d.presentation.PresentationEngine;

public class TransactionTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    public TransactionTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity().getStageView().waitSurfaceReady();
    }

    @SmallTest
    @UiThreadTest
    public void testPropertyValueIsSetInTransaction() {
        Stage stage = getActivity().getStage();
        PresentationEngine pe = getActivity().getStageView().getPresentationEngine();
        pe.pauseRendering();

        Empty empty = new Empty();
        stage.add();
        Transaction.beginPropertiesModification();
        empty.setVisible(true);
        Transaction.commit();

        // It should still be visible for client
        assertEquals(empty.getVisible(), true);
    }
}
