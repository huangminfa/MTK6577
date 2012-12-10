/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.exchange.provider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.provider.EmailContent.MailboxColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MailboxUtilities {
    public static final String WHERE_PARENT_KEY_UNINITIALIZED =
        "(" + MailboxColumns.PARENT_KEY + " isnull OR " + MailboxColumns.PARENT_KEY + "=" +
        Mailbox.PARENT_KEY_UNINITIALIZED + ")";
    // The flag we use in Account to indicate a mailbox change in progress
    private static final int ACCOUNT_MAILBOX_CHANGE_FLAG = Account.FLAGS_SYNC_ADAPTER;

    /*
     * Set the parent key and flags of mailbox.
     */
    synchronized public static void setParentKeyAndFlags(Context context, long accountId) {
        ArrayList<TemporaryMailbox> temporaryMailboxs = new ArrayList<TemporaryMailbox>();
        HashSet<String> parentServerIdSet = new HashSet<String>();
        HashMap<String, Long> serverIdMap = new HashMap<String, Long>();
        String accountSelector = Mailbox.ACCOUNT_KEY + "=" + accountId;
        String noParentKeySelection = WHERE_PARENT_KEY_UNINITIALIZED + " AND " + accountSelector;
        ContentResolver resolver = context.getContentResolver();
        // Select all mailboxes which 'parentkey' is null or 0.
        Cursor c = resolver.query(Mailbox.CONTENT_URI, Mailbox.CONTENT_PROJECTION,
                noParentKeySelection, null, null);
        if (c == null) return;
        
        try {
            while (c.moveToNext()) {
                long contentId =  c.getLong(Mailbox.CONTENT_ID_COLUMN);
                String serverId = c.getString(Mailbox.CONTENT_SERVER_ID_COLUMN);
                String parentServerId = c.getString(Mailbox.CONTENT_PARENT_SERVER_ID_COLUMN);
                if (serverId != null && !serverIdMap.containsKey(serverId)) {
                    serverIdMap.put(serverId, contentId);
                }
                if (parentServerId != null && !parentServerIdSet.contains(parentServerId)) {
                    parentServerIdSet.add(parentServerId);
                }
                temporaryMailboxs.add(new TemporaryMailbox(
                        contentId, serverId, parentServerId,
                        c.getLong(Mailbox.CONTENT_PARENT_KEY_COLUMN), 
                        c.getInt(Mailbox.CONTENT_TYPE_COLUMN),
                        c.getInt(Mailbox.CONTENT_FLAGS_COLUMN)));
            }
        } finally {
            c.close();
        }
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues cv = new ContentValues();
        int commitCount = 0;
        if (temporaryMailboxs.size() > 0) {
            for (TemporaryMailbox temporaryMailbox : temporaryMailboxs) {
                long id = temporaryMailbox.mId;
                String psId = temporaryMailbox.mParentServerId;
                int type = temporaryMailbox.mType;
                if (psId != null && serverIdMap.get(psId) != null) {
                    temporaryMailbox.mParentKey = serverIdMap.get(psId);
                }
                if (type <= Mailbox.TYPE_NOT_EMAIL) {
                    temporaryMailbox.mFlags |= Mailbox.FLAG_HOLDS_MAIL;
                }
                if (type == Mailbox.TYPE_MAIL || type == Mailbox.TYPE_TRASH ||
                        type == Mailbox.TYPE_JUNK || type == Mailbox.TYPE_INBOX) {
                    temporaryMailbox.mFlags |= Mailbox.FLAG_ACCEPTS_MOVED_MAIL;
                }
                if (parentServerIdSet.contains(temporaryMailbox.mServerId)) {
                    temporaryMailbox.mFlags |= Mailbox.FLAG_HAS_CHILDREN | Mailbox.FLAG_CHILDREN_VISIBLE;
                }
                if (temporaryMailbox.mParentKey == 0) {
                    temporaryMailbox.mParentKey = -1;
                }
                cv.clear();
                cv.put(MailboxColumns.PARENT_KEY, temporaryMailbox.mParentKey);
                cv.put(MailboxColumns.FLAGS, temporaryMailbox.mFlags);
                ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(Mailbox.CONTENT_URI, id))
                        .withValues(cv).build());
                if (++commitCount == 50) {
                    updateMailbox(resolver, ops);
                    commitCount = 0;
                    ops.clear();
                }
            }
            if (ops.size() > 0) {
                updateMailbox(resolver, ops);
            }
        }
    }
    
    public static void updateMailbox(ContentResolver resolver, ArrayList<ContentProviderOperation> ops) {
        try {
            resolver.applyBatch(EmailContent.AUTHORITY, ops);
        } catch (RemoteException e) {
            Logging.d("RemoteException in updateMailbox parentKey and flags");
        } catch (OperationApplicationException e) {
            Logging.d("OperationApplicationException in commitMailboxes");
        }
    }
    
    /**
     * Recalculate a mailbox's flags and the parent key of any children
     * @param context the caller's context
     * @param parentCursor a cursor to a mailbox that requires fixup
     */
    public static void setFlagsAndChildrensParentKey(Context context, Cursor parentCursor,
            String accountSelector) {
        ContentResolver resolver = context.getContentResolver();
        String[] selectionArgs = new String[1];
        ContentValues parentValues = new ContentValues();
        // Get the data we need first
        long parentId = parentCursor.getLong(Mailbox.CONTENT_ID_COLUMN);
        int parentFlags = 0;
        int parentType = parentCursor.getInt(Mailbox.CONTENT_TYPE_COLUMN);
        String parentServerId = parentCursor.getString(Mailbox.CONTENT_SERVER_ID_COLUMN);
        // All email-type boxes hold mail
        if (parentType <= Mailbox.TYPE_NOT_EMAIL) {
            parentFlags |= Mailbox.FLAG_HOLDS_MAIL;
        }
        // Outbox, Drafts, and Sent don't allow mail to be moved to them
        if (parentType == Mailbox.TYPE_MAIL || parentType == Mailbox.TYPE_TRASH ||
                parentType == Mailbox.TYPE_JUNK || parentType == Mailbox.TYPE_INBOX) {
            parentFlags |= Mailbox.FLAG_ACCEPTS_MOVED_MAIL;
        }
        // There's no concept of "append" in EAS so FLAG_ACCEPTS_APPENDED_MAIL is never used
        // Mark parent mailboxes as parents & add parent key to children
        // An example of a mailbox with a null serverId would be an Outbox that we create locally
        // for hotmail accounts (which don't have a server-based Outbox)
        if (parentServerId != null) {
            selectionArgs[0] = parentServerId;
            Cursor childCursor = resolver.query(Mailbox.CONTENT_URI,
                    Mailbox.ID_PROJECTION, MailboxColumns.PARENT_SERVER_ID + "=? AND " +
                    accountSelector, selectionArgs, null);
            if (childCursor == null) return;
            try {
                while (childCursor.moveToNext()) {
                    parentFlags |= Mailbox.FLAG_HAS_CHILDREN | Mailbox.FLAG_CHILDREN_VISIBLE;
                    ContentValues childValues = new ContentValues();
                    childValues.put(Mailbox.PARENT_KEY, parentId);
                    long childId = childCursor.getLong(Mailbox.ID_PROJECTION_COLUMN);
                    resolver.update(ContentUris.withAppendedId(Mailbox.CONTENT_URI, childId),
                            childValues, null, null);
                }
            } finally {
                childCursor.close();
            }
        } else {
            // Mark this is having no parent, so that we don't examine this mailbox again
            parentValues.put(Mailbox.PARENT_KEY, Mailbox.NO_MAILBOX);
            Log.w(Logging.LOG_TAG, "Mailbox with null serverId: " +
                    parentCursor.getString(Mailbox.CONTENT_DISPLAY_NAME_COLUMN) + ", type: " +
                    parentType);
        }
        // Save away updated flags and parent key (if any)
        parentValues.put(Mailbox.FLAGS, parentFlags);
        resolver.update(ContentUris.withAppendedId(Mailbox.CONTENT_URI, parentId),
                parentValues, null, null);
    }

    /**
     * Recalculate a mailbox's flags and the parent key of any children
     * @param context the caller's context
     * @param accountSelector (see description below in fixupUninitializedParentKeys)
     * @param serverId the server id of an individual mailbox
     */
    public static void setFlagsAndChildrensParentKey(Context context, String accountSelector,
            String serverId) {
        Cursor cursor = context.getContentResolver().query(Mailbox.CONTENT_URI,
                Mailbox.CONTENT_PROJECTION, MailboxColumns.SERVER_ID + "=? AND " + accountSelector,
                new String[] {serverId}, null);
        if (cursor == null) return;
        try {
            if (cursor.moveToFirst()) {
                setFlagsAndChildrensParentKey(context, cursor, accountSelector);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Given an account selector, specifying the account(s) on which to work, create the parentKey
     * and flags for each mailbox in the account(s) that is uninitialized (parentKey = 0 or null)
     *
     * @param accountSelector a sqlite WHERE clause expression to be used in determining the
     * mailboxes to be acted upon, e.g. accountKey IN (1, 2), accountKey = 12, etc.
     */
    public static void fixupUninitializedParentKeys(Context context, String accountSelector) {
        // Sanity check first on our arguments
        if (accountSelector == null) throw new IllegalArgumentException();
        // The selection we'll use to find uninitialized parent key mailboxes
        String noParentKeySelection = WHERE_PARENT_KEY_UNINITIALIZED + " AND " + accountSelector;

        // We'll loop through mailboxes with an uninitialized parent key
        ContentResolver resolver = context.getContentResolver();
        Cursor parentCursor = resolver.query(Mailbox.CONTENT_URI, Mailbox.CONTENT_PROJECTION,
                noParentKeySelection, null, null);
        if (parentCursor == null) return;
        try {
            while (parentCursor.moveToNext()) {
                setFlagsAndChildrensParentKey(context, parentCursor, accountSelector);
                // Fix up the parent as well
                String parentServerId =
                        parentCursor.getString(Mailbox.CONTENT_PARENT_SERVER_ID_COLUMN);
                if (parentServerId != null) {
                    setFlagsAndChildrensParentKey(context, accountSelector, parentServerId);
                }
            }
        } finally {
            parentCursor.close();
        }

        // Any mailboxes without a parent key should have parentKey set to -1 (no parent)
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Mailbox.PARENT_KEY, Mailbox.NO_MAILBOX);
        resolver.update(Mailbox.CONTENT_URI, values, noParentKeySelection, null);
     }

    private static void setAccountSyncAdapterFlag(Context context, long accountId, boolean start) {
        Account account = Account.restoreAccountWithId(context, accountId);
        if (account == null) return;
        // Set temporary flag indicating state of update of mailbox list
        ContentValues cv = new ContentValues();
        cv.put(Account.FLAGS, start ? (account.mFlags | ACCOUNT_MAILBOX_CHANGE_FLAG) :
            account.mFlags & ~ACCOUNT_MAILBOX_CHANGE_FLAG);
        context.getContentResolver().update(
                ContentUris.withAppendedId(Account.CONTENT_URI, account.mId), cv, null, null);
    }

    /**
     * Indicate that the specified account is starting the process of changing its mailbox list
     * @param context the caller's context
     * @param accountId the account that is starting to change its mailbox list
     */
    public static void startMailboxChanges(Context context, long accountId) {
        setAccountSyncAdapterFlag(context, accountId, true);
    }

    /**
     * Indicate that the specified account is ending the process of changing its mailbox list
     * @param context the caller's context
     * @param accountId the account that is finished with changes to its mailbox list
     */
    public static void endMailboxChanges(Context context, long accountId) {
        setAccountSyncAdapterFlag(context, accountId, false);
    }

    /**
     * Check that we didn't leave the account's mailboxes in a (possibly) inconsistent state
     * If we did, make them consistent again
     * @param context the caller's context
     * @param accountId the account whose mailboxes are to be checked
     */
    public static void checkMailboxConsistency(Context context, long accountId) {
        // If our temporary flag is set, we were interrupted during an update
        // First, make sure we're current (really fast w/ caching)
        Account account = Account.restoreAccountWithId(context, accountId);
        if (account == null) return;
        if ((account.mFlags & ACCOUNT_MAILBOX_CHANGE_FLAG) != 0) {
            Log.w(Logging.LOG_TAG, "Account " + account.mDisplayName +
                    " has inconsistent mailbox data; fixing up...");
            // Set all account mailboxes to uninitialized parent key
            ContentValues values = new ContentValues();
            values.put(Mailbox.PARENT_KEY, Mailbox.PARENT_KEY_UNINITIALIZED);
            String accountSelector = Mailbox.ACCOUNT_KEY + "=" + account.mId;
            ContentResolver resolver = context.getContentResolver();
            resolver.update(Mailbox.CONTENT_URI, values, accountSelector, null);
            // Fix up keys and flags
            MailboxUtilities.setParentKeyAndFlags(context, accountId);
            // Clear the temporary flag
            endMailboxChanges(context, accountId);
        }
    }
    
    private static class TemporaryMailbox {
        long mId;
        String mServerId;
        String mParentServerId;
        long mParentKey;
        int mType;
        int mFlags;
        
        public TemporaryMailbox(long id, String serverId, String parentServerId, long parentKey, int type, int flags) {
            mId = id;
            mServerId = serverId;
            mParentServerId = parentServerId;
            mParentKey = parentKey;
            mType = type;
            mFlags = flags;
        }
    }
}
