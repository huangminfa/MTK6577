package com.mediatek.weather3dwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class Alarm {
    private static final String TAG = "W3D/Alarm";

    private Alarm() {}

    public static void setAlarm(Context context, String timezone) {
        LogUtil.v(TAG, "timezone = " + timezone);
        Calendar c = Calendar.getInstance();
        LogUtil.v(TAG, "now = " + c.getTimeInMillis());
        Calendar worldCalendar = Util.getTime(timezone);
        Calendar tempCalendar = Util.getTime(timezone);
        long deltaTime;

        int worldHour = worldCalendar.get(Calendar.HOUR_OF_DAY);
        if (worldHour >= 6 && worldHour < 18) {
            tempCalendar.set(Calendar.HOUR_OF_DAY, 18);
            tempCalendar.set(Calendar.MINUTE, 0);
            tempCalendar.set(Calendar.SECOND, 0);
            tempCalendar.set(Calendar.MILLISECOND, 0);
            deltaTime = tempCalendar.getTimeInMillis() - worldCalendar.getTimeInMillis();
        } else if (worldHour >= 18) {
            tempCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tempCalendar.set(Calendar.MINUTE, 59);
            tempCalendar.set(Calendar.SECOND, 59);
            tempCalendar.set(Calendar.MILLISECOND, 0);
            deltaTime = tempCalendar.getTimeInMillis() - worldCalendar.getTimeInMillis() + 1000 + 6 * 60 * 60 * 1000;
        } else {
            tempCalendar.set(Calendar.HOUR_OF_DAY, 6);
            tempCalendar.set(Calendar.MINUTE, 0);
            tempCalendar.set(Calendar.SECOND, 0);
            tempCalendar.set(Calendar.MILLISECOND, 0);
            deltaTime = tempCalendar.getTimeInMillis() - worldCalendar.getTimeInMillis();
        }
        LogUtil.v(TAG, "deltaTime = " + deltaTime);

        Intent intent = new Intent(context, WeatherWidget.class);
        intent.setAction(WeatherWidgetAction.ACTION_ALARM_TIME_UP);
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + deltaTime, sender);
    }

    public static void stopAlarm(Context context) {
        LogUtil.v(TAG);
        Intent intent = new Intent(context, WeatherWidget.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
