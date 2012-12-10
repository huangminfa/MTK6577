package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.utils.Utils;
import com.mediatek.nfc.tag.write.TagTypeList;

import java.util.Arrays;

/**
 * Used for empty NDEF record
 */
public class EmptyRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/EmptyRecord";

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // For tag info reader
    private View mReadLayoutView = null;

    private TextView mReadTagTypeView = null;

    private TextView mReadTagSizeView = null;

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create empty record instance now");
            sRecord = new EmptyRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    public static int getTagTypeTitleResId() {
        return R.string.read_view_type_info_empty_title;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        return null;
    }

    @Override
    public View getEditView() {
        return null;
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
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
        if (tnf != NdefRecord.TNF_MIME_MEDIA
                || !Arrays.equals(type, Utils.MIME_TYPE_EMPYT_TAG.getBytes())) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        String payloadStr = new String(payload);
        String[] payloadArr = payloadStr.split(Utils.SEPARATOR_EMPTY_TAG_TYPE_SIZE);
        if (payloadArr == null || payloadArr.length != 2) {
            Utils.loge(TAG, "Invalid payload [" + payloadStr + "]");
            return null;
        } else {
            Utils.logd(TAG, "Empty tag type=" + payloadArr[0] + ", size=" + payloadArr[1]);
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadLayoutView = inflater.inflate(R.xml.read_view_empty, null);

        if (mReadLayoutView != null) {
            mReadTagTypeView = (TextView) mReadLayoutView.findViewById(R.id.read_info_empty_type);
            if (mReadTagTypeView != null) {
                mReadTagTypeView.setText(payloadArr[0]);
            }
            mReadTagSizeView = (TextView) mReadLayoutView.findViewById(R.id.read_info_empty_size);
            if (mReadTagSizeView != null) {
                mReadTagSizeView.setText(payloadArr[1]);
            }
        }
        return mReadLayoutView;
    }

    @Override
    public View getHistoryView(int uriId) {
        return null;
    }

    @Override
    public NdefMessage getHistoryNdefMessage(int uriId) {
        return null;
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        return null;
    }

    @Override
    public boolean handleReadTag() {
        Intent intent = new Intent(sActivity, TagTypeList.class);
        sActivity.startActivity(intent);
        sActivity.finish();
        return true;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_empty;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_empty_title;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return 0;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_empty;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return 0;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return 0;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_EMPTY;
    }
}
