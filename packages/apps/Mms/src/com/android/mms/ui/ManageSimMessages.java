/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.transaction.MessagingNotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.Telephony.Mms;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.Sms;
import android.telephony.gemini.GeminiSmsManager;
import android.telephony.SmsManager;
import android.telephony.SmsMemoryStatus;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import com.android.mms.util.Recycler;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Displays a list of the SMS messages stored on the ICC.
 */
public class ManageSimMessages extends Activity
        implements View.OnCreateContextMenuListener, View.OnClickListener {
    private static final int DIALOG_REFRESH = 1;        
    private static Uri DELETE_ALL_CONTENT_URI;
    private static Uri MULTI_DELETE_CONTENT_URI;
    private static final String TAG = "ManageSimMessages";
    private static final int MENU_COPY_TO_PHONE_MEMORY = 0;
    private static final int MENU_DELETE_FROM_SIM = 1;
    private static final int MENU_FORWARD = 2;
    private static final int MENU_REPLY =3;
    private static final int MENU_ADD_TO_BOOKMARK      = 4;
    private static final int MENU_CALL_BACK            = 5;
    private static final int MENU_SEND_EMAIL           = 6;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 7;
    private static final int MENU_SEND_SMS              = 9;
    private static final int MENU_ADD_CONTACT           = 10;
    
    private static final int OPTION_MENU_DELETE = 0;
    //MTK_OP01_PROTECT_START
    private static final int OPTION_MENU_SIM_CAPACITY = 1;
    //MTK_OP01_PROTECT_END

    private static final int SHOW_LIST = 0;
    private static final int SHOW_EMPTY = 1;
    private static final int SHOW_BUSY = 2;
    private int mState;
    ProgressDialog dialog;
    private static final String ALL_SMS = "999999"; 
    private static final String FOR_MULTIDELETE = "ForMultiDelete";
    private int currentSlotId = 0;

    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    private ListView mSimList;
    private TextView mMessage;
    private MessageListAdapter mMsgListAdapter = null;
    private AsyncQueryHandler mQueryHandler = null;

    public static final int SIM_FULL_NOTIFICATION_ID = 234;
    public boolean isQuerying = false;
    public boolean isDeleting = false;    
    private boolean isInit = false;
    public static int observerCount = 0; 
    //extract telephony number ...
    private ArrayList<String> mURLs = new ArrayList<String>();
    private ContactList mContactList;

    //add for multi-delete
    private View mDeletePanel;              // View containing the delete and cancel buttons
    private ImageButton mDeleteButton;
    private ImageButton mCancelButton;
    private ImageButton mSelectAllButton;

    private Menu mOptionMenu;
    
    private final ContentObserver simChangeObserver =
            new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfUpdate) {
            if(!isQuerying){
                refreshMessageList();
            }else{
                if(isDeleting == false){
                    observerCount ++;
                }
                Xlog.e(TAG, "observerCount = " + observerCount);                
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Intent it = getIntent();
        currentSlotId = it.getIntExtra("SlotId", 0);
        Xlog.i(TAG, "Got slot id is : " + currentSlotId);
        if (currentSlotId == Phone.GEMINI_SIM_1) {
            DELETE_ALL_CONTENT_URI = Uri.parse("content://sms/icc");
            MULTI_DELETE_CONTENT_URI = Uri.parse("content://sms/icc/#");
        } else if (currentSlotId == Phone.GEMINI_SIM_2) {
            DELETE_ALL_CONTENT_URI = Uri.parse("content://sms/icc2");
            MULTI_DELETE_CONTENT_URI = Uri.parse("content://sms/icc2/#");
        }
        setContentView(R.layout.sim_list);
        
        initResourceRefs();
        
//MTK_OP01_PROTECT_START
        // add for select text copy
        String optr = SystemProperties.get("ro.operator.optr");
        if (null != optr && optr.equals("OP01")) {
            mIsCmcc = true;
        }
        
        if(mIsCmcc){
            float size = MessageUtils.getTextSize(this);
            mTextSize = size;
            if(mMsgListAdapter != null){
                mMsgListAdapter.setTextSize(size);
            }
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
        }
//MTK_OP01_PROTECT_END
        
    }

    private void initResourceRefs() {
        mContentResolver = getContentResolver();
        mQueryHandler = new QueryHandler(mContentResolver, this);
        mSimList = (ListView) findViewById(R.id.messages);
        mMessage = (TextView) findViewById(R.id.empty_message);
        mDeletePanel = findViewById(R.id.delete_panel);
        mSelectAllButton = (ImageButton)findViewById(R.id.select_all);
        mSelectAllButton.setOnClickListener(this);
        mCancelButton = (ImageButton)findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);
        mDeleteButton = (ImageButton)findViewById(R.id.delete);
        mDeleteButton.setOnClickListener(this);
        mDeleteButton.setEnabled(false);

        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        if(FeatureOption.MTK_GEMINI_SUPPORT == true &&
           !TelephonyManager.getDefault().hasIccCardGemini(currentSlotId)){
            mSimList.setVisibility(View.GONE);
            if (currentSlotId == Phone.GEMINI_SIM_1) {
                mMessage.setText(R.string.no_sim_1);
            } else if (currentSlotId == Phone.GEMINI_SIM_2) {
                mMessage.setText(R.string.no_sim_2);
            }
            mMessage.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.sim_manage_messages_title));
            setProgressBarIndeterminateVisibility(false);
        }else if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            try{
                boolean mIsSim1Ready = false;
                if (null != iTelephony) {
                    mIsSim1Ready = iTelephony.isRadioOnGemini(currentSlotId);
                } else {
                    Xlog.e(TAG, "Can not get phone service !");
                }
                
                if(!mIsSim1Ready){
                    mSimList.setVisibility(View.GONE);
                    mMessage.setText(com.mediatek.R.string.sim_close);
                    mMessage.setVisibility(View.VISIBLE);
                    setTitle(getString(R.string.sim_manage_messages_title));
                    setProgressBarIndeterminateVisibility(false);
                }else{
                    isInit = true;
                }
            }catch(RemoteException e){
                Xlog.e(TAG, "RemoteException happens......");
            }
        }else{
            isInit = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        Xlog.d(TAG, "onNewIntent .....");
        currentSlotId = intent.getIntExtra("SlotId", 0);
        Xlog.d(TAG, "onNewIntent Got slot id is : " + currentSlotId);
        if (currentSlotId == Phone.GEMINI_SIM_1) {
            DELETE_ALL_CONTENT_URI = Uri.parse("content://sms/icc");
        } else if (currentSlotId == Phone.GEMINI_SIM_2) {
            DELETE_ALL_CONTENT_URI = Uri.parse("content://sms/icc2");
        }
        init();
    }

    private void init() {
        MessagingNotification.cancelNotification(getApplicationContext(),
                SIM_FULL_NOTIFICATION_ID);

        updateState(SHOW_BUSY);
        startQuery();
    }

    private class QueryHandler extends AsyncQueryHandler {
        private final ManageSimMessages mParent;

        public QueryHandler(
                ContentResolver contentResolver, ManageSimMessages parent) {
            super(contentResolver);
            mParent = parent;
        }

        @Override
        protected void onQueryComplete(
                int token, Object cookie, Cursor cursor) {
            Xlog.d(TAG, "onQueryComplete");
            removeDialog(DIALOG_REFRESH);
            mQueryHandler.removeCallbacksAndMessages(null);
            if(isDeleting) {
                isDeleting = false;
            }
            if (observerCount > 0) {
                ManageSimMessages.this.startQuery();
                observerCount = 0;
                return;
            } else {
                isQuerying = false;
            }
            if (mCursor != null && !mCursor.isClosed()) {
                stopManagingCursor(mCursor);
            }
            
            mCursor = cursor;
            if (mCursor != null) {
                if (!mCursor.moveToFirst()) {
                    // Let user know the SIM is empty
                    updateState(SHOW_EMPTY);
                } else if (mMsgListAdapter == null) {
                    // Note that the MessageListAdapter doesn't support auto-requeries. If we
                    // want to respond to changes we'd need to add a line like:
                    //   mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
                    // See ComposeMessageActivity for an example.
                    mMsgListAdapter = new MessageListAdapter(
                            mParent, mCursor, mSimList, false, null);
                    mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
                    mSimList.setAdapter(mMsgListAdapter);
                    mSimList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (view != null) {
                                MessageListItem mli = (MessageListItem)view;
                                //add for multi-delete
                                if (mli.mSelectedBox != null && mli.mSelectedBox.getVisibility() == View.VISIBLE) {
                                    if (!mli.mSelectedBox.isChecked()) {
                                        mli.setSelectedBackGroud(true);
                                    } else {
                                        mli.setSelectedBackGroud(false);
                                    }
                                    Cursor cursor = (Cursor)mMsgListAdapter.getCursor();
                                    String msgIndex = cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
                                    Xlog.d(MmsApp.TXN_TAG, "simMsg msgIndex = " + msgIndex);
                                    String[] index = msgIndex.split(";");
                                    for (int n = 0; n < index.length; n++) {
                                        mMsgListAdapter.changeSelectedState(index[n]);
                                    }
                                    if (mMsgListAdapter.getSelectedNumber() > 0) {
                                        mDeleteButton.setEnabled(true);
                                    } else {
                                        mDeleteButton.setEnabled(false);
                                    }
                                    return;
                                }
                                mli.onMessageListItemClick();
                            }
                        }
                    });
                    mSimList.setOnCreateContextMenuListener(mParent);
                    updateState(SHOW_LIST);
                } else {
                    mMsgListAdapter.changeCursor(mCursor);
                    updateState(SHOW_LIST);
                }
                startManagingCursor(mCursor);
                registerSimChangeObserver();
            } else {
                // Let user know the SIM is empty
                updateState(SHOW_EMPTY);
            }
            checkDeleteMode();
            if (mMsgListAdapter != null) {
                mMsgListAdapter.initListMap(cursor);
            }
        }
    }

    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {             
                case MessageListItem.ITEM_CLICK: {
                    /*if (mMsgListAdapter.mIsDeleteMode) {
                        //mMsgListAdapter.changeSelectedState(msg.arg1);
                        Xlog.d("MMSLog", "msg.arg1 = " + msg.arg1);
                        Cursor cursor = (Cursor)mMsgListAdapter.getCursor();
                        String msgIndex = cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
                        Xlog.d("MMSLog", "simMsg msgIndex = " + msgIndex);
                        String[] index = msgIndex.split(";");
                        for (int n = 0; n < index.length; n++) {
                            mMsgListAdapter.changeSelectedState(index[n]);
                        }
                        if (mMsgListAdapter.getSelectedNumber() > 0) {
                            mDeleteButton.setEnabled(true);
                            if (mMsgListAdapter.getSelectedNumber() >= mMsgListAdapter.getCount()) {
                                mIsSelectedAll = true;
                                return;
                            }
                        } else {
                            mDeleteButton.setEnabled(false);
                        }
                        mIsSelectedAll = false;
                    }*/
                    break;
                }

                default:                
                    return;
            }
        }
    };


    @Override 
    protected Dialog onCreateDialog(int id){
        switch(id){
            case DIALOG_REFRESH: {
                if (dialog != null && dialog.getContext()!= this){
                    removeDialog(DIALOG_REFRESH);
                    Xlog.d(TAG, "onCreateDialog dialog is not null");
                }
                dialog = new ProgressDialog(this);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setMessage(getString(R.string.refreshing));
                return dialog;
            }
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {          
            case KeyEvent.KEYCODE_BACK:
                if (mMsgListAdapter != null && mMsgListAdapter.mIsDeleteMode) {
                    mMsgListAdapter.mIsDeleteMode = false;
                    checkDeleteMode();
                   return true;
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    //add for multi-delete
    public void onClick(View v) {
        if (v == mDeleteButton) { 
            /*if (mMsgListAdapter.getSelectedNumber() >= mMsgListAdapter.getCount()) {
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(SHOW_BUSY);
                        //deleteAllFromSim();
                        new Thread(new Runnable() {
                            public void run() {
                                deleteAllFromSim();
                            }
                        }, "ManageSimMessages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_all_SIM_messages);
            } else {
                confirmMultiDelete();
            }*/
            confirmMultiDelete();
        } else if (v == mCancelButton) {
            if (mMsgListAdapter != null && mMsgListAdapter.getSelectedNumber() > 0) {
                markCheckedState(false);
            }
        } else if (v == mSelectAllButton) {
            markCheckedState(true);
        }
    }

    private void confirmMultiDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_selected_messages);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 new Thread(new Runnable() {
                     public void run() {
                         Iterator iter = mMsgListAdapter.getSimMsgItemList().entrySet().iterator();
                         String[] argsSimMsg = new String[mMsgListAdapter.getSelectedNumber()];
                         int i = 0;
                         while (iter.hasNext()) {
                             @SuppressWarnings("unchecked")
                             Map.Entry<String, Boolean> entry = (Entry<String, Boolean>) iter.next();
                             if (entry.getValue()) {
                                 argsSimMsg[i] = entry.getKey();
                                 Xlog.d(TAG, "argsSimMsg[i] = " + argsSimMsg[i]);
                                 //deleteSmsUri = ContentUris.withAppendedId(Sms.CONTENT_URI, entry.getKey());
                                 i++;
                             }                             
                         }
                         //Uri deleteUri = ContentUris.withAppendedId(CONTENT_URI, entry.getKey());
                         mQueryHandler.startDelete(/*DELETE_MESSAGE_TOKEN*/2,
                                null, MULTI_DELETE_CONTENT_URI, FOR_MULTIDELETE, argsSimMsg);
                     }
                 }).start();
                 mMsgListAdapter.mIsDeleteMode = false;
                 mDeletePanel.setVisibility(View.GONE);
                 isDeleting = true;
             }
         });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
     }

    private void checkDeleteMode() {
        if (mMsgListAdapter == null) {
            return;
        }
        markCheckedState(false);
        if (mMsgListAdapter.mIsDeleteMode) {
            mDeletePanel.setVisibility(View.VISIBLE);
        } else {
            mDeletePanel.setVisibility(View.GONE);
            //mMsgListAdapter.clearList();
        }
    }

    private void markCheckedState(boolean checkedState) {
        mMsgListAdapter.setSimItemsValue(checkedState, null);
        mDeleteButton.setEnabled(checkedState);
        int count = mSimList.getChildCount();
        MessageListItem item = null;
        for (int i = 0; i < count; i++) {
            item = (MessageListItem)mSimList.getChildAt(i);
            if (null != item) {
                item.setSelectedBackGroud(checkedState);
            }
        }
    }

    private void startQuery() {
        Xlog.d(TAG, "startQuery");                            
        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            showDialog(DIALOG_REFRESH);
        }

        try {
            isQuerying = true;
            mQueryHandler.startQuery(0, null, DELETE_ALL_CONTENT_URI, null, null, null, null);
            Xlog.d(TAG, "startQuery  mQueryHandler.postDelayed ten sec");
            mQueryHandler.postDelayed(new Runnable() {
                public void run() {
                    Xlog.d(TAG, "startQuery  mQueryHandler.postDelayed");        
                    removeDialog(DIALOG_REFRESH);
                    updateState(SHOW_EMPTY);
                    isQuerying = false;
                }
            }, 10000);//
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void refreshMessageList() {
        updateState(SHOW_BUSY);
        if (mCursor != null) {
            stopManagingCursor(mCursor);
            // mCursor.close();
        }
        startQuery();
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        if (mMsgListAdapter != null && mMsgListAdapter.mIsDeleteMode) {
            return;
        }
        menu.setHeaderTitle(R.string.message_options);
        //MTK_OP02_PROTECT_START
        String optr = SystemProperties.get("ro.operator.optr");
        if ("OP02".equals(optr)) {
            AdapterView.AdapterContextMenuInfo info = null;
            try {
                 info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            } catch (ClassCastException exception) {
                Log.e(TAG, "Bad menuInfo.", exception);
            }
            final Cursor cursor = (Cursor) mMsgListAdapter.getItem(info.position);
            addCallAndContactMenuItems(menu, cursor);
            //addRecipientToContact(menu, cursor);
            menu.add(0, MENU_FORWARD, 0, R.string.menu_forward);
            menu.add(0, MENU_REPLY, 0, R.string.menu_reply);
        }
        //MTK_OP02_PROTECT_END
        menu.add(0, MENU_COPY_TO_PHONE_MEMORY, 0,
                R.string.sim_copy_to_phone_memory);
        menu.add(0, MENU_DELETE_FROM_SIM, 0, R.string.sim_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException exception) {
            Log.e(TAG, "Bad menuInfo.", exception);
            return false;
        }

        final Cursor cursor = (Cursor) mMsgListAdapter.getItem(info.position);
        if(cursor == null){
            Xlog.e(TAG, "Bad menuInfo, cursor is null");
            return false;
        }
        switch (item.getItemId()) {
            case MENU_COPY_TO_PHONE_MEMORY:
                copyToPhoneMemory(cursor);
                return true;
            case MENU_DELETE_FROM_SIM:
                final String msgIndex = getMsgIndexByCursor(cursor);
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(SHOW_BUSY);
                        new Thread(new Runnable() {
                            public void run() {
                                deleteFromSim(msgIndex);
                            }
                        }, "ManageSimMessages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_SIM_message);
                return true;
            //MTK_OP02_PROTECT_START
            case MENU_FORWARD:
                forwardMessage(cursor);
                return true;
            case MENU_REPLY:
                replyMessage(cursor);
                return true;
            case MENU_ADD_TO_BOOKMARK:{
                if (mURLs.size() == 1) {
                    Browser.saveBookmark(ManageSimMessages.this, null, mURLs.get(0));
                } else if(mURLs.size() > 1) {
                    CharSequence[] items = new CharSequence[mURLs.size()];
                    for (int i = 0; i < mURLs.size(); i++) {
                        items[i] = mURLs.get(i);
                    }
                    new AlertDialog.Builder(ManageSimMessages.this)
                        .setTitle(R.string.menu_add_to_bookmark)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Browser.saveBookmark(ManageSimMessages.this, null, mURLs.get(which));
                                }
                            })
                        .show();
                }
                return true;
             }
            case MENU_ADD_CONTACT:
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                startActivity(createAddContactIntent(number));
                return true;
            //MTK_OP02_PROTECT_END
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        Xlog.d(TAG, "onResume");                                        
        super.onResume();

        /* If recreate this activity, the dialog and cursor will be restore in method
         * onRestoreInstanceState() which will be invoked between onStart and onResume.
         * Note: The dialog showed before onPause() will be recreated 
         *      (Refer to Activity.onSaveInstanceState() and onRestoreInstanceState()).
         * So, we should initialize in onResume method when it is first time enter this
         * activity, if need.*/
        if (isInit) {
            isInit = false;
            init();
        }
        registerSimChangeObserver();
        if (isDeleting) {
            // This means app is deleting SIM SMS when left activity last time
            refreshMessageList();
        }

        if (isQuerying) {
            // This means app is querying SIM SMS when left activity last time
            showDialog(DIALOG_REFRESH);
        }
 
    }

    @Override
    public void onPause() {
        Xlog.d(TAG, "onPause");                                            
        super.onPause();
        //invalidate cache to refresh contact data
        Contact.invalidateCache();        
        mContentResolver.unregisterContentObserver(simChangeObserver);
    }

    @Override
    public void onStop() {
        Xlog.d(TAG, "onStop");                                            
        super.onStop();
        if (dialog != null) {
            removeDialog(DIALOG_REFRESH);
        }
    }
    
    @Override
    protected void onDestroy() {
        Xlog.d(TAG,"onDestroy");
        super.onDestroy();
        mQueryHandler.removeCallbacksAndMessages(null);
    }

    private void registerSimChangeObserver() {
        mContentResolver.registerContentObserver(
                DELETE_ALL_CONTENT_URI, true, simChangeObserver);
    }

    private void copyToPhoneMemory(Cursor cursor) {
        final String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        final Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        final String serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow("service_center_address"));
        final boolean isIncomingMessage = isIncomingMessage(cursor);
        Xlog.d(MmsApp.TXN_TAG, "\t address \t=" + address);
        Xlog.d(MmsApp.TXN_TAG, "\t body \t=" + body);
        Xlog.d(MmsApp.TXN_TAG, "\t date \t=" + date);
        Xlog.d(MmsApp.TXN_TAG, "\t sc \t=" + serviceCenter);
        Xlog.d(MmsApp.TXN_TAG, "\t isIncoming \t=" + isIncomingMessage);

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (isIncomingMessage) {
                        Xlog.d(MmsApp.TXN_TAG, "Copy incoming sms to phone");
                        if(FeatureOption.MTK_GEMINI_SUPPORT){
                            SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(getApplicationContext(), currentSlotId);
                            if (simInfo != null) {
                                Sms.Inbox.addMessage(mContentResolver, address, body, null, serviceCenter, date, true, (int)simInfo.mSimId);
                            } else {
                                Sms.Inbox.addMessage(mContentResolver, address, body, null, serviceCenter, date, true, -1);
                            }
                        } else {
                                Sms.Inbox.addMessage(mContentResolver, address, body, null, serviceCenter, date, true);
                        }
                    } else {
                        // outgoing sms has not date info
                        Long currentTime = System.currentTimeMillis();
                        Xlog.d(MmsApp.TXN_TAG, "Copy outgoing sms to phone");
                        
                        if(FeatureOption.MTK_GEMINI_SUPPORT){
                            SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(getApplicationContext(), currentSlotId);
                            if (simInfo != null) {
                                Sms.Sent.addMessage(mContentResolver, address, body, null, serviceCenter, currentTime, (int)simInfo.mSimId);
                            } else {
                                Sms.Sent.addMessage(mContentResolver, address, body, null, serviceCenter, currentTime, -1);
                            }
                        } else {
                            Sms.Sent.addMessage(mContentResolver, address, body, null, serviceCenter, currentTime);
                        }
                    }
                    Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
                    MmsApp.getToastHandler().sendEmptyMessage(MmsApp.MSG_DONE);
                } catch (SQLiteException e) {
                    SqliteWrapper.checkSQLiteException(getApplicationContext(), e);
                }
            }
        }, "copyToPhoneMemory").start();
    }

    private boolean isIncomingMessage(Cursor cursor) {
        int messageStatus = cursor.getInt(
                cursor.getColumnIndexOrThrow("status"));
        Xlog.d(MmsApp.TXN_TAG, "message status:" + messageStatus);
        return (messageStatus == SmsManager.STATUS_ON_ICC_READ) ||
               (messageStatus == SmsManager.STATUS_ON_ICC_UNREAD);
    }

    private String getMsgIndexByCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
    }

private void deleteFromSim(String msgIndex) {
        /* 1. Non-Concatenated SMS's message index string is like "1"
         * 2. Concatenated SMS's message index string is like "1;2;3;".
         * 3. If a concatenated SMS only has one segment stored in SIM Card, its message 
         *    index string is like "1;".
         */
        String[] index = msgIndex.split(";");
        Uri simUri = DELETE_ALL_CONTENT_URI.buildUpon().build();
        if (SqliteWrapper.delete(this, mContentResolver, simUri, FOR_MULTIDELETE, index) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }

    private void deleteFromSim(Cursor cursor) {
        String msgIndex = getMsgIndexByCursor(cursor);
        deleteFromSim(msgIndex);
    }

    private void deleteAllFromSim() {
        // For Delete all,MTK FW support delete all using messageIndex = -1, here use 999999 instead of -1;
        String messageIndexString = ALL_SMS;
        //cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
        Uri simUri = DELETE_ALL_CONTENT_URI.buildUpon().appendPath(messageIndexString).build();
        Xlog.i(TAG, "delete simUri: " + simUri);
        if (SqliteWrapper.delete(this, mContentResolver, simUri, null, null) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (mOptionMenu != null) {
            mOptionMenu.clear();
        }
        MenuItem miDeleteAll = menu.add(0, OPTION_MENU_DELETE, 0, R.string.menu_delete_messages).setIcon(
                    android.R.drawable.ic_menu_delete);

        if ((null != mCursor) && (mCursor.getCount() > 0) 
                && mState == SHOW_LIST && !mMsgListAdapter.mIsDeleteMode) {
            miDeleteAll.setEnabled(true);
        } else {
            miDeleteAll.setEnabled(false);
        }
        
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP01_PROTECT_START
        if ("OP01".equals(optr)) {
            MenuItem miSimCapacity = menu.add(0, OPTION_MENU_SIM_CAPACITY, 0, R.string.menu_show_icc_sms_capacity).setIcon(
                        R.drawable.ic_menu_sim_capacity);
            if (mState == SHOW_LIST || mState == SHOW_EMPTY) {
                miSimCapacity.setEnabled(true);
            } else {
                miSimCapacity.setEnabled(false);
            }
        }
        //MTK_OP01_PROTECT_END
        mOptionMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case OPTION_MENU_DELETE:
                mMsgListAdapter.mIsDeleteMode = true;
                item.setVisible(false);
                checkDeleteMode();
                /*
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(SHOW_BUSY);
                        //deleteAllFromSim();
                        new Thread(new Runnable() {
                            public void run() {
                                deleteAllFromSim();
                            }
                        }, "ManageSimMessages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_all_SIM_messages);
                */
                break;
            //MTK_OP01_PROTECT_START
            case OPTION_MENU_SIM_CAPACITY:
                SmsMemoryStatus SimMemStatus = null;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    SimMemStatus = GeminiSmsManager.getSmsSimMemoryStatusGemini(currentSlotId);
                } else {
                    SimMemStatus = SmsManager.getDefault().getSmsSimMemoryStatus();
                }

                String message = null;
                if (null != SimMemStatus) {
                    message = getString(R.string.icc_sms_used) + Integer.toString(SimMemStatus.getUsed())
                                + "\n" + getString(R.string.icc_sms_total) + Integer.toString(SimMemStatus.getTotal());
                } else {
                    message = getString(R.string.get_icc_sms_capacity_failed);
                }
                new AlertDialog.Builder(ManageSimMessages.this)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(R.string.show_icc_sms_capacity_title)
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, null)
                            .setCancelable(true)
                            .show();
                break;
            //MTK_OP01_PROTECT_END
        }

        return true;
    }

    private void confirmDeleteDialog(OnClickListener listener, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.setMessage(messageId);

        builder.show();
    }

    private void updateState(int state) {
        Xlog.d(TAG, "updateState, state = "+ state);            
        if (mState == state) {
            return;
        }

        mState = state;
        switch (state) {
            case SHOW_LIST:
                mSimList.setVisibility(View.VISIBLE);
                mSimList.requestFocus();
                mSimList.setSelection(mSimList.getCount()-1);
                mMessage.setVisibility(View.GONE);
                setTitle(getString(R.string.sim_manage_messages_title));
                setProgressBarIndeterminateVisibility(false);
                break;
            case SHOW_EMPTY:
                mSimList.setVisibility(View.GONE);
                mMessage.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.sim_manage_messages_title));
                setProgressBarIndeterminateVisibility(false);
                break;
            case SHOW_BUSY:
                mSimList.setVisibility(View.GONE);
                mMessage.setVisibility(View.GONE);
                setTitle(getString(R.string.refreshing));
                setProgressBarIndeterminateVisibility(true);
                break;
            default:
                Log.e(TAG, "Invalid State");
        }
    }

    private void viewMessage(Cursor cursor) {
        // TODO: Add this.
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to startActivityForResult: " + intent);
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to startActivityForResult: " + intent);
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
          }
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
    
    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent mChooserIntent = Intent.createChooser(intent, null);
            super.startActivity(mChooserIntent);
        }
    }
    
    
    
    
    //MTK_OP02_PROTECT_START
    private void forwardMessage(Cursor cursor) {
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Intent intent = new Intent();
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        intent.putExtra(ComposeMessageActivity.FORWARD_MESSAGE, true);
        if (body != null) {
            intent.putExtra(ComposeMessageActivity.SMS_BODY, body);
        }
        
        startActivity(intent);
    }
    
    private void replyMessage(Cursor cursor) {
        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("sms", address, null));
        startActivity(intent);
    }

    private final void addCallAndContactMenuItems(ContextMenu menu, Cursor cursor) {
        // Add all possible links in the address & message
        StringBuilder textToSpannify = new StringBuilder();  
        String reciBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        String reciNumber = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        textToSpannify.append(reciNumber + ": ");
        textToSpannify.append(reciBody);
        SpannableString msg = new SpannableString(textToSpannify.toString());
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
        mURLs.clear();
        Log.d(TAG, "addCallAndContactMenuItems uris.size() = " + uris.size());
        while (uris.size() > 0) {
            String uriString = uris.remove(0);
            // Remove any dupes so they don't get added to the menu multiple times
            while (uris.contains(uriString)) {
                uris.remove(uriString);
            }

            int sep = uriString.indexOf(":");
            String prefix = null;
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                if ("mailto".equalsIgnoreCase(prefix) || "tel".equalsIgnoreCase(prefix)){
                    uriString = uriString.substring(sep + 1);
                }
            }
            boolean addToContacts = false;
            if ("mailto".equalsIgnoreCase(prefix)) {
                String sendEmailString = getString(R.string.menu_send_email).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mailto:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                menu.add(0, MENU_SEND_EMAIL, 0, sendEmailString).setIntent(intent);
                addToContacts = !haveEmailContact(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                addToContacts = !isNumberInContacts(uriString);
                Xlog.d(TAG, "addCallAndContactMenuItems  addToContacts2 = " + addToContacts);
            } else {
                //add URL to book mark
                if (mURLs.size() <= 0) {
                    menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark);
                }
                mURLs.add(uriString);
            }
            if (addToContacts) {
                 Intent intent = ConversationList.createAddContactIntent(uriString);
                //Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, uriString);
                String addContactString = getString(
                        R.string.menu_add_address_to_contacts).replace("%s", uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setIntent(intent);
            }
        }
    }
    
    private boolean addRecipientToContact(ContextMenu menu, Cursor cursor){
        boolean showAddContact = false;
        String reciNumber = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        Log.d(TAG, "addRecipientToContact reciNumber = " + reciNumber);
        // if there is at least one number not exist in contact db, should show add.
        mContactList = ContactList.getByNumbers(reciNumber, false, true);
        for (Contact contact : mContactList) {
            if (!contact.existsInDatabase()) {
                 showAddContact = true;
                 Log.d(TAG, "not in contact[number:" + contact.getNumber() + ",name:" + contact.getName());
                 break;
             }
         }
        boolean menuAddExist = (menu.findItem(MENU_ADD_CONTACT)!= null);
        if (showAddContact) {
            if (!menuAddExist) {
                menu.add(0, MENU_ADD_CONTACT, 1, R.string.menu_add_to_contacts).setIcon(R.drawable.ic_menu_contact);
            }
        } else {
             menu.removeItem(MENU_ADD_CONTACT);
        }
        return true;
    }
    private boolean isNumberInContacts(String phoneNumber) {
        return Contact.get(phoneNumber, true).existsInDatabase();
    }
    
    private boolean haveEmailContact(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Contacts.DISPLAY_NAME }, null, null, null);

        if (cursor != null) {
            try {
                String name;
                while (cursor.moveToNext()) {
                    name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }
    
    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return intent;
    }
    //MTK_OP02_PROTECT_END
    
    @Override
    public boolean  dispatchTouchEvent(MotionEvent event){
        
        boolean ret = false;

//MTK_OP01_PROTECT_START
        if(mIsCmcc && mScaleDetector != null){
                ret = mScaleDetector.onTouchEvent(event);
        }
//MTK_OP01_PROTECT_END
        
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    }
    
//MTK_OP01_PROTECT_START
    
    private final int DEFAULT_TEXT_SIZE = 18;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 32;
    private ScaleDetector mScaleDetector;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float MIN_ADJUST_TEXT_SIZE = 0.2f;
    private boolean mIsCmcc = false;    
    
    // add for cmcc changTextSize by multiTouch
    private void changeTextSize(float size){
        if(mMsgListAdapter != null){
            mMsgListAdapter.setTextSize(size);
        }
        
        if(mSimList != null && mSimList.getVisibility() == View.VISIBLE){
            int count = mSimList.getChildCount();
            for(int i = 0; i < count; i++){
                MessageListItem item =  (MessageListItem)mSimList.getChildAt(i);
                if(item != null){
                    item.setBodyTextSize(size);
                }
            }
        }
    }    
    
    public class ScaleListener implements OnScaleListener{
        
        public boolean onScaleStart(ScaleDetector detector) {
            Xlog.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
            return true;
        }
        
        public void onScaleEnd(ScaleDetector detector) {
            Xlog.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);
            
            //save current value to preference
            MessageUtils.setTextSize(ManageSimMessages.this, mTextSize);
        }
        
        public boolean onScale(ScaleDetector detector) {

            float size = mTextSize * detector.getScaleFactor();
            
            if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
                return false;
            }            
            if(size < MIN_TEXT_SIZE){
                size = MIN_TEXT_SIZE;
            }            
            if(size > MAX_TEXT_SIZE){
                size = MAX_TEXT_SIZE;
            }            
            if(size != mTextSize){
                changeTextSize(size);
                mTextSize = size;
            }
            return true;
        }
    };
//MTK_OP01_PROTECT_END
}

