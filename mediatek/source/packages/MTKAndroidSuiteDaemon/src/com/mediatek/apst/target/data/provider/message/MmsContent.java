/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.apst.target.data.provider.message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.mediatek.apst.target.util.Debugger;
import com.mediatek.apst.target.util.Global;
import com.mediatek.apst.target.util.StringUtils;
import com.mediatek.apst.util.entity.DatabaseRecordEntity;
import com.mediatek.apst.util.entity.message.Mms;
import com.mediatek.apst.util.entity.message.MmsPart;

public abstract class MmsContent {
    // ==============================================================
    // Constants
    // ==============================================================
    /** All MMS */
    public static final Uri CONTENT_URI = Uri.parse("content://mms");
    public static final Uri CONTENT_URI_PART = Uri.parse("content://mms/part");
    /** MMS ADDR */
    public static final Uri CONTENT_URI_ADDR = Uri.parse("content://mms/addr");
    /** MMS OB_URI */
    public static final Uri CONTENT_URI_OB = Uri.parse("content://mms-sms/");
    /** Inbox. msg_box = 1 */
    public static final Uri CONTENT_URI_INBOX = Uri
            .parse("content://mms/inbox");
    /** Sent. msg_box = 2 */
    public static final Uri CONTENT_URI_SENT = Uri.parse("content://mms/sent");
    /** Draft. msg_box = 3 */
    public static final Uri CONTENT_URI_DRAFT = Uri
            .parse("content://mms/draft");
    /** MMS conversations */
    public static final Uri CONTENT_URI_CONVERSATIONS = Uri
            .parse("content://mms-mms/conversations");

    /** Id. INTEGER(long). */
    public static final String COLUMN_ID = "_id";
    /** Thread id. INTEGER(long). */
    public static final String COLUMN_THREAD_ID = "thread_id";
    /** Timestamp of the MMS. INTEGER(long). */
    public static final String COLUMN_DATE = "date";
    /** Message box type. INTEGER(int). */
    public static final String COLUMN_MSG_BOX = "msg_box";
    /** Has read or not. INTEGER(int). */
    public static final String COLUMN_READ = "read";
    /** ?. TEXT(String). */
    public static final String COLUMN_M_ID = "m_id";
    /** Subject text. TEXT(String). */
    public static final String COLUMN_SUBJECT = "sub";
    /** Subject char set. INTEGER(int). */
    public static final String COLUMN_SUBJECT_CHAR_SET = "sub_cs";
    /** Content type?. TEXT(String). */
    public static final String COLUMN_CT_T = "ct_t";
    /** ?. TEXT(String). */
    public static final String COLUMN_CT_L = "ct_l";
    /** ?. INTEGER(int). */
    public static final String COLUMN_EXP = "exp";
    /** ?. TEXT(String). */
    public static final String COLUMN_M_CLS = "m_cls";
    /** ?. INTEGER(int). */
    public static final String COLUMN_M_TYPE = "m_type";
    /** ?. INTEGER(int). */
    public static final String COLUMN_V = "v";
    /** ?. INTEGER(int). */
    public static final String COLUMN_M_SIZE = "m_size";
    /** ?. INTEGER(int). */
    public static final String COLUMN_PRI = "pri";
    /** ?. INTEGER(int). */
    public static final String COLUMN_RR = "rr";
    /** ?. INTEGER(int). */
    public static final String COLUMN_RPT_A = "rpt_a";
    /** ?. INTEGER(int). */
    public static final String COLUMN_RESP_ST = "resp_st";
    /** ?. INTEGER(int). */
    public static final String COLUMN_ST = "st";
    /** ?. TEXT(String). */
    public static final String COLUMN_TR_ID = "tr_id";
    /** ?. INTEGER(int). */
    public static final String COLUMN_RETR_ST = "retr_st";
    /** ?. TEXT(String). */
    public static final String COLUMN_RETR_TXT = "retr_txt";
    /** ?. INTEGER(int). */
    public static final String COLUMN_RETR_TXT_CS = "retr_txt_cs";
    /** ?. INTEGER(int). */
    public static final String COLUMN_READ_STATUS = "read_status";
    /** ?. INTEGER(int). */
    public static final String COLUMN_CT_CLS = "ct_cls";
    /** ?. TEXT(String). */
    public static final String COLUMN_RESP_TXT = "resp_txt";
    /** ?. INTEGER(int). */
    public static final String COLUMN_D_TM = "d_tm";
    /** ?. INTEGER(int). */
    public static final String COLUMN_D_RPT = "d_rpt";
    /** ?. INTEGER(int). */
    public static final String COLUMN_LOCKED = "locked";
    /** ?. INTEGER(int). */
    public static final String COLUMN_SEEN = "seen";
    /**
     * ID indicates from which SIM the MMS comes.
     * <P>
     * Type: INTEGER (int)
     * </P>
     */
    public static final String COLUMN_SIM_ID = "sim_id";

    /**
     * The part of the Mms
     * 
     * 
     * @add by mtk54043 Yu.Chen
     */
    /** ?. INTEGER(Long). */
    public static final String COLUMN_PART_ID = "_id";
    /** ?. TEXT(int). */
    public static final String COLUMN_PART_MID = "mid";
    /** ?. TEXT(int). */
    public static final String COLUMN_PART_SEQ = "seq";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_CONTENTTYPE = "ct";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_NAME = "name";
    /** ?. TEXT(int). */
    public static final String COLUMN_PART_CHARSET = "chset";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_CID = "cid";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_CL = "cl";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_DATAPATH = "_data";
    /** ?. TEXT(String). */
    public static final String COLUMN_PART_TEXT = "text";
    
    /**
     * The addr of the Mms
     * 
     * 
     * @add by mtk54043 Yu.Chen
     */
    /** ?. INTEGER(Long). */
    public static final String COLUMN_ADDR_ADDRESS = "address";
    public static final String COLUMN_ADDR_MSG_ID = "msg_id";
    public static final String COLUMN_ADDR_CHARSET = "charset";
    public static final String COLUMN_ADDR_TYPE = "type";
    
    public static final String COLUMN_DATE_SENT = "date_sent";
    
    public static final int ADDR_TYPE_SENT = 151;
    public static final int ADDR_TYPE_RECEIVE = 137;
    public static final int ADDR_CHARSET_VALUE = 106;
    
    public static final String NOTIFY_MMS = "130";
    
    

    // ==============================================================
    // Fields
    // ==============================================================

    // ==============================================================
    // Constructors
    // ==============================================================

    // ==============================================================
    // Getters
    // ==============================================================

    // ==============================================================
    // Setters
    // ==============================================================

    // ==============================================================
    // Methods
    // ==============================================================
    public static Mms cursorToMms(Cursor c) {
        if (null == c || c.getPosition() == -1
                || c.getPosition() == c.getCount()) {
            return null;
        }

        // Create a new MMS object
        Mms mms = new Mms();

        try {
            int colId;
            // id
            colId = c.getColumnIndex(COLUMN_ID);
            if (-1 != colId) {
                mms.setId(c.getLong(colId));
            }
            // thread id
            colId = c.getColumnIndex(COLUMN_THREAD_ID);
            if (-1 != colId) {
                mms.setThreadId(c.getLong(colId));
            }

            // date
            colId = c.getColumnIndex(COLUMN_DATE);
            if (-1 != colId) {
                mms.setDate(c.getLong(colId) * 1000);
            }
            // box type
            colId = c.getColumnIndex(COLUMN_MSG_BOX);
            if (-1 != colId) {
                mms.setBox(c.getInt(colId));
            }
            // read
            colId = c.getColumnIndex(COLUMN_READ);
            if (-1 != colId) {
                mms.setRead(c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // locked
            colId = c.getColumnIndex(COLUMN_LOCKED);
            if (-1 != colId) {
                mms.setLocked(c.getInt(colId) == DatabaseRecordEntity.TRUE);
            }
            // subject
            colId = c.getColumnIndex(COLUMN_SUBJECT);
            if (-1 != colId) {
                mms.setSubject(c.getString(colId));
            }
            // content type
            colId = c.getColumnIndex(COLUMN_CT_T);
            if (-1 != colId) {
                mms.setContentType(c.getString(colId));
            }

            // m_id
            colId = c.getColumnIndex(COLUMN_M_ID);
            if (-1 != colId) {
                mms.setM_id(c.getString(colId));
            }

            // sub_cs
            colId = c.getColumnIndex(COLUMN_SUBJECT_CHAR_SET);
            if (-1 != colId) {
                mms.setSub_cs(c.getString(colId));
            }

            // m_cls
            colId = c.getColumnIndex(COLUMN_M_CLS);
            if (-1 != colId) {
                mms.setM_cls(c.getString(colId));
            }

            // m_type
            colId = c.getColumnIndex(COLUMN_M_TYPE);
            if (-1 != colId) {
                mms.setM_type(c.getString(colId));
            }

            // v
            colId = c.getColumnIndex(COLUMN_V);
            if (-1 != colId) {
                mms.setV(c.getString(colId));
            }

            // m_size
            colId = c.getColumnIndex(COLUMN_M_SIZE);
            if (-1 != colId) {
                mms.setM_size(c.getString(colId));
            }

            // tr_id
            colId = c.getColumnIndex(COLUMN_TR_ID);
            if (-1 != colId) {
                mms.setTr_id(c.getString(colId));
            }

            // d_rpt
            colId = c.getColumnIndex(COLUMN_D_RPT);
            if (-1 != colId) {
                mms.setD_rpt(c.getString(colId));
            }

            // seen
            colId = c.getColumnIndex(COLUMN_SEEN);
            if (-1 != colId) {
                mms.setSeen(c.getString(colId));
            }
            
            // date_sent,  added by Yu for ics
            colId = c.getColumnIndex(COLUMN_DATE_SENT);
            if (-1 != colId){
                mms.setDate_sent(c.getInt(colId));
            }

            // For MTK DUAL-SIM feature.
            // sim_id
            colId = c.getColumnIndex(COLUMN_SIM_ID);
            if (-1 != colId) {
                mms.setSimId(c.getInt(colId));
                // sim name. Added by Shaoying Han
                mms.setSimName(Global.getSimName(c.getInt(colId)));
            }
        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[] { c }, null, e);
        }

        return mms;
    }

    /**
     * @param c
     * @return the mms's parts from cursor
     */
    public static MmsPart cursorToMmsPart(Cursor c) {

        if (null == c || c.getPosition() == -1
                || c.getPosition() == c.getCount()) {
            return null;
        }

        MmsPart mmsPart = new MmsPart();
        try {
            int colId;
            // id long
            colId = c.getColumnIndex(COLUMN_PART_ID);
            if (-1 != colId) {
                mmsPart.setId(c.getLong(colId));
            }
            // mid long
            colId = c.getColumnIndex(COLUMN_PART_MID);
            if (-1 != colId) {
                mmsPart.setMmsId(c.getLong(colId));
            }
            // seq
            colId = c.getColumnIndex(COLUMN_PART_SEQ);
            if (-1 != colId) {
                mmsPart.setSequence(c.getInt(colId));
            }
            // contentType
            colId = c.getColumnIndex(COLUMN_PART_CONTENTTYPE);
            if (-1 != colId) {
                mmsPart.setContentType(c.getString(colId));
            }
            // name
            colId = c.getColumnIndex(COLUMN_PART_NAME);
            if (-1 != colId) {
                mmsPart.setName(c.getString(colId));
            }
            // charset
            colId = c.getColumnIndex(COLUMN_PART_CHARSET);
            if (-1 != colId) {
                mmsPart.setCharset(c.getString(colId));
            }
            // cid
            colId = c.getColumnIndex(COLUMN_PART_CID);
            if (-1 != colId) {
                mmsPart.setCid(c.getString(colId));
            }
            // cl
            colId = c.getColumnIndex(COLUMN_PART_CL);
            if (-1 != colId) {
                mmsPart.setCl(c.getString(colId));
            }
            // datapath
            colId = c.getColumnIndex(COLUMN_PART_DATAPATH);
            if (-1 != colId) {
                mmsPart.setDataPath(c.getString(colId));
            }
            // text
            colId = c.getColumnIndex(COLUMN_PART_TEXT);
            if (-1 != colId) {
                mmsPart.setText(c.getString(colId));
            }

        } catch (IllegalArgumentException e) {
            Debugger.logE(new Object[] { c }, null, e);
        }

        return mmsPart;
    }


    public static List<MmsPart> getParts(List<MmsPart> mMarts, Long id) {
        List<MmsPart> parts = new ArrayList<MmsPart>();

        for (MmsPart part : mMarts) {
            if (part.getMmsId() == id) {
                parts.add(part);
            }
        }
        return parts;
    }

    /**
     * Returns true if the address is an email address
     * 
     * @param address
     *            the input address to be tested
     * @return true if address is an email address
     */
    public static boolean isEmailAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }

        String s = extractAddrSpec(address);
        if (null == s) {
            return false;
        }
        Matcher match = StringUtils.EMAIL_ADDRESS_PATTERN.matcher(s);
        return match.matches();
    }

    public static String extractAddrSpec(String address) {
        Matcher match = StringUtils.NAME_ADDR_EMAIL_PATTERN.matcher(address);

        if (match.matches()) {
            return match.group(2);
        }
        return address;
    }
    
    
    
    public ContentValues MmsToContentValues(Mms mms) {
        if (null != mms) {
            Debugger.logI(new Object[] { mms }, ">>insertMms begin");
            // Debugger.logI(new Object[] { mms }, "The Mms id is" +
            // mms.getId());
            ContentValues valuesPdu = new ContentValues(19);
            valuesPdu.put(MmsContent.COLUMN_ID, mms.getId());
            valuesPdu.put(MmsContent.COLUMN_THREAD_ID, mms.getThreadId());
            valuesPdu.put(MmsContent.COLUMN_SUBJECT, mms.getSubject());
            valuesPdu.put(MmsContent.COLUMN_CT_T, mms.getContentType());
            valuesPdu.put(MmsContent.COLUMN_MSG_BOX, mms.getBox());
            valuesPdu.put(MmsContent.COLUMN_DATE, mms.getDate() / 1000);
            valuesPdu.put(MmsContent.COLUMN_READ,
                    mms.isRead() ? DatabaseRecordEntity.TRUE
                            : DatabaseRecordEntity.FALSE);
            valuesPdu.put(MmsContent.COLUMN_LOCKED,
                    mms.isLocked() ? DatabaseRecordEntity.TRUE
                            : DatabaseRecordEntity.FALSE);

            valuesPdu.put(MmsContent.COLUMN_M_ID, mms.getM_id());
            valuesPdu.put(MmsContent.COLUMN_SUBJECT_CHAR_SET, mms.getSub_cs());
            valuesPdu.put(MmsContent.COLUMN_M_CLS, mms.getM_cls());
            valuesPdu.put(MmsContent.COLUMN_M_TYPE, mms.getM_type());
            valuesPdu.put(MmsContent.COLUMN_V, mms.getV());
            valuesPdu.put(MmsContent.COLUMN_M_SIZE, mms.getM_size());
            valuesPdu.put(MmsContent.COLUMN_TR_ID, mms.getTr_id());
            valuesPdu.put(MmsContent.COLUMN_D_RPT, mms.getD_rpt());
            valuesPdu.put(MmsContent.COLUMN_SEEN, mms.getSeen());
            valuesPdu.put(MmsContent.COLUMN_SIM_ID, mms.getSimId());

            valuesPdu.put(MmsContent.COLUMN_DATE_SENT, mms.getDate_sent());
            
            return valuesPdu;
        } else {
            return new ContentValues();
        }
    }

    // ==============================================================
    // Inner & Nested classes
    // ==============================================================
}
