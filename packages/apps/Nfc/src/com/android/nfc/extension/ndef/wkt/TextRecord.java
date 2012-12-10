package com.android.nfc.extension.ndef.wkt;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.Record;
import com.android.nfc.extension.nfc.utility.Preconditions;

/**
 * @hide
 */
public class TextRecord {

	/** ISO/IANA language code */
    private final String mLanguageCode;

    private final String mText;

    private TextRecord(String languageCode, String text) {
        mLanguageCode = Preconditions.checkNotNull(languageCode);
        mText = Preconditions.checkNotNull(text);
    }

    public String getText() {
        return mText;
    }

    /**
     * Returns the ISO/IANA language code associated with this text element.
     */
    public String getLanguageCode() {
        return mLanguageCode;
    }
    
 // TODO: deal with text fields which span multiple NdefRecords
    public static TextRecord parse(Record record) throws UnsupportedEncodingException {
        Preconditions.checkArgument(record.getTNF() == NDEF.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(), NDEF.RTD_TEXT));
        
            byte[] payload = record.getPayload();
            /*
             * payload[0] contains the "Status Byte Encodings" field, per the
             * NFC Forum "Text Record Type Definition" section 3.2.1.
             *
             * bit7 is the Text Encoding Field.
             *
             * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
             * The text is encoded in UTF16
             *
             * Bit_6 is reserved for future use and must be set to zero.
             *
             * Bits 5 to 0 are the length of the IANA language code.
             */
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0x3F;
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            String text =
                new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return new TextRecord(languageCode, text);       
    }

    public static boolean isText(Record record) throws UnsupportedEncodingException {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
