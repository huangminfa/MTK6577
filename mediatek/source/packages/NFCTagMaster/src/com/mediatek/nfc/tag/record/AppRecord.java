
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.record.AppList.AppEntry;
import com.mediatek.nfc.tag.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * This record can store an application's package name info. This info can be
 * used to launch this application or, if not exist yet, search it in market.
 */
public class AppRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/AppRecord";

    private static final int REQUEST_CODE_THIRD_PARTY_APP = 1;

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // For tag info reader
    private View mReadContentView = null;

    // Message stored in readed record
    private TextView mReadAppNameInTagView = null;

    private TextView mReadPackageNameInTagView = null;

    // Tag related application information in local device
    private ImageView mReadLocalAppIconView;

    private TextView mReadLocalAppNameView;

    private TextView mReadNotInstalledView = null;

    private String mReadAppPackageStr = null;

    // Whether the new read application exist on local device
    private boolean mReadAppExist = true;

    // For tag info editor
    private View mEditContentView = null;

    private LinearLayout mEditSelector;

    private ImageView mEditAppIconView;

    private TextView mEditAppNameView;

    private TextView mEditPackageNameView;

    // For history tag view
    private View mHistoryContentView = null;

    // Message stored in history record
    private TextView mHistoryAppNameInTagView = null;

    private TextView mHistoryPackageNameInTagView = null;

    // History related application information in local device
    private ImageView mHistoryLocalAppIconView;

    private TextView mHistoryLocalAppNameView;

    private TextView mHistoryNotInstalledView = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    // Tag special column
    private static final String DB_COLUMN_APP_NAME = TagContract.COLUMN_01;

    private static final String DB_COLUMN_PACKAGE_NAME = TagContract.COLUMN_02;

    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_APP_NAME,
            DB_COLUMN_PACKAGE_NAME, TagContract.COLUMN_BYTES, TagContract.COLUMN_IS_CREATED_BY_ME
    };

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create application record instance now");
            sRecord = new AppRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_app, sActivity
                .getString(R.string.tag_title_app));
        pref.setTagType(Utils.TAG_TYPE_APP);
        return pref;
    }

    @Override
    public View getEditView() {
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_app, null);
        if (mEditContentView != null) {
            mEditSelector = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_app_selector);
            mEditAppIconView = (ImageView) mEditContentView.findViewById(R.id.edit_info_app_icon);
            mEditAppNameView = (TextView) mEditContentView.findViewById(R.id.edit_info_app_name);
            mEditPackageNameView = (TextView) mEditContentView
                    .findViewById(R.id.edit_info_app_package_name);

            mEditSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    startAppSelectPage();
                }
            });
        }
        // Start application select page directly
        startAppSelectPage();

        return mEditContentView;
    }

    private void startAppSelectPage() {
        Utils.logd(TAG, "-->startAppSelectPage()");
        Intent intent = new Intent(sActivity, AppList.class);
        sActivity.startActivityForResult(intent, REQUEST_CODE_THIRD_PARTY_APP);
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);
        if (requestCode == REQUEST_CODE_THIRD_PARTY_APP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ApplicationInfo info = data.getParcelableExtra(AppList.EXTRA_APP_INFO);
                AppEntry entry = new AppEntry(sActivity, info);
                if (entry != null) {
                    if (entry.ensureIcon(sActivity)) {
                        mEditAppIconView.setImageDrawable(entry.mIcon);
                    }
                    mEditAppNameView.setText(entry.mLabel);
                    mEditPackageNameView.setText(entry.mInfo.packageName);
                }
            } else {
                Utils.logw(TAG,
                        "-->onActivityResultCallback(), no application is selected. resultCode="
                                + resultCode + ", data=null?" + (data == null));
                sActivity.finish();
            }
        } else {
            Utils.loge(TAG, "Unknown request code.");
        }
    }

    @Override
    public View getPreview(NdefMessage msg) {
        Utils.logd(TAG, "-->getPreview()");
        NdefRecord record = msg.getRecords()[0];
        mReadAppPackageStr = null;
        mReadAppExist = true;
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

        if (tnf != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(type, NdefRecord.RTD_URI)
                || payload[0] != (byte) 0) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type))
                    + ", uri type=" + payload[0]);
            return null;
        }

        String contentStr = new String(payload, 1, payload.length - 1);
        if (TextUtils.isEmpty(contentStr) || !contentStr.startsWith(Utils.PREFIX_APP_TAG)) {
            Utils.loge(TAG, "Invalid midlet tag");
            return null;
        }
        int separatorIndex = contentStr.indexOf(Utils.SEPARATOR_APP_NAME);
        String nameStr = "";
        mReadAppPackageStr = "";
        if (separatorIndex > 0) {
            mReadAppPackageStr = contentStr
                    .substring(Utils.PREFIX_APP_TAG.length(), separatorIndex);
            nameStr = contentStr.substring(separatorIndex + Utils.SEPARATOR_APP_NAME.length());
        } else {
            mReadAppPackageStr = contentStr.substring(Utils.PREFIX_APP_TAG.length());
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.history_view_app, null);
        if (mReadContentView != null) {
            mReadAppNameInTagView = (TextView) mReadContentView
                    .findViewById(R.id.history_info_app_name_intag);
            mReadPackageNameInTagView = (TextView) mReadContentView
                    .findViewById(R.id.history_info_app_package_intag);
            mReadLocalAppIconView = (ImageView) mReadContentView
                    .findViewById(R.id.history_info_app_local_icon);
            mReadLocalAppNameView = (TextView) mReadContentView
                    .findViewById(R.id.history_info_local_app_name);
            mReadNotInstalledView = (TextView) mReadContentView
                    .findViewById(R.id.history_view_app_not_installed);
        }
        if (mReadAppNameInTagView != null) {
            mReadAppNameInTagView.setText(nameStr);
        }
        if (mReadPackageNameInTagView != null) {
            mReadPackageNameInTagView.setText(mReadAppPackageStr);
        }

        ApplicationInfo info = Utils.getApplicationInfo(sActivity, mReadAppPackageStr);
        if (info != null) {
            mReadNotInstalledView.setVisibility(View.GONE);
            AppEntry entry = new AppEntry(sActivity, info);
            mReadLocalAppNameView.setText(entry.mLabel);
            if (entry.ensureIcon(sActivity)) {
                mReadLocalAppIconView.setImageDrawable(entry.mIcon);
            }
        } else {
            mReadAppExist = false;
            Utils.logi(TAG, "Fail to get application from local device, app package name="
                    + mReadAppPackageStr);
            mReadLocalAppNameView.setText(nameStr);
            mReadLocalAppIconView.setImageResource(R.drawable.ic_tag_app);
            mReadNotInstalledView.setVisibility(View.VISIBLE);

            showDownloadConfirmDialog(nameStr, mReadAppPackageStr);
        }

        addNewRecordToDB(msg, nameStr, mReadAppPackageStr, 0);
        return mReadContentView;
    }

    /**
     * Show a dialog to download needed application from Google market
     * 
     * @param appName
     * @param packageName
     */
    private void showDownloadConfirmDialog(final String appName, final String packageName) {
        Utils.logd(TAG, "-->showDownloadConfirmDialog(), packageName=" + packageName);
        AlertDialog dialog = new AlertDialog.Builder(sActivity).setTitle(
                android.R.string.dialog_alert_title).setMessage(
                sActivity.getString(R.string.app_download_confirm_message, appName))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAppFromMarket(packageName);
                        sActivity.finish();
                    }
                }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Click 'Cancel' will quit this tag read page
                                sActivity.finish();
                            }
                        }).create();
        dialog.show();
    }

    private void downloadAppFromMarket(String pkgName) {
        Utils.logd(TAG, "-->downloadAppFromMarket()");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:"
                + pkgName));
        try {
            sActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(sActivity, R.string.app_download_fail_message, Toast.LENGTH_SHORT)
                    .show();
            Utils.loge(TAG, "Start market fail. Google Market may have not been installed.", e);
        }
    }

    @Override
    public View getHistoryView(int uriId) {
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        mHistoryUriId = uriId;
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryContentView = inflater.inflate(R.xml.history_view_app, null);
        // Read or write history
        int createdByMeFlag = 0;
        if (mHistoryContentView != null) {
            mHistoryAppNameInTagView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_app_name_intag);
            mHistoryPackageNameInTagView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_app_package_intag);
            mHistoryLocalAppIconView = (ImageView) mHistoryContentView
                    .findViewById(R.id.history_info_app_local_icon);
            mHistoryLocalAppNameView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_info_local_app_name);
            mHistoryNotInstalledView = (TextView) mHistoryContentView
                    .findViewById(R.id.history_view_app_not_installed);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);

            String appName = "";
            String packageName = "";
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    appName = cursor.getString(cursor.getColumnIndex(DB_COLUMN_APP_NAME));
                    packageName = cursor.getString(cursor.getColumnIndex(DB_COLUMN_PACKAGE_NAME));
                    createdByMeFlag = cursor.getInt(cursor
                            .getColumnIndex(TagContract.COLUMN_IS_CREATED_BY_ME));
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    if (mHistoryAppNameInTagView != null) {
                        mHistoryAppNameInTagView.setText(appName);
                    }
                    if (mHistoryPackageNameInTagView != null) {
                        mHistoryPackageNameInTagView.setText(packageName);
                    }
                } else {
                    Utils.loge(TAG, "Fail to get application history record with id:" + uriId);
                }
                cursor.close();
            }
            ApplicationInfo info = Utils.getApplicationInfo(sActivity, packageName);
            if (info != null) {
                mHistoryNotInstalledView.setVisibility(View.GONE);
                AppEntry entry = new AppEntry(sActivity, info);
                if (entry.ensureIcon(sActivity)) {
                    mHistoryLocalAppIconView.setImageDrawable(entry.mIcon);
                }
                mHistoryLocalAppNameView.setText(entry.mLabel);
            } else {
                mHistoryLocalAppNameView.setText(appName);
                mHistoryLocalAppIconView.setImageResource(R.drawable.ic_tag_app);
                mHistoryNotInstalledView.setVisibility(View.VISIBLE);

                if (createdByMeFlag == 0) { // Read tag
                    showDownloadConfirmDialog(appName, packageName);
                }
            }
        }
        return mHistoryContentView;
    }

    @Override
    public boolean handleHistoryReadTag() {
        String historyPkgName = ((mHistoryPackageNameInTagView != null) ? mHistoryPackageNameInTagView
                .getText().toString()
                : "");
        Utils.logd(TAG, "-->handleHistoryReadTag(), mReadAppPackageStr=" + historyPkgName);
        if (TextUtils.isEmpty(historyPkgName)) {
            Utils.logw(TAG, "Read application pacakge string is empty");
            return false;
        }

        return resolveAndStartActivity(historyPkgName);
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
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNewNdefMessage()");
        if (mEditPackageNameView == null || mEditAppNameView == null) {
            Utils.loge(TAG, "Fail to load app info, input area is null");
            return null;
        }
        String nameStr = mEditAppNameView.getText().toString();
        String packageStr = mEditPackageNameView.getText().toString();
        if (TextUtils.isEmpty(packageStr)) {
            Utils.loge(TAG, "Application package name is empty, invalid");
            Toast.makeText(sActivity, R.string.error_app_empty_package, Toast.LENGTH_SHORT).show();
            return null;
        }

        String payloadStr = Utils.PREFIX_APP_TAG + packageStr;
        if (!TextUtils.isEmpty(nameStr)) {
            payloadStr += Utils.SEPARATOR_APP_NAME + nameStr;
        }
        byte[] bytes = payloadStr.getBytes();
        byte[] payload = new byte[bytes.length + 1];// Attention: Some
        // language's
        // String.length() is not
        // equal to bytes array's
        // length

        Utils.logv(TAG, "PayloadStr=" + payloadStr + ", str len=" + payloadStr.length()
                + ", byte len=" + bytes.length);

        payload[0] = (byte) 0;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
                new byte[0], payload);
        // Save the new input record into database
        NdefMessage msg = new NdefMessage(new NdefRecord[] {
            record
        });
        addNewRecordToDB(msg, nameStr, packageStr, 1);

        return msg;
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param appName Selected application name
     * @param packageName Application package name
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String appName, String packageName, int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), appName=" + appName + ", packageName="
                + packageName + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_APP);
        selectionBuilder.append(" AND " + DB_COLUMN_APP_NAME + "=\'"
                + Utils.encodeStrForDB(appName) + "\' ");
        selectionBuilder.append(" AND " + DB_COLUMN_PACKAGE_NAME + "=\'"
                + Utils.encodeStrForDB(packageName) + "\' ");
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
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_APP);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_APP_NAME, appName);
            values.put(DB_COLUMN_PACKAGE_NAME, packageName);
            String historyTitle = (TextUtils.isEmpty(appName) ? packageName : appName);
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
                        "After insert a new record, check total history size, deleted record number="
                                + deletedNum);
            }
        }
        return true;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag(), mReadAppPackageStr=" + mReadAppPackageStr);
        if (TextUtils.isEmpty(mReadAppPackageStr)) {
            Utils.logw(TAG, "Read application pacakge string is empty");
            return false;
        }
        if (!mReadAppExist) {
            // Read application does not exist in local device, so this button
            // should not be clicked
            // But for auto-start-app mode, this button may be clicked
            // automatically, so ignore this
            // click event in this situation
            Utils.logw(TAG, "Read application does not exist in local device, ignore click event.");
            return false;
        }

        boolean startResult = resolveAndStartActivity(mReadAppPackageStr);
        // Finish tag information preview page
        sActivity.finish();
        Utils.logd(TAG, "<--handleReadTag(), handle result=" + startResult);
        return startResult;
    }

    /**
     * Parse a package name string and try to pickup a suitable activity
     * 
     * @param packageName
     * @return
     */
    private boolean resolveAndStartActivity(String packageName) {
        Utils.logd(TAG, "-->resolveAndStartActivity(), packageName=" + packageName);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);

        PackageManager pm = sActivity.getPackageManager();
        // ActivityInfo activityInfo = intent.resolveActivityInfo(pm, 0);
        // Utils.logi(TAG, "Resolved activity name = "+activityInfo.name);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            Toast.makeText(sActivity, R.string.error_app_found_no_app, Toast.LENGTH_SHORT).show();
            Utils.loge(TAG, "Fail to resolve activity in package [" + packageName + "]");
            return false;
        }
        if (resolveInfos.size() == 1) { // Only one launcher activity found in
            // package, just start it
            ResolveInfo resolveInfo = resolveInfos.get(0);
            Intent startIntent = new Intent();
            startIntent.setClassName(resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                sActivity.startActivity(startIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Utils.loge(TAG, "Fail to start activity [" + resolveInfo.activityInfo.name
                        + "] extracted from package [" + packageName + "]", e);
                return false;
            }
        } else {
            // Found more than one launcher activity in package
            int count = resolveInfos.size();
            Utils.logw(TAG, "Total " + count + " launcher activity was found in package "
                    + packageName);
            for (int i = 0; i < count; i++) {
                Utils.logd(TAG, " Launcher activity name=" + resolveInfos.get(i).activityInfo.name);
            }

            // First let system determine which to start, only can take effect
            // for android.intent.category.DEFAULT
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sActivity.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Utils
                        .loge(
                                TAG,
                                "Fail to start package ["
                                        + packageName
                                        + "] by system, "
                                        + "maybe only can take effect for DEFAULT category, try to start it manually.",
                                e);
                // Try to start the first match activity
                ResolveInfo resolveInfo = resolveInfos.get(0);
                Intent startIntent = new Intent();
                startIntent.setClassName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    sActivity.startActivity(startIntent);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    Utils.loge(TAG, "Fail to start activity [" + resolveInfo.activityInfo.name
                            + "], the first one extracted from package [" + packageName + "]", ex);
                    return false;
                }
            }
        }
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_app;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_app_title;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_app_summary;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_app;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_app;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_app;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_APP;
    }

}
