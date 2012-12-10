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

package com.mediatek.MediatekDM.mdm.scomo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mediatek.MediatekDM.mdm.MdmException;


/**
 * SCOMO manager.
 *
 * @author ye.jiao@mediatek.com
 *
 */
public class MdmScomo {
    private static MdmScomo mInstance;

    private Map<String, MdmScomoDp> mDps;
    private Map<String, MdmScomoDc> mDcs;
    private boolean mAutoAddDPChildNodes;
    private String mAlertType;
    private boolean mDestroyed;

    private String mRootURI;

    private MdmScomoHandler mHandler;

    /**
     * Get the single instance of SCOMO manager.
     * @param scomoRootURI Root URI in DM tree.
     * @param h MdmScomoHandler instance.
     * @return Single instance of SCOMO manager.
     * @throws MdmException
     */
    public static MdmScomo getInstance(String scomoRootURI, MdmScomoHandler h) throws MdmException {
        synchronized (MdmScomo.class) {
            if (mInstance == null) {
                mInstance = new MdmScomo(scomoRootURI, h);
            }
            return mInstance;
        }
    }

    private MdmScomo(String scomoRootURI, MdmScomoHandler h) throws MdmException {
        if (scomoRootURI == null) {
            throw new MdmException(MdmException.MdmError.INTERNAL, "scomoRootURI can NOT be null.");
        }
        mRootURI = scomoRootURI;
        mHandler = h;
        mDps = new HashMap<String, MdmScomoDp>();
        mDcs = new HashMap<String, MdmScomoDc>();
        // TODO check these two fields
        mAutoAddDPChildNodes = true;
        mAlertType = "";
    }

    public void destroy() {
        mDps = null;
        mInstance = null;
        mDestroyed = true;
    }

    public MdmScomoDp createDP(String dpName, MdmScomoDpHandler h) {
        MdmScomoDp dp = new MdmScomoDp(dpName, h, this);
        mDps.put(dpName, dp);
        if (mAutoAddDPChildNodes) {
            // TODO
        }
        // TODO is this right?
        if (mHandler != null) {
            mHandler.newDpAdded(dpName);
        }
        return dp;
    }

    public MdmScomoDc createDC(String dcName, MdmScomoDcHandler h, PLInventory inventory, ScomoFactory factory) {
        MdmScomoDc dc = new MdmScomoDc(dcName, h, inventory, factory, this);
        mDcs.put(dcName, dc);
        return dc;
    }

    public MdmScomoDc createDC(String dcName, MdmScomoDcHandler h, PLInventory inventory) {
        MdmScomoDc dc = new MdmScomoDc(dcName, h, inventory, this);
        mDcs.put(dcName, dc);
        return dc;
    }

    protected MdmScomoDc searchDc(String dcName) {
        return mDcs.get(dcName);
    }

    /**
     * return the first DP with name equals to dpName.
     *
     * @param dpName
     * @return The DP found or null if nothing found.
     */
    protected MdmScomoDp searchDp(String dpName) {
        return mDps.get(dpName);
    }

    protected void removeDp(String dpName) {
        mDps.remove(dpName);
    }

    protected void removeDc(String dcName) {
        mDcs.remove(dcName);
    }

    public void setAutoAddDPChildNodes(boolean autoAdd) throws MdmException {
        mAutoAddDPChildNodes = autoAdd;
    }

    public boolean getAutoAddDPChildNodes() throws MdmException {
        return mAutoAddDPChildNodes;
    }

    public void setAlertType(String alertType) throws MdmException {
        mAlertType = alertType;
    }

    public String getAlertType() throws MdmException {
        return mAlertType;
    }

    public ArrayList<MdmScomoDp> getDps() {
        return new ArrayList<MdmScomoDp>(mDps.values());
    }

    public ArrayList<MdmScomoDc> getDcs() {
        return new ArrayList<MdmScomoDc>(mDcs.values());
    }

    public int querySessionActions() {
        // TODO retrieve info from DM Tree
        return 0;
    }

    protected void finalize() throws MdmException {
        if (!mDestroyed) {
            throw new MdmException(MdmException.MdmError.INTERNAL,
                                   "MdmScomo.destroy() must be invoked before this object is freed.");
        }
    }
}
