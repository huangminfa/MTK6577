package com.mediatek.nfc.tag;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.nfc.tag.record.ParsedNdefRecord;
import com.mediatek.nfc.tag.utils.Utils;

public class TagReaderFrame extends Activity {
    private static final String TAG = Utils.TAG + "/TagReader";

    private LinearLayout mSubContentView = null;

    private ImageView mTypeIconView = null;

    private TextView mTypeLabelTitleView = null;

    private TextView mTypeLabelSummaryView = null;

    private LinearLayout mReadBtnLayout = null;

    private TextView mReadBtnTextView = null;

    private int mTagType;

    private ParsedNdefRecord mRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tag_read_frame);
        mSubContentView = (LinearLayout) findViewById(R.id.sub_content);
        mTypeIconView = (ImageView) findViewById(R.id.read_tag_icon);
        mTypeLabelTitleView = (TextView) findViewById(R.id.tag_read_label_title);
        mTypeLabelSummaryView = (TextView) findViewById(R.id.tag_read_label_summary);
        mReadBtnLayout = (LinearLayout) findViewById(R.id.read_btn_view);
        mReadBtnTextView = (TextView) findViewById(R.id.read_btn_text);

        mTypeIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleReadTag();
            }
        });
        mReadBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleReadTag();
            }
        });

        parseIntent(getIntent());

        if (mRecord != null && mReadBtnTextView != null) {
            int actionStrRes = mRecord.getTagReaderActionStrResId();
            if (actionStrRes > 0) {
                mReadBtnTextView.setText(actionStrRes);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        parseIntent(intent);
    }
    
    @Override
    protected void onResume() {
        //Since tag read page and tag write page may use the same tag record instance,
        //when this page come back, update the context object in tag record
        mRecord = ParsedNdefRecord.getRecordInstance(this, mTagType);
        super.onResume();
    }

    private void parseIntent(Intent intent) {
        String action = intent.getAction();
        Utils.logd(TAG, "-->parseIntent(), action=" + action);
        Utils.logd(TAG, "-->parseIntent(), type=" + intent.getType() + ", schema="
                + intent.getScheme());

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                Utils.logw(TAG, "rawMsgs size=" + rawMsgs.length);
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // TODO only support NDEF tag now
                Utils.loge(TAG, "Unknown tag type, only support NDEF tag now");
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }
            Utils.logd(TAG, "NdefMessage size=" + msgs.length);

            // Empty tag, compose a self-define empty tag for it
            if (msgs.length == 0) {
                Utils.logw(TAG, "Encount an empty tag.");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                String tagInfoStr = getEmptyTagInfo(tag);

                byte[] empty = new byte[] {};
                byte[] payload = tagInfoStr.getBytes();

                NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                        Utils.MIME_TYPE_EMPYT_TAG.getBytes(), empty, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }

            // just deal with the first one
            NdefRecord record = msgs[0].getRecords()[0];

            mTagType = Utils.getTagType(record);
            Utils.logi(TAG, "Tag type = " + mTagType);

            mRecord = ParsedNdefRecord.getRecordInstance(this, mTagType);
            View readView = mRecord.getPreview(msgs[0]);
            if (readView != null) {
                mSubContentView.removeAllViews();
                mSubContentView.addView(readView, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            } else {
                Utils.loge(TAG, "Fail to get sub-view for showing tag info. Tag type=" + mTagType);
            }

            // set tag type info
            if (mTypeIconView != null) {
                mTypeIconView.setImageResource(mRecord.getTagReaderTypeIconResId());
            }
            if (mTypeLabelTitleView != null) {
                mTypeLabelTitleView.setText(mRecord.getTagReaderTypeTitleResId());
            }
            if (mTypeLabelSummaryView != null) {
                int resId = mRecord.getTagReaderTypeSummaryResId();
                if (resId <= 0) {
                    mTypeLabelSummaryView.setVisibility(View.GONE);
                } else {
                    mTypeLabelSummaryView.setText(resId);
                    mTypeLabelSummaryView.setVisibility(View.VISIBLE);
                }
            }
        }
        autoLaunchApp();
    }

    private void autoLaunchApp() {
        boolean needAutoLaunch = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Utils.KEY_AUTO_LAUNCH_APP, Utils.DEFAULT_VALUE_AUTO_LAUNCH_APP);
        Utils.logd(TAG, "-->autoLaunchApp(),  Need to auto launch app?" + needAutoLaunch
                + ", tagType=" + mTagType);
        if (needAutoLaunch) {
            switch (mTagType) {
                case Utils.TAG_TYPE_APP:
                case Utils.TAG_TYPE_EMAIL:
                case Utils.TAG_TYPE_MMS:
                case Utils.TAG_TYPE_PARAM:
                case Utils.TAG_TYPE_PHONE_NUM:
                case Utils.TAG_TYPE_SMS:
                case Utils.TAG_TYPE_URL:
                case Utils.TAG_TYPE_VCARD:
                case Utils.TAG_TYPE_VEVENT:
                    Utils.logd(TAG, "Start related application automaticall for tagType: "
                            + mTagType);
                    handleReadTag();
                    break;
                default:
                    // Unknown, text, empty
                    Utils.logw(TAG, "TagType:" + mTagType + " does not support auto launch yet");
                    break;
            }
        }
    }

    /**
     * Get empty tag type and size info
     * 
     * @param tag
     * @return
     */
    private String getEmptyTagInfo(Tag tag) {
        Utils.logd(TAG, "-->getEmptyTagInfo()");
        String type = getString(R.string.read_view_empty_type_unknown);
        int size = 0;
        if (tag == null) {
            Utils.logw(TAG, "Could not parse null object.");
        } else {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                type = ndef.getType();
                size = ndef.getMaxSize();
            } else {
                Utils.loge(TAG, " Fail to get NDEF instance.");
            }
        }
        return type + Utils.SEPARATOR_EMPTY_TAG_TYPE_SIZE + size;
    }

    private void handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");
        mRecord.handleReadTag();
        Utils.logd(TAG, "<--handleReadTag()");
    }

}
