/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.android.mms.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import com.android.internal.telephony.ITelephony;
import com.android.mms.MmsConfig;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.transaction.CBMessagingNotification;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsRejectedReceiver;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.transaction.MmsSystemEventReceiver.OnSimInforChangedListener;
import com.android.mms.util.DraftCache;
import com.android.mms.util.Recycler;
import com.google.android.mms.pdu.PduHeaders;
import com.mediatek.featureoption.FeatureOption;
import android.database.sqlite.SqliteWrapper;
import android.provider.Settings;
import android.app.StatusBarManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnDismissListener;
import android.app.SearchableInfo;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.Sms;
import android.provider.Telephony.WapPush;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.provider.Telephony.Threads;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.net.Uri;
import android.provider.Telephony.Threads;

import com.mediatek.wappush.SiExpiredCheck;
import com.mediatek.featureoption.FeatureOption;
import java.util.List;
import android.text.TextUtils;
import android.os.SystemProperties; 
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;
/**
 * This activity provides a list view of existing conversations.
 */
public class ConversationList extends ListActivity implements 
        DraftCache.OnDraftChangedListener, OnSimInforChangedListener {
    private static final String TAG = "ConversationList";
    private static final String CONV_TAG = "Mms/convList";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG;

    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    private static final int UNREAD_THREADS_QUERY_TOKEN    = 1702;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;

    // IDs of the context menu items for the list of conversations.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;
    public static final int MENU_VIEW_CONTACT         = 2;
    public static final int MENU_ADD_TO_CONTACTS      = 3;
    public static final int MENU_SIM_SMS              = 4;
    public static final int MENU_CHANGEVIEW           = 6;
    private ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter = null;
    private CharSequence mTitle;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private boolean mNeedToMarkAsSeen;
    private TextView mUnreadConvCount;

    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private StatusBarManager mStatusBarManager;
    private ComponentName mComponentName;
    //wappush: indicates the type of thread, this exits already, but has not been used before
    private int mType;
    //wappush: SiExpired Check
    private SiExpiredCheck siExpiredCheck;
    //wappush: wappush TAG
    private static final String WP_TAG = "Mms/WapPush";
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 100;

    private PostDrawListener mPostDrawListener = new PostDrawListener();
    private MyScrollListener mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "ConversationList_Scroll_Tread");

    // If adapter data is valid
    private boolean mDataValid;
    private boolean mDisableSearchFalg = false;
    private static int mDeleteCounter = 0;
    
    private ModeCallback mActionModeListener = new ModeCallback();
    private ActionMode mActionMode;
    private static String ACTIONMODE = "actionMode";
    
    private int isQuerying = 0;//
    private boolean needQuery = false;//
    private boolean isInActivity = false;//
    
    private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
    	@Override
    	public boolean onPostDraw() {
    		Xlog.i("AppLaunch", "[AppLaunch] MMS onPostDraw");
    		return true;
    	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = ConversationList.this;
        //MTK_OP01_PROTECT_START
        Intent intent;
        boolean cmcc = "OP01".equals(SystemProperties.get("ro.operator.optr"));
        boolean dirMode;
        dirMode = MmsConfig.getMmsDirMode();
        if (cmcc && dirMode) {
            intent = new Intent(this, FolderViewList.class);
            intent.putExtra("floderview_key", FolderViewList.OPTION_INBOX);// show inbox by default
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            finish();
            startActivity(intent);
        }
        //MTK_OP01_PROTECT_END

        //Notify to close dialog mode screen
        if (cmcc) {
            closeMsgDialog();
        }
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.conversation_list_screen);

        mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        mComponentName = getComponentName();
        mQueryHandler = new ThreadListQueryHandler(getContentResolver());

        ListView listView = getListView();
        listView.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
        listView.setOnKeyListener(mThreadListKeyListener);
        listView.setOnScrollListener(mScrollListener);

        // Tell the list view which view to display when the list is empty
        View emptyView = findViewById(R.id.empty);
        listView.setEmptyView(emptyView);

        
        listView.setOnItemLongClickListener(new ItemLongClickListener());
        
        initListAdapter();

        setupActionBar();

        mTitle = getString(R.string.app_label);

        mHandler = new Handler();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
        if (DEBUG) Log.v(TAG, "checkedMessageLimits: " + checkedMessageLimits);
        if (!checkedMessageLimits || DEBUG) {
            runOneTimeStorageLimitCheckForLegacyMessages();
        }
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck = new SiExpiredCheck(this);
            siExpiredCheck.startSiExpiredCheckThread();
        }
    }

    //Notify to close dialog mode screen
    private void closeMsgDialog() {
    	Xlog.d(TAG, "ConversationList.closeMsgDialog");
        Intent intent = new Intent();
        intent.setAction("com.android.mms.dialogmode.VIEWED");
        sendBroadcast(intent);
    }
    
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();

        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.conversation_list_actionbar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(v,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.RIGHT));

        mUnreadConvCount = (TextView)v.findViewById(R.id.unread_conv_count);
    }

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            Log.d(TAG,"onContentChanged : isInActivity ="+isInActivity +"isQuerying ="+isQuerying +
                "needQuery ="+needQuery);
            if (isInActivity) {
                needQuery = true;
                if (isQuerying == 0) {
                    startAsyncQuery();
                }
            }
        }
    };

    private void initListAdapter() {
        Xlog.d(TAG, "initListAdapter");
        if (mListAdapter == null) {
            Xlog.d(TAG, "create it");
            mListAdapter = new ConversationListAdapter(this, null);
            mListAdapter.setOnContentChangedListener(mContentChangedListener);
            setListAdapter(mListAdapter);
            getListView().setRecyclerListener(mListAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(this)) {
            if (DEBUG) Log.v(TAG, "recycler is already turned on");
            // The recycler is already turned on. We don't need to check anything or warn
            // the user, just remember that we've made the check.
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                if (Recycler.checkForThreadsOverLimit(ConversationList.this)) {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit TRUE");
                    // Dang, one or more of the threads are over the limit. Show an activity
                    // that'll encourage the user to manually turn on the setting. Delay showing
                    // this activity until a couple of seconds after the conversation list appears.
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(ConversationList.this,
                                    WarnOfStorageLimitsActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);
                }// else {
                 //   if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit silently turning on recycler");
                 //     No threads were over the limit. Turn on the recycler by default.
                 //   runOnUiThread(new Runnable() {
                 //       public void run() {
                 //           SharedPreferences.Editor editor = mPrefs.edit();
                 //           editor.putBoolean(MessagingPreferenceActivity.AUTO_DELETE, true);
                 //           editor.apply();
                 //       }
                 //   });
                 // }
                // Remember that we don't have to do the check anymore when starting MMS.
                runOnUiThread(new Runnable() {
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
            }
        }).start();
    }

    /**
     * Mark in preferences that we've checked the user's message limits. Once checked, we'll
     * never check them again, unless the user wipe-data or resets the device.
     */
    private void markCheckedMessageLimit() {
        if (DEBUG) Log.v(TAG, "markCheckedMessageLimit");
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(CHECKED_MESSAGE_LIMITS, true);
        editor.apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
        startAsyncQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.startExpiredCheck();
        }
        ComposeMessageActivity.mDestroy = true;

        Handler mShowIndicatorHandler = new Handler();
        final ComponentName name = getComponentName();
        mIsShowSIMIndicator = true;
        mStatusBarManager.hideSIMIndicator(name);
        mStatusBarManager.showSIMIndicator(name, Settings.System.SMS_SIM_SETTING);
    }

    @Override
    protected void onPause() {
    	mStatusBarManager.hideSIMIndicator(getComponentName());
    	mIsShowSIMIndicator = false;
    	super.onPause();
    }
    @Override
    protected void onStart() {
        super.onStart();
        MmsConfig.setMmsDirMode(false);
        Xlog.i(TAG,"[Performance test][Mms] loading data start time ["
            + System.currentTimeMillis() + "]" );
        getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        MessagingNotification.cancelNotification(getApplicationContext(),
                SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);

        DraftCache.getInstance().addOnDraftChangedListener(this);
        mNeedToMarkAsSeen = true;
        startAsyncQuery();
        isInActivity = true;

        if (mListAdapter != null) {
            Xlog.d(TAG, "set onContentChanged listener");
            mListAdapter.setOnContentChangedListener(mContentChangedListener);
        }
        
        // We used to refresh the DraftCache here, but
        // refreshing the DraftCache each time we go to the ConversationList seems overly
        // aggressive. We already update the DraftCache when leaving CMA in onStop() and
        // onNewIntent(), and when we delete threads or delete all in CMA or this activity.
        // I hope we don't have to do such a heavy operation each time we enter here.
        // new: third party may add/delete draft, and we must refresh to check this.
        DraftCache.getInstance().refresh();
        // we invalidate the contact cache here because we want to get updated presence
        // and any contact changes. We don't invalidate the cache by observing presence and contact
        // changes (since that's too untargeted), so as a tradeoff we do it here.
        // If we're in the middle of the app initialization where we're loading the conversation
        // threads, don't invalidate the cache because we're in the process of building it.
        // TODO: think of a better way to invalidate cache more surgically or based on actual
        // TODO: changes we care about
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isInActivity = false;
        getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        DraftCache.getInstance().removeOnDraftChangedListener(this);
        if (mListAdapter != null) {
            Xlog.d(TAG, "remove OnContentChangedListener");
            mListAdapter.setOnContentChangedListener(null);
        }
        
        // Simply setting the choice mode causes the previous choice mode to finish and we exit
        // multi-select mode (if we're in it) and remove all the selections.
        
        
        //wappush: stop expiration check when exit
        Xlog.i(WP_TAG, "ConversationList: " + "stopExpiredCheck");
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopExpiredCheck();
        }
    }

    @Override
    protected void onDestroy() {
        Xlog.d(TAG,"onDestroy");

        if (mQueryHandler != null) {
            mQueryHandler.removeCallbacksAndMessages(null);
            mQueryHandler.cancelOperation(THREAD_LIST_QUERY_TOKEN);
            mQueryHandler.cancelOperation(UNREAD_THREADS_QUERY_TOKEN);
        }
        if (mListAdapter != null) {
            Xlog.d(TAG, "clear it");
            mListAdapter.changeCursor(null);
        }
        
        //stop the si expired check thread
        if(FeatureOption.MTK_WAPPUSH_SUPPORT){
            siExpiredCheck.stopSiExpiredCheckThread();
        }
        mScrollListener.destroyThread();
        super.onDestroy();
    }

    @Override
    protected void  onSaveInstanceState  (Bundle outState){
        super.onSaveInstanceState(outState);
        if(mActionMode != null){
            outState.putBoolean(ACTIONMODE, true);
        }
    }
    
    protected void  onRestoreInstanceState  (Bundle state){
        super.onRestoreInstanceState(state);
        boolean isActionMode = state.getBoolean(ACTIONMODE, false);
        if(isActionMode){
            mActionMode = this.startActionMode(mActionModeListener);
        }
    }
    

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            public void run() {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                }
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mDisableSearchFalg){
            switch (keyCode) {
                case KeyEvent.KEYCODE_SEARCH:
                    // do nothing since we don't want search box which may cause UI crash
                    // TODO: mapping to other useful feature
                    return true;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }   
    
    private void startAsyncQuery() {
        try {
            needQuery= false;
            setTitle(getString(R.string.refreshing));
            setProgressBarIndeterminateVisibility(true);

            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
            isQuerying++;
            Conversation.startQuery(mQueryHandler, UNREAD_THREADS_QUERY_TOKEN, Threads.READ + "=0");
            isQuerying++;
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
            Intent intent = new Intent();
            intent.setClass(ConversationList.this, SearchActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            mSearchItem.collapseActionView();
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        menu.removeItem(R.id.action_siminfo);
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchItem.getActionView();

        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setIconifiedByDefault(true);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(this.getComponentName());
            mSearchView.setSearchableInfo(info);
        }
        //MTK_OP01_PROTECT_START
        if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
            menu.add(0, MENU_CHANGEVIEW, 0, R.string.changeview);
        }
        //MTK_OP01_PROTECT_END
        
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP02_PROTECT_START
        if ("OP02".equals(optr)) {
            menu.add(0, MENU_SIM_SMS, 0, R.string.menu_sim_sms).setIcon(
                    R.drawable.ic_menu_sim_sms);
            MenuItem item = menu.findItem(MENU_SIM_SMS);
            List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
            if(listSimInfo == null || listSimInfo.isEmpty()){
                item.setEnabled(false);
                Log.d(TAG, "onCreateOptionsMenu MenuItem setEnabled(false)  "+"optr ="+optr);
            }
        }
        //MTK_OP02_PROTECT_END
        //MTK_OP01_PROTECT_START
        else if ("OP01".equals(optr)) {
            menu.add(0, MENU_SIM_SMS, 0, R.string.menu_sim_sms).setIcon(
                    R.drawable.ic_menu_sim_sms);
            MenuItem item = menu.findItem(MENU_SIM_SMS);
            List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
            if(listSimInfo == null || listSimInfo.isEmpty()){
                item.setEnabled(false);
                Log.d(TAG, "onCreateOptionsMenu MenuItem setEnabled(false)  "+"optr ="+optr);
            }
        }
        //MTK_OP01_PROTECT_END

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_delete_all);
        if (item != null) {
            mDataValid = mListAdapter.isDataValid();
            item.setVisible(mListAdapter.getCount() > 0);
        }
        if (!LogTag.DEBUG_DUMP) {
            item = menu.findItem(R.id.action_debug_dump);
            if (item != null) {
                item.setVisible(false);
            }
        }
        item = menu.findItem(R.id.action_omacp);
        item.setVisible(false);
        Context otherAppContext = null;
        try{
            otherAppContext = this.createPackageContext("com.mediatek.omacp", 
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch(Exception e) {
            Xlog.e(CONV_TAG, "ConversationList NotFoundContext");
        }
        if (null != otherAppContext) {
            SharedPreferences sp = otherAppContext.getSharedPreferences("omacp", 
                    MODE_WORLD_READABLE | MODE_MULTI_PROCESS);
            boolean omaCpShow = sp.getBoolean("configuration_msg_exist", false);
            if(omaCpShow) {  
                item.setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        mSearchItem.expandActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_compose_new:
                createNewMessage();
                break;
            case R.id.action_delete_all:
                // The invalid threadId of -1 means all threads here.
                confirmDeleteThread(-1L, mQueryHandler);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                startActivityIfNeeded(intent, -1);
                break;
            //MTK_OP02_PROTECT_START
            case MENU_SIM_SMS:
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                    List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
                    if (listSimInfo.size() > 1) { 
                        Intent simSmsIntent = new Intent();
                        simSmsIntent.setClass(this, SelectCardPreferenceActivity.class);
                        simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        simSmsIntent.putExtra("preference", MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES);
                        simSmsIntent.putExtra("preferenceTitle", getString(R.string.pref_title_manage_sim_messages));
                        startActivity(simSmsIntent);
                    } else {  
                        Intent simSmsIntent = new Intent();
                        simSmsIntent.setClass(this, ManageSimMessages.class);
                        simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        simSmsIntent.putExtra("SlotId", listSimInfo.get(0).mSlot); 
                        startActivity(simSmsIntent);
                    }
                } else { 
                    startActivity(new Intent(this, ManageSimMessages.class));
                }
                break;
            //MTK_OP02_PROTECT_END
            case R.id.action_omacp:
                Intent omacpintent = new Intent();
                omacpintent.setClassName("com.mediatek.omacp", "com.mediatek.omacp.message.OmacpMessageList");
                omacpintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityIfNeeded(omacpintent, -1);
                break;
            case R.id.action_debug_dump:
                LogTag.dumpInternalTables(this);
                break;
            //MTK_OP01_PROTECT_START
            case MENU_CHANGEVIEW:
                MmsConfig.setMmsDirMode(true);
                MessageUtils.updateNotification(this);
                Intent it = new Intent(this, FolderViewList.class);
                it.putExtra("floderview_key", FolderViewList.OPTION_INBOX);// show inbox by default
                startActivity(it);
                finish();
                break;
            //MTK_OP01_PROTECT_END
            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Note: don't read the thread id data from the ConversationListItem view passed in.
        // It's unreliable to read the cached data stored in the view because the ListItem
        // can be recycled, and the same view could be assigned to a different position
        // if you click the list item fast enough. Instead, get the cursor at the position
        // clicked and load the data from the cursor.
        // (ConversationListAdapter extends CursorAdapter, so getItemAtPosition() should
        // return the cursor object, which is moved to the position passed in)
        Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            //klocwork issue pid:18182
            if (cursor == null) {
                return;
            }
        Conversation conv = Conversation.from(this, cursor);
        
        if(mActionMode != null){
            boolean checked = conv.isChecked();            
            mActionModeListener.setItemChecked(position, !checked);
            if(mListAdapter != null){
                mListAdapter.notifyDataSetChanged();
            }
            return;
        }
        
        
        long tid = conv.getThreadId();

        //wappush: modify the calling of openThread, add one parameter
        if (LogTag.VERBOSE) {
            Log.d(TAG, "onListItemClick: pos=" + position + ", view=" + v + ", tid=" + tid);
        }

        Xlog.i(WP_TAG, "ConversationList: " + "conv.getType() is : " + conv.getType());
        openThread(tid, conv.getType());
    }

    private void createNewMessage() {
        startActivity(ComposeMessageActivity.createIntent(this, 0));
    }

    private void openThread(long threadId, int type) {
        if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
            //wappush: add opptunities for starting wappush activity if it is a wappush thread 
            //type: Threads.COMMON_THREAD, Threads.BROADCAST_THREAD and Threads.WAP_PUSH
            if(type == Threads.WAPPUSH_THREAD){
                startActivity(WPMessageActivity.createIntent(this, threadId));            
            } else if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(this, threadId));                
            } else {
                startActivity(ComposeMessageActivity.createIntent(this, threadId));
            }
        }else{
            if (type == Threads.CELL_BROADCAST_THREAD) {
                startActivity(CBMessageListActivity.createIntent(this, threadId));                
            } else {
                startActivity(ComposeMessageActivity.createIntent(this, threadId));
            }
        }
    }

    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        return intent;
    }

    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            Cursor cursor = mListAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            //wappush: get the added mType value
            mType = conv.getType();
            Xlog.i(WP_TAG, "ConversationList: " + "mType is : " + mType);   

            ContactList recipients = conv.getRecipients();
            menu.setHeaderTitle(recipients.formatNames(","));

            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.add(0, MENU_VIEW, 0, R.string.menu_view);

            // Only show if there's a single recipient
            if (recipients.size() == 1) {
                // do we have this recipient in contacts?
                if (recipients.get(0).existsInDatabase()) {
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact);
                } else {
                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts);
                }
            }
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            long threadId = conv.getThreadId();
            switch (item.getItemId()) {
            case MENU_DELETE: {
                confirmDeleteThread(threadId, mQueryHandler);
                break;
            }
            case MENU_VIEW: {
                openThread(threadId, mType);
                break;
            }
            case MENU_VIEW_CONTACT: {
                Contact contact = conv.getRecipients().get(0);
                Intent intent = new Intent(Intent.ACTION_VIEW, contact.getUri());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
                break;
            }
            case MENU_ADD_TO_CONTACTS: {
                String address = conv.getRecipients().get(0).getNumber();
                startActivity(createAddContactIntent(address));
                break;
            }
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // We override this method to avoid restarting the entire
        // activity when the keyboard is opened (declared in
        // AndroidManifest.xml).  Because the only translatable text
        // in this activity is "New Message", which has the full width
        // of phone to work with, localization shouldn't be a problem:
        // no abbreviated alternate words should be needed even in
        // 'wide' languages like German or Russian.

        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.v(TAG, "onConfigurationChanged: " + newConfig);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting a thread,
     * but first start a background query to see if any of the threads or thread
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadId id of the thread to delete or -1 for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThread(long threadId, AsyncQueryHandler handler) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        confirmDeleteThreads(threadIds, handler);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting threads,
     * but first start a background query to see if any of the threads
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadIds list of threadIds to delete or null for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThreads(Collection<Long> threadIds, AsyncQueryHandler handler) {
        Conversation.startQueryHaveLockedMessages(handler, threadIds,
                HAVE_LOCKED_MESSAGES_TOKEN);
    }

    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting single/multiple threads or all threads.
     * @param listener gets called when the delete button is pressed
     * @param deleteAll whether to show a single thread or all threads UI
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener,
            Collection<Long> threadIds,
            boolean hasLockedMessages,
            Context context) {
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        TextView msg = (TextView)contents.findViewById(R.id.message);

        if (threadIds == null) {
            msg.setText(R.string.confirm_delete_all_conversations);
        } else {
            // Show the number of threads getting deleted in the confirmation dialog.
            int cnt = threadIds.size();
            msg.setText(context.getResources().getQuantityString(
                R.plurals.confirm_delete_conversation, cnt, cnt));
        }

        final CheckBox checkbox = (CheckBox)contents.findViewById(R.id.delete_locked);
        if (!hasLockedMessages) {
            checkbox.setVisibility(View.GONE);
        } else {
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }
        Cursor cursor = null;
    	int smsId = 0;
    	int mmsId = 0;
        cursor = context.getContentResolver().query(Sms.CONTENT_URI,
                new String[] {"max(_id)"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    smsId = cursor.getInt(0);
                    Xlog.d(TAG, "confirmDeleteThreadDialog max SMS id = " + smsId);
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        }
        cursor = context.getContentResolver().query(Mms.CONTENT_URI,
                new String[] {"max(_id)"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    mmsId = cursor.getInt(0);
                    Xlog.d(TAG, "confirmDeleteThreadDialog max MMS id = " + mmsId);
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        }
        listener.setMaxMsgId(mmsId, smsId);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_dialog_title)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setCancelable(true)
            .setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.no, null)
            .setView(contents)
            .show();
    }

    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            confirmDeleteThread(id, mQueryHandler);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };

    public static class DeleteThreadListener implements OnClickListener {
        private final Collection<Long> mThreadIds;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteLockedMessages;
        private int maxMmsId;
        private int maxSmsId;

        public DeleteThreadListener(Collection<Long> threadIds, AsyncQueryHandler handler,
                Context context) {
            mThreadIds = threadIds;
            mHandler = handler;
            mContext = context;
        }

        public void setMaxMsgId(int mmsId, int smsId) {
            maxMmsId = mmsId;
            maxSmsId = smsId;
        }
        
        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        public void onClick(DialogInterface dialog, final int whichButton) {
            MessageUtils.handleReadReport(mContext, mThreadIds,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                public void run() {
                    showProgressDialog();
                    int token = DELETE_CONVERSATION_TOKEN;
                    if (mThreadIds == null) {
                        //wappush: do not need modify the code here, but delete function in provider has been modified.
                        Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages, maxMmsId, maxSmsId);
                        DraftCache.getInstance().refresh();
                    } else {
                        mDeleteCounter = 0;
                        //wappush: do not need modify the code here, but delete function in provider has been modified.
                        for (long threadId : mThreadIds) {
                            mDeleteCounter++;
                            Conversation.startDelete(mHandler, token, mDeleteLockedMessages,
                                    threadId, maxMmsId, maxSmsId);
                            DraftCache.getInstance().setDraftState(threadId, false);
                        }
                        Xlog.d(TAG, "mDeleteCounter = "+mDeleteCounter);
                    }
                }
                private void showProgressDialog() {
                    if (mHandler instanceof BaseProgressQueryHandler) {
                        ((BaseProgressQueryHandler) mHandler).setProgressDialog(
                                DeleteProgressDialogUtil.getProgressDialog(mContext));
                        ((BaseProgressQueryHandler) mHandler).showProgressDialog();
                    }
                }
            });
            dialog.dismiss();
        }
    }
    
    /**
     * The base class about the handler with progress dialog function.
     */
    public static abstract class BaseProgressQueryHandler extends AsyncQueryHandler {
        private NewProgressDialog dialog;
        private int progress;
        
        public BaseProgressQueryHandler(ContentResolver resolver) {
            super(resolver);
        }
        
        /**
         * Sets the progress dialog.
         * @param dialog the progress dialog.
         */
        public void setProgressDialog(NewProgressDialog dialog) {
            this.dialog = dialog;
        }
        
        /**
         * Sets the max progress.
         * @param max the max progress.
         */
        public void setMax(int max) {
            if (dialog != null) {
                dialog.setMax(max);
            }
        }
        
        /**
         * Shows the progress dialog. Must be in UI thread.
         */
        public void showProgressDialog() {
            if (dialog != null) {
                dialog.show();
            }
        }
        
        /**
         * Rolls the progress as + 1.
         * @return if progress >= max.
         */
        protected boolean progress() {
            if (dialog != null) {
                return ++progress >= dialog.getMax();
            } else {
                return false;
            }
        }
        
        /**
         * Dismisses the progress dialog.
         */
        protected void dismissProgressDialog() {
            try {
                dialog.setDismiss(true);
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dialog = null;
            }
        }
    }

    private final Runnable mDeleteObsoleteThreadsRunnable = new Runnable() {
        public void run() {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("mDeleteObsoleteThreadsRunnable getSavingDraft(): " +
                        DraftCache.getInstance().getSavingDraft());
            }
            if (DraftCache.getInstance().getSavingDraft()) {
                // We're still saving a draft. Try again in a second. We don't want to delete
                // any threads out from under the draft.
                mHandler.postDelayed(mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                MessageUtils.asyncDeleteOldMms();
                Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                        DELETE_OBSOLETE_THREADS_TOKEN);
            }
        }
    };

    private final class ThreadListQueryHandler extends BaseProgressQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
              
            if (cursor == null) {
              setTitle(mTitle);
              setProgressBarIndeterminateVisibility(false); 
              if (token == THREAD_LIST_QUERY_TOKEN || token == UNREAD_THREADS_QUERY_TOKEN) {
                  Log.d("TAG","onQueryComplete cursor == null isQuerying ="+isQuerying);
                  isQuerying--;
              }
              if (needQuery && isInActivity && isQuerying == 0) {
                  Log.d("TAG","onQueryComplete cursor == null startAsyncQuery");
                  startAsyncQuery();
              }
            	   return;
            }
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                isQuerying--;
                Log.d("TAG","onQueryComplete THREAD_LIST_QUERY_TOKEN isQuerying = "+isQuerying);
                    /* After deleting a conversation, The ListView may refresh again.
                     * Because the cursor is not changed before query again, it may
                     * cause that the deleted threads's data is added in the cache again
                     * by ConversationListAdapter::bindView().
                     * We need to remove the not existed conversation in cache*/
                //Conversation.removeInvalidCache(cursor);
                if (mListAdapter.getOnContentChangedListener() == null) {
                    cursor.close();
                    return;
                }
                
                mListAdapter.changeCursor(cursor);
                if (!mDataValid) {
                    invalidateOptionsMenu();
                }
                setTitle(mTitle);
                setProgressBarIndeterminateVisibility(false);  
                if (!Conversation.isInitialized()) {
                    Conversation.init(getApplicationContext());
                }else{
                    Conversation.removeInvalidCache(cursor);
                }
                
                if (mNeedToMarkAsSeen) {
                    mNeedToMarkAsSeen = false;
                    Conversation.markAllConversationsAsSeen(getApplicationContext(),
                            Conversation.MARK_ALL_MESSAGE_AS_SEEN);

                    // Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables. We only call
                    // this on the first query (because of mNeedToMarkAsSeen).
                    mHandler.post(mDeleteObsoleteThreadsRunnable);
                }
                
                if(mActionMode != null){
                    mActionModeListener.confirmSyncCheckedPositons();
                }
                break;

            case UNREAD_THREADS_QUERY_TOKEN:
                isQuerying--;
                Log.d("TAG","onQueryComplete UNREAD_THREADS_QUERY_TOKEN isQuerying = "+isQuerying);
                int count = cursor.getCount();
                mUnreadConvCount.setText(count > 0 ? Integer.toString(count) : null);
                if (FeatureOption.MTK_THEMEMANAGER_APP) {
                    Resources res = getResources();
                    int textColor = res.getThemeMainColor();
                    if (textColor != 0) {
                        mUnreadConvCount.setTextColor(textColor);
                    }
                }
                cursor.close();
                break;

            case HAVE_LOCKED_MESSAGES_TOKEN:
            	Log.d(TAG, "onQueryComplete HAVE_LOCKED_MESSAGES_TOKEN" );
                Collection<Long> threadIds = (Collection<Long>)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(threadIds, mQueryHandler,
                        ConversationList.this), threadIds,
                        cursor != null && cursor.getCount() > 0,
                        ConversationList.this);
                cursor.close();
                break;

            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
            if (needQuery && isInActivity && (isQuerying == 0)) {
                startAsyncQuery();
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // When this callback is called after deleting, token is 1803(DELETE_OBSOLETE_THREADS_TOKEN)
            // not 1801(DELETE_CONVERSATION_TOKEN)
            switch (token) {
            case DELETE_CONVERSATION_TOKEN:
                if (mDeleteCounter > 1) {
                    mDeleteCounter--;
                    Xlog.d(TAG, "igonre a onDeleteComplete,mDeleteCounter:"+mDeleteCounter);
                    return;
                }
                mDeleteCounter = 0;
                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                Contact.init(ConversationList.this);

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ConversationList.this);

                try {
                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    if(phone != null) {
                        if(phone.isTestIccCard()) {
                            Xlog.d(CONV_TAG, "All threads has been deleted, send notification..");
                            SmsManager.getDefault().setSmsMemoryStatus(true);
                        }
                    } else {
                        Xlog.d(CONV_TAG, "Telephony service is not available!");
                    }
                } catch(Exception ex) {
                    Xlog.e(CONV_TAG, " " + ex.getMessage());
                }
                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(ConversationList.this);
                MessagingNotification.updateDownloadFailedNotification(ConversationList.this);

                //Update the notification for new WAP Push messages
                if(FeatureOption.MTK_WAPPUSH_SUPPORT){
                    WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,false);
                }
                CBMessagingNotification.updateNewMessageIndicator(ConversationList.this);
                // Make sure the list reflects the delete
                //startAsyncQuery();
                Xlog.d(CONV_TAG, "Begin dismiss Dialog");
                dismissProgressDialog();
                break;

            case DELETE_OBSOLETE_THREADS_TOKEN:
                // Nothing to do here.
                break;
            }
        }
    }

    private class ModeCallback implements ActionMode.Callback {
        private View mMultiSelectActionBarView;
        private TextView mSelectedConvCount;
        private HashSet<Long> mSelectedThreadIds;
        private HashSet<Integer> mCheckedPosition;
        private int mCheckedNum = 0;

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            mSelectedThreadIds = new HashSet<Long>();
            mCheckedPosition = new HashSet<Integer>();
            inflater.inflate(R.menu.conversation_multi_select_menu_with_selectall, menu);

            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = (ViewGroup)LayoutInflater.from(ConversationList.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);

                mSelectedConvCount =
                    (TextView)mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
            }
            mode.setCustomView(mMultiSelectActionBarView);
            ((TextView)mMultiSelectActionBarView.findViewById(R.id.title))
                .setText(R.string.select_conversations);
            mDisableSearchFalg = true;
            
            getListView().setLongClickable(false);            
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup)LayoutInflater.from(ConversationList.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);
                mode.setCustomView(v);

                mSelectedConvCount = (TextView)v.findViewById(R.id.selected_conv_count);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    if (mSelectedThreadIds.size() > 0) {
                        Log.v(TAG, "ConversationList->ModeCallback: delete");
                        confirmDeleteThreads(mSelectedThreadIds, mQueryHandler);
                        mode.finish();
                    }else{
                        mHandler.post(new Runnable(){

                            public void run() {
                                // TODO Auto-generated method stub
                                Toast.makeText(ConversationList.this, R.string.no_item_selected, Toast.LENGTH_SHORT).show();
                            }
                            
                        });
                    }
                    
                    break;
                    
                case R.id.select_all:
                    Log.v(TAG, "ConversationList->ModeCallback: select all");
                    setAllItemChecked(mode, true);
                    break;
                    
                case R.id.cancel_select:
                    Log.v(TAG, "ConversationList->ModeCallback: unselect all");
                    setAllItemChecked(mode, false);
                    break;

                default:
                    if (mCheckedPosition != null && mCheckedPosition.size() > 0){
                        mCheckedPosition.clear();
                    }
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            ConversationListAdapter adapter = (ConversationListAdapter)getListView().getAdapter();
            //adapter.uncheckAll();
            adapter.uncheckSelect(mCheckedPosition);
            mSelectedThreadIds = null;
            mCheckedPosition = null;
            mDisableSearchFalg = false;
            
            getListView().setLongClickable(true);
            mCheckedNum = 0;
            mActionMode = null;
            if(mListAdapter != null){
                mListAdapter.notifyDataSetChanged();
            }
        }

        public void setItemChecked(int position, boolean checked){
            ListView listView = getListView();
            Cursor cursor  = (Cursor)listView.getItemAtPosition(position);
            Conversation conv = Conversation.getFromCursor(ConversationList.this, cursor);
            if(checked == conv.isChecked()){
                return;
            }
            conv.setIsChecked(checked);
            
            long threadId = conv.getThreadId();
            if (checked) {
                mSelectedThreadIds.add(threadId);
                mCheckedPosition.add(position);
                mCheckedNum ++;
            } else {
                mSelectedThreadIds.remove(threadId);
                mCheckedPosition.remove(position);
                mCheckedNum --;
            }
            
            mSelectedConvCount.setText(Integer.toString(mCheckedNum));
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                Resources res = getResources();
                int textColor = res.getThemeMainColor();
                if (textColor != 0) {
                    mSelectedConvCount.setTextColor(textColor);
                }
            }
        }
        
        
        private void setAllItemChecked(ActionMode mode, boolean checked){
            ListView listView = getListView();
            int num = listView.getCount();
            for (int position = 0; position< num ; position++){
                setItemChecked(position, checked);
        }

            if(mListAdapter != null){
                mListAdapter.notifyDataSetChanged();
            }
        }
        
        /**
         * after adater's cursor changed, must sync witch mCheckedPosition
         * for one Scenario: a new message with a new thread id comes when user are selecting items
         */
        public void confirmSyncCheckedPositons(){
            
            mCheckedPosition.clear();
            mSelectedThreadIds.clear();
            ListView listView = getListView();
            int num = listView.getCount();
            
            for(int position= 0; position < num; position ++){
                Cursor cursor  = (Cursor)listView.getItemAtPosition(position);
                Conversation conv = Conversation.from(ConversationList.this, cursor);
                if(conv.isChecked()){
                   mCheckedPosition.add(position);
                   mSelectedThreadIds.add(conv.getThreadId());
                }
    }
            mCheckedNum = mCheckedPosition.size();
            mSelectedConvCount.setText(Integer.toString(mCheckedNum));
        }
    }

    private void log(String format, Object... args) {
        String s = String.format(format, args);
        Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
    }

    private boolean mIsShowSIMIndicator = true;
    @Override
    public void OnSimInforChanged() {
        Xlog.i(MmsApp.LOG_TAG, "OnSimInforChanged(): Conversation List");
        // show SMS indicator
        if (!isFinishing() && mIsShowSIMIndicator) {
            Xlog.i(MmsApp.LOG_TAG, "Hide current indicator and show new one.");
            mStatusBarManager.hideSIMIndicator(mComponentName);
            mStatusBarManager.showSIMIndicator(mComponentName, Settings.System.SMS_SIM_SETTING);
        }
    }

    private static Activity mActivity = null;
    public static Activity getContext() {
        return mActivity;
    }
 
    class ItemLongClickListener implements  ListView.OnItemLongClickListener {

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            mActionMode = startActionMode(mActionModeListener);
            Log.e(TAG, "OnItemLongClickListener");
            mActionModeListener.setItemChecked(position, true);
            if(mListAdapter != null){
                mListAdapter.notifyDataSetChanged();
            }
            return true;
        }
    }
}
