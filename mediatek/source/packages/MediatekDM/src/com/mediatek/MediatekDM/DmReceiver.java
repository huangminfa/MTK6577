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

package com.mediatek.MediatekDM;

import java.io.File;
import java.util.concurrent.ExecutorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.MediatekDM.DmConst.TAG;
import com.mediatek.MediatekDM.ext.DMAgent;
import com.mediatek.MediatekDM.ext.MTKPhone;
import com.mediatek.MediatekDM.util.DmThreadPool;

public class DmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.i(TAG.Receiver, "Receiver intent: " + intent);
        if (intent==null||intent.getAction()==null||context==null)
            return;
        String intentAction = intent.getAction();

        if (intentAction.equalsIgnoreCase(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
            int simState = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
            Log.d(TAG.Receiver,"[phone-state]->"+simState);
            if (simState == Phone.SIM_INDICATOR_NORMAL
                    || simState == Phone.SIM_INDICATOR_ROAMING) {
                Log.i(TAG.Receiver,"[phone-state]->SIM_STATE_READY, continue.");
            } else {
                Log.d(TAG.Receiver,"[phone-state]->not SIM_STATE_READY, ignore.");
                return;
            }

            Log.d(TAG.Receiver,"------- checking status when sim ready --------");

            Intent newIntent = new Intent();
            newIntent.setAction(DmConst.intentAction.ACTION_REBOOT_CHECK);
            CheckReboot checker=new CheckReboot(newIntent,context);

            if (cExec==null)
                cExec=DmThreadPool.getInstance();
            if (cExec!=null&&checker!=null)
                cExec.execute(checker);
            return;
        } else if (intentAction.equals(DmConst.intentAction.DM_SWUPDATE)) {
            Log.i(TAG.Receiver, "Receive software update intent");
            Intent activityIntent = new Intent(context, DmEntry.class);
            //activityIntent.setAction("com.mediatek.MediatekDM.UPDATECOMPLETE");
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);

        } else {
            Log.w(TAG.Receiver, "Normal intent.");
        }

        Log.i(TAG.Receiver, "Start dm service");
        Intent DmIntent = new Intent(intent);
        if (DmIntent==null||DmIntent.getAction()==null)
            return;
        DmIntent.setClass(context, DmService.class);
        DmIntent.setAction(intentAction);
        context.startService(DmIntent);
    }


    public int checkUpdateResult() {
        Log.i(TAG.Receiver, "checkUpdateResult enter");
        int ret= NORMAL_REBOOT;
        try
        {
            File updateFile=new File(DmConst.pathname.FOTAExecFlagFile);
            if (updateFile.exists()) {
                Log.d(TAG.Receiver, "+++FOTA flag found.");
                updateFile.delete();
                ret=UPDATE_REBOOT;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG.Receiver, e.toString());
            e.printStackTrace();
        }
        return ret;
    }
    public class CheckReboot implements Runnable {
        private Intent mDmIntent = null;
        private Context mContext = null;

        public CheckReboot(Intent DmIntent,Context context)
        {
            mDmIntent=DmIntent;
            mContext=context;
        }
        public void run() {
            DMAgent dmAgent = MTKPhone.getDmAgent();

            boolean isStart=false;
            if (mDmIntent==null||mDmIntent.getAction()==null)
            {
                Log.w(TAG.Receiver, "mDmIntent is null");
                return;
            }
            mDmIntent.setAction(mDmIntent.getAction());
            mDmIntent.setClass(mContext, DmService.class);
            Bundle bundle=new Bundle();

            int res = checkUpdateResult();
            if (res == UPDATE_REBOOT)
            {
                Log.i(TAG.Receiver, "CheckReboot Start dm service, this is update reboot");
                bundle.putString("update","true");
                isStart=true;
            }
            Log.i(TAG.Receiver, "CheckReboot check nia message");
            boolean niaret=niaexist();
            if (niaret==true)
            {
                Log.i(TAG.Receiver, "CheckReboot Start dm service really, this is nia exist");
                bundle.putString("nia", "true");
                isStart=true;
            }

            Log.i(TAG.Receiver, "CheckReboot check wipe flag");
            boolean wipeSign=wipeexist(dmAgent);
            Log.i(TAG.Receiver, "CheckReboot wipe sign is "+wipeSign);
            if (wipeSign==true)
            {
                Log.i(TAG.Receiver, "CheckReboot Start dm service really, this is wipe rebbot");
                bundle.putString("wipe", "true");
                isStart=true;

            }
            if (isStart==true)
            {
                Log.d(TAG.Receiver, "+++ starting service...");
                mDmIntent.putExtras(bundle);
                mContext.startService(mDmIntent);
            }
            else
            {
                Log.d(TAG.Receiver, "--- no need to start service.");
            }
        }

    }
    public boolean niaexist()
    {
        Log.i(TAG.Receiver, "niaexist enter");
        boolean ret=false;
        try
        {
            String nia_Folder=DmConst.pathname.PathNia;
            File folder=new File(nia_Folder);
            if (!folder.exists())
            {
                Log.w(TAG.Receiver, "CheckNia the nia dir is noet exist");
                return ret;
            }

            String[] fileExist=folder.list();
            if (fileExist==null||fileExist.length<=0)
            {
                Log.w(TAG.Receiver, "CheckNia there is no unproceed message");
                return ret;
            }
            ret=true;
        }
        catch (Exception e)
        {
            Log.e(TAG.Receiver, e.getMessage());
        }

        return ret;
    }
    public boolean wipeexist(DMAgent agent)
    {
        Log.i(TAG.Receiver, "wipeexist enter");
        boolean ret=false;
        try
        {
            if (agent!=null)
            {
                ret=agent.isWipeSet();
            }
            else
            {
                Log.w(TAG.Receiver, "mDmAgent is null");
            }
        }
        catch (Exception e)
        {
            Log.e(TAG.Receiver, e.getMessage());
        }
        return ret;
    }


    public static final int NORMAL_REBOOT = 0;
    public static final int UPDATE_REBOOT=1;
    private static  ExecutorService cExec=null;
}
