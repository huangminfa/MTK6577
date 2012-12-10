package com.mediatek.nfc.tag.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * For ICS, to obtain contact info by phone id
 */
public class Contact {
    public static final Intent INTENT_SELECT_MULTI_PHONE;

    public static final Intent INTENT_SELECT_MULTI_EMAIL;
    static {
        String version = android.os.Build.VERSION.RELEASE;
        if (version.startsWith("4.")) {
            INTENT_SELECT_MULTI_PHONE = new Intent(
                    "android.intent.action.contacts.list.PICKMULTIPHONES");
            INTENT_SELECT_MULTI_PHONE.setType(Phone.CONTENT_TYPE);

            INTENT_SELECT_MULTI_EMAIL = new Intent(
                    "android.intent.action.contacts.list.PICKMULTIEMAILS");
            INTENT_SELECT_MULTI_EMAIL.setType(Phone.CONTENT_TYPE);
        } else {
            INTENT_SELECT_MULTI_PHONE = new Intent("android.intent.action.CONTACTSMULTICHOICE");
            INTENT_SELECT_MULTI_PHONE.setType(Phone.CONTENT_ITEM_TYPE);
            INTENT_SELECT_MULTI_PHONE.putExtra("request_type", 1);// only phone
            // number

            INTENT_SELECT_MULTI_EMAIL = new Intent("android.intent.action.CONTACTSMULTICHOICE");
            INTENT_SELECT_MULTI_EMAIL.setType(Phone.CONTENT_ITEM_TYPE);
            INTENT_SELECT_MULTI_EMAIL.putExtra("request_type", 2);// only email
        }
    }

    public static final String VCARD_FILE_NAME = "vcard_file.vcf";

    private static final Uri PHONES_WITH_ID_URI = Data.CONTENT_URI;

    public static final String VCARD_PREFIX_V21 = "BEGIN:VCARD\nVERSION:2.1\n";

    public static final String VCARD_SUFFIX = "END:VCARD\n";

    private static final String[] CALLER_ID_PROJECTION = new String[] {
            Phone._ID, // 0
            Phone.NUMBER, // 1
            Phone.LABEL, // 2
            Phone.DISPLAY_NAME, // 3
            Phone.CONTACT_ID, // 4
            Phone.CONTACT_PRESENCE, // 5
            Phone.CONTACT_STATUS, // 6
            Email.ADDRESS, // 7
            Email.TYPE
    // 8
    };

    /**
     * Projection used for the query that loads detail data for the entire
     * contact
     */
    public static final String[] DATA_DETAIL_COLUMNS = {
            // Contacts.NAME_RAW_CONTACT_ID,
            "name_raw_contact_id", Contacts.DISPLAY_NAME_SOURCE, Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME, Contacts.DISPLAY_NAME_ALTERNATIVE, Contacts.PHONETIC_NAME,
            Contacts.PHOTO_ID, Contacts.STARRED, Contacts.CONTACT_PRESENCE,
            Contacts.CONTACT_STATUS, Contacts.CONTACT_STATUS_TIMESTAMP,
            Contacts.CONTACT_STATUS_RES_PACKAGE,
            Contacts.CONTACT_STATUS_LABEL,
            Contacts.Entity.CONTACT_ID,
            Contacts.Entity.RAW_CONTACT_ID,

            // Contacts.Entity.DATA_ID,
            Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7,
            Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11, Data.DATA12, Data.DATA13,
            Data.DATA14, Data.DATA15, Data.SYNC1, Data.SYNC2, Data.SYNC3, Data.SYNC4,
            Data.DATA_VERSION, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY, Data.MIMETYPE,
    // Data.RES_PACKAGE,
    };

    private static final int COLUMN_PHONE_ID = 0;

    private static final int COLUMN_PHONE_NUMBER = 1;

    private static final int COLUMN_PHONE_LABEL = 2;

    private static final int COLUMN_CONTACT_NAME = 3;

    private static final int COLUMN_CONTACT_ID = 4;

    private static final int COLUMN_CONTACT_PRESENCE = 5;

    private static final int COLUMN_CONTACT_STATUS = 6;

    private static final int COLUMN_CONTACT_EMAIL_ADDR = 7;

    private static final int COLUMN_CONTACT_EMAIL_TYPE = 8;

    String mPhoneNum;

    String mDisplayName;

    String mEmailAddr;

    int mEmailType;

    public Contact(String name, String number, String emailAddr, int emailType) {
        mDisplayName = name;
        mPhoneNum = number;
        mEmailAddr = emailAddr;
        mEmailType = emailType;
    }

    /**
     * Get number-name pair according to contact phone id
     * 
     * @param context
     * @param ids
     * @return
     */
    public static List<Contact> getContactInfoForPhoneIds(Context context, long[] ids) {
        if (ids.length == 0) {
            return null;
        }
        StringBuilder idSetBuilder = new StringBuilder();
        boolean first = true;
        for (long id : ids) {
            if (first) {
                first = false;
                idSetBuilder.append(id);
            } else {
                idSetBuilder.append(',').append(id);
            }
        }

        Cursor cursor = null;
        if (idSetBuilder.length() > 0) {
            final String whereClause = Phone._ID + " IN (" + idSetBuilder.toString() + ")";
            cursor = context.getContentResolver().query(PHONES_WITH_ID_URI, CALLER_ID_PROJECTION,
                    whereClause, null, null);
        }

        if (cursor == null) {
            return null;
        }

        List<Contact> entries = new ArrayList<Contact>();

        try {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(COLUMN_CONTACT_NAME);
                String phoneNumber = cursor.getString(COLUMN_PHONE_NUMBER);
                String emailAddr = cursor.getString(COLUMN_CONTACT_EMAIL_ADDR);
                int emailType = cursor.getInt(COLUMN_CONTACT_EMAIL_TYPE);

                String numberExcludeSpace = ((phoneNumber == null) ? "" : phoneNumber.replace(" ",
                        "").replace("-", ""));
                Contact entry = new Contact(displayName, numberExcludeSpace, emailAddr, emailType);
                entries.add(entry);
            }
        } finally {
            cursor.close();
        }
        return entries;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.mPhoneNum = phoneNum;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getEmailAddr() {
        return mEmailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.mEmailAddr = emailAddr;
    }

    public int getEmailType() {
        return mEmailType;
    }

    public void setEmailType(int emailType) {
        this.mEmailType = emailType;
    }
}
