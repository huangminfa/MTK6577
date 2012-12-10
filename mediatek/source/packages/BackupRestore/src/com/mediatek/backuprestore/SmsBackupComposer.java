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

import java.util.Iterator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.SubmitPdu;
import java.util.List;
import java.util.ArrayList;
import com.mediatek.backuprestore.BackupZip;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.Composer;
import com.mediatek.backuprestore.ProgressReporter;
import android.provider.Telephony.Sms;
import java.io.IOException;


public class SmsBackupComposer extends Composer {
    private byte [] timePdu;
    //private final int timeLength = 13;
    private final int readLenth = 1;
    private final int seenLenth = 1;    
    private final int simcardLength = 1;
    private final int boxLength = 1;
    private final String SMSTAG = "SMS:";
    private int mSmsIdx;

    private static final Uri[] mSmsUri = {
        Sms.Inbox.CONTENT_URI,  
        Sms.Sent.CONTENT_URI,   
        Sms.Outbox.CONTENT_URI,
        Sms.Draft.CONTENT_URI
    };
    private Cursor[] mSmsCur = {null, null, null, null};


	public SmsBackupComposer(Context context) {
		super(context);
	}


    @Override
    public int getModuleType() {
		return ModuleType.TYPE_SMS;
	}


	@Override
    public int getCount() {
        int count = 0;
        for (Cursor cur : mSmsCur) {
            if (cur != null && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        Log.d(LogTag.BACKUP, SMSTAG + "getCount():" + count);
        return count;
	}


	@Override
	public boolean init() {
		boolean result = false;
        for (int i = 0; i < mSmsUri.length; ++i) {
            mSmsCur[i] = mContext.getContentResolver().query(mSmsUri[i], null, null, null, null);
            if (mSmsCur[i] != null) {
                mSmsCur[i].moveToFirst();
                result = true;
            }
        }

        Log.d(LogTag.BACKUP, SMSTAG + "init():" + result + ",count:" + getCount());
		return result;
	}


	@Override
	public boolean isAfterLast() {
		boolean result = true;
        for (Cursor cur : mSmsCur) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(LogTag.BACKUP, SMSTAG + "isAfterLast():" + result);
		return result;
	}


	@Override
    public boolean implementComposeOneEntity() {
        boolean result = false;

        for (int i = 0; i < mSmsCur.length; ++i) {
            if (mSmsCur[i] != null && !mSmsCur[i].isAfterLast()) {
                Cursor tmpCur = mSmsCur[i];

                String timeStamp = tmpCur.getString(tmpCur.getColumnIndex("date"));
                if (timeStamp == null) {
                    timeStamp = "";
                }
                timePdu = timeStamp.getBytes();

                String readByte = tmpCur.getString(tmpCur.getColumnIndex("read"));
                byte[] readPdu = readByte.getBytes(); // read

                String seenByte = tmpCur.getString(tmpCur.getColumnIndex("seen"));
                byte[] seenPdu = null;
                if (seenByte != null) {
                    seenPdu = seenByte.getBytes(); //seen
                }

                String boxType = tmpCur.getString(tmpCur.getColumnIndex("type"));
                byte[] boxtypePdu = boxType.getBytes(); // boxtype

                String simCardid = tmpCur.getString(tmpCur.getColumnIndex("sim_id"));
                byte[] simCardidPdu = simCardid.getBytes(); // simCardidPdu

                String smsAddress = null;
                if (i == 3) {
                    String threadId = tmpCur.getString(tmpCur.getColumnIndex("thread_id"));
                    Cursor draftCursor = mContext
                        .getContentResolver()
                        .query(Uri.parse("content://sms"), 
                               new String[] {"canonical_addresses.address " +
                                             "from sms,threads,canonical_addresses " +
                                             "where sms.thread_id=threads._id and threads.recipient_ids=canonical_addresses._id and sms.thread_id =" + threadId + " --"},
                               null,
                               null,
                               null);

                    if (draftCursor != null && draftCursor.moveToFirst()) {
                        smsAddress = draftCursor.getString(draftCursor.getColumnIndex("address"));
                        draftCursor.close();
                    }
                } else {
                     smsAddress = tmpCur.getString(tmpCur.getColumnIndex("address"));
                }

                if (smsAddress == null) {
                    smsAddress = "";
                }
                String sc = tmpCur.getString(tmpCur.getColumnIndex("service_center"));
                String body = tmpCur.getString(tmpCur.getColumnIndex("body"));

                // Log.d(LogTag.BACKUP, SMSTAG +
                //       //"timeStamp:" + (timeStamp == null ? "" : timeStamp) +
                //       //",timePdu:" + timePdu.toString() + 
                //       ",read:" + readByte +
                //       ",type:" + boxType +
                //       ",sim_id:" + simCardid +
                //       ",address:" + smsAddress +
                //       ",service_center:" + sc +
                //       //",body:" + body +
                //       ",seen:" + seenByte);

                ArrayList<String> bodyArray = SmsMessage.fragmentText(body);
                ArrayList<byte[]> pduScArray = new ArrayList<byte[]>();
                ArrayList<byte[]> pduMsgArray = new ArrayList<byte[]>();
                for (int k = 0; k < bodyArray.size(); ++k) {
                    SubmitPdu tmpPdu = SmsMessage.getSubmitPdu(sc, smsAddress, bodyArray.get(k), false);
                    if (tmpPdu.encodedScAddress != null) {
                        pduScArray.add(tmpPdu.encodedScAddress);
                    } else {
                        pduScArray.add(new byte[]{0});
                    }
                    if (tmpPdu.encodedMessage != null) {
                        pduMsgArray.add(tmpPdu.encodedMessage);
                    } else {
                        pduMsgArray.add(new byte[]{0});
                    }
                }

                byte[] storePdu = combinePdu(timePdu, readPdu, seenPdu, boxtypePdu, simCardidPdu, pduScArray, pduMsgArray);

                // String tmpString = "storePdu:[";
                // for (int j = 0; j < storePdu.length; ++j) {
                //     tmpString += (storePdu[j] + ",");
                // }
                // tmpString += "]";
                // Log.d(LogTag.BACKUP, SMSTAG + tmpString);

                String fileName = "sms/sms" + Integer.toString(++mSmsIdx);
                Log.d(LogTag.BACKUP, SMSTAG + "FileName:" + fileName);

                try {
                    mZipHandler.addFile(fileName, storePdu);
                    Log.d(LogTag.BACKUP, SMSTAG + "add zip file:" + fileName + " success");
                } catch (IOException e) {
                    if (super.mReporter != null) {
                        super.mReporter.onErr(e);
                    }
                    Log.d(LogTag.BACKUP, SMSTAG + "add sms zip failed");
                }

                tmpCur.moveToNext();
                result = true;
                break;
            }
        }

		return result;
	}


    private byte[] combinePdu(byte[] time, byte[] read, byte[] seen, byte[] box, byte[] simcard,
                              ArrayList<byte[]> smscArray, ArrayList<byte[]> bodyArray) {
        int curIndex = 0;
        int pduLen = 0;
        for (int i = 0; i < smscArray.size(); ++i) {
            pduLen += (smscArray.get(i).length + bodyArray.get(i).length);
        }

        byte[] msgPdu = new byte[(time.length + 1) + readLenth + seenLenth + boxLength + simcardLength +
                                 (smscArray.size() + 1) + pduLen];
        try {
            //copy time zone
            if (time != null && time.length > 0) {
                msgPdu[curIndex++] = (byte)time.length;
                System.arraycopy(time, 0, msgPdu, curIndex, (byte)time.length);
                curIndex += (byte)time.length;
            } else {
                msgPdu[curIndex++] = 0;
                Log.w(LogTag.BACKUP, SMSTAG + "time is null");
            }

            // copy read zone
            if (read != null) {
                System.arraycopy(read, 0, msgPdu, curIndex, readLenth);
            } else {
                Log.w(LogTag.BACKUP, SMSTAG + "read is null");
            }
            curIndex += readLenth;

            if (seen != null) {
                System.arraycopy(seen, 0, msgPdu, curIndex, seenLenth);
            } else {
                Log.w(LogTag.BACKUP, SMSTAG + "seen is null");
            }
            curIndex += seenLenth;

            // copy box type zone
            if (box != null) {
                System.arraycopy(box, 0, msgPdu, curIndex, boxLength);
            } else {
                msgPdu[curIndex] = 0;
                Log.w(LogTag.BACKUP, SMSTAG + "box is null");
            }
            curIndex += boxLength;

            // copy simcard zone
            if (simcard != null) {
                System.arraycopy(simcard, 0, msgPdu, curIndex, simcardLength);
            } else {
                Log.w(LogTag.BACKUP, SMSTAG + "simcard is null");
            }
            curIndex += simcardLength;

            msgPdu[curIndex++] = (byte)(smscArray.size());
            for (int i = 0; i < smscArray.size(); ++i) {
                msgPdu[curIndex++] = (byte)(smscArray.get(i).length + bodyArray.get(i).length);
            }

            for (int i = 0; i < smscArray.size(); ++i) {
                System.arraycopy(smscArray.get(i), 0, msgPdu, curIndex, smscArray.get(i).length);
                curIndex += smscArray.get(i).length;
                System.arraycopy(bodyArray.get(i), 0, msgPdu, curIndex, bodyArray.get(i).length);
                curIndex += bodyArray.get(i).length;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d(LogTag.BACKUP, SMSTAG + "NQ out of bounds error when copy pdu data");
        }

        return msgPdu;
    }

    /**
     * Describe <code>onEnd</code> method here.
     *
     */
    public final void onEnd() {
        super.onEnd();
        for (Cursor cur : mSmsCur) {
            if(cur != null) {
                cur.close();
                cur = null;
            }
        }
    }

}
