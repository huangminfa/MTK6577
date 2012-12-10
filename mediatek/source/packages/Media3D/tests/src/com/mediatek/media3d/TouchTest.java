package com.mediatek.media3d;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w -e class com.mediatek.media3d.TouchTest com.mediatek.media3d.tests/android.test.InstrumentationTestRunner
 */

public class TouchTest extends ActivityInstrumentationTestCase2<Main> {
    private Solo solo;

    private static final int X_CENTER = 400;
    private static final int Y_CENTER = 240;

    private static final int X_WEATHER = 150;
    private static final int X_PHOTO = 400;
    private static final int X_VIDEO = 650;

    private static final int X_BAR_OUT = 702;
    private static final int Y_BAR_OUT = 400;

    private static final int Y_TOOLBAR = 50;
    private static final int X_TOOLBAR_HOME = 50;
    private static final int X_TOOLBAR_MENU2 = 653;
    private static final int X_TOOLBAR_MENU1 = 751;

    private static final int Y_NVBAR = 430;
    private static final int X_NVBAR_WEATHER = 302;
    private static final int X_NVBAR_PHOTO = 400;
    private static final int X_NVBAR_VIDEO = 498;

    public TouchTest() {
        super("com.mediatek.media3d", Main.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        //Robotium will finish all the activities that have been opened
        solo.finishOpenedActivities();
    }

    private void enterPortalPage(Main activity) {
        CommonTestUtil.waitPageForIdleSync(getInstrumentation(), activity.getMedia3DView(), activity.getPortalPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);

        CommonTestUtil.waitLoadForIdleSync(getInstrumentation(), activity.getWeatherPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        CommonTestUtil.waitLoadForIdleSync(getInstrumentation(), activity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        CommonTestUtil.waitLoadForIdleSync(getInstrumentation(), activity.getVideoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);

        assertTrue(activity.getMedia3DView().getCurrentPage() == activity.getPortalPage());
    }

    public void test01WeatherPageTouch() {
        final Main activity = getActivity();
        enterPortalPage(activity);
        solo.clickOnScreen(X_WEATHER, Y_CENTER);
        solo.sleep(5000);

        getInstrumentation().waitForIdleSync();
        Media3DView m3d = activity.getMedia3DView();
        assertTrue(m3d.getCurrentPage() == activity.getWeatherPage());
        solo.sleep(3000);

        // wait time out back
        //test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        // test if the bar is back
        solo.sleep(4000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // tap back on bar
        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // tap back not on bar
        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_CENTER, Y_CENTER);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // tap home button on tool bar
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        solo.clickOnScreen(X_TOOLBAR_HOME, Y_TOOLBAR);
        solo.sleep(1500);
        assertTrue(m3d.getCurrentPage() == activity.getPortalPage());
    }

    public void test02PhotoPageTouch() {
        final Main activity = getActivity();
        enterPortalPage(activity);
        solo.clickOnScreen(X_PHOTO, Y_CENTER);
        solo.sleep(5000);

        getInstrumentation().waitForIdleSync();
        Media3DView m3d = activity.getMedia3DView();
        assertTrue(m3d.getCurrentPage() == activity.getPhotoPage());
        solo.sleep(3000);

        //test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // test if the bar is back
        solo.sleep(4000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());


        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_CENTER, Y_CENTER);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // tap home button on tool bar
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        solo.clickOnScreen(X_TOOLBAR_HOME, Y_TOOLBAR);
        solo.sleep(1500);
        assertTrue(m3d.getCurrentPage() == activity.getPortalPage());
    }

    public void test03VideoPageTouch() {
        final Main activity = getActivity();
        enterPortalPage(activity);
        solo.clickOnScreen(X_VIDEO, Y_CENTER);
        solo.sleep(5000);

        getInstrumentation().waitForIdleSync();
        Media3DView m3d = activity.getMedia3DView();
        assertTrue(m3d.getCurrentPage() == activity.getVideoPage());
        solo.sleep(3000);

        //test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // test if the bar is back
        solo.sleep(4000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(2000);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());


        // test tap bar out/in
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());
        solo.clickOnScreen(X_CENTER, Y_CENTER);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_HIDDEN, m3d.getBarState());

        // tap home button on tool bar
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(1500);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        solo.clickOnScreen(X_TOOLBAR_HOME, Y_TOOLBAR);
        solo.sleep(1500);
        assertTrue(m3d.getCurrentPage() == activity.getPortalPage());
    }

    public void test04NavigationBar() {
        int PAGE_SWITCH_DELAY_SLEEP = 5000;
        int BAR_DELAY_SLEEP = 3000;

        final Main activity = getActivity();
        enterPortalPage(activity);
        solo.clickOnScreen(X_WEATHER, Y_CENTER);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);

        getInstrumentation().waitForIdleSync();
        Media3DView m3d = activity.getMedia3DView();
        assertTrue(m3d.getCurrentPage() == activity.getWeatherPage());
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // weather -> weather
        solo.clickOnScreen(X_NVBAR_WEATHER, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getWeatherPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // weather -> photo
        solo.clickOnScreen(X_NVBAR_PHOTO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getPhotoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // photo -> photo
        solo.clickOnScreen(X_NVBAR_PHOTO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getPhotoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // photo -> video
        solo.clickOnScreen(X_NVBAR_VIDEO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getVideoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // video -> video
        solo.clickOnScreen(X_NVBAR_VIDEO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getVideoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // video -> weather
        solo.clickOnScreen(X_NVBAR_WEATHER, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getWeatherPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // weather -> video
        solo.clickOnScreen(X_NVBAR_VIDEO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getVideoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // video -> photo
        solo.clickOnScreen(X_NVBAR_PHOTO, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getPhotoPage());

        // test if the bar is out
        solo.clickOnScreen(X_BAR_OUT, Y_BAR_OUT);
        solo.sleep(BAR_DELAY_SLEEP);
        assertEquals(Media3DView.BAR_STATE_ENTERED, m3d.getBarState());

        // photo -> weather
        solo.clickOnScreen(X_NVBAR_WEATHER, Y_NVBAR);
        solo.sleep(PAGE_SWITCH_DELAY_SLEEP);
        assertTrue(m3d.getCurrentPage() == activity.getWeatherPage());
    }
}
