
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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/PhoneNumberRecord";

    private static final int REQUEST_CODE_CONTACT = 1;

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // For tag info editor
    private View mEditContentView = null;

    private EditText mEditDescView;

    private EditText mEditPhoneNumberView;

    private LinearLayout mSelectorView;

    // For tag info reader
    private View mReadContentView = null;

    private TextView mReadDescView = null;

    private TextView mReadPhoneNumberView = null;

    private boolean mIsUSSDNumber = false;

    // For history tag view
    private View mHistoryContentView = null;

    private TextView mHistoryDescView = null;

    private TextView mHistoryPhoneNumberView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // Tag special column
    private static final String DB_COLUMN_DESC = TagContract.COLUMN_01;

    private static final String DB_COLUMN_NUMBER = TagContract.COLUMN_02;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_DESC, DB_COLUMN_NUMBER,
            TagContract.COLUMN_BYTES
    };

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_phone_num,
                sActivity.getString(R.string.tag_title_phone_num));
        pref.setTagType(Utils.TAG_TYPE_PHONE_NUM);
        return pref;
    }

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create PhoneNumber record instance now");
            sRecord = new PhoneNumberRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    @Override
    public View getEditView() {
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_phone_number, null);
        if (mEditContentView != null) {
            mEditDescView = (EditText) mEditContentView.findViewById(R.id.edit_info_phone_desc);
            mEditPhoneNumberView = (EditText) mEditContentView
                    .findViewById(R.id.edit_info_phone_number);
            mSelectorView = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_phone_selector);
            mSelectorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContactSelectorPage();
                }
            });
        }
        return mEditContentView;
    }

    private void startContactSelectorPage() {
        Utils.logd(TAG, "-->startContactSelectorPage()");
        Intent intent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);

        sActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT);
        Utils.logd(TAG, "<--startContactSelectorPage()");
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);
        if (requestCode == REQUEST_CODE_CONTACT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri contactData = data.getData();
                Cursor c = sActivity.managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String displayName = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
                    String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        phoneNumber = phoneNumber.replace(" ", "").replace("-", "");
                    }
                    Utils.logi(TAG, "User selected contact displayName=" + displayName
                            + ", phoneNumber=" + phoneNumber);
                    mEditDescView.setText(displayName);
                    mEditPhoneNumberView.setText(phoneNumber);
                } else {
                    Utils.loge(TAG, "Fail to get phone number info by URI");
                }
            }
        } else {
            Utils.loge(TAG, "Unknown request code.");
        }
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mEditDescView == null || mEditPhoneNumberView == null) {
            Utils.loge(TAG, "Fail to load edit page, input area is null");
            return null;
        }
        String descStr = mEditDescView.getText().toString();
        String numberStr = mEditPhoneNumberView.getText().toString();
        if (TextUtils.isEmpty(numberStr)) {
            Utils.loge(TAG, "Phone number is null, invalid");
            Toast.makeText(sActivity, R.string.error_phone_empty_number, Toast.LENGTH_SHORT).show();
            return null;
        }

        byte[] bytes = numberStr.getBytes();
        byte[] payload = new byte[bytes.length + 1];// Attention: Some
        // language's
        // String.length() is not
        // equal to bytes array's
        // length

        payload[0] = (byte) Utils.URI_TYPE_TEL;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);

        NdefRecord numberRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
                new byte[0], payload);

        NdefMessage message = null;
        if (TextUtils.isEmpty(descStr)) {
            message = new NdefMessage(new NdefRecord[] {
                numberRecord
            });
        } else {
            NdefRecord descRecord = Utils.newNDEFTextRecord(descStr, Locale.getDefault(), true);
            NdefMessage combinedMsg = new NdefMessage(new NdefRecord[] {
                    descRecord, numberRecord
            });
            NdefRecord combinedRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_SMART_POSTER, new byte[0], combinedMsg.toByteArray());
            message = new NdefMessage(new NdefRecord[] {
                combinedRecord
            });
        }

        // Save the new input record into database
        addNewRecordToDB(message, descStr, numberStr, 1);

        return message;
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        String descStr = "";
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();

        // Check whether this is a Smart Poster record first
        if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type, NdefRecord.RTD_SMART_POSTER)) {
            Utils.logi(TAG, "Encounter a SP PhoneNumber record, parse its second level record now");
            NdefMessage subMsg = null;
            try {
                subMsg = new NdefMessage(payload);
                payload = null;// reset payload first
            } catch (FormatException e) {
                Utils.loge(TAG, "Fail to parse second level PhoneNumber record", e);
                e.printStackTrace();
            }
            if (subMsg != null) {
                NdefRecord[] recordList = subMsg.getRecords();
                NdefRecord descRecord = Utils.getFirstRecordIfExists(recordList,
                        Utils.TAG_TYPE_TEXT);
                NdefRecord urlRecord = Utils.getOnlyExistingRecord(recordList,
                        Utils.TAG_TYPE_PHONE_NUM);
                descStr = Utils.getTextRecordContent(descRecord);
                if (urlRecord != null) {
                    tnf = urlRecord.getTnf();
                    type = urlRecord.getType();
                    payload = urlRecord.getPayload();
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
                || payload[0] != Utils.URI_TYPE_TEL) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        String numberStr = new String(payload, 1, payload.length - 1);
        mIsUSSDNumber = isUSSDNumber(numberStr);

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_phone_number, null);

        if (mReadContentView != null) {
            mReadDescView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_phone_desc_content);
            mReadPhoneNumberView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_phone_number_content);
            TextView readDescLabelView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_phone_desc_label);
            if (mIsUSSDNumber) {
                readDescLabelView.setText(R.string.edit_view_ussd_description_label);
            }
            if (mReadDescView != null) {
                mReadDescView.setText(descStr);
            }
            if (mReadPhoneNumberView != null) {
                mReadPhoneNumberView.setText(numberStr);
            }
        }

        addNewRecordToDB(msg, descStr, numberStr, 0);

        return mReadContentView;
    }

    private boolean isUSSDNumber(String number) {
        Utils.logd(TAG, "-->isUSSDNumber(), number=" + number);
        boolean result = false;
        if (TextUtils.isEmpty(number)) {
            return result;
        }
        Pattern pattern = Pattern.compile("^[*](\\S)+#$");
        Matcher matcher = pattern.matcher(number);
        if (matcher.matches()) {
            result = true;
        }
        Utils.logd(TAG, "<--isUSSDNumber(),number[" + number + "] can match USSD number?" + result);
        return result;
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        mHistoryUriId = uriId;
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryContentView = inflater.inflate(R.xml.history_view_phone_number, null);
        if (mHistoryContentView != null) {
            mHistoryDescView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_phone_desc);
            mHistoryPhoneNumberView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_phone_number);
            TextView historyDescView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_phone_desc_label);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String descStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_DESC));
                    String numberStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_NUMBER));
                    mIsUSSDNumber = isUSSDNumber(numberStr);
                    if (mIsUSSDNumber) {
                        historyDescView.setText(R.string.edit_view_ussd_description_label);
                    }

                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    if (mHistoryDescView != null) {
                        mHistoryDescView.setText(descStr);
                    }
                    if (mHistoryPhoneNumberView != null) {
                        mHistoryPhoneNumberView.setText(numberStr);
                    }
                } else {
                    Utils.loge(TAG, "Fail to get phone number history record with id:" + uriId);
                }
                cursor.close();
            }
        }
        return mHistoryContentView;
    }

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
    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");

        if (mHistoryPhoneNumberView == null) {
            Utils.loge(TAG, " Fail to get needed phone number view.");
            return false;
        }
        String phoneNum = mHistoryPhoneNumberView.getText().toString();
        if (!TextUtils.isEmpty(phoneNum)) {
            startDial(phoneNum);
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_phone_empty_number, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_phone_empty_number));
            return false;
        }
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param desc phone number description string
     * @param number phone number
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String desc, String number, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), desc=" + desc + ", number=" + number
                + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_PHONE_NUM);
        selectionBuilder.append(" AND " + DB_COLUMN_DESC + "=\'" + Utils.encodeStrForDB(desc)
                + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_NUMBER + "=\'" + number + "\' ");
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
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_PHONE_NUM);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_DESC, desc);
            values.put(DB_COLUMN_NUMBER, number);
            String historyTitle = (TextUtils.isEmpty(desc) ? number : desc);
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
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");

        // mActivity.getContentResolver().query(TagContract.TAGS_CONTENT_URI,
        // null, null, null, null);

        if (mReadPhoneNumberView == null) {
            Utils.loge(TAG, "Fail to get needed phone number view.");
            return false;
        }
        String phoneNum = mReadPhoneNumberView.getText().toString();
        if (!TextUtils.isEmpty(phoneNum)) {
            startDial(phoneNum);
            // Finish tag information preview page
            sActivity.finish();
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_phone_empty_number, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_phone_empty_number));
            return false;
        }
    }

    private void startDial(String number) {
        Utils.logd(TAG, "-->startDial(), number = " + number);

        if (TextUtils.isEmpty(number)) {
            Utils.loge(TAG, "Can not dial a empty number");
            return;
        }
        // Since '#' and other special character cannot be delivered to phone
        // directly, encode it first
        String encodedNumber = Uri.encode(number);

        String prefixStr = Utils.URI_PREFIX_MAP.get((byte) Utils.URI_TYPE_TEL);
        if (!encodedNumber.startsWith(prefixStr)) {
            Utils.logd(TAG, "Add prefix[" + prefixStr + "] to number " + encodedNumber
                    + " to make it an URI string");
            encodedNumber = prefixStr + encodedNumber;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri uri = Uri.parse(encodedNumber);
        Utils.logi(TAG, "Uri=" + uri.toString());
        intent.setData(uri);
        sActivity.startActivity(intent);
        Utils.logd(TAG, "<--startDial()");
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_phone_num;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_phone_title;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_phone_summary;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_phone;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_phone_num;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_phone;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_PHONE_NUM;
    }
}
