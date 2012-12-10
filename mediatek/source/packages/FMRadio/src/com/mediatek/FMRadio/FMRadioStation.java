/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.FMRadio;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.mediatek.featureoption.FeatureOption;
import android.content.Context;
public class FMRadioStation {
    public static final String TAG = "FMRadioStation";
    
    public static final String AUTHORITY  = "com.mediatek.FMRadio.FMRadioContentProvider";
    public static final String STATION = "station";
    public static final int FIXED_STATION_FREQ = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 10000 : 1000; // 1000 * 100k Hz
    public static final int HIGHEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 10800 : 1080;
    public static final int LOWEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 8750 :875;
    public static final String CURRENT_STATION_NAME = "FmDfltSttnNm";
    
    // Station types.
    public static final int STATION_TYPE_CURRENT = 1;
    public static final int STATION_TYPE_FAVORITE = 2;
    public static final int STATION_TYPE_SEARCHED = 3;
    public static final int STATION_TYPE_RDS_SETTING = 4;

    // RDS setting items
    public static final int RDS_SETTING_FREQ_PSRT = 1;
    public static final int RDS_SETTING_FREQ_AF = 2;
    public static final int RDS_SETTING_FREQ_TA = 3;

    // RDS setting values for every item
    public static final String RDS_SETTING_VALUE_ENABLED = "ENABLED";
    public static final String RDS_SETTING_VALUE_DISABLED = "DISABLED";
    
    // The max count of favorite stations.
    public static final int MAX_FAVORITE_STATION_COUNT = 5;

    static final String columns[] = new String[] {
        Station._ID,
        Station.COLUMN_STATION_NAME,
        Station.COLUMN_STATION_FREQ,
        // Use this type to identify different stations.
        Station.COLUMN_STATION_TYPE
    };

    // BaseColumn._ID = "_id"
    public static final class Station implements BaseColumns {
        public static final Uri CONTENT_URI  = Uri.parse("content://"+ AUTHORITY + "/" + STATION);
        // Extra columns of the table: COLUMN_STATION_NAME COLUMN_STATION_FREQ COLUMN_STATION_TYPE
        public static final String COLUMN_STATION_NAME = "COLUMN_STATION_NAME";
        public static final String COLUMN_STATION_FREQ = "COLUMN_STATION_FREQ";
        public static final String COLUMN_STATION_TYPE = "COLUMN_STATION_TYPE";
    }
    
    public static void insertStationToDB(Activity activity, String stationName, int stationFreq, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.insertStationToDB");
        ContentValues values = new ContentValues();
        values.put(Station.COLUMN_STATION_NAME, stationName);
        values.put(Station.COLUMN_STATION_FREQ, stationFreq);
        values.put(Station.COLUMN_STATION_TYPE, stationType);
        activity.getContentResolver().insert(Station.CONTENT_URI, values);
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.insertStationToDB");
    }
    
    public static void updateStationToDB(Activity activity, String stationName, int oldStationFreq, int newStationFreq, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.updateStationToDB");
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (iStationType == stationType && iStationFreq == oldStationFreq) {
                        // Have find the current station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            if (bFindInDB) {
                ContentValues values = new ContentValues();
                values.put(Station.COLUMN_STATION_NAME, stationName);
                values.put(Station.COLUMN_STATION_FREQ, newStationFreq);
                values.put(Station.COLUMN_STATION_TYPE, stationType);
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                activity.getContentResolver().update(uri, values, null, null);
            }
            else {
                FMRadioLogUtils.e(TAG, "Error: Can not find the station in data base.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.updateStationToDB");
    }
    
    public static void deleteStationInDB(Activity activity, int stationFreq, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.deleteStationInDB");
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (iStationType == stationType && iStationFreq == stationFreq) {
                        // Have find the station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            if (bFindInDB) {
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                activity.getContentResolver().delete(uri, null, null);
            }
            else {
                FMRadioLogUtils.e(TAG, "Error: Can not find the station in data base.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.deleteStationInDB");
    }
    
    public static boolean isStationExist(Activity activity, int stationFreq, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.isStationExist");
        boolean bRet = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (iStationType == stationType && stationFreq == iStationFreq) {
                        // The station exists.
                        bRet = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.isStationExist: " + bRet);
        return bRet;
    }
    
    public static int getCurrentStation(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getCurrentStation");
        int iRet = FIXED_STATION_FREQ;
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_CURRENT == iStationType) {
                        // Have find the current station.
                        iRet = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                        if (iRet < LOWEST_STATION || iRet > HIGHEST_STATION) {
                            //... Update the database?
                            iRet = FIXED_STATION_FREQ;
                        }
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
            
            if (!bFindInDB) {
                // Insert the default station name and frequency.
                setCurrentStation(activity, iRet);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getCurrentStation: " + iRet);
        return iRet;
    }
    
    public static void setCurrentStation(Activity activity, int iStation) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.setCurrentStation");
        // If the current station already exists, update it; else, insert it.
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_CURRENT == iStationType) {
                        // Have find the current station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            ContentValues values = new ContentValues();
            values.put(Station.COLUMN_STATION_NAME, CURRENT_STATION_NAME);
            values.put(Station.COLUMN_STATION_FREQ, iStation);
            values.put(Station.COLUMN_STATION_TYPE, STATION_TYPE_CURRENT);
            if (bFindInDB) {
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                // String selection = Station.COLUMN_STATION_NAME + "=" + CURRENT_STATION_NAME;
                // getContentResolver().update(Station.CONTENT_URI, values, selection, null);
                activity.getContentResolver().update(uri, values, null, null);
            }
            else {
                activity.getContentResolver().insert(Station.CONTENT_URI, values);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.setCurrentStation");
    }
    
    public static void cleanDB(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.cleanDB");
        activity.getContentResolver().delete(Station.CONTENT_URI, null, null);
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.cleanDB");
    }
    
    public static void cleanSearchedStations(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.cleanSearchedStations");
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_SEARCHED == iStationType) {
                        // Have find one station.
                        uri = ContentUris.appendId(
                                Station.CONTENT_URI.buildUpon(),
                                cur.getInt(cur.getColumnIndex(Station._ID))
                        ).build();
                        activity.getContentResolver().delete(uri, null, null);
                    }
                    
                    cur.moveToNext();
                }
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.cleanSearchedStations");
    }
    
    public static boolean isDBEmpty(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.isDBEmpty");
        boolean bRet = true;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_CURRENT != iStationType) {
                        // Have find one station and it's not current station.
                        bRet = false;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.isDBEmpty: " + bRet);
        return bRet;
    }
    
    public static String getStationName(Activity activity, int stationFreq, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getStationName");
        String sRet = null;
        boolean IsFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    String sStationName = cur.getString(cur.getColumnIndex(Station.COLUMN_STATION_NAME));
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (iStationType == stationType && iStationFreq == stationFreq) {
                        // Have find the station.
                        sRet = sStationName;
                        IsFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
            
            if (!IsFindInDB) {
                // Set the default station name.
                sRet = activity.getString(R.string.default_station_name);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getStationName: " + sRet);
        return sRet;
    }
    
    public static boolean isFavoriteStation(Activity activity, int iStation) {
        return isStationExist(activity, iStation, STATION_TYPE_FAVORITE);
    }
    
    public static int getStationCount(Activity activity, int stationType) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getStationCount Type: " + stationType);
        int iRet = 0;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    if (cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE)) == stationType) {
                        iRet++;
                    }
                    
                    cur.moveToNext();
                }
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getStationCount: " + iRet);
        return iRet;
    }

    public static void setEnablePSRT(Activity activity, boolean enable) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.setEnablePSRT: " + enable);
        // If the item already exists, update it; else, insert it.
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_PSRT == iStationFreq) {
                        // Have find the current station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            ContentValues values = new ContentValues();
            if (enable) {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_ENABLED);
            }
            else {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_DISABLED);
            }
            values.put(Station.COLUMN_STATION_FREQ, RDS_SETTING_FREQ_PSRT);
            values.put(Station.COLUMN_STATION_TYPE, STATION_TYPE_RDS_SETTING);
            if (bFindInDB) {
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                activity.getContentResolver().update(uri, values, null, null);
            }
            else {
                activity.getContentResolver().insert(Station.CONTENT_URI, values);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.setEnablePSRT");
    }

    public static void setEnableAF(Activity activity, boolean enable) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.setEnableAF: " + enable);
        // If the item already exists, update it; else, insert it.
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_AF == iStationFreq) {
                        // Have find the current station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            ContentValues values = new ContentValues();
            if (enable) {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_ENABLED);
            }
            else {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_DISABLED);
            }
            values.put(Station.COLUMN_STATION_FREQ, RDS_SETTING_FREQ_AF);
            values.put(Station.COLUMN_STATION_TYPE, STATION_TYPE_RDS_SETTING);
            if (bFindInDB) {
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                activity.getContentResolver().update(uri, values, null, null);
            }
            else {
                activity.getContentResolver().insert(Station.CONTENT_URI, values);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.setEnableAF");
    }

    public static void setEnableTA(Activity activity, boolean enable) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.setEnableTA: " + enable);
        // If the item already exists, update it; else, insert it.
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_TA == iStationFreq) {
                        // Have find the current station.
                        bFindInDB = true;
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            
            ContentValues values = new ContentValues();
            if (enable) {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_ENABLED);
            }
            else {
                values.put(Station.COLUMN_STATION_NAME, RDS_SETTING_VALUE_DISABLED);
            }
            values.put(Station.COLUMN_STATION_FREQ, RDS_SETTING_FREQ_TA);
            values.put(Station.COLUMN_STATION_TYPE, STATION_TYPE_RDS_SETTING);
            if (bFindInDB) {
                uri = ContentUris.appendId(
                    Station.CONTENT_URI.buildUpon(),
                    cur.getInt(cur.getColumnIndex(Station._ID))
                ).build();
                activity.getContentResolver().update(uri, values, null, null);
            }
            else {
                activity.getContentResolver().insert(Station.CONTENT_URI, values);
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.setEnableTA");
    }

    public static boolean getEnablePSRT(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getEnablePSRT");
        boolean bRet = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_PSRT == iStationFreq) {
                        // The station exists.
                        String sStationName = cur.getString(cur.getColumnIndex(Station.COLUMN_STATION_NAME));
                        if (sStationName.equalsIgnoreCase(RDS_SETTING_VALUE_ENABLED)) {
                            bRet = true;
                        }
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getEnablePSRT: " + bRet);
        return bRet;
    }

    public static boolean getEnableAF(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getEnableAF");
        boolean bRet = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_AF == iStationFreq) {
                        // The station exists.
                        String sStationName = cur.getString(cur.getColumnIndex(Station.COLUMN_STATION_NAME));
                        if (sStationName.equalsIgnoreCase(RDS_SETTING_VALUE_ENABLED)) {
                            bRet = true;
                        }
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getEnableAF: " + bRet);
        return bRet;
    }

    public static boolean getEnableTA(Activity activity) {
        FMRadioLogUtils.d(TAG, ">>> FMRadioStation.getEnableTA");
        boolean bRet = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = activity.getContentResolver().query(uri, columns, null, null, null);
        try {
            if (null != cur) {
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                    int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                    if (STATION_TYPE_RDS_SETTING == iStationType
                        && RDS_SETTING_FREQ_TA== iStationFreq) {
                        // The station exists.
                        String sStationName = cur.getString(cur.getColumnIndex(Station.COLUMN_STATION_NAME));
                        if (sStationName.equalsIgnoreCase(RDS_SETTING_VALUE_ENABLED)) {
                            bRet = true;
                        }
                        break;
                    }
                    
                    cur.moveToNext();
                }
            }
            else {
                // Empty table.
                FMRadioLogUtils.d(TAG, "Empty database.");
            }
        } finally {
            if (null != cur) {
                cur.close();
            }
        }
        
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.getEnableTA: " + bRet);
        return bRet;
    }
    
    public static void cleanAllStations(Context context) {
	    FMRadioLogUtils.d(TAG, ">>> FMRadioStation.cleanAllStations");
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
        if (null != cur) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                    // Have find one station.
                uri = ContentUris.appendId(Station.CONTENT_URI.buildUpon(), 
                        cur.getInt(cur.getColumnIndex(Station._ID))).build();
                context.getContentResolver().delete(uri, null, null);
                
                cur.moveToNext();
            }
        }
        
        if (null != cur) {
            cur.close();
        }
        FMRadioLogUtils.d(TAG, "<<< FMRadioStation.cleanAllStations");
    }
    
}
