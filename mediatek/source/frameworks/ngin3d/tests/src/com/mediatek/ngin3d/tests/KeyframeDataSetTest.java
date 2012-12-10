package com.mediatek.ngin3d.tests;

import com.mediatek.ngin3d.animation.KeyframeData;
import com.mediatek.ngin3d.animation.KeyframeDataSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class KeyframeDataSetTest extends Ngin3dTest{

    public void testKeyFrame() {
        KeyframeDataSet keyframeDataSet = new KeyframeDataSet();
        keyframeDataSet.setAnchor(1f, 1f);
        assertThat(keyframeDataSet.getAnchor().x, is(1f));
        assertThat(keyframeDataSet.getAnchor().y, is(1f));

        keyframeDataSet.setOpacity(10);
        assertThat(keyframeDataSet.getOpacity(), is(10));

        keyframeDataSet.setPosition(1f, 1f, 1f);
        assertThat(keyframeDataSet.getPosition().x, is(1f));
        assertThat(keyframeDataSet.getPosition().y, is(1f));
        assertThat(keyframeDataSet.getPosition().z, is(1f));

        keyframeDataSet.setRotation(1f, 1f, 1f);
        assertThat(keyframeDataSet.getRotation().x, is(1f));
        assertThat(keyframeDataSet.getRotation().y, is(1f));
        assertThat(keyframeDataSet.getRotation().z, is(1f));

        keyframeDataSet.setScale(1f, 1f);
        assertThat(keyframeDataSet.getScale().x, is(1f));
        assertThat(keyframeDataSet.getScale().y, is(1f));
    }

    public void testKeyFrameData() {
        KeyframeData keyframeData = new KeyframeData(10, 1, null);
        assertThat(keyframeData.getDelay(), is(1));
        assertThat(keyframeData.getDuration(), is(10));
    }
}
