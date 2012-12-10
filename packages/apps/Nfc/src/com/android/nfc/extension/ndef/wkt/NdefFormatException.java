package com.android.nfc.extension.ndef.wkt;

/**
 * @hide
 */
public class NdefFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 669817942820683381L;

	/**
	 * Constructor
	 * @param detailMessage
	 */
	public NdefFormatException(String detailMessage) {
		super(detailMessage);
	}
}
