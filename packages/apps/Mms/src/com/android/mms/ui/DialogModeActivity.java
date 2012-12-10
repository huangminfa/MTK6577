/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

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
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager.LayoutParams;
import com.android.mms.R;
import com.android.mms.MmsApp;
import com.mediatek.xlog.Xlog;
import com.mediatek.xlog.SXlog;
import java.util.Iterator;
import com.android.mms.ui.MsgNumSlideview.MsgNumBarSlideListener;
import com.android.mms.ui.MsgContentSlideView.MsgContentSlideListener;
import android.view.View;
import android.view.ViewStub;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.database.Cursor;
import com.android.mms.data.ContactList;
import android.provider.Telephony.SIMInfo;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import com.mediatek.featureoption.FeatureOption;
import android.graphics.drawable.Drawable;
import com.android.mms.data.Contact;
import com.android.mms.ui.QuickContactDivot;
import android.view.View.OnClickListener;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PresenterFactory;
import com.android.mms.ui.Presenter;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.android.mms.model.SlideshowModel;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageButton;
import java.util.Map;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.PduHeaders;
import com.android.mms.ui.VideoAttachmentView;
import com.android.mms.model.SlideModel;
import com.android.mms.model.TextModel;
import android.text.SpannableStringBuilder;
import com.android.mms.util.SmileyParser;
import android.text.TextUtils;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import com.android.internal.telephony.TelephonyProperties;
import com.android.mms.MmsConfig;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.net.ConnectivityManager;
import android.widget.EditText;
import android.app.AlertDialog;
import com.mediatek.CellConnService.CellConnMgr;
import android.text.TextWatcher;
import android.text.InputFilter;
import android.widget.Toast;
import android.text.Spanned;
import android.text.method.TextKeyListener;
import java.util.HashMap;
import com.mediatek.telephony.TelephonyManagerEx;
import android.widget.SimpleAdapter;
import android.content.DialogInterface;
import com.android.internal.telephony.TelephonyIntents;
import android.content.ActivityNotFoundException;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.telephony.SmsMessage;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.Editable;
import com.android.mms.transaction.MessagingNotification;
import android.content.ContentValues;
import android.widget.Button;
import android.database.sqlite.SqliteWrapper;
//import android.content.DialogInterface.OnClickListener;
//import android.content.DialogInterface.OnClickListener;
import com.google.android.mms.pdu.EncodedStringValue;
import android.graphics.Typeface;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.MmsException;
import com.android.mms.ui.FolderViewList;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

//Dialog mode
public class DialogModeActivity extends Activity implements MsgNumBarSlideListener, MsgContentSlideListener, OnClickListener, SlideViewInterface, MessageStatusListener {

    private static final String TAG = "Mms/DialogMode";
    
    private final ArrayList<Uri> mUris;
    private int mUriNum;
    private int mCurUriIdx;
    private Uri mCurUri;
    private MsgNumSlideview mMsgNumBar;
    private ImageView mLeftArrow;
    private ImageView mRightArrow;
    private TextView mMsgNumText;
    private TextView mSender;
    private MsgContentSlideView mContentLayout;
    private TextView mSmsContentText;
    private TextView mRecvTime;
    private TextView mSimName;
    private TextView mSimVia;
    private QuickContactDivot mContactImage;
    private static Drawable sDefaultContactImage;
    private View mMmsView;
    private ImageView mMmsImageView;
    private ImageButton mMmsPlayButton;
    private EditText mReplyEditor;
    private ImageButton mSendButton;
    private TextView mTextCounter;
    private Button mMarkAsReadBtn;
    private Button mDeleteBtn;
    private ImageButton mCloseBtn;
    
    private Cursor mCursor;
    private boolean mWaitingForSubActivity;
    
    private DialogModeReceiver mReceiver;
    private boolean mContentViewSet;
    
    public DialogModeActivity() {
        mUris = new ArrayList<Uri>();
        mUriNum = 0;
        mCurUriIdx = 0;
        mCurUri = null;
        mCursor = null;
        mMmsView = null;
        mMmsImageView = null;
        mMmsPlayButton = null;
        mCellMgr = null;
        mReceiver = null;
        mContentViewSet = false;
        mWorkingMessage = null;
        mWaitingForSubActivity = false;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Xlog.d(TAG, "DialogModeActivity.onCreate");
        if (!isHome()) {
            Xlog.d(TAG, "not at Home, just finish");
            finish();
            return;
        }

        Xlog.d(TAG, "at Home");
        registerReceiver();
        addNewUri(getIntent());
        loadCurMsg();
        initDialogView();
        setDialogView();

        //mWorkingMessage = null;
        //mWaitingForSubActivity = false;
        if (mCellMgr == null) {
            mCellMgr = new CellConnMgr();
            mCellMgr.register(getApplication());
        }
        
        resetMessage();
    }

    private void registerReceiver() {
        Xlog.d(TAG, "DialogModeActivity.registerReceiver");
        if (mReceiver != null) {
            return;
        }
        Xlog.d(TAG, "register receiver");
        mReceiver = new DialogModeReceiver();
        IntentFilter filter = new IntentFilter("com.android.mms.dialogmode.VIEWED");
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onDestroy() {
        Xlog.d(TAG, "DialogModeActivity.onDestroy");
        if (mCellMgr != null) {
            mCellMgr.unregister();
            mCellMgr = null;
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        Xlog.d(TAG, "DialogModeActivity.onNewIntent");
        super.onNewIntent(intent);

        registerReceiver();
        addNewUri(intent);
        loadCurMsg();
        initDialogView();
        setDialogView();
        if (mCellMgr == null) {
            mCellMgr = new CellConnMgr();
            mCellMgr.register(getApplication());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Xlog.d(TAG, "DialogModeActivity.onActivityResult, requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        mWaitingForSubActivity = false;

        if (resultCode != RESULT_OK){
            Xlog.d(TAG, "fail due to resultCode=" + resultCode);
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_ECM_EXIT_DIALOG:
                boolean outOfEmergencyMode = data.getBooleanExtra(EXIT_ECM_RESULT, false);
                Xlog.d(TAG, "REQUEST_CODE_ECM_EXIT_DIALOG, mode=" + outOfEmergencyMode);
                
                if (outOfEmergencyMode) {
                    sendMessage(false);
                }
                break;
                
            default:
                Xlog.d(TAG, "bail due to unknown requestCode=" + requestCode);
                break;
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        Xlog.d(TAG, "DialogModeActivity.startActivityForResult");
        if (requestCode >= 0) {
            mWaitingForSubActivity = true;
        }
        super.startActivityForResult(intent, requestCode);
    }

    Runnable mResetMessageRunnable = new Runnable() {
        public void run() {
            Xlog.d(TAG, "mResetMessageRunnable.run");
            resetMessage();
        }
    };

    private void resetMessage() {
        Xlog.d(TAG, "DialogModeActivity.resetMessage");

        //mReplyEditor.requestFocus();

        // We have to remove the text change listener while the text editor gets cleared and
        // we subsequently turn the message back into SMS. When the listener is listening while
        // doing the clearing, it's fighting to update its counts and itself try and turn
        // the message one way or the other.
        mReplyEditor.removeTextChangedListener(mTextEditorWatcher);

        // Clear the text box.
        TextKeyListener.clear(mReplyEditor.getText());

        if (mWorkingMessage != null) {
            Xlog.d(TAG, "clear working message");
            mWorkingMessage.clearConversation(getConversation(), false);
            //mWorkingMessage = WorkingMessage.createEmpty(this);
            //mWorkingMessage.setConversation(getConversation());
            mWorkingMessage = null;
        }

        updateSendButtonState();
        mReplyEditor.addTextChangedListener(mTextEditorWatcher);
        mReplyEditor.setText("");
        mSendingMessage = false;
   }
    
    public void onPreMessageSent() {
        Xlog.d(TAG, "DialogModeActivity.onPreMessageSent");
        runOnUiThread(mResetMessageRunnable);
    }

    public void onMessageSent() {
        Xlog.d(TAG, "DialogModeActivity.onMessageSent");
        mWaitingForSendMessage = false;
        //String body = getString(R.string.strOk);
        //Xlog.d(TAG, "string=" + body);
        //Toast.makeText(DialogModeActivity.this, body, Toast.LENGTH_SHORT).show();
        runOnUiThread(mMessageSentRunnable);
    }

    Runnable mMessageSentRunnable = new Runnable() {
        public void run() {
            Xlog.d(TAG, "mMessageSentRunnable.run");
            String body = getString(R.string.strOk);
            Xlog.d(TAG, "string=" + body);
            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_SHORT).show();
        }
    };
    
    public void onProtocolChanged(boolean mms, boolean needToast) {
        Xlog.d(TAG, "DialogModeActivity.onProtocolChanged");
    }

    public void onAttachmentChanged() {
        Xlog.d(TAG, "DialogModeActivity.onAttachmentChanged");
    }

    public void onPreMmsSent() {
        Xlog.d(TAG, "DialogModeActivity.onPreMmsSent");
    }

    public void onMaxPendingMessagesReached() {
        Xlog.d(TAG, "DialogModeActivity.onMaxPendingMessagesReached");
    }

    public void onAttachmentError(int error) {
        Xlog.d(TAG, "DialogModeActivity.onAttachmentError");
    }
    
    private void addNewUri(Intent intent) {
        if (intent == null)
            return;
        
        String newString = intent.getStringExtra("com.android.mms.transaction.new_msg_uri");
        Xlog.d(TAG, "DialogModeActivity.addNewUri, new uri=" + newString);
        Uri newUri = Uri.parse(newString);
        mUris.add(mUris.size(), newUri);
        mCurUriIdx = mUris.size() - 1;
        Xlog.d(TAG, "new index=" + mCurUriIdx);
    }
    
    private void initDialogView() {
        Xlog.d(TAG, "DialogModeActivity.initDialogView");

        if (mContentViewSet == true) {
            Xlog.d(TAG, "have init");
            return;
        }

        //initDislogSize();
        setContentView(R.layout.msg_dlg_activity);
        mContentViewSet = true;
        getSimInfoList();
        
        //Msg number bar
        mMsgNumBar = (MsgNumSlideview)findViewById(R.id.msg_number_bar_linear);
        mLeftArrow = (ImageView)findViewById(R.id.left_arrow);
        mRightArrow = (ImageView)findViewById(R.id.right_arrow);
        mMsgNumText = (TextView)findViewById(R.id.msg_counter);
        //mMsgNumBar.registerFlingListener(this);

        mSender = (TextView)findViewById(R.id.recepient_name);
        Typeface tf = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        if (tf != null)
            mSender.setTypeface(tf);
        
        mCloseBtn = (ImageButton)findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);
        
        mContentLayout = (MsgContentSlideView)findViewById(R.id.content_scroll_view);
        mContentLayout.registerFlingListener(this);
        mSmsContentText = (TextView)findViewById(R.id.msg_content);
        mSmsContentText.setClickable(true);
        mSmsContentText.setOnClickListener(this);
        if (tf != null)
            mSmsContentText.setTypeface(tf);
        
        mRecvTime = (TextView)findViewById(R.id.msg_recv_timer);
        if (tf != null)
            mRecvTime.setTypeface(tf);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            LinearLayout simInfo = (LinearLayout)findViewById(R.id.sim_info_linear);
            simInfo.setVisibility(View.VISIBLE);
            
            mSimName = (TextView)findViewById(R.id.sim_name);
            mSimName.setVisibility(View.VISIBLE);
            
            mSimVia = (TextView)findViewById(R.id.sim_via_text);
            mSimVia.setVisibility(View.VISIBLE);

            if (tf != null) {
                mSimName.setTypeface(tf);
                mSimVia.setTypeface(tf);
            }
        }
        else {
            mSimName = null;
            mSimVia = null;
        }

        mContactImage = (QuickContactDivot)findViewById(R.id.contact_img);
        sDefaultContactImage = getApplicationContext().getResources().getDrawable(R.drawable.ic_contact_picture);
/*
        mMmsView = findViewById(R.id.msg_dlg_mms_view);
        mMmsView.setVisibility(View.GONE);
        mMmsImageView = (ImageView) findViewById(R.id.msg_dlg_image_view);
        mMmsImageView.setVisibility(View.GONE);
        mMmsPlayButton = (ImageButton) findViewById(R.id.msg_dlg_play_slideshow_button);
        mMmsPlayButton.setVisibility(View.GONE);
*/
        mReplyEditor = (EditText)findViewById(R.id.embedded_reply_text_editor);
        mReplyEditor.addTextChangedListener(mTextEditorWatcher);
        mReplyEditor.setFilters(new InputFilter[] {
                new TextLengthFilter(MmsConfig.getMaxTextLimit())});
        
        mSendButton = (ImageButton)findViewById(R.id.reply_send_button);
        mSendButton.setOnClickListener(this);
        mTextCounter = (TextView)findViewById(R.id.text_counter);

        mMarkAsReadBtn = (Button)findViewById(R.id.mark_as_read_btn);
        mMarkAsReadBtn.setOnClickListener(this);

        mDeleteBtn = (Button)findViewById(R.id.delete_btn);
        mDeleteBtn.setOnClickListener(this);

        mReplyEditor.setText("");
    }

    private void setDialogView() {
        Xlog.d(TAG, "DialogModeActivity.setDialogView");

        //Msg count bar
        int msgNum = mUris.size();
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.msg_number_bar_relative);
        if (msgNum <= 1) {
            rl.setVisibility(View.GONE);
            //mMsgNumBar.setVisibility(View.GONE);
            mMsgNumBar.unregisterFlingListener();
        }
        else {
            StringBuilder msgNumStrBuilder = new StringBuilder("");
            msgNumStrBuilder.append(mCurUriIdx+1);
            msgNumStrBuilder.append('/');
            msgNumStrBuilder.append(msgNum);
            String msgNumStr = msgNumStrBuilder.toString();
            mMsgNumText.setText(msgNumStr);

            /*
                    if (mCurUriIdx <= 0) {
                        mLeftArrow.setVisibility(View.INVISIBLE);
                    }
                    else {
                        mLeftArrow.setVisibility(View.VISIBLE);
                    }
                    if (mCurUriIdx < (msgNum - 1)) {
                        mRightArrow.setVisibility(View.VISIBLE);
                    }
                    else {
                        mRightArrow.setVisibility(View.INVISIBLE);
                    }
                    */
            mLeftArrow.setVisibility(View.VISIBLE);
            mRightArrow.setVisibility(View.VISIBLE);
            //mMsgNumBar.setVisibility(View.VISIBLE);
            rl.setVisibility(View.VISIBLE);
            mMsgNumBar.registerFlingListener(this);
        }

        mSender.setText(getSenderString());
        mSmsContentText.setText(getSmsContent());
        mRecvTime.setText(getReceivedTime());
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mSimName.setText(getSIMName());
            int color = getSIMColor();
            if (color != 0)
                mSimName.setBackgroundDrawable(getResources().getDrawable(getSIMColor()));
        }

        Drawable image = getContactImage();
        if (image != null){
            mContactImage.setImageDrawable(image);
        }

        if (!isCurSMS()) {
            Xlog.d(TAG, "a MMS");
            loadMmsView();
        }
        else {
            if (mMmsView != null) {
                Xlog.d(TAG, "Hide MMS views");
                mMmsView.setVisibility(View.GONE);
            }
        }
        
    }

    private void initDislogSize() {
        //Display display = getWindowManager().getDefaultDisplay();
        //setTheme(R.style.MmsHoloTheme);
        //setTheme(R.style.SmsDlgScreen);
        setContentView(R.layout.msg_dlg_activity);
        //LayoutParams p = getWindow().getAttributes();
        //p.height = (int)(display.getHeight() / 3);
        //getWindow().setAttributes(p);
    }
    
    private List<String> getHomes() {        
        Xlog.d(TAG, "DialogModeActivity.getHomes");
        
        List<String> names = new ArrayList<String>();  
        PackageManager packageManager = this.getPackageManager();  
        Intent intent = new Intent(Intent.ACTION_MAIN);  
        intent.addCategory(Intent.CATEGORY_HOME);  
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,  
                PackageManager.MATCH_DEFAULT_ONLY);  
        
        for(ResolveInfo ri : resolveInfo){  
            names.add(ri.activityInfo.packageName);  
            //System.out.println(ri.activityInfo.packageName);
            //System.out.println(ri.activityInfo.name);
            Xlog.d(TAG, "package name="+ri.activityInfo.packageName);
            Xlog.d(TAG, "class name="+ri.activityInfo.name);
        }  
        return names;  
    }  

    
    public boolean isHome(){
        List<String> homePackageNames = getHomes();
        
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);  
        List<RunningTaskInfo> rti = activityManager.getRunningTasks(2);  
        
        RunningTaskInfo info = rti.get(0);
        String packageName0 = info.topActivity.getPackageName();
        String className0 = info.topActivity.getClassName();
        //String baseClass = info.get(0).baseActivity.getClassName();
        int num0 = info.numActivities;
        Xlog.d(TAG, "package0="+packageName0+" class0="+className0 + " num0=" + num0);

        info = rti.get(1);
        String packageName1 = info.topActivity.getPackageName();
        String className1 = info.topActivity.getClassName();
        Xlog.d(TAG, "package1="+packageName1+"class1="+className1);
        
        boolean ret;
        /* Below is Launcher?*/
        ret = homePackageNames.contains(packageName1);
        if (ret == true) {
            if (className0.equals("com.android.mms.ui.DialogModeActivity") &&
                num0 == 1) {
                ret = true;
            } else {
                ret = false;
            }
        }
        return ret;
    }  

    public void onSlideToPrev() {
        int msgNum = mUris.size();
        
        Xlog.d(TAG, "DialogModeActivity.onSlideToPrev, num=" + msgNum);
        
        if (msgNum <= 1) {
            return;
        }
        if (mCurUriIdx == 0) {
            return;
        }
        mCurUriIdx--;
        loadCurMsg();
        setDialogView();
    }

    public void onSlideToNext() {
        int msgNum = mUris.size();
        
        Xlog.d(TAG, "DialogModeActivity.onSlideToNext, num=" + msgNum);
        
        if (msgNum <= 1) {
            return;
        }
        if (mCurUriIdx == (msgNum - 1)) {
            return;
        }
        mCurUriIdx++;
        loadCurMsg();
        setDialogView();
    }

    private final static int SMS_ID = 0;
    private final static int SMS_TID = 1;
    private final static int SMS_ADDR = 2;
    private final static int SMS_DATE = 3;
    private final static int SMS_READ = 4;
    private final static int SMS_BODY = 5;
    private final static int SMS_SIM = 6;
    private final static int SMS_TYPE = 7;
    private final static String TYPE_MMS = "mms";
    private final static String TYPE_SMS = "sms";

    private boolean isCurSMS() {
        Xlog.d(TAG, "DialogModeActivity.isCurSMS");
        mCurUri = (Uri)mUris.get(mCurUriIdx);
        if (mCurUri == null) {
            Xlog.d(TAG, "no uri available");
            mCursor = null;
            return true;
        }

        //List<String> segs = mCurUri.getPathSegments();
        //String type = segs.get(0);
        String type = mCurUri.getAuthority();
        Xlog.d(TAG, "type=" + type);
        if (type.equals(TYPE_SMS)) {
            return true;
        }

        return false;
    }
    
    private Cursor loadCurMsg() {
        Xlog.d(TAG, "DialogModeActivity.loadCurMsg, idx=" + mCurUriIdx);

        mCurUri = (Uri)mUris.get(mCurUriIdx);
        if (mCurUri == null) {
            Xlog.d(TAG, "no uri available");
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            return null;
        }

        Xlog.d(TAG, "uri=" + mCurUri.toString());
        String projection[];
        if (isCurSMS()) {
            projection = 
    			new String[] {"_id", "thread_id", "address", "date", "read", "body", "sim_id"};
        }
        else {
            projection = 
    			new String[] {"_id", "thread_id", "null as address", "date", "read", "sub", "sim_id", "m_type"};
        }

        if (mCursor != null) {
            mCursor.close();
        }
        
        Cursor cursor = getContentResolver().query(mCurUri, 
			projection, 
			null,
			null,
		    null);

        if (cursor == null) {
            Xlog.d(TAG, "no msg found");
            mCursor = null;
            return null;
        }
/*
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(SMS_ID);
            Xlog.d(TAG, "id=" + id);
            long tid =cursor.getLong(SMS_TID); 
            Xlog.d(TAG, "tid=" + tid);
			String addr = cursor.getString(SMS_ADDR);
            Xlog.d(TAG, "addr=" + addr);
            long date = cursor.getLong(SMS_DATE);
            Xlog.d(TAG, "date=" + date);
            int read = cursor.getInt(SMS_READ);
            Xlog.d(TAG, "read=" + read);
            String body = cursor.getString(SMS_BODY);
            Xlog.d(TAG, "body=" + body);
            int sim = cursor.getInt(SMS_SIM);
            Xlog.d(TAG, "sim=" + sim);
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
        }
*/
        mCursor = cursor;
        return cursor;
    }

    private void deleteCurMsg() {
        Xlog.d(TAG, "DialogModeActivity.deleteCurMsg");

        mCurUri = (Uri)mUris.get(mCurUriIdx);
        if (mCurUri == null) {
            Xlog.d(TAG, "no uri available");
            //mCursor = null;
            return;
        }

        Xlog.d(TAG, "uri=" + mCurUri.toString());
        
        DeleteMessageListener l = new DeleteMessageListener(mCurUri);
        confirmDeleteDialog(l);
    }

    private void confirmDeleteDialog(android.content.DialogInterface.OnClickListener listener) {
        Xlog.d(TAG, "DialogModeActivity.confirmDeleteDialog");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setMessage(R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private class DeleteMessageListener implements android.content.DialogInterface.OnClickListener {
        private final Uri mDeleteUri;
        
        public DeleteMessageListener(Uri uri) {
            mDeleteUri = uri;
        }
/*
        public DeleteMessageListener(long msgId, String type) {
            if ("mms".equals(type)) {
                mDeleteUri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgId);
            } else {
                mDeleteUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
            }
        }
*/
        public void onClick(DialogInterface dialog, int whichButton) {
            Xlog.d(TAG, "DeleteMessageListener.onClick, " + mDeleteUri.toString());
            //mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
            //        null, mDeleteUri, null, null);
            SqliteWrapper.delete(getApplicationContext(), 
                getContentResolver(), 
                mDeleteUri, null, null);

            DialogModeActivity.this.removeMsg(mDeleteUri);
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(DialogModeActivity.this, false, false);
            dialog.dismiss();
        }
    }
    
    private void removeCurMsg() {
        Xlog.d(TAG, "DialogModeActivity.removeCurMsg");

        mCurUri = (Uri)mUris.get(mCurUriIdx);
        if (mCurUri == null) {
            Xlog.d(TAG, "no uri available");
            //mCursor = null;
            return;
        }
        Xlog.d(TAG, "uri=" + mCurUri.toString());
        mUris.remove(mCurUriIdx);
        if (mUris.size() == 0){
            Xlog.d(TAG, "no msg");
            finish();
            return;
        }
        
        if (mCurUriIdx != 0) {
            mCurUriIdx--;
        }

        loadCurMsg();
        setDialogView();
    }
    
    private void removeMsg(Uri deleteUri) {
        Xlog.d(TAG, "DialogModeActivity.removeMsg + " + deleteUri);

        int idx = mUris.indexOf(deleteUri);
        if (idx == mCurUriIdx) {
            removeCurMsg();
            return;
        }

        mUris.remove(idx);
        if (mUris.size() == 0){
            Xlog.d(TAG, "no msg");
            finish();
            return;
        }

        if (mCurUriIdx != 0) {
            mCurUriIdx--;
        }

        loadCurMsg();
        setDialogView();
    }
    
    private String getSenderString() {
        Xlog.d(TAG, "DialogModeActivity.getSenderString");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return new String("");
        }

        if (mCursor.moveToFirst()) {
            if (isCurSMS()) {
    			String recipientIds = mCursor.getString(SMS_ADDR);
                ContactList recipients;
                recipients = ContactList.getByNumbers(recipientIds, false, true);
                //Xlog.d(TAG, "recipients=" + recipientIds);
                Xlog.d(TAG, "recipients=" + recipients.formatNames(", "));
                return recipients.formatNames(", ");
            }
            else {
                Conversation conv = Conversation.get(this, getThreadId(), true);
                if (conv == null) {
                    Xlog.d(TAG, "conv null");
                    return new String("");
                }
                ContactList recipients = conv.getRecipients();
                Xlog.d(TAG, "recipients=" + recipients.formatNames(", "));
                return recipients.formatNames(", ");
            }
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return new String("");
        }
    }

    private String getSenderNumber() {
        Xlog.d(TAG, "DialogModeActivity.getSenderNumber");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return new String("");
        }

        if (mCursor.moveToFirst()) {
            if (isCurSMS()) {
            String addr = mCursor.getString(SMS_ADDR);
            Xlog.d(TAG, "addr=" + addr);
            return addr;
            } else {
                Conversation conv = Conversation.get(this, getThreadId(), true);
                if (conv == null) {
                    Xlog.d(TAG, "conv null");
                    return new String("");
                }
                ContactList recipients = conv.getRecipients();
                String[] numbers = recipients.getNumbers();
                //Xlog.d(TAG, "recipients=" + recipients.formatNames(", "));
                if (numbers != null) {
                    Xlog.d(TAG, "number0=" + numbers[0]);
                    return numbers[0];
                } else {
                    Xlog.d(TAG, "empty number");
                    return new String("");
                }
            }
	}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return new String("");
        }
    }
    private String getSmsContent() {
        Xlog.d(TAG, "DialogModeActivity.getSmsContent");

        if (!isCurSMS()) {
            return new String("");
        }
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return new String("");
        }

        if (mCursor.moveToFirst()) {
			String content = mCursor.getString(SMS_BODY);
            Xlog.d(TAG, "content=" + content);
            return content;
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return new String("");
        }
    }

    private String getReceivedTime() {
        Xlog.d(TAG, "DialogModeActivity.getReceivedTime");

        StringBuilder builder = new StringBuilder("");
        builder.append(getString(R.string.received_header));
        builder.append(" ");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return builder.toString();
        }

        if (mCursor.moveToFirst()) {
			long date = mCursor.getLong(SMS_DATE);
            String strDate;
            
            if (isCurSMS())
                strDate = MessageUtils.formatTimeStampString(getApplicationContext(), date);
            else
                strDate = MessageUtils.formatTimeStampString(getApplicationContext(), date * 1000L);

            Xlog.d(TAG, "date=" + strDate);
            builder.append(strDate);
            return builder.toString();
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return builder.toString();
        }
    }

    private String getSIMName() {
        Xlog.d(TAG, "DialogModeActivity.getSIMName");

        StringBuilder builder = new StringBuilder("");
        //builder.append(" ");

        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return builder.toString();
        }

        if (mCursor.moveToFirst()) {
			long sim_id = mCursor.getLong(SMS_SIM);
            Xlog.d(TAG, "sim=" + sim_id);
            SIMInfo simInfo = SIMInfo.getSIMInfoById(getApplicationContext(), sim_id);
            builder.append(simInfo.mDisplayName);
            return builder.toString();
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return builder.toString();
        }        
    }

    private int getSIMColor() {
        Xlog.d(TAG, "DialogModeActivity.getSIMColor");

        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return 0;
        }

        if (mCursor.moveToFirst()) {
			long sim_id = mCursor.getLong(SMS_SIM);
            Xlog.d(TAG, "sim=" + sim_id);
            SIMInfo simInfo = SIMInfo.getSIMInfoById(getApplicationContext(), sim_id);
            Xlog.d(TAG, "color=" + simInfo.mSimBackgroundRes);
            Xlog.d(TAG, "color=" + simInfo.mColor);
            return simInfo.mSimBackgroundRes;
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return 0;
        }        
    }
    
    private Drawable getContactImage() {
        Xlog.d(TAG, "DialogModeActivity.getContactImage");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return sDefaultContactImage;
        }

        if (mCursor.moveToFirst()) {
            ContactList recipients;
            
            if (isCurSMS()) {
    			String recipientIds = mCursor.getString(SMS_ADDR);
                recipients = ContactList.getByNumbers(recipientIds, false, true);
            }
            else {
                Conversation conv = Conversation.get(this, getThreadId(), true);
                if (conv == null) {
                    Xlog.d(TAG, "conv null");
                    return sDefaultContactImage;
                }
                recipients = conv.getRecipients();
                Xlog.d(TAG, "recipients=" + recipients.formatNames(", "));
                return sDefaultContactImage;
            }
            Contact contact = recipients.get(0);
            if (contact == null) {
                Xlog.d(TAG, "no contact");
                return sDefaultContactImage;
            }
            return contact.getAvatar(getApplicationContext(), sDefaultContactImage);
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return sDefaultContactImage;
        }
    }

    private long getThreadId() {
        Xlog.d(TAG, "DialogModeActivity.getThreadId");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return -1;
        }

        if (mCursor.moveToFirst()) {
            long tid = mCursor.getLong(SMS_TID); 
            Xlog.d(TAG, "tid=" + tid);
			return tid;
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return -1;
        }
    }

    private Conversation getConversation() {
        Xlog.d(TAG, "DialogModeActivity.getConversation");
        long tid = getThreadId();
        if (tid < 0) {
            Xlog.d(TAG, "invalid tid");
            return null;
        }

        Xlog.d(TAG, "tid=" + tid);
        Conversation conv = Conversation.get(this, tid, true); //new Conversation(this, tid, true);
        if (conv == null) {
            Xlog.d(TAG, "conv null");
            return null;
        }

        return conv;
    }
    
    //Implements OnClickListener
    public void onClick(View v)
    {
        Xlog.d(TAG, "DialogModeActivity.onClick");
        if (v == mSmsContentText) {
            Xlog.d(TAG, "Clicent content view");
            openThread(getThreadId());
        }
        else if (v == mMmsPlayButton) { //PLay MMS
            Xlog.d(TAG, "View this MMS");
            try {
                MessageUtils.viewMmsMessageAttachment(this, mCurUri, null);
            } catch(Exception e) {
                Xlog.d(TAG, e.toString());
            }
        }
        else if (v == mSendButton) {
            Xlog.d(TAG, "Send SMS");
            sendReplySms();
        }
        else if (v == mMarkAsReadBtn) {
            Xlog.d(TAG, "Mark as read");
            if (mCurUri != null) {
                markAsRead(mCurUri);
            }
        }
        else if (v == mDeleteBtn) {
            Xlog.d(TAG, "Delete");
            deleteCurMsg();
        }
        else if (v == mCloseBtn) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            finish();
        }
    }
    
    private void openThread(long threadId) {
        Xlog.d(TAG, "DialogModeActivity.openThread " + threadId);
        
        if (MmsConfig.getMmsDirMode()) {
            Xlog.d(TAG, "go to inbox");
            
            Intent it = new Intent(this, FolderViewList.class);
            it.putExtra("floderview_key", FolderViewList.OPTION_INBOX);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(it);
        }
        else {
        if (threadId < 0)
            return;
        startActivity(ComposeMessageActivity.createIntent(this, threadId));
        }
        finish();
    }

    private void loadMmsView() {
        Xlog.d(TAG, "DialogModeActivity.loadMmsView ");
        
        if (mMmsView == null) {
            Xlog.d(TAG, "set Mms views visible");
            //findViewById(R.id.mms_thumbnail_stub).setVisibility(View.VISIBLE);

            mMmsView = findViewById(R.id.msg_dlg_mms_view);
            mMmsImageView = (ImageView) findViewById(R.id.msg_dlg_image_view);
            mMmsPlayButton = (ImageButton) findViewById(R.id.msg_dlg_play_slideshow_button);

            mMmsImageView.setVisibility(View.VISIBLE);
            mMmsPlayButton.setVisibility(View.VISIBLE);
        }

        loadMmsContents();
    }

    private AlertDialog mSIMSelectDialog;
    private int mAssociatedSimId;
    private long mMessageSimId;
    private int mSelectedSimId;
    private static CellConnMgr mCellMgr = null;
    private WorkingMessage mWorkingMessage;
    private boolean mSendingMessage;
    private boolean mWaitingForSendMessage;
    public static final int REQUEST_CODE_ECM_EXIT_DIALOG  = 107;
    private static final String EXIT_ECM_RESULT         = "exit_ecm_result";
    private static final String SELECT_TYPE                             = "Select_type";
    private static final int SIM_SELECT_FOR_SEND_MSG                    = 1;
    private boolean mSentMessage;
    
    private void sendReplySms() {
        Xlog.d(TAG, "DialogModeActivity.sendReplySms");
        simSelection();
    }

    private void simSelection() {
        Xlog.d(TAG, "DialogModeActivity.simSelection");
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == false) {
            Xlog.d(TAG, "non GEMINI");
            confirmSendMessageIfNeeded();
        } else if (mSimCount == 0) {
            Xlog.d(TAG, "no card");
            // SendButton can't click in this case
        } else if (mSimCount == 1) {
            Xlog.d(TAG, "1 card");
            mSelectedSimId = (int) mSimInfoList.get(0).mSimId;
            confirmSendMessageIfNeeded();
        } else if (mSimCount > 1) {
            Xlog.d(TAG, "2 cards");
            Intent intent = new Intent();
            intent.putExtra(SELECT_TYPE, SIM_SELECT_FOR_SEND_MSG);
            // getContactSIM
            String number = getSenderNumber();
            if (number == null || number.length() == 0) {
                mAssociatedSimId = -1;
            }
            else {
                mAssociatedSimId = getContactSIM(number);
            }
            Xlog.d(TAG, "mAssociatedSimId=" + mAssociatedSimId);
            
            mMessageSimId = Settings.System.getLong(getContentResolver(), 
                Settings.System.SMS_SIM_SETTING, 
                Settings.System.DEFAULT_SIM_NOT_SET);
            Xlog.d(TAG, "mMessageSimId=" + mMessageSimId);
            
            if (mMessageSimId == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                // always ask, show SIM selection dialog
                showSimSelectedDialog(intent);
                updateSendButtonState();
            } else if (mMessageSimId == Settings.System.DEFAULT_SIM_NOT_SET) {
                if (mAssociatedSimId == -1) {
                    showSimSelectedDialog(intent);
                    updateSendButtonState();
                } else {
                    mSelectedSimId = mAssociatedSimId;
                    confirmSendMessageIfNeeded();
                }
            } else {
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

    private int getContactSIM(String number) {
        Xlog.d(TAG, "DialogModeActivity.getContactSIM, " + number);
        
        int simId = -1;
        List simIdList = new ArrayList<Integer>();
        Cursor associateSIMCursor = DialogModeActivity.this.getContentResolver().query(
                Data.CONTENT_URI, 
                new String[]{Data.SIM_ASSOCIATION_ID}, 
                Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
                    + "' AND (" + Data.DATA1 + "='" + number + "') AND (" + Data.SIM_ASSOCIATION_ID + "!= -1)", 
                null,
                null
        );

        if (null == associateSIMCursor) {
            Xlog.i(TAG, " queryContactInfo : associateSIMCursor is null");
        } else {
            Xlog.i(TAG, " queryContactInfo : associateSIMCursor is not null. Count[" + 
                    associateSIMCursor.getCount() + "]");
        }

        if ((null != associateSIMCursor) && (associateSIMCursor.getCount() > 0)) {
            associateSIMCursor.moveToFirst();
            // Get only one record is OK
            simId = (Integer) associateSIMCursor.getInt(0);
        } else {
            simId = -1;
        }
        Xlog.d(TAG, "simId=" + simId);
        associateSIMCursor.close();
        return simId;
    }
    
    private void showSimSelectedDialog(Intent intent) {
        Xlog.d(TAG, "DialogModeActivity.showSimSelectedDialog");
        
        // TODO get default SIM and get contact SIM
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
            if (mAssociatedSimId == (int) simInfo.mSimId) {
                // if this SIM is contact SIM, set "Suggested"
                entry.put("suggested", getString(R.string.suggested));
            } else {
                entry.put("suggested", "");// not suggested
            }
            if (!MessageUtils.is3G(i, mSimInfoList)) {
                entry.put("sim3g", "");
            } else {
                String optr = SystemProperties.get("ro.operator.optr");
                //MTK_OP02_PROTECT_START
                if (optr != null && optr.equals("OP02")) {
                    entry.put("sim3g", "3G");
                } else 
                //MTK_OP02_PROTECT_END
                {
                    entry.put("sim3g", "");
                }
            }
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
                updateSendButtonState();
                mSelectedSimId = (int) mSimInfoList.get(which).mSimId;
                if (it.getIntExtra(SELECT_TYPE, -1) == SIM_SELECT_FOR_SEND_MSG) {
                    confirmSendMessageIfNeeded();
                }
                dialog.dismiss();
            }
        });
        mSIMSelectDialog = b.create();
        mSIMSelectDialog.show();
    }

    private void confirmSendMessageIfNeeded() {
        Xlog.d(TAG, "DialogModeActivity.confirmSendMessageIfNeeded");
        checkConditionsAndSendMessage(true);
    }

    private void checkConditionsAndSendMessage(boolean bCheckEcmMode){
        Xlog.d(TAG, "DialogModeActivity.checkConditionsAndSendMessage");
        // check pin
        // convert sim id to slot id
        int requestType = CellConnMgr.REQUEST_TYPE_SIMLOCK;
        final int slotId;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Xlog.d(TAG, "CEMINI");
            requestType = CellConnMgr.REQUEST_TYPE_ROAMING;
            slotId = SIMInfo.getSlotById(this, mSelectedSimId);
            Xlog.d(TAG, "check pin and...: simId=" + mSelectedSimId + "\t slotId=" + slotId);
        } else {
            Xlog.d(TAG, "non GEMILNI");
            slotId = 0;
        }
        final boolean bCEM = bCheckEcmMode;
        if (mCellMgr != null) {
        mCellMgr.handleCellConn(slotId, requestType, new Runnable() {
            public void run() {
                Xlog.d(TAG, "mCellMgr.run");
                
                int nRet = mCellMgr.getResult();
                Xlog.d(TAG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
                if (mCellMgr.RESULT_ABORT == nRet || mCellMgr.RESULT_OK == nRet) {
                    updateSendButtonState();
                    return;
                }
                if (slotId != mCellMgr.getPreferSlot()) {
                        Xlog.d(TAG, "111");
                    SIMInfo si = SIMInfo.getSIMInfoBySlot(DialogModeActivity.this, mCellMgr.getPreferSlot());
                    if (si == null) {
                            Xlog.e(TAG, "serviceComplete siminfo is null");
                        updateSendButtonState();
                        return;
                    }
                    mSelectedSimId = (int)si.mSimId;
                }
                sendMessage(bCEM);
            }
        });
    }
        else {
            Xlog.d(TAG, "mCellMgr is null!");
        }
    }

    private void sendMessage(boolean bCheckEcmMode) {
        Xlog.d(TAG, "DialogModeActivity.sendMessage," + bCheckEcmMode);
        
        if (bCheckEcmMode) {
            Xlog.d(TAG, "bCheckEcmMode=" + bCheckEcmMode);
            
            // TODO: expose this in telephony layer for SDK build
            String inEcm = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (Boolean.parseBoolean(inEcm)) {
                try {
                    Xlog.d(TAG, "show notice to block others");
                    startActivityForResult(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                            REQUEST_CODE_ECM_EXIT_DIALOG);
                    return;
                } catch (ActivityNotFoundException e) {
                    // continue to send message
                    Xlog.e(TAG, "Cannot find EmergencyCallbackModeExitDialog", e);
                }
            }
        }
        /*
            ContactList contactList = isRecipientsEditorVisible() ?
                mRecipientsEditor.constructContactsFromInput(false) : getRecipients();
            mDebugRecipients = contactList.serialize();
            */
        if (!mSendingMessage) {
            /*
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
                    */

            // send can change the recipients. Make sure we remove the listeners first and then add
            // them back once the recipient list has settled.
            //removeRecipientsListeners();
            Xlog.d(TAG, "new working message");
            mWorkingMessage = WorkingMessage.createEmpty(this, this);
            //mWorkingMessage.setMessageStatusListener(this);
            mWorkingMessage.setConversation(getConversation());
            mWorkingMessage.setText(mReplyEditor.getText());
            
            //add for gemini TODO
            if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                mWorkingMessage.sendGemini(mSelectedSimId);
            } else {
                mWorkingMessage.send("");
            }
            mSentMessage = true;
            mSendingMessage = true;
            mWaitingForSendMessage = true;
        }
    }
    
    private void loadMmsContents() {
        Xlog.d(TAG, "DialogModeActivity.loadMmsContents");
        
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return;
        }

        if (!mCursor.moveToFirst()) {
            Xlog.d(TAG, "moveToFirst fail");
            return;
        }

        Xlog.d(TAG, "cursor ok");
        try {
            //check msg type
            int type = mCursor.getInt(SMS_TYPE);
            Xlog.d(TAG, "type=" + type);

            if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == type) {
                Xlog.d(TAG, "mms nofity");
                String content;
                content = getNotificationContentString(mCurUri);
                mSmsContentText.setText(content);
                return;
            }

            //get MMS pdu
            PduPersister p = PduPersister.getPduPersister(this);
            MultimediaMessagePdu msg;
            SlideshowModel slideshow;
            
            try {
                msg = (MultimediaMessagePdu) p.load(mCurUri);
            } catch (MmsException e) {
                Xlog.d(TAG, e.toString());
                msg = null;
            }
            
            if (msg == null) {
                Xlog.d(TAG, "msg null");
                return;
            }

            //get slideshow
            slideshow = SlideshowModel.createFromPduBody(this, msg.getBody());
            if (slideshow == null)
                Xlog.d(TAG, "slideshow null");
            else
                Xlog.d(TAG, "slideshow ok");

            //set Mms content text
            EncodedStringValue subObj = msg.getSubject();
            String subject = null;
            
            if (subObj != null) {
                subject = subObj.getString();
                Xlog.d(TAG, "sub=" + subject);
            }
            
            SpannableStringBuilder buf = new SpannableStringBuilder();
            boolean hasSubject = false;
            SmileyParser parser = SmileyParser.getInstance();

            //init set a empty string
            buf.append("");
            
            //add subject
            if ((subject != null) && (subject.length() > 0)) {
                hasSubject = true;
                
                CharSequence smilizedSubject = parser.addSmileySpans(subject);

                buf.append(TextUtils.replace(getResources().getString(R.string.inline_subject),
                        new String[] { "%s" }, new CharSequence[] { smilizedSubject }));
                buf.replace(0, buf.length(), parser.addSmileySpans(buf));
            }

            Xlog.d(TAG, "with sub=" + buf.toString());
            
            if (slideshow != null) {
                //append first text to content
                SlideModel slide = slideshow.get(0);
                String body;
                
                if ((slide != null) && slide.hasText()) {
                    TextModel tm = slide.getText();
                    if (tm.isDrmProtected()) {
                        body = getString(R.string.drm_protected_text);
                    } else {
                        body = tm.getText();
                    }
                    Xlog.d(TAG, "body=" + body);

                    if (hasSubject) {
                        buf.append(" - ");
                    }
                    buf.append(parser.addSmileySpans(body));
                }
                else { //First slide  no text
                    if (!hasSubject) {
                        buf.append("        ");
                    }
                }
                Xlog.d(TAG, "with cont=" + buf.toString());
                mSmsContentText.setText(buf);
                
                //Set Mms play button
                boolean needPresent = false;
                for (int i = 0; i < slideshow.size(); i++) {
                    Xlog.d(TAG, "check slide" + i);
                    slide = slideshow.get(i);
                    if (slide.hasImage() || slide.hasVideo() || slide.hasAudio()) {
                        Xlog.d(TAG, "found");
                        needPresent = true;
                        break;
                    }
                }

                if (!needPresent) {
                    if (slideshow.size() > 1)
                        needPresent = true;
                }
                if (needPresent) {
                    try {
                        Xlog.d(TAG, "present slidehsow");
                        Presenter presenter = PresenterFactory.getPresenter(
                                "MmsThumbnailPresenter", this,
                                this, slideshow);
                        presenter.present();
                        mMmsPlayButton.setOnClickListener(this);
                    } catch(Exception e) {
                        Xlog.d(TAG, e.toString());
                        return;
                    }
                }
                else {
                    Xlog.d(TAG, "no media");
                    mMmsView.setVisibility(View.GONE);
                }
            }
            else {
                Xlog.d(TAG, "slideshow null");
                mMmsView.setVisibility(View.GONE);
                if (buf.length() == 0) {
                    mSmsContentText.setText("        ");
                }
            }
        } catch (Exception e) {
            Xlog.d(TAG, e.toString());
        }
    }
    
    private String getNotificationContentString(Uri uri) {
        Xlog.d(TAG, "DialogModeActivity.getNotificationContentString");
        
        PduPersister p = PduPersister.getPduPersister(this);
        NotificationInd msg;
        
        try{
            msg = (NotificationInd) p.load(mCurUri);
        } catch(MmsException e) {
            Xlog.d(TAG, e.toString());
            return "";
        }
        if (msg == null) {
            Xlog.d(TAG, "msg null");
            return "";
        }
        
        String msgSizeText = this.getString(R.string.message_size_label)
                            + String.valueOf((msg.getMessageSize() + 1023) / 1024)
                            + this.getString(R.string.kilobyte);

        String timestamp = this.getString(R.string.expire_on,
                    MessageUtils.formatTimeStampString(this, msg.getExpiry() * 1000L));

        String ret = msgSizeText + "\r\n" + timestamp;
        Xlog.d(TAG, "ret=" + ret);
        
        return ret;
    }
    
    //SlideshowModel mSlideshow;
    private SlideshowModel getSlideShow() {
        Xlog.d(TAG, "DialogModeActivity.getSlideShow ");
        if (mCursor == null) {
            Xlog.d(TAG, "mCursor null");
            return null;
        }

        if (mCursor.moveToFirst()) {
            Xlog.d(TAG, "cursor ok");
            try {
                PduPersister p = PduPersister.getPduPersister(this);
                int type = mCursor.getInt(SMS_TYPE);
                Xlog.d(TAG, "type=" + type);

                if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == type) {
                    Xlog.d(TAG, "mms nofity");
                    return null;
                }

                MultimediaMessagePdu msg;
                try {
                    msg = (MultimediaMessagePdu) p.load(mCurUri);
                } catch(MmsException e) {
                    Xlog.d(TAG, e.toString());
                    msg = null;
                }
                
                if (msg != null) {
                    SlideshowModel slideshow = SlideshowModel.createFromPduBody(this, msg.getBody());
                    if (slideshow == null)
                        Xlog.d(TAG, "slideshow null");
                    else
                        Xlog.d(TAG, "slideshow ok");
                    return slideshow;
                }
                Xlog.d(TAG, "msg null");
            } catch (Exception e) {
                Xlog.d(TAG, e.toString());
            }
			return null;
		}
        else {
            Xlog.d(TAG, "moveToFirst fail");
            return null;
        }
    }

    @Override
    public void startAudio() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.startAudio");
    }

    @Override
    public void startVideo() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.startVideo");
    }

    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setAudio");
    }

    @Override
    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setTextVisibility");
    }

    @Override
    public void setText(String name, String text) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setText");
    }
    
    @Override
    public void setImage(String name, Bitmap bitmap) {
        Xlog.d(TAG, "DialogModeActivity.setImage " + name);
        //inflateMmsView();

        try {
            if (null == bitmap) {
                Xlog.d(TAG, "bitmap null");
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            Xlog.d(TAG, "set bitmap to mMmsImageView");
            mMmsImageView.setImageBitmap(bitmap);
            mMmsView.setVisibility(View.VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Xlog.d(TAG, "setImage: out of memory:" + e.toString());
        }
    }

    @Override
    public void setImage(Uri mUri) {
        Xlog.d(TAG, "DialogModeActivity.setImage(uri) ");
        try {
            Bitmap bitmap = null;
            if (null == mUri) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_missing_thumbnail_picture);
            } else {
                String mScheme = mUri.getScheme();
                InputStream mInputStream = null;
                try {
                    mInputStream = getApplicationContext().getContentResolver().openInputStream(mUri);
                    if (mInputStream != null) {
                        bitmap = BitmapFactory.decodeStream(mInputStream);
                    }
                } catch (FileNotFoundException e) {
                    bitmap = null;
                } finally {
                    if (mInputStream != null) {
                        mInputStream.close();
                    }
                }
                setImage("", bitmap);
            }
        } catch (java.lang.OutOfMemoryError e) {
            Xlog.d(TAG, "setImage(Uri): out of memory: ", e);
        } catch (Exception e) {
            Xlog.d(TAG, "setImage(uri) error." + e);
        }
    }
    
    @Override
    public void reset() {
        Xlog.d(TAG, "DialogModeActivity.reset");

        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
    }

    
    @Override
    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setVisibility");
        mMmsView.setVisibility(View.VISIBLE);
    }

    @Override
    public void pauseAudio() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.pauseAudio");

    }

    @Override
    public void pauseVideo() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.pauseVideo");

    }

    @Override
    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.seekAudio");

    }

    @Override
    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.seekVideo");

    }

    
    @Override
    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setVideoVisibility");
    }

    @Override
    public void stopAudio() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.stopAudio");
    }

    @Override
    public void stopVideo() {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.stopVideo");
    }

    @Override
    public void setVideo(String name, Uri video) {
        Xlog.d(TAG, "DialogModeActivity.setVideo");
        //inflateMmsView();

        try {
            Bitmap bitmap = VideoAttachmentView.createVideoThumbnail(this, video);
            if (null == bitmap) {
                Xlog.d(TAG, "bitmap null");
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_video);
            }
            Xlog.d(TAG, "set bitmap to mMmsImageView");
            mMmsImageView.setImageBitmap(bitmap);
            mMmsView.setVisibility(View.VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Xlog.d(TAG, "setImage: out of memory:" + e.toString());
        }
    }

    @Override
    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setImageRegionFit");
    }

    @Override
    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
        Xlog.d(TAG, "DialogModeActivity.setImageVisibility");
        mMmsView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getWidth() {
        Xlog.d(TAG, "DialogModeActivity.getWidth" + mMmsImageView.getWidth());
        return mMmsImageView.getWidth();
    }
    
    @Override
    public int getHeight() {
        Xlog.d(TAG, "DialogModeActivity.getHeight" + mMmsImageView.getHeight());
        return mMmsImageView.getHeight();
    }

    
    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Xlog.d(TAG, "mTextEditorWatcher.onTextChanged");
            //mWorkingMessage.setText(s);
            updateSendButtonState();
            updateCounter(s, start, before, count);
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private void updateSendButtonState() {
        boolean enable = false;
        int len = mReplyEditor.getText().toString().length();

        Xlog.d(TAG, "DialogModeActivity.updateSendButtonState(): len = " + len);

        if (mSendButton != null) {
            if (len > 0) {
                Xlog.d(TAG, "updateSendButtonState(): mSimCount = " + mSimCount);

                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    if (isAnySimInsert()) { // check SIM state
                        enable = true;
                    }
                }
            }

            //View sendButton = showSmsOrMmsSendButton(mWorkingMessage.requiresMms());
            mSendButton.setEnabled(enable);
            mSendButton.setFocusable(enable);
        }
    }

    private boolean isAnySimInsert() {
        Xlog.d(TAG, "DialogModeActivity.isAnySimInsert,mSimCount=" + mSimCount);
        if (mSimCount > 0) {
            return true;
        }
        return false;
    }

    private int mSimCount;
    private List<SIMInfo> mSimInfoList;
    
    private void getSimInfoList() {
        Xlog.d(TAG, "DialogModeActivity.getSimInfoList");
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Xlog.d(TAG, "GEMINI");
            mSimInfoList = SIMInfo.getInsertedSIMList(this);
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
            Xlog.d(TAG, "ComposeMessageActivity.getSimInfoList(): mSimCount = " + mSimCount);
        } else { // single SIM
            Xlog.d(TAG, "non GEMINI");
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                try {
                    mSimCount = phone.isSimInsert(0) ? 1: 0;
                    Xlog.d(TAG, "sim count=" + mSimCount);
                } catch (RemoteException e) {
                    Xlog.e(TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }
    }

    private void updateCounter(CharSequence text, int start, int before, int count) {
        Xlog.d(TAG, "DialogModeActivity.updateCounter");
        
    	int[] params = null;
    	String optr = SystemProperties.get("ro.operator.optr");
    	if ("OP03".equals(optr)) {
    		int encodingType = SmsMessage.ENCODING_UNKNOWN;
    	    encodingType = MessageUtils.getSmsEncodingType(this);
    	    params = SmsMessage.calculateLength(text, false, encodingType);
    	} else {
    		params = SmsMessage.calculateLength(text, false);
    	}
      /* SmsMessage.calculateLength returns an int[4] with:
             *   int[0] being the number of SMS's required,
             *   int[1] the number of code units used,
             *   int[2] is the number of code units remaining until the next message.
             *   int[3] is the encoding type that should be used for the message.
             */
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
        int unitesUsed = params[1];
        
        //mWorkingMessage.setLengthRequiresMms(
        //    msgCount >= MmsConfig.getSmsToMmsTextThreshold(), true);
        // Show the counter
        // Update the remaining characters and number of messages required.
        //if (mWorkingMessage.requiresMms()) {
        //    mTextCounter.setVisibility(View.GONE);
        //} else {
        //    mTextCounter.setVisibility(View.VISIBLE);
        //}
        String counterText = remainingInCurrentMessage + "/" + msgCount;
        Xlog.d(TAG, "counterText=" + counterText);
        mTextCounter.setText(counterText);
//m1
    }

    class TextLengthFilter implements InputFilter {
        private Toast mExceedMessageSizeToast = null;
        private int mMaxLength;
        
        public TextLengthFilter(int max) {
            mMaxLength = max - 1;
            mExceedMessageSizeToast = Toast.makeText(DialogModeActivity.this, R.string.exceed_message_size_limitation,
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
    }

    private void markAsRead(final Uri uri) {
        Xlog.d(TAG, "DialogModeActivity.markAsRead, " + uri.toString());
        
        new Thread(new Runnable() {
            public void run() {
                final ContentValues values = new ContentValues(2);
                values.put("read", 1);
                values.put("seen", 1);
                SqliteWrapper.update(getApplicationContext(), getContentResolver(), uri, values, null, null);
            }
        }).start();
        MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, false, false);
        removeCurMsg();
    }

    public class DialogModeReceiver extends BroadcastReceiver {

    	private static final String MSG_VIEWED_ACTION = "com.android.mms.dialogmode.VIEWED";
        
    	public void onReceive(Context context, Intent intent) {
    	    Xlog.d(TAG, "DialogModeActivity.DialogModeReceiver.onReceive");
            
    		// TODO Auto-generated method stub
    		if (intent != null)
    		{
    		    String action = intent.getAction();
    		    if (action == null)
    		        return;
                    Xlog.d(TAG, "action=" + action);
                    DialogModeActivity.this.finish();
    		}
    	}
}
}

