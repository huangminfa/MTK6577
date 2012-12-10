package com.mediatek.nfc.tag.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.record.VEventRecord;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    public static final String TAG = "NfcTag";

    private static final boolean DEBUG = true;

    public static final float ANDROID_VERSION_NUMBER;
    static {
        String version = android.os.Build.VERSION.RELEASE;
        Utils.logi(TAG, "Andriod version string = " + version);
        float versionNum = (float) 4.0;
        try {
            versionNum = Float.parseFloat(version);
        } catch (NumberFormatException e) {
            if (version.startsWith("4.")) {
                versionNum = 4;
            } else {
                versionNum = (float) 2.3;
            }
        }
        ANDROID_VERSION_NUMBER = versionNum;
        Utils.logi(TAG, "Android version number=" + ANDROID_VERSION_NUMBER);
    }

    public static final int TAG_TYPE_UNKNOWN = -1;

    public static final int TAG_TYPE_SMART_POSTER = 0;

    public static final int TAG_TYPE_TEXT = 1;

    public static final int TAG_TYPE_PHONE_NUM = 2;

    public static final int TAG_TYPE_SMS = 3;

    public static final int TAG_TYPE_MMS = 4;

    public static final int TAG_TYPE_EMAIL = 5;

    public static final int TAG_TYPE_URL = 6;

    public static final int TAG_TYPE_VCARD = 7;

    public static final int TAG_TYPE_VEVENT = 8;

    public static final int TAG_TYPE_VTODO = 9;

    public static final int TAG_TYPE_PARAM = 10;

    public static final int TAG_TYPE_APP = 11;

    public static final int TAG_TYPE_EMPTY = 12;

    public static final String SEPARATOR_SMS_NUMBER = ",";

    public static final String SEPARATOR_SMS_NUMBER_BODY = "?body=";

    public static final String SEPARATOR_SMS_SUBJECT = "?subject=";

    // Used to identify midlet name field
    public static final String SEPARATOR_APP_NAME = "?name=";

    public static final String PREFIX_APP_TAG = "midlet:";

    public static final String SEPARATOR_EMAIL_TO_LIST = " ,";

    public static final String MIME_TYPE_PARAM_TAG = "application/com.mediatek.nfc.tag.param";

    /**
     * Add for empty tag
     */
    public static final String MIME_TYPE_EMPYT_TAG = "application/com.mediatek.nfc.tag.empty";

    public static final String SEPARATOR_EMPTY_TAG_TYPE_SIZE = ";";

    public static final String SMS_ATTACH_FILE_PREFIX = "attach_";

    public static final int SMS_ATTACH_FILE_MAX_SIZE = 6000; // max attachment
                                                             // file byte size

    public static final String PREFIX_SMS_TAG = "sms:";

    /**
     * Add for NFC settings, settings value will be stored in shared preference
     */
    public static final String CONFIG_FILE_NAME = "tag_settings";

    public static final String KEY_AUTO_LAUNCH_APP = "auto_launch_app";

    public static final boolean DEFAULT_VALUE_AUTO_LAUNCH_APP = false;

    public static final String KEY_HISTORY_SIZE = "history_size";

    public static final int DEFAULT_VALUE_HISTORY_SIZE = 50;

    public static final String KEY_SHOW_ADVANCED_WRITING = "show_advanced_writing";

    public static final boolean DEFAULT_VALUE_SHOW_ADVANCED_WRITIN = false;

    public static final String KEY_CONFIRM_OVERWRITE = "confirm_overwrite";

    public static final boolean DEFAULT_VALUE_CONFIRM_OVERWRITE = true;

    // Whether show system application list when show available app list
    public static final String KEY_ADVANCED_APP_TAG = "allow_system_app_for_app_tag";

    public static final boolean DEFAULT_VALUE_ADVANCED_APP_TAG = false;

    // Whether need to lock tag after write something to it
    public static final String KEY_LOCK_TAG = "lock_tag_after_write";

    /**
     * Tag read/write history record count have a limitation. use this to delete
     * not needed records
     */
    public static final String HISTORY_LIMITATION_FLAG = "record_number<";

    public static final HashMap<Byte, String> URI_PREFIX_MAP = new HashMap<Byte, String>();

    public static final HashMap<String, String> MIME_MAP = new HashMap<String, String>();
    /**
     * NFC Forum "URI Record Type Definition"
     */
    static {
        URI_PREFIX_MAP.put((byte) 0x00, "");
        URI_PREFIX_MAP.put((byte) 0x01, "http://www.");
        URI_PREFIX_MAP.put((byte) 0x02, "https://www.");
        URI_PREFIX_MAP.put((byte) 0x03, "http://");
        URI_PREFIX_MAP.put((byte) 0x04, "https://");
        URI_PREFIX_MAP.put((byte) 0x05, "tel:");
        URI_PREFIX_MAP.put((byte) 0x06, "mailto:");
        URI_PREFIX_MAP.put((byte) 0x07, "ftp://anonymous:anonymous@");
        URI_PREFIX_MAP.put((byte) 0x08, "ftp://ftp.");
        URI_PREFIX_MAP.put((byte) 0x09, "ftps://");
        URI_PREFIX_MAP.put((byte) 0x0A, "sftp://");
        URI_PREFIX_MAP.put((byte) 0x0B, "smb://");
        URI_PREFIX_MAP.put((byte) 0x0C, "nfs://");
        URI_PREFIX_MAP.put((byte) 0x0D, "ftp://");
        URI_PREFIX_MAP.put((byte) 0x0E, "dav://");
        URI_PREFIX_MAP.put((byte) 0x0F, "news:");
        URI_PREFIX_MAP.put((byte) 0x10, "telnet://");
        URI_PREFIX_MAP.put((byte) 0x11, "imap:");
        URI_PREFIX_MAP.put((byte) 0x12, "rtsp://");
        URI_PREFIX_MAP.put((byte) 0x13, "urn:");
        URI_PREFIX_MAP.put((byte) 0x14, "pop:");
        URI_PREFIX_MAP.put((byte) 0x15, "sip:");
        URI_PREFIX_MAP.put((byte) 0x16, "sips:");
        URI_PREFIX_MAP.put((byte) 0x17, "tftp:");
        URI_PREFIX_MAP.put((byte) 0x18, "btspp://");
        URI_PREFIX_MAP.put((byte) 0x19, "btl2cap://");
        URI_PREFIX_MAP.put((byte) 0x1A, "btgoep://");
        URI_PREFIX_MAP.put((byte) 0x1B, "tcpobex://");
        URI_PREFIX_MAP.put((byte) 0x1C, "irdaobex://");
        URI_PREFIX_MAP.put((byte) 0x1D, "file://");
        URI_PREFIX_MAP.put((byte) 0x1E, "urn:epc:id:");
        URI_PREFIX_MAP.put((byte) 0x1F, "urn:epc:tag:");
        URI_PREFIX_MAP.put((byte) 0x20, "urn:epc:pat:");
        URI_PREFIX_MAP.put((byte) 0x21, "urn:epc:raw:");
        URI_PREFIX_MAP.put((byte) 0x22, "urn:epc:");
        URI_PREFIX_MAP.put((byte) 0x23, "urn:nfc:");
    }

    static {
        MIME_MAP.put(".3gp", "video/3gpp");
        MIME_MAP.put(".apk", "application/vnd.android.package-archive");
        MIME_MAP.put(".asf", "video/x-ms-asf");
        MIME_MAP.put(".avi", "video/x-msvideo");
        MIME_MAP.put(".bin", "application/octet-stream");
        MIME_MAP.put(".bmp", "image/bmp");
        MIME_MAP.put(".c", "text/plain");
        MIME_MAP.put(".class", "application/octet-stream");
        MIME_MAP.put(".conf", "text/plain");
        MIME_MAP.put(".cpp", "text/plain");
        MIME_MAP.put(".doc", "application/msword");
        MIME_MAP.put(".exe", "application/octet-stream");
        MIME_MAP.put(".gif", "image/gif");
        MIME_MAP.put(".gtar", "application/x-gtar");
        MIME_MAP.put(".gz", "application/x-gzip");
        MIME_MAP.put(".h", "text/plain");
        MIME_MAP.put(".htm", "text/plain");
        MIME_MAP.put(".html", "text/plain");
        MIME_MAP.put(".jar", "application/java-archive");
        MIME_MAP.put(".java", "text/plain");
        MIME_MAP.put(".jpeg", "image/jpeg");
        MIME_MAP.put(".jpg", "image/jpeg");
        MIME_MAP.put(".js", "application/x-javascript");
        MIME_MAP.put(".log", "text/plain");
        MIME_MAP.put(".m3u", "audio/x-mpegurl");
        MIME_MAP.put(".m4a", "audio/mp4a-latm");
        MIME_MAP.put(".m4b", "audio/mp4a-latm");
        MIME_MAP.put(".m4p", "audio/mp4a-latm");
        MIME_MAP.put(".m4u", "video/vnd.mpegurl");
        MIME_MAP.put(".m4v", "video/x-m4v");
        MIME_MAP.put(".mov", "video/quicktime");
        MIME_MAP.put(".mp2", "audio/x-mpeg");
        MIME_MAP.put(".mp3", "audio/x-mpeg");
        MIME_MAP.put(".mp4", "video/mp4");
        MIME_MAP.put(".mpc", "application/vnd.mpohun.certificate");
        MIME_MAP.put(".mpe", "video/mpeg");
        MIME_MAP.put(".mpeg", "video/mpeg");
        MIME_MAP.put(".mpg", "video/mpeg");
        MIME_MAP.put(".mpg4", "video/mp4");
        MIME_MAP.put(".mpga", "audio/mpeg");
        MIME_MAP.put(".msg", "application/vnd.ms-outlook");
        MIME_MAP.put(".ogg", "audio/ogg");
        MIME_MAP.put(".pdf", "application/pdf");
        MIME_MAP.put(".png", "image/png");
        MIME_MAP.put(".pps", "application/vnd.ms-powerpoint");
        MIME_MAP.put(".ppt", "application/vnd.ms-powerpoint");
        MIME_MAP.put(".prop", "text/plain");
        MIME_MAP.put(".rar", "application/x-rar-compressed");
        MIME_MAP.put(".rc", "text/plain");
        MIME_MAP.put(".rmvb", "audio/x-pn-realaudio");
        MIME_MAP.put(".rtf", "application/rtf");
        MIME_MAP.put(".sh", "text/plain");
        MIME_MAP.put(".tar", "application/x-tar");
        MIME_MAP.put(".tgz", "application/x-compressed");
        MIME_MAP.put(".txt", "text/plain");
        MIME_MAP.put(".wav", "audio/x-wav");
        MIME_MAP.put(".wma", "audio/x-ms-wma");
        MIME_MAP.put(".wmv", "audio/x-ms-wmv");
        MIME_MAP.put(".wps", "application/vnd.ms-works");
        MIME_MAP.put(".xml", "text/plain");
        MIME_MAP.put(".z", "application/x-compress");
        MIME_MAP.put(".zip", "application/zip");
        MIME_MAP.put("", "*/*");
    }

    public static final String IMAGE_UNSPECIFIED = "image/*";

    public static final String AUDIO_UNSPECIFIED = "audio/*";

    public static final String AUDIO_OGG = "application/ogg";

    public static final int URI_TYPE_TEL = 5;

    /**
     * Just check one level for NDEFRecord type, if it's a smart post record,
     * just return SP, then you may need to have a more detail check for SP's
     * each record
     * 
     * @param record
     * @return
     */
    public static int getRecordType(NdefRecord record) {
        Utils.logd(TAG, "-->getRecordType()");
        int recordType = TAG_TYPE_UNKNOWN;
        if (record == null) {
            Utils.loge(TAG, "Null record");
            return recordType;
        }
        if (record.getPayload() == null || record.getPayload().length == 0) {
            Utils.loge(TAG, "Empty payload");
            return recordType;
        }

        short tnf = record.getTnf();
        byte[] type = record.getType();
        Utils.logd(TAG, "TNF=" + tnf + ", type=" + new String(type) + ", payload[0]="
                + record.getPayload()[0]);
        Utils.logv(TAG, "Payload content="
                + new String(record.getPayload(), 1, record.getPayload().length - 1, Charset
                        .forName("UTF-8")));

        // Judge tag type first
        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            byte[] payload = record.getPayload();
            if (Arrays.equals(type, NdefRecord.RTD_TEXT)) { // common text
                recordType = Utils.TAG_TYPE_TEXT;
            } else if (Arrays.equals(type, NdefRecord.RTD_URI)) {
                String prefix = Utils.URI_PREFIX_MAP.get(payload[0]);
                if (prefix == null) {
                    Utils.loge(TAG, "Null URI");
                    return recordType;
                }
                String fullUri = prefix
                        + new String(payload, 1, payload.length - 1, Charset.forName("UTF-8"));
                Uri uri = Uri.parse(fullUri);
                String schema = uri.getScheme();
                Utils.logw(TAG, "URI tag schema=" + schema);

                if (TextUtils.isEmpty(schema)) {
                    Utils.loge(TAG, "Null schema URI, invalid!");
                }
                if (schema.startsWith("tel")) {
                    recordType = Utils.TAG_TYPE_PHONE_NUM;
                } else if (schema.startsWith("http")) {
                    recordType = Utils.TAG_TYPE_URL;
                } else if (schema.startsWith("sms") || schema.startsWith("mms")) {
                    //mms is required by Operator spec
                    recordType = Utils.TAG_TYPE_SMS;
                } else if (schema.startsWith("mailto")) {
                    recordType = Utils.TAG_TYPE_EMAIL;
                } else if (schema.startsWith("midlet")) {
                    recordType = Utils.TAG_TYPE_APP;
                } else {
                    Utils.logd(TAG, "Unsupported url schema, uri=" + fullUri);
                }
            } else if (Arrays.equals(type, NdefRecord.RTD_SMART_POSTER)) { // Smart
                                                                    // post record
                recordType = TAG_TYPE_SMART_POSTER;
            } else {
                Utils.logd(TAG, "Unknown type: " + new String(type));
            }
            Utils.logd(TAG, "Received payload byte length=" + payload.length);
        } else if (tnf == NdefRecord.TNF_MIME_MEDIA) {
            String typeStr = new String(type);
            if (ContactsContract.Contacts.CONTENT_VCARD_TYPE.equalsIgnoreCase(typeStr)) {
                recordType = Utils.TAG_TYPE_VCARD;
            } else if (VEventRecord.MIME_TYPE_CALENDAR.equalsIgnoreCase(typeStr)) {
                recordType = Utils.TAG_TYPE_VEVENT;
            } else if (Utils.MIME_TYPE_PARAM_TAG.equals(typeStr)) {
                recordType = Utils.TAG_TYPE_PARAM;
            } else if (Utils.MIME_TYPE_EMPYT_TAG.equals(typeStr)) {
                recordType = Utils.TAG_TYPE_EMPTY;
            } else {
                Utils.loge(TAG, "Unsupported mime type: " + typeStr);
            }
        } else {
            Utils.loge(TAG, "Unsupported tnf type: " + tnf);
        }
        return recordType;
    }

    /**
     * Check the info type stored in a NFC tag. If it contain a smart poster
     * record, twice check will be needed
     * 
     * @return
     */
    public static final int getTagType(NdefRecord recordInTag) {
        Utils.logd(TAG, "-->getTagType()");
        int type = getRecordType(recordInTag);
        if (type != TAG_TYPE_SMART_POSTER) {
            return type;
        } else { // For SP tag, check second level info again
            type = TAG_TYPE_UNKNOWN;
            NdefMessage message = null;
            try {
                message = new NdefMessage(recordInTag.getPayload());
            } catch (FormatException e) {
                Utils.loge(TAG, "Fail to get sub-records info from SP record", e);
            }
            if (message != null) {
                NdefRecord[] records = message.getRecords();
                for (NdefRecord record : records) {
                    short subTnf = record.getTnf();
                    byte[] subType = record.getType();
                    // Check the only exit URI record type
                    if (subTnf == NdefRecord.TNF_WELL_KNOWN
                            && Arrays.equals(subType, NdefRecord.RTD_URI)) {
                        type = getRecordType(record);
                        Utils.logi(TAG, "Record type in SP tag is " + type);
                        if (type == TAG_TYPE_SMART_POSTER) {
                            Utils.loge(TAG,
                                    "More than one layer smart post tag is not supported yet");
                            type = TAG_TYPE_UNKNOWN;
                        } else {
                            break;
                        }
                    }
                }
            } else {
                Utils.loge(TAG, "Message in SP tag payload is null.");
            }
            return type;
        }
    }

    /**
     * Get the first needed record from the array, if exist
     * 
     * @param records
     * @param mType
     * @return
     */
    public static final NdefRecord getFirstRecordIfExists(NdefRecord[] records, int neededType) {
        Utils.logd(TAG, "-->getFirstIfExists(), neededType=" + neededType);
        for (NdefRecord record : records) {
            int type = getRecordType(record);
            if (type == neededType) {
                return record;
            }
        }
        Utils.logw(TAG, "Could not find needed type from record array.");
        return null;
    }

    /**
     * Found the only needed record from array, which must exist
     * 
     * @param records
     * @param neededType
     * @return
     */
    public static final NdefRecord getOnlyExistingRecord(NdefRecord[] records, int neededType) {
        Utils.logd(TAG, "-->getOnlyExistingRecord(), neededType=" + neededType);
        int foundNum = 0;
        NdefRecord result = null;
        for (NdefRecord record : records) {
            int type = getRecordType(record);
            if (type == neededType) {
                if (foundNum == 0) {
                    result = record;
                }
                foundNum++;
            }
        }
        if (foundNum == 0) {
            Utils.loge(TAG, "Could not find needed type from record array.");
        } else if (foundNum > 1) {
            Utils.loge(TAG, "Found more than one needed record from record array.");
        }
        Utils.logd(TAG, "<--getOnlyExistingRecord()");
        return result;
    }

    /**
     * transfer long time(UTC) to time string
     * 
     * @param time
     * @return ex: 2012/12/21 23:59
     */
    public static String translateTime(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        Date date = new Date();
        
        DecimalFormat df = new DecimalFormat();
        String pattern = "00";
        df.applyPattern(pattern);
        //Translate UTC time to local time
        calendar.setTimeInMillis(time + TimeZone.getDefault().getRawOffset());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minu = calendar.get(Calendar.MINUTE);
        return "" + year + "/" + df.format(month) + "/" + df.format(day) + "  " + df.format(hour)
                + ":" + df.format(minu);
    }

    /**
     * This method will parse a date string(e.g. 20100101T173019Z ) into a time
     * expressed by milli-second
     */
    public static long parseDateToMill(String dateStr) {
        Utils.logv(TAG, "-->parseDateToMill(), dateStr=" + dateStr);
        if (!TextUtils.isEmpty(dateStr)) {
            dateStr = dateStr.trim(); // remove no-needed space
        } else {
            Utils.loge(TAG, "Date string is empty");
            return 0;
        }
        if (dateStr.length() != 16 || !"T".equals(dateStr.substring(8, 9))) {
            Utils.loge(TAG, "Unknown date string format");
            return 0;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            calendar.set(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr
                    .substring(4, 6)), Integer.parseInt(dateStr.substring(6, 8)), Integer
                    .parseInt(dateStr.substring(9, 11)), Integer
                    .parseInt(dateStr.substring(11, 13)), Integer.parseInt(dateStr
                    .substring(13, 15)));
        } catch (NumberFormatException e) {
            Utils.loge(TAG, "Invalid time format!");
            return 0;
        }
        return calendar.getTimeInMillis();
    }
    
    /**
     * Since some special character like ' was already used by database, 
     * before insert string into db, encoding them first
     * @param initStr
     * @return
     */
    public static String encodeStrForDB(String initStr){
        if(TextUtils.isEmpty(initStr)){
            return "";
        }
        return initStr.replace("\'", "\'\'");
    }

    public static boolean storeBytesIntoFile(Context context, byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            Utils.loge(TAG, "Nothing to store");
            return false;
        }
        Utils.logd(TAG, "-->storeBytesIntoFile(), bytes size=" + bytes.length + ", fileName="
                + fileName);
        FileOutputStream fos = null;
        boolean result = false;
        try {
            context.deleteFile(fileName);
            fos = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            fos.write(bytes);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * Create a new NDEFRecord from the given text
     * 
     * @param text
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    public static NdefRecord newNDEFTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        Utils.logd(TAG, "-->newNDEFTextRecord(), encodeInUtf8?" + encodeInUtf8 + ", text length="
                + text.length() + "\n  text=" + text);
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[textBytes.length + langBytes.length + 1];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, langBytes.length + 1, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    /**
     * Get text stored in text record
     * 
     * @param record
     * @return
     */
    public static final String getTextRecordContent(NdefRecord record) {
        Utils.logd(TAG, "-->getTextRecordContent()");

        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();
        if (tnf != NdefRecord.TNF_WELL_KNOWN || !Arrays.equals(type, NdefRecord.RTD_TEXT)) {
            Utils
                    .loge(TAG, "Invalid text record type, tnf=" + tnf + ", type="
                            + (new String(type)));
            return null;
        }

        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }

        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        String contentStr = "";
        int languageCodeLength = payload[0] & 0077;
        try {
            contentStr = new String(payload, languageCodeLength + 1, payload.length - 1
                    - languageCodeLength, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Utils.loge(TAG, "Exception happen while parsing tag content", e);
        }
        Utils.logd(TAG, "Content string=[" + contentStr + "]");
        return contentStr;
    }

    /**
     * Read/write tag history number should not be too big. This method will
     * delete the oldest record to keep the total history number in the range
     * given by user
     * 
     * @param context Context for access content resolve
     * @param maxNumber history record number upper limitation
     * @return Deleted history record number
     */
    public static int limitHistorySize(Context context, int maxNumber) {
        Utils.logd(TAG, "-->limitHistorySize(), maxNumber=" + maxNumber);

        int deletedNum = context.getContentResolver().delete(TagContract.TAGS_CONTENT_URI,
                Utils.HISTORY_LIMITATION_FLAG, new String[] {
                    (String.valueOf(maxNumber))
                });

        Utils.logd(TAG, "<--limitHistorySize(), deleted record number=" + deletedNum);
        return deletedNum;
    }

    /**
     * Select need mime type file
     * 
     * @param activity
     * @param requestCode
     * @param contentType
     * @param localFilesOnly
     */
    public static void selectMediaByType(Activity activity, int requestCode, String contentType,
            boolean localFilesOnly) {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType(contentType);
        if (localFilesOnly) {
            innerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }

        // createChoose will generate some broadcast receiver leak
        // Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        // activity.startActivityForResult(wrapperIntent, requestCode);
        activity.startActivityForResult(innerIntent, requestCode);
    }

    public static void selectAudio(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(AUDIO_UNSPECIFIED);
        intent.setType(AUDIO_OGG);
        intent.setType("application/x-ogg");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void selectRingtone(Activity activity, int requestCode) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, activity
                .getString(R.string.mms_select_ringtone_title));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Get application info by package name
     * 
     * @param packageName
     * @return
     */
    public static final ApplicationInfo getApplicationInfo(Context context, String packageName) {
        Utils.logd(TAG, "-->getApplicationInfo(), packageName=" + packageName);
        if (TextUtils.isEmpty(packageName) || context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
        mApplications = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
                | PackageManager.GET_DISABLED_COMPONENTS);

        int size = mApplications.size();
        for (int i = 0; i < size; i++) {
            ApplicationInfo info = mApplications.get(i);
            // Search for needed application info by package name
            if (packageName.equals(info.packageName)) {
                Utils.logd(TAG, "Found needed application.");
                return info;
            }
        }
        Utils.logw(TAG, "Fail to find needed application.");
        return null;
    }

    /**
     * Log out put segment, get ready for XLog
     */
    public static void logv(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void logd(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void logi(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void logw(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void loge(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void loge(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}
