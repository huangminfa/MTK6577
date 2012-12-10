package com.mediatek.contacts.dialpad;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.DialerSearch;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.ContactsApplication;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.PhoneNumberFormatter.PhoneNumberFormattingTextWatcherEx;
import com.mediatek.contacts.CallOptionHandler;

public class DialerSearchController extends AsyncQueryHandler {

    private static final String TAG = "DialerSearchController";
    private static final boolean DBG = true;

private static final String EMPTY_NUMBER = "";
    
    public static final int DIALER_SEARCH_MODE_ALL = 0;
    public static final int DIALER_SEARCH_MODE_NUMBER = 1;

    private static final int DS_MSG_CONTACTS_DELETE_CHANGED = 1000;
    private static final int DS_MSG_DELAY_TIME = 1000;

    private static final int QUERY_TOKEN_INIT = 30;
    private static final int QUERY_TOKEN_NULL = 40;
    private static final int QUERY_TOKEN_INCREMENT = 50;
    private static final int QUERY_TOKEN_SIMPLE = 60;

    public static final String[] DIALER_SEARCH_PROJECTION = {
        DialerSearch.NAME_LOOKUP_ID,
        DialerSearch.CONTACT_ID ,
        DialerSearch.CALL_DATE,
        DialerSearch.CALL_LOG_ID,
        DialerSearch.CALL_TYPE,
        DialerSearch.SIM_ID,
        DialerSearch.INDICATE_PHONE_SIM,
        DialerSearch.CONTACT_STARRED,
        DialerSearch.PHOTO_ID,
        DialerSearch.SEARCH_PHONE_TYPE,
        DialerSearch.NAME, 
        DialerSearch.SEARCH_PHONE_NUMBER,
        DialerSearch.CONTACT_NAME_LOOKUP,
        DialerSearch.MATCHED_DATA_OFFSETS,
        DialerSearch.MATCHED_NAME_OFFSETS,
        DialerSearch.IS_SDN_CONTACT
   };

    protected Context mContext;

    protected EditText mDigits;
    protected ListView mListView;
    protected DialerSearchAdapter mAdapter;

    protected Queue<Integer> mSearchNumCntQ = new LinkedList<Integer>();
    protected int mPrevQueryDigCnt;
    protected int mDialerSearchCursorCount;
    protected boolean mQueryComplete;
    protected int noResultDigCnt;
    protected boolean noMoreResult;

    protected boolean mFormatting;
    protected int searchMode; 
    protected boolean mSearchNumberOnly;
    protected boolean mChangeInMiddle;
    protected String mDigitString;

    protected Uri mSelectedContactUri;

    protected ContactsPreferences mContactsPrefs;
    protected int mDisplayOrder;
    protected int mSortOrder;

    protected OnDialerSearchResult mOnDialerSearchResult;
    CallLogContentObserver mCallLogContentObserver;
    ContactContentObserver mFilterContentObserver;
    
    private boolean mIsForeground = false;

    private boolean mDataChanged = false;

    private boolean mDigitsFilled = false;
    
    private static int delCount=0;

    private String mPreviousText;
    
    private DsTextWatcher mDsTextWatcher = null;

    public DialerSearchController (Context context, ListView listView,
                                   DialerSearchAdapter.Listener listener,
                                   CallOptionHandler callOptionHandler) {
        super(context.getContentResolver());
        mContext = context;
        mListView = listView;
        mAdapter = new DialerSearchAdapter(context, listener, callOptionHandler);
        mListView.setAdapter(mAdapter);

        mCallLogContentObserver = new CallLogContentObserver();
        mFilterContentObserver = new ContactContentObserver();
        
        mContactsPrefs = new ContactsPreferences(context);
        mContactsPrefs.registerChangeListener(new ContactsPreferences.ChangeListener() {
            public void onChange() {
                log("contacts display or sort order changed");
                if (mIsForeground) {
                    if (mDigits != null) {
                        if(mDigits.length() > 0) {
                            // clear the text will trigger startQuery
                            mDataChanged = true;
                        } else {
                            startQuery(null, DIALER_SEARCH_MODE_ALL);
                        }
                    }
                } else {
                    mDataChanged = true;
                }
            }
        });
    }

    public DialerSearchController (Context context, ListView listView, DialerSearchAdapter.Listener listener) {
        this(context, listView, listener, null);
    }

    public void setDialerSearchTextWatcher(EditText digits) {
        mDigits = digits;
        
        if (mDsTextWatcher == null) {
            mDsTextWatcher = new DsTextWatcher();
            mDigits.addTextChangedListener(mDsTextWatcher);
        }

        mContext.getContentResolver().registerContentObserver(
                Uri.parse("content://com.android.contacts.dialer_search/callLog/"), true,
                mCallLogContentObserver);
        mContext.getContentResolver().registerContentObserver(
                Uri.parse("content://com.android.contacts/dialer_search/filter/"), true,
                mFilterContentObserver);
    }

    public void setOnDialerSearchResult(OnDialerSearchResult dialerSearchResult) {
        mOnDialerSearchResult = dialerSearchResult;
    }

    DialerSearchResult obtainDialerSearchResult(int count) {
        DialerSearchResult dialerSearchResult = new DialerSearchResult();
        dialerSearchResult.count = count;
        return dialerSearchResult;
    }

    public void configureFromIntent(boolean digitsFilled) {
        mDigitsFilled = digitsFilled;
    }
    public void onResume() {
        log("onResume");
        if(mAdapter != null)
            mAdapter.onResume();

        if(mDigits.getText().length() == 0) {
            log("DialerSearchController onResume startQuery");
            startQuery(null, DIALER_SEARCH_MODE_ALL);
        } else {
            log("DialerSearchController onResume with digits");
            if (mDigitsFilled) {
                //digitest filled
                log("DialerSearchController mDigitsFilled" + mDigitsFilled);
                mDigitsFilled = false;
            } else  if (mDataChanged) {
                log("DialerSearchController onResume with digits, refresh the data");
                mDigits.setText(null);
                //startQuery(mDigits.getText().toString(), DIALER_SEARCH_MODE_ALL);
//                startQuery(null, DIALER_SEARCH_MODE_ALL);
	    }else if(mAdapter.needClearDigits){
	        mDigits.setText(null);
	        mAdapter.needClearDigits = false;
            }
        }
        mDataChanged = false;
        mIsForeground = true;
    }
    
    public void onPause() {
        log("onPause");
        mIsForeground = false;
    }
    
    public void onStop() {
        log("onStop");
        if(mDigits.getText().length() > 0) {
            log("DialerSearchController onStop");
            //mDigits.setText(EMPTY_NUMBER);
        }
    }

    public void onDestroy() {
        if(mCallLogContentObserver != null)
            mContext.getContentResolver().unregisterContentObserver(mCallLogContentObserver);
        
        if(mFilterContentObserver != null) {
            log("DialerSearchController onDestroy : unregister the filter observer.");
            mContext.getContentResolver().unregisterContentObserver(mFilterContentObserver);
        }
        
        if (mDsTextWatcher != null) {
            if (mDigits != null) {
                mDigits.removeTextChangedListener(mDsTextWatcher);
            }
            mDsTextWatcher = null;
        }
        
        if(mContactsPrefs != null)
            mContactsPrefs.unregisterChangeListener();
    }

    public void startQuery(String searchContent, int mode) {
        if (ContactsApplication.getInstance().sDialerSearchSupport) {
            log("startQuery searchContent: "+searchContent+" mode: "+mode);
            searchContent = DialerSearchUtils.tripHyphen(searchContent);
            noMoreResult = (noResultDigCnt > 0 && mDigits.getText().length() > noResultDigCnt) ? true : false;
            log("noResultDigCnt: " + noResultDigCnt + " || mDigits.getText(): " + mDigits.getText());
            mQueryComplete = false;
            if (searchContent == null) {
                mDisplayOrder = mContactsPrefs.getDisplayOrder();
                mSortOrder = mContactsPrefs.getSortOrder();
                startQuery(QUERY_TOKEN_INIT, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/init#" + mDisplayOrder+"#"+mSortOrder), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (searchContent.equals("NULL_INPUT")) {
                startQuery(QUERY_TOKEN_NULL, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/null_input"), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(0));
            } else if (mode == DIALER_SEARCH_MODE_ALL) {
                if (!noMoreResult) {
                    startQuery(QUERY_TOKEN_INCREMENT, null, 
                        Uri.parse("content://com.android.contacts/dialer_search/filter/"+searchContent), 
                        DIALER_SEARCH_PROJECTION, null, null, null);
                    mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
                }
            } else if (mode == DIALER_SEARCH_MODE_NUMBER) {
                // won't check noMoreResult for search number mode, since if edit in middle will invoke no search result!
                startQuery(QUERY_TOKEN_SIMPLE, null, 
                    Uri.parse("content://com.android.contacts/dialer_search_number/filter/"+searchContent), 
                    DIALER_SEARCH_PROJECTION, null, null, null);
                mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
            }
        }
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Integer cnt = mSearchNumCntQ.poll();
        if (cnt != null) {
            mPrevQueryDigCnt = cnt.intValue();
        }
        log("+onQueryComplete");
        
        //if (activity != null && !activity.isFinishing())
        {
            final DialerSearchAdapter dialerSearchAdapter = mAdapter;
            // Whenever we get a suggestions cursor, we need to immediately kick off
            // another query for the complete list of contacts
            if (cursor!= null) {
                mDialerSearchCursorCount = cursor.getCount();
                log("cursor count: "+mDialerSearchCursorCount);
                String tempStr = mDigits.getText().toString();

                if (tempStr != null && mDialerSearchCursorCount > 0) {
                    mQueryComplete = true;
                    noResultDigCnt = 0;
                    // notify UI to update view only if the search digit count is equal to current input search digits in text view
                    // since user may input/delete quickly, the list view will be update continuously and take a lot of time 
                    if (DialerSearchUtils.tripHyphen(tempStr).length() == mPrevQueryDigCnt) {
                        // Don't need to close cursor every time after query complete.
                        if(mOnDialerSearchResult != null) {
                            mOnDialerSearchResult.onDialerSearchResult(obtainDialerSearchResult(mDialerSearchCursorCount));
                        }
                        dialerSearchAdapter.setResultCursor(cursor);
                        dialerSearchAdapter.changeCursor(cursor);
                    } else {
                        cursor.close();
                    }
                } else {
                    if(mOnDialerSearchResult != null) {
                        mOnDialerSearchResult.onDialerSearchResult(obtainDialerSearchResult(mDialerSearchCursorCount));
                    }
                    noResultDigCnt = mDigits.getText().length();
                    cursor.close();
                    dialerSearchAdapter.setResultCursor(null);
                }
            }
        }/* else {
            if (cursor != null)
                cursor.close();
        }*/
        log("-onQueryComplete");
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {//80794
        log("result is "+result);
        if (result < 0) {
            Toast.makeText(mContext,
                    R.string.delete_error, Toast.LENGTH_SHORT).show();
        } else {
            if (mSelectedContactUri != null) {
                log("Before delete db");
                int deleteCount = mContext.getContentResolver().delete(mSelectedContactUri, null, null);
                log("onDeleteComplete startQuery");
                if (deleteCount > 0) {
                    delCount = deleteCount;
                    mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                }
            }
        }

    }

    public void setSearchNumberOnly() {
        mSearchNumberOnly = true;
    }

    private class DsTextWatcher extends PhoneNumberFormattingTextWatcherEx {
        
        private static final boolean DBG_INT = DBG;
        
        DsTextWatcher() {
            super();
        }

        public void afterTextChanged(Editable arg0) {
            if (mSelfChanged) {
                logd("[afterTextChanged]mSelfChanged:" + mSelfChanged);
                return;
            }
            logd("[afterTextChanged]text:" + arg0.toString());
            mPreviousText = arg0.toString();
    
            if (!mFormatting) {
                logd("formatting");
                mFormatting = true;
                mDigitString = mDigits.getText().toString();
    
                mDigitString = DialerSearchUtils.tripNonDigit(mDigitString);
                if (arg0.length() > 0) {
                    String digits = arg0.toString();
                    startQuery(digits, searchMode);
                } else if (arg0.length() == 0) {
                    mSearchNumberOnly = false;
                    if (mDataChanged) {
                        startQuery(null, DIALER_SEARCH_MODE_ALL);
                        mDataChanged = false;
                    } else {
                        startQuery("NULL_INPUT", DIALER_SEARCH_MODE_ALL);
                    }
                }
            }
            mFormatting = false;
        }
    
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mSelfChanged) {
                logd("[beforeTextChanged]mSelfChanged:" + mSelfChanged);
                return;
            }
            logd("[beforeTextChanged]s:" + s.toString()
                    + "|start:" + start + "|count:" + count + "|after:" + after);
            int selIdex = Selection.getSelectionStart(s);
            if (selIdex < s.length()) {
                mChangeInMiddle = true;
            }
        }
    
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mSelfChanged) {
                logd("[onTextChanged]mSelfChanged:" + mSelfChanged);
                return;
            }
            logd("[onTextChanged]s:" + s.toString()
                    + "|start:" + start + "|count:" + count + "|before:" + before);
            String digis = s.toString();
            if (!mFormatting && digis.length() > 0) {
                int len = s.length();
                if (ContactsApplication.getInstance().sDialerSearchSupport) {
                    if (mSearchNumberOnly
                            || count > 1 || before >1 || (count==before && start==len-1)
                            || mChangeInMiddle) {
                        // parse action should also set flag
                        setSearchNumberOnly();
                        searchMode = DIALER_SEARCH_MODE_NUMBER;
                    } else {
                        searchMode = DIALER_SEARCH_MODE_ALL;
                    }
                }
            }
            mChangeInMiddle = false;
        }
        
        private void logd(String msg) {
            if (DBG_INT) {
                Log.d(TAG, msg);
            }
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mAdapter != null)
            mAdapter.onScrollStateChanged(view, scrollState);
    }

    private final Handler mDBHandlerForDelContacts = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DS_MSG_CONTACTS_DELETE_CHANGED: {
                    if (delCount > 0) {
                        if (mDigits != null) {
                            mDigits.getText().clear();
                            log("mPhoneStateListener startQuery");
                            startQuery(null, DIALER_SEARCH_MODE_ALL);
                        }
                        delCount = 0;
                    }
                    return;
                }
            }
        }
    };

    public class DialerSearchResult {
        int count;
    }

    private class CallLogContentObserver extends ContentObserver {
        public CallLogContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            log("call log observer onChange length: "+ mDigits.length());
            if (mIsForeground) {
                if (mDigits != null) {
                    if(mDigits.length() > 0) {
                        // clear the text will trigger startQuery
                        mDataChanged = true;
                    } else {
                        startQuery(null, DIALER_SEARCH_MODE_ALL);
                    }
                }
            } else {
                mDataChanged = true;
            }
        }
    }
    
    
    private class ContactContentObserver extends ContentObserver {

        public ContactContentObserver() {
            super(new Handler());
        }
        
        @Override
        public void onChange(boolean selfChange) {
            log("ContactContentObserver: ");
            if (!mIsForeground) {
                mDataChanged = true;
            }else{
                if (mDigits != null) {
                    if(mDigits.length() > 0) {
                        // clear the text will trigger startQuery
                        mDataChanged = true;
                    } else {
                        startQuery(null, DIALER_SEARCH_MODE_ALL);
                    }
                }
            }
        }
    }

    public void updateDialerSearch() {
        if (mDigits != null) {
            if (mDigits.length() > 0) {
                // clear the text will trigger startQuery
                mDataChanged = true;
            } else {
                startQuery(null, DIALER_SEARCH_MODE_ALL);
            }
        }
    }

    public interface OnDialerSearchResult {
        public void onDialerSearchResult(DialerSearchResult dialerSearchResult);
    }
}
