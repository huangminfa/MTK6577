package com.mediatek.weather3dwidget;

public class WeatherInfo {
    private int mCityIndex;
    private int mTotalCity;
    protected String mLocationName;
    protected String mTimezone;
    protected int mTempType;
    protected double mCurrentTemp;
    protected double mTempHigh;
    protected double mTempLow;
    protected long mLastUpdated = 0;
    protected int mCondition;
    protected ForecastData[] mForecastData;
    protected int mResult;

    public int getCityIndex() {
        return mCityIndex;
    }

    public int getTotalCity() {
        return mTotalCity;
    }

    public String getCityName() {
        return mLocationName;
    }

    public double getCurrentTemp() {
        return mCurrentTemp;
    }

    public int getTempType() {
        return mTempType;
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

    public int getCondition() {
        return mCondition;
    }

    public String getTimeZone() {
        return mTimezone;
    }

    public ForecastData[] getForecastData() {
        return mForecastData.clone();
    }

    public int getResult() {
        return mResult;
    }

    public void setCityIndex(int index) {
        mCityIndex = index;
    }

    public void setTotalCity(int total) {
        mTotalCity = total;
    }

    public void setCityName(String name) {
        mLocationName = name;
    }

    public void setTempType(int type) {
        mTempType = type;
    }

    public void setCurrentTemp(double temp) {
        mCurrentTemp = temp;
    }

    public void setTempHigh(double high) {
        mTempHigh = high;
    }

    public void setTempLow(double low) {
        mTempLow = low;
    }

    public void setLastUpdated(long update) {
        mLastUpdated = update;
    }

    public void setCondition(int condition) {
        mCondition = condition;
    }

    public void setTimeZone(String timeZone) {
        mTimezone = timeZone;
    }

    public void setResult(int result) {
        mResult = result;
    }

    public void setForecastData(ForecastData[] data) {
        mForecastData = null;
        mForecastData = new ForecastData[data.length];
        System.arraycopy(data, 0, mForecastData, 0, data.length);
    }
}
