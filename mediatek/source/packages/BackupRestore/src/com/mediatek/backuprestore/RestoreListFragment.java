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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mediatek.backuprestore.BackupRestoreUtils.FileUtils;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;

public class RestoreListFragment extends ListFragment {
    
    private SimpleAdapter mAdapter;
    List<Map<String, Object>> mAdapterMapList;
    

    private Handler mHandler;
    
    private Activity mOwnerActivity;
    
    final static String LIST_ITEM_DATE = "date";
    final static String LIST_ITEM_SIZE = "size";
    final static String LIST_ITEM_FILE = "file";
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(LogTag.RESTORE, "RestoreListFragment: onCreate");
    }
    
    @Override
    public void  onDestroy(){
        super.onDestroy();
        Log.i(LogTag.RESTORE, "RestoreListFragment: onDestroy");
        if (mFileScanner != null){
            mFileScanner.setHandler(null);
//            mFileScanner.quitScan();
        }
    }
    
    public void  onPause(){
        super.onPause();
        Log.i(LogTag.RESTORE, "RestoreListFragment: onPasue");
        if (mFileScanner != null){
            mFileScanner.quitScan();
        }
    }
    
   
    @Override
    public void onResume(){
        //refresh
        super.onResume();
        if (BackupRestoreUtils.isSdCardAvailable()){
            startScanFiles();
        }
        Log.i(LogTag.RESTORE, "RestoreListFragment: onResume");
    }
    
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.i(LogTag.RESTORE, "RestoreListFragment: onAttach");
    }
    
    @Override
    public void  onDetach(){
        super.onDetach();
        Log.i(LogTag.RESTORE, "RestoreListFragment: onDetach");
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.registerForContextMenu(getListView());
        init();
    }
    
    
    private void init(){        
        initHandler();
        initAdapter();
        mOwnerActivity = getActivity();
    }
    
    private void initAdapter(){
        mAdapterMapList = new ArrayList<Map<String, Object>>();
        String[] from = new String[] {LIST_ITEM_DATE, LIST_ITEM_SIZE};
        int[] to = new int[] {R.id.restore_list_item_text1, R.id.restore_list_item_text2};
        mAdapter = new SimpleAdapter(getActivity(), mAdapterMapList, R.layout.restore_list_item, from, to);
        setListAdapter(mAdapter);
    }
    
    private BackupFileScanner mFileScanner;
    private void startScanFiles(){
        if (mFileScanner == null){
            mFileScanner = new BackupFileScanner(mOwnerActivity, mHandler);
        }
        mFileScanner.startScan();
    }
    
    
    private void initHandler(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                case RestoreActivity.SCANNER_FINISH:
                        handleScanResults((List<BackupFilePreview>)msg.obj);                   
                    break;
                    
                default:
                    break;
                }
            }
        };
    }
    
    public void onSDcardChanged(boolean bMount){
        if(!bMount){
            if(mAdapterMapList != null){
                mAdapterMapList.clear();
            }
            if(mAdapter != null){
                mAdapter.notifyDataSetChanged();
            }
        }
        
        if (BackupRestoreUtils.isSdCardAvailable()){
            startScanFiles();
        }
    }
    
    private void handleScanResults(List<BackupFilePreview> items) {
        //avoid to fragment to do something when the fragment is detached from activity
        if (this.getActivity() != null){
            addResultsAsPreferences(items);
        }
    }
    
    
    private void addResultsAsPreferences(List<BackupFilePreview> items) {
        if (items == null) {
            return;
        }
        
        mAdapterMapList.clear();
        for (BackupFilePreview item : items) {
            Map<String, Object> values = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder(item.getBackupTime());
            values.put(LIST_ITEM_DATE, builder.toString());         

            builder = new StringBuilder(getString(R.string.backup_data));
            builder.append(" ");
            builder.append(FileUtils.getDisplaySize(item.getFileSize(), mOwnerActivity));
            values.put(LIST_ITEM_SIZE, builder.toString());
            
            values.put(LIST_ITEM_FILE, item.getFile());
            
            mAdapterMapList.add(values);
            
        }
        mAdapter.notifyDataSetChanged();
    }
    
    

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("FragmentList", "Item clicked: " + id);
        Map<String, Object> values = (HashMap<String, Object>)(l.getItemAtPosition(position));
        File file = (File)values.get(LIST_ITEM_FILE);
        String filename = file.getAbsolutePath();
        TextView date = (TextView)v.findViewById(R.id.restore_list_item_text1);
        TextView size = (TextView)v.findViewById(R.id.restore_list_item_text2);
        
        RestoreActivity activity =(RestoreActivity)mOwnerActivity; 
        activity.showItemDetail(date.getText(), size.getText(), filename);
    }
    
    @Override
    public void  onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.add(0, RestoreActivity.MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }
    
    @Override
    public boolean  onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e("mtk80999", "bad menuInfo", e);
            return false;
        }
        switch (item.getItemId()) {
            case RestoreActivity.MENU_ITEM_DELETE: {
                int index = (int)info.id;
                Map<String, Object> values = (HashMap<String, Object>)(this.getListView().getItemAtPosition(index));
                File file = (File)values.get(LIST_ITEM_FILE);
                file.delete();
                mAdapterMapList.remove(index);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }
    
    public void  onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        
        Log.e("mtk80999", "RestoreListFragment: onConfigurationChanged");
    }
    
    
    
}
