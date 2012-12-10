package com.android.nfc.extension.ndef;

/**
 * @hide
 */
public class NDEF 
{
	/**
     * Indicates no type, id, or payload is associated with this NDEF Record.
     * <p>
     * Type, id and payload fields must all be empty to be a valid TNF_EMPTY
     * record.
     */
    public static final short TNF_EMPTY = 0x00;

    /**
     * Indicates the type field uses the RTD type name format.
     * <p>
     * Use this TNF with RTD types such as RTD_TEXT, RTD_URI.
     */
    public static final short TNF_WELL_KNOWN = 0x01;

    /**
     * Indicates the type field contains a value that follows the media-type BNF
     * construct defined by RFC 2046.
     */
    public static final short TNF_MIME_MEDIA = 0x02;

    /**
     * Indicates the type field contains a value that follows the absolute-URI
     * BNF construct defined by RFC 3986.
     */
    public static final short TNF_ABSOLUTE_URI = 0x03;

    /**
     * Indicates the type field contains a value that follows the RTD external
     * name specification.
     * <p>
     * Note this TNF should not be used with RTD_TEXT or RTD_URI constants.
     * Those are well known RTD constants, not external RTD constants.
     */
    public static final short TNF_EXTERNAL_TYPE = 0x04;

    /**
     * Indicates the payload type is unknown.
     * <p>
     * This is similar to the "application/octet-stream" MIME type. The payload
     * type is not explicitly encoded within the NDEF Message.
     * <p>
     * The type field must be empty to be a valid TNF_UNKNOWN record.
     */
    public static final short TNF_UNKNOWN = 0x05;

    /**
     * Indicates the payload is an intermediate or final chunk of a chunked
     * NDEF Record.
     * <p>
     * The payload type is specified in the first chunk, and subsequent chunks
     * must use TNF_UNCHANGED with an empty type field. TNF_UNCHANGED must not
     * be used in any other situation.
     */
    public static final short TNF_UNCHANGED = 0x06;

    /**
     * Reserved TNF type.
     * <p>
     * The NFC Forum NDEF Specification v1.0 suggests for NDEF parsers to treat this
     * value like TNF_UNKNOWN.
     * @hide
     */
    public static final short TNF_RESERVED = 0x07;

    /**
     * RTD Text type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_TEXT = {0x54};  // "T"

    /**
     * RTD URI type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_URI = {0x55};   // "U"
    
    public static final byte[] RTD_GENERIC_CONTROL = {(byte)0x47,(byte)0x63};

    /**
     * RTD Smart Poster type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_SMART_POSTER = {0x53, 0x70};  // "Sp"

    /**
     * RTD Alternative Carrier type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_ALTERNATIVE_CARRIER = {0x61, 0x63};  // "ac"

    /**
     * RTD Handover Carrier type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_CARRIER = {0x48, 0x63};  // "Hc"

    /**
     * RTD Handover Request type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_REQUEST = {0x48, 0x72};  // "Hr"

    /**
     * RTD Handover Select type. For use with TNF_WELL_KNOWN.
     */
    public static final byte[] RTD_HANDOVER_SELECT = {0x48, 0x73}; // "Hs"

    /**
     * RTD Signature
     */
    public static final byte[] RTD_SIGNATURE = {0x53, 0x69, 0x67}; // "Sig"

    

	/**
	 * Parser interface
	 *
	 */
	public interface IParser
	{
		/**
		 * Get Next record
		 * @return
		 * @throws Exception 
		 */
		public Record [] getRecords(byte [] raw) throws Exception;
	}
	
	/**
	 * Builder interface
	 *
	 */
	public interface IBuilder
	{
		
		/**
		 * Append record
		 * @param record
		 */
		public void appendRecord(Record record);

		
		/**
		 * To bytes array
		 * @return
		 */
		public byte [] toBytesArray();		
		
		/**
		 * To bytes array
		 * @return
		 */
		public byte[] toBytesArray(Record record);
	}
	
	/**
	 * Record
	 *
	 */
	public static class Record
	{
		/**
		 * Constructor
		 */
		Record()
		{
			
		}
		
		/**
		 * Anonymous constructor
		 */
		public Record(short tnf, 
					  byte [] type, 
					  byte [] id, 
					  byte [] payload)
		{
			mMB = false;
			mME = false;
			mTNF = (byte)tnf;
			mType = type;
			mID = id;
			mPayload = payload;
		}
		
		/**
		 * Public constructor
		 * @param record
		 */
		public Record(byte [] record)
		{
			
		}
		
		/**
		 * TNF
		 */
		private byte mTNF;
		
		/**
		 * MB
		 */
		private boolean mMB;
		
		/**
		 * ME
		 */
		private boolean mME;
		
		/**
		 * Type
		 */
		private byte [] mType;
		
		/**
		 * ID
		 */
		private byte [] mID;
		
		/**
		 * Payload
		 */
		private byte [] mPayload;

		/**
		 * Getter of TNF
		 * @return
		 */
		public short getTNF() {
			return (short) (mTNF & 0xFF);
		}

		/**
		 * Setter of TNF
		 * @param tNF
		 */
		public void setTNF(short tNF) {
			mTNF = (byte)tNF;
		}

		/**
		 * Getter of MB
		 * @return
		 */
		public boolean isMB() {
			return mMB;
		}

		/**
		 * Setter of MB
		 * @param mB
		 */
		public void setMB(boolean mB) {
			mMB = mB;
		}

		/**
		 * Getter of ME
		 * @return
		 */
		public boolean isME() {
			return mME;
		}

		/**
		 * Setter of ME
		 * @param mE
		 */
		public void setME(boolean mE) {
			mME = mE;
		}

		/**
		 * Getter of type
		 * @return
		 */
		public byte [] getType() {
			return mType;
		}

		/**
		 * Setter of type
		 * @param type
		 */
		public void setType(byte [] type) {
			this.mType = type;
		}

		/**
		 * Getter of ID
		 * @return
		 */
		public byte[] getID() {
			
			if(mID == null)
			{
				return new byte[0];
			}
			
			return mID;
		}

		/**
		 * Setter of ID
		 * @param iD
		 */
		public void setID(byte[] iD) {
			mID = iD;
		}

		/**
		 * Getter of payload
		 * @return
		 */
		public byte[] getPayload() {
			return mPayload;
		}

		/**
		 * Setter of payload
		 * @param payload
		 */
		public void setPayload(byte[] payload) {
			this.mPayload = payload;
		}
	}
	
	/**
	 * create default parser
	 * @return
	 * @throws Exception 
	 */
	public static IParser createParser() 
	{
		return new DefaultParser();
	}
	
	/**
	 * create specific parser
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static IParser createParser(String parseName)
	{
		Class parserClazz = null;
		try 
		{
			parserClazz = Class.forName(parseName);
		} 
		catch (ClassNotFoundException e) 
		{
			return null;
		}
		
		Class [] interfaces = parserClazz.getInterfaces();
		if(interfaces == null || interfaces.length == 0)
		{
			return null;			
		}
		
		for(int i = 0; i < interfaces.length; i++)
		{
			if(interfaces[i].equals(NDEF.IParser.class))
			{
				try 
				{
					return (NDEF.IParser)parserClazz.newInstance();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					return null;
				}
			}
		}	
		
		return null;
	}
	
	/**
	 * create default builder
	 * @return
	 */
	public static IBuilder createBuilder()
	{
		return new DefaultBuilder();
	}
	
	/**
	 * create specific builder
	 * @param builderName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static IBuilder createBuilder(String builderName)
	{
		Class parserClazz = null;
		try 
		{
			parserClazz = Class.forName(builderName);
		} 
		catch (ClassNotFoundException e) 
		{
			return null;
		}
		
		Class [] interfaces = parserClazz.getInterfaces();
		if(interfaces == null || interfaces.length == 0)
		{
			return null;			
		}
		
		for(int i = 0; i < interfaces.length; i++)
		{
			if(interfaces[i].equals(NDEF.IBuilder.class))
			{
				try 
				{
					return (NDEF.IBuilder)parserClazz.newInstance();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					return null;
				}
			}
		}	
		
		return null;
	}
}
