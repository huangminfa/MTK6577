
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Bookmark;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.Arrays;
import java.util.Locale;

public class URLRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/URLRecord";

    private static final int REQUEST_CODE_BOOKMARK = 1;

    private static ParsedNdefRecord sInstance;

    private static Activity sActivity;

    // Tag special column
    private static final String DB_COLUMN_DESC = TagContract.COLUMN_01;

    private static final String DB_COLUMN_ADDR = TagContract.COLUMN_02;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_DESC, DB_COLUMN_ADDR,
            TagContract.COLUMN_BYTES
    };

    // For tag info editor
    private View mEditContentView = null;

    private EditText mEditDescView;

    private EditText mEditAddressView;

    // This LinearLayout is a mock button
    private LinearLayout mSelectorFromBookMarkView;

    // For history tag view
    private View mHistoryContentView = null;

    private TextView mHistoryDescView = null;

    private TextView mHistoryDescLabelView = null;

    private TextView mHistoryAddressView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // For tag info reader
    private View mReadContentView = null;

    private TextView mReadDescView = null;

    private TextView mReadDescLabelView = null;

    private TextView mReadAddressView = null;

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sInstance == null) {
            Utils.logi(TAG, "Create URL record instance now");
            sInstance = new URLRecord();
        }
        sActivity = activity;
        return sInstance;
    }

    @Override
    public View getEditView() {
        Utils.logd(TAG, "-->getEditView()");
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_url, null);
        if (mEditContentView != null) {
            mEditDescView = (EditText) mEditContentView.findViewById(R.id.edit_info_url_desc);
            mEditAddressView = (EditText) mEditContentView.findViewById(R.id.edit_info_url_address);
            mSelectorFromBookMarkView = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_url_bookmark_selector);
            mSelectorFromBookMarkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBookmarkSelectorPage();
                }
            });
        }
        return mEditContentView;
    }

    private void startBookmarkSelectorPage() {
        Utils.logd(TAG, "-->startBookmarkSelectorPage()");
        Intent intent = new Intent(sActivity, BookmarkListActivity.class);
        sActivity.startActivityForResult(intent, REQUEST_CODE_BOOKMARK);
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);
        if (requestCode == REQUEST_CODE_BOOKMARK) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                long selectedBookmarkId = data
                        .getLongExtra(Bookmark.EXTRA_SELECTED_BOOKMARK_ID, -1);
                Utils.logd(TAG, " Selected bookmark id=" + selectedBookmarkId);
                if (selectedBookmarkId >= 0) {
                    Cursor cursor = sActivity.getContentResolver().query(Bookmark.BOOKMARK_URI,
                            Bookmark.PROJECTION, Bookmark.COLUMN_ID + "=" + selectedBookmarkId,
                            null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        String labelStr = cursor.getString(cursor
                                .getColumnIndex(Bookmark.COLUMN_TITLE));
                        String urlStr = cursor
                                .getString(cursor.getColumnIndex(Bookmark.COLUMN_URL));
                        if (mEditDescView != null) {
                            mEditDescView.setText(labelStr);
                        }
                        if (mEditAddressView != null) {
                            mEditAddressView.setText(urlStr);
                        }
                        cursor.close();
                    } else {
                        Utils.loge(TAG, "Fail to fetch data ");
                    }
                }
            }
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
        mHistoryContentView = inflater.inflate(R.xml.history_view_url, null);
        if (mHistoryContentView != null) {
            mHistoryDescView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_url_desc);
            mHistoryDescLabelView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_url_desc_label);
            mHistoryAddressView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_url_address);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String descStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_DESC));
                    String addressStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_ADDR));
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    if (mHistoryDescView != null) {
                        mHistoryDescView.setText(descStr);
                        if (TextUtils.isEmpty(descStr)) {
                            mHistoryDescView.setVisibility(View.GONE);
                            mHistoryDescLabelView.setVisibility(View.GONE);
                        } else {
                            mHistoryDescView.setVisibility(View.VISIBLE);
                            mHistoryDescLabelView.setVisibility(View.VISIBLE);
                        }
                    }
                    if (mHistoryAddressView != null) {
                        mHistoryAddressView.setText(addressStr);
                    }
                } else {
                    Utils.loge(TAG, "Fail to get URL history record with id:" + uriId);
                }
                cursor.close();
            }
        }
        return mHistoryContentView;
    }

    @Override
    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        if (mHistoryAddressView == null) {
            Utils.loge(TAG, "Fail to get address view.");
            return false;
        }
        String urlAddr = mHistoryAddressView.getText().toString();
        if (!TextUtils.isEmpty(urlAddr)) {
            startBrowser(urlAddr);
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_url_empty_addr, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_url_empty_addr));
            return false;
        }
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mEditDescView == null || mEditAddressView == null) {
            Utils.loge(TAG, "Fail to load edit page, input area is null");
            return null;
        }
        String descStr = mEditDescView.getText().toString();
        String addrStr = mEditAddressView.getText().toString();
        if (TextUtils.isEmpty(addrStr)) {
            Utils.loge(TAG, "URL is empty");
            Toast.makeText(sActivity, R.string.error_url_empty_addr, Toast.LENGTH_SHORT).show();
            return null;
        }

        boolean isValidUri = false;
        int urlType = 3; // default http://
        // This pure string has remove URL prefix like http://
        String pureAddStr = addrStr;
        for (int i = 1; i < 5; i++) {
            String prefix = Utils.URI_PREFIX_MAP.get((byte) i);
            if (addrStr.startsWith(prefix)) {
                urlType = i;
                pureAddStr = addrStr.substring(prefix.length());
                isValidUri = true;
                break;
            }
        }
        if (!isValidUri) {
            Utils.loge(TAG, "Selected URI[" + addrStr
                    + "] is not a valid/completed one, add default prefix http:// to it");

            addrStr = Utils.URI_PREFIX_MAP.get((byte) urlType) + addrStr;
        }

        byte[] bytes = pureAddStr.getBytes();
        byte[] payload = new byte[bytes.length + 1];

        payload[0] = (byte) urlType;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);

        NdefRecord urlRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
                new byte[0], payload);
        NdefMessage message = null;
        if (TextUtils.isEmpty(descStr)) {
            message = new NdefMessage(new NdefRecord[] {
                urlRecord
            });
        } else {
            NdefRecord descRecord = Utils.newNDEFTextRecord(descStr, Locale.getDefault(), true);
            NdefMessage combinedMsg = new NdefMessage(new NdefRecord[] {
                    descRecord, urlRecord
            });
            NdefRecord combinedRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_SMART_POSTER, new byte[0], combinedMsg.toByteArray());
            message = new NdefMessage(new NdefRecord[] {
                combinedRecord
            });
        }

        addNewRecordToDB(message, descStr, addrStr, 1);
        return message;
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param desc URL description string
     * @param url URL address value
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String desc, String url, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), desc=" + desc + ", url=" + url + ", isNewCreated?"
                + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_URL);
        selectionBuilder.append(" AND " + DB_COLUMN_DESC + "=\'" + Utils.encodeStrForDB(desc)
                + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_ADDR + "=\'" + Utils.encodeStrForDB(url)
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
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_URL);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_DESC, desc);
            values.put(DB_COLUMN_ADDR, url);
            String historyTitle = (TextUtils.isEmpty(desc) ? url : desc);
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
        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        String descStr = "";

        // First judge if this is a 1-level URL record or a SP record with URL
        // in it
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();
        if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type, NdefRecord.RTD_SMART_POSTER)) {
            Utils.logi(TAG, "Encounter a SP URL record, parse its second level record now");
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
                NdefRecord descRecord = Utils.getFirstRecordIfExists(recordList,
                        Utils.TAG_TYPE_TEXT);
                NdefRecord urlRecord = Utils.getOnlyExistingRecord(recordList, Utils.TAG_TYPE_URL);
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
        String prefix = Utils.URI_PREFIX_MAP.get(payload[0]);
        if (tnf != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(type, NdefRecord.RTD_URI)
                || !prefix.startsWith("http")) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_url, null);
        String contentStr = new String(payload, 1, payload.length - 1);

        String urlStr = prefix + contentStr;

        if (mReadContentView != null) {
            mReadDescView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_url_desc_content);
            mReadDescLabelView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_url_desc_content_label);
            mReadAddressView = (TextView) mReadContentView.findViewById(R.id.read_info_url_content);
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
            if (mReadAddressView != null) {
                mReadAddressView.setText(urlStr);
            }
        }

        addNewRecordToDB(msg, descStr, urlStr, 0);

        return mReadContentView;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_url;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_url;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_url;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_url;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_url_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_url_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_URL;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_url, sActivity
                .getString(R.string.tag_title_url));
        pref.setTagType(Utils.TAG_TYPE_URL);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");

        if (mReadAddressView == null) {
            Utils.loge(TAG, "Fail to get needed address view.");
            return false;
        }
        String urlAddr = mReadAddressView.getText().toString();
        if (!TextUtils.isEmpty(urlAddr)) {
            startBrowser(urlAddr);
            // Finish tag information preview page
            sActivity.finish();
            return true;
        } else {
            Toast.makeText(sActivity, R.string.error_url_empty_addr, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, sActivity.getString(R.string.error_url_empty_addr));
            return false;
        }
    }

    private void startBrowser(String urlAddr) {
        Utils.logd(TAG, "-->startBrowser(), URL = " + urlAddr);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(urlAddr);
        Utils.logi(TAG, "startBrowser, Uri=" + uri.toString());
        intent.setData(uri);
        sActivity.startActivity(intent);
        Utils.logd(TAG, "<--startBrowser()");
    }

}
