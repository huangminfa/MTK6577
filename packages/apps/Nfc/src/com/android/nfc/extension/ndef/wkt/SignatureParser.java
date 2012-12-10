package com.android.nfc.extension.ndef.wkt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.nfc.extension.ndef.NDEF;
import com.android.nfc.extension.ndef.NDEF.IParser;
import com.android.nfc.extension.ndef.NDEF.Record;
import com.android.nfc.extension.ndef.wkt.Signature.CertificateFormat;
import com.android.nfc.extension.ndef.wkt.Signature.ISignatureParser;
import com.android.nfc.extension.ndef.wkt.Signature.ReasonCode;
import com.android.nfc.extension.ndef.wkt.Signature.Segment;
import com.android.nfc.extension.ndef.wkt.Signature.SignatureType;
import com.android.nfc.extension.nfc.log.Logger;
import com.android.nfc.extension.nfc.log.Logger.ILog;
import com.android.nfc.extension.nfc.utility.Bit;
import com.android.nfc.extension.nfc.utility.Convert;
import com.android.nfc.extension.security.cert.AndroidX509CertificateChainVerifer;
import com.android.nfc.extension.security.cert.X509CertificateChain;
import com.android.nfc.extension.security.cert.X509CertificateChain.IVerifier;
import com.android.nfc.extension.utility.net.UrlDownloader;

/**
 * Signature Parser 
 * @author Eddie
 * @hide
 */
public class SignatureParser implements ISignatureParser 
{
	/**
	 * Constructor
	 * @param builder
	 */
	SignatureParser(String parserName)
	{
		mParserName = parserName;
	}
	
	/**
	 * Constructor
	 * @param builder
	 */
	SignatureParser()
	{
		
	}
	
	/**
	 * NDEF default version
	 */
	private static final byte NDEF_DEFAULT_VERSION	= 0x01;
	
	/**
	 * Position of content on NDEF Signature RTD
	 */
	private static final int NDEF_SIG_PAYLOAD_POS_VERSION 			= 0;
	private static final int NDEF_SIG_PAYLOAD_POS_SIGNATURE_FLAG 	= 1;
	private static final int NDEF_SIG_PAYLOAD_POS_SIGNATURE_LENGTH 	= 2;
	private static final int NDEF_SIG_PAYLOAD_POS_SIGNATURE_DATA 	= 4;
	
	/**
	 * NDEF Parser
	 */
	private String mParserName;

	/**
	 * Logger
	 */
	private static ILog log = Logger.getInstance();
	
	/**
	 * Constant for JCE ALGORITHM String
	 */
	private static final String [] JCE_ALGORITHM_STRING = {"","","Sha1withRSA","DSA"};	
	
	/**
	 * TAG
	 */
	private static String TAG = "SignatureParser";

	/**
	 * Certificate Chain Validate module for Android 4.0 Platform
	 */
	private IVerifier mCertificateChainVerifier = new AndroidX509CertificateChainVerifer();
	
	/**
	 * Get instance of URL Downloader
	 */
	private UrlDownloader.IUrlDownloader mDownloader = UrlDownloader.createUrlDownloader();
	
	/**
	 * User Interrupt flag
	 */
	private boolean mDisable = false;
	
	/**
	 * Continue when error occur
	 */
	private boolean mContinueWhenErrorOccur;
	
	/**
	 * parse well-known signature structure
	 * @param records
	 * @return
	 * @throws CertificateException 
	 * @throws IOException 
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws CertPathValidatorException 
	 * @throws InvalidAlgorithmParameterException 
	 */
	private Segment verifyRecord(List<Record> records, 
								 Record signature) throws Exception
	{
		Segment rs = null;
		
		Record [] mtkRecords = new Record[records.size()];
		records.toArray(mtkRecords);
		
		// ************************************ verify signature RTD *******************************************
		byte [] sigPayload = signature.getPayload();
		
		// verify version
		byte version = sigPayload[NDEF_SIG_PAYLOAD_POS_VERSION];
		if(version != NDEF_DEFAULT_VERSION)
		{
			log.e(TAG, "invalid version code");
			throw new NdefFormatException("invalid version code");
		}
		
		// get configuration
		boolean isSigURIPresent = Bit.checkBitStatus(sigPayload[NDEF_SIG_PAYLOAD_POS_SIGNATURE_FLAG], 7);
		
		// verify signature's type
		byte mSignatureType = Bit.setMask(sigPayload[NDEF_SIG_PAYLOAD_POS_SIGNATURE_FLAG], 0x7F);		
		if(isSigURIPresent == false && mSignatureType == SignatureType.NO_SIGNATURE)
		{
			rs = new Segment(mtkRecords);
			return rs;
		}		
		
		if(mSignatureType != SignatureType.RSASSA_PKCS1_V1_5 && 
		   mSignatureType != SignatureType.DSA)
		{
			throw new NdefFormatException("Not supported Algorithm");
		}
		
		if(isSigURIPresent == true && 
		   mSignatureType == SignatureType.NO_SIGNATURE)
		{
			throw new NdefFormatException("Signatuare RTD Format Error");
		}
		
		// get length of (Signature/URI) and (Signature/URI) 
		short signatureLen = Convert.toShort(sigPayload, NDEF_SIG_PAYLOAD_POS_SIGNATURE_LENGTH, 2);		
		int signatureOffset = NDEF_SIG_PAYLOAD_POS_SIGNATURE_DATA;
		int signatureBufferLen = signatureLen & 0xFFFF;		
		byte [] mSignatureContent = sigPayload; // Signature or URI , by isURIPresent
		
		// download signature
		if(isSigURIPresent)
		{ 
			// mSignatureContent is real signature's uri
			try 
			{				
				mSignatureContent = downloadSignature(sigPayload,
													  signatureOffset,
													  signatureLen);
				signatureOffset = 0;
				signatureBufferLen = mSignatureContent.length;
			} 
			catch (Exception e) 
			{
				if(mContinueWhenErrorOccur)
				{
					return new Segment(mtkRecords, ReasonCode.DOWNLOAD_SIGNATURE_FAIL, e, null, false, false);
				}
				
				throw e;
			}
		}
		
		// get certificate from NDEF message
		int certFlagPosition = NDEF_SIG_PAYLOAD_POS_SIGNATURE_DATA + signatureLen;
		boolean isCertURIPresent = Bit.checkBitStatus(sigPayload[certFlagPosition], 7); // sigPayload[{version},{isSigURIPresent, mSignatureType},{SignatureLen value},{Signature contents}]
		byte mCertType = Bit.setMask(sigPayload[certFlagPosition], 0x70);
		byte mCertificateNum = Bit.setMask(sigPayload[certFlagPosition], 0x0F);
		log.d(TAG, "CertificateNum is: " ,Convert.toHex(new byte[]{mCertificateNum}));
		
		if(mCertType != CertificateFormat.X_509 || mCertificateNum < 0)
		{
			throw new NdefFormatException("Certificate Format Error");
		}
		
		if(isCertURIPresent != true && mCertificateNum == 0)
		{
			throw new NdefFormatException("Not Support Certificate is Null");
		}
		
		List<X509Certificate> certs = new ArrayList<X509Certificate>(mCertificateNum);
		int offset = certFlagPosition + 1;
		for(int i = 0; i < mCertificateNum; i++)
		{
			short certLen = Convert.toShort(sigPayload, offset, 2);
			offset += 2;	//shift cert length bytes(2bytes)
			try 
			{
				certs.add(getCertificate(sigPayload, offset, certLen));
			} 
			catch (CertificateException e) 
			{				
				if(mContinueWhenErrorOccur)
				{				
					return new Segment(mtkRecords, ReasonCode.CERTIFICATE_FORMAT_ERROR, e, null, false, false);
				}
				
				throw e;
				
			} 
			catch (IOException e) 
			{
				if(mContinueWhenErrorOccur)
				{	
					return new Segment(mtkRecords, ReasonCode.SIGNATURE_RTD_CERTIFICATE_DATA_DAMAGE, e, null, false, false);
				}
				
				throw e;
			}
			
			offset += certLen;  //shift cert size bytes(certLen bytes)
		}
		// download Certificate
		if(isCertURIPresent)
		{	
			short certLen = Convert.toShort(sigPayload, offset, 2);
			offset = offset + 2; //shift cert length bytes(2bytes)

			try 
			{
				certs.add(downloadCertificate(sigPayload, offset, certLen));
			} 
			catch (CertificateException e) 
			{
				if(mContinueWhenErrorOccur)
				{	
					return new Segment(mtkRecords, ReasonCode.DOWNLOAD_CERTIFICATE_FORMAT_ERROR, e, null, false, false);
				}
				
				throw e;
			} 
			catch (IOException e) 
			{
				if(mContinueWhenErrorOccur)
				{
					return new Segment(mtkRecords, ReasonCode.DOWNLOAD_CERTIFICATE_FAIL, e, null, false, false);
				}
				
				throw e;
			}
		}

		// verify the signature
		try 
		{
			verifySignature(mtkRecords,  
							mSignatureType, 
							certs.get(0), 
							mSignatureContent, 
							signatureOffset, 
							signatureBufferLen);			
		} 
		catch (Exception e) 
		{
			if(mContinueWhenErrorOccur)
			{
				return new Segment(mtkRecords, ReasonCode.INVALIDATE_SIGNATURE, e, certs.get(0), false, false);
			}
			
			throw e;
		}
		
		// verify the certificate chain and its trust certificate
		try 
		{
			X509Certificate target = certs.remove(0);			
			verifyCertificates(target, certs);
			return new Segment(mtkRecords, ReasonCode.SUCCESS, null, target, true, true);
		} 
		catch (Exception e) 
		{
			if(mContinueWhenErrorOccur)
			{	
				return new Segment(mtkRecords, ReasonCode.INVALIDATE_CERTIFICATE_PATH, e, certs.get(0), false, true);
			}
			
			throw e;			
		}	
	}
	
	/**
	 * verify signature at signature rtd
	 * @param records
	 * @param signType
	 * @param cert
	 * @param signature
	 * @return verify status
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws IOException 
	 */
	private void verifySignature(Record [] records, 	 
								 int signType, 	 
								 X509Certificate cert, 
								 byte [] signature,
								 int offset,
								 int len) throws NoSuchAlgorithmException, 
						 				     	 InvalidKeyException, 
						 						 SignatureException, IOException
	{
		if(mDisable)
		{
			throw new IOException("User disable");
		}
		
		Signature signer = Signature.getInstance(JCE_ALGORITHM_STRING[signType]);		
		signer.initVerify(cert);
		
		for(int index = 0;index < records.length;index ++)
		{
			// Sign Type
			signer.update(records[index].getType());
			
			// Sign ID
			if(records[index].getID() == null)
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

		if(!signer.verify(signature, offset, len))
		{
			throw new SignatureException("invalidate signature");
		}
	}
	 
	/**
	 * get certificate from byte array
	 * @param raw
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	private X509Certificate getCertificate(byte[] raw, 
										   int offset, 
										   int len) throws CertificateException, 
														   IOException
	{
		if(mDisable)
		{
			throw new IOException("Use disable");
		}		
		
		ByteArrayInputStream bais = new ByteArrayInputStream(raw, offset, len);
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate)cf.generateCertificate(bais);
		}
		finally
		{
			bais.close();
		}
	}
	
	/**
	 * Validate certificates
	 * @param target
	 * @param intermediate
	 * @throws Exception
	 */
	private void verifyCertificates(X509Certificate target,
									List<X509Certificate> intermediate) throws Exception
	{
		mCertificateChainVerifier.setIntermediateRoot(intermediate);
		mCertificateChainVerifier.setTarget(target, X509CertificateChain.KeyUsage.DIGITAL_SIGNATURE);
		if(mCertificateChainVerifier.check())
		{
			// do nothing
		}
		else
		{
			mCertificateChainVerifier.throwLastException();
		}
	}
	
	/**
	 *  download signature
	 * @param uri
	 * @return
	 * @throws IOException 
	 */
	private byte[] downloadSignature(byte [] uri, int offset, int len) throws IOException
	{
		if(mDisable)
		{
			throw new IOException("Use disable");
		}
		
		String urlString = new String(uri, offset, len, "UTF-8");
		URI mUri = URI.create(urlString);
		URL url = mUri.toURL();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mDownloader.copyTo(url, baos);
		
		return baos.toByteArray();
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 * @throws IOException 
	 * @throws CertificateException 
	 */
	private X509Certificate downloadCertificate(byte [] uri, 
												int offset, 
												int len) throws IOException, 
												 			   	CertificateException
	{
		if(mDisable)
		{
			throw new IOException("Use disable");
		}
		
		String uriString = new String(uri, offset, len, "UTF-8");
		URI mUri = URI.create(uriString);
		URL url = mUri.toURL();
		
		InputStream is = mDownloader.getInputStream(url);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		
		return (X509Certificate)cf.generateCertificate(is);
	}

	/**
	 * Set Connect Timeout
	 * @param timeout
	 */
	@Override
	public void setConnectTimeout(int timeout) {
		mDownloader.setConnectTimeout(timeout);
	}

	/**
	 * Set read timeout
	 * @param timeout
	 */
	@Override
	public void setReadTimeout(int timeout) {
		mDownloader.setReadTimeout(timeout);
	}

	/**
	 * Set disable
	 * @param disable
	 */
	@Override
	public void setDisable(boolean disable) 
	{
		mDisable = disable;
		mDownloader.setDisable(disable);
		mCertificateChainVerifier.setDisable(disable);
		mContinueWhenErrorOccur = disable ? mContinueWhenErrorOccur: false;
	}
	
	/**
	 * Parse NDEF Signature Raw
	 * @param ndefMessage
	 */
	@Override
	public Segment [] parse(byte[] ndefMessageRaw) throws Exception 
	{
		IParser parser = null;
		if(mParserName == null)
		{
			parser = NDEF.createParser();
		}
		else
		{
			parser = NDEF.createParser(mParserName);
		}
		
		List<Segment> sigContents = new ArrayList<Segment>();
		Record [] ndefRecords = parser.getRecords(ndefMessageRaw);
		
		// check this raw is contain signature RTD
		int sigCount = 0;
		for(int i = 0; i < ndefRecords.length; i++)
		{
			if(Arrays.equals(ndefRecords[i].getType(), NDEF.RTD_SIGNATURE))
			{
				sigCount = sigCount + 1; 
			}
		}	
		
		// If there is no any signature, put all records in a segement
		if(sigCount == 0)
		{
			Segment [] mock = new Segment []{new Segment(ndefRecords)};
			return mock;
		}
		
		// verify signature rtds
		List<Record> records = new ArrayList<NDEF.Record>();
		for(int i = 0; i < ndefRecords.length; i++)
		{
			if(mDisable)
			{
				throw new NdefFormatException("User disable");
			}
			
			if(Arrays.equals(ndefRecords[i].getType(), NDEF.RTD_SIGNATURE))
			{
				Segment resp = verifyRecord(records, ndefRecords[i]);
				sigContents.add(resp);
				records.clear();
			}
			else
			{
				records.add(ndefRecords[i]);
			}
		}
		
		//the end record is not signature rtd at  ndef message
		if(records.size() > 0)
		{
			Record [] leafRecords = new Record[records.size()];
			Segment resp = new Segment(records.toArray(leafRecords)) ;
			sigContents.add(resp);
			records.clear();
		}
		
		return sigContents.toArray(new Segment[sigContents.size()]);
	}

	@Override
	public void setRevocationEnable(boolean val) {
		mCertificateChainVerifier.setRevocationEnabled(val);
	}

	@Override
	public void setContinueWhenErrorOccur(boolean val) {
		mContinueWhenErrorOccur = val;
	}

	@Override
	public int getReadTimeout() {
		return mDownloader.getReadTimeout();
	}

	@Override
	public int getConnectTimeout() {
		return mDownloader.getConnectTimeout();
	}
}
