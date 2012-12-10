package com.mediatek.weather3dwidget;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import java.lang.String;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.mediatek.media3d.MainTest \
 * com.mediatek.media3d.tests/android.test.InstrumentationTestRunner
 */
public class WeatherActivityTest extends ActivityInstrumentationTestCase2<WeatherActivity> {
    private Instrumentation mInstrumentation;
    private WeatherActivity mActivity;

    public WeatherActivityTest() {
        super("com.mediatek.weather3dwidget", WeatherActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        mInstrumentation = null;
        mActivity = null;

        super.tearDown();
    }

    // test case #1
    public void test01Activity() {

    }
}