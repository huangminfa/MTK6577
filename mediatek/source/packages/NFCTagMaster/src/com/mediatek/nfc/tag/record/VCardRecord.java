
package com.mediatek.nfc.tag.record;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.vcard.VCardBuilder;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntry.EmailData;
import com.android.vcard.VCardEntry.ImData;
import com.android.vcard.VCardEntry.NicknameData;
import com.android.vcard.VCardEntry.NoteData;
import com.android.vcard.VCardEntry.OrganizationData;
import com.android.vcard.VCardEntry.PhoneData;
import com.android.vcard.VCardEntry.PostalData;
import com.android.vcard.VCardEntry.WebsiteData;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardVersionException;
import com.mediatek.nfc.tag.R;
import com.mediatek.nfc.tag.TagTypePreference;
import com.mediatek.nfc.tag.provider.TagContract;
import com.mediatek.nfc.tag.utils.Contact;
import com.mediatek.nfc.tag.utils.ContactItemView;
import com.mediatek.nfc.tag.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VCardRecord extends ParsedNdefRecord {
    private static final String TAG = Utils.TAG + "/VCardRecord";

    private static final int REQUEST_CODE_CONTACT = 1;

    private static final Uri URI_CONTACT_DATA = Data.CONTENT_URI;

    private static final String COLUMN_DATA_CONTACT_ID = "raw_contact_id";

    private static final String DATA_MIME_PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;

    private static final String DATA_MIME_EMAIL = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;

    private static final String VCARD_PREFIX_NAME = "FN";

    private static final String VCARD_PREFIX_PHONE = "TEL";

    private static final String VCARD_PREFIX_EMAIL = "EMAIL";

    // Tag history database
    private static final String DB_COLUMN_CONTENT = TagContract.COLUMN_01;

    // private static final String DB_COLUMN_DISPLAY_NAME =
    // TagContract.COLUMN_02;
    private static final String[] PROJECTION = {
            TagContract.COLUMN_TYPE, TagContract.COLUMN_DATE, DB_COLUMN_CONTENT,
            // DB_COLUMN_DISPLAY_NAME,
            TagContract.COLUMN_BYTES
    };

    private static ParsedNdefRecord sRecord;

    private static Activity sActivity;

    // This byte array stand for vCard file content, used for tag read and tag
    // editor, not history
    private byte[] mVCard = null;

    // For tag info editor
    private View mEditContentView = null;

    private LinearLayout mSelectView = null;

    private LinearLayout mEditDetailLayout = null;

    // For tag info reader
    private View mReadContentView = null;

    private LinearLayout mReadDetailLayout = null;

    // For history tag view
    private View mHistoryContentView = null;

    private LinearLayout mHistoryDetailLayout = null;

    private int mHistoryUriId;

    private NdefMessage mHistoryMessage = null;

    /**
     * Detail item list
     */
    private List<DetailItem> mDetailStructuredNameList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailPhoneList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailEmailList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailIMList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailNickNameList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailWebSiteList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailInternetCallList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailPostalAddressList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailNoteList = new ArrayList<DetailItem>();

    private List<DetailItem> mDetailOrganizationList = new ArrayList<DetailItem>();

    /**
     * Class stand for a row item in data table
     */
    public class DetailItem {
        public int mType = -1;// data2

        public String mValue;// data1

        public int mTypeDescRes = -1;

        public boolean mSelected = true;

        public int mProtocalType = -1;// data5 for IM only

        public StructNameDetail mNameDetail = null; // For struct name item

        public POAddressDetail mPoAddressDetail = null; // For struct postal

        // address

        public DetailItem(int type, String value, boolean selected) {
            this(type, value, selected, -1);
        }

        public DetailItem(StructNameDetail nameDetail, boolean selected) {
            this(-1, (nameDetail == null) ? "" : nameDetail.mDisplayName, selected, -1, -1,
                    nameDetail, null);
        }

        public DetailItem(int type, String value, boolean selected, int typeDescRes) {
            this(type, value, selected, typeDescRes, -1, null, null);
        }

        public DetailItem(int type, String value, boolean selected, int typeDescRes,
                int protocalType) {
            this(type, value, selected, typeDescRes, protocalType, null, null);
        }

        public DetailItem(int type, String value, boolean selected, int typeDescRes,
                POAddressDetail poDetail) {
            this(type, value, selected, typeDescRes, -1, null, poDetail);
        }

        public DetailItem(int type, String value, boolean selected, int typeDescRes,
                int protocalType, StructNameDetail nameDetail, POAddressDetail poDetail) {
            this.mType = type;
            this.mValue = value;
            this.mSelected = selected;
            this.mTypeDescRes = typeDescRes;
            this.mProtocalType = protocalType;
            this.mNameDetail = nameDetail;
            this.mPoAddressDetail = poDetail;
        }
    }

    /**
     * Postal Struct address contain more than one item
     */
    class POAddressDetail {
        String mStreet; // Data4

        String mPoBox; // Data5

        String mNeighborhood; // Data6

        String mRregion; // Data7

        String mCity; // Data8

        String mPostCode; // Data9

        String mCountry; // Data10

        public POAddressDetail(String street, String poBox, String neighborhood, String region,
                String city, String postCode, String country) {
            this.mStreet = street;
            this.mPoBox = poBox;
            this.mNeighborhood = neighborhood;
            this.mRregion = region;
            this.mCity = city;
            this.mPostCode = postCode;
            this.mCountry = country;
        }

        /**
         * Append this address instance into the given content, if null is
         * given, make a new one
         * 
         * @return
         */
        public ContentValues appendToContentValue(ContentValues contentValues) {
            if (contentValues == null) {
                contentValues = new ContentValues();
            }
            contentValues.put(StructuredPostal.STREET, mStreet);
            contentValues.put(StructuredPostal.POBOX, mPoBox);
            contentValues.put(StructuredPostal.NEIGHBORHOOD, mNeighborhood);
            contentValues.put(StructuredPostal.REGION, mRregion);
            contentValues.put(StructuredPostal.CITY, mCity);
            contentValues.put(StructuredPostal.POSTCODE, mPostCode);
            contentValues.put(StructuredPostal.COUNTRY, mCountry);

            return contentValues;
        }

        @Override
        public String toString() {
            return "Street: " + mStreet + "\n" + "Postat Box: " + mPoBox + "\n" + "Neighborhood: "
                    + mNeighborhood + "\n" + "Region: " + mRregion + "\n" + "City: " + mCity + "\n"
                    + "Postal code: " + mPostCode + "\n" + "Country: " + mCountry + "\n";
        }
    }

    /**
     * Detail info of struct name item
     */
    class StructNameDetail {
        String mDisplayName; // Data1

        String mGivenName; // Data2

        String mFamilyName; // Data3

        String mPrefix; // Data4

        String mMiddleName; // Data5

        String mSuffix; // Data6

        String mPhoneticGivenName; // Data7

        String mPhoneticMidleName; // Data8

        String mPhoneticFamilyName; // Data9

        String mFullNameStyle; // Data10

        String mPhoneticNameStyle; // Data11

        public StructNameDetail(String displayName, String givenName, String familyName,
                String prefix, String middleName, String suffix, String phoneticGivenName,
                String phoneticMidleName, String phoneticFamilyName, String fullNameStyle,
                String phoneticNameStyle) {
            this.mDisplayName = displayName;
            this.mGivenName = givenName;
            this.mFamilyName = familyName;
            this.mPrefix = prefix;
            this.mMiddleName = middleName;
            this.mSuffix = suffix;
            this.mPhoneticGivenName = phoneticGivenName;
            this.mPhoneticMidleName = phoneticMidleName;
            this.mPhoneticFamilyName = phoneticFamilyName;
            this.mFullNameStyle = fullNameStyle;
            this.mPhoneticNameStyle = phoneticNameStyle;
        }

        /**
         * Append this user name instance into the given content, if null is
         * given, make a new one
         * 
         * @return
         */
        public ContentValues appendToContentValue(ContentValues contentValues) {
            if (contentValues == null) {
                contentValues = new ContentValues();
            }
            contentValues.put(StructuredName.DISPLAY_NAME, mDisplayName);
            contentValues.put(StructuredName.GIVEN_NAME, mGivenName);
            contentValues.put(StructuredName.FAMILY_NAME, mFamilyName);
            contentValues.put(StructuredName.PREFIX, mPrefix);
            contentValues.put(StructuredName.MIDDLE_NAME, mMiddleName);
            contentValues.put(StructuredName.SUFFIX, mSuffix);
            contentValues.put(StructuredName.PHONETIC_GIVEN_NAME, mPhoneticGivenName);
            contentValues.put(StructuredName.PHONETIC_MIDDLE_NAME, mPhoneticMidleName);
            contentValues.put(StructuredName.PHONETIC_FAMILY_NAME, mPhoneticFamilyName);
            contentValues.put("data10", mFullNameStyle);
            contentValues.put("data11", mPhoneticNameStyle);
            return contentValues;
        }
    }

    public static ParsedNdefRecord getInstance(Activity activity) {
        if (sRecord == null) {
            Utils.logi(TAG, "Create vCard record instance now");
            sRecord = new VCardRecord();
        }
        sActivity = activity;
        return sRecord;
    }

    @Override
    public View getEditView() {
        mVCard = null;// Remove cached data
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mEditContentView = inflater.inflate(R.xml.edit_view_vcard, null);
        if (mEditContentView != null) {
            mSelectView = (LinearLayout) mEditContentView
                    .findViewById(R.id.edit_info_vcard_selector);
            mEditDetailLayout = (LinearLayout) mEditContentView
                    .findViewById(R.id.editor_view_vcard_detail);
            if (mSelectView != null) {
                mSelectView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSelectContactPage();
                    }
                });
            }
            if (mEditDetailLayout != null) {
                mEditDetailLayout.removeAllViews();
            }
        }

        // Start contact select page directly
        startSelectContactPage();

        return mEditContentView;
    }

    private void startSelectContactPage() {
        Utils.logd(TAG, "-->startContactSelectorPage()");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        sActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT);
        Utils.logd(TAG, "<--startContactSelectorPage()");
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        Utils.logd(TAG, "onActivityResultCallback(), requestCode=" + requestCode + ", resultCode="
                + resultCode);
        if (requestCode == REQUEST_CODE_CONTACT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri lookupUri = data.getData();
                Utils.logd(TAG, "Selected contact uri=" + lookupUri);

                // Get vCard info directly from Contact content provider
                // get contact detail info and vCard bytes according to contact
                // uri
                if (lookupUri != null) {
                    // Find display name and lookup key, lookup key will be used
                    // to get vCard bytes
                    Cursor cursor = null;
                    String lookupKey = null;
                    String[] projectionContact = {
                            ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.LOOKUP_KEY
                    };
                    try {
                        cursor = sActivity.getContentResolver().query(lookupUri, projectionContact,
                                null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            lookupKey = cursor.getString(2);
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                    }

                    if (lookupKey == null) {
                        Utils.loge(TAG, "Fail to get selected Contact\'s lookup key");
                        return;
                    }

                    // Get vCard bytes array
                    Utils.logi(TAG, "Lookup key=" + lookupKey);
                    Uri vCardUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
                    Utils.logi(TAG, "After query, vCardUri = " + vCardUri);

                    AssetFileDescriptor descriptor = null;
                    FileInputStream in = null;

                    try {
                        descriptor = sActivity.getContentResolver().openAssetFileDescriptor(
                                vCardUri, "r");
                        mVCard = new byte[(int) descriptor.getLength()];
                        in = descriptor.createInputStream();
                        in.read(mVCard);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mVCard = null;
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Utils.logi(TAG, "Selected vCard:[[\n" + new String(mVCard) + "\n\n]]");

                    // Get contact detail info from Contact.Data table, this is
                    // the raw_contact_id column
                    long contactId = ContentUris.parseId(lookupUri);
                    getContactDetailInfoFromDB(contactId);
                    fillEditPage();
                }
            } else {
                Utils.logw(TAG,
                        "-->onActivityResultCallback(), no contact is selected. resultCode="
                                + resultCode + ", data=null?" + (data == null));
                sActivity.finish();
            }
        }
    }

    /**
     * Get contact detail info from Data table by raw_contact_id
     */
    private void getContactDetailInfoFromDB(long contactId) {
        Utils.logd(TAG, "-->getContactDetailInfo(), contactId=" + contactId);
        String dataWhereStr = Contacts.Entity.RAW_CONTACT_ID + "=" + contactId;
        Cursor dataCursor = sActivity.getContentResolver().query(URI_CONTACT_DATA,
                Contact.DATA_DETAIL_COLUMNS, dataWhereStr, null, null);
        if (dataCursor != null) {
            // clear old data first
            clearCacheDetailList();

            if (dataCursor.moveToFirst()) {
                while (!dataCursor.isAfterLast()) {
                    String mimeType = dataCursor
                            .getString(dataCursor.getColumnIndex(Data.MIMETYPE));
                    String value = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA1));
                    int type = dataCursor.getInt(dataCursor.getColumnIndex(Data.DATA2));

                    if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // Phone number record
                        Utils.logi(TAG, "Structured display name" + ":  " + value);
                        String displayName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.DISPLAY_NAME));
                        String givenName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.GIVEN_NAME));
                        String familyName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.FAMILY_NAME));
                        String prefix = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.PREFIX));
                        String middleName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.MIDDLE_NAME));
                        String suffix = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.SUFFIX));
                        String phoneticGivenName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.PHONETIC_GIVEN_NAME));
                        String phoneticMiddleName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.PHONETIC_MIDDLE_NAME));
                        String phoneticFamilyName = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredName.PHONETIC_FAMILY_NAME));
                        String fullNameStyle = dataCursor.getString(dataCursor
                                .getColumnIndex("data10"));
                        String phoneticNameStyle = dataCursor.getString(dataCursor
                                .getColumnIndex("data10"));
                        StructNameDetail nameDetail = new StructNameDetail(displayName, givenName,
                                familyName, prefix, middleName, suffix, phoneticGivenName,
                                phoneticMiddleName, phoneticFamilyName, fullNameStyle,
                                phoneticNameStyle);
                        DetailItem item = new DetailItem(nameDetail, true);
                        mDetailStructuredNameList.add(item);
                    } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // Phone number record
                        Utils.logi(TAG, "Phone number("
                                + sActivity.getResources().getString(
                                        Phone.getTypeLabelResource(type)) + "):  " + value);
                        DetailItem item = new DetailItem(type, value.replace("-", ""), true, Phone
                                .getTypeLabelResource(type));
                        Utils.logi(TAG, "New phone number=" + value.replace("-", ""));
                        mDetailPhoneList.add(item);
                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // Email address
                        Utils.logi(TAG, "Email address("
                                + sActivity.getResources().getString(
                                        Email.getTypeLabelResource(type)) + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true, Email
                                .getTypeLabelResource(type));
                        mDetailEmailList.add(item);
                    } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) { // IM
                        int protocalType = dataCursor
                                .getInt(dataCursor.getColumnIndex(Im.PROTOCOL));
                        Utils
                                .logi(TAG, "Im ("
                                        + sActivity.getResources().getString(
                                                Im.getProtocolLabelResource(protocalType)) + "):  "
                                        + value);
                        DetailItem item = new DetailItem(type, value, true, Im
                                .getProtocolLabelResource(protocalType), protocalType);
                        mDetailIMList.add(item);
                    } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // Nick Name
                        Utils.logi(TAG, "NickName (" + type + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true);
                        mDetailNickNameList.add(item);
                    } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        // Web site
                        Utils.logi(TAG, "Web site (" + type + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true);
                        mDetailWebSiteList.add(item);
                    } else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType)) { // Internet
                        // call
                        Utils.logi(TAG, "Sip addres/Internet call ("
                                + sActivity.getResources().getString(
                                        SipAddress.getTypeLabelResource(type)) + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true, SipAddress
                                .getTypeLabelResource(type));
                        mDetailInternetCallList.add(item);
                    } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) { // Postal
                        // address
                        Utils.logi(TAG, "Posta address ("
                                + sActivity.getResources().getString(
                                        StructuredPostal.getTypeLabelResource(type)) + "):  "
                                + value);
                        String street = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.STREET));
                        String poBox = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.POBOX));
                        String neighborhood = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.NEIGHBORHOOD));
                        String region = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.REGION));
                        String city = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.CITY));
                        String postCode = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.POSTCODE));
                        String country = dataCursor.getString(dataCursor
                                .getColumnIndex(StructuredPostal.COUNTRY));

                        POAddressDetail addressDetail = new POAddressDetail(street, poBox,
                                neighborhood, region, city, postCode, country);
                        Utils
                                .logi(TAG, "PostalStruct address info:  \n"
                                        + addressDetail.toString());

                        DetailItem item = new DetailItem(type, value, true, StructuredPostal
                                .getTypeLabelResource(type), addressDetail);
                        mDetailPostalAddressList.add(item);
                    } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) { // Note
                        Utils.logi(TAG, "Note (" + type + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true);
                        mDetailNoteList.add(item);
                    } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) { // Organization
                        Utils.logi(TAG, "Organization ("
                                + sActivity.getResources().getString(
                                        Organization.getTypeLabelResource(type)) + "):  " + value);
                        DetailItem item = new DetailItem(type, value, true, Organization
                                .getTypeLabelResource(type));
                        mDetailOrganizationList.add(item);
                    } else { // unknown type
                        Utils.logw(TAG, "Unknow mime type(" + mimeType + "), sub-type (" + type
                                + "):  " + value);
                    }
                    dataCursor.moveToNext();
                }
            }
            dataCursor.close();
        } else {
            Utils.loge(TAG, "Fail to get detail info from data table");
        }
    }

    /**
     * Before query new data from DB, clear old cached items
     */
    private void clearCacheDetailList() {
        mDetailStructuredNameList.clear();
        mDetailNickNameList.clear();
        mDetailPhoneList.clear();
        mDetailEmailList.clear();
        mDetailPostalAddressList.clear();
        mDetailOrganizationList.clear();
        mDetailWebSiteList.clear();
        mDetailNoteList.clear();
        mDetailIMList.clear();
        mDetailInternetCallList.clear();
    }

    /**
     * Fill contact editor page with info extracted from contact database
     */
    private void fillEditPage() {
        Utils.logd(TAG, "-->fillEditPage()");
        if (mEditDetailLayout != null) {
            mEditDetailLayout.removeAllViews();
            // add each detail category, if exist
            pasteDetailEditInfo(mEditDetailLayout, mDetailStructuredNameList,
                    R.string.vcard_category_title_name);
            pasteDetailEditInfo(mEditDetailLayout, mDetailPhoneList,
                    R.string.vcard_category_title_phone);
            pasteDetailEditInfo(mEditDetailLayout, mDetailEmailList,
                    R.string.vcard_category_title_email);
            pasteDetailEditInfo(mEditDetailLayout, mDetailIMList, R.string.vcard_category_title_im);
            pasteDetailEditInfo(mEditDetailLayout, mDetailNickNameList,
                    R.string.vcard_category_title_nickname);
            pasteDetailEditInfo(mEditDetailLayout, mDetailWebSiteList,
                    R.string.vcard_category_title_website);
            pasteDetailEditInfo(mEditDetailLayout, mDetailInternetCallList,
                    R.string.vcard_category_title_sip_address);
            pasteDetailEditInfo(mEditDetailLayout, mDetailPostalAddressList,
                    R.string.vcard_category_title_postal_address);
            pasteDetailEditInfo(mEditDetailLayout, mDetailNoteList,
                    R.string.vcard_category_title_note);
            pasteDetailEditInfo(mEditDetailLayout, mDetailOrganizationList,
                    R.string.vcard_category_title_organization);
        }
    }

    private void pasteDetailEditInfo(ViewGroup parent, List<DetailItem> list, int categoryTitleRes) {
        pasteDetailItemIntoLayout(parent, list, categoryTitleRes, true);
    }

    private void pasteDetailReadInfo(ViewGroup parent, List<DetailItem> list, int categoryTitleRes) {
        pasteDetailItemIntoLayout(parent, list, categoryTitleRes, false);
    }

    /**
     * Draw an item view according to its data list
     * 
     * @param parent where the view will be appended to
     * @param list data list
     * @param categoryTitleRes view category title
     * @param withCheckBox whether check box is needed
     */
    private void pasteDetailItemIntoLayout(ViewGroup parent, List<DetailItem> list,
            int categoryTitleRes, boolean withCheckBox) {
        Resources resources = sActivity.getResources();
        LayoutInflater inflater = LayoutInflater.from(sActivity);

        if (list != null && list.size() > 0) {
            View categoryView = ContactItemView.getCategoryHeaderView(sActivity, resources
                    .getString(categoryTitleRes));
            if (categoryView != null) {
                parent.addView(categoryView);
            } else {
                Utils.loge(TAG, "Fail to get item category title view");
            }
            for (int i = 0; i < list.size(); i++) {
                DetailItem item = list.get(i);
                String typeDescStr = null;
                if (item.mTypeDescRes > 0) {
                    typeDescStr = resources.getString(item.mTypeDescRes);
                }
                View itemView = new ContactItemView(sActivity, item, typeDescStr, withCheckBox)
                        .getItemView();
                if (itemView != null) {
                    if (i > 0) {
                        inflater.inflate(R.layout.item_divider, parent);
                    }
                    parent.addView(itemView);
                } else {
                    Utils.loge(TAG, "Fail to get item view for: " + item.mValue);
                }
            }
        }
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
        Utils.logd(TAG, "-->getHistoryView(), uriId=" + uriId);
        mVCard = null;// Remove cached data
        mHistoryUriId = uriId;
        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mHistoryContentView = inflater.inflate(R.xml.read_view_vcard, null);
        if (mHistoryContentView != null) {
            mHistoryDetailLayout = (LinearLayout) mHistoryContentView
                    .findViewById(R.id.read_view_vcard_detail);

            Uri uri = ContentUris.withAppendedId(TagContract.TAGS_CONTENT_URI, uriId);
            Cursor cursor = sActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String vCardStr = cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT));
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(TagContract.COLUMN_BYTES));
                    try {
                        mHistoryMessage = new NdefMessage(bytes);
                    } catch (FormatException e) {
                        Utils.loge(TAG, "Fail to parse byte array in history db to NdefMessage.");
                        e.printStackTrace();
                    }
                    Utils.logi(TAG, "History vCard string = [[\n\n" + vCardStr + "\n\n[]]");

                    if (vCardStr != null) {
                        // Get vCard content, so user can re-load this info from
                        // read history
                        mVCard = vCardStr.getBytes();
                    }
                    extractVCard(vCardStr);
                    fillReadPage(mHistoryDetailLayout);
                } else {
                    Utils.loge(TAG, "Fail to get phone number history record with id:" + uriId);
                }
                cursor.close();
            }
        }
        return mHistoryContentView;
    }

    public boolean handleHistoryReadTag() {
        Utils.logd(TAG, "-->handleHistoryReadTag()");
        return handleReadTag();
    };

    private void extractVCard(String vCardStr) {
        Utils.logd(TAG, "-->extractVCard()");

        final int type = VCardConfig.VCARD_TYPE_UNKNOWN;
        final VCardEntryConstructor constructor = new VCardEntryConstructor(type);
        boolean result = false;
        constructor.addEntryHandler(new VCardEntryHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onEnd() {
            }

            @Override
            public void onEntryCreated(VCardEntry entry) {
                clearCacheDetailList();
                DetailItem item = null;

                // User name
                String displayName = entry.getDisplayName();
                item = new DetailItem(-1, displayName, true);
                mDetailStructuredNameList.add(item);

                // Phone number
                List<PhoneData> phoneData = entry.getPhoneList();
                if (phoneData != null && phoneData.size() > 0) {
                    for (PhoneData data : phoneData) {
                        item = new DetailItem(data.getType(), data.getNumber(), true, Phone
                                .getTypeLabelResource(data.getType()));
                        mDetailPhoneList.add(item);
                    }
                }

                // Email address
                List<EmailData> emailData = entry.getEmailList();
                if (emailData != null && emailData.size() > 0) {
                    for (EmailData data : emailData) {
                        item = new DetailItem(data.getType(), data.getAddress(), true, Email
                                .getTypeLabelResource(data.getType()));
                        mDetailEmailList.add(item);
                    }
                }

                // IM
                List<ImData> imData = entry.getImList();
                if (imData != null && imData.size() > 0) {
                    for (ImData data : imData) {
                        Utils.loge(TAG, "Read IM data=" + data.getAddress());
                        item = new DetailItem(data.getType(), data.getAddress(), true, Im
                                .getProtocolLabelResource(data.getType()));
                        mDetailIMList.add(item);
                    }
                }

                // Nick name
                List<NicknameData> nickNameData = entry.getNickNameList();
                if (nickNameData != null && nickNameData.size() > 0) {
                    for (NicknameData data : nickNameData) {
                        item = new DetailItem(-1, data.getNickname(), true);
                        mDetailNickNameList.add(item);
                    }
                }

                // Web site
                List<WebsiteData> webSiteData = entry.getWebsiteList();
                if (webSiteData != null && webSiteData.size() > 0) {
                    for (WebsiteData data : webSiteData) {
                        item = new DetailItem(-1, data.getWebsite(), true);
                        mDetailWebSiteList.add(item);
                    }
                }

                // TODO It seems no Internet call supported in VCardEntry yet

                // Postal address
                List<PostalData> postalData = entry.getPostalList();
                if (postalData != null && postalData.size() > 0) {
                    for (PostalData data : postalData) {
                        item = new DetailItem(data.getType(), data
                                .getFormattedAddress(VCardConfig.VCARD_TYPE_DEFAULT), true);
                        mDetailPostalAddressList.add(item);
                    }
                }

                // Note
                List<NoteData> noteData = entry.getNotes();
                if (noteData != null && noteData.size() > 0) {
                    for (NoteData data : noteData) {
                        item = new DetailItem(-1, data.getNote(), true);
                        mDetailNoteList.add(item);
                    }
                }

                // Organization
                List<OrganizationData> organizationData = entry.getOrganizationList();
                if (organizationData != null && organizationData.size() > 0) {
                    for (OrganizationData data : organizationData) {
                        item = new DetailItem(data.getType(), data.getOrganizationName(), true,
                                Organization.getTypeLabelResource(data.getType()));
                        mDetailOrganizationList.add(item);
                    }
                }

                Utils.logi(TAG, "Encount a new record: " + entry.getDisplayName());

            }
        });

        VCardParser parser = new VCardParser_V21(type);
        try {
            try {
                parser.parse(new ByteArrayInputStream(vCardStr.getBytes()), constructor);
                result = true;
            } catch (VCardVersionException e) {
                parser = new VCardParser_V30(type);
                parser.parse(new ByteArrayInputStream(vCardStr.getBytes()), constructor);
                result = true;
            }
        } catch (IOException e) {
            Utils.loge(TAG, "IOException happened when parse vCard string.", e);
        } catch (VCardException e) {
            Utils.loge(TAG, "VCardException happened when parse vCard string.", e);
        }

        if (result) {
            Utils.logd(TAG, "Parse vCard file successfully.");
        } else {
            Utils.loge(TAG, "Fail to parse vCard file");
        }
    }

    private void fillReadPage(LinearLayout parentLayout) {
        Utils.logd(TAG, "-->fillReadPage()");
        if (parentLayout != null) {
            parentLayout.removeAllViews();
            // add each detail category, if exist
            pasteDetailReadInfo(parentLayout, mDetailStructuredNameList,
                    R.string.vcard_category_title_name);
            pasteDetailReadInfo(parentLayout, mDetailPhoneList, R.string.vcard_category_title_phone);
            pasteDetailReadInfo(parentLayout, mDetailEmailList, R.string.vcard_category_title_email);
            pasteDetailReadInfo(parentLayout, mDetailIMList, R.string.vcard_category_title_im);
            pasteDetailReadInfo(parentLayout, mDetailNickNameList,
                    R.string.vcard_category_title_nickname);
            pasteDetailReadInfo(parentLayout, mDetailWebSiteList,
                    R.string.vcard_category_title_website);
            pasteDetailReadInfo(parentLayout, mDetailInternetCallList,
                    R.string.vcard_category_title_sip_address);
            pasteDetailReadInfo(parentLayout, mDetailPostalAddressList,
                    R.string.vcard_category_title_postal_address);
            pasteDetailReadInfo(parentLayout, mDetailNoteList, R.string.vcard_category_title_note);
            pasteDetailReadInfo(parentLayout, mDetailOrganizationList,
                    R.string.vcard_category_title_organization);
        }
    }

    @Override
    public NdefMessage getNewNdefMessage() {
        Utils.logd(TAG, "-->getNdefRecord()");
        String vCardStr = composeVCard();
        mVCard = null;
        Utils.logi(TAG, "New composed vCard String:[[\n\n" + vCardStr + "\n\n]]");
        if (!TextUtils.isEmpty(vCardStr)) {
            mVCard = vCardStr.getBytes();
            String vCardMimeType = ContactsContract.Contacts.CONTENT_VCARD_TYPE;
            NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, vCardMimeType.getBytes(),
                    new byte[0], mVCard);
            // Save the new input record into database
            NdefMessage msg = new NdefMessage(new NdefRecord[] {
                record
            });
            String displayName = "";
            if (mDetailStructuredNameList != null && mDetailStructuredNameList.size() > 0) {
                StructNameDetail detail = mDetailStructuredNameList.get(0).mNameDetail;
                if (detail != null) {
                    displayName = detail.mDisplayName;
                } else {
                    displayName = mDetailStructuredNameList.get(0).mValue;
                }
            }

            addNewRecordToDB(msg, displayName, vCardStr, 1);
            return msg;
        }
        return null;
    }

    private String composeVCard() {
        Utils.logd(TAG, "-->composeVCard()");
        List<ContentValues> contentValueList = null;
        VCardBuilder vCardBuilder = new VCardBuilder(VCardConfig.VCARD_TYPE_DEFAULT);

        // User name
        contentValueList = transformDetailItemToContentValues(mDetailStructuredNameList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendNameProperties(contentValueList);
        }
        // Nick name
        contentValueList = transformDetailItemToContentValues(mDetailNickNameList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendNickNames(contentValueList);
        }
        // Phone number
        contentValueList = transformDetailItemToContentValues(mDetailPhoneList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendPhones(contentValueList, null);
        }
        // Email address
        contentValueList = transformDetailItemToContentValues(mDetailEmailList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendEmails(contentValueList);
        }
        // Postal address
        contentValueList = transformDetailItemToContentValues(mDetailPostalAddressList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendPostals(contentValueList);
        }
        // Organization
        contentValueList = transformDetailItemToContentValues(mDetailOrganizationList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendOrganizations(contentValueList);
        }
        // Web site
        contentValueList = transformDetailItemToContentValues(mDetailWebSiteList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendWebsites(contentValueList);
        }
        // Note
        contentValueList = transformDetailItemToContentValues(mDetailNoteList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendNotes(contentValueList);
        }
        // IM like QQ
        contentValueList = transformDetailItemToContentValues(mDetailIMList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendIms(contentValueList);
        }
        // Sip address
        contentValueList = transformDetailItemToContentValues(mDetailInternetCallList);
        if (contentValueList != null && contentValueList.size() > 0) {
            vCardBuilder.appendSipAddresses(contentValueList);
        }

        return vCardBuilder.toString();

    }

    private List<ContentValues> transformDetailItemToContentValues(List<DetailItem> itemList) {
        if (itemList == null || itemList.size() == 0) {
            return null;
        }
        List<ContentValues> results = new ArrayList<ContentValues>();
        for (DetailItem item : itemList) {
            if (item.mSelected) {
                ContentValues value = new ContentValues();
                if (!TextUtils.isEmpty(item.mValue)) {
                    value.put(Data.DATA1, item.mValue);
                }
                if (item.mType >= 0) {
                    value.put(Data.DATA2, item.mType);
                }
                if (item.mProtocalType >= 0) { // Apply for Im
                    value.put(Im.PROTOCOL, item.mProtocalType);
                }
                if (item.mNameDetail != null) { // For struct name
                    value = item.mNameDetail.appendToContentValue(value);
                }
                if (item.mPoAddressDetail != null) { // For postal address
                    value = item.mPoAddressDetail.appendToContentValue(value);
                }

                results.add(value);
            }
        }
        return results;
    }

    /**
     * Add a new tag record into tag database. If the same record already exist,
     * do not insert a duplicated one
     * 
     * @param msg the whole NDEF message
     * @param displayName string which can indentify this record
     * @param vCardStr vCard content
     * @param tagSrc tag origin, user created(1) or scanned from tag(0)
     * @return
     */
    private boolean addNewRecordToDB(NdefMessage msg, String displayName, String vCardStr,
            int tagSrc) {
        Utils.logd(TAG, "-->addNewRecordToDB(), displayName=" + displayName + ", vCardStr=\n"
                + vCardStr + ", isNewCreated?" + tagSrc);
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(TagContract.COLUMN_TYPE + "=" + Utils.TAG_TYPE_VCARD);
        selectionBuilder.append(" AND " + DB_COLUMN_CONTENT + "=\'"
                + Utils.encodeStrForDB(vCardStr) + "\' ");
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
            Utils.logw(TAG, "VCard record already exist, count=" + recordNum
                    + ", do not insert it again");
        } else {
            Utils.logi(TAG, "Insert new tag record into database");
            ContentValues values = new ContentValues();
            values.put(TagContract.COLUMN_DATE, System.currentTimeMillis());
            values.put(TagContract.COLUMN_TYPE, Utils.TAG_TYPE_VCARD);
            values.put(TagContract.COLUMN_BYTES, msg.toByteArray());
            values.put(TagContract.COLUMN_IS_CREATED_BY_ME, tagSrc);
            values.put(DB_COLUMN_CONTENT, vCardStr);
            values.put(TagContract.COLUMN_HISTORY_TITLE, displayName);

            Uri uri = sActivity.getContentResolver().insert(TagContract.TAGS_CONTENT_URI, values);
            if (uri == null) {
                Utils.loge(TAG, "Add new vCard record fail");
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
        mVCard = null;// Remove cached data
        NdefRecord record = msg.getRecords()[0];
        if (record == null) {
            Utils.loge(TAG, "Invalid NdefRecord [Null]");
            return null;
        }
        int tnf = record.getTnf();
        byte[] type = record.getType();
        byte[] payload = record.getPayload();
        if (tnf != NdefRecord.TNF_MIME_MEDIA
                || !ContactsContract.Contacts.CONTENT_VCARD_TYPE.equalsIgnoreCase(new String(type))) {
            Utils.loge(TAG, "Invalid tag type, tnf=" + tnf + ", type=" + (new String(type)));
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(sActivity);
        mReadContentView = inflater.inflate(R.xml.read_view_vcard, null);

        if (mReadContentView == null) {
            Utils.loge(TAG, "Fail to load read content view");
            return null;
        } else {
            mReadDetailLayout = (LinearLayout) mReadContentView
                    .findViewById(R.id.read_view_vcard_detail);
        }
        if (payload == null || payload.length == 0) {
            Utils.loge(TAG, "Payload is empty.");
            return null;
        }

        mVCard = payload;
        String payloadStr = new String(payload);

        extractVCard(payloadStr);
        fillReadPage(mReadDetailLayout);

        String displayName = "";
        if (mDetailStructuredNameList != null && mDetailStructuredNameList.size() > 0) {
            StructNameDetail detail = mDetailStructuredNameList.get(0).mNameDetail;
            if (detail != null) {
                displayName = detail.mDisplayName;
            } else {
                displayName = mDetailStructuredNameList.get(0).mValue;
            }
        }

        addNewRecordToDB(msg, displayName, payloadStr, 0);
        return mReadContentView;
    }

    @Override
    public int getTagHistoryItemIconResId() {
        return R.drawable.ic_tag_vcard;
    }

    @Override
    public int getTagHistoryItemSummaryResId() {
        return R.string.history_item_summary_vcard;
    }

    @Override
    public int getTagReaderActionStrResId() {
        return R.string.read_view_button_action_str_vcard;
    }

    @Override
    public int getTagReaderTypeIconResId() {
        return R.drawable.ic_tag_vcard;
    }

    @Override
    public int getTagReaderTypeSummaryResId() {
        return R.string.read_view_type_info_vcard_summary;
    }

    @Override
    public int getTagReaderTypeTitleResId() {
        return R.string.read_view_type_info_vcard_title;
    }

    @Override
    public int getTagType() {
        return Utils.TAG_TYPE_VCARD;
    }

    @Override
    public TagTypePreference getTagTypePreference() {
        TagTypePreference pref = new TagTypePreference(sActivity, R.drawable.ic_tag_vcard,
                sActivity.getString(R.string.tag_title_vcard));
        pref.setTagType(Utils.TAG_TYPE_VCARD);
        return pref;
    }

    @Override
    public boolean handleReadTag() {
        Utils.logd(TAG, "-->handleReadTag()");
        if (mVCard == null || mVCard.length == 0) {
            Utils.loge(TAG, "vCard byte array is empty");
            Toast.makeText(sActivity, R.string.error_vcard_empty_vcard, Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean storeSuccess = Utils.storeBytesIntoFile(sActivity, mVCard, Contact.VCARD_FILE_NAME);
        if (!storeSuccess) {
            Utils.loge(TAG, "Fail to store vCard info into a temp file for importing");
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(sActivity.getFilesDir(), Contact.VCARD_FILE_NAME));
        intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_VCARD_TYPE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sActivity.startActivity(intent);
        // Finish tag information preview page
        sActivity.finish();
        return true;
    }
}
