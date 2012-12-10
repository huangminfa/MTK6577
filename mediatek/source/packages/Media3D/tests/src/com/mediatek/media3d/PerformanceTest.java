package com.mediatek.media3d;

import android.opengl.GLSurfaceView;
import com.mediatek.glui.GLRootView;
import com.mediatek.media3d.Media3D;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w -e class com.mediatek.media3d.PerformanceTest com.mediatek.media3d.tests/android.test.InstrumentationTestRunner
 */
public class PerformanceTest extends ActivityInstrumentationTestCase2<Main> {

    private static final String TAG = "M3dMark";

    public PerformanceTest() {
        super("com.mediatek.media3d", Main.class);
    }

    //
    // Test criteria
    //
    private static final long APP_LAUNCH_TIME_CRITERIA = 2000;
    private static final long PAGE_LAUNCH_TIME_CRITERIA = 2000;
    private static final long APP_PAUSE_TIME_CRITERIA = 2000;
    private static final long APP_RESUME_TIME_CRITERIA = 2000;
    private static final long APP_EXIT_TIME_CRITERIA = 1000;
    private static final float FPS_CRITERIA = 25.0f;
    private static final long APP_SHOWN_TIME_CRITERIA = 3000;

    private static final int NO_DIRECTION = 0;
    private static final int DRAG_RIGHT = 1;
    private static final int DRAG_LEFT = 2;
    private static final int DRAG_UP = 3;
    private static final int DRAG_DOWN = 4;
    private static final int MOTION_INTERVAL_LONG = 50;
    private static final int MOTION_INTERVAL_SHORT = 10;

    private static final int MOVE_DISTANCE = 200;       // 150 pixel.

    // util
    private void sendDragSync(int direction) {

        Instrumentation inst = getInstrumentation();

        int x = 400;  // 800 /2
        int y = 240;  // 480 /2

        int dx = 0;
        int dy = 0;
        if (direction == DRAG_RIGHT || direction == DRAG_LEFT) {
            dx = (direction == DRAG_RIGHT) ? MOVE_DISTANCE : -MOVE_DISTANCE;
        } else if (direction == DRAG_UP || direction == DRAG_DOWN) {
            dy = (direction == DRAG_UP) ? -MOVE_DISTANCE : MOVE_DISTANCE;
        }

        long t = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, x, y, 0));
        t += MOTION_INTERVAL_LONG;
        x += dx;
        y += dy;
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_MOVE, x, y, 0));
        t += MOTION_INTERVAL_SHORT;
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, x, y, 0));
    }

    private void sendTouchForFocusSync() {

        Instrumentation inst = getInstrumentation();

        int x = 400;  // 800 /2
        int y = 240;  // 480 /2

        int dx = 10;
        int dy = 0;
        long t = SystemClock.uptimeMillis();
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, x, y, 0));
        t += 500;
        x += dx;
        y += dy;
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_MOVE, x, y, 0));
        t += MOTION_INTERVAL_SHORT;
        inst.sendPointerSync(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, x, y, 0));
    }

    private static final int PHOTO_PAGE = 1;
    private static final int WEATHER_PAGE = 2;
    private static final int VIDEO_PAGE = 3;

    private void enterPage(Main activity, int type) {
        final Main finalActivity = activity;
        final int pageType = type;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Media3DView m3d = finalActivity.getMedia3DView();
                if (pageType == PHOTO_PAGE) {
                    m3d.enterPage(finalActivity.getPhotoPage());
                    assertThat(m3d.getCurrentPage(), sameInstance((Page) finalActivity.getPhotoPage()));
                } else if (pageType == WEATHER_PAGE) {
                    m3d.enterPage(finalActivity.getWeatherPage());
                    assertThat(m3d.getCurrentPage(), sameInstance((Page) finalActivity.getWeatherPage()));
                } else {
                    m3d.enterPage(finalActivity.getVideoPage());
                    assertThat(m3d.getCurrentPage(), sameInstance((Page) finalActivity.getVideoPage()));
                }
            }
        });
        getInstrumentation().waitForIdleSync();
        sendTouchForFocusSync();
    }

    private String getName(int type) {
        if (type == PHOTO_PAGE) {
            return "photo page";
        } else if (type == WEATHER_PAGE) {
            return "weather page";
        } else {
            return "video page";
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            // just end it.
        }
    }

    private long getPageLaunchTime(int pageType, Main activity) {
        sleep(5000);    // make sure page is entered properly.
        long t1 = SystemClock.uptimeMillis();
        enterPage(activity, pageType);
        return SystemClock.uptimeMillis() - t1;
    }

    private void testPauseResume(int pageType, Main activity) {
        enterPage(activity, VIDEO_PAGE);
        sleep(5000);    // make sure video page is entered properly.

        long t1 = SystemClock.uptimeMillis();
        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().waitForIdleSync();
        long t2 = SystemClock.uptimeMillis();
        long pauseTime = t2 - t1;

        getInstrumentation().callActivityOnResume(activity);
        getInstrumentation().waitForIdleSync();
        long t3 = SystemClock.uptimeMillis();
        long resumeTime = t3 - t2;

        writePerformanceData(activity, "app.pause-time.txt", pauseTime);
        writePerformanceData(activity, "app.resume-time.txt", resumeTime);
        assertThat(pauseTime, is(lessThanOrEqualTo(APP_PAUSE_TIME_CRITERIA)));
        assertThat(resumeTime, is(lessThanOrEqualTo(APP_RESUME_TIME_CRITERIA)));
    }

    private static final int MAX_TRIAL = 30;
    private static final int WAITING_INTERVAL = 1000;

    public void test00_FrameDrawTime() {
        long t1 = SystemClock.uptimeMillis();
        final Main activity = getActivity();
        getInstrumentation().waitForIdleSync();

        // sleep and polling , try MAX_TRIAL times .
        final Media3DView m3d = activity.getMedia3DView();
        long t3 = GLRootView.INVALID_TIME;
        int tried = 0;
        for (; tried < MAX_TRIAL; tried++) {
            t3 = m3d.getFirstOnDrawTime();
            if (t3 != GLRootView.INVALID_TIME) {
                break;
            }
            sleep(WAITING_INTERVAL);
        }
        assertThat(tried, is(lessThan(MAX_TRIAL)));
        t3 -= t1;
        Log.d(TAG, "test00_FrameDrawTime(): " + t3);
        writePerformanceData(activity, "app.shown-time.txt", t3);
        assertThat(t3, is(lessThanOrEqualTo(APP_SHOWN_TIME_CRITERIA)));
    }

    public void test01_AppLaunchTime() {
        long t1 = SystemClock.uptimeMillis();
        Log.v(TAG, "starting activity");
        final Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        long t2 = SystemClock.uptimeMillis() - t1;
        Log.v(TAG, "getActivity costs: " + t2);

        writePerformanceData(activity, "app.launch-time.txt", t2);
        assertThat(t2, is(lessThanOrEqualTo(APP_LAUNCH_TIME_CRITERIA)));
    }

    public void test02_AppLeaveTime() {
        final Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        sleep(5000);
        long t1 = SystemClock.uptimeMillis();
        sendKeys(KeyEvent.KEYCODE_BACK);
        getInstrumentation().waitForIdleSync();
        long t2 = SystemClock.uptimeMillis() - t1;

        writePerformanceData(activity, "app.leave-time.txt", t2);
        assertThat(t2, is(lessThanOrEqualTo(APP_EXIT_TIME_CRITERIA)));
    }

    public void test03_VideoPageLaunchTime() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        long launchTime = getPageLaunchTime(VIDEO_PAGE, activity);
        writePerformanceData(activity, "video.launch-time.txt", launchTime);
        assertThat(launchTime, is(lessThanOrEqualTo(PAGE_LAUNCH_TIME_CRITERIA)));
    }

    public void test04_VideoPagePauseResume() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        testPauseResume(VIDEO_PAGE, activity);
    }

    public void test05_PhotoPageLaunchTime() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        long launchTime = getPageLaunchTime(PHOTO_PAGE, activity);
        writePerformanceData(activity, "photo.launch-time.txt", launchTime);
        assertThat(launchTime, is(lessThanOrEqualTo(PAGE_LAUNCH_TIME_CRITERIA)));
    }

    public void test06_PhotoPagePauseResume() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        testPauseResume(PHOTO_PAGE, activity);
    }

    public void test07_WeatherPageLaunchTime() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        long launchTime = getPageLaunchTime(WEATHER_PAGE, activity);
        writePerformanceData(activity, "weather.launch-time.txt", launchTime);
        assertThat(launchTime, is(lessThanOrEqualTo(PAGE_LAUNCH_TIME_CRITERIA)));
    }

    public void test08_WeatherPagePauseResume() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        testPauseResume(WEATHER_PAGE, activity);
    }

    private float getPageFps(int pageType, Main activity, int msDuration, int msSampleInterval) {
        if (msSampleInterval < 0 || msDuration < 0) {
            return -1;
        }
        Media3DView m3dView = activity.getMedia3DView();
        m3dView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        if (pageType != 0) {
            enterPage(activity, pageType);
        }

        sleep(3000);  // wait 3 sec to make sure it is entered

        int i = 0;
        float fpsTotal = 0;
        for (; msDuration > 0; msDuration -= msSampleInterval) {
            sleep(msSampleInterval);
            fpsTotal += m3dView.getFPS();
            i++;
        }
        fpsTotal /= i;
        m3dView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        return fpsTotal;
    }

    public void test09_portalFpsTest() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();

        float fps = getPageFps(0,   // portal
                               activity,
                               30000,      // measure 30 sec
                               1000);      // sample each second
        writePerformanceData(activity, "portal.fps.txt", fps);
        assertThat(fps, is(greaterThanOrEqualTo(FPS_CRITERIA)));
    }

    public void test10_videoFpsTest() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        float fps = getPageFps(VIDEO_PAGE,   //
                               activity,
                               30000,
                               1000);
        writePerformanceData(activity, "video.fps.txt", fps);
        assertThat(fps, is(greaterThanOrEqualTo(FPS_CRITERIA)));
    }

    public void test11_weatherFpsTest() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        float fps = getPageFps(WEATHER_PAGE,
                               activity,
                               30000,
                               1000);
        writePerformanceData(activity, "weather.fps.txt", fps);
        assertThat(fps, is(greaterThanOrEqualTo(FPS_CRITERIA)));
    }

    public void test12_photoFpsTest() {
        Main activity = getActivity();
        getInstrumentation().waitForIdleSync();
        float fps = getPageFps(PHOTO_PAGE,
                               activity,
                               30000,
                               1000);
        writePerformanceData(activity, "photo.fps.txt", fps);
        assertThat(fps, is(greaterThanOrEqualTo(FPS_CRITERIA)));
    }

    private void writePerformanceData(Activity activity, String name, Object data) {
        File dataFile = new File(activity.getDir("perf", Context.MODE_PRIVATE), name);
        dataFile.delete();
        try {
            FileWriter writer = new FileWriter(dataFile);
            writer.write("YVALUE=" + data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
