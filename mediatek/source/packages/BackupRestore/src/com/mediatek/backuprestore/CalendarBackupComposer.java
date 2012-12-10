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

package com.mediatek.backuprestore;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import org.apache.http.util.ByteArrayBuffer;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
//import android.provider.Calendar;


public class CalendarBackupComposer extends Composer {

    //private static final String IMPORTER_EVENT_URI = "content://com.mediatek.calendarimporter/events";
    //private static final Uri calanderEventURI = Uri.parse("content://com.android.calendar/events");
    //private static final Uri calanderEventURI = Uri.parse("content://calendar/events");
    //private static final Uri calanderEventURI = Calendar.Events.CONTENT_URI;
    private static final String CALENDARTAG = "Calendar:";
    private static final Uri calanderEventURI = CalendarContract.Events.CONTENT_URI;
    private static final Uri calanderEventURI2 = Uri.parse("content://com.mediatek.calendarimporter/events");
    private int mCalendarIdx;
    private Cursor mCur;

    public CalendarBackupComposer(Context context) {
        super(context);
    }


    @Override
    public int getModuleType() {
		return ModuleType.TYPE_CALENDAR;
	}


    @Override
    public int getCount() {
        int count = 0;

        if (mCur != null) {
            count = mCur.getCount();
        }
        
        Log.d(LogTag.BACKUP, CALENDARTAG + "getCount():" + count);
        return count;
    }


    @Override
    public boolean init() {
        boolean result = true;
        mCalendarIdx = 0;

        mCur = mContext.getContentResolver().query(calanderEventURI, null, null, null, null);

        if (mCur != null) {
            mCur.moveToFirst();
        } else {
            result = false;
        }

        Log.d(LogTag.BACKUP, CALENDARTAG + "init(),result:" + result + ", count:" + (mCur != null ? mCur.getCount() : 0));
        return result;
    }


    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mCur != null) {
            result = mCur.isAfterLast();
        }
        
        Log.d(LogTag.BACKUP, CALENDARTAG + "isAfterLast():" + result);
        return result;
    }


    @Override
    public boolean implementComposeOneEntity() {
        //Log.d(LogTag.BACKUP, CALENDARTAG + "implementComposeOneEntity");
        boolean result = false;

        if (mCur != null && !mCur.isAfterLast()) {
            int id = mCur.getInt(mCur.getColumnIndex("_id"));
            Log.d(LogTag.BACKUP, CALENDARTAG + "id:" + id);

            Uri uri = ContentUris.withAppendedId(calanderEventURI2, id);

            try {
                InputStream input = mContext.getContentResolver().openInputStream(uri);

                ByteArrayBuffer array = new ByteArrayBuffer(1024);
                byte[] temp = new byte[1024];
                int len;
                while ((len = input.read(temp)) > 0) {
                    array.append(temp, 0, len);
                }

                String fileName = "calendar/calendar" + Integer.toString(++mCalendarIdx) + ".vcs";
                Log.d(LogTag.BACKUP, CALENDARTAG + "array:" + array.toByteArray().toString());

                try {
                    mZipHandler.addFile(fileName, array.toByteArray());
                    Log.d(LogTag.BACKUP, CALENDARTAG + "implenmentComposeneBackupInfo() addFile:" + fileName + " succes");
                } catch (IOException e) {
                    if (super.mReporter != null) {
                        super.mReporter.onErr(e);
                    }
                    Log.d(LogTag.BACKUP, CALENDARTAG + "add Zip failed");
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                Log.d(LogTag.BACKUP, CALENDARTAG + "FileNotFoundException"); 
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LogTag.BACKUP, CALENDARTAG + "IOException");
                e.printStackTrace();
            }

            mCur.moveToNext();
            result = true;
        }

        return result;
    }


    @Override
    public void onEnd() {
        super.onEnd();
        if (mCur != null) {
            mCur.close();
            mCur = null;
        }

        Log.d(LogTag.BACKUP, CALENDARTAG + "onEnd");
    }
}
