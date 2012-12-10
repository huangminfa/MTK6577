
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Contact;
import com.mediatek.nfc.tag.utils.Utils;

import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SMSRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/SMSRecord";

    private static final int REQUEST_CODE_CONTACT = 1;

    private static final int REQUEST_CODE_ATTACH_IMAGE = 11;

    private static final int REQUEST_CODE_ATTACH_AUDIO = 12;

    private static final int REQUEST_CODE_ATTACH_RINGTONE = 13;

    private static final int REQUEST_CODE_ATTACH_UNKNOWN = 29;

    private static final int MENU_ADD_SUBJECT = Menu.FIRST;

    private static final int MENU_ADD_ATTACH = Menu.FIRST + 1;

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // TODO after user modify receiver text area, refresh mReceiverList
    private List<NameValuePair> mReceiverList = new ArrayList<NameValuePair>();

    // For tag info editor
    private View mEditContentView = null;

    private ImageView mSelectorView = null;

    private EditText mEditReceiverView = null;

    private EditText mEditSMSContentView = null;

    private LinearLayout mEditSubjectLayout = null;

    private EditText mEditSubjectView = null;

    private LinearLayout mEditAttachLayout = null;

    private ImageView mEditAttachButtonView = null;

    private TextView mEditAttachPathView = null;

    private AttachmentTypeSelectorAdapter mAttachmentTypeSelectorAdapter = null;

    private String mAttachFilePath = "";

    private String mAttachFileMimeType = "";

    boolean mIsNeedShowMMS = false;

    // For history tag view
    private View mHistoryContentView = null;

    private TextView mHistoryReceiverView = null;

    private TextView mHistorySMSContentView = null;

    private TextView mHistorySMSSubjectView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    private String mHistoryReceiverListStr = null;

    private String mHistorySMSContentStr = null;

    private String mHistorySMSSubjectStr = null;

    // For tag info reader
    private View mReadContentView = null;

    private TextView mReadReceiverView = null;

    private String mReadReceiverListStr = null;

    private String mReadSMSContentStr = null;

    private String mReadSMSSubjectStr = null;

    private boolean mReadSMSHasAttach = false;

    // Tag special column
    private static final String DB_COLUMN_RECV_LIST = TagContract.COLUMN_01;

    private static final String DB_COLUMN_SMS_CONTENT = TagContract.COLUMN_02;

    private static final String DB_COLUMN_SMS_SUBJECT = TagContract.COLUMN_03;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_RECV_LIST,
            DB_COLUMN_SMS_CONTENT, DB_COLUMN_SMS_SUBJECT, TagContract.COLUMN_BYTES
    };

    /**
     * Internal class for storing SMS receiver name and number info
     */
    class NameValuePair {
        public String mName;

        public String mNumber;

        public NameValuePair(String name, String number) {
            this.mName = name;
            this.mNumber = number;
        }

        public NameValuePair(String nameValueStr) {
            // like 'Loading<15810269021>'
            int numberBeginIndex = nameValueStr.indexOf("<");
            int numberEndIndex = nameValueStr.indexOf(">");
            String name = "";
            String number = "";
            if (numberBeginIndex > 0 && numberEndIndex > numberBeginIndex) {
                // contain <>
                String tempNumberStr = nameValueStr.substring(numberBeginIndex + 1, numberEndIndex);
                if (PhoneNumberUtils.isWellFormedSmsAddress(tempNumberStr)) {
                    number = tempNumberStr;
                    name = nameValueStr.substring(0, numberBeginIndex);
                } else {
                    name = nameValueStr;
                }
            } else {
                if (numberBeginIndex < 0 && numberEndIndex < 0
                        && PhoneNumberUtils.isWellFormedSmsAddress(nameValueStr)) {
                    // pure number
                    number = nameValueStr;
                    name = "";
                } else {
                    name = nameValueStr;
                    number = "";
                }
            }

            this.mName = name;
            this.mNumber = number;
        }

        @Override
        public String toString() {
            if (TextUtils.isEmpty(mName)) {
                return mNumber;
            } else if (TextUtils.isEmpty(mNumber)) {
                return mName;
            } else {
                return mName + "<" + mNumber + ">";
            }
        }

        /**
         * Compare two receiver's priority
         * 
         * @param other
         * @return 0 means different ones, 1 mean this one has higher priority
         */
        public int compare(NameValuePair other) {
            if (mNumber == null) {
                Utils.loge(TAG, "Invalid SMS receiver number of null");
                return 0;
            }
            if (!mNumber.equalsIgnoreCase(other.mNumber)) {
                return 0;
            }
            if (TextUtils.isEmpty(mName)) {
                return -1;
            } else if (TextUtils.isEmpty(other.mName)) {
                return 1;
            } else {
                return -1; // the later appeared one has higher priority
            }
        }
    }

    /**
     * This class is used to monitor the validation of SMS receiver list
     */
    class SMSReceiverListWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            mReceiverList.clear();
            if (mEditReceiverView != null) {
                String receiverListStr = mEditReceiverView.getText().toString();
                String[] receiverArray = receiverListStr.split(Utils.SEPARATOR_SMS_NUMBER);
                for (String receiver : receiverArray) {
                    if (receiver != null && receiver.trim().length() > 0) {
                        NameValuePair pair = new NameValuePair(receiver.trim());
                        if (TextUtils.isEmpty(pair.mNumber)) {
                            mReceiverList.add(pair);// This is a invalid
                            // NameValuePair
                        } else { // Judge whether exist duplicated one
                            updateRecieverList(pair);
                        }
                    }
                }
            }

            StringBuffer buffer = new StringBuffer();
            for (NameValuePair pair : mReceiverList) {
                if (buffer.length() > 0) {
                    buffer.append(";");
                }
                buffer.append(pair.toString());
            }
            Utils.logv(TAG, "    afterTextChanged(), receiver list=" + buffer.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create SMS record instance now");
            sRecord = new SMSRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    @Override
    public View getEditView() {
        mReceiverList.clear();
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_sms, null);
        if (mEditContentView != null) {
            mSelectorView = (ImageView) mEditContentView.findViewById(R.id.edit_info_sms_selector);
            mEditReceiverView = (EditText) mEditContentView
                    .findViewById(R.id.edit_info_sms_receiver);
            mEditSMSContentView = (EditText) mEditContentView
                    .findViewById(R.id.edit_info_sms_content);
            mEditSubjectLayout = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_sms_subject_component);
            mEditSubjectView = (EditText) mEditContentView.findViewById(R.id.edit_info_sms_subject);
            mEditAttachLayout = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_sms_attach_component);
            mEditAttachButtonView = (ImageView) mEditContentView
                    .findViewById(R.id.edit_info_sms_attach_selector);
            mEditAttachPathView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_sms_attach_path);

            mSelectorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContactSelectorPage();
                }
            });
            mEditAttachButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAttachmentSelector();
                }
            });
            if (mEditReceiverView != null) {
                mEditReceiverView.addTextChangedListener(new SMSReceiverListWatcher());
            } else {
                Utils.loge(TAG, "Fail to get EditReceiverView");
            }

            mIsNeedShowMMS = PreferenceManager.getDefaultSharedPreferences(sActivity).getBoolean(
                    Utils.KEY_SHOW_ADVANCED_WRITING, Utils.DEFAULT_VALUE_ADVANCED_APP_TAG);
        }
        return mEditContentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ADD_SUBJECT, 0, R.string.edit_view_sms_menu_add_subject)
                .setVisible(mIsNeedShowMMS).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_ADD_ATTACH, 0, R.string.edit_view_sms_menu_add_attach).setVisible(
                mIsNeedShowMMS).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_SUBJECT:
                if (mEditSubjectLayout != null) {
                    mEditSubjectLayout.setVisibility(View.VISIBLE);
                    item.setVisible(false);
                }
                break;
            case MENU_ADD_ATTACH:
                if (mEditAttachLayout != null) {
                    mEditAttachLayout.setVisibility(View.VISIBLE);
                    item.setVisible(false);
                }
                break;
            default:
                break;
        }
    }

    private void startContactSelectorPage() {
        Utils.logd(TAG, "-->startContactSelectorPage()");
        Intent intent = Contact.INTENT_SELECT_MULTI_PHONE;

        sActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT);
        Utils.logd(TAG, "<--startContactSelectorPage()");
    }

    private void startAttachmentSelector() {
        Utils.logd(TAG, "-->startAttachmentSelector()");
        showAddAttachmentDialog();
    }

    private void showAddAttachmentDialog() {
        Utils.logd(TAG, "-->showAddAttachmentDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(sActivity);
        builder.setTitle(R.string.mms_add_attach_dialog_title);
        builder.setIcon(R.drawable.ic_edit_info_sms_attach_selector);
        mAttachmentTypeSelectorAdapter = new AttachmentTypeSelectorAdapter(sActivity);
        builder.setAdapter(mAttachmentTypeSelectorAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedAttachType = mAttachmentTypeSelectorAdapter.buttonToCommand(which);
                addAttachment(selectedAttachType);
            }
        });
        builder.show();
    }

    private void addAttachment(int type) {
        Utils.logd(TAG, "-->addAttachment(), type=" + type);
        switch (type) {
            case AttachmentTypeSelectorAdapter.ADD_IMAGE:
                Utils.selectMediaByType(sActivity, REQUEST_CODE_ATTACH_IMAGE,
                        Utils.IMAGE_UNSPECIFIED, false);
                break;
            case AttachmentTypeSelectorAdapter.ADD_AUDIO:
                Utils.selectAudio(sActivity, REQUEST_CODE_ATTACH_AUDIO);
                break;
            case AttachmentTypeSelectorAdapter.ADD_RINGTONE:
                Utils.selectRingtone(sActivity, REQUEST_CODE_ATTACH_RINGTONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);

        // Uri uri = ((data == null)?null:data.getData());
        if (requestCode == REQUEST_CODE_CONTACT) {
            // For ICS begin
            if (resultCode == Activity.RESULT_OK && data != null) {
                final long[] contactsId = data
                        .getLongArrayExtra("com.mediatek.contacts.list.pickdataresult");
                Utils.logd(TAG, "Selected contact number=" + contactsId.length);
                List<Contact> entries = Contact.getContactInfoForPhoneIds(sActivity, contactsId);
                for (Contact entry : entries) {
                    Utils.logi(TAG, "Selected contact name=" + entry.getDisplayName() + ", number="
                            + entry.getPhoneNum());
                    // add the new selected contact into receiver list
                    if (!TextUtils.isEmpty(entry.getPhoneNum())) {
                        NameValuePair newOne = new NameValuePair(entry.getDisplayName(), entry
                                .getPhoneNum());
                        updateRecieverList(newOne);
                    } else {
                        Utils.logw(TAG,
                                "Empty phone number will be invalid receiver here, display name="
                                        + entry.getDisplayName());
                    }
                }

                if (entries == null || entries.size() == 0) {
                    Utils.loge(TAG, "No phone number is selected");
                    return;
                }

                StringBuffer receiverBuffer = new StringBuffer();
                for (NameValuePair pair : mReceiverList) {
                    receiverBuffer.append(pair.toString());
                    receiverBuffer.append(", ");
                }
                mEditReceiverView.setText(receiverBuffer.toString());
            }
            // For ICS end
        } else {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Utils.logi(TAG, "Request code = " + requestCode + ", Data=" + data.getData()
                        + ", type=" + data.getType());
                // Clear old cached attachment file info
                if (requestCode >= REQUEST_CODE_ATTACH_IMAGE
                        && requestCode <= REQUEST_CODE_ATTACH_UNKNOWN) {
                    mAttachFilePath = "";
                    mAttachFileMimeType = "";
                }

                switch (requestCode) {
                    case REQUEST_CODE_ATTACH_IMAGE:
                        Uri imageUri = data.getData();
                        String imageSchema = imageUri.getScheme();
                        if ("content".equals(imageSchema)) {
                            initMediaContentUri(imageUri);
                        } else if ("file".equals(imageSchema)) {
                            initMediaFromFile(imageUri);
                        }
                        break;
                    case REQUEST_CODE_ATTACH_RINGTONE:
                        Uri ringUri = (Uri) data
                                .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (Settings.System.DEFAULT_RINGTONE_URI.equals(ringUri)) {
                            break;
                        }
                        Utils.logi(TAG, "Selected Ringtong uri=" + ringUri);
                        String ringSchema = ringUri.getScheme();
                        if ("content".equals(ringSchema)) {
                            initMediaContentUri(ringUri);
                        } else if ("file".equals(ringSchema)) {
                            initMediaFromFile(ringUri);
                        }
                        break;
                    default:
                        break;
                }
                // Add attachment file info to UI
                if (requestCode >= REQUEST_CODE_ATTACH_IMAGE
                        && requestCode <= REQUEST_CODE_ATTACH_UNKNOWN) {
                    mEditAttachPathView.setText(mAttachFilePath);
                }
            } else {
                Utils.logd(TAG, "User cancel selection or select nothing.");
            }
        }
    }

    /**
     * Load attachment file info from local file system
     */
    private void initMediaFromFile(Uri fileUri) {
        Utils.logd(TAG, "-->initImageFromFile(), fileUri=" + fileUri);
        mAttachFilePath = fileUri.getPath();
        mAttachFileMimeType = "";
        int index = mAttachFilePath.lastIndexOf(".");
        if (index != -1) {
            String suffix = mAttachFilePath.substring(index + 1);
            mAttachFileMimeType = Utils.MIME_MAP.get(suffix);
        }
        if (TextUtils.isEmpty(mAttachFileMimeType)) {
            mAttachFileMimeType = "*/*";
        }
    }

    /**
     * Load attachment file info from gallery contend providers
     */
    private void initMediaContentUri(Uri imageUri) {
        Utils.logd(TAG, "-->initImageContentUri(), imageUri=" + imageUri);
        Cursor cursor = sActivity.getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    mAttachFilePath = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    mAttachFileMimeType = cursor.getString(cursor
                            .getColumnIndexOrThrow("mime_type"));
                } else {
                    Utils.loge(TAG, "No record exist in the selected uri");
                }
            } catch (IllegalArgumentException e) {
                Utils.loge(TAG, "Could not find need content column.", e);
            } finally {
                cursor.close();
            }
        } else {
            Utils.loge(TAG, "Null cursor");
        }
    }

    /**
     * Check the receiver list, to check whether the newOne already exist in the
     * list
     * 
     * @param newOne
     */
    private void updateRecieverList(NameValuePair newOne) {
        Utils.logv(TAG, "-->updateRecieverList()");
        boolean exist = false;
        int compareResult = 0;
        for (NameValuePair oldOne : mReceiverList) {
            compareResult = oldOne.compare(newOne);
            if (compareResult != 0) {
                exist = true;
                if (compareResult < 0) {
                    // replace old one with the new found one
                    oldOne.mName = newOne.mName;
                    oldOne.mNumber = newOne.mNumber;
                }
                break;
            }
        }
        if (!exist) {
            mReceiverList.add(newOne);
        }
    }

    @Override
    public NdefMessage getHistoryNdefMessage(int uriId) {
        Utils.logd(TAG, "-->getHistoryNdefMessage(), uriId=" + uriId + ", UI related uriId="
                + mHistoryUriId);
        if (uriId != mHistoryUriId) {
            Utils.logw(TAG, "History view did not show, just get the stored NdefMessage");
            NdefMessage historyMsg = getHistoryNdefMessage(sActivity, uriId);
            return historyMsg;
        } else {
            if (mHistoryMessage == null) {
                Utils.loge(TAG, "NDEF message is null");
            }
            return mHistoryMessage;
        }
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        mHistoryUriId = uriId;
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryContentView = inflater.inflate(R.xml.history_view_sms, null);
        if (mHistoryContentView != null) {
            mHistoryReceiverView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_sms_receiver);
            mHistorySMSContentView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_sms_content);
            mHistorySMSSubjectView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_sms_subject);
            LinearLayout mSubjectLabelLayout = (LinearLayout) mHistoryContentView
                    .findViewById(R.id.history_info_sms_subject_area);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mHistoryReceiverListStr = cursor.getString(cursor
                            .getColumnIndex(DB_COLUMN_RECV_LIST));
                    mHistorySMSContentStr = cursor.getString(cursor
                            .getColumnIndex(DB_COLUMN_SMS_CONTENT));
                    mHistorySMSSubjectStr = cursor.getString(cursor
                            .getColumnIndex(DB_COLUMN_SMS_SUBJECT));
                    Utils.logi(TAG, "History record, receiverStr=" + mHistoryReceiverListStr
                            + ", smsContentStr=" + mHistorySMSContentStr);
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    if (mHistoryReceiverView != null) {
                        mHistoryReceiverView.setText(mHistoryReceiverListStr);
                    }
                    if (mHistorySMSContentView != null) {
                        mHistorySMSContentView.setText(mHistorySMSContentStr);
                    }

                    if (mHistorySMSSubjectView != null && mSubjectLabelLayout != null) {
                        if (!TextUtils.isEmpty(mHistorySMSSubjectStr)) {
                            mSubjectLabelLayout.setVisibility(View.VISIBLE);
                            mHistorySMSSubjectView.setText(mHistorySMSSubjectStr);
                        } else {
                            mSubjectLabelLayout.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Utils.loge(TAG, "Fail to get SMS history record with id:" + uriId);
                }
                cursor.close();
            } else {
                Utils.loge(TAG, "Null cursor");
            }
        }
        return mHistoryContentView;
    }

    @Override
    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        if (!TextUtils.isEmpty(mHistoryReceiverListStr)
                && (!TextUtils.isEmpty(mHistorySMSContentStr) || !TextUtils
                        .isEmpty(mHistorySMSSubjectStr))) {
            if (TextUtils.isEmpty(mHistorySMSSubjectStr)) {
                startSms(mHistoryReceiverListStr, mHistorySMSContentStr);
            } else {
                startMms(mHistoryReceiverListStr, mHistorySMSContentStr, mHistorySMSSubjectStr);
            }
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_sms_empty_receiver_or_content,
                    Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_sms_empty_receiver_or_content));
            return false;
        }
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mEditReceiverView == null || mEditContentView == null) {
            Utils.loge(TAG, "Fail to load edit page, input area is null");
            return null;
        }
        String receiverStr = "";
        StringBuffer buffer = new StringBuffer();
        for (NameValuePair pair : mReceiverList) {
            if (TextUtils.isEmpty(pair.mNumber)) {
                Utils.loge(TAG, "Receiver list is invalid, [" + pair.mName + "]");
                Toast.makeText(
                        sActivity,
                        sActivity.getString(R.string.error_sms_invalid_receiver) + " ["
                                + pair.mName + "]", Toast.LENGTH_SHORT).show();
                return null;
            }
            if (buffer.length() > 0) {
                buffer.append(Utils.SEPARATOR_SMS_NUMBER);
            }
            buffer.append(pair.mNumber);
        }
        receiverStr = buffer.toString();
        String smsContentStr = mEditSMSContentView.getText().toString();
        String smsSubjectStr = mEditSubjectView.getText().toString();
        if (TextUtils.isEmpty(receiverStr)
                || (TextUtils.isEmpty(smsContentStr) && TextUtils.isEmpty(smsSubjectStr))) {
            Utils.loge(TAG, "Receiver list or content & subject is empty ");
            Toast.makeText(sActivity, R.string.error_sms_empty_receiver_or_content,
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        // To avoid 'Null' string
        if (TextUtils.isEmpty(smsContentStr)) {
            smsContentStr = "";
        }
        if (TextUtils.isEmpty(smsSubjectStr)) {
            smsSubjectStr = "";
        }

        // String payloadStr = Utils.PREFIX_SMS_TAG + receiverStr +
        // Utils.SEPARATOR_SMS_NUMBER_BODY
        // + smsContentStr;
        // if (!TextUtils.isEmpty(smsSubjectStr)) {
        // payloadStr = payloadStr + Utils.SEPARATOR_SMS_SUBJECT +
        // smsSubjectStr;
        // }

        String payloadStr = receiverStr + Utils.SEPARATOR_SMS_NUMBER_BODY + smsContentStr;
        if (!TextUtils.isEmpty(smsSubjectStr)) {
            payloadStr = "mms:" + payloadStr + Utils.SEPARATOR_SMS_SUBJECT + smsSubjectStr;
        } else {
            payloadStr = Utils.PREFIX_SMS_TAG + payloadStr;
        }

        byte[] bytes = payloadStr.getBytes();
        byte[] payload = new byte[bytes.length + 1];// Attention: Some
        // language's
        // String.length() is not
        // equal to bytes array's
        // length

        Utils.logv(TAG, "PayloadStr=" + payloadStr + ", str len=" + payloadStr.length()
                + ", byte len=" + bytes.length);

        payload[0] = (byte) 0;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);

        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
                new byte[0], payload);
        NdefMessage message = null;

        if (!TextUtils.isEmpty(mAttachFilePath)) { // whether append attachment
            // to this message
            byte[] attachStream = getAttachmentStream();
            Utils.logi(TAG, "Attachment file path=" + mAttachFilePath + ", size="
                    + ((attachStream == null) ? "" : attachStream.length) + ", mime_type="
                    + mAttachFileMimeType);
            if (!TextUtils.isEmpty(mAttachFileMimeType) && attachStream != null
                    && attachStream.length > 0
                    && attachStream.length <= Utils.SMS_ATTACH_FILE_MAX_SIZE) {
                NdefRecord attachRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                        mAttachFileMimeType.getBytes(), new byte[0], attachStream);
                NdefMessage combinedMsg = new NdefMessage(new NdefRecord[] {
                        textRecord, attachRecord
                });
                NdefRecord combinedRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                        NdefRecord.RTD_SMART_POSTER, new byte[0], combinedMsg.toByteArray());
                message = new NdefMessage(new NdefRecord[] {
                    combinedRecord
                });
            } else {
                if (attachStream.length > Utils.SMS_ATTACH_FILE_MAX_SIZE) {
                    String msg = "Attachment file is too large, it should no larger than "
                            + Utils.SMS_ATTACH_FILE_MAX_SIZE + ", drop it.";
                    Utils.loge(TAG, msg);
                    Toast.makeText(sActivity, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Utils.loge(TAG, "Unknown attachment type of null stream");
                }
                message = new NdefMessage(new NdefRecord[] {
                    textRecord
                });
            }
        } else {
            message = new NdefMessage(new NdefRecord[] {
                textRecord
            });
        }

        // Save the new input record into database
        addNewRecordToDB(message, receiverStr, smsContentStr, smsSubjectStr, 1);

        return message;
    }

    private byte[] getAttachmentStream() {
        Utils.logd(TAG, "-->getAttachmentStream()");
        File file = new File(mAttachFilePath);
        if (!file.exists()) {
            Utils.loge(TAG, "Selected attachment file does not exist!");
            return null;
        }
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(1024);
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[512];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                byteArrayBuffer.append(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayBuffer.toByteArray();
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param desc phone number description string
     * @param smsContentStr phone number
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String recevierListStr, String smsContentStr,
            String smsSubjectStr, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), recevierListStr=" + recevierListStr
                + ", smsContentStr=" + smsContentStr + ", subjectStr=" + smsSubjectStr
                + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_SMS);
        selectionBuilder.append(" AND " + DB_COLUMN_RECV_LIST + "=\'" + recevierListStr + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_SMS_CONTENT + "=\'"
                + Utils.encodeStrForDB(smsContentStr) + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_SMS_SUBJECT + "=\'"
                + Utils.encodeStrForDB(smsSubjectStr) + "\' ");
        selectionBuilder.append(" AND " + TagContract.COLUMN_IS_CREATED_BY_ME + "=\'" + tagSrc
                + "\' ");

        Cursor cursor = sActivity.getContentResolver().query(TagContract.TAGS_CONTENT_URI,
                PROJECTION, selectionBuilder.toString(), null, null);
        boolean exist = false;
        int recordNum = 0;
        if (cursor != null) {
            recordNum = cursor.getCount();
            if (recordNum > 0) {
                exist = true;
            }
            cursor.close();
        }

        if (exist) {
            Utils.logw(TAG, "SMS Record already exist, count=" + recordNum
                    + ", do not insert it again");
        } else {
            Utils.logi(TAG, "Insert new tag record into database");
            ContentValues values = new ContentValues();
            values.put(TagContract.COLUMN_DATE, System.currentTimeMillis());
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_SMS);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_RECV_LIST, recevierListStr);
            values.put(DB_COLUMN_SMS_CONTENT, smsContentStr);
            values.put(DB_COLUMN_SMS_SUBJECT, smsSubjectStr);
            String historyTitle = recevierListStr;
            values.put(TagContract.COLUMN_HISTORY_TITLE, historyTitle);

            Uri uri = sActivity.getContentResolver().insert(TagContract.TAGS_CONTENT_URI, values);
            if (uri == null) {
                Utils.loge(TAG, "Add new record fail");
                return false;
            } else {
                int deletedNum = Utils.limitHistorySize(sActivity, sActivity.getSharedPreferences(
                        Utils.CONFIG_FILE_NAME, Context.MODE_PRIVATE).getInt(
                        Utils.KEY_HISTORY_SIZE, Utils.DEFAULT_VALUE_HISTORY_SIZE));
                Utils.logd(TAG,
                        "After insert a new record, check total history size, deleted size="
                                + deletedNum);
            }
        }
        return true;
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
        mReadReceiverListStr = "";
        mReadSMSContentStr = "";
        mReadSMSSubjectStr = "";
        NdefRecord record = msg.getRecords()[0];
        clearCachedAttachmentFiles();
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();

        if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type, NdefRecord.RTD_SMART_POSTER)) {
            Utils.logi(TAG, "Encounter a SP MMS record, parse its second level record now");
            NdefMessage subMsg = null;
            try {
                subMsg = new NdefMessage(payload);
                payload = null;// reset payload first
            } catch (FormatException e) {
                Utils.loge(TAG, "Fail to parse second level URL record", e);
                e.printStackTrace();
            }
            if (subMsg != null) {
                NdefRecord[] recordList = subMsg.getRecords();
                NdefRecord smstRecord = Utils.getOnlyExistingRecord(recordList, Utils.TAG_TYPE_SMS);
                // extract attachment file info
                getAttachmentList(recordList);
                if (smstRecord != null) {
                    tnf = smstRecord.getTnf();
                    type = smstRecord.getType();
                    payload = smstRecord.getPayload();
                }
            } else {
                Utils.loge(TAG, "SP tag payload is not a valid NdefMessage");
            }
        }

        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }

        if (tnf != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(type, NdefRecord.RTD_URI)
                || payload[0] != (byte) 0) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_sms, null);

        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }
        String contentStr = new String(payload, 1, payload.length - 1);
        int bodyIndex = contentStr.indexOf(Utils.SEPARATOR_SMS_NUMBER_BODY);
        int subjectIndex = contentStr.indexOf(Utils.SEPARATOR_SMS_SUBJECT);
        int contentBeginIndex = contentStr.indexOf(":");
        if (contentBeginIndex > 0 && bodyIndex > contentBeginIndex) {
            mReadReceiverListStr = contentStr.substring(contentBeginIndex + 1, bodyIndex);
        } else {
            Utils.loge(TAG, "SMS tag payload did not begin with 'sms:'");
        }
        if (subjectIndex > bodyIndex) {
            mReadSMSContentStr = contentStr.substring(bodyIndex
                    + Utils.SEPARATOR_SMS_NUMBER_BODY.length(), subjectIndex);
            mReadSMSSubjectStr = contentStr.substring(subjectIndex
                    + Utils.SEPARATOR_SMS_SUBJECT.length());
        } else {
            mReadSMSContentStr = contentStr.substring(bodyIndex
                    + Utils.SEPARATOR_SMS_NUMBER_BODY.length());
        }

        if (mReadContentView != null) {
            mReadReceiverView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_sms_receiver);
            if (mReadReceiverView != null) {
                mReadReceiverView.setText(mReadReceiverListStr);
            }
        }

        addNewRecordToDB(msg, mReadReceiverListStr, mReadSMSContentStr, mReadSMSSubjectStr, 0);

        return mReadContentView;
    }

    /**
     * After read a NDEF message, extract attachment file in it and store them
     * into local file system Cached file name will like attach_xx.png
     */
    private void getAttachmentList(NdefRecord[] records) {
        Utils.logd(TAG, "-->getAttachmentList()");

        int tempIndex = 1;
        for (NdefRecord record : records) {
            int tnf = record.getTnf();
            byte[] type = record.getType();
            String fileMimeType = new String(type);
            byte[] payload = record.getPayload();
            if (tnf == NdefRecord.TNF_MIME_MEDIA && fileMimeType != null) {
                String attachSuffix = "";
                boolean foundSuffix = false;
                if (!TextUtils.isEmpty(fileMimeType)) {
                    for (String suffix : Utils.MIME_MAP.keySet()) {
                        if (Utils.MIME_MAP.get(suffix).equals(fileMimeType)) {
                            if (!foundSuffix) {
                                foundSuffix = true;
                                attachSuffix = suffix;
                            } else {
                                Utils.loge(TAG, "More than one file suffix can fit MIME type ["
                                        + fileMimeType + "], just take the first one.");
                                break;
                            }
                        }
                    }
                }
                if (!foundSuffix) {
                    Utils.loge(TAG, "Could not find suitable file suffix for MIME type ["
                            + fileMimeType + "]");
                }

                String tempAttachName = Utils.SMS_ATTACH_FILE_PREFIX + (tempIndex++) + attachSuffix;
                FileOutputStream out = null;

                try {
                    out = sActivity.openFileOutput(tempAttachName, Context.MODE_WORLD_READABLE);
                    out.write(payload);
                    out.flush();
                    mReadSMSHasAttach = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Clear former cached attachment file in local file system
     */
    private void clearCachedAttachmentFiles() {
        Utils.logd(TAG, "-->clearCachedAttachmentFiles()");
        mReadSMSHasAttach = false;
        File attachFolder = sActivity.getFilesDir();
        if (attachFolder.exists() && attachFolder.isDirectory()) {
            File[] files = attachFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(Utils.SMS_ATTACH_FILE_PREFIX)) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_sms;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_sms;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_sms;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_sms;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_sms_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_sms_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_SMS;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_sms, sActivity
                .getString(R.string.tag_title_sms));
        pref.setTagType(Utils.TAG_TYPE_SMS);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");

        if (!TextUtils.isEmpty(mReadReceiverListStr)
                && (!TextUtils.isEmpty(mReadSMSContentStr) || !TextUtils
                        .isEmpty(mReadSMSSubjectStr))) {
            if (TextUtils.isEmpty(mReadSMSSubjectStr) && !mReadSMSHasAttach) {
                startSms(mReadReceiverListStr, mReadSMSContentStr);
            } else {
                startMms(mReadReceiverListStr, mReadSMSContentStr, mReadSMSSubjectStr);
            }
            // Finish tag information preview page
            sActivity.finish();
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_sms_empty_receiver_or_content,
                    Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_sms_empty_receiver_or_content));
            return false;
        }
    }

    private void startSms(String receiverList, String smsContent) {
        Utils.logd(TAG, "-->startSms(), receiverList=" + receiverList + ", smsContent="
                + smsContent);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        Uri uri = Uri.parse("sms:" + receiverList);
        intent.setData(uri);
        if (!TextUtils.isEmpty(smsContent)) {
            intent.putExtra("sms_body", smsContent);
        }
        sActivity.startActivity(intent);
        Utils.logd(TAG, "<--startSms()");
    }

    private void startMms(String receiverList, String smsContent, String subjectString) {
        Utils.logd(TAG, "-->startMms(), receiverList=" + receiverList + ", smsContent="
                + smsContent + ", subjectStr=" + subjectString);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        File attachFolder = sActivity.getFilesDir();
        if (attachFolder.exists() && attachFolder.isDirectory()) {
            File[] attachFiles = attachFolder.listFiles();
            boolean foundAttach = false;
            for (File file : attachFiles) {
                if (file.getName().startsWith(Utils.SMS_ATTACH_FILE_PREFIX)) {
                    if (!foundAttach) {
                        foundAttach = true;
                        Utils.logi(TAG, "Found a attachment file: " + file.getName());
                        Uri atttachmentUri = Uri.fromFile(file);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, atttachmentUri);
                    } else {
                        Utils.logw(TAG, "Current only one attachment is supported");
                        break;
                    }
                }
            }
            if (!foundAttach) {
                Utils.logw(TAG, "Found no attachment file.");
            }
        }

        sendIntent.putExtra("subject", subjectString);
        sendIntent.putExtra("sms_body", smsContent);
        sendIntent.putExtra("address", receiverList);
        sendIntent.setType("image/*");// Attachment type of MMS
        sendIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");

        sActivity.startActivity(sendIntent);

        Utils.logd(TAG, "<--startMms()");
    }

}
