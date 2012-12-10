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
import java.util.zip.ZipFile;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.WapPush;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import android.content.ContentProviderOperation;

public class SmsRestoreComposer extends Composer {
    private static final String SMSTAG = "SMS:";
    private static final Uri[] mSmsUri = {
        Sms.Inbox.CONTENT_URI,  
        Sms.Sent.CONTENT_URI,   
        Sms.Draft.CONTENT_URI,
        Sms.Outbox.CONTENT_URI
    };
    //private final int timeLength = 13;
    private final int readLenth = 1;
    private final int seenLenth = 1;
    private final int boxLength = 1;
    private final int simcardLength = 1;
    private int mboxType;
    private ArrayList<String> mFileNameList;
    private int mIdx;
    private long mTime;
    private ZipFile mZipFile;
    private ArrayList<ContentProviderOperation> mOperationList;
    private static final int MAX_OPERATIONS_PER_BATCH = 20;

	public SmsRestoreComposer(Context context) {
		super(context);
	}

    @Override
    public int getModuleType() {
		return ModuleType.TYPE_SMS;
	}

    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }

        Log.d(LogTag.RESTORE, SMSTAG + "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;

        Log.d(LogTag.RESTORE, SMSTAG + "begin init:" + System.currentTimeMillis());
        mFileNameList = new ArrayList<String>();
        mOperationList = new ArrayList<ContentProviderOperation>();
        try {
            mTime = System.currentTimeMillis();
            mFileNameList = (ArrayList<String>)BackupZip.GetFileList(mZipFileName, true, true, "sms/sms[0-9]+");
            mZipFile = BackupZip.getZipFileFromFileName(mZipFileName);
            result = true;
        } catch (IOException e) {
        }

        Log.d(LogTag.RESTORE, SMSTAG + "end init:" + System.currentTimeMillis());
        Log.d(LogTag.RESTORE, SMSTAG + "init():" + result + ",count:" + mFileNameList.size());
        return result;
    }


    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIdx >= mFileNameList.size()) ? true : false;
        }

        Log.d(LogTag.RESTORE, SMSTAG + "isAfterLast():" + result);
        return result;
    }


    @Override
    public boolean implementComposeOneEntity() {
        Log.d(LogTag.RESTORE, SMSTAG + "begin readFileContent:" + System.currentTimeMillis());

        Log.d(LogTag.RESTORE, SMSTAG + "mZipFileName:" + mZipFileName + ", mIdx:" + mIdx);

        boolean result = false;
        String pduFileName = mFileNameList.get(mIdx++);
        byte[] storePdu = BackupZip.readFileContent(mZipFile, pduFileName);
        Log.d(LogTag.RESTORE, SMSTAG + "end readFileContent:" + System.currentTimeMillis());
        if (storePdu != null) {
            Log.d(LogTag.RESTORE, SMSTAG + "begin parse:" + System.currentTimeMillis());
            ContentValues values = parsePdu(storePdu);
            Log.d(LogTag.RESTORE, SMSTAG + "end parse:" + System.currentTimeMillis());
            if (values == null){
                Log.d(LogTag.RESTORE, SMSTAG + "parsePdu():values=null");
            } else {
                Log.d(LogTag.RESTORE, SMSTAG + "mboxType:" + mboxType);
                Log.d(LogTag.RESTORE, SMSTAG + "begin restore:" + System.currentTimeMillis());
                if (isAfterLast()) {
                    values.remove("import_sms");
                }


                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(mSmsUri[mboxType - 1]);
                builder.withValues(values);
                mOperationList.add(builder.build());
                if ((mIdx % MAX_OPERATIONS_PER_BATCH != 0) && !isAfterLast()) {
                    return true;
                }

                if (mOperationList.size() > 0) {
                    try {
                        mContext.getContentResolver().applyBatch("sms", mOperationList);
                    } catch (android.os.RemoteException e) {
                        //Xlog.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (android.content.OperationApplicationException e) {
                        //Xlog.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } finally {
                        mOperationList.clear();
                    }
                }
                // if (mboxType <= mSmsUri.length) {
                //     SqliteWrapper.insert(mContext, mContext.getContentResolver(), mSmsUri[mboxType - 1], values);
                // }

                Log.d(LogTag.RESTORE, SMSTAG + "end restore:" + System.currentTimeMillis());
                result = true;
            }
        } else {
            if (super.mReporter != null) {
                super.mReporter.onErr(new IOException());
            }
        }

        return result;
    }


    private ContentValues parsePdu(byte[] pdu) {
        // if (pdu == null) {
        //     return null;
        // }

        try {
            int curIndex = 0;
            int timeLength = pdu[curIndex++] & 0xff;
            String timeStamp = new String(pdu, curIndex, timeLength);
            curIndex += timeLength;
            String readStamp = new String(pdu, curIndex, readLenth);
            curIndex += readLenth;
            String seenStamp = new String(pdu, curIndex, seenLenth);
            curIndex += seenLenth;
            String boxStamp = new String(pdu, curIndex, boxLength);
            mboxType = pdu[curIndex] - 0x30;
            curIndex += boxLength;
            String simcardStamp = new String(pdu, curIndex, simcardLength);
            curIndex += simcardLength;
            simcardStamp = simcardStamp.equals("-") ? "-1" : simcardStamp;
            Log.d(LogTag.RESTORE, SMSTAG + "parsePdu timeStamp:"+ timeStamp + 
                  "timeLength:" + timeLength +
                  ",readStamp:" + readStamp +
                  ",boxStamp:" + boxStamp +
                  ",seenStamp:" + seenStamp + 
                  ",simcardStamp:" + simcardStamp);
            int num = pdu[curIndex++] & 0xff;
            byte[] pduLens = new byte[num];
            System.arraycopy(pdu, curIndex, pduLens, 0, num);
            curIndex += num;
            
            StringBuilder bodyBuilder = new StringBuilder();
            ContentValues values = null;
            for (int i = 0; i < num; ++i) {
                int tmpLen = pduLens[i] & 0xff;
                byte[] rawPdu = new byte[tmpLen];
                System.arraycopy(pdu, curIndex, rawPdu, 0, tmpLen);
                curIndex += tmpLen;
                Log.d(LogTag.RESTORE, SMSTAG + "begin createFromPdu:" + System.currentTimeMillis());
                SmsMessage msg = SmsMessage.createFromPdu(rawPdu);
                Log.d(LogTag.RESTORE, SMSTAG + "end createFromPdu:" + System.currentTimeMillis());
                if (msg != null) {
                    bodyBuilder.append(msg.getDisplayMessageBody());
                    if (i == 0) {
                        Log.d(LogTag.RESTORE, SMSTAG + "begin extractContentValues:" + System.currentTimeMillis());
                        values = extractContentValues(msg);
                        Log.d(LogTag.RESTORE, SMSTAG + "end extractContentValues:" + System.currentTimeMillis());
                    }
                } else {
                    Log.d(LogTag.RESTORE, SMSTAG + "createFromPdu is null");
                }
            }

            if (values != null) {
                values.put(Sms.BODY, bodyBuilder.toString());
                values.put(Sms.READ, readStamp);
                values.put(Sms.SEEN, seenStamp);
                values.put(Sms.SIM_ID, simcardStamp);
                values.put(Sms.DATE, timeStamp);
                values.put(Sms.TYPE, boxStamp);
                values.put("import_sms", true);
            }

            return values;
        } catch(ArrayIndexOutOfBoundsException e) {
            Log.d(LogTag.RESTORE, SMSTAG + "out of bounds");
        }

        return null;
    }

    private ContentValues extractContentValues(SmsMessage sms) {
        ContentValues values = new ContentValues();
        values.put(Sms.PROTOCOL, sms.getProtocolIdentifier());

        if (sms.getPseudoSubject().length() > 0) {
            values.put(Sms.SUBJECT, sms.getPseudoSubject());
        }

        String address = sms.getDestinationAddress();
        // Log.d(LogTag.RESTORE, SMSTAG +
        //       "getOriginatingAddress():" + sms.getOriginatingAddress() +
        //       ",getDestinationAddress():" + sms.getDestinationAddress() +
        //       ",getServiceCenterAddress:" + sms.getServiceCenterAddress());
        if (TextUtils.isEmpty(address)) {
            address = "unknown";
        }
        //Log.d(LogTag.RESTORE, SMSTAG + "getNewContentValues address:" + address);
        values.put(Sms.ADDRESS, address);
        values.put(Sms.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Sms.SERVICE_CENTER, sms.getServiceCenterAddress());
        //values.put(Sms.BODY, sms.getDisplayMessageBody());

        // Long threadId = values.getAsLong(Sms.THREAD_ID);
        // if (threadId == null || threadId == 0) {
        //      try {
        //          Log.d(LogTag.RESTORE, SMSTAG + "begin extractContentValues5:" + System.currentTimeMillis());
        //          threadId = Threads.getOrCreateThreadId(mContext, address);
        //          Log.d(LogTag.RESTORE, SMSTAG + "end   extractContentValues5:" + System.currentTimeMillis());                 
        //      } catch (IllegalArgumentException iae) {
        //          Log.e(LogTag.RESTORE, SMSTAG + "getOrCreateThreadId failed for this time");
        //      }
        // }
        // Log.d(LogTag.RESTORE, SMSTAG + "threadId:" + threadId);
        // values.put(Sms.THREAD_ID, threadId);

        return values;
    }


	private boolean deleteAllPhoneSms() {
        boolean result = false;
		if (mContext != null) {
            Log.d(LogTag.RESTORE, SMSTAG + "begin delete:" + System.currentTimeMillis());
            int count = mContext.getContentResolver().delete(Uri.parse("content://sms/"),
                                                             "type <> ?",
                                                             new String[]{"1"});
            count += mContext.getContentResolver().delete(Uri.parse("content://sms/"),
                                                          "date < ?",
                                                          new String[]{Long.toString(mTime)});

            int count2 = mContext.getContentResolver().delete(WapPush.CONTENT_URI,
                                                              null,
                                                              null);
            Log.d(LogTag.RESTORE, SMSTAG + "deleteAllPhoneSms():" + count + " sms deleted!" + count2 + "wappush deleted!");
            result = true;
            Log.d(LogTag.RESTORE, SMSTAG + "end delete:" + System.currentTimeMillis());
        }

		return result;
	}


    @Override
    public void onStart() {
        super.onStart();
        deleteAllPhoneSms();

        Log.d(LogTag.RESTORE, SMSTAG + "onStart()");
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }

        if (mOperationList != null) {
            mOperationList = null;
        }

        mZipFile = null;
        Log.d(LogTag.RESTORE, SMSTAG + "onEnd()");
    }
}
