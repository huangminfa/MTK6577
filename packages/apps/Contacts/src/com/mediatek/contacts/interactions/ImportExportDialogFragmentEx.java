/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.mediatek.contacts.interactions;

import com.android.contacts.R;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.vcard.ExportVCardActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * An dialog invoked to import/export contacts.
 */
public class ImportExportDialogFragmentEx extends DialogFragment
        /*implements SelectAccountDialogFragment.Listener*/ {

    public static final String TAG = "ImportExportDialogFragmentEx";

    //private static final String KEY_RES_ID = "resourceId";

    /*private final String[] LOOKUP_PROJECTION = new String[] {
            Contacts.LOOKUP_KEY
    };*/
    
    private static List<String> mDisplayTextList;
    private static String mTitle;
    public static Listener mListener;
    
    public interface Listener {
        void onItemClicked(String title, String textSelected);
    }

    /** Preferred way to show this dialog */
    public static void show(FragmentManager fragmentManager, List<String> displayTextList,
                            String title, Listener listener) {
        mDisplayTextList = displayTextList;
        mTitle = title;
        mListener = listener;
        final ImportExportDialogFragmentEx fragment = new ImportExportDialogFragmentEx();
        fragment.show(fragmentManager, ImportExportDialogFragmentEx.TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Wrap our context to inflate list items using the correct theme
        final Resources res = getActivity().getResources();
        final LayoutInflater dialogInflater = (LayoutInflater)getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Adapter that shows a list of string resources
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.select_dialog_item, mDisplayTextList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TextView result = (TextView)(convertView != null ? convertView :
                        dialogInflater.inflate(R.layout.select_dialog_item, parent, false));

                final String text = getItem(position);
                result.setText(text);
                return result;
            }
        };

        final DialogInterface.OnClickListener clickListener =
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String textSelected = adapter.getItem(which);
                if (null != mListener) {
                    mListener.onItemClicked(mTitle, textSelected);
                }
                dialog.dismiss();
            }
        };
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setSingleChoiceItems(adapter, -1, clickListener)
                .create();
    }

    /*private void doShareVisibleContacts() {
        // TODO move the query into a loader and do this in a background thread
        final Cursor cursor = getActivity().getContentResolver().query(Contacts.CONTENT_URI,
                LOOKUP_PROJECTION, Contacts.IN_VISIBLE_GROUP + "!=0", null, null);
        if (cursor != null) {
            try {
                if (!cursor.moveToFirst()) {
                    Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder uriListBuilder = new StringBuilder();
                int index = 0;
                do {
                    if (index != 0)
                        uriListBuilder.append(':');
                    uriListBuilder.append(cursor.getString(0));
                    index++;
                } while (cursor.moveToNext());
                Uri uri = Uri.withAppendedPath(
                        Contacts.CONTENT_MULTI_VCARD_URI,
                        Uri.encode(uriListBuilder.toString()));

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                getActivity().startActivity(intent);
            } finally {
                cursor.close();
            }
        }
    }*/

    /**
     * Handle "import from SIM" and "import from SD".
     *
     * @return {@code true} if the dialog show be closed.  {@code false} otherwise.
     */
    /*private boolean handleImportRequest(int resId) {
        // There are three possibilities:
        // - more than one accounts -> ask the user
        // - just one account -> use the account without asking the user
        // - no account -> use phone-local storage without asking the user
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
        final List<AccountWithDataSet> accountList = accountTypes.getAccounts(true);
        final int size = accountList.size();
        if (size > 1) {
            // Send over to the account selector
            final Bundle args = new Bundle();
            args.putInt(KEY_RES_ID, resId);
            SelectAccountDialogFragment.show(
                    getFragmentManager(), this,
                    R.string.dialog_new_contact_account,
                    AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, args);

            // In this case, because this DialogFragment is used as a target fragment to
            // SelectAccountDialogFragment, we can't close it yet.  We close the dialog when
            // we get a callback from it.
            return false;
        }

        AccountSelectionUtil.doImport(getActivity(), resId,
                (size == 1 ? accountList.get(0) : null));
        return true; // Close the dialog.
    }*/

    /**
     * Called when an account is selected on {@link SelectAccountDialogFragment}.
     */
    /*@Override
    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        AccountSelectionUtil.doImport(getActivity(), extraArgs.getInt(KEY_RES_ID), account);

        // At this point the dialog is still showing (which is why we can use getActivity() above)
        // So close it.
        dismiss();
    }

    @Override
    public void onAccountSelectorCancelled() {
        // See onAccountChosen() -- at this point the dialog is still showing.  Close it.
        dismiss();
    }*/
}
