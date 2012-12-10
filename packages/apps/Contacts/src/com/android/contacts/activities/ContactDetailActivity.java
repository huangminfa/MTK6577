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
 * limitations under the License
 */

package com.android.contacts.activities;

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.detail.ContactDetailLayoutController;
import com.android.contacts.detail.ContactLoaderFragment;
import com.android.contacts.detail.ContactLoaderFragment.ContactLoaderFragmentListener;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.PhoneCapabilityTester;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;

import android.content.Context;
import android.provider.Settings;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.Telephony.SIMInfo;

import com.mediatek.contacts.SubContactsUtils;
import android.os.SystemProperties;
import java.util.List;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.mediatek.contacts.extention.ContactExtention;
import com.mediatek.contacts.extention.ContactExtentionManager;

public class ContactDetailActivity extends ContactsActivity {
    private static final String TAG = "ContactDetailActivity";

    /**
     * Boolean intent key that specifies whether pressing the "up" affordance in this activity
     * should cause it to finish itself or launch an intent to bring the user back to a specific
     * parent activity - the {@link PeopleActivity}.
     */
    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED =
            "finishActivityOnUpSelected";

    private ContactLoader.Result mContactData;
    private Uri mLookupUri;
    // edit by chenlong 81249
    private Uri simOrPhoneUri;
    private boolean mFinishActivityOnUpSelected;

    private ContactDetailLayoutController mContactDetailLayoutController;
    private ContactLoaderFragment mLoaderFragment;

    private Handler mHandler = new Handler();
    public StatusBarManager mStatusBarMgr;
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (PhoneCapabilityTester.isUsingTwoPanes(this)) {
            // This activity must not be shown. We have to select the contact in the
            // PeopleActivity instead ==> Create a forward intent and finish
            final Intent originalIntent = getIntent();
            Intent intent = new Intent();
            intent.setAction(originalIntent.getAction());
            intent.setDataAndType(originalIntent.getData(), originalIntent.getType());
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.setClass(this, PeopleActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mFinishActivityOnUpSelected = getIntent().getBooleanExtra(
                INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED, false);

        setContentView(R.layout.contact_detail_activity);

        mContactDetailLayoutController = new ContactDetailLayoutController(this, savedState,
                getFragmentManager(), findViewById(R.id.contact_detail_container),
                mContactDetailFragmentListener);

        // We want the UP affordance but no app icon.
        // Setting HOME_AS_UP, SHOW_TITLE and clearing SHOW_HOME does the trick.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle("");
        }
        // edit by chenlong 81249
        simOrPhoneUri = getIntent().getData();
        Log.i(TAG, getIntent().getData().toString());
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     xxx
         *   CR ID: ALPS00307025 ;ALPS00311243 
         *   Descriptions: add new receiver; if it's tablet not use receiver
         */
        boolean isUsingTwoPanes = PhoneCapabilityTester.isUsingTwoPanes(this);
        if(!isUsingTwoPanes && FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    /*
     * New Feature by Mediatek Begin.
     *   Original Android's code:
     *     
     *   CR ID: ALPS00308657
     *   Descriptions: RCS
     */
        Uri contactLoopupUri = getIntent().getData();
        Log.i(TAG,"contactLoopupUri : "+contactLoopupUri);
        if (mContactExtention == null){
            mContactExtention = ContactExtentionManager.getInstance().getContactExtention();
        }
        mContactExtention.onContactDetialOpen(contactLoopupUri);
    /*
     * New Feature by Mediatek End.
     */
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00307025
             *   Descriptions: add new receiver
             */
            mShowSimIndicator = true;
            /*
             * Bug Fix by Mediatek End.
             */
            setSimIndicatorVisibility(true);
        }
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     xxx
         *   CR ID: ALPS00273970
         *   Descriptions: 
         */
        if(s_isNeedFinish){
            s_isNeedFinish = false;
            finish();
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }     
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     xxx
             *   CR ID: ALPS00307025
             *   Descriptions: add new receiver
             */
            mShowSimIndicator = true;
            /*
             * Bug Fix by Mediatek End.
             */
            setSimIndicatorVisibility(false);        
        }
    }
    
    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
         if (fragment instanceof ContactLoaderFragment) {
            mLoaderFragment = (ContactLoaderFragment) fragment;
            mLoaderFragment.setListener(mLoaderFragmentListener);
            mLoaderFragment.loadUri(getIntent().getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.star, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem starredMenuItem = menu.findItem(R.id.menu_star);
        ViewGroup starredContainer = (ViewGroup) getLayoutInflater().inflate(
                R.layout.favorites_star, null, false);
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
        // edit by chenlong 81249
        //Log.i(TAG, "=================="+ContentUris.parseId(simOrPhoneUri)+"=======================");
		//Cursor cursor = getContentResolver().query(simOrPhoneUri,
		//		new String[] { Contacts.INDICATE_PHONE_SIM}, null,null, null);
		
		//int indicatePhoneSim = 0;
		//if(cursor != null && cursor.moveToFirst()){
		//	indicatePhoneSim = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
		//	Log.i(TAG, "==" + indicatePhoneSim + "===" + indicatePhoneSim);
		//}
		
        //cursor.close();
         
        if (indicatePhoneSim < 0) {
         *   CR ID: ALPS00115684
         */
        if (this.mContactData != null && this.mContactData.getIndicate() < 0) {
        /*
         * Bug Fix by Mediatek End.
         */
            final CheckBox starredView = (CheckBox) starredContainer.findViewById(R.id.star);

            starredView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle "starred" state
                    // Make sure there is a contact
                    if (mLookupUri != null) {
                        Intent intent = ContactSaveService.createSetStarredIntent(
                                ContactDetailActivity.this, mLookupUri, starredView.isChecked());
                        ContactDetailActivity.this.startService(intent);
                    }
                }
            });
            // If there is contact data, update the starred state
            //if (mContactData != null) {
            ContactDetailDisplayUtils.setStarred(mContactData, starredView);
            //}

        }
        starredMenuItem.setActionView(starredContainer);
        
        /*
         * New Feature by Mediatek Begin.            
         * set this if show new association menu        
         */
        setAssociationMenu(menu, true);
        /*
         * New Feature  by Mediatek End.
        */
        
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // First check if the {@link ContactLoaderFragment} can handle the key
        if (mLoaderFragment != null && mLoaderFragment.handleKeyDown(keyCode)) return true;

        // Otherwise find the correct fragment to handle the event
        FragmentKeyListener mCurrentFragment = mContactDetailLayoutController.getCurrentPage();
        if (mCurrentFragment != null && mCurrentFragment.handleKeyDown(keyCode)) return true;

        // In the last case, give the key event to the superclass.
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mContactDetailLayoutController != null) {
            mContactDetailLayoutController.onSaveInstanceState(outState);
        }
    }

    private final ContactLoaderFragmentListener mLoaderFragmentListener =
            new ContactLoaderFragmentListener() {
        @Override
        public void onContactNotFound() {
            finish();
        }

        @Override
        public void onDetailsLoaded(final ContactLoader.Result result) {
            if (result == null) {
                return;
            }
            // Since {@link FragmentTransaction}s cannot be done in the onLoadFinished() of the
            // {@link LoaderCallbacks}, then post this {@link Runnable} to the {@link Handler}
            // on the main thread to execute later.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // If the activity is destroyed (or will be destroyed soon), don't update the UI
                    if (isFinishing()) {
                        return;
                    }
                    mContactData = result;
                    mLookupUri = result.getLookupUri();
                    invalidateOptionsMenu();
                    setupTitle();
                    mContactDetailLayoutController.setContactData(mContactData);
                }
            });
        }

        @Override
        public void onEditRequested(Uri contactLookupUri) {
            Intent intent = new Intent(Intent.ACTION_EDIT, contactLookupUri);
            intent.putExtra(
                    ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
            // Don't finish the detail activity after launching the editor because when the
            // editor is done, we will still want to show the updated contact details using
            // this activity.
            startActivity(intent);
        }

        @Override
        public void onDeleteRequested(Uri contactUri) {
            if (mContactData.getIndicate() < 0) {
            ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true);
            } else {                
                int simIndex = mContactData.getSimIndex();
                Uri simUri = SubContactsUtils.getUri(SIMInfo.getSlotById(ContactDetailActivity.this, mContactData.getIndicate()));
                Log.d(TAG, "onDeleteRequested contact indicate = " + mContactData.getIndicate());
                Log.d(TAG, "onDeleteRequested slot id = " + SIMInfo.getSlotById(ContactDetailActivity.this, mContactData.getIndicate()));
                Log.d(TAG, "onDeleteRequested simUri = " + simUri);
                ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true, simUri, ("index = " + simIndex));                
            }
        }
    };

    /**
     * Setup the activity title and subtitle with contact name and company.
     */
    private void setupTitle() {
        CharSequence displayName = ContactDetailDisplayUtils.getDisplayName(this, mContactData);
        String company =  ContactDetailDisplayUtils.getCompany(this, mContactData);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(displayName);
        actionBar.setSubtitle(company);

        if (!TextUtils.isEmpty(displayName) &&
                AccessibilityManager.getInstance(this).isEnabled()) {
            View decorView = getWindow().getDecorView();
            decorView.setContentDescription(displayName);
            decorView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    }

    private final ContactDetailFragment.Listener mContactDetailFragmentListener =
            new ContactDetailFragment.Listener() {
        @Override
        public void onItemClicked(Intent intent) {
            if (intent == null) {
                return;
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for intent: " + intent);
            }
        }

        @Override
        public void onCreateRawContactRequested(
                ArrayList<ContentValues> values, AccountWithDataSet account) {
            Toast.makeText(ContactDetailActivity.this, R.string.toast_making_personal_copy,
                    Toast.LENGTH_LONG).show();
            Intent serviceIntent = ContactSaveService.createNewRawContactIntent(
                    ContactDetailActivity.this, values, account,
                    ContactDetailActivity.class, Intent.ACTION_VIEW);
            startService(serviceIntent);

        }
    };

    /**
     * This interface should be implemented by {@link Fragment}s within this
     * activity so that the activity can determine whether the currently
     * displayed view is handling the key event or not.
     */
    public interface FragmentKeyListener {
        /**
         * Returns true if the key down event will be handled by the implementing class, or false
         * otherwise.
         */
        public boolean handleKeyDown(int keyCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mFinishActivityOnUpSelected) {
                    finish();
                    return true;
                }
                Intent intent = new Intent(this, PeopleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
                
            case R.id.menu_association_sim:
                ContactDetailFragment detailFragment =  mContactDetailLayoutController.getDetailFragment();
                if (detailFragment != null) {
                    detailFragment.handleAssociationSimOptionMenu();                   
                }                
                return true;                
                
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /*
     * New Feature by Mediatek Begin.            
     * set this if show new association menu        
     */
    public void setAssociationMenu(Menu menu, boolean fromOptionsMenu) {
        if (fromOptionsMenu) {
            MenuItem associationMenuItem = menu.findItem(R.id.menu_association_sim);
            if (associationMenuItem != null) {
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original Android's code:
                 *     if (isHasPhoneItem()) { 
                 *   CR ID: ALPS00116397
                 */
                if (FeatureOption.MTK_GEMINI_SUPPORT && isHasPhoneItem() && !isMe()) {
                /*
                 * Bug Fix by Mediatek End.
                 */    
                    associationMenuItem.setVisible(!this.mContactData.isDirectoryEntry());
                    associationMenuItem.setEnabled(ContactDetailActivity.getInsertedSimCardInfoList(this, false).size() > 0);
                } else {
                    associationMenuItem.setVisible(false);
                }
            }
           
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * get if has phone number item        
     */
    public boolean isHasPhoneItem() {
        ContactDetailFragment detailFragment =  mContactDetailLayoutController.getDetailFragment();
        if (detailFragment != null && detailFragment.hasPhoneEntry(this.mContactData)) {
            return true;
        }
        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * Bug Fix by Mediatek Begin.
     *   CR ID: ALPS00116397
     */
    public boolean isMe() {
        ContactDetailFragment detailFragment =  mContactDetailLayoutController.getDetailFragment();
        if (detailFragment != null) {
            return detailFragment.isMe();
        }
        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * get current inserted sim card info list        
     */
    public static List<SIMInfo> getInsertedSimCardInfoList(Context mContext, boolean reGet) {
        List<SIMInfo> sSimInfoList = null;
        if (reGet || sSimInfoList == null) {
            sSimInfoList = SIMInfo.getInsertedSIMList(mContext); 
        }
        return sSimInfoList;
    }
    /*
     * New Feature  by Mediatek End.
    */
    /*
     * Bug Fix by Mediatek Begin.
     *   Original Android's code:
     *     xxx
     *   CR ID: ALPS00273970
     *   Descriptions: 
     */       
    public static boolean s_isNeedFinish = false;
    public static void finishMyself(boolean result){
        s_isNeedFinish = result;
    }

    /*
     * Bug Fix by Mediatek End.
     */
    /*
     * Bug Fix by Mediatek Begin.
     *   Original Android's code:
     *     xxx
     *   CR ID: ALPS00307025
     *   Descriptions: add new receiver
     */
    private BroadcastReceiver mReceiver = new CallDetailBroadcastReceiver();
    private boolean mShowSimIndicator = false;
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     xxx
         *   CR ID: ALPS00311243 
         *   Descriptions: if it's tablet not use receiver
         */
        boolean isUsingTwoPanes = PhoneCapabilityTester.isUsingTwoPanes(this);
        if(!isUsingTwoPanes && FeatureOption.MTK_GEMINI_SUPPORT) {
            unregisterReceiver(mReceiver);
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }
    private class CallDetailBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"CallDetailBroadcastReceiver, onReceive action = " + action);

            if(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    if (mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
        }
    }
    /*
     * Bug Fix by Mediatek End.
     */

    /*
     * New Feature by Mediatek Begin.
     *   Original Android's code:
     *     
     *   CR ID: ALPS00308657
     *   Descriptions: RCS
     */
    private static ContactExtention mContactExtention;
    /*
     * New Feature by Mediatek End.
     */
}
