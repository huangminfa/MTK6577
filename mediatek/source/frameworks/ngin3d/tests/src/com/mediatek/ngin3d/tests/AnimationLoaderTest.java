package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import com.mediatek.ngin3d.animation.Animation;
import com.mediatek.ngin3d.animation.AnimationLoader;

public class AnimationLoaderTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {
    public AnimationLoaderTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    @MediumTest
    @UiThreadTest
    public void testAnimationLoading() {
        final PresentationStubActivity activity = getActivity();
        AnimationLoader.setCacheDir(activity.getCacheDir());

        Animation ani;

        ani = AnimationLoader.loadAnimation(activity, R.raw.photo_next_enter_photo1_ani);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.photo_next_enter_photo2_ani);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.photo_next_enter_photo3_ani);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.photo_next_enter_photo4_ani);
        assertNotNull(ani);

        ani = AnimationLoader.loadAnimation(activity, R.raw.photo_last_enter_photo1_ani, false);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.building_last_enter_building_ani, false);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.building_next_enter_weather_ani, false);
        assertNotNull(ani);
        ani = AnimationLoader.loadAnimation(activity, R.raw.home_fly_away, false);
        assertNotNull(ani);
    }
}
