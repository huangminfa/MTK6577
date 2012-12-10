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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.MailTo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.ContactsContract.Profile;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.SmileyParser;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.PduHeaders;

//a0
import android.os.SystemProperties;
import android.provider.Telephony.Mms;
import android.provider.Telephony.SIMInfo;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.Spannable;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import com.android.mms.MmsConfig;
//add for gemini
import android.database.Cursor;
import com.google.android.mms.util.SqliteWrapper;
import android.content.ContentValues;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.Phone;
import android.telephony.gemini.GeminiSmsManager;
import android.util.Log;
import android.provider.Telephony.TextBasedSmsColumns;
import com.android.internal.telephony.TelephonyProperties;
import android.drm.DrmManagerClient;
import com.mediatek.xlog.Xlog;
//a1

/**
 * This class provides view of a message in the messages list.
 */
public class MessageListItem extends LinearLayout implements
        SlideViewInterface, OnClickListener {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";

    private static final String TAG = "MessageListItem";
    private static final String M_TAG = "Mms/MessageListItem";
    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    static final int MSG_LIST_EDIT_MMS   = 1;
    static final int MSG_LIST_EDIT_SMS   = 2;
    private static final int PADDING_LEFT_THR = 3;
    private static final int PADDING_LEFT_TWE = 13;
    
    private View mMmsView;
    // add for vcard
    private View mFileAttachmentView;
    private ImageView mImageView;
    private ImageView mLockedIndicator;
    private ImageView mDeliveredIndicator;
    private ImageView mDetailsIndicator;
    private ImageButton mSlideShowButton;
    private TextView mBodyTextView;
    private Button mDownloadButton;
    private TextView mDownloadingLabel;
    private Handler mHandler;
    private MessageItem mMessageItem;
    private String mDefaultCountryIso;
    private TextView mDateView;
    public View mMessageBlock;
    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    private QuickContactDivot mAvatar;
    private boolean mIsLastItemInList;
    static private Drawable sDefaultContactImage;

    public MessageListItem(Context context) {
        super(context);
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
        mDefaultCountryIso = MmsApp.getApplication().getCurrentCountryIso();

        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBodyTextView = (TextView) findViewById(R.id.text_view);
        mDateView = (TextView) findViewById(R.id.date_view);
        //a0
        mSimStatus = (TextView) findViewById(R.id.sim_status);
        //a1
        mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        mDeliveredIndicator = (ImageView) findViewById(R.id.delivered_indicator);
        mDetailsIndicator = (ImageView) findViewById(R.id.details_indicator);
        mAvatar = (QuickContactDivot) findViewById(R.id.avatar);
        mMessageBlock = findViewById(R.id.message_block);
        //a0
        //add for multi-delete
        mSelectedBox = (CheckBox)findViewById(R.id.select_check_box);
        //a1
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.v(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        //unbind();
    }

    public void bind(MessageItem msgItem, boolean isLastItem, boolean isDeleteMode) {
        //a0
        Xlog.i(TAG, "MessageListItem.bind() : msgItem.mSimId = " + msgItem.mSimId);
        //a1
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;
        
        //a0
        setSelectedBackGroud(false);
        if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            if (msgItem.isSelected()) {
                setSelectedBackGroud(true);
            }
        } else {
            mSelectedBox.setVisibility(View.GONE);
        }
        //a0

        setLongClickable(false);
        //set item these two false can make listview always get click event.
        setFocusable(false);
        setClickable(false);
        switch (msgItem.mMessageType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                bindNotifInd(msgItem);
                break;
            default:
                bindCommonMessage(msgItem);
                break;
        }
    }

    public void unbind() {
        // Clear all references to the message item, which can contain attachments and other
        // memory-intensive objects
        mMessageItem = null;
        if (mImageView != null) {
            // Because #setOnClickListener may have set the listener to an object that has the
            // message item in its closure.
            mImageView.setOnClickListener(null);
        }
        if (mSlideShowButton != null) {
            // Because #drawPlaybackButton sets the tag to mMessageItem
            mSlideShowButton.setTag(null);
        }
    }

    public MessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }

    private void bindNotifInd(final MessageItem msgItem) {
        hideMmsViewIfNeeded();
        // add for vcard
        hideFileAttachmentViewIfNeeded();

        String msgSizeText = mContext.getString(R.string.message_size_label)
                                + String.valueOf((msgItem.mMessageSize + 1023) / 1024)
                                + mContext.getString(R.string.kilobyte);
        mBodyTextView.setVisibility(View.VISIBLE);
        mBodyTextView.setText(formatMessage(msgItem, msgItem.mContact, null, msgItem.mSubject,
                                            msgItem.mHighlight, msgItem.mTextContentType));
        mDateView.setVisibility(View.VISIBLE);
        mDateView.setText(msgSizeText + " " + msgItem.mTimestamp);
        //a0
        mSimStatus.setVisibility(View.VISIBLE);
        mSimStatus.setText(formatSimStatus(msgItem));
        //a1

        int state = DownloadManager.getInstance().getState(msgItem.mMessageUri);
        switch (state) {
            case DownloadManager.STATE_DOWNLOADING:
                inflateDownloadControls();
                mDownloadingLabel.setVisibility(View.VISIBLE);
                mDownloadButton.setVisibility(View.GONE);
                //a0
                findViewById(R.id.text_view).setVisibility(GONE);
                //a1
                break;
            case DownloadManager.STATE_UNSTARTED:
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            default:
                setLongClickable(true);
                inflateDownloadControls();
                mDownloadingLabel.setVisibility(View.GONE);
                mDownloadButton.setVisibility(View.VISIBLE);
                //a0
                findViewById(R.id.text_view).setVisibility(GONE);
                //a1
                mDownloadButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //a0
                        //add for multi-delete
                        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                            return;
                        }

                        // add for gemini
                        int simId = 0;
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                            // get sim id by uri
                            Cursor cursor = SqliteWrapper.query(msgItem.mContext, msgItem.mContext.getContentResolver(),
                                msgItem.mMessageUri, new String[] { Mms.SIM_ID }, null, null, null);
                            if (cursor != null) {
                                try {
                                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                                        simId = cursor.getInt(0);
                                    }
                                } finally {
                                    cursor.close();
                                }
                            }
                        }
                        
                        String optr = SystemProperties.get("ro.operator.optr");
                        //MTK_OP01_PROTECT_START
                        if (null != optr && optr.equals("OP01")) {
                            // check device memory status
                            if (MmsConfig.getDeviceStorageFullStatus()) {
                                MmsApp.getToastHandler().sendEmptyMessage(MmsApp.MSG_RETRIEVE_FAILURE_DEVICE_MEMORY_FULL);
                                return;
                            }
                        }
                        //MTK_OP01_PROTECT_END
                        //a1
                        mDownloadingLabel.setVisibility(View.VISIBLE);
                        mDownloadButton.setVisibility(View.GONE);
                        Intent intent = new Intent(mContext, TransactionService.class);
                        intent.putExtra(TransactionBundle.URI, msgItem.mMessageUri.toString());
                        intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                Transaction.RETRIEVE_TRANSACTION);
                        //a0
                        // add for gemini
                        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
                        //a1
                        mContext.startService(intent);
                    }
                });
                //mtk81083 this is a google default bug. it has no this code!
                // When we show the mDownloadButton, this list item's onItemClickListener doesn't
                // get called. (It gets set in ComposeMessageActivity:
                // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                // onClickListener. It allows the item to respond to embedded html links and at the
                // same time, allows the button to work.
                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMessageListItemClick();
                    }
                });
                break;
        }

        // Hide the indicators.
        //m0
        //mLockedIndicator.setVisibility(View.GONE);
        if (msgItem.mLocked) {
            mLockedIndicator.setImageResource(R.drawable.ic_lock_message_sms);          
            mLockedIndicator.setVisibility(View.VISIBLE);
            mSimStatus.setPadding(PADDING_LEFT_THR, 0, 0, 0);
        } else {
            mLockedIndicator.setVisibility(View.GONE);
            mSimStatus.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
        }
        //m1
        mDeliveredIndicator.setVisibility(View.GONE);
        mDetailsIndicator.setVisibility(View.GONE);
        updateAvatarView(msgItem.mAddress, false);
    }

    private Toast mInvalidContactToast;
    private boolean exists =false;
    private boolean selfExists() {
        exists = false;
        final Object dbQueryLock = new Object();
        new Thread(new Runnable() {
            public void run() {
                Cursor c = null;
                try {
                    c = mContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
                    exists = c != null && c.moveToFirst();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
                synchronized (dbQueryLock) {
                    dbQueryLock.notify();
                }
            }
        }).start();
        synchronized (dbQueryLock) {
            try {
                dbQueryLock.wait(100);
            } catch(InterruptedException e) {
                //time out
            }
            return exists;
        }
    }

    private void updateAvatarView(String addr, boolean isSelf) {
        Drawable avatarDrawable;
        if (isSelf || !TextUtils.isEmpty(addr)) {
            Contact contact = isSelf ? Contact.getMe(false) : Contact.get(addr, false);
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage);

            if (isSelf) {
                if (selfExists()) {
                    mAvatar.assignContactUri(Profile.CONTENT_URI);
                } else {
                    mAvatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mInvalidContactToast == null) {
                                mInvalidContactToast = Toast.makeText(mContext, R.string.invalid_contact_message, Toast.LENGTH_SHORT);
                            }
                            mInvalidContactToast.show();
                        }
                    });
                }
            } else {
                String number = contact.getNumber();
                if (Mms.isEmailAddress(number)) {
                    mAvatar.assignContactFromEmail(number, true);
                } else {
                    mAvatar.assignContactFromPhone(number, true);
                }
            }
        } else {
            avatarDrawable = sDefaultContactImage;
        }
        mAvatar.setImageDrawable(avatarDrawable);
    }

    private void bindCommonMessage(final MessageItem msgItem) {
        if (mDownloadButton != null) {
            mDownloadButton.setVisibility(View.GONE);
            mDownloadingLabel.setVisibility(View.GONE);
            //a0
            mBodyTextView.setVisibility(View.VISIBLE);
            //a1
        }
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        boolean isSelf = Sms.isOutgoingFolder(msgItem.mBoxId);
        String addr = isSelf ? null : msgItem.mAddress;
        updateAvatarView(addr, isSelf);

        // Get and/or lazily set the formatted message from/on the
        // MessageItem.  Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        //a0
        CharSequence formattedTimestamp = msgItem.getCachedFormattedTimestamp();
        CharSequence formattedSimStatus= msgItem.getCachedFormattedTimestamp();
        //a1
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem, msgItem.mContact, msgItem.mBody,
                                             msgItem.mSubject,
                                             msgItem.mHighlight, msgItem.mTextContentType);
            //a0
            formattedTimestamp = formatTimestamp(msgItem, msgItem.mTimestamp);
            formattedSimStatus = formatSimStatus(msgItem);
            //a1
        }
        mBodyTextView.setText(formattedMessage);

        // If we're in the process of sending a message (i.e. pending), then we show a "SENDING..."
        // string in place of the timestamp.
        //m0
        /*mDateView.setText(msgItem.isSending() ?
                mContext.getResources().getString(R.string.sending_message) :
                    msgItem.mTimestamp);
        */
        if (msgItem.isFailedMessage() || (!msgItem.isSending() && TextUtils.isEmpty(msgItem.mTimestamp))) {
            mDateView.setVisibility(View.GONE);
        } else {
            mDateView.setVisibility(View.VISIBLE);
            mDateView.setText(msgItem.isSending() ?
                mContext.getResources().getString(R.string.sending_message) :
                    msgItem.mTimestamp);
        }
        //m1
        
        //a0
        if (!msgItem.isSimMsg() && !TextUtils.isEmpty(formattedSimStatus)) {
            mSimStatus.setVisibility(View.VISIBLE);
            mSimStatus.setText(formattedSimStatus);
        } else {
            mSimStatus.setVisibility(View.GONE);
        }
        //a1

        if (msgItem.isSms()) {
            hideMmsViewIfNeeded();
            // add for vcard
            hideFileAttachmentViewIfNeeded();
        } else {
            Presenter presenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext,
                    this, msgItem.mSlideshow);
            presenter.present();

            if (msgItem.mAttachmentType != WorkingMessage.TEXT) {
                if (msgItem.mAttachmentType == WorkingMessage.ATTACHMENT) {
                    // show file attachment view
                    hideMmsViewIfNeeded();
                    showFileAttachmentView(msgItem.mSlideshow.getAttachFiles());
                } else {
                    hideFileAttachmentViewIfNeeded();
                    inflateMmsView();
                    mMmsView.setVisibility(View.VISIBLE);
                    drawPlaybackButton(msgItem);
                    if (mSlideShowButton.getVisibility() == View.GONE) {
                        setMediaOnClickListener(msgItem);
                    }
                }
            } else {
                hideMmsViewIfNeeded();
                // add for vcard
                hideFileAttachmentViewIfNeeded();
            }
        }
        drawRightStatusIndicator(msgItem);

        requestLayout();
    }

    private void hideMmsViewIfNeeded() {
        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
    }

    @Override
    public void startAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void startVideo() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setImage(String name, Bitmap bitmap) {
        inflateMmsView();

        try {
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }
    
    @Override
    public void setImage(Uri mUri) {
        try {
            Bitmap bitmap = null;
            if (null == mUri) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_missing_thumbnail_picture);
            } else {
                String mScheme = mUri.getScheme();
                InputStream mInputStream = null;
                try {
                    mInputStream = this.getContext().getContentResolver().openInputStream(mUri);
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
            Log.e(TAG, "setImage(Uri): out of memory: ", e);
        } catch (Exception e) {
            Log.e(TAG, "setImage(uri) error." + e);
        }
    }

    private void inflateMmsView() {
        if (mMmsView == null) {
            //inflate the surrounding view_stub
            findViewById(R.id.mms_layout_view_stub).setVisibility(VISIBLE);

            mMmsView = findViewById(R.id.mms_view);
            mImageView = (ImageView) findViewById(R.id.image_view);
            mSlideShowButton = (ImageButton) findViewById(R.id.play_slideshow_button);
        }
    }
    
    // Add for vCard begin
    private void hideFileAttachmentViewIfNeeded() {
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
    }

    private void importVCard(FileAttachmentModel attach) {
        final String[] filenames = mContext.fileList();
        for (String file : filenames) {
            if (file.endsWith(".vcf")) {
                mContext.deleteFile(file);
            }
        }
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = mContext.getContentResolver().openInputStream(attach.getUri());
                out = mContext.openFileOutput(attach.getSrc(), Context.MODE_WORLD_READABLE);
                byte[] buf = new byte[8096];
                int seg = 0;
                while ((seg = in.read(buf)) != -1) {
                    out.write(buf, 0, seg);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            Xlog.e(TAG, "importVCard, file not found " + attach + ", exception ", e);
        } catch (IOException e) {
            Xlog.e(TAG, "importVCard, ioexception " + attach + ", exception ", e);
        } catch (Exception e) {
            Xlog.e(TAG, "importVCard, unknown errror ", e);
        }
        final File tempVCard = mContext.getFileStreamPath(attach.getSrc());
        if (!tempVCard.exists() || tempVCard.length() <= 0) {
            Xlog.e(TAG, "importVCard, file is not exists or empty " + tempVCard);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(tempVCard), attach.getContentType().toLowerCase());
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(intent);
    }

    private void showFileAttachmentView(ArrayList<FileAttachmentModel> files) {
        // There should be one and only one file
        if (files == null || files.size() < 1) {
            Log.e(TAG, "showFileAttachmentView, oops no attachment files found");
            return;
        }
        if (mFileAttachmentView == null) {
            findViewById(R.id.mms_file_attachment_view_stub).setVisibility(VISIBLE);
            mFileAttachmentView = findViewById(R.id.file_attachment_view);
        }
        mFileAttachmentView.setVisibility(View.VISIBLE);
        final FileAttachmentModel attach = files.get(0);
        mFileAttachmentView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
                    return;
                }
                if (attach.isVCard()) {
                    importVCard(attach);
                } else if (attach.isVCalendar()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(attach.getUri(), attach.getContentType().toLowerCase());
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Xlog.e(TAG, "no activity handle ", e);
                    }
                }
            }
        });
        final ImageView thumb = (ImageView) mFileAttachmentView.findViewById(R.id.file_attachment_thumbnail);
        final TextView name = (TextView) mFileAttachmentView.findViewById(R.id.file_attachment_name_info);
        String nameText = null;
        int thumbResId = -1;
        if (attach.isVCard()) {
            nameText = mContext.getString(R.string.file_attachment_vcard_name, attach.getSrc());
            thumbResId = R.drawable.ic_vcard_attach;
        } else if (attach.isVCalendar()) {
            nameText = mContext.getString(R.string.file_attachment_vcalendar_name, attach.getSrc());
            thumbResId = R.drawable.ic_vcalendar_attach;
        }
        name.setText(nameText);
        thumb.setImageResource(thumbResId);
        final TextView size = (TextView) mFileAttachmentView.findViewById(R.id.file_attachment_size_info);
        size.setText(MessageUtils.getHumanReadableSize(attach.getAttachSize()));
    }
    // Add for vCard end

    private void inflateDownloadControls() {
        if (mDownloadButton == null) {
            //inflate the download controls
            findViewById(R.id.mms_downloading_view_stub).setVisibility(VISIBLE);
            mDownloadButton = (Button) findViewById(R.id.btn_download_msg);
            mDownloadingLabel = (TextView) findViewById(R.id.label_downloading);
        }
    }
    
    private LeadingMarginSpan mLeadingMarginSpan;

    private LineHeightSpan mSpan = new LineHeightSpan() {
        @Override
        public void chooseHeight(CharSequence text, int start,
                int end, int spanstartv, int v, FontMetricsInt fm) {
            fm.ascent -= 10;
        }
    };

    TextAppearanceSpan mTextSmallSpan =
        new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Small);

    ForegroundColorSpan mColorSpan = null;  // set in ctor

    private CharSequence formatMessage(MessageItem msgItem, String contact, String body,
                                       String subject, Pattern highlight,
                                       String contentType) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        boolean hasSubject = !TextUtils.isEmpty(subject);
        SmileyParser parser = SmileyParser.getInstance();
        if (hasSubject) {
            CharSequence smilizedSubject = parser.addSmileySpans(subject);
            // Can't use the normal getString() with extra arguments for string replacement
            // because it doesn't preserve the SpannableText returned by addSmileySpans.
            // We have to manually replace the %s with our text.
            buf.append(TextUtils.replace(mContext.getResources().getString(R.string.inline_subject),
                    new String[] { "%s" }, new CharSequence[] { smilizedSubject }));
            buf.replace(0, buf.length(), parser.addSmileySpans(buf));
        }

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            if (contentType != null && ContentType.TEXT_HTML.equals(contentType)) {
                buf.append("\n");
                buf.append(Html.fromHtml(body));
            } else {
                if (hasSubject) {
                    buf.append(" - ");
                }
                buf.append(parser.addSmileySpans(body));
            }
        }

        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }
        //a0
        buf.setSpan(mLeadingMarginSpan, 0, buf.length(), 0);
        //a1
        return buf;
    }

    private void drawPlaybackButton(MessageItem msgItem) {
        switch (msgItem.mAttachmentType) {
            case WorkingMessage.SLIDESHOW:
            case WorkingMessage.AUDIO:
            case WorkingMessage.VIDEO:
                // Show the 'Play' button and bind message info on it.
                mSlideShowButton.setTag(msgItem);
                //a0
                mSlideShowButton.setVisibility(View.GONE);
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mms_play_btn); 
                if (msgItem.hasDrmContent()) {
                    if (FeatureOption.MTK_DRM_APP) {
                        Xlog.i(TAG," msgItem hasDrmContent"); 
                        Drawable front = mContext.getResources().getDrawable(com.mediatek.internal.R.drawable.drm_red_lock);
                        DrmManagerClient drmManager= new DrmManagerClient(mContext);
                        Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                        mSlideShowButton.setImageBitmap(drmBitmap);
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    } else {
                        Xlog.i(TAG," msgItem hasn't DrmContent");
                        mSlideShowButton.setImageBitmap(bitmap);
                    }
                } else {
                    Xlog.i(TAG," msgItem hasn't DrmContent"); 
                    mSlideShowButton.setImageBitmap(bitmap);
                }
                //a1
                // Set call-back for the 'Play' button.
                mSlideShowButton.setOnClickListener(this);
                mSlideShowButton.setVisibility(View.VISIBLE);
                setLongClickable(true);

                // When we show the mSlideShowButton, this list item's onItemClickListener doesn't
                // get called. (It gets set in ComposeMessageActivity:
                // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                // onClickListener. It allows the item to respond to embedded html links and at the
                // same time, allows the slide show play button to work.
                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMessageListItemClick();
                    }
                });
                break;
            case WorkingMessage.IMAGE:
                if (msgItem.mSlideshow.get(0).hasText()) {
                    Xlog.d(TAG, "msgItem is image and text");
                    mSlideShowButton.setTag(msgItem);
                    mSlideShowButton.setVisibility(View.GONE);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mms_play_btn); 
                    if (msgItem.hasDrmContent()) {
                        if (FeatureOption.MTK_DRM_APP) {
                            Xlog.i(TAG," msgItem hasDrmContent"); 
                            Drawable front = mContext.getResources().getDrawable(com.mediatek.internal.R.drawable.drm_red_lock);
                            DrmManagerClient drmManager= new DrmManagerClient(mContext);
                            Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                            mSlideShowButton.setImageBitmap(drmBitmap);
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                                bitmap = null;
                            }
                        } else {
                            Xlog.i(TAG," msgItem hasn't DrmContent");
                            mSlideShowButton.setImageBitmap(bitmap);
                        }
                    } else {
                        Xlog.i(TAG," msgItem hasn't DrmContent"); 
                        mSlideShowButton.setImageBitmap(bitmap);
                    }
                    // Set call-back for the 'Play' button.
                    mSlideShowButton.setOnClickListener(this);
                    mSlideShowButton.setVisibility(View.VISIBLE);
                    setLongClickable(true);
                    
                    // When we show the mSlideShowButton, this list item's onItemClickListener doesn't
                    // get called. (It gets set in ComposeMessageActivity:
                    // mMsgListView.setOnItemClickListener) Here we explicitly set the item's
                    // onClickListener. It allows the item to respond to embedded html links and at the
                    // same time, allows the slide show play button to work.
                    setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            onMessageListItemClick();
                        }
                    });
                } else {
                    mSlideShowButton.setVisibility(View.GONE);
                }
                break;
            default:
                mSlideShowButton.setVisibility(View.GONE);
                break;
        }
    }

    // OnClick Listener for the playback button
    @Override
    public void onClick(View v) {
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
            return;
        }
        if (sImageButtonCanClick == false) {
            return;
        }
        sImageButtonCanClick = false;
        //a1
        MessageItem mi = (MessageItem) v.getTag();
        if (mi != null) {
            switch (mi.mAttachmentType) {
                case WorkingMessage.VIDEO:
                case WorkingMessage.IMAGE:
                    if (mi.mSlideshow.get(0).hasText()) {
                        MessageUtils.viewMmsMessageAttachmentMini(mContext, mi.mMessageUri, mi.mSlideshow);
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, mi.mMessageUri, mi.mSlideshow);
                    }
                    break;
                case WorkingMessage.AUDIO:
                case WorkingMessage.SLIDESHOW:
                    MessageUtils.viewMmsMessageAttachment(mContext, mi.mMessageUri, null);
                    break;
            }
        } else {
            Xlog.e(TAG, "onClick(): (MessageItem) v.getTag() == null");
        }
        if (mHandler != null) {
            Runnable run = new Runnable() {
                public void run() {
                    sImageButtonCanClick = true;
                }
            };
            mHandler.postDelayed(run, 1000);
        }
    }

    public void onMessageListItemClick() {
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            if (!mSelectedBox.isChecked()) {
                setSelectedBackGroud(true);
            } else {
                setSelectedBackGroud(false);
            }
            if (null != mHandler) {
                Message msg = Message.obtain(mHandler, ITEM_CLICK);
                msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                msg.sendToTarget();
            }
            return;
        }
        //a1
        
        // If the message is a failed one, clicking it should reload it in the compose view,
        // regardless of whether it has links in it
        if (mMessageItem != null &&
                mMessageItem.isOutgoingMessage() &&
                mMessageItem.isFailedMessage() ) {
            recomposeFailedMessage();
            return;
        }

        // Check for links. If none, do nothing; if 1, open it; if >1, ask user to pick one
        URLSpan[] spans = mBodyTextView.getUrls();
        //a0
        final java.util.ArrayList<String> urls = MessageUtils.extractUris(spans);
        final String telPrefix = "tel:";
        String url = ""; 
        for(int i=0;i<spans.length;i++) {
            url = urls.get(i);
            if(url.startsWith(telPrefix)) {
                mIsTel = true;
                urls.add("smsto:"+url.substring(telPrefix.length()));
            }
        }
        //a1

        if (spans.length == 0) {
            // Do nothing.
        //m0
        //} else if (spans.length == 1) {
        } else if (spans.length == 1 && !mIsTel) {
        //m1
            /*
            Uri uri = Uri.parse(spans[0].getURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            mContext.startActivity(intent);
            */
            final String mUriTemp = spans[0].getURL();
            
            //MTK_OP01_PROTECT_START
            String optr = SystemProperties.get("ro.operator.optr");
            if (optr != null && optr.equals("OP01") && (!mUriTemp.startsWith("mailto:"))) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setTitle(com.mediatek.internal.R.string.url_dialog_choice_title);
                b.setMessage(com.mediatek.internal.R.string.url_dialog_choice_message);
                b.setCancelable(true);
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(mUriTemp);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        mContext.startActivity(intent);
                    }
                });
                b.show();
            } else {
            //MTK_OP01_PROTECT_END
                
                Uri uri = Uri.parse(mUriTemp);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                mContext.startActivity(intent);
                
            //MTK_OP01_PROTECT_START
            }
            //MTK_OP01_PROTECT_END
            
        } else {
            //m0
            //final java.util.ArrayList<String> urls = MessageUtils.extractUris(spans);
            //m1
            ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item, urls) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    try {
                        String url = getItem(position).toString();
                        TextView tv = (TextView) v;
                        Drawable d = mContext.getPackageManager().getActivityIcon(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        if (d != null) {
                            d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicHeight());
                            tv.setCompoundDrawablePadding(10);
                            tv.setCompoundDrawables(d, null, null, null);
                        }
                        final String telPrefix = "tel:";
                        //a0
                        final String smsPrefix = "smsto:";
                        //a1
                        if (url.startsWith(telPrefix)) {
                            url = PhoneNumberUtils.formatNumber(
                                            url.substring(telPrefix.length()), mDefaultCountryIso);
                            if (url == null) {
                                Xlog.w(TAG,"url turn to null after calling PhoneNumberUtils.formatNumber");
                                url = getItem(position).toString().substring(telPrefix.length());
                            }
                        }
                        //a0
                        else if (url.startsWith(smsPrefix)) {
                            url = PhoneNumberUtils.formatNumber(
                                            url.substring(smsPrefix.length()), mDefaultCountryIso);
                            if (url == null) {
                                Xlog.w(TAG,"url turn to null after calling PhoneNumberUtils.formatNumber");
                                url = getItem(position).toString().substring(smsPrefix.length());
                            }
                        }
                        final String mailPrefix ="mailto";
                        if(url.startsWith(mailPrefix))
                        {
                            MailTo mt = MailTo.parse(url);
                            url = mt.getTo();
                        }
                        //a1
                        tv.setText(url);
                    } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
                        // it's ok if we're unable to set the drawable for this view - the user
                        // can still use it
                    }
                    return v;
                }
            };

            AlertDialog.Builder b = new AlertDialog.Builder(mContext);

            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialog, int which) {
                    if (which >= 0) {
                        Uri uri = Uri.parse(urls.get(which));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
                        //a0
                        if (urls.get(which).startsWith("smsto:")) {
                            intent.setClassName(mContext, "com.android.mms.ui.SendMessageToActivity");
                        }
                        //a1
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        mContext.startActivity(intent);
                    }
                    dialog.dismiss();
                }
            };

            b.setTitle(R.string.select_link_title);
            b.setCancelable(true);
            b.setAdapter(adapter, click);

            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            b.show();
        }
    }

    public static  boolean sImageButtonCanClick = true;// this is a hack for quick click.
    private void setMediaOnClickListener(final MessageItem msgItem) {
        switch(msgItem.mAttachmentType) {
        case WorkingMessage.IMAGE:
        case WorkingMessage.VIDEO:
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //a0
                    //add for multi-delete
                    if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
                        mSelectedBox.setChecked(!mSelectedBox.isChecked()); 

                        if (mSelectedBox.isChecked()) {
                            setSelectedBackGroud(true);
                        } else {
                            setSelectedBackGroud(false);
                        }

                        if (null != mHandler) {
                            Message msg = Message.obtain(mHandler, ITEM_CLICK);
                            msg.arg1 = (int)(mMessageItem.mType.equals("mms")? -mMessageItem.mMsgId : mMessageItem.mMsgId);
                            msg.sendToTarget();
                        }
                        return;
                    }
                    //a1
                    if (sImageButtonCanClick == false) {
                        return;
                    }
                    sImageButtonCanClick = false;
                    //m0
                    //MessageUtils.viewMmsMessageAttachment(mContext, null, msgItem.mSlideshow);
                    if (msgItem.mAttachmentType == WorkingMessage.IMAGE && msgItem.mSlideshow.get(0).hasText()) {
                        mImageView.setOnClickListener(null);
                    } else {
                        MessageUtils.viewMmsMessageAttachment(mContext, msgItem.mMessageUri, msgItem.mSlideshow);
                    }
                    if (mHandler != null) {
                        Runnable run = new Runnable() {
                            public void run() {
                                sImageButtonCanClick = true;
                            }
                        };
                        mHandler.postDelayed(run, 1000);
                    }
                    //m1
                }
            });
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return v.showContextMenu();
                }
            });
            break;

        default:
            mImageView.setOnClickListener(null);
            break;
        }
    }

    private long handlerTime = 0;
    /**
     * Assuming the current message is a failed one, reload it into the compose view so that the
     * user can resend it.
     */
    private void recomposeFailedMessage() {
        String type = mMessageItem.mType;
        final int what;
        if (type.equals("sms")) {
            what = MSG_LIST_EDIT_SMS;
        } else {
            what = MSG_LIST_EDIT_MMS;
        }
        //a0
        //add for multi-delete
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {         
            return;
        }
        //a1
        if (handlerTime != 0) {
            long currentTime = System.currentTimeMillis();
            Xlog.d(M_TAG, "recomposeFailedMessage(): coming one click. currentTime=" + currentTime + ", handlerTime=" + handlerTime);
            Xlog.d(M_TAG, "recomposeFailedMessage(): currentTime - handlerTime=" + (currentTime - handlerTime));
            if ((currentTime - handlerTime) < 1000) {
                Xlog.d(M_TAG, "recomposeFailedMessage(): cancel one click");
                handlerTime = currentTime;
                return;
            }
        }
        handlerTime = System.currentTimeMillis();

        if (null != mHandler) {
            Xlog.d(M_TAG, "recomposeFailedMessage(): sending one message");
            Message msg = Message.obtain(mHandler, what);
            msg.obj = new Long(mMessageItem.mMsgId);
            msg.sendToTarget();
        }
    }

    private void drawRightStatusIndicator(MessageItem msgItem) {
        // Locked icon
        if (msgItem.mLocked) {
            mLockedIndicator.setImageResource(R.drawable.ic_lock_message_sms);
            mLockedIndicator.setVisibility(View.VISIBLE);
            mSimStatus.setPadding(PADDING_LEFT_THR, 0, 0, 0);
        } else {
            mLockedIndicator.setVisibility(View.GONE);
            mSimStatus.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
            
        }

        // Delivery icon - we can show a failed icon for both sms and mms, but for an actual
        // delivery, we only show the icon for sms. We don't have the information here in mms to
        // know whether the message has been delivered. For mms, msgItem.mDeliveryStatus set
        // to MessageItem.DeliveryStatus.RECEIVED simply means the setting requesting a
        // delivery report was turned on when the message was sent. Yes, it's confusing!
        if ((msgItem.isOutgoingMessage() && msgItem.isFailedMessage()) ||
                msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
            mDeliveredIndicator.setImageResource(R.drawable.ic_list_alert_sms_failed);
            mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else if (msgItem.isSms() &&
                msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED) {
            //a0
            mDeliveredIndicator.setClickable(false);
            //a1
            mDeliveredIndicator.setImageResource(R.drawable.ic_sms_mms_delivered);
            mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else {
            mDeliveredIndicator.setVisibility(View.GONE);
        }

        // Message details icon - this icon is shown both for sms and mms messages. For mms,
        // we show the icon if the read report or delivery report setting was set when the
        // message was sent. Showing the icon tells the user there's more information
        // by selecting the "View report" menu.
        if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.INFO || msgItem.mReadReport
                || (msgItem.isMms() &&
                        msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED)) {
            mDetailsIndicator.setImageResource(R.drawable.ic_sms_mms_details);
            mDetailsIndicator.setVisibility(View.VISIBLE);
        } else {
            mDetailsIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setText(String name, String text) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setVideo(String name, Uri video) {
        inflateMmsView();

        try {
            Bitmap bitmap = VideoAttachmentView.createVideoThumbnail(mContext, video);
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_video);
            }
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }
    }

    @Override
    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopAudio() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopVideo() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reset() {
        if (mImageView != null) {
            mImageView.setVisibility(GONE);
        }
    }

    @Override
    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

    }

    /**
     * Override dispatchDraw so that we can put our own background and border in.
     * This is all complexity to support a shared border from one item to the next.
     */
    @Override
    public void dispatchDraw(Canvas c) {
        View v = mMessageBlock;
        int selectBoxWidth = 0;
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            selectBoxWidth = mSelectedBox.getWidth();
        }
        if (v != null) {
            float l = v.getX() + selectBoxWidth;
            float t = v.getY();
            float r = v.getX() + v.getWidth() + selectBoxWidth;
            float b = v.getY() + v.getHeight();

            Path path = mPath;
            path.reset();

            super.dispatchDraw(c);

            path.reset();

            r -= 1;

            // This block of code draws the border around the "message block" section
            // of the layout.  This would normally be a simple rectangle but we omit
            // the border at the point of the avatar's divot.  Also, the bottom is drawn
            // 1 pixel below our own bounds to get it to line up with the border of
            // the next item.
            //
            // But for the last item we draw the bottom in our own bounds -- so it will
            // show up.
            if (mIsLastItemInList) {
                b -= 1;
            }
            if (mAvatar.getPosition() == Divot.RIGHT_UPPER) {
                path.moveTo(l, t + mAvatar.getCloseOffset());
                path.lineTo(l, t);
                if (selectBoxWidth > 0) {
                    path.lineTo(l - mAvatar.getWidth() - selectBoxWidth, t);
                }
                path.lineTo(r, t);
                path.lineTo(r, b);
                path.lineTo(l, b);
                path.lineTo(l, t + mAvatar.getFarOffset());
            } else if (mAvatar.getPosition() == Divot.LEFT_UPPER) {
                path.moveTo(r, t + mAvatar.getCloseOffset());
                path.lineTo(r, t);
                path.lineTo(l - selectBoxWidth, t);
                path.lineTo(l - selectBoxWidth, b);
                path.lineTo(r, b);
                path.lineTo(r, t + mAvatar.getFarOffset());
            }

            Paint paint = mPaint;
//            paint.setColor(0xff00ff00);
            paint.setColor(0xffcccccc);
            paint.setStrokeWidth(1F);
            paint.setStyle(Paint.Style.STROKE);
            c.drawPath(path, paint);
        } else {
            super.dispatchDraw(c);
        }
    }
    
    //a0
    static final int ITEM_CLICK          = 5;
    static final int ITEM_MARGIN         = 50;
    private TextView mSimStatus;
    public CheckBox mSelectedBox;
    private boolean mIsTel = false;
    
    private CharSequence formatTimestamp(MessageItem msgItem, String timestamp) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        if (msgItem.isSending()) {
            timestamp = mContext.getResources().getString(R.string.sending_message);
        }

           buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);        
           buf.setSpan(mSpan, 1, buf.length(), 0);
           
        //buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buf;
    }

    private CharSequence formatSimStatus(MessageItem msgItem ) {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        // If we're in the process of sending a message (i.e. pending), then we show a "Sending..."
        // string in place of the timestamp.
        //Add sim info
        int simInfoStart = buffer.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, msgItem.mSimId);
        if(simInfo.length() > 0){
            if (msgItem.mBoxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
                buffer.append(" ");
                buffer.append(mContext.getString(R.string.via_without_time_for_recieve));
            } else {
                buffer.append(" ");
                buffer.append(mContext.getString(R.string.via_without_time_for_send));
            }
            simInfoStart = buffer.length();
            buffer.append(" ");
            buffer.append(simInfo);
            buffer.append(" ");
        }

        //buffer.setSpan(mTextSmallSpan, 0, buffer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buffer.setSpan(mColorSpan, 0, simInfoStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return buffer;
    }
    
    public void setSelectedBackGroud(boolean selected) {
        if (selected) {
            mSelectedBox.setChecked(true);
            mSelectedBox.setBackgroundDrawable(null);
            mMessageBlock.setBackgroundDrawable(null);
            mDateView.setBackgroundDrawable(null);
            setBackgroundResource(R.drawable.list_selected_holo_light);
        } else {
            setBackgroundDrawable(null);
            mSelectedBox.setChecked(false);
            mSelectedBox.setBackgroundResource(R.drawable.listitem_background);
            mMessageBlock.setBackgroundResource(R.drawable.listitem_background);
            mDateView.setBackgroundResource(R.drawable.listitem_background);
        }
    }

    public void bindDefault(boolean isLastItem) {
        Xlog.d(M_TAG, "bindDefault()");
        mIsLastItemInList = isLastItem;
        mSelectedBox.setVisibility(View.GONE);
        setLongClickable(false);
        setFocusable(false);
        setClickable(false);

        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
        mBodyTextView.setText(R.string.refreshing);

        mDateView.setVisibility(View.GONE);
        mSimStatus.setVisibility(View.GONE);
        if (mDownloadButton != null) {
            mDownloadingLabel.setVisibility(View.GONE);
            mDownloadButton.setVisibility(View.GONE);
        }
        mLockedIndicator.setVisibility(View.GONE);
        mSimStatus.setPadding(PADDING_LEFT_TWE, 0, 0, 0);
        mDeliveredIndicator.setVisibility(View.GONE);
        mDetailsIndicator.setVisibility(View.GONE);
        mAvatar.setImageDrawable(sDefaultContactImage);

        requestLayout();
    }
    //a1
    
//MTK_OP01_PROTECT_START    
    public void setBodyTextSize(float size){        
        if(mBodyTextView != null && mBodyTextView.getVisibility() == View.VISIBLE){
            mBodyTextView.setTextSize(size);
        }
    }
//MTK_OP01_PROTECT_END
    
}
