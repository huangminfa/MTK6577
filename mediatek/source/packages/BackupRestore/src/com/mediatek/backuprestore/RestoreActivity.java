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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;

public class RestoreActivity extends Activity {
    
    final static String LIST_ITEM_DATE = "date";
    final static String LIST_ITEM_SIZE = "size";
    final static String LISTFRAGTAG = "list";
    final static String DETAILFRAGTAG = "detail";
    
    final public static int SCANNER_FINISH = 0x100;
    
    public static final int MENU_ITEM_DELETE = Menu.FIRST;

    public static final int DLG_RESTORE_CONFORM = 2000;
    public static final int DLG_SDCARD_REMOVED = 2001;
    public static final int DLG_SDCARD_FULL = 2002;
    public static final int DLG_RESULT = 2004;
    
    private static final String INTENT_SD_SWAP = "com.mediatek.SD_SWAP";
    private static final String SD_EXIST = "SD_EXIST";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LogTag.RESTORE, "RestoreActivity : onCreate");
        init();
    }
    
    private void init(){
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            RestoreListFragment fragment = new RestoreListFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, fragment, LISTFRAGTAG).commit();
        }
        initSDCardReceiver();
        registeSDCardReceiver();
    }
    
    protected void  onDestroy(){
        super.onDestroy();
        Log.e(LogTag.RESTORE, "RestoreActivity : onDestroy");
        unRegisteSDCardReceiver();
    }
    
    RestoreDetailFragment mDetailFragment;
    public void showItemDetail(CharSequence date, CharSequence size, String fileName){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RestoreDetailFragment fragment = RestoreDetailFragment.newInstance(date, size, fileName);
        fragmentTransaction.replace(android.R.id.content, fragment, DETAILFRAGTAG);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        
        mDetailFragment = fragment;
    }
    
    
    public void  onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Log.e(LogTag.RESTORE, "RestoreActivity : onConfigurationChanged");
    }
    
    private BroadcastReceiver mSDCardReceiver;
    private void initSDCardReceiver(){
        mSDCardReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                RestoreListFragment listFragment = getListFrament();
                RestoreDetailFragment detailFragment = getDetailFragment();
                
                if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                    
                    if(listFragment != null){
                        listFragment.onSDcardChanged(false);
                    }
                    if(detailFragment != null){
                        detailFragment.onSDcardChanged(false);
                    }
                    
                    
                }else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){ 
                    if(listFragment != null){
                        listFragment.onSDcardChanged(false);
                    }
                    if(detailFragment != null){
                        detailFragment.onSDcardChanged(true);
                    }
                }else if(action.equals(INTENT_SD_SWAP)){
                    boolean sd_card_exist = intent.getBooleanExtra(SD_EXIST, false);
                    int res = sd_card_exist ? R.string.sdcard_swap_insert : R.string.sdcard_swap_remove;
                    Toast.makeText(RestoreActivity.this, res, Toast.LENGTH_SHORT).show();
                    if(listFragment != null){
                        listFragment.onSDcardChanged(sd_card_exist);
                    }
                    if(detailFragment != null){
                        detailFragment.onSDcardChanged(sd_card_exist);
                    }
                }
            }
        };
    }
    private void registeSDCardReceiver(){
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(INTENT_SD_SWAP);
        filter.addDataScheme("file");        
        if (mSDCardReceiver != null){
            registerReceiver(mSDCardReceiver, filter);
        }
    }
    
    
    private void unRegisteSDCardReceiver(){
        if (mSDCardReceiver != null){
            unregisterReceiver(mSDCardReceiver);
            mSDCardReceiver = null;
        }
    }
    
    
    private RestoreDetailFragment getDetailFragment(){
        RestoreDetailFragment fragment = null;
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(DETAILFRAGTAG);
        if(f != null){
            fragment = (RestoreDetailFragment)f;
        }
        return fragment;
    }
    
    private RestoreListFragment getListFrament(){
        RestoreListFragment fragment = null;
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(LISTFRAGTAG);
        if(f != null){
            fragment = (RestoreListFragment)f;
        }
        return fragment;
    }
    
    private void processDlgHandler(int id, boolean isPositive){
        RestoreDetailFragment f = getDetailFragment();
        if(f != null){
            f.diaLogHandler(id, isPositive);
        }
    }
    
    
    protected Dialog onCreateDialog(int id, Bundle args){
        Dialog dialog = null;
        Log.d(LogTag.RESTORE, "RestoreActivity: oncreateDialog, id = " + id);
        switch (id){
        
        case DLG_RESTORE_CONFORM:
            dialog = new AlertDialog.Builder(this)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(R.string.notice)
            .setMessage(R.string.restore_confirm_notice)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.contitue, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.e(LogTag.RESTORE, "to Restore");
                    processDlgHandler(DLG_RESTORE_CONFORM, true);
                }
            }) 
            .setCancelable(false)
            .create();
            break;
        
        
        case DLG_SDCARD_REMOVED:{
            dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setMessage(R.string.sdcard_removed)
            .setPositiveButton(R.string.btn_ok,new DialogInterface.OnClickListener(){

                public void onClick(DialogInterface dialog,
                        int which) {
                    processDlgHandler(DLG_SDCARD_REMOVED, true);
                }
            
            })
            .setCancelable(false)
            .create();
            break;
        }
        
        case DLG_SDCARD_FULL:{
            dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setMessage(R.string.sdcard_is_full)
            .setPositiveButton(R.string.btn_ok,new DialogInterface.OnClickListener(){

                public void onClick(DialogInterface dialog,
                        int which) {
                    
                    processDlgHandler(DLG_SDCARD_FULL, true);
                }
            })
            .setCancelable(false)
            .create();
            break;
        }

        case DLG_RESULT:
            final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    
                    processDlgHandler(DLG_RESULT, true);
                }
            };
            dialog = ResultDialog.createResultDlg(this, R.string.restore_result, args, listener);
        break;
        }
        return dialog;
    }
    
    protected void  onPrepareDialog(int id, Dialog dialog, Bundle args){
        Log.d(LogTag.RESTORE, "RestoreActivity: onPrepareDialog, id = " + id);
        switch(id){
        case DLG_RESULT:
            AlertDialog dlg = (AlertDialog)dialog;
            ListView view = (ListView)dlg.getListView();
            if (view != null){
                ListAdapter adapter = ResultDialog.createResultAdapter(this, args);
                view.setAdapter(adapter);
            }
            break;
        }
    }
}
