package com.mediatek.weather3dwidget;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.weather3dwidget.DayNight;
import com.mediatek.weather3dwidget.TimeZoneTransition;
import com.mediatek.weather3dwidget.Util;
import com.mediatek.weather3dwidget.WeatherActivity;
import com.mediatek.weather3dwidget.WeatherType;

import java.lang.Readable;
import java.lang.String;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.mediatek.weather3dwidget.WeatherTypeTest \
 * com.mediatek.weather3dwidget.tests/android.test.InstrumentationTestRunner
 */
public class WeatherTypeTest extends ActivityInstrumentationTestCase2<WeatherActivity> {
    private Instrumentation mInstrumentation;
    private WeatherActivity mActivity;

    public WeatherTypeTest() {
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
    public void test01IsSunMoonType() {
        assertTrue(WeatherType.isSunMoonNeededModelType(WeatherType.ModelType.SUNNY));
        assertFalse(WeatherType.isSunMoonNeededModelType(WeatherType.ModelType.RAIN));
    }

    // test case #2
    public void test02IsSnowType() {
        assertTrue(WeatherType.isSnowModelType(WeatherType.ModelType.SNOW));
        assertFalse(WeatherType.isSnowModelType(WeatherType.ModelType.SUNNY));
    }

    // test case #3
    public void test03IsSandType() {
        assertTrue(WeatherType.isSandModelType(WeatherType.ModelType.SAND));
        assertFalse(WeatherType.isSandModelType(WeatherType.ModelType.SUNNY));
    }

    // test case #4
    public void test04GetWeatherIcon() {
        assertEquals(R.raw.ic_sunny, WeatherType.getWeatherIcon(WeatherType.ModelType.SUNNY));
    }

    // test case #5
    public void test05ConvertToModelType() {
        assertEquals(WeatherType.ModelType.SUNNY, WeatherType.convertToModelType(WeatherType.Type.SUNNY));
    }

    // test case #6
    public void test06IsModelTypeInRange() {
        int testType = WeatherType.ModelType.SUNNY;
        assertTrue(WeatherType.isModelTypeInRange(testType));

        testType = WeatherType.ModelType.INDEX_MAX + 1;
        assertFalse(WeatherType.isModelTypeInRange(testType));

        testType = WeatherType.ModelType.INDEX_MIN - 1;
        assertFalse(WeatherType.isModelTypeInRange(testType));
    }
}