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


import com.mediatek.MediatekDM.DmConst.TAG;
import com.mediatek.MediatekDM.util.DialogFactory;
import com.mediatek.MediatekDM.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.util.Log;

public class DmNiInfoActivity extends Activity {

    private int item = 0;
    private boolean[] checkedItem;
    private Context context=null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        String opName=DmCommomFun.getOperatorName();

        if (opName!=null&&opName.equalsIgnoreCase("cu"))
        {
            uiVisible=R.string.usermode_visible_cu;
            uiInteract=R.string.usermode_interact_cu;
        }
        else if (opName!=null&&opName.equalsIgnoreCase("cmcc"))
        {
            uiVisible=R.string.usermode_visible_cmcc;
            uiInteract=R.string.usermode_interact_cmcc;
        }
        else
        {
            uiVisible=R.string.usermode_visible_cu;
            uiInteract=R.string.usermode_interact_cu;
        }
        Intent intent = getIntent();
        int type = intent.getIntExtra("Type", 0);
        Bundle mBundle = intent.getExtras();
        Log.d(TAG.MMI, "DmNiInfoActivity type " + type);
        showDialog(type);
        registerReceiver(mBroadcastReceiver, new IntentFilter(DmConst.intentAction.DM_CLOSE_DIALOG));
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DmConst.ServerMessage.TYPE_ALERT_1100:
            return DialogFactory.newAlert(this)
                   .setTitle(R.string.app_name)
                   .setMessage(DmInfoMsg.viewContext.displayText)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_ALERT_1100, onClick NeutralButton");
                    DmInfoMsg.observer.notifyInfoMsgClosed();
                    finish();
                }
            })
                   .create();
        case DmConst.ServerMessage.TYPE_ALERT_1101:
            Log.i(TAG.MMI, "displayText: " + DmConfirmInfo.viewContext.displayText);
            return DialogFactory.newAlert(this)
                   .setTitle(R.string.app_name)
                   .setMessage(DmConfirmInfo.viewContext.displayText)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_ALERT_1101, onClick PositiveButton");
                    if (DmService.getServiceInstance()!=null)
                        DmService.getServiceInstance().cancleNiaAlarm();
                    DmConfirmInfo.observer.notifyConfirmationResult(true);
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_ALERT_1101, onClick NegativeButton");
                    DmConfirmInfo.observer.notifyConfirmationResult(false);
//                    if(DmService.getServiceInstance()!=null)
//                        DmService.getServiceInstance().cancleDmSession();
                    finish();
                }
            })
                   .create();
        case DmConst.ServerMessage.TYPE_ALERT_1103_1104:
            if (DmChoiceList.checked) {
                checkedItem = new boolean[DmChoiceList.stringArray.length];
                for (int i=0; i<DmChoiceList.stringArray.length; i++) {
                    checkedItem[i] = ((DmChoiceList.selected & (1 << i)) > 0)?true:false;
                }
                return DialogFactory.newAlert(this)
                       .setTitle(R.string.app_name)
                .setMultiChoiceItems(DmChoiceList.stringArray, checkedItem, new OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface arg0, int arg1,
                    boolean arg2) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick MultiChoiceItems, item "+arg1+" is "+arg2);
                        checkedItem[arg1] = arg2;

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick MultiChoice PositiveButton");
                        int newSelection = 1;
                        for (int i=0; i<DmChoiceList.stringArray.length; i++) {
                            if (checkedItem[i]) {
                                newSelection |= newSelection<<i;
                            }
                        }
                        DmChoiceList.observer.notifyChoicelistSelection(newSelection);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick MultiChoice NegativeButton");
                        DmChoiceList.observer.notifyCancelEvent();
                        finish();
                    }
                })
                       .create();
            } else {
                return DialogFactory.newAlert(this)
                       .setTitle(R.string.app_name)
                .setSingleChoiceItems(DmChoiceList.stringArray, DmChoiceList.selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick SingleChoiceItems, item is "+whichButton);
                        item = whichButton;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick SingleChoice PositiveButton");
                        DmChoiceList.observer.notifyChoicelistSelection(item);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.v(TAG.MMI,"TYPE_ALERT_1103_1104, onClick SingleChoice NegativeButton");
                        DmChoiceList.observer.notifyCancelEvent();
                        finish();
                    }
                })
                       .create();
            }
        case DmConst.ServerMessage.TYPE_UIMODE_VISIBLE:
            return DialogFactory.newAlert(this)
                   .setTitle(R.string.app_name)
                   .setMessage(uiVisible)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_UIMODE_VISIBLE, onClick NeutralButto");
                    startService();
                    finish();
                }
            })
                   .create();
        case DmConst.ServerMessage.TYPE_UIMODE_INTERACT:
            return DialogFactory.newAlert(this)
                   .setTitle(R.string.app_name)
                   .setMessage(uiInteract)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_UIMODE_INTERACT, onClick PositiveButton");
                    startService();
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.v(TAG.MMI,"TYPE_UIMODE_INTERACT, onClick NegativeButton");
                    if (DmService.getServiceInstance()!=null)
                        DmService.getServiceInstance().userCancled();
                    finish();
                }
            })
                   .create();
        default:
            break;
        }
        return null;
    }
    private void startService()
    {
        Intent serviceIntent = new Intent(context, DmService.class);
        serviceIntent.setAction(DmConst.intentAction.DM_NIA_START);
        startService(serviceIntent);
    }
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            DmNiInfoActivity.this.finish();

        }
    };
    private static Integer uiVisible=null;
    private static Integer uiInteract=null;

}
