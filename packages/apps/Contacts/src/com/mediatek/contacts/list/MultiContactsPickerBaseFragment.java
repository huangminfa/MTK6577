
package com.mediatek.contacts.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.R;

import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.LegacyContactListAdapter;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.util.AccountFilterUtil;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.contacts.list.MultiContactsBasePickerAdapter;

public class MultiContactsPickerBaseFragment extends ContactEntryListFragment<ContactListAdapter>
        implements ContactListMultiChoiceListener {

    private static final String TAG = "MultiContactsPickerBaseFragment";

    public static final String FRAGMENT_ARGS = "intent";

    protected static final String resultIntentExtraName = "com.mediatek.contacts.list.pickcontactsresult";

    private static final String KEY_FILTER = "filter";
    private static final String KEY_CHECKEDIDS = "checkedids";
    private static final String KEY_CHECKEDSTATES = "checkedstates";

    private static final int REQUEST_CODE_ACCOUNT_FILTER = 1;

    //private TextView mSelectedItemsView = null;
    private String mSlectedItemsFormater = null;

    // Show account filter settings
    private View mAccountFilterHeader;
    //private TextView mCounterHeaderView;
    private ContactListFilter mFilter;
    private SharedPreferences mPrefs;

    //private ArrayList<Long> mCheckedItemsList = new ArrayList<Long>();
    private HashMap<Long, Boolean> mCheckedItemsMap = new HashMap<Long, Boolean>();

    private String mSearchString;

    private boolean mShowFilterHeader = true;

    private TextView mEmptyView = null;

    private class FilterHeaderClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            AccountFilterUtil.startAccountFilterActivityForResult(
                    MultiContactsPickerBaseFragment.this, REQUEST_CODE_ACCOUNT_FILTER);
        }
    }

    private OnClickListener mFilterHeaderClickListener = new FilterHeaderClickListener();

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.multichoice_contact_list, null);
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);

        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        if (isAccountFilterEnable()) {
            mAccountFilterHeader.setOnClickListener(mFilterHeaderClickListener);
        } else {
            mAccountFilterHeader.setClickable(false);
        }
        //mCounterHeaderView = (TextView) getView().findViewById(R.id.contacts_count);
        checkHeaderViewVisibility();

        mEmptyView = (TextView) getView().findViewById(R.id.contact_list_empty);
        if (mEmptyView != null) {
            mEmptyView.setText(R.string.noContacts);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (isAccountFilterEnable()) {
            restoreFilter();
        }
    }

    private void restoreFilter() {
        mFilter = ContactListFilter.restoreDefaultPreferences(mPrefs);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSlectedItemsFormater = getActivity().getString(R.string.menu_actionbar_selected_items);
        updateSelectedItemsView();
        this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void configureAdapter() {
        super.configureAdapter();
        ContactEntryListAdapter adapter = getAdapter();
        // Display the empty list UI
        adapter.setEmptyListEnabled(true);
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);
        super.setPhotoLoaderEnabled(true);
        super.setSectionHeaderDisplayEnabled(true);
        adapter.setFilter(mFilter);
        adapter.setQueryString(mSearchString);

        if (mAccountFilterHeader != null) {
            final TextView headerTextView = (TextView) mAccountFilterHeader.findViewById(
                    R.id.account_filter_header);
            
            
            //MTK_THEMEMANAGER_APP
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                Resources res = getContext().getResources();
                int textColor = res.getThemeMainColor();
                if (textColor != 0) {
                    headerTextView.setTextColor(textColor);
                }
            }
            //MTK_THEMEMANAGER_APP
            
            if (headerTextView != null) {
                headerTextView.setText(R.string.contact_list_loading);
            }
        }
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        MultiContactsBasePickerAdapter adapter = new MultiContactsBasePickerAdapter(getActivity(),
                getListView());
        adapter.setFilter(ContactListFilter
                .createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);
        return adapter;
    }

    protected void setListFilter(ContactListFilter filter) {
        if (isAccountFilterEnable()) {
            throw new RuntimeException(
                    "The #setListFilter could not be called if #isAccountFilterEnable is true");
        }
        mFilter = filter;
        getAdapter().setFilter(mFilter);
        updateFilterHeaderView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick with adapterView");
        if (getListView().getCheckedItemCount() > 5000) {
            Toast.makeText(getActivity(), R.string.multichoice_contacts_limit, Toast.LENGTH_SHORT).show();
            getListView().setItemChecked(position, false);
            return;
        }
        super.onItemClick(parent, view, position, id);

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) getAdapter();
        mCheckedItemsMap.put(Long.valueOf(id), getListView().isItemChecked(position));

        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    @Override
    protected void onItemClick(int position, long id) {
        Log.d(TAG, "onItemClick");
        return;
    }

    @Override
    public void onClearSelect() {
        updateListCheckBoxeState(false);
    }

    /**
     * Check whether or not to show the account filter
     * 
     * @return true: the UI would to follow the user selected, false: the fixed
     *         account to passed and could not changed In the case, the the
     *         filter would to get by the function of
     *         {@link MultiContactsPickerBaseFragment#setListFilter(ContactListFilter)}
     */
    public boolean isAccountFilterEnable() {
        return true;
    }

    private void checkHeaderViewVisibility() {
//        if (mCounterHeaderView != null) {
//            mCounterHeaderView.setVisibility(isSearchMode() ? View.GONE : View.VISIBLE);
//        }
        updateFilterHeaderView();

        // // Hide the search header by default. See showCount().
        // if (mSearchHeaderView != null) {
        // mSearchHeaderView.setVisibility(View.GONE);
        // }
    }

/*    @Override
    protected void showCount(int partitionIndex, Cursor data) {
        if (!isSearchMode() && data != null) {
            int count = data.getCount();
            if (count != 0) {
                String format = getResources().getQuantityText(R.plurals.listTotalAllContacts,
                        count).toString();
                // Do not count the user profile in the contacts count
                mCounterHeaderView.setText(String.format(format, count));
            } else {
                ContactListFilter filter = mFilter;
                int filterType = filter != null ? filter.filterType
                        : ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS;
                switch (filterType) {
                    case ContactListFilter.FILTER_TYPE_ACCOUNT:
                        mCounterHeaderView.setText(getString(
                                R.string.listTotalAllContactsZeroGroup, filter.accountName));
                        break;
                    case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY:
                        mCounterHeaderView.setText(R.string.listTotalPhoneContactsZero);
                        break;
                    case ContactListFilter.FILTER_TYPE_STARRED:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZeroStarred);
                        break;
                    case ContactListFilter.FILTER_TYPE_CUSTOM:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZeroCustom);
                        break;
                    default:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZero);
                        break;
                }
            }
        } else {
//            ContactListAdapter adapter = getAdapter();
//            if (adapter == null) {
//                return;
//            }
//
//            // In search mode we only display the header if there is nothing
//            // found
//            if (TextUtils.isEmpty(getQueryString()) || !adapter.areAllPartitionsEmpty()) {
//                mSearchHeaderView.setVisibility(View.GONE);
//            } else {
//                TextView textView = (TextView) mSearchHeaderView
//                        .findViewById(R.id.totalContactsText);
//                ProgressBar progress = (ProgressBar) mSearchHeaderView.findViewById(R.id.progress);
//                mSearchHeaderView.setVisibility(View.VISIBLE);
//                if (adapter.isLoading()) {
//                    textView.setText(R.string.search_results_searching);
//                    progress.setVisibility(View.VISIBLE);
//                } else {
//                    textView.setText(R.string.listFoundAllContactsZero);
//                    textView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
//                    progress.setVisibility(View.GONE);
//                }
//            }
        }
    }*/

    @Override
    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);

        if (savedState == null) {
            return;
        }

        mFilter = savedState.getParcelable(KEY_FILTER);

        long ids[] = savedState.getLongArray(KEY_CHECKEDIDS);
        boolean[] states = savedState.getBooleanArray(KEY_CHECKEDSTATES);
        if (mCheckedItemsMap == null) {
            mCheckedItemsMap = new HashMap<Long, Boolean>();
        }
        if (ids.length != states.length) {
            return;
        }
        mCheckedItemsMap.clear();
        int checkedStatesSize = ids.length;
        for (int index = 0; index < checkedStatesSize; ++index) {
            mCheckedItemsMap.put(Long.valueOf(ids[index]), states[index]);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int CheckedItemsCount = mCheckedItemsMap.size();
        long[] checkedIds = new long[CheckedItemsCount];
        int index = 0;
        Set<Long> ids = mCheckedItemsMap.keySet();
        for (Long id : ids) {
            checkedIds[index++] = id;
        }
        outState.putLongArray(KEY_CHECKEDIDS, checkedIds);

        boolean[] checkedStates = new boolean[CheckedItemsCount];
        Collection<Boolean> states = mCheckedItemsMap.values();
        index = 0;
        for (Boolean state : states) {
            checkedStates[index++] = state;
        }
        outState.putBooleanArray(KEY_CHECKEDSTATES, checkedStates);

        outState.putParcelable(KEY_FILTER, mFilter);
    }

    private void setFilter(ContactListFilter filter) {
        if (mFilter == null && filter == null) {
            return;
        }

        if (mFilter != null && mFilter.equals(filter)) {
            return;
        }

        Log.v(TAG, "New filter: " + filter);

        mFilter = filter;
        saveFilter();
        reloadData();
    }

    private void updateFilterHeaderView() {
        if (!mShowFilterHeader) {
            if (mAccountFilterHeader != null) {
                mAccountFilterHeader.setVisibility(View.GONE);
            }
            return;
        }
        if (mAccountFilterHeader == null) {
            return; // Before onCreateView -- just ignore it.
        }
        final ContactListFilter filter = mFilter;
        if (filter != null && !isSearchMode()) {
            final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
                    mAccountFilterHeader, filter, false, true);
            mAccountFilterHeader.setVisibility(shouldShowHeader ? View.VISIBLE : View.GONE);
        } else {
            mAccountFilterHeader.setVisibility(View.GONE);
        }
    }

    private void saveFilter() {
        ContactListFilter.storeToPreferences(mPrefs, mFilter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACCOUNT_FILTER) {
            if (getActivity() != null) {
                AccountFilterUtil.handleAccountFilterResult(ContactListFilterController
                        .getInstance(getActivity()), resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                    setFilter(ContactListFilterController.getInstance(getActivity()).getFilter());
                    updateFilterHeaderView();
                }
            } else {
                Log.e(TAG, "getActivity() returns null during Fragment#onActivityResult()");
            }
        }
    }

    public void onOptionAction() {

        Activity activity = getActivity();

        int selectedCount = this.getListView().getCheckedItemCount();

        if (selectedCount == 0) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
        }

        final Intent retIntent = new Intent();
        if (null == retIntent) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        int curArray = 0;
        long[] idArray = new long[selectedCount];
        if (null == idArray) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArray[curArray++] = adapter.getContactID(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        retIntent.putExtra(resultIntentExtraName, idArray);
        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }

    @Override
    public void onSelectAll() {
        updateListCheckBoxeState(true);
    }

    private void updateListCheckBoxeState(boolean checked) {
        final MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) getAdapter();
        final int count = getListView().getAdapter().getCount();
        long contactId = -1;
        for (int position = 0; position < count; ++position) {
            if (checked) {
                if (getListView().getCheckedItemCount() >= 5000) {
                    Toast.makeText(getActivity(), R.string.multichoice_contacts_limit, Toast.LENGTH_SHORT)
                            .show();
                    break;
                }
                getListView().setItemChecked(position, checked);
                contactId = adapter.getContactID(position);
                mCheckedItemsMap.put(Long.valueOf(contactId), checked);
            } else {
                if (getListView().isItemChecked(position)) {
                    getListView().setItemChecked(position, checked);
                    contactId = adapter.getContactID(position);
                    mCheckedItemsMap.put(Long.valueOf(contactId), checked);
                }
            }
        }
        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    protected void setDataSetChangedNotifyEnable(boolean enable) {
        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) getAdapter();
        if (adapter != null) {
            adapter.setDataSetChangedNotifyEnable(enable);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mEmptyView != null) {
            if (getAdapter().isSearchMode()) {
                mEmptyView.setText(R.string.listFoundAllContactsZero);
            } else {
                mEmptyView.setText(R.string.noContacts);
            }
        }
        if (data == null || (data != null && data.getCount() == 0)) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        // clear list view choices
        getListView().clearChoices();

        int checkedItemsCount = 0;
        int contactId = -1;
        int position = 0;

        if (data != null) {
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                contactId = -1;
                contactId = data.getInt(0);

                if (mCheckedItemsMap.containsKey(Long.valueOf(contactId))) {
                    boolean checked = mCheckedItemsMap.get(Long.valueOf(contactId));
                    getListView().setItemChecked(position, checked);
                    if (checked) {
                        ++checkedItemsCount;
                    }
                } else {
                    getListView().setItemChecked(position, false);
                    mCheckedItemsMap.put(Long.valueOf(contactId), false);
                }

                ++position;
            }
        }

        updateSelectedItemsView(checkedItemsCount);
        updateFilterHeaderView();

        super.onLoadFinished(loader, data);
    }

    public void startSearch(String searchString) {
        // It could not meet the layout Req. So, we should not use the default search function

        // Normalize the empty query.
        if (TextUtils.isEmpty(searchString)) {
            searchString = null;
        }

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) getAdapter();
        if (searchString == null) {
            if (adapter != null) {
                mSearchString = null;
                adapter.setQueryString(searchString);
                adapter.setSearchMode(false);
                reloadData();
            }
        } else if (!TextUtils.equals(mSearchString, searchString)) {
            mSearchString = searchString;
            if (adapter != null) {
                adapter.setQueryString(searchString);
                adapter.setSearchMode(true);
                reloadData();
            }
        }
    }

    private void updateSelectedItemsView(int checkedItemsCount) {
        if (getAdapter().isSearchMode()) {
            return;
        }

        TextView selectedItemsView = (TextView) getActivity().getActionBar().getCustomView()
                .findViewById(R.id.select_items);
        if (selectedItemsView == null) {
            Log.e(TAG, "Load view resource error!");
            return;
        }
        if (mSlectedItemsFormater == null) {
            Log.e(TAG, "Load string resource error!");
            return;
        }

        selectedItemsView.setText(String.format(mSlectedItemsFormater, String
                .valueOf(checkedItemsCount)));
    }

    public void updateSelectedItemsView() {
        final MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) getAdapter();
        int checkedItemsCount = 0;
        long contactId = -1;
        int count = getListView().getAdapter().getCount();
        for (int position = 0; position < count; ++position) {
            contactId = -1;
            contactId = adapter.getItemId(position);
            if (mCheckedItemsMap.containsKey(Long.valueOf(contactId))
                    && mCheckedItemsMap.get(Long.valueOf(contactId))) {
                ++checkedItemsCount;
            }
        }

        updateSelectedItemsView(checkedItemsCount);
    }

    public void showFilterHeader(boolean enable) {
        mShowFilterHeader = enable;
    }
}
