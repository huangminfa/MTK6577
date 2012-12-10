package com.mediatek.location;

import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import com.mediatek.media3d.LogUtil;

public class LocationUpdater implements android.location.LocationListener {
    private static final String TAG = "LocationUpdater";

    public static final int MSG_LOCATION_NOT_FOUND_YET = 0;
    public static final int MSG_CANCELLED_BY_CLIENT = 1;
    public static final int MSG_PROVIDER_ENABLED = 2;
    public static final int MSG_PROVIDER_DISABLED = 3;
    public static final int MSG_PROVIDER_OUT_OF_SERVICE = 4;
    public static final int MSG_PROVIDER_UNAVAILABLE = 5;
    public static final int MSG_PROVIDER_AVALIABLE = 6;

    private LocationManager mLocationManager;
    private LocationListener mListener;
    private Context mContext;
    private int mUpdateInterval;

    private final GeoCoder mGeoCoder = new GeoCoder();

    public LocationUpdater(Context ctx) {
        mContext = ctx;
    }

    public interface LocationListener {
        public void onLocationChanged(LocationEx location);
        public void onStatusChanged(int msg);
    }

    public Location getLastKnownLocation() {
        try {
            if (null != mLocationManager) {
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null) {
                    lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                return lastKnownLocation; 
            }
        } catch (SecurityException ex) {
            LogUtil.v(TAG, "Exception :" + ex);
        } catch (IllegalArgumentException ex) {
            LogUtil.v(TAG, "Exception :" + ex);
        }
        return null;
    }

    public void registerLocationListener(LocationListener listener) {
        mListener = listener;
    }

    public void unregisterLocationListener(LocationListener listener) {
        if (mListener.equals(listener)) {
            mLocationManager.removeUpdates(this);
            mIsRequestLocationUpdate = false;
            mContext = null;
            mListener = null;
        }
    }

    private boolean mIsRequestLocationUpdate;
    private static final long MINUTE = 60000l;

    public void requestLocationUpdates(int interval) {
        mUpdateInterval = interval;
        mIsRequestLocationUpdate = true;
        
        if (null == mLocationManager) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            LogUtil.v(TAG, "location manager : " + mLocationManager);
        }

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, MINUTE * interval,
                10, this);
        } else {
            LogUtil.v(TAG, "Gps provider is disabled.");
        }

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, MINUTE * interval,
                0, this);
        } else {
            LogUtil.v(TAG, "Network provider is disabled.");
        }

        Location lastKnownLocation = getLastKnownLocation();
        LogUtil.v(TAG, "last known location :" + lastKnownLocation);
        if (lastKnownLocation != null) {
            new ReverseGeocodeWoeidTask().execute(lastKnownLocation);
        }
    }

    private class ReverseGeocodeWoeidTask extends AsyncTask<Location, Void, LocationEx> {
        @Override
        protected LocationEx doInBackground(Location... params) {
            return mGeoCoder.getCityFromGeoCode(params[0]);
        }

        @Override
        protected void onPostExecute(LocationEx location) {
            if (isCancelled()) {
                notifyStatusChanged(MSG_CANCELLED_BY_CLIENT);
                return;
            }

            if (location == null) {
                notifyStatusChanged(MSG_LOCATION_NOT_FOUND_YET);
            } else {
                LogUtil.v(TAG, "locationEx : " + location);
                notifyLocationChanged(location);
            }
        }
    }

    private void notifyLocationChanged(LocationEx location) {
        if (mListener != null) {
            LogUtil.v(TAG, "Notify Location Changed : " + location);
            mListener.onLocationChanged(location);
        }
    }

    private void notifyStatusChanged(int msg) {
        if (mListener != null) {
            LogUtil.v(TAG, "Notify Status Changed : " + msg);
            mListener.onStatusChanged(msg);
        }
    }

    // android.location.LocationListener
    public void onLocationChanged(Location location) {
        LogUtil.v(TAG, "Location : " + location);
        new ReverseGeocodeWoeidTask().execute(location);
    }

    public void onProviderDisabled(String provider) {
        if (mIsRequestLocationUpdate) {
            notifyStatusChanged(MSG_PROVIDER_DISABLED);
        }
    }

    public void onProviderEnabled(String provider) {
        if (mIsRequestLocationUpdate) {
            requestLocationUpdates(mUpdateInterval);
            notifyStatusChanged(MSG_PROVIDER_ENABLED);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (mIsRequestLocationUpdate) {
            int result = MSG_PROVIDER_AVALIABLE;
            switch (status) {
            case LocationProvider.OUT_OF_SERVICE :
                result = MSG_PROVIDER_OUT_OF_SERVICE;
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE :
                result = MSG_PROVIDER_UNAVAILABLE;
                break;
            case LocationProvider.AVAILABLE :
                result = MSG_PROVIDER_AVALIABLE;
                break;
            default:
                break;
            }
            notifyStatusChanged(result);
        }
    }
}