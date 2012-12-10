package com.android.nfc.extension.ndef.wkt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.IBuilder;
import com.android.nfc.extension.ndef.NDEF.Record;
import com.android.nfc.extension.ndef.wkt.Signature.ISignatureBuilder;
import com.android.nfc.extension.ndef.wkt.Signature.CertificateFormat;
import com.android.nfc.extension.ndef.wkt.Signature.SignatureType;

/**
 * NDEF Signature
 * @author Ray for Builder
 * @author Eddie for Parser
 * @hide
 */
public class SignatureBuilder implements ISignatureBuilder
{
	/**
	 * Constructor
	 * @param builder
	 */
	SignatureBuilder(IBuilder builder)
	{
		mBuilder = builder;
	}
	
	/**
	 * NDEF Builder
	 */
	private IBuilder mBuilder;
	
	/**
	 * NDEF Signature RTD Version
	 * Default is equal to 1
	 * Defined in Signature NDEF Structure Page 6, 
	 * 			  3.3 Records Mapping 
	 *            3.3.1 Version Field Line 4
	 *            
	 *            "the only valid version number is 1"
	 */
	private static final int SIGNATURE_DEFAULT_VERSION	= 1;
	
	/**
	 * NDEF Signature RTD
	 * Defined in Signature NDEF Structure Page 6, 
	 * 			  3.3 Records Mapping 
	 *            3.3.1 Syntax Line 1
	 *            
	 *            "The NFC Forum Well Known Type[NDEF],[RTD] for Signature is "Sig"
	 *            (0x53,0x69,0x67)"
	 */
	private static final byte [] SIGNATURE_RTD_NAME = {0x53,0x69,0x67};
	
	/**
	 * Marked Signature Payload
	 */
	private static final byte [] MARKED_SIGNATURE_PAYLOAD = {0x01, 	// Version
		                                                     0x00}; // URI_Present and SignatureType
	
	/**
	 * NDEF Message (NDEF Records)
	 */
	//private ArrayList<Record> mNdefMessage = new ArrayList<Record>();
	
	/**
	 * If the last record is a signature
	 */
	private boolean mIsSignedDataBefore = false;
	
	/**
	 * has any records
	 */
	private boolean mIsEmpty = true;
	
	/**
	 * Create a mock signature RTD
	 * @return
	 */
	private Record createMockSignature()
	{
		byte [] type = SIGNATURE_RTD_NAME;
		byte [] id = null;
		byte [] payload = MARKED_SIGNATURE_PAYLOAD;
		
		return new Record(NDEF.TNF_WELL_KNOWN, type, id, payload);
	}
	
	/**
	 * Check if Top Most Certificate is on the Chain
	 * If only one certificate, it means the self signed certificate
	 * @param certChains
	 * @return
	 */
	private boolean hasTopMostCertificate(X509Certificate [] certChains)
	{
		if(certChains.length == 1)
		{
			return false;
		}
		
		// Search the Top-Most Certificate on this chain
		for(int index = certChains.length - 1;index > 0;index --)
		{
			if(certChains[index].getIssuerDN().equals(certChains[index].getSubjectDN()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Create a solid signature
	 * @return
	 * @throws IOException 
	 * @throws CertificateEncodingException 
	 */
	private Record createSolidSignature(int signType,
										boolean isSolidSignature,
									    byte [] signature,									    
									    X509Certificate [] certchains,
									    byte [] certURL) throws IOException,
									    					   CertificateEncodingException
	{
		int length = 0;		
		
		/**
		 * Signature Payload
		 * +-----------------------------------+
		 * |           Signature Record        |
		 * +-------+---------+-----------------+
		 * |Version|Signature|Certificate Chain|
		 * +-------+---------+-----------------+
		 */		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{		
			/**
			 * Version Field
			 */
			baos.write(SIGNATURE_DEFAULT_VERSION);
		
			/**
			 * Signature Field
			 */
			baos.write((isSolidSignature ? 0x00:0x80) | (signType & 0x7F));		// URI_Present and Signature Type
			
			length = signature.length;
			baos.write((length >> 8) & 0xFF);									// Signature Length First byte
			baos.write(length & 0xFF);											// Signature Length second byte
			baos.write(signature);												// Signature body

			/**
			 * Certificate Chain Field
			 */
			int certiticateChainLength = 0;
			if(certchains == null)
			{
				
			}
			else 
			{
				certiticateChainLength = certchains.length;
				if(hasTopMostCertificate(certchains))
				{
					certiticateChainLength --;
				}
			}
				
			// URI_Present = 0, Cert_Format and Nbr_of_Certs
			baos.write((certURL == null ? 0x00:0x80) | 
					   ((CertificateFormat.X_509 & 0x07) << 4) | 
					   (certiticateChainLength & 0x0F));
			
			int certificateDataLength = 0;
			byte [] certificateRaw = null;
			for(int index = 0;index < certiticateChainLength;index ++)
			{
				certificateRaw = certchains[index].getEncoded();
				certificateDataLength = certificateRaw.length;
				
				// Write certificate chain length
				baos.write((certificateDataLength >> 8) & 0xFF);
				baos.write(certificateDataLength & 0xFF);
				
				// write certificate
				baos.write(certificateRaw);
			}
			
			if(certURL == null)
			{
				// do nothing
			}
			else
			{
				// Write certificate chain length
				certificateDataLength = certURL.length;
				baos.write((certificateDataLength >> 8) & 0xFF);
				baos.write(certificateDataLength & 0xFF);
				
				baos.write(certURL);
			}
			
			// write certificate URI
			byte [] payload = baos.toByteArray();			
			return new Record(NDEF.TNF_WELL_KNOWN, SIGNATURE_RTD_NAME, null, payload);
		}
		finally
		{
			baos.close();
		}
	}
	
	
	/**
	 * Append Unsigned Records
	 * @param record
	 */
	public void appendUnsignedRecords(Record [] records)
	{
		if(records == null || records.length == 0)
		{
			throw new IllegalArgumentException("Not allow no records");
		}
		
		mIsSignedDataBefore = false;
		for(int index = 0; index < records.length; index++)
		{
			mBuilder.appendRecord(records[index]);
		}
		
		mIsEmpty = false;
	}
	
	/**
	 * Verify Arguments
	 * @param record
	 */	
	private void verifyArguments(Record [] records)
	{
		if(records == null || records.length == 0)
		{
			throw new IllegalArgumentException("Not allow no records");
		}
	}
	
	/**
	 * Verify Arguments
	 * @param record
	 * @param signType
	 * @param aliasName
	 */
	private void verifyArguments(Record [] records, 
								 int signType, 
								 PrivateKey privateKey)
	{
		verifyArguments(records);		
		if(privateKey == null)
		{
			throw new IllegalArgumentException("Private Key MUST not be null");
		}
		
		switch(signType)
		{
		case SignatureType.RSASSA_PKCS1_V1_5:
		case SignatureType.DSA:			
			break;
		case SignatureType.RSASSA_PSS_SHA_1:			
		case SignatureType.ECDSA_P_192_SHA_1:
			throw new IllegalArgumentException("Not supported Algorithm");
		default:
			throw new IllegalArgumentException("Undefined Algorithm");
		}
		
		if(signType == SignatureType.RSASSA_PKCS1_V1_5 && 
		   SignatureType.SIGNATURE_ALGORITHM_RSA.equalsIgnoreCase(privateKey.getAlgorithm()))
		{
			// do nothing
		}
		else if(SignatureType.SIGNATURE_ALGORITHM_DSA.equalsIgnoreCase(privateKey.getAlgorithm()))
		{
			// do nothing
		}
		else
		{
			throw new IllegalArgumentException("Invalid PrivateKey");
		}
	}
	
	/**
	 * Only create a Signature Record with Signing
	 * @param records
	 * @param signType
	 * @param signatureURL
	 * @param certificateChains
	 * @param certificateURL
	 * @throws Exception
	 */
	public void appendSignedRecords(Record [] records, 
									int signType,
									String signatureURL,
									X509Certificate [] certificateChains,
									String certificateURL) throws Exception
	{
		verifyArguments(records);		
		if(signatureURL == null || signatureURL.length() == 0)
		{
			throw new IllegalArgumentException("signature URL MUST not be null");
		}
		
		// Sign Records
		byte [] signature = signatureURL.getBytes();	
		
		byte [] certURL = null;
		if(certificateURL == null || certificateURL.length() == 0)
		{
			// do nothing
		}
		else
		{
			certURL = certificateURL.getBytes();
		}
		
		Record sigRTD = createSolidSignature(signType, false, signature, certificateChains, certURL);
		if(sigRTD == null)
		{
			throw new SignatureException("Cannot create NDEF Signature RTD");
		}
		
		if(!mIsEmpty && !mIsSignedDataBefore)
		{
			mBuilder.appendRecord(createMockSignature());
		}
		
		// Append Raw RTD
		for(int index = 0;index < records.length;index ++)
		{
			mBuilder.appendRecord(records[index]);
		}
		
		// Append Signature RTD
		mBuilder.appendRecord(sigRTD);	
		mIsEmpty = false;	
		mIsSignedDataBefore = true;
		
	}		
	
	/**
	 * Append Signed Records
	 * @param record
	 */
	public void appendSignedRecords(Record [] records, 
									int signType, 
									PrivateKey privateKey, 
									X509Certificate [] certificateChains,
									String certificateURL) throws Exception
	{
		verifyArguments(records, signType, privateKey);
		
		// Sign Records
		byte [] signature = sign(records, signType, privateKey);
		if(signature == null)
		{
			throw new SignatureException("Signature is null.");
		}
		
		byte [] certURL = null;
		if(certificateURL == null || certificateURL.length() == 0)
		{
			// do nothing
		}
		else
		{
			certURL = certificateURL.getBytes();
		}
		
		Record sigRTD = createSolidSignature(signType, true, signature, certificateChains, certURL);
		if(sigRTD == null)
		{
			throw new SignatureException("Cannot create NDEF Signature RTD");
		}
		
		if(!mIsEmpty && !mIsSignedDataBefore)
		{
			mBuilder.appendRecord(createMockSignature());
		}
		
		// Append Raw RTD
		for(int index = 0;index < records.length;index ++)
		{
			mBuilder.appendRecord(records[index]);
		}
		
		// Append Signature RTD
		mBuilder.appendRecord(sigRTD);	
		mIsEmpty = false;
		mIsSignedDataBefore = true;
	}
	
	/**
	 * Sign NDEF Records (NDEF Signature is only cover Type/ID/Payload, 
	 * The header (MB,ME,SR,IR and Length) is ignore
	 * @param records
	 * @param signType
	 * @param privateKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	private byte [] sign(Record [] records, 
						 int signType, 
						 PrivateKey privateKey) throws NoSuchAlgorithmException, 
						 							   InvalidKeyException, 
						 							   SignatureException
	{
		java.security.Signature signer = null;
		switch(signType)
		{
		case SignatureType.RSASSA_PKCS1_V1_5:
			signer = java.security.Signature.getInstance("Sha1withRSA");
			break;
		case SignatureType.DSA:	
			signer = java.security.Signature.getInstance("DSA");
			break;
		}		

		signer.initSign(privateKey);		
		for(int index = 0;index < records.length;index ++)
		{	
			// Sign Type
			signer.update(records[index].getType());
			
			// Sign ID
			if(records[index].getID() == null ||
			   records[index].getID().length == 0)
			{
				// do nothing
			}
			else
			{
				signer.update(records[index].getID());
			}
			
			// Sign Payload
			signer.update(records[index].getPayload());
		}	
		
		return signer.sign();
	}

	/**
	 * To bytes array
	 * @return
	 */
	public byte [] toBytesArray()
	{
		return mBuilder.toBytesArray();
	}
}
