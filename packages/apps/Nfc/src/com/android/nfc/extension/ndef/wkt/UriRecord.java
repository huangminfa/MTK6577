package com.android.nfc.extension.ndef.wkt;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.Record;
import com.android.nfc.extension.nfc.utility.Preconditions;

/**
 * @hide
 */
public class UriRecord {

	  /**
     * NFC Forum "URI Record Type Definition"
     *
     * This is a mapping of "URI Identifier Codes" to URI string prefixes,
     * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static final Map<Byte, String> URI_PREFIX_MAP = new HashMap<Byte, String>();
                        

    private final URI mUri;

    private UriRecord(URI uri) {
        this.mUri = Preconditions.checkNotNull(uri);
    }
    
    public URI getUri() {
        return mUri;
    }

    /**
     * Convert {@link android.nfc.NdefRecord} into a {@link android.net.Uri}.
     * This will handle both TNF_WELL_KNOWN / RTD_URI and TNF_ABSOLUTE_URI.
     *
     * @throws IllegalArgumentException if the NdefRecord is not a record
     *         containing a URI.
     */
    public static UriRecord parse(Record record) {

    	if(URI_PREFIX_MAP.size() <= 0){
    		setPrefixMap();
    	}
        short tnf = record.getTNF();
        if (tnf == NDEF.TNF_WELL_KNOWN) {
            return parseWellKnown(record);
        } else if (tnf == NDEF.TNF_ABSOLUTE_URI) {
            return parseAbsolute(record);
        }
        throw new IllegalArgumentException("Unknown TNF " + tnf);
    }

    /** Parse and absolute URI record */
    private static UriRecord parseAbsolute(Record record) {

        byte[] payload = record.getPayload();
        URI uri = URI.create(new String(payload, Charset.forName("UTF-8")));
        return new UriRecord(uri);
    }

    /** Parse an well known URI record */
    private static UriRecord parseWellKnown(Record record) {

        Preconditions.checkArgument(Arrays.equals(record.getType(), NDEF.RTD_URI));
        byte[] payload = record.getPayload();
        /*
         * payload[0] contains the URI Identifier Code, per the
         * NFC Forum "URI Record Type Definition" section 3.2.2.
         *
         * payload[1]...payload[payload.length - 1] contains the rest of
         * the URI.
         */
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        byte[] fullUri = new byte[prefix.length() + (payload.length -1)];
        System.arraycopy(prefix.getBytes(), 0, fullUri, 0, prefix.getBytes().length);	// copy prefix to fullUri(1)
        System.arraycopy(payload, 1, fullUri, prefix.getBytes().length, payload.length - 1);	// copy payload(length - 1) to fullUri(2)
        //(1)+(2) instead ---- Bytes.concat(prefix.getBytes(Charset.forName("UTF-8")), Arrays.copyOfRange(payload, 1, payload.length));
        URI uri = URI.create(new String(fullUri, Charset.forName("UTF-8")));
        return new UriRecord(uri);
    }

    public static boolean isUri(Record record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    //private static final byte[] EMPTY = new byte[0];
    private static void setPrefixMap(){
    	
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
}
