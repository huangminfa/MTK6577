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

import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.util.Log;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;

import static com.google.android.mms.pdu.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.google.android.mms.pdu.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;

public class MmsBackupComposer extends Composer {
    private static final Uri[] mMmsUri = {
        Mms.Sent.CONTENT_URI,
        Mms.Outbox.CONTENT_URI,
        Mms.Draft.CONTENT_URI,
        Mms.Inbox.CONTENT_URI
    };
    private Cursor[] mMmsCur = {null, null, null, null};
    private static final String MMSTAG = "Mms:";
    private int mMmsIdx;
    private MmsXmlComposer mXmlComposer;
    private Object mLock = new Object();
    private ArrayList<MmsBackupContent> mPduList = null;
    private ArrayList<MmsBackupContent> mTmpPduList = null;
    private static final int MAX_NUM_PER_TIME = 5;

    public MmsBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_MMS;
	}

    @Override
    public int getCount() {
        int count = 0;
        for (Cursor cur : mMmsCur) {
            if (cur != null && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        Log.d(LogTag.BACKUP, MMSTAG + "getCount():" + count);
        return count;
    }

    @Override
    public boolean init() {
        boolean result = false;
        mTmpPduList = new ArrayList<MmsBackupContent>();
        for (int i = 0; i < mMmsUri.length; ++i) {
            if (mMmsUri[i] == Mms.Inbox.CONTENT_URI) {
                mMmsCur[i] = mContext.getContentResolver().query(mMmsUri[i],
                                                                 null,
                                                                 "m_type <> ?",
                                                                 new String[] {"134"},
                                                                 null);
            } else {
                mMmsCur[i] = mContext.getContentResolver().query(mMmsUri[i], null, null, null, null);
            }
            if (mMmsCur[i] != null) {
                mMmsCur[i].moveToFirst();
                result = true;
            }
        }

        Log.d(LogTag.BACKUP, MMSTAG + "init():" + result + " count:" + getCount());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        for (Cursor cur : mMmsCur) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(LogTag.BACKUP, MMSTAG + "isAfterLast():" + result);
        return result;
    }
    
    public boolean composeOneEntity() {
		return implementComposeOneEntity();
	}

    @Override
    public boolean implementComposeOneEntity() {
        boolean result = false;
        byte[] pduMid;

        for (int i = 0; i < mMmsCur.length; ++i) {
            if (mMmsCur[i] != null && !mMmsCur[i].isAfterLast()) {
                int id = mMmsCur[i].getInt(mMmsCur[i].getColumnIndex("_id"));
                Uri realUri = ContentUris.withAppendedId(mMmsUri[i], id);
                Log.d(LogTag.BACKUP, MMSTAG + "id:" + id + ",realUri:" + realUri);

                PduPersister p = PduPersister.getPduPersister(mContext);
				try {
                    if (mMmsUri[i] == Mms.Inbox.CONTENT_URI) {
                        int type = mMmsCur[i].getInt(mMmsCur[i].getColumnIndex("m_type"));
                        Log.d(LogTag.BACKUP, MMSTAG + "inbox,m_type:" + type);
                        if (type == MESSAGE_TYPE_NOTIFICATION_IND) {
                            NotificationInd nPdu = (NotificationInd)p.load(realUri);
                            pduMid = new PduComposer(mContext, nPdu).make(true);
                        } else if (type == MESSAGE_TYPE_RETRIEVE_CONF) {
                            RetrieveConf rPdu = (RetrieveConf)p.load(realUri, true);
                            pduMid = new PduComposer(mContext, rPdu).make(true);
                        } else {
                            pduMid = null;
                        }
                    } else {
                        SendReq sPdu = (SendReq)p.load(realUri);
                        pduMid = new PduComposer(mContext, sPdu).make();
                    }

                    if (pduMid != null) {
                        String fileName   = "mms/mms" + Integer.toString(mMmsIdx++) + ".pdu";
                        String date       = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("date"));
                        String localDate  = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("date_sent"));
                        String msgBox     = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("msg_box"));
                        String isRead     = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("read"));
                        String st         = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("st"));
                        String size       = mMmsCur[i].getString(mMmsCur[i].getColumnIndex("m_size"));
                        MmsXmlInfo record = new MmsXmlInfo();
                        record.setID(fileName.subSequence(fileName.lastIndexOf("/") + 1, fileName.length()).toString());
                        record.setIsRead(isRead);
                        //record.setLocalDate(localDate);
                        record.setLocalDate(date + "000");
                        record.setST(st);
                        record.setMsgBox(msgBox);
                        record.setDate(date);
                        record.setSize(Integer.toString(pduMid.length));

                        MmsBackupContent tmpContent = new MmsBackupContent();
                        tmpContent.cursorIdx = i;
                        tmpContent.pduMid = pduMid;
                        tmpContent.fileName = fileName;
                        tmpContent.record = record;
                        mTmpPduList.add(tmpContent);
                    }
                    
                    if (mMmsIdx % MAX_NUM_PER_TIME == 0 || mMmsIdx >= getCount()) {
                        if (mPduList != null) {
                            synchronized(mLock) {
                                try {
                                    Log.d(LogTag.BACKUP, MMSTAG + "wait for ZipThread:");
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                        mPduList = mTmpPduList;
                        new ZipThread().start();
                        if (!isAfterLast()) {
                            mTmpPduList = new ArrayList<MmsBackupContent>();
                        }
                    }

                    result = true;
				} catch (InvalidHeaderValueException e) {
		        } catch (MmsException e) {
		        } finally {
                    //mMmsCur[i].moveToNext();
				}

                mMmsCur[i].moveToNext();
                break;
            }
        }

        Log.d(LogTag.BACKUP, MMSTAG + "implementComposeOneEntity:" + result);
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        if ((mXmlComposer = new MmsXmlComposer()) != null) {
            mXmlComposer.startCompose();
        }
    }

    @Override
    public void onEnd() {
        if (mPduList != null) {
            synchronized(mLock) {
                try {
                    Log.d(LogTag.BACKUP, MMSTAG + "wait for ZipThread:");
                    mLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        super.onEnd();
        if (mXmlComposer != null) {
            mXmlComposer.endCompose();
            String msgXmlInfo = mXmlComposer.getXmlInfo();
            if (getComposed() > 0 && msgXmlInfo != null) {
                try {
                    mZipHandler.addFile("mms/msg_box.xml", msgXmlInfo);
                } catch (IOException e) {
                    if (super.mReporter != null) {
                        super.mReporter.onErr(e);
                    }
                }
            }
        }

        for (Cursor cur : mMmsCur) {
            if(cur != null) {
                cur.close();
                cur = null;
            }
        }
    }

    private class ZipThread extends Thread {
        @Override
        public void run() {
            for (int j = 0; (mPduList != null) && (j < mPduList.size()); ++j) {
                byte[] pduMid = mPduList.get(j).pduMid;
                String fileName = mPduList.get(j).fileName;
                Log.d(LogTag.BACKUP, MMSTAG + "ZipThread() pduMid.length:" + pduMid.length);
                try {
                    if (pduMid != null) {
                        mZipHandler.addFile(fileName, pduMid);
                        if (mXmlComposer != null) {
                            mXmlComposer.addOneMmsRecord(mPduList.get(j).record);
                        }

                        increaseComposed();
                        Log.d(LogTag.BACKUP, MMSTAG + "ZipThread() addFile:" + fileName + " success");
                    }
                } catch (IOException e) {
                    if (mReporter != null) {
                        mReporter.onErr(e);
                    }
                    Log.e(LogTag.BACKUP, MMSTAG + "ZipThread() addFile:" + fileName + " fail");
                }
            }

            synchronized (mLock) {
                mPduList = null;
                mLock.notifyAll();
            }
        }
    }

    private class MmsBackupContent {
        public int cursorIdx;
        public byte[] pduMid;
        public String fileName;
        MmsXmlInfo record;
    }

}
