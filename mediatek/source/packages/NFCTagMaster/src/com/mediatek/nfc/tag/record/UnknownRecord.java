
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.utils.Utils;

/**
 * Used for current unsupported record
 * 
 * @author MTK80906
 */
public class UnknownRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/UnknownRecord";

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // For tag info reader
    private View mReadLayoutView = null;

    private TextView mReadDescView = null;

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create unknown record instance now");
            sRecord = new UnknownRecord();
        }
        sActivity = activity;
        return sRecord;
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

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadLayoutView = inflater.inflate(R.xml.read_view_unknown, null);

        String contentStr0 = new String(payload, 0, payload.length - 1);
        String contentStr1 = new String(payload, 1, payload.length - 1);

        StringBuffer buffer = new StringBuffer();
        buffer.append("TNF: " + tnf + "\n");
        buffer.append("Type: " + new String(type) + "\n");
        buffer.append("Payload[0]: " + payload[0] + "\n");
        buffer.append("Payload[1~ ]: " + contentStr1 + "\n");
        buffer.append("Payload[0~ ]: " + contentStr0 + "\n");

        String descStr = buffer.toString();
        Utils.logd(TAG, "Unknown tag content: \n" + descStr);

        if (mReadLayoutView != null) {
            mReadDescView = (TextView) mReadLayoutView.findViewById(R.id.read_info_unknown_content);
            if (mReadDescView != null) {
                mReadDescView.setText(descStr);
            }
        }
        return mReadLayoutView;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_unknown;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        // return R.string.read_view_type_info_unknown_summary;
        return 0;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_unknown_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_UNKNOWN;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_unknown;
    }

    @Override
    public boolean handleReadTag() {
        sActivity.finish();
        return true;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return 0;
    }

    @Override
    public View getEditView() {
        return null;
    }

    @Override
    public NdefMessage getHistoryNdefMessage(int uriId) {
        return null;
    }

    @Override
    public View getHistoryView(int uriId) {
        return null;
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        return null;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return 0;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        return null;
    }

}
