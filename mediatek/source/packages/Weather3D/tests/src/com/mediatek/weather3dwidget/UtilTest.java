package com.mediatek.weather3dwidget;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.weather3dwidget.DayNight;
import com.mediatek.weather3dwidget.TimeZoneTransition;
import com.mediatek.weather3dwidget.Util;

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
 * -e class com.mediatek.weather3dwidget.UtilTest \
 * com.mediatek.weather3dwidget.tests/android.test.InstrumentationTestRunner
 */
public class UtilTest extends ActivityInstrumentationTestCase2<WeatherActivity> {
    private Instrumentation mInstrumentation;
    private WeatherActivity mActivity;

    public UtilTest() {
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
    public void test01GetDayNight() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        Calendar now = Calendar.getInstance(timeZone);
        int hourNow = now.get(Calendar.HOUR_OF_DAY);
        int expectedDayNight = ((hourNow >= 6 && hourNow < 18) ? DayNight.DAY : DayNight.NIGHT);

        int actualDayNight = Util.getDayNight("GMT+8");
        assertEquals(expectedDayNight, actualDayNight);

        actualDayNight = Util.getDayNight("GMT-4");
        assertTrue(expectedDayNight != actualDayNight);
    }

    // test case #2
    public void test02IsSameDay() {
        long currentTime = System.currentTimeMillis();
        long targetTime = currentTime;
        assertTrue(Util.isSameDay(currentTime, targetTime));

        targetTime = currentTime - 24 * 60 * 60 * 1000;
        // targetTime = currentTime - 1 day (24 hr * 60 min * 60 sec * 1000 mSec)
        assertFalse(Util.isSameDay(currentTime, targetTime));
    }
}