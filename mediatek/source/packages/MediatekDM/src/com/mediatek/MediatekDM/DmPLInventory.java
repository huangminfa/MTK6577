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

package com.mediatek.MediatekDM;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.mediatek.MediatekDM.mdm.scomo.PLInventory;
import com.mediatek.MediatekDM.mdm.scomo.ScomoComponent;

import com.mediatek.MediatekDM.DmConst.TAG;

import android.util.Log;

public class DmPLInventory implements PLInventory {
//	public static final String TAG="sunway";
    private static class Holder {
        private static DmPLInventory mInventory=new DmPLInventory();
    }
    public static DmPLInventory getInstance() {
        return Holder.mInventory;
    }
    private Context context;

    public void addComponent(ScomoComponent comp) {
        Log.i(TAG.Scomo, "addComponent begin");
        Log.i(TAG.Scomo, "addComponent end");
    }

    public void deleteComponent(String arg0) {
        Log.i(TAG.Scomo, "deleteComponent");
    }

    public ScomoComponent findComponentById(String pkgName) {
        Log.i(TAG.Scomo, "findComponentById begin: "+pkgName);
        ScomoComponent ret=new ScomoComponent();
        if (pkgName==null) {
            Log.w(TAG.Scomo,"plInventory: findComponentById: can't find pkgName corresponding to id: "+pkgName);
            return null;
        }
        DmScomoPackageManager.ScomoPackageInfo info=DmScomoPackageManager.getInstance().getPackageInfo(pkgName);
        if (info==null) {
            return null;
        }
        ret.setActive(true);
        ret.setDescription(info.description);
        //ret.setEnvType("test");
        ret.setId(pkgName);
        ret.setName(pkgName);
        ret.setVersion(info.version);
        return ret;
    }

    public DmPLInventory() {
        Log.i(TAG.Scomo, "DmPLInventory begin");
    }

    public void attachContext(Context context) {
        this.context=context;
    }
}