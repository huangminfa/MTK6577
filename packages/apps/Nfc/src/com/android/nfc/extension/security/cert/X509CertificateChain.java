package com.android.nfc.extension.security.cert;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * @hide
 */
public class X509CertificateChain
{
	/**
	 * Private Constructor
	 * To avoid create a instance
	 */
	private X509CertificateChain()
	{
		// do nothing
	}
	
	/**
	 * CRL Distribution points for X509Certification extension field
	 */
	public static final String CRL_DISTRIBUTION_POINTS = "2.5.29.31";
	
	/**
	 * Private Constructor
	 * To avoid create a instance
	 */
	public static class KeyUsage
	{
		private KeyUsage()
		{
			// do nothing
		}
		
	    public static final int DIGITAL_SIGNATURE	= 0;
	    public static final int NON_REPUDIATION		= 1;
	    public static final int KEY_ENCIPHERMENT	= 2;
	    public static final int DATA_ENCIPHERMENT	= 3;
	    public static final int KEY_AGREEMENT		= 4;
	    public static final int KEY_CERT_SIGN		= 5;
	    public static final int CRL_SIGN			= 6;
	    public static final int ENCIPHER_ONLY		= 7;
	    public static final int DECIPHER_ONLY		= 8;
	}
	
	/**
	 * CRL check interface
	 * @author vend_iii08
	 *
	 */
	public interface ICRLVerifier
	{
		/**
		 * Check crl 
		 * @param certNo
		 * @param crlSigner
		 * @param crlLink
		 * @return
		 */
		void check(X509Certificate certiticate, String crlLink) throws CertificateChainException;
		
		/**
		 * Set the validator disabled
		 * @param val
		 */
		void setDisable(boolean val);
		
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
	}
	
	
	/**
	 * Verify the certificate 
	 * @author Ray
	 *
	 */
	public interface IVerifier 
	{
		void setDisable(boolean val);
		void setRevocationEnabled(boolean val);
		void setRevocationVerifer(ICRLVerifier verifier);
		void setTarget(X509Certificate target, int usage);
		void setIntermediateRoot(List<X509Certificate> chains);
		void setValidateDate(Date date);
		
		boolean check();
		
		void throwLastException() throws Exception;
		Exception getLastException();
		
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
	}
}
