
package com.mediatek.contacts.model;

import com.android.contacts.R;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.BaseAccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.BaseAccountType.EventActionInflater;
import com.android.contacts.model.BaseAccountType.SimpleInflater;
import com.android.contacts.util.DateUtils;
import com.android.internal.telephony.AlphaTag;
import com.google.android.collect.Lists;
import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMAas;

import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class UsimAccountType extends BaseAccountType {
    public static final String TAG = "UsimAccountType";
    public static final String ACCOUNT_TYPE = AccountType.ACCOUNT_TYPE_USIM;

    public UsimAccountType(Context context, String resPackageName) {
        this.accountType = ACCOUNT_TYPE;
        this.titleRes = R.string.account_type_usim_label;
        this.iconRes = R.drawable.ic_contact_account_usim;
        this.resPackageName = null;
        this.summaryResPackageName = resPackageName;

        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            // addDataKindPhoneticName(context);
            // addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            // addDataKindStructuredPostal(context);
            // addDataKindIm(context);
            // addDataKindOrganization(context);
            addDataKindPhoto(context);
            // addDataKindNote(context);
            // addDataKindEvent(context);
            // addDataKindWebsite(context);
            addDataKindGroupMembership(context);
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }
    }

    @Override
    protected DataKind addDataKindStructuredName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                R.string.nameLabelsGroup, -1, true, R.layout.structured_name_editor_view));
        kind.actionHeader = new SimpleInflater(R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater(Nickname.NAME);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.DISPLAY_NAME, R.string.full_name,
                FLAGS_PERSON_NAME));
        
        kind.mAccountType = AccountType.ACCOUNT_TYPE_USIM;
        return kind;
    }

    @Override
    protected DataKind addDataKindDisplayName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME,
                R.string.nameLabelsGroup, -1, true, R.layout.text_fields_editor_view));
        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.DISPLAY_NAME, R.string.full_name,
                FLAGS_PERSON_NAME));
        
        kind.mAccountType = AccountType.ACCOUNT_TYPE_USIM;
        return kind;
    }

    @Override
    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhone(context);
        Log.d("UsimAccountType", "call addDataKindPhone()");
        kind.typeColumn = Phone.TYPE;
        if (OperatorUtils.isOp03Enabled()) {
            int slotId = ContactEditorFragment.getSlotId();
            BaseAccountType.updatePhoneType(slotId, kind);
        } else {
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE).setSpecificMax(2));
            kind.typeList.add(buildPhoneType(Phone.TYPE_OTHER).setSpecificMax(1));
            kind.typeOverallMax = 2;

            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));
        }
        kind.mAccountType = AccountType.ACCOUNT_TYPE_USIM;
        return kind;
    }

    @Override
    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindEmail(context);

        kind.typeOverallMax = 1;
        kind.typeColumn = Email.TYPE;
        kind.typeList = Lists.newArrayList();
        if (!OperatorUtils.isOp03Enabled()) {
            kind.typeList.add(buildEmailType(Email.TYPE_MOBILE));
        }

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));

        return kind;
    }

    @Override
    protected DataKind addDataKindStructuredPostal(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindStructuredPostal(context);

        final boolean useJapaneseOrder = Locale.JAPANESE.getLanguage().equals(
                Locale.getDefault().getLanguage());
        kind.typeColumn = StructuredPostal.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_WORK).setSpecificMax(1));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_HOME).setSpecificMax(1));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_OTHER).setSpecificMax(1));

        kind.fieldList = Lists.newArrayList();
        if (useJapaneseOrder) {
            kind.fieldList.add(new EditField(StructuredPostal.COUNTRY, R.string.postal_country,
                    FLAGS_POSTAL).setOptional(true));
            kind.fieldList.add(new EditField(StructuredPostal.POSTCODE, R.string.postal_postcode,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.REGION, R.string.postal_region,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.CITY, R.string.postal_city,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
        } else {
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.CITY, R.string.postal_city,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.REGION, R.string.postal_region,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POSTCODE, R.string.postal_postcode,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.COUNTRY, R.string.postal_country,
                    FLAGS_POSTAL).setOptional(true));
        }

        return kind;
    }

    @Override
    protected DataKind addDataKindIm(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindIm(context);

        // Types are not supported for IM. There can be 3 IMs, but OWA only
        // shows only the first
        kind.typeOverallMax = 3;

        kind.defaultValues = new ContentValues();
        kind.defaultValues.put(Im.TYPE, Im.TYPE_OTHER);

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Im.DATA, R.string.imLabelsGroup, FLAGS_EMAIL));

        return kind;
    }

    @Override
    protected DataKind addDataKindOrganization(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindOrganization(context);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Organization.COMPANY, R.string.ghostData_company,
                FLAGS_GENERIC_NAME));
        kind.fieldList.add(new EditField(Organization.TITLE, R.string.ghostData_title,
                FLAGS_GENERIC_NAME));

        return kind;
    }

    @Override
    protected DataKind addDataKindPhoto(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhoto(context);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Photo.PHOTO, -1, -1));

        kind.mAccountType = AccountType.ACCOUNT_TYPE_USIM;
        return kind;
    }

    @Override
    protected DataKind addDataKindNote(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindNote(context);

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Note.NOTE, R.string.label_notes, FLAGS_NOTE));

        return kind;
    }

    protected DataKind addDataKindEvent(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(Event.CONTENT_ITEM_TYPE, R.string.eventLabelsGroup,
                150, true, R.layout.event_field_editor_view));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater(Event.START_DATE);

        kind.typeOverallMax = 1;

        kind.typeColumn = Event.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, false).setSpecificMax(1));

        kind.dateFormatWithYear = DateUtils.DATE_AND_TIME_FORMAT;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Event.DATA, R.string.eventLabelsGroup, FLAGS_EVENT));

        return kind;
    }

    @Override
    protected DataKind addDataKindWebsite(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindWebsite(context);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Website.URL, R.string.websiteLabelsGroup, FLAGS_WEBSITE));

        return kind;
    }
//google 4.03 delete this function
//    @Override
//    public int getHeaderColor(Context context) {
//        return 0xffd5ba96;
//    }
//
//    @Override
//    public int getSideBarColor(Context context) {
//        return 0xffb58e59;
//    }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
}
