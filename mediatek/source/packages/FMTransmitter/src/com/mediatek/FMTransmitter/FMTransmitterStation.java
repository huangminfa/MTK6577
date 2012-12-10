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

package com.mediatek.FMTransmitter;

import com.mediatek.featureoption.FeatureOption;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class FMTransmitterStation {
    public static final String TAG = "FMTxAPK";
    
    public static final String AUTHORITY  = "com.mediatek.FMTransmitter.FMTransmitterContentProvider";
    public static final String STATION = "TxStation";
    public static final int FIXED_STATION_FREQ = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 10000 : 1000; 
    public static final int HIGHEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 10800 : 1080;
    public static final int LOWEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 8750 : 875;
    public static final String CURRENT_STATION_NAME = "FmTxDfltSttnNm";
    
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
    
    public static void insertStationToDB(Context context, String stationName, int stationFreq, int stationType) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.insertStationToDB");
        ContentValues values = new ContentValues();
        values.put(Station.COLUMN_STATION_NAME, stationName);
        values.put(Station.COLUMN_STATION_FREQ, stationFreq);
        values.put(Station.COLUMN_STATION_TYPE, stationType);
        context.getContentResolver().insert(Station.CONTENT_URI, values);
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.insertStationToDB");
    }
    
    public static void updateStationToDB(Context context, String stationName, int stationFreq, int stationType) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.updateStationToDB");
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
        if (null != cur) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                int iStationFreq = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_FREQ));
                int iStationType = cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE));
                if (iStationType == stationType && iStationFreq == stationFreq) {
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
            values.put(Station.COLUMN_STATION_FREQ, stationFreq);
            values.put(Station.COLUMN_STATION_TYPE, stationType);
            uri = ContentUris.appendId(
                Station.CONTENT_URI.buildUpon(),
                cur.getInt(cur.getColumnIndex(Station._ID))
            ).build();
            context.getContentResolver().update(uri, values, null, null);
        }
        else {
            FMTxLogUtils.e(TAG, "Error: Can not find the station in data base.");
        }

        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.updateStationToDB");
    }
    
    public static void deleteStationInDB(Context context, int stationFreq, int stationType) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.deleteStationInDB");
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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
            context.getContentResolver().delete(uri, null, null);
        }
        else {
            FMTxLogUtils.e(TAG, "Error: Can not find the station in data base.");
        }

        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.deleteStationInDB");
    }
    
    public static boolean isStationExist(Context context, int stationFreq, int stationType) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.isStationExist");
        boolean bRet = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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
            FMTxLogUtils.i(TAG, "Empty database.");
        }

        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.isStationExist: " + bRet);
        return bRet;
    }
    
    public static int getCurrentStation(Context context) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.getCurrentStation");
        int iRet = FIXED_STATION_FREQ;
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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
            FMTxLogUtils.i(TAG, "Empty database.");
        }
        
        if (!bFindInDB) {
            // Insert the default station name and frequency.
            setCurrentStation(context, iRet);
        }

        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.getCurrentStation: " + iRet);
        return iRet;
    }
    
    public static void setCurrentStation(Context context, int iStation) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.setCurrentStation");
        // If the current station already exists, update it; else, insert it.
        boolean bFindInDB = false;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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
            context.getContentResolver().update(uri, values, null, null);
        }
        else {
            context.getContentResolver().insert(Station.CONTENT_URI, values);
        }
        
        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.setCurrentStation");
    }
    
    public static void cleanDB(Context context) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.cleanDB");
        context.getContentResolver().delete(Station.CONTENT_URI, null, null);
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.cleanDB");
    }
    
    public static void cleanSearchedStations(Context context) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.cleanSearchedStations");
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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
                    context.getContentResolver().delete(uri, null, null);
                }
                
                cur.moveToNext();
            }
        }
        
        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.cleanSearchedStations");
    }
    
    public static boolean isDBEmpty(Context context) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.isDBEmpty");
        boolean bRet = true;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
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

        if (null != cur) {
            cur.close();
        }

        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.isDBEmpty: " + bRet);
        return bRet;
    }
    

    public static int getStationCount(Context context, int stationType) {
        FMTxLogUtils.d(TAG, ">>> FMTransmitterStation.getStationCount Type: " + stationType);
        int iRet = 0;
        Uri uri = Station.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(uri, columns, null, null, null);
        if (null != cur) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                if (cur.getInt(cur.getColumnIndex(Station.COLUMN_STATION_TYPE)) == stationType) {
                    iRet++;
                }
                
                cur.moveToNext();
            }
        }

        if (null != cur) {
            cur.close();
        }
        FMTxLogUtils.d(TAG, "<<< FMTransmitterStation.getStationCount: " + iRet);
        return iRet;
    }


}
