package com.android.mms.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.ThreadsColumns;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DraftCache;
import com.google.android.mms.util.PduCache;

//a0
import java.util.List;
import android.provider.Telephony.WapPush;
import android.provider.Telephony;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;
//a1

/**
 * An interface for finding information about conversations and/or creating new ones.
 */
public class Conversation {
    private static final String TAG = "Mms/conv";
    private static final boolean DEBUG = false;
    private static boolean mIsInitialized = false;
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();

    //m0
    /*
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };*/
    
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT, Threads.TYPE ,Threads.READCOUNT ,Threads.STATUS
    };
    //m1

    private static final String[] UNREAD_PROJECTION = {
        Threads._ID,
        Threads.READ
    };

    private static final String UNREAD_SELECTION = "(read=0 OR seen=0)";
    private static final String[] SEEN_PROJECTION = new String[] {"seen"};
    private static final int UPDATE_LIMIT = 100;
    private static final String UNREAD_SELECTION_WITH_LIMIT = "(read=0 OR seen=0) " +
            "and _id in (select _id from sms where (read=0 OR seen=0) and thread_id=? order by _id limit 0," + UPDATE_LIMIT + ")";

    private static final int ID             = 0;
    private static final int DATE           = 1;
    private static final int MESSAGE_COUNT  = 2;
    private static final int RECIPIENT_IDS  = 3;
    private static final int SNIPPET        = 4;
    private static final int SNIPPET_CS     = 5;
    private static final int READ           = 6;
    private static final int ERROR          = 7;
    private static final int HAS_ATTACHMENT = 8;

    // add for WappushNotification canceled with other message
    public static final int MARK_ALL_MESSAGE_AS_SEEN                   = 0;
    public static final int MARK_ALL_MESSAGE_AS_SEEN_WITHOUT_WAPPUSH   = 1;

    private final Context mContext;

    // The thread ID of this conversation.  Can be zero in the case of a
    // new conversation where the recipient set is changing as the user
    // types and we have not hit the database yet to create a thread.
    private long mThreadId;

    private ContactList mRecipients;    // The current set of recipients.
    private long mDate;                 // The last update time.
    private int mMessageCount;          // Number of messages.
    private String mSnippet;            // Text of the most recent message.
    private boolean mHasUnreadMessages; // True if there are unread messages.
    private boolean mHasAttachment;     // True if any message has an attachment.
    private boolean mHasError;          // True if any message is in an error state.
    private boolean mIsChecked;         // True if user has selected the conversation for a
                                        // multi-operation such as delete.
    private boolean mIsDoingMarkAsRead = false;

    private static ContentValues mReadContentValues;
    private static boolean mLoadingThreads;
    private boolean mMarkAsReadBlocked;
    private Object mMarkAsBlockedSyncer = new Object();

    private Conversation(Context context) {
        mContext = context;
        mRecipients = new ContactList();
        mThreadId = 0;
    }

    private Conversation(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor threadId: " + threadId);
        }
        mContext = context;
        Xlog.d(TAG,"new Conversation.loadFromThreadId(threadId, allowQuery): threadId = " 
                + threadId + "allowQuery = " + allowQuery);
        if (!loadFromThreadId(threadId, allowQuery)) {
            mRecipients = new ContactList();
            mThreadId = 0;
        }
    }

    private Conversation(Context context, Cursor cursor, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor cursor, allowQuery: " + allowQuery);
        }
        mContext = context;
        fillFromCursor(context, this, cursor, allowQuery);
    }

    /**
     * Create a new conversation with no recipients.  {@link #setRecipients} can
     * be called as many times as you like; the conversation will not be
     * created in the database until {@link #ensureThreadId} is called.
     */
    public static Conversation createNew(Context context) {
        return new Conversation(context);
    }

    /**
     * Find the conversation matching the provided thread ID.
     */
    public static Conversation get(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by threadId: " + threadId);
        }
        Conversation conv = Cache.get(threadId);
        if (conv != null)
            return conv;

        conv = new Conversation(context, threadId, allowQuery);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from threadId): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by threadId cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    /**
     * Find the conversation matching the provided recipient set.
     * When called with an empty recipient list, equivalent to {@link #createNew}.
     */
    public static Conversation get(Context context, ContactList recipients, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by recipients: " + recipients.serialize());
        }
        // If there are no recipients in the list, make a new conversation.
        if (recipients.size() < 1) {
            return createNew(context);
        }

        Conversation conv = Cache.get(recipients);
        //m0
        /*
        if (conv != null) {
            return conv
        }*/
        
        if (conv != null) {
            //if messgeCount = 0 and no draft, update this conversation. For the case,
            //send SMS from calllog, don't back to ConversationList, then send again,
            //the mssageCount = 0 and have action in the intent, it will init RecipientEditor
            //in ComposeMessageActivity. The CR is 96268
            if (conv.getMessageCount() == 0 && !conv.hasDraft()) {
                long threadId = conv.getThreadId();
                Cache.remove(threadId);
                conv = new Conversation(context, threadId, allowQuery);
                try {
                    Cache.put(conv);
                } catch (IllegalStateException e) {
                    Xlog.e(TAG, "Tried to add duplicate Conversation to Cache");
                }
            }
            return conv;
        }
        //m1

        long threadId = getOrCreateThreadId(context, recipients);
        conv = new Conversation(context, threadId, allowQuery);
        Log.d(TAG, "Conversation.get: created new conversation " + /*conv.toString()*/ "xxxxxxx");

        if (!conv.getRecipients().equals(recipients)) {
            LogTag.error(TAG, "Conversation.get: new conv's recipients don't match input recpients "
                    + /*recipients*/ "xxxxxxx");
        }

        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from recipients): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("get by recipients cache.replace failed on " + conv);
            }
        }

        return conv;
    }

    /**
     * Find the conversation matching in the specified Uri.  Example
     * forms: {@value content://mms-sms/conversations/3} or
     * {@value sms:+12124797990}.
     * When called with a null Uri, equivalent to {@link #createNew}.
     */
    public static Conversation get(Context context, Uri uri, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation get by uri: " + uri);
        }
        if (uri == null) {
            return createNew(context);
        }

        if (DEBUG) Log.v(TAG, "Conversation get URI: " + uri);

        // Handle a conversation URI
        if (uri.getPathSegments().size() >= 2) {
            try {
                //m0
                /*
                long threadId = Long.parseLong(uri.getPathSegments().get(1));
                */
                
                String threadIdStr = uri.getPathSegments().get(1);
                threadIdStr = threadIdStr.replaceAll("-","");
                //long threadId = Long.parseLong(uri.getPathSegments().get(1));
                long threadId = Long.parseLong(threadIdStr);
                //m1
                
                if (DEBUG) {
                    Log.v(TAG, "Conversation get threadId: " + threadId);
                }
                return get(context, threadId, allowQuery);
            } catch (NumberFormatException exception) {
                LogTag.error("Invalid URI: " + uri);
            }
        }

        String recipient = getRecipients(uri);
        
//m0
//        returen get(context, ContactList.getByNumbers(recipient,
//                allowQuery /* don't block */, true /* replace number */), allowQuery);
        return get(context, ContactList.getByNumbers(context, recipient, false /* don't block */, true /* replace number */), allowQuery);
//m1
    }

    /**
     * Returns true if the recipient in the uri matches the recipient list in this
     * conversation.
     */
    public boolean sameRecipient(Uri uri, Context context) {
        int size = mRecipients.size();
        if (size > 1) {
            return false;
        }
        if (uri == null) {
            return size == 0;
        }
        ContactList incomingRecipient = null;
        if (uri.getPathSegments().size() >= 2) {
            // it's a thread id for a conversation
            Conversation otherConv = get(context, uri, false);
            if (otherConv == null) {
                return false;
            }
            incomingRecipient = otherConv.mRecipients;
        } else {
            String recipient = getRecipients(uri);
            incomingRecipient = ContactList.getByNumbers(recipient,
                    false /* don't block */, false /* don't replace number */);
        }
        if (DEBUG) Log.v(TAG, "sameRecipient incomingRecipient: " + incomingRecipient +
                " mRecipients: " + mRecipients);
        return mRecipients.equals(incomingRecipient);
    }

    /**
     * Returns a temporary Conversation (not representing one on disk) wrapping
     * the contents of the provided cursor.  The cursor should be the one
     * returned to your AsyncQueryHandler passed in to {@link #startQueryForAll}.
     * The recipient list of this conversation can be empty if the results
     * were not in cache.
     */
    public static Conversation from(Context context, Cursor cursor) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will get updated when fillFromCursor() is
        // called with this cursor.
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null) {
                fillFromCursor(context, conv, cursor, false);   // update the existing conv in-place
                return conv;
            }
        }
        Conversation conv = new Conversation(context, cursor, false);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    public static Conversation getFromCursor(Context context, Cursor cursor) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will get updated when fillFromCursor() is
        // called with this cursor.
    	 Log.v(TAG, "getFromCursor "); 
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null) {
                return conv;
            }
        }
        Conversation conv = new Conversation(context, cursor, false);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }
    
    private void buildReadContentValues() {
        if (mReadContentValues == null) {
            mReadContentValues = new ContentValues(2);
            mReadContentValues.put("read", 1);
            mReadContentValues.put("seen", 1);
        }
    }

    /**
     * Marks all messages in this conversation as read and updates
     * relevant notifications.  This method returns immediately;
     * work is dispatched to a background thread.
     */
    public void markAsRead() {
        if (mIsDoingMarkAsRead) {
            Log.d(TAG, "markAsRead(): mIsDoingMarkAsRead is true");
            return;
        }
        // If we have no Uri to mark (as in the case of a conversation that
        // has not yet made its way to disk), there's nothing to do.
        final Uri threadUri = getUri();

        new Thread(new Runnable() {
            public void run() {
                synchronized(mMarkAsBlockedSyncer) {
                    if (mMarkAsReadBlocked) {
                        try {
                            mMarkAsBlockedSyncer.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (threadUri != null) {
                        buildReadContentValues();

                        // Check the read flag first. It's much faster to do a query than
                        // to do an update. Timing this function show it's about 10x faster to
                        // do the query compared to the update, even when there's nothing to
                        // update.
                        int needUpdateCount = 0;
                        Log.d(TAG, "markAsRead(): threadUri = " + threadUri);
                        Cursor c = mContext.getContentResolver().query(threadUri,
                                UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                        if (c != null) {
                            try {
                                needUpdateCount = c.getCount();
                                Log.d(TAG, "markAsRead(): needUpdateCount= " + needUpdateCount);
                            } finally {
                                c.close();
                            }
                        }

                        if (needUpdateCount > 0) {
                            mIsDoingMarkAsRead = true;
                            LogTag.debug("markAsRead: update read/seen for thread uri: " +
                                    threadUri);
                            final int allNeedUpdateCount = needUpdateCount;
                            final Conversation conv = Conversation.this;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int updateCount = UPDATE_LIMIT;
                                    if (allNeedUpdateCount >= UPDATE_LIMIT) {
                                        while (updateCount > 0) {
                                            updateCount = mContext.getContentResolver().update(
                                                    threadUri, mReadContentValues,
                                                    UNREAD_SELECTION_WITH_LIMIT, 
                                                    new String[] {mThreadId + ""});
                                            Xlog.d(TAG, "markAsRead-updateThread: updateCount=" + updateCount);
                                            synchronized(this) {
                                                try {
                                                    this.wait(UPDATE_LIMIT * 2);
                                                } catch (InterruptedException ex) {
                                                    // do nothing
                                                }
                                            }
                                        }
                                    } else {
                                        updateCount = mContext.getContentResolver().update(threadUri, mReadContentValues,
                                                UNREAD_SELECTION, null);
                                        Xlog.d(TAG, "markAsRead-updateThread: updateCount=" + updateCount);
                                    }
                                    conv.setIsDoingMarkAsRead(false);
                                    // Always update notifications regardless of the read state.
                                    MessagingNotification.blockingUpdateAllNotifications(mContext);
                                }
                            }, "markAsRead-updateThread").start();
                        }
                        setHasUnreadMessages(false);
                    }
                }
            }
        }, "markAsRead-mainThread").start();
    }

    public void blockMarkAsRead(boolean block) {
        if (mIsDoingMarkAsRead) {
            Log.d(TAG, "markAsRead(): mIsDoingMarkAsRead is true");
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("blockMarkAsRead: " + block);
        }

        synchronized(mMarkAsBlockedSyncer) {
            if (block != mMarkAsReadBlocked) {
                mMarkAsReadBlocked = block;
                if (!mMarkAsReadBlocked) {
                    mMarkAsBlockedSyncer.notifyAll();
                }
            }

        }
    }

    /**
     * Returns a content:// URI referring to this conversation,
     * or null if it does not exist on disk yet.
     */
    public synchronized Uri getUri() {
        if (mThreadId <= 0)
            return null;

        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }

    /**
     * Return the Uri for all messages in the given thread ID.
     * @deprecated
     */
    public static Uri getUri(long threadId) {
        // TODO: Callers using this should really just have a Conversation
        // and call getUri() on it, but this guarantees no blocking.
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    /**
     * Returns the thread ID of this conversation.  Can be zero if
     * {@link #ensureThreadId} has not been called yet.
     */
    public synchronized long getThreadId() {
        return mThreadId;
    }

    /**
     * Guarantees that the conversation has been created in the database.
     * This will make a blocking database call if it hasn't.
     *
     * @return The thread ID of this conversation in the database
     */
    public synchronized long ensureThreadId() {
        //m0
        /*
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients);
        }
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
        */
        return ensureThreadId(false);
        //m1
    }

    public synchronized void clearThreadId() {
        // remove ourself from the cache
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("clearThreadId old threadId was: " + mThreadId + " now zero");
        }
        Cache.remove(mThreadId);
        //a0
        DraftCache.getInstance().updateDraftStateInCache(mThreadId);
        //a1
        mThreadId = 0;
    }

    /**
     * Sets the list of recipients associated with this conversation.
     * If called, {@link #ensureThreadId} must be called before the next
     * operation that depends on this conversation existing in the
     * database (e.g. storing a draft message to it).
     */
    public synchronized void setRecipients(ContactList list) {
        //a0
        // remove the same contacts
        Set<Contact> contactSet = new LinkedHashSet<Contact>();
        contactSet.addAll(list);
        list.clear();
        list.addAll(contactSet);
        //a1

        mRecipients = list;

        // Invalidate thread ID because the recipient set has changed.
        mThreadId = 0;

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "setRecipients after: " + this.toString());
        }
}

    /**
     * Returns the recipient set of this conversation.
     */
    public synchronized ContactList getRecipients() {
        return mRecipients;
    }

    /**
     * Returns true if a draft message exists in this conversation.
     */
    public synchronized boolean hasDraft() {
        if (mThreadId <= 0)
            return false;

        return DraftCache.getInstance().hasDraft(mThreadId);
    }

    /**
     * Sets whether or not this conversation has a draft message.
     */
    public synchronized void setDraftState(boolean hasDraft) {
        if (mThreadId <= 0)
            return;

        DraftCache.getInstance().setDraftState(mThreadId, hasDraft);
    }

    /**
     * Returns the time of the last update to this conversation in milliseconds,
     * on the {@link System#currentTimeMillis} timebase.
     */
    public synchronized long getDate() {
        return mDate;
    }

    /**
     * Returns the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized int getMessageCount() {
        return mMessageCount;
    }
    /**
     * Set the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized void setMessageCount(int cnt) {
        mMessageCount = cnt;
    }

    /**
     * Returns a snippet of text from the most recent message in the conversation.
     */
    public synchronized String getSnippet() {
        return mSnippet;
    }

    /**
     * Returns true if there are any unread messages in the conversation.
     */
    public boolean hasUnreadMessages() {
        synchronized (this) {
            return mHasUnreadMessages;
        }
    }

    private void setHasUnreadMessages(boolean flag) {
        synchronized (this) {
            mHasUnreadMessages = flag;
        }
    }

    /**
     * Returns true if any messages in the conversation have attachments.
     */
    public synchronized boolean hasAttachment() {
        return mHasAttachment;
    }

    /**
     * Returns true if any messages in the conversation are in an error state.
     */
    public synchronized boolean hasError() {
        return mHasError;
    }

    /**
     * Returns true if this conversation is selected for a multi-operation.
     */
    public synchronized boolean isChecked() {
        return mIsChecked;
    }

    public synchronized void setIsChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    private static long getOrCreateThreadId(Context context, ContactList list) {
        //m0
        /*
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false);
            if (cacheContact != null) {
                recipients.add(cacheContact.getNumber());
            } else {
                recipients.add(c.getNumber());
            }
        }
        long retVal = Threads.getOrCreateThreadId(context, recipients);
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
        */
        
        return getOrCreateThreadId(context, list, false);
        //m1
    }

    /*
     * The primary key of a conversation is its recipient set; override
     * equals() and hashCode() to just pass through to the internal
     * recipient sets.
     */
    @Override
    public synchronized boolean equals(Object obj) {
        try {
            Conversation other = (Conversation)obj;
            return (mRecipients.equals(other.mRecipients));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public synchronized int hashCode() {   
        if(mType == Threads.WAPPUSH_THREAD){
            Log.d(TAG,"mRecipients.hashCode()*100 ="+mRecipients.hashCode()*100);
            return mRecipients.hashCode()*100;
        } else {
            return mRecipients.hashCode();
        }
    }

    @Override
    public synchronized String toString() {
        return String.format("[%s] (tid %d)", mRecipients.serialize(), mThreadId);
    }

    /**
     * Remove any obsolete conversations sitting around on disk. Obsolete threads are threads
     * that aren't referenced by any message in the pdu or sms tables.
     */
    public static void asyncDeleteObsoleteThreads(AsyncQueryHandler handler, int token) {
        handler.startDelete(token, null, Threads.OBSOLETE_THREADS_URI, null, null);
    }

    /**
     * Start a query for all conversations in the database on the specified
     * AsyncQueryHandler.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryForAll(AsyncQueryHandler handler, int token) {
       // handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        startQuery(handler, token, null);
    }

    /**
     * Start a query for in the database on the specified AsyncQueryHandler with the specified
     * "where" clause.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     * @param selection   A where clause (can be null) to select particular conv items.
     */
    public static void startQuery(AsyncQueryHandler handler, int token, String selection) {
        handler.cancelOperation(token);
        //a0
        final int queryToken = token;
        final AsyncQueryHandler queryHandler = handler;
        final String Selection = selection;
        //a1

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        //m0
        //handler.startQuery(token, null, sAllThreadsUri,
        //        ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
        
        queryHandler.postDelayed(new Runnable() {
            public void run() {
                queryHandler.startQuery(queryToken, null, sAllThreadsUri,
                    ALL_THREADS_PROJECTION, Selection, null, Conversations.DEFAULT_SORT_ORDER);
            }
        }, 10);
        //m1
    }

    /**
     * Start a delete of the conversation with the specified thread ID.
     *
     * @param handler An AsyncQueryHandler that will receive onDeleteComplete
     *                upon completion of the conversation being deleted
     * @param token   The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     * @param threadId Thread ID of the conversation to be deleted
     */
    public static void startDelete(AsyncQueryHandler handler, int token, boolean deleteAll,
            long threadId, int maxMmsId, int maxSmsId) {
        //wappush: do not need modify the code here, but delete function in provider has been modified.
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        String selection = deleteAll ? null : "locked=0";
        PduCache.getInstance().purge(uri);
        if (maxMmsId != 0 || maxSmsId != 0){
            uri = uri.buildUpon().appendQueryParameter("mmsId", String.valueOf(maxMmsId)).build();
            uri = uri.buildUpon().appendQueryParameter("smsId", String.valueOf(maxSmsId)).build();
        }
        handler.startDelete(token, null, uri, selection, null);
    }

    /**
     * Start deleting all conversations in the database.
     * @param handler An AsyncQueryHandler that will receive onDeleteComplete
     *                upon completion of all conversations being deleted
     * @param token   The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     */
    public static void startDeleteAll(AsyncQueryHandler handler, int token, boolean deleteAll, int maxMmsId, int maxSmsId) {
        //wappush: do not need modify the code here, but delete function in provider has been modified.
        String selection = deleteAll ? null : "locked=0";
        PduCache.getInstance().purge(Threads.CONTENT_URI);
        Uri uri = Threads.CONTENT_URI;
        Log.d(TAG, "startDeleteAll maxMmsId=" + maxMmsId + " maxSmsId="+maxSmsId);
        if (maxMmsId != 0 || maxSmsId != 0){
            uri = uri.buildUpon().appendQueryParameter("mmsId", String.valueOf(maxMmsId)).build();
            uri = uri.buildUpon().appendQueryParameter("smsId", String.valueOf(maxSmsId)).build();
        }
        handler.startDelete(token, null, uri, selection, null);
    }

    /**
     * Check for locked messages in all threads or a specified thread.
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of looking for locked messages
     * @param threadIds   A list of threads to search. null means all threads
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
            Collection<Long> threadIds,
            int token) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;

        String selection = null;
        if (threadIds != null) {
            StringBuilder buf = new StringBuilder();
            int i = 0;
            buf.append("thread_id in ( ");
            for (long threadId : threadIds) {
                if (i++ > 0) {
                    buf.append(",");
                }
                // We have to build the selection arg into the selection because deep down in
                // provider, the function buildUnionSubQuery takes selectionArgs, but ignores it.
                buf.append(Long.toString(threadId));
            }
            buf.append(" )");
            selection = buf.toString();
        }
        handler.startQuery(token, threadIds, uri, UNREAD_PROJECTION, selection, null, null);
    }

    /**
     * Check for locked messages in all threads or a specified thread.
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of looking for locked messages
     * @param threadId   The threadId of the thread to search. -1 means all threads
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
            long threadId,
            int token) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        startQueryHaveLockedMessages(handler, threadIds, token);
    }

    /**
     * Fill the specified conversation with the values from the specified
     * cursor, possibly setting recipients to empty if {@value allowQuery}
     * is false and the recipient IDs are not in cache.  The cursor should
     * be one made via {@link #startQueryForAll}.
     */
    private static void fillFromCursor(Context context, Conversation conv,
                                       Cursor c, boolean allowQuery) {
        synchronized (conv) {
            conv.mThreadId = c.getLong(ID);
            conv.mDate = c.getLong(DATE);
            conv.mMessageCount = c.getInt(MESSAGE_COUNT);

            // Replace the snippet with a default value if it's empty.
            String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            conv.mSnippet = snippet;

            conv.setHasUnreadMessages(c.getInt(READ) == 0);
            conv.mHasError = (c.getInt(ERROR) != 0);
            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
            
            //a0
            //wappush: get the value for mType
            conv.mType = c.getInt(TYPE);
            conv.mMessageStatus = c.getInt(STATUS);
            //a1
        }
        // Fill in as much of the conversation as we can before doing the slow stuff of looking
        // up the contacts associated with this conversation.
        String recipientIds = c.getString(RECIPIENT_IDS);
        ContactList recipients = ContactList.getByIds(recipientIds, allowQuery);
        synchronized (conv) {
            conv.mRecipients = recipients;
        }

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }

    /**
     * Private cache for the use of the various forms of Conversation.get.
     */
    private static class Cache {
        private static Cache sInstance = new Cache();
        static Cache getInstance() { return sInstance; }
        private final HashSet<Conversation> mCache;
        private Cache() {
            mCache = new HashSet<Conversation>(10);
        }

        /**
         * Return the conversation with the specified thread ID, or
         * null if it's not in cache.
         */
        static Conversation get(long threadId) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with threadId: " + threadId);
                }
                for (Conversation c : sInstance.mCache) {
                    if (DEBUG) {
                        LogTag.debug("Conversation get() threadId: " + threadId +
                                " c.getThreadId(): " + c.getThreadId());
                    }
                    if (c.getThreadId() == threadId) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Return the conversation with the specified recipient
         * list, or null if it's not in cache.
         */
        static Conversation get(ContactList list) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with ContactList: " + list);
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Put the specified conversation in the cache.  The caller
         * should not place an already-existing conversation in the
         * cache, but rather update it in place.
         */
        static void put(Conversation c) {
            synchronized (sInstance) {
                // We update cache entries in place so people with long-
                // held references get updated.
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    Log.d(TAG, "Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    throw new IllegalStateException("cache already contains " + c +
                            " threadId: " + c.mThreadId);
                }
                sInstance.mCache.add(c);
            }
        }

        /**
         * Replace the specified conversation in the cache. This is used in cases where we
         * lookup a conversation in the cache by threadId, but don't find it. The caller
         * then builds a new conversation (from the cursor) and tries to add it, but gets
         * an exception that the conversation is already in the cache, because the hash
         * is based on the recipients and it's there under a stale threadId. In this function
         * we remove the stale entry and add the new one. Returns true if the operation is
         * successful
         */
        static boolean replace(Conversation c) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (!sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    return false;
                }
                // Here it looks like we're simply removing and then re-adding the same object
                // to the hashset. Because the hashkey is the conversation's recipients, and not
                // the thread id, we'll actually remove the object with the stale threadId and
                // then add the the conversation with updated threadId, both having the same
                // recipients.
                sInstance.mCache.remove(c);
                sInstance.mCache.add(c);
                return true;
            }
        }

        static void remove(long threadId) {
            synchronized (sInstance) {
                if (DEBUG) {
                    LogTag.debug("remove threadid: " + threadId);
                    dumpCache();
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getThreadId() == threadId) {
                        sInstance.mCache.remove(c);
                        return;
                    }
                }
            }
        }

        static void dumpCache() {
            synchronized (sInstance) {
                LogTag.debug("Conversation dumpCache: ");
                for (Conversation c : sInstance.mCache) {
                    LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
                }
            }
        }

        /**
         * Remove all conversations from the cache that are not in
         * the provided set of thread IDs.
         */
        static void keepOnly(Set<Long> threads) {
            synchronized (sInstance) {
                Iterator<Conversation> iter = sInstance.mCache.iterator();
                //m0
                /*
                while (iter.hasNext()) {
                    Conversation c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
                */
                
                Conversation c = null;
                while (iter.hasNext()) {
                    c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
                //m1
            }
            if (DEBUG) {
                LogTag.debug("after keepOnly");
                dumpCache();
            }
        }
    }

    /**
     * Set up the conversation cache.  To be called once at application
     * startup time.
     */
    public static void init(final Context context) {
        mIsInitialized = true;
        new Thread(new Runnable() {
            public void run() {
                cacheAllThreads(context);
            }
        }).start();
    }

    
    public static boolean isInitialized() {
        return mIsInitialized;
    }

    public static void markAllConversationsAsSeen(final Context context, final int wappushFlag) {
        if (DEBUG) {
            LogTag.debug("Conversation.markAllConversationsAsSeen");
        }

        new Thread(new Runnable() {
            public void run() {
                blockingMarkAllSmsMessagesAsSeen(context);
                blockingMarkAllMmsMessagesAsSeen(context);

                //a0
                blockingMarkAllCellBroadcastMessagesAsSeen(context);
                //a1

                // Always update notifications regardless of the read state.
                MessagingNotification.blockingUpdateAllNotifications(context);

                // add for WappushNotification canceled with other message
                if (wappushFlag != MARK_ALL_MESSAGE_AS_SEEN_WITHOUT_WAPPUSH) {
                    if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                        blockingMarkAllWapPushMessagesAsSeen(context);
                        WapPushMessagingNotification.blockingUpdateNewMessageIndicator(context, false);
                    }
                }
            }
        }).start();
    }

    private static void blockingMarkAllSmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Sms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " SMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Sms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);
    }

    private static void blockingMarkAllMmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Mms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " MMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Mms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);

    }
    
    /**
     * Are we in the process of loading and caching all the threads?.
     */
    public static boolean loadingThreads() {
        synchronized (Cache.getInstance()) {
            return mLoadingThreads;
        }
    }

    private static void cacheAllThreads(Context context) {
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: begin");
        }
        synchronized (Cache.getInstance()) {
            if (mLoadingThreads) {
                return;
                }
            mLoadingThreads = true;
        }

        // Keep track of what threads are now on disk so we
        // can discard anything removed from the cache.
        HashSet<Long> threadsOnDisk = new HashSet<Long>();

        // Query for all conversations.
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, null);
        try {
            if (c != null) {
                //m0
                /*
                while (c.moveToNext()) {
                    long threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);
                }
                */
                
                long threadId = 0L;
                while (c.moveToNext()) {
                    threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);
                //m1

                    // Try to find this thread ID in the cache.
                    Conversation conv;
                    synchronized (Cache.getInstance()) {
                        conv = Cache.get(threadId);
                    }

                    if (conv == null) {
                        // Make a new Conversation and put it in
                        // the cache if necessary.
                        conv = new Conversation(context, c, true);

                        try {
                            Thread.sleep(10);                
                        } catch (Exception e) {
                            
                        }
                        try {
                            synchronized (Cache.getInstance()) {
                                Cache.put(conv);
                            }
                        } catch (IllegalStateException e) {
                            Xlog.e(TAG, "Tried to add duplicate Conversation to Cache" +
                                    " for threadId: " + threadId + " new conv: " + conv);
                            if (!Cache.replace(conv)) {
                                Xlog.e(TAG, "cacheAllThreads cache.replace failed on " + conv);
                            }
                        }
                    } else {
                        // Or update in place so people with references
                        // to conversations get updated too.
                        fillFromCursor(context, conv, c, true);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            synchronized (Cache.getInstance()) {
                mLoadingThreads = false;
            }
        }

        // Purge the cache of threads that no longer exist on disk.
        Cache.keepOnly(threadsOnDisk);

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: finished");
            Cache.dumpCache();
        }
    }

    private boolean loadFromThreadId(long threadId, boolean allowQuery) {
        Cursor c = mContext.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        try {
            if (c.moveToFirst()) {
                fillFromCursor(mContext, this, c, allowQuery);

                if (threadId != mThreadId) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id!" +
                            " threadId=" + threadId + ", mThreadId=" + mThreadId);
                }
            } else {
                LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
                return false;
            }
        } finally {
            c.close();
        }
        return true;
    }

    public static String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        int pos = base.indexOf('?');
        return (pos == -1) ? base : base.substring(0, pos);
    }

    public static void dump() {
        Cache.dumpCache();
    }

    public static void dumpThreadsTable(Context context) {
        LogTag.debug("**** Dump of threads table ****");
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, "date ASC");
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
                Log.d(TAG, "dumpThreadsTable threadId: " + c.getLong(ID) +
                        " " + ThreadsColumns.DATE + " : " + c.getLong(DATE) +
                        " " + ThreadsColumns.MESSAGE_COUNT + " : " + c.getInt(MESSAGE_COUNT) +
                        " " + ThreadsColumns.SNIPPET + " : " + snippet +
                        " " + ThreadsColumns.READ + " : " + c.getInt(READ) +
                        " " + ThreadsColumns.ERROR + " : " + c.getInt(ERROR) +
                        " " + ThreadsColumns.HAS_ATTACHMENT + " : " + c.getInt(HAS_ATTACHMENT) +
                        " " + ThreadsColumns.RECIPIENT_IDS + " : " + c.getString(RECIPIENT_IDS));

                ContactList recipients = ContactList.getByIds(c.getString(RECIPIENT_IDS), false);
                Log.d(TAG, "----recipients: " + recipients.serialize());
            }
        } finally {
            c.close();
        }
    }

    static final String[] SMS_PROJECTION = new String[] {
        BaseColumns._ID,
        // For SMS
        Sms.THREAD_ID,
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_ID                  = 0;
    static final int COLUMN_THREAD_ID           = 1;
    static final int COLUMN_SMS_ADDRESS         = 2;
    static final int COLUMN_SMS_BODY            = 3;
    static final int COLUMN_SMS_DATE            = 4;
    static final int COLUMN_SMS_READ            = 5;
    static final int COLUMN_SMS_TYPE            = 6;
    static final int COLUMN_SMS_STATUS          = 7;
    static final int COLUMN_SMS_LOCKED          = 8;
    static final int COLUMN_SMS_ERROR_CODE      = 9;

    public static void dumpSmsTable(Context context) {
        LogTag.debug("**** Dump of sms table ****");
        Cursor c = context.getContentResolver().query(Sms.CONTENT_URI,
                SMS_PROJECTION, null, null, "_id DESC");
        try {
            // Only dump the latest 20 messages
            c.moveToPosition(-1);
            while (c.moveToNext() && c.getPosition() < 20) {
                String body = c.getString(COLUMN_SMS_BODY);
                LogTag.debug("dumpSmsTable " + BaseColumns._ID + ": " + c.getLong(COLUMN_ID) +
                        " " + Sms.THREAD_ID + " : " + c.getLong(DATE) +
                        " " + Sms.ADDRESS + " : " + c.getString(COLUMN_SMS_ADDRESS) +
                        " " + Sms.BODY + " : " + body.substring(0, Math.min(body.length(), 8)) +
                        " " + Sms.DATE + " : " + c.getLong(COLUMN_SMS_DATE) +
                        " " + Sms.TYPE + " : " + c.getInt(COLUMN_SMS_TYPE));
            }
        } finally {
            c.close();
        }
    }

    /**
     * verifySingleRecipient takes a threadId and a string recipient [phone number or email
     * address]. It uses that threadId to lookup the row in the threads table and grab the
     * recipient ids column. The recipient ids column contains a space-separated list of
     * recipient ids. These ids are keys in the canonical_addresses table. The recipient is
     * compared against what's stored in the mmssms.db, but only if the recipient id list has
     * a single address.
     * @param context is used for getting a ContentResolver
     * @param threadId of the thread we're sending to
     * @param recipientStr is a phone number or email address
     * @return the verified number or email of the recipient
     */
    public static String verifySingleRecipient(final Context context,
            final long threadId, final String recipientStr) {
        if (threadId <= 0) {
            LogTag.error("verifySingleRecipient threadId is ZERO, recipient: " + recipientStr);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        Cursor c = context.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        if (c == null) {
            LogTag.error("verifySingleRecipient threadId: " + threadId +
                    " resulted in NULL cursor , recipient: " + recipientStr);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        String address = recipientStr;
        String recipientIds;
        try {
            if (!c.moveToFirst()) {
                LogTag.error("verifySingleRecipient threadId: " + threadId +
                        " can't moveToFirst , recipient: " + recipientStr);
                LogTag.dumpInternalTables(context);
                return recipientStr;
            }
            recipientIds = c.getString(RECIPIENT_IDS);
        } finally {
            c.close();
        }
        String[] ids = recipientIds.split(" ");

        if (ids.length != 1) {
            // We're only verifying the situation where we have a single recipient input against
            // a thread with a single recipient. If the thread has multiple recipients, just
            // assume the input number is correct and return it.
            return recipientStr;
        }

        // Get the actual number from the canonical_addresses table for this recipientId
        address = RecipientIdCache.getSingleAddressFromCanonicalAddressInDb(context, ids[0]);

        if (TextUtils.isEmpty(address)) {
            LogTag.error("verifySingleRecipient threadId: " + threadId +
                    " getSingleNumberFromCanonicalAddresses returned empty number for: " +
                    ids[0] + " recipientIds: " + recipientIds);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        if (PhoneNumberUtils.compareLoosely(recipientStr, address)) {
            // Bingo, we've got a match. We're returning the input number because of area
            // codes. We could have a number in the canonical_address name of "232-1012" and
            // assume the user's phone's area code is 650. If the user sends a message to
            // "(415) 232-1012", it will loosely match "232-1202". If we returned the value
            // from the table (232-1012), the message would go to the wrong person (to the
            // person in the 650 area code rather than in the 415 area code).
            return recipientStr;
        }

        if (context instanceof Activity) {
            LogTag.warnPossibleRecipientMismatch("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address, (Activity)context);
        }
        LogTag.dumpInternalTables(context);
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address);
        }
        return address;
    }
    
    //a0
    private static final String[] ALL_CHANNEL_PROJECTION = { "_id",
        Telephony.CbSms.CbChannel.NAME,
        Telephony.CbSms.CbChannel.NUMBER };
    private static final String[] ALL_ADDRESS_PROJECTION = { "_id",
        Telephony.CbSms.CanonicalAddressesColumns.ADDRESS };
    
    private static final String READ_SELECTION = "(read=1)";
    private static final Uri ADDRESS_ID_URI = Uri.parse("content://cb/addresses/#");
    
    //wappush: add TYPE, which already exists
    private static final int TYPE           = 9;
    private static final int READCOUNT    = 10;
    private static final int STATUS       = 11;
    
    private int mUnreadMessageCount;    // Number of unread message.
    private int mMessageStatus;         // Message Status.
    
    //add for cb
    private int mAddressId;          // Number of messages.
    private int mChannelId = -1;            // Text of the most recent message.
    private String mChannelName = null;
    
    //wappush: add mType variable
    private int mType;
    //a1
    
    //a0
    public static Conversation upDateThread(Context context, long threadId, boolean allowQuery) {
        Cache.remove(threadId);
        return get(context, threadId, allowQuery);
    }
    
    public static List<String> getNumbers(String recipient) {
        int len = recipient.length();
        List<String> list = new ArrayList<String>();

        int start = 0;
        int i = 0;
        char c;
        while (i < len + 1) {
            if ((i == len) || ((c = recipient.charAt(i)) == ',') || (c == ';')) {
                if (i > start) {
                    list.add(recipient.substring(start, i));

                    // calculate the recipients total length. This is so if the name contains
                    // commas or semis, we'll skip over the whole name to the next
                    // recipient, rather than parsing this single name into multiple
                    // recipients.
                    int spanLen = recipient.substring(start, i).length();
                    if (spanLen > i) {
                        i = spanLen;
                    }
                }

                i++;

                while ((i < len) && (recipient.charAt(i) == ' ')) {
                    i++;
                }

                start = i;
            } else {
                i++;
            }
        }

        return list;
    }
    
    //add for cb
    public synchronized int getChannelId() {
        if(mChannelId == -1) {
            setChannelIdFromDatabase();
        }
        return mChannelId;
    }
    
    private void setChannelIdFromDatabase() {
        Uri uri = ContentUris.withAppendedId(ADDRESS_ID_URI, mAddressId);
        Cursor c = mContext.getContentResolver().query(uri,
                ALL_ADDRESS_PROJECTION, null, null, null);
        try {
            if (c == null || c.getCount() == 0) {
                mChannelId = -1;
            } else {
                c.moveToFirst();
                // Name
                mChannelId = c.getInt(1);
            }
        } finally {
            if(c != null) {
                c.close();
            }
        }
    }

    //wappush: because of different uri
    public void WPMarkAsRead() {
        final Uri threadUri = ContentUris.withAppendedId(
                WapPush.CONTENT_URI_THREAD, mThreadId);
        new Thread(new Runnable() {
            public void run() {
                synchronized (mMarkAsBlockedSyncer) {
                    if (mMarkAsReadBlocked) {
                        try {
                            mMarkAsBlockedSyncer.wait();
                        } catch (InterruptedException e) {
                        }
                    }

                    if (threadUri != null) {
                        buildReadContentValues();

                        // Check the read flag first. It's much faster to do a query than
                        // to do an update. Timing this function show it's about 10x faster to
                        // do the query compared to the update, even when there's nothing to
                        // update.
                        boolean needUpdate = true;

                        Cursor c = mContext.getContentResolver().query(threadUri,
                                UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                        if (c != null) {
                            try {
                                needUpdate = c.getCount() > 0;
                            } finally {
                                c.close();
                            }
                        }

                        if(needUpdate){
                            mContext.getContentResolver().update(threadUri,mReadContentValues, UNREAD_SELECTION, null);
                            mHasUnreadMessages = false;
                        }
                    }
                }
                WapPushMessagingNotification.updateAllNotifications(mContext);
            }
        }).start();
    }
    
    /**
     * Returns a content:// URI referring to this conversation,
     * or null if it does not exist on disk yet.
     */
    public synchronized Uri getQueryMsgUri() {
        if (mThreadId < 0)
            return null;

        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }

    // Guarantee Thread ID exists in Database
    public synchronized void guaranteeThreadId() {
        mThreadId = getOrCreateThreadId(mContext, mRecipients, false);
    }

    public synchronized long ensureThreadId(boolean scrubForMmsAddress) {
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mNeedForceUpdateThreadId) {
            Xlog.d(TAG, "ensureThreadId(): Need force update thread id.");
        }
        Xlog.d(TAG, "ensureThreadId(): before: ThreadId=" + mThreadId);
        if (mThreadId <= 0 || (mThreadId > 0 && mNeedForceUpdateThreadId)) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients, scrubForMmsAddress);
            mNeedForceUpdateThreadId = false;
        }
        Xlog.d(TAG, "ensureThreadId(): after: ThreadId=" + mThreadId);
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
    }
    
    public synchronized int getUnreadMessageCount() {
        return mUnreadMessageCount;
    }
    
    public synchronized int getMessageStatus() {
        return mMessageStatus;
    }
    
    //wappush: add getType function
    /**
     * Returns type of the thread.
     */
    public synchronized int getType() {
        return mType;
    }
    
    private static long getOrCreateThreadId(Context context, ContactList list, final boolean scrubForMmsAddress) {
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false);
            String number;
            if (cacheContact != null) {
                number = cacheContact.getNumber();
            } else {
                number = c.getNumber();
            }
            if (scrubForMmsAddress) {
                number = MessageUtils.parseMmsAddress(number);
            }
            
            if (!TextUtils.isEmpty(number) && !recipients.contains(number)) {
                recipients.add(number);
            }
        }
        long retVal = 0;
        try{
            retVal = Threads.getOrCreateThreadId(context, recipients);
        }catch(IllegalArgumentException e){
            LogTag.error("Can't get or create the thread id");
            return 0;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }

        return retVal;
    }
    
    //mark all wappush message as seen
    private static void blockingMarkAllWapPushMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(WapPush.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Xlog.d(TAG, "mark " + count + " MMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(WapPush.CONTENT_URI,
                values,
                "seen=0",
                null);

    }
    
    //mark all CellBroadcast message as seen
    private static void blockingMarkAllCellBroadcastMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Telephony.CbSms.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Xlog.d(TAG, "mark " + count + " CB msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Telephony.CbSms.CONTENT_URI,
                values,
                "seen=0",
                null);
    }

    static public void removeInvalidCache(Cursor cursor) {
        if (cursor != null) {
            synchronized (Cache.getInstance()) {
                HashSet<Long> threadsOnDisk = new HashSet<Long>();
                if(cursor.moveToFirst()){
                    do{
                    long threadId = cursor.getLong(ID);

                    threadsOnDisk.add(threadId);
                    }while (cursor.moveToNext());
                }

                Cache.keepOnly(threadsOnDisk);
            }
        }
    }

    public void setIsDoingMarkAsRead(boolean isDoingMarkAsRead) {
        this.mIsDoingMarkAsRead = isDoingMarkAsRead;
    }

    private boolean mNeedForceUpdateThreadId = false;
    public void setNeedForceUpdateThreadId(boolean isNeeded) {
        mNeedForceUpdateThreadId = isNeeded;
    }
  //a1
}
