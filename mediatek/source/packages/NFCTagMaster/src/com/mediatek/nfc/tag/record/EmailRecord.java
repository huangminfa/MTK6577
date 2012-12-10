
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Contact;
import com.mediatek.nfc.tag.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EmailRecord extends ParsedNdefRecord {

    private static final String TAG = Utils.TAG + "/EmailRecord";

    private static final int REQUEST_CODE_CONTACT = 1;

    private static ParsedNdefRecord sInstance;

    private static Activity sActivity;

    // Tag special column
    private static final String DB_COLUMN_DESC = TagContract.COLUMN_01;

    private static final String DB_COLUMN_TO = TagContract.COLUMN_02;

    private static final String DB_COLUMN_SUBJECT = TagContract.COLUMN_03;

    private static final String DB_COLUMN_CONTENT = TagContract.COLUMN_04;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_DESC, DB_COLUMN_TO,
            DB_COLUMN_SUBJECT, DB_COLUMN_CONTENT, TagContract.COLUMN_BYTES
    };

    // For tag info editor
    private View mEditLayoutView = null;

    private EditText mEditDescView;

    private EditText mEditToView;

    private EditText mEditSubjectView;

    private EditText mEditContentView;

    private ImageView mContactSelectorView;

    // For history tag view
    private View mHistoryLayoutView = null;

    private TextView mHistoryDescView = null;

    private TextView mHistoryDescLabelView = null;

    private TextView mHistoryToView = null;

    private TextView mHistorySubjectView = null;

    private String mHistoryContentStr = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // For tag info reader
    private View mReadLayoutView = null;

    private TextView mReadDescView = null;

    private TextView mReadDescLabelView = null;

    private TextView mReadToView = null;

    private TextView mReadSubjectView = null;

    private String mReadContentStr = null;

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sInstance == null) {
            Utils.logi(TAG, "Create Email record instance now");
            sInstance = new EmailRecord();
        }
        sActivity = activity;
        return sInstance;
    }

    @Override
    public View getEditView() {
        Utils.logd(TAG, "-->getEditView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditLayoutView = inflater.inflate(R.xml.edit_view_email, null);
        if (mEditLayoutView != null) {
            mEditDescView = (EditText) mEditLayoutView
                    .findViewById(R.id.edit_info_email_description);
            mEditToView = (EditText) mEditLayoutView.findViewById(R.id.edit_info_email_to);
            mEditSubjectView = (EditText) mEditLayoutView
                    .findViewById(R.id.edit_info_email_subject);
            mEditContentView = (EditText) mEditLayoutView
                    .findViewById(R.id.edit_info_email_content);
            mContactSelectorView = (ImageView) mEditLayoutView
                    .findViewById(R.id.edit_info_email_selector);
            mContactSelectorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContactSelectorPage();
                }
            });
        }
        return mEditLayoutView;
    }

    private void startContactSelectorPage() {
        Utils.logd(TAG, "-->startContactSelectorPage()");
        Intent intent = Contact.INTENT_SELECT_MULTI_EMAIL;

        sActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT);
        Utils.logd(TAG, "<--startContactSelectorPage()");
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);

        if (requestCode == REQUEST_CODE_CONTACT) {
            // For ICS begin
            if (resultCode == Activity.RESULT_OK && data != null) {
                final long[] contactsId = data
                        .getLongArrayExtra("com.mediatek.contacts.list.pickdataresult");
                Utils.logd(TAG, "Selected contact number=" + contactsId.length);
                List<Contact> entries = Contact.getContactInfoForPhoneIds(sActivity, contactsId);
                StringBuffer receiverBuffer = new StringBuffer();
                for (Contact entry : entries) {
                    Utils.logi(TAG, "Selected email addr =" + entry.getEmailAddr() + ", type="
                            + entry.getEmailType());
                    receiverBuffer.append(entry.getEmailAddr());
                    receiverBuffer.append(Utils.SEPARATOR_EMAIL_TO_LIST);
                }

                if (entries == null || entries.size() == 0) {
                    Utils.loge(TAG, "No email address is selected");
                    return;
                }
                mEditToView.setText(receiverBuffer.toString());
            }
            // For ICS end
        } else {
            Utils.loge(TAG, "Unknown request code.");
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
        mHistoryContentStr = "";
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryLayoutView = inflater.inflate(R.xml.history_view_email, null);
        if (mHistoryLayoutView != null) {
            mHistoryDescView = (TextView) mHistoryLayoutView
                    .findViewById(R.id.history_info_email_desc);
            mHistoryDescLabelView = (TextView) mHistoryLayoutView
                    .findViewById(R.id.history_info_email_desc_label);
            mHistoryToView = (TextView) mHistoryLayoutView.findViewById(R.id.history_info_email_to);
            mHistorySubjectView = (TextView) mHistoryLayoutView
                    .findViewById(R.id.history_info_email_subject);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String descStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_DESC));
                    String toStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_TO));
                    String subjectStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_SUBJECT));
                    mHistoryContentStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT));

                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    if (mHistoryDescView != null) {
                        if (TextUtils.isEmpty(descStr)) {
                            mHistoryDescView.setVisibility(View.GONE);
                            mHistoryDescLabelView.setVisibility(View.GONE);
                        } else {
                            mHistoryDescView.setVisibility(View.VISIBLE);
                            mHistoryDescLabelView.setVisibility(View.VISIBLE);
                        }
                        mHistoryDescView.setText(descStr);
                    }
                    if (mHistoryToView != null) {
                        mHistoryToView.setText(toStr);
                    }
                    if (mHistorySubjectView != null) {
                        mHistorySubjectView.setText(subjectStr);
                    }
                } else {
                    Utils.loge(TAG, "Fail to get Email history record with id:" + uriId);
                }
                cursor.close();
            }
        }
        return mHistoryLayoutView;
    }

    @Override
    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        String toStr = "";
        String subjectStr = "";
        if (mHistoryToView == null || mHistorySubjectView == null) {
            Utils.loge(TAG, "Fail to get toView and subjectView");
            return false;
        }
        toStr = mHistoryToView.getText().toString();
        subjectStr = mHistorySubjectView.getText().toString();
        if (TextUtils.isEmpty(toStr)) {
            Toast.makeText(sActivity, R.string.error_email_empty_to, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_email_empty_to));
            return false;
        } else {
            startEmail(toStr, subjectStr, mHistoryContentStr);
            return true;
        }
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mEditToView == null || mEditSubjectView == null || mEditContentView == null) {
            Utils.loge(TAG, "Fail to load edit page, input area is null");
            return null;
        }

        String toStr = mEditToView.getText().toString();
        String subjectStr = mEditSubjectView.getText().toString();
        String contentStr = mEditContentView.getText().toString();
        String descStr = mEditDescView == null ? "" : mEditDescView.getText().toString();

        if (TextUtils.isEmpty(toStr)) {
            Utils.loge(TAG, "Email receiver list is empty, invalid");
            Toast.makeText(sActivity, R.string.error_email_empty_to, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (TextUtils.isEmpty(subjectStr)) {
            Utils.logw(TAG, "Email subject is empty!");
            // Toast.makeText(sActivity, R.string.error_email_empty_subject,
            // Toast.LENGTH_SHORT)
            // .show();
            // return null;
        }
        if (TextUtils.isEmpty(contentStr)) {
            Utils.logw(TAG, "Email content is empty!");
            // Toast.makeText(sActivity, R.string.error_email_empty_content,
            // Toast.LENGTH_SHORT)
            // .show();
            // return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(toStr);
        builder.append("?subject=" + subjectStr);
        builder.append("&body=" + contentStr);
        String payloadStr = builder.toString();

        byte[] bytes = payloadStr.getBytes();
        byte[] payload = new byte[bytes.length + 1];// Attention: Some
        // language's
        // String.length() is not
        // equal to bytes array's
        // length

        payload[0] = (byte) 6;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);

        NdefRecord emailRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
                new byte[0], payload);
        NdefMessage msg = null;
        if (TextUtils.isEmpty(descStr)) { // Pure email record, no description
            // text
            msg = new NdefMessage(new NdefRecord[] {
                emailRecord
            });
        } else { // Email record with a text description record
            NdefRecord descRecord = Utils.newNDEFTextRecord(descStr, Locale.getDefault(), true);
            NdefMessage combinedMsg = new NdefMessage(new NdefRecord[] {
                    descRecord, emailRecord
            });
            NdefRecord combinedRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_SMART_POSTER, new byte[0], combinedMsg.toByteArray());
            msg = new NdefMessage(new NdefRecord[] {
                combinedRecord
            });
        }
        // Save the new input record into database
        addNewRecordToDB(msg, descStr, toStr, subjectStr, contentStr, 1);

        return msg;
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param desc Email description string
     * @param toStr Email receiver list
     * @param subject Email subject
     * @param content Email content
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String desc, String toStr, String subject,
            String content, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), desc=" + desc + ", toStr=" + toStr + ", subject="
                + subject + ", content=" + content + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_EMAIL);
        selectionBuilder.append(" AND " + DB_COLUMN_DESC + "=\'" + Utils.encodeStrForDB(desc)
                + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_TO + "=\'" + toStr + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_SUBJECT + "=\'" + Utils.encodeStrForDB(subject)
                + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_CONTENT + "=\'" + Utils.encodeStrForDB(content)
                + "\' ");
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
            Utils
                    .logw(TAG, "Record already exist, count=" + recordNum
                            + ", do not insert it again");
        } else {
            Utils.logi(TAG, "Insert new tag record into database");
            ContentValues values = new ContentValues();
            values.put(TagContract.COLUMN_DATE, System.currentTimeMillis());
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_EMAIL);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_DESC, desc);
            values.put(DB_COLUMN_TO, toStr);
            values.put(DB_COLUMN_SUBJECT, subject);
            values.put(DB_COLUMN_CONTENT, content);
            String historyTitle = (TextUtils.isEmpty(desc) ? subject : desc);
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
        mReadContentStr = "";
        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        String descStr = "";

        // First judge if this is a 1-level Email record or a SP record with
        // Email in it
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();
        if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type, NdefRecord.RTD_SMART_POSTER)) {
            Utils.logi(TAG, "Encounter a SP record, parse its second level record now");
            NdefMessage subMsg = null;
            try {
                subMsg = new NdefMessage(payload);
                payload = null;// reset payload to null first
            } catch (FormatException e) {
                Utils.loge(TAG, "Fail to parse second level Email record", e);
                e.printStackTrace();
            }
            if (subMsg != null) {
                NdefRecord[] recordList = subMsg.getRecords();
                NdefRecord descRecord = Utils.getFirstRecordIfExists(recordList,
                        Utils.TAG_TYPE_TEXT);
                NdefRecord emailRecord = Utils.getOnlyExistingRecord(recordList,
                        Utils.TAG_TYPE_EMAIL);
                descStr = Utils.getTextRecordContent(descRecord);
                if (emailRecord != null) {
                    tnf = emailRecord.getTnf();
                    type = emailRecord.getType();
                    payload = emailRecord.getPayload();
                }
            } else {
                Utils.loge(TAG, "SP tag payload is not a valid NdefMessage");
            }
        }

        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }
        String prefix = Utils.URI_PREFIX_MAP.get(payload[0]);
        if (tnf != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(type, NdefRecord.RTD_URI)
                || !prefix.startsWith("mailto")) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadLayoutView = inflater.inflate(R.xml.read_view_email, null);
        String contentStr = new String(payload, 1, payload.length - 1);

        Utils.logd(TAG, "Email tag contentStr=" + contentStr);
        String toStr = "";
        String subjectStr = "";

        int index = contentStr.indexOf("?");
        try {
            if (index == -1) {
                toStr = URLDecoder.decode(contentStr, "UTF-8");
            } else {
                toStr = contentStr.substring(0, index);
            }
        } catch (UnsupportedEncodingException e) {
            Utils.loge(TAG, "Invalid email receiver string", e);
        }
        Uri uri = Uri.parse("foo://" + contentStr);
        subjectStr = uri.getQueryParameter("subject");
        mReadContentStr = uri.getQueryParameter("body");

        if (mReadLayoutView != null) {
            mReadDescView = (TextView) mReadLayoutView
                    .findViewById(R.id.read_info_email_description);
            mReadDescLabelView = (TextView) mReadLayoutView
                    .findViewById(R.id.read_info_email_description_label);
            mReadToView = (TextView) mReadLayoutView.findViewById(R.id.read_info_email_to);
            mReadSubjectView = (TextView) mReadLayoutView
                    .findViewById(R.id.read_info_email_subject);
            if (mReadDescView != null) {
                mReadDescView.setText(descStr);
                if (TextUtils.isEmpty(descStr)) {
                    mReadDescView.setVisibility(View.GONE);
                    mReadDescLabelView.setVisibility(View.GONE);
                } else {
                    mReadDescView.setVisibility(View.VISIBLE);
                    mReadDescLabelView.setVisibility(View.VISIBLE);
                }
            }
            if (mReadToView != null) {
                mReadToView.setText(toStr);
            }
            if (mReadSubjectView != null) {
                mReadSubjectView.setText(subjectStr);
            }
        }

        addNewRecordToDB(msg, descStr, toStr, subjectStr, mReadContentStr, 0);

        return mReadLayoutView;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_email;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_email;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_email;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_email;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_email_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_email_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_EMAIL;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_email,
                sActivity.getString(R.string.tag_title_email));
        pref.setTagType(Utils.TAG_TYPE_EMAIL);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");

        if (mReadToView == null || mReadSubjectView == null) {
            Utils.loge(TAG, "Fail to get toView and subjectView");
            return false;
        }
        String toStr = mReadToView.getText().toString();
        String subjectStr = mReadSubjectView.getText().toString();
        if (TextUtils.isEmpty(toStr)) {
            Toast.makeText(sActivity, R.string.error_email_empty_to, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_email_empty_to));
            return false;
        } else {
            startEmail(toStr, subjectStr, mReadContentStr);
            // Finish tag information preview page
            sActivity.finish();
            return true;
        }
    }

    private void startEmail(String toStr, String subjectStr, String bodyStr) {
        Utils.logd(TAG, "-->startEmail(), toStr = " + toStr + ", subjectStr = " + subjectStr
                + ", bodyStr = " + bodyStr);
        if(TextUtils.isEmpty(toStr)){
            Utils.loge(TAG, "Email receiver list is empty.");
            return;
        }
        String[] recvStrings = toStr.split("[,;]");
        Utils.logv(TAG, "Email receiver number = "+recvStrings.length);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        Uri uri = Uri.parse("mailto:");
        intent.setData(uri);
        intent.putExtra(Intent.EXTRA_EMAIL, recvStrings);
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectStr);
        intent.putExtra(Intent.EXTRA_TEXT, bodyStr);
        
        sActivity.startActivity(intent);
        Utils.logd(TAG, "<--startEmail()");
    }
}
