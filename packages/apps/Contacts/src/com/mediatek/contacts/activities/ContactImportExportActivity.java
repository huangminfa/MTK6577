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

package com.mediatek.contacts.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.contacts.R;
import android.content.AsyncTaskLoader;

import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.android.contacts.list.ContactListFilterView;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.CustomContactListFilterActivity;
import android.content.Loader;
import android.accounts.Account;
import android.app.LoaderManager.LoaderCallbacks;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.mediatek.contacts.interactions.ImportExportDialogFragmentEx;
import com.mediatek.contacts.util.ContactsIntent;
import com.google.android.collect.Lists;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckedTextView;

import com.mediatek.CellConnService.CellConnMgr;
import android.provider.Telephony.SIMInfo;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume; // Description: for SIM name display
import com.android.contacts.util.AccountFilterUtil;

import android.widget.LinearLayout;


public class ContactImportExportActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = ContactImportExportActivity.class.getSimpleName();

    public static final int REQUEST_CODE = 111111;
    public static final int RESULT_CODE = 111112;

    /*
     * To unify the storages(includes internal storage and external storage)
     * handling, we looks all of storages as one kind of account type.
     */
    public static final String STORAGE_ACCOUNT_TYPE = "_STORAGE_ACCOUNT";

    private final int ACCOUNT_LOADER_ID = 0;

    private ListView mListView;
    private List<AccountWithDataSetEx> mAccounts = null;

    private int mShowingStep = 0;
    private int mCheckedPosition = 0;
    private boolean mIsFirstEntry = true;
    private AccountWithDataSetEx mCheckedAccount1 = null;
    private AccountWithDataSetEx mCheckedAccount2 = null;
    private List<ListViewItemObject> mListItemObjectList = new ArrayList<ListViewItemObject>();
    private AccountListAdapter mAdapter = null;

    private Runnable mServiceComplete = new Runnable() {
        public void run() {
            Log.d(TAG, "mServiceComplete run");
            int nRet = mCellMgr.getResult();
            Log.d(TAG, "mServiceComplete result = " + CellConnMgr.resultToString(nRet));
            if (mCellMgr.RESULT_ABORT == nRet) {
                return;
            } else {
                handleImportExportAction();
                return;
            }
        }
    };

    private CellConnMgr mCellMgr = new CellConnMgr(mServiceComplete);

    private class ListViewItemObject {
        public AccountWithDataSetEx mAccount;
        public CheckedTextView view;

        public ListViewItemObject(AccountWithDataSetEx account) {
            mAccount = account;
        }

        public String getName() {
            if (mAccount == null) {
                return "null";
            } else {
                String displayName = null;
                displayName = AccountFilterUtil.getAccountDisplayNameByAccount(mAccount.type,
                        mAccount.name);
                if (null == displayName) {
                    return mAccount.name;
                } else {
                    return displayName;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.import_export_bridge_layout);

        ((Button) findViewById(R.id.btn_action)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_back)).setOnClickListener(this);

        ((LinearLayout) findViewById(R.id.buttonbar_layout)).setVisibility(View.GONE);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                            | ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(R.string.imexport_title);
        }

        mAdapter = new AccountListAdapter(ContactImportExportActivity.this);
        getLoaderManager().initLoader(ACCOUNT_LOADER_ID, null, new MyLoaderCallbacks());

        mCellMgr.register(this);
    }

    private void setButtonState(boolean isTrue) {

        findViewById(R.id.btn_back).setVisibility(
                (isTrue && (mShowingStep > 1)) ? View.VISIBLE : View.GONE);

        findViewById(R.id.btn_action).setEnabled(isTrue && (mShowingStep > 0));
    }

    private void setShowingStep(int showingStep) {
        mShowingStep = showingStep;
        mListItemObjectList.clear();

        ((LinearLayout) findViewById(R.id.buttonbar_layout)).setVisibility(View.VISIBLE);

        if (mShowingStep == 1) {
            ((TextView) findViewById(R.id.tips)).setText(R.string.tips_source);
            for (AccountWithDataSetEx account : mAccounts) {
                mListItemObjectList.add(new ListViewItemObject(account));
            }
        } else if (mShowingStep == 2) {
            ((TextView) findViewById(R.id.tips)).setText(R.string.tips_target);
            for (AccountWithDataSetEx account : mAccounts) {
                if (mCheckedAccount1 != account) {
                    /*
                     * It is not allowed for the importing from Storage -> SIM
                     * or USIM and from SIM or USIM -> Storage and also is not
                     * for importing from Storage -> Storage
                     */
                    /*
                    boolean importFromStorageToSIM = mCheckedAccount1.type
                            .equals(STORAGE_ACCOUNT_TYPE)
                            && isSIMUSIMAccount(account);
                    boolean importFromSIMToStorage = isSIMUSIMAccount(mCheckedAccount1)
                            && account.type.equals(STORAGE_ACCOUNT_TYPE);
                    boolean importFromStorageToStorage = mCheckedAccount1.type
                            .equals(STORAGE_ACCOUNT_TYPE)
                            && account.type.equals(STORAGE_ACCOUNT_TYPE); 
                    */
                    if ((isStorageAccount(mCheckedAccount1) && isSIMUSIMAccount(account))
                            || (isSIMUSIMAccount(mCheckedAccount1) && isStorageAccount(account))
                            || (isStorageAccount(mCheckedAccount1) && isStorageAccount(account))) {
                        continue;
                    }

                    mListItemObjectList.add(new ListViewItemObject(account));
                }
            }
        }
    }

    private static boolean isSIMUSIMAccount(final Account account) {
        if (account != null) {
            //UIM
            return account.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_SIM)
                    || account.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_USIM)
                    || account.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_UIM);
            //UIM
        }
        return false;
    }

    private static boolean isStorageAccount(final Account account) {
        if (account != null) {
            return account.type.equalsIgnoreCase(STORAGE_ACCOUNT_TYPE);
        }
        return false;
    }

    private static class AccountsLoader extends AsyncTaskLoader<List<AccountWithDataSetEx>> {
        private Context mContext;

        public AccountsLoader(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public List<AccountWithDataSetEx> loadInBackground() {
            return loadAccountFilters(mContext);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            onStopLoading();
        }
    }

    private void setCheckedPosition(int checkedPosition) {
        if (mCheckedPosition != checkedPosition) {
            setListViewItemChecked(mCheckedPosition, false);
            mCheckedPosition = checkedPosition;
            setListViewItemChecked(mCheckedPosition, true);
        }
    }

    private void setCheckedAccount(int position) {
        if (mShowingStep == 1) {
            mCheckedAccount1 = mListItemObjectList.get(position).mAccount;
        } else if (mShowingStep == 2) {
            mCheckedAccount2 = mListItemObjectList.get(position).mAccount;
            //mCheckedAccountEnd = mCheckedAccount2;
        }
    }

    private void setListViewItemChecked(int checkedPosition, boolean checked) {
        if (checkedPosition > -1) {
            ListViewItemObject itemObj = mListItemObjectList.get(checkedPosition);
            if (itemObj.view != null) {
                itemObj.view.setChecked(checked);
            }
            // CheckedTextView view = (CheckedTextView)
            // mListView.getChildAt(checkedPosition);
            // view.setChecked(checked);
        }
    }

    private static List<AccountWithDataSetEx> loadAccountFilters(Context context) {

        List<AccountWithDataSetEx> accountsEx = new ArrayList<AccountWithDataSetEx>();

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
        List<AccountWithDataSet> accounts = accountTypes.getAccounts(true);
        for (AccountWithDataSet account : accounts) {
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            if (accountType.isExtension() && !account.hasData(context)) {
                // Hide extensions with no raw_contacts.
                continue;
            }

            int slot = 0;
            if (account instanceof AccountWithDataSetEx) {
                slot = ((AccountWithDataSetEx) account).getSlotId();
            }

            accountsEx.add(new AccountWithDataSetEx(account.name, account.type, slot));

        }

        return accountsEx;
    }

    private class MyLoaderCallbacks implements LoaderCallbacks<List<AccountWithDataSetEx>> {
        @Override
        public Loader<List<AccountWithDataSetEx>> onCreateLoader(int id, Bundle args) {
            return new AccountsLoader(ContactImportExportActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<AccountWithDataSetEx>> loader,
                List<AccountWithDataSetEx> data) {
            if (data == null) { // Just in case...
                Log.e(TAG, "Failed to load accounts");
                return;
            }
            if (mAccounts == null) {
                mAccounts = data;

                // Add all of storages accounts
                mAccounts.addAll(getStorageAccounts());

                if (mShowingStep == 0) {
                    setShowingStep(1);
                } else {
                    setShowingStep(mShowingStep);
                }
                setCheckedAccount(mCheckedPosition);
                updateUi();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<AccountWithDataSetEx>> loader) {
        }
    }

    private class AccountListAdapter extends BaseAdapter {
        private final LayoutInflater mLayoutInflater;

        public AccountListAdapter(Context context) {
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mListItemObjectList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public AccountWithDataSetEx getItem(int position) {
            return null;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final CheckedTextView view;
            if (convertView != null) {
                view = (CheckedTextView) convertView;
            } else {
                view = (CheckedTextView) mLayoutInflater.inflate(
                        R.layout.simple_list_item_single_choice, parent, false);
            }

            ListViewItemObject itemObj = mListItemObjectList.get(position);
            itemObj.view = view;
            view.setText(itemObj.getName());
            view.setChecked(mCheckedPosition == position);

            return view;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setCheckedPosition(position);
        setCheckedAccount(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_action:
            case R.id.btn_back:
                int pos = 0;
                if (view.getId() == R.id.btn_action) {
                    if (mShowingStep >= 2) {
                        doImportExport();
                        return;
                    }
                    setShowingStep(2);
                    if (mIsFirstEntry || (mCheckedAccount1 == null && mCheckedAccount2 == null)) {
                        pos = 0;
                    } else {
                        pos = getCheckedAccountPosition(mCheckedAccount2);
                    }
                    mIsFirstEntry = false;
                } else {
                    setShowingStep(1);
                    pos = getCheckedAccountPosition(mCheckedAccount1);
                }
                mCheckedPosition = pos;
                setCheckedAccount(mCheckedPosition);
                updateUi();
                break;

            default:
                break;
        }
    }

    private void updateUi() {
        setButtonState(true);
        mListView.setAdapter(mAdapter);
    }

    private int getCheckedAccountPosition(AccountWithDataSetEx checkedAccount) {
        for (int i = 0; i < mListItemObjectList.size(); i++) {
            ListViewItemObject obj = mListItemObjectList.get(i);
            if (obj.mAccount == checkedAccount) {
                return i;
            }
        }
        return 0;
    }

    private void handleImportExportAction() {       

        if (isStorageAccount(mCheckedAccount1) && !checkSDCardAvaliable(mCheckedAccount1.dataSet)
                || isStorageAccount(mCheckedAccount2)
                && !checkSDCardAvaliable(mCheckedAccount2.dataSet)) {
            new AlertDialog.Builder(this).setMessage(R.string.no_sdcard_message).setTitle(
                    R.string.no_sdcard_title).setIcon(
                    com.android.internal.R.drawable.ic_dialog_alert_holo_light).setPositiveButton(
                    android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
            return;
        }

        if (isStorageAccount(mCheckedAccount1)) { // import from SDCard
            if (mCheckedAccount2 != null) {
                AccountSelectionUtil.doImportFromSdCard(this, mCheckedAccount1.dataSet, mCheckedAccount2);
            }
        } else {

           if (isStorageAccount(mCheckedAccount2)) { // export to SDCard
                //if (isSDCardFull()) { // SD card is full
                if (isSDCardFull(mCheckedAccount2.dataSet)) { // SD card is full
                    Log.i(TAG, "[handleImportExportAction] isSDCardFull");
                    new AlertDialog.Builder(this).setMessage(R.string.storage_full).setTitle(
                            R.string.storage_full).setIcon(
                            com.android.internal.R.drawable.ic_dialog_alert_holo_light)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                    return;
                }
                Intent intent = new Intent(this,
                        com.mediatek.contacts.list.ContactListMultiChoiceActivity.class).setAction(
                        ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS).putExtra("request_type",
                        ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER).putExtra(
                        "toSDCard", true).putExtra("fromaccount", mCheckedAccount1).putExtra(
                        "toaccount", mCheckedAccount2);
                startActivityForResult(intent, ContactImportExportActivity.REQUEST_CODE);
            } else { // account to account
                Intent intent = new Intent(this,
                        com.mediatek.contacts.list.ContactListMultiChoiceActivity.class).setAction(
                        ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS).putExtra("request_type",
                        ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER).putExtra(
                        "toSDCard", false).putExtra("fromaccount", mCheckedAccount1).putExtra(
                        "toaccount", mCheckedAccount2);
                startActivityForResult(intent, ContactImportExportActivity.REQUEST_CODE);
            }
        }
    }

    private boolean checkSDCardAvaliable(final String path) {
        if (path == null) {
            return false;
        }

        String volumeState = "";

        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                    .getService("mount"));
            volumeState = mountService.getVolumeState(path);
        } catch (Exception rex) {
            Log.e(TAG, rex.getStackTrace().toString());
        }

        return volumeState.equals(Environment.MEDIA_MOUNTED);
    }

    private boolean isSDCardFull(final String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        Log.d(TAG, "isSDCardFull storage path is " + path);
        if (checkSDCardAvaliable(path)) {
            StatFs sf = null;
            try {
                sf = new StatFs(path);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                return false;
            }

            if (sf == null) {
                Log.e(TAG, "isSDCardFull sf is null ");
                return false;
            }

            long availCount = sf.getAvailableBlocks();
            if (availCount > 0) {
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ContactImportExportActivity.REQUEST_CODE) {
            if (resultCode == ContactImportExportActivity.RESULT_CODE) {
                this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mCellMgr.unregister();
        super.onDestroy();
        Log.i(TAG, "[onDestroy]");
    }

    public void doImportExport() {
        int slotId = SimCardUtils.SimSlot.SLOT_NONE;
        //UIM
//        if ((mCheckedAccount1 != null)
//                && (mCheckedAccount1.type.equals(AccountType.ACCOUNT_TYPE_SIM) || mCheckedAccount1.type
//                        .equals(AccountType.ACCOUNT_TYPE_USIM))) 
        if ((mCheckedAccount1 != null)
                && (mCheckedAccount1.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                        || mCheckedAccount1.type.equals(AccountType.ACCOUNT_TYPE_USIM) || mCheckedAccount1.type
                        .equals(AccountType.ACCOUNT_TYPE_UIM)))
        //UIM
            {
            slotId = ((AccountWithDataSetEx) mCheckedAccount1).getSlotId();
            int nRet = mCellMgr.handleCellConn(slotId, CellConnMgr.REQUEST_TYPE_FDN);
            Log.i(TAG, "[doImportExport] nRet : " + nRet);
        }
        //UIM
//        else if ((mCheckedAccount2 != null)
//               && (mCheckedAccount2.type.equals(AccountType.ACCOUNT_TYPE_SIM) || mCheckedAccount2.type
//                        .equals(AccountType.ACCOUNT_TYPE_USIM))) 
        else if ((mCheckedAccount2 != null)
                && (mCheckedAccount2.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                        || mCheckedAccount2.type.equals(AccountType.ACCOUNT_TYPE_USIM) || mCheckedAccount2.type
                        .equals(AccountType.ACCOUNT_TYPE_UIM)))
        //UIM
            {
        	slotId = ((AccountWithDataSetEx) mCheckedAccount2).getSlotId();
            int nRet = mCellMgr.handleCellConn(slotId, CellConnMgr.REQUEST_TYPE_FDN);
            Log.i(TAG, "[doImportExport] nRet : " + nRet);
        } else {
            handleImportExportAction();
        }
    }

    public File getDirectory(String path, String defaultPath) {
        Log.i("getDirectory", "path : " + path);
        return path == null ? new File(defaultPath) : new File(path);
    }

    public List<AccountWithDataSetEx> getStorageAccounts() {
        List<AccountWithDataSetEx> storageAccounts = new ArrayList<AccountWithDataSetEx>();
        StorageManager mSM = (StorageManager) getApplicationContext().getSystemService(
                STORAGE_SERVICE);
        if (null == mSM) {
            return storageAccounts;
        }
        StorageVolume volumes[] = mSM.getVolumeList();
        if (volumes != null) {
            for (StorageVolume volume : volumes) {
                if(volume.getPath().equals("/mnt/usbotg")) {
                    continue;
                }
                storageAccounts.add(new AccountWithDataSetEx(volume.getDescription(),
                        STORAGE_ACCOUNT_TYPE, volume.getPath()));
            }
        }
        return storageAccounts;
    }

}
