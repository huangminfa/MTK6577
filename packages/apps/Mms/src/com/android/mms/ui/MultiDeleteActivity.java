/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

import static com.android.mms.ui.MessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.MessageListAdapter.PROJECTION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationList.BaseProgressQueryHandler;
import com.android.mms.util.DraftCache;
import com.android.mms.util.ThreadCountManager;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;
import android.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

import com.mediatek.xlog.Xlog;


public class MultiDeleteActivity extends ListActivity {
    
    public static final String TAG = "Mms/MultiDeleteActivity";
    
    private static final int MESSAGE_LIST_QUERY_TOKEN   = 9527;
    private static final int DELETE_MESSAGE_TOKEN       = 9700;

    private static final String FOR_MULTIDELETE         = "ForMultiDelete";

    private ListView mMsgListView;        // ListView for messages in this conversation
    public MessageListAdapter mMsgListAdapter;  // and its corresponding ListAdapter
    
    private boolean mPossiblePendingNotification;   // If the message list has changed, we may have
                                                    // a pending notification to deal with.
    private long threadId;     // Thread we are working in
    private Conversation mConversation;    // Conversation we are working in
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private ThreadCountManager mThreadCountManager = ThreadCountManager.getInstance();
    
    private MenuItem mSelectAll;
    private MenuItem mCancelSelect;
    private MenuItem mDelete;
    private TextView mActionBarText;
    
    private boolean mIsSelectedAll;
    private int mDeleteRunningCount = 0;  // The count of running Message-deleting
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_delete_list_screen);
        setProgressBarVisibility(false);
        
        threadId = getIntent().getLongExtra("thread_id", 0);
        if (threadId == 0) {
            Xlog.e("TAG", "threadId can't be zero");
            finish();
        }
        mConversation = Conversation.get(this, threadId, false);
        mMsgListView = getListView();
        setUpActionBar();
        initMessageList();
        initActivityState(savedInstanceState);
        
        mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mConversation.blockMarkAsRead(true);
        startMsgListQuery();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged " + newConfig);
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);      
        if (mMsgListAdapter != null) {
            if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
                outState.putBoolean("is_all_selected", true);
            } else if (mMsgListAdapter.getSelectedNumber() == 0) {
                return;
            } else {
                long [] checkedArray = new long[mMsgListAdapter.getSelectedNumber()];
                Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                    if (entry.getValue()) {                     
                        checkedArray[i] = entry.getKey();
                        i++;
                    }
                }   
                outState.putLongArray("select_list", checkedArray);
            }
            
        }     
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_multi_select_menu, menu);
        mSelectAll = menu.findItem(R.id.select_all);
        mCancelSelect = menu.findItem(R.id.cancel_select);
        mDelete = menu.findItem(R.id.delete);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int selectNum = getSelectedCount();
        mActionBarText.setText(getResources().getQuantityString(
            R.plurals.message_view_selected_message_count, selectNum, selectNum));
//        mSelectAll.setVisible(!mIsSelectedAll);
//        mCancelSelect.setVisible(hasSelected);
//        mDelete.setVisible(hasSelected);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_all:
                if (!mIsSelectedAll) {
                    mIsSelectedAll = true;
                    markCheckedState(mIsSelectedAll);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.cancel_select:
                if (mMsgListAdapter.getSelectedNumber() > 0) {
                    mIsSelectedAll = false;
                    markCheckedState(mIsSelectedAll);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.delete:
                int mSelectedNumber = mMsgListAdapter.getSelectedNumber();
                if (mSelectedNumber >= mMsgListAdapter.getCount()) {
                    Long threadId = mConversation.getThreadId();
                    MultiDeleteMsgListener mMultiDeleteMsgListener = new MultiDeleteMsgListener();
                    confirmMultiDeleteMsgDialog(mMultiDeleteMsgListener, selectedMsgHasLocked(), true, threadId,
                        MultiDeleteActivity.this);
                } else if (mMsgListAdapter.getSelectedNumber() > 0) {
                    MultiDeleteMsgListener mMultiDeleteMsgListener = new MultiDeleteMsgListener();
                    confirmMultiDeleteMsgDialog(mMultiDeleteMsgListener, selectedMsgHasLocked(), false, null,
                        MultiDeleteActivity.this);
                }
                break;
        }
        return true;
    }
    
    @Override
    protected void onListItemClick(ListView parent, View view, int position, long id) {
        if (view != null) {
            ((MessageListItem) view).onMessageListItemClick();
        }
    }
    
    private void initActivityState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean selectedAll = savedInstanceState.getBoolean("is_all_selected");
            if (selectedAll) {
                mMsgListAdapter.setItemsValue(true, null);
                return;
            } 
            
            long [] selectedItems = savedInstanceState.getLongArray("select_list");
            if (selectedItems != null) {
                mMsgListAdapter.setItemsValue(true, selectedItems);
            }
        }
    }
    
    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.multi_delete_list_actionbar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        ImageButton mQuit = (ImageButton) v.findViewById(R.id.cancel_button);
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MultiDeleteActivity.this.finish();
            }
        });
        
        mActionBarText = (TextView) v.findViewById(R.id.select_items);
        actionBar.setCustomView(v);
    }
    
    private void initMessageList() {
        if (mMsgListAdapter != null) {
            return;
        }

        String highlightString = getIntent().getStringExtra("highlight");
        Pattern highlight = highlightString == null
            ? null
            : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);

        // Initialize the list adapter with a null cursor.
        mMsgListAdapter = new MessageListAdapter(this, null, mMsgListView, true, highlight);
        mMsgListAdapter.mIsDeleteMode = true;
        mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
        mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        mMsgListView.setAdapter(mMsgListAdapter);
        mMsgListView.setItemsCanFocus(false);
        mMsgListView.setVisibility(View.VISIBLE);
    }
    
    private void startMsgListQuery() {
        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        try {
            mBackgroundQueryHandler.postDelayed(new Runnable() {
                public void run() {
                    mBackgroundQueryHandler.startQuery(
                            MESSAGE_LIST_QUERY_TOKEN, threadId, mConversation.getUri(),
                            PROJECTION, null, null, null);
                }
            }, 50);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
    
    private void markCheckedState(boolean checkedState) {
        mMsgListAdapter.setItemsValue(checkedState, null);
        int count = mMsgListView.getChildCount();
        MessageListItem item = null;
        for (int i = 0; i < count; i++) {
            item = (MessageListItem) mMsgListView.getChildAt(i);
            item.setSelectedBackGroud(checkedState);
        }
    }
    
    /**
     * @return the number of messages that are currently selected.
     */
    private int getSelectedCount() {
        return mMsgListAdapter.getSelectedNumber();
    }
    
    @Override
    public void onUserInteraction() {
        checkPendingNotification();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            checkPendingNotification();
        }
    }
    
    private void checkPendingNotification() {
        if (mPossiblePendingNotification && hasWindowFocus()) {
            mConversation.markAsRead();
            mPossiblePendingNotification = false;
        }
    }

    private HashSet<Long> mSelectedLockedMsgIds;

    /**
     * Judge weather selected messages include locked messages or not.
     * 
     * @return
     */
    private boolean selectedMsgHasLocked() {
        boolean mHasLockedMsg = false;
        if (mMsgListAdapter == null) {
            return false;
        }
        mSelectedLockedMsgIds = new HashSet<Long>();
        Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
        Cursor cursor = mMsgListAdapter.getCursor();
        int position = cursor.getPosition();
        int locked = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                if (entry.getValue()) {
                    long mMmsId = entry.getKey();
                    MessageItem m = null;
                    if (mMmsId < 0) {
                        locked = cursor.getInt(mMsgListAdapter.COLUMN_MMS_LOCKED);
                    } else {
                        locked = cursor.getInt(mMsgListAdapter.COLUMN_SMS_LOCKED);
                    }
                    if (locked == 1) {
                        mHasLockedMsg = true;
                        mSelectedLockedMsgIds.add(mMmsId);
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.moveToPosition(position);
        return mHasLockedMsg;
    }

    private boolean isMsgLocked(Map.Entry<Long, Boolean> entry) {
        if (entry == null) {
            return false;
        }
        long mMmsId = entry.getKey();
        MessageItem m = null;
        for (Long selectedMsgIds : mSelectedLockedMsgIds) {
            if (mMmsId == selectedMsgIds) {
                return true;
            }
        }
        return false;
    }

    private void confirmMultiDeleteMsgDialog(final MultiDeleteMsgListener listener,
            boolean hasLockedMessages,
            boolean deleteThread,
            Long threadIds,
            Context context) {
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        TextView msg = (TextView)contents.findViewById(R.id.message);
        if (!deleteThread) {
            msg.setText(getString(R.string.confirm_delete_selected_messages));
        } else {
            listener.setDeleteThread(deleteThread);
            listener.setHasLockedMsg(hasLockedMessages);
            listener.setThreadIds(threadIds);
            if (threadIds == null) {
                msg.setText(R.string.confirm_delete_all_conversations);
            } else {
                // Show the number of threads getting deleted in the confirmation dialog.
                msg.setText(context.getResources().getQuantityString(R.plurals.confirm_delete_conversation, 1, 1));
            }
        }

        final CheckBox checkbox = (CheckBox)contents.findViewById(R.id.delete_locked);
        if (!hasLockedMessages) {
            checkbox.setVisibility(View.GONE);
        } else {
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_dialog_title)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setCancelable(true)
            .setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.no, null)
            .setView(contents)
            .show();
    }
    
    private class MultiDeleteMsgListener implements OnClickListener {
        private boolean mDeleteLockedMessages = false;
        private boolean mDeleteThread = false;
        private boolean mHasLockedMsg = false;
        private Long threadIds = null;
        private ContentResolver mContentResolver = null;

        public MultiDeleteMsgListener() {
        }
        
        public void setContentResolver(ContentResolver contentResolver){
            this.mContentResolver = contentResolver;
        }
        
        public void setThreadIds(Long threadIds) {
            this.threadIds = threadIds;
        }
        
        public void setHasLockedMsg(boolean hasLockedMsg){
            this.mHasLockedMsg = hasLockedMsg;
        }
        
        public void setDeleteThread(boolean deleteThread){
            mDeleteThread = deleteThread;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        public void onClick(DialogInterface dialog, final int whichButton) {
            
            if (mDeleteThread) {
                if ((!mHasLockedMsg) || (mDeleteLockedMessages && mHasLockedMsg)) {

                    int token = ConversationList.DELETE_CONVERSATION_TOKEN;
                    
                        Conversation.startDelete(mBackgroundQueryHandler, token, mDeleteLockedMessages, threadId, 0, 0);
                        DraftCache.getInstance().setDraftState(threadId, false);
                    return;
                }
            }
            mBackgroundQueryHandler.setProgressDialog(DeleteProgressDialogUtil.getProgressDialog(MultiDeleteActivity.this));
            mBackgroundQueryHandler.showProgressDialog();
            new Thread(new Runnable() {
                public void run() {
                    Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
                    Uri deleteSmsUri = null;
                    Uri deleteMmsUri = null;
                    String[] argsSms = new String[mMsgListAdapter.getSelectedNumber()];
                    String[] argsMms = new String[mMsgListAdapter.getSelectedNumber()];
                    int i = 0;
                    int j = 0;
                    while (iter.hasNext()) {
                        @SuppressWarnings("unchecked")
                        Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                        if (!mDeleteLockedMessages) {
                            if (isMsgLocked(entry)) {
                                continue;
                            }
                        }
                        if (entry.getValue()) {
                            if (entry.getKey() > 0){
                                Xlog.i(TAG, "sms");
                                argsSms[i] = Long.toString(entry.getKey());
                                Xlog.i(TAG, "argsSms[i]" + argsSms[i]);
                                deleteSmsUri = Sms.CONTENT_URI;
                                i++;
                            } else {
                                Xlog.i(TAG, "mms");
                                argsMms[j] = Long.toString(-entry.getKey());
                                Xlog.i(TAG, "argsMms[j]" + argsMms[j]);
                                deleteMmsUri = Mms.CONTENT_URI;
                                j++;
                            }
                        }
                    }
                    mBackgroundQueryHandler.setMax(
                            (deleteSmsUri != null ? 1 : 0) +
                            (deleteMmsUri != null ? 1 : 0));
                    if (deleteSmsUri != null) {
                        mDeleteRunningCount++;
                        mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                null, deleteSmsUri, FOR_MULTIDELETE, argsSms);
                    }
                    if (deleteMmsUri != null) {
                        mDeleteRunningCount++;
                        mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                null, deleteMmsUri, FOR_MULTIDELETE, argsMms);
                    }
                    
                    if(deleteSmsUri == null && deleteMmsUri == null){
                        mBackgroundQueryHandler.dismissProgressDialog();
                    }
                }
            }).start();
        }
    }

    private void updateSendFailedNotification() {
        final long threadId = mConversation.getThreadId();
        if (threadId <= 0)
            return;

        // updateSendFailedNotificationForThread makes a database call, so do the work off
        // of the ui thread.
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.updateSendFailedNotificationForThread(
                        MultiDeleteActivity.this, threadId);
            }
        }, "updateSendFailedNotification").start();
    }
    
    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String type;
            switch (msg.what) {
                case MessageListItem.ITEM_CLICK: {
                    //add for multi-delete
                    mMsgListAdapter.changeSelectedState(msg.arg1);
                    if (mMsgListAdapter.getSelectedNumber() > 0) {
                        //mDeleteButton.setEnabled(true);
                        if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
                            mIsSelectedAll = true;
                            invalidateOptionsMenu();
                            return;
                        }
                    } else {
                        //mDeleteButton.setEnabled(false);
                    }
                    mIsSelectedAll = false;
                    invalidateOptionsMenu();
                    return;
                }
                default:
                    Log.w(TAG, "Unknown message: " + msg.what);
                    return;
            }
        }
    };

    private final class BackgroundQueryHandler extends BaseProgressQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    if (cursor == null) {
                        Xlog.w(TAG, "onQueryComplete, cursor is null.");
                        return;
                    }
                    // check consistency between the query result and
                    // 'mConversation'
                    long tid = (Long) cookie;

                    if (tid != mConversation.getThreadId()) {
                        Xlog.d(TAG, "onQueryComplete: msg history query result is for threadId " + tid
                            + ", but mConversation has threadId " + mConversation.getThreadId()
                            + " starting a new query");
                        startMsgListQuery();
                        return;
                    }

                    if (mMsgListAdapter.mIsDeleteMode) {
                        mMsgListAdapter.initListMap(cursor);
                    }

                    mMsgListAdapter.changeCursor(cursor);
                    mConversation.blockMarkAsRead(false);
                    return;
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            Intent mIntent = new Intent();
            switch (token) {
                case ConversationList.DELETE_CONVERSATION_TOKEN:
                    try {
                        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                        if(phone != null) {
                            if(phone.isTestIccCard()) {
                                Xlog.d(TAG, "All messages has been deleted, send notification...");
                                SmsManager.getDefault().setSmsMemoryStatus(true);
                            }
                        } else {
                            Xlog.d(TAG, "Telephony service is not available!");
                        }
                    } catch(Exception ex) {
                        Xlog.e(TAG, "" + ex.getMessage());
                    }
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            MultiDeleteActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(MultiDeleteActivity.this);
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    mIntent.putExtra("delete_all", true);
                    break;
                case DELETE_MESSAGE_TOKEN:
                    if (mDeleteRunningCount > 1) {
                        mDeleteRunningCount--;
                        return;
                    }
                    Xlog.d(TAG, "onDeleteComplete(): before update mConversation, ThreadId = " + mConversation.getThreadId());
                    mConversation = Conversation.upDateThread(MultiDeleteActivity.this, mConversation.getThreadId(), false);
                    mThreadCountManager.isFull(threadId, MultiDeleteActivity.this, 
                            ThreadCountManager.OP_FLAG_DECREASE);
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            MultiDeleteActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(MultiDeleteActivity.this);
                    Xlog.d(TAG, "onDeleteComplete(): MessageCount = " + mConversation.getMessageCount() + 
                            ", ThreadId = " + mConversation.getThreadId());
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    mIntent.putExtra("delete_all", false);
                    mDeleteRunningCount = 0;
                    break;
            }
            setResult(RESULT_OK, mIntent);
            finish();
        }
    }
    
    private final MessageListAdapter.OnDataSetChangedListener mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
        public void onDataSetChanged(MessageListAdapter adapter) {
            mPossiblePendingNotification = true;
        }

        public void onContentChanged(MessageListAdapter adapter) {
            Xlog.d(TAG, "MessageListAdapter.OnDataSetChangedListener.onContentChanged");
            startMsgListQuery();
        }
    };
}
