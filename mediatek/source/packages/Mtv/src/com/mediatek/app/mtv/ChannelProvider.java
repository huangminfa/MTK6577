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

package com.mediatek.app.mtv;

import com.mediatek.atv.AtvChannelManager;
import com.mediatek.atv.AtvChannelManager.AtvChannelEntry;

import com.mediatek.mtvbase.ChannelManager;
import com.mediatek.mtvbase.ChannelManager.ChannelTableEmpty;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mediatek.xlog.Xlog;
import java.util.Arrays;


public class ChannelProvider implements ChannelManager.ChannelHolder {

    private static final String TAG = "ATV/ChannelProvider";

    private static final String DATABASE_NAME = "channels.db";
    private static final int DATABASE_VERSION = 2;
    private String mCurrTable;    
    private static final int mMtvMode = MtvEngine.MTV_ATV;
    private String[] mAreaCodes;
    private DatabaseHelper mOpenHelper;   
    private static ChannelProvider sChannelProvider = new ChannelProvider();
    public static ChannelProvider instance(Context context,String[] codes){
        if (sChannelProvider.mOpenHelper == null) {
            sChannelProvider.mOpenHelper = sChannelProvider.new DatabaseHelper(context.getApplicationContext());          
        }
        //activity may be restarted so update codes every time.
        if (codes != null) {
            sChannelProvider.mAreaCodes = codes;
        }
        return sChannelProvider;
    }    

    private ChannelProvider() {
        //just to declare a private constructor.
    }    

    public static final String CHANNEL_ENTRY = "chEntry";
    public static final String CHANNEL_NUM = "chNum";
    public static final String CHANNEL_NAME = "chName";
    //public static final String IMAGE_PATH = "imgPath";    
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (int i = 0;i < mAreaCodes.length;i++) {
                if (!mAreaCodes[i].equals("-1")) {                    
                    db.execSQL("CREATE TABLE " + "Channel"+mMtvMode+"_"+mAreaCodes[i] + " ("
                            //use channel number as row ID to get benefits on such as sorting,inserting...
                            + CHANNEL_NUM + " INTEGER PRIMARY KEY,"
                            + CHANNEL_ENTRY + " INTEGER,"                    
                            + CHANNEL_NAME + " TEXT"                    
                            //+ IMAGE_PATH + " TEXT"
                            + ");");
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Xlog.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+mCurrTable);
            onCreate(db);
        }
    }

    public void clear() {    
        Xlog.d(TAG, "clear");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+mCurrTable);
        mOpenHelper.onCreate(db);
    }
    
    public long insert(ContentValues initialValues) {        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.insert(mCurrTable, null, initialValues);
    }

    public int delete(long chNum) {
        Xlog.d(TAG, "delete chNum = "+chNum);             
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();        
        return db.delete(mCurrTable, CHANNEL_NUM+"=" + chNum, null);
    }

    public int update(long chNum, ContentValues values) {
        Xlog.d(TAG, "update chNum = "+chNum);             
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(mCurrTable, values, CHANNEL_NUM+"=" + chNum, null);
    }

    public synchronized void setTableLocation (int mode,String loc) {
        mCurrTable = "Channel"+mode+"_"+loc;
        Xlog.d(TAG, "setTableLocation mCurrTable = "+mCurrTable);     
        
    }

    public synchronized void setTableLocation (String currTable) {
        mCurrTable = currTable;
        Xlog.d(TAG, "setTableLocation mCurrTable = "+mCurrTable);     
        
    }    

    public synchronized String getTableLocation () {
        return mCurrTable;        
    }
    
    public Cursor getCursor(String[] columns,boolean all) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();        
        Cursor c = db.query(mCurrTable, columns, all ? null : CHANNEL_ENTRY+">0", null, null,null, CHANNEL_NUM+" ASC");
        if (c == null) {
            throw new IllegalArgumentException("Unknown fields " + Arrays.toString(columns));
        } else {
            return c;
        }
    }
    
    //handy function for inserting channel info.
    public long insertChannelEntry(int chNum,long entry,String name) {  
        Xlog.d(TAG, "insertChannelEntry() chNum = "+chNum+" entry = "+entry);     
    
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(CHANNEL_ENTRY, entry);
        initialValues.put(CHANNEL_NUM, chNum);
        //default channel name is Ch xx,while xx is the channel number.
        initialValues.put(CHANNEL_NAME,name);
        //initialValues.put(IMAGE_PATH, "");
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.insert(mCurrTable, null,initialValues);
    }
    
    //handy function for updating channel info.
    public int updateChannelEntry (int chNum,long entry) { 
        Xlog.d(TAG, "updateChannelEntry chNum = "+chNum+" entry = "+entry);     
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(CHANNEL_ENTRY, entry);
        return update(chNum,initialValues);
    }    
    
    //handy function for updating channel name.
    public int updateChannelName (int chNum,String name) {    
        Xlog.d(TAG, "updateChannelName chNum = "+chNum+" name = "+name);     
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(CHANNEL_NAME, name);
        return update(chNum,initialValues);
    }     
    
    public synchronized ChannelManager.ChannelEntry[] getChannelTable
        (int mode,ChannelManager manager) throws ChannelTableEmpty {
        if (mode == MtvEngine.MTV_ATV) {
            Cursor cursor = getCursor(new String[]{CHANNEL_NUM,CHANNEL_ENTRY},false);
            if (cursor.getCount() <= 0) {
                throw new ChannelTableEmpty();
            }
            
            AtvChannelEntry[] table = new AtvChannelEntry[cursor.getCount()];
            
            int chIndex = cursor.getColumnIndex(ChannelProvider.CHANNEL_NUM);
            int entryIndex = cursor.getColumnIndex(ChannelProvider.CHANNEL_ENTRY);
            int i = 0;
            
            cursor.moveToFirst();
            do {
                table[i] = ((AtvChannelManager)manager).new AtvChannelEntry();
                table[i].ch = cursor.getInt(chIndex);
                table[i].packedEntry = cursor.getLong(entryIndex);
                i++;
            } while (cursor.moveToNext());
            cursor.close();
            return table;
        }
        
        return new AtvChannelEntry[0];
    }
    
}
