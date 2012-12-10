package com.android.nfc.extension.nfc.utility;

/**
 * @hide
 */
public class Convert {

	public static String toHex(String src){						
		return toHex(src.getBytes());
	}
	
	public static String toHex(byte[] src){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < src.length; i++){						
			sb.append("0x");
			if(Integer.toHexString((int)src[i]).length() < 2)
				sb.append("0");
			
			sb.append(Integer.toHexString((int)src[i]));
			sb.append("  ");
		}
		return sb.toString();
	}	
	
	public static short toShort(byte[] src){
		short rs = 0;
		for(int i = 0; i < src.length; i++){
			rs = (short)((rs << 8) | (src[i] & 0xFF));
		}
		return rs;
	}
	
	public static short toShort(byte [] src, int offset, int len){
		short rs = 0;
		for(int i = 0; i < len; i++){
			rs = (short)((rs << 8) | (src[i + offset] & 0xFF));
		}
		return rs;
	}
}
