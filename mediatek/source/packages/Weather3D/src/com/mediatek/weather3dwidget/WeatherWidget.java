package com.mediatek.weather3dwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import com.mediatek.weather.Weather;

public class WeatherWidget extends AppWidgetProvider {
    private static final String TAG = "W3D/WeatherWidget";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        LogUtil.v(TAG, "onUpdate");

        int[] newAppWidgetIds = appWidgetIds;
        if (appWidgetIds == null) {
            LogUtil.v(TAG, "appWidgetIds = null");
            newAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                    context, WeatherWidget.class));
        }

        LogUtil.v(TAG, "appWidgetIds_length = " + newAppWidgetIds.length);

        if (newAppWidgetIds.length == 1) {
            int appWidgetId = newAppWidgetIds[0];
            LogUtil.v(TAG, "appWidgetId_sub = " + appWidgetId);

            WeatherWidgetManager manager = WeatherWidgetManager.getInstance(context);
            WidgetInfo info = manager.getWidgetStatus(appWidgetId);

            if (info == null) {
                // first added
                boolean isDemo = false;
                Weather3D.setIsDemoMode(isDemo);
                manager.saveWidgetStatus(appWidgetId, isDemo, Weather.TEMPERATURE_CELSIUS, 0, null, null, System.currentTimeMillis());
            } else {
                Weather3D.setIsDemoMode(info.mDemoMode);
            }

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather);
            appWidgetManager.updateAppWidget(newAppWidgetIds, views);

            Intent intent = new Intent(context, UpdateService.class);
            intent.setAction(WeatherWidgetAction.ACTION_INIT);
            intent.putExtra("widget_id", appWidgetId);
            context.startService(intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        LogUtil.v(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        LogUtil.v(TAG, "onDisabled");
        context.stopService(new Intent(context, UpdateService.class));
        LogUtil.v(TAG, "stopUpdateService");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        LogUtil.v(TAG, "onDeleted");
        WeatherWidgetManager manager = WeatherWidgetManager.getInstance(context);
        for (int appWidgetId : appWidgetIds) {
            manager.deleteWidgetStatus(appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        LogUtil.i(TAG, "onReceive action = " + intent.getAction());

        if (WeatherWidgetAction.ACTION_SCROLL.equals(intent.getAction())) {
            LogUtil.i(TAG, "onReceive action = scroll");
            String direction = intent.getStringExtra(WeatherWidgetAction.DIRECTION);

            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID && direction != null) {
                WeatherWidgetManager manager = WeatherWidgetManager.getInstance(context);
                WidgetInfo wInfo = manager.getWidgetStatus(id);
                if (wInfo == null) {
                    LogUtil.i(TAG, "onReceive no widget info");
                    return;
                }

                Intent i = new Intent(context, UpdateService.class);
                i.putExtra("widget_id", id);
                i.putExtra(WeatherWidgetAction.DIRECTION, direction);
                i.setAction(WeatherWidgetAction.ACTION_SCROLL);
                context.startService(i);
            }
        } else if (WeatherWidgetAction.ACTION_WEATHER_WIDGET_VIEW_ATTACH.equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            LogUtil.i(TAG, "onReceive action = WeatherWidgetView-onAttachedToWindow, id = " + id);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WeatherWidgetManager manager = WeatherWidgetManager.getInstance(context);
                WidgetInfo wInfo = manager.getWidgetStatus(id);
                if (wInfo == null) {
                    LogUtil.i(TAG, "onReceive no widget info");
                    return;
                }

                Intent i = new Intent(context, UpdateService.class);
                i.putExtra("widget_id", id);
                i.setAction(WeatherWidgetAction.ACTION_WEATHER_WIDGET_VIEW_ATTACH);
                context.startService(i);
            }
        } else {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                context, WeatherWidget.class));
            if (appWidgetIds.length == 0) {
                LogUtil.v(TAG, "onReceive - no widget instance, no handle intent");
                return;
            }

            boolean sendOutIntent = false;
            if (WeatherWidgetAction.ACTION_CITY_LIST_CHANGE.equals(intent.getAction())) {
                LogUtil.v(TAG, "onReceive action = city_list_change");
                sendOutIntent = true;
            } else if (WeatherWidgetAction.ACTION_ALARM_TIME_UP.equals(intent.getAction())) {
                LogUtil.v(TAG, "onReceive action = alarm_time_up");
                sendOutIntent = true;
            } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                LogUtil.v(TAG, "onReceive action = time_zone_changed");
                sendOutIntent = true;
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                LogUtil.v(TAG, "onReceive action = time_changed");
                sendOutIntent = true;
            }

            if (sendOutIntent) {
                Intent i = new Intent(context, UpdateService.class);
                i.setAction(intent.getAction());
                context.startService(i);
            }
        }
    }

    public static PendingIntent getRefreshPendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, UpdateService.class).setAction(
                            WeatherWidgetAction.ACTION_REFRESH).setData(
                            Uri.parse(String.valueOf(appWidgetId)));
        return (PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static PendingIntent getSettingPendingIntent(Context context) {
        Intent intent = new Intent(Weather.Intents.ACTION_SETTING);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return (PendingIntent.getActivity(context, 0, intent, 0));
    }
}

