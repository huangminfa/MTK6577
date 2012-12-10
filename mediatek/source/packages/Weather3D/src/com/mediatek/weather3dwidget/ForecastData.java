package com.mediatek.weather3dwidget;

public class ForecastData {
    private int mDateOffset;
    private double mHighTemp;
    private double mLowTemp;
    private int mWeatherCondition;

    public ForecastData(int dataOffset, double highTemp, double lowTemp, int condition) {
        mDateOffset = dataOffset;
        mHighTemp = highTemp;
        mLowTemp = lowTemp;
        mWeatherCondition = condition;
    }

    public int getDateOffset() {
        return mDateOffset;
    }

    public double getHighTemp() {
        return mHighTemp;
    }

    public double getLowTemp() {
        return mLowTemp;
    }

    public int getWeatherCondition() {
        return mWeatherCondition;
    }

    public void resetForecastData() {
        mDateOffset = 0;
        mHighTemp = 0;
        mLowTemp = 0;
        mWeatherCondition = 0;
    }

    public void setForecastData(int dateOffset, double highTemp, double lowTemp, int condition) {
        mDateOffset = dateOffset;
        mHighTemp = highTemp;
        mLowTemp = lowTemp;
        mWeatherCondition = condition;
    }

    @Override
    public String toString() {
        return ("Forecast, Low/High = " + mLowTemp + "/" + mHighTemp + ", Condition = " + mWeatherCondition);
    }
}
