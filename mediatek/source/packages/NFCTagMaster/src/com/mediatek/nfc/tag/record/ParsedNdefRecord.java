package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Utils;

public abstract class ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/ParsedNdefRecord";

    /**
     * Get preference that will show tag info of the available tag list
     * 
     * @return
     */
    public abstract TagTypePreference getTagTypePreference();

    /**
     * Get the view of editing input tag info area
     * 
     * @return
     */
    public abstract View getEditView();

    /**
     * After scanning a tag, info in it will be shown in a preview area stand by
     * this view
     * 
     * @return
     */
    public abstract View getPreview(NdefMessage msg);

    /**
     * Get a history information view. The information is stored in database,
     * whose _id is uriId
     * 
     * @param uriId
     * @return
     */
    public abstract View getHistoryView(int uriId);

    /**
     * Get the NdefMessage stored in tag history content provider. This message
     * can be written into tag directly
     * 
     * @return
     */
    public abstract NdefMessage getHistoryNdefMessage(int uriId);

    /**
     * Get the NdefMessage stored in tag history content provider. This message
     * can be written into tag directly This method will get data from content
     * provider directly
     * 
     * @return
     */
    public NdefMessage getHistoryNdefMessage(Context context, int uriId) {
        Utils.logd(TAG, "-->getHistoryNdefMessage(), uriId=" + uriId);
        NdefMessage historyMsg = null;
        Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
        Cursor cursor = context.getContentResolver().query(uri, new String[] {
                "", ""
        }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                try {
                    historyMsg = new NdefMessage(bytes);
                } catch (FormatException e) {
                    Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                    e.printStackTrace();
                }
            } else {
                Utils.loge(TAG, "Fail to get pure phone number history record with id:" + uriId);
            }
        }
        return historyMsg;
    }

    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", do nothing");
    }

    public void onCreateOptionsMenu(Menu menu) {
    }

    public void onOptionsItemSelected(MenuItem item) {
    }

    /**
     * Get the new edited NDEF record which will be written into Tag
     * 
     * @return
     */
    public abstract NdefMessage getNewNdefMessage();

    /**
     * Get a single instance object of Tag
     * 
     * @param context
     * @param type
     * @return
     */
    public static ParsedNdefRecord getRecordInstance(Activity activity, int type) {
        ParsedNdefRecord record;
        switch (type) {
            case Utils.TAG_TYPE_TEXT:
                record = TextRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_PHONE_NUM:
                record = PhoneNumberRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_VCARD:
                record = VCardRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_VEVENT:
                record = VEventRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_SMS:
                record = SMSRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_URL:
                record = URLRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_EMAIL:
                record = EmailRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_PARAM:
                record = ParamRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_APP:
                record = AppRecord.getInstance(activity);
                break;
            case Utils.TAG_TYPE_EMPTY:
                record = EmptyRecord.getInstance(activity);
                break;
            default:
                loge("Unsupported tag type");
                record = UnknownRecord.getInstance(activity);
                break;
        }
        return record;
    }

    /**
     * After read and preview a NFC tag, make the next operation on it(store,
     * make a call, open URL or what ever)
     * 
     * @return true for success, false for fail
     */
    public abstract boolean handleReadTag();

    /**
     * When viewing a read tag from history list, this operation will be adopter
     * if user click action button
     * 
     * @return
     */
    public boolean handleHistoryReadTag() {
        Utils.loge(TAG, "Do nothing in super class.");
        return false;
    };

    /**
     * Get a icon resource id stand for the new read tag
     * 
     * @return
     */
    public abstract int getTagReaderTypeIconResId();

    /**
     * Get a string resource id stand for the new read tag title
     * 
     * @return
     */
    public abstract int getTagReaderTypeTitleResId();

    /**
     * Get a string resource id stand for the new read tag title
     * 
     * @return
     */
    public abstract int getTagReaderTypeSummaryResId();

    /**
     * After scanning a NFC tag, preview it first. In the preview page, user can
     * click the bottom action button to dealing with the new tag. This method
     * will return the string shown in the action button
     * 
     * @return
     */
    public abstract int getTagReaderActionStrResId();

    /**
     * Get a icon resource id, it will be shown as a icon in tag history list
     * item
     * 
     * @return
     */
    public abstract int getTagHistoryItemIconResId();

    /**
     * Get a tag summary resource id, it will be shown as an introduction in tag
     * history list item
     * 
     * @return
     */
    public abstract int getTagHistoryItemSummaryResId();

    /**
     * Get tag type of this record instance
     * 
     * @return
     */
    public abstract int getTagType();

    public static void log(String msg) {
        Utils.logd(TAG, msg);
    }

    public static void loge(String msg) {
        Utils.loge(TAG, msg);
    }
}
