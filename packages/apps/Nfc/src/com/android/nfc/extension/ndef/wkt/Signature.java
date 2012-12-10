package com.android.nfc.extension.ndef.wkt;


import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.IBuilder;
import com.android.nfc.extension.ndef.NDEF.Record;

/**
 * @hide
 */
public class Signature 
{
	/**
	 * Create a NDEF Signature Parser with specific NDEF Parser
	 * @return
	 */
	public static ISignatureParser createParser(String ndefParserName)
	{
		return new SignatureParser(ndefParserName);
	}
	
	/**
	 * Create a NDEF Signature Parser with default NDEF Parser
	 * @return
	 */
	public static ISignatureParser createParser()
	{
		return new SignatureParser();
	}
	
	/**
	 * Create a NDEF Signature Builder with specific NDEF Parser
	 * @return
	 */	
	public static ISignatureBuilder createBuilder()
	{
		IBuilder builder = NDEF.createBuilder();
		return new SignatureBuilder(builder);
	}
	
	/**
	 * Create a NDEF Signature Builder with specific NDEF Parser
	 * @return
	 */	
	public static ISignatureBuilder createBuilder(String ndefBuilderName)
	{
		IBuilder builder = NDEF.createBuilder(ndefBuilderName);
		return new SignatureBuilder(builder);
	}
	
	
	/**
	 * Parser interface
	 * @author Ray
	 *
	 */
	public interface ISignatureParser
	{
		/**
		 * Get Read Timeout
		 * @return
		 */
		int getReadTimeout();
		
		/**
		 * Get Connect Timeout
		 * @return
		 */
		int getConnectTimeout();
		
		
		/**
		 * Set Connect timeout
		 * @param timeout
		 */
		void setConnectTimeout(int timeout);
		
		/**
		 * Set Read Timeout
		 * @param timeout
		 */
		void setReadTimeout(int timeout);
		
		/**
		 * Set disable
		 * @param disable
		 */
		void setDisable(boolean disable);		
		
		/**
		 * CRL enable
		 * @param val
		 */
		void setRevocationEnable(boolean val);		
		
		/**
		 * Set continue when error occur
		 * @param val
		 */
		void setContinueWhenErrorOccur(boolean val);
		
		/**
		 * Parse
		 * @param ndefMessageRaw
		 * @return
		 * @throws Exception
		 */
		Segment [] parse(byte [] ndefMessageRaw) throws Exception;		
	}
	
	/**
	 * Builder interface
	 * @author Ray
	 *
	 */
	public interface ISignatureBuilder
	{
		void appendUnsignedRecords(Record [] records);
		
		/**
		 * Only create a Signature Record with Signing
		 * @param records
		 * @param signType
		 * @param signatureURL
		 * @param certificateChains
		 * @param certificateURL
		 * @throws Exception
		 */
		void appendSignedRecords(Record [] records, 
							     int signType,
								 String signatureURL,
								 X509Certificate [] certificateChains,
								 String certificateURL) throws Exception;
		/**
		 * Sign and create a Signature Record
		 * @param records
		 * @param signType
		 * @param privateKey
		 * @param certificateChains
		 * @param certificateURL
		 * @throws Exception
		 */
		void appendSignedRecords(Record [] records, 
								 int signType, 
								 PrivateKey privateKey, 
								 X509Certificate [] certificateChains,
								 String certificateURL) throws Exception;
		
		/**
		 * To bytes array, build a NDEF raw
		 * @return
		 */
		byte [] toBytesArray();
	}	
	
	/**
	 * Signed Status 
	 * @author vend_iii08
	 *
	 */
	public static class SignedStatus
	{
		SignedStatus(long resonCode, 
					 Throwable exception,
				     X509Certificate cert, 
					 boolean isTrust, 
					 boolean isValidate)
		{
			mReasonCode = resonCode;
			mLastException = exception;
			mSigner = cert;
			mTrust = isTrust;
			mValidate = isValidate;
		}		
		
		private Throwable mLastException;
		private long mReasonCode;
		private X509Certificate mSigner;
		private boolean mTrust;
		private boolean mValidate;
		
		/**
		 * @return the ReasonCode
		 */
		public long getReasonCode() {
			return mReasonCode;
		}
		/**
		 * @return the mSigner
		 */
		public X509Certificate getSigner() {
			return mSigner;
		}
		/**
		 * @return the mTrust
		 */
		public boolean isTrust() {
			return mTrust;
		}
		/**
		 * @return the mValidate
		 */
		public boolean isValidate() {
			return mValidate;
		}		
		
		/**
		 * Get Signer's DN
		 * @return
		 */
		public String getSignerDN() {
			return (mSigner == null) ? null : mSigner.getSubjectDN().getName();
		}
		
		/**
		 * Last exception
		 * @return
		 */
		public Throwable getException() {
			return mLastException; 
		}
		
	}
	
	/**
	 * Signature Segment
	 * @author Ray
	 *
	 */
	public static class Segment
	{
		/**
		 * Constructor for MockSignature
		 * @param records
		 */
		public Segment(Record [] records)
		{
			mRecords = records;
		}
		
		/**
		 * Constructor for SolidSignature
		 * @param records
		 * @param resonCode
		 * @param cert
		 * @param isTrust
		 * @param isValidate
		 */
		public Segment(Record [] records, 
					   long resonCode,
					   Throwable exception,
					   X509Certificate cert, 
					   boolean isTrust, 
					   boolean isValidate)
		{
			mRecords = records;
			mStatus = new SignedStatus(resonCode, exception, cert, isTrust, isValidate);			
		}
		
		/**
		 * Records
		 */
		private Record [] mRecords;
		
		/**
		 * Sign Status
		 */
		private SignedStatus mStatus;		
		
		/**
		 * Get Records of segment
		 * @return
		 */
		public Record [] getRecords() {
			return mRecords;
		}
		
		/**
		 * Get Signed status (if records are not signed, no SignedStatus will be found)
		 * @return
		 */
		public SignedStatus getSignedStatus() {
			return mStatus;
		}
	}
	
	
	/**
	 * Static class of Certificate type
	 * @author Ray
	 */

	public static class CertificateFormat {	
		
			/**
			 * Private constructor to avoid create instance
			 */
			private CertificateFormat()
			{
				// do nothing
			}
			
			/**
			 * Certificate Format
			 */
			public static final int X_509	= 0x00;
			public static final int X9_68	= 0x01;
		
	}
	
	/**
	 * Static class of Signature Type
	 * @author Ray
	 */

	public static class SignatureType {

		/**
		 * Private constructor to avoid create instance
		 */
		private SignatureType()
		{
			// do nothing 
		}
		
		/**
		 * Signature Type
		 */
		public static final int NO_SIGNATURE		= 0x00;
		public static final int RSASSA_PSS_SHA_1	= 0x01;
		public static final int RSASSA_PKCS1_V1_5	= 0x02;
		public static final int DSA					= 0x03;
		public static final int ECDSA_P_192_SHA_1	= 0x04;
		
		public static final String SIGNATURE_ALGORITHM_RSA = "RSA";
		public static final String SIGNATURE_ALGORITHM_DSA = "DSA";
	}
	
	/**
	 * Reason code
	 * @author Eddie
	 *
	 */
	public static class ReasonCode
	{
		/**
		 * Private constructor to avoid create instance
		 */
		private ReasonCode()
		{
			// do nothing 
		}
		
		public static final int SUCCESS = 0x000000;
		public static final int DOWNLOAD_SIGNATURE_FAIL = 0x990001;
		public static final int INVALIDATE_SIGNATURE = 0x990002;
		public static final int CERTIFICATE_FORMAT_ERROR = 0x880001;
		public static final int SIGNATURE_RTD_CERTIFICATE_DATA_DAMAGE = 0x880002;
		public static final int DOWNLOAD_CERTIFICATE_FAIL = 880003;
		public static final int DOWNLOAD_CERTIFICATE_FORMAT_ERROR = 880004;
		public static final int INVALIDATE_CERTIFICATE_PATH = 880005;
	}	
}
