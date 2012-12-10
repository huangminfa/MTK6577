/*
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

package com.android.providers.telephony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony;
import android.provider.Telephony.CanonicalAddressesColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.ThreadsColumns;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.xlog.Xlog;
import com.google.android.mms.pdu.PduHeaders;
//Add for WapPush
import android.content.ContentUris;
import android.provider.Telephony.WapPush;
import com.mediatek.featureoption.FeatureOption;

/**
 * This class provides the ability to query the MMS and SMS databases
 * at the same time, mixing messages from both in a single thread
 * (A.K.A. conversation).
 *
 * A virtual column, MmsSms.TYPE_DISCRIMINATOR_COLUMN, may be
 * requested in the projection for a query.  Its value is either "mms"
 * or "sms", depending on whether the message represented by the row
 * is an MMS message or an SMS message, respectively.
 *
 * This class also provides the ability to find out what addresses
 * participated in a particular thread.  It doesn't support updates
 * for either of these.
 *
 * This class provides a way to allocate and retrieve thread IDs.
 * This is done atomically through a query.  There is no insert URI
 * for this.
 *
 * Finally, this class provides a way to delete or update all messages
 * in a thread.
 */
public class MmsSmsProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = "MmsSmsProvider";
    private static final String WAPPUSH_TAG = "WapPush/Provider";
    private static final boolean DEBUG = false;

    private static final String NO_DELETES_INSERTS_OR_UPDATES =
            "MmsSmsProvider does not support deletes, inserts, or updates for this URI.";
    private static final int URI_CONVERSATIONS                     = 0;
    private static final int URI_CONVERSATIONS_MESSAGES            = 1;
    private static final int URI_CONVERSATIONS_RECIPIENTS          = 2;
    private static final int URI_MESSAGES_BY_PHONE                 = 3;
    private static final int URI_THREAD_ID                         = 4;
    private static final int URI_CANONICAL_ADDRESS                 = 5;
    private static final int URI_PENDING_MSG                       = 6;
    private static final int URI_COMPLETE_CONVERSATIONS            = 7;
    private static final int URI_UNDELIVERED_MSG                   = 8;
    private static final int URI_CONVERSATIONS_SUBJECT             = 9;
    private static final int URI_NOTIFICATIONS                     = 10;
    private static final int URI_OBSOLETE_THREADS                  = 11;
    private static final int URI_DRAFT                             = 12;
    private static final int URI_CANONICAL_ADDRESSES               = 13;
    private static final int URI_SEARCH                            = 14;
    private static final int URI_SEARCH_SUGGEST                    = 15;
    private static final int URI_FIRST_LOCKED_MESSAGE_ALL          = 16;
    private static final int URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID = 17;
    private static final int URI_MESSAGE_ID_TO_THREAD              = 18;
    private static final int URI_QUICK_TEXT                        = 19;
    private static final int URI_MESSAGES_INBOX                    = 20;
    private static final int URI_MESSAGES_OUTBOX                   = 21;
    private static final int URI_MESSAGES_SENTBOX                  = 22;
    private static final int URI_MESSAGES_DRAFTBOX                 = 23;
    private static final int URI_RECIPIENTS_NUMBER                 = 24;
    private static final int URI_SEARCH_FOLDER                     = 25;
    private static final int URI_STATUS                            = 26;
    private static final int URI_CELLBROADCAST                     = 27;
    private static final int URI_UNREADCOUNT                       = 28;
    private static final int URI_SIMID_LIST                        = 29;
    private static final int URI_FOLDER_DELETE_ALL                 = 30;
    
    private static final int NORMAL_NUMBER_MAX_LENGTH              = 15;

    /**
     * the name of the table that is used to store the queue of
     * messages(both MMS and SMS) to be sent/downloaded.
     */
    public static final String TABLE_PENDING_MSG = "pending_msgs";

    /**
     * the name of the table that is used to store the canonical addresses for both SMS and MMS.
     */
    private static final String TABLE_CANONICAL_ADDRESSES = "canonical_addresses";
    /**
     * the name of the table quicktext
     */
    private static final String TABLE_QUICK_TEXT = "quicktext";
    
    private static final String TABLE_CELLBROADCAST = "cellbroadcast";

    private static final String TABLE_THREADS = "threads";

    private static final Uri PICK_PHONE_EMAIL_URI = Uri
    .parse("content://com.android.contacts/data/phone_email");
    private static final Uri PICK_PHONE_EMAIL_FILTER_URI = Uri.withAppendedPath(
            PICK_PHONE_EMAIL_URI, "filter");

    // These constants are used to construct union queries across the
    // MMS and SMS base tables.

    // These are the columns that appear in both the MMS ("pdu") and
    // SMS ("sms") message tables.
    private static final String[] MMS_SMS_COLUMNS =
            { BaseColumns._ID, Mms.DATE, Mms.DATE_SENT, Mms.READ, Mms.THREAD_ID, Mms.LOCKED, Mms.SIM_ID };

    // These are the columns that appear only in the MMS message
    // table.
    private static final String[] MMS_ONLY_COLUMNS = {
        Mms.CONTENT_CLASS, Mms.CONTENT_LOCATION, Mms.CONTENT_TYPE,
        Mms.DELIVERY_REPORT, Mms.EXPIRY, Mms.MESSAGE_CLASS, Mms.MESSAGE_ID,
        Mms.MESSAGE_SIZE, Mms.MESSAGE_TYPE, Mms.MESSAGE_BOX, Mms.PRIORITY,
        Mms.READ_STATUS, Mms.RESPONSE_STATUS, Mms.RESPONSE_TEXT,
        Mms.RETRIEVE_STATUS, Mms.RETRIEVE_TEXT_CHARSET, Mms.REPORT_ALLOWED,
        Mms.READ_REPORT, Mms.STATUS, Mms.SUBJECT, Mms.SUBJECT_CHARSET,
        Mms.TRANSACTION_ID, Mms.MMS_VERSION, Mms.SERVICE_CENTER };

    // These are the columns that appear only in the SMS message
    // table.
    private static final String[] SMS_ONLY_COLUMNS =
            { "address", "body", "person", "reply_path_present",
              "service_center", "status", "subject", "type", "error_code" };
    private static final String[] CB_ONLY_COLUMNS =
            { "channel_id" };

    // These are all the columns that appear in the "threads" table.
    private static final String[] THREADS_COLUMNS = {
        BaseColumns._ID,
        ThreadsColumns.DATE,
        ThreadsColumns.RECIPIENT_IDS,
        ThreadsColumns.MESSAGE_COUNT
    };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_1 =
            new String[] { CanonicalAddressesColumns.ADDRESS };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
            new String[] { CanonicalAddressesColumns._ID,
                    CanonicalAddressesColumns.ADDRESS };

    // These are all the columns that appear in the MMS and SMS
    // message tables.
    private static final String[] UNION_COLUMNS =
            new String[MMS_SMS_COLUMNS.length
                       + MMS_ONLY_COLUMNS.length
                       + SMS_ONLY_COLUMNS.length];

    // These are all the columns that appear in the MMS table.
    private static final Set<String> MMS_COLUMNS = new HashSet<String>();

    // These are all the columns that appear in the SMS table.
    private static final Set<String> SMS_COLUMNS = new HashSet<String>();
    private static final Set<String> CB_COLUMNS = new HashSet<String>();

    private static final String VND_ANDROID_DIR_MMS_SMS =
            "vnd.android-dir/mms-sms";

    private static final String[] ID_PROJECTION = { BaseColumns._ID };

    private static final String[] STATUS_PROJECTION = { Threads.STATUS };
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String SMS_CONVERSATION_CONSTRAINT = "(" +
            Sms.TYPE + " != " + Sms.MESSAGE_TYPE_DRAFT + ")";

    private static final String MMS_CONVERSATION_CONSTRAINT = "(" +
            Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS + " AND (" +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_SEND_REQ + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND + "))";
    private static final String SELF_ITEM_KEY = "Self_Item_Key";
    private static final String AUTHORITY = "mms-sms";

    static {
        URI_MATCHER.addURI(AUTHORITY, "conversations", URI_CONVERSATIONS);
        URI_MATCHER.addURI(AUTHORITY, "complete-conversations", URI_COMPLETE_CONVERSATIONS);

        // In these patterns, "#" is the thread ID.
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#", URI_CONVERSATIONS_MESSAGES);
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/recipients",
                URI_CONVERSATIONS_RECIPIENTS);

        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/subject",
                URI_CONVERSATIONS_SUBJECT);

        // URI for deleting obsolete threads.
        URI_MATCHER.addURI(AUTHORITY, "conversations/obsolete", URI_OBSOLETE_THREADS);

        URI_MATCHER.addURI(
                AUTHORITY, "messages/byphone/*",
                URI_MESSAGES_BY_PHONE);

        // In this pattern, two query parameter names are expected:
        // "subject" and "recipient."  Multiple "recipient" parameters
        // may be present.
        URI_MATCHER.addURI(AUTHORITY, "threadID", URI_THREAD_ID);

        // Use this pattern to query the canonical address by given ID.
        URI_MATCHER.addURI(AUTHORITY, "canonical-address/#", URI_CANONICAL_ADDRESS);

        // Use this pattern to query all canonical addresses.
        URI_MATCHER.addURI(AUTHORITY, "canonical-addresses", URI_CANONICAL_ADDRESSES);

        URI_MATCHER.addURI(AUTHORITY, "search", URI_SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "searchSuggest", URI_SEARCH_SUGGEST);
        URI_MATCHER.addURI(AUTHORITY, "searchFolder", URI_SEARCH_FOLDER);
        // In this pattern, two query parameters may be supplied:
        // "protocol" and "message." For example:
        //   content://mms-sms/pending?
        //       -> Return all pending messages;
        //   content://mms-sms/pending?protocol=sms
        //       -> Only return pending SMs;
        //   content://mms-sms/pending?protocol=mms&message=1
        //       -> Return the the pending MM which ID equals '1'.
        //
        URI_MATCHER.addURI(AUTHORITY, "pending", URI_PENDING_MSG);

        // Use this pattern to get a list of undelivered messages.
        URI_MATCHER.addURI(AUTHORITY, "undelivered", URI_UNDELIVERED_MSG);

        // Use this pattern to see what delivery status reports (for
        // both MMS and SMS) have not been delivered to the user.
        URI_MATCHER.addURI(AUTHORITY, "notifications", URI_NOTIFICATIONS);

        URI_MATCHER.addURI(AUTHORITY, "draft", URI_DRAFT);

        URI_MATCHER.addURI(AUTHORITY, "locked", URI_FIRST_LOCKED_MESSAGE_ALL);

        URI_MATCHER.addURI(AUTHORITY, "locked/#", URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID);

        URI_MATCHER.addURI(AUTHORITY, "quicktext", URI_QUICK_TEXT);
        
        URI_MATCHER.addURI(AUTHORITY, "cellbroadcast", URI_CELLBROADCAST);
        
        URI_MATCHER.addURI(AUTHORITY, "conversations/status/#", URI_STATUS);

        URI_MATCHER.addURI(AUTHORITY, "messageIdToThread", URI_MESSAGE_ID_TO_THREAD);

        URI_MATCHER.addURI(AUTHORITY, "inbox", URI_MESSAGES_INBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "outbox", URI_MESSAGES_OUTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "sentbox", URI_MESSAGES_SENTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "draftbox", URI_MESSAGES_DRAFTBOX);
        
        URI_MATCHER.addURI(AUTHORITY, "thread_id/#", URI_RECIPIENTS_NUMBER);
        
        URI_MATCHER.addURI(AUTHORITY, "unread_count", URI_UNREADCOUNT);
        
        URI_MATCHER.addURI(AUTHORITY, "simid_list/#", URI_SIMID_LIST);
        
        URI_MATCHER.addURI(AUTHORITY, "folder_delete/#", URI_FOLDER_DELETE_ALL);
        
        initializeColumnSets();
    }

    private SQLiteOpenHelper mOpenHelper;

    private boolean mUseStrictPhoneNumberComparation;

    @Override
    public boolean onCreate() {
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        mUseStrictPhoneNumberComparation =
            getContext().getResources().getBoolean(
                    com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
    	Xlog.d(LOG_TAG, "query uri = " + uri);
        switch(URI_MATCHER.match(uri)) {
            case URI_COMPLETE_CONVERSATIONS:
                cursor = getCompleteConversations(projection, selection, sortOrder);
                break;
            case URI_CONVERSATIONS:
                String simple = uri.getQueryParameter("simple");
                if ((simple != null) && simple.equals("true")) {
                    String threadType = uri.getQueryParameter("thread_type");
                    if (!TextUtils.isEmpty(threadType)) {
                        selection = concatSelections(
                                selection, Threads.TYPE + "=" + threadType);
                    }
                    cursor = getSimpleConversations(
                            projection, selection, selectionArgs, sortOrder);
                    notifyUnreadMessageNumberChanged(getContext());
                } else {
                    cursor = getConversations(
                            projection, selection, sortOrder);
                }
                break;
            case URI_CONVERSATIONS_MESSAGES:
                cursor = getConversationMessages(uri.getPathSegments().get(1), projection,
                        selection, sortOrder);
                break;
            case URI_CONVERSATIONS_RECIPIENTS:
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_CONVERSATIONS_SUBJECT:
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_MESSAGES_BY_PHONE:
                cursor = getMessagesByPhoneNumber(
                        uri.getPathSegments().get(2), projection, selection, sortOrder);
                break;
            case URI_THREAD_ID:
                List<String> recipients = uri.getQueryParameters("recipient");

                //if WAP Push is supported, SMS and WAP Push from same sender will be put in diferent threads
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    if(!uri.getQueryParameters("wappush").isEmpty()){
                        cursor = getWapPushThreadId(recipients);
                    } else {
                        cursor = getThreadId(recipients);
                    }
                    break;
                }
                cursor = getThreadId(recipients);
                break;
            case URI_CANONICAL_ADDRESS: {
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_1,
                        finalSelection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            case URI_CANONICAL_ADDRESSES:
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_2,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case URI_SEARCH_SUGGEST: {       	
            	if (       sortOrder != null
		    			|| selection != null
		    			|| selectionArgs != null
		    			|| projection != null) {
		    		throw new IllegalArgumentException(
		    				"do not specify sortOrder, selection, selectionArgs, or projection" +
		    		"with this query");
		    	}
		        String searchString = uri.getQueryParameter("pattern");
		    	String pattern = "%" + searchString + "%";
		        Xlog.d(LOG_TAG, "search suggest pattern is: " + searchString);
		        if (searchString.trim().equals("") || searchString == null) {
		        	cursor = null;
		        } else {
		        	HashMap<String,String> contactRes = getContactsByNumber(searchString);
		        	String searchContacts = searchContacts(searchString, contactRes);
		        	
		        	String smsIdQuery = String.format("SELECT _id FROM sms WHERE thread_id " + searchContacts);
		        	String smsIn = queryIdAndFormatIn(db, smsIdQuery);
		        	
		        	String mmsIdQuery = String.format("SELECT part._id FROM part JOIN pdu " +
		        			" ON part.mid=pdu._id " +
		        			" WHERE part.ct='text/plain' AND pdu.thread_id " + searchContacts);
		        	String mmsIn = queryIdAndFormatIn(db, mmsIdQuery);
		            String query = String.format("SELECT DISTINCT index_text AS snippet " +
		            		" FROM words WHERE (index_text LIKE '%s') " +
		            		" OR (source_id " + smsIn + " AND table_to_use=1) " +
		            		" OR (source_id " + mmsIn + " AND table_to_use=2) " +
		            		" ORDER BY snippet LIMIT 50", pattern);
		            cursor = db.rawQuery(query, null);
		            Xlog.d(LOG_TAG, "search suggestion cursor count is : " + cursor.getCount());
		        }
		        break;
            }
            case URI_MESSAGE_ID_TO_THREAD: {
                // Given a message ID and an indicator for SMS vs. MMS return
                // the thread id of the corresponding thread.
                try {
                    long id = Long.parseLong(uri.getQueryParameter("row_id"));
                    switch (Integer.parseInt(uri.getQueryParameter("table_to_use"))) {
                        case 1:  // sms
                            cursor = db.query(
                                "sms",
                                new String[] { "thread_id" },
                                "_id=?",
                                new String[] { String.valueOf(id) },
                                null,
                                null,
                                null);
                            break;
                        case 2:  // mms
                            String mmsQuery =
                                "SELECT thread_id FROM pdu,part WHERE ((part.mid=pdu._id) AND " +
                                "(part._id=?))";
                            cursor = db.rawQuery(mmsQuery, new String[] { String.valueOf(id) });
                            break;
                    }
                } catch (NumberFormatException ex) {
                    // ignore... return empty cursor
                }
                break;
            }
            case URI_SEARCH: {
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }

                // This code queries the sms and mms tables and returns a unified result set
                // of text matches.  We query the sms table which is pretty simple.  We also
                // query the pdu, part and addr table to get the mms result.  Note that we're
                // using a UNION so we have to have the same number of result columns from
                // both queries.

                String pattern = uri.getQueryParameter("pattern");
                if (pattern != null){
                   Log.d(LOG_TAG, "URI_SEARCH pattern = " + pattern.length());
                }
                HashMap<String,String> contactRes = getContactsByNumber(pattern);
                String searchContacts = searchContacts(pattern, contactRes);
                String searchString = "%" + pattern + "%";

                String smsProjection = "sms._id as _id,thread_id,address,body,date," +
                "index_text,words._id";
                String mmsProjection = "pdu._id,thread_id,addr.address,pdu.sub as " + "" +
                        "body,pdu.date,0 as index_text,0";

                // search on the words table but return the rows from the corresponding sms table
                String smsQuery = String.format(
                        "SELECT %s FROM sms,words WHERE ((sms.body LIKE ? OR thread_id %s)" +
                        " AND sms._id=words.source_id AND words.table_to_use=1) ",
                        smsProjection,
                        searchContacts);

                // search on the words table but return the rows from the corresponding parts table
                String mmsQuery = String.format(
                        "SELECT %s FROM pdu left join part,addr WHERE (" +
                        "(addr.msg_id=pdu._id) AND " +
                        "(((addr.type=%d) AND (pdu.msg_box == %d)) OR " +
                        "((addr.type=%d) AND (pdu.msg_box != %d))) AND " +
                        "(((part.mid=pdu._id) AND part.ct='text/plain' AND part.text LIKE ?) OR thread_id %s))",
                        mmsProjection,
                        PduHeaders.FROM,
                        Mms.MESSAGE_BOX_INBOX,
                        PduHeaders.TO,
                        Mms.MESSAGE_BOX_INBOX,
                        searchContacts);
                /*
                 * search wap push
                 * table words is not used
                 * field index_text and _id are just used for union operation.
                 */
                String wappushProjection = "wappush._id as _id,thread_id,address, coalesce(text||' '||url,text,url) as body,date," +
                "0 as index_text,1 as _id";
                String wappushQuery = String.format(
                        "SELECT %s FROM wappush WHERE (body LIKE ? OR thread_id %s)",
                        wappushProjection,
                        searchContacts);
                
                // join the results from sms and part (mms)
                //FeatureOption.MTK_WAPPUSH_SUPPORT
                String rawQuery = null;
                if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s) GROUP BY %s ORDER BY %s",
                            smsQuery,
                            mmsQuery,
                            "thread_id",
                            "date DESC");
                }else{
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s UNION %s) GROUP BY %s ORDER BY %s ",
                            smsQuery,
                            wappushQuery,
                            mmsQuery,
                            "thread_id",
                            "date DESC");
                }

                try {
                    if (!FeatureOption.MTK_WAPPUSH_SUPPORT) {
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString});
                    } else {
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString, searchString});
                    }
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "got exception: " + ex.toString());
                    return null;                    
                }
                break;
            }
            case URI_SEARCH_FOLDER: {
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }

                // This code queries the sms and mms tables and returns a unified result set
                // of text matches.  We query the sms table which is pretty simple.  We also
                // query the pdu, part and addr table to get the mms result.  Note that we're
                // using a UNION so we have to have the same number of result columns from
                // both queries.

                String pattern = uri.getQueryParameter("pattern");
                HashMap<String,String> contactRes = getContactsByNumber(pattern);
                String searchContacts = searchContacts(pattern, contactRes);
                String searchString = "%" + pattern + "%";
                String smsProjection = "sms._id as _id,sms.thread_id as thread_id,sms.address as address,sms.body as body,sms.date as date," +
                "index_text,words._id,1 as msg_type,sms.type as msg_box,sms.sim_id as sim_id";
                String smsQuery = String.format(
                        "SELECT %s FROM sms,words WHERE ((sms.body LIKE ? OR sms.thread_id %s)" +
                        " AND sms._id=words.source_id AND words.table_to_use=1) ",
                        smsProjection,
                        searchContacts);
       
                String mmsProjection = "pdu._id,thread_id,addr.address,pdu.sub as " + "" +
                "body,pdu.date,0 as index_text,0 as _id,2 as msg_type,msg_box,sim_id";
                // search on the words table but return the rows from the corresponding parts table
                String mmsQuery = String.format(
                        "SELECT %s FROM pdu left join part,addr WHERE (" +
                        "(addr.msg_id=pdu._id) AND " +
                        "(((addr.type=%d) AND (pdu.msg_box == %d)) OR " +
                        "((addr.type=%d) AND (pdu.msg_box != %d))) AND " +
                        "(((part.mid=pdu._id) AND part.ct='text/plain' AND part.text LIKE ?) OR thread_id %s)) group by pdu._id",
                        mmsProjection,
                        PduHeaders.FROM,
                        Mms.MESSAGE_BOX_INBOX,
                        PduHeaders.TO,
                        Mms.MESSAGE_BOX_INBOX,
                        searchContacts);
                /*
                 * search wap push
                 * table words is not used
                 * field index_text and _id are just used for union operation.
                 */
                String wappushProjection = "wappush._id as _id,thread_id,address, coalesce(text||' '||url,text,url) as body,date," +
                "0 as index_text,1 as _id,3 as msg_type,1 as msg_box,sim_id";
                String wappushQuery = String.format(
                        "SELECT %s FROM wappush WHERE (body LIKE ? OR thread_id %s)",
                        wappushProjection,
                        searchContacts);
                
                // join the results from sms and part (mms)
                //FeatureOption.MTK_WAPPUSH_SUPPORT
                String rawQuery = null;
                if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s) ORDER BY %s",
                            smsQuery,
                            mmsQuery,
                            "date DESC");
                }else{
                    rawQuery = String.format(
                            "SELECT * FROM (%s UNION %s UNION %s) ORDER BY %s ",
                            smsQuery,
                            wappushQuery,
                            mmsQuery,
                            "date DESC");
                } 

                try {
                    if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString});
                    }else{
                        cursor = db.rawQuery(rawQuery, new String[] {searchString, searchString, searchString});
                    }
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "got exception: " + ex.toString());
                    return null;                    
                }
                break;
            }
            case URI_PENDING_MSG: {
                String protoName = uri.getQueryParameter("protocol");
                String msgId = uri.getQueryParameter("message");
                int proto = TextUtils.isEmpty(protoName) ? -1
                        : (protoName.equals("sms") ? MmsSms.SMS_PROTO : MmsSms.MMS_PROTO);

                String extraSelection = (proto != -1) ?
                        (PendingMessages.PROTO_TYPE + "=" + proto) : " 0=0 ";
                if (!TextUtils.isEmpty(msgId)) {
                    extraSelection += " AND " + PendingMessages.MSG_ID + "=" + msgId;
                }

                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : ("(" + extraSelection + ") AND " + selection);
                String finalOrder = TextUtils.isEmpty(sortOrder)
                        ? PendingMessages.DUE_TIME : sortOrder;
                cursor = db.query(TABLE_PENDING_MSG, null,
                        finalSelection, selectionArgs, null, null, finalOrder);
                break;
            }
            case URI_UNDELIVERED_MSG: {
                cursor = getUndeliveredMessages(projection, selection,
                        selectionArgs, sortOrder);
                break;
            }
            case URI_DRAFT: {
                cursor = getDraftThread(projection, selection, sortOrder);
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID: {
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                cursor = getFirstLockedMessage(projection, "thread_id=" + Long.toString(threadId),
                        sortOrder);
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_ALL: {
                cursor = getFirstLockedMessage(projection, selection, sortOrder);
                break;
            }
            case URI_QUICK_TEXT: {
                cursor = db.query(TABLE_QUICK_TEXT, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_STATUS:{
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                    Xlog.d(LOG_TAG, "query URI_STATUS Thread ID is " + threadId);
                } catch (NumberFormatException e) {
                    Xlog.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                cursor = db.query(TABLE_THREADS, STATUS_PROJECTION,
                		"_id=" + Long.toString(threadId), null, null, null, sortOrder);
                Xlog.d(LOG_TAG, "query URI_STATUS ok");
            	break;
            }
            case URI_MESSAGES_INBOX: {
                 cursor = getInboxMessage(db, selection);
                 notifyUnreadMessageNumberChanged(getContext());
                break;
            }
            case URI_MESSAGES_OUTBOX: {
                cursor = getOutboxMessage(db, selection);
                notifyUnreadMessageNumberChanged(getContext());
                break;
            }
            case URI_MESSAGES_SENTBOX: {
               cursor = getSentboxMessage(db, selection);
               notifyUnreadMessageNumberChanged(getContext());
                break;
            }
            case URI_MESSAGES_DRAFTBOX: {
                cursor = getDraftboxMessage(db);
                break;
            }
            
            case URI_RECIPIENTS_NUMBER: {
                cursor = getRecipientsNumber(uri.getPathSegments().get(1));
                break;
            }
            
            case URI_UNREADCOUNT: {
                cursor = getAllUnreadCount(db);
                break;
            }
            case URI_SIMID_LIST: {
            	  long threadId;
                  try {
                      threadId = Long.parseLong(uri.getLastPathSegment());
                      Xlog.d(LOG_TAG, "query URI_SIMID_LIST Thread ID is " + threadId);
                  } catch (NumberFormatException e) {
                      Xlog.e(LOG_TAG, "URI_SIMID_LIST Thread ID must be a long.");
                      break;
                  }
                cursor = getSimidListByThread(db, threadId);
                break;
            }
            default:
                throw new IllegalStateException("Unrecognized URI:" + uri);
        }
        Xlog.d(LOG_TAG, "query end");
        cursor.setNotificationUri(getContext().getContentResolver(), MmsSms.CONTENT_URI);
        return cursor;
    }

    private String queryIdAndFormatIn(SQLiteDatabase db, String sql) {
    	Cursor cursor = null;
    	Xlog.d(LOG_TAG, "queryIdAndFormatIn sql is: " + sql);
    	if (sql != null && sql.trim() != "") {
    		cursor = db.rawQuery(sql, null);
    	}
    	if (cursor == null) {
    		return " IN () ";
    	}
    	try {
    		Xlog.d(LOG_TAG, "queryIdAndFormatIn Cursor count is: " + cursor.getCount());
    		Set<Long> ids = new HashSet<Long>();
    		while (cursor.moveToNext()) {
    			Long id = cursor.getLong(0);
    			ids.add(id);
    		}
    		/* to IN sql */
    		String in = " IN ";
    		in += ids.toString();
    		in = in.replace('[', '(');
    		in = in.replace(']', ')');
    		Xlog.d(LOG_TAG,"queryIdAndFormatIn, In = " + in);
    		return in;
    	} finally {
    		cursor.close();
    	}
    }
    
    private Cursor getAllUnreadCount(SQLiteDatabase db){
    	Xlog.d(LOG_TAG, "getAllUnreadCount begin");
        String rawQuery = ""; 
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
             rawQuery = String.format("SELECT COUNT(_id) FROM (SELECT _id,date FROM sms WHERE read=0 " +
             "UNION SELECT _id,date FROM pdu WHERE read=0 AND (pdu.m_type=132 OR pdu.m_type=130 OR pdu.m_type=128) UNION SELECT _id,date FROM cellbroadcast WHERE read=0 " +
             "UNION SELECT _id,date FROM wappush WHERE read=0)"); 
        } else {
             rawQuery = String.format("SELECT COUNT(_id) FROM (SELECT _id,date FROM sms WHERE read=0 " +
            "UNION SELECT _id,date FROM pdu WHERE read=0 AND (pdu.m_type=132 OR pdu.m_type=130 OR pdu.m_type=128) UNION SELECT _id,date FROM cellbroadcast WHERE read=0)"); 
        }
        //Xlog.d(LOG_TAG, "getAllUnreadCount rawQuery = " +rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    private Cursor getSimidListByThread(SQLiteDatabase db, long threadId){
        String rawQuery = String.format("SELECT DISTINCT sim_id FROM" +
        		"(SELECT DISTINCT sim_id FROM sms WHERE thread_id=" + threadId +
        		" UNION SELECT DISTINCT sim_id FROM pdu WHERE thread_id=" + threadId + ")");
    	Xlog.d(LOG_TAG, "getSimidListByThread begin rawQuery = " + rawQuery);
        return db.rawQuery(rawQuery, null);
    }
    
    private String getSmsProjection(){
        String smsProjection = 
                "sms._id as _id," +
                "sms.thread_id as thread_id," +
                "sms.address as address," +
                "sms.body as body," +
                "sms.date as date," +
                "sms.read as read," +
                "1 as msg_type," +
                "sms.status as status," +
                "0 as attachment," +
                "0 as m_type," +
                "sms.sim_id as sim_id," +
                "sms.type as box_type," +
                "0 as sub_cs," +
                "sms.locked as locked ";
        return smsProjection;
    }
     
    private String getSmsDraftProjection(){
        String smsProjection = 
                "sms._id as _id," +
                "sms.thread_id as thread_id," +
                "threads.recipient_ids as address," +
                "sms.body as body," +
                "sms.date as date," +
                "sms.read as read," +
                "1 as msg_type," +
                "sms.status as status," +
                "0 as attachment," +
                "0 as m_type," +
                "sms.sim_id as sim_id," +
                "sms.type as box_type," +
                "0 as sub_cs," +
                "sms.locked as locked ";
        return smsProjection;
    }
    
    private String getMmsProjection(){
        String mmsProjection = 
            "pdu._id as _id," +
            "pdu.thread_id as thread_id," +
            "threads.recipient_ids as address," +
            "pdu.sub as body," +
            "pdu.date * 1000 as date," +
            "pdu.read as read," +
            "2 as msg_type," +
            "pending_msgs.err_type as status," +
            "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
            "pdu.m_type as m_type," +
            "pdu.sim_id as sim_id," +
            "pdu.msg_box as box_type," +
            "pdu.sub_cs as sub_cs," +
            "pdu.locked as locked ";
        return mmsProjection;
    }
    
    private String getFinalProjection(){
        String finalProjection = "_id,thread_id,address,body,date,read,msg_type,status," +
        		"MAX(attachment) as attachment,m_type,sim_id,box_type,sub_cs" +
                ",locked ";
        return finalProjection;
    }
    
    private Cursor getInboxMessage(SQLiteDatabase db, String selection) {
         String cbProjection = "cellbroadcast._id as _id,cellbroadcast.thread_id as thread_id,threads.recipient_ids as address," +
                 "cellbroadcast.body,cellbroadcast.date as date,cellbroadcast.read as read,4 as msg_type,0 as status,0 as attachment,0 as m_type" +
                 ",cellbroadcast.sim_id as sim_id,0 as box_type,0 as sub_cs" +
                 ",cellbroadcast.locked as locked";
         String smsWhere = concatSelections("sms.type=1", selection);
         String smsQuery = String.format("SELECT %s FROM sms WHERE " + smsWhere, getSmsProjection());
         String mmsWhere = concatSelections(" AND pdu.msg_box=1", selection);
         String mmsQuery = String.format(
                 "SELECT %s FROM threads,pending_msgs,pdu left join part ON pdu._id = part.mid WHERE (pdu.m_type=130 OR pdu.m_type=132) AND pending_msgs.msg_id=pdu._id " +
                 "AND pdu.thread_id=threads._id" + mmsWhere, getMmsProjection());
         String mmsNotInPendingQuery = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address," +
         		"pdu.sub as body,pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status,(part.ct!='text/plain' AND part.ct!='application/smil' " +
                 ") as attachment,pdu.m_type as m_type,pdu.sim_id as sim_id,pdu.msg_box as box_type,pdu.sub_cs as sub_cs" +
                 ",pdu.locked as locked " + 
                 " FROM " +
         		"threads,pdu left join part ON pdu._id = part.mid WHERE (pdu.m_type=130 OR pdu.m_type=132) AND pdu.thread_id=threads._id AND" +
         		" pdu._id NOT IN( SELECT pdu._id FROM pdu, pending_msgs WHERE pending_msgs.msg_id=pdu._id)" + mmsWhere);
         String mmsNotInPartQeury = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body," +
         		"pdu.date * 1000 as date,pdu.read as read,2 as msg_type,pending_msgs.err_type as status,0 as attachment,pdu.m_type as m_type,pdu.sim_id as sim_id," +
                 "pdu.msg_box as box_type,pdu.sub_cs as sub_cs " +
                 ",pdu.locked as locked " + 
                 " FROM pdu,threads,pending_msgs WHERE (pdu.m_type=130 OR pdu.m_type=132) AND " +
         		"pending_msgs.msg_id=pdu._id AND pdu.thread_id=threads._id AND pdu._id NOT IN (SELECT pdu._id FROM pdu,part WHERE pdu._id=part.mid)" + mmsWhere);
         String mmsNotInBothQeury = String.format("SELECT pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body,pdu.date * 1000 as date," +
                 "pdu.read as read,2 as msg_type,0 as status,0 as attachment,pdu.m_type as m_type,pdu.sim_id as sim_id,pdu.msg_box as box_type,pdu.sub_cs as sub_cs " +
                 ",pdu.locked as locked " + 
                 " FROM pdu," +
         		"threads WHERE (pdu.m_type=130 OR pdu.m_type=132) AND pdu.thread_id=threads._id AND pdu._id NOT IN " +
         		"(SELECT pdu._id FROM pdu,part WHERE pdu._id=part.mid UNION SELECT pdu._id FROM pdu, pending_msgs WHERE pending_msgs.msg_id=pdu._id)" + mmsWhere);
         String cbWhere = concatSelections(" cellbroadcast.thread_id=threads._id ", selection);
         String cbQuery = String.format("SELECT %s FROM cellbroadcast,threads WHERE " + cbWhere, cbProjection);
         String rawQuery = null;
         if(!FeatureOption.MTK_WAPPUSH_SUPPORT){
             rawQuery = String.format(
                     "SELECT %s FROM (%s UNION %s UNION %s UNION %s UNION %s UNION %s) GROUP BY _id,msg_type ORDER BY %s",
                     getFinalProjection(),
                     smsQuery,
                     mmsQuery,
                     mmsNotInPendingQuery,
                     mmsNotInPartQeury,
                     mmsNotInBothQeury,
                     cbQuery,
                     "date DESC");
         } else {
            String wappushProjection = "wappush._id as _id,thread_id,address,coalesce(text||' '||url,text,url) as body," +
                    "date,read,3 as msg_type,0 as status,0 as attachment,0 as m_type,wappush.sim_id as sim_id,0 as box_type,0 as sub_cs" +
                    ",wappush.locked as locked ";    
            String wappushQuery;
            if (selection!=null && selection.length()!=0) {
            	wappushQuery = String.format("SELECT %s FROM wappush WHERE " + selection, wappushProjection);
            } else {
            	wappushQuery = String.format("SELECT %s FROM wappush ", wappushProjection);
            }
              rawQuery = String.format(
                      "SELECT %s FROM (%s UNION %s UNION %s UNION %s UNION %s UNION %s UNION %s) GROUP BY _id,msg_type ORDER BY %s",
                      getFinalProjection(),
                      smsQuery,
                      mmsQuery,
                      mmsNotInPendingQuery,
                      mmsNotInPartQeury,
                      mmsNotInBothQeury,
                      cbQuery,
                      wappushQuery,
                      "date DESC");
              
         }
        // Log.d(LOG_TAG, "getInboxMessage rawQuery ="+rawQuery);
         return db.rawQuery(rawQuery, null);
    }

    private Cursor getOutboxMessage(SQLiteDatabase db, String selection) {
        String smsWhere = concatSelections("(sms.type=4 OR sms.type=5 OR sms.type=6)", selection);
        String smsQuery = String.format("SELECT %s FROM sms WHERE " + smsWhere,
                getSmsProjection());
        String mmsWhere = concatSelections("pdu.msg_box=4", selection);
        String mmsQuery = String.format("SELECT %s FROM threads,pending_msgs,pdu left join part ON pdu._id = part.mid WHERE" +
                " (pdu.m_type=128 AND pdu.thread_id=threads._id AND pending_msgs.msg_id=pdu._id) AND " + mmsWhere, getMmsProjection());
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(), smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getOutboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    private Cursor getSentboxMessage(SQLiteDatabase db, String selection) {
        String mmsProjection = "pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address,pdu.sub as body," +
                "pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status," +
                "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
                "pdu.m_type as m_type,pdu.sim_id as sim_id,pdu.msg_box as box_type,pdu.sub_cs as sub_cs" +
                ",pdu.locked as locked "; 
        String smsWhere = concatSelections("sms.type=2", selection);
        String smsQuery = String.format("SELECT %s FROM sms WHERE " + smsWhere, getSmsProjection());
        String mmsWhere = concatSelections("pdu.msg_box=2", selection);
        String mmsQuery = String.format("SELECT %s FROM threads,pdu left join part ON pdu._id = part.mid WHERE pdu.m_type=128" +
                " AND pdu.thread_id=threads._id AND " + mmsWhere, mmsProjection);
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(),
                smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getSentboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    private Cursor getDraftboxMessage(SQLiteDatabase db) {
        String mmsProjection = "pdu._id as _id,pdu.thread_id as thread_id,threads.recipient_ids as address," +
         "pdu.sub as body,pdu.date * 1000 as date,pdu.read as read,2 as msg_type,0 as status," +
         "(part.ct!='text/plain' AND part.ct!='application/smil') as attachment," +
         "pdu.m_type as m_type,pdu.sim_id as sim_id,pdu.msg_box as box_type,pdu.sub_cs as sub_cs " +
         ", pdu.locked as locked "; 

        String smsQuery = String.format("SELECT %s FROM sms,threads WHERE sms.type=3 " +
                "AND sms.thread_id=threads._id", getSmsDraftProjection());
        String mmsQuery = String.format("SELECT %s FROM threads,pdu left join part ON pdu._id = part.mid WHERE pdu.msg_box = 3 " +
                "AND pdu.thread_id=threads._id", mmsProjection);
        String rawQuery = String.format("SELECT %s FROM (%s UNION %s) GROUP BY _id,msg_type ORDER BY %s", getFinalProjection(),
                smsQuery, mmsQuery, "date DESC");
       // Log.d(LOG_TAG, "getDraftboxMessage rawQuery =" + rawQuery);
        return db.rawQuery(rawQuery, null);
    }

    // through threadid to get the recipient number.
    private Cursor getRecipientsNumber(String threadId) {
         
        String outerQuery = String.format("SELECT recipient_ids FROM threads WHERE _id = " + threadId);
        Log.d(LOG_TAG, "getRecipientsNumber " + outerQuery);
        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    private HashMap<String,String> getContactsByNumber(String pattern){
        Builder builder = PICK_PHONE_EMAIL_FILTER_URI.buildUpon();
        builder.appendPath(pattern);      // Builder will encode the query
         Log.d(LOG_TAG, "getContactsByNumber uri = " + builder.build().toString());
        Cursor cursor = getContext().getContentResolver().query(builder.build(), 
                new String[] {Phone.DISPLAY_NAME_PRIMARY, Phone.NUMBER}, null, null, "sort_key");
        
        /* query the related contact numbers and name */
        HashMap<String,String> contacts = new HashMap<String,String>();
         Log.d(LOG_TAG, "getContactsByNumber getContentResolver query contact 1 cursor " + cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String number = getValidNumber(cursor.getString(1));
                Xlog.d(LOG_TAG,"getContactsByNumber number = " + number + " name = " + name);
                contacts.put(number,name);
            }
        } finally {
            cursor.close();
        }
         return contacts;
    }
 
    private String searchContacts(String pattern, HashMap<String,String> contactRes) { 
        String in = " IN ";
        /* query the related thread ids */
        Set<Long> threadIds = new HashSet<Long>();
        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(
                "SELECT " + Threads._ID + "," + Threads.RECIPIENT_IDS + " FROM threads", null);
        try {
            while (cursor.moveToNext()) {
                Long threadId = cursor.getLong(0);
                Set<String> recipients = searchRecipients(cursor.getString(1));
                for (String recipient : recipients) {
                   // Log.d(LOG_TAG, "searchContacts cursor recipient " + recipient);
                    if (recipient.contains(pattern) || likeContacts(contactRes, pattern, recipient)) {
                        threadIds.add(threadId);
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        Log.d(LOG_TAG, "searchContacts getContentResolver query recipient");
        /* to IN sql */
        in += threadIds.toString();
        in = in.replace('[', '(');
        in = in.replace(']', ')');
        Xlog.d(LOG_TAG,"searchContacts in = "+in);
        return in;
    }
    
    public static String getValidNumber(String numberOrEmail) {
        if (numberOrEmail == null) {
            return null;
        }
       // Xlog.d(LOG_TAG, "Contact.getValidNumber(): numberOrEmail=" + numberOrEmail);
        String workingNumberOrEmail = new String(numberOrEmail);
        workingNumberOrEmail = workingNumberOrEmail.replaceAll(" ", "").replaceAll("-", "");
        if (numberOrEmail.equals(SELF_ITEM_KEY) || Mms.isEmailAddress(numberOrEmail)) {
            //Xlog.d(LOG_TAG, "Contact.getValidNumber(): The number is me or Email.");
            return numberOrEmail;
        } else if (PhoneNumberUtils.isWellFormedSmsAddress(workingNumberOrEmail)) {
          //  Xlog.d(LOG_TAG, "Contact.getValidNumber(): Number without space and '-' is a well-formed number for sending sms.");
            return workingNumberOrEmail;
        } else {
           // Xlog.d(LOG_TAG, "Contact.getValidNumber(): Unknown formed number");
            workingNumberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            workingNumberOrEmail = PhoneNumberUtils.formatNumber(workingNumberOrEmail);
            if (numberOrEmail.equals(workingNumberOrEmail)) {
           //     Xlog.d(LOG_TAG, "Contact.getValidNumber(): Unknown formed number, but the number without local number formatting is a well-formed number.");
                return PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            } else {
                return numberOrEmail;
            }
        }
    }

    /* match the cantacts name*/
    private boolean likeContacts(HashMap<String,String> contacts,String pattern, String recipient) {
    	if (contacts == null ||contacts.isEmpty()) {
           //Xlog.d(LOG_TAG,"likeContacts is null");
    	   return false;
    	}
    	Iterator iter = contacts.entrySet().iterator();
    	while (iter.hasNext()){
           Map.Entry entry = (Map.Entry) iter.next();
           String number = (String) entry.getKey();
           String name = (String) entry.getValue();
           if (number.equals(recipient) || number.contains(recipient) || recipient.contains(number)) {
              Xlog.d(LOG_TAG,"name.contains(pattern) name = "+name+" number = "+number);
              return true;
          }
        }
        return false;
    }
    
    private Set<String> searchRecipients(String recipientIds) {
        /* link the recipient ids to the addresses */
        Set<String> recipients = new HashSet<String>();
        for (String id : recipientIds.split(" ")) {
            /* search the canonical address */
            Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(
                    "SELECT address FROM canonical_addresses WHERE _id=?", new String[] {id});
            try {
                if (cursor == null || cursor.getCount() == 0) {
                    Xlog.d(LOG_TAG, "searchRecipients cursor is null");
                    break;
                }
                cursor.moveToFirst();
                String address = cursor.getString(0);
                if (!address.trim().isEmpty()) {
                    recipients.add(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return recipients;
    }

    /**
     * Return the canonical address ID for this address.
     */
    private long getSingleAddressId(String address) {
        boolean isEmail = Mms.isEmailAddress(address);
        boolean isPhoneNumber = Mms.isPhoneNumber(address);

        // We lowercase all email addresses, but not addresses that aren't numbers, because
        // that would incorrectly turn an address such as "My Vodafone" into "my vodafone"
        // and the thread title would be incorrect when displayed in the UI.
        String refinedAddress = isEmail ? address.toLowerCase() : address;
        String selection = "address=?";
        String[] selectionArgs;
        long retVal = -1L;
        Xlog.d(LOG_TAG, "refinedAddress = " + refinedAddress);
        if (!isPhoneNumber || (address != null && address.length() > NORMAL_NUMBER_MAX_LENGTH)) {
            selectionArgs = new String[] { refinedAddress };
        } else {
            selection += " OR " + String.format(Locale.ENGLISH, "PHONE_NUMBERS_EQUAL(address, ?, %d)",
                        (mUseStrictPhoneNumberComparation ? 1 : 0));
            selectionArgs = new String[] { refinedAddress, refinedAddress };
        }
        Xlog.i(LOG_TAG, "selection: " + selection);
        Cursor cursor = null;

        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.query(
                    "canonical_addresses", CANONICAL_ADDRESSES_COLUMNS_2,
                    selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {
                retVal = insertCanonicalAddresses(mOpenHelper, refinedAddress);
                Xlog.d(LOG_TAG, "getSingleAddressId: insert new canonical_address for " +
                        /*address*/ "xxxxxx" + ", _id=" + retVal);
                return retVal;
            } else {
                Xlog.d(LOG_TAG, "getSingleAddressId(): number matched count is " + cursor.getCount());
                while (cursor.moveToNext()) {
                    String currentNumber = cursor.getString(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns.ADDRESS));
                    Xlog.d(LOG_TAG, "getSingleAddressId(): currentNumber != null ? " + (currentNumber != null));
                    if (currentNumber != null) {
                        Xlog.d(LOG_TAG, "getSingleAddressId(): refinedAddress=" + refinedAddress + ", currentNumber=" + currentNumber);
                        Xlog.d(LOG_TAG, "getSingleAddressId(): currentNumber.length() > 15 ?= " + (currentNumber.length() > NORMAL_NUMBER_MAX_LENGTH));
                        if (refinedAddress.equals(currentNumber) || currentNumber.length() <= NORMAL_NUMBER_MAX_LENGTH) {
                            retVal = cursor.getLong(cursor.getColumnIndexOrThrow(CanonicalAddressesColumns._ID));
                            Xlog.d(LOG_TAG, "getSingleAddressId(): get exist id=" + retVal);
                            break;
                        }
                    }
                }
                if (retVal == -1) {
                    retVal = insertCanonicalAddresses(mOpenHelper, refinedAddress);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return retVal;
    }

    private long insertCanonicalAddresses(SQLiteOpenHelper openHelper, String refinedAddress) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CanonicalAddressesColumns.ADDRESS, refinedAddress);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.insert("canonical_addresses", CanonicalAddressesColumns.ADDRESS, contentValues);
    }

    /**
     * Return the canonical address IDs for these addresses.
     */
    private Set<Long> getAddressIds(List<String> addresses) {
        Set<Long> result = new HashSet<Long>(addresses.size());

        for (String address : addresses) {
            if (!address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = getSingleAddressId(address);
                if (id != -1L) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "getAddressIds: address ID not found for " + address);
                }
            }
        }
        return result;
    }

    /**
     * Return a sorted array of the given Set of Longs.
     */
    private long[] getSortedSet(Set<Long> numbers) {
        int size = numbers.size();
        long[] result = new long[size];
        int i = 0;

        for (Long number : numbers) {
            result[i++] = number;
        }

        if (size > 1) {
            Arrays.sort(result);
        }

        return result;
    }

    /**
     * Return a String of the numbers in the given array, in order,
     * separated by spaces.
     */
    private String getSpaceSeparatedNumbers(long[] numbers) {
        int size = numbers.length;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(' ');
            }
            buffer.append(numbers[i]);
        }
        return buffer.toString();
    }

    /**
     * Insert a record for a new thread.
     */
    private void insertThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put("status", 1);
        if (numberOfRecipients > 1) {
            values.put(Threads.TYPE, Threads.BROADCAST_THREAD);
        }
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.d(LOG_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);

        //getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
    }

    private static final String THREAD_QUERY;      
    //Add query parameter "type" so that SMS & WAP Push Message from same sender will be put in different threads.
    static{
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE type<>" + Threads.WAPPUSH_THREAD + " AND recipient_ids=?";
        }else{
            THREAD_QUERY = "SELECT _id FROM threads " + "WHERE recipient_ids=?";
        }
    }

    /**
     * Return the thread ID for this list of
     * recipients IDs.  If no thread exists with this ID, create
     * one and return it.  Callers should always use
     * Threads.getThreadId to access this information.
     */
    private synchronized Cursor getThreadId(List<String> recipients) {
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";

        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }

        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
        if (cursor.getCount() == 0) {
            cursor.close();

            Log.d(LOG_TAG, "getThreadId: create new thread_id for recipients " + recipients);
            insertThread(recipientIds, recipients.size());

            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
        }
        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getThreadId: why is cursorCount=" + cursor.getCount());
        }

        return cursor;
    }
    
    /**
     * Insert a record for a new wap push thread.
     */
    private void insertWapPushThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.TYPE, Threads.WAPPUSH_THREAD);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Xlog.d(WAPPUSH_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);
        Xlog.w(WAPPUSH_TAG,"insertWapPushThread!");
        notifyChange();
    }
    /*
    private void insertCellBroadcastThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        values.put(ThreadsColumns.TYPE, Threads.WAPPUSH_THREAD);

        long result = mOpenHelper.getWritableDatabase().insert("threads", null, values);
        Log.d("PUSH", "insertThread: created new thread_id " + result +
                " for recipientIds " + recipientIds);
        Log.w("PUSH","insertCellBroadcastThread!");
        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
    }
    */
    /**
     * Return the wappush thread ID for this list of
     * recipients IDs.  If no thread exists with this ID, create
     * one and return it. It should only be called for wappush
     * 
     */
    private synchronized Cursor getWapPushThreadId(List<String> recipients) {
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";
        
        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getWapPushThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }
        
        String queryString = "SELECT _id FROM threads " + "WHERE type=" + Threads.WAPPUSH_THREAD + " AND recipient_ids=?";
        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, selectionArgs);

        if (cursor.getCount() == 0) {
            cursor.close();

            Log.d(LOG_TAG, "getWapPushThreadId: create new thread_id for recipients " + recipients);
            insertWapPushThread(recipientIds, recipients.size());

            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            cursor = db.rawQuery(queryString, selectionArgs);
        }

        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getWapPushThreadId: why is cursorCount=" + cursor.getCount());
        }

        return cursor;
    }
    /*
    private synchronized Cursor getCellBroadcastThreadId(List<String> recipients) {
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";

        // optimize for size==1, which should be most of the cases
        if (addressIds.size() == 1) {
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getCellBroadcastThreadId: recipientIds (selectionArgs) =" + recipientIds);
        }
        
        String queryString = "SELECT _id FROM threads " + "WHERE type=" + Threads.BROADCAST_THREAD + " AND recipient_ids=?";
        String[] selectionArgs = new String[] { recipientIds };
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, selectionArgs);

        if (cursor.getCount() == 0) {
            cursor.close();

            Log.d(LOG_TAG, "getCellBroadcastThreadId: create new thread_id for recipients " + recipients);
            insertCellBroadcastThread(recipientIds, recipients.size());

            db = mOpenHelper.getReadableDatabase();  // In case insertThread closed it
            cursor = db.rawQuery(queryString, selectionArgs);
        }
        
        if (cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getCellBroadcastThreadId: why is cursorCount=" + cursor.getCount());
        }
        
        return cursor;
    }
    */
    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }

    /**
     * If a null projection is given, return the union of all columns
     * in both the MMS and SMS messages tables.  Otherwise, return the
     * given projection.
     */
    private static String[] handleNullMessageProjection(
            String[] projection) {
        return projection == null ? UNION_COLUMNS : projection;
    }

    /**
     * If a null projection is given, return the set of all columns in
     * the threads table.  Otherwise, return the given projection.
     */
    private static String[] handleNullThreadsProjection(
            String[] projection) {
        return projection == null ? THREADS_COLUMNS : projection;
    }

    /**
     * If a null sort order is given, return "normalized_date ASC".
     * Otherwise, return the given sort order.
     */
    private static String handleNullSortOrder (String sortOrder) {
        return sortOrder == null ? "normalized_date ASC" : sortOrder;
    }

    /**
     * Return existing threads in the database.
     */
    private Cursor getSimpleConversations(String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query("threads", projection,
                selection, selectionArgs, null, null, " date DESC");
    }

    /**
     * Return the thread which has draft in both MMS and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT _id, thread_id, ...
     *             FROM pdu
     *             WHERE msg_box = 3 AND ...
     *           UNION
     *           SELECT _id, thread_id, ...
     *             FROM sms
     *             WHERE type = 3 AND ...
     *          )
     *   ;
     */
    private Cursor getDraftThread(String[] projection, String selection,
            String sortOrder) {
        String[] innerProjection = new String[] {BaseColumns._ID, Conversations.THREAD_ID};
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_DRAFTS),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT),
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                projection, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the most recent message in each conversation in both MMS
     * and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT thread_id AS tid, date * 1000 AS normalized_date, ...
     *             FROM pdu
     *             WHERE msg_box != 3 AND ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date)
     *           UNION
     *           SELECT thread_id AS tid, date AS normalized_date, ...
     *             FROM sms
     *             WHERE ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date))
     *     GROUP BY tid
     *     HAVING normalized_date = MAX(normalized_date);
     *
     * The msg_box != 3 comparisons ensure that we don't include draft
     * messages.
     */
    private Cursor getConversations(String[] projection, String selection,
            String sortOrder) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] columns = handleNullMessageProjection(projection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, MMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                columns, null, "tid",
                "normalized_date = MAX(normalized_date)", sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the first locked message found in the union of MMS
     * and SMS messages.
     *
     * Use this query:
     *
     *  SELECT _id FROM pdu GROUP BY _id HAVING locked=1 UNION SELECT _id FROM sms GROUP
     *      BY _id HAVING locked=1 LIMIT 1
     *
     * We limit by 1 because we're only interested in knowing if
     * there is *any* locked message, not the actual messages themselves.
     */
    private Cursor getFirstLockedMessage(String[] projection, String selection,
            String sortOrder) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder wappushQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(MmsProvider.TABLE_PDU);
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] idColumn = new String[] { BaseColumns._ID };

        // NOTE: buildUnionSubQuery *ignores* selectionArgs
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "mms",
                selection,
                BaseColumns._ID, "locked=1");

        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "sms",
                selection,
                BaseColumns._ID, "locked=1");

        String wappushSubQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            wappushQueryBuilder.setTables(WapPushProvider.TABLE_WAPPUSH);
            wappushSubQuery = wappushQueryBuilder.buildUnionSubQuery(
                    MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                    null, 1, "wappush",
                    selection,
                    BaseColumns._ID, "locked=1");
        }

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = null;
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery, wappushSubQuery }, null, "1");
        }else{
            unionQuery = unionQueryBuilder.buildUnionQuery(
                    new String[] { mmsSubQuery, smsSubQuery }, null, "1");
        }

        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);

        if (DEBUG) {
            Xlog.v(LOG_TAG, "getFirstLockedMessage query: " + unionQuery);
            Xlog.v(LOG_TAG, "cursor count: " + cursor.getCount());
        }
        return cursor;
    }

    /**
     * Return every message in each conversation in both MMS
     * and SMS.
     */
    private Cursor getCompleteConversations(String[] projection,
            String selection, String sortOrder) {
        String unionQuery = buildConversationQuery(projection, selection, sortOrder);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date and thread_id to the list of columns for an
     * inner projection.  This is necessary so that the outer query
     * can have access to these columns even if the caller hasn't
     * requested them in the result.
     */
    private String[] makeProjectionWithDateAndThreadId(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 2];

        result[0] = "thread_id AS tid";
        result[1] = "date * " + dateMultiple + " AS normalized_date";
        for (int i = 0; i < projectionSize; i++) {
            result[i + 2] = projection[i];
        }
        return result;
    }

    /**
     * Return the union of MMS and SMS messages for this thread ID.
     */
    private Cursor getConversationMessages(
            String threadIdString, String[] projection, String selection,
            String sortOrder) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String finalSelection = concatSelections(
                selection, "thread_id = " + threadIdString);
        String unionQuery = buildConversationQuery(projection, finalSelection, sortOrder);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the union of MMS and SMS messages whose recipients
     * included this phone number.
     *
     * Use this query:
     *
     * SELECT ...
     *   FROM pdu, (SELECT _id AS address_id
     *              FROM addr
     *              WHERE (address='<phoneNumber>' OR
     *              PHONE_NUMBERS_EQUAL(addr.address, '<phoneNumber>', 1/0)))
     *             AS matching_addresses
     *   WHERE pdu._id = matching_addresses.address_id
     * UNION
     * SELECT ...
     *   FROM sms
     *   WHERE (address='<phoneNumber>' OR PHONE_NUMBERS_EQUAL(sms.address, '<phoneNumber>', 1/0));
     */
    private Cursor getMessagesByPhoneNumber(
            String phoneNumber, String[] projection, String selection,
            String sortOrder) {
        String escapedPhoneNumber = DatabaseUtils.sqlEscapeString(phoneNumber);
        String finalMmsSelection =
                concatSelections(
                        selection,
                        "pdu._id = matching_addresses.address_id");
        String finalSmsSelection =
                concatSelections(
                        selection,
                        "(address=" + escapedPhoneNumber + " OR PHONE_NUMBERS_EQUAL(address, " +
                        escapedPhoneNumber +
                        (mUseStrictPhoneNumberComparation ? ", 1))" : ", 0))"));
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(
                MmsProvider.TABLE_PDU +
                ", (SELECT _id AS address_id " +
                "FROM addr WHERE (address=" + escapedPhoneNumber +
                " OR PHONE_NUMBERS_EQUAL(addr.address, " +
                escapedPhoneNumber +
                (mUseStrictPhoneNumberComparation ? ", 1))) " : ", 0))) ") +
                "AS matching_addresses");
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String[] columns = handleNullMessageProjection(projection);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, MMS_COLUMNS,
                0, "mms", finalMmsSelection, null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, SMS_COLUMNS,
                0, "sms", finalSmsSelection, null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the conversation of certain thread ID.
     */
    private Cursor getConversationById(
            String threadIdString, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String extraSelection = "_id=" + threadIdString;
        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String[] columns = handleNullThreadsProjection(projection);

        queryBuilder.setDistinct(true);
        queryBuilder.setTables("threads");
        return queryBuilder.query(
                mOpenHelper.getReadableDatabase(), columns, finalSelection,
                selectionArgs, sortOrder, null, null);
    }

    private static String joinPduAndPendingMsgTables() {
        return MmsProvider.TABLE_PDU + " LEFT JOIN " + TABLE_PENDING_MSG
                + " ON pdu._id = pending_msgs.msg_id";
    }

    private static String[] createMmsProjection(String[] old) {
        String[] newProjection = new String[old.length];
        for (int i = 0; i < old.length; i++) {
            if (old[i].equals(BaseColumns._ID)) {
                newProjection[i] = "pdu._id";
            } else {
                newProjection[i] = old[i];
            }
        }
        return newProjection;
    }

    private Cursor getUndeliveredMessages(
            String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String[] mmsProjection = createMmsProjection(projection);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables());
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);

        String finalMmsSelection = concatSelections(
                selection, Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_OUTBOX);
        String finalSmsSelection = concatSelections(
                selection, "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_FAILED
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_QUEUED + ")");

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                smsColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add("pdu._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 1, "mms", finalMmsSelection,
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms", finalSmsSelection,
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date to the list of columns for an inner
     * projection.
     */
    private static String[] makeProjectionWithNormalizedDate(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 1];

        result[0] = "date * " + dateMultiple + " AS normalized_date";
        System.arraycopy(projection, 0, result, 1, projectionSize);
        return result;
    }

    private static String buildConversationQuery(String[] projection,
            String selection, String sortOrder) {
        String[] mmsProjection = createMmsProjection(projection);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder cbQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        cbQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables());
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS);
        cbQueryBuilder.setTables("cellbroadcast");

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] cbColumns = handleNullMessageProjection(projection);
        String[] innerMmsProjection = makeProjectionWithNormalizedDate(mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithNormalizedDate(smsColumns, 1);
        String[] innerCbProjection = makeProjectionWithNormalizedDate(cbColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add("pdu._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);

        String mmsSelection = concatSelections(selection,
                                Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 0, "mms",
                concatSelections(mmsSelection, MMS_CONVERSATION_CONSTRAINT),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection, SMS_COLUMNS,
                0, "sms", concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                null, null);
        String cbSubQuery = cbQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerCbProjection, CB_COLUMNS,
                0, "cellbroadcast", selection, null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery, cbSubQuery },
                handleNullSortOrder(sortOrder), null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        return outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);
    }

    @Override
    public String getType(Uri uri) {
        return VND_ANDROID_DIR_MMS_SMS;
    }

    @Override
    public int delete(Uri uri, String selection,
            String[] selectionArgs) {
    	Xlog.d(LOG_TAG, "delete uri = " + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Context context = getContext();
        int affectedRows = 0;

        switch(URI_MATCHER.match(uri)) {
            case URI_CONVERSATIONS_MESSAGES:
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                affectedRows = deleteConversation(db, uri, selection, selectionArgs);
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    Xlog.i(WAPPUSH_TAG,"delete Thread"+threadId);
                    affectedRows += context.getContentResolver().delete(ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD,threadId),selection,selectionArgs);
                }
                MmsSmsDatabaseHelper.updateThread(db, threadId);
                break;
            case URI_CONVERSATIONS:
                Xlog.d(LOG_TAG, "delete URI_CONVERSATIONS begin");
                affectedRows = deleteAllConversation(db, uri, selection, selectionArgs);
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    affectedRows += context.getContentResolver().delete(WapPush.CONTENT_URI,selection,selectionArgs);
                }
                Xlog.d(LOG_TAG, "delete URI_CONVERSATIONS end");
                MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
                break;
            case URI_OBSOLETE_THREADS:
                String delSelectionString = "_id NOT IN (SELECT DISTINCT thread_id FROM sms " 
                    + "UNION SELECT DISTINCT thread_id FROM cellbroadcast "
                    + "UNION SELECT DISTINCT thread_id FROM pdu) AND (status <> 1)";
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    delSelectionString = "_id NOT IN (SELECT DISTINCT thread_id FROM sms "
                    + "UNION SELECT DISTINCT thread_id FROM cellbroadcast "
                    + "UNION SELECT DISTINCT thread_id FROM pdu UNION SELECT DISTINCT thread_id FROM wappush) AND (status <> 1)";
                }
                affectedRows = db.delete("threads", delSelectionString, null);
                affectedRows += db.delete("threads",
                                        "recipient_ids = \"\"", null);
                break;
            case URI_QUICK_TEXT: 
                affectedRows = db.delete(TABLE_QUICK_TEXT, selection, selectionArgs);
                break;
                
            case URI_CELLBROADCAST: 
                affectedRows = db.delete(TABLE_CELLBROADCAST, selection, selectionArgs);
                break;
            case URI_FOLDER_DELETE_ALL: //3
                affectedRows = deleteAllInFolderMode(uri, db, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES);
        }

        if (affectedRows > 0) {
            notifyChange();
        }
    	Xlog.d(LOG_TAG, "delete end");
        return affectedRows;
    }
        
    private int deleteAllInFolderMode(Uri uri, SQLiteDatabase db, String selection,
            String[] selectionArgs){
        int boxType = Integer.parseInt(uri.getLastPathSegment());
        String smsWhere = "type=" + boxType;
        if (boxType == 4) {
            smsWhere = "(type=4 or type=5 or type=6)";
        }
        String mmsWhere = "msg_box=" + boxType;
        smsWhere = concatSelections(smsWhere, selection);
        mmsWhere = concatSelections(mmsWhere, selection);

        Cursor cursor = null;
        int smsId = 0;
        int mmsId = 0;
        int cbId = 0;
        int wappushId = 0;
        cursor = db.query("sms", new String[] {"max(_id)"},
                null, null, null, null, null, null);
        if (cursor != null) { 
        	try {
        		if (cursor.moveToFirst()){
        			smsId = cursor.getInt(0);
        			Xlog.d(LOG_TAG, "deleteAllInFolderMode max SMS id = " + smsId);
        		}
        	} finally {
        		cursor.close();
        		cursor = null;
        	}
        }
        cursor = db.query("pdu", new String[] {"max(_id)"},
                null, null, null, null, null, null);
        if (cursor != null) { 
        	try {
        		if (cursor.moveToFirst()){
        			mmsId = cursor.getInt(0);
        			Xlog.d(LOG_TAG, "deleteAllInFolderMode max MMS id = " + smsId);
        		}
        	} finally {
        		cursor.close();
        		cursor = null;
        	}
        }
        cursor = db.query("cellbroadcast", new String[] {"max(_id)"},
                null, null, null, null, null, null);
        if (cursor != null) { 
        	try {
        		if (cursor.moveToFirst()){
        			cbId = cursor.getInt(0);
        			Xlog.d(LOG_TAG, "deleteAllInFolderMode max CB id = " + cbId);
        		}
        	} finally {
        		cursor.close();
        		cursor = null;
        	}
        }
        if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
            cursor = db.query("wappush", new String[] {"max(_id)"},
                    null, null, null, null, null, null);
            if (cursor != null) { 
            	try {
            		if (cursor.moveToFirst()){
            			wappushId = cursor.getInt(0);
            			Xlog.d(LOG_TAG, "confirmDeleteThreadDialog max WAPPUSH id = " + wappushId);
            		}
            	} finally {
            		cursor.close();
            		cursor = null;
            	}
            }
        }

        smsWhere = concatSelections(smsWhere, "_id<=" + smsId);
        mmsWhere = concatSelections(mmsWhere, "_id<=" + mmsId);

        int affectedRows = MmsProvider.deleteMessages(getContext(), db, mmsWhere, selectionArgs, uri)
                + db.delete("sms", smsWhere, selectionArgs);
        if (boxType == 1) {//inbox
            String cbWhere = concatSelections(selection, "_id<=" + cbId);
            affectedRows += db.delete("cellbroadcast", cbWhere, selectionArgs);
            if (FeatureOption.MTK_WAPPUSH_SUPPORT) {
                String wappushWhere = concatSelections(selection, "_id<=" + wappushId);
                affectedRows += db.delete("wappush", wappushWhere, selectionArgs);
            }
        }
        MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
        return affectedRows;
    }

    private void notifyChange() {
        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null);
        notifyUnreadMessageNumberChanged(getContext());
    }
    private static int getUnreadMessageNumber(Context context){
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), 
                Uri.parse("content://mms-sms/unread_count"), null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()){
                    int count = cursor.getInt(0);
                    Xlog.d(LOG_TAG, "unread message count: " + count);
                    return count;

                }
            } finally {
                cursor.close();
            }
        } else {
            Xlog.d(LOG_TAG, "can not get unread message count.");
        }
        return 0;
    }

    private static void broadcastUnreadMessageNumber(Context context, int unreadMsgNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.MTK_ACTION_UNREAD_CHANGED);
        intent.putExtra(Intent.MTK_EXTRA_UNREAD_NUMBER, unreadMsgNumber);
        intent.putExtra(Intent.MTK_EXTRA_UNREAD_COMPONENT, 
                new ComponentName("com.android.mms", "com.android.mms.ui.BootActivity"));
        context.sendBroadcast(intent);
    }

    private static void recordUnreadMessageNumberToSys(Context context, int unreadMsgNumber) {
        android.provider.Settings.System.putInt(context.getContentResolver(), 
                "com_android_mms_mtk_unread", unreadMsgNumber);
    }

    public static void notifyUnreadMessageNumberChanged(Context context) {
        int unreadNumber = getUnreadMessageNumber(context);
        recordUnreadMessageNumberToSys(context, unreadNumber);
        broadcastUnreadMessageNumber(context, unreadNumber);
    }
    /**
     * Delete all the conversation
     */
    private int deleteAllConversation(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
        String smsId = uri.getQueryParameter("smsId");
        String mmsId = uri.getQueryParameter("mmsId");
        Xlog.d(LOG_TAG, "deleteAllConversation get max message smsId = " + smsId + " mmsId =" + mmsId);
        String finalSmsSelection;
        String finalMmsSelection;
        if (smsId != null){
            finalSmsSelection = concatSelections(selection, "_id<=" + smsId);
            db.execSQL("DELETE FROM words WHERE table_to_use=1 AND source_id<" + smsId + ";");
        } else {
            finalSmsSelection = selection;
            db.execSQL("DELETE FROM words;");
        }
        if (mmsId != null){
            finalMmsSelection = concatSelections(selection, "_id<=" + mmsId);
            db.execSQL("DELETE FROM words WHERE table_to_use=2 AND source_id<" + mmsId + ";");
        } else {
            finalMmsSelection = selection;
            db.execSQL("DELETE FROM words ;");
        }
         
        return MmsProvider.deleteMessages(getContext(), db, finalMmsSelection, selectionArgs, uri)
                + db.delete("sms", finalSmsSelection, selectionArgs)
                + db.delete("cellbroadcast", selection, selectionArgs);
    }
    

    /**
     * Delete the conversation with the given thread ID.
     */
    private int deleteConversation(SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
        String threadId = uri.getLastPathSegment();
        String finalSelection = concatSelections(selection, "thread_id = " + threadId);
        String smsId = uri.getQueryParameter("smsId");
        String mmsId = uri.getQueryParameter("mmsId");
        Xlog.d(LOG_TAG, "deleteConversation get max message smsId = " + smsId + " mmsId =" + mmsId);
        String finalSmsSelection;
        String finalMmsSelection;
        if (smsId != null){
            finalSmsSelection = concatSelections(finalSelection, "_id<=" + smsId);
        } else {
            finalSmsSelection = finalSelection;
        }
        if (mmsId != null){
            finalMmsSelection = concatSelections(finalSelection, "_id<=" + mmsId);
        } else {
            finalMmsSelection = finalSelection;
        }
         
        return MmsProvider.deleteMessages(getContext(), db, finalMmsSelection, selectionArgs, uri)
                + db.delete("sms", finalSmsSelection, selectionArgs)
                + db.delete("cellbroadcast", finalSelection, selectionArgs);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_QUICK_TEXT:
                db.insertOrThrow("quicktext", null, values);
                return uri;

            case URI_PENDING_MSG:
                Xlog.d(LOG_TAG, "insert pending_msgs");
                long id = db.insertOrThrow(TABLE_PENDING_MSG, null, values);
                Uri res = Uri.parse(uri + "/" + id);
                Xlog.d(LOG_TAG, "res=" + res);
                return res;
                
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES);
        }
    }
 
    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int affectedRows = 0;
        Xlog.d(LOG_TAG, "update URI is " + uri);
        switch(URI_MATCHER.match(uri)) {
            case URI_CONVERSATIONS_MESSAGES:
                String threadIdString = uri.getPathSegments().get(1);
                affectedRows = updateConversation(threadIdString, values,
                        selection, selectionArgs);
                break;

            case URI_PENDING_MSG:
                affectedRows = db.update(TABLE_PENDING_MSG, values, selection, null);
                break;

            case URI_CANONICAL_ADDRESS: {
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;

                affectedRows = db.update(TABLE_CANONICAL_ADDRESSES, values, finalSelection, null);
                break;
            }
            case URI_QUICK_TEXT: 
                affectedRows = db.update(TABLE_QUICK_TEXT, values, 
                        selection, selectionArgs);
                break;
            case URI_STATUS:{
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                    Xlog.d(LOG_TAG, "update URI_STATUS Thread ID is " + threadId);
                } catch (NumberFormatException e) {
                    Xlog.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }

                affectedRows = db.update(TABLE_THREADS, values, "_id = " + Long.toString(threadId), null);
                Xlog.d(LOG_TAG, "update URI_STATUS ok");
                break;
            }

            default:
                throw new UnsupportedOperationException(
                        NO_DELETES_INSERTS_OR_UPDATES);
        }

        if (affectedRows > 0) {
            notifyChange();
        }
    	Xlog.d(LOG_TAG, "update end ");
        return affectedRows;
    }

    private int updateConversation(
            String threadIdString, ContentValues values, String selection,
            String[] selectionArgs) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return 0;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalSelection = concatSelections(selection, "thread_id=" + threadIdString);
        return db.update(MmsProvider.TABLE_PDU, values, finalSelection, selectionArgs)
                + db.update("sms", values, finalSelection, selectionArgs)
                        + db.update("cellbroadcast", values, finalSelection, selectionArgs);
    }

    /**
     * Construct Sets of Strings containing exactly the columns
     * present in each table.  We will use this when constructing
     * UNION queries across the MMS and SMS tables.
     */
    private static void initializeColumnSets() {
        int commonColumnCount = MMS_SMS_COLUMNS.length;
        int mmsOnlyColumnCount = MMS_ONLY_COLUMNS.length;
        int smsOnlyColumnCount = SMS_ONLY_COLUMNS.length;
        int cbOnlyColumnCount = CB_ONLY_COLUMNS.length;
        Set<String> unionColumns = new HashSet<String>();

        for (int i = 0; i < commonColumnCount; i++) {
            MMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            SMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            CB_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            unionColumns.add(MMS_SMS_COLUMNS[i]);
        }
        for (int i = 0; i < mmsOnlyColumnCount; i++) {
            MMS_COLUMNS.add(MMS_ONLY_COLUMNS[i]);
            unionColumns.add(MMS_ONLY_COLUMNS[i]);
        }
        for (int i = 0; i < smsOnlyColumnCount; i++) {
            SMS_COLUMNS.add(SMS_ONLY_COLUMNS[i]);
            unionColumns.add(SMS_ONLY_COLUMNS[i]);
        }
        for (int i = 0; i < cbOnlyColumnCount; i++) {
            CB_COLUMNS.add(CB_ONLY_COLUMNS[i]);
            //unionColumns.add(CB_ONLY_COLUMNS[i]);
        }
        int i = 0;
        for (String columnName : unionColumns) {
            UNION_COLUMNS[i++] = columnName;
        }
    }
}
