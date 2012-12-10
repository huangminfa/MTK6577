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

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_ABORT;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_COMPLETE;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_START;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_STATUS_ACTION;
import static com.android.mms.ui.MessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.MessageListAdapter.COLUMN_MMS_LOCKED;
import static com.android.mms.ui.MessageListAdapter.COLUMN_MSG_TYPE;
import static com.android.mms.ui.MessageListAdapter.PROJECTION;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.drm.mobile1.DrmException;
import android.drm.mobile1.DrmRawContent;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.DrmStore;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.ClipboardManager;
import android.text.format.DateFormat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.TempFileProvider;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.google.android.mms.util.PduCache;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.HeightChangedLinearLayout;
import com.android.mms.ui.HeightChangedLinearLayout.LayoutSizeChangedListener;
import com.android.mms.ui.MessageUtils.ResizeImageResultCallback;
import com.android.mms.ui.RecipientsEditor.RecipientContextMenuInfo;
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import com.android.mms.util.DraftCache;
import com.android.mms.util.SendingProgressTokenManager;
import com.android.mms.util.SmileyParser;

import android.text.InputFilter.LengthFilter;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.android.vcard.exception.VCardException;

//a0
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.CamcorderProfile;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore.Audio;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.MmsSms;
import android.telephony.gemini.GeminiSmsManager;
import android.telephony.SmsManager;
import android.text.style.AbsoluteSizeSpan;
import android.util.Config;
import android.view.ActionMode;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
//import android.widget.RecipientsView.OnRecipientsChangeListener;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.view.ViewGroup;

import com.android.mms.transaction.SendTransaction;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.ui.ConversationList.BaseProgressQueryHandler;
import com.android.mms.util.ThreadCountManager;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.ITelephony;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.mms.MmsApp;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import com.android.mms.transaction.MmsSystemEventReceiver.OnShutDownListener;
import com.android.mms.transaction.MmsSystemEventReceiver.OnSimInforChangedListener;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;

// M: add for read report-start
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import com.android.mms.transaction.TransactionService;
import com.android.mms.transaction.Transaction;
import com.google.android.mms.pdu.PduHeaders;

// M: add for read report-end
//a1
/**
 * This is the main UI for:
 * 1. Composing a new message;
 * 2. Viewing/managing message history of a conversation.
 *
 * This activity can handle following parameters from the intent
 * by which it's launched.
 * thread_id long Identify the conversation to be viewed. When creating a
 *         new message, this parameter shouldn't be present.
 * msg_uri Uri The message which should be opened for editing in the editor.
 * address String The addresses of the recipients in current conversation.
 * exit_on_sent boolean Exit this activity after the message is sent.
 */
//m0
/*
public class ComposeMessageActivity extends Activity
        implements View.OnClickListener, TextView.OnEditorActionListener,
        MessageStatusListener, Contact.UpdateListener {
*/
public class ComposeMessageActivity extends Activity
        implements View.OnClickListener, TextView.OnEditorActionListener,
        MessageStatusListener, Contact.UpdateListener
//        ,OnRecipientsChangeListener
        ,OnShutDownListener, OnSimInforChangedListener
        {
//m1
    public static final int REQUEST_CODE_ATTACH_IMAGE     = 100;
    public static final int REQUEST_CODE_TAKE_PICTURE     = 101;
    public static final int REQUEST_CODE_ATTACH_VIDEO     = 102;
    public static final int REQUEST_CODE_TAKE_VIDEO       = 103;
    public static final int REQUEST_CODE_ATTACH_SOUND     = 104;
    public static final int REQUEST_CODE_RECORD_SOUND     = 105;
    public static final int REQUEST_CODE_CREATE_SLIDESHOW = 106;
    public static final int REQUEST_CODE_ECM_EXIT_DIALOG  = 107;
    public static final int REQUEST_CODE_ADD_CONTACT      = 108;
    public static final int REQUEST_CODE_PICK             = 109;
    public static final int REQUEST_CODE_FOR_MULTIDELETE  = 110;

    public static final int MIN_SIZE_FOR_CAPTURE_VIDEO    = 1024 * 10;  // 10K
    public static final int MIN_SIZE_FOR_RECORD_AUDIO = 1024 * 5; // 5K 

    private static final String TAG = "Mms/compose";

    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;
    private static final boolean LOCAL_LOGV = false;

    // Menu ID
    private static final int MENU_ADD_SUBJECT           = 0;
    private static final int MENU_DELETE_THREAD         = 1;
    private static final int MENU_ADD_ATTACHMENT        = 2;
    private static final int MENU_DISCARD               = 3;
    private static final int MENU_SEND                  = 4;
    private static final int MENU_CALL_RECIPIENT        = 5;
    private static final int MENU_CONVERSATION_LIST     = 6;
    private static final int MENU_DEBUG_DUMP            = 7;
    private static final int MENU_ADD_QUICK_TEXT        = 8;
    private static final int MENU_ADD_TEXT_VCARD        = 9;
    private static final int MENU_CALL_RECIPIENT_BY_VT  = 10;

    // Context menu ID
    private static final int MENU_VIEW_CONTACT          = 12;
    private static final int MENU_ADD_TO_CONTACTS       = 13;

    private static final int MENU_EDIT_MESSAGE          = 14;
    private static final int MENU_VIEW_SLIDESHOW        = 16;
    private static final int MENU_VIEW_MESSAGE_DETAILS  = 17;
    private static final int MENU_DELETE_MESSAGE        = 18;
    private static final int MENU_SEARCH                = 19;
    private static final int MENU_DELIVERY_REPORT       = 20;
    private static final int MENU_FORWARD_MESSAGE       = 21;
    private static final int MENU_CALL_BACK             = 22;
    private static final int MENU_SEND_EMAIL            = 23;
    private static final int MENU_COPY_MESSAGE_TEXT     = 24;
    private static final int MENU_COPY_TO_SDCARD        = 25;
    private static final int MENU_INSERT_SMILEY         = 26;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
    private static final int MENU_LOCK_MESSAGE          = 28;
    private static final int MENU_UNLOCK_MESSAGE        = 29;
    private static final int MENU_COPY_TO_DRM_PROVIDER  = 30;
    private static final int MENU_PREFERENCES           = 31;
    private static final int MENU_SELECT_TEXT           = 36; // add for select text copy

//m0
//    private static final int RECIPIENTS_MAX_LENGTH = 312;
    private static final int RECIPIENTS_MAX_LENGTH      = 5000/*312*/;
//m1

    private static final int DOUBLECLICK_INTERVAL_TIME  = 2000;
    private static final int MESSAGE_LIST_QUERY_TOKEN   = 9527;
    private static final int DELETE_MESSAGE_TOKEN       = 9700;

    private static final int CHARS_REMAINING_BEFORE_COUNTER_SHOWN = 10;

    private static final long NO_DATE_FOR_DIALOG        = -1L;

    private static final String EXIT_ECM_RESULT         = "exit_ecm_result";
    private static final String SIGN_CREATE_AFTER_KILL_BY_SYSTEM = "ForCreateAfterKilledBySystem";

    private ContentResolver mContentResolver;

    private BackgroundQueryHandler mBackgroundQueryHandler;

    private Conversation mConversation;     // Conversation we are working in

    private boolean mExitOnSent;            // TODO: mExitOnSent is obsolete -- remove

    private View mTopPanel;                 // View containing the recipient and subject editors
    private View mBottomPanel;              // View containing the text editor, send button, et.
    private EditText mTextEditor;           // Text editor to type your message into
    private TextView mTextCounter;          // Shows the number of characters used in text editor
    private TextView mSendButtonMms;        // Press to send mms
    private ImageButton mSendButtonSms;     // Press to send sms
    private EditText mSubjectTextEditor;    // Text editor for MMS subject

    private AttachmentEditor mAttachmentEditor;
//    private View mAttachmentEditorScrollView;

    private MessageListView mMsgListView;        // ListView for messages in this conversation
    public MessageListAdapter mMsgListAdapter = null;  // and its corresponding ListAdapter

    private RecipientsEditor mRecipientsEditor;  // UI control for editing recipients
    private ImageButton mRecipientsPicker;       // UI control for recipients picker

    private boolean mIsKeyboardOpen;             // Whether the hardware keyboard is visible
    private boolean mIsLandscape;                // Whether we're in landscape mode

    private boolean mPossiblePendingNotification;   // If the message list has changed, we may have
                                                    // a pending notification to deal with.

    private boolean mToastForDraftSave;   // Whether to notify the user that a draft is being saved

    private boolean mSentMessage;       // true if the user has sent a message while in this
                                        // activity. On a new compose message case, when the first
                                        // message is sent is a MMS w/ attachment, the list blanks
                                        // for a second before showing the sent message. But we'd
                                        // think the message list is empty, thus show the recipients
                                        // editor thinking it's a draft message. This flag should
                                        // help clarify the situation.
    
    private boolean mAppendAttachmentSign = true;
    private WorkingMessage mWorkingMessage;         // The message currently being composed.

    private AlertDialog mSmileyDialog;
    private ProgressDialog mProgressDialog;
    private boolean mWaitingForSubActivity;
    private int mLastRecipientCount;            // Used for warning the user on too many recipients.
    private AttachmentTypeSelectorAdapter mAttachmentTypeSelectorAdapter;

    private boolean mSendingMessage;    // Indicates the current message is sending, and shouldn't send again.

    private Intent mAddContactIntent;   // Intent used to add a new contact

    // State variable indicating an image is being compressed, which may take a while.
    private boolean mCompressingImage = false;

    private String mDebugRecipients;
    public static final String SMS_ADDRESS = "sms_address";
    public static final String SMS_BODY = "sms_body";
    public static final String FORWARD_MESSAGE = "forwarded_message";

    //MTK_OP01_PROTECT_START
    // add for text zoom
    private final int DEFAULT_TEXT_SIZE = 18;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 32;
    private ScaleDetector mScaleDetector;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float MIN_ADJUST_TEXT_SIZE = 0.2f;
    private boolean mIsCmcc = false;
    //MTK_OP01_PROTECT_END
    
    private static final String LEMEIINTENT= "android.intent.action.SENDMini";
    private static final String LEMEIINTENTMULTIPLE = "android.intent.action.SEND_MULTIPLEMini";
    private static final int IMAGE_SIZE_MINI = 100;
    private static final int AUDIO_VIDEO_SIZE_MINI = 300;
    private int mSimDis = -1;
    private boolean mIsLeMei = false;
    
    private int mToastCountForResizeImage = 0; // For indicate whether show toast message for resize image or not. If
                                               // mToastCountForResizeImage equals 0, show toast.
    private int mHomeBox = 0;
    private Toast mExceedSubjectSizeToast = null;
    private Toast mExceedMessageSizeToast = null;

    private SoloAlertDialog mSoloAlertDialog;

    private boolean mNeedUpdateContactForMessageContent = true;

    private boolean  mDrawBottomPanel = false; 

    @SuppressWarnings("unused")
    public static void log(String logMsg) {
        Thread current = Thread.currentThread();
        long tid = current.getId();
        StackTraceElement[] stack = current.getStackTrace();
        String methodName = stack[3].getMethodName();
        // Prepend current thread ID and name of calling method to the message.
        logMsg = "[" + tid + "] [" + methodName + "] " + logMsg;
        Log.d(TAG, logMsg);
    }

    //==========================================================
    // Inner classes
    //==========================================================

    private void editSlideshow() {
        // make this aync because save mms may be long.
        runAsyncWithDialog(new Runnable() {
            public void run() {
                mWorkingMessage.removeAllFileAttaches();
                Uri dataUri = mWorkingMessage.saveAsMms(false);
                if (dataUri == null) {
                    return;
                }
                Intent intent = new Intent(ComposeMessageActivity.this, SlideshowEditActivity.class);
                intent.setData(dataUri);
                startActivityForResult(intent, REQUEST_CODE_CREATE_SLIDESHOW);
            }
        }, R.string.sync_mms_to_db);
    }

    private boolean mIsEditingSlideshow = false;
    private long mLastButtonClickTime = -65535;
    private final Handler mAttachmentEditorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // get the last click time
            long oldTime = mLastButtonClickTime;
            long nowTime = SystemClock.elapsedRealtime();
            //Xlog.d("MmsTest", "oldTime:"+oldTime+",time:"+mLastButtonClickTime);
            // ignore a click if too close.
            if ((nowTime - oldTime < DOUBLECLICK_INTERVAL_TIME) && (nowTime - oldTime > 0)) {
                Xlog.d("MmsTest", "ignore a close click");
                return;
            }
            mLastButtonClickTime = SystemClock.elapsedRealtime();
            switch (msg.what) {
                case AttachmentEditor.MSG_EDIT_SLIDESHOW: {
                    if (mClickCanResponse) {
                        mClickCanResponse = false;
                        mIsEditingSlideshow = true;
                        editSlideshow();
                    }
                    break;
                }
                case AttachmentEditor.MSG_SEND_SLIDESHOW: {
                    if (isPreparedForSending()) {
//m0
//                        ComposeMessageActivity.this.confirmSendMessageIfNeeded();
                        checkRecipientsCount();
//m1
                    }
                    break;
                }
                case AttachmentEditor.MSG_VIEW_IMAGE:
                case AttachmentEditor.MSG_PLAY_VIDEO:
                case AttachmentEditor.MSG_PLAY_AUDIO:
                case AttachmentEditor.MSG_PLAY_SLIDESHOW:
                    if (mClickCanResponse) {
                        mClickCanResponse = false;
                        if(mIsLeMei) {
                            MessageUtils.viewMmsMessageAttachmentMini(
                                    ComposeMessageActivity.this, mWorkingMessage.saveAsMms(false), 
                                    mWorkingMessage.getSlideshow());
                        } else {
                            try {
                                MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
                                        mWorkingMessage, msg.what);
                            } catch (IllegalStateException e) {
                                Xlog.e(TAG, "mWorkingMessage.getSlideshow() is null!");
                            }
                        }
                    }
                    hideInputMethod();
                    break;

                case AttachmentEditor.MSG_REPLACE_IMAGE:
//a0
                    getSharedPreferences("SetDefaultLayout", 0).edit().putBoolean("SetDefaultLayout", false).commit();
//a1
                case AttachmentEditor.MSG_REPLACE_VIDEO:
                case AttachmentEditor.MSG_REPLACE_AUDIO:
//a0
                    hideInputMethod();
//a1
                    showAddAttachmentDialog(false);
                    mLastButtonClickTime = -65535;
                    break;

                case AttachmentEditor.MSG_REMOVE_ATTACHMENT:
//                  Here uses android4.0 default code
                	if(mIsLeMei) {
                		finish();
                	} else {
                		mWorkingMessage.removeAttachment(true);
                	}
                    
                    break;

                default:
                    break;
            }
        }
    };

    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String type;
            switch (msg.what) {
                case MessageListItem.MSG_LIST_EDIT_MMS:
                    type = "mms";
                    break;
                case MessageListItem.MSG_LIST_EDIT_SMS:
                    type = "sms";
                    break;
//a0
                case MessageListAdapter.MSG_LIST_NEED_REFRASH: {
                    Xlog.d(MessageListAdapter.CACHE_TAG, "mMessageListItemHandler.handleMessage(): run adapter notify in mMessageListItemHandler.");
                    mMsgListAdapter.setClearCacheFlag(false);
                    mMsgListAdapter.notifyDataSetChanged();
                    return;
                }
                case MessageListItem.ITEM_CLICK: {
                    //add for multi-delete
                    mMsgListAdapter.changeSelectedState(msg.arg1);
                    mSelectedConvCount.setText(Integer.toString(mMsgListAdapter.getSelectedNumber()));
                    if (mMsgListAdapter.getSelectedNumber() > 0) {
                        mDeleteButton.setEnabled(true);
                        if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
                            mIsSelectedAll = true;
                            return;
                        }
                    } else {
                        mDeleteButton.setEnabled(false);
                    }
                    mIsSelectedAll = false;
                    return;
                }
// a1
                default:
                    Log.w(TAG, "Unknown message: " + msg.what);
                    return;
            }

            MessageItem msgItem = getMessageItem(type, (Long) msg.obj, false);
            if (msgItem != null) {
                editMessageItem(msgItem);
                drawBottomPanel();
                invalidateOptionsMenu();
            }
        }
    };

    private final OnKeyListener mSubjectKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            // When the subject editor is empty, press "DEL" to hide the input field.
            if ((keyCode == KeyEvent.KEYCODE_DEL) && (mSubjectTextEditor.length() == 0)) {
                showSubjectEditor(false);
                mWorkingMessage.setSubject(null, true);
//a0
                resetCounter();
//a1
                return true;
            }

            return false;
        }
    };

    // Shows the activity's progress spinner. Should be canceled if exiting the activity.
    private Runnable mShowProgressDialogRunnable = new Runnable() {
        public void run() {
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }
        }
    };

    /**
     * Return the messageItem associated with the type ("mms" or "sms") and message id.
     * @param type Type of the message: "mms" or "sms"
     * @param msgId Message id of the message. This is the _id of the sms or pdu row and is
     * stored in the MessageItem
     * @param createFromCursorIfNotInCache true if the item is not found in the MessageListAdapter's
     * cache and the code can create a new MessageItem based on the position of the current cursor.
     * If false, the function returns null if the MessageItem isn't in the cache.
     * @return MessageItem or null if not found and createFromCursorIfNotInCache is false
     */
    private MessageItem getMessageItem(String type, long msgId,
            boolean createFromCursorIfNotInCache) {
        return mMsgListAdapter.getCachedMessageItem(type, msgId,
                createFromCursorIfNotInCache ? mMsgListAdapter.getCursor() : null);
    }

    private boolean isCursorValid() {
        // Check whether the cursor is valid or not.
        Cursor cursor = mMsgListAdapter.getCursor();
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            Log.e(TAG, "Bad cursor.", new RuntimeException());
            return false;
        }
        return true;
    }

    private void resetCounter() {
//m0
//        mTextCounter.setText("");
//        mTextCounter.setVisibility(View.GONE);
        mTextEditor.setText(mWorkingMessage.getText());
        // once updateCounter.
        updateCounter(mWorkingMessage.getText(), 0, 0, 0);
        if (mWorkingMessage.requiresMms()) {
            mTextCounter.setVisibility(View.GONE);
        } else {
            mTextCounter.setVisibility(View.VISIBLE);
        }
//m1
    }

    private void updateCounter(CharSequence text, int start, int before, int count) {
//m0
/*
        WorkingMessage workingMessage = mWorkingMessage;
        if (workingMessage.requiresMms()) {
            // If we're not removing text (i.e. no chance of converting back to SMS
            // because of this change) and we're in MMS mode, just bail out since we
            // then won't have to calculate the length unnecessarily.
            final boolean textRemoved = (before > count);
            if (!textRemoved) {
                showSmsOrMmsSendButton(workingMessage.requiresMms());
                return;
            }
        }
*/
//m1
    	int[] params = null;
    	String optr = SystemProperties.get("ro.operator.optr");
    	//MTK_OP03_PROTECT_START
    	if ("OP03".equals(optr)) {
    		int encodingType = SmsMessage.ENCODING_UNKNOWN;
    	    encodingType = MessageUtils.getSmsEncodingType(ComposeMessageActivity.this);
    	    params = SmsMessage.calculateLength(text, false, encodingType);
    	} else {
    	//MTK_OP03_PROTECT_END
    		params = SmsMessage.calculateLength(text, false);
        //MTK_OP03_PROTECT_START
    	}
    	//MTK_OP03_PROTECT_END
            /* SmsMessage.calculateLength returns an int[4] with:
             *   int[0] being the number of SMS's required,
             *   int[1] the number of code units used,
             *   int[2] is the number of code units remaining until the next message.
             *   int[3] is the encoding type that should be used for the message.
             */
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
//m0
/*
        if (!MmsConfig.getMultipartSmsEnabled()) {
            mWorkingMessage.setLengthRequiresMms(
                    msgCount >= MmsConfig.getSmsToMmsTextThreshold(), true);
        }

        // Show the counter only if:
        // - We are not in MMS mode
        // - We are going to send more than one message OR we are getting close
        boolean showCounter = false;
        if (!workingMessage.requiresMms() &&
                (msgCount > 1 ||
                 remainingInCurrentMessage <= CHARS_REMAINING_BEFORE_COUNTER_SHOWN)) {
            showCounter = true;
        }

        showSmsOrMmsSendButton(workingMessage.requiresMms());

        if (showCounter) {
            // Update the remaining characters and number of messages required.
            String counterText = msgCount > 1 ? remainingInCurrentMessage + " / " + msgCount
                    : String.valueOf(remainingInCurrentMessage);
            mTextCounter.setText(counterText);
            mTextCounter.setVisibility(View.VISIBLE);
        } else {
            mTextCounter.setVisibility(View.GONE);
        }
*/
        int unitesUsed = params[1];
        mWorkingMessage.setLengthRequiresMms(
            msgCount >= MmsConfig.getSmsToMmsTextThreshold(), true);
        // Show the counter
        // Update the remaining characters and number of messages required.
        if (mWorkingMessage.requiresMms()) {
            mTextCounter.setVisibility(View.GONE);
        } else {
            mTextCounter.setVisibility(View.VISIBLE);
        }
        String counterText = remainingInCurrentMessage + "/" + msgCount;
        mTextCounter.setText(counterText);
//m1
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent mChooserIntent = Intent.createChooser(intent, null);
            super.startActivity(mChooserIntent);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        // requestCode >= 0 means the activity in question is a sub-activity.
        if (requestCode >= 0) {
            mWaitingForSubActivity = true;
        }

//m0
//        super.startActivityForResult(intent, requestCode);
        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Xlog.e(TAG, "Failed to startActivityForResult: " + intent);
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
                finish();
            } catch (Exception e) {
                Xlog.e(TAG, "Failed to startActivityForResult: " + intent);
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
          }
        } else {
//            Intent mchooserIntent = Intent.createChooser(intent, null);
//            super.startActivityForResult(mchooserIntent, requestCode);
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                if (requestCode == REQUEST_CODE_PICK) {
                    misPickContatct = false;
                }
                Intent mchooserIntent = Intent.createChooser(intent, null);
                super.startActivityForResult(mchooserIntent, requestCode);
            }
        }
        // m1
    }

    private void toastConvertInfo(boolean toMms) {
        final int resId = toMms ? R.string.converting_to_picture_message
                : R.string.converting_to_text_message;
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private class DeleteMessageListener implements OnClickListener {
        private final Uri mDeleteUri;
        private final boolean mDeleteLocked;

        public DeleteMessageListener(Uri uri, boolean deleteLocked) {
            mDeleteUri = uri;
            mDeleteLocked = deleteLocked;
        }

        public DeleteMessageListener(long msgId, String type, boolean deleteLocked) {
            if ("mms".equals(type)) {
                mDeleteUri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgId);
            } else {
                mDeleteUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
            }
            mDeleteLocked = deleteLocked;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            PduCache.getInstance().purge(mDeleteUri);
            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                    null, mDeleteUri, mDeleteLocked ? null : "locked=0", null);
            dialog.dismiss();
        }
    }

    private class DiscardDraftListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
//m0
/*
            mWorkingMessage.discard();
            dialog.dismiss();
            finish();
*/
            try {
                mWorkingMessage.discard();
                dialog.dismiss();
                finish();
            } catch(IllegalStateException e) {
                Xlog.e(TAG, e.getMessage());
            }
        }
    }

    private class SendIgnoreInvalidRecipientListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
//m0
//            sendMessage(true);
            checkConditionsAndSendMessage(true);
//m1
            dialog.dismiss();
        }
    }

    private class CancelSendingListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            if (isRecipientsEditorVisible()) {
                mRecipientsEditor.requestFocus();
            }
            dialog.dismiss();
//a0
            updateSendButtonState(true);
//a1
        }
    }

    private void confirmSendMessageIfNeeded() {
        if (!isRecipientsEditorVisible()) {
//m0
//            sendMessage(true);
            checkConditionsAndSendMessage(true);
//m1
            return;
        }

        boolean isMms = mWorkingMessage.requiresMms();
        if (mRecipientsEditor.hasInvalidRecipient(isMms)) {
//m0
/*
            if (mRecipientsEditor.hasValidRecipient(isMms)) {
                String title = getResourcesString(R.string.has_invalid_recipient,
                        mRecipientsEditor.formatInvalidNumbers(isMms));
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(title)
                    .setMessage(R.string.invalid_recipient_message)
                    .setPositiveButton(R.string.try_to_send,
                            new SendIgnoreInvalidRecipientListener())
                    .setNegativeButton(R.string.no, new CancelSendingListener())
                    .show();
            } else {
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.cannot_send_message)
                    .setMessage(R.string.cannot_send_message_reason)
                    .setPositiveButton(R.string.yes, new CancelSendingListener())
                    .show();
            }
*/
            updateSendButtonState();
            String title = getResourcesString(R.string.has_invalid_recipient, 
                    mRecipientsEditor.formatInvalidNumbers(isMms));
            new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(R.string.invalid_recipient_message)
                .setPositiveButton(R.string.try_to_send, new SendIgnoreInvalidRecipientListener())
                .setNegativeButton(R.string.no, new CancelSendingListener())
                .show();
//m1
        } else {
//m0
//            sendMessage(true);
            checkConditionsAndSendMessage(true);
//m1
        }
    }

    private final TextWatcher mRecipientsWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // This is a workaround for bug 1609057.  Since onUserInteraction() is
            // not called when the user touches the soft keyboard, we pretend it was
            // called when textfields changes.  This should be removed when the bug
            // is fixed.
            onUserInteraction();
        }

        public void afterTextChanged(Editable s) {
            // Bug 1474782 describes a situation in which we send to
            // the wrong recipient.  We have been unable to reproduce this,
            // but the best theory we have so far is that the contents of
            // mRecipientList somehow become stale when entering
            // ComposeMessageActivity via onNewIntent().  This assertion is
            // meant to catch one possible path to that, of a non-visible
            // mRecipientsEditor having its TextWatcher fire and refreshing
            // mRecipientList with its stale contents.
            if (!isRecipientsEditorVisible()) {
//m0
/*
                IllegalStateException e = new IllegalStateException(
                        "afterTextChanged called with invisible mRecipientsEditor");
*/
//m1
                // Make sure the crash is uploaded to the service so we
                // can see if this is happening in the field.
                Log.w(TAG,
                     "RecipientsWatcher: afterTextChanged called with invisible mRecipientsEditor");
                return;
            }

            mWorkingMessage.setWorkingRecipients(mRecipientsEditor.getNumbers());
            mWorkingMessage.setHasEmail(mRecipientsEditor.containsEmail(), true);
//            mConversation.getRecipients().clear();

            checkForTooManyRecipients();

            // Walk backwards in the text box, skipping spaces.  If the last
            // character is a comma, update the title bar.
            for (int pos = s.length() - 1; pos >= 0; pos--) {
                char c = s.charAt(pos);
                if (c == ' ')
                    continue;

                //65292 for Chinese ',' and 65307 for Chinese ';'
                if ((c == ',') || (c == ';') || ((int)c == 65307) || ((int)c == 65292)) {
                    updateTitle(new ContactList());
                }

                break;
            }
            //update title when no content
            if (s.length() == 0) {
                updateTitle(new ContactList());
            }
            // If we have gone to zero recipients, disable send button.
            updateSendButtonState();
        }
    };

    private void checkForTooManyRecipients() {
        final int recipientLimit = MmsConfig.getSmsRecipientLimit();
        if (recipientLimit != Integer.MAX_VALUE) {
            final int recipientCount = recipientCount();
            boolean tooMany = recipientCount > recipientLimit;

            if (recipientCount != mLastRecipientCount) {
                // Don't warn the user on every character they type when they're over the limit,
                // only when the actual # of recipients changes.
                mLastRecipientCount = recipientCount;
                if (tooMany) {
                    String tooManyMsg = getString(R.string.too_many_recipients, recipientCount,
                            recipientLimit);
                    Toast.makeText(ComposeMessageActivity.this,
                            tooManyMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private final OnCreateContextMenuListener mRecipientsMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            if (menuInfo != null) {
                Contact c = ((RecipientContextMenuInfo) menuInfo).recipient;
                RecipientsMenuClickListener l = new RecipientsMenuClickListener(c);

                menu.setHeaderTitle(c.getName());

                if (c.existsInDatabase()) {
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact)
                            .setOnMenuItemClickListener(l);
                } else if (MessageUtils.canAddToContacts(c)){
                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                            .setOnMenuItemClickListener(l);
                }
            }
        }
    };

    private final class RecipientsMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private final Contact mRecipient;

        RecipientsMenuClickListener(Contact recipient) {
            mRecipient = recipient;
        }

        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                // Context menu handlers for the recipients editor.
                case MENU_VIEW_CONTACT: {
                    Uri contactUri = mRecipient.getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                    return true;
                }
                case MENU_ADD_TO_CONTACTS: {
// Use google default code
                    mAddContactIntent = ConversationList.createAddContactIntent(
                            mRecipient.getNumber());
                    ComposeMessageActivity.this.startActivityForResult(mAddContactIntent,
                            REQUEST_CODE_ADD_CONTACT);
                    return true;
                }
            }
            return false;
        }
    }

    private void addPositionBasedMenuItems(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo");
            return;
        }
        final int position = info.position;

        addUriSpecificMenuItems(menu, v, position);
    }

    private Uri getSelectedUriFromMessageList(ListView listView, int position) {
        // If the context menu was opened over a uri, get that uri.
        MessageListItem msglistItem = (MessageListItem) listView.getChildAt(position);
        if (msglistItem == null) {
            // FIXME: Should get the correct view. No such interface in ListView currently
            // to get the view by position. The ListView.getChildAt(position) cannot
            // get correct view since the list doesn't create one child for each item.
            // And if setSelection(position) then getSelectedView(),
            // cannot get corrent view when in touch mode.
            return null;
        }

        TextView textView;
        CharSequence text = null;
        int selStart = -1;
        int selEnd = -1;

        //check if message sender is selected
        textView = (TextView) msglistItem.findViewById(R.id.text_view);
        if (textView != null) {
            text = textView.getText();
            selStart = textView.getSelectionStart();
            selEnd = textView.getSelectionEnd();
        }

        if (selStart == -1) {
            //sender is not being selected, it may be within the message body
            textView = (TextView) msglistItem.findViewById(R.id.body_text_view);
            if (textView != null) {
                text = textView.getText();
                selStart = textView.getSelectionStart();
                selEnd = textView.getSelectionEnd();
            }
        }

        // Check that some text is actually selected, rather than the cursor
        // just being placed within the TextView.
        if (selStart != selEnd) {
            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            URLSpan[] urls = ((Spanned) text).getSpans(min, max,
                                                        URLSpan.class);

            if (urls.length == 1) {
                return Uri.parse(urls[0].getURL());
            }
        }

        //no uri was selected
        return null;
    }

    private void addUriSpecificMenuItems(ContextMenu menu, View v, int position) {
        Uri uri = getSelectedUriFromMessageList((ListView) v, position);

        if (uri != null) {
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
            menu.addIntentOptions(0, 0, 0,
                    new android.content.ComponentName(this, ComposeMessageActivity.class),
                    null, intent, 0, null);
        }
    }

    private final void addCallAndContactMenuItems(
            ContextMenu menu, MsgListMenuClickListener l, MessageItem msgItem) {
        if (TextUtils.isEmpty(msgItem.mBody)) {
            return;
        }
        SpannableString msg = new SpannableString(msgItem.mBody);
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
//a0
// add for adding url to bookmark
        mURLs.clear();
//a1
        // Remove any dupes so they don't get added to the menu multiple times
        HashSet<String> collapsedUris = new HashSet<String>();
        for (String uri : uris) {
            collapsedUris.add(uri.toLowerCase());
        }
        for (String uriString : collapsedUris) {
            String prefix = null;
            int sep = uriString.indexOf(":");
            //m0
            /*if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                uriString = uriString.substring(sep + 1);
            }*/
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                if ("mailto".equalsIgnoreCase(prefix) || "tel".equalsIgnoreCase(prefix)){
                    uriString = uriString.substring(sep + 1);
                }
            }
            //m1
            Uri contactUri = null;
            boolean knownPrefix = true;
            if ("mailto".equalsIgnoreCase(prefix))  {
                contactUri = getContactUriForEmail(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                contactUri = getContactUriForPhoneNumber(uriString);
            } else {
                knownPrefix = false;
//a0
                //add URL to book mark
                if (msgItem.isSms() && mURLs.size() <= 0) {
                    menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark)
                    .setOnMenuItemClickListener(l);
                }
                
                //add for CMCC MMS URL to book mark
                String optr = SystemProperties.get("ro.operator.optr");
                if (null != optr && optr.equals("OP01") && msgItem.isMms() && mURLs.size() <= 0) {
                    menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark)
                    .setOnMenuItemClickListener(l);
                }
                
                mURLs.add(uriString);
//a1
            }
            if (knownPrefix && contactUri == null) {
                Intent intent = ConversationList.createAddContactIntent(uriString);

                String addContactString = getString(R.string.menu_add_address_to_contacts,
                        uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            }
        }
    }

    private Uri getContactUriForEmail(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Email.CONTACT_ID, Contacts.DISPLAY_NAME }, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(1);
                    if (!TextUtils.isEmpty(name)) {
                        return ContentUris.withAppendedId(Contacts.CONTENT_URI, cursor.getLong(0));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private Uri getContactUriForPhoneNumber(String phoneNumber) {
        Contact contact = Contact.get(phoneNumber, true);
        if (contact.existsInDatabase()) {
            return contact.getUri();
        }
        return null;
    }

    private final OnCreateContextMenuListener mMsgListMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//a0
            //add for multi-delete
            if (mMsgListAdapter.mIsDeleteMode) {
                return;
            }
//a1
            if (!isCursorValid()) {
                return;
            }
            Cursor cursor = mMsgListAdapter.getCursor();
            String type = cursor.getString(COLUMN_MSG_TYPE);
            long msgId = cursor.getLong(COLUMN_ID);
//a0
            Xlog.i(TAG, "onCreateContextMenu(): msgId=" + msgId);
//a1
            addPositionBasedMenuItems(menu, v, menuInfo);
//m0
/*
            MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
            if (msgItem == null) {
*/
            mMsgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
            if (mMsgItem == null) {
//m1
                Log.e(TAG, "Cannot load message item for type = " + type
                        + ", msgId = " + msgId);
                return;
            }

            menu.setHeaderTitle(R.string.message_options);

            MsgListMenuClickListener l = new MsgListMenuClickListener();

            // It is unclear what would make most sense for copying an MMS message
            // to the clipboard, so we currently do SMS only.
//m0
//            if (msgItem.isSms()) {
            if (mMsgItem.isSms()) {
//m1
                // Message type is sms. Only allow "edit" if the message has a single recipient
//m0
/*
                if (getRecipients().size() == 1 &&
                        (msgItem.mBoxId == Sms.MESSAGE_TYPE_OUTBOX ||
                                msgItem.mBoxId == Sms.MESSAGE_TYPE_FAILED)) {
*/
                if (getRecipients().size() == 1
                    && (mMsgItem.mBoxId == Sms.MESSAGE_TYPE_OUTBOX || mMsgItem.mBoxId == Sms.MESSAGE_TYPE_FAILED)
                    && (!mMsgItem.isSending())) {
// m1
                    menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                            .setOnMenuItemClickListener(l);
                }

                menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                        .setOnMenuItemClickListener(l);
//a0
                if (mSimCount > 0 && !mMsgItem.isSending()) {
                    menu.add(0, MENU_SAVE_MESSAGE_TO_SIM, 0, R.string.save_message_to_sim)
                        .setOnMenuItemClickListener(l);
                }
//a1
            }
//m0
//            addCallAndContactMenuItems(menu, l, msgItem);
            addCallAndContactMenuItems(menu, l, mMsgItem);
//m1

            // Forward is not available for undownloaded messages.
//m0
//            if (msgItem.isDownloaded()) {
            if (mMsgItem.isDownloaded()) {
//m1
                menu.add(0, MENU_FORWARD_MESSAGE, 0, R.string.menu_forward)
                        .setOnMenuItemClickListener(l);
            }
//m0
/*
            if (msgItem.isMms()) {
                switch (msgItem.mBoxId) {
*/
            if (mMsgItem.isMms()) {
                switch (mMsgItem.mBoxId) {
//m1
                    case Mms.MESSAGE_BOX_INBOX:
                        break;
                    case Mms.MESSAGE_BOX_OUTBOX:
                        // Since we currently break outgoing messages to multiple
                        // recipients into one message per recipient, only allow
                        // editing a message for single-recipient conversations.
//m0
                        if (getRecipients().size() == 1
                            && (mMsgItem.mBoxId == Sms.MESSAGE_TYPE_OUTBOX || mMsgItem.mBoxId == Sms.MESSAGE_TYPE_FAILED)
                            && (!mMsgItem.isSending())) {
                            menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                                    .setOnMenuItemClickListener(l);
                        }
                        break;
                }
//m0
//                switch (msgItem.mAttachmentType) {
                switch (mMsgItem.mAttachmentType) {
//m1
                    case WorkingMessage.TEXT:
                        break;
                    case WorkingMessage.VIDEO:
                    case WorkingMessage.IMAGE:
//m0
//                        if (haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                        if (haveSomethingToCopyToSDCard(mMsgItem.mMsgId)) {
//m1
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }
                        break;
                    case WorkingMessage.SLIDESHOW:
//a0
                        menu.add(0, MENU_VIEW_SLIDESHOW, 0, R.string.view_slideshow)
                        .setOnMenuItemClickListener(l);
                        if (haveSomethingToCopyToSDCard(mMsgItem.mMsgId)) {
                            //m0
                            /*menu.add(0, MENU_PREVIEW, 0, R.string.preview)
                            .setOnMenuItemClickListener(l);*/
                            //m1
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }
                        break;
//a1
                    default:
//m0
/*
                        menu.add(0, MENU_VIEW_SLIDESHOW, 0, R.string.view_slideshow)
                        .setOnMenuItemClickListener(l);
                        if (haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }

                        if (haveSomethingToCopyToDrmProvider(msgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_DRM_PROVIDER, 0,
                                    getDrmMimeMenuStringRsrc(msgItem.mMsgId))
                            .setOnMenuItemClickListener(l);
                        }
*/
                        if (haveSomethingToCopyToSDCard(mMsgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }
                        if (haveSomethingToCopyToDrmProvider(mMsgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_DRM_PROVIDER, 0,
                                    getDrmMimeMenuStringRsrc(mMsgItem.mMsgId))
                            .setOnMenuItemClickListener(l);
                        }
//m1
                        break;
                }
            }
//m0
//            if (msgItem.mLocked) {
            if (mMsgItem.mLocked) {
//m1
                menu.add(0, MENU_UNLOCK_MESSAGE, 0, R.string.menu_unlock)
                    .setOnMenuItemClickListener(l);
            } else {
                menu.add(0, MENU_LOCK_MESSAGE, 0, R.string.menu_lock)
                    .setOnMenuItemClickListener(l);
            }

            menu.add(0, MENU_VIEW_MESSAGE_DETAILS, 0, R.string.view_message_details)
                    .setOnMenuItemClickListener(l);
//m0
//            if (msgItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE || msgItem.mReadReport) {
            if (mMsgItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE || mMsgItem.mReadReport) {
//m1
                menu.add(0, MENU_DELIVERY_REPORT, 0, R.string.view_delivery_report)
                        .setOnMenuItemClickListener(l);
            }

            menu.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
                    .setOnMenuItemClickListener(l);

            // add for select text copy
            if (!TextUtils.isEmpty(mMsgItem.mBody)) {
                Log.i(TAG, "onCreateContextMenu(): add select text menu");
                menu.add(0, MENU_SELECT_TEXT, 0, R.string.select_text)
                        .setOnMenuItemClickListener(l);
            }
        }
    };

//a0
    // edit fail message item
//a1
    private void editMessageItem(MessageItem msgItem) {
        if ("sms".equals(msgItem.mType)) {
            editSmsMessageItem(msgItem);
        } else {
            editMmsMessageItem(msgItem);
            mWorkingMessage.setHasMmsDraft(true);
        }
//m0
/*
        if (msgItem.isFailedMessage() && mMsgListAdapter.getCount() <= 1) {
            // For messages with bad addresses, let the user re-edit the recipients.
            initRecipientsEditor();
        }
*/
        if ((msgItem.isFailedMessage() || msgItem.isSending()) && mMsgListAdapter.getCount() <= 1 ) {
            // For messages with bad addresses, let the user re-edit the recipients.
            initRecipientsEditor();
            isInitRecipientsEditor = true;
            mMsgListAdapter.changeCursor(null);
            invalidateOptionsMenu();
        }
//m1
    }

    private void editSmsMessageItem(MessageItem msgItem) {
        // When the message being edited is the only message in the conversation, the delete
        // below does something subtle. The trigger "delete_obsolete_threads_pdu" sees that a
        // thread contains no messages and silently deletes the thread. Meanwhile, the mConversation
        // object still holds onto the old thread_id and code thinks there's a backing thread in
        // the DB when it really has been deleted. Here we try and notice that situation and
        // clear out the thread_id. Later on, when Conversation.ensureThreadId() is called, we'll
        // create a new thread if necessary.
        synchronized(mConversation) {
//m0
//            if (mConversation.getMessageCount() <= 1) {
            if (mMsgListAdapter.getCursor().getCount() <= 1) {
//m1
                mConversation.clearThreadId();
            }
        }
        // Delete the old undelivered SMS and load its content.
        Uri uri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId);
        SqliteWrapper.delete(ComposeMessageActivity.this,
                mContentResolver, uri, null, null);

        mWorkingMessage.setText(msgItem.mBody);
    }

    private long clickTime = -65536;
    private void editMmsMessageItem(MessageItem msgItem) {
        // get the last click time
        long oldTime = clickTime;
        clickTime = SystemClock.elapsedRealtime();
        // ignore a click if too close.
        if ((clickTime-oldTime < 500)&&(clickTime-oldTime > 0)) {
            return;
        }
        // Discard the current message in progress.
        mWorkingMessage.discard();

        // Load the selected message in as the working message.
        WorkingMessage newWorkingMessage = WorkingMessage.load(this, msgItem.mMessageUri);
        if (newWorkingMessage == null){
            Xlog.e(TAG, "editMmsMessageItem, load returns null message");
            return;
        }
        mWorkingMessage = newWorkingMessage;
        mWorkingMessage.setConversation(mConversation);
        invalidateOptionsMenu();
//a0
        mAttachmentEditor.update(mWorkingMessage);
        updateTextEditorHeightInFullScreen();
//a1
        drawTopPanel(false);

        // WorkingMessage.load() above only loads the slideshow. Set the
        // subject here because we already know what it is and avoid doing
        // another DB lookup in load() just to get it.
        mWorkingMessage.setSubject(msgItem.mSubject, false);

        if (mWorkingMessage.hasSubject()) {
            showSubjectEditor(true);
        }
    }

    private void copyToClipboard(String str) {
        ClipboardManager clip =
            (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(str);
    }

    private void forwardMessage(MessageItem msgItem) {
        // add for input method covered Compose UI issue
        hideInputMethod();
        Intent intent = createIntent(this, 0);

        intent.putExtra(FORWARD_MESSAGE, true);

        if (msgItem.mType.equals("sms")) {
            String smsBody = msgItem.mBody;
//MTK_OP01_PROTECT_START
            // add for SMS forward with sender
            String optr = SystemProperties.get("ro.operator.optr");
            if (null != optr && optr.equals("OP01")) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ComposeMessageActivity.this);
                boolean smsForwardWithSender = prefs.getBoolean(MessagingPreferenceActivity.SMS_FORWARD_WITH_SENDER, true);
                Xlog.d(TAG, "forwardMessage(): SMS Forward With Sender ?= " + smsForwardWithSender);
                if (smsForwardWithSender) {
                    if (msgItem.mBoxId == Mms.MESSAGE_BOX_INBOX) {
                        smsBody += "\n" + getString(R.string.forward_from);
                        Contact contact = Contact.get(msgItem.mAddress, false);
                        Xlog.d(TAG, "forwardMessage(): Contact's name and number="
                                + Contact.formatNameAndNumber(contact.getName(), contact.getNumber(), ""));
                        smsBody += Contact.formatNameAndNumber(contact.getName(), contact.getNumber(), "");
                    }
                }
            }
//MTK_OP01_PROTECT_END
            intent.putExtra(SMS_BODY, smsBody);
        } else {
            SendReq sendReq = new SendReq();
            String subject = getString(R.string.forward_prefix);
            if (msgItem.mSubject != null) {
                subject += msgItem.mSubject;
            }
            sendReq.setSubject(new EncodedStringValue(subject));
            sendReq.setBody(msgItem.mSlideshow.makeCopy(
                    ComposeMessageActivity.this));

            Uri uri = null;
            try {
                PduPersister persister = PduPersister.getPduPersister(this);
                // Copy the parts of the message here.
                uri = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
            } catch (MmsException e) {
                Log.e(TAG, "Failed to copy message: " + msgItem.mMessageUri);
                Toast.makeText(ComposeMessageActivity.this,
                        R.string.cannot_save_message, Toast.LENGTH_SHORT).show();
                return;
            }

            intent.putExtra("msg_uri", uri);
            intent.putExtra("subject", subject);
        }
        // ForwardMessageActivity is simply an alias in the manifest for ComposeMessageActivity.
        // We have to make an alias because ComposeMessageActivity launch flags specify
        // singleTop. When we forward a message, we want to start a separate ComposeMessageActivity.
        // The only way to do that is to override the singleTop flag, which is impossible to do
        // in code. By creating an alias to the activity, without the singleTop flag, we can
        // launch a separate ComposeMessageActivity to edit the forward message.
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        startActivity(intent);
    }

    /**
     * Context menu handlers for the message list view.
     */
    private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
//m0
/*
            if (!isCursorValid()) {
                return false;
            }
            Cursor cursor = mMsgListAdapter.getCursor();
            String type = cursor.getString(COLUMN_MSG_TYPE);
            long msgId = cursor.getLong(COLUMN_ID);
            MessageItem msgItem = getMessageItem(type, msgId, true);
*/
//m1
//m0
//            if (msgItem == null) {
            if (mMsgItem == null) {
//m1
                return false;
            }

            switch (item.getItemId()) {
                case MENU_EDIT_MESSAGE:
//m0
//                    editMessageItem(msgItem);
                    editMessageItem(mMsgItem);
//m1
                    drawBottomPanel();
                    return true;

                case MENU_COPY_MESSAGE_TEXT:
//m0
//                    copyToClipboard(msgItem.mBody);
                	if (mMsgItem.mBody != null) {
                        String copyBody = mMsgItem.mBody.replaceAll(STR_RN, STR_CN);
                        copyToClipboard(copyBody);
                	} else {
                		Xlog.i(TAG, "onMenuItemClick, mMsgItem.mBody == null");
                		return false;
                	}
//m1
                    return true;

                case MENU_FORWARD_MESSAGE:
//m0
/*
                    final MessageItem mRestrictedItem = msgItem;
                    if (WorkingMessage.sCreationMode == 0 || !isRestrictedType(msgItem.mMsgId)) {
*/
                    final MessageItem mRestrictedItem = mMsgItem;
                    if (WorkingMessage.sCreationMode == 0 || !isRestrictedType(mMsgItem.mMsgId)) {
//m1
                    	new Thread(new Runnable() {
                            public void run() {
                            	forwardMessage(mRestrictedItem);
                            }
                        }, "ForwardMessage").start();
                        
                    } else if(WorkingMessage.sCreationMode == WorkingMessage.WARNING_TYPE) {
                        new AlertDialog.Builder(ComposeMessageActivity.this)
                        .setTitle(R.string.restricted_forward_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setMessage(R.string.restricted_forward_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public final void onClick(DialogInterface dialog, int which) {
                                int createMode = WorkingMessage.sCreationMode;
                                WorkingMessage.sCreationMode = 0;
                                new Thread(new Runnable() {
                                    public void run() {
                                    	forwardMessage(mRestrictedItem);
                                    }
                                }, "ForwardMessage").start();
                                WorkingMessage.sCreationMode = createMode;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                    }
                    return true;
                    
                case MENU_VIEW_SLIDESHOW:
                    if (mClickCanResponse) {
                        mClickCanResponse = false;
                        MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
//m0
//                                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId), null);
                                ContentUris.withAppendedId(Mms.CONTENT_URI, mMsgItem.mMsgId), null);
//m1
                        return true;
                    }

                case MENU_VIEW_MESSAGE_DETAILS: {
                    String messageDetails = MessageUtils.getMessageDetails(
//m0
/*
                            ComposeMessageActivity.this, cursor, msgItem.mMessageSize);
                    new AlertDialog.Builder(ComposeMessageActivity.this)
*/
                            ComposeMessageActivity.this, mMsgItem);
                    mDetailDialog = new AlertDialog.Builder(ComposeMessageActivity.this)
//m1
                            .setTitle(R.string.message_details_title)
                            .setMessage(messageDetails)
                            .setCancelable(true)
                            .show();
                    return true;
                }
                case MENU_DELETE_MESSAGE: {
                    DeleteMessageListener l = new DeleteMessageListener(
//m0
/*
                            msgItem.mMessageUri, msgItem.mLocked);
                    confirmDeleteDialog(l, msgItem.mLocked);
*/
                            mMsgItem.mMessageUri, mMsgItem.mLocked);
                    String where = Telephony.Mms._ID + "=" + mMsgItem.mMsgId;
                    String[] projection = new String[] { Sms.Inbox.THREAD_ID };
                    Xlog.d(TAG, "where:" + where);
                    Cursor queryCursor = Sms.query(getContentResolver(),
                            projection, where, null);
                    if (queryCursor.moveToFirst()) {
                        mThreadId = queryCursor.getLong(0);
                        queryCursor.close();
                    }
                    confirmDeleteDialog(l, mMsgItem.mLocked);
//m1
                    return true;
                }
                case MENU_DELIVERY_REPORT:
//m0
//                    showDeliveryReport(msgId, type);
                    showDeliveryReport(mMsgItem.mMsgId, mMsgItem.mType);
//m1
                    return true;

                case MENU_COPY_TO_SDCARD: {
                    StorageManager storageManager = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
                    long availSize = MessageUtils.getAvailableBytesInFileSystemAtGivenRoot(storageManager.getDefaultPath());
                    if (mMsgItem.mMessageSize > availSize) {
                        Toast.makeText(ComposeMessageActivity.this, getString(R.string.export_disk_problem), Toast.LENGTH_LONG).show();
                        return false; 
                    }

                    //m0 for Multi save
                    Intent i = new Intent(ComposeMessageActivity.this, MultiSaveActivity.class);
                    i.putExtra("msgid", mMsgItem.mMsgId);
                    startActivityForResult(i, REQUEST_CODE_MULTI_SAVE);
                    //m1
                    return true;
                }
//m1
                case MENU_COPY_TO_DRM_PROVIDER: {
//m0
//                    int resId = getDrmMimeSavedStringRsrc(msgId, copyToDrmProvider(msgId));
                    int resId = getDrmMimeSavedStringRsrc(mMsgItem.mMsgId, copyToDrmProvider(mMsgItem.mMsgId));
//m1
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                    return true;
                }

                case MENU_LOCK_MESSAGE: {
//m0
//                    lockMessage(msgItem, true);
                    lockMessage(mMsgItem, true);
//m1
                    return true;
                }

                case MENU_UNLOCK_MESSAGE: {
//m0
//                    lockMessage(msgItem, false);
                    lockMessage(mMsgItem, false);
//m1
                    return true;
                }
//a0
                case MENU_ADD_TO_BOOKMARK: {
                    if (mURLs.size() == 1) {
                        Browser.saveBookmark(ComposeMessageActivity.this, null, mURLs.get(0));
                    } else if(mURLs.size() > 1) {
                        CharSequence[] items = new CharSequence[mURLs.size()];
                        for (int i = 0; i < mURLs.size(); i++) {
                            items[i] = mURLs.get(i);
                        }
                        new AlertDialog.Builder(ComposeMessageActivity.this)
                            .setTitle(R.string.menu_add_to_bookmark)
                            .setIcon(com.mediatek.R.drawable.ic_dialog_menu_generic)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Browser.saveBookmark(ComposeMessageActivity.this, null, mURLs.get(which));
                                    }
                                })
                            .show();
                    }
                    return true;
                }

                case MENU_PREVIEW: {
                    final long iMsgId = mMsgItem.mMsgId;
                    //m0
                    /*Intent i = new Intent(ComposeMessageActivity.this, PreviewActivity.class);
                    i.putExtra("msgid", iMsgId);
                    startActivity(i);*/
                    //m1
                    return true;
                }

                case MENU_SAVE_MESSAGE_TO_SIM: {
                    mSaveMsgThread = new SaveMsgThread(mMsgItem.mType, mMsgItem.mMsgId);
                    mSaveMsgThread.start();
                    return true;
                }

                // add for select text copy
                case MENU_SELECT_TEXT: {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    Log.i(TAG, "onMenuItemClick(): info.position = " + info.position);
                    mMsgListAdapter.getItemId(info.position);
                    MessageListItem msglistItem = (MessageListItem) info.targetView;
                    if (msglistItem != null) {
                        Log.i(TAG, "msglistItem != null");
                        TextView textView = (TextView) msglistItem.findViewById(R.id.text_view);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeMessageActivity.this);
                        LayoutInflater factory = LayoutInflater.from(builder.getContext());
                        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
                        EditText contentSelector = (EditText)textEntryView.findViewById(R.id.content_selector);
                        contentSelector.setText(textView.getText());

                        builder.setTitle(R.string.select_text)
                               .setView(textEntryView)
                               .setPositiveButton(R.string.yes, null)
                               .show();
                    }
                    return true;
                }

                case MENU_ADD_ADDRESS_TO_CONTACTS: {
                    mAddContactIntent = item.getIntent();
                    startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
                    return true;
                }
//a1
                default:
                    return false;
            }
        }
    }

    private void lockMessage(MessageItem msgItem, boolean locked) {
        Uri uri;
        if ("sms".equals(msgItem.mType)) {
            uri = Sms.CONTENT_URI;
        } else {
            uri = Mms.CONTENT_URI;
        }
        final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);

        final ContentValues values = new ContentValues(1);
        values.put("locked", locked ? 1 : 0);

        new Thread(new Runnable() {
            public void run() {
                getContentResolver().update(lockUri,
                        values, null, null);
            }
        }, "lockMessage").start();
    }

    /**
     * Looks to see if there are any valid parts of the attachment that can be copied to a SD card.
     * @param msgId
     */
    private boolean haveSomethingToCopyToSDCard(long msgId) {
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }

        boolean result = false;
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("[CMA] haveSomethingToCopyToSDCard: part[" + i + "] contentType=" + type);
            }

            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    ContentType.isAudioType(type) || "application/ogg".equalsIgnoreCase(type)) {
                result = true;
                break;
            }

            // add for vcard
            if (FileAttachmentModel.isSupportedFile(part)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Looks to see if there are any drm'd parts of the attachment that can be copied to the
     * DrmProvider. Right now we only support saving audio (e.g. ringtones).
     * @param msgId
     */
    private boolean haveSomethingToCopyToDrmProvider(long msgId) {
        String mimeType = getDrmMimeType(msgId);
        return isAudioMimeType(mimeType);
    }

    /**
     * Simple cache to prevent having to load the same PduBody again and again for the same uri.
     */
    private static class PduBodyCache {
        private static PduBody mLastPduBody;
        private static Uri mLastUri;

        static public PduBody getPduBody(Context context, Uri contentUri) {
            if (contentUri.equals(mLastUri)) {
                return mLastPduBody;
            }
            try {
                mLastPduBody = SlideshowModel.getPduBody(context, contentUri);
                mLastUri = contentUri;
             } catch (MmsException e) {
                 Log.e(TAG, e.getMessage(), e);
                 return null;
             }
             return mLastPduBody;
        }
    };

    /* package */ static PduBody getPduBody(Context context, long msgid) {
        return  PduBodyCache.getPduBody(context,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgid));
    }

    /**
     * Copies media from an Mms to the DrmProvider
     * @param msgId
     */
    private boolean copyToDrmProvider(long msgId) {
        boolean result = true;
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (ContentType.isDrmType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
                result &= copyPartToDrmProvider(part);
            }
        }
        return result;
    }

    private String mimeTypeOfDrmPart(PduPart part) {
        Uri uri = part.getDataUri();
        InputStream input = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                DrmRawContent content = new DrmRawContent(fin, fin.available(),
                        DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING);
                String mimeType = content.getContentType();
                return mimeType;
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
        } catch (DrmException e) {
            Log.e(TAG, "DrmException caught ", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the type of the first drm'd pdu part.
     * @param msgId
     */
    private String getDrmMimeType(long msgId) {
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return null;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (ContentType.isDrmType(type)) {
                return mimeTypeOfDrmPart(part);
            }
        }
        return null;
    }

    private int getDrmMimeMenuStringRsrc(long msgId) {
        String mimeType = getDrmMimeType(msgId);
        if (isAudioMimeType(mimeType)) {
            return R.string.save_ringtone;
        }
        return 0;
    }

    private int getDrmMimeSavedStringRsrc(long msgId, boolean success) {
        String mimeType = getDrmMimeType(msgId);
        if (isAudioMimeType(mimeType)) {
            return success ? R.string.saved_ringtone : R.string.saved_ringtone_fail;
        }
        return 0;
    }

    private boolean isAudioMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }

    private boolean isImageMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    private boolean copyPartToDrmProvider(PduPart part) {
        Uri uri = part.getDataUri();

        InputStream input = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                // Build a nice title
                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }

                // Depending on the location, there may be an
                // extension already on the name or not
                String title = new String(location);
                int index;
                if ((index = title.indexOf(".")) == -1) {
                    String type = new String(part.getContentType());
                } else {
                    title = title.substring(0, index);
                }

                // transfer the file to the DRM content provider
                Intent item = DrmStore.addDrmFile(mContentResolver, fin, title);
                if (item == null) {
                    Log.w(TAG, "unable to add file " + uri + " to DrmProvider");
                    return false;
                }
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies media from an Mms to the "download" directory on the SD card
     * @param msgId
     */
    private boolean copyMedia(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(this,
                        ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "copyMedia can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    "application/ogg".equalsIgnoreCase(type) ||
                    ContentType.isAudioType(type) || FileAttachmentModel.isSupportedFile(type)) {
                result &= copyPart(part, Long.toHexString(msgId));   // all parts have to be successful for a valid result.
            }
        }
        return result;
    }

    private boolean copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();

        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }

                String fileName;
                if (location == null) {
                    // Use fallback name.
                    fileName = fallback;
                } else {
                    // For locally captured videos, fileName can end up being something like this:
                    //      /mnt/sdcard/Android/data/com.android.mms/cache/.temp1.3gp
                    fileName = new String(location);
                }
                File originalFile = new File(fileName);
                fileName = originalFile.getName();  // Strip the full path of where the "part" is
                                                    // stored down to just the leaf filename.

                // Depending on the location, there may be an
                // extension already on the name or not
                String dir = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS  + "/";
                String extension;
                int index;
                if ((index = fileName.lastIndexOf('.')) == -1) {
                    String type = new String(part.getContentType());
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
//a0
                    Xlog.i(TAG, "Save part extension name is: " + extension);
//a1
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
//a0
                    Xlog.i(TAG, "Save part extension name is: " + extension);
//a1
                    fileName = fileName.substring(0, index);
                }

                File file = getUniqueDestination(dir + fileName, extension);

                // make sure the path is valid and directories created for this file.
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return false;
                }

                fout = new FileOutputStream(file);

                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size=fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }

                // Notify other applications listening to scanner events
                // that a media file has been added to the sd card
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }

    private File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);

        for (int i = 2; file.exists(); i++) {
            file = new File(base + "_" + i + "." + extension);
        }
        return file;
    }

    private void showDeliveryReport(long messageId, String type) {
        Intent intent = new Intent(this, DeliveryReportActivity.class);
        intent.putExtra("message_id", messageId);
        intent.putExtra("message_type", type);

        startActivity(intent);
    }

    private final IntentFilter mHttpProgressFilter = new IntentFilter(PROGRESS_STATUS_ACTION);

    private final BroadcastReceiver mHttpProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PROGRESS_STATUS_ACTION.equals(intent.getAction())) {
                long token = intent.getLongExtra("token",
                                    SendingProgressTokenManager.NO_TOKEN);
                if (token != mConversation.getThreadId()) {
                    return;
                }

                int progress = intent.getIntExtra("progress", 0);
                switch (progress) {
                    case PROGRESS_START:
                        setProgressBarVisibility(true);
                        break;
                    case PROGRESS_ABORT:
                    case PROGRESS_COMPLETE:
                        setProgressBarVisibility(false);
                        break;
                    default:
                        setProgress(100 * progress);
                }
            }
        }
    };

    private static ContactList sEmptyContactList;

    private ContactList getRecipients() {
        // If the recipients editor is visible, the conversation has
        // not really officially 'started' yet.  Recipients will be set
        // on the conversation once it has been saved or sent.  In the
        // meantime, let anyone who needs the recipient list think it
        // is empty rather than giving them a stale one.
        if (isRecipientsEditorVisible()) {
            if (sEmptyContactList == null) {
                sEmptyContactList = new ContactList();
            }
            return sEmptyContactList;
        }
        return mConversation.getRecipients();
    }

    private void updateTitle(ContactList list) {
        String title = null;;
        String subTitle = null;
        int cnt = list.size();
        Xlog.d(TAG, "updateTitle(): list.size()" + list.size());
        switch (cnt) {
            case 0: {
                String recipient = null;
                if (mRecipientsEditor != null) {
                    recipient = mRecipientsEditor.getText().toString();
                }
                if (TextUtils.isEmpty(recipient)) {
                    title = getString(R.string.new_message);
                } else {
                    // remove trailing separtors
                    if (recipient.endsWith(", ") || recipient.endsWith(",")) {
                        title = recipient.substring(0, recipient.lastIndexOf(","));
                    } else {
                        title = recipient;
                    }
                    final int c = mRecipientsEditor.getRecipientCount();
                    if (c > 1) {
                        subTitle = getResources().getQuantityString(R.plurals.recipient_count, c, c);
                    }
                }
                break;
            }
            case 1: {
                title = list.get(0).getName();      // get name returns the number if there's no
                                                    // name available.
                String number = list.get(0).getNumber();
                String numberAfterFormat = MessageUtils.formatNumber(number,
                        this.getApplicationContext());
                if (!title.equals(number) && !title.equals(numberAfterFormat)) {
                    subTitle = numberAfterFormat;
                }
                break;
            }
            default: {
                // Handle multiple recipients
                title = list.formatNames(", ");
                subTitle = getResources().getQuantityString(R.plurals.recipient_count, cnt, cnt);
                break;
            }
        }
        mDebugRecipients = list.serialize();

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
    }

    // Get the recipients editor ready to be displayed onscreen.
    private void initRecipientsEditor() {
//m0
//        if (isRecipientsEditorVisible()) {
        if (isRecipientsEditorVisible() && isInitRecipientsEditor) {
//m1
            return;
        }
        // Must grab the recipients before the view is made visible because getRecipients()
        // returns empty recipients when the editor is visible.
        ContactList recipients = getRecipients();
//a0
        while (!recipients.isEmpty() && recipients.size() > RECIPIENTS_LIMIT_FOR_SMS) {
            recipients.remove(RECIPIENTS_LIMIT_FOR_SMS);
        }
//a1

        ViewStub stub = (ViewStub)findViewById(R.id.recipients_editor_stub);
        if (stub != null) {
            View stubView = stub.inflate();
            mRecipientsEditor = (RecipientsEditor) stubView.findViewById(R.id.recipients_editor);
            mRecipientsPicker = (ImageButton) stubView.findViewById(R.id.recipients_picker);
        } else {
            mRecipientsEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);
            mRecipientsEditor.setVisibility(View.VISIBLE);
            mRecipientsPicker = (ImageButton)findViewById(R.id.recipients_picker);
            mRecipientsPicker.setVisibility(View.VISIBLE);
        }
        mRecipientsPicker.setOnClickListener(this);

        mRecipientsEditor.setAdapter(new RecipientsAdapter(this));
        mRecipientsEditor.populate(recipients);
        mRecipientsEditor.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
        mRecipientsEditor.addTextChangedListener(mRecipientsWatcher);
        // TODO : Remove the max length limitation due to the multiple phone picker is added and the
        // user is able to select a large number of recipients from the Contacts. The coming
        // potential issue is that it is hard for user to edit a recipient from hundred of
        // recipients in the editor box. We may redesign the editor box UI for this use case.
        // mRecipientsEditor.setFilters(new InputFilter[] {
        //         new InputFilter.LengthFilter(RECIPIENTS_MAX_LENGTH) });
        mRecipientsEditor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // After the user selects an item in the pop-up contacts list, move the
                // focus to the text editor if there is only one recipient.  This helps
                // the common case of selecting one recipient and then typing a message,
                // but avoids annoying a user who is trying to add five recipients and
                // keeps having focus stolen away.
                if (mRecipientsEditor.getRecipientCount() == 1) {
                    // if we're in extract mode then don't request focus
                    final InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager == null || !inputManager.isFullscreenMode()) {
                        mTextEditor.requestFocus();
                    }
                }
            }
        });

        mRecipientsEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    RecipientsEditor editor = (RecipientsEditor) v;
                    ContactList contacts = editor.constructContactsFromInput(false);
                    updateTitle(contacts);
                }
            }
        });

        mTopPanel.setVisibility(View.VISIBLE);
        if (mIsRecipientHasIntentNotHandle && (mIntent != null)) {
            processPickResult(mIntent);
            mIsRecipientHasIntentNotHandle = false;
            mIntent = null;
        }
    }

    //==========================================================
    // Activity methods
    //==========================================================

    public static boolean cancelFailedToDeliverNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDeliver(intent)) {
            // Cancel any failed message notifications
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.MESSAGE_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }

    public static boolean cancelFailedDownloadNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDownload(intent)) {
            // Cancel any failed download notifications
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.DOWNLOAD_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//a0
        // If a new ComposeMessageActivity is created, kill old one
        if (sCompose != null && !sCompose.isFinishing() && savedInstanceState == null) {
            sCompose.finish();
        }
        sCompose = ComposeMessageActivity.this;
        initMessageSettings();
//a1
        resetConfiguration(getResources().getConfiguration());
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (mIsLandscape) {
            mCurrentMaxHeight = windowManager.getDefaultDisplay().getWidth();
        } else {
            mCurrentMaxHeight = windowManager.getDefaultDisplay().getHeight();
        }

        setContentView(R.layout.compose_message_activity);
        setProgressBarVisibility(false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize members for UI elements.
        initResourceRefs();

//a0
//MTK_OP01_PROTECT_START
        // add for text zoom
        // Initialize members for Zoom UI elements.
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && optr.equals("OP01")) {
            mIsCmcc = true;
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
            //Notify to close dialog mode screen
            closeMsgDialog();
        }
//MTK_OP01_PROTECT_END
        // SIM indicator manager
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        mComponentName = getComponentName();
        mSimCount = 0;
//a1
        mContentResolver = getContentResolver();
        mBackgroundQueryHandler = new BackgroundQueryHandler(mContentResolver);

        initialize(savedInstanceState, 0);

        if (TRACE) {
            android.os.Debug.startMethodTracing("compose");
        }
//a0
        mDestroy = false;
        if (mCellMgr == null) {
            mCellMgr = new CellConnMgr();
        }
        mCellMgr.register(getApplication());
        mCellMgrRegisterCount++;
//a1
        mSoloAlertDialog = new SoloAlertDialog(this);
    }

    //Notify to close dialog mode screen
    private void closeMsgDialog() {
    	Xlog.d(TAG, "ComposeMessageActivity.closeMsgDialog");
        Intent intent = new Intent();
        intent.setAction("com.android.mms.dialogmode.VIEWED");
        sendBroadcast(intent);
    }

    private void showSubjectEditor(boolean show) {
    	if(mIsLeMei) {
    		return;
    	}
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
//m0
//            log("" + show);
            log("showSubjectEditor: " + show);
//m1
        }

        if (mSubjectTextEditor == null) {
            // Don't bother to initialize the subject editor if
            // we're just going to hide it.
            if (show == false) {
                return;
            }
            mSubjectTextEditor = (EditText)findViewById(R.id.subject);
//m0
/*
            mSubjectTextEditor.setFilters(new InputFilter[] {
                    new LengthFilter(MmsConfig.getMaxSubjectLength())});
*/
//MTK_OP01_PROTECT_START
            if ("OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                // cmcc request subject <= 40bytes and need a tips, other op has no constraint.
                mSubjectTextEditor.setFilters(new InputFilter[] { new MyLengthFilter(DEFAULT_LENGTH) });
            }
//MTK_OP01_PROTECT_END
//m1
        }

        mSubjectTextEditor.setOnKeyListener(show ? mSubjectKeyListener : null);

        if (show) {
            mSubjectTextEditor.addTextChangedListener(mSubjectEditorWatcher);
        } else {
            mSubjectTextEditor.removeTextChangedListener(mSubjectEditorWatcher);
        }

        mSubjectTextEditor.setText(mWorkingMessage.getSubject());
        mSubjectTextEditor.setVisibility(show ? View.VISIBLE : View.GONE);
        hideOrShowTopPanel();
    }

    private void hideOrShowTopPanel() {
        boolean anySubViewsVisible = (isSubjectEditorVisible() || isRecipientsEditorVisible());
        mTopPanel.setVisibility(anySubViewsVisible ? View.VISIBLE : View.GONE);
    }

    public void initialize(Bundle savedInstanceState, long originalThreadId) {
        Intent intent = getIntent();
        // add for cmcc dir ui begin
        boolean showInput = false;
        boolean hiderecipient = false;
        boolean isMustRecipientEditable = false;
        if (MmsConfig.getMmsDirMode()) {
            mHomeBox = intent.getIntExtra("folderbox", 0);
            showInput = intent.getBooleanExtra("showinput", false);
            hiderecipient = intent.getBooleanExtra("hiderecipient", false);
            isMustRecipientEditable = true;
        }
        // add for cmcc dir ui end

        // Create a new empty working message.
        mWorkingMessage = WorkingMessage.createEmpty(this);

        // Read parameters or previously saved state of this activity. This will load a new
        // mConversation
        initActivityState(savedInstanceState);

        if (LogTag.SEVERE_WARNING && originalThreadId != 0 &&
                originalThreadId == mConversation.getThreadId()) {
            LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity.initialize: " +
                    " threadId didn't change from: " + originalThreadId, this);
        }

        log("savedInstanceState = " + savedInstanceState +
            ", intent = " + intent +
            ", originalThreadId = " + originalThreadId +
            ", mConversation = " + mConversation);

        // add for cmcc dir ui begin
        if (!MmsConfig.getMmsDirMode()) {
            // add for cmcc dir ui end
            if (cancelFailedToDeliverNotification(getIntent(), this)) {
                // Show a pop-up dialog to inform user the message was
                // failed to deliver.
                undeliveredMessageDialog(getMessageDate(null));
            }
            cancelFailedDownloadNotification(getIntent(), this);
            // add for cmcc dir ui begin
        }
        // add for cmcc dir ui end
        // Set up the message history ListAdapter
        initMessageList();

        // Load the draft for this thread, if we aren't already handling
        // existing data, such as a shared picture or forwarded message.
        boolean isForwardedMessage = false;
        // We don't attempt to handle the Intent.ACTION_SEND when saveInstanceState is non-null.
        // saveInstanceState is non-null when this activity is killed. In that case, we already
        // handled the attachment or the send, so we don't try and parse the intent again.
        boolean intentHandled = savedInstanceState == null &&
            (handleSendIntent() || (handleForwardedMessage() && !mConversation.hasDraft()));
        if (!intentHandled) {
            loadDraft();
        }

        // Let the working message know what conversation it belongs to
        mWorkingMessage.setConversation(mConversation);
        invalidateOptionsMenu();

        // Show the recipients editor if we don't have a valid thread. Hide it otherwise.
//m0
//        if (mConversation.getThreadId() <= 0) {
        if (mConversation.getThreadId() <= 0
            || (mConversation.getMessageCount() <= 0 && (intent.getAction() != null || mConversation.hasDraft()))
            || (mConversation.getThreadId() > 0 && mConversation.getMessageCount() <= 0)
            || isMustRecipientEditable) {
//m1
            // Hide the recipients editor so the call to initRecipientsEditor won't get
            // short-circuited.
            hideRecipientEditor();
//a0
            isInitRecipientsEditor = true;
//a1
            initRecipientsEditor();

            // Bring up the softkeyboard so the user can immediately enter recipients. This
            // call won't do anything on devices with a hard keyboard.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            hideRecipientEditor();
//a0
            mConversation.markAsRead();
//a1
        }
        // add for cmcc dir mode begin
        if (MmsConfig.getMmsDirMode()) {
            if (showInput) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            } else {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
            if (hiderecipient) {
                if (isRecipientsEditorVisible()) {
                    hideRecipientEditor();
                }
            }
        }
        // add for cmcc dir mode end

        invalidateOptionsMenu();    // do after show/hide of recipients editor because the options
                                    // menu depends on the recipients, which depending upon the
                                    // visibility of the recipients editor, returns a different
                                    // value (see getRecipients()).
        updateSendButtonState();

        drawTopPanel(false);
        drawBottomPanel();

        onKeyboardStateChanged(mIsKeyboardOpen);

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("update title, mConversation=" + mConversation.toString());
        }

        updateTitle(mConversation.getRecipients());

        if (isForwardedMessage && isRecipientsEditorVisible()) {
            // The user is forwarding the message to someone. Put the focus on the
            // recipient editor rather than in the message editor.
            mRecipientsEditor.requestFocus();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Xlog.d(TAG, "onNewIntent: intent = " + intent.toString());
        setIntent(intent);

        Conversation conversation = null;
        mSentMessage = false;

        // If we have been passed a thread_id, use that to find our
        // conversation.

        // Note that originalThreadId might be zero but if this is a draft and we save the
        // draft, ensureThreadId gets called async from WorkingMessage.asyncUpdateDraftSmsMessage
        // the thread will get a threadId behind the UI thread's back.
        long originalThreadId = mConversation.getThreadId();
        long threadId = intent.getLongExtra("thread_id", 0);
        Uri intentUri = intent.getData();

        boolean sameThread = false;
        if (threadId > 0) {
            conversation = Conversation.get(this, threadId, false);
        } else {
            if (mConversation.getThreadId() == 0) {
                // We've got a draft. Make sure the working recipients are synched
                // to the conversation so when we compare conversations later in this function,
                // the compare will work.
                mWorkingMessage.syncWorkingRecipients();
            }
            // Get the "real" conversation based on the intentUri. The intentUri might specify
            // the conversation by a phone number or by a thread id. We'll typically get a threadId
            // based uri when the user pulls down a notification while in ComposeMessageActivity and
            // we end up here in onNewIntent. mConversation can have a threadId of zero when we're
            // working on a draft. When a new message comes in for that same recipient, a
            // conversation will get created behind CMA's back when the message is inserted into
            // the database and the corresponding entry made in the threads table. The code should
            // use the real conversation as soon as it can rather than finding out the threadId
            // when sending with "ensureThreadId".
            conversation = Conversation.get(this, intentUri, false);
        }

        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onNewIntent: data=" + intentUri + ", thread_id extra is " + threadId +
                    ", new conversation=" + conversation + ", mConversation=" + mConversation);
        }

        // this is probably paranoid to compare both thread_ids and recipient lists,
        // but we want to make double sure because this is a last minute fix for Froyo
        // and the previous code checked thread ids only.
        // (we cannot just compare thread ids because there is a case where mConversation
        // has a stale/obsolete thread id (=1) that could collide against the new thread_id(=1),
        // even though the recipient lists are different)
        sameThread = ((conversation.getThreadId() == mConversation.getThreadId() ||
                mConversation.getThreadId() == 0) &&
                conversation.equals(mConversation));

        // Don't let any markAsRead DB updates occur before we've loaded the messages for
        // the thread. Unblocking occurs when we're done querying for the conversation
        // items.
        conversation.blockMarkAsRead(true);

        if (sameThread) {
            log("onNewIntent: same conversation");
            if (mConversation.getThreadId() == 0) {
                mConversation = conversation;
                mWorkingMessage.setConversation(mConversation);
                invalidateOptionsMenu();
            }
            mConversation.markAsRead();         // dismiss any notifications for this convo
        } else {
            if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("onNewIntent: different conversation");
            }
//a0
            // Don't let any markAsRead DB updates occur before we've loaded the messages for
            // the thread.
            conversation.blockMarkAsRead(true);
//a1
            if ((!isRecipientsEditorVisible())
                    || (mRecipientsEditor.hasValidRecipient(mWorkingMessage.requiresMms()))) {
                saveDraft(false);    // if we've got a draft, save it first
            }
//a0
            mMsgListAdapter.changeCursor(null);
            mConversation = conversation;
//a1
            initialize(null, originalThreadId);
        }
        loadMessageContent();
        //a0
        send_sim_id = intent.getIntExtra(com.android.internal.telephony.Phone.GEMINI_SIM_ID_KEY, -1);
        Xlog.d(TAG, "onNewIntent get simId from intent = " + send_sim_id);
        //a1
    }

    private void sanityCheckConversation() {
        if (mWorkingMessage.getConversation() != mConversation) {
            LogTag.warnPossibleRecipientMismatch(
                    "ComposeMessageActivity: mWorkingMessage.mConversation=" +
                    mWorkingMessage.getConversation() + ", mConversation=" +
                    mConversation + ", MISMATCH!", this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mWorkingMessage.isDiscarded()) {
            // If the message isn't worth saving, don't resurrect it. Doing so can lead to
            // a situation where a new incoming message gets the old thread id of the discarded
            // draft. This activity can end up displaying the recipients of the old message with
            // the contents of the new message. Recognize that dangerous situation and bail out
            // to the ConversationList where the user can enter this in a clean manner.
            mWorkingMessage.unDiscard();    // it was discarded in onStop().
            if (mWorkingMessage.isWorthSaving()) {
                if (LogTag.VERBOSE) {
                    log("onRestart: mWorkingMessage.unDiscard()");
                }
//                mWorkingMessage.unDiscard();    // it was discarded in onStop().

                sanityCheckConversation();
            } else if (isRecipientsEditorVisible()) {
                if (LogTag.VERBOSE) {
                    log("onRestart: goToConversationList");
                }
                goToConversationList();
            } else {
                if (LogTag.VERBOSE) {
                    log("onRestart: loadDraft");
                }
//m0
//                loadDraft();
//m1
                mWorkingMessage.setConversation(mConversation);
                mAttachmentEditor.update(mWorkingMessage);
                updateTextEditorHeightInFullScreen();
                invalidateOptionsMenu();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        
//MTK_OP01_PROTECT_START
        if(mIsCmcc){
            float size = MessageUtils.getTextSize(this);
            if(size != mTextSize){
                mTextSize = size;
                if(mTextEditor != null){
                    mTextEditor.setTextSize(size);
                }        
                if(mMsgListAdapter != null){
                    mMsgListAdapter.setTextSize(size);
                }
            }
        }
//MTK_OP01_PROTECT_END

//a0
        misPickContatct = false;
//a1
        mConversation.blockMarkAsRead(true);

        initFocus();

        // Register a BroadcastReceiver to listen on HTTP I/O process.
        registerReceiver(mHttpProgressReceiver, mHttpProgressFilter);

        if (mMsgListAdapter != null) {
            Xlog.d(TAG, "setOnDataSetChangedListener");
            mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        }
        loadMessageContent();

        // Update the fasttrack info in case any of the recipients' contact info changed
        // while we were paused. This can happen, for example, if a user changes or adds
        // an avatar associated with a contact.
//m0
//        mWorkingMessage.syncWorkingRecipients();
        if (mConversation.getThreadId() == 0) {
            mWorkingMessage.syncWorkingRecipients();
        }
//m1

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
//m0
//            log("update title, mConversation=" + mConversation.toString());
            log("onStart: update title, mConversation=" + mConversation.toString());
//m1
        }

        updateTitle(mConversation.getRecipients());

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mNeedUpdateContactForMessageContent = true;
    }

    public void loadMessageContent() {
        startMsgListQuery(0);
        updateSendFailedNotification();
        drawBottomPanel();
    }

    private void updateSendFailedNotification() {
        final long threadId = mConversation.getThreadId();
        if (threadId <= 0)
            return;

        // updateSendFailedNotificationForThread makes a database call, so do the work off
        // of the ui thread.
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.updateSendFailedNotificationForThread(
                        ComposeMessageActivity.this, threadId);
            }
        }, "updateSendFailedNotification").start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save recipients of this coversation
        if (mRecipientsEditor != null && isRecipientsEditorVisible()) {
            // TODO need re-coding for below code
//            outState.putString("recipients", mRecipientsEditor.allNumberToString());
            // We are compressing the image, so save the thread id in order to restore the draft when activity
            // restarting.
            if (mCompressingImage) {
                outState.putLong("thread", mConversation.ensureThreadId());
            } else if (mRecipientsEditor.getRecipientCount() < 1) {
                outState.putLong("thread",mConversation.ensureThreadId());
            }
                
        } else {
            // save the current thread id
            outState.putLong("thread", mConversation.getThreadId());
            Xlog.i(TAG, "saved thread id:" + mConversation.getThreadId());
        }

        mWorkingMessage.writeStateToBundle(outState);

        if (mExitOnSent) {
            outState.putBoolean("exit_on_sent", mExitOnSent);
        }

        outState.putBoolean("compressing_image", mCompressingImage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsEditingSlideshow = false;
//a0
        if(mProgressDialog != null && !mProgressDialog.isShowing())  {
            mProgressDialog.show();
        }

        Configuration config = getResources().getConfiguration();
        Xlog.d(TAG, "onResume - config.orientation="+config.orientation);
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Xlog.e(TAG, "onResume Set setSoftInputMode to 0x"+Integer.toHexString(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN));
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
//a1
        // OLD: get notified of presence updates to update the titlebar.
        // NEW: we are using ContactHeaderWidget which displays presence, but updating presence
        //      there is out of our control.
        //Contact.startPresenceObserver();

        addRecipientsListeners();

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
//m1
//            log("update title, mConversation=" + mConversation.toString());
            log("onResume: update title, mConversation=" + mConversation.toString());
//m1
        }

//a0
        // get all SIM info
        mGetSimInfoRunnable.run();
        
        String optr = SystemProperties.get("ro.operator.optr");
        //MTK_OP02_PROTECT_START
        if ("OP02".equals(optr)) {
        	if (mIsLeMei) {
                TextView sendButton = (TextView)findViewById(R.id.send_button_mms);
                sendButton.setText(R.string.send);
        	}
        }
        //MTK_OP02_PROTECT_END    
        updateSendButtonState();
//a1

        // There seems to be a bug in the framework such that setting the title
        // here gets overwritten to the original title.  Do this delayed as a
        // workaround.
        mMessageListItemHandler.postDelayed(new Runnable() {
            public void run() {
                ContactList recipients = isRecipientsEditorVisible() ?
                        mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
                updateTitle(recipients);
            }
//m0
//        }, 100);
        }, 10);
//m1
//a0
        // show SMS indicator
        mIsShowSIMIndicator = true;
        mStatusBarManager.hideSIMIndicator(mComponentName);
        mStatusBarManager.showSIMIndicator(mComponentName, Settings.System.SMS_SIM_SETTING);

        // make button can response to start other activity
        mClickCanResponse = true;
        if (mDrawBottomPanel) {
            drawBottomPanel();
        }
//a1
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawBottomPanel = true;
        // OLD: stop getting notified of presence updates to update the titlebar.
        // NEW: we are using ContactHeaderWidget which displays presence, but updating presence
        //      there is out of our control.
        //Contact.stopPresenceObserver();
//a0
        if (mDetailDialog != null){
            mDetailDialog.dismiss();
        }
        if (mSendDialog != null){
            mSendDialog.dismiss();
        }
        // hide SIM indicator
        mIsShowSIMIndicator = false;
        mStatusBarManager.hideSIMIndicator(mComponentName);
//a1
        removeRecipientsListeners();

        clearPendingProgressDialog();

        // we thought that the contacts data can be changed as long as the user leave.
        Contact.invalidateCache();
        mLastButtonClickTime = -65535;
    }

    @Override
    protected void onStop() {
        super.onStop();
//a0
        if (misPickContatct){
            return;
        }
//a1
        // Allow any blocked calls to update the thread's read status.
        mConversation.blockMarkAsRead(false);

        if (mMsgListAdapter != null) {
            Xlog.d(TAG, "Composer close cursor in onStop");
            mMsgListAdapter.changeCursor(null);
            mMsgListAdapter.setOnDataSetChangedListener(null);
        }

        if (mRecipientsEditor != null) {
            CursorAdapter recipientsAdapter = (CursorAdapter)mRecipientsEditor.getAdapter();
            if (recipientsAdapter != null) {
                recipientsAdapter.changeCursor(null);
            }
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
//m0
//            log("save draft");
            log("onStop: save draft");
//m1
        }

        // If image is being compressed, wait for it
        if (isFinishing()) {
            waitForCompressing();
        }
//m0
//        saveDraft(true);
        if ((!isRecipientsEditorVisible()) || (mRecipientsEditor.hasValidRecipient(mWorkingMessage.requiresMms()))) {
            saveDraft(true);
        }
//m1
        // Cleanup the BroadcastReceiver.
        unregisterReceiver(mHttpProgressReceiver);
//a0
        Xlog.i(TAG, "onStop(): mWorkingMessage.isDiscarded() == " + mWorkingMessage.isDiscarded());
//a1
    }

    private boolean needSaveDraft() {
        return ((!isRecipientsEditorVisible())
                    || (mRecipientsEditor.hasValidRecipient(mWorkingMessage.requiresMms())))
                && !mWorkingMessage.isDiscarded()
                && mWorkingMessage.isWorthSaving();
    }

    @Override
    protected void onDestroy() {
        if (TRACE) {
            android.os.Debug.stopMethodTracing();
        }
//a0
        if (mCellMgrRegisterCount == 1) {
            mCellMgr.unregister();
        }
        mCellMgrRegisterCount--;
        mDestroy = true;
        mScrollListener.destroyThread();

        if (mBackgroundQueryHandler != null) {
            Xlog.d(TAG, "clear pending queries in onDestroy");
            //mBackgroundQueryHandler.removeCallbacks(mListQueryRunnable);
            mBackgroundQueryHandler.removeCallbacksAndMessages(null);
            mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        }
        if (mMsgListAdapter != null) {
            Xlog.d(TAG, "clear adapter in onDestroy");
            mMsgListAdapter.changeCursor(null);
            mMsgListAdapter.setOnDataSetChangedListener(null);
            mMsgListAdapter.destroyTaskStack();
        }
//a1
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Xlog.d(TAG, "onConfigurationChanged-Start");
        super.onConfigurationChanged(newConfig);
        if (LOCAL_LOGV) {
            Log.v(TAG, "onConfigurationChanged: " + newConfig);
        }
/*
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            // Have to re-layout the attachment editor because we have different layouts
            // depending on whether we're portrait or landscape.
            //add for multi-delete
            if (!mMsgListAdapter.mIsDeleteMode) {
                mAttachmentEditor.update(mWorkingMessage);
            }
        }
        onKeyboardStateChanged(mIsKeyboardOpen);
*/
        if (resetConfiguration(newConfig)) {
            // Have to re-layout the attachment editor because we have different layouts
            // depending on whether we're portrait or landscape.
//m0
//            drawTopPanel(isSubjectEditorVisible());
            if (!mMsgListAdapter.mIsDeleteMode) {
                drawTopPanel(isSubjectEditorVisible());
            }
//m1
        }
        onKeyboardStateChanged(mIsKeyboardOpen);
//a0
        /*if (mDeletePanel != null && mDeletePanel.getVisibility() == View.VISIBLE) {
            LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mParams.height = getActionBar().getHeight();
            mDeletePanel.setLayoutParams(mParams);
        }*/
//a1
        Xlog.d(TAG, "onConfigurationChanged-End");
    }

    // returns true if landscape/portrait configuration has changed
    private boolean resetConfiguration(Configuration config) {
        Xlog.d(TAG, "resetConfiguration-Start");
        mIsKeyboardOpen = config.keyboardHidden == KEYBOARDHIDDEN_NO;
        boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        Xlog.d(TAG, "resetConfiguration: isLandscape = " + isLandscape);
        if ((mTextEditor != null) && (mTextEditor.getVisibility() == View.VISIBLE) && isLandscape) {
            mUiHandler.postDelayed(new Runnable() {
                public void run() {
                    Xlog.d(TAG, "resetConfiguration(): mTextEditor.setMaxHeight: "
                            + mReferencedTextEditorThreeLinesHeight);
                    mTextEditor.setMaxHeight(mReferencedTextEditorThreeLinesHeight * mCurrentMaxHeight
                            / mReferencedMaxHeight);
                }
            }, 100);
        }

        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            Xlog.d(TAG, "resetConfiguration-End");
            return true;
        }
        Xlog.d(TAG, "resetConfiguration-End");
        return false;
    }

    private void onKeyboardStateChanged(boolean isKeyboardOpen) {
        // If the keyboard is hidden, don't show focus highlights for
        // things that cannot receive input.
        if (isKeyboardOpen) {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusableInTouchMode(true);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusableInTouchMode(true);
            }
            mTextEditor.setFocusableInTouchMode(true);
            mTextEditor.setHint(R.string.type_to_compose_text_enter_to_send);
        } else {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusable(false);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusable(false);
            }
            mTextEditor.setFocusable(false);
            mTextEditor.setHint(R.string.open_keyboard_to_compose_message);
        }
    }

    @Override
    public void onUserInteraction() {
        checkPendingNotification();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            checkPendingNotification();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                if ((mMsgListAdapter != null) && mMsgListView.isFocused()) {
                    Cursor cursor;
                    try {
                        cursor = (Cursor) mMsgListView.getSelectedItem();
                    } catch (ClassCastException e) {
                        Log.e(TAG, "Unexpected ClassCastException.", e);
                        return super.onKeyDown(keyCode, event);
                    }

                    if (cursor != null) {
                        boolean locked = cursor.getInt(COLUMN_MMS_LOCKED) != 0;
                        DeleteMessageListener l = new DeleteMessageListener(
                                cursor.getLong(COLUMN_ID),
                                cursor.getString(COLUMN_MSG_TYPE),
                                locked);
                        confirmDeleteDialog(l, locked);
                        return true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
//a0
                break;
//a1
            case KeyEvent.KEYCODE_ENTER:
//m0
/*
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                    return true;
                }
*/
                if (isPreparedForSending()) {
                    //simSelection();
                    checkRecipientsCount();
                    return true;
                } else {
                    if (!isHasRecipientCount()) {
                            new AlertDialog.Builder(this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle(R.string.cannot_send_message)
                                .setMessage(R.string.cannot_send_message_reason)
                                .setPositiveButton(R.string.yes,new CancelSendingListener()).show();
                    } else {
                        new AlertDialog.Builder(this)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(R.string.cannot_send_message)
                            .setMessage(R.string.cannot_send_message_reason_no_content)
                            .setPositiveButton(R.string.yes,new CancelSendingListener()).show();
                    }
                }
//m1
                break;
            case KeyEvent.KEYCODE_BACK:
//m0
                if (mIsEditingSlideshow) {
                    return true;
                }
/*
                exitComposeMessageActivity(new Runnable() {
                    public void run() {
                        finish();
                    }
                });
*/
//                if (isRecipientsEditorVisible()) {
//                    mRecipientsEditor.structLastRecipient();
//                }

                // M: when out of composemessageactivity,try to send read report
                if (mIsLeMei) {
                    if (mWorkingMessage != null) {
                        mWorkingMessage.discard();
                    }
                    finish();
                } else {
                    if (FeatureOption.MTK_SEND_RR_SUPPORT) {
                        checkAndSendReadReport();
                    }
                    exitComposeMessageActivity(new Runnable() {
                        public void run() {
                            finish();
                        }
                    });
                }
//m1
                return true;
            case KeyEvent.KEYCODE_MENU:
                invalidateOptionsMenu();
                return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void exitComposeMessageActivity(final Runnable exit) {
    	  VideoThumbnailCache.clear();
        // If the message is empty, just quit -- finishing the
        // activity will cause an empty draft to be deleted.
        if (!mWorkingMessage.isWorthSaving()) {
            mWorkingMessage.discard();
//            Conversation c = mWorkingMessage.getConversation();
//            if (c != null && !mWaitingForSendMessage) {
//                mWorkingMessage.asyncDeleteAllMmsDraft(c.getThreadId());
//            }
            exit.run();
            return;
        }

        if (isRecipientsEditorVisible() &&
                !mRecipientsEditor.hasValidRecipient(mWorkingMessage.requiresMms())) {
            MessageUtils.showDiscardDraftConfirmDialog(this, new DiscardDraftListener());
            return;
        }

        if (needSaveDraft()) {
            DraftCache.getInstance().setSavingDraft(true);
        }
        mWorkingMessage.setNeedDeleteOldMmsDraft(true);
        mToastForDraftSave = true;
        exit.run();
    }

    private void goToConversationList() {
        finish();
        // add for cmcc dir ui begin
        if(MmsConfig.getMmsDirMode()) {
            Intent it = new Intent(this, FolderViewList.class);
            it.putExtra("floderview_key", mHomeBox);
            startActivity(it);
        } else {
        // add for cmcc dir ui end
        startActivity(new Intent(this, ConversationList.class));
        // add for cmcc dir ui begin
        }
        // add for cmcc dir ui end
    }

    private void hideRecipientEditor() {
        if (mRecipientsEditor != null) {
            mRecipientsEditor.removeTextChangedListener(mRecipientsWatcher);
            mRecipientsEditor.setVisibility(View.GONE);
            mRecipientsPicker.setVisibility(View.GONE);
            hideOrShowTopPanel();
        }
    }

    private boolean isRecipientsEditorVisible() {
        return (null != mRecipientsEditor)
                    && (View.VISIBLE == mRecipientsEditor.getVisibility());
    }

    private boolean isSubjectEditorVisible() {
        return (null != mSubjectTextEditor)
                    && (View.VISIBLE == mSubjectTextEditor.getVisibility());
    }

    public void onAttachmentChanged() {
        // Have to make sure we're on the UI thread. This function can be called off of the UI
        // thread when we're adding multi-attachments
        runOnUiThread(new Runnable() {
            public void run() {
                drawBottomPanel();
                updateSendButtonState();
                drawTopPanel(isSubjectEditorVisible());
                if (null != mRecipientsEditor) {
                    if (mWorkingMessage.hasSlideshow()) {
                        mRecipientsEditor.setImeActionLabel(getString(com.android.internal.R.string.ime_action_done), EditorInfo.IME_ACTION_DONE);
                        mRecipientsEditor.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    } else {
                        mRecipientsEditor.setImeActionLabel(getString(com.android.internal.R.string.ime_action_next), EditorInfo.IME_ACTION_NEXT);
                        mRecipientsEditor.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    }
                }

                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).restartInput(mRecipientsEditor);
            }
        });
    }

//m0
/*
    public void onProtocolChanged(final boolean mms) {
        // Have to make sure we're on the UI thread. This function can be called off of the UI
        // thread when we're adding multi-attachments
        runOnUiThread(new Runnable() {
            public void run() {
                toastConvertInfo(mms);
                showSmsOrMmsSendButton(mms);

                if (mms) {
                    // In the case we went from a long sms with a counter to an mms because
                    // the user added an attachment or a subject, hide the counter --
                    // it doesn't apply to mms.
                    mTextCounter.setVisibility(View.GONE);
                }
            }
        });
    }
*/
    public void onProtocolChanged(final boolean mms, final boolean needToast) {
        // Have to make sure we're on the UI thread. This function can be called off of the UI
        // thread when we're adding multi-attachments
        runOnUiThread(new Runnable() {
            public void run() {
                showSmsOrMmsSendButton(mms);

                if (mms == true) {
                    mTextCounter.setVisibility(View.GONE);
                } else {
                    mTextCounter.setVisibility(View.VISIBLE);
                }
                updateSendButtonState();
                if(needToast && !mIsLeMei){
                    toastConvertInfo(mms);
                }
            }
        });
    }
//m1

    // Show or hide the Sms or Mms button as appropriate. Return the view so that the caller
    // can adjust the enableness and focusability.
    private View showSmsOrMmsSendButton(boolean isMms) {
        View showButton;
        View hideButton;
        if (isMms) {
            showButton = mSendButtonMms;
            hideButton = mSendButtonSms;
        } else {
            showButton = mSendButtonSms;
            hideButton = mSendButtonMms;
        }
        showButton.setVisibility(View.VISIBLE);
        hideButton.setVisibility(View.GONE);

        return showButton;
    }

    Runnable mResetMessageRunnable = new Runnable() {
        public void run() {
            resetMessage();
        }
    };

    public void onPreMmsSent() {
        startMsgListQuery(0);
    }

    public void onPreMessageSent() {
        runOnUiThread(mResetMessageRunnable);
    }

    public void onMessageSent() {
//a0
        mWaitingForSendMessage = false;
//a1
        // If we already have messages in the list adapter, it
        // will be auto-requerying; don't thrash another query in.
        if (mMsgListAdapter.getCount() == 0) {
            if (LogTag.VERBOSE) {
                log("onMessageSent");
            }
            startMsgListQuery(0);
        }
    }

    public void onMaxPendingMessagesReached() {
        saveDraft(false);

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ComposeMessageActivity.this, R.string.too_many_unsent_mms,
                        Toast.LENGTH_LONG).show();
                mSendingMessage = false;
                updateSendButtonState();
            }
        });
    }

    public void onAttachmentError(final int error) {
        runOnUiThread(new Runnable() {
            public void run() {
                handleAddAttachmentError(error, R.string.type_picture);
                onMessageSent();        // now requery the list of messages
            }
        });
    }

    // We don't want to show the "call" option unless there is only one
    // recipient and it's a phone number.
    private boolean isRecipientCallable() {
        ContactList recipients = getRecipients();
        return (recipients.size() == 1 && !recipients.containsEmail());
    }

    private void dialRecipient(Boolean isVideoCall) {
        if (isRecipientCallable()) {
            String number = getRecipients().get(0).getNumber();
            Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            if (isVideoCall) {
                dialIntent.putExtra("com.android.phone.extra.video", true);
            }
            startActivity(dialIntent);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu) ;

        menu.clear();
        if (mIsLeMei) {
            menu.add(0, MENU_DISCARD, 0, R.string.discard).setIcon(
                    android.R.drawable.ic_menu_delete);
            return true;
        }
        
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (telephony != null && telephony.isVoiceCapable() && isRecipientCallable()) {
            MenuItem item = menu.add(0, MENU_CALL_RECIPIENT, 0, R.string.menu_call)
                    .setIcon(R.drawable.ic_menu_call)
                    .setTitle(R.string.menu_call);
            if (!isRecipientsEditorVisible()) {
                // If we're not composing a new message, show the call icon in the actionbar
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            if (FeatureOption.MTK_VT3G324M_SUPPORT) {
                menu.add(0, MENU_CALL_RECIPIENT_BY_VT, 0, R.string.call_video_call)
                        .setIcon(R.drawable.ic_video_call).setTitle(R.string.call_video_call);
            }
        }

        if (MmsConfig.getMmsEnabled()) {
            if (!isSubjectEditorVisible()) {
                menu.add(0, MENU_ADD_SUBJECT, 0, R.string.add_subject).setIcon(
                        R.drawable.ic_menu_edit);
            }
            menu.add(0, MENU_ADD_ATTACHMENT, 0, R.string.add_attachment)
                .setIcon(R.drawable.ic_menu_attachment)
                .setTitle(R.string.add_attachment)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);    // add to actionbar
        }

        if (isPreparedForSending()) {
            menu.add(0, MENU_SEND, 0, R.string.send).setIcon(android.R.drawable.ic_menu_send);
        }

        if (mMsgListAdapter.getCount() > 0) {
            //MTK_OP01_PROTECT_START
            if (!MmsConfig.getMmsDirMode()) {
            //MTK_OP01_PROTECT_END
            // Removed search as part of b/1205708
            //menu.add(0, MENU_SEARCH, 0, R.string.menu_search).setIcon(
            //        R.drawable.ic_menu_search);
            Cursor cursor = mMsgListAdapter.getCursor();
            if ((null != cursor) && (cursor.getCount() > 0)) {
                menu.add(0, MENU_DELETE_THREAD, 0, R.string.menu_delete_messages).setIcon(
                    android.R.drawable.ic_menu_delete);
            }
            //MTK_OP01_PROTECT_START
            }
            //MTK_OP01_PROTECT_END
        } else {
            menu.add(0, MENU_DISCARD, 0, R.string.discard).setIcon(android.R.drawable.ic_menu_delete);
        }
        if (!mWorkingMessage.hasSlideshow() || (mSubjectTextEditor != null && mSubjectTextEditor.isFocused())) {
            menu.add(0, MENU_ADD_QUICK_TEXT, 0, R.string.menu_insert_quick_text).setIcon(
                R.drawable.ic_menu_quick_text);
        }
//a0
        if (!mWorkingMessage.hasSlideshow() || (mSubjectTextEditor != null && mSubjectTextEditor.isFocused())) {
            menu.add(0, MENU_INSERT_SMILEY, 0, R.string.menu_insert_smiley).setIcon(
                R.drawable.ic_menu_emoticons);
        }

        if (!mWorkingMessage.hasSlideshow()){
            menu.add(0, MENU_ADD_TEXT_VCARD, 0, R.string.menu_insert_text_vcard).setIcon(
                    R.drawable.ic_menu_text_vcard);
        }
//a1

        buildAddAddressToContactMenuItem(menu);

        menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences).setIcon(
                android.R.drawable.ic_menu_preferences);

        if (LogTag.DEBUG_DUMP) {
            menu.add(0, MENU_DEBUG_DUMP, 0, R.string.menu_debug_dump);
        }

        return true;
    }

    private void buildAddAddressToContactMenuItem(Menu menu) {
        // Look for the first recipient we don't have a contact for and create a menu item to
        // add the number to contacts.
        for (Contact c : getRecipients()) {
            if (!c.existsInDatabase() && MessageUtils.canAddToContacts(c)) {
                Intent intent = ConversationList.createAddContactIntent(c.getNumber());
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setIntent(intent);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_SUBJECT:
                showSubjectEditor(true);
                mWorkingMessage.setSubject("", true);
//a0
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(
                    getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
//a1
                mSubjectTextEditor.requestFocus();
                break;
            case MENU_ADD_ATTACHMENT:
                // Launch the add-attachment list dialog
//a0
                hideInputMethod();
//a1
                showAddAttachmentDialog(!mWorkingMessage.hasAttachedFiles());
                break;
//a0
            case MENU_ADD_QUICK_TEXT:
                showQuickTextDialog();
                break;

            case MENU_ADD_TEXT_VCARD: {
                Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTICONTACTS");
                intent.setType(Contacts.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE_TEXT_VCARD);
                break;
            }
//a1
            case MENU_DISCARD:
                mWorkingMessage.discard();
                finish();
                break;
            case MENU_SEND:
                if (isPreparedForSending()) {
                    updateSendButtonState(false);
                    checkRecipientsCount();
                    mSendButtonCanResponse = true;
                }
                break;
            case MENU_SEARCH:
                onSearchRequested();
                break;
            case MENU_DELETE_THREAD:
                //m0
                //confirmDeleteThread(mConversation.getThreadId());
                
                // add for multi-delete
//                mMsgListAdapter.mIsDeleteMode = true;
//                hideInputMethod();
//                mMsgListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//                mDeleteActionMode = startActionMode(new DeleteCallback());
//                drawTopPanel(false);
//                drawBottomPanel();
//                startMsgListQuery();
                Intent mIntent = new Intent(this, MultiDeleteActivity.class);
                mIntent.putExtra("thread_id", mConversation.getThreadId());
                startActivityForResult(mIntent, REQUEST_CODE_FOR_MULTIDELETE);
                //m1
                break;

            case android.R.id.home:
            case MENU_CONVERSATION_LIST:
                exitComposeMessageActivity(new Runnable() {
                    public void run() {
                        goToConversationList();
                    }
                });
                break;
            case MENU_CALL_RECIPIENT: {
                dialRecipient(false);
                break;
            }
            case MENU_CALL_RECIPIENT_BY_VT: {
                dialRecipient(true);
                break;
            }
            case MENU_INSERT_SMILEY:
                showSmileyDialog();
                break;
            case MENU_VIEW_CONTACT: {
                // View the contact for the first (and only) recipient.
                ContactList list = getRecipients();
                if (list.size() == 1 && list.get(0).existsInDatabase()) {
                    Uri contactUri = list.get(0).getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                }
                break;
            }
            case MENU_ADD_ADDRESS_TO_CONTACTS:
                mAddContactIntent = item.getIntent();
                startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
                break;
            case MENU_PREFERENCES: {
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                startActivityIfNeeded(intent, -1);
                break;
            }
            case MENU_DEBUG_DUMP:
                mWorkingMessage.dump();
                Conversation.dump();
                LogTag.dumpInternalTables(this);
                break;
        }

        return true;
    }

    private void confirmDeleteThread(long threadId) {
        Conversation.startQueryHaveLockedMessages(mBackgroundQueryHandler,
                threadId, ConversationList.HAVE_LOCKED_MESSAGES_TOKEN);
    }

//    static class SystemProperties { // TODO, temp class to get unbundling working
//        static int getInt(String s, int value) {
//            return value;       // just return the default value or now
//        }
//    }

    private boolean checkSlideCount(boolean append) {
        String mMsg = this.getString(R.string.cannot_add_slide_anymore);
        Toast mToast = Toast.makeText(this, mMsg, Toast.LENGTH_SHORT);
        int mSlideCount = 0;
        SlideshowModel slideShow = mWorkingMessage.getSlideshow();
        if (slideShow != null) {
            mSlideCount = slideShow.size();
        }
        if (mSlideCount >= SlideshowEditor.MAX_SLIDE_NUM && append) {
            mToast.show();
            return false;
        }
        return true;
    }
    
    private void addAttachment(int type, boolean append) {
        // Calculate the size of the current slide if we're doing a replace so the
        // slide size can optionally be used in computing how much room is left for an attachment.
        int currentSlideSize = 0;
        SlideshowModel slideShow = mWorkingMessage.getSlideshow();

        if (append) {
            mAppendAttachmentSign = true;
        } else {
            mAppendAttachmentSign = false;
        }

        if (slideShow != null) {
            SlideModel slide = slideShow.get(0);
            currentSlideSize = slide == null ? 0 : slide.getSlideSize();
        }
        if ((type != AttachmentTypeSelectorAdapter.ADD_SLIDESHOW) && (type != AttachmentTypeSelectorAdapter.ADD_VCARD)
            && (!checkSlideCount(mAppendAttachmentSign))) {
            return;
        }
        
        switch (type) {
            case AttachmentTypeSelectorAdapter.ADD_IMAGE:
                MessageUtils.selectImage(this, REQUEST_CODE_ATTACH_IMAGE);
                break;

            case AttachmentTypeSelectorAdapter.TAKE_PICTURE: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //MTK_OP01_PROTECT_START
                String optr = SystemProperties.get("ro.operator.optr");
                if (optr != null && optr.equals("OP01")) {
                    intent.putExtra("OP01", true);
                } else {
                //MTK_OP01_PROTECT_END
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, TempFileProvider.SCRAP_CONTENT_URI);
                //MTK_OP01_PROTECT_START
                }
                //MTK_OP01_PROTECT_END
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
                break;
            }

            case AttachmentTypeSelectorAdapter.ADD_VIDEO:
                MessageUtils.selectVideo(this, REQUEST_CODE_ATTACH_VIDEO);
                break;

            case AttachmentTypeSelectorAdapter.RECORD_VIDEO: {
                long sizeLimit = 0;
                if (mAppendAttachmentSign) {
                    sizeLimit = computeAttachmentSizeLimitForAppen(slideShow);
                } else {
                    sizeLimit = computeAttachmentSizeLimit(slideShow, currentSlideSize);
                }
                if (sizeLimit > MIN_SIZE_FOR_CAPTURE_VIDEO) {
                    MessageUtils.recordVideo(this, REQUEST_CODE_TAKE_VIDEO, sizeLimit);
                } else {
                    Toast.makeText(this,
                            getString(R.string.space_not_enough),
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case AttachmentTypeSelectorAdapter.ADD_SOUND:
//m0
//                MessageUtils.selectAudio(this, REQUEST_CODE_ATTACH_SOUND);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(getString(R.string.add_music));
                String[] items = new String[2];
                items[0] = getString(R.string.attach_ringtone);
                items[1] = getString(R.string.attach_sound);
                alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                MessageUtils.selectRingtone(ComposeMessageActivity.this, REQUEST_CODE_ATTACH_RINGTONE);
                                break;
                            case 1:
                                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    Toast.makeText(ComposeMessageActivity.this, getString(R.string.Insert_sdcard), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                MessageUtils.selectAudio(ComposeMessageActivity.this, REQUEST_CODE_ATTACH_SOUND);
                                break;
                        }
                    }
                });
                alertBuilder.create().show();
//m1
                break;

            case AttachmentTypeSelectorAdapter.RECORD_SOUND:
                long sizeLimit = 0;
                if (mAppendAttachmentSign) {
                    sizeLimit = computeAttachmentSizeLimitForAppen(slideShow);
                } else {
                    sizeLimit = computeAttachmentSizeLimit(slideShow, currentSlideSize);
                }
                if (sizeLimit > ComposeMessageActivity.MIN_SIZE_FOR_RECORD_AUDIO) {
                    MessageUtils.recordSound(this, REQUEST_CODE_RECORD_SOUND, sizeLimit);
                } else {
                    Toast.makeText(this, getString(R.string.space_not_enough_for_audio), Toast.LENGTH_SHORT).show();
                }
                break;

            case AttachmentTypeSelectorAdapter.ADD_SLIDESHOW:
                editSlideshow();
                break;
                
            case AttachmentTypeSelectorAdapter.ADD_VCARD:
                Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTICONTACTS");
                intent.setType(Contacts.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE_ATTACH_VCARD);
                break;
            case AttachmentTypeSelectorAdapter.ADD_VCALENDAR:
                Intent i = new Intent("android.intent.action.CALENDARCHOICE");
                i.setType("text/x-vcalendar");
                i.putExtra("request_type", 0);
                startActivityForResult(i, REQUEST_CODE_ATTACH_VCALENDAR);
                break;
            default:
                break;
        }
    }

    public static long computeAttachmentSizeLimit(SlideshowModel slideShow, int currentSlideSize) {
        // Computer attachment size limit. Subtract 1K for some text.
        long sizeLimit = MmsConfig.getUserSetMmsSizeLimit(true) - SlideshowModel.SLIDESHOW_SLOP;
        if (slideShow != null) {
            sizeLimit -= slideShow.getCurrentSlideshowSize();

            // We're about to ask the camera to capture some video (or the sound recorder
            // to record some audio) which will eventually replace the content on the current
            // slide. Since the current slide already has some content (which was subtracted
            // out just above) and that content is going to get replaced, we can add the size of the
            // current slide into the available space used to capture a video (or audio).
            sizeLimit += currentSlideSize;
        }
        return sizeLimit;
    }

    public static long computeAttachmentSizeLimitForAppen(SlideshowModel slideShow) {
        long sizeLimit = MmsConfig.getUserSetMmsSizeLimit(true) - SlideshowModel.MMS_SLIDESHOW_INIT_SIZE;
        if (slideShow != null) {
            sizeLimit -= slideShow.getCurrentSlideshowSize();
        }
        if (sizeLimit > 0) {
            return sizeLimit;
        }
        return 0;
    }

    private class SoloAlertDialog extends AlertDialog {
        private AlertDialog mAlertDialog;

        private SoloAlertDialog(Context context) {
            super(context);
        }

        private boolean needShow() {
            return mAlertDialog == null || !mAlertDialog.isShowing();
        }
        public void show(final boolean append) {
            if (!needShow()) {
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setIcon(R.drawable.ic_dialog_attach);
            builder.setTitle(R.string.add_attachment);

            // if (mAttachmentTypeSelectorAdapter == null) {
            // add for vcard, if there is a real slideshow, hide vCard
            int mode = AttachmentTypeSelectorAdapter.MODE_WITH_SLIDESHOW;
            if (mWorkingMessage.hasSlideshow()) {
                mode |= AttachmentTypeSelectorAdapter.MODE_WITHOUT_FILE_ATTACHMENT;
            } else {
                mode |= AttachmentTypeSelectorAdapter.MODE_WITH_FILE_ATTACHMENT;
            }
            if (MessageUtils.isVCalendarAvailable(ComposeMessageActivity.this)) {
                mode |= AttachmentTypeSelectorAdapter.MODE_WITH_VCALENDAR;
            }
            mAttachmentTypeSelectorAdapter = new AttachmentTypeSelectorAdapter(getContext(), mode);
            // }
            builder.setAdapter(mAttachmentTypeSelectorAdapter,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            addAttachment(mAttachmentTypeSelectorAdapter.buttonToCommand(which), append);
                            dialog.dismiss();
                        }
                    });
            mAlertDialog = builder.show();
        }
    }

    private void showAddAttachmentDialog(final boolean append) {
        mSoloAlertDialog.show(append);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (LogTag.VERBOSE) {
            log("requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        }
        boolean needSaveDraft = true;// add this to avoid invoke multi times
        mWaitingForSubActivity = false;          // We're back!
        boolean mNeedAppendAttachment = true;

        
        if (mAppendAttachmentSign == true) {
            mNeedAppendAttachment = true;
        }else if (mAppendAttachmentSign == false) {
            mNeedAppendAttachment = false;
        }
        if (mWorkingMessage.isFakeMmsForDraft()) {
            // We no longer have to fake the fact we're an Mms. At this point we are or we aren't,
            // based on attachments and other Mms attrs.
            mWorkingMessage.removeFakeMmsForDraft();
        }
//a0        
        if (requestCode == REQUEST_CODE_FOR_MULTIDELETE && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("delete_all", false)) {
                mWorkingMessage.discard();

                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                Contact.init(ComposeMessageActivity.this);

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ComposeMessageActivity.this);
                finish();
            }
            return;
        }
//a1
        if (requestCode == REQUEST_CODE_PICK) {
            mWorkingMessage.asyncDeleteDraftSmsMessage(mConversation);
        }

        if (requestCode == REQUEST_CODE_ADD_CONTACT) {
            // The user might have added a new contact. When we tell contacts to add a contact
            // and tap "Done", we're not returned to Messaging. If we back out to return to
            // messaging after adding a contact, the resultCode is RESULT_CANCELED. Therefore,
            // assume a contact was added and get the contact and force our cached contact to
            // get reloaded with the new info (such as contact name). After the
            // contact is reloaded, the function onUpdate() in this file will get called
            // and it will update the title bar, etc.
            if (mAddContactIntent != null) {
                String address =
                    mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.EMAIL);
                if (address == null) {
                    address =
                        mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
                }
                if (address != null) {
                    Contact contact = Contact.get(address, false);
                    if (contact != null) {
                        contact.reload();
                    }
                }
            }
        }

        if (resultCode != RESULT_OK){
            if (LogTag.VERBOSE) log("bail due to resultCode=" + resultCode);
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CREATE_SLIDESHOW:
                if (data != null) {
                    WorkingMessage newMessage = WorkingMessage.load(this, data.getData());
                    if (newMessage != null) {
                        // add for vcard, vcard is exclusive with other attaches, so remove them
                        if (newMessage.hasMediaAttachments()) {
                            newMessage.removeAllFileAttaches();
                        }
//a0
                        boolean isMmsBefore = mWorkingMessage.requiresMms();
                        newMessage.setSubject(mWorkingMessage.getSubject(), false);
//a1
                        mWorkingMessage = newMessage;
                        drawTopPanel(false);
                        updateSendButtonState();
                        invalidateOptionsMenu();

//a0
                        boolean isMmsAfter = mWorkingMessage.requiresMms();
                        if (isMmsAfter && !isMmsBefore) {
                            toastConvertInfo(true);
                        } else if (!isMmsAfter && isMmsBefore) {
                            toastConvertInfo(false);
                        }
//a1
                    }
                }
                break;

            case REQUEST_CODE_TAKE_PICTURE: {
                // create a file based uri and pass to addImage(). We want to read the JPEG
                // data directly from file (using UriImage) instead of decoding it into a Bitmap,
                // which takes up too much memory and could easily lead to OOM.
                String optr = SystemProperties.get("ro.operator.optr");
                //MTK_OP01_PROTECT_START
                if (optr != null && optr.equals("OP01")) {
                    Uri uri = data.getData();
                    addImageAsync(uri, data.getType(), mNeedAppendAttachment);
                } else
                //MTK_OP01_PROTECT_END
                {
                    File file = new File(TempFileProvider.getScrapPath(this));
                    Uri uri = Uri.fromFile(file);
                    addImageAsync(uri,null, mNeedAppendAttachment);
                }
                needSaveDraft = false;
                break;
            }

            case REQUEST_CODE_ATTACH_IMAGE: {
                if (data != null) {
                    addImageAsync(data.getData(),data.getType(), mNeedAppendAttachment);
                }
                needSaveDraft = false;
                break;
            }

            case REQUEST_CODE_TAKE_VIDEO:
                Uri videoUri = TempFileProvider.renameScrapVideoFile(System.currentTimeMillis() + ".3gp", null, this);
                addVideoAsync(videoUri, mNeedAppendAttachment);      // can handle null videoUri
                needSaveDraft = false;
                break;

            case REQUEST_CODE_ATTACH_VIDEO:
                if (data != null) {
                    addVideoAsync(data.getData(), mNeedAppendAttachment);
                }
                needSaveDraft = false;
                break;

            case REQUEST_CODE_ATTACH_SOUND: {
//m0
/*
                Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                    break;
                }
                addAudio(uri);
*/
                addAudio(data.getData(),mNeedAppendAttachment);
//m1
                break;
            }

            case REQUEST_CODE_RECORD_SOUND:
                if (data != null) {
                    addAudio(data.getData(),mNeedAppendAttachment);
                }
                break;

//a0
            case REQUEST_CODE_ATTACH_RINGTONE:
                Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                    break;
                }
                addAudio(uri,mNeedAppendAttachment);
                break;
//a1

            case REQUEST_CODE_ECM_EXIT_DIALOG:
                boolean outOfEmergencyMode = data.getBooleanExtra(EXIT_ECM_RESULT, false);
                if (outOfEmergencyMode) {
                    sendMessage(false);
                }
                break;

            case REQUEST_CODE_PICK:
                if (data != null) {
                    if (mRecipientsEditor != null) {
                    processPickResult(data);
                    } else {
                        mIsRecipientHasIntentNotHandle = true;
                        mIntent = data;
                    }
                }
                misPickContatct = false;
                return;

//a0
            case REQUEST_CODE_TEXT_VCARD:
                if (data != null) {
                    long[] contactIds = data.getLongArrayExtra("com.mediatek.contacts.list.pickcontactsresult");
                    addTextVCardAsync(contactIds);
                } else {
                    Xlog.e(TAG, "data should not be null," + "requestCode=" + requestCode
                            + ", resultCode=" + resultCode + ", data=" + data);
                }
                misPickContatct = false;
                return;
                
            case REQUEST_CODE_ATTACH_VCARD:
                // add for vcard
                asyncAttachVCardByContactsId(data);
                misPickContatct = false;
                isInitRecipientsEditor = false;
                return;
                
            case REQUEST_CODE_ATTACH_VCALENDAR:
                asyncAttachVCalendar(data.getData());
                break;

            case REQUEST_CODE_MULTI_SAVE:
                boolean succeeded = false;
                if (data != null && data.hasExtra("multi_save_result")) {
                    succeeded = data.getBooleanExtra("multi_save_result", false);
                    int resId = succeeded ? R.string.copy_to_sdcard_success : R.string.copy_to_sdcard_fail;
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                }

                return;
//a1
            default:
                if (LogTag.VERBOSE) log("bail due to unknown requestCode=" + requestCode);
                break;
        }
//a0
        isInitRecipientsEditor = false;
        // 181 add for 121871
        if (needSaveDraft) {
            mWorkingMessage.saveDraft(false);
        }
//a1
    }

    private void processPickResult(final Intent data) {
        // The EXTRA_PHONE_URIS stores the phone's urls that were selected by user in the
        // multiple phone picker.
        //m0
        /*final Parcelable[] uris =
            data.getParcelableArrayExtra(Intents.EXTRA_PHONE_URIS);

        final int recipientCount = uris != null ? uris.length : 0;*/
        
        final long[] contactsId = data.getLongArrayExtra("com.mediatek.contacts.list.pickdataresult");
        if (contactsId == null || contactsId.length <= 0) {
            return;
        }
        final int recipientCount = mRecipientsEditor.getRecipientCount() + contactsId.length;
        //m1

        final int recipientLimit = MmsConfig.getSmsRecipientLimit();
        if (recipientLimit != Integer.MAX_VALUE && recipientCount > recipientLimit) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.pick_too_many_recipients)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage(getString(R.string.too_many_recipients, recipientCount, recipientLimit))
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
            return;
        }

        final Handler handler = new Handler();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getText(R.string.adding_recipients));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        final Runnable showProgress = new Runnable() {
            public void run() {
                progressDialog.show();
            }
        };
        // Only show the progress dialog if we can not finish off parsing the return data in 1s,
        // otherwise the dialog could flicker.
        handler.postDelayed(showProgress, 1000);

        new Thread(new Runnable() {
            public void run() {
                final ContactList list = new ContactList();
                 try {
                    //m0
                    //list = ContactList.blockingGetByUris(uris);
                     ContactList selected = ContactList.blockingGetByIds(contactsId);
                    final List<String> numbers = mRecipientsEditor.getNumbers();
                    
                    /*
                    for (Contact c : selected) {
                        // Must remove duplicated number
                        if (!numbers.contains(c.getNumber())) {
                            list.add(c);
                        }
                    }
                    */
                    /* better merge strategy.
                     * Avoid the use of mRecipientsEditor.contrcutionContactsFromInput()
                     * all Contacts in selected list should be added.
                     * */
                    if (numbers.size() > 0) {
                        List<String> selectedNumbers = Arrays.asList(selected.getNumbers());
                        numbers.removeAll(selectedNumbers);
                    
                        for (String number : numbers) {
                            Contact c = Contact.get(number, false);
                            list.add(c);
                            // selected.add(c);
                        }
                    }
                    list.addAll(selected);
                    // list = selected;
                    //m1
                } finally {
                    handler.removeCallbacks(showProgress);
                    progressDialog.dismiss();
                }
                // TODO: there is already code to update the contact header widget and recipients
                // editor if the contacts change. we can re-use that code.
                final Runnable populateWorker = new Runnable() {
                    public void run() {
                        mConversation.setRecipients(list);
                        mRecipientsEditor.populate(list);
                        updateTitle(list);
                    }
                };
                handler.post(populateWorker);
            }
        }).start();
    }

    private void waitForCompressing() {
        synchronized (ComposeMessageActivity.this) {
            while (mCompressingImage) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Xlog.e(TAG, "intterrupted exception e ", e);
                }
            }
        }
    }

    private void notifyCompressingDone() {
        synchronized (ComposeMessageActivity.this) {
            mCompressingImage = false;
            notify();
        }
    }

    private final ResizeImageResultCallback mResizeImageCallback = new ResizeImageResultCallback() {
        // TODO: make this produce a Uri, that's what we want anyway
        public void onResizeResult(PduPart part, boolean append) {
            if (part == null) {
                notifyCompressingDone();
                handleAddAttachmentError(WorkingMessage.UNKNOWN_ERROR, R.string.type_picture);
                return;
            }

//a0
            mWorkingMessage.setmResizeImage(true);
//a1
            Context context = ComposeMessageActivity.this;
            PduPersister persister = PduPersister.getPduPersister(context);
            int result;
//a0
            if(mWorkingMessage.isDiscarded()){
                notifyCompressingDone();
                return;
            }
//a1
            Uri messageUri = mWorkingMessage.getMessageUri();   
            if (null == messageUri) {
                try {
                    messageUri = mWorkingMessage.saveAsMms(true);
                } catch (IllegalStateException e) {
                    notifyCompressingDone();
                    Xlog.e(TAG, e.getMessage() + ", go to ConversationList!");
                    goToConversationList();
                }
            }


//            Uri messageUri = mWorkingMessage.saveAsMms(true);
            if (messageUri == null) {
                result = WorkingMessage.UNKNOWN_ERROR;
            } else {
                try {
                    // it is modifying the mms draft, maybe interlaced with WorkingMessage.saveDraft!
                    Uri dataUri;
                    int mode;
                    synchronized (WorkingMessage.sDraftMmsLock) {
                        dataUri = persister.persistPart(part, ContentUris.parseId(messageUri));
                        mode = mWorkingMessage.sCreationMode;
                        mWorkingMessage.sCreationMode = 0;
                        result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, dataUri, append);
                    }
                    mWorkingMessage.sCreationMode = mode;
                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("ResizeImageResultCallback: dataUri=" + dataUri);
                    }
                } catch (MmsException e) {
                    result = WorkingMessage.UNKNOWN_ERROR;
                }
            }

            handleAddAttachmentError(result, R.string.type_picture);
            if(result == WorkingMessage.OK){
                try {
                    mWorkingMessage.saveAsMms(false);
                } catch (IllegalStateException e) {
                    Xlog.e(TAG, e.getMessage() + ", go to ConversationList!");
                    notifyCompressingDone();
                    goToConversationList();
                } 
            }
            notifyCompressingDone();
        }
    };

    private void handleAddAttachmentError(final int error, final int mediaTypeStringId) {
        if (error == WorkingMessage.OK) {
            return;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                Resources res = getResources();
                String mediaType = res.getString(mediaTypeStringId);
                String title, message;
                
                Xlog.d(TAG, "Error Code:" + error);
                switch(error) {
                case WorkingMessage.WARNING_TYPE:
                case WorkingMessage.UNKNOWN_ERROR:
                    message = res.getString(R.string.error_add_attachment, mediaType);
                    Toast.makeText(ComposeMessageActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                case WorkingMessage.UNSUPPORTED_TYPE:
//a0
                case WorkingMessage.RESTRICTED_TYPE:
//a1
                    title = res.getString(R.string.unsupported_media_format, mediaType);
                    message = res.getString(R.string.select_different_media, mediaType);
                    break;
                case WorkingMessage.MESSAGE_SIZE_EXCEEDED:
                    title = res.getString(R.string.exceed_message_size_limitation, mediaType);
                    message = res.getString(R.string.failed_to_add_media, mediaType);
                    break;
                case WorkingMessage.IMAGE_TOO_LARGE:
                    title = res.getString(R.string.failed_to_resize_image);
                    message = res.getString(R.string.resize_image_error_information);
                    break;
//a0
                case WorkingMessage.RESTRICTED_RESOLUTION:
                    title = res.getString(R.string.select_different_media_type);
                    message = res.getString(R.string.image_resolution_too_large);
                    break;
//a1
                default:
                    throw new IllegalArgumentException("unknown error " + error);
                }

                MessageUtils.showErrorDialog(ComposeMessageActivity.this, title, message);
            }
        });
    }

    private void addImageAsync(final Uri uri, final String mimeType, final boolean append) {
        mCompressingImage = true;
        runAsyncWithDialog(new Runnable() {
            public void run() {
                addImage(mimeType, uri, append);
//                mWorkingMessage.saveAsMms(false);
            }
        }, R.string.adding_attachments_title);
    }

    private void addImage(String mimeType, Uri uri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("addImage: append=" + append + ", uri=" + uri);
        }
        int result = WorkingMessage.OK;
        try {
            mWorkingMessage.checkSizeBeforeAppend();
        } catch (ExceedMessageSizeException e) {
            result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
            notifyCompressingDone();
            handleAddAttachmentError(result, R.string.type_picture);
            return;
        }

        result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, uri, append,mimeType);
        if(result == WorkingMessage.OK){
        	mWorkingMessage.saveAsMms(false);
        }
        if (result == WorkingMessage.IMAGE_TOO_LARGE ||
            result == WorkingMessage.MESSAGE_SIZE_EXCEEDED) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("addImage: resize image " + uri);
            }
//m0
//            MessageUtils.resizeImageAsync(this,
//                   uri, mAttachmentEditorHandler, mResizeImageCallback, append);
            
            // Adjust whether its a DRM IMAGE
            if (FeatureOption.MTK_DRM_APP) {
                if (!MessageUtils.checkUriContainsDrm(this, uri)) {
                    mToastCountForResizeImage++;
                    if (mToastCountForResizeImage == 1) {
                        MessageUtils.resizeImage(this, uri, mAttachmentEditorHandler, mResizeImageCallback, append,
                            true);
                    } else {
                        MessageUtils.resizeImage(this, uri, mAttachmentEditorHandler, mResizeImageCallback, append,
                            false);
                    }
                } else {
                    notifyCompressingDone();
                    handleAddAttachmentError(result, R.string.type_picture);
                }
            } else {
                mToastCountForResizeImage++;
                if (mToastCountForResizeImage == 1) {
                    MessageUtils.resizeImage(this, uri, mAttachmentEditorHandler, mResizeImageCallback, append, true);
                } else {
                    MessageUtils.resizeImage(this, uri, mAttachmentEditorHandler, mResizeImageCallback, append, false);
                }
            }
            return;
        } else if (result == WorkingMessage.WARNING_TYPE) {
            notifyCompressingDone();
            mCreationUri = uri;
            mCreationAppend = append;
            runOnUiThread(new Runnable() {
                public void run() {
                    showConfirmDialog(mCreationUri, mCreationAppend, WorkingMessage.IMAGE, R.string.confirm_restricted_image);
                }
            });
            return;
        }
        notifyCompressingDone();
//m1
        handleAddAttachmentError(result, R.string.type_picture);
    }

    private void addVideoAsync(final Uri uri, final boolean append) {
        runAsyncWithDialog(new Runnable() {
            public void run() {
                addVideo(uri, append);
                //mWorkingMessage.saveAsMms(false);
            }
        }, R.string.adding_attachments_title);
    }

    private void addVideo(Uri uri, boolean append) {
        if (uri != null) {
            int result = WorkingMessage.OK;
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                handleAddAttachmentError(result, R.string.type_picture);
                return;
            }
            result = mWorkingMessage.setAttachment(WorkingMessage.VIDEO, uri, append);
            // m0
            // handleAddAttachmentError(result, R.string.type_video);
            if (result == WorkingMessage.WARNING_TYPE) {
                final boolean mAppend = append;
                final Uri mUri = uri;
                runOnUiThread(new Runnable() {
                    public void run() {
                        showConfirmDialog(mUri, mAppend, WorkingMessage.VIDEO, R.string.confirm_restricted_video);
                    }
                });
            } else {
                handleAddAttachmentError(result, R.string.type_video);
            }
            if (result == WorkingMessage.OK) {
                mWorkingMessage.saveAsMms(false);
            }
            // m1
        }
    }

    private void addAudio(Uri uri) {
        int result = WorkingMessage.OK;
        try {
            mWorkingMessage.checkSizeBeforeAppend();
        } catch (ExceedMessageSizeException e) {
            result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
            handleAddAttachmentError(result, R.string.type_picture);
            return;
        }
        result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri, false);
        // a0
        if (result == WorkingMessage.WARNING_TYPE) {
            final Uri mUriTemp = uri;
            runOnUiThread(new Runnable() {
                public void run() {
                    showConfirmDialog(mUriTemp, false, WorkingMessage.AUDIO, R.string.confirm_restricted_audio);
                }
            });
            return;
        }
        // a1
        handleAddAttachmentError(result, R.string.type_audio);
    }

    /**
     * Asynchronously executes a task while blocking the UI with a progress spinner.
     *
     * Must be invoked by the UI thread.  No exceptions!
     *
     * @param task the work to be done wrapped in a Runnable
     * @param dialogStringId the id of the string to be shown in the dialog
     */
    public void runAsyncWithDialog(final Runnable task, final int dialogStringId) {
        new ModalDialogAsyncTask(dialogStringId).execute(new Runnable[] {task});
    }

    /**
     * Asynchronously performs tasks specified by Runnables.
     * Displays a progress spinner while the tasks are running.  The progress spinner
     * will only show if tasks have not finished after a certain amount of time.
     *
     * This AsyncTask must be instantiated and invoked on the UI thread.
     */
    private class ModalDialogAsyncTask extends AsyncTask<Runnable, Void, Void> {
        final int mDialogStringId;

        /**
         * Creates the Task with the specified string id to be shown in the dialog
         */
        public ModalDialogAsyncTask(int dialogStringId) {
            this.mDialogStringId = dialogStringId;
            // lazy initialization of progress dialog for loading attachments
            if (mProgressDialog == null) {
                mProgressDialog = createProgressDialog();
            }
        }

        /**
         * Initializes the progress dialog with its intended settings.
         */
        private ProgressDialog createProgressDialog() {
            ProgressDialog dialog = new ProgressDialog(ComposeMessageActivity.this);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setMessage(ComposeMessageActivity.this.
                    getText(mDialogStringId));
            return dialog;
        }

        /**
         * Activates a progress spinner on the UI.  This assumes the UI has invoked this Task.
         */
        @Override
        protected void onPreExecute() {
            // activate spinner after half a second
            mAttachmentEditorHandler.postDelayed(mShowProgressDialogRunnable, 500);
        }

        /**
         * Perform the specified Runnable tasks on a background thread
         */
        @Override
        protected Void doInBackground(Runnable... params) {
            if (params != null) {
                try {
                    for (int i = 0; i < params.length; i++) {
                        params[i].run();
                    }
                } finally {
                    // Cancel pending display of the progress bar if the image has finished loading.
                    mAttachmentEditorHandler.removeCallbacks(mShowProgressDialogRunnable);
                }
            }
            return null;
        }

        /**
         * Deactivates the progress spinner on the UI. This assumes the UI has invoked this Task.
         */
        @Override
        protected void onPostExecute(Void result) {

            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = null;
            }
        }
    }

    // Add for vCard begin
    private void setFileAttachment(final String fileName, final int type, final boolean append) {
        final File attachFile = getFileStreamPath(fileName);
        Xlog.d(TAG, "setFileAttachment(): attachFile.exists()?=" + attachFile.exists() + ", attachFile.length()=" + attachFile.length());
        final Resources res = getResources();
        if (attachFile.exists() && attachFile.length() > 0) {
            Uri attachUri = Uri.fromFile(attachFile);
            int result = WorkingMessage.OK;
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                handleAddAttachmentError(result, R.string.type_picture);
                return;
            }
            result = mWorkingMessage.setAttachment(type, attachUri, append);
            handleAddAttachmentError(result, R.string.type_common_file);
        } else {
            mUiHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(ComposeMessageActivity.this, 
                            res.getString(R.string.failed_to_add_media, fileName), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // the uri must be a vcard uri created by Contacts
    private void attachVCardByUri(Uri uri) {
        if (uri == null) {
            return;
        }
        final String filename = getVCardFileName();
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getContentResolver().openInputStream(uri);
                out = openFileOutput(filename, Context.MODE_PRIVATE);
                byte[] buf = new byte[8096];
                int size = 0;
                while ((size = in.read(buf)) != -1) {
                    out.write(buf, 0, size);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception attachVCardByUri ", e);
        }
        setFileAttachment(filename, WorkingMessage.VCARD, false);
    }

    private void asyncAttachVCalendar(final Uri eventUri) {
        if (eventUri == null) {
            return;
        }
        runAsyncWithDialog(new Runnable() {
            public void run() {
                attachVCalendar(eventUri);
                mWorkingMessage.saveDraft(false);
            }
        }, R.string.adding_attachments_title);
    }

    private void attachVCalendar(Uri eventUri) {
        if (eventUri == null) {
            Log.e(TAG, "attachVCalendar, oops uri is null");
            return;
        }
        int result = WorkingMessage.OK;
        try {
            mWorkingMessage.checkSizeBeforeAppend();
        } catch (ExceedMessageSizeException e) {
            result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
            handleAddAttachmentError(result, R.string.type_picture);
            return;
        }
        result = mWorkingMessage.setAttachment(WorkingMessage.VCALENDAR, eventUri, false);
        handleAddAttachmentError(result, R.string.type_common_file);
    }

    private void asyncAttachVCardByContactsId(final Intent data) {
        if (data == null) {
            return;
        }
        runAsyncWithDialog(new Runnable() {
            public void run() {
                mWorkingMessage.setIsDeleteVcardFile(false);
                long[] contactsId = data.getLongArrayExtra("com.mediatek.contacts.list.pickcontactsresult");
                attachVCardByContactsId(contactsId);
                mWorkingMessage.saveDraft(false);
                mWorkingMessage.setIsDeleteVcardFile(true);
            }
        }, R.string.adding_attachments_title);
    }

    private void attachVCardByContactsId(long[] contactsIds) {
        // make contacts' id string
        StringBuilder contactsIdsStr = new StringBuilder("");
        for (int i = 0; i < contactsIds.length-1; i++) {
            contactsIdsStr.append(contactsIds[i]);
            contactsIdsStr.append(',');
        }
        contactsIdsStr.append(contactsIds[contactsIds.length-1]);
        final String ids = contactsIdsStr.toString();
        if (!ids.equals("")) {
            AttachVCardWorkerThread worker = new AttachVCardWorkerThread(ids);
            worker.run();
        }
    }

    // turn contacts id into *.vcf file attachment
    private class AttachVCardWorkerThread extends Thread {
        private String mContactIds;

        public AttachVCardWorkerThread(String ids) {
            mContactIds = ids;
        }

        @Override
        public void run() {
            final String fileName = getVCardFileName();
            try {
                VCardComposer composer = null;
                Writer writer = null;
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE)));
                    composer = new VCardComposer(ComposeMessageActivity.this);
                    if (!composer.init(Contacts._ID + " IN (" + mContactIds + ")", null)) {
                        // fall through to catch clause
                        throw new VCardException("Canot initialize " + composer.getErrorReason());
                    }
                    while (!composer.isAfterLast()) {
                        writer.write(composer.createOneEntry());
                    }
                } finally {
                    if (composer != null) {
                        composer.terminate();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (VCardException e) {
                Log.e(TAG, "export vcard file, vcard exception " + e.getMessage());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "export vcard file, file not found exception " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "export vcard file, IO exception " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "export vcard file, exception " + e.getMessage());
            }

            Xlog.d(TAG, "write vCard file done!");
            setFileAttachment(fileName, WorkingMessage.VCARD, false);
        }
    }

    private String getVCardFileName() {
        final String fileExtension = ".vcf";
        // base on time stamp
        String name = DateFormat.format("yyyyMMdd_hhmmss", new Date(System.currentTimeMillis())).toString();
        name = name.trim();
        return name + fileExtension;
    }
    // Add for vCard end

    private boolean handleForwardedMessage() {
        Intent intent = getIntent();

        // If this is a forwarded message, it will have an Intent extra
        // indicating so.  If not, bail out.
        if (intent.getBooleanExtra(FORWARD_MESSAGE, false) == false) {
            return false;
        }

        Uri uri = intent.getParcelableExtra("msg_uri");

        if (Log.isLoggable(LogTag.APP, Log.DEBUG)) {
            log("handle forwarded message " + uri);
        }

        if (uri != null) {
            mWorkingMessage = WorkingMessage.load(this, uri);
            mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
            
            SlideshowModel mSlideshowModel = mWorkingMessage.getSlideshow();
            if (mSlideshowModel != null) {
                int mSsmSize = mSlideshowModel.size();
                for (int index = 0; index < mSsmSize; index++) {
                    SlideModel mSlideModel = mSlideshowModel.get(index);
                    if (mSlideModel != null) {
                        if (mSlideModel.hasText()) {
                            TextModel mTextModel = mSlideModel.getText();
                            String textChar = mTextModel.getText();
                            long textLength = textChar.length();
                            if (textLength > MmsConfig.getMaxTextLimit()) {
                                mTextModel.setText(textChar.substring(0, MmsConfig.getMaxTextLimit()));
                            }
                        }
                    }
                }
            }
        } else {
        	String smsAddress = null;
        	if (intent.hasExtra(SMS_ADDRESS)) {
        		smsAddress = intent.getStringExtra(SMS_ADDRESS);
        		if (smsAddress != null){
//        		TODO need re-coding
//                    mRecipientsEditor.addRecipient(smsAddress, true);
                }
        	}
            mWorkingMessage.setText(intent.getStringExtra(SMS_BODY));
        }

        // let's clear the message thread for forwarded messages
        mMsgListAdapter.changeCursor(null);

        return true;
    }

    // Handle send actions, where we're told to send a picture(s) or text.
    private boolean handleSendIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return false;
        }
        mSimDis = intent.getIntExtra("SimDis", -1);
//a0
        //add for saveAsMms
        mWorkingMessage.setConversation(mConversation);
//a1

        final String mimeType = intent.getType();
        String action = intent.getAction();
        Xlog.i(TAG, "Get mimeType: " + mimeType);
        Xlog.i(TAG, "Get action: " + action);
        Xlog.i(TAG, "Get simDis: " + mSimDis);
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                final Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                if (mimeType.equals("text/plain")) {
                    String fileName = "";
                    if (uri != null) {
                        String mUriStr = Uri.decode(uri.toString());
                        fileName = mUriStr.substring(mUriStr.lastIndexOf("/") + 1, mUriStr.length());
                    }
                    String mMessage = this.getString(R.string.failed_to_add_media, fileName);
                    Toast.makeText(this, mMessage, Toast.LENGTH_SHORT).show();
                    return false;
                }
                runAsyncWithDialog(new Runnable() {
                    public void run() {
                        addAttachment(mimeType, uri, false);
                        //a0
                        SlideshowModel mSlideShowModel = mWorkingMessage.getSlideshow();
                        if (mSlideShowModel != null && mSlideShowModel.getCurrentSlideshowSize() > 0) {
                            mWorkingMessage.saveAsMms(false);
                        }
                        //a1
                    }
                }, R.string.adding_attachments_title);
//a0
//                SlideshowModel mSlideShowModel = mWorkingMessage.getSlideshow();
//                if (mSlideShowModel != null && mSlideShowModel.getCurrentSlideshowSize() > 0) {
//                    mWorkingMessage.saveAsMms(false);
//                }
                intent.setAction(SIGN_CREATE_AFTER_KILL_BY_SYSTEM);
//                mWorkingMessage.saveDraft(false);
//a1
                return true;
            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                mWorkingMessage.setText(extras.getString(Intent.EXTRA_TEXT));
                intent.setAction(SIGN_CREATE_AFTER_KILL_BY_SYSTEM);
                return true;
            }
        } else if (LEMEIINTENT.equals(action)) {
        	mIsLeMei = true;
            mWorkingMessage.setSubject(getString(R.string.cu_subject), true);
            mWorkingMessage.setMessageClassMini();
            mAttachmentEditor.update(mWorkingMessage, true);
            updateTextEditorHeightInFullScreen();
            
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
                addLeMeiAttachment(mimeType, uri, false);
                return true;
            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                mWorkingMessage.setText(extras.getString(Intent.EXTRA_TEXT));
                return true;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) &&
                extras.containsKey(Intent.EXTRA_STREAM)) {
            SlideshowModel slideShow = mWorkingMessage.getSlideshow();
            final ArrayList<Parcelable> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            int currentSlideCount = slideShow != null ? slideShow.size() : 0;
            int importCount = uris.size();
            if (importCount + currentSlideCount > SlideshowEditor.MAX_SLIDE_NUM) {
                importCount = Math.min(SlideshowEditor.MAX_SLIDE_NUM - currentSlideCount,
                        importCount);
                Toast.makeText(ComposeMessageActivity.this,
                        getString(R.string.too_many_attachments,
                                SlideshowEditor.MAX_SLIDE_NUM, importCount),
                                Toast.LENGTH_LONG).show();
            }

//m0
/*
            // Attach all the pictures/videos off of the UI thread.
            // Show a progress alert if adding all the slides hasn't finished
            // within one second.
            // Stash the runnable for showing it away so we can cancel
            // it later if adding completes ahead of the deadline.
            final AlertDialog dialog = new AlertDialog.Builder(ComposeMessageActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.adding_attachments_title)
                .setMessage(R.string.adding_attachments)
                .create();
            final Runnable showProgress = new Runnable() {
                public void run() {
                    dialog.show();
                }
            };
            // Schedule it for one second from now.
            mAttachmentEditorHandler.postDelayed(showProgress, 1000);

            final int numberToImport = importCount;
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < numberToImport; i++) {
                        Parcelable uri = uris.get(i);
                        addAttachment(mimeType, (Uri) uri, true);
                    }
                    // Cancel pending show of the progress alert if necessary.
                    mAttachmentEditorHandler.removeCallbacks(showProgress);
                    dialog.dismiss();
                }
            }, "addAttachment").start();
*/
            // Attach all the pictures/videos asynchronously off of the UI thread.
            // Show a progress dialog if adding all the slides hasn't finished
            // within half a second.
            final int numberToImport = importCount;
            Xlog.i(TAG, "numberToImport: " + numberToImport);
            final WorkingMessage msg = mWorkingMessage;
            runAsyncWithDialog(new Runnable() {
                public void run() {
                    mToastCountForResizeImage = 0;
                    for (int i = 0; i < numberToImport; i++) {
                        Parcelable uri = uris.get(i);

                        String scheme = ((Uri)uri).getScheme();
                        if (scheme != null && scheme.equals("file")) {
                            // change "file://..." Uri to "Content://...., and attemp to add this attachment"
                            addFileAttachment(mimeType, (Uri)uri, true);  
                        } else {
                            addAttachment(mimeType, (Uri) uri, true);
                        }
                    }
                    mToastCountForResizeImage = 0;
                    SlideshowModel mSlideShowModel = mWorkingMessage.getSlideshow();
                    if (mSlideShowModel != null && mSlideShowModel.size() > 0) {
                        mWorkingMessage.saveAsMms(false);
                    }
//                    msg.saveDraft(false);
                }
            }, R.string.adding_attachments_title);
            intent.setAction(SIGN_CREATE_AFTER_KILL_BY_SYSTEM);
            return true;
        }else if(SIGN_CREATE_AFTER_KILL_BY_SYSTEM.equals(action))
        {
            return true;
        }
        return false;
    }

    // mVideoUri will look like this: content://media/external/video/media
    private static final String mVideoUri = Video.Media.getContentUri("external").toString();
    // mImageUri will look like this: content://media/external/images/media
    private static final String mImageUri = Images.Media.getContentUri("external").toString();
    
//a0
    // mAudioUri will look like this: content://media/external/images/media
    private static final String mAudioUri = Audio.Media.getContentUri("external").toString();
//a1

    private void addAttachment(String type, Uri uri, boolean append) {
        if (uri != null) {
            // When we're handling Intent.ACTION_SEND_MULTIPLE, the passed in items can be
            // videos, and/or images, and/or some other unknown types we don't handle. When
            // a single attachment is "shared" the type will specify an image or video. When
            // there are multiple types, the type passed in is "*/*". In that case, we've got
            // to look at the uri to figure out if it is an image or video.
            boolean wildcard = "*/*".equals(type);
            Xlog.i(TAG, "Got send intent mimeType :" + type);
            if (type.startsWith("image/") || (wildcard && uri.toString().startsWith(mImageUri))) {
                addImage(type,uri, append);
            } else if (type.startsWith("video/") ||
                    (wildcard && uri.toString().startsWith(mVideoUri))) {
                addVideo(uri, append);
            }
//a0 
            else if (type.startsWith("audio/") || type.equals("application/ogg")
                || (wildcard && uri.toString().startsWith(mAudioUri))) {
                addAudio(uri, append);
            } else if (type.equalsIgnoreCase("text/x-vcard")) {
                // add for vcard
                attachVCardByUri(uri);
            } else if (type.equalsIgnoreCase("text/x-vcalendar")) {
                // add for vcalendar
                attachVCalendar(uri);
            }
//a1
        }
    }

    private String getResourcesString(int id, String mediaName) {
        Resources r = getResources();
        return r.getString(id, mediaName);
    }

    private void drawBottomPanel() {
        // Reset the counter for text editor.
        mDrawBottomPanel = false;
        resetCounter();

        //m0
        /*if (mWorkingMessage.hasSlideshow()) {
            mBottomPanel.setVisibility(View.GONE);
            mAttachmentEditor.requestFocus();
            return;
        }*/
        if (mWorkingMessage.hasSlideshow() && !mMsgListAdapter.mIsDeleteMode) {
            mBottomPanel.setVisibility(View.GONE);
            mDeletePanel.setVisibility(View.GONE);
            mAttachmentEditor.update(mWorkingMessage);
            mAttachmentEditor.requestFocus();
            return;
        }
        //add for multi-delete
        if (mMsgListAdapter.mIsDeleteMode) {
            mBottomPanel.setVisibility(View.GONE);
            mAttachmentEditor.hideView();
            mDeletePanel.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mParams.height = getActionBar().getHeight();
            mDeletePanel.setLayoutParams(mParams);
            return;
        } 

        mDeletePanel.setVisibility(View.GONE);
        mAttachmentEditor.update(mWorkingMessage);
        updateTextEditorHeightInFullScreen();
        //m1

        mBottomPanel.setVisibility(View.VISIBLE);

        CharSequence text = mWorkingMessage.getText();

        // TextView.setTextKeepState() doesn't like null input.
        if (text != null) {
            mTextEditor.setTextKeepState(text);
//a0
            mTextEditor.setSelection(text.length());
//a1
        } else {
            mTextEditor.setText("");
        }
    }

    private void drawTopPanel(boolean showSubjectEditor) {
//m0
/*
        boolean showingAttachment = mAttachmentEditor.update(mWorkingMessage);
        mAttachmentEditorScrollView.setVisibility(showingAttachment ? View.VISIBLE : View.GONE);
        showSubjectEditor(showSubjectEditor || mWorkingMessage.hasSubject());
*/

        //add for multi-delete
        if (mMsgListAdapter.mIsDeleteMode) {
            //mSelectPanel.setVisibility(View.VISIBLE);
            //mSelectedAll.setChecked(false);
        } else {
//m0
            //mSelectPanel.setVisibility(View.GONE);
//m1
            boolean showingAttachment = mAttachmentEditor.update(mWorkingMessage);
            updateTextEditorHeightInFullScreen();
//            mAttachmentEditorScrollView.setVisibility(showingAttachment ? View.VISIBLE : View.GONE);
        }
        showSubjectEditor(mWorkingMessage.hasSubject() && !mMsgListAdapter.mIsDeleteMode);
//m1
    }

    //==========================================================
    // Interface methods
    //==========================================================

    public void onClick(View v) {
//m0
/*        if ((v == mSendButtonSms || v == mSendButtonMms) && isPreparedForSending()) {
            confirmSendMessageIfNeeded();
        }
*/
        if (v == mSendButtonSms || v == mSendButtonMms) {
            if (mSendButtonCanResponse) {
                mSendButtonCanResponse = false;
                if (isPreparedForSending()) {
                    // Since sending message here, why not disable button 'Send'??
                    updateSendButtonState(false);
                    //simSelection();
                    checkRecipientsCount();
                    mUiHandler.sendEmptyMessageDelayed(MSG_RESUME_SEND_BUTTON, RESUME_BUTTON_INTERVAL);
                    //mSendButtonCanResponse = true;
                } else {
                    mSendButtonCanResponse = true;
                    if (!isHasRecipientCount()) {
                        new AlertDialog.Builder(this)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(R.string.cannot_send_message)
                            .setMessage(R.string.cannot_send_message_reason)
                            .setPositiveButton(R.string.yes,new CancelSendingListener()).show();
                    } else {
                        new AlertDialog.Builder(this)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle( R.string.cannot_send_message)
                            .setMessage(R.string.cannot_send_message_reason_no_content)
                            .setPositiveButton(R.string.yes,new CancelSendingListener()).show();

                    }
                }
            }

        } else if (v == mDeleteButton){ 
            if (mMsgListAdapter.getSelectedNumber() >= mMsgListAdapter.getCount()) {
                ArrayList<Long> threadIds = new ArrayList<Long> (1);
                threadIds.add(mConversation.getThreadId());
                ConversationList.confirmDeleteThreadDialog(
                        new ConversationList.DeleteThreadListener(threadIds,
                                mBackgroundQueryHandler, ComposeMessageActivity.this),
                                threadIds, false, ComposeMessageActivity.this);
            } else {
                confirmMultiDelete();
            }
        } else if (v == mCancelButton) {
            if (mMsgListAdapter.getSelectedNumber() > 0) {
                mIsSelectedAll = false;
                markCheckedState(mIsSelectedAll);
            }
        } else if (v == mSelectAllButton) {
            if (!mIsSelectedAll) {
                mIsSelectedAll = true;
                markCheckedState(mIsSelectedAll);
            }
        }
//m0
        else if ((v == mRecipientsPicker)) {
//m0
            //launchMultiplePhonePicker();
            if (recipientCount() >= RECIPIENTS_LIMIT_FOR_SMS) {
                Toast.makeText(ComposeMessageActivity.this, R.string.cannot_add_recipient, Toast.LENGTH_SHORT).show();
            } else {
                addContacts(-1);
            }
//m1
        }
    }

    private void launchMultiplePhonePicker() {
        Intent intent = new Intent(Intents.ACTION_GET_MULTIPLE_PHONES);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType(Phone.CONTENT_TYPE);
        // We have to wait for the constructing complete.
        ContactList contacts = mRecipientsEditor.constructContactsFromInput(true);
        int recipientsCount = 0;
        int urisCount = 0;
        Uri[] uris = new Uri[contacts.size()];
        urisCount = 0;
        for (Contact contact : contacts) {
            if (Contact.CONTACT_METHOD_TYPE_PHONE == contact.getContactMethodType()) {
                    uris[urisCount++] = contact.getPhoneUri();
            }
        }
        if (urisCount > 0) {
            intent.putExtra(Intents.EXTRA_PHONE_URIS, uris);
        }
        startActivityForResult(intent, REQUEST_CODE_PICK);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null) {
            // if shift key is down, then we want to insert the '\n' char in the TextView;
            // otherwise, the default action is to send the message.
            if (!event.isShiftPressed()) {
//m0
/*
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                }
*/
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (isPreparedForSending()) {
                    //simSelection();
                    checkRecipientsCount();
                } else {
                        if (!isHasRecipientCount()) {
                            new AlertDialog.Builder(this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle(R.string.cannot_send_message)
                                .setMessage(R.string.cannot_send_message_reason)
                                .setPositiveButton(R.string.yes,new CancelSendingListener()).show();

                        } else {
                            new AlertDialog.Builder(this)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setTitle(R.string.cannot_send_message)
                                .setMessage(R.string.cannot_send_message_reason_no_content)
                                .setPositiveButton(R.string.yes,new CancelSendingListener()).show();
                        }
                    }
//m1
                return true;
            }
            return false;
        }

        if (isPreparedForSending()) {
//m0
//            confirmSendMessageIfNeeded();
            checkRecipientsCount();
//m1
        }
//a0
        else {
            if (!isHasRecipientCount()) {
                new AlertDialog.Builder(this).setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(R.string.cannot_send_message)
                    .setMessage(R.string.cannot_send_message_reason)
                    .setPositiveButton(R.string.yes,new CancelSendingListener()).show();
            } else {
                new AlertDialog.Builder(this)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(R.string.cannot_send_message)
                    .setMessage(R.string.cannot_send_message_reason_no_content)
                    .setPositiveButton(R.string.yes, new CancelSendingListener()).show();

            }
        }
//a1
        return true;
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // This is a workaround for bug 1609057.  Since onUserInteraction() is
            // not called when the user touches the soft keyboard, we pretend it was
            // called when textfields changes.  This should be removed when the bug
            // is fixed.
            onUserInteraction();

            mWorkingMessage.setText(s);
//a0
            mAttachmentEditor.onTextChangeForOneSlide(s);
//a1

            updateSendButtonState();

            updateCounter(s, start, before, count);

//m0
//            ensureCorrectButtonHeight();
//m1
        }

        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * Ensures that if the text edit box extends past two lines then the
     * button will be shifted up to allow enough space for the character
     * counter string to be placed beneath it.
     */
    private void ensureCorrectButtonHeight() {
        int currentTextLines = mTextEditor.getLineCount();
        if (currentTextLines <= 2) {
            mTextCounter.setVisibility(View.GONE);
        }
        else if (currentTextLines > 2 && mTextCounter.getVisibility() == View.GONE) {
            // Making the counter invisible ensures that it is used to correctly
            // calculate the position of the send button even if we choose not to
            // display the text.
            mTextCounter.setVisibility(View.INVISIBLE);
        }
    }

    private final TextWatcher mSubjectEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mWorkingMessage.setSubject(s, true);
//a0
            mTextCounter.setVisibility(View.GONE);
//a1
        }

        public void afterTextChanged(Editable s) { }
    };

    //==========================================================
    // Private methods
    //==========================================================

    /**
     * Initialize all UI elements from resources.
     */
    private void initResourceRefs() {
        mHeightChangedLinearLayout = (HeightChangedLinearLayout) findViewById(R.id.changed_linear_layout);
        mHeightChangedLinearLayout.setLayoutSizeChangedListener(mLayoutSizeChangedListener);
        mMsgListView = (MessageListView) findViewById(R.id.history);
        mMsgListView.setDivider(null);      // no divider so we look like IM conversation.

        // called to enable us to show some padding between the message list and the
        // input field but when the message list is scrolled that padding area is filled
        // in with message content
        mMsgListView.setClipToPadding(false);

        // turn off children clipping because we draw the border outside of our own
        // bounds at the bottom.  The background is also drawn in code to avoid drawing
        // the top edge.
        mMsgListView.setClipChildren(false);

        mBottomPanel = findViewById(R.id.bottom_panel);
        //a0
        mDeletePanel = findViewById(R.id.delete_panel);
        mSelectAllButton = (ImageButton) findViewById(R.id.select_all);
        mSelectAllButton.setOnClickListener(this);
        mCancelButton = (ImageButton) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);
        mDeleteButton = (ImageButton) findViewById(R.id.delete);
        mDeleteButton.setOnClickListener(this);
        //a1
        mTextEditor = (EditText) findViewById(R.id.embedded_text_editor);
//m0
//        mTextEditor.setOnEditorActionListener(this);
//m1
        mTextEditor.addTextChangedListener(mTextEditorWatcher);
        mTextEditor.setFilters(new InputFilter[] {
                new TextLengthFilter(MmsConfig.getMaxTextLimit())});
        mTextCounter = (TextView) findViewById(R.id.text_counter);
        mSendButtonMms = (TextView) findViewById(R.id.send_button_mms);
        mSendButtonSms = (ImageButton) findViewById(R.id.send_button_sms);
        mSendButtonMms.setOnClickListener(this);
        mSendButtonSms.setOnClickListener(this);
        mTopPanel = findViewById(R.id.recipients_subject_linear);
        mTopPanel.setFocusable(false);
        mAttachmentEditor = (AttachmentEditor) findViewById(R.id.attachment_editor);
        mAttachmentEditor.setHandler(mAttachmentEditorHandler);
//        mAttachmentEditorScrollView = findViewById(R.id.attachment_editor_scroll_view);
    }

    private void confirmDeleteDialog(OnClickListener listener, boolean locked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(locked ? R.string.confirm_dialog_locked_title :
            R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(locked ? R.string.confirm_delete_locked_message :
                    R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    void undeliveredMessageDialog(long date) {
        String body;

        if (date >= 0) {
            body = getString(R.string.undelivered_msg_dialog_body,
                    MessageUtils.formatTimeStampString(this, date));
        } else {
            // FIXME: we can not get sms retry time.
            body = getString(R.string.undelivered_sms_dialog_body);
        }

        Toast.makeText(this, body, Toast.LENGTH_LONG).show();
    }

    private void startMsgListQuery() {
        startMsgListQuery(500);
    }

    private void startMsgListQuery(int delay) {
        Xlog.d(TAG, "startMsgListQuery,timeout=" + delay);
//MTK_OP01_PROTECT_START
        if (MmsConfig.getMmsDirMode()) {
            return;
        }
//MTK_OP01_PROTECT_END
        if (isRecipientsEditorVisible()) {
            return;
        }
        final Uri conversationUri = mConversation.getUri();

        if (conversationUri == null) {
            log("##### startMsgListQuery: conversationUri is null, bail!");
            return;
        }

        final long threadId = mConversation.getThreadId();
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("startMsgListQuery for " + conversationUri + ", threadId=" + threadId);
        }

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        try {
            // Kick off the new query
//m0
/*
            mBackgroundQueryHandler.startQuery(
                    MESSAGE_LIST_QUERY_TOKEN,
                    threadId, // cookie
                    conversationUri,
                    PROJECTION,
                    null, null, null);
*/
            mBackgroundQueryHandler.postDelayed(new Runnable() {
                public void run() {
                    Xlog.d(TAG, "mListQueryRunnable, to query, " + "activity=" + ComposeMessageActivity.this);
                    if (mMsgListAdapter.getOnDataSetChangedListener() == null) {
                        Xlog.d(TAG, "mListQueryRunnable, no listener");
                        return;
                    }
                    mBackgroundQueryHandler.startQuery(
                            MESSAGE_LIST_QUERY_TOKEN, threadId, conversationUri,
                            PROJECTION, null, null, null);
                }
            }, delay);
//m1
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private static int CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT = 100;
    private MyScrollListener mScrollListener = new MyScrollListener(CHANGE_SCROLL_LISTENER_MIN_CURSOR_COUNT, "MessageList_Scroll_Tread");
    private void initMessageList() {
        if (mMsgListAdapter != null) {
            return;
        }

        String highlightString = getIntent().getStringExtra("highlight");
        Pattern highlight = highlightString == null
            ? null
            : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);

        // Initialize the list adapter with a null cursor.
        mMsgListAdapter = new MessageListAdapter(this, null, mMsgListView, true, highlight);
        //add for cmcc dir ui begin
        if (MmsConfig.getMmsDirMode()) {
            mMsgListView.setVisibility(View.GONE);
            return;
        }
        //add for cmcc dir ui end
        mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
        mMsgListView.setAdapter(mMsgListAdapter);
        mMsgListView.setItemsCanFocus(false);
        mMsgListView.setVisibility(View.VISIBLE);
        mMsgListView.setOnCreateContextMenuListener(mMsgListMenuCreateListener);
        mMsgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    ((MessageListItem) view).onMessageListItemClick();
                }
            }
        });
        mMsgListView.setOnScrollListener(mScrollListener);
        mMsgListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideInputMethod();
                return false;
            }
        });
    }

    private void loadDraft() {
        if (mWorkingMessage.isWorthSaving()) {
            Log.w(TAG, "loadDraft() called with non-empty working message");
            return;
        }

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("loadDraft() call WorkingMessage.loadDraft");
        }

        mWorkingMessage = WorkingMessage.loadDraft(this, mConversation);
    }

    private void saveDraft(boolean isStopping) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("saveDraft");
        }
        // TODO: Do something better here.  Maybe make discard() legal
        // to call twice and make isEmpty() return true if discarded
        // so it is caught in the clause above this one?
        if (mWorkingMessage.isDiscarded()) {
            return;
        }

        if (!mWaitingForSubActivity &&
                !mWorkingMessage.isWorthSaving() &&
                (!isRecipientsEditorVisible() || recipientCount() == 0)) {
            if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("not worth saving, discard WorkingMessage and bail");
            }
            mWorkingMessage.discard(false);
            return;
        }

        mWorkingMessage.saveDraft(isStopping);

        if (mToastForDraftSave) {
            Toast.makeText(this, R.string.message_saved_as_draft,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPreparedForSending() {
//m0
/*
        int recipientCount = recipientCount();

        return recipientCount > 0 && recipientCount <= MmsConfig.getRecipientLimit() &&
            (mWorkingMessage.hasAttachment() ||
                    mWorkingMessage.hasText() ||
                    mWorkingMessage.hasSubject());
*/
        if (isRecipientsEditorVisible()) {
            String recipientText = mRecipientsEditor.getText() == null? "" : mRecipientsEditor.getText().toString();
            return mSimCount> 0 && (recipientText != null && !recipientText.equals("")) && (mWorkingMessage.hasAttachment() || mWorkingMessage.hasText());
        } else {
            return mSimCount> 0 && (mWorkingMessage.hasAttachment() || mWorkingMessage.hasText());
        }
//m1
    }

    private int recipientCount() {
        int recipientCount;

        // To avoid creating a bunch of invalid Contacts when the recipients
        // editor is in flux, we keep the recipients list empty.  So if the
        // recipients editor is showing, see if there is anything in it rather
        // than consulting the empty recipient list.
        if (isRecipientsEditorVisible()) {
            recipientCount = mRecipientsEditor.getRecipientCount();
        } else {
            recipientCount = getRecipients().size();
        }
        return recipientCount;
    }

    private void sendMessage(boolean bCheckEcmMode) {
//a0
        if (mWorkingMessage.requiresMms() && (mWorkingMessage.hasSlideshow() || mWorkingMessage.hasAttachment())) {
            if (mWorkingMessage.getCurrentMessageSize() > MmsConfig.getUserSetMmsSizeLimit(true)) {
                MessageUtils.showErrorDialog(ComposeMessageActivity.this,
                        getResourcesString(R.string.exceed_message_size_limitation),
                        getResourcesString(R.string.exceed_message_size_limitation));
                mLastButtonClickTime = -DOUBLECLICK_INTERVAL_TIME;
                updateSendButtonState();
                return;
            }
        }
//a1
        if (bCheckEcmMode) {
            // TODO: expose this in telephony layer for SDK build
            String inEcm = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (Boolean.parseBoolean(inEcm)) {
                try {
                    startActivityForResult(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                            REQUEST_CODE_ECM_EXIT_DIALOG);
                    return;
                } catch (ActivityNotFoundException e) {
                    // continue to send message
                    Log.e(TAG, "Cannot find EmergencyCallbackModeExitDialog", e);
                }
            }
        }

//a0
        ContactList contactList = isRecipientsEditorVisible() ?
            mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
        mDebugRecipients = contactList.serialize();
//a1

        if (!mSendingMessage) {
            if (LogTag.SEVERE_WARNING) {
                String sendingRecipients = mConversation.getRecipients().serialize();
                if (!sendingRecipients.equals(mDebugRecipients)) {
                    String workingRecipients = mWorkingMessage.getWorkingRecipients();
                    if (!mDebugRecipients.equals(workingRecipients)) {
                        LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity.sendMessage" +
                                " recipients in window: \"" +
                                mDebugRecipients + "\" differ from recipients from conv: \"" +
                                sendingRecipients + "\" and working recipients: " +
                                workingRecipients, this);
                    }
                }
                sanityCheckConversation();
            }

            // send can change the recipients. Make sure we remove the listeners first and then add
            // them back once the recipient list has settled.
            removeRecipientsListeners();

//m0
//            mWorkingMessage.send(mDebugRecipients);
            //add for gemini TODO
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                mWorkingMessage.sendGemini(mSelectedSimId);
            } else {
                mWorkingMessage.send(mDebugRecipients);
            }
            Xlog.d(TAG, "Compose.sendMessage(): after sendMessage. mConversation.ThreadId=" + mConversation.getThreadId()
                    + ", MessageCount=" + mConversation.getMessageCount());
//m1
            /*
             *   If message count is 0, it should be a new message.
             *   After tap send button, the sent message will have draft flag for a short time.
             *   That means, the message count will be 0 for a short time.
             *   If user tap home key in this short time, it will change the conversation id to 0 in the method savedraft().
             *   When the screen is back to Message Composer, it will query database with thread(conversation) id 0.
             *   So, it will query for nothing. The screen is always blank.
             *   Fix this issue by force to set message count with 1.
             */
            if (mConversation.getMessageCount() == 0) {
                mConversation.setMessageCount(1);
            }
            mSentMessage = true;
            mSendingMessage = true;
//a0
            mWaitingForSendMessage = true;
            isInitRecipientsEditor = false; // when tap fail icon, don't add recipients
//a1
            addRecipientsListeners();
//a0
            mMsgListView.setVisibility(View.VISIBLE);
//a1
        }
        // But bail out if we are supposed to exit after the message is sent.
        if (mExitOnSent || mIsLeMei) {
            finish();
        }
    }

    private void resetMessage() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("resetMessage");
        }
        updateTitle(mConversation.getRecipients());

        // Make the attachment editor hide its view.
        mAttachmentEditor.hideView();
//        mAttachmentEditorScrollView.setVisibility(View.GONE);

        // Hide the subject editor.
        showSubjectEditor(false);

        // Focus to the text editor.
        mTextEditor.requestFocus();

        // We have to remove the text change listener while the text editor gets cleared and
        // we subsequently turn the message back into SMS. When the listener is listening while
        // doing the clearing, it's fighting to update its counts and itself try and turn
        // the message one way or the other.
        mTextEditor.removeTextChangedListener(mTextEditorWatcher);

        // Clear the text box.
        TextKeyListener.clear(mTextEditor.getText());

        mWorkingMessage.clearConversation(mConversation, false);
        mWorkingMessage = WorkingMessage.createEmpty(this);
        mWorkingMessage.setConversation(mConversation);

        hideRecipientEditor();
        drawBottomPanel();

        // "Or not", in this case.
        updateSendButtonState();

        // Our changes are done. Let the listener respond to text changes once again.
        mTextEditor.addTextChangedListener(mTextEditorWatcher);

        // Close the soft on-screen keyboard if we're in landscape mode so the user can see the
        // conversation.
        if (mIsLandscape) {
//m0
/*
            InputMethodManager inputMethodManager =
                (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

            inputMethodManager.hideSoftInputFromWindow(mTextEditor.getWindowToken(), 0);
*/
            hideInputMethod();
//m1
        }

        mLastRecipientCount = 0;
        mSendingMessage = false;
        invalidateOptionsMenu();
        // update list, this must put after hideRecipientEditor(); to avoid a bug.
        startMsgListQuery(0);
   }

    private void updateSendButtonState() {
        boolean enable = false;
//m0
/*
        if (isPreparedForSending()) {
            // When the type of attachment is slideshow, we should
            // also hide the 'Send' button since the slideshow view
            // already has a 'Send' button embedded.
            if (!mWorkingMessage.hasSlideshow()) {
                enable = true;
            } else {
                mAttachmentEditor.setCanSend(true);
            }
        } else if (null != mAttachmentEditor){
            mAttachmentEditor.setCanSend(false);
        }
*/
        if (isPreparedForSending()) {
            Xlog.v(TAG, "updateSendButtonState(): mSimCount = " + mSimCount);
            if (FeatureOption.MTK_GEMINI_SUPPORT == true && mSimCount > 0) {
                // When the type of attachment is slideshow, we should
                // also hide the 'Send' button since the slideshow view
                // already has a 'Send' button embedded.
                if (!mWorkingMessage.hasSlideshow()) {
                    enable = true;
                } else {
                    mAttachmentEditor.setCanSend(true);
                }
            } else {
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    try {
                        if (phone.isSimInsert(0)) { // check SIM state
                            if (!mWorkingMessage.hasSlideshow()) {
                                enable = true;
                            } else {
                                mAttachmentEditor.setCanSend(true);
                            }
                        } else {
                            mAttachmentEditor.setCanSend(false);
                        }
                    } catch (RemoteException e) {
                        Xlog.w(TAG, "compose.updateSendButton()_singleSIM");
                    }
                }
            }
        } else {
            mAttachmentEditor.setCanSend(false);
        }
//m1

        View sendButton = showSmsOrMmsSendButton(mWorkingMessage.requiresMms());
        sendButton.setEnabled(enable);
        sendButton.setFocusable(enable);
    }

    private long getMessageDate(Uri uri) {
        if (uri != null) {
            Cursor cursor = SqliteWrapper.query(this, mContentResolver,
                    uri, new String[] { Mms.DATE }, null, null, null);
            if (cursor != null) {
                try {
                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                        return cursor.getLong(0) * 1000L;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return NO_DATE_FOR_DIALOG;
    }

//m0
//    private void initActivityState(Intent intent) {
//        // If we have been passed a thread_id, use that to find our conversation.
//        long threadId = intent.getLongExtra("thread_id", 0);
//        if (threadId > 0) {
//            if (LogTag.VERBOSE) log("get mConversation by threadId " + threadId);
//            mConversation = Conversation.get(this, threadId, false);
//        } else {
//            Uri intentData = intent.getData();
//            if (intentData != null) {
//                // try to get a conversation based on the data URI passed to our intent.
//                if (LogTag.VERBOSE) log("get mConversation by intentData " + intentData);
//                mConversation = Conversation.get(this, intentData, false);
//                mWorkingMessage.setText(getBody(intentData));
//            } else {
//                // special intent extra parameter to specify the address
//                String address = intent.getStringExtra("address");
//                if (!TextUtils.isEmpty(address)) {
//                    if (LogTag.VERBOSE) log("get mConversation by address " + address);
//                    mConversation = Conversation.get(this, ContactList.getByNumbers(address,
//                            false /* don't block */, true /* replace number */), false);
//                } else {
//                    if (LogTag.VERBOSE) log("create new conversation");
//                    mConversation = Conversation.createNew(this);
//                }
//            }
//        }
//        addRecipientsListeners();
//
//        mExitOnSent = intent.getBooleanExtra("exit_on_sent", false);
//        if (intent.hasExtra("sms_body")) {
//            mWorkingMessage.setText(intent.getStringExtra("sms_body"));
//        }
//        mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
//    }
    private void initActivityState(Bundle bundle) {
        Intent intent = getIntent();
        mIsTooManyRecipients = false;
        if (bundle != null) {
            mCompressingImage = bundle.getBoolean("compressing_image", false);
            String recipientsStr = bundle.getString("recipients");
            int recipientCount = 0;
            if(recipientsStr != null){
                recipientCount = recipientsStr.split(";").length;
                mConversation = Conversation.get(this,
                    ContactList.getByNumbers(this, recipientsStr,
                            false /* don't block */, true /* replace number */), false);
            } else {
                Long threadId = bundle.getLong("thread", 0);
                mConversation = Conversation.get(this, threadId, false);
                mWorkingMessage.setConversation(mConversation);
            }
            
            mExitOnSent = bundle.getBoolean("exit_on_sent", false);
            // add for cmcc dir ui begin
            if (MmsConfig.getMmsDirMode()) {
                mExitOnSent = true;
            }
            // add for cmcc dir ui end
            mWorkingMessage.readStateFromBundle(bundle);
            if (!mCompressingImage && mConversation.hasDraft() && mConversation.getMessageCount() == 0) {
                mWorkingMessage.clearConversation(mConversation, true);
            }
            if (recipientCount > RECIPIENTS_LIMIT_FOR_SMS) {
                mIsTooManyRecipients = true;
            }
            mCompressingImage = false;
            return;
        }

        String vCardContactsIds = intent.getStringExtra("multi_export_contacts");
        long[] contactsIds = null;
        if (vCardContactsIds != null && !vCardContactsIds.equals("")) {
            String[] vCardConIds = vCardContactsIds.split(",");
            Xlog.e(TAG, "ComposeMessage.initActivityState(): vCardConIds.length" + vCardConIds.length);
            contactsIds = new long[vCardConIds.length];
            try {
                for (int i = 0; i < vCardConIds.length; i++) {
                    contactsIds[i] = Long.parseLong(vCardConIds[i]);
                }
            } catch (NumberFormatException e) {
                contactsIds = null;
            }
        }
        // If we have been passed a thread_id, use that to find our
        // conversation.
        long threadId = intent.getLongExtra("thread_id", 0);
        if (threadId > 0) {
            mConversation = Conversation.get(this, threadId, false);
        } else if (contactsIds != null && contactsIds.length > 0) {
            addTextVCard(contactsIds);
            mConversation = Conversation.createNew(this);
            return;
        } else {
            Uri intentData = intent.getData();

            // If intent is SEND, just create a new empty thread, otherwise Conversation.get() will
            // throw exception.
            String action = intent.getAction();
            if (intentData != null && (TextUtils.isEmpty(action) || !action.equals(Intent.ACTION_SEND))) {
                // group-contact send message
                // try to get a conversation based on the data URI passed to our intent.
                if (intentData.getPathSegments().size() < 2) {
                    mConversation = mConversation.get(this,
                            ContactList.getByNumbers(this,
                                    getStringForMultipleRecipients(Conversation.getRecipients(intentData)),
                                    false /* don't block */, true /* replace number */),
                            false);
                } else {
                    mConversation = Conversation.get(this, intentData, false);
                }
                mWorkingMessage.setText(getBody(intentData));
            } else {
                // special intent extra parameter to specify the address
                String address = intent.getStringExtra("address");
                if (!TextUtils.isEmpty(address)) {
                    mConversation = Conversation.get(this, ContactList.getByNumbers(address,
                            false /* don't block */, true /* replace number */), false);
                } else {
                    mConversation = Conversation.createNew(this);
                }
            }
        }
        mExitOnSent = intent.getBooleanExtra("exit_on_sent", false);
        // add for cmcc dir ui begin
        if (MmsConfig.getMmsDirMode()) {
            mExitOnSent = true;
        }
        // add for cmcc dir ui end
        if (intent.hasExtra("sms_body")) {
            String sms_body = intent.getStringExtra("sms_body");
            if (sms_body != null && sms_body.length() > MmsConfig.getMaxTextLimit()) {
                mWorkingMessage.setText(sms_body.subSequence(0, MmsConfig.getMaxTextLimit()));
            } else {
                mWorkingMessage.setText(sms_body);
            }
        }
        mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
        
        //a0
        send_sim_id = intent.getIntExtra(com.android.internal.telephony.Phone.GEMINI_SIM_ID_KEY, -1);
        Xlog.d(TAG, "init get simId from intent = " + send_sim_id);
        //a1
    }
//m1

    private void initFocus() {
        if (!mIsKeyboardOpen) {
            return;
        }

        // If the recipients editor is visible, there is nothing in it,
        // and the text editor is not already focused, focus the
        // recipients editor.
        if (isRecipientsEditorVisible()
                && TextUtils.isEmpty(mRecipientsEditor.getText())
                && !mTextEditor.isFocused()) {
            mRecipientsEditor.requestFocus();
            return;
        }

        // If we decided not to focus the recipients editor, focus the text editor.
        mTextEditor.requestFocus();
    }

    private final MessageListAdapter.OnDataSetChangedListener
                    mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
        public void onDataSetChanged(MessageListAdapter adapter) {
            mPossiblePendingNotification = true;
        }

        public void onContentChanged(MessageListAdapter adapter) {
            if (LogTag.VERBOSE) {
                log("MessageListAdapter.OnDataSetChangedListener.onContentChanged");
            }

            if (mMsgListAdapter != null &&
                mMsgListAdapter.getOnDataSetChangedListener() != null) {
                Xlog.d(TAG, "OnDataSetChangedListener is not cleared");
                startMsgListQuery();
            } else {
                Xlog.d(TAG, "OnDataSetChangedListener is cleared");
            }
        }
    };

    private void checkPendingNotification() {
        if (mPossiblePendingNotification && hasWindowFocus()) {
            mConversation.markAsRead();
            mPossiblePendingNotification = false;
        }
    }

//m0
//    private final class BackgroundQueryHandler extends AsyncQueryHandler {
    private final class BackgroundQueryHandler extends BaseProgressQueryHandler {
//m1
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Xlog.d(TAG, "onQueryComplete, token=" + token + "activity=" + ComposeMessageActivity.this);
            switch(token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    if (cursor == null) {
                        Xlog.w(TAG, "onQueryComplete, cursor is null.");
                        return;
                    }
                    if (mMsgListAdapter == null) {
                        Xlog.w(TAG, "onQueryComplete, mMsgListAdapter is null.");
                        cursor.close();
                        return;
                    }
                    if (mMsgListAdapter.getOnDataSetChangedListener() == null) {
                        Xlog.d(TAG, "OnDataSetChangedListener is cleared");
                        cursor.close();
                        return;
                    }
                    
                    // check consistency between the query result and 'mConversation'
                    long tid = (Long) cookie;

                    if (LogTag.VERBOSE) {
                        log("##### onQueryComplete: msg history result for threadId " + tid);
                    }
                    if (tid != mConversation.getThreadId()) {
                        log("onQueryComplete: msg history query result is for threadId " +
                                tid + ", but mConversation has threadId " +
                                mConversation.getThreadId() + " starting a new query");
                        // since this query is invalid the cursor is useless, we need close it
                        // or cursor leak is happened.
                        cursor.close();
                        startMsgListQuery();
                        return;
                    }

                    // check consistency b/t mConversation & mWorkingMessage.mConversation
                    ComposeMessageActivity.this.sanityCheckConversation();

                    int newSelectionPos = -1;
                    long targetMsgId = getIntent().getLongExtra("select_id", -1);
                    if (targetMsgId != -1) {
                        cursor.moveToPosition(-1);
                        while (cursor.moveToNext()) {
                            long msgId = cursor.getLong(COLUMN_ID);
                            if (msgId == targetMsgId) {
                                newSelectionPos = cursor.getPosition();
                                break;
                            }
                        }
                    }
                    if (mNeedUpdateContactForMessageContent) {
                        updateContactCache(cursor);
                        mNeedUpdateContactForMessageContent = false;
                    }

                    //a0
                    //add for multi-delete
                    Xlog.i(TAG, "compose.onContentChanged(): onContentChanged()");
                    if (mMsgListAdapter.mIsDeleteMode) {
                        mMsgListAdapter.initListMap(cursor);
                    }

                    changeDeleteMode();
                    //a1

                    mMsgListAdapter.changeCursor(cursor);

                    if (newSelectionPos != -1) {
                        mMsgListView.setSelection(newSelectionPos);
                    }
                    // Adjust the conversation's message count to match reality. The
                    // conversation's message count is eventually used in
                    // WorkingMessage.clearConversation to determine whether to delete
                    // the conversation or not.
                    if (mMsgListAdapter.getCount() == 0 && mWaitingForSendMessage) {
                        mConversation.setMessageCount(1);
                    } else {
                        mConversation.setMessageCount(mMsgListAdapter.getCount());
                    }
                    Xlog.d(TAG, "onQueryComplete(): Conversation.ThreadId=" + mConversation.getThreadId()
                            + ", MessageCount=" + mConversation.getMessageCount());

                    // Once we have completed the query for the message history, if
                    // there is nothing in the cursor and we are not composing a new
                    // message, we must be editing a draft in a new conversation (unless
                    // mSentMessage is true).
                    // Show the recipients editor to give the user a chance to add
                    // more people before the conversation begins.
                    if (cursor.getCount() == 0 && !isRecipientsEditorVisible() && !mSentMessage) {
                        initRecipientsEditor();
                    }

                    // FIXME: freshing layout changes the focused view to an unexpected
                    // one, set it back to TextEditor forcely.
                    mTextEditor.requestFocus();

                    mConversation.blockMarkAsRead(false);

                    invalidateOptionsMenu();    // some menu items depend on the adapter's count
                    return;

                case ConversationList.HAVE_LOCKED_MESSAGES_TOKEN:
                    ArrayList<Long> threadIds = (ArrayList<Long>)cookie;
                    ConversationList.confirmDeleteThreadDialog(
                            new ConversationList.DeleteThreadListener(threadIds,
                                mBackgroundQueryHandler, ComposeMessageActivity.this),
                            threadIds,
                            cursor != null && cursor.getCount() > 0,
                            ComposeMessageActivity.this);
                    break;
            }
        }

        // Scan Sms body and update contact cache
        private void updateContactCache(Cursor cursor) {
            if (cursor != null) {
                Set<SpannableString> msgs = new HashSet<SpannableString>();
                while (cursor.moveToNext()) {
                    String smsBody = cursor.getString(MessageListAdapter.COLUMN_SMS_BODY);

                    if (smsBody == null) {
                        continue;
                    }

                    SpannableString msg = new SpannableString(smsBody);
                    msgs.add(msg);
                }
                // update the contact cache in an async thread to avoid ANR
                updateContactCacheAsync(msgs);
            }
        }

        private void updateContactCacheAsync(final Set<SpannableString> msgs) {
            new Thread(new Runnable() {
                public void run() {
                    Set<String> uriSet = new HashSet<String>();
                    for (SpannableString msg : msgs) {
                        Linkify.addLinks(msg, Linkify.ALL);
                        List<String> uris = MessageUtils.extractUris(msg.getSpans(0, msg.length(),
                                URLSpan.class));
                        for (String uri : uris) {
                            uriSet.add(uri);
                        }
                    }
                    for (String uri : uriSet) {
                        String[] body = uri.toLowerCase().split("tel:");
                        if (body.length > 1) {
                            Contact.get(body[1].trim(), false);
                        }
                    }
                }
            }).start();
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch(token) {
                case ConversationList.DELETE_CONVERSATION_TOKEN:
//m0
/*
                    mConversation.setMessageCount(0);
                    // fall through
*/
                    try {
                        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                        if(phone != null) {
                            if(phone.isTestIccCard()) {
                                Xlog.d(TAG, "All messages has been deleted, send notification...");
                                SmsManager.getDefault().setSmsMemoryStatus(true);
                            }
                        } else {
                            Xlog.d(TAG, "Telephony service is not available!");
                        }
                    } catch(Exception ex) {
                        Xlog.e(TAG, "" + ex.getMessage());
                    }
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            ComposeMessageActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(ComposeMessageActivity.this);
                    if (mMsgListAdapter.mIsDeleteMode) {
                        changeDeleteMode();
                    }
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    if (mDeleteActionMode != null) {
                        mDeleteActionMode.finish();
                    }
                    break;
//m1
                case DELETE_MESSAGE_TOKEN:
//m0
/*
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            ComposeMessageActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
*/
                    Xlog.d(TAG, "onDeleteComplete(): before update mConversation, ThreadId = " + mConversation.getThreadId());
                    mConversation = Conversation.upDateThread(ComposeMessageActivity.this, mConversation.getThreadId(), false);
                    mThreadCountManager.isFull(mThreadId, ComposeMessageActivity.this, 
                            ThreadCountManager.OP_FLAG_DECREASE);
                    // Update the notification for new messages since they
                    // may be deleted.
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                            ComposeMessageActivity.this, false, false);
                    // Update the notification for failed messages since they
                    // may be deleted.
                    updateSendFailedNotification();
                    MessagingNotification.updateDownloadFailedNotification(ComposeMessageActivity.this);
                    Xlog.d(TAG, "onDeleteComplete(): MessageCount = " + mConversation.getMessageCount() + 
                            ", ThreadId = " + mConversation.getThreadId());
                    if (mConversation.getMessageCount() <= 0 || mConversation.getThreadId() <= 0l) {
                        finish();
                    }
                    if (progress()) {
                        dismissProgressDialog();
                    }
                    
                    if (mDeleteActionMode != null) {
                        mDeleteActionMode.finish();
                    }
//m1
                    break;
            }
            // If we're deleting the whole conversation, throw away
            // our current working message and bail.
            if (token == ConversationList.DELETE_CONVERSATION_TOKEN) {
                mWorkingMessage.discard();

                // Rebuild the contacts cache now that a thread and its associated unique
                // recipients have been deleted.
                Contact.init(ComposeMessageActivity.this);

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ComposeMessageActivity.this);
                finish();
            }
        }
    }

    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            int[] icons = SmileyParser.DEFAULT_SMILEY_RES_IDS;
            String[] names = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_NAMES);
            final String[] texts = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_TEXTS);

            final int N = names.length;

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
                // We might have different ASCII for the same icon, skip it if
                // the icon is already added.
                boolean added = false;
                for (int j = 0; j < i; j++) {
                    if (icons[i] == icons[j]) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();

                    entry. put("icon", icons[i]);
                    entry. put("name", names[i]);
                    entry.put("text", texts[i]);

                    entries.add(entry);
                }
            }

            final SimpleAdapter a = new SimpleAdapter(
                    this,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[] {"icon", "name", "text"},
                    new int[] {R.id.smiley_icon, R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = getResources().getDrawable((Integer)data);
                        ((ImageView)view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(getString(R.string.menu_insert_smiley));

            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);

                    String smiley = (String)item.get("text");
                    if (mSubjectTextEditor != null && mSubjectTextEditor.hasFocus()) {
                        insertText(mSubjectTextEditor, smiley);
                    } else {
                        insertText(mTextEditor, smiley);
                    }

                    dialog.dismiss();
                }
            });

            mSmileyDialog = b.create();
        }

        mSmileyDialog.show();
    }

    public void onUpdate(final Contact updated) {
        // Using an existing handler for the post, rather than conjuring up a new one.
        mMessageListItemHandler.post(new Runnable() {
            public void run() {
                if (isRecipientsEditorVisible()) {
                    String recipientsString = mRecipientsEditor.getText().toString();
                    if (recipientsString.endsWith(",") || recipientsString.contains(updated.getNumber())) {
                        ContactList recipients = mRecipientsEditor.constructContactsFromInput(false);
                        mRecipientsEditor.populate(recipients);
                        updateTitle(recipients);
                        if (mRecipientsEditor.hasFocus()) {
                            recipientsString = mRecipientsEditor.getText().toString();
                            mRecipientsEditor.setSelection(recipientsString.length());
                        }
                    }
                } else {
                    ContactList recipients = getRecipients();
                    updateTitle(recipients);

                    // The contact information for one (or more) of the recipients has changed.
                    // Rebuild the message list so each MessageItem will get the last contact info.
                    ComposeMessageActivity.this.mMsgListAdapter.notifyDataSetChanged();
                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("[CMA] onUpdate contact updated: " + updated);
                        log("[CMA] onUpdate recipients: " + recipients);
                    }
                }
            }
        });
    }

    private void addRecipientsListeners() {
        Contact.addListener(this);
    }

    private void removeRecipientsListeners() {
        Contact.removeListener(this);
    }

    private void clearPendingProgressDialog() {
        // remove any callback to display a progress spinner
        mAttachmentEditorHandler.removeCallbacks(mShowProgressDialogRunnable);
        // clear the dialog so any pending dialog.dismiss() call can be avoided
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static Intent createIntent(Context context, long threadId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);

        if (threadId > 0) {
            intent.setData(Conversation.getUri(threadId));
        }

        return intent;
    }

    private String getBody(Uri uri) {
        if (uri == null) {
            return null;
        }
        String urlStr = uri.getSchemeSpecificPart();
        if (!urlStr.contains("?")) {
            return null;
        }
        urlStr = urlStr.substring(urlStr.indexOf('?') + 1);
        String[] params = urlStr.split("&");
        for (String p : params) {
            if (p.startsWith("body=")) {
                try {
                    return URLDecoder.decode(p.substring(5), "UTF-8");
                } catch (UnsupportedEncodingException e) { }
            }
        }
        return null;
    }
//a0
    public static final int REQUEST_CODE_ATTACH_RINGTONE  = 20;
    public static final int REQUEST_CODE_ATTACH_VCARD     = 21;
    public static final int REQUEST_CODE_TEXT_VCARD       = 22;
    public static final int REQUEST_CODE_MULTI_SAVE       = 23;
    public static final int REQUEST_CODE_LOAD_DRAFT       = 24;
    public static final int REQUEST_CODE_ATTACH_VCALENDAR = 25;

    private static final int MENU_SAVE_MESSAGE_TO_SIM   = 32;
    private static final int MENU_PREVIEW               = 33;
    private static final int MENU_SEND_SMS              = 34;
    private static final int MENU_ADD_TO_BOOKMARK       = 35;

    private static final int DEFAULT_LENGTH             = 40;
    private static final int RECIPIENTS_LIMIT_FOR_SMS   = MmsConfig.getSmsRecipientLimit();

    private static final String VCARD_INTENT            = "com.android.contacts.pickphoneandemail";
    private static final String FOR_MULTIDELETE         = "ForMultiDelete";
    private static final String NUMBER_ADD_CONTACT_ACTION ="android.intent.action.INSERT_OR_EDIT";

    // for save message to sim card
    private static final int SIM_SELECT_FOR_SEND_MSG                    = 1;
    private static final int SIM_SELECT_FOR_SAVE_MSG_TO_SIM             = 2;
    private static final int MSG_QUIT_SAVE_MESSAGE_THREAD               = 100;
    private static final int MSG_SAVE_MESSAGE_TO_SIM                    = 102;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM   = 104;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_SUCCEED            = 106;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC     = 108;
    private static final int MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL    = 110;
    private static final int MSG_RESUME_SEND_BUTTON                     = 112;
    private static final String SELECT_TYPE                             = "Select_type";

    //add for multi-delete
    private TextView mSelectedConvCount;
    private View mDeletePanel;              // View containing the delete and cancel buttons
    private View mSelectPanel;              // View containing the select all check box
    private ImageButton mSelectAllButton;
    private ImageButton mDeleteButton;
    private ImageButton mCancelButton;
    private boolean mIsSelectedAll;

    //add for gemini
    private int mSelectedSimId;

    //add for Gemini Enhancement
    private View mRecipientsAvatar;         // View Recipients stub
//    private ContactHeaderWidget mContactHeader;
    private ImageButton mPickContacts;      // title bar button
    private ImageButton mJumpToContacts;    // title bar button
    private int mSimCount;
    private List<SIMInfo> mSimInfoList;
    private int mAssociatedSimId;
    private long mMessageSimId;
    private long mDataConnectionSimId;
    private StatusBarManager mStatusBarManager;
    private ComponentName mComponentName;

    private boolean mIsTooManyRecipients;   // Whether the recipients are too many
    private boolean isInitRecipientsEditor = true;    // true, init mRecipientsEditor and add recipients;
                                                      // false, init mRecipientsEditor, but recipients
    private boolean mWaitingForSendMessage;

    private AlertDialog mSIMSelectDialog;
    private AlertDialog mQuickTextDialog;
    private AlertDialog mDetailDialog;
    private AlertDialog mSendDialog;

    private static final String STR_RN = "\\r\\n"; // for "\r\n"
    private static final String STR_CN = "\n"; // the char value of '\n'
    public static boolean mDestroy = false;
    private boolean misPickContatct = false;
    private ThreadCountManager mThreadCountManager = ThreadCountManager.getInstance();
    private Long mThreadId = -1l;

    private ArrayList<String> mURLs = new ArrayList<String>();
    private String mSizeLimitTemp;
    private int mMmsSizeLimit;
    private final String ARABIC = "ar";
    private static CellConnMgr mCellMgr = null;
    private static int mCellMgrRegisterCount = 0;
    private Handler mIndicatorHandler;
    private static Activity sCompose = null;
    private Handler mSaveMsgHandler = null;
    private Thread mSaveMsgThread = null;
    private boolean mSendButtonCanResponse = true;    // can click send button
    private static final long RESUME_BUTTON_INTERVAL = 1000;
    boolean mClickCanResponse = true;         // can click button or some view items
    
    // handle NullPointerException in onActivityResult() for pick up recipients
    private boolean mIsRecipientHasIntentNotHandle = false;                                                             //
    private Intent mIntent = null;
    
    private ActionMode mDeleteActionMode;
    private HeightChangedLinearLayout mHeightChangedLinearLayout;

    private static final int mReferencedTextEditorThreeLinesHeight = 110;
    private static final int mReferencedTextEditorFourLinesHeight  = 140;
    private static final int mReferencedTextEditorSevenLinesHeight = 224;
    private static final int mReferencedAttachmentEditorHeight     = 266;
    private static final int mReferencedMaxHeight                  = 800;
    private int mCurrentMaxHeight                                  = 800;

    //a0
    //add this can send msg from a marked sim card which is delivered in Intent.
    private int send_sim_id = -1;
    //a1

    private MessageItem mMsgItem = null;
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SAVE_MESSAGE_TO_SIM_SUCCEED:
                Toast.makeText(ComposeMessageActivity.this, R.string.save_message_to_sim_successful, Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC:
                Toast.makeText(ComposeMessageActivity.this, R.string.save_message_to_sim_unsuccessful, Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL:
                Toast.makeText(ComposeMessageActivity.this, 
                        getString(R.string.save_message_to_sim_unsuccessful) + ". " + getString(R.string.sim_full_title), 
                        Toast.LENGTH_SHORT).show();
                break;

            case MSG_SAVE_MESSAGE_TO_SIM:
                String type = (String)msg.obj;
                long msgId = msg.arg1;
                saveMessageToSim(type, msgId);
                break;
            case MSG_RESUME_SEND_BUTTON:
                mSendButtonCanResponse = true;
                break;
            default:
                Log.d(TAG, "inUIHandler msg unhandled.");
                break;
            }
        }
    };

    private final class SaveMsgThread extends Thread {
        private String msgType = null;
        private long msgId = 0;
        public SaveMsgThread(String type, long id) {
            msgType = type;
            msgId = id;
        }
        public void run() {
            Looper.prepare();
            if (null != Looper.myLooper()) {
                mSaveMsgHandler = new SaveMsgHandler(Looper.myLooper());
            }
            Message msg = mSaveMsgHandler.obtainMessage(MSG_SAVE_MESSAGE_TO_SIM);
            msg.arg1 = (int)msgId;
            msg.obj = msgType;
            if (FeatureOption.MTK_GEMINI_SUPPORT && mSimCount > 1) {
                mUiHandler.sendMessage(msg);
            } else {
                mSaveMsgHandler.sendMessage(msg);
            }
            Looper.loop();
        }
    }

    private final class SaveMsgHandler extends Handler {
        public SaveMsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUIT_SAVE_MESSAGE_THREAD: {
                    Xlog.v(MmsApp.TXN_TAG, "exit save message thread");
                    getLooper().quit();
                    break;
                }

                case MSG_SAVE_MESSAGE_TO_SIM: {
                    String type = (String)msg.obj;
                    long msgId = msg.arg1;
                    //saveMessageToSim(type, msgId);
                    getMessageAndSaveToSim(type, msgId);
                    break;
                }

                case MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM: {
                    Intent it = (Intent)msg.obj;
                    getMessageAndSaveToSim(it);
                    break;
                }

                default:
                    break;
            }
        }
    }

    private void saveMessageToSim(String msgType, long msgId) {
        Xlog.d(MmsApp.TXN_TAG, "save message to sim, message type:" + msgType 
                + "; message id:" + msgId + "; sim count:" + mSimCount);

        Intent intent = new Intent();
        intent.putExtra("message_type", msgType);
        intent.putExtra("message_id", msgId);
        intent.putExtra(SELECT_TYPE, SIM_SELECT_FOR_SAVE_MSG_TO_SIM);
        showSimSelectedDialog(intent);
    }

    private void getMessageAndSaveToSim(Intent intent) {
        Xlog.v(MmsApp.TXN_TAG, "get message and save to sim, selected sim id = " + mSelectedSimId);
        String msgType = intent.getStringExtra("message_type");
        long msgId = intent.getLongExtra("message_id", 0);
        if (msgType == null) {
            //mSaveMsgHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC);
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC);            
            return;
        }
        getMessageAndSaveToSim(msgType, msgId);
    }

    private void getMessageAndSaveToSim(String msgType, long msgId){
        int result = 0;
        MessageItem msgItem = getMessageItem(msgType, msgId, true);
        if (msgItem == null || msgItem.mBody == null) {
            Xlog.e(MmsApp.TXN_TAG, "getMessageAndSaveToSim, can not get Message Item.");
            return;
        }
        
        String scAddress = null;
   
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = null;
        messages = smsManager.divideMessage(msgItem.mBody);

        int smsStatus = 0;
        long timeStamp = 0;
        if (msgItem.isReceivedMessage()) {
            smsStatus = SmsManager.STATUS_ON_ICC_READ;
            timeStamp = msgItem.mSmsDate;
            scAddress = msgItem.getServiceCenter();
        } else if (msgItem.isSentMessage()) {
            smsStatus = SmsManager.STATUS_ON_ICC_SENT;
        } else if (msgItem.isFailedMessage()) {
            smsStatus = SmsManager.STATUS_ON_ICC_UNSENT;
        } else {
            Xlog.w(MmsApp.TXN_TAG, "Unknown sms status");
        }

        if (scAddress == null) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                scAddress = TelephonyManagerEx.getDefault().getScAddress(SIMInfo.getSlotById(this, mSelectedSimId));
            } else {
                scAddress = TelephonyManagerEx.getDefault().getScAddress(0);
            }
        }

        Xlog.d(MmsApp.TXN_TAG, "\t scAddress\t= " + scAddress);
        Xlog.d(MmsApp.TXN_TAG, "\t Address\t= " + msgItem.mAddress);
        Xlog.d(MmsApp.TXN_TAG, "\t msgBody\t= " + msgItem.mBody);
        Xlog.d(MmsApp.TXN_TAG, "\t smsStatus\t= " + smsStatus);
        Xlog.d(MmsApp.TXN_TAG, "\t timeStamp\t= " + timeStamp);


        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int slotId = -1;
            if (mSimCount == 1) {
                slotId = mSimInfoList.get(0).mSlot;
            } else {
                slotId = SIMInfo.getSlotById(this, mSelectedSimId);
            }
            Xlog.d(MmsApp.TXN_TAG, "\t slot Id\t= " + slotId);

            result = GeminiSmsManager.copyTextMessageToIccCardGemini(scAddress, 
                    msgItem.mAddress, messages, smsStatus, timeStamp, slotId);
        } else {
            
            result = SmsManager.getDefault().copyTextMessageToIccCard(scAddress, 
                    msgItem.mAddress, messages, smsStatus, timeStamp);
        }

        if (result == SmsManager.RESULT_ERROR_SUCCESS) {
            Xlog.d(MmsApp.TXN_TAG, "save message to sim succeed.");
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_SUCCEED);            
        } else if (result == SmsManager.RESULT_ERROR_SIM_MEM_FULL) {
            Xlog.w(MmsApp.TXN_TAG, "save message to sim failed: sim memory full.");
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_SIM_FULL);
        } else {
            Xlog.w(MmsApp.TXN_TAG, "save message to sim failed: generic error.");
            mUiHandler.sendEmptyMessage(MSG_SAVE_MESSAGE_TO_SIM_FAILED_GENERIC);
        }
        mSaveMsgHandler.sendEmptyMessageDelayed(MSG_QUIT_SAVE_MESSAGE_THREAD, 5000);
    }

    private boolean isRestrictedType(long msgId){
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            int width = 0;
            int height = 0;
            String type = new String(part.getContentType());

            int mediaTypeStringId;
            if (ContentType.isVideoType(type)) {
                mediaTypeStringId = R.string.type_video;
            } else if (ContentType.isAudioType(type) || "application/ogg".equalsIgnoreCase(type)) {
                mediaTypeStringId = R.string.type_audio;
            } else if (ContentType.isImageType(type)) {
                mediaTypeStringId = R.string.type_picture;
                InputStream input = null;
                try {
                    input = this.getContentResolver().openInputStream(part.getDataUri());
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(input, null, opt);
                    width = opt.outWidth;
                    height = opt.outHeight;
                } catch (FileNotFoundException e) {
                    // Ignore
                    Xlog.e(TAG, "FileNotFoundException caught while opening stream", e);
                } finally {
                    if (null != input) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            // Ignore
                            Xlog.e(TAG, "IOException caught while closing stream", e);
                        }
                    }
                }
            } else {
                continue;
            }
            if (!ContentType.isRestrictedType(type) || width > MmsConfig.getMaxRestrictedImageWidth()
                    || height > MmsConfig.getMaxRestrictedImageHeight()){
                if (WorkingMessage.sCreationMode == WorkingMessage.RESTRICTED_TYPE){
                    Resources res = getResources();
                    String mediaType = res.getString(mediaTypeStringId);
                    MessageUtils.showErrorDialog(ComposeMessageActivity.this, res.getString(R.string.unsupported_media_format, mediaType)
                            , res.getString(R.string.select_different_media, mediaType));
                }
            return true;
            }
        }
        return false;
    }

    private void initMessageSettings() {
        Context otherAppContext = null;
        SharedPreferences sp = null;
        try {
            otherAppContext = this.createPackageContext("com.android.mms", Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            Xlog.e(TAG, "ConversationList NotFoundContext");
        }
        if (otherAppContext != null) {
            sp = otherAppContext.getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        }
        String mSizeLimitTemp = null;
        int mMmsSizeLimit = 0;
        if (sp != null) {
            mSizeLimitTemp = sp.getString("pref_key_mms_size_limit", "300");
        }
        if (0 == mSizeLimitTemp.compareTo("100")) {
            mMmsSizeLimit = 100;
        } else if (0 == mSizeLimitTemp.compareTo("200")) {
            mMmsSizeLimit = 200;
        } else {
            mMmsSizeLimit = 300;
        }
        MmsConfig.setUserSetMmsSizeLimit(mMmsSizeLimit);
    }

//a1
//Li Lian
//a0
    Runnable mGetSimInfoRunnable = new Runnable() {
        public void run() {
        	if (mIsLeMei) {
        		getSimInfoListForLeMei();
        	} else {
                getSimInfoList();
        	}
        }
    };

    //add for multi-delete
    private void changeDeleteMode() {
        if (!mMsgListAdapter.mIsDeleteMode) {
            mMsgListAdapter.clearList();
            markCheckedState(false);
        }
    }

    private void markCheckedState(boolean checkedState) {
        mMsgListAdapter.setItemsValue(checkedState, null);
        if (mSelectedConvCount != null) {
            mSelectedConvCount.setText(Integer.toString(mMsgListAdapter.getSelectedNumber()));
        }
        mDeleteButton.setEnabled(checkedState);
        int count = mMsgListView.getChildCount();
        MessageListItem item = null;
        for (int i = 0; i < count; i++) {
            item = (MessageListItem) mMsgListView.getChildAt(i);
            item.setSelectedBackGroud(checkedState);
        }
    }

    private void showConfirmDialog(Uri uri, boolean append, int type, int messageId) {
        if (isFinishing()) {
            return;
        }

        final Uri mRestrictedMidea = uri;
        final boolean mRestrictedAppend = append;
        final int mRestrictedType = type;
         
        new AlertDialog.Builder(ComposeMessageActivity.this)
        .setTitle(R.string.unsupport_media_type)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setMessage(messageId)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialog, int which) {
                    if (mRestrictedMidea == null || mRestrictedType == WorkingMessage.TEXT
                        || mWorkingMessage.isDiscarded()) {
                        return;
                    }
                    runAsyncWithDialog(new Runnable() {
                        public void run() {
                            int createMode = WorkingMessage.sCreationMode;
                            WorkingMessage.sCreationMode = 0;
                            int result = mWorkingMessage.setAttachment(mRestrictedType, mRestrictedMidea,
                                mRestrictedAppend);
                            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                                log("Restricted Midea: dataUri=" + mRestrictedMidea);
                            }
                            if (mRestrictedType == WorkingMessage.IMAGE
                                && (result == WorkingMessage.IMAGE_TOO_LARGE || result == WorkingMessage.MESSAGE_SIZE_EXCEEDED)) {
                                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                                    log("addImage: resize image " + mRestrictedMidea);
                                }
                                MessageUtils.resizeImage(ComposeMessageActivity.this, mRestrictedMidea, mAttachmentEditorHandler, mResizeImageCallback, mRestrictedAppend,
                                    true);
//                                MessageUtils.resizeImageAsync(ComposeMessageActivity.this, mRestrictedMidea,
//                                    mAttachmentEditorHandler, mResizeImageCallback, mRestrictedAppend);
                                WorkingMessage.sCreationMode = createMode;
                                return;
                            }
                            WorkingMessage.sCreationMode = createMode;
                            int typeId = R.string.type_picture;
                            if (mRestrictedType == WorkingMessage.AUDIO) {
                                typeId = R.string.type_audio;
                            } else if (mRestrictedType == WorkingMessage.VIDEO) {
                                typeId = R.string.type_video;
                            }
                            handleAddAttachmentError(result, typeId);
                        }
                    }, R.string.adding_attachments_title);
            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
    }

    private Uri mCreationUri = null;
    private boolean mCreationAppend = false;

    private void addAudio(Uri uri, boolean append) {
        int result = WorkingMessage.OK;
        try {
            mWorkingMessage.checkSizeBeforeAppend();
        } catch (ExceedMessageSizeException e) {
            result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
            handleAddAttachmentError(result, R.string.type_picture);
            return;
        }
        result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri, append);
        if (result == WorkingMessage.WARNING_TYPE) {
            final Uri mUriTemp = uri;
            final boolean needAppend = append;
            runOnUiThread(new Runnable() {
                public void run() {
                    showConfirmDialog(mUriTemp, needAppend, WorkingMessage.AUDIO, R.string.confirm_restricted_audio);
                }
            });
            return;
        }
        handleAddAttachmentError(result, R.string.type_audio);
    }

    private void addTextVCardAsync(final long[] contactsIds) {
        Xlog.i(TAG, "compose.addTextVCardAsync(): contactsIds.length() = " + contactsIds.length);
        runAsyncWithDialog(new Runnable() {
            public void run() {
                addTextVCard(contactsIds);
            }
        }, R.string.menu_insert_text_vcard);// the string is ok for reuse[or use a new string].
    }
    
    private void addTextVCard(long[] contactsIds) {
        Xlog.i(TAG, "compose.addTextVCard(): contactsIds.length() = " + contactsIds.length);
        String textVCard = TextUtils.isEmpty(mTextEditor.getText())? "": "\n";
        StringBuilder sb = new StringBuilder("");
        for (long contactId : contactsIds) {
            if (contactId == contactsIds[contactsIds.length-1]) {
                sb.append(contactId);
            } else {
                sb.append(contactId + ",");
            }
        }
        String selection = Data.CONTACT_ID + " in (" + sb.toString() + ")";

        Xlog.i(TAG, "compose.addTextVCard(): selection = " + selection);
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor = getContentResolver().query(
            dataUri, // URI
            new String[]{Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1}, // projection
            selection, // selection
            null, // selection args
            RawContacts.SORT_KEY_PRIMARY); // sortOrder
        if (cursor != null) {
            textVCard = getVCardString(cursor, textVCard);
            final String textString = textVCard;
            runOnUiThread(new Runnable() {
                public void run() {
                    insertText(mTextEditor, textString);
                }
            });
            cursor.close();
        }
    }

    private void addFileAttachment(String type, Uri uri, boolean append) {
        
        if (!addFileAttachment(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, type, uri, append)) {
            if (!addFileAttachment(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, type, uri, append)) {
                if (!addFileAttachment(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, type, uri, append)) {
                    Xlog.i(TAG, "This file is not in media store(audio, video or image)," +
                            "attemp to add it like file uri");
                    addAttachment(type, (Uri) uri, append);
                }
            }
        } 
    }

    private boolean addFileAttachment(Uri mediaStoreUri, String type, Uri uri, boolean append) {
        String path = uri.getPath();
        if (path != null) {
            Cursor c = getContentResolver().query(mediaStoreUri, 
                    new String[] {MediaStore.MediaColumns._ID, Audio.Media.MIME_TYPE}, MediaStore.MediaColumns.DATA + "=?",
                    new String[] {path}, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        Uri contentUri = Uri.withAppendedPath(mediaStoreUri, c.getString(0));
                        Xlog.i(TAG, "Get id in MediaStore:" + c.getString(0));
                        Xlog.i(TAG, "Get content type in MediaStore:" + c.getString(1));
                        Xlog.i(TAG, "Get uri in MediaStore:" + contentUri);
                        
                        String contentType = c.getString(1);
                        addAttachment(contentType, contentUri, append);
                        return true;
                    } else {
                        Xlog.i(TAG, "MediaStore:" + mediaStoreUri.toString() + " has not this file");
                    }
                } finally {
                    c.close();
                }
            }
        }
        return false;
    }

    private boolean isHasRecipientCount(){
        int recipientCount = recipientCount();
        return (recipientCount > 0 && recipientCount < RECIPIENTS_LIMIT_FOR_SMS);
    }

    private String getResourcesString(int id) {
        Resources r = getResources();
        return r.getString(id);
    }

    private void checkConditionsAndSendMessage(boolean bCheckEcmMode){
        // check pin
        // convert sim id to slot id
        int requestType = CellConnMgr.REQUEST_TYPE_SIMLOCK;
        final int slotId;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            requestType = CellConnMgr.REQUEST_TYPE_ROAMING;
            slotId = SIMInfo.getSlotById(this, mSelectedSimId);
            Xlog.d(MmsApp.TXN_TAG, "check pin and...: simId=" + mSelectedSimId + "\t slotId=" + slotId);
        } else {
            slotId = 0;
        }
        final boolean bCEM = bCheckEcmMode;
        mCellMgr.handleCellConn(slotId, requestType, new Runnable() {
            public void run() {
                int nRet = mCellMgr.getResult();
                Xlog.d(MmsApp.TXN_TAG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
                if (mCellMgr.RESULT_ABORT == nRet || mCellMgr.RESULT_OK == nRet) {
                    updateSendButtonState();
                    return;
                }
                if (slotId != mCellMgr.getPreferSlot()) {
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(ComposeMessageActivity.this, mCellMgr.getPreferSlot());
                    if (si == null) {
                        Xlog.e(MmsApp.TXN_TAG, "serviceComplete siminfo is null");
                        updateSendButtonState();
                        return;
                    }
                    mSelectedSimId = (int)si.mSimId;
                }
                sendMessage(bCEM);
            }
        });
    }

    private void updateSendButtonState(final boolean enabled) {
        if (!mWorkingMessage.hasSlideshow()) {
            //m0
            //mSendButton.setEnabled(enabled);
            //mSendButton.setFocusable(enabled);
            View sendButton = showSmsOrMmsSendButton(mWorkingMessage.requiresMms());
            sendButton.setEnabled(enabled);
            sendButton.setFocusable(enabled);
            //m1
        } else {
            mAttachmentEditor.setCanSend(enabled);
        }
    }

    private void insertText(EditText edit, String insertText){
        int where = edit.getSelectionStart();

        if (where == -1) {
            edit.append(insertText);
        } else {
            edit.getText().insert(where, insertText);
        }
    }

    /**
     * This filter will constrain edits not to make the length of the text
     * greater than the specified length.
     */  
    class TextLengthFilter implements InputFilter {
        public TextLengthFilter(int max) {
            mMaxLength = max - 1;
            mExceedMessageSizeToast = Toast.makeText(ComposeMessageActivity.this, R.string.exceed_message_size_limitation,
                    Toast.LENGTH_SHORT);
        }

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            
            int keep = mMaxLength - (dest.length() - (dend - dstart));
            
            if (keep < (end - start)) {
                mExceedMessageSizeToast.show();
            }

            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                return source.subSequence(start, start + keep);
            }
        }

        private int mMaxLength;
    }
    
    /**
     * This filter will constrain edits not to make the length of the text
     * greater than the specified length ( eg. 40 Bytes).
     */  
    class MyLengthFilter implements InputFilter {
        public MyLengthFilter(int max) {
            mMax = max;
            mExceedSubjectSizeToast = Toast.makeText(ComposeMessageActivity.this, R.string.exceed_subject_length_limitation,
                    Toast.LENGTH_SHORT);
        }

        private CharSequence getMaxByteSequence(CharSequence str, int keep) {
            String source = str.toString();
            int byteSize = source.getBytes().length;
            if (byteSize <= keep) {
                return str;
            } else {
                int charSize = source.length();
                while (charSize > 0) {
                    source = source.substring(0, source.length()-1);
                    charSize--;
                    if (source.getBytes().length <= keep) {
                        break;
                    }
                }
                return source;
            }
        }
        
        //this is just the method code in LengthFilter, just add a Toast to show max length exceed.
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            int destOldLength = dest.toString().getBytes().length;
            int destReplaceLength = dest.subSequence(dstart, dend).toString().getBytes().length;
            CharSequence sourceSubString = source.subSequence(start, end); 
            int sourceReplaceLength = sourceSubString.toString().getBytes().length;
            int newLength =  destOldLength - destReplaceLength + sourceReplaceLength;
            if (newLength > mMax) {
                // need cut the new input charactors
                mExceedSubjectSizeToast.show();
                int keep = mMax - (destOldLength - destReplaceLength);
                if (keep <= 0) {
                    return ""; 
                } else {
                    return getMaxByteSequence(sourceSubString, keep);
                }
            } else {
                return null; // can replace
            }
        }
        private int mMax;
    }
    
    private void hideInputMethod() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(this.getWindow()!=null && this.getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(this.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

    // toast there are too many recipients.
    private void toastTooManyRecipients(int recipientCount) {
        String tooManyRecipients = getString(R.string.too_many_recipients, recipientCount, RECIPIENTS_LIMIT_FOR_SMS);
        Toast.makeText(ComposeMessageActivity.this, tooManyRecipients, Toast.LENGTH_LONG).show();
    }

    private void addContacts(int pickCount) {
        //m0
        /*Intent intent = new Intent("android.intent.action.CONTACTSMULTICHOICE");
        intent.setType(Phone.CONTENT_ITEM_TYPE);
        intent.putExtra("request_email", true);

        intent.putExtra("pick_count", pickCount);
        misPickContatct = true;
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);*/
        try {
            misPickContatct = true;
            Intent intent = new Intent("android.intent.action.contacts.list.PICKMULTIPHONEANDEMAILS");
            intent.setType(Phone.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } catch (ActivityNotFoundException e) {
            misPickContatct = false;
            Toast.makeText(this, this.getString(R.string.no_application_response), Toast.LENGTH_SHORT).show();
            Xlog.e(TAG, e.getMessage());
        }
        
        //m1
    }

    private class TextVCardContact {
        protected String name = "";
        protected List<String> numbers = new ArrayList<String>();
        protected List<String> emails = new ArrayList<String>();
        protected List<String> organizations = new ArrayList<String>();

        protected void reset() {
            name = "";
            numbers.clear();
            emails.clear();
            organizations.clear();
        }
        @Override
        public String toString() {
            String textVCardString = "";
            int i = 1;
            if (name != null && !name.equals("")) {
                textVCardString += getString(R.string.contact_name) + ": " + name + "\n";
            }
            if (!numbers.isEmpty()) {
                if (numbers.size() > 1) {
                    i = 1;
                    for (String number : numbers) {
                        textVCardString += getString(R.string.contact_tel) + i + ": " + number + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.contact_tel) + ": " + numbers.get(0) + "\n";
                }
            }
            if (!emails.isEmpty()) {
                if (emails.size() > 1) {
                    i = 1;
                    for (String email : emails) {
                        textVCardString += getString(R.string.contact_email) + i + ": " + email + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.contact_email) + ": " + emails.get(0) + "\n";
                }
            }
            if (!organizations.isEmpty()) {
                if (organizations.size() > 1) {
                    i = 1;
                    for (String organization : organizations) {
                        textVCardString += getString(R.string.contact_organization) + i + ": " + organization + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.contact_organization) + ": " + organizations.get(0) + "\n";
                }
            }
            return textVCardString;
        }
    }

    // create the String of vCard via Contacts message
    private String getVCardString(Cursor cursor, String textVCard) {
        final int dataContactId     = 0;
        final int dataMimeType      = 1;
        final int dataString        = 2;
        long contactId = 0l;
        long contactCurrentId = 0l;
        int i = 1;
        String mimeType;
        TextVCardContact tvc = new TextVCardContact();
        int j = 0;
        while (cursor.moveToNext()) {
            contactId = cursor.getLong(dataContactId);
            mimeType = cursor.getString(dataMimeType);
            if (contactCurrentId == 0l) {
                contactCurrentId = contactId;
            }

            // put one contact information into textVCard string
            if (contactId != contactCurrentId) {
                contactCurrentId = contactId;
                textVCard += tvc.toString();
                tvc.reset();
            }

            // get cursor data
            if (CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.name = cursor.getString(dataString);
            }
            if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.numbers.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.emails.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.organizations.add(cursor.getString(dataString));
            }
            // put the last one contact information into textVCard string
            if (cursor.isLast()) {
                textVCard += tvc.toString();
            }
            j++;
            if (j%10 == 0) {
                if (textVCard.length() > MmsConfig.getMaxTextLimit()) {
                    break;
                }
            }
        }
        Xlog.i(TAG, "compose.getVCardString():return string = " + textVCard);
        return textVCard;
    }

    private int getContactSIM(final String num) {
        class Int {
            private int value = -1;
            public void  set(int n) {
                value = n;
            }
            public int get() {
                return value;
            }
        }
        final Int simID = new Int();
        final Object dbQueryLock = new Object();
        Object waitLock = new Object();
        final Context mContextTemp = this.getApplicationContext();
        // query the db in another thread.
        new Thread(new Runnable() {
            public void run() {
                int simId = -1;
                String number = num;                
                String formatNumber = MessageUtils.formatNumber(number,mContextTemp);
                Cursor associateSIMCursor = ComposeMessageActivity.this.getContentResolver().query(
                    Data.CONTENT_URI, 
                    new String[]{Data.SIM_ASSOCIATION_ID}, 
                    Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
                    + "' AND (" + Data.DATA1 + "=?" +  
                    " OR " + Data.DATA1 +"=?" + 
                    ") AND (" + Data.SIM_ASSOCIATION_ID + "!= -1)", 
                    new String[]{number,formatNumber},
                    null
                );

                if ((null != associateSIMCursor) && (associateSIMCursor.getCount() > 0)) {
                    associateSIMCursor.moveToFirst();
                    // Get only one record is OK
                    simId = (Integer) associateSIMCursor.getInt(0);
                } else {
                    simId = -1;
                }
                associateSIMCursor.close();
                synchronized (dbQueryLock) {
                    simID.set(simId);
                    dbQueryLock.notify();
                }
            }
        }).start();
        // UI thread wait 50ms at most.
        synchronized (dbQueryLock) {
            try {
                dbQueryLock.wait(200);
            } catch(InterruptedException e) {
                //time out
            }
            return simID.get();
        }
    }
    
    private void getSimInfoList() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//            mSimInfoList = SIMInfo.getInsertedSIMList(this);
            mSimInfoList = new ArrayList<SIMInfo>();
            SIMInfo sim1Info = SIMInfo.getSIMInfoBySlot(this, com.android.internal.telephony.Phone.GEMINI_SIM_1);
            SIMInfo sim2Info = SIMInfo.getSIMInfoBySlot(this, com.android.internal.telephony.Phone.GEMINI_SIM_2);
            if (sim1Info != null) {
                mSimInfoList.add(sim1Info);
            }
            if (sim2Info != null) {
                mSimInfoList.add(sim2Info);
            }
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
            Xlog.v(TAG, "ComposeMessageActivity.getSimInfoList(): mSimCount = " + mSimCount);
        } else { // single SIM
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                try {
                    mSimCount = phone.isSimInsert(0) ? 1: 0;
                } catch (RemoteException e) {
                    Xlog.e(MmsApp.TXN_TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }
    }

    private void checkRecipientsCount() {
//        if (isRecipientsEditorVisible()) {
//            mRecipientsEditor.structLastRecipient();
//        }
        hideInputMethod();
        final int mmsLimitCount = MmsConfig.getMmsRecipientLimit();
        if (mWorkingMessage.requiresMms() && recipientCount() > mmsLimitCount) {
            String message = getString(R.string.max_recipients_message, mmsLimitCount);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.max_recipients_title);
            builder.setIconAttribute(android.R.attr.alertDialogIcon);
            builder.setCancelable(true);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            /*
                             * If entering an existing thread, #mRecipientsEditor never gets initialized.
                             * So, when mRecipientsEditor is not visible, it might be null.
                             */
                            List<String> recipientsList;
                            if (isRecipientsEditorVisible()) {
                                recipientsList = mRecipientsEditor.getNumbers();
                            } else {
                                recipientsList = new ArrayList<String>(Arrays.asList(getRecipients().getNumbers()));
                            }
                            List<String> newRecipientsList = new ArrayList<String>();

                            if (recipientCount() > mmsLimitCount * 2) {
                                for (int i = 0; i < mmsLimitCount; i++) {
                                    newRecipientsList.add(recipientsList.get(i));
                                }
                                mWorkingMessage.setWorkingRecipients(newRecipientsList);
                            } else {
                                for (int i = recipientCount() - 1; i >= mmsLimitCount; i--) {
                                    recipientsList.remove(i);
                                }
                                mWorkingMessage.setWorkingRecipients(recipientsList);
                            }
                            simSelection();
                        }
                    });
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
            updateSendButtonState();
        } else {
            /*
             * fix CR ALPS00069541
             * if the message copy from sim card with unknown recipient
             * the recipient will be ""
             */
            if (isRecipientsEditorVisible() && "".equals(mRecipientsEditor.getText()
//                    .allNumberToString().replaceAll(";", "")
                    )) {
                new AlertDialog.Builder(this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.cannot_send_message)
                        .setMessage(R.string.cannot_send_message_reason)
                        .setPositiveButton(R.string.yes, new CancelSendingListener())
                        .show();
            } else if (!isRecipientsEditorVisible() && "".equals(mConversation.getRecipients().serialize().replaceAll(";", ""))) {
                new AlertDialog.Builder(this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.cannot_send_message)
                        .setMessage(R.string.cannot_send_message_reason)
                        .setPositiveButton(R.string.yes, new CancelSendingListener())
                        .show();
            } else {
//MTK_OP01_PROTECT_START
                if (FeatureOption.MTK_GEMINI_SUPPORT && "OP01".equals(SystemProperties.get("ro.operator.optr"))) {
                    if ((MmsConfig.getMmsDirMode() == false) && !isRecipientsEditorVisible()) {
                        send_sim_id = checkConversationSingleCardRelated();
                        Xlog.d(TAG, "send_sim_id="+send_sim_id);
                    }
                }
//MTK_OP01_PROTECT_END
                simSelection();
            }
        }
    }

//MTK_OP01_PROTECT_START
    private int checkConversationSingleCardRelated() {
        Uri uri = Uri.parse("content://mms-sms/simid_list/"+mConversation.getThreadId());
        Cursor cursor = SqliteWrapper.query(this, mContentResolver, uri, null, null, null, null);
        int simId = -1;
        if (cursor != null) {
            try {
                int cnt = cursor.getCount();
                Xlog.d(TAG, "cnt=" + cnt);
                if (cnt == 1) {
                    cursor.moveToFirst();
                    simId = cursor.getInt(0);
                } else if (cnt == 2) {
                    // a mms draft may have simid -1, so at most 2 maybe return.
                    cursor.moveToFirst();
                    int simId1 = cursor.getInt(0);
                    cursor.moveToNext();
                    int simId2 = cursor.getInt(0);
                    if (simId1 == -1) {
                        simId = simId2;
                    } else if (simId2 == -1) {
                        simId = simId1;
                    } else {
                        return -1;
                    }
                }
            } catch (SQLiteException e) {
                // no need to log, there is in SliteWrapper.
            } finally {
                cursor.close();
            }
        }
        // the card maybe not in the phone, must check it
        boolean isValid = false;
        for (int i =0; i< mSimCount; i++ ) {
            if (mSimInfoList.get(i).mSimId == simId) {
                isValid = true;
                break;
            }
        }
        Xlog.d(TAG, "valid:"+isValid);
        if (isValid) {
            return simId;
        } else {
            return -1;
        }
    }
//MTK_OP01_PROTECT_END

    private void simSelection() {
        if (FeatureOption.MTK_GEMINI_SUPPORT == false) {
            confirmSendMessageIfNeeded();
        } else if (mSimCount == 0) {
            // SendButton can't click in this case
        //a0
        } else if (send_sim_id >= 1) {
            mSelectedSimId = send_sim_id;
            send_sim_id = -1;
            Xlog.d(TAG, "send msg from send_sim_id = " + mSelectedSimId);
            confirmSendMessageIfNeeded();
        //a1
        } else if (mSimCount == 1) {
            mSelectedSimId = (int) mSimInfoList.get(0).mSimId;
            confirmSendMessageIfNeeded();
        } else if (mSimCount > 1) {
            Intent intent = new Intent();
            intent.putExtra(SELECT_TYPE, SIM_SELECT_FOR_SEND_MSG);
            // getContactSIM
            if (isRecipientsEditorVisible()) {
                if (mRecipientsEditor.getRecipientCount() == 1/*isOnlyOneRecipient()*/) {
                    mAssociatedSimId = getContactSIM(mRecipientsEditor.getNumbers().get(0)); // 152188888888 is a contact number
                } else {
                    mAssociatedSimId = -1;
                }
            } else {
                if (getRecipients().size() == 1/*isOnlyOneRecipient()*/) {
                    mAssociatedSimId = getContactSIM(getRecipients().get(0).getNumber()); // 152188888888 is a contact number
                } else {
                    mAssociatedSimId = -1;
                }
            }
            Xlog.d(TAG, "mAssociatedSimId = " + mAssociatedSimId);
            // getDefaultSIM()
            mMessageSimId = Settings.System.getLong(getContentResolver(), Settings.System.SMS_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
            if (mMessageSimId == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                // always ask, show SIM selection dialog
                showSimSelectedDialog(intent);
                updateSendButtonState();
            } else if (mMessageSimId == Settings.System.DEFAULT_SIM_NOT_SET) {
                /*
                 * not set default SIM: 
                 * if recipients are morn than 2,or there is no associated SIM,
                 * show SIM selection dialog
                 * else send message via associated SIM
                 */
                if (mAssociatedSimId == -1) {
                    showSimSelectedDialog(intent);
                    updateSendButtonState();
                } else {
                    mSelectedSimId = mAssociatedSimId;
                    confirmSendMessageIfNeeded();
                }
            } else {
                /*
                 * default SIM:
                 * if recipients are morn than 2,or there is no associated SIM,
                 * send message via default SIM
                 * else show SIM selection dialog
                 */
                if (mAssociatedSimId == -1 || (mMessageSimId == mAssociatedSimId)) {
                    mSelectedSimId = (int) mMessageSimId;
                    confirmSendMessageIfNeeded();
                } else {
                    showSimSelectedDialog(intent);
                    updateSendButtonState();
                }
            }
        }
    }
    
    private void showSimSelectedDialog(Intent intent) {
        // TODO get default SIM and get contact SIM
        mLastButtonClickTime = -65535;
        final Intent it = intent;
        List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
        for (int i = 0; i < mSimCount; i++) {
            SIMInfo simInfo = mSimInfoList.get(i);
            HashMap<String, Object> entry = new HashMap<String, Object>();

            entry.put("simIcon", simInfo.mSimBackgroundRes);
            int state = MessageUtils.getSimStatus(i, mSimInfoList, TelephonyManagerEx.getDefault());
            entry.put("simStatus", MessageUtils.getSimStatusResource(state));
            String simNumber = "";
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                switch(simInfo.mDispalyNumberFormat) {
                    //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_FIRST:
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(0, 4);
                        break;
                    case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_LAST:
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(simInfo.mNumber.length() - 4);
                        break;
                    case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        simNumber = "";
                        break;
                }
            }
            if (!TextUtils.isEmpty(simNumber)) {
                entry.put("simNumberShort",simNumber);
            } else {
                entry.put("simNumberShort", "");
            }

            entry.put("simName", simInfo.mDisplayName);
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                entry.put("simNumber", simInfo.mNumber);
            } else {
                entry.put("simNumber", "");
            }
            if (mAssociatedSimId == (int) simInfo.mSimId
                && it.getIntExtra(SELECT_TYPE, -1) != SIM_SELECT_FOR_SAVE_MSG_TO_SIM) {
                // if this SIM is contact SIM, set "Suggested"
                entry.put("suggested", getString(R.string.suggested));
            } else {
                entry.put("suggested", "");// not suggested
            }
//            if (!MessageUtils.is3G(i, mSimInfoList)) {
                entry.put("sim3g", "");
//            } else {
//                String optr = SystemProperties.get("ro.operator.optr");
//                //MTK_OP02_PROTECT_START
//                if (optr != null && optr.equals("OP02")) {
//                    entry.put("sim3g", "3G");
//                } else 
//                //MTK_OP02_PROTECT_END
//                {
//                    entry.put("sim3g", "");
//                }
//            }
            entries.add(entry);
        }

        final SimpleAdapter a = new SimpleAdapter(
                this,
                entries,
                R.layout.sim_selector,
                new String[] {"simIcon", "simStatus", "simNumberShort", "simName", "simNumber", "suggested", "sim3g"},
                new int[] {R.id.sim_icon, R.id.sim_status, R.id.sim_number_short, 
                        R.id.sim_name, R.id.sim_number, R.id.sim_suggested, R.id.sim3g});
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    if (view.getId() == R.id.sim_icon) {
                        ImageView simicon = (ImageView) view.findViewById(R.id.sim_icon);
                        simicon.setBackgroundResource((Integer) data);
                    } else if (view.getId() == R.id.sim_status) {
                        ImageView simstatus = (ImageView)view.findViewById(R.id.sim_status);
                        if ((Integer)data != com.android.internal.telephony.Phone.SIM_INDICATOR_UNKNOWN
                                && (Integer)data != com.android.internal.telephony.Phone.SIM_INDICATOR_NORMAL) {
                            simstatus.setVisibility(View.VISIBLE);
                            simstatus.setImageResource((Integer)data);
                        } else {
                            simstatus.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        a.setViewBinder(viewBinder);
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.sim_selected_dialog_title));
        b.setCancelable(true);
        b.setAdapter(a, new DialogInterface.OnClickListener() {
            @SuppressWarnings("unchecked")
            public final void onClick(DialogInterface dialog, int which) {
                updateSendButtonState(false);
                mSelectedSimId = (int) mSimInfoList.get(which).mSimId;
                if (it.getIntExtra(SELECT_TYPE, -1) == SIM_SELECT_FOR_SEND_MSG) {
                    confirmSendMessageIfNeeded();
                } else if (it.getIntExtra(SELECT_TYPE, -1) == SIM_SELECT_FOR_SAVE_MSG_TO_SIM) {
                    //getMessageAndSaveToSim(it);
                    Message msg = mSaveMsgHandler.obtainMessage(MSG_SAVE_MESSAGE_TO_SIM_AFTER_SELECT_SIM);
                    msg.obj = it;
                    //mSaveMsgHandler.sendMessageDelayed(msg, 60);
                    mSaveMsgHandler.sendMessage(msg);
                }
                dialog.dismiss();
            }
        });
        mSIMSelectDialog = b.create();
        mSIMSelectDialog.show();
    }
    
    //add for multi-delete  
    private void confirmMultiDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        //builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_selected_messages);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mBackgroundQueryHandler.setProgressDialog(DeleteProgressDialogUtil.getProgressDialog(ComposeMessageActivity.this));
                mBackgroundQueryHandler.showProgressDialog();
                new Thread(new Runnable() {
                    public void run() {
                        Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
                        Uri deleteSmsUri = null;
                        Uri deleteMmsUri = null;
                        String[] argsSms = new String[mMsgListAdapter.getSelectedNumber()];
                        String[] argsMms = new String[mMsgListAdapter.getSelectedNumber()];
                        int i = 0;
                        int j = 0;
                        while (iter.hasNext()) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
                            if (entry.getValue()) {
                                if (entry.getKey() > 0){
                                    Xlog.i(TAG, "sms");
                                    argsSms[i] = Long.toString(entry.getKey());
                                    Xlog.i(TAG, "argsSms[i]" + argsSms[i]);
                                    //deleteSmsUri = ContentUris.withAppendedId(Sms.CONTENT_URI, entry.getKey());
                                    deleteSmsUri = Sms.CONTENT_URI;
                                    i++;
                                } else {
                                    Xlog.i(TAG, "mms");
                                    argsMms[j] = Long.toString(-entry.getKey());
                                    Xlog.i(TAG, "argsMms[j]" + argsMms[j]);
                                    //deleteMmsUri = ContentUris.withAppendedId(Mms.CONTENT_URI, -entry.getKey());
                                    deleteMmsUri = Mms.CONTENT_URI;
                                    j++;
                                }
                            }
                        }
                        mBackgroundQueryHandler.setMax(
                                (deleteSmsUri != null ? 1 : 0) +
                                (deleteMmsUri != null ? 1 : 0));
                        if (deleteSmsUri != null) {
                            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                    null, deleteSmsUri, FOR_MULTIDELETE, argsSms);
                        }
                        if (deleteMmsUri != null) {
                            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                                    null, deleteMmsUri, FOR_MULTIDELETE, argsMms);
                        }
                        mMsgListAdapter.mIsDeleteMode = false;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                drawTopPanel(false);
                                drawBottomPanel();
                            }
                        });
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
//a1

    public static Activity getComposeContext() {
        return sCompose;
    }

    @Override
    public void onShutDown() {
        saveDraft(false);
    }

    /*
    this function is add for read report
    */
    private final int READ_REPORT_DISABLED                      = 0;
    private final int READ_REPORT_SINGLE_MODE_ENABLED           = 1;
    private final int READ_REPORT_GEMINI_MODE_ENABLED_SLOT_0    = 2;
    private final int READ_REPORT_GEMINI_MODE_ENABLED_SLOT_1    = 4;
    private final int READ_REPORT_GEMINI_MODE_ENABLED_BOTH      = READ_REPORT_GEMINI_MODE_ENABLED_SLOT_0|READ_REPORT_GEMINI_MODE_ENABLED_SLOT_1;
    
    private void checkAndSendReadReport() {
        final Context ct = ComposeMessageActivity.this;
        final long threadId = mConversation.getThreadId();        
        Xlog.d(MmsApp.TXN_TAG,"checkAndSendReadReport,threadId:"+threadId);
        new Thread(new Runnable() {
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ComposeMessageActivity.this);
                int rrAllowed = READ_REPORT_DISABLED;
                long simId1 = -1;
                long simId2 = -1;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(ComposeMessageActivity.this, 0);                    
                    if (si != null) {
                        if (prefs.getBoolean(Long.toString(si.mSimId)+ "_" + MessagingPreferenceActivity.READ_REPORT_AUTO_REPLY, false) == true) {
                            rrAllowed = READ_REPORT_GEMINI_MODE_ENABLED_SLOT_0;
                        }
                        simId1 = si.mSimId;
                        Xlog.d(MmsApp.TXN_TAG,"slot0 simId is:"+si.mSimId);
                    }
                    si = null;
                    si = SIMInfo.getSIMInfoBySlot(ComposeMessageActivity.this, 1);
                    if (si != null) {
                        if (prefs.getBoolean(Long.toString(si.mSimId)+ "_" + MessagingPreferenceActivity.READ_REPORT_AUTO_REPLY, false) == true) {
                            rrAllowed += READ_REPORT_GEMINI_MODE_ENABLED_SLOT_1;
                        }
                        simId2 = si.mSimId;
                        Xlog.d(MmsApp.TXN_TAG,"slot1 simId is:"+si.mSimId);
                    }
                } else {
                    if (prefs.getBoolean(MessagingPreferenceActivity.READ_REPORT_AUTO_REPLY, false) == true) {
                        rrAllowed = READ_REPORT_SINGLE_MODE_ENABLED;
                    }
                }
                Xlog.d(MmsApp.TXN_TAG,"rrAllowed="+rrAllowed);
                // if read report is off, mark the mms read report status readed.
                if (rrAllowed == READ_REPORT_DISABLED) {
                    ContentValues values = new ContentValues(1);
                    String where = Mms.THREAD_ID + " = " + threadId + " and " + Mms.READ_REPORT + " = 128";
                    // update uri inbox is not used, must indicate here.
                    where += " and " + Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_INBOX;
                    values.put(Mms.READ_REPORT, PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ);
                    SqliteWrapper.update(ct, ct.getContentResolver(), Mms.Inbox.CONTENT_URI,
                                        values,
                                        where,
                                        null);
                    return;
                }
                if (rrAllowed > READ_REPORT_DISABLED) {
                    String suffix = "";
                    switch (rrAllowed) {
                    case READ_REPORT_SINGLE_MODE_ENABLED:
                        //nothing to do in single card mode
                        break;
                    case READ_REPORT_GEMINI_MODE_ENABLED_SLOT_0:
                        //slot 0 has card and read report on
                        suffix = " and " + Mms.SIM_ID + " = " + simId1;
                        //mark slot 1 card readed, because it off read report.
                        if (simId2 != -1) {
                            Xlog.d(MmsApp.TXN_TAG, "mark slot 1 card readed");
                            markReadReportProcessed(ct, threadId, simId2);
                        }
                        break;
                    case READ_REPORT_GEMINI_MODE_ENABLED_SLOT_1:
                        //slot 1 has card and read report on
                        suffix = " and " + Mms.SIM_ID + " = " + simId2;
                        //mark slot 0 card readed, because it off read report.
                        if (simId1 != -1) {
                            Xlog.d(MmsApp.TXN_TAG,"mark slot 0 card readed");
                            markReadReportProcessed(ct, threadId, simId1);
                        }
                        break;
                    case READ_REPORT_GEMINI_MODE_ENABLED_BOTH:
                        //both slot has card and on.
                        suffix = " and (" + Mms.SIM_ID + " = " + simId2 + " or " + Mms.SIM_ID + " = " + simId1 +")"; 
                        break;
                    default:
                        Xlog.e(MmsApp.TXN_TAG,"impossible value for rrAllowed.");
                        break;
                    }
                    boolean networkOk = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))
                                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).isAvailable();
                    int airplaneMode = Settings.System.getInt(ct.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);                    
                    //network not ok.next time will try.
                    if ((networkOk != true)||(airplaneMode == 1)) {
                        Xlog.d(MmsApp.TXN_TAG, "networkok:"+networkOk+",airplaneMode:"+airplaneMode);
                        return;
                    }
                    Cursor cs = null;
                    try {
                        String where = Mms.THREAD_ID + " = " + threadId + " and " + Mms.READ_REPORT + " = 128" + suffix;
                        cs = SqliteWrapper.query(ct, ct.getContentResolver(),Mms.Inbox.CONTENT_URI,
                                                new String[]{Mms._ID, Mms.SIM_ID},
                                                where,
                                                null, null);
                        if (cs != null) {
                            final int count = cs.getCount();
                            if (count > 0) {
                                //mark the ones need send read report status to pending as 130.
                                ContentValues values = new ContentValues(1);
                                values.put(Mms.READ_REPORT, 130);
                                // update uri inbox is not used, must indicate here.
                                where += " and " + Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_INBOX;
                                SqliteWrapper.update(ct, ct.getContentResolver(), Mms.Inbox.CONTENT_URI,
                                                    values,
                                                    where,
                                                    null);
                                //show a toast.
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(ComposeMessageActivity.this,
                                                    ct.getResources().getQuantityString(R.plurals.read_report_toast_msg, count, count),
                                                    Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            startSendReadReport(cs);
                        }
                    } catch (Exception e) {
                        Xlog.e(MmsApp.TXN_TAG,"exception happend when scan read report!:"+e.getMessage());
                    } finally {
                        if (cs != null) {
                            cs.close();
                        }
                    }
                }
            }

            private void markReadReportProcessed(Context ct, long threadId, long simId) {
                ContentValues values = new ContentValues(1);
                values.put(Mms.READ_REPORT, PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ);
                String where = Mms.THREAD_ID + " = " + threadId + " and " + Mms.READ_REPORT + " = 128"
                                    + " and " + Mms.SIM_ID + " = " + simId;
                // update uri inbox is not used, must indicate here.
                where += " and " + Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_INBOX;                
                SqliteWrapper.update(ct, ct.getContentResolver(), Mms.Inbox.CONTENT_URI,
                                    values,
                                    where,
                                    null);
            }
            
            private void startSendReadReport(final Cursor cursor) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    Xlog.d(MmsApp.TXN_TAG,"send an intent for read report.");
                    long msgId = cursor.getLong(0);
                    Intent rrIntent = new Intent(ct, TransactionService.class);
                    rrIntent.putExtra("uri",Mms.Inbox.CONTENT_URI+"/"+msgId);//the uri of mms that need send rr
                    rrIntent.putExtra("type",Transaction.READREC_TRANSACTION);
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        int simId = cursor.getInt(1);
                        Xlog.d(MmsApp.TXN_TAG,"simId:"+simId);
                        rrIntent.putExtra("simId", simId);
                    }
                    ct.startService(rrIntent);
                }
            }
        }).start();
    }
    
    /**
     * Remove the number which is the same as any one before;
     * When the count of recipients over the limit, make a toast and remove the recipients over the limit.
     * @param recipientsString the numbers slipt by ','.
     * @return recipientsString the numbers slipt by ',' after modified.
     */
    private String getStringForMultipleRecipients(String recipientsString) {
        recipientsString = recipientsString.replaceAll(",", ";");
        String[] recipients_all = recipientsString.split(";");
        List<String> recipientsList = new ArrayList<String>();
        for (String recipient : recipients_all) {
            recipientsList.add(recipient);
        }

        Set<String> recipientsSet = new HashSet<String>();
        recipientsSet.addAll(recipientsList);

        if (recipientsSet.size() > RECIPIENTS_LIMIT_FOR_SMS) {
            toastTooManyRecipients(recipients_all.length);
        }

        recipientsList.clear();
        recipientsList.addAll(recipientsSet);

        recipientsString = "";
        int count = recipientsList.size() > RECIPIENTS_LIMIT_FOR_SMS ? RECIPIENTS_LIMIT_FOR_SMS : recipientsList.size();
        for(int i = 0; i < count; i++) {
            if (i == (count - 1)) {
                recipientsString += recipientsList.get(i);
            } else {
                recipientsString += recipientsList.get(i) + ";";
            }
        }
        return recipientsString;
    }
    
    private void addLeMeiImage(Uri uri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("addImage: append=" + append + ", uri=" + uri);
        }
        // For CU spec, the max image size is 100K.
        MmsConfig.setUserSetMmsSizeLimit(IMAGE_SIZE_MINI);
        int result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, uri,
                append);

        if (result == WorkingMessage.IMAGE_TOO_LARGE
                || result == WorkingMessage.MESSAGE_SIZE_EXCEEDED) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("addImage: resize image " + uri);
            }
            MessageUtils.resizeImageAsync(this, uri, mAttachmentEditorHandler,
                    mResizeImageCallback, append);
            return;
        } else if (result == WorkingMessage.WARNING_TYPE) {
            mCreationUri = uri;
            mCreationAppend = append;
            runOnUiThread(new Runnable() {
                public void run() {
                    showConfirmDialog(mCreationUri, mCreationAppend,
                            WorkingMessage.IMAGE,
                            R.string.confirm_restricted_image);
                }
            });
            return;
        }
        handleAddAttachmentError(result, R.string.type_picture);
    }

    private void addLeMeiVideo(Uri uri, boolean append) {
        if (uri != null) {
             // For CU spec, the video size is 300K.
            MmsConfig.setUserSetMmsSizeLimit(AUDIO_VIDEO_SIZE_MINI);
            int result = mWorkingMessage.setAttachment(WorkingMessage.VIDEO,
                    uri, append);
            if (result == WorkingMessage.UNSUPPORTED_TYPE) {
                addLeMeiImage(uri, append);
            } else if (result == WorkingMessage.WARNING_TYPE) {
                showConfirmDialog(uri, append, WorkingMessage.VIDEO,
                        R.string.confirm_restricted_video);
            } else {
                handleAddAttachmentError(result, R.string.type_video);
            }
        }
    }

    private void addLeMeiAudio(Uri uri, boolean append) {
        Xlog.i(TAG, "Enter addAudio");
        MmsConfig.setUserSetMmsSizeLimit(AUDIO_VIDEO_SIZE_MINI);
        int result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri,
                append);
        if (result == WorkingMessage.WARNING_TYPE) {
            showConfirmDialog(uri, false, WorkingMessage.AUDIO,
                    R.string.confirm_restricted_audio);
            return;
        }
        handleAddAttachmentError(result, R.string.type_audio);
    }

    private void addLeMeiAudio(Uri uri) {
        MmsConfig.setUserSetMmsSizeLimit(AUDIO_VIDEO_SIZE_MINI);
        int result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri,
                false);
        if (result == WorkingMessage.WARNING_TYPE) {
            showConfirmDialog(uri, false, WorkingMessage.AUDIO,
                    R.string.confirm_restricted_audio);
            return;
        }
        handleAddAttachmentError(result, R.string.type_audio);
    }
    
    private void addLeMeiAttachment(String type, Uri uri, boolean append) {
        if (uri != null) {
            // When we're handling Intent.ACTION_SEND_MULTIPLE, the passed in items can be
            // videos, and/or images, and/or some other unknown types we don't handle. When
            // a single attachment is "shared" the type will specify an image or video. When
            // there are multiple types, the type passed in is "*/*". In that case, we've got
            // to look at the uri to figure out if it is an image or video.
            boolean wildcard = "*/*".equals(type);
            Xlog.i(TAG, "Got send intent mimeType :" + type);
            if (type.startsWith("image/") || 
            		(wildcard && uri.toString().startsWith(mImageUri))) {
                addLeMeiImage(uri, append);
            } else if (type.startsWith("video/") ||
                    (wildcard && uri.toString().startsWith(mVideoUri))) {
                addLeMeiVideo(uri, append);
            } else if (type.startsWith("audio/") || 
					(wildcard && uri.toString().startsWith(mAudioUri))) {
                addLeMeiAudio(uri, append);
            }
        }
    }
    
    private void getSimInfoListForLeMei() {
        // mSimInfoList = SIMInfo.getInsertedSIMList(this);
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mSimInfoList = new ArrayList<SIMInfo>();
            if (mSimDis == 0) {
                // add both two card.
                SIMInfo sim1Info = SIMInfo.getSIMInfoBySlot(this,
                        com.android.internal.telephony.Phone.GEMINI_SIM_1);
                SIMInfo sim2Info = SIMInfo.getSIMInfoBySlot(this,
                        com.android.internal.telephony.Phone.GEMINI_SIM_2);
                if (sim2Info != null && sim1Info != null) {
                    mSimInfoList.add(sim1Info);
                    mSimInfoList.add(sim2Info);
                }
            } else if (mSimDis == 1) {
                // add sim2
                SIMInfo sim2Info = SIMInfo.getSIMInfoBySlot(this,
                        com.android.internal.telephony.Phone.GEMINI_SIM_2);
                if (sim2Info != null) {
                    Xlog.i(TAG, "sim2Info is null");
                    mSimInfoList.add(sim2Info);
                }
            } else if (mSimDis == 2) {
                // add sim1;
                SIMInfo sim1Info = SIMInfo.getSIMInfoBySlot(this,
                        com.android.internal.telephony.Phone.GEMINI_SIM_1);
                if (sim1Info != null) {
                    Xlog.i(TAG, "sim1Info is null");
                    mSimInfoList.add(sim1Info);
                }
            } else {
                mSimInfoList = SIMInfo.getInsertedSIMList(this);
            }
            mSimCount = mSimInfoList.isEmpty() ? 0 : mSimInfoList.size();
        } else {
            mSimCount = 1;
        }
    }

    private class DeleteCallback implements ActionMode.Callback {
        private View mMultiSelectActionBarView;
        
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = (ViewGroup)LayoutInflater.from(ComposeMessageActivity.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);

                mSelectedConvCount =
                    (TextView)mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
                mSelectedConvCount.setText("0");
            }
            mode.setCustomView(mMultiSelectActionBarView);
            ((TextView)mMultiSelectActionBarView.findViewById(R.id.title))
                .setText(R.string.select_conversations);
            return true;
        }
        
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mMultiSelectActionBarView == null) {
                ViewGroup v = (ViewGroup)LayoutInflater.from(ComposeMessageActivity.this)
                    .inflate(R.layout.conversation_list_multi_select_actionbar, null);
                mode.setCustomView(v);

                mSelectedConvCount = (TextView)v.findViewById(R.id.selected_conv_count);
                mSelectedConvCount.setText("0");
            }
            return true;
        }
        
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }
        
        public void onDestroyActionMode(ActionMode mode) {
            mDeleteActionMode = null;
            mIsSelectedAll = false;
            mMsgListAdapter.mIsDeleteMode = false;
            drawTopPanel(true);
            drawBottomPanel();
            startMsgListQuery();
        }
    };

    public Conversation getConversation() {
    	return mConversation;
    }
    
    private void showQuickTextDialog() {
        mQuickTextDialog = null;
        //if (mQuickTextDialog == null) {
            List<String> quickTextsList = new ArrayList<String>();

            // add user's quick text
            Cursor cursor = getContentResolver().query(MmsSms.CONTENT_URI_QUICKTEXT, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    quickTextsList.add(cursor.getString(1));
                }
                cursor.close();
            }

            // add default quick text
            String[] default_quick_texts = getResources().getStringArray(R.array.default_quick_texts);
            for (int i = 0; i < default_quick_texts.length; i++) {
                quickTextsList.add(default_quick_texts[i]);
            }

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (String text : quickTextsList) {
                HashMap<String, Object> entry = new HashMap<String, Object>();
                entry.put("text", text);
                entries.add(entry);
            }

            final SimpleAdapter qtAdapter = new SimpleAdapter( this, entries, R.layout.quick_text_list_item, 
                    new String[] {"text"}, new int[] {R.id.quick_text});
            
            AlertDialog.Builder qtBuilder = new AlertDialog.Builder(this);

            qtBuilder.setTitle(getString(R.string.select_quick_text));
            qtBuilder.setCancelable(true);
            qtBuilder.setAdapter(qtAdapter, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) qtAdapter.getItem(which);
                    if (mSubjectTextEditor != null && mSubjectTextEditor.isFocused()){
                        insertText(mSubjectTextEditor, (String)item.get("text"));
                    } else {
                        insertText(mTextEditor, (String)item.get("text"));
                    }
                    dialog.dismiss();
                }
            });
            mQuickTextDialog = qtBuilder.create();
        //}
        mQuickTextDialog.show();
    }

//MTK_OP01_PROTECT_START
    private boolean mIsShowSIMIndicator = true;
    @Override
    public void OnSimInforChanged() {
        Xlog.i(MmsApp.LOG_TAG, "OnSimInforChanged(): Composer");
        // show SMS indicator
        if (!isFinishing() && mIsShowSIMIndicator) {
            Xlog.i(MmsApp.LOG_TAG, "Hide current indicator and show new one.");
            mStatusBarManager.hideSIMIndicator(mComponentName);
            mStatusBarManager.showSIMIndicator(mComponentName, Settings.System.SMS_SIM_SETTING);
        }
    }
//MTK_OP01_PROTECT_END

    private final HeightChangedLinearLayout.LayoutSizeChangedListener mLayoutSizeChangedListener = 
            new HeightChangedLinearLayout.LayoutSizeChangedListener() {
        @Override
        public void onLayoutSizeChanged(int w, int h, int oldw, int oldh) {
            if (h == oldh || mTextEditor == null || mTextEditor.getVisibility() == View.GONE) {
                return;
            }
            Xlog.d(TAG, "onLayoutSizeChanged(): mIsLandscape = " + mIsLandscape);
            if (!mIsLandscape) {
                if (h > oldh) {
                    updateTextEditorHeightInFullScreen();
                } else {
                    mUiHandler.postDelayed(new Runnable() {
                        public void run() {
                            Xlog.d(TAG, "onLayoutSizeChanged(): mTextEditor.setMaxHeight: "
                                    + mReferencedTextEditorFourLinesHeight);
                            mTextEditor.setMaxHeight(mReferencedTextEditorFourLinesHeight
                                    * mCurrentMaxHeight / mReferencedMaxHeight);
                        }
                    }, 100);
                }
            }
        }
    };

    private void updateTextEditorHeightInFullScreen() {
        if (mIsLandscape || mTextEditor == null || mTextEditor.getVisibility() == View.GONE) {
            return;
        }
        mUiHandler.postDelayed(new Runnable() {
            public void run() {
                if (mAttachmentEditor.getVisibility() == View.VISIBLE 
                        && mAttachmentEditor.getHeight() > 0
                        && !mWorkingMessage.hasSlideshow()) {
                    Xlog.d(TAG, "updateTextEditorHeight(): mTextEditor.setMaxHeight: "
                            + (mReferencedTextEditorSevenLinesHeight
                                    * mCurrentMaxHeight / mReferencedMaxHeight));
                    mTextEditor.setMaxHeight(mReferencedTextEditorSevenLinesHeight
                            * mCurrentMaxHeight / mReferencedMaxHeight);
                } else {
                    Xlog.d(TAG, "updateTextEditorHeight(): mTextEditor.setMaxHeight: "
                        + ((mReferencedTextEditorSevenLinesHeight + mReferencedAttachmentEditorHeight)
                                * mCurrentMaxHeight / mReferencedMaxHeight));
                    mTextEditor.setMaxHeight(
                            (mReferencedTextEditorSevenLinesHeight + mReferencedAttachmentEditorHeight)
                            * mCurrentMaxHeight / mReferencedMaxHeight);
                }
            }
        }, 100);
    }

    @Override
    public boolean  dispatchTouchEvent(MotionEvent event){

        boolean ret = false;

//MTK_OP01_PROTECT_START
        if(mIsCmcc && mScaleDetector != null){
                ret = mScaleDetector.onTouchEvent(event);
        }
//MTK_OP01_PROTECT_END

        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    }

//MTK_OP01_PROTECT_START
    // add for cmcc changTextSize by multiTouch
    private void changeTextSize(float size){
        if(mTextEditor != null){
            mTextEditor.setTextSize(size);
        }
        if(mMsgListAdapter != null){
            mMsgListAdapter.setTextSize(size);
        }

        if(mMsgListView != null && mMsgListView.getVisibility() == View.VISIBLE){
            int count = mMsgListView.getChildCount();
            for(int i = 0; i < count; i++){
                MessageListItem item =  (MessageListItem)mMsgListView.getChildAt(i);
                if(item != null){
                    item.setBodyTextSize(size);
                }
            }
        }
    }

    public class ScaleListener implements OnScaleListener{

        public boolean onScaleStart(ScaleDetector detector) {
            Xlog.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
            return true;
        }

        public void onScaleEnd(ScaleDetector detector) {
            Xlog.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);

            //save current value to preference
            MessageUtils.setTextSize(ComposeMessageActivity.this, mTextSize);
        }

        public boolean onScale(ScaleDetector detector) {

            float size = mTextSize * detector.getScaleFactor();

            if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
                return false;
            }
            if(size < MIN_TEXT_SIZE){
                size = MIN_TEXT_SIZE;
            }
            if(size > MAX_TEXT_SIZE){
                size = MAX_TEXT_SIZE;
            }
            if(size != mTextSize){
                changeTextSize(size);
                mTextSize = size;
                Log.e("mtk80999", "mTextSize = " + mTextSize);
            }
            return true;
        }
    };
//MTK_OP01_PROTECT_END
}
