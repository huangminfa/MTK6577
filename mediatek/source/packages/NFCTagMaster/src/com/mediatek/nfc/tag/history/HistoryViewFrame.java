
package com.mediatek.nfc.tag.history;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.record.ParsedNdefRecord;
import com.mediatek.nfc.tag.utils.Utils;
import com.mediatek.nfc.tag.write.WriteTagActivity;

/**
 * This class will show main record info stored in database. User can write this
 * record into tag, or re-make it to take effect
 */
public class HistoryViewFrame extends Activity {
    private static final String TAG = Utils.TAG + "/HistoryViewFrame";

    private ImageView mHistoryIconView = null;

    private TextView mHistoryLabelTitleView = null;

    private LinearLayout mSubContentView = null;

    private LinearLayout mHistoryBtnLayout = null;

    private TextView mHistoryBtnTextView = null;

    private int mTagType;

    private int mRecordId;

    private ParsedNdefRecord mRecord;

    private int mTagIsCreatedByMe = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logi(TAG, "-->onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_view_frame);
        mTagIsCreatedByMe = getIntent().getIntExtra(TagContract.COLUMN_IS_CREATED_BY_ME, 0);

        mHistoryIconView = (ImageView) findViewById(R.id.history_tag_icon);
        mHistoryLabelTitleView = (TextView) findViewById(R.id.history_tag_label_title);
        mSubContentView = (LinearLayout) findViewById(R.id.history_sub_content);
        mHistoryBtnLayout = (LinearLayout) findViewById(R.id.history_btn_view);
        mHistoryBtnTextView = (TextView) findViewById(R.id.history_btn_text);
        mHistoryBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagIsCreatedByMe == 1) {
                    handleWriteHistoryTag();
                } else {
                    mRecord.handleHistoryReadTag();
                }
            }
        });
        parseIntent();

        View historyView = mRecord.getHistoryView(mRecordId);
        if (mSubContentView != null) {
            mSubContentView.removeAllViews();
            if (historyView != null) {
                historyView.setBackgroundResource(R.drawable.bg_info);
                mSubContentView.addView(historyView, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            } else {
                Utils.loge(TAG, "Fail to get sub-content view for showing history record.");
            }
        }

        // Change action button text
        int actionStrRes = mRecord.getTagReaderActionStrResId();
        if (actionStrRes <= 0 || mTagIsCreatedByMe == 1) {
            Utils.logw(TAG, "Fail to get tag special action button string, use default one.");
            actionStrRes = R.string.text_write_btn;
            setTitle(R.string.history_view_label_write);
        } else {
            setTitle(R.string.history_view_label_read);
        }
        mHistoryBtnTextView.setText(actionStrRes);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Utils.logd(TAG, "-->onNewIntent()");
        parseIntent();
    }

    private void parseIntent() {
        Utils.logd(TAG, "--parseIntent()");
        Intent intent = getIntent();
        mRecordId = intent.getIntExtra("record_id", -1);
        mTagType = intent.getIntExtra("tag_type", Utils.TAG_TYPE_UNKNOWN);
        Utils.logd(TAG, " Tag type=" + mTagType + ", record Uri _id=" + mRecordId);

        if (mTagType == Utils.TAG_TYPE_UNKNOWN && mRecordId > 0) {
            Utils.logi(TAG, "Unknown tag type for history record [uri=" + mRecordId
                    + "], re-get it from db");
            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, mRecordId);
            Cursor cursor = getContentResolver().query(uri, new String[] {
                TagContract.COLUMN_TYPE
            }, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mTagType = cursor.getInt(0);
                }
                cursor.close();
            }
        }
        if (mTagType == Utils.TAG_TYPE_UNKNOWN) {
            Utils.loge(TAG, "Fail to get tag info from tag history record");
            return;
        }

        mRecord = ParsedNdefRecord.getRecordInstance(this, mTagType);

        if (mHistoryIconView != null) {
            mHistoryIconView.setImageResource(mRecord.getTagReaderTypeIconResId());
        }
        if (mHistoryLabelTitleView != null) {
            mHistoryLabelTitleView.setText(mRecord.getTagReaderTypeTitleResId());
        }
    }

    private void handleWriteHistoryTag() {
        Utils.logd(TAG, "-->handleWriteHistoryTag(), history uri id=" + mRecordId);
        NdefMessage ndefMsg = mRecord.getHistoryNdefMessage(mRecordId);
        if (ndefMsg != null) {
            Intent intent = new Intent(this, WriteTagActivity.class);
            intent.putExtra("ndef_message", ndefMsg);
            startActivity(intent);
        } else {
            Utils.loge(TAG, "Fail to get ndef message");
        }
    }
}
