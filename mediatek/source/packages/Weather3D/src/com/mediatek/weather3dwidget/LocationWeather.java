package com.mediatek.weather3dwidget;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import com.mediatek.weather.Weather;

public class LocationWeather {

    private static final String TAG = "W3D/LocationWeather";

    protected int mLocationIndex;
    protected int mCityId = -1;
    protected String mLocationName;
    protected String mTimezone;
    protected int mTempType;
    protected double mCurrentTemp;
    protected double mTempHigh;
    protected double mTempLow;
    protected long mLastUpdated;
    protected Weather.WeatherCondition mCondition;
    private static final int FORECAST_DAY = 3;
    protected ForecastData[] mForecastData;
    protected int mResult = -1;
    /*
        (0) initial state
            -1
        (1) success
            public static final int SUCCESS = 0;
        (2) system time not correct
            public static final int ERROR_SYSTEM_TIME_NOT_CORRECT = 1;
        (3) network not available
            public static final int ERROR_NETWORK_NOT_AVAILABLE = 2;
        (4) update weather failed
            public static final int ERROR_UPDATE_WEATHER_FAILED = 3;
        (5) city id not correct
            public static final int ERROR_CITY_ID_NOT_CORRECT = 4;
    */

    public LocationWeather(int locationIndex) {
        LogUtil.v(TAG, "locationIndex = " + locationIndex);
        mLocationIndex = locationIndex;
        if (!isValid()) {
            mLocationName = "";
            mTimezone = Time.getCurrentTimezone();
            mCondition = Weather.WeatherCondition.Sunny;
        }
        initForecastData();
    }

    public LocationWeather(final int locationIndex, final int cityId) {
        this(locationIndex);
        mCityId = cityId;
    }

    public LocationWeather(final int locationIndex, final String name, final String timezone, final Weather.WeatherCondition condition,
                           final double currentTemp, final double tempLow, final double tempHigh, final ForecastData[] data) {
        // only for demo data, will always set result = 0, which means successfully
        this(locationIndex);
        mLocationName = name;
        mTimezone = timezone;
        mCondition = condition;
        mCurrentTemp = currentTemp;
        mTempLow = tempLow;
        mTempHigh = tempHigh;
        mLastUpdated = System.currentTimeMillis();
        System.arraycopy(data, 0, mForecastData, 0, data.length < FORECAST_DAY ? data.length : FORECAST_DAY);
        mResult = 0;
    }

    private void initForecastData() {
        mForecastData = new ForecastData[FORECAST_DAY];
        for (int i = 0; i < FORECAST_DAY; i++) {
            mForecastData[i] = new ForecastData(0, 0, 0, 0);
        }
    }

    private boolean isValid() {
        return (mLocationIndex >= 0);
    }

    public int getLocationIndex() {
        return mLocationIndex;
    }

    public int getCityId() {
        return mCityId;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public int getTempType() {
        return mTempType;
    }

    public double getCurrentTemp() {
        return mCurrentTemp;
    }

    public double getTempHigh() {
        return mTempHigh;
    }

    public double getTempLow() {
        return mTempLow;
    }

    public long getLastUpdated() {
        return mLastUpdated;
    }

    public int getResult() {
        return mResult;
    }

    public Weather.WeatherCondition getCondition() {
        return mCondition;
    }

    public ForecastData[] getForecastData() {
        return mForecastData.clone();
    }

    public void queryCityName(Cursor c) {
        mLocationName = c.getString(c.getColumnIndex(Weather.City.NAME));
        LogUtil.v(TAG, "LocationName = " + mLocationName);
    }

    public void queryGeoInformation(ContentResolver contentResolver) {
        Cursor c = contentResolver.query(ContentUris.withAppendedId(Weather.City.CONTENT_URI, mCityId), null, null,
                null, null);
        if (c == null) {
            return;
        }

        if (c.getCount() != 0) {
            String timezone;
            if (mLocationIndex == -1) {
                mLocationIndex = 0;
            }
            if (c.moveToFirst()) {
                mLocationName = c.getString(c.getColumnIndex(Weather.City.NAME));
                timezone = c.getString(c.getColumnIndex(Weather.City.TIMEZONE));
                if (timezone.startsWith("GMT")) {
                    mTimezone = timezone;
                } else {
                    mTimezone = TimeZoneTransition.getGmtTz(timezone);
                }
            }
        }
        LogUtil.v(TAG, "LocationName = " + mLocationName + ", TimeZone = " + mTimezone);
        c.close();
    }

    public void queryWeather(ContentResolver contentResolver) {
        LogUtil.v(TAG);
        Cursor c = contentResolver.query(ContentUris.withAppendedId(Weather.Current.CONTENT_URI, mCityId), null, null,
            null, null);
        if (c != null && c.getCount() != 0) {
            if (mLocationIndex == -1) {
                mLocationIndex = 0;
            }
            if (c.moveToFirst()) {
                mTempType = c.getInt(c.getColumnIndex(Weather.Current.TEMPERATURE_TYPE));
                mCurrentTemp = c.getDouble(c.getColumnIndex(Weather.Current.TEMP_CURRENT));
                mTempHigh = c.getDouble(c.getColumnIndex(Weather.Current.TEMP_HIGH));
                mTempLow = c.getDouble(c.getColumnIndex(Weather.Current.TEMP_LOW));
                mLastUpdated = c.getLong(c.getColumnIndex(Weather.Current.LAST_UPDATED));
                mCondition = Weather
                        .intToWeatherCondition(c.getInt(c.getColumnIndex(Weather.Current.CONDITION_TYPE_ID)));
            }
        }
        if (c != null) {
            c.close();
        }

        final Uri futureWeatherUri = ContentUris.withAppendedId(Weather.Forecast.CONTENT_URI, mCityId);
        LogUtil.i(TAG, "futureWeatherUri = " + futureWeatherUri);
        Cursor futureCursor = contentResolver.query(futureWeatherUri, null, null, null, null);
        if (futureCursor == null || !futureCursor.moveToFirst() || futureCursor.getCount() < 3) {
            LogUtil.i(TAG, "get current weather failed");
            if (futureCursor != null) {
                futureCursor.close();
            }
            for (int i = 0; i < 3; i++) {
                mForecastData[i].resetForecastData();
            }
            return;
        }

        try {
            if (futureCursor != null && futureCursor.moveToFirst()) {
                int conditionTypeIndex = futureCursor.getColumnIndex(Weather.Forecast.CONDITION_TYPE_ID);
                int tempHighIndex = futureCursor.getColumnIndex(Weather.Forecast.TEMP_HIGH);
                int tempLowIndex = futureCursor.getColumnIndex(Weather.Forecast.TEMP_LOW);

                int i = 0;
                while (!futureCursor.isAfterLast() && i < FORECAST_DAY) {
                    int condition = LocationWeather.getWeather(Weather.intToWeatherCondition(futureCursor.getInt(conditionTypeIndex)));
                    double highTemp = futureCursor.getDouble(tempHighIndex);
                    double lowTemp = futureCursor.getDouble(tempLowIndex);
                    mForecastData[i].setForecastData(i + 1, highTemp, lowTemp, condition);
                    LogUtil.v(TAG, "queryWeather = (" + i + ", " + highTemp + ", " + lowTemp + ", " + condition + ")");
                    futureCursor.moveToNext();
                    i++;
                }
            }
        } finally {
            if (futureCursor != null) {
                futureCursor.close();
            }
        }
    }

    public String getTimeZone() {
        if (isValid() && mTimezone != null) {
            return mTimezone;
        }
        return Time.getCurrentTimezone();
    }

    /**
     * Return the weather id by city. it's a simulation function
     * 
     * @param city
     *            city id
     * @return weather id
     */
    public int getWeather() {
        int weather = WeatherType.Type.SUNNY;
        if (isValid() && mCondition != null) {
            weather = getWeather(mCondition);
        }
        return weather;
    }

    public static int getWeather(Weather.WeatherCondition condition) {
        int weather = WeatherType.Type.SUNNY;

        if (condition != null) {
            switch (condition) {
                case Sunny:
                    weather = WeatherType.Type.SUNNY;
                    break;

                case Windy:
                    weather = WeatherType.Type.WINDY;
                    break;

                case Hurricane:
                    weather = WeatherType.Type.BLUSTERY;
                    break;

                case Tornado:
                    weather = WeatherType.Type.TORNADO;
                    break;

                case Cloudy:
                    weather = WeatherType.Type.CLOUDY;
                    break;

                case Overcast:
                    weather = WeatherType.Type.OVERCAST;
                    break;

                case Drizzle:
                case Shower:
                    weather = WeatherType.Type.SHOWER;
                    break;

                case Rain:
                    weather = WeatherType.Type.RAIN;
                    break;

                case Downpour:
                case FreezingRain:
                case SuperDownpour:
                    weather = WeatherType.Type.DOWNPOUR;
                    break;

                case Hail:
                    weather = WeatherType.Type.HAIL;
                    break;

                case ThunderyShower:
                    weather = WeatherType.Type.THUNDER_SHOWER;
                    break;

                case ThunderstormHail:
                    weather = WeatherType.Type.THUNDER_STORM_HAIL;
                    break;

                case SnowShowers:
                    weather = WeatherType.Type.SNOW_SHOWER;
                    break;

                case Flurries:
                    weather = WeatherType.Type.SNOW_LIGHT;
                    break;

                case Snow:
                    weather = WeatherType.Type.SNOW;
                    break;

                case Blizzard:
                case HeavySnow:
                    weather = WeatherType.Type.HEAVY_SNOW;
                    break;

                case Sleet:
                    weather = WeatherType.Type.SLEET;
                    break;

                case Fog:
                    weather = WeatherType.Type.FOG;
                    break;

                case Dust:
                    weather = WeatherType.Type.DUST;
                    break;

                case Sand:
                case SandStorm:
                    weather = WeatherType.Type.SAND;
                    break;

                default:
                    weather = WeatherType.Type.SUNNY;
                    break;
            }
        }
        return weather;
    }

    public boolean isValidCityId() {
        return (mCityId >= 0);
    }

    @Override
    public String toString() {
        return ("result = " + mResult+ ", cityID = " + mCityId + ", Timezone = " + mTimezone + ", Temp = " + mCurrentTemp + ", Low = " + mTempLow + ", High = " + mTempHigh +
                ", lastUpdate = " + mLastUpdated + ", Day1: " + mForecastData[0] + ", Day2: " + mForecastData[1] +
                ", Day3: " +mForecastData[2]);
    }
}
