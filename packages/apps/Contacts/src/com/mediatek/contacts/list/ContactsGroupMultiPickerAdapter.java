
package com.mediatek.contacts.list;

import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.DefaultContactListAdapter;

import android.accounts.Account;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.SearchSnippetColumns;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ContactsGroupMultiPickerAdapter extends MultiContactsBasePickerAdapter {

    public static final char SNIPPET_START_MATCH = '\u0001';
    public static final char SNIPPET_END_MATCH = '\u0001';
    public static final String SNIPPET_ELLIPSIS = "\u2026";
    public static final int SNIPPET_MAX_TOKENS = 5;

    public static final String SNIPPET_ARGS = SNIPPET_START_MATCH + "," + SNIPPET_END_MATCH + ","
            + SNIPPET_ELLIPSIS + "," + SNIPPET_MAX_TOKENS;

    protected static final String[] PROJECTION_CONTACT = new String[] {
            Contacts._ID, // 0
            Contacts.DISPLAY_NAME_PRIMARY, // 1
            Contacts.CONTACT_PRESENCE, // 2
            Contacts.CONTACT_STATUS, // 3
            Contacts.PHOTO_ID, // 4
            Contacts.PHOTO_THUMBNAIL_URI, // 5
            Contacts.LOOKUP_KEY, // 6
            Contacts.IS_USER_PROFILE, // 7
            Contacts.INDICATE_PHONE_SIM, // 8
            Contacts.INDEX_IN_SIM, // 9
            Contacts.IS_SDN_CONTACT,  //10
    };
    
    protected static final String[] PROJECTION_CONTACT_FILTER = new String[] {
        Contacts._ID, // 0
        Contacts.DISPLAY_NAME_PRIMARY, // 1
        Contacts.CONTACT_PRESENCE, // 2
        Contacts.CONTACT_STATUS, // 3
        Contacts.PHOTO_ID, // 4
        Contacts.PHOTO_THUMBNAIL_URI, // 5
        Contacts.LOOKUP_KEY, // 6
        Contacts.IS_USER_PROFILE, // 7
        Contacts.INDICATE_PHONE_SIM, // 8
        Contacts.INDEX_IN_SIM, // 9
        Contacts.IS_SDN_CONTACT,  //10
        SearchSnippetColumns.SNIPPET,           // 11
};

    public static final String CONTACTS_IN_GROUP_SELECT =
        " IN "
                + "(SELECT " + RawContacts.CONTACT_ID
                + " FROM " + "raw_contacts"
                + " WHERE " + "raw_contacts._id" + " IN "
                        + "(SELECT " + "data."+Data.RAW_CONTACT_ID
                        + " FROM " + "data "
                        + "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)"
                        + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                                + "' AND " + GroupMembership.GROUP_ROW_ID + " IN "
                                + "(SELECT " + "groups" + "." + Groups._ID
                                + " FROM " + "groups"
                                + " WHERE " + Groups.DELETED + "=0 AND " + Groups.TITLE + "=?))" +
                        " AND "+ RawContacts.DELETED +"=0 ";
    
    public static final String END_BRACKET = " )";
    
    private String mGroupTitle = null;
    private Account mAccount = null;

    public ContactsGroupMultiPickerAdapter(Context context, ListView lv) {
        super(context, lv);
    }
    
    public void setGroupTitle(String groupTitle) {
        mGroupTitle = groupTitle;
    }
    
    public void setGroupAccount(Account account) {
            mAccount = account;
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {

        if (isSearchMode()) {
            String query = getQueryString();
            if (query == null) {
                query = "";
            }
            query = query.trim();
            if (TextUtils.isEmpty(query)) {
                // Regardless of the directory, we don't want anything returned,
                // so let's just send a "nothing" query to the local directory.
                loader.setUri(Contacts.CONTENT_URI);
                loader.setProjection(getProjection(false));
                loader.setSelection("0");
            } else {
                Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
                builder.appendPath(query); // Builder will encode the query
                builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, String
                        .valueOf(directoryId));
                if (directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE) {
                    builder.appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, String
                            .valueOf(getDirectoryResultLimit()));
                }
                builder.appendQueryParameter(SearchSnippetColumns.SNIPPET_ARGS_PARAM_KEY,
                        SNIPPET_ARGS);
                builder.appendQueryParameter(SearchSnippetColumns.DEFERRED_SNIPPETING_KEY, "1");
                loader.setUri(builder.build());
                loader.setProjection(PROJECTION_CONTACT_FILTER);
                String selection = Contacts._ID + CONTACTS_IN_GROUP_SELECT.replace("?", "'" + mGroupTitle + "'");
                if (mAccount != null) {
                    String accountFilter = " AND raw_contacts." + RawContacts.ACCOUNT_NAME + "='" + mAccount.name
                        + "' AND raw_contacts." + RawContacts.ACCOUNT_TYPE + "='" + mAccount.type + "'";
                    selection += accountFilter;
                } else {
                    String accountFilter = " AND raw_contacts." + RawContacts.ACCOUNT_NAME + " IS NULL "
                        + " AND raw_contacts." + RawContacts.ACCOUNT_TYPE + " IS NULL ";
                    selection += accountFilter;
                }
                selection += END_BRACKET;
                loader.setSelection(selection);
            }
        } else {
            Uri uri = Contacts.CONTENT_URI;
            uri = uri.buildUpon()
            .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
            loader.setUri(uri);
            //loader.setProjection(MultiContactsBasePickerAdapter.PROJECTION_CONTACT);
            loader.setProjection(PROJECTION_CONTACT);
            String selection = Contacts._ID + CONTACTS_IN_GROUP_SELECT.replace("?", "'" + mGroupTitle + "'");
            if (mAccount != null) {
                String accountFilter = " AND raw_contacts." + RawContacts.ACCOUNT_NAME + "='" + mAccount.name
                    + "' AND raw_contacts." + RawContacts.ACCOUNT_TYPE + "='" + mAccount.type + "'";
                selection += accountFilter;
            } else {
                String accountFilter = " AND raw_contacts." + RawContacts.ACCOUNT_NAME + " IS NULL "
                    + " AND raw_contacts." + RawContacts.ACCOUNT_TYPE + " IS NULL ";
                selection += accountFilter;
            }
            selection += END_BRACKET;
            loader.setSelection(selection);
        }

        String sortOrder;
        if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            sortOrder = Contacts.SORT_KEY_PRIMARY;
        } else {
            sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
        }

        loader.setSortOrder(sortOrder);
    }
    
    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        final ContactListItemView view = (ContactListItemView) itemView;
        if (isSearchMode()) {
            view.showSnippet(cursor, 11);
        }
    }
    
    
}
