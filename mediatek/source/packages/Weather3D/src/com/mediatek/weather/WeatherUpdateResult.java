package com.mediatek.weather;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherUpdateResult implements Parcelable{
    /** success */ 
    public static final int SUCCESS = 0;
    /** system time not correct */
    public static final int ERROR_SYSTEM_TIME_NOT_CORRECT = 1;
    /**network not available */
    public static final int ERROR_NETWORK_NOT_AVAILABLE = 2;
    /**update weather failed */
    public static final int ERROR_UPDATE_WEATHER_FAILED = 3;
    /** city id not correct */
    public static final int ERROR_CITY_ID_NOT_CORRECT = 4; 

    public int mResult;
    public int mErrorMessageResId = -1;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flag) {
        out.writeInt(mResult);
        out.writeInt(mErrorMessageResId);
    }

    public void readFromParcel(Parcel in) {
        mResult = in.readInt();
        mErrorMessageResId = in.readInt();
    }

    public static final Parcelable.Creator<WeatherUpdateResult> CREATOR = new Parcelable.Creator<WeatherUpdateResult>() {
        public WeatherUpdateResult createFromParcel(Parcel in) {
            WeatherUpdateResult r = new WeatherUpdateResult();
            r.readFromParcel(in);
            return r;
        }

        public WeatherUpdateResult[] newArray(int size) {
            return new WeatherUpdateResult[size];
        }
    };

    public String toString() {
        if (mResult == SUCCESS) {
            return "update success";
        } else if (mResult == ERROR_CITY_ID_NOT_CORRECT) {
            return "city id not correct";
        } else if (mResult == ERROR_NETWORK_NOT_AVAILABLE) {
            return "network not available";
        } else if (mResult == ERROR_SYSTEM_TIME_NOT_CORRECT) {
            return "system time not correct";
        } else if (mResult == ERROR_UPDATE_WEATHER_FAILED) {
            return "general update weather fail";
        } else {
            return "check impl";
        }
    }
}
