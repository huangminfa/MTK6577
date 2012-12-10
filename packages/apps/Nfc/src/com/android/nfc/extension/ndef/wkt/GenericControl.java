package com.android.nfc.extension.ndef.wkt;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.IBuilder;
import com.android.nfc.extension.ndef.NDEF.IParser;
import com.android.nfc.extension.ndef.NDEF.Record;
import com.android.nfc.extension.nfc.log.Logger;
import com.android.nfc.extension.nfc.log.Logger.ILog;
import com.android.nfc.extension.nfc.utility.Bit;
import com.android.nfc.extension.nfc.utility.Convert;
import com.android.nfc.extension.nfc.utility.Operator;

/**
 * @hide
 */
public class GenericControl 
{		
	private static String TAG = "GenericControl";
	
	private static boolean USE_RECOMMENDED_ACTION = false;
	private static boolean SET_CONFIGURATION_SC = false;
	private static boolean SET_CONFIGURATION_EC = false;
		
	private static final byte GENERIC_CONTROL_TARGET_RECORD = (byte)0x74;
	private static final byte GENERIC_CONTROL_ACTION_RECORD = (byte)0x61;
	private static final byte GENERIC_CONTROL_DATA_RECORD = (byte)0x64;
	
	private static String  gcTarget = null;
	private static String gcCustomAction = null;
	private static RecommendedAction gcRecommendedAction = null;
	private static String[] gcData = null;
	private static ILog ProjectLog = Logger.getInstance();
		
	private GenericControl(String target, String customAction, RecommendedAction recommendedAction, String[] data){
		gcTarget = target;
		gcCustomAction = customAction;
		gcRecommendedAction = recommendedAction;
		gcData = data;
	}
	
	/**
	 * Parse and create GenericControl object

	 * @param payload
	 * @param parseName
	 * @return GenericControl
	 * @throws Exception
	 */
	public static GenericControl parse(byte [] payload) throws Exception
	{		
		return ndefForumReferenceParse(payload);
	}
	
	private static GenericControl ndefForumReferenceParse(byte[] payload) throws Exception{
						
		ProjectLog.i(TAG, "parser configuration of GC");
		byte config = payload[0];		
		if(Bit.checkBitStatus(config, 1))
			SET_CONFIGURATION_SC = true;
		if(Bit.checkBitStatus(config, 2))
			SET_CONFIGURATION_EC = true;
		
		
		ProjectLog.i(TAG, "parser generic control record");
		byte[] gcPayload = new byte[payload.length - 1];
		GCParser parser = new GCParser();
		System.arraycopy(payload, 1, gcPayload, 0, gcPayload.length);
		Record[]gcRecords = parser.getRecords(gcPayload);
		
		if(gcRecords.length != 3)
			throw new IllegalArgumentException("[FORMAT ERROR]:payload length is not equal to 3");
		
		for(int i = 0; i < 3; i++){
			dispatchRecordParser(gcRecords[i], parser);
		}
		return new GenericControl(gcTarget, gcCustomAction, gcRecommendedAction, gcData);
	}
	
	private static void dispatchRecordParser(Record record, GCParser parser) throws Exception{ 
		
		switch(record.getType()[0]){
		
			case GENERIC_CONTROL_TARGET_RECORD:
				parseTargetRecord(parser.getRecords(record.getPayload())[0]);
			break;
			
			case GENERIC_CONTROL_ACTION_RECORD:
				byte cfgAction = record.getPayload()[0];								
				parseActionRecord(cfgAction, record, parser);
			break;
			
			case GENERIC_CONTROL_DATA_RECORD:
				parseDataRecord(parser.getRecords(record.getPayload()));
			break;
		}
	}
	
	private static void parseTargetRecord(Record record) throws UnsupportedEncodingException{
				
		if(Convert.toHex(record.getType()).trim().equalsIgnoreCase(Convert.toHex(NDEF.RTD_TEXT).trim())){
			ProjectLog.d(TAG, "target record's type is TEXT!");
			TextRecord textRecord = TextRecord.parse(record);
			gcTarget = textRecord.getText();
		}else if(Convert.toHex(record.getType()).trim().equalsIgnoreCase(Convert.toHex(NDEF.RTD_URI).trim())){
			ProjectLog.d(TAG, "target record's type is URI!");
			UriRecord uriRecord = UriRecord.parse(record);
			gcTarget = uriRecord.getUri().toString();
		}
		
		ProjectLog.d(TAG, "target record's content is: " + gcTarget);
	}
	
	private static void parseActionRecord(byte cfg, Record record, GCParser parser) throws Exception{
				
		if(Bit.checkBitStatus(cfg, 0)){
			USE_RECOMMENDED_ACTION = true;
			byte action = record.getPayload()[1];
			 if (RecommendedAction.LOOKUP.containsKey(action)) {
		            gcRecommendedAction = RecommendedAction.LOOKUP.get(action);
		            return;
		        }
			 gcRecommendedAction = RecommendedAction.Unknown;
		}else{
			byte[] customAction = new byte[record.getPayload().length - 1];
			System.arraycopy(record.getPayload(), 1, customAction, 0, customAction.length);
			TextRecord textRecord = TextRecord.parse(parser.getRecords(customAction)[0]); 
			gcCustomAction = textRecord.getText();
		}
		
		ProjectLog.d(TAG, "action record's content is: " + gcCustomAction);
		ProjectLog.d(TAG, "action record's content is: " + gcRecommendedAction);
	}
	
	private static void parseDataRecord(Record[] records) throws UnsupportedEncodingException{
		
		ProjectLog.d(TAG, "data record's length is: " + records.length);
		if(records.length == 0)
			throw new IllegalArgumentException("data record is null ");
		
		List<String> infos = new ArrayList<String>();
		for(int i = 0; i < records.length; i++){
			TextRecord textRecord = TextRecord.parse(records[i]);
			infos.add(textRecord.getText());
		}
		gcData = new String[infos.size()];
		infos.toArray(gcData);

	}
	
	public enum RecommendedAction {
        Unknown((byte) -1), DO_ACTION((byte) 0), SAVE_FOR_LATER((byte) 1), OPEN_FOR_EDITING(
            (byte) 2);

        private static final Map<Byte, RecommendedAction> LOOKUP;
        static {
            Map<Byte, RecommendedAction> builder = new HashMap<Byte, GenericControl.RecommendedAction>();
            for (RecommendedAction action : RecommendedAction.values()) {
                builder.put(action.getByte(), action);
            }
            LOOKUP = builder;
        }

        private final byte mAction;

        private RecommendedAction(byte val) {
            this.mAction = val;
        }

        private byte getByte() {
            return mAction;
        }
    }
	
	/**
	 * build payload of format base on Generic Control RTD
	 * @param config
	 * @param target
	 * @param customAction
	 * @param recommendedAction
	 * @param data
	 * @return
	 */
	public static byte[] build(byte config, String target, RecommendedAction recommendedAction, String[] data){
				
		if(target == null || target.length() == 0){
			throw new IllegalArgumentException("target is null ");
		}
		
		if(recommendedAction == RecommendedAction.Unknown){
			throw new IllegalArgumentException("Unknown action ");
		}
		
		if(data == null || data.length == 0){
			throw new IllegalArgumentException("data is null ");
		}
		return build(config, target, null, recommendedAction, null, data);				
	}

	/**
	 * build payload of format base on Generic Control RTD
	 * @param config
	 * @param target
	 * @param recommendedAction
	 * @param data
	 * @return
	 */
	public static byte[] build(byte config, URI target, RecommendedAction recommendedAction, String[] data){
		
		if(target == null){
			throw new IllegalArgumentException("target is null ");
		}
		
		if(recommendedAction == RecommendedAction.Unknown){
			throw new IllegalArgumentException("Unknown action ");
		}
		
		if(data == null || data.length == 0){
			throw new IllegalArgumentException("data is null ");
		}
		return build(config, null, target, recommendedAction, null, data);				
	}
	/**
	 * build payload of format base on Generic Control RTD
	 * @param config
	 * @param target
	 * @param customAction
	 * @param data
	 * @return
	 */
	public static byte[] build(byte config, URI target, String customAction,  String[] data){
		
		if(target == null){
			throw new IllegalArgumentException("target is null ");
		}
		
		if(customAction == null || customAction.length() == 0){
			throw new IllegalArgumentException("action is null ");
		}
		
		if(data == null || data.length == 0){
			throw new IllegalArgumentException("data is null ");
		}
		return build(config, null, target, null, customAction, data);
	}
	
	/**
	 * build payload of format base on Generic Control RTD
	 * @param config
	 * @param target
	 * @param customAction
	 * @param data
	 * @return
	 */
	public static byte[] build(byte config, String target, String customAction,  String[] data){
		
		if(target == null){
			throw new IllegalArgumentException("target is null ");
		}
		
		if(customAction == null || customAction.length() == 0){
			throw new IllegalArgumentException("action is null ");
		}
		
		if(data == null || data.length == 0){
			throw new IllegalArgumentException("data is null ");
		}
		return build(config, target, null, null, customAction, data);
	}

	private static byte[] build(byte config, String sTarget, URI uTarget, RecommendedAction recommendedAction, String customAction,  String[] data){
		
		//IBuilder builder = NDEF.createBuilder();
		GCBuilder builder = new GCBuilder();
		
		// build payload of target
		Record tPayload = new Record(new byte[]{0});
		tPayload.setID(new byte[]{});
		tPayload.setMB(true);
		tPayload.setME(true);
		tPayload.setTNF((byte)NDEF.TNF_WELL_KNOWN);
		if(sTarget != null){
			tPayload.setPayload(sTarget.getBytes());		
			tPayload.setType(NDEF.RTD_TEXT);
		}else{
			tPayload.setPayload(uTarget.toString().getBytes());		
			tPayload.setType(NDEF.RTD_URI);
		}
		builder.appendRecord(tPayload);
		byte[] tbPayload = builder.toBytesArray();
		ProjectLog.d(TAG, "target's payload is "+ Convert.toHex(tbPayload));
		
		// build payload of action
		byte[] abPayload;
		if(recommendedAction != null){
			abPayload = new byte[2];
			abPayload[0] = (byte)0x01;
			abPayload[1] = recommendedAction.getByte();
		}else{						
			Record aPayload = new Record(new byte[]{0});
			aPayload.setID(new byte[]{});
			aPayload.setMB(true);
			aPayload.setME(true);
			aPayload.setTNF((byte)NDEF.TNF_WELL_KNOWN);
			aPayload.setPayload(customAction.getBytes());		
			aPayload.setType(NDEF.RTD_TEXT);
			builder.appendRecord(aPayload);
			byte[] tmp = builder.toBytesArray();
			abPayload = new byte[tmp.length + 1];
			abPayload[0] = (byte)0x00;
			System.arraycopy(tmp, 0, abPayload, 1, tmp.length);
		}
		
		ProjectLog.d(TAG, "action's payload is "+ Convert.toHex(abPayload));
		// build payload of data
		Record[] dPayload = new Record[data.length];
		for(int i = 0; i < data.length; i++){
			dPayload[i] = new Record(new byte[]{0});
			dPayload[i].setID(new byte[]{});
			// is first record
			if(i == 0)
				dPayload[i].setMB(true);
			else
				dPayload[i].setMB(false);
			
			// is end record
			if((i+1) == data.length)
				dPayload[i].setME(true);
			else
				dPayload[i].setME(false);
			
			dPayload[i].setTNF((byte)NDEF.TNF_WELL_KNOWN);
			dPayload[i].setPayload(data[i].getBytes());		
			dPayload[i].setType(NDEF.RTD_TEXT);
			builder.appendRecord(dPayload[i]);
		}
		byte[] dbPayload = builder.toBytesArray();
		ProjectLog.d(TAG, "data's payload is "+ Convert.toHex(dbPayload));
		
		// build target record
		Record tRecord = new Record(new byte[]{0});
		tRecord.setID(new byte[]{});
		tRecord.setMB(true);
		tRecord.setME(true);
		tRecord.setTNF((byte)NDEF.TNF_WELL_KNOWN);
		tRecord.setPayload(tbPayload);
		tRecord.setType(new byte[]{(byte)0x74});
		builder.appendRecord(tRecord);
		
		// build action record
		Record aRecord = new Record(new byte[]{0});
		aRecord.setID(new byte[]{});
		aRecord.setMB(true);
		aRecord.setME(true);
		aRecord.setTNF((byte)NDEF.TNF_WELL_KNOWN);
		aRecord.setPayload(abPayload);
		aRecord.setType(new byte[]{(byte)0x61});
		builder.appendRecord(aRecord);
		
		// build data record
		Record dRecord = new Record(new byte[]{0});
		dRecord.setID(new byte[]{});
		dRecord.setMB(true);
		dRecord.setME(true);
		dRecord.setTNF((byte)NDEF.TNF_WELL_KNOWN);
		dRecord.setPayload(dbPayload);
		dRecord.setType(new byte[]{(byte)0x64});
		builder.appendRecord(dRecord);
		
		byte[] gcPayload =  builder.toBytesArray();
		byte[] result = new byte[gcPayload.length + 1];
		byte[] configs = new byte[1];
		configs[0] = config;		
		System.arraycopy(configs, 0, result, 0, 1);
		System.arraycopy(gcPayload, 0, result, 1, gcPayload.length);
		ProjectLog.d(TAG, "result is "+ Convert.toHex(result));
		return result;
	}

	public static boolean isCONFIGURATION_EC() {
		return SET_CONFIGURATION_EC;
	}
	
	public static boolean isCONFIGURATION_SC() {
		return SET_CONFIGURATION_SC;
	}

	public static boolean isUSE_RECOMMENDED_ACTION() {
		return USE_RECOMMENDED_ACTION;
	}
	
	public String getTarget(){
		return gcTarget;
	}
	
	public String getCustomAction(){
		return gcCustomAction;
	}
	
	public RecommendedAction getRecommendedAction(){
		return gcRecommendedAction;
	}
	
	public String[] getData(){
		return gcData;
	}
	
	private static class GCParser{
		private String TAG = "GCParser";
		private final int NDEF_HEADER_LENGTH = 4;
		private final int NDEF_HEADER_LENGTH_NO_ID = 3;
				
		public Record[] getRecords(byte[] raw) throws Exception {
			
			if(raw == null || raw.length == 0)
			{
				return null;
			}
			IParser parser = NDEF.createParser();
			int offset = 0;
			List<Record> records = new ArrayList<Record>();
			ProjectLog.d(TAG, "raw.length: "+ raw.length);
			
			while(offset < raw.length){
				byte[] ndefRaw = new byte[raw.length - offset];
				System.arraycopy(raw, offset, ndefRaw, 0, ndefRaw.length);
				Record tmp = parseNdefRecord(parser, ndefRaw);
				records.add(tmp);
				if(tmp.getID().length > 0){
					offset = offset + tmp.getPayload().length + NDEF_HEADER_LENGTH + tmp.getType().length;
				}
				else{
					offset = offset + tmp.getPayload().length + NDEF_HEADER_LENGTH_NO_ID + tmp.getType().length + tmp.getID().length;
				}
					
				ProjectLog.d(TAG, "offset: "+offset);				
			}
			Record[] result = new Record[records.size()];
			records.toArray(result);
			return result;
		}

		public Record parseNdefRecord(IParser parser, byte[] raw) throws Exception{

			ProjectLog.d(TAG, "raw is: "+Convert.toHex(raw));
			Record[] rs = parser.getRecords(raw);			
			return rs[0];
		}
	}
	
	private static class GCBuilder{
		private List<Record> records = new ArrayList<Record>();
		private String TAG = "GCBuilder";
		
		public void appendRecord(Record record) {		
			records.add(record);
		}

		public byte[] toBytesArray() {
			
			if(records.size() <= 0)
				return null;
			
			Record[] ndefRecords = new Record[records.size()];
			records.toArray(ndefRecords);
			
			records.clear();
			if ((ndefRecords == null) || (ndefRecords.length == 0))
	            return null;

	        byte[] msg = {};

	        try{
		        for (int i = 0; i < ndefRecords.length; i++) 
		        {
		        	if(Operator.equals(ndefRecords[i].getType(), NDEF.RTD_TEXT)){
		        		ProjectLog.d(TAG, "wrap Text Record");
		        		//ndefRecords[i] = new NdefRecord(ndefRecords[i].getTnf(), ndefRecords[i].getType(), ndefRecords[i].getId(), createText(ndefRecords[i].getPayload()));
		        		ndefRecords[i].setPayload(createText(ndefRecords[i].getPayload()));
		        	}else if(Operator.equals(ndefRecords[i].getType(), NDEF.RTD_URI)){
		        		ProjectLog.d(TAG, "wrap URI Record");
		        		//ndefRecords[i] = new NdefRecord(ndefRecords[i].getTnf(), ndefRecords[i].getType(), ndefRecords[i].getId(), createUri(ndefRecords[i].getPayload()));
		        		ndefRecords[i].setPayload(createUri(ndefRecords[i].getPayload()));
		        	}
		        	IBuilder builder = NDEF.createBuilder();	        	
		            byte[] record = builder.toBytesArray(ndefRecords[i]);
		            byte[] tmp = new byte[msg.length + record.length];          
		            System.arraycopy(msg, 0, tmp, 0, msg.length);
		            System.arraycopy(record, 0, tmp, msg.length, record.length);
		            msg = tmp;
		        }
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        return msg;		
		}	
		
		private byte[] createText(byte[] payload) throws UnsupportedEncodingException{
		
			Locale locale = Locale.US;
			final byte[] langBytes = locale.getLanguage().getBytes("US_ASCII"); 
			final byte[] textBytes = payload;
			final int utfBit = 0;
			final char status = (char) (utfBit + langBytes.length);
			byte[] data = new byte[1 + langBytes.length + textBytes.length];
			data[0] = (byte) status;		
			System.arraycopy(langBytes, 0, data, 1, langBytes.length);
			System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
			return data;
		}
		
		private byte[] createUri(byte[] payload) throws UnsupportedEncodingException{
			String uriString = new String(payload);
			byte prefix = 0x0;
	        for (int i = 1; i < URI_PREFIX_MAP.length; i++) {
	            if (uriString.startsWith(URI_PREFIX_MAP[i])) {
	                prefix = (byte) i;
	                uriString = uriString.substring(URI_PREFIX_MAP[i].length());
	                break;
	            }
	        }
	        byte[] uriBytes = uriString.getBytes("UTF-8");
	        byte[] recordBytes = new byte[uriBytes.length + 1];
	        recordBytes[0] = prefix;
	        System.arraycopy(uriBytes, 0, recordBytes, 1, uriBytes.length);			
			return recordBytes;
		}
		
				
		private static final String[] URI_PREFIX_MAP = new String[] {
            "", // 0x00
            "http://www.", // 0x01
            "https://www.", // 0x02
            "http://", // 0x03
            "https://", // 0x04
            "tel:", // 0x05
            "mailto:", // 0x06
            "ftp://anonymous:anonymous@", // 0x07
            "ftp://ftp.", // 0x08
            "ftps://", // 0x09
            "sftp://", // 0x0A
            "smb://", // 0x0B
            "nfs://", // 0x0C
            "ftp://", // 0x0D
            "dav://", // 0x0E
            "news:", // 0x0F
            "telnet://", // 0x10
            "imap:", // 0x11
            "rtsp://", // 0x12
            "urn:", // 0x13
            "pop:", // 0x14
            "sip:", // 0x15
            "sips:", // 0x16
            "tftp:", // 0x17
            "btspp://", // 0x18
            "btl2cap://", // 0x19
            "btgoep://", // 0x1A
            "tcpobex://", // 0x1B
            "irdaobex://", // 0x1C
            "file://", // 0x1D
            "urn:epc:id:", // 0x1E
            "urn:epc:tag:", // 0x1F
            "urn:epc:pat:", // 0x20
            "urn:epc:raw:", // 0x21
            "urn:epc:", // 0x22
    };
	}
}
