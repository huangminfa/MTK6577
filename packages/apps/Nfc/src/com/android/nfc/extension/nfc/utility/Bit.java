package com.android.nfc.extension.nfc.utility;

/**
 * @hide
 */
public class Bit {

	/**
	 * check bit status by position
	 * @param content
	 * @param position
	 * @return
	 */
	public static boolean checkBitStatus(byte content, int position)
	{
		byte flag = (byte) (1 << position);				
		return ((content & flag) == flag);
	}
	
	/**
	 * set mask
	 * @param src
	 * @param mask
	 * @return
	 */
	public static byte setMask(byte src, int mask){
		return (byte)(src & mask);
	}
}
