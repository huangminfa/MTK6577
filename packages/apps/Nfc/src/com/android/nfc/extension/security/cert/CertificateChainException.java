package com.android.nfc.extension.security.cert;

/**
 * @hide
 */
public class CertificateChainException extends Exception {
	
	public static final long REASON_CODE_USER_DISABLE							= 0x7300;
	public static final long REASON_CODE_TARGET_NOT_VALIDITY					= 0x7301;
	public static final long REASON_CODE_TARGET_KEY_USAGE_NOT_MATCH				= 0x7302;
	public static final long REASON_CODE_TARGET_PARENT_NOT_MATCH				= 0x7303;
	public static final long REASON_CODE_TARGET_REVOKED							= 0x7304;
	public static final long REASON_CODE_CERTIFICATE_CHAIN_VALIDITY				= 0x7305;
	public static final long REASON_CODE_CERTIFICATE_KEY_USAGE_NOT_MATCH		= 0x7306;
	public static final long REASON_CODE_CERTIFICATE_CHAIN_PARENT_NOT_MATCH		= 0x7307;
	public static final long REASON_CODE_CERTIFICATE_CHAIN_REVOKED				= 0x7308;
	public static final long REASON_CODE_CERTIFICATE_CHAIN_UNTRUSTED			= 0x7309;
	public static final long REASON_CODE_COUND_NOT_OBTAIN_CRL					= 0x7310;

	/**
	 * 
	 */
	private static final long serialVersionUID = -4703095400396075947L;
	
	/**
	 * Reason code
	 */
	private long mReasonCode;
	
	/**
	 * Certificate Chain
	 * @param resonCode
	 */
	public CertificateChainException(long resonCode,Throwable e) {
		super(e);
		mReasonCode = resonCode;
	}
	
	/**
	 * Certificate Chain
	 * @param resonCode
	 */
	public CertificateChainException(long resonCode) {
		mReasonCode = resonCode;
	}

	/**
	 * @return the mReasonCode
	 */
	public long getReasonCode() {
		return mReasonCode;
	}


}
