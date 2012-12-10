
package com.mediatek.contacts.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.util.AccountFilterUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.featureoption.FeatureOption;

public abstract class DataKindPickerBaseFragment extends
        ContactEntryListFragment<ContactEntryListAdapter> implements ContactListMultiChoiceListener {

    private final String TAG = DataKindPickerBaseFragment.class.getSimpleName();

    private static final String resultIntentExtraName = "com.mediatek.contacts.list.pickdataresult";
    private static final String KEY_CHECKEDIDS = "checkedids";
    private static final String KEY_CHECKEDSTATES = "checkedstates";

    private String mSlectedItemsFormater = null;

    //private ArrayList<Long> mCheckedItemsList = new ArrayList<Long>();
    private HashMap<Long, Boolean> mCheckedItemsMap = new HashMap<Long, Boolean>();
    private String mSearchString;

    // Show account filter settings
    private View mAccountFilterHeader;

    private TextView mEmptyView = null;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.multichoice_contact_list, null);
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        mAccountFilterHeader.setClickable(false);
        mAccountFilterHeader.setVisibility(View.GONE);

        mEmptyView = (TextView) getView().findViewById(R.id.contact_list_empty);
        if (mEmptyView != null) {
            mEmptyView.setText(R.string.noContacts);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSlectedItemsFormater = getActivity().getString(R.string.menu_actionbar_selected_items);
        updateSelectedItemsView();

        this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setFastScrollEnabled(true);
        getListView().setFastScrollAlwaysVisible(true);
    }

    @Override
    protected void configureAdapter() {
        ContactEntryListAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }

        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);
        //adapter.setSearchMode(false);
        adapter.setIncludeProfile(false);
        // Show A-Z section index.
        adapter.setSectionHeaderDisplayEnabled(true);
        // Disable pinned header. It doesn't work with this fragment.
        adapter.setPinnedPartitionHeadersEnabled(false);
        super.setPhotoLoaderEnabled(true);
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
                mAccountFilterHeader.setVisibility(View.VISIBLE);
            }
        }
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

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
        mCheckedItemsMap.put(Long.valueOf(id), getListView().isItemChecked(position));

        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    @Override
    public void onClearSelect() {
        updateListCheckBoxeState(false);
    }

    @Override
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

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArray[curArray++] = adapter.getDataId(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        for (long item:idArray) {
            Log.d(TAG, "result array: item " + item);
        }
        retIntent.putExtra(resultIntentExtraName, idArray);
        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }

    @Override
    public void onSelectAll() {
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     updateListCheckBoxeState(true);
         *   CR ID: ALPS00247750
         *   Descriptions: add progessdialog when it's busy 
         */
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
                getString(R.string.please_wait),
                getString(R.string.upgrade_in_progress), true, false);
        progressDialog.show();
        Log.i(TAG,"onSelectAll+");
        updateListCheckBoxeState(true);
        Log.i(TAG,"onSelectAll-");
        progressDialog.dismiss();
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private void updateListCheckBoxeState(boolean checked) {
        final DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
        final int count = getListView().getAdapter().getCount();
        long dataId = -1;
        for (int position = 0; position < count; ++position) {
            if (checked) {
                if (getListView().getCheckedItemCount() >= 5000) {
                    Toast.makeText(getActivity(), R.string.multichoice_contacts_limit, Toast.LENGTH_SHORT)
                            .show();
                    break;
                }
                getListView().setItemChecked(position, checked);
                dataId = adapter.getDataId(position);
                mCheckedItemsMap.put(Long.valueOf(dataId), checked);
            } else {
                if (getListView().isItemChecked(position)) {
                    getListView().setItemChecked(position, checked);
                    dataId = adapter.getDataId(position);
                    mCheckedItemsMap.put(Long.valueOf(dataId), checked);
                }
            }
        }

        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    @Override
    protected void onItemClick(int position, long id) {
        return;
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
        long dataId = -1;
        int position = 0;

        if (data != null) {
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                dataId = -1;
                dataId = data.getInt(0);

                if (mCheckedItemsMap.containsKey(Long.valueOf(dataId))) {
                    boolean checked = mCheckedItemsMap.get(Long.valueOf(dataId));
                    getListView().setItemChecked(position, checked);
                    if (checked) {
                        ++checkedItemsCount;
                    }
                } else {
                    getListView().setItemChecked(position, false);
                    mCheckedItemsMap.put(Long.valueOf(dataId), false);
                }

                ++position;
            }
        }

        updateSelectedItemsView(checkedItemsCount);
        final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
                mAccountFilterHeader, ContactListFilter
                        .createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS), false,
                true);

        super.onLoadFinished(loader, data);
    }

    @Override
    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);

        if (savedState == null) {
            return;
        }

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
    }

    public void startSearch(String searchString) {
        // It could not meet the layout Req. So, we should not use the default search function

        // Normalize the empty query.
        if (TextUtils.isEmpty(searchString)) {
            searchString = null;
        }

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
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

        final DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();

        int checkedItemsCount = 0;
        long dataId = -1;
        int count = getListView().getAdapter().getCount();
        for (int position = 0; position < count; ++position) {
            dataId = -1;
            dataId = adapter.getDataId(position);
            if (mCheckedItemsMap.containsKey(Long.valueOf(dataId))
                    && mCheckedItemsMap.get(Long.valueOf(dataId))) {
                ++checkedItemsCount;
            }
        }

        updateSelectedItemsView(checkedItemsCount);
    }
}
