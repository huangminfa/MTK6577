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

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import com.android.mms.R;
import com.android.mms.ui.MessageCursorAdapter;
import com.google.android.mms.MmsException;

import java.util.regex.Pattern;

//a0
import android.content.ContentUris;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.View.OnCreateContextMenuListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
// add for gemini
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
//a1

/**
 * The back-end data adapter of a message list.
 */
public class MessageListAdapter extends MessageCursorAdapter {
    private static final String TAG = "MessageListAdapter";
    public static final String CACHE_TAG = "Mms/MessageItemCache";
    private static final boolean LOCAL_LOGV = false;

    static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        //a0
        Sms.SIM_ID,        
        Mms.SIM_ID,
        Sms.SERVICE_CENTER,
        Mms.SERVICE_CENTER
        //a1
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_MSG_TYPE            = 0;
    static final int COLUMN_ID                  = 1;
    static final int COLUMN_THREAD_ID           = 2;
    static final int COLUMN_SMS_ADDRESS         = 3;
    static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SMS_DATE            = 5;
    static final int COLUMN_SMS_DATE_SENT       = 6;
    static final int COLUMN_SMS_READ            = 7;
    static final int COLUMN_SMS_TYPE            = 8;
    static final int COLUMN_SMS_STATUS          = 9;
    static final int COLUMN_SMS_LOCKED          = 10;
    static final int COLUMN_SMS_ERROR_CODE      = 11;
    static final int COLUMN_MMS_SUBJECT         = 12;
    static final int COLUMN_MMS_SUBJECT_CHARSET = 13;
    static final int COLUMN_MMS_DATE            = 14;
    static final int COLUMN_MMS_DATE_SENT       = 15;
    static final int COLUMN_MMS_READ            = 16;
    static final int COLUMN_MMS_MESSAGE_TYPE    = 17;
    static final int COLUMN_MMS_MESSAGE_BOX     = 18;
    static final int COLUMN_MMS_DELIVERY_REPORT = 19;
    static final int COLUMN_MMS_READ_REPORT     = 20;
    static final int COLUMN_MMS_ERROR_TYPE      = 21;
    static final int COLUMN_MMS_LOCKED          = 22;
    //a0
    static final int COLUMN_SMS_SIMID           = 23;
    static final int COLUMN_MMS_SIMID           = 24;
    static final int COLUMN_SMS_SERVICE_CENTER  = 25;
    static final int COLUMN_MMS_SERVICE_CENTER  = 26;
    //a1

    private static final int CACHE_SIZE         = 50;

    public static final int INCOMING_ITEM_TYPE = 0;
    public static final int OUTGOING_ITEM_TYPE = 1;

    protected LayoutInflater mInflater;
    private final LruCache<Long, MessageItem> mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Handler mMsgListItemHandler;
    private Pattern mHighlight;
    private Context mContext;

    public MessageListAdapter(
            Context context, Cursor c, ListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mContext = context;
        mHighlight = highlight;

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMessageItemCache = new LruCache<Long, MessageItem>(CACHE_SIZE);
        
        //a0
        mListItem = new HashMap<Long, Boolean>();
        mSimMsgListItem = new HashMap<String, Boolean>();
        //a1

        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    MessageListItem mli = (MessageListItem) view;
                    // Clear references to resources
                    mli.unbind();
                }
            }
        });
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Xlog.d(CACHE_TAG, "bindView() start.");
        if (view instanceof MessageListItem) {
            if (!mIsScrolling || mIsDeleteMode) {
                String type = cursor.getString(mColumnsMap.mColumnMsgType);
                long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                Xlog.d(CACHE_TAG, "bindView(): type=" + type + ", msgId=" + msgId);
                MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
                if (msgItem != null) {
                    MessageListItem mli = (MessageListItem) view;
                    //a0
                    //for multi-delete
                    if (mIsDeleteMode) {
                        if (msgItem.isSimMsg()) {
                            String msgIndex = cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
                            Xlog.d(CACHE_TAG, "bindView(): type=" + type + ",simMsg msgIndex=" + msgIndex);
                            String[] index = msgIndex.split(";");
                            for (int n = 0; n < index.length; n++) {
                                if (mSimMsgListItem.get(index[n]) == null) {
                                    mSimMsgListItem.put(index[n], false);
                                } else {
                                    msgItem.setSelectedState(mSimMsgListItem.get(index[n]));
                                }
                            }
                        } else {
                            msgId = getKey(type, msgId);
                            if (mListItem.get(msgId) == null) {
                                mListItem.put(msgId, false);
                            } else {
                                msgItem.setSelectedState(mListItem.get(msgId));
                            }
                        }
                    }
                    //a1
                    mli.bind(msgItem, cursor.getPosition() == cursor.getCount() - 1, mIsDeleteMode);
                    mli.setMsgListItemHandler(mMsgListItemHandler);
                } else {
                    MessageListItem mli = (MessageListItem) view;
                    mli.bindDefault(cursor.getPosition() == cursor.getCount() - 1);
                }
            } else {
                MessageListItem mli = (MessageListItem) view;
                mli.bindDefault(cursor.getPosition() == cursor.getCount() - 1);
            }
        }
        
//MTK_OP01_PROTECT_START
        // add for text zoom
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && optr.equals("OP01")) {
            MessageListItem mli = (MessageListItem) view;
            mli.setBodyTextSize(mTextSize);
        }
//MTK_OP01_PROTECT_END

    }

    public interface OnDataSetChangedListener {
        void onDataSetChanged(MessageListAdapter adapter);
        void onContentChanged(MessageListAdapter adapter);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }

    public OnDataSetChangedListener getOnDataSetChangedListener() {
        return mOnDataSetChangedListener;
    }
    
    public void setMsgListItemHandler(Handler handler) {
        mMsgListItemHandler = handler;
    }

    private boolean mClearCacheFlag = true;
    public void setClearCacheFlag(boolean clearCacheFlag) {
        mClearCacheFlag = clearCacheFlag;
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (LOCAL_LOGV) {
            Log.v(TAG, "MessageListAdapter.notifyDataSetChanged().");
        }

        if (mClearCacheFlag) {
            mMessageItemCache.evictAll();
        }
        mClearCacheFlag = true;

        if (mOnDataSetChangedListener != null) {
            mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnDataSetChangedListener != null) {
                mOnDataSetChangedListener.onContentChanged(this);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(getItemViewType(cursor) == INCOMING_ITEM_TYPE ?
                R.layout.message_list_item_recv : R.layout.message_list_item_send,
                parent, false);
    }

    static final int MSG_LIST_NEED_REFRASH   = 3;

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        final long key = getKey(type, msgId);
        MessageItem item = mMessageItemCache.get(key);
        Xlog.d(CACHE_TAG, "getCachedMessageItem(): key=" + key + ", item is in cache?=" + (item != null));
        if (item == null && c != null && isCursorValid(c)) {
            if (type.equals("mms")) {
                Xlog.d(CACHE_TAG, "getCachedMessageItem(): no cache, create one MessageItem on background.");
                final int boxId = c.getInt(mColumnsMap.mColumnMmsMessageBox);
                final int messageType = c.getInt(mColumnsMap.mColumnMmsMessageType);
                final int simId = FeatureOption.MTK_GEMINI_SUPPORT ? c.getInt(mColumnsMap.mColumnSmsSimId) : -1;
                final int errorType = c.getInt(mColumnsMap.mColumnMmsErrorType);
                final int locked = c.getInt(mColumnsMap.mColumnMmsLocked);
                final int charset = c.getInt(mColumnsMap.mColumnMmsSubjectCharset);
                final long mMsgId = msgId;
                final String mmsType = type;
                final String subject = c.getString(mColumnsMap.mColumnMmsSubject);
                final String serviceCenter = c.getString(mColumnsMap.mColumnSmsServiceCenter);
                final String deliveryReport = c.getString(mColumnsMap.mColumnMmsDeliveryReport);
                final String readReport = c.getString(mColumnsMap.mColumnMmsReadReport);
                final Pattern highlight = mHighlight;
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Xlog.d(CACHE_TAG, "getCachedMessageItem(): call UI thread notify data set change.");
                        final Message msg = Message.obtain(mMsgListItemHandler, MSG_LIST_NEED_REFRASH);
                        msg.sendToTarget();
                    }
                };
                final Object object = new Object();
                pushTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MessageItem backgroundItem = mMessageItemCache.get(key);
                            if (backgroundItem == null) {
                                backgroundItem = new MessageItem(mContext, boxId, messageType, simId, errorType,
                                        locked, charset, mMsgId, mmsType, subject, serviceCenter, deliveryReport,
                                        readReport, highlight);
                                Xlog.d(CACHE_TAG,
                                        "getCachedMessageItem(): put new MessageItem into cache, messageId = -"
                                        + backgroundItem.mMsgId);
                                mMessageItemCache.put(key, backgroundItem);
                                mMsgListItemHandler.postDelayed(r, 200);
                            }
                            synchronized (object) {
                                object.notifyAll();
                            }
                        } catch (MmsException e) {
                            Log.e(TAG, "getCachedMessageItem: ", e);
                        }
                    }
                });

                synchronized (object) {
                    try {
                        int waitTime = 1300;
                        object.wait(waitTime);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                }
                item = mMessageItemCache.get(key);
                if (item != null) {
                    Xlog.d(CACHE_TAG, "getCachedMessageItem(): get item during wait.");
                    Xlog.d(CACHE_TAG, "getCachedMessageItem(): cancel UI thread notify data set change.");
                    mMsgListItemHandler.removeCallbacks(r);
                }
            } else {
                try {
                    item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight);
                    mMessageItemCache.put(key, item);
                } catch (MmsException e) {
                    Log.e(TAG, "getCachedMessageItem: ", e);
                }
            }
        }
        return item;
    }

    private boolean isCursorValid(Cursor cursor) {
        // Check whether the cursor is valid or not.
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    private static long getKey(String type, long id) {
        if (type.equals("mms")) {
            return -id;
        } else {
            return id;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /* MessageListAdapter says that it contains two types of views. Really, it just contains
     * a single type, a MessageListItem. Depending upon whether the message is an incoming or
     * outgoing message, the avatar and text and other items are laid out either left or right
     * justified. That works fine for everything but the message text. When views are recycled,
     * there's a greater than zero chance that the right-justified text on outgoing messages
     * will remain left-justified. The best solution at this point is to tell the adapter we've
     * got two different types of views. That way we won't recycle views between the two types.
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 2;   // Incoming and outgoing messages
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor)getItem(position);
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        String type = cursor.getString(mColumnsMap.mColumnMsgType);
        int boxId;
        if ("sms".equals(type)) {
            long status = cursor.getLong(mColumnsMap.mColumnSmsStatus);
            // check sim sms and set box id
            if (status == SmsManager.STATUS_ON_ICC_SENT 
                    || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                boxId = Sms.MESSAGE_TYPE_SENT;
            } else if (status == SmsManager.STATUS_ON_ICC_READ
                    || status == SmsManager.STATUS_ON_ICC_UNREAD) {
                boxId = Sms.MESSAGE_TYPE_INBOX;
            } else {
                boxId = cursor.getInt(mColumnsMap.mColumnSmsType);
            }
        } else {
            boxId = cursor.getInt(mColumnsMap.mColumnMmsMessageBox);
        }
        return boxId == Mms.MESSAGE_BOX_INBOX ? INCOMING_ITEM_TYPE : OUTGOING_ITEM_TYPE;
    }

    public static class ColumnsMap {
        public int mColumnMsgType;
        public int mColumnMsgId;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        //a0
        public int mColumnSmsSimId;
        public int mColumnMmsSimId;
        public int mColumnSmsServiceCenter;
        public int mColumnMmsServiceCenter;
        //a1

        public ColumnsMap() {
            mColumnMsgType            = COLUMN_MSG_TYPE;
            mColumnMsgId              = COLUMN_ID;
            mColumnSmsAddress         = COLUMN_SMS_ADDRESS;
            mColumnSmsBody            = COLUMN_SMS_BODY;
            mColumnSmsDate            = COLUMN_SMS_DATE;
            mColumnSmsDateSent        = COLUMN_SMS_DATE_SENT;
            mColumnSmsType            = COLUMN_SMS_TYPE;
            mColumnSmsStatus          = COLUMN_SMS_STATUS;
            mColumnSmsLocked          = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode       = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject         = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset  = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType     = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox      = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport  = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport      = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType       = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked          = COLUMN_MMS_LOCKED;
            //a0
            mColumnSmsSimId           = COLUMN_SMS_SIMID;
            mColumnMmsSimId           = COLUMN_MMS_SIMID;
            mColumnSmsServiceCenter   = COLUMN_SMS_SERVICE_CENTER;
            mColumnMmsServiceCenter   = COLUMN_MMS_SERVICE_CENTER;
            //a1
        }

        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(
                        MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Sms.BODY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Sms.DATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDateSent = cursor.getColumnIndexOrThrow(Sms.DATE_SENT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Sms.TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Sms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            
            try {
                mColumnSmsSimId = cursor.getColumnIndexOrThrow(Sms.SIM_ID);
            } catch (IllegalArgumentException e) {
                Xlog.w(TAG, e.getMessage());
            }


            try {
                mColumnMmsSimId = cursor.getColumnIndexOrThrow(Mms.SIM_ID);
            } catch (IllegalArgumentException e) {
                Xlog.w(TAG, e.getMessage());
            }
            
            try {
                mColumnSmsServiceCenter = cursor.getColumnIndexOrThrow(Sms.SERVICE_CENTER);
            } catch (IllegalArgumentException e) {
                Xlog.w(TAG, e.getMessage());
            }

            try {
                mColumnMmsServiceCenter = cursor.getColumnIndexOrThrow(Mms.SERVICE_CENTER);
            } catch (IllegalArgumentException e) {
                Xlog.w(TAG, e.getMessage());
            }
        }
    }
    
    //a0
    //add for multi-delete
    public boolean mIsDeleteMode = false;
    private Map<Long, Boolean> mListItem;
    private Map<String, Boolean> mSimMsgListItem;
    
    //add for multi-delete
    public void changeSelectedState(long listId) {
        mListItem.put(listId, !mListItem.get(listId));
        
    }

    public void changeSelectedState(String listId) {
        mSimMsgListItem.put(listId, !mSimMsgListItem.get(listId));
        
    }
    
    public  Map<Long, Boolean> getItemList() {
        return mListItem;
        
    }

    public  Map<String, Boolean> getSimMsgItemList() {
        return mSimMsgListItem;
        
    }
    
    public Uri getMessageUri(long messageId) {
        Uri messageUri = null;
        if (messageId > 0) {
            messageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, messageId);
        } else {
            messageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, -messageId);
        }        
        return messageUri;
    }
    
    public void initListMap(Cursor cursor) {
        if (cursor != null) {
            long itemId = 0;
            String type;
            long msgId = 0L;
            long status = 0L;
            boolean isSimMsg = false;
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                status = cursor.getLong(mColumnsMap.mColumnSmsStatus);
                if (status == SmsManager.STATUS_ON_ICC_READ
                        || status == SmsManager.STATUS_ON_ICC_UNREAD
                        || status == SmsManager.STATUS_ON_ICC_SENT
                        || status == SmsManager.STATUS_ON_ICC_UNSENT) {
                    isSimMsg = true;
                }
                if (isSimMsg) {
                    String msgIndex = cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
                    String[] index = msgIndex.split(";");
                    for (int n = 0; n < index.length; n++) {
                        if (mSimMsgListItem.get(index[n]) == null) {
                            mSimMsgListItem.put(index[n], false);
                        }
                    }
                } else {
                    type = cursor.getString(mColumnsMap.mColumnMsgType);
                    msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                    //MessageItem item = getCachedMessageItem(type, msgId, cursor);
                    itemId = getKey(type, msgId);
        
                    if (mListItem.get(itemId) == null) {
                        mListItem.put(itemId, false);
                    }
                }
            }
        }  
    }
    
    public void setItemsValue(boolean value, long[] keyArray) {
        Iterator iter = mListItem.entrySet().iterator();
        //keyArray = null means set the all item
        if (keyArray == null) {
            while (iter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                entry.setValue(value);
            }
        } else {
            for (int i = 0; i < keyArray.length; i++) {
                mListItem.put(keyArray[i], value);
            }
        }
    }

    public void setSimItemsValue(boolean value, long[] keyArray) {
        Iterator iter = mSimMsgListItem.entrySet().iterator();
        //keyArray = null means set the all item
        if (keyArray == null) {
            while (iter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Boolean> entry = (Entry<String, Boolean>) iter.next();
                entry.setValue(value);
            }
        } else {
            // TODO: 
        }
    }
    
    public void clearList() {
        if (mListItem != null) {
            mListItem.clear();
        }
        if (mSimMsgListItem != null) {
            mSimMsgListItem.clear();
        }
    }

    public int getSelectedNumber() {
        int number = 0;
        if (mListItem != null) {
            Iterator iter = mListItem.entrySet().iterator();
            while (iter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                if (entry.getValue()) {
                    number++;
                }
            }
        }
        if (mSimMsgListItem != null) {
            Iterator simMsgIter = mSimMsgListItem.entrySet().iterator();
            while (simMsgIter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Boolean> entry = (Entry<String, Boolean>) simMsgIter.next();
                if (entry.getValue()) {
                    number++;
                }
            }
        }
        return number;
    }
//MTK_OP01_PROTECT_START
    // add for text zoom
    private final int DEFAULT_TEXT_SIZE = 18;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    public void setTextSize(float size) {
        mTextSize = size;
    }
//MTK_OP01_PROTECT_END

    private static class TaskStack {
        boolean mThreadOver = false;
        Thread mWorkerThread;
        private final ArrayList<Runnable> mThingsToLoad;

        public TaskStack() {
            mThingsToLoad = new ArrayList<Runnable>();
            mWorkerThread = new Thread(new Runnable() {
                public void run() {
                    while (!mThreadOver) {
                        Runnable r = null;
                        synchronized (mThingsToLoad) {
                            if (mThingsToLoad.size() == 0) {
                                try {
                                    mThingsToLoad.wait();
                                } catch (InterruptedException ex) {
                                    // nothing to do
                                }
                            }
                            if (mThingsToLoad.size() > 0) {
                                r = mThingsToLoad.remove(0);
                            }
                        }
                        if (r != null) {
                            r.run();
                        }
                    }
                }
            });
            mWorkerThread.start();
        }

        public void push(Runnable r) {
            synchronized (mThingsToLoad) {
                mThingsToLoad.add(r);
                mThingsToLoad.notify();
            }
        }

        public void destroy() {
            synchronized (mThingsToLoad) {
                mThreadOver = true;
                mThingsToLoad.clear();
                mThingsToLoad.notify();
            }
        }
    }

    private final TaskStack mTaskQueue = new TaskStack();
    public void pushTask(Runnable r) {
        mTaskQueue.push(r);
    }

    public void destroyTaskStack() {
        if (mTaskQueue != null) {
            mTaskQueue.destroy();
        }
    }
}
