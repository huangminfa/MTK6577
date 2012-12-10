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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.backuprestore.BackupEngine.BackupResult;
import com.mediatek.backuprestore.BackupRestoreUtils.LogTag;
import com.mediatek.backuprestore.BackupRestoreUtils.ModuleType;
import com.mediatek.backuprestore.BackupRestoreUtils.ScreenLock;
import com.mediatek.backuprestore.BackupService.BackupBinder;
import com.mediatek.backuprestore.BackupService.BackupProgress;
import com.mediatek.backuprestore.BackupService.OnBackupChanged;
import com.mediatek.backuprestore.ResultDialog.ResultEntity;

public class BackupActivity extends ListActivity {
    private ListView mListView;
    private Button mBt_Action;
    private Button mBt_select;
    
    public static final int PRESS_BACK = 501;
    
    //for progressdialog
    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    
    private BroadcastReceiver mReceiver;
    
    private ArrayList<BackupItemData>mDataList;
    private MyBackupAdapter mBackupAdapter;
    private InitListAdapterTask mAsynTask;
    
    private final int DLG_CANCEL_CONFORM = 1000;
    private final int DLG_SDCARD_REMOVED = 1001;
    private final int DLG_SDCARD_FULL = 1002;
    private final int DLG_NO_SDCARD = 1003;
    private final int DLG_RESULT = 1004;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);
        if (savedInstanceState != null){
            mDataList = savedInstanceState.getParcelableArrayList("data");
        }
        init();
        mListView = (ListView)findViewById(android.R.id.list);
        mListView.setClickable(true);
        Log.i(LogTag.BACKUP, "BackupActivity: onCreate");
        
        if (savedInstanceState != null){
            updateButtonState();
        }
        
    }
    
    protected void  onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (mDataList != null){
         outState.putParcelableArrayList("data", mDataList);   
        }
    }
    protected void  onResume(){
        super.onResume();
        Log.i(LogTag.BACKUP, "BackupActivity: onResume");
    }
    protected void onDestroy(){
        super.onDestroy();
        Log.i(LogTag.BACKUP, "BackupActivity: onDestroy");
        if (mAsynTask != null){
            mAsynTask.cancel();
        }
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        unRegisteSDCardReceiver();
        if(mBackupService!= null && mBackupService.getState() == BackupService.INIT){
            this.stopService();
        }
        this.unBindService();
    }
    
    private void init(){
        this.bindService();
        initButton();
        initHandler();
        initSDCardReceiver();
        createProgressDlg();
        mAsynTask = new InitListAdapterTask();
        mAsynTask.execute();
    }
    
    private ProgressDialog createProgressDlg(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.backuping));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelMessage(mHandler.obtainMessage(PRESS_BACK));
        }
        return mProgressDialog;
    }
    
    private ProgressDialog createCancelDlg(){
        if (mCancelDlg == null){
            mCancelDlg = new ProgressDialog(this);
            mCancelDlg.setMessage(getString(R.string.cancelling));
            mCancelDlg.setCancelable(false);
        }
        return mCancelDlg;
    }
    
    private void showProgress() {
        if(mProgressDialog == null){
            mProgressDialog = createProgressDlg();
        }
            mProgressDialog.show();
    }

    private void startBackup(){
        Log.v(LogTag.BACKUP, "BackupActivity: startBackup");
        
        this.startService();
        if (mBackupService != null){
            mBackupService.startBackup(getSelectedItemList());
            showProgress();
        }
    }
    
    private boolean errChecked(){
        boolean ret = false;
        String path = BackupRestoreUtils.getStoragePath();
        if (path == null){
            // no sdcard
            Log.d(LogTag.BACKUP, "BackupActivity: SDCard is removed");
            ret = true;
            mHandler.post(new Runnable(){
                public void run() {
                    BackupActivity.this.showDialog(DLG_SDCARD_REMOVED);
                }
            });
            
        }else if (BackupRestoreUtils.getAvailableSize(path) <= 512){
            //no space
            Log.d(LogTag.BACKUP, "BackupActivity: SDCard is full");
            ret = true;
            mHandler.post(new Runnable(){
                public void run() {
                    BackupActivity.this.showDialog(DLG_SDCARD_FULL);
                }
            });
        }else{
            Log.e(LogTag.BACKUP, "BackupActivity: unkown error");
        }
        return ret;
    }
    
    private void initButton(){
        mBt_Action = (Button)findViewById(R.id.backup_bt_backcup);
        mBt_Action.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                if(mBackupService == null || mBackupService.getState() != BackupService.INIT){
                    Log.e(LogTag.BACKUP, "Can not to start. BackupService not ready or BackupService is ruuning");
                    return;
                }

                ArrayList<Integer> list = getSelectedItemList();
                if(list == null || list.isEmpty()){
                    Log.e(LogTag.BACKUP, "to Backup List is null or empty");
                    return;
                }
                
                String path = BackupRestoreUtils.getStoragePath(); 
                if (path != null) {
                    startBackup();
                    registeSDCardReceiver();
                }else{
                    //scard not available
                    showDialog(DLG_NO_SDCARD);
                }
            }
        });
        
        
        mBt_select = (Button)findViewById(R.id.backup_bt_select);
        mBt_select.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view) {
                if (isAllValued(true)){
                    setAllChecked(false);
                    mBt_select.setText(R.string.selectall);
                    mBt_Action.setEnabled(false);
                }else{
                    setAllChecked(true);
                    mBt_select.setText(R.string.unselectall);
                    mBt_Action.setEnabled(true);
                }
                mBackupAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private void updateButtonState(){
        if (isAllValued(false)){
            mBt_Action.setEnabled(false);
            mBt_select.setText(R.string.selectall);
        }else{
            mBt_Action.setEnabled(true);
            if (isAllValued(true)){
                mBt_select.setText(R.string.unselectall);
            }else{
                mBt_select.setText(R.string.selectall);
            }
        }
    }
    
    protected void  onListItemClick(ListView list, View view, int position, long id){
        
        BackupItemData item = mDataList.get(position);
        boolean toChecked = !item.getChecked();
        
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.backup_select_list_item_chkbox);
        checkBox.setChecked(toChecked);
        item.setChecked(toChecked);
        if (!toChecked){
            mBt_select.setText(R.string.selectall);
            if(isAllValued(false)){
                mBt_Action.setEnabled(false);
            }
        }else{
            mBt_Action.setEnabled(true);
            if(isAllValued(true)){
                mBt_select.setText(R.string.unselectall);
            }
        }
    }
    
    private void setAllChecked(boolean checked){
        for (BackupItemData item : mDataList){
            if (item.isEnable()){
                item.setChecked(checked);
            }
        }
        mBackupAdapter.notifyDataSetChanged();
    }
    
    private boolean isAllValued(boolean value){
        boolean ret = true;
        for (BackupItemData item : mDataList){
            if (item.isEnable() && item.getChecked() != value){
                ret = false;
                break;
            }
        }
        return ret;
    }

      
    private ArrayList<Integer> getSelectedItemList(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (BackupItemData item : mDataList){
            if (item.isEnable() && item.getChecked()){
                list.add(item.getType());
            }
        }
        return list;
    }
    
    private MyBackupAdapter initBackupAdapter(){
        if (mDataList == null){
            mDataList = new ArrayList<BackupItemData>();
    
            int types[] = new int[]{ModuleType.TYPE_CONTACT, ModuleType.TYPE_MESSAGE, ModuleType.TYPE_PICTURE,
                                    ModuleType.TYPE_CALENDAR, ModuleType.TYPE_APP, ModuleType.TYPE_MUSIC, ModuleType.TYPE_NOTEBOOK};
            int num = types.length;
            for (int i = 0; i < num; i++){
                BackupItemData item = new BackupItemData(types[i], true);
                mDataList.add(item);
    }
    }
    
        MyBackupAdapter adapter = new MyBackupAdapter(this, mDataList, R.layout.backup_select_list_item);
        return adapter;
    }
    
    protected void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case PRESS_BACK:
                    if(mBackupService != null &&  mBackupService.getState() != BackupService.INIT && 
                            mBackupService.getState() != BackupService.FINISH){
                        mBackupService.pauseBackup();
                        BackupActivity.this.showDialog(DLG_CANCEL_CONFORM);
                    }
                    break;
                }
            }
        };
    }
    
        
    private void showBackupResult(BackupResult result, ArrayList<ResultEntity> list){
        
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        
            if (mCancelDlg != null && mCancelDlg.isShowing()){
                mCancelDlg.dismiss();
            }
    
        if(result != BackupResult.Cancel){
           Bundle args = new Bundle();
           args.putParcelableArrayList("result", list);
           showDialog(DLG_RESULT, args);   
        }else{
           stopService();
        }
    }
    
    private void initSDCardReceiver(){

        mReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                
                String action = intent.getAction();
                Log.e("mtk80999", "BroadcastReceiver : " + action);
                if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                    if(mBackupService != null && mBackupService.getState() != BackupService.INIT &&
                            mBackupService.getState() != BackupService.FINISH){
                        mBackupService.pauseBackup();
                        BackupActivity.this.showDialog(DLG_SDCARD_REMOVED);                        
                    }
                }
            }
        };
        this.registeSDCardReceiver();
    }

    protected Dialog onCreateDialog(int id, Bundle args){
        Dialog dialog = null;
        switch(id){
            case DLG_CANCEL_CONFORM:{
                dialog = new AlertDialog.Builder(BackupActivity.this)
                            .setTitle(R.string.warning).
                            setMessage(R.string.cancel_backup_confirm)
                            .setPositiveButton(R.string.bt_yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                if (mBackupService != null && mBackupService.getState() != BackupService.INIT && 
                                        mBackupService.getState() != BackupService.FINISH) {
                                        if (mCancelDlg == null){
                                            mCancelDlg = createCancelDlg();
                                        }
                                        mCancelDlg.show();
                                    mBackupService.cancelBackup();                                    
                                }
                            }
                            })
                            .setNegativeButton(R.string.bt_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                
                                if (mBackupService != null &&  
                                    mBackupService.getState() == BackupService.PAUSE) {
                                    mBackupService.continueBackup();
                                }
                                    if(mProgressDialog != null){
                                        mProgressDialog.show();
                                    }
                            }
                            })
                            .setCancelable(false)
                            .create();
                break;
            }
            

            case DLG_SDCARD_REMOVED:{
                dialog = new AlertDialog.Builder(BackupActivity.this)
                                .setTitle(R.string.warning).
                setMessage(R.string.sdcard_removed)
                .setPositiveButton(R.string.btn_ok,new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface dialog,
                            int which) {
                    if (mBackupService != null
                            && mBackupService.getState() == BackupService.PAUSE) {
                        mBackupService.cancelBackup();
                        }
                    }
                
                })
                                .setCancelable(false)
                .create();
                    break;
                }
            
            case DLG_SDCARD_FULL:{
                dialog = new AlertDialog.Builder(BackupActivity.this)
                .setTitle(R.string.warning).
                setMessage(R.string.sdcard_is_full)
                .setPositiveButton(R.string.btn_ok,new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface dialog,
                            int which) {
                    if (mBackupService != null
                            && mBackupService.getState() == BackupService.PAUSE) {
                        mBackupService.cancelBackup();
                    }
                }
            })
            .setCancelable(false)
            .create();
            break;
        }
            
            case DLG_NO_SDCARD:
                dialog = new AlertDialog.Builder(BackupActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.notice)
                .setMessage(R.string.nosdcard_notice)
                .setPositiveButton(android.R.string.ok, null)
                .create();
                break;
            case DLG_RESULT:
                final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        if(mBackupService != null){
                            mBackupService.reset();
                        }
                        stopService();
                    }
                };
                dialog = ResultDialog.createResultDlg(this, R.string.backup_result, args, listener);
            break;

        }
        return dialog;
    }

    protected void  onPrepareDialog(int id, Dialog dialog, Bundle args){
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
    
    
    
    private ProgressDialog mCancelDlg; 
    
    private void registeSDCardReceiver(){
                        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");        
        if (mReceiver != null){
            registerReceiver(mReceiver, filter);
                        }
                }

    
    private void unRegisteSDCardReceiver(){
        if (mReceiver != null){
            unregisterReceiver(mReceiver);
            mReceiver = null;
                    }                
                        }
                        
    public void  onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Log.e("mtk80999", "BackupActivity : onConfigurationChanged");
    }
    
    public static class BackupItemData implements Parcelable{
        private int mType;
        private boolean mEnable;
        private boolean mIsChecked;
        
        public BackupItemData(int type, boolean isChecked, boolean enable){
            mType = type;
            mIsChecked = isChecked;
            mEnable = enable;
        }
        
        public BackupItemData(int type, boolean enable){
            this(type, enable, enable);
        }
        
        public void setChecked(boolean checked){
            mIsChecked = checked;
        }
        
        public boolean getChecked(){
            return mIsChecked;
        }
        
        public int  getType(){
            return mType;
        }
        
        public int getIconId(){
            int ret = 0;
            switch(mType){
            case ModuleType.TYPE_CONTACT:
                ret = R.drawable.ic_contact;
                break;
                    
            case ModuleType.TYPE_MESSAGE:
                ret = R.drawable.ic_message;
                break;
                
            case ModuleType.TYPE_PICTURE:
                ret = R.drawable.ic_picture;
                break;
            case ModuleType.TYPE_CALENDAR:
                ret = R.drawable.ic_canlendar;
                break;
            case ModuleType.TYPE_APP:
                ret = R.drawable.ic_application;
                break;

            case ModuleType.TYPE_MUSIC:
                ret = R.drawable.ic_music;
                break;

            case ModuleType.TYPE_NOTEBOOK:
                ret = R.drawable.ic_notebook;
                break;

            default:
                break;
            }
            return ret;
        }
        
        public int getTextId(){
            int ret = 0;
            switch(mType){
            case ModuleType.TYPE_CONTACT:
                ret = R.string.contact_module;
                break;
                    
            case ModuleType.TYPE_MESSAGE:
                ret = R.string.message_module;
                break;
        
            case ModuleType.TYPE_PICTURE:
                ret = R.string.picture_module;
                break;
            case ModuleType.TYPE_CALENDAR:
                ret = R.string.calendar_module;
                break;
            case ModuleType.TYPE_APP:
                ret = R.string.app_module;
                break;

            case ModuleType.TYPE_MUSIC:
                ret = R.string.music_module;
                break;

            case ModuleType.TYPE_NOTEBOOK:
                ret = R.string.notebook_module;
                break;

            default:
                break;
            }
            return ret;
            
        }
        public boolean isEnable(){
            return mEnable;
        }
        
        public int describeContents() {
            return 0;
                }
    
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mType);
            dest.writeInt(mEnable ? 1 : 0);
            dest.writeInt(mIsChecked ? 1 : 0);

            }
        private BackupItemData(Parcel in){
            mType = in.readInt();
            mEnable = in.readInt() > 0;
            mIsChecked = in.readInt() > 0;
       
        }
        public static final Parcelable.Creator<BackupItemData> CREATOR = new Parcelable.Creator<BackupItemData>() {
    
            public BackupItemData createFromParcel(Parcel in) {
                return new BackupItemData(in);
            }
    
            public BackupItemData[] newArray(int size) {
                return new BackupItemData[size];
            }
        };
    }
    
    private class MyBackupAdapter extends BaseAdapter{
    
        private ArrayList<BackupItemData> mList;
        private int mLayoutId;
        private LayoutInflater mInflater;
    
        public MyBackupAdapter(Context context, ArrayList<BackupItemData> list, int resource){
            mList = list;
            mLayoutId = resource;
            mInflater = LayoutInflater.from(context);
        }
        
        public int getCount() {
            return mList.size();
                }

        public Object getItem(int arg0) {
            return null;
            }           

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null){
                view = mInflater.inflate(mLayoutId, parent, false);
        }

            BackupItemData item = mList.get(position);
            ImageView imgView = (ImageView)view.findViewById(R.id.backup_select_list_item_image);
            TextView textView = (TextView)view.findViewById(R.id.backup_select_list_item_text);
            CheckBox chxbox = (CheckBox)view.findViewById(R.id.backup_select_list_item_chkbox);
            
            imgView.setImageResource(item.getIconId());
            textView.setText(item.getTextId());
            
            boolean enabled = item.isEnable();

            imgView.setEnabled(enabled);
            textView.setEnabled(enabled);
            chxbox.setEnabled(enabled);
            view.setClickable(!enabled);
            if (enabled){
                chxbox.setChecked(item.getChecked());
            }else{
                chxbox.setChecked(false);
            }
            return view;
        }
    }
    
     private class InitListAdapterTask extends AsyncTask<Void, Void, Long> {
         private boolean isCanceled = false;
         
         public void cancel(){
             isCanceled = true;
             Log.v(LogTag.BACKUP, "InitListAdapterTask is canceled");
         }
        
        @Override
        protected void onPostExecute(Long arg0) {
            super.onPostExecute(arg0);
            if (!isCanceled){
                if (mListView != null){
                    mListView.setAdapter(mBackupAdapter);
                }
                updateButtonState();
            }
            mAsynTask = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isCanceled = false;
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            mBackupAdapter = initBackupAdapter();
            return null;
        }
    } 
     
     
     private String getProgressDlgMessage(int type){
         StringBuilder builder = new StringBuilder(getString(R.string.backuping));
         
         builder.append("(");
         builder.append(BackupRestoreUtils.getModuleStringFromType(this, type));
         builder.append(")");
         return builder.toString();
     }
     
     
     private void checkBackupState(){
         if(mBackupService != null){
             int state = mBackupService.getState();
             switch(state){
                 case BackupService.RUNNING:
                 case BackupService.PAUSE:
                 {                 
                     BackupProgress p =  mBackupService.getCurBackupProgress();
                     String msg = getProgressDlgMessage(p.mType);
                     Log.e("mtk80999", "checkBackupState: Max = " + p.mMax + " curprogress = " + p.mCurNum);
                     if(state == BackupService.RUNNING){
                         mProgressDialog.show();
                     }
                     if(mProgressDialog != null){
                         mProgressDialog.setMessage(msg);
                         mProgressDialog.setMax(p.mMax);
                         mProgressDialog.setProgress(p.mCurNum);
                     }
                     break;
                 }
                 
                 case BackupService.FINISH:
                     showBackupResult(mBackupService.getBackupResultType(), mBackupService.getBackupResult());
                     break;
                     
                 case BackupService.ERROHAPPEN:
                     errChecked();
                     break;
                     
                 default:
                     break;
             }
         }
     }
     
     
     private BackupBinder mBackupService;
     
     private void bindService(){
         this.getApplicationContext().
             bindService(new Intent(this, BackupService.class), mServiceCon, Service.BIND_AUTO_CREATE);
     }
     
     private void unBindService(){
         if(mBackupService != null){
             mBackupService.setOnBackupChangedListner(null);
         }
         this.getApplicationContext().unbindService(mServiceCon);
     }
     
     private void startService(){
         this.startService(new Intent(this, BackupService.class));
     }
     
     private void stopService(){
         if(mBackupService != null){
             mBackupService.reset();
         }
         this.stopService(new Intent(this, BackupService.class));
     }
     
     
     ServiceConnection mServiceCon = new ServiceConnection(){

         public void onServiceConnected(ComponentName name, IBinder service) {
             mBackupService = (BackupBinder)service;
             if (mBackupService != null){
                 mBackupService.setOnBackupChangedListner(mBackupListener);
             }
             checkBackupState();
             Log.i(LogTag.BACKUP, "BackupActivity: onServiceConnected");
         }

         public void onServiceDisconnected(ComponentName name) {
             // TODO Auto-generated method stub
             mBackupService = null;
             Log.i(LogTag.BACKUP, "BackupActivity: onServiceDisconnected");
         }
     }; 
     
     OnBackupChanged mBackupListener = new OnBackupChanged(){

        public void onBackupEnd(BackupResult resultCode, ArrayList<ResultEntity> resultRecord) {
            final BackupResult iResultCode = resultCode;
            final ArrayList<ResultEntity> iResultRecord = resultRecord;
            if (mHandler != null){
                mHandler.post(new Runnable(){

                    public void run() {
                        showBackupResult(iResultCode, iResultRecord);
                    }
                });
            }
        }

        public void onBackupErr(IOException e) {
            // TODO Auto-generated method stub
            if(errChecked()){
                if(mBackupService != null && mBackupService.getState() != BackupService.INIT &&
                        mBackupService.getState() != BackupService.FINISH){
                    mBackupService.pauseBackup();
                }
            }
        }

        public void onComposerChanged(int type, int max) {
            Log.i(LogTag.BACKUP, "BackupActivity: onComposerChanged: type = " + type + "Max = " + max);
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
            final int value = progress;
            mHandler.post(new Runnable(){
                public void run() {
                    if (mProgressDialog != null){
                        mProgressDialog.setProgress(value);
                    }
                }
            });
        }
     };
}
