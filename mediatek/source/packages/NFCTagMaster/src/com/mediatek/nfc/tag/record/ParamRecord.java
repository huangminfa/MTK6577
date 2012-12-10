
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.record.param.AirplaneModeEnablerParam;
import com.mediatek.nfc.tag.record.param.AudioProfileParam;
import com.mediatek.nfc.tag.record.param.AutoRotateParam;
import com.mediatek.nfc.tag.record.param.BTEnablerParam;
import com.mediatek.nfc.tag.record.param.DataConnEnablerParam;
import com.mediatek.nfc.tag.record.param.ParamItem;
import com.mediatek.nfc.tag.record.param.WifiEnablerParam;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParamRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/ParamRecord";

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    public static ParamItem[] sSupportedParamItems = null;

    // For tag info editor
    private View mEditLayoutView = null;

    // For history tag view
    private View mHistoryLayoutView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    private List<ParamItem> mHistoryItems = new ArrayList<ParamItem>();

    // For tag info read
    private View mReadLayoutView = null;

    private List<ParamItem> mReadItems = new ArrayList<ParamItem>();

    // Tag special column
    private static final String DB_COLUMN_PARAM = TagContract.COLUMN_01;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_PARAM,
            TagContract.COLUMN_BYTES
    };

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create parameters record instance now");
            sRecord = new ParamRecord();
        }
        sActivity = activity;
        sSupportedParamItems = new ParamItem[] {
                WifiEnablerParam.getInstance(sActivity), BTEnablerParam.getInstance(sActivity),
                DataConnEnablerParam.getInstance(sActivity),
                AudioProfileParam.getInstance(sActivity), AutoRotateParam.getInstance(sActivity),
                AirplaneModeEnablerParam.getInstance(sActivity)
        };
        return sRecord;
    }

    @Override
    public View getEditView() {
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditLayoutView = inflater.inflate(R.xml.edit_view_param, null);
        LinearLayout subContentLayout = (LinearLayout) mEditLayoutView
                .findViewById(R.id.sub_content_layout);
        subContentLayout.removeAllViews();
        for (int i = 0; i < sSupportedParamItems.length; i++) {
            View subView = sSupportedParamItems[i].getEditItemView();
            if (subView != null) {
                if (i > 0) { // add a item separator
                    inflater.inflate(R.layout.item_divider, subContentLayout);
                }
                subContentLayout.addView(subView, params);
            } else {
                Utils.loge(TAG, "Subview at " + i + " is null");
            }
        }

        return mEditLayoutView;
    }

    @Override
    public NdefMessage getHistoryNdefMessage(int uriId) {
        Utils.logd(TAG, "-->getHistoryNdefMessage()");

        ParamItem[] itemArray = mHistoryItems.toArray(new ParamItem[0]);

        return getNdefMessageFromPage(itemArray, false);
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        mHistoryItems.clear();
        mHistoryUriId = uriId;
        mHistoryLayoutView = LayoutInflater.from(sActivity).inflate(R.xml.read_view_param, null);
        LinearLayout subContentLayout = (LinearLayout) mHistoryLayoutView
                .findViewById(R.id.sub_content_layout);
        subContentLayout.removeAllViews();
        LayoutParams layoutPparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);

        String paramStr = null;
        Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
        Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                paramStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_PARAM));
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                try {
                    mHistoryMessage = new NdefMessage(bytes);
                } catch (FormatException e) {
                    Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                    e.printStackTrace();
                }
            } else {
                Utils.loge(TAG, "Fail to get parameters history record with id:" + uriId);
            }
            cursor.close();
        }

        Utils.logi(TAG, "Parameters string from hsitory DB = " + paramStr);
        String[] paramArray = paramStr.split("&");
        for (String param : paramArray) { // get a view for each parameter item
            boolean found = false;
            for (ParamItem item : sSupportedParamItems) {
                if (item.match(param)) {
                    found = true;
                    mHistoryItems.add(item);
                    subContentLayout.addView(item.getHistoryItemView(param), layoutPparams);
                    break;
                }
            }
            if (!found) {
                Utils.loge(TAG, "Unsupported param: " + param);
            }
        }
        return mHistoryLayoutView;
    }

    @Override
    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        boolean result = true;
        for (ParamItem item : mHistoryItems) {
            if (!item.enableParam(mHandler)) {
                result = false;
            }
        }
        // Give out a progress dialog first. When this progress dialog
        // disappear, mean enable finish.
        // This lines can avoid no parameter need to enable case
        mHandler.obtainMessage(ParamRecord.MSG_BEGIN_WAIT).sendToTarget();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, ParamRecord.MSG_END_WAIT), 500);
        return result;
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        return getNdefMessageFromPage(sSupportedParamItems, true);
    }

    /**
     * Used for edit view, history view and read view
     * 
     * @param paramItems Items contains in this page
     * @param needSave Whether this item is a new one that need to insert into
     *            DB
     * @return
     */
    private NdefMessage getNdefMessageFromPage(ParamItem[] paramItems, boolean needSave) {
        Utils.logd(TAG, "-->getNdefMessageFromPage");
        StringBuffer buffer = new StringBuffer();
        StringBuffer labelBuf = new StringBuffer();
        for (ParamItem item : paramItems) {
            String paramStr = item.getParamStr();
            String labelStr = item.getLabel();
            if (!TextUtils.isEmpty(paramStr)) {
                if (buffer.length() != 0) {
                    buffer.append("&");
                }
                buffer.append(paramStr);

                if (labelBuf.length() != 0 && !TextUtils.isEmpty(labelStr)) {
                    labelBuf.append(", ");
                }
                labelBuf.append(labelStr);
            }
        }
        if (buffer.length() == 0) {
            Utils.loge(TAG, "No param item is selected.");
            Toast.makeText(sActivity, R.string.error_param_empty_content, Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
        String payloadStr = buffer.toString();

        Utils.logd(TAG, " payloadStr=" + payloadStr);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, Utils.MIME_TYPE_PARAM_TAG
                .getBytes(), new byte[0], payloadStr.getBytes());
        // Save the new input record into database
        NdefMessage msg = new NdefMessage(new NdefRecord[] {
            record
        });
        if (needSave) {
            addNewRecordToDB(msg, payloadStr, labelBuf.toString(), 1);
        }
        return msg;
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param paramStr selected parameters string
     * @param labelStr introduction for this parameter string
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String paramStr, String labelStr, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), paramStr=" + paramStr + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_PARAM);
        selectionBuilder.append(" AND " + DB_COLUMN_PARAM + "=\'" + paramStr + "\' ");
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
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_PARAM);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_PARAM, paramStr);
            values.put(TagContract.COLUMN_HISTORY_TITLE, labelStr);

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
        LayoutParams layoutPparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        mReadLayoutView = LayoutInflater.from(sActivity).inflate(R.xml.read_view_param, null);
        LinearLayout subContentLayout = (LinearLayout) mReadLayoutView
                .findViewById(R.id.sub_content_layout);
        subContentLayout.removeAllViews();
        mReadItems.clear();

        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();

        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }
        String paramStr = new String(payload);
        StringBuffer labelBuf = new StringBuffer();
        Utils.logi(TAG, "Parameters string from new read tag = " + paramStr);
        if (tnf == NdefRecord.TNF_MIME_MEDIA
                && Arrays.equals(type, Utils.MIME_TYPE_PARAM_TAG.getBytes())) {
            String[] paramArray = paramStr.split("&");
            for (String param : paramArray) { // get a view for each parameter
                // item
                boolean found = false;
                for (ParamItem item : sSupportedParamItems) {
                    if (item.match(param)) {
                        found = true;
                        String labelStr = item.getLabel();
                        if (labelBuf.length() != 0 && !TextUtils.isEmpty(labelStr)) {
                            labelBuf.append(", ");
                        }
                        labelBuf.append(labelStr);
                        mReadItems.add(item);
                        subContentLayout.addView(item.getReadItemView(param), layoutPparams);
                        break;
                    }
                }
                if (!found) {
                    Utils.loge(TAG, "Unsupported param: " + param);
                }
            }
        } else {
            Utils.loge(TAG, "Not a parameter tag");
            return null;
        }

        addNewRecordToDB(msg, paramStr, labelBuf.toString(), 0);

        return mReadLayoutView;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_param;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_param;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_param;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_param;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_param_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_param_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_PARAM;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_param,
                sActivity.getString(R.string.tag_title_param));
        pref.setTagType(Utils.TAG_TYPE_PARAM);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");
        boolean result = true;
        for (ParamItem item : mReadItems) {
            if (!item.enableParam(mHandler)) {
                result = false;
            }
        }
        // Give out a progress dialog first. When this progress dialog
        // disappear, mean enable finish.
        // This lines can avoid no parameter need to enable case
        mHandler.obtainMessage(ParamRecord.MSG_BEGIN_WAIT).sendToTarget();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, ParamRecord.MSG_END_WAIT), 500);

        return result;
    }

    /**
     * Enable parameters may take some time, show a progress dialog to wait here
     */
    private ProgressDialog mProgressDialog = null;

    private int mWaitingParamCount = 0;

    public static final int MSG_BEGIN_WAIT = 1;

    public static final int MSG_END_WAIT = 2;

    public static final int MSG_WAIT_WIFI_TIMEOUT = 11;

    public static final int MSG_WAIT_BT_TIMEOUT = 12;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            int what = msg.what;
            Utils.logv(TAG, " Receive a messge: " + what + ", mWaitingParamCount="
                    + mWaitingParamCount);
            switch (what) {
                case MSG_BEGIN_WAIT:
                    increaseProgressDialog(what);
                    break;
                case MSG_END_WAIT:
                case MSG_WAIT_WIFI_TIMEOUT:
                case MSG_WAIT_BT_TIMEOUT:
                    Object receiverObj = msg.obj;
                    if (receiverObj instanceof BroadcastReceiver) {
                        // There is a broadcast receiver coming with this
                        // message
                        // It used to monitor parameter enable result, but
                        // timeout now,
                        // so we need to unregister this receiver here to avoid
                        // exception
                        Utils
                                .logw(TAG,
                                        "Some BroadcastReceiver has not been unregister, do it now");
                        try {
                            sActivity.unregisterReceiver((BroadcastReceiver) receiverObj);
                        } catch (ClassCastException e) {
                            Utils.loge(TAG, "Unregister former broadcast receiver fail", e);
                        }
                    }
                    decreaseProgressDialog(what);
                    break;
                default:
                    Utils.loge(TAG, "Unsupported message");
                    break;
            }
        };
    };

    private void increaseProgressDialog(int reason) {
        Utils.logd(TAG, "-->increaseProgressDialog(), reason=" + reason);
        mWaitingParamCount++;
        if (mWaitingParamCount > 0) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(sActivity);
                mProgressDialog.setMessage(sActivity.getResources().getString(
                        R.string.param_progress_enable_param));
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    private void decreaseProgressDialog(int reason) {
        Utils.logd(TAG, "-->decreaseProgressDialog(), reason=" + reason);
        mWaitingParamCount--;
        if (mWaitingParamCount <= 0) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = null;
            }

            // Finish activity when enable parameter finish
            sActivity.finish();
        }
    }
}
