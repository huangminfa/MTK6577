package com.mediatek.weather3dwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class UpdateService extends Service {
    private static final String TAG = "W3D/UpdateService";
    private WeatherBureau mWeatherBureau;
    private boolean mIsUpdating;
    private static final String METHOD_SETID = "setWidgetId";

    private int mWidgetId;
    private int mLocationId;
    private int mCityId;
    
    private static final String KEY_WIDGET_ID = "widget_id";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        LogUtil.e(TAG, "onDestroy");
        if (mWeatherBureau != null) {
            mWeatherBureau.cancelAlarm(UpdateService.this);
            mWeatherBureau.deinit();
            mWeatherBureau = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand, intent = " + intent);

        if (intent == null) {
            return START_REDELIVER_INTENT;
        }

        if (mWeatherBureau == null) {
            mWeatherBureau = new WeatherBureau();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.weather);

        LogUtil.v(TAG, "packageName = " + this.getPackageName());
        LogUtil.v(TAG, "remoteViews = " + views);

        int widgetId = intent.getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID);
        LogUtil.v(TAG, "widgetId = " + widgetId);
        String intentAction = intent.getAction();

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // update all widgets
            if (WeatherWidgetAction.ACTION_CITY_LIST_CHANGE.equals(intentAction)) {
                LogUtil.v(TAG, "city list change intent");
                if (!getWidgetInfo(mWidgetId).mDemoMode) {
                    mWeatherBureau.refreshWeatherForCityListChange();
                }
            } else if (WeatherWidgetAction.ACTION_REFRESH.equals(intentAction)) {
                if (!isWeatherBureauNeedInit()) {
                    int id = Integer.parseInt(intent.getDataString());
                    if (!mIsUpdating && !getWidgetInfo(id).mDemoMode) {
                        int cityIndex = CityManager.getCurrentIndex();
                        LogUtil.v(TAG, "refresh intent, widgetId = " + id + ", locationId = " + cityIndex);
                        views.setInt(R.id.view_3d_weather, "showUpdating", 1);
                        views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                        appWidgetManager.updateAppWidget(id, views);
                        mIsUpdating = true;
                        mWeatherBureau.refreshWeatherByLocationId(cityIndex);
                    }
                }
            } else if (WeatherWidgetAction.ACTION_ALARM_TIME_UP.equals(intentAction)) {
                LogUtil.v(TAG, "alarm time up intent");
                if (mWeatherBureau.isInited()) {
                    String timeUpTimeZone = mWeatherBureau.getUpdateTimeZone();
                    LogUtil.v(TAG, "timeZone = " + timeUpTimeZone);
                    if (timeUpTimeZone != null) {
                        updateDayNight();
                        mWeatherBureau.cancelAlarm(UpdateService.this);
                        mWeatherBureau.setNextAlarm(UpdateService.this);
                    }
                }
            } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(intentAction) || Intent.ACTION_TIME_CHANGED.equals(intentAction)) {
                LogUtil.v(TAG, intentAction);
                if (mWeatherBureau.isInited()) {
                    updateDayNight();
                    mWeatherBureau.cancelAlarm(UpdateService.this);
                    mWeatherBureau.setNextAlarm(UpdateService.this);
                }
            } else if (WeatherWidgetAction.ACTION_WEATHER_BUREAU_NOTIFY.equals(intentAction)) {
                int notifyType = intent.getIntExtra(WeatherWidgetAction.NOTIFY_TYPE, 0);
                LogUtil.v(TAG, "weather bureau notify, type = " + notifyType);
                if (notifyType == WeatherBureau.NOTIFY_INITED) {
                    if (mWeatherBureau.isInited()) {
                        mWeatherBureau.cancelAlarm(UpdateService.this);
                        mWeatherBureau.setNextAlarm(UpdateService.this);
                        updateWeatherView(mWidgetId);
                    }
                } else if (notifyType == WeatherBureau.NOTIFY_REFRESH_FINISH) {
                    LogUtil.v(TAG, "handleMsg - refresh_finish");
                    onRefreshFinish();
                } else if (notifyType == WeatherBureau.NOTIFY_ON_WEATHER_UPDATE_FINISH) {
                    onUpdateWeatherFinish(intent);
                } else if (notifyType == WeatherBureau.NOTIFY_ON_CITY_LIST_CHANGE_FINISH) {
                    onCityListChangeFinish();
                }
            }
        } else {
            // update the assigned widget id widget
            Context context = getApplicationContext();

            WeatherWidgetManager wManager = WeatherWidgetManager.getInstance(context);
            WidgetInfo wInfo = wManager.getWidgetStatus(widgetId);

            if (WeatherWidgetAction.ACTION_INIT.equals(intentAction)) {
                LogUtil.v(TAG, "init intent - WeatherBureau isNeedInit = " + mWeatherBureau.isNeedInit());
                if (mWeatherBureau.isNeedInit()) {
                    views.setInt(R.id.view_3d_weather, "showUpdating", 1);
                    views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                    appWidgetManager.updateAppWidget(widgetId, views);

                    mWeatherBureau.init(UpdateService.this);
                    mWidgetId = widgetId;
                    setWidgetId(mWidgetId);
                }
            } else if (WeatherWidgetAction.ACTION_SCROLL.equals(intentAction)) {
                LogUtil.v(TAG, "scroll intent");

                if (!isWeatherBureauNeedInit()) {
                    int total = mWeatherBureau.getLocationCount();
                    LogUtil.v(TAG, "total = " + total);
                    CityManager.setTotal(total);

                    if (total > 0) {
                        // could not send out city change when there is not city set.
                        int cityIndex = wInfo.mCityId;
                        CityManager.setCurrentIndex(cityIndex);
                        LogUtil.v(TAG, "cityIndex = " + cityIndex);

                        int order;
                        if (intent.getStringExtra(WeatherWidgetAction.DIRECTION).equals(WeatherWidgetAction.DIRECTION_NEXT)) {
                            LogUtil.v(TAG, "scroll down - widgetId = " + widgetId);
                            cityIndex = CityManager.getNextCity();
                            order = ScrollType.SCROLL_DOWN;
                        } else {
                            LogUtil.v(TAG, "scroll up - widgetId = " + widgetId);
                            cityIndex = CityManager.getPreviousCity();
                            order = ScrollType.SCROLL_UP;
                        }

                        LocationWeather weather = mWeatherBureau.getLocationByIndex(cityIndex);
                        mLocationId = cityIndex;
                        mCityId = weather.getCityId();

                        views.setBundle(R.id.view_3d_weather, "updateWeatherView", getWeatherBundle(cityIndex, total, weather, order));

                        wInfo.mCityId = cityIndex;
                        wInfo.mTimeZone = weather.getTimeZone();
                        wInfo.mCityName = weather.getLocationName();
                        wManager.updateWidgetStatus(widgetId, wInfo);
                        appWidgetManager.updateAppWidget(widgetId, views);
                    }
                }
            } else if (WeatherWidgetAction.ACTION_WEATHER_WIDGET_VIEW_ATTACH.equals(intentAction)) {
                LogUtil.v(TAG, "onAttach intent");
                if (mWeatherBureau.isInited()) {
                    mWeatherBureau.setNextAlarm(UpdateService.this);
                    updateWeatherView(widgetId);
                } else {
                    views.setInt(R.id.view_3d_weather, "showUpdating", 1);
                    views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                    appWidgetManager.updateAppWidget(widgetId, views);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private static Bundle getWeatherBundle(int cityIndex, int totalCity, LocationWeather weather, int order) {
        Bundle bundle = new Bundle();
        bundle.putInt("totalCity", totalCity);
        if (totalCity != 0) {
            bundle.putInt("order", order);
            bundle.putInt("cityIndex", cityIndex);
            bundle.putInt("tempType", weather.getTempType());
            bundle.putDouble("temp", weather.getCurrentTemp());
            bundle.putDouble("lowTemp", weather.getTempLow());
            bundle.putDouble("highTemp", weather.getTempHigh());
            bundle.putString("cityName", weather.getLocationName());
            bundle.putLong("lastUpdated", weather.getLastUpdated());
            bundle.putInt("condition", weather.getWeather());
            bundle.putString("timeZone", weather.getTimezone());
            bundle.putInt("result", weather.getResult());
            ForecastData[] data = weather.getForecastData();
            bundle.putInt("firstOffset", data[0].getDateOffset());
            bundle.putDouble("firstHighTemp", data[0].getHighTemp());
            bundle.putDouble("firstLowTemp", data[0].getLowTemp());
            bundle.putInt("firstForecast", data[0].getWeatherCondition());
            bundle.putInt("secondOffset", data[1].getDateOffset());
            bundle.putDouble("secondHighTemp", data[1].getHighTemp());
            bundle.putDouble("secondLowTemp", data[1].getLowTemp());
            bundle.putInt("secondForecast", data[1].getWeatherCondition());
            bundle.putInt("thirdOffset", data[2].getDateOffset());
            bundle.putDouble("thirdHighTemp", data[2].getHighTemp());
            bundle.putDouble("thirdLowTemp", data[2].getLowTemp());
            bundle.putInt("thirdForecast", data[2].getWeatherCondition());
        }
        return bundle;
    }

    private void onRefreshFinish() {
        LogUtil.v(TAG, "handleMsg - refresh_finish");

        mIsUpdating = false;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(UpdateService.this.getPackageName(), R.layout.weather);
        Context context = getApplicationContext();
        WeatherWidgetManager wManager = WeatherWidgetManager.getInstance(context);
        WidgetInfo wInfo = wManager.getWidgetStatus(mWidgetId);

        int total = mWeatherBureau.getLocationCount();
        LogUtil.v(TAG, "total = " + total);
        int cityIndex = wInfo.mCityId;

        LocationWeather weather = mWeatherBureau.getLocationByIndex(cityIndex);
        LogUtil.v(TAG, "handleMsg - refresh_finish - LocationWeather = " + weather);
        views.setBundle(R.id.view_3d_weather, "updateWeatherView", getWeatherBundle(cityIndex, total, weather, ScrollType.NO_SCROLL));
        views.setViewVisibility(R.id.progress_bar, View.GONE);
        appWidgetManager.updateAppWidget(mWidgetId, views);
    }

    private void onUpdateWeatherFinish(Intent intent) {
        int locationId = intent.getIntExtra(WeatherWidgetAction.LOCATION_ID, 0);
        LogUtil.v(TAG, "handleMsg - on_update_finish - id = " + locationId);

        WidgetInfo wInfo = getWidgetInfo(mWidgetId);

        if (locationId == wInfo.mCityId) {
            LocationWeather weather = mWeatherBureau.getLocationByIndex(locationId);
            int total = mWeatherBureau.getLocationCount();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            RemoteViews views = new RemoteViews(UpdateService.this.getPackageName(), R.layout.weather);
            views.setBundle(R.id.view_3d_weather, "updateWeatherView", getWeatherBundle(locationId, total, weather, ScrollType.NO_SCROLL));
            appWidgetManager.updateAppWidget(mWidgetId, views);
            LogUtil.v(TAG, "handleMsg - on_update_finish - LocationWeather = " + weather);
        } else {
            LogUtil.v(TAG, "handleMsg - on_update_finish - updateId =" + locationId + ", currentId = " + wInfo.mCityId);
        }
    }

    private void onCityListChangeFinish() {
        LogUtil.v(TAG, "handleMsg - city_list_change_finish - (locationId, cityId) = (" + mLocationId + ", " + mCityId + ")");

        Context context = getApplicationContext();
        WeatherWidgetManager wManager = WeatherWidgetManager.getInstance(context);
        WidgetInfo wInfo = wManager.getWidgetStatus(mWidgetId);
        int total = mWeatherBureau.getLocationCount();
        RemoteViews views = new RemoteViews(UpdateService.this.getPackageName(), R.layout.weather);

        mWeatherBureau.cancelAlarm(UpdateService.this);
        LocationWeather weather;
        if (total == 0) {
            // city count = 0
            mLocationId = 0;
            mCityId = 0;
            wInfo.mCityId = 0;
            wManager.updateWidgetStatus(mWidgetId, wInfo);
            weather = null;
            views.setViewVisibility(R.id.refresh, View.GONE);
        } else {
            int locationId;

            if (mWeatherBureau.isCityIdExist(mCityId)) {
                locationId = mWeatherBureau.getLocationIdByCityId(mCityId);
                wInfo.mCityId = locationId;
            } else {
                if (mLocationId == 0 && total == 1) {
                    locationId = mLocationId;
                } else if (mLocationId < total - 1) {
                    locationId = mLocationId;
                } else {
                    locationId = mLocationId - 1;
                }

                wInfo.mCityId = locationId;
            }
            mWeatherBureau.setNextAlarm(UpdateService.this);

            weather = mWeatherBureau.getLocationByIndex(locationId);
            wInfo.mTimeZone = weather.getTimeZone();
            wInfo.mCityName = weather.getLocationName();
            wManager.updateWidgetStatus(mWidgetId, wInfo);

            mLocationId = locationId;
            mCityId = weather.getCityId();

            views.setViewVisibility(R.id.refresh, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.refresh, WeatherWidget.getRefreshPendingIntent(context, mWidgetId));
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        views.setBundle(R.id.view_3d_weather, "updateWeatherView", getWeatherBundle(mLocationId, total, weather, ScrollType.NO_SCROLL));
        appWidgetManager.updateAppWidget(mWidgetId, views);
    }

    private void updateWeatherView(int id) {
        LogUtil.v(TAG, "updateWeatherView - id = " + id);

        Context context = getApplicationContext();
        WeatherWidgetManager manager = WeatherWidgetManager.getInstance(context);
        WidgetInfo wInfo = manager.getWidgetStatus(id);
        if (wInfo == null) {
            LogUtil.v(TAG, "updateWeatherView - noSuchWidgetId");
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.weather);

        int cityIndex = 0;
        int total = mWeatherBureau.getLocationCount();

        LogUtil.v(TAG, "init_weather, cityIndex = " + cityIndex);
        LocationWeather weather = mWeatherBureau.getLocationByIndex(cityIndex);

        LogUtil.v(TAG, "cityName = " + weather.getLocationName());
        LogUtil.v(TAG, "temp = " + weather.getCurrentTemp());
        LogUtil.v(TAG, "high/low = " + weather.getTempLow() + "/" + weather.getTempHigh());
        LogUtil.v(TAG, "timezone = " + weather.getTimezone());
        LogUtil.v(TAG, "total = " + total);

        views.setBundle(R.id.view_3d_weather, "updateWeatherView", getWeatherBundle(cityIndex, total, weather, ScrollType.NO_SCROLL));
        views.setInt(R.id.view_3d_weather, METHOD_SETID, id);

        LogUtil.v(TAG, "init intent - widgetId = " + id);
        views.setViewVisibility(R.id.progress_bar, View.GONE);
        if (!wInfo.mDemoMode) {
            if (total == 0) {
                views.setViewVisibility(R.id.refresh, View.GONE);
            } else {
                views.setViewVisibility(R.id.refresh, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.refresh, WeatherWidget.getRefreshPendingIntent(context, id));
            }
            views.setViewVisibility(R.id.setting, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.setting, WeatherWidget.getSettingPendingIntent(context));
        }
        LogUtil.v(TAG, "set button setting/refresh intent");
        appWidgetManager.updateAppWidget(id, views);

        wInfo.mCityId = cityIndex;
        wInfo.mTimeZone = weather.getTimeZone();
        wInfo.mCityName = weather.getLocationName();
        manager.updateWidgetStatus(id, wInfo);
    }

    private void updateDayNight() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(UpdateService.this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                UpdateService.this, WeatherWidget.class));
        for (int widgetId: appWidgetIds) {
            LogUtil.v(TAG, "updateDayNight - id = " + widgetId);
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.weather);
            views.setInt(R.id.view_3d_weather, "switchDayNight", 1);
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    private boolean isWeatherBureauNeedInit() {
        if (!mWeatherBureau.isInited()) {
            // if not in Inited State, then return true. (includes NOT_INIT and INITING)
            if (mWeatherBureau.isNeedInit()) {
                // if in INITING and INITED case, then will not init again.
                // if in NOT_INIT case, then init again.

                // show Updating and re initialize WeatherBureau
                LogUtil.v(TAG, "re-initialize WeatherBureau");
                mWidgetId = getWidgetId();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.weather);
                views.setInt(R.id.view_3d_weather, "showUpdating", 1);
                views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                appWidgetManager.updateAppWidget(mWidgetId, views);

                mWeatherBureau.init(UpdateService.this);
            }
            return true;
        }
        return false;
    }
    
    private int getWidgetId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return pref.getInt(KEY_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private void setWidgetId(int widgetId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_WIDGET_ID, widgetId);
        editor.commit();
    }

    private WidgetInfo getWidgetInfo(int widgetId) {
        return WeatherWidgetManager.getInstance(getApplicationContext()).getWidgetStatus(widgetId);
    }
}
