package com.mediatek.contacts.model;

import com.android.contacts.R;
import com.android.contacts.model.BaseAccountType;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.BaseAccountType.EventActionInflater;
import com.android.contacts.model.BaseAccountType.ImActionInflater;
import com.android.contacts.model.BaseAccountType.SimpleInflater;
import com.android.contacts.util.DateUtils;
import com.android.contacts.model.AccountType;
import com.google.android.collect.Lists;
import com.mediatek.contacts.util.OperatorUtils;

import android.content.ContentValues;
import android.content.Context;
import android.net.sip.SipManager;
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

import java.util.Locale;

public class LocalPhoneAccountType extends BaseAccountType {
    public static final String TAG = "LocalPhoneAccountType";
    
	public static final String ACCOUNT_TYPE = AccountType.ACCOUNT_TYPE_LOCAL_PHONE;

	public LocalPhoneAccountType(Context context, String resPackageName) {
	    this.accountType = ACCOUNT_TYPE;
        this.resPackageName = null;
        this.summaryResPackageName = resPackageName;
        this.titleRes = R.string.account_phone;
        this.iconRes = R.drawable.ic_contact_account_phone;
        
        try {
            boolean bl = OperatorUtils.getOptrProperties().equals("OP01");
            Log.i("yongjian","bl : "+bl);
            
            if (OperatorUtils.getOptrProperties().equals("OP01")) {
                addDataKindStructuredNameForCMCC(context);
                addDataKindDisplayNameForCMCC(context);
                addDataKindImForCMCC(context);
            } else {
                addDataKindStructuredName(context);
                addDataKindDisplayName(context);
                addDataKindIm(context);
            }
            
            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
//            addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
//            addDataKindEvent(context);
            addDataKindWebsite(context);
            addDataKindGroupMembership(context);
            
            if (SipManager.isVoipSupported(context)) {
                addDataKindSipAddress(context);
            }
        } catch (DefinitionException e) {
            Log.e(TAG, "Problem building account type", e);
        }
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
            kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD, R.string.postal_neighborhood,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POBOX, R.string.postal_pobox,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
        } else {
            kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.POBOX, R.string.postal_pobox,
                    FLAGS_POSTAL));
            kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD, R.string.postal_neighborhood,
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
    
    
    protected DataKind addDataKindStructuredNameForCMCC(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                R.string.nameLabelsGroup, -1, true, R.layout.structured_name_editor_view));
        kind.actionHeader = new SimpleInflater(R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater(Nickname.NAME);

        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                FLAGS_PERSON_NAME).setOptional(true));
        kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                R.string.name_family, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                R.string.name_middle, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                R.string.name_given, FLAGS_PERSON_NAME));
        kind.fieldList.add(new EditField(StructuredName.SUFFIX,
                R.string.name_suffix, FLAGS_PERSON_NAME));

        kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                R.string.name_phonetic_family, FLAGS_PHONETIC));
        kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                R.string.name_phonetic_given, FLAGS_PHONETIC));

        return kind;
    }

    protected DataKind addDataKindDisplayNameForCMCC(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME,
                R.string.nameLabelsGroup, -1, true, R.layout.text_fields_editor_view));

        boolean displayOrderPrimary =
                context.getResources().getBoolean(R.bool.config_editor_field_order_primary);
        kind.typeOverallMax = 1;

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                FLAGS_PERSON_NAME).setOptional(true));
        if (!displayOrderPrimary) {
            kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                    R.string.name_family, FLAGS_PERSON_NAME));
            kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                    R.string.name_middle, FLAGS_PERSON_NAME).setOptional(true));
            kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                    R.string.name_given, FLAGS_PERSON_NAME));
        } else {
            kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME,
                    R.string.name_given, FLAGS_PERSON_NAME));
            kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME,
                    R.string.name_middle, FLAGS_PERSON_NAME).setOptional(true));
            kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME,
                    R.string.name_family, FLAGS_PERSON_NAME));
        }
        kind.fieldList.add(new EditField(StructuredName.SUFFIX,
                R.string.name_suffix, FLAGS_PERSON_NAME).setOptional(true));

        return kind;
    }
    
    
    protected DataKind addDataKindImForCMCC(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(Im.CONTENT_ITEM_TYPE, R.string.imLabelsGroup, 20, true,
                    R.layout.text_fields_editor_view));
        kind.actionHeader = new ImActionInflater();
        kind.actionBody = new SimpleInflater(Im.DATA);

        // NOTE: even though a traditional "type" exists, for editing
        // purposes we're using the protocol to pick labels

        kind.defaultValues = new ContentValues();
        kind.defaultValues.put(Im.TYPE, Im.TYPE_OTHER);

        kind.typeColumn = Im.PROTOCOL;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildImType(Im.PROTOCOL_AIM));
        kind.typeList.add(buildImType(Im.PROTOCOL_MSN));
        kind.typeList.add(buildImType(Im.PROTOCOL_YAHOO));
        kind.typeList.add(buildImType(Im.PROTOCOL_SKYPE));
        kind.typeList.add(buildImType(Im.PROTOCOL_QQ));
        kind.typeList.add(buildImType(Im.PROTOCOL_ICQ));
        kind.typeList.add(buildImType(Im.PROTOCOL_JABBER));
        kind.typeList.add(buildImType(Im.PROTOCOL_CUSTOM).setSecondary(true).setCustomColumn(
                Im.CUSTOM_PROTOCOL));

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Im.DATA, R.string.imLabelsGroup, FLAGS_EMAIL));

        return kind;
    }

    // google 4.03 delete this function
    // @Override
    // public int getHeaderColor(Context context) {
    // return 0xffd5ba96;
    // }
    //
    // @Override
    // public int getSideBarColor(Context context) {
    // return 0xffb58e59;
    // }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }
}
