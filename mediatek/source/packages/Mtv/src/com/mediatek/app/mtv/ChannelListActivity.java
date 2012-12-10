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

package com.mediatek.app.mtv;

import com.mediatek.app.mtv.MtvEngine.ListItem;
import com.mediatek.app.mtv.ChannelProvider;

import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;
import com.mediatek.featureoption.FeatureOption;


import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.text.Editable;
import android.text.Selection;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import android.view.WindowManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;


public class ChannelListActivity extends ListActivity {
    private static final String TAG = "ATV/ChannelListActivity";


    private static final String STATE_ACTIVITY_STATE = "mState";
    private static final String STATE_MULTIPLE_SELECTION= "mMultiSelected";

    private static final String VT_CALL_END = "android.phone.extra.VT_CALL_END";

    //states
    private static final int STATE_NOT_CREATED= 0;    
    private static final int STATE_CREATED= 1;
    private static final int STATE_INITIALIZING_SCANNING = 2;
    private static final int STATE_INITIALIZED = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_SCANNING = 5;
    private static final int STATE_PAUSED_SCANNING = 6;
    private static final int STATE_PAUSED_INITIALIZING_SCANNING = 7;
    private int mState;  

    private static final int DIALOG_SCANNING = 1;
    private static final int DIALOG_NO_CHANNEL_FOUND = 2;  
    private static final int DIALOG_RENAME = 3;
    private static final int DIALOG_CHOOSE_LOCATION = 4;
    private static final int DIALOG_PROCESSING = 5; 
    public static final int DIALOG_SELECT_LOCATION_FAIL = 6;
    public static final int DIALOG_INITIALIZE_HW_FAIL = 7;
    public static final int DIALOG_SCAN_CONFIRM = 8;
    public static final int DIALOG_DELETE_CONFIRM = 9;    
    private static final int MAX_PROGRESS = 100;
    
    private static final int MENU_ITEM_DELETE = 1;
    private static final int MENU_ITEM_RENAME = 2;
    private static final int MENU_ITEM_WATCH = 3;

    private MtvEngine mEngine;
    private ChannelProvider mChannelProvider;
    private View mEmpty;

    private ProgressDialog mScanProgressDialog;
    private AlertDialog mVtInUseWarningDialog;
    
    
    //indicate whether auto selection is checked now.
    private boolean mAutoSelection;
    private boolean mGotoPlayer;
    //indicate whether the scanning is finished after onPaused.
    private boolean mScanFinishAfterPaused;    
    private WakeLock mWakeLock;  
    private BroadcastReceiver mReceiver = null;
    //current location in use.
    private String mLocation;
    private Runnable mDeleteAction;
       //this is the location user selects in dialog box.
       private static final String KEY_LOCATION = "my_location";
    //this is the real location when user selects "Auto location" in dialog box.
    private static final String KEY_AUTO_LOCATION = "auto_location";

    //private SharedPreferences mPreferences;  
    
    //channel list holder.
    private ArrayList<ListItem> mList = new ArrayList<ListItem>();

    //the list item whose context is being opened.
    private int mListItemClicked = -1;

    private HashSet<Integer> mMultiSelected = null;
    private ChannelListAdapter mAdapter; 
    private ActionMode mSelectionMode;
    private final Handler mHandler = new MainHandler();
    private static final int MSG_LOCATION_SELECTED = 0xf0008001; 
    private static final int MSG_UPDATE_SIGNAL_INDICATOR = 0xf0008003; 

    private static class ActivityState {
        int mState;
        String mLocation;
        HashSet<Integer> mMultiSelected;
        ArrayList<ListItem> mList;
        int mListItemClicked;
        boolean mAutoSelection;
        //boolean mSelectedAll;
        boolean mScanFinishAfterPaused;        
        ActivityState(int state,String loc,HashSet<Integer> selected,ArrayList<ListItem> list,
                int listItemClicked,boolean scanFinishAfterPaused,boolean autoSelection) {
            mState = state;
            mLocation = loc;
            mMultiSelected = selected;
            //avoid GC on MtvEngine.
            mList = list;
            mListItemClicked = listItemClicked;
            mScanFinishAfterPaused = scanFinishAfterPaused;
            mAutoSelection = autoSelection;
        }
    }
    
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Xlog.d(TAG, "handleMessage what = "+msg.what);
        
            switch (msg.what) {  
                /*
                case MSG_UPDATE_SIGNAL_INDICATOR: {
                    Xlog.d(TAG, "MSG_UPDATE_SIGNAL_INDICATOR");
                    mAdapter.updateSignalIndicator();
                    break;
                }*/
                case MtvEngine.MTV_MSG_NOTIFY_POWER_ON: {                
                    Intent intent = new Intent(MtvEngine.NOTIFY_POWER_ON);
                    sendBroadcast(intent);    
                    break;
                }
                case MtvEngine.MTV_MSG_INITIALIZE_DONE:
                    if (msg.arg1 == 1) {
                        //Initialization succeed.
                        initializeTrue();                        
                    } else {
                        initializeFalse();
                    }
                    break;
                
                case MSG_LOCATION_SELECTED: {
                    boolean result = true;
                    boolean needLoadTable = false;
                    String location = null;
                    String[] locations = getResources().getStringArray(R.array.supported_locations);
                    String[] codes = (String[])msg.obj;
                    int which = msg.arg1;
                    SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(ChannelListActivity.this);                    
                    SharedPreferences.Editor editor = mPreferences.edit();         
                    if (codes[which].equals("-1")) {
                        int i = getAutoSelectLocationIndex();
                        if (i == -1) {
                            //mLocation = mPreferences.getString(KEY_LOCATION,null); 
                            result = false;
                        } else { 
                                mAutoSelection = true;                                
                                editor.putString(KEY_LOCATION,"-1");                                            
                                if (!mLocation.equals(codes[i])) {
                                    mLocation = codes[i];
                                    editor.putString(KEY_AUTO_LOCATION,mLocation);                                        
                                    needLoadTable = true;                                        
                                }
                                location = locations[i];
                        }                                                                    
                    } else {
                        mAutoSelection = false;                        
                        mLocation = codes[which];
                        location = locations[which];                        
                        editor.putString(KEY_LOCATION,mLocation);            
                        needLoadTable = true;                                        
                    }
                    
                    editor.commit();                    
                    editor = null;

                    if (needLoadTable) {                        
                        initializeChannelList();
                        mAdapter.notifyDataSetChanged();                        
                    }

                    dismissDialog(DIALOG_PROCESSING);

                    if (result) {                        
                        if (needLoadTable) {
                            String text = String.format(getResources().getString(R.string.location_changed),location);
                            Toast.makeText(ChannelListActivity.this,text, 1000).show();
                        }
                    } else {                        
                        showDialog(DIALOG_SELECT_LOCATION_FAIL);
                    }
                    break;
                }                       
                
                case MtvEngine.MTV_SCAN_PROGRESS: {
                    onScanProgressEvent(msg.arg1 >> 8,msg.arg1 & 0xff,(long)(Long)msg.obj,null);
                    break;
                }
                
                case MtvEngine.MTV_SCAN_FINISH: {
                    onScanFinishEvent(msg.arg1,msg.arg2);
                    break;
                }                                        
                    
                case MtvEngine.MTV_CHIP_SHUTDOWN: {
                    if (mState == STATE_SCANNING 
                        || mState == STATE_PAUSED_SCANNING
                        || mState == STATE_PAUSED_INITIALIZING_SCANNING) {
                        onScanFinishEvent(msg.arg1,msg.arg2);
                    } else if (mState == STATE_INITIALIZED) {
                        mState = STATE_CREATED;
                    }
                    break;
                }    

                default:
                    break;
                    
            }
        }
    }  


    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        
        state.putInt(STATE_ACTIVITY_STATE, mState);
        if (isInMultiSelectMode()) {
            ArrayList<Integer> intArray = new ArrayList<Integer>();    
            intArray.ensureCapacity(mMultiSelected.size());
            for (Integer position : mMultiSelected) {
                intArray.add(position);
            }            
            state.putIntegerArrayList(STATE_MULTIPLE_SELECTION, intArray);
        }
    }


    @Override
    public void onCreate(Bundle icicle) {
        Xlog.d(TAG, "onCreate() this = "+this);        
        super.onCreate(icicle); 

        mEngine = MtvEngine.getEngine(MtvEngine.MTV_ATV);
        
        //check whether it is recreated due to configuration change,if yes,restore the state.
        ActivityState a = (ActivityState)getLastNonConfigurationInstance();
        if (a != null) {
            switch(a.mState) {
                case STATE_PAUSED_SCANNING:
                    disallowSleeping();                                
                    mState = STATE_SCANNING;    
                    break;
                case STATE_PAUSED_INITIALIZING_SCANNING:
                    mState = STATE_INITIALIZING_SCANNING;
                    break;
                case STATE_PAUSED:
                    mListItemClicked = a.mListItemClicked;
                    //mSelectedAll = a.mSelectedAll;                    
                    mMultiSelected = a.mMultiSelected;
                    break;        
                default:
                    break;                
            }
            mLocation = a.mLocation;
            mScanFinishAfterPaused = a.mScanFinishAfterPaused;
            mList = (ArrayList<ListItem>)a.mList;    
            mAutoSelection = a.mAutoSelection;
            
        }else if (icicle != null) {
            int state = icicle.getInt(STATE_ACTIVITY_STATE,STATE_CREATED);
            if (state == STATE_SCANNING || state == STATE_PAUSED_SCANNING || state == STATE_PAUSED_INITIALIZING_SCANNING) {
                mScanFinishAfterPaused = !mEngine.isScanning();        
            }
            //mScanFinishAfterPaused = icicle.getBoolean(STATE_SCAN_FINISH_AFTER_PAUSED,false);
            ArrayList<Integer> intArray = icicle.getIntegerArrayList(STATE_MULTIPLE_SELECTION);
            if (intArray != null) {
                mMultiSelected = new HashSet<Integer>();    
                int size = intArray.size();
                for (int i = 0;i < size;i++) {
                    mMultiSelected.add(intArray.get(i));
                }            
            }
        }
        
        //start engine initialization before the time-comsuming function setContentView could reduce the response time of the first TV operation.
        mEngine.setEventCallback(new MtvEngine.EventCallback() {
            public void onEvent(Message msg) {
                mHandler.handleMessage(msg);
            }
        });        
        // We will not init here because hardware limitation.ALPS00230598
        //mEngine.turnOn();

        setContentView(R.layout.main_list);
        
        mEmpty = findViewById(R.id.empty);                
        getListView().setEmptyView(mEmpty);        
        getListView().setOnCreateContextMenuListener(this);
        findViewById(R.id.refresh_list).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tryScan();
            }
        });
        
        findViewById(R.id.choose_location).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_CHOOSE_LOCATION);
            }
        });         

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);        
        if (isInMultiSelectMode()) {//on configuration change.
            startActionMode(new SelectionModeCallback());  
        }    
        
        // Set up our adapter before construct ChannelListAdapter.
        mChannelProvider = ChannelProvider.instance(this,getResources().getStringArray(R.array.supported_location_codes));
        
        mAdapter = new ChannelListAdapter(this);
        setListAdapter(mAdapter);    
        
        if (mState == STATE_NOT_CREATED) {
            mState = STATE_CREATED;
        }            
        registerMyReceiver();
    }
    

    @Override
    protected void onStart() {
        super.onStart();
    if (FeatureOption.MTK_VT3G324M_SUPPORT) {  
            boolean isVTIdle = true;
            try {
                ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (iTel == null) {
                    Xlog.e(TAG, "iTel is null!! ");
                } else {//we need to check it always.see ALPS00047426.
                    isVTIdle = iTel.isVTIdle();
                }    
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } finally {
                showVtInUseWarning(!isVTIdle);
            }
        }
    }    

        
    private void myAssert(boolean noassert) {
        if (!noassert) {
            throw new RuntimeException(TAG+" assertion failed!");  
        }
    }
    @Override
    public Object onRetainNonConfigurationInstance() {
        Xlog.d(TAG, "onRetainNonConfigurationInstance() mState = "+mState);    
        mEngine.setEventCallback(null);//ignore the messages
        if (mScanProgressDialog != null) {
            mScanProgressDialog.setOnDismissListener(null);
            mScanProgressDialog.setOnKeyListener(null);
        }
        //MtvReceiver.setBroadcastIntentListener(null);
        allowSleeping();
        mEngine.setChannelList(mList);        
        ActivityState a = new ActivityState(mState,mLocation,mMultiSelected,mList,
            mListItemClicked,mScanFinishAfterPaused,mAutoSelection);
        return a;
    }

    private void onScanProgressEvent(int chnum, int ch, long chInfo, Object obj) {        
        Xlog.d(TAG, "onScanProgressEvent() mState = "+mState);    
        
        if (mState != STATE_SCANNING && mState != STATE_PAUSED_SCANNING) { 
            //we may still receive scan event when user stop scan but driver has not got the notification yet,because ap and driver run in different threads.
            return;
        }

        if (mScanProgressDialog.isIndeterminate()) {            
            //stop the indeterminate animation
            mScanProgressDialog.setIndeterminate(false);
            
            //we need to reset the Progress otherwise it will use the last value because managed dialogs are cached for an activity.
            mScanProgressDialog.setProgress(0); 
            mScanProgressDialog.setMax(chnum);    

            //show the progress number and percentage.
            Window w = mScanProgressDialog.getWindow();            
            TextView view = (TextView)w.findViewById(com.android.internal.R.id.progress_number);
            view.setText("");            
            view.setVisibility(View.VISIBLE);            
            view = (TextView)w.findViewById(com.android.internal.R.id.progress_percent);
            SpannableString tmp = new SpannableString(NumberFormat.getPercentInstance().format(0));
            tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);            
            view.setText(tmp);
            view.setVisibility(View.VISIBLE);        
        }

        if (chInfo != 0) {
            //mAdapter.addFoundChannel(ch,name);
            mAdapter.notifyDataSetChanged();
        }
        
        mScanProgressDialog.incrementProgressBy(1);         
    }
    

    private void onScanFinishEvent(int chnum, Object obj) {    
        Xlog.d(TAG, "onScanFinishEvent() mState = "+mState);
        switch(mState) {
            case STATE_SCANNING:
                mState = STATE_INITIALIZED;                
                mScanProgressDialog.dismiss();    
                if (mList.size() <= 0) {
                    showDialog(DIALOG_NO_CHANNEL_FOUND);
                }                
                break;
            case STATE_PAUSED_SCANNING:                
            case STATE_PAUSED_INITIALIZING_SCANNING:
                mState = STATE_PAUSED;                                                
                mScanProgressDialog.dismiss();
                //set a flag here to solve ALPS00225270 and ALPS00221352
                mScanFinishAfterPaused    = true;
                mEngine.turnOff(true);                    
                break;                
            default:
                //driver will send a scan finish event on scan finish event and stop scan event.we just ingore it if it is not in the above states.
                break;
        }
    }    

    private void initializeTrue() {    
        
        Xlog.d(TAG, "initializeTrue() mState = "+mState);
        switch(mState) {
            case STATE_NOT_CREATED:            
            case STATE_CREATED:                
                mState = STATE_INITIALIZED;
                break;
            case STATE_PAUSED_INITIALIZING_SCANNING:
                startScan();
                mState = STATE_PAUSED_SCANNING;
                break;                    
            case STATE_INITIALIZING_SCANNING:
                startScan();
                mState = STATE_SCANNING;
                break;                
            default:
                //ingore asnchronous events because it may already in queue when state is changed.
                return;
        }                        
    }

    private void initializeFalse() {    
        
        Xlog.d(TAG, "initializeFalse() mState = "+mState);
        switch(mState) {
            case STATE_CREATED:
                break;
            case STATE_PAUSED_INITIALIZING_SCANNING:    
                mState = STATE_PAUSED;                
                mScanProgressDialog.dismiss();    
                break;        
            case STATE_INITIALIZING_SCANNING:    
                mState = STATE_CREATED;                
                mScanProgressDialog.dismiss();            
                showDialog(DIALOG_INITIALIZE_HW_FAIL);    
                mAdapter.notifyDataSetChanged();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
                break;                
            default:
                //ingore asnchronous events because it may already in queue when state is changed.
                return;
        }                
    }    

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        finishSelectionMode();
    }   
    
    private void registerMyReceiver() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(MtvEngine.REQUEST_SHUTDOWN_CMD);
        intentFilter.addAction(VT_CALL_END);
        
        mReceiver = new MyBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(VT_CALL_END)) {
                    showVtInUseWarning(false);
            } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN) 
                    || intent.getAction().equals(MtvEngine.REQUEST_SHUTDOWN_CMD)) {
                    switch(mState) {
                        case STATE_PAUSED_SCANNING:
                            mEngine.stopChannelScan();    
                            //fall through
                        case STATE_PAUSED_INITIALIZING_SCANNING:
                            mState = STATE_PAUSED;                            
                            mScanProgressDialog.dismiss();
                            //set a flag here to solve ALPS00225270 and ALPS00221352
                            mScanFinishAfterPaused    = true;
                            mEngine.turnOff(true);                    
                            break;                            
                        default:
                            break;
                    }
            }  
        }
    }

    private void startScan() {  
        mEngine.setChannelList(mList);
        mEngine.setChannelProvider(mChannelProvider);            
        disallowSleeping();
        if (!mEngine.channelScan(Integer.parseInt(mLocation))) {
            mHandler.sendEmptyMessageDelayed(MtvEngine.MTV_SCAN_FINISH,
                    500);
        }                    
    }    

    private void disallowSleeping() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        
        if (mWakeLock == null) {
            PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);        
            if (mPowerManager == null) {
                Xlog.e(TAG, "mPowerManager is null!! ");
            } else {
                mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
            }
        }
        
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    private void allowSleeping() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        

        Xlog.d(TAG, "mWakeLock = "+mWakeLock);        
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Xlog.d(TAG, "mWakeLock.isHeld()");    
            
            mWakeLock.release();
        }
    }
    
    
    private void tryScan() {  
        Xlog.d(TAG, "tryScan mState = "+mState+" mList.size() = "+mList.size());
        if (mState == STATE_SCANNING || mState == STATE_PAUSED || mState == STATE_PAUSED_SCANNING
                        || mState == STATE_PAUSED_INITIALIZING_SCANNING || mState == STATE_INITIALIZING_SCANNING) {
            return;
        }
        
         if (mLocation == null) {
            showDialog(DIALOG_SELECT_LOCATION_FAIL);
            return;
        }

        //clear background.
        if (!mList.isEmpty()) {
            //marked all items in database as invalid before scanning.
            int size = mList.size();
            for (int i = 0;i < size;i++) {
                mChannelProvider.updateChannelEntry(Integer.parseInt(mList.get(i).mCh),0);
            }               

            //mOldList = mList;
            mList.clear(); //= new ArrayList<ListItem>();    
            mAdapter.notifyDataSetChanged();            
        }          
        
        mEmpty.setVisibility(View.INVISIBLE);
        
        showDialog(DIALOG_SCANNING);
        switch (mState) {
            case STATE_CREATED:        
                mState = STATE_INITIALIZING_SCANNING;                
                //if initialization failed,we need to do it again here.
                mEngine.turnOn();
                break;

            case STATE_INITIALIZED:    
                mState = STATE_SCANNING;                
                startScan();
                break;    
            default:
                break;                    
        }        
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        getActionBar().setHomeButtonEnabled(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Xlog.d(TAG, "onPrepareOptionsMenu");
    
        super.onPrepareOptionsMenu(menu);
        closeContextMenu();//solve ALPS00132172.
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        switch(id) {
            case R.id.action_scan:{
                if (mList.isEmpty()) {
                    tryScan();
                } else {
                    showDialog(DIALOG_SCAN_CONFIRM);
                }
                break;
            }
            case R.id.select_location:{                
                showDialog(DIALOG_CHOOSE_LOCATION);
                break;
            }
            case R.id.multiselect:{    
                if (!mList.isEmpty() && !mHandler.hasMessages(MSG_LOCATION_SELECTED)/*location may be on the change*/) {
                    openMultiSelectMode();                                    
                }
                break;
            }
            default:
                break;
        }
        return true;
    }           
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        Xlog.d(TAG, "onCreateContextMenu");
        if (isInMultiSelectMode()) return;
        
        closeOptionsMenu();//solve ALPS00132172.
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Xlog.e(TAG, "bad menuInfo", e);
            return;
        }
        // Setup the menu header
        menu.setHeaderTitle(mList.get(info.position).mName);

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.delete);
        menu.add(0, MENU_ITEM_RENAME, 0, R.string.rename);
        menu.add(0, MENU_ITEM_WATCH, 0, R.string.watch_now);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Xlog.e(TAG, "bad menuInfo", e);
            return false;
        }
        mListItemClicked = info.position;

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {                
                showDialog(DIALOG_DELETE_CONFIRM);
                return true;
            }
            
            case MENU_ITEM_RENAME: {
                showDialog(DIALOG_RENAME);
                return true;
            }    
            
            case MENU_ITEM_WATCH: {
                startWatchTv(mListItemClicked);
                return true;
            }               
            default:
                break;            
        }
        return false;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (isInMultiSelectMode()) {
            toggleMultiSelected(position);
        } else {
            startWatchTv(position);
        }
    }

    private void toggleMultiSelected(Integer item) {
        if (!mMultiSelected.add(item)) {
            mMultiSelected.remove(item);
        }
        updateSelectionModeView();
    }    
    
    private boolean isInMultiSelectMode() {
        return mMultiSelected != null;
    }

    private void closeMultiSelectMode() {
        if (mMultiSelected == null) return;
        mMultiSelected = null;   
        finishSelectionMode();
        mAdapter.notifyDataSetChanged();        
    }

    private void openMultiSelectMode() {    
        Xlog.d(TAG, "openMultiSelectMode");
        
        mMultiSelected = new HashSet<Integer>();    
        mAdapter.notifyDataSetChanged();
        startActionMode(new SelectionModeCallback());        
    }

    private String getMcc() {
        //TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //String mccmnc = tm.getSubscriberIdGemini(Phone.GEMINI_SIM_1);
        String mccmnc = null;

        //use MTK SDK for gemini functions.
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            TelephonyManagerEx tm = new TelephonyManagerEx(this);
            mccmnc = tm.getSubscriberId(Phone.GEMINI_SIM_1);        
            if (mccmnc == null) {
                mccmnc = tm.getSubscriberId(Phone.GEMINI_SIM_2);
            }
        } else {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                Xlog.e(TAG, "tm is null!! ");
            } else {
                mccmnc = tm.getSubscriberId();
            }            
        }
        Xlog.d(TAG, "getMcc() mccmnc = "+mccmnc);
          if (mccmnc != null && !mccmnc.equals("")) {
             return mccmnc.substring(0, 3);
          }
          return null;
    }
    
    private int getAutoSelectLocationIndex(){
        String mcc = getMcc();
        Xlog.d(TAG, "mcc = "+mcc);
        if (mcc != null) {
            String[] mccs = getResources().getStringArray(R.array.supported_location_mccs);
            //String[] codes = a.getResources().getStringArray(R.array.supported_location_codes); 
            //Because some countries has more than one mcc,we need to handle it specially here.
            //TODO:make customer know that this needs customization according to their target market.
            switch (Integer.parseInt(mcc)) {
                case 461:
                    mcc = "460";
                    break;
                case 235:
                    mcc = "234";
                    break;        
                //case 216: /* Hungary */ 
                case 218: /* Bosnia and Herzegovina */
                case 219: /* Croatia */
                case 230: /* Czech */
                case 231: /* Slovak */  
                case 246: /* Lithuania */ 
                case 247: /* Latvia */  
                case 248: /* Estonia */
                case 255: /* Ukraine */
                case 260: /* Poland */
                case 284: /* Bulgaria */ 
                case 428: /* Mongolia */    
                    mcc = "216";
                    break;                    
                //case 202: /* Greece */
                case 204: /* Netherlands */ 
                case 206: /* Belgium */
                case 213: /* Andorra */
                case 225: /* Vatican city */
                case 228: /* Switzerland */
                case 232: /* Austria */
                case 238: /* Denmark */ 
                case 240: /* Sweden */ 
                case 242: /* Norway */ 
                case 244: /* Finland */
                case 262: /* Gemany */ 
                case 270: /* Luxembourg */
                case 274: /* Iceland */
                case 276: /* Albania */
                case 278: /* Malta */
                case 280: /* Cyprus */ 
                case 288: /* Faroe Island */
                case 290: /* Greenland */
                case 292: /* San Marino */
                case 293: /* Slovenia */
                case 294: /* Macedonia */
                case 295: /* Liechtenstein */    
                    mcc = "202";
                    break;         
                case 257: /* Belarus */ 
                case 259: /* Moldova */
                case 282: /* Georgia */
                case 283: /* Armenia */ 
                case 401: /* Kazakstan */ 
                case 434: /* Uzbekistan */
                case 436: /* Tajikistan */
                case 437: /* Kyrgyz Republic */
                case 438: /* Turkmenistan */    
                    mcc = "250";
                    break;    
                default:
                    break;                    
            }

            
            //TODO:improve the comparation here. 
            for(int i = 0;i<mccs.length;i++){
                if (mcc.equals(mccs[i])) {
                    Xlog.d(TAG, "getAutoSelectLocationCode() index = "+i);                 
                    return i;
                }
            }    

        } 
        
        return -1;
    } 

    //TODO: can we avoid to cache the dialog?
    protected void onPrepareDialog(int id, Dialog dialog) {
        Xlog.d(TAG, "onPrepareDialog,id = " + id);
    
        super.onPrepareDialog(id,dialog);
        switch (id) {    
        case DIALOG_RENAME:
            if (!mList.isEmpty() && mListItemClicked != -1) {//onPrepareDialog maybe called when configuration changed but DIALOG_RENAME was not being showed..We need to ignore this case.
                EditText nameTextEdit = (EditText)dialog.findViewById(R.id.name_edit);
                nameTextEdit.setText(mList.get(mListItemClicked).mName,TextView.BufferType.EDITABLE);
                Editable text = nameTextEdit.getText();
                int index = text.length();
                Selection.setSelection(text, index);                        
            }
            break;
        case DIALOG_SCANNING:            
            Xlog.d(TAG, "onPrepareDialog() mState = "+mState);      
            
            mScanProgressDialog = (ProgressDialog)dialog;//we must reassign the dialog reference here because onCreateDialog will only be called for the 1st time.
            //Window w = mScanProgressDialog.getWindow();    
                            
            //hide the progress number because we don't know how many channels will be scanned at this time.
            dialog.findViewById(com.android.internal.R.id.progress_number).setVisibility(View.INVISIBLE);
            dialog.findViewById(com.android.internal.R.id.progress_percent).setVisibility(View.INVISIBLE);    
            mScanProgressDialog.setIndeterminate(true);            
            break;
            /*
        case DIALOG_PROCESSING: 
            mProgressDialog = (ProgressDialog)dialog;//we must reassign the dialog reference here because onCreateDialog will only be called for the 1st time.
            break;*/

        case DIALOG_CHOOSE_LOCATION: 
            String[] codes = getResources().getStringArray(R.array.supported_location_codes); 
            int i = 0;
            if (mAutoSelection) {
                for(;i<codes.length;i++){
                    if (codes[i].equals("-1")) {
                        break;
                    }
                }    
            } else {
                for(;i<codes.length;i++){
                    if (codes[i].equals(mLocation)) {
                        break;
                    }
                }    
            }
            myAssert(i<codes.length);
            ((AlertDialog)dialog).getListView().setItemChecked(i,true);    
            ((AlertDialog)dialog).getListView().setSelection(i);
            break;
        case DIALOG_DELETE_CONFIRM:    
            int strId = R.string.confirm_delete_message;
            Xlog.d(TAG, "onPrepareDialog() DIALOG_DELETE_CONFIRM  mMultiSelected = "+mMultiSelected);      
            if (isInMultiSelectMode()) {
                if(mMultiSelected.size() > 1 ) {
                    strId = R.string.confirm_delete_multiple_message;
                }                
                     mDeleteAction = new Runnable() {
                        public void run() {                         
                            for (Integer position : mMultiSelected) {                
                                //remove all the selected items from database.                        
                                mChannelProvider.delete(Integer.parseInt(mList.get(position).mCh));                        
                                mList.set((int)position,null);//we can't remove the item here because the order in ArrayList will be shifted after removal.
                            }
                
                            //remove all the selected items from mList
                            ArrayList<ListItem> removal = new ArrayList<ListItem>();
                            removal.add(null);
                            mList.removeAll(removal);    
                            
                            closeMultiSelectMode();                
                        }
                    };
                } else {
                    mDeleteAction = new Runnable() {
                        public void run() {
                            deleteChannel(mListItemClicked);               
                        }
                    };
                }
            String msg = getResources().getString(strId);
                ((AlertDialog)dialog).setMessage(msg);                
                break;
                    
        default:
                break;
        }
    }

    //we don't use a managed dialog here to avoid ALPS00221352.
    private void showVtInUseWarning(boolean toShow) {    
        Xlog.d(TAG, "showVtInUseWarning() toShow = "+toShow);
        
        if (toShow && mVtInUseWarningDialog == null) {        
            mVtInUseWarningDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.vt_in_use_warning)            
                .create();

            mVtInUseWarningDialog.setCanceledOnTouchOutside(false);
            mVtInUseWarningDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                        return true;
                    }
                    return false;
                }
            });        

            mVtInUseWarningDialog.show();
        } else if (!toShow) {
                    // turn on for open channel more quickly when VTcall is not going
                    if (mState == STATE_CREATED) {
                        mEngine.turnOn();
                    }
                    if (mVtInUseWarningDialog != null) {
                        mVtInUseWarningDialog.dismiss();
                        mVtInUseWarningDialog = null;
                    }
        }
    }

    private void abortScanning () {        
        
        Xlog.d(TAG, "abortScanning() mState = "+mState);
        switch (mState) {
            case STATE_INITIALIZING_SCANNING:                
                mState = STATE_CREATED;
                break;

            case STATE_SCANNING:    
                mEngine.stopChannelScan();         
                mState = STATE_INITIALIZED;
                break;    
            //it is amazing to receive a button-click event after onPause,but QA did make it happen!
            case STATE_PAUSED_SCANNING:    
                mEngine.stopChannelScan();
                //fall through
            case STATE_PAUSED_INITIALIZING_SCANNING:                 
                mState = STATE_PAUSED;
                break;                    
            default:
                break;
                //should not assert here because button press event is possible to be received after onScanningDialogDismiss.                
                //myAssert(false);                
        }            
    }
    
    protected Dialog onCreateDialog(int id) {
        Xlog.d(TAG, "onCreateDialog,id = " + id);
        
        switch (id) {
            case DIALOG_CHOOSE_LOCATION: {
                String[] locations = getResources().getStringArray(R.array.supported_locations); 

                AlertDialog dlg = new AlertDialog.Builder(this)
                    .setTitle(R.string.select_location)
                    .setSingleChoiceItems(locations, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {                            
                            Xlog.d(TAG, "DIALOG_CHOOSE_LOCATION,whichButton = " + whichButton);
                            
                            dialog.dismiss();                                          
                            String[] codes = getResources().getStringArray(R.array.supported_location_codes);                             
                            if (codes[whichButton].equals(mLocation)) {
                                //though mLocation is same as the one chosen by SIM info,
                                //but we still need to disable auto selection mode.                            
                                if (mAutoSelection && !codes[whichButton].equals("-1")) {
                                    mAutoSelection = false;
                                    SharedPreferences mPreferences = 
                                        PreferenceManager.getDefaultSharedPreferences(ChannelListActivity.this);                    
                                    SharedPreferences.Editor editor = mPreferences.edit();                                        
                                    editor.putString(KEY_LOCATION,mLocation);
                                    editor.commit();                    
                                }
                            } else {
                                if (mAutoSelection && codes[whichButton].equals("-1")) {//auto-select is chosen again.
                                    return;
                                }                                                        
                                showDialog(DIALOG_PROCESSING);
                                
                                //loading table may need some time,so we put it to message queue to do it later.
                                Message msg = mHandler.obtainMessage(MSG_LOCATION_SELECTED, whichButton,0,codes); 
                                mHandler.sendMessage(msg); 
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //
                        }
                    })
                   .create();
                    
                    dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
                        public void onDismiss(DialogInterface dialog) {
                            removeDialog(DIALOG_CHOOSE_LOCATION);//don't keep the dialog to simply the recreation due to configuration change.
                        }
                    });                    
                    return dlg;
            }
            
        case DIALOG_PROCESSING: {
            //remove the assertion because onCreateDialog/onPrepareDialog will also be called when re-created due to configuration change.
            //in such case,mProgressDialog will be assigned twice on DIALOG_PROCESSING and DIALOG_SCANNING.
                //myAssert(mProgressDialog == null);
                ProgressDialog dlg = new ProgressDialog(this);
                dlg.setMessage(getResources().getString(R.string.please_wait));
                dlg.setIndeterminate(true);
                dlg.setCancelable(true);
                return dlg;
            }
            
        case DIALOG_SCANNING: {
            //myAssert(mProgressDialog == null);
            
            Xlog.d(TAG, "onCreateDialog() DIALOG_SCANNING mScanFinishAfterPaused = "+mScanFinishAfterPaused);            
            //solve ALPS00221352
            //this issue is caused by the execution flow as illustrated below:
            //1.onSaveInstanceState ->saveManagedDialogs(the state of the dialog is saved,expecially DIALOG_SHOWING_TAG)
            //2.onPause
            //3.onScanFinishEvent(will dismiss the dialog,but can not change the saved state,so removeDialog can't help either.)
            //4.(screen orientation) ...restoreManagedDialogs-> onCreateDialog,md.mDialog.onRestoreInstanceState->show(with regard to DIALOG_SHOWING_TAG)
            if (mScanFinishAfterPaused) {
                return null;
            }        
            mScanProgressDialog = new ProgressDialog(this);
            mScanProgressDialog.setTitle(R.string.scanning_channels);
            mScanProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mScanProgressDialog.setMax(MAX_PROGRESS);
            mScanProgressDialog.setCanceledOnTouchOutside(false);
            mScanProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
                public void onDismiss(DialogInterface dialog) {                    
                    onScanningDialogDismiss();                            
                }
            });

            mScanProgressDialog.setButton(getResources().getString(R.string.stop),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    abortScanning();
                } 
            });
            
            mScanProgressDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getAction() == KeyEvent.ACTION_DOWN) {
                        // do nothing since we don't want search box to dismiss the scanning dialog.
                        return true;
                    }
                    return false;
                }
            });
            return mScanProgressDialog;
        }

        case DIALOG_INITIALIZE_HW_FAIL: {
            Xlog.d(TAG, "DIALOG_INITIALIZE_HW_FAIL is showed,mState = "+mState);
            
            AlertDialog dlg = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.analog_tv)
            .setMessage(R.string.hw_init_failed)            
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //empty
                }
            })            
            .create();
                
            dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            }); 
            return dlg;
        }

        case DIALOG_DELETE_CONFIRM:{
            
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                            if (mDeleteAction != null) {
                                mDeleteAction.run();
                                mDeleteAction = null;
                            }
                    }
                }
            };
            AlertDialog dlg = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete)
                .setMessage("")
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
            final int dId = id;            
                dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(dId);//don't keep the dialog to simply the recreation due to configuration change.
                }
            });        
                return dlg;
        }
        
        case DIALOG_SCAN_CONFIRM: {
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.scan)
            .setMessage(R.string.scan_confirm)            
            .setPositiveButton(this.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    tryScan();
                }
            })
            .setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //empty
                }
            })
           .create();
        }
        
        case DIALOG_NO_CHANNEL_FOUND: {
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.scan)            
            .setMessage(R.string.no_channels_found)
            .setNeutralButton(R.string.scan_again,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    tryScan();
               }
            })
            .setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //empty
                }
            })
            .create();

        }    

        case DIALOG_SELECT_LOCATION_FAIL: {
            //TODO:should provide more hint for user,e.g. use manual selection.
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.analog_tv)
            .setMessage(R.string.fail_to_select_location)   
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //empty
                }
            })            
            .create();            
        }
        
        case DIALOG_RENAME: {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.rename_dialog, null);
            final EditText nameTextEdit = (EditText)textEntryView.findViewById(R.id.name_edit);
            AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.rename)
                .setView(textEntryView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = nameTextEdit.getText().toString();
                        if (!"".equals(name)) {
                            ListItem item = mList.get(mListItemClicked);
                            item.mName = name;
                            mChannelProvider.updateChannelName(Integer.parseInt(item.mCh),item.mName); 
                            mAdapter.notifyDataSetChanged();
                        } 
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
                
            dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
                        public void onDismiss(DialogInterface dialog) {
                            removeDialog(DIALOG_RENAME);//don't keep the dialog to simply the recreation due to configuration change.See ALPS00126490
                        }
                    });
            return dlg;
        }  
        
        default:
            break;                    
        }
        return null;
    }    
    
    private void onScanningDialogDismiss() {
        
        Xlog.d(TAG, "onScanningDialogDismiss() this = "+this + " mState = "+mState);        
        //just to make sure the current activity will not be harmed 
        //due to dismiss this dialog by someone else than this activity. see ALPS00047426.
        abortScanning();

           mScanProgressDialog = null;
        if (mList.size() <= 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {//solve ALPS00135556
            ListView list = getListView();
            if (list.isInTouchMode()) {
                list.requestFocus();
            }
        }

        allowSleeping();

    }
        
    @Override
    public void onResume() {
        Xlog.d(TAG, "onResume() mState = "+mState); 
    
        super.onResume();             
        switch (mState) {
            case STATE_PAUSED:    
                //if it is back from MtvPlayer,we need to set EventCallback again.
                if (mGotoPlayer) {
                    mGotoPlayer = false;                    
                    mEngine.setEventCallback(new MtvEngine.EventCallback() {
                        public void onEvent(Message msg) {
                            mHandler.handleMessage(msg);
                        }
                    });    

                    //solve ALPS00128388:if channel is renamed and channel list screen is in landscape mode,then the list will not be updated.
                    mAdapter.notifyDataSetChanged();                    
                } 
                mState = STATE_CREATED;
                //fall through
            case STATE_CREATED:
                        // We will not init here because hardware limitation.ALPS00230598
                //always turn on in case of failure of last turnOn in onCreate.
                //mEngine.turnOn();                
                break;
            case STATE_INITIALIZED://this usally happen when user exit the activity and enter it immediately and in this case the engine is not released yet.
                break;
            case STATE_PAUSED_SCANNING:    
                mState = STATE_SCANNING;
                break;    
            case STATE_SCANNING://means the activity is recreated due to a configuration change.            
                int chNum = mEngine.getChannelNum();
                Xlog.d(TAG, "onResume() chNum = "+chNum); 
                
                if (chNum > 0 ) {//already scan for more than one  channel.
                    if (mScanProgressDialog.isIndeterminate()) {//we can't use getProgress==0 because the value of the progress remains the old value of last session.
                        
                        //stop the indeterminate animation
                        mScanProgressDialog.setIndeterminate(false);
                        
                        //update progress.                        
                        mScanProgressDialog.setProgress(mEngine.getLastChannel());             
                        mScanProgressDialog.setMax(chNum);    
                        
                        //show the progress number and percentage.
                        Window w = mScanProgressDialog.getWindow();     
                        TextView view = (TextView)w.findViewById(com.android.internal.R.id.progress_number);
                        view.setText("");            
                        view.setVisibility(View.VISIBLE);            
                        view = (TextView)w.findViewById(com.android.internal.R.id.progress_percent);
                        SpannableString tmp = new SpannableString(NumberFormat.getPercentInstance().format(mEngine.getLastChannel()/chNum));
                        tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                                        0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);            
                        view.setText(tmp);
                        view.setVisibility(View.VISIBLE);    

                    } else if (mEngine.getLastChannel() == chNum) {//scan is finished during the destroy/create of the activity.
                        onScanFinishEvent(chNum,null);
                    }
                } 
                mEmpty.setVisibility(View.INVISIBLE);
                break;
            case STATE_PAUSED_INITIALIZING_SCANNING:
                mState = STATE_INITIALIZING_SCANNING;
            //fall through
            case STATE_INITIALIZING_SCANNING:
                mEmpty.setVisibility(View.INVISIBLE);
                break;
                
            default:
                //should not go here.
                myAssert(false);                
        }
        
        if (mScanFinishAfterPaused){
            mScanFinishAfterPaused = false;                 
            if (mList.size() <= 0) {
            showDialog(DIALOG_NO_CHANNEL_FOUND);
            }
        }
    }
    
    @Override
    protected void onPause() {
        Xlog.d(TAG, "onPause() mState = "+mState);
        super.onPause();
        
        switch (mState) {
            //case STATE_INITIALIZING_SCANNING:    
                //mScanProgressDialog.dismiss();    
                //fall through
            case STATE_CREATED:    
            case STATE_INITIALIZED:                
                
                if (!mGotoPlayer) {
                    mEngine.turnOff(true);
                }
                mHandler.removeMessages(MSG_UPDATE_SIGNAL_INDICATOR);                
                mState = STATE_PAUSED;
                break;
            case STATE_INITIALIZING_SCANNING:    
                //allow background scanning.                                
                mState = STATE_PAUSED_INITIALIZING_SCANNING;
                break;                    
            case STATE_SCANNING:    
                //allow background scanning.                    
                mState = STATE_PAUSED_SCANNING;
                break;    
                
            default:
                //should not go here.
                myAssert(false);    
                break;
        }        
    }
    

    public void deleteChannel(int position) {
        mChannelProvider.delete(Integer.parseInt(mList.get(position).mCh));
        mAdapter.deleteChannel(position);
    }        
    
    
    private void startWatchTv(Integer num) {
        if (mGotoPlayer) {//solve ALPS00128034 to avoid  this function being executed more than once when clicking in a very quick speed.
            return;
        }    

        //ignore the event because MtvPlayer is not created now,we can still catch it later because we will do watchOn again in onCreate of MtvPlayer.
        mEngine.setEventCallback(null);
        //mEngine.turnOn();

        mGotoPlayer = true;
        Intent intent = new Intent(ChannelListActivity.this,MtvPlayer.class);        
        mEngine.setCurrentChannel(num);
        mEngine.setChannelList(mList);    
        
        startActivity(intent);             
    }

    private void initializeChannelList () {
        mChannelProvider.setTableLocation(MtvEngine.MTV_ATV,mLocation);
        Cursor mCursor = mChannelProvider.getCursor(new String[]{ChannelProvider.CHANNEL_NUM,ChannelProvider.CHANNEL_NAME},false);
        //startManagingCursor(mCursor);        
        mList.clear();
        if (mCursor.getCount()>0) {
            int no = mCursor.getColumnIndex(ChannelProvider.CHANNEL_NUM);
            int name = mCursor.getColumnIndex(ChannelProvider.CHANNEL_NAME);
            //int path = mCursor.getColumnIndex(ChannelProvider.IMAGE_PATH); 
            
            mCursor.moveToFirst();                        
            do {
                mList.add(new ListItem(mCursor.getInt(no),mCursor.getString(name)));
            } while (mCursor.moveToNext());
        }
        mCursor.close();    
        
        //if the channel table is changed,we need to init the tv chip with new table.
        //anyway we just init it every time when we load the channel table to simply the implementation.
        mEngine.setNeedSetTable(true);
    }

    private void initializeLocation() {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);        
        mLocation = mPreferences.getString(KEY_LOCATION,null);
        if (mLocation == null) {
            mLocation = getResources().getString(R.string.default_location); 
            myAssert(mLocation != null);
            SharedPreferences.Editor editor = mPreferences.edit();    
            editor.putString(KEY_LOCATION, mLocation);
            editor.commit();            
        } 

        if ("-1".equals(mLocation)) {
            mAutoSelection = true;
                int i = getAutoSelectLocationIndex();//TODO:implement a bootcompletereceiver and do it only for one time.
                if (i == -1) {
                    mLocation = null;
                } else {
                    String[] codes = getResources().getStringArray(R.array.supported_location_codes); 
                    mLocation = codes[i];
                    SharedPreferences.Editor editor = mPreferences.edit();    
                    editor.putString(KEY_AUTO_LOCATION, mLocation);
                    editor.commit();                                    
                }
        }
        Xlog.d(TAG, "initializeLocation() location = "+mLocation);     
    }    

    
    public class ChannelListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;

        class ViewHolder{
            private TextView mChannelNumber;
            private TextView mChannelName;
            //private ImageView signalIndicator;
            private CheckBox mCheckbox;
        }

        
        public ChannelListAdapter(Context c){
            mInflater =LayoutInflater.from(c);
            if (mLocation == null/*mLocation != null means the activity is recreated because of configuration change*/) {
                initializeLocation();

                if (mLocation != null) {
                    initializeChannelList();
                }
            }
        }
      
        /*
        public void updateSignalIndicator(){
            if (mState != STATE_OPENED) {return;}
            mList.get(mCurrentChannel).signal = mEngine.getSignalStrengh();
            notifyDataSetChanged();
            mMainHandler.sendEmptyMessageDelayed(MSG_UPDATE_SIGNAL_INDICATOR,
                    UPDATE_SIGNAL_PERIOD);
        }        */
        

        public int getCount() {
            return mList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //FIXME:thanks to google,the convertView of the same position may be different through 
            //many times of iteration,so I need to inflate/findView/setText... everytime!!This is ugly!!
            
            ListItem item = null;
            ViewHolder holder;
            View v = convertView;
            if(v == null)
            {      
                
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.channel_list_item, null);
                
                holder.mChannelNumber =(TextView)v.findViewById(R.id.ch_no);
                holder.mChannelName =(TextView)v.findViewById(R.id.ch_name);   
                holder.mCheckbox = (CheckBox)v.findViewById(R.id.check_box);
                v.setTag(holder);

            } else{
                holder=(ViewHolder)v.getTag();
            }    
            
            item = mList.get(position);
            holder.mChannelNumber.setText(getResources().getString(R.string.channel)+" "+item.mCh);
            holder.mChannelName.setText(item.mName);
                       
            //holder.signalIndicator.setImageDrawable(mSignal[item.signal]);
            
            if (isInMultiSelectMode()) {
                if (mMultiSelected.contains(position)) {
                    holder.mCheckbox.setChecked(true);
                } else {
                    holder.mCheckbox.setChecked(false);
                }
                
                holder.mCheckbox.setVisibility(View.VISIBLE);
            } else {
                holder.mCheckbox.setVisibility(View.GONE);
            }
            
            return v;
        }

        public void deleteChannel(int position) {
            mList.remove(position);            
            Xlog.d(TAG, "deleteChannel() mList.size()="+mList.size());                 
            notifyDataSetChanged();
        }
        
        public void addFoundChannel(int c,String n) {
            //mList.add((new ListItem(c,n,noPreviewImage)));
            notifyDataSetChanged();
        }
    }
    
    private void finishSelectionMode() {
        if (mSelectionMode != null) {            
            mSelectionMode.finish();
        }
    }

    /** Update the "selection" action mode bar */
    private void updateSelectionModeView() {
        mSelectionMode.invalidate();
        mAdapter.notifyDataSetChanged();
    }
    
    private class SelectionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mSelectionMode = mode;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.selection_mode_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Set title -- "# selected"
            mSelectionMode.setTitle(getResources().getString(R.string.selected,mMultiSelected.size()));

            int number = mMultiSelected.size();
            if (number == mList.size()) {
                menu.findItem(R.id.do_select_all).setIcon(R.drawable.ic_menu_selectnone);
            } else {
                menu.findItem(R.id.do_select_all).setIcon(R.drawable.ic_menu_selectall);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {            
            /*switch (item.getItemId()) {
                case R.id.do_select_all:
                    if ((mMultiSelected.size() == mList.size())) {
                        mMultiSelected.clear();
                    } else {
                        int size = mList.size();                        
                        for (int i = 0;i < size;i++) {
                            mMultiSelected.add(i);
                        }
                    }
                    updateSelectionModeView();
                    break;
                case R.id.do_delete:
                    if (!mMultiSelected.isEmpty()) {
                        showDialog(DIALOG_DELETE_CONFIRM);
                    }
                    break;
            }*/
            if(item.getItemId() == R.id.do_select_all){
                if ((mMultiSelected.size() == mList.size())) {
                    mMultiSelected.clear();
                } else {
                    int size = mList.size();                        
                    for (int i = 0;i < size;i++) {
                        mMultiSelected.add(i);
                    }
                }
                updateSelectionModeView();
            } else if(item.getItemId() == R.id.do_delete){
                if (!mMultiSelected.isEmpty()) {
                    showDialog(DIALOG_DELETE_CONFIRM);
                }
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectionMode = null;
            if (mMultiSelected != null) {
                mMultiSelected = null;
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
}
