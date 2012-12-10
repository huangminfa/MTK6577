package com.mediatek.videoplayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.text.format.Formatter;

public class CachedVideoInfo {
    private static final String TAG = "CachedVideoInfo";
    private static final boolean LOG = true;
    
    private Locale mLocale;
    private HashMap<Long, String> mDurations = new HashMap<Long, String>();
    private HashMap<Long, String> mDateTimes = new HashMap<Long, String>();
    private HashMap<Long, String> mFileSizes = new HashMap<Long, String>();
    private boolean mCanOpitmized;
    private ArrayList<Locale> mCanOptimizedLocales = new ArrayList<Locale>();
    
    public CachedVideoInfo() {
        mCanOptimizedLocales.add(Locale.ENGLISH);
        mCanOptimizedLocales.add(Locale.CHINA);
        mCanOptimizedLocales.add(Locale.TAIWAN);
        mCanOptimizedLocales.add(Locale.UK);
        mCanOptimizedLocales.add(Locale.US);
        mCanOptimizedLocales.add(Locale.FRANCE);
        mCanOptimizedLocales.add(Locale.GERMANY);
        mCanOptimizedLocales.add(Locale.ITALY);
        setLocale(Locale.getDefault());
    }
    
    public synchronized void setLocale(Locale locale) {
        if (LOG) MtkLog.v(TAG, "setLocale(" + locale + ") mLocale=" + mLocale + ", mCanOpitmized=" + mCanOpitmized);
        if (locale == null) {
            mDateTimes.clear();
            mDurations.clear();
            mFileSizes.clear();
        } else {
            if (locale.equals(mLocale)) {
                //do nothing
            } else {
                mLocale = locale;
                mDateTimes.clear();
                mFileSizes.clear();
                boolean newOptimized = false;
                if (mCanOptimizedLocales.contains(mLocale)) {
                    newOptimized = true;
                }
                if (mCanOpitmized == true && newOptimized == true) {
                    //use old cached duration
                } else {
                    mCanOpitmized = newOptimized;
                    mDurations.clear();
                }
            }
        }
        if (LOG) MtkLog.v(TAG, "setLocale() mCanOpitmized=" + mCanOpitmized);
    }
    
    public synchronized String getFileSize(Context context, Long size) {
        String fileSize = mFileSizes.get(size);
        if (fileSize == null) {
            fileSize = Formatter.formatFileSize(context, size);
            mFileSizes.put(size, fileSize);
        }
        return fileSize;
    }
    
    public synchronized String getTime(Long millis) {
        String time = mDateTimes.get(millis);
        if (time == null) {
            time = MtkUtils.localTime(millis);
            mDateTimes.put(millis, time);
        }
        return time;
    }
    
    public synchronized String getDuration(Long millis) {
        String duration = mDurations.get(millis);
        if (duration == null) {
            if (mCanOpitmized) {
                duration = stringForDurationOptimized(millis);
            } else {
                duration = MtkUtils.stringForTime(millis);
            }
            mDurations.put(millis, duration);
        }
        return duration;
    }
    
    private String stringForDurationOptimized(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        //optimize format time, but not support special language
        StringBuilder builder = new StringBuilder(10);
        if (hours > 0) {
            builder.append(hours)
            .append(":");
        }
        if (minutes < 10) {
            builder.append("0");
        }
        builder.append(minutes);
        builder.append(":");
        if (seconds < 10) {
            builder.append("0");
        }
        builder.append(seconds);
        return builder.toString();
    }
}
