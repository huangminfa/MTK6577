package com.mediatek.location;

import android.location.Location;
/**
 * This class represents the location support message
 */
public class LocationEx extends Location {
    private long mWoeid;
    private String mCity;
    private boolean mIsTown; // For granularity, locate position to "town" level
    
    public LocationEx(String provider) {
        super(provider);
    }

    public LocationEx(Location l) {
        super(l);
    }
    
    public long getWoeid() {
        return mWoeid;
    }

    public void setWoeid(long woeid) {
        mWoeid = woeid;
    }
    
    public String getCity() {
        return mCity; 
    }
    
    public void setCity(String city) {
        mCity = city;
    }

    public void setAsTown(boolean isTown) {
        mIsTown = isTown;
    }

    public boolean isTown() {
        return mIsTown;
    }
}