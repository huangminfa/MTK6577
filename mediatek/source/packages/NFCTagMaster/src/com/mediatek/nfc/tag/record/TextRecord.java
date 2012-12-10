
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.Locale;

public class TextRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/TextRecord";

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    private View mEditContentView = null;

    private View mReadContentView = null;

    // For tag history
    private View mHistoryContentView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // Tag special column
    private static final String DB_COLUMN_TEXT = TagContract.COLUMN_01;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_TEXT,
            TagContract.COLUMN_BYTES
    };

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_text, sActivity
                .getString(R.string.tag_title_text));
        pref.setTagType(Utils.TAG_TYPE_TEXT);
        return pref;
    }

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create Text record instance now");
            sRecord = new TextRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    @Override
    public View getEditView() {
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_text, null);
        return mEditContentView;
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNdefRecord()");
        String inputStr = null;
        NdefRecord ndefRecord = null;
        if (mEditContentView != null) {
            EditText textView = (EditText) mEditContentView.findViewById(R.id.edit_info_text);
            if (textView != null) {
                inputStr = textView.getText().toString();
            }
        }
        NdefMessage msg = null;
        if (!TextUtils.isEmpty(inputStr)) {
            ndefRecord = Utils.newNDEFTextRecord(inputStr, Locale.getDefault(), true);

            // Save the new input record into database
            msg = new NdefMessage(new NdefRecord[] {
                ndefRecord
            });
            addNewRecordToDB(msg, inputStr, 1);
        } else {
            Utils.loge(TAG, "Input text view is empty.");
        }
        Utils.logd(TAG, "<--getNdefRecord()");
        return msg;
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_text, null);

        NdefRecord record = msg.getRecords()[0];
        String contentStr = Utils.getTextRecordContent(record);

        if (mReadContentView != null) {
            TextView contentView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_text_content);
            if (contentView != null) {
                if (contentStr == null) {
                    contentStr = "";
                }
                contentView.setText(contentStr);
            }
        }

        addNewRecordToDB(msg, contentStr, 0);

        return mReadContentView;
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryUriId = uriId;
        mHistoryContentView = inflater.inflate(R.xml.history_view_text, null);
        if (mHistoryContentView != null) {
            TextView contentView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_text_content);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String contentStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_TEXT));
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    contentView.setText(contentStr);
                } else {
                    Utils.loge(TAG, "Fail to get text history record with _id:" + uriId);
                }
                cursor.close();
            }
        }
        return mHistoryContentView;
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
    public boolean handleHistoryReadTag() {
        sActivity.finish();
        return super.handleHistoryReadTag();
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param text text content
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String text, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), text=" + text);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_TEXT);
        selectionBuilder.append(" AND " + DB_COLUMN_TEXT + "=\'" + Utils.encodeStrForDB(text)
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
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_TEXT);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_TEXT, text);
            values.put(TagContract.COLUMN_HISTORY_TITLE, text);

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
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_text;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_text_title;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_text_summary;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_text;
    }

    @Override
    public boolean handleReadTag() {
        sActivity.finish();
        return true;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_text;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_text;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_TEXT;
    }

}
