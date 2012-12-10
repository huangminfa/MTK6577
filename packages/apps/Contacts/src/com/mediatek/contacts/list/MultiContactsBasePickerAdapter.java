
package com.mediatek.contacts.list;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.SearchSnippetColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.contacts.R;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.DefaultContactListAdapter;
import com.android.contacts.list.ProfileAndContactsLoader;
import com.android.contacts.model.AccountType;

public class MultiContactsBasePickerAdapter extends ContactListAdapter {

    public static final char SNIPPET_START_MATCH = '\u0001';
    public static final char SNIPPET_END_MATCH = '\u0001';
    public static final String SNIPPET_ELLIPSIS = "\u2026";
    public static final int SNIPPET_MAX_TOKENS = 5;

    public static final String SNIPPET_ARGS = SNIPPET_START_MATCH + "," + SNIPPET_END_MATCH + ","
            + SNIPPET_ELLIPSIS + "," + SNIPPET_MAX_TOKENS;

    public static final int FILTER_ACCOUNT_WITH_PHONE_NUMBER_ONLY = 100;
    public static final int FILTER_ACCOUNT_WITH_PHONE_NUMBER_OR_EMAIL = 101;

    private ListView mListView = null;
    private CursorLoader mLoader = null;

    private int mFilterAccountOptions = 0;

    public MultiContactsBasePickerAdapter(Context context, ListView lv) {
        super(context);
        mListView = lv;
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position,
            ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(context.getText(R.string.missing_name));
        view.setQuickContactEnabled(isQuickContactEnabled());

        // Enable check box
        view.setCheckable(true);

        // For using list-view's check states
        view.setActivatedStateSupported(true);

        return view;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        final ContactListItemView view = (ContactListItemView) itemView;

        view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString() : null);

        if (isSelectionVisible()) {
            view.setActivated(isSelectedContact(partition, cursor));
        }

        bindSectionHeaderAndDivider(view, position, cursor);

        if (isQuickContactEnabled()) {
            bindQuickContact(view, partition, cursor, ContactQuery.CONTACT_PHOTO_ID,
                    ContactQuery.CONTACT_ID, ContactQuery.CONTACT_LOOKUP_KEY);
        } else {
            bindPhoto(view, partition, cursor);
        }

        bindName(view, cursor);
        bindPresenceAndStatusMessage(view, cursor);

        if (isSearchMode()) {
            bindSearchSnippet(view, cursor);
        } else {
            view.setSnippet(null);
        }

        Log.d("MultiContactsBasePickerAdapter", "bind view position = " + position
                + " check state = " + mListView.isItemChecked(position));
        view.getCheckBox().setChecked(mListView.isItemChecked(position));
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {
        mLoader = loader;

        if (loader instanceof ProfileAndContactsLoader) {
            ((ProfileAndContactsLoader) loader).setLoadProfile(shouldIncludeProfile());
        }

        ContactListFilter filter = getFilter();
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
                loader.setProjection(getProjection(true));
                configureSelection(loader, directoryId, filter);
            }
        } else {
            configureUri(loader, directoryId, filter);
            loader.setProjection(getProjection(false));
            configureSelection(loader, directoryId, filter);
        }

        String sortOrder;
        if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            sortOrder = Contacts.SORT_KEY_PRIMARY;
        } else {
            sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
        }

        loader.setSortOrder(sortOrder);
    }

    protected void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;

        if (directoryId == Directory.DEFAULT && isSectionHeaderDisplayEnabled()) {
            uri = buildSectionIndexerUri(uri);
        }

        // The "All accounts" filter is the same as the entire contents of
        // Directory.DEFAULT
        if (filter != null && filter.filterType != ContactListFilter.FILTER_TYPE_CUSTOM
                && filter.filterType != ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
            uri = uri.buildUpon().appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(Directory.DEFAULT)).build();
        }

        loader.setUri(uri);
    }

    private void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter == null) {
            return;
        }

        if (directoryId != Directory.DEFAULT) {
            return;
        }

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        switch (filter.filterType) {
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
                // We have already added directory=0 to the URI, which takes
                // care of this
                // filter
            	if ("delete".equals(this.mTempFlag)) {
                    selection.append(RawContacts.IS_SDN_CONTACT + " = 0");
            	}
                break;
            }
            case ContactListFilter.FILTER_TYPE_STARRED: {
                selection.append(Contacts.STARRED + "!=0");
                break;
            }
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
                selection.append(Contacts.HAS_PHONE_NUMBER + "=1");
                if ("delete".equals(this.mTempFlag)) {
                    selection.append(" AND " + RawContacts.IS_SDN_CONTACT + " = 0");
                }
                break;
            }
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
                selection.append(Contacts.IN_VISIBLE_GROUP + "=1");
                if ("delete".equals(this.mTempFlag)) {
                    selection.append(" AND " + RawContacts.IS_SDN_CONTACT + " = 0");
                }
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                // TODO: avoid the use of private API
                if (AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(filter.accountType)) {
                	final String q1 = "EXISTS ("
                                    + "SELECT DISTINCT " + RawContacts.CONTACT_ID
                                    + " FROM raw_contacts"
                                    + " WHERE ( ";                	
                    final String q2 = RawContacts.CONTACT_ID + " = " + "view_contacts."+ Contacts._ID
                                    + " AND (" + RawContacts.ACCOUNT_TYPE + " IS NULL "
                                    + " AND " + RawContacts.ACCOUNT_NAME + " IS NULL "
                                    + " AND " +  RawContacts.DATA_SET + " IS NULL "
                                    + " OR " + RawContacts.ACCOUNT_TYPE + "=? "
                                    + " AND " + RawContacts.ACCOUNT_NAME + "=? ";                	
                	String sql;
                	if ("delete".equals(this.mTempFlag)) {
                		sql = q1 + RawContacts.IS_SDN_CONTACT +  " = 0 AND " + q2;
                    } else {
                    	sql = q1 + q2;
                    }                	
                    selection.append(sql);
                                    
                } else {                	
                	final String q1 = "EXISTS ("
                                    + "SELECT DISTINCT " + RawContacts.CONTACT_ID
                                    + " FROM raw_contacts"
                                    + " WHERE ( ";    	
                    final String q2 = RawContacts.CONTACT_ID + " = " + "view_contacts."+ Contacts._ID
                                    + " AND (" + RawContacts.ACCOUNT_TYPE + "=?"
                                    + " AND " + RawContacts.ACCOUNT_NAME + "=?";                	
                    String sql;
                	if ("delete".equals(this.mTempFlag)) {
                		sql = q1 + RawContacts.IS_SDN_CONTACT +  " = 0 AND " + q2;
                    } else {
                    	sql = q1 + q2;
                    }                    
                    selection.append(sql);
                }
                selectionArgs.add(filter.accountType);
                selectionArgs.add(filter.accountName);
                if (filter.dataSet != null) {
                    selection.append(" AND " + RawContacts.DATA_SET + "=? )");
                    selectionArgs.add(filter.dataSet);
                } else {
                    selection.append(" AND " +  RawContacts.DATA_SET + " IS NULL )");
                }
                selection.append("))");
                break;
            }
        }

        if (mFilterAccountOptions == FILTER_ACCOUNT_WITH_PHONE_NUMBER_ONLY) {
            selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
        } else if (mFilterAccountOptions == FILTER_ACCOUNT_WITH_PHONE_NUMBER_OR_EMAIL) {
            // TODO : to filter phone number or email
            selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }

    public void setFilterAccountOption(int filterAccountOptions) {
        mFilterAccountOptions = filterAccountOptions;
    }

    public boolean isSimContact(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return false;
        }
        return (cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM)) > 0);
    }

    public int getSimIndex(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDEX_IN_SIM));
    }

    public int getContactID(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
    }

    public int getContactIndicator(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
    }

    public void setDataSetChangedNotifyEnable(boolean enable) {
        if (mLoader != null) {
            if (enable) {
                mLoader.startLoading();
            } else {
                mLoader.stopLoading();
            }
        }
    }

    public boolean hasStableIds() {
        return false;
    }

    public long getItemId(int position) {
        return getContactID(position);
    }

    public int getMyCheckedItemCount(ArrayList<Long> checkedItemsList, Cursor cursor) {

        int checkItemsCount = 0;
        long contactId = -1;

        if (checkedItemsList == null) {
            checkedItemsList = new ArrayList<Long>();
            return checkItemsCount;
        }
        if (checkedItemsList.size() == 0) {
            return checkItemsCount;
        } else {
            cursor.moveToPosition(-1);
            int count = cursor.getCount();
            while (cursor.moveToNext()) {
                contactId = -1;
                contactId = cursor.getInt(ContactQuery.CONTACT_ID);
                if (checkedItemsList.contains(contactId)) {
                    ++checkItemsCount;
                }
            }
            cursor.moveToPosition(-1);
        }

        return checkItemsCount;
    }
}
