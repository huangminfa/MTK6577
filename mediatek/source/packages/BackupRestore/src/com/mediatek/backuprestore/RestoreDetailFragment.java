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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.backuprestore.BackupRestoreUtils.FileUtils;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.RestoreService.OnRestoreChanged;
import com.mediatek.backuprestore.RestoreService.RestoreBinder;
import com.mediatek.backuprestore.RestoreService.RestoreProgress;
import com.mediatek.backuprestore.ResultDialog.ResultEntity;

public class RestoreDetailFragment extends Fragment {
    private static final String DATE = "date";
    private static final String SIZE = "size";
    private static final String FILENAME = "filename";
    
    private final String ITEM_TEXT = "text";
    
    private ListView mListView = null;
    private Button mBtRestore = null;
    private Button mBtSelect = null;
    
    private TextView mDate = null;
    private TextView mSize = null;
    
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    
    private boolean[] mCheckedArray;
    private boolean mIsActive;
    
    /**
     * Create a new instance of MyFragment that will be initialized
     * with the given arguments.
     */
    static RestoreDetailFragment newInstance(CharSequence date, CharSequence size, String filename) {
        RestoreDetailFragment f = new RestoreDetailFragment();
        Bundle b = new Bundle();
        b.putCharSequence(DATE, date);
        b.putCharSequence(SIZE, size);
        b.putString(FILENAME, filename);
        f.setArguments(b);
        return f;
    }
    
    
    
    /**
     * During creation, if arguments have been supplied to the fragment
     * then parse those out.
     */
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            mCheckedArray = savedInstanceState.getBooleanArray("selectResult");
        }
        Log.d(LogTag.RESTORE, "RestoreDetailFragment -> onCreate");
        this.bindService();
        if (!BackupRestoreUtils.isSdCardAvailable()){
            finish();
        }
    }
    
    public void  onDestroy(){
        super.onDestroy();
        Log.d(LogTag.RESTORE, "RestoreDetailFragment -> onDestory");
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }

        if(mRestoreService!= null && mRestoreService.getState() == BackupService.INIT){
            this.stopService();
        }
        this.unBindService();
    }
    
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (mCheckedArray != null){
            outState.putBooleanArray("selectResult", mCheckedArray);
        }
        mIsActive = false;
    }
    
    @Override
    public void onResume(){
        super.onResume();
        Log.d(LogTag.RESTORE, "RestoreDetailFragment -> onResume");
        if (!BackupRestoreUtils.isSdCardAvailable()){
            finish();
        }
        mIsActive = true;
    }
    
    
    @Override
    public void  onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.d(LogTag.RESTORE, "RestoreDetailFragment -> onActivityCreated");
        new FilePreviewTask().execute();
    }
    
    /**
     * Create the view for this fragment, using the arguments given to it.
     */
    @Override 
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(LogTag.RESTORE, "RestoreDetailFragment -> onCreateView");
        if (savedInstanceState != null){
            mCheckedArray = savedInstanceState.getBooleanArray("selectResult");
        }
        
        View view = inflater.inflate(R.layout.restoredetail, null, false);
        init(view);
        
        if (savedInstanceState != null){
            updateButtonState();
        }
        return view;
    }
    
    private void init(View view){
        Bundle bundle = getArguments();
        mDate = (TextView)(view.findViewById(R.id.restore_detail_date));
        mDate.setText(bundle.getCharSequence(DATE));
        mSize = (TextView)(view.findViewById(R.id.restore_detail_size));
        mSize.setText(bundle.getCharSequence(SIZE));        
        mListView = (ListView)view.findViewById(R.id.restoredetail_list);
        mListView.setOnItemClickListener(new OnItemClickListener(){

            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.restore_select_list_item_chkbox);
                boolean toChecked = !checkBox.isChecked();
                mCheckedArray[position] = toChecked;
                checkBox.setChecked(toChecked);
                if (!toChecked){
                    mBtSelect.setText(R.string.selectall);
                    if(isAllValued(false)){
                        mBtRestore.setEnabled(false);
                    }
                }else{
                    mBtRestore.setEnabled(true);
                    if(isAllValued(true)){
                        mBtSelect.setText(R.string.unselectall);
                    }
                }
            }
            
        });
        initButton(view);
        initHandler();
        createProgressDlg();
    }
    
    private void finish(){
        FragmentManager fm = this.getFragmentManager();
        fm.popBackStack();
    }
    
    private void showRestoreConfirmDlg(){
        
        this.getActivity().showDialog(RestoreActivity.DLG_RESTORE_CONFORM);
    }
    
    private void startRestore(){
        if(mRestoreService == null){
            Log.e(LogTag.RESTORE, "RestoreDetailFragment-> startRestore : mRestoreService is null");
            return;
        }
        
        if(mRestoreService.getState() != RestoreService.INIT){
            Log.e(LogTag.RESTORE, "Can not to start Restore. Restore Service is ruuning");
            return;
        }
        
        Bundle bundle = getArguments();
        String fileName = bundle.getString(FILENAME);
        
        ArrayList<Integer> list = getSelectedItemList();
        this.startService();
        if (mRestoreService != null){
            mRestoreService.startRestore(fileName, list);
        }        
        showProgress(list);
    }
    
    private boolean errChecked(){
        boolean ret = false;
        String path = BackupRestoreUtils.getStoragePath();
        if (path == null){
            // no sdcard
            ret = true;
            mHandler.post(new Runnable(){
                public void run() {
                    showDialog(RestoreActivity.DLG_SDCARD_REMOVED, null);
                }
            });
            
        }else if (BackupRestoreUtils.getAvailableSize(path) <= 512){
            //no space
            ret = true;
            mHandler.post(new Runnable(){
                public void run() {
                    showDialog(RestoreActivity.DLG_SDCARD_FULL, null);
                }
            });
        }
        return ret;
    }
    
    
    private ProgressDialog createProgressDlg(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.restoring));
            mProgressDialog.setCancelable(false);
        }
        return mProgressDialog;
    }
    
    private void showProgress(ArrayList<Integer> list) {
        if(mProgressDialog != null){
            
            int num = 0;
            int type = list.get(0);
            if (mPreview != null){
                num = mPreview.getTypeCount(type);
            }
            mProgressDialog.show();
            mProgressDialog.setMax(num);
            StringBuilder builder = new StringBuilder(getString(R.string.restoring));
          
            builder.append("(");
            builder.append(BackupRestoreUtils.getModuleStringFromType(getActivity(), type));
            builder.append(")");
            
            mProgressDialog.setMessage(builder.toString());
            mProgressDialog.setProgress(0);
        }
    }
    
    
    private void initButton(View view){
        mBtRestore = (Button)view.findViewById(R.id.restoredetail_bt_restore);
        mBtRestore.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v) {
                
                ArrayList<Integer> list = getSelectedItemList();
                if(list == null || list.isEmpty()){
                    Log.e(LogTag.RESTORE, "to Restore List is null or empty");
                    return;
                }
                
                showRestoreConfirmDlg();
            }
            
        });
        mBtSelect = (Button)view.findViewById(R.id.restoredetail_bt_select);
        mBtSelect.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v) {
                if(isAllValued(true)){
                    selectAllItems(false);
                    mBtSelect.setText(R.string.selectall);
                    mBtRestore.setEnabled(false);
                }
                else{
                    mBtSelect.setText(R.string.unselectall);
                    mBtRestore.setEnabled(true);
                    selectAllItems(true);
                }
            }
        });
    }
    private void selectAllItems(boolean select){
        int num = mCheckedArray.length;
        
        for (int i = 0; i < num; i++){
            mCheckedArray[i] = select;
        }
        
        int count = mListView.getChildCount();
        for(int i = 0; i< count; i++){
            View view = mListView.getChildAt(i);
            if (view != null){
                CheckBox box = (CheckBox)view.findViewById(R.id.restore_select_list_item_chkbox);
                if(box != null){
                    box.setChecked(select);
                }
            }
        }
    }
    
    private boolean isAllValued(boolean value){
        boolean ret = true;
        int length = mCheckedArray.length;
        for (int i = 0; i < length; i++){
            if (mCheckedArray[i] != value){
                ret = false;
                break;
            }
        }
        return ret;
    }

    private void updateButtonState(){
        if (isAllValued(false)){
            mBtRestore.setEnabled(false);
            mBtSelect.setText(R.string.selectall);
        }else{
            mBtRestore.setEnabled(true);
            if (isAllValued(true)){
                mBtSelect.setText(R.string.unselectall);
            }else{
                mBtSelect.setText(R.string.selectall);
            }
        }
    }

      
    private ArrayList<Integer> getSelectedItemList(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        int num = mCheckedArray.length;
        for(int i = 0; i < num; i++){
            if (mCheckedArray[i] == true){
                list.add(mModuleList.get(i));
            }
        }
        return list;
    }
    
    
    private BackupFilePreview mPreview = null;
    private void initFilePreview(){
        mPreview = new BackupFilePreview(new File(this.getArguments().getString(FILENAME)));
    }
    
    private int getModuleType(){
        int ret = 0;
        if (mPreview != null){
            ret = mPreview.getBackupModules();
        }
        return ret;
    }
    
    private SimpleAdapter initAdapter(int module) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        fillListItems(mapList, module);
        String[] from = new String[] {ITEM_TEXT};
        int[] to = new int[] {R.id.restore_select_list_item_text};
        SimpleAdapter simpleAdapter = new RestoreDetailAdapter(getActivity(), mapList,
                                        R.layout.restore_select_list_item, from, to);
        return simpleAdapter;
    }
    
    private void fillListItems(List<Map<String, Object>> list, int module){
        
        int[] types = new int[]{ModuleType.TYPE_CONTACT, ModuleType.TYPE_MESSAGE, ModuleType.TYPE_PICTURE,
                                ModuleType.TYPE_CALENDAR, ModuleType.TYPE_APP, ModuleType.TYPE_MUSIC, ModuleType.TYPE_NOTEBOOK};
        int[] strings = new int[]{R.string.contact_module, R.string.message_module, R.string.picture_module,
                                  R.string.calendar_module, R.string.app_module, R.string.music_module, R.string.notebook_module};
        
        int length = types.length;
        
        for(int i = 0; i < length; i++){
            int type = types[i];
            if ((module & types[i]) != 0){
                fillOneItem(list, strings[i]);
                addModuleType(type);
            }
        }
        initCheckArray(list.size());
    }
    
    private void initCheckArray(int size){
        if (mCheckedArray == null && size > 0){
            mCheckedArray = new boolean[size];
            for (int i = 0; i < size; i++){
                mCheckedArray[i] = true;
            }
        }
    }
    
    
    private void fillOneItem(List<Map<String, Object>> mapList, int text_id){
        Map<String, Object> values = new HashMap<String, Object>();
        if (this.getActivity() != null){
        values.put(ITEM_TEXT, getString(text_id));
        }
        mapList.add(values);
    }
    
        
    private List<Integer> mModuleList = new ArrayList<Integer>();
    private void addModuleType(int type){
        mModuleList.add(type);
    }
    
    private void initHandler(){
        getActivity().getMainLooper();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                }
            }
        };
    }
    
    private void showRestoreResult(ArrayList<ResultEntity> list){
        if (mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        Bundle args = new Bundle();
        args.putParcelableArrayList("result", list);
        showDialog(RestoreActivity.DLG_RESULT, args);
    }
                
    
    private class RestoreDetailAdapter extends SimpleAdapter {
        
        public RestoreDetailAdapter(Context context, List<? extends Map<String, ?>> data,
            int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);  
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.restore_select_list_item_chkbox);
            
            if (checkBox != null) {
                checkBox.setChecked(mCheckedArray[position]);
            }
            return view;
        }
    }
    
    private void FilePreviewEnd(int module){
        if(this.getActivity() != null){
            SimpleAdapter adapter = initAdapter(module);
                if(adapter != null){
                    mListView.setAdapter(adapter);
                    mBtSelect.setVisibility(View.VISIBLE);
                    mBtRestore.setVisibility(View.VISIBLE);
                    
                    mDate.setText(mPreview.getBackupTime());
                    StringBuilder builder = new StringBuilder(getString(R.string.backup_data));
                    builder.append(" ");
                    builder.append(FileUtils.getDisplaySize(mPreview.getFileSize(), RestoreDetailFragment.this.getActivity()));
                    mSize.setText(builder.toString());
                }
        }
    }
    
    private class FilePreviewTask extends AsyncTask<Void, Void, Long> {

//            private SimpleAdapter adapter;
            private int module;
        
        @Override
        protected void onPostExecute(Long arg0) {
            super.onPostExecute(arg0);
            FilePreviewEnd(module);
        }

        @Override
        protected void onPreExecute() {
            initFilePreview();
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Void... arg0) {
                module = getModuleType();
            return null;
        }
    }
    
    
    public void onSDcardChanged(boolean bMount){
        if(!bMount){
            if(mRestoreService != null && mRestoreService.getState() != RestoreService.INIT &&
                    mRestoreService.getState() != RestoreService.FINISH){
                mRestoreService.cancelRestore();
            }
            if(mIsActive){
                finish();
            }
        }
    }
    
    
    private void showDialog(int id, Bundle args){
        Activity activity = this.getActivity();
        Log.e(LogTag.RESTORE, "RestoreDetailFragment ->showDialog, activity = " + activity);
        if(activity != null){
            activity.showDialog(id, args);
        }
    }
    
    public void diaLogHandler(int id, boolean isPositive){
        switch(id){
            case RestoreActivity.DLG_RESULT:
            {
                if(mRestoreService != null){
                    mRestoreService.reset();
                }
                this.stopService();
                break;
            }   
                
            case RestoreActivity.DLG_RESTORE_CONFORM:
                startRestore();
                break;
                
            case RestoreActivity.DLG_SDCARD_FULL:
                if(mRestoreService != null){
                    mRestoreService.cancelRestore();
                }
                break;
                
            case RestoreActivity.DLG_SDCARD_REMOVED:
                if(mRestoreService != null){
                    mRestoreService.reset();
                }
                stopService();
                break;
        }
    }
    
    private String getProgressDlgMessage(int type){
        StringBuilder builder = new StringBuilder(getString(R.string.restoring));
        
        builder.append("(");
        builder.append(BackupRestoreUtils.getModuleStringFromType(this.getActivity(), type));
        builder.append(")");
        return builder.toString();
    }
    
    private void checkRestoreState(){
        if(mRestoreService != null){
            int state = mRestoreService.getState();
            Log.d(LogTag.RESTORE, "checkRestoreState: state = " + state);
            if(state == RestoreService.RUNNING || state == RestoreService.PAUSE){
                
                RestoreProgress p =  mRestoreService.getCurRestoreProgress();
                String msg = getProgressDlgMessage(p.mType);
                Log.e(LogTag.RESTORE, "checkRestoreState: Max = " + p.mMax + " curprogress = " + p.mCurNum);
                if(state == RestoreService.RUNNING){
                    mProgressDialog.show();
                }
                if(mProgressDialog != null){
                    mProgressDialog.setMessage(msg);
                    mProgressDialog.setMax(p.mMax);
                    mProgressDialog.setProgress(p.mCurNum);
                }
            }else if(state == RestoreService.FINISH){
                
                showRestoreResult(mRestoreService.getRestoreResult());
            }else if(state == RestoreService.ERROHAPPEN){
                errChecked();
            }
        }
    }
    
    
    private RestoreBinder mRestoreService;
    
    private void bindService(){
       this.getActivity().getApplicationContext()
           .bindService(new Intent(this.getActivity(), RestoreService.class), mServiceCon, Service.BIND_AUTO_CREATE);
    }
    
    private void unBindService(){
        if(mRestoreService != null){
            mRestoreService.setOnRestoreChangedListner(null);
        }
        this.getActivity().getApplicationContext().unbindService(mServiceCon);
    }
    
    private void startService(){
        this.getActivity().startService(new Intent(this.getActivity(), RestoreService.class));
    }
    
    private void stopService(){
        if(mRestoreService != null){
            mRestoreService.reset();
        }
        this.getActivity().stopService(new Intent(this.getActivity(), RestoreService.class));
    }
    
    
    ServiceConnection mServiceCon = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            mRestoreService = (RestoreBinder)service;
            if (mRestoreService != null){
                mRestoreService.setOnRestoreChangedListner(mRestoreListener);
            }
            checkRestoreState();
            Log.i(LogTag.RESTORE, "RestoreDetailFragment: onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            mRestoreService = null;
            Log.i(LogTag.RESTORE, "RestoreDetailFragment: onServiceDisconnected");
        }
    }; 
    
    OnRestoreChanged mRestoreListener = new OnRestoreChanged(){

        public void onComposerChanged(int type, int max) {
            Log.i(LogTag.BACKUP, "RestoreDetailFragment: onComposerChanged");
            final int iType = type;
            final int iMax = max;
            if (mHandler != null){
                mHandler.post(new Runnable(){
               
                    public void run() {
                        
                        String msg = getProgressDlgMessage(iType);
                        if(mProgressDialog != null){
                            mProgressDialog.setMessage(msg);
                            mProgressDialog.setMax(iMax);
                            mProgressDialog.setProgress(0);
                        }
                    }
                });
            }
        }

        public void onProgressChange(int progress) {
            final int p = progress;
            mHandler.post(new Runnable(){
                public void run() {
                    if (mProgressDialog != null){
                        mProgressDialog.setProgress(p);
                    }
                }
            });
        }

        public void onRestoreEnd(boolean bSuccess, ArrayList<ResultEntity> resultRecord) {
            final ArrayList<ResultEntity> iResultRecord = resultRecord;
            Log.d(LogTag.RESTORE, "RestoreDetailFragment: Restore end");
            if (mHandler != null){
                mHandler.post(new Runnable(){
                    public void run() {
                        showRestoreResult(iResultRecord);
                        Log.d(LogTag.RESTORE, "RestoreDetailFragment: Restore show Result Dialog");
                    }
                });
            }
        }
    
        public void onRestoreErr(IOException e) {
            if(errChecked()){
                if(mRestoreService != null && mRestoreService.getState() != RestoreService.INIT &&
                        mRestoreService.getState() != RestoreService.FINISH){
                    mRestoreService.pauseRestore();
                }
            }
        }
    };
}
