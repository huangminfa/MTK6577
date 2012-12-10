package com.mediatek.weather3dwidget;

import android.content.Context;
import android.content.SharedPreferences;

public final class WeatherWidgetManager {
    private static final String TAG = "W3D/WeatherWidgetManager";

    private static WeatherWidgetManager mInstance = null;
    private final Context mContext;

    public static final String SEPARATOR = ", ";

    public static final String INSEPARATOR = "=";

    public static final String WIDGET_ID = "widget_id_";

    public static final String WIDGET_ID_M = "id";

    public static final String DEMO_MODE = "demo_mode";

    public static final String TEMPERATURE_TYPE = "temp_type";

    public static final String CITY_ID = "city_id";

    public static final String CITY_NAME = "city_name";

    public static final String TIME_ZONE = "time_zone";

    public static final String LAST_UPDATED = "last_updated";

    private static Object sWidgetLock = new Object();

    private WeatherWidgetManager(Context c) {
        mContext = c;
    }

    public static WeatherWidgetManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new WeatherWidgetManager(c);
        }
        return mInstance;
    }

    // General field
    public void saveGeneralInfo(int tempType, long lastUpdatedTime) {
        synchronized (sWidgetLock) {
            SharedPreferences pref = Util.getSharedPreferences(mContext);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(TEMPERATURE_TYPE, tempType);
            editor.putLong(LAST_UPDATED, lastUpdatedTime);
            editor.commit();
        }
    }

    public int getTempType() {
        synchronized (sWidgetLock) {
            SharedPreferences pref = Util.getSharedPreferences(mContext);
            int tempType = pref.getInt(TEMPERATURE_TYPE, 1);
            LogUtil.v(TAG, "tempType = " + tempType);
            return tempType;
        }
    }

    public int getLastUpdated() {
        synchronized (sWidgetLock) {
            SharedPreferences pref = Util.getSharedPreferences(mContext);
            int lastUpdated = pref.getInt(LAST_UPDATED, 0);
            LogUtil.v(TAG, "lastUpdated = " + lastUpdated);
            return lastUpdated;
        }
    }

    // separate widget information
    public void saveWidgetStatus(int appWidgetId, boolean demoMode, int tempType, int cityId, String cityName, String timeZone, long lastUpdate) {
        synchronized(sWidgetLock){
            SharedPreferences pref = Util.getSharedPreferences(mContext);

            String key = WIDGET_ID + appWidgetId;
            LogUtil.v(TAG, "saveWidgetStatus key = " + key);

            StringBuilder sb = new StringBuilder();
            sb.append(WIDGET_ID_M).append(INSEPARATOR).append(appWidgetId).append(SEPARATOR);
            sb.append(DEMO_MODE).append(INSEPARATOR).append(demoMode).append(SEPARATOR);
            sb.append(TEMPERATURE_TYPE).append(INSEPARATOR).append(tempType).append(SEPARATOR);
            sb.append(CITY_ID).append(INSEPARATOR).append(cityId).append(SEPARATOR);
            sb.append(CITY_NAME).append(INSEPARATOR).append(cityName).append(SEPARATOR);
            sb.append(TIME_ZONE).append(INSEPARATOR).append(timeZone).append(SEPARATOR);
            sb.append(LAST_UPDATED).append(INSEPARATOR).append(lastUpdate);

            pref.edit().putString(key, sb.toString()).commit();
            String content = pref.getString(key, "");
            LogUtil.v(TAG, "saveWidgetStatus content == " + content);
        }
    }

    public void updateWidgetStatus(int appWidgetId, WidgetInfo wInfo) {
        synchronized(sWidgetLock){
            WidgetInfo oldInfo = getWidgetStatus(appWidgetId);
            if (oldInfo == null) {
                LogUtil.i(TAG, "oldInfo == " + null);
                return;
            }

            oldInfo.mWidgetId = appWidgetId;
            oldInfo.mDemoMode = wInfo.mDemoMode;
            oldInfo.mTempType = wInfo.mTempType;
            oldInfo.mCityId = wInfo.mCityId;
            oldInfo.mCityName = wInfo.mCityName;
            oldInfo.mTimeZone = wInfo.mTimeZone;
            oldInfo.mLastUpdate = wInfo.mLastUpdate;

            SharedPreferences pref = Util.getSharedPreferences(mContext);
            String key = WIDGET_ID + appWidgetId;
            if (pref.contains(key)) {
                LogUtil.i(TAG, "updateWidgetStatus delete key = " + key);
                pref.edit().remove(key).commit();
            }
            saveWidgetStatus(appWidgetId, oldInfo.mDemoMode, oldInfo.mTempType, oldInfo.mCityId, oldInfo.mCityName, oldInfo.mTimeZone, oldInfo.mLastUpdate);
        }
    }

    private WidgetInfo parseWidgetInfo(String widgetContent, int appWidgetId) {
        WidgetInfo widgetInfo = null;
        if (widgetContent != null && !"".equals(widgetContent)) {
            widgetInfo = new WidgetInfo();

            widgetInfo.mWidgetId = appWidgetId;
            String[] keyValues = widgetContent.split(SEPARATOR);
            for (String keyValue : keyValues) {
                String[] entry = keyValue.split(INSEPARATOR);
                //LogUtil.e(TAG,"parseWidgetInfo, entry length = " + entry.length);
                if (entry.length != 2) {
                    //LogUtil.e(TAG,"parseWidgetInfo, entry length error");
                    continue;
                }
                if (DEMO_MODE.equals(entry[0])) {
                    widgetInfo.mDemoMode = Boolean.valueOf(entry[1]);
                } else if (TEMPERATURE_TYPE.equals(entry[0])) {
                    widgetInfo.mTempType = Integer.valueOf(entry[1]);
                } else if (CITY_ID.equals(entry[0])) {
                    widgetInfo.mCityId = Integer.valueOf(entry[1]);
                } else if (CITY_NAME.equals(entry[0]) && !(entry[1].equals("null"))) {
                    widgetInfo.mCityName = entry[1];
                } else if (TIME_ZONE.equals(entry[0])) {
                    widgetInfo.mTimeZone = entry[1];
                } else if (LAST_UPDATED.equals(entry[0])) {
                    widgetInfo.mLastUpdate = Long.valueOf(entry[1]);
                } else if (WIDGET_ID_M.equals(entry[0])) {
                    widgetInfo.mWidgetId = Integer.valueOf(entry[1]);
                }
            }
        }
        return widgetInfo;
    }

    public WidgetInfo getWidgetStatus(int appWidgetId) {
        WidgetInfo widgetInfo = null;
        synchronized(sWidgetLock){
            SharedPreferences pref = Util.getSharedPreferences(mContext);
            String key = WIDGET_ID + appWidgetId;
            //LogUtil.i(TAG, "getWidgetStatus, key = " + key);
            if (pref.contains(key)) {
                String widgetContent = pref.getString(key,"");
                widgetInfo = parseWidgetInfo(widgetContent, appWidgetId);
            }
        }
        //LogUtil.i(TAG, "getWidgetStatus, widgetInfo = " + widgetInfo);
        return widgetInfo;
    }

    public void deleteWidgetStatus(int appWidgetId) {
        synchronized(sWidgetLock){
            SharedPreferences pref = Util.getSharedPreferences(mContext);
            String key = WIDGET_ID + appWidgetId;
            if (pref.contains(key)) {
                pref.edit().remove(key).commit();
            }
        }
    }
}
