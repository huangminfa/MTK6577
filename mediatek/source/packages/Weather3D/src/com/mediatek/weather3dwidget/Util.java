package com.mediatek.weather3dwidget;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Util {
    private static final String TAG = "W3D/Util";
    private static final String PREF = "WEATHERWIDGET_PREF";

    private Util() {}

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void clear(Context c) {
        getSharedPreferences(c).edit().clear().commit();
    }

    public static boolean isSameDay(long day1, long day2) {
        Calendar C1 = Calendar.getInstance();
        Calendar C2 = Calendar.getInstance();
        C1.setTimeInMillis(day1);
        C2.setTimeInMillis(day2);
        return C1.get(Calendar.YEAR) == C2.get(Calendar.YEAR) && C1.get(Calendar.MONTH) == C2.get(Calendar.MONTH)
                && C1.get(Calendar.DAY_OF_MONTH) == C2.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDateString(long day) {
        return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date(day));
    }

    public static String getTimeString(Context context, long day) {
        DateFormat dateFormat;
        String timeFormat = android.provider.Settings.System.getString(context.getContentResolver(),
                android.provider.Settings.System.TIME_12_24);

        Locale locale = Locale.getDefault();
        LogUtil.v(TAG, "locale = " + locale);
        if ("24".equals(timeFormat)){
            dateFormat = new SimpleDateFormat("HH:mm", locale);
        } else {
            dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        }

        return dateFormat.format(new Date(day));
    }

    public static int getDayNight(String timeZoneString) {
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        final Calendar now = Calendar.getInstance(timeZone);
        final int hourNow = now.get(Calendar.HOUR_OF_DAY);

        LogUtil.v(TAG, "getDayNight = (hour, timeZone) = (" + hourNow + ", " + timeZone + ")");

        if (hourNow >= 6 && hourNow < 18) {
            return DayNight.DAY;
        } else {
            return DayNight.NIGHT;
        }
    }

    public static Calendar getTime(String timeZoneString) {
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        return Calendar.getInstance(timeZone);
    }
}
