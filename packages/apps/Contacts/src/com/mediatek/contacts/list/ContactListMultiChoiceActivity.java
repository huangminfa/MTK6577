
package com.mediatek.contacts.list;

import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.android.contacts.R;

import com.android.contacts.ContactsActivity;
import com.android.contacts.activities.ConfirmAddDetailActivity;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.model.AccountType;
import com.android.contacts.widget.ContextMenuAdapter;
import com.mediatek.contacts.activities.ContactImportExportActivity;

/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting multiple contacts.
 */

public class ContactListMultiChoiceActivity extends ContactsActivity implements
        View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener,
        OnCloseListener, OnFocusChangeListener {
    private static final String TAG = "ContactsMultiChoiceActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    // Delay to allow the UI to settle before making search view visible
    private static final int FOCUS_DELAY = 200;

    private ContactsIntentResolverEx mIntentResolverEx;
    protected ContactEntryListFragment<?> mListFragment;

    private int mActionCode = -1;

    private ContactsRequest mRequest;
    private SearchView mSearchView;

    public ContactListMultiChoiceActivity() {
        mIntentResolverEx = new ContactsIntentResolverEx(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment<?>) {
            mListFragment = (ContactEntryListFragment<?>) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
        }

        // Extract relevant information from the intent
        mRequest = mIntentResolverEx.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            Log.d(TAG, "Request is invalid!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Intent redirect = mRequest.getRedirectIntent();
        if (redirect != null) {
            // Need to start a different activity
            startActivity(redirect);
            finish();
            return;
        }

        setContentView(R.layout.contact_picker);

        configureListFragment();

        // Disable Search View in listview
        SearchView searchViewInListview = (SearchView) findViewById(R.id.search_view);
        searchViewInListview.setVisibility(View.GONE);

        // Disable create new contact button
        View CreateNewContactButton = (View) findViewById(R.id.new_contact);
        if (CreateNewContactButton != null) {
                CreateNewContactButton.setVisibility(View.GONE);
        }

        showActionBar(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"[onDestroy]");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_multichoice, menu);
        MenuItem optionItem = menu.findItem(R.id.menu_option);
        int actionCode = mRequest.getActionCode();
        switch (actionCode) {
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
                optionItem.setIcon(com.android.internal.R.drawable.ic_menu_copy_holo_dark);
                optionItem.setTitle(android.R.string.copy);
                break;
            case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS:
                optionItem.setIcon(R.drawable.ic_menu_contact_trash);
                optionItem.setTitle(R.string.menu_deleteContact);
                break;
            case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS:
                optionItem.setIcon(com.android.internal.R.drawable.ic_menu_share_holo_dark);
                optionItem.setTitle(R.string.menu_share);
                break;
            case ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS:
                optionItem.setIcon(R.drawable.ic_menu_forward_holo_dark);
                optionItem.setTitle(R.string.move);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_menu_item) {
            showActionBar(true);
        }
        return;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            switch (itemId) {
                case R.id.menu_select_all:
                    fragment.onSelectAll();
                    return true;

                case R.id.menu_clear_select:
                    fragment.onClearSelect();
                    return true;

                case R.id.menu_option:
                    fragment.onOptionAction();
                    if (fragment instanceof MultiContactsDuplicationFragment) {
                        Log.d(TAG, "Send result for copy action");
                        setResult(ContactImportExportActivity.RESULT_CODE);
                    }
                    return true;

                default:
                    break;
            }
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            switch (itemId) {
                case R.id.menu_select_all:
                    fragment.onSelectAll();
                    return true;

                case R.id.menu_clear_select:
                    fragment.onClearSelect();
                    return true;

                case R.id.menu_option:
                    fragment.onOptionAction();
                    return true;

                default:
                    break;
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Creates the fragment based on the current request.
     */
    public void configureListFragment() {
        if (mActionCode == mRequest.getActionCode()) {
            return;
        }

        mActionCode = mRequest.getActionCode();
        Log.d(TAG, "configureListFragment action code is " + mActionCode);

        switch (mActionCode) {

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS: {
                mListFragment = new MultiContactsPickerBaseFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER: {
                mListFragment = new ContactsVCardPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER: {
                mListFragment = new MultiContactsDuplicationFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(MultiContactsPickerBaseFragment.FRAGMENT_ARGS, getIntent());
                mListFragment.setArguments(bundle);
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_EMAILS: {
                mListFragment = new MultiEmailsPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_PHONES: {
                mListFragment = new MultiPhoneNumbersPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS: {
                mListFragment = new ContactsMultiDeletionFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS: {
                mListFragment = new ContactsGroupMultiPickerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(MultiContactsPickerBaseFragment.FRAGMENT_ARGS, getIntent());
                mListFragment.setArguments(bundle);
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_PHONEANDEMAILS: {
                mListFragment = new MultiPhoneAndEmailsPickerFragment();
                break;
            }
            
            case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS: {
                mListFragment = new MultiContactsShareFragment();
                break;
            }

            default:
                throw new IllegalStateException("Invalid action code: " + mActionCode);
        }

        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setQueryString(mRequest.getQueryString(), false);
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
        mListFragment.setVisibleScrollbarEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    public void startActivityAndForwardResult(final Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Forward extras to the new activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuAdapter menuAdapter = mListFragment.getContextMenuAdapter();
        if (menuAdapter != null) {
            return menuAdapter.onContextItemSelected(item);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            fragment.startSearch(newText);
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            fragment.startSearch(newText);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onClose() {
        if (mSearchView == null) {
            return false;
        }
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        showActionBar(false);
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            fragment.updateSelectedItemsView();
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            fragment.updateSelectedItemsView();
        }
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()) {
            case R.id.search_view: {
                if (hasFocus) {
                    showInputMethod(mSearchView.findFocus());
                }
            }
        }
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    public void returnPickerResult(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        returnPickerResult(intent);
    }

    public void returnPickerResult(Intent intent) {
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    startActivity(data);
                }
                finish();
            }
        }

        if (resultCode == ContactImportExportActivity.RESULT_CODE) {
            finish();
        }
    }

    public void onBackPressed() {
        if (mSearchView != null && !mSearchView.isFocused()) {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            showActionBar(false);
            if (mListFragment instanceof MultiContactsPickerBaseFragment) {
                MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
                fragment.updateSelectedItemsView();
            } else if (mListFragment instanceof DataKindPickerBaseFragment) {
                DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
                fragment.updateSelectedItemsView();
            }
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        
        Log.i(TAG,"[onConfigurationChanged]" +newConfig);
        super.onConfigurationChanged(newConfig);
        //do nothing
    }

    private void showActionBar(boolean searchMode) {
        ActionBar actionBar = getActionBar();
        if (searchMode) {
            final View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.custom_action_bar, null);
            mSearchView = (SearchView) searchViewContainer.findViewById(R.id.search_view);

            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setQueryHint(getString(R.string.hint_findContacts));
            mSearchView.setIconified(false);

            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setOnQueryTextFocusChangeListener(this);
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00292346
             *   Descriptions: change ime action
             */
            mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            /*
             * Bug Fix by Mediatek End.
             */

            actionBar.setCustomView(searchViewContainer, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            // Inflate a custom action bar that contains the "done" button for
            // multi-choice
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.multichoice_custom_action_bar,
                    null);
            ImageButton doneMenuItem = (ImageButton) customActionBarView
                    .findViewById(R.id.done_menu_item);
            doneMenuItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

            ImageButton searchMenuItem = (ImageButton) customActionBarView
                    .findViewById(R.id.search_menu_item);
            searchMenuItem.setOnClickListener(this);

            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
            mSearchView = null;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:               
            	onBackPressed();
                return true;
                
            default:
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
