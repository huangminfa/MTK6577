
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class VEventRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/VEventRecord";

    public static final String ACTION_SELECT_CALENDAR = "android.intent.action.CALENDARCHOICE";

    private static final int REQUEST_CODE_CALENDAR = 1;

    public static final String MIME_TYPE_CALENDAR = "text/x-vcalendar";

    // Column in Calendar database
    // private static final Uri CALENDAR_CONTENT_URI =
    // CalendarContract.Events.CONTENT_URI;
    // For Android 2.3
    private static final Uri CALENDAR_CONTENT_URI = Uri
            .parse("content://com.android.calendar/events");

    private static final String CALENDAR_COLUMN_ID = "_id";

    private static final String CALENDAR_COLUMN_TITLE = "title";

    private static final String CALENDAR_COLUMN_LOCATION = "eventLocation";

    private static final String CALENDAR_COLUMN_DESCRIPTION = "description";

    private static final String CALENDAR_COLUMN_START_TIME = "dtstart";

    private static final String CALENDAR_COLUMN_END_TIME = "dtend";

    private static final String[] CALENDAR_PROJECTION = {
            CALENDAR_COLUMN_ID, CALENDAR_COLUMN_TITLE, CALENDAR_COLUMN_LOCATION,
            CALENDAR_COLUMN_DESCRIPTION, CALENDAR_COLUMN_START_TIME, CALENDAR_COLUMN_END_TIME
    };

    // Column in NFC history database
    private static final String NFC_COLUMN_TITLE = TagContract.COLUMN_01;

    private static final String NFC_COLUMN_LOCATION = TagContract.COLUMN_02;

    private static final String NFC_COLUMN_DESC = TagContract.COLUMN_03;

    private static final String NFC_COLUMN_START_TIME = TagContract.COLUMN_04;

    private static final String NFC_COLUMN_END_TIME = TagContract.COLUMN_05;

    // String expression of vEvent payload
    private static final String NFC_COLUMN_CONTENT = TagContract.COLUMN_06;

    private static final String[] NFC_PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, NFC_COLUMN_TITLE,
            NFC_COLUMN_LOCATION, NFC_COLUMN_DESC, NFC_COLUMN_START_TIME, NFC_COLUMN_END_TIME,
            NFC_COLUMN_CONTENT, TagContract.COLUMN_BYTES
    };

    private static final String VEVENT_PREFIX_TITLE = "SUMMARY";

    private static final String VEVENT_PREFIX_LOCATION = "LOCATION";

    private static final String VEVENT_PREFIX_DESCRIPTION = "DESCRIPTION";

    private static final String VEVENT_PREFIX_START_TIME = "DTSTART";

    private static final String VEVENT_PREFIX_END_TIME = "DTEND";

    private static final String VEVENT_FILE_NAME = "vevent_file.vcs";

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // This byte array stand for vEvent file content
    private byte[] mVEvent = null;

    // For tag info editor
    private View mEditContentView = null;

    private LinearLayout mSelectView = null;

    private TextView mEditTitleView;

    private TextView mEditStartTimeView;

    private TextView mEditEndTimeView;

    private TextView mEditLocationView;

    private TextView mEditDescriptionView;

    private EventInfo mEventInfo;

    // For tag history view
    private View mHistoryContentView = null;

    private TextView mHistoryTitleView = null;

    private TextView mHistoryStartTimeView = null;

    private TextView mHistoryEndTimeView = null;

    private TextView mHistoryLocationView = null;

    private TextView mHistoryDescriptionView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // For tag read view
    private View mReadContentView = null;

    private TextView mReadTitleView = null;

    private TextView mReadStartTimeView = null;

    private TextView mReadEndTimeView = null;

    private TextView mReadLocationView = null;

    private TextView mReadDescriptionView = null;

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create vCard record instance now");
            sRecord = new VEventRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    /**
     * This class will be used to cache calendar event info
     */
    class EventInfo {
        public String mTitle;

        public String mLocation;

        public String mDescription;

        public String mStartTime; // In the form of mill-second

        public String mEndTime; // In the form of mill-second

        public EventInfo(String titleStr, String locStr, String descStr, String startStr,
                String endStr) {
            mTitle = titleStr;
            mLocation = locStr;
            mDescription = descStr;
            mStartTime = startStr;
            mEndTime = endStr;
        }

        @Override
        public String toString() {
            return "[ Title = " + mTitle + ",\n Location = " + mLocation + ",\n Description = "
                    + mDescription + ",\n startTime = " + mStartTime + " ("
                    + Utils.translateTime(Long.valueOf(mStartTime)) + ")" + ",\n endTime = "
                    + mEndTime + " (" + Utils.translateTime(Long.valueOf(mEndTime)) + ") ]\n";
        }
    }

    @Override
    public View getEditView() {
        mVEvent = null;
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_vevent, null);
        if (mEditContentView != null) {
            mSelectView = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_vevent_selector);
            mEditTitleView = (TextView) mEditContentView.findViewById(R.id.edit_info_vevent_title);
            mEditStartTimeView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_vevent_start_time);
            mEditEndTimeView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_vevent_end_time);
            mEditLocationView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_vevent_location);
            mEditDescriptionView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_vevent_description);

            if (mSelectView != null) {
                mSelectView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSelectCalendarPage();
                    }
                });
            }
        }

        // Start vEvent select page directly
        startSelectCalendarPage();

        return mEditContentView;
    }

    private void startSelectCalendarPage() {
        Utils.logd(TAG, "-->startSelectCalendarPage()");
        // Start calendar event select page
        Intent intent = new Intent(ACTION_SELECT_CALENDAR);
        intent.setType(MIME_TYPE_CALENDAR);
        intent.putExtra("request_type", 0);// Special for GB, not needed for ICS
        // any more
        sActivity.startActivityForResult(intent, REQUEST_CODE_CALENDAR);

        Utils.logd(TAG, "<--startSelectCalendarPage()");
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);
        if (requestCode == REQUEST_CODE_CALENDAR) {
            // For Android2.3, after select a calendar event, result code still
            // be 0
            if (/* resultCode == Activity.RESULT_OK && */data != null) {
                Uri lookupUri = data.getData();
                Utils.logd(TAG, "Selected calendar uri=" + lookupUri);
                // get contact detail info and vCard bytes according to contact
                // uri
                if (lookupUri != null) {
                    AssetFileDescriptor descriptor = null;
                    FileInputStream in = null;

                    try {
                        descriptor = sActivity.getContentResolver().openAssetFileDescriptor(
                                lookupUri, "r");
                        mVEvent = new byte[(int) descriptor.getLength()];
                        in = descriptor.createInputStream();
                        in.read(mVEvent);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        mVEvent = null;
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (mVEvent != null) {
                        Utils.logi(TAG, "Selected contact event content=\n[[\n"
                                + new String(mVEvent) + "\n]]\n");
                    }

                    // need to fill UI now
                    findOutCalendarDetailInfo(ContentUris.parseId(lookupUri));
                }
            } else {
                Utils.logw(TAG,
                        "-->onActivityResultCallback(), no calendar event is selected. resultCode="
                                + resultCode + ", data=null?" + (data == null));
                sActivity.finish();
            }
        }
    }

    /**
     * Get event detail info from calendar data base TODO maybe we should get
     * these infos
     * 
     * @param eventId
     */
    private void findOutCalendarDetailInfo(long eventId) {
        Utils.logd(TAG, "-->findOutCalendarDetailInfo(), eventId=" + eventId);
        mEventInfo = new EventInfo("", "", "", "", "");
        String selectionStr = CALENDAR_COLUMN_ID + "=" + String.valueOf(eventId);
        Cursor calendarCursor = sActivity.managedQuery(CALENDAR_CONTENT_URI, CALENDAR_PROJECTION,
                selectionStr, null, null);
        if (calendarCursor != null) {
            if (calendarCursor.moveToFirst()) {
                mEventInfo = loadEventInfoFromCursor(calendarCursor, CALENDAR_COLUMN_TITLE,
                        CALENDAR_COLUMN_LOCATION, CALENDAR_COLUMN_DESCRIPTION,
                        CALENDAR_COLUMN_START_TIME, CALENDAR_COLUMN_END_TIME);
                Utils.logi(TAG, "User selected event:\n" + mEventInfo.toString());
            } else {
                Utils.loge(TAG, "Could not find a event record from calendar DB whose _id="
                        + eventId);
            }
        } else {
            Utils.loge(TAG, "Fail to get calendar event(_id=" + eventId + ") from calendar DB.");
        }

        if (mEditTitleView != null) {
            mEditTitleView.setText(mEventInfo.mTitle);
        }
        if (mEditStartTimeView != null) {
            mEditStartTimeView.setText(Utils.translateTime(Long.valueOf(mEventInfo.mStartTime)));
        }
        if (mEditEndTimeView != null) {
            mEditEndTimeView.setText(Utils.translateTime(Long.valueOf(mEventInfo.mEndTime)));
        }
        if (mEditLocationView != null) {
            mEditLocationView.setText(mEventInfo.mLocation);
        }
        if (mEditDescriptionView != null) {
            mEditDescriptionView.setText(mEventInfo.mDescription);
        }

        Utils.logd(TAG, "<--findOutCalendarDetailInfo()");
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
                Utils.loge(TAG, "History NDEF message is null");
            }
            return mHistoryMessage;
        }
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logv(TAG, "-->getHistoryView(). uriId=" + uriId);
        mVEvent = null;// Remove cached data
        mHistoryUriId = uriId;

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryContentView = inflater.inflate(R.xml.read_view_vevent, null);
        if (mHistoryContentView != null) {
            mHistoryTitleView = (TextView) mHistoryContentView
                    .findViewById(R.id.read_info_vevent_title);
            mHistoryStartTimeView = (TextView) mHistoryContentView
                    .findViewById(R.id.read_info_vevent_start_time);
            mHistoryEndTimeView = (TextView) mHistoryContentView
                    .findViewById(R.id.read_info_vevent_end_time);
            mHistoryLocationView = (TextView) mHistoryContentView
                    .findViewById(R.id.read_info_vevent_location);
            mHistoryDescriptionView = (TextView) mHistoryContentView
                    .findViewById(R.id.read_info_vevent_description);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, NFC_PROJECTION, null, null,
                    null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mEventInfo = loadEventInfoFromCursor(cursor, NFC_COLUMN_TITLE,
                            NFC_COLUMN_LOCATION, NFC_COLUMN_DESC, NFC_COLUMN_START_TIME,
                            NFC_COLUMN_END_TIME);
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    String vEventStr = cursor.getString(cursor.getColumnIndex(NFC_COLUMN_CONTENT));
                    if (vEventStr != null) {
                        mVEvent = vEventStr.getBytes();
                    }
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }

                    if (mHistoryTitleView != null) {
                        mHistoryTitleView.setText(mEventInfo.mTitle);
                    }
                    if (mHistoryStartTimeView != null) {
                        mHistoryStartTimeView.setText(Utils.translateTime(Long
                                .valueOf(mEventInfo.mStartTime)));
                    }
                    if (mHistoryEndTimeView != null) {
                        mHistoryEndTimeView.setText(Utils.translateTime(Long
                                .valueOf(mEventInfo.mEndTime)));
                    }
                    if (mHistoryLocationView != null) {
                        mHistoryLocationView.setText(mEventInfo.mLocation);
                    }
                    if (mHistoryDescriptionView != null) {
                        mHistoryDescriptionView.setText(mEventInfo.mDescription);
                    }
                }
                cursor.close();
            } else {
                Utils.loge(TAG,
                        "Fail to access NFC content provider for vCalendar event record whose id="
                                + uriId);
            }

        } else {
            Utils.loge(TAG, "Fail to load vCalendar history view layout");
        }
        return mHistoryContentView;
    }

    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        return handleReadTag();
    };

    private EventInfo loadEventInfoFromCursor(Cursor cursor, String titleCol, String locCol,
            String descCol, String startCol, String endCol) {
        Utils.logv(TAG, "-->loadEventInfoFromCursor()");
        if (cursor == null || !cursor.moveToFirst()) {
            Utils.loge(TAG, "Empty cursor");
            return new EventInfo("", "", "", "", "");
        }
        String title = cursor.getString(cursor.getColumnIndex(titleCol));
        String startTime = cursor.getString(cursor.getColumnIndex(startCol));
        String endTime = cursor.getString(cursor.getColumnIndex(endCol));
        String location = cursor.getString(cursor.getColumnIndex(locCol));
        String description = cursor.getString(cursor.getColumnIndex(descCol));

        Utils.logv(TAG, "<--loadEventInfoFromCursor(), title=" + title + ", location=" + location
                + ", description=" + description);
        return new EventInfo(title, location, description, startTime, endTime);
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mVEvent == null || mEventInfo == null) {
            Utils.loge(TAG, "Event info is null. Can not return a empty NDEF message");
            return null;
        }

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                MIME_TYPE_CALENDAR.getBytes(), new byte[0], mVEvent);
        NdefMessage message = new NdefMessage(new NdefRecord[] {
            record
        });

        addNewRecordToDB(message, mEventInfo, new String(mVEvent), 1);
        return message;
    }

    /**
     * Add the new selected event into NFC history database
     * 
     * @param msg The NDEF message will will be written into tag as a whole
     * @param eventInfo Detail calendar event info extract from msg
     * @param vEventStr Calendar event info string
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     */
    private void addNewRecordToDB(NdefMessage msg, EventInfo eventInfo, String vEventStr, int tagSrc) {
        Utils.logv(TAG, "-->addNewRecordToDB(), event detail: \n" + eventInfo.toString()
                + ",\n tagSrc=" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_VEVENT);
        selectionBuilder.append(" AND " + NFC_COLUMN_TITLE + "=\'"
                + Utils.encodeStrForDB(eventInfo.mTitle) + "\'");
        selectionBuilder.append(" AND " + NFC_COLUMN_LOCATION + "=\'"
                + Utils.encodeStrForDB(eventInfo.mLocation) + "\'");
        selectionBuilder.append(" AND " + NFC_COLUMN_DESC + "=\'"
                + Utils.encodeStrForDB(eventInfo.mDescription) + "\'");
        selectionBuilder.append(" AND " + NFC_COLUMN_START_TIME + "=\'" + eventInfo.mStartTime
                + "\'");
        selectionBuilder.append(" AND " + NFC_COLUMN_END_TIME + "=\'" + eventInfo.mEndTime + "\'");
        selectionBuilder.append(" AND " + TagContract.COLUMN_IS_CREATED_BY_ME + "=\'" + tagSrc
                + "\'");

        Cursor cursor = sActivity.getContentResolver().query(TagContract.TAGS_CONTENT_URI,
                NFC_PROJECTION, selectionBuilder.toString(), null, null);
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
            Utils.logw(TAG, "VCalendar record already exist, count=" + recordNum
                    + ", do not insert it again");
        } else {
            Utils.logi(TAG, "Insert new tag record into database");
            ContentValues values = new ContentValues();
            values.put(TagContract.COLUMN_DATE, System.currentTimeMillis());
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_VEVENT);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);

            values.put(NFC_COLUMN_TITLE, eventInfo.mTitle);
            values.put(NFC_COLUMN_LOCATION, eventInfo.mLocation);
            values.put(NFC_COLUMN_DESC, eventInfo.mDescription);
            values.put(NFC_COLUMN_START_TIME, eventInfo.mStartTime);
            values.put(NFC_COLUMN_END_TIME, eventInfo.mEndTime);
            values.put(TagContract.COLUMN_HISTORY_TITLE, eventInfo.mTitle);
            values.put(NFC_COLUMN_CONTENT, vEventStr);

            Uri uri = sActivity.getContentResolver().insert(TagContract.TAGS_CONTENT_URI, values);
            if (uri == null) {
                Utils.loge(TAG, "Add new vEvent record fail");
            } else {
                int deletedNum = Utils.limitHistorySize(sActivity, sActivity.getSharedPreferences(
                        Utils.CONFIG_FILE_NAME, Context.MODE_PRIVATE).getInt(
                        Utils.KEY_HISTORY_SIZE, Utils.DEFAULT_VALUE_HISTORY_SIZE));
                Utils.logd(TAG,
                        "After insert a new record, check total history size, deleted size="
                                + deletedNum);
            }
        }
        Utils.logv(TAG, "<--addNewRecordToDB(), exist before?" + exist);
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
        mEventInfo = new EventInfo("", "", "", "", "");
        mVEvent = null;

        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();
        if (tnf != NdefRecord.TNF_MIME_MEDIA
                || !MIME_TYPE_CALENDAR.equalsIgnoreCase(new String(type))) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type)));
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_vevent, null);

        if (mReadContentView == null) {
            Utils.loge(TAG, "Fail to load read content view");
            return null;
        } else {
            mReadTitleView = (TextView) mReadContentView.findViewById(R.id.read_info_vevent_title);
            mReadStartTimeView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_vevent_start_time);
            mReadEndTimeView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_vevent_end_time);
            mReadLocationView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_vevent_location);
            mReadDescriptionView = (TextView) mReadContentView
                    .findViewById(R.id.read_info_vevent_description);
        }
        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }

        mVEvent = payload;
        String payloadStr = new String(payload);
        parseVEvent(payloadStr);

        addNewRecordToDB(msg, mEventInfo, payloadStr, 0);

        return mReadContentView;
    }

    // TODO This may call Android's native method which is hidden in framework
    private void parseVEvent(String vEventStr) {
        Utils.logd(TAG, "-->parseVEvent(), content=[[\n" + vEventStr + "\n]]");

        if (TextUtils.isEmpty(vEventStr)) {
            Utils.loge(TAG, "Fail to parse a empty vEvent file");
            return;
        }

        String[] lines = vEventStr.split("\\n");
        String titleStr = "";
        String locationStr = "";
        String descriptionStr = "";
        String startTimeStr = "";
        String endTimeStr = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!TextUtils.isEmpty(line) && line.indexOf(":") >= 0) {
                if (line.startsWith(VEVENT_PREFIX_TITLE) && TextUtils.isEmpty(titleStr)) {
                    titleStr = line.substring(line.indexOf(":") + 1);
                } else if (line.startsWith(VEVENT_PREFIX_LOCATION)
                        && TextUtils.isEmpty(locationStr)) {
                    locationStr = line.substring(line.indexOf(":") + 1);
                } else if (line.startsWith(VEVENT_PREFIX_DESCRIPTION)
                        && TextUtils.isEmpty(descriptionStr)) {
                    descriptionStr = line.substring(line.indexOf(":") + 1);
                } else if (line.startsWith(VEVENT_PREFIX_START_TIME)
                        && TextUtils.isEmpty(startTimeStr)) {
                    startTimeStr = line.substring(line.indexOf(":") + 1);
                } else if (line.startsWith(VEVENT_PREFIX_END_TIME) && TextUtils.isEmpty(endTimeStr)) {
                    endTimeStr = line.substring(line.indexOf(":") + 1);
                }
            }
        }
        Utils.logi(TAG, "Parse vCard, title=" + titleStr + ", location=" + locationStr
                + ", description=" + descriptionStr + "\n startTime=" + startTimeStr + ", endTime="
                + endTimeStr);

        mEventInfo = new EventInfo(titleStr, locationStr, descriptionStr, String.valueOf(Utils
                .parseDateToMill(startTimeStr)), String.valueOf(Utils.parseDateToMill(endTimeStr)));

        if (mReadTitleView != null) {
            mReadTitleView.setText(mEventInfo.mTitle);
        }
        if (mReadLocationView != null) {
            mReadLocationView.setText(mEventInfo.mLocation);
        }
        if (mReadDescriptionView != null) {
            mReadDescriptionView.setText(mEventInfo.mDescription);
        }
        if (mReadStartTimeView != null) {
            mReadStartTimeView.setText(Utils.translateTime(Long.valueOf(mEventInfo.mStartTime)));
        }
        if (mReadEndTimeView != null) {
            mReadEndTimeView.setText(Utils.translateTime(Long.valueOf(mEventInfo.mEndTime)));
        }
        Utils.logd(TAG, "<--parseVEvent()");
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_vevent;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_vevent;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_vevent;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_vevent;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_vevent_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_vevent_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_VEVENT;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_vevent,
                sActivity.getString(R.string.tag_title_vevent));
        pref.setTagType(Utils.TAG_TYPE_VEVENT);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");
        if (mVEvent == null || mVEvent.length == 0) {
            Utils.loge(TAG, "vEvent byte array is empty");
            Toast.makeText(sActivity, R.string.error_vevent_empty_vevent, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        boolean storeSuccess = Utils.storeBytesIntoFile(sActivity, mVEvent, VEVENT_FILE_NAME);
        if (!storeSuccess) {
            Utils.loge(TAG, "Fail to store vCard info into a temp file for importing");
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(sActivity.getFilesDir(), VEVENT_FILE_NAME));
        intent.setDataAndType(uri, MIME_TYPE_CALENDAR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sActivity.startActivity(intent);
        // Finish tag information preview page
        sActivity.finish();
        return true;
    }

}
