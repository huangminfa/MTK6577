package com.android.nfc.extension.security.cert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CRL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.x509.CRLDistributionPoints;
import org.apache.harmony.xnet.provider.jsse.TrustedCertificateStore;

import com.android.nfc.extension.nfc.log.Logger;
import com.android.nfc.extension.security.cert.X509CertificateChain.ICRLVerifier;
import com.android.nfc.extension.security.cert.X509CertificateChain.IVerifier;
import com.android.nfc.extension.security.cert.X509CertificateChain.KeyUsage;
import com.android.nfc.extension.utility.net.UrlDownloader;
import com.android.nfc.extension.utility.net.UrlDownloader.IUrlDownloader;

/**
 * Certificate Chain Verifier for Android Platform 4.0
 * @author Ray
 * @hide
 */
public class AndroidX509CertificateChainVerifer implements IVerifier 
{
	/**
	 * Program TAG
	 */
	private static final String TAG = "CertificateVerifier";
	
	/**
	 * CRL verifier
	 * @author Ray
	 *
	 */
	private class AndroidCRLVerifier implements ICRLVerifier {
		
		/**
		 * InputStream which can be interrupt
		 * @author vend_iii08
		 *
		 */
		private class InterruptedInputStream extends BufferedInputStream
		{
			public InterruptedInputStream(InputStream in) 
			{
				super(in);
			}
			
			/* (non-Javadoc)
			 * @see java.io.BufferedInputStream#read(byte[], int, int)
			 */
			@Override
			public synchronized int read(byte[] arg0, int arg1, int arg2) throws IOException 
			{
				if(mDisable)
				{
					throw new IOException("User disable");
				}
				
				return super.read(arg0, arg1, arg2);
			}
		}
		
		/**
		 * Constructor for downloading Timeout
		 * @param connectTimeout
		 * @param readTimeout
		 */
		AndroidCRLVerifier()
		{
			// constructor
		}
		
		/**
		 * Certificate factory
		 */
		private CertificateFactory mFactroy;
		
		/**
		 * create a downloader
		 */
		private IUrlDownloader mDownloader = UrlDownloader.createUrlDownloader();
		
		/**
		 * Disable
		 */
		private boolean mDisable = false;
		
		/**
		 * Check CRL validate
		 */
		@Override
		public void check(X509Certificate certificate,
						  String crlLink) throws CertificateChainException 
		{
			CRL crl = null;
			mDisable = false;
			try
			{
				URI uri = new URI(crlLink);
				URL url = uri.toURL();
				
				if(mFactroy == null)
				{			
					mFactroy = CertificateFactory.getInstance("X.509");
				}
				
				InterruptedInputStream bis = new InterruptedInputStream(mDownloader.getInputStream(url));
				try
				{
					crl = mFactroy.generateCRL(bis);
				}
				finally
				{
					bis.close();
				}
			}
			catch(Exception e)
			{
				throw new CertificateChainException(CertificateChainException
													.REASON_CODE_COUND_NOT_OBTAIN_CRL,e);
			}
			
			// check revoke
			if(crl.isRevoked(certificate))
			{
				throw new CertificateChainException(CertificateChainException
						  .REASON_CODE_CERTIFICATE_CHAIN_REVOKED);
			}
		}
		
		/**
		 * Set validator disable
		 * @param val
		 */
		public void setDisable(boolean val)	{
			mDisable = val;
		}

		@Override
		public int getReadTimeout() {		
			return mDownloader.getReadTimeout();
		}

		@Override
		public int getConnectTimeout() {
			return mDownloader.getConnectTimeout();
		}

		@Override
		public void setConnectTimeout(int timeout) {
			mDownloader.setConnectTimeout(timeout);
		}

		@Override
		public void setReadTimeout(int timeout) {
			mDownloader.setReadTimeout(timeout);
		}
	}
	
	
	/**
	 * CRL validate task
	 * @author Ray
	 *
	 */
	private static class CRLTask
	{
		/**
		 * Constructor
		 */
		CRLTask()
		{
			
		}		
		
		/**
		 * Certificate Serial No
		 */
		X509Certificate certificate;
		
		/**
		 * CRL Link
		 */
		String crlLink;
	}
	
	
	/**
	 * Android Certificate Store
	 */
	private TrustedCertificateStore mStore = new TrustedCertificateStore();
	
	/**
	 * For CRL Distributions Points
	 */
	private static final String URL_SYMBOL 		= "uniformResourceIdentifier[6]: ";
	private static final int URL_SYMBOL_LEN 	= 30;
	
	/**
	 * Validate state
	 */
	private static final int VALIDATE_STATE_COMPLETE							= 0x00;
	private static final int VALIDATE_STATE_VALIDATE_TARGET						= 0x01;
	private static final int VALIDATE_STATE_VALIDATE_CHAINS						= 0x02;
	private static final int VALIDATE_STATE_VALIDATE_CRLS						= 0x03;
	
	/**
	 * Default revocation option is closed
	 */
	private boolean mRevocationEnabled = false;
	
	/**
	 * Validate state
	 */
	private int mValidateState;
	
	/**
	 * Last Exception
	 */
	private Exception mException;
	
	/**
	 * Disabled
	 */
	private boolean mDisable = false;
	
	/**
	 * Target
	 */
	private X509Certificate mTarget;
	
	/**
	 * Target Usage
	 */
	private int mTargetUsage;
	
	/**
	 * Intermediate certificate
	 */
	private List<X509Certificate> mIntermediate;
	
	/**
	 * Current Date
	 */
	private Date mValidateDate = new Date(System.currentTimeMillis());
	
	/**
	 * CRL verify task
	 */
	private List<CRLTask> mCRLTasks = new ArrayList<CRLTask>();
	
	/**
	 * Crl verifier
	 */
	private ICRLVerifier mCrlVerifier;
	
	
	/**
	 * Fetch the CRL URL link from X509Certificate
	 * @param cert
	 * @return
	 */
	private String [] fetchX509CRLDownloadURL(X509Certificate cert)
	{
		byte [] pointsRaw = cert.getExtensionValue(X509CertificateChain.CRL_DISTRIBUTION_POINTS);
		if(pointsRaw == null)
		{
			return null;
		}
		
		try 
		{
			// find the Octet String from CRL Distribution Points
			ASN1OctetString parser = ASN1OctetString.getInstance();
			byte [] octetString = (byte[]) parser.decode(pointsRaw);
			
			CRLDistributionPoints distributionPoints = CRLDistributionPoints.decode(octetString);
			StringBuilder buffer = new StringBuilder();
			distributionPoints.dumpValue(buffer);
			
			ArrayList<String> links = new ArrayList<String>();
			int cursor = 0;
			String line = null;
			while(true)
			{
				if((cursor = buffer.indexOf("      ")) < 0)
				{
					break;
				}
				else
				{
					buffer.delete(0, cursor + 6);
					if(URL_SYMBOL.equals(buffer.substring(0, URL_SYMBOL.length())))
					{
						cursor = buffer.indexOf("\n",URL_SYMBOL_LEN);
						line = buffer.substring(URL_SYMBOL_LEN, cursor);
						links.add(line);
					}
					else
					{
						cursor = buffer.indexOf("\n");
					}
					
					buffer.delete(0, cursor);
				}
			}
			
			if(links.size() == 0)
			{
				return null;
			}
			
			return links.toArray(new String[links.size()]);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * Validate Target certificate
	 * @throws CertificateChainException 
	 */
	private void validateTarget() throws CertificateChainException
	{
		try
		{
			mTarget.checkValidity(mValidateDate);
		}
		catch(Exception e)
		{
			throw new CertificateChainException(CertificateChainException
												.REASON_CODE_TARGET_NOT_VALIDITY ,e);
		}
		
		// validate the target certificate's keyusage
		if(mTarget.getKeyUsage()[mTargetUsage])
		{
			// do nothing
		}
		else
		{
			throw new CertificateChainException(CertificateChainException
												.REASON_CODE_TARGET_KEY_USAGE_NOT_MATCH);
		}
	}
	
	/**
	 * Validate CRLs
	 * @throws CertificateChainException
	 */
	private void validateCRLs() throws CertificateChainException
	{
		for(CRLTask task : mCRLTasks)
		{
			if(mDisable)
			{
				throw new CertificateChainException(CertificateChainException
													.REASON_CODE_USER_DISABLE);
			}			
			
			mCrlVerifier.check(task.certificate,
							   task.crlLink);
			
			Logger.getInstance().d(TAG, "validateCRLs()");
		}
		
		mCRLTasks.clear();
	}
	
	/**
	 * Validate certificate chains
	 */
	private void validateChains() throws CertificateChainException
	{
		X509Certificate parent = null;
		X509Certificate self = mTarget;
		boolean isTrust = false;
		
		if(mIntermediate == null || mIntermediate.size() == 0)
		{
			if((parent = mStore.findIssuer(self)) != null)
			{
				isTrust = true;
				
				// check revoke list
				if(mRevocationEnabled)
				{
					String [] crlLink = fetchX509CRLDownloadURL(self);
					if(crlLink == null || crlLink.length == 0)
					{
						throw new CertificateChainException(CertificateChainException
								  .REASON_CODE_COUND_NOT_OBTAIN_CRL);	
					}
					
					CRLTask task = new CRLTask();
					task.certificate = self;
					task.crlLink = crlLink[0];
					mCRLTasks.add(task);
					
					mValidateState = VALIDATE_STATE_VALIDATE_CRLS;
				}
				else
				{
					mValidateState = VALIDATE_STATE_COMPLETE;
				}
				
				return;
			}
			else
			{			
				throw new CertificateChainException(CertificateChainException
													.REASON_CODE_CERTIFICATE_CHAIN_UNTRUSTED);
			}
		}
		
		for(int index = 0;index < mIntermediate.size();index ++)
		{
			if((parent = mStore.findIssuer(self)) != null)
			{
				isTrust = true;
			}
			else
			{
				parent = mIntermediate.get(index);
				if(parent.getKeyUsage()[KeyUsage.KEY_CERT_SIGN])
				{
					// no nothing
				}
				else
				{
					throw new CertificateChainException(CertificateChainException
														.REASON_CODE_CERTIFICATE_KEY_USAGE_NOT_MATCH);
				}
			}
			
			// check validity
			try 
			{
				parent.checkValidity(mValidateDate);
			} 
			catch (Exception e) 
			{
				throw new CertificateChainException(CertificateChainException
						  .REASON_CODE_CERTIFICATE_CHAIN_VALIDITY,e);			
			}
						
			// check relationship
			try 
			{
				self.verify(parent.getPublicKey());
			} 
			catch (Exception e) 
			{
				throw new CertificateChainException(CertificateChainException
						  .REASON_CODE_CERTIFICATE_CHAIN_PARENT_NOT_MATCH ,e);		
			}
			
			// check revoke list
			if(mRevocationEnabled)
			{
				String [] crlLink = fetchX509CRLDownloadURL(self);
				if(crlLink == null || crlLink.length == 0)
				{
					throw new CertificateChainException(CertificateChainException
							  .REASON_CODE_COUND_NOT_OBTAIN_CRL);	
				}
				
				CRLTask task = new CRLTask();
				task.certificate = self;
				task.crlLink = crlLink[0];
				mCRLTasks.add(task);
			}
			
			if(isTrust)
			{
				if(mCRLTasks.size() == 0) 
				{
					mValidateState = VALIDATE_STATE_COMPLETE;
				}
				else 
				{
					mValidateState = VALIDATE_STATE_VALIDATE_CRLS;
				}
				
				return;
			}
			else
			{
				self = parent;
			}
		}
		
		throw new CertificateChainException(CertificateChainException
				  .REASON_CODE_CERTIFICATE_CHAIN_UNTRUSTED);	
	}	

	@Override
	public void setRevocationEnabled(boolean val) 
	{
		if(val)
		{	
			if(mCrlVerifier == null)
			{
				mCrlVerifier = new AndroidCRLVerifier();
			}
		}
		
		mRevocationEnabled = val;
	}

	@Override
	public void setTarget(X509Certificate target, int usage) {
		mTarget = target;
		mTargetUsage = usage;
	}

	@Override
	public void setIntermediateRoot(List<X509Certificate> chains) {
		mIntermediate = chains;
	}

	@Override
	public boolean check() 
	{
		mCRLTasks.clear();
		mDisable = false;
		
		if(mTarget == null)
		{
			throw new IllegalArgumentException("Target must not null");
		}
		
		try
		{
			mValidateState = VALIDATE_STATE_VALIDATE_TARGET;
			while(!mDisable)
			{
				switch(mValidateState)
				{
				case VALIDATE_STATE_VALIDATE_TARGET:					
					validateTarget();
					mValidateState = VALIDATE_STATE_VALIDATE_CHAINS;
					break;
					
				case VALIDATE_STATE_VALIDATE_CHAINS:
					validateChains();
					break;
					
				case VALIDATE_STATE_VALIDATE_CRLS:
					validateCRLs();
					mValidateState = VALIDATE_STATE_COMPLETE;
					break;
					
				case VALIDATE_STATE_COMPLETE:
					return true;
					
				default:
				}
			}
			
			return false;
		}
		catch(Exception e)
		{
			mException = e;
			return false;
		}
	}

	@Override
	public void throwLastException() throws Exception {
		if(mException == null)
		{
			// do nothing
		}
		else
		{
			throw mException;
		}
	}

	@Override
	public Exception getLastException() {
		return mException;
	}
	
	@Override
	public void setValidateDate(Date date) {
		mValidateDate = date;
	}

	@Override
	public void setDisable(boolean val) {
		mDisable = val;
	}

	@Override
	public void setRevocationVerifer(ICRLVerifier verifer) {
		mCrlVerifier = verifer;		
	}

	@Override
	public int getReadTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getConnectTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setConnectTimeout(int timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReadTimeout(int timeout) {
		// TODO Auto-generated method stub
		
	}
}
